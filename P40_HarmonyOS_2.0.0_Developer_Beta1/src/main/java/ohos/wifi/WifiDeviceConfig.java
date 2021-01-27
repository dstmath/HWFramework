package ohos.wifi;

import java.util.Arrays;
import ohos.annotation.SystemApi;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.net.HttpProxy;
import ohos.utils.Parcel;
import ohos.utils.ParcelException;
import ohos.utils.Sequenceable;

public final class WifiDeviceConfig implements Sequenceable {
    private static final byte[] DEFAULT_MAC_ADDR = {2, 0, 0, 0, 0, 0};
    private static final HiLogLabel LABEL = new HiLogLabel(3, InnerUtils.LOG_ID_WIFI, "WifiDeviceConfig");
    private static final int MAC_ADDR_LEN = 6;
    private String bssid;
    private String caller;
    private int creatorUid;
    private int disableReason;
    private WifiEapConfig eapConfig;
    private HttpProxy httpProxy;
    private IpType ipType;
    private boolean isDataRestricted;
    private boolean isHiddenSsid;
    private int netId;
    private String preSharedKey;
    private ProxyType proxyType;
    private byte[] randomMacAddr;
    private int randomMacType;
    private int securityType;
    private String ssid;
    private IpConfig staticIp;
    private String wapiAsCert;
    private int wapiPskType;
    private String wapiUserCert;

    @SystemApi
    public enum IpType {
        STATIC,
        DHCP,
        UNKNOWN
    }

    @SystemApi
    public enum ProxyType {
        NONE,
        STATIC,
        UNKNOWN,
        PAC
    }

    public WifiDeviceConfig(String str, String str2, String str3, boolean z, int i, int i2) {
        this.disableReason = -1;
        this.netId = -1;
        this.isDataRestricted = false;
        this.ipType = IpType.UNKNOWN;
        this.proxyType = ProxyType.UNKNOWN;
        this.wapiPskType = -1;
        this.ssid = str;
        this.bssid = str2;
        this.preSharedKey = str3;
        this.isHiddenSsid = z;
        this.securityType = i;
        this.creatorUid = i2;
        this.disableReason = 0;
        this.netId = -1;
        this.caller = null;
        this.eapConfig = new WifiEapConfig();
        this.isDataRestricted = false;
        this.ipType = IpType.UNKNOWN;
        this.proxyType = ProxyType.UNKNOWN;
        this.httpProxy = null;
        this.staticIp = null;
        this.randomMacType = 1;
        byte[] bArr = DEFAULT_MAC_ADDR;
        this.randomMacAddr = Arrays.copyOf(bArr, bArr.length);
        this.wapiPskType = -1;
        this.wapiAsCert = null;
        this.wapiUserCert = null;
    }

    public WifiDeviceConfig() {
        this(null, null, null, false, -1, -1);
    }

