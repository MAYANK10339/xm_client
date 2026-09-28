package com.xmclient.mobile.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.RoundedCornersTransformation
import com.xmclient.mobile.R
import com.xmclient.mobile.XMApplication
import com.xmclient.mobile.databinding.ActivityModDetailBinding
import com.xmclient.mobile.xmods.XModsManager
import com.xmclient.mobile.xmods.model.ModDependency
import com.xmclient.mobile.xmods.model.ModFile
import com.xmclient.mobile.xmods.model.UnifiedMod
import kotlinx.coroutines.launch

class ModDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModDetailBinding
    private val app by lazy { application as XMApplication }
    private lateinit var xmodsManager: XModsManager
    private lateinit var depAdapter: DependenciesAdapter

    private var activeMod: UnifiedMod? = null
    private var activeFile: ModFile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        xmodsManager = XModsManager(app.modsTracker, app.profileManager.modsDirectory)

        setupModFromIntent()
        setupUI()
        loadDependencies()
    }

    private fun setupModFromIntent() {
        val id = intent.getStringExtra("mod_id") ?: ""
        val slug = intent.getStringExtra("mod_slug") ?: id
        val title = intent.getStringExtra("mod_title") ?: "Mod Details"
        val desc = intent.getStringExtra("mod_desc") ?: ""
        val author = intent.getStringExtra("mod_author") ?: "Unknown"
        val icon = intent.getStringExtra("mod_icon")
        val source = intent.getStringExtra("mod_source") ?: "Modrinth"
        val isInstalled = intent.getBooleanExtra("mod_installed", false)
        val installedFile = intent.getStringExtra("mod_installed_file")

        activeMod = UnifiedMod(
            id = id,
            slug = slug,
            title = title,
            description = desc,
            author = author,
            iconUrl = icon,
            downloads = 0L,
            source = source,
            isInstalled = isInstalled,
            installedFileName = installedFile
        )
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        val mod = activeMod ?: return
        binding.tvDetailName.text = mod.title
        binding.tvDetailMeta.text = "Source: ${mod.source} • by ${mod.author}"
        binding.tvDetailDesc.text = if (mod.description.isNotBlank()) mod.description else "No detailed description provided."

        if (!mod.iconUrl.isNullOrEmpty()) {
            binding.ivDetailIcon.load(mod.iconUrl) {
                crossfade(true)
                placeholder(R.mipmap.ic_launcher)
                transformations(RoundedCornersTransformation(16f))
            }
        }

        // Setup Dependencies RecyclerView
        depAdapter = DependenciesAdapter(emptyList()) { dep ->
            downloadManualDependency(dep)
        }
        binding.rvDependencies.layoutManager = LinearLayoutManager(this)
        binding.rvDependencies.adapter = depAdapter

        updateInstallButtonState()
    }

    private fun updateInstallButtonState() {
        val mod = activeMod ?: return

        // Verify with tracker again to be 100% accurate
        val installed = app.modsTracker.isModInstalled(mod)

        if (installed) {
            binding.btnMainAction.text = "UNINSTALL MOD (ALREADY INSTALLED)"
            binding.btnMainAction.setBackgroundColor(Color.parseColor("#7F1D1D"))
            binding.btnMainAction.setTextColor(Color.parseColor("#FCA5A5"))
            binding.btnMainAction.setOnClickListener {
                uninstallMod()
            }
        } else {
            binding.btnMainAction.text = "INSTALL MOD"
            binding.btnMainAction.setBackgroundColor(Color.parseColor("#DC2626"))
            binding.btnMainAction.setTextColor(Color.WHITE)
            binding.btnMainAction.setOnClickListener {
                installMod()
            }
        }
    }

    private fun loadDependencies() {
        val mod = activeMod ?: return
        binding.progressDeps.visibility = View.VISIBLE
        binding.tvNoDeps.visibility = View.GONE

        lifecycleScope.launch {
            val modFile = xmodsManager.getModDetails(mod, app.profileManager.currentVersion, app.profileManager.currentLoader)
            activeFile = modFile
            binding.progressDeps.visibility = View.GONE

            if (modFile == null || modFile.dependencies.isEmpty()) {
                binding.tvNoDeps.visibility = View.VISIBLE
                depAdapter.updateList(emptyList())
            } else {
                binding.tvNoDeps.visibility = View.GONE
                depAdapter.updateList(modFile.dependencies)
            }
        }
    }

    private fun downloadManualDependency(dep: ModDependency) {
        Toast.makeText(this, "Searching dependency ${dep.name}...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            // Find and install dependency from store
            val search = xmodsManager.searchAllMods(dep.name, app.profileManager.currentVersion, app.profileManager.currentLoader)
            val match = search.firstOrNull()
            if (match != null) {
                val matchFile = xmodsManager.getModDetails(match, app.profileManager.currentVersion, app.profileManager.currentLoader)
                if (matchFile != null) {
                    val success = xmodsManager.downloadModFile(matchFile.downloadUrl, matchFile.fileName)
                    if (success) {
                        Toast.makeText(this@ModDetailActivity, "Downloaded dependency: ${match.title}", Toast.LENGTH_SHORT).show()
                        dep.isInstalled = true
                        depAdapter.notifyDataSetChanged()
                        return@launch
                    }
                }
            }
            Toast.makeText(this@ModDetailActivity, "Could not auto-download ${dep.name}. Please search manually.", Toast.LENGTH_LONG).show()
        }
    }

    private fun installMod() {
        val mod = activeMod ?: return
        val file = activeFile ?: run {
            Toast.makeText(this, "Fetching download links...", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnMainAction.isEnabled = false
        binding.btnMainAction.text = "DOWNLOADING..."

        lifecycleScope.launch {
            val success = xmodsManager.downloadModFile(file.downloadUrl, file.fileName)
            binding.btnMainAction.isEnabled = true
            if (success) {
                Toast.makeText(this@ModDetailActivity, "Installed ${mod.title} successfully!", Toast.LENGTH_SHORT).show()
                mod.isInstalled = true
                mod.installedFileName = file.fileName
                updateInstallButtonState()
            } else {
                Toast.makeText(this@ModDetailActivity, "Download failed for ${mod.title}", Toast.LENGTH_SHORT).show()
                updateInstallButtonState()
            }
        }
    }

    private fun uninstallMod() {
        val mod = activeMod ?: return
        val fileName = mod.installedFileName ?: "${mod.slug}.jar"
        val removed = app.modsTracker.uninstallMod(fileName)

        if (removed) {
            Toast.makeText(this, "Uninstalled ${mod.title} from mods folder.", Toast.LENGTH_SHORT).show()
            mod.isInstalled = false
            mod.installedFileName = null
            updateInstallButtonState()
        } else {
            Toast.makeText(this, "Could not find file to remove.", Toast.LENGTH_SHORT).show()
        }
    }
}
