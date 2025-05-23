package com.example.week09.week12.roomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ItemDatabase : RoomDatabase()  {
    abstract fun getItemDao(): ItemDAO

    companion object{
        @Volatile
        private var DBINSTANCE: ItemDatabase? = null

        fun getDBInstance(context: Context): ItemDatabase {
            return DBINSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ItemDatabase::class.java,
                    "ItemDB"
                ).build()
                DBINSTANCE = instance
                instance
            }
        }
    }
}
//abstract