package com.ghosts.of.history.persistentcloudanchor

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService

internal fun getCurrentLocation(activity: Activity): Location? {
    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(activity, "WARNING: THIS IS NOT SUPPOSED TO HAPPEN", Toast.LENGTH_SHORT).show()
        return null
    }
    val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.getProviders(true).reversed().firstNotNullOfOrNull { locationManager.getLastKnownLocation(it) }
}
