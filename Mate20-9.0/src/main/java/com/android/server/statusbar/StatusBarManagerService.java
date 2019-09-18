package com.android.server.statusbar;

import android.app.ActivityThread;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.biometrics.IBiometricPromptReceiver;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.internal.statusbar.IStatusBar;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.internal.util.DumpUtils;
import com.android.server.LocalServices;
import com.android.server.notification.NotificationDelegate;
import com.android.server.policy.GlobalActionsProvider;
import com.android.server.power.ShutdownThread;
import com.android.server.wm.WindowManagerService;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StatusBarManagerService extends IStatusBarService.Stub {
    private static final boolean SPEW = false;
    private static final String TAG = "StatusBarManagerService";
    /* access modifiers changed from: private */
    public volatile IStatusBar mBar;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public int mCurrentUserId;
    private final ArrayList<DisableRecord> mDisableRecords = new ArrayList<>();
    private int mDisabled1 = 0;
    /* access modifiers changed from: private */
    public int mDisabled2 = 0;
    private final Rect mDockedStackBounds = new Rect();
    private int mDockedStackSysUiVisibility;
    private final Rect mFullscreenStackBounds = new Rect();
    private int mFullscreenStackSysUiVisibility;
    /* access modifiers changed from: private */
    public GlobalActionsProvider.GlobalActionsListener mGlobalActionListener;
    private final GlobalActionsProvider mGlobalActionsProvider = new GlobalActionsProvider() {
        public boolean isGlobalActionsDisabled() {
            return (StatusBarManagerService.this.mDisabled2 & 8) != 0;
        }

        public void setGlobalActionsListener(GlobalActionsProvider.GlobalActionsListener listener) {
            GlobalActionsProvider.GlobalActionsListener unused = StatusBarManagerService.this.mGlobalActionListener = listener;
            StatusBarManagerService.this.mGlobalActionListener.onGlobalActionsAvailableChanged(StatusBarManagerService.this.mBar != null);
        }

        public void showGlobalActions() {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.showGlobalActionsMenu();
                } catch (RemoteException e) {
                }
            }
        }
    };
    private Handler mHandler = new Handler();
    private ArrayMap<String, StatusBarIcon> mIcons = new ArrayMap<>();
    private int mImeBackDisposition;
    private IBinder mImeToken = null;
    private int mImeWindowVis = 0;
    private final StatusBarManagerInternal mInternalService = new StatusBarManagerInternal() {
        private boolean mNotificationLightOn;

        public void setNotificationDelegate(NotificationDelegate delegate) {
            NotificationDelegate unused = StatusBarManagerService.this.mNotificationDelegate = delegate;
        }

        public void showScreenPinningRequest(int taskId) {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.showScreenPinningRequest(taskId);
                } catch (RemoteException e) {
                }
            }
        }

        public void showAssistDisclosure() {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.showAssistDisclosure();
                } catch (RemoteException e) {
                }
            }
        }

        public void startAssist(Bundle args) {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.startAssist(args);
                } catch (RemoteException e) {
                }
            }
        }

        public void onCameraLaunchGestureDetected(int source) {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.onCameraLaunchGestureDetected(source);
                } catch (RemoteException e) {
                }
            }
        }

        public void topAppWindowChanged(boolean menuVisible) {
            StatusBarManagerService.this.topAppWindowChanged(menuVisible);
        }

        public void setSystemUiVisibility(int vis, int fullscreenStackVis, int dockedStackVis, int mask, Rect fullscreenBounds, Rect dockedBounds, String cause) {
            StatusBarManagerService.this.setSystemUiVisibility(vis, fullscreenStackVis, dockedStackVis, mask, fullscreenBounds, dockedBounds, cause);
        }

        public void toggleSplitScreen() {
            StatusBarManagerService.this.enforceStatusBarService();
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.toggleSplitScreen();
                } catch (RemoteException e) {
                }
            }
        }

        public void appTransitionFinished() {
            StatusBarManagerService.this.enforceStatusBarService();
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.appTransitionFinished();
                } catch (RemoteException e) {
                }
            }
        }

        public void toggleRecentApps() {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.toggleRecentApps();
                } catch (RemoteException e) {
                }
            }
        }

        public void setCurrentUser(int newUserId) {
            int unused = StatusBarManagerService.this.mCurrentUserId = newUserId;
        }

        public void preloadRecentApps() {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.preloadRecentApps();
                } catch (RemoteException e) {
                }
            }
        }

        public void cancelPreloadRecentApps() {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.cancelPreloadRecentApps();
                } catch (RemoteException e) {
                }
            }
        }

        public void showRecentApps(boolean triggeredFromAltTab) {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.showRecentApps(triggeredFromAltTab);
                } catch (RemoteException e) {
                }
            }
        }

        public void hideRecentApps(boolean triggeredFromAltTab, boolean triggeredFromHomeKey) {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.hideRecentApps(triggeredFromAltTab, triggeredFromHomeKey);
                } catch (RemoteException e) {
                }
            }
        }

        public void dismissKeyboardShortcutsMenu() {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.dismissKeyboardShortcutsMenu();
                } catch (RemoteException e) {
                }
            }
        }

        public void toggleKeyboardShortcutsMenu(int deviceId) {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.toggleKeyboardShortcutsMenu(deviceId);
                } catch (RemoteException e) {
                }
            }
        }

        public void showChargingAnimation(int batteryLevel) {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.showWirelessChargingAnimation(batteryLevel);
                } catch (RemoteException e) {
                }
            }
        }

        public void showPictureInPictureMenu() {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.showPictureInPictureMenu();
                } catch (RemoteException e) {
                }
            }
        }

        public void setWindowState(int window, int state) {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.setWindowState(window, state);
                } catch (RemoteException e) {
                }
            }
        }

        public void appTransitionPending() {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.appTransitionPending();
                } catch (RemoteException e) {
                }
            }
        }

        public void appTransitionCancelled() {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.appTransitionCancelled();
                } catch (RemoteException e) {
                }
            }
        }

        public void appTransitionStarting(long statusBarAnimationsStartTime, long statusBarAnimationsDuration) {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.appTransitionStarting(statusBarAnimationsStartTime, statusBarAnimationsDuration);
                } catch (RemoteException e) {
                }
            }
        }

        public void setTopAppHidesStatusBar(boolean hidesStatusBar) {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.setTopAppHidesStatusBar(hidesStatusBar);
                } catch (RemoteException e) {
                }
            }
        }

        public boolean showShutdownUi(boolean isReboot, String reason) {
            if (StatusBarManagerService.this.mContext.getResources().getBoolean(17957020) && StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.showShutdownUi(isReboot, reason);
                    return true;
                } catch (RemoteException e) {
                }
            }
            return false;
        }

        public void onProposedRotationChanged(int rotation, boolean isValid) {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.onProposedRotationChanged(rotation, isValid);
                } catch (RemoteException e) {
                }
            }
        }
    };
    private final Object mLock = new Object();
    private boolean mMenuVisible = false;
    /* access modifiers changed from: private */
    public NotificationDelegate mNotificationDelegate;
    private boolean mShowImeSwitcher;
    private IBinder mSysUiVisToken = new Binder();
    private int mSystemUiVisibility = 0;
    private final WindowManagerService mWindowManager;

    private class DisableRecord implements IBinder.DeathRecipient {
        String pkg;
        IBinder token;
        int userId;
        int what1;
        int what2;

        public DisableRecord(int userId2, IBinder token2) {
            this.userId = userId2;
            this.token = token2;
            try {
                token2.linkToDeath(this, 0);
            } catch (RemoteException e) {
            }
        }

        public void binderDied() {
            Slog.i(StatusBarManagerService.TAG, "binder died for pkg=" + this.pkg);
            StatusBarManagerService.this.disableForUser(0, this.token, this.pkg, this.userId);
            StatusBarManagerService.this.disable2ForUser(0, this.token, this.pkg, this.userId);
            this.token.unlinkToDeath(this, 0);
        }

        public void setFlags(int what, int which, String pkg2) {
            Slog.i("StatusBarDisable", "setFlags what=" + Integer.toHexString(what) + " which=" + which + " pkg=" + pkg2);
            switch (which) {
                case 1:
                    this.what1 = what;
                    return;
                case 2:
                    this.what2 = what;
                    return;
                default:
                    Slog.w(StatusBarManagerService.TAG, "Can't set unsupported disable flag " + which + ": 0x" + Integer.toHexString(what));
                    this.pkg = pkg2;
                    return;
            }
        }

        public int getFlags(int which) {
            switch (which) {
                case 1:
                    return this.what1;
                case 2:
                    return this.what2;
                default:
                    Slog.w(StatusBarManagerService.TAG, "Can't get unsupported disable flag " + which);
                    return 0;
            }
        }

        public boolean isEmpty() {
            return this.what1 == 0 && this.what2 == 0;
        }

        public String toString() {
            return String.format("userId=%d what1=0x%08X what2=0x%08X pkg=%s token=%s", new Object[]{Integer.valueOf(this.userId), Integer.valueOf(this.what1), Integer.valueOf(this.what2), this.pkg, this.token});
        }
    }

    public StatusBarManagerService(Context context, WindowManagerService windowManager) {
        this.mContext = context;
        this.mWindowManager = windowManager;
        LocalServices.addService(StatusBarManagerInternal.class, this.mInternalService);
        LocalServices.addService(GlobalActionsProvider.class, this.mGlobalActionsProvider);
    }

    public void expandNotificationsPanel() {
        enforceExpandStatusBar();
        if (this.mBar != null) {
            try {
                this.mBar.animateExpandNotificationsPanel();
            } catch (RemoteException e) {
            }
        }
    }

    public void collapsePanels() {
        enforceExpandStatusBar();
        if (this.mBar != null) {
            try {
                this.mBar.animateCollapsePanels();
            } catch (RemoteException e) {
            }
        }
    }

    public void togglePanel() {
        enforceExpandStatusBar();
        if (this.mBar != null) {
            try {
                this.mBar.togglePanel();
            } catch (RemoteException e) {
            }
        }
    }

    public void expandSettingsPanel(String subPanel) {
        enforceExpandStatusBar();
        if (this.mBar != null) {
            try {
                this.mBar.animateExpandSettingsPanel(subPanel);
            } catch (RemoteException e) {
            }
        }
    }

    public void addTile(ComponentName component) {
        enforceStatusBarOrShell();
        if (this.mBar != null) {
            try {
                this.mBar.addQsTile(component);
            } catch (RemoteException e) {
            }
        }
    }

    public void remTile(ComponentName component) {
        enforceStatusBarOrShell();
        if (this.mBar != null) {
            try {
                this.mBar.remQsTile(component);
            } catch (RemoteException e) {
            }
        }
    }

    public void clickTile(ComponentName component) {
        enforceStatusBarOrShell();
        if (this.mBar != null) {
            try {
                this.mBar.clickQsTile(component);
            } catch (RemoteException e) {
            }
        }
    }

    public void handleSystemKey(int key) throws RemoteException {
        enforceExpandStatusBar();
        if (this.mBar != null) {
            try {
                this.mBar.handleSystemKey(key);
            } catch (RemoteException e) {
            }
        }
    }

    public void showPinningEnterExitToast(boolean entering) throws RemoteException {
        if (this.mBar != null) {
            try {
                this.mBar.showPinningEnterExitToast(entering);
            } catch (RemoteException e) {
            }
        }
    }

    public void showPinningEscapeToast() throws RemoteException {
        if (this.mBar != null) {
            try {
                this.mBar.showPinningEscapeToast();
            } catch (RemoteException e) {
            }
        }
    }

    public void showFingerprintDialog(Bundle bundle, IBiometricPromptReceiver receiver) {
        if (this.mBar != null) {
            try {
                this.mBar.showFingerprintDialog(bundle, receiver);
            } catch (RemoteException e) {
            }
        }
    }

    public void onFingerprintAuthenticated() {
        if (this.mBar != null) {
            try {
                this.mBar.onFingerprintAuthenticated();
            } catch (RemoteException e) {
            }
        }
    }

    public void onFingerprintHelp(String message) {
        if (this.mBar != null) {
            try {
                this.mBar.onFingerprintHelp(message);
            } catch (RemoteException e) {
            }
        }
    }

    public void onFingerprintError(String error) {
        if (this.mBar != null) {
            try {
                this.mBar.onFingerprintError(error);
            } catch (RemoteException e) {
            }
        }
    }

    public void hideFingerprintDialog() {
        if (this.mBar != null) {
            try {
                this.mBar.hideFingerprintDialog();
            } catch (RemoteException e) {
            }
        }
    }

    public void disable(int what, IBinder token, String pkg) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.STATUSBAR_DISABLE);
        }
        disableForUser(what, token, pkg, this.mCurrentUserId);
    }

    public void disableForUser(int what, IBinder token, String pkg, int userId) {
        enforceStatusBar();
        synchronized (this.mLock) {
            disableLocked(userId, what, token, pkg, 1);
        }
    }

    public void disable2(int what, IBinder token, String pkg) {
        disable2ForUser(what, token, pkg, this.mCurrentUserId);
    }

    public void disable2ForUser(int what, IBinder token, String pkg, int userId) {
        enforceStatusBar();
        synchronized (this.mLock) {
            disableLocked(userId, what, token, pkg, 2);
        }
    }

    private void disableLocked(int userId, int what, IBinder token, String pkg, int whichFlag) {
        manageDisableListLocked(userId, what, token, pkg, whichFlag);
        final int net1 = gatherDisableActionsLocked(this.mCurrentUserId, 1);
        int net2 = gatherDisableActionsLocked(this.mCurrentUserId, 2);
        if (net1 != this.mDisabled1 || net2 != this.mDisabled2) {
            this.mDisabled1 = net1;
            this.mDisabled2 = net2;
            this.mHandler.post(new Runnable() {
                public void run() {
                    StatusBarManagerService.this.mNotificationDelegate.onSetDisabled(net1);
                }
            });
            if (this.mBar != null) {
                try {
                    Slog.i("StatusBarDisable", "disableLocked mBar.disable net1=" + Integer.toHexString(net1) + " net2=" + Integer.toHexString(net2) + " pkg=" + pkg + " what=" + Integer.toHexString(what));
                    this.mBar.disable(net1, net2);
                } catch (RemoteException ex) {
                    Slog.i("StatusBarDisable", "mBar.disable failed! ex=" + ex.toString());
                }
            } else {
                Slog.i(TAG, "disableLocked mBar is null.");
            }
        }
    }

    public void setIcon(String slot, String iconPackage, int iconId, int iconLevel, String contentDescription) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.STATUSBAR_SETICON);
        }
        enforceStatusBar();
        synchronized (this.mIcons) {
            StatusBarIcon statusBarIcon = new StatusBarIcon(iconPackage, UserHandle.SYSTEM, iconId, iconLevel, 0, contentDescription);
            StatusBarIcon icon = statusBarIcon;
            this.mIcons.put(slot, icon);
            if (this.mBar != null) {
                try {
                    this.mBar.setIcon(slot, icon);
                } catch (RemoteException e) {
                }
            }
        }
    }

    public void setIconVisibility(String slot, boolean visibility) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.STATUSBAR_SETICONVISIBILITY);
        }
        enforceStatusBar();
        synchronized (this.mIcons) {
            StatusBarIcon icon = this.mIcons.get(slot);
            if (icon != null) {
                if (icon.visible != visibility) {
                    icon.visible = visibility;
                    if (this.mBar != null) {
                        try {
                            this.mBar.setIcon(slot, icon);
                        } catch (RemoteException e) {
                        }
                    }
                }
            }
        }
    }

    public void removeIcon(String slot) {
        enforceStatusBar();
        synchronized (this.mIcons) {
            this.mIcons.remove(slot);
            if (this.mBar != null) {
                try {
                    this.mBar.removeIcon(slot);
                } catch (RemoteException e) {
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void topAppWindowChanged(final boolean menuVisible) {
        enforceStatusBar();
        synchronized (this.mLock) {
            this.mMenuVisible = menuVisible;
            this.mHandler.post(new Runnable() {
                public void run() {
                    IStatusBar bar = StatusBarManagerService.this.mBar;
                    if (bar != null) {
                        try {
                            bar.topAppWindowChanged(menuVisible);
                        } catch (RemoteException e) {
                        }
                    }
                }
            });
        }
    }

    public void setImeWindowStatus(IBinder token, int vis, int backDisposition, boolean showImeSwitcher) {
        enforceStatusBar();
        synchronized (this.mLock) {
            this.mImeWindowVis = vis;
            this.mImeBackDisposition = backDisposition;
            this.mImeToken = token;
            this.mShowImeSwitcher = showImeSwitcher;
            Handler handler = this.mHandler;
            final IBinder iBinder = token;
            final int i = vis;
            final int i2 = backDisposition;
            final boolean z = showImeSwitcher;
            AnonymousClass5 r2 = new Runnable() {
                public void run() {
                    IStatusBar bar = StatusBarManagerService.this.mBar;
                    if (bar != null) {
                        try {
                            bar.setImeWindowStatus(iBinder, i, i2, z);
                        } catch (RemoteException e) {
                        }
                    }
                }
            };
            handler.post(r2);
        }
    }

    public void setSystemUiVisibility(int vis, int mask, String cause) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.STATUSBAR_SETSYSTEMUIVISIBILITY);
        }
        setSystemUiVisibility(vis, 0, 0, mask, this.mFullscreenStackBounds, this.mDockedStackBounds, cause);
    }

    /* access modifiers changed from: private */
    public void setSystemUiVisibility(int vis, int fullscreenStackVis, int dockedStackVis, int mask, Rect fullscreenBounds, Rect dockedBounds, String cause) {
        enforceStatusBarService();
        synchronized (this.mLock) {
            updateUiVisibilityLocked(vis, fullscreenStackVis, dockedStackVis, mask, fullscreenBounds, dockedBounds);
            disableLocked(this.mCurrentUserId, vis & 67043328, this.mSysUiVisToken, cause, 1);
        }
    }

    private void updateUiVisibilityLocked(int vis, int fullscreenStackVis, int dockedStackVis, int mask, Rect fullscreenBounds, Rect dockedBounds) {
        int i = vis;
        int i2 = fullscreenStackVis;
        int i3 = dockedStackVis;
        Rect rect = fullscreenBounds;
        Rect rect2 = dockedBounds;
        if (this.mSystemUiVisibility != i || this.mFullscreenStackSysUiVisibility != i2 || this.mDockedStackSysUiVisibility != i3 || !this.mFullscreenStackBounds.equals(rect) || !this.mDockedStackBounds.equals(rect2)) {
            this.mSystemUiVisibility = i;
            this.mFullscreenStackSysUiVisibility = i2;
            this.mDockedStackSysUiVisibility = i3;
            this.mFullscreenStackBounds.set(rect);
            this.mDockedStackBounds.set(rect2);
            Handler handler = this.mHandler;
            final int i4 = i;
            final int i5 = i2;
            final int i6 = i3;
            final int i7 = mask;
            final Rect rect3 = rect;
            final Rect rect4 = rect2;
            AnonymousClass6 r0 = new Runnable() {
                public void run() {
                    IStatusBar bar = StatusBarManagerService.this.mBar;
                    if (bar != null) {
                        try {
                            bar.setSystemUiVisibility(i4, i5, i6, i7, rect3, rect4);
                        } catch (Exception ex) {
                            Slog.e(StatusBarManagerService.TAG, "Exception had happend in method updateUiVisibilityLocked, getMessage=" + ex.getMessage());
                        }
                    }
                }
            };
            handler.post(r0);
        }
    }

    private void enforceStatusBarOrShell() {
        if (Binder.getCallingUid() != 2000) {
            enforceStatusBar();
        }
    }

    private void enforceStatusBar() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR", TAG);
    }

    private void enforceExpandStatusBar() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.EXPAND_STATUS_BAR", TAG);
    }

    /* access modifiers changed from: private */
    public void enforceStatusBarService() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", TAG);
    }

    public void registerStatusBar(IStatusBar bar, List<String> iconSlots, List<StatusBarIcon> iconList, int[] switches, List<IBinder> binders, Rect fullscreenStackBounds, Rect dockedStackBounds) {
        enforceStatusBarService();
        Slog.i(TAG, "registerStatusBar bar=" + bar);
        synchronized (this.mLock) {
            this.mBar = bar;
            try {
                this.mBar.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                    public void binderDied() {
                        IStatusBar unused = StatusBarManagerService.this.mBar = null;
                        StatusBarManagerService.this.notifyBarAttachChanged();
                    }
                }, 0);
            } catch (RemoteException e) {
            }
            notifyBarAttachChanged();
        }
        synchronized (this.mIcons) {
            for (String slot : this.mIcons.keySet()) {
                iconSlots.add(slot);
                iconList.add(this.mIcons.get(slot));
            }
        }
        synchronized (this.mLock) {
            switches[0] = gatherDisableActionsLocked(this.mCurrentUserId, 1);
            switches[1] = this.mSystemUiVisibility;
            switches[2] = this.mMenuVisible;
            switches[3] = this.mImeWindowVis;
            switches[4] = this.mImeBackDisposition;
            switches[5] = this.mShowImeSwitcher;
            switches[6] = gatherDisableActionsLocked(this.mCurrentUserId, 2);
            switches[7] = this.mFullscreenStackSysUiVisibility;
            switches[8] = this.mDockedStackSysUiVisibility;
            binders.add(this.mImeToken);
            fullscreenStackBounds.set(this.mFullscreenStackBounds);
            dockedStackBounds.set(this.mDockedStackBounds);
        }
    }

    /* access modifiers changed from: private */
    public void notifyBarAttachChanged() {
        this.mHandler.post(new Runnable() {
            public final void run() {
                StatusBarManagerService.lambda$notifyBarAttachChanged$0(StatusBarManagerService.this);
            }
        });
    }

    public static /* synthetic */ void lambda$notifyBarAttachChanged$0(StatusBarManagerService statusBarManagerService) {
        if (statusBarManagerService.mGlobalActionListener != null) {
            statusBarManagerService.mGlobalActionListener.onGlobalActionsAvailableChanged(statusBarManagerService.mBar != null);
        }
    }

    public void onPanelRevealed(boolean clearNotificationEffects, int numItems) {
        enforceStatusBarService();
        long identity = Binder.clearCallingIdentity();
        try {
            this.mNotificationDelegate.onPanelRevealed(clearNotificationEffects, numItems);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void clearNotificationEffects() throws RemoteException {
        enforceStatusBarService();
        long identity = Binder.clearCallingIdentity();
        try {
            this.mNotificationDelegate.clearEffects();
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void onPanelHidden() throws RemoteException {
        enforceStatusBarService();
        long identity = Binder.clearCallingIdentity();
        try {
            this.mNotificationDelegate.onPanelHidden();
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void shutdown() {
        enforceStatusBarService();
        long identity = Binder.clearCallingIdentity();
        try {
            this.mHandler.post($$Lambda$StatusBarManagerService$izMbpkX9bmZwnjh3sH07yuoJPNY.INSTANCE);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void reboot(boolean safeMode) {
        enforceStatusBarService();
        long identity = Binder.clearCallingIdentity();
        try {
            this.mHandler.post(new Runnable(safeMode) {
                private final /* synthetic */ boolean f$0;

                {
                    this.f$0 = r1;
                }

                public final void run() {
                    StatusBarManagerService.lambda$reboot$2(this.f$0);
                }
            });
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    static /* synthetic */ void lambda$reboot$2(boolean safeMode) {
        if (safeMode) {
            ShutdownThread.rebootSafeMode(getUiContext(), true);
        } else {
            ShutdownThread.reboot(getUiContext(), "userrequested", false);
        }
    }

    public void onGlobalActionsShown() {
        enforceStatusBarService();
        long identity = Binder.clearCallingIdentity();
        try {
            if (this.mGlobalActionListener != null) {
                this.mGlobalActionListener.onGlobalActionsShown();
                Binder.restoreCallingIdentity(identity);
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void onGlobalActionsHidden() {
        enforceStatusBarService();
        long identity = Binder.clearCallingIdentity();
        try {
            if (this.mGlobalActionListener != null) {
                this.mGlobalActionListener.onGlobalActionsDismissed();
                Binder.restoreCallingIdentity(identity);
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void onNotificationClick(String key, NotificationVisibility nv) {
        enforceStatusBarService();
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();
        long identity = Binder.clearCallingIdentity();
        try {
            this.mNotificationDelegate.onNotificationClick(callingUid, callingPid, key, nv);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void onNotificationActionClick(String key, int actionIndex, NotificationVisibility nv) {
        enforceStatusBarService();
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();
        long identity = Binder.clearCallingIdentity();
        try {
            this.mNotificationDelegate.onNotificationActionClick(callingUid, callingPid, key, actionIndex, nv);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void onNotificationError(String pkg, String tag, int id, int uid, int initialPid, String message, int userId) {
        enforceStatusBarService();
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();
        long identity = Binder.clearCallingIdentity();
        try {
            this.mNotificationDelegate.onNotificationError(callingUid, callingPid, pkg, tag, id, uid, initialPid, message, userId);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void onNotificationClear(String pkg, String tag, int id, int userId, String key, int dismissalSurface, NotificationVisibility nv) {
        enforceStatusBarService();
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();
        long identity = Binder.clearCallingIdentity();
        try {
            this.mNotificationDelegate.onNotificationClear(callingUid, callingPid, pkg, tag, id, userId, key, dismissalSurface, nv);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void onNotificationVisibilityChanged(NotificationVisibility[] newlyVisibleKeys, NotificationVisibility[] noLongerVisibleKeys) throws RemoteException {
        enforceStatusBarService();
        long identity = Binder.clearCallingIdentity();
        try {
            this.mNotificationDelegate.onNotificationVisibilityChanged(newlyVisibleKeys, noLongerVisibleKeys);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void onNotificationExpansionChanged(String key, boolean userAction, boolean expanded) throws RemoteException {
        enforceStatusBarService();
        long identity = Binder.clearCallingIdentity();
        try {
            this.mNotificationDelegate.onNotificationExpansionChanged(key, userAction, expanded);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void onNotificationDirectReplied(String key) throws RemoteException {
        enforceStatusBarService();
        long identity = Binder.clearCallingIdentity();
        try {
            this.mNotificationDelegate.onNotificationDirectReplied(key);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void onNotificationSmartRepliesAdded(String key, int replyCount) throws RemoteException {
        enforceStatusBarService();
        long identity = Binder.clearCallingIdentity();
        try {
            this.mNotificationDelegate.onNotificationSmartRepliesAdded(key, replyCount);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void onNotificationSmartReplySent(String key, int replyIndex) throws RemoteException {
        enforceStatusBarService();
        long identity = Binder.clearCallingIdentity();
        try {
            this.mNotificationDelegate.onNotificationSmartReplySent(key, replyIndex);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void onNotificationSettingsViewed(String key) throws RemoteException {
        enforceStatusBarService();
        long identity = Binder.clearCallingIdentity();
        try {
            this.mNotificationDelegate.onNotificationSettingsViewed(key);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void onClearAllNotifications(int userId) {
        enforceStatusBarService();
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();
        long identity = Binder.clearCallingIdentity();
        try {
            this.mNotificationDelegate.onClearAll(callingUid, callingPid, userId);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [android.os.Binder] */
    /* JADX WARNING: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new StatusBarShellCommand(this).exec(this, in, out, err, args, callback, resultReceiver);
    }

    public String[] getStatusBarIcons() {
        return this.mContext.getResources().getStringArray(17236037);
    }

    /* access modifiers changed from: package-private */
    public void manageDisableListLocked(int userId, int what, IBinder token, String pkg, int which) {
        if (Log.HWINFO) {
            if (!Pattern.compile("[0-9]++").matcher(pkg != null ? pkg : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS).find()) {
                Slog.d(TAG, "manageDisableList userId=" + userId + " what=0x" + Integer.toHexString(what) + " pkg=" + pkg + " which=" + which);
            }
        }
        int N = this.mDisableRecords.size();
        DisableRecord record = null;
        int i = 0;
        while (true) {
            if (i >= N) {
                break;
            }
            DisableRecord r = this.mDisableRecords.get(i);
            if (r.token == token && r.userId == userId) {
                record = r;
                break;
            }
            i++;
        }
        if (!token.isBinderAlive()) {
            if (record != null) {
                this.mDisableRecords.remove(i);
                Slog.i(TAG, "Disable records remove token " + record + " list: " + this.mDisableRecords);
                record.token.unlinkToDeath(record, 0);
            }
        } else if (record != null) {
            record.setFlags(what, which, pkg);
            if (record.isEmpty()) {
                this.mDisableRecords.remove(i);
                record.token.unlinkToDeath(record, 0);
            }
        } else {
            DisableRecord record2 = new DisableRecord(userId, token);
            record2.setFlags(what, which, pkg);
            this.mDisableRecords.add(record2);
        }
    }

    /* access modifiers changed from: package-private */
    public int gatherDisableActionsLocked(int userId, int which) {
        int N = this.mDisableRecords.size();
        int net = 0;
        for (int i = 0; i < N; i++) {
            DisableRecord rec = this.mDisableRecords.get(i);
            if (rec.userId == userId) {
                net |= rec.getFlags(which);
            }
        }
        return net;
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            synchronized (this.mLock) {
                pw.println("  mDisabled1=0x" + Integer.toHexString(this.mDisabled1));
                pw.println("  mDisabled2=0x" + Integer.toHexString(this.mDisabled2));
                int N = this.mDisableRecords.size();
                pw.println("  mDisableRecords.size=" + N);
                for (int i = 0; i < N; i++) {
                    pw.println("    [" + i + "] " + this.mDisableRecords.get(i));
                }
                pw.println("  mCurrentUserId=" + this.mCurrentUserId);
                pw.println("  mIcons=");
                for (String slot : this.mIcons.keySet()) {
                    pw.println("    ");
                    pw.print(slot);
                    pw.print(" -> ");
                    StatusBarIcon icon = this.mIcons.get(slot);
                    pw.print(icon);
                    if (!TextUtils.isEmpty(icon.contentDescription)) {
                        pw.print(" \"");
                        pw.print(icon.contentDescription);
                        pw.print("\"");
                    }
                    pw.println();
                }
            }
        }
    }

    public IStatusBar getStatusBar() {
        return this.mBar;
    }

    public boolean isNotificationsPanelExpand() {
        if (this.mBar == null) {
            return false;
        }
        try {
            return this.mBar.isNotificationPanelExpanded();
        } catch (RemoteException e) {
            return false;
        }
    }

    private static final Context getUiContext() {
        return ActivityThread.currentActivityThread().getSystemUiContext();
    }
}
