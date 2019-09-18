package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;
import com.unionpay.tsmservice.AppID;

public class AppLockRequestParams extends RequestParams {
    public static final Parcelable.Creator<AppLockRequestParams> CREATOR = new Parcelable.Creator<AppLockRequestParams>() {
        public final AppLockRequestParams createFromParcel(Parcel parcel) {
            return new AppLockRequestParams(parcel);
        }

        public final AppLockRequestParams[] newArray(int i) {
            return new AppLockRequestParams[i];
        }
    };
    private AppID mAppID;

    public AppLockRequestParams() {
    }

    public AppLockRequestParams(Parcel parcel) {
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
