package com.huawei.android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public final class LeRangingSettings implements Parcelable {
    public static final Parcelable.Creator<LeRangingSettings> CREATOR = new Parcelable.Creator<LeRangingSettings>() {
        /* class com.huawei.android.bluetooth.LeRangingSettings.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public LeRangingSettings[] newArray(int size) {
            return new LeRangingSettings[size];
        }

        @Override // android.os.Parcelable.Creator
        public LeRangingSettings createFromParcel(Parcel in) {
            return new LeRangingSettings(in);
        }
    };
    public static final int LE_RANGING_RELECT_MODE = 1;
    public static final int LE_RANGING_SIMPLE_MODE = 0;
    private static final String TAG = "LeRangingSettings";
    private int mForceUpdateTime;
    private int mRangingMode;

    private LeRangingSettings(Parcel in) {
        this.mRangingMode = in.readInt();
        this.mForceUpdateTime = in.readInt();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mRangingMode);
        dest.writeInt(this.mForceUpdateTime);
    }

    public int describeContents() {
        return 0;
    }

    public LeRangingSettings(int rangingMode, int forceUpdateTime) {
        this.mRangingMode = rangingMode;
        this.mForceUpdateTime = forceUpdateTime;
    }

    public void setRangingMode(int rangingMode) {
        Log.d(TAG, "setRangingMode" + rangingMode);
        this.mRangingMode = rangingMode;
    }

    public int getRangingMode() {
        Log.d(TAG, "getRangingMode" + this.mRangingMode);
        return this.mRangingMode;
    }

    public void setForceUpdate(int msTime) {
        Log.d(TAG, "setForceUpdate" + msTime);
        this.mForceUpdateTime = msTime;
    }

    public int getForceUpdate() {
        Log.d(TAG, "getForceUpdate");
        return this.mForceUpdateTime;
    }
}
