package com.huawei.server.fsm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Point;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.hardware.display.HwFoldScreenState;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.CoordinationModeUtils;
import android.util.Flog;
import android.util.IMonitor;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicyEx;
import com.android.server.wm.ActivityTaskManagerServiceEx;
import com.android.server.wm.WindowManagerInternalEx;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.android.content.ContextEx;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import com.huawei.android.fsm.IFoldDisplayModeListener;
import com.huawei.android.fsm.IFoldFsmTipsRequestListener;
import com.huawei.android.fsm.IFoldableStateListener;
import com.huawei.android.fsm.IHwFoldScreenManagerEx;
import com.huawei.android.hardware.SensorManagerEx;
import com.huawei.android.hardware.display.DisplayManagerInternalEx;
import com.huawei.android.internal.util.DumpUtilsEx;
import com.huawei.android.os.HandlerAdapter;
import com.huawei.android.os.PowerManagerEx;
import com.huawei.android.os.PowerManagerInternalEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.pgmng.plug.PowerKit;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.server.WatchdogEx;
import com.huawei.android.util.HwLogEx;
import com.huawei.android.util.SlogEx;
import com.huawei.hwpartfoldscreenopt.BuildConfig;
import com.huawei.server.ServiceThreadExt;
import huawei.android.hardware.tp.HwTpManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public final class HwFoldScreenManagerService extends DefaultHwFoldScreenManagerService {
    private static final String ACTION_SMART_NOTIFY_FAULT = "huawei.intent.action.SMART_NOTIFY_FAULT";
    private static final String ACTION_THERMAL_LOW_TEMP_WARNING = "huawei.intent.action.THERMAL_LOW_TEMP_WARNING";
    private static final int BAT0_TEMP_FLAG = 33;
    private static final int CAMERA_STATE_OFF = 2;
    private static final int CAMERA_STATE_ON = 1;
    private static final int DISPLAY_LOCK_MODE = 1;
    private static final int DISPLAY_UNLOCK_MODE = 0;
    private static final int ENTER_LOW_TEMP = 1;
    private static final String FRONT_CAMERA = "1";
    private static final int FSM_FORCE_WAKEUP_TIMEOUT = 1500;
    private static final int FSM_FREEZE_FOLD_ROTATION_CMD = 8;
    private static final int FSM_MAGNETOMETER_TURNOFF_SENSOR_CMD = 4;
    private static final int FSM_MAGNETOMETER_TURNOFF_SENSOR_TIMEOUT = 5000;
    private static final int FSM_NOTIFY_DISPLAYMODE_CHANGE_CMD = 2;
    private static final int FSM_NOTIFY_FOLDSTATE_CHANGE_CMD = 1;
    private static final int FSM_NOTIFY_POSTURE_CHANGE_CMD = 0;
    private static final int FSM_SWITCH_DISP_MODE_FROM_RESUME_CMD = 7;
    private static final int FSM_SWITCH_FOREGROUND_USER_CMD = 6;
    private static final int FSM_UNFREEZE_FOLD_ROTATION_CMD = 9;
    private static final String HW_FOLD_DISPLAY_MODE = "hw_fold_display_mode";
    private static final String HW_FOLD_DISPLAY_MODE_PREPARE = "hw_fold_display_mode_prepare";
    private static final String HW_FOLD_DISPMODE = "persist.sys.foldDispMode";
    private static final String HW_FOLD_SCREEN_COUNTER = "hw_fold_screen_counter";
    private static final String HW_FOLD_SCRREN_STATE = "hw_fold_screen_state";
    private static final int IMONITOR_TEMP_EVENT_ID = 936004016;
    private static final int INTELLIGENT_AWAKEN_ON = 1;
    private static boolean IS_FACTORY = "factory".equals(SystemPropertiesEx.get("ro.runmode", "normal"));
    private static final String KEY_FAULT_CODE = "FAULT_CODE";
    private static final String KEY_FAULT_DESCRIPTION = "FAULT_DESCRIPTION";
    private static final String KEY_FAULT_SUGGESTION = "FAULT_SUGGESTION";
    private static final String KEY_INTELLIGENT_AWAKEN = "intelligent_awaken_enabled";
    private static final int LEAVE_LOW_TEMP = 0;
    private static final String LOCK_DISPLAY_MODE = "lock_display_mode";
    private static final String LOW_TEMP_WARNING = "low_temp_warning";
    private static final int MAX_FOLD_CREEN_NUM = 500000;
    private static final int MSG_NOTIFY_INTELLIGENT_MODE_TO_TP = 10;
    private static final int MSG_SET_FOLD_DISPLAY_MODE_FINISHED = 11;
    private static final String PERMISSION_FOLD_SCREEN = "com.huawei.permission.MANAGE_FOLD_SCREEN";
    private static final String PERMISSION_FOLD_SCREEN_PERMISSION = "Requires MANAGE_FOLD_SCREEN permission";
    private static final String PERMISSION_FOLD_SCREEN_PRIVILEGED = "com.huawei.permission.MANAGE_FOLD_SCREEN_PRIVILEGED";
    private static final String REAR_CAMERA = "0";
    private static final String SMART_NOTIFY_FAULT_PERMISSION = "huawei.permission.SMART_NOTIFY_FAULT";
    private static final String TAG = "Fsm_FoldScreenManagerService";
    private static final String THERMAL_RECEIVE_PERMISSION = "com.huawei.thermal.receiverPermission";
    private static final String VALUE_FAULT_CODE = "642003014";
    private static final String VALUE_FAULT_DESCRIPTION = "842003014";
    private static final String VALUE_FAULT_SUGGESTION = "542003014";
    private static final int WAKE_REASON_FINGER_PRINT = 101;
    private CameraManager.AvailabilityCallback mCameraAvailableCallback;
    private CameraManager mCameraManager;
    private ContentObserver mContentObserver;
    private final Context mContext;
    private String mCurCameraId;
    private final Object mDeferDispLock = new Object();
    private int mDeferredDispMode;
    private int mDeferredDispModeChangeCount;
    private final RemoteCallbackList<IFoldDisplayModeListener> mDisplayModeListeners = new RemoteCallbackList<>();
    private DisplayManagerInternalEx mDms;
    private HwFoldScreenManagerInternal.FoldScreenOnListener mFoldScreenOnListener;
    private int mFoldState;
    private final RemoteCallbackList<IFoldableStateListener> mFoldableStateListeners = new RemoteCallbackList<>();
    private final RemoteCallbackList<IFoldFsmTipsRequestListener> mFsmTipsRequestListeners = new RemoteCallbackList<>();
    private final HwFoldScreenManagerHandler mHandler;
    private final ServiceThreadExt mHandlerThread;
    private volatile boolean mIsCameraOn;
    private boolean mIsDisplayLocked;
    private volatile boolean mIsDrawingScreenOff;
    private volatile boolean mIsIntelligent;
    private boolean mIsLockDisplayModeEnable;
    private boolean mIsMagnetomerter;
    private int mLastDisplayMode;
    private final Object mLock = new Object();
    private ContentObserver mLockDisplayModeObserver;
    private BroadcastReceiver mLowTempWarningReceiver;
    private final PhoneStateListener mPhoneStateListener;
    private WindowManagerPolicyEx mPolicy;
    private final PostureStateMachine mPostureSM;
    private PowerManager mPowerManager;
    private PowerManagerInternalEx mPowerManagerInternal;
    private final PosturePreprocessManager mPreprocess;
    private int mScreenOffLowTemp;
    private int mScreenOnLowTemp;
    private SensorManager mSensorManager;
    private boolean mShouldNotifyLowTemp;
    private String mSpecifiedCameraId;
    private TelephonyManager mTelephonyManager;
    private HwTpManager mTpManager;
    private BroadcastReceiver mUserPresentReceiver;
    private final WindowManagerInternalEx mWm;

    /* JADX WARN: Type inference failed for: r14v0, types: [android.os.Handler, com.huawei.server.fsm.HwFoldScreenManagerService$HwFoldScreenManagerHandler] */
    /* JADX WARN: Type inference failed for: r7v2, types: [android.os.Handler, com.huawei.server.fsm.HwFoldScreenManagerService$HwFoldScreenManagerHandler] */
    /* JADX WARNING: Unknown variable types count: 2 */
    public HwFoldScreenManagerService(Context context) {
        super(context);
        boolean z = false;
        this.mScreenOnLowTemp = 0;
        this.mScreenOffLowTemp = 0;
        this.mShouldNotifyLowTemp = false;
        this.mIsIntelligent = true;
        this.mIsDisplayLocked = false;
        this.mIsMagnetomerter = false;
        this.mIsCameraOn = false;
        this.mIsDrawingScreenOff = false;
        this.mCurCameraId = null;
        this.mSpecifiedCameraId = null;
        this.mLastDisplayMode = 0;
        this.mDeferredDispModeChangeCount = 0;
        this.mDeferredDispMode = 0;
        this.mIsLockDisplayModeEnable = false;
        this.mPowerManagerInternal = PowerManagerInternalEx.getInstance();
        this.mCameraAvailableCallback = new CameraManager.AvailabilityCallback() {
            /* class com.huawei.server.fsm.HwFoldScreenManagerService.AnonymousClass1 */

            @Override // android.hardware.camera2.CameraManager.AvailabilityCallback
            public void onCameraAvailable(String cameraId) {
                HwFoldScreenManagerService.this.processCameraState(cameraId, 2);
                if (HwFoldScreenManagerService.REAR_CAMERA.equals(cameraId) || HwFoldScreenManagerService.FRONT_CAMERA.equals(cameraId)) {
                    HwFoldScreenManagerService.this.mIsCameraOn = false;
                    HwFoldScreenManagerService.this.mCurCameraId = null;
                }
            }

            @Override // android.hardware.camera2.CameraManager.AvailabilityCallback
            public void onCameraUnavailable(String cameraId) {
                HwFoldScreenManagerService.this.processCameraState(cameraId, 1);
                if (HwFoldScreenManagerService.REAR_CAMERA.equals(cameraId) || HwFoldScreenManagerService.FRONT_CAMERA.equals(cameraId)) {
                    HwFoldScreenManagerService.this.mIsCameraOn = true;
                    HwFoldScreenManagerService.this.mCurCameraId = cameraId;
                }
            }
        };
        this.mContentObserver = new ContentObserver(new Handler()) {
            /* class com.huawei.server.fsm.HwFoldScreenManagerService.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                int intelligent = SettingsEx.Secure.getIntForUser(HwFoldScreenManagerService.this.mContext.getContentResolver(), HwFoldScreenManagerService.KEY_INTELLIGENT_AWAKEN, 1, -2);
                HwFoldScreenManagerService.this.changeIntelligentMode(intelligent);
                if (intelligent != 1) {
                    return;
                }
                if (!HwFoldScreenManagerService.this.isIncall()) {
                    PostureStateMachine postureStateMachine = HwFoldScreenManagerService.this.mPostureSM;
                    PostureStateMachine unused = HwFoldScreenManagerService.this.mPostureSM;
                    postureStateMachine.setScreeStateWhenCallComing(0);
                } else if (HwFoldScreenManagerService.this.isScreenOn()) {
                    PostureStateMachine postureStateMachine2 = HwFoldScreenManagerService.this.mPostureSM;
                    PostureStateMachine unused2 = HwFoldScreenManagerService.this.mPostureSM;
                    postureStateMachine2.setScreeStateWhenCallComing(1);
                } else {
                    PostureStateMachine postureStateMachine3 = HwFoldScreenManagerService.this.mPostureSM;
                    PostureStateMachine unused3 = HwFoldScreenManagerService.this.mPostureSM;
                    postureStateMachine3.setScreeStateWhenCallComing(2);
                }
            }
        };
        this.mLockDisplayModeObserver = new ContentObserver(new Handler()) {
            /* class com.huawei.server.fsm.HwFoldScreenManagerService.AnonymousClass3 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                boolean z = false;
                int lockDisplayMode = Settings.Global.getInt(HwFoldScreenManagerService.this.mContext.getContentResolver(), HwFoldScreenManagerService.LOCK_DISPLAY_MODE, 0);
                HwFoldScreenManagerService hwFoldScreenManagerService = HwFoldScreenManagerService.this;
                if (lockDisplayMode == 1) {
                    z = true;
                }
                hwFoldScreenManagerService.mIsLockDisplayModeEnable = z;
                SlogEx.i("Fsm_FoldScreenManagerService", "lockDisplayMode changed to: " + lockDisplayMode);
                if (!HwFoldScreenManagerService.this.mIsLockDisplayModeEnable) {
                    HwFoldScreenManagerService.this.unlockDisplayModeInner();
                }
            }
        };
        this.mLowTempWarningReceiver = new BroadcastReceiver() {
            /* class com.huawei.server.fsm.HwFoldScreenManagerService.AnonymousClass4 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    SlogEx.e("Fsm_FoldScreenManagerService", "lowTempWarningReceiver intent is null");
                    return;
                }
                HwFoldScreenManagerService.this.mScreenOnLowTemp = intent.getIntExtra(HwFoldScreenManagerService.LOW_TEMP_WARNING, 0);
                SlogEx.i("Fsm_FoldScreenManagerService", "on receive lowTempWarningReceiver, value = " + HwFoldScreenManagerService.this.mScreenOnLowTemp);
            }
        };
        this.mPhoneStateListener = new PhoneStateListener() {
            /* class com.huawei.server.fsm.HwFoldScreenManagerService.AnonymousClass5 */

            @Override // android.telephony.PhoneStateListener
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == 0) {
                    SlogEx.i("Fsm_FoldScreenManagerService", "setScreeStateWhenCallComing:CALL_STATE_IDLE");
                    PostureStateMachine postureStateMachine = HwFoldScreenManagerService.this.mPostureSM;
                    PostureStateMachine unused = HwFoldScreenManagerService.this.mPostureSM;
                    postureStateMachine.setScreeStateWhenCallComing(0);
                } else if (state == 1) {
                    SlogEx.i("Fsm_FoldScreenManagerService", "setScreeStateWhenCallComing:CALL_STATE_RINGING");
                    if (HwFoldScreenManagerService.this.isScreenOn()) {
                        PostureStateMachine postureStateMachine2 = HwFoldScreenManagerService.this.mPostureSM;
                        PostureStateMachine unused2 = HwFoldScreenManagerService.this.mPostureSM;
                        postureStateMachine2.setScreeStateWhenCallComing(1);
                    } else {
                        PostureStateMachine postureStateMachine3 = HwFoldScreenManagerService.this.mPostureSM;
                        PostureStateMachine unused3 = HwFoldScreenManagerService.this.mPostureSM;
                        postureStateMachine3.setScreeStateWhenCallComing(2);
                    }
                } else if (state != 2) {
                    SlogEx.e("Fsm_FoldScreenManagerService", "setScreeStateWhenCallComing error state=" + state);
                } else {
                    SlogEx.i("Fsm_FoldScreenManagerService", "setScreeStateWhenCallComing:CALL_STATE_OFFHOOK");
                    if (HwFoldScreenManagerService.this.isScreenOn()) {
                        PostureStateMachine postureStateMachine4 = HwFoldScreenManagerService.this.mPostureSM;
                        PostureStateMachine unused4 = HwFoldScreenManagerService.this.mPostureSM;
                        postureStateMachine4.setScreeStateWhenCallComing(1);
                    } else {
                        PostureStateMachine postureStateMachine5 = HwFoldScreenManagerService.this.mPostureSM;
                        PostureStateMachine unused5 = HwFoldScreenManagerService.this.mPostureSM;
                        postureStateMachine5.setScreeStateWhenCallComing(2);
                    }
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        this.mUserPresentReceiver = new BroadcastReceiver() {
            /* class com.huawei.server.fsm.HwFoldScreenManagerService.AnonymousClass6 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    SlogEx.e("Fsm_FoldScreenManagerService", "userPresentReceiver intent is null");
                    return;
                }
                SlogEx.i("Fsm_FoldScreenManagerService", "on receive userPresentReceiver");
                if (HwFoldScreenManagerService.this.mShouldNotifyLowTemp) {
                    HwFoldScreenManagerService.this.sendLowTempWarningBroadcast();
                    HwFoldScreenManagerService.this.mShouldNotifyLowTemp = false;
                }
            }
        };
        this.mContext = context;
        this.mHandlerThread = new ServiceThreadExt("Fsm_FoldScreenManagerService", -4, false);
        this.mHandlerThread.start();
        this.mHandler = new HwFoldScreenManagerHandler(this.mHandlerThread.getLooper());
        this.mPostureSM = PostureStateMachine.getInstance();
        this.mPostureSM.init(this);
        this.mIsIntelligent = SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), KEY_INTELLIGENT_AWAKEN, 1, -2) == 1;
        this.mPreprocess = PosturePreprocessManager.getInstance();
        this.mIsDisplayLocked = isLockedDisplayMode(SystemPropertiesEx.getInt(HW_FOLD_DISPMODE, 0));
        notifyIntelligentModeChangeToTp(this.mIsIntelligent);
        ContentResolverExt.registerContentObserver(this.mContext.getContentResolver(), Settings.Secure.getUriFor(KEY_INTELLIGENT_AWAKEN), true, this.mContentObserver, -1);
        this.mPolicy = WindowManagerPolicyEx.getInstance();
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mCameraManager = (CameraManager) this.mContext.getSystemService("camera");
        this.mWm = new WindowManagerInternalEx();
        ContextEx.registerReceiverAsUser(this.mContext, this.mLowTempWarningReceiver, UserHandleEx.ALL, new IntentFilter(ACTION_THERMAL_LOW_TEMP_WARNING), THERMAL_RECEIVE_PERMISSION, (Handler) this.mHandler);
        ContextEx.registerReceiverAsUser(this.mContext, this.mUserPresentReceiver, UserHandleEx.ALL, new IntentFilter("android.intent.action.USER_PRESENT"), (String) null, (Handler) this.mHandler);
        this.mFoldState = Settings.Secure.getInt(this.mContext.getContentResolver(), HW_FOLD_SCRREN_STATE, 0);
        this.mIsLockDisplayModeEnable = Settings.Global.getInt(this.mContext.getContentResolver(), LOCK_DISPLAY_MODE, 0) == 1 ? true : z;
        ContentResolverExt.registerContentObserver(this.mContext.getContentResolver(), Settings.Global.getUriFor(LOCK_DISPLAY_MODE), true, this.mLockDisplayModeObserver, -1);
    }

    /* access modifiers changed from: package-private */
    public Context getServiceContext() {
        return this.mContext;
    }

    public void setDrawWindowFlag(boolean isNeedDraw) {
        if (isScreenOnEary()) {
            SlogEx.i("Fsm_FoldScreenManagerService", "Current display is ScrrenON, return.");
            return;
        }
        this.mIsDrawingScreenOff = isNeedDraw;
        this.mPostureSM.setPosture(103);
    }

    public boolean isCameraOn() {
        return this.mIsCameraOn;
    }

    public boolean isFrontCameraOn() {
        if (this.mIsCameraOn) {
            SlogEx.i("Fsm_FoldScreenManagerService", "isFrontCameraOn, mCurCameraId = " + this.mCurCameraId);
            String str = this.mCurCameraId;
            if (str != null) {
                if (!FRONT_CAMERA.equals(str) && !FRONT_CAMERA.equals(this.mSpecifiedCameraId)) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public boolean isIntelligentOn() {
        return this.mIsIntelligent;
    }

    public boolean isScreenOn() {
        if (this.mPowerManager == null) {
            this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        }
        return this.mPowerManager.isScreenOn();
    }

    public boolean isIncall() {
        if (this.mTelephonyManager == null) {
            this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        }
        if (this.mTelephonyManager.getCallState() != 0) {
            return true;
        }
        return false;
    }

    /* JADX WARN: Type inference failed for: r0v2, types: [android.os.Handler, com.huawei.server.fsm.HwFoldScreenManagerService$HwFoldScreenManagerHandler] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void onStart() {
        addBinderService("fold_screen", new BinderService());
        addLocalService(HwFoldScreenManagerInternal.class, new LocalService());
        WatchdogEx.addMonitor(this);
        WatchdogEx.addThread((Handler) this.mHandler);
        CameraManager cameraManager = this.mCameraManager;
        if (cameraManager != null) {
            cameraManager.registerAvailabilityCallback(this.mCameraAvailableCallback, (Handler) null);
        }
        this.mPostureSM.start();
        this.mPreprocess.init(this.mContext, getPolicy());
        MagnetometerWakeupManager.getInstance(this.mContext).initSensorListener();
        if (this.mTelephonyManager == null) {
            this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        }
        this.mTelephonyManager.listen(this.mPhoneStateListener, 32);
    }

    public void onBootPhase(int phase) {
    }

    public void monitor() {
        synchronized (this) {
        }
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [android.os.Handler, com.huawei.server.fsm.HwFoldScreenManagerService$HwFoldScreenManagerHandler] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void onSwitchUser(int userHandle) {
        ?? r0 = this.mHandler;
        r0.sendMessage(Message.obtain(r0, FSM_SWITCH_FOREGROUND_USER_CMD, userHandle, 0));
        saveAndUpdateDisplayMode(getDisplayModeInner());
    }

    public void systemReady() {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processCameraState(String cameraId, int state) {
        SlogEx.i("Fsm_FoldScreenManagerService", "processCameraState cameraId: " + cameraId + " , state: " + state);
        if (this.mTpManager == null) {
            this.mTpManager = HwTpManager.getInstance();
        }
        if (state == 1) {
            this.mTpManager.hwTsSetAftConfig("version:3+camera_ON");
        } else {
            this.mTpManager.hwTsSetAftConfig("version:3+camera_OFF");
        }
    }

    private boolean isScreenOnEary() {
        WindowManagerPolicyEx windowManagerPolicyEx = this.mPolicy;
        if (windowManagerPolicyEx != null) {
            return windowManagerPolicyEx.isScreenOn();
        }
        return false;
    }

    private boolean isKeyguardLocked() {
        WindowManagerPolicyEx windowManagerPolicyEx = this.mPolicy;
        if (windowManagerPolicyEx != null) {
            return windowManagerPolicyEx.isKeyguardLocked();
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyFoldStateChangeInner(int foldState) {
        int i = this.mFoldableStateListeners.beginBroadcast();
        while (i > 0) {
            i--;
            IFoldableStateListener listener = this.mFoldableStateListeners.getBroadcastItem(i);
            Integer type = (Integer) this.mFoldableStateListeners.getBroadcastCookie(i);
            if (!(listener == null || type == null)) {
                Bundle extra = new Bundle();
                try {
                    if (type.intValue() == 1) {
                        extra.putInt("android.intent.extra.REASON", 1);
                        extra.putInt("fold_state", foldState);
                        listener.onStateChange(extra);
                    }
                } catch (RemoteException e) {
                    SlogEx.e("Fsm_FoldScreenManagerService", "notifyFoldStateChangeInner RemoteException");
                }
            }
        }
        this.mFoldableStateListeners.finishBroadcast();
        SlogEx.i("Fsm_FoldScreenManagerService", "notifyFoldStateChangeInner foldState : " + foldState);
        if (this.mFoldState != foldState) {
            if ((foldState == 1 || foldState == 2) && this.mScreenOnLowTemp == 1) {
                sendLowTempWarningBroadcast();
            }
            this.mFoldState = foldState;
            reportTempWhenFolding();
        }
    }

    private void reportTempWhenFolding() {
        PowerKit powerKit = PowerKit.getInstance();
        if (powerKit == null) {
            SlogEx.d("Fsm_FoldScreenManagerService", "powerKit is null");
            return;
        }
        try {
            int curTemp = powerKit.getThermalInfo(this.mContext, (int) BAT0_TEMP_FLAG);
            SlogEx.i("Fsm_FoldScreenManagerService", "report tmepture, curTemp = " + curTemp);
            IMonitor.EventStream stream = IMonitor.openEventStream((int) IMONITOR_TEMP_EVENT_ID);
            stream.setParam(0, curTemp);
            stream.setParam(1, 1);
            IMonitor.sendEvent(stream);
            IMonitor.closeEventStream(stream);
        } catch (RemoteException e) {
            SlogEx.e("Fsm_FoldScreenManagerService", "powerKit RemoteException error");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendLowTempWarningBroadcast() {
        if (!isScreenOn()) {
            SlogEx.i("Fsm_FoldScreenManagerService", "screen is off, not sendLowTempWarningBroadcast");
        } else if (isKeyguardLocked()) {
            SlogEx.i("Fsm_FoldScreenManagerService", "in keyguard state, not sendLowTempWarningBroadcast");
            this.mShouldNotifyLowTemp = true;
        } else {
            SlogEx.i("Fsm_FoldScreenManagerService", "sendLowTempWarningBroadcast");
            Intent intent = new Intent(ACTION_SMART_NOTIFY_FAULT);
            intent.putExtra(KEY_FAULT_DESCRIPTION, VALUE_FAULT_DESCRIPTION);
            intent.putExtra(KEY_FAULT_SUGGESTION, VALUE_FAULT_SUGGESTION);
            intent.putExtra(KEY_FAULT_CODE, VALUE_FAULT_CODE);
            this.mContext.sendBroadcastAsUser(intent, UserHandleEx.ALL, SMART_NOTIFY_FAULT_PERMISSION);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void saveAndUpdateDisplayMode(int displayMode) {
        SettingsEx.Secure.putIntForUser(this.mContext.getContentResolver(), HW_FOLD_DISPLAY_MODE, displayMode, -2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyPostureChangeInner(int posture) {
        int i = this.mFoldableStateListeners.beginBroadcast();
        while (i > 0) {
            i--;
            IFoldableStateListener listener = this.mFoldableStateListeners.getBroadcastItem(i);
            Integer type = (Integer) this.mFoldableStateListeners.getBroadcastCookie(i);
            if (!(listener == null || type == null)) {
                Bundle extra = new Bundle();
                try {
                    if (type.intValue() == 2) {
                        extra.putInt("android.intent.extra.REASON", 2);
                        extra.putInt("posture_mode", posture);
                        listener.onStateChange(extra);
                    }
                } catch (RemoteException e) {
                    SlogEx.e("Fsm_FoldScreenManagerService", "notifyPostureChangeInner RemoteException");
                }
            }
        }
        this.mFoldableStateListeners.finishBroadcast();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyDisplayModeChangeInner(int displayMode) {
        int i = this.mDisplayModeListeners.beginBroadcast();
        while (i > 0) {
            i--;
            IFoldDisplayModeListener listener = this.mDisplayModeListeners.getBroadcastItem(i);
            if (listener != null) {
                try {
                    listener.onScreenDisplayModeChange(displayMode);
                } catch (RemoteException e) {
                    SlogEx.e("Fsm_FoldScreenManagerService", "notifyDisplayModeChangeInner RemoteException");
                }
            }
        }
        this.mDisplayModeListeners.finishBroadcast();
        WindowManagerPolicyEx windowManagerPolicyEx = this.mPolicy;
        if (windowManagerPolicyEx != null) {
            windowManagerPolicyEx.setDisplayMode(displayMode);
        }
    }

    public void notifyDispalyModeChangePrepare(int displayMode) {
        long ident = Binder.clearCallingIdentity();
        try {
            Settings.Global.putInt(this.mContext.getContentResolver(), HW_FOLD_DISPLAY_MODE_PREPARE, displayMode);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyDisplayModeToSubScreenView(int displayMode) {
        HwPhoneWindowManager hwPhoneWindowManager = WindowManagerPolicyEx.getInstance().getHwPhoneWindowManager();
        SlogEx.d("Fsm_FoldScreenManagerService", "notifyDisplayModeChangeBefore displayMode:" + displayMode);
        if (hwPhoneWindowManager != null) {
            hwPhoneWindowManager.notifyDispalyModeChangeBefore(displayMode);
        }
    }

    private void notifyFsmTipsRequestInner(int reqTipsType, Bundle data) {
        synchronized (this.mFsmTipsRequestListeners) {
            int i = this.mFsmTipsRequestListeners.beginBroadcast();
            while (i > 0) {
                i--;
                IFoldFsmTipsRequestListener listener = this.mFsmTipsRequestListeners.getBroadcastItem(i);
                Integer type = (Integer) this.mFsmTipsRequestListeners.getBroadcastCookie(i);
                if (!(listener == null || type == null)) {
                    try {
                        if ((type.intValue() & reqTipsType) > 0) {
                            listener.onRequestFsmTips(reqTipsType, data);
                        }
                    } catch (RemoteException e) {
                        SlogEx.e("Fsm_FoldScreenManagerService", "notifyFsmTipsRequestInner RemoteException");
                    }
                }
            }
            this.mFsmTipsRequestListeners.finishBroadcast();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyFoldStateChangeToTp(int foldState) {
        String config;
        SlogEx.d("Fsm_FoldScreenManagerService", "notifyFoldStateChangeToTp foldState:" + foldState);
        if (foldState == 1) {
            config = "version:3+expand";
        } else if (foldState == 2) {
            config = "version:3+folder";
        } else if (foldState != 3) {
            SlogEx.w("Fsm_FoldScreenManagerService", "Invalid foldState=" + foldState);
            return;
        } else {
            config = "version:3+trestle";
        }
        if (this.mTpManager == null) {
            this.mTpManager = HwTpManager.getInstance();
        }
        this.mTpManager.hwTsSetAftConfig(config);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyDisplayModeChangeToTp(int displayMode) {
        String config;
        SlogEx.d("Fsm_FoldScreenManagerService", "notifyDisplayModeChangeToTp displayMode:" + displayMode);
        if (displayMode == 1) {
            config = "version:3+whole";
        } else if (displayMode == 2) {
            config = "version:3+main";
        } else if (displayMode == 3) {
            config = "version:3+minor";
        } else if (displayMode != 4) {
            SlogEx.w("Fsm_FoldScreenManagerService", "Invalid displayMode=" + displayMode);
            return;
        } else {
            config = "version:3+s_main";
            if (CoordinationModeUtils.getInstance(this.mContext).getCoordinationCreateMode() == 3) {
                config = "version:3+s_minor";
            }
        }
        if (this.mTpManager == null) {
            this.mTpManager = HwTpManager.getInstance();
        }
        this.mTpManager.hwTsSetAftConfig(config);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyIntelligentModeChangeToTp(boolean isIntelligent) {
        String config;
        SlogEx.d("Fsm_FoldScreenManagerService", "notifyIntelligentModeChangeToTp isIntelligent:" + isIntelligent);
        if (isIntelligent) {
            config = "version:3+gesture_ON";
        } else {
            config = "version:3+gesture_OFF";
        }
        if (this.mTpManager == null) {
            this.mTpManager = HwTpManager.getInstance();
        }
        this.mTpManager.hwTsSetAftConfig(config);
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [android.os.Handler, com.huawei.server.fsm.HwFoldScreenManagerService$HwFoldScreenManagerHandler] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void notifyFoldStateChange(int foldState) {
        ?? r0 = this.mHandler;
        r0.sendMessage(Message.obtain(r0, 1, foldState, 0));
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [android.os.Handler, com.huawei.server.fsm.HwFoldScreenManagerService$HwFoldScreenManagerHandler] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void notifyPostureChange(int posture) {
        ?? r0 = this.mHandler;
        r0.sendMessage(Message.obtain(r0, 0, posture, 0));
        if (this.mPowerManager == null) {
            this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        }
        PowerManagerEx.userActivity(this.mPowerManager, SystemClock.uptimeMillis(), false);
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [android.os.Handler, com.huawei.server.fsm.HwFoldScreenManagerService$HwFoldScreenManagerHandler] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void notifyDisplayModeChange(int displayMode) {
        ?? r0 = this.mHandler;
        r0.sendMessage(Message.obtain(r0, 2, displayMode, 0));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getPostureInner() {
        PostureStateMachine postureStateMachine = this.mPostureSM;
        if (postureStateMachine != null) {
            return postureStateMachine.getPosture();
        }
        return 100;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getFoldableStateInner() {
        PostureStateMachine postureStateMachine = this.mPostureSM;
        if (postureStateMachine != null) {
            return postureStateMachine.getFoldableState();
        }
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int setDisplayModeInner(int mode) {
        int displayModeBeforeSet = getDisplayModeInner();
        if (!isNeedSetDisplayModeInner(mode)) {
            return displayModeBeforeSet;
        }
        SlogEx.w("Fsm_FoldScreenManagerService", "mPostureSM.setDisplayMode " + mode);
        PostureStateMachine postureStateMachine = this.mPostureSM;
        if (postureStateMachine != null) {
            return postureStateMachine.setDisplayMode(mode);
        }
        return displayModeBeforeSet;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNeedSetDisplayModeInner(int mode) {
        int displayModeBeforeSet = getDisplayModeInner();
        if (!isScreenOn() && displayModeBeforeSet != 4 && HwFoldScreenState.isOutFoldDevice()) {
            SlogEx.w("Fsm_FoldScreenManagerService", "can not set display mode when screenoff ");
            return false;
        } else if (mode < 1 || mode > 4) {
            return false;
        } else {
            if (mode == displayModeBeforeSet) {
                SlogEx.w("Fsm_FoldScreenManagerService", "Display mode not change.");
                return false;
            } else if (this.mIsDisplayLocked) {
                SlogEx.w("Fsm_FoldScreenManagerService", "Display mode already be locked.");
                return false;
            } else {
                PostureStateMachine postureStateMachine = this.mPostureSM;
                if (postureStateMachine == null || postureStateMachine.getDisplayMode() != mode) {
                    return true;
                }
                SlogEx.w("Fsm_FoldScreenManagerService", "mPostureSM display mode not change.");
                return false;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getDisplayModeInner() {
        PostureStateMachine postureStateMachine = this.mPostureSM;
        if (postureStateMachine != null) {
            return postureStateMachine.getDisplayMode();
        }
        return 0;
    }

    private boolean isAllowedModeChange(int mode) {
        int currentMode = getDisplayModeInner();
        if (mode == 5) {
            if (currentMode != 2 && currentMode != FSM_SWITCH_FOREGROUND_USER_CMD) {
                return false;
            }
        } else if (mode == FSM_SWITCH_FOREGROUND_USER_CMD && currentMode != 1 && currentMode != 5) {
            return false;
        }
        return true;
    }

    private boolean isLockedDisplayMode(int mode) {
        if (mode < 1 || mode > FSM_SWITCH_FOREGROUND_USER_CMD) {
            return false;
        }
        return true;
    }

    private int getPolicy() {
        if (this.mIsDisplayLocked) {
            return 3;
        }
        if (this.mIsIntelligent) {
            return 1;
        }
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int lockDisplayModeInner(int mode) {
        if (!isLockedDisplayMode(mode)) {
            SlogEx.w("Fsm_FoldScreenManagerService", "lockDisplayModeInner: invalid mode " + mode);
            return getDisplayModeInner();
        } else if (!isAllowedModeChange(mode)) {
            SlogEx.w("Fsm_FoldScreenManagerService", "lockDisplayModeInner: current mode can't change to " + mode);
            return getDisplayModeInner();
        } else if (!isScreenOn()) {
            SlogEx.w("Fsm_FoldScreenManagerService", "lockDisplayModeInner: can not set display mode when screenoff");
            return getDisplayModeInner();
        } else if (!this.mIsDisplayLocked || mode != SystemPropertiesEx.getInt(HW_FOLD_DISPMODE, 0)) {
            this.mIsDisplayLocked = true;
            SystemPropertiesEx.set(HW_FOLD_DISPMODE, Integer.toString(mode));
            this.mPreprocess.updatePolicy(3);
            SlogEx.i("Fsm_FoldScreenManagerService", "lockDisplayModeInner: mode is " + mode);
            return mode;
        } else {
            SlogEx.w("Fsm_FoldScreenManagerService", "lockDisplayModeInner: lock the mode again");
            return getDisplayModeInner();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int unlockDisplayModeInner() {
        if (!isScreenOn()) {
            SlogEx.w("Fsm_FoldScreenManagerService", "lockDisplayModeInner: can not set display mode when screenoff");
            return getDisplayModeInner();
        }
        this.mIsDisplayLocked = false;
        SystemPropertiesEx.set(HW_FOLD_DISPMODE, Integer.toString(0));
        this.mPreprocess.updatePolicy(getPolicy());
        SlogEx.i("Fsm_FoldScreenManagerService", "unlockDisplayModeInner");
        return getDisplayModeInner();
    }

    private void setDisplayModeByForce(int mode) {
        if (mode == 0) {
            unlockDisplayModeInner();
        } else {
            lockDisplayModeInner(mode);
        }
    }

    private void dumpAllInfo(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("FOLD SCREEN MANAGER STARTER (dumpsys fold_screen)");
        pw.println("  StateMachine");
        this.mPostureSM.dump(fd, pw, args);
        pw.println(BuildConfig.FLAVOR);
        pw.println("  PreprocessManager");
        this.mPreprocess.dump("  ", pw);
        pw.println(BuildConfig.FLAVOR);
        pw.println("  DisplayMode = " + getDisplayModeInner());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doDump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int len = args.length;
        if (len > 1) {
            String cmd = args[0];
            try {
                if ("setDisplayMode".equals(cmd)) {
                    setDisplayModeInner(Integer.parseInt(args[1]));
                } else if ("setPosture".equals(cmd)) {
                    int posture = Integer.parseInt(args[1]);
                    if (this.mPostureSM != null) {
                        this.mPostureSM.setPosture(posture);
                    }
                } else if (this.mIsLockDisplayModeEnable && "lockDisplayMode".equals(cmd)) {
                    setDisplayModeByForce(Integer.parseInt(args[1]));
                }
            } catch (NumberFormatException e) {
                SlogEx.e("Fsm_FoldScreenManagerService", "parseInt fail with NumberFormatException");
            }
        } else if (len == 1) {
            pw.println("FOLD SCREEN MANAGER STARTER: args number error");
            pw.println("  Example: dumpsys fold_screen setDisplayMode 1");
        } else if (len == 0) {
            dumpAllInfo(fd, pw, args);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void changeIntelligentMode(int intelligent) {
        synchronized (this.mLock) {
            boolean isIntelligentNow = intelligent == 1;
            if (isIntelligentNow) {
                if (!isIncall()) {
                    PostureStateMachine postureStateMachine = this.mPostureSM;
                    PostureStateMachine postureStateMachine2 = this.mPostureSM;
                    postureStateMachine.setScreeStateWhenCallComing(0);
                } else if (isScreenOn()) {
                    PostureStateMachine postureStateMachine3 = this.mPostureSM;
                    PostureStateMachine postureStateMachine4 = this.mPostureSM;
                    postureStateMachine3.setScreeStateWhenCallComing(1);
                } else {
                    PostureStateMachine postureStateMachine5 = this.mPostureSM;
                    PostureStateMachine postureStateMachine6 = this.mPostureSM;
                    postureStateMachine5.setScreeStateWhenCallComing(2);
                }
            }
            if (isIntelligentNow != this.mIsIntelligent) {
                this.mIsIntelligent = isIntelligentNow;
                SlogEx.i("Fsm_FoldScreenManagerService", "changeIntelligentMode mIsIntelligent = " + this.mIsIntelligent);
                if (!this.mIsDisplayLocked) {
                    this.mPreprocess.updatePolicy(getPolicy());
                }
                notifyIntelligentModeChangeToTp(this.mIsIntelligent);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class HwFoldScreenManagerHandler extends HandlerAdapter {
        HwFoldScreenManagerHandler(Looper looper) {
            super(looper, (Handler.Callback) null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    HwFoldScreenManagerService.this.notifyPostureChangeInner(msg.arg1);
                    return;
                case 1:
                    Settings.Secure.putInt(HwFoldScreenManagerService.this.mContext.getContentResolver(), HwFoldScreenManagerService.HW_FOLD_SCRREN_STATE, msg.arg1);
                    HwFoldScreenManagerService.this.notifyFoldStateChangeInner(msg.arg1);
                    HwFoldScreenManagerService.this.notifyFoldStateChangeToTp(msg.arg1);
                    return;
                case 2:
                    HwFoldScreenManagerService.this.saveAndUpdateDisplayMode(msg.arg1);
                    if (HwFoldScreenManagerService.this.mSensorManager == null) {
                        HwFoldScreenManagerService hwFoldScreenManagerService = HwFoldScreenManagerService.this;
                        hwFoldScreenManagerService.mSensorManager = (SensorManager) hwFoldScreenManagerService.mContext.getSystemService("sensor");
                    }
                    HwFoldScreenManagerService.this.setSensorConfig(msg.arg1);
                    HwFoldScreenManagerService.this.notifyDisplayModeToSubScreenView(msg.arg1);
                    HwFoldScreenManagerService.this.removeFsmTipsOnDisplaymodeChange(msg.arg1);
                    HwFoldScreenManagerService.this.notifyDisplayModeChangeInner(msg.arg1);
                    HwFoldScreenManagerService.this.notifyDisplayModeChangeToTp(msg.arg1);
                    HwLogEx.dubaie("DUBAI_TAG_DISPLAY_MODE", "mode=" + msg.arg1);
                    HwFoldScreenManagerService.this.recordFoldScreenCounter(msg.arg1);
                    return;
                case FsmConst.POLICY_TYPE_TEST /* 3 */:
                case 5:
                default:
                    return;
                case 4:
                    SlogEx.i("Fsm_FoldScreenManagerService", "magnetometer timeout, turnoff sensor");
                    if (!HwFoldScreenManagerService.this.isScreenOn()) {
                        HwFoldScreenManagerService.this.mPreprocess.stop();
                        HwFoldScreenManagerService.this.mIsMagnetomerter = false;
                        return;
                    }
                    return;
                case HwFoldScreenManagerService.FSM_SWITCH_FOREGROUND_USER_CMD /* 6 */:
                    SlogEx.i("Fsm_FoldScreenManagerService", "handle user switch ");
                    HwFoldScreenManagerService.this.changeIntelligentMode(SettingsEx.Secure.getIntForUser(HwFoldScreenManagerService.this.mContext.getContentResolver(), HwFoldScreenManagerService.KEY_INTELLIGENT_AWAKEN, 1, -2));
                    return;
                case HwFoldScreenManagerService.FSM_SWITCH_DISP_MODE_FROM_RESUME_CMD /* 7 */:
                    int displayMode = msg.arg1;
                    SlogEx.i("Fsm_FoldScreenManagerService", "switch display mode from FSM resume, displayMode=" + displayMode);
                    if (HwFoldScreenManagerService.this.mIsDisplayLocked) {
                        HwFoldScreenManagerService.this.mPreprocess.updatePolicy(3);
                        return;
                    } else {
                        HwFoldScreenManagerService.this.setDisplayModeInner(displayMode);
                        return;
                    }
                case HwFoldScreenManagerService.FSM_FREEZE_FOLD_ROTATION_CMD /* 8 */:
                    SlogEx.d("Fsm_FoldScreenManagerService", "handle msg FSM_FREEZE_FOLD_ROTATION_CMD");
                    if (HwFoldScreenManagerService.this.mWm != null) {
                        HwFoldScreenManagerService.this.mWm.setFoldSwitchState(true);
                        return;
                    }
                    return;
                case HwFoldScreenManagerService.FSM_UNFREEZE_FOLD_ROTATION_CMD /* 9 */:
                    SlogEx.d("Fsm_FoldScreenManagerService", "handle msg FSM_UNFREEZE_FOLD_ROTATION_CMD");
                    if (HwFoldScreenManagerService.this.mWm != null) {
                        HwFoldScreenManagerService.this.mWm.setFoldSwitchState(false);
                        return;
                    }
                    return;
                case HwFoldScreenManagerService.MSG_NOTIFY_INTELLIGENT_MODE_TO_TP /* 10 */:
                    synchronized (HwFoldScreenManagerService.this.mLock) {
                        HwFoldScreenManagerService.this.notifyIntelligentModeChangeToTp(HwFoldScreenManagerService.this.mIsIntelligent);
                    }
                    return;
                case HwFoldScreenManagerService.MSG_SET_FOLD_DISPLAY_MODE_FINISHED /* 11 */:
                    HwFoldScreenManagerService.this.finishSetFoldDisplayMode(msg.arg1, msg.arg2);
                    return;
            }
        }
    }

    public void pauseDispModeChange() {
        pauseDispModeChangeInner();
    }

    public boolean getInfoDrawWindow() {
        return this.mIsDrawingScreenOff;
    }

    public boolean getMagnetorFlag() {
        return this.mIsMagnetomerter;
    }

    public void freezeFoldRotation() {
        handleDeferDispModeCmd(FSM_FREEZE_FOLD_ROTATION_CMD);
    }

    public void unFreezeFoldRotation() {
        handleDeferDispModeCmd(FSM_UNFREEZE_FOLD_ROTATION_CMD);
    }

    public boolean isPausedDispModeChange() {
        return isPausedDispModeChangeInner();
    }

    public void setDeferredDispMode(int mode) {
        synchronized (this.mDeferDispLock) {
            this.mDeferredDispMode = mode;
        }
    }

    public void resetDispModeChange() {
        synchronized (this.mDeferDispLock) {
            SlogEx.w("Fsm_FoldScreenManagerService", "resetDispModeChange mDeferredDispModeChangeCount = " + this.mDeferredDispModeChangeCount);
            this.mDeferredDispModeChangeCount = 0;
            this.mDeferredDispMode = 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSensorConfig(int displayMode) {
        if (!HwFoldScreenState.isInwardFoldDevice() || displayMode != 2) {
            SensorManager sensorManager = this.mSensorManager;
            SensorManagerEx.hwSetSensorConfig(sensorManager, "setDisplayMode::" + displayMode);
            return;
        }
        SensorManagerEx.hwSetSensorConfig(this.mSensorManager, "setDisplayMode::3");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void pauseDispModeChangeInner() {
        synchronized (this.mDeferDispLock) {
            this.mDeferredDispModeChangeCount++;
            SlogEx.d("Fsm_FoldScreenManagerService", "pauseDispModeChangeInner mDeferredDispModeChangeCount = " + this.mDeferredDispModeChangeCount + " mDeferredDispMode " + this.mDeferredDispMode);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPausedDispModeChangeInner() {
        boolean z;
        synchronized (this.mDeferDispLock) {
            z = this.mDeferredDispModeChangeCount > 0;
        }
        return z;
    }

    /* JADX WARN: Type inference failed for: r4v0, types: [android.os.Handler, com.huawei.server.fsm.HwFoldScreenManagerService$HwFoldScreenManagerHandler] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void resumeDispModeChangeInner() {
        synchronized (this.mDeferDispLock) {
            if (this.mDeferredDispModeChangeCount > 0) {
                SlogEx.d("Fsm_FoldScreenManagerService", "resumeDispModeChangeInner mDeferredDispModeChangeCount = " + this.mDeferredDispModeChangeCount + " mDeferredDispMode = " + this.mDeferredDispMode);
                this.mDeferredDispModeChangeCount = 0;
                unFreezeFoldRotation();
                if (this.mDeferredDispMode != 0) {
                    this.mHandler.removeMessages(FSM_SWITCH_DISP_MODE_FROM_RESUME_CMD);
                    this.mHandler.sendMessage(Message.obtain(this.mHandler, FSM_SWITCH_DISP_MODE_FROM_RESUME_CMD, this.mDeferredDispMode, 0));
                    this.mDeferredDispMode = 0;
                }
            }
        }
    }

    private void handleDeferDispModeCmd(int cmd) {
        HwFoldScreenManagerHandler hwFoldScreenManagerHandler = this.mHandler;
        hwFoldScreenManagerHandler.sendMessage(hwFoldScreenManagerHandler.obtainMessage(cmd));
    }

    private final class BinderService extends IHwFoldScreenManagerEx {
        private BinderService() {
        }

        public int getPosture() {
            if (Binder.getCallingUid() == 1000 || HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN) == 0) {
                return HwFoldScreenManagerService.this.getPostureInner();
            }
            throw new SecurityException(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN_PERMISSION);
        }

        public int getFoldableState() {
            if (Binder.getCallingUid() == 1000 || HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN) == 0) {
                return HwFoldScreenManagerService.this.getFoldableStateInner();
            }
            throw new SecurityException(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN_PERMISSION);
        }

        public void registerFoldableState(IFoldableStateListener listener, int type) {
            if (Binder.getCallingUid() == 1000 || HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN) == 0) {
                synchronized (this) {
                    HwFoldScreenManagerService.this.mFoldableStateListeners.register(listener, new Integer(type));
                }
                return;
            }
            throw new SecurityException(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN_PERMISSION);
        }

        public void unregisterFoldableState(IFoldableStateListener listener) {
            if (Binder.getCallingUid() == 1000 || HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN) == 0) {
                synchronized (this) {
                    HwFoldScreenManagerService.this.mFoldableStateListeners.unregister(listener);
                }
                return;
            }
            throw new SecurityException(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN_PERMISSION);
        }

        public int setDisplayMode(int mode) {
            if (Binder.getCallingUid() == 1000 || HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN) == 0) {
                SlogEx.i("Fsm_FoldScreenManagerService", "setDisplayMode mode=" + mode);
                long origId = Binder.clearCallingIdentity();
                try {
                    return HwFoldScreenManagerService.this.setDisplayModeInner(mode);
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } else {
                throw new SecurityException(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN_PERMISSION);
            }
        }

        public int getDisplayMode() {
            if (Binder.getCallingUid() == 1000 || HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN) == 0) {
                return HwFoldScreenManagerService.this.getDisplayModeInner();
            }
            throw new SecurityException(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN_PERMISSION);
        }

        public int lockDisplayMode(int mode) {
            if (Binder.getCallingUid() == 1000 || HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN_PRIVILEGED) == 0) {
                return HwFoldScreenManagerService.this.lockDisplayModeInner(mode);
            }
            throw new SecurityException("Requires PERMISSION_FOLD_SCREEN_PRIVILEGED permission");
        }

        public int unlockDisplayMode() {
            if (Binder.getCallingUid() == 1000 || HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN_PRIVILEGED) == 0) {
                return HwFoldScreenManagerService.this.unlockDisplayModeInner();
            }
            throw new SecurityException("Requires PERMISSION_FOLD_SCREEN_PRIVILEGED permission");
        }

        public void registerFoldDisplayMode(IFoldDisplayModeListener listener) {
            if (Binder.getCallingUid() == 1000 || Binder.getCallingUid() == 1047 || HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN) == 0) {
                synchronized (this) {
                    HwFoldScreenManagerService.this.mDisplayModeListeners.register(listener);
                }
                return;
            }
            throw new SecurityException(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN_PERMISSION);
        }

        public void unregisterFoldDisplayMode(IFoldDisplayModeListener listener) {
            if (Binder.getCallingUid() == 1000 || Binder.getCallingUid() == 1047 || HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN) == 0) {
                synchronized (this) {
                    HwFoldScreenManagerService.this.mDisplayModeListeners.unregister(listener);
                }
                return;
            }
            throw new SecurityException(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN_PERMISSION);
        }

        public void registerFsmTipsRequestListener(IFoldFsmTipsRequestListener listener, int type) {
            if (Binder.getCallingUid() != 1000 && Binder.getCallingUid() != 1047 && HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN) != 0) {
                throw new SecurityException(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN_PERMISSION);
            } else if (Binder.getCallingUid() == 1000 || type == 4) {
                synchronized (HwFoldScreenManagerService.this.mFsmTipsRequestListeners) {
                    HwFoldScreenManagerService.this.mFsmTipsRequestListeners.register(listener, new Integer(type));
                }
            } else {
                throw new SecurityException("only provide monitor REQ_BROADCAST_TIPS_REMOVED permission");
            }
        }

        public void unregisterFsmTipsRequestListener(IFoldFsmTipsRequestListener listener) {
            if (Binder.getCallingUid() == 1000 || Binder.getCallingUid() == 1047 || HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN) == 0) {
                synchronized (HwFoldScreenManagerService.this.mFsmTipsRequestListeners) {
                    HwFoldScreenManagerService.this.mFsmTipsRequestListeners.unregister(listener);
                }
                return;
            }
            throw new SecurityException(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN_PERMISSION);
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtilsEx.checkDumpPermission(HwFoldScreenManagerService.this.mContext, "Fsm_FoldScreenManagerService", pw)) {
                HwFoldScreenManagerService.this.doDump(fd, pw, args);
                pw.println("HwFoldScreenManagerService is running...");
            }
        }

        public int reqShowTipsToFsm(int reqTipsType, Bundle data) {
            if (Binder.getCallingUid() != 1000 && Binder.getCallingUid() != 1047 && HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN) != 0) {
                throw new SecurityException(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN_PERMISSION);
            } else if (!HwFoldScreenManagerService.IS_FACTORY && !"true".equals(SystemPropertiesEx.get("runtime.mmitest.isrunning", "false"))) {
                return HwFoldScreenManagerService.this.reqShowTipsToFsmInner(reqTipsType, data, Binder.getCallingUid());
            } else {
                SlogEx.i("Fsm_FoldScreenManagerService", "Factory binary or MMI test, return. isFactory:" + HwFoldScreenManagerService.IS_FACTORY);
                return reqTipsType;
            }
        }
    }

    private void scheduleNotifyIntellientMode() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_NOTIFY_INTELLIGENT_MODE_TO_TP));
    }

    private void prepareSetFoldScreenModeLocked() {
        SlogEx.w("Fsm_FoldScreenManagerService", "prepareSetFoldScreenModeLocked");
        this.mIsDrawingScreenOff = false;
        this.mPreprocess.start(0);
        scheduleNotifyIntellientMode();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean waitForSetFoldDisplayMode(HwFoldScreenManagerInternal.FoldScreenOnListener listener) {
        synchronized (this.mLock) {
            this.mFoldScreenOnListener = listener;
            SlogEx.i("Fsm_FoldScreenManagerService", "waitForSetFoldDisplayMode");
            ReportMonitorProcess.getInstance().updateDurationEndTime(SystemClock.uptimeMillis(), this.mPostureSM.getSleepMode());
            if (this.mIsMagnetomerter) {
                sendFinishSetModeMessage(0, 0, 0);
                return true;
            }
            sendFinishSetModeMessage(0, 0, FSM_FORCE_WAKEUP_TIMEOUT);
            prepareSetFoldScreenModeLocked();
            return true;
        }
    }

    public void sendFinishSetModeMessage(int newMode, int oldMode, int delay) {
        this.mHandler.removeMessages(MSG_SET_FOLD_DISPLAY_MODE_FINISHED);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_SET_FOLD_DISPLAY_MODE_FINISHED, newMode, oldMode), (long) delay);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void finishSetFoldDisplayMode(int newMode, int oldMode) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(MSG_SET_FOLD_DISPLAY_MODE_FINISHED);
            HwFoldScreenManagerInternal.FoldScreenOnListener listener = this.mFoldScreenOnListener;
            SlogEx.i("Fsm_FoldScreenManagerService", "finishSetFoldDisplayMode listener:" + listener + ", newMode:" + newMode + ", oldMode:" + oldMode);
            if (listener != null) {
                int adjustNewMode = newMode;
                int adjustOldMode = oldMode;
                if (adjustNewMode == 0 && adjustOldMode == 0) {
                    int currentMode = getDisplayModeInner();
                    adjustNewMode = currentMode;
                    adjustOldMode = currentMode;
                }
                listener.onFoldScreenOn(adjustNewMode, adjustOldMode);
            }
            this.mFoldScreenOnListener = null;
        }
        this.mPostureSM.setInPostureChangingFlag(false);
    }

    private final class LocalService extends HwFoldScreenManagerInternal {
        private LocalService() {
        }

        public int getPosture() {
            return HwFoldScreenManagerService.this.getPostureInner();
        }

        public int getFoldableState() {
            return HwFoldScreenManagerService.this.getFoldableStateInner();
        }

        public int setDisplayMode(int mode) {
            return HwFoldScreenManagerService.this.setDisplayModeInner(mode);
        }

        public int getDisplayMode() {
            return HwFoldScreenManagerService.this.getDisplayModeInner();
        }

        public int lockDisplayMode(int mode) {
            return HwFoldScreenManagerService.this.lockDisplayModeInner(mode);
        }

        public int unlockDisplayMode() {
            return HwFoldScreenManagerService.this.unlockDisplayModeInner();
        }

        public void onDoubleClick(boolean isScreenOn, Bundle extra) {
            if (extra != null && !isScreenOn) {
                HwFoldScreenManagerService.this.mPostureSM.setDisplayRectForDoubleClick(HwFoldScreenState.getClickRegion((Point) extra.getParcelable("position")));
                wakeup(4, "doubleclick");
            }
        }

        public void notifySleep() {
            synchronized (HwFoldScreenManagerService.this.mLock) {
                HwFoldScreenManagerService.this.notifySleepInner();
            }
        }

        public boolean foldScreenTurningOn(HwFoldScreenManagerInternal.FoldScreenOnListener listener) {
            return HwFoldScreenManagerService.this.waitForSetFoldDisplayMode(listener);
        }

        public void prepareWakeup(int wakeupType) {
            boolean z;
            if (HwFoldScreenManagerService.this.isScreenOn()) {
                SlogEx.w("Fsm_FoldScreenManagerService", "screen is on, skip it");
                return;
            }
            synchronized (HwFoldScreenManagerService.this.mLock) {
                z = true;
                HwFoldScreenManagerService.this.mIsMagnetomerter = true;
                if (wakeupType == 4) {
                    HwFoldScreenManagerService.this.mHandler.sendEmptyMessageDelayed(4, 5000);
                    ReportMonitorProcess.getInstance().updateExitMotionTime(SystemClock.uptimeMillis());
                }
                HwFoldScreenManagerService.this.mIsDrawingScreenOff = false;
                HwFoldScreenManagerService.this.mPreprocess.start(wakeupType);
                HwFoldScreenManagerService.this.notifyIntelligentModeChangeToTp(HwFoldScreenManagerService.this.mIsIntelligent);
            }
            SlogEx.i("Fsm_FoldScreenManagerService", "notifyKeyguardWakeupType prepareWakeup " + wakeupType);
            if (HwFoldScreenState.isInwardFoldDevice()) {
                if (wakeupType != 4) {
                    z = false;
                }
                ActivityTaskManagerServiceEx.setExpandScreenTurningOn(z);
                SlogEx.i("Fsm_FoldScreenManagerService", "notifyKeyguardWakeupType " + wakeupType);
            }
        }

        public void wakeup(int reason, String detail) {
            SlogEx.d("Fsm_FoldScreenManagerService", "Wakeup reason = " + detail + " detail= " + detail);
            if (HwFoldScreenManagerService.this.mPowerManager == null) {
                HwFoldScreenManagerService hwFoldScreenManagerService = HwFoldScreenManagerService.this;
                hwFoldScreenManagerService.mPowerManager = (PowerManager) hwFoldScreenManagerService.mContext.getSystemService("power");
            }
            PowerManagerEx.wakeUp(HwFoldScreenManagerService.this.mPowerManager, SystemClock.uptimeMillis(), reason, detail);
        }

        public boolean onSetFoldDisplayModeFinished(int newMode, int oldMode) {
            synchronized (HwFoldScreenManagerService.this.mLock) {
                SlogEx.i("Fsm_FoldScreenManagerService", "onSetFoldDisplayModeFinished newMode:" + newMode + ", oldMode:" + oldMode + ", mIsMagnetomerter:" + HwFoldScreenManagerService.this.mIsMagnetomerter);
                if (HwFoldScreenManagerService.this.mIsDrawingScreenOff || !HwFoldScreenManagerService.this.mIsMagnetomerter || newMode != 1 || oldMode != 2) {
                    HwFoldScreenManagerService.this.sendFinishSetModeMessage(newMode, oldMode, 0);
                } else {
                    wakeup(103, "magnetic.wakeUp");
                    HwFoldScreenManagerService.this.resumeDispModeChangeInner();
                    if (HwFoldScreenManagerService.this.mWm != null) {
                        HwFoldScreenManagerService.this.mWm.unFreezeFoldRotation();
                    }
                }
            }
            return true;
        }

        public void handleDrawWindow() {
            if (HwFoldScreenManagerService.this.isScreenOn()) {
                SlogEx.w("Fsm_FoldScreenManagerService", "screen is on");
                return;
            }
            ReportMonitorProcess.getInstance().updateScreenOffInitTime(SystemClock.uptimeMillis());
            ReportMonitorProcess.getInstance().updateDurationEndTime(SystemClock.uptimeMillis(), 1);
            HwFoldScreenManagerService.this.mIsDrawingScreenOff = true;
            HwFoldScreenManagerService.this.mPostureSM.setPosture(103);
        }

        public boolean getInfoDrawWindow() {
            return HwFoldScreenManagerService.this.mIsDrawingScreenOff;
        }

        public void resetInfoDrawWindow() {
            HwFoldScreenManagerService.this.mPostureSM.notifySleep();
            HwFoldScreenManagerService.this.mIsDrawingScreenOff = false;
            HwFoldScreenManagerService.this.mIsMagnetomerter = false;
        }

        public int reqShowTipsToFsm(int reqTipsType, Bundle data) {
            return HwFoldScreenManagerService.this.reqShowTipsToFsmInner(reqTipsType, data, Process.myUid());
        }

        public void pauseDispModeChange() {
            HwFoldScreenManagerService.this.pauseDispModeChangeInner();
        }

        public void resumeDispModeChange() {
            HwFoldScreenManagerService.this.resumeDispModeChangeInner();
        }

        public boolean isPausedDispModeChange() {
            return HwFoldScreenManagerService.this.isPausedDispModeChangeInner();
        }

        public boolean shouldChangeDisplayMode() {
            synchronized (HwFoldScreenManagerService.this.mDeferDispLock) {
                if (HwFoldScreenManagerService.this.mDeferredDispModeChangeCount > 0) {
                    if (HwFoldScreenManagerService.this.mDeferredDispMode != 0) {
                        return HwFoldScreenManagerService.this.isNeedSetDisplayModeInner(HwFoldScreenManagerService.this.mDeferredDispMode);
                    }
                }
                return false;
            }
        }

        public void notifyLowTempWarning(int value) {
            HwFoldScreenManagerService.this.mScreenOffLowTemp = value;
        }

        public void notifyScreenOn() {
            if (HwFoldScreenManagerService.this.mScreenOffLowTemp == 1) {
                HwFoldScreenManagerService.this.sendLowTempWarningBroadcast();
                HwFoldScreenManagerService.this.mScreenOffLowTemp = 0;
            }
        }

        public void notifyScreenOnFinished() {
            if (HwFoldScreenManagerService.this.mIsMagnetomerter) {
                HwFoldScreenManagerService.this.mIsMagnetomerter = false;
            }
        }

        public void startDawnAnimaiton() {
            if (HwFoldScreenManagerService.this.mDms == null) {
                HwFoldScreenManagerService.this.mDms = DisplayManagerInternalEx.getInstance();
                if (HwFoldScreenManagerService.this.mDms == null) {
                    SlogEx.e("Fsm_FoldScreenManagerService", "mDms is null");
                    return;
                }
            }
            HwFoldScreenManagerService.this.mDms.startDawnAnimation();
            HwFoldScreenManagerService.this.mIsMagnetomerter = false;
        }

        public boolean registerScreenOnUnBlockerCallback(HwFoldScreenManagerInternal.ScreenOnUnblockerCallback screenOnUnblockerCallback) {
            if (HwFoldScreenManagerService.this.mDms == null) {
                HwFoldScreenManagerService.this.mDms = DisplayManagerInternalEx.getInstance();
                if (HwFoldScreenManagerService.this.mDms == null) {
                    SlogEx.e("Fsm_FoldScreenManagerService", "mDms is null");
                    return false;
                }
            }
            return HwFoldScreenManagerService.this.mDms.registerScreenOnUnBlockerCallback(screenOnUnblockerCallback);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifySleepInner() {
        this.mHandler.removeMessages(4);
        if (!this.mIsMagnetomerter) {
            this.mPreprocess.stop();
        }
        this.mPostureSM.notifySleep();
        this.mPostureSM.setPickupWakeUp(false);
    }

    /* access modifiers changed from: protected */
    public void exitCoordinationDisplayMode() {
        ActivityTaskManagerServiceEx.exitCoordinationMode(true, false);
    }

    /* access modifiers changed from: protected */
    public void removeForceWakeUp() {
        this.mHandler.removeMessages(4);
    }

    /* access modifiers changed from: protected */
    public void bdReport(int eventId, String eventMsg) {
        Flog.bdReport(eventId, eventMsg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int reqShowTipsToFsmInner(int reqTipsType, Bundle data, int callerUid) {
        Bundle fsmTipsData;
        int targetDisplayMode;
        String callerUidName = this.mContext.getPackageManager().getNameForUid(callerUid);
        SlogEx.d("Fsm_FoldScreenManagerService", "reqShowTipsToFsmInner reqTipsType = " + reqTipsType + " ,Bundle = " + data + ", callerName = " + callerUidName);
        if (reqTipsType != 4 || callerUid == 1000) {
            if (data == null) {
                fsmTipsData = new Bundle();
                fsmTipsData.putString("KEY_TIPS_STR_CALLER_NAME", callerUidName);
            } else {
                if (callerUid != 1000) {
                    data.putString("KEY_TIPS_STR_CALLER_NAME", callerUidName);
                } else if (data.getString("KEY_TIPS_STR_CALLER_NAME") == null) {
                    data.putString("KEY_TIPS_STR_CALLER_NAME", callerUidName);
                }
                String cameraId = data.getString("KEY_TIPS_STR_CAMERA_ID", null);
                if (cameraId != null) {
                    if (REAR_CAMERA.equals(cameraId) || FRONT_CAMERA.equals(cameraId)) {
                        this.mSpecifiedCameraId = cameraId;
                    }
                    SlogEx.i("Fsm_FoldScreenManagerService", "reqShowTipsToFsmInner set new mSpecifiedCameraId = " + this.mSpecifiedCameraId);
                }
                fsmTipsData = data;
            }
            if (reqTipsType == 2 && fsmTipsData.getInt("KEY_TIPS_INT_VIEW_TYPE", 0) == 1 && (targetDisplayMode = fsmTipsData.getInt("KEY_TIPS_INT_DISPLAY_MODE", 0)) == 2 && getDisplayModeInner() == 3) {
                notifyDisplayModeToSubScreenView(targetDisplayMode);
            }
            notifyFsmTipsRequestInner(reqTipsType, fsmTipsData);
            return reqTipsType;
        }
        SlogEx.w("Fsm_FoldScreenManagerService", "permission check fail");
        return reqTipsType;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeFsmTipsOnDisplaymodeChange(int displayMode) {
        SlogEx.i("Fsm_FoldScreenManagerService", "removeFsmTipsOnDisplaymodeChange:");
        if (this.mLastDisplayMode == displayMode) {
            SlogEx.i("Fsm_FoldScreenManagerService", "displayMode does not change, return");
            return;
        }
        this.mSpecifiedCameraId = null;
        String callerUidName = this.mContext.getPackageManager().getNameForUid(1000);
        Bundle data = new Bundle();
        data.putString("KEY_TIPS_STR_CALLER_NAME", callerUidName);
        data.putInt("KEY_TIPS_INT_REMOVED_REASON", 1);
        data.putInt("KEY_TIPS_INT_DISPLAY_MODE", displayMode);
        reqShowTipsToFsmInner(1, null, 1000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void recordFoldScreenCounter(int displayMode) {
        int i = this.mLastDisplayMode;
        if (i == displayMode) {
            SlogEx.d("Fsm_FoldScreenManagerService", "displayMode does not change, return");
            return;
        }
        if (displayMode == 1 && i != 0) {
            int foldCounter = Settings.Global.getInt(this.mContext.getContentResolver(), HW_FOLD_SCREEN_COUNTER, 0);
            if (foldCounter == 0 || foldCounter > MAX_FOLD_CREEN_NUM) {
                SlogEx.d("Fsm_FoldScreenManagerService", "Begin to record folding number");
                foldCounter = 0;
            }
            Settings.Global.putInt(this.mContext.getContentResolver(), HW_FOLD_SCREEN_COUNTER, foldCounter + 1);
        }
        this.mLastDisplayMode = displayMode;
    }
}
