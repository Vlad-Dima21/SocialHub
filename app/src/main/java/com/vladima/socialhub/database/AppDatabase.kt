package com.vladima.socialhub.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Post::class], version = 2)
@TypeConverters(DbConverters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun postDao(): PostDao

    companion object {
        const val DATABASE_NAME = "socialhub_db"

        @Volatile
        private var instance: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                """
                    ALTER TABLE `post` ADD COLUMN `addedAt` INTEGER NOT NULL DEFAULT 0
                """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            if (instance != null) {
                return instance as AppDatabase
            }
            synchronized(this) {
                return Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it}
            }
        }
    }
}