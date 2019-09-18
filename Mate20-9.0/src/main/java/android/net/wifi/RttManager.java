package android.net.wifi;

import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.content.Context;
import android.net.wifi.rtt.RangingRequest;
import android.net.wifi.rtt.RangingResult;
import android.net.wifi.rtt.RangingResultCallback;
import android.net.wifi.rtt.WifiRttManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import java.util.List;

@SystemApi
@Deprecated
public class RttManager {
    public static final int BASE = 160256;
    public static final int CMD_OP_ABORTED = 160260;
    public static final int CMD_OP_DISABLE_RESPONDER = 160262;
    public static final int CMD_OP_ENABLE_RESPONDER = 160261;
    public static final int CMD_OP_ENALBE_RESPONDER_FAILED = 160264;
    public static final int CMD_OP_ENALBE_RESPONDER_SUCCEEDED = 160263;
    public static final int CMD_OP_FAILED = 160258;
    public static final int CMD_OP_REG_BINDER = 160265;
    public static final int CMD_OP_START_RANGING = 160256;
    public static final int CMD_OP_STOP_RANGING = 160257;
    public static final int CMD_OP_SUCCEEDED = 160259;
    private static final boolean DBG = false;
    public static final String DESCRIPTION_KEY = "android.net.wifi.RttManager.Description";
    public static final int PREAMBLE_HT = 2;
    public static final int PREAMBLE_LEGACY = 1;
    public static final int PREAMBLE_VHT = 4;
    public static final int REASON_INITIATOR_NOT_ALLOWED_WHEN_RESPONDER_ON = -6;
    public static final int REASON_INVALID_LISTENER = -3;
    public static final int REASON_INVALID_REQUEST = -4;
    public static final int REASON_NOT_AVAILABLE = -2;
    public static final int REASON_PERMISSION_DENIED = -5;
    public static final int REASON_UNSPECIFIED = -1;
    public static final int RTT_BW_10_SUPPORT = 2;
    public static final int RTT_BW_160_SUPPORT = 32;
    public static final int RTT_BW_20_SUPPORT = 4;
    public static final int RTT_BW_40_SUPPORT = 8;
    public static final int RTT_BW_5_SUPPORT = 1;
    public static final int RTT_BW_80_SUPPORT = 16;
    @Deprecated
    public static final int RTT_CHANNEL_WIDTH_10 = 6;
    @Deprecated
    public static final int RTT_CHANNEL_WIDTH_160 = 3;
    @Deprecated
    public static final int RTT_CHANNEL_WIDTH_20 = 0;
    @Deprecated
    public static final int RTT_CHANNEL_WIDTH_40 = 1;
    @Deprecated
    public static final int RTT_CHANNEL_WIDTH_5 = 5;
    @Deprecated
    public static final int RTT_CHANNEL_WIDTH_80 = 2;
    @Deprecated
    public static final int RTT_CHANNEL_WIDTH_80P80 = 4;
    @Deprecated
    public static final int RTT_CHANNEL_WIDTH_UNSPECIFIED = -1;
    public static final int RTT_PEER_NAN = 5;
    public static final int RTT_PEER_P2P_CLIENT = 4;
    public static final int RTT_PEER_P2P_GO = 3;
    public static final int RTT_PEER_TYPE_AP = 1;
    public static final int RTT_PEER_TYPE_STA = 2;
    @Deprecated
    public static final int RTT_PEER_TYPE_UNSPECIFIED = 0;
    public static final int RTT_STATUS_ABORTED = 8;
    public static final int RTT_STATUS_FAILURE = 1;
    public static final int RTT_STATUS_FAIL_AP_ON_DIFF_CHANNEL = 6;
    public static final int RTT_STATUS_FAIL_BUSY_TRY_LATER = 12;
    public static final int RTT_STATUS_FAIL_FTM_PARAM_OVERRIDE = 15;
    public static final int RTT_STATUS_FAIL_INVALID_TS = 9;
    public static final int RTT_STATUS_FAIL_NOT_SCHEDULED_YET = 4;
    public static final int RTT_STATUS_FAIL_NO_CAPABILITY = 7;
    public static final int RTT_STATUS_FAIL_NO_RSP = 2;
    public static final int RTT_STATUS_FAIL_PROTOCOL = 10;
    public static final int RTT_STATUS_FAIL_REJECTED = 3;
    public static final int RTT_STATUS_FAIL_SCHEDULE = 11;
    public static final int RTT_STATUS_FAIL_TM_TIMEOUT = 5;
    public static final int RTT_STATUS_INVALID_REQ = 13;
    public static final int RTT_STATUS_NO_WIFI = 14;
    public static final int RTT_STATUS_SUCCESS = 0;
    @Deprecated
    public static final int RTT_TYPE_11_MC = 4;
    @Deprecated
    public static final int RTT_TYPE_11_V = 2;
    public static final int RTT_TYPE_ONE_SIDED = 1;
    public static final int RTT_TYPE_TWO_SIDED = 2;
    @Deprecated
    public static final int RTT_TYPE_UNSPECIFIED = 0;
    private static final String TAG = "RttManager";
    private final Context mContext;
    private final WifiRttManager mNewService;
    private RttCapabilities mRttCapabilities = new RttCapabilities();

