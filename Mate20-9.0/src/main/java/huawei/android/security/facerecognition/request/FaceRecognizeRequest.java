package huawei.android.security.facerecognition.request;

import huawei.android.security.facerecognition.FaceRecognizeManagerImpl;
import huawei.android.security.facerecognition.base.HwSecurityTaskBase;
import huawei.android.security.facerecognition.utils.LogUtil;

public abstract class FaceRecognizeRequest extends HwSecurityTaskBase {
    /* access modifiers changed from: private */
    public static final String TAG = FaceRecognizeRequest.class.getSimpleName();
    private static final String[] TYPESTR = {"ENROLL", "AUTH", "REMOVE"};
    public static final int TYPE_AUTH = 1;
    public static final int TYPE_ENROLL = 0;
    public static final int TYPE_REMOVE = 2;
    private boolean mActiveCanceled;
    private boolean mCameraCanceled;
    private boolean mCanceled;
    /* access modifiers changed from: private */
    public FaceRecognizeManagerImpl mMgr;
    private long mReqId;
    protected HwSecurityTaskBase.RetCallback mRetCallback = new HwSecurityTaskBase.RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            if (FaceRecognizeRequest.this.mMgr != null) {
                String access$100 = FaceRecognizeRequest.TAG;
                LogUtil.d(access$100, "End : " + toString());
                FaceRecognizeRequest.this.mMgr.onRequestEnd(FaceRecognizeRequest.this);
                return;
            }
            LogUtil.e("", "null manager");
        }
    };

    public abstract int getType();

    public abstract boolean onReqStart();

    public abstract void sendCancelOK();

    public FaceRecognizeRequest(long reqId, FaceRecognizeManagerImpl mgr) {
        super(null, null);
        this.mReqId = reqId;
        this.mMgr = mgr;
        this.mCanceled = false;
    }

    public int doAction() {
        String str = TAG;
        LogUtil.i(str, "New " + toString());
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

    public long getReqId() {
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
