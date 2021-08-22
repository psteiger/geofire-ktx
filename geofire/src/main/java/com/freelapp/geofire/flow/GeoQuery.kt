package com.freelapp.geofire.flow

import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.freelapp.firebase.database.rtdb.valueFlow
import com.freelapp.geofire.util.Key
import com.freelapp.geofire.addGeoQueryEventListener
import com.freelapp.geofire.model.LocationData
import com.freelapp.geofire.model.LocationDataSnapshot
import com.freelapp.geofire.util.getTypedValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

private sealed class Msg {
    data class LocationChange(val block: (MutableMap<Key, GeoLocation>) -> Unit): Msg()
    object Ready : Msg()
}

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
internal fun GeoQuery.asFlowImpl(): Flow<Map<Key, GeoLocation>> = callbackFlow {
    val channel = actor<Msg>(capacity = Channel.UNLIMITED) {
        val locations = mutableMapOf<Key, GeoLocation>()
        var initialDataHandled = false
        fun maybeSend() {
            if (initialDataHandled) trySend(locations.toMap())
        }
        for (msg in channel) {
            when (msg) {
                is Msg.LocationChange -> msg.block(locations)
                is Msg.Ready -> initialDataHandled = true
            }
            maybeSend()
        }
    }

    val listener = addGeoQueryEventListener {
        onKeyEntered { key, location ->
            channel.trySend(Msg.LocationChange { it[key] = location })
        }
        onKeyExited { key ->
            channel.trySend(Msg.LocationChange { it.remove(key) })
        }
        onKeyMoved { key, location ->
            channel.trySend(Msg.LocationChange { it[key] = location })
        }
        onGeoQueryReady {
            channel.trySend(Msg.Ready)
        }
        onGeoQueryError {
            cancel("API Error", it.toException())
        }
    }

    awaitClose { removeGeoQueryEventListener(listener) }
}.flowOn(Dispatchers.IO)

@ObsoleteCoroutinesApi
@PublishedApi
@ExperimentalCoroutinesApi
internal fun GeoQuery.asFlowImpl(
    dataRef: String
): Flow<Map<Key, LocationDataSnapshot>> =
    asFlowImpl()
        .mapLatest { geoLocationMap ->
            geoLocationMap
                .mapValues {
                    it.value to Firebase.database.getReference(dataRef)
                        .child(it.key)
                        .valueFlow()
                }
        }
        .flatMapLatest { geoLocationDataSnapshotFlowMap ->
            val snapFlows = geoLocationDataSnapshotFlowMap.map { it.value.second }
            combine(snapFlows) { snapArray ->
                snapArray
                    .filterNot { it.key.isNullOrBlank() }
                    .associateBy { it.key as Key }
                    .mapValues {
                        val geoLocation = geoLocationDataSnapshotFlowMap.getValue(it.key).first
                        val dataSnapshot = it.value
                        LocationDataSnapshot(geoLocation, dataSnapshot)
                    }
            }.onEmpty { emit(emptyMap()) } // if no snaps are found, we need to emit.
        }
        .flowOn(Dispatchers.IO)

@ObsoleteCoroutinesApi
@PublishedApi
@ExperimentalCoroutinesApi
internal inline fun <reified T : Any> GeoQuery.asTypedFlowImpl(
    dataRef: String
): Flow<Map<Key, LocationData<T>>> =
    asFlowImpl(dataRef)
        .mapLatest { map ->
            map.mapValues {
                it.value.run {
                    LocationData(location, data.getTypedValue<T>())
                }
            }
        }
        .flowOn(Dispatchers.IO)

@ObsoleteCoroutinesApi
@PublishedApi
@ExperimentalCoroutinesApi
internal fun <T : Any> GeoQuery.asTypedFlowImpl(
    clazz: Class<T>,
    dataRef: String
): Flow<Map<Key, LocationData<T>>> =
    asFlowImpl(dataRef)
        .mapLatest { map ->
            map.mapValues {
                it.value.run {
                    LocationData(location, data.getTypedValue(clazz))
                }
            }
        }
        .flowOn(Dispatchers.IO)
