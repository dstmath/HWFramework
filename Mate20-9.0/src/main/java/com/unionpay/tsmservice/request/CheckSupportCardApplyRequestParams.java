package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;

public class CheckSupportCardApplyRequestParams extends RequestParams {
    public static final Parcelable.Creator<CheckSupportCardApplyRequestParams> CREATOR = new Parcelable.Creator<CheckSupportCardApplyRequestParams>() {
        public final CheckSupportCardApplyRequestParams createFromParcel(Parcel parcel) {
            return new CheckSupportCardApplyRequestParams(parcel);
        }

        public final CheckSupportCardApplyRequestParams[] newArray(int i) {
            return new CheckSupportCardApplyRequestParams[i];
        }
    };

    public CheckSupportCardApplyRequestParams() {
    }

    public CheckSupportCardApplyRequestParams(Parcel parcel) {
        super(parcel);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
    }
}
