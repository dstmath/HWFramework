package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;

public class SetSamsungDefWalletRequestParams extends RequestParams {
    public static final Parcelable.Creator<SetSamsungDefWalletRequestParams> CREATOR = new Parcelable.Creator<SetSamsungDefWalletRequestParams>() {
        public final SetSamsungDefWalletRequestParams createFromParcel(Parcel parcel) {
            return new SetSamsungDefWalletRequestParams(parcel);
        }

        public final SetSamsungDefWalletRequestParams[] newArray(int i) {
            return new SetSamsungDefWalletRequestParams[i];
        }
    };

    public SetSamsungDefWalletRequestParams() {
    }

    public SetSamsungDefWalletRequestParams(Parcel parcel) {
        super(parcel);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
    }
}
