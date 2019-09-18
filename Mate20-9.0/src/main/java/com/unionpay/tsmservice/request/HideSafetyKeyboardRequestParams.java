package com.unionpay.tsmservice.request;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class HideSafetyKeyboardRequestParams extends RequestParams {
    public static final Parcelable.Creator<HideSafetyKeyboardRequestParams> CREATOR = new Parcelable.Creator<HideSafetyKeyboardRequestParams>() {
        public final HideSafetyKeyboardRequestParams createFromParcel(Parcel parcel) {
            return new HideSafetyKeyboardRequestParams(parcel);
        }

        public final HideSafetyKeyboardRequestParams[] newArray(int i) {
            return new HideSafetyKeyboardRequestParams[i];
        }
    };
    private Bundle mParams;

    public HideSafetyKeyboardRequestParams() {
    }

    public HideSafetyKeyboardRequestParams(Parcel parcel) {
        super(parcel);
        this.mParams = parcel.readBundle();
    }

    public int describeContents() {
        return 0;
    }

    public Bundle getParams() {
        return this.mParams;
    }

    public void setParams(Bundle bundle) {
        this.mParams = bundle;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeBundle(this.mParams);
    }
}
