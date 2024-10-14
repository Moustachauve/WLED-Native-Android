package ca.cgagnier.wlednativeandroid.domain.use_case

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test

class ValidateAddressTest {

    @Before
    fun setUp() {
    }

    @Test
    fun executeEmpty() {
        val validateAddress = ValidateAddress()
        val result = validateAddress.execute("")
        assertEquals(result.successful, false)
    }
}