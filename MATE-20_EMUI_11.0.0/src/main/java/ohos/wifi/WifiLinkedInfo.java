package ohos.wifi;

import java.util.EnumMap;
import java.util.Locale;
import ohos.annotation.SystemApi;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class WifiLinkedInfo implements Sequenceable {
    private static final EnumMap<SuppState, ConnState> CONVERT_MAP = new EnumMap<>(SuppState.class);
    private int band;
    private String bssid;
    private int chload;
    private int frequency;
    private int ipAddress;
    private boolean isDataRestricted;
    private boolean isHidden;
    private int linkSpeed;
    private String macAddress;
    private int networkId;
    private int rssi;
    private int rxLinkSpeed;
    private int snr;
    private String ssid;
    private String suppState;
    private int txLinkSpeeds;

    public enum ConnState {
        SCANNING,
        CONNECTING,
        AUTHENTICATING,
        OBTAINING_IPADDR,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED,
        UNKNOWN
    }

    @SystemApi
    public enum SuppState {
        DISCONNECTED,
        INTERFACE_DISABLED,
        INACTIVE,
        SCANNING,
        AUTHENTICATING,
        ASSOCIATING,
        ASSOCIATED,
        FOUR_WAY_HANDSHAKE,
        GROUP_HANDSHAKE,
        COMPLETED,
        UNINITIALIZED,
        INVALID
    }

    static {
        CONVERT_MAP.put((EnumMap<SuppState, ConnState>) SuppState.DISCONNECTED, (SuppState) ConnState.DISCONNECTED);
        CONVERT_MAP.put((EnumMap<SuppState, ConnState>) SuppState.INTERFACE_DISABLED, (SuppState) ConnState.DISCONNECTED);
        CONVERT_MAP.put((EnumMap<SuppState, ConnState>) SuppState.INACTIVE, (SuppState) ConnState.DISCONNECTED);
        CONVERT_MAP.put((EnumMap<SuppState, ConnState>) SuppState.SCANNING, (SuppState) ConnState.SCANNING);
        CONVERT_MAP.put((EnumMap<SuppState, ConnState>) SuppState.AUTHENTICATING, (SuppState) ConnState.CONNECTING);
        CONVERT_MAP.put((EnumMap<SuppState, ConnState>) SuppState.ASSOCIATING, (SuppState) ConnState.CONNECTING);
        CONVERT_MAP.put((EnumMap<SuppState, ConnState>) SuppState.ASSOCIATED, (SuppState) ConnState.CONNECTING);
        CONVERT_MAP.put((EnumMap<SuppState, ConnState>) SuppState.FOUR_WAY_HANDSHAKE, (SuppState) ConnState.AUTHENTICATING);
        CONVERT_MAP.put((EnumMap<SuppState, ConnState>) SuppState.GROUP_HANDSHAKE, (SuppState) ConnState.AUTHENTICATING);
        CONVERT_MAP.put((EnumMap<SuppState, ConnState>) SuppState.COMPLETED, (SuppState) ConnState.OBTAINING_IPADDR);
        CONVERT_MAP.put((EnumMap<SuppState, ConnState>) SuppState.UNINITIALIZED, (SuppState) ConnState.DISCONNECTED);
        CONVERT_MAP.put((EnumMap<SuppState, ConnState>) SuppState.INVALID, (SuppState) ConnState.UNKNOWN);
    }

    public WifiLinkedInfo(int i, String str, String str2, int i2, int i3, int i4, int i5, String str3) {
        this.networkId = i;
        this.ssid = str;
        this.bssid = str2;
        this.rssi = i2;
        this.band = i3;
        this.linkSpeed = i4;
        this.frequency = i5;
        this.macAddress = str3;
    }

    public WifiLinkedInfo() {
        this(0, null, null, 0, 0, 0, 0, null);
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
    /* JADX WARN: Type inference failed for: r3v1 */
    /* JADX WARN: Type inference failed for: r3v2 */
    /* JADX WARN: Type inference failed for: r3v5 */
    /* JADX WARN: Type inference failed for: r0v16 */
    /* JADX WARN: Type inference failed for: r0v19 */
    /* JADX WARN: Type inference failed for: r0v20 */
    /* JADX WARN: Type inference failed for: r0v23 */
    /* JADX WARN: Type inference failed for: r0v24 */
    /* JADX WARN: Type inference failed for: r0v27 */
    /* JADX WARN: Type inference failed for: r0v28 */
    /* JADX WARN: Type inference failed for: r0v31 */
    /* JADX WARN: Type inference failed for: r0v32 */
    /* JADX WARN: Type inference failed for: r0v35 */
    /* JADX WARN: Type inference failed for: r0v36 */
    /* JADX WARN: Type inference failed for: r0v39 */
    /* JADX WARN: Type inference failed for: r0v40 */
    /* JADX WARN: Type inference failed for: r0v43 */
    /* JADX WARN: Type inference failed for: r0v44 */
    /* JADX WARN: Type inference failed for: r0v47 */
    /* JADX WARN: Type inference failed for: r0v48 */
    /* JADX WARN: Type inference failed for: r0v51 */
    /* JADX WARN: Type inference failed for: r0v52 */
    /* JADX WARN: Type inference failed for: r0v55 */
    /* JADX WARN: Type inference failed for: r0v56 */
    /* JADX WARN: Type inference failed for: r0v59 */
    /* JADX WARN: Type inference failed for: r0v60 */
    /* JADX WARN: Type inference failed for: r0v63 */
    /* JADX WARN: Type inference failed for: r0v64 */
    /* JADX WARN: Type inference failed for: r0v67 */
    /* JADX WARN: Type inference failed for: r0v68 */
    /* JADX WARN: Type inference failed for: r0v71 */
    /* JADX WARNING: Unknown variable types count: 15 */
    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if ((((((((((((((((parcel.writeInt(this.networkId) && parcel.writeString(this.ssid)) != false && parcel.writeString(this.bssid)) != false && parcel.writeInt(this.rssi)) != false && parcel.writeInt(this.band)) != false && parcel.writeInt(this.linkSpeed)) != false && parcel.writeInt(this.frequency)) != false && parcel.writeString(this.macAddress)) != false && parcel.writeInt(this.ipAddress)) != false && parcel.writeString(this.suppState)) != false && parcel.writeInt(this.isHidden ? 1 : 0)) != false && parcel.writeInt(this.isDataRestricted ? 1 : 0)) != false && parcel.writeInt(this.rxLinkSpeed)) != false && parcel.writeInt(this.txLinkSpeeds)) != false && parcel.writeInt(this.chload)) == true && parcel.writeInt(this.snr)) != false) {
            return true;
        }
        parcel.reclaim();
        return false;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.networkId = parcel.readInt();
        this.ssid = parcel.readString();
        this.bssid = parcel.readString();
        this.rssi = parcel.readInt();
        this.band = parcel.readInt();
        this.linkSpeed = parcel.readInt();
        this.frequency = parcel.readInt();
        this.macAddress = parcel.readString();
        this.ipAddress = parcel.readInt();
        this.suppState = parcel.readString();
        boolean z = false;
        this.isHidden = parcel.readInt() != 0;
        if (parcel.readInt() != 0) {
            z = true;
        }
        this.isDataRestricted = z;
        this.rxLinkSpeed = parcel.readInt();
        this.txLinkSpeeds = parcel.readInt();
        this.chload = parcel.readInt();
        this.snr = parcel.readInt();
        return true;
    }

    @SystemApi
    public int getNetworkId() {
        return this.networkId;
    }

    public String getBssid() {
        return this.bssid;
    }

    public int getRssi() {
        return this.rssi;
    }

    public int getFrequency() {
        return this.frequency;
    }

    public String getSsid() {
        return this.ssid;
    }

    public int getLinkSpeed() {
        return this.linkSpeed;
    }

    public int getBand() {
        return this.band;
    }

    public String getMacAddress() {
        return this.macAddress;
    }

    public int getIpAddress() {
        return this.ipAddress;
    }

    @SystemApi
    public int getChload() {
        return this.chload;
    }

    @SystemApi
    public int getSnr() {
        return this.snr;
    }

    @SystemApi
    public SuppState getSuppState() {
        String str = this.suppState;
        if (str == null) {
            return SuppState.INVALID;
        }
        return SuppState.valueOf(str.toUpperCase(Locale.ROOT));
    }

    public ConnState getConnState() {
        return CONVERT_MAP.get(getSuppState());
    }

    public boolean isHiddenSsid() {
        return this.isHidden;
    }

    public boolean isRestricted() {
        return this.isDataRestricted;
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("SSID: ");
        String str = "<none>";
        stringBuffer.append(this.ssid == null ? str : "<private>");
        stringBuffer.append(", BSSID: ");
        String str2 = this.bssid;
        if (str2 != null) {
            str = InnerUtils.safeDisplayBssid(str2);
        }
        stringBuffer.append(str);
        stringBuffer.append(", RSSI: ");
        stringBuffer.append(this.rssi);
        stringBuffer.append(", Link speed: ");
        stringBuffer.append(this.linkSpeed);
        stringBuffer.append(", Frequency: ");
        stringBuffer.append(this.frequency);
        stringBuffer.append(", Net ID: ");
        stringBuffer.append(this.networkId);
        stringBuffer.append(", Mac address: ");
        stringBuffer.append(InnerUtils.safeDisplayBssid(this.macAddress));
        stringBuffer.append(", SuppState: ");
        stringBuffer.append(this.suppState);
        stringBuffer.append(", IsHidden: ");
        stringBuffer.append(this.isHidden);
        stringBuffer.append(", IsDataRestricted: ");
        stringBuffer.append(this.isDataRestricted);
        stringBuffer.append(", RxLinkSpeedMbps: ");
        stringBuffer.append(this.rxLinkSpeed);
        stringBuffer.append(", TxLinkSpeedMbps: ");
        stringBuffer.append(this.txLinkSpeeds);
        stringBuffer.append(", Chload: ");
        stringBuffer.append(this.chload);
        stringBuffer.append(", Snr: ");
        stringBuffer.append(this.snr);
        return stringBuffer.toString();
    }
}
