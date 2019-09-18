package com.unionpay.tsmservice.result;

import android.os.Parcel;
import android.os.Parcelable;

public class SendApduResult implements Parcelable {
    public static final Parcelable.Creator<SendApduResult> CREATOR = new Parcelable.Creator<SendApduResult>() {
        public final SendApduResult createFromParcel(Parcel parcel) {
            return new SendApduResult(parcel);
        }

        public final SendApduResult[] newArray(int i) {
            return new SendApduResult[i];
        }
    };
    private String outHexApdu;

    public SendApduResult() {
    }

    public SendApduResult(Parcel parcel) {
        this.outHexApdu = parcel.readString();
    }

    public int describeContents() {
        return 0;
    }

    public String getOutHexApdu() {
        return this.outHexApdu;
    }

    public void setOutHexApdu(String str) {
        this.outHexApdu = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.outHexApdu);
    }
}
