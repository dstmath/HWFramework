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
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import com.android.internal.util.DumpUtils;
import com.android.server.Watchdog;
import com.android.server.gesture.DeviceStateController;
import com.android.server.gesture.GestureNavView;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.policy.WindowManagerPolicy;
import java.io.PrintWriter;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsCompModeID;

public class GestureNavManager implements GestureNavPolicy, GestureNavView.IGestureEventProxy, Watchdog.Monitor {
    private static final int MSG_CONFIG_CHANGED = 3;
    private static final int MSG_DEVICE_STATE_CHANGED = 4;
    private static final int MSG_DISPLAY_CUTOUT_MODE_CHANGED = 12;
    private static final int MSG_FOCUS_CHANGED = 5;
    private static final int MSG_GESTURE_NAV_TIPS_CHANGED = 10;
    private static final int MSG_KEYGUARD_STATE_CHANGED = 6;
    private static final int MSG_NOTCH_DISPLAY_CHANGED = 9;
    private static final int MSG_PREFER_CHANGED = 8;
    private static final int MSG_RELOAD_NAV_GLOBAL_STATE = 2;
    private static final int MSG_ROTATION_CHANGED = 7;
    private static final int MSG_SET_GESTURE_NAV_MODE = 11;
    private static final int MSG_UPDATE_NAV_GLOBAL_STATE = 1;
    private static final String TAG = "GestureNavManager";
    private int mAppGestureNavBottomMode;
    private int mAppGestureNavLeftMode;
    private int mAppGestureNavRightMode;
    /* access modifiers changed from: private */
    public GestureNavBottomStrategy mBottomStrategy;
    private Context mContext;
    private int mCurrentUserId;
    private DensityObserver mDensityObserver;
    private String mDensityStr;
    private final DeviceStateController.DeviceChangedListener mDeviceChangedCallback = new DeviceStateController.DeviceChangedListener() {
        public void onDeviceProvisionedChanged(boolean provisioned) {
            if (GestureNavManager.this.mNavStarted) {
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavManager.TAG, "Device provisioned changed, provisioned=" + provisioned);
                }
                GestureNavManager.this.mHandler.sendEmptyMessage(4);
            }
        }

        public void onUserSwitched(int newUserId) {
            if (GestureNavManager.this.mNavStarted) {
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavManager.TAG, "User switched, newUserId=" + newUserId);
                }
                GestureNavManager.this.mHandler.sendEmptyMessage(4);
            }
        }

        public void onUserSetupChanged(boolean setup) {
            if (GestureNavManager.this.mNavStarted) {
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavManager.TAG, "User setup changed, setup=" + setup);
                }
                GestureNavManager.this.mHandler.sendEmptyMessage(4);
            }
        }

        public void onPreferredActivityChanged(boolean isPrefer) {
            if (GestureNavManager.this.mNavStarted) {
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavManager.TAG, "Preferred activity changed, isPrefer=" + isPrefer);
                }
                GestureNavManager.this.mHandler.sendEmptyMessage(8);
            }
        }
    };
    private boolean mDeviceProvisioned;
    private DeviceStateController mDeviceStateController;
    private Point mDisplaySize = new Point();
    private int mFocusAppUid;
    private String mFocusPackageName;
    private int mFocusWinNavOptions;
    private String mFocusWindowTitle;
    private boolean mFocusWindowUsingNotch = true;
    private GestureDataTracker mGestureDataTracker;
    private GestureNavAnimProxy mGestureNavAnimProxy;
    /* access modifiers changed from: private */
    public GestureNavView mGestureNavBottom;
    private boolean mGestureNavEnabled;
    /* access modifiers changed from: private */
    public GestureNavView mGestureNavLeft;
    /* access modifiers changed from: private */
    public boolean mGestureNavReady;
    /* access modifiers changed from: private */
    public GestureNavView mGestureNavRight;
    private boolean mGestureNavTipsEnabled;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mHasNotch;
    private int mHoleHeight = 0;
    private String mHomeWindow;
    private boolean mInKeyguardMainWindow;
    private boolean mKeyguardShowing;
    private boolean mLandscape;
    private LauncherStateChangedReceiver mLauncherStateChangedReceiver;
    private GestureNavBaseStrategy mLeftBackStrategy;
    private final Object mLock = new Object();
    private boolean mNavBottomEnabled;
    private boolean mNavLeftBackEnabled;
    private boolean mNavRightBackEnabled;
    /* access modifiers changed from: private */
    public boolean mNavStarted;
    private boolean mNotchDisplayDisabled;
    private NotchObserver mNotchObserver;
    private GestureNavBaseStrategy mRightBackStrategy;
    private int mRotation = GestureNavConst.DEFAULT_ROTATION;
    private boolean mUserSetuped;
    private WindowManager mWindowManager;
    /* access modifiers changed from: private */
    public boolean mWindowViewSetuped;

    private static final class AppGestureNavMode {
        public int bottomMode;
        public int leftMode;
        public String packageName;
        public int rightMode;
        public int uid;

        public AppGestureNavMode(String _packageName, int _uid, int _leftMode, int _rightMode, int _bottomMode) {
            this.packageName = _packageName;
            this.uid = _uid;
            this.leftMode = _leftMode;
            this.rightMode = _rightMode;
            this.bottomMode = _bottomMode;
        }

        public boolean isFromSameApp(String _packageName, int _uid) {
            return this.packageName != null && this.packageName.equals(_packageName) && this.uid == _uid;
        }

        public String toString() {
            return "pkg:" + this.packageName + ", uid:" + this.uid + ", left:" + this.leftMode + ", right:" + this.rightMode + ", bottom:" + this.bottomMode;
        }
    }

    private final class DensityObserver extends ContentObserver {
        public DensityObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (GestureNavManager.this.mNavStarted && GestureNavManager.this.updateDisplayDensity()) {
                GestureNavManager.this.mHandler.sendEmptyMessage(2);
            }
        }
    }

    private static final class FocusWindowState {
        public int gestureNavOptions;
        public String packageName;
        public String title;
        public int uid;
        public boolean usingNotch;

        public FocusWindowState(String _packageName, int _uid, String _title, boolean _usingNotch, int _gestureNavOptions) {
            this.packageName = _packageName;
            this.uid = _uid;
            this.title = _title;
            this.usingNotch = _usingNotch;
            this.gestureNavOptions = _gestureNavOptions;
        }
    }

    private final class GestureHandler extends Handler {
        public GestureHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (GestureNavConst.DEBUG) {
                Log.d(GestureNavManager.TAG, "handleMessage before msg=" + msg.what);
            }
            switch (msg.what) {
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
                case 12:
                    GestureNavManager.this.handleDisplayCutoutModeChanged();
                    break;
            }
            if (GestureNavConst.DEBUG) {
                Log.d(GestureNavManager.TAG, "handleMessage after msg=" + msg.what);
            }
        }
    }

    private final class GestureNavTipsObserver extends ContentObserver {
        public GestureNavTipsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (GestureNavManager.this.mNavStarted) {
                GestureNavManager.this.mHandler.sendEmptyMessage(10);
            }
        }
    }

    private final class LauncherStateChangedReceiver extends BroadcastReceiver {
        private LauncherStateChangedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (GestureNavManager.this.mNavStarted) {
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavManager.TAG, "Launcher state changed, intent=" + intent);
                }
                GestureNavManager.this.mHandler.sendEmptyMessage(8);
            }
        }
    }

    private final class NotchObserver extends ContentObserver {
        public NotchObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (GestureNavManager.this.mNavStarted) {
                GestureNavManager.this.mHandler.sendEmptyMessage(9);
            }
        }
    }

    public GestureNavManager(Context context) {
        this.mContext = context;
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new GestureHandler(this.mHandlerThread.getLooper());
    }

    public void monitor() {
        synchronized (this.mLock) {
        }
    }

    public void systemReady() {
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        Watchdog.getInstance().addMonitor(this);
        Watchdog.getInstance().addThread(this.mHandler);
        GestureUtils.systemReady();
        this.mHasNotch = GestureUtils.hasNotch();
        this.mGestureDataTracker = GestureDataTracker.getInstance(this.mContext);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(GestureNavConst.KEY_SECURE_GESTURE_NAVIGATION), false, new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                Log.i(GestureNavManager.TAG, "gesture nav status change");
                GestureNavManager.this.mHandler.sendEmptyMessage(1);
            }
        }, -1);
        this.mHandler.sendEmptyMessage(1);
    }

    private void updateEnableStateLocked() {
        this.mGestureNavEnabled = GestureNavConst.isGestureNavEnabled(this.mContext, -2);
        Log.i(TAG, "GestureNavEnabled=" + this.mGestureNavEnabled + ", tipsEnabled=" + this.mGestureNavTipsEnabled);
    }

    /* access modifiers changed from: private */
    public void updateGestureNavGlobalState() {
        synchronized (this.mLock) {
            updateEnableStateLocked();
            if (this.mGestureNavEnabled) {
                startGestureNavLocked();
            } else {
                stopGestureNavLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public void reloadGestureNavGlobalState() {
        synchronized (this.mLock) {
            Log.i(TAG, "force reloadGestureNavGlobalState");
            this.mGestureNavEnabled = false;
            stopGestureNavLocked();
            updateEnableStateLocked();
            if (this.mGestureNavEnabled) {
                startGestureNavLocked();
            }
        }
    }

    private void startGestureNavLocked() {
        Log.i(TAG, "startGestureNavLocked");
        this.mNavStarted = true;
        resetAppGestureNavModeLocked();
        this.mGestureDataTracker.checkStartTrackerIfNeed();
        updateDisplayDensity();
        updateNotchDisplayStateLocked();
        this.mDeviceStateController = DeviceStateController.getInstance(this.mContext);
        this.mDeviceStateController.addCallback(this.mDeviceChangedCallback);
        updateGestureNavStateLocked();
        ContentResolver resolver = this.mContext.getContentResolver();
        this.mDensityObserver = new DensityObserver(this.mHandler);
        resolver.registerContentObserver(Settings.Secure.getUriFor("display_density_forced"), false, this.mDensityObserver, -1);
        this.mNotchObserver = new NotchObserver(this.mHandler);
        resolver.registerContentObserver(Settings.Secure.getUriFor("display_notch_status"), false, this.mNotchObserver, -1);
        this.mLauncherStateChangedReceiver = new LauncherStateChangedReceiver();
        IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
        filter.addDataScheme("package");
        filter.addDataSchemeSpecificPart(GestureNavConst.DEFAULT_LAUNCHER_PACKAGE, 0);
        filter.addAction("android.intent.action.PACKAGE_CHANGED");
        this.mContext.registerReceiverAsUser(this.mLauncherStateChangedReceiver, UserHandle.ALL, filter, null, this.mHandler);
    }

    private void stopGestureNavLocked() {
        Log.i(TAG, "stopGestureNavLocked");
        if (this.mLauncherStateChangedReceiver != null) {
            this.mContext.unregisterReceiver(this.mLauncherStateChangedReceiver);
            this.mLauncherStateChangedReceiver = null;
        }
        ContentResolver resolver = this.mContext.getContentResolver();
        if (this.mDensityObserver != null) {
            resolver.unregisterContentObserver(this.mDensityObserver);
            this.mDensityObserver = null;
        }
        if (this.mNotchObserver != null) {
            resolver.unregisterContentObserver(this.mNotchObserver);
            this.mNotchObserver = null;
        }
        updateGestureNavStateLocked();
        if (this.mDeviceStateController != null) {
            this.mDeviceStateController.removeCallback(this.mDeviceChangedCallback);
            this.mDeviceStateController = null;
        }
        this.mNavStarted = false;
    }

    private void updateGestureNavStateLocked() {
        updateDeviceStateLocked();
        updateConfigLocked();
        updateNavWindowLocked();
        updateNavVisibleLocked();
    }

    public boolean isGestureNavStartedNotLocked() {
        return this.mNavStarted;
    }

    public void onUserChanged(int newUserId) {
        if (GestureNavConst.DEBUG) {
            Log.i(TAG, "User switched then reInit, newUserId=" + newUserId);
        }
        this.mHandler.sendEmptyMessage(2);
    }

    public void onConfigurationChanged() {
        if (this.mNavStarted) {
            if (GestureNavConst.DEBUG) {
                Log.d(TAG, "onConfigurationChanged");
            }
            this.mHandler.sendEmptyMessage(3);
        }
    }

    public void onRotationChanged(int rotation) {
        if (this.mNavStarted) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(7, rotation, 0));
        }
    }

    public void onKeyguardShowingChanged(boolean showing) {
        if (this.mNavStarted) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(6, showing, 0), 300);
        }
    }

    public boolean onFocusWindowChanged(WindowManagerPolicy.WindowState lastFocus, WindowManagerPolicy.WindowState newFocus) {
        WindowManagerPolicy.WindowState windowState = newFocus;
        if (!this.mNavStarted || windowState == null || newFocus.getAttrs() == null) {
            return true;
        }
        String packageName = newFocus.getOwningPackage();
        int uid = newFocus.getOwningUid();
        String focusWindowTitle = newFocus.getAttrs().getTitle().toString();
        boolean usingNotch = isUsingNotch(windowState);
        int focusWindowNavOptions = newFocus.getHwGestureNavOptions();
        Handler handler = this.mHandler;
        FocusWindowState focusWindowState = new FocusWindowState(packageName, uid, focusWindowTitle, usingNotch, focusWindowNavOptions);
        this.mHandler.sendMessage(handler.obtainMessage(5, focusWindowState));
        return usingNotch;
    }

    public void onLayoutInDisplayCutoutModeChanged(WindowManagerPolicy.WindowState win, boolean oldUsingNotch, boolean usingUsingNotch) {
        if (this.mNavStarted && this.mHasNotch) {
            if (GestureNavConst.DEBUG) {
                Log.d(TAG, "oldUN=" + oldUsingNotch + ", newUN=" + usingUsingNotch);
            }
            this.mHandler.sendEmptyMessage(12);
        }
    }

    public void setGestureNavMode(String packageName, int uid, int leftMode, int rightMode, int bottomMode) {
        if (this.mNavStarted) {
            if (packageName == null) {
                Log.i(TAG, "packageName is null, return");
                return;
            }
            Handler handler = this.mHandler;
            AppGestureNavMode appGestureNavMode = new AppGestureNavMode(packageName, uid, leftMode, rightMode, bottomMode);
            this.mHandler.sendMessage(handler.obtainMessage(11, appGestureNavMode));
        }
    }

    /* access modifiers changed from: private */
    public void handleDeviceStateChanged() {
        synchronized (this.mLock) {
            updateDeviceStateLocked();
            updateNavVisibleLocked();
        }
    }

    /* access modifiers changed from: private */
    public void handleConfigChanged() {
        synchronized (this.mLock) {
            if (this.mDeviceStateController != null) {
                this.mDeviceStateController.onConfigurationChanged();
            }
            updateConfigLocked();
            updateNavWindowLocked();
            updateNavVisibleLocked();
            if (this.mBottomStrategy != null) {
                this.mBottomStrategy.updateScreenConfigState(this.mLandscape);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleRotationChanged(int rotation) {
        synchronized (this.mLock) {
            int lastRotation = this.mRotation;
            this.mRotation = rotation;
            if (GestureNavConst.DEBUG) {
                Log.d(TAG, "lastRotation=" + lastRotation + ", currentRotation=" + rotation);
            }
            if (isRotationChangedInLand(lastRotation, rotation)) {
                updateConfigLocked();
                updateNavWindowLocked();
                updateNavVisibleLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0013, code lost:
        return;
     */
    public void handlePreferChanged() {
        synchronized (this.mLock) {
            if (this.mDeviceStateController != null) {
                if (updateHomeWindowLocked()) {
                    updateNavVisibleLocked();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleNotchDisplayChanged() {
        synchronized (this.mLock) {
            if (updateNotchDisplayStateLocked()) {
                updateConfigLocked();
                updateNavWindowLocked();
                updateNavVisibleLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleGestureNavTipsChanged() {
        synchronized (this.mLock) {
            if (updateGestureNavTipsStateLocked() && this.mBottomStrategy != null) {
                this.mBottomStrategy.updateNavTipsState(this.mGestureNavTipsEnabled);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleFocusChanged(FocusWindowState focusWindowState) {
        synchronized (this.mLock) {
            resetAppGestureNavModeLocked();
            this.mFocusAppUid = focusWindowState.uid;
            this.mFocusPackageName = focusWindowState.packageName;
            this.mFocusWindowTitle = focusWindowState.title;
            this.mFocusWinNavOptions = focusWindowState.gestureNavOptions;
            this.mInKeyguardMainWindow = isInKeyguardMainWindowLocked();
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "Focus:" + this.mFocusWindowTitle + ", Uid=" + this.mFocusAppUid + ", UN=" + focusWindowState.usingNotch + ", LUN=" + this.mFocusWindowUsingNotch + ", FNO=" + this.mFocusWinNavOptions + ", IKMW=" + this.mInKeyguardMainWindow + ", pkg:" + this.mFocusPackageName);
            }
            if (this.mFocusWindowUsingNotch != focusWindowState.usingNotch) {
                this.mFocusWindowUsingNotch = focusWindowState.usingNotch;
                if (this.mLandscape) {
                    updateConfigLocked();
                    updateNavWindowLocked();
                }
            }
            updateNavVisibleLocked(this.mLandscape);
        }
    }

    /* access modifiers changed from: private */
    public void handleAppGestureNavMode(AppGestureNavMode appGestureNavMode) {
        synchronized (this.mLock) {
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "AppMode:" + appGestureNavMode);
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
    public void handleKeygaurdStateChanged(boolean showing) {
        synchronized (this.mLock) {
            if (showing) {
                try {
                    resetAppGestureNavModeLocked();
                } catch (Throwable th) {
                    throw th;
                }
            }
            this.mKeyguardShowing = showing;
            this.mInKeyguardMainWindow = isInKeyguardMainWindowLocked();
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "keyguard showing=" + showing + ", IKMW=" + this.mInKeyguardMainWindow);
            }
            updateNavVisibleLocked();
            if (this.mBottomStrategy != null) {
                this.mBottomStrategy.updateKeyguardState(this.mKeyguardShowing);
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0039, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0079, code lost:
        return;
     */
    public void handleDisplayCutoutModeChanged() {
        synchronized (this.mLock) {
            if (this.mDeviceStateController != null) {
                boolean changed = false;
                WindowManagerPolicy.WindowState focusWindowState = this.mDeviceStateController.getFocusWindow();
                if (!(focusWindowState == null || focusWindowState.getAttrs() == null)) {
                    String windowTitle = focusWindowState.getAttrs().getTitle().toString();
                    int uid = focusWindowState.getOwningUid();
                    if ((this.mFocusWindowTitle == null || this.mFocusWindowTitle.equals(windowTitle)) && this.mFocusAppUid == uid) {
                        boolean usingNotch = isUsingNotch(focusWindowState);
                        if (usingNotch != this.mFocusWindowUsingNotch) {
                            changed = true;
                            this.mFocusWindowUsingNotch = usingNotch;
                        }
                        if (GestureNavConst.DEBUG) {
                            Log.i(TAG, "display cutout mode change:" + changed + ", UN:" + this.mFocusWindowUsingNotch);
                        }
                    }
                }
                if (changed) {
                    if (this.mLandscape) {
                        updateConfigLocked();
                        updateNavWindowLocked();
                    }
                    updateNavVisibleLocked();
                }
            }
        }
    }

    private void updateDeviceStateLocked() {
        if (this.mDeviceStateController != null) {
            this.mCurrentUserId = this.mDeviceStateController.getCurrentUser();
            this.mDeviceProvisioned = this.mDeviceStateController.isDeviceProvisioned();
            this.mUserSetuped = this.mDeviceStateController.isCurrentUserSetup();
            this.mKeyguardShowing = this.mDeviceStateController.isKeyguardShowingOrOccluded();
            this.mInKeyguardMainWindow = this.mDeviceStateController.isKeyguardShowingAndNotOccluded();
            this.mRotation = this.mDeviceStateController.getCurrentRotation();
            updateHomeWindowLocked();
            WindowManagerPolicy.WindowState focusWindowState = this.mDeviceStateController.getFocusWindow();
            if (!(focusWindowState == null || focusWindowState.getAttrs() == null)) {
                this.mFocusWindowTitle = focusWindowState.getAttrs().getTitle().toString();
                this.mFocusWindowUsingNotch = isUsingNotch(focusWindowState);
                this.mFocusPackageName = focusWindowState.getOwningPackage();
                this.mFocusAppUid = focusWindowState.getOwningUid();
                this.mFocusWinNavOptions = focusWindowState.getHwGestureNavOptions();
            }
            if (this.mBottomStrategy != null) {
                this.mBottomStrategy.updateKeyguardState(this.mKeyguardShowing);
                this.mBottomStrategy.updateScreenConfigState(this.mLandscape);
                this.mBottomStrategy.updateNavTipsState(this.mGestureNavTipsEnabled);
            }
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "Update device state, provisioned:" + this.mDeviceProvisioned + ", userSetup:" + this.mUserSetuped + ", KS:" + this.mKeyguardShowing + ", IKMW:" + this.mInKeyguardMainWindow + ", focus:" + this.mFocusWindowTitle + ", UN:" + this.mFocusWindowUsingNotch + ", home:" + this.mHomeWindow);
            }
        }
    }

    private void updateConfigLocked() {
        if (this.mGestureNavReady) {
            boolean z = false;
            this.mLandscape = this.mContext.getResources().getConfiguration().orientation == 2;
            this.mWindowManager.getDefaultDisplay().getRealSize(this.mDisplaySize);
            int displayWidth = this.mDisplaySize.x;
            int displayHeight = this.mDisplaySize.y;
            int backWindowHeight = Math.round(((float) displayHeight) * 0.75f);
            int backWindowWidth = GestureNavConst.getBackWindowWidth(this.mContext);
            int bottomWindowHeight = GestureNavConst.getBottomWindowHeight(this.mContext);
            boolean usingNotch = true;
            if (this.mHasNotch) {
                this.mHoleHeight = GestureNavConst.getStatusBarHeight(this.mContext);
                if (this.mLandscape) {
                    if (!this.mNotchDisplayDisabled && this.mFocusWindowUsingNotch) {
                        z = true;
                    }
                    usingNotch = z;
                } else {
                    usingNotch = true;
                }
                this.mGestureNavLeft.updateViewNotchState(usingNotch);
                this.mGestureNavRight.updateViewNotchState(usingNotch);
                this.mGestureNavAnimProxy.updateViewNotchState(usingNotch);
            }
            boolean usingNotch2 = usingNotch;
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "w=" + displayWidth + ", h=" + displayHeight + ", backH=" + backWindowHeight + ", backW=" + backWindowWidth + ", bottomH=" + bottomWindowHeight + ", usingNotch=" + usingNotch2 + ", holeH=" + this.mHoleHeight);
            }
            updateViewConfigLocked(displayWidth, displayHeight, backWindowWidth, backWindowHeight, bottomWindowHeight, usingNotch2);
        }
    }

    private void updateNavWindowLocked() {
        if (this.mGestureNavEnabled) {
            if (!this.mWindowViewSetuped) {
                createNavWindows();
            } else {
                updateNavWindows();
            }
        } else if (this.mWindowViewSetuped) {
            destroyNavWindows();
        }
    }

    private void updateNavVisibleLocked() {
        updateNavVisibleLocked(false);
    }

    private void updateNavVisibleLocked(boolean delay) {
        if (this.mWindowViewSetuped) {
            boolean enableLeftBack = true;
            boolean enableRightBack = true;
            boolean enableBottom = true;
            if (this.mInKeyguardMainWindow) {
                enableLeftBack = false;
                enableRightBack = false;
                enableBottom = false;
            } else {
                if (isFocusWindowLeftBackDisabledLocked()) {
                    enableLeftBack = false;
                }
                if (isFocusWindowRightBackDisabledLocked()) {
                    enableRightBack = false;
                }
                if (!this.mDeviceProvisioned || !this.mUserSetuped || isFocusWindowBottomDisabledLocked()) {
                    enableBottom = false;
                }
            }
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "updateNavVisible left:" + enableLeftBack + ", right:" + enableRightBack + ", bottom:" + enableBottom);
            }
            enableBackNavLocked(enableLeftBack, enableRightBack, delay);
            enableBottomNavLocked(enableBottom, delay);
        }
    }

    private void enableBackNavLocked(boolean enableLeft, boolean enableRight, boolean delay) {
        if (this.mWindowViewSetuped) {
            boolean changed = false;
            if (this.mNavLeftBackEnabled != enableLeft) {
                this.mGestureNavLeft.show(enableLeft, delay);
                this.mNavLeftBackEnabled = enableLeft;
                changed = true;
            }
            if (this.mNavRightBackEnabled != enableRight) {
                this.mGestureNavRight.show(enableRight, delay);
                this.mNavRightBackEnabled = enableRight;
                changed = true;
            }
            if (changed && GestureNavConst.DEBUG) {
                Log.i(TAG, "enableBackNav left:" + this.mNavLeftBackEnabled + ", right:" + this.mNavRightBackEnabled + ", delay:" + delay);
            }
        }
    }

    private void enableBottomNavLocked(boolean enable, boolean delay) {
        if (this.mWindowViewSetuped && this.mNavBottomEnabled != enable) {
            this.mGestureNavBottom.show(enable, false);
            this.mNavBottomEnabled = enable;
            if (GestureNavConst.DEBUG) {
                Log.i(TAG, "enableBottomNav enable:" + this.mNavBottomEnabled);
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
        this.mGestureNavReady = true;
        Log.i(TAG, "gesture nav ready.");
        updateConfigLocked();
        configAndAddNavWindow("GestureNavLeft", this.mGestureNavLeft, this.mLeftBackStrategy);
        configAndAddNavWindow("GestureNavRight", this.mGestureNavRight, this.mRightBackStrategy);
        configAndAddNavWindow("GestureNavBottom", this.mGestureNavBottom, this.mBottomStrategy);
        this.mLeftBackStrategy.onNavCreate(this.mGestureNavBottom);
        this.mRightBackStrategy.onNavCreate(this.mGestureNavBottom);
        this.mBottomStrategy.onNavCreate(this.mGestureNavBottom);
        this.mGestureNavAnimProxy.onNavCreate();
        this.mBottomStrategy.updateKeyguardState(this.mKeyguardShowing);
        this.mBottomStrategy.updateScreenConfigState(this.mLandscape);
        this.mBottomStrategy.updateNavTipsState(this.mGestureNavTipsEnabled);
        this.mWindowViewSetuped = true;
        this.mNavLeftBackEnabled = true;
        this.mNavRightBackEnabled = true;
        this.mNavBottomEnabled = true;
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
    }

    private void destroyNavWindows() {
        Log.i(TAG, "destoryNavWindows");
        this.mGestureNavReady = false;
        this.mWindowViewSetuped = false;
        this.mNavLeftBackEnabled = false;
        this.mNavRightBackEnabled = false;
        this.mNavBottomEnabled = false;
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
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(HwArbitrationDEFS.MSG_VPN_STATE_OPEN, 296);
        if (ActivityManager.isHighEndGfx()) {
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
        if (config.usingNotch) {
            lp.hwFlags |= 65536;
        } else {
            lp.hwFlags &= -65537;
        }
        lp.hwFlags |= 131072;
        lp.hwFlags |= HighBitsCompModeID.MODE_EYE_PROTECT;
        return lp;
    }

    private void configAndAddNavWindow(String title, GestureNavView view, GestureNavBaseStrategy strategy) {
        GestureNavView.WindowConfig config = view.getViewConfig();
        WindowManager.LayoutParams params = createLayoutParams(title, config);
        strategy.updateConfig(config.displayWidth, config.displayHeight, new Rect(config.locationOnScreenX, config.locationOnScreenY, config.locationOnScreenX + config.width, config.locationOnScreenY + config.height));
        view.setGestureEventProxy(this);
        GestureUtils.addWindowView(this.mWindowManager, view, params);
    }

    private void reLayoutNavWindow(String title, GestureNavView view, GestureNavBaseStrategy strategy) {
        GestureNavView.WindowConfig config = view.getViewConfig();
        WindowManager.LayoutParams params = createLayoutParams(title, config);
        strategy.updateConfig(config.displayWidth, config.displayHeight, new Rect(config.locationOnScreenX, config.locationOnScreenY, config.locationOnScreenX + config.width, config.locationOnScreenY + config.height));
        GestureUtils.updateViewLayout(this.mWindowManager, view, params);
    }

    private boolean isInKeyguardMainWindowLocked() {
        if (!this.mKeyguardShowing || ((this.mDeviceStateController == null || this.mDeviceStateController.isKeyguardOccluded()) && !GestureNavConst.STATUSBAR_WINDOW.equals(this.mFocusWindowTitle))) {
            return false;
        }
        return true;
    }

    private void updateViewConfigLocked(int displayWidth, int displayHeight, int backWindowWidth, int backWindowHeight, int bottomWindowHeight, boolean usingNotch) {
        int leftViewStartPos = 0;
        int rightViewOffset = 0;
        int viewWidth = displayWidth;
        if (this.mHasNotch && !usingNotch) {
            int i = this.mRotation;
            if (i == 1) {
                leftViewStartPos = this.mHoleHeight;
                viewWidth = displayWidth - this.mHoleHeight;
                rightViewOffset = 0;
            } else if (i == 3) {
                leftViewStartPos = 0;
                viewWidth = displayWidth - this.mHoleHeight;
                rightViewOffset = this.mHoleHeight;
            }
        }
        int leftViewStartPos2 = leftViewStartPos;
        int rightViewOffset2 = rightViewOffset;
        int viewWidth2 = viewWidth;
        int startX = leftViewStartPos2;
        this.mGestureNavLeft.updateViewConfig(displayWidth, displayHeight, startX, displayHeight - backWindowHeight, backWindowWidth, backWindowHeight, leftViewStartPos2, displayHeight - backWindowHeight);
        int i2 = displayWidth;
        int i3 = displayHeight;
        this.mGestureNavRight.updateViewConfig(i2, i3, (displayWidth - backWindowWidth) - rightViewOffset2, displayHeight - backWindowHeight, backWindowWidth, backWindowHeight, (displayWidth - backWindowWidth) - rightViewOffset2, displayHeight - backWindowHeight);
        int i4 = startX;
        int i5 = viewWidth2;
        int i6 = leftViewStartPos2;
        this.mGestureNavBottom.updateViewConfig(i2, i3, i4, displayHeight - bottomWindowHeight, i5, bottomWindowHeight, i6, displayHeight - bottomWindowHeight);
        this.mGestureNavAnimProxy.updateViewConfig(i2, i3, i4, 0, i5, displayHeight, i6, 0);
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
        if (this.mHomeWindow == null || !this.mHomeWindow.equals(windowName)) {
            return false;
        }
        return true;
    }

    private boolean isFocusWindowLeftBackDisabledLocked() {
        return isFocusWindowBackDisabledLocked(this.mAppGestureNavLeftMode, 4456448);
    }

    private boolean isFocusWindowRightBackDisabledLocked() {
        return isFocusWindowBackDisabledLocked(this.mAppGestureNavRightMode, 8650752);
    }

    private boolean isFocusWindowBackDisabledLocked(int sideMode, int sideDisableOptions) {
        boolean z = false;
        switch (sideMode) {
            case 1:
                return false;
            case 2:
                return true;
            default:
                if (isHomeWindowLocked(this.mFocusWindowTitle) || (this.mFocusWinNavOptions & sideDisableOptions) != 0) {
                    z = true;
                }
                return z;
        }
    }

    private boolean isFocusWindowBottomDisabledLocked() {
        boolean disable;
        switch (this.mAppGestureNavBottomMode) {
            case 1:
                disable = false;
                break;
            case 2:
                disable = true;
                break;
            default:
                disable = (this.mFocusWinNavOptions & 524288) != 0;
                break;
        }
        if (!disable || isAppCanDisableGesture()) {
            return disable;
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
        return GestureUtils.isSystemApp(this.mContext, this.mFocusPackageName);
    }

    private boolean updateNotchDisplayStateLocked() {
        boolean notchStatus = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "display_notch_status", 0, -2) == 1;
        if (notchStatus == this.mNotchDisplayDisabled) {
            return false;
        }
        if (GestureNavConst.DEBUG) {
            Log.i(TAG, "newNotch=" + notchStatus + ", oldNotch=" + this.mNotchDisplayDisabled);
        }
        this.mNotchDisplayDisabled = notchStatus;
        return true;
    }

    private boolean updateGestureNavTipsStateLocked() {
        if (false == this.mGestureNavTipsEnabled) {
            return false;
        }
        if (GestureNavConst.DEBUG) {
            Log.i(TAG, "newTips=" + false + ", oldTips=" + this.mGestureNavTipsEnabled);
        }
        this.mGestureNavTipsEnabled = false;
        return true;
    }

    private void updateNavBarModeProp(boolean enableTips) {
        int oldPropValue = SystemProperties.getInt("persist.sys.navigationbar.mode", 0);
        int newPropValue = enableTips ? oldPropValue | 2 : oldPropValue & -3;
        if (GestureNavConst.DEBUG) {
            Log.i(TAG, "newPropValue=" + newPropValue + ", oldPropValue=" + oldPropValue);
        }
        if (newPropValue != oldPropValue) {
            SystemProperties.set("persist.sys.navigationbar.mode", String.valueOf(newPropValue));
        }
    }

    /* access modifiers changed from: private */
    public boolean updateDisplayDensity() {
        String densityStr = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "display_density_forced", -2);
        if (densityStr == null || densityStr.equals(this.mDensityStr)) {
            return false;
        }
        if (GestureNavConst.DEBUG) {
            Log.i(TAG, "newDensity=" + densityStr + ", oldDensity=" + this.mDensityStr);
        }
        this.mDensityStr = densityStr;
        return true;
    }

    private boolean isUsingNotch(WindowManagerPolicy.WindowState win) {
        if (this.mHasNotch) {
            return win.isWindowUsingNotch();
        }
        return true;
    }

    private boolean isRotationChangedInLand(int lastRotation, int newRotation) {
        if ((lastRotation == 1 && newRotation == 3) || (lastRotation == 3 && newRotation == 1)) {
            return true;
        }
        return false;
    }

    public boolean onTouchEvent(GestureNavView view, MotionEvent event) {
        switch (view.getNavId()) {
            case 1:
                if (this.mLeftBackStrategy != null) {
                    this.mLeftBackStrategy.onTouchEvent(event);
                    break;
                }
                break;
            case 2:
                if (this.mRightBackStrategy != null) {
                    this.mRightBackStrategy.onTouchEvent(event);
                    break;
                }
                break;
            case 3:
                if (this.mBottomStrategy != null) {
                    this.mBottomStrategy.onTouchEvent(event);
                    break;
                }
                break;
        }
        return true;
    }

    public void dump(String prefix, PrintWriter pw, String[] args) {
        if (GestureNavConst.DEBUG_DUMP && this.mGestureNavEnabled) {
            pw.println(prefix + TAG);
            String prefix2 = prefix + "  ";
            pw.print(prefix2);
            pw.print("mGestureNavTipsEnabled=" + this.mGestureNavTipsEnabled);
            pw.print(" mCurrentUserId=" + this.mCurrentUserId);
            pw.println();
            pw.print(prefix2);
            pw.print("mDeviceProvisioned=" + this.mDeviceProvisioned);
            pw.print(" mUserSetuped=" + this.mUserSetuped);
            pw.println();
            pw.print(prefix2);
            pw.print("mKeyguardShowing=" + this.mKeyguardShowing);
            pw.print(" mInKeyguardMainWindow=" + this.mInKeyguardMainWindow);
            pw.print(" mNotchDisplayDisabled=" + this.mNotchDisplayDisabled);
            pw.println();
            pw.print(prefix2);
            pw.println("mHomeWindow=" + this.mHomeWindow);
            pw.print(prefix2);
            pw.println("mFocusWindowTitle=" + this.mFocusWindowTitle);
            pw.print(prefix2);
            pw.print("mFocusWindowUsingNotch=" + this.mFocusWindowUsingNotch);
            pw.print(" mFocusWinNavOptions=0x" + Integer.toHexString(this.mFocusWinNavOptions));
            pw.print(" mFocusAppUid=" + this.mFocusAppUid);
            pw.println();
            pw.print(prefix2);
            pw.print("mAppGestureNavLeftMode=" + this.mAppGestureNavLeftMode);
            pw.print(" mAppGestureNavRightMode=" + this.mAppGestureNavRightMode);
            pw.print(" mAppGestureNavBottomMode=" + this.mAppGestureNavBottomMode);
            pw.println();
            pw.print(prefix2);
            pw.print("mWindowViewSetuped=" + this.mWindowViewSetuped);
            pw.print(" mNavLeftBackEnabled=" + this.mNavLeftBackEnabled);
            pw.print(" mNavRightBackEnabled=" + this.mNavRightBackEnabled);
            pw.print(" mNavBottomEnabled=" + this.mNavBottomEnabled);
            pw.println();
            pw.print(prefix2);
            pw.print("mRotation=" + this.mRotation);
            pw.print(" mLandscape=" + this.mLandscape);
            pw.print(" mDensityStr=" + this.mDensityStr);
            if (this.mHasNotch) {
                pw.print(" mHasNotch=" + this.mHasNotch);
            }
            pw.println();
            if (this.mGestureDataTracker != null) {
                this.mGestureDataTracker.dump(prefix2, pw, args);
            }
            DumpUtils.dumpAsync(this.mHandler, new DumpUtils.Dump() {
                public void dump(PrintWriter pw, String prefix) {
                    if (GestureNavManager.this.mGestureNavReady && GestureNavManager.this.mWindowViewSetuped && GestureNavManager.this.mGestureNavLeft != null) {
                        pw.print(prefix);
                        pw.println("GestureNavLeft=" + GestureNavManager.this.mGestureNavLeft.getViewConfig());
                    }
                    if (GestureNavManager.this.mGestureNavReady && GestureNavManager.this.mWindowViewSetuped && GestureNavManager.this.mGestureNavRight != null) {
                        pw.print(prefix);
                        pw.println("GestureNavRight=" + GestureNavManager.this.mGestureNavRight.getViewConfig());
                    }
                    if (GestureNavManager.this.mGestureNavReady && GestureNavManager.this.mWindowViewSetuped && GestureNavManager.this.mGestureNavBottom != null) {
                        pw.print(prefix);
                        pw.println("GestureNavBottom=" + GestureNavManager.this.mGestureNavBottom.getViewConfig());
                    }
                    if (GestureNavManager.this.mGestureNavReady && GestureNavManager.this.mWindowViewSetuped && GestureNavManager.this.mBottomStrategy != null) {
                        GestureNavManager.this.mBottomStrategy.dump(prefix, pw, null);
                    }
                }
            }, pw, prefix2, 200);
        }
    }
}
