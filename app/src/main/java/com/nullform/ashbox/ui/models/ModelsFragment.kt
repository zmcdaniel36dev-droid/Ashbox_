package com.nullform.ashbox.ui.models

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nullform.ashbox.databinding.FragmentModelsBinding
import com.nullform.ashbox.ui.chat.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ModelsFragment : Fragment() {

    private var _binding: FragmentModelsBinding? = null
    private val binding get() = _binding!!

    private val modelsViewModel: ModelsViewModel by viewModels()
    private val chatViewModel: ChatViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentModelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val modelsAdapter = ModelsAdapter { model ->
            chatViewModel.selectModel(model)
        }
        binding.recyclerViewModels.adapter = modelsAdapter

        // This observer submits the list of available models to the adapter
        modelsViewModel.models.observe(viewLifecycleOwner) { models ->
            modelsAdapter.submitList(models)
        }

        // This collector observes the shared UI state from ChatViewModel
        // and updates the UI whenever the selected model changes.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.uiState.collect { uiState ->
                    uiState.selectedModel?.let { selectedModel ->
                        // Update the hero card with the selected model's details
                        binding.modelNameTextView.text = selectedModel.name
                        "Version: ${selectedModel.version}".also { binding.modelVersionTextView.text = it }
                        binding.modelDescriptionTextView.text = selectedModel.description

                        // Update the adapter to highlight the correct item in the list
                        modelsAdapter.setSelectedModelId(selectedModel.id)

                        modelsViewModel.saveSelectedModel(selectedModel)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val APP_PREFS_FILE_NAME = "ashbox_app_preferences" // Name for your app's preference file
        private const val MODEL_NAME_KEY = "key_selected_model_name" // Key for the specific setting
    }
}
