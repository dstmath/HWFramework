package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;

public class OpenChannelRequestParams extends RequestParams {
    public static final Parcelable.Creator<OpenChannelRequestParams> CREATOR = new Parcelable.Creator<OpenChannelRequestParams>() {
        public final OpenChannelRequestParams createFromParcel(Parcel parcel) {
            return new OpenChannelRequestParams(parcel);
        }

        public final OpenChannelRequestParams[] newArray(int i) {
            return new OpenChannelRequestParams[i];
        }
    };
    private String mAppAID;

    public OpenChannelRequestParams() {
    }

    public OpenChannelRequestParams(Parcel parcel) {
        super(parcel);
        this.mAppAID = parcel.readString();
    }

    public OpenChannelRequestParams(String str) {
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
