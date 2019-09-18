package com.unionpay.tsmservice.request;

import android.os.Parcel;
import android.os.Parcelable;
import com.unionpay.tsmservice.AppID;

public class GetSMSAuthCodeRequestParams extends RequestParams {
    public static final Parcelable.Creator<GetSMSAuthCodeRequestParams> CREATOR = new Parcelable.Creator<GetSMSAuthCodeRequestParams>() {
        public final GetSMSAuthCodeRequestParams createFromParcel(Parcel parcel) {
            return new GetSMSAuthCodeRequestParams(parcel);
        }

        public final GetSMSAuthCodeRequestParams[] newArray(int i) {
            return new GetSMSAuthCodeRequestParams[i];
        }
    };
    private AppID mAppID;
    private String mPan;
    private String mSisdn;

    public GetSMSAuthCodeRequestParams() {
    }

    public GetSMSAuthCodeRequestParams(Parcel parcel) {
        super(parcel);
        this.mAppID = (AppID) parcel.readParcelable(AppID.class.getClassLoader());
        this.mPan = parcel.readString();
        this.mSisdn = parcel.readString();
    }

    public AppID getAppID() {
        return this.mAppID;
    }

    public String getMsisdn() {
        return this.mSisdn;
    }

    public String getPan() {
        return this.mPan;
    }

    public void setAppID(AppID appID) {
        this.mAppID = appID;
    }

    public void setMsisdn(String str) {
        this.mSisdn = str;
    }

    public void setPan(String str) {
        this.mPan = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeParcelable(this.mAppID, i);
        parcel.writeString(this.mPan);
        parcel.writeString(this.mSisdn);
    }
}
