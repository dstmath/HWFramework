package com.android.server.swing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Slog;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.huawei.android.os.IScreenStateCallbackEx;
import com.huawei.android.os.PowerManagerEx;
import com.huawei.hiai.awareness.service.AwarenessManager;
import com.huawei.hiai.awareness.service.AwarenessServiceConnection;
import com.huawei.hiai.awareness.service.ExtendAwarenessFence;
import com.huawei.hiai.awareness.service.IAwarenessListener;
import com.huawei.hiai.awareness.service.IRequestCallBack;
import com.huawei.hiai.awareness.service.RequestResult;
import java.lang.ref.WeakReference;

public class HwSwingEyeGazeHub {
    private static final int AWARE_ACTION_UNKNOWN = 0;
    private static final int DEFAULT_SCREEN_OFF_TIMEOUT = 15000;
    private static final int EYE_GAZE_ENABLED = 1;
    private static final String HW_SCREEN_OFF_FOR_POSITIVE = "hw.intent.action.HW_SCREEN_OFF_FOR_POSITIVE";
    private static final String KEY_MOTION_ITEM_WAKEUP_GAZE = "item_wakeup_gaze_switch";
    private static final int RECONNECT_MAX_COUNT = 3;
    private static final int RECONNECT_WAIT_TIME_MS = 10000;
    private static final int REMAIN_TIME_MAX = 16000;
    private static final int REMAIN_TIME_MIN = 8000;
    private static final int REPORT_INTERVAL_TIME = 18000000;
    private static final int REPORT_THRESHOLD = 200;
    private static final int STATE_CHANGE = 1;
    private static final String TAG = "HwSwingEyeGazeHub";
    private IRequestCallBack mAwarenessCallback = new IRequestCallBack.Stub() {
        /* class com.android.server.swing.HwSwingEyeGazeHub.AnonymousClass1 */

        @Override // com.huawei.hiai.awareness.service.IRequestCallBack
        public void onRequestResult(RequestResult result) throws RemoteException {
            Slog.i(HwSwingEyeGazeHub.TAG, "registerFence() result = " + result);
        }
    };
    private ExtendAwarenessFence mAwarenessEyeGazeFence;
    private Handler mAwarenessHandler;
    private IAwarenessListener mAwarenessListener = new IAwarenessListener.Stub() {
        /* class com.android.server.swing.HwSwingEyeGazeHub.AnonymousClass3 */

        @Override // com.huawei.hiai.awareness.service.IAwarenessListener
        public void handleEvent(ExtendAwarenessFence awarenessFence, Bundle result) {
            Slog.e(HwSwingEyeGazeHub.TAG, "IAwarenessListener handleEvent awarenessFence.hashCode() = " + awarenessFence.hashCode());
            HwSwingEyeGazeHub.this.handleFenceResult(result);
        }
    };
    private AwarenessManager mAwarenessManager;
    private int mAwarenessReconnectTimes;
    private AwarenessServiceConnection mAwarenessServiceConnection = new AwarenessServiceConnection() {
        /* class com.android.server.swing.HwSwingEyeGazeHub.AnonymousClass4 */

        @Override // com.huawei.hiai.awareness.service.AwarenessServiceConnection
        public void onServiceConnected() {
            HwSwingEyeGazeHub.this.mIsAwarenessConnected = true;
            HwSwingEyeGazeHub.this.mAwarenessReconnectTimes = 0;
            Slog.i(HwSwingEyeGazeHub.TAG, "mAwarenessServiceConnection connected !");
            HwSwingEyeGazeHub.this.startRegisterEyeGazeFenceThread();
        }

        @Override // com.huawei.hiai.awareness.service.AwarenessServiceConnection
        public void onServiceDisconnected() {
            HwSwingEyeGazeHub.this.unregisterEyeGazeFence();
            HwSwingEyeGazeHub.this.mIsAwarenessConnected = false;
            if (HwSwingEyeGazeHub.this.mRegisteredFenceAction == 0) {
                Slog.w(HwSwingEyeGazeHub.TAG, "no need to reconnect service");
                return;
            }
            Slog.w(HwSwingEyeGazeHub.TAG, "wait 10000 ms to reconnect...");
            HwSwingEyeGazeHub.this.mAwarenessHandler.postDelayed(new Runnable() {
                /* class com.android.server.swing.HwSwingEyeGazeHub.AnonymousClass4.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    HwSwingEyeGazeHub.access$308(HwSwingEyeGazeHub.this);
                    if (!HwSwingEyeGazeHub.this.mIsAwarenessConnected && HwSwingEyeGazeHub.this.mAwarenessReconnectTimes < 3) {
                        Slog.w(HwSwingEyeGazeHub.TAG, "mAwarenessHandler try connectService...");
                        if (!HwSwingEyeGazeHub.this.mAwarenessManager.connectService(HwSwingEyeGazeHub.this.mAwarenessServiceConnection)) {
                            Slog.w(HwSwingEyeGazeHub.TAG, "connectService failed!");
                            HwSwingEyeGazeHub.this.mAwarenessHandler.postDelayed(this, 10000);
                        }
                    }
                }
            }, 10000);
        }
    };
    private Context mContext;
    private int mEyeGazeCount = 0;
    private EyeGazeFenceRunnable mEyeGazeFenceRunnable;
    private ContentObserver mEyeGazeObserver = new ContentObserver(new Handler()) {
        /* class com.android.server.swing.HwSwingEyeGazeHub.AnonymousClass5 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            super.onChange(isSelfChange);
            Slog.i(HwSwingEyeGazeHub.TAG, "Eye Gaze Switch has changed!");
            HwSwingEyeGazeHub.this.refreshScreenOutTime();
        }
    };
    private int mEyeGazeSwitchValue = 0;
    private boolean mIsAwarenessConnected;
    private boolean mIsAwarenessRegistered;
    private boolean mIsPowerManagerRegistered;
    private int mLastRemainTime = 0;
    private long mLastReportTime;
    private PowerManager mPowerManager;
    private IScreenStateCallbackEx mPowerManagerCallback = new IScreenStateCallbackEx() {
        /* class com.android.server.swing.HwSwingEyeGazeHub.AnonymousClass2 */

        public void onStateChange(int state) {
            HwSwingEyeGazeHub.this.mPowerManagerCallbackHandler.sendEmptyMessage(state);
        }
    };
    private Handler mPowerManagerCallbackHandler;
    private PowerManagerEx mPowerManagerEx;
    private Thread mRegisterEyeGazeFenceThread = null;
    private int mRegisteredFenceAction = 0;
    private int mRemainTime = 0;
    private ContentObserver mScreenOffTimeObserver = new ContentObserver(new Handler()) {
        /* class com.android.server.swing.HwSwingEyeGazeHub.AnonymousClass6 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            super.onChange(isSelfChange);
            Slog.i(HwSwingEyeGazeHub.TAG, "Screen Out Time Switch has changed!");
            HwSwingEyeGazeHub.this.refreshScreenOutTime();
        }
    };
    private final Object registerLock = new Object();

    static /* synthetic */ int access$308(HwSwingEyeGazeHub x0) {
        int i = x0.mAwarenessReconnectTimes;
        x0.mAwarenessReconnectTimes = i + 1;
        return i;
    }

    public HwSwingEyeGazeHub(Context context) {
        Slog.i(TAG, "constructor");
        this.mContext = context;
        this.mAwarenessManager = new AwarenessManager(this.mContext);
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mPowerManagerEx = PowerManagerEx.getDefault();
        context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(KEY_MOTION_ITEM_WAKEUP_GAZE), true, this.mEyeGazeObserver, -1);
        context.getContentResolver().registerContentObserver(Settings.System.getUriFor("screen_off_timeout"), true, this.mScreenOffTimeObserver, -1);
        this.mEyeGazeSwitchValue = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), KEY_MOTION_ITEM_WAKEUP_GAZE, 0, -2);
        this.mAwarenessHandler = new Handler();
        this.mPowerManagerCallbackHandler = new PowerManagerCallbackHandler();
        this.mEyeGazeFenceRunnable = new EyeGazeFenceRunnable();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON);
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF);
        filter.addAction(HW_SCREEN_OFF_FOR_POSITIVE);
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_ACTION_USER_SWITCHED);
        this.mContext.registerReceiver(new SwingStatusReceiver(), filter);
    }

    private class SwingStatusReceiver extends BroadcastReceiver {
        private SwingStatusReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Slog.e(HwSwingEyeGazeHub.TAG, "SwingStatusReceiver intent is null");
                return;
            }
            String action = intent.getAction();
            Slog.i(HwSwingEyeGazeHub.TAG, "on receive action : " + action);
            if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON.equals(action)) {
                HwSwingEyeGazeHub.this.refreshScreenOutTime();
            } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF.equals(action)) {
                HwSwingEyeGazeHub.this.unregisterEyeGazeFence();
                HwSwingEyeGazeHub.this.unregisterPowerManagerCallback();
            } else if (SmartDualCardConsts.SYSTEM_STATE_ACTION_USER_SWITCHED.equals(action)) {
                HwSwingEyeGazeHub.this.refreshScreenOutTime();
            } else if (HwSwingEyeGazeHub.HW_SCREEN_OFF_FOR_POSITIVE.equals(action)) {
                Bundle extras = null;
                try {
                    extras = intent.getExtras();
                } catch (BadParcelableException e) {
                    Slog.e(HwSwingEyeGazeHub.TAG, "SwingStatusReceiver extras is error");
                } catch (Exception e2) {
                    Slog.e(HwSwingEyeGazeHub.TAG, "SwingStatusReceiver extras is error");
                }
                if (extras != null) {
                    if (extras.getBoolean("key_positive")) {
                        HwSwingEyeGazeHub.this.unregisterEyeGazeFence();
                        HwSwingEyeGazeHub.this.unregisterPowerManagerCallback();
                        return;
                    }
                    HwSwingEyeGazeHub.this.refreshScreenOutTime();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean getEyeGazeSwitchEnable() {
        return this.mEyeGazeSwitchValue == 1;
    }

    public void start() {
        Slog.i(TAG, "start");
        refreshScreenOutTime();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshEyeGazeAwarenessConnection() {
        int eyeGazeAction = updateEyeGazeAwarenessAction();
        if (eyeGazeAction == 0) {
            Slog.i(TAG, "refreshAwarenessConnection, no action need to register");
            unregisterEyeGazeFence();
            this.mRegisteredFenceAction = 0;
            if (this.mIsAwarenessConnected) {
                this.mAwarenessManager.disconnectService();
                return;
            }
            return;
        }
        this.mAwarenessEyeGazeFence = new ExtendAwarenessFence(13, 3, eyeGazeAction, null);
        this.mRegisteredFenceAction = eyeGazeAction;
        if (!this.mIsAwarenessConnected) {
            Slog.i(TAG, "refreshAwarenessConnection, connect to awareness service, action=" + eyeGazeAction);
            this.mAwarenessManager.connectService(this.mAwarenessServiceConnection);
            return;
        }
        Slog.i(TAG, "refreshAwarenessConnection, registerEyeGazeFence, eyeGazeAction=" + eyeGazeAction);
        startRegisterEyeGazeFenceThread();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startRegisterEyeGazeFenceThread() {
        synchronized (this.registerLock) {
            if (this.mRegisterEyeGazeFenceThread == null) {
                Slog.i(TAG, "start new RegisterEyeGazeFenceThread");
                this.mRegisterEyeGazeFenceThread = new Thread(this.mEyeGazeFenceRunnable);
                this.mRegisterEyeGazeFenceThread.start();
            }
        }
    }

    private int updateEyeGazeAwarenessAction() {
        boolean isEnableEyeGaze = false;
        this.mEyeGazeSwitchValue = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), KEY_MOTION_ITEM_WAKEUP_GAZE, 0, -2);
        Slog.i(TAG, "eyeGazeSwitchValue = " + this.mEyeGazeSwitchValue);
        if (this.mEyeGazeSwitchValue == 1) {
            isEnableEyeGaze = true;
        }
        if (isEnableEyeGaze) {
            return 0 | 1;
        }
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFenceResult(Bundle result) {
        if (result == null) {
            Slog.e(TAG, "handleFenceResult intent is null !");
            return;
        }
        Bundle swingResult = (Bundle) result.getParcelable("FENCE_SWING_STATUS_CHANGE");
        if (swingResult == null) {
            Slog.e(TAG, "handleFenceResult swingResult is null !");
            return;
        }
        boolean isEyeGazeEnable = swingResult.getBoolean("EyeGaze");
        Slog.i(TAG, "isEyeGaze = " + isEyeGazeEnable);
        if (isEyeGazeEnable) {
            PowerManager powerManager = this.mPowerManager;
            if (powerManager != null && powerManager.isScreenOn()) {
                this.mAwarenessHandler.post(new Runnable() {
                    /* class com.android.server.swing.HwSwingEyeGazeHub.AnonymousClass7 */

                    @Override // java.lang.Runnable
                    public void run() {
                        Slog.i(HwSwingEyeGazeHub.TAG, "userActivity() Method is called ! ");
                        HwSwingEyeGazeHub.this.mPowerManager.userActivity(SystemClock.uptimeMillis(), 0, 0);
                    }
                });
            }
            unregisterEyeGazeFence();
            Slog.i(TAG, "Successfully refresh screen sleep time !");
        }
        this.mAwarenessHandler.post(new Runnable() {
            /* class com.android.server.swing.HwSwingEyeGazeHub.AnonymousClass8 */

            @Override // java.lang.Runnable
            public void run() {
                HwSwingEyeGazeHub.this.reportSwingEyeGazeCountIfNeeded();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshScreenOutTime() {
        Slog.e(TAG, "refreshScreenOutTime is beginning !");
        this.mEyeGazeSwitchValue = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), KEY_MOTION_ITEM_WAKEUP_GAZE, 0, -2);
        if (this.mEyeGazeSwitchValue == 0) {
            Slog.e(TAG, "The Eye Gaze Switch is off !");
            unregisterEyeGazeFence();
            unregisterPowerManagerCallback();
            return;
        }
        int screenOffTimeOut = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_off_timeout", 15000, -2);
        Slog.e(TAG, "screenOffTimeOut = " + screenOffTimeOut + " , mLastRemainTime = " + this.mLastRemainTime);
        unregisterPowerManagerCallback();
        if (screenOffTimeOut == 15000) {
            this.mRemainTime = REMAIN_TIME_MIN;
        } else {
            this.mRemainTime = 16000;
        }
        this.mLastRemainTime = screenOffTimeOut;
        registerPowerManagerCallback(this.mRemainTime);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportSwingEyeGazeCountIfNeeded() {
        long now = SystemClock.uptimeMillis();
        this.mEyeGazeCount++;
        if (now - this.mLastReportTime >= 18000000) {
            HwSwingReport.reportSwingEyeGaze(this.mContext, HwSwingReport.EVENT_ID_SWING_EYE_GAZE_ACTION, this.mEyeGazeCount);
            this.mEyeGazeCount = 0;
            this.mLastReportTime = now;
            return;
        }
        int i = this.mEyeGazeCount;
        if (i >= 200) {
            HwSwingReport.reportSwingEyeGaze(this.mContext, HwSwingReport.EVENT_ID_SWING_EYE_GAZE_ACTION, i);
            this.mEyeGazeCount = 0;
            this.mLastReportTime = now;
        }
    }

    public void unregisterEyeGazeFenceByTouch() {
        unregisterEyeGazeFence();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerEyeGazeFence() {
        if (!this.mIsAwarenessConnected) {
            Slog.i(TAG, "registerEyeGazeFence Awareness not connected !");
        } else if (this.mIsAwarenessRegistered) {
            Slog.i(TAG, "AwarenessEyeGazeFence already registered !");
        } else {
            this.mIsAwarenessRegistered = this.mAwarenessManager.registerAwarenessListener(this.mAwarenessCallback, this.mAwarenessEyeGazeFence, this.mAwarenessListener);
            Slog.i(TAG, "registerEyeGazeFence mRegisteredFenceAction : " + this.mRegisteredFenceAction + ", registerEyeGazeFence mIsAwarenessRegistered = " + this.mIsAwarenessRegistered);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterEyeGazeFence() {
        if (this.mIsAwarenessConnected && this.mIsAwarenessRegistered) {
            Slog.i(TAG, "unregisterEyeGazeFence mRegisteredFenceAction : " + this.mRegisteredFenceAction);
            this.mAwarenessManager.unRegisterAwarenessListener(this.mAwarenessCallback, this.mAwarenessEyeGazeFence, this.mAwarenessListener);
            this.mIsAwarenessRegistered = false;
        }
    }

    private void registerPowerManagerCallback(int remainTime) {
        if (this.mIsPowerManagerRegistered) {
            Slog.i(TAG, "PowerManagerCallback already registered !");
            return;
        }
        Slog.i(TAG, "registerPowerManagerCallback remainTime = " + remainTime);
        PowerManagerEx powerManagerEx = this.mPowerManagerEx;
        if (powerManagerEx != null) {
            this.mIsPowerManagerRegistered = powerManagerEx.registerScreenStateCallback(remainTime, this.mPowerManagerCallback);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterPowerManagerCallback() {
        if (!this.mIsPowerManagerRegistered) {
            Slog.i(TAG, "PowerManagerCallback not registered yet !");
            return;
        }
        Slog.i(TAG, "unregisterPowerManagerCallback done !");
        PowerManagerEx powerManagerEx = this.mPowerManagerEx;
        if (powerManagerEx != null) {
            powerManagerEx.unRegisterScreenStateCallback();
            this.mIsPowerManagerRegistered = false;
        }
    }

    private static class PowerManagerCallbackHandler extends Handler {
        private WeakReference<HwSwingEyeGazeHub> weakReference;

        private PowerManagerCallbackHandler(HwSwingEyeGazeHub hwSwingEyeGazeHub) {
            this.weakReference = new WeakReference<>(hwSwingEyeGazeHub);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            HwSwingEyeGazeHub hwSwingEyeGazeHub = this.weakReference.get();
            if (hwSwingEyeGazeHub != null) {
                Slog.i(HwSwingEyeGazeHub.TAG, "PowerManagerCallback state = " + msg.what);
                if (msg.what == 1) {
                    hwSwingEyeGazeHub.refreshEyeGazeAwarenessConnection();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class EyeGazeFenceRunnable implements Runnable {
        private EyeGazeFenceRunnable() {
        }

        @Override // java.lang.Runnable
        public void run() {
            HwSwingEyeGazeHub.this.registerEyeGazeFence();
            synchronized (HwSwingEyeGazeHub.this.registerLock) {
                HwSwingEyeGazeHub.this.mRegisterEyeGazeFenceThread = null;
            }
        }
    }
}
