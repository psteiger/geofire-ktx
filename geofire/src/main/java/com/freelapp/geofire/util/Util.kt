package com.freelapp.geofire.util

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.GenericTypeIndicator
import kotlinx.coroutines.channels.SendChannel

internal typealias Key = String

@PublishedApi
internal inline fun <reified T : Any> DataSnapshot.getTypedValue(): T? =
    getValue(object : GenericTypeIndicator<T>() {})

@PublishedApi
internal fun <T : Any> DataSnapshot.getTypedValue(clazz: Class<T>): T? =
    getValue(clazz)