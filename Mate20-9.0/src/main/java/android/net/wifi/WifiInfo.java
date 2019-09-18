package android.net.wifi;

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
    public static final Parcelable.Creator<WifiInfo> CREATOR = new Parcelable.Creator<WifiInfo>() {
        public WifiInfo createFromParcel(Parcel in) {
            WifiInfo info = new WifiInfo();
            info.setNetworkId(in.readInt());
            info.setLastNetIdForAp(in.readInt());
            info.setRssi(in.readInt());
            info.setLinkSpeed(in.readInt());
            info.setFrequency(in.readInt());
            boolean z = true;
            if (in.readByte() == 1) {
                try {
                    info.setInetAddress(InetAddress.getByAddress(in.createByteArray()));
                } catch (UnknownHostException e) {
                }
            }
            if (in.readInt() == 1) {
                WifiSsid unused = info.mWifiSsid = WifiSsid.CREATOR.createFromParcel(in);
            }
            String unused2 = info.mBSSID = in.readString();
            String unused3 = info.mMacAddress = in.readString();
            boolean unused4 = info.mMeteredHint = in.readInt() != 0;
            if (in.readInt() == 0) {
                z = false;
            }
            boolean unused5 = info.mEphemeral = z;
            info.score = in.readInt();
            info.txSuccessRate = in.readDouble();
            info.txRetriesRate = in.readDouble();
            info.txBadRate = in.readDouble();
            info.rxSuccessRate = in.readDouble();
            SupplicantState unused6 = info.mSupplicantState = SupplicantState.CREATOR.createFromParcel(in);
            return info;
        }

        public WifiInfo[] newArray(int size) {
            return new WifiInfo[size];
        }
    };
    public static final String DEFAULT_MAC_ADDRESS = "02:00:00:00:00:00";
    public static final int DUPLEX_PATH_SELECTED = 1;
    public static final String FREQUENCY_UNITS = "MHz";
    public static final int INVALID_RSSI = -127;
    public static final String LINK_SPEED_UNITS = "Mbps";
    public static final int MAX_RSSI = 200;
    public static final int MIN_RSSI = -126;
    private static final int NET_ID_NONE = -1;
    private static final String TAG = "WifiInfo";
    public static final int WIFI_AP_TYPE_AI_DEVICE = 1;
    public static final int WIFI_AP_TYPE_AI_DEVICE_SINGLE_NETWORK = 2;
    public static final int WIFI_AP_TYPE_BASE = 0;
    public static final int WIFI_AP_TYPE_NORMAL = 0;
    private static final EnumMap<SupplicantState, NetworkInfo.DetailedState> stateMap = new EnumMap<>(SupplicantState.class);
    /* access modifiers changed from: private */
    public String mBSSID;
    private int mChload;
    /* access modifiers changed from: private */
    public boolean mEphemeral;
    private int mFrequency;
    private InetAddress mIpAddress;
    private int mLinkSpeed;
    /* access modifiers changed from: private */
    public String mMacAddress;
    /* access modifiers changed from: private */
    public boolean mMeteredHint;
    private int mNetworkId;
    private int mNoise;
    private int mRssi;
    private int mSnr;
    /* access modifiers changed from: private */
    public SupplicantState mSupplicantState;
    private int mWifiApType;
    /* access modifiers changed from: private */
    public WifiSsid mWifiSsid;
    private int mlastNetIdForAp;
    public long rxSuccess;
    public double rxSuccessRate;
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

    public WifiInfo() {
        this.mWifiApType = 0;
        this.mMacAddress = DEFAULT_MAC_ADDRESS;
        this.mWifiSsid = null;
        this.mBSSID = null;
        this.mNetworkId = -1;
        this.mSupplicantState = SupplicantState.UNINITIALIZED;
        this.mRssi = INVALID_RSSI;
        this.mLinkSpeed = -1;
        this.mFrequency = -1;
        this.mNoise = 0;
        this.mlastNetIdForAp = -1;
        this.mSnr = -1;
        this.mChload = -1;
    }

    public void reset() {
        setInetAddress(null);
        setBSSID(null);
        setSSID(null);
        setNetworkId(-1);
        setRssi(INVALID_RSSI);
        setLinkSpeed(-1);
        setFrequency(-1);
        setMeteredHint(false);
        setEphemeral(false);
        setNoise(0);
        setSnr(-1);
        setChload(-1);
        this.txBad = 0;
        this.txSuccess = 0;
        this.rxSuccess = 0;
        this.txRetries = 0;
        this.txBadRate = 0.0d;
        this.txSuccessRate = 0.0d;
        this.rxSuccessRate = 0.0d;
        this.txRetriesRate = 0.0d;
        this.score = 0;
    }

    public WifiInfo(WifiInfo source) {
        this.mWifiApType = 0;
        this.mMacAddress = DEFAULT_MAC_ADDRESS;
        if (source != null) {
            this.mSupplicantState = source.mSupplicantState;
            this.mBSSID = source.mBSSID;
            this.mWifiSsid = source.mWifiSsid;
            this.mNetworkId = source.mNetworkId;
            this.mRssi = source.mRssi;
            this.mLinkSpeed = source.mLinkSpeed;
            this.mFrequency = source.mFrequency;
            this.mIpAddress = source.mIpAddress;
            this.mMacAddress = source.mMacAddress;
            this.mMeteredHint = source.mMeteredHint;
            this.mEphemeral = source.mEphemeral;
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
            this.mlastNetIdForAp = source.mlastNetIdForAp;
            this.mSnr = source.mSnr;
            this.mChload = source.mChload;
        }
    }

    public void setSSID(WifiSsid wifiSsid) {
        this.mWifiSsid = wifiSsid;
    }

    public String getSSID() {
        if (this.mWifiSsid != null) {
            try {
                String unicode = this.mWifiSsid.toString();
                if (!TextUtils.isEmpty(unicode)) {
                    return "\"" + unicode + "\"";
                }
                String hex = this.mWifiSsid.getHexString();
                return hex != null ? hex : WifiSsid.NONE;
            } catch (NullPointerException e) {
                Log.d(TAG, "NullPointerException e: " + e);
            }
        }
        return WifiSsid.NONE;
    }

    public WifiSsid getWifiSsid() {
        return this.mWifiSsid;
    }

    public void setBSSID(String BSSID) {
        this.mBSSID = BSSID;
    }

    public String getBSSID() {
        return this.mBSSID;
    }

    public int getRssi() {
        return this.mRssi;
    }

    public void setRssi(int rssi) {
        if (rssi < -127) {
            rssi = INVALID_RSSI;
        }
        if (rssi > 200) {
            rssi = 200;
        }
        this.mRssi = rssi;
    }

    public int getLinkSpeed() {
        return this.mLinkSpeed;
    }

    public void setLinkSpeed(int linkSpeed) {
        this.mLinkSpeed = linkSpeed;
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

    public boolean is5GHz() {
        return ScanResult.is5GHz(this.mFrequency);
    }

    public void setMacAddress(String macAddress) {
        this.mMacAddress = macAddress;
    }

    public String getMacAddress() {
        return this.mMacAddress;
    }

    public boolean hasRealMacAddress() {
        return this.mMacAddress != null && !DEFAULT_MAC_ADDRESS.equals(this.mMacAddress);
    }

    public void setMeteredHint(boolean meteredHint) {
        this.mMeteredHint = meteredHint;
    }

    public boolean getMeteredHint() {
        return this.mMeteredHint;
    }

    public void setEphemeral(boolean ephemeral) {
        this.mEphemeral = ephemeral;
    }

    public boolean isEphemeral() {
        return this.mEphemeral;
    }

    public void setNetworkId(int id) {
        this.mNetworkId = id;
    }

    public int getNetworkId() {
        return this.mNetworkId;
    }

    public SupplicantState getSupplicantState() {
        return this.mSupplicantState;
    }

    public void setSupplicantState(SupplicantState state) {
        this.mSupplicantState = state;
    }

    public void setInetAddress(InetAddress address) {
        this.mIpAddress = address;
    }

    public int getIpAddress() {
        if (this.mIpAddress instanceof Inet4Address) {
            return NetworkUtils.inetAddressToInt((Inet4Address) this.mIpAddress);
        }
        return 0;
    }

    public boolean getHiddenSSID() {
        if (this.mWifiSsid == null) {
            return false;
        }
        return this.mWifiSsid.isHidden();
    }

    public static NetworkInfo.DetailedState getDetailedStateOf(SupplicantState suppState) {
        return stateMap.get(suppState);
    }

    /* access modifiers changed from: package-private */
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
        String str;
        SupplicantState supplicantState;
        StringBuffer sb = new StringBuffer();
        sb.append("SSID: ");
        sb.append(this.mWifiSsid == null ? WifiSsid.NONE : this.mWifiSsid);
        sb.append(", BSSID: ");
        if (this.mBSSID == null) {
            str = "<none>";
        } else {
            str = ParcelUtil.safeDisplayMac(this.mBSSID);
        }
        sb.append(str);
        sb.append(", Supplicant state: ");
        if (this.mSupplicantState == null) {
            supplicantState = "<none>";
        } else {
            supplicantState = this.mSupplicantState;
        }
        sb.append(supplicantState);
        sb.append(", RSSI: ");
        sb.append(this.mRssi);
        sb.append(", Link speed: ");
        sb.append(this.mLinkSpeed);
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mNetworkId);
        dest.writeInt(this.mlastNetIdForAp);
        dest.writeInt(this.mRssi);
        dest.writeInt(this.mLinkSpeed);
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
        dest.writeInt(this.score);
        dest.writeDouble(this.txSuccessRate);
        dest.writeDouble(this.txRetriesRate);
        dest.writeDouble(this.txBadRate);
        dest.writeDouble(this.rxSuccessRate);
        SupplicantState state = this.mSupplicantState;
        if (state != null) {
            state.writeToParcel(dest, flags);
        }
    }
}
