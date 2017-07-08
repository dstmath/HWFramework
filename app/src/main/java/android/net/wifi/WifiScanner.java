package android.net.wifi;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.os.WorkSource;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.Preconditions;
import java.util.List;

public class WifiScanner {
    private static final int BASE = 159744;
    public static final int CMD_AP_FOUND = 159753;
    public static final int CMD_AP_LOST = 159754;
    public static final int CMD_CONFIGURE_WIFI_CHANGE = 159757;
    public static final int CMD_FULL_SCAN_RESULT = 159764;
    public static final int CMD_GET_SCAN_RESULTS = 159748;
    public static final int CMD_OP_FAILED = 159762;
    public static final int CMD_OP_SUCCEEDED = 159761;
    public static final int CMD_PERIOD_CHANGED = 159763;
    public static final int CMD_PNO_NETWORK_FOUND = 159770;
    public static final int CMD_RESET_HOTLIST = 159751;
    public static final int CMD_SCAN = 159744;
    public static final int CMD_SCAN_RESULT = 159749;
    public static final int CMD_SET_HOTLIST = 159750;
    public static final int CMD_SINGLE_SCAN_COMPLETED = 159767;
    public static final int CMD_START_BACKGROUND_SCAN = 159746;
    public static final int CMD_START_PNO_SCAN = 159768;
    public static final int CMD_START_SINGLE_SCAN = 159765;
    public static final int CMD_START_TRACKING_CHANGE = 159755;
    public static final int CMD_STOP_BACKGROUND_SCAN = 159747;
    public static final int CMD_STOP_PNO_SCAN = 159769;
    public static final int CMD_STOP_SINGLE_SCAN = 159766;
    public static final int CMD_STOP_TRACKING_CHANGE = 159756;
    public static final int CMD_WIFI_CHANGES_STABILIZED = 159760;
    public static final int CMD_WIFI_CHANGE_DETECTED = 159759;
    private static final boolean DBG = false;
    public static final String GET_AVAILABLE_CHANNELS_EXTRA = "Channels";
    private static final int INVALID_KEY = 0;
    public static final int MAX_SCAN_PERIOD_MS = 1024000;
    public static final int MIN_SCAN_PERIOD_MS = 1000;
    public static final String PNO_PARAMS_PNO_SETTINGS_KEY = "PnoSettings";
    public static final String PNO_PARAMS_SCAN_SETTINGS_KEY = "ScanSettings";
    public static final int REASON_DUPLICATE_REQEUST = -5;
    public static final int REASON_INVALID_LISTENER = -2;
    public static final int REASON_INVALID_REQUEST = -3;
    public static final int REASON_NOT_AUTHORIZED = -4;
    public static final int REASON_SUCCEEDED = 0;
    public static final int REASON_UNSPECIFIED = -1;
    @Deprecated
    public static final int REPORT_EVENT_AFTER_BUFFER_FULL = 0;
    public static final int REPORT_EVENT_AFTER_EACH_SCAN = 1;
    public static final int REPORT_EVENT_FULL_SCAN_RESULT = 2;
    public static final int REPORT_EVENT_NO_BATCH = 4;
    public static final String SCAN_PARAMS_SCAN_SETTINGS_KEY = "ScanSettings";
    public static final String SCAN_PARAMS_WORK_SOURCE_KEY = "WorkSource";
    private static final String TAG = "WifiScanner";
    public static final int WIFI_BAND_24_GHZ = 1;
    public static final int WIFI_BAND_5_GHZ = 2;
    public static final int WIFI_BAND_5_GHZ_DFS_ONLY = 4;
    public static final int WIFI_BAND_5_GHZ_WITH_DFS = 6;
    public static final int WIFI_BAND_BOTH = 3;
    public static final int WIFI_BAND_BOTH_WITH_DFS = 7;
    public static final int WIFI_BAND_UNSPECIFIED = 0;
    private AsyncChannel mAsyncChannel;
    private Context mContext;
    private final Handler mInternalHandler;
    private int mListenerKey;
    private final SparseArray mListenerMap;
    private final Object mListenerMapLock;
    private IWifiScanner mService;

    public interface ActionListener {
        void onFailure(int i, String str);

        void onSuccess();
    }

    public static class BssidInfo {
        public String bssid;
        public int frequencyHint;
        public int high;
        public int low;
    }

    public interface BssidListener extends ActionListener {
        void onFound(ScanResult[] scanResultArr);

