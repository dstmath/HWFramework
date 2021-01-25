package android.net.wifi;

import android.content.Context;
import android.net.IpConfiguration;
import android.net.MacAddress;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;
import android.net.wifi.hwUtil.SafeDisplayUtil;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

public class WifiDeviceConfig implements Parcelable {
    public static final Parcelable.Creator<WifiDeviceConfig> CREATOR = new Parcelable.Creator<WifiDeviceConfig>() {
        /* class android.net.wifi.WifiDeviceConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WifiDeviceConfig createFromParcel(Parcel in) {
            WifiDeviceConfig config = new WifiDeviceConfig();
            config.ssid = in.readString();
            config.bssid = in.readString();
            config.preSharedKey = in.readString();
            boolean z = true;
            config.isHiddenSsid = in.readInt() != 0;
            config.securityType = in.readInt();
            config.creatorUid = in.readInt();
            config.disableReason = in.readInt();
            config.netId = in.readInt();
            if (in.readInt() == 0) {
                z = false;
            }
            config.isDataRestricted = z;
            config.randomMacType = in.readInt();
            if (in.readInt() == 6) {
                config.randomMacAddr = new byte[6];
                in.readByteArray(config.randomMacAddr);
            }
            config.ipType = (IpType) WifiDeviceConfig.convertEnum(IpType.class, in.readString(), IpType.DHCP);
            config.staticIp = (StaticIpConfiguration) WifiDeviceConfig.readParcelable(StaticIpConfiguration.CREATOR, in);
            config.proxyType = (ProxyType) WifiDeviceConfig.convertEnum(ProxyType.class, in.readString(), ProxyType.NONE);
            config.httpProxy = (ProxyInfo) WifiDeviceConfig.readParcelable(ProxyInfo.CREATOR, in);
            config.eapConfig = (WifiEapConfig) WifiDeviceConfig.readParcelable(WifiEapConfig.CREATOR, in);
            config.callingPackage = in.readString();
            config.wapiPskType = in.readInt();
            config.wapiAsCert = in.readString();
            config.wapiUserCert = in.readString();
            return config;
        }

        @Override // android.os.Parcelable.Creator
        public WifiDeviceConfig[] newArray(int size) {
            return new WifiDeviceConfig[size];
        }
    };
    private static final int HISI_WAPI = 0;
    private static final int INVALID_WAPI = -1;
    private static final String LINE = System.lineSeparator();
    private static final int MAC_ADDR_LEN = 6;
    private static final int QUALCOMM_WAPI = 1;
    private static final String TAG = "WifiDeviceConfig";
    private static final String WAPI_TYPE = "wapi_type";
    private static final boolean WIFI_FAST_BSS_TRANSITION_ENABLED = SystemProperties.getBoolean("ro.config.wifi_fast_bss_enable", false);
    private String bssid;
    private String callingPackage;
    private int creatorUid;
    private int disableReason;
    private WifiEapConfig eapConfig;
    private ProxyInfo httpProxy;
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
    private StaticIpConfiguration staticIp;
    private String wapiAsCert;
    private int wapiPskType;
    private String wapiUserCert;

    /* access modifiers changed from: private */
    public enum IpType {
        STATIC,
        DHCP,
        UNKNOWN
    }

    /* access modifiers changed from: private */
    public enum ProxyType {
        NONE,
        STATIC,
        UNKNOWN,
        PAC
    }

    public WifiDeviceConfig(WifiDeviceConfig source) {
        this.ipType = IpType.UNKNOWN;
        this.proxyType = ProxyType.UNKNOWN;
        if (source != null) {
            this.ssid = source.ssid;
            this.bssid = source.bssid;
            this.preSharedKey = source.preSharedKey;
            this.isHiddenSsid = source.isHiddenSsid;
            this.securityType = source.securityType;
            this.creatorUid = source.creatorUid;
            this.disableReason = source.disableReason;
            this.netId = source.netId;
            this.isDataRestricted = source.isDataRestricted;
            this.randomMacType = source.randomMacType;
            this.randomMacAddr = source.randomMacAddr;
            this.ipType = source.ipType;
            StaticIpConfiguration staticIpConfiguration = source.staticIp;
            WifiEapConfig wifiEapConfig = null;
            this.staticIp = staticIpConfiguration == null ? null : new StaticIpConfiguration(staticIpConfiguration);
            this.proxyType = source.proxyType;
            ProxyInfo proxyInfo = source.httpProxy;
            this.httpProxy = proxyInfo == null ? null : new ProxyInfo(proxyInfo);
            WifiEapConfig wifiEapConfig2 = source.eapConfig;
            this.eapConfig = wifiEapConfig2 != null ? new WifiEapConfig(wifiEapConfig2) : wifiEapConfig;
            this.callingPackage = source.callingPackage;
            this.wapiPskType = source.wapiPskType;
            this.wapiAsCert = source.wapiAsCert;
            this.wapiUserCert = source.wapiUserCert;
        }
    }

    public WifiDeviceConfig() {
        this.ipType = IpType.UNKNOWN;
        this.proxyType = ProxyType.UNKNOWN;
        this.ssid = null;
        this.bssid = null;
        this.preSharedKey = null;
        this.isHiddenSsid = false;
        this.securityType = -1;
        this.creatorUid = -1;
        this.disableReason = 0;
        this.netId = -1;
        this.callingPackage = null;
        this.eapConfig = new WifiEapConfig();
        this.isDataRestricted = false;
        this.ipType = IpType.UNKNOWN;
        this.proxyType = ProxyType.UNKNOWN;
        this.httpProxy = null;
        this.staticIp = null;
        this.randomMacType = 1;
        this.randomMacAddr = MacAddress.fromString("02:00:00:00:00:00").toByteArray();
        this.wapiPskType = -1;
        this.wapiAsCert = null;
        this.wapiUserCert = null;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ssid);
        dest.writeString(this.bssid);
        dest.writeString(this.preSharedKey);
        dest.writeInt(this.isHiddenSsid ? 1 : 0);
        dest.writeInt(this.securityType);
        dest.writeInt(this.creatorUid);
        dest.writeInt(this.disableReason);
        dest.writeInt(this.netId);
        dest.writeInt(this.isDataRestricted ? 1 : 0);
        dest.writeInt(this.randomMacType);
        byte[] bArr = this.randomMacAddr;
        if (bArr == null || bArr.length != 6) {
            dest.writeInt(-1);
        } else {
            dest.writeInt(bArr.length);
            dest.writeByteArray(this.randomMacAddr);
        }
        dest.writeString(this.ipType.name());
        writeParcelable(dest, this.staticIp);
        dest.writeString(this.proxyType.name());
        writeParcelable(dest, this.httpProxy);
        writeParcelable(dest, this.eapConfig);
        dest.writeString(this.callingPackage);
        dest.writeInt(this.wapiPskType);
        dest.writeString(this.wapiAsCert);
        dest.writeString(this.wapiUserCert);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public static WifiDeviceConfig fromWifiConfiguration(WifiConfiguration config) {
        WifiEapConfig wifiEapConfig;
        WifiDeviceConfig deviceConfig = new WifiDeviceConfig();
        if (config != null) {
            deviceConfig.ssid = config.SSID;
            deviceConfig.bssid = config.BSSID;
            deviceConfig.preSharedKey = config.preSharedKey;
            deviceConfig.isHiddenSsid = config.hiddenSSID;
            deviceConfig.securityType = calSecurityType(config);
            deviceConfig.creatorUid = config.creatorUid;
            deviceConfig.disableReason = config.getNetworkSelectionStatus().getNetworkSelectionDisableReason();
            deviceConfig.netId = config.networkId;
            convertWapiFromWifiConfig(deviceConfig, config);
            deviceConfig.isDataRestricted = config.meteredHint;
            deviceConfig.callingPackage = config.callingPackage;
            deviceConfig.randomMacType = config.macRandomizationSetting;
            MacAddress addr = config.getRandomizedMacAddress();
            if (addr != null) {
                deviceConfig.randomMacAddr = addr.toByteArray();
            } else {
                deviceConfig.randomMacAddr = MacAddress.fromString("02:00:00:00:00:00").toByteArray();
            }
            IpConfiguration.IpAssignment ipType2 = config.getIpAssignment();
            if (ipType2 == IpConfiguration.IpAssignment.STATIC) {
                deviceConfig.ipType = IpType.STATIC;
            } else if (ipType2 == IpConfiguration.IpAssignment.DHCP) {
                deviceConfig.ipType = IpType.DHCP;
            } else {
                deviceConfig.ipType = IpType.UNKNOWN;
            }
            IpConfiguration.ProxySettings proxyType2 = config.getProxySettings();
            if (proxyType2 == IpConfiguration.ProxySettings.NONE) {
                deviceConfig.proxyType = ProxyType.NONE;
            } else if (proxyType2 == IpConfiguration.ProxySettings.STATIC) {
                deviceConfig.proxyType = ProxyType.STATIC;
            } else if (proxyType2 == IpConfiguration.ProxySettings.PAC) {
                deviceConfig.proxyType = ProxyType.PAC;
            } else {
                deviceConfig.proxyType = ProxyType.UNKNOWN;
            }
            deviceConfig.httpProxy = config.getHttpProxy();
            deviceConfig.staticIp = config.getStaticIpConfiguration();
            if (config.enterpriseConfig == null) {
                wifiEapConfig = null;
            } else {
                wifiEapConfig = WifiEapConfig.fromWifiEnterpriseConfig(config.enterpriseConfig);
            }
            deviceConfig.eapConfig = wifiEapConfig;
        }
        return deviceConfig;
    }

    public WifiConfiguration toWifiConfig(Context ctx) {
        int i = this.securityType;
        if ((i == 0 || i == 6) && !TextUtils.isEmpty(this.preSharedKey)) {
            Log.w(TAG, "Invalid config: Open network with preSharedKey.");
            return null;
        } else if (ctx == null) {
            Log.w(TAG, "context is null");
            return null;
        } else {
            WifiConfiguration config = new WifiConfiguration();
            int wapiType = Settings.Global.getInt(ctx.getContentResolver(), WAPI_TYPE, -1);
            config.SSID = this.ssid;
            config.BSSID = this.bssid;
            config.hiddenSSID = this.isHiddenSsid;
            fillSecurity(config, wapiType);
            fillPreSharedKey(config);
            fillWapiConfig(config, wapiType);
            config.creatorUid = this.creatorUid;
            config.meteredHint = this.isDataRestricted;
            config.callingPackage = this.callingPackage;
            config.macRandomizationSetting = this.randomMacType;
            try {
                config.setRandomizedMacAddress(MacAddress.fromBytes(this.randomMacAddr));
            } catch (IllegalArgumentException e) {
                config.setRandomizedMacAddress(MacAddress.fromString("02:00:00:00:00:00"));
            }
            config.setIpConfiguration(new IpConfiguration());
            fillIpConfiguration(config);
            config.enterpriseConfig = new WifiEnterpriseConfig();
            WifiEapConfig wifiEapConfig = this.eapConfig;
            if (wifiEapConfig != null) {
                config.enterpriseConfig = wifiEapConfig.toWifiEnterpriseConfig();
            }
            return config;
        }
    }

    /* access modifiers changed from: private */
    public static <T extends Parcelable> T readParcelable(Parcelable.Creator<T> creator, Parcel in) {
        if (in.readInt() == 0) {
            return null;
        }
        return creator.createFromParcel(in);
    }

    /* access modifiers changed from: private */
    public static <T extends Enum<T>> T convertEnum(Class<T> enumType, String enumStr, T defVal) {
        if (enumStr == null || enumStr.isEmpty()) {
            Log.w(TAG, "Read enum string is empty in eap config");
            return defVal;
        }
        try {
            return (T) Enum.valueOf(enumType, enumStr);
        } catch (IllegalArgumentException e) {
            return defVal;
        }
    }

    private static void convertWapiFromWifiConfig(WifiDeviceConfig deviceConfig, WifiConfiguration config) {
        if (deviceConfig.securityType == 8) {
            if (config.wapiPskTypeBcm == 1 || config.wapiPskTypeQualcomm == 1) {
                deviceConfig.wapiPskType = 1;
            } else {
                deviceConfig.wapiPskType = 0;
            }
        }
        if (config.allowedKeyManagement.get(17)) {
            deviceConfig.wapiAsCert = config.wapiAsCertBcm;
            deviceConfig.wapiUserCert = config.wapiUserCertBcm;
        }
        if (config.allowedKeyManagement.get(19)) {
            deviceConfig.wapiAsCert = config.wapiAsCertQualcomm;
            deviceConfig.wapiUserCert = config.wapiUserCertQualcomm;
        }
    }

    private void fillWapiConfig(WifiConfiguration config, int wapiType) {
        int i = 0;
        if (wapiType == 0) {
            if (this.securityType == 8) {
                config.wapiPskTypeBcm = this.wapiPskType == 1 ? 1 : 0;
            }
            if (this.securityType == 7) {
                config.wapiAsCertBcm = "keystore://WAPIAS_" + this.wapiAsCert;
                config.wapiUserCertBcm = "keystore://WAPIUSR_" + this.wapiUserCert;
            }
        }
        if (wapiType == 1) {
            if (this.securityType == 8) {
                if (this.wapiPskType == 1) {
                    i = 1;
                }
                config.wapiPskTypeQualcomm = i;
            }
            if (this.securityType == 7) {
                config.wapiAsCertQualcomm = "keystore://WAPIAS_" + this.wapiAsCert;
                config.wapiUserCertQualcomm = "keystore://WAPIUSR_" + this.wapiUserCert;
            }
        }
    }

    private void fillIpConfiguration(WifiConfiguration config) {
        if (this.ipType == IpType.STATIC) {
            config.setIpAssignment(IpConfiguration.IpAssignment.STATIC);
        } else if (this.ipType == IpType.DHCP) {
            config.setIpAssignment(IpConfiguration.IpAssignment.DHCP);
        } else {
            config.setIpAssignment(IpConfiguration.IpAssignment.UNASSIGNED);
        }
        if (this.proxyType == ProxyType.NONE) {
            config.setProxySettings(IpConfiguration.ProxySettings.NONE);
        } else if (this.proxyType == ProxyType.STATIC) {
            config.setProxySettings(IpConfiguration.ProxySettings.STATIC);
        } else if (this.proxyType == ProxyType.PAC) {
            config.setProxySettings(IpConfiguration.ProxySettings.PAC);
        } else {
            config.setProxySettings(IpConfiguration.ProxySettings.UNASSIGNED);
        }
        config.setStaticIpConfiguration(this.staticIp);
        try {
            config.setHttpProxy(this.httpProxy);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "http proxy in config is invalid");
        }
    }

