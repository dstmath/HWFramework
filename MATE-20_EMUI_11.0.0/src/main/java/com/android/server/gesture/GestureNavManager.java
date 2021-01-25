package com.android.server.gesture;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.display.HwFoldScreenState;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.util.Size;
import android.view.ISystemGestureExclusionListener;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import com.android.server.gesture.DefaultDeviceStateController;
import com.android.server.gesture.GestureNavView;
import com.android.server.policy.WindowManagerPolicyEx;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.ActivityManagerExt;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.app.WindowConfigurationEx;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.android.content.ContextEx;
import com.huawei.android.fsm.HwFoldScreenManager;
import com.huawei.android.fsm.HwFoldScreenManagerEx;
import com.huawei.android.internal.util.DumpUtilsEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.server.WatchdogEx;
import com.huawei.android.util.HwNotchSizeUtil;
import com.huawei.android.view.WindowManagerEx;
import com.huawei.hiai.awareness.AwarenessConstants;
import com.huawei.util.HwPartCommInterfaceWraper;
import java.io.PrintWriter;
import java.util.List;

public class GestureNavManager extends DefaultGestureNavManager implements GestureNavPolicy, GestureNavView.IGestureEventProxy {
    private static final int ANR_RESET_GESTURE_NAV_COUNT = 2;
    private static final long FOCUS_CHANGED_TIMEOUT = 1500;
    private static final boolean IS_GESTRUE_NAV_THREAD_DISABLED = SystemPropertiesEx.getBoolean("ro.config.gesturenavthread.disable", false);
    private static final int MSG_CONFIG_CHANGED = 3;
    private static final int MSG_DEVICE_STATE_CHANGED = 4;
    private static final int MSG_DISPLAY_CUTOUT_MODE_CHANGED = 12;
    private static final int MSG_FOCUS_CHANGED = 5;
    private static final int MSG_FOLD_DISPLAY_MODE_CHANGED = 19;
    private static final int MSG_GESTURE_EXCLUSION_REGION_CHANGED = 23;
    private static final int MSG_GESTURE_NAV_TIPS_CHANGED = 10;
    private static final int MSG_KEYGUARD_STATE_CHANGED = 6;
    private static final int MSG_LOCK_TASK_STATE_CHANGED = 13;
    private static final int MSG_MULTI_WINDOW_CHANGED = 18;
    private static final int MSG_NIGHT_MODE_CHANGED = 14;
    private static final int MSG_NOTCH_DISPLAY_CHANGED = 9;
    private static final int MSG_PREFER_CHANGED = 8;
    private static final int MSG_RELOAD_NAV_GLOBAL_STATE = 2;
    private static final int MSG_ROTATION_CHANGED = 7;
    private static final int MSG_SET_GESTURE_NAV_MODE = 11;
    private static final int MSG_SUB_SCREEN_BRING_TOP_NAV = 17;
    private static final int MSG_SUB_SCREEN_INIT_NAV = 15;
    private static final int MSG_SUB_SCREEN_REMOVE_NAV = 16;
    private static final int MSG_UPDATE_NAV_GLOBAL_STATE = 1;
    private static final int MSG_UPDATE_NAV_HORIZONTAL_SWITCH_STATE = 21;
    private static final int MSG_UPDATE_NAV_REGION = 22;
    private static final int MSG_USER_CHANGED = 20;
    private static final int NIGHT_MODE_DEFAULT = 1;
    private static final int NIGHT_MODE_ON = 2;
    private static final String TAG = "GestureNavManager";
    private int mAppGestureNavBottomMode;
    private int mAppGestureNavLeftMode;
    private int mAppGestureNavRightMode;
    private GestureNavBottomStrategy mBottomStrategy;
    private Context mContext;
    private int mCurrentUserId;
    private DensityObserver mDensityObserver;
    private String mDensityStr;
    private final DefaultDeviceStateController.DeviceChangedListener mDeviceChangedCallback = new DefaultDeviceStateController.DeviceChangedListener() {
        /* class com.android.server.gesture.GestureNavManager.AnonymousClass1 */

        public void onDeviceProvisionedChanged(boolean isProvisioned) {
            if (GestureNavManager.this.mIsNavStarted) {
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavManager.TAG, "Device provisioned changed, provisioned=" + isProvisioned);
                }
                GestureNavManager.this.mHandler.sendEmptyMessage(4);
            }
        }

        public void onUserSwitched(int newUserId) {
            if (GestureNavManager.this.mIsNavStarted) {
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavManager.TAG, "User switched, newUserId=" + newUserId);
                }
                GestureNavManager.this.mHandler.sendEmptyMessage(4);
            }
        }

        public void onUserSetupChanged(boolean isSetup) {
            if (GestureNavManager.this.mIsNavStarted) {
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavManager.TAG, "User setup changed, setup=" + isSetup);
                }
                GestureNavManager.this.mHandler.sendEmptyMessage(4);
            }
        }

        public void onPreferredActivityChanged(boolean isPrefer) {
            if (GestureNavManager.this.mIsNavStarted) {
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavManager.TAG, "Preferred activity changed, isPrefer=" + isPrefer);
                }
                GestureNavManager.this.mHandler.sendEmptyMessage(8);
            }
        }
    };
    private DeviceStateController mDeviceStateController;
    private Point mDisplaySize = new Point();
    private int mExcludedRegionHeight;
    private int mFocusAppUid;
    private String mFocusPackageName;
    private int mFocusWinNavOptions;
    private String mFocusWindowTitle;
    private FoldDisplayListener mFoldDisplayListener;
    private int mFoldDisplayMode;
    private GestureDataTracker mGestureDataTracker;
    private GestureExclusionListener mGestureExclusionListener;
    private int mGestureNavANRCount;
    private GestureNavAnimProxy mGestureNavAnimProxy;
    private GestureNavView mGestureNavBottom;
    private GestureNavView mGestureNavLeft;
    private GestureNavView mGestureNavRight;
    private Region mGlobalExcludeRegion = new Region();
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mHasGestureNavReady;
    private boolean mHasNotchProp;
    private int mHoleHeight = 0;
    private String mHomeWindow;
    private boolean mIsDeviceProvisioned;
    private boolean mIsFocusWindowUsingNotch = true;
    private boolean mIsGestureNavEnabled;
    private boolean mIsGestureNavTipsEnabled;
    private boolean mIsInKeyguardMainWindow;
    private boolean mIsInShrinkState;
    private boolean mIsKeyNavEnabled;
    private boolean mIsKeyguardShowing;
    private boolean mIsLandscape;
    private boolean mIsNavBottomEnabled;
    private boolean mIsNavLeftBackEnabled;
    private boolean mIsNavRightBackEnabled;
    private boolean mIsNavStarted;
    private boolean mIsNeedChange2FullHeight = false;
    private boolean mIsNightMode = false;
    private boolean mIsNotchDisplayDisabled;
    private boolean mIsSubScreenEnableGestureNav;
    private boolean mIsUserSetuped;
    private boolean mIsWindowViewSetuped;
    private LauncherStateChangedReceiver mLauncherStateChangedReceiver;
    private GestureNavBaseStrategy mLeftBackStrategy;
    private final Object mLock = new Object();
    private NightModeObserver mNightModeObserver;
    private NotchObserver mNotchObserver;
    private GestureNavBaseStrategy mRightBackStrategy;
    private int mRotation = GestureNavConst.DEFAULT_ROTATION;
    private int mShrinkNavId = 0;
    private GestureNavSubScreenManager mSubScreenGestureNavManager;
    private WindowManager mWindowManager;

    public GestureNavManager(Context context) {
        super(context);
        this.mContext = context;
        if (IS_GESTRUE_NAV_THREAD_DISABLED) {
            Log.w(TAG, "GestureNavManager thread is disabled.");
            this.mHandler = new Handler(Looper.getMainLooper());
            return;
        }
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new GestureHandler(this.mHandlerThread.getLooper());
    }

    @Override // com.android.server.gesture.GestureNavPolicy
    public Looper getGestureLoooper() {
        HandlerThread handlerThread = this.mHandlerThread;
        if (handlerThread != null) {
            return handlerThread.getLooper();
        }
        return null;
    }

    public void monitor() {
        synchronized (this.mLock) {
        }
    }

    @Override // com.android.server.gesture.GestureNavPolicy
    public void systemReady() {
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        WatchdogEx.addMonitor(this);
        WatchdogEx.addThread(this.mHandler);
        HwGestureNavWhiteConfig.getInstance().init(this.mContext);
        GestureUtils.systemReady();
        this.mHasNotchProp = GestureUtils.hasNotchProp();
        this.mGestureDataTracker = GestureDataTracker.getInstance(this.mContext);
        ContentResolver resolver = this.mContext.getContentResolver();
        ContentResolverExt.registerContentObserver(resolver, Settings.Secure.getUriFor(GestureNavConst.KEY_SECURE_GESTURE_NAVIGATION), false, new ContentObserver(new Handler()) {
            /* class com.android.server.gesture.GestureNavManager.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                Log.i(GestureNavManager.TAG, "gesture nav status change");
                GestureNavManager.this.mHandler.sendEmptyMessage(1);
            }
        }, -1);
        ContentResolverExt.registerContentObserver(resolver, Settings.Secure.getUriFor(GestureNavConst.KEY_SECURE_MULTI_WIN), false, new ContentObserver(new Handler()) {
            /* class com.android.server.gesture.GestureNavManager.AnonymousClass3 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                Log.i(GestureNavManager.TAG, "multi win switch status change");
                GestureNavManager.this.mHandler.sendEmptyMessage(1);
            }
        }, -1);
        registerGuideFinishObserver();
        registerHorizontalSwitchObserver(resolver);
        this.mHandler.sendEmptyMessage(1);
    }

    @Override // com.android.server.gesture.GestureNavPolicy
    public boolean isKeyNavEnabled() {
        return this.mIsKeyNavEnabled;
    }

    private void registerHorizontalSwitchObserver(ContentResolver resolver) {
        ContentResolverExt.registerContentObserver(resolver, Settings.Secure.getUriFor(GestureNavConst.KEY_SECURE_GESTURE_HORIZONTAL_SWITCH), false, new ContentObserver(new Handler()) {
            /* class com.android.server.gesture.GestureNavManager.AnonymousClass4 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                Log.i(GestureNavManager.TAG, "horizontal quick switch status change");
                GestureNavManager.this.mHandler.sendEmptyMessage(21);
            }
        }, -1);
    }

    private void registerGuideFinishObserver() {
        if (isShowDockEnabled()) {
            ContentResolverExt.registerContentObserver(this.mContext.getContentResolver(), Settings.Secure.getUriFor("user_setup_complete"), false, new ContentObserver(new Handler()) {
                /* class com.android.server.gesture.GestureNavManager.AnonymousClass5 */

                @Override // android.database.ContentObserver
                public void onChange(boolean isSelfChange) {
                    Log.i(GestureNavManager.TAG, "user setup complete status change");
                    if (!GestureNavManager.this.mIsNavStarted && GestureNavManager.this.isShowDockEnabled()) {
                        GestureNavManager.this.mHandler.sendEmptyMessage(1);
                    }
                }
            }, -1);
        }
    }

    /* access modifiers changed from: private */
    public final class DensityObserver extends ContentObserver {
        DensityObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange, Uri uri) {
            if (GestureNavManager.this.mIsNavStarted && GestureNavManager.this.updateDisplayDensity()) {
                GestureNavManager.this.mHandler.sendEmptyMessage(2);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class NotchObserver extends ContentObserver {
        NotchObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange, Uri uri) {
            if (GestureNavManager.this.mIsNavStarted) {
                GestureNavManager.this.mHandler.sendEmptyMessage(9);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class NightModeObserver extends ContentObserver {
        NightModeObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange, Uri uri) {
            if (GestureNavManager.this.mIsNavStarted) {
                Log.d(GestureNavManager.TAG, "NightModeObserver onChange");
                GestureNavManager.this.mHandler.sendEmptyMessage(14);
            }
        }
    }

    private final class GestureNavTipsObserver extends ContentObserver {
        GestureNavTipsObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange, Uri uri) {
            if (GestureNavManager.this.mIsNavStarted) {
                GestureNavManager.this.mHandler.sendEmptyMessage(10);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class LauncherStateChangedReceiver extends BroadcastReceiver {
        private LauncherStateChangedReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (GestureNavManager.this.mIsNavStarted) {
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavManager.TAG, "Launcher state changed, intent=" + intent);
                }
                GestureNavManager.this.mHandler.sendEmptyMessage(8);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class FoldDisplayListener implements HwFoldScreenManagerEx.FoldDisplayModeListener {
        private FoldDisplayListener() {
        }

        public void onScreenDisplayModeChange(int displayMode) {
            if (GestureNavManager.this.mIsNavStarted) {
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavManager.TAG, "Fold display mode changed, displayMode:" + displayMode);
                }
                GestureNavManager.this.mHandler.sendMessage(GestureNavManager.this.mHandler.obtainMessage(19, displayMode, 0));
            }
        }
    }

    /* access modifiers changed from: private */
    public final class GestureExclusionListener extends ISystemGestureExclusionListener.Stub {
        private GestureExclusionListener() {
        }

        public void onSystemGestureExclusionChanged(int displayId, Region systemGestureExclusion) {
            if (GestureNavManager.this.mIsNavStarted && displayId == 0) {
                if (GestureNavConst.DEBUG) {
                    Log.d(GestureNavManager.TAG, "systemGestureExclusion region changed:" + systemGestureExclusion);
                }
                GestureNavManager.this.mHandler.sendMessage(GestureNavManager.this.mHandler.obtainMessage(23, new Region(systemGestureExclusion)));
            }
        }
    }

    private final class GestureHandler extends Handler {
        GestureHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (GestureNavConst.DEBUG) {
                Log.d(GestureNavManager.TAG, "handleMessage before msg=" + msg.what);
            }
            int i = msg.what;
            if (i != 21) {
                switch (i) {
                    case 1:
                        GestureNavManager.this.updateGestureNavGlobalState();
                        break;
                    case 2:
                        GestureNavManager.this.reloadGestureNavGlobalState();
                        break;
                    case 3:
                        GestureNavManager.this.handleConfigChanged();
                        break;
                    case 4:
                        GestureNavManager.this.handleDeviceStateChanged();
                        break;
                    case 5:
                        GestureNavManager.this.handleFocusChanged((FocusWindowState) msg.obj);
                        break;
                    case 6:
                        GestureNavManager gestureNavManager = GestureNavManager.this;
                        boolean z = true;
                        if (msg.arg1 != 1) {
                            z = false;
                        }
                        gestureNavManager.handleKeygaurdStateChanged(z);
                        break;
                    case 7:
                        GestureNavManager.this.handleRotationChanged(msg.arg1);
                        break;
                    case 8:
                        GestureNavManager.this.handlePreferChanged();
                        break;
                    case 9:
                        GestureNavManager.this.handleNotchDisplayChanged();
                        break;
                    case 10:
                        GestureNavManager.this.handleGestureNavTipsChanged();
                        break;
                    case 11:
                        GestureNavManager.this.handleAppGestureNavMode((AppGestureNavMode) msg.obj);
                        break;
                }
            } else {
                GestureNavManager.this.updateHorizontalSwitchState();
            }
            GestureNavManager.this.handleOtherMessage(msg);
            if (GestureNavConst.DEBUG) {
                Log.d(GestureNavManager.TAG, "handleMessage after msg=" + msg.what);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleOtherMessage(Message msg) {
        switch (msg.what) {
            case 12:
                handleDisplayCutoutModeChanged();
                return;
            case 13:
                handleLockTaskStateChanged(msg.arg1);
                return;
            case 14:
                handleNightModeChanged();
                return;
            case 15:
                handleSubScreenCreateNavView();
                return;
            case 16:
                handleSubScreenDestoryNavView();
                return;
            case 17:
                handleSubScreenBringTopNavView();
                return;
            case 18:
                handleMultiWindowChanged(msg.arg1);
                return;
            case 19:
                handleFoldDisplayModeChanged();
                return;
            case 20:
                handleUserChanged();
                return;
            case 21:
            default:
                return;
            case 22:
                boolean z = true;
                if (msg.arg1 != 1) {
                    z = false;
                }
                handleNavRegionChanged(z, msg.arg2);
                return;
            case 23:
                handleGestureExclusionRegionChanged((Region) msg.obj);
                return;
        }
    }

    /* access modifiers changed from: private */
    public static final class FocusWindowState {
        public static final FocusWindowState EMPTY_FOCUS = new FocusWindowState(null, 0, null, true, 0);
        public int gestureNavOptions;
        public boolean isUseNotch;
        public String packageName;
        public String title;
        public int uid;

        FocusWindowState(String packageName2, int uid2, String title2, boolean isUseNotch2, int gestureNavOptions2) {
            this.packageName = packageName2;
            this.uid = uid2;
            this.title = title2;
            this.isUseNotch = isUseNotch2;
            this.gestureNavOptions = gestureNavOptions2;
        }
    }

    /* access modifiers changed from: private */
    public static final class AppGestureNavMode {
        public int bottomMode;
        public int leftMode;
        public String packageName;
        public int rightMode;
        public int uid;

        AppGestureNavMode(String packageName2, int uid2, int leftMode2, int rightMode2, int bottomMode2) {
            this.packageName = packageName2;
            this.uid = uid2;
            this.leftMode = leftMode2;
            this.rightMode = rightMode2;
            this.bottomMode = bottomMode2;
        }

        public boolean isFromSameApp(String packageName2, int uid2) {
            return packageName2 != null && packageName2.equals(this.packageName) && uid2 == this.uid;
        }

        public String toString() {
            return "pkg:" + this.packageName + ", uid:" + this.uid + ", left:" + this.leftMode + ", right:" + this.rightMode + ", bottom:" + this.bottomMode;
        }
    }

    private boolean updateEnableStateLocked() {
        boolean isOldStateEnable = this.mIsGestureNavEnabled;
        this.mIsGestureNavEnabled = GestureNavConst.isGestureNavEnabled(this.mContext, -2);
        this.mIsKeyNavEnabled = !this.mIsGestureNavEnabled;
        Log.i(TAG, "GestureNavEnabled=" + this.mIsGestureNavEnabled + ", tipsEnabled=" + this.mIsGestureNavTipsEnabled);
        if (this.mIsGestureNavEnabled != isOldStateEnable) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isShowDockEnabled() {
        return GestureNavConst.isShowDockEnabled(this.mContext, -2);
    }

    /* JADX INFO: Multiple debug info for r3v0 boolean: [D('devStateCtl' com.android.server.gesture.DeviceStateController), D('isNavEnableOld' boolean)] */
    private boolean updateNavEnableStateForHwMultiWinMode() {
        boolean isKeyNavEnabled;
        this.mIsGestureNavEnabled = true;
        try {
            isKeyNavEnabled = SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), GestureNavConst.KEY_SECURE_GESTURE_NAVIGATION, -2) == 0;
        } catch (Settings.SettingNotFoundException e) {
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "updateNavEnableStateForHwMultiWinMode: setting not found.");
            }
            DeviceStateController devStateCtl = DeviceStateController.getInstance(this.mContext);
            if (!devStateCtl.isDeviceProvisioned() || !devStateCtl.isCurrentUserSetup()) {
                Log.i(TAG, "updateNavEnableStateForHwMultiWinMode: user is setting up.");
                this.mIsGestureNavEnabled = false;
                isKeyNavEnabled = false;
            } else {
                isKeyNavEnabled = true;
            }
        }
        boolean isNavEnableOld = this.mIsKeyNavEnabled;
        this.mIsKeyNavEnabled = isKeyNavEnabled;
        boolean z = this.mIsKeyNavEnabled;
        if (z == isNavEnableOld || z) {
            return false;
        }
        return true;
    }

    private void hideGestureNavBottomViewIfNeed() {
        GestureNavView gestureNavView = this.mGestureNavBottom;
        if (gestureNavView != null && this.mIsKeyNavEnabled) {
            gestureNavView.show(false, false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateGestureNavGlobalState() {
        synchronized (this.mLock) {
            if (isShowDockEnabled() ? updateNavEnableStateForHwMultiWinMode() : updateEnableStateLocked()) {
                if (this.mIsGestureNavEnabled) {
                    startGestureNavLocked();
                } else {
                    stopGestureNavLocked();
                }
                hideGestureNavBottomViewIfNeed();
            } else {
                Log.i(TAG, "changing maybe quickly, reload to avoid start again");
                reloadGestureNavGlobalStateLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateHorizontalSwitchState() {
        synchronized (this.mLock) {
            if (this.mBottomStrategy != null) {
                this.mBottomStrategy.updateHorizontalSwitch();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reloadGestureNavGlobalState() {
        synchronized (this.mLock) {
            reloadGestureNavGlobalStateLocked();
        }
    }

    private void reloadGestureNavGlobalStateLocked() {
        Log.i(TAG, "force reloadGestureNavGlobalState");
        this.mIsGestureNavEnabled = false;
        this.mIsKeyNavEnabled = false;
        stopGestureNavLocked();
        if (isShowDockEnabled()) {
            updateNavEnableStateForHwMultiWinMode();
        } else {
            updateEnableStateLocked();
        }
        if (this.mIsGestureNavEnabled) {
            startGestureNavLocked();
        }
        hideGestureNavBottomViewIfNeed();
    }

    private void startGestureNavLocked() {
        Log.i(TAG, "startGestureNavLocked");
        this.mIsNavStarted = true;
        this.mGestureDataTracker.checkStartTrackerIfNeed();
        if (this.mDeviceStateController == null) {
            this.mDeviceStateController = DeviceStateController.getInstance(this.mContext);
            this.mDeviceStateController.addCallback(this.mDeviceChangedCallback);
        }
        registerLocalMonitor();
        resetAppGestureNavModeLocked();
        updateDisplayDensity();
        updateNotchDisplayStateLocked();
        updateFoldDisplayModeLocked();
        updateGestureNavStateLocked(true);
        handleSubScreenCreateNavView();
    }

    private void stopGestureNavLocked() {
        Log.i(TAG, "stopGestureNavLocked");
        updateGestureNavStateLocked(true);
        unregisterLocalMonitor();
        DeviceStateController deviceStateController = this.mDeviceStateController;
        if (deviceStateController != null) {
            deviceStateController.removeCallback(this.mDeviceChangedCallback);
            this.mDeviceStateController = null;
        }
        this.mIsNavStarted = false;
        handleSubScreenDestoryNavView();
    }

    private void registerLocalMonitor() {
        ContentResolver resolver = this.mContext.getContentResolver();
        if (this.mDensityObserver == null) {
            this.mDensityObserver = new DensityObserver(this.mHandler);
            ContentResolverExt.registerContentObserver(resolver, Settings.Secure.getUriFor("display_density_forced"), false, this.mDensityObserver, -1);
        }
        if (this.mNotchObserver == null) {
            this.mNotchObserver = new NotchObserver(this.mHandler);
            ContentResolverExt.registerContentObserver(resolver, Settings.Secure.getUriFor("display_notch_status"), false, this.mNotchObserver, -1);
        }
        if (this.mNightModeObserver == null) {
            this.mNightModeObserver = new NightModeObserver(this.mHandler);
            ContentResolverExt.registerContentObserver(resolver, Settings.Secure.getUriFor("ui_night_mode"), false, this.mNightModeObserver, -1);
        }
        if (this.mLauncherStateChangedReceiver == null) {
            this.mLauncherStateChangedReceiver = new LauncherStateChangedReceiver();
            IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
            filter.addDataScheme("package");
            filter.addDataSchemeSpecificPart("com.huawei.android.launcher", 0);
            filter.addAction("android.intent.action.PACKAGE_CHANGED");
            ContextEx.registerReceiverAsUser(this.mContext, this.mLauncherStateChangedReceiver, UserHandleEx.ALL, filter, (String) null, this.mHandler);
        }
        if (HwFoldScreenState.isFoldScreenDevice() && this.mFoldDisplayListener == null) {
            this.mFoldDisplayListener = new FoldDisplayListener();
            HwFoldScreenManagerEx.registerFoldDisplayMode(this.mFoldDisplayListener);
        }
        if (this.mGestureExclusionListener == null) {
            this.mGestureExclusionListener = new GestureExclusionListener();
            try {
                WindowManagerGlobal.getWindowManagerService().registerSystemGestureExclusionListener(this.mGestureExclusionListener, 0);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to register gesture exclusion listener");
            }
        }
    }

    private void unregisterLocalMonitor() {
        FoldDisplayListener foldDisplayListener;
        ContentResolver resolver = this.mContext.getContentResolver();
        DensityObserver densityObserver = this.mDensityObserver;
        if (densityObserver != null) {
            resolver.unregisterContentObserver(densityObserver);
            this.mDensityObserver = null;
        }
        NotchObserver notchObserver = this.mNotchObserver;
        if (notchObserver != null) {
            resolver.unregisterContentObserver(notchObserver);
            this.mNotchObserver = null;
        }
        NightModeObserver nightModeObserver = this.mNightModeObserver;
        if (nightModeObserver != null) {
            resolver.unregisterContentObserver(nightModeObserver);
            this.mNightModeObserver = null;
        }
        LauncherStateChangedReceiver launcherStateChangedReceiver = this.mLauncherStateChangedReceiver;
        if (launcherStateChangedReceiver != null) {
            this.mContext.unregisterReceiver(launcherStateChangedReceiver);
            this.mLauncherStateChangedReceiver = null;
        }
        if (HwFoldScreenState.isFoldScreenDevice() && (foldDisplayListener = this.mFoldDisplayListener) != null) {
            HwFoldScreenManagerEx.unregisterFoldDisplayMode(foldDisplayListener);
            this.mFoldDisplayListener = null;
        }
        if (this.mGestureExclusionListener != null) {
            try {
                WindowManagerGlobal.getWindowManagerService().unregisterSystemGestureExclusionListener(this.mGestureExclusionListener, 0);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to unregister gesture exclusion listener");
            }
            this.mGestureExclusionListener = null;
        }
    }

    private void updateGestureNavStateLocked() {
        updateGestureNavStateLocked(false);
    }

    private void updateGestureNavStateLocked(boolean updateDeviceState) {
        if (updateDeviceState) {
            updateDeviceStateLocked();
        }
        updateConfigLocked();
        updateNavWindowLocked();
        updateNavVisibleLocked();
    }

    @Override // com.android.server.gesture.GestureNavPolicy
    public boolean isGestureNavStartedNotLocked() {
        return this.mIsNavStarted;
    }

    @Override // com.android.server.gesture.GestureNavPolicy
    public void onUserChanged(int newUserId) {
        if (GestureNavConst.DEBUG) {
            Log.i(TAG, "User switched then reInit, newUserId=" + newUserId);
        }
        this.mHandler.sendEmptyMessage(2);
        if (GestureNavConst.SUPPORT_DOCK_TRIGGER) {
            this.mHandler.sendEmptyMessage(20);
        }
    }

    @Override // com.android.server.gesture.GestureNavPolicy
    public void onConfigurationChanged() {
        if (this.mIsNavStarted) {
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "onConfigurationChanged");
            }
            this.mHandler.sendEmptyMessage(3);
        }
    }

    @Override // com.android.server.gesture.GestureNavPolicy
    public void onMultiWindowChanged(int state) {
        if (this.mIsNavStarted) {
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "onMultiWindowChanged, state=" + state);
            }
            this.mHandler.sendMessage(this.mHandler.obtainMessage(18, state, 0));
        }
    }

    @Override // com.android.server.gesture.GestureNavPolicy
    public void onRotationChanged(int rotation) {
        if (this.mIsNavStarted) {
            HwGestureNavWhiteConfig.getInstance().updateRotation(rotation);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(7, rotation, 0));
        }
    }

    @Override // com.android.server.gesture.GestureNavPolicy
    public void onKeyguardShowingChanged(boolean isShowing) {
        if (this.mIsNavStarted) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(6, isShowing ? 1 : 0, 0), 300);
        }
    }

    @Override // com.android.server.gesture.GestureNavPolicy
    public boolean onFocusWindowChanged(WindowManagerPolicyEx.WindowStateEx lastFocus, WindowManagerPolicyEx.WindowStateEx newFocus) {
        if (!this.mIsNavStarted) {
            return true;
        }
        if (newFocus == null || newFocus.getAttrs() == null) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(5, FocusWindowState.EMPTY_FOCUS), FOCUS_CHANGED_TIMEOUT);
            return true;
        }
        this.mHandler.removeMessages(5, FocusWindowState.EMPTY_FOCUS);
        HwGestureNavWhiteConfig.getInstance().updateWindow(newFocus);
        String packageName = newFocus.getOwningPackage();
        int uid = newFocus.getOwningUid();
        String focusWindowTitle = newFocus.getAttrs().getTitle().toString();
        boolean isUseNotch = isUsingNotch(newFocus);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(5, new FocusWindowState(packageName, uid, focusWindowTitle, isUseNotch, newFocus.getHwGestureNavOptions())));
        return isUseNotch;
    }

    @Override // com.android.server.gesture.GestureNavPolicy
    public void onLayoutInDisplayCutoutModeChanged(WindowManagerPolicyEx.WindowStateEx win, boolean isOldUsingNotch, boolean isNewUsingNotch) {
        if (this.mIsNavStarted && isDisplayHasNotch()) {
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "oldUN=" + isOldUsingNotch + ", newUN=" + isNewUsingNotch);
            }
            this.mHandler.sendEmptyMessage(12);
        }
    }

    @Override // com.android.server.gesture.GestureNavPolicy
    public void onLockTaskStateChanged(int lockTaskState) {
        if (this.mIsNavStarted) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(13, lockTaskState, 0));
        }
    }

    @Override // com.android.server.gesture.GestureNavPolicy
    public void setGestureNavMode(String packageName, int uid, int leftMode, int rightMode, int bottomMode) {
        if (!this.mIsNavStarted) {
            return;
        }
        if (packageName == null) {
            Log.i(TAG, "packageName is null, return");
            return;
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(11, new AppGestureNavMode(packageName, uid, leftMode, rightMode, bottomMode)));
    }

    @Override // com.android.server.gesture.GestureNavPolicy
    public void updateGestureNavRegion(boolean shrink, int navId) {
        if (this.mIsNavStarted) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(22, shrink ? 1 : 0, navId));
        }
    }

    @Override // com.android.server.gesture.GestureNavPolicy
    public boolean isPointInExcludedRegion(Point point) {
        if (!this.mIsNavStarted || point.y >= this.mExcludedRegionHeight) {
            return false;
        }
        return true;
    }

    @Override // com.android.server.gesture.GestureNavPolicy
    public void notifyANR(CharSequence windowTitle) {
        this.mGestureNavANRCount++;
        if (GestureNavConst.DEBUG) {
            Log.i(TAG, ((Object) windowTitle) + " anr,count:" + this.mGestureNavANRCount);
        }
        if (this.mIsNavStarted && this.mGestureNavANRCount == 2) {
            this.mHandler.sendEmptyMessage(2);
            this.mGestureNavANRCount = 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDeviceStateChanged() {
        synchronized (this.mLock) {
            updateDeviceStateLocked();
            updateNavVisibleLocked();
            if (isShowDockEnabled()) {
                setIsInHomeOfLauncher(this.mHomeWindow != null && this.mHomeWindow.equals(this.mFocusWindowTitle));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleConfigChanged() {
        synchronized (this.mLock) {
            if (this.mDeviceStateController != null) {
                this.mDeviceStateController.onConfigurationChanged();
            }
            updateConfigLocked();
            updateNavWindowLocked();
            updateNavVisibleLocked();
            if (this.mBottomStrategy != null) {
                this.mBottomStrategy.updateScreenConfigState(this.mIsLandscape);
            }
            handleConfigChangedSubScreen();
        }
    }

    private void handleFoldDisplayModeChanged() {
        synchronized (this.mLock) {
            int lastMode = this.mFoldDisplayMode;
            updateFoldDisplayModeLocked();
            boolean isNewModeCoordinate = true;
            boolean isLastModeCoordinate = lastMode == 4;
            if (this.mFoldDisplayMode != 4) {
                isNewModeCoordinate = false;
            }
            if (isLastModeCoordinate != isNewModeCoordinate) {
                updateConfigLocked();
                updateNavWindowLocked();
            }
        }
    }

    private void handleMultiWindowChanged(int state) {
        if (GestureNavConst.SUPPORT_DOCK_TRIGGER) {
            if (this.mDeviceStateController == null) {
                Log.i(TAG, "handleMultiWindowChanged DeviceStateController is null");
                return;
            }
            boolean isNeedChangeFull = false;
            boolean isNeedRefreshConfig = false;
            if (state == 0) {
                isNeedRefreshConfig = isNeedRefreshWinConfig(false, 0);
            } else if (state == 1) {
                isNeedChangeFull = false;
                isNeedRefreshConfig = isNeedRefreshWinConfig(false, 1);
            } else if (state == 2 || state == 3) {
                isNeedChangeFull = isNeedChange2FullHeight(state);
            } else {
                return;
            }
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "handleMultiWindowChanged: isNeedChangeFull=" + isNeedChangeFull);
            }
            if (this.mIsNeedChange2FullHeight == isNeedChangeFull) {
                if (GestureNavConst.DEBUG) {
                    Log.d(TAG, "handleMultiWindowChanged: no need update.");
                }
                if (!isNeedRefreshConfig) {
                    return;
                }
            }
            this.mIsNeedChange2FullHeight = isNeedChangeFull;
            updateConfigLocked();
            updateNavWindowLocked();
            updateNavVisibleLocked();
        }
    }

    private boolean isNeedRefreshWinConfig(boolean isNeedRefreshConfig, int state) {
        if (state != 0) {
            if (state == 1) {
                WindowManagerPolicyEx.WindowStateEx focusWindowState = this.mDeviceStateController.getFocusWindow();
                if (!(!isDisplayHasNotch() || focusWindowState == null || focusWindowState.getAttrs() == null)) {
                    this.mIsFocusWindowUsingNotch = focusWindowState.isWindowUsingNotch();
                }
            }
        } else if (!GestureNavConst.DEFAULT_DOCK_PACKAGE.equals(this.mFocusPackageName)) {
            this.mIsFocusWindowUsingNotch = false;
        }
        return true;
    }

    private void handleUserChanged() {
        StringBuilder sb = new StringBuilder();
        sb.append("handleUserChanged,sDockService=");
        sb.append(GestureNavBaseStrategy.sDockService == null);
        Log.i(TAG, sb.toString());
        GestureNavBaseStrategy gestureNavBaseStrategy = this.mLeftBackStrategy;
        if (gestureNavBaseStrategy != null) {
            gestureNavBaseStrategy.rmvDockDeathRecipient();
        }
        GestureNavBaseStrategy gestureNavBaseStrategy2 = this.mRightBackStrategy;
        if (gestureNavBaseStrategy2 != null) {
            gestureNavBaseStrategy2.rmvDockDeathRecipient();
        }
        GestureNavBaseStrategy.sDockService = null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRotationChanged(int rotation) {
        synchronized (this.mLock) {
            int lastRotation = this.mRotation;
            this.mRotation = rotation;
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "lastRotation=" + lastRotation + ", currentRotation=" + rotation);
            }
            if (isRotationChangedInLand(lastRotation, rotation)) {
                if (GestureNavConst.SUPPORT_DOCK_TRIGGER) {
                    this.mIsNeedChange2FullHeight = isNeedChange2FullHeight(3);
                }
                updateConfigLocked();
                updateNavWindowLocked();
                updateNavVisibleLocked();
                handleRotationChangedSubScreen(rotation);
            } else {
                handleMultiWindowChanged(3);
            }
            if (this.mBottomStrategy != null) {
                this.mBottomStrategy.updateCheckTwiceSettings();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePreferChanged() {
        synchronized (this.mLock) {
            if (this.mDeviceStateController != null) {
                if (updateHomeWindowLocked()) {
                    updateNavVisibleLocked();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotchDisplayChanged() {
        synchronized (this.mLock) {
            if (updateNotchDisplayStateLocked()) {
                updateConfigLocked();
                updateNavWindowLocked();
                updateNavVisibleLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGestureNavTipsChanged() {
        synchronized (this.mLock) {
            if (updateGestureNavTipsStateLocked() && this.mBottomStrategy != null) {
                this.mBottomStrategy.updateNavTipsState(this.mIsGestureNavTipsEnabled);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFocusChanged(FocusWindowState focusWindowState) {
        synchronized (this.mLock) {
            resetAppGestureNavModeLocked();
            this.mFocusAppUid = focusWindowState.uid;
            this.mFocusPackageName = focusWindowState.packageName;
            this.mFocusWindowTitle = focusWindowState.title;
            this.mFocusWinNavOptions = focusWindowState.gestureNavOptions;
            this.mIsInKeyguardMainWindow = isInKeyguardMainWindowLocked();
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "Focus:" + this.mFocusWindowTitle + ", Uid=" + this.mFocusAppUid + ", UN=" + focusWindowState.isUseNotch + ", LUN=" + this.mIsFocusWindowUsingNotch + ", FNO=" + this.mFocusWinNavOptions + ", IKMW=" + this.mIsInKeyguardMainWindow + ", pkg:" + this.mFocusPackageName);
            }
            boolean updateConfig = false;
            if (this.mIsFocusWindowUsingNotch != focusWindowState.isUseNotch) {
                this.mIsFocusWindowUsingNotch = focusWindowState.isUseNotch;
                if (this.mIsLandscape) {
                    updateConfig = true;
                }
            }
            if (updateShrinkStateLocked()) {
                updateConfig = true;
            }
            if (updateConfig) {
                updateConfigLocked();
                updateNavWindowLocked();
            }
            updateNavVisibleLocked(this.mIsLandscape);
            if (isShowDockEnabled()) {
                setIsInHomeOfLauncher(this.mHomeWindow != null && this.mHomeWindow.equals(this.mFocusWindowTitle));
                setFocusOut(this.mFocusPackageName);
            }
            if (this.mBottomStrategy != null) {
                this.mBottomStrategy.updateCheckTwiceSettings();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAppGestureNavMode(AppGestureNavMode appGestureNavMode) {
        synchronized (this.mLock) {
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "AppMode:" + appGestureNavMode);
            }
            if (isShowDockEnabled() && GestureNavConst.isLauncher(appGestureNavMode.packageName)) {
                checkIsHomeActivityOfLauncher(appGestureNavMode);
                if (appGestureNavMode.leftMode == 2 && appGestureNavMode.rightMode == 2) {
                    return;
                }
            }
            if (appGestureNavMode.isFromSameApp(this.mFocusPackageName, this.mFocusAppUid)) {
                this.mAppGestureNavLeftMode = appGestureNavMode.leftMode;
                this.mAppGestureNavRightMode = appGestureNavMode.rightMode;
                this.mAppGestureNavBottomMode = appGestureNavMode.bottomMode;
                updateNavVisibleLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleKeygaurdStateChanged(boolean isShowing) {
        synchronized (this.mLock) {
            if (isShowing) {
                resetAppGestureNavModeLocked();
            }
            this.mIsKeyguardShowing = isShowing;
            this.mIsInKeyguardMainWindow = isInKeyguardMainWindowLocked();
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "keyguard showing=" + isShowing + ", IKMW=" + this.mIsInKeyguardMainWindow);
            }
            updateNavVisibleLocked();
            if (this.mBottomStrategy != null) {
                this.mBottomStrategy.updateKeyguardState(this.mIsKeyguardShowing);
            }
        }
    }

    private void handleDisplayCutoutModeChanged() {
        synchronized (this.mLock) {
            if (this.mDeviceStateController != null) {
                boolean isChanged = false;
                WindowManagerPolicyEx.WindowStateEx focusWindowState = this.mDeviceStateController.getFocusWindow();
                if (!(focusWindowState == null || focusWindowState.getAttrs() == null)) {
                    String windowTitle = focusWindowState.getAttrs().getTitle().toString();
                    int uid = focusWindowState.getOwningUid();
                    if ((this.mFocusWindowTitle == null || this.mFocusWindowTitle.equals(windowTitle)) && this.mFocusAppUid == uid) {
                        boolean isUseNotch = isUsingNotch(focusWindowState);
                        if (isUseNotch != this.mIsFocusWindowUsingNotch) {
                            isChanged = true;
                            this.mIsFocusWindowUsingNotch = isUseNotch;
                        }
                        if (GestureNavConst.DEBUG) {
                            Log.i(TAG, "display cutout mode change:" + isChanged + ", UN:" + this.mIsFocusWindowUsingNotch);
                        }
                    } else {
                        return;
                    }
                }
                if (isChanged) {
                    if (this.mIsLandscape) {
                        updateConfigLocked();
                        updateNavWindowLocked();
                    }
                    updateNavVisibleLocked();
                }
            }
        }
    }

    private void handleLockTaskStateChanged(int lockTaskState) {
        synchronized (this.mLock) {
            if (this.mBottomStrategy != null) {
                this.mBottomStrategy.updateLockTaskState(lockTaskState);
            }
        }
    }

    private void handleNavRegionChanged(boolean shrink, int navId) {
        synchronized (this.mLock) {
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "shrink:" + shrink + ", navId:" + navId);
            }
            if (!shrink || !(navId == 1 || navId == 2)) {
                this.mShrinkNavId = 0;
            } else {
                this.mShrinkNavId = navId;
            }
            if (updateShrinkStateLocked()) {
                updateGestureNavStateLocked();
            }
        }
    }

    private void handleGestureExclusionRegionChanged(Region excludeRegion) {
        synchronized (this.mLock) {
            if (!this.mGlobalExcludeRegion.equals(excludeRegion)) {
                if (GestureNavConst.DEBUG) {
                    Log.i(TAG, "System gesture exclude region:" + excludeRegion);
                }
                this.mGlobalExcludeRegion.set(excludeRegion);
                updateNavExclusionRegionLocked(false);
            }
        }
    }

    private void updateDeviceStateLocked() {
        DeviceStateController deviceStateController = this.mDeviceStateController;
        if (deviceStateController != null) {
            this.mCurrentUserId = deviceStateController.getCurrentUser();
            this.mIsDeviceProvisioned = this.mDeviceStateController.isDeviceProvisioned();
            this.mIsUserSetuped = this.mDeviceStateController.isCurrentUserSetup();
            this.mIsKeyguardShowing = this.mDeviceStateController.isKeyguardShowingOrOccluded();
            this.mIsInKeyguardMainWindow = this.mDeviceStateController.isKeyguardShowingAndNotOccluded();
            this.mRotation = this.mDeviceStateController.getCurrentRotation();
            updateHomeWindowLocked();
            WindowManagerPolicyEx.WindowStateEx focusWindowState = this.mDeviceStateController.getFocusWindow();
            if (!(focusWindowState == null || focusWindowState.getAttrs() == null)) {
                this.mFocusWindowTitle = focusWindowState.getAttrs().getTitle().toString();
                this.mIsFocusWindowUsingNotch = isUsingNotch(focusWindowState);
                this.mFocusPackageName = focusWindowState.getOwningPackage();
                this.mFocusAppUid = focusWindowState.getOwningUid();
                this.mFocusWinNavOptions = focusWindowState.getHwGestureNavOptions();
            }
            this.mShrinkNavId = this.mDeviceStateController.getShrinkIdByDockPosition();
            updateShrinkStateLocked();
            HwGestureNavWhiteConfig.getInstance().updateRotation(this.mRotation);
            HwGestureNavWhiteConfig.getInstance().updateWindow(focusWindowState);
            GestureNavBottomStrategy gestureNavBottomStrategy = this.mBottomStrategy;
            if (gestureNavBottomStrategy != null) {
                gestureNavBottomStrategy.updateKeyguardState(this.mIsKeyguardShowing);
                this.mBottomStrategy.updateScreenConfigState(this.mIsLandscape);
                this.mBottomStrategy.updateNavTipsState(this.mIsGestureNavTipsEnabled);
                this.mBottomStrategy.updateHorizontalSwitch();
                this.mBottomStrategy.updateCheckTwiceSettings();
            }
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "Update device state, provisioned:" + this.mIsDeviceProvisioned + ", userSetup:" + this.mIsUserSetuped + ", KS:" + this.mIsKeyguardShowing + ", IKMW:" + this.mIsInKeyguardMainWindow + ", focus:" + this.mFocusWindowTitle + ", UN:" + this.mIsFocusWindowUsingNotch + ", home:" + this.mHomeWindow);
            }
        }
    }

    private void updateConfigLocked() {
        Rect appendSize;
        boolean isUsingNotch;
        if (this.mHasGestureNavReady) {
            this.mIsLandscape = this.mContext.getResources().getConfiguration().orientation == 2;
            this.mExcludedRegionHeight = GestureNavConst.getStatusBarHeight(this.mContext);
            this.mWindowManager.getDefaultDisplay().getRealSize(this.mDisplaySize);
            int displayWidth = this.mDisplaySize.x;
            int displayHeight = this.mDisplaySize.y;
            int backWindowWidth = GestureNavConst.getBackWindowWidth(this.mContext);
            int leftBackWindowHeight = GestureNavConst.getBackWindowHeight(displayHeight, this.mIsInShrinkState && this.mShrinkNavId == 1, this.mExcludedRegionHeight, this.mIsNeedChange2FullHeight);
            int rightBackWindowHeight = GestureNavConst.getBackWindowHeight(displayHeight, this.mIsInShrinkState && this.mShrinkNavId == 2, this.mExcludedRegionHeight, this.mIsNeedChange2FullHeight);
            int bottomWindowHeight = GestureNavConst.getBottomQuickOutHeight(this.mContext);
            Rect appendSize2 = getAppendWindowSizeLocked();
            Size displaySize = new Size(displayWidth, displayHeight);
            Size leftBackWindowSize = new Size(appendSize2.left + backWindowWidth, leftBackWindowHeight);
            Size rightBackWindowSize = new Size(appendSize2.right + backWindowWidth, rightBackWindowHeight);
            Size bottomWindowSize = new Size(displayWidth, appendSize2.bottom + bottomWindowHeight);
            if (isDisplayHasNotch()) {
                appendSize = appendSize2;
                this.mHoleHeight = HwNotchSizeUtil.getNotchSize()[1];
                if (this.mIsLandscape) {
                    isUsingNotch = !this.mIsNotchDisplayDisabled && this.mIsFocusWindowUsingNotch;
                } else {
                    isUsingNotch = true;
                }
            } else {
                appendSize = appendSize2;
                isUsingNotch = true;
            }
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "w=" + displayWidth + ", h=" + displayHeight + ", leftH=" + leftBackWindowHeight + ", rightH=" + rightBackWindowHeight + ", backW=" + backWindowWidth + ", bottomH=" + bottomWindowHeight + ", usingNotch=" + isUsingNotch + ", holeH=" + this.mHoleHeight);
            }
            updateViewConfigLocked(displaySize, leftBackWindowSize, rightBackWindowSize, bottomWindowSize, isUsingNotch);
        }
    }

    private boolean isNeedChange2FullHeight(int state) {
        boolean isUnFoldedState = HwFoldScreenManager.isFoldable() && HwFoldScreenManager.getFoldableState() != 2;
        int i = this.mRotation;
        boolean isLandscapeMode = i == 1 || i == 3;
        if (GestureNavConst.DEBUG) {
            Log.d(TAG, "isNeedChange2FullHeight: isUnFoldedState=" + isUnFoldedState + "; isLandscapeMode=" + isLandscapeMode);
        }
        if (isUnFoldedState || isLandscapeMode) {
            return false;
        }
        if (state == 0) {
            return true;
        }
        return isHasHwSplitScreen();
    }

    private boolean isHasHwSplitScreen() {
        List<ActivityManager.RunningTaskInfo> visibleTaskInfoList = HwActivityTaskManager.getVisibleTasks();
        if (visibleTaskInfoList == null || visibleTaskInfoList.isEmpty()) {
            return false;
        }
        for (ActivityManager.RunningTaskInfo rti : visibleTaskInfoList) {
            if (rti != null && WindowConfigurationEx.isHwSplitScreenWindowingMode(ActivityManagerExt.getWindowMode(rti))) {
                return true;
            }
        }
        return false;
    }

    private void updateNavWindowLocked() {
        if (this.mIsGestureNavEnabled) {
            if (!this.mIsWindowViewSetuped) {
                createNavWindows();
            } else {
                updateNavWindows();
            }
        } else if (this.mIsWindowViewSetuped) {
            destroyNavWindows();
        }
    }

    private void updateNavVisibleLocked() {
        updateNavVisibleLocked(false);
    }

    private void updateNavVisibleLocked(boolean isDelay) {
        if (this.mIsWindowViewSetuped) {
            boolean isEnableLeftBack = true;
            boolean isEnableRightBack = true;
            boolean isEnableBottom = true;
            if (this.mIsInKeyguardMainWindow) {
                isEnableLeftBack = false;
                isEnableRightBack = false;
                isEnableBottom = false;
            } else {
                if (isFocusWindowLeftBackDisabledLocked()) {
                    isEnableLeftBack = false;
                }
                if (isFocusWindowRightBackDisabledLocked()) {
                    isEnableRightBack = false;
                }
                if (!this.mIsDeviceProvisioned || !this.mIsUserSetuped || isFocusWindowBottomDisabledLocked() || this.mIsKeyNavEnabled) {
                    isEnableBottom = false;
                }
            }
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "updateNavVisible left:" + isEnableLeftBack + ", right:" + isEnableRightBack + ", bottom:" + isEnableBottom);
            }
            enableBackNavLocked(isEnableLeftBack, isEnableRightBack, isDelay);
            enableBottomNavLocked(isEnableBottom, isDelay);
            updateSubScreenNavVisibleLocked(isDelay);
        }
    }

    private void enableBackNavLocked(boolean isEnableLeft, boolean isEnableRight, boolean isDelay) {
        if (this.mIsWindowViewSetuped) {
            boolean isChanged = false;
            if (this.mIsNavLeftBackEnabled != isEnableLeft) {
                this.mGestureNavLeft.show(isEnableLeft, isDelay);
                this.mIsNavLeftBackEnabled = isEnableLeft;
                isChanged = true;
            }
            if (this.mIsNavRightBackEnabled != isEnableRight) {
                this.mGestureNavRight.show(isEnableRight, isDelay);
                this.mIsNavRightBackEnabled = isEnableRight;
                isChanged = true;
            }
            if (isChanged && GestureNavConst.DEBUG) {
                Log.i(TAG, "enableBackNav left:" + this.mIsNavLeftBackEnabled + ", right:" + this.mIsNavRightBackEnabled + ", delay:" + isDelay);
            }
        }
    }

    private void enableBottomNavLocked(boolean isEnable, boolean isDelay) {
        if (this.mIsWindowViewSetuped && this.mIsNavBottomEnabled != isEnable) {
            this.mGestureNavBottom.show(isEnable, false);
            this.mIsNavBottomEnabled = isEnable;
            GestureNavBottomStrategy gestureNavBottomStrategy = this.mBottomStrategy;
            if (gestureNavBottomStrategy != null) {
                gestureNavBottomStrategy.updateBottomVisible(isEnable);
            }
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "enableBottomNav enable:" + this.mIsNavBottomEnabled);
            }
        }
    }

    private void createNavWindows() {
        Log.i(TAG, "createNavWindows");
        this.mGestureNavLeft = new GestureNavView(this.mContext, 1);
        this.mGestureNavRight = new GestureNavView(this.mContext, 2);
        this.mGestureNavBottom = new GestureNavView(this.mContext, 3);
        Looper looper = this.mHandlerThread.getLooper();
        this.mGestureNavAnimProxy = new GestureNavAnimProxy(this.mContext, looper);
        this.mLeftBackStrategy = new GestureNavBackStrategy(1, this.mContext, looper, this.mGestureNavAnimProxy);
        this.mRightBackStrategy = new GestureNavBackStrategy(2, this.mContext, looper, this.mGestureNavAnimProxy);
        this.mBottomStrategy = new GestureNavBottomStrategy(3, this.mContext, looper);
        this.mHasGestureNavReady = true;
        Log.i(TAG, "gesture nav ready.");
        updateConfigLocked();
        configAndAddNavWindow("GestureNavLeft", this.mGestureNavLeft, this.mLeftBackStrategy);
        configAndAddNavWindow("GestureNavRight", this.mGestureNavRight, this.mRightBackStrategy);
        configAndAddNavWindow("GestureNavBottom", this.mGestureNavBottom, this.mBottomStrategy);
        this.mLeftBackStrategy.onNavCreate(this.mGestureNavBottom);
        this.mRightBackStrategy.onNavCreate(this.mGestureNavBottom);
        this.mBottomStrategy.onNavCreate(this.mGestureNavBottom);
        this.mGestureNavAnimProxy.onNavCreate();
        this.mIsWindowViewSetuped = true;
        this.mIsNavLeftBackEnabled = true;
        this.mIsNavRightBackEnabled = true;
        this.mIsNavBottomEnabled = true;
        notifyNavCreatedLocked();
    }

    private void updateNavWindows() {
        if (GestureNavConst.DEBUG) {
            Log.d(TAG, "updateNavWindows");
        }
        reLayoutNavWindow("GestureNavLeft", this.mGestureNavLeft, this.mLeftBackStrategy);
        reLayoutNavWindow("GestureNavRight", this.mGestureNavRight, this.mRightBackStrategy);
        reLayoutNavWindow("GestureNavBottom", this.mGestureNavBottom, this.mBottomStrategy);
        this.mLeftBackStrategy.onNavUpdate();
        this.mRightBackStrategy.onNavUpdate();
        this.mBottomStrategy.onNavUpdate();
        this.mGestureNavAnimProxy.onNavUpdate();
        updateNavBottomExclusionRegionLocked(false);
    }

    private void destroyNavWindows() {
        Log.i(TAG, "destoryNavWindows");
        this.mHasGestureNavReady = false;
        this.mIsWindowViewSetuped = false;
        this.mIsNavLeftBackEnabled = false;
        this.mIsNavRightBackEnabled = false;
        this.mIsNavBottomEnabled = false;
        this.mLeftBackStrategy.onNavDestroy();
        this.mRightBackStrategy.onNavDestroy();
        this.mBottomStrategy.onNavDestroy();
        GestureUtils.removeWindowView(this.mWindowManager, this.mGestureNavLeft, true);
        GestureUtils.removeWindowView(this.mWindowManager, this.mGestureNavRight, true);
        GestureUtils.removeWindowView(this.mWindowManager, this.mGestureNavBottom, true);
        this.mGestureNavAnimProxy.onNavDestroy();
        this.mGestureNavAnimProxy = null;
        this.mLeftBackStrategy = null;
        this.mRightBackStrategy = null;
        this.mBottomStrategy = null;
        this.mGestureNavLeft = null;
        this.mGestureNavRight = null;
        this.mGestureNavBottom = null;
    }

    private WindowManager.LayoutParams createLayoutParams(String title, GestureNavView.WindowConfig config) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(WindowManagerEx.LayoutParamsEx.getTypeNavigationBarPanel(), 296);
        if (ActivityManagerEx.isHighEndGfx()) {
            lp.flags |= 16777216;
        }
        lp.flags |= 512;
        lp.format = -2;
        lp.alpha = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        lp.gravity = 51;
        lp.x = config.startX;
        lp.y = config.startY;
        lp.width = config.width;
        lp.height = config.height;
        lp.windowAnimations = 0;
        lp.softInputMode = 49;
        lp.setTitle(title);
        WindowManagerEx.LayoutParamsEx paramsEx = new WindowManagerEx.LayoutParamsEx(lp);
        if (config.usingNotch) {
            paramsEx.addHwFlags((int) AwarenessConstants.MSDP_ENVIRONMENT_TYPE_HOME);
        } else {
            paramsEx.clearHwFlags((int) AwarenessConstants.MSDP_ENVIRONMENT_TYPE_HOME);
        }
        paramsEx.addHwFlags((int) AwarenessConstants.MSDP_ENVIRONMENT_TYPE_OFFICE);
        paramsEx.addHwFlags(2097152);
        if (!isShowDockEnabled() || !this.mIsKeyNavEnabled) {
            paramsEx.clearHwFlags((int) AwarenessConstants.MSDP_ENVIRONMENT_TYPE_WAY_HOME);
        } else {
            paramsEx.addHwFlags((int) AwarenessConstants.MSDP_ENVIRONMENT_TYPE_WAY_HOME);
        }
        return lp;
    }

    private void configAndAddNavWindow(String title, GestureNavView view, GestureNavBaseStrategy strategy) {
        GestureNavView.WindowConfig config = view.getViewConfig();
        WindowManager.LayoutParams params = createLayoutParams(title, config);
        strategy.updateConfig(config.displayWidth, config.displayHeight, new Rect(config.locationOnScreenX, config.locationOnScreenY, config.locationOnScreenX + config.width, config.locationOnScreenY + config.height), this.mRotation);
        view.setGestureEventProxy(this);
        GestureUtils.addWindowView(this.mWindowManager, view, params);
    }

    private void reLayoutNavWindow(String title, GestureNavView view, GestureNavBaseStrategy strategy) {
        GestureNavView.WindowConfig config = view.getViewConfig();
        WindowManager.LayoutParams params = createLayoutParams(title, config);
        strategy.updateConfig(config.displayWidth, config.displayHeight, new Rect(config.locationOnScreenX, config.locationOnScreenY, config.locationOnScreenX + config.width, config.locationOnScreenY + config.height), this.mRotation);
        GestureUtils.updateViewLayout(this.mWindowManager, view, params);
    }

    private void notifyNavCreatedLocked() {
        this.mBottomStrategy.updateKeyguardState(this.mIsKeyguardShowing);
        this.mBottomStrategy.updateScreenConfigState(this.mIsLandscape);
        this.mBottomStrategy.updateNavTipsState(this.mIsGestureNavTipsEnabled);
        this.mBottomStrategy.updateHorizontalSwitch();
        updateNightModeLocked();
        updateNavExclusionRegionLocked(true);
        updateNavBottomExclusionRegionLocked(true);
    }

    private void updateNavExclusionRegionLocked(boolean isFirst) {
        if (this.mIsWindowViewSetuped) {
            if (isFirst) {
                this.mGestureNavLeft.initTapExcludeCheckConfig(true);
                this.mGestureNavRight.initTapExcludeCheckConfig(true);
            }
            this.mGestureNavLeft.setGlobalExcludeRegion(this.mGlobalExcludeRegion);
            this.mGestureNavRight.setGlobalExcludeRegion(this.mGlobalExcludeRegion);
        }
    }

    private void updateNavBottomExclusionRegionLocked(boolean isFirst) {
        if (this.mIsWindowViewSetuped) {
            if (isFirst) {
                this.mGestureNavBottom.initTapExcludeCheckConfig(true);
            }
            this.mGestureNavBottom.setGlobalExcludeRegion(getBottomGlobalExclusionRegion());
        }
    }

    private Region getBottomGlobalExclusionRegion() {
        Region bottmRegion = new Region();
        int height = GestureNavConst.getBottomQuickOutHeight(this.mContext) - GestureNavConst.getBottomWindowHeight(this.mContext);
        int left = GestureNavConst.getBottomSideWidth(this.mContext);
        int top = this.mGestureNavBottom.getViewConfig().getFrameInDisplay().top;
        bottmRegion.set(left, top, this.mDisplaySize.x - GestureNavConst.getBottomSideWidth(this.mContext), top + height);
        if (GestureNavConst.DEBUG) {
            Log.i(TAG, "Bottom gesture exclude region: " + bottmRegion);
        }
        return bottmRegion;
    }

    private boolean isInKeyguardMainWindowLocked() {
        if (!this.mIsKeyguardShowing) {
            return false;
        }
        DeviceStateController deviceStateController = this.mDeviceStateController;
        if ((deviceStateController == null || deviceStateController.isKeyguardOccluded()) && !GestureNavConst.STATUSBAR_WINDOW.equals(this.mFocusWindowTitle)) {
            return false;
        }
        return true;
    }

    private int getCoordinateOffset() {
        int coordinateOffset = 0;
        if (this.mFoldDisplayMode == 4) {
            coordinateOffset = HwPartCommInterfaceWraper.getFoldScreenFullWidth() - HwPartCommInterfaceWraper.getFoldScreenMainWidth();
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "coordinateOffset = " + coordinateOffset);
            }
        }
        return coordinateOffset;
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0043  */
    private Rect getAppendWindowSizeLocked() {
        Rect appendSize = new Rect(0, 0, 0, 0);
        if (GestureUtils.isCurvedSideDisp()) {
            int gestureNavOffset = GestureNavConst.getGestureCurvedOffset(this.mContext);
            int i = this.mRotation;
            if (i != 0) {
                if (i == 1) {
                    appendSize.bottom = GestureUtils.getCurvedSideLeftDisp() + gestureNavOffset;
                } else if (i != 2) {
                    if (i == 3) {
                        appendSize.bottom = GestureUtils.getCurvedSideRightDisp() + gestureNavOffset;
                    }
                }
                if (GestureNavConst.DEBUG) {
                    Log.i(TAG, "appendSize: " + appendSize);
                }
            }
            appendSize.left = GestureUtils.getCurvedSideLeftDisp() + gestureNavOffset;
            appendSize.right = GestureUtils.getCurvedSideRightDisp() + gestureNavOffset;
            if (GestureNavConst.DEBUG) {
            }
        }
        return appendSize;
    }

    private void updateViewConfigLocked(Size displaySize, Size leftBackWindowSize, Size rightBackWindowSize, Size bottomWindowSize, boolean isUsingNotch) {
        int leftOffset = 0;
        int rightOffset = 0;
        int displayWidth = displaySize.getWidth();
        int dispalyHeight = displaySize.getHeight();
        if (isDisplayHasNotch() && !isUsingNotch) {
            int i = this.mRotation;
            if (i == 1) {
                leftOffset = this.mHoleHeight;
                rightOffset = 0;
            } else if (i == 3) {
                leftOffset = 0;
                rightOffset = this.mHoleHeight;
            }
        }
        int leftOffset2 = leftOffset + getCoordinateOffset();
        int fullScreenViewWidth = (displayWidth - leftOffset2) - rightOffset;
        this.mGestureNavLeft.updateViewConfig(displayWidth, dispalyHeight, leftOffset2, dispalyHeight - leftBackWindowSize.getHeight(), leftBackWindowSize.getWidth(), leftBackWindowSize.getHeight(), leftOffset2, dispalyHeight - leftBackWindowSize.getHeight(), leftOffset2, rightOffset);
        this.mGestureNavRight.updateViewConfig(displayWidth, dispalyHeight, (displayWidth - rightBackWindowSize.getWidth()) - rightOffset, dispalyHeight - rightBackWindowSize.getHeight(), rightBackWindowSize.getWidth(), rightBackWindowSize.getHeight(), (displayWidth - rightBackWindowSize.getWidth()) - rightOffset, dispalyHeight - rightBackWindowSize.getHeight(), leftOffset2, rightOffset);
        this.mGestureNavBottom.updateViewConfig(displayWidth, dispalyHeight, leftOffset2, dispalyHeight - bottomWindowSize.getHeight(), fullScreenViewWidth, bottomWindowSize.getHeight(), leftOffset2, dispalyHeight - bottomWindowSize.getHeight(), leftOffset2, rightOffset);
        this.mGestureNavAnimProxy.updateViewConfig(displayWidth, dispalyHeight, leftOffset2, 0, fullScreenViewWidth, dispalyHeight, leftOffset2, 0, leftOffset2, rightOffset);
        this.mGestureNavLeft.updateViewNotchState(isUsingNotch);
        this.mGestureNavRight.updateViewNotchState(isUsingNotch);
        this.mGestureNavBottom.updateViewNotchState(isUsingNotch);
        this.mGestureNavAnimProxy.updateViewNotchState(isUsingNotch);
    }

    private boolean updateHomeWindowLocked() {
        String homeWindow = this.mDeviceStateController.getCurrentHomeActivity(this.mCurrentUserId);
        if (homeWindow == null || homeWindow.equals(this.mHomeWindow)) {
            return false;
        }
        if (GestureNavConst.DEBUG) {
            Log.i(TAG, "newHome=" + homeWindow + ", oldHome=" + this.mHomeWindow);
        }
        this.mHomeWindow = homeWindow;
        return true;
    }

    private boolean isHomeWindowLocked(String windowName) {
        String str = this.mHomeWindow;
        if (str == null || !str.equals(windowName) || isShowDockEnabled()) {
            return false;
        }
        return true;
    }

    private boolean isFocusWindowLeftBackDisabledLocked() {
        return isFocusWindowBackDisabledLocked(this.mAppGestureNavLeftMode, 4194304);
    }

    private boolean isFocusWindowRightBackDisabledLocked() {
        return isFocusWindowBackDisabledLocked(this.mAppGestureNavRightMode, 8388608);
    }

    private boolean isFocusWindowBackDisabledLocked(int sideMode, int sideDisableOptions) {
        if (sideMode == 1) {
            return false;
        }
        if (sideMode != 2) {
            return isHomeWindowLocked(this.mFocusWindowTitle) || (this.mFocusWinNavOptions & sideDisableOptions) != 0;
        }
        return true;
    }

    private boolean isFocusWindowBottomDisabledLocked() {
        boolean isDisabled;
        int i = this.mAppGestureNavBottomMode;
        boolean z = true;
        if (i == 1) {
            isDisabled = false;
        } else if (i != 2) {
            if ((this.mFocusWinNavOptions & AwarenessConstants.MSDP_ENVIRONMENT_TYPE_WAY_OFFICE) == 0) {
                z = false;
            }
            isDisabled = z;
        } else {
            isDisabled = true;
        }
        if (!isDisabled || isAppCanDisableGesture()) {
            return isDisabled;
        }
        Log.i(TAG, "Permission denied for disabling bottom");
        return false;
    }

    private void resetAppGestureNavModeLocked() {
        this.mAppGestureNavLeftMode = 0;
        this.mAppGestureNavRightMode = 0;
        this.mAppGestureNavBottomMode = 0;
    }

    private boolean isAppCanDisableGesture() {
        return GestureUtils.isSystemOrSignature(this.mContext, this.mFocusPackageName);
    }

    private void handleNightModeChanged() {
        synchronized (this.mLock) {
            updateNightModeLocked();
        }
    }

    private void updateNightModeLocked() {
        boolean z = true;
        if (SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), "ui_night_mode", 1, -2) != 2) {
            z = false;
        }
        this.mIsNightMode = z;
        if (this.mGestureNavAnimProxy != null) {
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "New nightMode=" + this.mIsNightMode);
            }
            this.mGestureNavAnimProxy.setNightMode(this.mIsNightMode);
        }
    }

    private boolean updateNotchDisplayStateLocked() {
        boolean isDisplayNotchStatus = SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), "display_notch_status", 0, -2) == 1;
        if (isDisplayNotchStatus == this.mIsNotchDisplayDisabled) {
            return false;
        }
        if (GestureNavConst.DEBUG) {
            Log.i(TAG, "isDisplayNotchStatus=" + isDisplayNotchStatus + ", oldNotch=" + this.mIsNotchDisplayDisabled);
        }
        this.mIsNotchDisplayDisabled = isDisplayNotchStatus;
        return true;
    }

    private boolean updateShrinkStateLocked() {
        int i;
        boolean shrinkState = GestureNavConst.IS_SUPPORT_FULL_BACK && ((i = this.mShrinkNavId) == 1 || i == 2) && !GestureNavConst.STATUSBAR_WINDOW.equals(this.mFocusWindowTitle);
        if (this.mIsInShrinkState == shrinkState) {
            return false;
        }
        if (GestureNavConst.DEBUG) {
            Log.i(TAG, "newShrink=" + shrinkState + ", oldShrink=" + this.mIsInShrinkState);
        }
        this.mIsInShrinkState = shrinkState;
        return true;
    }

    private boolean updateGestureNavTipsStateLocked() {
        if (false == this.mIsGestureNavTipsEnabled) {
            return false;
        }
        if (GestureNavConst.DEBUG) {
            Log.i(TAG, "newTips=false, oldTips=" + this.mIsGestureNavTipsEnabled);
        }
        this.mIsGestureNavTipsEnabled = false;
        return true;
    }

    private void updateNavBarModeProp(boolean isEnableTips) {
        int oldPropValue = SystemPropertiesEx.getInt("persist.sys.navigationbar.mode", 0);
        int newPropValue = isEnableTips ? oldPropValue | 2 : oldPropValue & -3;
        if (GestureNavConst.DEBUG) {
            Log.i(TAG, "newPropValue=" + newPropValue + ", oldPropValue=" + oldPropValue);
        }
        if (newPropValue != oldPropValue) {
            SystemPropertiesEx.set("persist.sys.navigationbar.mode", String.valueOf(newPropValue));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updateDisplayDensity() {
        String densityStr = SettingsEx.Secure.getStringForUser(this.mContext.getContentResolver(), "display_density_forced", -2);
        if (densityStr == null || densityStr.equals(this.mDensityStr)) {
            return false;
        }
        if (GestureNavConst.DEBUG) {
            Log.i(TAG, "newDensity=" + densityStr + ", oldDensity=" + this.mDensityStr);
        }
        this.mDensityStr = densityStr;
        return true;
    }

    private boolean updateFoldDisplayModeLocked() {
        int foldDisplayMode;
        if (!HwFoldScreenState.isFoldScreenDevice() || this.mFoldDisplayMode == (foldDisplayMode = HwFoldScreenManagerEx.getDisplayMode())) {
            return false;
        }
        if (GestureNavConst.DEBUG) {
            Log.i(TAG, "newFoldDisplayMode:" + foldDisplayMode + ", lastFoldDisplayMode:" + this.mFoldDisplayMode);
        }
        this.mFoldDisplayMode = foldDisplayMode;
        return true;
    }

    private boolean isDisplayHasNotch() {
        if (!this.mHasNotchProp) {
            return false;
        }
        if (!HwFoldScreenState.isFoldScreenDevice() || !HwFoldScreenState.isInwardFoldDevice() || this.mFoldDisplayMode == 2) {
            return true;
        }
        return false;
    }

    private boolean isUsingNotch(WindowManagerPolicyEx.WindowStateEx winEx) {
        if (isDisplayHasNotch()) {
            return isSplitWinUsingNotch(winEx);
        }
        return true;
    }

    private boolean isSplitWinUsingNotch(WindowManagerPolicyEx.WindowStateEx winEx) {
        if (!isShowDockEnabled() || !isHasHwSplitScreen()) {
            return winEx.isWindowUsingNotch();
        }
        return false;
    }

    private boolean isRotationChangedInLand(int lastRotation, int newRotation) {
        if ((lastRotation == 1 && newRotation == 3) || (lastRotation == 3 && newRotation == 1)) {
            return true;
        }
        return false;
    }

    @Override // com.android.server.gesture.GestureNavView.IGestureEventProxy
    public boolean onTouchEvent(GestureNavView view, MotionEvent event) {
        if (view == null) {
            Log.i(TAG, "GestureNavView is NULL, return.");
            return false;
        }
        int navId = view.getNavId();
        if (navId == 1) {
            GestureNavBaseStrategy gestureNavBaseStrategy = this.mLeftBackStrategy;
            if (gestureNavBaseStrategy != null) {
                gestureNavBaseStrategy.onTouchEvent(event, false);
            }
        } else if (navId == 2) {
            GestureNavBaseStrategy gestureNavBaseStrategy2 = this.mRightBackStrategy;
            if (gestureNavBaseStrategy2 != null) {
                gestureNavBaseStrategy2.onTouchEvent(event, false);
            }
        } else if (navId != 3) {
            switch (navId) {
                case 11:
                    GestureNavBaseStrategy gestureNavBaseStrategy3 = this.mLeftBackStrategy;
                    if (gestureNavBaseStrategy3 != null) {
                        gestureNavBaseStrategy3.onTouchEvent(event, true);
                        break;
                    }
                    break;
                case 12:
                    GestureNavBaseStrategy gestureNavBaseStrategy4 = this.mRightBackStrategy;
                    if (gestureNavBaseStrategy4 != null) {
                        gestureNavBaseStrategy4.onTouchEvent(event, true);
                        break;
                    }
                    break;
                case 13:
                    GestureNavBottomStrategy gestureNavBottomStrategy = this.mBottomStrategy;
                    if (gestureNavBottomStrategy != null && !this.mIsKeyNavEnabled) {
                        gestureNavBottomStrategy.onTouchEvent(event, true);
                        break;
                    }
            }
        } else {
            GestureNavBottomStrategy gestureNavBottomStrategy2 = this.mBottomStrategy;
            if (gestureNavBottomStrategy2 != null && !this.mIsKeyNavEnabled) {
                gestureNavBottomStrategy2.onTouchEvent(event, false);
            }
        }
        return true;
    }

    @Override // com.android.server.gesture.GestureNavPolicy
    public void dump(String dumpPrefix, PrintWriter pw, String[] args) {
        if (GestureNavConst.DEBUG_DUMP && this.mIsGestureNavEnabled) {
            pw.println(dumpPrefix + TAG);
            String prefix = dumpPrefix + "  ";
            printDump(prefix, pw);
            GestureDataTracker gestureDataTracker = this.mGestureDataTracker;
            if (gestureDataTracker != null) {
                gestureDataTracker.dump(prefix, pw, args);
            }
            DumpUtilsEx.dumpAsync(this.mHandler, new DumpUtilsEx.DumpEx() {
                /* class com.android.server.gesture.GestureNavManager.AnonymousClass6 */

                public void dump(PrintWriter pw, String prefix) {
                    if (GestureNavManager.this.mHasGestureNavReady && GestureNavManager.this.mIsWindowViewSetuped && GestureNavManager.this.mGestureNavLeft != null) {
                        GestureNavManager.this.mGestureNavLeft.dump(prefix, pw);
                    }
                    if (GestureNavManager.this.mHasGestureNavReady && GestureNavManager.this.mIsWindowViewSetuped && GestureNavManager.this.mGestureNavRight != null) {
                        GestureNavManager.this.mGestureNavRight.dump(prefix, pw);
                    }
                    if (GestureNavManager.this.mHasGestureNavReady && GestureNavManager.this.mIsWindowViewSetuped && GestureNavManager.this.mGestureNavBottom != null) {
                        GestureNavManager.this.mGestureNavBottom.dump(prefix, pw);
                    }
                    if (GestureNavManager.this.mHasGestureNavReady && GestureNavManager.this.mIsWindowViewSetuped && GestureNavManager.this.mBottomStrategy != null) {
                        GestureNavManager.this.mBottomStrategy.dump(prefix, pw, null);
                    }
                }
            }, pw, prefix, 200);
        }
    }

    private void printDump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("mIsGestureNavTipsEnabled=" + this.mIsGestureNavTipsEnabled);
        pw.print(" mCurrentUserId=" + this.mCurrentUserId);
        pw.println();
        pw.print(prefix);
        pw.print("mIsDeviceProvisioned=" + this.mIsDeviceProvisioned);
        pw.print(" mIsUserSetuped=" + this.mIsUserSetuped);
        pw.print(" mIsWindowViewSetuped=" + this.mIsWindowViewSetuped);
        pw.println();
        pw.print(prefix);
        pw.print("mIsKeyguardShowing=" + this.mIsKeyguardShowing);
        pw.print(" mIsInKeyguardMainWindow=" + this.mIsInKeyguardMainWindow);
        pw.print(" mIsNotchDisplayDisabled=" + this.mIsNotchDisplayDisabled);
        pw.println();
        pw.print(prefix);
        pw.println("mHomeWindow=" + this.mHomeWindow);
        pw.print(prefix);
        pw.println("mFocusWindowTitle=" + this.mFocusWindowTitle);
        pw.print(prefix);
        pw.print("mIsFocusWindowUsingNotch=" + this.mIsFocusWindowUsingNotch);
        pw.print(" mFocusWinNavOptions=0x" + Integer.toHexString(this.mFocusWinNavOptions));
        pw.print(" mFocusAppUid=" + this.mFocusAppUid);
        pw.println();
        pw.print(prefix);
        pw.print("mAppGestureNavLeftMode=" + this.mAppGestureNavLeftMode);
        pw.print(" mAppGestureNavRightMode=" + this.mAppGestureNavRightMode);
        pw.print(" mAppGestureNavBottomMode=" + this.mAppGestureNavBottomMode);
        pw.println();
        pw.print(prefix);
        pw.print("mIsNavLeftBackEnabled=" + this.mIsNavLeftBackEnabled);
        pw.print(" mIsNavRightBackEnabled=" + this.mIsNavRightBackEnabled);
        pw.print(" mIsNavBottomEnabled=" + this.mIsNavBottomEnabled);
        pw.println();
        pw.print(prefix);
        pw.print("mRotation=" + this.mRotation);
        pw.print(" mIsLandscape=" + this.mIsLandscape);
        pw.print(" mDensityStr=" + this.mDensityStr);
        pw.print(" mFoldDisplayMode=" + this.mFoldDisplayMode);
        if (this.mHasNotchProp) {
            pw.print(" mHasNotchProp=" + this.mHasNotchProp);
        }
        pw.println();
        pw.print(prefix);
        pw.print("mExcludedRegionHeight=" + this.mExcludedRegionHeight);
        pw.print(" mIsInShrinkState=" + this.mIsInShrinkState);
        pw.print(" mShrinkNavId=" + this.mShrinkNavId);
        pw.println();
        pw.print(prefix);
        pw.print("mGlobalExcludeRegion=" + this.mGlobalExcludeRegion);
        pw.println();
    }

    @Override // com.android.server.gesture.GestureNavPolicy
    public void destroySubScreenNavView() {
        this.mIsSubScreenEnableGestureNav = false;
        if (this.mIsNavStarted) {
            this.mHandler.sendEmptyMessage(16);
        }
    }

    @Override // com.android.server.gesture.GestureNavPolicy
    public void initSubScreenNavView() {
        this.mIsSubScreenEnableGestureNav = true;
        if (this.mIsNavStarted) {
            this.mHandler.sendEmptyMessage(15);
        }
    }

    @Override // com.android.server.gesture.GestureNavPolicy
    public void bringTopSubScreenNavView() {
        if (this.mIsSubScreenEnableGestureNav && this.mIsNavStarted) {
            this.mHandler.sendEmptyMessage(17);
        }
    }

    private void handleSubScreenCreateNavView() {
        if (this.mIsSubScreenEnableGestureNav && this.mIsGestureNavEnabled) {
            GestureNavSubScreenManager gestureNavSubScreenManager = this.mSubScreenGestureNavManager;
            if (gestureNavSubScreenManager != null) {
                gestureNavSubScreenManager.destroySubScreenNavWindows();
                this.mSubScreenGestureNavManager = null;
            }
            Context context = this.mContext;
            int i = this.mRotation;
            boolean z = this.mIsNavLeftBackEnabled;
            this.mSubScreenGestureNavManager = new GestureNavSubScreenManager(context, i, z, z, this.mIsNavBottomEnabled);
            this.mSubScreenGestureNavManager.setGestureEventProxy(this);
        }
    }

    private void handleSubScreenDestoryNavView() {
        GestureNavSubScreenManager gestureNavSubScreenManager = this.mSubScreenGestureNavManager;
        if (gestureNavSubScreenManager != null) {
            gestureNavSubScreenManager.destroySubScreenNavWindows();
            this.mSubScreenGestureNavManager = null;
        }
    }

    private void handleSubScreenBringTopNavView() {
        GestureNavSubScreenManager gestureNavSubScreenManager = this.mSubScreenGestureNavManager;
        if (gestureNavSubScreenManager != null) {
            if (!this.mIsSubScreenEnableGestureNav || !this.mIsGestureNavEnabled) {
                handleSubScreenDestoryNavView();
            } else {
                gestureNavSubScreenManager.bringSubScreenNavViewToTop();
            }
        }
    }

    private void handleRotationChangedSubScreen(int rotation) {
        GestureNavSubScreenManager gestureNavSubScreenManager = this.mSubScreenGestureNavManager;
        if (gestureNavSubScreenManager != null) {
            if (!this.mIsSubScreenEnableGestureNav || !this.mIsGestureNavEnabled) {
                handleSubScreenDestoryNavView();
            } else {
                gestureNavSubScreenManager.handleRotationChangedSubScreen(rotation);
            }
        }
    }

    private void handleConfigChangedSubScreen() {
        GestureNavSubScreenManager gestureNavSubScreenManager = this.mSubScreenGestureNavManager;
        if (gestureNavSubScreenManager != null) {
            if (!this.mIsSubScreenEnableGestureNav || !this.mIsGestureNavEnabled) {
                handleSubScreenDestoryNavView();
            } else {
                gestureNavSubScreenManager.handleConfigChangedSubScreen(this.mRotation);
            }
        }
    }

    private void updateSubScreenNavVisibleLocked(boolean isDelay) {
        GestureNavSubScreenManager gestureNavSubScreenManager = this.mSubScreenGestureNavManager;
        if (gestureNavSubScreenManager != null) {
            if (!this.mIsSubScreenEnableGestureNav || !this.mIsGestureNavEnabled) {
                handleSubScreenDestoryNavView();
            } else {
                gestureNavSubScreenManager.updateSubScreenNavVisibleLocked(this.mIsNavLeftBackEnabled, this.mIsNavRightBackEnabled, this.mIsNavBottomEnabled, this.mRotation, isDelay);
            }
        }
    }

    private void checkIsHomeActivityOfLauncher(AppGestureNavMode appGestureNavMode) {
        if (GestureNavConst.isLauncher(this.mFocusPackageName)) {
            if (appGestureNavMode.leftMode == 1 && appGestureNavMode.rightMode == 1) {
                setIsInHomeOfLauncher(false);
            } else if (appGestureNavMode.leftMode == 2 && appGestureNavMode.rightMode == 2) {
                setIsInHomeOfLauncher(true);
            }
        }
    }

    private void setIsInHomeOfLauncher(boolean status) {
        GestureNavBaseStrategy gestureNavBaseStrategy = this.mLeftBackStrategy;
        if (gestureNavBaseStrategy != null) {
            gestureNavBaseStrategy.mIsInHomeOfLauncher = status;
        }
        GestureNavBaseStrategy gestureNavBaseStrategy2 = this.mRightBackStrategy;
        if (gestureNavBaseStrategy2 != null) {
            gestureNavBaseStrategy2.mIsInHomeOfLauncher = status;
        }
    }

    private void setFocusOut(String packageName) {
        GestureNavBaseStrategy gestureNavBaseStrategy = this.mLeftBackStrategy;
        if (gestureNavBaseStrategy != null && (gestureNavBaseStrategy instanceof GestureNavBackStrategy)) {
            gestureNavBaseStrategy.focusOut(packageName);
        }
        GestureNavBaseStrategy gestureNavBaseStrategy2 = this.mRightBackStrategy;
        if (gestureNavBaseStrategy2 != null && (gestureNavBaseStrategy2 instanceof GestureNavBackStrategy)) {
            gestureNavBaseStrategy2.focusOut(packageName);
        }
    }
}
