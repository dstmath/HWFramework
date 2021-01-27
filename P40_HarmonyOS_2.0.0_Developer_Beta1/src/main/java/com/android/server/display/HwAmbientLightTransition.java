package com.android.server.display;

import android.util.Log;
import com.android.server.display.HwBrightnessXmlLoader;

public class HwAmbientLightTransition {
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final boolean HWFLOW;
    private static final String TAG = "HwAmbientLightTransition";
    private final HwBrightnessXmlLoader.Data mData = HwBrightnessXmlLoader.getData();

    static {
        boolean z = false;
        if (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4))) {
            z = true;
        }
        HWFLOW = z;
    }

    public long getNextAmbientLightTransitionTime(HwRingBuffer luxRingBuffer, long time, boolean isBrighen) {
        int buffSize;
        long earliestValidTime = time;
        if (luxRingBuffer == null || (buffSize = luxRingBuffer.size()) < 1) {
            return earliestValidTime;
        }
        for (int i = buffSize - 1; i >= 0; i--) {
            boolean isLuxChange = false;
            if (isBrighen) {
                if (luxRingBuffer.getLux(i) >= this.mData.frontCameraBrightenLuxThreshold) {
                    isLuxChange = true;
                }
            } else if (luxRingBuffer.getLux(i) < this.mData.frontCameraDarkenLuxThreshold) {
                isLuxChange = true;
            }
            if (!isLuxChange) {
                break;
            }
            earliestValidTime = luxRingBuffer.getTime(i);
        }
        HwBrightnessXmlLoader.Data data = this.mData;
        return earliestValidTime + ((long) (isBrighen ? data.frontCameraBrighenDebounceTime : data.frontCameraDarkenDebounceTime));
    }
}
