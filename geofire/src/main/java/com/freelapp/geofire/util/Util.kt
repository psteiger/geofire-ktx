package com.freelapp.geofire.util

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.GenericTypeIndicator
import kotlinx.coroutines.channels.SendChannel

internal typealias Key = String

internal fun <E> SendChannel<E>.tryOffer(element: E): Boolean =
    runCatching { offer(element) }.getOrDefault(false)

@PublishedApi
internal inline fun <reified T : Any> DataSnapshot.getTypedValue(): T? =
    getValue(object : GenericTypeIndicator<T>() {})