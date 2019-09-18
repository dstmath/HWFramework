package com.huawei.g11n.tmr.datetime.detect;

import com.huawei.g11n.tmr.datetime.utils.LocaleParam;
import java.util.HashMap;

public class RuleLevel {
    private HashMap<Integer, Integer> levels = new HashMap<Integer, Integer>() {
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
            if (shortDateMark.equalsIgnoreCase("ymd")) {
                this.levels.put(20016, 1);
                this.levels.put(20014, 3);
                this.levels.put(20015, 2);
            } else if (shortDateMark.equalsIgnoreCase("mdy")) {
                this.levels.put(20016, 2);
                this.levels.put(20014, 3);
                this.levels.put(20015, 1);
            }
        }
    }

    public int compare(int l1, int l2) {
        int r = 0;
        int c = getLevels(l2) - getLevels(l1);
        if (c > 0) {
            r = 1;
        }
        if (c < 0) {
            return -1;
        }
        return r;
    }

    public int getLevels(int name) {
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
