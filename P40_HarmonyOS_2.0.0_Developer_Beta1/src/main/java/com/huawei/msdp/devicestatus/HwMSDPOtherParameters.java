package com.huawei.msdp.devicestatus;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Locale;

public class HwMSDPOtherParameters implements Parcelable {
    public static final Parcelable.Creator<HwMSDPOtherParameters> CREATOR = new Parcelable.Creator<HwMSDPOtherParameters>() {
        /* class com.huawei.msdp.devicestatus.HwMSDPOtherParameters.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwMSDPOtherParameters createFromParcel(Parcel source) {
            if (source != null) {
                return new HwMSDPOtherParameters(source.readString(), source.readString(), source.readString(), source.readString());
            }
            return null;
        }

        @Override // android.os.Parcelable.Creator
        public HwMSDPOtherParameters[] newArray(int size) {
            return new HwMSDPOtherParameters[size];
        }
    };
    private String mParam1;
    private String mParam2;
    private String mParam3;
    private String mParam4;

    public HwMSDPOtherParameters(String mParam12, String mParam22, String mParam32, String mParam42) {
        this.mParam1 = mParam12;
        this.mParam2 = mParam22;
        this.mParam3 = mParam32;
        this.mParam4 = mParam42;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        if (parcel != null) {
            parcel.writeString(this.mParam1);
            parcel.writeString(this.mParam2);
            parcel.writeString(this.mParam3);
            parcel.writeString(this.mParam4);
        }
    }

    @Override // java.lang.Object
    public String toString() {
        return String.format(Locale.ENGLISH, "Param1=%s, Param2=%s, Param3=%s, Param4=%s, Param5=%s", this.mParam1, this.mParam2, this.mParam3, this.mParam4);
    }

    public String getmParam1() {
        return this.mParam1;
    }

    public void setmParam1(String mParam12) {
        this.mParam1 = mParam12;
    }

    public String getmParam2() {
        return this.mParam2;
    }

    public void setmParam2(String mParam22) {
        this.mParam2 = mParam22;
    }

    public String getmParam3() {
        return this.mParam3;
    }

    public void setmParam3(String mParam32) {
        this.mParam3 = mParam32;
    }

    public String getmParam4() {
        return this.mParam4;
    }

    public void setmParam4(String mParam42) {
        this.mParam4 = mParam42;
    }
}
