package com.android.internal.telephony.dataconnection;

import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.uicc.IccRecords;

public class HwCustDataConnectionImpl extends HwCustDataConnection {
    private static final String FOTA_APN = "open-dm2.dcm-dm.ne.jp";
    private static final boolean HWDBG = true;
    private static final boolean IS_DOCOMO;
    private static final String TAG = "HwCustDataConnectionImpl";

    static {
        boolean equals;
        if (SystemProperties.get("ro.config.hw_opta", "").equals("341")) {
            equals = SystemProperties.get("ro.config.hw_optb", "").equals("392");
        } else {
            equals = false;
        }
        IS_DOCOMO = equals;
    }

    private void log(String message) {
        Rlog.d(TAG, message);
    }

    public boolean setMtuIfNeeded(LinkProperties lp, Phone phone) {
        if (phone == null) {
            return false;
        }
        if (phone.mIccRecords == null || phone.mIccRecords.get() == null) {
            return false;
        }
        String mccmnc = ((IccRecords) phone.mIccRecords.get()).getOperatorNumeric();
        String plmnsConfig = System.getString(phone.getContext().getContentResolver(), "hw_set_mtu_by_mccmnc");
        log("mccmnc = " + mccmnc + " plmnsConfig = " + plmnsConfig);
        if (TextUtils.isEmpty(plmnsConfig) || TextUtils.isEmpty(mccmnc) || lp == null) {
            return false;
        }
        for (String plmn : plmnsConfig.split(";")) {
            String[] mcc = plmn.split(",");
            if (mcc.length != 2 || TextUtils.isEmpty(mcc[0]) || TextUtils.isEmpty(mcc[1])) {
                return false;
            }
            String IPType = "";
            int pos = mcc[0].indexOf(":");
            if (pos != -1) {
                IPType = mcc[0].substring(pos + 1);
                log("IPType: " + IPType);
            }
            int mtuValue = Integer.parseInt(mcc[1]);
            if ("all".equals(mcc[0]) || (mcc[0].startsWith(mccmnc) && (TextUtils.isEmpty(IPType) || IPType.equals(getIPType(lp))))) {
                lp.setMtu(mtuValue);
                log("set MTU by cust to " + mtuValue);
                return HWDBG;
            }
        }
        return false;
    }

    private String getIPType(LinkProperties lp) {
        String result = "";
        if (lp == null) {
            return result;
        }
        if (lp.hasIPv4Address() && lp.hasGlobalIPv6Address()) {
            result = "IPV4V6";
        } else if (lp.hasIPv4Address()) {
            result = "IPV4";
        } else if (lp.hasGlobalIPv6Address()) {
            result = "IPV6";
        }
        return result;
    }

    public boolean whetherSetApnByCust(Phone phone) {
        if (phone == null || phone.mIccRecords == null || phone.mIccRecords.get() == null) {
            return false;
        }
        String mccmnc = ((IccRecords) phone.mIccRecords.get()).getOperatorNumeric();
        String plmnsConfig = System.getString(phone.getContext().getContentResolver(), "hw_set_apn_by_mccmnc");
        int dataRadioTech = phone.getServiceState().getRilDataRadioTechnology();
        log("mccmnc = " + mccmnc + " plmnsConfig = " + plmnsConfig + " dataRadioTech = " + dataRadioTech);
        if (TextUtils.isEmpty(plmnsConfig) || TextUtils.isEmpty(mccmnc) || dataRadioTech != 14) {
            return false;
        }
        for (String plmn : plmnsConfig.split(",")) {
            if (plmn.equals(mccmnc)) {
                return HWDBG;
            }
        }
        return false;
    }

    public boolean isNeedReMakeCapability() {
        return IS_DOCOMO;
    }

    public NetworkCapabilities getNetworkCapabilities(String[] types, NetworkCapabilities result, ApnSetting apnSetting, DcTracker dct) {
        if (apnSetting == null || result == null || types == null || dct == null) {
            return result;
        }
        boolean isDcmFotaApn = FOTA_APN.equals(apnSetting.apn);
        log("getNetworkCapabilities isDcmFotaApn = " + isDcmFotaApn);
        for (String type : types) {
            if (type.equals("*")) {
                if (IS_DOCOMO && (isDcmFotaApn ^ 1) != 0) {
                    result.removeCapability(3);
                }
            } else if (type.equals("fota")) {
                if (IS_DOCOMO) {
                    if (isDcmFotaApn) {
                        result.addCapability(12);
                    } else {
                        result.removeCapability(3);
                    }
                }
            } else if (type.equals("dun") && IS_DOCOMO) {
                ApnSetting securedDunApn = dct.fetchDunApn();
                if (securedDunApn == null || securedDunApn.equals(apnSetting)) {
                    result.addCapability(12);
                }
            }
        }
        return result;
    }

    public boolean isEmergencyApnSetting(ApnSetting sApnSetting) {
        if (!SystemProperties.getBoolean("ro.config.emergency_apn_handle", false)) {
            return false;
        }
        String[] types = sApnSetting.types;
        if (types != null) {
            for (String s : types) {
                if ("emergency".equals(s)) {
                    return HWDBG;
                }
            }
        }
        return false;
    }
}
