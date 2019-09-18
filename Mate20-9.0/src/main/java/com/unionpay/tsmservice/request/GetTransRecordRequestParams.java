package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;
import com.unionpay.tsmservice.AppID;

public class GetTransRecordRequestParams extends RequestParams {
    public static final Parcelable.Creator<GetTransRecordRequestParams> CREATOR = new Parcelable.Creator<GetTransRecordRequestParams>() {
        public final GetTransRecordRequestParams createFromParcel(Parcel parcel) {
            return new GetTransRecordRequestParams(parcel);
        }

        public final GetTransRecordRequestParams[] newArray(int i) {
            return new GetTransRecordRequestParams[i];
        }
    };
    private AppID mAppID;

    public GetTransRecordRequestParams() {
    }

    public GetTransRecordRequestParams(Parcel parcel) {
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
