package android.bluetooth.le;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;

public final class ScanSettings implements Parcelable {
    public static final int CALLBACK_TYPE_ALL_MATCHES = 1;
    public static final int CALLBACK_TYPE_FIRST_MATCH = 2;
    public static final int CALLBACK_TYPE_MATCH_LOST = 4;
    public static final Parcelable.Creator<ScanSettings> CREATOR = new Parcelable.Creator<ScanSettings>() {
        public ScanSettings[] newArray(int size) {
            return new ScanSettings[size];
        }

        public ScanSettings createFromParcel(Parcel in) {
            return new ScanSettings(in);
        }
    };
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
    public static final int PHY_LE_ALL_SUPPORTED = 255;
    public static final int SCAN_MODE_BALANCED = 1;
    public static final int SCAN_MODE_LOW_LATENCY = 2;
    public static final int SCAN_MODE_LOW_POWER = 0;
    public static final int SCAN_MODE_OPPORTUNISTIC = -1;
    @SystemApi
    public static final int SCAN_RESULT_TYPE_ABBREVIATED = 1;
    @SystemApi
    public static final int SCAN_RESULT_TYPE_FULL = 0;
    private static final String TAG = "ScanSettings";
    private int mCallbackType;
    private int mExtendedSelection;
    private int mFilterLogicType;
    private boolean mLegacy;
    private int mListLogicType;
    private int mMatchMode;
    private int mNumOfMatchesPerFilter;
    private int mPhy;
    private long mReportDelayMillis;
    private int mRssiHighValue;
    private int mRssiLowValue;
    private int mScanIntervalMillis;
    private int mScanMode;
    private int mScanResultType;
    private int mScanWindowMillis;

    public static final class Builder {
        private int mCallbackType = 1;
        private int mExtendedSelection = 0;
        private int mFilterLogicType = 0;
        private boolean mLegacy = true;
        private int mListLogicType = 0;
        private int mMatchMode = 1;
        private int mNumOfMatchesPerFilter = 3;
        private int mPhy = 1;
        private long mReportDelayMillis = 0;
        private int mRssiHighValue = 0;
        private int mRssiLowValue = 0;
        private int mScanIntervalMillis = 0;
        private int mScanMode = 0;
        private int mScanResultType = 0;
        private int mScanWindowMillis = 0;

        public Builder setScanMode(int scanMode) {
            if (scanMode < -1 || scanMode > 2) {
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
            if (callbackType == 1 || callbackType == 2 || callbackType == 4) {
                return true;
            }
            if (callbackType != 6) {
                z = false;
            }
            return z;
        }

        @SystemApi
        public Builder setScanResultType(int scanResultType) {
            if (scanResultType < 0 || scanResultType > 1) {
                throw new IllegalArgumentException("invalid scanResultType - " + scanResultType);
            }
            this.mScanResultType = scanResultType;
            return this;
        }

        public Builder setReportDelay(long reportDelayMillis) {
            if (reportDelayMillis >= 0) {
                this.mReportDelayMillis = reportDelayMillis;
                return this;
            }
            throw new IllegalArgumentException("reportDelay must be > 0");
        }

        public Builder setNumOfMatches(int numOfMatches) {
            if (numOfMatches < 1 || numOfMatches > 3) {
                throw new IllegalArgumentException("invalid numOfMatches " + numOfMatches);
            }
            this.mNumOfMatchesPerFilter = numOfMatches;
            return this;
        }

        public Builder setMatchMode(int matchMode) {
            if (matchMode < 1 || matchMode > 2) {
                throw new IllegalArgumentException("invalid matchMode " + matchMode);
            }
            this.mMatchMode = matchMode;
            return this;
        }

        public Builder setRssiHighValue(int rssi_high_thres) {
            if (rssi_high_thres < -128 || rssi_high_thres > 127) {
                throw new IllegalArgumentException("invalid rssi_high_thres " + rssi_high_thres);
            }
            this.mExtendedSelection |= 1;
            this.mRssiHighValue = rssi_high_thres;
            return this;
        }

        public Builder setRssiLowValue(int rssi_low_thres) {
            if (rssi_low_thres < -128 || rssi_low_thres > 127) {
                throw new IllegalArgumentException("invalid rssi_low_thres " + rssi_low_thres);
            }
            this.mExtendedSelection |= 2;
            this.mRssiLowValue = rssi_low_thres;
            return this;
        }

        public Builder setListLogicType(int list_logic_type) {
            if (list_logic_type < 0 || list_logic_type > 65535) {
                throw new IllegalArgumentException("invalid list_logic_type " + list_logic_type);
            }
            this.mExtendedSelection |= 4;
            this.mListLogicType = list_logic_type;
            return this;
        }

        public Builder setFilterLogicType(int filter_logic_type) {
            if (filter_logic_type < -128 || filter_logic_type > 127) {
                throw new IllegalArgumentException("invalid filter_logic_type " + filter_logic_type);
            }
            this.mExtendedSelection |= 8;
            this.mFilterLogicType = filter_logic_type;
            return this;
        }

        public Builder setScanIntervalMillis(int scanIntervalMillis) {
            if (scanIntervalMillis >= 0) {
                this.mExtendedSelection |= 16;
                this.mScanIntervalMillis = scanIntervalMillis;
                return this;
            }
            throw new IllegalArgumentException("scanIntervalMillis must be > 0");
        }

        public Builder setScanWindowMillis(int scanWindowMillis) {
            if (scanWindowMillis >= 0) {
                this.mExtendedSelection |= 32;
                this.mScanWindowMillis = scanWindowMillis;
                return this;
            }
            throw new IllegalArgumentException("scanWindowMillis must be > 0");
        }

        public Builder setLegacy(boolean legacy) {
            this.mLegacy = legacy;
            return this;
        }

        public Builder setPhy(int phy) {
            this.mPhy = phy;
            return this;
        }

        public ScanSettings build() {
            int i = this.mScanMode;
            int i2 = this.mCallbackType;
            int i3 = this.mScanResultType;
            long j = this.mReportDelayMillis;
            int i4 = this.mMatchMode;
            int i5 = this.mNumOfMatchesPerFilter;
            boolean z = this.mLegacy;
            int i6 = this.mPhy;
            int i7 = this.mExtendedSelection;
            int i8 = this.mRssiHighValue;
            int i9 = this.mRssiLowValue;
            int i10 = this.mListLogicType;
            int i11 = this.mFilterLogicType;
            int i12 = i11;
            ScanSettings scanSettings = new ScanSettings(i, i2, i3, j, i4, i5, z, i6, i7, i8, i9, i10, i12, this.mScanIntervalMillis, this.mScanWindowMillis);
            return scanSettings;
        }
    }

