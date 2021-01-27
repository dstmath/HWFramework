package huawei.android.widget;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import android.widget.HwSmartSlideOptimize;
import com.huawei.uifirst.smartslide.HighFreqSmartSlideModel;
import com.huawei.uifirst.smartslide.SmartSlideOverScroller;

public class HwSmartSlideOptimizeImpl implements HwSmartSlideOptimize {
    private static final boolean DEBUG_SMART_SLIDE_LOG = SystemProperties.getBoolean("ro.config.hw_smart_slide_log", false);
    private static final int DEFAULT_SCREEN_FREQ = 1;
    private static final double FLING_DITANCE_DIFF = 0.0d;
    private static final boolean IS_M_PLATFORM_AND_NEED_GPU_POLICY = SystemProperties.get("ro.board.platform", "unknow").startsWith("mt68");
    private static final float LENGTH_DELTA = 0.01f;
    private static final float MULTIPLE_LENGTH = 150.0f;
    private static final String PRODUCT_SCREEN_FREQ = "persist.sys.hw_screen_freq";
    private static final int SET_GPU_POLICY = 20030;
    private static final String STANDARD_FREQ_SLIDE = "persist.sys.standard_freq_slide_enable";
    private static final int STANDARD_SCREEN_FREQ = 0;
    private static final String TAG = "HwSmartSlideOptimizeImpl";
    private static HwSmartSlideOptimizeImpl sHwSmartSlideOptimizeImpl = null;
    private IBinder mAgpService = null;
    private HighFreqSmartSlideModel mHighFreqSmartSlideModel = null;
    private boolean mIsHighFreqSmartSlideModelEnable = false;
    private boolean mIsOptimizeEnable = false;
    private SmartSlideOverScroller mSmartSlideOverScroller = null;

    private HwSmartSlideOptimizeImpl(Context context) {
        boolean isSmartSlideEnable = SystemProperties.getBoolean("uifirst_listview_optimization_enable", false);
        if (context != null) {
            if (IS_M_PLATFORM_AND_NEED_GPU_POLICY) {
                this.mAgpService = ServiceManager.getService("AGPService");
                if (this.mAgpService == null) {
                    Log.e(TAG, "get AGPService fail");
                }
            }
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
            int highScreenFreq = SystemProperties.getInt(PRODUCT_SCREEN_FREQ, 0);
            int standardFreqSlide = SystemProperties.getInt(STANDARD_FREQ_SLIDE, 1);
            if (highScreenFreq == 0 && standardFreqSlide == 0) {
                this.mIsOptimizeEnable = false;
            } else {
                this.mIsOptimizeEnable = this.mSmartSlideOverScroller.getAppEnable();
            }
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
        int initVelocity = this.mSmartSlideOverScroller.fling(velocityX, velocityY, oldVelocityX, oldVelocityY, Math.abs(distance) < LENGTH_DELTA ? 150.0f : distance);
        if (IS_M_PLATFORM_AND_NEED_GPU_POLICY && this.mAgpService != null && velocityY > 0 && velocityX == 0) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                this.mAgpService.transact(SET_GPU_POLICY, data, reply, 1);
            } catch (RemoteException e) {
                Log.e(TAG, "sendFlingMsgToAGP failed!!!");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
            reply.recycle();
            data.recycle();
        }
        return initVelocity;
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
        double updateDistance;
        if (this.mIsHighFreqSmartSlideModelEnable) {
            updateDistance = this.mHighFreqSmartSlideModel.getUpdateDistance(currentTime, splineDuration, splineDistance, FLING_DITANCE_DIFF);
        } else {
            updateDistance = this.mSmartSlideOverScroller.getUpdateDistance(currentTime, splineDuration, splineDistance, FLING_DITANCE_DIFF);
        }
        if (DEBUG_SMART_SLIDE_LOG) {
            Log.i(TAG, "updateDistance:" + updateDistance + ", splineDistance:" + splineDistance + ", splineDuration:" + splineDuration);
        }
        return updateDistance;
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
