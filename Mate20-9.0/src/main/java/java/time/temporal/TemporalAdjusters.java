package java.time.temporal;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;
import java.util.function.UnaryOperator;

public final class TemporalAdjusters {
    private TemporalAdjusters() {
    }

    public static TemporalAdjuster ofDateAdjuster(UnaryOperator<LocalDate> dateBasedAdjuster) {
        Objects.requireNonNull(dateBasedAdjuster, "dateBasedAdjuster");
        return new TemporalAdjuster() {
            public final Temporal adjustInto(Temporal temporal) {
                return temporal.with((LocalDate) UnaryOperator.this.apply(LocalDate.from(temporal)));
            }
        };
    }

    public static TemporalAdjuster firstDayOfMonth() {
        return $$Lambda$TemporalAdjusters$8EK8KVP193YLBVkxtkiyg8uZHVo.INSTANCE;
    }

    public static TemporalAdjuster lastDayOfMonth() {
        return $$Lambda$TemporalAdjusters$WAuzLMCz2SwnlcREz0tiYj3XA.INSTANCE;
    }

    public static TemporalAdjuster firstDayOfNextMonth() {
        return $$Lambda$TemporalAdjusters$P7_rZO2XINPKRC8_LcPrXpeSbek.INSTANCE;
    }

    public static TemporalAdjuster firstDayOfYear() {
        return $$Lambda$TemporalAdjusters$w9cWh2WC9cZ6gKDdIl4UmC4HmUM.INSTANCE;
    }

    public static TemporalAdjuster lastDayOfYear() {
        return $$Lambda$TemporalAdjusters$iS4EYkMA1JewgnCHCuVtjW33u14.INSTANCE;
    }

    public static TemporalAdjuster firstDayOfNextYear() {
        return $$Lambda$TemporalAdjusters$lxLY2BERW0kc72bWr7fARmx5Z8.INSTANCE;
    }

    public static TemporalAdjuster firstInMonth(DayOfWeek dayOfWeek) {
        return dayOfWeekInMonth(1, dayOfWeek);
    }

    public static TemporalAdjuster lastInMonth(DayOfWeek dayOfWeek) {
        return dayOfWeekInMonth(-1, dayOfWeek);
    }

    public static TemporalAdjuster dayOfWeekInMonth(int ordinal, DayOfWeek dayOfWeek) {
        Objects.requireNonNull(dayOfWeek, "dayOfWeek");
        int dowValue = dayOfWeek.getValue();
        if (ordinal >= 0) {
            return new TemporalAdjuster(dowValue, ordinal) {
                private final /* synthetic */ int f$0;
                private final /* synthetic */ int f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final Temporal adjustInto(Temporal temporal) {
                    return temporal.with(ChronoField.DAY_OF_MONTH, 1);
                }
            };
        }
        return new TemporalAdjuster(dowValue, ordinal) {
            private final /* synthetic */ int f$0;
            private final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final Temporal adjustInto(Temporal temporal) {
                return TemporalAdjusters.lambda$dayOfWeekInMonth$8(this.f$0, this.f$1, temporal);
            }
        };
    }

    static /* synthetic */ Temporal lambda$dayOfWeekInMonth$8(int dowValue, int ordinal, Temporal temporal) {
        Temporal temp = temporal.with(ChronoField.DAY_OF_MONTH, temporal.range(ChronoField.DAY_OF_MONTH).getMaximum());
        int daysDiff = dowValue - temp.get(ChronoField.DAY_OF_WEEK);
        return temp.plus((long) ((int) (((long) (daysDiff == 0 ? 0 : daysDiff > 0 ? daysDiff - 7 : daysDiff)) - ((((long) (-ordinal)) - 1) * 7))), ChronoUnit.DAYS);
    }

    public static TemporalAdjuster next(DayOfWeek dayOfWeek) {
        return new TemporalAdjuster(dayOfWeek.getValue()) {
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final Temporal adjustInto(Temporal temporal) {
                return TemporalAdjusters.lambda$next$9(this.f$0, temporal);
            }
        };
    }

    static /* synthetic */ Temporal lambda$next$9(int dowValue, Temporal temporal) {
        int daysDiff = temporal.get(ChronoField.DAY_OF_WEEK) - dowValue;
        return temporal.plus((long) (daysDiff >= 0 ? 7 - daysDiff : -daysDiff), ChronoUnit.DAYS);
    }

    public static TemporalAdjuster nextOrSame(DayOfWeek dayOfWeek) {
        return new TemporalAdjuster(dayOfWeek.getValue()) {
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final Temporal adjustInto(Temporal temporal) {
                return TemporalAdjusters.lambda$nextOrSame$10(this.f$0, temporal);
            }
        };
    }

    static /* synthetic */ Temporal lambda$nextOrSame$10(int dowValue, Temporal temporal) {
        int calDow = temporal.get(ChronoField.DAY_OF_WEEK);
        if (calDow == dowValue) {
            return temporal;
        }
        int daysDiff = calDow - dowValue;
        return temporal.plus((long) (daysDiff >= 0 ? 7 - daysDiff : -daysDiff), ChronoUnit.DAYS);
    }

    public static TemporalAdjuster previous(DayOfWeek dayOfWeek) {
        return new TemporalAdjuster(dayOfWeek.getValue()) {
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final Temporal adjustInto(Temporal temporal) {
                return TemporalAdjusters.lambda$previous$11(this.f$0, temporal);
            }
        };
    }

    static /* synthetic */ Temporal lambda$previous$11(int dowValue, Temporal temporal) {
        int daysDiff = dowValue - temporal.get(ChronoField.DAY_OF_WEEK);
        return temporal.minus((long) (daysDiff >= 0 ? 7 - daysDiff : -daysDiff), ChronoUnit.DAYS);
    }

    public static TemporalAdjuster previousOrSame(DayOfWeek dayOfWeek) {
        return new TemporalAdjuster(dayOfWeek.getValue()) {
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final Temporal adjustInto(Temporal temporal) {
                return TemporalAdjusters.lambda$previousOrSame$12(this.f$0, temporal);
            }
        };
    }

    static /* synthetic */ Temporal lambda$previousOrSame$12(int dowValue, Temporal temporal) {
        int calDow = temporal.get(ChronoField.DAY_OF_WEEK);
        if (calDow == dowValue) {
            return temporal;
        }
        int daysDiff = dowValue - calDow;
        return temporal.minus((long) (daysDiff >= 0 ? 7 - daysDiff : -daysDiff), ChronoUnit.DAYS);
    }
}
