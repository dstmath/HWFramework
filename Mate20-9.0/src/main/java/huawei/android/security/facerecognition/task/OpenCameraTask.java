package huawei.android.security.facerecognition.task;

import huawei.android.security.facerecognition.FaceCamera;
import huawei.android.security.facerecognition.FaceRecognizeEvent;
import huawei.android.security.facerecognition.base.HwSecurityMsgCenter;
import huawei.android.security.facerecognition.base.HwSecurityTaskBase;
import huawei.android.security.facerecognition.base.HwSecurityTimerTask;
import huawei.android.security.facerecognition.request.FaceRecognizeRequest;
import huawei.android.security.facerecognition.utils.LogUtil;

public class OpenCameraTask extends FaceRecognizeTask implements HwSecurityTaskBase.EventListener {
    private static final String TAG = "OpenCamera";
    private static final long TIMEOUT = 3000;
    /* access modifiers changed from: private */
    public boolean mCanRetry = true;
    private int mReqType;
    HwSecurityTaskBase.TimerOutProc mTimeoutProc = new HwSecurityTaskBase.TimerOutProc() {
        public void onTimerOut() {
            boolean unused = OpenCameraTask.this.mCanRetry = false;
            OpenCameraTask.this.endWithResult(5);
        }
    };
    private HwSecurityTimerTask mTimer;

    public OpenCameraTask(HwSecurityTaskBase parent, HwSecurityTaskBase.RetCallback callback, FaceRecognizeRequest request) {
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
                    if (((int) ev.getArgs()[0]) != 1000) {
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
            default:
                return false;
        }
        return true;
    }

    public int doAction() {
        LogUtil.i("", "start open camera task");
        int openRet = FaceCamera.getInstance().openCamera(this.mReqType);
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

    public boolean canRetry() {
        return this.mCanRetry;
    }
}
