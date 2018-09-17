package com.android.internal.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class DcParamObject implements Parcelable {
    public static final Creator<DcParamObject> CREATOR = new Creator<DcParamObject>() {
        public DcParamObject createFromParcel(Parcel in) {
            return new DcParamObject(in);
        }

        public DcParamObject[] newArray(int size) {
            return new DcParamObject[size];
        }
    };
    private int mSubId;

    public DcParamObject(int subId) {
        this.mSubId = subId;
    }

    public DcParamObject(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSubId);
    }

    private void readFromParcel(Parcel in) {
        this.mSubId = in.readInt();
    }

    public int getSubId() {
        return this.mSubId;
    }
}
