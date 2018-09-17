package android.hardware.camera2.impl;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.ICameraDeviceCallbacks.Stub;
import android.hardware.camera2.ICameraDeviceUser;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.InputConfiguration;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.camera2.utils.SubmitInfo;
import android.hardware.camera2.utils.SurfaceUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseArray;
import android.view.Surface;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class CameraDeviceImpl extends CameraDevice implements DeathRecipient {
    private static final long NANO_PER_SECOND = 1000000000;
    private static final int REQUEST_ID_NONE = -1;
    private final boolean DEBUG = false;
    private final String TAG;
    private final int mAppTargetSdkVersion;
    private final Runnable mCallOnActive = new Runnable() {
        /* JADX WARNING: Missing block: B:10:0x0017, code:
            if (r0 == null) goto L_0x001e;
     */
        /* JADX WARNING: Missing block: B:11:0x0019, code:
            r0.onActive(r3.this$0);
     */
        /* JADX WARNING: Missing block: B:12:0x001e, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice == null) {
                    return;
                }
                StateCallbackKK sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
            }
        }
    };
    private final Runnable mCallOnBusy = new Runnable() {
        /* JADX WARNING: Missing block: B:10:0x0017, code:
            if (r0 == null) goto L_0x001e;
     */
        /* JADX WARNING: Missing block: B:11:0x0019, code:
            r0.onBusy(r3.this$0);
     */
        /* JADX WARNING: Missing block: B:12:0x001e, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice == null) {
                    return;
                }
                StateCallbackKK sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
            }
        }
    };
    private final Runnable mCallOnClosed = new Runnable() {
        private boolean mClosedOnce = false;

        public void run() {
            if (this.mClosedOnce) {
                throw new AssertionError("Don't post #onClosed more than once");
            }
            StateCallbackKK sessionCallback;
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
            }
            if (sessionCallback != null) {
                sessionCallback.onClosed(CameraDeviceImpl.this);
            }
            CameraDeviceImpl.this.mDeviceCallback.onClosed(CameraDeviceImpl.this);
            this.mClosedOnce = true;
        }
    };
    private final Runnable mCallOnDisconnected = new Runnable() {
        /* JADX WARNING: Missing block: B:10:0x0017, code:
            if (r0 == null) goto L_0x001e;
     */
        /* JADX WARNING: Missing block: B:11:0x0019, code:
            r0.onDisconnected(r3.this$0);
     */
        /* JADX WARNING: Missing block: B:12:0x001e, code:
            android.hardware.camera2.impl.CameraDeviceImpl.-get6(r3.this$0).onDisconnected(r3.this$0);
     */
        /* JADX WARNING: Missing block: B:13:0x0029, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice == null) {
                    return;
                }
                StateCallbackKK sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
            }
        }
    };
    private final Runnable mCallOnIdle = new Runnable() {
        /* JADX WARNING: Missing block: B:10:0x0017, code:
            if (r0 == null) goto L_0x001e;
     */
        /* JADX WARNING: Missing block: B:11:0x0019, code:
            r0.onIdle(r3.this$0);
     */
        /* JADX WARNING: Missing block: B:12:0x001e, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice == null) {
                    return;
                }
                StateCallbackKK sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
            }
        }
    };
    private final Runnable mCallOnOpened = new Runnable() {
        /* JADX WARNING: Missing block: B:10:0x0017, code:
            if (r0 == null) goto L_0x001e;
     */
        /* JADX WARNING: Missing block: B:11:0x0019, code:
            r0.onOpened(r3.this$0);
     */
        /* JADX WARNING: Missing block: B:12:0x001e, code:
            android.hardware.camera2.impl.CameraDeviceImpl.-get6(r3.this$0).onOpened(r3.this$0);
     */
        /* JADX WARNING: Missing block: B:13:0x0029, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice == null) {
                    return;
                }
                StateCallbackKK sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
            }
        }
    };
    private final Runnable mCallOnUnconfigured = new Runnable() {
        /* JADX WARNING: Missing block: B:10:0x0017, code:
            if (r0 == null) goto L_0x001e;
     */
        /* JADX WARNING: Missing block: B:11:0x0019, code:
            r0.onUnconfigured(r3.this$0);
     */
        /* JADX WARNING: Missing block: B:12:0x001e, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice == null) {
                    return;
                }
                StateCallbackKK sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
            }
        }
    };
    private final CameraDeviceCallbacks mCallbacks = new CameraDeviceCallbacks();
    private final String mCameraId;
    private final SparseArray<CaptureCallbackHolder> mCaptureCallbackMap = new SparseArray();
    private final CameraCharacteristics mCharacteristics;
    private final AtomicBoolean mClosing = new AtomicBoolean();
    private SimpleEntry<Integer, InputConfiguration> mConfiguredInput = new SimpleEntry(Integer.valueOf(-1), null);
    private final SparseArray<OutputConfiguration> mConfiguredOutputs = new SparseArray();
    private CameraCaptureSessionCore mCurrentSession;
    private final StateCallback mDeviceCallback;
    private final Handler mDeviceHandler;
    private final FrameNumberTracker mFrameNumberTracker = new FrameNumberTracker();
    private boolean mIdle = true;
    private boolean mInError = false;
    final Object mInterfaceLock = new Object();
    private int mNextSessionId = 0;
    private ICameraDeviceUserWrapper mRemoteDevice;
    private int mRepeatingRequestId = -1;
    private final List<RequestLastFrameNumbersHolder> mRequestLastFrameNumbersList = new ArrayList();
    private volatile StateCallbackKK mSessionStateCallback;
    private final int mTotalPartialCount;

    public interface CaptureCallback {
        public static final int NO_FRAMES_CAPTURED = -1;

        void onCaptureBufferLost(CameraDevice cameraDevice, CaptureRequest captureRequest, Surface surface, long j);

        void onCaptureCompleted(CameraDevice cameraDevice, CaptureRequest captureRequest, TotalCaptureResult totalCaptureResult);

        void onCaptureFailed(CameraDevice cameraDevice, CaptureRequest captureRequest, CaptureFailure captureFailure);

        void onCapturePartial(CameraDevice cameraDevice, CaptureRequest captureRequest, CaptureResult captureResult);

        void onCaptureProgressed(CameraDevice cameraDevice, CaptureRequest captureRequest, CaptureResult captureResult);

        void onCaptureSequenceAborted(CameraDevice cameraDevice, int i);

        void onCaptureSequenceCompleted(CameraDevice cameraDevice, int i, long j);

        void onCaptureStarted(CameraDevice cameraDevice, CaptureRequest captureRequest, long j, long j2);
    }

    public static abstract class StateCallbackKK extends StateCallback {
        public void onUnconfigured(CameraDevice camera) {
        }

        public void onActive(CameraDevice camera) {
        }

        public void onBusy(CameraDevice camera) {
        }

        public void onIdle(CameraDevice camera) {
        }

        public void onRequestQueueEmpty() {
        }

        public void onSurfacePrepared(Surface surface) {
        }
    }

    public class CameraDeviceCallbacks extends Stub {
        public IBinder asBinder() {
            return this;
        }

        /* JADX WARNING: Missing block: B:15:0x0048, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onDeviceError(int errorCode, CaptureResultExtras resultExtras) {
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice == null) {
                    return;
                }
                int publicErrorCode;
                switch (errorCode) {
                    case 0:
                        CameraDeviceImpl.this.mDeviceHandler.post(CameraDeviceImpl.this.mCallOnDisconnected);
                        break;
                    case 1:
                    case 2:
                        break;
                    case 3:
                    case 4:
                    case 5:
                        onCaptureErrorLocked(errorCode, resultExtras);
                        break;
                    default:
                        Log.e(CameraDeviceImpl.this.TAG, "Unknown error from camera device: " + errorCode);
                        break;
                }
                CameraDeviceImpl.this.mInError = true;
                if (errorCode == 1) {
                    publicErrorCode = 4;
                } else {
                    publicErrorCode = 5;
                }
                CameraDeviceImpl.this.mDeviceHandler.post(new Runnable() {
                    public void run() {
                        if (!CameraDeviceImpl.this.isClosed()) {
                            CameraDeviceImpl.this.mDeviceCallback.onError(CameraDeviceImpl.this, publicErrorCode);
                        }
                    }
                });
            }
        }

        /* JADX WARNING: Missing block: B:8:0x0017, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onRepeatingRequestError(long lastFrameNumber) {
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice == null || CameraDeviceImpl.this.mRepeatingRequestId == -1) {
                } else {
                    CameraDeviceImpl.this.checkEarlyTriggerSequenceComplete(CameraDeviceImpl.this.mRepeatingRequestId, lastFrameNumber);
                    CameraDeviceImpl.this.mRepeatingRequestId = -1;
                }
            }
        }

        public void onDeviceIdle() {
            Log.i(CameraDeviceImpl.this.TAG, "Camera now idle");
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice == null) {
                    return;
                }
                if (!CameraDeviceImpl.this.mIdle) {
                    CameraDeviceImpl.this.mDeviceHandler.post(CameraDeviceImpl.this.mCallOnIdle);
                }
                CameraDeviceImpl.this.mIdle = true;
            }
        }

        public void onCaptureStarted(CaptureResultExtras resultExtras, long timestamp) {
            int requestId = resultExtras.getRequestId();
            final long frameNumber = resultExtras.getFrameNumber();
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice == null) {
                    return;
                }
                final CaptureCallbackHolder holder = (CaptureCallbackHolder) CameraDeviceImpl.this.mCaptureCallbackMap.get(requestId);
                if (holder == null) {
                } else if (CameraDeviceImpl.this.isClosed()) {
                } else {
                    final CaptureResultExtras captureResultExtras = resultExtras;
                    final long j = timestamp;
                    holder.getHandler().post(new Runnable() {
                        public void run() {
                            if (!CameraDeviceImpl.this.isClosed()) {
                                int subsequenceId = captureResultExtras.getSubsequenceId();
                                CaptureRequest request = holder.getRequest(subsequenceId);
                                if (holder.hasBatchedOutputs()) {
                                    Range<Integer> fpsRange = (Range) request.get(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE);
                                    for (int i = 0; i < holder.getRequestCount(); i++) {
                                        holder.getCallback().onCaptureStarted(CameraDeviceImpl.this, holder.getRequest(i), j - ((CameraDeviceImpl.NANO_PER_SECOND * ((long) (subsequenceId - i))) / ((long) ((Integer) fpsRange.getUpper()).intValue())), frameNumber - ((long) (subsequenceId - i)));
                                    }
                                    return;
                                }
                                holder.getCallback().onCaptureStarted(CameraDeviceImpl.this, holder.getRequest(captureResultExtras.getSubsequenceId()), j, frameNumber);
                            }
                        }
                    });
                }
            }
        }

        /* JADX WARNING: Missing block: B:34:0x00c6, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onResultReceived(CameraMetadataNative result, CaptureResultExtras resultExtras) throws RemoteException {
            int requestId = resultExtras.getRequestId();
            long frameNumber = resultExtras.getFrameNumber();
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice == null) {
                    return;
                }
                result.set(CameraCharacteristics.LENS_INFO_SHADING_MAP_SIZE, (Size) CameraDeviceImpl.this.getCharacteristics().get(CameraCharacteristics.LENS_INFO_SHADING_MAP_SIZE));
                final CaptureCallbackHolder holder = (CaptureCallbackHolder) CameraDeviceImpl.this.mCaptureCallbackMap.get(requestId);
                final CaptureRequest request = holder.getRequest(resultExtras.getSubsequenceId());
                boolean isPartialResult = resultExtras.getPartialResultCount() < CameraDeviceImpl.this.mTotalPartialCount;
                boolean isReprocess = request.isReprocess();
                if (holder == null) {
                    CameraDeviceImpl.this.mFrameNumberTracker.updateTracker(frameNumber, null, isPartialResult, isReprocess);
                } else if (CameraDeviceImpl.this.isClosed()) {
                    CameraDeviceImpl.this.mFrameNumberTracker.updateTracker(frameNumber, null, isPartialResult, isReprocess);
                } else {
                    CameraMetadataNative resultCopy;
                    Runnable resultDispatch;
                    CaptureResult finalResult;
                    if (holder.hasBatchedOutputs()) {
                        resultCopy = new CameraMetadataNative(result);
                    } else {
                        resultCopy = null;
                    }
                    if (isPartialResult) {
                        final CaptureResult resultAsCapture = new CaptureResult(result, request, resultExtras);
                        final CaptureResultExtras captureResultExtras = resultExtras;
                        resultDispatch = new Runnable() {
                            public void run() {
                                if (!CameraDeviceImpl.this.isClosed()) {
                                    if (holder.hasBatchedOutputs()) {
                                        for (int i = 0; i < holder.getRequestCount(); i++) {
                                            holder.getCallback().onCaptureProgressed(CameraDeviceImpl.this, holder.getRequest(i), new CaptureResult(new CameraMetadataNative(resultCopy), holder.getRequest(i), captureResultExtras));
                                        }
                                        return;
                                    }
                                    holder.getCallback().onCaptureProgressed(CameraDeviceImpl.this, request, resultAsCapture);
                                }
                            }
                        };
                        finalResult = resultAsCapture;
                    } else {
                        List<CaptureResult> partialResults = CameraDeviceImpl.this.mFrameNumberTracker.popPartialResults(frameNumber);
                        final long sensorTimestamp = ((Long) result.get(CaptureResult.SENSOR_TIMESTAMP)).longValue();
                        final Range<Integer> fpsRange = (Range) request.get(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE);
                        final int subsequenceId = resultExtras.getSubsequenceId();
                        CaptureResult resultAsCapture2 = new TotalCaptureResult(result, request, resultExtras, partialResults, holder.getSessionId());
                        final CaptureCallbackHolder captureCallbackHolder = holder;
                        final CameraMetadataNative cameraMetadataNative = resultCopy;
                        final CaptureResultExtras captureResultExtras2 = resultExtras;
                        final List<CaptureResult> list = partialResults;
                        final CaptureRequest captureRequest = request;
                        final CaptureResult captureResult = resultAsCapture2;
                        Runnable anonymousClass4 = new Runnable() {
                            public void run() {
                                if (!CameraDeviceImpl.this.isClosed()) {
                                    if (captureCallbackHolder.hasBatchedOutputs()) {
                                        for (int i = 0; i < captureCallbackHolder.getRequestCount(); i++) {
                                            cameraMetadataNative.set(CaptureResult.SENSOR_TIMESTAMP, Long.valueOf(sensorTimestamp - ((((long) (subsequenceId - i)) * CameraDeviceImpl.NANO_PER_SECOND) / ((long) ((Integer) fpsRange.getUpper()).intValue()))));
                                            captureCallbackHolder.getCallback().onCaptureCompleted(CameraDeviceImpl.this, captureCallbackHolder.getRequest(i), new TotalCaptureResult(new CameraMetadataNative(cameraMetadataNative), captureCallbackHolder.getRequest(i), captureResultExtras2, list, captureCallbackHolder.getSessionId()));
                                        }
                                        return;
                                    }
                                    captureCallbackHolder.getCallback().onCaptureCompleted(CameraDeviceImpl.this, captureRequest, captureResult);
                                }
                            }
                        };
                        finalResult = resultAsCapture2;
                    }
                    holder.getHandler().post(resultDispatch);
                    CameraDeviceImpl.this.mFrameNumberTracker.updateTracker(frameNumber, finalResult, isPartialResult, isReprocess);
                    if (!isPartialResult) {
                        CameraDeviceImpl.this.checkAndFireSequenceComplete();
                    }
                }
            }
        }

        public void onPrepared(int streamId) {
            OutputConfiguration output;
            StateCallbackKK sessionCallback;
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                output = (OutputConfiguration) CameraDeviceImpl.this.mConfiguredOutputs.get(streamId);
                sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
            }
            if (sessionCallback != null) {
                if (output == null) {
                    Log.w(CameraDeviceImpl.this.TAG, "onPrepared invoked for unknown output Surface");
                    return;
                }
                for (Surface surface : output.getSurfaces()) {
                    sessionCallback.onSurfacePrepared(surface);
                }
            }
        }

        public void onRequestQueueEmpty() {
            StateCallbackKK sessionCallback;
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
            }
            if (sessionCallback != null) {
                sessionCallback.onRequestQueueEmpty();
            }
        }

        private void onCaptureErrorLocked(int errorCode, CaptureResultExtras resultExtras) {
            int requestId = resultExtras.getRequestId();
            int subsequenceId = resultExtras.getSubsequenceId();
            final long frameNumber = resultExtras.getFrameNumber();
            final CaptureCallbackHolder holder = (CaptureCallbackHolder) CameraDeviceImpl.this.mCaptureCallbackMap.get(requestId);
            final CaptureRequest request = holder.getRequest(subsequenceId);
            if (errorCode == 5) {
                for (final Surface surface : ((OutputConfiguration) CameraDeviceImpl.this.mConfiguredOutputs.get(resultExtras.getErrorStreamId())).getSurfaces()) {
                    if (request.containsTarget(surface)) {
                        holder.getHandler().post(new Runnable() {
                            public void run() {
                                if (!CameraDeviceImpl.this.isClosed()) {
                                    holder.getCallback().onCaptureBufferLost(CameraDeviceImpl.this, request, surface, frameNumber);
                                }
                            }
                        });
                    }
                }
                return;
            }
            int reason;
            boolean mayHaveBuffers = errorCode == 4;
            if (CameraDeviceImpl.this.mCurrentSession == null || !CameraDeviceImpl.this.mCurrentSession.isAborting()) {
                reason = 0;
            } else {
                reason = 1;
            }
            final CaptureFailure failure = new CaptureFailure(request, reason, mayHaveBuffers, requestId, frameNumber);
            Runnable failureDispatch = new Runnable() {
                public void run() {
                    if (!CameraDeviceImpl.this.isClosed()) {
                        holder.getCallback().onCaptureFailed(CameraDeviceImpl.this, request, failure);
                    }
                }
            };
            CameraDeviceImpl.this.mFrameNumberTracker.updateTracker(frameNumber, true, request.isReprocess());
            CameraDeviceImpl.this.checkAndFireSequenceComplete();
            holder.getHandler().post(failureDispatch);
        }
    }

    static class CaptureCallbackHolder {
        private final CaptureCallback mCallback;
        private final Handler mHandler;
        private final boolean mHasBatchedOutputs;
        private final boolean mRepeating;
        private final List<CaptureRequest> mRequestList;
        private final int mSessionId;

        CaptureCallbackHolder(CaptureCallback callback, List<CaptureRequest> requestList, Handler handler, boolean repeating, int sessionId) {
            if (callback == null || handler == null) {
                throw new UnsupportedOperationException("Must have a valid handler and a valid callback");
            }
            this.mRepeating = repeating;
            this.mHandler = handler;
            this.mRequestList = new ArrayList(requestList);
            this.mCallback = callback;
            this.mSessionId = sessionId;
            boolean hasBatchedOutputs = true;
            int i = 0;
            while (i < requestList.size()) {
                CaptureRequest request = (CaptureRequest) requestList.get(i);
                if (request.isPartOfCRequestList()) {
                    if (i == 0 && request.getTargets().size() != 2) {
                        hasBatchedOutputs = false;
                        break;
                    }
                    i++;
                } else {
                    hasBatchedOutputs = false;
                    break;
                }
            }
            this.mHasBatchedOutputs = hasBatchedOutputs;
        }

        public boolean isRepeating() {
            return this.mRepeating;
        }

        public CaptureCallback getCallback() {
            return this.mCallback;
        }

        public CaptureRequest getRequest(int subsequenceId) {
            if (subsequenceId >= this.mRequestList.size()) {
                throw new IllegalArgumentException(String.format("Requested subsequenceId %d is larger than request list size %d.", new Object[]{Integer.valueOf(subsequenceId), Integer.valueOf(this.mRequestList.size())}));
            } else if (subsequenceId >= 0) {
                return (CaptureRequest) this.mRequestList.get(subsequenceId);
            } else {
                throw new IllegalArgumentException(String.format("Requested subsequenceId %d is negative", new Object[]{Integer.valueOf(subsequenceId)}));
            }
        }

        public CaptureRequest getRequest() {
            return getRequest(0);
        }

        public Handler getHandler() {
            return this.mHandler;
        }

        public int getSessionId() {
            return this.mSessionId;
        }

        public int getRequestCount() {
            return this.mRequestList.size();
        }

        public boolean hasBatchedOutputs() {
            return this.mHasBatchedOutputs;
        }
    }

    public class FrameNumberTracker {
        private long mCompletedFrameNumber = -1;
        private long mCompletedReprocessFrameNumber = -1;
        private final TreeMap<Long, Boolean> mFutureErrorMap = new TreeMap();
        private final HashMap<Long, List<CaptureResult>> mPartialResults = new HashMap();
        private final LinkedList<Long> mSkippedRegularFrameNumbers = new LinkedList();
        private final LinkedList<Long> mSkippedReprocessFrameNumbers = new LinkedList();

        private void update() {
            Iterator iter = this.mFutureErrorMap.entrySet().iterator();
            while (iter.hasNext()) {
                Entry pair = (Entry) iter.next();
                Long errorFrameNumber = (Long) pair.getKey();
                Boolean reprocess = (Boolean) pair.getValue();
                Boolean removeError = Boolean.valueOf(true);
                if (reprocess.booleanValue()) {
                    if (errorFrameNumber.longValue() == this.mCompletedReprocessFrameNumber + 1) {
                        this.mCompletedReprocessFrameNumber = errorFrameNumber.longValue();
                    } else if (this.mSkippedReprocessFrameNumbers.isEmpty() || errorFrameNumber != this.mSkippedReprocessFrameNumbers.element()) {
                        removeError = Boolean.valueOf(false);
                    } else {
                        this.mCompletedReprocessFrameNumber = errorFrameNumber.longValue();
                        this.mSkippedReprocessFrameNumbers.remove();
                    }
                } else if (errorFrameNumber.longValue() == this.mCompletedFrameNumber + 1) {
                    this.mCompletedFrameNumber = errorFrameNumber.longValue();
                } else if (this.mSkippedRegularFrameNumbers.isEmpty() || errorFrameNumber != this.mSkippedRegularFrameNumbers.element()) {
                    removeError = Boolean.valueOf(false);
                } else {
                    this.mCompletedFrameNumber = errorFrameNumber.longValue();
                    this.mSkippedRegularFrameNumbers.remove();
                }
                if (removeError.booleanValue()) {
                    iter.remove();
                }
            }
        }

        public void updateTracker(long frameNumber, boolean isError, boolean isReprocess) {
            if (isError) {
                this.mFutureErrorMap.put(Long.valueOf(frameNumber), Boolean.valueOf(isReprocess));
            } else if (isReprocess) {
                try {
                    updateCompletedReprocessFrameNumber(frameNumber);
                } catch (IllegalArgumentException e) {
                    Log.e(CameraDeviceImpl.this.TAG, e.getMessage());
                }
            } else {
                updateCompletedFrameNumber(frameNumber);
            }
            update();
        }

        public void updateTracker(long frameNumber, CaptureResult result, boolean partial, boolean isReprocess) {
            if (!partial) {
                updateTracker(frameNumber, false, isReprocess);
            } else if (result != null) {
                List<CaptureResult> partials = (List) this.mPartialResults.get(Long.valueOf(frameNumber));
                if (partials == null) {
                    partials = new ArrayList();
                    this.mPartialResults.put(Long.valueOf(frameNumber), partials);
                }
                partials.add(result);
            }
        }

        public List<CaptureResult> popPartialResults(long frameNumber) {
            return (List) this.mPartialResults.remove(Long.valueOf(frameNumber));
        }

        public long getCompletedFrameNumber() {
            return this.mCompletedFrameNumber;
        }

        public long getCompletedReprocessFrameNumber() {
            return this.mCompletedReprocessFrameNumber;
        }

        private void updateCompletedFrameNumber(long frameNumber) throws IllegalArgumentException {
            if (frameNumber <= this.mCompletedFrameNumber) {
                throw new IllegalArgumentException("frame number " + frameNumber + " is a repeat");
            }
            if (frameNumber > this.mCompletedReprocessFrameNumber) {
                for (long i = Math.max(this.mCompletedFrameNumber, this.mCompletedReprocessFrameNumber) + 1; i < frameNumber; i++) {
                    this.mSkippedReprocessFrameNumbers.add(Long.valueOf(i));
                }
            } else if (this.mSkippedRegularFrameNumbers.isEmpty() || frameNumber < ((Long) this.mSkippedRegularFrameNumbers.element()).longValue()) {
                throw new IllegalArgumentException("frame number " + frameNumber + " is a repeat");
            } else if (frameNumber > ((Long) this.mSkippedRegularFrameNumbers.element()).longValue()) {
                throw new IllegalArgumentException("frame number " + frameNumber + " comes out of order. Expecting " + this.mSkippedRegularFrameNumbers.element());
            } else {
                this.mSkippedRegularFrameNumbers.remove();
            }
            this.mCompletedFrameNumber = frameNumber;
        }

        private void updateCompletedReprocessFrameNumber(long frameNumber) throws IllegalArgumentException {
            if (frameNumber < this.mCompletedReprocessFrameNumber) {
                throw new IllegalArgumentException("frame number " + frameNumber + " is a repeat");
            }
            if (frameNumber >= this.mCompletedFrameNumber) {
                for (long i = Math.max(this.mCompletedFrameNumber, this.mCompletedReprocessFrameNumber) + 1; i < frameNumber; i++) {
                    this.mSkippedRegularFrameNumbers.add(Long.valueOf(i));
                }
            } else if (this.mSkippedReprocessFrameNumbers.isEmpty() || frameNumber < ((Long) this.mSkippedReprocessFrameNumbers.element()).longValue()) {
                throw new IllegalArgumentException("frame number " + frameNumber + " is a repeat");
            } else if (frameNumber > ((Long) this.mSkippedReprocessFrameNumbers.element()).longValue()) {
                throw new IllegalArgumentException("frame number " + frameNumber + " comes out of order. Expecting " + this.mSkippedReprocessFrameNumbers.element());
            } else {
                this.mSkippedReprocessFrameNumbers.remove();
            }
            this.mCompletedReprocessFrameNumber = frameNumber;
        }
    }

    static class RequestLastFrameNumbersHolder {
        private final long mLastRegularFrameNumber;
        private final long mLastReprocessFrameNumber;
        private final int mRequestId;

        public RequestLastFrameNumbersHolder(List<CaptureRequest> requestList, SubmitInfo requestInfo) {
            long lastRegularFrameNumber = -1;
            long lastReprocessFrameNumber = -1;
            long frameNumber = requestInfo.getLastFrameNumber();
            if (requestInfo.getLastFrameNumber() < ((long) (requestList.size() - 1))) {
                throw new IllegalArgumentException("lastFrameNumber: " + requestInfo.getLastFrameNumber() + " should be at least " + (requestList.size() - 1) + " for the number of " + " requests in the list: " + requestList.size());
            }
            for (int i = requestList.size() - 1; i >= 0; i--) {
                CaptureRequest request = (CaptureRequest) requestList.get(i);
                if (request.isReprocess() && lastReprocessFrameNumber == -1) {
                    lastReprocessFrameNumber = frameNumber;
                } else if (!request.isReprocess() && lastRegularFrameNumber == -1) {
                    lastRegularFrameNumber = frameNumber;
                }
                if (lastReprocessFrameNumber != -1 && lastRegularFrameNumber != -1) {
                    break;
                }
                frameNumber--;
            }
            this.mLastRegularFrameNumber = lastRegularFrameNumber;
            this.mLastReprocessFrameNumber = lastReprocessFrameNumber;
            this.mRequestId = requestInfo.getRequestId();
        }

        public RequestLastFrameNumbersHolder(int requestId, long lastRegularFrameNumber) {
            this.mLastRegularFrameNumber = lastRegularFrameNumber;
            this.mLastReprocessFrameNumber = -1;
            this.mRequestId = requestId;
        }

        public long getLastRegularFrameNumber() {
            return this.mLastRegularFrameNumber;
        }

        public long getLastReprocessFrameNumber() {
            return this.mLastReprocessFrameNumber;
        }

        public long getLastFrameNumber() {
            return Math.max(this.mLastRegularFrameNumber, this.mLastReprocessFrameNumber);
        }

        public int getRequestId() {
            return this.mRequestId;
        }
    }

    public CameraDeviceImpl(String cameraId, StateCallback callback, Handler handler, CameraCharacteristics characteristics, int appTargetSdkVersion) {
        if (cameraId == null || callback == null || handler == null || characteristics == null) {
            throw new IllegalArgumentException("Null argument given");
        }
        this.mCameraId = cameraId;
        this.mDeviceCallback = callback;
        this.mDeviceHandler = handler;
        this.mCharacteristics = characteristics;
        this.mAppTargetSdkVersion = appTargetSdkVersion;
        String tag = String.format("CameraDevice-JV-%s", new Object[]{this.mCameraId});
        if (tag.length() > 23) {
            tag = tag.substring(0, 23);
        }
        this.TAG = tag;
        Integer partialCount = (Integer) this.mCharacteristics.get(CameraCharacteristics.REQUEST_PARTIAL_RESULT_COUNT);
        if (partialCount == null) {
            this.mTotalPartialCount = 1;
        } else {
            this.mTotalPartialCount = partialCount.intValue();
        }
    }

    public CameraDeviceCallbacks getCallbacks() {
        return this.mCallbacks;
    }

    public void setRemoteDevice(ICameraDeviceUser remoteDevice) throws CameraAccessException {
        synchronized (this.mInterfaceLock) {
            if (this.mInError) {
                return;
            }
            this.mRemoteDevice = new ICameraDeviceUserWrapper(remoteDevice);
            IBinder remoteDeviceBinder = remoteDevice.asBinder();
            if (remoteDeviceBinder != null) {
                try {
                    remoteDeviceBinder.linkToDeath(this, 0);
                } catch (RemoteException e) {
                    this.mDeviceHandler.post(this.mCallOnDisconnected);
                    throw new CameraAccessException(2, "The camera device has encountered a serious error");
                }
            }
            this.mDeviceHandler.post(this.mCallOnOpened);
            this.mDeviceHandler.post(this.mCallOnUnconfigured);
        }
    }

    public void setRemoteFailure(ServiceSpecificException failure) {
        int failureCode = 4;
        boolean failureIsError = true;
        switch (failure.errorCode) {
            case 4:
                failureIsError = false;
                break;
            case 6:
                failureCode = 3;
                break;
            case 7:
                failureCode = 1;
                break;
            case 8:
                failureCode = 2;
                break;
            case 10:
                failureCode = 4;
                break;
            default:
                Log.e(this.TAG, "Unexpected failure in opening camera device: " + failure.errorCode + failure.getMessage());
                break;
        }
        final int code = failureCode;
        final boolean isError = failureIsError;
        synchronized (this.mInterfaceLock) {
            this.mInError = true;
            this.mDeviceHandler.post(new Runnable() {
                public void run() {
                    if (isError) {
                        CameraDeviceImpl.this.mDeviceCallback.onError(CameraDeviceImpl.this, code);
                    } else {
                        CameraDeviceImpl.this.mDeviceCallback.onDisconnected(CameraDeviceImpl.this);
                    }
                }
            });
        }
    }

    public String getId() {
        return this.mCameraId;
    }

    public void configureOutputs(List<Surface> outputs) throws CameraAccessException {
        ArrayList<OutputConfiguration> outputConfigs = new ArrayList(outputs.size());
        for (Surface s : outputs) {
            outputConfigs.add(new OutputConfiguration(s));
        }
        configureStreamsChecked(null, outputConfigs, 0);
    }

    public boolean configureStreamsChecked(InputConfiguration inputConfig, List<OutputConfiguration> outputs, int operatingMode) throws CameraAccessException {
        if (outputs == null) {
            outputs = new ArrayList();
        }
        if (outputs.size() != 0 || inputConfig == null) {
            checkInputConfiguration(inputConfig);
            synchronized (this.mInterfaceLock) {
                OutputConfiguration outConfig;
                checkIfCameraClosedOrInError();
                HashSet<OutputConfiguration> addSet = new HashSet(outputs);
                List<Integer> deleteList = new ArrayList();
                for (int i = 0; i < this.mConfiguredOutputs.size(); i++) {
                    int streamId = this.mConfiguredOutputs.keyAt(i);
                    outConfig = (OutputConfiguration) this.mConfiguredOutputs.valueAt(i);
                    if (!outputs.contains(outConfig) || outConfig.isDeferredConfiguration()) {
                        deleteList.add(Integer.valueOf(streamId));
                    } else {
                        addSet.remove(outConfig);
                    }
                }
                this.mDeviceHandler.post(this.mCallOnBusy);
                stopRepeating();
                try {
                    waitUntilIdle();
                    this.mRemoteDevice.beginConfigure();
                    InputConfiguration currentInputConfig = (InputConfiguration) this.mConfiguredInput.getValue();
                    if (inputConfig != currentInputConfig && (inputConfig == null || (inputConfig.equals(currentInputConfig) ^ 1) != 0)) {
                        if (currentInputConfig != null) {
                            this.mRemoteDevice.deleteStream(((Integer) this.mConfiguredInput.getKey()).intValue());
                            this.mConfiguredInput = new SimpleEntry(Integer.valueOf(-1), null);
                        }
                        if (inputConfig != null) {
                            this.mConfiguredInput = new SimpleEntry(Integer.valueOf(this.mRemoteDevice.createInputStream(inputConfig.getWidth(), inputConfig.getHeight(), inputConfig.getFormat())), inputConfig);
                        }
                    }
                    for (Integer streamId2 : deleteList) {
                        this.mRemoteDevice.deleteStream(streamId2.intValue());
                        this.mConfiguredOutputs.delete(streamId2.intValue());
                    }
                    for (OutputConfiguration outConfig2 : outputs) {
                        if (addSet.contains(outConfig2)) {
                            this.mConfiguredOutputs.put(this.mRemoteDevice.createStream(outConfig2), outConfig2);
                        }
                    }
                    this.mRemoteDevice.endConfigure(operatingMode);
                    if (1 != null) {
                        if (outputs.size() > 0) {
                            this.mDeviceHandler.post(this.mCallOnIdle);
                        }
                    }
                    this.mDeviceHandler.post(this.mCallOnUnconfigured);
                } catch (IllegalArgumentException e) {
                    Log.w(this.TAG, "Stream configuration failed due to: " + e.getMessage());
                    if (null != null) {
                        if (outputs.size() > 0) {
                            this.mDeviceHandler.post(this.mCallOnIdle);
                            return false;
                        }
                    }
                    this.mDeviceHandler.post(this.mCallOnUnconfigured);
                    return false;
                } catch (CameraAccessException e2) {
                    if (e2.getReason() == 4) {
                        throw new IllegalStateException("The camera is currently busy. You must wait until the previous operation completes.", e2);
                    }
                    throw e2;
                } catch (Throwable th) {
                    if (null != null) {
                        if (outputs.size() > 0) {
                            this.mDeviceHandler.post(this.mCallOnIdle);
                        }
                    }
                    this.mDeviceHandler.post(this.mCallOnUnconfigured);
                }
            }
            return true;
        }
        throw new IllegalArgumentException("cannot configure an input stream without any output streams");
    }

    public void createCaptureSession(List<Surface> outputs, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        List<OutputConfiguration> outConfigurations = new ArrayList(outputs.size());
        for (Surface surface : outputs) {
            outConfigurations.add(new OutputConfiguration(surface));
        }
        createCaptureSessionInternal(null, outConfigurations, callback, handler, 0);
    }

    public void createCaptureSessionByOutputConfigurations(List<OutputConfiguration> outputConfigurations, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        createCaptureSessionInternal(null, new ArrayList(outputConfigurations), callback, handler, 0);
    }

    public void createReprocessableCaptureSession(InputConfiguration inputConfig, List<Surface> outputs, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        if (inputConfig == null) {
            throw new IllegalArgumentException("inputConfig cannot be null when creating a reprocessable capture session");
        }
        List<OutputConfiguration> outConfigurations = new ArrayList(outputs.size());
        for (Surface surface : outputs) {
            outConfigurations.add(new OutputConfiguration(surface));
        }
        createCaptureSessionInternal(inputConfig, outConfigurations, callback, handler, 0);
    }

    public void createReprocessableCaptureSessionByConfigurations(InputConfiguration inputConfig, List<OutputConfiguration> outputs, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        if (inputConfig == null) {
            throw new IllegalArgumentException("inputConfig cannot be null when creating a reprocessable capture session");
        } else if (outputs == null) {
            throw new IllegalArgumentException("Output configurations cannot be null when creating a reprocessable capture session");
        } else {
            List<OutputConfiguration> currentOutputs = new ArrayList();
            for (OutputConfiguration output : outputs) {
                currentOutputs.add(new OutputConfiguration(output));
            }
            createCaptureSessionInternal(inputConfig, currentOutputs, callback, handler, 0);
        }
    }

    public void createConstrainedHighSpeedCaptureSession(List<Surface> outputs, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        if (outputs == null || outputs.size() == 0 || outputs.size() > 2) {
            throw new IllegalArgumentException("Output surface list must not be null and the size must be no more than 2");
        }
        SurfaceUtils.checkConstrainedHighSpeedSurfaces(outputs, null, (StreamConfigurationMap) getCharacteristics().get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP));
        List<OutputConfiguration> outConfigurations = new ArrayList(outputs.size());
        for (Surface surface : outputs) {
            outConfigurations.add(new OutputConfiguration(surface));
        }
        createCaptureSessionInternal(null, outConfigurations, callback, handler, 1);
    }

    public void createCustomCaptureSession(InputConfiguration inputConfig, List<OutputConfiguration> outputs, int operatingMode, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        List<OutputConfiguration> currentOutputs = new ArrayList();
        for (OutputConfiguration output : outputs) {
            currentOutputs.add(new OutputConfiguration(output));
        }
        createCaptureSessionInternal(inputConfig, currentOutputs, callback, handler, operatingMode);
    }

    private void createCaptureSessionInternal(InputConfiguration inputConfig, List<OutputConfiguration> outputConfigurations, CameraCaptureSession.StateCallback callback, Handler handler, int operatingMode) throws CameraAccessException {
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            boolean isConstrainedHighSpeed = operatingMode == 1;
            if (!isConstrainedHighSpeed || inputConfig == null) {
                boolean configureSuccess;
                CameraCaptureSessionCore newSession;
                if (this.mCurrentSession != null) {
                    this.mCurrentSession.replaceSessionClose();
                }
                CameraAccessException pendingException = null;
                Surface input = null;
                try {
                    configureSuccess = configureStreamsChecked(inputConfig, outputConfigurations, operatingMode);
                    if (configureSuccess && inputConfig != null) {
                        input = this.mRemoteDevice.getInputSurface();
                    }
                } catch (CameraAccessException e) {
                    configureSuccess = false;
                    pendingException = e;
                    input = null;
                }
                if (isConstrainedHighSpeed) {
                    int i = this.mNextSessionId;
                    this.mNextSessionId = i + 1;
                    newSession = new CameraConstrainedHighSpeedCaptureSessionImpl(i, callback, handler, this, this.mDeviceHandler, configureSuccess, this.mCharacteristics);
                } else {
                    int i2 = this.mNextSessionId;
                    this.mNextSessionId = i2 + 1;
                    CameraCaptureSessionCore cameraCaptureSessionImpl = new CameraCaptureSessionImpl(i2, input, callback, handler, this, this.mDeviceHandler, configureSuccess);
                }
                this.mCurrentSession = newSession;
                if (pendingException != null) {
                    throw pendingException;
                }
                this.mSessionStateCallback = this.mCurrentSession.getDeviceStateCallback();
            } else {
                throw new IllegalArgumentException("Constrained high speed session doesn't support input configuration yet.");
            }
        }
    }

    public void setSessionListener(StateCallbackKK sessionCallback) {
        synchronized (this.mInterfaceLock) {
            this.mSessionStateCallback = sessionCallback;
        }
    }

    private void overrideEnableZsl(CameraMetadataNative request, boolean newValue) {
        if (((Boolean) request.get(CaptureRequest.CONTROL_ENABLE_ZSL)) != null) {
            request.set(CaptureRequest.CONTROL_ENABLE_ZSL, Boolean.valueOf(newValue));
        }
    }

    public Builder createCaptureRequest(int templateType) throws CameraAccessException {
        Builder builder;
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            CameraMetadataNative templatedRequest = this.mRemoteDevice.createDefaultRequest(templateType);
            if (this.mAppTargetSdkVersion < 26 || templateType != 2) {
                overrideEnableZsl(templatedRequest, false);
            }
            builder = new Builder(templatedRequest, false, -1);
        }
        return builder;
    }

    public Builder createReprocessCaptureRequest(TotalCaptureResult inputResult) throws CameraAccessException {
        Builder builder;
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            builder = new Builder(new CameraMetadataNative(inputResult.getNativeCopy()), true, inputResult.getSessionId());
        }
        return builder;
    }

    public void prepare(Surface surface) throws CameraAccessException {
        if (surface == null) {
            throw new IllegalArgumentException("Surface is null");
        }
        synchronized (this.mInterfaceLock) {
            int streamId = -1;
            for (int i = 0; i < this.mConfiguredOutputs.size(); i++) {
                if (((OutputConfiguration) this.mConfiguredOutputs.valueAt(i)).getSurfaces().contains(surface)) {
                    streamId = this.mConfiguredOutputs.keyAt(i);
                    break;
                }
            }
            if (streamId == -1) {
                throw new IllegalArgumentException("Surface is not part of this session");
            }
            this.mRemoteDevice.prepare(streamId);
        }
    }

    public void prepare(int maxCount, Surface surface) throws CameraAccessException {
        if (surface == null) {
            throw new IllegalArgumentException("Surface is null");
        } else if (maxCount <= 0) {
            throw new IllegalArgumentException("Invalid maxCount given: " + maxCount);
        } else {
            synchronized (this.mInterfaceLock) {
                int streamId = -1;
                for (int i = 0; i < this.mConfiguredOutputs.size(); i++) {
                    if (surface == ((OutputConfiguration) this.mConfiguredOutputs.valueAt(i)).getSurface()) {
                        streamId = this.mConfiguredOutputs.keyAt(i);
                        break;
                    }
                }
                if (streamId == -1) {
                    throw new IllegalArgumentException("Surface is not part of this session");
                }
                this.mRemoteDevice.prepare2(maxCount, streamId);
            }
        }
    }

    public void tearDown(Surface surface) throws CameraAccessException {
        if (surface == null) {
            throw new IllegalArgumentException("Surface is null");
        }
        synchronized (this.mInterfaceLock) {
            int streamId = -1;
            for (int i = 0; i < this.mConfiguredOutputs.size(); i++) {
                if (surface == ((OutputConfiguration) this.mConfiguredOutputs.valueAt(i)).getSurface()) {
                    streamId = this.mConfiguredOutputs.keyAt(i);
                    break;
                }
            }
            if (streamId == -1) {
                throw new IllegalArgumentException("Surface is not part of this session");
            }
            this.mRemoteDevice.tearDown(streamId);
        }
    }

    public void finalizeOutputConfigs(List<OutputConfiguration> outputConfigs) throws CameraAccessException {
        if (outputConfigs == null || outputConfigs.size() == 0) {
            throw new IllegalArgumentException("deferred config is null or empty");
        }
        synchronized (this.mInterfaceLock) {
            for (OutputConfiguration config : outputConfigs) {
                int streamId = -1;
                for (int i = 0; i < this.mConfiguredOutputs.size(); i++) {
                    if (config.equals(this.mConfiguredOutputs.valueAt(i))) {
                        streamId = this.mConfiguredOutputs.keyAt(i);
                        break;
                    }
                }
                if (streamId == -1) {
                    throw new IllegalArgumentException("Deferred config is not part of this session");
                } else if (config.getSurfaces().size() == 0) {
                    throw new IllegalArgumentException("The final config for stream " + streamId + " must have at least 1 surface");
                } else {
                    this.mRemoteDevice.finalizeOutputConfigurations(streamId, config);
                }
            }
        }
    }

    public int capture(CaptureRequest request, CaptureCallback callback, Handler handler) throws CameraAccessException {
        List<CaptureRequest> requestList = new ArrayList();
        requestList.add(request);
        return submitCaptureRequest(requestList, callback, handler, false);
    }

    public int captureBurst(List<CaptureRequest> requests, CaptureCallback callback, Handler handler) throws CameraAccessException {
        if (requests != null && !requests.isEmpty()) {
            return submitCaptureRequest(requests, callback, handler, false);
        }
        throw new IllegalArgumentException("At least one request must be given");
    }

    private void checkEarlyTriggerSequenceComplete(final int requestId, long lastFrameNumber) {
        if (lastFrameNumber == -1) {
            int index = this.mCaptureCallbackMap.indexOfKey(requestId);
            final CaptureCallbackHolder holder = index >= 0 ? (CaptureCallbackHolder) this.mCaptureCallbackMap.valueAt(index) : null;
            if (holder != null) {
                this.mCaptureCallbackMap.removeAt(index);
            }
            if (holder != null) {
                holder.getHandler().post(new Runnable() {
                    public void run() {
                        if (!CameraDeviceImpl.this.isClosed()) {
                            holder.getCallback().onCaptureSequenceAborted(CameraDeviceImpl.this, requestId);
                        }
                    }
                });
                return;
            } else {
                Log.w(this.TAG, String.format("did not register callback to request %d", new Object[]{Integer.valueOf(requestId)}));
                return;
            }
        }
        this.mRequestLastFrameNumbersList.add(new RequestLastFrameNumbersHolder(requestId, lastFrameNumber));
        checkAndFireSequenceComplete();
    }

    private int submitCaptureRequest(List<CaptureRequest> requestList, CaptureCallback callback, Handler handler, boolean repeating) throws CameraAccessException {
        int requestId;
        handler = checkHandler(handler, callback);
        for (CaptureRequest request : requestList) {
            if (request.getTargets().isEmpty()) {
                throw new IllegalArgumentException("Each request must have at least one Surface target");
            }
            for (Surface surface : request.getTargets()) {
                if (surface == null) {
                    throw new IllegalArgumentException("Null Surface targets are not allowed");
                }
            }
        }
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            if (repeating) {
                stopRepeating();
            }
            SubmitInfo requestInfo = this.mRemoteDevice.submitRequestList((CaptureRequest[]) requestList.toArray(new CaptureRequest[requestList.size()]), repeating);
            if (callback != null) {
                SparseArray sparseArray = this.mCaptureCallbackMap;
                int requestId2 = requestInfo.getRequestId();
                sparseArray.put(requestId2, new CaptureCallbackHolder(callback, requestList, handler, repeating, this.mNextSessionId - 1));
            }
            if (repeating) {
                if (this.mRepeatingRequestId != -1) {
                    checkEarlyTriggerSequenceComplete(this.mRepeatingRequestId, requestInfo.getLastFrameNumber());
                }
                this.mRepeatingRequestId = requestInfo.getRequestId();
            } else {
                this.mRequestLastFrameNumbersList.add(new RequestLastFrameNumbersHolder((List) requestList, requestInfo));
            }
            if (this.mIdle) {
                this.mDeviceHandler.post(this.mCallOnActive);
            }
            this.mIdle = false;
            requestId = requestInfo.getRequestId();
        }
        return requestId;
    }

    public int setRepeatingRequest(CaptureRequest request, CaptureCallback callback, Handler handler) throws CameraAccessException {
        List<CaptureRequest> requestList = new ArrayList();
        requestList.add(request);
        return submitCaptureRequest(requestList, callback, handler, true);
    }

    public int setRepeatingBurst(List<CaptureRequest> requests, CaptureCallback callback, Handler handler) throws CameraAccessException {
        if (requests != null && !requests.isEmpty()) {
            return submitCaptureRequest(requests, callback, handler, true);
        }
        throw new IllegalArgumentException("At least one request must be given");
    }

    public void stopRepeating() throws CameraAccessException {
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            if (this.mRepeatingRequestId != -1) {
                int requestId = this.mRepeatingRequestId;
                this.mRepeatingRequestId = -1;
                try {
                    checkEarlyTriggerSequenceComplete(requestId, this.mRemoteDevice.cancelRequest(requestId));
                } catch (IllegalArgumentException e) {
                    return;
                }
            }
        }
    }

    private void waitUntilIdle() throws CameraAccessException {
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            if (this.mRepeatingRequestId != -1) {
                throw new IllegalStateException("Active repeating request ongoing");
            }
            this.mRemoteDevice.waitUntilIdle();
        }
    }

    /* JADX WARNING: Missing block: B:13:0x0036, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void flush() throws CameraAccessException {
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            this.mDeviceHandler.post(this.mCallOnBusy);
            if (this.mIdle) {
                Log.i(this.TAG, "camera device is idle now!");
                this.mDeviceHandler.post(this.mCallOnIdle);
                return;
            }
            long lastFrameNumber = this.mRemoteDevice.flush();
            if (this.mRepeatingRequestId != -1) {
                checkEarlyTriggerSequenceComplete(this.mRepeatingRequestId, lastFrameNumber);
                this.mRepeatingRequestId = -1;
            }
        }
    }

    public void close() {
        synchronized (this.mInterfaceLock) {
            if (this.mClosing.getAndSet(true)) {
                return;
            }
            if (this.mRemoteDevice != null) {
                this.mRemoteDevice.disconnect();
                this.mRemoteDevice.unlinkToDeath(this, 0);
            }
            if (this.mRemoteDevice != null || this.mInError) {
                this.mDeviceHandler.post(this.mCallOnClosed);
            }
            this.mRemoteDevice = null;
        }
    }

    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    private void checkInputConfiguration(InputConfiguration inputConfig) {
        int i = 0;
        if (inputConfig != null) {
            int length;
            StreamConfigurationMap configMap = (StreamConfigurationMap) this.mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            boolean validFormat = false;
            for (int format : configMap.getInputFormats()) {
                if (format == inputConfig.getFormat()) {
                    validFormat = true;
                }
            }
            if (validFormat) {
                boolean validSize = false;
                Size[] inputSizes = configMap.getInputSizes(inputConfig.getFormat());
                length = inputSizes.length;
                while (i < length) {
                    Size s = inputSizes[i];
                    if (inputConfig.getWidth() == s.getWidth() && inputConfig.getHeight() == s.getHeight()) {
                        validSize = true;
                    }
                    i++;
                }
                if (!validSize) {
                    throw new IllegalArgumentException("input size " + inputConfig.getWidth() + "x" + inputConfig.getHeight() + " is not valid");
                }
                return;
            }
            throw new IllegalArgumentException("input format " + inputConfig.getFormat() + " is not valid");
        }
    }

    /* JADX WARNING: Missing block: B:22:0x008e, code:
            if (r6 == null) goto L_0x0092;
     */
    /* JADX WARNING: Missing block: B:23:0x0090, code:
            if (r17 == false) goto L_0x0095;
     */
    /* JADX WARNING: Missing block: B:24:0x0092, code:
            r9.remove();
     */
    /* JADX WARNING: Missing block: B:25:0x0095, code:
            if (r17 == false) goto L_0x001f;
     */
    /* JADX WARNING: Missing block: B:26:0x0097, code:
            r6.getHandler().post(new android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass10(r21));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void checkAndFireSequenceComplete() {
        long completedFrameNumber = this.mFrameNumberTracker.getCompletedFrameNumber();
        long completedReprocessFrameNumber = this.mFrameNumberTracker.getCompletedReprocessFrameNumber();
        Iterator<RequestLastFrameNumbersHolder> iter = this.mRequestLastFrameNumbersList.iterator();
        while (iter.hasNext()) {
            final RequestLastFrameNumbersHolder requestLastFrameNumbers = (RequestLastFrameNumbersHolder) iter.next();
            boolean sequenceCompleted = false;
            final int requestId = requestLastFrameNumbers.getRequestId();
            synchronized (this.mInterfaceLock) {
                if (this.mRemoteDevice == null) {
                    Log.w(this.TAG, "Camera closed while checking sequences");
                    return;
                }
                int index = this.mCaptureCallbackMap.indexOfKey(requestId);
                final CaptureCallbackHolder holder = index >= 0 ? (CaptureCallbackHolder) this.mCaptureCallbackMap.valueAt(index) : null;
                if (holder != null) {
                    long lastRegularFrameNumber = requestLastFrameNumbers.getLastRegularFrameNumber();
                    long lastReprocessFrameNumber = requestLastFrameNumbers.getLastReprocessFrameNumber();
                    if (lastRegularFrameNumber <= completedFrameNumber && lastReprocessFrameNumber <= completedReprocessFrameNumber) {
                        sequenceCompleted = true;
                        this.mCaptureCallbackMap.removeAt(index);
                    }
                }
            }
        }
    }

    static Handler checkHandler(Handler handler) {
        if (handler != null) {
            return handler;
        }
        Looper looper = Looper.myLooper();
        if (looper != null) {
            return new Handler(looper);
        }
        throw new IllegalArgumentException("No handler given, and current thread has no looper!");
    }

    static <T> Handler checkHandler(Handler handler, T callback) {
        if (callback != null) {
            return checkHandler(handler);
        }
        return handler;
    }

    private void checkIfCameraClosedOrInError() throws CameraAccessException {
        if (this.mRemoteDevice == null) {
            throw new IllegalStateException("CameraDevice was already closed");
        } else if (this.mInError) {
            throw new CameraAccessException(3, "The camera device has encountered a serious error");
        }
    }

    private boolean isClosed() {
        return this.mClosing.get();
    }

    private CameraCharacteristics getCharacteristics() {
        return this.mCharacteristics;
    }

    public void binderDied() {
        Log.w(this.TAG, "CameraDevice " + this.mCameraId + " died unexpectedly");
        if (this.mRemoteDevice != null) {
            this.mInError = true;
            this.mDeviceHandler.post(new Runnable() {
                public void run() {
                    if (!CameraDeviceImpl.this.isClosed()) {
                        CameraDeviceImpl.this.mDeviceCallback.onError(CameraDeviceImpl.this, 5);
                    }
                }
            });
        }
    }
}
