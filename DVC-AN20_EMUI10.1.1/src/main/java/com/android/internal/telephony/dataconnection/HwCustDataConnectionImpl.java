package com.android.internal.telephony.dataconnection;

import android.common.HwCfgKey;
import android.content.ContentResolver;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.uicc.IccRecords;
import huawei.cust.HwGetCfgFileConfig;
import java.util.ArrayList;

public class HwCustDataConnectionImpl extends HwCustDataConnection {
    private static final String DEFAULT_PCO_DATA = "-2;-2";
    private static final String DEFAULT_PCO_VALUE = "-2";
    private static final String FOTA_APN = "open-dm2.dcm-dm.ne.jp";
    private static final boolean HWDBG = true;
    private static final boolean HW_SIM_ACTIVATION = SystemProperties.getBoolean("ro.config.hw_sim_activation", false);
    private static final int INTERNET_PCO_TYPE = 3;
    private static final boolean IS_DOCOMO = ((!SystemProperties.get("ro.config.hw_opta", "").equals("341") || !SystemProperties.get("ro.config.hw_optb", "").equals("392")) ? false : HWDBG);
    private static final String PCO_DATA = "pco_data";
    private static final String SPLIT = ";";
    private static final String TAG = "HwCustDataConnectionImpl";

    private void log(String message) {
        Rlog.d(TAG, message);
    }

    public boolean setMtuIfNeeded(LinkProperties lp, Phone phone) {
        if (phone == null) {
            return false;
        }
        char c = 1;
        try {
            Integer custMtu = (Integer) HwGetCfgFileConfig.getCfgFileData(new HwCfgKey("set_mtu", "ip", (String) null, (String) null, "mtu", getIPType(lp), (String) null, (String) null, phone.getPhoneId()), Integer.class);
            if (custMtu != null) {
                int mtu = custMtu.intValue();
                lp.setMtu(mtu);
                log("HwCfgFile:set MTU by cust to " + mtu);
                return HWDBG;
            }
        } catch (Exception e) {
            log("Exception: read set_mtu error in setMtuIfNeeded");
        }
        IccRecords iccRecords = phone.getIccRecords();
        if (iccRecords == null) {
            return false;
        }
        String mccmnc = iccRecords.getOperatorNumeric();
        String plmnsConfig = Settings.System.getString(phone.getContext().getContentResolver(), "hw_set_mtu_by_mccmnc");
        log("mccmnc = " + mccmnc + " plmnsConfig = " + plmnsConfig);
        if (TextUtils.isEmpty(plmnsConfig) || TextUtils.isEmpty(mccmnc) || lp == null) {
            return false;
        }
        String[] plmns = plmnsConfig.split(SPLIT);
        int length = plmns.length;
        int i = 0;
        while (i < length) {
            String[] mcc = plmns[i].split(",");
            if (mcc.length != 2 || TextUtils.isEmpty(mcc[0]) || TextUtils.isEmpty(mcc[c])) {
                return false;
            }
            String IPType = "";
            int pos = mcc[0].indexOf(":");
            if (pos != -1) {
                IPType = mcc[0].substring(pos + 1);
                log("IPType: " + IPType);
            }
            int mtuValue = Integer.parseInt(mcc[c]);
            if ("all".equals(mcc[0]) || (mcc[0].startsWith(mccmnc) && (TextUtils.isEmpty(IPType) || IPType.equals(getIPType(lp))))) {
                lp.setMtu(mtuValue);
                log("set MTU by cust to " + mtuValue);
                return HWDBG;
            }
            i++;
            c = 1;
        }
        return false;
    }

    private String getIPType(LinkProperties lp) {
        if (lp == null) {
            return "";
        }
        if (lp.hasIPv4Address() && lp.hasGlobalIPv6Address()) {
            return "IPV4V6";
        }
        if (lp.hasIPv4Address()) {
            return "IPV4";
        }
        if (lp.hasGlobalIPv6Address()) {
            return "IPV6";
        }
        return "";
    }

    public boolean whetherSetApnByCust(Phone phone) {
        if (phone == null || phone.getIccRecords() == null) {
            return false;
        }
        String mccmnc = phone.getIccRecords().getOperatorNumeric();
        String plmnsConfig = Settings.System.getString(phone.getContext().getContentResolver(), "hw_set_apn_by_mccmnc");
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
        boolean isDcmFotaApn = FOTA_APN.equals(apnSetting.getApnName());
        log("getNetworkCapabilities isDcmFotaApn = " + isDcmFotaApn);
        int length = types.length;
        for (int i = 0; i < length; i++) {
            String type = types[i];
            char c = 65535;
            int hashCode = type.hashCode();
            if (hashCode != 42) {
                if (hashCode != 99837) {
                    if (hashCode == 3149046 && type.equals("fota")) {
                        c = 1;
                    }
                } else if (type.equals("dun")) {
                    c = 2;
                }
            } else if (type.equals("*")) {
                c = 0;
            }
            if (c != 0) {
                if (c != 1) {
                    if (c == 2 && IS_DOCOMO) {
                        ArrayList<ApnSetting> securedDunApns = dct.fetchDunApns();
                        if (securedDunApns == null || securedDunApns.size() == 0) {
                            result.addCapability(12);
                        } else {
                            int size = securedDunApns.size();
                            int i2 = 0;
                            while (true) {
                                if (i2 >= size) {
                                    break;
                                } else if (apnSetting.equals(securedDunApns.get(i2))) {
                                    result.addCapability(12);
                                    break;
                                } else {
                                    i2++;
                                }
                            }
                        }
                    }
                } else if (IS_DOCOMO) {
                    if (isDcmFotaApn) {
                        result.addCapability(12);
                    } else {
                        result.removeCapability(INTERNET_PCO_TYPE);
                    }
                }
            } else if (IS_DOCOMO && !isDcmFotaApn) {
                result.removeCapability(INTERNET_PCO_TYPE);
            }
        }
        return result;
    }

    public void clearInternetPcoValue(int profileId, Phone phone) {
        if (HW_SIM_ACTIVATION) {
            log("profileId is : " + profileId);
            if (profileId == INTERNET_PCO_TYPE) {
                ContentResolver mResolver = phone.getContext().getContentResolver();
                String pcoValue = getPcoValueFromSetting(mResolver)[0].concat(SPLIT).concat(DEFAULT_PCO_VALUE);
                Settings.Global.putString(mResolver, PCO_DATA, pcoValue);
                log("setting pco values is : " + pcoValue);
            }
        }
    }

    private String[] getPcoValueFromSetting(ContentResolver mResolver) {
        String[] pcoValues = Settings.Global.getString(mResolver, PCO_DATA).split(SPLIT);
        if (pcoValues == null || pcoValues.length != 2) {
            return new String[]{DEFAULT_PCO_VALUE, DEFAULT_PCO_VALUE};
        }
        return pcoValues;
    }

    public boolean isEmergencyApnSetting(ApnSetting sApnSetting) {
        String types;
        if (SystemProperties.getBoolean("ro.config.emergency_apn_handle", false) && (types = ApnSetting.getApnTypesStringFromBitmask(sApnSetting.getApnTypeBitmask())) != null) {
            for (String s : types.split(",")) {
                if ("emergency".equals(s)) {
                    return HWDBG;
                }
            }
        }
        return false;
    }
}
