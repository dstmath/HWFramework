package java.util.concurrent.atomic;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.Striped64;
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

        SerializationProxy(long value2, LongBinaryOperator function2, long identity2) {
            this.value = value2;
            this.function = function2;
            this.identity = identity2;
        }

        private Object readResolve() {
            LongAccumulator a = new LongAccumulator(this.function, this.identity);
            a.base = this.value;
            return a;
        }
    }

    public LongAccumulator(LongBinaryOperator accumulatorFunction, long identity2) {
        this.function = accumulatorFunction;
        this.identity = identity2;
        this.base = identity2;
    }

    public void accumulate(long x) {
        Striped64.Cell[] cellArr = this.cells;
        Striped64.Cell[] as = cellArr;
        if (cellArr == null) {
            LongBinaryOperator longBinaryOperator = this.function;
            long j = this.base;
            long b = j;
            long applyAsLong = longBinaryOperator.applyAsLong(j, x);
            long r = applyAsLong;
            if (applyAsLong == b || casBase(b, r)) {
                return;
            }
        }
        boolean uncontended = true;
        if (as != null) {
            boolean z = true;
            int length = as.length - 1;
            int m = length;
            if (length >= 0) {
                Striped64.Cell cell = as[getProbe() & m];
                Striped64.Cell a = cell;
                if (cell != null) {
                    LongBinaryOperator longBinaryOperator2 = this.function;
                    long j2 = a.value;
                    long v = j2;
                    long applyAsLong2 = longBinaryOperator2.applyAsLong(j2, x);
                    long r2 = applyAsLong2;
                    if (applyAsLong2 != v && !a.cas(v, r2)) {
                        z = false;
                    }
                    uncontended = z;
                    if (z) {
                        return;
                    }
                }
            }
        }
        longAccumulate(x, this.function, uncontended);
    }

    public long get() {
        Striped64.Cell[] as = this.cells;
        long result = this.base;
        if (as != null) {
            for (Striped64.Cell a : as) {
                if (a != null) {
                    result = this.function.applyAsLong(result, a.value);
                }
            }
        }
        return result;
    }

    public void reset() {
        Striped64.Cell[] as = this.cells;
        this.base = this.identity;
        if (as != null) {
            for (Striped64.Cell a : as) {
                if (a != null) {
                    a.reset(this.identity);
                }
            }
        }
    }

    public long getThenReset() {
        Striped64.Cell[] as = this.cells;
        long result = this.base;
        this.base = this.identity;
        if (as != null) {
            for (Striped64.Cell a : as) {
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
        SerializationProxy serializationProxy = new SerializationProxy(get(), this.function, this.identity);
        return serializationProxy;
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
}
