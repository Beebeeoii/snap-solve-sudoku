package com.beebeeoii.snapsolvesudoku.utils

import java.util.*

/**
 * UniqueIdGenerator is a helper class that generates random IDs.
 */
object UniqueIdGenerator {
    /**
     * Generates a random 8 character ID.
     *
     * @return The generated 8 character string of ID.
     */
    fun generateId(): String {
        return UUID.randomUUID().toString().substring(0, 8)
    }
}