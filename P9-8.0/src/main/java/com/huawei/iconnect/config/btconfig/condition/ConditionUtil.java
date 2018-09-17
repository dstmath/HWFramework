package com.huawei.iconnect.config.btconfig.condition;

import android.util.Log;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ConditionUtil {
    private static final String TAG = "ConditionUtil";

    public static boolean isRegexFind(String regex, String localDeviceName) {
        if (regex == null || regex.isEmpty() || localDeviceName == null) {
            return false;
        }
        Log.d(TAG, "regexName:" + regex + "|localDeviceName:" + localDeviceName);
        try {
            if (Pattern.compile(regex).matcher(localDeviceName).find()) {
                Log.d(TAG, "regexName find");
                return true;
            }
        } catch (PatternSyntaxException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
        return false;
    }
}
