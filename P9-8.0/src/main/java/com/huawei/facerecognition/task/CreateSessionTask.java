package com.huawei.facerecognition.task;

import android.view.Surface;
import com.huawei.facerecognition.FaceCamera;
import com.huawei.facerecognition.FaceRecognizeEvent;
import com.huawei.facerecognition.base.HwSecurityMsgCenter;
import com.huawei.facerecognition.base.HwSecurityTaskBase;
import com.huawei.facerecognition.base.HwSecurityTaskBase.EventListener;
import com.huawei.facerecognition.base.HwSecurityTaskBase.RetCallback;
import com.huawei.facerecognition.base.HwSecurityTaskBase.TimerOutProc;
import com.huawei.facerecognition.base.HwSecurityTimerTask;
import com.huawei.facerecognition.request.FaceRecognizeRequest;
import com.huawei.facerecognition.utils.LogUtil;
import java.util.List;

public class CreateSessionTask extends FaceRecognizeTask implements EventListener {
    private static final long TIMEOUT = 3000;
    private int mReqType;
    private List<Surface> mSurfaces;
    TimerOutProc mTimeoutProc = new TimerOutProc() {
        public void onTimerOut() {
            CreateSessionTask.this.-wrap0(5);
        }
    };
    private HwSecurityTimerTask mTimer = new HwSecurityTimerTask();

    public CreateSessionTask(HwSecurityTaskBase parent, RetCallback callback, FaceRecognizeRequest request, List<Surface> surfaces) {
        super(parent, callback, request);
        this.mSurfaces = surfaces;
        this.mReqType = request.getType();
        if (1 == this.mReqType) {
            HwSecurityMsgCenter.staticRegisterEvent(1, this, this);
        } else if (this.mReqType == 0) {
            HwSecurityMsgCenter.staticRegisterEvent(2, this, this);
        }
        HwSecurityMsgCenter.staticRegisterEvent(6, this, this);
        HwSecurityMsgCenter.staticRegisterEvent(4, this, this);
    }

    public boolean onEvent(FaceRecognizeEvent ev) {
        switch (ev.getType()) {
            case 1:
            case 2:
                if (ev.getArgs()[0] == this.mTaskRequest.getReqId()) {
                    this.mTaskRequest.setCancel();
                    return true;
                }
                break;
            case 4:
                if (!this.mTaskRequest.isCanceled()) {
                    if (ev.getArgs()[0] != FaceCamera.RET_CREATE_SESSION_OK) {
                        -wrap0(5);
                        break;
                    }
                    -wrap0(0);
                    break;
                }
                -wrap0(2);
                break;
            case 6:
                -wrap0(5);
                break;
            default:
                return false;
        }
        return true;
    }

    public int doAction() {
        LogUtil.i("", "start create session task");
        int ret = FaceCamera.getInstance().createPreviewSession(this.mSurfaces);
        if (ret == 1) {
            this.mTimer.setTimeout(TIMEOUT, this.mTimeoutProc);
            return -1;
        } else if (ret == 0) {
            return 0;
        } else {
            return 5;
        }
    }

    public void onStop() {
        this.mTimer.cancel();
        if (1 == this.mReqType) {
            HwSecurityMsgCenter.staticUnregisterEvent(1, this);
        } else if (this.mReqType == 0) {
            HwSecurityMsgCenter.staticUnregisterEvent(2, this);
        }
        HwSecurityMsgCenter.staticUnregisterEvent(6, this);
        HwSecurityMsgCenter.staticUnregisterEvent(4, this);
    }
}
