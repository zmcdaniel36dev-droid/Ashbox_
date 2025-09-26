package com.nullform.ashbox.ui.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nullform.ashbox.data.Model

class ModelsViewModel : ViewModel() {

    private val _models = MutableLiveData<List<Model>>()
    val models: LiveData<List<Model>> = _models

    init {
        loadModels()
    }

    private fun loadModels() {
        // This is where you'd fetch models from a server in the future.
        // For now, we use the hardcoded list from our sealed class.
        val modelList = listOf(
            Model.Gemma2B,
            Model.Mistral7B,
            Model.TinyLlama
        )
        _models.value = modelList
    }
}
