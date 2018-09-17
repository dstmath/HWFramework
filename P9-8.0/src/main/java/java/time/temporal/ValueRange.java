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
            return new ValueRange(min, min, max, max);
        }
        throw new IllegalArgumentException("Minimum value must be less than maximum value");
    }

    public static ValueRange of(long min, long maxSmallest, long maxLargest) {
        return of(min, min, maxSmallest, maxLargest);
    }

    public static ValueRange of(long minSmallest, long minLargest, long maxSmallest, long maxLargest) {
        if (minSmallest > minLargest) {
            throw new IllegalArgumentException("Smallest minimum value must be less than largest minimum value");
        } else if (maxSmallest > maxLargest) {
            throw new IllegalArgumentException("Smallest maximum value must be less than largest maximum value");
        } else if (minLargest <= maxLargest) {
            return new ValueRange(minSmallest, minLargest, maxSmallest, maxLargest);
        } else {
            throw new IllegalArgumentException("Minimum value must be less than maximum value");
        }
    }

    private ValueRange(long minSmallest, long minLargest, long maxSmallest, long maxLargest) {
        this.minSmallest = minSmallest;
        this.minLargest = minLargest;
        this.maxSmallest = maxSmallest;
        this.maxLargest = maxLargest;
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
        return isIntValue() ? isValidValue(value) : false;
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
        if (this.minSmallest != other.minSmallest || this.minLargest != other.minLargest || this.maxSmallest != other.maxSmallest) {
            z = false;
        } else if (this.maxLargest != other.maxLargest) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        long hash = ((((((this.minSmallest + this.minLargest) << ((int) (this.minLargest + 16))) >> ((int) (this.maxSmallest + 48))) << ((int) (this.maxSmallest + 32))) >> ((int) (this.maxLargest + 32))) << ((int) (this.maxLargest + 48))) >> 16;
        return (int) ((hash >>> 32) ^ hash);
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.minSmallest);
        if (this.minSmallest != this.minLargest) {
            buf.append('/').append(this.minLargest);
        }
        buf.append(" - ").append(this.maxSmallest);
        if (this.maxSmallest != this.maxLargest) {
            buf.append('/').append(this.maxLargest);
        }
        return buf.toString();
    }
}
