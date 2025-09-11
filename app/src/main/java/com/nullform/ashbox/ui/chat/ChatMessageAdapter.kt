package com.nullform.ashbox.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nullform.ashbox.data.entity.ChatMessage // Assuming your ChatMessage data class
import com.nullform.ashbox.data.entity.SenderType
import com.nullform.ashbox.databinding.ItemChatMessageAiBinding
import com.nullform.ashbox.databinding.ItemChatMessageUserBinding

class ChatMessageAdapter :
    ListAdapter<ChatMessage, RecyclerView.ViewHolder>(ChatMessageDiffCallback) {

    private val VIEW_TYPE_USER_MESSAGE = 1
    private val VIEW_TYPE_AI_MESSAGE = 2

    class UserMessageViewHolder(val binding: ItemChatMessageUserBinding) : RecyclerView.ViewHolder(binding.root)

    // ViewHolder for AI messages
    class AiMessageViewHolder(val binding: ItemChatMessageAiBinding) : RecyclerView.ViewHolder(binding.root)


 /*   open class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.textViewMessageContent) // Example ID

        fun bind(chatMessage: ChatMessage) {
            messageTextView.text = chatMessage.content
            // Format and set timestamp
            //timestampTextView.text = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date(chatMessage.timestamp))
        }
    }*/

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).sender == SenderType.USER) {
            VIEW_TYPE_USER_MESSAGE
        } else {
            VIEW_TYPE_AI_MESSAGE
        }
    }

    // --- RecyclerView.Adapter Methods ---
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_TYPE_USER_MESSAGE -> {
                val binding = ItemChatMessageUserBinding.inflate(layoutInflater, parent, false)
                UserMessageViewHolder(binding)
            }
            VIEW_TYPE_AI_MESSAGE -> {
                val binding = ItemChatMessageAiBinding.inflate(layoutInflater, parent, false)
                AiMessageViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid argument provided for viewType")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val message = getItem(position)

        when(holder) {
            is UserMessageViewHolder -> {
                holder.binding.textViewMessageContent.text = message.content
            }
            is AiMessageViewHolder -> {
                holder.binding.textViewMessageContent.text = message.content
            }
        }
    }
}

object ChatMessageDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
    override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
        return oldItem == newItem
    }
}
