package com.unionpay.tsmservice.request;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class ClearEncryptDataRequestParams extends RequestParams {
    public static final Parcelable.Creator<ClearEncryptDataRequestParams> CREATOR = new Parcelable.Creator<ClearEncryptDataRequestParams>() {
        public final ClearEncryptDataRequestParams createFromParcel(Parcel parcel) {
            return new ClearEncryptDataRequestParams(parcel);
        }

        public final ClearEncryptDataRequestParams[] newArray(int i) {
            return new ClearEncryptDataRequestParams[i];
        }
    };
    private Bundle mParams;

    public ClearEncryptDataRequestParams() {
    }

    public ClearEncryptDataRequestParams(Parcel parcel) {
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
