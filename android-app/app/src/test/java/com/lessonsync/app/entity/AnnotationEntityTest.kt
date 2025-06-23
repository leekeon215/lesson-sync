package com.lessonsync.app.entity

import org.junit.Assert.*
import org.junit.Test

class AnnotationEntityTest {

    @Test
    fun `AnnotationEntity 객체가 올바른 값으로 생성되어야 한다`() {
        // 준비 (Arrange)
        val scoreId = 10
        val measure = 4
        val directive = "crescendo"

        // 실행 (Act)
        val annotation = AnnotationEntity(
            scoreOwnerId = scoreId,
            measureNumber = measure,
            directive = directive
        )

        // 검증 (Assert)
        assertEquals(0, annotation.id) // 기본값 확인
        assertEquals(scoreId, annotation.scoreOwnerId)
        assertEquals(measure, annotation.measureNumber)
        assertEquals(directive, annotation.directive)
    }

    @Test
    fun `모든 데이터가 동일한 두 인스턴스는 동등해야 한다`() {
        // 준비
        val annotation1 = AnnotationEntity(1, 10, 4, "crescendo")
        val annotation2 = AnnotationEntity(1, 10, 4, "crescendo")

        // 실행 & 검증
        assertEquals(annotation1, annotation2)
        assertEquals(annotation1.hashCode(), annotation2.hashCode())
    }

    @Test
    fun `scoreOwnerId가 다른 두 인스턴스는 동등하지 않아야 한다`() {
        // 준비
        val annotation1 = AnnotationEntity(1, 10, 4, "crescendo")
        val annotation2 = AnnotationEntity(1, 11, 4, "crescendo") // scoreOwnerId 다름

        // 실행 & 검증
        assertNotEquals(annotation1, annotation2)
    }

    @Test
    fun `measureNumber가 다른 두 인스턴스는 동등하지 않아야 한다`() {
        // 준비
        val annotation1 = AnnotationEntity(1, 10, 4, "crescendo")
        val annotation2 = AnnotationEntity(1, 10, 5, "crescendo") // measureNumber 다름

        // 실행 & 검증
        assertNotEquals(annotation1, annotation2)
    }

    @Test
    fun `directive가 다른 두 인스턴스는 동등하지 않아야 한다`() {
        // 준비
        val annotation1 = AnnotationEntity(1, 10, 4, "crescendo")
        val annotation2 = AnnotationEntity(1, 10, 4, "diminuendo") // directive 다름

        // 실행 & 검증
        assertNotEquals(annotation1, annotation2)
    }
}