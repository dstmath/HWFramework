package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;
import com.unionpay.tsmservice.AppID;
import java.util.HashMap;
import java.util.Map;

public class AppDownloadApplyRequestParams extends RequestParams {
    public static final Parcelable.Creator<AppDownloadApplyRequestParams> CREATOR = new Parcelable.Creator<AppDownloadApplyRequestParams>() {
        public final AppDownloadApplyRequestParams createFromParcel(Parcel parcel) {
            return new AppDownloadApplyRequestParams(parcel);
        }

        public final AppDownloadApplyRequestParams[] newArray(int i) {
            return new AppDownloadApplyRequestParams[i];
        }
    };
    private AppID mAppID;
    private HashMap<String, String> mParams;

    public AppDownloadApplyRequestParams() {
    }

    public AppDownloadApplyRequestParams(Parcel parcel) {
        super(parcel);
        this.mAppID = (AppID) parcel.readParcelable(AppID.class.getClassLoader());
        this.mParams = parcel.readHashMap(HashMap.class.getClassLoader());
    }

    public AppID getAppID() {
        return this.mAppID;
    }

    public Map<String, String> getParams() {
        return this.mParams;
    }

    public void setAppID(AppID appID) {
        this.mAppID = appID;
    }

    public void setParams(HashMap<String, String> hashMap) {
        this.mParams = hashMap;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeParcelable(this.mAppID, i);
        parcel.writeMap(this.mParams);
    }
}
