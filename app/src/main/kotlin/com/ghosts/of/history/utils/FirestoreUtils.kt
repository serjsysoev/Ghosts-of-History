package com.ghosts.of.history.utils

import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.ktx.storage


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

fun fetchVideoFromStorage(path: String, onSuccessCallback: (Uri?) -> Unit) {
    val storage = Firebase.storage.reference.child("gs://ghosts-of-history.appspot.com/$path").downloadUrl.addOnSuccessListener {
        onSuccessCallback(it)
    }
}
