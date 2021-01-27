package ohos.global.icu.util;

import java.util.Date;
import java.util.Locale;
import ohos.global.icu.impl.CalType;
import ohos.global.icu.impl.EraRules;
import ohos.global.icu.text.SCSU;

public class JapaneseCalendar extends GregorianCalendar {
    public static final int CURRENT_ERA = ERA_RULES.getCurrentEraIndex();
    private static final EraRules ERA_RULES = EraRules.getInstance(CalType.JAPANESE, enableTentativeEra());
    private static final int GREGORIAN_EPOCH = 1970;
    public static final int HEISEI = SCSU.UDEFINE3;
    public static final int MEIJI = SCSU.UDEFINE0;
    public static final int REIWA = SCSU.UDEFINE4;
    public static final int SHOWA = SCSU.UDEFINE2;
    public static final int TAISHO = SCSU.UDEFINE1;
    private static final long serialVersionUID = -2977189902603704691L;

    @Override // ohos.global.icu.util.GregorianCalendar, ohos.global.icu.util.Calendar
    public String getType() {
        return "japanese";
    }

    @Override // ohos.global.icu.util.Calendar
    @Deprecated
    public boolean haveDefaultCentury() {
        return false;
    }

    public JapaneseCalendar() {
    }

    public JapaneseCalendar(TimeZone timeZone) {
        super(timeZone);
    }

    public JapaneseCalendar(Locale locale) {
        super(locale);
    }

    public JapaneseCalendar(ULocale uLocale) {
        super(uLocale);
    }

    public JapaneseCalendar(TimeZone timeZone, Locale locale) {
        super(timeZone, locale);
    }

    public JapaneseCalendar(TimeZone timeZone, ULocale uLocale) {
        super(timeZone, uLocale);
    }

    public JapaneseCalendar(Date date) {
        this();
        setTime(date);
    }

    public JapaneseCalendar(int i, int i2, int i3, int i4) {
        super(i2, i3, i4);
        set(0, i);
    }

    public JapaneseCalendar(int i, int i2, int i3) {
        super(i, i2, i3);
        set(0, CURRENT_ERA);
    }

    public JapaneseCalendar(int i, int i2, int i3, int i4, int i5, int i6) {
        super(i, i2, i3, i4, i5, i6);
        set(0, CURRENT_ERA);
    }

    @Deprecated
    public static boolean enableTentativeEra() {
        String property = System.getProperty("ICU_ENABLE_TENTATIVE_ERA");
        if (property == null) {
            property = System.getenv("ICU_ENABLE_TENTATIVE_ERA");
        }
        if (property != null) {
            return property.equalsIgnoreCase("true");
        }
        return System.getProperty("jdk.calendar.japanese.supplemental.era") != null;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.GregorianCalendar, ohos.global.icu.util.Calendar
    public int handleGetExtendedYear() {
        if (newerField(19, 1) == 19 && newerField(19, 0) == 19) {
            return internalGet(19, GREGORIAN_EPOCH);
        }
        return (internalGet(1, 1) + ERA_RULES.getStartYear(internalGet(0, CURRENT_ERA))) - 1;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public int getDefaultMonthInYear(int i) {
        int[] startDate = ERA_RULES.getStartDate(internalGet(0, CURRENT_ERA), (int[]) null);
        if (i == startDate[0]) {
            return startDate[1] - 1;
        }
        return super.getDefaultMonthInYear(i);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.Calendar
    public int getDefaultDayInMonth(int i, int i2) {
        int[] startDate = ERA_RULES.getStartDate(internalGet(0, CURRENT_ERA), (int[]) null);
        if (i == startDate[0] && i2 == startDate[1] - 1) {
            return startDate[2];
        }
        return super.getDefaultDayInMonth(i, i2);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.GregorianCalendar, ohos.global.icu.util.Calendar
    public void handleComputeFields(int i) {
        super.handleComputeFields(i);
        int internalGet = internalGet(19);
        int eraIndex = ERA_RULES.getEraIndex(internalGet, internalGet(2) + 1, internalGet(5));
        internalSet(0, eraIndex);
        internalSet(1, (internalGet - ERA_RULES.getStartYear(eraIndex)) + 1);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.util.GregorianCalendar, ohos.global.icu.util.Calendar
    public int handleGetLimit(int i, int i2) {
        if (i != 0) {
            if (i == 1) {
                if (i2 == 0 || i2 == 1 || i2 == 2) {
                    return 1;
                }
                if (i2 == 3) {
                    return super.handleGetLimit(i, 3) - ERA_RULES.getStartYear(CURRENT_ERA);
                }
            }
            return super.handleGetLimit(i, i2);
        } else if (i2 == 0 || i2 == 1) {
            return 0;
        } else {
            return ERA_RULES.getNumberOfEras() - 1;
        }
    }

    @Override // ohos.global.icu.util.GregorianCalendar, ohos.global.icu.util.Calendar
    public int getActualMaximum(int i) {
        if (i != 1) {
            return super.getActualMaximum(i);
        }
        int i2 = get(0);
        if (i2 == ERA_RULES.getNumberOfEras() - 1) {
            return handleGetLimit(1, 3);
        }
        int[] startDate = ERA_RULES.getStartDate(i2 + 1, (int[]) null);
        int i3 = startDate[0];
        int i4 = startDate[1];
        int i5 = startDate[2];
        int startYear = (i3 - ERA_RULES.getStartYear(i2)) + 1;
        return (i4 == 1 && i5 == 1) ? startYear - 1 : startYear;
    }
}
