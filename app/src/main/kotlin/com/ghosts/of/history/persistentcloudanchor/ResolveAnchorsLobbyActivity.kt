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

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ghosts.of.history.R
import com.ghosts.of.history.common.helpers.DisplayRotationHelper
import com.google.android.material.button.MaterialButton
import java.util.concurrent.TimeUnit

/** Lobby activity for resolving anchors in the Persistent Cloud Anchor Sample.  */
class ResolveAnchorsLobbyActivity : AppCompatActivity() {
    private lateinit var selectedAnchors: List<AnchorItem>
    private lateinit var displayRotationHelper: DisplayRotationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.resolve_anchors_lobby)
        displayRotationHelper = DisplayRotationHelper(this)
        val resolveButton = findViewById<MaterialButton>(R.id.resolve_button)
        resolveButton.setOnClickListener { onResolveButtonPress() }
        val sharedPreferences = getSharedPreferences(CloudAnchorActivity.PREFERENCE_FILE_KEY, MODE_PRIVATE)
        selectedAnchors = retrieveStoredAnchors(sharedPreferences)
        val spinner = findViewById<View>(R.id.select_anchors_spinner) as Spinner
        val adapter = MultiSelectItem(this, 0, selectedAnchors, spinner)
        spinner.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        displayRotationHelper.onResume()
    }

    public override fun onPause() {
        super.onPause()
        displayRotationHelper.onPause()
    }

    /** Callback function invoked when the Resolve Button is pressed.  */
    private fun onResolveButtonPress() {
        // Toast.makeText(this, "Lat: ${getCurrentLocation(this)}", Toast.LENGTH_LONG).show()
        val anchorsToResolve = selectedAnchors
                .filter { it.isSelected }
                .map { it.anchorId }
                .let(::ArrayList)
        val enteredAnchorIds = findViewById<View>(R.id.anchor_edit_text) as EditText
        anchorsToResolve += enteredAnchorIds.text.toString()
                .trim { it <= ' ' }
                .split(",")
                .filter { it.isNotEmpty() }
        val intent: Intent = CloudAnchorActivity.newResolvingIntent(this, anchorsToResolve)
        startActivity(intent)
    }

    companion object {
        fun retrieveStoredAnchors(anchorPreferences: SharedPreferences): List<AnchorItem> {
            val anchors: MutableList<AnchorItem> = ArrayList()
            val hostedAnchorIds = anchorPreferences.getString(CloudAnchorActivity.HOSTED_ANCHOR_IDS, "")!!
            val hostedAnchorNames = anchorPreferences.getString(CloudAnchorActivity.HOSTED_ANCHOR_NAMES, "")!!
            val hostedAnchorMinutes = anchorPreferences.getString(CloudAnchorActivity.HOSTED_ANCHOR_MINUTES, "")!!
            if (hostedAnchorIds.isNotEmpty()) {
                val anchorIds = hostedAnchorIds.split(";")
                val anchorNames = hostedAnchorNames.split(";")
                val anchorMinutes = hostedAnchorMinutes.split(";")
                for (i in 0 until anchorIds.size - 1) {
                    val timeSinceCreation = (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis())
                            - anchorMinutes[i].toLong())
                    if (timeSinceCreation < 24 * 60) {
                        anchors.add(AnchorItem(anchorIds[i], anchorNames[i], timeSinceCreation))
                    }
                }
            }
            return anchors
        }

        /** Callback function invoked when the Host Button is pressed.  */
        fun newIntent(packageContext: Context?): Intent {
            return Intent(packageContext, ResolveAnchorsLobbyActivity::class.java)
        }
    }
}