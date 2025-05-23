package com.example.week09.week12.roomDB

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

interface ItemDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun InsertItem(itemEntity: ItemEntity)

    @Update
    suspend fun UpdateItem(itemEntity: ItemEntity)

    @Delete
    suspend fun DeleteItem(itemEntity: ItemEntity)

    @Query("SELECT * FROM Itemtable where itemName = :itemName")
    fun GetItems(itemName : String): Flow<List<ItemEntity>>

}