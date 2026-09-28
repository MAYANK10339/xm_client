package com.xmclient.mobile.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.xmclient.mobile.R
import com.xmclient.mobile.databinding.ItemModCardBinding
import com.xmclient.mobile.xmods.model.UnifiedMod

class XModsAdapter(
    private var mods: List<UnifiedMod>,
    private val onModClick: (UnifiedMod) -> Unit,
    private val onInstallClick: (UnifiedMod) -> Unit
) : RecyclerView.Adapter<XModsAdapter.ModViewHolder>() {

    fun updateList(newList: List<UnifiedMod>) {
        mods = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModViewHolder {
        val binding = ItemModCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ModViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ModViewHolder, position: Int) {
        holder.bind(mods[position])
    }

    override fun getItemCount(): Int = mods.size

    inner class ModViewHolder(private val binding: ItemModCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(mod: UnifiedMod) {
            binding.tvModTitle.text = mod.title
            binding.tvModAuthor.text = "by ${mod.author}"
            binding.tvModDesc.text = mod.description
            binding.tvBadgeSource.text = mod.source

            // Badge color based on source
            if (mod.source == "Modrinth") {
                binding.tvBadgeSource.setTextColor(Color.parseColor("#1BD96A"))
                binding.tvBadgeSource.setBackgroundColor(Color.parseColor("#052E16"))
            } else {
                binding.tvBadgeSource.setTextColor(Color.parseColor("#F16436"))
                binding.tvBadgeSource.setBackgroundColor(Color.parseColor("#431407"))
            }

            // Load icon
            if (!mod.iconUrl.isNullOrEmpty()) {
                binding.ivModIcon.load(mod.iconUrl) {
                    crossfade(true)
                    placeholder(R.mipmap.ic_launcher)
                    transformations(RoundedCornersTransformation(16f))
                }
            } else {
                binding.ivModIcon.setImageResource(R.mipmap.ic_launcher)
            }

            // CRITICAL DUPLICATE PREVENTION:
            // If already installed, show "INSTALLED" and disable re-installation!
            if (mod.isInstalled) {
                binding.btnAction.text = "INSTALLED"
                binding.btnAction.setBackgroundColor(Color.parseColor("#064E3B"))
                binding.btnAction.setTextColor(Color.parseColor("#10B981"))
                binding.btnAction.isEnabled = false
            } else {
                binding.btnAction.text = "INSTALL"
                binding.btnAction.setBackgroundColor(Color.parseColor("#DC2626"))
                binding.btnAction.setTextColor(Color.WHITE)
                binding.btnAction.isEnabled = true
            }

            binding.btnAction.setOnClickListener {
                if (!mod.isInstalled) {
                    onInstallClick(mod)
                }
            }

            binding.root.setOnClickListener {
                onModClick(mod)
            }
        }
    }
}
