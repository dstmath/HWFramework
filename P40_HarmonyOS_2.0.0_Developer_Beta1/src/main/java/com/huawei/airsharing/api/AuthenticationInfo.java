package com.huawei.airsharing.api;

import android.os.Parcel;
import android.os.Parcelable;

public class AuthenticationInfo implements Parcelable {
    public static final int AUTH_CANCEL = 0;
    public static final int AUTH_CONFIRM = 1;
    public static final Parcelable.Creator<AuthenticationInfo> CREATOR = new Parcelable.Creator<AuthenticationInfo>() {
        /* class com.huawei.airsharing.api.AuthenticationInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AuthenticationInfo createFromParcel(Parcel in) {
            return new AuthenticationInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public AuthenticationInfo[] newArray(int size) {
            return new AuthenticationInfo[size];
        }
    };
    private int mAuthAction = 0;
    private String mAuthCode = null;

    public AuthenticationInfo() {
    }

    public AuthenticationInfo(int action, String authCode) {
        this.mAuthAction = action;
        this.mAuthCode = authCode;
    }

    protected AuthenticationInfo(Parcel in) {
        this.mAuthAction = in.readInt();
        this.mAuthCode = in.readString();
    }

    public void setAuthAction(int action) {
        this.mAuthAction = action;
    }

    public int getAuthAction() {
        return this.mAuthAction;
    }

    public void setAuthCode(String code) {
        this.mAuthCode = code;
    }

    public String getAuthCode() {
        return this.mAuthCode;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mAuthAction);
        dest.writeString(this.mAuthCode);
    }
}
