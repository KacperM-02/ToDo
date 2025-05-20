package com.example.todo.fragments.addTask

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.R

class AttachmentAdapter (
    private val attachmentsList: MutableList<String>,
    private val onDeleteClickListener: (String) -> Unit
) : RecyclerView.Adapter<AttachmentAdapter.AttachmentViewHolder>() {
    inner class AttachmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val attachmentImage: ImageView = itemView.findViewById(R.id.attachmentImage)
        private val deleteImageButton: ImageView = itemView.findViewById(R.id.deleteAttachmentImage)

        fun bind(attachmentPath: String) {
            attachmentImage.setImageURI(attachmentPath.toUri())
            deleteImageButton.setOnClickListener {
                onDeleteClickListener(attachmentPath)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.attachment_item, parent, false)
        return AttachmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        holder.bind(attachmentsList[position])
    }

    override fun getItemCount() = attachmentsList.size

    fun removeAttachment(attachmentPath: String) {
        val position = attachmentsList.indexOf(attachmentPath)
        if (position != -1) {
            attachmentsList.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}