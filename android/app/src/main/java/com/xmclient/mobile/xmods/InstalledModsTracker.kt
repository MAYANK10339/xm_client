package com.xmclient.mobile.xmods

import com.xmclient.mobile.xmods.model.UnifiedMod
import java.io.File
import java.util.Locale

class InstalledModsTracker(private val gameDir: File) {

    private val modsDir: File
        get() = File(gameDir, "mods").apply { if (!exists()) mkdirs() }

    /**
     * Scans and returns all jar files currently in the mods folder.
     */
    fun getInstalledJars(): List<File> {
        val files = modsDir.listFiles { file ->
            file.isFile && file.name.endsWith(".jar", ignoreCase = true)
        } ?: emptyArray()

        // Clean up accidental duplicate copy files (e.g. "mod - Copy.jar", "mod (1).jar")
        purgeAccidentalDuplicates(files)

        return modsDir.listFiles { file ->
            file.isFile && file.name.endsWith(".jar", ignoreCase = true)
        }?.toList() ?: emptyList()
    }

    private fun purgeAccidentalDuplicates(files: Array<File>) {
        for (f in files) {
            val name = f.name.lowercase(Locale.ROOT)
            if (name.contains(" - copy") || name.contains(" - copy (") || name.matches(Regex(".*\\s\\(\\d+\\)\\.jar$"))) {
                try {
                    f.delete()
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Checks if a given mod is already installed in the mods folder.
     * Prevents duplicate installations.
     */
    fun isModInstalled(mod: UnifiedMod): Boolean {
        val installedJars = getInstalledJars()
        val slugClean = mod.slug.lowercase(Locale.ROOT).replace("-", "").replace("_", "")
        val titleClean = mod.title.lowercase(Locale.ROOT).replace(" ", "").replace("-", "")

        for (jar in installedJars) {
            val jarName = jar.name.lowercase(Locale.ROOT).replace("-", "").replace("_", "")
            if (jarName.contains(slugClean) || jarName.contains(titleClean)) {
                mod.isInstalled = true
                mod.installedFileName = jar.name
                return true
            }
        }
        mod.isInstalled = false
        mod.installedFileName = null
        return false
    }

    /**
     * Checks if a specific file name or dependency exists.
     */
    fun isFileInstalled(fileName: String): Boolean {
        val target = File(modsDir, fileName)
        if (target.exists()) return true

        val clean = fileName.lowercase(Locale.ROOT).replace(".jar", "").split("-", "_").firstOrNull() ?: return false
        return getInstalledJars().any { it.name.lowercase(Locale.ROOT).contains(clean) }
    }

    /**
     * Safely uninstalls/deletes a mod jar.
     */
    fun uninstallMod(fileName: String): Boolean {
        val target = File(modsDir, fileName)
        return if (target.exists()) {
            target.delete()
        } else false
    }
}
