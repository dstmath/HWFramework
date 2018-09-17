package com.huawei.g11n.tmr.datetime.detect;

import com.huawei.g11n.tmr.datetime.utils.LocaleParam;
import java.util.HashMap;

public class RuleLevel {
    private HashMap<Integer, Integer> levels;

    public RuleLevel(String str) {
        this.levels = new HashMap<Integer, Integer>() {
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
        String withoutB = new LocaleParam(str).getWithoutB("mark_ShortDateLevel");
        if (withoutB != null && !withoutB.trim().isEmpty()) {
            if (withoutB.equalsIgnoreCase("ymd")) {
                this.levels.put(Integer.valueOf(20016), Integer.valueOf(1));
                this.levels.put(Integer.valueOf(20014), Integer.valueOf(3));
                this.levels.put(Integer.valueOf(20015), Integer.valueOf(2));
            } else if (withoutB.equalsIgnoreCase("mdy")) {
                this.levels.put(Integer.valueOf(20016), Integer.valueOf(2));
                this.levels.put(Integer.valueOf(20014), Integer.valueOf(3));
                this.levels.put(Integer.valueOf(20015), Integer.valueOf(1));
            }
        }
    }

    public int compare(int i, int i2) {
        int i3 = 0;
        int levels = getLevels(i2) - getLevels(i);
        if (levels > 0) {
            i3 = 1;
        }
        if (levels >= 0) {
            return i3;
        }
        return -1;
    }

    public int getLevels(int i) {
        int i2;
        if (i > 9999 && i < 20000) {
            i2 = 10;
        } else {
            if (i <= 19999 || i >= 30000) {
                if (i > 29999) {
                    if (i >= 40000) {
                    }
                }
                i2 = 30;
            }
            i2 = 20;
        }
        return (!this.levels.containsKey(Integer.valueOf(i)) ? 1 : ((Integer) this.levels.get(Integer.valueOf(i))).intValue()) + i2;
    }
}
