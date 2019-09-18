package android.icu.util;

public class Measure {
    private final Number number;
    private final MeasureUnit unit;

    public Measure(Number number2, MeasureUnit unit2) {
        if (number2 == null || unit2 == null) {
            throw new NullPointerException("Number and MeasureUnit must not be null");
        }
        this.number = number2;
        this.unit = unit2;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Measure)) {
            return false;
        }
        Measure m = (Measure) obj;
        if (!this.unit.equals(m.unit) || !numbersEqual(this.number, m.number)) {
            z = false;
        }
        return z;
    }

    private static boolean numbersEqual(Number a, Number b) {
        if (!a.equals(b) && a.doubleValue() != b.doubleValue()) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (31 * Double.valueOf(this.number.doubleValue()).hashCode()) + this.unit.hashCode();
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
