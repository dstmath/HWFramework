package android.rms.iaware;

import android.os.Parcel;
import android.os.Parcelable;

public class DumpData implements Parcelable, Comparable<DumpData> {
    public static final Parcelable.Creator<DumpData> CREATOR = new Parcelable.Creator<DumpData>() {
        public DumpData createFromParcel(Parcel source) {
            DumpData dumpData = new DumpData(source.readLong(), source.readInt(), source.readString(), source.readInt(), source.readString());
            return dumpData;
        }

        public DumpData[] newArray(int size) {
            return new DumpData[size];
        }
    };
    private int mExeTime;
    private int mFeatureId;
    private String mOperation;
    private long mOperationTimeStamp;
    private String mReason;

    public DumpData(long time, int featureid, String operation, int exetime, String reason) {
        this.mOperationTimeStamp = time;
        this.mFeatureId = featureid;
        this.mOperation = operation;
        this.mExeTime = exetime;
        this.mReason = reason;
    }

    public int compareTo(DumpData other) {
        if (other == null) {
            return 1;
        }
        if (this.mOperationTimeStamp < other.getTime()) {
            return -1;
        }
        if (this.mOperationTimeStamp > other.getTime()) {
            return 1;
        }
        return 0;
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        if (this == other) {
            return true;
        }
        DumpData otherData = (DumpData) other;
        if (this.mOperationTimeStamp == otherData.getTime() && this.mFeatureId == otherData.getFeatureId() && this.mExeTime == otherData.getExeTime() && compareObject(this.mOperation, otherData.getOperation()) && compareObject(this.mReason, otherData.getReson())) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = 31 * ((31 * ((31 * ((31 * ((31 * 1) + ((int) (this.mOperationTimeStamp ^ (this.mOperationTimeStamp >>> 32))))) + this.mFeatureId)) + (this.mOperation != null ? this.mOperation.hashCode() : 0))) + this.mExeTime);
        if (this.mReason != null) {
            i = this.mReason.hashCode();
        }
        return hashCode + i;
    }

    public long getTime() {
        return this.mOperationTimeStamp;
    }

    public void setTime(long time) {
        this.mOperationTimeStamp = time;
    }

    public int getFeatureId() {
        return this.mFeatureId;
    }

    public void setFeatureId(int featureId) {
        this.mFeatureId = featureId;
    }

    public String getOperation() {
        return this.mOperation;
    }

    public void setOperation(String operation) {
        this.mOperation = operation;
    }

    public String getReson() {
        return this.mReason;
    }

    public void setReason(String reason) {
        this.mReason = reason;
    }

    public int getExeTime() {
        return this.mExeTime;
    }

    public void setExeTime(int exetime) {
        this.mExeTime = exetime;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mOperationTimeStamp);
        dest.writeInt(this.mFeatureId);
        dest.writeString(this.mOperation);
        dest.writeInt(this.mExeTime);
        dest.writeString(this.mReason);
    }

    public int describeContents() {
        return 0;
    }

    private boolean compareObject(Object object1, Object object2) {
        boolean z = true;
        if (object1 == object2) {
            return true;
        }
        if (object1 == null || !object1.equals(object2)) {
            z = false;
        }
        return z;
    }
}
