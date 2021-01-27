package com.android.internal.widget;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.android.app.admin.HwCustDevicePolicyManagerEx;
import com.huawei.android.util.HwPasswordUtils;

public class HwCustLockPatternUtilsImpl extends HwCustLockPatternUtils {
    private static final String DOCOMO_MODEL_NAME = SystemProperties.get("ro.product.custom", "NULL");
    private static final boolean FORBIDDEN_SIMPLE_PWD = SystemProperties.getBoolean("ro.config.not_allow_simple_pwd", false);
    private static final boolean IS_POWER_BTN_DEFAULT = SystemProperties.getBoolean("ro.config.power_btn_lock", true);
    private static final String TAG = "HwCustLockPatternUtilsImpl";
    private Context mContext;
    private DevicePolicyManager mDevicePolicyManager;

    public boolean getPowerBtnInstantlyLockDefault() {
        return IS_POWER_BTN_DEFAULT;
    }

    public boolean isForbiddenSimplePwdFeatureEnable() {
        return FORBIDDEN_SIMPLE_PWD;
    }

    public boolean currentpwdSimpleCheck(String password) {
        Context context = this.mContext;
        if (context == null) {
            return false;
        }
        HwPasswordUtils.loadSimplePasswordTable(context);
        if (HwPasswordUtils.isSimpleAlphaNumericPassword(password) || HwPasswordUtils.isOrdinalCharatersPassword(password) || HwPasswordUtils.isSimplePasswordInDictationary(password)) {
            return true;
        }
        return false;
    }

    public void initHwCustLockPatternUtils(DevicePolicyManager devicePolicyManager, Context context) {
        this.mContext = context;
        if (devicePolicyManager == null) {
            Log.e(TAG, "init the HwCustLockPatternUtils failed because mDevicePolicyManager is null");
        } else {
            this.mDevicePolicyManager = devicePolicyManager;
        }
    }

    public void saveCurrentPwdStatus(boolean flag) {
        HwCustDevicePolicyManagerEx.saveCurrentPwdStatus(flag);
    }

    public DevicePolicyManager getDevicePolicyManager() {
        return this.mDevicePolicyManager;
    }

    public boolean needLimit() {
        return DOCOMO_MODEL_NAME.contains("docomo");
    }
}
