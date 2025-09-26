package com.nullform.ashbox.ui.models

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nullform.ashbox.R // Make sure to import R for drawables
import com.nullform.ashbox.data.Model
import com.nullform.ashbox.databinding.ItemModelBinding

class ModelsAdapter (private val onModelClicked: (model: Model) -> Unit)
    : ListAdapter<Model, ModelsAdapter.ModelViewHolder>(ModelDiffCallback()) {

    private var _selectedModelId: String? = null

    fun setSelectedModelId(newSelectedModelId: String?) {
        if (_selectedModelId != newSelectedModelId) {
            // Find the position of the previously selected item and notify change
            val oldSelectedPosition = currentList.indexOfFirst { it.id == _selectedModelId }
            if (oldSelectedPosition != -1) {
                notifyItemChanged(oldSelectedPosition)
            }

            _selectedModelId = newSelectedModelId

            // Find the position of the newly selected item and notify change
            val newSelectedPosition = currentList.indexOfFirst { it.id == _selectedModelId }
            if (newSelectedPosition != -1) {
                notifyItemChanged(newSelectedPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        val binding = ItemModelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ModelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        val model = getItem(position)
        holder.bind(model, model.id == _selectedModelId) // Pass selection state to bind
    }

    inner class ModelViewHolder(private val binding: ItemModelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(model: Model, isSelected: Boolean) {
            binding.modelName.text = model.name
            binding.root.setOnClickListener {
                this@ModelsAdapter.onModelClicked(model)
            }
            // Set background based on selection state
            binding.root.setBackgroundResource(
                if (isSelected) R.drawable.model_item_background_selected
                else R.drawable.model_item_background
            )
        }
    }

    class ModelDiffCallback : DiffUtil.ItemCallback<Model>() {
        override fun areItemsTheSame(oldItem: Model, newItem: Model): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Model, newItem: Model): Boolean {
            return oldItem == newItem
        }
    }
}
