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
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.Flow

// Builders

inline fun GeoQuery.addGeoQueryEventListener(
    block: GeoQueryEventListenerBuilder.() -> Unit
): GeoQueryEventListener =
    addGeoQueryEventListenerImpl(block)

// Flow

/**
 * Transforms a [GeoQuery] into a cold [Flow] of maps between [GeoQuery] keys and corresponding
 * [GeoLocation]s.
 *
 * @return A [Flow] of maps between [GeoQuery] keys and corresponding [GeoLocation]s.
 */
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
fun GeoQuery.asFlow(): Flow<Map<Key, GeoLocation>> =
    asFlowImpl()

/**
 * Transforms a [GeoQuery] into a cold [Flow] of maps between [GeoQuery] keys and corresponding
 * [LocationDataSnapshot] objects, which contain the [GeoLocation] and the [DataSnapshot] stored in
 * "[dataRef]/$key".
 *
 * @return A [Flow] of maps between [GeoQuery] keys and corresponding [LocationDataSnapshot] objects.
 */
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
fun GeoQuery.asFlow(
    dataRef: DatabaseReference
): Flow<Map<Key, LocationDataSnapshot>> =
    asFlowImpl(dataRef)

/**
 * Transforms a [GeoQuery] into a cold [Flow] of maps between [GeoQuery] keys and corresponding
 * [LocationData] objects, which contain the [GeoLocation] and an object of type [T] (the object
 * obtained by converting the [DataSnapshot] stored in "[dataRef]/$key" to an object of type [T]).
 * <p>
 * If conversion of the [DataSnapshot] to an object of type [T] fails, [LocationData]'s data will be
 * null.
 *
 * @return a flow of a mapping between the key and the [LocationData].
 */
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
inline fun <reified T : Any> GeoQuery.asTypedFlow(
    dataRef: DatabaseReference
): Flow<Map<Key, LocationData<T?>>> =
    asTypedFlowImpl(dataRef)

/**
 * Transforms a [GeoQuery] into a cold [Flow] of lists of objects of type [U].
 * Conversion from the GeoQuery's key, location, and the object of type [T] stored in [dataRef],
 * to type [U] is given by the [combiner] function.
 *
 * @return a flow of lists of [U].
 */
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
inline fun <reified T : Any, U> GeoQuery.asTypedFlow(
    dataRef: DatabaseReference,
    crossinline combiner: (key: String, location: GeoLocation, data: T) -> U
): Flow<List<U>> =
    asTypedFlowImpl(dataRef, combiner)

/**
 * Transforms a [GeoQuery] into a cold [Flow] of maps between [GeoQuery] keys and corresponding
 * [LocationData] objects, which contain the [GeoLocation] and an object of type [T] (the object
 * obtained by converting the [DataSnapshot] stored in "[dataRef]/$key" to an object of type [T]).
 * <p>
 * If conversion of the [DataSnapshot] to an object of type [T] fails, [LocationData]'s data will be
 * null.
 *
 * @return a flow of a mapping between the key and the [LocationData].
 */
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
fun <T : Any> GeoQuery.asTypedFlow(
    clazz: Class<T>,
    dataRef: DatabaseReference
): Flow<Map<Key, LocationData<T?>>> =
    asTypedFlowImpl(clazz, dataRef)