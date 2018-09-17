package java.util.concurrent.atomic;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
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

        SerializationProxy(double value, DoubleBinaryOperator function, long identity) {
            this.value = value;
            this.function = function;
            this.identity = identity;
        }

        private Object readResolve() {
            DoubleAccumulator a = new DoubleAccumulator(this.function, Double.longBitsToDouble(this.identity));
            a.base = Double.doubleToRawLongBits(this.value);
            return a;
        }
    }

    public DoubleAccumulator(DoubleBinaryOperator accumulatorFunction, double identity) {
        this.function = accumulatorFunction;
        long doubleToRawLongBits = Double.doubleToRawLongBits(identity);
        this.identity = doubleToRawLongBits;
        this.base = doubleToRawLongBits;
    }

    public void accumulate(double x) {
        DoubleBinaryOperator doubleBinaryOperator;
        long r;
        Cell[] as = this.cells;
        if (as == null) {
            doubleBinaryOperator = this.function;
            long b = this.base;
            r = Double.doubleToRawLongBits(doubleBinaryOperator.applyAsDouble(Double.longBitsToDouble(b), x));
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
                    doubleBinaryOperator = this.function;
                    long v = a.value;
                    r = Double.doubleToRawLongBits(doubleBinaryOperator.applyAsDouble(Double.longBitsToDouble(v), x));
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
        doubleAccumulate(x, this.function, uncontended);
    }

    public double get() {
        Cell[] as = this.cells;
        double result = Double.longBitsToDouble(this.base);
        if (as != null) {
            for (Cell a : as) {
                if (a != null) {
                    result = this.function.applyAsDouble(result, Double.longBitsToDouble(a.value));
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

    public double getThenReset() {
        Cell[] as = this.cells;
        double result = Double.longBitsToDouble(this.base);
        this.base = this.identity;
        if (as != null) {
            for (Cell a : as) {
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
        return new SerializationProxy(get(), this.function, this.identity);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
}
