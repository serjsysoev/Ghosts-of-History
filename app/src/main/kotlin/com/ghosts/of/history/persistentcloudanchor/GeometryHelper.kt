package com.ghosts.of.history.persistentcloudanchor

import android.opengl.Matrix
import android.util.Log
import com.google.ar.core.Pose
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt

fun getCameraPos(view: FloatArray): FloatArray {
    val inv = FloatArray(16)
    Matrix.invertM(inv, 0, view, 0)
    return floatArrayOf(inv[12], inv[13], inv[14])
}

fun getCameraLook(view: FloatArray): FloatArray {
    // get the inverse of the view matrix
    val inverseView = FloatArray(16)
    android.opengl.Matrix.invertM(inverseView, 0, view, 0)

    // get the z-vector from view matrix
    val zVector = floatArrayOf(inverseView[8], inverseView[9], inverseView[10])

    // normalize the z-vector
    // android.opengl.Matrix.multiplyMV(zVector, 0, zVector, 0, zVector, 0)
    val zVectorLen = getVectorLength(zVector)
    if (abs(zVectorLen) < 0.0001) {
        Log.d("DIVZERO", "/0")
    }
    zVector[0] = zVector[0] / zVectorLen
    zVector[0] = zVector[1] / zVectorLen
    zVector[0] = zVector[2] / zVectorLen

    return zVector
}

fun getVectorLength(vec: FloatArray): Float {
    return sqrt(vec[0] * vec[0] + vec[1] * vec[1] + vec[2] * vec[2])
}

fun getViewAngle(view: FloatArray, anchorPose: Pose): Double {
    val cameraPos = getCameraPos(view)
    val cameraLook = getCameraLook(view)
    val diffVector = floatArrayOf(
            anchorPose.tx() - cameraPos[0],
            anchorPose.ty() - cameraPos[1],
            anchorPose.tz() - cameraPos[2])
    val dotProduct = diffVector[0] * cameraLook[0] + diffVector[1] * cameraLook[1] + diffVector[2] * cameraLook[2]
    return acos(dotProduct / (getVectorLength(cameraLook) * getVectorLength(diffVector))) / Math.PI * 180

}

fun canAnchorBeSeen(view: FloatArray, anchorPose: Pose): Boolean {
    // An anchor can be seen if the angle between the camera's look
    // and the vector from camera to the anchor is at least 130 degrees
    // this was observed empirically
    return getViewAngle(view, anchorPose) >= 130
}
