package com.brentzey.functional

import kotlin.test.Test
import kotlin.test.assertEquals

class LibraryTest {
    @Test
    fun testGreet() {
        assertEquals("Hello, World!", greet("World"))
    }
    
    @Test
    fun testCompose() {
        val addOne = { x: Int -> x + 1 }
        val double = { x: Int -> x * 2 }
        val composed = compose(double, addOne)
        assertEquals(4, composed(1))
    }
    
    @Test
    fun testPipe() {
        val result = 5 pipe { it * 2 } pipe { it + 1 }
        assertEquals(11, result)
    }
}
