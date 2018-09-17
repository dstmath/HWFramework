package com.huawei.facerecognition.task;

import com.huawei.facerecognition.FaceCamera;
import com.huawei.facerecognition.FaceRecognizeEvent;
import com.huawei.facerecognition.base.HwSecurityMsgCenter;
import com.huawei.facerecognition.base.HwSecurityTaskBase;
import com.huawei.facerecognition.base.HwSecurityTaskBase.EventListener;
import com.huawei.facerecognition.base.HwSecurityTaskBase.RetCallback;
import com.huawei.facerecognition.base.HwSecurityTaskBase.TimerOutProc;
import com.huawei.facerecognition.base.HwSecurityTimerTask;
import com.huawei.facerecognition.request.FaceRecognizeRequest;
import com.huawei.facerecognition.utils.LogUtil;

public class RepeatingRequestTask extends FaceRecognizeTask implements EventListener {
    private static final long TIMEOUT = 3000;
    private int mReqType;
    TimerOutProc mTimeoutProc = new TimerOutProc() {
        public void onTimerOut() {
            RepeatingRequestTask.this.-wrap1(5);
        }
    };
    private HwSecurityTimerTask mTimer;

    public boolean onEvent(FaceRecognizeEvent ev) {
        switch (ev.getType()) {
            case 1:
            case 2:
                if (ev.getArgs()[0] == this.mTaskRequest.getReqId()) {
                    this.mTaskRequest.setCancel();
                    return true;
                }
                break;
            case 5:
                if (!this.mTaskRequest.isCanceled()) {
                    if (ev.getArgs()[0] != FaceCamera.RET_REPEAT_REQUEST_OK) {
                        -wrap1(5);
                        break;
                    }
                    -wrap1(0);
                    break;
                }
                -wrap1(2);
                break;
            case 6:
                -wrap1(5);
                break;
            default:
                return false;
        }
        return true;
    }

    public RepeatingRequestTask(HwSecurityTaskBase parent, RetCallback callback, FaceRecognizeRequest request) {
        super(parent, callback, request);
        this.mReqType = request.getType();
        this.mTimer = new HwSecurityTimerTask();
        if (1 == this.mReqType) {
            HwSecurityMsgCenter.staticRegisterEvent(1, this, this);
        } else if (this.mReqType == 0) {
            HwSecurityMsgCenter.staticRegisterEvent(2, this, this);
        }
        HwSecurityMsgCenter.staticRegisterEvent(6, this, this);
        HwSecurityMsgCenter.staticRegisterEvent(5, this, this);
    }

    public int doAction() {
        LogUtil.i("", "start repeat request task");
        int previewRet = FaceCamera.getInstance().createPreviewRequest(this.mReqType);
        if (previewRet == 1) {
            this.mTimer.setTimeout(TIMEOUT, this.mTimeoutProc);
            return -1;
        } else if (previewRet == 0) {
            return 0;
        } else {
            return 5;
        }
    }

    public void onStop() {
        this.mTimer.cancel();
        if (1 == this.mReqType) {
            HwSecurityMsgCenter.staticUnregisterEvent(1, this);
        } else if (this.mReqType == 0) {
            HwSecurityMsgCenter.staticUnregisterEvent(2, this);
        }
        HwSecurityMsgCenter.staticUnregisterEvent(6, this);
        HwSecurityMsgCenter.staticUnregisterEvent(5, this);
    }
}
