package com.example.vokabeltrainer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Word::class, LearningState::class, LearningUnit::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun learningDao(): LearningStateDao
    abstract fun unitDao(): UnitDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "vokabeln.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
