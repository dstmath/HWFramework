package android.icu.util;

public class Measure {
    private final Number number;
    private final MeasureUnit unit;

    public Measure(Number number, MeasureUnit unit) {
        if (number == null || unit == null) {
            throw new NullPointerException();
        }
        this.number = number;
        this.unit = unit;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Measure)) {
            return false;
        }
        Measure m = (Measure) obj;
        if (this.unit.equals(m.unit)) {
            z = numbersEqual(this.number, m.number);
        }
        return z;
    }

    private static boolean numbersEqual(Number a, Number b) {
        if (a.equals(b) || a.doubleValue() == b.doubleValue()) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (Double.valueOf(this.number.doubleValue()).hashCode() * 31) + this.unit.hashCode();
    }

    public String toString() {
        return this.number.toString() + ' ' + this.unit.toString();
    }

    public Number getNumber() {
        return this.number;
    }

    public MeasureUnit getUnit() {
        return this.unit;
    }
}
