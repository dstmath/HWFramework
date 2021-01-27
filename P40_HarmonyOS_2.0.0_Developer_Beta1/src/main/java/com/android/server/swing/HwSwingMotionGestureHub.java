package com.android.server.swing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.rms.iaware.AppTypeRecoManager;
import android.util.Log;
import android.view.KeyEvent;
import com.android.internal.util.ScreenshotHelper;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import com.huawei.hiai.awareness.client.AwarenessEnvelope;
import com.huawei.hiai.awareness.client.AwarenessFence;
import com.huawei.hiai.awareness.client.AwarenessManager;
import com.huawei.hiai.awareness.client.AwarenessRequest;
import com.huawei.hiai.awareness.client.AwarenessResult;
import com.huawei.hiai.awareness.client.AwarenessServiceConnection;
import com.huawei.hiai.awareness.client.OnEnvelopeReceiver;
import com.huawei.hiai.awareness.client.OnResultListener;
import com.huawei.systemserver.swing.IHwSwingEventNotifier;

public class HwSwingMotionGestureHub extends HwSwingMotionGestureBaseHub {
    private static final String ACCESSIBILITY_SCREENREADER_ENABLED = "accessibility_screenreader_enabled";
    private static final String INTENT_CHANGE_POWER_MODE = "huawei.intent.action.HWSYSTEMMANAGER_CHANGE_POWERMODE";
    private static final String INTENT_SHUTDOWN_SUPER_POWER_SAVING_MODE = "huawei.intent.action.HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE";
    private static final String KEYGUARD_URI = "content://com.huawei.keyguard.LockState";
    private static final String PERMISSION_SWING_GRAB_SCREEN_SHOT = "com.huawei.motionservice.permission.ACCESS_SWING_GRAB";
    private static final String REASON_SWING_HOVERGESTURE = "SwingHoverGesture";
    private static final int REPORT_THRESHOLD = 200;
    private static final String SWING_GESTURE_ABILITY = SystemProperties.get("hw_mc.hiai.swing_gesture_ability", "v:2.0;u:1;d:1;l:0;r:0;p:0;f:1;h:0");
    private static final int SWING_GESTURE_LENGTH = 2;
    private static final String SWING_MOTION_GUIDE_TITLE = "com.huawei.motionservice/com.huawei.motionsettings.MotionSwingGuide";
    private static final String SWING_MOTION_PERMISSION = "com.huawei.permission.SWING_MOTION";
    private static final String SYSTEM_MANAGER_PERMISSION = "com.huawei.systemmanager.permission.ACCESS_INTERFACE";
    private static final String TAG = "HwSwingMotionGestureHub";
    private static final int TAKE_SCREENSHOT_FULLSCREEN = 1;
    private static final int TALKBACK_SERVICE_DISABLE = 0;
    private static final int TALKBACK_SERVICE_ENABLE = 1;
    private static HwSwingMotionGestureHub sInstance;
    private AwarenessManager mAwarenessManager;
    private AwarenessServiceConnection mAwarenessServiceConnection = new AwarenessServiceConnection() {
        /* class com.android.server.swing.HwSwingMotionGestureHub.AnonymousClass1 */

        @Override // com.huawei.hiai.awareness.client.AwarenessServiceConnection
        public void onConnected() {
            HwSwingMotionGestureHub hwSwingMotionGestureHub = HwSwingMotionGestureHub.this;
            hwSwingMotionGestureHub.mIsAwarenessConnected = true;
            hwSwingMotionGestureHub.mAwarenessReconnectTimes = 0;
            Log.i(HwSwingMotionGestureHub.TAG, "mAwarenessServiceConnection onServiceConnected");
            HwSwingMotionGestureHub.this.lambda$notifyFocusChange$0$HwSwingMotionGestureHub();
        }

        @Override // com.huawei.hiai.awareness.client.AwarenessServiceConnection
        public void onDisconnected() {
            Log.i(HwSwingMotionGestureHub.TAG, "mAwarenessServiceConnection onServiceDisConnected");
            HwSwingMotionGestureHub hwSwingMotionGestureHub = HwSwingMotionGestureHub.this;
            hwSwingMotionGestureHub.mIsAwarenessConnected = false;
            hwSwingMotionGestureHub.disableSwingMotionDispatch();
            if (!HwSwingMotionGestureHub.this.isNeedRegisterAwareness()) {
                Log.w(HwSwingMotionGestureHub.TAG, "no need to reconnect service");
                return;
            }
            Log.w(HwSwingMotionGestureHub.TAG, "wait 10000 ms to reconnect");
            HwSwingMotionGestureHub.this.mAwarenessHandler.postDelayed(new Runnable() {
                /* class com.android.server.swing.HwSwingMotionGestureHub.AnonymousClass1.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    HwSwingMotionGestureHub.this.mAwarenessReconnectTimes++;
                    if (!HwSwingMotionGestureHub.this.mIsAwarenessConnected && HwSwingMotionGestureHub.this.mAwarenessReconnectTimes < 3) {
                        Log.w(HwSwingMotionGestureHub.TAG, "mAwarenessHandler try connectService");
                        if (!HwSwingMotionGestureHub.this.mAwarenessManager.connectService(HwSwingMotionGestureHub.this.mAwarenessServiceConnection)) {
                            Log.w(HwSwingMotionGestureHub.TAG, "connectService failed!");
                            HwSwingMotionGestureHub.this.mAwarenessHandler.postDelayed(this, 10000);
                        }
                    }
                }
            }, 10000);
        }
    };
    private int mDownActionCount = 0;
    private int mDownResponseCount = 0;
    private long mHoverScreenOffTimeMs;
    private long mHoverScreenOnTimeMs;
    private boolean mIsCurrentSwingMotionGuide = false;
    private boolean mIsDownFenceRegistered;
    private boolean mIsDownFenceSupport = true;
    private boolean mIsFetchFenceRegistered;
    private boolean mIsFetchFenceSupport = true;
    private boolean mIsHoverFenceRegistered;
    private boolean mIsHoverFenceSupport;
    private boolean mIsLeftFenceRegistered;
    private boolean mIsLeftFenceSupport;
    private boolean mIsLeftRightGestureEnabled;
    private boolean mIsMotionAwarenessRegistered;
    private boolean mIsPushFenceRegistered;
    private boolean mIsPushFenceSupport;
    private boolean mIsRightFenceRegistered;
    private boolean mIsRightFenceSupport;
    private boolean mIsStartDownFenceRegistered;
    private boolean mIsStartLeftFenceRegistered;
    private boolean mIsStartRightFenceRegistered;
    private boolean mIsStartUpFenceRegistered;
    private boolean mIsSuperPowerMode = false;
    private boolean mIsUpFenceRegistered;
    private boolean mIsUpFenceSupport = true;
    private String mLastSwingMotionGesture;
    private int mLeftActionCount = 0;
    private int mLeftResponseCount = 0;
    private AwarenessFence mMotionDownAwarenessFence;
    private AwarenessFence mMotionFetchAwarenessFence;
    private HwSwingMotionGestureHandler mMotionGestureHandler;
    private AwarenessFence mMotionHoverAwarenessFence;
    private AwarenessFence mMotionLeftAwarenessFence;
    private AwarenessFence mMotionPushAwarenessFence;
    private AwarenessFence mMotionRightAwarenessFence;
    private AwarenessFence mMotionStartDownAwarenessFence;
    private AwarenessFence mMotionStartLeftAwarenessFence;
    private AwarenessFence mMotionStartRightAwarenessFence;
    private AwarenessFence mMotionStartUpAwarenessFence;
    private AwarenessFence mMotionUpAwarenessFence;
    private OnEnvelopeReceiver mOnEnvelopeReceiver = new OnEnvelopeReceiver.Stub() {
        /* class com.android.server.swing.HwSwingMotionGestureHub.AnonymousClass2 */

        @Override // com.huawei.hiai.awareness.client.OnEnvelopeReceiver
        public void onReceive(AwarenessEnvelope envelope) throws RemoteException {
            if (envelope == null) {
                Log.e(HwSwingMotionGestureHub.TAG, "onReceive envelope is null");
                return;
            }
            HwSwingMotionGestureHub.this.handleFenceResult(AwarenessFence.parseFrom(envelope));
        }
    };
    private OnResultListener mOnResultListener = new OnResultListener.Stub() {
        /* class com.android.server.swing.HwSwingMotionGestureHub.AnonymousClass3 */

        @Override // com.huawei.hiai.awareness.client.OnResultListener
        public void onResult(AwarenessResult awarenessResult) throws RemoteException {
            if (awarenessResult == null) {
                Log.e(HwSwingMotionGestureHub.TAG, "onResult awarenessResult is null");
                return;
            }
            Log.w(HwSwingMotionGestureHub.TAG, "register fence result:" + awarenessResult);
        }
    };
    private PowerManager mPowerManager = null;
    private int mRightActionCount = 0;
    private int mRightResponseCount = 0;
    private HwSwingEventNotifierUtil mSwingEventNotifierUtil;
    private int mUpActionCount = 0;
    private int mUpResponseCount = 0;

    private HwSwingMotionGestureHub(Context context) {
        super(context);
        Log.i(TAG, "constructor");
        this.mAwarenessManager = new AwarenessManager(this.mContext);
        this.mMotionGestureHandler = new HwSwingMotionGestureHandler(this.mContext);
        this.mSwingEventNotifierUtil = HwSwingEventNotifierUtil.getInstance(this.mContext);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        SwingStatusReceiver receiver = new SwingStatusReceiver();
        IntentFilter statusFilter = new IntentFilter();
        statusFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON);
        statusFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF);
        this.mContext.registerReceiver(receiver, statusFilter);
        IntentFilter powerFilter = new IntentFilter();
        powerFilter.addAction(INTENT_CHANGE_POWER_MODE);
        powerFilter.addAction(INTENT_SHUTDOWN_SUPER_POWER_SAVING_MODE);
        this.mContext.registerReceiver(receiver, powerFilter, SYSTEM_MANAGER_PERMISSION, null);
        createMotionAwarenessFence();
        checkMotionAwarenessFenceSupport();
    }

    public static synchronized HwSwingMotionGestureHub getInstance(Context context) {
        HwSwingMotionGestureHub hwSwingMotionGestureHub;
        synchronized (HwSwingMotionGestureHub.class) {
            if (sInstance == null) {
                sInstance = new HwSwingMotionGestureHub(context);
            }
            hwSwingMotionGestureHub = sInstance;
        }
        return hwSwingMotionGestureHub;
    }

    @Override // com.android.server.swing.HwSwingMotionGestureBaseHub
    public boolean dispatchUnhandledKey(final KeyEvent event, String pkgName) {
        if (event == null) {
            Log.e(TAG, "dispatchUnhandledKey event is null");
            return false;
        } else if (event.getAction() != 0) {
            return false;
        } else {
            this.mAwarenessHandler.post(new Runnable() {
                /* class com.android.server.swing.HwSwingMotionGestureHub.AnonymousClass4 */

                @Override // java.lang.Runnable
                public void run() {
                    HwSwingMotionGestureHub.this.reportFenceResultByKeyEvent(event);
                }
            });
            return this.mMotionGestureHandler.dispatchUnhandledKey(event);
        }
    }

    @Override // com.android.server.swing.HwSwingMotionGestureBaseHub
    public void notifyRotationChange(int rotation) {
        this.mMotionGestureHandler.notifyRotationChange(rotation);
    }

    @Override // com.android.server.swing.HwSwingMotionGestureBaseHub
    public void notifyFingersTouching(boolean isTouching) {
        this.mMotionGestureHandler.notifyFingersTouching(isTouching);
    }

    @Override // com.android.server.swing.HwSwingMotionGestureBaseHub
    public void notifyFocusChange(String focusWindowTitle, String focusPkgName) {
        super.notifyFocusChange(focusWindowTitle, focusPkgName);
        this.mIsCurrentSwingMotionGuide = SWING_MOTION_GUIDE_TITLE.equals(focusWindowTitle);
        if (shouldRefreshLeftRightGesture(focusPkgName)) {
            this.mAwarenessHandler.post(new Runnable() {
                /* class com.android.server.swing.$$Lambda$HwSwingMotionGestureHub$cV0JOcvfT9mlAgfjGUpGTF3L_iA */

                @Override // java.lang.Runnable
                public final void run() {
                    HwSwingMotionGestureHub.this.lambda$notifyFocusChange$0$HwSwingMotionGestureHub();
                }
            });
        }
        if (focusWindowTitle != null) {
            this.mFocusWindowTitle = focusWindowTitle;
        }
        if (focusPkgName != null) {
            this.mFocusPkgName = focusPkgName;
        }
        this.mMotionGestureHandler.notifyFocusChange(focusWindowTitle, focusPkgName);
        HwSwingReport.setFocusPkgName(focusPkgName);
    }

    private boolean shouldRefreshLeftRightGesture(String focusPkgName) {
        boolean shouldRefresh = false;
        if (focusPkgName == null || focusPkgName.equals(this.mFocusPkgName)) {
            return false;
        }
        boolean isLeftRightGestureEnabled = isGallery(focusPkgName) || isReadeingApp(focusPkgName);
        if (isLeftRightGestureEnabled != this.mIsLeftRightGestureEnabled) {
            shouldRefresh = true;
        }
        if (shouldRefresh) {
            this.mIsLeftRightGestureEnabled = isLeftRightGestureEnabled;
        }
        return shouldRefresh;
    }

    private boolean isGallery(String focusPkgName) {
        return "com.android.gallery3d".equals(focusPkgName) || "com.huawei.photos".equals(focusPkgName);
    }

    private boolean isReadeingApp(String focusPkgName) {
        int appType = AppTypeRecoManager.getInstance().getAppType(focusPkgName);
        Log.i(TAG, "current app:" + focusPkgName + ",appType:" + appType);
        return appType == 6;
    }

    private void createMotionAwarenessFence() {
        this.mMotionUpAwarenessFence = AwarenessFence.create(HwSwingMotionGestureConstant.GESTURE_FENCE).putArg(HwSwingMotionGestureConstant.KEY_MOTION_GESTURE, HwSwingMotionGestureConstant.VALUE_SLIDE_UP).putArg(HwSwingMotionGestureConstant.KEY_SCREEN_DISPLAY, HwSwingMotionGestureConstant.VALUE_SCREEN_ON);
        this.mMotionStartUpAwarenessFence = AwarenessFence.create(HwSwingMotionGestureConstant.GESTURE_FENCE).putArg(HwSwingMotionGestureConstant.KEY_MOTION_GESTURE, HwSwingMotionGestureConstant.VALUE_START_UP).putArg(HwSwingMotionGestureConstant.KEY_SCREEN_DISPLAY, HwSwingMotionGestureConstant.VALUE_SCREEN_ON);
        this.mMotionDownAwarenessFence = AwarenessFence.create(HwSwingMotionGestureConstant.GESTURE_FENCE).putArg(HwSwingMotionGestureConstant.KEY_MOTION_GESTURE, HwSwingMotionGestureConstant.VALUE_SLIDE_DOWN).putArg(HwSwingMotionGestureConstant.KEY_SCREEN_DISPLAY, HwSwingMotionGestureConstant.VALUE_SCREEN_ON);
        this.mMotionStartDownAwarenessFence = AwarenessFence.create(HwSwingMotionGestureConstant.GESTURE_FENCE).putArg(HwSwingMotionGestureConstant.KEY_MOTION_GESTURE, HwSwingMotionGestureConstant.VALUE_START_DOWN).putArg(HwSwingMotionGestureConstant.KEY_SCREEN_DISPLAY, HwSwingMotionGestureConstant.VALUE_SCREEN_ON);
        this.mMotionLeftAwarenessFence = AwarenessFence.create(HwSwingMotionGestureConstant.GESTURE_FENCE).putArg(HwSwingMotionGestureConstant.KEY_MOTION_GESTURE, HwSwingMotionGestureConstant.VALUE_SLIDE_LEFT).putArg(HwSwingMotionGestureConstant.KEY_SCREEN_DISPLAY, HwSwingMotionGestureConstant.VALUE_SCREEN_ON);
        this.mMotionStartLeftAwarenessFence = AwarenessFence.create(HwSwingMotionGestureConstant.GESTURE_FENCE).putArg(HwSwingMotionGestureConstant.KEY_MOTION_GESTURE, HwSwingMotionGestureConstant.VALUE_START_LEFT).putArg(HwSwingMotionGestureConstant.KEY_SCREEN_DISPLAY, HwSwingMotionGestureConstant.VALUE_SCREEN_ON);
        this.mMotionRightAwarenessFence = AwarenessFence.create(HwSwingMotionGestureConstant.GESTURE_FENCE).putArg(HwSwingMotionGestureConstant.KEY_MOTION_GESTURE, HwSwingMotionGestureConstant.VALUE_SLIDE_RIGHT).putArg(HwSwingMotionGestureConstant.KEY_SCREEN_DISPLAY, HwSwingMotionGestureConstant.VALUE_SCREEN_ON);
        this.mMotionStartRightAwarenessFence = AwarenessFence.create(HwSwingMotionGestureConstant.GESTURE_FENCE).putArg(HwSwingMotionGestureConstant.KEY_MOTION_GESTURE, HwSwingMotionGestureConstant.VALUE_START_RIGHT).putArg(HwSwingMotionGestureConstant.KEY_SCREEN_DISPLAY, HwSwingMotionGestureConstant.VALUE_SCREEN_ON);
        this.mMotionPushAwarenessFence = AwarenessFence.create(HwSwingMotionGestureConstant.GESTURE_FENCE).putArg(HwSwingMotionGestureConstant.KEY_MOTION_GESTURE, HwSwingMotionGestureConstant.VALUE_PUSH).putArg(HwSwingMotionGestureConstant.KEY_SCREEN_DISPLAY, HwSwingMotionGestureConstant.VALUE_SCREEN_ON);
        this.mMotionFetchAwarenessFence = AwarenessFence.create(HwSwingMotionGestureConstant.GESTURE_FENCE).putArg(HwSwingMotionGestureConstant.KEY_MOTION_GESTURE, HwSwingMotionGestureConstant.VALUE_FETCH).putArg(HwSwingMotionGestureConstant.KEY_SCREEN_DISPLAY, HwSwingMotionGestureConstant.VALUE_SCREEN_ON);
        this.mMotionHoverAwarenessFence = AwarenessFence.create(HwSwingMotionGestureConstant.GESTURE_FENCE).putArg(HwSwingMotionGestureConstant.KEY_MOTION_GESTURE, HwSwingMotionGestureConstant.VALUE_HOVER).putArg(HwSwingMotionGestureConstant.KEY_SCREEN_DISPLAY, HwSwingMotionGestureConstant.VALUE_SCREEN_OFF);
    }

    private void checkMotionAwarenessFenceSupport() {
        for (String ablity : SWING_GESTURE_ABILITY.split(AwarenessInnerConstants.SEMI_COLON_KEY)) {
            String[] gestureValues = ablity.split(AwarenessInnerConstants.COLON_KEY);
            if (gestureValues.length != 2) {
                Log.e(TAG, "swing gesture ability config error");
                return;
            }
            String gestureName = gestureValues[0];
            String gestureState = gestureValues[1];
            Log.i(TAG, "gestureName:" + gestureName + ",gestureState:" + gestureState);
            boolean isEnabled = "1".equals(gestureState);
            if ("u".equals(gestureName)) {
                this.mIsUpFenceSupport = isEnabled;
            } else if ("d".equals(gestureName)) {
                this.mIsDownFenceSupport = isEnabled;
            } else if ("l".equals(gestureName)) {
                this.mIsLeftFenceSupport = isEnabled;
            } else if ("r".equals(gestureName)) {
                this.mIsRightFenceSupport = isEnabled;
            } else if ("f".equals(gestureName)) {
                this.mIsFetchFenceSupport = isEnabled;
            } else if ("p".equals(gestureName)) {
                this.mIsPushFenceSupport = isEnabled;
            } else if ("h".equals(gestureName)) {
                this.mIsHoverFenceSupport = isEnabled;
            }
        }
    }

    private class SwingStatusReceiver extends BroadcastReceiver {
        private SwingStatusReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.e(HwSwingMotionGestureHub.TAG, "SwingStatusReceiver intent is null");
                return;
            }
            String action = intent.getAction();
            Log.i(HwSwingMotionGestureHub.TAG, "on receive action:" + action);
            if (HwSwingMotionGestureHub.INTENT_SHUTDOWN_SUPER_POWER_SAVING_MODE.equals(action)) {
                HwSwingMotionGestureHub.this.mIsSuperPowerMode = false;
                HwSwingMotionGestureHub.this.refreshAwarenessConnection();
            } else if (HwSwingMotionGestureHub.INTENT_CHANGE_POWER_MODE.equals(action)) {
                HwSwingMotionGestureHub.this.mIsSuperPowerMode = true;
                HwSwingMotionGestureHub.this.refreshAwarenessConnection();
            } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON.equals(action)) {
                Log.i(HwSwingMotionGestureHub.TAG, "slidescreen switch:" + HwSwingMotionGestureHub.this.mIsSwingSlideScreenEnabled + ",grabscreen switch:" + HwSwingMotionGestureHub.this.mIsSwingGrabScreenEnabled + ",pushscreen switch:" + HwSwingMotionGestureHub.this.mIsSwingPushGestureEnabled + ",up register:" + HwSwingMotionGestureHub.this.mIsUpFenceRegistered + ",down register:" + HwSwingMotionGestureHub.this.mIsDownFenceRegistered + ",left register:" + HwSwingMotionGestureHub.this.mIsLeftFenceRegistered + ",right register:" + HwSwingMotionGestureHub.this.mIsRightFenceRegistered + ",push register:" + HwSwingMotionGestureHub.this.mIsPushFenceRegistered + ",fetch register:" + HwSwingMotionGestureHub.this.mIsFetchFenceRegistered);
            } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF.equals(action)) {
                Log.i(HwSwingMotionGestureHub.TAG, "hover switch:" + HwSwingMotionGestureHub.this.mIsSwingHoverGestureEnabled + ",hover register:" + HwSwingMotionGestureHub.this.mIsHoverFenceRegistered);
                HwSwingMotionGestureHub.this.mHoverScreenOffTimeMs = SystemClock.uptimeMillis();
                if (HwSwingMotionGestureHub.this.mHoverScreenOffTimeMs - HwSwingMotionGestureHub.this.mHoverScreenOnTimeMs > 0 && HwSwingMotionGestureHub.this.mHoverScreenOffTimeMs - HwSwingMotionGestureHub.this.mHoverScreenOnTimeMs < HwSwingMotionGestureConstant.HOVER_SCREEN_OFF_THRESHOLD) {
                    HwSwingMotionGestureHub.this.reportMotionEventIfNeeded(HwSwingMotionGestureConstant.VALUE_HOVER_SCREEN_OFF);
                }
            }
        }
    }

    private boolean isTalkBackServicesOn() {
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), ACCESSIBILITY_SCREENREADER_ENABLED, 0, -2) == 1) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNeedRegisterAwareness() {
        if (this.mIsSuperPowerMode) {
            Log.i(TAG, "current is super power mode");
            return false;
        } else if (this.mIsSwingGrabScreenEnabled || this.mIsSwingSlideScreenEnabled || this.mIsSwingPushGestureEnabled || this.mIsSwingHoverGestureEnabled) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.swing.HwSwingMotionGestureBaseHub
    public void refreshAwarenessConnection() {
        super.refreshAwarenessConnection();
        refreshSystemUiConnection();
        if (!isNeedRegisterAwareness()) {
            disableSwingMotionDispatch();
            if (this.mIsAwarenessConnected) {
                boolean isSuccess = this.mAwarenessManager.disconnectService();
                Log.w(TAG, "disconnectService success:" + isSuccess);
            }
        } else if (this.mIsAwarenessConnected) {
            lambda$notifyFocusChange$0$HwSwingMotionGestureHub();
        } else if (!this.mAwarenessManager.connectService(this.mAwarenessServiceConnection)) {
            Log.w(TAG, "connectService failed");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* renamed from: refreshMotionAwarenessConnection */
    public void lambda$notifyFocusChange$0$HwSwingMotionGestureHub() {
        if (!this.mIsAwarenessConnected) {
            Log.w(TAG, "refreshMotionAwarenessConnection service not connected");
            return;
        }
        refreshMotionUpAwarenessFence(this.mIsSwingSlideScreenEnabled);
        refreshMotionDownAwarenessFence(this.mIsSwingSlideScreenEnabled);
        boolean z = true;
        refreshMotionLeftAwarenessFence(this.mIsSwingSlideScreenEnabled && this.mIsLeftRightGestureEnabled);
        refreshMotionRightAwarenessFence(this.mIsSwingSlideScreenEnabled && this.mIsLeftRightGestureEnabled);
        refreshMotionPushAwarenessFence(this.mIsSwingPushGestureEnabled);
        refreshMotionFetchAwarenessFence(this.mIsSwingGrabScreenEnabled);
        refreshMotionHoverAwarenessFence(this.mIsSwingHoverGestureEnabled);
        refreshMotionStartUpAwarenessFence(this.mIsSwingSlideScreenEnabled || this.mIsSwingGrabScreenEnabled || this.mIsSwingPushGestureEnabled);
        refreshMotionStartDownAwarenessFence(this.mIsSwingSlideScreenEnabled);
        refreshMotionStartLeftAwarenessFence(this.mIsSwingSlideScreenEnabled && this.mIsLeftRightGestureEnabled);
        if (!this.mIsSwingSlideScreenEnabled || !this.mIsLeftRightGestureEnabled) {
            z = false;
        }
        refreshMotionStartRightAwarenessFence(z);
    }

    private void refreshSystemUiConnection() {
        if (this.mIsSwingPushGestureEnabled || this.mIsSwingGrabScreenEnabled || this.mIsSwingSlideScreenEnabled) {
            this.mSwingEventNotifierUtil.bindSystemUiSwingEventService();
        } else {
            this.mSwingEventNotifierUtil.unbindSystemUiSwingEventService();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disableSwingMotionDispatch() {
        Log.w(TAG, "disableSwingMotionDispatch isAwarenessConnected:" + this.mIsAwarenessConnected);
        if (this.mIsAwarenessConnected) {
            refreshMotionUpAwarenessFence(false);
            refreshMotionDownAwarenessFence(false);
            refreshMotionLeftAwarenessFence(false);
            refreshMotionRightAwarenessFence(false);
            refreshMotionPushAwarenessFence(false);
            refreshMotionFetchAwarenessFence(false);
            refreshMotionHoverAwarenessFence(false);
            refreshMotionStartUpAwarenessFence(false);
            refreshMotionStartDownAwarenessFence(false);
            return;
        }
        this.mIsUpFenceRegistered = false;
        this.mIsDownFenceRegistered = false;
        this.mIsLeftFenceRegistered = false;
        this.mIsRightFenceRegistered = false;
        this.mIsHoverFenceRegistered = false;
        this.mIsFetchFenceRegistered = false;
        this.mIsPushFenceRegistered = false;
        this.mIsStartUpFenceRegistered = false;
        this.mIsStartDownFenceRegistered = false;
    }

    private void refreshMotionUpAwarenessFence(boolean isEnabled) {
        if (!this.mIsUpFenceSupport) {
            Log.w(TAG, "swing up gesture is not support");
        } else if (isEnabled) {
            if (this.mIsUpFenceRegistered) {
                Log.w(TAG, "swing up gesture already registered");
                return;
            }
            this.mIsUpFenceRegistered = this.mAwarenessManager.dispatch(AwarenessRequest.registerFence(this.mMotionUpAwarenessFence, this.mOnEnvelopeReceiver).addOnResultListener(this.mOnResultListener));
            Log.i(TAG, "swing up gesture register result:" + this.mIsUpFenceRegistered);
        } else if (!this.mIsUpFenceRegistered) {
            Log.w(TAG, "swing up gesture not registered yet");
        } else {
            this.mAwarenessManager.dispatch(AwarenessRequest.unregisterFence(this.mMotionUpAwarenessFence).addOnResultListener(this.mOnResultListener));
            Log.i(TAG, "swing up gesture unregister");
            this.mIsUpFenceRegistered = false;
        }
    }

    private void refreshMotionStartUpAwarenessFence(boolean isEnabled) {
        if (!this.mIsUpFenceSupport) {
            Log.w(TAG, "swing up gesture is not support");
        } else if (isEnabled) {
            if (this.mIsStartUpFenceRegistered) {
                Log.w(TAG, "swing startup gesture already registered");
                return;
            }
            this.mIsStartUpFenceRegistered = this.mAwarenessManager.dispatch(AwarenessRequest.registerFence(this.mMotionStartUpAwarenessFence, this.mOnEnvelopeReceiver).addOnResultListener(this.mOnResultListener));
            Log.i(TAG, "swing startup gesture register result:" + this.mIsStartUpFenceRegistered);
        } else if (!this.mIsStartUpFenceRegistered) {
            Log.i(TAG, "swing startup gesture not registered yet");
        } else {
            this.mAwarenessManager.dispatch(AwarenessRequest.unregisterFence(this.mMotionStartUpAwarenessFence).addOnResultListener(this.mOnResultListener));
            Log.i(TAG, "swing startup gesture unregister");
            this.mIsStartUpFenceRegistered = false;
        }
    }

    private void refreshMotionDownAwarenessFence(boolean isEnabled) {
        if (!this.mIsDownFenceSupport) {
            Log.w(TAG, "swing down gesture is not support");
        } else if (isEnabled) {
            if (this.mIsDownFenceRegistered) {
                Log.w(TAG, "swing down gesture already registered");
                return;
            }
            this.mIsDownFenceRegistered = this.mAwarenessManager.dispatch(AwarenessRequest.registerFence(this.mMotionDownAwarenessFence, this.mOnEnvelopeReceiver).addOnResultListener(this.mOnResultListener));
            Log.i(TAG, "swing down gesture register result:" + this.mIsDownFenceRegistered);
        } else if (!this.mIsDownFenceRegistered) {
            Log.w(TAG, "swing down gesture not registered yet");
        } else {
            this.mAwarenessManager.dispatch(AwarenessRequest.unregisterFence(this.mMotionDownAwarenessFence).addOnResultListener(this.mOnResultListener));
            Log.i(TAG, "swing down gesture unregister");
            this.mIsDownFenceRegistered = false;
        }
    }

    private void refreshMotionStartDownAwarenessFence(boolean isEnabled) {
        if (!this.mIsDownFenceSupport) {
            Log.w(TAG, "swing down gesture is not support");
        } else if (isEnabled) {
            if (this.mIsStartDownFenceRegistered) {
                Log.w(TAG, "swing startdown gesture already registered");
                return;
            }
            this.mIsStartDownFenceRegistered = this.mAwarenessManager.dispatch(AwarenessRequest.registerFence(this.mMotionStartDownAwarenessFence, this.mOnEnvelopeReceiver).addOnResultListener(this.mOnResultListener));
            Log.i(TAG, "swing startdown gesture register result:" + this.mIsStartDownFenceRegistered);
        } else if (!this.mIsStartDownFenceRegistered) {
            Log.i(TAG, "swing startdown gesture not registered yet");
        } else {
            this.mAwarenessManager.dispatch(AwarenessRequest.unregisterFence(this.mMotionStartDownAwarenessFence).addOnResultListener(this.mOnResultListener));
            Log.i(TAG, "swing startdown gesture unregister");
            this.mIsStartDownFenceRegistered = false;
        }
    }

    private void refreshMotionLeftAwarenessFence(boolean isEnabled) {
        if (!this.mIsLeftFenceSupport) {
            Log.w(TAG, "swing left gesture is not support");
        } else if (isEnabled) {
            if (this.mIsLeftFenceRegistered) {
                Log.w(TAG, "swing left gesture already registered");
                return;
            }
            this.mIsLeftFenceRegistered = this.mAwarenessManager.dispatch(AwarenessRequest.registerFence(this.mMotionLeftAwarenessFence, this.mOnEnvelopeReceiver).addOnResultListener(this.mOnResultListener));
            Log.i(TAG, "swing left gesture register result:" + this.mIsLeftFenceRegistered);
        } else if (!this.mIsLeftFenceRegistered) {
            Log.w(TAG, "swing left gesture not registered yet");
        } else {
            this.mAwarenessManager.dispatch(AwarenessRequest.unregisterFence(this.mMotionLeftAwarenessFence).addOnResultListener(this.mOnResultListener));
            Log.i(TAG, "swing left gesture unregister");
            this.mIsLeftFenceRegistered = false;
        }
    }

    private void refreshMotionStartLeftAwarenessFence(boolean isEnabled) {
        if (!this.mIsLeftFenceSupport) {
            Log.w(TAG, "swing left gesture is not support");
        } else if (isEnabled) {
            if (this.mIsStartLeftFenceRegistered) {
                Log.w(TAG, "swing startleft gesture already registered");
                return;
            }
            this.mIsStartLeftFenceRegistered = this.mAwarenessManager.dispatch(AwarenessRequest.registerFence(this.mMotionStartLeftAwarenessFence, this.mOnEnvelopeReceiver).addOnResultListener(this.mOnResultListener));
            Log.i(TAG, "swing startleft gesture register result:" + this.mIsStartLeftFenceRegistered);
        } else if (!this.mIsStartLeftFenceRegistered) {
            Log.i(TAG, "swing startleft gesture not registered yet");
        } else {
            this.mAwarenessManager.dispatch(AwarenessRequest.unregisterFence(this.mMotionStartLeftAwarenessFence).addOnResultListener(this.mOnResultListener));
            Log.i(TAG, "swing startleft gesture unregister");
            this.mIsStartLeftFenceRegistered = false;
        }
    }

    private void refreshMotionRightAwarenessFence(boolean isEnabled) {
        if (!this.mIsRightFenceSupport) {
            Log.w(TAG, "swing right gesture is not support");
        } else if (isEnabled) {
            if (this.mIsRightFenceRegistered) {
                Log.w(TAG, "swing right gesture already registered");
                return;
            }
            this.mIsRightFenceRegistered = this.mAwarenessManager.dispatch(AwarenessRequest.registerFence(this.mMotionRightAwarenessFence, this.mOnEnvelopeReceiver).addOnResultListener(this.mOnResultListener));
            Log.i(TAG, "swing right gesture register result:" + this.mIsRightFenceRegistered);
        } else if (!this.mIsRightFenceRegistered) {
            Log.w(TAG, "swing right gesture not registered yet");
        } else {
            this.mAwarenessManager.dispatch(AwarenessRequest.unregisterFence(this.mMotionRightAwarenessFence).addOnResultListener(this.mOnResultListener));
            Log.i(TAG, "swing right gesture unregister");
            this.mIsRightFenceRegistered = false;
        }
    }

    private void refreshMotionStartRightAwarenessFence(boolean isEnabled) {
        if (!this.mIsRightFenceSupport) {
            Log.w(TAG, "swing right gesture is not support");
        } else if (isEnabled) {
            if (this.mIsStartRightFenceRegistered) {
                Log.w(TAG, "swing startright gesture already registered");
                return;
            }
            this.mIsStartRightFenceRegistered = this.mAwarenessManager.dispatch(AwarenessRequest.registerFence(this.mMotionStartRightAwarenessFence, this.mOnEnvelopeReceiver).addOnResultListener(this.mOnResultListener));
            Log.i(TAG, "swing startright gesture register result:" + this.mIsStartRightFenceRegistered);
        } else if (!this.mIsStartRightFenceRegistered) {
            Log.i(TAG, "swing startright gesture not registered yet");
        } else {
            this.mAwarenessManager.dispatch(AwarenessRequest.unregisterFence(this.mMotionStartRightAwarenessFence).addOnResultListener(this.mOnResultListener));
            Log.i(TAG, "swing startright gesture unregister");
            this.mIsStartRightFenceRegistered = false;
        }
    }

    private void refreshMotionPushAwarenessFence(boolean isEnabled) {
        if (!this.mIsPushFenceSupport) {
            Log.w(TAG, "swing push gesture is not support");
        } else if (isEnabled) {
            if (this.mIsPushFenceRegistered) {
                Log.w(TAG, "swing push gesture already registered");
                return;
            }
            this.mIsPushFenceRegistered = this.mAwarenessManager.dispatch(AwarenessRequest.registerFence(this.mMotionPushAwarenessFence, this.mOnEnvelopeReceiver).addOnResultListener(this.mOnResultListener));
            Log.i(TAG, "swing push gesture register result:" + this.mIsPushFenceRegistered);
        } else if (!this.mIsPushFenceRegistered) {
            Log.w(TAG, "swing push gesture not registered yet");
        } else {
            this.mAwarenessManager.dispatch(AwarenessRequest.unregisterFence(this.mMotionPushAwarenessFence));
            Log.i(TAG, "swing push gesture unregister");
            this.mIsPushFenceRegistered = false;
        }
    }

    private void refreshMotionFetchAwarenessFence(boolean isEnabled) {
        if (!this.mIsFetchFenceSupport) {
            Log.w(TAG, "swing fetch gesture is not support");
        } else if (isEnabled) {
            if (this.mIsFetchFenceRegistered) {
                Log.w(TAG, "swing fetch gesture already registered");
                return;
            }
            this.mIsFetchFenceRegistered = this.mAwarenessManager.dispatch(AwarenessRequest.registerFence(this.mMotionFetchAwarenessFence, this.mOnEnvelopeReceiver).addOnResultListener(this.mOnResultListener));
            Log.i(TAG, "swing fetch gesture register result:" + this.mIsFetchFenceRegistered);
        } else if (!this.mIsFetchFenceRegistered) {
            Log.w(TAG, "swing fetch gesture not registered yet");
        } else {
            this.mAwarenessManager.dispatch(AwarenessRequest.unregisterFence(this.mMotionFetchAwarenessFence));
            Log.i(TAG, "swing fetch gesture unregister");
            this.mIsFetchFenceRegistered = false;
        }
    }

    private void refreshMotionHoverAwarenessFence(boolean isEnabled) {
        if (!this.mIsHoverFenceSupport) {
            Log.w(TAG, "swing hover gesture is not support");
        } else if (isEnabled) {
            if (this.mIsHoverFenceRegistered) {
                Log.w(TAG, "swing hover gesture already registered");
                return;
            }
            this.mIsHoverFenceRegistered = this.mAwarenessManager.dispatch(AwarenessRequest.registerFence(this.mMotionHoverAwarenessFence, this.mOnEnvelopeReceiver).addOnResultListener(this.mOnResultListener));
            Log.i(TAG, "swing hover gesture register result:" + this.mIsHoverFenceRegistered);
        } else if (!this.mIsHoverFenceRegistered) {
            Log.w(TAG, "swing hover gesture not registered yet");
        } else {
            this.mAwarenessManager.dispatch(AwarenessRequest.unregisterFence(this.mMotionHoverAwarenessFence).addOnResultListener(this.mOnResultListener));
            Log.i(TAG, "swing hover gesture unregister");
            this.mIsHoverFenceRegistered = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchMotionGesture(String motionGesture) {
        if (!isStartMotionGesture(motionGesture)) {
            if (checkSwingPermission()) {
                Log.i(TAG, "SWING_PERMISSION is granted, dispatch event to application");
                this.mMotionGestureDispatcher.dispatchMotionGesture(motionGesture);
                return;
            }
            if (HwSwingMotionGestureConstant.VALUE_SLIDE_UP.equals(motionGesture)) {
                Log.i(TAG, "SWING_PERMISSION is not granted, slide up directly");
                this.mMotionGestureHandler.emulateSwingTouch(3);
            } else if (HwSwingMotionGestureConstant.VALUE_SLIDE_DOWN.equals(motionGesture)) {
                Log.i(TAG, "SWING_PERMISSION is not granted, slide down directly");
                this.mMotionGestureHandler.emulateSwingTouch(4);
            } else if (HwSwingMotionGestureConstant.VALUE_SLIDE_LEFT.equals(motionGesture)) {
                Log.i(TAG, "SWING_PERMISSION is not granted, slide left directly");
                this.mMotionGestureHandler.emulateSwingTouch(1);
            } else if (HwSwingMotionGestureConstant.VALUE_SLIDE_RIGHT.equals(motionGesture)) {
                Log.i(TAG, "SWING_PERMISSION is not granted, slide right directly");
                this.mMotionGestureHandler.emulateSwingTouch(2);
            } else if (HwSwingMotionGestureConstant.VALUE_PUSH.equals(motionGesture)) {
                Log.i(TAG, "SWING_PERMISSION is not granted, push directly");
                this.mMotionGestureHandler.lambda$dispatchUnhandledKey$4$HwSwingMotionGestureHandler();
            }
            reportMotionResponceIfNeeded(motionGesture);
        }
    }

    private boolean isStartMotionGesture(String motionGesture) {
        for (String startGesture : new String[]{HwSwingMotionGestureConstant.VALUE_START_UP, HwSwingMotionGestureConstant.VALUE_START_DOWN, HwSwingMotionGestureConstant.VALUE_START_LEFT, HwSwingMotionGestureConstant.VALUE_START_RIGHT}) {
            if (startGesture.equals(motionGesture)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkSwingPermission() {
        String focusPkgName = this.mFocusPkgName;
        if (focusPkgName == null) {
            Log.w(TAG, "focus package name is null");
            return false;
        }
        Log.i(TAG, "current focus package:" + focusPkgName);
        PackageManager packageManager = this.mContext.getPackageManager();
        if (packageManager == null) {
            Log.e(TAG, "packageManager is null");
            return false;
        } else if (packageManager.checkPermission(SWING_MOTION_PERMISSION, focusPkgName) == 0) {
            return true;
        } else {
            Log.w(TAG, "current top activity has no permission");
            return false;
        }
    }

    /* JADX INFO: finally extract failed */
    private void fetchGestureResponse(Context context) {
        Log.i(TAG, "Fetch gesture has Response !");
        long token = Binder.clearCallingIdentity();
        try {
            new ScreenshotHelper(context).takeScreenshot(1, true, true, this.mAwarenessHandler);
            Binder.restoreCallingIdentity(token);
            HwSwingDecisionUtil.reportEvent(context, HwSwingDecisionUtil.SWING_GRAB_SCREEN_SHOT_TYPE);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    private void hoverGestureResponse() {
        if (this.mPowerManager == null) {
            Log.e(TAG, "power manager is null");
            return;
        }
        long token = Binder.clearCallingIdentity();
        try {
            this.mHoverScreenOnTimeMs = SystemClock.uptimeMillis();
            this.mPowerManager.wakeUp(this.mHoverScreenOnTimeMs, REASON_SWING_HOVERGESTURE);
            doKeyguardFaceRecognize();
            Log.i(TAG, "Hover gesture has Response");
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void doKeyguardFaceRecognize() {
        Log.i(TAG, "doKeyguardFaceRecognize");
        Bundle paramBundle = new Bundle();
        paramBundle.putString(AppActConstant.ATTR_PACKAGE_NAME, "com.huawei.systemserver");
        paramBundle.putString("reason", REASON_SWING_HOVERGESTURE);
        try {
            this.mContext.getContentResolver().call(Uri.parse(KEYGUARD_URI), "doKeyguardFaceRecognize", (String) null, paramBundle);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "doKeyguardFaceRecognize illeagal argument exception");
        } catch (SecurityException e2) {
            Log.e(TAG, "doKeyguardFaceRecognize securitiy exception");
        } catch (Exception e3) {
            Log.e(TAG, "doKeyguardFaceRecognize throw exception");
        }
    }

    private void sendBroadcastToMotionGesture(Context context) {
        Log.i(TAG, "send Broadcast To MotionGesture !");
        Intent intent = new Intent();
        intent.setAction("com.huawei.motionservice.SEND_SWING_GRAB_SCREEN_SHOT_EVENT");
        intent.setPackage("com.huawei.motionservice");
        context.sendBroadcastAsUser(intent, UserHandle.ALL, PERMISSION_SWING_GRAB_SCREEN_SHOT);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFenceResult(AwarenessFence fence) {
        if (fence == null || fence.getState() == null) {
            Log.e(TAG, "handleFenceResult fence is null");
        } else if (fence.getState().getCurrentState() == 1) {
            Bundle bundle = fence.getState().getExtras();
            if (bundle == null) {
                Log.e(TAG, "handleFenceResult bundle is null");
                return;
            }
            final String motionGesture = bundle.getString(HwSwingMotionGestureConstant.KEY_MOTION_GESTURE);
            if (motionGesture != null) {
                this.mAwarenessHandler.post(new Runnable() {
                    /* class com.android.server.swing.HwSwingMotionGestureHub.AnonymousClass5 */

                    @Override // java.lang.Runnable
                    public void run() {
                        HwSwingMotionGestureHub.this.reportMotionEventIfNeeded(motionGesture);
                    }
                });
                if (!motionGesture.equals(this.mLastSwingMotionGesture)) {
                    Log.i(TAG, "Awareness handleFenceResult, motionGesture = " + motionGesture);
                    this.mLastSwingMotionGesture = motionGesture;
                }
                if (HwSwingMotionGestureConstant.VALUE_HOVER.equals(motionGesture)) {
                    hoverGestureResponse();
                } else if (isTalkBackServicesOn()) {
                    Log.i(TAG, "not dispatchMotionGesture in talkback mode");
                } else {
                    notifySystemUiIfNeeded(motionGesture);
                    if (HwSwingMotionGestureConstant.VALUE_FETCH.equals(motionGesture) && !this.mIsCurrentSwingMotionGuide) {
                        fetchGestureResponse(this.mContext);
                    }
                    this.mAwarenessHandler.post(new Runnable() {
                        /* class com.android.server.swing.HwSwingMotionGestureHub.AnonymousClass6 */

                        @Override // java.lang.Runnable
                        public void run() {
                            HwSwingMotionGestureHub.this.dispatchMotionGesture(motionGesture);
                        }
                    });
                }
            }
        }
    }

    private void notifySystemUiIfNeeded(String motionGesture) {
        if (!this.mIsCurrentSwingMotionGuide) {
            IHwSwingEventNotifier hwSwingEventNotifier = this.mSwingEventNotifierUtil.getHwSwingEventNotifier();
            if (hwSwingEventNotifier == null) {
                Log.w(TAG, "systemui service not connected");
                return;
            }
            try {
                hwSwingEventNotifier.swingMotionGesture(motionGesture);
            } catch (RemoteException e) {
                Log.e(TAG, "notify systemui motionGesture error");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportFenceResultByKeyEvent(KeyEvent event) {
        String motionGesture = "";
        switch (event.getKeyCode()) {
            case 710:
                motionGesture = HwSwingMotionGestureConstant.VALUE_SLIDE_LEFT;
                break;
            case 711:
                motionGesture = HwSwingMotionGestureConstant.VALUE_SLIDE_RIGHT;
                break;
            case 712:
                motionGesture = HwSwingMotionGestureConstant.VALUE_SLIDE_UP;
                break;
            case 713:
                motionGesture = HwSwingMotionGestureConstant.VALUE_SLIDE_DOWN;
                break;
            case 714:
                motionGesture = HwSwingMotionGestureConstant.VALUE_PUSH;
                break;
        }
        reportMotionResponceIfNeeded(motionGesture);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportMotionEventIfNeeded(String motionGesture) {
        if (HwSwingMotionGestureConstant.VALUE_SLIDE_LEFT.equals(motionGesture)) {
            this.mLeftActionCount++;
            if (HwSwingReport.reportMotionEventAction(motionGesture, this.mLeftActionCount, true)) {
                this.mLeftActionCount = 0;
            }
        } else if (HwSwingMotionGestureConstant.VALUE_SLIDE_RIGHT.equals(motionGesture)) {
            this.mRightActionCount++;
            if (HwSwingReport.reportMotionEventAction(motionGesture, this.mRightActionCount, true)) {
                this.mRightActionCount = 0;
            }
        } else if (HwSwingMotionGestureConstant.VALUE_SLIDE_UP.equals(motionGesture)) {
            this.mUpActionCount++;
            if (HwSwingReport.reportMotionEventAction(motionGesture, this.mUpActionCount, true)) {
                this.mUpActionCount = 0;
            }
        } else if (HwSwingMotionGestureConstant.VALUE_SLIDE_DOWN.equals(motionGesture)) {
            this.mDownActionCount++;
            if (HwSwingReport.reportMotionEventAction(motionGesture, this.mDownActionCount, true)) {
                this.mDownActionCount = 0;
            }
        } else if (HwSwingMotionGestureConstant.VALUE_PUSH.equals(motionGesture)) {
            HwSwingReport.reportMotionEventActionTimely(motionGesture, true);
        } else if (HwSwingMotionGestureConstant.VALUE_FETCH.equals(motionGesture)) {
            HwSwingReport.reportMotionEventActionTimely(motionGesture, true);
        } else if (HwSwingMotionGestureConstant.VALUE_HOVER.equals(motionGesture)) {
            HwSwingReport.reportMotionEventActionTimely(HwSwingMotionGestureConstant.VALUE_HOVER_SCREEN_ON, false);
        } else if (HwSwingMotionGestureConstant.VALUE_HOVER_SCREEN_OFF.equals(motionGesture)) {
            HwSwingReport.reportMotionEventActionTimely(HwSwingMotionGestureConstant.VALUE_HOVER_SCREEN_OFF, false);
        }
    }

    private void reportMotionResponceIfNeeded(String motionGesture) {
        if (HwSwingMotionGestureConstant.VALUE_SLIDE_LEFT.equals(motionGesture)) {
            this.mLeftResponseCount++;
            if (HwSwingReport.reportMotionEventResponse(motionGesture, this.mLeftResponseCount, true)) {
                this.mLeftResponseCount = 0;
            }
        } else if (HwSwingMotionGestureConstant.VALUE_SLIDE_RIGHT.equals(motionGesture)) {
            this.mRightResponseCount++;
            if (HwSwingReport.reportMotionEventResponse(motionGesture, this.mRightResponseCount, true)) {
                this.mRightResponseCount = 0;
            }
        } else if (HwSwingMotionGestureConstant.VALUE_SLIDE_UP.equals(motionGesture)) {
            this.mUpResponseCount++;
            if (HwSwingReport.reportMotionEventResponse(motionGesture, this.mUpResponseCount, true)) {
                this.mUpResponseCount = 0;
            }
        } else if (HwSwingMotionGestureConstant.VALUE_SLIDE_DOWN.equals(motionGesture)) {
            this.mDownResponseCount++;
            if (HwSwingReport.reportMotionEventResponse(motionGesture, this.mDownResponseCount, true)) {
                this.mDownResponseCount = 0;
            }
        } else if (HwSwingMotionGestureConstant.VALUE_PUSH.equals(motionGesture)) {
            HwSwingReport.reportMotionEventResponseTimely(motionGesture, true);
        }
    }
}
