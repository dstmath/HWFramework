package com.huawei.i18n.tmr.datetime.detect;

import com.huawei.i18n.tmr.datetime.data.LocaleParam;
import java.util.HashMap;

public class RuleLevel {
    private HashMap<Integer, Integer> levels = new HashMap<Integer, Integer>() {
        /* class com.huawei.i18n.tmr.datetime.detect.RuleLevel.AnonymousClass1 */

        {
            put(21018, 1);
            put(20001, 2);
            put(20012, 2);
            put(20008, 2);
            put(20005, 2);
            put(21015, 2);
            put(20006, 4);
            put(20007, 3);
            put(20009, 4);
            put(20010, 4);
            put(20011, 3);
            put(20016, 2);
            put(20014, 1);
            put(20015, 3);
            put(20013, 4);
        }
    };

    public RuleLevel(String locale) {
        String shortDateMark = new LocaleParam(locale).getWithoutB("mark_ShortDateLevel");
        if (shortDateMark != null && !shortDateMark.trim().isEmpty()) {
            if ("ymd".equalsIgnoreCase(shortDateMark)) {
                this.levels.put(20016, 1);
                this.levels.put(20014, 3);
                this.levels.put(20015, 2);
            } else if ("mdy".equalsIgnoreCase(shortDateMark)) {
                this.levels.put(20016, 2);
                this.levels.put(20014, 3);
                this.levels.put(20015, 1);
            }
        }
    }

    public int compare(int key1, int key2) {
        int result = 0;
        int sub = getLevels(key2) - getLevels(key1);
        if (sub > 0) {
            result = 1;
        }
        if (sub < 0) {
            return -1;
        }
        return result;
    }

    private int getLevels(int name) {
        int baseLevel;
        if (name > 9999 && name < 20000) {
            baseLevel = 10;
        } else if ((name <= 19999 || name >= 30000) && (name <= 29999 || name >= 40000)) {
            baseLevel = 30;
        } else {
            baseLevel = 20;
        }
        return baseLevel + (this.levels.containsKey(Integer.valueOf(name)) ? this.levels.get(Integer.valueOf(name)).intValue() : 1);
    }
}
