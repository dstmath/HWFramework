package huawei.android.security.facerecognition.task;

import huawei.android.security.facerecognition.FaceRecognizeEvent;
import huawei.android.security.facerecognition.FaceRecognizeManagerImpl;
import huawei.android.security.facerecognition.base.HwSecurityMsgCenter;
import huawei.android.security.facerecognition.base.HwSecurityTaskBase;
import huawei.android.security.facerecognition.base.HwSecurityTimerTask;
import huawei.android.security.facerecognition.request.RemoveRequest;
import huawei.android.security.facerecognition.utils.LogUtil;

public class RemoveTask extends FaceRecognizeTask implements HwSecurityTaskBase.EventListener {
    private static final long TIMEOUT = 3000;
    private int mFaceId;
    HwSecurityTaskBase.TimerOutProc mTimeoutProc = new HwSecurityTaskBase.TimerOutProc() {
        public void onTimerOut() {
            RemoveTask.this.sendRemoveResult(1);
            RemoveTask.this.endWithResult(5);
        }
    };
    private HwSecurityTimerTask mTimer = new HwSecurityTimerTask();

    public RemoveTask(HwSecurityTaskBase parent, HwSecurityTaskBase.RetCallback callback, RemoveRequest request) {
        super(parent, callback, request);
        this.mFaceId = request.getFaceId();
        HwSecurityMsgCenter.staticRegisterEvent(13, this, this);
    }

    public void onStop() {
        this.mTimer.cancel();
        HwSecurityMsgCenter.staticUnregisterEvent(13, this);
    }

    public int doAction() {
        if (FaceRecognizeManagerImpl.ServiceHolder.getInstance().remove(this.mFaceId) == 0) {
            this.mTimer.setTimeout(TIMEOUT, this.mTimeoutProc);
            return -1;
        }
        sendRemoveResult(1);
        return 5;
    }

    /* access modifiers changed from: private */
    public void sendRemoveResult(int errorCode) {
        FaceRecognizeManagerImpl.CallbackHolder.getInstance().onCallbackEvent((int) this.mTaskRequest.getReqId(), 3, 1, errorCode);
    }

    public boolean onEvent(FaceRecognizeEvent ev) {
        int removeResult = (int) ev.getArgs()[0];
        LogUtil.d("***********", "remove result : " + removeResult);
        if (13 != ev.getType()) {
            return false;
        }
        sendRemoveResult(removeResult);
        endWithResult(0);
        return true;
    }
}
