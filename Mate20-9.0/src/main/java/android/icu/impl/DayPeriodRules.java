package android.icu.impl;

import android.icu.impl.UResource;
import android.icu.util.ICUException;
import android.icu.util.ULocale;
import java.util.HashMap;
import java.util.Map;

public final class DayPeriodRules {
    private static final DayPeriodRulesData DATA = loadData();
    /* access modifiers changed from: private */
    public DayPeriod[] dayPeriodForHour;
    /* access modifiers changed from: private */
    public boolean hasMidnight;
    /* access modifiers changed from: private */
    public boolean hasNoon;

    private enum CutoffType {
        BEFORE,
        AFTER,
        FROM,
        AT;

        /* access modifiers changed from: private */
        public static CutoffType fromStringOrNull(CharSequence str) {
            if ("from".contentEquals(str)) {
                return FROM;
            }
            if ("before".contentEquals(str)) {
                return BEFORE;
            }
            if ("after".contentEquals(str)) {
                return AFTER;
            }
            if ("at".contentEquals(str)) {
                return AT;
            }
            return null;
        }
    }

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
        
        public static DayPeriod[] VALUES;

        static {
            VALUES = values();
        }

        /* access modifiers changed from: private */
        public static DayPeriod fromStringOrNull(CharSequence str) {
            if ("midnight".contentEquals(str)) {
                return MIDNIGHT;
            }
            if ("noon".contentEquals(str)) {
                return NOON;
            }
            if ("morning1".contentEquals(str)) {
                return MORNING1;
            }
            if ("afternoon1".contentEquals(str)) {
                return AFTERNOON1;
            }
            if ("evening1".contentEquals(str)) {
                return EVENING1;
            }
            if ("night1".contentEquals(str)) {
                return NIGHT1;
            }
            if ("morning2".contentEquals(str)) {
                return MORNING2;
            }
            if ("afternoon2".contentEquals(str)) {
                return AFTERNOON2;
            }
            if ("evening2".contentEquals(str)) {
                return EVENING2;
            }
            if ("night2".contentEquals(str)) {
                return NIGHT2;
            }
            if ("am".contentEquals(str)) {
                return AM;
            }
            if ("pm".contentEquals(str)) {
                return PM;
            }
            return null;
        }
    }

    private static class DayPeriodRulesCountSink extends UResource.Sink {
        private DayPeriodRulesData data;

        private DayPeriodRulesCountSink(DayPeriodRulesData data2) {
            this.data = data2;
        }

        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Table rules = value.getTable();
            for (int i = 0; rules.getKeyAndValue(i, key, value); i++) {
                int setNum = DayPeriodRules.parseSetNum(key.toString());
                if (setNum > this.data.maxRuleSetNum) {
                    this.data.maxRuleSetNum = setNum;
                }
            }
        }
    }

    private static final class DayPeriodRulesData {
        Map<String, Integer> localesToRuleSetNumMap;
        int maxRuleSetNum;
        DayPeriodRules[] rules;

        private DayPeriodRulesData() {
            this.localesToRuleSetNumMap = new HashMap();
            this.maxRuleSetNum = -1;
        }
    }

    private static final class DayPeriodRulesDataSink extends UResource.Sink {
        private CutoffType cutoffType;
        private int[] cutoffs;
        private DayPeriodRulesData data;
        private DayPeriod period;
        private int ruleSetNum;

        private DayPeriodRulesDataSink(DayPeriodRulesData data2) {
            this.cutoffs = new int[25];
            this.data = data2;
        }

        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Table dayPeriodData = value.getTable();
            for (int i = 0; dayPeriodData.getKeyAndValue(i, key, value); i++) {
                if (key.contentEquals("locales")) {
                    UResource.Table locales = value.getTable();
                    for (int j = 0; locales.getKeyAndValue(j, key, value); j++) {
                        this.data.localesToRuleSetNumMap.put(key.toString(), Integer.valueOf(DayPeriodRules.parseSetNum(value.getString())));
                    }
                } else if (key.contentEquals("rules")) {
                    processRules(value.getTable(), key, value);
                }
            }
        }

        private void processRules(UResource.Table rules, UResource.Key key, UResource.Value value) {
            for (int i = 0; rules.getKeyAndValue(i, key, value); i++) {
                this.ruleSetNum = DayPeriodRules.parseSetNum(key.toString());
                this.data.rules[this.ruleSetNum] = new DayPeriodRules();
                UResource.Table ruleSet = value.getTable();
                int j = 0;
                while (ruleSet.getKeyAndValue(j, key, value)) {
                    this.period = DayPeriod.fromStringOrNull(key);
                    if (this.period != null) {
                        UResource.Table periodDefinition = value.getTable();
                        for (int k = 0; periodDefinition.getKeyAndValue(k, key, value); k++) {
                            if (value.getType() == 0) {
                                addCutoff(CutoffType.fromStringOrNull(key), value.getString());
                            } else {
                                this.cutoffType = CutoffType.fromStringOrNull(key);
                                UResource.Array cutoffArray = value.getArray();
                                int length = cutoffArray.getSize();
                                for (int l = 0; l < length; l++) {
                                    cutoffArray.getValue(l, value);
                                    addCutoff(this.cutoffType, value.getString());
                                }
                            }
                        }
                        setDayPeriodForHoursFromCutoffs();
                        for (int k2 = 0; k2 < this.cutoffs.length; k2++) {
                            this.cutoffs[k2] = 0;
                        }
                        j++;
                    } else {
                        throw new ICUException("Unknown day period in data.");
                    }
                }
                DayPeriod[] access$400 = this.data.rules[this.ruleSetNum].dayPeriodForHour;
                int length2 = access$400.length;
                int i2 = 0;
                while (i2 < length2) {
                    if (access$400[i2] != null) {
                        i2++;
                    } else {
                        throw new ICUException("Rules in data don't cover all 24 hours (they should).");
                    }
                }
            }
        }

        private void addCutoff(CutoffType type, String hourStr) {
            if (type != null) {
                int hour = parseHour(hourStr);
                int[] iArr = this.cutoffs;
                iArr[hour] = iArr[hour] | (1 << type.ordinal());
                return;
            }
            throw new ICUException("Cutoff type not recognized.");
        }

        private void setDayPeriodForHoursFromCutoffs() {
            DayPeriodRules rule = this.data.rules[this.ruleSetNum];
            for (int startHour = 0; startHour <= 24; startHour++) {
                if ((this.cutoffs[startHour] & (1 << CutoffType.AT.ordinal())) > 0) {
                    if (startHour == 0 && this.period == DayPeriod.MIDNIGHT) {
                        boolean unused = rule.hasMidnight = true;
                    } else if (startHour == 12 && this.period == DayPeriod.NOON) {
                        boolean unused2 = rule.hasNoon = true;
                    } else {
                        throw new ICUException("AT cutoff must only be set for 0:00 or 12:00.");
                    }
                }
                if ((this.cutoffs[startHour] & (1 << CutoffType.FROM.ordinal())) > 0 || (this.cutoffs[startHour] & (1 << CutoffType.AFTER.ordinal())) > 0) {
                    int hour = startHour + 1;
                    while (hour != startHour) {
                        if (hour == 25) {
                            hour = 0;
                        }
                        if ((this.cutoffs[hour] & (1 << CutoffType.BEFORE.ordinal())) > 0) {
                            rule.add(startHour, hour, this.period);
                        } else {
                            hour++;
                        }
                    }
                    throw new ICUException("FROM/AFTER cutoffs must have a matching BEFORE cutoff.");
                }
            }
        }

        private static int parseHour(String str) {
            int firstColonPos = str.indexOf(58);
            if (firstColonPos < 0 || !str.substring(firstColonPos).equals(":00")) {
                throw new ICUException("Cutoff time must end in \":00\".");
            }
            String hourStr = str.substring(0, firstColonPos);
            if (firstColonPos == 1 || firstColonPos == 2) {
                int hour = Integer.parseInt(hourStr);
                if (hour >= 0 && hour <= 24) {
                    return hour;
                }
                throw new ICUException("Cutoff hour must be between 0 and 24, inclusive.");
            }
            throw new ICUException("Cutoff time must begin with h: or hh:");
        }
    }

    private DayPeriodRules() {
        this.hasMidnight = false;
        this.hasNoon = false;
        this.dayPeriodForHour = new DayPeriod[24];
    }

    public static DayPeriodRules getInstance(ULocale locale) {
        String localeCode = locale.getBaseName();
        if (localeCode.isEmpty()) {
            localeCode = "root";
        }
        String localeCode2 = localeCode;
        Integer ruleSetNum = null;
        while (ruleSetNum == null) {
            ruleSetNum = DATA.localesToRuleSetNumMap.get(localeCode2);
            if (ruleSetNum != null) {
                break;
            }
            localeCode2 = ULocale.getFallback(localeCode2);
            if (localeCode2.isEmpty()) {
                break;
            }
        }
        if (ruleSetNum == null || DATA.rules[ruleSetNum.intValue()] == null) {
            return null;
        }
        return DATA.rules[ruleSetNum.intValue()];
    }

    public double getMidPointForDayPeriod(DayPeriod dayPeriod) {
        int startHour = getStartHourForDayPeriod(dayPeriod);
        int endHour = getEndHourForDayPeriod(dayPeriod);
        double midPoint = ((double) (startHour + endHour)) / 2.0d;
        if (startHour <= endHour) {
            return midPoint;
        }
        double midPoint2 = midPoint + 12.0d;
        if (midPoint2 >= 24.0d) {
            return midPoint2 - 24.0d;
        }
        return midPoint2;
    }

    private static DayPeriodRulesData loadData() {
        DayPeriodRulesData data = new DayPeriodRulesData();
        ICUResourceBundle rb = ICUResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "dayPeriods", ICUResourceBundle.ICU_DATA_CLASS_LOADER, true);
        rb.getAllItemsWithFallback("rules", new DayPeriodRulesCountSink(data));
        data.rules = new DayPeriodRules[(data.maxRuleSetNum + 1)];
        rb.getAllItemsWithFallback("", new DayPeriodRulesDataSink(data));
        return data;
    }

    private int getStartHourForDayPeriod(DayPeriod dayPeriod) throws IllegalArgumentException {
        int i = 0;
        if (dayPeriod == DayPeriod.MIDNIGHT) {
            return 0;
        }
        if (dayPeriod == DayPeriod.NOON) {
            return 12;
        }
        if (this.dayPeriodForHour[0] != dayPeriod || this.dayPeriodForHour[23] != dayPeriod) {
            while (true) {
                int i2 = i;
                if (i2 > 23) {
                    break;
                } else if (this.dayPeriodForHour[i2] == dayPeriod) {
                    return i2;
                } else {
                    i = i2 + 1;
                }
            }
        } else {
            for (int i3 = 22; i3 >= 1; i3--) {
                if (this.dayPeriodForHour[i3] != dayPeriod) {
                    return i3 + 1;
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
        int i = 23;
        if (this.dayPeriodForHour[0] != dayPeriod || this.dayPeriodForHour[23] != dayPeriod) {
            while (true) {
                int i2 = i;
                if (i2 < 0) {
                    break;
                } else if (this.dayPeriodForHour[i2] == dayPeriod) {
                    return i2 + 1;
                } else {
                    i = i2 - 1;
                }
            }
        } else {
            for (int i3 = 1; i3 <= 22; i3++) {
                if (this.dayPeriodForHour[i3] != dayPeriod) {
                    return i3;
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

    public DayPeriod getDayPeriodForHour(int hour) {
        return this.dayPeriodForHour[hour];
    }

    /* access modifiers changed from: private */
    public void add(int startHour, int limitHour, DayPeriod period) {
        int i = startHour;
        while (i != limitHour) {
            if (i == 24) {
                i = 0;
            }
            this.dayPeriodForHour[i] = period;
            i++;
        }
    }

    /* access modifiers changed from: private */
    public static int parseSetNum(String setNumStr) {
        if (setNumStr.startsWith("set")) {
            return Integer.parseInt(setNumStr.substring(3));
        }
        throw new ICUException("Set number should start with \"set\".");
    }
}
