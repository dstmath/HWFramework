package android.rms.iaware;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
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

    public DumpData(long time, int featureId, String operation, int exeTime, String reason) {
        this.mOperationTimeStamp = time;
        this.mFeatureId = featureId;
        this.mOperation = operation;
        this.mExeTime = exeTime;
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

    @Override // java.lang.Object
    public boolean equals(Object other) {
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!(other instanceof DumpData)) {
            return false;
        }
        DumpData otherData = (DumpData) other;
        if (this.mOperationTimeStamp == otherData.getTime() && this.mFeatureId == otherData.getFeatureId() && this.mExeTime == otherData.getExeTime() && this.mOperation.equals(otherData.getOperation()) && this.mReason.equals(otherData.getReason())) {
            return true;
        }
        return false;
    }

    @Override // java.lang.Object
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

    public String getReason() {
        return this.mReason;
    }

    public void setReason(String reason) {
        this.mReason = reason;
    }

    public int getExeTime() {
        return this.mExeTime;
    }

    public void setExeTime(int exeTime) {
        this.mExeTime = exeTime;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mOperationTimeStamp);
        dest.writeInt(this.mFeatureId);
        dest.writeString(this.mOperation);
        dest.writeInt(this.mExeTime);
        dest.writeString(this.mReason);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
