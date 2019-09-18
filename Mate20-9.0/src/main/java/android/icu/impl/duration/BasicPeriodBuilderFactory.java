package android.icu.impl.duration;

import android.icu.impl.duration.impl.PeriodFormatterData;
import android.icu.impl.duration.impl.PeriodFormatterDataService;
import java.util.TimeZone;

class BasicPeriodBuilderFactory implements PeriodBuilderFactory {
    private static final short allBits = 255;
    /* access modifiers changed from: private */
    public PeriodFormatterDataService ds;
    private Settings settings = new Settings();

    class Settings {
        boolean allowMillis = true;
        boolean allowZero = true;
        boolean inUse;
        int maxLimit;
        TimeUnit maxUnit = TimeUnit.YEAR;
        int minLimit;
        TimeUnit minUnit = TimeUnit.MILLISECOND;
        short uset = BasicPeriodBuilderFactory.allBits;
        boolean weeksAloneOnly;

        Settings() {
        }

        /* access modifiers changed from: package-private */
        public Settings setUnits(int uset2) {
            if (this.uset == uset2) {
                return this;
            }
            Settings result = this.inUse ? copy() : this;
            result.uset = (short) uset2;
            if ((uset2 & 255) == 255) {
                result.uset = BasicPeriodBuilderFactory.allBits;
                result.maxUnit = TimeUnit.YEAR;
                result.minUnit = TimeUnit.MILLISECOND;
            } else {
                int lastUnit = -1;
                for (int i = 0; i < TimeUnit.units.length; i++) {
                    if (((1 << i) & uset2) != 0) {
                        if (lastUnit == -1) {
                            result.maxUnit = TimeUnit.units[i];
                        }
                        lastUnit = i;
                    }
                }
                if (lastUnit == -1) {
                    result.maxUnit = null;
                    result.minUnit = null;
                } else {
                    result.minUnit = TimeUnit.units[lastUnit];
                }
            }
            return result;
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
            int i = TimeUnit.units.length - 1;
            do {
                i--;
                if (i < 0) {
                    return TimeUnit.SECOND;
                }
            } while ((this.uset & (1 << i)) == 0);
            return TimeUnit.units[i];
        }

        /* access modifiers changed from: package-private */
        public Settings setMaxLimit(float maxLimit2) {
            int val = maxLimit2 <= 0.0f ? 0 : (int) (1000.0f * maxLimit2);
            if (maxLimit2 == ((float) val)) {
                return this;
            }
            Settings result = this.inUse ? copy() : this;
            result.maxLimit = val;
            return result;
        }

        /* access modifiers changed from: package-private */
        public Settings setMinLimit(float minLimit2) {
            int val = minLimit2 <= 0.0f ? 0 : (int) (1000.0f * minLimit2);
            if (minLimit2 == ((float) val)) {
                return this;
            }
            Settings result = this.inUse ? copy() : this;
            result.minLimit = val;
            return result;
        }

        /* access modifiers changed from: package-private */
        public Settings setAllowZero(boolean allow) {
            if (this.allowZero == allow) {
                return this;
            }
            Settings result = this.inUse ? copy() : this;
            result.allowZero = allow;
            return result;
        }

        /* access modifiers changed from: package-private */
        public Settings setWeeksAloneOnly(boolean weeksAlone) {
            if (this.weeksAloneOnly == weeksAlone) {
                return this;
            }
            Settings result = this.inUse ? copy() : this;
            result.weeksAloneOnly = weeksAlone;
            return result;
        }

        /* access modifiers changed from: package-private */
        public Settings setAllowMilliseconds(boolean allowMillis2) {
            if (this.allowMillis == allowMillis2) {
                return this;
            }
            Settings result = this.inUse ? copy() : this;
            result.allowMillis = allowMillis2;
            return result;
        }

