package com.huawei.facerecognition.request;

import com.huawei.facerecognition.FaceRecognizeManager;
import com.huawei.facerecognition.base.HwSecurityTaskThread;
import com.huawei.facerecognition.task.RemoveTask;
import com.huawei.facerecognition.utils.LogUtil;

public class RemoveRequest extends FaceRecognizeRequest {
    private int mFaceId;

    public RemoveRequest(int reqId, FaceRecognizeManager mgr, int faceId) {
        super(reqId, mgr);
        this.mFaceId = faceId;
    }

    public boolean onReqStart() {
        LogUtil.i("", "start remove request");
        HwSecurityTaskThread.staticPushTask(new RemoveTask(this, this.mRetCallback, this), 0);
        return true;
    }

    public int getType() {
        return 2;
    }

    public void sendCancelOK() {
    }

    public int getFaceId() {
        return this.mFaceId;
    }
}
