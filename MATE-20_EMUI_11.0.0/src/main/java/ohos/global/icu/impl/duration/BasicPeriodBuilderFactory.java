package ohos.global.icu.impl.duration;

import java.util.TimeZone;
import ohos.global.icu.impl.duration.impl.PeriodFormatterData;
import ohos.global.icu.impl.duration.impl.PeriodFormatterDataService;

/* access modifiers changed from: package-private */
public class BasicPeriodBuilderFactory implements PeriodBuilderFactory {
    private static final short allBits = 255;
    private PeriodFormatterDataService ds;
    private Settings settings = new Settings();

    @Override // ohos.global.icu.impl.duration.PeriodBuilderFactory
    public PeriodBuilderFactory setTimeZone(TimeZone timeZone) {
        return this;
    }

    BasicPeriodBuilderFactory(PeriodFormatterDataService periodFormatterDataService) {
        this.ds = periodFormatterDataService;
    }

    static long approximateDurationOf(TimeUnit timeUnit) {
        return TimeUnit.approxDurations[timeUnit.ordinal];
    }

    /* access modifiers changed from: package-private */
    public class Settings {
        boolean allowMillis = true;
        boolean allowZero = true;
        boolean inUse;
        int maxLimit;
        TimeUnit maxUnit = TimeUnit.YEAR;
        int minLimit;
        TimeUnit minUnit = TimeUnit.MILLISECOND;
        short uset = 255;
        boolean weeksAloneOnly;

        Settings() {
        }

        /* access modifiers changed from: package-private */
        public Settings setUnits(int i) {
            if (this.uset == i) {
                return this;
            }
            if (this.inUse) {
                this = copy();
            }
            this.uset = (short) i;
            if ((i & 255) == 255) {
                this.uset = 255;
                this.maxUnit = TimeUnit.YEAR;
                this.minUnit = TimeUnit.MILLISECOND;
            } else {
                int i2 = -1;
                for (int i3 = 0; i3 < TimeUnit.units.length; i3++) {
                    if (((1 << i3) & i) != 0) {
                        if (i2 == -1) {
                            this.maxUnit = TimeUnit.units[i3];
                        }
                        i2 = i3;
                    }
                }
                if (i2 == -1) {
                    this.maxUnit = null;
                    this.minUnit = null;
                } else {
                    this.minUnit = TimeUnit.units[i2];
                }
            }
            return this;
        }

        /* access modifiers changed from: package-private */
        public short effectiveSet() {
            if (this.allowMillis) {
                return this.uset;
            }
            return (short) (this.uset & (~(1 << TimeUnit.MILLISECOND.ordinal)));
        }

        /* access modifiers changed from: package-private */
        public TimeUnit effectiveMinUnit() {
            if (this.allowMillis || this.minUnit != TimeUnit.MILLISECOND) {
                return this.minUnit;
            }
            int length = TimeUnit.units.length - 1;
            do {
                length--;
                if (length < 0) {
                    return TimeUnit.SECOND;
                }
            } while ((this.uset & (1 << length)) == 0);
            return TimeUnit.units[length];
        }

