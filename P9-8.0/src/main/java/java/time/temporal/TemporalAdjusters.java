package java.time.temporal;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.-$Lambda$OLNcPvjff81GnHHsYVRY4mMpF30.AnonymousClass10;
import java.time.temporal.-$Lambda$OLNcPvjff81GnHHsYVRY4mMpF30.AnonymousClass11;
import java.time.temporal.-$Lambda$OLNcPvjff81GnHHsYVRY4mMpF30.AnonymousClass12;
import java.time.temporal.-$Lambda$OLNcPvjff81GnHHsYVRY4mMpF30.AnonymousClass6;
import java.time.temporal.-$Lambda$OLNcPvjff81GnHHsYVRY4mMpF30.AnonymousClass7;
import java.time.temporal.-$Lambda$OLNcPvjff81GnHHsYVRY4mMpF30.AnonymousClass8;
import java.time.temporal.-$Lambda$OLNcPvjff81GnHHsYVRY4mMpF30.AnonymousClass9;
import java.util.Objects;
import java.util.function.UnaryOperator;

public final class TemporalAdjusters {
    private TemporalAdjusters() {
    }

    public static TemporalAdjuster ofDateAdjuster(UnaryOperator<LocalDate> dateBasedAdjuster) {
        Objects.requireNonNull((Object) dateBasedAdjuster, "dateBasedAdjuster");
        return new AnonymousClass6(dateBasedAdjuster);
    }

    public static TemporalAdjuster firstDayOfMonth() {
        return new -$Lambda$OLNcPvjff81GnHHsYVRY4mMpF30();
    }

    public static TemporalAdjuster lastDayOfMonth() {
        return new TemporalAdjuster() {
            public final Temporal adjustInto(Temporal temporal) {
                return $m$0(temporal);
            }
        };
    }

    public static TemporalAdjuster firstDayOfNextMonth() {
        return new TemporalAdjuster() {
            public final Temporal adjustInto(Temporal temporal) {
                return $m$0(temporal);
            }
        };
    }

    public static TemporalAdjuster firstDayOfYear() {
        return new TemporalAdjuster() {
            public final Temporal adjustInto(Temporal temporal) {
                return $m$0(temporal);
            }
        };
    }

    public static TemporalAdjuster lastDayOfYear() {
        return new TemporalAdjuster() {
            public final Temporal adjustInto(Temporal temporal) {
                return $m$0(temporal);
            }
        };
    }

    public static TemporalAdjuster firstDayOfNextYear() {
        return new TemporalAdjuster() {
            public final Temporal adjustInto(Temporal temporal) {
                return $m$0(temporal);
            }
        };
    }

    public static TemporalAdjuster firstInMonth(DayOfWeek dayOfWeek) {
        return dayOfWeekInMonth(1, dayOfWeek);
    }

    public static TemporalAdjuster lastInMonth(DayOfWeek dayOfWeek) {
        return dayOfWeekInMonth(-1, dayOfWeek);
    }

    public static TemporalAdjuster dayOfWeekInMonth(int ordinal, DayOfWeek dayOfWeek) {
        Objects.requireNonNull((Object) dayOfWeek, "dayOfWeek");
        int dowValue = dayOfWeek.getValue();
        if (ordinal >= 0) {
            return new AnonymousClass11(dowValue, ordinal);
        }
        return new AnonymousClass12(dowValue, ordinal);
    }

    static /* synthetic */ Temporal lambda$-java_time_temporal_TemporalAdjusters_15513(int dowValue, int ordinal, Temporal temporal) {
        Temporal temp = temporal.with(ChronoField.DAY_OF_MONTH, temporal.range(ChronoField.DAY_OF_MONTH).getMaximum());
        int daysDiff = dowValue - temp.get(ChronoField.DAY_OF_WEEK);
        if (daysDiff == 0) {
            daysDiff = 0;
        } else if (daysDiff > 0) {
            daysDiff -= 7;
        }
        return temp.plus((long) ((int) (((long) daysDiff) - ((((long) (-ordinal)) - 1) * 7))), ChronoUnit.DAYS);
    }

    public static TemporalAdjuster next(DayOfWeek dayOfWeek) {
        return new AnonymousClass7(dayOfWeek.getValue());
    }

    static /* synthetic */ Temporal lambda$-java_time_temporal_TemporalAdjusters_17076(int dowValue, Temporal temporal) {
        int daysDiff = temporal.get(ChronoField.DAY_OF_WEEK) - dowValue;
        return temporal.plus((long) (daysDiff >= 0 ? 7 - daysDiff : -daysDiff), ChronoUnit.DAYS);
    }

    public static TemporalAdjuster nextOrSame(DayOfWeek dayOfWeek) {
        return new AnonymousClass8(dayOfWeek.getValue());
    }

    static /* synthetic */ Temporal lambda$-java_time_temporal_TemporalAdjusters_18421(int dowValue, Temporal temporal) {
        int calDow = temporal.get(ChronoField.DAY_OF_WEEK);
        if (calDow == dowValue) {
            return temporal;
        }
        int daysDiff = calDow - dowValue;
        return temporal.plus((long) (daysDiff >= 0 ? 7 - daysDiff : -daysDiff), ChronoUnit.DAYS);
    }

    public static TemporalAdjuster previous(DayOfWeek dayOfWeek) {
        return new AnonymousClass9(dayOfWeek.getValue());
    }

    static /* synthetic */ Temporal lambda$-java_time_temporal_TemporalAdjusters_19758(int dowValue, Temporal temporal) {
        int daysDiff = dowValue - temporal.get(ChronoField.DAY_OF_WEEK);
        return temporal.minus((long) (daysDiff >= 0 ? 7 - daysDiff : -daysDiff), ChronoUnit.DAYS);
    }

    public static TemporalAdjuster previousOrSame(DayOfWeek dayOfWeek) {
        return new AnonymousClass10(dayOfWeek.getValue());
    }

    static /* synthetic */ Temporal lambda$-java_time_temporal_TemporalAdjusters_21123(int dowValue, Temporal temporal) {
        int calDow = temporal.get(ChronoField.DAY_OF_WEEK);
        if (calDow == dowValue) {
            return temporal;
        }
        int daysDiff = dowValue - calDow;
        return temporal.minus((long) (daysDiff >= 0 ? 7 - daysDiff : -daysDiff), ChronoUnit.DAYS);
    }
}
