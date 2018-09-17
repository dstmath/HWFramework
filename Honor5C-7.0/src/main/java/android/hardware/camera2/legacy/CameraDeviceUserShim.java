package android.hardware.camera2.legacy;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.ICameraDeviceCallbacks;
import android.hardware.camera2.ICameraDeviceUser;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.impl.CaptureResultExtras;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.utils.SubmitInfo;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.security.keymaster.KeymasterArguments;
import android.system.OsConstants;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;

public class CameraDeviceUserShim implements ICameraDeviceUser {
    private static final boolean DEBUG = false;
    private static final int OPEN_CAMERA_TIMEOUT_MS = 5000;
    private static final String TAG = "CameraDeviceUserShim";
    private final CameraCallbackThread mCameraCallbacks;
    private final CameraCharacteristics mCameraCharacteristics;
    private final CameraLooper mCameraInit;
    private final Object mConfigureLock;
    private boolean mConfiguring;
    private final LegacyCameraDevice mLegacyDevice;
    private int mSurfaceIdCounter;
    private final SparseArray<Surface> mSurfaces;

    private static class CameraCallbackThread implements ICameraDeviceCallbacks {
        private static final int CAMERA_ERROR = 0;
        private static final int CAMERA_IDLE = 1;
        private static final int CAPTURE_STARTED = 2;
        private static final int PREPARED = 4;
        private static final int REPEATING_REQUEST_ERROR = 5;
        private static final int RESULT_RECEIVED = 3;
        private final ICameraDeviceCallbacks mCallbacks;
        private Handler mHandler;
        private final HandlerThread mHandlerThread;

        private class CallbackHandler extends Handler {
            public CallbackHandler(Looper l) {
                super(l);
            }

            public void handleMessage(Message msg) {
                try {
                    switch (msg.what) {
                        case CameraCallbackThread.CAMERA_ERROR /*0*/:
                            CameraCallbackThread.this.mCallbacks.onDeviceError(msg.arg1, msg.obj);
                            return;
                        case CameraCallbackThread.CAMERA_IDLE /*1*/:
                            CameraCallbackThread.this.mCallbacks.onDeviceIdle();
                            return;
                        case CameraCallbackThread.CAPTURE_STARTED /*2*/:
                            CaptureResultExtras resultExtras = (CaptureResultExtras) msg.obj;
                            CameraCallbackThread.this.mCallbacks.onCaptureStarted(resultExtras, ((((long) msg.arg2) & KeymasterArguments.UINT32_MAX_VALUE) << 32) | (((long) msg.arg1) & KeymasterArguments.UINT32_MAX_VALUE));
                            return;
                        case CameraCallbackThread.RESULT_RECEIVED /*3*/:
                            Object[] resultArray = msg.obj;
                            CameraCallbackThread.this.mCallbacks.onResultReceived(resultArray[CameraCallbackThread.CAMERA_ERROR], (CaptureResultExtras) resultArray[CameraCallbackThread.CAMERA_IDLE]);
                            return;
                        case CameraCallbackThread.PREPARED /*4*/:
                            CameraCallbackThread.this.mCallbacks.onPrepared(msg.arg1);
                            return;
                        case CameraCallbackThread.REPEATING_REQUEST_ERROR /*5*/:
                            CameraCallbackThread.this.mCallbacks.onRepeatingRequestError(((((long) msg.arg2) & KeymasterArguments.UINT32_MAX_VALUE) << 32) | (((long) msg.arg1) & KeymasterArguments.UINT32_MAX_VALUE));
                            return;
                        default:
                            throw new IllegalArgumentException("Unknown callback message " + msg.what);
                    }
                } catch (RemoteException e) {
                    throw new IllegalStateException("Received remote exception during camera callback " + msg.what, e);
                }
                throw new IllegalStateException("Received remote exception during camera callback " + msg.what, e);
            }
        }

