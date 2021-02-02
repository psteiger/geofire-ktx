package com.freelapp.geofire.flow

import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.freelapp.geofire.util.Key
import com.freelapp.geofire.addGeoQueryEventListener
import com.freelapp.geofire.model.LocationData
import com.freelapp.geofire.model.LocationDataSnapshot
import com.freelapp.geofire.util.getTypedValue
import com.freelapp.geofire.util.tryOffer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
internal fun GeoQuery.asFlowImpl(): Flow<Map<Key, GeoLocation>> = callbackFlow {
    val locations = mutableMapOf<Key, GeoLocation>()
    val listener = addGeoQueryEventListener {
        onKeyEntered { locations[this] = it }
        onKeyExited { locations.remove(this) }
        onKeyMoved { locations[this] = it }
        onGeoQueryReady { tryOffer(locations.toMap()) }
        onGeoQueryError { cancel(CancellationException("API Error", toException())) }
    }
    awaitClose { removeGeoQueryEventListener(listener) }
}

@PublishedApi
@ExperimentalCoroutinesApi
internal fun GeoQuery.asFlowImpl(
    dataRef: String
): Flow<Map<Key, LocationDataSnapshot>> =
    asFlowImpl()
        .mapLatest { geoLocationMap ->
            geoLocationMap
                .mapValues { it.value to it.key.asDataSnapshotFlow(dataRef) }
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