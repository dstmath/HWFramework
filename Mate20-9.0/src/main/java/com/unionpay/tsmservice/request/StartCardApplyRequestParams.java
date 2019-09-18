package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;

public class StartCardApplyRequestParams extends RequestParams {
    public static final Parcelable.Creator<StartCardApplyRequestParams> CREATOR = new Parcelable.Creator<StartCardApplyRequestParams>() {
        public final StartCardApplyRequestParams createFromParcel(Parcel parcel) {
            return new StartCardApplyRequestParams(parcel);
        }

        public final StartCardApplyRequestParams[] newArray(int i) {
            return new StartCardApplyRequestParams[i];
        }
    };

    public StartCardApplyRequestParams() {
    }

    public StartCardApplyRequestParams(Parcel parcel) {
        super(parcel);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
    }
}
