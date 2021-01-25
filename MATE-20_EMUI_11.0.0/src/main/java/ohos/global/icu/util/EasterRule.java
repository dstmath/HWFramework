package ohos.global.icu.util;

import java.util.Date;

/* compiled from: EasterHoliday */
class EasterRule implements DateRule {
    private GregorianCalendar calendar = new GregorianCalendar();
    private int daysAfterEaster;

    public EasterRule(int i, boolean z) {
        this.daysAfterEaster = i;
        if (z) {
            this.calendar.setGregorianChange(new Date(Long.MAX_VALUE));
        }
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
        synchronized (this.calendar) {
            this.calendar.setTime(date);
            int i = this.calendar.get(6);
            this.calendar.setTime(computeInYear(this.calendar.getTime(), this.calendar));
            z = this.calendar.get(6) == i;
        }
        return z;
    }

    @Override // ohos.global.icu.util.DateRule
    public boolean isBetween(Date date, Date date2) {
        return firstBetween(date, date2) != null;
    }

    private Date doFirstBetween(Date date, Date date2) {
        synchronized (this.calendar) {
            Date computeInYear = computeInYear(date, this.calendar);
            if (computeInYear.before(date)) {
                this.calendar.setTime(date);
                this.calendar.get(1);
                this.calendar.add(1, 1);
                computeInYear = computeInYear(this.calendar.getTime(), this.calendar);
            }
            if (date2 == null || computeInYear.before(date2)) {
                return computeInYear;
            }
            return null;
        }
    }

    private Date computeInYear(Date date, GregorianCalendar gregorianCalendar) {
        int i;
        int i2;
        Date time;
        if (gregorianCalendar == null) {
            gregorianCalendar = this.calendar;
        }
        synchronized (gregorianCalendar) {
            gregorianCalendar.setTime(date);
            int i3 = gregorianCalendar.get(1);
            int i4 = i3 % 19;
            if (gregorianCalendar.getTime().after(gregorianCalendar.getGregorianChange())) {
                int i5 = i3 / 100;
                int i6 = ((((i5 - (i5 / 4)) - (((i5 * 8) + 13) / 25)) + (i4 * 19)) + 15) % 30;
                i = i6 - ((i6 / 28) * (1 - (((i6 / 28) * (29 / (i6 + 1))) * ((21 - i4) / 11))));
                i2 = ((((((i3 / 4) + i3) + i) + 2) - i5) + (i5 / 4)) % 7;
            } else {
                i = ((i4 * 19) + 15) % 30;
                i2 = (((i3 / 4) + i3) + i) % 7;
            }
            int i7 = i - i2;
            int i8 = ((i7 + 40) / 44) + 3;
            gregorianCalendar.clear();
            gregorianCalendar.set(0, 1);
            gregorianCalendar.set(1, i3);
            gregorianCalendar.set(2, i8 - 1);
            gregorianCalendar.set(5, (i7 + 28) - ((i8 / 4) * 31));
            gregorianCalendar.getTime();
            gregorianCalendar.add(5, this.daysAfterEaster);
            time = gregorianCalendar.getTime();
        }
        return time;
    }
}
