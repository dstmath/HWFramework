package com.huawei.facerecognition.request;

import android.view.Surface;
import com.huawei.facerecognition.FaceRecognizeManager;
import com.huawei.facerecognition.FaceRecognizeManager.CallbackHolder;
import com.huawei.facerecognition.base.HwSecurityTaskThread;
import com.huawei.facerecognition.task.EnrollTask;
import com.huawei.facerecognition.utils.LogUtil;
import java.util.List;

public class EnrollRequest extends FaceRecognizeRequest {
    private byte[] mAuthToken;
    private int mFlags;
    private List<Surface> mSurfaces;

    public EnrollRequest(int reqId, FaceRecognizeManager mgr, byte[] authToken, int flags, List<Surface> surfaces) {
        super(reqId, mgr);
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
        CallbackHolder.getInstance().onCallbackEvent(getReqId(), 1, 2, 0);
    }
}
