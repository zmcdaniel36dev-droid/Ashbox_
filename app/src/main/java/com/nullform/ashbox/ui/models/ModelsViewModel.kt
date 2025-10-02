package com.nullform.ashbox.ui.models

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nullform.ashbox.data.Model

class ModelsViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val APP_PREFS_FILE_NAME = "ashbox_app_preferences"
        private const val KEY_SELECTED_MODEL_NAME = "key_selected_model_name"
        private const val DEFAULT_MODEL_NAME = "tinyllama"
    }

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

    fun saveSelectedModel(model: Model) {
        val prefs = getApplication<Application>().getSharedPreferences(APP_PREFS_FILE_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putString(KEY_SELECTED_MODEL_NAME, model.name) // Assuming Model has a 'name' property
            apply()
        }
    }

    fun getSelectedModelName(): String {
        val prefs = getApplication<Application>().getSharedPreferences(APP_PREFS_FILE_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SELECTED_MODEL_NAME, KEY_SELECTED_MODEL_NAME) ?: DEFAULT_MODEL_NAME
    }
}
