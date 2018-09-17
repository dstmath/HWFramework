package com.android.internal.telephony.cat;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.cat.CatCmdMessage.ChannelSettings;

public class HwCustBipProxyImpl extends HwCustBipProxy {
    private static final String KDDI_PLMN = "44050";
    private static final String KDDI_PLMN_MAIN = "44051";
    private static final String KDDI_TEST_PLMN = "44110";
    private static final boolean OTA_BIP_KDDI = SystemProperties.getBoolean("ro.config.enable_ota_bip_kddi", false);

    public HwCustBipProxyImpl(Context mContext) {
        super(mContext);
    }

    public String getApnString(ChannelSettings newChannel, String channelInfoType) {
        if (newChannel == null || TextUtils.isEmpty(channelInfoType)) {
            return "";
        }
        StringBuilder apnstring = new StringBuilder("[ApnSettingV3] ");
        apnstring.append("bipapn, ");
        apnstring.append(newChannel.networkAccessName);
        apnstring.append(",,");
        apnstring.append(String.valueOf(newChannel.port));
        apnstring.append(",");
        apnstring.append(newChannel.userLogin);
        apnstring.append(",");
        apnstring.append(newChannel.userPassword);
        apnstring.append(",,,,,,,2,");
        apnstring.append(channelInfoType);
        apnstring.append(",IPV4V6,IP,true,0,");
        return apnstring.toString();
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
}
