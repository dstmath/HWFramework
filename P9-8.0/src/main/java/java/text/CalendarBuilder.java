package java.text;

import java.util.Calendar;

class CalendarBuilder {
    private static final int COMPUTED = 1;
    public static final int ISO_DAY_OF_WEEK = 1000;
    private static final int MAX_FIELD = 18;
    private static final int MINIMUM_USER_STAMP = 2;
    private static final int UNSET = 0;
    public static final int WEEK_YEAR = 17;
    private final int[] field = new int[36];
    private int maxFieldIndex = -1;
    private int nextStamp = 2;

    CalendarBuilder() {
    }

    CalendarBuilder set(int index, int value) {
        if (index == 1000) {
            index = 7;
            value = toCalendarDayOfWeek(value);
        }
        int[] iArr = this.field;
        int i = this.nextStamp;
        this.nextStamp = i + 1;
        iArr[index] = i;
        this.field[index + 18] = value;
        if (index > this.maxFieldIndex && index < 17) {
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
        if (index == 1000) {
            index = 7;
        }
        if (this.field[index] > 0) {
            return true;
        }
        return false;
    }

    CalendarBuilder clear(int index) {
        if (index == 1000) {
            index = 7;
        }
        this.field[index] = 0;
        this.field[index + 18] = 0;
        return this;
    }

    Calendar establish(Calendar cal) {
        boolean weekDate = isSet(17) ? this.field[17] > this.field[1] : false;
        if (weekDate && (cal.isWeekDateSupported() ^ 1) != 0) {
            if (!isSet(1)) {
                set(1, this.field[35]);
            }
            weekDate = false;
        }
        cal.clear();
        for (int stamp = 2; stamp < this.nextStamp; stamp++) {
            for (int index = 0; index <= this.maxFieldIndex; index++) {
                if (this.field[index] == stamp) {
                    cal.set(index, this.field[index + 18]);
                    break;
                }
            }
        }
        if (weekDate) {
            int weekOfYear = isSet(3) ? this.field[21] : 1;
            int dayOfWeek = isSet(7) ? this.field[25] : cal.getFirstDayOfWeek();
            if (!isValidDayOfWeek(dayOfWeek) && cal.isLenient()) {
                if (dayOfWeek >= 8) {
                    dayOfWeek--;
                    weekOfYear += dayOfWeek / 7;
                    dayOfWeek = (dayOfWeek % 7) + 1;
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
        for (int i = 0; i < this.field.length; i++) {
            if (isSet(i)) {
                sb.append(i).append('=').append(this.field[i + 18]).append(',');
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
        return calendarDayOfWeek == 1 ? 7 : calendarDayOfWeek - 1;
    }

    static int toCalendarDayOfWeek(int isoDayOfWeek) {
        if (!isValidDayOfWeek(isoDayOfWeek)) {
            return isoDayOfWeek;
        }
        return isoDayOfWeek == 7 ? 1 : isoDayOfWeek + 1;
    }

    static boolean isValidDayOfWeek(int dayOfWeek) {
        return dayOfWeek > 0 && dayOfWeek <= 7;
    }
}
