package android.hardware.camera2.legacy;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.ICameraDeviceCallbacks;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.impl.CaptureResultExtras;
import android.hardware.camera2.impl.PhysicalCaptureResultInfo;
import android.hardware.camera2.legacy.CameraDeviceState;
import android.hardware.camera2.legacy.LegacyExceptionUtils;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.camera2.utils.ArrayUtils;
import android.hardware.camera2.utils.SubmitInfo;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.util.SparseArray;
import android.view.Surface;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class LegacyCameraDevice implements AutoCloseable {
    private static final boolean DEBUG = false;
    private static final int GRALLOC_USAGE_HW_COMPOSER = 2048;
    private static final int GRALLOC_USAGE_HW_RENDER = 512;
    private static final int GRALLOC_USAGE_HW_TEXTURE = 256;
    private static final int GRALLOC_USAGE_HW_VIDEO_ENCODER = 65536;
    private static final int GRALLOC_USAGE_RENDERSCRIPT = 1048576;
    private static final int GRALLOC_USAGE_SW_READ_OFTEN = 3;
    private static final int ILLEGAL_VALUE = -1;
    public static final int MAX_DIMEN_FOR_ROUNDING = 1920;
    public static final int NATIVE_WINDOW_SCALING_MODE_SCALE_TO_WINDOW = 1;
    private final String TAG;
    private final Handler mCallbackHandler;
    private final HandlerThread mCallbackHandlerThread = new HandlerThread("CallbackThread");
    private final int mCameraId;
    private boolean mClosed = false;
    private SparseArray<Surface> mConfiguredSurfaces;
    private final ICameraDeviceCallbacks mDeviceCallbacks;
    private final CameraDeviceState mDeviceState = new CameraDeviceState();
    private final ConditionVariable mIdle = new ConditionVariable(true);
    private final RequestThreadManager mRequestThreadManager;
    private final Handler mResultHandler;
    private final HandlerThread mResultThread = new HandlerThread("ResultThread");
    private final CameraDeviceState.CameraDeviceStateListener mStateListener = new CameraDeviceState.CameraDeviceStateListener() {
        /* class android.hardware.camera2.legacy.LegacyCameraDevice.AnonymousClass1 */

        @Override // android.hardware.camera2.legacy.CameraDeviceState.CameraDeviceStateListener
        public void onError(final int errorCode, Object errorArg, final RequestHolder holder) {
            if (errorCode == 0 || errorCode == 1 || errorCode == 2) {
                LegacyCameraDevice.this.mIdle.open();
            }
            final CaptureResultExtras extras = LegacyCameraDevice.this.getExtrasFromRequest(holder, errorCode, errorArg);
            LegacyCameraDevice.this.mResultHandler.post(new Runnable() {
                /* class android.hardware.camera2.legacy.LegacyCameraDevice.AnonymousClass1.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        LegacyCameraDevice.this.mDeviceCallbacks.onDeviceError(errorCode, extras);
                    } catch (RemoteException e) {
                        throw new IllegalStateException("Received remote exception during onCameraError callback: ", e);
                    }
                }
            });
        }

        @Override // android.hardware.camera2.legacy.CameraDeviceState.CameraDeviceStateListener
        public void onConfiguring() {
        }

        @Override // android.hardware.camera2.legacy.CameraDeviceState.CameraDeviceStateListener
        public void onIdle() {
            LegacyCameraDevice.this.mIdle.open();
            LegacyCameraDevice.this.mResultHandler.post(new Runnable() {
                /* class android.hardware.camera2.legacy.LegacyCameraDevice.AnonymousClass1.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        LegacyCameraDevice.this.mDeviceCallbacks.onDeviceIdle();
                    } catch (RemoteException e) {
                        throw new IllegalStateException("Received remote exception during onCameraIdle callback: ", e);
                    }
                }
            });
        }

        @Override // android.hardware.camera2.legacy.CameraDeviceState.CameraDeviceStateListener
        public void onBusy() {
            LegacyCameraDevice.this.mIdle.close();
        }

        @Override // android.hardware.camera2.legacy.CameraDeviceState.CameraDeviceStateListener
        public void onCaptureStarted(final RequestHolder holder, final long timestamp) {
            final CaptureResultExtras extras = LegacyCameraDevice.this.getExtrasFromRequest(holder);
            LegacyCameraDevice.this.mResultHandler.post(new Runnable() {
                /* class android.hardware.camera2.legacy.LegacyCameraDevice.AnonymousClass1.AnonymousClass3 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        LegacyCameraDevice.this.mDeviceCallbacks.onCaptureStarted(extras, timestamp);
                    } catch (RemoteException e) {
                        throw new IllegalStateException("Received remote exception during onCameraError callback: ", e);
                    }
                }
            });
        }

        @Override // android.hardware.camera2.legacy.CameraDeviceState.CameraDeviceStateListener
        public void onRequestQueueEmpty() {
            LegacyCameraDevice.this.mResultHandler.post(new Runnable() {
                /* class android.hardware.camera2.legacy.LegacyCameraDevice.AnonymousClass1.AnonymousClass4 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        LegacyCameraDevice.this.mDeviceCallbacks.onRequestQueueEmpty();
                    } catch (RemoteException e) {
                        throw new IllegalStateException("Received remote exception during onRequestQueueEmpty callback: ", e);
                    }
                }
            });
        }

        @Override // android.hardware.camera2.legacy.CameraDeviceState.CameraDeviceStateListener
        public void onCaptureResult(final CameraMetadataNative result, final RequestHolder holder) {
            final CaptureResultExtras extras = LegacyCameraDevice.this.getExtrasFromRequest(holder);
            LegacyCameraDevice.this.mResultHandler.post(new Runnable() {
                /* class android.hardware.camera2.legacy.LegacyCameraDevice.AnonymousClass1.AnonymousClass5 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        LegacyCameraDevice.this.mDeviceCallbacks.onResultReceived(result, extras, new PhysicalCaptureResultInfo[0]);
                    } catch (RemoteException e) {
                        throw new IllegalStateException("Received remote exception during onCameraError callback: ", e);
                    }
                }
            });
        }

        @Override // android.hardware.camera2.legacy.CameraDeviceState.CameraDeviceStateListener
        public void onRepeatingRequestError(final long lastFrameNumber, final int repeatingRequestId) {
            LegacyCameraDevice.this.mResultHandler.post(new Runnable() {
                /* class android.hardware.camera2.legacy.LegacyCameraDevice.AnonymousClass1.AnonymousClass6 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        LegacyCameraDevice.this.mDeviceCallbacks.onRepeatingRequestError(lastFrameNumber, repeatingRequestId);
                    } catch (RemoteException e) {
                        throw new IllegalStateException("Received remote exception during onRepeatingRequestError callback: ", e);
                    }
                }
            });
        }
    };
    private final CameraCharacteristics mStaticCharacteristics;

    private static native int nativeConnectSurface(Surface surface);

    private static native int nativeDetectSurfaceDataspace(Surface surface);

    private static native int nativeDetectSurfaceDimens(Surface surface, int[] iArr);

    private static native int nativeDetectSurfaceType(Surface surface);

    private static native int nativeDetectSurfaceUsageFlags(Surface surface);

    private static native int nativeDetectTextureDimens(SurfaceTexture surfaceTexture, int[] iArr);

    private static native int nativeDisconnectSurface(Surface surface);

    static native int nativeGetJpegFooterSize();

    private static native long nativeGetSurfaceId(Surface surface);

    private static native int nativeProduceFrame(Surface surface, byte[] bArr, int i, int i2, int i3);

    private static native int nativeSetNextTimestamp(Surface surface, long j);

    private static native int nativeSetScalingMode(Surface surface, int i);

    private static native int nativeSetSurfaceDimens(Surface surface, int i, int i2);

    private static native int nativeSetSurfaceFormat(Surface surface, int i);

    private static native int nativeSetSurfaceOrientation(Surface surface, int i, int i2);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CaptureResultExtras getExtrasFromRequest(RequestHolder holder) {
        return getExtrasFromRequest(holder, -1, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CaptureResultExtras getExtrasFromRequest(RequestHolder holder, int errorCode, Object errorArg) {
        int errorStreamId = -1;
        if (errorCode == 5) {
            int indexOfTarget = this.mConfiguredSurfaces.indexOfValue((Surface) errorArg);
            if (indexOfTarget < 0) {
                Log.e(this.TAG, "Buffer drop error reported for unknown Surface");
            } else {
                errorStreamId = this.mConfiguredSurfaces.keyAt(indexOfTarget);
            }
        }
        if (holder == null) {
            return new CaptureResultExtras(-1, -1, -1, -1, -1, -1, -1, null);
        }
        return new CaptureResultExtras(holder.getRequestId(), holder.getSubsequeceId(), 0, 0, holder.getFrameNumber(), 1, errorStreamId, null);
    }

    static boolean needsConversion(Surface s) throws LegacyExceptionUtils.BufferQueueAbandonedException {
        int nativeType = detectSurfaceType(s);
        return nativeType == 35 || nativeType == 842094169 || nativeType == 17;
    }

    public LegacyCameraDevice(int cameraId, Camera camera, CameraCharacteristics characteristics, ICameraDeviceCallbacks callbacks) {
        this.mCameraId = cameraId;
        this.mDeviceCallbacks = callbacks;
        this.TAG = String.format("CameraDevice-%d-LE", Integer.valueOf(this.mCameraId));
        this.mResultThread.start();
        this.mResultHandler = new Handler(this.mResultThread.getLooper());
        this.mCallbackHandlerThread.start();
        this.mCallbackHandler = new Handler(this.mCallbackHandlerThread.getLooper());
        this.mDeviceState.setCameraDeviceCallbacks(this.mCallbackHandler, this.mStateListener);
        this.mStaticCharacteristics = characteristics;
        this.mRequestThreadManager = new RequestThreadManager(cameraId, camera, characteristics, this.mDeviceState);
        this.mRequestThreadManager.start();
    }

    public int configureOutputs(SparseArray<Surface> outputs) {
        return configureOutputs(outputs, false);
    }

    public int configureOutputs(SparseArray<Surface> outputs, boolean validateSurfacesOnly) {
        List<Pair<Surface, Size>> sizedSurfaces = new ArrayList<>();
        if (outputs != null) {
            int count = outputs.size();
            for (int i = 0; i < count; i++) {
                Surface output = outputs.valueAt(i);
                if (output == null) {
                    Log.e(this.TAG, "configureOutputs - null outputs are not allowed");
                    return LegacyExceptionUtils.BAD_VALUE;
                } else if (!output.isValid()) {
                    Log.e(this.TAG, "configureOutputs - invalid output surfaces are not allowed");
                    return LegacyExceptionUtils.BAD_VALUE;
                } else {
                    StreamConfigurationMap streamConfigurations = (StreamConfigurationMap) this.mStaticCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    try {
                        Size s = getSurfaceSize(output);
                        int surfaceType = detectSurfaceType(output);
                        boolean flexibleConsumer = isFlexibleConsumer(output);
                        Size[] sizes = streamConfigurations.getOutputSizes(surfaceType);
                        if (sizes == null) {
                            if (surfaceType == 34) {
                                sizes = streamConfigurations.getOutputSizes(35);
                            } else if (surfaceType == 33) {
                                sizes = streamConfigurations.getOutputSizes(256);
                            }
                        }
                        if (!ArrayUtils.contains(sizes, s)) {
                            if (flexibleConsumer) {
                                Size findClosestSize = findClosestSize(s, sizes);
                                s = findClosestSize;
                                if (findClosestSize != null) {
                                    sizedSurfaces.add(new Pair<>(output, s));
                                }
                            }
                            String reason = sizes == null ? "format is invalid." : "size not in valid set: " + Arrays.toString(sizes);
                            if (s != null) {
                                Log.e(this.TAG, String.format("Surface with size (w=%d, h=%d) and format 0x%x is not valid, %s", Integer.valueOf(s.getWidth()), Integer.valueOf(s.getHeight()), Integer.valueOf(surfaceType), reason));
                            }
                            return LegacyExceptionUtils.BAD_VALUE;
                        }
                        sizedSurfaces.add(new Pair<>(output, s));
                        if (!validateSurfacesOnly) {
                            setSurfaceDimens(output, s.getWidth(), s.getHeight());
                        }
                    } catch (LegacyExceptionUtils.BufferQueueAbandonedException e) {
                        Log.e(this.TAG, "Surface bufferqueue is abandoned, cannot configure as output: ", e);
                        return LegacyExceptionUtils.BAD_VALUE;
                    }
                }
            }
        }
        if (validateSurfacesOnly) {
            return 0;
        }
        boolean success = false;
        if (this.mDeviceState.setConfiguring()) {
            this.mRequestThreadManager.configure(sizedSurfaces);
            success = this.mDeviceState.setIdle();
        }
        if (!success) {
            return LegacyExceptionUtils.INVALID_OPERATION;
        }
        this.mConfiguredSurfaces = outputs;
        return 0;
    }

    public SubmitInfo submitRequestList(CaptureRequest[] requestList, boolean repeating) {
        List<Long> surfaceIds;
        if (requestList == null || requestList.length == 0) {
            Log.e(this.TAG, "submitRequestList - Empty/null requests are not allowed");
            throw new ServiceSpecificException(LegacyExceptionUtils.BAD_VALUE, "submitRequestList - Empty/null requests are not allowed");
        }
        try {
            if (this.mConfiguredSurfaces == null) {
                surfaceIds = new ArrayList<>();
            } else {
                surfaceIds = getSurfaceIds(this.mConfiguredSurfaces);
            }
            for (CaptureRequest request : requestList) {
                if (!request.getTargets().isEmpty()) {
                    for (Surface surface : request.getTargets()) {
                        if (surface == null) {
                            Log.e(this.TAG, "submitRequestList - Null Surface targets are not allowed");
                            throw new ServiceSpecificException(LegacyExceptionUtils.BAD_VALUE, "submitRequestList - Null Surface targets are not allowed");
                        } else if (this.mConfiguredSurfaces == null) {
                            Log.e(this.TAG, "submitRequestList - must configure  device with valid surfaces before submitting requests");
                            throw new ServiceSpecificException(LegacyExceptionUtils.INVALID_OPERATION, "submitRequestList - must configure  device with valid surfaces before submitting requests");
                        } else if (!containsSurfaceId(surface, surfaceIds)) {
                            Log.e(this.TAG, "submitRequestList - cannot use a surface that wasn't configured");
                            throw new ServiceSpecificException(LegacyExceptionUtils.BAD_VALUE, "submitRequestList - cannot use a surface that wasn't configured");
                        }
                    }
                } else {
                    Log.e(this.TAG, "submitRequestList - Each request must have at least one Surface target");
                    throw new ServiceSpecificException(LegacyExceptionUtils.BAD_VALUE, "submitRequestList - Each request must have at least one Surface target");
                }
            }
            this.mIdle.close();
            return this.mRequestThreadManager.submitCaptureRequests(requestList, repeating);
        } catch (LegacyExceptionUtils.BufferQueueAbandonedException e) {
            throw new ServiceSpecificException(LegacyExceptionUtils.BAD_VALUE, "submitRequestList - configured surface is abandoned.");
        }
    }

    public SubmitInfo submitRequest(CaptureRequest request, boolean repeating) {
        return submitRequestList(new CaptureRequest[]{request}, repeating);
    }

    public long cancelRequest(int requestId) {
        return this.mRequestThreadManager.cancelRepeating(requestId);
    }

    public void waitUntilIdle() {
        this.mIdle.block();
    }

    public long flush() {
        long lastFrame = this.mRequestThreadManager.flush();
        waitUntilIdle();
        return lastFrame;
    }

    public boolean isClosed() {
        return this.mClosed;
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        this.mRequestThreadManager.quit();
        this.mCallbackHandlerThread.quitSafely();
        this.mResultThread.quitSafely();
        try {
            this.mCallbackHandlerThread.join();
        } catch (InterruptedException e) {
            Log.e(this.TAG, String.format("Thread %s (%d) interrupted while quitting.", this.mCallbackHandlerThread.getName(), Long.valueOf(this.mCallbackHandlerThread.getId())));
        }
        try {
            this.mResultThread.join();
        } catch (InterruptedException e2) {
            Log.e(this.TAG, String.format("Thread %s (%d) interrupted while quitting.", this.mResultThread.getName(), Long.valueOf(this.mResultThread.getId())));
        }
        this.mClosed = true;
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        try {
            close();
        } catch (ServiceSpecificException e) {
            String str = this.TAG;
            Log.e(str, "Got error while trying to finalize, ignoring: " + e.getMessage());
        } catch (Throwable th) {
            super.finalize();
            throw th;
        }
        super.finalize();
    }

    static long findEuclidDistSquare(Size a, Size b) {
        long d0 = (long) (a.getWidth() - b.getWidth());
        long d1 = (long) (a.getHeight() - b.getHeight());
        return (d0 * d0) + (d1 * d1);
    }

    static Size findClosestSize(Size size, Size[] supportedSizes) {
        if (size == null || supportedSizes == null) {
            return null;
        }
        Size bestSize = null;
        for (Size s : supportedSizes) {
            if (s.equals(size)) {
                return size;
            }
            if (s.getWidth() <= 1920 && (bestSize == null || findEuclidDistSquare(size, s) < findEuclidDistSquare(bestSize, s))) {
                bestSize = s;
            }
        }
        return bestSize;
    }

    public static Size getSurfaceSize(Surface surface) throws LegacyExceptionUtils.BufferQueueAbandonedException {
        Preconditions.checkNotNull(surface);
        int[] dimens = new int[2];
        LegacyExceptionUtils.throwOnError(nativeDetectSurfaceDimens(surface, dimens));
        return new Size(dimens[0], dimens[1]);
    }

    public static boolean isFlexibleConsumer(Surface output) {
        int usageFlags = detectSurfaceUsageFlags(output);
        return (usageFlags & 1114112) == 0 && (usageFlags & 2307) != 0;
    }

    public static boolean isPreviewConsumer(Surface output) {
        int usageFlags = detectSurfaceUsageFlags(output);
        boolean previewConsumer = (usageFlags & 1114115) == 0 && (usageFlags & 2816) != 0;
        try {
            detectSurfaceType(output);
            return previewConsumer;
        } catch (LegacyExceptionUtils.BufferQueueAbandonedException e) {
            throw new IllegalArgumentException("Surface was abandoned", e);
        }
    }

    public static boolean isVideoEncoderConsumer(Surface output) {
        int usageFlags = detectSurfaceUsageFlags(output);
        boolean videoEncoderConsumer = (usageFlags & 1050883) == 0 && (usageFlags & 65536) != 0;
        try {
            detectSurfaceType(output);
            return videoEncoderConsumer;
        } catch (LegacyExceptionUtils.BufferQueueAbandonedException e) {
            throw new IllegalArgumentException("Surface was abandoned", e);
        }
    }

    static int detectSurfaceUsageFlags(Surface surface) {
        Preconditions.checkNotNull(surface);
        return nativeDetectSurfaceUsageFlags(surface);
    }

    public static int detectSurfaceType(Surface surface) throws LegacyExceptionUtils.BufferQueueAbandonedException {
        Preconditions.checkNotNull(surface);
        int surfaceType = nativeDetectSurfaceType(surface);
        if (surfaceType >= 1 && surfaceType <= 5) {
            surfaceType = 34;
        }
        return LegacyExceptionUtils.throwOnError(surfaceType);
    }

    public static int detectSurfaceDataspace(Surface surface) throws LegacyExceptionUtils.BufferQueueAbandonedException {
        Preconditions.checkNotNull(surface);
        return LegacyExceptionUtils.throwOnError(nativeDetectSurfaceDataspace(surface));
    }

    static void connectSurface(Surface surface) throws LegacyExceptionUtils.BufferQueueAbandonedException {
        Preconditions.checkNotNull(surface);
        LegacyExceptionUtils.throwOnError(nativeConnectSurface(surface));
    }

    static void disconnectSurface(Surface surface) throws LegacyExceptionUtils.BufferQueueAbandonedException {
        if (surface != null) {
            LegacyExceptionUtils.throwOnError(nativeDisconnectSurface(surface));
        }
    }

    static void produceFrame(Surface surface, byte[] pixelBuffer, int width, int height, int pixelFormat) throws LegacyExceptionUtils.BufferQueueAbandonedException {
        Preconditions.checkNotNull(surface);
        Preconditions.checkNotNull(pixelBuffer);
        Preconditions.checkArgumentPositive(width, "width must be positive.");
        Preconditions.checkArgumentPositive(height, "height must be positive.");
        LegacyExceptionUtils.throwOnError(nativeProduceFrame(surface, pixelBuffer, width, height, pixelFormat));
    }

    static void setSurfaceFormat(Surface surface, int pixelFormat) throws LegacyExceptionUtils.BufferQueueAbandonedException {
        Preconditions.checkNotNull(surface);
        LegacyExceptionUtils.throwOnError(nativeSetSurfaceFormat(surface, pixelFormat));
    }

    static void setSurfaceDimens(Surface surface, int width, int height) throws LegacyExceptionUtils.BufferQueueAbandonedException {
        Preconditions.checkNotNull(surface);
        Preconditions.checkArgumentPositive(width, "width must be positive.");
        Preconditions.checkArgumentPositive(height, "height must be positive.");
        LegacyExceptionUtils.throwOnError(nativeSetSurfaceDimens(surface, width, height));
    }

    public static long getSurfaceId(Surface surface) throws LegacyExceptionUtils.BufferQueueAbandonedException {
        Preconditions.checkNotNull(surface);
        try {
            return nativeGetSurfaceId(surface);
        } catch (IllegalArgumentException e) {
            throw new LegacyExceptionUtils.BufferQueueAbandonedException();
        }
    }

    static List<Long> getSurfaceIds(SparseArray<Surface> surfaces) throws LegacyExceptionUtils.BufferQueueAbandonedException {
        if (surfaces != null) {
            List<Long> surfaceIds = new ArrayList<>();
            int count = surfaces.size();
            for (int i = 0; i < count; i++) {
                long id = getSurfaceId(surfaces.valueAt(i));
                if (id != 0) {
                    surfaceIds.add(Long.valueOf(id));
                } else {
                    throw new IllegalStateException("Configured surface had null native GraphicBufferProducer pointer!");
                }
            }
            return surfaceIds;
        }
        throw new NullPointerException("Null argument surfaces");
    }

    static List<Long> getSurfaceIds(Collection<Surface> surfaces) throws LegacyExceptionUtils.BufferQueueAbandonedException {
        if (surfaces != null) {
            List<Long> surfaceIds = new ArrayList<>();
            for (Surface s : surfaces) {
                long id = getSurfaceId(s);
                if (id != 0) {
                    surfaceIds.add(Long.valueOf(id));
                } else {
                    throw new IllegalStateException("Configured surface had null native GraphicBufferProducer pointer!");
                }
            }
            return surfaceIds;
        }
        throw new NullPointerException("Null argument surfaces");
    }

    static boolean containsSurfaceId(Surface s, Collection<Long> ids) {
        try {
            return ids.contains(Long.valueOf(getSurfaceId(s)));
        } catch (LegacyExceptionUtils.BufferQueueAbandonedException e) {
            return false;
        }
    }

    static void setSurfaceOrientation(Surface surface, int facing, int sensorOrientation) throws LegacyExceptionUtils.BufferQueueAbandonedException {
        Preconditions.checkNotNull(surface);
        LegacyExceptionUtils.throwOnError(nativeSetSurfaceOrientation(surface, facing, sensorOrientation));
    }

    static Size getTextureSize(SurfaceTexture surfaceTexture) throws LegacyExceptionUtils.BufferQueueAbandonedException {
        Preconditions.checkNotNull(surfaceTexture);
        int[] dimens = new int[2];
        LegacyExceptionUtils.throwOnError(nativeDetectTextureDimens(surfaceTexture, dimens));
        return new Size(dimens[0], dimens[1]);
    }

    static void setNextTimestamp(Surface surface, long timestamp) throws LegacyExceptionUtils.BufferQueueAbandonedException {
        Preconditions.checkNotNull(surface);
        LegacyExceptionUtils.throwOnError(nativeSetNextTimestamp(surface, timestamp));
    }

    static void setScalingMode(Surface surface, int mode) throws LegacyExceptionUtils.BufferQueueAbandonedException {
        Preconditions.checkNotNull(surface);
        LegacyExceptionUtils.throwOnError(nativeSetScalingMode(surface, mode));
    }
}
