package com.brentzey.functional

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertFailsWith

class IOTest {

    @Test
    fun testPureValue() {
        val io = IO.pure(42)
        assertEquals(42, io.unsafeRunSync())
    }

    @Test
    fun testOf() {
        val io = IO.of { "test" }
        assertEquals("test", io.unsafeRunSync())
    }

    @Test
    fun testMap() {
        val io = IO.of { 10 }
        val mapped = io.map { it * 2 }
        
        assertEquals(20, mapped.unsafeRunSync())
    }

    @Test
    fun testMapChaining() {
        val io = IO.of { 5 }
            .map { it + 10 }
            .map { it * 2 }
        
        assertEquals(30, io.unsafeRunSync())
    }

    @Test
    fun testFlatMap() {
        val io = IO.of { 10 }
        val result = io.flatMap { x -> IO.of { x * 2 } }
        
        assertEquals(20, result.unsafeRunSync())
    }

    @Test
    fun testFlatMapChaining() {
        val io = IO.of { 5 }
            .flatMap { x -> IO.of { x + 10 } }
            .flatMap { x -> IO.of { x * 2 } }
        
        assertEquals(30, io.unsafeRunSync())
    }

    @Test
    fun testRunToNullableSuccess() {
        val io = IO.of { "success" }
        val result = io.runToNullable()
        
        assertNotNull(result)
        assertEquals("success", result)
    }

    @Test
    fun testRunToNullableFailure() {
        val io = IO.of<String> {
            throw RuntimeException("error")
        }
        val result = io.runToNullable()
        
        assertNull(result)
    }

    @Test
    fun testRunToNullableCheckedException() {
        val io = IO.of<String> {
            throw Exception("checked error")
        }
        val result = io.runToNullable()
        
        assertNull(result)
    }

    @Test
    fun testUnsafeRunSyncSuccess() {
        val io = IO.of { "result" }
        val result = io.unsafeRunSync()
        
        assertEquals("result", result)
    }

    @Test
    fun testUnsafeRunSyncThrowsException() {
        val io = IO.of<String> {
            throw RuntimeException("error")
        }
        
        val exception = assertFailsWith<RuntimeException> {
            io.unsafeRunSync()
        }
        
        assertEquals("error", exception.message)
    }

    @Test
    fun testUnsafeRunSyncCheckedException() {
        val io = IO.of<String> {
            throw Exception("checked error")
        }
        
        assertFailsWith<Exception> {
            io.unsafeRunSync()
        }
    }

    @Test
    fun testAttemptSuccess() {
        val io = IO.of { 42 }
        val result = io.attempt()
        
        assertTrue(result.isSuccess)
        assertTrue(result is IO.Result.Success)
        assertEquals(42, (result as IO.Result.Success).value)
    }

    @Test
    fun testAttemptFailure() {
        val io = IO.of<Int> {
            throw RuntimeException("error")
        }
        val result = io.attempt()
        
        assertTrue(result.isFailure)
        assertTrue(result is IO.Result.Failure)
        assertEquals("error", (result as IO.Result.Failure).error.message)
    }

    @Test
    fun testAttemptCheckedException() {
        val io = IO.of<Int> {
            throw Exception("checked error")
        }
        val result = io.attempt()
        
        assertTrue(result.isFailure)
        assertTrue(result is IO.Result.Failure)
        assertEquals("checked error", (result as IO.Result.Failure).error.message)
    }

    @Test
    fun testLaziness() {
        var counter = 0
        val io = IO.of {
            counter++
            42
        }
        
        assertEquals(0, counter) // Not executed yet
        
        io.unsafeRunSync()
        assertEquals(1, counter) // Executed once
        
        io.unsafeRunSync()
        assertEquals(2, counter) // Executed again (not memoized)
    }

    @Test
    fun testSideEffects() {
        val sb = StringBuilder()
        val io = IO.of {
            sb.append("x")
            sb.toString()
        }
        
        assertEquals("x", io.unsafeRunSync())
        assertEquals("xx", io.unsafeRunSync())
        assertEquals("xxx", io.unsafeRunSync())
    }

    @Test
    fun testMapOnFailure() {
        val io = IO.of<Int> {
            throw RuntimeException("initial error")
        }.map { it * 2 }
        
        val exception = assertFailsWith<RuntimeException> {
            io.unsafeRunSync()
        }
        
        assertEquals("initial error", exception.message)
    }

    @Test
    fun testFlatMapOnFailure() {
        val io = IO.of<Int> {
            throw RuntimeException("initial error")
        }.flatMap { x -> IO.of { x * 2 } }
        
        val exception = assertFailsWith<RuntimeException> {
            io.unsafeRunSync()
        }
        
        assertEquals("initial error", exception.message)
    }

    @Test
    fun testResultSuccessType() {
        val result = IO.Result.success("value")
        
        assertTrue(result.isSuccess)
        assertTrue(result is IO.Result.Success)
        assertEquals("value", (result as IO.Result.Success).value)
    }

    @Test
    fun testResultFailureType() {
        val ex = Exception("error")
        val result = IO.Result.failure<String>(ex)
        
        assertTrue(result.isFailure)
        assertTrue(result is IO.Result.Failure)
        assertEquals(ex, (result as IO.Result.Failure).error)
    }

    @Test
    fun testResultIsSuccessProperty() {
        val success = IO.Result.success(42)
        val failure = IO.Result.failure<Int>(Exception())
        
        assertTrue(success.isSuccess)
        assertTrue(failure.isFailure)
    }

    @Test
    fun testComplexWorkflow() {
        val workflow = IO.of { 10 }
            .map { it + 5 }
            .flatMap { x -> IO.of { x * 2 } }
            .map { it - 10 }
        
        assertEquals(20, workflow.unsafeRunSync())
    }

    @Test
    fun testWorkflowWithFailure() {
        val workflow = IO.of { 10 }
            .map { it + 5 }
            .flatMap<Int> { _ -> IO.of { throw RuntimeException("middle failure") } }
            .map { it - 10 }
        
        assertFailsWith<RuntimeException> {
            workflow.unsafeRunSync()
        }
    }
}

