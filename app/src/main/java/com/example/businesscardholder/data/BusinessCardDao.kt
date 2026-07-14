package com.example.businesscardholder.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessCardDao {

    @Query("SELECT * FROM business_cards ORDER BY createdAt DESC")
    fun getAll(): Flow<List<BusinessCard>>

    @Query(
        """
        SELECT * FROM business_cards
        WHERE contactName LIKE '%' || :query || '%'
           OR companyName LIKE '%' || :query || '%'
           OR phoneNumber LIKE '%' || :query || '%'
           OR email LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
        """
    )
    fun search(query: String): Flow<List<BusinessCard>>

    @Query("SELECT * FROM business_cards WHERE id = :id")
    suspend fun getById(id: Long): BusinessCard?

    @Insert
    suspend fun insert(card: BusinessCard): Long

    @Update
    suspend fun update(card: BusinessCard)

    @Delete
    suspend fun delete(card: BusinessCard)
}
