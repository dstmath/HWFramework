package android.view;

import android.aps.IApsManager;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.android.hwaps.HwapsWrapper;
import com.huawei.android.hwaps.IEventAnalyzed;
import com.huawei.android.hwaps.IFpsController;
import com.huawei.android.hwaps.ISmartLowpowerBrowser;
import java.util.HashMap;
import java.util.Map;

public class HwNsdImpl implements IHwNsdImpl {
    private static final int APS_SUPPORT_2DSDR = 4096;
    private static final int COMPAT_MODE_ENABLE_BIT = 32768;
    private static final int CONFIGTYPE_BLACKLIST = 7000;
    private static final int CONFIGTYPE_QUERYRESULTLIST = 7001;
    private static final int CONFIGTYPE_WHITELIST = 9998;
    private static final String TAG = "APS";
    private static HwNsdImpl sInstance = null;
    private Context mContext = null;
    private IEventAnalyzed mEventAnalyzed = null;
    private IFpsController mFpsController = null;
    private boolean mIsFirstEnterAPS = true;
    private Map<String, Integer> mMapCheckResult = new HashMap();
    private ISmartLowpowerBrowser mSmartLowpowerBrower = null;

    protected void HwNsdImpl() {
        Log.e(TAG, "aps new HwNsdImpl ");
    }

    public static synchronized HwNsdImpl getDefault() {
        HwNsdImpl hwNsdImpl;
        synchronized (HwNsdImpl.class) {
            if (sInstance == null) {
                sInstance = new HwNsdImpl();
            }
            hwNsdImpl = sInstance;
        }
        return hwNsdImpl;
    }

    public boolean checkAdBlock(View inView, String pkgName) {
        boolean retBlockStatus = false;
        int prop = SystemProperties.getInt("debug.aps.enable", 0);
        if ((prop != 1 && prop != 2 && prop != 9999) || inView == null) {
            return false;
        }
        String viewClsName = inView.getClass().getName();
        if (this.mMapCheckResult.containsKey(viewClsName)) {
            if (1 == ((Integer) this.mMapCheckResult.get(viewClsName)).intValue()) {
                blockAdView(inView);
                retBlockStatus = true;
            }
            return retBlockStatus;
        }
        IEventAnalyzed eventAnalyzed = HwapsWrapper.getEventAnalyzed();
        if (eventAnalyzed != null && eventAnalyzed.isAdCheckEnable(pkgName)) {
            return checkViewAndParent(inView, viewClsName, eventAnalyzed, false);
        }
        return false;
    }

    private boolean checkViewAndParent(View inView, String viewClsName, IEventAnalyzed eventAnalyzed, boolean retBlockStatus) {
        int checkResult;
        int nCount = 0;
        String clsName = "";
        ViewParent parent = null;
        while (true) {
            if (nCount == 0) {
                clsName = viewClsName;
                parent = inView.getParent();
            } else if (parent != null) {
                clsName = parent.getClass().getName();
                parent = parent.getParent();
            }
            checkResult = eventAnalyzed.checkAd(clsName);
            if (checkResult <= 0) {
                nCount++;
                if (parent == null || nCount >= 5) {
                    break;
                }
            } else {
                break;
            }
        }
        if (1 == checkResult) {
            blockAdView(inView);
            this.mMapCheckResult.put(viewClsName, Integer.valueOf(1));
            return true;
        } else if (2 == checkResult) {
            unBlockAdView(inView);
            return retBlockStatus;
        } else {
            this.mMapCheckResult.put(viewClsName, Integer.valueOf(0));
            return retBlockStatus;
        }
    }

    private void blockAdView(View inView) {
        if (inView.getVisibility() != 8) {
            inView.setVisibility(8);
            Log.i("AdCheck", "APS: blockAdView! ");
        }
    }

    private void unBlockAdView(View inView) {
        if (inView.getVisibility() == 8) {
            inView.setVisibility(0);
            Log.i("AdCheck", "APS: unBlockAdView! ");
        }
    }

    public void adaptPowerSave(Context context, MotionEvent event) {
        createEventAnalyzed();
        if (this.mEventAnalyzed != null && isSupportAps()) {
            this.mEventAnalyzed.processAnalyze(context, event.getAction(), event.getEventTime(), (int) event.getX(), (int) event.getY(), event.getPointerCount(), event.getDownTime());
        }
    }

    public void initAPS(Context context, int screenWidth, int myPid) {
        if (this.mIsFirstEnterAPS) {
            createEventAnalyzed();
            if (this.mEventAnalyzed != null) {
                this.mEventAnalyzed.initAPS(context, screenWidth, myPid);
            }
            this.mIsFirstEnterAPS = false;
        }
    }

