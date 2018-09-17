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
import android.rms.AppAssociate;
import android.util.ArraySet;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.LogException;
import android.view.IWindowManager.Stub;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import com.android.internal.util.FastPrintWriter;
import com.huawei.pgmng.log.LogPower;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

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
    private final ArraySet<View> mDyingViews = new ArraySet();
    private final Object mLock = new Object();
    private final ArrayList<LayoutParams> mParams = new ArrayList();
    private final ArrayList<ViewRootImpl> mRoots = new ArrayList();
    private Runnable mSystemPropertyUpdater;
    private final ArrayList<View> mViews = new ArrayList();

    private WindowManagerGlobal() {
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
                sWindowManagerService = Stub.asInterface(ServiceManager.getService(AppAssociate.ASSOC_WINDOW));
                try {
                    if (sWindowManagerService != null) {
                        ValueAnimator.setDurationScale(sWindowManagerService.getCurrentAnimatorScale());
                    }
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
            for (int i = 0; i < numRoots; i++) {
                mViewRoots[i] = getWindowName((ViewRootImpl) this.mRoots.get(i));
            }
        }
        return mViewRoots;
    }

    public ArrayList<ViewRootImpl> getRootViews(IBinder token) {
        ArrayList<ViewRootImpl> views = new ArrayList();
        synchronized (this.mLock) {
            int numRoots = this.mRoots.size();
            for (int i = 0; i < numRoots; i++) {
                LayoutParams params = (LayoutParams) this.mParams.get(i);
                if (params.token != null) {
                    if (params.token != token) {
                        boolean isChild = false;
                        if (params.type >= 1000 && params.type <= LayoutParams.LAST_SUB_WINDOW) {
                            for (int j = 0; j < numRoots; j++) {
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

    public View getWindowView(IBinder windowToken) {
        synchronized (this.mLock) {
            int numViews = this.mViews.size();
            for (int i = 0; i < numViews; i++) {
                View view = (View) this.mViews.get(i);
                if (view.getWindowToken() == windowToken) {
                    return view;
                }
            }
            return null;
        }
    }

    public View getRootView(String name) {
        synchronized (this.mLock) {
            for (int i = this.mRoots.size() - 1; i >= 0; i--) {
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
        if (view == null) {
            throw new IllegalArgumentException("view must not be null");
        } else if (display == null) {
            throw new IllegalArgumentException("display must not be null");
        } else if (params instanceof LayoutParams) {
            LayoutParams wparams = (LayoutParams) params;
            if (wparams.type >= 2000) {
                LogPower.push(151, String.valueOf(Binder.getCallingPid()), String.valueOf(Binder.getCallingUid()), String.valueOf(wparams.type));
            }
            if (parentWindow != null) {
                parentWindow.adjustLayoutParamsForSubWindow(wparams);
            } else {
                Context context = view.getContext();
                if (!(context == null || Process.myUid() == 1000 || (context.getApplicationInfo().flags & 536870912) == 0)) {
                    wparams.flags |= 16777216;
                }
            }
            View view2 = null;
            synchronized (this.mLock) {
                if (this.mSystemPropertyUpdater == null) {
                    this.mSystemPropertyUpdater = new Runnable() {
                        public void run() {
                            synchronized (WindowManagerGlobal.this.mLock) {
                                for (int i = WindowManagerGlobal.this.mRoots.size() - 1; i >= 0; i--) {
                                    ((ViewRootImpl) WindowManagerGlobal.this.mRoots.get(i)).loadSystemProperties();
                                }
                            }
                        }
                    };
                    SystemProperties.addChangeCallback(this.mSystemPropertyUpdater);
                }
                int index = findViewLocked(view, false);
                if (index >= 0) {
                    if (this.mDyingViews.contains(view)) {
                        ((ViewRootImpl) this.mRoots.get(index)).doDie();
                    } else {
                        throw new IllegalStateException("View " + view + " has already been added to the window manager.");
                    }
                }
                if (wparams.type >= 1000 && wparams.type <= LayoutParams.LAST_SUB_WINDOW) {
                    int count = this.mViews.size();
                    for (int i = 0; i < count; i++) {
                        if (((ViewRootImpl) this.mRoots.get(i)).mWindow.asBinder() == wparams.token) {
                            view2 = (View) this.mViews.get(i);
                        }
                    }
                }
                ViewRootImpl root = new ViewRootImpl(view.getContext(), display);
                view.-wrap18(wparams);
                this.mViews.add(view);
                this.mRoots.add(root);
                this.mParams.add(wparams);
                try {
                    root.setView(view, wparams, view2);
                } catch (RuntimeException e) {
                    if (index >= 0) {
                        removeViewLocked(index, true);
                    }
                    throw e;
                }
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
            view.-wrap18(wparams);
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
            try {
                int index = findViewLocked(view, true);
                View curView = ((ViewRootImpl) this.mRoots.get(index)).getView();
                removeViewLocked(index, immediate);
                if (curView == view) {
                } else {
                    throw new IllegalStateException("Calling with view " + view + " but the ViewAncestor is attached to " + curView);
                }
            } catch (IllegalArgumentException e) {
                Context context = view.getContext();
                if (context == null || !HwPCUtils.isValidExtDisplayId(context) || (HwPCUtils.isPcCastMode() ^ 1) == 0) {
                    throw e;
                }
            }
        }
    }

    public void closeAll(IBinder token, String who, String what) {
        closeAllExceptView(token, null, who, what);
    }

    public void closeAllExceptView(IBinder token, View view, String who, String what) {
        synchronized (this.mLock) {
            int count = this.mViews.size();
            int i = 0;
            while (i < count) {
                if ((view == null || this.mViews.get(i) != view) && (token == null || ((LayoutParams) this.mParams.get(i)).token == token)) {
                    ViewRootImpl root = (ViewRootImpl) this.mRoots.get(i);
                    if (who != null) {
                        WindowLeaked leak = new WindowLeaked(what + " " + who + " has leaked window " + root.getView() + " that was originally added here");
                        leak.setStackTrace(root.getLocation().getStackTrace());
                        Log.e(TAG, LogException.NO_VALUE, leak);
                    }
                    removeViewLocked(i, false);
                }
                i++;
            }
        }
    }

    private void removeViewLocked(int index, boolean immediate) {
        if (index >= 0 && index < this.mViews.size() && index < this.mParams.size() && ((LayoutParams) this.mParams.get(index)).type >= 2000) {
            LogPower.push(152, String.valueOf(Binder.getCallingPid()), String.valueOf(Binder.getCallingUid()), String.valueOf(((LayoutParams) this.mParams.get(index)).type));
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
        if (trimLevel < 60 || (ActivityManager.isHighEndGfx() ^ 1) == 0) {
            return false;
        }
        return true;
    }

    public void trimMemory(int level) {
        if (ThreadedRenderer.isAvailable()) {
            if (shouldDestroyEglContext(level)) {
                synchronized (this.mLock) {
                    for (int i = this.mRoots.size() - 1; i >= 0; i--) {
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
            for (int i = this.mRoots.size() - 1; i >= 0; i--) {
                ViewRootImpl root = (ViewRootImpl) this.mRoots.get(i);
                if (root.mView == null || root.getHostVisibility() != 0 || root.mAttachInfo.mThreadedRenderer == null) {
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
                int count = this.mViews.size();
                pw.println("Profile data in ms:");
                for (i = 0; i < count; i++) {
                    root = (ViewRootImpl) this.mRoots.get(i);
                    pw.printf("\n\t%s (visibility=%d)", new Object[]{getWindowName(root), Integer.valueOf(root.getHostVisibility())});
                    ThreadedRenderer renderer = null;
                    if (root.getView().mAttachInfo != null) {
                        renderer = root.getView().mAttachInfo.mThreadedRenderer;
                    }
                    if (renderer != null) {
                        renderer.dumpGfxInfo(pw, fd, args);
                    }
                }
                pw.println("\nView hierarchy:\n");
                int viewsCount = 0;
                int displayListsSize = 0;
                int[] info = new int[2];
                for (i = 0; i < count; i++) {
                    ((ViewRootImpl) this.mRoots.get(i)).dumpGfxInfo(info);
                    pw.printf("  %s\n  %d views, %.2f kB of display lists", new Object[]{getWindowName(root), Integer.valueOf(info[0]), Float.valueOf(((float) info[1]) / 1024.0f)});
                    pw.printf("\n\n", new Object[0]);
                    viewsCount += info[0];
                    displayListsSize += info[1];
                }
                pw.printf("\nTotal ViewRootImpl: %d\n", new Object[]{Integer.valueOf(count)});
                pw.printf("Total Views:        %d\n", new Object[]{Integer.valueOf(viewsCount)});
                pw.printf("Total DisplayList:  %.2f kB\n\n", new Object[]{Float.valueOf(((float) displayListsSize) / 1024.0f)});
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
            int i = 0;
            while (i < count) {
                if (token == null || ((LayoutParams) this.mParams.get(i)).token == token) {
                    ((ViewRootImpl) this.mRoots.get(i)).setWindowStopped(stopped);
                }
                i++;
            }
        }
    }

    public void reportNewConfiguration(Configuration config) {
        Throwable th;
        synchronized (this.mLock) {
            try {
                int count = this.mViews.size();
                Configuration config2 = new Configuration(config);
                int i = 0;
                while (i < count) {
                    try {
                        ((ViewRootImpl) this.mRoots.get(i)).requestUpdateConfiguration(config2);
                        i++;
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
                for (int i = this.mParams.size() - 1; i >= 0; i--) {
                    if (((LayoutParams) this.mParams.get(i)).token == token) {
                        ((ViewRootImpl) this.mRoots.get(i)).changeCanvasOpacity(opaque);
                        return;
                    }
                }
            }
        }
    }
}
