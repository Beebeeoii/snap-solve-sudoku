package com.beebeeoii.snapsolvesudoku

import java.util.*

object UniqueIdGenerator {

    lateinit var uniqueId: String

    fun generateId() : UniqueIdGenerator {
        uniqueId = UUID.randomUUID().toString().substring(0, 8)
        return this
    }

    fun resetId() {
        uniqueId = null.toString()
    }
}