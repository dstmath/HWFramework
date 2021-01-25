package com.huawei.hwpostcamera;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

public class HwPostCamera {
    private static final int BLOCK_PARAM_PRE_AE_REGION = 2;
    private static final int BLOCK_PARAM_PRE_AF_REGION = 1;
    private static final int CLICK_DOWN_CAPTURE_CANCEL = 2;
    private static final int CLICK_DOWN_CAPTURE_CONFIRM = 1;
    private static final int COMMAND_AF_LOCK = 4;
    private static final int COMMAND_BIND_CORE = 6;
    private static final int COMMAND_CLICKUP_CAPTURE = 8;
    private static final int COMMAND_CLICK_DOWN_CAPTRUE = 3;
    private static final int COMMAND_MOTORIZED_POPUP_MODE = 9;
    private static final int COMMAND_NOTIFY_CAPTURE_REQ_DONE = 5;
    private static final int COMMAND_PRE_CAPTRUE = 1;
    private static final int COMMAND_SET_LCD_WORK_MODE = 30;
    private static final int COMMAND_START_CAPTRUE = 2;
    private static final int CURRENT_PID_DEFAULE = -1;
    private static final int ERROR_DEFAULT = -1;
    private static final String FILE_NAME_DEFAULT = "unknown";
    private static final int FRAME_NUMBER_DEFAULT = -1;
    public static final int HWPOST_CAMERA_HIDL_DIED_ERR = 32769;
    private static final int HWPOST_CAMERA_MSG_JPEG_DATA = 1;
    private static final int HWPOST_CAMERA_MSG_SNAPSHOT_CAP = 2;
    private static final String HW_POST_CAMERA_JNI = "HwPostCamera_jni";
    private static final Object HW_POST_ERROR_CALLBACK_LOCK = new Object();
    private static final int IDENTIFY_CODE = -1412567059;
    private static final int MOTORIZED_POPUP_FAST = 1;
    private static final int PARAM_DEFAULT = 0;
    private static final String POST_DATA = "post_data";
    private static final String POST_FILE = "post_file";
    private static final String POST_FRAMENUM = "post_framenum";
    private static final String POST_GRADE = "post_grade";
    private static final String POST_STATUS = "post_status";
    private static final int PRECAPTRUE_TRIGGER_CANCEL = 2;
    private static final int PRECAPTRUE_TRIGGER_START = 1;
    private static final String TAG = "HwPostCamera";
    private static int mApiVersion = 1;
    private static int mCurrentPid = -1;
    private static EventHandler mEventHandler = null;
    private static PostErrorCallback mHwPostErrorCallback = null;
    private static HwPostCamera mPostCamera = null;
    private final Object mAvSnapNumberCallbackLock = new Object();
    private AvailableSnapshotNumCallback mAvailableSnapshotNumCallback = null;
    private PostPictureCallback mHwPostProcCallback = null;
    private final Object mHwPostProcCallbackLock = new Object();

    public interface AvailableSnapshotNumCallback {
        void onAvailableSnapshotNum(int i);
    }

    public interface PostErrorCallback {
        void onError(int i);
    }

    public interface PostPictureCallback {
        void onPictureTaken(Bundle bundle);
    }

    private static native int nativeCheckIdentifyCode();

    private static native void nativePreLaunch(int i);

    private static native void nativeRegisterService(int i);

    private static native int nativeSendBlockParam(int i, int i2, int[] iArr);

    private static native int nativeSendCommand(int i, int i2, int i3);

    private static native void nativeUnregisterService();

    static {
        try {
            System.loadLibrary(HW_POST_CAMERA_JNI);
        } catch (UnsatisfiedLinkError error) {
            Log.e(TAG, "The UnsatisfiedLinkError is " + error.getMessage());
        } catch (Exception error2) {
            Log.e(TAG, "The Exception is " + error2.getMessage());
        }
    }

