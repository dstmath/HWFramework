package huawei.android.security.facerecognition.request;

import android.view.Surface;
import huawei.android.security.facerecognition.FaceRecognizeManagerImpl;
import huawei.android.security.facerecognition.base.HwSecurityTaskThread;
import huawei.android.security.facerecognition.task.EnrollTask;
import huawei.android.security.facerecognition.utils.LogUtil;
import java.util.List;

public class EnrollRequest extends FaceRecognizeRequest {
    private byte[] mAuthToken;
    private int mFlags;
    private List<Surface> mSurfaces;

    public EnrollRequest(int reqId, FaceRecognizeManagerImpl mgr, byte[] authToken, int flags, List<Surface> surfaces) {
        super((long) reqId, mgr);
        if (authToken != null) {
            this.mAuthToken = (byte[]) authToken.clone();
        }
        this.mFlags = flags;
        this.mSurfaces = surfaces;
    }

    public boolean onReqStart() {
        LogUtil.i("", "start enroll request");
        HwSecurityTaskThread.staticPushTask(new EnrollTask(null, this.mRetCallback, this), 0);
        return true;
    }

    public int getType() {
        return 0;
    }

    public byte[] getAuthToken() {
        return (byte[]) this.mAuthToken.clone();
    }

    public int getFlags() {
        return this.mFlags;
    }

    public List<Surface> getSurfaces() {
        return this.mSurfaces;
    }

    public void sendCancelOK() {
        FaceRecognizeManagerImpl.CallbackHolder.getInstance().onCallbackEvent((int) getReqId(), 1, 2, 0);
    }
}
