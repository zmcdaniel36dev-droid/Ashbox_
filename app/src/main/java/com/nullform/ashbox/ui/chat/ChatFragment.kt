package com.nullform.ashbox.ui.chat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.nullform.ashbox.data.entity.ChatMessage
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
        // Initialize ViewModel (ensure you have a factory if ChatViewModel has dependencies)
        // If ChatViewModel has no constructor args, this is fine:
        // Otherwise, use:
        // val chatRepository = ... // Get your repository
        // chatViewModel = ViewModelProvider(this, ChatViewModelFactory(chatRepository)).get(ChatViewModel::class.java)

        _binding = FragmentChatBinding.inflate(inflater, container, false)
        binding.chatViewModel = chatViewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupInput()
        observeUiState() //TODO: Finish implementing and call in onViewCreated()

        // Example: If you need to load a specific chat session when the fragment starts
        // val args = arguments?.let { ChatFragmentArgs.fromBundle(it) }
        // val sessionIdToLoad = args?.sessionId
        // if (sessionIdToLoad != null) {
        //     chatViewModel.loadChatSession(sessionIdToLoad)
        // } else {
        //     // Let ViewModel's init block handle the default empty state
        // }
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
        // Assuming 'editTextMessageInput' and 'buttonSendMessage' are IDs in fragment_chat.xml
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
                // Update messages list
                messagesAdapter.submitList(state.messages) {
                    // Scroll to the bottom after list is updated if new messages were added
                    if (state.messages.isNotEmpty()) {
                        binding.recyclerViewMessages.smoothScrollToPosition(state.messages.size - 1)
                    }
                }

                // Update input field (usually only needed if ViewModel clears it)
                if (binding.editTextMessageInput.text.toString() != state.currentInputText) {
                    binding.editTextMessageInput.setText(state.currentInputText)
                    // binding.editTextMessageInput.setSelection(state.currentInputText.length) // Move cursor to end
                }

                // Handle loading state for messages (e.g., show a ProgressBar)
                binding.progressBarMessages.visibility = if (state.isLoadingMessages) View.VISIBLE else View.GONE
                // Handle sending state (e.g., disable send button or show a different indicator)
                binding.buttonSendMessage.isEnabled = !state.isSendingMessage
                binding.progressBarSending.visibility = if (state.isSendingMessage) View.VISIBLE else View.GONE

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Important to prevent memory leaks
    }
}

