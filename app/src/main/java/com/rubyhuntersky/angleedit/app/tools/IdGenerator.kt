package com.rubyhuntersky.angleedit.app.tools

import java.math.BigInteger
import java.security.SecureRandom

object IdGenerator {
    private val random by lazy { SecureRandom() }

    fun nextId(): String {
        return BigInteger(130, random).toString(32)
    }
}