    private <T extends Enum<T>> T convertEnum(Class<T> cls, String str, T t) {
        if (str != null && !str.isEmpty()) {
            try {
                return (T) Enum.valueOf(cls, str);
            } catch (IllegalArgumentException unused) {
            }
        }
        return t;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v2 */
    /* JADX WARN: Type inference failed for: r0v3 */
    /* JADX WARN: Type inference failed for: r0v4 */
    /* JADX WARN: Type inference failed for: r0v5 */
    /* JADX WARN: Type inference failed for: r0v6 */
    /* JADX WARN: Type inference failed for: r0v7 */
    /* JADX WARN: Type inference failed for: r0v8 */
    /* JADX WARN: Type inference failed for: r0v9 */
    /* JADX WARN: Type inference failed for: r0v10 */
    /* JADX WARN: Type inference failed for: r0v11 */
    /* JADX WARN: Type inference failed for: r0v12 */
    /* JADX WARN: Type inference failed for: r0v13 */
    /* JADX WARN: Type inference failed for: r0v14 */
    /* JADX WARN: Type inference failed for: r0v15 */
    /* JADX WARN: Type inference failed for: r0v16 */
    /* JADX WARN: Type inference failed for: r0v17 */
    /* JADX WARN: Type inference failed for: r4v1 */
    /* JADX WARN: Type inference failed for: r4v2 */
    /* JADX WARN: Type inference failed for: r4v5 */
    /* JADX WARN: Type inference failed for: r0v18 */
    /* JADX WARN: Type inference failed for: r0v21 */
    /* JADX WARN: Type inference failed for: r0v22 */
    /* JADX WARN: Type inference failed for: r0v25 */
    /* JADX WARN: Type inference failed for: r0v26 */
    /* JADX WARN: Type inference failed for: r0v29 */
    /* JADX WARN: Type inference failed for: r0v30 */
    /* JADX WARN: Type inference failed for: r0v34 */
    /* JADX WARN: Type inference failed for: r0v35 */
    /* JADX WARN: Type inference failed for: r0v39 */
    /* JADX WARN: Type inference failed for: r0v40 */
    /* JADX WARN: Type inference failed for: r0v43 */
    /* JADX WARN: Type inference failed for: r0v44 */
    /* JADX WARN: Type inference failed for: r0v48 */
    /* JADX WARN: Type inference failed for: r0v49 */
    /* JADX WARN: Type inference failed for: r0v52 */
    /* JADX WARN: Type inference failed for: r0v53 */
    /* JADX WARN: Type inference failed for: r0v56 */
    /* JADX WARN: Type inference failed for: r0v57 */
    /* JADX WARN: Type inference failed for: r0v60 */
    /* JADX WARN: Type inference failed for: r0v61 */
    /* JADX WARN: Type inference failed for: r0v64 */
    /* JADX WARN: Type inference failed for: r0v65 */
    /* JADX WARN: Type inference failed for: r0v68 */
    /* JADX WARN: Type inference failed for: r0v69 */
    /* JADX WARN: Type inference failed for: r0v72 */
    /* JADX WARN: Type inference failed for: r0v73 */
    /* JADX WARN: Type inference failed for: r0v76 */
    /* JADX WARN: Type inference failed for: r0v77 */
    /* JADX WARN: Type inference failed for: r0v80 */
    /* JADX WARN: Type inference failed for: r0v81 */
    /* JADX WARN: Type inference failed for: r0v84 */
    /* JADX WARNING: Unknown variable types count: 17 */
    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        ?? r0 = (((((((((((parcel.writeString(this.ssid) && parcel.writeString(this.bssid)) != false && parcel.writeString(this.preSharedKey)) != false && parcel.writeInt(this.isHiddenSsid ? 1 : 0)) != false && parcel.writeInt(this.securityType)) != false && parcel.writeInt(this.creatorUid)) != false && parcel.writeInt(this.disableReason)) != false && parcel.writeInt(this.netId)) != false && parcel.writeInt(this.isDataRestricted ? 1 : 0)) != false && parcel.writeInt(this.randomMacType)) != false && parcel.writeInt(this.randomMacAddr.length)) != false && parcel.writeByteArray(this.randomMacAddr)) == true && parcel.writeString(this.ipType.name());
        parcel.writeSequenceable(this.staticIp);
        ?? r02 = r0 == true && parcel.writeString(this.proxyType.name());
        parcel.writeSequenceable(this.httpProxy);
        parcel.writeSequenceable(this.eapConfig);
        if (((((r02 != false && parcel.writeString(this.caller)) != false && parcel.writeInt(this.wapiPskType)) != false && parcel.writeString(this.wapiAsCert)) == true && parcel.writeString(this.wapiUserCert)) != false) {
            return true;
        }
        parcel.reclaim();
        return false;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.ssid = parcel.readString();
        this.bssid = parcel.readString();
        this.preSharedKey = parcel.readString();
        this.isHiddenSsid = parcel.readInt() == 1;
        this.securityType = parcel.readInt();
        this.creatorUid = parcel.readInt();
        this.disableReason = parcel.readInt();
        this.netId = parcel.readInt();
        this.isDataRestricted = parcel.readInt() != 0;
        this.randomMacType = parcel.readInt();
        int readInt = parcel.readInt();
        byte[] bArr = DEFAULT_MAC_ADDR;
        this.randomMacAddr = Arrays.copyOf(bArr, bArr.length);
        if (readInt == 6) {
            try {
                parcel.readByteArray(this.randomMacAddr);
            } catch (ParcelException unused) {
                HiLog.warn(LABEL, "Read randomMacAddr error", new Object[0]);
            }
        }
        this.ipType = (IpType) convertEnum(IpType.class, parcel.readString(), IpType.DHCP);
        this.staticIp = new IpConfig();
        if (!parcel.readSequenceable(this.staticIp)) {
            this.staticIp = null;
        }
        this.proxyType = (ProxyType) convertEnum(ProxyType.class, parcel.readString(), ProxyType.NONE);
        this.httpProxy = new HttpProxy();
        if (!parcel.readSequenceable(this.httpProxy)) {
            this.httpProxy = null;
        }
        this.eapConfig = new WifiEapConfig();
        if (!parcel.readSequenceable(this.eapConfig)) {
            this.eapConfig = null;
        }
        this.caller = parcel.readString();
        this.wapiPskType = parcel.readInt();
        this.wapiAsCert = parcel.readString();
        this.wapiUserCert = parcel.readString();
        return true;
    }

