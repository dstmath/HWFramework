package android.hardware;

import android.app.ActivityThread;
import android.bluetooth.BluetoothAssignedNumbers;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hsm.HwSystemManager;
import android.media.IAudioService.Stub;
import android.net.ProxyInfo;
import android.net.wifi.AnqpInformationElement;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Element.DataKind;
import android.renderscript.Element.DataType;
import android.renderscript.RSIllegalArgumentException;
import android.renderscript.RenderScript;
import android.renderscript.Type.Builder;
import android.rms.HwSysResource;
import android.system.OsConstants;
import android.text.TextUtils.SimpleStringSplitter;
import android.text.TextUtils.StringSplitter;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Deprecated
public class Camera {
    @Deprecated
    public static final String ACTION_NEW_PICTURE = "android.hardware.action.NEW_PICTURE";
    @Deprecated
    public static final String ACTION_NEW_VIDEO = "android.hardware.action.NEW_VIDEO";
    public static final int CAMERA_ERROR_EVICTED = 2;
    public static final int CAMERA_ERROR_SERVER_DIED = 100;
    public static final int CAMERA_ERROR_UNKNOWN = 1;
    private static final int CAMERA_FACE_DETECTION_HW = 0;
    private static final int CAMERA_FACE_DETECTION_SW = 1;
    public static final int CAMERA_HAL_API_VERSION_1_0 = 256;
    private static final int CAMERA_HAL_API_VERSION_NORMAL_CONNECT = -2;
    private static final int CAMERA_HAL_API_VERSION_UNSPECIFIED = -1;
    private static final int CAMERA_MSG_COMPRESSED_IMAGE = 256;
    private static final int CAMERA_MSG_DARK_RAIDER = 134217728;
    private static final int CAMERA_MSG_DUAL_CAMERA_SHELTER = 524288;
    private static final int CAMERA_MSG_DUAL_CAMERA_VERIFY = 262144;
    private static final int CAMERA_MSG_ERROR = 1;
    private static final int CAMERA_MSG_EXPOSURE_MEASURE_TIME = 1048576;
    private static final int CAMERA_MSG_EXPOSURE_PREVIEW_STATE = 2097152;
    private static final int CAMERA_MSG_EXTEND = 1073741824;
    private static final int CAMERA_MSG_FOCUS = 4;
    private static final int CAMERA_MSG_FOCUS_MOVE = 2048;
    private static final int CAMERA_MSG_FOV_RESULT = 67108864;
    private static final int CAMERA_MSG_HDC_RESULT = 33554432;
    private static final int CAMERA_MSG_LASER_CALIBRATION = 16777216;
    private static final int CAMERA_MSG_LCD_COMPENSATE_LIGHT = 131072;
    private static final int CAMERA_MSG_LPD_PARAMS = Integer.MIN_VALUE;
    private static final int CAMERA_MSG_MMI_FOCUS_RESULT = 268435456;
    private static final int CAMERA_MSG_MMI_TEST_FEATURE = 536870912;
    private static final int CAMERA_MSG_OIS_GRYO_OFFSET_CALIB_RESULT = 1;
    private static final int CAMERA_MSG_OIS_HALL_CALIB_RESULT = 2;
    private static final int CAMERA_MSG_OIS_HALL_CHECK_RESULT = 3;
    private static final int CAMERA_MSG_PDAF_VERIFY = 4194304;
    private static final int CAMERA_MSG_POSTVIEW_FRAME = 64;
    private static final int CAMERA_MSG_PREVIEW_FRAME = 16;
    private static final int CAMERA_MSG_PREVIEW_METADATA = 1024;
    private static final int CAMERA_MSG_PREVIEW_PARAMS = 8388608;
    private static final int CAMERA_MSG_RAW_IMAGE = 128;
    private static final int CAMERA_MSG_RAW_IMAGE_NOTIFY = 512;
    private static final int CAMERA_MSG_SHUTTER = 2;
    private static final int CAMERA_MSG_TARGET_TRACKING = 65536;
    private static final int CAMERA_MSG_VIDEO_FRAME = 32;
    private static final int CAMERA_MSG_ZOOM = 8;
    private static boolean HWFLOW = false;
    private static final int INT32_SIZE = 4;
    private static final int NO_ERROR = 0;
    private static final int PARAM_DEFAULT_VALUE = -1;
    private static final String PARAM_EXPOSURE_HINT = "exposure_hint";
    private static final String PARAM_EXPOSURE_TIME = "exposure_time";
    private static final String PARAM_ISO_VALUE = "iso_value";
    private static final String TAG = "Camera";
    private final String HW_FACE_BEAUTY_FOR_CHATTING;
    private final String HW_FACE_BEAUTY_OFF;
    private final String HW_FACE_BEAUTY_ON;
    private AutoFocusCallback mAutoFocusCallback;
    private final Object mAutoFocusCallbackLock;
    private AutoFocusMoveCallback mAutoFocusMoveCallback;
    private DarkRaiderCallback mDarkRaiderCallback;
    private DualCameraStateChangedCallback mDualCameraStateChangedCallback;
    private DualCameraVerificationCallback mDualCameraVerificationCallback;
    private ErrorCallback mErrorCallback;
    private EventHandler mEventHandler;
    private ExposureMeasureCallback mExposureMeasureCallback;
    private ExposurePreviewStateCallback mExposurePreviewStateCallback;
    private final Object mExposurePreviewStateCallbackLock;
    private Object mFaceDetectionCallbackLock;
    private boolean mFaceDetectionRunning;
    private FaceDetectionListener mFaceListener;
    private FovResultCallback mFovResultCallback;
    private HdcResultCallback mHdcResultCallback;
    private PictureCallback mJpegCallback;
    private Object mJpegCallbackLock;
    private LaserCalibrationCallback mLaserCalibrationCallback;
    private LcdCompensateCallback mLcdCompensateCallback;
    private LpdParametersCallback mLpdParasCallback;
    private MMITestFeatureCallback mMMITestFeatureCallback;
    private final Object mMmiFocusResultCallBackLock;
    private MmiFocusResultCallback mMmiFocusResultCallback;
    private long mNativeContext;
    private OisGryoOffseCalibResultCallback mOisGryoOffseCalibResultCallback;
    private OisHallCalibResultCallback mOisHallCalibResultCallback;
    private OisHallCheckResultCallback mOisHallCheckResultCallback;
    private boolean mOneShot;
    private PdafVerificationCallback mPdafVerificationCallback;
    private PictureCallback mPostviewCallback;
    private PreviewCallback mPreviewCallback;
    private int mPreviewExposureHint;
    private int mPreviewExposureTime;
    private int mPreviewIsoValue;
    private PreviewParametersCallback mPreviewParamsCallBack;
    private final Object mPreviewParamsCallBackLock;
    private PictureCallback mRawImageCallback;
    private ShutterCallback mShutterCallback;
    private TargetTrackingCallback mTargetTrackingCallback;
    private boolean mUsingPreviewAllocation;
    private boolean mWithBuffer;
    private OnZoomChangeListener mZoomListener;

    @Deprecated
    public static class Area {
        public Rect rect;
        public int weight;

