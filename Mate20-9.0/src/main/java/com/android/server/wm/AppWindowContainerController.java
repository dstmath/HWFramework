package com.android.server.wm;

import android.app.ActivityManager;
import android.content.pm.ActivityInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.IApplicationToken;
import android.view.RemoteAnimationDefinition;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.AttributeCache;
import com.android.server.HwServiceFactory;
import com.android.server.pm.DumpState;
import com.android.server.policy.WindowManagerPolicy;
import java.util.HashSet;
import java.util.Set;

public class AppWindowContainerController extends WindowContainerController<AppWindowToken, AppWindowContainerListener> {
    private static final int STARTING_WINDOW_TYPE_NONE = 0;
    private static final int STARTING_WINDOW_TYPE_SNAPSHOT = 1;
    private static final int STARTING_WINDOW_TYPE_SPLASH_SCREEN = 2;
    private static Set<String> sDisableSnapShots = new HashSet();
    private static Set<String> sSkipStartingWindowActivitys = new HashSet();
    private final Runnable mAddStartingWindow;
    private final Handler mHandler;
    private final Runnable mOnWindowsGone;
    private final Runnable mOnWindowsVisible;
    private Task mTask;
    /* access modifiers changed from: private */
    public final IApplicationToken mToken;

    private final class H extends Handler {
        public static final int NOTIFY_STARTING_WINDOW_DRAWN = 2;
        public static final int NOTIFY_WINDOWS_DRAWN = 1;

