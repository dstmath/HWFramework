package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;

public class GetAppListRequestParams extends RequestParams implements Parcelable {
    public static final Parcelable.Creator<GetAppListRequestParams> CREATOR = new Parcelable.Creator<GetAppListRequestParams>() {
        public final GetAppListRequestParams createFromParcel(Parcel parcel) {
            return new GetAppListRequestParams(parcel);
        }

        public final GetAppListRequestParams[] newArray(int i) {
            return new GetAppListRequestParams[i];
        }
    };
    private String mKeyword;
    private String[] mStatus;

    public GetAppListRequestParams() {
    }

    public GetAppListRequestParams(Parcel parcel) {
        super(parcel);
        this.mKeyword = parcel.readString();
        this.mStatus = parcel.createStringArray();
    }

    public int describeContents() {
        return 0;
    }

    public String getKeyword() {
        return this.mKeyword;
    }

    public String[] getStatus() {
        return this.mStatus;
    }

    public void setKeyword(String str) {
        this.mKeyword = str;
    }

    public void setStatus(String[] strArr) {
        this.mStatus = strArr;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(this.mKeyword);
        parcel.writeStringArray(this.mStatus);
    }
}
