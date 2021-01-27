package com.huawei.coauthservice.identitymgr.model;

import android.os.Parcel;
import android.os.Parcelable;

public enum UserType implements Parcelable {
    SAME_USER_ID(0),
    NO_USER_ID(1);
    
    public static final Parcelable.Creator<UserType> CREATOR = new Parcelable.Creator<UserType>() {
        /* class com.huawei.coauthservice.identitymgr.model.UserType.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public UserType createFromParcel(Parcel in) {
            return UserType.values()[UserType.checkTypeIndex(in.readInt())];
        }

        @Override // android.os.Parcelable.Creator
        public UserType[] newArray(int size) {
            return new UserType[size];
        }
    };
    private static final int MAX_TYPE_INDEX = 1;
    private int type;

    private UserType(int value) {
        this.type = value;
    }

    public int toInt() {
        return this.type;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // java.lang.Enum, java.lang.Object
    public String toString() {
        return "UserType{type=" + this.type + '}';
    }

    /* access modifiers changed from: private */
    public static int checkTypeIndex(int index) {
        if (index < 0 || index > 1) {
            return 0;
        }
        return index;
    }
}
