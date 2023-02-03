package com.ghosts.of.history.persistentcloudanchor

fun getVideoFileName(anchorId: String): String {
    return when (anchorId) {
        "ua-9bbd55f93d6adbd759569897a449de19" -> "test.mp4"
        else -> "badapple.mp4"
    }
}
