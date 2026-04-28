package com.amro.domain.result

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

    @Test
    fun `flatMap transforms success into another result`() {
        val result = DomainResult.Success(21).flatMap { value ->
            DomainResult.Success(value * 2)
        }

        assertEquals(DomainResult.Success(42), result)
    }

    @Test
    fun `flatMap keeps error unchanged`() {
        val error = DomainResult.Error(DomainError.Server)

        val result = error.flatMap { value: Int ->
            DomainResult.Success(value * 2)
        }

        assertSame(error, result)
    }

    @Test
    fun `onSuccess invokes action for success and returns same result`() {
        val success = DomainResult.Success(42)
        var observed: Int? = null

        val result = success.onSuccess { observed = it }

        assertSame(success, result)
        assertEquals(42, observed)
    }

    @Test
    fun `onSuccess skips action for error`() {
        val error = DomainResult.Error(DomainError.NotFound)
        var invoked = false

        val result = error.onSuccess { invoked = true }

        assertSame(error, result)
        assertEquals(false, invoked)
    }

    @Test
    fun `onError invokes action for error and returns same result`() {
        val error = DomainResult.Error(DomainError.RateLimited)
        var observed: DomainError? = null

        val result = error.onError { observed = it }

        assertSame(error, result)
        assertEquals(DomainError.RateLimited, observed)
    }

    @Test
    fun `onError skips action for success`() {
        val success = DomainResult.Success(42)
        var invoked = false

        val result = success.onError { invoked = true }

        assertSame(success, result)
        assertEquals(false, invoked)
    }

    @Test
    fun `getOrNull returns value for success`() {
        assertEquals(42, DomainResult.Success(42).getOrNull())
    }

    @Test
    fun `getOrNull returns null for error`() {
        assertNull(DomainResult.Error(DomainError.Unknown()).getOrNull())
    }

    @Test
    fun `getOrElse returns value for success`() {
        val result = DomainResult.Success(42).getOrElse { 0 }

        assertEquals(42, result)
    }

    @Test
    fun `getOrElse returns default for error`() {
        val result = DomainResult.Error(DomainError.Unknown()).getOrElse { 0 }

        assertEquals(0, result)
    }
}
