package android.hardware.camera2.legacy;

import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.ICameraDeviceCallbacks;
import android.hardware.camera2.ICameraDeviceUser;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.impl.CaptureResultExtras;
import android.hardware.camera2.impl.PhysicalCaptureResultInfo;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.utils.SubmitInfo;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.system.OsConstants;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;
import android.view.Surface;
import java.util.List;

public class CameraDeviceUserShim implements ICameraDeviceUser {
    private static final boolean DEBUG = false;
    private static final int OPEN_CAMERA_TIMEOUT_MS = 5000;
    private static final String TAG = "CameraDeviceUserShim";
    private final CameraCallbackThread mCameraCallbacks;
    private final CameraCharacteristics mCameraCharacteristics;
    private final CameraLooper mCameraInit;
    private final Object mConfigureLock = new Object();
    private boolean mConfiguring;
    private final LegacyCameraDevice mLegacyDevice;
    private int mSurfaceIdCounter;
    private final SparseArray<Surface> mSurfaces;

    protected CameraDeviceUserShim(int cameraId, LegacyCameraDevice legacyCamera, CameraCharacteristics characteristics, CameraLooper cameraInit, CameraCallbackThread cameraCallbacks) {
        this.mLegacyDevice = legacyCamera;
        this.mConfiguring = false;
        this.mSurfaces = new SparseArray<>();
        this.mCameraCharacteristics = characteristics;
        this.mCameraInit = cameraInit;
        this.mCameraCallbacks = cameraCallbacks;
        this.mSurfaceIdCounter = 0;
    }

    private static int translateErrorsFromCamera1(int errorCode) {
        if (errorCode == (-OsConstants.EACCES)) {
            return 1;
        }
        return errorCode;
    }

    /* access modifiers changed from: private */
    public static class CameraLooper implements Runnable, AutoCloseable {
        private final Camera mCamera = Camera.openUninitialized();
        private final int mCameraId;
        private volatile int mInitErrors;
        private Looper mLooper;
        private final ConditionVariable mStartDone = new ConditionVariable();
        private final Thread mThread;

        public CameraLooper(int cameraId) {
            this.mCameraId = cameraId;
            this.mThread = new Thread(this);
            this.mThread.start();
        }

        public Camera getCamera() {
            return this.mCamera;
        }

        public void run() {
            Looper.prepare();
            this.mLooper = Looper.myLooper();
            this.mInitErrors = this.mCamera.cameraInitUnspecified(this.mCameraId);
            this.mStartDone.open();
            Looper.loop();
        }

        @Override // java.lang.AutoCloseable
        public void close() {
            Looper looper = this.mLooper;
            if (looper != null) {
                looper.quitSafely();
                try {
                    this.mThread.join();
                    this.mLooper = null;
                } catch (InterruptedException e) {
                    throw new AssertionError(e);
                }
            }
        }

        public int waitForOpen(int timeoutMs) {
            if (this.mStartDone.block((long) timeoutMs)) {
                return this.mInitErrors;
            }
            Log.e(CameraDeviceUserShim.TAG, "waitForOpen - Camera failed to open after timeout of 5000 ms");
            try {
                this.mCamera.release();
            } catch (RuntimeException e) {
                Log.e(CameraDeviceUserShim.TAG, "connectBinderShim - Failed to release camera after timeout ", e);
            }
            throw new ServiceSpecificException(10);
        }
    }

    /* access modifiers changed from: private */
    public static class CameraCallbackThread implements ICameraDeviceCallbacks {
        private static final int CAMERA_ERROR = 0;
        private static final int CAMERA_IDLE = 1;
        private static final int CAPTURE_STARTED = 2;
        private static final int PREPARED = 4;
        private static final int REPEATING_REQUEST_ERROR = 5;
        private static final int REQUEST_QUEUE_EMPTY = 6;
        private static final int RESULT_RECEIVED = 3;
        private final ICameraDeviceCallbacks mCallbacks;
        private Handler mHandler;
        private final HandlerThread mHandlerThread = new HandlerThread("LegacyCameraCallback");

