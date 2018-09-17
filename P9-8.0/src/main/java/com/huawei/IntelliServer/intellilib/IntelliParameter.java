package com.huawei.IntelliServer.intellilib;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class IntelliParameter implements Parcelable {
    public static final Creator<IntelliParameter> CREATOR = new Creator<IntelliParameter>() {
        public IntelliParameter createFromParcel(Parcel in) {
            return new IntelliParameter(in);
        }

        public IntelliParameter[] newArray(int size) {
            return new IntelliParameter[size];
        }
    };

    protected IntelliParameter(Parcel in) {
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
    }
}
