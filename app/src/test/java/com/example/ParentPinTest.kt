package com.example

import com.example.util.ParentPin
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ParentPinTest {
    @Test fun hashesAndVerifiesPinWithoutPersistingPlaintext() {
        val stored = ParentPin.hash("806427")
        assertFalse(stored.contains("806427"))
        assertTrue(ParentPin.verify("806427", stored))
        assertFalse(ParentPin.verify("806428", stored))
        assertFalse(ParentPin.verify("", stored))
    }

    @Test fun refusesDefaultAndMalformedPin() {
        assertFalse(ParentPin.isValid("1234"))
        assertFalse(ParentPin.isValid("abcd"))
        assertFalse(ParentPin.isValid("123"))
        assertFalse(ParentPin.verify("1234", ""))
        assertFalse(ParentPin.verify("1234", "pbkdf2-sha1:garbage"))
    }
}
