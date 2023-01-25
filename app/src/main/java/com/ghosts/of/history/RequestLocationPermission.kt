package com.ghosts.of.history

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.ghosts.of.history.common.helpers.DisplayRotationHelper
import com.ghosts.of.history.databinding.ActivityRequestLocationPermissionBinding
import com.ghosts.of.history.persistentcloudanchor.CloudAnchorActivity
import com.ghosts.of.history.persistentcloudanchor.MUSEUM_LIST_URL
import com.ghosts.of.history.persistentcloudanchor.PERMISSION_REQUEST_CODE
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

data class Museum(val name: String, val lat: Double, val lng: Double)

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class RequestLocationPermission : AppCompatActivity(), OnTouchListener {

    private lateinit var displayRotationHelper: DisplayRotationHelper

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_location_permission)
        displayRotationHelper = DisplayRotationHelper(this)
        findViewById<FrameLayout>(R.id.requestPermissionsLayout).setOnTouchListener(this)
    }

    override fun onResume() {
        super.onResume()
        displayRotationHelper.onResume()
    }

    public override fun onPause() {
        super.onPause()
        displayRotationHelper.onPause()
    }

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Manifest.permission.ACCESS_FINE_LOCATION in permissions) {
            Toast.makeText(this, "Success", Toast.LENGTH_LONG).show()
            val anchorsToResolve = mutableListOf<String>()
            val museums: List<Museum>
            val url = URL(MUSEUM_LIST_URL)
            val connection = url.openConnection() as HttpURLConnection
            try {
                val museumListRaw = connection.inputStream.bufferedReader().readText()
                museums = museumListRaw.split('\n').map { it.trim() }.map { row ->
                    val rowSplit = row.split(' ').map { it.trim() }.filter { it.isNotEmpty() }
                    Museum(rowSplit[0], rowSplit[1].toDouble(), rowSplit[2].toDouble())
                }
            } finally {
                connection.disconnect()
            }
            // TODO: I stopped here
            val intent: Intent = CloudAnchorActivity.newResolvingIntent(this, ArrayList(anchorsToResolve))
            startActivity(intent)
        }
    }

}