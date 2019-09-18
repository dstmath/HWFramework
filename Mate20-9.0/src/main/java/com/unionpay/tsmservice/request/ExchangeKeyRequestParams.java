package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;

public class ExchangeKeyRequestParams extends RequestParams {
    public static final Parcelable.Creator<ExchangeKeyRequestParams> CREATOR = new Parcelable.Creator<ExchangeKeyRequestParams>() {
        public final ExchangeKeyRequestParams createFromParcel(Parcel parcel) {
            return new ExchangeKeyRequestParams(parcel);
        }

        public final ExchangeKeyRequestParams[] newArray(int i) {
            return new ExchangeKeyRequestParams[i];
        }
    };
    private String mTempKey;
    private int mType;

    public ExchangeKeyRequestParams() {
    }

    public ExchangeKeyRequestParams(Parcel parcel) {
        super(parcel);
        this.mType = parcel.readInt();
        this.mTempKey = parcel.readString();
    }

    public String getTempKey() {
        return this.mTempKey;
    }

    public int getType() {
        return this.mType;
    }

    public void setTempKey(String str) {
        this.mTempKey = str;
    }

    public void setType(int i) {
        this.mType = i;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(this.mType);
        parcel.writeString(this.mTempKey);
    }
}
