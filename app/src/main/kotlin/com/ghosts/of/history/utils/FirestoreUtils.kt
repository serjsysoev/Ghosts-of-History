package com.ghosts.of.history.utils

import android.content.Context
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*


// onSuccessCallback processes an in-storage-path of this video
fun processVideoPathByName(videoName: String, onSuccessCallback: (String?) -> Unit) {
    Firebase.firestore.collection("StorageLinks").whereEqualTo("id", videoName).get().addOnSuccessListener {docs ->
        val firstDoc = if (docs.documents.size > 0) {docs.documents[0]} else {null}
        onSuccessCallback(firstDoc?.get("in_storage_path") as String?)
    }
}

// onSuccessCallback processes an in-storage-path of this video
fun saveAnchorToFirebase(anchorId: String, anchorName: String) {
    val document = mapOf(
            "id" to anchorId,
            "name" to anchorName,
            "video_name" to ""
    )
    Firebase.firestore
            .collection("AnchorBindings")
            .document()
            .set(document)
}

fun getAnchorsDataFromFirebase(onSuccessCallback: (List<AnchorData>) -> Unit) {
    Firebase.firestore
            .collection("AnchorBindings")
            .whereNotEqualTo("video_name", "")
            .get()
            .addOnSuccessListener { result ->
                result.map {
                    AnchorData(
                            it.get("id") as String,
                            it.get("video_name") as String
                    )
                }.let(onSuccessCallback)
            }
}

data class AnchorData(val anchorId: String, val videoName: String)

// onSuccessCallback processes just a video name
fun processAnchorDescription(anchorId: String, onSuccessCallback: (String?) -> Unit) {
    Firebase.firestore.collection("AnchorBindings").whereEqualTo("id", anchorId).get().addOnSuccessListener {docs ->
        val firstDoc = if (docs.documents.size > 0) {docs.documents[0]} else {null}
        onSuccessCallback(firstDoc?.get("video_name") as String?)
    }
}

fun processAnchorSets(setName: String, onSuccessCallback: (Array<String>?) -> Unit) {
    Firebase.firestore.collection("AnchorSets").whereEqualTo("name", setName).get().addOnSuccessListener {docs ->
        val firstDoc = if (docs.documents.size > 0) {docs.documents[0]} else {null}
        onSuccessCallback(firstDoc?.get("anchor_ids") as Array<String>?)
    }
}

fun fetchVideoFromStorage(path: String, context: Context, onSuccessCallback: (File) -> Unit) {
    Firebase.storage.reference.child(path).downloadUrl.addOnSuccessListener {
        CoroutineScope(Dispatchers.IO).launch {
            val url = URL(it.toString())
            val connection = url.openConnection()
            connection.connect()
            val stream = connection.getInputStream()
            val randomFilename = UUID.randomUUID().toString() + File(path).name
            val downloadingMediaFile = File(context.cacheDir, randomFilename)

            val out = FileOutputStream(downloadingMediaFile)
            stream.copyTo(out)
            onSuccessCallback(downloadingMediaFile)
        }
    }
}
