package com.ghosts.of.history.persistentcloudanchor

fun getVideoFilenameByAnchorId(anchorId: String): String {
    return when (anchorId) {
        "ua-955bfe0a900fe0cc6656109858c1d5d4" -> "test.mp4"
        else -> "badapple.mp4"
    }
}
