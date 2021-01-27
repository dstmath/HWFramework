package ohos.wifi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import ohos.annotation.SystemApi;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.ParcelException;
import ohos.utils.Sequenceable;

public final class WifiScanInfo implements Sequenceable {
    private static final long FLAG_80211MC_RESPONDER = 2;
    private static final long FLAG_PASSPOINT_NETWORK = 1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, InnerUtils.LOG_ID_WIFI, "WifiScanInfo");
    private int band;
    private String bssid;
    private String capabilities;
    private int channelWidth;
    private long features;
    private Map<Integer, byte[]> informationElements;
    private boolean isHilink;
    private int rssi;
    private int securityType;
    private String ssid;
    private long timestamp;

    public WifiScanInfo(String str, String str2, String str3, int i, int i2, int i3, int i4, long j) {
        this.ssid = str;
        this.bssid = str2;
        this.capabilities = str3;
        this.securityType = i;
        this.rssi = i2;
        this.band = i3;
        this.channelWidth = i4;
        this.timestamp = j;
        this.informationElements = new HashMap();
        this.features = 0;
        this.isHilink = false;
    }

    public WifiScanInfo() {
        this(null, null, null, 0, 0, 0, 0, 0);
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:74:0x00a2 */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:75:0x00d9 */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:77:0x00a2 */
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
    /* JADX WARN: Type inference failed for: r0v13 */
    /* JADX WARN: Type inference failed for: r0v14 */
    /* JADX WARN: Type inference failed for: r0v15 */
    /* JADX WARN: Type inference failed for: r0v16 */
    /* JADX WARN: Type inference failed for: r0v17 */
    /* JADX WARN: Type inference failed for: r0v21 */
    /* JADX WARN: Type inference failed for: r0v22 */
    /* JADX WARN: Type inference failed for: r0v27 */
    /* JADX WARN: Type inference failed for: r0v28 */
    /* JADX WARN: Type inference failed for: r0v32 */
    /* JADX WARN: Type inference failed for: r0v33 */
    /* JADX WARN: Type inference failed for: r0v36 */
    /* JADX WARN: Type inference failed for: r0v37 */
    /* JADX WARN: Type inference failed for: r0v39 */
    /* JADX WARN: Type inference failed for: r0v40 */
    /* JADX WARN: Type inference failed for: r0v42 */
    /* JADX WARN: Type inference failed for: r0v43 */
    /* JADX WARN: Type inference failed for: r0v46 */
    /* JADX WARN: Type inference failed for: r0v47 */
    /* JADX WARN: Type inference failed for: r0v50 */
    /* JADX WARN: Type inference failed for: r0v51 */
    /* JADX WARN: Type inference failed for: r0v54 */
    /* JADX WARN: Type inference failed for: r0v55 */
    /* JADX WARN: Type inference failed for: r0v58 */
    /* JADX WARN: Type inference failed for: r0v59 */
    /* JADX WARN: Type inference failed for: r0v62 */
    /* JADX WARN: Type inference failed for: r0v63 */
    /* JADX WARN: Type inference failed for: r0v66 */
    /* JADX WARNING: Unknown variable types count: 10 */
    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        ?? r0 = ((((((((parcel.writeString(this.ssid) && parcel.writeString(this.bssid)) != false && parcel.writeString(this.capabilities)) != false && parcel.writeInt(this.securityType)) != false && parcel.writeInt(this.rssi)) != false && parcel.writeInt(this.band)) != false && parcel.writeInt(this.channelWidth)) != false && parcel.writeLong(this.timestamp)) != false && parcel.writeLong(this.features)) == true && parcel.writeInt(this.isHilink ? 1 : 0);
        Map<Integer, byte[]> map = this.informationElements;
        if (map == null || map.size() <= 0) {
            parcel.writeInt(-1);
        } else {
            r0 = r0 == true && parcel.writeInt(this.informationElements.size());
            for (Map.Entry<Integer, byte[]> entry : this.informationElements.entrySet()) {
                r0 = (r0 != false && parcel.writeInt(entry.getKey().intValue())) == true && parcel.writeByteArray(entry.getValue());
            }
        }
        if (r0 == true) {
            return true;
        }
        parcel.reclaim();
        HiLog.warn(LABEL, "WifiScanInfo marshalling failed, reclaim", new Object[0]);
        return false;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.ssid = parcel.readString();
        this.bssid = parcel.readString();
        this.capabilities = parcel.readString();
        this.securityType = parcel.readInt();
        this.rssi = parcel.readInt();
        this.band = parcel.readInt();
        this.channelWidth = parcel.readInt();
        this.timestamp = parcel.readLong();
        this.features = parcel.readLong();
        this.isHilink = parcel.readInt() == 1;
        int readInt = parcel.readInt();
        if (readInt > 0) {
            for (int i = 0; i < readInt; i++) {
                try {
                    this.informationElements.put(Integer.valueOf(parcel.readInt()), parcel.readByteArray());
                } catch (ParcelException unused) {
                    HiLog.warn(LABEL, "Read informationElements bytes error", new Object[0]);
                }
            }
        } else {
            this.informationElements.clear();
            HiLog.warn(LABEL, "Read informationElements length is invalied!", new Object[0]);
        }
        return true;
    }

    public String getSsid() {
        return this.ssid;
    }

    public String getBssid() {
        return this.bssid;
    }

    public String getCapabilities() {
        return this.capabilities;
    }

    public int getSecurityType() {
        return this.securityType;
    }

    public int getRssi() {
        return this.rssi;
    }

    public int getBand() {
        return this.band;
    }

    public int getBandWidth() {
        return this.channelWidth;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public Map<Integer, byte[]> getInformationElements() {
        return this.informationElements;
    }

    @SystemApi
    public boolean is80211mcResponder() {
        return (this.features & 2) != 0;
    }

    @SystemApi
    public boolean isPasspointNetwork() {
        return (this.features & 1) != 0;
    }

    @SystemApi
    public boolean isHiLinkNetwork() {
        return this.isHilink;
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("SSID: ");
        String str = this.ssid;
        String str2 = "<none>";
        stringBuffer.append(str == null ? str2 : InnerUtils.safeDisplaySsid(str));
        stringBuffer.append(", BSSID: ");
        String str3 = this.bssid;
        if (str3 != null) {
            str2 = InnerUtils.safeDisplayBssid(str3);
        }
        stringBuffer.append(str2);
        stringBuffer.append(", Capabilities: ");
        stringBuffer.append(this.capabilities);
        stringBuffer.append(", SecurityType: ");
        stringBuffer.append(this.securityType);
        stringBuffer.append(", RSSI: ");
        stringBuffer.append(this.rssi);
        stringBuffer.append(", Band: ");
        stringBuffer.append(this.band);
        stringBuffer.append(", ChannelWidth: ");
        stringBuffer.append(this.channelWidth);
        stringBuffer.append(", Timestamp: ");
        stringBuffer.append(this.timestamp);
        stringBuffer.append(", features: ");
        stringBuffer.append(this.features);
        stringBuffer.append(", isHilink: ");
        stringBuffer.append(this.isHilink);
        int i = 0;
        for (Map.Entry<Integer, byte[]> entry : this.informationElements.entrySet()) {
            stringBuffer.append(", id: ");
            stringBuffer.append(entry.getKey());
            stringBuffer.append(", informations: ");
            stringBuffer.append(Arrays.toString(entry.getValue()));
            i++;
            if (i > 5) {
                break;
            }
        }
        return stringBuffer.toString();
    }
}
