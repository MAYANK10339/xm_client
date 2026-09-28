package com.xmclient.mobile.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xmclient.mobile.R
import com.xmclient.mobile.XMApplication
import com.xmclient.mobile.core.LaunchEngine
import com.xmclient.mobile.core.MinecraftVersion
import com.xmclient.mobile.core.ModLoader
import com.xmclient.mobile.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val profileManager by lazy { (application as XMApplication).profileManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupVersionAndLoaderSelectors()
        setupMemoryCard()
        setupNavigation()
    }

    private fun setupVersionAndLoaderSelectors() {
        // Version 1.21.1 vs 1.20.1 (Strictly Locked)
        updateVersionUi(profileManager.currentVersion)
        binding.btnVer1211.setOnClickListener {
            profileManager.currentVersion = MinecraftVersion.V_1_21_1
            updateVersionUi(MinecraftVersion.V_1_21_1)
        }
        binding.btnVer1201.setOnClickListener {
            profileManager.currentVersion = MinecraftVersion.V_1_20_1
            updateVersionUi(MinecraftVersion.V_1_20_1)
        }

        // Loader Fabric vs NeoForge (Strictly Locked)
        updateLoaderUi(profileManager.currentLoader)
        binding.btnLoaderFabric.setOnClickListener {
            profileManager.currentLoader = ModLoader.FABRIC
            updateLoaderUi(ModLoader.FABRIC)
        }
        binding.btnLoaderNeoforge.setOnClickListener {
            profileManager.currentLoader = ModLoader.NEOFORGE
            updateLoaderUi(ModLoader.NEOFORGE)
        }
    }

    private fun updateVersionUi(version: MinecraftVersion) {
        if (version == MinecraftVersion.V_1_21_1) {
            binding.btnVer1211.setBackgroundResource(R.drawable.bg_button_crimson)
            binding.btnVer1211.setTextColor(Color.WHITE)
            binding.btnVer1201.setBackgroundColor(Color.parseColor("#27272A"))
            binding.btnVer1201.setTextColor(Color.parseColor("#A1A1AA"))
        } else {
            binding.btnVer1201.setBackgroundResource(R.drawable.bg_button_crimson)
            binding.btnVer1201.setTextColor(Color.WHITE)
            binding.btnVer1211.setBackgroundColor(Color.parseColor("#27272A"))
            binding.btnVer1211.setTextColor(Color.parseColor("#A1A1AA"))
        }
    }

    private fun updateLoaderUi(loader: ModLoader) {
        if (loader == ModLoader.FABRIC) {
            binding.btnLoaderFabric.setBackgroundResource(R.drawable.bg_button_crimson)
            binding.btnLoaderFabric.setTextColor(Color.WHITE)
            binding.btnLoaderNeoforge.setBackgroundColor(Color.parseColor("#27272A"))
            binding.btnLoaderNeoforge.setTextColor(Color.parseColor("#A1A1AA"))
        } else {
            binding.btnLoaderNeoforge.setBackgroundResource(R.drawable.bg_button_crimson)
            binding.btnLoaderNeoforge.setTextColor(Color.WHITE)
            binding.btnLoaderFabric.setBackgroundColor(Color.parseColor("#27272A"))
            binding.btnLoaderFabric.setTextColor(Color.parseColor("#A1A1AA"))
        }
    }

    private fun setupMemoryCard() {
        binding.tvAllocatedRam.text = "Allocated RAM: ${profileManager.allocatedRamMb} MB (Aikar G1GC Active)"
    }

    private fun setupNavigation() {
        // Open XMods Store
        binding.btnOpenXmods.setOnClickListener {
            val intent = Intent(this, XModsActivity::class.java)
            startActivity(intent)
        }

        // Open Virtual Controls Editor
        binding.btnOpenControls.setOnClickListener {
            val intent = Intent(this, ControlEditorActivity::class.java)
            startActivity(intent)
        }

        // Launch Game
        binding.btnLaunch.setOnClickListener {
            val ver = profileManager.currentVersion
            val loader = profileManager.currentLoader
            LaunchEngine.prepareLaunch(profileManager.gameRootDirectory, ver, loader)
            Toast.makeText(this, "Launching XM Client (${ver.displayName} • ${loader.displayName})...", Toast.LENGTH_LONG).show()
        }
    }
}
