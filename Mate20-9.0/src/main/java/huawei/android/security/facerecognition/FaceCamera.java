package huawei.android.security.facerecognition;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.util.Flog;
import android.util.Range;
import android.view.Surface;
import huawei.android.security.facerecognition.FaceRecognizeManagerImpl;
import huawei.android.security.facerecognition.base.HwSecurityEventTask;
import huawei.android.security.facerecognition.base.HwSecurityTaskThread;
import huawei.android.security.facerecognition.utils.LogUtil;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class FaceCamera {
    private static final CaptureRequest.Key<Integer> ANDROID_HW_BIO_FACE_MODE = new CaptureRequest.Key<>("com.huawei.capture.metadata.bioFaceMode", Integer.TYPE);
    private static final String ANDROID_HW_BIO_FACE_RUNNING_MODE_KEY = "com.huawei.capture.metadata.bioFaceRunningMode";
    private static final int[] ANDROID_HW_BIO_FACE_RUNNING_MODE_KEY_VALUE = {1};
    private static final CameraCharacteristics.Key<Byte> ANDROID_HW_BIO_FACE_RUNNING_SUPPORTED = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.bioFaceRunningSupported", Byte.TYPE);
    private static final int AUTH_FPS_FOR_3D_STRUCTURE_LIGHT_CAMERA = 60;
    private static final int AUTH_FPS_FOR_DUAL_CAMERA = 30;
    private static final int AUTH_PAY_TYPE = 1;
    public static final int BD_REPORT_EVENT_ID_IMAGE = 502;
    public static final int BD_REPORT_EVNET_ID_OPEN = 501;
    private static final int BIO_FACE_MODE_3D_AUTHENTICATION = 5;
    private static final int BIO_FACE_MODE_3D_ENROLLMENT = 4;
    private static final int BIO_FACE_MODE_AUTHENTICATION = 2;
    private static final int BIO_FACE_MODE_ENROLLMENT = 1;
    private static final int BIO_FACE_MODE_PAY = 3;
    private static final int BIO_FACE_RUNNING_NOT_SUPPORTED = 0;
    private static final int BIO_FACE_RUNNING_SUPPORTED = 1;
    private static final int BIO_FACE_RUNNING_UNKOWN = -1;
    private static final int CAMERA_CLOSING = 2;
    private static final int CAMERA_IDLE = 0;
    private static final int CAMERA_OPENING = 3;
    private static final int CAMERA_READY = 1;
    private static final int CAMERA_STRUCTURE_LIGHT_SUPPORTED = 1;
    private static final int ENROLL_FPS_FOR_3D_STRUCTURE_LIGHT_CAMERA = 30;
    private static final int ENROLL_FPS_FOR_DUAL_CAMERA = 30;
    private static final int HEIGHT = 480;
    private static final CaptureRequest.Key<Integer> HUAWEI_BIO_RUNNING_MODE = new CaptureRequest.Key<>(ANDROID_HW_BIO_FACE_RUNNING_MODE_KEY, Integer.TYPE);
    private static final boolean IS_SOFTLIGHT_ALLOWED = SystemProperties.getBoolean("ro.config.allow_face_softlight", false);
    private static final int MSG_CAMERA_CLOSED = 4;
    private static final int MSG_CAMERA_DISCONNECTED = 3;
    private static final int MSG_CREATE_REQUEST_OK = 20;
    private static final int MSG_CREATE_SESSION_CAMERA_CLOSED = 10;
    private static final int MSG_CREATE_SESSION_FAILED = 12;
    private static final int MSG_CREATE_SESSION_OK = 11;
    private static final int MSG_OPEN_CAMERA_ERROR = 2;
    private static final int MSG_OPEN_CAMERA_OK = 0;
    private static final int MSG_OPEN_CAMERA_TIME_OUT = 1;
    private static final int NO_MSG_CODE = -1;
    private static final int OP_CLOSE_CAMERA = 3;
    private static final int OP_CREATE_SESSION = 1;
    private static final int OP_OPEN_CAMERA = 0;
    private static final int OP_SEND_REQUEST = 2;
    private static final int REQUEST_SENDING = 6;
    private static final int REQUEST_WORKING = 7;
    public static final int RET_CREATE_SESSION_FAILED = 1004;
    public static final int RET_CREATE_SESSION_OK = 1003;
    public static final int RET_OPEN_CAMERA_FAILED = 1001;
    public static final int RET_OPEN_CAMERA_OK = 1000;
    public static final int RET_OPEN_CAMERA_TIMEOUT = 1002;
    public static final int RET_OP_ALLOW = 1;
    public static final int RET_OP_ALREADY = 0;
    public static final int RET_OP_DENY = 2;
    public static final int RET_REPEAT_REQUEST_FAILED = 1006;
    public static final int RET_REPEAT_REQUEST_OK = 1005;
    private static final int SECURE_CAMERA = 1;
    private static final int SECURE_CAMERA_TYPE_POSITION = 1;
    private static final int SECURE_CAMERA_WITH_3D = 4;
    private static final int SECURE_CAMERA_WITH_DEPTH_MAP = 3;
    private static final int SECURE_MODE = 1;
    private static final float SENSOR_LIGHT_THRESHOLD = 2.0f;
    private static final int SESSION_CREATED = 5;
    private static final int SESSION_CREATING = 4;
    private static final int SKIP_IMAGE_QUEUE_SIZE = 2;
    private static final boolean SKIP_IMAGE_SWICH_ON = true;
    /* access modifiers changed from: private */
    public static final int SUPPORT_FACE_MODE = SystemProperties.getInt("ro.config.support_face_mode", 1);
    public static final String SYSTEM_UI_PKG = "com.android.systemui";
    private static final String TAG = "FaceCamera";
    private static final int UNSECURE_CAMERA = 0;
    private static final int WIDTH = 640;
    /* access modifiers changed from: private */
    public static float mCurrentLux;
    private static final SensorEventListener mLightSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == 5) {
                float unused = FaceCamera.mCurrentLux = event.values[0];
                FaceCamera.mSensorDataLock.release();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    /* access modifiers changed from: private */
    public static Semaphore mSensorDataLock = new Semaphore(1);
    private static FaceCamera sInstance;
    private Range<Integer>[] fpsRanges;
    /* access modifiers changed from: private */
    public boolean isEnrolling;
    /* access modifiers changed from: private */
    public Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private int mBioFaceRunningSupportMode = -1;
    /* access modifiers changed from: private */
    public Semaphore mCameraCloseLock = new Semaphore(1);
    /* access modifiers changed from: private */
    public CameraDevice mCameraDevice;
    private Handler mCameraHandler;
    private HandlerThread mCameraHandlerThread = new HandlerThread("face_camera");
    private String mCameraId;
    /* access modifiers changed from: private */
    public Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private final CameraDevice.StateCallback mCameraStateCallback = new CameraDevice.StateCallback() {
        public void onOpened(CameraDevice cameraDevice) {
            LogUtil.i(FaceCamera.TAG, "cb - onOpened");
            long current = System.nanoTime();
            LogUtil.d("PerformanceTime", "Time 2.1. call-back open camera --- " + current);
            FaceCamera.this.mCameraOpenCloseLock.release();
            CameraDevice unused = FaceCamera.this.mCameraDevice = cameraDevice;
            if (FaceCamera.this.updateStateTo(1)) {
                FaceCamera.this.handleMessage(0);
            }
        }

        public void onDisconnected(CameraDevice cameraDevice) {
            LogUtil.i(FaceCamera.TAG, "cb - onDisconnected");
            boolean unused = FaceCamera.this.updateStateTo(2);
            FaceCamera.this.unRegisterLightSensorListener();
            FaceCamera.this.mCameraOpenCloseLock.release();
            FaceCamera.this.mCameraCloseLock.release();
            FaceCamera.this.closeImageReader();
            FaceCamera.this.stopBackgroundThread();
            cameraDevice.close();
            CameraDevice unused2 = FaceCamera.this.mCameraDevice = null;
            FaceCamera.this.handleMessage(3);
        }

        public void onError(CameraDevice cameraDevice, int error) {
            LogUtil.i(FaceCamera.TAG, "cb - onError " + error);
            boolean unused = FaceCamera.this.updateStateTo(0);
            FaceCamera.this.unRegisterLightSensorListener();
            FaceCamera.this.mCameraOpenCloseLock.release();
            FaceCamera.this.mCameraCloseLock.release();
            FaceCamera.this.closeImageReader();
            FaceCamera.this.stopBackgroundThread();
            cameraDevice.close();
            CameraDevice unused2 = FaceCamera.this.mCameraDevice = null;
            FaceCamera.this.handleMessage(2, error);
        }

        public void onClosed(CameraDevice camera) {
            LogUtil.i(FaceCamera.TAG, "cb - onClosed");
            boolean unused = FaceCamera.this.updateStateTo(0);
            FaceCamera.this.handleMessage(4);
            FaceCamera.this.unRegisterLightSensorListener();
            FaceCamera.this.mCameraCloseLock.release();
            FaceCamera.this.closeImageReader();
            FaceCamera.this.stopBackgroundThread();
            CameraDevice unused2 = FaceCamera.this.mCameraDevice = null;
        }
    };
    /* access modifiers changed from: private */
    public CameraCaptureSession mCaptureSession;
    private CameraCharacteristics mCharacteristics;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public long mCurrentTime;
    private CaptureRequest.Key<int[]> mHwBioFaceRunningModeKey = null;
    private int mImageCount = 0;
    private Object mImageCountLock = new Object();
    private ImageReader mImageReader;
    /* access modifiers changed from: private */
    public final Object mImageReaderLock = new Object();
    /* access modifiers changed from: private */
    public volatile boolean mIsImageReported = false;
    private Byte mIsStructuredLightSupported = (byte) 0;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        /* JADX WARNING: Unknown top exception splitter block from list: {B:28:0x0042=Splitter:B:28:0x0042, B:17:0x002b=Splitter:B:17:0x002b} */
        public void onImageAvailable(ImageReader reader) {
            Image image;
            Throwable th;
            Throwable th2;
            LogUtil.d("DebugImage", "OnImageAvailable.");
            synchronized (FaceCamera.this.mImageReaderLock) {
                if (reader != null) {
                    try {
                        image = reader.acquireNextImage();
                        if (image != null) {
                            try {
                                if (FaceCamera.this.mBackgroundHandler == null) {
                                    if (image != null) {
                                        image.close();
                                    }
                                    return;
                                } else if (FaceCamera.this.isEnrolling || !FaceCamera.this.increaseAndCheckImageCount()) {
                                    if (FaceCamera.this.printTimeLog) {
                                        long current = System.nanoTime();
                                        LogUtil.d("PerformanceTime", "Time 4.2. call-back get First Image --- " + current);
                                    }
                                    if (!FaceCamera.this.mIsImageReported && "com.android.systemui".equals(FaceCamera.this.mContext.getOpPackageName())) {
                                        LogUtil.d(FaceCamera.TAG, "Big data report image");
                                        long now = System.currentTimeMillis();
                                        Context access$800 = FaceCamera.this.mContext;
                                        Flog.bdReport(access$800, 502, "{\"capture_picture_cost_ms\":\"" + (now - FaceCamera.this.mCurrentTime) + "\"}");
                                        long unused = FaceCamera.this.mCurrentTime = now;
                                        boolean unused2 = FaceCamera.this.mIsImageReported = true;
                                    } else if (!FaceCamera.this.mIsImageReported) {
                                        LogUtil.d(FaceCamera.TAG, "Need report? : " + FaceCamera.this.mIsImageReported + ", OP pkg name : " + FaceCamera.this.mContext.getOpPackageName());
                                        boolean unused3 = FaceCamera.this.mIsImageReported = true;
                                    }
                                    try {
                                        LogUtil.d("DebugImage", "Extract image");
                                        final Rect crop = image.getCropRect();
                                        Image.Plane[] planes = image.getPlanes();
                                        ByteBuffer[] planeArray = new ByteBuffer[planes.length];
                                        int[] rowStrideArray = new int[planes.length];
                                        int[] pixelStrideArray = new int[planes.length];
                                        for (int i = 0; i < planes.length; i++) {
                                            Image.Plane plane = planes[i];
                                            planeArray[i] = FaceCamera.this.cloneByteBuffer(plane.getBuffer());
                                            rowStrideArray[i] = plane.getRowStride();
                                            pixelStrideArray[i] = plane.getPixelStride();
                                        }
                                        LogUtil.d("DebugImage", "Extract image end");
                                        Handler access$300 = FaceCamera.this.mBackgroundHandler;
                                        final ByteBuffer[] byteBufferArr = planeArray;
                                        final int[] iArr = rowStrideArray;
                                        AnonymousClass1 r10 = r1;
                                        final int[] iArr2 = pixelStrideArray;
                                        AnonymousClass1 r1 = new Runnable() {
                                            public void run() {
                                                if (FaceCamera.SUPPORT_FACE_MODE == 0 && FaceCamera.native_send_image(FaceCamera.this.getDataFromImage(crop, byteBufferArr, iArr, iArr2)) != 0) {
                                                    LogUtil.e(FaceCamera.TAG, "SendImageData failed");
                                                }
                                                if (FaceCamera.this.printTimeLog) {
                                                    long current = System.nanoTime();
                                                    LogUtil.d("PerformanceTime", "Time 4.3. call-back Send First Image Data --- " + current);
                                                    boolean unused = FaceCamera.this.printTimeLog = false;
                                                }
                                            }
                                        };
                                        access$300.post(r10);
                                    } catch (Exception ex) {
                                        LogUtil.e(FaceCamera.TAG, "Catch un-handle exception " + ex.getMessage());
                                    }
                                } else {
                                    if (image != null) {
                                        image.close();
                                    }
                                    return;
                                }
                            } catch (Throwable th3) {
                                th2 = th3;
                            }
                        } else {
                            LogUtil.d(FaceCamera.TAG, "Image is null.");
                        }
                        if (image != null) {
                            image.close();
                        }
                    } catch (Exception ex2) {
                        LogUtil.e(FaceCamera.TAG, "Catch un-handle image exception " + ex2.getMessage());
                    }
                } else {
                    return;
                }
            }
            return;
            if (image != null) {
                if (th != null) {
                    try {
                        image.close();
                    } catch (Throwable th4) {
                        th.addSuppressed(th4);
                    }
                } else {
                    image.close();
                }
            }
            throw th2;
            throw th2;
        }
    };
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private SensorManager mSensorManager;
    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        private boolean needSendRequestMsg = true;

        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
            LogUtil.i(FaceCamera.TAG, "cb - onConfigured");
            long current = System.nanoTime();
            LogUtil.d("PerformanceTime", "Time 3.1. call-back create session --- " + current);
            if (FaceCamera.this.mCameraDevice == null) {
                boolean unused = FaceCamera.this.updateStateTo(0);
                FaceCamera.this.handleMessage(10);
                return;
            }
            if (FaceCamera.this.updateStateTo(5)) {
                CameraCaptureSession unused2 = FaceCamera.this.mCaptureSession = cameraCaptureSession;
                FaceCamera.this.handleMessage(11);
            }
        }

        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
            LogUtil.i(FaceCamera.TAG, "cb - onConfiguredFailed");
            if (FaceCamera.this.updateStateTo(0)) {
                FaceCamera.this.handleMessage(12);
            }
        }

        public void onReady(CameraCaptureSession session) {
            LogUtil.i(FaceCamera.TAG, "cb - onReady");
            this.needSendRequestMsg = true;
        }

        public void onActive(CameraCaptureSession session) {
            LogUtil.i(FaceCamera.TAG, "cb - onActive");
            long current = System.nanoTime();
            LogUtil.d("PerformanceTime", "Time 4.1. call-back create request --- " + current);
            if ("com.android.systemui".equals(FaceCamera.this.mContext.getOpPackageName())) {
                LogUtil.d(FaceCamera.TAG, "Big data report open");
                long now = System.currentTimeMillis();
                Context access$800 = FaceCamera.this.mContext;
                Flog.bdReport(access$800, FaceCamera.BD_REPORT_EVNET_ID_OPEN, "{\"open_camera_cost_ms\":\"" + (now - FaceCamera.this.mCurrentTime) + "\"}");
                long unused = FaceCamera.this.mCurrentTime = now;
                boolean unused2 = FaceCamera.this.mIsImageReported = false;
            } else {
                LogUtil.d(FaceCamera.TAG, "Pkg name : " + FaceCamera.this.mContext.getOpPackageName());
                boolean unused3 = FaceCamera.this.mIsImageReported = false;
            }
            if (this.needSendRequestMsg) {
                this.needSendRequestMsg = false;
                FaceCamera.this.handleMessage(20);
            }
        }
    };
    private boolean mSoftlightOn = false;
    private int mState = 0;
    private final Object mStateLock = new Object();
    /* access modifiers changed from: private */
    public volatile boolean printTimeLog = false;

    private static class HandlerExecutor implements Executor {
        private final Handler mHandler;

        public HandlerExecutor(Handler handler) {
            this.mHandler = handler;
        }

        public void execute(Runnable runCmd) {
            this.mHandler.post(runCmd);
        }
    }

    public static native int native_send_image(byte[] bArr);

    static {
        try {
            System.loadLibrary("FaceRecognizeSendImage");
        } catch (UnsatisfiedLinkError e) {
            LogUtil.e(TAG, "LoadLibrary occurs error " + e.toString());
        }
    }

    private synchronized void registerLightSensorListener() {
        LogUtil.d(TAG, "registerLightSensorListener");
        mSensorDataLock.tryAcquire();
        if (this.mSensorManager == null) {
            this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        }
        this.mSensorManager.registerListener(mLightSensorListener, this.mSensorManager.getDefaultSensor(5), 3);
    }

    /* access modifiers changed from: private */
    public synchronized void unRegisterLightSensorListener() {
        if (this.mSensorManager != null) {
            this.mSensorManager.unregisterListener(mLightSensorListener);
            this.mSensorManager = null;
            LogUtil.w(TAG, "unRegisterLightSensorListener");
        }
    }

    /* access modifiers changed from: private */
    public boolean increaseAndCheckImageCount() {
        boolean z;
        synchronized (this.mImageCountLock) {
            int i = this.mImageCount;
            this.mImageCount = i + 1;
            z = i % 2 != 0;
        }
        return z;
    }

    private void resetImageCount() {
        synchronized (this.mImageCountLock) {
            this.mImageCount = 0;
        }
    }

    /* access modifiers changed from: private */
    public ByteBuffer cloneByteBuffer(ByteBuffer original) {
        ByteBuffer clone = ByteBuffer.allocate(original.capacity());
        original.rewind();
        clone.put(original);
        original.rewind();
        clone.flip();
        return clone;
    }

    private void startBackgroundThread() {
        if (this.mBackgroundThread == null) {
            this.mBackgroundThread = new HandlerThread("ImageExtractorThread");
            this.mBackgroundThread.start();
            this.mBackgroundHandler = new Handler(this.mBackgroundThread.getLooper());
            resetImageCount();
        }
    }

    /* access modifiers changed from: private */
    public void stopBackgroundThread() {
        if (this.mBackgroundThread != null) {
            this.mBackgroundThread.quit();
            try {
                this.mBackgroundThread.join();
                this.mBackgroundThread = null;
                this.mBackgroundHandler = null;
                resetImageCount();
            } catch (InterruptedException e) {
                LogUtil.e(TAG, "Stop background thread occurs InterruptedException");
            }
        }
    }

    /* access modifiers changed from: private */
    public byte[] getDataFromImage(Rect crop, ByteBuffer[] planeArray, int[] rowStrideArray, int[] pixelStrideArray) {
        int length;
        Rect rect = crop;
        ByteBuffer[] byteBufferArr = planeArray;
        byte[] data = new byte[(((crop.width() * crop.height()) * ImageFormat.getBitsPerPixel(35)) / 8)];
        int i = 0;
        byte[] rowData = new byte[rowStrideArray[0]];
        int offsetV = 0;
        int offsetU = 0;
        int offset = 0;
        int i2 = 0;
        while (i2 < byteBufferArr.length) {
            int shift = i2 == 0 ? i : 1;
            ByteBuffer buffer = byteBufferArr[i2];
            int rowStride = rowStrideArray[i2];
            int pixelStride = pixelStrideArray[i2];
            int w = crop.width() >> shift;
            int h = crop.height() >> shift;
            buffer.position(((rect.top >> shift) * rowStride) + ((rect.left >> shift) * pixelStride));
            int row = 0;
            while (row < h) {
                int bytesPerPixel = ImageFormat.getBitsPerPixel(35) / 8;
                if (pixelStride == bytesPerPixel) {
                    length = w * bytesPerPixel;
                    buffer.get(data, offset, length);
                    offset += length;
                    offsetU = offset + 1;
                    offsetV = offset;
                    int i3 = bytesPerPixel;
                } else {
                    length = ((w - 1) * pixelStride) + bytesPerPixel;
                    int i4 = bytesPerPixel;
                    buffer.get(rowData, 0, length);
                    if (i2 == 1) {
                        int offsetU2 = offsetU;
                        for (int col = 0; col < w; col++) {
                            data[offsetU2] = rowData[col * pixelStride];
                            offsetU2 += 2;
                        }
                        offsetU = offsetU2;
                    }
                    if (i2 == 2) {
                        for (int col2 = 0; col2 < w; col2++) {
                            data[offsetV] = rowData[col2 * pixelStride];
                            offsetV += 2;
                        }
                    }
                }
                if (row < h - 1) {
                    buffer.position((buffer.position() + rowStride) - length);
                }
                row++;
                Rect rect2 = crop;
            }
            i2++;
            rect = crop;
            byteBufferArr = planeArray;
            i = 0;
        }
        return data;
    }

    private FaceCamera() {
        this.mCameraHandlerThread.start();
        this.mCameraHandler = new Handler(this.mCameraHandlerThread.getLooper());
    }

    public static synchronized FaceCamera getInstance() {
        FaceCamera faceCamera;
        synchronized (FaceCamera.class) {
            if (sInstance == null) {
                sInstance = new FaceCamera();
            }
            faceCamera = sInstance;
        }
        return faceCamera;
    }

    public void init(Context context) {
        this.mContext = context;
    }

    private CaptureRequest.Key<int[]> getAvailableSessionKeys(CameraCharacteristics characteristics, String key) {
        if (characteristics == null) {
            LogUtil.d(TAG, "[getAvailableSessionKeys] characteristics is null");
            return null;
        }
        CaptureRequest.Key<int[]> keyP2NotificationRequest = null;
        List<CaptureRequest.Key<?>> requestKeyList = characteristics.getAvailableSessionKeys();
        if (requestKeyList == null) {
            LogUtil.d(TAG, "[getAvailableSessionKeys] No keys!");
            return null;
        }
        for (CaptureRequest.Key<int[]> requestKey : requestKeyList) {
            if (requestKey.getName().equals(key)) {
                keyP2NotificationRequest = requestKey;
            }
        }
        return keyP2NotificationRequest;
    }

    private void configureQuickPreview(CaptureRequest.Builder builder) {
        LogUtil.d(TAG, "configureQuickPreview mHwBioFaceRunningModeKey:" + this.mHwBioFaceRunningModeKey);
        if (this.mHwBioFaceRunningModeKey != null) {
            builder.set(this.mHwBioFaceRunningModeKey, ANDROID_HW_BIO_FACE_RUNNING_MODE_KEY_VALUE);
        }
    }

    public int openCamera(int requestType) {
        LogUtil.d(TAG, "call openCamera");
        long current = System.nanoTime();
        LogUtil.d("PerformanceTime", "Time 2. call open camera --- " + current);
        int checkOpRlt = checkOperation(0);
        if (checkOpRlt != 1) {
            return checkOpRlt;
        }
        updateStateTo(3);
        CameraManager manager = (CameraManager) this.mContext.getSystemService("camera");
        if (IS_SOFTLIGHT_ALLOWED && "com.android.systemui".equals(this.mContext.getPackageName())) {
            registerLightSensorListener();
        }
        try {
            String[] cameraList = manager.getCameraIdList();
            if (cameraList != null) {
                if (cameraList.length != 0) {
                    int index = cameraList.length - 1;
                    while (true) {
                        if (index <= 0) {
                            break;
                        }
                        String cameraId = cameraList[index];
                        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                        Integer facing = (Integer) characteristics.get(CameraCharacteristics.LENS_FACING);
                        if (facing != null) {
                            if (facing.intValue() == 0) {
                                this.mCameraId = cameraId;
                                this.mCharacteristics = characteristics;
                                break;
                            }
                        }
                        index--;
                    }
                    if (!this.mCameraOpenCloseLock.tryAcquire(300, TimeUnit.MILLISECONDS)) {
                        handleMessage(1);
                        updateStateTo(0);
                        unRegisterLightSensorListener();
                        LogUtil.e(TAG, "Query CameraOpenCloseLock Timeout!");
                        return 2;
                    }
                    this.mCurrentTime = System.currentTimeMillis();
                    if (SUPPORT_FACE_MODE != 0) {
                        LogUtil.d(TAG, "call setSecureFaceMode. requestType=" + requestType);
                        if (FaceRecognizeManagerImpl.ServiceHolder.getInstance().setSecureFaceMode((requestType << 1) | 1) != 0) {
                            LogUtil.e(TAG, "setSecureMode failed");
                            handleMessage(2);
                            updateStateTo(0);
                            unRegisterLightSensorListener();
                            this.mCameraOpenCloseLock.release();
                            return 2;
                        }
                    }
                    LogUtil.d(TAG, "call manager.openCamera");
                    manager.openCamera(this.mCameraId, this.mCameraStateCallback, this.mCameraHandler);
                    this.fpsRanges = (Range[]) this.mCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
                    if (SUPPORT_FACE_MODE == 4) {
                        this.mIsStructuredLightSupported = (Byte) this.mCharacteristics.get(new CameraCharacteristics.Key("com.huawei.device.capabilities.hwStructuredLightSupported", Byte.TYPE));
                        LogUtil.d(TAG, "mIsStructuredLightSupported " + this.mIsStructuredLightSupported);
                    }
                    this.mHwBioFaceRunningModeKey = getAvailableSessionKeys(this.mCharacteristics, ANDROID_HW_BIO_FACE_RUNNING_MODE_KEY);
                    startBackgroundThread();
                    return 1;
                }
            }
            updateStateTo(0);
            unRegisterLightSensorListener();
            LogUtil.e(TAG, "get cameraList error: cameraList=" + cameraList);
            return 2;
        } catch (CameraAccessException | IllegalArgumentException | InterruptedException | SecurityException e) {
            unRegisterLightSensorListener();
            LogUtil.e(TAG, "Occurs error " + e.getMessage());
            updateStateTo(0);
            this.mCameraOpenCloseLock.drainPermits();
            this.mCameraOpenCloseLock.release();
            return 2;
        } catch (Exception e2) {
            unRegisterLightSensorListener();
            updateStateTo(0);
            LogUtil.e(TAG, "Occurs un-handle error " + e2.getMessage());
            this.mCameraOpenCloseLock.drainPermits();
            this.mCameraOpenCloseLock.release();
            return 2;
        }
    }

    public int createPreviewSession(List<Surface> surfaces) {
        LogUtil.d(TAG, "call createPreviewSession");
        long current = System.nanoTime();
        LogUtil.d("PerformanceTime", "Time 3. call create session --- " + current);
        int checkOpRlt = checkOperation(1);
        if (checkOpRlt != 1) {
            return checkOpRlt;
        }
        updateStateTo(4);
        try {
            this.mPreviewRequestBuilder = this.mCameraDevice.createCaptureRequest(1);
            synchronized (this.mImageReaderLock) {
                this.mImageReader = ImageReader.newInstance(WIDTH, HEIGHT, 35, 2);
                this.mImageReader.setOnImageAvailableListener(this.mOnImageAvailableListener, this.mCameraHandler);
                List<OutputConfiguration> outputConfigs = new ArrayList<>();
                ArrayList<Surface> tmpSurfaces = new ArrayList<>();
                for (Surface singleSurface : surfaces) {
                    outputConfigs.add(new OutputConfiguration(singleSurface));
                    this.mPreviewRequestBuilder.addTarget(singleSurface);
                    tmpSurfaces.add(singleSurface);
                }
                if (SUPPORT_FACE_MODE == 0 || tmpSurfaces.isEmpty()) {
                    outputConfigs.add(new OutputConfiguration(this.mImageReader.getSurface()));
                    tmpSurfaces.add(this.mImageReader.getSurface());
                    this.mPreviewRequestBuilder.addTarget(this.mImageReader.getSurface());
                    LogUtil.d(TAG, "add image surface.");
                }
                configureQuickPreview(this.mPreviewRequestBuilder);
                SessionConfiguration sessionConfigByOutput = new SessionConfiguration(0, outputConfigs, new HandlerExecutor(this.mCameraHandler), this.mSessionStateCallback);
                sessionConfigByOutput.setSessionParameters(this.mPreviewRequestBuilder.build());
                this.mCameraDevice.createCaptureSession(sessionConfigByOutput);
            }
            return 1;
        } catch (CameraAccessException | IllegalArgumentException | IllegalStateException e) {
            LogUtil.e(TAG, "Preview occurs un-handle error " + e.getMessage());
            return 2;
        } catch (RuntimeException ex) {
            LogUtil.e(TAG, "Preview occurs un-handle error " + ex.getMessage());
            return 2;
        }
    }

    public int createPreviewRequest(int requestType, int flag) {
        int fpsForAuth;
        int fpsForEnroll;
        Range<Integer> targetFps;
        LogUtil.d(TAG, "call createPreviewRequest");
        long current = System.nanoTime();
        LogUtil.d("PerformanceTime", "Time 4. call create request --- " + current);
        if (this.mCaptureSession == null || this.mPreviewRequestBuilder == null) {
            return 2;
        }
        int checkOpRlt = checkOperation(2);
        if (checkOpRlt != 1) {
            return checkOpRlt;
        }
        this.printTimeLog = true;
        int bioFaceMode = 1;
        if (requestType == 0) {
            try {
                this.isEnrolling = true;
            } catch (CameraAccessException | IllegalArgumentException | IllegalStateException e) {
                LogUtil.e(TAG, "Request occurs un-handle error " + e.getMessage());
                return 2;
            } catch (InterruptedException ec) {
                LogUtil.e(TAG, "InterruptedException occurs on getting mSensorDataLock." + ec.getMessage());
            } catch (RuntimeException ex) {
                LogUtil.e(TAG, "Request occurs un-handle error " + ex.getMessage());
                return 2;
            }
        } else if (requestType == 1) {
            this.isEnrolling = false;
            bioFaceMode = flag == 1 ? 3 : 2;
        }
        if (SUPPORT_FACE_MODE == 4) {
            if (1 == bioFaceMode) {
                bioFaceMode = 4;
            } else {
                bioFaceMode = 5;
            }
        }
        if (SUPPORT_FACE_MODE != 0) {
            LogUtil.d(TAG, String.format("camera mode set mode:%d", new Object[]{Integer.valueOf(bioFaceMode)}));
            this.mPreviewRequestBuilder.set(ANDROID_HW_BIO_FACE_MODE, Integer.valueOf(bioFaceMode));
        }
        if (SUPPORT_FACE_MODE == 4) {
            LogUtil.d(TAG, "Range : set range");
            if (this.mIsStructuredLightSupported == null || this.mIsStructuredLightSupported.byteValue() != 1) {
                fpsForEnroll = 30;
                fpsForAuth = 30;
            } else {
                fpsForEnroll = 30;
                fpsForAuth = AUTH_FPS_FOR_3D_STRUCTURE_LIGHT_CAMERA;
            }
            if (4 == bioFaceMode) {
                targetFps = new Range<>(Integer.valueOf(fpsForEnroll), Integer.valueOf(fpsForEnroll));
            } else {
                targetFps = new Range<>(Integer.valueOf(fpsForAuth), Integer.valueOf(fpsForAuth));
            }
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, targetFps);
            LogUtil.d(TAG, "Range : set to success." + targetFps.toString());
        } else if (SUPPORT_FACE_MODE == 0 || SUPPORT_FACE_MODE == 1) {
            Range<Integer> targetFps2 = null;
            if (this.fpsRanges != null) {
                LogUtil.d(TAG, "Range : set range");
                Range<Integer> targetFps3 = null;
                for (Range<Integer> fpsRange : this.fpsRanges) {
                    if (fpsRange != null && (targetFps3 == null || (fpsRange.getUpper().intValue() <= targetFps3.getUpper().intValue() && fpsRange.getLower().intValue() <= targetFps3.getLower().intValue()))) {
                        targetFps3 = fpsRange;
                    }
                }
                targetFps2 = targetFps3;
            }
            if (targetFps2 != null) {
                this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, targetFps2);
                LogUtil.d(TAG, "Range : set to success." + targetFps2.toString());
            }
        }
        if (this.mCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) != null) {
            if (!IS_SOFTLIGHT_ALLOWED || !((Boolean) this.mCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)).booleanValue()) {
                this.mSoftlightOn = false;
                LogUtil.d(TAG, "Softlight is not supported or allowed.");
            } else if (!mSensorDataLock.tryAcquire(30, TimeUnit.MILLISECONDS)) {
                LogUtil.d(TAG, "Wait light sensor data out of time.");
            } else if (mCurrentLux <= SENSOR_LIGHT_THRESHOLD) {
                this.mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, 2);
                LogUtil.d(TAG, "lux : " + mCurrentLux);
                LogUtil.d(TAG, "Dark scene, open Softlight.");
                this.mSoftlightOn = true;
            }
        }
        if (isBioFaceRunningSupported()) {
            this.mPreviewRequestBuilder.set(HUAWEI_BIO_RUNNING_MODE, 1);
        }
        configureQuickPreview(this.mPreviewRequestBuilder);
        this.mCaptureSession.setRepeatingRequest(this.mPreviewRequestBuilder.build(), null, this.mCameraHandler);
        return 1;
    }

    private boolean isBioFaceRunningSupported() {
        if (this.mBioFaceRunningSupportMode == -1 && this.mCharacteristics != null) {
            try {
                byte supportMode = ((Byte) this.mCharacteristics.get(ANDROID_HW_BIO_FACE_RUNNING_SUPPORTED)).byteValue();
                this.mBioFaceRunningSupportMode = supportMode == 1 ? 1 : 0;
                LogUtil.i(TAG, "supportMode " + supportMode);
            } catch (Exception e) {
                this.mBioFaceRunningSupportMode = 0;
                LogUtil.e(TAG, "get support mode exception: " + e);
            }
        }
        if (this.mBioFaceRunningSupportMode == 1) {
            return true;
        }
        return false;
    }

    public boolean close() {
        LogUtil.d(TAG, "call close");
        long current = System.nanoTime();
        LogUtil.d("PerformanceTime", "Time 5. call close camera --- " + current);
        int checkOpRlt = checkOperation(3);
        if (checkOpRlt == 0) {
            return true;
        }
        if (checkOpRlt == 2) {
            return false;
        }
        if (this.mSoftlightOn) {
            LogUtil.d(TAG, "Close Softlight.");
            try {
                this.mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, 0);
                this.mCaptureSession.setRepeatingRequest(this.mPreviewRequestBuilder.build(), null, this.mCameraHandler);
                this.mSoftlightOn = false;
            } catch (CameraAccessException | IllegalArgumentException | IllegalStateException e) {
                LogUtil.e(TAG, "Close Softlight error " + e.getMessage());
            }
        }
        updateStateTo(2);
        unRegisterLightSensorListener();
        closeImageReader();
        closeCamera();
        try {
            if (!this.mCameraCloseLock.tryAcquire(500, TimeUnit.MILLISECONDS)) {
                LogUtil.w(TAG, "Close Camera out of time.");
            }
        } catch (InterruptedException e2) {
            LogUtil.e(TAG, "InterruptedException occurs on closing.");
        } catch (Throwable th) {
            updateStateTo(0);
            this.mCameraCloseLock.release();
            throw th;
        }
        updateStateTo(0);
        this.mCameraCloseLock.release();
        long current2 = System.nanoTime();
        LogUtil.d("PerformanceTime", "Time 5.1. call-back close camera --- " + current2);
        return true;
    }

    /* access modifiers changed from: private */
    public void closeImageReader() {
        synchronized (this.mImageReaderLock) {
            if (this.mImageReader != null) {
                LogUtil.d(TAG, "Close image surface.");
                this.mImageReader.close();
                this.mImageReader = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleMessage(int msgType) {
        handleMessage(msgType, -1);
    }

    /* access modifiers changed from: private */
    public void handleMessage(int msgType, int msgCode) {
        if (msgType != 20) {
            switch (msgType) {
                case 0:
                    LogUtil.d(TAG, "MSG_OPEN_CAMERA_OK");
                    HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new FaceRecognizeEvent(3, 1000)), 2);
                    return;
                case 1:
                    LogUtil.d(TAG, "MSG_OPEN_CAMERA_TIME_OUT");
                    HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new FaceRecognizeEvent(3, 1002)), 2);
                    return;
                case 2:
                    LogUtil.d(TAG, "MSG_OPEN_CAMERA_ERROR");
                    HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new FaceRecognizeEvent(3, 1001)), 2);
                    return;
                case 3:
                    LogUtil.d(TAG, "MSG_CAMERA_DISCONNECTED");
                    HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new FaceRecognizeEvent(6, new long[0])), 2);
                    return;
                case 4:
                    LogUtil.d(TAG, "MSG_CAMERA_CLOSED");
                    return;
                default:
                    switch (msgType) {
                        case 10:
                            LogUtil.d(TAG, "MSG_CREATE_SESSION_CAMERA_CLOSED");
                            return;
                        case 11:
                            LogUtil.d(TAG, "MSG_CREATE_SESSION_OK");
                            HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new FaceRecognizeEvent(4, 1003)), 2);
                            return;
                        case 12:
                            LogUtil.d(TAG, "MSG_CREATE_SESSION_FAILED");
                            HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new FaceRecognizeEvent(4, 1004)), 2);
                            return;
                        default:
                            return;
                    }
            }
        } else {
            LogUtil.d(TAG, "MSG_CREATE_REQUEST_OK");
            HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new FaceRecognizeEvent(5, 1005)), 2);
        }
    }

    private void closeCamera() {
        try {
            this.mCameraCloseLock.acquire();
            if (!this.mCameraOpenCloseLock.tryAcquire(3000, TimeUnit.MILLISECONDS)) {
                LogUtil.e(TAG, "Lock not released, recycle resources now");
                updateStateTo(0);
                stopBackgroundThread();
                this.mCameraCloseLock.release();
            }
            if (this.mCaptureSession != null) {
                try {
                    this.mCaptureSession.abortCaptures();
                } catch (CameraAccessException e) {
                    LogUtil.e(TAG, "abort capture CameraAccessException error " + e);
                } catch (IllegalStateException e2) {
                    LogUtil.e(TAG, "abort capture IllegalStateException error " + e2);
                }
                this.mCaptureSession.close();
                this.mCaptureSession = null;
            }
            if (this.mCameraDevice != null) {
                this.mCameraDevice.close();
                this.mCameraDevice = null;
            }
        } catch (InterruptedException e3) {
        } catch (Throwable th) {
            this.mCameraOpenCloseLock.release();
            throw th;
        }
        this.mCameraOpenCloseLock.release();
    }

    /* access modifiers changed from: private */
    public boolean updateStateTo(int toState) {
        synchronized (this.mStateLock) {
            String oldState = getCurState();
            if (this.mState != 2) {
                this.mState = toState;
                LogUtil.d(TAG, oldState + "--> " + getCurState());
                return true;
            } else if (toState != 0) {
                return false;
            } else {
                this.mState = toState;
                LogUtil.d(TAG, oldState + "--> " + getCurState());
                return true;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00df, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x012d, code lost:
        return 0;
     */
    private int checkOperation(int operation) {
        synchronized (this.mStateLock) {
            switch (operation) {
                case 0:
                    StringBuilder sb = new StringBuilder();
                    sb.append("curState = ");
                    sb.append(getCurState());
                    sb.append(", Operate = ");
                    sb.append(getOperate(operation));
                    sb.append(" = ");
                    sb.append(this.mState == 0 ? "ok" : "fail");
                    LogUtil.d(TAG, sb.toString());
                    if (!(this.mState == 1 || this.mState == 4)) {
                        if (this.mState == 5) {
                            break;
                        } else {
                            return this.mState == 0 ? 1 : 2;
                        }
                    }
                case 1:
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("curState = ");
                    sb2.append(getCurState());
                    sb2.append(", Operate = ");
                    sb2.append(getOperate(operation));
                    sb2.append(" = ");
                    sb2.append(this.mState == 1 ? "ok" : "fail");
                    LogUtil.d(TAG, sb2.toString());
                    if (this.mState != 5) {
                        if (this.mState == 4) {
                            break;
                        } else {
                            return this.mState == 1 ? 1 : 2;
                        }
                    }
                    break;
                case 2:
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append("curState = ");
                    sb3.append(getCurState());
                    sb3.append(", Operate = ");
                    sb3.append(getOperate(operation));
                    sb3.append(" = ");
                    sb3.append(this.mState == 5 ? "ok" : "fail");
                    LogUtil.d(TAG, sb3.toString());
                    return this.mState == 5 ? 1 : 2;
                case 3:
                    StringBuilder sb4 = new StringBuilder();
                    sb4.append("curState = ");
                    sb4.append(getCurState());
                    sb4.append(", Operate = ");
                    sb4.append(getOperate(operation));
                    sb4.append(" = ");
                    sb4.append((this.mState == 2 || this.mState == 0) ? "fail" : "ok");
                    LogUtil.d(TAG, sb4.toString());
                    if (this.mState == 0) {
                        return 0;
                    }
                    return this.mState == 2 ? 2 : 1;
                default:
                    return 1;
            }
        }
    }

    private String getOperate(int operation) {
        switch (operation) {
            case 0:
                return "OP_OPEN_CAMERA";
            case 1:
                return "OP_CREATE_SESSION";
            case 2:
                return "OP_SEND_REQUEST";
            case 3:
                return "OP_CLOSE_CAMERA";
            default:
                return "ERROR_OP";
        }
    }

    private String getCurState() {
        switch (this.mState) {
            case 0:
                return "CAMERA_IDLE";
            case 1:
                return "CAMERA_READY";
            case 2:
                return "CAMERA_CLOSING";
            case 3:
                return "CAMERA_OPENING";
            case 4:
                return "SESSION_CREATING";
            case 5:
                return "SESSION_CREATED";
            case 6:
                return "REQUEST_SENDING";
            case 7:
                return "REQUEST_WORKING";
            default:
                return "ERROR_STATE";
        }
    }
}
