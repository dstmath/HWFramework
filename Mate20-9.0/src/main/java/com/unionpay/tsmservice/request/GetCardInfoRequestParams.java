package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;

public class GetCardInfoRequestParams extends RequestParams implements Parcelable {
    public static final Parcelable.Creator<GetCardInfoRequestParams> CREATOR = new Parcelable.Creator<GetCardInfoRequestParams>() {
        public final GetCardInfoRequestParams createFromParcel(Parcel parcel) {
            return new GetCardInfoRequestParams(parcel);
        }

        public final GetCardInfoRequestParams[] newArray(int i) {
            return new GetCardInfoRequestParams[i];
        }
    };
    private String[] mAppAID;

    public GetCardInfoRequestParams() {
    }

    public GetCardInfoRequestParams(Parcel parcel) {
        super(parcel);
        this.mAppAID = parcel.createStringArray();
    }

    public int describeContents() {
        return 0;
    }

    public String[] getAppAID() {
        return this.mAppAID;
    }

    public void setAppAID(String[] strArr) {
        this.mAppAID = strArr;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeStringArray(this.mAppAID);
    }
}
