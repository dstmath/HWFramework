package com.huawei.facerecognition.task;

import android.view.Surface;
import com.huawei.facerecognition.base.HwSecurityTaskBase;
import com.huawei.facerecognition.base.HwSecurityTaskBase.RetCallback;
import com.huawei.facerecognition.base.HwSecurityTaskThread;
import com.huawei.facerecognition.request.FaceRecognizeRequest;
import com.huawei.facerecognition.utils.LogUtil;
import java.util.List;

public class PrepareCameraTask extends FaceRecognizeTask {
    private RetCallback mCreateSessionCallback = new RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            if (ret == 0) {
                HwSecurityTaskThread.staticPushTask(new RepeatingRequestTask(PrepareCameraTask.this, PrepareCameraTask.this.mRepeatingRequestCallback, PrepareCameraTask.this.mTaskRequest), 1);
            } else {
                PrepareCameraTask.this.-wrap0(ret);
            }
        }
    };
    private RetCallback mOpenCameraCallback = new RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            if (ret == 0) {
                HwSecurityTaskThread.staticPushTask(new CreateSessionTask(PrepareCameraTask.this, PrepareCameraTask.this.mCreateSessionCallback, PrepareCameraTask.this.mTaskRequest, PrepareCameraTask.this.mSurfaces), 1);
            } else {
                PrepareCameraTask.this.-wrap0(ret);
            }
        }
    };
    private RetCallback mRepeatingRequestCallback = new RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            PrepareCameraTask.this.-wrap0(ret);
        }
    };
    private List<Surface> mSurfaces;

    public PrepareCameraTask(FaceRecognizeTask parent, RetCallback callback, FaceRecognizeRequest taskRequest, List<Surface> surfaces) {
        super(parent, callback, taskRequest);
        this.mSurfaces = surfaces;
    }

    public int doAction() {
        LogUtil.i("", "start prepare camera task");
        HwSecurityTaskThread.staticPushTask(new OpenCameraTask(this, this.mOpenCameraCallback, this.mTaskRequest), 1);
        return -1;
    }
}
