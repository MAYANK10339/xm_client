package com.xmclient.mobile.core

import java.io.File

object LaunchEngine {

    /**
     * Generates XM Aikar G1GC JVM parameters tuned for Android ARM64 phones.
     */
    fun buildJvmArgs(allocatedRamMb: Int): List<String> {
        return listOf(
            "-Xms${allocatedRamMb / 2}M",
            "-Xmx${allocatedRamMb}M",
            "-XX:+UseG1GC",
            "-XX:+ParallelRefProcEnabled",
            "-XX:MaxGCPauseMillis=200",
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:+DisableExplicitGC",
            "-XX:+AlwaysPreTouch",
            "-XX:G1NewSizePercent=30",
            "-XX:G1MaxNewSizePercent=40",
            "-XX:G1ReservePercent=20",
            "-XX:G1HeapWastePercent=5",
            "-XX:G1MixedGCCountTarget=4",
            "-XX:InitiatingHeapOccupancyPercent=15",
            "-XX:G1MixedGCLiveThresholdPercent=90",
            "-XX:G1RSetUpdatingPauseTimePercent=5",
            "-XX:SurvivorRatio=32",
            "-XX:+PerfDisableSharedMem",
            "-XX:MaxTenuringThreshold=1",
            "-Dminecraft.launcher.brand=XMClient-Mobile",
            "-Dminecraft.launcher.version=1.0.0"
        )
    }

    /**
     * Prepares launch environment before booting game runtime.
     */
    fun prepareLaunch(gameDir: File, version: MinecraftVersion, loader: ModLoader): Boolean {
        // Verify game directories
        val modsDir = File(gameDir, "mods")
        if (!modsDir.exists()) modsDir.mkdirs()

        val configDir = File(gameDir, "config")
        if (!configDir.exists()) configDir.mkdirs()

        return true
    }
}