    private void clearSecurity(WifiConfiguration config) {
        config.allowedKeyManagement.clear();
        config.allowedProtocols.clear();
        config.allowedAuthAlgorithms.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedGroupCiphers.clear();
        config.allowedGroupManagementCiphers.clear();
        config.allowedSuiteBCiphers.clear();
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0055  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x005c  */
    private void fillSecurity(WifiConfiguration config, int wapiType) {
        clearSecurity(config);
        switch (this.securityType) {
            case 0:
                config.allowedKeyManagement.set(0);
                return;
            case 1:
                config.allowedAuthAlgorithms.set(0);
                config.allowedAuthAlgorithms.set(1);
                config.allowedKeyManagement.set(0);
                return;
            case 2:
                if (WIFI_FAST_BSS_TRANSITION_ENABLED) {
                    config.allowedKeyManagement.set(6);
                    return;
                } else {
                    config.allowedKeyManagement.set(1);
                    return;
                }
            case 3:
                if (!WIFI_FAST_BSS_TRANSITION_ENABLED) {
                    config.allowedKeyManagement.set(7);
                    return;
                }
                config.allowedKeyManagement.set(2);
                config.allowedKeyManagement.set(3);
                return;
            case 4:
                config.allowedKeyManagement.set(8);
                config.requirePMF = true;
                return;
            case 5:
                config.allowedKeyManagement.set(10);
                config.allowedGroupCiphers.set(5);
                config.allowedGroupManagementCiphers.set(2);
                config.requirePMF = true;
                if (!WIFI_FAST_BSS_TRANSITION_ENABLED) {
                }
                break;
            case 6:
                config.allowedKeyManagement.set(9);
                config.requirePMF = true;
                return;
            case 7:
                config.allowedKeyManagement.set(wapiType == 1 ? 19 : 17);
                return;
            case 8:
                config.allowedKeyManagement.set(wapiType == 1 ? 18 : 16);
                return;
            default:
                return;
        }
    }

    private void addQuotePreSharedKey(WifiConfiguration config) {
        if (TextUtils.isEmpty(this.preSharedKey)) {
            return;
        }
        if (!this.preSharedKey.startsWith("\"") || !this.preSharedKey.endsWith("\"")) {
            config.preSharedKey = '\"' + this.preSharedKey + '\"';
            return;
        }
        config.preSharedKey = this.preSharedKey;
    }

    private void fillPreSharedKey(WifiConfiguration config) {
        if (!TextUtils.isEmpty(this.preSharedKey)) {
            int i = this.securityType;
            if (i != 1) {
                if (i != 2) {
                    if (i == 4) {
                        addQuotePreSharedKey(config);
                        return;
                    } else if (i != 8) {
                        return;
                    }
                }
                if (this.preSharedKey.matches("[0-9A-Fa-f]{64}")) {
                    config.preSharedKey = this.preSharedKey;
                } else {
                    addQuotePreSharedKey(config);
                }
            } else {
                int length = this.preSharedKey.length();
                if ((length == 10 || length == 26 || length == 58) && this.preSharedKey.matches("[0-9A-Fa-f]*")) {
                    config.wepKeys[0] = this.preSharedKey;
                    return;
                }
                String[] strArr = config.wepKeys;
                strArr[0] = '\"' + this.preSharedKey + '\"';
            }
        }
    }

    protected static int calSecurityType(WifiConfiguration config) {
        if (config == null) {
            return 0;
        }
        if (config.allowedKeyManagement.get(8)) {
            return 4;
        }
        if (config.allowedKeyManagement.get(9)) {
            return 6;
        }
        if (config.allowedKeyManagement.get(1) || config.allowedKeyManagement.get(4) || config.allowedKeyManagement.get(6)) {
            return 2;
        }
        if (config.allowedKeyManagement.get(10)) {
            return 5;
        }
        if (config.allowedKeyManagement.get(2) || config.allowedKeyManagement.get(3) || config.allowedKeyManagement.get(7)) {
            return 3;
        }
        if (config.allowedKeyManagement.get(16) || config.allowedKeyManagement.get(18)) {
            return 8;
        }
        if (config.allowedKeyManagement.get(17) || config.allowedKeyManagement.get(19)) {
            return 7;
        }
        if (!config.allowedKeyManagement.get(0)) {
            Log.w(TAG, "Unsupported keymgmt");
        } else if (!TextUtils.isEmpty(config.preSharedKey)) {
            return 1;
        }
        return 0;
    }

    private void writeParcelable(Parcel dest, Parcelable p) {
        if (p != null) {
            dest.writeInt(1);
            p.writeToParcel(dest, 1);
            return;
        }
        dest.writeInt(0);
    }

    protected static final class Security {
        public static final int EAP = 3;
        public static final int EAP_SUITE_B = 5;
        public static final int INVALID = -1;
        public static final int OPEN = 0;
        public static final int OWE = 6;
        public static final int PSK = 2;
        public static final int SAE = 4;
        public static final int WAPI_CERT = 7;
        public static final int WAPI_PSK = 8;
        public static final int WEP = 1;

        public static final class Wapi {
            public static final int PSK_ASCII = 0;
            public static final int PSK_HEX = 1;
        }

        protected Security() {
        }
    }

    public String toString() {
        String str;
        String str2;
        String str3;
        String str4;
        StringBuffer sb = new StringBuffer();
        sb.append("ssid: ");
        String str5 = this.ssid;
        sb.append(str5 != null ? SafeDisplayUtil.safeDisplaySsid(str5) : "null");
        sb.append(LINE);
        sb.append("bssid: ");
        String str6 = this.bssid;
        if (str6 != null) {
            str = SafeDisplayUtil.safeDisplayBssid(str6);
        } else {
            str = "null";
        }
        sb.append(str);
        sb.append(LINE);
        sb.append("isHiddenSsid: ");
        sb.append(this.isHiddenSsid);
        sb.append(LINE);
        sb.append("securityType: ");
        sb.append(this.securityType);
        sb.append(LINE);
        sb.append("creatorUid: ");
        sb.append(this.creatorUid);
        sb.append(LINE);
        sb.append("disableReason: ");
        sb.append(this.disableReason);
        sb.append(LINE);
        sb.append("netId: ");
        sb.append(this.netId);
        sb.append(LINE);
        sb.append("isDataRestricted: ");
        sb.append(this.isDataRestricted);
        sb.append(LINE);
        sb.append("randomMacType: ");
        sb.append(this.randomMacType);
        sb.append(LINE);
        sb.append("ipType: ");
        sb.append(this.ipType.name());
        sb.append(LINE);
        sb.append("staticIp: ");
        StaticIpConfiguration staticIpConfiguration = this.staticIp;
        if (staticIpConfiguration != null) {
            str2 = staticIpConfiguration.toString();
        } else {
            str2 = "null";
        }
        sb.append(str2);
        sb.append(LINE);
        sb.append("proxyType: ");
        sb.append(this.proxyType.name());
        sb.append(LINE);
        sb.append("httpProxy: ");
        ProxyInfo proxyInfo = this.httpProxy;
        if (proxyInfo != null) {
            str3 = proxyInfo.toString();
        } else {
            str3 = "null";
        }
        sb.append(str3);
        sb.append(LINE);
        sb.append("eapConfig: ");
        WifiEapConfig wifiEapConfig = this.eapConfig;
        if (wifiEapConfig != null) {
            str4 = wifiEapConfig.toString();
        } else {
            str4 = "null";
        }
        sb.append(str4);
        sb.append(LINE);
        sb.append("callingPackage: ");
        String str7 = this.callingPackage;
        if (str7 == null) {
            str7 = "null";
        }
        sb.append(str7);
        sb.append(LINE);
        sb.append("wapiPskType: ");
        sb.append(this.wapiPskType);
        sb.append(LINE);
        sb.append("wapiAsCert: ");
        String str8 = this.wapiAsCert;
        if (str8 == null) {
            str8 = "null";
        }
        sb.append(str8);
        sb.append(LINE);
        sb.append("wapiUserCert: ");
        String str9 = this.wapiUserCert;
        if (str9 == null) {
            str9 = "null";
        }
        sb.append(str9);
        sb.append(LINE);
        return sb.toString();
    }
}
