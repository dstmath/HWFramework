package com.unionpay.tsmservice.result;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class VendorPayStatusResult implements Parcelable {
    public static final Parcelable.Creator<VendorPayStatusResult> CREATOR = new Parcelable.Creator<VendorPayStatusResult>() {
        public final VendorPayStatusResult createFromParcel(Parcel parcel) {
            return new VendorPayStatusResult(parcel);
        }

        public final VendorPayStatusResult[] newArray(int i) {
            return new VendorPayStatusResult[i];
        }
    };
    private Bundle mStatus;

    public VendorPayStatusResult() {
    }

    public VendorPayStatusResult(Parcel parcel) {
        this.mStatus = parcel.readBundle();
    }

    public int describeContents() {
        return 0;
    }

    public Bundle getVendorPayStatusResult() {
        return this.mStatus;
    }

    public void setVendorPayStatusResult(Bundle bundle) {
        this.mStatus = bundle;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeBundle(this.mStatus);
    }
}
