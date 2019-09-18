package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;

public class GetVendorPayStatusRequestParams extends RequestParams {
    public static final Parcelable.Creator<GetVendorPayStatusRequestParams> CREATOR = new Parcelable.Creator<GetVendorPayStatusRequestParams>() {
        public final GetVendorPayStatusRequestParams createFromParcel(Parcel parcel) {
            return new GetVendorPayStatusRequestParams(parcel);
        }

        public final GetVendorPayStatusRequestParams[] newArray(int i) {
            return new GetVendorPayStatusRequestParams[i];
        }
    };

    public GetVendorPayStatusRequestParams() {
    }

    public GetVendorPayStatusRequestParams(Parcel parcel) {
        super(parcel);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
    }
}
