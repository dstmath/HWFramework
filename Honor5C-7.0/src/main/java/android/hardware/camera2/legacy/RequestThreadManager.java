package android.hardware.camera2.legacy;

import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.hardware.Camera;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.legacy.LegacyExceptionUtils.BufferQueueAbandonedException;
import android.hardware.camera2.utils.SizeAreaComparator;
import android.hardware.camera2.utils.SubmitInfo;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.SystemClock;
import android.rms.HwSysResource;
import android.speech.tts.TextToSpeech;
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
    private final String TAG;
    private final List<Surface> mCallbackOutputs;
    private Camera mCamera;
    private final int mCameraId;
    private final CaptureCollector mCaptureCollector;
    private final CameraCharacteristics mCharacteristics;
    private final CameraDeviceState mDeviceState;
    private Surface mDummySurface;
    private SurfaceTexture mDummyTexture;
    private final ErrorCallback mErrorCallback;
    private final LegacyFaceDetectMapper mFaceDetectMapper;
    private final LegacyFocusStateMapper mFocusStateMapper;
    private GLThreadManager mGLThreadManager;
    private final Object mIdleLock;
    private Size mIntermediateBufferSize;
    private final PictureCallback mJpegCallback;
    private final ShutterCallback mJpegShutterCallback;
    private final List<Long> mJpegSurfaceIds;
    private LegacyRequest mLastRequest;
    private Parameters mParams;
    private final FpsCounter mPrevCounter;
    private final OnFrameAvailableListener mPreviewCallback;
    private final List<Surface> mPreviewOutputs;
    private boolean mPreviewRunning;
    private SurfaceTexture mPreviewTexture;
    private final AtomicBoolean mQuit;
    private final ConditionVariable mReceivedJpeg;
    private final FpsCounter mRequestCounter;
    private final Callback mRequestHandlerCb;
    private final RequestQueue mRequestQueue;
    private final RequestHandlerThread mRequestThread;

    private static class ConfigureHolder {
        public final ConditionVariable condition;
        public final Collection<Pair<Surface, Size>> surfaces;

        public ConfigureHolder(ConditionVariable condition, Collection<Pair<Surface, Size>> surfaces) {
            this.condition = condition;
            this.surfaces = surfaces;
        }
    }

    public static class FpsCounter {
        private static final long NANO_PER_SECOND = 1000000000;
        private static final String TAG = "FpsCounter";
        private int mFrameCount;
        private double mLastFps;
        private long mLastPrintTime;
        private long mLastTime;
        private final String mStreamType;

        public FpsCounter(String streamType) {
            this.mFrameCount = 0;
            this.mLastTime = 0;
            this.mLastPrintTime = 0;
            this.mLastFps = 0.0d;
            this.mStreamType = streamType;
        }

        public synchronized void countFrame() {
            this.mFrameCount += RequestThreadManager.MSG_CONFIGURE_OUTPUTS;
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
            this.mPreviewRunning = DEBUG;
        }
    }

    private void startPreview() {
        if (!this.mPreviewRunning) {
            this.mCamera.startPreview();
            this.mPreviewRunning = USE_BLOB_FORMAT_OVERRIDE;
        }
    }

    private void doJpegCapturePrepare(RequestHolder request) throws IOException {
        if (!this.mPreviewRunning) {
            createDummySurface();
            this.mCamera.setPreviewTexture(this.mDummyTexture);
            startPreview();
        }
    }

    private void doJpegCapture(RequestHolder request) {
        this.mCamera.takePicture(this.mJpegShutterCallback, null, this.mJpegCallback);
        this.mPreviewRunning = DEBUG;
    }

    private void doPreviewCapture(RequestHolder request) throws IOException {
        if (!this.mPreviewRunning) {
            if (this.mPreviewTexture == null) {
                throw new IllegalStateException("Preview capture called with no preview surfaces configured.");
            }
            this.mPreviewTexture.setDefaultBufferSize(this.mIntermediateBufferSize.getWidth(), this.mIntermediateBufferSize.getHeight());
            this.mCamera.setPreviewTexture(this.mPreviewTexture);
            startPreview();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void configureOutputs(Collection<Pair<Surface, Size>> outputs) {
        try {
            Surface s;
            stopPreview();
            try {
                this.mCamera.setPreviewTexture(null);
            } catch (IOException e) {
                Log.w(this.TAG, "Failed to clear prior SurfaceTexture, may cause GL deadlock: ", e);
            } catch (RuntimeException e2) {
                Log.e(this.TAG, "Received device exception in configure call: ", e2);
                this.mDeviceState.setError(MSG_CONFIGURE_OUTPUTS);
                return;
            }
            if (this.mGLThreadManager != null) {
                this.mGLThreadManager.waitUntilStarted();
                this.mGLThreadManager.ignoreNewFrames();
                this.mGLThreadManager.waitUntilIdle();
            }
            resetJpegSurfaceFormats(this.mCallbackOutputs);
            for (Surface s2 : this.mCallbackOutputs) {
                try {
                    LegacyCameraDevice.disconnectSurface(s2);
                } catch (BufferQueueAbandonedException e3) {
                    Log.w(this.TAG, "Surface abandoned, skipping...", e3);
                }
            }
            this.mPreviewOutputs.clear();
            this.mCallbackOutputs.clear();
            this.mJpegSurfaceIds.clear();
            this.mPreviewTexture = null;
            List<Size> previewOutputSizes = new ArrayList();
            List<Size> callbackOutputSizes = new ArrayList();
            int facing = ((Integer) this.mCharacteristics.get(CameraCharacteristics.LENS_FACING)).intValue();
            int orientation = ((Integer) this.mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue();
            if (outputs != null) {
                for (Pair<Surface, Size> outPair : outputs) {
                    s2 = (Surface) outPair.first;
                    Size outSize = outPair.second;
                    try {
                        int format = LegacyCameraDevice.detectSurfaceType(s2);
                        LegacyCameraDevice.setSurfaceOrientation(s2, facing, orientation);
                        switch (format) {
                            case HwSysResource.APPMNGMEMNORMALPERS /*33*/:
                                LegacyCameraDevice.setSurfaceFormat(s2, MSG_CONFIGURE_OUTPUTS);
                                this.mJpegSurfaceIds.add(Long.valueOf(LegacyCameraDevice.getSurfaceId(s2)));
                                this.mCallbackOutputs.add(s2);
                                callbackOutputSizes.add(outSize);
                                LegacyCameraDevice.connectSurface(s2);
                                break;
                            default:
                                LegacyCameraDevice.setScalingMode(s2, MSG_CONFIGURE_OUTPUTS);
                                this.mPreviewOutputs.add(s2);
                                previewOutputSizes.add(outSize);
                                break;
                        }
                    } catch (BufferQueueAbandonedException e32) {
                        Log.w(this.TAG, "Surface abandoned, skipping...", e32);
                    }
                }
            }
            try {
                this.mParams = this.mCamera.getParameters();
                int[] bestRange = getPhotoPreviewFpsRange(this.mParams.getSupportedPreviewFpsRange());
                this.mParams.setPreviewFpsRange(bestRange[0], bestRange[MSG_CONFIGURE_OUTPUTS]);
                Size smallestSupportedJpegSize = calculatePictureSize(this.mCallbackOutputs, callbackOutputSizes, this.mParams);
                if (previewOutputSizes.size() > 0) {
                    Size chosenJpegDimen;
                    Size largestOutput = SizeAreaComparator.findLargestByArea(previewOutputSizes);
                    Size largestJpegDimen = ParameterUtils.getLargestSupportedJpegSizeByArea(this.mParams);
                    if (smallestSupportedJpegSize != null) {
                        chosenJpegDimen = smallestSupportedJpegSize;
                    } else {
                        chosenJpegDimen = largestJpegDimen;
                    }
                    List<Size> supportedPreviewSizes = ParameterUtils.convertSizeList(this.mParams.getSupportedPreviewSizes());
                    long largestOutputArea = ((long) largestOutput.getHeight()) * ((long) largestOutput.getWidth());
                    Size bestPreviewDimen = SizeAreaComparator.findLargestByArea(supportedPreviewSizes);
                    for (Size s3 : supportedPreviewSizes) {
                        long currArea = (long) (s3.getWidth() * s3.getHeight());
                        long bestArea = (long) (bestPreviewDimen.getWidth() * bestPreviewDimen.getHeight());
                        if (checkAspectRatiosMatch(chosenJpegDimen, s3) && currArea < bestArea && currArea >= largestOutputArea) {
                            bestPreviewDimen = s3;
                        }
                    }
                    this.mIntermediateBufferSize = bestPreviewDimen;
                    this.mParams.setPreviewSize(this.mIntermediateBufferSize.getWidth(), this.mIntermediateBufferSize.getHeight());
                } else {
                    this.mIntermediateBufferSize = null;
                }
                if (smallestSupportedJpegSize != null) {
                    Log.i(this.TAG, "configureOutputs - set take picture size to " + smallestSupportedJpegSize);
                    this.mParams.setPictureSize(smallestSupportedJpegSize.getWidth(), smallestSupportedJpegSize.getHeight());
                }
                if (this.mGLThreadManager == null) {
                    this.mGLThreadManager = new GLThreadManager(this.mCameraId, facing, this.mDeviceState);
                    this.mGLThreadManager.start();
                }
                this.mGLThreadManager.waitUntilStarted();
                List<Pair<Surface, Size>> previews = new ArrayList();
                Iterator<Size> previewSizeIter = previewOutputSizes.iterator();
                for (Surface p : this.mPreviewOutputs) {
                    previews.add(new Pair(p, (Size) previewSizeIter.next()));
                }
                this.mGLThreadManager.setConfigurationAndWait(previews, this.mCaptureCollector);
                this.mGLThreadManager.allowNewFrames();
                this.mPreviewTexture = this.mGLThreadManager.getCurrentSurfaceTexture();
                if (this.mPreviewTexture != null) {
                    this.mPreviewTexture.setOnFrameAvailableListener(this.mPreviewCallback);
                }
                try {
                    this.mCamera.setParameters(this.mParams);
                } catch (RuntimeException e22) {
                    Log.e(this.TAG, "Received device exception while configuring: ", e22);
                    this.mDeviceState.setError(MSG_CONFIGURE_OUTPUTS);
                }
            } catch (RuntimeException e222) {
                Log.e(this.TAG, "Received device exception: ", e222);
                this.mDeviceState.setError(MSG_CONFIGURE_OUTPUTS);
            }
        } catch (RuntimeException e2222) {
            Log.e(this.TAG, "Received device exception in configure call: ", e2222);
            this.mDeviceState.setError(MSG_CONFIGURE_OUTPUTS);
        }
    }

    private void resetJpegSurfaceFormats(Collection<Surface> surfaces) {
        if (surfaces != null) {
            for (Surface s : surfaces) {
                if (s == null || !s.isValid()) {
                    Log.w(this.TAG, "Jpeg surface is invalid, skipping...");
                } else {
                    try {
                        LegacyCameraDevice.setSurfaceFormat(s, 33);
                    } catch (BufferQueueAbandonedException e) {
                        Log.w(this.TAG, "Surface abandoned, skipping...", e);
                    }
                }
            }
        }
    }

    private Size calculatePictureSize(List<Surface> callbackOutputs, List<Size> callbackSizes, Parameters params) {
        if (callbackOutputs.size() != callbackSizes.size()) {
            throw new IllegalStateException("Input collections must be same length");
        }
        List<Size> configuredJpegSizes = new ArrayList();
        Iterator<Size> sizeIterator = callbackSizes.iterator();
        for (Surface callbackSurface : callbackOutputs) {
            Size jpegSize = (Size) sizeIterator.next();
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
            if (jpegSize2.getWidth() > maxConfiguredJpegWidth) {
                maxConfiguredJpegWidth = jpegSize2.getWidth();
            }
            if (jpegSize2.getHeight() > maxConfiguredJpegHeight) {
                maxConfiguredJpegHeight = jpegSize2.getHeight();
            }
        }
        Size smallestBoundJpegSize = new Size(maxConfiguredJpegWidth, maxConfiguredJpegHeight);
        List<Size> supportedJpegSizes = ParameterUtils.convertSizeList(params.getSupportedPictureSizes());
        List<Size> candidateSupportedJpegSizes = new ArrayList();
        for (Size supportedJpegSize : supportedJpegSizes) {
            if (supportedJpegSize.getWidth() >= maxConfiguredJpegWidth && supportedJpegSize.getHeight() >= maxConfiguredJpegHeight) {
                candidateSupportedJpegSizes.add(supportedJpegSize);
            }
        }
        if (candidateSupportedJpegSizes.isEmpty()) {
            throw new AssertionError("Could not find any supported JPEG sizes large enough to fit " + smallestBoundJpegSize);
        }
        Size smallestSupportedJpegSize = (Size) Collections.min(candidateSupportedJpegSizes, new SizeAreaComparator());
        if (!smallestSupportedJpegSize.equals(smallestBoundJpegSize)) {
            String str = this.TAG;
            Object[] objArr = new Object[MSG_SUBMIT_CAPTURE_REQUEST];
            objArr[0] = smallestSupportedJpegSize;
            objArr[MSG_CONFIGURE_OUTPUTS] = smallestBoundJpegSize;
            Log.w(str, String.format("configureOutputs - Will need to crop picture %s into smallest bound size %s", objArr));
        }
        return smallestSupportedJpegSize;
    }

    private static boolean checkAspectRatiosMatch(Size a, Size b) {
        return Math.abs((((float) a.getWidth()) / ((float) a.getHeight())) - (((float) b.getWidth()) / ((float) b.getHeight()))) < ASPECT_RATIO_TOLERANCE ? USE_BLOB_FORMAT_OVERRIDE : DEBUG;
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
            int maxFps = rate[MSG_CONFIGURE_OUTPUTS];
            if (maxFps > bestMax || (maxFps == bestMax && minFps > bestMin)) {
                bestMin = minFps;
                bestMax = maxFps;
                bestIndex = index;
            }
            index += MSG_CONFIGURE_OUTPUTS;
        }
        return (int[]) frameRates.get(bestIndex);
    }

    public RequestThreadManager(int cameraId, Camera camera, CameraCharacteristics characteristics, CameraDeviceState deviceState) {
        this.mPreviewRunning = DEBUG;
        this.mPreviewOutputs = new ArrayList();
        this.mCallbackOutputs = new ArrayList();
        this.mJpegSurfaceIds = new ArrayList();
        this.mRequestQueue = new RequestQueue(this.mJpegSurfaceIds);
        this.mLastRequest = null;
        this.mIdleLock = new Object();
        this.mPrevCounter = new FpsCounter("Incoming Preview");
        this.mRequestCounter = new FpsCounter("Incoming Requests");
        this.mQuit = new AtomicBoolean(DEBUG);
        this.mErrorCallback = new ErrorCallback() {
            public void onError(int i, Camera camera) {
                switch (i) {
                    case RequestThreadManager.MSG_SUBMIT_CAPTURE_REQUEST /*2*/:
                        RequestThreadManager.this.flush();
                        RequestThreadManager.this.mDeviceState.setError(0);
                    default:
                        Log.e(RequestThreadManager.this.TAG, "Received error " + i + " from the Camera1 ErrorCallback");
                        RequestThreadManager.this.mDeviceState.setError(RequestThreadManager.MSG_CONFIGURE_OUTPUTS);
                }
            }
        };
        this.mReceivedJpeg = new ConditionVariable(DEBUG);
        this.mJpegCallback = new PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.i(RequestThreadManager.this.TAG, "Received jpeg.");
                Pair<RequestHolder, Long> captureInfo = RequestThreadManager.this.mCaptureCollector.jpegProduced();
                if (captureInfo == null || captureInfo.first == null) {
                    Log.e(RequestThreadManager.this.TAG, "Dropping jpeg frame.");
                    return;
                }
                RequestHolder holder = captureInfo.first;
                long timestamp = ((Long) captureInfo.second).longValue();
                for (Surface s : holder.getHolderTargets()) {
                    try {
                        if (LegacyCameraDevice.containsSurfaceId(s, RequestThreadManager.this.mJpegSurfaceIds)) {
                            Log.i(RequestThreadManager.this.TAG, "Producing jpeg buffer...");
                            int totalSize = ((data.length + LegacyCameraDevice.nativeGetJpegFooterSize()) + RequestThreadManager.MSG_CLEANUP) & -4;
                            LegacyCameraDevice.setNextTimestamp(s, timestamp);
                            LegacyCameraDevice.setSurfaceFormat(s, RequestThreadManager.MSG_CONFIGURE_OUTPUTS);
                            int dimen = (((int) Math.ceil(Math.sqrt((double) totalSize))) + 15) & -16;
                            LegacyCameraDevice.setSurfaceDimens(s, dimen, dimen);
                            LegacyCameraDevice.produceFrame(s, data, dimen, dimen, 33);
                        }
                    } catch (BufferQueueAbandonedException e) {
                        Log.w(RequestThreadManager.this.TAG, "Surface abandoned, dropping frame. ", e);
                    }
                }
                RequestThreadManager.this.mReceivedJpeg.open();
            }
        };
        this.mJpegShutterCallback = new ShutterCallback() {
            public void onShutter() {
                RequestThreadManager.this.mCaptureCollector.jpegCaptured(SystemClock.elapsedRealtimeNanos());
            }
        };
        this.mPreviewCallback = new OnFrameAvailableListener() {
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                RequestThreadManager.this.mGLThreadManager.queueNewFrame();
            }
        };
        this.mRequestHandlerCb = new Callback() {
            private boolean mCleanup;
            private final LegacyResultMapper mMapper;

            {
                this.mCleanup = RequestThreadManager.DEBUG;
                this.mMapper = new LegacyResultMapper();
            }

            public boolean handleMessage(Message msg) {
                if (this.mCleanup) {
                    return RequestThreadManager.USE_BLOB_FORMAT_OVERRIDE;
                }
                switch (msg.what) {
                    case TextToSpeech.LANG_MISSING_DATA /*-1*/:
                        break;
                    case RequestThreadManager.MSG_CONFIGURE_OUTPUTS /*1*/:
                        ConfigureHolder config = msg.obj;
                        Log.i(RequestThreadManager.this.TAG, "Configure outputs: " + (config.surfaces != null ? config.surfaces.size() : 0) + " surfaces configured.");
                        try {
                            if (!RequestThreadManager.this.mCaptureCollector.waitForEmpty(4000, TimeUnit.MILLISECONDS)) {
                                Log.e(RequestThreadManager.this.TAG, "Timed out while queueing configure request.");
                                RequestThreadManager.this.mCaptureCollector.failAll();
                            }
                            RequestThreadManager.this.configureOutputs(config.surfaces);
                            config.condition.open();
                            break;
                        } catch (InterruptedException e) {
                            Log.e(RequestThreadManager.this.TAG, "Interrupted while waiting for requests to complete.");
                            RequestThreadManager.this.mDeviceState.setError(RequestThreadManager.MSG_CONFIGURE_OUTPUTS);
                            break;
                        }
                    case RequestThreadManager.MSG_SUBMIT_CAPTURE_REQUEST /*2*/:
                        Handler handler = RequestThreadManager.this.mRequestThread.getHandler();
                        boolean anyRequestOutputAbandoned = RequestThreadManager.DEBUG;
                        Pair<BurstHolder, Long> nextBurst = RequestThreadManager.this.mRequestQueue.getNext();
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
                                    break;
                                }
                            } catch (InterruptedException e2) {
                                Log.e(RequestThreadManager.this.TAG, "Interrupted while waiting for requests to complete: ", e2);
                                RequestThreadManager.this.mDeviceState.setError(RequestThreadManager.MSG_CONFIGURE_OUTPUTS);
                                break;
                            }
                        }
                        if (nextBurst != null) {
                            handler.sendEmptyMessage(RequestThreadManager.MSG_SUBMIT_CAPTURE_REQUEST);
                        }
                        for (RequestHolder holder : ((BurstHolder) nextBurst.first).produceRequestHolders(((Long) nextBurst.second).longValue())) {
                            CaptureRequest request = holder.getRequest();
                            boolean paramsChanged = RequestThreadManager.DEBUG;
                            if (RequestThreadManager.this.mLastRequest == null || RequestThreadManager.this.mLastRequest.captureRequest != request) {
                                LegacyRequest legacyRequest = new LegacyRequest(RequestThreadManager.this.mCharacteristics, request, ParameterUtils.convertSize(RequestThreadManager.this.mParams.getPreviewSize()), RequestThreadManager.this.mParams);
                                LegacyMetadataMapper.convertRequestMetadata(legacyRequest);
                                if (!RequestThreadManager.this.mParams.same(legacyRequest.parameters)) {
                                    try {
                                        RequestThreadManager.this.mCamera.setParameters(legacyRequest.parameters);
                                        paramsChanged = RequestThreadManager.USE_BLOB_FORMAT_OVERRIDE;
                                        RequestThreadManager.this.mParams = legacyRequest.parameters;
                                    } catch (RuntimeException e3) {
                                        Log.e(RequestThreadManager.this.TAG, "Exception while setting camera parameters: ", e3);
                                        holder.failRequest();
                                        RequestThreadManager.this.mDeviceState.setCaptureStart(holder, 0, RequestThreadManager.MSG_CLEANUP);
                                    }
                                }
                                RequestThreadManager.this.mLastRequest = legacyRequest;
                            }
                            try {
                                if (RequestThreadManager.this.mCaptureCollector.queueRequest(holder, RequestThreadManager.this.mLastRequest, 4000, TimeUnit.MILLISECONDS)) {
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
                                    if (paramsChanged) {
                                        try {
                                            RequestThreadManager.this.mParams = RequestThreadManager.this.mCamera.getParameters();
                                            RequestThreadManager.this.mLastRequest.setParameters(RequestThreadManager.this.mParams);
                                        } catch (RuntimeException e32) {
                                            Log.e(RequestThreadManager.this.TAG, "Received device exception: ", e32);
                                            RequestThreadManager.this.mDeviceState.setError(RequestThreadManager.MSG_CONFIGURE_OUTPUTS);
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
                                            anyRequestOutputAbandoned = RequestThreadManager.USE_BLOB_FORMAT_OVERRIDE;
                                        }
                                    } catch (InterruptedException e22) {
                                        Log.e(RequestThreadManager.this.TAG, "Interrupted waiting for request completion: ", e22);
                                        RequestThreadManager.this.mDeviceState.setError(RequestThreadManager.MSG_CONFIGURE_OUTPUTS);
                                    }
                                } else {
                                    Log.e(RequestThreadManager.this.TAG, "Timed out while queueing capture request.");
                                    holder.failRequest();
                                    RequestThreadManager.this.mDeviceState.setCaptureStart(holder, 0, RequestThreadManager.MSG_CLEANUP);
                                }
                            } catch (IOException e4) {
                                Log.e(RequestThreadManager.this.TAG, "Received device exception during capture call: ", e4);
                                RequestThreadManager.this.mDeviceState.setError(RequestThreadManager.MSG_CONFIGURE_OUTPUTS);
                            } catch (InterruptedException e222) {
                                Log.e(RequestThreadManager.this.TAG, "Interrupted during capture: ", e222);
                                RequestThreadManager.this.mDeviceState.setError(RequestThreadManager.MSG_CONFIGURE_OUTPUTS);
                            } catch (RuntimeException e322) {
                                Log.e(RequestThreadManager.this.TAG, "Received device exception during capture call: ", e322);
                                RequestThreadManager.this.mDeviceState.setError(RequestThreadManager.MSG_CONFIGURE_OUTPUTS);
                            }
                        }
                        if (anyRequestOutputAbandoned && ((BurstHolder) nextBurst.first).isRepeating()) {
                            RequestThreadManager.this.mDeviceState.setRepeatingRequestError(RequestThreadManager.this.cancelRepeating(((BurstHolder) nextBurst.first).getRequestId()));
                            break;
                        }
                    case RequestThreadManager.MSG_CLEANUP /*3*/:
                        this.mCleanup = RequestThreadManager.USE_BLOB_FORMAT_OVERRIDE;
                        try {
                            if (!RequestThreadManager.this.mCaptureCollector.waitForEmpty(4000, TimeUnit.MILLISECONDS)) {
                                Log.e(RequestThreadManager.this.TAG, "Timed out while queueing cleanup request.");
                                RequestThreadManager.this.mCaptureCollector.failAll();
                            }
                        } catch (InterruptedException e2222) {
                            Log.e(RequestThreadManager.this.TAG, "Interrupted while waiting for requests to complete: ", e2222);
                            RequestThreadManager.this.mDeviceState.setError(RequestThreadManager.MSG_CONFIGURE_OUTPUTS);
                        }
                        if (RequestThreadManager.this.mGLThreadManager != null) {
                            RequestThreadManager.this.mGLThreadManager.quit();
                            RequestThreadManager.this.mGLThreadManager = null;
                        }
                        if (RequestThreadManager.this.mCamera != null) {
                            RequestThreadManager.this.mCamera.release();
                            RequestThreadManager.this.mCamera = null;
                        }
                        RequestThreadManager.this.resetJpegSurfaceFormats(RequestThreadManager.this.mCallbackOutputs);
                        break;
                    default:
                        throw new AssertionError("Unhandled message " + msg.what + " on RequestThread.");
                }
                return RequestThreadManager.USE_BLOB_FORMAT_OVERRIDE;
            }
        };
        this.mCamera = (Camera) Preconditions.checkNotNull(camera, "camera must not be null");
        this.mCameraId = cameraId;
        this.mCharacteristics = (CameraCharacteristics) Preconditions.checkNotNull(characteristics, "characteristics must not be null");
        Object[] objArr = new Object[MSG_CONFIGURE_OUTPUTS];
        objArr[0] = Integer.valueOf(cameraId);
        String name = String.format("RequestThread-%d", objArr);
        this.TAG = name;
        this.mDeviceState = (CameraDeviceState) Preconditions.checkNotNull(deviceState, "deviceState must not be null");
        this.mFocusStateMapper = new LegacyFocusStateMapper(this.mCamera);
        this.mFaceDetectMapper = new LegacyFaceDetectMapper(this.mCamera, this.mCharacteristics);
        this.mCaptureCollector = new CaptureCollector(MSG_SUBMIT_CAPTURE_REQUEST, this.mDeviceState);
        this.mRequestThread = new RequestHandlerThread(name, this.mRequestHandlerCb);
        this.mCamera.setErrorCallback(this.mErrorCallback);
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
        if (!this.mQuit.getAndSet(USE_BLOB_FORMAT_OVERRIDE)) {
            Handler handler = this.mRequestThread.waitAndGetHandler();
            handler.sendMessageAtFrontOfQueue(handler.obtainMessage(MSG_CLEANUP));
            this.mRequestThread.quitSafely();
            try {
                this.mRequestThread.join();
            } catch (InterruptedException e) {
                String str = this.TAG;
                Object[] objArr = new Object[MSG_SUBMIT_CAPTURE_REQUEST];
                objArr[0] = this.mRequestThread.getName();
                objArr[MSG_CONFIGURE_OUTPUTS] = Long.valueOf(this.mRequestThread.getId());
                Log.e(str, String.format("Thread %s (%d) interrupted while quitting.", objArr));
            }
        }
    }

    public SubmitInfo submitCaptureRequests(CaptureRequest[] requests, boolean repeating) {
        SubmitInfo info;
        Handler handler = this.mRequestThread.waitAndGetHandler();
        synchronized (this.mIdleLock) {
            info = this.mRequestQueue.submit(requests, repeating);
            handler.sendEmptyMessage(MSG_SUBMIT_CAPTURE_REQUEST);
        }
        return info;
    }

    public long cancelRepeating(int requestId) {
        return this.mRequestQueue.stopRepeating(requestId);
    }

    public void configure(Collection<Pair<Surface, Size>> outputs) {
        Handler handler = this.mRequestThread.waitAndGetHandler();
        ConditionVariable condition = new ConditionVariable(DEBUG);
        handler.sendMessage(handler.obtainMessage(MSG_CONFIGURE_OUTPUTS, 0, 0, new ConfigureHolder(condition, outputs)));
        condition.block();
    }
}
