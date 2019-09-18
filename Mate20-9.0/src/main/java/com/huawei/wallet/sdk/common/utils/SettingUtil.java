package com.huawei.wallet.sdk.common.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.text.TextUtils;
import com.huawei.wallet.sdk.common.log.LogC;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class SettingUtil {
    private static final int AGREE_USER_EXPERIENCE_VALUE = 1;
    private static final String COLUMN_USER_EXPERIENCE_INVOLVED = "user_experience_involved";
    private static boolean hasCheckSupportUserExperience = false;
    private static boolean isSupportUserExperience = true;
    private static final Map<String, String> propertiesMap = new HashMap();

    public static boolean hasUserExperienceConfig(Context context) {
        if (!hasCheckSupportUserExperience) {
            try {
                context.getPackageManager().getApplicationInfo("com.huawei.bd", 8192);
            } catch (PackageManager.NameNotFoundException e) {
                LogC.i("isSupportUserExperience: bd is not installed", false);
                try {
                    context.getPackageManager().getApplicationInfo("com.huawei.lcagent", 8192);
                } catch (PackageManager.NameNotFoundException e2) {
                    isSupportUserExperience = false;
                    LogC.i("isSupportUserExperience: lcagent is not installed", false);
                }
            }
            hasCheckSupportUserExperience = true;
            LogC.d("isSupportUserExperience: has checked， need not check again, isSupportUserExperience = " + isSupportUserExperience, false);
        }
        return isSupportUserExperience;
    }

    public static boolean isAgreeUserExperience(Context context) {
        int userExperienceInvolved = Settings.Secure.getInt(context.getContentResolver(), COLUMN_USER_EXPERIENCE_INVOLVED, -1);
        LogC.d("get settings userExperienceInvolved value, userExperienceInvolved=" + userExperienceInvolved, false);
        return userExperienceInvolved == 1;
    }

    public static String getSystemProperties(String key) {
        String value = "";
        if (TextUtils.isEmpty(key)) {
            LogC.e("getSystemProperties, key is null or empty.", false);
            return value;
        } else if (propertiesMap == null || !propertiesMap.containsKey(key)) {
            try {
                Class<?> classType = Class.forName("android.os.SystemProperties");
                value = (String) classType.getDeclaredMethod("get", new Class[]{String.class}).invoke(classType, new Object[]{key});
                LogC.i("getSystemProperties VALUE IS :" + value, false);
            } catch (ClassNotFoundException e) {
                LogC.e("getSystemProperties, ClassNotFoundException.", false);
            } catch (NoSuchMethodException e2) {
                LogC.e("getSystemProperties NoSuchMethodException.", false);
            } catch (IllegalAccessException e3) {
                LogC.e("getSystemProperties IllegalAccessException.", false);
            } catch (IllegalArgumentException e4) {
                LogC.e("getSystemProperties IllegalArgumentException.", false);
            } catch (InvocationTargetException e5) {
                LogC.e("getSystemProperties InvocationTargetException.", false);
            }
            if (propertiesMap != null && TextUtils.isEmpty(value)) {
                propertiesMap.put(key, value);
            }
            return value;
        } else {
            String value2 = propertiesMap.get(key);
            LogC.i("getSystemProperties propertiesMap has VALUE IS :" + value2, false);
            return value2;
        }
    }
}