        public CameraCallbackThread(ICameraDeviceCallbacks callbacks) {
            this.mCallbacks = callbacks;
            this.mHandlerThread.start();
        }

        public void close() {
            this.mHandlerThread.quitSafely();
        }

        @Override // android.hardware.camera2.ICameraDeviceCallbacks
        public void onDeviceError(int errorCode, CaptureResultExtras resultExtras) {
            getHandler().sendMessage(getHandler().obtainMessage(0, errorCode, 0, resultExtras));
        }

        @Override // android.hardware.camera2.ICameraDeviceCallbacks
        public void onDeviceIdle() {
            getHandler().sendMessage(getHandler().obtainMessage(1));
        }

        @Override // android.hardware.camera2.ICameraDeviceCallbacks
        public void onCaptureStarted(CaptureResultExtras resultExtras, long timestamp) {
            getHandler().sendMessage(getHandler().obtainMessage(2, (int) (timestamp & 4294967295L), (int) (4294967295L & (timestamp >> 32)), resultExtras));
        }

        @Override // android.hardware.camera2.ICameraDeviceCallbacks
        public void onResultReceived(CameraMetadataNative result, CaptureResultExtras resultExtras, PhysicalCaptureResultInfo[] physicalResults) {
            getHandler().sendMessage(getHandler().obtainMessage(3, new Object[]{result, resultExtras}));
        }

        @Override // android.hardware.camera2.ICameraDeviceCallbacks
        public void onPrepared(int streamId) {
            getHandler().sendMessage(getHandler().obtainMessage(4, streamId, 0));
        }

        @Override // android.hardware.camera2.ICameraDeviceCallbacks
        public void onRepeatingRequestError(long lastFrameNumber, int repeatingRequestId) {
            getHandler().sendMessage(getHandler().obtainMessage(5, new Object[]{Long.valueOf(lastFrameNumber), Integer.valueOf(repeatingRequestId)}));
        }

        @Override // android.hardware.camera2.ICameraDeviceCallbacks
        public void onRequestQueueEmpty() {
            getHandler().sendMessage(getHandler().obtainMessage(6, 0, 0));
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }

        private Handler getHandler() {
            if (this.mHandler == null) {
                this.mHandler = new CallbackHandler(this.mHandlerThread.getLooper());
            }
            return this.mHandler;
        }

        /* access modifiers changed from: private */
        public class CallbackHandler extends Handler {
            public CallbackHandler(Looper l) {
                super(l);
            }

