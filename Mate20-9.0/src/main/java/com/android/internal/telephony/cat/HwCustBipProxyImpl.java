package com.android.internal.telephony.cat;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.cat.CatCmdMessage;

public class HwCustBipProxyImpl extends HwCustBipProxy {
    private static final String KDDI_PLMN = "44050";
    private static final String KDDI_PLMN_MAIN = "44051";
    private static final String KDDI_TEST_PLMN = "44110";
    private static final boolean OTA_BIP_KDDI = SystemProperties.getBoolean("ro.config.enable_ota_bip", false);

    public HwCustBipProxyImpl(Context mContext) {
        super(mContext);
    }

    public String getApnString(CatCmdMessage.ChannelSettings newChannel, String channelInfoType) {
        if (newChannel == null || TextUtils.isEmpty(channelInfoType)) {
            return "";
        }
        return "[ApnSettingV3] " + "bipapn, " + newChannel.networkAccessName + ",," + String.valueOf(newChannel.port) + "," + newChannel.userLogin + "," + newChannel.userPassword + ",,,,,,,2," + channelInfoType + ",IPV4V6,IP,true,0,";
    }

    public boolean kddiBipOtaEnable() {
        if (!OTA_BIP_KDDI) {
            return false;
        }
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm == null) {
            return false;
        }
        String hplmn = tm.getSimOperator();
        if (KDDI_PLMN_MAIN.equals(hplmn) || KDDI_PLMN.equals(hplmn) || KDDI_TEST_PLMN.equals(hplmn)) {
            return true;
        }
        return false;
    }

    public boolean isBipOtaEnable(int slotId) {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm == null) {
            return false;
        }
        String hplmn = tm.getSimOperatorNumericForPhone(slotId);
        String plmnsConfig = Settings.System.getString(this.mContext.getContentResolver(), "bip_ota_plmn_matched");
        if (TextUtils.isEmpty(plmnsConfig) || TextUtils.isEmpty(hplmn)) {
            return false;
        }
        for (String tmpPlmn : plmnsConfig.split(",")) {
            if (!TextUtils.isEmpty(tmpPlmn) && tmpPlmn.equals(hplmn)) {
                return true;
            }
        }
        return false;
    }
}
