package com.lessonsync.app.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * 하나의 레슨 분석 결과를 저장하는 테이블
 * ScoreEntity와 1:1 관계를 가집니다.
 */
@Entity(
    tableName = "lesson_results",
    foreignKeys = [ForeignKey(
        entity = ScoreEntity::class,
        parentColumns = ["id"],
        childColumns = ["scoreOwnerId"],
        onDelete = ForeignKey.CASCADE // 원본 악보가 삭제되면 이 결과도 함께 삭제
    )]
)
data class LessonResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val scoreOwnerId: Int, // 외래 키 (어떤 악보에 대한 결과인지)
    val summary: String,   // AI 요약 내용
    val fullTranscript: String, // 레슨 전체 전문
    val createdAt: Long = System.currentTimeMillis()
)