package android.icu.util;

import java.util.Date;

/* compiled from: EasterHoliday */
class EasterRule implements DateRule {
    private static GregorianCalendar gregorian = new GregorianCalendar();
    private static GregorianCalendar orthodox = new GregorianCalendar();
    private GregorianCalendar calendar = gregorian;
    private int daysAfterEaster;

    public EasterRule(int daysAfterEaster, boolean isOrthodox) {
        this.daysAfterEaster = daysAfterEaster;
        if (isOrthodox) {
            orthodox.setGregorianChange(new Date(Long.MAX_VALUE));
            this.calendar = orthodox;
        }
    }

    public Date firstAfter(Date start) {
        return doFirstBetween(start, null);
    }

    public Date firstBetween(Date start, Date end) {
        return doFirstBetween(start, end);
    }

    public boolean isOn(Date date) {
        boolean z;
        synchronized (this.calendar) {
            this.calendar.setTime(date);
            int dayOfYear = this.calendar.get(6);
            this.calendar.setTime(computeInYear(this.calendar.getTime(), this.calendar));
            z = this.calendar.get(6) == dayOfYear;
        }
        return z;
    }

    public boolean isBetween(Date start, Date end) {
        return firstBetween(start, end) != null;
    }

    /* JADX WARNING: Missing block: B:13:0x003b, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Date doFirstBetween(Date start, Date end) {
        synchronized (this.calendar) {
            Date result = computeInYear(start, this.calendar);
            if (result.before(start)) {
                this.calendar.setTime(start);
                this.calendar.get(1);
                this.calendar.add(1, 1);
                result = computeInYear(this.calendar.getTime(), this.calendar);
            }
            if (end == null || (result.before(end) ^ 1) == 0) {
            } else {
                return null;
            }
        }
    }

    private Date computeInYear(Date date, GregorianCalendar cal) {
        Date time;
        if (cal == null) {
            cal = this.calendar;
        }
        synchronized (cal) {
            int i;
            int j;
            cal.setTime(date);
            int year = cal.get(1);
            int g = year % 19;
            if (cal.getTime().after(cal.getGregorianChange())) {
                int c = year / 100;
                int h = ((((c - (c / 4)) - (((c * 8) + 13) / 25)) + (g * 19)) + 15) % 30;
                i = h - ((h / 28) * (1 - (((h / 28) * (29 / (h + 1))) * ((21 - g) / 11))));
                j = ((((((year / 4) + year) + i) + 2) - c) + (c / 4)) % 7;
            } else {
                i = ((g * 19) + 15) % 30;
                j = (((year / 4) + year) + i) % 7;
            }
            int l = i - j;
            int m = ((l + 40) / 44) + 3;
            int d = (l + 28) - ((m / 4) * 31);
            cal.clear();
            cal.set(0, 1);
            cal.set(1, year);
            cal.set(2, m - 1);
            cal.set(5, d);
            cal.getTime();
            cal.add(5, this.daysAfterEaster);
            time = cal.getTime();
        }
        return time;
    }
}
