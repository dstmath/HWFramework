package java.time.temporal;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.DateTimeException;

public final class ValueRange implements Serializable {
    private static final long serialVersionUID = -7317881728594519368L;
    private final long maxLargest;
    private final long maxSmallest;
    private final long minLargest;
    private final long minSmallest;

    public static ValueRange of(long min, long max) {
        if (min <= max) {
            ValueRange valueRange = new ValueRange(min, min, max, max);
            return valueRange;
        }
        throw new IllegalArgumentException("Minimum value must be less than maximum value");
    }

    public static ValueRange of(long min, long maxSmallest2, long maxLargest2) {
        return of(min, min, maxSmallest2, maxLargest2);
    }

    public static ValueRange of(long minSmallest2, long minLargest2, long maxSmallest2, long maxLargest2) {
        if (minSmallest2 > minLargest2) {
            throw new IllegalArgumentException("Smallest minimum value must be less than largest minimum value");
        } else if (maxSmallest2 > maxLargest2) {
            throw new IllegalArgumentException("Smallest maximum value must be less than largest maximum value");
        } else if (minLargest2 <= maxLargest2) {
            ValueRange valueRange = new ValueRange(minSmallest2, minLargest2, maxSmallest2, maxLargest2);
            return valueRange;
        } else {
            throw new IllegalArgumentException("Minimum value must be less than maximum value");
        }
    }

    private ValueRange(long minSmallest2, long minLargest2, long maxSmallest2, long maxLargest2) {
        this.minSmallest = minSmallest2;
        this.minLargest = minLargest2;
        this.maxSmallest = maxSmallest2;
        this.maxLargest = maxLargest2;
    }

    public boolean isFixed() {
        return this.minSmallest == this.minLargest && this.maxSmallest == this.maxLargest;
    }

    public long getMinimum() {
        return this.minSmallest;
    }

    public long getLargestMinimum() {
        return this.minLargest;
    }

    public long getSmallestMaximum() {
        return this.maxSmallest;
    }

    public long getMaximum() {
        return this.maxLargest;
    }

    public boolean isIntValue() {
        return getMinimum() >= -2147483648L && getMaximum() <= 2147483647L;
    }

    public boolean isValidValue(long value) {
        return value >= getMinimum() && value <= getMaximum();
    }

    public boolean isValidIntValue(long value) {
        return isIntValue() && isValidValue(value);
    }

    public long checkValidValue(long value, TemporalField field) {
        if (isValidValue(value)) {
            return value;
        }
        throw new DateTimeException(genInvalidFieldMessage(field, value));
    }

    public int checkValidIntValue(long value, TemporalField field) {
        if (isValidIntValue(value)) {
            return (int) value;
        }
        throw new DateTimeException(genInvalidFieldMessage(field, value));
    }

    private String genInvalidFieldMessage(TemporalField field, long value) {
        if (field != null) {
            return "Invalid value for " + field + " (valid values " + this + "): " + value;
        }
        return "Invalid value (valid values " + this + "): " + value;
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException, InvalidObjectException {
        s.defaultReadObject();
        if (this.minSmallest > this.minLargest) {
            throw new InvalidObjectException("Smallest minimum value must be less than largest minimum value");
        } else if (this.maxSmallest > this.maxLargest) {
            throw new InvalidObjectException("Smallest maximum value must be less than largest maximum value");
        } else if (this.minLargest > this.maxLargest) {
            throw new InvalidObjectException("Minimum value must be less than maximum value");
        }
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ValueRange)) {
            return false;
        }
        ValueRange other = (ValueRange) obj;
        if (!(this.minSmallest == other.minSmallest && this.minLargest == other.minLargest && this.maxSmallest == other.maxSmallest && this.maxLargest == other.maxLargest)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        long hash = ((((((this.minSmallest + this.minLargest) << ((int) (16 + this.minLargest))) >> ((int) (this.maxSmallest + 48))) << ((int) (this.maxSmallest + 32))) >> ((int) (32 + this.maxLargest))) << ((int) (48 + this.maxLargest))) >> 16;
        return (int) ((hash >>> 32) ^ hash);
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.minSmallest);
        if (this.minSmallest != this.minLargest) {
            buf.append('/');
            buf.append(this.minLargest);
        }
        buf.append(" - ");
        buf.append(this.maxSmallest);
        if (this.maxSmallest != this.maxLargest) {
            buf.append('/');
            buf.append(this.maxLargest);
        }
        return buf.toString();
    }
}
