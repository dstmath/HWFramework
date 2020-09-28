package android.magicwin;

import android.app.Activity;
import android.app.ActivityThread;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.magicwin.IHwMagicWindow;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.HwMwUtils;
import android.util.Slog;
import android.view.DisplayInfo;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import com.huawei.utils.HwPartResourceUtils;
import java.util.ArrayList;
import java.util.Iterator;

public class HwMagicWindowImpl implements HwMagicWindow {
    private static final int DEFAULT_WIDTH = -1;
    private static final int DISPLAY_INFO_PARAM_SIZE = 1;
    private static final int HOSTVIEW_PARAM_SIZE = 1;
    private static final int MAX_OFFSET_HEIGHT = 10;
    private static final int SETSCREENRECT_PARAM_SIZE = 3;
    private static final String TAG = "HwMagicWindowImpl";
    private static final int UPDATERESOURCES_PARAM_SIZE = 3;
    private static HwMagicWindowImpl sInstance = null;
    private int mDisplayWidth = -1;
    private Bundle mEmptyBundle = new Bundle();
    private boolean mIsFirstDetect = true;
    private boolean mNeedDetect = false;
    private IHwMagicWindow mService = null;
    private int mViewCountLevel = 0;

    public static synchronized HwMagicWindowImpl getDefault() {
        HwMagicWindowImpl hwMagicWindowImpl;
        synchronized (HwMagicWindowImpl.class) {
            if (sInstance == null) {
                sInstance = new HwMagicWindowImpl();
            }
            hwMagicWindowImpl = sInstance;
        }
        return hwMagicWindowImpl;
    }

    public IHwMagicWindow getService() {
        checkService();
        return this.mService;
    }

    private HwMagicWindowImpl() {
    }

    private boolean checkService() {
        if (this.mService != null) {
            return true;
        }
        this.mService = IHwMagicWindow.Stub.asInterface(ServiceManager.getService("HwMagicWindowService"));
        if (this.mService != null) {
            return true;
        }
        Slog.e(TAG, "check service failed");
        return false;
    }

    private boolean checkParams(Object... params) {
        return (params == null || params.length == 0) ? false : true;
    }

    public Bundle performHwMagicWindowPolicy(int policy, Object... params) {
        if (policy == 11) {
            detectFeatureForMainWindow(params);
            return this.mEmptyBundle;
        } else if (policy == 12) {
            return updateResource(params);
        } else {
            if (policy == 81) {
                updateDisplayInfo(params);
                return this.mEmptyBundle;
            } else if (policy == 82) {
                return isDisableSensor(params);
            } else {
                if (policy == 1001) {
                    return handleCoverView(params);
                }
                if (policy == 1002) {
                    setScreenRectForMagicWindow(params);
                    return this.mEmptyBundle;
                } else if (!checkService() || !checkParams(params)) {
                    Slog.e(TAG, "service has not been started");
                    return this.mEmptyBundle;
                } else {
                    ArrayList<Object> list = new ArrayList<>(params.length);
                    for (Object param : params) {
                        list.add(param);
                    }
                    try {
                        return this.mService.performHwMagicWindowPolicy(policy, list);
                    } catch (RemoteException e) {
                        Slog.e(TAG, "Remote Exception e : " + e);
                        return new Bundle();
                    }
                }
            }
        }
    }

    private Bundle handleCoverView(Object... params) {
        if (params == null || params.length != 2) {
            return this.mEmptyBundle;
        }
        if (params.length == 2 && (params[0] instanceof Activity) && (params[1] instanceof ArrayList)) {
            Context context = (Context) params[0];
            if (context == null || params[1] == null) {
                return this.mEmptyBundle;
            }
            int windowMode = context.getResources().getConfiguration().windowConfiguration.getWindowingMode();
            if (windowMode == 103 && ((ArrayList) params[1]).size() == 0) {
                ViewGroup decorView = (ViewGroup) ((Activity) context).getWindow().getDecorView();
                int childCount = decorView.getChildCount();
                int maxHeight = context.getResources().getDimensionPixelOffset(HwPartResourceUtils.getResourceId("status_bar_height")) + 10;
                for (int i = 0; i < childCount; i++) {
                    collectCoverView(decorView.getChildAt(i), (ArrayList) params[1], maxHeight);
                }
            }
            if (((ArrayList) params[1]).size() == 0) {
                return this.mEmptyBundle;
            }
            Iterator it = ((ArrayList) params[1]).iterator();
            while (it.hasNext()) {
                ((View) it.next()).setAlpha(windowMode == 103 ? 0.0f : 1.0f);
            }
        }
        return this.mEmptyBundle;
    }

    private void collectCoverView(View v, ArrayList<View> viewList, int maxHeight) {
        if (!(v instanceof ViewGroup)) {
            int width = v.getLayoutParams().width;
            int height = v.getLayoutParams().height;
            if ((width == -1 && height > 0 && height <= maxHeight) && !v.isFocusable()) {
                viewList.add(v);
            }
        }
    }

