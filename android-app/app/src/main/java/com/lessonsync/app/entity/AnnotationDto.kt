package com.lessonsync.app.entity

import com.google.gson.annotations.SerializedName

/**
 * 서버의 '/scores/parse-directives' API에 텍스트를 보낼 때 사용하는 요청 모델
 */
data class AnnotationRequest(
    val text: String
)



/**
 * 서버로부터 받은 개별 주석 정보를 담는 모델
 */
data class AnnotationInfo(
    @SerializedName("measure")
    val measure: Int,

    @SerializedName("directive")
    val directive: String
)

/**
 * '/scores/parse-directives' API의 최종 응답을 파싱하기 위한 모델
 */
data class AnnotationResponse(
    @SerializedName("annotations")
    val annotations: List<AnnotationInfo>
)