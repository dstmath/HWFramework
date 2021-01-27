package com.android.server.gesture;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Flog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;
import com.android.server.gesture.anim.HwGestureSideLayout;
import com.android.server.pm.HwPackageManagerServiceEx;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.android.content.ContextEx;
import com.huawei.android.content.pm.HwPackageManager;
import com.huawei.android.content.pm.PackageManagerExt;
import com.huawei.android.internal.app.AssistUtilsEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.util.MathUtilsEx;
import com.huawei.android.view.WindowManagerEx;
import com.huawei.android.widget.ToastEx;
import com.huawei.controlcenter.ui.service.IControlCenterGesture;
import com.huawei.server.statusbar.StatusBarManagerInternalEx;
import com.huawei.utils.HwPartResourceUtils;
import java.io.PrintWriter;
import java.util.List;

public class QuickSlideOutController extends QuickStartupStub {
    private static final String ACTION_STATUSBAR_CHANGE = "com.android.systemui.statusbar.visible.change";
    private static final String AUTHORITY = "com.huawei.controlcenter.SwitchProvider";
    private static final Uri AUTHORITY_URI = Uri.parse("content://com.huawei.controlcenter.SwitchProvider");
    private static final String CTRLCENTER_ACTION = "com.huawei.controlcenter.action.CONTROL_CENTER_GESTURE";
    private static final String CTRLCENTER_PKG = "com.huawei.controlcenter";
    private static final String HW_ASSISTANT_PACKAGE_NAME = "com.huawei.vassistant";
    private static final String ISCONTROLCENTERENABLE = "isControlCenterEnable";
    private static final String ISSWITCHON = "isSwitchOn";
    private static final String KEY_INVOKE = "invoke";
    private static final String KYE_VDRIVE_IS_RUN = "vdrive_is_run_state";
    private static final String METHOD_CHECK_CONTROL_CENTER_ENABLE = "getControlCenterState";
    private static final String METHOD_CHECK_SWITCH = "checkCtrlPanelSwitch";
    private static final int MSG_BIND_CTRLCENTER = 3;
    private static final int MSG_CHECK_START_CTRLCENTER = 4;
    private static final int MSG_CLOSE_VIEW = 1;
    private static final int MSG_UPDATE_WINDOW_VIEW = 2;
    private static final String SCANNER_APK_NAME = "HiVision";
    private static final String SCANNER_CLASS_NAME = "com.huawei.scanner.view.ScannerActivity";
    private static final String SCANNER_PACKAGE_NAME = "com.huawei.scanner";
    private static final int TYPE_NOT_PREINSTALL = 1;
    private static final int TYPE_PREINSTALL_AND_EXIST = 3;
    private static final int TYPE_PREINSTALL_BUT_UNINSTALLED = 2;
    private static final String URI_CONTROLCENTER_SWITCH = "content://com.huawei.controlcenter.SwitchProvider/ctrlPanelSwitch";
    private static final String VALUE_INVOKE = "gesture_nav";
    private static final int VALUE_VDRIVE_IS_RUN = 1;
    private static final int VALUE_VDRIVE_IS_UNRUN = 0;
    private AssistUtilsEx mAssistUtils;
    private BootCompletedReceiver mBootCompletedReceiver;
    private final ServiceConnection mCtrlCenterConn = new ServiceConnection() {
        /* class com.android.server.gesture.QuickSlideOutController.AnonymousClass2 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            QuickSlideOutController.this.mCtrlCenterIntf = IControlCenterGesture.Stub.asInterface(service);
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_QSO, "ControlCenterGesture Service connected, " + QuickSlideOutController.this.mCtrlCenterIntf);
            }
            QuickSlideOutController.this.mHandler.sendEmptyMessage(4);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_QSO, "ControlCenterGesture Service disconnected");
            }
            QuickSlideOutController.this.mCtrlCenterIntf = null;
        }
    };
    private IControlCenterGesture mCtrlCenterIntf = null;
    private float mCurrentTouch;
    private Handler mHandler = new MyHandler();
    private boolean mIsAssistantGestureOn;
    private boolean mIsControlCenterGestureOn = false;
    private boolean mIsCtrlCenterPreloaded = false;
    private boolean mIsDisableSlidingAnim;
    private boolean mIsGestureNavReady;
    private boolean mIsGoogleMode = true;
    private boolean mIsInDriveMode = false;
    private boolean mIsNeedStartAfterConnected = false;
    private boolean mIsScannerExist = true;
    private boolean mIsScannerPreInstalled = true;
    private boolean mIsSlideOutEnabled;
    private boolean mIsSlideOverThreshold;
    private boolean mIsSlidingCtrlCenter;
    private boolean mIsSlidingOnLeft = true;
    private boolean mIsSlowAnimTriggered;
    private boolean mIsStatusBarExplaned = false;
    private boolean mIsThresholdTriggered;
    private boolean mIsWindowViewSetuped;
    private final Object mLock = new Object();
    private PackageMonitorReceiver mPackageMonitorReceiver;
    private int mScannerAvailableType = 3;
    private SettingsObserver mSettingsObserver;
    private int mSlideMaxDistance;
    private float mSlidePhasePos;
    private int mSlideStartThreshold;
    private float mStartTouch;
    private StatusBarStatesChangedReceiver mStatusBarReceiver;
    private final Runnable mSuccessRunnable = new Runnable() {
        /* class com.android.server.gesture.QuickSlideOutController.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            QuickSlideOutController.this.gestureSuccessAtEnd(false);
        }
    };
    private SlideOutContainer mViewContainer;
    private WindowManager mWindowManager;

    public QuickSlideOutController(Context context, Looper looper) {
        super(context);
        this.mAssistUtils = new AssistUtilsEx(context);
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
    }

    private void notifyStart() {
        this.mIsGestureNavReady = true;
        if (this.mStatusBarReceiver == null) {
            this.mStatusBarReceiver = new StatusBarStatesChangedReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_STATUSBAR_CHANGE);
            ContextEx.registerReceiverAsUser(this.mContext, this.mStatusBarReceiver, UserHandleEx.ALL, filter, "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM", this.mHandler);
        }
        if (this.mPackageMonitorReceiver == null) {
            this.mPackageMonitorReceiver = new PackageMonitorReceiver();
            IntentFilter filter2 = new IntentFilter();
            filter2.addAction("android.intent.action.PACKAGE_ADDED");
            filter2.addAction("android.intent.action.PACKAGE_REMOVED");
            filter2.addDataScheme("package");
            filter2.addDataSchemeSpecificPart(SCANNER_PACKAGE_NAME, 0);
            ContextEx.registerReceiverAsUser(this.mContext, this.mPackageMonitorReceiver, UserHandleEx.ALL, filter2, (String) null, this.mHandler);
        }
        if (this.mBootCompletedReceiver == null) {
            this.mBootCompletedReceiver = new BootCompletedReceiver();
            IntentFilter filter3 = new IntentFilter();
            filter3.addAction("android.intent.action.BOOT_COMPLETED");
            ContextEx.registerReceiverAsUser(this.mContext, this.mBootCompletedReceiver, UserHandleEx.ALL, filter3, (String) null, this.mHandler);
        }
        if (this.mSettingsObserver == null) {
            this.mSettingsObserver = new SettingsObserver(this.mHandler);
            ContentResolver resolver = this.mContext.getContentResolver();
            ContentResolverExt.registerContentObserver(resolver, Settings.Secure.getUriFor(GestureNavConst.KEY_SECURE_GESTURE_NAVIGATION_ASSISTANT), false, this.mSettingsObserver, -1);
            ContentResolverExt.registerContentObserver(resolver, Settings.Global.getUriFor(KYE_VDRIVE_IS_RUN), false, this.mSettingsObserver, -1);
            ContentResolverExt.registerContentObserver(resolver, Uri.parse(URI_CONTROLCENTER_SWITCH), false, this.mSettingsObserver, -1);
            ContentResolverExt.registerContentObserver(resolver, Settings.Secure.getUriFor("assistant"), false, this.mSettingsObserver, -2);
        }
        updateSettings();
        updateConfig();
        updatePackageExistState();
    }

    private void notifyStop() {
        if (this.mStatusBarReceiver != null) {
            this.mContext.unregisterReceiver(this.mStatusBarReceiver);
            this.mStatusBarReceiver = null;
        }
        if (this.mPackageMonitorReceiver != null) {
            this.mContext.unregisterReceiver(this.mPackageMonitorReceiver);
            this.mPackageMonitorReceiver = null;
        }
        if (this.mBootCompletedReceiver != null) {
            this.mContext.unregisterReceiver(this.mBootCompletedReceiver);
            this.mBootCompletedReceiver = null;
        }
        if (this.mSettingsObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mSettingsObserver);
            this.mSettingsObserver = null;
        }
        this.mIsGestureNavReady = false;
    }

    /* access modifiers changed from: private */
    public final class StatusBarStatesChangedReceiver extends BroadcastReceiver {
        private StatusBarStatesChangedReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && QuickSlideOutController.ACTION_STATUSBAR_CHANGE.equals(intent.getAction())) {
                String visible = "false";
                if (intent.getExtras() != null) {
                    visible = intent.getExtras().getString("visible");
                }
                if (visible != null) {
                    QuickSlideOutController.this.mIsStatusBarExplaned = Boolean.valueOf(visible).booleanValue();
                }
                if (GestureNavConst.DEBUG) {
                    Log.d(GestureNavConst.TAG_GESTURE_QSO, "mIsStatusBarExplaned:" + QuickSlideOutController.this.mIsStatusBarExplaned);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public final class BootCompletedReceiver extends BroadcastReceiver {
        private BootCompletedReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavConst.TAG_GESTURE_QSO, "receive broadcast android.intent.action.BOOT_COMPLETED");
                }
                QuickSlideOutController.this.updateSettings();
            }
        }
    }

    private boolean isControlCenterSwitchOn() {
        ContentResolver resolver;
        Bundle res;
        try {
            if (!(AUTHORITY_URI == null || (res = (resolver = this.mContext.getContentResolver()).call(AUTHORITY_URI, METHOD_CHECK_CONTROL_CENTER_ENABLE, (String) null, (Bundle) null)) == null)) {
                if (res.getBoolean(ISCONTROLCENTERENABLE, false)) {
                    Bundle res2 = resolver.call(AUTHORITY_URI, METHOD_CHECK_SWITCH, (String) null, (Bundle) null);
                    if (res2 == null || !res2.getBoolean(ISSWITCHON, false)) {
                        return false;
                    }
                    return true;
                }
            }
            return false;
        } catch (IllegalArgumentException | IllegalStateException e) {
            Log.e(GestureNavConst.TAG_GESTURE_QSO, "control center may not ready");
            return false;
        } catch (Exception e2) {
            Log.e(GestureNavConst.TAG_GESTURE_QSO, "get control center content exception");
            return false;
        }
    }

    private boolean isCtrlCenterInstalled() {
        boolean isCtrlCenterExist = checkPackageExist(CTRLCENTER_PKG, ActivityManagerEx.getCurrentUser());
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_QSO, "ctrlCenter pkg isExist: " + isCtrlCenterExist);
        }
        return isCtrlCenterExist;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void bindCtrlCenter() {
        if (this.mCtrlCenterIntf == null) {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_QSO, "bindCtrlCenter");
            }
            Intent intent = new Intent(CTRLCENTER_ACTION);
            intent.setPackage(CTRLCENTER_PKG);
            try {
                ContextEx.bindServiceAsUser(this.mContext, intent, this.mCtrlCenterConn, 1, UserHandleEx.CURRENT);
            } catch (SecurityException e) {
                Log.i(GestureNavConst.TAG_GESTURE_QSO, "Bind ControlCenterGesture Service Failed");
            } catch (Exception e2) {
                Log.i(GestureNavConst.TAG_GESTURE_QSO, "Bind ControlCenterGesture Service Exception");
            }
        }
    }

    private void scheduleBindCtrlCenter() {
        if (!this.mHandler.hasMessages(3)) {
            this.mHandler.sendEmptyMessage(3);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkStartCtrlCenterIfNeed() {
        if (this.mIsNeedStartAfterConnected) {
            this.mIsNeedStartAfterConnected = false;
            startCtrlCenterDirectly(this.mIsSlidingOnLeft);
        }
    }

    private void startCtrlCenterDirectly(boolean isLeft) {
        if (this.mCtrlCenterIntf != null) {
            Log.i(GestureNavConst.TAG_GESTURE_QSO, "start control center directly:" + isLeft);
            try {
                this.mCtrlCenterIntf.startControlCenterSide(isLeft);
            } catch (RemoteException e) {
                Log.e(GestureNavConst.TAG_GESTURE_QSO, "start control center fail");
            } catch (Exception e2) {
                Log.e(GestureNavConst.TAG_GESTURE_QSO, "start control center exception");
            }
        }
    }

    private void preloadCtrlCenterIfNeed() {
        if (this.mCtrlCenterIntf != null && !this.mIsCtrlCenterPreloaded) {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_QSO, "preload control center");
            }
            try {
                this.mCtrlCenterIntf.preloadControlCenterSide(this.mIsSlidingOnLeft);
                this.mIsCtrlCenterPreloaded = true;
            } catch (RemoteException | IllegalStateException e) {
                Log.e(GestureNavConst.TAG_GESTURE_QSO, "preload control center fail");
            } catch (Exception e2) {
                Log.e(GestureNavConst.TAG_GESTURE_QSO, "preload control center exception");
            }
        }
    }

    private void moveCtrlCenter(float currentTouch) {
        if (this.mCtrlCenterIntf != null) {
            preloadCtrlCenterIfNeed();
            try {
                this.mCtrlCenterIntf.moveControlCenter(currentTouch);
            } catch (RemoteException | IllegalStateException e) {
                Log.e(GestureNavConst.TAG_GESTURE_QSO, "move control center fail");
            } catch (Exception e2) {
                Log.e(GestureNavConst.TAG_GESTURE_QSO, "move control center exception");
            }
        }
    }

    private void locateCtrlCenter() {
        if (this.mCtrlCenterIntf != null) {
            this.mIsNeedStartAfterConnected = false;
            preloadCtrlCenterIfNeed();
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_QSO, "locate control center");
            }
            try {
                this.mCtrlCenterIntf.locateControlCenter();
            } catch (RemoteException | IllegalStateException e) {
                Log.e(GestureNavConst.TAG_GESTURE_QSO, "locate control center fail");
            } catch (Exception e2) {
                Log.e(GestureNavConst.TAG_GESTURE_QSO, "locate control center exception");
            }
        } else {
            this.mIsNeedStartAfterConnected = true;
        }
    }

    public boolean dismissCtrlCenter(boolean isForce) {
        this.mIsNeedStartAfterConnected = false;
        if (this.mCtrlCenterIntf == null) {
            return false;
        }
        if (!this.mIsCtrlCenterPreloaded && !isForce) {
            return false;
        }
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_QSO, "dismissCtrlCenter isForce:" + isForce);
        }
        try {
            this.mCtrlCenterIntf.dismissControlCenter();
            return true;
        } catch (RemoteException | IllegalStateException e) {
            Log.e(GestureNavConst.TAG_GESTURE_QSO, "dismiss control center fail");
            return false;
        } catch (Exception e2) {
            Log.e(GestureNavConst.TAG_GESTURE_QSO, "dismiss control center exception");
            return false;
        }
    }

    private boolean isCtrlCenterShowing() {
        try {
            return this.mCtrlCenterIntf != null && this.mCtrlCenterIntf.isControlCenterShowing();
        } catch (RemoteException e) {
            Log.e(GestureNavConst.TAG_GESTURE_QSO, "isControlCenterShowing RemoteException");
            return false;
        } catch (Exception e2) {
            Log.e(GestureNavConst.TAG_GESTURE_QSO, "isControlCenterShowing Exception");
            return false;
        }
    }

    /* access modifiers changed from: private */
    public final class PackageMonitorReceiver extends BroadcastReceiver {
        private PackageMonitorReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            QuickSlideOutController.this.updatePackageExistState();
        }
    }

    /* access modifiers changed from: private */
    public final class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            QuickSlideOutController.this.updateSettings();
        }
    }

    private boolean isSuperPowerSaveMode() {
        return GestureUtils.isSuperPowerSaveMode();
    }

    private boolean isInLockTaskMode() {
        return GestureUtils.isInLockTaskMode();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSlideOutWindow() {
        synchronized (this.mLock) {
            if (!this.mIsSlideOutEnabled || !this.mIsGestureNavReady) {
                if (this.mIsWindowViewSetuped) {
                    destroySlideOutView();
                }
            } else if (!this.mIsWindowViewSetuped) {
                createSlideOutView();
            } else {
                updateSlideOutView();
            }
        }
    }

    public int slideOutThreshold(int windowThreshod) {
        return (int) (((float) windowThreshod) * 0.4f);
    }

    public boolean isSlideOutEnableAndAvailable(boolean isLeft) {
        synchronized (this.mLock) {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_QSO, "slideout enable=" + this.mIsSlideOutEnabled + ", assistOn=" + this.mIsAssistantGestureOn + ", ctrlCenterOn=" + this.mIsControlCenterGestureOn);
            }
            boolean z = false;
            if (!this.mIsSlideOutEnabled) {
                return false;
            }
            if (isOverseaAssistantTarget()) {
                if (this.mIsAssistantGestureOn && hasGoogleAssist()) {
                    z = true;
                }
                return z;
            }
            if (this.mIsControlCenterGestureOn && isCtrlCenterInstalled()) {
                z = true;
            }
            return z;
        }
    }

    public boolean isSlideTargetShowing() {
        synchronized (this.mLock) {
            boolean z = false;
            if (!this.mIsSlideOutEnabled) {
                return false;
            }
            if (isOverseaAssistantTarget()) {
                return false;
            }
            if (this.mIsControlCenterGestureOn && isCtrlCenterShowing()) {
                z = true;
            }
            return z;
        }
    }

    @Override // com.android.server.gesture.QuickStartupStub
    public void onNavCreate(GestureNavView navView) {
        super.onNavCreate(navView);
        notifyStart();
        updateSlideOutWindow();
    }

    @Override // com.android.server.gesture.QuickStartupStub
    public void onNavUpdate() {
        super.onNavUpdate();
        updateSlideOutWindow();
    }

    @Override // com.android.server.gesture.QuickStartupStub
    public void onNavDestroy() {
        super.onNavDestroy();
        notifyStop();
        updateSlideOutWindow();
    }

    @Override // com.android.server.gesture.QuickStartupStub
    public void updateConfig() {
        this.mSlideStartThreshold = this.mContext.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("slide_out_start_threshold"));
        this.mSlideMaxDistance = this.mContext.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("slide_out_max_distance"));
        if (GestureNavConst.DEBUG) {
            Log.d(GestureNavConst.TAG_GESTURE_QSO, "threshold=" + this.mSlideStartThreshold + ", max=" + this.mSlideMaxDistance);
        }
    }

    @Override // com.android.server.gesture.QuickStartupStub
    public void updateSettings() {
        synchronized (this.mLock) {
            boolean isLastSlideOutEnabled = this.mIsSlideOutEnabled;
            boolean z = false;
            this.mIsInDriveMode = Settings.Global.getInt(this.mContext.getContentResolver(), KYE_VDRIVE_IS_RUN, 0) == 1;
            this.mIsAssistantGestureOn = GestureNavConst.isAssistantGestureEnabled(this.mContext, -2);
            this.mIsControlCenterGestureOn = isControlCenterSwitchOn();
            if (this.mIsAssistantGestureOn || this.mIsControlCenterGestureOn) {
                z = true;
            }
            this.mIsSlideOutEnabled = z;
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_QSO, "driveMode=" + this.mIsInDriveMode + ", assistOn=" + this.mIsAssistantGestureOn + ", ctrlCenterOn = " + this.mIsControlCenterGestureOn);
            }
            if (this.mIsSlideOutEnabled != isLastSlideOutEnabled) {
                this.mHandler.sendEmptyMessage(2);
            }
            this.mIsGoogleMode = HwGestureSideLayout.GOOGLE_VOICE_ASSISTANT.equals(Settings.Secure.getString(this.mContext.getContentResolver(), "assistant"));
            if (GestureNavConst.DEBUG) {
                Log.d(GestureNavConst.TAG_GESTURE_QSO, "mIsGoogleMode:" + this.mIsGoogleMode);
            }
            if (this.mViewContainer != null) {
                this.mViewContainer.setVoiceIcon(this.mIsGoogleMode);
            }
        }
    }

    @Override // com.android.server.gesture.QuickStartupStub
    public boolean isPreConditionNotReady(boolean isOnLeft) {
        if (!isSuperPowerSaveMode() && !this.mIsStatusBarExplaned && !this.mIsInDriveMode && !isInLockTaskMode() && !this.mDeviceStateController.isKeyguardLocked()) {
            return false;
        }
        if (!GestureNavConst.DEBUG) {
            return true;
        }
        Log.i(GestureNavConst.TAG_GESTURE_QSO, "StatusBarExplaned:" + this.mIsStatusBarExplaned + ",inDriveMode:" + this.mIsInDriveMode);
        return true;
    }

    public void setSlidingSide(boolean isOnLeft) {
        this.mIsSlidingOnLeft = isOnLeft;
        if (GestureNavConst.CHINA_REGION && !this.mIsSlidingOnLeft) {
            this.mScannerAvailableType = getScannerAvailableType();
        }
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_QSO, "onLeft:" + isOnLeft + ", scannerType:" + this.mScannerAvailableType);
        }
        SlideOutContainer slideOutContainer = this.mViewContainer;
        if (slideOutContainer != null) {
            slideOutContainer.setSlidingSide(isOnLeft, this.mScannerAvailableType);
        }
    }

    @Override // com.android.server.gesture.QuickStartupStub
    public void handleTouchEvent(MotionEvent event) {
        if (this.mViewContainer != null) {
            int actionMasked = event.getActionMasked();
            if (actionMasked != 0) {
                if (actionMasked != 1) {
                    if (actionMasked == 2) {
                        handleActionMove(event);
                        return;
                    } else if (actionMasked != 3) {
                        return;
                    }
                }
                handleActionUp(event);
                return;
            }
            handleActionDown(event);
        }
    }

    @Override // com.android.server.gesture.QuickStartupStub
    public void onGestureReallyStarted() {
        super.onGestureReallyStarted();
        if (this.mIsSlidingCtrlCenter) {
            preloadCtrlCenterIfNeed();
        }
    }

    public void resetState(float pointerY) {
        super.resetAtDown();
        this.mViewContainer.reset();
        this.mIsThresholdTriggered = false;
        this.mIsSlideOverThreshold = false;
        this.mIsSlowAnimTriggered = false;
        this.mCurrentTouch = pointerY;
        this.mStartTouch = this.mCurrentTouch;
        this.mIsSlidingCtrlCenter = isSlidingCtrlCenter();
        boolean z = this.mIsSlidingCtrlCenter;
        this.mIsDisableSlidingAnim = z;
        this.mIsCtrlCenterPreloaded = false;
        this.mIsNeedStartAfterConnected = false;
        if (z && this.mCtrlCenterIntf == null) {
            scheduleBindCtrlCenter();
        }
    }

    private void handleActionDown(MotionEvent event) {
        resetState(event.getY());
    }

    private void handleActionMove(MotionEvent event) {
        this.mCurrentTouch = event.getY();
        if (!this.mIsDisableSlidingAnim) {
            if (this.mIsGestureReallyStarted && !this.mViewContainer.isShowing()) {
                showOrb();
                if (GestureNavConst.DEBUG) {
                    Log.d(GestureNavConst.TAG_GESTURE_QSO, "start showOrb");
                }
            }
            if (!this.mIsSlowAnimTriggered && this.mIsGestureSlowProcessStarted) {
                this.mIsSlowAnimTriggered = true;
                notifyAnimStarted();
            }
            if (!this.mIsThresholdTriggered && this.mViewContainer.isVisible() && (slideOverThreshold(false) || !this.mViewContainer.isAnimationRunning())) {
                this.mIsThresholdTriggered = true;
                this.mSlidePhasePos = this.mCurrentTouch;
                if (GestureNavConst.DEBUG) {
                    Log.d(GestureNavConst.TAG_GESTURE_QSO, "slide over threshold, slidePos=" + this.mSlidePhasePos);
                }
            }
            if (this.mIsThresholdTriggered && this.mIsSlowAnimTriggered) {
                this.mIsSlideOverThreshold = slideOverThreshold(false);
                this.mViewContainer.setSlideOverThreshold(this.mIsSlideOverThreshold);
                float offset = this.mSlidePhasePos - this.mCurrentTouch;
                if (offset < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                    offset = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                }
                this.mViewContainer.setSlideDistance(offset, MathUtilsEx.constrain(offset, (float) GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, (float) this.mSlideMaxDistance) / ((float) this.mSlideMaxDistance));
            }
        } else if (this.mIsSlidingCtrlCenter && this.mIsGestureReallyStarted) {
            moveCtrlCenter(this.mCurrentTouch);
        }
    }

    private void handleActionUp(MotionEvent event) {
        this.mCurrentTouch = event.getY();
        this.mIsSlideOverThreshold = slideOverThreshold(true);
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_QSO, "slideOver=" + this.mIsSlideOverThreshold + ", valid=" + this.mIsValidGuesture + ", fast=" + this.mIsFastSlideGesture);
        }
        int reportType = this.mIsSlidingOnLeft ? 991310850 : 991310851;
        if (!this.mIsValidGuesture || !this.mIsSlideOverThreshold) {
            if (!this.mIsDisableSlidingAnim) {
                this.mViewContainer.startExitAnimation(false, false);
            } else {
                dismissCtrlCenter(false);
            }
            Flog.bdReport(reportType, GestureNavConst.reportResultStr(false, this.mGestureFailedReason));
            return;
        }
        performSlideEndAction(this.mIsFastSlideGesture);
        Flog.bdReport(reportType, GestureNavConst.reportResultStr(true, -1));
    }

    private boolean slideOverThreshold(boolean isCheckAtEnd) {
        if (!isCheckAtEnd || Math.abs(this.mStartTouch - this.mCurrentTouch) > ((float) this.mSlideStartThreshold)) {
            return true;
        }
        return false;
    }

    private void performSlideEndAction(boolean isFastSlide) {
        if (this.mIsDisableSlidingAnim || !this.mViewContainer.isAnimationRunning()) {
            gestureSuccessAtEnd(isFastSlide);
            return;
        }
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_QSO, "preform action until anim finished");
        }
        this.mViewContainer.performOnAnimationFinished(this.mSuccessRunnable);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void gestureSuccessAtEnd(boolean isFastSlide) {
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_QSO, "execute animation and start target, isFastSlide:" + isFastSlide);
        }
        if (!this.mIsDisableSlidingAnim) {
            this.mViewContainer.startExitAnimation(isFastSlide, true);
        }
        startTarget();
    }

    private void startTarget() {
        if (GestureNavConst.CHINA_REGION) {
            Log.i(GestureNavConst.TAG_GESTURE_QSO, "launch target for controlcenter");
            locateCtrlCenter();
        } else if (isOverseaAssistantTarget()) {
            Log.i(GestureNavConst.TAG_GESTURE_QSO, "launch target for voice assistant");
            startVoiceAssist();
        } else {
            Log.i(GestureNavConst.TAG_GESTURE_QSO, "launch target for controlcenter");
            locateCtrlCenter();
        }
    }

    private boolean isSlidingCtrlCenter() {
        if (isOverseaAssistantTarget()) {
            return false;
        }
        return true;
    }

    private boolean isOverseaAssistantTarget() {
        return !GestureNavConst.CHINA_REGION;
    }

    private void startVoiceAssist() {
        try {
            Log.i(GestureNavConst.TAG_GESTURE_QSO, "startAssist");
            Bundle bundle = new Bundle();
            bundle.putBoolean("isFromGesture", true);
            StatusBarManagerInternalEx.startAssist(bundle);
        } catch (IllegalArgumentException e) {
            Log.e(GestureNavConst.TAG_GESTURE_QSO, "startVoiceAssist catch IllegalArgumentException");
        } catch (Exception e2) {
            Log.e(GestureNavConst.TAG_GESTURE_QSO, "startVoiceAssist catch Exception");
        }
    }

    private ComponentName getAssistInfo() {
        AssistUtilsEx assistUtilsEx = this.mAssistUtils;
        if (assistUtilsEx != null) {
            return assistUtilsEx.getAssistComponentForUser(-2);
        }
        return null;
    }

    private boolean hasAssist() {
        if (GestureNavConst.CHINA_REGION) {
            return checkPackageExist(HW_ASSISTANT_PACKAGE_NAME, ActivityManagerEx.getCurrentUser());
        }
        return hasGoogleAssist();
    }

    private boolean hasGoogleAssist() {
        if (getAssistInfo() == null) {
            return false;
        }
        return true;
    }

    private final class MyHandler extends Handler {
        private MyHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                QuickSlideOutController.this.hideSlideOutView();
            } else if (i == 2) {
                QuickSlideOutController.this.updateSlideOutWindow();
            } else if (i == 3) {
                QuickSlideOutController.this.bindCtrlCenter();
            } else if (i == 4) {
                QuickSlideOutController.this.checkStartCtrlCenterIfNeed();
            }
        }
    }

    private void createSlideOutView() {
        if (GestureNavConst.DEBUG) {
            Log.d(GestureNavConst.TAG_GESTURE_QSO, "createSlideOutView");
        }
        this.mViewContainer = (SlideOutContainer) LayoutInflater.from(this.mContext).inflate(HwPartResourceUtils.getResourceId("gesture_slide_out_view"), (ViewGroup) null);
        this.mViewContainer.setOnTouchListener(new TouchOutsideListener(1));
        this.mViewContainer.setVisibility(8);
        GestureUtils.addWindowView(this.mWindowManager, this.mViewContainer, getSlideOutLayoutParams());
        this.mIsWindowViewSetuped = true;
    }

    private void updateSlideOutView() {
        GestureUtils.updateViewLayout(this.mWindowManager, this.mViewContainer, getSlideOutLayoutParams());
    }

    private void destroySlideOutView() {
        if (GestureNavConst.DEBUG) {
            Log.d(GestureNavConst.TAG_GESTURE_QSO, "destroySlideOutView");
        }
        this.mIsWindowViewSetuped = false;
        GestureUtils.removeWindowView(this.mWindowManager, this.mViewContainer, true);
        this.mViewContainer = null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideSlideOutView() {
        try {
            if (this.mViewContainer != null) {
                this.mViewContainer.hide(true);
            }
        } catch (IllegalArgumentException e) {
            Log.e(GestureNavConst.TAG_GESTURE_QSO, "hideSlideOutView catch IllegalArgumentException");
        } catch (Exception e2) {
            Log.e(GestureNavConst.TAG_GESTURE_QSO, "hideSlideOutView catch Exception");
        }
    }

    private WindowManager.LayoutParams getSlideOutLayoutParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(WindowManagerEx.LayoutParamsEx.getTypeNavigationBarPanel(), 8519936, -3);
        if (ActivityManagerEx.isHighEndGfx()) {
            lp.flags |= 16777216;
        }
        lp.gravity = 8388691;
        lp.width = -1;
        lp.height = -1;
        lp.windowAnimations = HwPartResourceUtils.getResourceId("Animation_RecentApplications");
        lp.softInputMode = 49;
        lp.setTitle("GestureSildeOut");
        return lp;
    }

    /* access modifiers changed from: private */
    public class TouchOutsideListener implements View.OnTouchListener {
        private int mMsg;

        TouchOutsideListener(int msg) {
            this.mMsg = msg;
        }

        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View v, MotionEvent ev) {
            int action = ev.getAction();
            if (action != 4 && action != 0) {
                return false;
            }
            QuickSlideOutController.this.mHandler.removeMessages(this.mMsg);
            QuickSlideOutController.this.mHandler.sendEmptyMessage(this.mMsg);
            return true;
        }
    }

    private void showOrb() {
        this.mViewContainer.show(true);
    }

    private void notifyAnimStarted() {
        this.mViewContainer.startEnterAnimation();
    }

    private boolean startScanner() {
        if (this.mContext == null) {
            Log.i(GestureNavConst.TAG_GESTURE_QSO, "context error");
            return false;
        }
        Intent aiIntent = new Intent();
        aiIntent.setFlags(268435456);
        aiIntent.setFlags(65536);
        aiIntent.setClassName(SCANNER_PACKAGE_NAME, SCANNER_CLASS_NAME);
        aiIntent.putExtra(KEY_INVOKE, VALUE_INVOKE);
        try {
            Log.i(GestureNavConst.TAG_GESTURE_QSO, "start scanner");
            ContextEx.startActivityAsUser(this.mContext, aiIntent, (Bundle) null, UserHandleEx.CURRENT);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePackageExistState() {
        boolean isAppExistInMainUser;
        int currentUserId = ActivityManagerEx.getCurrentUser();
        this.mIsScannerExist = checkPackageExist(SCANNER_PACKAGE_NAME, currentUserId);
        if (!this.mIsScannerExist) {
            this.mIsScannerPreInstalled = false;
            if (currentUserId != 0) {
                isAppExistInMainUser = checkPackageExist(SCANNER_PACKAGE_NAME, 0);
            } else {
                isAppExistInMainUser = false;
            }
            if (isAppExistInMainUser) {
                this.mIsScannerPreInstalled = true;
            } else {
                checkScanInstallList();
            }
        } else {
            this.mIsScannerPreInstalled = true;
        }
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_QSO, "scannerExist=" + this.mIsScannerExist + ", scannerPreInstalled=" + this.mIsScannerPreInstalled);
        }
    }

    private void checkScanInstallList() {
        List<String> list = HwPackageManager.getScanInstallList();
        if (list != null) {
            for (String apkPatch : list) {
                if (apkPatch != null && apkPatch.contains(SCANNER_APK_NAME)) {
                    this.mIsScannerPreInstalled = true;
                    return;
                }
            }
        }
    }

    private boolean checkPackageExist(String packageName, int userId) {
        try {
            PackageManagerExt.getPackageInfoAsUser(this.mContext.getPackageManager(), packageName, (int) HwPackageManagerServiceEx.APP_FORCE_DARK_USER_SET_FLAG, userId);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(GestureNavConst.TAG_GESTURE_QSO, packageName + " not found for userId:" + userId);
            return false;
        } catch (Exception e2) {
            Log.w(GestureNavConst.TAG_GESTURE_QSO, packageName + " not available for userId:" + userId);
            return false;
        }
    }

    private int getScannerAvailableType() {
        if (this.mIsScannerExist) {
            return 3;
        }
        if (this.mIsScannerPreInstalled) {
            return 2;
        }
        return 1;
    }

    private void showNotAvailableToast() {
        if (GestureNavConst.DEBUG) {
            Log.d(GestureNavConst.TAG_GESTURE_QSO, "showNotAvailableToast");
        }
        this.mHandler.post(new Runnable() {
            /* class com.android.server.gesture.QuickSlideOutController.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                Toast toast = Toast.makeText(QuickSlideOutController.this.mContext, HwPartResourceUtils.getResourceId("toast_hiVision_not_available"), 0);
                WindowManager.LayoutParams params = ToastEx.getWindowParams(toast);
                params.type = 2010;
                new WindowManagerEx.LayoutParamsEx(params).addPrivateFlags(WindowManagerEx.LayoutParamsEx.getPrivateFlagShowForAllUsers());
                toast.show();
            }
        });
    }

    public void dump(String prefix, PrintWriter pw, String[] args) {
        pw.print(prefix);
        pw.print("sidleOutEnabled=" + this.mIsSlideOutEnabled);
        pw.print(" assitOn=" + this.mIsAssistantGestureOn);
        pw.print(" ctrlCenterOn=" + this.mIsControlCenterGestureOn);
        pw.print(" hasGoogleAssist=" + hasGoogleAssist());
        pw.print(" hasCtrlCenter=" + isCtrlCenterInstalled());
        pw.print(" ctrlCenterShowing=" + isCtrlCenterShowing());
        pw.println();
        pw.print(prefix);
        pw.print("driveMode=" + this.mIsInDriveMode);
        pw.print(" statusBarExplaned=" + this.mIsStatusBarExplaned);
        pw.print(" superPowerMode=" + isSuperPowerSaveMode());
        pw.println();
    }
}
