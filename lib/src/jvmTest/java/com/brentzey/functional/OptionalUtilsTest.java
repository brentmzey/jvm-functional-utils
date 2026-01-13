package com.brentzey.functional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class OptionalUtilsTest {

    @Test
    @DisplayName("zip() with two present optionals returns combined result")
    void testZipTwoPresent() {
        Optional<Integer> a = Optional.of(2);
        Optional<Integer> b = Optional.of(3);
        Optional<Integer> result = OptionalUtils.zip(a, b, (x, y) -> x + y);
        
        assertTrue(result.isPresent());
        assertEquals(5, result.get());
    }

    @Test
    @DisplayName("zip() with first empty optional returns empty")
    void testZipFirstEmpty() {
        Optional<Integer> a = Optional.empty();
        Optional<Integer> b = Optional.of(3);
        Optional<Integer> result = OptionalUtils.zip(a, b, (x, y) -> x + y);
        
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("zip() with second empty optional returns empty")
    void testZipSecondEmpty() {
        Optional<Integer> a = Optional.of(2);
        Optional<Integer> b = Optional.empty();
        Optional<Integer> result = OptionalUtils.zip(a, b, (x, y) -> x + y);
        
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("zip() with both empty optionals returns empty")
    void testZipBothEmpty() {
        Optional<Integer> a = Optional.empty();
        Optional<Integer> b = Optional.empty();
        Optional<Integer> result = OptionalUtils.zip(a, b, (x, y) -> x + y);
        
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("zip() with different types returns correctly typed result")
    void testZipDifferentTypes() {
        Optional<String> str = Optional.of("Count: ");
        Optional<Integer> num = Optional.of(42);
        Optional<String> result = OptionalUtils.zip(str, num, (s, n) -> s + n);
        
        assertTrue(result.isPresent());
        assertEquals("Count: 42", result.get());
    }

    @Test
    @DisplayName("zip() with three present optionals returns combined result")
    void testZipThreePresent() {
        Optional<Integer> a = Optional.of(1);
        Optional<Integer> b = Optional.of(2);
        Optional<Integer> c = Optional.of(3);
        Optional<Integer> result = OptionalUtils.zip(a, b, c, (x, y, z) -> x + y + z);
        
        assertTrue(result.isPresent());
        assertEquals(6, result.get());
    }

    @Test
    @DisplayName("zip() with three optionals returns empty if first is empty")
    void testZipThreeFirstEmpty() {
        Optional<Integer> a = Optional.empty();
        Optional<Integer> b = Optional.of(2);
        Optional<Integer> c = Optional.of(3);
        Optional<Integer> result = OptionalUtils.zip(a, b, c, (x, y, z) -> x + y + z);
        
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("zip() with three optionals returns empty if second is empty")
    void testZipThreeSecondEmpty() {
        Optional<Integer> a = Optional.of(1);
        Optional<Integer> b = Optional.empty();
        Optional<Integer> c = Optional.of(3);
        Optional<Integer> result = OptionalUtils.zip(a, b, c, (x, y, z) -> x + y + z);
        
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("zip() with three optionals returns empty if third is empty")
    void testZipThreeThirdEmpty() {
        Optional<Integer> a = Optional.of(1);
        Optional<Integer> b = Optional.of(2);
        Optional<Integer> c = Optional.empty();
        Optional<Integer> result = OptionalUtils.zip(a, b, c, (x, y, z) -> x + y + z);
        
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("sequence() with all present optionals returns list")
    void testSequenceAllPresent() {
        List<Optional<String>> optionals = List.of(
            Optional.of("a"),
            Optional.of("b"),
            Optional.of("c")
        );
        
        Optional<List<String>> result = OptionalUtils.sequence(optionals);
        
        assertTrue(result.isPresent());
        assertEquals(List.of("a", "b", "c"), result.get());
    }

    @Test
    @DisplayName("sequence() with empty optional at start returns empty")
    void testSequenceEmptyAtStart() {
        List<Optional<String>> optionals = List.of(
            Optional.empty(),
            Optional.of("b"),
            Optional.of("c")
        );
        
        Optional<List<String>> result = OptionalUtils.sequence(optionals);
        
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("sequence() with empty optional in middle returns empty")
    void testSequenceEmptyInMiddle() {
        List<Optional<String>> optionals = List.of(
            Optional.of("a"),
            Optional.empty(),
            Optional.of("c")
        );
        
        Optional<List<String>> result = OptionalUtils.sequence(optionals);
        
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("sequence() with empty optional at end returns empty")
    void testSequenceEmptyAtEnd() {
        List<Optional<String>> optionals = List.of(
            Optional.of("a"),
            Optional.of("b"),
            Optional.empty()
        );
        
        Optional<List<String>> result = OptionalUtils.sequence(optionals);
        
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("sequence() with empty list returns empty list")
    void testSequenceEmptyList() {
        List<Optional<String>> optionals = List.of();
        
        Optional<List<String>> result = OptionalUtils.sequence(optionals);
        
        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());
    }

    @Test
    @DisplayName("sequence() with single present optional returns single-element list")
    void testSequenceSinglePresent() {
        List<Optional<String>> optionals = List.of(Optional.of("only"));
        
        Optional<List<String>> result = OptionalUtils.sequence(optionals);
        
        assertTrue(result.isPresent());
        assertEquals(List.of("only"), result.get());
    }

    @Test
    @DisplayName("sequence() with single empty optional returns empty")
    void testSequenceSingleEmpty() {
        List<Optional<String>> optionals = List.of(Optional.empty());
        
        Optional<List<String>> result = OptionalUtils.sequence(optionals);
        
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("fold() with present optional calls present function")
    void testFoldPresent() {
        Optional<Integer> opt = Optional.of(42);
        String result = OptionalUtils.fold(
            opt,
            () -> "empty",
            x -> "value: " + x
        );
        
        assertEquals("value: 42", result);
    }

    @Test
    @DisplayName("fold() with empty optional calls empty supplier")
    void testFoldEmpty() {
        Optional<Integer> opt = Optional.empty();
        String result = OptionalUtils.fold(
            opt,
            () -> "empty",
            x -> "value: " + x
        );
        
        assertEquals("empty", result);
    }

    @Test
    @DisplayName("fold() with present optional and different return type")
    void testFoldDifferentTypes() {
        Optional<String> opt = Optional.of("test");
        Integer result = OptionalUtils.fold(
            opt,
            () -> 0,
            String::length
        );
        
        assertEquals(4, result);
    }

    @Test
    @DisplayName("fold() with empty optional and different return type")
    void testFoldEmptyDifferentTypes() {
        Optional<String> opt = Optional.empty();
        Integer result = OptionalUtils.fold(
            opt,
            () -> -1,
            String::length
        );
        
        assertEquals(-1, result);
    }

    @Test
    @DisplayName("TriFunction interface works correctly")
    void testTriFunction() {
        OptionalUtils.TriFunction<Integer, Integer, Integer, Integer> sum = 
            (a, b, c) -> a + b + c;
        
        assertEquals(10, sum.apply(2, 3, 5));
    }
}

