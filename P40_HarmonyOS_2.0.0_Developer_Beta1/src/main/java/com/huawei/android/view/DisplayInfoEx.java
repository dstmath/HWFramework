package com.huawei.android.view;

import android.view.Display;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import com.huawei.annotation.HwSystemApi;

public class DisplayInfoEx {
    private DisplayInfo mDisplayInfo = new DisplayInfo();

    public void setDisplayInfo(DisplayInfo displayInfo) {
        this.mDisplayInfo = displayInfo;
    }

    public DisplayInfo getDisplayInfo() {
        return this.mDisplayInfo;
    }

    @HwSystemApi
    public int getLogicalDensityDpi() {
        DisplayInfo displayInfo = this.mDisplayInfo;
        if (displayInfo != null) {
            return displayInfo.logicalDensityDpi;
        }
        return 1;
    }

    @HwSystemApi
    public int getLogicalWidth() {
        DisplayInfo displayInfo = this.mDisplayInfo;
        if (displayInfo != null) {
            return displayInfo.logicalWidth;
        }
        return 0;
    }

    @HwSystemApi
    public int getLogicalHeight() {
        DisplayInfo displayInfo = this.mDisplayInfo;
        if (displayInfo != null) {
            return displayInfo.logicalHeight;
        }
        return 0;
    }

    @HwSystemApi
    public int getType() {
        DisplayInfo displayInfo = this.mDisplayInfo;
        if (displayInfo != null) {
            return displayInfo.type;
        }
        return 0;
    }

    @HwSystemApi
    public String getUniqueId() {
        DisplayInfo displayInfo = this.mDisplayInfo;
        if (displayInfo != null) {
            return displayInfo.uniqueId;
        }
        return "";
    }

    @HwSystemApi
    public String getName() {
        DisplayInfo displayInfo = this.mDisplayInfo;
        if (displayInfo != null) {
            return displayInfo.name;
        }
        return "";
    }

    @HwSystemApi
    public Display.Mode getDefaultMode() {
        DisplayInfo displayInfo = this.mDisplayInfo;
        if (displayInfo != null) {
            return displayInfo.getDefaultMode();
        }
        return null;
    }

    @HwSystemApi
    public int getNaturalWidth() {
        DisplayInfo displayInfo = this.mDisplayInfo;
        if (displayInfo != null) {
            return displayInfo.getNaturalWidth();
        }
        return 0;
    }

    @HwSystemApi
    public int getNaturalHeight() {
        DisplayInfo displayInfo = this.mDisplayInfo;
        if (displayInfo != null) {
            return displayInfo.getNaturalHeight();
        }
        return 0;
    }

    @HwSystemApi
    public static boolean isInstanceOf(Object object) {
        return object instanceof DisplayInfo;
    }

    @HwSystemApi
    public void setLogicalWidth(int logicalWidth) {
        DisplayInfo displayInfo = this.mDisplayInfo;
        if (displayInfo != null) {
            displayInfo.logicalWidth = logicalWidth;
        }
    }

    @HwSystemApi
    public void setAppWidth(int appWidth) {
        DisplayInfo displayInfo = this.mDisplayInfo;
        if (displayInfo != null) {
            displayInfo.appWidth = appWidth;
        }
    }

    @HwSystemApi
    public int getAppWidth() {
        DisplayInfo displayInfo = this.mDisplayInfo;
        if (displayInfo != null) {
            return displayInfo.appWidth;
        }
        return 0;
    }

    @HwSystemApi
    public void setLogicalHeight(int logicalHeight) {
        DisplayInfo displayInfo = this.mDisplayInfo;
        if (displayInfo != null) {
            displayInfo.logicalHeight = logicalHeight;
        }
    }

    @HwSystemApi
    public void setAppHeight(int appHeight) {
        DisplayInfo displayInfo = this.mDisplayInfo;
        if (displayInfo != null) {
            displayInfo.appHeight = appHeight;
        }
    }

    @HwSystemApi
    public int getAppHeight() {
        DisplayInfo displayInfo = this.mDisplayInfo;
        if (displayInfo != null) {
            return displayInfo.appHeight;
        }
        return 0;
    }

    @HwSystemApi
    public void setRotation(int rotation) {
        DisplayInfo displayInfo = this.mDisplayInfo;
        if (displayInfo != null) {
            displayInfo.rotation = rotation;
        }
    }

    @HwSystemApi
    public int getRotation() {
        DisplayInfo displayInfo = this.mDisplayInfo;
        if (displayInfo != null) {
            return displayInfo.rotation;
        }
        return 0;
    }

    @HwSystemApi
    public DisplayCutout getDisplayCutout() {
        DisplayInfo displayInfo = this.mDisplayInfo;
        if (displayInfo != null) {
            return displayInfo.displayCutout;
        }
        return null;
    }

    @HwSystemApi
    public void resetDisplayInfo(Object displayInfoObj) {
        if (displayInfoObj instanceof DisplayInfo) {
            this.mDisplayInfo = (DisplayInfo) displayInfoObj;
        }
    }

    @HwSystemApi
    public void copyFrom(DisplayInfoEx other) {
        DisplayInfo displayInfo = this.mDisplayInfo;
        if (displayInfo != null) {
            displayInfo.copyFrom(other.mDisplayInfo);
        }
    }

    @HwSystemApi
    public boolean isEmpty() {
        return this.mDisplayInfo == null;
    }

    @HwSystemApi
    public static boolean getDisplayInfo(Display display, DisplayInfoEx displayInfoEx) {
        if (display == null || displayInfoEx == null) {
            return false;
        }
        return display.getDisplayInfo(displayInfoEx.getDisplayInfo());
    }
}
