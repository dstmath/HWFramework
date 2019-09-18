package com.android.internal.telephony.dataconnection;

import android.common.HwCfgKey;
import android.content.ContentResolver;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
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

    /* JADX WARNING: type inference failed for: r3v0 */
    /* JADX WARNING: type inference failed for: r3v1, types: [boolean] */
    /* JADX WARNING: type inference failed for: r3v5 */
    public boolean setMtuIfNeeded(LinkProperties lp, Phone phone) {
        LinkProperties linkProperties = lp;
        ? r3 = 0;
        if (phone == null) {
            return false;
        }
        char c = 1;
        try {
            HwCfgKey keyCollection = new HwCfgKey("set_mtu", "ip", null, null, "mtu", getIPType(lp), null, null, SubscriptionManager.getSlotIndex(phone.getPhoneId()));
            Integer custMtu = (Integer) HwGetCfgFileConfig.getCfgFileData(keyCollection, Integer.class);
            if (custMtu != null) {
                linkProperties.setMtu(custMtu.intValue());
                log("HwCfgFile:set MTU by cust to " + mtu);
                return HWDBG;
            }
        } catch (Exception e) {
            log("Exception: read set_mtu error " + e.toString());
        }
        IccRecords mIccRecords = phone.getIccRecords();
        if (mIccRecords == null) {
            return false;
        }
        String mccmnc = mIccRecords.getOperatorNumeric();
        String plmnsConfig = Settings.System.getString(phone.getContext().getContentResolver(), "hw_set_mtu_by_mccmnc");
        log("mccmnc = " + mccmnc + " plmnsConfig = " + plmnsConfig);
        if (TextUtils.isEmpty(plmnsConfig) || TextUtils.isEmpty(mccmnc) || linkProperties == null) {
            return false;
        }
        String[] plmns = plmnsConfig.split(SPLIT);
        int length = plmns.length;
        int i = 0;
        while (i < length) {
            String[] mcc = plmns[i].split(",");
            if (mcc.length != 2 || TextUtils.isEmpty(mcc[r3]) || TextUtils.isEmpty(mcc[c])) {
                return false;
            }
            String IPType = "";
            int pos = mcc[r3].indexOf(":");
            if (pos != -1) {
                IPType = mcc[r3].substring(pos + 1);
                log("IPType: " + IPType);
            }
            int mtuValue = Integer.parseInt(mcc[c]);
            if ("all".equals(mcc[0]) || (mcc[0].startsWith(mccmnc) && (TextUtils.isEmpty(IPType) || IPType.equals(getIPType(lp))))) {
                linkProperties.setMtu(mtuValue);
                log("set MTU by cust to " + mtuValue);
                return HWDBG;
            }
            i++;
            r3 = 0;
            c = 1;
        }
        return r3;
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
        int dataRadioTech;
        if (phone == null || phone.mIccRecords == null || phone.mIccRecords.get() == null) {
            return false;
        }
        String mccmnc = ((IccRecords) phone.mIccRecords.get()).getOperatorNumeric();
        String plmnsConfig = Settings.System.getString(phone.getContext().getContentResolver(), "hw_set_apn_by_mccmnc");
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

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x00b5 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0077  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00a6  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00b0  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0106  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x0125  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0145  */
    public NetworkCapabilities getNetworkCapabilities(ArrayList<String> requestedApnTypes, String[] supportedApnTypes, NetworkCapabilities result, ApnSetting apnSetting, DcTracker dct) {
        char c;
        char c2;
        ArrayList<String> arrayList = requestedApnTypes;
        String[] strArr = supportedApnTypes;
        NetworkCapabilities networkCapabilities = result;
        ApnSetting apnSetting2 = apnSetting;
        DcTracker dcTracker = dct;
        if (apnSetting2 == null || networkCapabilities == null || arrayList == null || strArr == null || dcTracker == null) {
            return networkCapabilities;
        }
        if (IS_DOCOMO) {
            boolean isDcmFotaApn = FOTA_APN.equals(apnSetting2.apn);
            log("getNetworkCapabilities isDcmFotaApn = " + isDcmFotaApn);
            for (String type : strArr) {
                int hashCode = type.hashCode();
                if (hashCode != 42) {
                    if (hashCode != 99837) {
                        if (hashCode == 3149046 && type.equals("fota")) {
                            c2 = 1;
                            switch (c2) {
                                case 0:
                                    if (isDcmFotaApn) {
                                        break;
                                    } else {
                                        networkCapabilities.removeCapability(INTERNET_PCO_TYPE);
                                        break;
                                    }
                                case 1:
                                    if (!isDcmFotaApn) {
                                        networkCapabilities.removeCapability(INTERNET_PCO_TYPE);
                                        break;
                                    } else {
                                        networkCapabilities.addCapability(12);
                                        break;
                                    }
                                case 2:
                                    ArrayList<ApnSetting> securedDunApns = dct.fetchDunApns();
                                    if (securedDunApns == null || securedDunApns.size() == 0) {
                                        networkCapabilities.addCapability(12);
                                        break;
                                    } else {
                                        int size = securedDunApns.size();
                                        int i = 0;
                                        while (true) {
                                            int i2 = i;
                                            if (i2 >= size) {
                                                break;
                                            } else if (apnSetting2.equals(securedDunApns.get(i2))) {
                                                networkCapabilities.addCapability(12);
                                                break;
                                            } else {
                                                i = i2 + 1;
                                            }
                                        }
                                    }
                                    break;
                            }
                        }
                    } else if (type.equals("dun")) {
                        c2 = 2;
                        switch (c2) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                        }
                    }
                } else if (type.equals("*")) {
                    c2 = 0;
                    switch (c2) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                }
                c2 = 65535;
                switch (c2) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
            return networkCapabilities;
        } else if (dcTracker.mHwCustDcTracker == null) {
            return networkCapabilities;
        } else {
            int radioTech = dcTracker.mPhone.getServiceState().getRilDataRadioTechnology();
            for (String type2 : strArr) {
                int hashCode2 = type2.hashCode();
                if (hashCode2 != 42) {
                    if (hashCode2 != 108243) {
                        if (hashCode2 == 3673178 && type2.equals("xcap")) {
                            c = 2;
                            switch (c) {
                                case 0:
                                    if (!arrayList.contains("mms") && dcTracker.mHwCustDcTracker.hasBetterApnByBearer(apnSetting2, dct.getAllApnList(), "mms", radioTech)) {
                                        log("mms type has other better apnsettings, not add to net cap");
                                        networkCapabilities.removeCapability(0);
                                    }
                                    if (!arrayList.contains("xcap") && dcTracker.mHwCustDcTracker.hasBetterApnByBearer(apnSetting2, dct.getAllApnList(), "xcap", radioTech)) {
                                        log("xcap type has other better apnsettings, not add to net cap");
                                        networkCapabilities.removeCapability(9);
                                        continue;
                                        continue;
                                        continue;
                                        continue;
                                    }
                                    break;
                                case 1:
                                    if (!arrayList.contains("mms") && dcTracker.mHwCustDcTracker.hasBetterApnByBearer(apnSetting2, dct.getAllApnList(), "mms", radioTech)) {
                                        log("mms type has other better apnsettings, not add to net cap");
                                        networkCapabilities.removeCapability(0);
                                        continue;
                                        continue;
                                        continue;
                                        continue;
                                    }
                                case 2:
                                    if (!arrayList.contains("xcap") && dcTracker.mHwCustDcTracker.hasBetterApnByBearer(apnSetting2, dct.getAllApnList(), "xcap", radioTech)) {
                                        log("xcap type has other better apnsettings, not add to net cap");
                                        networkCapabilities.removeCapability(9);
                                    }
                                    break;
                            }
                        }
                    } else if (type2.equals("mms")) {
                        c = 1;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                        }
                    }
                } else if (type2.equals("*")) {
                    c = 0;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                }
                c = 65535;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
            return networkCapabilities;
        }
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
