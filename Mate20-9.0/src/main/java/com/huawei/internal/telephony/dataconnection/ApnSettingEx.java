package com.huawei.internal.telephony.dataconnection;

import com.android.internal.telephony.dataconnection.ApnSetting;

public class ApnSettingEx {
    public final String apn;
    public final int authType;
    public final int bearer;
    public final int bearerBitmask;
    public final String carrier;
    public final boolean carrierEnabled;
    public final int id;
    ApnSetting mApnSetting = null;
    public final int maxConns;
    public final int maxConnsTime;
    public final String mmsPort;
    public final String mmsProxy;
    public final String mmsc;
    public final boolean modemCognitive;
    public final int mtu;
    public final String mvnoMatchData;
    public final String mvnoType;
    public final String numeric;
    public final String password;
    public final String port;
    public final int profileId;
    public final String protocol;
    public final String proxy;
    public final String roamingProtocol;
    public String[] types;
    public final String user;
    public final int waitTime;

    public ApnSettingEx(int id2, String numeric2, String carrier2, String apn2, String proxy2, String port2, String mmsc2, String mmsProxy2, String mmsPort2, String user2, String password2, int authType2, String[] types2, String protocol2, String roamingProtocol2, boolean carrierEnabled2, int bearer2, int bearerBitmask2, int profileId2, boolean modemCognitive2, int maxConns2, int waitTime2, int maxConnsTime2, int mtu2, String mvnoType2, String mvnoMatchData2) {
        ApnSetting apnSetting = new ApnSetting(id2, numeric2, carrier2, apn2, proxy2, port2, mmsc2, mmsProxy2, mmsPort2, user2, password2, authType2, types2, protocol2, roamingProtocol2, carrierEnabled2, bearer2, bearerBitmask2, profileId2, modemCognitive2, maxConns2, waitTime2, maxConnsTime2, mtu2, mvnoType2, mvnoMatchData2);
        this.mApnSetting = apnSetting;
        this.id = this.mApnSetting.id;
        this.numeric = this.mApnSetting.numeric;
        this.carrier = this.mApnSetting.carrier;
        this.apn = this.mApnSetting.apn;
        this.proxy = this.mApnSetting.proxy;
        this.port = this.mApnSetting.port;
        this.mmsc = this.mApnSetting.mmsc;
        this.mmsProxy = this.mApnSetting.mmsProxy;
        this.mmsPort = this.mApnSetting.mmsPort;
        this.user = this.mApnSetting.user;
        this.password = this.mApnSetting.password;
        this.authType = this.mApnSetting.authType;
        this.types = this.mApnSetting.types;
        this.protocol = this.mApnSetting.protocol;
        this.roamingProtocol = this.mApnSetting.roamingProtocol;
        this.carrierEnabled = this.mApnSetting.carrierEnabled;
        this.bearer = this.mApnSetting.bearer;
        this.bearerBitmask = this.mApnSetting.bearerBitmask;
        this.profileId = this.mApnSetting.profileId;
        this.modemCognitive = this.mApnSetting.modemCognitive;
        this.maxConns = this.mApnSetting.maxConns;
        this.waitTime = this.mApnSetting.waitTime;
        this.maxConnsTime = this.mApnSetting.maxConnsTime;
        this.mtu = this.mApnSetting.mtu;
        this.mvnoType = this.mApnSetting.mvnoType;
        this.mvnoMatchData = this.mApnSetting.mvnoMatchData;
    }

    public String toString() {
        return this.mApnSetting.toString();
    }
}
