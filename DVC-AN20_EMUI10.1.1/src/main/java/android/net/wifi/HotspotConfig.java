package android.net.wifi;

import android.net.wifi.hwUtil.SafeDisplayUtil;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import java.util.UUID;

public class HotspotConfig implements Parcelable {
    public static final Parcelable.Creator<HotspotConfig> CREATOR = new Parcelable.Creator<HotspotConfig>() {
        /* class android.net.wifi.HotspotConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HotspotConfig createFromParcel(Parcel in) {
            HotspotConfig config = new HotspotConfig();
            config.ssid = in.readString();
            config.securityType = in.readInt();
            config.band = in.readInt();
            config.preSharedKey = in.readString();
            config.maxConn = in.readInt();
            return config;
        }

        @Override // android.os.Parcelable.Creator
        public HotspotConfig[] newArray(int size) {
            return new HotspotConfig[size];
        }
    };
    public static final int DEFAULT_MAX_CONN = 8;
    private static final String TAG = "HotspotConfig";
    private int band;
    private int maxConn;
    private String preSharedKey;
    private int securityType;
    private String ssid;

    public HotspotConfig(HotspotConfig source) {
        if (source != null) {
            this.ssid = source.ssid;
            this.securityType = source.securityType;
            this.band = source.band;
            this.preSharedKey = source.preSharedKey;
            this.maxConn = source.maxConn;
        }
    }

    public HotspotConfig() {
        this.ssid = null;
        this.securityType = 2;
        this.band = 0;
        String randomUUID = UUID.randomUUID().toString();
        this.preSharedKey = randomUUID.substring(0, 8) + randomUUID.substring(9, 13);
        this.maxConn = 8;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ssid);
        dest.writeInt(this.securityType);
        dest.writeInt(this.band);
        dest.writeString(this.preSharedKey);
        dest.writeInt(this.maxConn);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public static HotspotConfig fromWifiConfiguration(WifiConfiguration config) {
        HotspotConfig hotspotConfig = new HotspotConfig();
        if (config != null) {
            hotspotConfig.ssid = config.SSID;
            hotspotConfig.securityType = WifiDeviceConfig.calSecurityType(config);
            hotspotConfig.band = config.apBand;
            hotspotConfig.preSharedKey = config.preSharedKey;
        }
        return hotspotConfig;
    }

    public WifiConfiguration toWifiConfig() {
        int i = this.securityType;
        if ((i == 0 || i == 6) && !TextUtils.isEmpty(this.preSharedKey)) {
            Log.w(TAG, "Invalid config: Open network with preSharedKey.");
            return null;
        }
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = this.ssid;
        fillSecurity(config);
        config.apBand = this.band;
        return config;
    }

    public void setMaxConn(int maxConn2) {
        this.maxConn = maxConn2;
    }

    public int getMaxConn() {
        return this.maxConn;
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("Hotspot Config:\n");
        sbuf.append("SSID: " + SafeDisplayUtil.safeDisplaySsid(this.ssid) + "\n");
        sbuf.append("SecurityType: " + String.valueOf(this.securityType) + "\n");
        sbuf.append("Band: " + String.valueOf(this.band) + "\n");
        StringBuilder sb = new StringBuilder();
        sb.append("PreSharedKey: ");
        sb.append(this.preSharedKey == null ? "null" : "*");
        sb.append("\n");
        sbuf.append(sb.toString());
        sbuf.append("MaxConn: " + String.valueOf(this.maxConn) + "\n");
        return sbuf.toString();
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
        } else if (i == 2) {
            config.allowedKeyManagement.set(4);
            config.allowedAuthAlgorithms.set(0);
            if (!TextUtils.isEmpty(this.preSharedKey)) {
                config.preSharedKey = this.preSharedKey;
            }
        }
    }
}
