package huawei.android.security.facerecognition.task;

import huawei.android.security.facerecognition.FaceRecognizeEvent;
import huawei.android.security.facerecognition.FaceRecognizeManagerImpl;
import huawei.android.security.facerecognition.base.HwSecurityMsgCenter;
import huawei.android.security.facerecognition.base.HwSecurityTaskBase;
import huawei.android.security.facerecognition.base.HwSecurityTimerTask;
import huawei.android.security.facerecognition.request.FaceRecognizeRequest;
import huawei.android.security.facerecognition.utils.LogUtil;

public class DoAuthTask extends FaceRecognizeTask implements HwSecurityTaskBase.EventListener {
    private static final long TIMEOUT = 10000;
    private int mFlags;
    private long mOpId;
    private int mRetErrorCode;
    private int mRetUserId;
    HwSecurityTaskBase.TimerOutProc mTimeoutProc = new HwSecurityTaskBase.TimerOutProc() {
        public void onTimerOut() {
            DoAuthTask.this.endWithResult(5);
        }
    };
    private HwSecurityTimerTask mTimer;

    public boolean onEvent(FaceRecognizeEvent ev) {
        int type = ev.getType();
        if (type == 1) {
            LogUtil.d("do auth", "cancel auth");
            if (ev.getArgs()[0] != this.mTaskRequest.getReqId()) {
                return false;
            }
            this.mTaskRequest.setCancel();
            endWithResult(2);
        } else if (type != 6) {
            switch (type) {
                case 10:
                    this.mRetErrorCode = (int) ev.getArgs()[0];
                    LogUtil.d("do auth", "auth result : " + this.mRetErrorCode);
                    if (this.mRetErrorCode != 0) {
                        endWithResult(6);
                        break;
                    } else {
                        this.mRetUserId = (int) ev.getArgs()[1];
                        endWithResult(0);
                        break;
                    }
                case 11:
                    LogUtil.d("do auth", "auth acquired");
                    FaceRecognizeManagerImpl.CallbackHolder.getInstance().onCallbackEvent((int) this.mTaskRequest.getReqId(), 2, 3, (int) ev.getArgs()[0]);
                    break;
                default:
                    return false;
            }
        } else {
            LogUtil.d("do auth", "interrupt auth");
            this.mTaskRequest.setCameraCancel();
            endWithResult(2);
        }
        return true;
    }

    public DoAuthTask(HwSecurityTaskBase parent, HwSecurityTaskBase.RetCallback callback, FaceRecognizeRequest request, int flags, long opId) {
        super(parent, callback, request);
        this.mFlags = flags;
        this.mTimer = new HwSecurityTimerTask();
        this.mOpId = opId;
        HwSecurityMsgCenter.staticRegisterEvent(6, this, this);
        HwSecurityMsgCenter.staticRegisterEvent(1, this, this);
        HwSecurityMsgCenter.staticRegisterEvent(10, this, this);
        HwSecurityMsgCenter.staticRegisterEvent(11, this, this);
    }

    public int doAction() {
        LogUtil.i("", "do auth task");
        if (FaceRecognizeManagerImpl.ServiceHolder.getInstance().authenticate(this.mOpId, this.mFlags) != 0) {
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
