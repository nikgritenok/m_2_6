package com.example.lab6.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lab6.databinding.ItemImageBinding
import com.example.lab6.model.ImageItem

class ImageAdapter(
    private val images: MutableList<ImageItem> = mutableListOf(),
    private val onItemClick: (ImageItem) -> Unit = {},
    private val onItemLongClick: (ImageItem, Int) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(private val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imageItem: ImageItem) {

            Glide.with(binding.root.context)
                .load(imageItem.uri)
                .centerCrop()
                .into(binding.imageViewItem)


            binding.textViewDescription.text = imageItem.description ?: "Нет описания"


            binding.root.setOnClickListener {
                onItemClick(imageItem)
            }


            binding.root.setOnLongClickListener {
                onItemLongClick(imageItem, adapterPosition)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount(): Int = images.size

    fun updateImages(newImages: List<ImageItem>) {
        images.clear()
        images.addAll(newImages)
        notifyDataSetChanged()
    }

    fun addImage(image: ImageItem) {
        images.add(image)
        notifyItemInserted(images.size - 1)
    }

    fun updateImageDescription(position: Int, description: String) {
        if (position in images.indices) {
            images[position].description = description
            notifyItemChanged(position)
        }
    }
} 