package com.android.internal.net;

import android.annotation.UnsupportedAppUsage;
import android.net.ProxyInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class VpnProfile implements Cloneable, Parcelable {
    public static final Parcelable.Creator<VpnProfile> CREATOR = new Parcelable.Creator<VpnProfile>() {
        /* class com.android.internal.net.VpnProfile.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public VpnProfile createFromParcel(Parcel in) {
            return new VpnProfile(in);
        }

        @Override // android.os.Parcelable.Creator
        public VpnProfile[] newArray(int size) {
            return new VpnProfile[size];
        }
    };
    public static final int PROXY_MANUAL = 1;
    public static final int PROXY_NONE = 0;
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
    @UnsupportedAppUsage
    public final String key;
    public String l2tpSecret;
    public boolean mppe;
    @UnsupportedAppUsage
    public String name;
    public String password;
    public ProxyInfo proxy;
    public String routes;
    @UnsupportedAppUsage
    public boolean saveLogin;
    public String searchDomains;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public String server;
    @UnsupportedAppUsage
    public int type;
    @UnsupportedAppUsage
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
        this.proxy = null;
        this.saveLogin = false;
        this.key = key2;
    }

    @UnsupportedAppUsage
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
        this.proxy = null;
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
        this.proxy = (ProxyInfo) in.readParcelable(null);
    }

    @Override // android.os.Parcelable
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
        out.writeParcelable(this.proxy, flags);
    }

    @UnsupportedAppUsage
    public static VpnProfile decode(String key2, byte[] value) {
        if (key2 == null) {
            return null;
        }
        try {
            String[] values = new String(value, StandardCharsets.UTF_8).split("\u0000", -1);
            if (values.length >= 14) {
                if (values.length <= 19) {
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
                            String pacFileUrl = "";
                            profile.ipsecServerCert = values.length > 14 ? values[14] : pacFileUrl;
                            if (values.length > 15) {
                                String host = values.length > 15 ? values[15] : pacFileUrl;
                                String port = values.length > 16 ? values[16] : pacFileUrl;
                                String exclList = values.length > 17 ? values[17] : pacFileUrl;
                                if (values.length > 18) {
                                    pacFileUrl = values[18];
                                }
                                if (pacFileUrl.isEmpty()) {
                                    profile.proxy = new ProxyInfo(host, port.isEmpty() ? 0 : Integer.parseInt(port), exclList);
                                } else {
                                    profile.proxy = new ProxyInfo(pacFileUrl);
                                }
                            }
                            if (!profile.username.isEmpty() || !profile.password.isEmpty()) {
                                z = true;
                            }
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
        builder.append((char) 0);
        builder.append(this.type);
        builder.append((char) 0);
        builder.append(this.server);
        builder.append((char) 0);
        String str = "";
        builder.append(this.saveLogin ? this.username : str);
        builder.append((char) 0);
        builder.append(this.saveLogin ? this.password : str);
        builder.append((char) 0);
        builder.append(this.dnsServers);
        builder.append((char) 0);
        builder.append(this.searchDomains);
        builder.append((char) 0);
        builder.append(this.routes);
        builder.append((char) 0);
        builder.append(this.mppe);
        builder.append((char) 0);
        builder.append(this.l2tpSecret);
        builder.append((char) 0);
        builder.append(this.ipsecIdentifier);
        builder.append((char) 0);
        builder.append(this.ipsecSecret);
        builder.append((char) 0);
        builder.append(this.ipsecUserCert);
        builder.append((char) 0);
        builder.append(this.ipsecCaCert);
        builder.append((char) 0);
        builder.append(this.ipsecServerCert);
        if (this.proxy != null) {
            builder.append((char) 0);
            builder.append(this.proxy.getHost() != null ? this.proxy.getHost() : str);
            builder.append((char) 0);
            builder.append(this.proxy.getPort());
            builder.append((char) 0);
            if (this.proxy.getExclusionListAsString() != null) {
                str = this.proxy.getExclusionListAsString();
            }
            builder.append(str);
            builder.append((char) 0);
            builder.append(this.proxy.getPacFileUrl().toString());
        }
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
