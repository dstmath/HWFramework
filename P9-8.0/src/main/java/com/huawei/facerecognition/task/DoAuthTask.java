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

public class DoAuthTask extends FaceRecognizeTask implements EventListener {
    private static final long TIMEOUT = 10000;
    private int mFlags;
    private int mRetErrorCode;
    private int mRetUserId;
    TimerOutProc mTimeoutProc = new TimerOutProc() {
        public void onTimerOut() {
            DoAuthTask.this.-wrap0(5);
        }
    };
    private HwSecurityTimerTask mTimer;

    public boolean onEvent(FaceRecognizeEvent ev) {
        switch (ev.getType()) {
            case 1:
                LogUtil.d("do auth", "cancel auth");
                if (ev.getArgs()[0] == this.mTaskRequest.getReqId()) {
                    this.mTaskRequest.setCancel();
                    -wrap0(2);
                    break;
                }
                return false;
            case 6:
                LogUtil.d("do auth", "interrupt auth");
                this.mTaskRequest.setCameraCancel();
                -wrap0(2);
                break;
            case 10:
                this.mRetErrorCode = ev.getArgs()[0];
                LogUtil.d("do auth", "auth result : " + this.mRetErrorCode);
                if (this.mRetErrorCode != 0) {
                    -wrap0(6);
                    break;
                }
                this.mRetUserId = ev.getArgs()[1];
                -wrap0(0);
                break;
            case 11:
                LogUtil.d("do auth", "auth acquired");
                CallbackHolder.getInstance().onCallbackEvent(this.mTaskRequest.getReqId(), 2, 3, ev.getArgs()[0]);
                break;
            default:
                return false;
        }
        return true;
    }

    public DoAuthTask(HwSecurityTaskBase parent, RetCallback callback, FaceRecognizeRequest request, int flags) {
        super(parent, callback, request);
        this.mFlags = flags;
        this.mTimer = new HwSecurityTimerTask();
        HwSecurityMsgCenter.staticRegisterEvent(6, this, this);
        HwSecurityMsgCenter.staticRegisterEvent(1, this, this);
        HwSecurityMsgCenter.staticRegisterEvent(10, this, this);
        HwSecurityMsgCenter.staticRegisterEvent(11, this, this);
    }

    public int doAction() {
        LogUtil.i("", "do auth task");
        if (ServiceHolder.getInstance().authenticate(this.mFlags) != 0) {
            return 5;
        }
        this.mTimer.setTimeout(TIMEOUT, this.mTimeoutProc);
        return -1;
    }

    public void onStop() {
        this.mTimer.cancel();
        HwSecurityMsgCenter.staticUnregisterEvent(6, this);
        HwSecurityMsgCenter.staticUnregisterEvent(1, this);
        HwSecurityMsgCenter.staticUnregisterEvent(10, this);
        HwSecurityMsgCenter.staticUnregisterEvent(11, this);
    }

    public int getErrorCode() {
        return this.mRetErrorCode;
    }

    public int getUserId() {
        return this.mRetUserId;
    }
}
