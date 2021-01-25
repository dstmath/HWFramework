package com.android.server.rms.iaware.hiber.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.rms.iaware.AwareLog;
import java.util.Arrays;

public class HiberBean implements Parcelable {
    private static final String TAG = "AppHiber_Mgr_Bean";
    public int funcId;
    public int operateType;
    public int[] pidArray;
    public String pkgName;

    public HiberBean(int funcId2, String pkgName2, int[] pidArray2, int operateType2) {
        this.funcId = funcId2;
        this.pkgName = pkgName2;
        if (pidArray2 != null) {
            this.pidArray = Arrays.copyOf(pidArray2, pidArray2.length);
        }
        this.operateType = operateType2;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        if (dest == null) {
            AwareLog.w(TAG, "dest is null!");
            return;
        }
        dest.writeInt(this.funcId);
        if (flags == 1) {
            dest.writeString(this.pkgName);
            dest.writeIntArray(this.pidArray);
        }
        dest.writeInt(this.operateType);
    }
}
