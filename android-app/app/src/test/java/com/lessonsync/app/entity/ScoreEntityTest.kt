package com.lessonsync.app.entity

import org.junit.Assert.*
import org.junit.Test
import kotlin.system.measureTimeMillis

class ScoreEntityTest {

    @Test
    fun `ScoreEntity 생성 시 createdAt은 현재 시간으로 자동 설정되어야 한다`() {
        // 준비 & 실행
        val currentTime = System.currentTimeMillis()
        val score = ScoreEntity(title = "Test Title", filePath = "/path")

        // 검증
        // 객체 생성 시간이 매우 짧으므로, 생성 전후 시간 사이에 있는지 확인
        assertTrue(score.createdAt >= currentTime)
        assertTrue(score.createdAt <= System.currentTimeMillis())
    }

    @Test
    fun `동일한 데이터를 가진 두 ScoreEntity 인스턴스는 동일해야 한다`() {
        // 준비
        val score1 = ScoreEntity(id = 1, title = "Title", filePath = "/path", createdAt = 123L)
        val score2 = ScoreEntity(id = 1, title = "Title", filePath = "/path", createdAt = 123L)

        // 실행 & 검증
        assertEquals(score1, score2)
    }

    @Test
    fun `다른 데이터를 가진 두 ScoreEntity 인스턴스는 동일하지 않아야 한다`() {
        // 준비
        val score1 = ScoreEntity(id = 1, title = "Title 1", filePath = "/path", createdAt = 123L)
        val score2 = ScoreEntity(id = 2, title = "Title 2", filePath = "/path", createdAt = 456L)

        // 실행 & 검증
        assertNotEquals(score1, score2)
    }
}