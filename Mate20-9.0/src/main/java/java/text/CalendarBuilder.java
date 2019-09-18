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

    /* access modifiers changed from: package-private */
    public CalendarBuilder set(int index, int value) {
        if (index == 1000) {
            index = 7;
            value = toCalendarDayOfWeek(value);
        }
        int[] iArr = this.field;
        int i = this.nextStamp;
        this.nextStamp = i + 1;
        iArr[index] = i;
        this.field[18 + index] = value;
        if (index > this.maxFieldIndex && index < 17) {
            this.maxFieldIndex = index;
        }
        return this;
    }

    /* access modifiers changed from: package-private */
    public CalendarBuilder addYear(int value) {
        int[] iArr = this.field;
        iArr[19] = iArr[19] + value;
        int[] iArr2 = this.field;
        iArr2[35] = iArr2[35] + value;
        return this;
    }

    /* access modifiers changed from: package-private */
    public boolean isSet(int index) {
        if (index == 1000) {
            index = 7;
        }
        return this.field[index] > 0;
    }

    /* access modifiers changed from: package-private */
    public CalendarBuilder clear(int index) {
        if (index == 1000) {
            index = 7;
        }
        this.field[index] = 0;
        this.field[18 + index] = 0;
        return this;
    }

    /* access modifiers changed from: package-private */
    public Calendar establish(Calendar cal) {
        int dayOfWeek;
        boolean weekDate = isSet(17) && this.field[17] > this.field[1];
        if (weekDate && !cal.isWeekDateSupported()) {
            if (!isSet(1)) {
                set(1, this.field[35]);
            }
            weekDate = false;
        }
        cal.clear();
        for (int stamp = 2; stamp < this.nextStamp; stamp++) {
            int index = 0;
            while (true) {
                if (index > this.maxFieldIndex) {
                    break;
                } else if (this.field[index] == stamp) {
                    cal.set(index, this.field[18 + index]);
                    break;
                } else {
                    index++;
                }
            }
        }
        if (weekDate) {
            int weekOfYear = isSet(3) ? this.field[21] : 1;
            int dayOfWeek2 = isSet(7) ? this.field[25] : cal.getFirstDayOfWeek();
            if (!isValidDayOfWeek(dayOfWeek2) && cal.isLenient()) {
                if (dayOfWeek2 >= 8) {
                    int dayOfWeek3 = dayOfWeek2 - 1;
                    weekOfYear += dayOfWeek3 / 7;
                    dayOfWeek = (dayOfWeek3 % 7) + 1;
                } else {
                    dayOfWeek = dayOfWeek2;
                    while (dayOfWeek <= 0) {
                        dayOfWeek += 7;
                        weekOfYear--;
                    }
                }
                dayOfWeek2 = toCalendarDayOfWeek(dayOfWeek);
            }
            cal.setWeekDate(this.field[35], weekOfYear, dayOfWeek2);
        }
        return cal;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CalendarBuilder:[");
        for (int i = 0; i < this.field.length; i++) {
            if (isSet(i)) {
                sb.append(i);
                sb.append('=');
                sb.append(this.field[18 + i]);
                sb.append(',');
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
        if (calendarDayOfWeek == 1) {
            return 7;
        }
        return calendarDayOfWeek - 1;
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
