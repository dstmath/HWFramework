package com.android.server.statusbar;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.internal.statusbar.IStatusBar;
import com.android.internal.statusbar.IStatusBarService.Stub;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.server.LocalServices;
import com.android.server.notification.NotificationDelegate;
import com.android.server.power.IHwShutdownThread;
import com.android.server.wm.WindowManagerService;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StatusBarManagerService extends Stub {
    private static final boolean SPEW = false;
    private static final String TAG = "StatusBarManagerService";
    private volatile IStatusBar mBar;
    private final Context mContext;
    private int mCurrentUserId;
    private final ArrayList<DisableRecord> mDisableRecords;
    private int mDisabled1;
    private int mDisabled2;
    private final Rect mDockedStackBounds;
    private int mDockedStackSysUiVisibility;
    private final Rect mFullscreenStackBounds;
    private int mFullscreenStackSysUiVisibility;
    private Handler mHandler;
    private ArrayMap<String, StatusBarIcon> mIcons;
    private int mImeBackDisposition;
    private IBinder mImeToken;
    private int mImeWindowVis;
    private final StatusBarManagerInternal mInternalService;
    private final Object mLock;
    private boolean mMenuVisible;
    private NotificationDelegate mNotificationDelegate;
    private boolean mShowImeSwitcher;
    private IBinder mSysUiVisToken;
    private int mSystemUiVisibility;
    private final WindowManagerService mWindowManager;

    /* renamed from: com.android.server.statusbar.StatusBarManagerService.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ int val$net1;

        AnonymousClass2(int val$net1) {
            this.val$net1 = val$net1;
        }

        public void run() {
            StatusBarManagerService.this.mNotificationDelegate.onSetDisabled(this.val$net1);
        }
    }

    /* renamed from: com.android.server.statusbar.StatusBarManagerService.3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ boolean val$menuVisible;

        AnonymousClass3(boolean val$menuVisible) {
            this.val$menuVisible = val$menuVisible;
        }

        public void run() {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.topAppWindowChanged(this.val$menuVisible);
                } catch (RemoteException e) {
                }
            }
        }
    }

    /* renamed from: com.android.server.statusbar.StatusBarManagerService.4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ int val$backDisposition;
        final /* synthetic */ boolean val$showImeSwitcher;
        final /* synthetic */ IBinder val$token;
        final /* synthetic */ int val$vis;

        AnonymousClass4(IBinder val$token, int val$vis, int val$backDisposition, boolean val$showImeSwitcher) {
            this.val$token = val$token;
            this.val$vis = val$vis;
            this.val$backDisposition = val$backDisposition;
            this.val$showImeSwitcher = val$showImeSwitcher;
        }

        public void run() {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.setImeWindowStatus(this.val$token, this.val$vis, this.val$backDisposition, this.val$showImeSwitcher);
                } catch (RemoteException e) {
                }
            }
        }
    }

    /* renamed from: com.android.server.statusbar.StatusBarManagerService.5 */
    class AnonymousClass5 implements Runnable {
        final /* synthetic */ Rect val$dockedBounds;
        final /* synthetic */ int val$dockedStackVis;
        final /* synthetic */ Rect val$fullscreenBounds;
        final /* synthetic */ int val$fullscreenStackVis;
        final /* synthetic */ int val$mask;
        final /* synthetic */ int val$vis;

        AnonymousClass5(int val$vis, int val$fullscreenStackVis, int val$dockedStackVis, int val$mask, Rect val$fullscreenBounds, Rect val$dockedBounds) {
            this.val$vis = val$vis;
            this.val$fullscreenStackVis = val$fullscreenStackVis;
            this.val$dockedStackVis = val$dockedStackVis;
            this.val$mask = val$mask;
            this.val$fullscreenBounds = val$fullscreenBounds;
            this.val$dockedBounds = val$dockedBounds;
        }

        public void run() {
            if (StatusBarManagerService.this.mBar != null) {
                try {
                    StatusBarManagerService.this.mBar.setSystemUiVisibility(this.val$vis, this.val$fullscreenStackVis, this.val$dockedStackVis, this.val$mask, this.val$fullscreenBounds, this.val$dockedBounds);
                } catch (RemoteException e) {
                }
            }
        }
    }

    private class DisableRecord implements DeathRecipient {
        String pkg;
        IBinder token;
        int userId;
        int what1;
        int what2;

        private DisableRecord() {
        }

        public void binderDied() {
            Slog.i(StatusBarManagerService.TAG, "binder died for pkg=" + this.pkg);
            StatusBarManagerService.this.disableForUser(0, this.token, this.pkg, this.userId);
            StatusBarManagerService.this.disable2ForUser(0, this.token, this.pkg, this.userId);
            this.token.unlinkToDeath(this, 0);
        }

        public String toString() {
            return "DisableRecord{userId=" + this.userId + " pkg=" + this.pkg + " what1=" + Integer.toHexString(this.what1) + " what2=" + Integer.toHexString(this.what2) + "}";
        }
    }

    public StatusBarManagerService(Context context, WindowManagerService windowManager) {
        this.mHandler = new Handler();
        this.mIcons = new ArrayMap();
        this.mDisableRecords = new ArrayList();
        this.mSysUiVisToken = new Binder();
        this.mDisabled1 = 0;
        this.mDisabled2 = 0;
        this.mLock = new Object();
        this.mSystemUiVisibility = 0;
        this.mFullscreenStackBounds = new Rect();
        this.mDockedStackBounds = new Rect();
        this.mMenuVisible = SPEW;
        this.mImeWindowVis = 0;
        this.mImeToken = null;
        this.mInternalService = new StatusBarManagerInternal() {
            private boolean mNotificationLightOn;

            public void setNotificationDelegate(NotificationDelegate delegate) {
                StatusBarManagerService.this.mNotificationDelegate = delegate;
            }

            public void buzzBeepBlinked() {
                if (StatusBarManagerService.this.mBar != null) {
                    try {
                        StatusBarManagerService.this.mBar.buzzBeepBlinked();
                    } catch (RemoteException e) {
                    }
                }
            }

            public void notificationLightPulse(int argb, int onMillis, int offMillis) {
                this.mNotificationLightOn = true;
                if (StatusBarManagerService.this.mBar != null) {
                    try {
                        StatusBarManagerService.this.mBar.notificationLightPulse(argb, onMillis, offMillis);
                    } catch (RemoteException e) {
                    }
                }
            }

            public void notificationLightOff() {
                if (this.mNotificationLightOn) {
                    this.mNotificationLightOn = StatusBarManagerService.SPEW;
                    if (StatusBarManagerService.this.mBar != null) {
                        try {
                            StatusBarManagerService.this.mBar.notificationLightOff();
                        } catch (RemoteException e) {
                        }
                    }
                }
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
                StatusBarManagerService.this.mCurrentUserId = newUserId;
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

            public void showRecentApps(boolean triggeredFromAltTab, boolean fromHome) {
                if (StatusBarManagerService.this.mBar != null) {
                    try {
                        StatusBarManagerService.this.mBar.showRecentApps(triggeredFromAltTab, fromHome);
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

            public void showTvPictureInPictureMenu() {
                if (StatusBarManagerService.this.mBar != null) {
                    try {
                        StatusBarManagerService.this.mBar.showTvPictureInPictureMenu();
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
        };
        this.mContext = context;
        this.mWindowManager = windowManager;
        LocalServices.addService(StatusBarManagerInternal.class, this.mInternalService);
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

    public void disable(int what, IBinder token, String pkg) {
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
        int net1 = gatherDisableActionsLocked(this.mCurrentUserId, 1);
        int net2 = gatherDisableActionsLocked(this.mCurrentUserId, 2);
        if (net1 != this.mDisabled1 || net2 != this.mDisabled2) {
            this.mDisabled1 = net1;
            this.mDisabled2 = net2;
            this.mHandler.post(new AnonymousClass2(net1));
            if (this.mBar != null) {
                try {
                    this.mBar.disable(net1, net2);
                    return;
                } catch (RemoteException e) {
                    return;
                }
            }
            Slog.i(TAG, "disableLocked mBar is null.");
        }
    }

    public void setIcon(String slot, String iconPackage, int iconId, int iconLevel, String contentDescription) {
        enforceStatusBar();
        synchronized (this.mIcons) {
            StatusBarIcon icon = new StatusBarIcon(iconPackage, UserHandle.SYSTEM, iconId, iconLevel, 0, contentDescription);
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
        enforceStatusBar();
        synchronized (this.mIcons) {
            StatusBarIcon icon = (StatusBarIcon) this.mIcons.get(slot);
            if (icon == null) {
                return;
            }
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

    private void topAppWindowChanged(boolean menuVisible) {
        enforceStatusBar();
        synchronized (this.mLock) {
            this.mMenuVisible = menuVisible;
            this.mHandler.post(new AnonymousClass3(menuVisible));
        }
    }

    public void setImeWindowStatus(IBinder token, int vis, int backDisposition, boolean showImeSwitcher) {
        enforceStatusBar();
        synchronized (this.mLock) {
            this.mImeWindowVis = vis;
            this.mImeBackDisposition = backDisposition;
            this.mImeToken = token;
            this.mShowImeSwitcher = showImeSwitcher;
            this.mHandler.post(new AnonymousClass4(token, vis, backDisposition, showImeSwitcher));
        }
    }

    public void setSystemUiVisibility(int vis, int mask, String cause) {
        setSystemUiVisibility(vis, 0, 0, mask, this.mFullscreenStackBounds, this.mDockedStackBounds, cause);
    }

    private void setSystemUiVisibility(int vis, int fullscreenStackVis, int dockedStackVis, int mask, Rect fullscreenBounds, Rect dockedBounds, String cause) {
        enforceStatusBarService();
        synchronized (this.mLock) {
            updateUiVisibilityLocked(vis, fullscreenStackVis, dockedStackVis, mask, fullscreenBounds, dockedBounds);
            disableLocked(this.mCurrentUserId, vis & 67043328, this.mSysUiVisToken, cause, 1);
        }
    }

    private void updateUiVisibilityLocked(int vis, int fullscreenStackVis, int dockedStackVis, int mask, Rect fullscreenBounds, Rect dockedBounds) {
        if (this.mSystemUiVisibility != vis || this.mFullscreenStackSysUiVisibility != fullscreenStackVis || this.mDockedStackSysUiVisibility != dockedStackVis || !this.mFullscreenStackBounds.equals(fullscreenBounds) || !this.mDockedStackBounds.equals(dockedBounds)) {
            this.mSystemUiVisibility = vis;
            this.mFullscreenStackSysUiVisibility = fullscreenStackVis;
            this.mDockedStackSysUiVisibility = dockedStackVis;
            this.mFullscreenStackBounds.set(fullscreenBounds);
            this.mDockedStackBounds.set(dockedBounds);
            this.mHandler.post(new AnonymousClass5(vis, fullscreenStackVis, dockedStackVis, mask, fullscreenBounds, dockedBounds));
        }
    }

    private void enforceStatusBarOrShell() {
        if (Binder.getCallingUid() != IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME) {
            enforceStatusBar();
        }
    }

    private void enforceStatusBar() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR", TAG);
    }

    private void enforceExpandStatusBar() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.EXPAND_STATUS_BAR", TAG);
    }

    private void enforceStatusBarService() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", TAG);
    }

    public void registerStatusBar(IStatusBar bar, List<String> iconSlots, List<StatusBarIcon> iconList, int[] switches, List<IBinder> binders, Rect fullscreenStackBounds, Rect dockedStackBounds) {
        enforceStatusBarService();
        Slog.i(TAG, "registerStatusBar bar=" + bar);
        this.mBar = bar;
        synchronized (this.mIcons) {
            for (String slot : this.mIcons.keySet()) {
                iconSlots.add(slot);
                iconList.add((StatusBarIcon) this.mIcons.get(slot));
            }
        }
        synchronized (this.mLock) {
            int i;
            switches[0] = gatherDisableActionsLocked(this.mCurrentUserId, 1);
            switches[1] = this.mSystemUiVisibility;
            switches[2] = this.mMenuVisible ? 1 : 0;
            switches[3] = this.mImeWindowVis;
            switches[4] = this.mImeBackDisposition;
            if (this.mShowImeSwitcher) {
                i = 1;
            } else {
                i = 0;
            }
            switches[5] = i;
            switches[6] = gatherDisableActionsLocked(this.mCurrentUserId, 2);
            switches[7] = this.mFullscreenStackSysUiVisibility;
            switches[8] = this.mDockedStackSysUiVisibility;
            binders.add(this.mImeToken);
            fullscreenStackBounds.set(this.mFullscreenStackBounds);
            dockedStackBounds.set(this.mDockedStackBounds);
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

    public void onNotificationClick(String key) {
        enforceStatusBarService();
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();
        long identity = Binder.clearCallingIdentity();
        try {
            this.mNotificationDelegate.onNotificationClick(callingUid, callingPid, key);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void onNotificationActionClick(String key, int actionIndex) {
        enforceStatusBarService();
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();
        long identity = Binder.clearCallingIdentity();
        try {
            this.mNotificationDelegate.onNotificationActionClick(callingUid, callingPid, key, actionIndex);
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

    public void onNotificationClear(String pkg, String tag, int id, int userId) {
        enforceStatusBarService();
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();
        long identity = Binder.clearCallingIdentity();
        try {
            this.mNotificationDelegate.onNotificationClear(callingUid, callingPid, pkg, tag, id, userId);
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

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ResultReceiver resultReceiver) throws RemoteException {
        new StatusBarShellCommand(this).exec(this, in, out, err, args, resultReceiver);
    }

    void manageDisableListLocked(int userId, int what, IBinder token, String pkg, int which) {
        if (Log.HWINFO) {
            String NUM_REGEX = "[0-9]++";
            if (!Pattern.compile("[0-9]++").matcher(pkg != null ? pkg : "").find()) {
                Slog.d(TAG, "manageDisableList userId=" + userId + " what=0x" + Integer.toHexString(what) + " pkg=" + pkg);
            }
        }
        int N = this.mDisableRecords.size();
        DisableRecord tok = null;
        int i = 0;
        while (i < N) {
            DisableRecord t = (DisableRecord) this.mDisableRecords.get(i);
            if (t.token == token && t.userId == userId) {
                tok = t;
                break;
            }
            i++;
        }
        if (what != 0 && token.isBinderAlive()) {
            if (tok == null) {
                tok = new DisableRecord();
                tok.userId = userId;
                try {
                    token.linkToDeath(tok, 0);
                    this.mDisableRecords.add(tok);
                } catch (RemoteException e) {
                    return;
                }
            }
            if (which == 1) {
                tok.what1 = what;
            } else {
                tok.what2 = what;
            }
            tok.token = token;
            tok.pkg = pkg;
        } else if (tok != null) {
            this.mDisableRecords.remove(i);
            Slog.i(TAG, "Disable records remove token " + tok + " list: " + this.mDisableRecords);
            tok.token.unlinkToDeath(tok, 0);
        }
    }

    int gatherDisableActionsLocked(int userId, int which) {
        int N = this.mDisableRecords.size();
        int net = 0;
        for (int i = 0; i < N; i++) {
            DisableRecord rec = (DisableRecord) this.mDisableRecords.get(i);
            if (rec.userId == userId) {
                net |= which == 1 ? rec.what1 : rec.what2;
            }
        }
        return net;
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump StatusBar from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        synchronized (this.mLock) {
            pw.println("  mDisabled1=0x" + Integer.toHexString(this.mDisabled1));
            pw.println("  mDisabled2=0x" + Integer.toHexString(this.mDisabled2));
            int N = this.mDisableRecords.size();
            pw.println("  mDisableRecords.size=" + N);
            for (int i = 0; i < N; i++) {
                DisableRecord tok = (DisableRecord) this.mDisableRecords.get(i);
                pw.println("    [" + i + "] userId=" + tok.userId + " what1=0x" + Integer.toHexString(tok.what1) + " what2=0x" + Integer.toHexString(tok.what2) + " pkg=" + tok.pkg + " token=" + tok.token);
            }
            pw.println("  mCurrentUserId=" + this.mCurrentUserId);
        }
    }

    public IStatusBar getStatusBar() {
        return this.mBar;
    }

    public boolean isNotificationsPanelExpand() {
        boolean result = SPEW;
        if (this.mBar != null) {
            try {
                result = this.mBar.isNotificationPanelExpanded();
            } catch (RemoteException e) {
            }
        }
        return result;
    }
}
