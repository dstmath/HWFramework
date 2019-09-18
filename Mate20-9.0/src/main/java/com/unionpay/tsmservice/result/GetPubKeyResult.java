package com.unionpay.tsmservice.result;

import android.os.Parcel;
import android.os.Parcelable;

public class GetPubKeyResult implements Parcelable {
    public static final Parcelable.Creator<GetPubKeyResult> CREATOR = new Parcelable.Creator<GetPubKeyResult>() {
        public final GetPubKeyResult createFromParcel(Parcel parcel) {
            return new GetPubKeyResult(parcel);
        }

        public final GetPubKeyResult[] newArray(int i) {
            return new GetPubKeyResult[i];
        }
    };
    private String key;

    public GetPubKeyResult() {
    }

    public GetPubKeyResult(Parcel parcel) {
        this.key = parcel.readString();
    }

    public int describeContents() {
        return 0;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String str) {
        this.key = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.key);
    }
}