    public HwPostCamera(PostPictureCallback procCallback, PostErrorCallback errorCallback) {
        Log.i(TAG, "HwPostCamera()");
        synchronized (HW_POST_ERROR_CALLBACK_LOCK) {
            mHwPostErrorCallback = errorCallback;
        }
        if (!registerHwPostProcCallback(procCallback)) {
            Log.i(TAG, "Can not register.");
            return;
        }
        Looper myLooper = Looper.myLooper();
        if (myLooper != null) {
            mEventHandler = new EventHandler(myLooper);
            return;
        }
        Looper mainLooper = Looper.getMainLooper();
        if (mainLooper != null) {
            mEventHandler = new EventHandler(mainLooper);
            return;
        }
        mEventHandler = null;
        Log.i(TAG, "Event handler is null.");
    }

    public static HwPostCamera open(PostPictureCallback procCallback, PostErrorCallback errorCallback) {
        int pid = Process.myPid();
        Log.i(TAG, "open(), and the pid: " + pid);
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

    public final void release() {
        Log.i(TAG, "release()");
        unregisterHwPostProcCallback();
    }

    private boolean registerHwPostProcCallback(PostPictureCallback callback) {
        synchronized (this.mHwPostProcCallbackLock) {
            this.mHwPostProcCallback = callback;
        }
        nativeRegisterService(mApiVersion);
        return true;
    }

    private void unregisterHwPostProcCallback() {
        synchronized (this.mHwPostProcCallbackLock) {
            this.mHwPostProcCallback = null;
        }
        synchronized (HW_POST_ERROR_CALLBACK_LOCK) {
            mHwPostErrorCallback = null;
        }
        nativeUnregisterService();
    }

    private static class PostProcCaptureResult {
        private byte[] mBuffers = null;
        private String mFileName = HwPostCamera.FILE_NAME_DEFAULT;
        private int mFrameNum = -1;
        private int mGrade;
        private int mStatus = HwPostCamera.PARAM_DEFAULT;

        PostProcCaptureResult() {
        }

        public int getStatus() {
            return this.mStatus;
        }

        public void setStatus(int status) {
            this.mStatus = status;
        }

        public String getFileName() {
            return this.mFileName;
        }

        public void setFileName(String fileName) {
            this.mFileName = fileName;
        }

        public byte[] getBuffers() {
            return this.mBuffers;
        }

        public void setBuffers(byte[] buffers) {
            this.mBuffers = buffers;
        }

        public int getGrade() {
            return this.mGrade;
        }

        public void setGrade(int gradre) {
            this.mGrade = gradre;
        }

        public int getFrameNum() {
            return this.mFrameNum;
        }

        public void setFrameNum(int frameNum) {
            this.mFrameNum = frameNum;
        }
    }

    private class EventHandler extends Handler {
        EventHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            PostPictureCallback callback;
            AvailableSnapshotNumCallback callback2;
            int i = message.what;
            if (i == 1) {
                Log.i(HwPostCamera.TAG, "call back post jpeg data");
                if (message.obj instanceof PostProcCaptureResult) {
                    PostProcCaptureResult postCaptureResult = (PostProcCaptureResult) message.obj;
                    Bundle bundle = new Bundle();
                    bundle.putInt(HwPostCamera.POST_STATUS, postCaptureResult.getStatus());
                    bundle.putInt(HwPostCamera.POST_GRADE, postCaptureResult.getGrade());
                    bundle.putInt(HwPostCamera.POST_FRAMENUM, postCaptureResult.getFrameNum());
                    bundle.putString(HwPostCamera.POST_FILE, postCaptureResult.getFileName());
                    bundle.putByteArray(HwPostCamera.POST_DATA, postCaptureResult.getBuffers());
                    synchronized (HwPostCamera.this.mHwPostProcCallbackLock) {
                        callback = HwPostCamera.this.mHwPostProcCallback;
                    }
                    if (callback != null) {
                        Log.i(HwPostCamera.TAG, "onPictureTaken() begin.");
                        callback.onPictureTaken(bundle);
                        Log.i(HwPostCamera.TAG, "onPictureTaken() end");
                    }
                }
            } else if (i != 2) {
                Log.i(HwPostCamera.TAG, "unknown message: " + message.what);
            } else {
                Log.i(HwPostCamera.TAG, "available capture number: " + message.arg1);
                synchronized (HwPostCamera.this.mAvSnapNumberCallbackLock) {
                    callback2 = HwPostCamera.this.mAvailableSnapshotNumCallback;
                }
                if (callback2 != null) {
                    callback2.onAvailableSnapshotNum(message.arg1);
                }
            }
        }
    }

