package com.nullform.ashbox.ui.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ModelsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is models Fragment"
    }
    val text: LiveData<String> = _text
}