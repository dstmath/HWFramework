package android.view;

import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.rog.AppRogInfo;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.view.IWindowManager.Stub;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.AsyncService;
import com.android.internal.util.FastPrintWriter;
import com.huawei.pgmng.log.LogPower;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public final class WindowManagerGlobal {
    public static final int ADD_APP_EXITING = -4;
    public static final int ADD_BAD_APP_TOKEN = -1;
    public static final int ADD_BAD_SUBWINDOW_TOKEN = -2;
    public static final int ADD_DUPLICATE_ADD = -5;
    public static final int ADD_FLAG_ALWAYS_CONSUME_NAV_BAR = 4;
    public static final int ADD_FLAG_APP_VISIBLE = 2;
    public static final int ADD_FLAG_IN_TOUCH_MODE = 1;
    public static final int ADD_INVALID_DISPLAY = -9;
    public static final int ADD_INVALID_TYPE = -10;
    public static final int ADD_MULTIPLE_SINGLETON = -7;
    public static final int ADD_NOT_APP_TOKEN = -3;
    public static final int ADD_OKAY = 0;
    public static final int ADD_PERMISSION_DENIED = -8;
    public static final int ADD_STARTING_NOT_NEEDED = -6;
    public static final int RELAYOUT_DEFER_SURFACE_DESTROY = 2;
    public static final int RELAYOUT_INSETS_PENDING = 1;
    public static final int RELAYOUT_RES_CONSUME_ALWAYS_NAV_BAR = 64;
    public static final int RELAYOUT_RES_DRAG_RESIZING_DOCKED = 8;
    public static final int RELAYOUT_RES_DRAG_RESIZING_FREEFORM = 16;
    public static final int RELAYOUT_RES_FIRST_TIME = 2;
    public static final int RELAYOUT_RES_IN_TOUCH_MODE = 1;
    public static final int RELAYOUT_RES_SURFACE_CHANGED = 4;
    public static final int RELAYOUT_RES_SURFACE_RESIZED = 32;
    private static final String TAG = "WindowManager";
    private static WindowManagerGlobal sDefaultWindowManager;
    private static IWindowManager sWindowManagerService;
    private static IWindowSession sWindowSession;
    private HashMap<String, AppRogInfo> mAppRogInfoSet;
    private final ArraySet<View> mDyingViews;
    private final Object mLock;
    private final ArrayList<LayoutParams> mParams;
    private boolean mRogEnable;
    private boolean mRogEnableFactor;
    private final ArrayList<ViewRootImpl> mRoots;
    private Runnable mSystemPropertyUpdater;
    private final ArrayList<View> mViews;

    private WindowManagerGlobal() {
        this.mLock = new Object();
        this.mViews = new ArrayList();
        this.mRoots = new ArrayList();
        this.mParams = new ArrayList();
        this.mDyingViews = new ArraySet();
        this.mAppRogInfoSet = new HashMap();
        this.mRogEnable = false;
        this.mRogEnableFactor = true;
    }

    public static void initialize() {
        getWindowManagerService();
    }

    public static WindowManagerGlobal getInstance() {
        WindowManagerGlobal windowManagerGlobal;
        synchronized (WindowManagerGlobal.class) {
            if (sDefaultWindowManager == null) {
                sDefaultWindowManager = new WindowManagerGlobal();
            }
            windowManagerGlobal = sDefaultWindowManager;
        }
        return windowManagerGlobal;
    }

    public static IWindowManager getWindowManagerService() {
        IWindowManager iWindowManager;
        synchronized (WindowManagerGlobal.class) {
            if (sWindowManagerService == null) {
                sWindowManagerService = Stub.asInterface(ServiceManager.getService("window"));
                try {
                    sWindowManagerService = getWindowManagerService();
                    ValueAnimator.setDurationScale(sWindowManagerService.getCurrentAnimatorScale());
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            iWindowManager = sWindowManagerService;
        }
        return iWindowManager;
    }

    public static IWindowSession getWindowSession() {
        IWindowSession iWindowSession;
        synchronized (WindowManagerGlobal.class) {
            if (sWindowSession == null) {
                try {
                    InputMethodManager imm = InputMethodManager.getInstance();
                    sWindowSession = getWindowManagerService().openSession(new IWindowSessionCallback.Stub() {
                        public void onAnimatorScaleChanged(float scale) {
                            ValueAnimator.setDurationScale(scale);
                        }
                    }, imm.getClient(), imm.getInputContext());
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            iWindowSession = sWindowSession;
        }
        return iWindowSession;
    }

    public static IWindowSession peekWindowSession() {
        IWindowSession iWindowSession;
        synchronized (WindowManagerGlobal.class) {
            iWindowSession = sWindowSession;
        }
        return iWindowSession;
    }

    public String[] getViewRootNames() {
        String[] mViewRoots;
        synchronized (this.mLock) {
            int numRoots = this.mRoots.size();
            mViewRoots = new String[numRoots];
            for (int i = ADD_OKAY; i < numRoots; i += RELAYOUT_RES_IN_TOUCH_MODE) {
                mViewRoots[i] = getWindowName((ViewRootImpl) this.mRoots.get(i));
            }
        }
        return mViewRoots;
    }

    public ArrayList<ViewRootImpl> getRootViews(IBinder token) {
        ArrayList<ViewRootImpl> views = new ArrayList();
        synchronized (this.mLock) {
            int numRoots = this.mRoots.size();
            for (int i = ADD_OKAY; i < numRoots; i += RELAYOUT_RES_IN_TOUCH_MODE) {
                LayoutParams params = (LayoutParams) this.mParams.get(i);
                if (params.token != null) {
                    if (params.token != token) {
                        boolean isChild = false;
                        if (params.type >= RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED && params.type <= LayoutParams.LAST_SUB_WINDOW) {
                            for (int j = ADD_OKAY; j < numRoots; j += RELAYOUT_RES_IN_TOUCH_MODE) {
                                LayoutParams paramsj = (LayoutParams) this.mParams.get(j);
                                if (params.token == ((View) this.mViews.get(j)).getWindowToken() && paramsj.token == token) {
                                    isChild = true;
                                    break;
                                }
                            }
                        }
                        if (!isChild) {
                        }
                    }
                    views.add((ViewRootImpl) this.mRoots.get(i));
                }
            }
        }
        return views;
    }

    public View getRootView(String name) {
        synchronized (this.mLock) {
            for (int i = this.mRoots.size() + ADD_BAD_APP_TOKEN; i >= 0; i += ADD_BAD_APP_TOKEN) {
                ViewRootImpl root = (ViewRootImpl) this.mRoots.get(i);
                if (name.equals(getWindowName(root))) {
                    View view = root.getView();
                    return view;
                }
            }
            return null;
        }
    }

    public void addView(View view, ViewGroup.LayoutParams params, Display display, Window parentWindow) {
        int index;
        if (view == null) {
            throw new IllegalArgumentException("view must not be null");
        } else if (display == null) {
            throw new IllegalArgumentException("display must not be null");
        } else if (params instanceof LayoutParams) {
            ViewRootImpl root;
            LayoutParams wparams = (LayoutParams) params;
            if (wparams.type >= LogPower.FIRST_IAWARE_TAG) {
                LogPower.push(LogPower.ADD_VIEW, String.valueOf(Binder.getCallingPid()), String.valueOf(Binder.getCallingUid()), String.valueOf(wparams.type));
            }
            if (parentWindow != null) {
                parentWindow.adjustLayoutParamsForSubWindow(wparams);
            } else {
                Context context = view.getContext();
                if (!(context == null || Process.myUid() == RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED || (context.getApplicationInfo().flags & EditorInfo.IME_FLAG_NO_ACCESSORY_ACTION) == 0)) {
                    wparams.flags |= AsyncService.CMD_ASYNC_SERVICE_DESTROY;
                }
            }
            View view2 = null;
            synchronized (this.mLock) {
                if (this.mSystemPropertyUpdater == null) {
                    this.mSystemPropertyUpdater = new Runnable() {
                        public void run() {
                            synchronized (WindowManagerGlobal.this.mLock) {
                                for (int i = WindowManagerGlobal.this.mRoots.size() + WindowManagerGlobal.ADD_BAD_APP_TOKEN; i >= 0; i += WindowManagerGlobal.ADD_BAD_APP_TOKEN) {
                                    ((ViewRootImpl) WindowManagerGlobal.this.mRoots.get(i)).loadSystemProperties();
                                }
                            }
                        }
                    };
                    SystemProperties.addChangeCallback(this.mSystemPropertyUpdater);
                }
                index = findViewLocked(view, false);
                if (index >= 0) {
                    if (this.mDyingViews.contains(view)) {
                        ((ViewRootImpl) this.mRoots.get(index)).doDie();
                    } else {
                        throw new IllegalStateException("View " + view + " has already been added to the window manager.");
                    }
                }
                if (wparams.type >= RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED && wparams.type <= LayoutParams.LAST_SUB_WINDOW) {
                    int count = this.mViews.size();
                    for (int i = ADD_OKAY; i < count; i += RELAYOUT_RES_IN_TOUCH_MODE) {
                        if (((ViewRootImpl) this.mRoots.get(i)).mWindow.asBinder() == wparams.token) {
                            view2 = (View) this.mViews.get(i);
                        }
                    }
                }
                root = new ViewRootImpl(view.getContext(), display);
                applyRogToViewRootImply(root, view.getContext());
                view.setLayoutParams(wparams);
                this.mViews.add(view);
                this.mRoots.add(root);
                this.mParams.add(wparams);
            }
            try {
                root.setView(view, wparams, view2);
            } catch (RuntimeException e) {
                synchronized (this.mLock) {
                }
                index = findViewLocked(view, false);
                if (index >= 0) {
                    removeViewLocked(index, true);
                }
                throw e;
            }
        } else {
            throw new IllegalArgumentException("Params must be WindowManager.LayoutParams");
        }
    }

    public void updateViewLayout(View view, ViewGroup.LayoutParams params) {
        if (view == null) {
            throw new IllegalArgumentException("view must not be null");
        } else if (params instanceof LayoutParams) {
            LayoutParams wparams = (LayoutParams) params;
            view.setLayoutParams(wparams);
            synchronized (this.mLock) {
                int index = findViewLocked(view, true);
                ViewRootImpl root = (ViewRootImpl) this.mRoots.get(index);
                this.mParams.remove(index);
                this.mParams.add(index, wparams);
                root.setLayoutParams(wparams, false);
            }
        } else {
            throw new IllegalArgumentException("Params must be WindowManager.LayoutParams");
        }
    }

    public void removeView(View view, boolean immediate) {
        if (view == null) {
            throw new IllegalArgumentException("view must not be null");
        }
        synchronized (this.mLock) {
            int index = findViewLocked(view, true);
            View curView = ((ViewRootImpl) this.mRoots.get(index)).getView();
            removeViewLocked(index, immediate);
            if (curView == view) {
            } else {
                throw new IllegalStateException("Calling with view " + view + " but the ViewAncestor is attached to " + curView);
            }
        }
    }

    public void closeAll(IBinder token, String who, String what) {
        closeAllExceptView(token, null, who, what);
    }

    public void closeAllExceptView(IBinder token, View view, String who, String what) {
        synchronized (this.mLock) {
            int count = this.mViews.size();
            int i = ADD_OKAY;
            while (i < count) {
                if ((view == null || this.mViews.get(i) != view) && (token == null || ((LayoutParams) this.mParams.get(i)).token == token)) {
                    ViewRootImpl root = (ViewRootImpl) this.mRoots.get(i);
                    if (who != null) {
                        WindowLeaked leak = new WindowLeaked(what + " " + who + " has leaked window " + root.getView() + " that was originally added here");
                        leak.setStackTrace(root.getLocation().getStackTrace());
                        Log.e(TAG, "", leak);
                    }
                    removeViewLocked(i, false);
                }
                i += RELAYOUT_RES_IN_TOUCH_MODE;
            }
        }
    }

    private void removeViewLocked(int index, boolean immediate) {
        if (index >= 0 && index < this.mViews.size() && index < this.mParams.size() && ((LayoutParams) this.mParams.get(index)).type >= LogPower.FIRST_IAWARE_TAG) {
            LogPower.push(LogPower.REMOVE_VIEW, String.valueOf(Binder.getCallingPid()), String.valueOf(Binder.getCallingUid()), String.valueOf(((LayoutParams) this.mParams.get(index)).type));
        }
        ViewRootImpl root = (ViewRootImpl) this.mRoots.get(index);
        View view = root.getView();
        if (view != null) {
            InputMethodManager imm = InputMethodManager.getInstance();
            if (imm != null) {
                imm.windowDismissed(((View) this.mViews.get(index)).getWindowToken());
            }
        }
        boolean deferred = root.die(immediate);
        if (view != null) {
            view.assignParent(null);
            if (deferred) {
                this.mDyingViews.add(view);
            }
        }
    }

    void doRemoveView(ViewRootImpl root) {
        synchronized (this.mLock) {
            int index = this.mRoots.indexOf(root);
            if (index >= 0) {
                this.mRoots.remove(index);
                this.mParams.remove(index);
                this.mDyingViews.remove((View) this.mViews.remove(index));
            }
        }
        if (ThreadedRenderer.sTrimForeground && ThreadedRenderer.isAvailable()) {
            doTrimForeground();
        }
    }

    private int findViewLocked(View view, boolean required) {
        int index = this.mViews.indexOf(view);
        if (!required || index >= 0) {
            return index;
        }
        throw new IllegalArgumentException("View=" + view + " not attached to window manager");
    }

    public static boolean shouldDestroyEglContext(int trimLevel) {
        if (trimLevel >= 80) {
            return true;
        }
        if (trimLevel < 60 || ActivityManager.isHighEndGfx()) {
            return false;
        }
        return true;
    }

    public void trimMemory(int level) {
        if (ThreadedRenderer.isAvailable()) {
            if (shouldDestroyEglContext(level)) {
                synchronized (this.mLock) {
                    for (int i = this.mRoots.size() + ADD_BAD_APP_TOKEN; i >= 0; i += ADD_BAD_APP_TOKEN) {
                        ((ViewRootImpl) this.mRoots.get(i)).destroyHardwareResources();
                    }
                }
                level = 80;
            }
            ThreadedRenderer.trimMemory(level);
            if (ThreadedRenderer.sTrimForeground) {
                doTrimForeground();
            }
        }
    }

    public static void trimForeground() {
        if (ThreadedRenderer.sTrimForeground && ThreadedRenderer.isAvailable()) {
            getInstance().doTrimForeground();
        }
    }

    private void doTrimForeground() {
        boolean hasVisibleWindows = false;
        synchronized (this.mLock) {
            for (int i = this.mRoots.size() + ADD_BAD_APP_TOKEN; i >= 0; i += ADD_BAD_APP_TOKEN) {
                ViewRootImpl root = (ViewRootImpl) this.mRoots.get(i);
                if (root.mView == null || root.getHostVisibility() != 0 || root.mAttachInfo.mHardwareRenderer == null) {
                    root.destroyHardwareResources();
                } else {
                    hasVisibleWindows = true;
                }
            }
        }
        if (!hasVisibleWindows) {
            ThreadedRenderer.trimMemory(80);
        }
    }

    public void dumpGfxInfo(FileDescriptor fd, String[] args) {
        PrintWriter pw = new FastPrintWriter(new FileOutputStream(fd));
        try {
            synchronized (this.mLock) {
                int i;
                ViewRootImpl root;
                Object[] objArr;
                int count = this.mViews.size();
                pw.println("Profile data in ms:");
                for (i = ADD_OKAY; i < count; i += RELAYOUT_RES_IN_TOUCH_MODE) {
                    root = (ViewRootImpl) this.mRoots.get(i);
                    objArr = new Object[RELAYOUT_RES_FIRST_TIME];
                    objArr[ADD_OKAY] = getWindowName(root);
                    objArr[RELAYOUT_RES_IN_TOUCH_MODE] = Integer.valueOf(root.getHostVisibility());
                    pw.printf("\n\t%s (visibility=%d)", objArr);
                    ThreadedRenderer renderer = null;
                    if (root.getView().mAttachInfo != null) {
                        renderer = root.getView().mAttachInfo.mHardwareRenderer;
                    }
                    if (renderer != null) {
                        renderer.dumpGfxInfo(pw, fd, args);
                    }
                }
                pw.println("\nView hierarchy:\n");
                int viewsCount = ADD_OKAY;
                int displayListsSize = ADD_OKAY;
                int[] info = new int[RELAYOUT_RES_FIRST_TIME];
                for (i = ADD_OKAY; i < count; i += RELAYOUT_RES_IN_TOUCH_MODE) {
                    ((ViewRootImpl) this.mRoots.get(i)).dumpGfxInfo(info);
                    pw.printf("  %s\n  %d views, %.2f kB of display lists", new Object[]{getWindowName(root), Integer.valueOf(info[ADD_OKAY]), Float.valueOf(((float) info[RELAYOUT_RES_IN_TOUCH_MODE]) / 1024.0f)});
                    pw.printf("\n\n", new Object[ADD_OKAY]);
                    viewsCount += info[ADD_OKAY];
                    displayListsSize += info[RELAYOUT_RES_IN_TOUCH_MODE];
                }
                objArr = new Object[RELAYOUT_RES_IN_TOUCH_MODE];
                objArr[ADD_OKAY] = Integer.valueOf(count);
                pw.printf("\nTotal ViewRootImpl: %d\n", objArr);
                objArr = new Object[RELAYOUT_RES_IN_TOUCH_MODE];
                objArr[ADD_OKAY] = Integer.valueOf(viewsCount);
                pw.printf("Total Views:        %d\n", objArr);
                objArr = new Object[RELAYOUT_RES_IN_TOUCH_MODE];
                objArr[ADD_OKAY] = Float.valueOf(((float) displayListsSize) / 1024.0f);
                pw.printf("Total DisplayList:  %.2f kB\n\n", objArr);
            }
        } finally {
            pw.flush();
        }
    }

    private static String getWindowName(ViewRootImpl root) {
        return root.mWindowAttributes.getTitle() + "/" + root.getClass().getName() + '@' + Integer.toHexString(root.hashCode());
    }

    public void setStoppedState(IBinder token, boolean stopped) {
        synchronized (this.mLock) {
            int count = this.mViews.size();
            int i = ADD_OKAY;
            while (i < count) {
                if (token == null || ((LayoutParams) this.mParams.get(i)).token == token) {
                    ((ViewRootImpl) this.mRoots.get(i)).setWindowStopped(stopped);
                }
                i += RELAYOUT_RES_IN_TOUCH_MODE;
            }
        }
    }

    public void reportNewConfiguration(Configuration config) {
        Throwable th;
        synchronized (this.mLock) {
            try {
                int count = this.mViews.size();
                Configuration config2 = new Configuration(config);
                int i = ADD_OKAY;
                while (i < count) {
                    try {
                        ((ViewRootImpl) this.mRoots.get(i)).requestUpdateConfiguration(config2);
                        i += RELAYOUT_RES_IN_TOUCH_MODE;
                    } catch (Throwable th2) {
                        th = th2;
                        config = config2;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    public void changeCanvasOpacity(IBinder token, boolean opaque) {
        if (token != null) {
            synchronized (this.mLock) {
                for (int i = this.mParams.size() + ADD_BAD_APP_TOKEN; i >= 0; i += ADD_BAD_APP_TOKEN) {
                    if (((LayoutParams) this.mParams.get(i)).token == token) {
                        ((ViewRootImpl) this.mRoots.get(i)).changeCanvasOpacity(opaque);
                        return;
                    }
                }
            }
        }
    }

    private void applyRogToViewRootImply(ViewRootImpl root, Context context) {
        root.setRogInfo(getAppRogInfo(context.getPackageName()), getRogSwitchState());
    }

    public AppRogInfo getAppRogInfo(String packageName) {
        AppRogInfo info = (AppRogInfo) this.mAppRogInfoSet.get(packageName);
        if (info == null) {
            return null;
        }
        return new AppRogInfo(info);
    }

    public void addAppRogInfo(String packageName, AppRogInfo rogInfo) {
        if (!TextUtils.isEmpty(packageName)) {
            this.mAppRogInfoSet.put(packageName, rogInfo);
        }
    }

    public boolean isRogInfoAlreadyExist(String packageName) {
        return this.mAppRogInfoSet.containsKey(packageName);
    }

    public boolean getRogSwitchState() {
        return this.mRogEnableFactor ? this.mRogEnable : false;
    }

    public void setRogSwitchState(boolean rogEnable) {
        this.mRogEnable = rogEnable;
    }

    public int translateIntegerInAppToScreen(String pacakgeName, int value) {
        AppRogInfo rogInfo = getAppRogInfo(pacakgeName);
        return (!getRogSwitchState() || rogInfo == null) ? value : (int) ((((float) value) * rogInfo.mRogScale) + 0.5f);
    }

    public int translateIntegerInScreenToApp(String pacakgeName, int value) {
        AppRogInfo rogInfo = getAppRogInfo(pacakgeName);
        return (!getRogSwitchState() || rogInfo == null) ? value : (int) ((((float) value) / rogInfo.mRogScale) + 0.5f);
    }

    public void dispatchRogInfoUpdated(String packageName, AppRogInfo rogInfo) {
        boolean rogEnable = getRogSwitchState();
        synchronized (this.mLock) {
            for (ViewRootImpl root : this.mRoots) {
                if (packageName.equalsIgnoreCase(root.mBasePackageName)) {
                    root.setRogInfo(rogInfo, rogEnable);
                    root.dispatchRogSwitchStateChange(rogEnable);
                }
            }
        }
    }

    public void setRogEnableFactor(boolean factor) {
        this.mRogEnableFactor = factor;
    }

    public boolean getRogEnableFactor() {
        return this.mRogEnableFactor;
    }
}
