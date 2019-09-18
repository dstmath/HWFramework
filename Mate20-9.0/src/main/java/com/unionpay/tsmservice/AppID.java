package com.unionpay.tsmservice;

import android.os.Parcel;
import android.os.Parcelable;

public class AppID implements Parcelable {
    public static final Parcelable.Creator<AppID> CREATOR = new Parcelable.Creator<AppID>() {
        public final /* synthetic */ Object createFromParcel(Parcel parcel) {
            return new AppID(parcel);
        }

        public final /* bridge */ /* synthetic */ Object[] newArray(int i) {
            return new AppID[i];
        }
    };
    String a = "";
    String b = "";

    public AppID(Parcel parcel) {
        this.a = parcel.readString();
        this.b = parcel.readString();
    }

    public AppID(String str, String str2) {
        this.a = str;
        this.b = str2;
    }

    public int describeContents() {
        return 0;
    }

    public String getAppAid() {
        return this.a;
    }

    public String getAppVersion() {
        return this.b;
    }

    public void setAppAid(String str) {
        this.a = str;
    }

    public void setAppVersion(String str) {
        this.b = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.a);
        parcel.writeString(this.b);
    }
}
