package android.net.wifi;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.net.NetworkInfo;
import android.net.NetworkUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.EnumMap;
import java.util.Locale;

public class WifiInfo implements Parcelable {
    @UnsupportedAppUsage
    public static final Parcelable.Creator<WifiInfo> CREATOR = new Parcelable.Creator<WifiInfo>() {
        /* class android.net.wifi.WifiInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WifiInfo createFromParcel(Parcel in) {
            WifiInfo info = new WifiInfo();
            info.setNetworkId(in.readInt());
            info.setLastNetIdForAp(in.readInt());
            info.setRssi(in.readInt());
            info.setLinkSpeed(in.readInt());
            info.setTxLinkSpeedMbps(in.readInt());
            info.setRxLinkSpeedMbps(in.readInt());
            info.setFrequency(in.readInt());
            boolean z = true;
            if (in.readByte() == 1) {
                try {
                    info.setInetAddress(InetAddress.getByAddress(in.createByteArray()));
                } catch (UnknownHostException e) {
                }
            }
            if (in.readInt() == 1) {
                info.mWifiSsid = WifiSsid.CREATOR.createFromParcel(in);
            }
            info.mBSSID = in.readString();
            info.mMacAddress = in.readString();
            info.mMeteredHint = in.readInt() != 0;
            info.mEphemeral = in.readInt() != 0;
            info.mTrusted = in.readInt() != 0;
            info.score = in.readInt();
            info.txSuccess = in.readLong();
            info.txSuccessRate = in.readDouble();
            info.txRetries = in.readLong();
            info.txRetriesRate = in.readDouble();
            info.txBad = in.readLong();
            info.txBadRate = in.readDouble();
            info.rxSuccess = in.readLong();
            info.rxSuccessRate = in.readDouble();
            info.mSupplicantState = SupplicantState.CREATOR.createFromParcel(in);
            if (in.readInt() == 0) {
                z = false;
            }
            info.mOsuAp = z;
            info.mNetworkSuggestionOrSpecifierPackageName = in.readString();
            info.mFqdn = in.readString();
            info.mProviderFriendlyName = in.readString();
            info.mSupportedWifiCategory = in.readInt();
            info.setWifiApType(in.readInt());
            return info;
        }

        @Override // android.os.Parcelable.Creator
        public WifiInfo[] newArray(int size) {
            return new WifiInfo[size];
        }
    };
    @UnsupportedAppUsage
    public static final String DEFAULT_MAC_ADDRESS = "02:00:00:00:00:00";
    public static final int DUPLEX_PATH_SELECTED = 1;
    public static final String FREQUENCY_UNITS = "MHz";
    @UnsupportedAppUsage
    public static final int INVALID_RSSI = -127;
    public static final String LINK_SPEED_UNITS = "Mbps";
    public static final int LINK_SPEED_UNKNOWN = -1;
    public static final int MAX_RSSI = 200;
    public static final int MIN_RSSI = -126;
    private static final int NET_ID_NONE = -1;
    private static final String TAG = "WifiInfo";
    public static final int WIFI_AP_TYPE_AI_DEVICE = 1;
    public static final int WIFI_AP_TYPE_BASE = 0;
    public static final int WIFI_AP_TYPE_DC = 100;
    public static final int WIFI_AP_TYPE_NORMAL = 0;
    public static final int WIFI_CATEGORY_DEFAULT = 1;
    public static final int WIFI_CATEGORY_WIFI6 = 2;
    public static final int WIFI_CATEGORY_WIFI7 = 3;
    public static final int WIFI_CATEGORY_WIFI8 = 4;
    private static final EnumMap<SupplicantState, NetworkInfo.DetailedState> stateMap = new EnumMap<>(SupplicantState.class);
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private String mBSSID;
    private int mChload;
    private boolean mEphemeral;
    private String mFqdn;
    private int mFrequency;
    @UnsupportedAppUsage
    private InetAddress mIpAddress;
    private int mLinkSpeed;
    @UnsupportedAppUsage
    private String mMacAddress;
    private boolean mMeteredHint;
    private int mNetworkId;
    private String mNetworkSuggestionOrSpecifierPackageName;
    private int mNoise;
    private boolean mOsuAp;
    private String mProviderFriendlyName;
    private int mRssi;
    private int mRxLinkSpeed;
    private int mSnr;
    private SupplicantState mSupplicantState;
    private int mSupportedWifiCategory;
    private boolean mTrusted;
    private int mTxLinkSpeed;
    private int mUlDelay;
    private int mWifiApType;
    @UnsupportedAppUsage
    private WifiSsid mWifiSsid;
    private int mlastNetIdForAp;
    public long rxSuccess;
    public double rxSuccessRate;
    @UnsupportedAppUsage
    public int score;
    public long txBad;
    public double txBadRate;
    public long txRetries;
    public double txRetriesRate;
    public long txSuccess;
    public double txSuccessRate;

    static {
        stateMap.put(SupplicantState.DISCONNECTED, NetworkInfo.DetailedState.DISCONNECTED);
        stateMap.put(SupplicantState.INTERFACE_DISABLED, NetworkInfo.DetailedState.DISCONNECTED);
        stateMap.put(SupplicantState.INACTIVE, NetworkInfo.DetailedState.IDLE);
        stateMap.put(SupplicantState.SCANNING, NetworkInfo.DetailedState.SCANNING);
        stateMap.put(SupplicantState.AUTHENTICATING, NetworkInfo.DetailedState.CONNECTING);
        stateMap.put(SupplicantState.ASSOCIATING, NetworkInfo.DetailedState.CONNECTING);
        stateMap.put(SupplicantState.ASSOCIATED, NetworkInfo.DetailedState.CONNECTING);
        stateMap.put(SupplicantState.FOUR_WAY_HANDSHAKE, NetworkInfo.DetailedState.AUTHENTICATING);
        stateMap.put(SupplicantState.GROUP_HANDSHAKE, NetworkInfo.DetailedState.AUTHENTICATING);
        stateMap.put(SupplicantState.COMPLETED, NetworkInfo.DetailedState.OBTAINING_IPADDR);
        stateMap.put(SupplicantState.DORMANT, NetworkInfo.DetailedState.DISCONNECTED);
        stateMap.put(SupplicantState.UNINITIALIZED, NetworkInfo.DetailedState.IDLE);
        stateMap.put(SupplicantState.INVALID, NetworkInfo.DetailedState.FAILED);
    }

    @UnsupportedAppUsage
    public WifiInfo() {
        this.mWifiApType = 0;
        this.mMacAddress = "02:00:00:00:00:00";
        this.mSupportedWifiCategory = 1;
        this.mWifiSsid = null;
        this.mBSSID = null;
        this.mNetworkId = -1;
        this.mSupplicantState = SupplicantState.UNINITIALIZED;
        this.mRssi = -127;
        this.mLinkSpeed = -1;
        this.mFrequency = -1;
        this.mNoise = 0;
        this.mUlDelay = -1;
        this.mlastNetIdForAp = -1;
        this.mSnr = -1;
        this.mChload = -1;
        this.mWifiApType = 0;
    }

    public void reset() {
        setInetAddress(null);
        setBSSID(null);
        setSSID(null);
        setNetworkId(-1);
        setRssi(-127);
        setLinkSpeed(-1);
        setTxLinkSpeedMbps(-1);
        setRxLinkSpeedMbps(-1);
        setFrequency(-1);
        setMeteredHint(false);
        setEphemeral(false);
        setOsuAp(false);
        setNetworkSuggestionOrSpecifierPackageName(null);
        setFQDN(null);
        setProviderFriendlyName(null);
        setNoise(0);
        setSnr(-1);
        setChload(-1);
        setUlDelay(-1);
        this.txBad = 0;
        this.txSuccess = 0;
        this.rxSuccess = 0;
        this.txRetries = 0;
        this.txBadRate = 0.0d;
        this.txSuccessRate = 0.0d;
        this.rxSuccessRate = 0.0d;
        this.txRetriesRate = 0.0d;
        this.score = 0;
        this.mSupportedWifiCategory = 1;
        this.mWifiApType = 0;
    }

    public WifiInfo(WifiInfo source) {
        this.mWifiApType = 0;
        this.mMacAddress = "02:00:00:00:00:00";
        this.mSupportedWifiCategory = 1;
        if (source != null) {
            this.mSupplicantState = source.mSupplicantState;
            this.mBSSID = source.mBSSID;
            this.mWifiSsid = source.mWifiSsid;
            this.mNetworkId = source.mNetworkId;
            this.mRssi = source.mRssi;
            this.mLinkSpeed = source.mLinkSpeed;
            this.mTxLinkSpeed = source.mTxLinkSpeed;
            this.mRxLinkSpeed = source.mRxLinkSpeed;
            this.mFrequency = source.mFrequency;
            this.mIpAddress = source.mIpAddress;
            this.mMacAddress = source.mMacAddress;
            this.mMeteredHint = source.mMeteredHint;
            this.mEphemeral = source.mEphemeral;
            this.mTrusted = source.mTrusted;
            this.mNetworkSuggestionOrSpecifierPackageName = source.mNetworkSuggestionOrSpecifierPackageName;
            this.mOsuAp = source.mOsuAp;
            this.mFqdn = source.mFqdn;
            this.mProviderFriendlyName = source.mProviderFriendlyName;
            this.txBad = source.txBad;
            this.txRetries = source.txRetries;
            this.txSuccess = source.txSuccess;
            this.rxSuccess = source.rxSuccess;
            this.txBadRate = source.txBadRate;
            this.txRetriesRate = source.txRetriesRate;
            this.txSuccessRate = source.txSuccessRate;
            this.rxSuccessRate = source.rxSuccessRate;
            this.score = source.score;
            this.mNoise = source.mNoise;
            this.mUlDelay = source.mUlDelay;
            this.mlastNetIdForAp = source.mlastNetIdForAp;
            this.mSnr = source.mSnr;
            this.mChload = source.mChload;
            this.mSupportedWifiCategory = source.mSupportedWifiCategory;
            this.mWifiApType = source.mWifiApType;
        }
    }

    public void setSSID(WifiSsid wifiSsid) {
        this.mWifiSsid = wifiSsid;
    }

    public String getSSID() {
        WifiSsid wifiSsid = this.mWifiSsid;
        if (wifiSsid != null) {
            try {
                String unicode = wifiSsid.toString();
                if (!TextUtils.isEmpty(unicode)) {
                    return "\"" + unicode + "\"";
                }
                String hex = this.mWifiSsid.getHexString();
                if (hex != null) {
                    return hex;
                }
                return "<unknown ssid>";
            } catch (NullPointerException e) {
                Log.d(TAG, "NullPointerException e: " + e);
            }
        }
        return "<unknown ssid>";
    }

    @UnsupportedAppUsage
    public WifiSsid getWifiSsid() {
        return this.mWifiSsid;
    }

    @UnsupportedAppUsage
    public void setBSSID(String BSSID) {
        this.mBSSID = BSSID;
    }

    public String getBSSID() {
        return this.mBSSID;
    }

    public void setSupportedWifiCategory(int supportedWifiCategory) {
        this.mSupportedWifiCategory = supportedWifiCategory;
    }

    public int getSupportedWifiCategory() {
        return this.mSupportedWifiCategory;
    }

    public int getRssi() {
        return this.mRssi;
    }

    @UnsupportedAppUsage
    public void setRssi(int rssi) {
        if (rssi < -127) {
            rssi = -127;
        }
        if (rssi > 200) {
            rssi = 200;
        }
        this.mRssi = rssi;
    }

    public int getLinkSpeed() {
        return this.mLinkSpeed;
    }

    @UnsupportedAppUsage
    public void setLinkSpeed(int linkSpeed) {
        this.mLinkSpeed = linkSpeed;
    }

    public int getTxLinkSpeedMbps() {
        return this.mTxLinkSpeed;
    }

    public void setTxLinkSpeedMbps(int txLinkSpeed) {
        this.mTxLinkSpeed = txLinkSpeed;
    }

    public int getRxLinkSpeedMbps() {
        return this.mRxLinkSpeed;
    }

    public void setRxLinkSpeedMbps(int rxLinkSpeed) {
        this.mRxLinkSpeed = rxLinkSpeed;
    }

    public int getFrequency() {
        return this.mFrequency;
    }

    public void setFrequency(int frequency) {
        this.mFrequency = frequency;
    }

    public int getNoise() {
        return this.mNoise;
    }

    public void setNoise(int noise) {
        this.mNoise = noise;
    }

    public int getSnr() {
        return this.mSnr;
    }

    public void setSnr(int snr) {
        this.mSnr = snr;
    }

    public int getChload() {
        return this.mChload;
    }

    public void setChload(int chload) {
        this.mChload = chload;
    }

    public int getUlDelay() {
        return this.mUlDelay;
    }

    public void setUlDelay(int ulDelay) {
        this.mUlDelay = ulDelay;
    }

    public int getLastNetIdForAp() {
        return this.mlastNetIdForAp;
    }

    public void setLastNetIdForAp(int lastNetIdForAp) {
        this.mlastNetIdForAp = lastNetIdForAp;
    }

    public int getWifiApType() {
        return this.mWifiApType;
    }

    public void setWifiApType(int type) {
        this.mWifiApType = type;
    }

    public boolean is24GHz() {
        return ScanResult.is24GHz(this.mFrequency);
    }

    @UnsupportedAppUsage
    public boolean is5GHz() {
        return ScanResult.is5GHz(this.mFrequency);
    }

    @UnsupportedAppUsage
    public void setMacAddress(String macAddress) {
        this.mMacAddress = macAddress;
    }

    public String getMacAddress() {
        return this.mMacAddress;
    }

    public boolean hasRealMacAddress() {
        String str = this.mMacAddress;
        return str != null && !"02:00:00:00:00:00".equals(str);
    }

    public void setMeteredHint(boolean meteredHint) {
        this.mMeteredHint = meteredHint;
    }

    @UnsupportedAppUsage
    public boolean getMeteredHint() {
        return this.mMeteredHint;
    }

    public void setEphemeral(boolean ephemeral) {
        this.mEphemeral = ephemeral;
    }

    @UnsupportedAppUsage
    public boolean isEphemeral() {
        return this.mEphemeral;
    }

    public void setTrusted(boolean trusted) {
        this.mTrusted = trusted;
    }

    public boolean isTrusted() {
        return this.mTrusted;
    }

    public void setOsuAp(boolean osuAp) {
        this.mOsuAp = osuAp;
    }

    @SystemApi
    public boolean isOsuAp() {
        return this.mOsuAp;
    }

    @SystemApi
    public boolean isPasspointAp() {
        return (this.mFqdn == null || this.mProviderFriendlyName == null) ? false : true;
    }

    public void setFQDN(String fqdn) {
        this.mFqdn = fqdn;
    }

    public String getPasspointFqdn() {
        return this.mFqdn;
    }

    public void setProviderFriendlyName(String providerFriendlyName) {
        this.mProviderFriendlyName = providerFriendlyName;
    }

    public String getPasspointProviderFriendlyName() {
        return this.mProviderFriendlyName;
    }

    public void setNetworkSuggestionOrSpecifierPackageName(String packageName) {
        this.mNetworkSuggestionOrSpecifierPackageName = packageName;
    }

    public String getNetworkSuggestionOrSpecifierPackageName() {
        return this.mNetworkSuggestionOrSpecifierPackageName;
    }

    @UnsupportedAppUsage
    public void setNetworkId(int id) {
        this.mNetworkId = id;
    }

    public int getNetworkId() {
        return this.mNetworkId;
    }

    public SupplicantState getSupplicantState() {
        return this.mSupplicantState;
    }

    @UnsupportedAppUsage
    public void setSupplicantState(SupplicantState state) {
        this.mSupplicantState = state;
    }

    public void setInetAddress(InetAddress address) {
        this.mIpAddress = address;
    }

    public int getIpAddress() {
        InetAddress inetAddress = this.mIpAddress;
        if (inetAddress instanceof Inet4Address) {
            return NetworkUtils.inetAddressToInt((Inet4Address) inetAddress);
        }
        return 0;
    }

    public boolean getHiddenSSID() {
        WifiSsid wifiSsid = this.mWifiSsid;
        if (wifiSsid == null) {
            return false;
        }
        return wifiSsid.isHidden();
    }

    public static NetworkInfo.DetailedState getDetailedStateOf(SupplicantState suppState) {
        return stateMap.get(suppState);
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void setSupplicantState(String stateName) {
        this.mSupplicantState = valueOf(stateName);
    }

    static SupplicantState valueOf(String stateName) {
        if ("4WAY_HANDSHAKE".equalsIgnoreCase(stateName)) {
            return SupplicantState.FOUR_WAY_HANDSHAKE;
        }
        try {
            return SupplicantState.valueOf(stateName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return SupplicantState.INVALID;
        }
    }

    @UnsupportedAppUsage
    public static String removeDoubleQuotes(String string) {
        if (string == null) {
            return null;
        }
        int length = string.length();
        if (length > 1 && string.charAt(0) == '\"' && string.charAt(length - 1) == '\"') {
            return string.substring(1, length - 1);
        }
        return string;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("SSID: ");
        Object obj = this.mWifiSsid;
        if (obj == null) {
            obj = "<unknown ssid>";
        }
        sb.append(obj);
        sb.append(", Supplicant state: ");
        SupplicantState supplicantState = this.mSupplicantState;
        if (supplicantState == null) {
            supplicantState = "<none>";
        }
        sb.append(supplicantState);
        sb.append(", RSSI: ");
        sb.append(this.mRssi);
        sb.append(", Link speed: ");
        sb.append(this.mLinkSpeed);
        sb.append(LINK_SPEED_UNITS);
        sb.append(", Tx Link speed: ");
        sb.append(this.mTxLinkSpeed);
        sb.append(LINK_SPEED_UNITS);
        sb.append(", Rx Link speed: ");
        sb.append(this.mRxLinkSpeed);
        sb.append(LINK_SPEED_UNITS);
        sb.append(", Frequency: ");
        sb.append(this.mFrequency);
        sb.append(FREQUENCY_UNITS);
        sb.append(", Net ID: ");
        sb.append(this.mNetworkId);
        sb.append(", Metered hint: ");
        sb.append(this.mMeteredHint);
        sb.append(", score: ");
        sb.append(Integer.toString(this.score));
        return sb.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mNetworkId);
        dest.writeInt(this.mlastNetIdForAp);
        dest.writeInt(this.mRssi);
        dest.writeInt(this.mLinkSpeed);
        dest.writeInt(this.mTxLinkSpeed);
        dest.writeInt(this.mRxLinkSpeed);
        dest.writeInt(this.mFrequency);
        InetAddress address = this.mIpAddress;
        if (address != null) {
            dest.writeByte((byte) 1);
            dest.writeByteArray(address.getAddress());
        } else {
            dest.writeByte((byte) 0);
        }
        WifiSsid ssid = this.mWifiSsid;
        if (ssid != null) {
            dest.writeInt(1);
            ssid.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
        dest.writeString(this.mBSSID);
        dest.writeString(this.mMacAddress);
        dest.writeInt(this.mMeteredHint ? 1 : 0);
        dest.writeInt(this.mEphemeral ? 1 : 0);
        dest.writeInt(this.mTrusted ? 1 : 0);
        dest.writeInt(this.score);
        dest.writeLong(this.txSuccess);
        dest.writeDouble(this.txSuccessRate);
        dest.writeLong(this.txRetries);
        dest.writeDouble(this.txRetriesRate);
        dest.writeLong(this.txBad);
        dest.writeDouble(this.txBadRate);
        dest.writeLong(this.rxSuccess);
        dest.writeDouble(this.rxSuccessRate);
        SupplicantState state = this.mSupplicantState;
        if (state != null) {
            state.writeToParcel(dest, flags);
        }
        dest.writeInt(this.mOsuAp ? 1 : 0);
        dest.writeString(this.mNetworkSuggestionOrSpecifierPackageName);
        dest.writeString(this.mFqdn);
        dest.writeString(this.mProviderFriendlyName);
        dest.writeInt(this.mSupportedWifiCategory);
        dest.writeInt(this.mWifiApType);
    }
}
