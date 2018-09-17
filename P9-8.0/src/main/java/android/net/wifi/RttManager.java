package android.net.wifi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ProxyInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.AsyncChannel;

public class RttManager {
    public static final int BASE = 160256;
    public static final int CMD_OP_ABORTED = 160260;
    public static final int CMD_OP_DISABLE_RESPONDER = 160262;
    public static final int CMD_OP_ENABLE_RESPONDER = 160261;
    public static final int CMD_OP_ENALBE_RESPONDER_FAILED = 160264;
    public static final int CMD_OP_ENALBE_RESPONDER_SUCCEEDED = 160263;
    public static final int CMD_OP_FAILED = 160258;
    public static final int CMD_OP_START_RANGING = 160256;
    public static final int CMD_OP_STOP_RANGING = 160257;
    public static final int CMD_OP_SUCCEEDED = 160259;
    private static final boolean DBG = false;
    public static final String DESCRIPTION_KEY = "android.net.wifi.RttManager.Description";
    private static final int INVALID_KEY = 0;
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
    private AsyncChannel mAsyncChannel;
    private final Object mCapabilitiesLock = new Object();
    private final Context mContext;
    private int mListenerKey = 1;
    private final SparseArray mListenerMap = new SparseArray();
    private final Object mListenerMapLock = new Object();
    private RttCapabilities mRttCapabilities;
    private final IRttManager mService;

    @Deprecated
    public class Capabilities {
        public int supportedPeerType;
        public int supportedType;
    }

