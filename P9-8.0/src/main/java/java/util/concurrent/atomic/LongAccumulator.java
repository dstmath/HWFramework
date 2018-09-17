package java.util.concurrent.atomic;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.function.LongBinaryOperator;

public class LongAccumulator extends Striped64 implements Serializable {
    private static final long serialVersionUID = 7249069246863182397L;
    private final LongBinaryOperator function;
    private final long identity;

    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 7249069246863182397L;
        private final LongBinaryOperator function;
        private final long identity;
        private final long value;

        SerializationProxy(long value, LongBinaryOperator function, long identity) {
            this.value = value;
            this.function = function;
            this.identity = identity;
        }

        private Object readResolve() {
            LongAccumulator a = new LongAccumulator(this.function, this.identity);
            a.base = this.value;
            return a;
        }
    }

    public LongAccumulator(LongBinaryOperator accumulatorFunction, long identity) {
        this.function = accumulatorFunction;
        this.identity = identity;
        this.base = identity;
    }

    public void accumulate(long x) {
        LongBinaryOperator longBinaryOperator;
        long r;
        Cell[] as = this.cells;
        if (as == null) {
            longBinaryOperator = this.function;
            long b = this.base;
            r = longBinaryOperator.applyAsLong(b, x);
            if (r == b || (casBase(b, r) ^ 1) == 0) {
                return;
            }
        }
        boolean uncontended = true;
        if (as != null) {
            int m = as.length - 1;
            if (m >= 0) {
                Cell a = as[Striped64.getProbe() & m];
                if (a != null) {
                    longBinaryOperator = this.function;
                    long v = a.value;
                    r = longBinaryOperator.applyAsLong(v, x);
                    if (r != v) {
                        uncontended = a.cas(v, r);
                    } else {
                        uncontended = true;
                    }
                    if ((uncontended ^ 1) == 0) {
                        return;
                    }
                }
            }
        }
        longAccumulate(x, this.function, uncontended);
    }

    public long get() {
        Cell[] as = this.cells;
        long result = this.base;
        if (as != null) {
            for (Cell a : as) {
                if (a != null) {
                    result = this.function.applyAsLong(result, a.value);
                }
            }
        }
        return result;
    }

    public void reset() {
        Cell[] as = this.cells;
        this.base = this.identity;
        if (as != null) {
            for (Cell a : as) {
                if (a != null) {
                    a.reset(this.identity);
                }
            }
        }
    }

    public long getThenReset() {
        Cell[] as = this.cells;
        long result = this.base;
        this.base = this.identity;
        if (as != null) {
            for (Cell a : as) {
                if (a != null) {
                    long v = a.value;
                    a.reset(this.identity);
                    result = this.function.applyAsLong(result, v);
                }
            }
        }
        return result;
    }

    public String toString() {
        return Long.toString(get());
    }

    public long longValue() {
        return get();
    }

    public int intValue() {
        return (int) get();
    }

    public float floatValue() {
        return (float) get();
    }

    public double doubleValue() {
        return (double) get();
    }

    private Object writeReplace() {
        return new SerializationProxy(get(), this.function, this.identity);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
}
