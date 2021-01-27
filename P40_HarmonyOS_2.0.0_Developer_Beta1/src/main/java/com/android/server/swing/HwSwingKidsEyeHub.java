package com.android.server.swing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.huawei.android.biometric.FingerprintServiceEx;
import com.huawei.hiai.awareness.service.AwarenessManager;
import com.huawei.hiai.awareness.service.AwarenessServiceConnection;
import com.huawei.hiai.awareness.service.ExtendAwarenessFence;
import com.huawei.hiai.awareness.service.IAwarenessListener;
import com.huawei.hiai.awareness.service.IRequestCallBack;
import com.huawei.hiai.awareness.service.RequestResult;

public class HwSwingKidsEyeHub {
    private static final String ACTION_SWING_ALL_EXIT = "com.huawei.parentcontrol.swing.all_exit";
    private static final String ACTION_SWING_DISTANCE_ENTER = "com.huawei.parentcontrol.swing.distance_enter";
    private static final String ACTION_SWING_DISTANCE_EXIT = "com.huawei.parentcontrol.swing.distance_exit";
    private static final String ACTION_SWING_LYING_ENTER = "com.huawei.parentcontrol.swing.lying_enter";
    private static final String ACTION_SWING_LYING_EXIT = "com.huawei.parentcontrol.swing.lying_exit";
    private static final String ACTION_SWING_WALKING_ENTER = "com.huawei.parentcontrol.swing.walking_enter";
    private static final String ACTION_SWING_WALKING_EXIT = "com.huawei.parentcontrol.swing.walking_exit";
    private static final String ACTION_TYPE = "ActionType";
    private static final String COMMA = ",";
    private static final int DISTANCE_CORRECT_VALUE = 20;
    private static final int DISTANCE_NEAR_VALUE = 20;
    private static final String KEY_SWING_STATUS = "KidsSmartEyeProtection_Status";
    private static final int NOT_SUPPORT_SWING = -1;
    private static final int OPEN_AGE_WAIT_TIME_MS = 1000;
    private static final String PKG_PARENTCONTROL = "com.huawei.parentcontrol";
    private static final int RECONNECT_MAX_COUNT = 3;
    private static final int RECOONECT_WAIT_TIME_MS = 10000;
    private static final String SWING_AGE_ACTION = "swing.age";
    private static final String SWING_DISTANCE_ACTION = "swing.distance";
    private static final String SWING_HAVE_FACE_ACTION = "swing.have_face";
    private static final String SWING_LYING_ACTION = "swing.lying";
    private static final String SWING_WALKING_ACTION = "swing.walking";
    private static final String TAG = "HwSwingKidsEyeHub";
    private static HwSwingKidsEyeHub sInstance;
    private Holder mAgeHolder;
    private Handler mAwarenessHandler;
    private AwarenessManager mAwarenessManager;
    private int mAwarenessReconnectTimes;
    private AwarenessServiceConnection mAwarenessServiceConnection = new AwarenessServiceConnection() {
        /* class com.android.server.swing.HwSwingKidsEyeHub.AnonymousClass1 */

        @Override // com.huawei.hiai.awareness.service.AwarenessServiceConnection
        public void onServiceConnected() {
            HwSwingKidsEyeHub.this.mIsAwarenessConnected = true;
            HwSwingKidsEyeHub.this.mAwarenessReconnectTimes = 0;
            Log.w(HwSwingKidsEyeHub.TAG, "mAwarenessServiceConnection onServiceConnected..");
            HwSwingKidsEyeHub.this.refreshKidsEyeProtectStatus();
        }

        @Override // com.huawei.hiai.awareness.service.AwarenessServiceConnection
        public void onServiceDisconnected() {
            HwSwingKidsEyeHub.this.mIsAwarenessConnected = false;
            HwSwingKidsEyeHub.this.mIsEyeProtectRunning = false;
            Log.w(HwSwingKidsEyeHub.TAG, "mAwarenessServiceConnection onServiceDisconnected..");
            if (!HwSwingKidsEyeHub.this.isNeedToReconnect()) {
                Log.w(HwSwingKidsEyeHub.TAG, "onServiceDisconnected and not need to reconnect");
                return;
            }
            if (HwSwingKidsEyeHub.this.mAwarenessHandler == null) {
                HwSwingKidsEyeHub.this.mAwarenessHandler = new Handler();
            }
            Log.w(HwSwingKidsEyeHub.TAG, "wait 10000 ms to reconnect..");
            HwSwingKidsEyeHub.this.mAwarenessHandler.postDelayed(new Runnable() {
                /* class com.android.server.swing.HwSwingKidsEyeHub.AnonymousClass1.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    HwSwingKidsEyeHub.access$108(HwSwingKidsEyeHub.this);
                    if (HwSwingKidsEyeHub.this.mIsAwarenessConnected) {
                        return;
                    }
                    if (HwSwingKidsEyeHub.this.mAwarenessReconnectTimes < 3) {
                        Log.e(HwSwingKidsEyeHub.TAG, "mAwarenessHandler try connectService..");
                        if (!HwSwingKidsEyeHub.this.mAwarenessManager.connectService(HwSwingKidsEyeHub.this.mAwarenessServiceConnection)) {
                            Log.e(HwSwingKidsEyeHub.TAG, "connectService failed!");
                            HwSwingKidsEyeHub.this.mAwarenessHandler.postDelayed(this, 10000);
                            return;
                        }
                        return;
                    }
                    Log.e(HwSwingKidsEyeHub.TAG, "mAwarenessHandler try connectService 3 times, forceExit");
                    HwSwingKidsEyeHub.this.forceExit();
                }
            }, 10000);
        }
    };
    private Context mContext;
    private Holder mDistanceHolder;
    private Holder mHaveFaceHolder;
    private boolean mIsAwarenessConnected = false;
    private boolean mIsEyeProtectRunning = false;
    private boolean mIsKidsAge = false;
    private boolean mIsKidsEyeProtectOpened = false;
    private boolean mIsNeedToReconnect = true;
    private ContentObserver mKidsEyeStatusObserver = new ContentObserver(new Handler()) {
        /* class com.android.server.swing.HwSwingKidsEyeHub.AnonymousClass2 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            super.onChange(isSelfChange);
            Log.w(HwSwingKidsEyeHub.TAG, "mKidsEyeStatusObserver onChange");
            HwSwingKidsEyeHub.this.refreshAwarenessConnection();
        }
    };
    private Holder mLyingHolder;
    private Holder mWalkingHolder;

    /* access modifiers changed from: private */
    public enum Status {
        NONE,
        ENTER,
        EXIT,
        ERROR
    }

    static /* synthetic */ int access$108(HwSwingKidsEyeHub x0) {
        int i = x0.mAwarenessReconnectTimes;
        x0.mAwarenessReconnectTimes = i + 1;
        return i;
    }

    private HwSwingKidsEyeHub(Context context) {
        Log.w(TAG, "constructor");
        this.mContext = context;
        this.mAwarenessManager = new AwarenessManager(this.mContext);
        this.mAgeHolder = new Holder(SWING_AGE_ACTION, 4, 1 | 2 | 4 | 8);
        this.mDistanceHolder = new Holder(SWING_DISTANCE_ACTION, 6, 3);
        this.mWalkingHolder = new Holder(SWING_WALKING_ACTION, 9, 3);
        this.mLyingHolder = new Holder(SWING_LYING_ACTION, 8, 3);
        this.mHaveFaceHolder = new Holder(SWING_HAVE_FACE_ACTION, 1, 1);
        IntentFilter filter = new IntentFilter();
        filter.addAction(FingerprintServiceEx.ACTION_USER_PRESENT);
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF);
        context.registerReceiver(new SwingStatusReceiver(), filter);
    }

    public static synchronized HwSwingKidsEyeHub getInstance(Context context) {
        HwSwingKidsEyeHub hwSwingKidsEyeHub;
        synchronized (HwSwingKidsEyeHub.class) {
            if (sInstance == null) {
                if (context != null) {
                    sInstance = new HwSwingKidsEyeHub(context.getApplicationContext());
                } else {
                    throw new NullPointerException("HwSwingKidsEyeHub getInstance() -> context cannot be null");
                }
            }
            hwSwingKidsEyeHub = sInstance;
        }
        return hwSwingKidsEyeHub;
    }

    public void start() {
        Log.w(TAG, "start");
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(KEY_SWING_STATUS), true, this.mKidsEyeStatusObserver);
        refreshAwarenessConnection();
    }

    public void stop() {
        Log.w(TAG, "stop");
        this.mContext.getContentResolver().unregisterContentObserver(this.mKidsEyeStatusObserver);
        onKidsEyeModeSwitchClose();
    }

    private void setNeedToReconnect(boolean isNeedToReconnect) {
        this.mIsNeedToReconnect = isNeedToReconnect;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNeedToReconnect() {
        return this.mIsNeedToReconnect;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshAwarenessConnection() {
        if (!this.mIsAwarenessConnected) {
            this.mIsEyeProtectRunning = false;
            Log.w(TAG, "refreshAwarenessConnection, connect to awareness service.");
            setNeedToReconnect(true);
            this.mAwarenessManager.connectService(this.mAwarenessServiceConnection);
            return;
        }
        refreshKidsEyeProtectStatus();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshKidsEyeProtectStatus() {
        boolean z = true;
        if (getParentSwingStatus() != 1) {
            z = false;
        }
        this.mIsKidsEyeProtectOpened = z;
        if (this.mIsKidsEyeProtectOpened) {
            onKidsEyeModeSwitchOpen();
        } else {
            onKidsEyeModeSwitchClose();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getParentSwingStatus() {
        try {
            return Settings.Secure.getInt(this.mContext.getContentResolver(), KEY_SWING_STATUS);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "getParentSwingStatus SettingNotFoundException");
            return -1;
        }
    }

    private void onKidsEyeModeSwitchOpen() {
        Log.w(TAG, "onKidsEyeModeSwitchOpen");
        handleKidsAge();
    }

    private void onKidsEyeModeSwitchClose() {
        Log.w(TAG, "onKidsEyeModeSwitchClose");
        stopEyeProtect("onKidsEyeModeSwitchClose");
        setNeedToReconnect(false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startEyeProtect() {
        Log.i(TAG, "startEyeProtect");
        if (!this.mIsEyeProtectRunning) {
            this.mIsEyeProtectRunning = true;
            registerFence(this.mDistanceHolder);
            registerFence(this.mWalkingHolder);
            registerFence(this.mLyingHolder);
        }
    }

    private void stopEyeProtect(String reason) {
        Log.w(TAG, "stopEyeProtect:" + reason);
        if (this.mIsEyeProtectRunning) {
            this.mIsEyeProtectRunning = false;
            unregisterFence(this.mDistanceHolder);
            unregisterFence(this.mWalkingHolder);
            unregisterFence(this.mLyingHolder);
        }
        forceExit();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAgeAction(Bundle bundle) {
        int actionType = bundle.getInt(ACTION_TYPE);
        int swingDeviceStatus = bundle.getInt("SwingDeviceStatus");
        int ageGroup = bundle.getInt("AgeGroup");
        Log.w(TAG, "handleAgeAction:actionType=" + actionType + ", swingDeviceStatus=" + swingDeviceStatus + ", ageGroup=" + ageGroup);
        if (swingDeviceStatus == 0) {
            stopEyeProtect("Age Action, deviceStatus=0");
        } else if (actionType == 1 || actionType == 2) {
            handleKidsAge();
        } else {
            handleAdultAge();
        }
    }

    private void handleKidsAge() {
        this.mIsKidsAge = true;
        if (getScrenState()) {
            startEyeProtect();
        } else {
            this.mIsEyeProtectRunning = false;
        }
    }

    private void handleAdultAge() {
        this.mIsKidsAge = false;
        stopEyeProtect("Age Action, Adult age");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFaceNumAction(Bundle bundle) {
        int swingDeviceStatus = bundle.getInt("SwingDeviceStatus");
        int faceNum = bundle.getInt("FaceNum");
        boolean isHasFace = bundle.getBoolean("HasFace");
        Log.w(TAG, "handleFaceNumAction:swingDeviceStatus=" + swingDeviceStatus + ", faceNum=" + faceNum + ", hasFace=" + isHasFace);
        if (swingDeviceStatus == 0 || !isHasFace) {
            stopEyeProtect("Have Face:" + swingDeviceStatus + "," + isHasFace);
        } else if (this.mIsKidsEyeProtectOpened && this.mIsKidsAge) {
            Log.w(TAG, "handleFaceNumAction have face and isKidsAge, need to start eye protect");
            startEyeProtect();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDistanceAction(Bundle bundle) {
        int actionType = bundle.getInt(ACTION_TYPE);
        int faceDistance = bundle.getInt("FaceDistance");
        Log.w(TAG, "handleDistanceAction:" + actionType + "," + faceDistance);
        if (faceDistance > 0) {
            if (actionType == 1 && this.mDistanceHolder.getStatus() != Status.ENTER) {
                callService(ACTION_SWING_DISTANCE_ENTER);
                this.mDistanceHolder.setStatus(Status.ENTER);
            }
            if (actionType == 2 && this.mDistanceHolder.getStatus() != Status.EXIT) {
                callService(ACTION_SWING_DISTANCE_EXIT);
                this.mDistanceHolder.setStatus(Status.EXIT);
            }
        } else if (this.mDistanceHolder.getStatus() != Status.EXIT) {
            callService(ACTION_SWING_DISTANCE_EXIT);
            this.mDistanceHolder.setStatus(Status.EXIT);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWalkingAction(Bundle bundle) {
        int actionType = bundle.getInt(ACTION_TYPE);
        Log.w(TAG, "handleWalkingAction:actionType=" + actionType);
        if (actionType == 1 && this.mWalkingHolder.getStatus() != Status.ENTER) {
            callService(ACTION_SWING_WALKING_ENTER);
            this.mWalkingHolder.setStatus(Status.ENTER);
        }
        if (actionType == 2 && this.mWalkingHolder.getStatus() != Status.EXIT) {
            callService(ACTION_SWING_WALKING_EXIT);
            this.mWalkingHolder.setStatus(Status.EXIT);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleLyingAction(Bundle bundle) {
        int actionType = bundle.getInt(ACTION_TYPE);
        Log.w(TAG, "handleLyingAction:actionType=" + actionType);
        if (actionType == 1 && this.mLyingHolder.getStatus() != Status.ENTER) {
            callService(ACTION_SWING_LYING_ENTER);
            this.mLyingHolder.setStatus(Status.ENTER);
        }
        if (actionType == 2 && this.mLyingHolder.getStatus() != Status.EXIT) {
            callService(ACTION_SWING_LYING_EXIT);
            this.mLyingHolder.setStatus(Status.EXIT);
        }
    }

    private void callService(String action) {
        if (action == null || action.length() == 0) {
            Log.e(TAG, "callService get invalid action");
            return;
        }
        Intent intent = new Intent();
        intent.setAction(action);
        intent.setPackage(PKG_PARENTCONTROL);
        try {
            this.mContext.startService(intent);
        } catch (IllegalStateException e) {
            Log.e(TAG, "callService IllegalStateException");
        } catch (SecurityException e2) {
            Log.e(TAG, "callService SecurityException");
        }
        Log.w(TAG, "start HwSwingService action=" + action);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void forceExit() {
        Log.w(TAG, "forceExit");
        callService(ACTION_SWING_ALL_EXIT);
    }

    private void registerFence(Holder holder) {
        if (!this.mIsAwarenessConnected) {
            Log.e(TAG, "Awareness not connected");
            return;
        }
        boolean isSuccess = holder.register();
        Log.w(TAG, "registerFence:" + isSuccess + ", action=" + holder.action);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterFence(Holder holder) {
        if (!this.mIsAwarenessConnected) {
            Log.e(TAG, "Awareness not connected");
            return;
        }
        boolean isSuccess = holder.unregister();
        Log.w(TAG, "unregisterFence:" + isSuccess + ", action=" + holder.action);
    }

    /* access modifiers changed from: private */
    public final class Holder {
        private String action;
        private IRequestCallBack callBack;
        private ExtendAwarenessFence fence;
        private IAwarenessListener mListener;
        private Status status = Status.NONE;

        Holder(String action2, int swingStatus, int swingActions) {
            this.action = action2;
            this.fence = new ExtendAwarenessFence(13, swingStatus, swingActions, null);
            this.callBack = new MyFenceRegisterCallback(action2, swingStatus, swingActions);
            this.mListener = new MyAwarenessListener(action2);
        }

        public boolean register() {
            setStatus(Status.NONE);
            return HwSwingKidsEyeHub.this.mAwarenessManager.registerAwarenessListener(this.callBack, this.fence, this.mListener);
        }

        public boolean unregister() {
            setStatus(Status.NONE);
            return HwSwingKidsEyeHub.this.mAwarenessManager.unRegisterAwarenessListener(this.callBack, this.fence, this.mListener);
        }

        public void setStatus(Status status2) {
            this.status = status2;
        }

        public Status getStatus() {
            return this.status;
        }
    }

    private class MyFenceRegisterCallback extends IRequestCallBack.Stub {
        private String mAction;
        private int mActions;
        private int mStatus;

        MyFenceRegisterCallback(String action, int status, int actions) {
            this.mAction = action;
            this.mStatus = status;
            this.mActions = actions;
        }

        @Override // com.huawei.hiai.awareness.service.IRequestCallBack
        public void onRequestResult(RequestResult result) throws RemoteException {
            Log.w(HwSwingKidsEyeHub.TAG, "MyFenceRegisterCallback(" + this.mAction + "," + this.mStatus + "," + this.mActions + ") onRequestResult: swingStatus=" + result.getTriggerStatus() + ", errorCode=" + result.getErrorCode() + ", errorResult=" + result.getErrorResult());
        }
    }

    private class MyAwarenessListener extends IAwarenessListener.Stub {
        private String mAction;

        MyAwarenessListener(String action) {
            this.mAction = action;
        }

        @Override // com.huawei.hiai.awareness.service.IAwarenessListener
        public void handleEvent(ExtendAwarenessFence extendAwarenessFence, Bundle bundle) {
            if (bundle == null) {
                Log.e(HwSwingKidsEyeHub.TAG, "MyAwarenessListener handleEvent bundle is null:" + this.mAction);
                return;
            }
            Bundle swingResult = (Bundle) bundle.getParcelable("FENCE_SWING_STATUS_CHANGE");
            if (swingResult == null) {
                Log.e(HwSwingKidsEyeHub.TAG, "MyAwarenessListener handleEvent swingResult is null:" + this.mAction);
                return;
            }
            String str = this.mAction;
            char c = 65535;
            switch (str.hashCode()) {
                case -2130839483:
                    if (str.equals(HwSwingKidsEyeHub.SWING_LYING_ACTION)) {
                        c = 4;
                        break;
                    }
                    break;
                case -151725084:
                    if (str.equals(HwSwingKidsEyeHub.SWING_HAVE_FACE_ACTION)) {
                        c = 1;
                        break;
                    }
                    break;
                case 1450849929:
                    if (str.equals(HwSwingKidsEyeHub.SWING_WALKING_ACTION)) {
                        c = 3;
                        break;
                    }
                    break;
                case 1481568783:
                    if (str.equals(HwSwingKidsEyeHub.SWING_AGE_ACTION)) {
                        c = 0;
                        break;
                    }
                    break;
                case 1991589093:
                    if (str.equals(HwSwingKidsEyeHub.SWING_DISTANCE_ACTION)) {
                        c = 2;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                HwSwingKidsEyeHub.this.handleAgeAction(swingResult);
            } else if (c == 1) {
                HwSwingKidsEyeHub.this.handleFaceNumAction(swingResult);
            } else if (c == 2) {
                HwSwingKidsEyeHub.this.handleDistanceAction(swingResult);
            } else if (c == 3) {
                HwSwingKidsEyeHub.this.handleWalkingAction(swingResult);
            } else if (c != 4) {
                Log.e(HwSwingKidsEyeHub.TAG, "MyAwarenessListener handleEvent unkown action.");
            } else {
                HwSwingKidsEyeHub.this.handleLyingAction(swingResult);
            }
        }
    }

    private class SwingStatusReceiver extends BroadcastReceiver {
        private SwingStatusReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.e(HwSwingKidsEyeHub.TAG, "SwingStatusReceiver intent is null");
                return;
            }
            String action = intent.getAction();
            Log.i(HwSwingKidsEyeHub.TAG, "on receive action : " + action);
            if (FingerprintServiceEx.ACTION_USER_PRESENT.equals(action)) {
                if (HwSwingKidsEyeHub.this.getParentSwingStatus() == 1) {
                    HwSwingKidsEyeHub.this.startEyeProtect();
                }
            } else if (!SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF.equals(action)) {
                Log.w(HwSwingKidsEyeHub.TAG, "error action:" + action);
            } else if (HwSwingKidsEyeHub.this.mIsEyeProtectRunning) {
                HwSwingKidsEyeHub.this.mIsEyeProtectRunning = false;
                HwSwingKidsEyeHub hwSwingKidsEyeHub = HwSwingKidsEyeHub.this;
                hwSwingKidsEyeHub.unregisterFence(hwSwingKidsEyeHub.mDistanceHolder);
                HwSwingKidsEyeHub hwSwingKidsEyeHub2 = HwSwingKidsEyeHub.this;
                hwSwingKidsEyeHub2.unregisterFence(hwSwingKidsEyeHub2.mWalkingHolder);
                HwSwingKidsEyeHub hwSwingKidsEyeHub3 = HwSwingKidsEyeHub.this;
                hwSwingKidsEyeHub3.unregisterFence(hwSwingKidsEyeHub3.mLyingHolder);
            }
        }
    }

    private boolean getScrenState() {
        PowerManager pm = null;
        if (this.mContext.getSystemService("power") instanceof PowerManager) {
            pm = (PowerManager) this.mContext.getSystemService("power");
        }
        if (pm != null) {
            return pm.isInteractive();
        }
        return false;
    }
}