        public CameraCallbackThread(ICameraDeviceCallbacks callbacks) {
            this.mCallbacks = callbacks;
            this.mHandlerThread = new HandlerThread("LegacyCameraCallback");
            this.mHandlerThread.start();
        }

        public void close() {
            this.mHandlerThread.quitSafely();
        }

        public void onDeviceError(int errorCode, CaptureResultExtras resultExtras) {
            getHandler().sendMessage(getHandler().obtainMessage(CAMERA_ERROR, errorCode, CAMERA_ERROR, resultExtras));
        }

        public void onDeviceIdle() {
            getHandler().sendMessage(getHandler().obtainMessage(CAMERA_IDLE));
        }

        public void onCaptureStarted(CaptureResultExtras resultExtras, long timestamp) {
            getHandler().sendMessage(getHandler().obtainMessage(CAPTURE_STARTED, (int) (timestamp & KeymasterArguments.UINT32_MAX_VALUE), (int) ((timestamp >> 32) & KeymasterArguments.UINT32_MAX_VALUE), resultExtras));
        }

        public void onResultReceived(CameraMetadataNative result, CaptureResultExtras resultExtras) {
            Object[] resultArray = new Object[CAPTURE_STARTED];
            resultArray[CAMERA_ERROR] = result;
            resultArray[CAMERA_IDLE] = resultExtras;
            getHandler().sendMessage(getHandler().obtainMessage(RESULT_RECEIVED, resultArray));
        }

        public void onPrepared(int streamId) {
            getHandler().sendMessage(getHandler().obtainMessage(PREPARED, streamId, CAMERA_ERROR));
        }

        public void onRepeatingRequestError(long lastFrameNumber) {
            getHandler().sendMessage(getHandler().obtainMessage(REPEATING_REQUEST_ERROR, (int) (lastFrameNumber & KeymasterArguments.UINT32_MAX_VALUE), (int) ((lastFrameNumber >> 32) & KeymasterArguments.UINT32_MAX_VALUE)));
        }

        public IBinder asBinder() {
            return null;
        }

        private Handler getHandler() {
            if (this.mHandler == null) {
                this.mHandler = new CallbackHandler(this.mHandlerThread.getLooper());
            }
            return this.mHandler;
        }
    }

    private static class CameraLooper implements Runnable, AutoCloseable {
        private final Camera mCamera;
        private final int mCameraId;
        private volatile int mInitErrors;
        private Looper mLooper;
        private final ConditionVariable mStartDone;
        private final Thread mThread;

