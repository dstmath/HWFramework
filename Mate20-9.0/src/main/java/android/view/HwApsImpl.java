package android.view;

import android.aps.IApsManager;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.android.hwaps.HwapsWrapper;
import com.huawei.android.hwaps.IEventAnalyzed;
import com.huawei.android.hwaps.IFpsController;

public class HwApsImpl implements IHwApsImpl {
    private static final String APS_DEBUG_PU_ON_STRING = "debug.aps.pu.on";
    private static final String APS_DEBUG_PU_STRING = "debug.aps.pu";
    private static final String APS_PARTIALUPDATE_POWER_POWERTEST = "debug.aps.powertest";
    private static final int APS_PARTIAL_UPDATE_BIT = 33554432;
    private static final String APS_SUPPORT_PROPERTY_STRING = "sys.aps.support";
    private static final int COMPAT_MODE_ENABLE_BIT = 32768;
    private static final String TAG = "HwApsImpl";
    private static volatile HwApsImpl sInstance = null;
    private boolean mApsDebugPartialUpdate = false;
    private boolean mApsInitialized = false;
    private int mApsSupportValue = 0;
    private IEventAnalyzed mEventAnalyzed = null;

    private void HwApsImpl() {
    }

    public static HwApsImpl getDefault() {
        if (sInstance == null) {
            synchronized (HwApsImpl.class) {
                if (sInstance == null) {
                    sInstance = new HwApsImpl();
                    Log.i(TAG, "APS: new HwApsImpl created");
                    boolean z = false;
                    sInstance.mApsSupportValue = SystemProperties.getInt(APS_SUPPORT_PROPERTY_STRING, 0);
                    if (sInstance.isSupportAps()) {
                        sInstance.mEventAnalyzed = HwapsWrapper.getEventAnalyzed();
                        HwApsImpl hwApsImpl = sInstance;
                        if (SystemProperties.getInt(APS_DEBUG_PU_STRING, 0) != 0) {
                            z = true;
                        }
                        hwApsImpl.mApsDebugPartialUpdate = z;
                    }
                }
            }
        }
        return sInstance;
    }

    public void adaptPowerSave(Context context, MotionEvent event) {
        if (this.mEventAnalyzed != null && isSupportAps()) {
            this.mEventAnalyzed.processAnalyze(context, event.getAction(), event.getEventTime(), (int) event.getX(), (int) event.getY(), event.getPointerCount(), event.getDownTime());
        }
    }

    public void initAPS(Context context, int screenWidth, int myPid) {
        if (!this.mApsInitialized) {
            if (this.mEventAnalyzed != null) {
                this.mEventAnalyzed.initAPS(context, screenWidth, myPid);
            }
            this.mApsInitialized = true;
        }
    }

    public boolean isGameProcess(String pkgName) {
        if (this.mEventAnalyzed != null) {
            return this.mEventAnalyzed.isGameProcess(pkgName);
        }
        return false;
    }

    public boolean isSupportAps() {
        return this.mApsSupportValue > 0;
    }

    public boolean isAPSReady() {
        if (this.mEventAnalyzed != null) {
            return this.mEventAnalyzed.isAPSReady();
        }
        return false;
    }

    public void powerCtroll() {
        IFpsController fpsController = HwapsWrapper.getFpsController();
        if (fpsController != null) {
            fpsController.powerCtroll();
        }
    }

    public void setAPSOnPause() {
        if (this.mEventAnalyzed != null) {
            this.mEventAnalyzed.setHasOnPaused(true);
        }
    }

    public boolean StopSdrForSpecial(String strinfo, int keycode) {
        if (this.mEventAnalyzed != null) {
            return this.mEventAnalyzed.StopSdrForSpecial(strinfo, keycode);
        }
        Log.e(TAG, "APS: SDR: mEventAnalyzed is null");
        return false;
    }

    public int getCustScreenDimDurationLocked(int screenOffTimeout) {
        if (this.mEventAnalyzed != null) {
            return this.mEventAnalyzed.getCustScreenDimDurationLocked(screenOffTimeout);
        }
        Log.w(TAG, "APS: Screen Dim: getCustScreenDimDuration eventAnalyzed null");
        return -1;
    }

    public void setLowResolutionMode(Context context, boolean enableLowResolutionMode) {
        Log.i("sdr", "APS: SDR: HwApsImpl.setLowResolutionMod, enableLowResolutionMode = " + enableLowResolutionMode);
        try {
            IApsManager apsManager = HwFrameworkFactory.getApsManager();
            if (apsManager != null) {
                apsManager.setLowResolutionMode(enableLowResolutionMode);
            }
        } catch (Exception e) {
            Log.i("sdr", "APS: SDR: HwApsImpl.setLowResolutionMode, exception = " + e);
        }
    }

    public boolean isLowResolutionSupported() {
        return (this.mApsSupportValue & 32768) != 0;
    }

    public int setGameProcessName(String processName, int pid, int gameType) {
        if (this.mEventAnalyzed != null) {
            return this.mEventAnalyzed.setGameProcessName(processName, pid, gameType);
        }
        Log.w(TAG, "APS: setGameProcessName eventAnalyzed is null");
        return 0;
    }

    public boolean isSupportApsPartialUpdate() {
        boolean z = true;
        if (isDebugPartialUpdateOn()) {
            return SystemProperties.getBoolean(APS_DEBUG_PU_ON_STRING, true);
        }
        if ((this.mApsSupportValue & 33554432) == 0) {
            z = false;
        }
        return z;
    }

    public boolean isDebugPartialUpdateOn() {
        return this.mApsDebugPartialUpdate;
    }

    public boolean isInPowerTest() {
        return SystemProperties.getBoolean(APS_PARTIALUPDATE_POWER_POWERTEST, false);
    }

    public boolean isIn1kResolutionof2kScreen() {
        int width = SystemProperties.getInt("sys.rog.width", 0);
        int realWidth = SystemProperties.getInt("persist.sys.rog.width", 0);
        if (width == 0 || width != realWidth) {
            return false;
        }
        return true;
    }
}
