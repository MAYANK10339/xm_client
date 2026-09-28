package com.xmclient.mobile.core

import android.content.Context
import android.content.SharedPreferences
import java.io.File

class ProfileManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("xm_client_prefs", Context.MODE_PRIVATE)

    var currentVersion: MinecraftVersion
        get() {
            val ver = prefs.getString("current_version", MinecraftVersion.V_1_21_1.versionString) ?: "1.21.1"
            return MinecraftVersion.fromString(ver)
        }
        set(value) {
            prefs.edit().putString("current_version", value.versionString).apply()
        }

    var currentLoader: ModLoader
        get() {
            val loader = prefs.getString("current_loader", ModLoader.FABRIC.id) ?: "fabric"
            return ModLoader.fromString(loader)
        }
        set(value) {
            prefs.edit().putString("current_loader", value.id).apply()
        }

    var allocatedRamMb: Int
        get() = prefs.getInt("allocated_ram_mb", 3072) // 3GB default for smooth mobile gameplay
        set(value) {
            prefs.edit().putInt("allocated_ram_mb", value).apply()
        }

    val gameRootDirectory: File
        get() {
            val base = context.getExternalFilesDir(null) ?: context.filesDir
            val mcDir = File(base, ".minecraft")
            if (!mcDir.exists()) mcDir.mkdirs()
            return mcDir
        }

    val modsDirectory: File
        get() {
            val dir = File(gameRootDirectory, "mods")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    val controlsDirectory: File
        get() {
            val dir = File(gameRootDirectory, "control_layouts")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }
}
