package com.huawei.coauthservice.identitymgr.model;

import android.os.Parcel;
import android.os.Parcelable;

public enum LinkType implements Parcelable {
    AP(0),
    P2P(1);
    
    public static final Parcelable.Creator<LinkType> CREATOR = new Parcelable.Creator<LinkType>() {
        /* class com.huawei.coauthservice.identitymgr.model.LinkType.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public LinkType createFromParcel(Parcel in) {
            return LinkType.values()[LinkType.checkTypeIndex(in.readInt())];
        }

        @Override // android.os.Parcelable.Creator
        public LinkType[] newArray(int size) {
            return new LinkType[size];
        }
    };
    private static final int MAX_TYPE_INDEX = 1;
    private int type;

    private LinkType(int value) {
        this.type = value;
    }

    public int toInt() {
        return this.type;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
    }

    @Override // java.lang.Enum, java.lang.Object
    public String toString() {
        return "LinkType{type=" + this.type + '}';
    }

    /* access modifiers changed from: private */
    public static int checkTypeIndex(int index) {
        if (index < 0 || index > 1) {
            return 0;
        }
        return index;
    }
}
