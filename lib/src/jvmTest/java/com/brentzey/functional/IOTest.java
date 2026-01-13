package com.brentzey.functional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class JavaIOTest {

    @Test
    @DisplayName("pure() wraps a value in IO")
    void testPureValue() {
        JavaIO<Integer> io = JavaIO.pure(42);
        assertEquals(42, io.unsafeRunSync());
    }

    @Test
    @DisplayName("of() wraps a computation")
    void testOf() {
        JavaIO<String> io = JavaIO.of(() -> "test");
        assertEquals("test", io.unsafeRunSync());
    }

    @Test
    @DisplayName("map() transforms the result")
    void testMap() {
        JavaIO<Integer> io = JavaIO.of(() -> 10);
        JavaIO<Integer> mapped = io.map(x -> x * 2);
        
        assertEquals(20, mapped.unsafeRunSync());
    }

    @Test
    @DisplayName("map() chains multiple transformations")
    void testMapChaining() {
        JavaIO<Integer> io = JavaIO.of(() -> 5)
            .map(x -> x + 10)
            .map(x -> x * 2);
        
        assertEquals(30, io.unsafeRunSync());
    }

    @Test
    @DisplayName("flatMap() chains another IO operation")
    void testFlatMap() {
        JavaIO<Integer> io = JavaIO.of(() -> 10);
        JavaIO<Integer> result = io.flatMap(x -> JavaIO.of(() -> x * 2));
        
        assertEquals(20, result.unsafeRunSync());
    }

    @Test
    @DisplayName("flatMap() chains multiple IO operations")
    void testFlatMapChaining() {
        JavaIO<Integer> io = JavaIO.of(() -> 5)
            .flatMap(x -> JavaIO.of(() -> x + 10))
            .flatMap(x -> JavaIO.of(() -> x * 2));
        
        assertEquals(30, io.unsafeRunSync());
    }

    @Test
    @DisplayName("runToOptional() returns present on success")
    void testRunToOptionalSuccess() {
        JavaIO<String> io = JavaIO.of(() -> "success");
        Optional<String> result = io.runToOptional();
        
        assertTrue(result.isPresent());
        assertEquals("success", result.get());
    }

    @Test
    @DisplayName("runToOptional() returns empty on exception")
    void testRunToOptionalFailure() {
        JavaIO<String> io = JavaIO.of(() -> {
            throw new RuntimeException("error");
        });
        Optional<String> result = io.runToOptional();
        
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("runToOptional() returns empty on checked exception")
    void testRunToOptionalCheckedException() {
        JavaIO<String> io = JavaIO.of(() -> {
            throw new Exception("checked error");
        });
        Optional<String> result = io.runToOptional();
        
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("runToOptional() handles null values")
    void testRunToOptionalNullValue() {
        JavaIO<String> io = JavaIO.of(() -> null);
        Optional<String> result = io.runToOptional();
        
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("unsafeRunSync() returns value on success")
    void testUnsafeRunSyncSuccess() {
        JavaIO<String> io = JavaIO.of(() -> "result");
        String result = io.unsafeRunSync();
        
        assertEquals("result", result);
    }

    @Test
    @DisplayName("unsafeRunSync() throws RuntimeException on failure")
    void testUnsafeRunSyncRuntimeException() {
        JavaIO<String> io = JavaIO.of(() -> {
            throw new RuntimeException("error");
        });
        
        RuntimeException thrown = assertThrows(
            RuntimeException.class,
            io::unsafeRunSync
        );
        
        assertEquals("error", thrown.getMessage());
    }

    @Test
    @DisplayName("unsafeRunSync() wraps checked exception in IOExecutionException")
    void testUnsafeRunSyncCheckedException() {
        JavaIO<String> io = JavaIO.of(() -> {
            throw new Exception("checked error");
        });
        
        JavaIO.IOExecutionException thrown = assertThrows(
            JavaIO.IOExecutionException.class,
            io::unsafeRunSync
        );
        
        assertNotNull(thrown.getCause());
        assertEquals("checked error", thrown.getCause().getMessage());
    }

    @Test
    @DisplayName("attempt() returns Success on success")
    void testAttemptSuccess() {
        JavaIO<Integer> io = JavaIO.of(() -> 42);
        JavaIO.Result<Integer> result = io.attempt();
        
        assertTrue(result.isSuccess());
        assertEquals(42, result.value());
        assertNull(result.error());
    }

    @Test
    @DisplayName("attempt() returns Failure on exception")
    void testAttemptFailure() {
        JavaIO<Integer> io = JavaIO.of(() -> {
            throw new RuntimeException("error");
        });
        JavaIO.Result<Integer> result = io.attempt();
        
        assertFalse(result.isSuccess());
        assertNull(result.value());
        assertNotNull(result.error());
        assertEquals("error", result.error().getMessage());
    }

    @Test
    @DisplayName("attempt() returns Failure on checked exception")
    void testAttemptCheckedException() {
        JavaIO<Integer> io = JavaIO.of(() -> {
            throw new Exception("checked error");
        });
        JavaIO.Result<Integer> result = io.attempt();
        
        assertFalse(result.isSuccess());
        assertEquals("checked error", result.error().getMessage());
    }

    @Test
    @DisplayName("IO is lazy - effect not executed on creation")
    void testLaziness() {
        final int[] counter = {0};
        JavaIO<Integer> io = JavaIO.of(() -> {
            counter[0]++;
            return 42;
        });
        
        assertEquals(0, counter[0]); // Not executed yet
        
        io.unsafeRunSync();
        assertEquals(1, counter[0]); // Executed once
        
        io.unsafeRunSync();
        assertEquals(2, counter[0]); // Executed again (not memoized)
    }

    @Test
    @DisplayName("IO with side effects executes each time")
    void testSideEffects() {
        final StringBuilder sb = new StringBuilder();
        JavaIO<String> io = JavaIO.of(() -> {
            sb.append("x");
            return sb.toString();
        });
        
        assertEquals("x", io.unsafeRunSync());
        assertEquals("xx", io.unsafeRunSync());
        assertEquals("xxx", io.unsafeRunSync());
    }

    @Test
    @DisplayName("map() on failed IO propagates error")
    void testMapOnFailure() {
        JavaIO<Integer> io = JavaIO.<Integer>of(() -> {
            throw new RuntimeException("initial error");
        }).map(x -> x * 2);
        
        RuntimeException thrown = assertThrows(
            RuntimeException.class,
            io::unsafeRunSync
        );
        
        assertEquals("initial error", thrown.getMessage());
    }

    @Test
    @DisplayName("flatMap() on failed IO propagates error")
    void testFlatMapOnFailure() {
        JavaIO<Integer> io = JavaIO.<Integer>of(() -> {
            throw new RuntimeException("initial error");
        }).flatMap(x -> JavaIO.of(() -> x * 2));
        
        RuntimeException thrown = assertThrows(
            RuntimeException.class,
            io::unsafeRunSync
        );
        
        assertEquals("initial error", thrown.getMessage());
    }

    @Test
    @DisplayName("Result.success() creates success result")
    void testResultSuccess() {
        JavaIO.Result<String> result = JavaIO.Result.success("value");
        
        assertTrue(result.isSuccess());
        assertEquals("value", result.value());
        assertNull(result.error());
    }

    @Test
    @DisplayName("Result.failure() creates failure result")
    void testResultFailure() {
        Exception ex = new Exception("error");
        JavaIO.Result<String> result = JavaIO.Result.failure(ex);
        
        assertFalse(result.isSuccess());
        assertNull(result.value());
        assertEquals(ex, result.error());
    }

    @Test
    @DisplayName("CheckedSupplier can throw exceptions")
    void testCheckedSupplier() {
        JavaIO.CheckedSupplier<String> supplier = () -> {
            if (Math.random() < 0) { // Always false, but checked at compile time
                throw new Exception("test");
            }
            return "ok";
        };
        
        assertDoesNotThrow(() -> supplier.get());
    }
}

