package android.icu.util;

import java.util.Date;

public class SimpleDateRule implements DateRule {
    private Calendar calendar = new GregorianCalendar();
    private int dayOfMonth;
    private int dayOfWeek;
    private int month;

    public SimpleDateRule(int month2, int dayOfMonth2) {
        this.month = month2;
        this.dayOfMonth = dayOfMonth2;
        this.dayOfWeek = 0;
    }

    SimpleDateRule(int month2, int dayOfMonth2, Calendar cal) {
        this.month = month2;
        this.dayOfMonth = dayOfMonth2;
        this.dayOfWeek = 0;
        this.calendar = cal;
    }

    public SimpleDateRule(int month2, int dayOfMonth2, int dayOfWeek2, boolean after) {
        this.month = month2;
        this.dayOfMonth = dayOfMonth2;
        this.dayOfWeek = after ? dayOfWeek2 : -dayOfWeek2;
    }

    public Date firstAfter(Date start) {
        return doFirstBetween(start, null);
    }

    public Date firstBetween(Date start, Date end) {
        return doFirstBetween(start, end);
    }

    public boolean isOn(Date date) {
        boolean z;
        Calendar c = this.calendar;
        synchronized (c) {
            c.setTime(date);
            int dayOfYear = c.get(6);
            z = true;
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

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0037, code lost:
        return r3;
     */
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
            if (end != null && result.after(end)) {
                return null;
            }
        }
    }

    private Date computeInYear(int year, Calendar c) {
        Date time;
        int delta;
        synchronized (c) {
            c.clear();
            c.set(0, c.getMaximum(0));
            c.set(1, year);
            c.set(2, this.month);
            c.set(5, this.dayOfMonth);
            if (this.dayOfWeek != 0) {
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