    public synchronized void createEventAnalyzed() {
        if (this.mEventAnalyzed == null) {
            this.mEventAnalyzed = HwapsWrapper.getEventAnalyzed();
        }
    }

    public boolean isGameProcess(String pkgName) {
        createEventAnalyzed();
        if (this.mEventAnalyzed != null) {
            return this.mEventAnalyzed.isGameProcess(pkgName);
        }
        return false;
    }

    public boolean isSupportAps() {
        int support = SystemProperties.getInt("sys.aps.support", 0);
        if (support <= 0 || 268435456 == (support & 268435456)) {
            return false;
        }
        return true;
    }

    public boolean isSupportAPSEventAnalysis() {
        return 1 == (SystemProperties.getInt("sys.aps.support", 0) & 1);
    }

    public boolean isAPSReady() {
        if (this.mEventAnalyzed == null) {
            this.mEventAnalyzed = HwapsWrapper.getEventAnalyzed();
        }
        if (this.mEventAnalyzed != null) {
            return this.mEventAnalyzed.isAPSReady();
        }
        return false;
    }

    public void powerCtroll() {
        if (this.mFpsController == null) {
            this.mFpsController = HwapsWrapper.getFpsController();
        } else {
            this.mFpsController.powerCtroll();
        }
    }

    public void setAPSOnPause() {
        if (this.mEventAnalyzed == null) {
            this.mEventAnalyzed = HwapsWrapper.getEventAnalyzed();
        }
        if (this.mEventAnalyzed != null) {
            this.mEventAnalyzed.setHasOnPaused(true);
        }
    }

    public boolean StopSdrForSpecial(String strinfo, int keycode) {
        if (this.mEventAnalyzed == null) {
            this.mEventAnalyzed = HwapsWrapper.getEventAnalyzed();
        }
        if (this.mEventAnalyzed != null) {
            return this.mEventAnalyzed.StopSdrForSpecial(strinfo, keycode);
        }
        Log.e(TAG, "APS: SDR: mEventAnalyzed is null");
        return false;
    }

