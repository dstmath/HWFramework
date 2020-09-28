package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
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
            config.isHiddenSsid = in.readInt() != 0;
            config.securityType = in.readInt();
            config.creatorUid = in.readInt();
            return config;
        }

        @Override // android.os.Parcelable.Creator
        public WifiDeviceConfig[] newArray(int size) {
            return new WifiDeviceConfig[size];
        }
    };
    private static final String TAG = "WifiDeviceConfig";
    private String bssid;
    private int creatorUid;
    private int disableReason;
    private boolean isHiddenSsid;
    private String preSharedKey;
    private int securityType;
    private String ssid;

    public WifiDeviceConfig(WifiDeviceConfig source) {
        if (source != null) {
            this.ssid = source.ssid;
            this.bssid = source.bssid;
            this.preSharedKey = source.preSharedKey;
            this.isHiddenSsid = source.isHiddenSsid;
            this.securityType = source.securityType;
            this.creatorUid = source.creatorUid;
            this.disableReason = source.disableReason;
        }
    }

    public WifiDeviceConfig() {
        this.ssid = null;
        this.bssid = null;
        this.preSharedKey = null;
        this.isHiddenSsid = false;
        this.securityType = -1;
        this.creatorUid = -1;
        this.disableReason = 0;
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
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public static WifiDeviceConfig fromWifiConfiguration(WifiConfiguration config) {
        WifiDeviceConfig deviceConfig = new WifiDeviceConfig();
        if (config != null) {
            deviceConfig.ssid = config.SSID;
            deviceConfig.bssid = config.BSSID;
            deviceConfig.preSharedKey = config.preSharedKey;
            deviceConfig.isHiddenSsid = config.hiddenSSID;
            deviceConfig.securityType = calSecurityType(config);
            deviceConfig.creatorUid = config.creatorUid;
            deviceConfig.disableReason = config.getNetworkSelectionStatus().getNetworkSelectionDisableReason();
        }
        return deviceConfig;
    }

    public WifiConfiguration toWifiConfig() {
        int i = this.securityType;
        if ((i == 0 || i == 6) && !TextUtils.isEmpty(this.preSharedKey)) {
            Log.w(TAG, "Invalid config: Open network with preSharedKey.");
            return null;
        }
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = this.ssid;
        config.BSSID = this.bssid;
        config.hiddenSSID = true;
        fillSecurity(config);
        config.creatorUid = this.creatorUid;
        return config;
    }

    private void fillSecurity(WifiConfiguration config) {
        config.allowedKeyManagement.clear();
        config.allowedProtocols.clear();
        config.allowedAuthAlgorithms.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedGroupCiphers.clear();
        config.allowedGroupManagementCiphers.clear();
        config.allowedSuiteBCiphers.clear();
        int i = this.securityType;
        if (i == 0) {
            config.allowedKeyManagement.set(0);
        } else if (i == 1) {
            config.allowedKeyManagement.set(0);
            config.allowedAuthAlgorithms.set(0);
            config.allowedAuthAlgorithms.set(1);
            if (!TextUtils.isEmpty(this.preSharedKey)) {
                int length = this.preSharedKey.length();
                if ((length == 10 || length == 26 || length == 58) && this.preSharedKey.matches("[0-9A-Fa-f]*")) {
                    config.wepKeys[0] = this.preSharedKey;
                    return;
                }
                String[] strArr = config.wepKeys;
                strArr[0] = '\"' + this.preSharedKey + '\"';
            }
        } else if (i == 2) {
            config.allowedKeyManagement.set(1);
            if (TextUtils.isEmpty(this.preSharedKey)) {
                return;
            }
            if (this.preSharedKey.matches("[0-9A-Fa-f]{64}")) {
                config.preSharedKey = this.preSharedKey;
                return;
            }
            config.preSharedKey = '\"' + this.preSharedKey + '\"';
        } else if (i == 4) {
            config.allowedKeyManagement.set(8);
            config.requirePMF = true;
            if (!TextUtils.isEmpty(this.preSharedKey)) {
                config.preSharedKey = '\"' + this.preSharedKey + '\"';
            }
        } else if (i == 6) {
            config.allowedKeyManagement.set(9);
            config.requirePMF = true;
        }
    }

    protected static int calSecurityType(WifiConfiguration config) {
        int keyMgmt = config.getAuthType();
        if (keyMgmt != 0) {
            if (keyMgmt != 1) {
                if (keyMgmt == 2) {
                    return 3;
                }
                if (keyMgmt != 4) {
                    switch (keyMgmt) {
                        case 8:
                            return 4;
                        case 9:
                            return 6;
                        case 10:
                            return 5;
                        default:
                            return 0;
                    }
                }
            }
            return 2;
        } else if (!TextUtils.isEmpty(config.preSharedKey)) {
            return 1;
        } else {
            return 0;
        }
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

        protected Security() {
        }
    }
}
