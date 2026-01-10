package io.github.functional

/**
 * Functional utilities for working with nullable values in a type-safe manner.
 * Kotlin multiplatform equivalent to Scala's Option and Java's Optional utilities.
 */
object OptionUtils {

    /**
     * Combines two nullable values. The function is only applied if BOTH are non-null.
     * Scala equivalent: for { a <- oa; b <- ob } yield f(a, b)
     */
    inline fun <A, B, R> zip(a: A?, b: B?, combiner: (A, B) -> R): R? {
        return if (a != null && b != null) combiner(a, b) else null
    }

    /**
     * Combines three nullable values.
     */
    inline fun <A, B, C, R> zip(a: A?, b: B?, c: C?, combiner: (A, B, C) -> R): R? {
        return if (a != null && b != null && c != null) combiner(a, b, c) else null
    }

    /**
     * Converts a List of nullable values into a nullable List.
     * If ANY element is null, the whole result is null.
     * Useful for: "I need all these database lookups to succeed, or I fail the whole request."
     */
    fun <T> sequence(values: Collection<T?>): List<T>? {
        val result = mutableListOf<T>()
        for (value in values) {
            if (value == null) return null
            result.add(value)
        }
        return result
    }

    /**
     * Functional "if-else" that returns a value.
     */
    inline fun <T, R> fold(value: T?, ifNull: () -> R, ifNotNull: (T) -> R): R {
        return if (value != null) ifNotNull(value) else ifNull()
    }
}
