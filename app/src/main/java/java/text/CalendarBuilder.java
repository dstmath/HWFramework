package java.text;

import java.util.Calendar;

class CalendarBuilder {
    private static final int COMPUTED = 1;
    public static final int ISO_DAY_OF_WEEK = 1000;
    private static final int MAX_FIELD = 18;
    private static final int MINIMUM_USER_STAMP = 2;
    private static final int UNSET = 0;
    public static final int WEEK_YEAR = 17;
    private final int[] field;
    private int maxFieldIndex;
    private int nextStamp;

    CalendarBuilder() {
        this.field = new int[36];
        this.nextStamp = MINIMUM_USER_STAMP;
        this.maxFieldIndex = -1;
    }

    CalendarBuilder set(int index, int value) {
        if (index == ISO_DAY_OF_WEEK) {
            index = 7;
            value = toCalendarDayOfWeek(value);
        }
        int[] iArr = this.field;
        int i = this.nextStamp;
        this.nextStamp = i + COMPUTED;
        iArr[index] = i;
        this.field[index + MAX_FIELD] = value;
        if (index > this.maxFieldIndex && index < WEEK_YEAR) {
            this.maxFieldIndex = index;
        }
        return this;
    }

    CalendarBuilder addYear(int value) {
        int[] iArr = this.field;
        iArr[19] = iArr[19] + value;
        iArr = this.field;
        iArr[35] = iArr[35] + value;
        return this;
    }

    boolean isSet(int index) {
        if (index == ISO_DAY_OF_WEEK) {
            index = 7;
        }
        if (this.field[index] > 0) {
            return true;
        }
        return false;
    }

    CalendarBuilder clear(int index) {
        if (index == ISO_DAY_OF_WEEK) {
            index = 7;
        }
        this.field[index] = UNSET;
        this.field[index + MAX_FIELD] = UNSET;
        return this;
    }

    Calendar establish(Calendar cal) {
        boolean weekDate = false;
        if (isSet(WEEK_YEAR) && this.field[WEEK_YEAR] > this.field[COMPUTED]) {
            weekDate = true;
        }
        if (weekDate && !cal.isWeekDateSupported()) {
            if (!isSet(COMPUTED)) {
                set(COMPUTED, this.field[35]);
            }
            weekDate = false;
        }
        cal.clear();
        for (int stamp = MINIMUM_USER_STAMP; stamp < this.nextStamp; stamp += COMPUTED) {
            for (int index = UNSET; index <= this.maxFieldIndex; index += COMPUTED) {
                if (this.field[index] == stamp) {
                    cal.set(index, this.field[index + MAX_FIELD]);
                    break;
                }
            }
        }
        if (weekDate) {
            int weekOfYear = isSet(3) ? this.field[21] : COMPUTED;
            int dayOfWeek = isSet(7) ? this.field[25] : cal.getFirstDayOfWeek();
            if (!isValidDayOfWeek(dayOfWeek) && cal.isLenient()) {
                if (dayOfWeek >= 8) {
                    dayOfWeek--;
                    weekOfYear += dayOfWeek / 7;
                    dayOfWeek = (dayOfWeek % 7) + COMPUTED;
                } else {
                    while (dayOfWeek <= 0) {
                        dayOfWeek += 7;
                        weekOfYear--;
                    }
                }
                dayOfWeek = toCalendarDayOfWeek(dayOfWeek);
            }
            cal.setWeekDate(this.field[35], weekOfYear, dayOfWeek);
        }
        return cal;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CalendarBuilder:[");
        for (int i = UNSET; i < this.field.length; i += COMPUTED) {
            if (isSet(i)) {
                sb.append(i).append('=').append(this.field[i + MAX_FIELD]).append(',');
            }
        }
        int lastIndex = sb.length() - 1;
        if (sb.charAt(lastIndex) == ',') {
            sb.setLength(lastIndex);
        }
        sb.append(']');
        return sb.toString();
    }

    static int toISODayOfWeek(int calendarDayOfWeek) {
        return calendarDayOfWeek == COMPUTED ? 7 : calendarDayOfWeek - 1;
    }

    static int toCalendarDayOfWeek(int isoDayOfWeek) {
        if (!isValidDayOfWeek(isoDayOfWeek)) {
            return isoDayOfWeek;
        }
        return isoDayOfWeek == 7 ? COMPUTED : isoDayOfWeek + COMPUTED;
    }

    static boolean isValidDayOfWeek(int dayOfWeek) {
        return dayOfWeek > 0 && dayOfWeek <= 7;
    }
}
