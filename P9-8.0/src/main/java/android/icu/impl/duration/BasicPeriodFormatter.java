package android.icu.impl.duration;

import android.icu.impl.duration.impl.PeriodFormatterData;

class BasicPeriodFormatter implements PeriodFormatter {
    private Customizations customs;
    private PeriodFormatterData data;
    private BasicPeriodFormatterFactory factory;
    private String localeName;

    BasicPeriodFormatter(BasicPeriodFormatterFactory factory, String localeName, PeriodFormatterData data, Customizations customs) {
        this.factory = factory;
        this.localeName = localeName;
        this.data = data;
        this.customs = customs;
    }

    public String format(Period period) {
        if (period.isSet()) {
            return format(period.timeLimit, period.inFuture, period.counts);
        }
        throw new IllegalArgumentException("period is not set");
    }

    public PeriodFormatter withLocale(String locName) {
        if (this.localeName.equals(locName)) {
            return this;
        }
        return new BasicPeriodFormatter(this.factory, locName, this.factory.getData(locName), this.customs);
    }

    /* JADX WARNING: Removed duplicated region for block: B:81:0x0159  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0119  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x015b  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0125  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0130  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String format(int tl, boolean inFuture, int[] counts) {
        int i;
        StringBuffer sb;
        int td;
        boolean useDigitPrefix;
        boolean multiple;
        boolean wasSkipped;
        boolean skipped;
        boolean countSep;
        int j;
        int mask = 0;
        for (i = 0; i < counts.length; i++) {
            if (counts[i] > 0) {
                mask |= 1 << i;
            }
        }
        if (!this.data.allowZero()) {
            i = 0;
            int m = 1;
            while (i < counts.length) {
                if ((mask & m) != 0 && counts[i] == 1) {
                    mask &= ~m;
                }
                i++;
                m <<= 1;
            }
            if (mask == 0) {
                return null;
            }
        }
        boolean forceD3Seconds = false;
        if (!(this.data.useMilliseconds() == 0 || ((1 << TimeUnit.MILLISECOND.ordinal) & mask) == 0)) {
            byte sx = TimeUnit.SECOND.ordinal;
            byte mx = TimeUnit.MILLISECOND.ordinal;
            int sf = 1 << sx;
            int mf = 1 << mx;
            switch (this.data.useMilliseconds()) {
                case 1:
                    if ((mask & sf) == 0) {
                        mask |= sf;
                        counts[sx] = 1;
                    }
                    counts[sx] = counts[sx] + ((counts[mx] - 1) / 1000);
                    mask &= ~mf;
                    forceD3Seconds = true;
                    break;
                case 2:
                    if ((mask & sf) != 0) {
                        counts[sx] = counts[sx] + ((counts[mx] - 1) / 1000);
                        mask &= ~mf;
                        forceD3Seconds = true;
                        break;
                    }
                    break;
            }
        }
        int first = 0;
        int last = counts.length - 1;
        while (first < counts.length && ((1 << first) & mask) == 0) {
            first++;
        }
        while (last > first && ((1 << last) & mask) == 0) {
            last--;
        }
        boolean isZero = true;
        i = first;
        while (i <= last) {
            if (((1 << i) & mask) == 0 || counts[i] <= 1) {
                i++;
            } else {
                isZero = false;
                sb = new StringBuffer();
                if (!this.customs.displayLimit || isZero) {
                    tl = 0;
                }
                td = (this.customs.displayDirection || isZero) ? 0 : inFuture ? 2 : 1;
                useDigitPrefix = this.data.appendPrefix(tl, td, sb);
                multiple = first == last;
                wasSkipped = true;
                skipped = false;
                countSep = this.customs.separatorVariant == (byte) 0;
                i = first;
                j = first;
                while (i <= last) {
                    TimeUnit unit;
                    int count;
                    int cv;
                    if (skipped) {
                        this.data.appendSkippedUnit(sb);
                        skipped = false;
                        wasSkipped = true;
                    }
                    while (true) {
                        j++;
                        if (j >= last || ((1 << j) & mask) != 0) {
                            unit = TimeUnit.units[i];
                            count = counts[i] - 1;
                            cv = this.customs.countVariant;
                        } else {
                            skipped = true;
                        }
                    }
                    unit = TimeUnit.units[i];
                    count = counts[i] - 1;
                    cv = this.customs.countVariant;
                    if (i != last) {
                        cv = 0;
                    } else if (forceD3Seconds) {
                        cv = 5;
                    }
                    skipped |= this.data.appendUnit(unit, count, cv, this.customs.unitVariant, countSep, useDigitPrefix, multiple, i == last, wasSkipped, sb);
                    wasSkipped = false;
                    if (this.customs.separatorVariant == (byte) 0 || j > last) {
                        useDigitPrefix = false;
                    } else {
                        useDigitPrefix = this.data.appendUnitSeparator(unit, this.customs.separatorVariant == (byte) 2, i == first, j == last, sb);
                    }
                    i = j;
                }
                this.data.appendSuffix(tl, td, sb);
                return sb.toString();
            }
        }
        sb = new StringBuffer();
        tl = 0;
        if (this.customs.displayDirection) {
        }
        useDigitPrefix = this.data.appendPrefix(tl, td, sb);
        if (first == last) {
        }
        wasSkipped = true;
        skipped = false;
        if (this.customs.separatorVariant == (byte) 0) {
        }
        i = first;
        j = first;
        while (i <= last) {
        }
        this.data.appendSuffix(tl, td, sb);
        return sb.toString();
    }
}
