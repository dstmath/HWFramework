package ohos.net;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class VpnProfile implements Sequenceable {
    private static final String DEFAULT_EMPTY_STRING = "";
    public static final int PROXY_MANUAL = 1;
    public static final int PROXY_NONE = 0;
    private static final String TAG = "VpnProfile";
    public static final int VPN_TYPE_IPSEC_HYBRID_RSA = 6;
    public static final int VPN_TYPE_IPSEC_XAUTH_PSK = 4;
    public static final int VPN_TYPE_IPSEC_XAUTH_RSA = 5;
    public static final int VPN_TYPE_L2TP = 1;
    public static final int VPN_TYPE_L2TP_IPSEC_PSK = 2;
    public static final int VPN_TYPE_L2TP_IPSEC_RSA = 3;
    public static final int VPN_TYPE_PPTP = 0;
    public String dnsServers = "";
    public HttpProxy httpProxy = null;
    public String ipsecCaCert = "";
    public String ipsecIdentifier = "";
    public String ipsecSecret = "";
    public String ipsecServerCert = "";
    public String ipsecUserCert = "";
    public boolean isMppe = true;
    public boolean isSaveLogin = false;
    public final String key;
    public String l2tpSecret = "";
    public String name = "";
    public String password = "";
    public String routes = "";
    public String searchDomains = "";
    public String server = "";
    public int type = 0;
    public String username = "";

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        return true;
    }

    public VpnProfile(String str) {
        this.key = str;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeString(this.key);
        parcel.writeString(this.name);
        parcel.writeInt(this.type);
        parcel.writeString(this.server);
        parcel.writeString(this.username);
        parcel.writeString(this.password);
        parcel.writeString(this.dnsServers);
        parcel.writeString(this.searchDomains);
        parcel.writeString(this.routes);
        parcel.writeInt(this.isMppe ? 1 : 0);
        parcel.writeString(this.l2tpSecret);
        parcel.writeString(this.ipsecIdentifier);
        parcel.writeString(this.ipsecSecret);
        parcel.writeString(this.ipsecUserCert);
        parcel.writeString(this.ipsecCaCert);
        parcel.writeString(this.ipsecServerCert);
        parcel.writeInt(this.isSaveLogin ? 1 : 0);
        if (this.httpProxy == null) {
            parcel.writeString(null);
            return true;
        }
        parcel.writeString("com.android.internal.net.ProxyInfo");
        this.httpProxy.marshalling(parcel);
        return true;
    }
}