        /* access modifiers changed from: package-private */
        public Settings setLocale(String localeName) {
            PeriodFormatterData data = BasicPeriodBuilderFactory.this.ds.get(localeName);
            Settings weeksAloneOnly2 = setAllowZero(data.allowZero()).setWeeksAloneOnly(data.weeksAloneOnly());
            boolean z = true;
            if (data.useMilliseconds() == 1) {
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
        public Period createLimited(long duration, boolean inPast) {
            long eml;
            if (this.maxLimit > 0) {
                if (duration * 1000 > ((long) this.maxLimit) * BasicPeriodBuilderFactory.approximateDurationOf(this.maxUnit)) {
                    return Period.moreThan(((float) this.maxLimit) / 1000.0f, this.maxUnit).inPast(inPast);
                }
            }
            if (this.minLimit > 0) {
                TimeUnit emu = effectiveMinUnit();
                long emud = BasicPeriodBuilderFactory.approximateDurationOf(emu);
                if (emu == this.minUnit) {
                    eml = (long) this.minLimit;
                } else {
                    eml = Math.max(1000, (BasicPeriodBuilderFactory.approximateDurationOf(this.minUnit) * ((long) this.minLimit)) / emud);
                }
                if (1000 * duration < eml * emud) {
                    return Period.lessThan(((float) eml) / 1000.0f, emu).inPast(inPast);
                }
            }
            return null;
        }

        public Settings copy() {
            Settings result = new Settings();
            result.inUse = this.inUse;
            result.uset = this.uset;
            result.maxUnit = this.maxUnit;
            result.minUnit = this.minUnit;
            result.maxLimit = this.maxLimit;
            result.minLimit = this.minLimit;
            result.allowZero = this.allowZero;
            result.weeksAloneOnly = this.weeksAloneOnly;
            result.allowMillis = this.allowMillis;
            return result;
        }
    }

    BasicPeriodBuilderFactory(PeriodFormatterDataService ds2) {
        this.ds = ds2;
    }

    static long approximateDurationOf(TimeUnit unit) {
        return TimeUnit.approxDurations[unit.ordinal];
    }

    public PeriodBuilderFactory setAvailableUnitRange(TimeUnit minUnit, TimeUnit maxUnit) {
        int uset = 0;
        for (int i = maxUnit.ordinal; i <= minUnit.ordinal; i++) {
            uset |= 1 << i;
        }
        if (uset != 0) {
            this.settings = this.settings.setUnits(uset);
            return this;
        }
        throw new IllegalArgumentException("range " + minUnit + " to " + maxUnit + " is empty");
    }

    public PeriodBuilderFactory setUnitIsAvailable(TimeUnit unit, boolean available) {
        int uset;
        int uset2 = this.settings.uset;
        if (available) {
            uset = uset2 | (1 << unit.ordinal);
        } else {
            uset = uset2 & (~(1 << unit.ordinal));
        }
        this.settings = this.settings.setUnits(uset);
        return this;
    }

    public PeriodBuilderFactory setMaxLimit(float maxLimit) {
        this.settings = this.settings.setMaxLimit(maxLimit);
        return this;
    }

    public PeriodBuilderFactory setMinLimit(float minLimit) {
        this.settings = this.settings.setMinLimit(minLimit);
        return this;
    }

    public PeriodBuilderFactory setAllowZero(boolean allow) {
        this.settings = this.settings.setAllowZero(allow);
        return this;
    }

    public PeriodBuilderFactory setWeeksAloneOnly(boolean aloneOnly) {
        this.settings = this.settings.setWeeksAloneOnly(aloneOnly);
        return this;
    }

    public PeriodBuilderFactory setAllowMilliseconds(boolean allow) {
        this.settings = this.settings.setAllowMilliseconds(allow);
        return this;
    }

    public PeriodBuilderFactory setLocale(String localeName) {
        this.settings = this.settings.setLocale(localeName);
        return this;
    }

    public PeriodBuilderFactory setTimeZone(TimeZone timeZone) {
        return this;
    }

    private Settings getSettings() {
        if (this.settings.effectiveSet() == 0) {
            return null;
        }
        return this.settings.setInUse();
    }

    public PeriodBuilder getFixedUnitBuilder(TimeUnit unit) {
        return FixedUnitBuilder.get(unit, getSettings());
    }

    public PeriodBuilder getSingleUnitBuilder() {
        return SingleUnitBuilder.get(getSettings());
    }

    public PeriodBuilder getOneOrTwoUnitBuilder() {
        return OneOrTwoUnitBuilder.get(getSettings());
    }

    public PeriodBuilder getMultiUnitBuilder(int periodCount) {
        return MultiUnitBuilder.get(periodCount, getSettings());
    }
}
