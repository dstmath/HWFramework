package ohos.global.icu.util;

public class TimeUnitAmount extends Measure {
    public TimeUnitAmount(Number number, TimeUnit timeUnit) {
        super(number, timeUnit);
    }

    public TimeUnitAmount(double d, TimeUnit timeUnit) {
        super(new Double(d), timeUnit);
    }

    public TimeUnit getTimeUnit() {
        return (TimeUnit) getUnit();
    }
}
