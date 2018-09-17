package android.bluetooth.le;

import android.net.wifi.ScanResult.InformationElement;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.PowerManager;
import android.util.HwLog;

public final class ScanSettings implements Parcelable {
    public static final int CALLBACK_TYPE_ALL_MATCHES = 1;
    public static final int CALLBACK_TYPE_FIRST_MATCH = 2;
    public static final int CALLBACK_TYPE_MATCH_LOST = 4;
    public static final Creator<ScanSettings> CREATOR = null;
    public static final int EXTENDED_SELECTION_BIT_FILTER_LOGIC_TYPE = 3;
    public static final int EXTENDED_SELECTION_BIT_LIST_LOGIC_TYPE = 2;
    public static final int EXTENDED_SELECTION_BIT_RSSI_HIGH_VALUE = 0;
    public static final int EXTENDED_SELECTION_BIT_RSSI_LOW_VALUE = 1;
    public static final int EXTENDED_SELECTION_BIT_SCAN_INTERVAL_MILLIS = 4;
    public static final int EXTENDED_SELECTION_BIT_SCAN_WINDOW_MILLIS = 5;
    public static final int MATCH_MODE_AGGRESSIVE = 1;
    public static final int MATCH_MODE_STICKY = 2;
    public static final int MATCH_NUM_FEW_ADVERTISEMENT = 2;
    public static final int MATCH_NUM_MAX_ADVERTISEMENT = 3;
    public static final int MATCH_NUM_ONE_ADVERTISEMENT = 1;
    public static final int SCAN_MODE_BALANCED = 1;
    public static final int SCAN_MODE_LOW_LATENCY = 2;
    public static final int SCAN_MODE_LOW_POWER = 0;
    public static final int SCAN_MODE_OPPORTUNISTIC = -1;
    public static final int SCAN_RESULT_TYPE_ABBREVIATED = 1;
    public static final int SCAN_RESULT_TYPE_FULL = 0;
    private static final String TAG = "ScanSettings";
    private int mCallbackType;
    private int mExtendedSelection;
    private int mFilterLogicType;
    private int mListLogicType;
    private int mMatchMode;
    private int mNumOfMatchesPerFilter;
    private long mReportDelayMillis;
    private int mRssiHighValue;
    private int mRssiLowValue;
    private int mScanIntervalMillis;
    private int mScanMode;
    private int mScanResultType;
    private int mScanWindowMillis;

    public static final class Builder {
        private int mCallbackType;
        private int mExtendedSelection;
        private int mFilterLogicType;
        private int mListLogicType;
        private int mMatchMode;
        private int mNumOfMatchesPerFilter;
        private long mReportDelayMillis;
        private int mRssiHighValue;
        private int mRssiLowValue;
        private int mScanIntervalMillis;
        private int mScanMode;
        private int mScanResultType;
        private int mScanWindowMillis;

        public Builder() {
            this.mScanMode = ScanSettings.SCAN_RESULT_TYPE_FULL;
            this.mCallbackType = ScanSettings.SCAN_RESULT_TYPE_ABBREVIATED;
            this.mScanResultType = ScanSettings.SCAN_RESULT_TYPE_FULL;
            this.mReportDelayMillis = 0;
            this.mMatchMode = ScanSettings.SCAN_RESULT_TYPE_ABBREVIATED;
            this.mNumOfMatchesPerFilter = ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT;
            this.mExtendedSelection = ScanSettings.SCAN_RESULT_TYPE_FULL;
            this.mRssiHighValue = ScanSettings.SCAN_RESULT_TYPE_FULL;
            this.mRssiLowValue = ScanSettings.SCAN_RESULT_TYPE_FULL;
            this.mListLogicType = ScanSettings.SCAN_RESULT_TYPE_FULL;
            this.mFilterLogicType = ScanSettings.SCAN_RESULT_TYPE_FULL;
            this.mScanIntervalMillis = ScanSettings.SCAN_RESULT_TYPE_FULL;
            this.mScanWindowMillis = ScanSettings.SCAN_RESULT_TYPE_FULL;
        }

        public Builder setScanMode(int scanMode) {
            if (scanMode < ScanSettings.SCAN_MODE_OPPORTUNISTIC || scanMode > ScanSettings.SCAN_MODE_LOW_LATENCY) {
                throw new IllegalArgumentException("invalid scan mode " + scanMode);
            }
            this.mScanMode = scanMode;
            return this;
        }

        public Builder setCallbackType(int callbackType) {
            if (isValidCallbackType(callbackType)) {
                this.mCallbackType = callbackType;
                return this;
            }
            throw new IllegalArgumentException("invalid callback type - " + callbackType);
        }

