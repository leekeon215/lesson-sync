package com.lessonsync.app.navigation

// Route.kt
sealed class Route(val path: String) {
    object Upload     : Route("upload")
    object Record     : Route("record")
    object Processing : Route("processing")
    object Viewer     : Route("viewer")
    object Summary    : Route("summary")
    object Settings   : Route("settings")
}