    private static void postProcResultFromNative(int status, String fileName, int grade, int frameNum, Object obj) {
        Log.i(TAG, "postProcResultFromNative() with the Jni Method");
        EventHandler eventHandler = mEventHandler;
        if (eventHandler != null) {
            Message message = eventHandler.obtainMessage();
            message.what = 1;
            PostProcCaptureResult postResult = new PostProcCaptureResult();
            postResult.setStatus(status);
            postResult.setFileName(fileName);
            postResult.setGrade(grade);
            postResult.setFrameNum(frameNum);
            if (obj instanceof byte[]) {
                postResult.setBuffers((byte[]) obj);
            }
            message.obj = postResult;
            Log.i(TAG, "sendMessage for capture result.");
            mEventHandler.sendMessage(message);
        }
    }

    private static void postCaptureCapabilityFromNative(int captureNum) {
        Log.i(TAG, "postCaptureCapabilityFromNative() number: " + captureNum);
        EventHandler eventHandler = mEventHandler;
        if (eventHandler != null) {
            Message message = eventHandler.obtainMessage();
            message.what = 2;
            message.arg1 = captureNum;
            Log.i(TAG, "sendMessage for capture capability.");
            mEventHandler.sendMessage(message);
        }
    }

    private static void postHidlDiedFromNative() {
        PostErrorCallback postErrorCallback;
        Log.i(TAG, "postHidlDiedFromNative()");
        synchronized (HW_POST_ERROR_CALLBACK_LOCK) {
            postErrorCallback = mHwPostErrorCallback;
        }
        if (postErrorCallback != null) {
            Log.i(TAG, "Camera daemon died!");
            postErrorCallback.onError(HWPOST_CAMERA_HIDL_DIED_ERR);
        }
    }

    public final void setAvailableSnapshotNumCallback(AvailableSnapshotNumCallback callback) {
        if (callback == null) {
            Log.i(TAG, "setAvailableSnapshotNumCallback: the callback is null.");
        } else {
            Log.i(TAG, "setAvailableSnapshotNumCallback: the callback is not null.");
        }
        synchronized (this.mAvSnapNumberCallbackLock) {
            this.mAvailableSnapshotNumCallback = callback;
        }
    }

    private void sendCommand(int command, int commandType, int commandValue) {
        Log.i(TAG, "send command: " + command + " with parameter: " + commandType + " and " + commandValue);
        int sendCommandRet = nativeSendCommand(command, commandType, commandValue);
        if (sendCommandRet == -1) {
            Log.e(TAG, "send command error and the return is " + sendCommandRet);
        }
    }

    private void sendBlockParam(int command, int[] parameters) {
        if (parameters == null) {
            Log.e(TAG, "command: " + command + ", with null parameters!");
            return;
        }
        Log.i(TAG, "send block param: " + command + " with size: " + parameters.length);
        int sendBlockParamRet = nativeSendBlockParam(command, parameters.length, parameters);
        if (sendBlockParamRet == -1) {
            Log.e(TAG, "send block param error and the return is " + sendBlockParamRet);
        }
    }

    public final void preCaptureStart() {
        Log.i(TAG, "preCaptureStart");
        sendCommand(1, 1, PARAM_DEFAULT);
    }

