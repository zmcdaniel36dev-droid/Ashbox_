package com.nullform.ashbox.ui.chat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nullform.ashbox.databinding.FragmentChatBinding // Make sure this is your actual binding class
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var messagesAdapter: ChatMessageAdapter // You'll need an adapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        binding.chatViewModel = chatViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupInput()
        observeUiState()
    }

    override fun onResume() {
        super.onResume()
        // Set the subtitle when the fragment is resumed
        (activity as? AppCompatActivity)?.supportActionBar?.subtitle = "tinyllama"
    }

    private fun setupRecyclerView() {
        messagesAdapter = ChatMessageAdapter()
        binding.recyclerViewMessages.apply {
            adapter = messagesAdapter
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
        }
    }

    private fun setupInput() {
        binding.editTextMessageInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                chatViewModel.onUserMessageChanged(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.buttonSendMessage.setOnClickListener {
            try{
                chatViewModel.sendUserMessage()
            }catch (e: Exception) {
                Log.e("sendUserMessage", e.message.toString())
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            chatViewModel.uiState.collectLatest { state ->
                messagesAdapter.submitList(state.messages)

                if (binding.editTextMessageInput.text.toString() != state.currentInputText) {
                    binding.editTextMessageInput.setText(state.currentInputText)
                }

                binding.progressBarMessages.visibility = if (state.isLoadingMessages) View.VISIBLE else View.GONE
                binding.buttonSendMessage.isEnabled = !state.isSendingMessage
                binding.progressBarSending.visibility = if (state.isSendingMessage) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Clear the subtitle when the fragment is paused
        (activity as? AppCompatActivity)?.supportActionBar?.subtitle = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
