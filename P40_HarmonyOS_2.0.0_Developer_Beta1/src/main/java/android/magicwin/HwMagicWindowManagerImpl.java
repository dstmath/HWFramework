package android.magicwin;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.magicwin.IHwMagicWindow;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.HwMwUtils;
import android.view.View;
import android.view.ViewGroup;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.android.content.res.ConfigurationAdapter;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.util.SlogEx;
import com.huawei.android.view.ViewRootImplEx;
import com.huawei.utils.HwPartResourceUtils;
import java.util.ArrayList;
import java.util.Iterator;

public class HwMagicWindowManagerImpl extends DefaultHwMagicWindowManager {
    private static final int HOSTVIEW_PARAM_SIZE = 1;
    private static final int MAX_OFFSET_HEIGHT = 10;
    private static final int SETSCREENRECT_PARAM_SIZE = 3;
    private static final String TAG = "HWMW_HwMagicWindowManagerImpl";
    private static HwMagicWindow sInstance = null;
    private Bundle mEmptyBundle = new Bundle();
    private boolean mIsFirstDetect = true;
    private boolean mNeedDetect = false;
    private IHwMagicWindow mService = null;
    private int mViewCountLevel = 0;

    private HwMagicWindowManagerImpl() {
    }

    public static synchronized HwMagicWindow getInstance() {
        HwMagicWindow hwMagicWindow;
        synchronized (HwMagicWindowManagerImpl.class) {
            if (sInstance == null) {
                sInstance = new HwMagicWindowManagerImpl();
            }
            hwMagicWindow = sInstance;
        }
        return hwMagicWindow;
    }

    public IHwMagicWindow getService() {
        checkService();
        return this.mService;
    }

    private boolean checkService() {
        if (this.mService != null) {
            return true;
        }
        this.mService = IHwMagicWindow.Stub.asInterface(ServiceManagerEx.getService("HwMagicWindowService"));
        if (this.mService != null) {
            return true;
        }
        SlogEx.e(TAG, "check service failed");
        return false;
    }

    private boolean checkParams(Object... params) {
        return (params == null || params.length == 0) ? false : true;
    }

    public Bundle performHwMagicWindowPolicy(int policy, Object... params) {
        if (policy == 11) {
            detectFeatureForMainWindow(params);
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
                SlogEx.e(TAG, "service has not been started");
                return this.mEmptyBundle;
            } else {
                ArrayList<Object> list = new ArrayList<>(params.length);
                for (Object param : params) {
                    list.add(param);
                }
                try {
                    return this.mService.performHwMagicWindowPolicy(policy, list);
                } catch (RemoteException e) {
                    SlogEx.e(TAG, "Remote Exception e : " + e);
                    return new Bundle();
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
            int windowMode = ConfigurationAdapter.getWindowingMode(context.getResources().getConfiguration());
            if (windowMode == 103 && ((ArrayList) params[1]).size() == 0) {
                ViewGroup decorView = (ViewGroup) ((Activity) context).getWindow().getDecorView();
                int childCount = decorView.getChildCount();
                int maxHeight = context.getResources().getDimensionPixelOffset(HwPartResourceUtils.getResourceId("status_bar_height")) + MAX_OFFSET_HEIGHT;
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

    private void collectCoverView(View view, ArrayList<View> viewList, int maxHeight) {
        if (!(view instanceof ViewGroup)) {
            int width = view.getLayoutParams().width;
            int height = view.getLayoutParams().height;
            if ((width == -1 && height > 0 && height <= maxHeight) && !view.isFocusable()) {
                viewList.add(view);
            }
        }
    }

    private void detectFeatureForMainWindow(Object... params) {
        View view;
        if (params.length == 1 && (params[0] instanceof View) && (view = (View) params[0]) != null) {
            String currentPkgName = ActivityThreadEx.currentPackageName();
            if (HwMwUtils.ENABLED && this.mIsFirstDetect && currentPkgName != null) {
                Bundle bundle = performHwMagicWindowPolicy(31, currentPkgName);
                this.mNeedDetect = bundle.getBoolean("NEED_HOST_DETECT", false);
                this.mViewCountLevel = bundle.getInt("VIEW_COUNT", 0);
                this.mIsFirstDetect = false;
            }
            if (this.mNeedDetect && !ViewRootImplEx.isViewRootImplNull(view)) {
                int detectedFlag = ViewRootImplEx.getDetectedFlag(view);
                if (ViewRootImplEx.getViewCount(view) < this.mViewCountLevel && !(view instanceof ViewGroup)) {
                    ViewRootImplEx.increaseViewCount(view);
                    if (ViewRootImplEx.getViewCount(view) >= this.mViewCountLevel) {
                        detectedFlag |= 1;
                    }
                }
                if (detectedFlag != ViewRootImplEx.getDetectedFlag(view)) {
                    if (performHwMagicWindowPolicy(32, ViewRootImplEx.getWindowAttributes(view).token).getBoolean("IS_RESULT_DETECT", false)) {
                        this.mNeedDetect = false;
                    }
                    ViewRootImplEx.setDetectedFlag(view, detectedFlag);
                }
            }
        }
    }

    private void setScreenRectForMagicWindow(Object... params) {
        if (params != null && params.length == SETSCREENRECT_PARAM_SIZE && (params[0] instanceof View) && (params[1] instanceof Rect) && (params[2] instanceof Point)) {
            int[] windowLoc = {0, 0};
            View view = (View) params[0];
            int[] rdmViewLocOnScreen = new int[2];
            view.getLocationOnScreen(rdmViewLocOnScreen);
            int[] rdmViewLocInWindow = new int[2];
            view.getLocationInWindow(rdmViewLocInWindow);
            windowLoc[0] = rdmViewLocOnScreen[0] - rdmViewLocInWindow[0];
            windowLoc[1] = rdmViewLocOnScreen[1] - rdmViewLocInWindow[1];
            Point displaySize = (Point) params[2];
            ((Rect) params[1]).set(windowLoc[0], windowLoc[1], windowLoc[0] + displaySize.x, windowLoc[1] + displaySize.y);
        }
    }

    private Bundle isDisableSensor(Object... params) {
        Bundle result = new Bundle();
        ActivityThreadEx thread = ActivityThreadEx.currentActivityThread();
        if (!(thread == null || thread.getApplication() == null || thread.getApplication().getResources() == null)) {
            Configuration config = thread.getApplication().getResources().getConfiguration();
            if (ConfigurationAdapter.inHwMagicWindowingMode(config)) {
                if (!(config.orientation == 2)) {
                    result.putBoolean("IS_DISABLE_SENSOR", true);
                }
            }
        }
        return result;
    }
}
