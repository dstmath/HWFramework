package com.android.ims;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ImsCallForwardInfo implements Parcelable {
    public static final Creator<ImsCallForwardInfo> CREATOR = new Creator<ImsCallForwardInfo>() {
        public ImsCallForwardInfo createFromParcel(Parcel in) {
            return new ImsCallForwardInfo(in);
        }

        public ImsCallForwardInfo[] newArray(int size) {
            return new ImsCallForwardInfo[size];
        }
    };
    public int mCondition;
    public int mEndHour = 0;
    public int mEndMinute = 0;
    public String mNumber;
    public int mServiceClass;
    public int mStartHour = 0;
    public int mStartMinute = 0;
    public int mStatus;
    public int mTimeSeconds;
    public int mToA;

    public ImsCallForwardInfo(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mCondition);
        out.writeInt(this.mStatus);
        out.writeInt(this.mToA);
        out.writeString(this.mNumber);
        out.writeInt(this.mTimeSeconds);
        out.writeInt(this.mServiceClass);
    }

    public String toString() {
        return super.toString() + ", Condition: " + this.mCondition + ", Status: " + (this.mStatus == 0 ? "disabled" : "enabled") + ", ToA: " + this.mToA + ", Service Class: " + this.mServiceClass + ", Number=" + this.mNumber + ", Time (seconds): " + this.mTimeSeconds + ", mStartHour=" + this.mStartHour + ", mStartMinute=" + this.mStartMinute + ", mEndHour=" + this.mEndHour + ", mEndMinute" + this.mEndMinute;
    }

    private void readFromParcel(Parcel in) {
        this.mCondition = in.readInt();
        this.mStatus = in.readInt();
        this.mToA = in.readInt();
        this.mNumber = in.readString();
        this.mTimeSeconds = in.readInt();
        this.mServiceClass = in.readInt();
    }
}
