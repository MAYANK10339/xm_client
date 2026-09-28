package com.xmclient.mobile.core

/**
 * Strict version lock as specified: Only 1.21.1 and 1.20.1 are supported.
 */
enum class MinecraftVersion(val versionString: String, val displayName: String, val recommendedJava: Int) {
    V_1_21_1("1.21.1", "Minecraft 1.21.1", 21),
    V_1_20_1("1.20.1", "Minecraft 1.20.1", 17);

    companion object {
        fun fromString(str: String): MinecraftVersion {
            return entries.find { it.versionString == str } ?: V_1_21_1
        }
    }
}

/**
 * Strict loader lock as specified: Only Fabric and NeoForge are supported.
 */
enum class ModLoader(val id: String, val displayName: String) {
    FABRIC("fabric", "Fabric"),
    NEOFORGE("neoforge", "NeoForge");

    companion object {
        fun fromString(str: String): ModLoader {
            return entries.find { it.id.equals(str, ignoreCase = true) } ?: FABRIC
        }
    }
}
