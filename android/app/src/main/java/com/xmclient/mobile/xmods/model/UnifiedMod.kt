package com.xmclient.mobile.xmods.model

data class UnifiedMod(
    val id: String,
    val slug: String,
    val title: String,
    val description: String,
    val author: String,
    val iconUrl: String?,
    val downloads: Long,
    val source: String, // "Modrinth" or "CurseForge"
    val loaders: List<String> = emptyList(),
    val gameVersions: List<String> = emptyList(),
    var isInstalled: Boolean = false,
    var installedFileName: String? = null
)

data class ModDependency(
    val id: String,
    val name: String,
    val source: String,
    val isRequired: Boolean = true,
    var isInstalled: Boolean = false,
    val downloadUrl: String? = null,
    val fileName: String? = null
)

data class ModFile(
    val fileId: String,
    val fileName: String,
    val downloadUrl: String,
    val gameVersion: String,
    val loader: String,
    val dependencies: List<ModDependency> = emptyList()
)
