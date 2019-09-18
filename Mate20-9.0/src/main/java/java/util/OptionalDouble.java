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

    private OptionalDouble(double value2) {
        this.isPresent = true;
        this.value = value2;
    }

    public static OptionalDouble of(double value2) {
        return new OptionalDouble(value2);
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
        if (!this.isPresent || !other.isPresent ? this.isPresent != other.isPresent : Double.compare(this.value, other.value) != 0) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        if (this.isPresent) {
            return Double.hashCode(this.value);
        }
        return 0;
    }

    public String toString() {
        if (!this.isPresent) {
            return "OptionalDouble.empty";
        }
        return String.format("OptionalDouble[%s]", Double.valueOf(this.value));
    }
}
