package com.android.internal.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.LogException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class VpnProfile implements Cloneable, Parcelable {
    public static final Creator<VpnProfile> CREATOR = new Creator<VpnProfile>() {
        public VpnProfile createFromParcel(Parcel in) {
            return new VpnProfile(in);
        }

        public VpnProfile[] newArray(int size) {
            return new VpnProfile[size];
        }
    };
    private static final String TAG = "VpnProfile";
    public static final int TYPE_IPSEC_HYBRID_RSA = 6;
    public static final int TYPE_IPSEC_XAUTH_PSK = 4;
    public static final int TYPE_IPSEC_XAUTH_RSA = 5;
    public static final int TYPE_L2TP = 1;
    public static final int TYPE_L2TP_IPSEC_PSK = 2;
    public static final int TYPE_L2TP_IPSEC_RSA = 3;
    public static final int TYPE_MAX = 6;
    public static final int TYPE_PPTP = 0;
    public String dnsServers = LogException.NO_VALUE;
    public String ipsecCaCert = LogException.NO_VALUE;
    public String ipsecIdentifier = LogException.NO_VALUE;
    public String ipsecSecret = LogException.NO_VALUE;
    public String ipsecServerCert = LogException.NO_VALUE;
    public String ipsecUserCert = LogException.NO_VALUE;
    public final String key;
    public String l2tpSecret = LogException.NO_VALUE;
    public boolean mppe = true;
    public String name = LogException.NO_VALUE;
    public String password = LogException.NO_VALUE;
    public String routes = LogException.NO_VALUE;
    public boolean saveLogin = false;
    public String searchDomains = LogException.NO_VALUE;
    public String server = LogException.NO_VALUE;
    public int type = 0;
    public String username = LogException.NO_VALUE;

    public VpnProfile(String key) {
        this.key = key;
    }

    public VpnProfile(Parcel in) {
        boolean z = true;
        this.key = in.readString();
        this.name = in.readString();
        this.type = in.readInt();
        this.server = in.readString();
        this.username = in.readString();
        this.password = in.readString();
        this.dnsServers = in.readString();
        this.searchDomains = in.readString();
        this.routes = in.readString();
        this.mppe = in.readInt() != 0;
        this.l2tpSecret = in.readString();
        this.ipsecIdentifier = in.readString();
        this.ipsecSecret = in.readString();
        this.ipsecUserCert = in.readString();
        this.ipsecCaCert = in.readString();
        this.ipsecServerCert = in.readString();
        if (in.readInt() == 0) {
            z = false;
        }
        this.saveLogin = z;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i = 1;
        out.writeString(this.key);
        out.writeString(this.name);
        out.writeInt(this.type);
        out.writeString(this.server);
        out.writeString(this.username);
        out.writeString(this.password);
        out.writeString(this.dnsServers);
        out.writeString(this.searchDomains);
        out.writeString(this.routes);
        out.writeInt(this.mppe ? 1 : 0);
        out.writeString(this.l2tpSecret);
        out.writeString(this.ipsecIdentifier);
        out.writeString(this.ipsecSecret);
        out.writeString(this.ipsecUserCert);
        out.writeString(this.ipsecCaCert);
        out.writeString(this.ipsecServerCert);
        if (!this.saveLogin) {
            i = 0;
        }
        out.writeInt(i);
    }

    public static VpnProfile decode(String key, byte[] value) {
        boolean z = true;
        if (key == null) {
            return null;
        }
        try {
            String[] values = new String(value, StandardCharsets.UTF_8).split("\u0000", -1);
            if (values.length < 14 || values.length > 15) {
                return null;
            }
            VpnProfile profile = new VpnProfile(key);
            profile.name = values[0];
            profile.type = Integer.parseInt(values[1]);
            if (profile.type < 0 || profile.type > 6) {
                return null;
            }
            profile.server = values[2];
            profile.username = values[3];
            profile.password = values[4];
            profile.dnsServers = values[5];
            profile.searchDomains = values[6];
            profile.routes = values[7];
            profile.mppe = Boolean.parseBoolean(values[8]);
            profile.l2tpSecret = values[9];
            profile.ipsecIdentifier = values[10];
            profile.ipsecSecret = values[11];
            profile.ipsecUserCert = values[12];
            profile.ipsecCaCert = values[13];
            profile.ipsecServerCert = values.length > 14 ? values[14] : LogException.NO_VALUE;
            if (profile.username.isEmpty()) {
                z = profile.password.isEmpty() ^ 1;
            }
            profile.saveLogin = z;
            return profile;
        } catch (Exception e) {
            return null;
        }
    }

    public byte[] encode() {
        StringBuilder builder = new StringBuilder(this.name);
        builder.append(0).append(this.type);
        builder.append(0).append(this.server);
        builder.append(0).append(this.saveLogin ? this.username : LogException.NO_VALUE);
        builder.append(0).append(this.saveLogin ? this.password : LogException.NO_VALUE);
        builder.append(0).append(this.dnsServers);
        builder.append(0).append(this.searchDomains);
        builder.append(0).append(this.routes);
        builder.append(0).append(this.mppe);
        builder.append(0).append(this.l2tpSecret);
        builder.append(0).append(this.ipsecIdentifier);
        builder.append(0).append(this.ipsecSecret);
        builder.append(0).append(this.ipsecUserCert);
        builder.append(0).append(this.ipsecCaCert);
        builder.append(0).append(this.ipsecServerCert);
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    public boolean isValidLockdownProfile() {
        if (isTypeValidForLockdown() && isServerAddressNumeric() && hasDns()) {
            return areDnsAddressesNumeric();
        }
        return false;
    }

    public boolean isTypeValidForLockdown() {
        return this.type != 0;
    }

    public boolean isServerAddressNumeric() {
        try {
            InetAddress.parseNumericAddress(this.server);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean hasDns() {
        return TextUtils.isEmpty(this.dnsServers) ^ 1;
    }

    public boolean areDnsAddressesNumeric() {
        try {
            for (String dnsServer : this.dnsServers.split(" +")) {
                InetAddress.parseNumericAddress(dnsServer);
            }
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public int describeContents() {
        return 0;
    }
}
