package com.ghosts.of.history.common.helpers

import com.google.ar.core.Pose
import kotlin.math.*

/**
 * Created by inio on 2/28/18.
 */
object MathHelpers {
    /**
     * Returns a pose rotating about the origin so that the point `from` is rotated to be
     * colinear with the origin and `to`.  Rotation takes the shortest path.
     */
    fun rotateBetween(fromRaw: FloatArray, toRaw: FloatArray): Pose {
        val from = fromRaw.copyOf(3)
        normalize(from)
        val to = toRaw.copyOf(3)
        normalize(to)
        val cross = FloatArray(3)
        cross[0] = from[1] * to[2] - from[2] * to[1]
        cross[1] = from[2] * to[0] - from[0] * to[2]
        cross[2] = from[0] * to[1] - from[1] * to[0]
        val dot = from[0] * to[0] + from[1] * to[1] + from[2] * to[2]
        val angle = atan2(norm(cross).toDouble(), dot.toDouble()).toFloat()
        normalize(cross)
        val sinhalf = sin((angle / 2.0f).toDouble()).toFloat()
        val coshalf = cos((angle / 2.0f).toDouble()).toFloat()
        return Pose.makeRotation(cross[0] * sinhalf, cross[1] * sinhalf, cross[2] * sinhalf, coshalf)
    }

    fun axisRotation(axis: Int, angleRad: Float): Pose {
        val sinHalf = sin((angleRad / 2).toDouble()).toFloat()
        val cosHalf = cos((angleRad / 2).toDouble()).toFloat()
        return when (axis) {
            0 -> Pose.makeRotation(sinHalf, 0f, 0f, cosHalf)
            1 -> Pose.makeRotation(0f, sinHalf, 0f, cosHalf)
            2 -> Pose.makeRotation(0f, 0f, sinHalf, cosHalf)
            else -> throw IllegalArgumentException("invalid axis $axis")
        }
    }

    /**
     * Returns the 2-norm of the input array.
     */
    fun norm(input: FloatArray): Float {
        var sum = 0f
        for (f in input) {
            sum += f * f
        }
        return sqrt(sum.toDouble()).toFloat()
    }

    /**
     * Normalizes the input array in-place.
     */
    fun normalize(input: FloatArray) {
        val scale = 1 / norm(input)
        for (i in input.indices) {
            input[i] *= scale
        }
    }
}