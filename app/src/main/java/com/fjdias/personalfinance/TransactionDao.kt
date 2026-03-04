package com.fjdias.personalfinance

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Transaction>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Update
    suspend fun update(transaction: Transaction)

    @Query("UPDATE transactions SET categoryName = :newName WHERE categoryName = :oldName")
    suspend fun updateCategoryName(oldName: String, newName: String)
}
