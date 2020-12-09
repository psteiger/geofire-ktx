@file:Suppress("unused")

package com.freelapp.geofire

import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.freelapp.geofire.builder.GeoQueryEventListenerBuilder
import com.freelapp.geofire.builder.addGeoQueryEventListenerImpl
import com.freelapp.geofire.flow.asFlowImpl
import com.freelapp.geofire.flow.asTypedFlowImpl
import com.freelapp.geofire.model.LocationData
import com.freelapp.geofire.model.LocationDataSnapshot
import com.freelapp.geofire.util.Key
import com.google.firebase.database.DataSnapshot
import kotlinx.coroutines.flow.Flow

// Builders

fun GeoQuery.addGeoQueryEventListener(
    block: GeoQueryEventListenerBuilder.() -> Unit
): GeoQueryEventListener = addGeoQueryEventListenerImpl(block)

// Flow

/**
 * Transforms a [GeoQuery] into a cold [Flow] of pairs between the key and the [GeoLocation].
 * @return a flow of a mapping between the key and the [GeoLocation].
 */
fun GeoQuery.asFlow(): Flow<Map<Key, GeoLocation>> = asFlowImpl()

/**
 * Transforms a [GeoQuery] into a cold [Flow] of pairs between the key and [LocationDataSnapshot]
 * objects, which contain the [GeoLocation] and the [DataSnapshot] stored in [dataRef].
 * @return a flow of a mapping between the key and the [LocationDataSnapshot].
 */
fun GeoQuery.asFlow(
    dataRef: String
): Flow<Map<Key, LocationDataSnapshot>> = asFlowImpl(dataRef)

/**
 * Transforms a [GeoQuery] into a cold [Flow] of pairs between the key and [LocationData] objects,
 * which contain the [GeoLocation] and an object of type [T] (the object obtained by converting the
 * [DataSnapshot] stored in [dataRef] to an object of type [T]).
 * <p>
 * If conversion of the [DataSnapshot] to an object of type [T] fails, [LocationData]'s data will be
 * null.
 *
 * @return a flow of a mapping between the key and the [LocationData].
 */
inline fun <reified T : Any> GeoQuery.asTypedFlow(
    dataRef: String
): Flow<Map<Key, LocationData<T>>> = asTypedFlowImpl(dataRef)