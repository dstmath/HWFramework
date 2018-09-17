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
import com.huawei.facerecognition.request.RemoveRequest;
import com.huawei.facerecognition.utils.LogUtil;

public class RemoveTask extends FaceRecognizeTask implements EventListener {
    private static final long TIMEOUT = 3000;
    private int mFaceId;
    TimerOutProc mTimeoutProc = new TimerOutProc() {
        public void onTimerOut() {
            RemoveTask.this.sendRemoveResult(1);
            RemoveTask.this.-wrap0(5);
        }
    };
    private HwSecurityTimerTask mTimer = new HwSecurityTimerTask();

    public RemoveTask(HwSecurityTaskBase parent, RetCallback callback, RemoveRequest request) {
        super(parent, callback, request);
        this.mFaceId = request.getFaceId();
        HwSecurityMsgCenter.staticRegisterEvent(13, this, this);
    }

    public void onStop() {
        this.mTimer.cancel();
        HwSecurityMsgCenter.staticUnregisterEvent(13, this);
    }

    public int doAction() {
        if (ServiceHolder.getInstance().remove(this.mFaceId) == 0) {
            this.mTimer.setTimeout(TIMEOUT, this.mTimeoutProc);
            return -1;
        }
        sendRemoveResult(1);
        return 5;
    }

    private void sendRemoveResult(int errorCode) {
        CallbackHolder.getInstance().onCallbackEvent(this.mTaskRequest.getReqId(), 3, 1, errorCode);
    }

    public boolean onEvent(FaceRecognizeEvent ev) {
        int removeResult = ev.getArgs()[0];
        LogUtil.d("***********", "remove result : " + removeResult);
        if (13 != ev.getType()) {
            return false;
        }
        sendRemoveResult(removeResult);
        -wrap0(0);
        return true;
    }
}