    public static class ParcelableRttParams implements Parcelable {
        public static final Creator<ParcelableRttParams> CREATOR = new Creator<ParcelableRttParams>() {
            public ParcelableRttParams createFromParcel(Parcel in) {
                int num = in.readInt();
                RttParams[] params = new RttParams[num];
                for (int i = 0; i < num; i++) {
                    boolean z;
                    params[i] = new RttParams();
                    params[i].deviceType = in.readInt();
                    params[i].requestType = in.readInt();
                    RttParams rttParams = params[i];
                    if (in.readByte() != (byte) 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    rttParams.secure = z;
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
                    rttParams = params[i];
                    if (in.readInt() == 1) {
                        z = true;
                    } else {
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

        public ParcelableRttParams(RttParams[] params) {
            if (params == null) {
                params = new RttParams[0];
            }
            this.mParams = params;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mParams.length);
            for (RttParams params : this.mParams) {
                byte b;
                int i;
                dest.writeInt(params.deviceType);
                dest.writeInt(params.requestType);
                if (params.secure) {
                    b = (byte) 1;
                } else {
                    b = (byte) 0;
                }
                dest.writeByte(b);
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
                if (params.LCRRequest) {
                    i = 1;
                } else {
                    i = 0;
                }
                dest.writeInt(i);
                dest.writeInt(params.burstTimeout);
                dest.writeInt(params.preamble);
                dest.writeInt(params.bandwidth);
            }
        }
    }

    public static class ParcelableRttResults implements Parcelable {
        public static final Creator<ParcelableRttResults> CREATOR = new Creator<ParcelableRttResults>() {
            public ParcelableRttResults createFromParcel(Parcel in) {
                int num = in.readInt();
                if (num == 0) {
                    return new ParcelableRttResults(null);
                }
                RttResult[] results = new RttResult[num];
                for (int i = 0; i < num; i++) {
                    boolean z;
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
                    if (results[i].LCI.id != (byte) -1) {
                        results[i].LCI.data = new byte[in.readByte()];
                        in.readByteArray(results[i].LCI.data);
                    }
                    results[i].LCR = new WifiInformationElement();
                    results[i].LCR.id = in.readByte();
                    if (results[i].LCR.id != (byte) -1) {
                        results[i].LCR.data = new byte[in.readByte()];
                        in.readByteArray(results[i].LCR.data);
                    }
                    RttResult rttResult = results[i];
                    if (in.readByte() != (byte) 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    rttResult.secure = z;
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
            for (int i = 0; i < this.mResults.length; i++) {
                sb.append("[").append(i).append("]: ");
                sb.append("bssid=").append(this.mResults[i].bssid);
                sb.append(", burstNumber=").append(this.mResults[i].burstNumber);
                sb.append(", measurementFrameNumber=").append(this.mResults[i].measurementFrameNumber);
                sb.append(", successMeasurementFrameNumber=").append(this.mResults[i].successMeasurementFrameNumber);
                sb.append(", frameNumberPerBurstPeer=").append(this.mResults[i].frameNumberPerBurstPeer);
                sb.append(", status=").append(this.mResults[i].status);
                sb.append(", requestType=").append(this.mResults[i].requestType);
                sb.append(", measurementType=").append(this.mResults[i].measurementType);
                sb.append(", retryAfterDuration=").append(this.mResults[i].retryAfterDuration);
                sb.append(", ts=").append(this.mResults[i].ts);
                sb.append(", rssi=").append(this.mResults[i].rssi);
                sb.append(", rssi_spread=").append(this.mResults[i].rssi_spread);
                sb.append(", rssiSpread=").append(this.mResults[i].rssiSpread);
                sb.append(", tx_rate=").append(this.mResults[i].tx_rate);
                sb.append(", txRate=").append(this.mResults[i].txRate);
                sb.append(", rxRate=").append(this.mResults[i].rxRate);
                sb.append(", rtt_ns=").append(this.mResults[i].rtt_ns);
                sb.append(", rtt=").append(this.mResults[i].rtt);
                sb.append(", rtt_sd_ns=").append(this.mResults[i].rtt_sd_ns);
                sb.append(", rttStandardDeviation=").append(this.mResults[i].rttStandardDeviation);
                sb.append(", rtt_spread_ns=").append(this.mResults[i].rtt_spread_ns);
                sb.append(", rttSpread=").append(this.mResults[i].rttSpread);
                sb.append(", distance_cm=").append(this.mResults[i].distance_cm);
                sb.append(", distance=").append(this.mResults[i].distance);
                sb.append(", distance_sd_cm=").append(this.mResults[i].distance_sd_cm);
                sb.append(", distanceStandardDeviation=").append(this.mResults[i].distanceStandardDeviation);
                sb.append(", distance_spread_cm=").append(this.mResults[i].distance_spread_cm);
                sb.append(", distanceSpread=").append(this.mResults[i].distanceSpread);
                sb.append(", burstDuration=").append(this.mResults[i].burstDuration);
                sb.append(", negotiatedBurstNum=").append(this.mResults[i].negotiatedBurstNum);
                sb.append(", LCI=").append(this.mResults[i].LCI);
                sb.append(", LCR=").append(this.mResults[i].LCR);
                sb.append(", secure=").append(this.mResults[i].secure);
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
                    byte b;
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
                    if (result.LCI.id != (byte) -1) {
                        dest.writeByte((byte) result.LCI.data.length);
                        dest.writeByteArray(result.LCI.data);
                    }
                    dest.writeByte(result.LCR.id);
                    if (result.LCR.id != (byte) -1) {
                        dest.writeByte((byte) result.LCR.data.length);
                        dest.writeByteArray(result.LCR.data);
                    }
                    if (result.secure) {
                        b = (byte) 1;
                    } else {
                        b = (byte) 0;
                    }
                    dest.writeByte(b);
                }
                return;
            }
            dest.writeInt(0);
        }
    }

    public static abstract class ResponderCallback {
        public abstract void onResponderEnableFailure(int i);

        public abstract void onResponderEnabled(ResponderConfig responderConfig);
    }

    public static class ResponderConfig implements Parcelable {
        public static final Creator<ResponderConfig> CREATOR = new Creator<ResponderConfig>() {
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
        public String macAddress = ProxyInfo.LOCAL_EXCL_LIST;
        public int preamble;

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("macAddress = ").append(this.macAddress).append(" frequency = ").append(this.frequency).append(" centerFreq0 = ").append(this.centerFreq0).append(" centerFreq1 = ").append(this.centerFreq1).append(" channelWidth = ").append(this.channelWidth).append(" preamble = ").append(this.preamble);
            return builder.toString();
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

    public static class RttCapabilities implements Parcelable {
        public static final Creator<RttCapabilities> CREATOR = new Creator<RttCapabilities>() {
            public RttCapabilities createFromParcel(Parcel in) {
                boolean z;
                boolean z2 = true;
                RttCapabilities capabilities = new RttCapabilities();
                capabilities.oneSidedRttSupported = in.readInt() == 1;
                if (in.readInt() == 1) {
                    z = true;
                } else {
                    z = false;
                }
                capabilities.twoSided11McRttSupported = z;
                if (in.readInt() == 1) {
                    z = true;
                } else {
                    z = false;
                }
                capabilities.lciSupported = z;
                if (in.readInt() == 1) {
                    z = true;
                } else {
                    z = false;
                }
                capabilities.lcrSupported = z;
                capabilities.preambleSupported = in.readInt();
                capabilities.bwSupported = in.readInt();
                if (in.readInt() == 1) {
                    z = true;
                } else {
                    z = false;
                }
                capabilities.responderSupported = z;
                if (in.readInt() != 1) {
                    z2 = false;
                }
                capabilities.secureRttSupported = z2;
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
            String str;
            StringBuffer sb = new StringBuffer();
            StringBuffer append = sb.append("oneSidedRtt ").append(this.oneSidedRttSupported ? "is Supported. " : "is not supported. ").append("twoSided11McRtt ").append(this.twoSided11McRttSupported ? "is Supported. " : "is not supported. ").append("lci ");
            if (this.lciSupported) {
                str = "is Supported. ";
            } else {
                str = "is not supported. ";
            }
            append = append.append(str).append("lcr ");
            if (this.lcrSupported) {
                str = "is Supported. ";
            } else {
                str = "is not supported. ";
            }
            append.append(str);
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
            sb.append(" STA responder role is ").append(this.responderSupported ? "supported" : "not supported");
            sb.append(" Secure RTT protocol is ").append(this.secureRttSupported ? "supported" : "not supported");
            sb.append(" 11mc version is " + this.mcVersion);
            return sb.toString();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            int i2 = 1;
            dest.writeInt(this.oneSidedRttSupported ? 1 : 0);
            if (this.twoSided11McRttSupported) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeInt(i);
            if (this.lciSupported) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeInt(i);
            if (this.lcrSupported) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeInt(i);
            dest.writeInt(this.preambleSupported);
            dest.writeInt(this.bwSupported);
            if (this.responderSupported) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeInt(i);
            if (!this.secureRttSupported) {
                i2 = 0;
            }
            dest.writeInt(i2);
            dest.writeInt(this.mcVersion);
        }
    }

    public interface RttListener {
        void onAborted();

        void onFailure(int i, String str);

        void onSuccess(RttResult[] rttResultArr);
    }

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
            StringBuilder sb = new StringBuilder();
            sb.append("deviceType=").append(this.deviceType);
            sb.append(", requestType=").append(this.requestType);
            sb.append(", secure=").append(this.secure);
            sb.append(", bssid=").append(this.bssid);
            sb.append(", frequency=").append(this.frequency);
            sb.append(", channelWidth=").append(this.channelWidth);
            sb.append(", centerFreq0=").append(this.centerFreq0);
            sb.append(", centerFreq1=").append(this.centerFreq1);
            sb.append(", num_samples=").append(this.num_samples);
            sb.append(", num_retries=").append(this.num_retries);
            sb.append(", numberBurst=").append(this.numberBurst);
            sb.append(", interval=").append(this.interval);
            sb.append(", numSamplesPerBurst=").append(this.numSamplesPerBurst);
            sb.append(", numRetriesPerMeasurementFrame=").append(this.numRetriesPerMeasurementFrame);
            sb.append(", numRetriesPerFTMR=").append(this.numRetriesPerFTMR);
            sb.append(", LCIRequest=").append(this.LCIRequest);
            sb.append(", LCRRequest=").append(this.LCRRequest);
            sb.append(", burstTimeout=").append(this.burstTimeout);
            sb.append(", preamble=").append(this.preamble);
            sb.append(", bandwidth=").append(this.bandwidth);
            return sb.toString();
        }
    }

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

    private class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Log.i(RttManager.TAG, "RTT manager get message: " + msg.what);
            switch (msg.what) {
                case 69634:
                    return;
                case 69636:
                    Log.e(RttManager.TAG, "Channel connection lost");
                    RttManager.this.mAsyncChannel = null;
                    getLooper().quit();
                    return;
                default:
                    Object listener = RttManager.this.getListener(msg.arg2);
                    if (listener == null) {
                        Log.e(RttManager.TAG, "invalid listener key = " + msg.arg2);
                        return;
                    }
                    Log.i(RttManager.TAG, "listener key = " + msg.arg2);
                    switch (msg.what) {
                        case RttManager.CMD_OP_FAILED /*160258*/:
                            reportFailure(listener, msg);
                            RttManager.this.removeListener(msg.arg2);
                            break;
                        case RttManager.CMD_OP_SUCCEEDED /*160259*/:
                            reportSuccess(listener, msg);
                            RttManager.this.removeListener(msg.arg2);
                            break;
                        case RttManager.CMD_OP_ABORTED /*160260*/:
                            ((RttListener) listener).onAborted();
                            RttManager.this.removeListener(msg.arg2);
                            break;
                        case RttManager.CMD_OP_ENALBE_RESPONDER_SUCCEEDED /*160263*/:
                            ((ResponderCallback) listener).onResponderEnabled(msg.obj);
                            break;
                        case RttManager.CMD_OP_ENALBE_RESPONDER_FAILED /*160264*/:
                            ((ResponderCallback) listener).onResponderEnableFailure(msg.arg1);
                            RttManager.this.removeListener(msg.arg2);
                            break;
                        default:
                            return;
                    }
                    return;
            }
        }

        void reportSuccess(Object listener, Message msg) {
            RttListener rttListener = (RttListener) listener;
            ((RttListener) listener).onSuccess(msg.obj.mResults);
        }

        void reportFailure(Object listener, Message msg) {
            RttListener rttListener = (RttListener) listener;
            ((RttListener) listener).onFailure(msg.arg1, msg.obj.getString(RttManager.DESCRIPTION_KEY));
        }
    }

    public static class WifiInformationElement {
        public byte[] data;
        public byte id;
    }

    @SuppressLint({"Doclava125"})
    @Deprecated
    public Capabilities getCapabilities() {
        return new Capabilities();
    }

    public RttCapabilities getRttCapabilities() {
        RttCapabilities rttCapabilities;
        synchronized (this.mCapabilitiesLock) {
            if (this.mRttCapabilities == null) {
                try {
                    this.mRttCapabilities = this.mService.getRttCapabilities();
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            rttCapabilities = this.mRttCapabilities;
        }
        return rttCapabilities;
    }

    private boolean rttParamSanity(RttParams params, int index) {
        if (this.mRttCapabilities == null && getRttCapabilities() == null) {
            Log.e(TAG, "Can not get RTT capabilities");
            throw new IllegalStateException("RTT chip is not working");
        } else if (params.deviceType != 1) {
            return false;
        } else {
            if (params.requestType != 1 && params.requestType != 2) {
                Log.e(TAG, "Request " + index + ": Illegal Request Type: " + params.requestType);
                return false;
            } else if (params.requestType == 1 && (this.mRttCapabilities.oneSidedRttSupported ^ 1) != 0) {
                Log.e(TAG, "Request " + index + ": One side RTT is not supported");
                return false;
            } else if (params.requestType == 2 && (this.mRttCapabilities.twoSided11McRttSupported ^ 1) != 0) {
                Log.e(TAG, "Request " + index + ": two side RTT is not supported");
                return false;
            } else if (params.bssid == null || params.bssid.isEmpty()) {
                Log.e(TAG, "No BSSID in params");
                return false;
            } else if (params.numberBurst != 0) {
                Log.e(TAG, "Request " + index + ": Illegal number of burst: " + params.numberBurst);
                return false;
            } else if (params.numSamplesPerBurst <= 0 || params.numSamplesPerBurst > 31) {
                Log.e(TAG, "Request " + index + ": Illegal sample number per burst: " + params.numSamplesPerBurst);
                return false;
            } else if (params.numRetriesPerMeasurementFrame < 0 || params.numRetriesPerMeasurementFrame > 3) {
                Log.e(TAG, "Request " + index + ": Illegal measurement frame retry number:" + params.numRetriesPerMeasurementFrame);
                return false;
            } else if (params.numRetriesPerFTMR < 0 || params.numRetriesPerFTMR > 3) {
                Log.e(TAG, "Request " + index + ": Illegal FTMR frame retry number:" + params.numRetriesPerFTMR);
                return false;
            } else if (params.LCIRequest && (this.mRttCapabilities.lciSupported ^ 1) != 0) {
                Log.e(TAG, "Request " + index + ": LCI is not supported");
                return false;
            } else if (params.LCRRequest && (this.mRttCapabilities.lcrSupported ^ 1) != 0) {
                Log.e(TAG, "Request " + index + ": LCR is not supported");
                return false;
            } else if (params.burstTimeout < 1 || (params.burstTimeout > 11 && params.burstTimeout != 15)) {
                Log.e(TAG, "Request " + index + ": Illegal burst timeout: " + params.burstTimeout);
                return false;
            } else if ((params.preamble & this.mRttCapabilities.preambleSupported) == 0) {
                Log.e(TAG, "Request " + index + ": Do not support this preamble: " + params.preamble);
                return false;
            } else if ((params.bandwidth & this.mRttCapabilities.bwSupported) != 0) {
                return true;
            } else {
                Log.e(TAG, "Request " + index + ": Do not support this bandwidth: " + params.bandwidth);
                return false;
            }
        }
    }

    public void startRanging(RttParams[] params, RttListener listener) {
        int index = 0;
        int length = params.length;
        int i = 0;
        while (i < length) {
            if (rttParamSanity(params[i], index)) {
                index++;
                i++;
            } else {
                throw new IllegalArgumentException("RTT Request Parameter Illegal");
            }
        }
        validateChannel();
        ParcelableRttParams parcelableParams = new ParcelableRttParams(params);
        Log.i(TAG, "Send RTT request to RTT Service");
        this.mAsyncChannel.sendMessage(160256, 0, putListener(listener), parcelableParams);
    }

    public void stopRanging(RttListener listener) {
        validateChannel();
        this.mAsyncChannel.sendMessage(CMD_OP_STOP_RANGING, 0, removeListener((Object) listener));
    }

    public void enableResponder(ResponderCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }
        validateChannel();
        this.mAsyncChannel.sendMessage(CMD_OP_ENABLE_RESPONDER, 0, putListenerIfAbsent(callback));
    }

    public void disableResponder(ResponderCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }
        validateChannel();
        int key = removeListener((Object) callback);
        if (key == 0) {
            Log.e(TAG, "responder not enabled yet");
        } else {
            this.mAsyncChannel.sendMessage(CMD_OP_DISABLE_RESPONDER, 0, key);
        }
    }

    public RttManager(Context context, IRttManager service, Looper looper) {
        this.mContext = context;
        this.mService = service;
        try {
            Log.d(TAG, "Get the messenger from " + this.mService);
            Messenger messenger = this.mService.getMessenger();
            if (messenger == null) {
                throw new IllegalStateException("getMessenger() returned null!  This is invalid.");
            }
            this.mAsyncChannel = new AsyncChannel();
            this.mAsyncChannel.connectSync(this.mContext, new ServiceHandler(looper), messenger);
            this.mAsyncChannel.sendMessage(69633);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private void validateChannel() {
        if (this.mAsyncChannel == null) {
            throw new IllegalStateException("No permission to access and change wifi or a bad initialization");
        }
    }

    private int putListener(Object listener) {
        if (listener == null) {
            return 0;
        }
        int key;
        synchronized (this.mListenerMapLock) {
            do {
                key = this.mListenerKey;
                this.mListenerKey = key + 1;
            } while (key == 0);
            this.mListenerMap.put(key, listener);
        }
        return key;
    }

    private int putListenerIfAbsent(Object listener) {
        if (listener == null) {
            return 0;
        }
        synchronized (this.mListenerMapLock) {
            int key = getListenerKey(listener);
            if (key != 0) {
                return key;
            }
            do {
                key = this.mListenerKey;
                this.mListenerKey = key + 1;
            } while (key == 0);
            this.mListenerMap.put(key, listener);
            return key;
        }
    }

    private Object getListener(int key) {
        if (key == 0) {
            return null;
        }
        Object listener;
        synchronized (this.mListenerMapLock) {
            listener = this.mListenerMap.get(key);
        }
        return listener;
    }

    private int getListenerKey(Object listener) {
        if (listener == null) {
            return 0;
        }
        synchronized (this.mListenerMapLock) {
            int index = this.mListenerMap.indexOfValue(listener);
            if (index == -1) {
                return 0;
            }
            int keyAt = this.mListenerMap.keyAt(index);
            return keyAt;
        }
    }

    private Object removeListener(int key) {
        if (key == 0) {
            return null;
        }
        Object listener;
        synchronized (this.mListenerMapLock) {
            listener = this.mListenerMap.get(key);
            this.mListenerMap.remove(key);
        }
        return listener;
    }

    private int removeListener(Object listener) {
        int key = getListenerKey(listener);
        if (key == 0) {
            return key;
        }
        synchronized (this.mListenerMapLock) {
            this.mListenerMap.remove(key);
        }
        return key;
    }
}
