package java.util;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public final class OptionalDouble {
    private static final OptionalDouble EMPTY = new OptionalDouble();
    private final boolean isPresent;
    private final double value;

    private OptionalDouble() {
        this.isPresent = false;
        this.value = Double.NaN;
    }

    public static OptionalDouble empty() {
        return EMPTY;
    }

    private OptionalDouble(double value) {
        this.isPresent = true;
        this.value = value;
    }

    public static OptionalDouble of(double value) {
        return new OptionalDouble(value);
    }

    public double getAsDouble() {
        if (this.isPresent) {
            return this.value;
        }
        throw new NoSuchElementException("No value present");
    }

    public boolean isPresent() {
        return this.isPresent;
    }

    public void ifPresent(DoubleConsumer consumer) {
        if (this.isPresent) {
            consumer.accept(this.value);
        }
    }

    public double orElse(double other) {
        return this.isPresent ? this.value : other;
    }

    public double orElseGet(DoubleSupplier other) {
        return this.isPresent ? this.value : other.getAsDouble();
    }

    public <X extends Throwable> double orElseThrow(Supplier<X> exceptionSupplier) throws Throwable {
        if (this.isPresent) {
            return this.value;
        }
        throw ((Throwable) exceptionSupplier.get());
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OptionalDouble)) {
            return false;
        }
        OptionalDouble other = (OptionalDouble) obj;
        if (this.isPresent && other.isPresent) {
            if (Double.compare(this.value, other.value) != 0) {
                z = false;
            }
        } else if (this.isPresent != other.isPresent) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return this.isPresent ? Double.hashCode(this.value) : 0;
    }

    public String toString() {
        if (!this.isPresent) {
            return "OptionalDouble.empty";
        }
        return String.format("OptionalDouble[%s]", Double.valueOf(this.value));
    }
}
