package com.unionpay.tsmservice.request;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class GetTransactionDetailsRequestParams extends RequestParams {
    public static final Parcelable.Creator<GetTransactionDetailsRequestParams> CREATOR = new Parcelable.Creator<GetTransactionDetailsRequestParams>() {
        public final GetTransactionDetailsRequestParams createFromParcel(Parcel parcel) {
            return new GetTransactionDetailsRequestParams(parcel);
        }

        public final GetTransactionDetailsRequestParams[] newArray(int i) {
            return new GetTransactionDetailsRequestParams[i];
        }
    };
    private Bundle mParams;

    public GetTransactionDetailsRequestParams() {
    }

    public GetTransactionDetailsRequestParams(Parcel parcel) {
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
