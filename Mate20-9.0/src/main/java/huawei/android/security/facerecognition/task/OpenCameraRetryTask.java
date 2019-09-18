package huawei.android.security.facerecognition.task;

import huawei.android.security.facerecognition.base.HwSecurityTaskBase;
import huawei.android.security.facerecognition.base.HwSecurityTaskThread;
import huawei.android.security.facerecognition.request.FaceRecognizeRequest;
import huawei.android.security.facerecognition.utils.LogUtil;

public class OpenCameraRetryTask extends FaceRecognizeTask {
    private static final int RETRY_TIMES = 10;
    private static final String TAG = "OpenCameraRetry";
    /* access modifiers changed from: private */
    public HwSecurityTaskBase.RetCallback mOpenCameraCallback = new HwSecurityTaskBase.RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            if (ret == 0 || ret == 2) {
                OpenCameraRetryTask.this.endWithResult(ret);
            } else if (!(child instanceof OpenCameraTask) || !((OpenCameraTask) child).canRetry() || OpenCameraRetryTask.access$306(OpenCameraRetryTask.this) <= 0) {
                OpenCameraRetryTask.this.endWithResult(7);
            } else {
                HwSecurityTaskThread.staticPushTask(new RetryWaitTask(OpenCameraRetryTask.this, OpenCameraRetryTask.this.mRetryWaitCallback, OpenCameraRetryTask.this.mTaskRequest), 1);
            }
        }
    };
    /* access modifiers changed from: private */
    public HwSecurityTaskBase.RetCallback mRetryWaitCallback = new HwSecurityTaskBase.RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            if (ret == 0) {
                HwSecurityTaskThread.staticPushTask(new OpenCameraTask(OpenCameraRetryTask.this, OpenCameraRetryTask.this.mOpenCameraCallback, OpenCameraRetryTask.this.mTaskRequest), 1);
            } else {
                OpenCameraRetryTask.this.endWithResult(2);
            }
        }
    };
    private int mTimeLeft = 10;

    static /* synthetic */ int access$306(OpenCameraRetryTask x0) {
        int i = x0.mTimeLeft - 1;
        x0.mTimeLeft = i;
        return i;
    }

    public OpenCameraRetryTask(HwSecurityTaskBase parent, HwSecurityTaskBase.RetCallback callback, FaceRecognizeRequest request) {
        super(parent, callback, request);
    }

    public int doAction() {
        LogUtil.i("", "start OpenCameraRetryTask");
        HwSecurityTaskThread.staticPushTask(new RetryWaitTask(this, this.mRetryWaitCallback, this.mTaskRequest), 1);
        return -1;
    }
}
