package io.github.functional;

import java.util.function.Function;
import java.util.Optional;

/**
 * IO Monad for lazy, composable side effects - Java API.
 * Use this from Java code for type-safe checked exception handling.
 */
public final class JavaIO<T> {

    private final CheckedSupplier<T> effect;

    private JavaIO(CheckedSupplier<T> effect) {
        this.effect = effect;
    }

    public static <T> JavaIO<T> of(CheckedSupplier<T> effect) {
        return new JavaIO<>(effect);
    }

    public static <T> JavaIO<T> pure(T value) {
        return new JavaIO<>(() -> value);
    }

    public <R> JavaIO<R> map(Function<T, R> mapper) {
        return new JavaIO<>(() -> mapper.apply(this.effect.get()));
    }

    public <R> JavaIO<R> flatMap(Function<T, JavaIO<R>> mapper) {
        return new JavaIO<>(() -> mapper.apply(this.effect.get()).unsafeRunSync());
    }

    /**
     * Runs the effect. If it fails, returns Optional.empty() and logs error.
     * Great for "fire and forget" or when you don't care about the error reason.
     */
    public Optional<T> runToOptional() {
        try {
            return Optional.ofNullable(effect.get());
        } catch (Exception e) {
            System.err.println("IO Error: " + e.getMessage()); 
            return Optional.empty();
        }
    }

    /**
     * Runs the effect. Throws RuntimeException if it fails.
     * Equivalent to Scala IO.unsafeRunSync()
     */
    public T unsafeRunSync() {
        try {
            return effect.get();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IOExecutionException("IO execution failed", e);
        }
    }
    
    /**
     * Runs the effect and returns a simplified Try-like structure
     */
    public Result<T> attempt() {
        try {
            return Result.success(effect.get());
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get() throws Exception;
    }
    
    public static final class Result<T> {
        private final T value;
        private final Exception error;
        private final boolean isSuccess;
        
        private Result(T value, Exception error, boolean isSuccess) {
            this.value = value;
            this.error = error;
            this.isSuccess = isSuccess;
        }
        
        public T value() { return value; }
        public Exception error() { return error; }
        public boolean isSuccess() { return isSuccess; }
        
        public static <T> Result<T> success(T val) { return new Result<>(val, null, true); }
        public static <T> Result<T> failure(Exception e) { return new Result<>(null, e, false); }
    }
    
    /**
     * Exception thrown when IO execution fails with a checked exception
     */
    public static final class IOExecutionException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        
        public IOExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
