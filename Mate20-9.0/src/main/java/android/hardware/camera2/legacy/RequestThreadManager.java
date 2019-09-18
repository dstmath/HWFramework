package android.hardware.camera2.legacy;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.legacy.LegacyExceptionUtils;
import android.hardware.camera2.legacy.RequestQueue;
import android.hardware.camera2.utils.SizeAreaComparator;
import android.hardware.camera2.utils.SubmitInfo;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.MutableLong;
import android.util.Pair;
import android.util.Size;
import android.view.Surface;
import com.android.internal.util.Preconditions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class RequestThreadManager {
    private static final float ASPECT_RATIO_TOLERANCE = 0.01f;
    private static final boolean DEBUG = false;
    private static final int JPEG_FRAME_TIMEOUT = 4000;
    private static final int MAX_IN_FLIGHT_REQUESTS = 2;
    private static final int MSG_CLEANUP = 3;
    private static final int MSG_CONFIGURE_OUTPUTS = 1;
    private static final int MSG_SUBMIT_CAPTURE_REQUEST = 2;
    private static final int PREVIEW_FRAME_TIMEOUT = 2000;
    private static final int REQUEST_COMPLETE_TIMEOUT = 4000;
    private static final boolean USE_BLOB_FORMAT_OVERRIDE = true;
    private static final boolean VERBOSE = false;
    /* access modifiers changed from: private */
    public final String TAG;
    /* access modifiers changed from: private */
    public final List<Surface> mCallbackOutputs = new ArrayList();
    /* access modifiers changed from: private */
    public Camera mCamera;
    private final int mCameraId;
    /* access modifiers changed from: private */
    public final CaptureCollector mCaptureCollector;
    /* access modifiers changed from: private */
    public final CameraCharacteristics mCharacteristics;
    /* access modifiers changed from: private */
    public final CameraDeviceState mDeviceState;
    private Surface mDummySurface;
    private SurfaceTexture mDummyTexture;
    private final Camera.ErrorCallback mErrorCallback = new Camera.ErrorCallback() {
        public void onError(int i, Camera camera) {
            switch (i) {
                case 2:
                    RequestThreadManager.this.flush();
                    RequestThreadManager.this.mDeviceState.setError(0);
                    return;
                case 3:
                    RequestThreadManager.this.flush();
                    RequestThreadManager.this.mDeviceState.setError(6);
                    return;
                default:
                    String access$100 = RequestThreadManager.this.TAG;
                    Log.e(access$100, "Received error " + i + " from the Camera1 ErrorCallback");
                    RequestThreadManager.this.mDeviceState.setError(1);
                    return;
            }
        }
    };
    /* access modifiers changed from: private */
    public final LegacyFaceDetectMapper mFaceDetectMapper;
    /* access modifiers changed from: private */
    public final LegacyFocusStateMapper mFocusStateMapper;
    /* access modifiers changed from: private */
    public GLThreadManager mGLThreadManager;
    /* access modifiers changed from: private */
    public final Object mIdleLock = new Object();
    private Size mIntermediateBufferSize;
    private final Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.i(RequestThreadManager.this.TAG, "Received jpeg.");
            Pair<RequestHolder, Long> captureInfo = RequestThreadManager.this.mCaptureCollector.jpegProduced();
            if (captureInfo == null || captureInfo.first == null) {
                Log.e(RequestThreadManager.this.TAG, "Dropping jpeg frame.");
                return;
            }
            long timestamp = ((Long) captureInfo.second).longValue();
            for (Surface s : ((RequestHolder) captureInfo.first).getHolderTargets()) {
                try {
                    if (LegacyCameraDevice.containsSurfaceId(s, RequestThreadManager.this.mJpegSurfaceIds)) {
                        Log.i(RequestThreadManager.this.TAG, "Producing jpeg buffer...");
                        LegacyCameraDevice.setNextTimestamp(s, timestamp);
                        LegacyCameraDevice.setSurfaceFormat(s, 1);
                        int dimen = (((int) Math.ceil(Math.sqrt((double) ((data.length + LegacyCameraDevice.nativeGetJpegFooterSize() + 3) & -4)))) + 15) & -16;
                        LegacyCameraDevice.setSurfaceDimens(s, dimen, dimen);
                        LegacyCameraDevice.produceFrame(s, data, dimen, dimen, 33);
                    }
                } catch (LegacyExceptionUtils.BufferQueueAbandonedException e) {
                    Log.w(RequestThreadManager.this.TAG, "Surface abandoned, dropping frame. ", e);
                }
            }
            RequestThreadManager.this.mReceivedJpeg.open();
        }
    };
    private final Camera.ShutterCallback mJpegShutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            RequestThreadManager.this.mCaptureCollector.jpegCaptured(SystemClock.elapsedRealtimeNanos());
        }
    };
    /* access modifiers changed from: private */
    public final List<Long> mJpegSurfaceIds = new ArrayList();
    /* access modifiers changed from: private */
    public LegacyRequest mLastRequest = null;
    /* access modifiers changed from: private */
    public Camera.Parameters mParams;
    private final FpsCounter mPrevCounter = new FpsCounter("Incoming Preview");
    private final SurfaceTexture.OnFrameAvailableListener mPreviewCallback = new SurfaceTexture.OnFrameAvailableListener() {
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            RequestThreadManager.this.mGLThreadManager.queueNewFrame();
        }
    };
    private final List<Surface> mPreviewOutputs = new ArrayList();
    private boolean mPreviewRunning = false;
    private SurfaceTexture mPreviewTexture;
    private final AtomicBoolean mQuit = new AtomicBoolean(false);
    /* access modifiers changed from: private */
    public final ConditionVariable mReceivedJpeg = new ConditionVariable(false);
    private final FpsCounter mRequestCounter = new FpsCounter("Incoming Requests");
    private final Handler.Callback mRequestHandlerCb = new Handler.Callback() {
        private boolean mCleanup = false;
        private final LegacyResultMapper mMapper = new LegacyResultMapper();

        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean handleMessage(Message msg) {
            boolean z;
            long startTime;
            CaptureRequest request;
            RequestHolder holder;
            Message message = msg;
            if (this.mCleanup) {
                return true;
            }
            long startTime2 = 0;
            int i = message.what;
            if (i != -1) {
                switch (i) {
                    case 1:
                        ConfigureHolder config = (ConfigureHolder) message.obj;
                        int sizes = config.surfaces != null ? config.surfaces.size() : 0;
                        Log.i(RequestThreadManager.this.TAG, "Configure outputs: " + sizes + " surfaces configured.");
                        try {
                            if (!RequestThreadManager.this.mCaptureCollector.waitForEmpty(4000, TimeUnit.MILLISECONDS)) {
                                Log.e(RequestThreadManager.this.TAG, "Timed out while queueing configure request.");
                                RequestThreadManager.this.mCaptureCollector.failAll();
                            }
                            RequestThreadManager.this.configureOutputs(config.surfaces);
                            config.condition.open();
                        } catch (InterruptedException e) {
                            Log.e(RequestThreadManager.this.TAG, "Interrupted while waiting for requests to complete.");
                            z = true;
                            RequestThreadManager.this.mDeviceState.setError(1);
                            break;
                        }
                    case 2:
                        Handler handler = RequestThreadManager.this.mRequestThread.getHandler();
                        boolean anyRequestOutputAbandoned = false;
                        RequestQueue.RequestQueueEntry nextBurst = RequestThreadManager.this.mRequestQueue.getNext();
                        if (nextBurst == null) {
                            try {
                                if (!RequestThreadManager.this.mCaptureCollector.waitForEmpty(4000, TimeUnit.MILLISECONDS)) {
                                    Log.e(RequestThreadManager.this.TAG, "Timed out while waiting for prior requests to complete.");
                                    RequestThreadManager.this.mCaptureCollector.failAll();
                                }
                                synchronized (RequestThreadManager.this.mIdleLock) {
                                    nextBurst = RequestThreadManager.this.mRequestQueue.getNext();
                                    if (nextBurst == null) {
                                        RequestThreadManager.this.mDeviceState.setIdle();
                                        break;
                                    }
                                }
                            } catch (InterruptedException e2) {
                                Log.e(RequestThreadManager.this.TAG, "Interrupted while waiting for requests to complete: ", e2);
                                RequestThreadManager.this.mDeviceState.setError(1);
                                break;
                            }
                        }
                        if (nextBurst != null) {
                            handler.sendEmptyMessage(2);
                            if (nextBurst.isQueueEmpty()) {
                                RequestThreadManager.this.mDeviceState.setRequestQueueEmpty();
                            }
                        }
                        BurstHolder burstHolder = nextBurst.getBurstHolder();
                        for (RequestHolder holder2 : burstHolder.produceRequestHolders(nextBurst.getFrameNumber().longValue())) {
                            CaptureRequest request2 = holder2.getRequest();
                            boolean paramsChanged = false;
                            if (RequestThreadManager.this.mLastRequest == null || RequestThreadManager.this.mLastRequest.captureRequest != request2) {
                                Size previewSize = ParameterUtils.convertSize(RequestThreadManager.this.mParams.getPreviewSize());
                                LegacyRequest legacyRequest = new LegacyRequest(RequestThreadManager.this.mCharacteristics, request2, previewSize, RequestThreadManager.this.mParams);
                                LegacyMetadataMapper.convertRequestMetadata(legacyRequest);
                                if (!RequestThreadManager.this.mParams.same(legacyRequest.parameters)) {
                                    try {
                                        RequestThreadManager.this.mCamera.setParameters(legacyRequest.parameters);
                                        Camera.Parameters unused = RequestThreadManager.this.mParams = legacyRequest.parameters;
                                        Size size = previewSize;
                                        startTime = startTime2;
                                        paramsChanged = true;
                                    } catch (RuntimeException e3) {
                                        Size size2 = previewSize;
                                        Log.e(RequestThreadManager.this.TAG, "Exception while setting camera parameters: ", e3);
                                        holder2.failRequest();
                                        startTime = startTime2;
                                        RequestThreadManager.this.mDeviceState.setCaptureStart(holder2, 0, 3);
                                    }
                                } else {
                                    startTime = startTime2;
                                }
                                LegacyRequest unused2 = RequestThreadManager.this.mLastRequest = legacyRequest;
                            } else {
                                startTime = startTime2;
                            }
                            boolean paramsChanged2 = paramsChanged;
                            try {
                                request = request2;
                                holder = holder2;
                            } catch (IOException e4) {
                                e = e4;
                                CaptureRequest captureRequest = request2;
                                RequestHolder requestHolder = holder2;
                                Log.e(RequestThreadManager.this.TAG, "Received device exception during capture call: ", e);
                                RequestThreadManager.this.mDeviceState.setError(1);
                                RequestThreadManager.this.mDeviceState.setRepeatingRequestError(RequestThreadManager.this.cancelRepeating(burstHolder.getRequestId()), burstHolder.getRequestId());
                                z = true;
                                return z;
                            } catch (InterruptedException e5) {
                                e = e5;
                                CaptureRequest captureRequest2 = request2;
                                RequestHolder requestHolder2 = holder2;
                                Log.e(RequestThreadManager.this.TAG, "Interrupted during capture: ", e);
                                RequestThreadManager.this.mDeviceState.setError(1);
                                RequestThreadManager.this.mDeviceState.setRepeatingRequestError(RequestThreadManager.this.cancelRepeating(burstHolder.getRequestId()), burstHolder.getRequestId());
                                z = true;
                                return z;
                            } catch (RuntimeException e6) {
                                e = e6;
                                CaptureRequest captureRequest3 = request2;
                                RequestHolder requestHolder3 = holder2;
                                Log.e(RequestThreadManager.this.TAG, "Received device exception during capture call: ", e);
                                RequestThreadManager.this.mDeviceState.setError(1);
                                RequestThreadManager.this.mDeviceState.setRepeatingRequestError(RequestThreadManager.this.cancelRepeating(burstHolder.getRequestId()), burstHolder.getRequestId());
                                z = true;
                                return z;
                            }
                            try {
                                if (!RequestThreadManager.this.mCaptureCollector.queueRequest(holder2, RequestThreadManager.this.mLastRequest, 4000, TimeUnit.MILLISECONDS)) {
                                    Log.e(RequestThreadManager.this.TAG, "Timed out while queueing capture request.");
                                    holder.failRequest();
                                    RequestThreadManager.this.mDeviceState.setCaptureStart(holder, 0, 3);
                                } else {
                                    if (holder.hasPreviewTargets()) {
                                        RequestThreadManager.this.doPreviewCapture(holder);
                                    }
                                    if (holder.hasJpegTargets()) {
                                        while (!RequestThreadManager.this.mCaptureCollector.waitForPreviewsEmpty(2000, TimeUnit.MILLISECONDS)) {
                                            Log.e(RequestThreadManager.this.TAG, "Timed out while waiting for preview requests to complete.");
                                            RequestThreadManager.this.mCaptureCollector.failNextPreview();
                                        }
                                        RequestThreadManager.this.mReceivedJpeg.close();
                                        RequestThreadManager.this.doJpegCapturePrepare(holder);
                                    }
                                    RequestThreadManager.this.mFaceDetectMapper.processFaceDetectMode(request, RequestThreadManager.this.mParams);
                                    RequestThreadManager.this.mFocusStateMapper.processRequestTriggers(request, RequestThreadManager.this.mParams);
                                    if (holder.hasJpegTargets()) {
                                        RequestThreadManager.this.doJpegCapture(holder);
                                        if (!RequestThreadManager.this.mReceivedJpeg.block(4000)) {
                                            Log.e(RequestThreadManager.this.TAG, "Hit timeout for jpeg callback!");
                                            RequestThreadManager.this.mCaptureCollector.failNextJpeg();
                                        }
                                    }
                                    if (paramsChanged2) {
                                        try {
                                            Camera.Parameters unused3 = RequestThreadManager.this.mParams = RequestThreadManager.this.mCamera.getParameters();
                                            RequestThreadManager.this.mLastRequest.setParameters(RequestThreadManager.this.mParams);
                                        } catch (RuntimeException e7) {
                                            Log.e(RequestThreadManager.this.TAG, "Received device exception: ", e7);
                                            RequestThreadManager.this.mDeviceState.setError(1);
                                        }
                                    }
                                    MutableLong timestampMutable = new MutableLong(0);
                                    try {
                                        if (!RequestThreadManager.this.mCaptureCollector.waitForRequestCompleted(holder, 4000, TimeUnit.MILLISECONDS, timestampMutable)) {
                                            Log.e(RequestThreadManager.this.TAG, "Timed out while waiting for request to complete.");
                                            RequestThreadManager.this.mCaptureCollector.failAll();
                                        }
                                        CameraMetadataNative result = this.mMapper.cachedConvertResultMetadata(RequestThreadManager.this.mLastRequest, timestampMutable.value);
                                        RequestThreadManager.this.mFocusStateMapper.mapResultTriggers(result);
                                        RequestThreadManager.this.mFaceDetectMapper.mapResultFaces(result, RequestThreadManager.this.mLastRequest);
                                        if (!holder.requestFailed()) {
                                            RequestThreadManager.this.mDeviceState.setCaptureResult(holder, result);
                                        }
                                        if (holder.isOutputAbandoned()) {
                                            anyRequestOutputAbandoned = true;
                                        }
                                    } catch (InterruptedException e8) {
                                        Log.e(RequestThreadManager.this.TAG, "Interrupted waiting for request completion: ", e8);
                                        RequestThreadManager.this.mDeviceState.setError(1);
                                    }
                                }
                                startTime2 = startTime;
                            } catch (IOException e9) {
                                e = e9;
                                Log.e(RequestThreadManager.this.TAG, "Received device exception during capture call: ", e);
                                RequestThreadManager.this.mDeviceState.setError(1);
                                RequestThreadManager.this.mDeviceState.setRepeatingRequestError(RequestThreadManager.this.cancelRepeating(burstHolder.getRequestId()), burstHolder.getRequestId());
                                z = true;
                                return z;
                            } catch (InterruptedException e10) {
                                e = e10;
                                Log.e(RequestThreadManager.this.TAG, "Interrupted during capture: ", e);
                                RequestThreadManager.this.mDeviceState.setError(1);
                                RequestThreadManager.this.mDeviceState.setRepeatingRequestError(RequestThreadManager.this.cancelRepeating(burstHolder.getRequestId()), burstHolder.getRequestId());
                                z = true;
                                return z;
                            } catch (RuntimeException e11) {
                                e = e11;
                                Log.e(RequestThreadManager.this.TAG, "Received device exception during capture call: ", e);
                                RequestThreadManager.this.mDeviceState.setError(1);
                                RequestThreadManager.this.mDeviceState.setRepeatingRequestError(RequestThreadManager.this.cancelRepeating(burstHolder.getRequestId()), burstHolder.getRequestId());
                                z = true;
                                return z;
                            }
                        }
                        if (anyRequestOutputAbandoned && burstHolder.isRepeating()) {
                            RequestThreadManager.this.mDeviceState.setRepeatingRequestError(RequestThreadManager.this.cancelRepeating(burstHolder.getRequestId()), burstHolder.getRequestId());
                        }
                        z = true;
                        break;
                    case 3:
                        this.mCleanup = true;
                        try {
                            if (!RequestThreadManager.this.mCaptureCollector.waitForEmpty(4000, TimeUnit.MILLISECONDS)) {
                                Log.e(RequestThreadManager.this.TAG, "Timed out while queueing cleanup request.");
                                RequestThreadManager.this.mCaptureCollector.failAll();
                            }
                        } catch (InterruptedException e12) {
                            Log.e(RequestThreadManager.this.TAG, "Interrupted while waiting for requests to complete: ", e12);
                            RequestThreadManager.this.mDeviceState.setError(1);
                        }
                        if (RequestThreadManager.this.mGLThreadManager != null) {
                            RequestThreadManager.this.mGLThreadManager.quit();
                            GLThreadManager unused4 = RequestThreadManager.this.mGLThreadManager = null;
                        }
                        if (RequestThreadManager.this.mCamera != null) {
                            RequestThreadManager.this.mCamera.release();
                            Camera unused5 = RequestThreadManager.this.mCamera = null;
                        }
                        RequestThreadManager.this.resetJpegSurfaceFormats(RequestThreadManager.this.mCallbackOutputs);
                        break;
                    default:
                        throw new AssertionError("Unhandled message " + message.what + " on RequestThread.");
                }
            } else {
                z = true;
            }
            return z;
            z = true;
            return z;
        }
    };
    /* access modifiers changed from: private */
    public final RequestQueue mRequestQueue = new RequestQueue(this.mJpegSurfaceIds);
    /* access modifiers changed from: private */
    public final RequestHandlerThread mRequestThread;

    private static class ConfigureHolder {
        public final ConditionVariable condition;
        public final Collection<Pair<Surface, Size>> surfaces;

        public ConfigureHolder(ConditionVariable condition2, Collection<Pair<Surface, Size>> surfaces2) {
            this.condition = condition2;
            this.surfaces = surfaces2;
        }
    }

    public static class FpsCounter {
        private static final long NANO_PER_SECOND = 1000000000;
        private static final String TAG = "FpsCounter";
        private int mFrameCount = 0;
        private double mLastFps = 0.0d;
        private long mLastPrintTime = 0;
        private long mLastTime = 0;
        private final String mStreamType;

        public FpsCounter(String streamType) {
            this.mStreamType = streamType;
        }

        public synchronized void countFrame() {
            this.mFrameCount++;
            long nextTime = SystemClock.elapsedRealtimeNanos();
            if (this.mLastTime == 0) {
                this.mLastTime = nextTime;
            }
            if (nextTime > this.mLastTime + NANO_PER_SECOND) {
                this.mLastFps = ((double) this.mFrameCount) * (1.0E9d / ((double) (nextTime - this.mLastTime)));
                this.mFrameCount = 0;
                this.mLastTime = nextTime;
            }
        }

        public synchronized double checkFps() {
            return this.mLastFps;
        }

        public synchronized void staggeredLog() {
            if (this.mLastTime > this.mLastPrintTime + 5000000000L) {
                this.mLastPrintTime = this.mLastTime;
                Log.d(TAG, "FPS for " + this.mStreamType + " stream: " + this.mLastFps);
            }
        }

        public synchronized void countAndLog() {
            countFrame();
            staggeredLog();
        }
    }

    private void createDummySurface() {
        if (this.mDummyTexture == null || this.mDummySurface == null) {
            this.mDummyTexture = new SurfaceTexture(0);
            this.mDummyTexture.setDefaultBufferSize(640, 480);
            this.mDummySurface = new Surface(this.mDummyTexture);
        }
    }

    private void stopPreview() {
        if (this.mPreviewRunning) {
            this.mCamera.stopPreview();
            this.mPreviewRunning = false;
        }
    }

    private void startPreview() {
        if (!this.mPreviewRunning) {
            this.mCamera.startPreview();
            this.mPreviewRunning = true;
        }
    }

    /* access modifiers changed from: private */
    public void doJpegCapturePrepare(RequestHolder request) throws IOException {
        if (!this.mPreviewRunning) {
            createDummySurface();
            this.mCamera.setPreviewTexture(this.mDummyTexture);
            startPreview();
        }
    }

    /* access modifiers changed from: private */
    public void doJpegCapture(RequestHolder request) {
        this.mCamera.takePicture(this.mJpegShutterCallback, null, this.mJpegCallback);
        this.mPreviewRunning = false;
    }

    /* access modifiers changed from: private */
    public void doPreviewCapture(RequestHolder request) throws IOException {
        if (!this.mPreviewRunning) {
            if (this.mPreviewTexture != null) {
                this.mPreviewTexture.setDefaultBufferSize(this.mIntermediateBufferSize.getWidth(), this.mIntermediateBufferSize.getHeight());
                this.mCamera.setPreviewTexture(this.mPreviewTexture);
                startPreview();
                return;
            }
            throw new IllegalStateException("Preview capture called with no preview surfaces configured.");
        }
    }

    /* access modifiers changed from: private */
    public void configureOutputs(Collection<Pair<Surface, Size>> outputs) {
        List<Size> previewOutputSizes;
        try {
            stopPreview();
            try {
                this.mCamera.setPreviewTexture(null);
            } catch (IOException e) {
                Log.w(this.TAG, "Failed to clear prior SurfaceTexture, may cause GL deadlock: ", e);
            } catch (RuntimeException e2) {
                Log.e(this.TAG, "Received device exception in configure call: ", e2);
                this.mDeviceState.setError(1);
                return;
            }
            if (this.mGLThreadManager != null) {
                this.mGLThreadManager.waitUntilStarted();
                this.mGLThreadManager.ignoreNewFrames();
                this.mGLThreadManager.waitUntilIdle();
            }
            resetJpegSurfaceFormats(this.mCallbackOutputs);
            for (Surface s : this.mCallbackOutputs) {
                try {
                    LegacyCameraDevice.disconnectSurface(s);
                } catch (LegacyExceptionUtils.BufferQueueAbandonedException e3) {
                    LegacyExceptionUtils.BufferQueueAbandonedException bufferQueueAbandonedException = e3;
                    Log.w(this.TAG, "Surface abandoned, skipping...", e3);
                }
            }
            this.mPreviewOutputs.clear();
            this.mCallbackOutputs.clear();
            this.mJpegSurfaceIds.clear();
            this.mPreviewTexture = null;
            List<Size> previewOutputSizes2 = new ArrayList<>();
            List<Size> callbackOutputSizes = new ArrayList<>();
            int facing = ((Integer) this.mCharacteristics.get(CameraCharacteristics.LENS_FACING)).intValue();
            int orientation = ((Integer) this.mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue();
            if (outputs != null) {
                for (Pair<Surface, Size> outPair : outputs) {
                    Surface s2 = (Surface) outPair.first;
                    Size outSize = (Size) outPair.second;
                    try {
                        int format = LegacyCameraDevice.detectSurfaceType(s2);
                        LegacyCameraDevice.setSurfaceOrientation(s2, facing, orientation);
                        if (format != 33) {
                            LegacyCameraDevice.setScalingMode(s2, 1);
                            this.mPreviewOutputs.add(s2);
                            previewOutputSizes2.add(outSize);
                        } else {
                            LegacyCameraDevice.setSurfaceFormat(s2, 1);
                            this.mJpegSurfaceIds.add(Long.valueOf(LegacyCameraDevice.getSurfaceId(s2)));
                            this.mCallbackOutputs.add(s2);
                            callbackOutputSizes.add(outSize);
                            LegacyCameraDevice.connectSurface(s2);
                        }
                    } catch (LegacyExceptionUtils.BufferQueueAbandonedException e4) {
                        Log.w(this.TAG, "Surface abandoned, skipping...", e4);
                    }
                }
            }
            try {
                this.mParams = this.mCamera.getParameters();
                List<int[]> supportedFpsRanges = this.mParams.getSupportedPreviewFpsRange();
                int[] bestRange = getPhotoPreviewFpsRange(supportedFpsRanges);
                this.mParams.setPreviewFpsRange(bestRange[0], bestRange[1]);
                Size smallestSupportedJpegSize = calculatePictureSize(this.mCallbackOutputs, callbackOutputSizes, this.mParams);
                if (previewOutputSizes2.size() > 0) {
                    Size bestPreviewDimen = SizeAreaComparator.findLargestByArea(previewOutputSizes2);
                    Size largestJpegDimen = ParameterUtils.getLargestSupportedJpegSizeByArea(this.mParams);
                    Size chosenJpegDimen = smallestSupportedJpegSize != null ? smallestSupportedJpegSize : largestJpegDimen;
                    List<Size> supportedPreviewSizes = ParameterUtils.convertSizeList(this.mParams.getSupportedPreviewSizes());
                    Size size = largestJpegDimen;
                    long largestOutputArea = ((long) bestPreviewDimen.getHeight()) * ((long) bestPreviewDimen.getWidth());
                    Size bestPreviewDimen2 = SizeAreaComparator.findLargestByArea(supportedPreviewSizes);
                    Iterator<Size> it = supportedPreviewSizes.iterator();
                    while (it.hasNext()) {
                        Size largestOutput = bestPreviewDimen;
                        Size s3 = it.next();
                        Iterator<Size> it2 = it;
                        List<int[]> supportedFpsRanges2 = supportedFpsRanges;
                        long currArea = (long) (s3.getWidth() * s3.getHeight());
                        List<Size> previewOutputSizes3 = previewOutputSizes2;
                        long bestArea = (long) (bestPreviewDimen2.getWidth() * bestPreviewDimen2.getHeight());
                        if (checkAspectRatiosMatch(chosenJpegDimen, s3) && currArea < bestArea && currArea >= largestOutputArea) {
                            bestPreviewDimen2 = s3;
                        }
                        bestPreviewDimen = largestOutput;
                        it = it2;
                        supportedFpsRanges = supportedFpsRanges2;
                        previewOutputSizes2 = previewOutputSizes3;
                    }
                    Size largestOutput2 = bestPreviewDimen;
                    previewOutputSizes = previewOutputSizes2;
                    List<int[]> list = supportedFpsRanges;
                    this.mIntermediateBufferSize = bestPreviewDimen2;
                    this.mParams.setPreviewSize(this.mIntermediateBufferSize.getWidth(), this.mIntermediateBufferSize.getHeight());
                } else {
                    previewOutputSizes = previewOutputSizes2;
                    List<int[]> list2 = supportedFpsRanges;
                    this.mIntermediateBufferSize = null;
                }
                if (smallestSupportedJpegSize != null) {
                    String str = this.TAG;
                    Log.i(str, "configureOutputs - set take picture size to " + smallestSupportedJpegSize);
                    this.mParams.setPictureSize(smallestSupportedJpegSize.getWidth(), smallestSupportedJpegSize.getHeight());
                }
                if (this.mGLThreadManager == null) {
                    this.mGLThreadManager = new GLThreadManager(this.mCameraId, facing, this.mDeviceState);
                    this.mGLThreadManager.start();
                }
                this.mGLThreadManager.waitUntilStarted();
                List<Pair<Surface, Size>> previews = new ArrayList<>();
                Iterator<Size> previewSizeIter = previewOutputSizes.iterator();
                for (Surface p : this.mPreviewOutputs) {
                    previews.add(new Pair(p, previewSizeIter.next()));
                }
                this.mGLThreadManager.setConfigurationAndWait(previews, this.mCaptureCollector);
                for (Surface p2 : this.mPreviewOutputs) {
                    try {
                        LegacyCameraDevice.setSurfaceOrientation(p2, facing, orientation);
                    } catch (LegacyExceptionUtils.BufferQueueAbandonedException e5) {
                        LegacyExceptionUtils.BufferQueueAbandonedException bufferQueueAbandonedException2 = e5;
                        Log.e(this.TAG, "Surface abandoned, skipping setSurfaceOrientation()", e5);
                    }
                }
                this.mGLThreadManager.allowNewFrames();
                this.mPreviewTexture = this.mGLThreadManager.getCurrentSurfaceTexture();
                if (this.mPreviewTexture != null) {
                    this.mPreviewTexture.setOnFrameAvailableListener(this.mPreviewCallback);
                }
                try {
                    this.mCamera.setParameters(this.mParams);
                } catch (RuntimeException e6) {
                    Log.e(this.TAG, "Received device exception while configuring: ", e6);
                    this.mDeviceState.setError(1);
                }
            } catch (RuntimeException e7) {
                List<Size> list3 = previewOutputSizes2;
                Log.e(this.TAG, "Received device exception: ", e7);
                this.mDeviceState.setError(1);
            }
        } catch (RuntimeException e8) {
            RuntimeException runtimeException = e8;
            Log.e(this.TAG, "Received device exception in configure call: ", e8);
            this.mDeviceState.setError(1);
        }
    }

    /* access modifiers changed from: private */
    public void resetJpegSurfaceFormats(Collection<Surface> surfaces) {
        if (surfaces != null) {
            for (Surface s : surfaces) {
                if (s == null || !s.isValid()) {
                    Log.w(this.TAG, "Jpeg surface is invalid, skipping...");
                } else {
                    try {
                        LegacyCameraDevice.setSurfaceFormat(s, 33);
                    } catch (LegacyExceptionUtils.BufferQueueAbandonedException e) {
                        Log.w(this.TAG, "Surface abandoned, skipping...", e);
                    }
                }
            }
        }
    }

    private Size calculatePictureSize(List<Surface> callbackOutputs, List<Size> callbackSizes, Camera.Parameters params) {
        if (callbackOutputs.size() == callbackSizes.size()) {
            List<Size> configuredJpegSizes = new ArrayList<>();
            Iterator<Size> sizeIterator = callbackSizes.iterator();
            for (Surface callbackSurface : callbackOutputs) {
                Size jpegSize = sizeIterator.next();
                if (LegacyCameraDevice.containsSurfaceId(callbackSurface, this.mJpegSurfaceIds)) {
                    configuredJpegSizes.add(jpegSize);
                }
            }
            if (configuredJpegSizes.isEmpty()) {
                return null;
            }
            int maxConfiguredJpegWidth = -1;
            int maxConfiguredJpegHeight = -1;
            for (Size jpegSize2 : configuredJpegSizes) {
                maxConfiguredJpegWidth = jpegSize2.getWidth() > maxConfiguredJpegWidth ? jpegSize2.getWidth() : maxConfiguredJpegWidth;
                maxConfiguredJpegHeight = jpegSize2.getHeight() > maxConfiguredJpegHeight ? jpegSize2.getHeight() : maxConfiguredJpegHeight;
            }
            Size smallestBoundJpegSize = new Size(maxConfiguredJpegWidth, maxConfiguredJpegHeight);
            List<Size> supportedJpegSizes = ParameterUtils.convertSizeList(params.getSupportedPictureSizes());
            List<Size> candidateSupportedJpegSizes = new ArrayList<>();
            for (Size supportedJpegSize : supportedJpegSizes) {
                if (supportedJpegSize.getWidth() >= maxConfiguredJpegWidth && supportedJpegSize.getHeight() >= maxConfiguredJpegHeight) {
                    candidateSupportedJpegSizes.add(supportedJpegSize);
                }
            }
            if (!candidateSupportedJpegSizes.isEmpty()) {
                Size smallestSupportedJpegSize = (Size) Collections.min(candidateSupportedJpegSizes, new SizeAreaComparator());
                if (!smallestSupportedJpegSize.equals(smallestBoundJpegSize)) {
                    Log.w(this.TAG, String.format("configureOutputs - Will need to crop picture %s into smallest bound size %s", new Object[]{smallestSupportedJpegSize, smallestBoundJpegSize}));
                }
                return smallestSupportedJpegSize;
            }
            throw new AssertionError("Could not find any supported JPEG sizes large enough to fit " + smallestBoundJpegSize);
        }
        throw new IllegalStateException("Input collections must be same length");
    }

    private static boolean checkAspectRatiosMatch(Size a, Size b) {
        return Math.abs((((float) a.getWidth()) / ((float) a.getHeight())) - (((float) b.getWidth()) / ((float) b.getHeight()))) < ASPECT_RATIO_TOLERANCE;
    }

    private int[] getPhotoPreviewFpsRange(List<int[]> frameRates) {
        if (frameRates.size() == 0) {
            Log.e(this.TAG, "No supported frame rates returned!");
            return null;
        }
        int bestMin = 0;
        int bestMax = 0;
        int bestIndex = 0;
        int index = 0;
        for (int[] rate : frameRates) {
            int minFps = rate[0];
            int maxFps = rate[1];
            if (maxFps > bestMax || (maxFps == bestMax && minFps > bestMin)) {
                bestMin = minFps;
                bestMax = maxFps;
                bestIndex = index;
            }
            index++;
        }
        return frameRates.get(bestIndex);
    }

    public RequestThreadManager(int cameraId, Camera camera, CameraCharacteristics characteristics, CameraDeviceState deviceState) {
        this.mCamera = (Camera) Preconditions.checkNotNull(camera, "camera must not be null");
        this.mCameraId = cameraId;
        this.mCharacteristics = (CameraCharacteristics) Preconditions.checkNotNull(characteristics, "characteristics must not be null");
        String name = String.format("RequestThread-%d", new Object[]{Integer.valueOf(cameraId)});
        this.TAG = name;
        this.mDeviceState = (CameraDeviceState) Preconditions.checkNotNull(deviceState, "deviceState must not be null");
        this.mFocusStateMapper = new LegacyFocusStateMapper(this.mCamera);
        this.mFaceDetectMapper = new LegacyFaceDetectMapper(this.mCamera, this.mCharacteristics);
        this.mCaptureCollector = new CaptureCollector(2, this.mDeviceState);
        this.mRequestThread = new RequestHandlerThread(name, this.mRequestHandlerCb);
        this.mCamera.setDetailedErrorCallback(this.mErrorCallback);
    }

    public void start() {
        this.mRequestThread.start();
    }

    public long flush() {
        Log.i(this.TAG, "Flushing all pending requests.");
        long lastFrame = this.mRequestQueue.stopRepeating();
        this.mCaptureCollector.failAll();
        return lastFrame;
    }

    public void quit() {
        if (!this.mQuit.getAndSet(true)) {
            Handler handler = this.mRequestThread.waitAndGetHandler();
            handler.sendMessageAtFrontOfQueue(handler.obtainMessage(3));
            this.mRequestThread.quitSafely();
            try {
                this.mRequestThread.join();
            } catch (InterruptedException e) {
                Log.e(this.TAG, String.format("Thread %s (%d) interrupted while quitting.", new Object[]{this.mRequestThread.getName(), Long.valueOf(this.mRequestThread.getId())}));
            }
        }
    }

    public SubmitInfo submitCaptureRequests(CaptureRequest[] requests, boolean repeating) {
        SubmitInfo info;
        Handler handler = this.mRequestThread.waitAndGetHandler();
        synchronized (this.mIdleLock) {
            info = this.mRequestQueue.submit(requests, repeating);
            handler.sendEmptyMessage(2);
        }
        return info;
    }

    public long cancelRepeating(int requestId) {
        return this.mRequestQueue.stopRepeating(requestId);
    }

    public void configure(Collection<Pair<Surface, Size>> outputs) {
        Handler handler = this.mRequestThread.waitAndGetHandler();
        ConditionVariable condition = new ConditionVariable(false);
        handler.sendMessage(handler.obtainMessage(1, 0, 0, new ConfigureHolder(condition, outputs)));
        condition.block();
    }
}