            /* JADX INFO: Multiple debug info for r0v11 int: [D('objArray' java.lang.Object[]), D('streamId' int)] */
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                try {
                    switch (msg.what) {
                        case 0:
                            CameraCallbackThread.this.mCallbacks.onDeviceError(msg.arg1, (CaptureResultExtras) msg.obj);
                            return;
                        case 1:
                            CameraCallbackThread.this.mCallbacks.onDeviceIdle();
                            return;
                        case 2:
                            long timestamp = ((((long) msg.arg2) & 4294967295L) << 32) | (4294967295L & ((long) msg.arg1));
                            CameraCallbackThread.this.mCallbacks.onCaptureStarted((CaptureResultExtras) msg.obj, timestamp);
                            return;
                        case 3:
                            Object[] resultArray = (Object[]) msg.obj;
                            CameraCallbackThread.this.mCallbacks.onResultReceived((CameraMetadataNative) resultArray[0], (CaptureResultExtras) resultArray[1], new PhysicalCaptureResultInfo[0]);
                            return;
                        case 4:
                            CameraCallbackThread.this.mCallbacks.onPrepared(msg.arg1);
                            return;
                        case 5:
                            Object[] objArray = (Object[]) msg.obj;
                            CameraCallbackThread.this.mCallbacks.onRepeatingRequestError(((Long) objArray[0]).longValue(), ((Integer) objArray[1]).intValue());
                            return;
                        case 6:
                            CameraCallbackThread.this.mCallbacks.onRequestQueueEmpty();
                            return;
                        default:
                            throw new IllegalArgumentException("Unknown callback message " + msg.what);
                    }
                } catch (RemoteException e) {
                    throw new IllegalStateException("Received remote exception during camera callback " + msg.what, e);
                }
            }
        }
    }

    public static CameraDeviceUserShim connectBinderShim(ICameraDeviceCallbacks callbacks, int cameraId, Size displaySize) {
        CameraLooper init = new CameraLooper(cameraId);
        CameraCallbackThread threadCallbacks = new CameraCallbackThread(callbacks);
        int initErrors = init.waitForOpen(5000);
        Camera legacyCamera = init.getCamera();
        LegacyExceptionUtils.throwOnServiceError(initErrors);
        legacyCamera.disableShutterSound();
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        try {
            CameraCharacteristics characteristics = LegacyMetadataMapper.createCharacteristics(legacyCamera.getParameters(), info, cameraId, displaySize);
            return new CameraDeviceUserShim(cameraId, new LegacyCameraDevice(cameraId, legacyCamera, characteristics, threadCallbacks), characteristics, init, threadCallbacks);
        } catch (RuntimeException e) {
            throw new ServiceSpecificException(10, "Unable to get initial parameters: " + e.getMessage());
        }
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public void disconnect() {
        if (this.mLegacyDevice.isClosed()) {
            Log.w(TAG, "Cannot disconnect, device has already been closed.");
        }
        try {
            this.mLegacyDevice.close();
        } finally {
            this.mCameraInit.close();
            this.mCameraCallbacks.close();
        }
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public SubmitInfo submitRequest(CaptureRequest request, boolean streaming) {
        if (!this.mLegacyDevice.isClosed()) {
            synchronized (this.mConfigureLock) {
                if (this.mConfiguring) {
                    Log.e(TAG, "Cannot submit request, configuration change in progress.");
                    throw new ServiceSpecificException(10, "Cannot submit request, configuration change in progress.");
                }
            }
            return this.mLegacyDevice.submitRequest(request, streaming);
        }
        Log.e(TAG, "Cannot submit request, device has been closed.");
        throw new ServiceSpecificException(4, "Cannot submit request, device has been closed.");
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public SubmitInfo submitRequestList(CaptureRequest[] request, boolean streaming) {
        if (!this.mLegacyDevice.isClosed()) {
            synchronized (this.mConfigureLock) {
                if (this.mConfiguring) {
                    Log.e(TAG, "Cannot submit request, configuration change in progress.");
                    throw new ServiceSpecificException(10, "Cannot submit request, configuration change in progress.");
                }
            }
            return this.mLegacyDevice.submitRequestList(request, streaming);
        }
        Log.e(TAG, "Cannot submit request list, device has been closed.");
        throw new ServiceSpecificException(4, "Cannot submit request list, device has been closed.");
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public long cancelRequest(int requestId) {
        if (!this.mLegacyDevice.isClosed()) {
            synchronized (this.mConfigureLock) {
                if (this.mConfiguring) {
                    Log.e(TAG, "Cannot cancel request, configuration change in progress.");
                    throw new ServiceSpecificException(10, "Cannot cancel request, configuration change in progress.");
                }
            }
            return this.mLegacyDevice.cancelRequest(requestId);
        }
        Log.e(TAG, "Cannot cancel request, device has been closed.");
        throw new ServiceSpecificException(4, "Cannot cancel request, device has been closed.");
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public boolean isSessionConfigurationSupported(SessionConfiguration sessionConfig) {
        if (sessionConfig.getSessionType() != 0) {
            Log.e(TAG, "Session type: " + sessionConfig.getSessionType() + " is different from  regular. Legacy devices support only regular session types!");
            return false;
        } else if (sessionConfig.getInputConfiguration() != null) {
            Log.e(TAG, "Input configuration present, legacy devices do not support this feature!");
            return false;
        } else {
            List<OutputConfiguration> outputConfigs = sessionConfig.getOutputConfigurations();
            if (outputConfigs.isEmpty()) {
                Log.e(TAG, "Empty output configuration list!");
                return false;
            }
            SparseArray<Surface> surfaces = new SparseArray<>(outputConfigs.size());
            int idx = 0;
            for (OutputConfiguration outputConfig : outputConfigs) {
                List<Surface> surfaceList = outputConfig.getSurfaces();
                if (surfaceList.isEmpty() || surfaceList.size() > 1) {
                    Log.e(TAG, "Legacy devices do not support deferred or shared surfaces!");
                    return false;
                }
                surfaces.put(idx, outputConfig.getSurface());
                idx++;
            }
            if (this.mLegacyDevice.configureOutputs(surfaces, true) == 0) {
                return true;
            }
            return false;
        }
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public void beginConfigure() {
        if (!this.mLegacyDevice.isClosed()) {
            synchronized (this.mConfigureLock) {
                if (!this.mConfiguring) {
                    this.mConfiguring = true;
                } else {
                    Log.e(TAG, "Cannot begin configure, configuration change already in progress.");
                    throw new ServiceSpecificException(10, "Cannot begin configure, configuration change already in progress.");
                }
            }
            return;
        }
        Log.e(TAG, "Cannot begin configure, device has been closed.");
        throw new ServiceSpecificException(4, "Cannot begin configure, device has been closed.");
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public void endConfigure(int operatingMode, CameraMetadataNative sessionParams) {
        if (this.mLegacyDevice.isClosed()) {
            Log.e(TAG, "Cannot end configure, device has been closed.");
            synchronized (this.mConfigureLock) {
                this.mConfiguring = false;
            }
            throw new ServiceSpecificException(4, "Cannot end configure, device has been closed.");
        } else if (operatingMode == 0) {
            SparseArray<Surface> surfaces = null;
            synchronized (this.mConfigureLock) {
                if (this.mConfiguring) {
                    if (this.mSurfaces != null) {
                        surfaces = this.mSurfaces.clone();
                    }
                    this.mConfiguring = false;
                } else {
                    Log.e(TAG, "Cannot end configure, no configuration change in progress.");
                    throw new ServiceSpecificException(10, "Cannot end configure, no configuration change in progress.");
                }
            }
            this.mLegacyDevice.configureOutputs(surfaces);
        } else {
            Log.e(TAG, "LEGACY devices do not support this operating mode");
            synchronized (this.mConfigureLock) {
                this.mConfiguring = false;
            }
            throw new ServiceSpecificException(3, "LEGACY devices do not support this operating mode");
        }
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public void deleteStream(int streamId) {
        if (!this.mLegacyDevice.isClosed()) {
            synchronized (this.mConfigureLock) {
                if (this.mConfiguring) {
                    int index = this.mSurfaces.indexOfKey(streamId);
                    if (index >= 0) {
                        this.mSurfaces.removeAt(index);
                    } else {
                        String err = "Cannot delete stream, stream id " + streamId + " doesn't exist.";
                        Log.e(TAG, err);
                        throw new ServiceSpecificException(3, err);
                    }
                } else {
                    Log.e(TAG, "Cannot delete stream, no configuration change in progress.");
                    throw new ServiceSpecificException(10, "Cannot delete stream, no configuration change in progress.");
                }
            }
            return;
        }
        Log.e(TAG, "Cannot delete stream, device has been closed.");
        throw new ServiceSpecificException(4, "Cannot delete stream, device has been closed.");
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public int createStream(OutputConfiguration outputConfiguration) {
        int id;
        if (!this.mLegacyDevice.isClosed()) {
            synchronized (this.mConfigureLock) {
                if (!this.mConfiguring) {
                    Log.e(TAG, "Cannot create stream, beginConfigure hasn't been called yet.");
                    throw new ServiceSpecificException(10, "Cannot create stream, beginConfigure hasn't been called yet.");
                } else if (outputConfiguration.getRotation() == 0) {
                    id = this.mSurfaceIdCounter + 1;
                    this.mSurfaceIdCounter = id;
                    this.mSurfaces.put(id, outputConfiguration.getSurface());
                } else {
                    Log.e(TAG, "Cannot create stream, stream rotation is not supported.");
                    throw new ServiceSpecificException(3, "Cannot create stream, stream rotation is not supported.");
                }
            }
            return id;
        }
        Log.e(TAG, "Cannot create stream, device has been closed.");
        throw new ServiceSpecificException(4, "Cannot create stream, device has been closed.");
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public void finalizeOutputConfigurations(int steamId, OutputConfiguration config) {
        Log.e(TAG, "Finalizing output configuration is not supported on legacy devices");
        throw new ServiceSpecificException(10, "Finalizing output configuration is not supported on legacy devices");
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public int createInputStream(int width, int height, int format) {
        Log.e(TAG, "Creating input stream is not supported on legacy devices");
        throw new ServiceSpecificException(10, "Creating input stream is not supported on legacy devices");
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public Surface getInputSurface() {
        Log.e(TAG, "Getting input surface is not supported on legacy devices");
        throw new ServiceSpecificException(10, "Getting input surface is not supported on legacy devices");
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public CameraMetadataNative createDefaultRequest(int templateId) {
        if (!this.mLegacyDevice.isClosed()) {
            try {
                return LegacyMetadataMapper.createRequestTemplate(this.mCameraCharacteristics, templateId);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "createDefaultRequest - invalid templateId specified");
                throw new ServiceSpecificException(3, "createDefaultRequest - invalid templateId specified");
            }
        } else {
            Log.e(TAG, "Cannot create default request, device has been closed.");
            throw new ServiceSpecificException(4, "Cannot create default request, device has been closed.");
        }
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public CameraMetadataNative getCameraInfo() {
        Log.e(TAG, "getCameraInfo unimplemented.");
        return null;
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public void updateOutputConfiguration(int streamId, OutputConfiguration config) {
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public void waitUntilIdle() throws RemoteException {
        if (!this.mLegacyDevice.isClosed()) {
            synchronized (this.mConfigureLock) {
                if (this.mConfiguring) {
                    Log.e(TAG, "Cannot wait until idle, configuration change in progress.");
                    throw new ServiceSpecificException(10, "Cannot wait until idle, configuration change in progress.");
                }
            }
            this.mLegacyDevice.waitUntilIdle();
            return;
        }
        Log.e(TAG, "Cannot wait until idle, device has been closed.");
        throw new ServiceSpecificException(4, "Cannot wait until idle, device has been closed.");
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public long flush() {
        if (!this.mLegacyDevice.isClosed()) {
            synchronized (this.mConfigureLock) {
                if (this.mConfiguring) {
                    Log.e(TAG, "Cannot flush, configuration change in progress.");
                    throw new ServiceSpecificException(10, "Cannot flush, configuration change in progress.");
                }
            }
            return this.mLegacyDevice.flush();
        }
        Log.e(TAG, "Cannot flush, device has been closed.");
        throw new ServiceSpecificException(4, "Cannot flush, device has been closed.");
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public void prepare(int streamId) {
        if (!this.mLegacyDevice.isClosed()) {
            this.mCameraCallbacks.onPrepared(streamId);
        } else {
            Log.e(TAG, "Cannot prepare stream, device has been closed.");
            throw new ServiceSpecificException(4, "Cannot prepare stream, device has been closed.");
        }
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public void prepare2(int maxCount, int streamId) {
        prepare(streamId);
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public void tearDown(int streamId) {
        if (this.mLegacyDevice.isClosed()) {
            Log.e(TAG, "Cannot tear down stream, device has been closed.");
            throw new ServiceSpecificException(4, "Cannot tear down stream, device has been closed.");
        }
    }

    @Override // android.os.IInterface
    public IBinder asBinder() {
        return null;
    }
}
