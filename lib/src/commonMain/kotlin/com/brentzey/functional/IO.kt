package com.brentzey.functional

/**
 * IO Monad for lazy, composable side effects.
 * Multiplatform equivalent to Scala's IO and Cats Effect.
 * 
 * This type represents a lazy computation that may throw an exception.
 * Nothing happens until you call one of the execution methods.
 */
class IO<out T> private constructor(private val effect: () -> T) {

    companion object {
        /**
         * Wraps a computation that may throw an exception.
         */
        fun <T> of(effect: () -> T): IO<T> = IO(effect)

        /**
         * Wraps a pure value (no side effects).
         */
        fun <T> pure(value: T): IO<T> = IO { value }
    }

    /**
     * Transforms the result if successful.
     */
    fun <R> map(mapper: (T) -> R): IO<R> = IO { mapper(effect()) }

    /**
     * Chains another IO operation.
     */
    fun <R> flatMap(mapper: (T) -> IO<R>): IO<R> = IO { mapper(effect()).unsafeRunSync() }

    /**
     * Runs the effect. If it fails, returns null and prints error.
     * Great for "fire and forget" or when you don't care about the error reason.
     */
    fun runToNullable(): T? {
        return try {
            effect()
        } catch (e: Exception) {
            println("IO Error: ${e.message}")
            null
        }
    }

    /**
     * Runs the effect. Throws exception if it fails.
     * Equivalent to Scala IO.unsafeRunSync()
     */
    fun unsafeRunSync(): T = effect()

    /**
     * Runs the effect and returns a Result type
     */
    fun attempt(): Result<T> {
        return try {
            Result.success(effect())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Simple Result type (Either<Error, T>)
     */
    sealed class Result<out T> {
        data class Success<T>(val value: T) : Result<T>()
        data class Failure(val error: Exception) : Result<Nothing>()

        val isSuccess: Boolean get() = this is Success
        val isFailure: Boolean get() = this is Failure

        companion object {
            fun <T> success(value: T): Result<T> = Success(value)
            fun <T> failure(error: Exception): Result<T> = Failure(error)
        }
    }
}
