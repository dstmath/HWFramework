package org.hamcrest;

public abstract class Condition<T> {
    public static final NotMatched<Object> NOT_MATCHED = new NotMatched();

    private static final class Matched<T> extends Condition<T> {
        private final Description mismatch;
        private final T theValue;

        /* synthetic */ Matched(Object theValue, Description mismatch, Matched -this2) {
            this(theValue, mismatch);
        }

        private Matched(T theValue, Description mismatch) {
            super();
            this.theValue = theValue;
            this.mismatch = mismatch;
        }

        public boolean matching(Matcher<T> matcher, String message) {
            if (matcher.matches(this.theValue)) {
                return true;
            }
            this.mismatch.appendText(message);
            matcher.describeMismatch(this.theValue, this.mismatch);
            return false;
        }

        public <U> Condition<U> and(Step<? super T, U> next) {
            return next.apply(this.theValue, this.mismatch);
        }
    }

    private static final class NotMatched<T> extends Condition<T> {
        /* synthetic */ NotMatched(NotMatched -this0) {
            this();
        }

        private NotMatched() {
            super();
        }

        public boolean matching(Matcher<T> matcher, String message) {
            return false;
        }

        public <U> Condition<U> and(Step<? super T, U> step) {
            return Condition.notMatched();
        }
    }

    public interface Step<I, O> {
        Condition<O> apply(I i, Description description);
    }

    /* synthetic */ Condition(Condition -this0) {
        this();
    }

    public abstract <U> Condition<U> and(Step<? super T, U> step);

    public abstract boolean matching(Matcher<T> matcher, String str);

    private Condition() {
    }

    public final boolean matching(Matcher<T> match) {
        return matching(match, "");
    }

    public final <U> Condition<U> then(Step<? super T, U> mapping) {
        return and(mapping);
    }

    public static <T> Condition<T> notMatched() {
        return NOT_MATCHED;
    }

    public static <T> Condition<T> matched(T theValue, Description mismatch) {
        return new Matched(theValue, mismatch, null);
    }
}
