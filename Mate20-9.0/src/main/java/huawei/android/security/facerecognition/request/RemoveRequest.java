package huawei.android.security.facerecognition.request;

import huawei.android.security.facerecognition.FaceRecognizeManagerImpl;
import huawei.android.security.facerecognition.base.HwSecurityTaskThread;
import huawei.android.security.facerecognition.task.RemoveTask;
import huawei.android.security.facerecognition.utils.LogUtil;

public class RemoveRequest extends FaceRecognizeRequest {
    private int mFaceId;

    public RemoveRequest(int reqId, FaceRecognizeManagerImpl mgr, int faceId) {
        super((long) reqId, mgr);
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
