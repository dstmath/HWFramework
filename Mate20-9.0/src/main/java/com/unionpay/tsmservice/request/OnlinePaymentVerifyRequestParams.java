package com.unionpay.tsmservice.request;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class OnlinePaymentVerifyRequestParams extends RequestParams {
    public static final Parcelable.Creator<OnlinePaymentVerifyRequestParams> CREATOR = new Parcelable.Creator<OnlinePaymentVerifyRequestParams>() {
        public final OnlinePaymentVerifyRequestParams createFromParcel(Parcel parcel) {
            return new OnlinePaymentVerifyRequestParams(parcel);
        }

        public final OnlinePaymentVerifyRequestParams[] newArray(int i) {
            return new OnlinePaymentVerifyRequestParams[i];
        }
    };
    private String mAId;
    private String mOrderNumber;
    private Bundle mResource;

    public OnlinePaymentVerifyRequestParams() {
    }

    public OnlinePaymentVerifyRequestParams(Parcel parcel) {
        super(parcel);
        this.mResource = parcel.readBundle();
        this.mOrderNumber = parcel.readString();
        this.mAId = parcel.readString();
    }

    public int describeContents() {
        return 0;
    }

    public String getAId() {
        return this.mAId;
    }

    public String getOrderNumber() {
        return this.mOrderNumber;
    }

    public Bundle getResource() {
        return this.mResource;
    }

    public void setAId(String str) {
        this.mAId = str;
    }

    public void setOrderNumber(String str) {
        this.mOrderNumber = str;
    }

    public void setResource(Bundle bundle) {
        this.mResource = bundle;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeBundle(this.mResource);
        parcel.writeString(this.mOrderNumber);
        parcel.writeString(this.mAId);
    }
}
