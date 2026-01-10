package io.github.functional;

import java.util.*;
import java.util.function.*;

public class OptionalUtils {

    /**
     * Combines two Optionals. The function is only applied if BOTH are present.
     * Scala equivalent: for { a <- oa; b <- ob } yield f(a, b)
     */
    public static <A, B, R> Optional<R> zip(Optional<A> oa, Optional<B> ob, BiFunction<A, B, R> combiner) {
        return oa.flatMap(a -> ob.map(b -> combiner.apply(a, b)));
    }

    /**
     * Combines three Optionals.
     */
    public static <A, B, C, R> Optional<R> zip(Optional<A> oa, Optional<B> ob, Optional<C> oc, TriFunction<A, B, C, R> combiner) {
        return oa.flatMap(a -> 
               ob.flatMap(b -> 
               oc.map(c -> combiner.apply(a, b, c))));
    }

    /**
     * Converts a List of Optionals into an Optional List.
     * If ANY element is empty, the whole result is Empty.
     * Useful for: "I need all these database lookups to succeed, or I fail the whole request."
     */
    public static <T> Optional<List<T>> sequence(Collection<Optional<T>> optionals) {
        List<T> result = new ArrayList<>(optionals.size());
        for (Optional<T> opt : optionals) {
            if (opt.isEmpty()) return Optional.empty();
            result.add(opt.get());
        }
        return Optional.of(result);
    }

    /**
     * Functional "if-else" that returns a value (Java 9+ has ifPresentOrElse, but that returns void).
     */
    public static <T, R> R fold(Optional<T> opt, Supplier<R> ifEmpty, Function<T, R> ifPresent) {
        return opt.map(ifPresent).orElseGet(ifEmpty);
    }

    @FunctionalInterface
    public interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }
}