        private boolean isValidCallbackType(int callbackType) {
            boolean z = true;
            if (callbackType == ScanSettings.SCAN_RESULT_TYPE_ABBREVIATED || callbackType == ScanSettings.SCAN_MODE_LOW_LATENCY || callbackType == ScanSettings.EXTENDED_SELECTION_BIT_SCAN_INTERVAL_MILLIS) {
                return true;
            }
            if (callbackType != 6) {
                z = false;
            }
            return z;
        }

        public Builder setScanResultType(int scanResultType) {
            if (scanResultType < 0 || scanResultType > ScanSettings.SCAN_RESULT_TYPE_ABBREVIATED) {
                throw new IllegalArgumentException("invalid scanResultType - " + scanResultType);
            }
            this.mScanResultType = scanResultType;
            return this;
        }

        public Builder setReportDelay(long reportDelayMillis) {
            if (reportDelayMillis < 0) {
                throw new IllegalArgumentException("reportDelay must be > 0");
            }
            this.mReportDelayMillis = reportDelayMillis;
            return this;
        }

        public Builder setNumOfMatches(int numOfMatches) {
            if (numOfMatches < ScanSettings.SCAN_RESULT_TYPE_ABBREVIATED || numOfMatches > ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT) {
                throw new IllegalArgumentException("invalid numOfMatches " + numOfMatches);
            }
            this.mNumOfMatchesPerFilter = numOfMatches;
            return this;
        }

        public Builder setMatchMode(int matchMode) {
            if (matchMode < ScanSettings.SCAN_RESULT_TYPE_ABBREVIATED || matchMode > ScanSettings.SCAN_MODE_LOW_LATENCY) {
                throw new IllegalArgumentException("invalid matchMode " + matchMode);
            }
            this.mMatchMode = matchMode;
            return this;
        }

        public Builder setRssiHighValue(int rssi_high_thres) {
            if (rssi_high_thres < -128 || rssi_high_thres > InformationElement.EID_EXTENDED_CAPS) {
                throw new IllegalArgumentException("invalid rssi_high_thres " + rssi_high_thres);
            }
            HwLog.bdati(ScanSettings.TAG, "setRssiHighValue:: " + rssi_high_thres);
            this.mExtendedSelection |= ScanSettings.SCAN_RESULT_TYPE_ABBREVIATED;
            this.mRssiHighValue = rssi_high_thres;
            return this;
        }

        public Builder setRssiLowValue(int rssi_low_thres) {
            if (rssi_low_thres < -128 || rssi_low_thres > InformationElement.EID_EXTENDED_CAPS) {
                throw new IllegalArgumentException("invalid rssi_low_thres " + rssi_low_thres);
            }
            HwLog.bdati(ScanSettings.TAG, "setRssiLowValue:: " + rssi_low_thres);
            this.mExtendedSelection |= ScanSettings.SCAN_MODE_LOW_LATENCY;
            this.mRssiLowValue = rssi_low_thres;
            return this;
        }

        public Builder setListLogicType(int list_logic_type) {
            if (list_logic_type < 0 || list_logic_type > PowerManager.WAKE_LOCK_LEVEL_MASK) {
                throw new IllegalArgumentException("invalid list_logic_type " + list_logic_type);
            }
            HwLog.bdati(ScanSettings.TAG, "setListLogicType:: " + list_logic_type);
            this.mExtendedSelection |= ScanSettings.EXTENDED_SELECTION_BIT_SCAN_INTERVAL_MILLIS;
            this.mListLogicType = list_logic_type;
            return this;
        }

        public Builder setFilterLogicType(int filter_logic_type) {
            if (filter_logic_type < -128 || filter_logic_type > InformationElement.EID_EXTENDED_CAPS) {
                throw new IllegalArgumentException("invalid filter_logic_type " + filter_logic_type);
            }
            HwLog.bdati(ScanSettings.TAG, "setFilterLogicType:: " + filter_logic_type);
            this.mExtendedSelection |= 8;
            this.mFilterLogicType = filter_logic_type;
            return this;
        }

        public Builder setScanIntervalMillis(int scanIntervalMillis) {
            if (scanIntervalMillis < 0) {
                throw new IllegalArgumentException("scanIntervalMillis must be > 0");
            }
            HwLog.bdati(ScanSettings.TAG, "setScanIntervalMillis:: " + scanIntervalMillis);
            this.mExtendedSelection |= 16;
            this.mScanIntervalMillis = scanIntervalMillis;
            return this;
        }

