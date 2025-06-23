package com.lessonsync.app.entity

import org.junit.Assert.*
import org.junit.Test

class LessonResultEntityTest {

    @Test
    fun `LessonResultEntity 객체가 올바른 값으로 생성되어야 한다`() {
        // 준비 (Arrange)
        val scoreId = 1
        val summary = "AI 요약 내용입니다."
        val transcript = "레슨 전체 전문입니다."
        val time = System.currentTimeMillis()

        // 실행 (Act)
        val lessonResult = LessonResultEntity(
            scoreOwnerId = scoreId,
            summary = summary,
            fullTranscript = transcript,
            createdAt = time
        )

        // 검증 (Assert)
        assertEquals(0, lessonResult.id) // 기본값 확인
        assertEquals(scoreId, lessonResult.scoreOwnerId)
        assertEquals(summary, lessonResult.summary)
        assertEquals(transcript, lessonResult.fullTranscript)
        assertEquals(time, lessonResult.createdAt)
    }

    @Test
    fun `createdAt 프로퍼티는 기본값으로 현재 시간이 설정되어야 한다`() {
        // 준비 & 실행
        val before = System.currentTimeMillis()
        val lessonResult = LessonResultEntity(
            scoreOwnerId = 1,
            summary = "summary",
            fullTranscript = "transcript"
        )
        val after = System.currentTimeMillis()

        // 검증
        // 객체 생성 시점의 전후 시간 사이에 createdAt 값이 있는지 확인
        assertTrue("createdAt should be greater than or equal to the time before creation", lessonResult.createdAt >= before)
        assertTrue("createdAt should be less than or equal to the time after creation", lessonResult.createdAt <= after)
    }

    @Test
    fun `모든 데이터가 동일한 두 인스턴스는 동등해야 한다`() {
        // 준비
        val time = System.currentTimeMillis()
        val result1 = LessonResultEntity(1, 1, "summary", "transcript", time)
        val result2 = LessonResultEntity(1, 1, "summary", "transcript", time)

        // 실행 & 검증
        assertEquals(result1, result2)
        assertEquals(result1.hashCode(), result2.hashCode())
    }

    @Test
    fun `데이터가 다른 두 인스턴스는 동등하지 않아야 한다`() {
        // 준비
        val time = System.currentTimeMillis()
        val result1 = LessonResultEntity(1, 1, "summary", "transcript", time)
        val result2 = LessonResultEntity(2, 2, "different summary", "different transcript", time + 1)

        // 실행 & 검증
        assertNotEquals(result1, result2)
    }
}