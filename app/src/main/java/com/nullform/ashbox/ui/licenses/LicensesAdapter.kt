package com.nullform.ashbox.ui.licenses

import LicenseInfo
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nullform.ashbox.databinding.ItemLicenseBinding

/**
 * An adapter for displaying a list of open-source licenses in a RecyclerView.
 *
 * @param onItemClicked A lambda function to be invoked when a license item is clicked.
 *                      It provides the LicenseInfo for the clicked item.
 */
class LicensesAdapter(
    private val onItemClicked: (LicenseInfo) -> Unit
) : ListAdapter<LicenseInfo, LicensesAdapter.LicenseViewHolder>(LicenseDiffCallback()) {

    /**
     * ViewHolder for a single license item.
     */
    inner class LicenseViewHolder(private val binding: ItemLicenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // Set the click listener on the root view of the item
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(getItem(position))
                }
            }
        }

        fun bind(licenseInfo: LicenseInfo) {
            binding.libraryNameTextView.text = licenseInfo.libraryName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LicenseViewHolder {
        val binding = ItemLicenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LicenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LicenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * DiffUtil.ItemCallback for calculating the difference between two non-null items in a list.
     * This allows the ListAdapter to determine which items have been added, removed, or changed,
     * leading to efficient updates.
     */
    private class LicenseDiffCallback : DiffUtil.ItemCallback<LicenseInfo>() {
        override fun areItemsTheSame(oldItem: LicenseInfo, newItem: LicenseInfo): Boolean {
            // Library names are unique enough to be used as identifiers
            return oldItem.libraryName == newItem.libraryName
        }

        override fun areContentsTheSame(oldItem: LicenseInfo, newItem: LicenseInfo): Boolean {
            // The LicenseInfo is a data class, so '==' performs a structural equality check.
            return oldItem == newItem
        }
    }
}