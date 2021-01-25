package com.huawei.android.telephony.data;

import android.net.Uri;
import android.telephony.data.ApnSetting;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ApnSettingEx {
    public static final int TYPE_NONE = 0;
    private ApnSetting mApnSetting;

    public static ApnSetting makeApnSetting(int id, String operatorNumeric, String entryName, String apnName, String proxyAddress, int proxyPort, Uri mmsc, String mmsProxyAddress, int mmsProxyPort, String user, String password, int authType, int apnTypeBitmask, int protocol, int roamingProtocol, boolean carrierEnabled, int networkTypeBitmask, int profileId, boolean modemCognitive, int maxConns, int waitTime, int maxConnsTime, int mtu, int mvnoType, String mvnoMatchData, int apnSetId, int carrierId, int skip464xlat) {
        return ApnSetting.makeApnSetting(id, operatorNumeric, entryName, apnName, proxyAddress, proxyPort, mmsc, mmsProxyAddress, mmsProxyPort, user, password, authType, apnTypeBitmask, protocol, roamingProtocol, carrierEnabled, networkTypeBitmask, profileId, modemCognitive, maxConns, waitTime, maxConnsTime, mtu, mvnoType, mvnoMatchData, apnSetId, carrierId, skip464xlat);
    }

    public static int getApnTypesBitmaskFromString(String types) {
        return ApnSetting.getApnTypesBitmaskFromString(types);
    }

    public static int getProtocolIntFromString(String protocol) {
        return ApnSetting.getProtocolIntFromString(protocol);
    }

    public static int getMvnoTypeIntFromString(String mvnoType) {
        return ApnSetting.getMvnoTypeIntFromString(mvnoType);
    }

    @HwSystemApi
    public static String getProtocolStringFromInt(int protocol) {
        return ApnSetting.getProtocolStringFromInt(protocol);
    }

    @HwSystemApi
    public static String getApnTypesStringFromBitmask(int apnTypeBitmask) {
        return ApnSetting.getApnTypesStringFromBitmask(apnTypeBitmask);
    }

    @HwSystemApi
    public void setApnSetting(ApnSetting apnSetting) {
        this.mApnSetting = apnSetting;
    }

    @HwSystemApi
    public int getMtu() {
        ApnSetting apnSetting = this.mApnSetting;
        if (apnSetting == null) {
            return 0;
        }
        return apnSetting.getMtu();
    }
}
