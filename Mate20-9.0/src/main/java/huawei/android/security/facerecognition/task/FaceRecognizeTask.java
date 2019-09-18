package huawei.android.security.facerecognition.task;

import huawei.android.security.facerecognition.base.HwSecurityTaskBase;
import huawei.android.security.facerecognition.request.FaceRecognizeRequest;

public abstract class FaceRecognizeTask extends HwSecurityTaskBase {
    protected FaceRecognizeRequest mTaskRequest;

    public FaceRecognizeTask(HwSecurityTaskBase parent, HwSecurityTaskBase.RetCallback callback, FaceRecognizeRequest request) {
        super(parent, callback);
        this.mTaskRequest = request;
    }
}