        /* access modifiers changed from: package-private */
        public Settings setMaxLimit(float f) {
            int i = f <= 0.0f ? 0 : (int) (1000.0f * f);
            if (f == ((float) i)) {
                return this;
            }
            if (this.inUse) {
                this = copy();
            }
            this.maxLimit = i;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Settings setMinLimit(float f) {
            int i = f <= 0.0f ? 0 : (int) (1000.0f * f);
            if (f == ((float) i)) {
                return this;
            }
            if (this.inUse) {
                this = copy();
            }
            this.minLimit = i;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Settings setAllowZero(boolean z) {
            if (this.allowZero == z) {
                return this;
            }
            if (this.inUse) {
                this = copy();
            }
            this.allowZero = z;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Settings setWeeksAloneOnly(boolean z) {
            if (this.weeksAloneOnly == z) {
                return this;
            }
            if (this.inUse) {
                this = copy();
            }
            this.weeksAloneOnly = z;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Settings setAllowMilliseconds(boolean z) {
            if (this.allowMillis == z) {
                return this;
            }
            if (this.inUse) {
                this = copy();
            }
            this.allowMillis = z;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Settings setLocale(String str) {
            PeriodFormatterData periodFormatterData = BasicPeriodBuilderFactory.this.ds.get(str);
            Settings weeksAloneOnly2 = setAllowZero(periodFormatterData.allowZero()).setWeeksAloneOnly(periodFormatterData.weeksAloneOnly());
            boolean z = true;
            if (periodFormatterData.useMilliseconds() == 1) {
                z = false;
            }
            return weeksAloneOnly2.setAllowMilliseconds(z);
        }

        /* access modifiers changed from: package-private */
        public Settings setInUse() {
            this.inUse = true;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Period createLimited(long j, boolean z) {
            long j2;
            if (this.maxLimit > 0) {
                long approximateDurationOf = BasicPeriodBuilderFactory.approximateDurationOf(this.maxUnit);
                int i = this.maxLimit;
                if (j * 1000 > ((long) i) * approximateDurationOf) {
                    return Period.moreThan(((float) i) / 1000.0f, this.maxUnit).inPast(z);
                }
            }
            if (this.minLimit <= 0) {
                return null;
            }
            TimeUnit effectiveMinUnit = effectiveMinUnit();
            long approximateDurationOf2 = BasicPeriodBuilderFactory.approximateDurationOf(effectiveMinUnit);
            TimeUnit timeUnit = this.minUnit;
            if (effectiveMinUnit == timeUnit) {
                j2 = (long) this.minLimit;
            } else {
                j2 = Math.max(1000L, (BasicPeriodBuilderFactory.approximateDurationOf(timeUnit) * ((long) this.minLimit)) / approximateDurationOf2);
            }
            if (j * 1000 < approximateDurationOf2 * j2) {
                return Period.lessThan(((float) j2) / 1000.0f, effectiveMinUnit).inPast(z);
            }
            return null;
        }

        public Settings copy() {
            Settings settings = new Settings();
            settings.inUse = this.inUse;
            settings.uset = this.uset;
            settings.maxUnit = this.maxUnit;
            settings.minUnit = this.minUnit;
            settings.maxLimit = this.maxLimit;
            settings.minLimit = this.minLimit;
            settings.allowZero = this.allowZero;
            settings.weeksAloneOnly = this.weeksAloneOnly;
            settings.allowMillis = this.allowMillis;
            return settings;
        }
    }

    @Override // ohos.global.icu.impl.duration.PeriodBuilderFactory
    public PeriodBuilderFactory setAvailableUnitRange(TimeUnit timeUnit, TimeUnit timeUnit2) {
        int i = 0;
        for (int i2 = timeUnit2.ordinal; i2 <= timeUnit.ordinal; i2++) {
            i |= 1 << i2;
        }
        if (i != 0) {
            this.settings = this.settings.setUnits(i);
            return this;
        }
        throw new IllegalArgumentException("range " + timeUnit + " to " + timeUnit2 + " is empty");
    }

    @Override // ohos.global.icu.impl.duration.PeriodBuilderFactory
    public PeriodBuilderFactory setUnitIsAvailable(TimeUnit timeUnit, boolean z) {
        int i;
        short s = this.settings.uset;
        if (z) {
            i = (1 << timeUnit.ordinal) | s;
        } else {
            i = (~(1 << timeUnit.ordinal)) & s;
        }
        this.settings = this.settings.setUnits(i);
        return this;
    }

    @Override // ohos.global.icu.impl.duration.PeriodBuilderFactory
    public PeriodBuilderFactory setMaxLimit(float f) {
        this.settings = this.settings.setMaxLimit(f);
        return this;
    }

    @Override // ohos.global.icu.impl.duration.PeriodBuilderFactory
    public PeriodBuilderFactory setMinLimit(float f) {
        this.settings = this.settings.setMinLimit(f);
        return this;
    }

    @Override // ohos.global.icu.impl.duration.PeriodBuilderFactory
    public PeriodBuilderFactory setAllowZero(boolean z) {
        this.settings = this.settings.setAllowZero(z);
        return this;
    }

    @Override // ohos.global.icu.impl.duration.PeriodBuilderFactory
    public PeriodBuilderFactory setWeeksAloneOnly(boolean z) {
        this.settings = this.settings.setWeeksAloneOnly(z);
        return this;
    }

    @Override // ohos.global.icu.impl.duration.PeriodBuilderFactory
    public PeriodBuilderFactory setAllowMilliseconds(boolean z) {
        this.settings = this.settings.setAllowMilliseconds(z);
        return this;
    }

    @Override // ohos.global.icu.impl.duration.PeriodBuilderFactory
    public PeriodBuilderFactory setLocale(String str) {
        this.settings = this.settings.setLocale(str);
        return this;
    }

    private Settings getSettings() {
        if (this.settings.effectiveSet() == 0) {
            return null;
        }
        return this.settings.setInUse();
    }

    @Override // ohos.global.icu.impl.duration.PeriodBuilderFactory
    public PeriodBuilder getFixedUnitBuilder(TimeUnit timeUnit) {
        return FixedUnitBuilder.get(timeUnit, getSettings());
    }

    @Override // ohos.global.icu.impl.duration.PeriodBuilderFactory
    public PeriodBuilder getSingleUnitBuilder() {
        return SingleUnitBuilder.get(getSettings());
    }

    @Override // ohos.global.icu.impl.duration.PeriodBuilderFactory
    public PeriodBuilder getOneOrTwoUnitBuilder() {
        return OneOrTwoUnitBuilder.get(getSettings());
    }

    @Override // ohos.global.icu.impl.duration.PeriodBuilderFactory
    public PeriodBuilder getMultiUnitBuilder(int i) {
        return MultiUnitBuilder.get(i, getSettings());
    }
}
