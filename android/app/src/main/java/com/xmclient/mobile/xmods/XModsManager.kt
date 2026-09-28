package com.xmclient.mobile.xmods

import com.xmclient.mobile.core.MinecraftVersion
import com.xmclient.mobile.core.ModLoader
import com.xmclient.mobile.xmods.model.ModDependency
import com.xmclient.mobile.xmods.model.ModFile
import com.xmclient.mobile.xmods.model.UnifiedMod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class XModsManager(
    private val tracker: InstalledModsTracker,
    private val modsDirectory: File
) {
    private val modrinth = ModrinthService()
    private val curseForge = CurseForgeService()
    private val httpClient = OkHttpClient()

    /**
     * Unified search: Queries Modrinth and CurseForge simultaneously,
     * combines the results, removes duplicates, and marks installed mods.
     */
    suspend fun searchAllMods(
        query: String,
        version: MinecraftVersion,
        loader: ModLoader,
        filterSource: String = "ALL" // "ALL", "MODRINTH", "CURSEFORGE"
    ): List<UnifiedMod> = withContext(Dispatchers.IO) {
        val mrDeferred = async {
            if (filterSource == "ALL" || filterSource == "MODRINTH") {
                modrinth.searchMods(query, version, loader, limit = 25)
            } else emptyList()
        }

        val cfDeferred = async {
            if (filterSource == "ALL" || filterSource == "CURSEFORGE") {
                curseForge.searchMods(query, version, loader, limit = 25)
            } else emptyList()
        }

        val mrResults = mrDeferred.await()
        val cfResults = cfDeferred.await()

        // Combine and deduplicate by slug/title
        val combined = mutableListOf<UnifiedMod>()
        val seenSlugs = mutableSetOf<String>()

        // Add Modrinth results first
        for (m in mrResults) {
            val key = m.slug.lowercase(Locale.ROOT)
            seenSlugs.add(key)
            tracker.isModInstalled(m)
            combined.add(m)
        }

        // Add CurseForge results if not already present
        for (m in cfResults) {
            val key = m.slug.lowercase(Locale.ROOT)
            if (!seenSlugs.contains(key)) {
                seenSlugs.add(key)
                tracker.isModInstalled(m)
                combined.add(m)
            }
        }

        combined
    }

    /**
     * Resolves files and dependencies for a selected mod.
     */
    suspend fun getModDetails(
        mod: UnifiedMod,
        version: MinecraftVersion,
        loader: ModLoader
    ): ModFile? {
        val files = if (mod.source == "Modrinth") {
            modrinth.getModFiles(mod.id, version, loader)
        } else {
            curseForge.getModFiles(mod.id, version, loader)
        }

        val primaryFile = files.firstOrNull() ?: return null

        // Check which dependencies are already installed
        for (dep in primaryFile.dependencies) {
            dep.isInstalled = tracker.isFileInstalled(dep.name) || tracker.isFileInstalled(dep.id)
        }

        return primaryFile
    }

    /**
     * Downloads a mod file directly to .minecraft/mods.
     * Prevents duplicate download if file exists.
     */
    suspend fun downloadModFile(
        fileUrl: String,
        fileName: String,
        progressCallback: ((Int) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val target = File(modsDirectory, fileName)
        if (target.exists()) {
            return@withContext true // Already installed, do not re-download
        }

        val tempFile = File(modsDirectory, "$fileName.tmp")
        val request = Request.Builder()
            .url(fileUrl)
            .header("User-Agent", "XMClient-Mobile/1.0.0")
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext false
                val body = response.body ?: return@withContext false
                val totalLength = body.contentLength()

                body.byteStream().use { input ->
                    FileOutputStream(tempFile).use { output ->
                        val buffer = ByteArray(16384)
                        var bytesRead: Int
                        var totalDownloaded = 0L

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalDownloaded += bytesRead
                            if (totalLength > 0 && progressCallback != null) {
                                val progress = (totalDownloaded * 100 / totalLength).toInt()
                                progressCallback(progress)
                            }
                        }
                    }
                }

                if (target.exists()) target.delete()
                tempFile.renameTo(target)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (tempFile.exists()) tempFile.delete()
            false
        }
    }
}
