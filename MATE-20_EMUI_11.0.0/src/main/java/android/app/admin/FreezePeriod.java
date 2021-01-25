package android.app.admin;

import android.app.admin.SystemUpdatePolicy;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Pair;
import com.android.internal.content.NativeLibraryHelper;
import java.time.LocalDate;
import java.time.MonthDay;
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
        return this.mEndDay + 365;
    }

    /* access modifiers changed from: package-private */
    public boolean contains(LocalDate localDate) {
        int daysOfYear = dayOfYearDisregardLeapYear(localDate);
        return !isWrapped() ? this.mStartDay <= daysOfYear && daysOfYear <= this.mEndDay : this.mStartDay <= daysOfYear || daysOfYear <= this.mEndDay;
    }

    /* access modifiers changed from: package-private */
    public boolean after(LocalDate localDate) {
        return this.mStartDay > dayOfYearDisregardLeapYear(localDate);
    }

    /* access modifiers changed from: package-private */
    public Pair<LocalDate, LocalDate> toCurrentOrFutureRealDates(LocalDate now) {
        boolean z;
        int startYearAdjustment;
        int nowDays = dayOfYearDisregardLeapYear(now);
        if (contains(now)) {
            if (this.mStartDay <= nowDays) {
                startYearAdjustment = 0;
                z = isWrapped();
            } else {
                startYearAdjustment = -1;
                z = false;
            }
        } else if (this.mStartDay > nowDays) {
            startYearAdjustment = 0;
            z = isWrapped();
        } else {
            startYearAdjustment = 1;
            z = true;
        }
        LocalDate startDate = LocalDate.ofYearDay(2001, this.mStartDay).withYear(now.getYear() + startYearAdjustment);
        LocalDate ofYearDay = LocalDate.ofYearDay(2001, this.mEndDay);
        int year = now.getYear();
        int endYearAdjustment = z ? 1 : 0;
        int endYearAdjustment2 = z ? 1 : 0;
        int endYearAdjustment3 = z ? 1 : 0;
        int endYearAdjustment4 = z ? 1 : 0;
        int endYearAdjustment5 = z ? 1 : 0;
        return new Pair<>(startDate, ofYearDay.withYear(year + endYearAdjustment));
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
        return (dayOfYearDisregardLeapYear(first) - dayOfYearDisregardLeapYear(second)) + ((first.getYear() - second.getYear()) * 365);
    }

    static List<FreezePeriod> canonicalizePeriods(List<FreezePeriod> intervals) {
        boolean[] taken = new boolean[365];
        for (FreezePeriod interval : intervals) {
            for (int i = interval.mStartDay; i <= interval.getEffectiveEndDay(); i++) {
                taken[(i - 1) % 365] = true;
            }
        }
        List<FreezePeriod> result = new ArrayList<>();
        int i2 = 0;
        while (i2 < 365) {
            if (!taken[i2]) {
                i2++;
            } else {
                int intervalStart = i2 + 1;
                while (i2 < 365 && taken[i2]) {
                    i2++;
                }
                result.add(new FreezePeriod(intervalStart, i2));
            }
        }
        int lastIndex = result.size() - 1;
        if (lastIndex > 0 && result.get(lastIndex).mEndDay == 365 && result.get(0).mStartDay == 1) {
            result.set(lastIndex, new FreezePeriod(result.get(lastIndex).mStartDay, result.get(0).mEndDay));
            result.remove(0);
        }
        return result;
    }

    static void validatePeriods(List<FreezePeriod> periods) {
        FreezePeriod previous;
        List<FreezePeriod> allPeriods = canonicalizePeriods(periods);
        if (allPeriods.size() == periods.size()) {
            for (int i = 0; i < allPeriods.size(); i++) {
                FreezePeriod current = allPeriods.get(i);
                if (current.getLength() <= 90) {
                    if (i > 0) {
                        previous = allPeriods.get(i - 1);
                    } else {
                        previous = allPeriods.get(allPeriods.size() - 1);
                    }
                    if (previous != current) {
                        int separation = (i != 0 || previous.isWrapped()) ? (current.mStartDay - previous.mEndDay) - 1 : (current.mStartDay + (365 - previous.mEndDay)) - 1;
                        if (separation < 60) {
                            throw SystemUpdatePolicy.ValidationFailedException.freezePeriodTooClose("Freeze periods " + previous + " and " + current + " are too close together: " + separation + " days apart");
                        }
                    }
                } else {
                    throw SystemUpdatePolicy.ValidationFailedException.freezePeriodTooLong("Freeze period " + current + " is too long: " + current.getLength() + " days");
                }
            }
            return;
        }
        throw SystemUpdatePolicy.ValidationFailedException.duplicateOrOverlapPeriods();
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x006b A[EDGE_INSN: B:34:0x006b->B:17:0x006b ?: BREAK  , SYNTHETIC] */
    static void validateAgainstPreviousFreezePeriod(List<FreezePeriod> periods, LocalDate prevPeriodStart, LocalDate prevPeriodEnd, LocalDate now) {
        FreezePeriod interval;
        if (periods.size() != 0 && prevPeriodStart != null && prevPeriodEnd != null) {
            if (prevPeriodStart.isAfter(now) || prevPeriodEnd.isAfter(now)) {
                Log.w(TAG, "Previous period (" + prevPeriodStart + SmsManager.REGEX_PREFIX_DELIMITER + prevPeriodEnd + ") is after current date " + now);
            }
            List<FreezePeriod> allPeriods = canonicalizePeriods(periods);
            FreezePeriod curOrNextFreezePeriod = allPeriods.get(0);
            Iterator<FreezePeriod> it = allPeriods.iterator();
            while (true) {
                if (!it.hasNext()) {
                    interval = it.next();
                    if (interval.contains(now) || interval.mStartDay > dayOfYearDisregardLeapYear(now)) {
                        break;
                    }
                    if (!it.hasNext()) {
                        break;
                    }
                }
            }
            curOrNextFreezePeriod = interval;
            Pair<LocalDate, LocalDate> curOrNextFreezeDates = curOrNextFreezePeriod.toCurrentOrFutureRealDates(now);
            if (now.isAfter(curOrNextFreezeDates.first)) {
                curOrNextFreezeDates = new Pair<>(now, curOrNextFreezeDates.second);
            }
            if (!curOrNextFreezeDates.first.isAfter(curOrNextFreezeDates.second)) {
                String periodsDescription = "Prev: " + prevPeriodStart + SmsManager.REGEX_PREFIX_DELIMITER + prevPeriodEnd + "; cur: " + ((Object) curOrNextFreezeDates.first) + SmsManager.REGEX_PREFIX_DELIMITER + ((Object) curOrNextFreezeDates.second);
                long separation = (long) (distanceWithoutLeapYear(curOrNextFreezeDates.first, prevPeriodEnd) - 1);
                if (separation <= 0) {
                    long length = (long) (distanceWithoutLeapYear(curOrNextFreezeDates.second, prevPeriodStart) + 1);
                    if (length > 90) {
                        throw SystemUpdatePolicy.ValidationFailedException.combinedPeriodTooLong("Combined freeze period exceeds maximum days: " + length + ", " + periodsDescription);
                    }
                } else if (separation < 60) {
                    throw SystemUpdatePolicy.ValidationFailedException.combinedPeriodTooClose("Previous freeze period too close to new period: " + separation + ", " + periodsDescription);
                }
            } else {
                throw new IllegalStateException("Current freeze dates inverted: " + ((Object) curOrNextFreezeDates.first) + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + ((Object) curOrNextFreezeDates.second));
            }
        }
    }
}