    public String getSsid() {
        return this.ssid;
    }

    public void setSsid(String str) {
        this.ssid = str;
    }

    public String getBssid() {
        return this.bssid;
    }

    @SystemApi
    public void setBssid(String str) {
        this.bssid = str;
    }

    public String getPreSharedKey() {
        return this.preSharedKey;
    }

    public void setPreSharedKey(String str) {
        this.preSharedKey = str;
    }

    public boolean isHiddenSsid() {
        return this.isHiddenSsid;
    }

    public void setHiddenSsid(boolean z) {
        this.isHiddenSsid = z;
    }

    public int getSecurityType() {
        return this.securityType;
    }

    public void setSecurityType(int i) {
        this.securityType = i;
    }

    @SystemApi
    public int getCreatorUid() {
        return this.creatorUid;
    }

    @SystemApi
    public void setCreatorUid(int i) {
        this.creatorUid = i;
    }

    @SystemApi
    public int getDisableReason() {
        return this.disableReason;
    }

    @SystemApi
    public int getNetId() {
        return this.netId;
    }

    public boolean isRestricted() {
        return this.isDataRestricted;
    }

    public void setRestricted(boolean z) {
        this.isDataRestricted = z;
    }

    @SystemApi
    public IpType getIpType() {
        return this.ipType;
    }

    @SystemApi
    public void setIpType(IpType ipType2) {
        this.ipType = ipType2;
    }

    @SystemApi
    public ProxyType getProxyType() {
        return this.proxyType;
    }

    @SystemApi
    public void setProxyType(ProxyType proxyType2) {
        this.proxyType = proxyType2;
    }

    @SystemApi
    public HttpProxy getHttpProxy() {
        return this.httpProxy;
    }

    @SystemApi
    public void setHttpProxy(HttpProxy httpProxy2) {
        this.httpProxy = httpProxy2;
    }

    public WifiEapConfig getEapConfig() {
        return this.eapConfig;
    }

    public void setEapConfig(WifiEapConfig wifiEapConfig) {
        this.eapConfig = wifiEapConfig;
    }

    @SystemApi
    public int getWapiPskType() {
        return this.wapiPskType;
    }

    @SystemApi
    public void setWapiPskType(int i) {
        this.wapiPskType = i;
    }

    @SystemApi
    public String getWapiAsCert() {
        return this.wapiAsCert;
    }

    @SystemApi
    public void setWapiAsCert(String str) {
        this.wapiAsCert = str;
    }

    @SystemApi
    public String getWapiUserCert() {
        return this.wapiUserCert;
    }

    @SystemApi
    public void setWapiUserCert(String str) {
        this.wapiUserCert = str;
    }
}