        public CameraLooper(int cameraId) {
            this.mCamera = Camera.openUninitialized();
            this.mStartDone = new ConditionVariable();
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

        public void close() {
            if (this.mLooper != null) {
                this.mLooper.quitSafely();
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

    protected CameraDeviceUserShim(int cameraId, LegacyCameraDevice legacyCamera, CameraCharacteristics characteristics, CameraLooper cameraInit, CameraCallbackThread cameraCallbacks) {
        this.mConfigureLock = new Object();
        this.mLegacyDevice = legacyCamera;
        this.mConfiguring = DEBUG;
        this.mSurfaces = new SparseArray();
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

    public static CameraDeviceUserShim connectBinderShim(ICameraDeviceCallbacks callbacks, int cameraId) {
        CameraLooper init = new CameraLooper(cameraId);
        CameraCallbackThread threadCallbacks = new CameraCallbackThread(callbacks);
        int initErrors = init.waitForOpen(OPEN_CAMERA_TIMEOUT_MS);
        Camera legacyCamera = init.getCamera();
        LegacyExceptionUtils.throwOnServiceError(initErrors);
        legacyCamera.disableShutterSound();
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        try {
            CameraCharacteristics characteristics = LegacyMetadataMapper.createCharacteristics(legacyCamera.getParameters(), info);
            return new CameraDeviceUserShim(cameraId, new LegacyCameraDevice(cameraId, legacyCamera, characteristics, threadCallbacks), characteristics, init, threadCallbacks);
        } catch (RuntimeException e) {
            throw new ServiceSpecificException(10, "Unable to get initial parameters: " + e.getMessage());
        }
    }

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

    public SubmitInfo submitRequest(CaptureRequest request, boolean streaming) {
        if (this.mLegacyDevice.isClosed()) {
            String err = "Cannot submit request, device has been closed.";
            Log.e(TAG, err);
            throw new ServiceSpecificException(4, err);
        }
        synchronized (this.mConfigureLock) {
            if (this.mConfiguring) {
                err = "Cannot submit request, configuration change in progress.";
                Log.e(TAG, err);
                throw new ServiceSpecificException(10, err);
            }
        }
        return this.mLegacyDevice.submitRequest(request, streaming);
    }

    public SubmitInfo submitRequestList(CaptureRequest[] request, boolean streaming) {
        if (this.mLegacyDevice.isClosed()) {
            String err = "Cannot submit request list, device has been closed.";
            Log.e(TAG, err);
            throw new ServiceSpecificException(4, err);
        }
        synchronized (this.mConfigureLock) {
            if (this.mConfiguring) {
                err = "Cannot submit request, configuration change in progress.";
                Log.e(TAG, err);
                throw new ServiceSpecificException(10, err);
            }
        }
        return this.mLegacyDevice.submitRequestList(request, streaming);
    }

    public long cancelRequest(int requestId) {
        if (this.mLegacyDevice.isClosed()) {
            String err = "Cannot cancel request, device has been closed.";
            Log.e(TAG, err);
            throw new ServiceSpecificException(4, err);
        }
        synchronized (this.mConfigureLock) {
            if (this.mConfiguring) {
                err = "Cannot cancel request, configuration change in progress.";
                Log.e(TAG, err);
                throw new ServiceSpecificException(10, err);
            }
        }
        return this.mLegacyDevice.cancelRequest(requestId);
    }

    public void beginConfigure() {
        if (this.mLegacyDevice.isClosed()) {
            String err = "Cannot begin configure, device has been closed.";
            Log.e(TAG, err);
            throw new ServiceSpecificException(4, err);
        }
        synchronized (this.mConfigureLock) {
            if (this.mConfiguring) {
                err = "Cannot begin configure, configuration change already in progress.";
                Log.e(TAG, err);
                throw new ServiceSpecificException(10, err);
            }
            this.mConfiguring = true;
        }
    }

    public void endConfigure(boolean isConstrainedHighSpeed) {
        if (this.mLegacyDevice.isClosed()) {
            String err = "Cannot end configure, device has been closed.";
            Log.e(TAG, err);
            throw new ServiceSpecificException(4, err);
        }
        SparseArray sparseArray = null;
        synchronized (this.mConfigureLock) {
            if (this.mConfiguring) {
                if (this.mSurfaces != null) {
                    sparseArray = this.mSurfaces.clone();
                }
                this.mConfiguring = DEBUG;
            } else {
                err = "Cannot end configure, no configuration change in progress.";
                Log.e(TAG, err);
                throw new ServiceSpecificException(10, err);
            }
        }
        this.mLegacyDevice.configureOutputs(sparseArray);
    }

    public void deleteStream(int streamId) {
        if (this.mLegacyDevice.isClosed()) {
            String err = "Cannot delete stream, device has been closed.";
            Log.e(TAG, err);
            throw new ServiceSpecificException(4, err);
        }
        synchronized (this.mConfigureLock) {
            if (this.mConfiguring) {
                int index = this.mSurfaces.indexOfKey(streamId);
                if (index < 0) {
                    err = "Cannot delete stream, stream id " + streamId + " doesn't exist.";
                    Log.e(TAG, err);
                    throw new ServiceSpecificException(3, err);
                }
                this.mSurfaces.removeAt(index);
            } else {
                err = "Cannot delete stream, no configuration change in progress.";
                Log.e(TAG, err);
                throw new ServiceSpecificException(10, err);
            }
        }
    }

    public int createStream(OutputConfiguration outputConfiguration) {
        if (this.mLegacyDevice.isClosed()) {
            String err = "Cannot create stream, device has been closed.";
            Log.e(TAG, err);
            throw new ServiceSpecificException(4, err);
        }
        int id;
        synchronized (this.mConfigureLock) {
            if (!this.mConfiguring) {
                err = "Cannot create stream, beginConfigure hasn't been called yet.";
                Log.e(TAG, err);
                throw new ServiceSpecificException(10, err);
            } else if (outputConfiguration.getRotation() != 0) {
                err = "Cannot create stream, stream rotation is not supported.";
                Log.e(TAG, err);
                throw new ServiceSpecificException(3, err);
            } else {
                id = this.mSurfaceIdCounter + 1;
                this.mSurfaceIdCounter = id;
                this.mSurfaces.put(id, outputConfiguration.getSurface());
            }
        }
        return id;
    }

    public int createInputStream(int width, int height, int format) {
        String err = "Creating input stream is not supported on legacy devices";
        Log.e(TAG, err);
        throw new ServiceSpecificException(10, err);
    }

    public Surface getInputSurface() {
        String err = "Getting input surface is not supported on legacy devices";
        Log.e(TAG, err);
        throw new ServiceSpecificException(10, err);
    }

    public CameraMetadataNative createDefaultRequest(int templateId) {
        if (this.mLegacyDevice.isClosed()) {
            String err = "Cannot create default request, device has been closed.";
            Log.e(TAG, err);
            throw new ServiceSpecificException(4, err);
        }
        try {
            return LegacyMetadataMapper.createRequestTemplate(this.mCameraCharacteristics, templateId);
        } catch (IllegalArgumentException e) {
            err = "createDefaultRequest - invalid templateId specified";
            Log.e(TAG, err);
            throw new ServiceSpecificException(3, err);
        }
    }

    public CameraMetadataNative getCameraInfo() {
        Log.e(TAG, "getCameraInfo unimplemented.");
        return null;
    }

    public void waitUntilIdle() throws RemoteException {
        if (this.mLegacyDevice.isClosed()) {
            String err = "Cannot wait until idle, device has been closed.";
            Log.e(TAG, err);
            throw new ServiceSpecificException(4, err);
        }
        synchronized (this.mConfigureLock) {
            if (this.mConfiguring) {
                err = "Cannot wait until idle, configuration change in progress.";
                Log.e(TAG, err);
                throw new ServiceSpecificException(10, err);
            }
        }
        this.mLegacyDevice.waitUntilIdle();
    }

    public long flush() {
        if (this.mLegacyDevice.isClosed()) {
            String err = "Cannot flush, device has been closed.";
            Log.e(TAG, err);
            throw new ServiceSpecificException(4, err);
        }
        synchronized (this.mConfigureLock) {
            if (this.mConfiguring) {
                err = "Cannot flush, configuration change in progress.";
                Log.e(TAG, err);
                throw new ServiceSpecificException(10, err);
            }
        }
        return this.mLegacyDevice.flush();
    }

    public void prepare(int streamId) {
        if (this.mLegacyDevice.isClosed()) {
            String err = "Cannot prepare stream, device has been closed.";
            Log.e(TAG, err);
            throw new ServiceSpecificException(4, err);
        }
        this.mCameraCallbacks.onPrepared(streamId);
    }

    public void prepare2(int maxCount, int streamId) {
        prepare(streamId);
    }

    public void tearDown(int streamId) {
        if (this.mLegacyDevice.isClosed()) {
            String err = "Cannot tear down stream, device has been closed.";
            Log.e(TAG, err);
            throw new ServiceSpecificException(4, err);
        }
    }

    public IBinder asBinder() {
        return null;
    }
}
