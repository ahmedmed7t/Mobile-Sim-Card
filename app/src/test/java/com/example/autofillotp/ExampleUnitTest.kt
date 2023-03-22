package com.example.autofillotp

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals("", getMobileNumber("my name is Ahmed and "))
    }

    private fun getMobileNumber(fullMessage: String): String {
        val splitMessage = fullMessage.split("01".toRegex(), 2)
        var mobileNumber = ""
        if (splitMessage.size > 1) {
            mobileNumber = "01"+splitMessage[1].filter { it.isDigit() }
        }
        return mobileNumber
    }

}