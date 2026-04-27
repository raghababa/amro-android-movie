package com.amro.domain.result

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class DomainResultTest {

    @Test
    fun `map transforms success value`() {
        val result = DomainResult.Success(21).map { it * 2 }

        assertEquals(DomainResult.Success(42), result)
    }

    @Test
    fun `map keeps error unchanged`() {
        val error = DomainResult.Error(DomainError.Network())

        val result = error.map { value: Int -> value * 2 }

        assertSame(error, result)
    }
}
