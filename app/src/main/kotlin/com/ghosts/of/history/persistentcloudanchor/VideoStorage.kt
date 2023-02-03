package com.ghosts.of.history.persistentcloudanchor

fun getVideoFileName(anchorId: String): String {
    return when (anchorId) {
        "ua-889485a1b8f9e87b387a281f88b535ed" -> "test.mp4"
        else -> "badapple.mp4"
    }
}
