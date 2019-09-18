package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;

public final class NetworkScanRequest implements Parcelable {
    public static final Parcelable.Creator<NetworkScanRequest> CREATOR = new Parcelable.Creator<NetworkScanRequest>() {
        public NetworkScanRequest createFromParcel(Parcel in) {
            return new NetworkScanRequest(in);
        }

        public NetworkScanRequest[] newArray(int size) {
            return new NetworkScanRequest[size];
        }
    };
    public static final int MAX_BANDS = 8;
    public static final int MAX_CHANNELS = 32;
    public static final int MAX_INCREMENTAL_PERIODICITY_SEC = 10;
    public static final int MAX_MCC_MNC_LIST_SIZE = 20;
    public static final int MAX_RADIO_ACCESS_NETWORKS = 8;
    public static final int MAX_SEARCH_MAX_SEC = 3600;
    public static final int MAX_SEARCH_PERIODICITY_SEC = 300;
    public static final int MIN_INCREMENTAL_PERIODICITY_SEC = 1;
    public static final int MIN_SEARCH_MAX_SEC = 60;
    public static final int MIN_SEARCH_PERIODICITY_SEC = 5;
    public static final int SCAN_TYPE_ONE_SHOT = 0;
    public static final int SCAN_TYPE_PERIODIC = 1;
    private boolean mIncrementalResults;
    private int mIncrementalResultsPeriodicity;
    private int mMaxSearchTime;
    private ArrayList<String> mMccMncs;
    private int mScanType;
    private int mSearchPeriodicity;
    private RadioAccessSpecifier[] mSpecifiers;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ScanType {
    }

    public NetworkScanRequest(int scanType, RadioAccessSpecifier[] specifiers, int searchPeriodicity, int maxSearchTime, boolean incrementalResults, int incrementalResultsPeriodicity, ArrayList<String> mccMncs) {
        this.mScanType = scanType;
        if (specifiers != null) {
            this.mSpecifiers = (RadioAccessSpecifier[]) specifiers.clone();
        } else {
            this.mSpecifiers = null;
        }
        this.mSearchPeriodicity = searchPeriodicity;
        this.mMaxSearchTime = maxSearchTime;
        this.mIncrementalResults = incrementalResults;
        this.mIncrementalResultsPeriodicity = incrementalResultsPeriodicity;
        if (mccMncs != null) {
            this.mMccMncs = (ArrayList) mccMncs.clone();
        } else {
            this.mMccMncs = new ArrayList<>();
        }
    }

    public int getScanType() {
        return this.mScanType;
    }

    public int getSearchPeriodicity() {
        return this.mSearchPeriodicity;
    }

    public int getMaxSearchTime() {
        return this.mMaxSearchTime;
    }

    public boolean getIncrementalResults() {
        return this.mIncrementalResults;
    }

    public int getIncrementalResultsPeriodicity() {
        return this.mIncrementalResultsPeriodicity;
    }

    public RadioAccessSpecifier[] getSpecifiers() {
        if (this.mSpecifiers == null) {
            return null;
        }
        return (RadioAccessSpecifier[]) this.mSpecifiers.clone();
    }

    public ArrayList<String> getPlmns() {
        return (ArrayList) this.mMccMncs.clone();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mScanType);
        dest.writeParcelableArray(this.mSpecifiers, flags);
        dest.writeInt(this.mSearchPeriodicity);
        dest.writeInt(this.mMaxSearchTime);
        dest.writeBoolean(this.mIncrementalResults);
        dest.writeInt(this.mIncrementalResultsPeriodicity);
        dest.writeStringList(this.mMccMncs);
    }

    private NetworkScanRequest(Parcel in) {
        this.mScanType = in.readInt();
        this.mSpecifiers = (RadioAccessSpecifier[]) in.readParcelableArray(Object.class.getClassLoader(), RadioAccessSpecifier.class);
        this.mSearchPeriodicity = in.readInt();
        this.mMaxSearchTime = in.readInt();
        this.mIncrementalResults = in.readBoolean();
        this.mIncrementalResultsPeriodicity = in.readInt();
        this.mMccMncs = new ArrayList<>();
        in.readStringList(this.mMccMncs);
    }

    public boolean equals(Object o) {
        boolean z = false;
        try {
            NetworkScanRequest nsr = (NetworkScanRequest) o;
            if (o == null) {
                return false;
            }
            if (this.mScanType == nsr.mScanType && Arrays.equals(this.mSpecifiers, nsr.mSpecifiers) && this.mSearchPeriodicity == nsr.mSearchPeriodicity && this.mMaxSearchTime == nsr.mMaxSearchTime && this.mIncrementalResults == nsr.mIncrementalResults && this.mIncrementalResultsPeriodicity == nsr.mIncrementalResultsPeriodicity && this.mMccMncs != null && this.mMccMncs.equals(nsr.mMccMncs)) {
                z = true;
            }
            return z;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public int hashCode() {
        int hashCode = (this.mScanType * 31) + (Arrays.hashCode(this.mSpecifiers) * 37) + (this.mSearchPeriodicity * 41) + (this.mMaxSearchTime * 43);
        int i = 1;
        if (!this.mIncrementalResults) {
            i = 0;
        }
        return hashCode + (i * 47) + (this.mIncrementalResultsPeriodicity * 53) + (this.mMccMncs.hashCode() * 59);
    }
}
