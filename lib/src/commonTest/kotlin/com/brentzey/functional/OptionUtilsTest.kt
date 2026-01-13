package com.brentzey.functional

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class OptionUtilsTest {

    @Test
    fun testZipTwoPresent() {
        val result = OptionUtils.zip(2, 3) { a, b -> a + b }
        assertEquals(5, result)
    }

    @Test
    fun testZipFirstNull() {
        val result = OptionUtils.zip<Int, Int, Int>(null, 3) { a, b -> a + b }
        assertNull(result)
    }

    @Test
    fun testZipSecondNull() {
        val result = OptionUtils.zip<Int, Int, Int>(2, null) { a, b -> a + b }
        assertNull(result)
    }

    @Test
    fun testZipBothNull() {
        val result = OptionUtils.zip<Int, Int, Int>(null, null) { a, b -> a + b }
        assertNull(result)
    }

    @Test
    fun testZipDifferentTypes() {
        val result = OptionUtils.zip("Count: ", 42) { str, num -> "$str$num" }
        assertEquals("Count: 42", result)
    }

    @Test
    fun testZipThreePresent() {
        val result = OptionUtils.zip(1, 2, 3) { a, b, c -> a + b + c }
        assertEquals(6, result)
    }

    @Test
    fun testZipThreeFirstNull() {
        val result = OptionUtils.zip<Int, Int, Int, Int>(null, 2, 3) { a, b, c -> a + b + c }
        assertNull(result)
    }

    @Test
    fun testZipThreeSecondNull() {
        val result = OptionUtils.zip<Int, Int, Int, Int>(1, null, 3) { a, b, c -> a + b + c }
        assertNull(result)
    }

    @Test
    fun testZipThreeThirdNull() {
        val result = OptionUtils.zip<Int, Int, Int, Int>(1, 2, null) { a, b, c -> a + b + c }
        assertNull(result)
    }

    @Test
    fun testZipThreeAllNull() {
        val result = OptionUtils.zip<Int, Int, Int, Int>(null, null, null) { a, b, c -> a + b + c }
        assertNull(result)
    }

    @Test
    fun testSequenceAllPresent() {
        val values = listOf("a", "b", "c")
        val result = OptionUtils.sequence(values)
        
        assertNotNull(result)
        assertEquals(listOf("a", "b", "c"), result)
    }

    @Test
    fun testSequenceFirstNull() {
        val values = listOf(null, "b", "c")
        val result = OptionUtils.sequence(values)
        
        assertNull(result)
    }

    @Test
    fun testSequenceMiddleNull() {
        val values = listOf("a", null, "c")
        val result = OptionUtils.sequence(values)
        
        assertNull(result)
    }

    @Test
    fun testSequenceLastNull() {
        val values = listOf("a", "b", null)
        val result = OptionUtils.sequence(values)
        
        assertNull(result)
    }

    @Test
    fun testSequenceEmptyList() {
        val values = emptyList<String?>()
        val result = OptionUtils.sequence(values)
        
        assertNotNull(result)
        assertEquals(emptyList(), result)
    }

    @Test
    fun testSequenceSinglePresent() {
        val values = listOf("only")
        val result = OptionUtils.sequence(values)
        
        assertNotNull(result)
        assertEquals(listOf("only"), result)
    }

    @Test
    fun testSequenceSingleNull() {
        val values = listOf<String?>(null)
        val result = OptionUtils.sequence(values)
        
        assertNull(result)
    }

    @Test
    fun testFoldPresent() {
        val result = OptionUtils.fold(
            42,
            ifNull = { "empty" },
            ifNotNull = { "value: $it" }
        )
        
        assertEquals("value: 42", result)
    }

    @Test
    fun testFoldNull() {
        val result = OptionUtils.fold<Int, String>(
            null,
            ifNull = { "empty" },
            ifNotNull = { "value: $it" }
        )
        
        assertEquals("empty", result)
    }

    @Test
    fun testFoldDifferentTypes() {
        val result = OptionUtils.fold(
            "test",
            ifNull = { 0 },
            ifNotNull = { it.length }
        )
        
        assertEquals(4, result)
    }

    @Test
    fun testFoldNullDifferentTypes() {
        val result = OptionUtils.fold<String, Int>(
            null,
            ifNull = { -1 },
            ifNotNull = { it.length }
        )
        
        assertEquals(-1, result)
    }
}

