package com.unionpay.tsmservice.result;

import android.os.Parcel;
import android.os.Parcelable;

public class CheckSSamsungPayResult implements Parcelable {
    public static final Parcelable.Creator<CheckSSamsungPayResult> CREATOR = new Parcelable.Creator<CheckSSamsungPayResult>() {
        public final CheckSSamsungPayResult createFromParcel(Parcel parcel) {
            return new CheckSSamsungPayResult(parcel);
        }

        public final CheckSSamsungPayResult[] newArray(int i) {
            return new CheckSSamsungPayResult[i];
        }
    };

    public CheckSSamsungPayResult() {
    }

    public CheckSSamsungPayResult(Parcel parcel) {
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
    }
}
