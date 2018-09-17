package com.android.internal.util;

import java.util.Arrays;

public class Predicates {

    private static class AndPredicate<T> implements Predicate<T> {
        private final Iterable<? extends Predicate<? super T>> components;

        /* synthetic */ AndPredicate(Iterable components, AndPredicate -this1) {
            this(components);
        }

        private AndPredicate(Iterable<? extends Predicate<? super T>> components) {
            this.components = components;
        }

        public boolean apply(T t) {
            for (Predicate<? super T> predicate : this.components) {
                if (!predicate.apply(t)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static class NotPredicate<T> implements Predicate<T> {
        private final Predicate<? super T> predicate;

        /* synthetic */ NotPredicate(Predicate predicate, NotPredicate -this1) {
            this(predicate);
        }

        private NotPredicate(Predicate<? super T> predicate) {
            this.predicate = predicate;
        }

        public boolean apply(T t) {
            return this.predicate.apply(t) ^ 1;
        }
    }

    private static class OrPredicate<T> implements Predicate<T> {
        private final Iterable<? extends Predicate<? super T>> components;

        /* synthetic */ OrPredicate(Iterable components, OrPredicate -this1) {
            this(components);
        }

        private OrPredicate(Iterable<? extends Predicate<? super T>> components) {
            this.components = components;
        }

        public boolean apply(T t) {
            for (Predicate<? super T> predicate : this.components) {
                if (predicate.apply(t)) {
                    return true;
                }
            }
            return false;
        }
    }

    private Predicates() {
    }

    public static <T> Predicate<T> and(Predicate<? super T>... components) {
        return and(Arrays.asList(components));
    }

    public static <T> Predicate<T> and(Iterable<? extends Predicate<? super T>> components) {
        return new AndPredicate(components, null);
    }

    public static <T> Predicate<T> or(Predicate<? super T>... components) {
        return or(Arrays.asList(components));
    }

    public static <T> Predicate<T> or(Iterable<? extends Predicate<? super T>> components) {
        return new OrPredicate(components, null);
    }

    public static <T> Predicate<T> not(Predicate<? super T> predicate) {
        return new NotPredicate(predicate, null);
    }
}
