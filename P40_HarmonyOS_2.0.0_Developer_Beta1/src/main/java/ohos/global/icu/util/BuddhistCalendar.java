package ohos.global.icu.util;

import java.util.Date;
import java.util.Locale;

public class BuddhistCalendar extends GregorianCalendar {
    public static final int BE = 0;
    private static final int BUDDHIST_ERA_START = -543;
    private static final int GREGORIAN_EPOCH = 1970;
    private static final long serialVersionUID = 2583005278132380631L;

    @Override // ohos.global.icu.util.GregorianCalendar, ohos.global.icu.util.Calendar
    public String getType() {
        return "buddhist";
    }

    public BuddhistCalendar() {
    }

    public BuddhistCalendar(TimeZone timeZone) {
        super(timeZone);
    }

    public BuddhistCalendar(Locale locale) {
        super(locale);
    }

    public BuddhistCalendar(ULocale uLocale) {
        super(uLocale);
    }

    public BuddhistCalendar(TimeZone timeZone, Locale locale) {
        super(timeZone, locale);
    }

    public BuddhistCalendar(TimeZone timeZone, ULocale uLocale) {
        super(timeZone, uLocale);
    }

    public BuddhistCalendar(Date date) {
        this();
        setTime(date);
    }

    public BuddhistCalendar(int i, int i2, int i3) {
        super(i, i2, i3);
    }

    public BuddhistCalendar(int i, int i2, int i3, int i4, int i5, int i6) {
        super(i, i2, i3, i4, i5, i6);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.GregorianCalendar, ohos.global.icu.util.Calendar
    public int handleGetExtendedYear() {
        if (newerField(19, 1) == 19) {
            return internalGet(19, GREGORIAN_EPOCH);
        }
        return internalGet(1, 2513) + BUDDHIST_ERA_START;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.GregorianCalendar, ohos.global.icu.util.Calendar
    public int handleComputeMonthStart(int i, int i2, boolean z) {
        return super.handleComputeMonthStart(i, i2, z);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.GregorianCalendar, ohos.global.icu.util.Calendar
    public void handleComputeFields(int i) {
        super.handleComputeFields(i);
        internalSet(0, 0);
        internalSet(1, internalGet(19) + 543);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.GregorianCalendar, ohos.global.icu.util.Calendar
    public int handleGetLimit(int i, int i2) {
        if (i == 0) {
            return 0;
        }
        return super.handleGetLimit(i, i2);
    }
}
