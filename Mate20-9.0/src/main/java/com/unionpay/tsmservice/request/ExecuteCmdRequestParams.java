package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;

public class ExecuteCmdRequestParams extends RequestParams {
    public static final Parcelable.Creator<ExecuteCmdRequestParams> CREATOR = new Parcelable.Creator<ExecuteCmdRequestParams>() {
        public final ExecuteCmdRequestParams createFromParcel(Parcel parcel) {
            return new ExecuteCmdRequestParams(parcel);
        }

        public final ExecuteCmdRequestParams[] newArray(int i) {
            return new ExecuteCmdRequestParams[i];
        }
    };
    private String mSign;
    private String mSsid;

    public ExecuteCmdRequestParams() {
    }

    public ExecuteCmdRequestParams(Parcel parcel) {
        super(parcel);
        this.mSsid = parcel.readString();
        this.mSign = parcel.readString();
    }

    public String getSign() {
        return this.mSign;
    }

    public String getSsid() {
        return this.mSsid;
    }

    public void setSign(String str) {
        this.mSign = str;
    }

    public void setSsid(String str) {
        this.mSsid = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(this.mSsid);
        parcel.writeString(this.mSign);
    }
}
