package com.huawei.i18n.tmr.address.util;

import android.util.Log;
import com.huawei.android.os.storage.StorageManagerExt;
import java.util.HashMap;
import java.util.regex.Pattern;

public class PatternCache {
    private static final String TAG = "PatternCache";
    private static HashMap<String, Pattern> cache = null;

    public static Pattern getPattern(String regexName, String packageName) {
        Pattern pattern;
        String regex;
        synchronized (PatternCache.class) {
            String key = packageName + "_" + regexName;
            if (cache == null) {
                cache = new HashMap<>();
            }
            pattern = null;
            if (cache.containsKey(key)) {
                pattern = cache.get(key);
            } else {
                Regexs regexs = null;
                try {
                    if (Class.forName(packageName).newInstance() instanceof Regexs) {
                        regexs = (Regexs) Class.forName(packageName).newInstance();
                        regexs.init();
                    }
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                    Log.e(TAG, "getPattern ERROR");
                }
                if (!(regexs == null || (regex = regexs.getRegex(regexName)) == null || StorageManagerExt.INVALID_KEY_DESC.equals(regex))) {
                    pattern = Pattern.compile(regex);
                    cache.put(key, pattern);
                }
            }
        }
        return pattern;
    }
}
