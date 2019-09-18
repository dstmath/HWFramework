package com.unionpay.tsmservice.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Amount implements Parcelable {
    public static final Parcelable.Creator<Amount> CREATOR = new Parcelable.Creator<Amount>() {
        public final Amount createFromParcel(Parcel parcel) {
            return new Amount(parcel);
        }

        public final Amount[] newArray(int i) {
            return new Amount[i];
        }
    };
    private String mCurrencyType = "CNY";
    private String mProductPrice = "0.0";

    public Amount() {
    }

    public Amount(Parcel parcel) {
        this.mCurrencyType = parcel.readString();
        this.mProductPrice = parcel.readString();
    }

    public Amount(String str, String str2) {
        this.mCurrencyType = str;
        this.mProductPrice = str2;
    }

    public int describeContents() {
        return 0;
    }

    public String getCurrencyType() {
        return this.mCurrencyType;
    }

    public String getProductPrice() {
        return this.mProductPrice;
    }

    public void setCurrencyType(String str) {
        this.mCurrencyType = str;
    }

    public void setProductPrice(String str) {
        this.mProductPrice = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mCurrencyType);
        parcel.writeString(this.mProductPrice);
    }
}
