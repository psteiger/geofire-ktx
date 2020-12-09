package com.freelapp.geofire.flow

import com.freelapp.geofire.builder.addValueEventListener
import com.freelapp.geofire.util.Key
import com.freelapp.geofire.util.tryOffer
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@ExperimentalCoroutinesApi
private fun DatabaseReference.asDataSnapshotFlow(): Flow<DataSnapshot> = callbackFlow {
    val listener = addValueEventListener {
        onDataChange { tryOffer(this) }
        onCancelled { cancel(CancellationException("API Error", toException())) }
    }
    awaitClose { removeEventListener(listener) }
}

@ExperimentalCoroutinesApi
internal fun Key.asDataSnapshotFlow(ref: String): Flow<DataSnapshot> =
    FirebaseDatabase.getInstance()
        .getReference("$ref/$this")
        .asDataSnapshotFlow()