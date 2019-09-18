package java.util;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class OptionalInt {
    private static final OptionalInt EMPTY = new OptionalInt();
    private final boolean isPresent;
    private final int value;

    private OptionalInt() {
        this.isPresent = false;
        this.value = 0;
    }

    public static OptionalInt empty() {
        return EMPTY;
    }

    private OptionalInt(int value2) {
        this.isPresent = true;
        this.value = value2;
    }

    public static OptionalInt of(int value2) {
        return new OptionalInt(value2);
    }

    public int getAsInt() {
        if (this.isPresent) {
            return this.value;
        }
        throw new NoSuchElementException("No value present");
    }

    public boolean isPresent() {
        return this.isPresent;
    }

    public void ifPresent(IntConsumer consumer) {
        if (this.isPresent) {
            consumer.accept(this.value);
        }
    }

    public int orElse(int other) {
        return this.isPresent ? this.value : other;
    }

    public int orElseGet(IntSupplier other) {
        return this.isPresent ? this.value : other.getAsInt();
    }

    public <X extends Throwable> int orElseThrow(Supplier<X> exceptionSupplier) throws Throwable {
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
        if (!(obj instanceof OptionalInt)) {
            return false;
        }
        OptionalInt other = (OptionalInt) obj;
        if (!this.isPresent || !other.isPresent ? this.isPresent != other.isPresent : this.value != other.value) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        if (this.isPresent) {
            return Integer.hashCode(this.value);
        }
        return 0;
    }

    public String toString() {
        if (!this.isPresent) {
            return "OptionalInt.empty";
        }
        return String.format("OptionalInt[%s]", Integer.valueOf(this.value));
    }
}
