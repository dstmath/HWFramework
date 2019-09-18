package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;
import com.unionpay.tsmservice.AppID;

public class GetAccountBalanceRequestParams extends RequestParams {
    public static final Parcelable.Creator<GetAccountBalanceRequestParams> CREATOR = new Parcelable.Creator<GetAccountBalanceRequestParams>() {
        public final GetAccountBalanceRequestParams createFromParcel(Parcel parcel) {
            return new GetAccountBalanceRequestParams(parcel);
        }

        public final GetAccountBalanceRequestParams[] newArray(int i) {
            return new GetAccountBalanceRequestParams[i];
        }
    };
    private AppID mAppID;
    private String mEncryptPin;

    public GetAccountBalanceRequestParams() {
    }

    public GetAccountBalanceRequestParams(Parcel parcel) {
        super(parcel);
        this.mAppID = (AppID) parcel.readParcelable(AppID.class.getClassLoader());
        this.mEncryptPin = parcel.readString();
    }

    public AppID getAppID() {
        return this.mAppID;
    }

    public String getEncryptPin() {
        return this.mEncryptPin;
    }

    public void setAppID(AppID appID) {
        this.mAppID = appID;
    }

    public void setEncryptPin(String str) {
        this.mEncryptPin = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeParcelable(this.mAppID, i);
        parcel.writeString(this.mEncryptPin);
    }
}
