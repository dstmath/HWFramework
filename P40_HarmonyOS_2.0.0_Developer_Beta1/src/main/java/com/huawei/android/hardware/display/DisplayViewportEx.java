package com.huawei.android.hardware.display;

import android.graphics.Rect;
import android.hardware.display.DisplayViewport;
import android.util.Log;

public class DisplayViewportEx {
    private static final String TAG = "DisplayViewportEx";
    private DisplayViewport mDisplayViewport;

    public void setDisplayViewport(DisplayViewport displayViewport) {
        this.mDisplayViewport = displayViewport;
    }

    public DisplayViewport getDisplayViewport() {
        return this.mDisplayViewport;
    }

    public int getOrientation() {
        DisplayViewport displayViewport = this.mDisplayViewport;
        if (displayViewport != null) {
            return displayViewport.orientation;
        }
        Log.e(TAG, "DisplayViewport is null");
        return 0;
    }

    public Rect getLogicalFrame() {
        DisplayViewport displayViewport = this.mDisplayViewport;
        if (displayViewport != null) {
            return displayViewport.logicalFrame;
        }
        Log.e(TAG, "DisplayViewport is null");
        return null;
    }

    public Rect getPhysicalFrame() {
        DisplayViewport displayViewport = this.mDisplayViewport;
        if (displayViewport != null) {
            return displayViewport.physicalFrame;
        }
        Log.e(TAG, "DisplayViewport is null");
        return null;
    }
}
