package com.fjdias.personalfinance

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()
    private val categoryDao = db.categoryDao()
    private val csvReader = CsvReader()

    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    fun importCsv(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val transactions = csvReader.readTransactions(inputStream)
                    transactionDao.insertAll(transactions)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.deleteAll()
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryDao.insert(Category(name = name))
        }
    }

    fun updateCategory(oldName: String, newCategory: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            // Primeiro atualiza o nome da categoria na tabela de categorias
            categoryDao.update(newCategory)
            // Depois atualiza todas as transações que usavam o nome antigo
            transactionDao.updateCategoryName(oldName, newCategory.name)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryDao.delete(category)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.update(transaction)
        }
    }

    fun addManualTransaction(title: String, amount: Double, categoryName: String, date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.insert(
                Transaction(
                    title = title,
                    amount = amount,
                    categoryName = categoryName,
                    date = date
                )
            )
        }
    }
}
