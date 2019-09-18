package com.huawei.hwpostcamera;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

public class HwPostCamera {
    private static final int BIND_CORE_BIG = 2;
    private static final int BIND_CORE_RELEASE = 0;
    private static final int BIND_CORE_SMALL = 1;
    private static final int BLOCK_PARAM_PRE_AE_REGION = 2;
    private static final int BLOCK_PARAM_PRE_AF_REGION = 1;
    private static final int CLICK_DOWN_CAPTURE_CANCEL = 2;
    private static final int CLICK_DOWN_CAPTURE_CONFIRM = 1;
    private static final int CLICK_DOWN_CAPTURE_IDLE = 0;
    private static final int COMMAND_AF_LOCK = 4;
    private static final int COMMAND_BIND_CORE = 6;
    private static final int COMMAND_CLICKUP_CAPTURE = 8;
    private static final int COMMAND_CLICK_DOWN_CAPTRUE = 3;
    private static final int COMMAND_MOTORIZED_POPUP_MODE = 9;
    private static final int COMMAND_NOTIFY_CAPTURE_REQ_DONE = 5;
    private static final int COMMAND_PRE_CAPTRUE = 1;
    private static final int COMMAND_SET_LCD_WORK_MODE = 30;
    private static final int COMMAND_START_CAPTRUE = 2;
    public static final int HWPOST_CAMERA_HIDL_DIED_ERR = 32769;
    private static final int HWPOST_CAMERA_MSG_JPEG_DATA = 1;
    private static final int HWPOST_CAMERA_MSG_SNAPSHOT_CAP = 2;
    private static final int IDENTIFY_CODE = -1412567059;
    private static final int MOTORIZED_POPUP_FAST = 1;
    private static final int MOTORIZED_POPUP_MAX = 4;
    private static final int MOTORIZED_POPUP_NORMAL = 2;
    private static final int MOTORIZED_POPUP_SLOW = 3;
    private static final int PARAM_DEFAULT = 0;
    private static final String POST_DATA = "post_data";
    private static final String POST_FILE = "post_file";
    private static final String POST_FRAMENUM = "post_framenum";
    private static final String POST_GRADE = "post_grade";
    private static final String POST_STATUS = "post_status";
    private static final int PRECAPTRUE_TRIGGER_CANCEL = 2;
    private static final int PRECAPTRUE_TRIGGER_IDLE = 0;
    private static final int PRECAPTRUE_TRIGGER_START = 1;
    private static final String TAG = "HwPostCamera";
    private static int mApiVersion = 1;
    private static int mCurrentPid = -1;
    private static EventHandler mEventHandler = null;
    private static Object mHwPostErrCbLock = new Object();
    private static PostErrorCallback mHwPostErrorCallback = null;
    private static HwPostCamera mPostCamera = null;
    /* access modifiers changed from: private */
    public Object mAvSnapNumCbLock = new Object();
    /* access modifiers changed from: private */
    public AvailableSnapshotNumCallback mAvailableSnapshotNumCallback = null;
    /* access modifiers changed from: private */
    public PostPictureCallback mHwPostProcCallback = null;
    /* access modifiers changed from: private */
    public Object mHwPostProcCbLock = new Object();

