package android.app.admin;

import android.app.admin.SystemUpdatePolicy;
import android.util.Log;
import android.util.Pair;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FreezePeriod {
    static final int DAYS_IN_YEAR = 365;
    private static final int DUMMY_YEAR = 2001;
    private static final String TAG = "FreezePeriod";
    private final MonthDay mEnd;
    private final int mEndDay;
    private final MonthDay mStart;
    private final int mStartDay;

    public FreezePeriod(MonthDay start, MonthDay end) {
        this.mStart = start;
        this.mStartDay = this.mStart.atYear(2001).getDayOfYear();
        this.mEnd = end;
        this.mEndDay = this.mEnd.atYear(2001).getDayOfYear();
    }

    public MonthDay getStart() {
        return this.mStart;
    }

    public MonthDay getEnd() {
        return this.mEnd;
    }

    private FreezePeriod(int startDay, int endDay) {
        this.mStartDay = startDay;
        this.mStart = dayOfYearToMonthDay(startDay);
        this.mEndDay = endDay;
        this.mEnd = dayOfYearToMonthDay(endDay);
    }

    /* access modifiers changed from: package-private */
    public int getLength() {
        return (getEffectiveEndDay() - this.mStartDay) + 1;
    }

    /* access modifiers changed from: package-private */
    public boolean isWrapped() {
        return this.mEndDay < this.mStartDay;
    }

    /* access modifiers changed from: package-private */
    public int getEffectiveEndDay() {
        if (!isWrapped()) {
            return this.mEndDay;
        }
        return this.mEndDay + DAYS_IN_YEAR;
    }

    /* access modifiers changed from: package-private */
    public boolean contains(LocalDate localDate) {
        int daysOfYear = dayOfYearDisregardLeapYear(localDate);
        boolean z = false;
        if (!isWrapped()) {
            if (this.mStartDay <= daysOfYear && daysOfYear <= this.mEndDay) {
                z = true;
            }
            return z;
        }
        if (this.mStartDay <= daysOfYear || daysOfYear <= this.mEndDay) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean after(LocalDate localDate) {
        return this.mStartDay > dayOfYearDisregardLeapYear(localDate);
    }

    /* access modifiers changed from: package-private */
    public Pair<LocalDate, LocalDate> toCurrentOrFutureRealDates(LocalDate now) {
        int endYearAdjustment;
        int startYearAdjustment;
        int nowDays = dayOfYearDisregardLeapYear(now);
        if (contains(now)) {
            if (this.mStartDay <= nowDays) {
                startYearAdjustment = 0;
                endYearAdjustment = isWrapped();
            } else {
                startYearAdjustment = -1;
                endYearAdjustment = 0;
            }
        } else if (this.mStartDay > nowDays) {
            startYearAdjustment = 0;
            endYearAdjustment = isWrapped();
        } else {
            startYearAdjustment = 1;
            endYearAdjustment = 1;
        }
        return new Pair<>(LocalDate.ofYearDay(2001, this.mStartDay).withYear(now.getYear() + startYearAdjustment), LocalDate.ofYearDay(2001, this.mEndDay).withYear(now.getYear() + ((int) endYearAdjustment)));
    }

    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        return LocalDate.ofYearDay(2001, this.mStartDay).format(formatter) + " - " + LocalDate.ofYearDay(2001, this.mEndDay).format(formatter);
    }

    private static MonthDay dayOfYearToMonthDay(int dayOfYear) {
        LocalDate date = LocalDate.ofYearDay(2001, dayOfYear);
        return MonthDay.of(date.getMonth(), date.getDayOfMonth());
    }

    private static int dayOfYearDisregardLeapYear(LocalDate date) {
        return date.withYear(2001).getDayOfYear();
    }

    public static int distanceWithoutLeapYear(LocalDate first, LocalDate second) {
        return (dayOfYearDisregardLeapYear(first) - dayOfYearDisregardLeapYear(second)) + (DAYS_IN_YEAR * (first.getYear() - second.getYear()));
    }

    static List<FreezePeriod> canonicalizePeriods(List<FreezePeriod> intervals) {
        boolean[] taken = new boolean[DAYS_IN_YEAR];
        for (FreezePeriod interval : intervals) {
            for (int i = interval.mStartDay; i <= interval.getEffectiveEndDay(); i++) {
                taken[(i - 1) % DAYS_IN_YEAR] = true;
            }
        }
        List<FreezePeriod> result = new ArrayList<>();
        int i2 = 0;
        while (i2 < DAYS_IN_YEAR) {
            if (!taken[i2]) {
                i2++;
            } else {
                int intervalStart = i2 + 1;
                while (i2 < DAYS_IN_YEAR && taken[i2]) {
                    i2++;
                }
                result.add(new FreezePeriod(intervalStart, i2));
            }
        }
        int lastIndex = result.size() - 1;
        if (lastIndex > 0 && result.get(lastIndex).mEndDay == DAYS_IN_YEAR && result.get(0).mStartDay == 1) {
            result.set(lastIndex, new FreezePeriod(result.get(lastIndex).mStartDay, result.get(0).mEndDay));
            result.remove(0);
        }
        return result;
    }

    static void validatePeriods(List<FreezePeriod> periods) {
        FreezePeriod previous;
        int separation;
        List<FreezePeriod> allPeriods = canonicalizePeriods(periods);
        if (allPeriods.size() == periods.size()) {
            int i = 0;
            while (i < allPeriods.size()) {
                FreezePeriod current = allPeriods.get(i);
                if (current.getLength() <= 90) {
                    if (i > 0) {
                        previous = allPeriods.get(i - 1);
                    } else {
                        previous = allPeriods.get(allPeriods.size() - 1);
                    }
                    if (previous != current) {
                        if (i != 0 || previous.isWrapped()) {
                            separation = (current.mStartDay - previous.mEndDay) - 1;
                        } else {
                            separation = (current.mStartDay + (365 - previous.mEndDay)) - 1;
                        }
                        if (separation < 60) {
                            throw SystemUpdatePolicy.ValidationFailedException.freezePeriodTooClose("Freeze periods " + previous + " and " + current + " are too close together: " + separation + " days apart");
                        }
                    }
                    i++;
                } else {
                    throw SystemUpdatePolicy.ValidationFailedException.freezePeriodTooLong("Freeze period " + current + " is too long: " + current.getLength() + " days");
                }
            }
            return;
        }
        throw SystemUpdatePolicy.ValidationFailedException.duplicateOrOverlapPeriods();
    }

    static void validateAgainstPreviousFreezePeriod(List<FreezePeriod> periods, LocalDate prevPeriodStart, LocalDate prevPeriodEnd, LocalDate now) {
        FreezePeriod interval;
        if (periods.size() != 0 && prevPeriodStart != null && prevPeriodEnd != null) {
            if (prevPeriodStart.isAfter(now) || prevPeriodEnd.isAfter(now)) {
                Log.w(TAG, "Previous period (" + prevPeriodStart + "," + prevPeriodEnd + ") is after current date " + now);
            }
            List<FreezePeriod> allPeriods = canonicalizePeriods(periods);
            FreezePeriod curOrNextFreezePeriod = allPeriods.get(0);
            Iterator<FreezePeriod> it = allPeriods.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                interval = it.next();
                if (!interval.contains(now)) {
                    if (interval.mStartDay > dayOfYearDisregardLeapYear(now)) {
                        break;
                    }
                } else {
                    break;
                }
            }
            curOrNextFreezePeriod = interval;
            Pair<LocalDate, LocalDate> curOrNextFreezeDates = curOrNextFreezePeriod.toCurrentOrFutureRealDates(now);
            if (now.isAfter((ChronoLocalDate) curOrNextFreezeDates.first)) {
                curOrNextFreezeDates = new Pair<>(now, (LocalDate) curOrNextFreezeDates.second);
            }
            if (!((LocalDate) curOrNextFreezeDates.first).isAfter((ChronoLocalDate) curOrNextFreezeDates.second)) {
                String periodsDescription = "Prev: " + prevPeriodStart + "," + prevPeriodEnd + "; cur: " + curOrNextFreezeDates.first + "," + curOrNextFreezeDates.second;
                long separation = (long) (distanceWithoutLeapYear((LocalDate) curOrNextFreezeDates.first, prevPeriodEnd) - 1);
                if (separation <= 0) {
                    if (((long) (distanceWithoutLeapYear((LocalDate) curOrNextFreezeDates.second, prevPeriodStart) + 1)) > 90) {
                        throw SystemUpdatePolicy.ValidationFailedException.combinedPeriodTooLong("Combined freeze period exceeds maximum days: " + length + ", " + periodsDescription);
                    }
                } else if (separation < 60) {
                    throw SystemUpdatePolicy.ValidationFailedException.combinedPeriodTooClose("Previous freeze period too close to new period: " + separation + ", " + periodsDescription);
                }
                return;
            }
            throw new IllegalStateException("Current freeze dates inverted: " + curOrNextFreezeDates.first + "-" + curOrNextFreezeDates.second);
        }
    }
}
