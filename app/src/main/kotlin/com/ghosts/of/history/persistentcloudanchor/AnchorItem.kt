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

/** Container class holding identifying information for an Anchor to be resolved.  */
class AnchorItem(val anchorId: String, val anchorName: String, private val minutesSinceCreation: Long) {
    var isSelected = false

    fun getMinutesSinceCreation(): String {
        return if (minutesSinceCreation < 60) {
            minutesSinceCreation.toString() + "m ago"
        } else {
            (minutesSinceCreation.toInt() / 60).toString() + "hr ago"
        }
    }
}