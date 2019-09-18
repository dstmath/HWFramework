package com.android.internal.widget;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.android.app.admin.HwCustDevicePolicyManagerEx;
import com.huawei.android.util.HwPasswordUtils;

public class HwCustLockPatternUtilsImpl extends HwCustLockPatternUtils {
    private static String DOCOMO_MODEL_NAME = SystemProperties.get("ro.product.custom", "NULL");
    private static boolean FORBIDDEN_SIMPLE_PWD = SystemProperties.getBoolean("ro.config.not_allow_simple_pwd", false);
    private static final String TAG = "HwCustLockPatternUtilsImpl";
    private static boolean isPowerBtnDefault = SystemProperties.getBoolean("ro.config.power_btn_lock", true);
    private DevicePolicyManager dpm;
    private Context mContext;

    public boolean getPowerBtnInstantlyLockDefault() {
        return isPowerBtnDefault;
    }

    public boolean isForbiddenSimplePwdFeatureEnable() {
        return FORBIDDEN_SIMPLE_PWD;
    }

    public boolean currentpwdSimpleCheck(String password) {
        if (this.mContext == null) {
            return false;
        }
        HwPasswordUtils.loadSimplePasswordTable(this.mContext);
        if (HwPasswordUtils.isSimpleAlphaNumericPassword(password) || HwPasswordUtils.isOrdinalCharatersPassword(password) || HwPasswordUtils.isSimplePasswordInDictationary(password)) {
            return true;
        }
        return false;
    }

    public void initHwCustLockPatternUtils(DevicePolicyManager mDevicePolicyManager, Context context) {
        this.mContext = context;
        if (mDevicePolicyManager == null) {
            Log.e(TAG, "init the HwCustLockPatternUtils failed because mDevicePolicyManager is null");
        } else {
            this.dpm = mDevicePolicyManager;
        }
    }

    public void saveCurrentPwdStatus(boolean flag) {
        HwCustDevicePolicyManagerEx.saveCurrentPwdStatus(flag);
    }

    public DevicePolicyManager getDevicePolicyManager() {
        return this.dpm;
    }

    public boolean needLimit() {
        return DOCOMO_MODEL_NAME.contains("docomo");
    }
}