        public Builder setScanWindowMillis(int scanWindowMillis) {
            if (scanWindowMillis < 0) {
                throw new IllegalArgumentException("scanWindowMillis must be > 0");
            }
            HwLog.bdati(ScanSettings.TAG, "setScanWindowMillis:: " + scanWindowMillis);
            this.mExtendedSelection |= 32;
            this.mScanWindowMillis = scanWindowMillis;
            return this;
        }

        public ScanSettings build() {
            return new ScanSettings(this.mCallbackType, this.mScanResultType, this.mReportDelayMillis, this.mMatchMode, this.mNumOfMatchesPerFilter, this.mExtendedSelection, this.mRssiHighValue, this.mRssiLowValue, this.mListLogicType, this.mFilterLogicType, this.mScanIntervalMillis, this.mScanWindowMillis, null);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.bluetooth.le.ScanSettings.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.bluetooth.le.ScanSettings.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.ScanSettings.<clinit>():void");
    }

    public int getExtendedSelection() {
        return this.mExtendedSelection;
    }

    public boolean getExtendedSelection(int selection) {
        return SCAN_RESULT_TYPE_ABBREVIATED == ((this.mExtendedSelection >> selection) & SCAN_RESULT_TYPE_ABBREVIATED);
    }

    public int getRssiHighValue() {
        return this.mRssiHighValue;
    }

    public int getRssiLowValue() {
        return this.mRssiLowValue;
    }

    public int getListLogicType() {
        return this.mListLogicType;
    }

    public int getFilterLogicType() {
        return this.mFilterLogicType;
    }

    public int getScanIntervalMillis() {
        return this.mScanIntervalMillis;
    }

    public int getScanWindowMillis() {
        return this.mScanWindowMillis;
    }

    public int getScanMode() {
        return this.mScanMode;
    }

    public int getCallbackType() {
        return this.mCallbackType;
    }

    public int getScanResultType() {
        return this.mScanResultType;
    }

    public int getMatchMode() {
        return this.mMatchMode;
    }

    public int getNumOfMatches() {
        return this.mNumOfMatchesPerFilter;
    }

    public long getReportDelayMillis() {
        return this.mReportDelayMillis;
    }

    private ScanSettings(int scanMode, int callbackType, int scanResultType, long reportDelayMillis, int matchMode, int numOfMatchesPerFilter, int extended_selection, int rssi_high_thres, int rssi_low_thres, int list_logic_type, int filter_logic_type, int scan_interval_millis, int scan_window_millis) {
        this.mExtendedSelection = SCAN_RESULT_TYPE_FULL;
        this.mScanMode = scanMode;
        this.mCallbackType = callbackType;
        this.mScanResultType = scanResultType;
        this.mReportDelayMillis = reportDelayMillis;
        this.mNumOfMatchesPerFilter = numOfMatchesPerFilter;
        this.mMatchMode = matchMode;
        this.mExtendedSelection = extended_selection;
        this.mRssiHighValue = rssi_high_thres;
        this.mRssiLowValue = rssi_low_thres;
        this.mListLogicType = list_logic_type;
        this.mFilterLogicType = filter_logic_type;
        this.mScanIntervalMillis = scan_interval_millis;
        this.mScanWindowMillis = scan_window_millis;
    }

    private ScanSettings(Parcel in) {
        this.mExtendedSelection = SCAN_RESULT_TYPE_FULL;
        this.mScanMode = in.readInt();
        this.mCallbackType = in.readInt();
        this.mScanResultType = in.readInt();
        this.mReportDelayMillis = in.readLong();
        this.mMatchMode = in.readInt();
        this.mNumOfMatchesPerFilter = in.readInt();
        this.mExtendedSelection = in.readInt();
        this.mRssiHighValue = in.readInt();
        this.mRssiLowValue = in.readInt();
        this.mListLogicType = in.readInt();
        this.mFilterLogicType = in.readInt();
        this.mScanIntervalMillis = in.readInt();
        this.mScanWindowMillis = in.readInt();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mScanMode);
        dest.writeInt(this.mCallbackType);
        dest.writeInt(this.mScanResultType);
        dest.writeLong(this.mReportDelayMillis);
        dest.writeInt(this.mMatchMode);
        dest.writeInt(this.mNumOfMatchesPerFilter);
        dest.writeInt(this.mExtendedSelection);
        dest.writeInt(this.mRssiHighValue);
        dest.writeInt(this.mRssiLowValue);
        dest.writeInt(this.mListLogicType);
        dest.writeInt(this.mFilterLogicType);
        dest.writeInt(this.mScanIntervalMillis);
        dest.writeInt(this.mScanWindowMillis);
    }

    public int describeContents() {
        return SCAN_RESULT_TYPE_FULL;
    }
}
