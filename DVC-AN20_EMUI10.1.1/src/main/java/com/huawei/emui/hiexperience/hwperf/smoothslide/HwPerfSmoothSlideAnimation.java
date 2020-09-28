package com.huawei.emui.hiexperience.hwperf.smoothslide;

import android.content.Context;
import android.hwcontrol.HwWidgetFactory;
import android.widget.HwSmartSlideOptimize;
import com.huawei.emui.hiexperience.hwperf.HwPerfBase;
import com.huawei.emui.hiexperience.hwperf.utils.HwLog;

public class HwPerfSmoothSlideAnimation extends HwPerfBase {
    private static final String TAG = "HwPerfSmoothSlideAnimation";
    private static HwSmartSlideOptimize sHwSmartSlideOptimize = null;

    public HwPerfSmoothSlideAnimation(Context context) {
        if (context == null) {
            HwLog.e(TAG, " Context is null! ");
        } else if (sHwSmartSlideOptimize == null) {
            sHwSmartSlideOptimize = HwWidgetFactory.getHwSmartSlideOptimize(context);
        }
    }

    public boolean isOptimizeEnable() {
        return sHwSmartSlideOptimize.isOptimizeEnable();
    }

    public int fling(int velocityX, int velocityY, float oldVelocityX, float oldVelocityY, float distance) {
        return sHwSmartSlideOptimize.fling(velocityX, velocityY, oldVelocityX, oldVelocityY, distance);
    }

    public double getSplineFlingDistance(int velocity) {
        return sHwSmartSlideOptimize.getSplineFlingDistance(velocity);
    }

    public int getSplineFlingDuration(int velocity) {
        return sHwSmartSlideOptimize.getSplineFlingDuration(velocity);
    }

    public double getUpdateDistance(long currentTime, int splineDuration, int splineDistance) {
        return sHwSmartSlideOptimize.getUpdateDistance(currentTime, splineDuration, splineDistance);
    }

    public float getUpdateVelocity(long currentTime, int splineDuration, int velocity) {
        return sHwSmartSlideOptimize.getUpdateVelocity(currentTime, splineDuration, velocity);
    }

    public int adjustDuration(int adjustDistance, int splineDuration, int splineDistance) {
        return sHwSmartSlideOptimize.adjustDuration(adjustDistance, splineDuration, splineDistance);
    }
}
