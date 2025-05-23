// ============================== app/src/main/java/com/lessonsync/app/navigation/Score.kt
package com.lessonsync.app.navigation

data class Score(val id: String, val title: String)

val demoScores = listOf(
    Score("1", "Bach Minuet 1"),
    Score("2", "Vivaldi A minor 1st mov."),
    Score("3", "Kreutzer Etude 2")
)
