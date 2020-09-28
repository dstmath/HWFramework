package com.huawei.g11n.tmr.util;

import com.huawei.uikit.effect.BuildConfig;
import java.util.HashMap;
import java.util.regex.Pattern;

public class PatternCache {
    private static HashMap<String, Pattern> cache = null;

    public static synchronized Pattern getPattern(String rName, String regex) {
        Pattern pattern;
        String r;
        synchronized (PatternCache.class) {
            String name = String.valueOf(regex) + "_" + rName;
            if (cache == null) {
                cache = new HashMap<>();
            }
            pattern = null;
            if (cache.containsKey(name)) {
                pattern = cache.get(name);
            } else {
                Regexs rs = null;
                try {
                    rs = (Regexs) Class.forName(regex).newInstance();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                if (!(rs == null || (r = rs.getReg(rName)) == null || r.equals(BuildConfig.FLAVOR))) {
                    pattern = Pattern.compile(r);
                    cache.put(name, pattern);
                }
            }
        }
        return pattern;
    }
}
