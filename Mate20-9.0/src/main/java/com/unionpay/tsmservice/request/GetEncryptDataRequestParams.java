package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;

public class GetEncryptDataRequestParams extends RequestParams {
    public static final Parcelable.Creator<GetEncryptDataRequestParams> CREATOR = new Parcelable.Creator<GetEncryptDataRequestParams>() {
        public final GetEncryptDataRequestParams createFromParcel(Parcel parcel) {
            return new GetEncryptDataRequestParams(parcel);
        }

        public final GetEncryptDataRequestParams[] newArray(int i) {
            return new GetEncryptDataRequestParams[i];
        }
    };
    private String mPan;
    private int mType;

    public GetEncryptDataRequestParams() {
    }

    public GetEncryptDataRequestParams(Parcel parcel) {
        super(parcel);
        this.mType = parcel.readInt();
        this.mPan = parcel.readString();
    }

    public int describeContents() {
        return 0;
    }

    public String getPan() {
        return this.mPan;
    }

    public int getType() {
        return this.mType;
    }

    public void setPan(String str) {
        this.mPan = str;
    }

    public void setType(int i) {
        this.mType = i;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(this.mType);
        parcel.writeString(this.mPan);
    }
}
