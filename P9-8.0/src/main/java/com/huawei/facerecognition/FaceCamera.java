package com.huawei.facerecognition;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureRequest.Key;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Flog;
import android.util.Range;
import android.view.Surface;
import com.huawei.facerecognition.base.HwSecurityEventTask;
import com.huawei.facerecognition.base.HwSecurityTaskThread;
import com.huawei.facerecognition.utils.LogUtil;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class FaceCamera {
    private static final Key<Integer> ANDROID_HW_BIO_FACE_MODE = new Key("bioFaceMode", Integer.TYPE);
    public static final int BD_REPORT_EVENT_ID_IMAGE = 502;
    public static final int BD_REPORT_EVNET_ID_OPEN = 501;
    private static final int BIO_FACE_MODE_AUTHENTICATION = 2;
    private static final int BIO_FACE_MODE_ENROLLMENT = 1;
    private static final int CAMERA_CLOSING = 2;
    private static final int CAMERA_IDLE = 0;
    private static final int CAMERA_OPENING = 3;
    private static final int CAMERA_READY = 1;
    private static final int HEIGHT = 480;
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
    private static final int SESSION_CREATED = 5;
    private static final int SESSION_CREATING = 4;
    private static final int SKIP_IMAGE_QUEUE_SIZE = 2;
    private static final boolean SKIP_IMAGE_SWICH_ON = true;
    public static final String SYSTEM_UI_PKG = "com.android.systemui";
    private static final String TAG = "FaceCamera";
    private static final int WIDTH = 640;
    private static final SensorEventListener mLightSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private static FaceCamera sInstance;
    private Range<Integer>[] fpsRanges;
    private boolean isEnrolling;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private Semaphore mCameraCloseLock = new Semaphore(1);
    private CameraDevice mCameraDevice;
    private Handler mCameraHandler = new Handler();
    private String mCameraId;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private final StateCallback mCameraStateCallback = new StateCallback() {
        public void onOpened(CameraDevice cameraDevice) {
            LogUtil.i(FaceCamera.TAG, "cb - onOpened");
            LogUtil.d("PerformanceTime", "Time 2.1. call-back open camera --- " + System.nanoTime());
            FaceCamera.this.mCameraOpenCloseLock.release();
            FaceCamera.this.mCameraDevice = cameraDevice;
            if (FaceCamera.this.updateStateTo(1)) {
                FaceCamera.this.handleMessage(0);
            }
        }

        public void onDisconnected(CameraDevice cameraDevice) {
            LogUtil.i(FaceCamera.TAG, "cb - onDisconnected");
            FaceCamera.this.updateStateTo(2);
            FaceCamera.this.unRegisterLightSensorListener();
            FaceCamera.this.mCameraOpenCloseLock.release();
            FaceCamera.this.mCameraCloseLock.release();
            FaceCamera.this.closeImageReader();
            FaceCamera.this.stopBackgroundThread();
            cameraDevice.close();
            FaceCamera.this.mCameraDevice = null;
            FaceCamera.this.handleMessage(3);
        }

        public void onError(CameraDevice cameraDevice, int error) {
            LogUtil.i(FaceCamera.TAG, "cb - onError " + error);
            FaceCamera.this.updateStateTo(0);
            FaceCamera.this.unRegisterLightSensorListener();
            FaceCamera.this.mCameraOpenCloseLock.release();
            FaceCamera.this.mCameraCloseLock.release();
            FaceCamera.this.closeImageReader();
            FaceCamera.this.stopBackgroundThread();
            cameraDevice.close();
            FaceCamera.this.mCameraDevice = null;
            FaceCamera.this.handleMessage(2, error);
        }

        public void onClosed(CameraDevice camera) {
            LogUtil.i(FaceCamera.TAG, "cb - onClosed");
            FaceCamera.this.updateStateTo(0);
            FaceCamera.this.handleMessage(4);
            FaceCamera.this.unRegisterLightSensorListener();
            FaceCamera.this.mCameraCloseLock.release();
            FaceCamera.this.closeImageReader();
            FaceCamera.this.stopBackgroundThread();
            FaceCamera.this.mCameraDevice = null;
        }
    };
    private CameraCaptureSession mCaptureSession;
    private CameraCharacteristics mCharacteristics;
    private Context mContext;
    private long mCurrentTime;
    private int mImageCount = 0;
    private Object mImageCountLock = new Object();
    private ImageReader mImageReader;
    private final Object mImageReaderLock = new Object();
    private volatile boolean mIsImageReported = false;
    private final OnImageAvailableListener mOnImageAvailableListener = new OnImageAvailableListener() {
        public void onImageAvailable(ImageReader reader) {
            Throwable th;
            Image image;
            Throwable th2;
            Throwable th3;
            LogUtil.d("DebugImage", "OnImageAvailable.");
            synchronized (FaceCamera.this.mImageReaderLock) {
                if (FaceCamera.this.mImageReader == null) {
                    return;
                }
                th = null;
                image = null;
                image = reader.acquireNextImage();
                if (image == null) {
                    LogUtil.d(FaceCamera.TAG, "Image is null.");
                } else if (FaceCamera.this.mBackgroundHandler == null) {
                    if (image != null) {
                        try {
                            image.close();
                        } catch (Throwable th4) {
                            th = th4;
                        }
                    }
                    if (th != null) {
                        try {
                            throw th;
                        } catch (Exception ex) {
                            LogUtil.e(FaceCamera.TAG, "Catch un-handle image exception " + ex.getMessage());
                            return;
                        }
                    }
                    return;
                } else if (FaceCamera.this.isEnrolling || !FaceCamera.this.increaseAndCheckImageCount()) {
                    try {
                        if (FaceCamera.this.printTimeLog) {
                            LogUtil.d("PerformanceTime", "Time 4.2. call-back get First Image --- " + System.nanoTime());
                        }
                        if (!FaceCamera.this.mIsImageReported && "com.android.systemui".equals(FaceCamera.this.mContext.getOpPackageName())) {
                            LogUtil.d(FaceCamera.TAG, "Big data report image");
                            long now = System.currentTimeMillis();
                            Flog.bdReport(FaceCamera.this.mContext, 502, "{\"capture_picture_cost_ms\":\"" + (now - FaceCamera.this.mCurrentTime) + "\"}");
                            FaceCamera.this.mCurrentTime = now;
                            FaceCamera.this.mIsImageReported = true;
                        } else if (!FaceCamera.this.mIsImageReported) {
                            LogUtil.d(FaceCamera.TAG, "Need report? : " + FaceCamera.this.mIsImageReported + ", OP pkg name : " + FaceCamera.this.mContext.getOpPackageName());
                            FaceCamera.this.mIsImageReported = true;
                        }
                        try {
                            LogUtil.d("DebugImage", "Extract image");
                            final Rect crop = image.getCropRect();
                            Plane[] planes = image.getPlanes();
                            final ByteBuffer[] planeArray = new ByteBuffer[planes.length];
                            final int[] rowStrideArray = new int[planes.length];
                            final int[] pixelStrideArray = new int[planes.length];
                            for (int i = 0; i < planes.length; i++) {
                                Plane plane = planes[i];
                                planeArray[i] = FaceCamera.this.cloneByteBuffer(plane.getBuffer());
                                rowStrideArray[i] = plane.getRowStride();
                                pixelStrideArray[i] = plane.getPixelStride();
                            }
                            LogUtil.d("DebugImage", "Extract image end");
                            FaceCamera.this.mBackgroundHandler.post(new Runnable() {
                                public void run() {
                                    if (FaceCamera.native_send_image(FaceCamera.this.getDataFromImage(crop, planeArray, rowStrideArray, pixelStrideArray)) != 0) {
                                        LogUtil.e(FaceCamera.TAG, "SendImageData failed");
                                    }
                                    if (FaceCamera.this.printTimeLog) {
                                        LogUtil.d("PerformanceTime", "Time 4.3. call-back Send First Image Data --- " + System.nanoTime());
                                        FaceCamera.this.printTimeLog = false;
                                    }
                                }
                            });
                        } catch (Exception ex2) {
                            LogUtil.e(FaceCamera.TAG, "Catch un-handle exception " + ex2.getMessage());
                        }
                    } catch (Throwable th32) {
                        Throwable th5 = th32;
                        th32 = th2;
                        th2 = th5;
                    }
                } else {
                    if (image != null) {
                        try {
                            image.close();
                        } catch (Throwable th6) {
                            th = th6;
                        }
                    }
                    if (th != null) {
                        throw th;
                    } else {
                        return;
                    }
                }
                if (image != null) {
                    try {
                        image.close();
                    } catch (Throwable th7) {
                        th = th7;
                    }
                }
                if (th != null) {
                    throw th;
                }
            }
            if (image != null) {
                try {
                    image.close();
                } catch (Throwable th8) {
                    if (th32 == null) {
                        th32 = th8;
                    } else if (th32 != th8) {
                        th32.addSuppressed(th8);
                    }
                }
            }
            if (th32 != null) {
                throw th32;
            } else {
                throw th2;
            }
        }
    };
    private Builder mPreviewRequestBuilder;
    private SensorManager mSensorManager;
    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        private boolean needSendRequestMsg = true;

        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
            LogUtil.i(FaceCamera.TAG, "cb - onConfigured");
            LogUtil.d("PerformanceTime", "Time 3.1. call-back create session --- " + System.nanoTime());
            if (FaceCamera.this.mCameraDevice == null) {
                FaceCamera.this.updateStateTo(0);
                FaceCamera.this.handleMessage(10);
                return;
            }
            if (FaceCamera.this.updateStateTo(5)) {
                FaceCamera.this.mCaptureSession = cameraCaptureSession;
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
            LogUtil.d("PerformanceTime", "Time 4.1. call-back create request --- " + System.nanoTime());
            if ("com.android.systemui".equals(FaceCamera.this.mContext.getOpPackageName())) {
                LogUtil.d(FaceCamera.TAG, "Big data report open");
                long now = System.currentTimeMillis();
                Flog.bdReport(FaceCamera.this.mContext, 501, "{\"open_camera_cost_ms\":\"" + (now - FaceCamera.this.mCurrentTime) + "\"}");
                FaceCamera.this.mCurrentTime = now;
                FaceCamera.this.mIsImageReported = false;
            } else {
                LogUtil.d(FaceCamera.TAG, "Pkg name : " + FaceCamera.this.mContext.getOpPackageName());
                FaceCamera.this.mIsImageReported = false;
            }
            if (this.needSendRequestMsg) {
                this.needSendRequestMsg = false;
                FaceCamera.this.handleMessage(20);
            }
        }
    };
    private int mState = 0;
    private final Object mStateLock = new Object();
    private volatile boolean printTimeLog = false;

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
        if (this.mSensorManager == null) {
            this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        }
        this.mSensorManager.registerListener(mLightSensorListener, this.mSensorManager.getDefaultSensor(5), 3);
    }

    private synchronized void unRegisterLightSensorListener() {
        if (this.mSensorManager != null) {
            this.mSensorManager.unregisterListener(mLightSensorListener);
            this.mSensorManager = null;
            LogUtil.w(TAG, "unRegisterLightSensorListener");
        }
    }

    private boolean increaseAndCheckImageCount() {
        boolean z = false;
        synchronized (this.mImageCountLock) {
            int i = this.mImageCount;
            this.mImageCount = i + 1;
            if (i % 2 != 0) {
                z = true;
            }
        }
        return z;
    }

    private void resetImageCount() {
        synchronized (this.mImageCountLock) {
            this.mImageCount = 0;
        }
    }

    private ByteBuffer cloneByteBuffer(ByteBuffer original) {
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

    private void stopBackgroundThread() {
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

    private byte[] getDataFromImage(Rect crop, ByteBuffer[] planeArray, int[] rowStrideArray, int[] pixelStrideArray) {
        int offset = 0;
        int offsetU = 0;
        int offsetV = 0;
        byte[] data = new byte[(((crop.width() * crop.height()) * ImageFormat.getBitsPerPixel(35)) / 8)];
        byte[] rowData = new byte[rowStrideArray[0]];
        int i = 0;
        while (i < planeArray.length) {
            int shift = i == 0 ? 0 : 1;
            ByteBuffer buffer = planeArray[i];
            int rowStride = rowStrideArray[i];
            int pixelStride = pixelStrideArray[i];
            int w = crop.width() >> shift;
            int h = crop.height() >> shift;
            buffer.position(((crop.top >> shift) * rowStride) + ((crop.left >> shift) * pixelStride));
            for (int row = 0; row < h; row++) {
                int length;
                int bytesPerPixel = ImageFormat.getBitsPerPixel(35) / 8;
                if (pixelStride == bytesPerPixel) {
                    length = w * bytesPerPixel;
                    buffer.get(data, offset, length);
                    offset += length;
                    offsetU = offset + 1;
                    offsetV = offset;
                } else {
                    int col;
                    length = ((w - 1) * pixelStride) + bytesPerPixel;
                    buffer.get(rowData, 0, length);
                    if (i == 1) {
                        for (col = 0; col < w; col++) {
                            data[offsetU] = rowData[col * pixelStride];
                            offsetU += 2;
                        }
                    }
                    if (i == 2) {
                        for (col = 0; col < w; col++) {
                            data[offsetV] = rowData[col * pixelStride];
                            offsetV += 2;
                        }
                    }
                }
                if (row < h - 1) {
                    buffer.position((buffer.position() + rowStride) - length);
                }
            }
            i++;
        }
        return data;
    }

    private FaceCamera() {
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

    /* JADX WARNING: Removed duplicated region for block: B:31:0x00ef A:{ExcHandler: android.hardware.camera2.CameraAccessException (r6_1 'e' java.lang.Exception), Splitter: B:6:0x0051} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00ef A:{ExcHandler: android.hardware.camera2.CameraAccessException (r6_1 'e' java.lang.Exception), Splitter: B:6:0x0051} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00ef A:{ExcHandler: android.hardware.camera2.CameraAccessException (r6_1 'e' java.lang.Exception), Splitter: B:6:0x0051} */
    /* JADX WARNING: Missing block: B:31:0x00ef, code:
            r6 = move-exception;
     */
    /* JADX WARNING: Missing block: B:32:0x00f0, code:
            unRegisterLightSensorListener();
            com.huawei.facerecognition.utils.LogUtil.e(TAG, "Occurs error " + r6.getMessage());
            updateStateTo(0);
            r14.mCameraOpenCloseLock.drainPermits();
            r14.mCameraOpenCloseLock.release();
     */
    /* JADX WARNING: Missing block: B:33:0x0120, code:
            return 2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int openCamera() {
        LogUtil.d(TAG, "call openCamera");
        LogUtil.d("PerformanceTime", "Time 2. call open camera --- " + System.nanoTime());
        int checkOpRlt = checkOperation(0);
        if (checkOpRlt != 1) {
            return checkOpRlt;
        }
        updateStateTo(3);
        CameraManager manager = (CameraManager) this.mContext.getSystemService("camera");
        if (!"com.android.systemui".equals(this.mContext.getPackageName())) {
            registerLightSensorListener();
        }
        try {
            String[] cameraList = manager.getCameraIdList();
            if (cameraList == null || cameraList.length == 0) {
                return 2;
            }
            for (int index = cameraList.length - 1; index > 0; index--) {
                String cameraId = cameraList[index];
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                Integer facing = (Integer) characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing.intValue() == 0) {
                    this.mCameraId = cameraId;
                    this.mCharacteristics = characteristics;
                    break;
                }
            }
            if (this.mCameraOpenCloseLock.tryAcquire(300, TimeUnit.MILLISECONDS)) {
                this.mCurrentTime = System.currentTimeMillis();
                manager.openCamera(this.mCameraId, this.mCameraStateCallback, this.mCameraHandler);
                this.fpsRanges = (Range[]) this.mCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
                startBackgroundThread();
                return 1;
            }
            handleMessage(1);
            updateStateTo(0);
            LogUtil.e(TAG, "Query CameraOpenCloseLock Timeout!");
            return 2;
        } catch (Exception e) {
        } catch (Exception e2) {
            unRegisterLightSensorListener();
            updateStateTo(0);
            LogUtil.e(TAG, "Occurs un-handle error " + e2.getMessage());
            this.mCameraOpenCloseLock.drainPermits();
            this.mCameraOpenCloseLock.release();
            return 2;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0077 A:{ExcHandler: android.hardware.camera2.CameraAccessException (r1_0 'e' java.lang.Exception), Splitter: B:4:0x0034} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0077 A:{ExcHandler: android.hardware.camera2.CameraAccessException (r1_0 'e' java.lang.Exception), Splitter: B:4:0x0034} */
    /* JADX WARNING: Missing block: B:18:0x0077, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:19:0x0078, code:
            com.huawei.facerecognition.utils.LogUtil.e(TAG, "Preview occurs un-handle error " + r1.getMessage());
     */
    /* JADX WARNING: Missing block: B:20:0x0097, code:
            return 2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int createPreviewSession(List<Surface> surfaces) {
        LogUtil.d(TAG, "call createPreviewSession");
        LogUtil.d("PerformanceTime", "Time 3. call create session --- " + System.nanoTime());
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
                ArrayList<Surface> tmpSurfaces = new ArrayList();
                for (Surface singleSurface : surfaces) {
                    this.mPreviewRequestBuilder.addTarget(singleSurface);
                    tmpSurfaces.add(singleSurface);
                }
                tmpSurfaces.add(this.mImageReader.getSurface());
                this.mPreviewRequestBuilder.addTarget(this.mImageReader.getSurface());
                LogUtil.d(TAG, "add image surface.");
                this.mCameraDevice.createCaptureSession(tmpSurfaces, this.mSessionStateCallback, this.mCameraHandler);
            }
            return 1;
        } catch (Exception e) {
        } catch (RuntimeException ex) {
            LogUtil.e(TAG, "Preview occurs un-handle error " + ex.getMessage());
            return 2;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0092 A:{ExcHandler: android.hardware.camera2.CameraAccessException (r1_0 'e' java.lang.Exception), Splitter: B:11:0x003e} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0092 A:{ExcHandler: android.hardware.camera2.CameraAccessException (r1_0 'e' java.lang.Exception), Splitter: B:11:0x003e} */
    /* JADX WARNING: Missing block: B:29:0x0092, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:30:0x0093, code:
            com.huawei.facerecognition.utils.LogUtil.e(TAG, "Request occurs un-handle error " + r1.getMessage());
     */
    /* JADX WARNING: Missing block: B:31:0x00b1, code:
            return 2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int createPreviewRequest(int requestType) {
        LogUtil.d(TAG, "call createPreviewRequest");
        LogUtil.d("PerformanceTime", "Time 4. call create request --- " + System.nanoTime());
        if (this.mCaptureSession == null || this.mPreviewRequestBuilder == null) {
            return 2;
        }
        int checkOpRlt = checkOperation(2);
        if (checkOpRlt != 1) {
            return checkOpRlt;
        }
        this.printTimeLog = true;
        if (requestType == 0) {
            try {
                this.isEnrolling = true;
            } catch (Exception e) {
            } catch (RuntimeException ex) {
                LogUtil.e(TAG, "Request occurs un-handle error " + ex.getMessage());
                return 2;
            }
        } else if (requestType == 1) {
            this.isEnrolling = false;
        }
        Range range = null;
        if (this.fpsRanges != null) {
            LogUtil.d(TAG, "Range : set range");
            for (Range<Integer> fpsRange : this.fpsRanges) {
                if (fpsRange != null && (range == null || (((Integer) fpsRange.getUpper()).intValue() <= ((Integer) range.getUpper()).intValue() && ((Integer) fpsRange.getLower()).intValue() <= ((Integer) range.getLower()).intValue()))) {
                    range = fpsRange;
                }
            }
        }
        if (range != null) {
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, range);
            LogUtil.d(TAG, "Range : set to success." + range.toString());
        }
        this.mCaptureSession.setRepeatingRequest(this.mPreviewRequestBuilder.build(), null, this.mCameraHandler);
        return 1;
    }

    public boolean close() {
        LogUtil.d(TAG, "call close");
        LogUtil.d("PerformanceTime", "Time 5. call close camera --- " + System.nanoTime());
        int checkOpRlt = checkOperation(3);
        if (checkOpRlt == 0) {
            return true;
        }
        if (checkOpRlt == 2) {
            return false;
        }
        updateStateTo(2);
        unRegisterLightSensorListener();
        closeImageReader();
        closeCamera();
        try {
            if (this.mCameraCloseLock.tryAcquire(500, TimeUnit.MILLISECONDS)) {
                this.mCameraCloseLock.release();
            } else {
                LogUtil.w(TAG, "Close Camera out of time.");
            }
            updateStateTo(0);
        } catch (InterruptedException e) {
            LogUtil.e(TAG, "InterruptedException occurs on closing.");
            updateStateTo(0);
        } catch (Throwable th) {
            updateStateTo(0);
            throw th;
        }
        LogUtil.d("PerformanceTime", "Time 5.1. call-back close camera --- " + System.nanoTime());
        return true;
    }

    private void closeImageReader() {
        synchronized (this.mImageReaderLock) {
            if (this.mImageReader != null) {
                LogUtil.d(TAG, "Close image surface.");
                this.mImageReader.close();
                this.mImageReader = null;
            }
        }
    }

    private void handleMessage(int msgType) {
        handleMessage(msgType, -1);
    }

    private void handleMessage(int msgType, int msgCode) {
        switch (msgType) {
            case 0:
                LogUtil.d(TAG, "MSG_OPEN_CAMERA_OK");
                HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new FaceRecognizeEvent(3, 1000)), 2);
                return;
            case 1:
                LogUtil.d(TAG, "MSG_OPEN_CAMERA_TIME_OUT");
                HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new FaceRecognizeEvent(3, RET_OPEN_CAMERA_TIMEOUT)), 2);
                return;
            case 2:
                LogUtil.d(TAG, "MSG_OPEN_CAMERA_ERROR");
                HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new FaceRecognizeEvent(3, 1001)), 2);
                return;
            case 3:
                LogUtil.d(TAG, "MSG_CAMERA_DISCONNECTED");
                HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new FaceRecognizeEvent(6, new int[0])), 2);
                return;
            case 4:
                LogUtil.d(TAG, "MSG_CAMERA_CLOSED");
                return;
            case 10:
                LogUtil.d(TAG, "MSG_CREATE_SESSION_CAMERA_CLOSED");
                return;
            case 11:
                LogUtil.d(TAG, "MSG_CREATE_SESSION_OK");
                HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new FaceRecognizeEvent(4, RET_CREATE_SESSION_OK)), 2);
                return;
            case 12:
                LogUtil.d(TAG, "MSG_CREATE_SESSION_FAILED");
                HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new FaceRecognizeEvent(4, RET_CREATE_SESSION_FAILED)), 2);
                return;
            case 20:
                LogUtil.d(TAG, "MSG_CREATE_REQUEST_OK");
                HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new FaceRecognizeEvent(5, RET_REPEAT_REQUEST_OK)), 2);
                return;
            default:
                return;
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
                this.mCaptureSession.close();
                this.mCaptureSession = null;
            }
            if (this.mCameraDevice != null) {
                this.mCameraDevice.close();
                this.mCameraDevice = null;
            }
            this.mCameraOpenCloseLock.release();
        } catch (InterruptedException e) {
            this.mCameraOpenCloseLock.release();
        } catch (Throwable th) {
            this.mCameraOpenCloseLock.release();
            throw th;
        }
    }

    private boolean updateStateTo(int toState) {
        synchronized (this.mStateLock) {
            String oldState = getCurState();
            switch (this.mState) {
                case 2:
                    if (toState != 0) {
                        return false;
                    }
                    this.mState = toState;
                    LogUtil.d(TAG, oldState + "--> " + getCurState());
                    return true;
                default:
                    this.mState = toState;
                    LogUtil.d(TAG, oldState + "--> " + getCurState());
                    return true;
            }
        }
    }

    /* JADX WARNING: Missing block: B:14:0x0055, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:34:0x00ae, code:
            return 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int checkOperation(int operation) {
        synchronized (this.mStateLock) {
            switch (operation) {
                case 0:
                    LogUtil.d(TAG, "curState = " + getCurState() + ", Operate = " + getOperate(operation) + " = " + (this.mState == 0 ? "ok" : "fail"));
                    if (this.mState == 1 || this.mState == 4 || this.mState == 5) {
                    } else if (this.mState == 0) {
                        return 1;
                    } else {
                        return 2;
                    }
                    break;
                case 1:
                    LogUtil.d(TAG, "curState = " + getCurState() + ", Operate = " + getOperate(operation) + " = " + (this.mState == 1 ? "ok" : "fail"));
                    if (this.mState == 5 || this.mState == 4) {
                    } else if (this.mState == 1) {
                        return 1;
                    } else {
                        return 2;
                    }
                    break;
                case 2:
                    LogUtil.d(TAG, "curState = " + getCurState() + ", Operate = " + getOperate(operation) + " = " + (this.mState == 5 ? "ok" : "fail"));
                    if (this.mState == 5) {
                        return 1;
                    }
                    return 2;
                case 3:
                    String str = TAG;
                    StringBuilder append = new StringBuilder().append("curState = ").append(getCurState()).append(", Operate = ").append(getOperate(operation)).append(" = ");
                    String str2 = (this.mState == 2 || this.mState == 0) ? "fail" : "ok";
                    LogUtil.d(str, append.append(str2).toString());
                    if (this.mState == 0) {
                        return 0;
                    } else if (this.mState == 2) {
                        return 2;
                    } else {
                        return 1;
                    }
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
