package android.net.wifi.rtt;

import android.annotation.SystemApi;
import android.net.MacAddress;
import android.net.wifi.ScanResult;
import android.net.wifi.aware.PeerHandle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

@SystemApi
public final class ResponderConfig implements Parcelable {
    private static final int AWARE_BAND_2_DISCOVERY_CHANNEL = 2437;
    public static final int CHANNEL_WIDTH_160MHZ = 3;
    public static final int CHANNEL_WIDTH_20MHZ = 0;
    public static final int CHANNEL_WIDTH_40MHZ = 1;
    public static final int CHANNEL_WIDTH_80MHZ = 2;
    public static final int CHANNEL_WIDTH_80MHZ_PLUS_MHZ = 4;
    public static final Parcelable.Creator<ResponderConfig> CREATOR = new Parcelable.Creator<ResponderConfig>() {
        /* class android.net.wifi.rtt.ResponderConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ResponderConfig[] newArray(int size) {
            return new ResponderConfig[size];
        }

        @Override // android.os.Parcelable.Creator
        public ResponderConfig createFromParcel(Parcel in) {
            PeerHandle peerHandle;
            MacAddress macAddress = null;
            if (in.readBoolean()) {
                macAddress = MacAddress.CREATOR.createFromParcel(in);
            }
            if (in.readBoolean()) {
                peerHandle = new PeerHandle(in.readInt());
            } else {
                peerHandle = null;
            }
            int responderType = in.readInt();
            boolean supports80211mc = in.readInt() == 1;
            int channelWidth = in.readInt();
            int frequency = in.readInt();
            int centerFreq0 = in.readInt();
            int centerFreq1 = in.readInt();
            int preamble = in.readInt();
            if (peerHandle == null) {
                return new ResponderConfig(macAddress, responderType, supports80211mc, channelWidth, frequency, centerFreq0, centerFreq1, preamble);
            }
            return new ResponderConfig(peerHandle, responderType, supports80211mc, channelWidth, frequency, centerFreq0, centerFreq1, preamble);
        }
    };
    public static final int PREAMBLE_HT = 1;
    public static final int PREAMBLE_LEGACY = 0;
    public static final int PREAMBLE_VHT = 2;
    public static final int RESPONDER_AP = 0;
    public static final int RESPONDER_AWARE = 4;
    public static final int RESPONDER_P2P_CLIENT = 3;
    public static final int RESPONDER_P2P_GO = 2;
    public static final int RESPONDER_STA = 1;
    private static final String TAG = "ResponderConfig";
    public final int centerFreq0;
    public final int centerFreq1;
    public final int channelWidth;
    public final int frequency;
    public final MacAddress macAddress;
    public final PeerHandle peerHandle;
    public final int preamble;
    public final int responderType;
    public final boolean supports80211mc;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ChannelWidth {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PreambleType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ResponderType {
    }

    public ResponderConfig(MacAddress macAddress2, int responderType2, boolean supports80211mc2, int channelWidth2, int frequency2, int centerFreq02, int centerFreq12, int preamble2) {
        if (macAddress2 != null) {
            this.macAddress = macAddress2;
            this.peerHandle = null;
            this.responderType = responderType2;
            this.supports80211mc = supports80211mc2;
            this.channelWidth = channelWidth2;
            this.frequency = frequency2;
            this.centerFreq0 = centerFreq02;
            this.centerFreq1 = centerFreq12;
            this.preamble = preamble2;
            return;
        }
        throw new IllegalArgumentException("Invalid ResponderConfig - must specify a MAC address");
    }

    public ResponderConfig(PeerHandle peerHandle2, int responderType2, boolean supports80211mc2, int channelWidth2, int frequency2, int centerFreq02, int centerFreq12, int preamble2) {
        this.macAddress = null;
        this.peerHandle = peerHandle2;
        this.responderType = responderType2;
        this.supports80211mc = supports80211mc2;
        this.channelWidth = channelWidth2;
        this.frequency = frequency2;
        this.centerFreq0 = centerFreq02;
        this.centerFreq1 = centerFreq12;
        this.preamble = preamble2;
    }

    public ResponderConfig(MacAddress macAddress2, PeerHandle peerHandle2, int responderType2, boolean supports80211mc2, int channelWidth2, int frequency2, int centerFreq02, int centerFreq12, int preamble2) {
        this.macAddress = macAddress2;
        this.peerHandle = peerHandle2;
        this.responderType = responderType2;
        this.supports80211mc = supports80211mc2;
        this.channelWidth = channelWidth2;
        this.frequency = frequency2;
        this.centerFreq0 = centerFreq02;
        this.centerFreq1 = centerFreq12;
        this.preamble = preamble2;
    }

    public static ResponderConfig fromScanResult(ScanResult scanResult) {
        int preamble2;
        int preamble3;
        MacAddress macAddress2 = MacAddress.fromString(scanResult.BSSID);
        boolean supports80211mc2 = scanResult.is80211mcResponder();
        int channelWidth2 = translateScanResultChannelWidth(scanResult.channelWidth);
        int frequency2 = scanResult.frequency;
        int centerFreq02 = scanResult.centerFreq0;
        int centerFreq12 = scanResult.centerFreq1;
        if (scanResult.informationElements == null || scanResult.informationElements.length == 0) {
            Log.e(TAG, "Scan Results do not contain IEs - using backup method to select preamble");
            preamble2 = (channelWidth2 == 2 || channelWidth2 == 3) ? 2 : 1;
        } else {
            boolean htCapabilitiesPresent = false;
            boolean vhtCapabilitiesPresent = false;
            ScanResult.InformationElement[] informationElementArr = scanResult.informationElements;
            for (ScanResult.InformationElement ie : informationElementArr) {
                if (ie.id == 45) {
                    htCapabilitiesPresent = true;
                } else if (ie.id == 191) {
                    vhtCapabilitiesPresent = true;
                }
            }
            if (vhtCapabilitiesPresent) {
                preamble3 = 2;
            } else if (htCapabilitiesPresent) {
                preamble3 = 1;
            } else {
                preamble3 = 0;
            }
            preamble2 = preamble3;
        }
        return new ResponderConfig(macAddress2, 0, supports80211mc2, channelWidth2, frequency2, centerFreq02, centerFreq12, preamble2);
    }

    public static ResponderConfig fromWifiAwarePeerMacAddressWithDefaults(MacAddress macAddress2) {
        return new ResponderConfig(macAddress2, 4, true, 0, (int) AWARE_BAND_2_DISCOVERY_CHANNEL, 0, 0, 1);
    }

    public static ResponderConfig fromWifiAwarePeerHandleWithDefaults(PeerHandle peerHandle2) {
        return new ResponderConfig(peerHandle2, 4, true, 0, (int) AWARE_BAND_2_DISCOVERY_CHANNEL, 0, 0, 1);
    }

    public boolean isValid(boolean awareSupported) {
        if ((this.macAddress == null && this.peerHandle == null) || (this.macAddress != null && this.peerHandle != null)) {
            return false;
        }
        if (awareSupported || this.responderType != 4) {
            return true;
        }
        return false;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        if (this.macAddress == null) {
            dest.writeBoolean(false);
        } else {
            dest.writeBoolean(true);
            this.macAddress.writeToParcel(dest, flags);
        }
        if (this.peerHandle == null) {
            dest.writeBoolean(false);
        } else {
            dest.writeBoolean(true);
            dest.writeInt(this.peerHandle.peerId);
        }
        dest.writeInt(this.responderType);
        dest.writeInt(this.supports80211mc ? 1 : 0);
        dest.writeInt(this.channelWidth);
        dest.writeInt(this.frequency);
        dest.writeInt(this.centerFreq0);
        dest.writeInt(this.centerFreq1);
        dest.writeInt(this.preamble);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResponderConfig)) {
            return false;
        }
        ResponderConfig lhs = (ResponderConfig) o;
        if (Objects.equals(this.macAddress, lhs.macAddress) && Objects.equals(this.peerHandle, lhs.peerHandle) && this.responderType == lhs.responderType && this.supports80211mc == lhs.supports80211mc && this.channelWidth == lhs.channelWidth && this.frequency == lhs.frequency && this.centerFreq0 == lhs.centerFreq0 && this.centerFreq1 == lhs.centerFreq1 && this.preamble == lhs.preamble) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.macAddress, this.peerHandle, Integer.valueOf(this.responderType), Boolean.valueOf(this.supports80211mc), Integer.valueOf(this.channelWidth), Integer.valueOf(this.frequency), Integer.valueOf(this.centerFreq0), Integer.valueOf(this.centerFreq1), Integer.valueOf(this.preamble));
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer("ResponderConfig: macAddress=");
        stringBuffer.append(this.macAddress);
        stringBuffer.append(", peerHandle=");
        PeerHandle peerHandle2 = this.peerHandle;
        stringBuffer.append(peerHandle2 == null ? "<null>" : Integer.valueOf(peerHandle2.peerId));
        stringBuffer.append(", responderType=");
        stringBuffer.append(this.responderType);
        stringBuffer.append(", supports80211mc=");
        stringBuffer.append(this.supports80211mc);
        stringBuffer.append(", channelWidth=");
        stringBuffer.append(this.channelWidth);
        stringBuffer.append(", frequency=");
        stringBuffer.append(this.frequency);
        stringBuffer.append(", centerFreq0=");
        stringBuffer.append(this.centerFreq0);
        stringBuffer.append(", centerFreq1=");
        stringBuffer.append(this.centerFreq1);
        stringBuffer.append(", preamble=");
        stringBuffer.append(this.preamble);
        return stringBuffer.toString();
    }

    static int translateScanResultChannelWidth(int scanResultChannelWidth) {
        if (scanResultChannelWidth == 0) {
            return 0;
        }
        if (scanResultChannelWidth == 1) {
            return 1;
        }
        if (scanResultChannelWidth == 2) {
            return 2;
        }
        if (scanResultChannelWidth == 3) {
            return 3;
        }
        if (scanResultChannelWidth == 4) {
            return 4;
        }
        throw new IllegalArgumentException("translateScanResultChannelWidth: bad " + scanResultChannelWidth);
    }
}
