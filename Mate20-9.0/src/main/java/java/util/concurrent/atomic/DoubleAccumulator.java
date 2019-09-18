package java.util.concurrent.atomic;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.Striped64;
import java.util.function.DoubleBinaryOperator;

public class DoubleAccumulator extends Striped64 implements Serializable {
    private static final long serialVersionUID = 7249069246863182397L;
    private final DoubleBinaryOperator function;
    private final long identity;

    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 7249069246863182397L;
        private final DoubleBinaryOperator function;
        private final long identity;
        private final double value;

        SerializationProxy(double value2, DoubleBinaryOperator function2, long identity2) {
            this.value = value2;
            this.function = function2;
            this.identity = identity2;
        }

        private Object readResolve() {
            DoubleAccumulator a = new DoubleAccumulator(this.function, Double.longBitsToDouble(this.identity));
            a.base = Double.doubleToRawLongBits(this.value);
            return a;
        }
    }

    public DoubleAccumulator(DoubleBinaryOperator accumulatorFunction, double identity2) {
        this.function = accumulatorFunction;
        long doubleToRawLongBits = Double.doubleToRawLongBits(identity2);
        this.identity = doubleToRawLongBits;
        this.base = doubleToRawLongBits;
    }

    public void accumulate(double x) {
        Striped64.Cell[] cellArr = this.cells;
        Striped64.Cell[] as = cellArr;
        if (cellArr == null) {
            DoubleBinaryOperator doubleBinaryOperator = this.function;
            long j = this.base;
            long b = j;
            long doubleToRawLongBits = Double.doubleToRawLongBits(doubleBinaryOperator.applyAsDouble(Double.longBitsToDouble(j), x));
            long r = doubleToRawLongBits;
            if (doubleToRawLongBits == b || casBase(b, r)) {
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
                    DoubleBinaryOperator doubleBinaryOperator2 = this.function;
                    long j2 = a.value;
                    long v = j2;
                    long doubleToRawLongBits2 = Double.doubleToRawLongBits(doubleBinaryOperator2.applyAsDouble(Double.longBitsToDouble(j2), x));
                    long r2 = doubleToRawLongBits2;
                    if (doubleToRawLongBits2 != v && !a.cas(v, r2)) {
                        z = false;
                    }
                    uncontended = z;
                    if (z) {
                        return;
                    }
                }
            }
        }
        doubleAccumulate(x, this.function, uncontended);
    }

    public double get() {
        Striped64.Cell[] as = this.cells;
        double result = Double.longBitsToDouble(this.base);
        if (as != null) {
            for (Striped64.Cell a : as) {
                if (a != null) {
                    result = this.function.applyAsDouble(result, Double.longBitsToDouble(a.value));
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

    public double getThenReset() {
        Striped64.Cell[] as = this.cells;
        double result = Double.longBitsToDouble(this.base);
        this.base = this.identity;
        if (as != null) {
            for (Striped64.Cell a : as) {
                if (a != null) {
                    double v = Double.longBitsToDouble(a.value);
                    a.reset(this.identity);
                    result = this.function.applyAsDouble(result, v);
                }
            }
        }
        return result;
    }

    public String toString() {
        return Double.toString(get());
    }

    public double doubleValue() {
        return get();
    }

    public long longValue() {
        return (long) get();
    }

    public int intValue() {
        return (int) get();
    }

    public float floatValue() {
        return (float) get();
    }

    private Object writeReplace() {
        SerializationProxy serializationProxy = new SerializationProxy(get(), this.function, this.identity);
        return serializationProxy;
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
}
