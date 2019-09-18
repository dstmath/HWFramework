package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;

public class RequestParams implements Parcelable {
    public static final Parcelable.Creator<RequestParams> CREATOR = new Parcelable.Creator<RequestParams>() {
        public final RequestParams createFromParcel(Parcel parcel) {
            return new RequestParams(parcel);
        }

        public final RequestParams[] newArray(int i) {
            return new RequestParams[i];
        }
    };
    private String mReserve = "";

    public RequestParams() {
    }

    public RequestParams(Parcel parcel) {
        this.mReserve = parcel.readString();
    }

    public RequestParams(String str) {
        this.mReserve = str;
    }

    public int describeContents() {
        return 0;
    }

    public String getReserve() {
        return this.mReserve;
    }

    public void setReserve(String str) {
        this.mReserve = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mReserve);
    }
}
