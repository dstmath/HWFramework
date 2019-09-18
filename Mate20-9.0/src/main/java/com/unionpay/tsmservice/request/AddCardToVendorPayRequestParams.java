package com.unionpay.tsmservice.request;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class AddCardToVendorPayRequestParams extends RequestParams {
    public static final Parcelable.Creator<AddCardToVendorPayRequestParams> CREATOR = new Parcelable.Creator<AddCardToVendorPayRequestParams>() {
        public final AddCardToVendorPayRequestParams createFromParcel(Parcel parcel) {
            return new AddCardToVendorPayRequestParams(parcel);
        }

        public final AddCardToVendorPayRequestParams[] newArray(int i) {
            return new AddCardToVendorPayRequestParams[i];
        }
    };
    private Bundle mParams;

    public AddCardToVendorPayRequestParams() {
    }

    public AddCardToVendorPayRequestParams(Parcel parcel) {
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
