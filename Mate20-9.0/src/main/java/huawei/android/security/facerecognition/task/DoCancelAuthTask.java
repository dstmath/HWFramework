package huawei.android.security.facerecognition.task;

import huawei.android.security.facerecognition.FaceCamera;
import huawei.android.security.facerecognition.FaceRecognizeEvent;
import huawei.android.security.facerecognition.FaceRecognizeManagerImpl;
import huawei.android.security.facerecognition.base.HwSecurityMsgCenter;
import huawei.android.security.facerecognition.base.HwSecurityTaskBase;
import huawei.android.security.facerecognition.base.HwSecurityTimerTask;
import huawei.android.security.facerecognition.request.FaceRecognizeRequest;
import huawei.android.security.facerecognition.utils.LogUtil;

public class DoCancelAuthTask extends FaceRecognizeTask implements HwSecurityTaskBase.EventListener {
    private static final long TIMEOUT = 3000;
    private int mRetErrorCode;
    private int mRetUserId;
    HwSecurityTaskBase.TimerOutProc mTimeoutProc = new HwSecurityTaskBase.TimerOutProc() {
        public void onTimerOut() {
            DoCancelAuthTask.this.endWithResult(0);
        }
    };
    private HwSecurityTimerTask mTimer = new HwSecurityTimerTask();

    public DoCancelAuthTask(HwSecurityTaskBase parent, HwSecurityTaskBase.RetCallback callback, FaceRecognizeRequest request) {
        super(parent, callback, request);
        HwSecurityMsgCenter.staticRegisterEvent(10, this, this);
        HwSecurityMsgCenter.staticRegisterEvent(11, this, this);
        HwSecurityMsgCenter.staticRegisterEvent(12, this, this);
    }

    public int doAction() {
        LogUtil.i("", "do cancel auth task");
        if (FaceRecognizeManagerImpl.ServiceHolder.getInstance().cancelAuthentication() != 0) {
            return 0;
        }
        this.mTimer.setTimeout(TIMEOUT, this.mTimeoutProc);
        FaceCamera.getInstance().close();
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
                this.mRetErrorCode = (int) ev.getArgs()[0];
                if (this.mRetErrorCode == 0) {
                    this.mRetUserId = (int) ev.getArgs()[1];
                }
                if (!this.mTaskRequest.isCameraCanceled()) {
                    FaceRecognizeManagerImpl.CallbackHolder.getInstance().onCallbackEvent((int) this.mTaskRequest.getReqId(), 2, 1, this.mRetErrorCode);
                    break;
                } else {
                    FaceRecognizeManagerImpl.CallbackHolder.getInstance().onCallbackEvent((int) this.mTaskRequest.getReqId(), 2, 1, 1);
                    break;
                }
            case 11:
                FaceRecognizeManagerImpl.CallbackHolder.getInstance().onCallbackEvent((int) this.mTaskRequest.getReqId(), 2, 3, (int) ev.getArgs()[0]);
                break;
            case 12:
                endWithResult(0);
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
