package com.huawei.IntelliServer.intellilib;

import android.os.Parcel;
import android.os.Parcelable;

public class IntelliParameter implements Parcelable {
    public static final Parcelable.Creator<IntelliParameter> CREATOR = new Parcelable.Creator<IntelliParameter>() {
        /* class com.huawei.IntelliServer.intellilib.IntelliParameter.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IntelliParameter createFromParcel(Parcel in) {
            return new IntelliParameter(in);
        }

        @Override // android.os.Parcelable.Creator
        public IntelliParameter[] newArray(int size) {
            return new IntelliParameter[size];
        }
    };

    protected IntelliParameter(Parcel in) {
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
    }
}
