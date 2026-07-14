package com.example.businesscardholder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One saved business card record.
 * imagePath points to a JPEG stored in the app's private files directory
 * (card_images/) so everything stays fully local to the device.
 */
@Entity(tableName = "business_cards")
data class BusinessCard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactName: String = "",
    val companyName: String = "",
    val jobTitle: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val website: String = "",
    val address: String = "",
    val notes: String = "",
    val rawOcrText: String = "",
    val imagePath: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
