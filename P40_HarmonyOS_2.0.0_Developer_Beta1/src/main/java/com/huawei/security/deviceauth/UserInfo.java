package com.huawei.security.deviceauth;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class UserInfo implements Parcelable {
    public static final Parcelable.Creator<UserInfo> CREATOR = new Parcelable.Creator<UserInfo>() {
        /* class com.huawei.security.deviceauth.UserInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };
    private static final int MAX_AUTH_ID_LEN = 64;
    private byte[] mAuthId;
    private int mAuthIdLen;
    private String mServiceType;
    private int mUserType;

    public UserInfo() {
        this.mAuthIdLen = 0;
        this.mAuthId = new byte[0];
    }

    public UserInfo(String serviceType, byte[] authId, int userType) {
        this.mServiceType = serviceType;
        if (authId != null) {
            this.mAuthId = (byte[]) authId.clone();
            this.mAuthIdLen = authId.length;
        } else {
            this.mAuthId = new byte[0];
            this.mAuthIdLen = 0;
        }
        this.mUserType = userType;
    }

    protected UserInfo(@NonNull Parcel in) {
        this.mUserType = in.readInt();
        this.mServiceType = in.readString();
        this.mAuthIdLen = in.readInt();
        if (checkAuthIdLen(this.mAuthIdLen)) {
            this.mAuthId = new byte[this.mAuthIdLen];
            in.readByteArray(this.mAuthId);
            return;
        }
        this.mAuthId = new byte[0];
        this.mAuthIdLen = 0;
    }

    public String getServiceType() {
        return this.mServiceType;
    }

    public void setServiceType(String serviceType) {
        this.mServiceType = serviceType;
    }

    public byte[] getAuthId() {
        byte[] bArr = this.mAuthId;
        if (bArr == null) {
            return null;
        }
        return (byte[]) bArr.clone();
    }

    public int getUserType() {
        return this.mUserType;
    }

    public void setUserType(int userType) {
        this.mUserType = userType;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public void setAuthId(byte[] authId) {
        if (authId != null) {
            this.mAuthId = (byte[]) authId.clone();
            this.mAuthIdLen = authId.length;
            return;
        }
        this.mAuthId = new byte[0];
        this.mAuthIdLen = 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mUserType);
        dest.writeString(this.mServiceType);
        dest.writeInt(this.mAuthIdLen);
        dest.writeByteArray(this.mAuthId);
    }

    private boolean checkAuthIdLen(int authIdLen) {
        if (authIdLen < 0 || authIdLen > 64) {
            return false;
        }
        return true;
    }
}
