package android.icu.impl.duration;

import android.icu.impl.duration.BasicPeriodFormatterFactory;
import android.icu.impl.duration.impl.PeriodFormatterData;

class BasicPeriodFormatter implements PeriodFormatter {
    private BasicPeriodFormatterFactory.Customizations customs;
    private PeriodFormatterData data;
    private BasicPeriodFormatterFactory factory;
    private String localeName;

    BasicPeriodFormatter(BasicPeriodFormatterFactory factory2, String localeName2, PeriodFormatterData data2, BasicPeriodFormatterFactory.Customizations customs2) {
        this.factory = factory2;
        this.localeName = localeName2;
        this.data = data2;
        this.customs = customs2;
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

    private String format(int tl, boolean inFuture, int[] counts) {
        int i;
        int td;
        boolean wasSkipped;
        int j;
        int[] iArr = counts;
        int mask = 0;
        int i2 = 0;
        while (true) {
            i = 1;
            if (i2 >= iArr.length) {
                break;
            }
            if (iArr[i2] > 0) {
                mask |= 1 << i2;
            }
            i2++;
        }
        if (!this.data.allowZero()) {
            int mask2 = mask;
            int i3 = 0;
            int m = 1;
            while (i3 < iArr.length) {
                if ((mask2 & m) != 0 && iArr[i3] == 1) {
                    mask2 &= ~m;
                }
                i3++;
                m <<= 1;
            }
            if (mask2 == 0) {
                return null;
            }
            mask = mask2;
        }
        int forceD3Seconds = false;
        if (!(this.data.useMilliseconds() == 0 || ((1 << TimeUnit.MILLISECOND.ordinal) & mask) == 0)) {
            int sx = TimeUnit.SECOND.ordinal;
            int mx = TimeUnit.MILLISECOND.ordinal;
            int sf = 1 << sx;
            int mf = 1 << mx;
            switch (this.data.useMilliseconds()) {
                case 1:
                    if ((mask & sf) == 0) {
                        mask |= sf;
                        iArr[sx] = 1;
                    }
                    iArr[sx] = iArr[sx] + ((iArr[mx] - 1) / 1000);
                    mask &= ~mf;
                    forceD3Seconds = true;
                    break;
                case 2:
                    if ((mask & sf) != 0) {
                        iArr[sx] = iArr[sx] + ((iArr[mx] - 1) / 1000);
                        mask &= ~mf;
                        forceD3Seconds = true;
                        break;
                    }
                    break;
            }
        }
        int first = 0;
        int last = iArr.length - 1;
        while (first < iArr.length && ((1 << first) & mask) == 0) {
            first++;
        }
        while (last > first && ((1 << last) & mask) == 0) {
            last--;
        }
        boolean isZero = true;
        int i4 = first;
        while (true) {
            if (i4 <= last) {
                if (((1 << i4) & mask) == 0 || iArr[i4] <= 1) {
                    i4++;
                } else {
                    isZero = false;
                }
            }
        }
        StringBuffer sb = new StringBuffer();
        int tl2 = (!this.customs.displayLimit || isZero) ? 0 : tl;
        if (!this.customs.displayDirection || isZero) {
            td = 0;
        } else {
            td = inFuture ? 2 : 1;
        }
        int td2 = td;
        boolean useDigitPrefix = this.data.appendPrefix(tl2, td2, sb);
        boolean multiple = first != last;
        boolean skipped = false;
        boolean countSep = this.customs.separatorVariant != 0;
        int j2 = first;
        boolean useDigitPrefix2 = useDigitPrefix;
        boolean useDigitPrefix3 = true;
        int i5 = j2;
        while (i5 <= last) {
            if (skipped) {
                this.data.appendSkippedUnit(sb);
                skipped = false;
                wasSkipped = true;
            } else {
                wasSkipped = useDigitPrefix3;
            }
            boolean skipped2 = skipped;
            while (true) {
                j = j2 + 1;
                if (j >= last || (mask & (i << j)) != 0) {
                    TimeUnit unit = TimeUnit.units[i5];
                    int count = iArr[i5] - 1;
                    int cv = this.customs.countVariant;
                } else {
                    skipped2 = true;
                    j2 = j;
                }
            }
            TimeUnit unit2 = TimeUnit.units[i5];
            int count2 = iArr[i5] - 1;
            int cv2 = this.customs.countVariant;
            if (i5 != last) {
                cv2 = 0;
            } else if (forceD3Seconds != 0) {
                cv2 = 5;
            }
            int forceD3Seconds2 = forceD3Seconds;
            int j3 = j;
            int mask3 = mask;
            int td3 = td2;
            boolean isZero2 = isZero;
            int i6 = i5;
            int tl3 = tl2;
            skipped = skipped2 | this.data.appendUnit(unit2, count2, cv2, this.customs.unitVariant, countSep, useDigitPrefix2, multiple, i5 == last, wasSkipped, sb);
            useDigitPrefix3 = false;
            if (this.customs.separatorVariant == 0 || j3 > last) {
                useDigitPrefix2 = false;
            } else {
                useDigitPrefix2 = this.data.appendUnitSeparator(unit2, this.customs.separatorVariant == 2, i6 == first, j3 == last, sb);
            }
            i5 = j3;
            j2 = j3;
            forceD3Seconds = forceD3Seconds2;
            mask = mask3;
            isZero = isZero2;
            td2 = td3;
            tl2 = tl3;
            iArr = counts;
            i = 1;
        }
        int i7 = mask;
        boolean z = isZero;
        this.data.appendSuffix(tl2, td2, sb);
        return sb.toString();
    }
}
