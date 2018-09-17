package com.huawei.g11n.tmr.datetime.detect;

import com.huawei.g11n.tmr.datetime.utils.LocaleParam;
import java.util.HashMap;

public class RuleLevel {
    private HashMap<Integer, Integer> levels = new HashMap<Integer, Integer>() {
        {
            put(Integer.valueOf(21018), Integer.valueOf(1));
            put(Integer.valueOf(20001), Integer.valueOf(2));
            put(Integer.valueOf(20012), Integer.valueOf(2));
            put(Integer.valueOf(20008), Integer.valueOf(2));
            put(Integer.valueOf(20005), Integer.valueOf(2));
            put(Integer.valueOf(21015), Integer.valueOf(2));
            put(Integer.valueOf(20006), Integer.valueOf(4));
            put(Integer.valueOf(20007), Integer.valueOf(3));
            put(Integer.valueOf(20009), Integer.valueOf(4));
            put(Integer.valueOf(20010), Integer.valueOf(4));
            put(Integer.valueOf(20011), Integer.valueOf(3));
            put(Integer.valueOf(20016), Integer.valueOf(2));
            put(Integer.valueOf(20014), Integer.valueOf(1));
            put(Integer.valueOf(20015), Integer.valueOf(3));
            put(Integer.valueOf(20013), Integer.valueOf(4));
        }
    };

    public RuleLevel(String locale) {
        String shortDateMark = new LocaleParam(locale).getWithoutB("mark_ShortDateLevel");
        if (shortDateMark != null && !shortDateMark.trim().isEmpty()) {
            if (shortDateMark.equalsIgnoreCase("ymd")) {
                this.levels.put(Integer.valueOf(20016), Integer.valueOf(1));
                this.levels.put(Integer.valueOf(20014), Integer.valueOf(3));
                this.levels.put(Integer.valueOf(20015), Integer.valueOf(2));
            } else if (shortDateMark.equalsIgnoreCase("mdy")) {
                this.levels.put(Integer.valueOf(20016), Integer.valueOf(2));
                this.levels.put(Integer.valueOf(20014), Integer.valueOf(3));
                this.levels.put(Integer.valueOf(20015), Integer.valueOf(1));
            }
        }
    }

    public int compare(int l1, int l2) {
        int r = 0;
        int c = getLevels(l2) - getLevels(l1);
        if (c > 0) {
            r = 1;
        }
        if (c >= 0) {
            return r;
        }
        return -1;
    }

    public int getLevels(int name) {
        int baseLevel;
        if (name > 9999 && name < 20000) {
            baseLevel = 10;
        } else if ((name > 19999 && name < 30000) || (name > 29999 && name < 40000)) {
            baseLevel = 20;
        } else {
            baseLevel = 30;
        }
        return baseLevel + (!this.levels.containsKey(Integer.valueOf(name)) ? 1 : ((Integer) this.levels.get(Integer.valueOf(name))).intValue());
    }
}