    /* JADX WARNING: Missing block: B:9:0x001e, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:17:0x0044, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean checkIs2DSDRCase(Context context, ViewRootImpl viewRoot) {
        String APP_NAME_WILD_CARD_CHAR = "*";
        if (viewRoot == null || context == null) {
            Log.i("2DSDR", "APS: 2DSDR: AbsListView.java, viewRoot or context is null");
            return false;
        }
        ApplicationInfo info = context.getApplicationInfo();
        if (info == null || info.targetSdkVersion < 14 || (SystemProperties.getInt("sys.aps.support", 0) & 4096) == 0) {
            return false;
        }
        float startRatio = Float.parseFloat(SystemProperties.get("sys.2dsdr.startratio", "-1.0"));
        if (startRatio <= 0.0f || 1.0f <= startRatio || viewRoot.getView() == null) {
            return false;
        }
        Display display = viewRoot.getView().getDisplay();
        if (display == null || display.getDisplayId() != 0) {
            return false;
        }
        String appPkgName = context.getApplicationInfo().packageName;
        String targetPkgName = SystemProperties.get("sys.2dsdr.pkgname", "");
        if (targetPkgName.equals("*") || (targetPkgName.equals(appPkgName) ^ 1) == 0) {
            return true;
        }
        return false;
    }

    private float computeSDRStartRatio(Context context, View rootView, View scrollView) {
        float areaRatio = (float) ((((double) (scrollView.getWidth() * scrollView.getHeight())) * 1.0d) / ((double) (rootView.getWidth() * rootView.getHeight())));
        if (((double) areaRatio) < 0.7d) {
            return -1.0f;
        }
        float startRatio = Float.parseFloat(SystemProperties.get("sys.2dsdr.startratio", "-1.0"));
        if (0.0f >= startRatio || startRatio >= 1.0f) {
            return (float) (240.0d / ((double) (context.getResources().getDisplayMetrics().ydpi * areaRatio)));
        }
        return startRatio;
    }

    private float doComputeSDRRatioChange(float startRatio, float initialVelocity, float currentVelocity, int ratioBase) {
        int startNum = (int) Math.ceil((double) (((float) ratioBase) * startRatio));
        if (startNum == ratioBase) {
            return -1.0f;
        }
        return (float) ((((double) (startNum + ((int) (0.5d + (((double) (Math.abs(initialVelocity) - Math.abs(currentVelocity))) / ((double) (Math.abs(initialVelocity) / ((float) (ratioBase - startNum))))))))) * 1.0d) / ((double) ratioBase));
    }

    /* JADX WARNING: Missing block: B:4:0x0011, code:
            return -1.0f;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public float computeSDRRatio(Context context, View rootView, View scrollView, float initialVelocity, float currentVelocity, int ratioBase) {
        float startRatio = computeSDRStartRatio(context, rootView, scrollView);
        if (startRatio < 0.0f || 1.0f < startRatio || ratioBase <= 1) {
            return -1.0f;
        }
        return doComputeSDRRatioChange(startRatio, initialVelocity, currentVelocity, ratioBase);
    }

    public int computeSDRRatioBase(Context context, View viewRoot, View scrollView) {
        if (viewRoot.getWidth() == scrollView.getWidth() && viewRoot.getHeight() == scrollView.getHeight()) {
            for (int ratioBase = 16; ratioBase > 1; ratioBase--) {
                if (viewRoot.getHeight() % ratioBase == 0) {
                    return ratioBase;
                }
            }
        } else {
            float density = context.getResources().getDisplayMetrics().density;
            if (((int) (10.0f * density)) % 10 == 0) {
                return (int) density;
            }
        }
        return -1;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public String[] getCustAppList(int type) {
        IEventAnalyzed eventAnalyzed = HwapsWrapper.getEventAnalyzed();
        String[] result = new String[0];
        if (eventAnalyzed != null && this.mContext != null) {
            return eventAnalyzed.getCustAppList(this.mContext, type);
        }
        Log.w(TAG, "APS: SDR: Customized2d: getCustApplist eventAnalyzed null");
        return result;
    }

    public int getCustScreenDimDurationLocked(int screenOffTimeout) {
        IEventAnalyzed eventAnalyzed = HwapsWrapper.getEventAnalyzed();
        if (eventAnalyzed != null) {
            return eventAnalyzed.getCustScreenDimDurationLocked(screenOffTimeout);
        }
        Log.w(TAG, "APS: Screen Dim: getCustScreenDimDuration eventAnalyzed null");
        return -1;
    }

    public String[] getCustAppList(Context context, int type) {
        IEventAnalyzed eventAnalyzed = HwapsWrapper.getEventAnalyzed();
        String[] result = new String[0];
        if (eventAnalyzed != null && context != null) {
            return eventAnalyzed.getCustAppList(context, type);
        }
        Log.w(TAG, "APS: SDR: Customized2d: getCustApplist eventAnalyzed null");
        return result;
    }

    public String[] getQueryResultGameList(Context context, int type) {
        IEventAnalyzed eventAnalyzed = HwapsWrapper.getEventAnalyzed();
        String[] result = new String[0];
        if (eventAnalyzed != null && context != null) {
            return eventAnalyzed.getQueryResultGameList(context, type);
        }
        Log.w(TAG, "APS: SDR: HwNsdImp: getQueryResultGameList eventAnalyzed null or context is null.");
        return result;
    }

    public void setLowResolutionMode(Context context, boolean enableLowResolutionMode) {
        Log.i("sdr", "APS: SDR: HwNsdImpl.setLowResolutionMod, enableLowResolutionMode = " + enableLowResolutionMode);
        IApsManager apsManager = HwFrameworkFactory.getApsManager();
        if (apsManager != null) {
            apsManager.setLowResolutionMode(enableLowResolutionMode ? 1 : 0);
        }
    }

    public boolean isLowResolutionSupported() {
        if ((32768 & SystemProperties.getInt("sys.aps.support", 0)) != 0) {
            return true;
        }
        return false;
    }

    private synchronized void createSmartLowpowerBrowser() {
        if (this.mSmartLowpowerBrower == null) {
            this.mSmartLowpowerBrower = HwapsWrapper.getSmartLowpowerBrowser();
        }
    }

    public boolean isSLBSwitchOn(String pkgName) {
        createSmartLowpowerBrowser();
        if (this.mSmartLowpowerBrower != null) {
            return this.mSmartLowpowerBrower.initSLB(pkgName);
        }
        return false;
    }

    public boolean doProcessDrawSLB(long drawingTime, boolean viewScrollChanged, boolean handlingPointerEvent) {
        createSmartLowpowerBrowser();
        return this.mSmartLowpowerBrower != null ? this.mSmartLowpowerBrower.doProcessDrawSLB(drawingTime, viewScrollChanged, handlingPointerEvent) : false;
    }

    public void setFrameScheduledSLB() {
        createSmartLowpowerBrowser();
        if (this.mSmartLowpowerBrower != null) {
            this.mSmartLowpowerBrower.setFrameScheduledSLB();
        }
    }

    public void setPlayingVideoSLB(boolean isPlayingVideo) {
        createSmartLowpowerBrowser();
        if (this.mSmartLowpowerBrower != null) {
            this.mSmartLowpowerBrower.setPlayingVideoSLB(isPlayingVideo);
        }
    }
}
