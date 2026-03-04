package com.fjdias.personalfinance

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Transaction::class, Category::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        Thread {
                            val cursor = db.query("SELECT COUNT(*) FROM categories WHERE name = 'Renda'")
                            cursor.moveToFirst()
                            val count = cursor.getInt(0)
                            cursor.close()
                            
                            if (count == 0) {
                                db.execSQL("INSERT INTO categories (name) VALUES ('Renda')")
                            }
                        }.start()
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
