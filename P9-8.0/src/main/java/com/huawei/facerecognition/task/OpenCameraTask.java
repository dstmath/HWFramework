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

public class OpenCameraTask extends FaceRecognizeTask implements EventListener {
    private static final String TAG = "OpenCamera";
    private static final long TIMEOUT = 3000;
    private int mReqType;
    TimerOutProc mTimeoutProc = new TimerOutProc() {
        public void onTimerOut() {
            OpenCameraTask.this.-wrap0(5);
        }
    };
    private HwSecurityTimerTask mTimer;

    public OpenCameraTask(HwSecurityTaskBase parent, RetCallback callback, FaceRecognizeRequest request) {
        super(parent, callback, request);
        this.mReqType = request.getType();
        this.mTimer = new HwSecurityTimerTask();
        if (1 == this.mReqType) {
            HwSecurityMsgCenter.staticRegisterEvent(1, this, this);
        } else if (this.mReqType == 0) {
            HwSecurityMsgCenter.staticRegisterEvent(2, this, this);
        }
        HwSecurityMsgCenter.staticRegisterEvent(3, this, this);
    }

    public boolean onEvent(FaceRecognizeEvent ev) {
        switch (ev.getType()) {
            case 1:
            case 2:
                LogUtil.d(TAG, "cancel event");
                if (ev.getArgs()[0] == this.mTaskRequest.getReqId()) {
                    this.mTaskRequest.setCancel();
                    return true;
                }
                break;
            case 3:
                LogUtil.d(TAG, "receive open camera event");
                if (!this.mTaskRequest.isCanceled()) {
                    if (ev.getArgs()[0] != 1000) {
                        -wrap0(5);
                        break;
                    }
                    -wrap0(0);
                    break;
                }
                -wrap0(2);
                break;
            default:
                return false;
        }
        return true;
    }

    public int doAction() {
        LogUtil.i("", "start open camera task");
        int openRet = FaceCamera.getInstance().openCamera();
        if (openRet == 1) {
            this.mTimer.setTimeout(TIMEOUT, this.mTimeoutProc);
            return -1;
        } else if (openRet == 0) {
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
        HwSecurityMsgCenter.staticUnregisterEvent(3, this);
    }
}
