package com.example.mulahmanage.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Transaction::class, QuickExpense::class, Budget::class], // Add Budget::class
    version = 3, // IMPORTANT: Increase the version number
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun quickExpenseDao(): QuickExpenseDao
    abstract fun budgetDao(): BudgetDao // Add this abstract function

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 2 to 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `budgets` (`category` TEXT NOT NULL, `budgetAmount` REAL NOT NULL, PRIMARY KEY(`category`))")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mulah_manage_database"
                )
                    .addMigrations(MIGRATION_2_3) // Add the new migration
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

