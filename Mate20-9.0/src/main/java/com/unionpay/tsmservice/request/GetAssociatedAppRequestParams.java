package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;

public class GetAssociatedAppRequestParams extends RequestParams {
    public static final Parcelable.Creator<GetAssociatedAppRequestParams> CREATOR = new Parcelable.Creator<GetAssociatedAppRequestParams>() {
        public final GetAssociatedAppRequestParams createFromParcel(Parcel parcel) {
            return new GetAssociatedAppRequestParams(parcel);
        }

        public final GetAssociatedAppRequestParams[] newArray(int i) {
            return new GetAssociatedAppRequestParams[i];
        }
    };
    private String mEncryptPan;

    public GetAssociatedAppRequestParams() {
    }

    public GetAssociatedAppRequestParams(Parcel parcel) {
        super(parcel);
        this.mEncryptPan = parcel.readString();
    }

    public GetAssociatedAppRequestParams(String str) {
        this.mEncryptPan = str;
    }

    public String getEncryptPan() {
        return this.mEncryptPan;
    }

    public void setEncryptPan(String str) {
        this.mEncryptPan = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(this.mEncryptPan);
    }
}
