package com.example.businesscardholder.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.businesscardholder.data.AppDatabase
import com.example.businesscardholder.data.BusinessCard
import com.example.businesscardholder.databinding.ActivityCardDetailBinding
import kotlinx.coroutines.launch
import java.io.File

class CardDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCardDetailBinding
    private val dao by lazy { AppDatabase.getInstance(this).businessCardDao() }
    private var currentCard: BusinessCard? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == com.example.businesscardholder.R.id.action_delete) {
                confirmDelete()
                true
            } else false
        }

        val cardId = intent.getLongExtra(EXTRA_CARD_ID, -1L)
        if (cardId == -1L) {
            finish()
            return
        }

        lifecycleScope.launch {
            val card = dao.getById(cardId)
            if (card == null) {
                finish()
                return@launch
            }
            currentCard = card
            bindCard(card)
        }

        binding.btnSaveChanges.setOnClickListener { saveChanges() }
        binding.btnCall.setOnClickListener { callContact() }
    }

    private fun bindCard(card: BusinessCard) {
        binding.etName.setText(card.contactName)
        binding.etCompany.setText(card.companyName)
        binding.etJobTitle.setText(card.jobTitle)
        binding.etPhone.setText(card.phoneNumber)
        binding.etEmail.setText(card.email)
        binding.etWebsite.setText(card.website)
        binding.etAddress.setText(card.address)
        binding.etNotes.setText(card.notes)

        if (card.imagePath.isNotBlank() && File(card.imagePath).exists()) {
            binding.ivPhoto.setImageBitmap(BitmapFactory.decodeFile(card.imagePath))
        } else {
            binding.ivPhoto.setImageResource(com.example.businesscardholder.R.drawable.ic_card_placeholder)
        }
    }

    private fun saveChanges() {
        val existing = currentCard ?: return
        val updated = existing.copy(
            contactName = binding.etName.text.toString().trim(),
            companyName = binding.etCompany.text.toString().trim(),
            jobTitle = binding.etJobTitle.text.toString().trim(),
            phoneNumber = binding.etPhone.text.toString().trim(),
            email = binding.etEmail.text.toString().trim(),
            website = binding.etWebsite.text.toString().trim(),
            address = binding.etAddress.text.toString().trim(),
            notes = binding.etNotes.text.toString().trim()
        )
        lifecycleScope.launch {
            dao.update(updated)
            currentCard = updated
            Toast.makeText(this@CardDetailActivity, "Saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun callContact() {
        val phone = binding.etPhone.text.toString().trim()
        if (phone.isBlank()) {
            Toast.makeText(this, "No phone number saved for this card", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    }

    private fun confirmDelete() {
        val card = currentCard ?: return
        AlertDialog.Builder(this)
            .setTitle("Delete card?")
            .setMessage("This will permanently remove this business card from your phone.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    dao.delete(card)
                    if (card.imagePath.isNotBlank()) {
                        File(card.imagePath).delete()
                    }
                    finish()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    companion object {
        const val EXTRA_CARD_ID = "extra_card_id"
    }
}
