package android.icu.util;

import java.util.Date;

public class SimpleDateRule implements DateRule {
    private Calendar calendar = new GregorianCalendar();
    private int dayOfMonth;
    private int dayOfWeek;
    private int month;

    public SimpleDateRule(int month, int dayOfMonth) {
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = 0;
    }

    SimpleDateRule(int month, int dayOfMonth, Calendar cal) {
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = 0;
        this.calendar = cal;
    }

    public SimpleDateRule(int month, int dayOfMonth, int dayOfWeek, boolean after) {
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        if (!after) {
            dayOfWeek = -dayOfWeek;
        }
        this.dayOfWeek = dayOfWeek;
    }

    public Date firstAfter(Date start) {
        return doFirstBetween(start, null);
    }

    public Date firstBetween(Date start, Date end) {
        return doFirstBetween(start, end);
    }

    public boolean isOn(Date date) {
        boolean z = true;
        Calendar c = this.calendar;
        synchronized (c) {
            c.setTime(date);
            int dayOfYear = c.get(6);
            c.setTime(computeInYear(c.get(1), c));
            if (c.get(6) != dayOfYear) {
                z = false;
            }
        }
        return z;
    }

    public boolean isBetween(Date start, Date end) {
        return firstBetween(start, end) != null;
    }

    /* JADX WARNING: Missing block: B:17:0x0036, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Date doFirstBetween(Date start, Date end) {
        Calendar c = this.calendar;
        synchronized (c) {
            c.setTime(start);
            int year = c.get(1);
            int mon = c.get(2);
            if (mon > this.month) {
                year++;
            }
            Date result = computeInYear(year, c);
            if (mon == this.month && result.before(start)) {
                result = computeInYear(year + 1, c);
            }
            if (end == null || !result.after(end)) {
            } else {
                return null;
            }
        }
    }

    private Date computeInYear(int year, Calendar c) {
        Date time;
        synchronized (c) {
            c.clear();
            c.set(0, c.getMaximum(0));
            c.set(1, year);
            c.set(2, this.month);
            c.set(5, this.dayOfMonth);
            if (this.dayOfWeek != 0) {
                int delta;
                c.setTime(c.getTime());
                int weekday = c.get(7);
                if (this.dayOfWeek > 0) {
                    delta = ((this.dayOfWeek - weekday) + 7) % 7;
                } else {
                    delta = -(((this.dayOfWeek + weekday) + 7) % 7);
                }
                c.add(5, delta);
            }
            time = c.getTime();
        }
        return time;
    }
}
