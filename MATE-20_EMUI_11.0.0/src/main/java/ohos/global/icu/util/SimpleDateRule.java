package ohos.global.icu.util;

import java.util.Date;

public class SimpleDateRule implements DateRule {
    private Calendar calendar = new GregorianCalendar();
    private int dayOfMonth;
    private int dayOfWeek;
    private int month;

    public SimpleDateRule(int i, int i2) {
        this.month = i;
        this.dayOfMonth = i2;
        this.dayOfWeek = 0;
    }

    SimpleDateRule(int i, int i2, Calendar calendar2) {
        this.month = i;
        this.dayOfMonth = i2;
        this.dayOfWeek = 0;
        this.calendar = calendar2;
    }

    public SimpleDateRule(int i, int i2, int i3, boolean z) {
        this.month = i;
        this.dayOfMonth = i2;
        this.dayOfWeek = !z ? -i3 : i3;
    }

    @Override // ohos.global.icu.util.DateRule
    public Date firstAfter(Date date) {
        return doFirstBetween(date, null);
    }

    @Override // ohos.global.icu.util.DateRule
    public Date firstBetween(Date date, Date date2) {
        return doFirstBetween(date, date2);
    }

    @Override // ohos.global.icu.util.DateRule
    public boolean isOn(Date date) {
        boolean z;
        Calendar calendar2 = this.calendar;
        synchronized (calendar2) {
            calendar2.setTime(date);
            int i = calendar2.get(6);
            z = true;
            calendar2.setTime(computeInYear(calendar2.get(1), calendar2));
            if (calendar2.get(6) != i) {
                z = false;
            }
        }
        return z;
    }

    @Override // ohos.global.icu.util.DateRule
    public boolean isBetween(Date date, Date date2) {
        return firstBetween(date, date2) != null;
    }

    private Date doFirstBetween(Date date, Date date2) {
        Calendar calendar2 = this.calendar;
        synchronized (calendar2) {
            calendar2.setTime(date);
            int i = calendar2.get(1);
            int i2 = calendar2.get(2);
            if (i2 > this.month) {
                i++;
            }
            Date computeInYear = computeInYear(i, calendar2);
            if (i2 == this.month && computeInYear.before(date)) {
                computeInYear = computeInYear(i + 1, calendar2);
            }
            if (date2 == null || !computeInYear.after(date2)) {
                return computeInYear;
            }
            return null;
        }
    }

    private Date computeInYear(int i, Calendar calendar2) {
        Date time;
        int i2;
        synchronized (calendar2) {
            calendar2.clear();
            calendar2.set(0, calendar2.getMaximum(0));
            calendar2.set(1, i);
            calendar2.set(2, this.month);
            calendar2.set(5, this.dayOfMonth);
            if (this.dayOfWeek != 0) {
                calendar2.setTime(calendar2.getTime());
                int i3 = calendar2.get(7);
                if (this.dayOfWeek > 0) {
                    i2 = ((this.dayOfWeek - i3) + 7) % 7;
                } else {
                    i2 = -(((this.dayOfWeek + i3) + 7) % 7);
                }
                calendar2.add(5, i2);
            }
            time = calendar2.getTime();
        }
        return time;
    }
}
