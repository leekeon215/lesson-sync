package com.lessonsync.app.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * 자동 생성된 개별 주석을 저장하는 테이블
 * ScoreEntity와 1:N 관계를 가집니다.
 */
@Entity(
    tableName = "annotations",
    foreignKeys = [ForeignKey(
        entity = ScoreEntity::class,
        parentColumns = ["id"],
        childColumns = ["scoreOwnerId"],
        onDelete = ForeignKey.CASCADE // 원본 악보가 삭제되면 주석도 함께 삭제
    )]
)
data class AnnotationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val scoreOwnerId: Int, // 외래 키
    val measureNumber: Int,
    val directive: String
)