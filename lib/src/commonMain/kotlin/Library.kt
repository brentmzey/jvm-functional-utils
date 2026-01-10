package io.github.jvmfunctionalutils

/**
 * A simple functional utility example that works across all platforms.
 */
fun greet(name: String): String {
    return "Hello, $name!"
}

/**
 * Functional composition utility - compose two functions
 */
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C {
    return { x -> f(g(x)) }
}

/**
 * Pipe operator - apply a value to a function
 */
infix fun <A, B> A.pipe(f: (A) -> B): B = f(this)
