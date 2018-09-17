package java.util;

import java.util.-$Lambda$fWIoPSZ-61hz8SM5DJYpXGYqc5o.AnonymousClass1;
import java.util.-$Lambda$fWIoPSZ-61hz8SM5DJYpXGYqc5o.AnonymousClass2;
import java.util.-$Lambda$fWIoPSZ-61hz8SM5DJYpXGYqc5o.AnonymousClass3;
import java.util.-$Lambda$fWIoPSZ-61hz8SM5DJYpXGYqc5o.AnonymousClass4;
import java.util.-$Lambda$fWIoPSZ-61hz8SM5DJYpXGYqc5o.AnonymousClass5;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public interface Spliterator<T> {
    public static final int CONCURRENT = 4096;
    public static final int DISTINCT = 1;
    public static final int IMMUTABLE = 1024;
    public static final int NONNULL = 256;
    public static final int ORDERED = 16;
    public static final int SIZED = 64;
    public static final int SORTED = 4;
    public static final int SUBSIZED = 16384;

    public interface OfPrimitive<T, T_CONS, T_SPLITR extends OfPrimitive<T, T_CONS, T_SPLITR>> extends Spliterator<T> {
        boolean tryAdvance(T_CONS t_cons);

        T_SPLITR trySplit();

        void forEachRemaining(T_CONS action) {
            do {
            } while (tryAdvance(action));
        }
    }

    public interface OfInt extends OfPrimitive<Integer, IntConsumer, OfInt> {
        boolean tryAdvance(IntConsumer intConsumer);

        OfInt trySplit();

        void forEachRemaining(IntConsumer action) {
            do {
            } while (tryAdvance(action));
        }

        boolean tryAdvance(Consumer<? super Integer> action) {
            if (action instanceof IntConsumer) {
                return tryAdvance((IntConsumer) action);
            }
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling Spliterator.OfInt.tryAdvance((IntConsumer) action::accept)");
            }
            action.getClass();
            return tryAdvance(new AnonymousClass3(action));
        }

        void forEachRemaining(Consumer<? super Integer> action) {
            if (action instanceof IntConsumer) {
                forEachRemaining((IntConsumer) action);
                return;
            }
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling Spliterator.OfInt.forEachRemaining((IntConsumer) action::accept)");
            }
            action.getClass();
            forEachRemaining(new AnonymousClass2(action));
        }
    }

    public interface OfDouble extends OfPrimitive<Double, DoubleConsumer, OfDouble> {
        boolean tryAdvance(DoubleConsumer doubleConsumer);

        OfDouble trySplit();

        void forEachRemaining(DoubleConsumer action) {
            do {
            } while (tryAdvance(action));
        }

        boolean tryAdvance(Consumer<? super Double> action) {
            if (action instanceof DoubleConsumer) {
                return tryAdvance((DoubleConsumer) action);
            }
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling Spliterator.OfDouble.tryAdvance((DoubleConsumer) action::accept)");
            }
            action.getClass();
            return tryAdvance(new AnonymousClass1(action));
        }

        void forEachRemaining(Consumer<? super Double> action) {
            if (action instanceof DoubleConsumer) {
                forEachRemaining((DoubleConsumer) action);
                return;
            }
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling Spliterator.OfDouble.forEachRemaining((DoubleConsumer) action::accept)");
            }
            action.getClass();
            forEachRemaining(new -$Lambda$fWIoPSZ-61hz8SM5DJYpXGYqc5o(action));
        }
    }

    public interface OfLong extends OfPrimitive<Long, LongConsumer, OfLong> {
        boolean tryAdvance(LongConsumer longConsumer);

        OfLong trySplit();

        void forEachRemaining(LongConsumer action) {
            do {
            } while (tryAdvance(action));
        }

        boolean tryAdvance(Consumer<? super Long> action) {
            if (action instanceof LongConsumer) {
                return tryAdvance((LongConsumer) action);
            }
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling Spliterator.OfLong.tryAdvance((LongConsumer) action::accept)");
            }
            action.getClass();
            return tryAdvance(new AnonymousClass5(action));
        }

        void forEachRemaining(Consumer<? super Long> action) {
            if (action instanceof LongConsumer) {
                forEachRemaining((LongConsumer) action);
                return;
            }
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling Spliterator.OfLong.forEachRemaining((LongConsumer) action::accept)");
            }
            action.getClass();
            forEachRemaining(new AnonymousClass4(action));
        }
    }

    int characteristics();

    long estimateSize();

    boolean tryAdvance(Consumer<? super T> consumer);

    Spliterator<T> trySplit();

    void forEachRemaining(Consumer<? super T> action) {
        do {
        } while (tryAdvance(action));
    }

    long getExactSizeIfKnown() {
        return (characteristics() & 64) == 0 ? -1 : estimateSize();
    }

    boolean hasCharacteristics(int characteristics) {
        return (characteristics() & characteristics) == characteristics;
    }

    Comparator<? super T> getComparator() {
        throw new IllegalStateException();
    }
}
