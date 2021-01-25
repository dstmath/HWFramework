package ohos.global.icu.impl;

import java.util.HashMap;
import java.util.Map;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.global.icu.impl.UResource;
import ohos.global.icu.util.ICUException;
import ohos.global.icu.util.ULocale;

public final class DayPeriodRules {
    private static final DayPeriodRulesData DATA = loadData();
    private DayPeriod[] dayPeriodForHour;
    private boolean hasMidnight;
    private boolean hasNoon;

    public enum DayPeriod {
        MIDNIGHT,
        NOON,
        MORNING1,
        AFTERNOON1,
        EVENING1,
        NIGHT1,
        MORNING2,
        AFTERNOON2,
        EVENING2,
        NIGHT2,
        AM,
        PM;
        
        public static DayPeriod[] VALUES = values();

        /* access modifiers changed from: private */
        public static DayPeriod fromStringOrNull(CharSequence charSequence) {
            if ("midnight".contentEquals(charSequence)) {
                return MIDNIGHT;
            }
            if ("noon".contentEquals(charSequence)) {
                return NOON;
            }
            if ("morning1".contentEquals(charSequence)) {
                return MORNING1;
            }
            if ("afternoon1".contentEquals(charSequence)) {
                return AFTERNOON1;
            }
            if ("evening1".contentEquals(charSequence)) {
                return EVENING1;
            }
            if ("night1".contentEquals(charSequence)) {
                return NIGHT1;
            }
            if ("morning2".contentEquals(charSequence)) {
                return MORNING2;
            }
            if ("afternoon2".contentEquals(charSequence)) {
                return AFTERNOON2;
            }
            if ("evening2".contentEquals(charSequence)) {
                return EVENING2;
            }
            if ("night2".contentEquals(charSequence)) {
                return NIGHT2;
            }
            if ("am".contentEquals(charSequence)) {
                return AM;
            }
            if ("pm".contentEquals(charSequence)) {
                return PM;
            }
            return null;
        }
    }

    /* access modifiers changed from: private */
    public enum CutoffType {
        BEFORE,
        AFTER,
        FROM,
        AT;

        /* access modifiers changed from: private */
        public static CutoffType fromStringOrNull(CharSequence charSequence) {
            if (Constants.ATTRNAME_FROM.contentEquals(charSequence)) {
                return FROM;
            }
            if ("before".contentEquals(charSequence)) {
                return BEFORE;
            }
            if ("after".contentEquals(charSequence)) {
                return AFTER;
            }
            if ("at".contentEquals(charSequence)) {
                return AT;
            }
            return null;
        }
    }

    /* access modifiers changed from: private */
    public static final class DayPeriodRulesData {
        Map<String, Integer> localesToRuleSetNumMap;
        int maxRuleSetNum;
        DayPeriodRules[] rules;

        private DayPeriodRulesData() {
            this.localesToRuleSetNumMap = new HashMap();
            this.maxRuleSetNum = -1;
        }
    }

    /* access modifiers changed from: private */
    public static final class DayPeriodRulesDataSink extends UResource.Sink {
        private CutoffType cutoffType;
        private int[] cutoffs;
        private DayPeriodRulesData data;
        private DayPeriod period;
        private int ruleSetNum;

        private DayPeriodRulesDataSink(DayPeriodRulesData dayPeriodRulesData) {
            this.cutoffs = new int[25];
            this.data = dayPeriodRulesData;
        }

        @Override // ohos.global.icu.impl.UResource.Sink
        public void put(UResource.Key key, UResource.Value value, boolean z) {
            UResource.Table table = value.getTable();
            for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                if (key.contentEquals("locales")) {
                    UResource.Table table2 = value.getTable();
                    for (int i2 = 0; table2.getKeyAndValue(i2, key, value); i2++) {
                        this.data.localesToRuleSetNumMap.put(key.toString(), Integer.valueOf(DayPeriodRules.parseSetNum(value.getString())));
                    }
                } else if (key.contentEquals("rules")) {
                    processRules(value.getTable(), key, value);
                }
            }
        }

