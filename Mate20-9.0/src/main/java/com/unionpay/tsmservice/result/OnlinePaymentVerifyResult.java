package com.unionpay.tsmservice.result;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class OnlinePaymentVerifyResult implements Parcelable {
    public static final Parcelable.Creator<OnlinePaymentVerifyResult> CREATOR = new Parcelable.Creator<OnlinePaymentVerifyResult>() {
        public final OnlinePaymentVerifyResult createFromParcel(Parcel parcel) {
            return new OnlinePaymentVerifyResult(parcel);
        }

        public final OnlinePaymentVerifyResult[] newArray(int i) {
            return new OnlinePaymentVerifyResult[i];
        }
    };
    private Bundle mResultData;

    public OnlinePaymentVerifyResult() {
    }

    public OnlinePaymentVerifyResult(Parcel parcel) {
        this.mResultData = parcel.readBundle();
    }

    public int describeContents() {
        return 0;
    }

    public Bundle getResultData() {
        return this.mResultData;
    }

    public void setResultData(Bundle bundle) {
        this.mResultData = bundle;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeBundle(this.mResultData);
    }
}
