package com.brentzey.functional

import kotlin.test.Test
import kotlin.test.assertEquals

class JvmLibraryTest {
    @Test
    fun testJavaInterop() {
        assertEquals("Hello, Java!", FunctionalUtils.greetJava("Java"))
    }
    
    @Test
    fun testApply() {
        val result = FunctionalUtils.apply(10) { it * 2 }
        assertEquals(20, result)
    }
}
