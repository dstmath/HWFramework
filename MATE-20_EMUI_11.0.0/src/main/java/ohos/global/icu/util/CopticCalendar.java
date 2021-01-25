package ohos.global.icu.util;

import java.util.Date;
import java.util.Locale;

public final class CopticCalendar extends CECalendar {
    public static final int AMSHIR = 5;
    public static final int BABA = 1;
    public static final int BARAMHAT = 6;
    public static final int BARAMOUDA = 7;
    public static final int BASHANS = 8;
    private static final int BCE = 0;
    private static final int CE = 1;
    public static final int EPEP = 10;
    public static final int HATOR = 2;
    private static final int JD_EPOCH_OFFSET = 1824665;
    public static final int KIAHK = 3;
    public static final int MESRA = 11;
    public static final int NASIE = 12;
    public static final int PAONA = 9;
    public static final int TOBA = 4;
    public static final int TOUT = 0;
    private static final long serialVersionUID = 5903818751846742911L;

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.CECalendar
    @Deprecated
    public int getJDEpochOffset() {
        return JD_EPOCH_OFFSET;
    }

    @Override // ohos.global.icu.util.Calendar
    public String getType() {
        return "coptic";
    }

    public CopticCalendar() {
    }

    public CopticCalendar(TimeZone timeZone) {
        super(timeZone);
    }

    public CopticCalendar(Locale locale) {
        super(locale);
    }

    public CopticCalendar(ULocale uLocale) {
        super(uLocale);
    }

    public CopticCalendar(TimeZone timeZone, Locale locale) {
        super(timeZone, locale);
    }

    public CopticCalendar(TimeZone timeZone, ULocale uLocale) {
        super(timeZone, uLocale);
    }

    public CopticCalendar(int i, int i2, int i3) {
        super(i, i2, i3);
    }

    public CopticCalendar(Date date) {
        super(date);
    }

    public CopticCalendar(int i, int i2, int i3, int i4, int i5, int i6) {
        super(i, i2, i3, i4, i5, i6);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    @Deprecated
    public int handleGetExtendedYear() {
        if (newerField(19, 1) == 19) {
            return internalGet(19, 1);
        }
        if (internalGet(0, 1) == 0) {
            return 1 - internalGet(1, 1);
        }
        return internalGet(1, 1);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    @Deprecated
    public void handleComputeFields(int i) {
        int i2;
        int i3;
        int[] iArr = new int[3];
        jdToCE(i, getJDEpochOffset(), iArr);
        if (iArr[0] <= 0) {
            i2 = 1 - iArr[0];
            i3 = 0;
        } else {
            i2 = iArr[0];
            i3 = 1;
        }
        internalSet(19, iArr[0]);
        internalSet(0, i3);
        internalSet(1, i2);
        internalSet(2, iArr[1]);
        internalSet(5, iArr[2]);
        internalSet(6, (iArr[1] * 30) + iArr[2]);
    }

    public static int copticToJD(long j, int i, int i2) {
        return ceToJD(j, i, i2, JD_EPOCH_OFFSET);
    }
}
