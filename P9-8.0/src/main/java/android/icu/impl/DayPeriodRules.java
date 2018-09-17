package android.icu.impl;

import android.icu.impl.UResource.Array;
import android.icu.impl.UResource.Key;
import android.icu.impl.UResource.Sink;
import android.icu.impl.UResource.Table;
import android.icu.impl.UResource.Value;
import android.icu.util.ICUException;
import android.icu.util.ULocale;
import java.util.HashMap;
import java.util.Map;

public final class DayPeriodRules {
    private static final DayPeriodRulesData DATA = loadData();
    private DayPeriod[] dayPeriodForHour;
    private boolean hasMidnight;
    private boolean hasNoon;

    private enum CutoffType {
        BEFORE,
        AFTER,
        FROM,
        AT;

        private static CutoffType fromStringOrNull(CharSequence str) {
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

        private static DayPeriod fromStringOrNull(CharSequence str) {
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

    private static class DayPeriodRulesCountSink extends Sink {
        private DayPeriodRulesData data;

        /* synthetic */ DayPeriodRulesCountSink(DayPeriodRulesData data, DayPeriodRulesCountSink -this1) {
            this(data);
        }

        private DayPeriodRulesCountSink(DayPeriodRulesData data) {
            this.data = data;
        }

        public void put(Key key, Value value, boolean noFallback) {
            Table rules = value.getTable();
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

        /* synthetic */ DayPeriodRulesData(DayPeriodRulesData -this0) {
            this();
        }

        private DayPeriodRulesData() {
            this.localesToRuleSetNumMap = new HashMap();
            this.maxRuleSetNum = -1;
        }
    }

    private static final class DayPeriodRulesDataSink extends Sink {
        private CutoffType cutoffType;
        private int[] cutoffs;
        private DayPeriodRulesData data;
        private DayPeriod period;
        private int ruleSetNum;

        /* synthetic */ DayPeriodRulesDataSink(DayPeriodRulesData data, DayPeriodRulesDataSink -this1) {
            this(data);
        }

        private DayPeriodRulesDataSink(DayPeriodRulesData data) {
            this.cutoffs = new int[25];
            this.data = data;
        }

        public void put(Key key, Value value, boolean noFallback) {
            Table dayPeriodData = value.getTable();
            for (int i = 0; dayPeriodData.getKeyAndValue(i, key, value); i++) {
                if (key.contentEquals("locales")) {
                    Table locales = value.getTable();
                    for (int j = 0; locales.getKeyAndValue(j, key, value); j++) {
                        this.data.localesToRuleSetNumMap.put(key.toString(), Integer.valueOf(DayPeriodRules.parseSetNum(value.getString())));
                    }
                } else if (key.contentEquals("rules")) {
                    processRules(value.getTable(), key, value);
                }
            }
        }

        private void processRules(Table rules, Key key, Value value) {
            for (int i = 0; rules.getKeyAndValue(i, key, value); i++) {
                this.ruleSetNum = DayPeriodRules.parseSetNum(key.toString());
                this.data.rules[this.ruleSetNum] = new DayPeriodRules();
                Table ruleSet = value.getTable();
                for (int j = 0; ruleSet.getKeyAndValue(j, key, value); j++) {
                    this.period = DayPeriod.fromStringOrNull(key);
                    if (this.period == null) {
                        throw new ICUException("Unknown day period in data.");
                    }
                    int k;
                    Table periodDefinition = value.getTable();
                    for (k = 0; periodDefinition.getKeyAndValue(k, key, value); k++) {
                        if (value.getType() == 0) {
                            addCutoff(CutoffType.fromStringOrNull(key), value.getString());
                        } else {
                            this.cutoffType = CutoffType.fromStringOrNull(key);
                            Array cutoffArray = value.getArray();
                            int length = cutoffArray.getSize();
                            for (int l = 0; l < length; l++) {
                                cutoffArray.getValue(l, value);
                                addCutoff(this.cutoffType, value.getString());
                            }
                        }
                    }
                    setDayPeriodForHoursFromCutoffs();
                    for (k = 0; k < this.cutoffs.length; k++) {
                        this.cutoffs[k] = 0;
                    }
                }
                for (DayPeriod period : this.data.rules[this.ruleSetNum].dayPeriodForHour) {
                    if (period == null) {
                        throw new ICUException("Rules in data don't cover all 24 hours (they should).");
                    }
                }
            }
        }

        private void addCutoff(CutoffType type, String hourStr) {
            if (type == null) {
                throw new ICUException("Cutoff type not recognized.");
            }
            int hour = parseHour(hourStr);
            int[] iArr = this.cutoffs;
            iArr[hour] = iArr[hour] | (1 << type.ordinal());
        }

        private void setDayPeriodForHoursFromCutoffs() {
            DayPeriodRules rule = this.data.rules[this.ruleSetNum];
            int startHour = 0;
            while (startHour <= 24) {
                if ((this.cutoffs[startHour] & (1 << CutoffType.AT.ordinal())) > 0) {
                    if (startHour == 0 && this.period == DayPeriod.MIDNIGHT) {
                        rule.hasMidnight = true;
                    } else if (startHour == 12 && this.period == DayPeriod.NOON) {
                        rule.hasNoon = true;
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
                startHour++;
            }
        }

        private static int parseHour(String str) {
            int firstColonPos = str.indexOf(58);
            if (firstColonPos < 0 || (str.substring(firstColonPos).equals(":00") ^ 1) != 0) {
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

    /* synthetic */ DayPeriodRules(DayPeriodRules -this0) {
        this();
    }

    private DayPeriodRules() {
        this.hasMidnight = false;
        this.hasNoon = false;
        this.dayPeriodForHour = new DayPeriod[24];
    }

    public static DayPeriodRules getInstance(ULocale locale) {
        String localeCode = locale.getName();
        if (localeCode.isEmpty()) {
            localeCode = "root";
        }
        Integer ruleSetNum = null;
        while (ruleSetNum == null) {
            ruleSetNum = (Integer) DATA.localesToRuleSetNumMap.get(localeCode);
            if (ruleSetNum != null) {
                break;
            }
            localeCode = ULocale.getFallback(localeCode);
            if (localeCode.isEmpty()) {
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
        midPoint += 12.0d;
        if (midPoint >= 24.0d) {
            return midPoint - 24.0d;
        }
        return midPoint;
    }

    private static DayPeriodRulesData loadData() {
        DayPeriodRulesData data = new DayPeriodRulesData();
        ICUResourceBundle rb = ICUResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "dayPeriods", ICUResourceBundle.ICU_DATA_CLASS_LOADER, true);
        rb.getAllItemsWithFallback("rules", new DayPeriodRulesCountSink(data, null));
        data.rules = new DayPeriodRules[(data.maxRuleSetNum + 1)];
        rb.getAllItemsWithFallback("", new DayPeriodRulesDataSink(data, null));
        return data;
    }

    private int getStartHourForDayPeriod(DayPeriod dayPeriod) throws IllegalArgumentException {
        if (dayPeriod == DayPeriod.MIDNIGHT) {
            return 0;
        }
        if (dayPeriod == DayPeriod.NOON) {
            return 12;
        }
        int i;
        if (this.dayPeriodForHour[0] == dayPeriod && this.dayPeriodForHour[23] == dayPeriod) {
            for (i = 22; i >= 1; i--) {
                if (this.dayPeriodForHour[i] != dayPeriod) {
                    return i + 1;
                }
            }
        } else {
            for (i = 0; i <= 23; i++) {
                if (this.dayPeriodForHour[i] == dayPeriod) {
                    return i;
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
        int i;
        if (this.dayPeriodForHour[0] == dayPeriod && this.dayPeriodForHour[23] == dayPeriod) {
            for (i = 1; i <= 22; i++) {
                if (this.dayPeriodForHour[i] != dayPeriod) {
                    return i;
                }
            }
        } else {
            for (i = 23; i >= 0; i--) {
                if (this.dayPeriodForHour[i] == dayPeriod) {
                    return i + 1;
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

    private void add(int startHour, int limitHour, DayPeriod period) {
        int i = startHour;
        while (i != limitHour) {
            if (i == 24) {
                i = 0;
            }
            this.dayPeriodForHour[i] = period;
            i++;
        }
    }

    private static int parseSetNum(String setNumStr) {
        if (setNumStr.startsWith("set")) {
            return Integer.parseInt(setNumStr.substring(3));
        }
        throw new ICUException("Set number should start with \"set\".");
    }
}
