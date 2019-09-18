package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;

public class CheckSSamsungPayRequestParams extends RequestParams {
    public static final Parcelable.Creator<CheckSSamsungPayRequestParams> CREATOR = new Parcelable.Creator<CheckSSamsungPayRequestParams>() {
        public final CheckSSamsungPayRequestParams createFromParcel(Parcel parcel) {
            return new CheckSSamsungPayRequestParams(parcel);
        }

        public final CheckSSamsungPayRequestParams[] newArray(int i) {
            return new CheckSSamsungPayRequestParams[i];
        }
    };

    public CheckSSamsungPayRequestParams() {
    }

    public CheckSSamsungPayRequestParams(Parcel parcel) {
        super(parcel);
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
    }
}
