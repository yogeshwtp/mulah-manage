package com.example.mulahmanage.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Transaction::class, QuickExpense::class], version = 2) // Added QuickExpense and bumped version
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun quickExpenseDao(): QuickExpenseDao // Added DAO for quick expenses

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mulah_manage_database"
                )
                    .fallbackToDestructiveMigration() // Handles version change simply
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
