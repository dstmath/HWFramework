package com.android.server;

import android.content.Context;
import android.media.AudioManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.MSimTelephonyManager;
import android.text.TextUtils;

public class HwCustCbsUtilsImpl extends HwCustCbsUtils {
    private static final String ALLOW_VIBRATOR_PACKAGE = "allow_vibrator_package";
    private static final String CELLBROADCAST_PACKAGE = "com.android.cellbroadcastreceiver";
    private static final String[] DCM_ALLOW_CUST_PACKAGES = {"android", "com.nttdocomo.android.areamail", "jp.co.nttdocomo.lcsapp", "com.nttdocomo.android.phonemotion"};
    private static final boolean IS_DOCOMO = SystemProperties.get("ro.product.custom", "NULL").contains("docomo");
    private static final String KEY_VIBIRATE_WHEN_SILENT = "vibirate_when_silent";
    private static final int SIM_MCC_LEN = 3;
    private static final int VIBRATE_IN_SILENT_MODE_OFF = 0;
    private static final int VIBRATE_IN_SILENT_MODE_ON = 1;
    private static String lowPowerPkg = "com.nttdocomo.android.areamail;jp.co.nttdocomo.lcsapp";
    private String[] mAllowPackages;

    public HwCustCbsUtilsImpl(Context context) {
        super(context);
        if (this.mContext != null) {
            String allowPackageStr = Settings.Global.getString(this.mContext.getContentResolver(), ALLOW_VIBRATOR_PACKAGE);
            if (!TextUtils.isEmpty(allowPackageStr)) {
                this.mAllowPackages = allowPackageStr.split(";");
            }
        }
    }

    public boolean isNotAllowPkg(String currentVibrationPkg) {
        return (CELLBROADCAST_PACKAGE.equals(currentVibrationPkg) && isChile()) || isVibratorInOffScreen(currentVibrationPkg);
    }

    private boolean isVibratorInOffScreen(String currentVibrationPkg) {
        String[] strArr;
        if (TextUtils.isEmpty(currentVibrationPkg) || (strArr = this.mAllowPackages) == null || strArr.length == 0) {
            return false;
        }
        int length = strArr.length;
        for (int i = VIBRATE_IN_SILENT_MODE_OFF; i < length; i += VIBRATE_IN_SILENT_MODE_ON) {
            if (strArr[i].equals(currentVibrationPkg)) {
                return true;
            }
        }
        return false;
    }

    private boolean isChile() {
        return isCustSimOperator(SystemProperties.get("ro.config.hw_cbs_mcc"));
    }

    private boolean isCustSimOperator(String custMccForCBSPreference) {
        if (TextUtils.isEmpty(custMccForCBSPreference)) {
            return false;
        }
        if (!MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
            return isCustPlmn(custMccForCBSPreference, MSimTelephonyManager.getDefault().getSimOperator());
        }
        boolean flag = false;
        if (isCustPlmn(custMccForCBSPreference, MSimTelephonyManager.getDefault().getSimOperator((int) VIBRATE_IN_SILENT_MODE_OFF)) || isCustPlmn(custMccForCBSPreference, MSimTelephonyManager.getDefault().getSimOperator((int) VIBRATE_IN_SILENT_MODE_ON))) {
            flag = VIBRATE_IN_SILENT_MODE_ON;
        }
        return flag;
    }

    private boolean isCustPlmn(String custPlmnsString, String simMccMnc) {
        if (!(simMccMnc == null || simMccMnc.length() < SIM_MCC_LEN || custPlmnsString == null)) {
            String[] custPlmns = custPlmnsString.split(";");
            int length = custPlmns.length;
            for (int i = VIBRATE_IN_SILENT_MODE_OFF; i < length; i += VIBRATE_IN_SILENT_MODE_ON) {
                String custPlmn = custPlmns[i];
                if (simMccMnc.substring(VIBRATE_IN_SILENT_MODE_OFF, SIM_MCC_LEN).equals(custPlmn) || simMccMnc.equals(custPlmn)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isAllowLowPowerPkg(String currentVibrationPkg) {
        if (IS_DOCOMO) {
            return lowPowerPkg.contains(currentVibrationPkg);
        }
        return false;
    }

    public boolean allowVibrateWhenSlient(Context context, String currentVibrationPkg) {
        if (context == null || !IS_DOCOMO) {
            return true;
        }
        int ringerMode = ((AudioManager) context.getSystemService("audio")).getRingerModeInternal();
        int vibrateInSilent = Settings.Global.getInt(context.getContentResolver(), KEY_VIBIRATE_WHEN_SILENT, VIBRATE_IN_SILENT_MODE_OFF);
        if (!(ringerMode == 0 && vibrateInSilent == 0)) {
            return true;
        }
        String[] strArr = DCM_ALLOW_CUST_PACKAGES;
        int length = strArr.length;
        for (int i = VIBRATE_IN_SILENT_MODE_OFF; i < length; i += VIBRATE_IN_SILENT_MODE_ON) {
            if (strArr[i].equals(currentVibrationPkg)) {
                return true;
            }
        }
        return false;
    }
}