    @Deprecated
    public class Capabilities {
        public int supportedPeerType;
        public int supportedType;

        public Capabilities() {
        }
    }

    @Deprecated
    public static class ParcelableRttParams implements Parcelable {
        public static final Parcelable.Creator<ParcelableRttParams> CREATOR = new Parcelable.Creator<ParcelableRttParams>() {
            public ParcelableRttParams createFromParcel(Parcel in) {
                int num = in.readInt();
                RttParams[] params = new RttParams[num];
                for (int i = 0; i < num; i++) {
                    params[i] = new RttParams();
                    params[i].deviceType = in.readInt();
                    params[i].requestType = in.readInt();
                    boolean z = true;
                    params[i].secure = in.readByte() != 0;
                    params[i].bssid = in.readString();
                    params[i].channelWidth = in.readInt();
                    params[i].frequency = in.readInt();
                    params[i].centerFreq0 = in.readInt();
                    params[i].centerFreq1 = in.readInt();
                    params[i].numberBurst = in.readInt();
                    params[i].interval = in.readInt();
                    params[i].numSamplesPerBurst = in.readInt();
                    params[i].numRetriesPerMeasurementFrame = in.readInt();
                    params[i].numRetriesPerFTMR = in.readInt();
                    params[i].LCIRequest = in.readInt() == 1;
                    RttParams rttParams = params[i];
                    if (in.readInt() != 1) {
                        z = false;
                    }
                    rttParams.LCRRequest = z;
                    params[i].burstTimeout = in.readInt();
                    params[i].preamble = in.readInt();
                    params[i].bandwidth = in.readInt();
                }
                return new ParcelableRttParams(params);
            }

            public ParcelableRttParams[] newArray(int size) {
                return new ParcelableRttParams[size];
            }
        };
        public RttParams[] mParams;

