package com.huawei.android.app.admin;

public class DeviceVpnProfile {
    private static final String TAG = "DeviceVpnProfile";
    public static final int TYPE_IPSEC_HYBRID_RSA = 6;
    public static final int TYPE_IPSEC_XAUTH_PSK = 4;
    public static final int TYPE_IPSEC_XAUTH_RSA = 5;
    public static final int TYPE_L2TP = 1;
    public static final int TYPE_L2TP_IPSEC_PSK = 2;
    public static final int TYPE_L2TP_IPSEC_RSA = 3;
    public static final int TYPE_MAX = 6;
    public static final int TYPE_PPTP = 0;
    public String ipsecCaCert = "";
    public String ipsecIdentifier = "";
    public String ipsecSecret = "";
    public String ipsecServerCert = "";
    public String ipsecUserCert = "";
    public final String key;
    public String l2tpSecret = "";
    public boolean mppe = true;
    public String name = "";
    public String password = "";
    public String server = "";
    public int type = 0;
    public String username = "";

    public DeviceVpnProfile(String key2) {
        this.key = key2;
    }

    public DeviceVpnProfile(String key2, String name2, int type2, String server2, String username2, String password2, boolean mppe2, String l2tpSecret2, String ipsecIdentifier2, String ipsecSecret2, String ipsecUserCert2, String ipsecCaCert2, String ipsecServerCert2) {
        this.key = key2;
        setName(name2);
        setType(type2);
        setServer(server2);
        setUsername(username2);
        setPassword(password2);
        setMppe(mppe2);
        setL2tpSecret(l2tpSecret2);
        setIpsecIdentifier(ipsecIdentifier2);
        setIpsecSecret(ipsecSecret2);
        setIpsecUserCert(ipsecUserCert2);
        setIpsecCaCert(ipsecCaCert2);
        setIpsecServerCert(ipsecServerCert2);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type2) {
        this.type = type2;
    }

    public String getServer() {
        return this.server;
    }

    public void setServer(String server2) {
        this.server = server2;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username2) {
        this.username = username2;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password2) {
        this.password = password2;
    }

    public boolean isMppe() {
        return this.mppe;
    }

    public void setMppe(boolean mppe2) {
        this.mppe = mppe2;
    }

    public String getL2tpSecret() {
        return this.l2tpSecret;
    }

    public void setL2tpSecret(String l2tpSecret2) {
        this.l2tpSecret = l2tpSecret2;
    }

    public String getIpsecIdentifier() {
        return this.ipsecIdentifier;
    }

    public void setIpsecIdentifier(String ipsecIdentifier2) {
        this.ipsecIdentifier = ipsecIdentifier2;
    }

    public String getIpsecSecret() {
        return this.ipsecSecret;
    }

    public void setIpsecSecret(String ipsecSecret2) {
        this.ipsecSecret = ipsecSecret2;
    }

    public String getIpsecUserCert() {
        return this.ipsecUserCert;
    }

    public void setIpsecUserCert(String ipsecUserCert2) {
        this.ipsecUserCert = ipsecUserCert2;
    }

    public String getIpsecCaCert() {
        return this.ipsecCaCert;
    }

    public void setIpsecCaCert(String ipsecCaCert2) {
        this.ipsecCaCert = ipsecCaCert2;
    }

    public String getIpsecServerCert() {
        return this.ipsecServerCert;
    }

    public void setIpsecServerCert(String ipsecServerCert2) {
        this.ipsecServerCert = ipsecServerCert2;
    }

    public String getKey() {
        return this.key;
    }
}
