package com.huawei.msdp.movement;

import android.os.Parcel;
import android.os.Parcelable;

public class HwMSDPMovementEvent implements Parcelable {
    public static final Parcelable.Creator<HwMSDPMovementEvent> CREATOR = new Parcelable.Creator<HwMSDPMovementEvent>() {
        /* class com.huawei.msdp.movement.HwMSDPMovementEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwMSDPMovementEvent createFromParcel(Parcel source) {
            return new HwMSDPMovementEvent(source.readString(), source.readInt(), source.readLong(), source.readInt(), (HwMSDPOtherParameters) source.readParcelable(HwMSDPOtherParameters.class.getClassLoader()));
        }

        @Override // android.os.Parcelable.Creator
        public HwMSDPMovementEvent[] newArray(int size) {
            return new HwMSDPMovementEvent[size];
        }
    };
    private final int mConfidence;
    private final int mEventType;
    private final String mMovement;
    private final HwMSDPOtherParameters mOtherParams;
    private final long mTimestampNs;

    public HwMSDPMovementEvent(String movement, int type, long time, int confidence, HwMSDPOtherParameters param) {
        this.mMovement = movement;
        this.mEventType = type;
        this.mTimestampNs = time;
        this.mConfidence = confidence;
        this.mOtherParams = param;
    }

    public String getMovement() {
        return this.mMovement;
    }

    public int getEventType() {
        return this.mEventType;
    }

    public long getTimestampNs() {
        return this.mTimestampNs;
    }

    public int getConfidence() {
        return this.mConfidence;
    }

    public HwMSDPOtherParameters getmOtherParams() {
        return this.mOtherParams;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mMovement);
        parcel.writeInt(this.mEventType);
        parcel.writeLong(this.mTimestampNs);
        parcel.writeInt(this.mConfidence);
        parcel.writeParcelable(this.mOtherParams, flags);
    }

    @Override // java.lang.Object
    public String toString() {
        return "";
    }
}
