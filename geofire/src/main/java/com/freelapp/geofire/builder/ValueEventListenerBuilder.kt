package com.freelapp.geofire.builder

import com.google.firebase.database.*
import kotlinx.coroutines.flow.*

internal fun Query.addValueEventListener(block: ValueEventListenerBuilder.() -> Unit) =
    addValueEventListener(ValueEventListener(block))

@Suppress("FunctionName")
private fun ValueEventListener(block: ValueEventListenerBuilder.() -> Unit) =
    ValueEventListenerBuilder().apply(block).build()

internal class ValueEventListenerBuilder(
    private var onDataChange: ((DataSnapshot) -> Unit)? = null,
    private var onCancelled: ((DatabaseError) -> Unit)? = null
) {
    fun onDataChange(onDataChange: DataSnapshot.() -> Unit) =
        apply { this.onDataChange = onDataChange }
    fun onCancelled(onCancelled: DatabaseError.() -> Unit) =
        apply { this.onCancelled = onCancelled }

    fun build() = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            this@ValueEventListenerBuilder.onDataChange?.invoke(snapshot)
        }

        override fun onCancelled(error: DatabaseError) {
            this@ValueEventListenerBuilder.onCancelled?.invoke(error)
        }
    }
}
