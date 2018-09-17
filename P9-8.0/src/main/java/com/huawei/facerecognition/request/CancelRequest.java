package com.huawei.facerecognition.request;

import com.huawei.facerecognition.FaceRecognizeEvent;
import com.huawei.facerecognition.FaceRecognizeManager;
import com.huawei.facerecognition.base.HwSecurityEventTask;
import com.huawei.facerecognition.base.HwSecurityTaskBase;
import com.huawei.facerecognition.base.HwSecurityTaskThread;
import com.huawei.facerecognition.utils.LogUtil;

public class CancelRequest extends HwSecurityTaskBase {
    public static final int CANCEL_AUTH = 2;
    public static final int CANCEL_ENROLL = 1;
    private int mCancelType;
    private FaceRecognizeManager mMgr;
    private int mReqId;

    public CancelRequest(int reqId, FaceRecognizeManager mgr, int cancelType) {
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
