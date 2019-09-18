package huawei.android.security.facerecognition.task;

import android.content.Context;
import android.iawareperf.UniPerf;
import android.util.Flog;
import android.view.Surface;
import huawei.android.security.facerecognition.FaceCamera;
import huawei.android.security.facerecognition.FaceRecognizeManagerImpl;
import huawei.android.security.facerecognition.base.HwSecurityTaskBase;
import huawei.android.security.facerecognition.base.HwSecurityTaskThread;
import huawei.android.security.facerecognition.request.AuthenticateRequest;
import huawei.android.security.facerecognition.utils.DeviceUtil;
import huawei.android.security.facerecognition.utils.LogUtil;
import java.util.List;

public class AuthenticateTask extends FaceRecognizeTask {
    private static final int BD_REPORT_EVENT_ID_TEMP = 505;
    private static final int[] DISABLE_BOOST = {4};
    private static final int[] ENABLE_BOOST = {0};
    private static final int LOWTEMPERATURE_EVENT = 12289;
    public static final String SYSTEM_UI_PKG = "com.android.systemui";
    public static final String TAG = AuthenticateTask.class.getSimpleName();
    private static final int UNIPERF_ID = 3;
    private static final int[] UNIPERF_TAG = {57};
    private Context mContext;
    /* access modifiers changed from: private */
    public HwSecurityTaskBase.RetCallback mDoAuthCallback = new HwSecurityTaskBase.RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            if (2 == ret) {
                HwSecurityTaskThread.staticPushTask(new DoCancelAuthTask(null, AuthenticateTask.this.mDoCancelCallback, AuthenticateTask.this.mTaskRequest), 1);
            } else if (ret != 0 && 6 != ret) {
                AuthenticateTask.this.endAuth(ret, 0, 1);
            } else if (child instanceof DoAuthTask) {
                DoAuthTask detailTask = (DoAuthTask) child;
                AuthenticateTask.this.endAuth(ret, detailTask.getUserId(), detailTask.getErrorCode());
            } else {
                LogUtil.e(AuthenticateTask.TAG, "unexpected error after do auth, should never be here!!!!");
                AuthenticateTask.this.endAuth(ret, 0, 1);
            }
        }
    };
    /* access modifiers changed from: private */
    public HwSecurityTaskBase.RetCallback mDoCancelCallback = new HwSecurityTaskBase.RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            if (AuthenticateTask.this.mTaskRequest.isActiveCanceled()) {
                FaceRecognizeManagerImpl.CallbackHolder.getInstance().onCallbackEvent((int) AuthenticateTask.this.mTaskRequest.getReqId(), 2, 2, 0);
            }
            AuthenticateTask.this.endWithResult(ret);
        }
    };
    /* access modifiers changed from: private */
    public int mFlags;
    private boolean mIsLowTemperature;
    /* access modifiers changed from: private */
    public long mOpId;
    private HwSecurityTaskBase.RetCallback mPrepareCameraCallback = new HwSecurityTaskBase.RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            if (ret != 0 && ret != 2) {
                if (ret == 7) {
                    FaceRecognizeManagerImpl.CallbackHolder.getInstance().onCallbackEvent((int) AuthenticateTask.this.mTaskRequest.getReqId(), 2, 1, 12);
                } else {
                    FaceRecognizeManagerImpl.CallbackHolder.getInstance().onCallbackEvent((int) AuthenticateTask.this.mTaskRequest.getReqId(), 2, 1, 1);
                }
                AuthenticateTask.this.endWithResult(ret);
            } else if (!AuthenticateTask.this.mTaskRequest.isCanceled()) {
                DoAuthTask doAuthTask = new DoAuthTask(AuthenticateTask.this, AuthenticateTask.this.mDoAuthCallback, AuthenticateTask.this.mTaskRequest, AuthenticateTask.this.mFlags, AuthenticateTask.this.mOpId);
                HwSecurityTaskThread.staticPushTask(doAuthTask, 1);
            } else {
                FaceRecognizeManagerImpl.CallbackHolder.getInstance().onCallbackEvent((int) AuthenticateTask.this.mTaskRequest.getReqId(), 2, 1, 2);
                FaceRecognizeManagerImpl.CallbackHolder.getInstance().onCallbackEvent((int) AuthenticateTask.this.mTaskRequest.getReqId(), 2, 2, 0);
                AuthenticateTask.this.endWithResult(2);
            }
        }
    };
    private String mReportTempCap;
    private List<Surface> mSurfaces;

    public AuthenticateTask(FaceRecognizeTask parent, HwSecurityTaskBase.RetCallback callback, AuthenticateRequest request, Context context, long opId) {
        super(parent, callback, request);
        this.mFlags = request.getFlags();
        this.mSurfaces = request.getSurfaces();
        this.mContext = context;
        this.mOpId = opId;
    }

    public int doAction() {
        LogUtil.i("", "start auth task");
        double temp = DeviceUtil.getBatteryTemperature();
        double cap = DeviceUtil.getBatteryCapacity();
        this.mReportTempCap = "{\"temperature\":\"" + temp + "\", \"capacity\":\"" + cap + "\", \"nano_time\":\"" + System.nanoTime() + "\"}";
        Flog.bdReport(this.mContext, BD_REPORT_EVENT_ID_TEMP, this.mReportTempCap);
        LogUtil.i(TAG, this.mReportTempCap);
        this.mIsLowTemperature = false;
        if (DeviceUtil.reachDisabledTempCap(temp, cap)) {
            LogUtil.d("battery", "result : 11");
            FaceRecognizeManagerImpl.CallbackHolder.getInstance().onCallbackEvent((int) this.mTaskRequest.getReqId(), 2, 1, 11);
            return 5;
        }
        this.mIsLowTemperature = DeviceUtil.isLowTemperature(temp);
        if (this.mIsLowTemperature) {
            LogUtil.i(TAG, "low temperature");
            if ("com.android.systemui".equals(this.mContext.getOpPackageName())) {
                LogUtil.i(TAG, "is keygurad, start restrict frequency");
                UniPerf.getInstance().uniPerfSetConfig(3, UNIPERF_TAG, DISABLE_BOOST);
                UniPerf.getInstance().uniPerfEvent(LOWTEMPERATURE_EVENT, "", new int[]{0});
            }
        }
        HwSecurityTaskThread.staticPushTask(new PrepareCameraTask(this, this.mPrepareCameraCallback, this.mTaskRequest, this.mSurfaces), 1);
        return -1;
    }

    /* access modifiers changed from: protected */
    public void endWithResult(int ret) {
        Flog.bdReport(this.mContext, BD_REPORT_EVENT_ID_TEMP, this.mReportTempCap);
        LogUtil.i(TAG, this.mReportTempCap);
        this.mReportTempCap = null;
        if (this.mIsLowTemperature && "com.android.systemui".equals(this.mContext.getOpPackageName())) {
            LogUtil.i(TAG, "is keygurad, end restrict frequency");
            UniPerf.getInstance().uniPerfEvent(LOWTEMPERATURE_EVENT, "", new int[]{-1});
            UniPerf.getInstance().uniPerfSetConfig(3, UNIPERF_TAG, ENABLE_BOOST);
        }
        FaceCamera.getInstance().close();
        super.endWithResult(ret);
    }

    /* access modifiers changed from: private */
    public void endAuth(int ret, int userId, int errorCode) {
        LogUtil.d(">>>>>>>>>>", "result : " + errorCode);
        FaceRecognizeManagerImpl.CallbackHolder.getInstance().onCallbackEvent((int) this.mTaskRequest.getReqId(), 2, 1, errorCode);
        if (this.mTaskRequest.isActiveCanceled()) {
            FaceRecognizeManagerImpl.CallbackHolder.getInstance().onCallbackEvent((int) this.mTaskRequest.getReqId(), 2, 2, errorCode);
        }
        endWithResult(ret);
    }
}
