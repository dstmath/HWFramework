package com.android.server.forcerotation;

import android.os.IBinder;
import java.lang.ref.WeakReference;

public class ForceRotationAppInfo {
    private WeakReference<IBinder> mAppToken;
    private int mOrientation;
    private String mPackageName;

    public ForceRotationAppInfo() {
    }

    public ForceRotationAppInfo(String packageName, IBinder aToken, int orientation) {
        this.mPackageName = packageName;
        this.mAppToken = new WeakReference<>(aToken);
        this.mOrientation = orientation;
    }

    public String getmPackageName() {
        return this.mPackageName;
    }

    public void setmPackageName(String mPackageName2) {
        this.mPackageName = mPackageName2;
    }

    public WeakReference<IBinder> getmAppToken() {
        return this.mAppToken;
    }

    public void setmAppToken(WeakReference<IBinder> mAppToken2) {
        this.mAppToken = mAppToken2;
    }

    public int getmOrientation() {
        return this.mOrientation;
    }

    public void setmOrientation(int mOrientation2) {
        this.mOrientation = mOrientation2;
    }
}
