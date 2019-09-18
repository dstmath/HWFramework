package huawei.android.security.facerecognition.base;

import huawei.android.security.facerecognition.FaceRecognizeEvent;
import huawei.android.security.facerecognition.utils.LogUtil;

public abstract class HwSecurityTaskBase {
    public static final int E_CAMERA_FAILED = 7;
    public static final int E_CANCEL = 2;
    public static final int E_CONTINUE = -1;
    public static final int E_FAILED = 5;
    public static final int E_OK = 0;
    public static final int E_RES_FAILED = 6;
    public static final int E_TIMEOUT = 1;
    public static final int E_UNEXPECTATION = 4;
    public static final int STATUS_FINISHED = 2;
    public static final int STATUS_STARTED = 1;
    public static final int STATUS_UNSTART = 0;
    private static final String TAG = HwSecurityTaskBase.class.getSimpleName();
    private RetCallback mCallback;
    private HwSecurityTaskBase mParent;
    protected int mStatus = 0;

    public interface EventListener {
        boolean onEvent(FaceRecognizeEvent faceRecognizeEvent);
    }

    public interface RetCallback {
        void onTaskCallback(HwSecurityTaskBase hwSecurityTaskBase, int i);
    }

    public interface TimerOutProc {
        void onTimerOut();
    }

    public abstract int doAction();

    public HwSecurityTaskBase(HwSecurityTaskBase parent, RetCallback callback) {
        this.mParent = parent;
        this.mCallback = callback;
        onStart();
    }

    public HwSecurityTaskBase getParent() {
        return this.mParent;
    }

    public void execute() {
        String str = TAG;
        LogUtil.d(str, "execute task: " + getClass().getSimpleName());
        if (this.mStatus == 0) {
            this.mStatus = 1;
            int ret = doAction();
            if (ret != -1) {
                endWithResult(ret);
            }
        }
    }

    public void onStart() {
    }

    public void onStop() {
    }

    public int getTaskStatus() {
        return this.mStatus;
    }

    /* access modifiers changed from: protected */
    public void endWithResult(int ret) {
        String str = TAG;
        LogUtil.i(str, "endWithResult, task: " + this);
        if (this.mStatus != 2) {
            onStop();
            this.mStatus = 2;
            if (this.mCallback != null) {
                this.mCallback.onTaskCallback(this, ret);
            }
        }
    }

    public String toString() {
        String status;
        switch (this.mStatus) {
            case 0:
                status = "unstarted";
                break;
            case 1:
                status = "started";
                break;
            case 2:
                status = "finished";
                break;
            default:
                status = "unknown status";
                break;
        }
        return getClass().getSimpleName() + ", status : " + status;
    }
}
