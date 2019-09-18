package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;

public class GetSeAppListRequestParams extends RequestParams {
    public static final Parcelable.Creator<GetSeAppListRequestParams> CREATOR = new Parcelable.Creator<GetSeAppListRequestParams>() {
        public final GetSeAppListRequestParams createFromParcel(Parcel parcel) {
            return new GetSeAppListRequestParams(parcel);
        }

        public final GetSeAppListRequestParams[] newArray(int i) {
            return new GetSeAppListRequestParams[i];
        }
    };

    public GetSeAppListRequestParams() {
    }

    public GetSeAppListRequestParams(Parcel parcel) {
        super(parcel);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
    }
}
