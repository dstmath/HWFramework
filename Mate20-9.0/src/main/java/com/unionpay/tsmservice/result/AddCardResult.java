package com.unionpay.tsmservice.result;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class AddCardResult implements Parcelable {
    public static final Parcelable.Creator<AddCardResult> CREATOR = new Parcelable.Creator<AddCardResult>() {
        public final AddCardResult createFromParcel(Parcel parcel) {
            return new AddCardResult(parcel);
        }

        public final AddCardResult[] newArray(int i) {
            return new AddCardResult[i];
        }
    };
    private Bundle mBankCardInfo;

    public AddCardResult() {
    }

    public AddCardResult(Parcel parcel) {
        this.mBankCardInfo = parcel.readBundle();
    }

    public int describeContents() {
        return 0;
    }

    public Bundle getBankCardInfo() {
        return this.mBankCardInfo;
    }

    public void setBankCardInfo(Bundle bundle) {
        this.mBankCardInfo = bundle;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeBundle(this.mBankCardInfo);
    }
}
