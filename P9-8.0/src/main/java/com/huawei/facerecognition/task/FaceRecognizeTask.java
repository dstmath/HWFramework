package com.huawei.facerecognition.task;

import com.huawei.facerecognition.base.HwSecurityTaskBase;
import com.huawei.facerecognition.base.HwSecurityTaskBase.RetCallback;
import com.huawei.facerecognition.request.FaceRecognizeRequest;

public abstract class FaceRecognizeTask extends HwSecurityTaskBase {
    protected FaceRecognizeRequest mTaskRequest;

    public FaceRecognizeTask(HwSecurityTaskBase parent, RetCallback callback, FaceRecognizeRequest request) {
        super(parent, callback);
        this.mTaskRequest = request;
    }
}