        public H(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (AppWindowContainerController.this.mListener != null) {
                        if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                            Slog.v("WindowManager", "Reporting drawn in " + AppWindowContainerController.this.mToken);
                        }
                        ((AppWindowContainerListener) AppWindowContainerController.this.mListener).onWindowsDrawn(msg.getWhen());
                        break;
                    } else {
                        return;
                    }
                case 2:
                    if (AppWindowContainerController.this.mListener != null) {
                        if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                            Slog.v("WindowManager", "Reporting drawn in " + AppWindowContainerController.this.mToken);
                        }
                        ((AppWindowContainerListener) AppWindowContainerController.this.mListener).onStartingWindowDrawn(msg.getWhen());
                        break;
                    } else {
                        return;
                    }
            }
        }
    }

    public /* bridge */ /* synthetic */ void onOverrideConfigurationChanged(Configuration configuration) {
        super.onOverrideConfigurationChanged(configuration);
    }

    static {
        sSkipStartingWindowActivitys.add("com.tencent.mm/.plugin.voip.ui.VideoActivity");
        sSkipStartingWindowActivitys.add("com.tencent.mobileqq/com.tencent.av.ui.VideoInviteFull");
        sSkipStartingWindowActivitys.add("com.tencent.tim/com.tencent.av.ui.VideoInviteFull");
        sSkipStartingWindowActivitys.add("com.tencent.mobileqq/com.tencent.av.ui.VChatActivity");
        sSkipStartingWindowActivitys.add("com.huawei.android.launcher/.powersavemode.PowerSaveModeLauncher");
        sSkipStartingWindowActivitys.add("com.android.deskclock/.timer.TimerAlertActivity");
        sDisableSnapShots.add("com.android.contacts");
        sDisableSnapShots.add("com.huawei.camera");
        sDisableSnapShots.add("com.android.incallui");
        sDisableSnapShots.add("com.huawei.systemmanager");
        sDisableSnapShots.add("com.baidu.searchbox");
    }

    public static /* synthetic */ void lambda$new$0(AppWindowContainerController appWindowContainerController) {
        if (appWindowContainerController.mListener != null) {
            ((AppWindowContainerListener) appWindowContainerController.mListener).onWindowsVisible();
        }
    }

    public static /* synthetic */ void lambda$new$1(AppWindowContainerController appWindowContainerController) {
        if (appWindowContainerController.mListener != null) {
            ((AppWindowContainerListener) appWindowContainerController.mListener).onWindowsGone();
        }
    }

    public AppWindowContainerController(TaskWindowContainerController taskController, IApplicationToken token, AppWindowContainerListener listener, int index, int requestedOrientation, boolean fullscreen, boolean showForAllUsers, int configChanges, boolean voiceInteraction, boolean launchTaskBehind, boolean alwaysFocusable, int targetSdkVersion, int rotationAnimationHint, long inputDispatchingTimeoutNanos, boolean naviBarHide, ActivityInfo info) {
        this(taskController, token, listener, index, requestedOrientation, fullscreen, showForAllUsers, configChanges, voiceInteraction, launchTaskBehind, alwaysFocusable, targetSdkVersion, rotationAnimationHint, inputDispatchingTimeoutNanos, WindowManagerService.getInstance(), naviBarHide, info);
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    public AppWindowContainerController(TaskWindowContainerController taskController, IApplicationToken token, AppWindowContainerListener listener, int index, int requestedOrientation, boolean fullscreen, boolean showForAllUsers, int configChanges, boolean voiceInteraction, boolean launchTaskBehind, boolean alwaysFocusable, int targetSdkVersion, int rotationAnimationHint, long inputDispatchingTimeoutNanos, WindowManagerService service, boolean naviBarHide, ActivityInfo info) {
        super(listener, r12);
        WindowHashMap windowHashMap;
        AppWindowToken atoken;
        StringBuilder sb;
        int i;
        TaskWindowContainerController taskWindowContainerController = taskController;
        int i2 = index;
        WindowManagerService windowManagerService = service;
        this.mTask = null;
        this.mOnWindowsVisible = new Runnable() {
            public final void run() {
                AppWindowContainerController.lambda$new$0(AppWindowContainerController.this);
            }
        };
        this.mOnWindowsGone = new Runnable() {
            public final void run() {
                AppWindowContainerController.lambda$new$1(AppWindowContainerController.this);
            }
        };
        this.mAddStartingWindow = new Runnable() {
            /* JADX WARNING: Code restructure failed: missing block: B:10:0x001e, code lost:
                return;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:14:0x0037, code lost:
                com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
             */
            /* JADX WARNING: Code restructure failed: missing block: B:15:0x003a, code lost:
                if (r1 != null) goto L_0x005c;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:17:0x003e, code lost:
                if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_STARTING_WINDOW == false) goto L_0x005b;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:18:0x0040, code lost:
                android.util.Slog.v("WindowManager", "startingData was nulled out before handling mAddStartingWindow: " + r9.this$0.mContainer);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:19:0x005b, code lost:
                return;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:21:0x005e, code lost:
                if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_STARTING_WINDOW == false) goto L_0x0082;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:22:0x0060, code lost:
                android.util.Slog.v("WindowManager", "Add starting " + r9.this$0 + ": startingData=" + r2.startingData);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:23:0x0082, code lost:
                r3 = null;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:26:0x0088, code lost:
                r3 = r1.createStartingSurface(r2);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:27:0x008a, code lost:
                r4 = move-exception;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:28:0x008b, code lost:
                android.util.Slog.w("WindowManager", "Exception when adding starting window", r4);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:9:0x001b, code lost:
                com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
             */
            public void run() {
                AppWindowToken container;
                WindowManagerPolicy.StartingSurface surface;
                synchronized (AppWindowContainerController.this.mWindowMap) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        if (AppWindowContainerController.this.mContainer != null) {
                            AppWindowContainerController.this.mService.mAnimationHandler.removeCallbacks(this);
                            StartingData startingData = ((AppWindowToken) AppWindowContainerController.this.mContainer).startingData;
                            container = (AppWindowToken) AppWindowContainerController.this.mContainer;
                        } else if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                            Slog.v("WindowManager", "mContainer was null while trying to add starting window");
                        }
                    } finally {
                        while (true) {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                }
                if (surface != null) {
                    boolean abort = false;
                    synchronized (AppWindowContainerController.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (!container.removed) {
                                if (container.startingData != null) {
                                    container.startingSurface = surface;
                                    if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW && !abort) {
                                        Slog.v("WindowManager", "Added starting " + AppWindowContainerController.this.mContainer + ": startingWindow=" + container.startingWindow + " startingView=" + container.startingSurface);
                                    }
                                }
                            }
                            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                                Slog.v("WindowManager", "Aborted starting " + container + ": removed=" + container.removed + " startingData=" + container.startingData);
                            }
                            container.startingWindow = null;
                            container.startingData = null;
                            abort = true;
                            Slog.v("WindowManager", "Added starting " + AppWindowContainerController.this.mContainer + ": startingWindow=" + container.startingWindow + " startingView=" + container.startingSurface);
                        } catch (Throwable th) {
                            while (true) {
                                throw th;
                            }
                        }
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    if (abort) {
                        surface.remove();
                    }
                } else if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                    Slog.v("WindowManager", "Surface returned was null: " + AppWindowContainerController.this.mContainer);
                }
            }
        };
        this.mHandler = new H(windowManagerService.mH.getLooper());
        IApplicationToken iApplicationToken = token;
        this.mToken = iApplicationToken;
        WindowHashMap windowHashMap2 = this.mWindowMap;
        synchronized (windowHashMap2) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                AppWindowToken atoken2 = this.mRoot.getAppWindowToken(this.mToken.asBinder());
                if (atoken2 != null) {
                    Slog.w("WindowManager", "Attempted to add existing app token: " + this.mToken);
                } else {
                    Task task = (Task) taskWindowContainerController.mContainer;
                    if (task != null) {
                        AppWindowToken appWindowToken = atoken2;
                        Task task2 = task;
                        windowHashMap = windowHashMap2;
                        try {
                            atoken = createAppWindow(this.mService, iApplicationToken, voiceInteraction, task.getDisplayContent(), inputDispatchingTimeoutNanos, fullscreen, showForAllUsers, targetSdkVersion, requestedOrientation, rotationAnimationHint, configChanges, launchTaskBehind, alwaysFocusable, this, naviBarHide, info);
                            sb = new StringBuilder();
                            sb.append("addAppToken: ");
                            sb.append(atoken);
                            sb.append(" controller=");
                            try {
                                sb.append(taskController);
                                sb.append(" at ");
                                i = index;
                            } catch (Throwable th) {
                                th = th;
                                int i3 = index;
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            TaskWindowContainerController taskWindowContainerController2 = taskController;
                            int i32 = index;
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                        try {
                            sb.append(i);
                            Slog.v("WindowManager", sb.toString());
                            task2.addChild(atoken, i);
                            this.mTask = task2;
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return;
                        } catch (Throwable th3) {
                            th = th3;
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    } else {
                        AppWindowToken appWindowToken2 = atoken2;
                        Task task3 = task;
                        windowHashMap = windowHashMap2;
                        int i4 = i2;
                        TaskWindowContainerController taskWindowContainerController3 = taskWindowContainerController;
                        throw new IllegalArgumentException("AppWindowContainerController: invalid  controller=" + taskWindowContainerController3);
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public AppWindowToken createAppWindow(WindowManagerService service, IApplicationToken token, boolean voiceInteraction, DisplayContent dc, long inputDispatchingTimeoutNanos, boolean fullscreen, boolean showForAllUsers, int targetSdk, int orientation, int rotationAnimationHint, int configChanges, boolean launchTaskBehind, boolean alwaysFocusable, AppWindowContainerController controller, boolean naviBarHide, ActivityInfo info) {
        AppWindowToken appWindowToken = new AppWindowToken(service, token, voiceInteraction, dc, inputDispatchingTimeoutNanos, fullscreen, showForAllUsers, targetSdk, orientation, rotationAnimationHint, configChanges, launchTaskBehind, alwaysFocusable, controller, naviBarHide, info);
        return appWindowToken;
    }

    public void removeContainer(int displayId) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                DisplayContent dc = this.mRoot.getDisplayContent(displayId);
                if (dc == null) {
                    Slog.w("WindowManager", "removeAppToken: Attempted to remove binder token: " + this.mToken + " from non-existing displayId=" + displayId);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                dc.removeAppToken(this.mToken.asBinder());
                super.removeContainer();
                WindowManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void removeContainer() {
        throw new UnsupportedOperationException("Use removeContainer(displayId) instead.");
    }

    public void reparent(TaskWindowContainerController taskController, int position) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                Slog.i("WindowManager", "reparent: moving app token=" + this.mToken + " to task=" + taskController + " at " + position);
                if (this.mContainer == null) {
                    Slog.i("WindowManager", "reparent: could not find app token=" + this.mToken);
                    return;
                }
                Task task = (Task) taskController.mContainer;
                if (task != null) {
                    ((AppWindowToken) this.mContainer).reparent(task, position);
                    ((AppWindowToken) this.mContainer).getDisplayContent().layoutAndAssignWindowLayersIfNeeded();
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                throw new IllegalArgumentException("reparent: could not find task=" + taskController);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public Configuration setOrientation(int requestedOrientation, int displayId, Configuration displayConfig, boolean freezeScreenIfNeeded) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                IBinder binder = null;
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "Attempted to set orientation of non-existing app token: " + this.mToken);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return null;
                }
                ((AppWindowToken) this.mContainer).setOrientation(requestedOrientation);
                if (freezeScreenIfNeeded) {
                    binder = this.mToken.asBinder();
                }
                Configuration updateOrientationFromAppTokens = this.mService.updateOrientationFromAppTokens(displayConfig, binder, displayId);
                WindowManagerService.resetPriorityAfterLockedSection();
                return updateOrientationFromAppTokens;
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public int getOrientation() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return -1;
                }
                int orientationIgnoreVisibility = ((AppWindowToken) this.mContainer).getOrientationIgnoreVisibility();
                WindowManagerService.resetPriorityAfterLockedSection();
                return orientationIgnoreVisibility;
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void setDisablePreviewScreenshots(boolean disable) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "Attempted to set disable screenshots of non-existing app token: " + this.mToken);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                ((AppWindowToken) this.mContainer).setDisablePreviewScreenshots(disable);
                WindowManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003e, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0041, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0165, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0168, code lost:
        return;
     */
    public void setVisibility(boolean visible, boolean deferHidingClient) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "Attempted to set visibility of non-existing app token: " + this.mToken);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                AppWindowToken wtoken = (AppWindowToken) this.mContainer;
                if (visible || !wtoken.hiddenRequested) {
                    if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                        Slog.v("WindowManager", "setAppVisibility(" + this.mToken + ", visible=" + visible + "): " + this.mService.mAppTransition + " hidden=" + wtoken.isHidden() + " hiddenRequested=" + wtoken.hiddenRequested + " Callers=" + Debug.getCallers(6));
                    }
                    this.mService.mOpeningApps.remove(wtoken);
                    this.mService.mClosingApps.remove(wtoken);
                    wtoken.waitingToShow = false;
                    wtoken.hiddenRequested = !visible;
                    wtoken.mDeferHidingClient = deferHidingClient;
                    if (!visible) {
                        wtoken.removeDeadWindows();
                    } else {
                        if (!this.mService.mAppTransition.isTransitionSet() && this.mService.mAppTransition.isReady()) {
                            this.mService.mOpeningApps.add(wtoken);
                        }
                        wtoken.startingMoved = false;
                        if (wtoken.isHidden() || wtoken.mAppStopped) {
                            wtoken.clearAllDrawn();
                            if (wtoken.isHidden()) {
                                wtoken.waitingToShow = true;
                            }
                        }
                        wtoken.setClientHidden(false);
                        wtoken.requestUpdateWallpaperIfNeeded();
                        wtoken.mAppStopped = false;
                        ((AppWindowToken) this.mContainer).transferStartingWindowFromHiddenAboveTokenIfNeeded();
                    }
                    if (!wtoken.okToAnimate() || !this.mService.mAppTransition.isTransitionSet()) {
                        wtoken.setVisibility(null, visible, -1, true, wtoken.mVoiceInteraction);
                        wtoken.updateReportedVisibilityLocked();
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    wtoken.inPendingTransaction = true;
                    if (visible) {
                        this.mService.mOpeningApps.add(wtoken);
                        wtoken.mEnteringAnimation = true;
                    } else {
                        this.mService.mClosingApps.add(wtoken);
                        wtoken.mEnteringAnimation = false;
                    }
                    if (this.mService.mAppTransition.getAppTransition() == 16) {
                        WindowState win = this.mService.getDefaultDisplayContentLocked().findFocusedWindow();
                        if (win != null) {
                            AppWindowToken focusedToken = win.mAppToken;
                            if (focusedToken != null) {
                                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                                    Slog.d("WindowManager", "TRANSIT_TASK_OPEN_BEHIND,  adding " + focusedToken + " to mOpeningApps");
                                }
                                focusedToken.setHidden(true);
                                this.mService.mOpeningApps.add(focusedToken);
                            }
                        }
                    }
                } else if (!deferHidingClient && wtoken.mDeferHidingClient) {
                    wtoken.mDeferHidingClient = deferHidingClient;
                    wtoken.setClientHidden(true);
                }
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void notifyUnknownVisibilityLaunched() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    this.mService.mUnknownAppVisibilityController.notifyLaunched((AppWindowToken) this.mContainer);
                }
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    /* JADX WARNING: Removed duplicated region for block: B:162:0x028e A[Catch:{ all -> 0x02d8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:165:0x0293  */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:181:0x02c3=Splitter:B:181:0x02c3, B:186:0x02d3=Splitter:B:186:0x02d3} */
    public boolean addStartingWindow(String pkg, int theme, CompatibilityInfo compatInfo, CharSequence nonLocalizedLabel, int labelRes, int icon, int logo, int windowFlags, IBinder transferFrom, boolean newTask, boolean taskSwitch, boolean processRunning, boolean allowTaskSnapshot, boolean activityCreated, boolean fromRecents) {
        WindowHashMap windowHashMap;
        boolean z;
        boolean z2;
        boolean z3;
        boolean z4;
        IBinder transferFrom2;
        int type;
        boolean z5;
        int windowFlags2;
        CompatibilityInfo compatibilityInfo;
        boolean z6;
        boolean z7;
        IBinder transferFrom3;
        StringBuilder sb;
        String str = pkg;
        int i = theme;
        CompatibilityInfo compatibilityInfo2 = compatInfo;
        WindowHashMap windowHashMap2 = this.mWindowMap;
        synchronized (windowHashMap2) {
            WindowManagerService.boostPriorityForLockedSection();
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                try {
                    sb = new StringBuilder();
                    sb.append("setAppStartingWindow: token=");
                    sb.append(this.mToken);
                    sb.append(" pkg=");
                    sb.append(str);
                    sb.append(" transferFrom=");
                } catch (Throwable th) {
                    snapshot = th;
                    IBinder iBinder = transferFrom;
                    boolean z8 = newTask;
                    boolean z9 = taskSwitch;
                    boolean z10 = processRunning;
                    boolean z11 = allowTaskSnapshot;
                    windowHashMap = windowHashMap2;
                    CompatibilityInfo compatibilityInfo3 = compatibilityInfo2;
                    while (true) {
                        try {
                            break;
                        } catch (Throwable th2) {
                            snapshot = th2;
                        }
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw snapshot;
                }
                try {
                    sb.append(transferFrom);
                    sb.append(" newTask=");
                    z = newTask;
                    try {
                        sb.append(z);
                        sb.append(" taskSwitch=");
                        z2 = taskSwitch;
                    } catch (Throwable th3) {
                        snapshot = th3;
                        boolean z92 = taskSwitch;
                        boolean z102 = processRunning;
                        boolean z112 = allowTaskSnapshot;
                        windowHashMap = windowHashMap2;
                        CompatibilityInfo compatibilityInfo32 = compatibilityInfo2;
                        while (true) {
                            break;
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw snapshot;
                    }
                    try {
                        sb.append(z2);
                        sb.append(" processRunning=");
                        z3 = processRunning;
                    } catch (Throwable th4) {
                        snapshot = th4;
                        boolean z1022 = processRunning;
                        boolean z1122 = allowTaskSnapshot;
                        windowHashMap = windowHashMap2;
                        CompatibilityInfo compatibilityInfo322 = compatibilityInfo2;
                        while (true) {
                            break;
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw snapshot;
                    }
                } catch (Throwable th5) {
                    snapshot = th5;
                    boolean z82 = newTask;
                    boolean z922 = taskSwitch;
                    boolean z10222 = processRunning;
                    boolean z11222 = allowTaskSnapshot;
                    windowHashMap = windowHashMap2;
                    CompatibilityInfo compatibilityInfo3222 = compatibilityInfo2;
                    while (true) {
                        break;
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw snapshot;
                }
                try {
                    sb.append(z3);
                    sb.append(" allowTaskSnapshot=");
                    z4 = allowTaskSnapshot;
                    try {
                        sb.append(z4);
                        Slog.v("WindowManager", sb.toString());
                    } catch (Throwable th6) {
                        snapshot = th6;
                    }
                } catch (Throwable th7) {
                    snapshot = th7;
                    boolean z112222 = allowTaskSnapshot;
                    windowHashMap = windowHashMap2;
                    CompatibilityInfo compatibilityInfo32222 = compatibilityInfo2;
                    while (true) {
                        break;
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw snapshot;
                }
            } else {
                z = newTask;
                z2 = taskSwitch;
                z3 = processRunning;
                z4 = allowTaskSnapshot;
            }
            try {
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "Attempted to set icon of non-existing app token: " + this.mToken);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return false;
                } else if (!((AppWindowToken) this.mContainer).okToDisplay()) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return false;
                } else if (((AppWindowToken) this.mContainer).startingData != null) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return false;
                } else {
                    WindowState mainWin = ((AppWindowToken) this.mContainer).findMainWindow();
                    if (mainWin != null) {
                        if (mainWin.mWinAnimator.getShown()) {
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return false;
                        }
                    }
                    if (sSkipStartingWindowActivitys.contains(this.mToken.getName())) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return false;
                    }
                    ActivityManager.TaskSnapshot snapshot = this.mService.mTaskSnapshotController.getSnapshot(((AppWindowToken) this.mContainer).getTask().mTaskId, ((AppWindowToken) this.mContainer).getTask().mUserId, false, false);
                    WindowState windowState = mainWin;
                    int type2 = getStartingWindowType(z, z2, z3, z4, activityCreated, fromRecents, snapshot);
                    if (type2 == 1) {
                        try {
                            if (!sDisableSnapShots.contains(str) && this.mTask.isSamePackageInTask()) {
                                boolean createSnapshot = createSnapshot(snapshot);
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return createSnapshot;
                            }
                        } catch (Throwable th8) {
                            snapshot = th8;
                            int i2 = windowFlags;
                            windowHashMap = windowHashMap2;
                            CompatibilityInfo compatibilityInfo4 = compatibilityInfo2;
                            while (true) {
                                break;
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw snapshot;
                        }
                    }
                    if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                        Slog.v("WindowManager", "Checking theme of starting window: 0x" + Integer.toHexString(theme));
                    }
                    if (i != 0) {
                        AttributeCache.Entry ent = AttributeCache.instance().get(str, i, R.styleable.Window, this.mService.mCurrentUserId);
                        if (ent == null) {
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return false;
                        }
                        boolean windowIsTranslucent = ent.array.getBoolean(5, false);
                        boolean windowIsFloating = ent.array.getBoolean(4, false);
                        boolean windowShowWallpaper = ent.array.getBoolean(14, false);
                        boolean windowDisableStarting = ent.array.getBoolean(12, false);
                        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                            Slog.v("WindowManager", "Translucent=" + windowIsTranslucent + " Floating=" + windowIsFloating + " ShowWallpaper=" + windowShowWallpaper);
                        }
                        if ("com.huawei.android.launcher".equals(str)) {
                            int i3 = type2;
                            AttributeCache.Entry entry = ent;
                            windowHashMap = windowHashMap2;
                            CompatibilityInfo compatibilityInfo5 = compatibilityInfo2;
                            z6 = false;
                        } else if (this.mService.isSplitMode()) {
                            ActivityManager.TaskSnapshot taskSnapshot = snapshot;
                            int i4 = type2;
                            AttributeCache.Entry entry2 = ent;
                            windowHashMap = windowHashMap2;
                            CompatibilityInfo compatibilityInfo6 = compatibilityInfo2;
                            z6 = false;
                        } else if (compatibilityInfo2.mAppInfo == null) {
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return false;
                        } else {
                            boolean hwStartWindow = HwServiceFactory.getHwAppWindowContainerController() == null ? false : HwServiceFactory.getHwAppWindowContainerController().isHwStartWindowEnabled(str);
                            if (!compatibilityInfo2.mAppInfo.isSystemApp()) {
                                if (!compatibilityInfo2.mAppInfo.isPrivilegedApp() && !compatibilityInfo2.mAppInfo.isUpdatedSystemApp()) {
                                    z7 = true;
                                    boolean isUnistall = z7;
                                    if (hwStartWindow || !isUnistall) {
                                        type = type2;
                                        AttributeCache.Entry entry3 = ent;
                                        z5 = false;
                                        windowHashMap = windowHashMap2;
                                        compatibilityInfo = compatibilityInfo2;
                                        if (!windowIsTranslucent) {
                                            WindowManagerService.resetPriorityAfterLockedSection();
                                            return false;
                                        } else if (windowIsFloating || windowDisableStarting) {
                                            WindowManagerService.resetPriorityAfterLockedSection();
                                            return false;
                                        } else {
                                            transferFrom2 = transferFrom;
                                        }
                                    } else {
                                        ActivityManager.TaskSnapshot taskSnapshot2 = snapshot;
                                        type = type2;
                                        z5 = false;
                                        windowHashMap = windowHashMap2;
                                        AttributeCache.Entry entry4 = ent;
                                        AttributeCache.Entry entry5 = ent;
                                        compatibilityInfo = compatibilityInfo2;
                                        try {
                                            int result = HwServiceFactory.getHwAppWindowContainerController().continueHwStartWindow(str, entry4, compatibilityInfo2.mAppInfo, processRunning, windowIsFloating, windowIsTranslucent, windowDisableStarting, newTask, taskSwitch, windowShowWallpaper, transferFrom, this.mToken, this.mRoot, fromRecents);
                                            if (result < 0) {
                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                return false;
                                            }
                                            if (result > 0) {
                                                transferFrom3 = HwServiceFactory.getHwAppWindowContainerController().getTransferFrom(compatibilityInfo.mAppInfo);
                                            } else {
                                                transferFrom3 = transferFrom;
                                            }
                                            transferFrom2 = transferFrom3;
                                        } catch (Throwable th9) {
                                            snapshot = th9;
                                            while (true) {
                                                break;
                                            }
                                            WindowManagerService.resetPriorityAfterLockedSection();
                                            throw snapshot;
                                        }
                                    }
                                    if (windowShowWallpaper) {
                                        try {
                                            if (((AppWindowToken) this.mContainer).getDisplayContent().mWallpaperController.getWallpaperTarget() == null) {
                                                windowFlags2 = windowFlags | DumpState.DUMP_DEXOPT;
                                            } else if (!hwStartWindow || !isUnistall) {
                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                return z5;
                                            }
                                        } catch (Throwable th10) {
                                            snapshot = th10;
                                            int i5 = windowFlags;
                                            while (true) {
                                                break;
                                            }
                                            WindowManagerService.resetPriorityAfterLockedSection();
                                            throw snapshot;
                                        }
                                    }
                                    windowFlags2 = windowFlags;
                                }
                            }
                            z7 = false;
                            boolean isUnistall2 = z7;
                            if (hwStartWindow) {
                            }
                            type = type2;
                            AttributeCache.Entry entry32 = ent;
                            z5 = false;
                            windowHashMap = windowHashMap2;
                            compatibilityInfo = compatibilityInfo2;
                            if (!windowIsTranslucent) {
                            }
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return z6;
                    }
                    ActivityManager.TaskSnapshot taskSnapshot3 = snapshot;
                    type = type2;
                    z5 = false;
                    windowHashMap = windowHashMap2;
                    compatibilityInfo = compatibilityInfo2;
                    windowFlags2 = windowFlags;
                    transferFrom2 = transferFrom;
                    try {
                        if (((AppWindowToken) this.mContainer).transferStartingWindow(transferFrom2)) {
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return true;
                        } else if (type != 2) {
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return z5;
                        } else {
                            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                                Slog.v("WindowManager", "Creating SplashScreenStartingData");
                            }
                            SplashScreenStartingData splashScreenStartingData = new SplashScreenStartingData(this.mService, pkg, theme, compatibilityInfo, nonLocalizedLabel, labelRes, icon, logo, windowFlags2, ((AppWindowToken) this.mContainer).getMergedOverrideConfiguration());
                            ((AppWindowToken) this.mContainer).startingData = splashScreenStartingData;
                            scheduleAddStartingWindow();
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return true;
                        }
                    } catch (Throwable th11) {
                        snapshot = th11;
                        while (true) {
                            break;
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw snapshot;
                    }
                }
            } catch (RemoteException e) {
                Slog.w("WindowManager", "fail to getName for " + this.mToken);
            } catch (Throwable th12) {
                snapshot = th12;
                windowHashMap = windowHashMap2;
                CompatibilityInfo compatibilityInfo7 = compatibilityInfo2;
                while (true) {
                    break;
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                throw snapshot;
            }
        }
    }

    private boolean isContainedOnlyOneVisibleWindow() {
        WindowList<WindowState> child = ((AppWindowToken) this.mContainer).mChildren;
        for (int i = child.size() - 1; i >= 0; i--) {
            WindowState win = (WindowState) child.get(i);
            if ((win.mAttrs.flags & 2) != 0 || (win.mAttrs.flags & 4) != 0) {
                return false;
            }
        }
        return true;
    }

    private int getStartingWindowType(boolean newTask, boolean taskSwitch, boolean processRunning, boolean allowTaskSnapshot, boolean activityCreated, boolean fromRecents, ActivityManager.TaskSnapshot snapshot) {
        if (this.mService.mAppTransition.getAppTransition() == 19) {
            return 0;
        }
        int nType = 2;
        if (newTask || !processRunning || (taskSwitch && !activityCreated)) {
            return 2;
        }
        if (!taskSwitch || !allowTaskSnapshot) {
            return 0;
        }
        if (snapshot == null) {
            nType = 0;
        } else if (snapshotOrientationSameAsTask(snapshot) || fromRecents) {
            nType = 1;
        }
        if (1 == nType && !isContainedOnlyOneVisibleWindow()) {
            Slog.d("WindowManager", "Skip adding snapshot startingWindow for activity with more than one window, " + ((AppWindowToken) this.mContainer).toString());
            nType = 0;
        }
        return nType;
    }

    /* access modifiers changed from: package-private */
    public void scheduleAddStartingWindow() {
        if (!this.mService.mAnimationHandler.hasCallbacks(this.mAddStartingWindow)) {
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v("WindowManager", "Enqueueing ADD_STARTING");
            }
            if (HwPCUtils.isPcCastModeInServer() && (this.mContainer instanceof AppWindowToken)) {
                TaskStack ts = ((AppWindowToken) this.mContainer).getTask().mStack;
                if (ts != null && HwPCUtils.isPcDynamicStack(ts.mStackId)) {
                    return;
                }
            }
            this.mService.mAnimationHandler.postAtFrontOfQueue(this.mAddStartingWindow);
        }
    }

    private boolean createSnapshot(ActivityManager.TaskSnapshot snapshot) {
        if (snapshot == null) {
            return false;
        }
        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
            Slog.v("WindowManager", "Creating SnapshotStartingData");
        }
        ((AppWindowToken) this.mContainer).startingData = new SnapshotStartingData(this.mService, snapshot);
        scheduleAddStartingWindow();
        return true;
    }

    private boolean snapshotOrientationSameAsTask(ActivityManager.TaskSnapshot snapshot) {
        boolean z = false;
        if (snapshot == null) {
            return false;
        }
        if (((AppWindowToken) this.mContainer).getTask().getConfiguration().orientation == snapshot.getOrientation()) {
            z = true;
        }
        return z;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x003a, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x003d, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0074, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0077, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00ec, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00ef, code lost:
        return;
     */
    public void removeStartingWindow() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (((AppWindowToken) this.mContainer).startingWindow == null) {
                    if (((AppWindowToken) this.mContainer).startingData != null) {
                        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                            Slog.v("WindowManager", "Clearing startingData for token=" + this.mContainer);
                        }
                        ((AppWindowToken) this.mContainer).startingData = null;
                    }
                } else if (((AppWindowToken) this.mContainer).startingData != null) {
                    WindowManagerPolicy.StartingSurface surface = ((AppWindowToken) this.mContainer).startingSurface;
                    ((AppWindowToken) this.mContainer).startingData = null;
                    ((AppWindowToken) this.mContainer).startingSurface = null;
                    ((AppWindowToken) this.mContainer).startingWindow = null;
                    ((AppWindowToken) this.mContainer).startingDisplayed = false;
                    if (surface != null) {
                        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                            Slog.v("WindowManager", "Schedule remove starting " + this.mContainer + " startingWindow=" + ((AppWindowToken) this.mContainer).startingWindow + " startingView=" + ((AppWindowToken) this.mContainer).startingSurface + " Callers=" + Debug.getCallers(5));
                        }
                        this.mService.mAnimationHandler.post(new Runnable() {
                            public final void run() {
                                AppWindowContainerController.lambda$removeStartingWindow$2(WindowManagerPolicy.StartingSurface.this);
                            }
                        });
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } else if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                        Slog.v("WindowManager", "startingWindow was set but startingSurface==null, couldn't remove");
                    }
                } else if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                    Slog.v("WindowManager", "Tried to remove starting window but startingWindow was null:" + this.mContainer);
                }
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    static /* synthetic */ void lambda$removeStartingWindow$2(WindowManagerPolicy.StartingSurface surface) {
        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
            Slog.v("WindowManager", "Removing startingView=" + surface);
        }
        try {
            surface.remove();
        } catch (Exception e) {
            Slog.w("WindowManager", "Exception when removing starting window", e);
        }
    }

    public void pauseKeyDispatching() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    this.mService.mInputMonitor.pauseDispatchingLw((WindowToken) this.mContainer);
                }
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    public void resumeKeyDispatching() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    this.mService.mInputMonitor.resumeDispatchingLw((WindowToken) this.mContainer);
                }
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    public void notifyAppResumed(boolean wasStopped) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "Attempted to notify resumed of non-existing app token: " + this.mToken);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                ((AppWindowToken) this.mContainer).notifyAppResumed(wasStopped);
                WindowManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void notifyAppStopping() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "Attempted to notify stopping on non-existing app token: " + this.mToken);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                ((AppWindowToken) this.mContainer).detachChildren();
                WindowManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void notifyAppStopped() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "Attempted to notify stopped of non-existing app token: " + this.mToken);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                ((AppWindowToken) this.mContainer).notifyAppStopped();
                WindowManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0050, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0053, code lost:
        return;
     */
    public void startFreezingScreen(int configChanges) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "Attempted to freeze screen with non-existing app token: " + this.mContainer);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                if (configChanges == 0) {
                    if (((AppWindowToken) this.mContainer).okToDisplay()) {
                        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                            Slog.v("WindowManager", "Skipping set freeze of " + this.mToken);
                        }
                    }
                }
                ((AppWindowToken) this.mContainer).startFreezingScreen();
                WindowManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void stopFreezingScreen(boolean force) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v("WindowManager", "Clear freezing of " + this.mToken + ": hidden=" + ((AppWindowToken) this.mContainer).isHidden() + " freezing=" + ((AppWindowToken) this.mContainer).isFreezingScreen());
                }
                ((AppWindowToken) this.mContainer).stopFreezingScreen(true, force);
                WindowManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void registerRemoteAnimations(RemoteAnimationDefinition definition) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "Attempted to register remote animations with non-existing app token: " + this.mToken);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                ((AppWindowToken) this.mContainer).registerRemoteAnimations(definition);
                WindowManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void reportStartingWindowDrawn() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2));
    }

    /* access modifiers changed from: package-private */
    public void reportWindowsDrawn() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1));
    }

    /* access modifiers changed from: package-private */
    public void reportWindowsVisible() {
        this.mHandler.post(this.mOnWindowsVisible);
    }

    /* access modifiers changed from: package-private */
    public void reportWindowsGone() {
        this.mHandler.post(this.mOnWindowsGone);
    }

    /* access modifiers changed from: package-private */
    public boolean keyDispatchingTimedOut(String reason, int windowPid) {
        return this.mListener != null && ((AppWindowContainerListener) this.mListener).keyDispatchingTimedOut(reason, windowPid);
    }

    public void setWillCloseOrEnterPip(boolean willCloseOrEnterPip) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                ((AppWindowToken) this.mContainer).setWillCloseOrEnterPip(willCloseOrEnterPip);
                WindowManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public String toString() {
        return "AppWindowContainerController{ token=" + this.mToken + " mContainer=" + this.mContainer + " mListener=" + this.mListener + "}";
    }

    public void setShowWhenLocked(boolean showWhenLocked) {
        if (this.mContainer != null) {
            ((AppWindowToken) this.mContainer).setShowWhenLocked(showWhenLocked);
        }
    }
}
