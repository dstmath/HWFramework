package huawei.android.security.facerecognition.task;

import android.os.SystemProperties;
import huawei.android.security.facerecognition.FaceRecognizeEvent;
import huawei.android.security.facerecognition.FaceRecognizeManagerImpl;
import huawei.android.security.facerecognition.base.HwSecurityMsgCenter;
import huawei.android.security.facerecognition.base.HwSecurityTaskBase;
import huawei.android.security.facerecognition.base.HwSecurityTimerTask;
import huawei.android.security.facerecognition.request.FaceRecognizeRequest;
import huawei.android.security.facerecognition.utils.LogUtil;

public class DoEnrollTask extends FaceRecognizeTask implements HwSecurityTaskBase.EventListener {
    private static final int SECURE_CAMERA = 1;
    private static final int SUPPORT_FACE_MODE = SystemProperties.getInt("ro.config.support_face_mode", 1);
    private static final long TIMEOUT = (SUPPORT_FACE_MODE > 1 ? 190000 : 20000);
    private byte[] mAuthToken;
    private int mFlags;
    private int mRetErrorCode;
    private int mRetFaceId;
    private int mRetUserId;
    HwSecurityTaskBase.TimerOutProc mTimeoutProc = new HwSecurityTaskBase.TimerOutProc() {
        public void onTimerOut() {
            DoEnrollTask.this.endWithResult(5);
        }
    };
    private HwSecurityTimerTask mTimer;

    public DoEnrollTask(FaceRecognizeTask parent, HwSecurityTaskBase.RetCallback callback, FaceRecognizeRequest request, byte[] authToken, int flags) {
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
        if (FaceRecognizeManagerImpl.ServiceHolder.getInstance().enroll(this.mAuthToken, this.mFlags) != 0) {
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
        int type = ev.getType();
        if (type != 2) {
            switch (type) {
                case 6:
                    this.mTaskRequest.setCancel();
                    endWithResult(2);
                    break;
                case 7:
                    this.mRetErrorCode = (int) ev.getArgs()[0];
                    LogUtil.d("enroll result", "error code : " + this.mRetErrorCode);
                    if (this.mRetErrorCode != 0) {
                        endWithResult(6);
                        break;
                    } else {
                        this.mRetFaceId = (int) ev.getArgs()[1];
                        this.mRetUserId = (int) ev.getArgs()[2];
                        endWithResult(0);
                        break;
                    }
                case 8:
                    FaceRecognizeManagerImpl.CallbackHolder.getInstance().onCallbackEvent((int) this.mTaskRequest.getReqId(), 1, 3, (int) ev.getArgs()[0]);
                    break;
                default:
                    return false;
            }
        } else if (ev.getArgs()[0] != this.mTaskRequest.getReqId()) {
            return false;
        } else {
            this.mTaskRequest.setCancel();
            endWithResult(2);
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
