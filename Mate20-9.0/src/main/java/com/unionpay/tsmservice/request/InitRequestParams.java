package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;

public class InitRequestParams extends RequestParams {
    public static final Parcelable.Creator<InitRequestParams> CREATOR = new Parcelable.Creator<InitRequestParams>() {
        public final InitRequestParams createFromParcel(Parcel parcel) {
            return new InitRequestParams(parcel);
        }

        public final InitRequestParams[] newArray(int i) {
            return new InitRequestParams[i];
        }
    };
    private String mSignature = "";

    public InitRequestParams() {
    }

    public InitRequestParams(Parcel parcel) {
        super(parcel);
        this.mSignature = parcel.readString();
    }

    public String getSignature() {
        return this.mSignature;
    }

    public void setSignature(String str) {
        this.mSignature = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(this.mSignature);
    }
}
