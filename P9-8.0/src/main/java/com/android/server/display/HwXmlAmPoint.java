package com.android.server.display;

import android.util.Log;

public class HwXmlAmPoint {
    private static final boolean HWDEBUG;
    private static final boolean HWFLOW;
    private static final String TAG = "HwXmlAmPoint";
    float x;
    float y;
    float z;

    static {
        boolean z = true;
        boolean isLoggable = !Log.HWLog ? Log.HWModuleLog ? Log.isLoggable(TAG, 3) : false : true;
        HWDEBUG = isLoggable;
        if (!Log.HWINFO) {
            z = Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false;
        }
        HWFLOW = z;
    }

    public HwXmlAmPoint(float inx, float iny, float inz) {
        this.x = inx;
        this.y = iny;
        this.z = inz;
    }

    public String toString() {
        return "Point(" + this.x + ", " + this.y + ", " + this.z + ")";
    }
}