        @VisibleForTesting
        public ParcelableRttParams(RttParams[] params) {
            this.mParams = params == null ? new RttParams[0] : params;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mParams.length);
            for (RttParams params : this.mParams) {
                dest.writeInt(params.deviceType);
                dest.writeInt(params.requestType);
                dest.writeByte(params.secure ? (byte) 1 : 0);
                dest.writeString(params.bssid);
                dest.writeInt(params.channelWidth);
                dest.writeInt(params.frequency);
                dest.writeInt(params.centerFreq0);
                dest.writeInt(params.centerFreq1);
                dest.writeInt(params.numberBurst);
                dest.writeInt(params.interval);
                dest.writeInt(params.numSamplesPerBurst);
                dest.writeInt(params.numRetriesPerMeasurementFrame);
                dest.writeInt(params.numRetriesPerFTMR);
                dest.writeInt(params.LCIRequest ? 1 : 0);
                dest.writeInt(params.LCRRequest ? 1 : 0);
                dest.writeInt(params.burstTimeout);
                dest.writeInt(params.preamble);
                dest.writeInt(params.bandwidth);
            }
        }
    }

    @Deprecated
    public static class ParcelableRttResults implements Parcelable {
        public static final Parcelable.Creator<ParcelableRttResults> CREATOR = new Parcelable.Creator<ParcelableRttResults>() {
            public ParcelableRttResults createFromParcel(Parcel in) {
                int num = in.readInt();
                if (num == 0) {
                    return new ParcelableRttResults(null);
                }
                RttResult[] results = new RttResult[num];
                for (int i = 0; i < num; i++) {
                    results[i] = new RttResult();
                    results[i].bssid = in.readString();
                    results[i].burstNumber = in.readInt();
                    results[i].measurementFrameNumber = in.readInt();
                    results[i].successMeasurementFrameNumber = in.readInt();
                    results[i].frameNumberPerBurstPeer = in.readInt();
                    results[i].status = in.readInt();
                    results[i].measurementType = in.readInt();
                    results[i].retryAfterDuration = in.readInt();
                    results[i].ts = in.readLong();
                    results[i].rssi = in.readInt();
                    results[i].rssiSpread = in.readInt();
                    results[i].txRate = in.readInt();
                    results[i].rtt = in.readLong();
                    results[i].rttStandardDeviation = in.readLong();
                    results[i].rttSpread = in.readLong();
                    results[i].distance = in.readInt();
                    results[i].distanceStandardDeviation = in.readInt();
                    results[i].distanceSpread = in.readInt();
                    results[i].burstDuration = in.readInt();
                    results[i].negotiatedBurstNum = in.readInt();
                    results[i].LCI = new WifiInformationElement();
                    results[i].LCI.id = in.readByte();
                    if (results[i].LCI.id != -1) {
                        results[i].LCI.data = new byte[in.readByte()];
                        in.readByteArray(results[i].LCI.data);
                    }
                    results[i].LCR = new WifiInformationElement();
                    results[i].LCR.id = in.readByte();
                    if (results[i].LCR.id != -1) {
                        results[i].LCR.data = new byte[in.readByte()];
                        in.readByteArray(results[i].LCR.data);
                    }
                    results[i].secure = in.readByte() != 0;
                }
                return new ParcelableRttResults(results);
            }

            public ParcelableRttResults[] newArray(int size) {
                return new ParcelableRttResults[size];
            }
        };
        public RttResult[] mResults;

        public ParcelableRttResults(RttResult[] results) {
            this.mResults = results;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (RttResult rttResult : this.mResults) {
                sb.append("[" + i + "]: ");
                StringBuilder sb2 = new StringBuilder();
                sb2.append("bssid=");
                sb2.append(rttResult.bssid);
                sb.append(sb2.toString());
                sb.append(", burstNumber=" + this.mResults[i].burstNumber);
                sb.append(", measurementFrameNumber=" + this.mResults[i].measurementFrameNumber);
                sb.append(", successMeasurementFrameNumber=" + this.mResults[i].successMeasurementFrameNumber);
                sb.append(", frameNumberPerBurstPeer=" + this.mResults[i].frameNumberPerBurstPeer);
                sb.append(", status=" + this.mResults[i].status);
                sb.append(", requestType=" + this.mResults[i].requestType);
                sb.append(", measurementType=" + this.mResults[i].measurementType);
                sb.append(", retryAfterDuration=" + this.mResults[i].retryAfterDuration);
                sb.append(", ts=" + this.mResults[i].ts);
                sb.append(", rssi=" + this.mResults[i].rssi);
                sb.append(", rssi_spread=" + this.mResults[i].rssi_spread);
                sb.append(", rssiSpread=" + this.mResults[i].rssiSpread);
                sb.append(", tx_rate=" + this.mResults[i].tx_rate);
                sb.append(", txRate=" + this.mResults[i].txRate);
                sb.append(", rxRate=" + this.mResults[i].rxRate);
                sb.append(", rtt_ns=" + this.mResults[i].rtt_ns);
                sb.append(", rtt=" + this.mResults[i].rtt);
                sb.append(", rtt_sd_ns=" + this.mResults[i].rtt_sd_ns);
                sb.append(", rttStandardDeviation=" + this.mResults[i].rttStandardDeviation);
                sb.append(", rtt_spread_ns=" + this.mResults[i].rtt_spread_ns);
                sb.append(", rttSpread=" + this.mResults[i].rttSpread);
                sb.append(", distance_cm=" + this.mResults[i].distance_cm);
                sb.append(", distance=" + this.mResults[i].distance);
                sb.append(", distance_sd_cm=" + this.mResults[i].distance_sd_cm);
                sb.append(", distanceStandardDeviation=" + this.mResults[i].distanceStandardDeviation);
                sb.append(", distance_spread_cm=" + this.mResults[i].distance_spread_cm);
                sb.append(", distanceSpread=" + this.mResults[i].distanceSpread);
                sb.append(", burstDuration=" + this.mResults[i].burstDuration);
                sb.append(", negotiatedBurstNum=" + this.mResults[i].negotiatedBurstNum);
                sb.append(", LCI=" + this.mResults[i].LCI);
                sb.append(", LCR=" + this.mResults[i].LCR);
                sb.append(", secure=" + this.mResults[i].secure);
            }
            return sb.toString();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            if (this.mResults != null) {
                dest.writeInt(this.mResults.length);
                for (RttResult result : this.mResults) {
                    dest.writeString(result.bssid);
                    dest.writeInt(result.burstNumber);
                    dest.writeInt(result.measurementFrameNumber);
                    dest.writeInt(result.successMeasurementFrameNumber);
                    dest.writeInt(result.frameNumberPerBurstPeer);
                    dest.writeInt(result.status);
                    dest.writeInt(result.measurementType);
                    dest.writeInt(result.retryAfterDuration);
                    dest.writeLong(result.ts);
                    dest.writeInt(result.rssi);
                    dest.writeInt(result.rssiSpread);
                    dest.writeInt(result.txRate);
                    dest.writeLong(result.rtt);
                    dest.writeLong(result.rttStandardDeviation);
                    dest.writeLong(result.rttSpread);
                    dest.writeInt(result.distance);
                    dest.writeInt(result.distanceStandardDeviation);
                    dest.writeInt(result.distanceSpread);
                    dest.writeInt(result.burstDuration);
                    dest.writeInt(result.negotiatedBurstNum);
                    dest.writeByte(result.LCI.id);
                    if (result.LCI.id != -1) {
                        dest.writeByte((byte) result.LCI.data.length);
                        dest.writeByteArray(result.LCI.data);
                    }
                    dest.writeByte(result.LCR.id);
                    if (result.LCR.id != -1) {
                        dest.writeByte((byte) result.LCR.data.length);
                        dest.writeByteArray(result.LCR.data);
                    }
                    dest.writeByte(result.secure ? (byte) 1 : 0);
                }
                return;
            }
            dest.writeInt(0);
        }
    }

    @Deprecated
    public static abstract class ResponderCallback {
        public abstract void onResponderEnableFailure(int i);

        public abstract void onResponderEnabled(ResponderConfig responderConfig);
    }

    @Deprecated
    public static class ResponderConfig implements Parcelable {
        public static final Parcelable.Creator<ResponderConfig> CREATOR = new Parcelable.Creator<ResponderConfig>() {
            public ResponderConfig createFromParcel(Parcel in) {
                ResponderConfig config = new ResponderConfig();
                config.macAddress = in.readString();
                config.frequency = in.readInt();
                config.centerFreq0 = in.readInt();
                config.centerFreq1 = in.readInt();
                config.channelWidth = in.readInt();
                config.preamble = in.readInt();
                return config;
            }

            public ResponderConfig[] newArray(int size) {
                return new ResponderConfig[size];
            }
        };
        public int centerFreq0;
        public int centerFreq1;
        public int channelWidth;
        public int frequency;
        public String macAddress = "";
        public int preamble;

        public String toString() {
            return "macAddress = " + this.macAddress + " frequency = " + this.frequency + " centerFreq0 = " + this.centerFreq0 + " centerFreq1 = " + this.centerFreq1 + " channelWidth = " + this.channelWidth + " preamble = " + this.preamble;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.macAddress);
            dest.writeInt(this.frequency);
            dest.writeInt(this.centerFreq0);
            dest.writeInt(this.centerFreq1);
            dest.writeInt(this.channelWidth);
            dest.writeInt(this.preamble);
        }
    }

    @Deprecated
    public static class RttCapabilities implements Parcelable {
        public static final Parcelable.Creator<RttCapabilities> CREATOR = new Parcelable.Creator<RttCapabilities>() {
            public RttCapabilities createFromParcel(Parcel in) {
                RttCapabilities capabilities = new RttCapabilities();
                boolean z = false;
                capabilities.oneSidedRttSupported = in.readInt() == 1;
                capabilities.twoSided11McRttSupported = in.readInt() == 1;
                capabilities.lciSupported = in.readInt() == 1;
                capabilities.lcrSupported = in.readInt() == 1;
                capabilities.preambleSupported = in.readInt();
                capabilities.bwSupported = in.readInt();
                capabilities.responderSupported = in.readInt() == 1;
                if (in.readInt() == 1) {
                    z = true;
                }
                capabilities.secureRttSupported = z;
                capabilities.mcVersion = in.readInt();
                return capabilities;
            }

            public RttCapabilities[] newArray(int size) {
                return new RttCapabilities[size];
            }
        };
        public int bwSupported;
        public boolean lciSupported;
        public boolean lcrSupported;
        public int mcVersion;
        public boolean oneSidedRttSupported;
        public int preambleSupported;
        public boolean responderSupported;
        public boolean secureRttSupported;
        @Deprecated
        public boolean supportedPeerType;
        @Deprecated
        public boolean supportedType;
        public boolean twoSided11McRttSupported;

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("oneSidedRtt ");
            sb.append(this.oneSidedRttSupported ? "is Supported. " : "is not supported. ");
            sb.append("twoSided11McRtt ");
            sb.append(this.twoSided11McRttSupported ? "is Supported. " : "is not supported. ");
            sb.append("lci ");
            sb.append(this.lciSupported ? "is Supported. " : "is not supported. ");
            sb.append("lcr ");
            sb.append(this.lcrSupported ? "is Supported. " : "is not supported. ");
            if ((this.preambleSupported & 1) != 0) {
                sb.append("Legacy ");
            }
            if ((this.preambleSupported & 2) != 0) {
                sb.append("HT ");
            }
            if ((this.preambleSupported & 4) != 0) {
                sb.append("VHT ");
            }
            sb.append("is supported. ");
            if ((this.bwSupported & 1) != 0) {
                sb.append("5 MHz ");
            }
            if ((this.bwSupported & 2) != 0) {
                sb.append("10 MHz ");
            }
            if ((this.bwSupported & 4) != 0) {
                sb.append("20 MHz ");
            }
            if ((this.bwSupported & 8) != 0) {
                sb.append("40 MHz ");
            }
            if ((this.bwSupported & 16) != 0) {
                sb.append("80 MHz ");
            }
            if ((this.bwSupported & 32) != 0) {
                sb.append("160 MHz ");
            }
            sb.append("is supported.");
            sb.append(" STA responder role is ");
            sb.append(this.responderSupported ? "supported" : "not supported");
            sb.append(" Secure RTT protocol is ");
            sb.append(this.secureRttSupported ? "supported" : "not supported");
            sb.append(" 11mc version is " + this.mcVersion);
            return sb.toString();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.oneSidedRttSupported ? 1 : 0);
            dest.writeInt(this.twoSided11McRttSupported ? 1 : 0);
            dest.writeInt(this.lciSupported ? 1 : 0);
            dest.writeInt(this.lcrSupported ? 1 : 0);
            dest.writeInt(this.preambleSupported);
            dest.writeInt(this.bwSupported);
            dest.writeInt(this.responderSupported ? 1 : 0);
            dest.writeInt(this.secureRttSupported ? 1 : 0);
            dest.writeInt(this.mcVersion);
        }
    }

    @Deprecated
    public interface RttListener {
        void onAborted();

        void onFailure(int i, String str);

        void onSuccess(RttResult[] rttResultArr);
    }

    @Deprecated
    public static class RttParams {
        public boolean LCIRequest;
        public boolean LCRRequest;
        public int bandwidth = 4;
        public String bssid;
        public int burstTimeout = 15;
        public int centerFreq0;
        public int centerFreq1;
        public int channelWidth;
        public int deviceType = 1;
        public int frequency;
        public int interval;
        public int numRetriesPerFTMR = 0;
        public int numRetriesPerMeasurementFrame = 0;
        public int numSamplesPerBurst = 8;
        @Deprecated
        public int num_retries;
        @Deprecated
        public int num_samples;
        public int numberBurst = 0;
        public int preamble = 2;
        public int requestType = 1;
        public boolean secure;

        public String toString() {
            return ("deviceType=" + this.deviceType) + (", requestType=" + this.requestType) + (", secure=" + this.secure) + (", bssid=" + this.bssid) + (", frequency=" + this.frequency) + (", channelWidth=" + this.channelWidth) + (", centerFreq0=" + this.centerFreq0) + (", centerFreq1=" + this.centerFreq1) + (", num_samples=" + this.num_samples) + (", num_retries=" + this.num_retries) + (", numberBurst=" + this.numberBurst) + (", interval=" + this.interval) + (", numSamplesPerBurst=" + this.numSamplesPerBurst) + (", numRetriesPerMeasurementFrame=" + this.numRetriesPerMeasurementFrame) + (", numRetriesPerFTMR=" + this.numRetriesPerFTMR) + (", LCIRequest=" + this.LCIRequest) + (", LCRRequest=" + this.LCRRequest) + (", burstTimeout=" + this.burstTimeout) + (", preamble=" + this.preamble) + (", bandwidth=" + this.bandwidth);
        }
    }

    @Deprecated
    public static class RttResult {
        public WifiInformationElement LCI;
        public WifiInformationElement LCR;
        public String bssid;
        public int burstDuration;
        public int burstNumber;
        public int distance;
        public int distanceSpread;
        public int distanceStandardDeviation;
        @Deprecated
        public int distance_cm;
        @Deprecated
        public int distance_sd_cm;
        @Deprecated
        public int distance_spread_cm;
        public int frameNumberPerBurstPeer;
        public int measurementFrameNumber;
        public int measurementType;
        public int negotiatedBurstNum;
        @Deprecated
        public int requestType;
        public int retryAfterDuration;
        public int rssi;
        public int rssiSpread;
        @Deprecated
        public int rssi_spread;
        public long rtt;
        public long rttSpread;
        public long rttStandardDeviation;
        @Deprecated
        public long rtt_ns;
        @Deprecated
        public long rtt_sd_ns;
        @Deprecated
        public long rtt_spread_ns;
        public int rxRate;
        public boolean secure;
        public int status;
        public int successMeasurementFrameNumber;
        public long ts;
        public int txRate;
        @Deprecated
        public int tx_rate;
    }

    @Deprecated
    public static class WifiInformationElement {
        public byte[] data;
        public byte id;
    }

    @SuppressLint({"Doclava125"})
    @Deprecated
    public Capabilities getCapabilities() {
        throw new UnsupportedOperationException("getCapabilities is not supported in the adaptation layer");
    }

    public RttCapabilities getRttCapabilities() {
        return this.mRttCapabilities;
    }

    public void startRanging(RttParams[] params, final RttListener listener) {
        Log.i(TAG, "Send RTT request to RTT Service");
        if (!this.mNewService.isAvailable()) {
            listener.onFailure(-2, "");
            return;
        }
        RangingRequest.Builder builder = new RangingRequest.Builder();
        for (RttParams rttParams : params) {
            if (rttParams.deviceType != 1) {
                listener.onFailure(-4, "Only AP peers are supported");
                return;
            }
            ScanResult reconstructed = new ScanResult();
            reconstructed.BSSID = rttParams.bssid;
            if (rttParams.requestType == 2) {
                reconstructed.setFlag(2);
            }
            reconstructed.channelWidth = rttParams.channelWidth;
            reconstructed.frequency = rttParams.frequency;
            reconstructed.centerFreq0 = rttParams.centerFreq0;
            reconstructed.centerFreq1 = rttParams.centerFreq1;
            builder.addResponder(android.net.wifi.rtt.ResponderConfig.fromScanResult(reconstructed));
        }
        try {
            this.mNewService.startRanging(builder.build(), this.mContext.getMainExecutor(), new RangingResultCallback() {
                public void onRangingFailure(int code) {
                    int localCode = -1;
                    if (code == 2) {
                        localCode = -2;
                    }
                    listener.onFailure(localCode, "");
                }

                public void onRangingResults(List<RangingResult> results) {
                    RttResult[] legacyResults = new RttResult[results.size()];
                    int i = 0;
                    for (RangingResult result : results) {
                        legacyResults[i] = new RttResult();
                        legacyResults[i].status = result.getStatus();
                        legacyResults[i].bssid = result.getMacAddress().toString();
                        if (result.getStatus() == 0) {
                            legacyResults[i].distance = result.getDistanceMm() / 10;
                            legacyResults[i].distanceStandardDeviation = result.getDistanceStdDevMm() / 10;
                            legacyResults[i].rssi = result.getRssi() * -2;
                            legacyResults[i].ts = result.getRangingTimestampMillis() * 1000;
                            legacyResults[i].measurementFrameNumber = result.getNumAttemptedMeasurements();
                            legacyResults[i].successMeasurementFrameNumber = result.getNumSuccessfulMeasurements();
                        } else {
                            legacyResults[i].ts = SystemClock.elapsedRealtime() * 1000;
                        }
                        i++;
                    }
                    listener.onSuccess(legacyResults);
                }
            });
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "startRanging: invalid arguments - " + e);
            listener.onFailure(-4, e.getMessage());
        } catch (SecurityException e2) {
            Log.e(TAG, "startRanging: security exception - " + e2);
            listener.onFailure(-5, e2.getMessage());
        }
    }

    public void stopRanging(RttListener listener) {
        Log.e(TAG, "stopRanging: unsupported operation - nop");
    }

    public void enableResponder(ResponderCallback callback) {
        throw new UnsupportedOperationException("enableResponder is not supported in the adaptation layer");
    }

    public void disableResponder(ResponderCallback callback) {
        throw new UnsupportedOperationException("disableResponder is not supported in the adaptation layer");
    }

    public RttManager(Context context, WifiRttManager service) {
        this.mNewService = service;
        this.mContext = context;
        boolean rttSupported = context.getPackageManager().hasSystemFeature("android.hardware.wifi.rtt");
        this.mRttCapabilities.oneSidedRttSupported = rttSupported;
        this.mRttCapabilities.twoSided11McRttSupported = rttSupported;
        this.mRttCapabilities.lciSupported = false;
        this.mRttCapabilities.lcrSupported = false;
        this.mRttCapabilities.preambleSupported = 6;
        this.mRttCapabilities.bwSupported = 24;
        this.mRttCapabilities.responderSupported = false;
        this.mRttCapabilities.secureRttSupported = false;
    }
}