        void onLost(ScanResult[] scanResultArr);
    }

    public static class ChannelSpec {
        public int dwellTimeMS;
        public int frequency;
        public boolean passive;

        public ChannelSpec(int frequency) {
            this.frequency = frequency;
            this.passive = WifiScanner.DBG;
            this.dwellTimeMS = WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL;
        }
    }

    public static class HotlistSettings implements Parcelable {
        public static final Creator<HotlistSettings> CREATOR = null;
        public int apLostThreshold;
        public BssidInfo[] bssidInfos;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiScanner.HotlistSettings.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiScanner.HotlistSettings.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiScanner.HotlistSettings.<clinit>():void");
        }

        public int describeContents() {
            return WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.apLostThreshold);
            if (this.bssidInfos != null) {
                dest.writeInt(this.bssidInfos.length);
                for (int i = WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL; i < this.bssidInfos.length; i += WifiScanner.WIFI_BAND_24_GHZ) {
                    BssidInfo info = this.bssidInfos[i];
                    dest.writeString(info.bssid);
                    dest.writeInt(info.low);
                    dest.writeInt(info.high);
                    dest.writeInt(info.frequencyHint);
                }
                return;
            }
            dest.writeInt(WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL);
        }
    }

    public static class OperationResult implements Parcelable {
        public static final Creator<OperationResult> CREATOR = null;
        public String description;
        public int reason;

        /* renamed from: android.net.wifi.WifiScanner.OperationResult.1 */
        static class AnonymousClass1 implements Creator<OperationResult> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m38createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public OperationResult createFromParcel(Parcel in) {
                return new OperationResult(in.readInt(), in.readString());
            }

            public /* bridge */ /* synthetic */ Object[] m39newArray(int size) {
                return newArray(size);
            }

            public OperationResult[] newArray(int size) {
                return new OperationResult[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiScanner.OperationResult.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiScanner.OperationResult.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiScanner.OperationResult.<clinit>():void");
        }

        public OperationResult(int reason, String description) {
            this.reason = reason;
            this.description = description;
        }

        public int describeContents() {
            return WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.reason);
            dest.writeString(this.description);
        }
    }

    public static class ParcelableScanData implements Parcelable {
        public static final Creator<ParcelableScanData> CREATOR = null;
        public ScanData[] mResults;

        /* renamed from: android.net.wifi.WifiScanner.ParcelableScanData.1 */
        static class AnonymousClass1 implements Creator<ParcelableScanData> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m40createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public ParcelableScanData createFromParcel(Parcel in) {
                int n = in.readInt();
                ScanData[] results = new ScanData[n];
                for (int i = WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL; i < n; i += WifiScanner.WIFI_BAND_24_GHZ) {
                    results[i] = (ScanData) ScanData.CREATOR.createFromParcel(in);
                }
                return new ParcelableScanData(results);
            }

            public /* bridge */ /* synthetic */ Object[] m41newArray(int size) {
                return newArray(size);
            }

            public ParcelableScanData[] newArray(int size) {
                return new ParcelableScanData[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiScanner.ParcelableScanData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiScanner.ParcelableScanData.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiScanner.ParcelableScanData.<clinit>():void");
        }

        public ParcelableScanData(ScanData[] results) {
            this.mResults = results;
        }

        public ScanData[] getResults() {
            return this.mResults;
        }

        public int describeContents() {
            return WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL;
        }

        public void writeToParcel(Parcel dest, int flags) {
            if (this.mResults != null) {
                dest.writeInt(this.mResults.length);
                for (int i = WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL; i < this.mResults.length; i += WifiScanner.WIFI_BAND_24_GHZ) {
                    this.mResults[i].writeToParcel(dest, flags);
                }
                return;
            }
            dest.writeInt(WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL);
        }
    }

    public static class ParcelableScanResults implements Parcelable {
        public static final Creator<ParcelableScanResults> CREATOR = null;
        public ScanResult[] mResults;

        /* renamed from: android.net.wifi.WifiScanner.ParcelableScanResults.1 */
        static class AnonymousClass1 implements Creator<ParcelableScanResults> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m42createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public ParcelableScanResults createFromParcel(Parcel in) {
                int n = in.readInt();
                ScanResult[] results = new ScanResult[n];
                for (int i = WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL; i < n; i += WifiScanner.WIFI_BAND_24_GHZ) {
                    results[i] = (ScanResult) ScanResult.CREATOR.createFromParcel(in);
                }
                return new ParcelableScanResults(results);
            }

            public /* bridge */ /* synthetic */ Object[] m43newArray(int size) {
                return newArray(size);
            }

            public ParcelableScanResults[] newArray(int size) {
                return new ParcelableScanResults[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiScanner.ParcelableScanResults.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiScanner.ParcelableScanResults.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiScanner.ParcelableScanResults.<clinit>():void");
        }

        public ParcelableScanResults(ScanResult[] results) {
            this.mResults = results;
        }

        public ScanResult[] getResults() {
            return this.mResults;
        }

        public int describeContents() {
            return WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL;
        }

        public void writeToParcel(Parcel dest, int flags) {
            if (this.mResults != null) {
                dest.writeInt(this.mResults.length);
                for (int i = WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL; i < this.mResults.length; i += WifiScanner.WIFI_BAND_24_GHZ) {
                    this.mResults[i].writeToParcel(dest, flags);
                }
                return;
            }
            dest.writeInt(WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL);
        }
    }

    public interface ScanListener extends ActionListener {
        void onFullResult(ScanResult scanResult);

        void onPeriodChanged(int i);

        void onResults(ScanData[] scanDataArr);
    }

    public interface PnoScanListener extends ScanListener {
        void onPnoNetworkFound(ScanResult[] scanResultArr);
    }

    public static class PnoSettings implements Parcelable {
        public static final Creator<PnoSettings> CREATOR = null;
        public int band5GHzBonus;
        public int currentConnectionBonus;
        public int initialScoreMax;
        public boolean isConnected;
        public int min24GHzRssi;
        public int min5GHzRssi;
        public PnoNetwork[] networkList;
        public int sameNetworkBonus;
        public int secureBonus;

        /* renamed from: android.net.wifi.WifiScanner.PnoSettings.1 */
        static class AnonymousClass1 implements Creator<PnoSettings> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m44createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public PnoSettings createFromParcel(Parcel in) {
                boolean z = true;
                PnoSettings settings = new PnoSettings();
                if (in.readInt() != WifiScanner.WIFI_BAND_24_GHZ) {
                    z = WifiScanner.DBG;
                }
                settings.isConnected = z;
                settings.min5GHzRssi = in.readInt();
                settings.min24GHzRssi = in.readInt();
                settings.initialScoreMax = in.readInt();
                settings.currentConnectionBonus = in.readInt();
                settings.sameNetworkBonus = in.readInt();
                settings.secureBonus = in.readInt();
                settings.band5GHzBonus = in.readInt();
                int numNetworks = in.readInt();
                settings.networkList = new PnoNetwork[numNetworks];
                for (int i = WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL; i < numNetworks; i += WifiScanner.WIFI_BAND_24_GHZ) {
                    PnoNetwork network = new PnoNetwork(in.readString());
                    network.networkId = in.readInt();
                    network.priority = in.readInt();
                    network.flags = in.readByte();
                    network.authBitField = in.readByte();
                    settings.networkList[i] = network;
                }
                return settings;
            }

            public /* bridge */ /* synthetic */ Object[] m45newArray(int size) {
                return newArray(size);
            }

            public PnoSettings[] newArray(int size) {
                return new PnoSettings[size];
            }
        }

        public static class PnoNetwork {
            public static final byte AUTH_CODE_EAPOL = (byte) 4;
            public static final byte AUTH_CODE_OPEN = (byte) 1;
            public static final byte AUTH_CODE_PSK = (byte) 2;
            public static final byte FLAG_A_BAND = (byte) 2;
            public static final byte FLAG_DIRECTED_SCAN = (byte) 1;
            public static final byte FLAG_G_BAND = (byte) 4;
            public static final byte FLAG_SAME_NETWORK = (byte) 16;
            public static final byte FLAG_STRICT_MATCH = (byte) 8;
            public byte authBitField;
            public byte flags;
            public int networkId;
            public int priority;
            public String ssid;

            public PnoNetwork(String ssid) {
                this.ssid = ssid;
                this.flags = (byte) 0;
                this.authBitField = (byte) 0;
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiScanner.PnoSettings.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiScanner.PnoSettings.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiScanner.PnoSettings.<clinit>():void");
        }

        public PnoSettings() {
        }

        public int describeContents() {
            return WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            if (this.isConnected) {
                i = WifiScanner.WIFI_BAND_24_GHZ;
            } else {
                i = WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL;
            }
            dest.writeInt(i);
            dest.writeInt(this.min5GHzRssi);
            dest.writeInt(this.min24GHzRssi);
            dest.writeInt(this.initialScoreMax);
            dest.writeInt(this.currentConnectionBonus);
            dest.writeInt(this.sameNetworkBonus);
            dest.writeInt(this.secureBonus);
            dest.writeInt(this.band5GHzBonus);
            if (this.networkList != null) {
                dest.writeInt(this.networkList.length);
                for (int i2 = WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL; i2 < this.networkList.length; i2 += WifiScanner.WIFI_BAND_24_GHZ) {
                    dest.writeString(this.networkList[i2].ssid);
                    dest.writeInt(this.networkList[i2].networkId);
                    dest.writeInt(this.networkList[i2].priority);
                    dest.writeByte(this.networkList[i2].flags);
                    dest.writeByte(this.networkList[i2].authBitField);
                }
                return;
            }
            dest.writeInt(WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL);
        }
    }

    public static class ScanData implements Parcelable {
        public static final Creator<ScanData> CREATOR = null;
        private int mBucketsScanned;
        private int mFlags;
        private int mId;
        private ScanResult[] mResults;

        /* renamed from: android.net.wifi.WifiScanner.ScanData.1 */
        static class AnonymousClass1 implements Creator<ScanData> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m46createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public ScanData createFromParcel(Parcel in) {
                int id = in.readInt();
                int flags = in.readInt();
                int bucketsScanned = in.readInt();
                int n = in.readInt();
                ScanResult[] results = new ScanResult[n];
                for (int i = WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL; i < n; i += WifiScanner.WIFI_BAND_24_GHZ) {
                    results[i] = (ScanResult) ScanResult.CREATOR.createFromParcel(in);
                }
                return new ScanData(id, flags, bucketsScanned, results);
            }

            public /* bridge */ /* synthetic */ Object[] m47newArray(int size) {
                return newArray(size);
            }

            public ScanData[] newArray(int size) {
                return new ScanData[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiScanner.ScanData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiScanner.ScanData.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiScanner.ScanData.<clinit>():void");
        }

        ScanData() {
        }

        public ScanData(int id, int flags, ScanResult[] results) {
            this.mId = id;
            this.mFlags = flags;
            this.mResults = results;
        }

        public ScanData(int id, int flags, int bucketsScanned, ScanResult[] results) {
            this.mId = id;
            this.mFlags = flags;
            this.mBucketsScanned = bucketsScanned;
            this.mResults = results;
        }

        public ScanData(ScanData s) {
            this.mId = s.mId;
            this.mFlags = s.mFlags;
            this.mBucketsScanned = s.mBucketsScanned;
            this.mResults = new ScanResult[s.mResults.length];
            for (int i = WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL; i < s.mResults.length; i += WifiScanner.WIFI_BAND_24_GHZ) {
                this.mResults[i] = new ScanResult(s.mResults[i]);
            }
        }

        public int getId() {
            return this.mId;
        }

        public int getFlags() {
            return this.mFlags;
        }

        public int getBucketsScanned() {
            return this.mBucketsScanned;
        }

        public ScanResult[] getResults() {
            return this.mResults;
        }

        public int describeContents() {
            return WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL;
        }

        public void writeToParcel(Parcel dest, int flags) {
            if (this.mResults != null) {
                dest.writeInt(this.mId);
                dest.writeInt(this.mFlags);
                dest.writeInt(this.mBucketsScanned);
                dest.writeInt(this.mResults.length);
                for (int i = WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL; i < this.mResults.length; i += WifiScanner.WIFI_BAND_24_GHZ) {
                    this.mResults[i].writeToParcel(dest, flags);
                }
                return;
            }
            dest.writeInt(WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL);
        }
    }

    public static class ScanSettings implements Parcelable {
        public static final Creator<ScanSettings> CREATOR = null;
        public int band;
        public ChannelSpec[] channels;
        public int[] hiddenNetworkIds;
        public boolean isPnoScan;
        public int maxPeriodInMs;
        public int maxScansToCache;
        public int numBssidsPerScan;
        public int periodInMs;
        public int reportEvents;
        public int stepCount;

        /* renamed from: android.net.wifi.WifiScanner.ScanSettings.1 */
        static class AnonymousClass1 implements Creator<ScanSettings> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m48createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public ScanSettings createFromParcel(Parcel in) {
                boolean z;
                ScanSettings settings = new ScanSettings();
                settings.band = in.readInt();
                settings.periodInMs = in.readInt();
                settings.reportEvents = in.readInt();
                settings.numBssidsPerScan = in.readInt();
                settings.maxScansToCache = in.readInt();
                settings.maxPeriodInMs = in.readInt();
                settings.stepCount = in.readInt();
                if (in.readInt() == WifiScanner.WIFI_BAND_24_GHZ) {
                    z = true;
                } else {
                    z = WifiScanner.DBG;
                }
                settings.isPnoScan = z;
                int num_channels = in.readInt();
                settings.channels = new ChannelSpec[num_channels];
                for (int i = WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL; i < num_channels; i += WifiScanner.WIFI_BAND_24_GHZ) {
                    ChannelSpec spec = new ChannelSpec(in.readInt());
                    spec.dwellTimeMS = in.readInt();
                    if (in.readInt() == WifiScanner.WIFI_BAND_24_GHZ) {
                        z = true;
                    } else {
                        z = WifiScanner.DBG;
                    }
                    spec.passive = z;
                    settings.channels[i] = spec;
                }
                settings.hiddenNetworkIds = in.createIntArray();
                return settings;
            }

            public /* bridge */ /* synthetic */ Object[] m49newArray(int size) {
                return newArray(size);
            }

            public ScanSettings[] newArray(int size) {
                return new ScanSettings[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiScanner.ScanSettings.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiScanner.ScanSettings.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiScanner.ScanSettings.<clinit>():void");
        }

        public ScanSettings() {
        }

        public int describeContents() {
            return WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            dest.writeInt(this.band);
            dest.writeInt(this.periodInMs);
            dest.writeInt(this.reportEvents);
            dest.writeInt(this.numBssidsPerScan);
            dest.writeInt(this.maxScansToCache);
            dest.writeInt(this.maxPeriodInMs);
            dest.writeInt(this.stepCount);
            if (this.isPnoScan) {
                i = WifiScanner.WIFI_BAND_24_GHZ;
            } else {
                i = WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL;
            }
            dest.writeInt(i);
            if (this.channels != null) {
                dest.writeInt(this.channels.length);
                for (int i2 = WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL; i2 < this.channels.length; i2 += WifiScanner.WIFI_BAND_24_GHZ) {
                    dest.writeInt(this.channels[i2].frequency);
                    dest.writeInt(this.channels[i2].dwellTimeMS);
                    if (this.channels[i2].passive) {
                        i = WifiScanner.WIFI_BAND_24_GHZ;
                    } else {
                        i = WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL;
                    }
                    dest.writeInt(i);
                }
            } else {
                dest.writeInt(WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL);
            }
            dest.writeIntArray(this.hiddenNetworkIds);
        }
    }

    private class ServiceHandler extends Handler {
        final /* synthetic */ WifiScanner this$0;

        ServiceHandler(WifiScanner this$0, Looper looper) {
            this.this$0 = this$0;
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 69634:
                case 69636:
                    Log.e(WifiScanner.TAG, "Channel connection lost");
                    this.this$0.mAsyncChannel = null;
                    getLooper().quit();
                default:
                    Object listener = this.this$0.getListener(msg.arg2);
                    if (listener != null) {
                        switch (msg.what) {
                            case WifiScanner.CMD_SCAN_RESULT /*159749*/:
                                ((ScanListener) listener).onResults(((ParcelableScanData) msg.obj).getResults());
                            case WifiScanner.CMD_AP_FOUND /*159753*/:
                                ((BssidListener) listener).onFound(((ParcelableScanResults) msg.obj).getResults());
                            case WifiScanner.CMD_AP_LOST /*159754*/:
                                ((BssidListener) listener).onLost(((ParcelableScanResults) msg.obj).getResults());
                            case WifiScanner.CMD_WIFI_CHANGE_DETECTED /*159759*/:
                                ((WifiChangeListener) listener).onChanging(((ParcelableScanResults) msg.obj).getResults());
                            case WifiScanner.CMD_WIFI_CHANGES_STABILIZED /*159760*/:
                                ((WifiChangeListener) listener).onQuiescence(((ParcelableScanResults) msg.obj).getResults());
                            case WifiScanner.CMD_OP_SUCCEEDED /*159761*/:
                                ((ActionListener) listener).onSuccess();
                                break;
                            case WifiScanner.CMD_OP_FAILED /*159762*/:
                                OperationResult result = msg.obj;
                                ((ActionListener) listener).onFailure(result.reason, result.description);
                                this.this$0.removeListener(msg.arg2);
                                break;
                            case WifiScanner.CMD_PERIOD_CHANGED /*159763*/:
                                ((ScanListener) listener).onPeriodChanged(msg.arg1);
                            case WifiScanner.CMD_FULL_SCAN_RESULT /*159764*/:
                                ((ScanListener) listener).onFullResult(msg.obj);
                            case WifiScanner.CMD_SINGLE_SCAN_COMPLETED /*159767*/:
                                this.this$0.removeListener(msg.arg2);
                                break;
                            case WifiScanner.CMD_PNO_NETWORK_FOUND /*159770*/:
                                ((PnoScanListener) listener).onPnoNetworkFound(((ParcelableScanResults) msg.obj).getResults());
                            default:
                        }
                    }
            }
        }
    }

    public interface WifiChangeListener extends ActionListener {
        void onChanging(ScanResult[] scanResultArr);

        void onQuiescence(ScanResult[] scanResultArr);
    }

    public static class WifiChangeSettings implements Parcelable {
        public static final Creator<WifiChangeSettings> CREATOR = null;
        public BssidInfo[] bssidInfos;
        public int lostApSampleSize;
        public int minApsBreachingThreshold;
        public int periodInMs;
        public int rssiSampleSize;
        public int unchangedSampleSize;

        /* renamed from: android.net.wifi.WifiScanner.WifiChangeSettings.1 */
        static class AnonymousClass1 implements Creator<WifiChangeSettings> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m50createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public WifiChangeSettings createFromParcel(Parcel in) {
                WifiChangeSettings settings = new WifiChangeSettings();
                settings.rssiSampleSize = in.readInt();
                settings.lostApSampleSize = in.readInt();
                settings.unchangedSampleSize = in.readInt();
                settings.minApsBreachingThreshold = in.readInt();
                settings.periodInMs = in.readInt();
                int len = in.readInt();
                settings.bssidInfos = new BssidInfo[len];
                for (int i = WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL; i < len; i += WifiScanner.WIFI_BAND_24_GHZ) {
                    BssidInfo info = new BssidInfo();
                    info.bssid = in.readString();
                    info.low = in.readInt();
                    info.high = in.readInt();
                    info.frequencyHint = in.readInt();
                    settings.bssidInfos[i] = info;
                }
                return settings;
            }

            public /* bridge */ /* synthetic */ Object[] m51newArray(int size) {
                return newArray(size);
            }

            public WifiChangeSettings[] newArray(int size) {
                return new WifiChangeSettings[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiScanner.WifiChangeSettings.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiScanner.WifiChangeSettings.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiScanner.WifiChangeSettings.<clinit>():void");
        }

        public WifiChangeSettings() {
        }

        public int describeContents() {
            return WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.rssiSampleSize);
            dest.writeInt(this.lostApSampleSize);
            dest.writeInt(this.unchangedSampleSize);
            dest.writeInt(this.minApsBreachingThreshold);
            dest.writeInt(this.periodInMs);
            if (this.bssidInfos != null) {
                dest.writeInt(this.bssidInfos.length);
                for (int i = WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL; i < this.bssidInfos.length; i += WifiScanner.WIFI_BAND_24_GHZ) {
                    BssidInfo info = this.bssidInfos[i];
                    dest.writeString(info.bssid);
                    dest.writeInt(info.low);
                    dest.writeInt(info.high);
                    dest.writeInt(info.frequencyHint);
                }
                return;
            }
            dest.writeInt(WifiScanner.REPORT_EVENT_AFTER_BUFFER_FULL);
        }
    }

    public List<Integer> getAvailableChannels(int band) {
        try {
            return this.mService.getAvailableChannels(band).getIntegerArrayList(GET_AVAILABLE_CHANNELS_EXTRA);
        } catch (RemoteException e) {
            return null;
        }
    }

    public void startBackgroundScan(ScanSettings settings, ScanListener listener) {
        startBackgroundScan(settings, listener, null);
    }

    public void startBackgroundScan(ScanSettings settings, ScanListener listener, WorkSource workSource) {
        Preconditions.checkNotNull(listener, "listener cannot be null");
        int key = addListener(listener);
        if (key != 0) {
            validateChannel();
            Bundle scanParams = new Bundle();
            scanParams.putParcelable(SCAN_PARAMS_SCAN_SETTINGS_KEY, settings);
            scanParams.putParcelable(SCAN_PARAMS_WORK_SOURCE_KEY, workSource);
            this.mAsyncChannel.sendMessage(CMD_START_BACKGROUND_SCAN, REPORT_EVENT_AFTER_BUFFER_FULL, key, scanParams);
        }
    }

    public void stopBackgroundScan(ScanListener listener) {
        Preconditions.checkNotNull(listener, "listener cannot be null");
        int key = removeListener((Object) listener);
        if (key != 0) {
            validateChannel();
            this.mAsyncChannel.sendMessage(CMD_STOP_BACKGROUND_SCAN, REPORT_EVENT_AFTER_BUFFER_FULL, key);
        }
    }

    public boolean getScanResults() {
        validateChannel();
        if (this.mAsyncChannel.sendMessageSynchronously(CMD_GET_SCAN_RESULTS, REPORT_EVENT_AFTER_BUFFER_FULL).what == CMD_OP_SUCCEEDED) {
            return true;
        }
        return DBG;
    }

    public void startScan(ScanSettings settings, ScanListener listener) {
        startScan(settings, listener, null);
    }

    public void startScan(ScanSettings settings, ScanListener listener, WorkSource workSource) {
        Preconditions.checkNotNull(listener, "listener cannot be null");
        int key = addListener(listener);
        if (key != 0) {
            validateChannel();
            Bundle scanParams = new Bundle();
            scanParams.putParcelable(SCAN_PARAMS_SCAN_SETTINGS_KEY, settings);
            scanParams.putParcelable(SCAN_PARAMS_WORK_SOURCE_KEY, workSource);
            this.mAsyncChannel.sendMessage(CMD_START_SINGLE_SCAN, REPORT_EVENT_AFTER_BUFFER_FULL, key, scanParams);
        }
    }

    public void stopScan(ScanListener listener) {
        Preconditions.checkNotNull(listener, "listener cannot be null");
        int key = removeListener((Object) listener);
        if (key != 0) {
            validateChannel();
            this.mAsyncChannel.sendMessage(CMD_STOP_SINGLE_SCAN, REPORT_EVENT_AFTER_BUFFER_FULL, key);
        }
    }

    private void startPnoScan(ScanSettings scanSettings, PnoSettings pnoSettings, int key) {
        Bundle pnoParams = new Bundle();
        scanSettings.isPnoScan = true;
        pnoParams.putParcelable(SCAN_PARAMS_SCAN_SETTINGS_KEY, scanSettings);
        pnoParams.putParcelable(PNO_PARAMS_PNO_SETTINGS_KEY, pnoSettings);
        this.mAsyncChannel.sendMessage(CMD_START_PNO_SCAN, REPORT_EVENT_AFTER_BUFFER_FULL, key, pnoParams);
    }

    public void startConnectedPnoScan(ScanSettings scanSettings, PnoSettings pnoSettings, PnoScanListener listener) {
        Preconditions.checkNotNull(listener, "listener cannot be null");
        Preconditions.checkNotNull(pnoSettings, "pnoSettings cannot be null");
        int key = addListener(listener);
        if (key != 0) {
            validateChannel();
            pnoSettings.isConnected = true;
            startPnoScan(scanSettings, pnoSettings, key);
        }
    }

    public void startDisconnectedPnoScan(ScanSettings scanSettings, PnoSettings pnoSettings, PnoScanListener listener) {
        Preconditions.checkNotNull(listener, "listener cannot be null");
        Preconditions.checkNotNull(pnoSettings, "pnoSettings cannot be null");
        int key = addListener(listener);
        if (key != 0) {
            validateChannel();
            pnoSettings.isConnected = DBG;
            startPnoScan(scanSettings, pnoSettings, key);
        }
    }

    public void stopPnoScan(ScanListener listener) {
        Preconditions.checkNotNull(listener, "listener cannot be null");
        int key = removeListener((Object) listener);
        if (key != 0) {
            validateChannel();
            this.mAsyncChannel.sendMessage(CMD_STOP_PNO_SCAN, REPORT_EVENT_AFTER_BUFFER_FULL, key);
        }
    }

    public void configureWifiChange(int rssiSampleSize, int lostApSampleSize, int unchangedSampleSize, int minApsBreachingThreshold, int periodInMs, BssidInfo[] bssidInfos) {
        validateChannel();
        WifiChangeSettings settings = new WifiChangeSettings();
        settings.rssiSampleSize = rssiSampleSize;
        settings.lostApSampleSize = lostApSampleSize;
        settings.unchangedSampleSize = unchangedSampleSize;
        settings.minApsBreachingThreshold = minApsBreachingThreshold;
        settings.periodInMs = periodInMs;
        settings.bssidInfos = bssidInfos;
        configureWifiChange(settings);
    }

    public void startTrackingWifiChange(WifiChangeListener listener) {
        Preconditions.checkNotNull(listener, "listener cannot be null");
        int key = addListener(listener);
        if (key != 0) {
            validateChannel();
            this.mAsyncChannel.sendMessage(CMD_START_TRACKING_CHANGE, REPORT_EVENT_AFTER_BUFFER_FULL, key);
        }
    }

    public void stopTrackingWifiChange(WifiChangeListener listener) {
        int key = removeListener((Object) listener);
        if (key != 0) {
            validateChannel();
            this.mAsyncChannel.sendMessage(CMD_STOP_TRACKING_CHANGE, REPORT_EVENT_AFTER_BUFFER_FULL, key);
        }
    }

    public void configureWifiChange(WifiChangeSettings settings) {
        validateChannel();
        this.mAsyncChannel.sendMessage(CMD_CONFIGURE_WIFI_CHANGE, REPORT_EVENT_AFTER_BUFFER_FULL, REPORT_EVENT_AFTER_BUFFER_FULL, settings);
    }

    public void startTrackingBssids(BssidInfo[] bssidInfos, int apLostThreshold, BssidListener listener) {
        Preconditions.checkNotNull(listener, "listener cannot be null");
        int key = addListener(listener);
        if (key != 0) {
            validateChannel();
            HotlistSettings settings = new HotlistSettings();
            settings.bssidInfos = bssidInfos;
            settings.apLostThreshold = apLostThreshold;
            this.mAsyncChannel.sendMessage(CMD_SET_HOTLIST, REPORT_EVENT_AFTER_BUFFER_FULL, key, settings);
        }
    }

    public void stopTrackingBssids(BssidListener listener) {
        Preconditions.checkNotNull(listener, "listener cannot be null");
        int key = removeListener((Object) listener);
        if (key != 0) {
            validateChannel();
            this.mAsyncChannel.sendMessage(CMD_RESET_HOTLIST, REPORT_EVENT_AFTER_BUFFER_FULL, key);
        }
    }

    public WifiScanner(Context context, IWifiScanner service, Looper looper) {
        this.mListenerKey = WIFI_BAND_24_GHZ;
        this.mListenerMap = new SparseArray();
        this.mListenerMapLock = new Object();
        this.mContext = context;
        this.mService = service;
        try {
            Messenger messenger = this.mService.getMessenger();
            if (messenger == null) {
                throw new IllegalStateException("getMessenger() returned null!  This is invalid.");
            }
            this.mAsyncChannel = new AsyncChannel();
            this.mInternalHandler = new ServiceHandler(this, looper);
            this.mAsyncChannel.connectSync(this.mContext, this.mInternalHandler, messenger);
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

    private int addListener(ActionListener listener) {
        synchronized (this.mListenerMapLock) {
            boolean keyExists = getListenerKey(listener) != 0 ? true : DBG;
            int key = putListener(listener);
            if (keyExists) {
                Message.obtain(this.mInternalHandler, CMD_OP_FAILED, REPORT_EVENT_AFTER_BUFFER_FULL, key, new OperationResult(REASON_DUPLICATE_REQEUST, "Outstanding request with same key not stopped yet")).sendToTarget();
                return REPORT_EVENT_AFTER_BUFFER_FULL;
            }
            return key;
        }
    }

    private int putListener(Object listener) {
        if (listener == null) {
            return REPORT_EVENT_AFTER_BUFFER_FULL;
        }
        int key;
        synchronized (this.mListenerMapLock) {
            do {
                key = this.mListenerKey;
                this.mListenerKey = key + WIFI_BAND_24_GHZ;
            } while (key == 0);
            this.mListenerMap.put(key, listener);
        }
        return key;
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
            return REPORT_EVENT_AFTER_BUFFER_FULL;
        }
        synchronized (this.mListenerMapLock) {
            int index = this.mListenerMap.indexOfValue(listener);
            if (index == REASON_UNSPECIFIED) {
                return REPORT_EVENT_AFTER_BUFFER_FULL;
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
            Log.e(TAG, "listener cannot be found");
            return key;
        }
        synchronized (this.mListenerMapLock) {
            this.mListenerMap.remove(key);
        }
        return key;
    }
}
