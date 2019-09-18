package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;
import com.unionpay.tsmservice.AppID;

public class AppDataUpdateRequestParams extends RequestParams {
    public static final Parcelable.Creator<AppDataUpdateRequestParams> CREATOR = new Parcelable.Creator<AppDataUpdateRequestParams>() {
        public final AppDataUpdateRequestParams createFromParcel(Parcel parcel) {
            return new AppDataUpdateRequestParams(parcel);
        }

        public final AppDataUpdateRequestParams[] newArray(int i) {
            return new AppDataUpdateRequestParams[i];
        }
    };
    private AppID mAppID;

    public AppDataUpdateRequestParams() {
    }

    public AppDataUpdateRequestParams(Parcel parcel) {
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
