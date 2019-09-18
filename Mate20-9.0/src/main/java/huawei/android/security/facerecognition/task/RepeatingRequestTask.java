package huawei.android.security.facerecognition.task;

import huawei.android.security.facerecognition.FaceCamera;
import huawei.android.security.facerecognition.FaceRecognizeEvent;
import huawei.android.security.facerecognition.base.HwSecurityMsgCenter;
import huawei.android.security.facerecognition.base.HwSecurityTaskBase;
import huawei.android.security.facerecognition.base.HwSecurityTimerTask;
import huawei.android.security.facerecognition.request.AuthenticateRequest;
import huawei.android.security.facerecognition.request.FaceRecognizeRequest;
import huawei.android.security.facerecognition.utils.LogUtil;

public class RepeatingRequestTask extends FaceRecognizeTask implements HwSecurityTaskBase.EventListener {
    private static final long TIMEOUT = 3000;
    private int mFlag = 0;
    private int mReqType;
    HwSecurityTaskBase.TimerOutProc mTimeoutProc = new HwSecurityTaskBase.TimerOutProc() {
        public void onTimerOut() {
            RepeatingRequestTask.this.endWithResult(5);
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
                    if (((int) ev.getArgs()[0]) != 1005) {
                        endWithResult(5);
                        break;
                    } else {
                        endWithResult(0);
                        break;
                    }
                } else {
                    endWithResult(2);
                    break;
                }
            case 6:
                endWithResult(5);
                break;
            default:
                return false;
        }
        return true;
    }

    public RepeatingRequestTask(HwSecurityTaskBase parent, HwSecurityTaskBase.RetCallback callback, FaceRecognizeRequest request) {
        super(parent, callback, request);
        this.mReqType = request.getType();
        this.mTimer = new HwSecurityTimerTask();
        if (request instanceof AuthenticateRequest) {
            this.mFlag = ((AuthenticateRequest) request).getFlags();
            LogUtil.i("", "in RepeatingRequestTask get flag:" + this.mFlag);
        }
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
        int previewRet = FaceCamera.getInstance().createPreviewRequest(this.mReqType, this.mFlag);
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
