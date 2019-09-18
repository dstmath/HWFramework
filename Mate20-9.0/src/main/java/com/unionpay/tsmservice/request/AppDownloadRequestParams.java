package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;
import com.unionpay.tsmservice.AppID;

public class AppDownloadRequestParams extends RequestParams {
    public static final Parcelable.Creator<AppDownloadRequestParams> CREATOR = new Parcelable.Creator<AppDownloadRequestParams>() {
        public final AppDownloadRequestParams createFromParcel(Parcel parcel) {
            return new AppDownloadRequestParams(parcel);
        }

        public final AppDownloadRequestParams[] newArray(int i) {
            return new AppDownloadRequestParams[i];
        }
    };
    private AppID mAppID;
    private String mAppName;

    public AppDownloadRequestParams() {
    }

    public AppDownloadRequestParams(Parcel parcel) {
        super(parcel);
        this.mAppID = (AppID) parcel.readParcelable(AppID.class.getClassLoader());
        this.mAppName = parcel.readString();
    }

    public AppID getAppID() {
        return this.mAppID;
    }

    public String getAppName() {
        return this.mAppName;
    }

    public void setAppID(AppID appID) {
        this.mAppID = appID;
    }

    public void setAppName(String str) {
        this.mAppName = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeParcelable(this.mAppID, i);
        parcel.writeString(this.mAppName);
    }
}