    public int getExtendedSelection() {
        return this.mExtendedSelection;
    }

    public boolean getExtendedSelection(int selection) {
        return 1 == ((this.mExtendedSelection >> selection) & 1);
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

    public void setScanIntervalMillis(int scanIntervalMillis) {
        this.mScanIntervalMillis = scanIntervalMillis;
    }

    public void setScanWindowMillis(int scanWindowMillis) {
        this.mScanWindowMillis = scanWindowMillis;
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

    public boolean getLegacy() {
        return this.mLegacy;
    }

    public int getPhy() {
        return this.mPhy;
    }

    public long getReportDelayMillis() {
        return this.mReportDelayMillis;
    }

    private ScanSettings(int scanMode, int callbackType, int scanResultType, long reportDelayMillis, int matchMode, int numOfMatchesPerFilter, boolean legacy, int phy, int extended_selection, int rssi_high_thres, int rssi_low_thres, int list_logic_type, int filter_logic_type, int scan_interval_millis, int scan_window_millis) {
        this.mExtendedSelection = 0;
        this.mScanMode = scanMode;
        this.mCallbackType = callbackType;
        this.mScanResultType = scanResultType;
        this.mReportDelayMillis = reportDelayMillis;
        this.mNumOfMatchesPerFilter = numOfMatchesPerFilter;
        this.mMatchMode = matchMode;
        this.mLegacy = legacy;
        this.mPhy = phy;
        this.mExtendedSelection = extended_selection;
        this.mRssiHighValue = rssi_high_thres;
        this.mRssiLowValue = rssi_low_thres;
        this.mListLogicType = list_logic_type;
        this.mFilterLogicType = filter_logic_type;
        this.mScanIntervalMillis = scan_interval_millis;
        this.mScanWindowMillis = scan_window_millis;
    }

    private ScanSettings(Parcel in) {
        boolean z = false;
        this.mExtendedSelection = 0;
        this.mScanMode = in.readInt();
        this.mCallbackType = in.readInt();
        this.mScanResultType = in.readInt();
        this.mReportDelayMillis = in.readLong();
        this.mMatchMode = in.readInt();
        this.mNumOfMatchesPerFilter = in.readInt();
        this.mLegacy = in.readInt() != 0 ? true : z;
        this.mPhy = in.readInt();
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
        dest.writeInt(this.mLegacy ? 1 : 0);
        dest.writeInt(this.mPhy);
        dest.writeInt(this.mExtendedSelection);
        dest.writeInt(this.mRssiHighValue);
        dest.writeInt(this.mRssiLowValue);
        dest.writeInt(this.mListLogicType);
        dest.writeInt(this.mFilterLogicType);
        dest.writeInt(this.mScanIntervalMillis);
        dest.writeInt(this.mScanWindowMillis);
    }

    public int describeContents() {
        return 0;
    }
}
