package com.android.internal.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class VpnProfile implements Cloneable, Parcelable {
    public static final Parcelable.Creator<VpnProfile> CREATOR = new Parcelable.Creator<VpnProfile>() {
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
    public String dnsServers;
    public String ipsecCaCert;
    public String ipsecIdentifier;
    public String ipsecSecret;
    public String ipsecServerCert;
    public String ipsecUserCert;
    public final String key;
    public String l2tpSecret;
    public boolean mppe;
    public String name;
    public String password;
    public String routes;
    public boolean saveLogin;
    public String searchDomains;
    public String server;
    public int type;
    public String username;

    public VpnProfile(String key2) {
        this.name = "";
        this.type = 0;
        this.server = "";
        this.username = "";
        this.password = "";
        this.dnsServers = "";
        this.searchDomains = "";
        this.routes = "";
        this.mppe = true;
        this.l2tpSecret = "";
        this.ipsecIdentifier = "";
        this.ipsecSecret = "";
        this.ipsecUserCert = "";
        this.ipsecCaCert = "";
        this.ipsecServerCert = "";
        this.saveLogin = false;
        this.key = key2;
    }

    public VpnProfile(Parcel in) {
        this.name = "";
        boolean z = false;
        this.type = 0;
        this.server = "";
        this.username = "";
        this.password = "";
        this.dnsServers = "";
        this.searchDomains = "";
        this.routes = "";
        this.mppe = true;
        this.l2tpSecret = "";
        this.ipsecIdentifier = "";
        this.ipsecSecret = "";
        this.ipsecUserCert = "";
        this.ipsecCaCert = "";
        this.ipsecServerCert = "";
        this.saveLogin = false;
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
        this.saveLogin = in.readInt() != 0 ? true : z;
    }

    public void writeToParcel(Parcel out, int flags) {
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
        out.writeInt(this.saveLogin ? 1 : 0);
    }

    public static VpnProfile decode(String key2, byte[] value) {
        if (key2 == null) {
            return null;
        }
        try {
            String[] values = new String(value, StandardCharsets.UTF_8).split("\u0000", -1);
            if (values.length >= 14) {
                if (values.length <= 15) {
                    VpnProfile profile = new VpnProfile(key2);
                    boolean z = false;
                    profile.name = values[0];
                    profile.type = Integer.parseInt(values[1]);
                    if (profile.type >= 0) {
                        if (profile.type <= 6) {
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
                            profile.ipsecServerCert = values.length > 14 ? values[14] : "";
                            if (profile.username.isEmpty()) {
                                if (profile.password.isEmpty()) {
                                    profile.saveLogin = z;
                                    return profile;
                                }
                            }
                            z = true;
                            profile.saveLogin = z;
                            return profile;
                        }
                    }
                    return null;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public byte[] encode() {
        StringBuilder builder = new StringBuilder(this.name);
        builder.append(0);
        builder.append(this.type);
        builder.append(0);
        builder.append(this.server);
        builder.append(0);
        builder.append(this.saveLogin ? this.username : "");
        builder.append(0);
        builder.append(this.saveLogin ? this.password : "");
        builder.append(0);
        builder.append(this.dnsServers);
        builder.append(0);
        builder.append(this.searchDomains);
        builder.append(0);
        builder.append(this.routes);
        builder.append(0);
        builder.append(this.mppe);
        builder.append(0);
        builder.append(this.l2tpSecret);
        builder.append(0);
        builder.append(this.ipsecIdentifier);
        builder.append(0);
        builder.append(this.ipsecSecret);
        builder.append(0);
        builder.append(this.ipsecUserCert);
        builder.append(0);
        builder.append(this.ipsecCaCert);
        builder.append(0);
        builder.append(this.ipsecServerCert);
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    public boolean isValidLockdownProfile() {
        return isTypeValidForLockdown() && isServerAddressNumeric() && hasDns() && areDnsAddressesNumeric();
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
        return !TextUtils.isEmpty(this.dnsServers);
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
