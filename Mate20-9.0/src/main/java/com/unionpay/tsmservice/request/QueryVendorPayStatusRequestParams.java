package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;

public class QueryVendorPayStatusRequestParams extends RequestParams {
    public static final Parcelable.Creator<QueryVendorPayStatusRequestParams> CREATOR = new Parcelable.Creator<QueryVendorPayStatusRequestParams>() {
        public final QueryVendorPayStatusRequestParams createFromParcel(Parcel parcel) {
            return new QueryVendorPayStatusRequestParams(parcel);
        }

        public final QueryVendorPayStatusRequestParams[] newArray(int i) {
            return new QueryVendorPayStatusRequestParams[i];
        }
    };

    public QueryVendorPayStatusRequestParams() {
    }

    public QueryVendorPayStatusRequestParams(Parcel parcel) {
        super(parcel);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
    }
}
