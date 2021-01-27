package com.huawei.nearbysdk.closeRange;

import android.os.Parcel;
import android.os.Parcelable;

public class CloseRangeConfigInstance implements Parcelable {
    public static final Parcelable.Creator<CloseRangeConfigInstance> CREATOR = new Parcelable.Creator<CloseRangeConfigInstance>() {
        /* class com.huawei.nearbysdk.closeRange.CloseRangeConfigInstance.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CloseRangeConfigInstance createFromParcel(Parcel source) {
            return new CloseRangeConfigInstance(source.readString(), source.readString(), source.readInt(), source.readInt(), source.readInt(), source.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public CloseRangeConfigInstance[] newArray(int size) {
            return new CloseRangeConfigInstance[size];
        }
    };
    private static final String TAG = "CloseRangeConfigInstance";
    private final int findNearbyCount;
    private final int findNearbyTime;
    private final String modelId;
    private final String name;
    private final int rssiLowerLimit;
    private final int rssiReference;

    public CloseRangeConfigInstance(String name2, String modelId2, int rssiReference2, int rssiLowerLimit2, int findNearbyTime2, int findNearbyCount2) {
        this.name = name2;
        this.modelId = modelId2;
        this.rssiReference = rssiReference2;
        this.rssiLowerLimit = rssiLowerLimit2;
        this.findNearbyTime = findNearbyTime2;
        this.findNearbyCount = findNearbyCount2;
    }

    public String getName() {
        return this.name;
    }

    public String getModelId() {
        return this.modelId;
    }

    public int getRssiReference() {
        return this.rssiReference;
    }

    public int getRssiLowerLimit() {
        return this.rssiLowerLimit;
    }

    public int getFindNearbyTime() {
        return this.findNearbyTime;
    }

    public int getFindNearbyCount() {
        return this.findNearbyCount;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.modelId);
        dest.writeInt(this.rssiReference);
        dest.writeInt(this.rssiLowerLimit);
        dest.writeInt(this.findNearbyTime);
        dest.writeInt(this.findNearbyCount);
    }

    @Override // java.lang.Object
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CloseRangeConfigInstance)) {
            return false;
        }
        CloseRangeConfigInstance that = (CloseRangeConfigInstance) o;
        if (getRssiReference() != that.getRssiReference() || !getName().equals(that.getName())) {
            return false;
        }
        return getModelId().equals(that.getModelId());
    }

    @Override // java.lang.Object
    public int hashCode() {
        return (((((((((getName().hashCode() * 31) + getModelId().hashCode()) * 31) + getRssiReference()) * 31) + getRssiLowerLimit()) * 31) + getFindNearbyTime()) * 31) + getFindNearbyCount();
    }

    @Override // java.lang.Object
    public String toString() {
        return "CloseRangeConfigInstance{name='" + this.name + "', modelId='" + this.modelId + "', rssiReference=" + this.rssiReference + ", rssiLowerLimit=" + this.rssiLowerLimit + ", FindNearbyTime=" + this.findNearbyTime + ", FindNearbyCount=" + this.findNearbyCount + '}';
    }
}
