package com.huawei.facerecognition.task;

import android.content.ContentResolver;
import android.content.Context;
import android.iawareperf.UniPerf;
import android.os.SystemProperties;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.util.Flog;
import android.view.Surface;
import com.huawei.facerecognition.FaceCamera;
import com.huawei.facerecognition.FaceRecognizeManager.CallbackHolder;
import com.huawei.facerecognition.base.HwSecurityTaskBase;
import com.huawei.facerecognition.base.HwSecurityTaskBase.RetCallback;
import com.huawei.facerecognition.base.HwSecurityTaskThread;
import com.huawei.facerecognition.request.AuthenticateRequest;
import com.huawei.facerecognition.utils.DeviceUtil;
import com.huawei.facerecognition.utils.LogUtil;
import java.util.List;

public class AuthenticateTask extends FaceRecognizeTask {
    private static final int BD_REPORT_EVENT_ID_TEMP = 505;
    private static final boolean CHANGE_BRIGHTNESS;
    private static final int[] DISABLE_BOOST = new int[]{4};
    private static final int[] ENABLE_BOOST = new int[]{0};
    private static final int LOWTEMPERATURE_EVENT = 12289;
    private static final int LOW_TEMP_SAFE_BRIGHTNESS = 90;
    private static final String PRODUCT_NAME = SystemProperties.get("ro.product.name", "");
    public static final String SYSTEM_UI_PKG = "com.android.systemui";
    public static final String TAG = AuthenticateTask.class.getSimpleName();
    private static final int UNIPERF_ID = 3;
    private static final int[] UNIPERF_TAG = new int[]{38};
    private int mBrightness;
    private Context mContext;
    private RetCallback mDoAuthCallback = new RetCallback() {
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
    private RetCallback mDoCancelCallback = new RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            if (AuthenticateTask.this.mTaskRequest.isActiveCanceled()) {
                CallbackHolder.getInstance().onCallbackEvent(AuthenticateTask.this.mTaskRequest.getReqId(), 2, 2, 0);
            }
            AuthenticateTask.this.endWithResult(ret);
        }
    };
    private int mFlags;
    private boolean mIsLowTemperature;
    private RetCallback mPrepareCameraCallback = new RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            if (ret != 0 && ret != 2) {
                CallbackHolder.getInstance().onCallbackEvent(AuthenticateTask.this.mTaskRequest.getReqId(), 2, 1, 1);
                AuthenticateTask.this.endWithResult(ret);
            } else if (AuthenticateTask.this.mTaskRequest.isCanceled()) {
                CallbackHolder.getInstance().onCallbackEvent(AuthenticateTask.this.mTaskRequest.getReqId(), 2, 1, 2);
                CallbackHolder.getInstance().onCallbackEvent(AuthenticateTask.this.mTaskRequest.getReqId(), 2, 2, 0);
                AuthenticateTask.this.endWithResult(2);
            } else {
                HwSecurityTaskThread.staticPushTask(new DoAuthTask(AuthenticateTask.this, AuthenticateTask.this.mDoAuthCallback, AuthenticateTask.this.mTaskRequest, AuthenticateTask.this.mFlags), 1);
            }
        }
    };
    private String mReportTempCap;
    private ScreenLighter mScreenLighter;
    private int mScreenMode;
    private List<Surface> mSurfaces;

    static {
        boolean z = true;
        if (!(PRODUCT_NAME.startsWith("FIG") || PRODUCT_NAME.startsWith("LLD") || PRODUCT_NAME.startsWith("FLA") || PRODUCT_NAME.startsWith("LND") || PRODUCT_NAME.startsWith("LDN") || PRODUCT_NAME.startsWith("ATU"))) {
            z = PRODUCT_NAME.startsWith("AUM");
        }
        CHANGE_BRIGHTNESS = z;
    }

    public AuthenticateTask(FaceRecognizeTask parent, RetCallback callback, AuthenticateRequest request, Context context) {
        super(parent, callback, request);
        this.mFlags = request.getFlags();
        this.mSurfaces = request.getSurfaces();
        this.mContext = context;
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
            CallbackHolder.getInstance().onCallbackEvent(this.mTaskRequest.getReqId(), 2, 1, 11);
            return 5;
        }
        this.mIsLowTemperature = DeviceUtil.isLowTemperature(temp);
        if (this.mIsLowTemperature) {
            LogUtil.i(TAG, "low temperature");
            if ("com.android.systemui".equals(this.mContext.getOpPackageName())) {
                LogUtil.i(TAG, "is keygurad, start restrict frequency");
                UniPerf.getInstance().uniPerfSetConfig(3, UNIPERF_TAG, DISABLE_BOOST);
                UniPerf.getInstance().uniPerfEvent(LOWTEMPERATURE_EVENT, "", new int[]{0});
                if (CHANGE_BRIGHTNESS) {
                    this.mBrightness = getScreenBrightness();
                    if (this.mBrightness > 90) {
                        setScreenBrightness(90, true);
                    }
                }
            }
        } else if ("com.android.systemui".equals(this.mContext.getOpPackageName())) {
            this.mScreenLighter = new ScreenLighter(this.mContext);
            this.mScreenLighter.onStart();
        }
        HwSecurityTaskThread.staticPushTask(new PrepareCameraTask(this, this.mPrepareCameraCallback, this.mTaskRequest, this.mSurfaces), 1);
        return -1;
    }

    protected void endWithResult(int ret) {
        Flog.bdReport(this.mContext, BD_REPORT_EVENT_ID_TEMP, this.mReportTempCap);
        LogUtil.i(TAG, this.mReportTempCap);
        this.mReportTempCap = null;
        if (this.mIsLowTemperature) {
            if ("com.android.systemui".equals(this.mContext.getOpPackageName())) {
                LogUtil.i(TAG, "is keygurad, end restrict frequency");
                UniPerf.getInstance().uniPerfEvent(LOWTEMPERATURE_EVENT, "", new int[]{-1});
                UniPerf.getInstance().uniPerfSetConfig(3, UNIPERF_TAG, ENABLE_BOOST);
                if (CHANGE_BRIGHTNESS && this.mBrightness > 90) {
                    setScreenBrightness(this.mBrightness, false);
                    recoverScreenMode();
                }
            }
        } else if (this.mScreenLighter != null) {
            this.mScreenLighter.onStop();
        }
        FaceCamera.getInstance().close();
        super.-wrap1(ret);
    }

    private void endAuth(int ret, int userId, int errorCode) {
        LogUtil.d(">>>>>>>>>>", "result : " + errorCode);
        CallbackHolder.getInstance().onCallbackEvent(this.mTaskRequest.getReqId(), 2, 1, errorCode);
        if (this.mTaskRequest.isActiveCanceled()) {
            CallbackHolder.getInstance().onCallbackEvent(this.mTaskRequest.getReqId(), 2, 2, errorCode);
        }
        endWithResult(ret);
    }

    private void setScrennManualMode(boolean updateMode) {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        try {
            int mode = System.getInt(contentResolver, "screen_brightness_mode");
            LogUtil.d(TAG, "setScrennManualMode mScreenMode = " + mode);
            if (mode == 1) {
                System.putInt(contentResolver, "screen_brightness_mode", 0);
            }
            if (updateMode) {
                this.mScreenMode = mode;
            }
        } catch (SettingNotFoundException e) {
            LogUtil.w(TAG, "Setting Not Found!");
        }
    }

    private void recoverScreenMode() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        LogUtil.d(TAG, "recoverScreenMode mScreenMode = " + this.mScreenMode);
        if (this.mScreenMode == 1) {
            System.putInt(contentResolver, "screen_brightness_mode", 1);
        }
    }

    private int getScreenBrightness() {
        return System.getInt(this.mContext.getContentResolver(), "screen_brightness", 90);
    }

    private void setScreenBrightness(int brightness, boolean updateMode) {
        setScrennManualMode(updateMode);
        System.putInt(this.mContext.getContentResolver(), "screen_brightness", brightness);
    }
}
