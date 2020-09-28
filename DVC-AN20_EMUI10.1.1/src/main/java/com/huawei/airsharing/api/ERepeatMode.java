package com.huawei.airsharing.api;

import android.os.Parcel;
import android.os.Parcelable;

public enum ERepeatMode implements Parcelable {
    PLAY_IN_ORDER(0),
    REPEAT_CURRENT(1),
    REPEAT_LIST(2),
    SHUFFLE(3);
    
    public static final Parcelable.Creator<ERepeatMode> CREATOR = new Parcelable.Creator<ERepeatMode>() {
        /* class com.huawei.airsharing.api.ERepeatMode.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ERepeatMode createFromParcel(Parcel source) {
            return ERepeatMode.valueOf(source.readString());
        }

        @Override // android.os.Parcelable.Creator
        public ERepeatMode[] newArray(int size) {
            return new ERepeatMode[size];
        }
    };
    private int value;

    private ERepeatMode(int value2) {
        this.value = value2;
    }

    public int getValue() {
        return this.value;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name());
    }
}
