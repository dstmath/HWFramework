package android.net.wifi;

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
    private final Object mCapabilitiesLock;
    private final Context mContext;
    private int mListenerKey;
    private final SparseArray mListenerMap;
    private final Object mListenerMapLock;
    private RttCapabilities mRttCapabilities;
    private final IRttManager mService;

    @Deprecated
    public class Capabilities {
        public int supportedPeerType;
        public int supportedType;
    }

    public static class ParcelableRttParams implements Parcelable {
        public static final Creator<ParcelableRttParams> CREATOR = null;
        public RttParams[] mParams;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.RttManager.ParcelableRttParams.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.RttManager.ParcelableRttParams.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.RttManager.ParcelableRttParams.<clinit>():void");
        }

        public ParcelableRttParams(RttParams[] params) {
            if (params == null) {
                params = new RttParams[RttManager.RTT_TYPE_UNSPECIFIED];
            }
            this.mParams = params;
        }

        public int describeContents() {
            return RttManager.RTT_TYPE_UNSPECIFIED;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mParams.length);
            RttParams[] rttParamsArr = this.mParams;
            int length = rttParamsArr.length;
            for (int i = RttManager.RTT_TYPE_UNSPECIFIED; i < length; i += RttManager.RTT_TYPE_ONE_SIDED) {
                byte b;
                int i2;
                RttParams params = rttParamsArr[i];
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
                dest.writeInt(params.LCIRequest ? RttManager.RTT_TYPE_ONE_SIDED : RttManager.RTT_TYPE_UNSPECIFIED);
                if (params.LCRRequest) {
                    i2 = RttManager.RTT_TYPE_ONE_SIDED;
                } else {
                    i2 = RttManager.RTT_TYPE_UNSPECIFIED;
                }
                dest.writeInt(i2);
                dest.writeInt(params.burstTimeout);
                dest.writeInt(params.preamble);
                dest.writeInt(params.bandwidth);
            }
        }
    }

    public static class ParcelableRttResults implements Parcelable {
        public static final Creator<ParcelableRttResults> CREATOR = null;
        public RttResult[] mResults;

        /* renamed from: android.net.wifi.RttManager.ParcelableRttResults.1 */
        static class AnonymousClass1 implements Creator<ParcelableRttResults> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m67createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public ParcelableRttResults createFromParcel(Parcel in) {
                int num = in.readInt();
                if (num == 0) {
                    return new ParcelableRttResults(null);
                }
                RttResult[] results = new RttResult[num];
                for (int i = RttManager.RTT_TYPE_UNSPECIFIED; i < num; i += RttManager.RTT_TYPE_ONE_SIDED) {
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
                    if (in.readByte() != null) {
                        z = true;
                    } else {
                        z = RttManager.DBG;
                    }
                    rttResult.secure = z;
                }
                return new ParcelableRttResults(results);
            }

            public /* bridge */ /* synthetic */ Object[] m68newArray(int size) {
                return newArray(size);
            }

            public ParcelableRttResults[] newArray(int size) {
                return new ParcelableRttResults[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.RttManager.ParcelableRttResults.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.RttManager.ParcelableRttResults.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.RttManager.ParcelableRttResults.<clinit>():void");
        }

        public ParcelableRttResults(RttResult[] results) {
            this.mResults = results;
        }

        public int describeContents() {
            return RttManager.RTT_TYPE_UNSPECIFIED;
        }

        public void writeToParcel(Parcel dest, int flags) {
            if (this.mResults != null) {
                dest.writeInt(this.mResults.length);
                RttResult[] rttResultArr = this.mResults;
                int length = rttResultArr.length;
                for (int i = RttManager.RTT_TYPE_UNSPECIFIED; i < length; i += RttManager.RTT_TYPE_ONE_SIDED) {
                    byte b;
                    RttResult result = rttResultArr[i];
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
                        dest.writeInt((byte) result.LCR.data.length);
                        dest.writeByte(result.LCR.id);
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
            dest.writeInt(RttManager.RTT_TYPE_UNSPECIFIED);
        }
    }

    public static abstract class ResponderCallback {
        public abstract void onResponderEnableFailure(int i);

        public abstract void onResponderEnabled(ResponderConfig responderConfig);

        public ResponderCallback() {
        }
    }

    public static class ResponderConfig implements Parcelable {
        public static final Creator<ResponderConfig> CREATOR = null;
        public int centerFreq0;
        public int centerFreq1;
        public int channelWidth;
        public int frequency;
        public String macAddress;
        public int preamble;

        /* renamed from: android.net.wifi.RttManager.ResponderConfig.1 */
        static class AnonymousClass1 implements Creator<ResponderConfig> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m69createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

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

            public /* bridge */ /* synthetic */ Object[] m70newArray(int size) {
                return newArray(size);
            }

            public ResponderConfig[] newArray(int size) {
                return new ResponderConfig[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.RttManager.ResponderConfig.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.RttManager.ResponderConfig.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.RttManager.ResponderConfig.<clinit>():void");
        }

        public ResponderConfig() {
            this.macAddress = ProxyInfo.LOCAL_EXCL_LIST;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("macAddress = ").append(this.macAddress).append(" frequency = ").append(this.frequency).append(" centerFreq0 = ").append(this.centerFreq0).append(" centerFreq1 = ").append(this.centerFreq1).append(" channelWidth = ").append(this.channelWidth).append(" preamble = ").append(this.preamble);
            return builder.toString();
        }

        public int describeContents() {
            return RttManager.RTT_TYPE_UNSPECIFIED;
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
        public static final Creator<RttCapabilities> CREATOR = null;
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

        /* renamed from: android.net.wifi.RttManager.RttCapabilities.1 */
        static class AnonymousClass1 implements Creator<RttCapabilities> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m71createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public RttCapabilities createFromParcel(Parcel in) {
                boolean z;
                boolean z2 = true;
                RttCapabilities capabilities = new RttCapabilities();
                capabilities.oneSidedRttSupported = in.readInt() == RttManager.RTT_TYPE_ONE_SIDED ? true : RttManager.DBG;
                if (in.readInt() == RttManager.RTT_TYPE_ONE_SIDED) {
                    z = true;
                } else {
                    z = RttManager.DBG;
                }
                capabilities.twoSided11McRttSupported = z;
                if (in.readInt() == RttManager.RTT_TYPE_ONE_SIDED) {
                    z = true;
                } else {
                    z = RttManager.DBG;
                }
                capabilities.lciSupported = z;
                if (in.readInt() == RttManager.RTT_TYPE_ONE_SIDED) {
                    z = true;
                } else {
                    z = RttManager.DBG;
                }
                capabilities.lcrSupported = z;
                capabilities.preambleSupported = in.readInt();
                capabilities.bwSupported = in.readInt();
                if (in.readInt() == RttManager.RTT_TYPE_ONE_SIDED) {
                    z = true;
                } else {
                    z = RttManager.DBG;
                }
                capabilities.responderSupported = z;
                if (in.readInt() != RttManager.RTT_TYPE_ONE_SIDED) {
                    z2 = RttManager.DBG;
                }
                capabilities.secureRttSupported = z2;
                capabilities.mcVersion = in.readInt();
                return capabilities;
            }

            public /* bridge */ /* synthetic */ Object[] m72newArray(int size) {
                return newArray(size);
            }

            public RttCapabilities[] newArray(int size) {
                return new RttCapabilities[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.RttManager.RttCapabilities.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.RttManager.RttCapabilities.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.RttManager.RttCapabilities.<clinit>():void");
        }

        public RttCapabilities() {
        }

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
            if ((this.preambleSupported & RttManager.RTT_TYPE_ONE_SIDED) != 0) {
                sb.append("Legacy ");
            }
            if ((this.preambleSupported & RttManager.RTT_TYPE_TWO_SIDED) != 0) {
                sb.append("HT ");
            }
            if ((this.preambleSupported & RttManager.RTT_TYPE_11_MC) != 0) {
                sb.append("VHT ");
            }
            sb.append("is supported. ");
            if ((this.bwSupported & RttManager.RTT_TYPE_ONE_SIDED) != 0) {
                sb.append("5 MHz ");
            }
            if ((this.bwSupported & RttManager.RTT_TYPE_TWO_SIDED) != 0) {
                sb.append("10 MHz ");
            }
            if ((this.bwSupported & RttManager.RTT_TYPE_11_MC) != 0) {
                sb.append("20 MHz ");
            }
            if ((this.bwSupported & RttManager.RTT_STATUS_ABORTED) != 0) {
                sb.append("40 MHz ");
            }
            if ((this.bwSupported & RttManager.RTT_BW_80_SUPPORT) != 0) {
                sb.append("80 MHz ");
            }
            if ((this.bwSupported & RttManager.RTT_BW_160_SUPPORT) != 0) {
                sb.append("160 MHz ");
            }
            sb.append("is supported.");
            sb.append(" STA responder role is ").append(this.responderSupported ? "supported" : "not supported");
            sb.append(" Secure RTT protocol is ").append(this.secureRttSupported ? "supported" : "not supported");
            sb.append(" 11mc version is " + this.mcVersion);
            return sb.toString();
        }

        public int describeContents() {
            return RttManager.RTT_TYPE_UNSPECIFIED;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            int i2 = RttManager.RTT_TYPE_ONE_SIDED;
            if (this.oneSidedRttSupported) {
                i = RttManager.RTT_TYPE_ONE_SIDED;
            } else {
                i = RttManager.RTT_TYPE_UNSPECIFIED;
            }
            dest.writeInt(i);
            if (this.twoSided11McRttSupported) {
                i = RttManager.RTT_TYPE_ONE_SIDED;
            } else {
                i = RttManager.RTT_TYPE_UNSPECIFIED;
            }
            dest.writeInt(i);
            if (this.lciSupported) {
                i = RttManager.RTT_TYPE_ONE_SIDED;
            } else {
                i = RttManager.RTT_TYPE_UNSPECIFIED;
            }
            dest.writeInt(i);
            if (this.lcrSupported) {
                i = RttManager.RTT_TYPE_ONE_SIDED;
            } else {
                i = RttManager.RTT_TYPE_UNSPECIFIED;
            }
            dest.writeInt(i);
            dest.writeInt(this.preambleSupported);
            dest.writeInt(this.bwSupported);
            if (this.responderSupported) {
                i = RttManager.RTT_TYPE_ONE_SIDED;
            } else {
                i = RttManager.RTT_TYPE_UNSPECIFIED;
            }
            dest.writeInt(i);
            if (!this.secureRttSupported) {
                i2 = RttManager.RTT_TYPE_UNSPECIFIED;
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
        public int bandwidth;
        public String bssid;
        public int burstTimeout;
        public int centerFreq0;
        public int centerFreq1;
        public int channelWidth;
        public int deviceType;
        public int frequency;
        public int interval;
        public int numRetriesPerFTMR;
        public int numRetriesPerMeasurementFrame;
        public int numSamplesPerBurst;
        @Deprecated
        public int num_retries;
        @Deprecated
        public int num_samples;
        public int numberBurst;
        public int preamble;
        public int requestType;
        public boolean secure;

        public RttParams() {
            this.deviceType = RttManager.RTT_TYPE_ONE_SIDED;
            this.requestType = RttManager.RTT_TYPE_ONE_SIDED;
            this.numberBurst = RttManager.RTT_TYPE_UNSPECIFIED;
            this.numSamplesPerBurst = RttManager.RTT_STATUS_ABORTED;
            this.numRetriesPerMeasurementFrame = RttManager.RTT_TYPE_UNSPECIFIED;
            this.numRetriesPerFTMR = RttManager.RTT_TYPE_UNSPECIFIED;
            this.burstTimeout = RttManager.RTT_STATUS_FAIL_FTM_PARAM_OVERRIDE;
            this.preamble = RttManager.RTT_TYPE_TWO_SIDED;
            this.bandwidth = RttManager.RTT_TYPE_11_MC;
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

        public RttResult() {
        }
    }

    private class ServiceHandler extends Handler {
        final /* synthetic */ RttManager this$0;

        ServiceHandler(RttManager this$0, Looper looper) {
            this.this$0 = this$0;
            super(looper);
        }

        public void handleMessage(Message msg) {
            Log.i(RttManager.TAG, "RTT manager get message: " + msg.what);
            switch (msg.what) {
                case 69634:
                case 69636:
                    Log.e(RttManager.TAG, "Channel connection lost");
                    this.this$0.mAsyncChannel = null;
                    getLooper().quit();
                default:
                    Object listener = this.this$0.getListener(msg.arg2);
                    if (listener == null) {
                        Log.e(RttManager.TAG, "invalid listener key = " + msg.arg2);
                        return;
                    }
                    Log.i(RttManager.TAG, "listener key = " + msg.arg2);
                    switch (msg.what) {
                        case RttManager.CMD_OP_FAILED /*160258*/:
                            reportFailure(listener, msg);
                            this.this$0.removeListener(msg.arg2);
                            break;
                        case RttManager.CMD_OP_SUCCEEDED /*160259*/:
                            reportSuccess(listener, msg);
                            this.this$0.removeListener(msg.arg2);
                            break;
                        case RttManager.CMD_OP_ABORTED /*160260*/:
                            ((RttListener) listener).onAborted();
                            this.this$0.removeListener(msg.arg2);
                            break;
                        case RttManager.CMD_OP_ENALBE_RESPONDER_SUCCEEDED /*160263*/:
                            ((ResponderCallback) listener).onResponderEnabled(msg.obj);
                            break;
                        case RttManager.CMD_OP_ENALBE_RESPONDER_FAILED /*160264*/:
                            ((ResponderCallback) listener).onResponderEnableFailure(msg.arg1);
                            this.this$0.removeListener(msg.arg2);
                            break;
                        default:
                    }
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

        public WifiInformationElement() {
        }
    }

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
        } else if (params.deviceType != RTT_TYPE_ONE_SIDED) {
            return DBG;
        } else {
            if (params.requestType != RTT_TYPE_ONE_SIDED && params.requestType != RTT_TYPE_TWO_SIDED) {
                Log.e(TAG, "Request " + index + ": Illegal Request Type: " + params.requestType);
                return DBG;
            } else if (params.requestType == RTT_TYPE_ONE_SIDED && !this.mRttCapabilities.oneSidedRttSupported) {
                Log.e(TAG, "Request " + index + ": One side RTT is not supported");
                return DBG;
            } else if (params.requestType == RTT_TYPE_TWO_SIDED && !this.mRttCapabilities.twoSided11McRttSupported) {
                Log.e(TAG, "Request " + index + ": two side RTT is not supported");
                return DBG;
            } else if (params.bssid == null || params.bssid.isEmpty()) {
                Log.e(TAG, "No BSSID in params");
                return DBG;
            } else if (params.numberBurst != 0) {
                Log.e(TAG, "Request " + index + ": Illegal number of burst: " + params.numberBurst);
                return DBG;
            } else if (params.numSamplesPerBurst <= 0 || params.numSamplesPerBurst > 31) {
                Log.e(TAG, "Request " + index + ": Illegal sample number per burst: " + params.numSamplesPerBurst);
                return DBG;
            } else if (params.numRetriesPerMeasurementFrame < 0 || params.numRetriesPerMeasurementFrame > RTT_STATUS_FAIL_REJECTED) {
                Log.e(TAG, "Request " + index + ": Illegal measurement frame retry number:" + params.numRetriesPerMeasurementFrame);
                return DBG;
            } else if (params.numRetriesPerFTMR < 0 || params.numRetriesPerFTMR > RTT_STATUS_FAIL_REJECTED) {
                Log.e(TAG, "Request " + index + ": Illegal FTMR frame retry number:" + params.numRetriesPerFTMR);
                return DBG;
            } else if (params.LCIRequest && !this.mRttCapabilities.lciSupported) {
                Log.e(TAG, "Request " + index + ": LCI is not supported");
                return DBG;
            } else if (params.LCRRequest && !this.mRttCapabilities.lcrSupported) {
                Log.e(TAG, "Request " + index + ": LCR is not supported");
                return DBG;
            } else if (params.burstTimeout < RTT_TYPE_ONE_SIDED || (params.burstTimeout > RTT_STATUS_FAIL_SCHEDULE && params.burstTimeout != RTT_STATUS_FAIL_FTM_PARAM_OVERRIDE)) {
                Log.e(TAG, "Request " + index + ": Illegal burst timeout: " + params.burstTimeout);
                return DBG;
            } else if ((params.preamble & this.mRttCapabilities.preambleSupported) == 0) {
                Log.e(TAG, "Request " + index + ": Do not support this preamble: " + params.preamble);
                return DBG;
            } else if ((params.bandwidth & this.mRttCapabilities.bwSupported) != 0) {
                return true;
            } else {
                Log.e(TAG, "Request " + index + ": Do not support this bandwidth: " + params.bandwidth);
                return DBG;
            }
        }
    }

    public void startRanging(RttParams[] params, RttListener listener) {
        int index = RTT_TYPE_UNSPECIFIED;
        int length = params.length;
        int i = RTT_TYPE_UNSPECIFIED;
        while (i < length) {
            if (rttParamSanity(params[i], index)) {
                index += RTT_TYPE_ONE_SIDED;
                i += RTT_TYPE_ONE_SIDED;
            } else {
                throw new IllegalArgumentException("RTT Request Parameter Illegal");
            }
        }
        validateChannel();
        ParcelableRttParams parcelableParams = new ParcelableRttParams(params);
        Log.i(TAG, "Send RTT request to RTT Service");
        this.mAsyncChannel.sendMessage(CMD_OP_START_RANGING, RTT_TYPE_UNSPECIFIED, putListener(listener), parcelableParams);
    }

    public void stopRanging(RttListener listener) {
        validateChannel();
        this.mAsyncChannel.sendMessage(CMD_OP_STOP_RANGING, RTT_TYPE_UNSPECIFIED, removeListener((Object) listener));
    }

    public void enableResponder(ResponderCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }
        validateChannel();
        this.mAsyncChannel.sendMessage(CMD_OP_ENABLE_RESPONDER, RTT_TYPE_UNSPECIFIED, putListenerIfAbsent(callback));
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
            this.mAsyncChannel.sendMessage(CMD_OP_DISABLE_RESPONDER, RTT_TYPE_UNSPECIFIED, key);
        }
    }

    public RttManager(Context context, IRttManager service, Looper looper) {
        this.mListenerMap = new SparseArray();
        this.mListenerMapLock = new Object();
        this.mCapabilitiesLock = new Object();
        this.mListenerKey = RTT_TYPE_ONE_SIDED;
        this.mContext = context;
        this.mService = service;
        try {
            Log.d(TAG, "Get the messenger from " + this.mService);
            Messenger messenger = this.mService.getMessenger();
            if (messenger == null) {
                throw new IllegalStateException("getMessenger() returned null!  This is invalid.");
            }
            this.mAsyncChannel = new AsyncChannel();
            this.mAsyncChannel.connectSync(this.mContext, new ServiceHandler(this, looper), messenger);
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
            return RTT_TYPE_UNSPECIFIED;
        }
        int key;
        synchronized (this.mListenerMapLock) {
            do {
                key = this.mListenerKey;
                this.mListenerKey = key + RTT_TYPE_ONE_SIDED;
            } while (key == 0);
            this.mListenerMap.put(key, listener);
        }
        return key;
    }

    private int putListenerIfAbsent(Object listener) {
        if (listener == null) {
            return RTT_TYPE_UNSPECIFIED;
        }
        synchronized (this.mListenerMapLock) {
            int key = getListenerKey(listener);
            if (key != 0) {
                return key;
            }
            do {
                key = this.mListenerKey;
                this.mListenerKey = key + RTT_TYPE_ONE_SIDED;
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
            return RTT_TYPE_UNSPECIFIED;
        }
        synchronized (this.mListenerMapLock) {
            int index = this.mListenerMap.indexOfValue(listener);
            if (index == RTT_CHANNEL_WIDTH_UNSPECIFIED) {
                return RTT_TYPE_UNSPECIFIED;
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
