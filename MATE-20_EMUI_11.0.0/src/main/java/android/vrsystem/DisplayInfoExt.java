package android.vrsystem;

import android.view.DisplayInfo;
import com.huawei.annotation.HwSystemApi;

public class DisplayInfoExt {
    private DisplayInfo mDisplayInfo;

    public void setDisplayInfo(DisplayInfo displayInfo) {
        this.mDisplayInfo = displayInfo;
    }

    public DisplayInfo getDisplayInfo() {
        return this.mDisplayInfo;
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
}
