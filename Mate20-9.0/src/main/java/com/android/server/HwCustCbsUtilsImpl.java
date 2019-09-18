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
        if (TextUtils.isEmpty(currentVibrationPkg) || this.mAllowPackages == null || this.mAllowPackages.length == 0) {
            return false;
        }
        for (String pkgname : this.mAllowPackages) {
            if (pkgname.equals(currentVibrationPkg)) {
                return true;
            }
        }
        return false;
    }

    private boolean isChile() {
        return isCustSimOperator(SystemProperties.get("ro.config.hw_cbs_mcc"));
    }

    private boolean isCustSimOperator(String mCustMccForCBSPreference) {
        boolean flag;
        if (TextUtils.isEmpty(mCustMccForCBSPreference)) {
            return false;
        }
        if (MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
            boolean z = false;
            if (isCustPlmn(mCustMccForCBSPreference, MSimTelephonyManager.getDefault().getSimOperator(0)) || isCustPlmn(mCustMccForCBSPreference, MSimTelephonyManager.getDefault().getSimOperator(1))) {
                z = true;
            }
            flag = z;
        } else {
            flag = isCustPlmn(mCustMccForCBSPreference, MSimTelephonyManager.getDefault().getSimOperator());
        }
        return flag;
    }

    private boolean isCustPlmn(String custPlmnsString, String simMccMnc) {
        if (!(simMccMnc == null || simMccMnc.length() < SIM_MCC_LEN || custPlmnsString == null)) {
            String[] custPlmns = custPlmnsString.split(";");
            for (int i = 0; i < custPlmns.length; i++) {
                if (simMccMnc.substring(0, SIM_MCC_LEN).equals(custPlmns[i]) || simMccMnc.equals(custPlmns[i])) {
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
        int vibrateInSilent = Settings.Global.getInt(context.getContentResolver(), KEY_VIBIRATE_WHEN_SILENT, 0);
        if (ringerMode != 0 || vibrateInSilent != 0) {
            return true;
        }
        for (String pkgName : DCM_ALLOW_CUST_PACKAGES) {
            if (pkgName.equals(currentVibrationPkg)) {
                return true;
            }
        }
        return false;
    }
}
