package com.android.internal.telephony.dataconnection;

import android.telephony.ServiceState;
import android.text.TextUtils;

public class DataProfile {
    static final int TYPE_3GPP = 1;
    static final int TYPE_3GPP2 = 2;
    static final int TYPE_COMMON = 0;
    public final String apn;
    public int authType;
    public final int bearerBitmap;
    public final boolean enabled;
    public final int maxConns;
    public final int maxConnsTime;
    public final boolean modemCognitive;
    public final int mtu;
    public final String mvnoMatchData;
    public final String mvnoType;
    public String password;
    public final int profileId;
    public final String protocol;
    public final String roamingProtocol;
    public final int supportedApnTypesBitmap;
    public final int type;
    public String user;
    public final int waitTime;

    DataProfile(int profileId, String apn, String protocol, int authType, String user, String password, int type, int maxConnsTime, int maxConns, int waitTime, boolean enabled, int supportedApnTypesBitmap, String roamingProtocol, int bearerBitmap, int mtu, String mvnoType, String mvnoMatchData, boolean modemCognitive) {
        this.profileId = profileId;
        this.apn = apn;
        this.protocol = protocol;
        if (authType == -1) {
            if (TextUtils.isEmpty(user)) {
                authType = 0;
            } else {
                authType = 3;
            }
        }
        this.authType = authType;
        this.user = user;
        this.password = password;
        this.type = type;
        this.maxConnsTime = maxConnsTime;
        this.maxConns = maxConns;
        this.waitTime = waitTime;
        this.enabled = enabled;
        this.supportedApnTypesBitmap = supportedApnTypesBitmap;
        this.roamingProtocol = roamingProtocol;
        this.bearerBitmap = bearerBitmap;
        this.mtu = mtu;
        this.mvnoType = mvnoType;
        this.mvnoMatchData = mvnoMatchData;
        this.modemCognitive = modemCognitive;
    }

    public DataProfile(ApnSetting apn) {
        this(apn, apn.profileId);
    }

    public DataProfile(ApnSetting apn, int profileId) {
        String str = apn.apn;
        String str2 = apn.protocol;
        int i = apn.authType;
        String str3 = apn.user;
        String str4 = apn.password;
        int i2 = apn.bearerBitmask == 0 ? 0 : ServiceState.bearerBitmapHasCdma(apn.bearerBitmask) ? 2 : 1;
        this(profileId, str, str2, i, str3, str4, i2, apn.maxConnsTime, apn.maxConns, apn.waitTime, apn.carrierEnabled, apn.typesBitmap, apn.roamingProtocol, apn.bearerBitmask, apn.mtu, apn.mvnoType, apn.mvnoMatchData, apn.modemCognitive);
    }

    public DataProfile(ApnSetting apn, int profileId, String username, String passwd) {
        String str = apn.apn;
        String str2 = apn.protocol;
        int i = apn.authType;
        int i2 = apn.bearerBitmask == 0 ? 0 : ServiceState.bearerBitmapHasCdma(apn.bearerBitmask) ? 2 : 1;
        this(profileId, str, str2, i, username, passwd, i2, apn.maxConnsTime, apn.maxConns, apn.waitTime, apn.carrierEnabled, apn.typesBitmap, apn.roamingProtocol, apn.bearerBitmask, apn.mtu, apn.mvnoType, apn.mvnoMatchData, apn.modemCognitive);
    }

    public DataProfile(ApnSetting apn, String username, String passwd) {
        this(apn, apn.profileId, username, passwd);
    }

    public String toString() {
        return "DataProfile=" + this.profileId + "/" + this.apn + "/" + this.protocol + "/" + this.authType + "/" + this.user + "/" + this.type + "/" + this.maxConnsTime + "/" + this.maxConns + "/" + this.waitTime + "/" + this.enabled + "/" + this.supportedApnTypesBitmap + "/" + this.roamingProtocol + "/" + this.bearerBitmap + "/" + this.mtu + "/" + this.mvnoType + "/" + this.mvnoMatchData + "/" + this.modemCognitive;
    }

    public boolean equals(Object o) {
        if (!(o instanceof DataProfile)) {
            return false;
        }
        return o != this ? toString().equals(o.toString()) : true;
    }
}
