package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;

public class SetDefaultCardRequestParams extends RequestParams {
    public static final Parcelable.Creator<SetDefaultCardRequestParams> CREATOR = new Parcelable.Creator<SetDefaultCardRequestParams>() {
        public final SetDefaultCardRequestParams createFromParcel(Parcel parcel) {
            return new SetDefaultCardRequestParams(parcel);
        }

        public final SetDefaultCardRequestParams[] newArray(int i) {
            return new SetDefaultCardRequestParams[i];
        }
    };
    private String mAppAID;

    public SetDefaultCardRequestParams() {
    }

    public SetDefaultCardRequestParams(Parcel parcel) {
        super(parcel);
        this.mAppAID = parcel.readString();
    }

    public SetDefaultCardRequestParams(String str) {
        this.mAppAID = str;
    }

    public String getAppAID() {
        return this.mAppAID;
    }

    public void setAppAID(String str) {
        this.mAppAID = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(this.mAppAID);
    }
}
