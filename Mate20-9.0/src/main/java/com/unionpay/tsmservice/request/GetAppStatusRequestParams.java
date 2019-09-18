package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;
import com.unionpay.tsmservice.AppID;

public class GetAppStatusRequestParams extends RequestParams {
    public static final Parcelable.Creator<GetAppStatusRequestParams> CREATOR = new Parcelable.Creator<GetAppStatusRequestParams>() {
        public final GetAppStatusRequestParams createFromParcel(Parcel parcel) {
            return new GetAppStatusRequestParams(parcel);
        }

        public final GetAppStatusRequestParams[] newArray(int i) {
            return new GetAppStatusRequestParams[i];
        }
    };
    private AppID mAppID;

    public GetAppStatusRequestParams() {
    }

    public GetAppStatusRequestParams(Parcel parcel) {
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
