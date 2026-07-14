package com.example.businesscardholder.ui

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.businesscardholder.data.BusinessCard
import com.example.businesscardholder.databinding.ItemCardBinding
import java.io.File

class CardAdapter(
    private val onClick: (BusinessCard) -> Unit
) : ListAdapter<BusinessCard, CardAdapter.CardViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CardViewHolder(private val binding: ItemCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(card: BusinessCard) {
            binding.tvName.text = card.contactName.ifBlank { "(No name)" }
            binding.tvCompany.text = card.companyName
            binding.tvCompany.visibility = if (card.companyName.isBlank()) android.view.View.GONE else android.view.View.VISIBLE
            binding.tvPhone.text = card.phoneNumber
            binding.tvPhone.visibility = if (card.phoneNumber.isBlank()) android.view.View.GONE else android.view.View.VISIBLE

            if (card.imagePath.isNotBlank() && File(card.imagePath).exists()) {
                val bmp = BitmapFactory.decodeFile(card.imagePath)
                if (bmp != null) binding.ivThumb.setImageBitmap(bmp)
            } else {
                binding.ivThumb.setImageResource(com.example.businesscardholder.R.drawable.ic_card_placeholder)
            }

            binding.root.setOnClickListener { onClick(card) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BusinessCard>() {
            override fun areItemsTheSame(oldItem: BusinessCard, newItem: BusinessCard) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: BusinessCard, newItem: BusinessCard) =
                oldItem == newItem
        }
    }
}
