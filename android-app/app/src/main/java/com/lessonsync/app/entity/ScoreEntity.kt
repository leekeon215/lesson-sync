package com.lessonsync.app.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val filePath: String, // MusicXML 파일의 실제 경로
    val createdAt: Long = System.currentTimeMillis()
)