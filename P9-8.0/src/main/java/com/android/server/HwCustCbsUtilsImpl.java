package com.android.server;

import android.content.Context;
import android.media.AudioManager;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.telephony.MSimTelephonyManager;
import android.text.TextUtils;

public class HwCustCbsUtilsImpl extends HwCustCbsUtils {
    private static final String ALLOW_VIBRATOR_PACKAGE = "allow_vibrator_package";
    private static final String CELLBROADCAST_PACKAGE = "com.android.cellbroadcastreceiver";
    private static final String DCM_AREAMAIL_PACKAGE = "com.nttdocomo.android.areamail";
    private static final String DCM_LCSAPP_PACKAGE = "jp.co.nttdocomo.lcsapp";
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
            String allowPackageStr = Global.getString(this.mContext.getContentResolver(), ALLOW_VIBRATOR_PACKAGE);
            if (!TextUtils.isEmpty(allowPackageStr)) {
                this.mAllowPackages = allowPackageStr.split(";");
            }
        }
    }

    public boolean isNotAllowPkg(String currentVibrationPkg) {
        return (CELLBROADCAST_PACKAGE.equals(currentVibrationPkg) && isChile()) ? true : isVibratorInOffScreen(currentVibrationPkg);
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
        if (TextUtils.isEmpty(mCustMccForCBSPreference)) {
            return false;
        }
        boolean flag;
        if (!MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
            flag = isCustPlmn(mCustMccForCBSPreference, MSimTelephonyManager.getDefault().getSimOperator());
        } else if (isCustPlmn(mCustMccForCBSPreference, MSimTelephonyManager.getDefault().getSimOperator(0))) {
            flag = true;
        } else {
            flag = isCustPlmn(mCustMccForCBSPreference, MSimTelephonyManager.getDefault().getSimOperator(1));
        }
        return flag;
    }

    private boolean isCustPlmn(String custPlmnsString, String simMccMnc) {
        if (!(simMccMnc == null || simMccMnc.length() < SIM_MCC_LEN || custPlmnsString == null)) {
            String[] custPlmns = custPlmnsString.split(";");
            int i = 0;
            while (i < custPlmns.length) {
                if (simMccMnc.substring(0, SIM_MCC_LEN).equals(custPlmns[i]) || simMccMnc.equals(custPlmns[i])) {
                    return true;
                }
                i++;
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

    /* JADX WARNING: Missing block: B:14:0x0039, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean allowVibrateWhenSlient(Context context, String currentVibrationPkg) {
        if (context == null || !IS_DOCOMO) {
            return true;
        }
        return ((AudioManager) context.getSystemService("audio")).getRingerModeInternal() != 0 || Global.getInt(context.getContentResolver(), KEY_VIBIRATE_WHEN_SILENT, 0) != 0 || DCM_LCSAPP_PACKAGE.equals(currentVibrationPkg) || DCM_AREAMAIL_PACKAGE.equals(currentVibrationPkg);
    }
}
