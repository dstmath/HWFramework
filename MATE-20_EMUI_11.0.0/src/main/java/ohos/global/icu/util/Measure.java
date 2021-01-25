package ohos.global.icu.util;

public class Measure {
    private final Number number;
    private final MeasureUnit unit;

    public Measure(Number number2, MeasureUnit measureUnit) {
        if (number2 == null || measureUnit == null) {
            throw new NullPointerException("Number and MeasureUnit must not be null");
        }
        this.number = number2;
        this.unit = measureUnit;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Measure)) {
            return false;
        }
        Measure measure = (Measure) obj;
        return this.unit.equals(measure.unit) && numbersEqual(this.number, measure.number);
    }

    private static boolean numbersEqual(Number number2, Number number3) {
        if (!number2.equals(number3) && number2.doubleValue() != number3.doubleValue()) {
            return false;
        }
        return true;
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
