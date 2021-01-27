package com.android.server.swing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.huawei.hiai.awareness.AwarenessConstants;
import com.huawei.hiai.awareness.service.AwarenessManager;
import com.huawei.hiai.awareness.service.AwarenessServiceConnection;
import com.huawei.hiai.awareness.service.ExtendAwarenessFence;
import com.huawei.hiai.awareness.service.IAwarenessListener;
import com.huawei.hiai.awareness.service.IRequestCallBack;
import com.huawei.hiai.awareness.service.RequestResult;

public class HwTvSwingMotionGestureHub extends HwSwingMotionGestureBaseHub {
    private static final int CAMERA_TYPE = 20;
    private static final String TAG = "HwTvSwingMotionGestureHub";
    private static final int TAKE_SCREENSHOT_FULLSCREEN = 1;
    private static final String TV_AIVISION_TUTORIAL_WINDOW = "com.huawei.visionkit/com.huawei.visionkit.tutorial.TvAirGestureTutorialActivity";
    private static HwTvSwingMotionGestureHub sInstance;
    private IRequestCallBack mAwarenessCallback = new IRequestCallBack.Stub() {
        /* class com.android.server.swing.HwTvSwingMotionGestureHub.AnonymousClass1 */

        @Override // com.huawei.hiai.awareness.service.IRequestCallBack
        public void onRequestResult(RequestResult result) throws RemoteException {
            Log.i(HwTvSwingMotionGestureHub.TAG, "registerFence() result = " + result);
        }
    };
    private IAwarenessListener mAwarenessListener = new IAwarenessListener.Stub() {
        /* class com.android.server.swing.HwTvSwingMotionGestureHub.AnonymousClass4 */

        @Override // com.huawei.hiai.awareness.service.IAwarenessListener
        public void handleEvent(ExtendAwarenessFence awarenessFence, Bundle result) {
            HwTvSwingMotionGestureHub.this.handleTvFenceResult(result);
        }
    };
    private AwarenessManager mAwarenessManager;
    private AwarenessServiceConnection mAwarenessServiceConnection = new AwarenessServiceConnection() {
        /* class com.android.server.swing.HwTvSwingMotionGestureHub.AnonymousClass2 */

        @Override // com.huawei.hiai.awareness.service.AwarenessServiceConnection
        public void onServiceConnected() {
            HwTvSwingMotionGestureHub hwTvSwingMotionGestureHub = HwTvSwingMotionGestureHub.this;
            hwTvSwingMotionGestureHub.mIsAwarenessConnected = true;
            hwTvSwingMotionGestureHub.mAwarenessReconnectTimes = 0;
            Log.i(HwTvSwingMotionGestureHub.TAG, "mAwarenessServiceConnection onServiceConnected");
            HwTvSwingMotionGestureHub.this.registerTvMotionGestureFence();
        }

        @Override // com.huawei.hiai.awareness.service.AwarenessServiceConnection
        public void onServiceDisconnected() {
            HwTvSwingMotionGestureHub.this.unregisterTvMotionGestureFence();
            HwTvSwingMotionGestureHub hwTvSwingMotionGestureHub = HwTvSwingMotionGestureHub.this;
            hwTvSwingMotionGestureHub.mIsAwarenessConnected = false;
            if (!hwTvSwingMotionGestureHub.isNeedRegisterAwareness()) {
                Log.w(HwTvSwingMotionGestureHub.TAG, "no need to reconnect service");
                return;
            }
            Log.w(HwTvSwingMotionGestureHub.TAG, "wait 10000 ms to reconnect");
            HwTvSwingMotionGestureHub.this.mAwarenessHandler.postDelayed(new Runnable() {
                /* class com.android.server.swing.HwTvSwingMotionGestureHub.AnonymousClass2.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    HwTvSwingMotionGestureHub.this.mAwarenessReconnectTimes++;
                    if (!HwTvSwingMotionGestureHub.this.mIsAwarenessConnected && HwTvSwingMotionGestureHub.this.mAwarenessReconnectTimes < 3) {
                        Log.w(HwTvSwingMotionGestureHub.TAG, "mAwarenessHandler try connectService");
                        if (!HwTvSwingMotionGestureHub.this.mAwarenessManager.connectService(HwTvSwingMotionGestureHub.this.mAwarenessServiceConnection)) {
                            Log.w(HwTvSwingMotionGestureHub.TAG, "connectService failed!");
                            HwTvSwingMotionGestureHub.this.mAwarenessHandler.postDelayed(this, 10000);
                        }
                    }
                }
            }, 10000);
        }
    };
    private int mForwardRewindCount = 0;
    private int mForwardRewindResponseCount = 0;
    private boolean mIsTvAiVisionTutorialWindow = false;
    private boolean mIsTvGestureAwarenessNeedRegister = false;
    private boolean mIsTvPeopleCountAwarenessNeedRegister = false;
    private int mLastRegisteredTvGestureFenceAction = 0;
    private int mLastRegisteredTvGestureFenceStatus = 0;
    private int mLastRegisteredTvPeopleCountFenceAction = 0;
    private int mLastRegisteredTvPeopleCountFenceStatus = 0;
    private int mLastTvAiAction = 0;
    private int mLastTvAiStatus = 0;
    private int mPauseCount = 0;
    private int mPauseResponseCount = 0;
    private int mPeopleCount = 0;
    private int mPeopleResponseCount = 0;
    private int mRegisteredTvGestureFenceAction = 0;
    private int mRegisteredTvGestureFenceStatus = 0;
    private int mRegisteredTvPeopleCountFenceAction = 0;
    private int mRegisteredTvPeopleCountFenceStatus = 0;
    private ContentObserver mSwingMotionObserver = new ContentObserver(this.mAwarenessHandler) {
        /* class com.android.server.swing.HwTvSwingMotionGestureHub.AnonymousClass3 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            Log.i(HwTvSwingMotionGestureHub.TAG, "observer swing motion switch");
            HwTvSwingMotionGestureHub.this.lambda$notifyFocusChange$0$HwTvSwingMotionGestureHub();
        }
    };
    private ExtendAwarenessFence mTvGestureAwarenessFence;
    private ExtendAwarenessFence mTvPeopleCountAwarenessFence;
    private int mVolumeMuteCount = 0;
    private int mVolumeMuteResponseCount = 0;
    private int mVolumeUpDownCount;
    private int mVolumeUpDownResponseCount;

    private HwTvSwingMotionGestureHub(Context context) {
        super(context);
        Log.i(TAG, "constructor");
        this.mAwarenessManager = new AwarenessManager(this.mContext);
        SwingStatusReceiver receiver = new SwingStatusReceiver();
        IntentFilter statusFilter = new IntentFilter();
        statusFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON);
        this.mContext.registerReceiver(receiver, statusFilter);
    }

    public static synchronized HwTvSwingMotionGestureHub getInstance(Context context) {
        HwTvSwingMotionGestureHub hwTvSwingMotionGestureHub;
        synchronized (HwTvSwingMotionGestureHub.class) {
            if (sInstance == null) {
                sInstance = new HwTvSwingMotionGestureHub(context);
            }
            hwTvSwingMotionGestureHub = sInstance;
        }
        return hwTvSwingMotionGestureHub;
    }

    @Override // com.android.server.swing.HwSwingMotionGestureBaseHub
    public boolean dispatchUnhandledKey(final KeyEvent event, String pkgName) {
        if (event == null) {
            Log.e(TAG, "dispatchUnhandledKey event is null");
            return false;
        }
        this.mAwarenessHandler.post(new Runnable() {
            /* class com.android.server.swing.HwTvSwingMotionGestureHub.AnonymousClass5 */

            @Override // java.lang.Runnable
            public void run() {
                HwTvSwingMotionGestureHub.this.reportTvFenceResultByKeyEvent(event);
            }
        });
        return false;
    }

    @Override // com.android.server.swing.HwSwingMotionGestureBaseHub
    public void notifyRotationChange(int rotation) {
    }

    @Override // com.android.server.swing.HwSwingMotionGestureBaseHub
    public void notifyFingersTouching(boolean isTouching) {
    }

    @Override // com.android.server.swing.HwSwingMotionGestureBaseHub
    public void notifyFocusChange(String focusWindowTitle, String focusPkgName) {
        this.mIsTvAiVisionTutorialWindow = TV_AIVISION_TUTORIAL_WINDOW.equals(focusWindowTitle);
        boolean isLastTvAiVisionTutorialWindow = TV_AIVISION_TUTORIAL_WINDOW.equals(this.mFocusWindowTitle);
        if (this.mIsTvAiVisionTutorialWindow || isLastTvAiVisionTutorialWindow) {
            this.mAwarenessHandler.post(new Runnable() {
                /* class com.android.server.swing.$$Lambda$HwTvSwingMotionGestureHub$rW7w6bCgC1HfFU5gsP5Q1dpyzbM */

                @Override // java.lang.Runnable
                public final void run() {
                    HwTvSwingMotionGestureHub.this.lambda$notifyFocusChange$0$HwTvSwingMotionGestureHub();
                }
            });
        }
        this.mFocusWindowTitle = focusWindowTitle;
        HwSwingReport.setFocusPkgName(focusPkgName);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.swing.HwSwingMotionGestureBaseHub
    /* renamed from: refreshAwarenessConnection */
    public void lambda$notifyFocusChange$0$HwTvSwingMotionGestureHub() {
        super.refreshAwarenessConnection();
        refreshTvAwarenessConnection();
        enableSwingMotionDispatch();
    }

    private class SwingStatusReceiver extends BroadcastReceiver {
        private SwingStatusReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.e(HwTvSwingMotionGestureHub.TAG, "SwingStatusReceiver intent is null");
                return;
            }
            String action = intent.getAction();
            Log.i(HwTvSwingMotionGestureHub.TAG, "on receive action:" + action);
            if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON.equals(action)) {
                HwTvSwingMotionGestureHub.this.lambda$notifyFocusChange$0$HwTvSwingMotionGestureHub();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNeedRegisterAwareness() {
        if (this.mIsTvGestureAwarenessNeedRegister || this.mIsTvPeopleCountAwarenessNeedRegister) {
            return true;
        }
        return false;
    }

    private void refreshTvAwarenessConnection() {
        this.mLastRegisteredTvGestureFenceStatus = this.mRegisteredTvGestureFenceStatus;
        this.mLastRegisteredTvGestureFenceAction = this.mRegisteredTvGestureFenceAction;
        int gestureStatus = updateGestureAwarenessStatus();
        int gestureAction = updateGestureAwarenessAction();
        this.mRegisteredTvGestureFenceStatus = gestureStatus;
        this.mRegisteredTvGestureFenceAction = gestureAction;
        this.mLastRegisteredTvPeopleCountFenceStatus = this.mRegisteredTvPeopleCountFenceStatus;
        this.mLastRegisteredTvPeopleCountFenceAction = this.mRegisteredTvPeopleCountFenceAction;
        int peopleCountStatus = updatePeopleCountAwarenessStatus();
        int peopleCountAction = updatePeopleCountAwarenessAction();
        this.mRegisteredTvPeopleCountFenceStatus = peopleCountStatus;
        this.mRegisteredTvPeopleCountFenceAction = peopleCountAction;
        preRefreshAwarenessTvGestureFence(gestureStatus, gestureAction);
        preRefreshAwarenessTvPeopleCountFence(peopleCountStatus, peopleCountAction);
        if (isNeedRegisterAwareness()) {
            refreshAwarenessTvGestureFence(20, gestureStatus, gestureAction);
            refreshAwarenessTvPeopleCountFence(20, peopleCountStatus, peopleCountAction);
        } else if (this.mIsAwarenessConnected) {
            this.mAwarenessManager.disconnectService();
        }
    }

    private void preRefreshAwarenessTvGestureFence(int gestureStatus, int gestureAction) {
        if (gestureStatus == 0 && gestureAction == 0) {
            Log.i(TAG, "preRefreshAwarenessTvGestureFence, no action need to register");
            unregisterTvGestureFence();
            this.mIsTvGestureAwarenessNeedRegister = false;
            return;
        }
        this.mIsTvGestureAwarenessNeedRegister = true;
    }

    private void preRefreshAwarenessTvPeopleCountFence(int gestureStatus, int gestureAction) {
        if (gestureStatus == 0 && gestureAction == 0) {
            Log.i(TAG, "preRefreshAwarenessTvPeopleCountFence, no action need to register");
            unregisterTvPeopleCountGestureFence();
            this.mIsTvPeopleCountAwarenessNeedRegister = false;
            return;
        }
        this.mIsTvPeopleCountAwarenessNeedRegister = true;
    }

    private void refreshAwarenessTvGestureFence(int type, int gestureStatus, int gestureAction) {
        if (gestureStatus == this.mLastRegisteredTvGestureFenceStatus && gestureAction == this.mLastRegisteredTvGestureFenceAction && this.mIsAwarenessConnected) {
            this.mIsTvGestureAwarenessNeedRegister = false;
            Log.i(TAG, "refreshAwarenessTvGestureFence, no registered actions changed");
            return;
        }
        this.mTvGestureAwarenessFence = new ExtendAwarenessFence(type, gestureStatus, gestureAction, null);
        this.mIsTvGestureAwarenessNeedRegister = true;
    }

    private void refreshAwarenessTvPeopleCountFence(int type, int gestureStatus, int gestureAction) {
        if (gestureStatus == this.mLastRegisteredTvPeopleCountFenceStatus && gestureAction == this.mLastRegisteredTvPeopleCountFenceAction && this.mIsAwarenessConnected) {
            Log.i(TAG, "refreshAwarenessTvPeopleCountFence, no registered actions changed");
            this.mIsTvPeopleCountAwarenessNeedRegister = false;
            return;
        }
        this.mTvPeopleCountAwarenessFence = new ExtendAwarenessFence(type, gestureStatus, gestureAction, null);
        this.mIsTvPeopleCountAwarenessNeedRegister = true;
    }

    private void enableSwingMotionDispatch() {
        if (!isNeedRegisterAwareness()) {
            Log.i(TAG, "not need register awareness");
        } else if (!this.mIsAwarenessConnected) {
            this.mAwarenessManager.connectService(this.mAwarenessServiceConnection);
        } else {
            registerTvMotionGestureFence();
        }
    }

    private void disableSwingMotionDispatch() {
        unregisterTvMotionGestureFence();
        if (this.mIsAwarenessConnected) {
            this.mAwarenessManager.disconnectService();
        }
    }

    private int updateGestureAwarenessStatus() {
        int status = 0;
        if (!this.mIsTvAiEnabled) {
            return 0;
        }
        if (this.mIsTvVideoPlayEnabled || this.mIsTvSoundMuteEnabled || this.mIsTvSoundChangeEnabled || this.mIsTvVideoProcessEnabled) {
            status = 12;
        }
        Log.i(TAG, "updateGestureAwarenessStatus status : " + status);
        return status;
    }

    private int updatePeopleCountAwarenessStatus() {
        int status = 0;
        if (!this.mIsTvAiEnabled) {
            return 0;
        }
        if (this.mIsTvPeopleCountEnabled) {
            status = 13;
        }
        Log.i(TAG, "updatePeopleCountAwarenessStatus status : " + status);
        return status;
    }

    private int updateGestureAwarenessAction() {
        int action = 0;
        if (!this.mIsTvAiEnabled) {
            return 0;
        }
        if (this.mIsTvVideoPlayEnabled) {
            action = 0 | 2 | 512;
            if (this.mIsTvAiVisionTutorialWindow) {
                action = action | 256 | 16;
            }
        }
        if (this.mIsTvSoundMuteEnabled) {
            action = action | 1 | 512;
            if (this.mIsTvAiVisionTutorialWindow) {
                action = action | 128 | 16;
            }
        }
        if (this.mIsTvSoundChangeEnabled) {
            action = action | 8 | 512;
            if (this.mIsTvAiVisionTutorialWindow) {
                action = action | 32 | 64 | 16;
            }
        }
        if (this.mIsTvVideoProcessEnabled) {
            action = action | 4 | 512;
            if (this.mIsTvAiVisionTutorialWindow) {
                action = action | 32 | 64 | 16;
            }
        }
        Log.i(TAG, "updateGestureAwarenessAction action : " + action);
        return action;
    }

    private int updatePeopleCountAwarenessAction() {
        int action = 0;
        if (!this.mIsTvAiEnabled) {
            return 0;
        }
        if (this.mIsTvPeopleCountEnabled) {
            action = 0 | 1 | 2;
        }
        Log.i(TAG, "updatePeopleCountAwarenessAction action : " + action);
        return action;
    }

    private void dispatchTvGesture(final int gestureStatus, final int gestureAction, final int gestureOffset) {
        this.mAwarenessHandler.post(new Runnable() {
            /* class com.android.server.swing.HwTvSwingMotionGestureHub.AnonymousClass6 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(HwTvSwingMotionGestureHub.TAG, "dispatch event to application");
                HwTvSwingMotionGestureHub.this.mMotionGestureDispatcher.dispatchTvMotionGesture(gestureStatus, gestureAction, gestureOffset);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleTvFenceResult(Bundle bundle) {
        int gestureOffset;
        int gestureAction;
        if (bundle == null) {
            Log.e(TAG, "handleTvFenceResult bundle is null");
            return;
        }
        Bundle swingResult = (Bundle) bundle.getParcelable("FENCE_SWING_STATUS_CHANGE");
        if (swingResult == null) {
            Log.e(TAG, "handleFenceResult swingResult is null");
            return;
        }
        int status = swingResult.getInt("status");
        int action = swingResult.getInt("action");
        if (status == 13 && (action == 1 || action == 2)) {
            gestureAction = action;
            gestureOffset = action;
        } else if (status == 12) {
            gestureAction = action;
            gestureOffset = swingResult.getInt(AwarenessConstants.TvConstants.TV_OFFSET_GESTURE_NAME);
        } else {
            Log.i(TAG, "Awareness handleTvFenceResult, Status = " + status + " ,Action = " + action);
            return;
        }
        Log.i(TAG, "Awareness handleTvFenceResult, gestureAction = " + gestureAction + ",gestureOffset = " + gestureOffset + ",gestureStatus:" + status);
        dispatchTvGesture(status, gestureAction, gestureOffset);
        reportTvPeopleCountIfNeeded(status, gestureAction, gestureOffset);
        reportTvGestureIfNeeded(status, gestureAction, gestureOffset);
        this.mLastTvAiAction = action;
        this.mLastTvAiStatus = status;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerTvMotionGestureFence() {
        registerTvPeopleCountGestureFence();
        unregisterLastTvPeopleCountGestureFence();
        registerTvGestureFence();
        unregisterLastTvGestureFence();
    }

    private void registerTvPeopleCountGestureFence() {
        if (!this.mIsAwarenessConnected) {
            Log.i(TAG, "registerTvPeopleCountGestureFence Awareness not connected");
            return;
        }
        ExtendAwarenessFence extendAwarenessFence = this.mTvPeopleCountAwarenessFence;
        if (extendAwarenessFence == null) {
            Log.i(TAG, "registerTvPeopleCountGestureFence fence is null");
            return;
        }
        int gestureStatus = extendAwarenessFence.getStatus();
        int gestureAction = this.mTvPeopleCountAwarenessFence.getAction();
        Log.i(TAG, "registerTvPeopleCountGestureFence gestureStatus: " + gestureStatus + ", gestureAction : " + gestureAction);
        if (gestureStatus != 0 && gestureAction != 0) {
            if (!this.mIsTvPeopleCountAwarenessNeedRegister) {
                Log.i(TAG, "no need to registerTvPeopleCountGestureFence");
            } else {
                this.mAwarenessManager.registerAwarenessListener(this.mAwarenessCallback, this.mTvPeopleCountAwarenessFence, this.mAwarenessListener);
            }
        }
    }

    private void registerTvGestureFence() {
        if (!this.mIsAwarenessConnected) {
            Log.i(TAG, "registerTvGestureFence awareness not connected");
            return;
        }
        ExtendAwarenessFence extendAwarenessFence = this.mTvGestureAwarenessFence;
        if (extendAwarenessFence == null) {
            Log.i(TAG, "registerTvGestureFence fence is null");
            return;
        }
        int gestureStatus = extendAwarenessFence.getStatus();
        int gestureAction = this.mTvGestureAwarenessFence.getAction();
        Log.i(TAG, "registerTvGestureFence gestureStatus: " + gestureStatus + ", gestureAction : " + gestureAction);
        if (gestureStatus != 0 && gestureAction != 0) {
            if (!this.mIsTvGestureAwarenessNeedRegister) {
                Log.i(TAG, "no need to registerTvGestureFence");
            } else {
                this.mAwarenessManager.registerAwarenessListener(this.mAwarenessCallback, this.mTvGestureAwarenessFence, this.mAwarenessListener);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterTvMotionGestureFence() {
        unregisterTvPeopleCountGestureFence();
        unregisterTvGestureFence();
    }

    private void unregisterTvPeopleCountGestureFence() {
        if (!this.mIsAwarenessConnected) {
            Log.i(TAG, "unregisterTvPeopleCountGestureFence Awareness not connected");
            return;
        }
        ExtendAwarenessFence extendAwarenessFence = this.mTvPeopleCountAwarenessFence;
        if (extendAwarenessFence == null) {
            Log.i(TAG, "unregisterTvPeopleCountGestureFence fence is null");
            return;
        }
        int gestureStatus = extendAwarenessFence.getStatus();
        int gestureAction = this.mTvPeopleCountAwarenessFence.getAction();
        Log.i(TAG, "unregisterTvPeopleCountGestureFence gestureStatus: " + gestureStatus + ", gestureAction : " + gestureAction);
        if (gestureStatus != 0 && gestureAction != 0) {
            this.mAwarenessManager.unRegisterAwarenessListener(this.mAwarenessCallback, this.mTvPeopleCountAwarenessFence, this.mAwarenessListener);
        }
    }

    private void unregisterLastTvPeopleCountGestureFence() {
        Log.i(TAG, "unregisterLastTvPeopleCountGestureFence status:" + this.mLastRegisteredTvPeopleCountFenceStatus + ",action:" + this.mLastRegisteredTvPeopleCountFenceAction);
        if (this.mLastRegisteredTvPeopleCountFenceStatus == this.mRegisteredTvPeopleCountFenceStatus && this.mLastRegisteredTvPeopleCountFenceAction == this.mRegisteredTvPeopleCountFenceAction) {
            Log.w(TAG, "no need to unregisterLastTvPeopleCountGestureFence");
            return;
        }
        this.mTvPeopleCountAwarenessFence = new ExtendAwarenessFence(20, this.mLastRegisteredTvPeopleCountFenceStatus, this.mLastRegisteredTvPeopleCountFenceAction, null);
        unregisterTvPeopleCountGestureFence();
        this.mTvPeopleCountAwarenessFence = new ExtendAwarenessFence(20, this.mRegisteredTvPeopleCountFenceStatus, this.mRegisteredTvPeopleCountFenceAction, null);
    }

    private void unregisterTvGestureFence() {
        if (!this.mIsAwarenessConnected) {
            Log.i(TAG, "unregisterTvGestureFence Awareness not connected");
            return;
        }
        ExtendAwarenessFence extendAwarenessFence = this.mTvGestureAwarenessFence;
        if (extendAwarenessFence == null) {
            Log.i(TAG, "unregisterTvGestureFence fence is null");
            return;
        }
        int gestureStatus = extendAwarenessFence.getStatus();
        int gestureAction = this.mTvGestureAwarenessFence.getAction();
        Log.i(TAG, "unregisterTvGestureFence gestureStatus: " + gestureStatus + ", gestureAction : " + gestureAction);
        if (gestureStatus != 0 && gestureAction != 0) {
            this.mAwarenessManager.unRegisterAwarenessListener(this.mAwarenessCallback, this.mTvGestureAwarenessFence, this.mAwarenessListener);
        }
    }

    private void unregisterLastTvGestureFence() {
        Log.i(TAG, "unregisterLastTvGestureFence status:" + this.mLastRegisteredTvGestureFenceStatus + ",action:" + this.mLastRegisteredTvGestureFenceAction);
        if (this.mLastRegisteredTvGestureFenceStatus == this.mRegisteredTvGestureFenceStatus && this.mLastRegisteredTvGestureFenceAction == this.mRegisteredTvGestureFenceAction) {
            Log.w(TAG, "no need to unregisterLastTvGestureFence");
            return;
        }
        this.mTvGestureAwarenessFence = new ExtendAwarenessFence(20, this.mLastRegisteredTvGestureFenceStatus, this.mLastRegisteredTvGestureFenceAction, null);
        unregisterTvGestureFence();
        this.mTvGestureAwarenessFence = new ExtendAwarenessFence(20, this.mRegisteredTvGestureFenceStatus, this.mRegisteredTvGestureFenceAction, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportTvFenceResultByKeyEvent(KeyEvent event) {
        int motionStatus = 0;
        int motionAction = 0;
        int motionOffset = 0;
        int keyCode = event.getKeyCode();
        if (keyCode == 23) {
            motionStatus = 12;
            motionAction = 2;
        } else if (keyCode == 164) {
            motionStatus = 12;
            motionAction = 1;
        } else if (keyCode == 728) {
            motionStatus = 12;
            motionAction = 4;
            motionOffset = event.getScanCode();
        } else if (keyCode == 730) {
            motionStatus = 12;
            motionAction = 8;
            motionOffset = event.getScanCode();
        } else if (keyCode == 732) {
            motionStatus = 13;
            motionAction = event.getScanCode();
            motionOffset = event.getScanCode();
        }
        reportTvPeopleResponseIfNeeded(motionStatus, motionAction, motionOffset);
        reportTvGestureResponseIfNeeded(motionStatus, motionAction, motionOffset);
    }

    private void reportTvGestureIfNeeded(int motionStatus, int motionGesture, int motionOffSet) {
        int i;
        if (motionStatus == 12) {
            if (motionGesture == 1) {
                this.mVolumeMuteCount++;
                if (HwSwingReport.reportMotionEventAction(motionStatus, motionGesture, motionOffSet, this.mVolumeMuteCount, true)) {
                    this.mVolumeMuteCount = 0;
                }
            } else if (motionGesture == 2) {
                this.mPauseCount++;
                if (HwSwingReport.reportMotionEventAction(motionStatus, motionGesture, motionOffSet, this.mPauseCount, true)) {
                    this.mPauseCount = 0;
                }
            } else if ((motionGesture == 16 || motionGesture == 512) && (i = this.mLastTvAiStatus) == 12) {
                int i2 = this.mLastTvAiAction;
                if (i2 == 4) {
                    this.mForwardRewindCount++;
                    if (HwSwingReport.reportMotionEventAction(i, i2, motionOffSet, this.mForwardRewindCount, true)) {
                        this.mForwardRewindCount = 0;
                    }
                } else if (i2 == 8) {
                    this.mVolumeUpDownCount++;
                    if (HwSwingReport.reportMotionEventAction(i, i2, motionOffSet, this.mVolumeUpDownCount, true)) {
                        this.mVolumeUpDownCount = 0;
                    }
                }
            }
        }
    }

    private void reportTvPeopleCountIfNeeded(int motionStatus, int motionGesture, int motionOffSet) {
        if (motionStatus == 13) {
            if (motionGesture == 1 || motionGesture == 2) {
                this.mPeopleCount++;
                if (HwSwingReport.reportMotionEventAction(motionStatus, motionGesture, motionOffSet, this.mPeopleCount, true)) {
                    this.mPeopleCount = 0;
                }
            }
        }
    }

    private void reportTvGestureResponseIfNeeded(int motionStatus, int motionGesture, int motionOffSet) {
        if (motionStatus == 12) {
            if (motionGesture == 1) {
                this.mVolumeMuteResponseCount++;
                if (HwSwingReport.reportMotionEventResponse(motionStatus, motionGesture, motionOffSet, this.mVolumeMuteResponseCount, true)) {
                    this.mVolumeMuteResponseCount = 0;
                }
            } else if (motionGesture == 2) {
                this.mPauseResponseCount++;
                if (HwSwingReport.reportMotionEventResponse(motionStatus, motionGesture, motionOffSet, this.mPauseResponseCount, true)) {
                    this.mPauseResponseCount = 0;
                }
            } else if (motionGesture == 4) {
                this.mForwardRewindResponseCount++;
                if (HwSwingReport.reportMotionEventResponse(motionStatus, motionGesture, motionOffSet, this.mForwardRewindResponseCount, true)) {
                    this.mForwardRewindResponseCount = 0;
                }
            } else if (motionGesture == 8) {
                this.mVolumeUpDownResponseCount++;
                if (HwSwingReport.reportMotionEventResponse(motionStatus, motionGesture, motionOffSet, this.mVolumeUpDownResponseCount, true)) {
                    this.mVolumeUpDownResponseCount = 0;
                }
            }
        }
    }

    private void reportTvPeopleResponseIfNeeded(int motionStatus, int motionGesture, int motionOffSet) {
        if (motionStatus == 13) {
            if (motionGesture == 1 || motionGesture == 2) {
                this.mPeopleResponseCount++;
                if (HwSwingReport.reportMotionEventResponse(motionStatus, motionGesture, motionOffSet, this.mPeopleResponseCount, true)) {
                    this.mPeopleResponseCount = 0;
                }
            }
        }
    }
}
