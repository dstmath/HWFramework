package com.android.internal.telephony.dataconnection;

import android.hardware.radio.V1_0.ApnTypes;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.uicc.IccRecords;
import com.google.android.mms.pdu.CharacterSets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class ApnSetting {
    private static final boolean DBG = false;
    static final String LOG_TAG = "ApnSetting";
    static final String TAG = "ApnSetting";
    static final String V2_FORMAT_REGEX = "^\\[ApnSettingV2\\]\\s*";
    static final String V3_FORMAT_REGEX = "^\\[ApnSettingV3\\]\\s*";
    static final String V4_FORMAT_REGEX = "^\\[ApnSettingV4\\]\\s*";
    static final String V5_FORMAT_REGEX = "^\\[ApnSettingV5\\]\\s*";
    private static final boolean VDBG = false;
    public final String apn;
    public final int apnSetId;
    public final int authType;
    @Deprecated
    public final int bearer;
    @Deprecated
    public final int bearerBitmask;
    public final String carrier;
    public final boolean carrierEnabled;
    public final int id;
    public final int maxConns;
    public final int maxConnsTime;
    public final String mmsPort;
    public final String mmsProxy;
    public final String mmsc;
    public final boolean modemCognitive;
    public final int mtu;
    public final String mvnoMatchData;
    public final String mvnoType;
    public final int networkTypeBitmask;
    public final String numeric;
    public final String password;
    public boolean permanentFailed;
    public final String port;
    public final int profileId;
    public final String protocol;
    public final String proxy;
    public final String roamingProtocol;
    public String[] types;
    public int typesBitmap;
    public final String user;
    public final int waitTime;

    @Deprecated
    public ApnSetting(int id2, String numeric2, String carrier2, String apn2, String proxy2, String port2, String mmsc2, String mmsProxy2, String mmsPort2, String user2, String password2, int authType2, String[] types2, String protocol2, String roamingProtocol2, boolean carrierEnabled2, int bearer2, int bearerBitmask2, int profileId2, boolean modemCognitive2, int maxConns2, int waitTime2, int maxConnsTime2, int mtu2, String mvnoType2, String mvnoMatchData2) {
        String[] strArr = types2;
        this.permanentFailed = false;
        this.id = id2;
        this.numeric = numeric2;
        this.carrier = carrier2;
        this.apn = apn2;
        this.proxy = proxy2;
        this.port = port2;
        this.mmsc = mmsc2;
        this.mmsProxy = mmsProxy2;
        this.mmsPort = mmsPort2;
        this.user = user2;
        this.password = password2;
        this.authType = authType2;
        this.types = new String[strArr.length];
        int i = 0;
        int apnBitmap = 0;
        while (i < strArr.length) {
            this.types[i] = strArr[i].toLowerCase();
            apnBitmap |= getApnBitmask(this.types[i]);
            i++;
            int i2 = id2;
            String str = numeric2;
        }
        this.typesBitmap = apnBitmap;
        this.protocol = protocol2;
        this.roamingProtocol = roamingProtocol2;
        this.carrierEnabled = carrierEnabled2;
        this.bearer = bearer2;
        this.bearerBitmask = bearerBitmask2 | ServiceState.getBitmaskForTech(bearer2);
        this.profileId = profileId2;
        this.modemCognitive = modemCognitive2;
        this.maxConns = maxConns2;
        this.waitTime = waitTime2;
        this.maxConnsTime = maxConnsTime2;
        this.mtu = mtu2;
        this.mvnoType = mvnoType2;
        this.mvnoMatchData = mvnoMatchData2;
        this.apnSetId = 0;
        this.networkTypeBitmask = ServiceState.convertBearerBitmaskToNetworkTypeBitmask(this.bearerBitmask);
    }

    public ApnSetting(int id2, String numeric2, String carrier2, String apn2, String proxy2, String port2, String mmsc2, String mmsProxy2, String mmsPort2, String user2, String password2, int authType2, String[] types2, String protocol2, String roamingProtocol2, boolean carrierEnabled2, int networkTypeBitmask2, int profileId2, boolean modemCognitive2, int maxConns2, int waitTime2, int maxConnsTime2, int mtu2, String mvnoType2, String mvnoMatchData2) {
        this(id2, numeric2, carrier2, apn2, proxy2, port2, mmsc2, mmsProxy2, mmsPort2, user2, password2, authType2, types2, protocol2, roamingProtocol2, carrierEnabled2, networkTypeBitmask2, profileId2, modemCognitive2, maxConns2, waitTime2, maxConnsTime2, mtu2, mvnoType2, mvnoMatchData2, 0);
    }

    public ApnSetting(int id2, String numeric2, String carrier2, String apn2, String proxy2, String port2, String mmsc2, String mmsProxy2, String mmsPort2, String user2, String password2, int authType2, String[] types2, String protocol2, String roamingProtocol2, boolean carrierEnabled2, int networkTypeBitmask2, int profileId2, boolean modemCognitive2, int maxConns2, int waitTime2, int maxConnsTime2, int mtu2, String mvnoType2, String mvnoMatchData2, int apnSetId2) {
        String[] strArr = types2;
        this.permanentFailed = false;
        this.id = id2;
        this.numeric = numeric2;
        this.carrier = carrier2;
        this.apn = apn2;
        this.proxy = proxy2;
        this.port = port2;
        this.mmsc = mmsc2;
        this.mmsProxy = mmsProxy2;
        this.mmsPort = mmsPort2;
        this.user = user2;
        this.password = password2;
        this.authType = authType2;
        this.types = new String[strArr.length];
        int i = 0;
        int apnBitmap = 0;
        while (i < strArr.length) {
            this.types[i] = strArr[i].toLowerCase();
            apnBitmap |= getApnBitmask(this.types[i]);
            i++;
            int i2 = id2;
            String str = numeric2;
        }
        this.typesBitmap = apnBitmap;
        this.protocol = protocol2;
        this.roamingProtocol = roamingProtocol2;
        this.carrierEnabled = carrierEnabled2;
        this.bearer = 0;
        this.bearerBitmask = ServiceState.convertNetworkTypeBitmaskToBearerBitmask(networkTypeBitmask2);
        this.networkTypeBitmask = networkTypeBitmask2;
        this.profileId = profileId2;
        this.modemCognitive = modemCognitive2;
        this.maxConns = maxConns2;
        this.waitTime = waitTime2;
        this.maxConnsTime = maxConnsTime2;
        this.mtu = mtu2;
        this.mvnoType = mvnoType2;
        this.mvnoMatchData = mvnoMatchData2;
        this.apnSetId = apnSetId2;
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    public ApnSetting(ApnSetting apn2) {
        this(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r0.carrierEnabled, r0.networkTypeBitmask, r0.profileId, r0.modemCognitive, r0.maxConns, r0.waitTime, r0.maxConnsTime, r0.mtu, r0.mvnoType, r0.mvnoMatchData, r0.apnSetId);
        ApnSetting apnSetting = apn2;
        int i = apnSetting.id;
        String str = apnSetting.numeric;
        String str2 = apnSetting.carrier;
        String str3 = apnSetting.apn;
        String str4 = apnSetting.proxy;
        String str5 = apnSetting.port;
        String str6 = apnSetting.mmsc;
        String str7 = apnSetting.mmsProxy;
        String str8 = apnSetting.mmsPort;
        String str9 = apnSetting.user;
        String str10 = apnSetting.password;
        int i2 = apnSetting.authType;
        String[] strArr = apnSetting.types;
        String str11 = apnSetting.protocol;
        String str12 = apnSetting.roamingProtocol;
        String str13 = str12;
        String str14 = str11;
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x005d A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x005e  */
    public static ApnSetting fromString(String data) {
        String data2;
        int version;
        String[] a;
        int authType2;
        int apnSetId2;
        String mvnoMatchData2;
        String mvnoType2;
        int mtu2;
        int maxConnsTime2;
        String[] typeArray;
        int waitTime2;
        int maxConns2;
        boolean modemCognitive2;
        boolean carrierEnabled2;
        String roamingProtocol2;
        String protocol2;
        int profileId2;
        String data3;
        String str = data;
        if (str == null) {
            return null;
        }
        if (str.matches("^\\[ApnSettingV5\\]\\s*.*")) {
            version = 5;
            data3 = str.replaceFirst(V5_FORMAT_REGEX, "");
        } else if (str.matches("^\\[ApnSettingV4\\]\\s*.*")) {
            version = 4;
            data3 = str.replaceFirst(V4_FORMAT_REGEX, "");
        } else if (str.matches("^\\[ApnSettingV3\\]\\s*.*")) {
            version = 3;
            data3 = str.replaceFirst(V3_FORMAT_REGEX, "");
        } else if (str.matches("^\\[ApnSettingV2\\]\\s*.*")) {
            version = 2;
            data3 = str.replaceFirst(V2_FORMAT_REGEX, "");
        } else {
            data2 = str;
            version = 1;
            a = data2.split("\\s*,\\s*");
            if (a.length >= 14) {
                return null;
            }
            try {
                authType2 = Integer.parseInt(a[12]);
            } catch (NumberFormatException e) {
                authType2 = 0;
            }
            int bearerBitmask2 = 0;
            int networkTypeBitmask2 = 0;
            int profileId3 = 0;
            boolean modemCognitive3 = false;
            int maxConns3 = 0;
            int waitTime3 = 0;
            int maxConnsTime3 = 0;
            int mtu3 = 0;
            String mvnoType3 = "";
            String mvnoMatchData3 = "";
            if (version == 1) {
                String[] typeArray2 = new String[(a.length - 13)];
                System.arraycopy(a, 13, typeArray2, 0, a.length - 13);
                protocol2 = "IP";
                roamingProtocol2 = "IP";
                typeArray = typeArray2;
                profileId2 = 0;
                modemCognitive2 = false;
                maxConns2 = 0;
                waitTime2 = 0;
                maxConnsTime2 = 0;
                mtu2 = 0;
                mvnoType2 = mvnoType3;
                mvnoMatchData2 = mvnoMatchData3;
                apnSetId2 = 0;
                carrierEnabled2 = true;
            } else if (a.length < 18) {
                return null;
            } else {
                String[] typeArray3 = a[13].split("\\s*\\|\\s*");
                protocol2 = a[14];
                roamingProtocol2 = a[15];
                carrierEnabled2 = Boolean.parseBoolean(a[16]);
                int bearerBitmask3 = ServiceState.getBitmaskFromString(a[17]);
                typeArray = typeArray3;
                if (a.length > 22) {
                    modemCognitive3 = Boolean.parseBoolean(a[19]);
                    try {
                        profileId3 = Integer.parseInt(a[18]);
                        maxConns3 = Integer.parseInt(a[20]);
                        waitTime3 = Integer.parseInt(a[21]);
                        maxConnsTime3 = Integer.parseInt(a[22]);
                    } catch (NumberFormatException e2) {
                    }
                }
                if (a.length > 23) {
                    try {
                        mtu3 = Integer.parseInt(a[23]);
                    } catch (NumberFormatException e3) {
                    }
                }
                if (a.length > 25) {
                    mvnoType3 = a[24];
                    mvnoMatchData3 = a[25];
                }
                if (a.length > 26) {
                    networkTypeBitmask2 = ServiceState.getBitmaskFromString(a[26]);
                }
                if (a.length > 27) {
                    apnSetId2 = Integer.parseInt(a[27]);
                    profileId2 = profileId3;
                    modemCognitive2 = modemCognitive3;
                    maxConns2 = maxConns3;
                    waitTime2 = waitTime3;
                    maxConnsTime2 = maxConnsTime3;
                    mtu2 = mtu3;
                    mvnoType2 = mvnoType3;
                    mvnoMatchData2 = mvnoMatchData3;
                } else {
                    profileId2 = profileId3;
                    modemCognitive2 = modemCognitive3;
                    maxConns2 = maxConns3;
                    waitTime2 = waitTime3;
                    maxConnsTime2 = maxConnsTime3;
                    mtu2 = mtu3;
                    mvnoType2 = mvnoType3;
                    mvnoMatchData2 = mvnoMatchData3;
                    apnSetId2 = 0;
                }
                bearerBitmask2 = bearerBitmask3;
            }
            if (networkTypeBitmask2 == 0) {
                networkTypeBitmask2 = ServiceState.convertBearerBitmaskToNetworkTypeBitmask(bearerBitmask2);
            }
            ApnSetting apnSetting = new ApnSetting(-1, a[10] + a[11], a[0], a[1], a[2], a[3], a[7], a[8], a[9], a[4], a[5], authType2, typeArray, protocol2, roamingProtocol2, carrierEnabled2, networkTypeBitmask2, profileId2, modemCognitive2, maxConns2, waitTime2, maxConnsTime2, mtu2, mvnoType2, mvnoMatchData2, apnSetId2);
            return apnSetting;
        }
        data2 = data3;
        a = data2.split("\\s*,\\s*");
        if (a.length >= 14) {
        }
    }

    public static List<ApnSetting> arrayFromString(String data) {
        List<ApnSetting> retVal = new ArrayList<>();
        if (TextUtils.isEmpty(data)) {
            return retVal;
        }
        for (String apnString : data.split("\\s*;\\s*")) {
            ApnSetting apn2 = fromString(apnString);
            if (apn2 != null) {
                retVal.add(apn2);
            }
        }
        return retVal;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ApnSettingV5] ");
        sb.append(this.carrier);
        sb.append(", ");
        sb.append(this.id);
        sb.append(", ");
        sb.append(this.numeric);
        sb.append(", ");
        sb.append(this.apn);
        sb.append(", ");
        sb.append(this.proxy);
        sb.append(", ");
        sb.append(this.mmsc);
        sb.append(", ");
        sb.append(this.mmsProxy);
        sb.append(", ");
        sb.append(this.mmsPort);
        sb.append(", ");
        sb.append(this.port);
        sb.append(", ");
        sb.append(this.authType);
        sb.append(", ");
        for (int i = 0; i < this.types.length; i++) {
            sb.append(this.types[i]);
            if (i < this.types.length - 1) {
                sb.append(" | ");
            }
        }
        sb.append(", ");
        sb.append(this.protocol);
        sb.append(", ");
        sb.append(this.roamingProtocol);
        sb.append(", ");
        sb.append(this.carrierEnabled);
        sb.append(", ");
        sb.append(this.bearer);
        sb.append(", ");
        sb.append(this.bearerBitmask);
        sb.append(", ");
        sb.append(this.profileId);
        sb.append(", ");
        sb.append(this.modemCognitive);
        sb.append(", ");
        sb.append(this.maxConns);
        sb.append(", ");
        sb.append(this.waitTime);
        sb.append(", ");
        sb.append(this.maxConnsTime);
        sb.append(", ");
        sb.append(this.mtu);
        sb.append(", ");
        sb.append(this.mvnoType);
        sb.append(", ");
        sb.append(this.mvnoMatchData);
        sb.append(", ");
        sb.append(this.permanentFailed);
        sb.append(", ");
        sb.append(this.networkTypeBitmask);
        sb.append(", ");
        sb.append(this.apnSetId);
        return sb.toString();
    }

    public boolean hasMvnoParams() {
        return !TextUtils.isEmpty(this.mvnoType) && !TextUtils.isEmpty(this.mvnoMatchData);
    }

    public boolean canHandleType(String type) {
        if (!this.carrierEnabled) {
            return false;
        }
        boolean wildcardable = true;
        if ("ia".equalsIgnoreCase(type)) {
            wildcardable = false;
        }
        for (String t : this.types) {
            if (t.equalsIgnoreCase(type) || ((wildcardable && t.equalsIgnoreCase(CharacterSets.MIMENAME_ANY_CHARSET)) || (t.equalsIgnoreCase("default") && type.equalsIgnoreCase("hipri")))) {
                return true;
            }
        }
        return false;
    }

    private static boolean iccidMatches(String mvnoData, String iccId) {
        for (String mvnoIccid : mvnoData.split(",")) {
            if (iccId.startsWith(mvnoIccid)) {
                Log.d("ApnSetting", "mvno icc id match found");
                return true;
            }
        }
        return false;
    }

    private static boolean imsiMatches(String imsiDB, String imsiSIM) {
        int len = imsiDB.length();
        if (len <= 0 || len > imsiSIM.length()) {
            return false;
        }
        for (int idx = 0; idx < len; idx++) {
            char c = imsiDB.charAt(idx);
            if (c != 'x' && c != 'X' && c != imsiSIM.charAt(idx)) {
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x006a A[RETURN] */
    public static boolean mvnoMatches(IccRecords r, String mvnoType2, String mvnoMatchData2) {
        if (mvnoType2.equalsIgnoreCase("spn")) {
            return r.getServiceProviderName() != null && r.getServiceProviderName().equalsIgnoreCase(mvnoMatchData2);
        }
        if (mvnoType2.equalsIgnoreCase("imsi")) {
            String imsiSIM = r.getIMSI();
            if (imsiSIM != null && imsiMatches(mvnoMatchData2, imsiSIM)) {
                return true;
            }
        } else if (mvnoType2.equalsIgnoreCase("gid")) {
            String gid1 = r.getGid1();
            int mvno_match_data_length = mvnoMatchData2.length();
            if (gid1 != null && gid1.length() >= mvno_match_data_length && gid1.substring(0, mvno_match_data_length).equalsIgnoreCase(mvnoMatchData2)) {
                return true;
            }
        } else if (mvnoType2.equalsIgnoreCase("iccid")) {
            String iccId = r.getIccId();
            if (iccId != null && iccidMatches(mvnoMatchData2, iccId)) {
                return true;
            }
        }
    }

    public static boolean isMeteredApnType(String type, Phone phone) {
        String carrierConfig;
        if (phone == null) {
            return true;
        }
        boolean isRoaming = phone.getServiceState().getDataRoaming();
        boolean isIwlan = phone.getServiceState().getRilDataRadioTechnology() == 18;
        int subId = phone.getSubId();
        if (isIwlan) {
            carrierConfig = "carrier_metered_iwlan_apn_types_strings";
        } else if (isRoaming) {
            carrierConfig = "carrier_metered_roaming_apn_types_strings";
        } else {
            carrierConfig = "carrier_metered_apn_types_strings";
        }
        CarrierConfigManager configManager = (CarrierConfigManager) phone.getContext().getSystemService("carrier_config");
        if (configManager == null) {
            Rlog.e("ApnSetting", "Carrier config service is not available");
            return true;
        }
        PersistableBundle b = configManager.getConfigForSubId(subId);
        if (b == null) {
            Rlog.e("ApnSetting", "Can't get the config. subId = " + subId);
            return true;
        }
        String[] meteredApnTypes = b.getStringArray(carrierConfig);
        if (meteredApnTypes == null) {
            Rlog.e("ApnSetting", carrierConfig + " is not available. subId = " + subId);
            return true;
        }
        HashSet<String> meteredApnSet = new HashSet<>(Arrays.asList(meteredApnTypes));
        if (meteredApnSet.contains(CharacterSets.MIMENAME_ANY_CHARSET) || meteredApnSet.contains(type)) {
            return true;
        }
        if (!type.equals(CharacterSets.MIMENAME_ANY_CHARSET) || meteredApnSet.size() <= 0) {
            return false;
        }
        return true;
    }

    public boolean isMetered(Phone phone) {
        if (phone == null) {
            return true;
        }
        for (String type : this.types) {
            if (isMeteredApnType(type, phone)) {
                return true;
            }
        }
        return false;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof ApnSetting)) {
            return false;
        }
        ApnSetting other = (ApnSetting) o;
        if (this.carrier.equals(other.carrier) && this.id == other.id && this.numeric.equals(other.numeric) && this.apn.equals(other.apn) && this.proxy.equals(other.proxy) && this.mmsc.equals(other.mmsc) && this.mmsProxy.equals(other.mmsProxy) && TextUtils.equals(this.mmsPort, other.mmsPort) && this.port.equals(other.port) && TextUtils.equals(this.user, other.user) && TextUtils.equals(this.password, other.password) && this.authType == other.authType && Arrays.deepEquals(this.types, other.types) && this.typesBitmap == other.typesBitmap && this.protocol.equals(other.protocol) && this.roamingProtocol.equals(other.roamingProtocol) && this.carrierEnabled == other.carrierEnabled && this.bearer == other.bearer && this.bearerBitmask == other.bearerBitmask && this.profileId == other.profileId && this.modemCognitive == other.modemCognitive && this.maxConns == other.maxConns && this.waitTime == other.waitTime && this.maxConnsTime == other.maxConnsTime && this.mtu == other.mtu && this.mvnoType.equals(other.mvnoType) && this.mvnoMatchData.equals(other.mvnoMatchData) && this.networkTypeBitmask == other.networkTypeBitmask && this.apnSetId == other.apnSetId) {
            z = true;
        }
        return z;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public boolean equals(Object o, boolean isDataRoaming) {
        boolean z = false;
        if (!(o instanceof ApnSetting)) {
            return false;
        }
        ApnSetting other = (ApnSetting) o;
        if (this.carrier.equals(other.carrier) && this.numeric.equals(other.numeric) && this.apn.equals(other.apn) && this.proxy.equals(other.proxy) && this.mmsc.equals(other.mmsc) && this.mmsProxy.equals(other.mmsProxy) && TextUtils.equals(this.mmsPort, other.mmsPort) && this.port.equals(other.port) && TextUtils.equals(this.user, other.user) && TextUtils.equals(this.password, other.password) && this.authType == other.authType && Arrays.deepEquals(this.types, other.types) && this.typesBitmap == other.typesBitmap && ((isDataRoaming || this.protocol.equals(other.protocol)) && ((!isDataRoaming || this.roamingProtocol.equals(other.roamingProtocol)) && this.carrierEnabled == other.carrierEnabled && this.profileId == other.profileId && this.modemCognitive == other.modemCognitive && this.maxConns == other.maxConns && this.waitTime == other.waitTime && this.maxConnsTime == other.maxConnsTime && this.mtu == other.mtu && this.mvnoType.equals(other.mvnoType) && this.mvnoMatchData.equals(other.mvnoMatchData) && this.apnSetId == other.apnSetId))) {
            z = true;
        }
        return z;
    }

    public boolean similar(ApnSetting other) {
        return !canHandleType("dun") && !other.canHandleType("dun") && Objects.equals(this.apn, other.apn) && !typeSameAny(this, other) && xorEquals(this.proxy, other.proxy) && xorEquals(this.port, other.port) && xorEquals(this.protocol, other.protocol) && xorEquals(this.roamingProtocol, other.roamingProtocol) && this.carrierEnabled == other.carrierEnabled && this.bearerBitmask == other.bearerBitmask && this.profileId == other.profileId && Objects.equals(this.mvnoType, other.mvnoType) && Objects.equals(this.mvnoMatchData, other.mvnoMatchData) && xorEquals(this.mmsc, other.mmsc) && xorEquals(this.mmsProxy, other.mmsProxy) && xorEquals(this.mmsPort, other.mmsPort) && this.networkTypeBitmask == other.networkTypeBitmask && this.apnSetId == other.apnSetId;
    }

    private boolean typeSameAny(ApnSetting first, ApnSetting second) {
        for (int index1 = 0; index1 < first.types.length; index1++) {
            for (int index2 = 0; index2 < second.types.length; index2++) {
                if (first.types[index1].equals(CharacterSets.MIMENAME_ANY_CHARSET) || second.types[index2].equals(CharacterSets.MIMENAME_ANY_CHARSET) || first.types[index1].equals(second.types[index2])) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean xorEquals(String first, String second) {
        return Objects.equals(first, second) || TextUtils.isEmpty(first) || TextUtils.isEmpty(second);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private static int getApnBitmask(String apn2) {
        char c;
        switch (apn2.hashCode()) {
            case 42:
                if (apn2.equals(CharacterSets.MIMENAME_ANY_CHARSET)) {
                    c = 10;
                    break;
                }
            case 3352:
                if (apn2.equals("ia")) {
                    c = 8;
                    break;
                }
            case 98292:
                if (apn2.equals("cbs")) {
                    c = 7;
                    break;
                }
            case 99837:
                if (apn2.equals("dun")) {
                    c = 3;
                    break;
                }
            case 104399:
                if (apn2.equals("ims")) {
                    c = 6;
                    break;
                }
            case 108243:
                if (apn2.equals("mms")) {
                    c = 1;
                    break;
                }
            case 3149046:
                if (apn2.equals("fota")) {
                    c = 5;
                    break;
                }
            case 3541982:
                if (apn2.equals("supl")) {
                    c = 2;
                    break;
                }
            case 99285510:
                if (apn2.equals("hipri")) {
                    c = 4;
                    break;
                }
            case 1544803905:
                if (apn2.equals("default")) {
                    c = 0;
                    break;
                }
            case 1629013393:
                if (apn2.equals("emergency")) {
                    c = 9;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 4;
            case 3:
                return 8;
            case 4:
                return 16;
            case 5:
                return 32;
            case 6:
                return 64;
            case 7:
                return 128;
            case 8:
                return 256;
            case 9:
                return 512;
            case 10:
                return ApnTypes.ALL;
            default:
                return 0;
        }
    }

    protected static int getApnBitmaskEx(String apnType) {
        return getApnBitmask(apnType);
    }
}