        private void processRules(UResource.Table table, UResource.Key key, UResource.Value value) {
            for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                this.ruleSetNum = DayPeriodRules.parseSetNum(key.toString());
                this.data.rules[this.ruleSetNum] = new DayPeriodRules();
                UResource.Table table2 = value.getTable();
                for (int i2 = 0; table2.getKeyAndValue(i2, key, value); i2++) {
                    this.period = DayPeriod.fromStringOrNull(key);
                    if (this.period != null) {
                        UResource.Table table3 = value.getTable();
                        for (int i3 = 0; table3.getKeyAndValue(i3, key, value); i3++) {
                            if (value.getType() == 0) {
                                addCutoff(CutoffType.fromStringOrNull(key), value.getString());
                            } else {
                                this.cutoffType = CutoffType.fromStringOrNull(key);
                                UResource.Array array = value.getArray();
                                int size = array.getSize();
                                for (int i4 = 0; i4 < size; i4++) {
                                    array.getValue(i4, value);
                                    addCutoff(this.cutoffType, value.getString());
                                }
                            }
                        }
                        setDayPeriodForHoursFromCutoffs();
                        int i5 = 0;
                        while (true) {
                            int[] iArr = this.cutoffs;
                            if (i5 >= iArr.length) {
                                break;
                            }
                            iArr[i5] = 0;
                            i5++;
                        }
                    } else {
                        throw new ICUException("Unknown day period in data.");
                    }
                }
                for (DayPeriod dayPeriod : this.data.rules[this.ruleSetNum].dayPeriodForHour) {
                    if (dayPeriod == null) {
                        throw new ICUException("Rules in data don't cover all 24 hours (they should).");
                    }
                }
            }
        }

        private void addCutoff(CutoffType cutoffType2, String str) {
            if (cutoffType2 != null) {
                int parseHour = parseHour(str);
                int[] iArr = this.cutoffs;
                iArr[parseHour] = (1 << cutoffType2.ordinal()) | iArr[parseHour];
                return;
            }
            throw new ICUException("Cutoff type not recognized.");
        }

        private void setDayPeriodForHoursFromCutoffs() {
            DayPeriodRules dayPeriodRules = this.data.rules[this.ruleSetNum];
            for (int i = 0; i <= 24; i++) {
                if ((this.cutoffs[i] & (1 << CutoffType.AT.ordinal())) > 0) {
                    if (i == 0 && this.period == DayPeriod.MIDNIGHT) {
                        dayPeriodRules.hasMidnight = true;
                    } else if (i == 12 && this.period == DayPeriod.NOON) {
                        dayPeriodRules.hasNoon = true;
                    } else {
                        throw new ICUException("AT cutoff must only be set for 0:00 or 12:00.");
                    }
                }
                if ((this.cutoffs[i] & (1 << CutoffType.FROM.ordinal())) > 0 || (this.cutoffs[i] & (1 << CutoffType.AFTER.ordinal())) > 0) {
                    int i2 = i + 1;
                    while (i2 != i) {
                        if (i2 == 25) {
                            i2 = 0;
                        }
                        if ((this.cutoffs[i2] & (1 << CutoffType.BEFORE.ordinal())) > 0) {
                            dayPeriodRules.add(i, i2, this.period);
                        } else {
                            i2++;
                        }
                    }
                    throw new ICUException("FROM/AFTER cutoffs must have a matching BEFORE cutoff.");
                }
            }
        }

        private static int parseHour(String str) {
            int indexOf = str.indexOf(58);
            if (indexOf < 0 || !str.substring(indexOf).equals(":00")) {
                throw new ICUException("Cutoff time must end in \":00\".");
            }
            String substring = str.substring(0, indexOf);
            if (indexOf == 1 || indexOf == 2) {
                int parseInt = Integer.parseInt(substring);
                if (parseInt >= 0 && parseInt <= 24) {
                    return parseInt;
                }
                throw new ICUException("Cutoff hour must be between 0 and 24, inclusive.");
            }
            throw new ICUException("Cutoff time must begin with h: or hh:");
        }
    }

    /* access modifiers changed from: private */
    public static class DayPeriodRulesCountSink extends UResource.Sink {
        private DayPeriodRulesData data;

        private DayPeriodRulesCountSink(DayPeriodRulesData dayPeriodRulesData) {
            this.data = dayPeriodRulesData;
        }

        @Override // ohos.global.icu.impl.UResource.Sink
        public void put(UResource.Key key, UResource.Value value, boolean z) {
            UResource.Table table = value.getTable();
            for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                int parseSetNum = DayPeriodRules.parseSetNum(key.toString());
                if (parseSetNum > this.data.maxRuleSetNum) {
                    this.data.maxRuleSetNum = parseSetNum;
                }
            }
        }
    }

    private DayPeriodRules() {
        this.hasMidnight = false;
        this.hasNoon = false;
        this.dayPeriodForHour = new DayPeriod[24];
    }

    public static DayPeriodRules getInstance(ULocale uLocale) {
        String baseName = uLocale.getBaseName();
        if (baseName.isEmpty()) {
            baseName = Constants.ELEMNAME_ROOT_STRING;
        }
        String str = baseName;
        Integer num = null;
        while (num == null) {
            num = DATA.localesToRuleSetNumMap.get(str);
            if (num != null) {
                break;
            }
            str = ULocale.getFallback(str);
            if (str.isEmpty()) {
                break;
            }
        }
        if (num == null || DATA.rules[num.intValue()] == null) {
            return null;
        }
        return DATA.rules[num.intValue()];
    }

    public double getMidPointForDayPeriod(DayPeriod dayPeriod) {
        int startHourForDayPeriod = getStartHourForDayPeriod(dayPeriod);
        int endHourForDayPeriod = getEndHourForDayPeriod(dayPeriod);
        double d = ((double) (startHourForDayPeriod + endHourForDayPeriod)) / 2.0d;
        if (startHourForDayPeriod <= endHourForDayPeriod) {
            return d;
        }
        double d2 = d + 12.0d;
        return d2 >= 24.0d ? d2 - 24.0d : d2;
    }

    private static DayPeriodRulesData loadData() {
        DayPeriodRulesData dayPeriodRulesData = new DayPeriodRulesData();
        ICUResourceBundle bundleInstance = ICUResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "dayPeriods", ICUResourceBundle.ICU_DATA_CLASS_LOADER, true);
        bundleInstance.getAllItemsWithFallback("rules", new DayPeriodRulesCountSink(dayPeriodRulesData));
        dayPeriodRulesData.rules = new DayPeriodRules[(dayPeriodRulesData.maxRuleSetNum + 1)];
        bundleInstance.getAllItemsWithFallback("", new DayPeriodRulesDataSink(dayPeriodRulesData));
        return dayPeriodRulesData;
    }

    private int getStartHourForDayPeriod(DayPeriod dayPeriod) throws IllegalArgumentException {
        if (dayPeriod == DayPeriod.MIDNIGHT) {
            return 0;
        }
        if (dayPeriod == DayPeriod.NOON) {
            return 12;
        }
        DayPeriod[] dayPeriodArr = this.dayPeriodForHour;
        if (dayPeriodArr[0] == dayPeriod && dayPeriodArr[23] == dayPeriod) {
            for (int i = 22; i >= 1; i--) {
                if (this.dayPeriodForHour[i] != dayPeriod) {
                    return i + 1;
                }
            }
        } else {
            for (int i2 = 0; i2 <= 23; i2++) {
                if (this.dayPeriodForHour[i2] == dayPeriod) {
                    return i2;
                }
            }
        }
        throw new IllegalArgumentException();
    }

    private int getEndHourForDayPeriod(DayPeriod dayPeriod) {
        if (dayPeriod == DayPeriod.MIDNIGHT) {
            return 0;
        }
        if (dayPeriod == DayPeriod.NOON) {
            return 12;
        }
        DayPeriod[] dayPeriodArr = this.dayPeriodForHour;
        if (dayPeriodArr[0] == dayPeriod && dayPeriodArr[23] == dayPeriod) {
            for (int i = 1; i <= 22; i++) {
                if (this.dayPeriodForHour[i] != dayPeriod) {
                    return i;
                }
            }
        } else {
            for (int i2 = 23; i2 >= 0; i2--) {
                if (this.dayPeriodForHour[i2] == dayPeriod) {
                    return i2 + 1;
                }
            }
        }
        throw new IllegalArgumentException();
    }

    public boolean hasMidnight() {
        return this.hasMidnight;
    }

    public boolean hasNoon() {
        return this.hasNoon;
    }

    public DayPeriod getDayPeriodForHour(int i) {
        return this.dayPeriodForHour[i];
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void add(int i, int i2, DayPeriod dayPeriod) {
        while (i != i2) {
            if (i == 24) {
                i = 0;
            }
            this.dayPeriodForHour[i] = dayPeriod;
            i++;
        }
    }

    /* access modifiers changed from: private */
    public static int parseSetNum(String str) {
        if (str.startsWith("set")) {
            return Integer.parseInt(str.substring(3));
        }
        throw new ICUException("Set number should start with \"set\".");
    }
}
