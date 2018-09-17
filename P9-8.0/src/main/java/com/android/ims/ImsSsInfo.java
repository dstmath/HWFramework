package com.android.ims;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ImsSsInfo implements Parcelable {
    public static final Creator<ImsSsInfo> CREATOR = new Creator<ImsSsInfo>() {
        public ImsSsInfo createFromParcel(Parcel in) {
            return new ImsSsInfo(in);
        }

        public ImsSsInfo[] newArray(int size) {
            return new ImsSsInfo[size];
        }
    };
    public static final int DISABLED = 0;
    public static final int ENABLED = 1;
    public static final int NOT_REGISTERED = -1;
    public String mIcbNum;
    public int mStatus;

    public ImsSsInfo(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mStatus);
        out.writeString(this.mIcbNum);
    }

    public String toString() {
        return super.toString() + ", Status: " + (this.mStatus == 0 ? "disabled" : "enabled");
    }

    private void readFromParcel(Parcel in) {
        this.mStatus = in.readInt();
        this.mIcbNum = in.readString();
    }
}
