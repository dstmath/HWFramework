package huawei.android.widget;

import android.content.Context;
import android.os.SystemProperties;
import android.widget.HwSmartSlideOptimize;
import com.huawei.uifirst.smartslide.HighFreqSmartSlideModel;
import com.huawei.uifirst.smartslide.SmartSlideOverScroller;

public class HwSmartSlideOptimizeImpl implements HwSmartSlideOptimize {
    private static final double FLING_DITANCE_DIFF = 0.0d;
    private static final float LENGTH_DELTA = 0.01f;
    private static final float MULTIPLE_LENGTH = 150.0f;
    private static final String TAG = "HwSmartSlideOptimizeImpl";
    private static HwSmartSlideOptimizeImpl sHwSmartSlideOptimizeImpl = null;
    private HighFreqSmartSlideModel mHighFreqSmartSlideModel = null;
    private boolean mIsHighFreqSmartSlideModelEnable = false;
    private boolean mIsOptimizeEnable = false;
    private SmartSlideOverScroller mSmartSlideOverScroller = null;

    private HwSmartSlideOptimizeImpl(Context context) {
        boolean isSmartSlideEnable = SystemProperties.getBoolean("uifirst_listview_optimization_enable", false);
        if (context != null) {
            if (this.mSmartSlideOverScroller == null) {
                this.mSmartSlideOverScroller = new SmartSlideOverScroller(context);
            }
            if (this.mHighFreqSmartSlideModel == null) {
                this.mHighFreqSmartSlideModel = new HighFreqSmartSlideModel();
            }
            if (!isSmartSlideEnable) {
                this.mIsOptimizeEnable = false;
                this.mIsHighFreqSmartSlideModelEnable = false;
                return;
            }
            HighFreqSmartSlideModel highFreqSmartSlideModel = this.mHighFreqSmartSlideModel;
            if (highFreqSmartSlideModel != null) {
                this.mIsHighFreqSmartSlideModelEnable = highFreqSmartSlideModel.isOptimizeEnable();
            }
            this.mIsOptimizeEnable = this.mSmartSlideOverScroller.getAppEnable();
        }
    }

    public boolean isOptimizeEnable() {
        return this.mIsOptimizeEnable;
    }

    public static synchronized HwSmartSlideOptimizeImpl getInstance(Context context) {
        HwSmartSlideOptimizeImpl hwSmartSlideOptimizeImpl;
        synchronized (HwSmartSlideOptimizeImpl.class) {
            if (sHwSmartSlideOptimizeImpl == null) {
                sHwSmartSlideOptimizeImpl = new HwSmartSlideOptimizeImpl(context);
            }
            hwSmartSlideOptimizeImpl = sHwSmartSlideOptimizeImpl;
        }
        return hwSmartSlideOptimizeImpl;
    }

    public int fling(int velocityX, int velocityY, float oldVelocityX, float oldVelocityY, float distance) {
        return this.mSmartSlideOverScroller.fling(velocityX, velocityY, oldVelocityX, oldVelocityY, Math.abs(distance) < LENGTH_DELTA ? 150.0f : distance);
    }

    public double getSplineFlingDistance(int velocity) {
        if (this.mIsHighFreqSmartSlideModelEnable) {
            return this.mHighFreqSmartSlideModel.getSplineFlingDistance(velocity);
        }
        return this.mSmartSlideOverScroller.getSplineFlingDistance(velocity);
    }

    public int getSplineFlingDuration(int velocity) {
        if (this.mIsHighFreqSmartSlideModelEnable) {
            return this.mHighFreqSmartSlideModel.getSplineFlingDuration(velocity);
        }
        return this.mSmartSlideOverScroller.getSplineFlingDuration(velocity);
    }

    public double getUpdateDistance(long currentTime, int splineDuration, int splineDistance) {
        if (this.mIsHighFreqSmartSlideModelEnable) {
            return this.mHighFreqSmartSlideModel.getUpdateDistance(currentTime, splineDuration, splineDistance, FLING_DITANCE_DIFF);
        }
        return this.mSmartSlideOverScroller.getUpdateDistance(currentTime, splineDuration, splineDistance, FLING_DITANCE_DIFF);
    }

    public float getUpdateVelocity(long currentTime, int splineDuration, int velocity) {
        if (this.mIsHighFreqSmartSlideModelEnable) {
            return this.mHighFreqSmartSlideModel.getUpdateVelocity(currentTime, splineDuration, velocity);
        }
        return this.mSmartSlideOverScroller.getUpdateVelocity(currentTime, splineDuration, velocity);
    }

    public int adjustDuration(int adjustDistance, int splineDuration, int splineDistance) {
        return this.mSmartSlideOverScroller.getAdjustDuratuion(adjustDistance, splineDuration, splineDistance, FLING_DITANCE_DIFF);
    }
}
