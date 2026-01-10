package io.github.jvmfunctionalutils

/**
 * JVM-specific functional utilities with Java interoperability
 */
object FunctionalUtils {
    @JvmStatic
    fun greetJava(name: String): String = greet(name)
    
    @JvmStatic
    fun <A, B> apply(value: A, function: (A) -> B): B = function(value)
}
