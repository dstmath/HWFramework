package ohos.global.icu.impl.duration;

import ohos.global.icu.impl.duration.BasicPeriodFormatterFactory;
import ohos.global.icu.impl.duration.impl.PeriodFormatterData;

class BasicPeriodFormatter implements PeriodFormatter {
    private BasicPeriodFormatterFactory.Customizations customs;
    private PeriodFormatterData data;
    private BasicPeriodFormatterFactory factory;
    private String localeName;

    BasicPeriodFormatter(BasicPeriodFormatterFactory basicPeriodFormatterFactory, String str, PeriodFormatterData periodFormatterData, BasicPeriodFormatterFactory.Customizations customizations) {
        this.factory = basicPeriodFormatterFactory;
        this.localeName = str;
        this.data = periodFormatterData;
        this.customs = customizations;
    }

    @Override // ohos.global.icu.impl.duration.PeriodFormatter
    public String format(Period period) {
        if (period.isSet()) {
            return format(period.timeLimit, period.inFuture, period.counts);
        }
        throw new IllegalArgumentException("period is not set");
    }

    @Override // ohos.global.icu.impl.duration.PeriodFormatter
    public PeriodFormatter withLocale(String str) {
        if (this.localeName.equals(str)) {
            return this;
        }
        return new BasicPeriodFormatter(this.factory, str, this.factory.getData(str), this.customs);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r19v0, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r20v0, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r20v2, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r20v3, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r19v2, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x00aa A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x009c  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x00d5  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x00d8  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x00e0  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x00e3  */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x00ec  */
    private String format(int i, boolean z, int[] iArr) {
        int i2;
        boolean z2;
        int length;
        int i3;
        int i4;
        boolean z3;
        int i5;
        int i6;
        boolean z4;
        int i7;
        int i8;
        int i9;
        boolean z5;
        int i10 = 0;
        int i11 = 0;
        while (true) {
            i2 = 1;
            if (i10 >= iArr.length) {
                break;
            }
            if (iArr[i10] > 0) {
                i11 |= 1 << i10;
            }
            i10++;
        }
        if (!this.data.allowZero()) {
            int i12 = 1;
            int i13 = 0;
            while (i13 < iArr.length) {
                if ((i11 & i12) != 0 && iArr[i13] == 1) {
                    i11 &= ~i12;
                }
                i13++;
                i12 <<= 1;
            }
            if (i11 == 0) {
                return null;
            }
        }
        if (!(this.data.useMilliseconds() == 0 || ((1 << TimeUnit.MILLISECOND.ordinal) & i11) == 0)) {
            byte b = TimeUnit.SECOND.ordinal;
            byte b2 = TimeUnit.MILLISECOND.ordinal;
            int i14 = 1 << b;
            int i15 = 1 << b2;
            int useMilliseconds = this.data.useMilliseconds();
            if (useMilliseconds == 1) {
                if ((i11 & i14) == 0) {
                    i11 |= i14;
                    iArr[b] = 1;
                }
                iArr[b] = iArr[b] + ((iArr[b2] - 1) / 1000);
            } else if (useMilliseconds == 2 && (i14 & i11) != 0) {
                iArr[b] = iArr[b] + ((iArr[b2] - 1) / 1000);
            }
            i11 &= ~i15;
            z2 = true;
            length = iArr.length - 1;
            i3 = 0;
            while (i3 < iArr.length && ((1 << i3) & i11) == 0) {
                i3++;
            }
            while (length > i3 && ((1 << length) & i11) == 0) {
                length--;
            }
            i4 = i3;
            while (true) {
                if (i4 > length) {
                    if (((1 << i4) & i11) != 0 && iArr[i4] > 1) {
                        z3 = false;
                        break;
                    }
                    i4++;
                } else {
                    z3 = true;
                    break;
                }
            }
            StringBuffer stringBuffer = new StringBuffer();
            int i16 = (this.customs.displayLimit || z3) ? 0 : i;
            int i17 = (this.customs.displayDirection || z3) ? 0 : z ? 2 : 1;
            boolean appendPrefix = this.data.appendPrefix(i16, i17, stringBuffer);
            boolean z6 = i3 == length;
            boolean z7 = this.customs.separatorVariant == 0;
            boolean z8 = true;
            i5 = i3;
            boolean z9 = appendPrefix;
            int i18 = 0;
            while (i5 <= length) {
                if (i18 != 0) {
                    this.data.appendSkippedUnit(stringBuffer);
                    z4 = i2;
                    i7 = i5;
                    i6 = 0;
                } else {
                    i6 = i18;
                    z4 = z8;
                    i7 = i5;
                }
                while (true) {
                    i8 = i7 + 1;
                    if (i8 >= length || ((i2 << i8) & i11) != 0) {
                        break;
                    }
                    i6 = i2;
                    i7 = i8;
                }
                TimeUnit timeUnit = TimeUnit.units[i5];
                int i19 = iArr[i5] - 1;
                int i20 = this.customs.countVariant;
                if (i5 == length) {
                    if (z2) {
                        i20 = 5;
                    }
                    i9 = i20;
                } else {
                    i9 = 0;
                }
                int i21 = i6 | (this.data.appendUnit(timeUnit, i19, i9, this.customs.unitVariant, z7, z9, z6, i5 == length ? i2 : false, z4, stringBuffer) ? 1 : 0);
                if (this.customs.separatorVariant == 0 || i8 > length) {
                    z5 = false;
                } else {
                    z5 = this.data.appendUnitSeparator(timeUnit, this.customs.separatorVariant == 2, i5 == i3, i8 == length, stringBuffer);
                }
                stringBuffer = stringBuffer;
                i5 = i8;
                i18 = i21;
                i16 = i16;
                z8 = false;
                z9 = z5;
                i2 = 1;
            }
            this.data.appendSuffix(i16, i17, stringBuffer);
            return stringBuffer.toString();
        }
        z2 = false;
        length = iArr.length - 1;
        i3 = 0;
        while (i3 < iArr.length) {
            i3++;
        }
        while (length > i3) {
            length--;
        }
        i4 = i3;
        while (true) {
            if (i4 > length) {
            }
            i4++;
        }
        StringBuffer stringBuffer2 = new StringBuffer();
        if (this.customs.displayLimit) {
        }
        if (this.customs.displayDirection) {
        }
        boolean appendPrefix2 = this.data.appendPrefix(i16, i17, stringBuffer2);
        if (i3 == length) {
        }
        if (this.customs.separatorVariant == 0) {
        }
        boolean z82 = true;
        i5 = i3;
        boolean z92 = appendPrefix2;
        int i182 = 0;
        while (i5 <= length) {
        }
        this.data.appendSuffix(i16, i17, stringBuffer2);
        return stringBuffer2.toString();
    }
}
