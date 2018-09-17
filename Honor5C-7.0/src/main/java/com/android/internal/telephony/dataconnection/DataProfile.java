package com.android.internal.telephony.dataconnection;

import android.os.Parcel;
import android.telephony.ServiceState;

public class DataProfile {
    static final int TYPE_3GPP = 1;
    static final int TYPE_3GPP2 = 2;
    static final int TYPE_COMMON = 0;
    public final String apn;
    public final int authType;
    public final boolean enabled;
    public final int maxConns;
    public final int maxConnsTime;
    public final String password;
    public final int profileId;
    public final String protocol;
    public final int type;
    public final String user;
    public final int waitTime;

    DataProfile(int profileId, String apn, String protocol, int authType, String user, String password, int type, int maxConnsTime, int maxConns, int waitTime, boolean enabled) {
        this.profileId = profileId;
        this.apn = apn;
        this.protocol = protocol;
        this.authType = authType;
        this.user = user;
        this.password = password;
        this.type = type;
        this.maxConnsTime = maxConnsTime;
        this.maxConns = maxConns;
        this.waitTime = waitTime;
        this.enabled = enabled;
    }

    public DataProfile(ApnSetting apn, boolean isRoaming) {
        int i = 0;
        int i2 = apn.profileId;
        String str = apn.apn;
        String str2 = isRoaming ? apn.roamingProtocol : apn.protocol;
        int i3 = apn.authType;
        String str3 = apn.user;
        String str4 = apn.password;
        if (apn.bearerBitmask != 0) {
            i = ServiceState.bearerBitmapHasCdma(apn.bearerBitmask) ? TYPE_3GPP2 : TYPE_3GPP;
        }
        this(i2, str, str2, i3, str3, str4, i, apn.maxConnsTime, apn.maxConns, apn.waitTime, apn.carrierEnabled);
    }

    public static Parcel toParcel(Parcel pc, DataProfile[] dps) {
        if (pc == null) {
            return null;
        }
        pc.writeInt(dps.length);
        for (int i = 0; i < dps.length; i += TYPE_3GPP) {
            int i2;
            pc.writeInt(dps[i].profileId);
            pc.writeString(dps[i].apn);
            pc.writeString(dps[i].protocol);
            pc.writeInt(dps[i].authType);
            pc.writeString(dps[i].user);
            pc.writeString(dps[i].password);
            pc.writeInt(dps[i].type);
            pc.writeInt(dps[i].maxConnsTime);
            pc.writeInt(dps[i].maxConns);
            pc.writeInt(dps[i].waitTime);
            if (dps[i].enabled) {
                i2 = TYPE_3GPP;
            } else {
                i2 = 0;
            }
            pc.writeInt(i2);
        }
        return pc;
    }

    public String toString() {
        return "DataProfile " + this.profileId + "/" + this.apn + "/" + this.protocol + "/" + this.authType + "/" + this.user + "/" + this.password + "/" + this.type + "/" + this.maxConnsTime + "/" + this.maxConns + "/" + this.waitTime + "/" + this.enabled;
    }

    public boolean equals(Object o) {
        if (o instanceof DataProfile) {
            return toString().equals(o.toString());
        }
        return false;
    }
}