    private Bundle updateResource(Object... params) {
        Bundle result = new Bundle();
        if (params.length != 3 || !(params[0] instanceof Configuration) || !(params[1] instanceof Boolean)) {
            return result;
        }
        Configuration cur = (Configuration) params[0];
        Boolean isUpdate = (Boolean) params[1];
        Configuration old = null;
        if (isUpdate.booleanValue()) {
            old = ActivityThread.currentActivityThread().getOverrideConfig();
        } else if (params[2] instanceof Configuration) {
            old = (Configuration) params[2];
        }
        int oldMode = 0;
        int curMode = cur.windowConfiguration.getWindowingMode();
        if (old != null) {
            oldMode = old.windowConfiguration.getWindowingMode();
        }
        Boolean isFromOrToMw = false;
        if (oldMode == 103 || curMode == 103) {
            isFromOrToMw = true;
        }
        if (!isFromOrToMw.booleanValue()) {
            return result;
        }
        if (isUpdate.booleanValue()) {
            ActivityThread.currentActivityThread().updateOverrideConfig(cur);
            ActivityThread.currentActivityThread().applyConfigurationToResources(cur);
        } else if (old.screenWidthDp != cur.screenWidthDp) {
            result.putBoolean("UPDATE_RESOURCE", true);
        }
        return result;
    }

    private void detectFeatureForMainWindow(Object... params) {
        View view;
        if (params.length == 1 && (params[0] instanceof View) && (view = (View) params[0]) != null) {
            if (HwMwUtils.ENABLED && this.mIsFirstDetect && ActivityThread.currentPackageName() != null) {
                Bundle bundle = performHwMagicWindowPolicy(31, ActivityThread.currentPackageName());
                this.mNeedDetect = bundle.getBoolean("NEED_HOST_DETECT", false);
                this.mViewCountLevel = bundle.getInt("VIEW_COUNT", 0);
                this.mIsFirstDetect = false;
            }
            ViewRootImpl impl = view.getViewRootImpl();
            if (this.mNeedDetect && impl != null) {
                int detectedFlag = impl.mDetectedFlag;
                if (impl.mViewCount < this.mViewCountLevel && !(view instanceof ViewGroup)) {
                    impl.mViewCount++;
                    if (impl.mViewCount >= this.mViewCountLevel) {
                        detectedFlag |= 1;
                    }
                }
                if (detectedFlag != impl.mDetectedFlag) {
                    if (performHwMagicWindowPolicy(32, impl.mWindowAttributes.token).getBoolean("IS_RESULT_DETECT", false)) {
                        this.mNeedDetect = false;
                    }
                    impl.mDetectedFlag = detectedFlag;
                }
            }
        }
    }

    private void setScreenRectForMagicWindow(Object... params) {
        if (params != null && params.length == 3 && (params[0] instanceof View) && (params[1] instanceof Rect) && (params[2] instanceof Point)) {
            int[] windowLoc = {0, 0};
            View v = (View) params[0];
            int[] rdmViewLocOnScreen = new int[2];
            v.getLocationOnScreen(rdmViewLocOnScreen);
            int[] rdmViewLocInWindow = new int[2];
            v.getLocationInWindow(rdmViewLocInWindow);
            windowLoc[0] = rdmViewLocOnScreen[0] - rdmViewLocInWindow[0];
            windowLoc[1] = rdmViewLocOnScreen[1] - rdmViewLocInWindow[1];
            Point displaySize = (Point) params[2];
            ((Rect) params[1]).set(windowLoc[0], windowLoc[1], windowLoc[0] + displaySize.x, windowLoc[1] + displaySize.y);
        }
    }

    private void updateDisplayInfo(Object... params) {
        Configuration overrideConfig;
        if (params.length == 1 && (params[0] instanceof DisplayInfo)) {
            DisplayInfo displayInfo = (DisplayInfo) params[0];
            ActivityThread thread = ActivityThread.currentActivityThread();
            if (thread != null && (overrideConfig = thread.getOverrideConfig()) != null && !overrideConfig.equals(Configuration.EMPTY) && overrideConfig.windowConfiguration.inHwMagicWindowingMode()) {
                float density = ((float) overrideConfig.densityDpi) * 0.00625f;
                int i = (int) (((float) overrideConfig.screenWidthDp) * density);
                displayInfo.appWidth = i;
                displayInfo.logicalWidth = i;
                int i2 = (int) (((float) overrideConfig.screenHeightDp) * density);
                displayInfo.appHeight = i2;
                displayInfo.logicalHeight = i2;
                if (this.mDisplayWidth == -1 && !HwMwUtils.IS_FOLD_SCREEN_DEVICE && thread.getApplication() != null && thread.getApplication().getApplicationContext() != null) {
                    this.mDisplayWidth = ((DisplayManager) thread.getApplication().getApplicationContext().getSystemService("display")).getStableDisplaySize().y;
                }
                if (this.mDisplayWidth != overrideConfig.windowConfiguration.getBounds().width()) {
                    displayInfo.rotation = 0;
                }
            }
        }
    }

    private Bundle isDisableSensor(Object... params) {
        Configuration overrideConfig;
        Bundle result = new Bundle();
        ActivityThread thread = ActivityThread.currentActivityThread();
        if (thread != null && (overrideConfig = thread.getOverrideConfig()) != null && !overrideConfig.equals(Configuration.EMPTY) && overrideConfig.windowConfiguration.inHwMagicWindowingMode()) {
            if (!(this.mDisplayWidth == overrideConfig.windowConfiguration.getBounds().width())) {
                result.putBoolean("IS_DISABLE_SENSOR", true);
            }
        }
        return result;
    }
}
