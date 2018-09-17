package com.huawei.facerecognition.request;

import android.content.Context;
import android.view.Surface;
import com.huawei.facerecognition.FaceRecognizeManager;
import com.huawei.facerecognition.FaceRecognizeManager.CallbackHolder;
import com.huawei.facerecognition.base.HwSecurityTaskThread;
import com.huawei.facerecognition.task.AuthenticateTask;
import com.huawei.facerecognition.utils.LogUtil;
import java.util.List;

public class AuthenticateRequest extends FaceRecognizeRequest {
    private Context mContext;
    private int mFlags;
    private List<Surface> mSurfaces;

    public AuthenticateRequest(int reqId, FaceRecognizeManager mgr, int flags, List<Surface> surfaces, Context context) {
        super(reqId, mgr);
        this.mSurfaces = surfaces;
        this.mFlags = flags;
        this.mContext = context;
    }

    public int getType() {
        return 1;
    }

    public boolean onReqStart() {
        LogUtil.i("", "start auth request");
        HwSecurityTaskThread.staticPushTask(new AuthenticateTask(null, this.mRetCallback, this, this.mContext), 0);
        return true;
    }

    public List<Surface> getSurfaces() {
        return this.mSurfaces;
    }

    public int getFlags() {
        return this.mFlags;
    }

    public void sendCancelOK() {
        CallbackHolder.getInstance().onCallbackEvent(getReqId(), 2, 2, 0);
    }
}
