package com.unionpay.tsmservice.result;

import android.os.Parcel;
import android.os.Parcelable;

public class GetSeIdResult implements Parcelable {
    public static final Parcelable.Creator<GetSeIdResult> CREATOR = new Parcelable.Creator<GetSeIdResult>() {
        public final GetSeIdResult createFromParcel(Parcel parcel) {
            return new GetSeIdResult(parcel);
        }

        public final GetSeIdResult[] newArray(int i) {
            return new GetSeIdResult[i];
        }
    };
    private String mSeId;

    public GetSeIdResult() {
    }

    public GetSeIdResult(Parcel parcel) {
        this.mSeId = parcel.readString();
    }

    public int describeContents() {
        return 0;
    }

    public String getSeId() {
        return this.mSeId;
    }

    public void setSeId(String str) {
        this.mSeId = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mSeId);
    }
}
