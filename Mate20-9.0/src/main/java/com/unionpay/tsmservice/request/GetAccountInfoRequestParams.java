package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;
import com.unionpay.tsmservice.AppID;

public class GetAccountInfoRequestParams extends RequestParams {
    public static final Parcelable.Creator<GetAccountInfoRequestParams> CREATOR = new Parcelable.Creator<GetAccountInfoRequestParams>() {
        public final GetAccountInfoRequestParams createFromParcel(Parcel parcel) {
            return new GetAccountInfoRequestParams(parcel);
        }

        public final GetAccountInfoRequestParams[] newArray(int i) {
            return new GetAccountInfoRequestParams[i];
        }
    };
    private AppID mAppID;

    public GetAccountInfoRequestParams() {
    }

    public GetAccountInfoRequestParams(Parcel parcel) {
        super(parcel);
        this.mAppID = (AppID) parcel.readParcelable(AppID.class.getClassLoader());
    }

    public AppID getAppID() {
        return this.mAppID;
    }

    public void setAppID(AppID appID) {
        this.mAppID = appID;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeParcelable(this.mAppID, i);
    }
}
