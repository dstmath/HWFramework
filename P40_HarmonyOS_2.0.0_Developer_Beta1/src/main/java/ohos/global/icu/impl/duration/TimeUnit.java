package ohos.global.icu.impl.duration;

import ohos.agp.styles.attributes.TimePickerAttrsConstants;
import ohos.global.icu.impl.CalendarAstronomer;

public final class TimeUnit {
    public static final TimeUnit DAY = new TimeUnit("day", 3);
    public static final TimeUnit HOUR = new TimeUnit(TimePickerAttrsConstants.HOUR, 4);
    public static final TimeUnit MILLISECOND = new TimeUnit("millisecond", 7);
    public static final TimeUnit MINUTE = new TimeUnit(TimePickerAttrsConstants.MINUTE, 5);
    public static final TimeUnit MONTH = new TimeUnit("month", 1);
    public static final TimeUnit SECOND = new TimeUnit(TimePickerAttrsConstants.SECOND, 6);
    public static final TimeUnit WEEK = new TimeUnit("week", 2);
    public static final TimeUnit YEAR = new TimeUnit("year", 0);
    static final long[] approxDurations = {31557600000L, 2630880000L, 604800000, CalendarAstronomer.DAY_MS, 3600000, 60000, 1000, 1};
    static final TimeUnit[] units = {YEAR, MONTH, WEEK, DAY, HOUR, MINUTE, SECOND, MILLISECOND};
    final String name;
    final byte ordinal;

    private TimeUnit(String str, int i) {
        this.name = str;
        this.ordinal = (byte) i;
    }

    public String toString() {
        return this.name;
    }

    public TimeUnit larger() {
        byte b = this.ordinal;
        if (b == 0) {
            return null;
        }
        return units[b - 1];
    }

    public TimeUnit smaller() {
        byte b = this.ordinal;
        TimeUnit[] timeUnitArr = units;
        if (b == timeUnitArr.length - 1) {
            return null;
        }
        return timeUnitArr[b + 1];
    }

    public int ordinal() {
        return this.ordinal;
    }
}
