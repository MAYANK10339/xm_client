package com.xmclient.mobile.xmods

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.xmclient.mobile.core.MinecraftVersion
import com.xmclient.mobile.core.ModLoader
import com.xmclient.mobile.xmods.model.ModDependency
import com.xmclient.mobile.xmods.model.ModFile
import com.xmclient.mobile.xmods.model.UnifiedMod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class CurseForgeService(private val client: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()
) {
    private val gson = Gson()

    companion object {
        private const val API_KEY = "$2a$10$nG7BYyOwf3t09Ibut0eA4Ort7hIxI/oYWOqjO0phh.FIz/mwvaEPO"
        private const val BASE_URL = "https://api.curseforge.com/v1"
        private const val GAME_ID = 432
        private const val CLASS_MODS = 6
    }

    suspend fun searchMods(
        query: String,
        version: MinecraftVersion,
        loader: ModLoader,
        limit: Int = 20,
        offset: Int = 0
    ): List<UnifiedMod> = withContext(Dispatchers.IO) {
        val loaderType = when (loader) {
            ModLoader.FABRIC -> 4
            ModLoader.NEOFORGE -> 6
        }

        val urlBuilder = "$BASE_URL/mods/search".toHttpUrlOrNull()?.newBuilder()
            ?: return@withContext emptyList()

        urlBuilder.addQueryParameter("gameId", GAME_ID.toString())
        urlBuilder.addQueryParameter("classId", CLASS_MODS.toString())
        urlBuilder.addQueryParameter("pageSize", limit.toString())
        urlBuilder.addQueryParameter("index", offset.toString())
        urlBuilder.addQueryParameter("sortField", "2") // Popularity
        urlBuilder.addQueryParameter("sortOrder", "desc")
        urlBuilder.addQueryParameter("gameVersion", version.versionString)
        urlBuilder.addQueryParameter("modLoaderType", loaderType.toString())
        if (query.isNotEmpty()) {
            urlBuilder.addQueryParameter("searchFilter", query)
        }

        val request = Request.Builder()
            .url(urlBuilder.build())
            .header("x-api-key", API_KEY)
            .header("User-Agent", "XMClient-Mobile/1.0.0")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val json = response.body?.string() ?: return@withContext emptyList()
                val root = gson.fromJson(json, JsonObject::class.java)
                val data = root.getAsJsonArray("data") ?: return@withContext emptyList()

                val results = mutableListOf<UnifiedMod>()
                for (item in data) {
                    val obj = item.asJsonObject
                    val authors = obj.getAsJsonArray("authors")
                    val authorName = if (authors != null && authors.size() > 0) {
                        authors[0].asJsonObject.get("name")?.asString ?: "Unknown"
                    } else "Unknown"

                    val logo = obj.getAsJsonObject("logo")
                    val iconUrl = logo?.get("thumbnailUrl")?.takeIf { !it.isJsonNull }?.asString

                    results.add(
                        UnifiedMod(
                            id = obj.get("id").asString,
                            slug = obj.get("slug")?.asString ?: obj.get("id").asString,
                            title = obj.get("name")?.asString ?: "",
                            description = obj.get("summary")?.asString ?: "",
                            author = authorName,
                            iconUrl = iconUrl,
                            downloads = obj.get("downloadCount")?.asLong ?: 0L,
                            source = "CurseForge",
                            loaders = listOf(loader.id),
                            gameVersions = listOf(version.versionString)
                        )
                    )
                }
                results
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getModFiles(
        modId: String,
        version: MinecraftVersion,
        loader: ModLoader
    ): List<ModFile> = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/mods/$modId/files?pageSize=30"
        val request = Request.Builder()
            .url(url)
            .header("x-api-key", API_KEY)
            .header("User-Agent", "XMClient-Mobile/1.0.0")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val json = response.body?.string() ?: return@withContext emptyList()
                val root = gson.fromJson(json, JsonObject::class.java)
                val data = root.getAsJsonArray("data") ?: return@withContext emptyList()

                val filesList = mutableListOf<ModFile>()
                for (fileElem in data) {
                    val fObj = fileElem.asJsonObject
                    val gameVersions = fObj.getAsJsonArray("gameVersions")?.map { it.asString.lowercase() } ?: emptyList()

                    // Strict filter: must match version and loader
                    if (!gameVersions.contains(version.versionString.lowercase())) continue
                    if (!gameVersions.contains(loader.id.lowercase())) continue

                    val fileId = fObj.get("id").asString
                    val fileName = fObj.get("fileName").asString
                    var downloadUrl = fObj.get("downloadUrl")?.takeIf { !it.isJsonNull }?.asString

                    // If downloadUrl is null, query CurseForge download-url endpoint
                    if (downloadUrl.isNullOrEmpty()) {
                        downloadUrl = fetchDirectDownloadUrl(modId, fileId)
                    }

                    if (downloadUrl.isNullOrEmpty()) continue

                    // Parse dependencies
                    val depsList = mutableListOf<ModDependency>()
                    val depArray = fObj.getAsJsonArray("dependencies")
                    if (depArray != null) {
                        for (d in depArray) {
                            val dObj = d.asJsonObject
                            val relType = dObj.get("relationType")?.asInt ?: 0
                            val depModId = dObj.get("modId")?.asString ?: continue

                            // 3 = RequiredDependency, 1 = EmbeddedLibrary
                            if (relType == 3 || relType == 1) {
                                depsList.add(
                                    ModDependency(
                                        id = depModId,
                                        name = "CurseForge Dependency #$depModId",
                                        source = "CurseForge",
                                        isRequired = (relType == 3)
                                    )
                                )
                            }
                        }
                    }

                    filesList.add(
                        ModFile(
                            fileId = fileId,
                            fileName = fileName,
                            downloadUrl = downloadUrl,
                            gameVersion = version.versionString,
                            loader = loader.id,
                            dependencies = depsList
                        )
                    )
                }
                filesList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun fetchDirectDownloadUrl(modId: String, fileId: String): String? {
        val url = "$BASE_URL/mods/$modId/files/$fileId/download-url"
        val request = Request.Builder()
            .url(url)
            .header("x-api-key", API_KEY)
            .build()

        return try {
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val body = resp.body?.string() ?: return null
                val root = gson.fromJson(body, JsonObject::class.java)
                root.get("data")?.takeIf { !it.isJsonNull }?.asString
            }
        } catch (_: Exception) {
            null
        }
    }
}
