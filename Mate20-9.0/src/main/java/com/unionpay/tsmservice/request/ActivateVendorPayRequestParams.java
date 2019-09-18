package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;

public class ActivateVendorPayRequestParams extends RequestParams {
    public static final Parcelable.Creator<ActivateVendorPayRequestParams> CREATOR = new Parcelable.Creator<ActivateVendorPayRequestParams>() {
        public final ActivateVendorPayRequestParams createFromParcel(Parcel parcel) {
            return new ActivateVendorPayRequestParams(parcel);
        }

        public final ActivateVendorPayRequestParams[] newArray(int i) {
            return new ActivateVendorPayRequestParams[i];
        }
    };

    public ActivateVendorPayRequestParams() {
    }

    public ActivateVendorPayRequestParams(Parcel parcel) {
        super(parcel);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
    }
}
