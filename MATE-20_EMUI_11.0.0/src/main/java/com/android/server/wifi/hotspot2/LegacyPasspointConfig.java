package com.android.server.wifi.hotspot2;

import android.text.TextUtils;
import java.util.Arrays;
import java.util.Objects;

public class LegacyPasspointConfig {
    public String mFqdn;
    public String mFriendlyName;
    public String mImsi;
    public String mRealm;
    public long[] mRoamingConsortiumOis;

    public boolean equals(Object thatObject) {
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof LegacyPasspointConfig)) {
            return false;
        }
        LegacyPasspointConfig that = (LegacyPasspointConfig) thatObject;
        if (!TextUtils.equals(this.mFqdn, that.mFqdn) || !TextUtils.equals(this.mFriendlyName, that.mFriendlyName) || !Arrays.equals(this.mRoamingConsortiumOis, that.mRoamingConsortiumOis) || !TextUtils.equals(this.mRealm, that.mRealm) || !TextUtils.equals(this.mImsi, that.mImsi)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.mFqdn, this.mFriendlyName, this.mRoamingConsortiumOis, this.mRealm, this.mImsi);
    }
}
