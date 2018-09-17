package com.huawei.facerecognition.task;

import com.huawei.facerecognition.FaceRecognizeEvent;
import com.huawei.facerecognition.FaceRecognizeManager.CallbackHolder;
import com.huawei.facerecognition.FaceRecognizeManager.ServiceHolder;
import com.huawei.facerecognition.base.HwSecurityMsgCenter;
import com.huawei.facerecognition.base.HwSecurityTaskBase.EventListener;
import com.huawei.facerecognition.base.HwSecurityTaskBase.RetCallback;
import com.huawei.facerecognition.base.HwSecurityTaskBase.TimerOutProc;
import com.huawei.facerecognition.base.HwSecurityTimerTask;
import com.huawei.facerecognition.request.FaceRecognizeRequest;
import com.huawei.facerecognition.utils.LogUtil;

public class DoEnrollTask extends FaceRecognizeTask implements EventListener {
    private static final long TIMEOUT = 20000;
    private byte[] mAuthToken;
    private int mFlags;
    private int mRetErrorCode;
    private int mRetFaceId;
    private int mRetUserId;
    TimerOutProc mTimeoutProc = new TimerOutProc() {
        public void onTimerOut() {
            DoEnrollTask.this.-wrap0(5);
        }
    };
    private HwSecurityTimerTask mTimer;

    public DoEnrollTask(FaceRecognizeTask parent, RetCallback callback, FaceRecognizeRequest request, byte[] authToken, int flags) {
        super(parent, callback, request);
        this.mAuthToken = (byte[]) authToken.clone();
        this.mFlags = flags;
        this.mTimer = new HwSecurityTimerTask();
        HwSecurityMsgCenter.staticRegisterEvent(6, this, this);
        HwSecurityMsgCenter.staticRegisterEvent(2, this, this);
        HwSecurityMsgCenter.staticRegisterEvent(7, this, this);
        HwSecurityMsgCenter.staticRegisterEvent(8, this, this);
    }

    public int doAction() {
        LogUtil.i("", "do enroll task");
        if (ServiceHolder.getInstance().enroll(this.mAuthToken, this.mFlags) != 0) {
            return 5;
        }
        this.mTimer.setTimeout(TIMEOUT, this.mTimeoutProc);
        return -1;
    }

    public void onStop() {
        this.mTimer.cancel();
        HwSecurityMsgCenter.staticUnregisterEvent(6, this);
        HwSecurityMsgCenter.staticUnregisterEvent(2, this);
        HwSecurityMsgCenter.staticUnregisterEvent(7, this);
        HwSecurityMsgCenter.staticUnregisterEvent(8, this);
    }

    public boolean onEvent(FaceRecognizeEvent ev) {
        switch (ev.getType()) {
            case 2:
                if (ev.getArgs()[0] == this.mTaskRequest.getReqId()) {
                    this.mTaskRequest.setCancel();
                    -wrap0(2);
                    break;
                }
                return false;
            case 6:
                this.mTaskRequest.setCancel();
                -wrap0(2);
                break;
            case 7:
                this.mRetErrorCode = ev.getArgs()[0];
                LogUtil.d("enroll result", "error code : " + this.mRetErrorCode);
                if (this.mRetErrorCode != 0) {
                    -wrap0(6);
                    break;
                }
                this.mRetFaceId = ev.getArgs()[1];
                this.mRetUserId = ev.getArgs()[2];
                -wrap0(0);
                break;
            case 8:
                CallbackHolder.getInstance().onCallbackEvent(this.mTaskRequest.getReqId(), 1, 3, ev.getArgs()[0]);
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

    public int getFaceId() {
        return this.mRetFaceId;
    }
}
