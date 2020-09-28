package com.huawei.android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class LeRangingResult implements Parcelable {
    public static final Parcelable.Creator<LeRangingResult> CREATOR = new Parcelable.Creator<LeRangingResult>() {
        /* class com.huawei.android.bluetooth.LeRangingResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public LeRangingResult createFromParcel(Parcel in) {
            return new LeRangingResult(in);
        }

        @Override // android.os.Parcelable.Creator
        public LeRangingResult[] newArray(int size) {
            return new LeRangingResult[size];
        }
    };
    private static final String TAG = "LeRangingResult";
    private int mCallbackType;
    private int mTargetRange;
    private int mTolernace;

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mCallbackType);
        out.writeInt(this.mTolernace);
        out.writeInt(this.mTargetRange);
    }

    public int describeContents() {
        return 0;
    }

    private LeRangingResult(Parcel in) {
        this.mCallbackType = in.readInt();
        this.mTolernace = in.readInt();
        this.mTargetRange = in.readInt();
    }

    public LeRangingResult(int callbackType, int tolernace, int targetRange) {
        this.mCallbackType = callbackType;
        this.mTolernace = tolernace;
        this.mTargetRange = targetRange;
    }

    public int getCallbackType() {
        Log.d(TAG, "getCallbackType " + this.mTargetRange);
        return this.mCallbackType;
    }

    public int getTargetRange() {
        Log.d(TAG, "getTargetRange " + this.mTargetRange);
        return this.mTargetRange;
    }

    public int getTolernace() {
        Log.d(TAG, "getTolernace " + this.mTolernace);
        return this.mTolernace;
    }
}
