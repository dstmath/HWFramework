package huawei.android.security.facerecognition.request;

import android.content.Context;
import android.view.Surface;
import huawei.android.security.facerecognition.FaceRecognizeManagerImpl;
import huawei.android.security.facerecognition.base.HwSecurityTaskThread;
import huawei.android.security.facerecognition.task.AuthenticateTask;
import huawei.android.security.facerecognition.utils.LogUtil;
import java.util.List;

public class AuthenticateRequest extends FaceRecognizeRequest {
    private Context mContext;
    private int mFlags;
    private List<Surface> mSurfaces;

    public AuthenticateRequest(long opId, FaceRecognizeManagerImpl mgr, int flags, List<Surface> surfaces, Context context) {
        super(opId, mgr);
        this.mSurfaces = surfaces;
        this.mFlags = flags;
        this.mContext = context;
    }

    public int getType() {
        return 1;
    }

    public boolean onReqStart() {
        LogUtil.i("", "start auth request");
        AuthenticateTask authenticateTask = new AuthenticateTask(null, this.mRetCallback, this, this.mContext, getReqId());
        HwSecurityTaskThread.staticPushTask(authenticateTask, 0);
        return true;
    }

    public List<Surface> getSurfaces() {
        return this.mSurfaces;
    }

    public int getFlags() {
        return this.mFlags;
    }

    public void sendCancelOK() {
        FaceRecognizeManagerImpl.CallbackHolder.getInstance().onCallbackEvent((int) getReqId(), 2, 2, 0);
    }
}
