/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ghosts.of.history.persistentcloudanchor

import android.util.Log
import com.google.ar.core.Anchor
import com.google.ar.core.Anchor.CloudAnchorState
import com.google.ar.core.Session

/**
 * A helper class to handle all the Cloud Anchors logic, and add a callback-like mechanism on top of
 * the existing ARCore API.
 */
internal class CloudAnchorManager(private val session: Session) {
    /** Listener for the results of a host operation.  */
    internal interface CloudAnchorListener {
        /** This method is invoked when the results of a Cloud Anchor operation are available.  */
        fun onComplete(anchor: Anchor)
    }

    private val pendingAnchors: MutableMap<Anchor, CloudAnchorListener> = HashMap()

    /** Hosts an anchor. The `listener` will be invoked when the results are available.  */
    @Synchronized
    fun hostCloudAnchor(anchor: Anchor?, listener: CloudAnchorListener) {
        // Creating a Cloud Anchor with lifetime  = 1 day. This is configurable up to 365 days.
        val newAnchor = session.hostCloudAnchorWithTtl(anchor,  /* ttlDays = */1)
        pendingAnchors[newAnchor] = listener
    }

    /** Resolves an anchor. The `listener` will be invoked when the results are available.  */
    @Synchronized
    fun resolveCloudAnchor(anchorId: String?, listener: CloudAnchorListener) {
        val newAnchor = session.resolveCloudAnchor(anchorId)
        pendingAnchors[newAnchor] = listener
    }

    /** Should be called after a [Session.update] call.  */
    @Synchronized
    fun onUpdate() {
        val it: MutableIterator<Map.Entry<Anchor, CloudAnchorListener>> = pendingAnchors.entries.iterator()
        while (it.hasNext()) {
            val (anchor, listener) = it.next()
            if (isReturnableState(anchor.cloudAnchorState)) {
                listener.onComplete(anchor)
                it.remove() // ToDo: I may add tracking of anchors going out of view by changing this line
                // No, I cannot. I have tried to comment out this line and after an anchor has
                // been found the code inside this if would be executed regardless of whether
                // the camera sees the anchor.
                // I have tried logging anchor.cloudAnchorState, it is COMPLETE even if you
                // close the camera with your hand after it spots that anchor once

                // for that reason, I will need to use OpenGL methods or even manual
                // computation of 3D spaces matrices
                // (https://github.com/google-ar/arcore-android-sdk/issues/78)
                // (https://stackoverflow.com/questions/2407363/how-do-you-determine-when-an-object-is-drawn-on-screen-in-opengl)
                // (https://stackoverflow.com/questions/52828668/how-to-calculate-field-of-view-in-arcore)
                // in order to determine whether the anchor is still in the view
            }
        }
    }

    /** Clears any currently registered listeners, so they won't be called again.  */
    @Synchronized
    fun clearListeners() {
        pendingAnchors.clear()
    }

    companion object {
        private fun isReturnableState(cloudState: CloudAnchorState): Boolean {
            return when (cloudState) {
                CloudAnchorState.NONE, CloudAnchorState.TASK_IN_PROGRESS -> false
                else -> true
            }
        }
    }
}