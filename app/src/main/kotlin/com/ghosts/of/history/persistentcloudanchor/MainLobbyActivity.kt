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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ghosts.of.history.R
import com.ghosts.of.history.common.helpers.DisplayRotationHelper
import com.google.android.material.button.MaterialButton

/** Main Navigation Activity for the Persistent Cloud Anchor Sample.  */
class MainLobbyActivity : AppCompatActivity() {
    private lateinit var displayRotationHelper: DisplayRotationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_lobby)
        displayRotationHelper = DisplayRotationHelper(this)
        val hostButton = findViewById<MaterialButton>(R.id.host_button)
        hostButton.setOnClickListener { onHostButtonPress() }
        val resolveButton = findViewById<MaterialButton>(R.id.begin_resolve_button)
        resolveButton.setOnClickListener { onResolveButtonPress() }
        onResolveButtonPress()
        if (getCurrentLocation(this) == null) {
            DataKeeper.isLocationNull = true
        }
        //Log.d("Loc","Lat: ${DataKeeper.location.latitude} Lng: ${DataKeeper.location.longitude}")
        //Toast.makeText(this, "Lat: ${DataKeeper.location.latitude} Lng: ${DataKeeper.location.longitude}", Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        displayRotationHelper.onResume()
    }

    public override fun onPause() {
        super.onPause()
        displayRotationHelper.onPause()
    }

    private fun onHostButtonPress() {
        val intent: Intent = CloudAnchorActivity.newHostingIntent(this)
        startActivity(intent)
    }

    /** Callback function invoked when the Resolve Button is pressed.  */
    private fun onResolveButtonPress() {
        val intent: Intent = ResolveAnchorsLobbyActivity.newIntent(this)
        startActivity(intent)
    }
}