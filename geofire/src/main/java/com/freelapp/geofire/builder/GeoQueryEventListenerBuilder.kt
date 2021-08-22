package com.freelapp.geofire.builder

import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.database.DatabaseError

@PublishedApi
internal inline fun GeoQuery.addGeoQueryEventListenerImpl(
    block: GeoQueryEventListenerBuilder.() -> Unit
): GeoQueryEventListener =
    GeoQueryEventListener(block)
        .also { addGeoQueryEventListener(it) }

@Suppress("FunctionName")
@PublishedApi
internal inline fun GeoQueryEventListener(
    block: GeoQueryEventListenerBuilder.() -> Unit
) = GeoQueryEventListenerBuilder().apply(block).build()

class GeoQueryEventListenerBuilder(
    var onKeyEntered: ((String, GeoLocation) -> Unit)? = null,
    var onKeyExited: ((String) -> Unit)? = null,
    var onKeyMoved: ((String, GeoLocation) -> Unit)? = null,
    var onGeoQueryReady: (() -> Unit)? = null,
    var onGeoQueryError: ((DatabaseError) -> Unit)? = null
) {
    fun onKeyEntered(onKeyEntered: (String, GeoLocation) -> Unit) =
        apply { this.onKeyEntered = onKeyEntered }
    fun onKeyExited(onKeyExited: (String) -> Unit) =
        apply { this.onKeyExited = onKeyExited }
    fun onKeyMoved(onKeyMoved: (String, GeoLocation) -> Unit) =
        apply { this.onKeyMoved = onKeyMoved }
    fun onGeoQueryReady(onGeoQueryReady: () -> Unit) =
        apply { this.onGeoQueryReady = onGeoQueryReady }
    fun onGeoQueryError(onGeoQueryError: (DatabaseError) -> Unit) =
        apply { this.onGeoQueryError = onGeoQueryError }

    fun build() = object : GeoQueryEventListener {
        override fun onKeyEntered(key: String, l: GeoLocation) {
            this@GeoQueryEventListenerBuilder.onKeyEntered?.invoke(key, l)
        }

        override fun onKeyExited(key: String) {
            this@GeoQueryEventListenerBuilder.onKeyExited?.invoke(key)
        }

        override fun onKeyMoved(key: String, l: GeoLocation) {
            this@GeoQueryEventListenerBuilder.onKeyMoved?.invoke(key, l)
        }

        override fun onGeoQueryReady() {
            this@GeoQueryEventListenerBuilder.onGeoQueryReady?.invoke()
        }

        override fun onGeoQueryError(error: DatabaseError) {
            this@GeoQueryEventListenerBuilder.onGeoQueryError?.invoke(error)
        }
    }
}
