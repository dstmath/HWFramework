package huawei.android.security.facerecognition.request;

import huawei.android.security.facerecognition.FaceRecognizeEvent;
import huawei.android.security.facerecognition.FaceRecognizeManagerImpl;
import huawei.android.security.facerecognition.base.HwSecurityEventTask;
import huawei.android.security.facerecognition.base.HwSecurityTaskBase;
import huawei.android.security.facerecognition.base.HwSecurityTaskThread;
import huawei.android.security.facerecognition.utils.LogUtil;

public class CancelRequest extends HwSecurityTaskBase {
    public static final int CANCEL_AUTH = 2;
    public static final int CANCEL_ENROLL = 1;
    private int mCancelType;
    private FaceRecognizeManagerImpl mMgr;
    private long mReqId;

    public CancelRequest(long reqId, FaceRecognizeManagerImpl mgr, int cancelType) {
        super(null, null);
        this.mReqId = reqId;
        this.mMgr = mgr;
        this.mCancelType = cancelType;
    }

    public int doAction() {
        LogUtil.i("********", "start cancel request with ID : " + this.mReqId);
        if (!this.mMgr.onCancelReq(this.mReqId, this.mCancelType)) {
            if (2 == this.mCancelType) {
                HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new FaceRecognizeEvent(1, this.mReqId)), 2);
            } else if (1 == this.mCancelType) {
                HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new FaceRecognizeEvent(2, this.mReqId)), 2);
            }
        }
        return 0;
    }
}
