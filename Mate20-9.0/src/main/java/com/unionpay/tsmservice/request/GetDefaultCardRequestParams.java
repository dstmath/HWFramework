package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;

public class GetDefaultCardRequestParams extends RequestParams {
    public static final Parcelable.Creator<GetDefaultCardRequestParams> CREATOR = new Parcelable.Creator<GetDefaultCardRequestParams>() {
        public final GetDefaultCardRequestParams createFromParcel(Parcel parcel) {
            return new GetDefaultCardRequestParams(parcel);
        }

        public final GetDefaultCardRequestParams[] newArray(int i) {
            return new GetDefaultCardRequestParams[i];
        }
    };

    public GetDefaultCardRequestParams() {
    }

    public GetDefaultCardRequestParams(Parcel parcel) {
        super(parcel);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
    }
}
