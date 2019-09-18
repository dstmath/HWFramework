package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;

public class GetCurrentWalletClientRequestParams extends RequestParams {
    public static final Parcelable.Creator<GetCurrentWalletClientRequestParams> CREATOR = new Parcelable.Creator<GetCurrentWalletClientRequestParams>() {
        public final GetCurrentWalletClientRequestParams createFromParcel(Parcel parcel) {
            return new GetCurrentWalletClientRequestParams(parcel);
        }

        public final GetCurrentWalletClientRequestParams[] newArray(int i) {
            return new GetCurrentWalletClientRequestParams[i];
        }
    };

    public GetCurrentWalletClientRequestParams() {
    }

    public GetCurrentWalletClientRequestParams(Parcel parcel) {
        super(parcel);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
    }
}
