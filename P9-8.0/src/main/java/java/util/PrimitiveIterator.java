package java.util;

import java.util.-$Lambda$0ZvtycDcLIMOjCTCE-AzwhVMR0g.AnonymousClass1;
import java.util.-$Lambda$0ZvtycDcLIMOjCTCE-AzwhVMR0g.AnonymousClass2;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public interface PrimitiveIterator<T, T_CONS> extends Iterator<T> {

    public interface OfInt extends PrimitiveIterator<Integer, IntConsumer> {
        int nextInt();

        void forEachRemaining(IntConsumer action) {
            Objects.requireNonNull(action);
            while (hasNext()) {
                action.accept(nextInt());
            }
        }

        Integer next() {
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling PrimitiveIterator.OfInt.nextInt()");
            }
            return Integer.valueOf(nextInt());
        }

        void forEachRemaining(Consumer<? super Integer> action) {
            if (action instanceof IntConsumer) {
                forEachRemaining((IntConsumer) action);
                return;
            }
            Objects.requireNonNull(action);
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling PrimitiveIterator.OfInt.forEachRemainingInt(action::accept)");
            }
            action.getClass();
            forEachRemaining(new AnonymousClass1(action));
        }
    }

    public interface OfDouble extends PrimitiveIterator<Double, DoubleConsumer> {
        double nextDouble();

        void forEachRemaining(DoubleConsumer action) {
            Objects.requireNonNull(action);
            while (hasNext()) {
                action.accept(nextDouble());
            }
        }

        Double next() {
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling PrimitiveIterator.OfDouble.nextLong()");
            }
            return Double.valueOf(nextDouble());
        }

        void forEachRemaining(Consumer<? super Double> action) {
            if (action instanceof DoubleConsumer) {
                forEachRemaining((DoubleConsumer) action);
                return;
            }
            Objects.requireNonNull(action);
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling PrimitiveIterator.OfDouble.forEachRemainingDouble(action::accept)");
            }
            action.getClass();
            forEachRemaining(new -$Lambda$0ZvtycDcLIMOjCTCE-AzwhVMR0g(action));
        }
    }

    public interface OfLong extends PrimitiveIterator<Long, LongConsumer> {
        long nextLong();

        void forEachRemaining(LongConsumer action) {
            Objects.requireNonNull(action);
            while (hasNext()) {
                action.accept(nextLong());
            }
        }

        Long next() {
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling PrimitiveIterator.OfLong.nextLong()");
            }
            return Long.valueOf(nextLong());
        }

        void forEachRemaining(Consumer<? super Long> action) {
            if (action instanceof LongConsumer) {
                forEachRemaining((LongConsumer) action);
                return;
            }
            Objects.requireNonNull(action);
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling PrimitiveIterator.OfLong.forEachRemainingLong(action::accept)");
            }
            action.getClass();
            forEachRemaining(new AnonymousClass2(action));
        }
    }

    void forEachRemaining(T_CONS t_cons);
}
