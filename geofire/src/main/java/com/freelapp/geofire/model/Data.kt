package com.freelapp.geofire.model

import com.firebase.geofire.GeoLocation
import com.google.firebase.database.DataSnapshot

data class LocationDataSnapshot(
    val location: GeoLocation,
    val data: DataSnapshot
)

data class LocationData<T>(
    val location: GeoLocation,
    val data: T?
)