package com.android.server.policy;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.input.InputManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Slog;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.InputMonitor;
import android.view.MotionEvent;
import android.widget.Toast;
import com.android.server.gesture.DefaultDeviceStateController;
import com.android.server.gesture.DefaultGestureNavConst;
import com.android.server.gesture.DefaultGestureUtils;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.policy.WindowManagerPolicyEx;
import com.huawei.android.fsm.HwFoldScreenManager;
import com.huawei.controlcenter.ui.service.IControlCenterGesture;
import com.huawei.server.HwBasicPlatformFactory;

public class NavigationCallOut {
    private static final String ACCESSIBILITY_SCREENREADER_ENABLED = "accessibility_screenreader_enabled";
    private static final String AUTHORITY = "com.huawei.controlcenter.SwitchProvider";
    private static final Uri AUTHORITY_URI = Uri.parse("content://com.huawei.controlcenter.SwitchProvider");
    private static final String CTRLCENTER_ACTION = "com.huawei.controlcenter.action.CONTROL_CENTER_GESTURE";
    private static final String CTRLCENTER_PKG = "com.huawei.controlcenter";
    private static final int DEGREE_RAGE = 1;
    private static final float GESTURE_SLIDE_OUT_DISTANCE_RATIO = 0.5f;
    private static final String GET_PANEL_STATE = "getPanelState";
    private static final String ISCONTROLCENTERENABLE = "isControlCenterEnable";
    private static final String ISSWITCHON = "isSwitchOn";
    private static final boolean IS_DEBUG = false;
    private static final boolean IS_FOLD_SCREEN_DEVICE = HwFoldScreenManager.isFoldable();
    private static final String IS_NOTIFICATIONS_PANEL_EXPAND = "isNotificationsPanelExpand";
    private static final String KEY_ENABLE_NAVBAR_DB = "enable_navbar";
    private static final double LEFT_RANGE = 0.125d;
    private static final double LEFT_RANGE_LAND = 0.0833333358168602d;
    private static final String METHOD_CHECK_CONTROL_CENTER_ENABLE = "getControlCenterState";
    private static final String METHOD_CHECK_SWITCH = "checkCtrlPanelSwitch";
    private static final double NAV_START_PART = 0.28d;
    private static final int NOTCH_HEIGHT = 1;
    private static final int NOTCH_NUMS = 4;
    private static final double RIGHT_RANGE = 0.875d;
    private static final double RIGHT_RANGE_LAND = 0.9166666865348816d;
    private static final String SYSTEMUI_INTERACTION = "com.android.systemui.remote";
    private static final String TAG = "NavigationCallOut";
    private static final String TALKBACK_CONFIG = "ro.config.hw_talkback_btn";
    private static final String URI_CONTROLCENTER_SWITCH = "content://com.huawei.controlcenter.SwitchProvider/ctrlPanelSwitch";
    private int mAreaHeight;
    private Context mContext = null;
    private final ServiceConnection mCtrlCenterConn;
    private IControlCenterGesture mCtrlCenterIntf;
    private WindowManagerPolicy.WindowState mFocusWin;
    private Handler mHandler;
    private InputEventReceiver mInputEventReceiver;
    private InputMonitor mInputMonitor;
    private boolean mIsControlCenterOn;
    private boolean mIsCtrlCenterInstalled;
    private boolean mIsFirstGetControlCenter;
    private boolean mIsFirstStarted;
    private boolean mIsFromLeft;
    private boolean mIsGestureOn;
    private boolean mIsHwHasNaviBar;
    private boolean mIsInSpecialMode;
    private boolean mIsInTaskLockMode;
    private boolean mIsNeedStart;
    private boolean mIsNeedUpdate;
    private boolean mIsRegistered;
    private boolean mIsStartCtrlCenter;
    private boolean mIsValidGesture;
    private int mLandAreaHeight;
    private long mLastAftGestureTime;
    private Rect mLeftRect;
    private final Object mLock = new Object();
    private Looper mLooper;
    private PhoneWindowManager mPolicy = null;
    private final Uri mProviderUri = Uri.parse("content://com.android.systemui.remote");
    private Rect mRightRect;
    private SettingsObserver mSettingsObserver;
    private float mShortDistance;
    private int mSlideStartThreshold;
    private final BroadcastReceiver mUserSwitchedReceiver;
    private Point realSize;
    private MotionEvent startEvent;