    public interface AvailableSnapshotNumCallback {
        void onAvailableSnapshotNum(int i);
    }

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            PostPictureCallback cb;
            AvailableSnapshotNumCallback cb2;
            switch (msg.what) {
                case 1:
                    Log.i(HwPostCamera.TAG, "call back post jpeg data");
                    PostProcCaptureResult postRslt = (PostProcCaptureResult) msg.obj;
                    Bundle bndl = new Bundle();
                    bndl.putInt(HwPostCamera.POST_STATUS, postRslt.mStatus);
                    bndl.putInt(HwPostCamera.POST_GRADE, postRslt.mGrade);
                    bndl.putInt(HwPostCamera.POST_FRAMENUM, postRslt.mFrameNum);
                    bndl.putString(HwPostCamera.POST_FILE, postRslt.mFileName);
                    bndl.putByteArray(HwPostCamera.POST_DATA, postRslt.mBuffer);
                    synchronized (HwPostCamera.this.mHwPostProcCbLock) {
                        cb = HwPostCamera.this.mHwPostProcCallback;
                    }
                    if (cb != null) {
                        Log.i(HwPostCamera.TAG, "onPictureTaken() begin.");
                        cb.onPictureTaken(bndl);
                        Log.i(HwPostCamera.TAG, "onPictureTaken() end");
                        return;
                    }
                    return;
                case 2:
                    Log.i(HwPostCamera.TAG, "available capture number: " + msg.arg1);
                    synchronized (HwPostCamera.this.mAvSnapNumCbLock) {
                        cb2 = HwPostCamera.this.mAvailableSnapshotNumCallback;
                    }
                    if (cb2 != null) {
                        cb2.onAvailableSnapshotNum(msg.arg1);
                        return;
                    }
                    return;
                default:
                    Log.i(HwPostCamera.TAG, "unknown msg: " + msg.what);
                    return;
            }
        }
    }

    public interface PostErrorCallback {
        void onError(int i);
    }

    public interface PostPictureCallback {
        void onPictureTaken(Bundle bundle);
    }

    private static class PostProcCaptureResult {
        public byte[] mBuffer = null;
        public String mFileName = "unknown";
        public int mFrameNum = -1;
        public int mGrade;
        public int mStatus = 0;
    }

    private static native int nativeCheckIdentifyCode();

    private static native void nativePreLaunch(int i);

    private static native void nativeRegisterService(int i);

    private static native int nativeSendBlockParam(int i, int i2, int[] iArr);

    private static native int nativeSendCommand(int i, int i2, int i3);

    private static native void nativeUnregisterService();

    static {
        try {
            System.loadLibrary("HwPostCamera_jni");
        } catch (Exception e) {
            Log.i(TAG, e.toString(), e);
        }
    }

    public static HwPostCamera open(PostPictureCallback procCallback, PostErrorCallback errorCallback) {
        int pid = Process.myPid();
        Log.i(TAG, "open(), pid: " + pid);
        if (pid != mCurrentPid || !checkConnected()) {
            mCurrentPid = pid;
            mPostCamera = new HwPostCamera(procCallback, errorCallback);
        }
        return mPostCamera;
    }

    public static HwPostCamera camera2Open(PostPictureCallback procCallback, PostErrorCallback errorCallback, int version) {
        Log.i(TAG, "camera2Open()");
        mApiVersion = version;
        return open(procCallback, errorCallback);
    }

    HwPostCamera(PostPictureCallback procCallback, PostErrorCallback errorCallback) {
        Log.i(TAG, "HwPostCamera()");
        synchronized (mHwPostErrCbLock) {
            mHwPostErrorCallback = errorCallback;
        }
        if (!registerHwPostProcCallback(procCallback)) {
            Log.i(TAG, "Cann't register.");
            return;
        }
        Looper myLooper = Looper.myLooper();
        Looper looper = myLooper;
        if (myLooper != null) {
            mEventHandler = new EventHandler(looper);
        } else {
            Looper mainLooper = Looper.getMainLooper();
            Looper looper2 = mainLooper;
            if (mainLooper != null) {
                mEventHandler = new EventHandler(looper2);
            } else {
                mEventHandler = null;
                Log.i(TAG, "Event handler is null.");
            }
        }
    }

    public final void release() {
        Log.i(TAG, "release()");
        unregisterHwPostProcCallback();
    }

    private final boolean registerHwPostProcCallback(PostPictureCallback cb) {
        synchronized (this.mHwPostProcCbLock) {
            this.mHwPostProcCallback = cb;
        }
        nativeRegisterService(mApiVersion);
        return true;
    }

    private final void unregisterHwPostProcCallback() {
        synchronized (this.mHwPostProcCbLock) {
            this.mHwPostProcCallback = null;
        }
        synchronized (mHwPostErrCbLock) {
            mHwPostErrorCallback = null;
        }
        nativeUnregisterService();
    }

    private static void postProcResultFromNative(int status, String fileName, int grade, int frameNum, Object obj) {
        Log.i(TAG, "postProcResultFromNative(), status: " + status + ", file name: " + fileName + ", grade: " + grade + ", frame number: " + frameNum);
        if (mEventHandler != null) {
            Message msg = mEventHandler.obtainMessage();
            msg.what = 1;
            PostProcCaptureResult postResult = new PostProcCaptureResult();
            postResult.mStatus = status;
            postResult.mFileName = fileName;
            postResult.mGrade = grade;
            postResult.mFrameNum = frameNum;
            postResult.mBuffer = (byte[]) obj;
            msg.obj = postResult;
            Log.i(TAG, "sendMessage for capture result.");
            mEventHandler.sendMessage(msg);
        }
    }

    private static void postCaptureCapabilityFromNative(int capNum) {
        Log.i(TAG, "postCaptureCapabilityFromNative() number: " + capNum);
        if (mEventHandler != null) {
            Message msg = mEventHandler.obtainMessage();
            msg.what = 2;
            msg.arg1 = capNum;
            Log.i(TAG, "sendMessage for capture capability.");
            mEventHandler.sendMessage(msg);
        }
    }

    private static void postHidlDiedFromNative() {
        PostErrorCallback cb;
        Log.i(TAG, "postHidlDiedFromNative()");
        synchronized (mHwPostErrCbLock) {
            cb = mHwPostErrorCallback;
        }
        if (cb != null) {
            Log.i(TAG, "Camera daemon died!");
            cb.onError(HWPOST_CAMERA_HIDL_DIED_ERR);
        }
    }

    public final void setAvailableSnapshotNumCallback(AvailableSnapshotNumCallback cb) {
        StringBuilder sb = new StringBuilder();
        sb.append("setAvailableSnapshotNumCallback: ");
        sb.append(cb != null);
        Log.i(TAG, sb.toString());
        synchronized (this.mAvSnapNumCbLock) {
            this.mAvailableSnapshotNumCallback = cb;
        }
    }

    private final int sendCommand(int cmd, int param1, int param2) {
        Log.i(TAG, "send command: " + cmd + " with param: " + param1 + " and " + param2);
        return nativeSendCommand(cmd, param1, param2);
    }

    private final int sendBlockParam(int cmd, int[] param) {
        if (param == null) {
            Log.e(TAG, "cmd: " + cmd + ", with null param!");
            return -1;
        }
        Log.i(TAG, "send block param: " + cmd + " with size: " + param.length);
        return nativeSendBlockParam(cmd, param.length, param);
    }

    public final void preCaptureStart() {
        Log.i(TAG, "preCaptureStart");
        sendCommand(1, 1, 0);
    }

    public final void preCaptureCancel() {
        Log.i(TAG, "preCaptureStart");
        sendCommand(1, 2, 0);
    }

    public final void startCapture() {
        Log.i(TAG, "startCapture");
        sendCommand(2, 0, 0);
    }

    public final void motorizedPopupMode() {
        Log.i(TAG, "motorizedPopupMode");
        sendCommand(COMMAND_MOTORIZED_POPUP_MODE, 1, 0);
    }

    public static final boolean checkConnected() {
        Log.i(TAG, "checkConnected");
        if (IDENTIFY_CODE == nativeCheckIdentifyCode()) {
            return true;
        }
        return false;
    }

    public static final Boolean preLaunch(int id) {
        Log.i(TAG, "preLaunch");
        nativePreLaunch(id);
        return true;
    }

    public final void preAfRegion(int x, int y, int w, int h, int state) {
        Log.i(TAG, "preAfRegion: (" + x + ", " + y + ", " + w + ", " + h + ", " + state + ")");
        int[] param = new int[COMMAND_NOTIFY_CAPTURE_REQ_DONE];
        param[0] = x;
        param[1] = y;
        param[2] = w;
        param[3] = h;
        param[4] = state;
        sendBlockParam(1, param);
    }

    public final void preAeRegion(int x, int y, int w, int h, int state) {
        Log.i(TAG, "preAeRegion: (" + x + ", " + y + ", " + w + ", " + h + ", " + state + ")");
        int[] param = new int[COMMAND_NOTIFY_CAPTURE_REQ_DONE];
        param[0] = x;
        param[1] = y;
        param[2] = w;
        param[3] = h;
        param[4] = state;
        sendBlockParam(2, param);
    }

    public final void clickDownCaptureConfirm() {
        clickDownCaptureConfirmWithTime(0);
    }

    public final void clickDownCaptureConfirmWithTime(int onClickDownTime) {
        Log.i(TAG, "clickDownCaptureConfirmWithTime onClickDownTime=" + onClickDownTime);
        sendCommand(3, 1, onClickDownTime);
    }

    public final void clickDownCaptureConfirmWithTouchUpTime(int onClickUpTime) {
        Log.i(TAG, "clickDownCaptureConfirmWithTouchUpTime onClickUpTime=" + onClickUpTime);
        sendCommand(COMMAND_CLICKUP_CAPTURE, 1, onClickUpTime);
    }

    public final void clickDownCaptureCancel() {
        Log.i(TAG, "clickDownCaptureCancel");
        sendCommand(3, 2, 0);
    }

    public final void clickDownCaptureCancelWithTime(int onClickDownTime) {
        Log.i(TAG, "clickDownCaptureCancelWithTime onClickDownTime=" + onClickDownTime);
        sendCommand(3, 2, onClickDownTime);
    }

    public final void bindCore(int id) {
        Log.i(TAG, "bindCore " + id);
        sendCommand(COMMAND_BIND_CORE, id, 0);
    }

    public final void afLock() {
        Log.i(TAG, "afLock");
        sendCommand(4, 0, 0);
    }

    public final void notifyCaptureReqDone() {
        Log.i(TAG, "notifyCaptureReqDone");
        sendCommand(COMMAND_NOTIFY_CAPTURE_REQ_DONE, 0, 0);
    }

    public final void setLcdWorkMode(int lcdWorkMode, int lcdWorkAction) {
        Log.i(TAG, "setLcdWorkMode,  lcdWorkMode:" + lcdWorkMode + " lcdWorkAction:" + lcdWorkAction);
        sendCommand(COMMAND_SET_LCD_WORK_MODE, lcdWorkMode, lcdWorkAction);
    }
}
