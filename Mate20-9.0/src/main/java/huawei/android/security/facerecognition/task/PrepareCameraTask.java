package huawei.android.security.facerecognition.task;

import android.view.Surface;
import huawei.android.security.facerecognition.base.HwSecurityTaskBase;
import huawei.android.security.facerecognition.base.HwSecurityTaskThread;
import huawei.android.security.facerecognition.request.FaceRecognizeRequest;
import huawei.android.security.facerecognition.utils.LogUtil;
import java.util.List;

public class PrepareCameraTask extends FaceRecognizeTask {
    /* access modifiers changed from: private */
    public HwSecurityTaskBase.RetCallback mCreateSessionCallback = new HwSecurityTaskBase.RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            if (ret == 0) {
                HwSecurityTaskThread.staticPushTask(new RepeatingRequestTask(PrepareCameraTask.this, PrepareCameraTask.this.mRepeatingRequestCallback, PrepareCameraTask.this.mTaskRequest), 1);
            } else {
                PrepareCameraTask.this.endWithResult(ret);
            }
        }
    };
    private HwSecurityTaskBase.RetCallback mOpenCameraCallback = new HwSecurityTaskBase.RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            if (ret == 0) {
                HwSecurityTaskThread.staticPushTask(new CreateSessionTask(PrepareCameraTask.this, PrepareCameraTask.this.mCreateSessionCallback, PrepareCameraTask.this.mTaskRequest, PrepareCameraTask.this.mSurfaces), 1);
            } else if (ret == 2) {
                PrepareCameraTask.this.endWithResult(2);
            } else if (!(child instanceof OpenCameraTask) || !((OpenCameraTask) child).canRetry()) {
                PrepareCameraTask.this.endWithResult(ret);
            } else {
                HwSecurityTaskThread.staticPushTask(new OpenCameraRetryTask(PrepareCameraTask.this, PrepareCameraTask.this.mOpenRetryCallback, PrepareCameraTask.this.mTaskRequest), 1);
            }
        }
    };
    /* access modifiers changed from: private */
    public HwSecurityTaskBase.RetCallback mOpenRetryCallback = new HwSecurityTaskBase.RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            if (ret == 0) {
                HwSecurityTaskThread.staticPushTask(new CreateSessionTask(PrepareCameraTask.this, PrepareCameraTask.this.mCreateSessionCallback, PrepareCameraTask.this.mTaskRequest, PrepareCameraTask.this.mSurfaces), 1);
            } else {
                PrepareCameraTask.this.endWithResult(ret);
            }
        }
    };
    /* access modifiers changed from: private */
    public HwSecurityTaskBase.RetCallback mRepeatingRequestCallback = new HwSecurityTaskBase.RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            PrepareCameraTask.this.endWithResult(ret);
        }
    };
    /* access modifiers changed from: private */
    public List<Surface> mSurfaces;

    public PrepareCameraTask(FaceRecognizeTask parent, HwSecurityTaskBase.RetCallback callback, FaceRecognizeRequest taskRequest, List<Surface> surfaces) {
        super(parent, callback, taskRequest);
        this.mSurfaces = surfaces;
    }

    public int doAction() {
        LogUtil.i("", "start prepare camera task");
        HwSecurityTaskThread.staticPushTask(new OpenCameraTask(this, this.mOpenCameraCallback, this.mTaskRequest), 1);
        return -1;
    }
}
