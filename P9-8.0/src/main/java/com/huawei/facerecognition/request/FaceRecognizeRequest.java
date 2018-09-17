package com.huawei.facerecognition.request;

import com.huawei.facerecognition.FaceRecognizeManager;
import com.huawei.facerecognition.base.HwSecurityTaskBase;
import com.huawei.facerecognition.base.HwSecurityTaskBase.RetCallback;
import com.huawei.facerecognition.utils.LogUtil;

public abstract class FaceRecognizeRequest extends HwSecurityTaskBase {
    private static final String TAG = FaceRecognizeRequest.class.getSimpleName();
    private static final String[] TYPESTR = new String[]{"ENROLL", "AUTH", "REMOVE"};
    public static final int TYPE_AUTH = 1;
    public static final int TYPE_ENROLL = 0;
    public static final int TYPE_REMOVE = 2;
    private boolean mActiveCanceled;
    private boolean mCameraCanceled;
    private boolean mCanceled;
    private FaceRecognizeManager mMgr;
    private int mReqId;
    protected RetCallback mRetCallback = new RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            if (FaceRecognizeRequest.this.mMgr != null) {
                LogUtil.d(FaceRecognizeRequest.TAG, "End : " + toString());
                FaceRecognizeRequest.this.mMgr.onRequestEnd(FaceRecognizeRequest.this);
                return;
            }
            LogUtil.e("", "null manager");
        }
    };

    public abstract int getType();

    public abstract boolean onReqStart();

    public abstract void sendCancelOK();

    public FaceRecognizeRequest(int reqId, FaceRecognizeManager mgr) {
        super(null, null);
        this.mReqId = reqId;
        this.mMgr = mgr;
        this.mCanceled = false;
    }

    public int doAction() {
        LogUtil.i(TAG, "New " + toString());
        this.mMgr.onNewRequest(this);
        return 0;
    }

    public void onStop() {
        if (isActiveCanceled()) {
            sendCancelOK();
        }
    }

    public String toString() {
        return "request(id:" + this.mReqId + ", type:" + TYPESTR[getType()] + ")";
    }

    public String getTypeString() {
        return TYPESTR[getType()];
    }

    public int getReqId() {
        return this.mReqId;
    }

    public void setCancel() {
        this.mCanceled = true;
    }

    public void setCameraCancel() {
        this.mCanceled = true;
        this.mCameraCanceled = true;
    }

    public boolean isCanceled() {
        return this.mCanceled;
    }

    public boolean isCameraCanceled() {
        return this.mCameraCanceled;
    }

    public void setActiveCanceled() {
        this.mActiveCanceled = true;
    }

    public boolean isActiveCanceled() {
        return this.mActiveCanceled;
    }
}
