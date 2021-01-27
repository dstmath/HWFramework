package com.android.server.swing.notification;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.swing.HwSwingEventProcessor;
import com.huawei.hiai.awareness.service.ExtendAwarenessFence;
import com.huawei.hiai.awareness.service.IAwarenessListener;
import com.huawei.hiai.awareness.service.IRequestCallBack;
import com.huawei.hiai.awareness.service.RequestResult;

public class HwSwingFaceTrackingProcessor extends HwSwingEventProcessor {
    private static final int EVENT_DARK_LIGHT = -1;
    private static final int EVENT_MAX_FACE_CHANGED = 1;
    private static final int EVENT_MAX_FACE_CHANGE_ERROR = -1;
    private static final int EVENT_MAX_FACE_NO_CHANGE = 0;
    private static final int EVENT_MULTI_FACE = 2;
    private static final int EVENT_NOT_WORK = -2;
    private static final int EVENT_NO_FACE = 0;
    private static final int EVENT_SINGLE_FACE = 1;
    public static final int FACE_ERROR = -1;
    public static final int FACE_NONOWNER = 1;
    public static final int FACE_OWNER = 0;
    public static final int FACE_UNKNOW = -2;
    private static final String KEY_FACE_NUM = "FaceNum";
    private static final String KEY_MAX_FACE_CHANGE = "maxFaceInfoChange";
    private static final String KEY_SWING_STATUS_CHANGE = "FENCE_SWING_STATUS_CHANGE";
    private static final int MAX_FACE_CHANGED = 1;
    private static final int MSG_EVETN_CHANGE = 0;
    private static final boolean SWING_HIDE_NOTIFICATON_ENABLED = SystemProperties.getBoolean("hw_mc.systemui.swing_hide_noti_enable", false);
    private IAwarenessListener mAwarenessListener;
    private ExtendAwarenessFence mExtendAwarenessFence;
    private Handler mHandler = new Handler() {
        /* class com.android.server.swing.notification.HwSwingFaceTrackingProcessor.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                HwSwingFaceTrackingProcessor.this.handleFaceTrackingUpdate(((Integer) msg.obj).intValue());
            }
        }
    };
    private boolean mIsFirstFrame;
    private IRequestCallBack mRegisterRequestCallBack;
    private IRequestCallBack mUnregisterRequestCallBack;
    private int mWho = -2;

    public HwSwingFaceTrackingProcessor(IHwSwingEventDispatcher eventDispatcher, HwSwingEventAvailabler availabler) {
        super(eventDispatcher, availabler);
    }

    public int getWho() {
        return this.mWho;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.swing.HwSwingEventProcessor
    public boolean isEnabled() {
        return SWING_HIDE_NOTIFICATON_ENABLED;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.swing.HwSwingEventProcessor
    public void doInit() {
        super.doInit();
        setWho(-2);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.swing.HwSwingEventProcessor
    public void doRelease() {
        super.doRelease();
        setWho(-2);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.swing.HwSwingEventProcessor
    public void register() {
        if (isRegistered()) {
            String str = this.TAG;
            Log.w(str, "register: has already registered! status=" + getRegisterStatus());
        } else if (this.mAwarenessManager != null) {
            this.mIsFirstFrame = true;
            setRegisterStatus(0);
            this.mAwarenessManager.registerAwarenessListener(generateRegisterRequestCallBack(), generateAwarenessFence(), generateAwarenessListener());
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.swing.HwSwingEventProcessor
    public void unRegister() {
        if (!isRegistered()) {
            String str = this.TAG;
            Log.w(str, "unRegister: has already unregistered! status=" + getRegisterStatus());
            return;
        }
        setWho(-2);
        if (this.mAwarenessManager != null) {
            setRegisterStatus(2);
            this.mAwarenessManager.unRegisterAwarenessListener(generateUnregisterRequestCallBack(), generateAwarenessFence(), generateAwarenessListener());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFaceTrackingUpdate(int who) {
        if (who == -1) {
            unRegister();
        } else {
            setWho(who);
        }
    }

    private void setWho(int who) {
        if (who != this.mWho) {
            this.mWho = who;
            this.mEventDispatcher.dispatchFaceTrackingChange(this.mWho);
        }
    }

    private IAwarenessListener generateAwarenessListener() {
        if (this.mAwarenessListener == null) {
            this.mAwarenessListener = new IAwarenessListener.Stub() {
                /* class com.android.server.swing.notification.HwSwingFaceTrackingProcessor.AnonymousClass2 */

                @Override // com.huawei.hiai.awareness.service.IAwarenessListener
                public void handleEvent(ExtendAwarenessFence awarenessFence, Bundle result) throws RemoteException {
                    if (result == null) {
                        Log.e(HwSwingFaceTrackingProcessor.this.TAG, "handleFenceResult result is null");
                        return;
                    }
                    Bundle status = result.getBundle(HwSwingFaceTrackingProcessor.KEY_SWING_STATUS_CHANGE);
                    int faceNum = status != null ? status.getInt(HwSwingFaceTrackingProcessor.KEY_FACE_NUM, 0) : 0;
                    int maxFaceChange = 1;
                    if (status != null) {
                        maxFaceChange = status.getInt(HwSwingFaceTrackingProcessor.KEY_MAX_FACE_CHANGE, 1);
                    }
                    String str = HwSwingFaceTrackingProcessor.this.TAG;
                    Log.i(str, "handMaxFaceChangeEvent faceNum=" + faceNum + ";maxFaceChange=" + maxFaceChange);
                    int who = HwSwingFaceTrackingProcessor.this.processEvent(faceNum, maxFaceChange);
                    HwSwingFaceTrackingProcessor.this.mHandler.removeMessages(0);
                    Message.obtain(HwSwingFaceTrackingProcessor.this.mHandler, 0, Integer.valueOf(who)).sendToTarget();
                }
            };
        }
        return this.mAwarenessListener;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int processEvent(int faceNum, int maxFaceChange) {
        if (this.mIsFirstFrame) {
            this.mIsFirstFrame = false;
            setWho(-2);
        }
        if (isEventError(faceNum, maxFaceChange)) {
            return -1;
        }
        if (isEventOwer(faceNum, maxFaceChange)) {
            return 0;
        }
        if (isEventNonOwer(faceNum, maxFaceChange)) {
            return 1;
        }
        String str = this.TAG;
        Log.e(str, "processEvent: never gonna happen! unknow event: faceNum=" + faceNum + ";maxFaceChange=" + maxFaceChange);
        return -2;
    }

    private boolean isEventError(int faceNum, int maxFaceChange) {
        if (faceNum == 0 && maxFaceChange == -1) {
            return true;
        }
        if (faceNum == 2 && maxFaceChange == -1) {
            return true;
        }
        if (faceNum == -1 && maxFaceChange == -1) {
            return true;
        }
        if (faceNum == -2 && maxFaceChange == -2) {
            return true;
        }
        return false;
    }

    private boolean isEventNonOwer(int faceNum, int maxFaceChange) {
        if (faceNum == 0 && maxFaceChange == 1) {
            return true;
        }
        if (faceNum == 1 && maxFaceChange == 1) {
            return true;
        }
        return false;
    }

    private boolean isEventOwer(int faceNum, int maxFaceChange) {
        if (faceNum == 1 && maxFaceChange == 0) {
            return true;
        }
        if (faceNum == -1 && maxFaceChange == 0) {
            return true;
        }
        return false;
    }

    private ExtendAwarenessFence generateAwarenessFence() {
        if (this.mExtendAwarenessFence == null) {
            this.mExtendAwarenessFence = new ExtendAwarenessFence(13, 2, 1, null);
        }
        return this.mExtendAwarenessFence;
    }

    private IRequestCallBack generateRegisterRequestCallBack() {
        if (this.mRegisterRequestCallBack == null) {
            this.mRegisterRequestCallBack = new IRequestCallBack.Stub() {
                /* class com.android.server.swing.notification.HwSwingFaceTrackingProcessor.AnonymousClass3 */

                @Override // com.huawei.hiai.awareness.service.IRequestCallBack
                public void onRequestResult(RequestResult requestResult) throws RemoteException {
                    String str = HwSwingFaceTrackingProcessor.this.TAG;
                    Log.i(str, "onRegRequestCallback: result=" + requestResult);
                    if (requestResult != null && requestResult.getTriggerStatus() == 3) {
                        HwSwingFaceTrackingProcessor.this.setRegisterStatus(1);
                    }
                }
            };
        }
        return this.mRegisterRequestCallBack;
    }

    private IRequestCallBack generateUnregisterRequestCallBack() {
        if (this.mUnregisterRequestCallBack == null) {
            this.mUnregisterRequestCallBack = new IRequestCallBack.Stub() {
                /* class com.android.server.swing.notification.HwSwingFaceTrackingProcessor.AnonymousClass4 */

                @Override // com.huawei.hiai.awareness.service.IRequestCallBack
                public void onRequestResult(RequestResult requestResult) throws RemoteException {
                    String str = HwSwingFaceTrackingProcessor.this.TAG;
                    Log.i(str, "onUnRegRequestCallback: result=" + requestResult);
                    if (requestResult != null && requestResult.getTriggerStatus() == 5) {
                        HwSwingFaceTrackingProcessor.this.setRegisterStatus(3);
                    }
                }
            };
        }
        return this.mUnregisterRequestCallBack;
    }
}