        public Area(Rect rect, int weight) {
            this.rect = rect;
            this.weight = weight;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof Area)) {
                return false;
            }
            Area a = (Area) obj;
            if (this.rect == null) {
                if (a.rect != null) {
                    return false;
                }
            } else if (!this.rect.equals(a.rect)) {
                return false;
            }
            if (this.weight == a.weight) {
                z = true;
            }
            return z;
        }
    }

    @Deprecated
    public interface AutoFocusCallback {
        void onAutoFocus(boolean z, Camera camera);
    }

    @Deprecated
    public interface AutoFocusMoveCallback {
        void onAutoFocusMoving(boolean z, Camera camera);
    }

    @Deprecated
    public static class CameraInfo {
        public static final int CAMERA_FACING_BACK = 0;
        public static final int CAMERA_FACING_FRONT = 1;
        public boolean canDisableShutterSound;
        public int facing;
        public int orientation;
    }

    public interface DarkRaiderCallback {
        void onDarkRaiderCallback(int i, int i2, Camera camera);
    }

    public interface DualCameraStateChangedCallback {
        void onDualCameraStateChanged(int i, int i2, Camera camera);
    }

    public interface DualCameraVerificationCallback {
        void onDualCameraVerification(int i, int i2, Camera camera);
    }

    @Deprecated
    public interface ErrorCallback {
        void onError(int i, Camera camera);
    }

    private class EventHandler extends Handler {
        private final Camera mCamera;

        public EventHandler(Camera c, Looper looper) {
            super(looper);
            this.mCamera = c;
        }

        public void handleMessage(Message msg) {
            int length;
            switch (msg.what) {
                case Camera.CAMERA_MSG_LPD_PARAMS /*-2147483648*/:
                    if (Camera.this.mLpdParasCallback != null) {
                        Camera.this.mLpdParasCallback.onGetParametersByte((byte[]) msg.obj, this.mCamera);
                    }
                case Camera.CAMERA_MSG_OIS_GRYO_OFFSET_CALIB_RESULT /*1*/:
                    Log.e(Camera.TAG, "Error " + msg.arg1);
                    if (Camera.this.mErrorCallback != null) {
                        Camera.this.mErrorCallback.onError(msg.arg1, this.mCamera);
                    }
                case Camera.CAMERA_MSG_SHUTTER /*2*/:
                    if (Camera.this.mShutterCallback != null) {
                        Camera.this.mShutterCallback.onShutter();
                    }
                case Camera.INT32_SIZE /*4*/:
                    AutoFocusCallback cb;
                    synchronized (Camera.this.mAutoFocusCallbackLock) {
                        cb = Camera.this.mAutoFocusCallback;
                        break;
                    }
                    if (cb != null) {
                        cb.onAutoFocus(msg.arg1 != 0, this.mCamera);
                    }
                case Camera.CAMERA_MSG_ZOOM /*8*/:
                    if (Camera.this.mZoomListener != null) {
                        Camera.this.mZoomListener.onZoomChange(msg.arg1, msg.arg2 != 0, this.mCamera);
                    }
                case Camera.CAMERA_MSG_PREVIEW_FRAME /*16*/:
                    PreviewCallback pCb = Camera.this.mPreviewCallback;
                    if (pCb != null) {
                        if (Camera.this.mOneShot) {
                            Camera.this.mPreviewCallback = null;
                        } else {
                            if (!Camera.this.mWithBuffer) {
                                Camera.this.setHasPreviewCallback(true, false);
                            }
                        }
                        pCb.onPreviewFrame((byte[]) msg.obj, this.mCamera);
                    }
                case Camera.CAMERA_MSG_POSTVIEW_FRAME /*64*/:
                    if (Camera.this.mPostviewCallback != null) {
                        Camera.this.mPostviewCallback.onPictureTaken((byte[]) msg.obj, this.mCamera);
                    }
                case Camera.CAMERA_MSG_RAW_IMAGE /*128*/:
                    if (Camera.this.mRawImageCallback != null) {
                        Camera.this.mRawImageCallback.onPictureTaken((byte[]) msg.obj, this.mCamera);
                    }
                case Camera.CAMERA_MSG_COMPRESSED_IMAGE /*256*/:
                    PictureCallback jcb;
                    synchronized (Camera.this.mJpegCallbackLock) {
                        jcb = Camera.this.mJpegCallback;
                        break;
                    }
                    if (jcb != null) {
                        if (Camera.HWFLOW) {
                            Log.i(Camera.TAG, "handle CAMERA_MSG_COMPRESSED_IMAGE.");
                        }
                        jcb.onPictureTaken((byte[]) msg.obj, this.mCamera);
                    }
                case Camera.CAMERA_MSG_PREVIEW_METADATA /*1024*/:
                    FaceDetectionListener fdcb;
                    synchronized (Camera.this.mFaceDetectionCallbackLock) {
                        fdcb = Camera.this.mFaceListener;
                        break;
                    }
                    if (fdcb != null) {
                        fdcb.onFaceDetection((Face[]) msg.obj, this.mCamera);
                    }
                case Camera.CAMERA_MSG_FOCUS_MOVE /*2048*/:
                    if (Camera.this.mAutoFocusMoveCallback != null) {
                        Camera.this.mAutoFocusMoveCallback.onAutoFocusMoving(msg.arg1 != 0, this.mCamera);
                    }
                case Camera.CAMERA_MSG_TARGET_TRACKING /*65536*/:
                    if (Camera.this.mTargetTrackingCallback != null) {
                        Camera.this.mTargetTrackingCallback.onTargetTracking(msg.arg1, msg.arg2, this.mCamera);
                    }
                case Camera.CAMERA_MSG_LCD_COMPENSATE_LIGHT /*131072*/:
                    if (Camera.this.mLcdCompensateCallback != null) {
                        Camera.this.mLcdCompensateCallback.onLcdCompensate(msg.arg1, this.mCamera);
                    }
                case Camera.CAMERA_MSG_DUAL_CAMERA_VERIFY /*262144*/:
                    if (Camera.this.mDualCameraVerificationCallback != null) {
                        Camera.this.mDualCameraVerificationCallback.onDualCameraVerification(msg.arg1, msg.arg2, this.mCamera);
                    }
                case Camera.CAMERA_MSG_DUAL_CAMERA_SHELTER /*524288*/:
                    if (Camera.this.mDualCameraStateChangedCallback != null) {
                        Log.i(Camera.TAG, "CAMERA_MSG_DUAL_CAMERA_SHELTER: " + msg.arg1);
                        Camera.this.mDualCameraStateChangedCallback.onDualCameraStateChanged(msg.arg1, msg.arg2, this.mCamera);
                    }
                case Camera.CAMERA_MSG_EXPOSURE_MEASURE_TIME /*1048576*/:
                    if (Camera.this.mExposureMeasureCallback != null) {
                        Log.i(Camera.TAG, "CAMERA_MSG_EXPOSURE_MEASURE_TIME: " + msg.arg1);
                        Camera.this.mExposureMeasureCallback.onExposureMeasured(msg.arg1, msg.arg2, this.mCamera);
                        Camera.this.mExposureMeasureCallback = null;
                    }
                case Camera.CAMERA_MSG_EXPOSURE_PREVIEW_STATE /*2097152*/:
                    ExposurePreviewStateCallback previewStatecb;
                    synchronized (Camera.this.mExposurePreviewStateCallbackLock) {
                        previewStatecb = Camera.this.mExposurePreviewStateCallback;
                        break;
                    }
                    if (previewStatecb != null) {
                        Log.i(Camera.TAG, "CAMERA_MSG_EXPOSURE_PREVIEW_STATE: " + msg.arg1);
                        previewStatecb.onExposurePreviewState(this.mCamera);
                    }
                    Camera.this.mExposurePreviewStateCallback = null;
                case Camera.CAMERA_MSG_PDAF_VERIFY /*4194304*/:
                    if (Camera.this.mPdafVerificationCallback != null) {
                        Log.i(Camera.TAG, "CAMERA_MSG_PDAF_VERIFY: " + msg.arg1);
                        Camera.this.mPdafVerificationCallback.onPdafVerification(msg.arg1, msg.arg2, this.mCamera);
                    }
                case Camera.CAMERA_MSG_PREVIEW_PARAMS /*8388608*/:
                    synchronized (Camera.this.mPreviewParamsCallBackLock) {
                        if (Camera.this.mPreviewParamsCallBack == null) {
                            return;
                        }
                        PreviewParametersCallback previewCb = Camera.this.mPreviewParamsCallBack;
                        byte[] prevParams = (byte[]) msg.obj;
                        Bundle bndl = new Bundle();
                        length = prevParams.length;
                        if (r0 >= Camera.INT32_SIZE) {
                            String strExpoTime = Camera.this.formatExposureTime(prevParams);
                            if (strExpoTime != null) {
                                bndl.putString(Camera.PARAM_EXPOSURE_TIME, strExpoTime);
                                Log.i(Camera.TAG, "professional exposure time: " + strExpoTime);
                            }
                        }
                        length = prevParams.length;
                        if (r0 >= Camera.CAMERA_MSG_ZOOM) {
                            int isoValue = Camera.this.formatIsoValue(prevParams);
                            if (!(Camera.this.mPreviewIsoValue == isoValue || isoValue == Camera.PARAM_DEFAULT_VALUE)) {
                                bndl.putInt(Camera.PARAM_ISO_VALUE, isoValue);
                                Camera.this.mPreviewIsoValue = isoValue;
                                Log.i(Camera.TAG, "professional ISO: " + isoValue);
                            }
                        }
                        length = prevParams.length;
                        if (r0 >= 12) {
                            int expoHint = Camera.this.formatExposureHint(prevParams);
                            if (!(Camera.this.mPreviewExposureHint == expoHint || expoHint == Camera.PARAM_DEFAULT_VALUE)) {
                                bndl.putInt(Camera.PARAM_EXPOSURE_HINT, expoHint);
                                Camera.this.mPreviewExposureHint = expoHint;
                                Log.i(Camera.TAG, "professional exposure hint: " + expoHint);
                            }
                        }
                        if (!bndl.isEmpty()) {
                            previewCb.onParameterChanged(bndl);
                        }
                    }
                case Camera.CAMERA_MSG_LASER_CALIBRATION /*16777216*/:
                    if (Camera.this.mLaserCalibrationCallback != null) {
                        Camera.this.mLaserCalibrationCallback.onLaserCalibration(msg.arg1, msg.arg2, this.mCamera);
                    }
                case Camera.CAMERA_MSG_HDC_RESULT /*33554432*/:
                    if (Camera.this.mHdcResultCallback != null) {
                        Log.i(Camera.TAG, "CAMERA_MSG_HDC_RESULT: " + msg.arg1);
                        Camera.this.mHdcResultCallback.onResult(msg.arg1, msg.arg2, this.mCamera);
                    }
                case Camera.CAMERA_MSG_FOV_RESULT /*67108864*/:
                    if (Camera.this.mFovResultCallback != null) {
                        Log.i(Camera.TAG, "CAMERA_MSG_FOV_RESULT: " + msg.arg1);
                        Camera.this.mFovResultCallback.onResult(msg.arg1, msg.arg2, this.mCamera);
                    }
                case Camera.CAMERA_MSG_DARK_RAIDER /*134217728*/:
                    if (Camera.this.mDarkRaiderCallback != null) {
                        Log.i(Camera.TAG, "CAMERA_MSG_DARK_RAIDER: " + msg.arg1);
                        Camera.this.mDarkRaiderCallback.onDarkRaiderCallback(msg.arg1, msg.arg2, this.mCamera);
                    }
                case Camera.CAMERA_MSG_MMI_FOCUS_RESULT /*268435456*/:
                    synchronized (Camera.this.mMmiFocusResultCallBackLock) {
                        if (Camera.this.mMmiFocusResultCallback == null) {
                            return;
                        }
                        MmiFocusResultCallback resultCb = Camera.this.mMmiFocusResultCallback;
                        byte[] mmiFocus = (byte[]) msg.obj;
                        length = mmiFocus.length;
                        if (r0 > Camera.INT32_SIZE) {
                            int num = Camera.this.byteArrayToInt32(mmiFocus, Camera.NO_ERROR);
                            if (num > 0 && num <= 10) {
                                int[] result = new int[((num * Camera.CAMERA_MSG_SHUTTER) + Camera.CAMERA_MSG_SHUTTER)];
                                result[Camera.NO_ERROR] = num;
                                result[Camera.CAMERA_MSG_OIS_GRYO_OFFSET_CALIB_RESULT] = Camera.this.byteArrayToInt32(mmiFocus, Camera.INT32_SIZE);
                                Log.i(Camera.TAG, "CAMERA_MSG_MMI_FOCUS_RESULT, num: " + result[Camera.NO_ERROR] + " flag: " + result[Camera.CAMERA_MSG_OIS_GRYO_OFFSET_CALIB_RESULT]);
                                for (int i = Camera.NO_ERROR; i < num; i += Camera.CAMERA_MSG_OIS_GRYO_OFFSET_CALIB_RESULT) {
                                    result[(i * Camera.CAMERA_MSG_SHUTTER) + Camera.CAMERA_MSG_SHUTTER] = Camera.this.byteArrayToInt32(mmiFocus, ((i * Camera.CAMERA_MSG_SHUTTER) + Camera.CAMERA_MSG_SHUTTER) * Camera.INT32_SIZE);
                                    result[(i * Camera.CAMERA_MSG_SHUTTER) + Camera.CAMERA_MSG_OIS_HALL_CHECK_RESULT] = Camera.this.byteArrayToInt32(mmiFocus, ((i * Camera.CAMERA_MSG_SHUTTER) + Camera.CAMERA_MSG_OIS_HALL_CHECK_RESULT) * Camera.INT32_SIZE);
                                    Log.i(Camera.TAG, "CAMERA_MSG_MMI_FOCUS_RESULT, result: " + result[(i * Camera.CAMERA_MSG_SHUTTER) + Camera.CAMERA_MSG_SHUTTER] + " result: " + result[(i * Camera.CAMERA_MSG_SHUTTER) + Camera.CAMERA_MSG_OIS_HALL_CHECK_RESULT]);
                                }
                                resultCb.onResult(result);
                            }
                        }
                    }
                case Camera.CAMERA_MSG_MMI_TEST_FEATURE /*536870912*/:
                    if (Camera.this.mMMITestFeatureCallback != null) {
                        Camera.this.mMMITestFeatureCallback.onMMITestFeatureVerify(msg.arg1, msg.arg2, this.mCamera);
                    }
                case Camera.CAMERA_MSG_EXTEND /*1073741824*/:
                    int type = msg.arg1;
                    byte[] bArr = null;
                    if (type == 0) {
                        bArr = msg.obj;
                        if (bArr != null) {
                            type = Camera.this.byteArrayToInt32(bArr, Camera.NO_ERROR);
                        }
                    }
                    switch (type) {
                        case Camera.CAMERA_MSG_OIS_GRYO_OFFSET_CALIB_RESULT /*1*/:
                            if (bArr != null) {
                                int gryoOffsetCalibResult = Camera.this.byteArrayToInt32(bArr, Camera.INT32_SIZE);
                                int gryoOffset_x = Camera.this.byteArrayToInt32(bArr, Camera.CAMERA_MSG_ZOOM);
                                int gryoOffset_y = Camera.this.byteArrayToInt32(bArr, 12);
                                Log.i(Camera.TAG, "CAMERA_MSG_OIS_GRYO_OFFSET_CALIB_RESULT: x:" + gryoOffset_x + ", y:" + gryoOffset_y + ",result:" + gryoOffsetCalibResult);
                                if (Camera.this.mOisGryoOffseCalibResultCallback != null) {
                                    Camera.this.mOisGryoOffseCalibResultCallback.onResult(gryoOffset_x, gryoOffset_y, gryoOffsetCalibResult, this.mCamera);
                                }
                            }
                        case Camera.CAMERA_MSG_SHUTTER /*2*/:
                            if (bArr != null) {
                                int hallCalibResult = Camera.this.byteArrayToInt32(bArr, Camera.INT32_SIZE);
                                byte[] driverName = new byte[Camera.CAMERA_MSG_ZOOM];
                                System.arraycopy(bArr, Camera.CAMERA_MSG_ZOOM, driverName, Camera.NO_ERROR, driverName.length);
                                Log.i(Camera.TAG, "CAMERA_MSG_OIS_HALL_CALIB_RESULT: driverName:" + new String(driverName) + ", ad_offset_y:" + Camera.this.byteArrayToInt32(bArr, bArr.length - 4) + ",result:" + hallCalibResult);
                                if (Camera.this.mOisHallCalibResultCallback != null) {
                                    byte[] hallCalibData = new byte[(bArr.length - 8)];
                                    System.arraycopy(bArr, Camera.CAMERA_MSG_ZOOM, hallCalibData, Camera.NO_ERROR, hallCalibData.length);
                                    Camera.this.mOisHallCalibResultCallback.onResult(hallCalibData, hallCalibResult, this.mCamera);
                                }
                            }
                        case Camera.CAMERA_MSG_OIS_HALL_CHECK_RESULT /*3*/:
                            Log.i(Camera.TAG, "CAMERA_MSG_OIS_HALL_CHECK_RESULT: " + msg.arg2);
                            if (Camera.this.mOisHallCheckResultCallback != null) {
                                Camera.this.mOisHallCheckResultCallback.onResult(msg.arg2, this.mCamera);
                            }
                        default:
                            Log.e(Camera.TAG, "Unknown extend message type " + msg.arg1);
                    }
                default:
                    Log.e(Camera.TAG, "Unknown message type " + msg.what);
            }
        }
    }

    public interface ExposureMeasureCallback {
        void onExposureMeasured(int i, int i2, Camera camera);
    }

    public interface ExposurePreviewStateCallback {
        void onExposurePreviewState(Camera camera);
    }

    @Deprecated
    public static class Face {
        public int id;
        public Point leftEye;
        public Point mouth;
        public Rect rect;
        public Point rightEye;
        public int score;

        public Face() {
            this.id = Camera.PARAM_DEFAULT_VALUE;
            this.leftEye = null;
            this.rightEye = null;
            this.mouth = null;
        }
    }

    @Deprecated
    public interface FaceDetectionListener {
        void onFaceDetection(Face[] faceArr, Camera camera);
    }

    public interface FovResultCallback {
        void onResult(int i, int i2, Camera camera);
    }

    public interface HdcResultCallback {
        void onResult(int i, int i2, Camera camera);
    }

    public interface LaserCalibrationCallback {
        void onLaserCalibration(int i, int i2, Camera camera);
    }

    public interface LcdCompensateCallback {
        void onLcdCompensate(int i, Camera camera);
    }

    public interface LpdParametersCallback {
        void onGetParametersByte(byte[] bArr, Camera camera);
    }

    public interface MMITestFeatureCallback {
        void onMMITestFeatureVerify(int i, int i2, Camera camera);
    }

    public interface MmiFocusResultCallback {
        void onResult(int[] iArr);
    }

    public interface OisGryoOffseCalibResultCallback {
        void onResult(int i, int i2, int i3, Camera camera);
    }

    public interface OisHallCalibResultCallback {
        void onResult(byte[] bArr, int i, Camera camera);
    }

    public interface OisHallCheckResultCallback {
        void onResult(int i, Camera camera);
    }

    @Deprecated
    public interface OnZoomChangeListener {
        void onZoomChange(int i, boolean z, Camera camera);
    }

    @Deprecated
    public class Parameters {
        public static final String ANTIBANDING_50HZ = "50hz";
        public static final String ANTIBANDING_60HZ = "60hz";
        public static final String ANTIBANDING_AUTO = "auto";
        public static final String ANTIBANDING_OFF = "off";
        public static final String EFFECT_AQUA = "aqua";
        public static final String EFFECT_BLACKBOARD = "blackboard";
        public static final String EFFECT_MONO = "mono";
        public static final String EFFECT_NEGATIVE = "negative";
        public static final String EFFECT_NONE = "none";
        public static final String EFFECT_POSTERIZE = "posterize";
        public static final String EFFECT_SEPIA = "sepia";
        public static final String EFFECT_SOLARIZE = "solarize";
        public static final String EFFECT_WHITEBOARD = "whiteboard";
        private static final String FALSE = "false";
        public static final String FLASH_MODE_AUTO = "auto";
        public static final String FLASH_MODE_OFF = "off";
        public static final String FLASH_MODE_ON = "on";
        public static final String FLASH_MODE_RED_EYE = "red-eye";
        public static final String FLASH_MODE_TORCH = "torch";
        public static final int FOCUS_DISTANCE_FAR_INDEX = 2;
        public static final int FOCUS_DISTANCE_NEAR_INDEX = 0;
        public static final int FOCUS_DISTANCE_OPTIMAL_INDEX = 1;
        public static final String FOCUS_MODE_AUTO = "auto";
        public static final String FOCUS_MODE_CONTINUOUS_PICTURE = "continuous-picture";
        public static final String FOCUS_MODE_CONTINUOUS_VIDEO = "continuous-video";
        public static final String FOCUS_MODE_EDOF = "edof";
        public static final String FOCUS_MODE_FIXED = "fixed";
        public static final String FOCUS_MODE_INFINITY = "infinity";
        public static final String FOCUS_MODE_MACRO = "macro";
        private static final String KEY_ANTIBANDING = "antibanding";
        private static final String KEY_AUTO_EXPOSURE_LOCK = "auto-exposure-lock";
        private static final String KEY_AUTO_EXPOSURE_LOCK_SUPPORTED = "auto-exposure-lock-supported";
        private static final String KEY_AUTO_WHITEBALANCE_LOCK = "auto-whitebalance-lock";
        private static final String KEY_AUTO_WHITEBALANCE_LOCK_SUPPORTED = "auto-whitebalance-lock-supported";
        private static final String KEY_DUAL_CAMERA_HDC_CALIB = "key-dual-camera-hdc-calib";
        private static final String KEY_DUAL_CAMERA_VERIFY = "key-dual-camera-verify";
        private static final String KEY_EFFECT = "effect";
        private static final String KEY_EXPOSURE_COMPENSATION = "exposure-compensation";
        private static final String KEY_EXPOSURE_COMPENSATION_STEP = "exposure-compensation-step";
        private static final String KEY_FLASH_MODE = "flash-mode";
        private static final String KEY_FOCAL_LENGTH = "focal-length";
        private static final String KEY_FOCUS_AREAS = "focus-areas";
        private static final String KEY_FOCUS_DISTANCES = "focus-distances";
        private static final String KEY_FOCUS_MODE = "focus-mode";
        private static final String KEY_GPS_ALTITUDE = "gps-altitude";
        private static final String KEY_GPS_LATITUDE = "gps-latitude";
        private static final String KEY_GPS_LONGITUDE = "gps-longitude";
        private static final String KEY_GPS_PROCESSING_METHOD = "gps-processing-method";
        private static final String KEY_GPS_TIMESTAMP = "gps-timestamp";
        private static final String KEY_HORIZONTAL_VIEW_ANGLE = "horizontal-view-angle";
        private static final String KEY_JPEG_QUALITY = "jpeg-quality";
        private static final String KEY_JPEG_THUMBNAIL_HEIGHT = "jpeg-thumbnail-height";
        private static final String KEY_JPEG_THUMBNAIL_QUALITY = "jpeg-thumbnail-quality";
        private static final String KEY_JPEG_THUMBNAIL_SIZE = "jpeg-thumbnail-size";
        private static final String KEY_JPEG_THUMBNAIL_WIDTH = "jpeg-thumbnail-width";
        private static final String KEY_LASER_CALIBRATION = "key-laser-calibration";
        private static final String KEY_MAX_EXPOSURE_COMPENSATION = "max-exposure-compensation";
        private static final String KEY_MAX_NUM_DETECTED_FACES_HW = "max-num-detected-faces-hw";
        private static final String KEY_MAX_NUM_DETECTED_FACES_SW = "max-num-detected-faces-sw";
        private static final String KEY_MAX_NUM_FOCUS_AREAS = "max-num-focus-areas";
        private static final String KEY_MAX_NUM_METERING_AREAS = "max-num-metering-areas";
        private static final String KEY_MAX_ZOOM = "max-zoom";
        private static final String KEY_METERING_AREAS = "metering-areas";
        private static final String KEY_MIN_EXPOSURE_COMPENSATION = "min-exposure-compensation";
        private static final String KEY_PDAF_VERIFY = "key-pdaf-verify";
        private static final String KEY_PICTURE_FORMAT = "picture-format";
        private static final String KEY_PICTURE_SIZE = "picture-size";
        private static final String KEY_PREFERRED_PREVIEW_SIZE_FOR_VIDEO = "preferred-preview-size-for-video";
        private static final String KEY_PREVIEW_FORMAT = "preview-format";
        private static final String KEY_PREVIEW_FPS_RANGE = "preview-fps-range";
        private static final String KEY_PREVIEW_FRAME_RATE = "preview-frame-rate";
        private static final String KEY_PREVIEW_SIZE = "preview-size";
        private static final String KEY_RECORDING_HINT = "recording-hint";
        private static final String KEY_ROTATION = "rotation";
        private static final String KEY_SCENE_MODE = "scene-mode";
        private static final String KEY_SMOOTH_ZOOM_SUPPORTED = "smooth-zoom-supported";
        private static final String KEY_VERTICAL_VIEW_ANGLE = "vertical-view-angle";
        private static final String KEY_VIDEO_SIZE = "video-size";
        private static final String KEY_VIDEO_SNAPSHOT_SUPPORTED = "video-snapshot-supported";
        private static final String KEY_VIDEO_STABILIZATION = "video-stabilization";
        private static final String KEY_VIDEO_STABILIZATION_SUPPORTED = "video-stabilization-supported";
        private static final String KEY_WHITE_BALANCE = "whitebalance";
        private static final String KEY_ZOOM = "zoom";
        private static final String KEY_ZOOM_RATIOS = "zoom-ratios";
        private static final String KEY_ZOOM_SUPPORTED = "zoom-supported";
        private static final String PIXEL_FORMAT_BAYER_RGGB = "bayer-rggb";
        private static final String PIXEL_FORMAT_JPEG = "jpeg";
        private static final String PIXEL_FORMAT_RGB565 = "rgb565";
        private static final String PIXEL_FORMAT_YUV420P = "yuv420p";
        private static final String PIXEL_FORMAT_YUV420SP = "yuv420sp";
        private static final String PIXEL_FORMAT_YUV422I = "yuv422i-yuyv";
        private static final String PIXEL_FORMAT_YUV422SP = "yuv422sp";
        public static final int PREVIEW_FPS_MAX_INDEX = 1;
        public static final int PREVIEW_FPS_MIN_INDEX = 0;
        public static final String SCENE_MODE_ACTION = "action";
        public static final String SCENE_MODE_AUTO = "auto";
        public static final String SCENE_MODE_BARCODE = "barcode";
        public static final String SCENE_MODE_BEACH = "beach";
        public static final String SCENE_MODE_CANDLELIGHT = "candlelight";
        public static final String SCENE_MODE_FIREWORKS = "fireworks";
        public static final String SCENE_MODE_HDR = "hdr";
        public static final String SCENE_MODE_LANDSCAPE = "landscape";
        public static final String SCENE_MODE_NIGHT = "night";
        public static final String SCENE_MODE_NIGHT_PORTRAIT = "night-portrait";
        public static final String SCENE_MODE_PARTY = "party";
        public static final String SCENE_MODE_PORTRAIT = "portrait";
        public static final String SCENE_MODE_SNOW = "snow";
        public static final String SCENE_MODE_SPORTS = "sports";
        public static final String SCENE_MODE_STEADYPHOTO = "steadyphoto";
        public static final String SCENE_MODE_SUNSET = "sunset";
        public static final String SCENE_MODE_THEATRE = "theatre";
        private static final String SUPPORTED_VALUES_SUFFIX = "-values";
        private static final String TRUE = "true";
        public static final String WHITE_BALANCE_AUTO = "auto";
        public static final String WHITE_BALANCE_CLOUDY_DAYLIGHT = "cloudy-daylight";
        public static final String WHITE_BALANCE_DAYLIGHT = "daylight";
        public static final String WHITE_BALANCE_FLUORESCENT = "fluorescent";
        public static final String WHITE_BALANCE_INCANDESCENT = "incandescent";
        public static final String WHITE_BALANCE_SHADE = "shade";
        public static final String WHITE_BALANCE_TWILIGHT = "twilight";
        public static final String WHITE_BALANCE_WARM_FLUORESCENT = "warm-fluorescent";
        private final LinkedHashMap<String, String> mMap;

        private Parameters() {
            this.mMap = new LinkedHashMap(Camera.CAMERA_MSG_POSTVIEW_FRAME);
        }

        public void copyFrom(Parameters other) {
            if (other == null) {
                throw new NullPointerException("other must not be null");
            }
            this.mMap.putAll(other.mMap);
        }

        private Camera getOuter() {
            return Camera.this;
        }

        public boolean same(Parameters other) {
            if (this == other) {
                return true;
            }
            return other != null ? this.mMap.equals(other.mMap) : false;
        }

        @Deprecated
        public void dump() {
            Log.e(Camera.TAG, "dump: size=" + this.mMap.size());
            for (String k : this.mMap.keySet()) {
                Log.e(Camera.TAG, "dump: " + k + "=" + ((String) this.mMap.get(k)));
            }
        }

        public String flatten() {
            StringBuilder flattened = new StringBuilder(Camera.CAMERA_MSG_RAW_IMAGE);
            for (String k : this.mMap.keySet()) {
                flattened.append(k);
                flattened.append("=");
                flattened.append((String) this.mMap.get(k));
                flattened.append(";");
            }
            flattened.deleteCharAt(flattened.length() + Camera.PARAM_DEFAULT_VALUE);
            return flattened.toString();
        }

        public void unflatten(String flattened) {
            this.mMap.clear();
            StringSplitter<String> splitter = new SimpleStringSplitter(';');
            splitter.setString(flattened);
            for (String kv : splitter) {
                int pos = kv.indexOf(61);
                if (pos != Camera.PARAM_DEFAULT_VALUE) {
                    this.mMap.put(kv.substring(PREVIEW_FPS_MIN_INDEX, pos), kv.substring(pos + PREVIEW_FPS_MAX_INDEX));
                }
            }
        }

        public void remove(String key) {
            this.mMap.remove(key);
        }

        public void set(String key, String value) {
            if (key.indexOf(61) != Camera.PARAM_DEFAULT_VALUE || key.indexOf(59) != Camera.PARAM_DEFAULT_VALUE || key.indexOf(PREVIEW_FPS_MIN_INDEX) != Camera.PARAM_DEFAULT_VALUE) {
                Log.e(Camera.TAG, "Key \"" + key + "\" contains invalid character (= or ; or \\0)");
            } else if (value.indexOf(61) == Camera.PARAM_DEFAULT_VALUE && value.indexOf(59) == Camera.PARAM_DEFAULT_VALUE && value.indexOf(PREVIEW_FPS_MIN_INDEX) == Camera.PARAM_DEFAULT_VALUE) {
                put(key, value);
            } else {
                Log.e(Camera.TAG, "Value \"" + value + "\" contains invalid character (= or ; or \\0)");
            }
        }

        public void set(String key, int value) {
            put(key, Integer.toString(value));
        }

        private void put(String key, String value) {
            this.mMap.remove(key);
            this.mMap.put(key, value);
        }

        private void set(String key, List<Area> areas) {
            if (areas == null) {
                set(key, "(0,0,0,0,0)");
                return;
            }
            StringBuilder buffer = new StringBuilder();
            for (int i = PREVIEW_FPS_MIN_INDEX; i < areas.size(); i += PREVIEW_FPS_MAX_INDEX) {
                Area area = (Area) areas.get(i);
                Rect rect = area.rect;
                buffer.append('(');
                buffer.append(rect.left);
                buffer.append(',');
                buffer.append(rect.top);
                buffer.append(',');
                buffer.append(rect.right);
                buffer.append(',');
                buffer.append(rect.bottom);
                buffer.append(',');
                buffer.append(area.weight);
                buffer.append(')');
                if (i != areas.size() + Camera.PARAM_DEFAULT_VALUE) {
                    buffer.append(',');
                }
            }
            set(key, buffer.toString());
        }

        public String get(String key) {
            return (String) this.mMap.get(key);
        }

        public int getInt(String key) {
            return Integer.parseInt((String) this.mMap.get(key));
        }

        public void setPreviewSize(int width, int height) {
            set(KEY_PREVIEW_SIZE, Integer.toString(width) + "x" + Integer.toString(height));
        }

        public Size getPreviewSize() {
            return strToSize(get(KEY_PREVIEW_SIZE));
        }

        public List<Size> getSupportedPreviewSizes() {
            return splitSize(get("preview-size-values"));
        }

        public List<Size> getSupportedVideoSizes() {
            return splitSize(get("video-size-values"));
        }

        public Size getPreferredPreviewSizeForVideo() {
            return strToSize(get(KEY_PREFERRED_PREVIEW_SIZE_FOR_VIDEO));
        }

        public void setJpegThumbnailSize(int width, int height) {
            set(KEY_JPEG_THUMBNAIL_WIDTH, width);
            set(KEY_JPEG_THUMBNAIL_HEIGHT, height);
        }

        public Size getJpegThumbnailSize() {
            return new Size(getInt(KEY_JPEG_THUMBNAIL_WIDTH), getInt(KEY_JPEG_THUMBNAIL_HEIGHT));
        }

        public List<Size> getSupportedJpegThumbnailSizes() {
            return splitSize(get("jpeg-thumbnail-size-values"));
        }

        public void setJpegThumbnailQuality(int quality) {
            set(KEY_JPEG_THUMBNAIL_QUALITY, quality);
        }

        public int getJpegThumbnailQuality() {
            return getInt(KEY_JPEG_THUMBNAIL_QUALITY);
        }

        public void setJpegQuality(int quality) {
            set(KEY_JPEG_QUALITY, quality);
        }

        public int getJpegQuality() {
            return getInt(KEY_JPEG_QUALITY);
        }

        @Deprecated
        public void setPreviewFrameRate(int fps) {
            set(KEY_PREVIEW_FRAME_RATE, fps);
        }

        @Deprecated
        public int getPreviewFrameRate() {
            return getInt(KEY_PREVIEW_FRAME_RATE);
        }

        @Deprecated
        public List<Integer> getSupportedPreviewFrameRates() {
            return splitInt(get("preview-frame-rate-values"));
        }

        public void setPreviewFpsRange(int min, int max) {
            set(KEY_PREVIEW_FPS_RANGE, ProxyInfo.LOCAL_EXCL_LIST + min + "," + max);
        }

        public void getPreviewFpsRange(int[] range) {
            if (range == null || range.length != FOCUS_DISTANCE_FAR_INDEX) {
                throw new IllegalArgumentException("range must be an array with two elements.");
            }
            splitInt(get(KEY_PREVIEW_FPS_RANGE), range);
        }

        public List<int[]> getSupportedPreviewFpsRange() {
            return splitRange(get("preview-fps-range-values"));
        }

        public void setPreviewFormat(int pixel_format) {
            String s = cameraFormatForPixelFormat(pixel_format);
            if (s == null) {
                throw new IllegalArgumentException("Invalid pixel_format=" + pixel_format);
            }
            set(KEY_PREVIEW_FORMAT, s);
        }

        public int getPreviewFormat() {
            return pixelFormatForCameraFormat(get(KEY_PREVIEW_FORMAT));
        }

        public List<Integer> getSupportedPreviewFormats() {
            String str = get("preview-format-values");
            ArrayList<Integer> formats = new ArrayList();
            for (String s : split(str)) {
                int f = pixelFormatForCameraFormat(s);
                if (f != 0) {
                    formats.add(Integer.valueOf(f));
                }
            }
            return formats;
        }

        public void setPictureSize(int width, int height) {
            set(KEY_PICTURE_SIZE, Integer.toString(width) + "x" + Integer.toString(height));
        }

        public Size getPictureSize() {
            return strToSize(get(KEY_PICTURE_SIZE));
        }

        public List<Size> getSupportedPictureSizes() {
            return splitSize(get("picture-size-values"));
        }

        public void setPictureFormat(int pixel_format) {
            String s = cameraFormatForPixelFormat(pixel_format);
            if (s == null) {
                throw new IllegalArgumentException("Invalid pixel_format=" + pixel_format);
            }
            set(KEY_PICTURE_FORMAT, s);
        }

        public int getPictureFormat() {
            return pixelFormatForCameraFormat(get(KEY_PICTURE_FORMAT));
        }

        public List<Integer> getSupportedPictureFormats() {
            String str = get("picture-format-values");
            ArrayList<Integer> formats = new ArrayList();
            for (String s : split(str)) {
                int f = pixelFormatForCameraFormat(s);
                if (f != 0) {
                    formats.add(Integer.valueOf(f));
                }
            }
            return formats;
        }

        private String cameraFormatForPixelFormat(int pixel_format) {
            switch (pixel_format) {
                case Camera.INT32_SIZE /*4*/:
                    return PIXEL_FORMAT_RGB565;
                case Camera.CAMERA_MSG_PREVIEW_FRAME /*16*/:
                    return PIXEL_FORMAT_YUV422SP;
                case HwSysResource.CURSOR /*17*/:
                    return PIXEL_FORMAT_YUV420SP;
                case HwSysResource.MEMORY /*20*/:
                    return PIXEL_FORMAT_YUV422I;
                case Camera.CAMERA_MSG_COMPRESSED_IMAGE /*256*/:
                    return PIXEL_FORMAT_JPEG;
                case ImageFormat.YV12 /*842094169*/:
                    return PIXEL_FORMAT_YUV420P;
                default:
                    return null;
            }
        }

        private int pixelFormatForCameraFormat(String format) {
            if (format == null) {
                return PREVIEW_FPS_MIN_INDEX;
            }
            if (format.equals(PIXEL_FORMAT_YUV422SP)) {
                return Camera.CAMERA_MSG_PREVIEW_FRAME;
            }
            if (format.equals(PIXEL_FORMAT_YUV420SP)) {
                return 17;
            }
            if (format.equals(PIXEL_FORMAT_YUV422I)) {
                return 20;
            }
            if (format.equals(PIXEL_FORMAT_YUV420P)) {
                return ImageFormat.YV12;
            }
            if (format.equals(PIXEL_FORMAT_RGB565)) {
                return Camera.INT32_SIZE;
            }
            if (format.equals(PIXEL_FORMAT_JPEG)) {
                return Camera.CAMERA_MSG_COMPRESSED_IMAGE;
            }
            return PREVIEW_FPS_MIN_INDEX;
        }

        public void setRotation(int rotation) {
            if (rotation == 0 || rotation == 90 || rotation == BluetoothAssignedNumbers.BDE_TECHNOLOGY || rotation == AnqpInformationElement.ANQP_TDLS_CAP) {
                set(KEY_ROTATION, Integer.toString(rotation));
                return;
            }
            throw new IllegalArgumentException("Invalid rotation=" + rotation);
        }

        public void setGpsLatitude(double latitude) {
            set(KEY_GPS_LATITUDE, Double.toString(latitude));
        }

        public void setGpsLongitude(double longitude) {
            set(KEY_GPS_LONGITUDE, Double.toString(longitude));
        }

        public void setGpsAltitude(double altitude) {
            set(KEY_GPS_ALTITUDE, Double.toString(altitude));
        }

        public void setGpsTimestamp(long timestamp) {
            set(KEY_GPS_TIMESTAMP, Long.toString(timestamp));
        }

        public void setGpsProcessingMethod(String processing_method) {
            set(KEY_GPS_PROCESSING_METHOD, processing_method);
        }

        public void removeGpsData() {
            remove(KEY_GPS_LATITUDE);
            remove(KEY_GPS_LONGITUDE);
            remove(KEY_GPS_ALTITUDE);
            remove(KEY_GPS_TIMESTAMP);
            remove(KEY_GPS_PROCESSING_METHOD);
        }

        public String getWhiteBalance() {
            return get(KEY_WHITE_BALANCE);
        }

        public void setWhiteBalance(String value) {
            if (!same(value, get(KEY_WHITE_BALANCE))) {
                set(KEY_WHITE_BALANCE, value);
                set(KEY_AUTO_WHITEBALANCE_LOCK, FALSE);
            }
        }

        public List<String> getSupportedWhiteBalance() {
            return split(get("whitebalance-values"));
        }

        public String getColorEffect() {
            return get(KEY_EFFECT);
        }

        public void setColorEffect(String value) {
            set(KEY_EFFECT, value);
        }

        public List<String> getSupportedColorEffects() {
            return split(get("effect-values"));
        }

        public String getAntibanding() {
            return get(KEY_ANTIBANDING);
        }

        public void setAntibanding(String antibanding) {
            set(KEY_ANTIBANDING, antibanding);
        }

        public List<String> getSupportedAntibanding() {
            return split(get("antibanding-values"));
        }

        public String getSceneMode() {
            return get(KEY_SCENE_MODE);
        }

        public void setSceneMode(String value) {
            set(KEY_SCENE_MODE, value);
        }

        public List<String> getSupportedSceneModes() {
            return split(get("scene-mode-values"));
        }

        public String getFlashMode() {
            return get(KEY_FLASH_MODE);
        }

        public void setFlashMode(String value) {
            set(KEY_FLASH_MODE, value);
        }

        public List<String> getSupportedFlashModes() {
            return split(get("flash-mode-values"));
        }

        public String getFocusMode() {
            return get(KEY_FOCUS_MODE);
        }

        public void setFocusMode(String value) {
            set(KEY_FOCUS_MODE, value);
        }

        public List<String> getSupportedFocusModes() {
            return split(get("focus-mode-values"));
        }

        public float getFocalLength() {
            return Float.parseFloat(get(KEY_FOCAL_LENGTH));
        }

        public float getHorizontalViewAngle() {
            return Float.parseFloat(get(KEY_HORIZONTAL_VIEW_ANGLE));
        }

        public float getVerticalViewAngle() {
            return Float.parseFloat(get(KEY_VERTICAL_VIEW_ANGLE));
        }

        public int getExposureCompensation() {
            return getInt(KEY_EXPOSURE_COMPENSATION, PREVIEW_FPS_MIN_INDEX);
        }

        public void setExposureCompensation(int value) {
            set(KEY_EXPOSURE_COMPENSATION, value);
        }

        public int getMaxExposureCompensation() {
            return getInt(KEY_MAX_EXPOSURE_COMPENSATION, PREVIEW_FPS_MIN_INDEX);
        }

        public int getMinExposureCompensation() {
            return getInt(KEY_MIN_EXPOSURE_COMPENSATION, PREVIEW_FPS_MIN_INDEX);
        }

        public float getExposureCompensationStep() {
            return getFloat(KEY_EXPOSURE_COMPENSATION_STEP, 0.0f);
        }

        public void setAutoExposureLock(boolean toggle) {
            set(KEY_AUTO_EXPOSURE_LOCK, toggle ? TRUE : FALSE);
        }

        public boolean getAutoExposureLock() {
            return TRUE.equals(get(KEY_AUTO_EXPOSURE_LOCK));
        }

        public boolean isAutoExposureLockSupported() {
            return TRUE.equals(get(KEY_AUTO_EXPOSURE_LOCK_SUPPORTED));
        }

        public void setAutoWhiteBalanceLock(boolean toggle) {
            set(KEY_AUTO_WHITEBALANCE_LOCK, toggle ? TRUE : FALSE);
        }

        public boolean getAutoWhiteBalanceLock() {
            return TRUE.equals(get(KEY_AUTO_WHITEBALANCE_LOCK));
        }

        public boolean isAutoWhiteBalanceLockSupported() {
            return TRUE.equals(get(KEY_AUTO_WHITEBALANCE_LOCK_SUPPORTED));
        }

        public int getZoom() {
            return getInt(KEY_ZOOM, PREVIEW_FPS_MIN_INDEX);
        }

        public void setZoom(int value) {
            set(KEY_ZOOM, value);
        }

        public boolean isZoomSupported() {
            return TRUE.equals(get(KEY_ZOOM_SUPPORTED));
        }

        public int getMaxZoom() {
            return getInt(KEY_MAX_ZOOM, PREVIEW_FPS_MIN_INDEX);
        }

        public List<Integer> getZoomRatios() {
            return splitInt(get(KEY_ZOOM_RATIOS));
        }

        public boolean isSmoothZoomSupported() {
            return TRUE.equals(get(KEY_SMOOTH_ZOOM_SUPPORTED));
        }

        public void getFocusDistances(float[] output) {
            if (output == null || output.length != Camera.CAMERA_MSG_OIS_HALL_CHECK_RESULT) {
                throw new IllegalArgumentException("output must be a float array with three elements.");
            }
            splitFloat(get(KEY_FOCUS_DISTANCES), output);
        }

        public int getMaxNumFocusAreas() {
            return getInt(KEY_MAX_NUM_FOCUS_AREAS, PREVIEW_FPS_MIN_INDEX);
        }

        public List<Area> getFocusAreas() {
            return splitArea(get(KEY_FOCUS_AREAS));
        }

        public void setFocusAreas(List<Area> focusAreas) {
            set(KEY_FOCUS_AREAS, (List) focusAreas);
        }

        public int getMaxNumMeteringAreas() {
            return getInt(KEY_MAX_NUM_METERING_AREAS, PREVIEW_FPS_MIN_INDEX);
        }

        public List<Area> getMeteringAreas() {
            return splitArea(get(KEY_METERING_AREAS));
        }

        public void setMeteringAreas(List<Area> meteringAreas) {
            set(KEY_METERING_AREAS, (List) meteringAreas);
        }

        public int getMaxNumDetectedFaces() {
            return getInt(KEY_MAX_NUM_DETECTED_FACES_HW, PREVIEW_FPS_MIN_INDEX);
        }

        public void setRecordingHint(boolean hint) {
            set(KEY_RECORDING_HINT, hint ? TRUE : FALSE);
        }

        public boolean isVideoSnapshotSupported() {
            return TRUE.equals(get(KEY_VIDEO_SNAPSHOT_SUPPORTED));
        }

        public void setVideoStabilization(boolean toggle) {
            set(KEY_VIDEO_STABILIZATION, toggle ? TRUE : FALSE);
        }

        public boolean getVideoStabilization() {
            return TRUE.equals(get(KEY_VIDEO_STABILIZATION));
        }

        public boolean isVideoStabilizationSupported() {
            return TRUE.equals(get(KEY_VIDEO_STABILIZATION_SUPPORTED));
        }

        private ArrayList<String> split(String str) {
            if (str == null) {
                return null;
            }
            StringSplitter<String> splitter = new SimpleStringSplitter(',');
            splitter.setString(str);
            ArrayList<String> substrings = new ArrayList();
            for (String s : splitter) {
                substrings.add(s);
            }
            return substrings;
        }

        private ArrayList<Integer> splitInt(String str) {
            if (str == null) {
                return null;
            }
            StringSplitter<String> splitter = new SimpleStringSplitter(',');
            splitter.setString(str);
            ArrayList<Integer> substrings = new ArrayList();
            for (String s : splitter) {
                substrings.add(Integer.valueOf(Integer.parseInt(s)));
            }
            if (substrings.size() == 0) {
                return null;
            }
            return substrings;
        }

        private void splitInt(String str, int[] output) {
            if (str != null) {
                StringSplitter<String> splitter = new SimpleStringSplitter(',');
                splitter.setString(str);
                int index = PREVIEW_FPS_MIN_INDEX;
                for (String s : splitter) {
                    int index2 = index + PREVIEW_FPS_MAX_INDEX;
                    output[index] = Integer.parseInt(s);
                    index = index2;
                }
            }
        }

        private void splitFloat(String str, float[] output) {
            if (str != null) {
                StringSplitter<String> splitter = new SimpleStringSplitter(',');
                splitter.setString(str);
                int index = PREVIEW_FPS_MIN_INDEX;
                for (String s : splitter) {
                    int index2 = index + PREVIEW_FPS_MAX_INDEX;
                    output[index] = Float.parseFloat(s);
                    index = index2;
                }
            }
        }

        private float getFloat(String key, float defaultValue) {
            try {
                return Float.parseFloat((String) this.mMap.get(key));
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        private int getInt(String key, int defaultValue) {
            try {
                return Integer.parseInt((String) this.mMap.get(key));
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        private ArrayList<Size> splitSize(String str) {
            if (str == null) {
                return null;
            }
            StringSplitter<String> splitter = new SimpleStringSplitter(',');
            splitter.setString(str);
            ArrayList<Size> sizeList = new ArrayList();
            for (String s : splitter) {
                Size size = strToSize(s);
                if (size != null) {
                    sizeList.add(size);
                }
            }
            if (sizeList.size() == 0) {
                return null;
            }
            return sizeList;
        }

        private Size strToSize(String str) {
            if (str == null) {
                return null;
            }
            int pos = str.indexOf(BluetoothAssignedNumbers.NIKE);
            if (pos != Camera.PARAM_DEFAULT_VALUE) {
                return new Size(Integer.parseInt(str.substring(PREVIEW_FPS_MIN_INDEX, pos)), Integer.parseInt(str.substring(pos + PREVIEW_FPS_MAX_INDEX)));
            }
            Log.e(Camera.TAG, "Invalid size parameter string=" + str);
            return null;
        }

        private ArrayList<int[]> splitRange(String str) {
            if (str != null && str.charAt(PREVIEW_FPS_MIN_INDEX) == '(' && str.charAt(str.length() + Camera.PARAM_DEFAULT_VALUE) == ')') {
                ArrayList<int[]> rangeList = new ArrayList();
                int fromIndex = PREVIEW_FPS_MAX_INDEX;
                int endIndex;
                do {
                    int[] range = new int[FOCUS_DISTANCE_FAR_INDEX];
                    endIndex = str.indexOf("),(", fromIndex);
                    if (endIndex == Camera.PARAM_DEFAULT_VALUE) {
                        endIndex = str.length() + Camera.PARAM_DEFAULT_VALUE;
                    }
                    splitInt(str.substring(fromIndex, endIndex), range);
                    rangeList.add(range);
                    fromIndex = endIndex + Camera.CAMERA_MSG_OIS_HALL_CHECK_RESULT;
                } while (endIndex != str.length() + Camera.PARAM_DEFAULT_VALUE);
                if (rangeList.size() == 0) {
                    return null;
                }
                return rangeList;
            }
            Log.e(Camera.TAG, "Invalid range list string=" + str);
            return null;
        }

        private ArrayList<Area> splitArea(String str) {
            if (str != null && str.charAt(PREVIEW_FPS_MIN_INDEX) == '(' && str.charAt(str.length() + Camera.PARAM_DEFAULT_VALUE) == ')') {
                ArrayList<Area> result = new ArrayList();
                int fromIndex = PREVIEW_FPS_MAX_INDEX;
                int[] array = new int[5];
                int endIndex;
                do {
                    endIndex = str.indexOf("),(", fromIndex);
                    if (endIndex == Camera.PARAM_DEFAULT_VALUE) {
                        endIndex = str.length() + Camera.PARAM_DEFAULT_VALUE;
                    }
                    splitInt(str.substring(fromIndex, endIndex), array);
                    result.add(new Area(new Rect(array[PREVIEW_FPS_MIN_INDEX], array[PREVIEW_FPS_MAX_INDEX], array[FOCUS_DISTANCE_FAR_INDEX], array[Camera.CAMERA_MSG_OIS_HALL_CHECK_RESULT]), array[Camera.INT32_SIZE]));
                    fromIndex = endIndex + Camera.CAMERA_MSG_OIS_HALL_CHECK_RESULT;
                } while (endIndex != str.length() + Camera.PARAM_DEFAULT_VALUE);
                if (result.size() == 0) {
                    return null;
                }
                if (result.size() == PREVIEW_FPS_MAX_INDEX) {
                    Area area = (Area) result.get(PREVIEW_FPS_MIN_INDEX);
                    Rect rect = area.rect;
                    if (rect.left == 0 && rect.top == 0 && rect.right == 0 && rect.bottom == 0 && area.weight == 0) {
                        return null;
                    }
                    return result;
                }
                return result;
            }
            Log.e(Camera.TAG, "Invalid area string=" + str);
            return null;
        }

        private boolean same(String s1, String s2) {
            if (s1 == null && s2 == null) {
                return true;
            }
            if (s1 == null || !s1.equals(s2)) {
                return false;
            }
            return true;
        }

        public void startDualCameraVerification() {
            Parameters param = Camera.this.getParameters();
            param.set(KEY_DUAL_CAMERA_VERIFY, FLASH_MODE_ON);
            Camera.this.setParameters(param);
            Log.i(Camera.TAG, "startDualCameraVerification()");
        }

        public void stopDualCameraVerification() {
            Parameters param = Camera.this.getParameters();
            param.set(KEY_DUAL_CAMERA_VERIFY, FLASH_MODE_OFF);
            Camera.this.setParameters(param);
            Log.i(Camera.TAG, "stopDualCameraVerification()");
        }

        public void startPdafVerification() {
            Parameters param = Camera.this.getParameters();
            param.set(KEY_PDAF_VERIFY, FLASH_MODE_ON);
            Camera.this.setParameters(param);
            Log.i(Camera.TAG, "startPdafVerification()");
        }

        public void stopPdafVerification() {
            Parameters param = Camera.this.getParameters();
            param.set(KEY_PDAF_VERIFY, FLASH_MODE_OFF);
            Camera.this.setParameters(param);
            Log.i(Camera.TAG, "stopPdafVerification()");
        }

        public void startHdcCalib() {
            Parameters param = Camera.this.getParameters();
            param.set(KEY_DUAL_CAMERA_HDC_CALIB, FLASH_MODE_ON);
            Camera.this.setParameters(param);
            Log.i(Camera.TAG, "startHdcCalib()");
        }

        public void stopHdcCalib() {
            Parameters param = Camera.this.getParameters();
            param.set(KEY_DUAL_CAMERA_HDC_CALIB, FLASH_MODE_OFF);
            Camera.this.setParameters(param);
            Log.i(Camera.TAG, "stopHdcCalib()");
        }
    }

    public interface PdafVerificationCallback {
        void onPdafVerification(int i, int i2, Camera camera);
    }

    @Deprecated
    public interface PictureCallback {
        void onPictureTaken(byte[] bArr, Camera camera);
    }

    @Deprecated
    public interface PreviewCallback {
        void onPreviewFrame(byte[] bArr, Camera camera);
    }

    public interface PreviewParametersCallback {
        void onParameterChanged(Bundle bundle);
    }

    @Deprecated
    public interface ShutterCallback {
        void onShutter();
    }

    @Deprecated
    public class Size {
        public int height;
        public int width;

        public Size(int w, int h) {
            this.width = w;
            this.height = h;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof Size)) {
                return false;
            }
            Size s = (Size) obj;
            if (this.width == s.width && this.height == s.height) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return (this.width * 32713) + this.height;
        }
    }

    public interface TargetTrackingCallback {
        void onTargetTracking(int i, int i2, Camera camera);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.Camera.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.Camera.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.Camera.<clinit>():void");
    }

    private final native void _addCallbackBuffer(byte[] bArr, int i);

    private final native boolean _enableShutterSound(boolean z);

    private static native void _getCameraInfo(int i, CameraInfo cameraInfo);

    private final native void _startFaceDetection(int i);

    private final native void _stopFaceDetection();

    private final native void _stopPreview();

    private native void enableFocusMoveCallback(int i);

    public static native int getNumberOfCameras();

    private final native void native_autoFocus();

    private final native void native_cancelAutoFocus();

    private final native void native_cancelTakePicture();

    private final native int native_getFocusContrast();

    private final native int native_getFocusValue();

    private final native String native_getParameters();

    private final native void native_release();

    private final native void native_setParameters(String str);

    private final native int native_setup(Object obj, int i, int i2, String str, boolean z);

    private final native void native_takePicture(int i);

    private final native void setHasPreviewCallback(boolean z, boolean z2);

    private final native void setPreviewCallbackSurface(Surface surface);

    public final native void lock();

    public final native boolean previewEnabled();

    public final native void reconnect() throws IOException;

    public final native void setDisplayOrientation(int i);

    public final native void setPreviewSurface(Surface surface) throws IOException;

    public final native void setPreviewTexture(SurfaceTexture surfaceTexture) throws IOException;

    public final native void startPreview();

    public final native void startSmoothZoom(int i);

    public final native void stopSmoothZoom();

    public final native void unlock();

    public static void getCameraInfo(int cameraId, CameraInfo cameraInfo) {
        _getCameraInfo(cameraId, cameraInfo);
        try {
            if (Stub.asInterface(ServiceManager.getService(Context.AUDIO_SERVICE)).isCameraSoundForced()) {
                cameraInfo.canDisableShutterSound = false;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Audio service is unavailable for queries");
        }
    }

    public static Camera open(int cameraId) {
        return new Camera(cameraId);
    }

    public static Camera open() {
        int numberOfCameras = getNumberOfCameras();
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = NO_ERROR; i < numberOfCameras; i += CAMERA_MSG_OIS_GRYO_OFFSET_CALIB_RESULT) {
            getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == 0) {
                return new Camera(i);
            }
        }
        return null;
    }

    public static Camera openLegacy(int cameraId, int halVersion) {
        if (halVersion >= CAMERA_MSG_COMPRESSED_IMAGE) {
            return new Camera(cameraId, halVersion);
        }
        throw new IllegalArgumentException("Invalid HAL version " + halVersion);
    }

    private Camera(int cameraId, int halVersion) {
        this.mFaceDetectionRunning = false;
        this.mAutoFocusCallbackLock = new Object();
        this.mPreviewParamsCallBackLock = new Object();
        this.mExposurePreviewStateCallbackLock = new Object();
        this.mMmiFocusResultCallBackLock = new Object();
        this.mFaceDetectionCallbackLock = new Object();
        this.mJpegCallbackLock = new Object();
        this.HW_FACE_BEAUTY_FOR_CHATTING = "hw-face-beauty-preview-only";
        this.HW_FACE_BEAUTY_ON = Parameters.FLASH_MODE_ON;
        this.HW_FACE_BEAUTY_OFF = Parameters.FLASH_MODE_OFF;
        int err = cameraInitVersion(cameraId, halVersion);
        if (!checkInitErrors(err)) {
            return;
        }
        if (err == (-OsConstants.EACCES)) {
            throw new RuntimeException("Fail to connect to camera service");
        } else if (err == (-OsConstants.ENODEV)) {
            throw new RuntimeException("Camera initialization failed");
        } else if (err == (-OsConstants.ENOSYS)) {
            throw new RuntimeException("Camera initialization failed because some methods are not implemented");
        } else if (err == (-OsConstants.EOPNOTSUPP)) {
            throw new RuntimeException("Camera initialization failed because the hal version is not supported by this device");
        } else if (err == (-OsConstants.EINVAL)) {
            throw new RuntimeException("Camera initialization failed because the input arugments are invalid");
        } else if (err == (-OsConstants.EBUSY)) {
            throw new RuntimeException("Camera initialization failed because the camera device was already opened");
        } else if (err == (-OsConstants.EUSERS)) {
            throw new RuntimeException("Camera initialization failed because the max number of camera devices were already opened");
        } else {
            throw new RuntimeException("Unknown camera error");
        }
    }

    private int cameraInitVersion(int cameraId, int halVersion) {
        this.mShutterCallback = null;
        this.mRawImageCallback = null;
        this.mJpegCallback = null;
        this.mPreviewCallback = null;
        this.mPostviewCallback = null;
        this.mUsingPreviewAllocation = false;
        this.mZoomListener = null;
        Looper looper = Looper.myLooper();
        if (looper != null) {
            this.mEventHandler = new EventHandler(this, looper);
        } else {
            looper = Looper.getMainLooper();
            if (looper != null) {
                this.mEventHandler = new EventHandler(this, looper);
            } else {
                this.mEventHandler = null;
            }
        }
        return native_setup(new WeakReference(this), cameraId, halVersion, ActivityThread.currentOpPackageName(), !HwSystemManager.allowOp(CAMERA_MSG_PREVIEW_METADATA));
    }

    private int cameraInitNormal(int cameraId) {
        return cameraInitVersion(cameraId, CAMERA_HAL_API_VERSION_NORMAL_CONNECT);
    }

    public int cameraInitUnspecified(int cameraId) {
        return cameraInitVersion(cameraId, PARAM_DEFAULT_VALUE);
    }

    Camera(int cameraId) {
        this.mFaceDetectionRunning = false;
        this.mAutoFocusCallbackLock = new Object();
        this.mPreviewParamsCallBackLock = new Object();
        this.mExposurePreviewStateCallbackLock = new Object();
        this.mMmiFocusResultCallBackLock = new Object();
        this.mFaceDetectionCallbackLock = new Object();
        this.mJpegCallbackLock = new Object();
        this.HW_FACE_BEAUTY_FOR_CHATTING = "hw-face-beauty-preview-only";
        this.HW_FACE_BEAUTY_ON = Parameters.FLASH_MODE_ON;
        this.HW_FACE_BEAUTY_OFF = Parameters.FLASH_MODE_OFF;
        int err = cameraInitNormal(cameraId);
        Log.e(TAG, "Camera new cameraInitNormal:" + err);
        if (!checkInitErrors(err)) {
            return;
        }
        if (err == (-OsConstants.EACCES)) {
            throw new RuntimeException("Fail to connect to camera service");
        } else if (err == (-OsConstants.ENODEV)) {
            throw new RuntimeException("Camera initialization failed");
        } else {
            throw new RuntimeException("Unknown camera error");
        }
    }

    public static boolean checkInitErrors(int err) {
        return err != 0;
    }

    public static Camera openUninitialized() {
        return new Camera();
    }

    Camera() {
        this.mFaceDetectionRunning = false;
        this.mAutoFocusCallbackLock = new Object();
        this.mPreviewParamsCallBackLock = new Object();
        this.mExposurePreviewStateCallbackLock = new Object();
        this.mMmiFocusResultCallBackLock = new Object();
        this.mFaceDetectionCallbackLock = new Object();
        this.mJpegCallbackLock = new Object();
        this.HW_FACE_BEAUTY_FOR_CHATTING = "hw-face-beauty-preview-only";
        this.HW_FACE_BEAUTY_ON = Parameters.FLASH_MODE_ON;
        this.HW_FACE_BEAUTY_OFF = Parameters.FLASH_MODE_OFF;
    }

    protected void finalize() {
        release();
    }

    public final void release() {
        native_release();
        this.mFaceDetectionRunning = false;
    }

    public final void setPreviewDisplay(SurfaceHolder holder) throws IOException {
        if (!HwSystemManager.allowOp(CAMERA_MSG_PREVIEW_METADATA) || holder == null) {
            setPreviewSurface((Surface) null);
        } else {
            setPreviewSurface(holder.getSurface());
        }
    }

    public final void stopPreview() {
        if (HWFLOW) {
            Log.i(TAG, "stopPreview");
        }
        _stopPreview();
        this.mFaceDetectionRunning = false;
        this.mShutterCallback = null;
        this.mRawImageCallback = null;
        this.mPostviewCallback = null;
        synchronized (this.mAutoFocusCallbackLock) {
            this.mAutoFocusCallback = null;
        }
        this.mAutoFocusMoveCallback = null;
    }

    public final void setPreviewCallback(PreviewCallback cb) {
        this.mPreviewCallback = cb;
        this.mOneShot = false;
        this.mWithBuffer = false;
        if (cb != null) {
            this.mUsingPreviewAllocation = false;
        }
        setHasPreviewCallback(cb != null, false);
    }

    public final void setOneShotPreviewCallback(PreviewCallback cb) {
        boolean z = true;
        this.mPreviewCallback = cb;
        this.mOneShot = true;
        this.mWithBuffer = false;
        if (cb != null) {
            this.mUsingPreviewAllocation = false;
        }
        if (cb == null) {
            z = false;
        }
        setHasPreviewCallback(z, false);
    }

    public final void setPreviewCallbackWithBuffer(PreviewCallback cb) {
        boolean z = false;
        this.mPreviewCallback = cb;
        this.mOneShot = false;
        this.mWithBuffer = true;
        if (cb != null) {
            this.mUsingPreviewAllocation = false;
        }
        if (cb != null) {
            z = true;
        }
        setHasPreviewCallback(z, true);
    }

    public final void addCallbackBuffer(byte[] callbackBuffer) {
        _addCallbackBuffer(callbackBuffer, CAMERA_MSG_PREVIEW_FRAME);
    }

    public final void addRawImageCallbackBuffer(byte[] callbackBuffer) {
        addCallbackBuffer(callbackBuffer, CAMERA_MSG_RAW_IMAGE);
    }

    private final void addCallbackBuffer(byte[] callbackBuffer, int msgType) {
        if (msgType == CAMERA_MSG_PREVIEW_FRAME || msgType == CAMERA_MSG_RAW_IMAGE) {
            _addCallbackBuffer(callbackBuffer, msgType);
            return;
        }
        throw new IllegalArgumentException("Unsupported message type: " + msgType);
    }

    public final Allocation createPreviewAllocation(RenderScript rs, int usage) throws RSIllegalArgumentException {
        Size previewSize = getParameters().getPreviewSize();
        Builder yuvBuilder = new Builder(rs, Element.createPixel(rs, DataType.UNSIGNED_8, DataKind.PIXEL_YUV));
        yuvBuilder.setYuvFormat(ImageFormat.YV12);
        yuvBuilder.setX(previewSize.width);
        yuvBuilder.setY(previewSize.height);
        return Allocation.createTyped(rs, yuvBuilder.create(), usage | CAMERA_MSG_VIDEO_FRAME);
    }

    public final void setPreviewCallbackAllocation(Allocation previewAllocation) throws IOException {
        Surface previewSurface = null;
        if (previewAllocation != null) {
            Size previewSize = getParameters().getPreviewSize();
            if (previewSize.width != previewAllocation.getType().getX() || previewSize.height != previewAllocation.getType().getY()) {
                throw new IllegalArgumentException("Allocation dimensions don't match preview dimensions: Allocation is " + previewAllocation.getType().getX() + ", " + previewAllocation.getType().getY() + ". Preview is " + previewSize.width + ", " + previewSize.height);
            } else if ((previewAllocation.getUsage() & CAMERA_MSG_VIDEO_FRAME) == 0) {
                throw new IllegalArgumentException("Allocation usage does not include USAGE_IO_INPUT");
            } else if (previewAllocation.getType().getElement().getDataKind() != DataKind.PIXEL_YUV) {
                throw new IllegalArgumentException("Allocation is not of a YUV type");
            } else {
                previewSurface = previewAllocation.getSurface();
                this.mUsingPreviewAllocation = true;
            }
        } else {
            this.mUsingPreviewAllocation = false;
        }
        setPreviewCallbackSurface(previewSurface);
    }

    private static void postEventFromNative(Object camera_ref, int what, int arg1, int arg2, Object obj) {
        Camera c = (Camera) ((WeakReference) camera_ref).get();
        if (!(c == null || c.mEventHandler == null)) {
            Message m = c.mEventHandler.obtainMessage(what, arg1, arg2, obj);
            if (m.what == CAMERA_MSG_OIS_GRYO_OFFSET_CALIB_RESULT) {
                c.mEventHandler.sendMessageAtFrontOfQueue(m);
                Log.e(TAG, "notify error: " + arg1 + ", and: " + arg2);
                return;
            }
            c.mEventHandler.sendMessage(m);
        }
    }

    public final void autoFocus(AutoFocusCallback cb) {
        synchronized (this.mAutoFocusCallbackLock) {
            this.mAutoFocusCallback = cb;
        }
        native_autoFocus();
    }

    public final void cancelAutoFocus() {
        synchronized (this.mAutoFocusCallbackLock) {
            this.mAutoFocusCallback = null;
        }
        native_cancelAutoFocus();
        this.mEventHandler.removeMessages(INT32_SIZE);
    }

    public void setAutoFocusMoveCallback(AutoFocusMoveCallback cb) {
        this.mAutoFocusMoveCallback = cb;
        enableFocusMoveCallback(this.mAutoFocusMoveCallback != null ? CAMERA_MSG_OIS_GRYO_OFFSET_CALIB_RESULT : NO_ERROR);
    }

    public final void takePicture(ShutterCallback shutter, PictureCallback raw, PictureCallback jpeg) {
        takePicture(shutter, raw, null, jpeg);
    }

    public final void cancelTakePicture() {
        native_cancelTakePicture();
    }

    public final void takePicture(ShutterCallback shutter, PictureCallback raw, PictureCallback postview, PictureCallback jpeg) {
        this.mShutterCallback = shutter;
        this.mRawImageCallback = raw;
        this.mPostviewCallback = postview;
        synchronized (this.mJpegCallbackLock) {
            this.mJpegCallback = jpeg;
        }
        int msgType = NO_ERROR;
        if (this.mShutterCallback != null) {
            msgType = CAMERA_MSG_SHUTTER;
        }
        if (this.mRawImageCallback != null) {
            msgType |= CAMERA_MSG_RAW_IMAGE;
        }
        if (this.mPostviewCallback != null) {
            msgType |= CAMERA_MSG_POSTVIEW_FRAME;
        }
        if (this.mJpegCallback != null) {
            msgType |= CAMERA_MSG_COMPRESSED_IMAGE;
        }
        native_takePicture(msgType);
        this.mFaceDetectionRunning = false;
    }

    public final boolean enableShutterSound(boolean enabled) {
        if (!enabled) {
            try {
                if (Stub.asInterface(ServiceManager.getService(Context.AUDIO_SERVICE)).isCameraSoundForced()) {
                    return false;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Audio service is unavailable for queries");
            }
        }
        return _enableShutterSound(enabled);
    }

    public final boolean disableShutterSound() {
        return _enableShutterSound(false);
    }

    public final void setZoomChangeListener(OnZoomChangeListener listener) {
        this.mZoomListener = listener;
    }

    public final void setPdafVerificationCallback(PdafVerificationCallback cb) {
        Log.i(TAG, "setPdafVerificationCallback: " + (cb != null));
        this.mPdafVerificationCallback = cb;
    }

    public final void setDarkRaiderCallback(DarkRaiderCallback cb) {
        Log.i(TAG, "setDarkRaiderCallback: " + (cb != null));
        this.mDarkRaiderCallback = cb;
    }

    public final void setPreviewParametersCallback(PreviewParametersCallback cb) {
        synchronized (this.mPreviewParamsCallBackLock) {
            this.mPreviewParamsCallBack = cb;
            this.mPreviewExposureTime = PARAM_DEFAULT_VALUE;
            this.mPreviewIsoValue = PARAM_DEFAULT_VALUE;
            this.mPreviewExposureHint = PARAM_DEFAULT_VALUE;
        }
    }

    public final void setLpdParametersCallback(LpdParametersCallback cb) {
        Log.i(TAG, "setLpdParametersCallback: " + (cb != null));
        this.mLpdParasCallback = cb;
    }

    public final void setTargetTrackingCallback(TargetTrackingCallback cb) {
        this.mTargetTrackingCallback = cb;
    }

    public final void setLcdCompensateCallback(LcdCompensateCallback cb) {
        this.mLcdCompensateCallback = cb;
    }

    public final void setExposurePreviewStateCallback(ExposurePreviewStateCallback cb) {
        synchronized (this.mExposurePreviewStateCallbackLock) {
            this.mExposurePreviewStateCallback = cb;
        }
    }

    public final void setDualCameraVerificationCallback(DualCameraVerificationCallback cb) {
        this.mDualCameraVerificationCallback = cb;
    }

    public final void setMMITestFeaturenCallback(MMITestFeatureCallback cb) {
        this.mMMITestFeatureCallback = cb;
    }

    public final void setLaserCalibrationCallback(LaserCalibrationCallback cb) {
        this.mLaserCalibrationCallback = cb;
    }

    public final void setDualCameraStateChangedCallback(DualCameraStateChangedCallback cb) {
        Log.i(TAG, "setDualCameraStateChangedCallback: " + (cb != null));
        this.mDualCameraStateChangedCallback = cb;
    }

    public final void startExposureMeasure(ExposureMeasureCallback callback) {
        Log.i(TAG, "startExposureMeasure() " + (callback != null));
        this.mExposureMeasureCallback = callback;
    }

    public final void setFaceDetectionListener(FaceDetectionListener listener) {
        synchronized (this.mFaceDetectionCallbackLock) {
            this.mFaceListener = listener;
        }
    }

    public final void setHdcResultCallback(HdcResultCallback callback) {
        Log.i(TAG, "setHdcResultCallback() " + (callback != null));
        this.mHdcResultCallback = callback;
    }

    public final void setFovResultCallback(FovResultCallback callback) {
        Log.i(TAG, "FovResultCallback() " + (callback != null));
        this.mFovResultCallback = callback;
    }

    public final void setOisGryoOffseCalibResultCallback(OisGryoOffseCalibResultCallback callback) {
        Log.i(TAG, "OisGryoOffseCalibResultCallback() " + (callback != null));
        this.mOisGryoOffseCalibResultCallback = callback;
    }

    public final void setOisHallCalibResultCallback(OisHallCalibResultCallback callback) {
        Log.i(TAG, "OisHallCalibResultCallback() " + (callback != null));
        this.mOisHallCalibResultCallback = callback;
    }

    public final void setOisHallCheckResultCallback(OisHallCheckResultCallback callback) {
        Log.i(TAG, "setOisHallCheckResultCallback() " + (callback != null));
        this.mOisHallCheckResultCallback = callback;
    }

    public final void setMmiFocusResultCallback(MmiFocusResultCallback callback) {
        Log.i(TAG, "setMmiFocusResultCallback: " + (callback != null));
        synchronized (this.mMmiFocusResultCallBackLock) {
            this.mMmiFocusResultCallback = callback;
        }
    }

    public final void startFaceDetection() {
        if (this.mFaceDetectionRunning) {
            throw new RuntimeException("Face detection is already running");
        }
        _startFaceDetection(NO_ERROR);
        this.mFaceDetectionRunning = true;
    }

    public final void stopFaceDetection() {
        _stopFaceDetection();
        this.mFaceDetectionRunning = false;
    }

    public final void setErrorCallback(ErrorCallback cb) {
        this.mErrorCallback = cb;
    }

    public void setParameters(Parameters params) {
        if (this.mUsingPreviewAllocation) {
            Size newPreviewSize = params.getPreviewSize();
            Size currentPreviewSize = getParameters().getPreviewSize();
            if (!(newPreviewSize.width == currentPreviewSize.width && newPreviewSize.height == currentPreviewSize.height)) {
                throw new IllegalStateException("Cannot change preview size while a preview allocation is configured.");
            }
        }
        addAudioViopMode(params);
        native_setParameters(params.flatten());
    }

    public Parameters getParameters() {
        Parameters p = new Parameters();
        p.unflatten(native_getParameters());
        return p;
    }

    public static Parameters getEmptyParameters() {
        Camera camera = new Camera();
        camera.getClass();
        return new Parameters(null);
    }

    private final boolean audoIsVoip() {
        try {
            if (CAMERA_MSG_OIS_HALL_CHECK_RESULT == Stub.asInterface(ServiceManager.getService(Context.AUDIO_SERVICE)).getMode()) {
                return true;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Audio service is unavailable for queries");
        }
        return false;
    }

    private final void addAudioViopMode(Parameters params) {
        String faceBeautyMode = Parameters.FLASH_MODE_OFF;
        if (audoIsVoip()) {
            faceBeautyMode = Parameters.FLASH_MODE_ON;
        }
        params.set("hw-face-beauty-preview-only", faceBeautyMode);
    }

    public int getFocusValue() {
        Log.i(TAG, "getFocusValue");
        return native_getFocusValue();
    }

    public int getFocusContrast() {
        Log.i(TAG, "getFocusContrast");
        int focusContrast = native_getFocusContrast();
        Log.i(TAG, "get focus contrast: " + focusContrast);
        return focusContrast;
    }

    public static Parameters getParametersCopy(Parameters parameters) {
        if (parameters == null) {
            throw new NullPointerException("parameters must not be null");
        }
        Camera camera = parameters.getOuter();
        camera.getClass();
        Parameters p = new Parameters(null);
        p.copyFrom(parameters);
        return p;
    }

    private int byteArrayToInt32(byte[] bytes, int index) {
        int value = NO_ERROR;
        for (int i = NO_ERROR; i < INT32_SIZE; i += CAMERA_MSG_OIS_GRYO_OFFSET_CALIB_RESULT) {
            value |= (bytes[i + index] & Process.PROC_TERM_MASK) << (i * CAMERA_MSG_ZOOM);
        }
        return value;
    }

    private String formatExposureTime(byte[] bytes) {
        int expoTime = byteArrayToInt32(bytes, NO_ERROR);
        if (expoTime == this.mPreviewExposureTime || expoTime == PARAM_DEFAULT_VALUE) {
            return null;
        }
        String strExpoTime;
        this.mPreviewExposureTime = expoTime;
        if (expoTime >= 1000000) {
            strExpoTime = String.valueOf(expoTime / 1000000);
        } else if (expoTime != 0) {
            strExpoTime = "1/" + (1000000 / expoTime);
        } else {
            strExpoTime = WifiEnterpriseConfig.ENGINE_DISABLE;
        }
        return strExpoTime;
    }

    private int formatIsoValue(byte[] bytes) {
        return byteArrayToInt32(bytes, INT32_SIZE);
    }

    private int formatExposureHint(byte[] bytes) {
        return byteArrayToInt32(bytes, CAMERA_MSG_ZOOM);
    }
}
