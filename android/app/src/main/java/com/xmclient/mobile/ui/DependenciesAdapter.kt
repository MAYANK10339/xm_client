package com.xmclient.mobile.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.xmclient.mobile.databinding.ItemDependencyBinding
import com.xmclient.mobile.xmods.model.ModDependency

class DependenciesAdapter(
    private var dependencies: List<ModDependency>,
    private val onDownloadClick: (ModDependency) -> Unit
) : RecyclerView.Adapter<DependenciesAdapter.DepViewHolder>() {

    fun updateList(newList: List<ModDependency>) {
        dependencies = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepViewHolder {
        val binding = ItemDependencyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DepViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DepViewHolder, position: Int) {
        holder.bind(dependencies[position])
    }

    override fun getItemCount(): Int = dependencies.size

    inner class DepViewHolder(private val binding: ItemDependencyBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dep: ModDependency) {
            binding.tvDepName.text = dep.name

            if (dep.isInstalled) {
                binding.tvDepStatus.text = "Status: Installed in mods folder ✓"
                binding.tvDepStatus.setTextColor(Color.parseColor("#10B981"))

                binding.btnDepDownload.text = "INSTALLED"
                binding.btnDepDownload.setBackgroundColor(Color.parseColor("#064E3B"))
                binding.btnDepDownload.setTextColor(Color.parseColor("#10B981"))
                binding.btnDepDownload.isEnabled = false
            } else {
                binding.tvDepStatus.text = if (dep.isRequired) "Status: Missing (Required for this mod)" else "Status: Optional"
                binding.tvDepStatus.setTextColor(if (dep.isRequired) Color.parseColor("#EF4444") else Color.parseColor("#A1A1AA"))

                binding.btnDepDownload.text = "DOWNLOAD"
                binding.btnDepDownload.setBackgroundColor(Color.parseColor("#DC2626"))
                binding.btnDepDownload.setTextColor(Color.WHITE)
                binding.btnDepDownload.isEnabled = true
            }

            binding.btnDepDownload.setOnClickListener {
                if (!dep.isInstalled) {
                    onDownloadClick(dep)
                }
            }
        }
    }
}
