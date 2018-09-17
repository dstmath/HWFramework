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

    public DeviceVpnProfile(String key) {
        this.key = key;
    }

    public DeviceVpnProfile(String key, String name, int type, String server, String username, String password, boolean mppe, String l2tpSecret, String ipsecIdentifier, String ipsecSecret, String ipsecUserCert, String ipsecCaCert, String ipsecServerCert) {
        this.key = key;
        setName(name);
        setType(type);
        setServer(server);
        setUsername(username);
        setPassword(password);
        setMppe(mppe);
        setL2tpSecret(l2tpSecret);
        setIpsecIdentifier(ipsecIdentifier);
        setIpsecSecret(ipsecSecret);
        setIpsecUserCert(ipsecUserCert);
        setIpsecCaCert(ipsecCaCert);
        setIpsecServerCert(ipsecServerCert);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getServer() {
        return this.server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isMppe() {
        return this.mppe;
    }

    public void setMppe(boolean mppe) {
        this.mppe = mppe;
    }

    public String getL2tpSecret() {
        return this.l2tpSecret;
    }

    public void setL2tpSecret(String l2tpSecret) {
        this.l2tpSecret = l2tpSecret;
    }

    public String getIpsecIdentifier() {
        return this.ipsecIdentifier;
    }

    public void setIpsecIdentifier(String ipsecIdentifier) {
        this.ipsecIdentifier = ipsecIdentifier;
    }

    public String getIpsecSecret() {
        return this.ipsecSecret;
    }

    public void setIpsecSecret(String ipsecSecret) {
        this.ipsecSecret = ipsecSecret;
    }

    public String getIpsecUserCert() {
        return this.ipsecUserCert;
    }

    public void setIpsecUserCert(String ipsecUserCert) {
        this.ipsecUserCert = ipsecUserCert;
    }

    public String getIpsecCaCert() {
        return this.ipsecCaCert;
    }

    public void setIpsecCaCert(String ipsecCaCert) {
        this.ipsecCaCert = ipsecCaCert;
    }

    public String getIpsecServerCert() {
        return this.ipsecServerCert;
    }

    public void setIpsecServerCert(String ipsecServerCert) {
        this.ipsecServerCert = ipsecServerCert;
    }

    public String getKey() {
        return this.key;
    }
}
