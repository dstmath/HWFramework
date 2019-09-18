package huawei.android.security.facerecognition.task;

import huawei.android.security.facerecognition.FaceRecognizeEvent;
import huawei.android.security.facerecognition.base.HwSecurityMsgCenter;
import huawei.android.security.facerecognition.base.HwSecurityTaskBase;
import huawei.android.security.facerecognition.base.HwSecurityTimerTask;
import huawei.android.security.facerecognition.request.FaceRecognizeRequest;
import huawei.android.security.facerecognition.utils.LogUtil;

public class RetryWaitTask extends FaceRecognizeTask implements HwSecurityTaskBase.EventListener {
    private static final String TAG = "RetryWaitTask";
    private static final long TIMEOUT = 300;
    private int mReqType;
    HwSecurityTaskBase.TimerOutProc mTimeoutProc = new HwSecurityTaskBase.TimerOutProc() {
        public void onTimerOut() {
            if (!RetryWaitTask.this.mTaskRequest.isCanceled()) {
                RetryWaitTask.this.endWithResult(0);
            } else {
                RetryWaitTask.this.endWithResult(2);
            }
        }
    };
    private HwSecurityTimerTask mTimer;

    public RetryWaitTask(HwSecurityTaskBase parent, HwSecurityTaskBase.RetCallback callback, FaceRecognizeRequest request) {
        super(parent, callback, request);
        this.mReqType = request.getType();
        this.mTimer = new HwSecurityTimerTask();
        if (1 == this.mReqType) {
            HwSecurityMsgCenter.staticRegisterEvent(1, this, this);
        } else if (this.mReqType == 0) {
            HwSecurityMsgCenter.staticRegisterEvent(2, this, this);
        }
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
        }
        return false;
    }

    public int doAction() {
        LogUtil.i("", "start RetryWaitTask");
        this.mTimer.setTimeout(TIMEOUT, this.mTimeoutProc);
        return -1;
    }

    public void onStop() {
        this.mTimer.cancel();
        if (1 == this.mReqType) {
            HwSecurityMsgCenter.staticUnregisterEvent(1, this);
        } else if (this.mReqType == 0) {
            HwSecurityMsgCenter.staticUnregisterEvent(2, this);
        }
    }
}
