package com.example.week09.week12.roomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Itemtable")
data class ItemEntity(
    val itemName: String,
    val itemQuantity: Int,
    @PrimaryKey(autoGenerate = true)
    val itemID: Int = 0
)
