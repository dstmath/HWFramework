package com.huawei.facerecognition.task;

import com.huawei.facerecognition.FaceRecognizeEvent;
import com.huawei.facerecognition.FaceRecognizeManager.CallbackHolder;
import com.huawei.facerecognition.FaceRecognizeManager.ServiceHolder;
import com.huawei.facerecognition.base.HwSecurityMsgCenter;
import com.huawei.facerecognition.base.HwSecurityTaskBase;
import com.huawei.facerecognition.base.HwSecurityTaskBase.EventListener;
import com.huawei.facerecognition.base.HwSecurityTaskBase.RetCallback;
import com.huawei.facerecognition.base.HwSecurityTaskBase.TimerOutProc;
import com.huawei.facerecognition.base.HwSecurityTimerTask;
import com.huawei.facerecognition.request.FaceRecognizeRequest;
import com.huawei.facerecognition.utils.LogUtil;

public class DoCancelAuthTask extends FaceRecognizeTask implements EventListener {
    private static final long TIMEOUT = 3000;
    private int mRetErrorCode;
    private int mRetUserId;
    TimerOutProc mTimeoutProc = new TimerOutProc() {
        public void onTimerOut() {
            DoCancelAuthTask.this.-wrap0(0);
        }
    };
    private HwSecurityTimerTask mTimer = new HwSecurityTimerTask();

    public DoCancelAuthTask(HwSecurityTaskBase parent, RetCallback callback, FaceRecognizeRequest request) {
        super(parent, callback, request);
        HwSecurityMsgCenter.staticRegisterEvent(10, this, this);
        HwSecurityMsgCenter.staticRegisterEvent(11, this, this);
        HwSecurityMsgCenter.staticRegisterEvent(12, this, this);
    }

    public int doAction() {
        LogUtil.i("", "do cancel auth task");
        if (ServiceHolder.getInstance().cancelAuthentication() != 0) {
            return 0;
        }
        this.mTimer.setTimeout(TIMEOUT, this.mTimeoutProc);
        return -1;
    }

    public void onStop() {
        this.mTimer.cancel();
        HwSecurityMsgCenter.staticUnregisterEvent(10, this);
        HwSecurityMsgCenter.staticUnregisterEvent(11, this);
        HwSecurityMsgCenter.staticUnregisterEvent(12, this);
    }

    public boolean onEvent(FaceRecognizeEvent ev) {
        switch (ev.getType()) {
            case 10:
                this.mRetErrorCode = ev.getArgs()[0];
                if (this.mRetErrorCode == 0) {
                    this.mRetUserId = ev.getArgs()[1];
                }
                if (!this.mTaskRequest.isCameraCanceled()) {
                    CallbackHolder.getInstance().onCallbackEvent(this.mTaskRequest.getReqId(), 2, 1, this.mRetErrorCode);
                    break;
                }
                CallbackHolder.getInstance().onCallbackEvent(this.mTaskRequest.getReqId(), 2, 1, 1);
                break;
            case 11:
                CallbackHolder.getInstance().onCallbackEvent(this.mTaskRequest.getReqId(), 2, 3, ev.getArgs()[0]);
                break;
            case 12:
                -wrap0(0);
                break;
            default:
                return false;
        }
        return true;
    }

    public int getErrorCode() {
        return this.mRetErrorCode;
    }

    public int getUserId() {
        return this.mRetUserId;
    }
}