    public NavigationCallOut(Context context, PhoneWindowManager policy, Looper gestureLooper) {
        boolean z = false;
        this.mAreaHeight = 0;
        this.mLandAreaHeight = 0;
        this.mIsHwHasNaviBar = false;
        this.mIsValidGesture = false;
        this.realSize = new Point();
        this.mCtrlCenterIntf = null;
        this.mIsControlCenterOn = false;
        this.mIsFirstGetControlCenter = false;
        this.mLeftRect = new Rect();
        this.mRightRect = new Rect();
        this.mIsRegistered = false;
        this.mIsCtrlCenterInstalled = false;
        this.mIsNeedUpdate = false;
        this.mIsNeedStart = false;
        this.mIsStartCtrlCenter = false;
        this.startEvent = null;
        this.mIsFromLeft = false;
        this.mIsInTaskLockMode = false;
        this.mIsInSpecialMode = false;
        this.mIsFirstStarted = false;
        this.mShortDistance = 0.0f;
        this.mUserSwitchedReceiver = new BroadcastReceiver() {
            /* class com.android.server.policy.NavigationCallOut.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (context != null && intent != null) {
                    NavigationCallOut.this.updateSettings();
                    NavigationCallOut.this.updateHotArea();
                }
            }
        };
        this.mCtrlCenterConn = new ServiceConnection() {
            /* class com.android.server.policy.NavigationCallOut.AnonymousClass2 */

            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName name, IBinder service) {
                NavigationCallOut.this.mCtrlCenterIntf = IControlCenterGesture.Stub.asInterface(service);
                if (NavigationCallOut.this.mCtrlCenterIntf == null) {
                    Slog.e(NavigationCallOut.TAG, "mCtrlCenterIntf null object");
                } else if (NavigationCallOut.this.mIsNeedStart) {
                    NavigationCallOut navigationCallOut = NavigationCallOut.this;
                    navigationCallOut.startCtrlCenter(navigationCallOut.mIsFromLeft);
                    NavigationCallOut.this.mIsNeedStart = false;
                } else if (NavigationCallOut.this.mIsValidGesture) {
                    NavigationCallOut navigationCallOut2 = NavigationCallOut.this;
                    navigationCallOut2.preloadCtrlCenter(navigationCallOut2.mIsFromLeft);
                }
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName name) {
                NavigationCallOut.this.mCtrlCenterIntf = null;
            }
        };
        this.mContext = context;
        this.mLooper = gestureLooper != null ? gestureLooper : Looper.getMainLooper();
        this.mHandler = new Handler(this.mLooper);
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        ContentResolver resolver = this.mContext.getContentResolver();
        resolver.registerContentObserver(Settings.Secure.getUriFor(DefaultGestureNavConst.KEY_SECURE_GESTURE_NAVIGATION), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Uri.parse(URI_CONTROLCENTER_SWITCH), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.Secure.getUriFor(KEY_ENABLE_NAVBAR_DB), false, this.mSettingsObserver, -1);
        this.mPolicy = policy;
        updateRealSize();
        changeArea();
        this.mIsGestureOn = Settings.Secure.getInt(this.mContext.getContentResolver(), DefaultGestureNavConst.KEY_SECURE_GESTURE_NAVIGATION, 0) > 0;
        this.mIsHwHasNaviBar = Settings.System.getInt(this.mContext.getContentResolver(), KEY_ENABLE_NAVBAR_DB, 0) > 0 ? true : z;
        IntentFilter filter = new IntentFilter();
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_ACTION_USER_SWITCHED);
        this.mContext.registerReceiverAsUser(this.mUserSwitchedReceiver, UserHandle.ALL, filter, null, this.mHandler);
        if (!this.mIsGestureOn && this.mIsHwHasNaviBar) {
            registerPointer();
        }
    }

    private void registerPointer() {
        if (!this.mIsRegistered || this.mInputMonitor == null) {
            this.mInputMonitor = InputManager.getInstance().monitorGestureInput("hiplay-swipe", 0);
            this.mInputEventReceiver = new NavEventReceiver(this.mInputMonitor.getInputChannel(), this.mLooper);
            this.mIsRegistered = true;
        }
    }

    private void unregisterPointer() {
        if (this.mIsRegistered) {
            InputEventReceiver inputEventReceiver = this.mInputEventReceiver;
            if (inputEventReceiver != null) {
                inputEventReceiver.dispose();
                this.mInputEventReceiver = null;
            }
            InputMonitor inputMonitor = this.mInputMonitor;
            if (inputMonitor != null) {
                inputMonitor.dispose();
                this.mInputMonitor = null;
            }
            this.mIsRegistered = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addPointerEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == 0) {
            handleDown(event);
        } else if (action == 1) {
            handActionUp(this.startEvent, event);
        } else if (action == 2) {
            handleMove(this.startEvent, event);
        } else if (action == 3) {
            handleCancel();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reset() {
        this.mIsValidGesture = false;
        this.startEvent = null;
    }

    private boolean touchDownIsValid(float pointX, float pointY) {
        if (getDisplay() == null || (this.mPolicy.mKeyguardDelegate.isShowing() && !this.mPolicy.mKeyguardDelegate.isOccluded())) {
            return false;
        }
        if (!this.mIsFirstGetControlCenter) {
            this.mIsCtrlCenterInstalled = isCtrlCenterInstalled();
            this.mIsControlCenterOn = isControlCenterSwitchOn() && this.mIsCtrlCenterInstalled;
            this.mIsFirstGetControlCenter = true;
            changeArea();
        }
        if (this.mIsNeedUpdate) {
            changeArea();
        }
        if (this.mLeftRect.contains((int) pointX, (int) pointY)) {
            this.mIsFromLeft = true;
        } else if (!this.mRightRect.contains((int) pointX, (int) pointY)) {
            return false;
        } else {
            this.mIsFromLeft = false;
        }
        if (!this.mIsControlCenterOn) {
            unregisterPointer();
            return false;
        } else if (isNotificationsPanelExpand() || canDisableGesture()) {
            return false;
        } else {
            Slog.d(TAG, "touchDownIsValid");
            if (shouldStartGesture()) {
                bindCtrlCenter();
            }
            return true;
        }
    }

    private boolean isNotificationsPanelExpand() {
        try {
            Bundle bundle = this.mContext.getContentResolver().call(this.mProviderUri, GET_PANEL_STATE, (String) null, (Bundle) null);
            if (bundle == null) {
                return false;
            }
            boolean isExpand = bundle.getBoolean(IS_NOTIFICATIONS_PANEL_EXPAND);
            Slog.d(TAG, "isExpand:" + isExpand);
            return isExpand;
        } catch (IllegalArgumentException e) {
            Slog.d(TAG, "IllegalArgumentException error");
            return false;
        } catch (Exception e2) {
            Slog.d(TAG, "Exception error");
            return false;
        }
    }

    private boolean shouldStartGesture() {
        this.mIsInSpecialMode = shouldCheckAftForThisGesture();
        if (this.mIsInSpecialMode) {
            this.mIsFirstStarted = SystemClock.uptimeMillis() - this.mLastAftGestureTime <= 2500;
        }
        return (this.mIsInSpecialMode && this.mIsFirstStarted) || !this.mIsInSpecialMode;
    }

    private void updateRealSize() {
        if (getDisplay() != null) {
            getDisplay().getRealSize(this.realSize);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Display getDisplay() {
        return this.mContext.getDisplay();
    }

    private int getNavigationBarPosition() {
        if (this.mPolicy.mDefaultDisplayPolicy != null) {
            return this.mPolicy.mDefaultDisplayPolicy.getNavBarPosition();
        }
        PhoneWindowManager phoneWindowManager = this.mPolicy;
        return 4;
    }

    private void handleDown(MotionEvent event) {
        this.mIsValidGesture = touchDownIsValid(event.getRawX(), event.getRawY());
        if (this.mIsValidGesture) {
            this.startEvent = event.copy();
        }
    }

    private void handleMove(MotionEvent e1, MotionEvent e2) {
        if (!this.mIsValidGesture || this.startEvent == null) {
            dismissCtrlCenter();
            return;
        }
        float startX = e1.getX();
        float startY = e1.getY();
        float endX = e2.getX();
        float endY = e2.getY();
        float deltX = Math.max(Math.abs(endX - startX), 1.0f);
        float deltY = Math.max(Math.abs(endY - startY), 1.0f);
        if (deltX / deltY > 1.0f || endY > startY) {
            reset();
        } else if (((this.mIsInSpecialMode && this.mIsFirstStarted) || !this.mIsInSpecialMode) && deltY > this.mShortDistance) {
            if (!this.mIsStartCtrlCenter) {
                preloadCtrlCenter(this.mIsFromLeft);
                this.mInputMonitor.pilferPointers();
                return;
            }
            moveCtrlCenter(((float) this.realSize.y) - endY);
        }
    }

    private void handActionUp(MotionEvent e1, MotionEvent e2) {
        if (!this.mIsValidGesture || this.startEvent == null) {
            dismissCtrlCenter();
            return;
        }
        float startX = e1.getX();
        float startY = e1.getY();
        float endX = e2.getX();
        float endY = e2.getY();
        if (Math.max(Math.abs(endX - startX), 1.0f) / Math.max(Math.abs(endY - startY), 1.0f) <= 1.0f && Math.abs(startY - endY) > ((float) this.mSlideStartThreshold)) {
            if ((!this.mIsInSpecialMode || !this.mIsFirstStarted) && this.mIsInSpecialMode) {
                showReTryToast();
                this.mIsFirstStarted = true;
                this.mLastAftGestureTime = SystemClock.uptimeMillis();
            } else {
                locateCtrlCenter();
            }
            Slog.i(TAG, "start activity");
        }
        dismissCtrlCenter();
        reset();
    }

    private void handleCancel() {
        reset();
        dismissCtrlCenter();
    }

    private final class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            NavigationCallOut.this.updateSettings();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSettings() {
        synchronized (this.mLock) {
            boolean z = true;
            this.mIsGestureOn = Settings.Secure.getInt(this.mContext.getContentResolver(), DefaultGestureNavConst.KEY_SECURE_GESTURE_NAVIGATION, 0) > 0;
            this.mIsCtrlCenterInstalled = isCtrlCenterInstalled();
            this.mIsControlCenterOn = isControlCenterSwitchOn() && this.mIsCtrlCenterInstalled;
            if (Settings.System.getInt(this.mContext.getContentResolver(), KEY_ENABLE_NAVBAR_DB, 0) <= 0) {
                z = false;
            }
            this.mIsHwHasNaviBar = z;
            if (!this.mIsHwHasNaviBar || this.mIsGestureOn || !this.mIsControlCenterOn) {
                unregisterPointer();
            } else {
                registerPointer();
            }
            reset();
            Slog.e(TAG, "mIsControlCenterOn" + this.mIsControlCenterOn + this.mIsHwHasNaviBar + this.mIsGestureOn);
        }
    }

    private void bindCtrlCenter() {
        if (this.mCtrlCenterIntf == null) {
            Intent intent = new Intent(CTRLCENTER_ACTION);
            intent.setPackage(CTRLCENTER_PKG);
            try {
                Context context = this.mContext;
                ServiceConnection serviceConnection = this.mCtrlCenterConn;
                Context context2 = this.mContext;
                context.bindServiceAsUser(intent, serviceConnection, 1, UserHandle.of(ActivityManager.getCurrentUser()));
            } catch (SecurityException e) {
                Slog.i(TAG, "bind ControlCenterGesture Service failed");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startCtrlCenter(boolean isLeft) {
        if (this.mCtrlCenterIntf != null) {
            Slog.d(TAG, "start control center");
            try {
                this.mCtrlCenterIntf.startControlCenterSide(isLeft);
            } catch (RemoteException | IllegalStateException e) {
                Slog.e(TAG, "start ctrlCenter fail");
            } catch (Exception e2) {
                Slog.e(TAG, "unknow error");
            }
        }
    }

    private boolean isCtrlCenterInstalled() {
        try {
            boolean isCtrlCenterExist = checkPackageExist(CTRLCENTER_PKG, ActivityManager.getCurrentUser());
            if (!isCtrlCenterExist) {
                return isCtrlCenterExist;
            }
            Bundle res = this.mContext.getContentResolver().call(AUTHORITY_URI, METHOD_CHECK_CONTROL_CENTER_ENABLE, (String) null, (Bundle) null);
            if (res == null || !res.getBoolean(ISCONTROLCENTERENABLE, false)) {
                return false;
            }
            return isCtrlCenterExist;
        } catch (IllegalArgumentException | IllegalStateException e) {
            Slog.w(TAG, "not ready.");
            return false;
        } catch (Exception e2) {
            Slog.w(TAG, "Illegal.");
            return false;
        }
    }

    private boolean checkPackageExist(String packageName, int userId) {
        try {
            this.mContext.getPackageManager().getPackageInfoAsUser(packageName, 128, userId);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Slog.w(TAG, packageName + " not found for userId:" + userId);
            return false;
        } catch (Exception e2) {
            Slog.w(TAG, packageName + " not available for userId:" + userId);
            return false;
        }
    }

    private boolean isControlCenterSwitchOn() {
        Bundle res;
        try {
            if (AUTHORITY_URI == null || (res = this.mContext.getContentResolver().call(AUTHORITY_URI, METHOD_CHECK_SWITCH, (String) null, (Bundle) null)) == null || !res.getBoolean(ISSWITCHON, false)) {
                return false;
            }
            return true;
        } catch (IllegalArgumentException | IllegalStateException e) {
            Slog.w(TAG, "not ready.");
            return false;
        } catch (Exception e2) {
            Slog.w(TAG, "Illegal.");
            return false;
        }
    }

    public void updateHotArea() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.policy.NavigationCallOut.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                Point curSize = new Point();
                if (NavigationCallOut.this.getDisplay() != null) {
                    NavigationCallOut.this.getDisplay().getRealSize(curSize);
                }
                if (!curSize.equals(NavigationCallOut.this.realSize)) {
                    Slog.d(NavigationCallOut.TAG, "updateHotArea");
                    NavigationCallOut.this.reset();
                    NavigationCallOut.this.dismissCtrlCenter();
                    NavigationCallOut.this.mIsNeedUpdate = true;
                }
            }
        });
    }

    private void changeArea() {
        if (this.mIsCtrlCenterInstalled) {
            updateRealSize();
            this.mSlideStartThreshold = this.mContext.getResources().getDimensionPixelSize(34472895);
            this.mLandAreaHeight = this.mContext.getResources().getDimensionPixelSize(17105189);
            this.mAreaHeight = this.mContext.getResources().getDimensionPixelSize(17105189);
            int rotation = this.mPolicy.getDefaultDisplayPolicy().getDisplayRotation();
            if (!IS_FOLD_SCREEN_DEVICE || HwFoldScreenManager.getDisplayMode() != 3) {
                if (rotation == 3 || rotation == 1) {
                    this.mLeftRect.set(0, this.realSize.y - this.mLandAreaHeight, (int) (((double) this.realSize.x) * LEFT_RANGE_LAND), this.realSize.y);
                    this.mRightRect.set((int) (((double) this.realSize.x) * RIGHT_RANGE_LAND), this.realSize.y - this.mLandAreaHeight, this.realSize.x, this.realSize.y);
                } else {
                    this.mLeftRect.set(0, this.realSize.y - this.mAreaHeight, (int) (((double) this.realSize.x) * LEFT_RANGE), this.realSize.y);
                    this.mRightRect.set((int) (((double) this.realSize.x) * RIGHT_RANGE), this.realSize.y - this.mAreaHeight, this.realSize.x, this.realSize.y);
                }
                this.mShortDistance = ((float) (this.mLeftRect.bottom - this.mLeftRect.top)) * 0.5f;
                this.mIsNeedUpdate = false;
                return;
            }
            this.mLeftRect.set(0, 0, 0, 0);
            this.mRightRect.set(0, 0, 0, 0);
            this.mIsNeedUpdate = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void preloadCtrlCenter(boolean isLeft) {
        if (this.mCtrlCenterIntf != null) {
            try {
                Slog.d(TAG, "preloadCtrlCenter");
                this.mCtrlCenterIntf.preloadControlCenterSide(isLeft);
                this.mIsStartCtrlCenter = true;
            } catch (RemoteException | IllegalStateException e) {
                Slog.e(TAG, "start ctrlCenter fail");
            } catch (Exception e2) {
                Slog.e(TAG, "unknow error");
            }
        }
    }

    private void moveCtrlCenter(float currentTouch) {
        IControlCenterGesture iControlCenterGesture = this.mCtrlCenterIntf;
        if (iControlCenterGesture != null) {
            try {
                iControlCenterGesture.moveControlCenter(currentTouch);
            } catch (RemoteException | IllegalStateException e) {
            } catch (Exception e2) {
                Slog.e(TAG, "unknow error");
            }
        }
    }

    private void locateCtrlCenter() {
        if (this.mCtrlCenterIntf != null) {
            try {
                Slog.d(TAG, "locateCtrlCenter");
                this.mCtrlCenterIntf.locateControlCenter();
                this.mIsStartCtrlCenter = false;
            } catch (RemoteException | IllegalStateException e) {
                Slog.e(TAG, "reset ctrlCenter fail");
            } catch (Exception e2) {
                Slog.e(TAG, "unknow error");
            }
        } else {
            this.mIsNeedStart = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dismissCtrlCenter() {
        if (this.mIsStartCtrlCenter && this.mCtrlCenterIntf != null) {
            try {
                Slog.d(TAG, "dismissCtrlCenter");
                this.mCtrlCenterIntf.dismissControlCenter();
                this.mIsStartCtrlCenter = false;
            } catch (RemoteException | IllegalStateException e) {
                Slog.e(TAG, "dismiss ctrlCenter fail");
            } catch (Exception e2) {
                Slog.e(TAG, "unknow error");
            }
        }
    }

    private boolean shouldCheckAftForThisGesture() {
        if (this.mFocusWin == null) {
            return false;
        }
        WindowManagerPolicyEx policyEx = new WindowManagerPolicyEx();
        policyEx.setWindowManagerPolicy(this.mPolicy);
        WindowManagerPolicyEx.WindowStateEx focusWinEx = new WindowManagerPolicyEx.WindowStateEx();
        focusWinEx.setWindowState(this.mFocusWin);
        return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwGestureNavWhiteConfig().isEnable(focusWinEx, this.mPolicy.getDefaultDisplayPolicy().getDisplayRotation(), policyEx);
    }

    private void showReTryToast() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.policy.NavigationCallOut.AnonymousClass4 */

            @Override // java.lang.Runnable
            public void run() {
                Toast toast = Toast.makeText(new ContextThemeWrapper(NavigationCallOut.this.mContext, 33947656), 33686279, 0);
                toast.getWindowParams().type = 2010;
                toast.getWindowParams().privateFlags |= 16;
                toast.show();
            }
        });
    }

    public void updateLockTaskState(int lockTaskState) {
        HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getGestureUtils();
        this.mIsInTaskLockMode = DefaultGestureUtils.isInLockTaskMode(lockTaskState);
    }

    public void notifyFocusChange(WindowManagerPolicy.WindowState newFocus) {
        this.mFocusWin = newFocus;
    }

    public boolean isControlCenterArea(float pointX, float pointY) {
        if (!this.mIsControlCenterOn || canDisableGesture()) {
            return this.mIsControlCenterOn;
        }
        Slog.d(TAG, "isControlCenterArea x:" + pointX + " y:" + pointY);
        return this.mLeftRect.contains((int) pointX, (int) pointY) || this.mRightRect.contains((int) pointX, (int) pointY);
    }

    private boolean canDisableGesture() {
        boolean z = this.mIsInTaskLockMode;
        if (z) {
            return z;
        }
        if (SystemProperties.getBoolean(TALKBACK_CONFIG, true) && isTalkBackServicesOn()) {
            Slog.i(TAG, "in talkback mode");
            return true;
        } else if (SystemProperties.getBoolean("runtime.mmitest.isrunning", false)) {
            Slog.i(TAG, "in MMI test");
            return true;
        } else if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            Slog.i(TAG, "in supersave");
            return true;
        } else if (!isInStartUpGuide()) {
            return false;
        } else {
            Slog.i(TAG, "in StartUpGuide");
            return true;
        }
    }

    private boolean isTalkBackServicesOn() {
        Context context = this.mContext;
        boolean isScreenReaderEnabled = false;
        if (context == null) {
            return false;
        }
        if (Settings.Secure.getIntForUser(context.getContentResolver(), ACCESSIBILITY_SCREENREADER_ENABLED, 0, -2) == 1) {
            isScreenReaderEnabled = true;
        }
        return isScreenReaderEnabled;
    }

    private boolean isInStartUpGuide() {
        DefaultDeviceStateController deviceStateController = HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getDeviceStateController(this.mContext);
        return !deviceStateController.isDeviceProvisioned() || !deviceStateController.isCurrentUserSetup() || deviceStateController.isOOBEActivityEnabled() || deviceStateController.isSetupWizardEnabled();
    }

    /* access modifiers changed from: package-private */
    public class NavEventReceiver extends InputEventReceiver {
        NavEventReceiver(InputChannel channel, Looper looper) {
            super(channel, looper);
        }

        public void onInputEvent(InputEvent event) {
            if (event instanceof MotionEvent) {
                NavigationCallOut.this.addPointerEvent((MotionEvent) event);
            }
            finishInputEvent(event, true);
        }
    }
}
