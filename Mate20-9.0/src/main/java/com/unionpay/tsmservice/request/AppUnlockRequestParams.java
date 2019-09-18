package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;
import com.unionpay.tsmservice.AppID;

public class AppUnlockRequestParams extends RequestParams {
    public static final Parcelable.Creator<AppUnlockRequestParams> CREATOR = new Parcelable.Creator<AppUnlockRequestParams>() {
        public final AppUnlockRequestParams createFromParcel(Parcel parcel) {
            return new AppUnlockRequestParams(parcel);
        }

        public final AppUnlockRequestParams[] newArray(int i) {
            return new AppUnlockRequestParams[i];
        }
    };
    private AppID mAppID;

    public AppUnlockRequestParams() {
    }

    public AppUnlockRequestParams(Parcel parcel) {
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
