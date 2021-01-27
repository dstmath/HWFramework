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

public class HwSwingMultiEyeGazeProcessor extends HwSwingEventProcessor {
    private static final String KEY_MULTI_EYE_GAZE = "multiEyeGaze";
    private static final String KEY_SWING_STATUS_CHANGE = "FENCE_SWING_STATUS_CHANGE";
    private static final int MSG_EVETN_CHANGE = 0;
    public static final int MULTI_EYE_GAZE_FALSE = 0;
    public static final int MULTI_EYE_GAZE_TRUE = 1;
    public static final int MULTI_EYE_GAZE_UNKNOW = -1;
    private static final boolean SWING_HIDE_NOTIFICATON_ENABLED = SystemProperties.getBoolean("hw_mc.systemui.swing_hide_noti_enable", false);
    private IAwarenessListener mAwarenessListener;
    private ExtendAwarenessFence mExtendAwarenessFence;
    private Handler mHandler = new Handler() {
        /* class com.android.server.swing.notification.HwSwingMultiEyeGazeProcessor.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 0 && (msg.obj instanceof Boolean)) {
                HwSwingMultiEyeGazeProcessor.this.handleMultiEyeGazeUpdate(((Boolean) msg.obj).booleanValue());
            }
        }
    };
    private int mMultiEyeGazeState = -1;
    private IRequestCallBack mRegisterRequestCallBack;
    private IRequestCallBack mUnregisterRequestCallBack;

    public HwSwingMultiEyeGazeProcessor(IHwSwingEventDispatcher eventDispatcher, HwSwingEventAvailabler availabler) {
        super(eventDispatcher, availabler);
    }

    public int getMultiEyeGazeStatus() {
        return this.mMultiEyeGazeState;
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
        setMultiEyeGazeStatus(-1);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.swing.HwSwingEventProcessor
    public void doRelease() {
        super.doRelease();
        setMultiEyeGazeStatus(-1);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.swing.HwSwingEventProcessor
    public void register() {
        if (isRegistered()) {
            String str = this.TAG;
            Log.w(str, "register: has already registered! status=" + getRegisterStatus());
        } else if (this.mAwarenessManager != null) {
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
        setMultiEyeGazeStatus(-1);
        if (this.mAwarenessManager != null) {
            setRegisterStatus(2);
            this.mAwarenessManager.unRegisterAwarenessListener(generateUnregisterRequestCallBack(), generateAwarenessFence(), generateAwarenessListener());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMultiEyeGazeUpdate(boolean isMultiEyeGaze) {
        setMultiEyeGazeStatus(isMultiEyeGaze ? 1 : 0);
    }

    private void setMultiEyeGazeStatus(int multiEyeGaze) {
        if (this.mMultiEyeGazeState != multiEyeGaze) {
            this.mMultiEyeGazeState = multiEyeGaze;
            this.mEventDispatcher.dispatchMultiEyeGazeChange(multiEyeGaze);
        }
    }

    private IAwarenessListener generateAwarenessListener() {
        if (this.mAwarenessListener == null) {
            this.mAwarenessListener = new IAwarenessListener.Stub() {
                /* class com.android.server.swing.notification.HwSwingMultiEyeGazeProcessor.AnonymousClass2 */

                @Override // com.huawei.hiai.awareness.service.IAwarenessListener
                public void handleEvent(ExtendAwarenessFence awarenessFence, Bundle result) throws RemoteException {
                    if (result == null) {
                        Log.e(HwSwingMultiEyeGazeProcessor.this.TAG, "handleFenceResult result is null");
                        return;
                    }
                    Bundle status = result.getBundle(HwSwingMultiEyeGazeProcessor.KEY_SWING_STATUS_CHANGE);
                    boolean isMultiEyeGaze = status != null ? status.getBoolean(HwSwingMultiEyeGazeProcessor.KEY_MULTI_EYE_GAZE, false) : false;
                    String str = HwSwingMultiEyeGazeProcessor.this.TAG;
                    Log.i(str, "handleMultiEyeGazeChange multiEyeGaze=" + isMultiEyeGaze);
                    HwSwingMultiEyeGazeProcessor.this.mHandler.removeMessages(0);
                    Message.obtain(HwSwingMultiEyeGazeProcessor.this.mHandler, 0, Boolean.valueOf(isMultiEyeGaze)).sendToTarget();
                }
            };
        }
        return this.mAwarenessListener;
    }

    private ExtendAwarenessFence generateAwarenessFence() {
        if (this.mExtendAwarenessFence == null) {
            this.mExtendAwarenessFence = new ExtendAwarenessFence(13, 11, 1, null);
        }
        return this.mExtendAwarenessFence;
    }

    private IRequestCallBack generateRegisterRequestCallBack() {
        if (this.mRegisterRequestCallBack == null) {
            this.mRegisterRequestCallBack = new IRequestCallBack.Stub() {
                /* class com.android.server.swing.notification.HwSwingMultiEyeGazeProcessor.AnonymousClass3 */

                @Override // com.huawei.hiai.awareness.service.IRequestCallBack
                public void onRequestResult(RequestResult requestResult) throws RemoteException {
                    String str = HwSwingMultiEyeGazeProcessor.this.TAG;
                    Log.i(str, "onRegRequestCallback: result=" + requestResult);
                    if (requestResult != null && requestResult.getTriggerStatus() == 3) {
                        HwSwingMultiEyeGazeProcessor.this.setRegisterStatus(1);
                    }
                }
            };
        }
        return this.mRegisterRequestCallBack;
    }

    private IRequestCallBack generateUnregisterRequestCallBack() {
        if (this.mUnregisterRequestCallBack == null) {
            this.mUnregisterRequestCallBack = new IRequestCallBack.Stub() {
                /* class com.android.server.swing.notification.HwSwingMultiEyeGazeProcessor.AnonymousClass4 */

                @Override // com.huawei.hiai.awareness.service.IRequestCallBack
                public void onRequestResult(RequestResult requestResult) throws RemoteException {
                    String str = HwSwingMultiEyeGazeProcessor.this.TAG;
                    Log.i(str, "onUnRegRequestCallback: result=" + requestResult);
                    if (requestResult != null && requestResult.getTriggerStatus() == 5) {
                        HwSwingMultiEyeGazeProcessor.this.setRegisterStatus(3);
                    }
                }
            };
        }
        return this.mUnregisterRequestCallBack;
    }
}