    public final void preCaptureCancel() {
        Log.i(TAG, "preCaptureStart");
        sendCommand(1, 2, PARAM_DEFAULT);
    }

    public final void startCapture() {
        Log.i(TAG, "startCapture");
        sendCommand(2, PARAM_DEFAULT, PARAM_DEFAULT);
    }

    public final void motorizedPopupMode() {
        Log.i(TAG, "motorizedPopupMode");
        sendCommand(COMMAND_MOTORIZED_POPUP_MODE, 1, PARAM_DEFAULT);
    }

    public static final boolean checkConnected() {
        Log.i(TAG, "checkConnected");
        if (nativeCheckIdentifyCode() == IDENTIFY_CODE) {
            return true;
        }
        return false;
    }

    public static final boolean preLaunch(int id) {
        Log.i(TAG, "preLaunch");
        nativePreLaunch(id);
        return true;
    }

    public final void preAfRegion(int coordinateX, int coordinateY, int width, int height, int state) {
        Log.i(TAG, "preAfRegion: (" + coordinateX + ", " + coordinateY + ", " + width + ", " + height + ", " + state + ")");
        sendBlockParam(1, new int[]{coordinateX, coordinateY, width, height, state});
    }

    public final void preAeRegion(int coordinateX, int coordinateY, int width, int height, int state) {
        Log.i(TAG, "preAeRegion: (" + coordinateX + ", " + coordinateY + ", " + width + ", " + height + ", " + state + ")");
        sendBlockParam(2, new int[]{coordinateX, coordinateY, width, height, state});
    }

    public final void clickDownCaptureConfirm() {
        clickDownCaptureConfirmWithTime(PARAM_DEFAULT);
    }

    public final void clickDownCaptureConfirmWithTime(int onClickDownTime) {
        Log.i(TAG, "clickDownCaptureConfirmWithTime onClickDownTime=" + onClickDownTime);
        sendCommand(COMMAND_CLICK_DOWN_CAPTRUE, 1, onClickDownTime);
    }

    public final void clickDownCaptureConfirmWithTouchUpTime(int onClickUpTime) {
        Log.i(TAG, "clickDownCaptureConfirmWithTouchUpTime onClickUpTime=" + onClickUpTime);
        sendCommand(COMMAND_CLICKUP_CAPTURE, 1, onClickUpTime);
    }

    public final void clickDownCaptureCancel() {
        Log.i(TAG, "clickDownCaptureCancel");
        sendCommand(COMMAND_CLICK_DOWN_CAPTRUE, 2, PARAM_DEFAULT);
    }

    public final void clickDownCaptureCancelWithTime(int onClickDownTime) {
        Log.i(TAG, "clickDownCaptureCancelWithTime onClickDownTime=" + onClickDownTime);
        sendCommand(COMMAND_CLICK_DOWN_CAPTRUE, 2, onClickDownTime);
    }

    public final void bindCore(int commandValue) {
        Log.i(TAG, "bindCore " + commandValue);
        sendCommand(COMMAND_BIND_CORE, commandValue, PARAM_DEFAULT);
    }

    public final void afLock() {
        Log.i(TAG, "afLock");
        sendCommand(COMMAND_AF_LOCK, PARAM_DEFAULT, PARAM_DEFAULT);
    }

    public final void notifyCaptureReqDone() {
        Log.i(TAG, "notifyCaptureReqDone");
        sendCommand(COMMAND_NOTIFY_CAPTURE_REQ_DONE, PARAM_DEFAULT, PARAM_DEFAULT);
    }

    public final void setLcdWorkMode(int lcdWorkMode, int lcdWorkAction) {
        Log.i(TAG, "setLcdWorkMode, lcdWorkMode:" + lcdWorkMode + " lcdWorkAction:" + lcdWorkAction);
        sendCommand(COMMAND_SET_LCD_WORK_MODE, lcdWorkMode, lcdWorkAction);
    }
}
