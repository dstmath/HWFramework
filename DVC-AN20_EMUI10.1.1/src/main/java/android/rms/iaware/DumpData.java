package android.rms.iaware;

import android.os.Parcel;
import android.os.Parcelable;

public class DumpData implements Parcelable, Comparable<DumpData> {
    public static final Parcelable.Creator<DumpData> CREATOR = new Parcelable.Creator<DumpData>() {
        /* class android.rms.iaware.DumpData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DumpData createFromParcel(Parcel source) {
            return new DumpData(source.readLong(), source.readInt(), source.readString(), source.readInt(), source.readString());
        }

        @Override // android.os.Parcelable.Creator
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
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        if (this == other) {
            return true;
        }
        DumpData otherData = (DumpData) other;
        if (this.mOperationTimeStamp == otherData.getTime() && this.mFeatureId == otherData.getFeatureId() && this.mExeTime == otherData.getExeTime() && compareObject(this.mOperation, otherData.getOperation()) && compareObject(this.mReason, otherData.getReson())) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        long j = this.mOperationTimeStamp;
        int result = ((((1 * 31) + ((int) (j ^ (j >>> 32)))) * 31) + this.mFeatureId) * 31;
        String str = this.mOperation;
        int i = 0;
        int result2 = (((result + (str != null ? str.hashCode() : 0)) * 31) + this.mExeTime) * 31;
        String str2 = this.mReason;
        if (str2 != null) {
            i = str2.hashCode();
        }
        return result2 + i;
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
        if (object1 == object2) {
            return true;
        }
        if (object1 == null || !object1.equals(object2)) {
            return false;
        }
        return true;
    }
}
