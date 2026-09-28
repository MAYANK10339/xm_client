package com.xmclient.mobile

import android.app.Application
import com.xmclient.mobile.core.ProfileManager
import com.xmclient.mobile.xmods.InstalledModsTracker
import java.io.File

class XMApplication : Application() {

    lateinit var profileManager: ProfileManager
        private set

    lateinit var modsTracker: InstalledModsTracker
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        profileManager = ProfileManager(this)
        
        // Root game directory: Android/data/com.xmclient.mobile/files/.minecraft
        val gameDir = getExternalFilesDir(null)?.resolve(".minecraft") ?: File(filesDir, ".minecraft")
        if (!gameDir.exists()) {
            gameDir.mkdirs()
        }
        
        modsTracker = InstalledModsTracker(gameDir)
    }

    companion object {
        lateinit var instance: XMApplication
            private set
    }
}
