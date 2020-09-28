package com.mediatek.ims;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;

public class MtkImsCallForwardInfo implements Parcelable {
    public static final Parcelable.Creator<MtkImsCallForwardInfo> CREATOR = new Parcelable.Creator<MtkImsCallForwardInfo>() {
        /* class com.mediatek.ims.MtkImsCallForwardInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MtkImsCallForwardInfo createFromParcel(Parcel in) {
            return new MtkImsCallForwardInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public MtkImsCallForwardInfo[] newArray(int size) {
            return new MtkImsCallForwardInfo[size];
        }
    };
    public int mCondition;
    public String mNumber;
    public int mServiceClass;
    public int mStatus;
    public int mTimeSeconds;
    public long[] mTimeSlot;
    public int mToA;

    public MtkImsCallForwardInfo() {
    }

    public MtkImsCallForwardInfo(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mCondition);
        out.writeInt(this.mStatus);
        out.writeInt(this.mServiceClass);
        out.writeInt(this.mToA);
        out.writeString(this.mNumber);
        out.writeInt(this.mTimeSeconds);
        out.writeLongArray(this.mTimeSlot);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(", Condition: ");
        sb.append(this.mCondition);
        sb.append(", Status: ");
        sb.append(this.mStatus == 0 ? "disabled" : "enabled");
        sb.append(", ServiceClass: ");
        sb.append(this.mServiceClass);
        sb.append(", ToA: ");
        sb.append(this.mToA);
        sb.append(", Number=");
        sb.append(this.mNumber);
        sb.append(", Time (seconds): ");
        sb.append(this.mTimeSeconds);
        sb.append(", timeSlot: ");
        sb.append(Arrays.toString(this.mTimeSlot));
        return sb.toString();
    }

    private void readFromParcel(Parcel in) {
        this.mCondition = in.readInt();
        this.mStatus = in.readInt();
        this.mServiceClass = in.readInt();
        this.mToA = in.readInt();
        this.mNumber = in.readString();
        this.mTimeSeconds = in.readInt();
        this.mTimeSlot = new long[2];
        try {
            in.readLongArray(this.mTimeSlot);
        } catch (RuntimeException e) {
            this.mTimeSlot = null;
        }
    }
}
