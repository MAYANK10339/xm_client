package com.xmclient.mobile.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xmclient.mobile.R
import com.xmclient.mobile.XMApplication
import com.xmclient.mobile.databinding.ActivityXmodsBinding
import com.xmclient.mobile.xmods.XModsManager
import com.xmclient.mobile.xmods.model.UnifiedMod
import kotlinx.coroutines.launch

class XModsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityXmodsBinding
    private val app by lazy { application as XMApplication }
    private lateinit var xmodsManager: XModsManager
    private lateinit var adapter: XModsAdapter

    private var currentFilter = "ALL" // "ALL", "MODRINTH", "CURSEFORGE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityXmodsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        xmodsManager = XModsManager(app.modsTracker, app.profileManager.modsDirectory)

        setupUI()
        loadMods("")
    }

    override fun onResume() {
        super.onResume()
        // Re-check installed statuses in case user uninstalled/installed in details
        val query = binding.etSearch.text.toString().trim()
        loadMods(query)
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        val ver = app.profileManager.currentVersion.versionString
        val loader = app.profileManager.currentLoader.displayName.uppercase()
        binding.tvActiveTarget.text = "$ver • $loader"

        // Setup RecyclerView
        adapter = XModsAdapter(
            mods = emptyList(),
            onModClick = { mod ->
                val intent = Intent(this, ModDetailActivity::class.java).apply {
                    putExtra("mod_id", mod.id)
                    putExtra("mod_slug", mod.slug)
                    putExtra("mod_title", mod.title)
                    putExtra("mod_desc", mod.description)
                    putExtra("mod_author", mod.author)
                    putExtra("mod_icon", mod.iconUrl)
                    putExtra("mod_source", mod.source)
                    putExtra("mod_installed", mod.isInstalled)
                    putExtra("mod_installed_file", mod.installedFileName)
                }
                startActivity(intent)
            },
            onInstallClick = { mod ->
                installModDirect(mod)
            }
        )
        binding.rvMods.layoutManager = LinearLayoutManager(this)
        binding.rvMods.adapter = adapter

        // Search button & keyboard action
        binding.btnSearchGo.setOnClickListener {
            val q = binding.etSearch.text.toString().trim()
            loadMods(q)
        }

        // Filter toggles
        binding.filterAll.setOnClickListener {
            currentFilter = "ALL"
            updateFilterButtons()
            loadMods(binding.etSearch.text.toString().trim())
        }
        binding.filterModrinth.setOnClickListener {
            currentFilter = "MODRINTH"
            updateFilterButtons()
            loadMods(binding.etSearch.text.toString().trim())
        }
        binding.filterCurseforge.setOnClickListener {
            currentFilter = "CURSEFORGE"
            updateFilterButtons()
            loadMods(binding.etSearch.text.toString().trim())
        }
    }

    private fun updateFilterButtons() {
        val activeBg = R.drawable.bg_button_crimson
        val inactiveBg = Color.parseColor("#27272A")

        binding.filterAll.setBackgroundResource(if (currentFilter == "ALL") activeBg else 0)
        if (currentFilter != "ALL") binding.filterAll.setBackgroundColor(inactiveBg)

        binding.filterModrinth.setBackgroundResource(if (currentFilter == "MODRINTH") activeBg else 0)
        if (currentFilter != "MODRINTH") binding.filterModrinth.setBackgroundColor(inactiveBg)

        binding.filterCurseforge.setBackgroundResource(if (currentFilter == "CURSEFORGE") activeBg else 0)
        if (currentFilter != "CURSEFORGE") binding.filterCurseforge.setBackgroundColor(inactiveBg)
    }

    private fun loadMods(query: String) {
        binding.progressLoading.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE

        lifecycleScope.launch {
            val results = xmodsManager.searchAllMods(
                query = query,
                version = app.profileManager.currentVersion,
                loader = app.profileManager.currentLoader,
                filterSource = currentFilter
            )

            binding.progressLoading.visibility = View.GONE
            if (results.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
            } else {
                binding.tvEmpty.visibility = View.GONE
            }
            adapter.updateList(results)
        }
    }

    private fun installModDirect(mod: UnifiedMod) {
        if (mod.isInstalled) {
            Toast.makeText(this, "${mod.title} is already installed!", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Resolving ${mod.title}...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            val modFile = xmodsManager.getModDetails(mod, app.profileManager.currentVersion, app.profileManager.currentLoader)
            if (modFile == null) {
                Toast.makeText(this@XModsActivity, "No compatible download file found for ${mod.title}!", Toast.LENGTH_LONG).show()
                return@launch
            }

            // Check if mod has required dependencies
            val requiredDeps = modFile.dependencies.filter { it.isRequired && !it.isInstalled }
            if (requiredDeps.isNotEmpty()) {
                Toast.makeText(this@XModsActivity, "${mod.title} requires ${requiredDeps.size} dependency! Opening details...", Toast.LENGTH_LONG).show()
                val intent = Intent(this@XModsActivity, ModDetailActivity::class.java).apply {
                    putExtra("mod_id", mod.id)
                    putExtra("mod_slug", mod.slug)
                    putExtra("mod_title", mod.title)
                    putExtra("mod_desc", mod.description)
                    putExtra("mod_author", mod.author)
                    putExtra("mod_icon", mod.iconUrl)
                    putExtra("mod_source", mod.source)
                    putExtra("mod_installed", mod.isInstalled)
                }
                startActivity(intent)
                return@launch
            }

            // Download mod file
            val success = xmodsManager.downloadModFile(modFile.downloadUrl, modFile.fileName)
            if (success) {
                Toast.makeText(this@XModsActivity, "Successfully installed ${mod.title}!", Toast.LENGTH_SHORT).show()
                mod.isInstalled = true
                mod.installedFileName = modFile.fileName
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this@XModsActivity, "Download failed for ${mod.title}!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
