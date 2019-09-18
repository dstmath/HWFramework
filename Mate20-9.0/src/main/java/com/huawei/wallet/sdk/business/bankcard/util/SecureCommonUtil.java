package com.huawei.wallet.sdk.business.bankcard.util;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.ArrayList;

public class SecureCommonUtil {
    private static final String TAG = "SecureCommonUtil";

    public static Bundle getBundleExtra(Intent intent, String key) {
        if (!isParamValid(intent, key)) {
            return null;
        }
        try {
            return intent.getBundleExtra(key);
        } catch (RuntimeException e) {
            LogC.e(TAG, "getBundleExtra,Exception." + e, false);
            return null;
        }
    }

    public static <T extends Parcelable> T getParcelableExtra(Intent intent, String key) {
        if (!isParamValid(intent, key)) {
            return null;
        }
        try {
            return intent.getParcelableExtra(key);
        } catch (RuntimeException e) {
            LogC.e(TAG, "getParcelableExtra,Exception." + e, false);
            return null;
        }
    }

    public static byte[] getByteArrayExtra(Intent intent, String key) {
        if (!isParamValid(intent, key)) {
            return null;
        }
        try {
            return intent.getByteArrayExtra(key);
        } catch (RuntimeException e) {
            LogC.e(TAG, "getByteArrayExtra,Exception." + e, false);
            return null;
        }
    }

    public static long getLongExtra(Intent intent, String key, long def) {
        long result = def;
        if (!isParamValid(intent, key)) {
            return result;
        }
        try {
            return intent.getLongExtra(key, def);
        } catch (RuntimeException e) {
            LogC.e(TAG, "getLongExtra,Exception." + e, false);
            return result;
        }
    }

    public static boolean getBooleanExtra(Intent intent, String key, boolean def) {
        boolean result = def;
        if (!isParamValid(intent, key)) {
            return result;
        }
        try {
            return intent.getBooleanExtra(key, def);
        } catch (RuntimeException e) {
            LogC.e(TAG, "getBooleanExtra,Exception." + e, false);
            return result;
        }
    }

    public static int getIntExtra(Intent intent, String key, int def) {
        int result = def;
        if (!isParamValid(intent, key)) {
            return result;
        }
        try {
            return intent.getIntExtra(key, def);
        } catch (RuntimeException e) {
            LogC.e(TAG, "getIntExtra,Exception." + e, false);
            return result;
        }
    }

    public static String getStringExtra(Intent intent, String key) {
        if (!isParamValid(intent, key)) {
            return null;
        }
        try {
            return intent.getStringExtra(key);
        } catch (RuntimeException e) {
            LogC.e(TAG, "getStringExtra,Exception." + e, false);
            return null;
        }
    }

    public static ArrayList<String> getStringArrayListExtra(Intent intent, String key) {
        if (!isParamValid(intent, key)) {
            return null;
        }
        try {
            return intent.getStringArrayListExtra(key);
        } catch (RuntimeException e) {
            LogC.e(TAG, "getStringArrayListExtra,Exception." + e, false);
            return null;
        }
    }

    private static boolean isParamValid(Intent intent, String key) {
        if (intent != null && !StringUtil.isEmpty(key, false)) {
            return true;
        }
        LogC.w(TAG, "isParamValid,intent or key is null.", false);
        return false;
    }
}
