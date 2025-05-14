package com.example.todo.fragments.addTask

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.R
import com.example.todo.data.tasks.Attachment

class AttachmentAdapter (
    private val attachmentList: MutableList<Attachment>,
    private val onDeleteClickListener: (Attachment) -> Unit
) : RecyclerView.Adapter<AttachmentAdapter.AttachmentViewHolder>() {
    inner class AttachmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val attachmentImage: ImageView = itemView.findViewById(R.id.attachmentImage)
        private val deleteImageButton: ImageView = itemView.findViewById(R.id.deleteAttachmentImage)

        fun bind(attachment: Attachment) {
            attachmentImage.setImageURI(attachment.attachmentPath.toUri())
            deleteImageButton.setOnClickListener {
                onDeleteClickListener(attachment)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.attachment_item, parent, false)
        return AttachmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        holder.bind(attachmentList[position])
    }

    override fun getItemCount() = attachmentList.size

    fun removeAttachment(attachment: Attachment) {
        val position = attachmentList.indexOf(attachment)
        if (position != -1) {
            attachmentList.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}