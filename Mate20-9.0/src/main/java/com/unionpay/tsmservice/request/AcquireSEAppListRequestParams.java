package com.unionpay.tsmservice.request;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class AcquireSEAppListRequestParams extends RequestParams {
    public static final Parcelable.Creator<AcquireSEAppListRequestParams> CREATOR = new Parcelable.Creator<AcquireSEAppListRequestParams>() {
        public final AcquireSEAppListRequestParams createFromParcel(Parcel parcel) {
            return new AcquireSEAppListRequestParams(parcel);
        }

        public final AcquireSEAppListRequestParams[] newArray(int i) {
            return new AcquireSEAppListRequestParams[i];
        }
    };
    private Bundle mParams;

    public AcquireSEAppListRequestParams() {
    }

    public AcquireSEAppListRequestParams(Parcel parcel) {
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
