# geofire-ktx

[![](https://jitpack.io/v/psteiger/geofire-ktx.svg)](https://jitpack.io/#psteiger/geofire-ktx)

Kotlin extension functions for easy and idiomatic `GeoQuery`ing on Firebase Database with GeoFire, on Android.

## Why geofire-ktx?

GeoFire is built upon callbacks.

This library provides a series of extension functions that allow for a more idiomatic use of GeoQuery in Kotlin Android projects, by:

1. Converting `GeoQuery` callbacks to Kotlin `Flow`s
2. Automatically mapping `GeoLocation` `Flow`s to corresponding `DataSnapshot` in another database path.
3. Automatically mapping `GeoLocation` `Flow`s to corresponding data of type `T` in another database path.

## Installation 

On project-level `build.gradle`, add [Jitpack](https://jitpack.io/) repository:

```groovy
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}
```

On app-level `build.gradle`, add dependency:

```groovy
dependencies {
    implementation 'com.github.psteiger:geofire-ktx:0.6.0'
}
```

## Usage

Given the `GeoQuery`:

```kotlin
val geoFire = GeoFire(Firebase.database.getReference("geofire"))
val geoLocation = GeoLocation(0.0, 0.0)
val radius = 100.0
val geoQuery = geoFire.queryAtLocation(geoLocation, radius)
```

### GeoQuery as Flows

We recommend converting the GeoQuery to Flows for consuming the query result data. You can convert it to:

1. `Flow<Map<Key, GeoLocation>>`
2. `Flow<Map<Key, LocationDataSnapshot>>` // A pair of GeoLocation and DataSnapshot
3. `Flow<Map<Key, LocationData<T>>`       // A pair of GeoLocation and data of type T.

2 and 3 are for the use cases of querying GeoLocations to subsequently query for the related data on another database reference.

#### Querying for GeoLocations

```kotlin
val nearbyGeoLocations: Flow<Map<Key, GeoLocation>> = 
    geoQuery
        .asFlow()
        .flowOn(Dispatchers.IO)
```

#### Querying for locations and corresponding DataSnapshots with same keys in /users

```kotlin
val nearbyUsers: Flow<Map<Key, LocationDataSnapshot>> = 
    geoQuery
        .asFlow(Firebase.database.getReference("users"))
        .flowOn(Dispatchers.IO)
        .onEach { map ->
            map.onEach {
                val key = it.key
                val (geoLocation, dataSnapshot) = it.value
            }
        }
```

#### Querying for locations and corresponding users with same keys in /users

```kotlin
val nearbyUsers: Flow<Map<Key, LocationData<User>>> = 
    geoQuery
        .asTypedFlow<User>(Firebase.database.getReference("users"))
        .flowOn(Dispatchers.IO)
        .onEach { map ->
            map.onEach {
                val key = it.key
                val (geoLocation, user) = it.value
            }
        }
```

#### Notes

1. In examples above, `Key` is just a `typealias` to `String`.
2. All flows above are *cold*, and need to be collected so they start running (e.g. with `launchIn()`)
3. Consider converting the flows to `SharedFlow` (`Flow<T>.shareIn()`) or `StateFlow` (`Flow<T>.stateIn()`) if multiple collectors will be used.


### Builder

If you still want to use GeoQuery callbacks and not Kotlin flows, we also offer a convenience builder for building the callback object in a more idiomatic way:

```kotlin
sealed class GeoQueryState { 
    data class Ready(val geoLocations: Map<Key, GeoLocation>) : GeoQueryState
    data class Cancelled(val exception: Exception) : GeoQueryState
}

private val _geoLocations = MutableSharedFlow<GeoQueryState>() // private mutable shared flow
val geoLocations = _geoLocations.asSharedFlow() // publicly exposed as read-only shared flow

geoQuery.addGeoQueryEventListener {
    val locations = mutableMapOf<Key, GeoLocation>()

    onKeyEntered { locations[this] = it }
    onKeyExited { locations.remove(this) }
    onKeyMoved { locations[this] = it }
    onGeoQueryReady { geoLocations.emit(GeoQueryState.Ready(locations.toMap())) }
    onGeoQueryError { geoLocations.emit(GeoQueryState.Cancelled(toException())) }
}
```
