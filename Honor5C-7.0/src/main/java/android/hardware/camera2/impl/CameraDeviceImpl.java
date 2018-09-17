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
import android.service.notification.NotificationRankerService;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import android.util.Log;
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
    private static final int REQUEST_ID_NONE = -1;
    private final boolean DEBUG;
    private final String TAG;
    private final Runnable mCallOnActive;
    private final Runnable mCallOnBusy;
    private final Runnable mCallOnClosed;
    private final Runnable mCallOnDisconnected;
    private final Runnable mCallOnIdle;
    private final Runnable mCallOnOpened;
    private final Runnable mCallOnUnconfigured;
    private final CameraDeviceCallbacks mCallbacks;
    private final String mCameraId;
    private final SparseArray<CaptureCallbackHolder> mCaptureCallbackMap;
    private final CameraCharacteristics mCharacteristics;
    private final AtomicBoolean mClosing;
    private SimpleEntry<Integer, InputConfiguration> mConfiguredInput;
    private final SparseArray<OutputConfiguration> mConfiguredOutputs;
    private CameraCaptureSessionCore mCurrentSession;
    private final StateCallback mDeviceCallback;
    private final Handler mDeviceHandler;
    private final FrameNumberTracker mFrameNumberTracker;
    private boolean mIdle;
    private boolean mInError;
    final Object mInterfaceLock;
    private int mNextSessionId;
    private ICameraDeviceUserWrapper mRemoteDevice;
    private int mRepeatingRequestId;
    private final List<RequestLastFrameNumbersHolder> mRequestLastFrameNumbersList;
    private volatile StateCallbackKK mSessionStateCallback;
    private final int mTotalPartialCount;

    public static abstract class CaptureCallback {
        public static final int NO_FRAMES_CAPTURED = -1;

        public void onCaptureStarted(CameraDevice camera, CaptureRequest request, long timestamp, long frameNumber) {
        }

        public void onCapturePartial(CameraDevice camera, CaptureRequest request, CaptureResult result) {
        }

        public void onCaptureProgressed(CameraDevice camera, CaptureRequest request, CaptureResult partialResult) {
        }

        public void onCaptureCompleted(CameraDevice camera, CaptureRequest request, TotalCaptureResult result) {
        }

        public void onCaptureFailed(CameraDevice camera, CaptureRequest request, CaptureFailure failure) {
        }

        public void onCaptureSequenceCompleted(CameraDevice camera, int sequenceId, long frameNumber) {
        }

        public void onCaptureSequenceAborted(CameraDevice camera, int sequenceId) {
        }

        public void onCaptureBufferLost(CameraDevice camera, CaptureRequest request, Surface target, long frameNumber) {
        }
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

        public void onSurfacePrepared(Surface surface) {
        }
    }

    /* renamed from: android.hardware.camera2.impl.CameraDeviceImpl.10 */
    class AnonymousClass10 implements Runnable {
        final /* synthetic */ CaptureCallbackHolder val$holder;
        final /* synthetic */ int val$requestId;
        final /* synthetic */ RequestLastFrameNumbersHolder val$requestLastFrameNumbers;

        AnonymousClass10(int val$requestId, CaptureCallbackHolder val$holder, RequestLastFrameNumbersHolder val$requestLastFrameNumbers) {
            this.val$requestId = val$requestId;
            this.val$holder = val$holder;
            this.val$requestLastFrameNumbers = val$requestLastFrameNumbers;
        }

        public void run() {
            if (!CameraDeviceImpl.this.isClosed()) {
                this.val$holder.getCallback().onCaptureSequenceCompleted(CameraDeviceImpl.this, this.val$requestId, this.val$requestLastFrameNumbers.getLastFrameNumber());
            }
        }
    }

    /* renamed from: android.hardware.camera2.impl.CameraDeviceImpl.8 */
    class AnonymousClass8 implements Runnable {
        final /* synthetic */ int val$code;
        final /* synthetic */ boolean val$isError;

        AnonymousClass8(boolean val$isError, int val$code) {
            this.val$isError = val$isError;
            this.val$code = val$code;
        }

        public void run() {
            if (this.val$isError) {
                CameraDeviceImpl.this.mDeviceCallback.onError(CameraDeviceImpl.this, this.val$code);
            } else {
                CameraDeviceImpl.this.mDeviceCallback.onDisconnected(CameraDeviceImpl.this);
            }
        }
    }

    /* renamed from: android.hardware.camera2.impl.CameraDeviceImpl.9 */
    class AnonymousClass9 implements Runnable {
        final /* synthetic */ CaptureCallbackHolder val$holder;
        final /* synthetic */ int val$requestId;

        AnonymousClass9(int val$requestId, CaptureCallbackHolder val$holder) {
            this.val$requestId = val$requestId;
            this.val$holder = val$holder;
        }

        public void run() {
            if (!CameraDeviceImpl.this.isClosed()) {
                this.val$holder.getCallback().onCaptureSequenceAborted(CameraDeviceImpl.this, this.val$requestId);
            }
        }
    }

    public class CameraDeviceCallbacks extends Stub {

        /* renamed from: android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ int val$publicErrorCode;

            AnonymousClass1(int val$publicErrorCode) {
                this.val$publicErrorCode = val$publicErrorCode;
            }

            public void run() {
                if (!CameraDeviceImpl.this.isClosed()) {
                    CameraDeviceImpl.this.mDeviceCallback.onError(CameraDeviceImpl.this, this.val$publicErrorCode);
                }
            }
        }

        /* renamed from: android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ long val$frameNumber;
            final /* synthetic */ CaptureCallbackHolder val$holder;
            final /* synthetic */ CaptureResultExtras val$resultExtras;
            final /* synthetic */ long val$timestamp;

            AnonymousClass2(CaptureCallbackHolder val$holder, CaptureResultExtras val$resultExtras, long val$timestamp, long val$frameNumber) {
                this.val$holder = val$holder;
                this.val$resultExtras = val$resultExtras;
                this.val$timestamp = val$timestamp;
                this.val$frameNumber = val$frameNumber;
            }

            public void run() {
                if (!CameraDeviceImpl.this.isClosed()) {
                    this.val$holder.getCallback().onCaptureStarted(CameraDeviceImpl.this, this.val$holder.getRequest(this.val$resultExtras.getSubsequenceId()), this.val$timestamp, this.val$frameNumber);
                }
            }
        }

        /* renamed from: android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ CaptureCallbackHolder val$holder;
            final /* synthetic */ CaptureRequest val$request;
            final /* synthetic */ CaptureResult val$resultAsCapture;

            AnonymousClass3(CaptureCallbackHolder val$holder, CaptureRequest val$request, CaptureResult val$resultAsCapture) {
                this.val$holder = val$holder;
                this.val$request = val$request;
                this.val$resultAsCapture = val$resultAsCapture;
            }

            public void run() {
                if (!CameraDeviceImpl.this.isClosed()) {
                    this.val$holder.getCallback().onCaptureProgressed(CameraDeviceImpl.this, this.val$request, this.val$resultAsCapture);
                }
            }
        }

        /* renamed from: android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.4 */
        class AnonymousClass4 implements Runnable {
            final /* synthetic */ CaptureCallbackHolder val$holder;
            final /* synthetic */ CaptureRequest val$request;
            final /* synthetic */ TotalCaptureResult val$resultAsCapture;

            AnonymousClass4(CaptureCallbackHolder val$holder, CaptureRequest val$request, TotalCaptureResult val$resultAsCapture) {
                this.val$holder = val$holder;
                this.val$request = val$request;
                this.val$resultAsCapture = val$resultAsCapture;
            }

            public void run() {
                if (!CameraDeviceImpl.this.isClosed()) {
                    this.val$holder.getCallback().onCaptureCompleted(CameraDeviceImpl.this, this.val$request, this.val$resultAsCapture);
                }
            }
        }

        /* renamed from: android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.5 */
        class AnonymousClass5 implements Runnable {
            final /* synthetic */ long val$frameNumber;
            final /* synthetic */ CaptureCallbackHolder val$holder;
            final /* synthetic */ Surface val$outputSurface;
            final /* synthetic */ CaptureRequest val$request;

            AnonymousClass5(CaptureCallbackHolder val$holder, CaptureRequest val$request, Surface val$outputSurface, long val$frameNumber) {
                this.val$holder = val$holder;
                this.val$request = val$request;
                this.val$outputSurface = val$outputSurface;
                this.val$frameNumber = val$frameNumber;
            }

            public void run() {
                if (!CameraDeviceImpl.this.isClosed()) {
                    this.val$holder.getCallback().onCaptureBufferLost(CameraDeviceImpl.this, this.val$request, this.val$outputSurface, this.val$frameNumber);
                }
            }
        }

        /* renamed from: android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.6 */
        class AnonymousClass6 implements Runnable {
            final /* synthetic */ CaptureFailure val$failure;
            final /* synthetic */ CaptureCallbackHolder val$holder;
            final /* synthetic */ CaptureRequest val$request;

            AnonymousClass6(CaptureCallbackHolder val$holder, CaptureRequest val$request, CaptureFailure val$failure) {
                this.val$holder = val$holder;
                this.val$request = val$request;
                this.val$failure = val$failure;
            }

            public void run() {
                if (!CameraDeviceImpl.this.isClosed()) {
                    this.val$holder.getCallback().onCaptureFailed(CameraDeviceImpl.this, this.val$request, this.val$failure);
                }
            }
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onDeviceError(int errorCode, CaptureResultExtras resultExtras) {
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    switch (errorCode) {
                        case TextToSpeech.SUCCESS /*0*/:
                            CameraDeviceImpl.this.mDeviceHandler.post(CameraDeviceImpl.this.mCallOnDisconnected);
                            break;
                        case AudioState.ROUTE_EARPIECE /*1*/:
                        case AudioState.ROUTE_BLUETOOTH /*2*/:
                            break;
                        case Engine.DEFAULT_STREAM /*3*/:
                        case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                        case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
                            onCaptureErrorLocked(errorCode, resultExtras);
                            break;
                        default:
                            Log.e(CameraDeviceImpl.this.TAG, "Unknown error from camera device: " + errorCode);
                            break;
                    }
                }
            }
        }

        public void onRepeatingRequestError(long lastFrameNumber) {
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice == null || CameraDeviceImpl.this.mRepeatingRequestId == CameraDeviceImpl.REQUEST_ID_NONE) {
                    return;
                }
                CameraDeviceImpl.this.checkEarlyTriggerSequenceComplete(CameraDeviceImpl.this.mRepeatingRequestId, lastFrameNumber);
                CameraDeviceImpl.this.mRepeatingRequestId = CameraDeviceImpl.REQUEST_ID_NONE;
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
            long frameNumber = resultExtras.getFrameNumber();
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice == null) {
                    return;
                }
                CaptureCallbackHolder holder = (CaptureCallbackHolder) CameraDeviceImpl.this.mCaptureCallbackMap.get(requestId);
                if (holder == null) {
                } else if (CameraDeviceImpl.this.isClosed()) {
                } else {
                    holder.getHandler().post(new AnonymousClass2(holder, resultExtras, timestamp, frameNumber));
                }
            }
        }

        public void onResultReceived(CameraMetadataNative result, CaptureResultExtras resultExtras) throws RemoteException {
            int requestId = resultExtras.getRequestId();
            long frameNumber = resultExtras.getFrameNumber();
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice == null) {
                    return;
                }
                result.set(CameraCharacteristics.LENS_INFO_SHADING_MAP_SIZE, (Size) CameraDeviceImpl.this.getCharacteristics().get(CameraCharacteristics.LENS_INFO_SHADING_MAP_SIZE));
                CaptureCallbackHolder holder = (CaptureCallbackHolder) CameraDeviceImpl.this.mCaptureCallbackMap.get(requestId);
                CaptureRequest request = holder.getRequest(resultExtras.getSubsequenceId());
                boolean isPartialResult = resultExtras.getPartialResultCount() < CameraDeviceImpl.this.mTotalPartialCount;
                boolean isReprocess = request.isReprocess();
                if (holder == null) {
                    CameraDeviceImpl.this.mFrameNumberTracker.updateTracker(frameNumber, null, isPartialResult, isReprocess);
                } else if (CameraDeviceImpl.this.isClosed()) {
                    CameraDeviceImpl.this.mFrameNumberTracker.updateTracker(frameNumber, null, isPartialResult, isReprocess);
                } else {
                    CaptureResult finalResult;
                    Runnable anonymousClass3;
                    if (isPartialResult) {
                        CaptureResult captureResult = new CaptureResult(result, request, resultExtras);
                        anonymousClass3 = new AnonymousClass3(holder, request, captureResult);
                        finalResult = captureResult;
                    } else {
                        CaptureResult resultAsCapture = new TotalCaptureResult(result, request, resultExtras, CameraDeviceImpl.this.mFrameNumberTracker.popPartialResults(frameNumber), holder.getSessionId());
                        anonymousClass3 = new AnonymousClass4(holder, request, resultAsCapture);
                        finalResult = resultAsCapture;
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
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                OutputConfiguration output = (OutputConfiguration) CameraDeviceImpl.this.mConfiguredOutputs.get(streamId);
                StateCallbackKK sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
            }
            if (sessionCallback != null) {
                if (output == null) {
                    Log.w(CameraDeviceImpl.this.TAG, "onPrepared invoked for unknown output Surface");
                } else {
                    sessionCallback.onSurfacePrepared(output.getSurface());
                }
            }
        }

        private void onCaptureErrorLocked(int errorCode, CaptureResultExtras resultExtras) {
            Runnable failureDispatch;
            int requestId = resultExtras.getRequestId();
            int subsequenceId = resultExtras.getSubsequenceId();
            long frameNumber = resultExtras.getFrameNumber();
            CaptureCallbackHolder holder = (CaptureCallbackHolder) CameraDeviceImpl.this.mCaptureCallbackMap.get(requestId);
            CaptureRequest request = holder.getRequest(subsequenceId);
            if (errorCode == 5) {
                failureDispatch = new AnonymousClass5(holder, request, ((OutputConfiguration) CameraDeviceImpl.this.mConfiguredOutputs.get(resultExtras.getErrorStreamId())).getSurface(), frameNumber);
            } else {
                int reason;
                boolean mayHaveBuffers = errorCode == 4;
                if (CameraDeviceImpl.this.mCurrentSession == null || !CameraDeviceImpl.this.mCurrentSession.isAborting()) {
                    reason = 0;
                } else {
                    reason = 1;
                }
                failureDispatch = new AnonymousClass6(holder, request, new CaptureFailure(request, reason, mayHaveBuffers, requestId, frameNumber));
                CameraDeviceImpl.this.mFrameNumberTracker.updateTracker(frameNumber, true, request.isReprocess());
                CameraDeviceImpl.this.checkAndFireSequenceComplete();
            }
            holder.getHandler().post(failureDispatch);
        }
    }

    static class CaptureCallbackHolder {
        private final CaptureCallback mCallback;
        private final Handler mHandler;
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
    }

    public class FrameNumberTracker {
        private long mCompletedFrameNumber;
        private long mCompletedReprocessFrameNumber;
        private final TreeMap<Long, Boolean> mFutureErrorMap;
        private final HashMap<Long, List<CaptureResult>> mPartialResults;
        private final LinkedList<Long> mSkippedRegularFrameNumbers;
        private final LinkedList<Long> mSkippedReprocessFrameNumbers;

        public FrameNumberTracker() {
            this.mCompletedFrameNumber = -1;
            this.mCompletedReprocessFrameNumber = -1;
            this.mSkippedRegularFrameNumbers = new LinkedList();
            this.mSkippedReprocessFrameNumbers = new LinkedList();
            this.mFutureErrorMap = new TreeMap();
            this.mPartialResults = new HashMap();
        }

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
            if (requestInfo.getLastFrameNumber() < ((long) (requestList.size() + CameraDeviceImpl.REQUEST_ID_NONE))) {
                throw new IllegalArgumentException("lastFrameNumber: " + requestInfo.getLastFrameNumber() + " should be at least " + (requestList.size() + CameraDeviceImpl.REQUEST_ID_NONE) + " for the number of " + " requests in the list: " + requestList.size());
            }
            for (int i = requestList.size() + CameraDeviceImpl.REQUEST_ID_NONE; i >= 0; i += CameraDeviceImpl.REQUEST_ID_NONE) {
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

    public CameraDeviceImpl(String cameraId, StateCallback callback, Handler handler, CameraCharacteristics characteristics) {
        this.DEBUG = false;
        this.mInterfaceLock = new Object();
        this.mCallbacks = new CameraDeviceCallbacks();
        this.mClosing = new AtomicBoolean();
        this.mInError = false;
        this.mIdle = true;
        this.mCaptureCallbackMap = new SparseArray();
        this.mRepeatingRequestId = REQUEST_ID_NONE;
        this.mConfiguredInput = new SimpleEntry(Integer.valueOf(REQUEST_ID_NONE), null);
        this.mConfiguredOutputs = new SparseArray();
        this.mRequestLastFrameNumbersList = new ArrayList();
        this.mFrameNumberTracker = new FrameNumberTracker();
        this.mNextSessionId = 0;
        this.mCallOnOpened = new Runnable() {
            public void run() {
                synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                    if (CameraDeviceImpl.this.mRemoteDevice == null) {
                        return;
                    }
                    StateCallbackKK sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
                    if (sessionCallback != null) {
                        sessionCallback.onOpened(CameraDeviceImpl.this);
                    }
                    CameraDeviceImpl.this.mDeviceCallback.onOpened(CameraDeviceImpl.this);
                }
            }
        };
        this.mCallOnUnconfigured = new Runnable() {
            public void run() {
                synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                    if (CameraDeviceImpl.this.mRemoteDevice == null) {
                        return;
                    }
                    StateCallbackKK sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
                    if (sessionCallback != null) {
                        sessionCallback.onUnconfigured(CameraDeviceImpl.this);
                    }
                }
            }
        };
        this.mCallOnActive = new Runnable() {
            public void run() {
                synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                    if (CameraDeviceImpl.this.mRemoteDevice == null) {
                        return;
                    }
                    StateCallbackKK sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
                    if (sessionCallback != null) {
                        sessionCallback.onActive(CameraDeviceImpl.this);
                    }
                }
            }
        };
        this.mCallOnBusy = new Runnable() {
            public void run() {
                synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                    if (CameraDeviceImpl.this.mRemoteDevice == null) {
                        return;
                    }
                    StateCallbackKK sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
                    if (sessionCallback != null) {
                        sessionCallback.onBusy(CameraDeviceImpl.this);
                    }
                }
            }
        };
        this.mCallOnClosed = new Runnable() {
            private boolean mClosedOnce;

            {
                this.mClosedOnce = false;
            }

            public void run() {
                if (this.mClosedOnce) {
                    throw new AssertionError("Don't post #onClosed more than once");
                }
                synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                    StateCallbackKK sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
                }
                if (sessionCallback != null) {
                    sessionCallback.onClosed(CameraDeviceImpl.this);
                }
                CameraDeviceImpl.this.mDeviceCallback.onClosed(CameraDeviceImpl.this);
                this.mClosedOnce = true;
            }
        };
        this.mCallOnIdle = new Runnable() {
            public void run() {
                synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                    if (CameraDeviceImpl.this.mRemoteDevice == null) {
                        return;
                    }
                    StateCallbackKK sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
                    if (sessionCallback != null) {
                        sessionCallback.onIdle(CameraDeviceImpl.this);
                    }
                }
            }
        };
        this.mCallOnDisconnected = new Runnable() {
            public void run() {
                synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                    if (CameraDeviceImpl.this.mRemoteDevice == null) {
                        return;
                    }
                    StateCallbackKK sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
                    if (sessionCallback != null) {
                        sessionCallback.onDisconnected(CameraDeviceImpl.this);
                    }
                    CameraDeviceImpl.this.mDeviceCallback.onDisconnected(CameraDeviceImpl.this);
                }
            }
        };
        if (cameraId == null || callback == null || handler == null || characteristics == null) {
            throw new IllegalArgumentException("Null argument given");
        }
        this.mCameraId = cameraId;
        this.mDeviceCallback = callback;
        this.mDeviceHandler = handler;
        this.mCharacteristics = characteristics;
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
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                failureIsError = false;
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
                failureCode = 3;
                break;
            case SpeechRecognizer.ERROR_NO_MATCH /*7*/:
                failureCode = 1;
                break;
            case AudioState.ROUTE_SPEAKER /*8*/:
                failureCode = 2;
                break;
            case NotificationRankerService.REASON_LISTENER_CANCEL /*10*/:
                failureCode = 4;
                break;
            default:
                Log.e(this.TAG, "Unexpected failure in opening camera device: " + failure.errorCode + failure.getMessage());
                break;
        }
        int code = failureCode;
        boolean isError = failureIsError;
        synchronized (this.mInterfaceLock) {
            this.mInError = true;
            this.mDeviceHandler.post(new AnonymousClass8(isError, code));
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
        configureStreamsChecked(null, outputConfigs, false);
    }

    public boolean configureStreamsChecked(InputConfiguration inputConfig, List<OutputConfiguration> outputs, boolean isConstrainedHighSpeed) throws CameraAccessException {
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
                    if (outputs.contains(outConfig)) {
                        addSet.remove(outConfig);
                    } else {
                        deleteList.add(Integer.valueOf(streamId));
                    }
                }
                this.mDeviceHandler.post(this.mCallOnBusy);
                stopRepeating();
                try {
                    waitUntilIdle();
                    this.mRemoteDevice.beginConfigure();
                    InputConfiguration currentInputConfig = (InputConfiguration) this.mConfiguredInput.getValue();
                    if (inputConfig != currentInputConfig && (inputConfig == null || !inputConfig.equals(currentInputConfig))) {
                        if (currentInputConfig != null) {
                            ICameraDeviceUserWrapper iCameraDeviceUserWrapper = this.mRemoteDevice;
                            r17.deleteStream(((Integer) this.mConfiguredInput.getKey()).intValue());
                            this.mConfiguredInput = new SimpleEntry(Integer.valueOf(REQUEST_ID_NONE), null);
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
                    this.mRemoteDevice.endConfigure(isConstrainedHighSpeed);
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
        createCaptureSessionInternal(null, outConfigurations, callback, handler, false);
    }

    public void createCaptureSessionByOutputConfigurations(List<OutputConfiguration> outputConfigurations, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        createCaptureSessionInternal(null, new ArrayList(outputConfigurations), callback, handler, false);
    }

    public void createReprocessableCaptureSession(InputConfiguration inputConfig, List<Surface> outputs, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        if (inputConfig == null) {
            throw new IllegalArgumentException("inputConfig cannot be null when creating a reprocessable capture session");
        }
        List<OutputConfiguration> outConfigurations = new ArrayList(outputs.size());
        for (Surface surface : outputs) {
            outConfigurations.add(new OutputConfiguration(surface));
        }
        createCaptureSessionInternal(inputConfig, outConfigurations, callback, handler, false);
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
            createCaptureSessionInternal(inputConfig, currentOutputs, callback, handler, false);
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
        createCaptureSessionInternal(null, outConfigurations, callback, handler, true);
    }

    private void createCaptureSessionInternal(InputConfiguration inputConfig, List<OutputConfiguration> outputConfigurations, CameraCaptureSession.StateCallback callback, Handler handler, boolean isConstrainedHighSpeed) throws CameraAccessException {
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            if (!isConstrainedHighSpeed || inputConfig == null) {
                boolean configureSuccess;
                CameraCaptureSessionCore newSession;
                if (this.mCurrentSession != null) {
                    this.mCurrentSession.replaceSessionClose();
                }
                CameraAccessException pendingException = null;
                Surface input = null;
                try {
                    configureSuccess = configureStreamsChecked(inputConfig, outputConfigurations, isConstrainedHighSpeed);
                    if (configureSuccess && inputConfig != null) {
                        input = this.mRemoteDevice.getInputSurface();
                    }
                } catch (CameraAccessException e) {
                    configureSuccess = false;
                    pendingException = e;
                    input = null;
                }
                List<Surface> outSurfaces = new ArrayList(outputConfigurations.size());
                for (OutputConfiguration config : outputConfigurations) {
                    outSurfaces.add(config.getSurface());
                }
                if (isConstrainedHighSpeed) {
                    int i = this.mNextSessionId;
                    this.mNextSessionId = i + 1;
                    newSession = new CameraConstrainedHighSpeedCaptureSessionImpl(i, outSurfaces, callback, handler, this, this.mDeviceHandler, configureSuccess, this.mCharacteristics);
                } else {
                    int i2 = this.mNextSessionId;
                    this.mNextSessionId = i2 + 1;
                    CameraCaptureSessionCore cameraCaptureSessionImpl = new CameraCaptureSessionImpl(i2, input, outSurfaces, callback, handler, this, this.mDeviceHandler, configureSuccess);
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

    public Builder createCaptureRequest(int templateType) throws CameraAccessException {
        Builder builder;
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            builder = new Builder(this.mRemoteDevice.createDefaultRequest(templateType), false, REQUEST_ID_NONE);
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void prepare(Surface surface) throws CameraAccessException {
        if (surface == null) {
            throw new IllegalArgumentException("Surface is null");
        }
        synchronized (this.mInterfaceLock) {
            int streamId = REQUEST_ID_NONE;
            int i = 0;
            while (true) {
                if (i >= this.mConfiguredOutputs.size()) {
                    break;
                } else if (surface == ((OutputConfiguration) this.mConfiguredOutputs.valueAt(i)).getSurface()) {
                    break;
                } else {
                    i++;
                }
            }
            if (streamId == REQUEST_ID_NONE) {
                throw new IllegalArgumentException("Surface is not part of this session");
            }
            this.mRemoteDevice.prepare(streamId);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void prepare(int maxCount, Surface surface) throws CameraAccessException {
        if (surface == null) {
            throw new IllegalArgumentException("Surface is null");
        } else if (maxCount <= 0) {
            throw new IllegalArgumentException("Invalid maxCount given: " + maxCount);
        } else {
            synchronized (this.mInterfaceLock) {
                int streamId = REQUEST_ID_NONE;
                int i = 0;
                while (true) {
                    if (i >= this.mConfiguredOutputs.size()) {
                        break;
                    } else if (surface == ((OutputConfiguration) this.mConfiguredOutputs.valueAt(i)).getSurface()) {
                        break;
                    } else {
                        i++;
                    }
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void tearDown(Surface surface) throws CameraAccessException {
        if (surface == null) {
            throw new IllegalArgumentException("Surface is null");
        }
        synchronized (this.mInterfaceLock) {
            int streamId = REQUEST_ID_NONE;
            int i = 0;
            while (true) {
                if (i >= this.mConfiguredOutputs.size()) {
                    break;
                } else if (surface == ((OutputConfiguration) this.mConfiguredOutputs.valueAt(i)).getSurface()) {
                    break;
                } else {
                    i++;
                }
            }
            if (streamId == REQUEST_ID_NONE) {
                throw new IllegalArgumentException("Surface is not part of this session");
            }
            this.mRemoteDevice.tearDown(streamId);
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

    private void checkEarlyTriggerSequenceComplete(int requestId, long lastFrameNumber) {
        CaptureCallbackHolder holder = null;
        if (lastFrameNumber == -1) {
            int index = this.mCaptureCallbackMap.indexOfKey(requestId);
            if (index >= 0) {
                holder = (CaptureCallbackHolder) this.mCaptureCallbackMap.valueAt(index);
            }
            if (holder != null) {
                this.mCaptureCallbackMap.removeAt(index);
            }
            if (holder != null) {
                holder.getHandler().post(new AnonymousClass9(requestId, holder));
                return;
            }
            Log.w(this.TAG, String.format("did not register callback to request %d", new Object[]{Integer.valueOf(requestId)}));
            return;
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
                sparseArray.put(requestId2, new CaptureCallbackHolder(callback, requestList, handler, repeating, this.mNextSessionId + REQUEST_ID_NONE));
            }
            if (repeating) {
                if (this.mRepeatingRequestId != REQUEST_ID_NONE) {
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
            if (this.mRepeatingRequestId != REQUEST_ID_NONE) {
                int requestId = this.mRepeatingRequestId;
                this.mRepeatingRequestId = REQUEST_ID_NONE;
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
            if (this.mRepeatingRequestId != REQUEST_ID_NONE) {
                throw new IllegalStateException("Active repeating request ongoing");
            }
            this.mRemoteDevice.waitUntilIdle();
        }
    }

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
            if (this.mRepeatingRequestId != REQUEST_ID_NONE) {
                checkEarlyTriggerSequenceComplete(this.mRepeatingRequestId, lastFrameNumber);
                this.mRepeatingRequestId = REQUEST_ID_NONE;
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

    private void checkAndFireSequenceComplete() {
        long completedFrameNumber = this.mFrameNumberTracker.getCompletedFrameNumber();
        long completedReprocessFrameNumber = this.mFrameNumberTracker.getCompletedReprocessFrameNumber();
        Iterator<RequestLastFrameNumbersHolder> iter = this.mRequestLastFrameNumbersList.iterator();
        while (iter.hasNext()) {
            RequestLastFrameNumbersHolder requestLastFrameNumbers = (RequestLastFrameNumbersHolder) iter.next();
            boolean sequenceCompleted = false;
            int requestId = requestLastFrameNumbers.getRequestId();
            synchronized (this.mInterfaceLock) {
                if (this.mRemoteDevice == null) {
                    Log.w(this.TAG, "Camera closed while checking sequences");
                    return;
                }
                CaptureCallbackHolder captureCallbackHolder;
                int index = this.mCaptureCallbackMap.indexOfKey(requestId);
                if (index >= 0) {
                    captureCallbackHolder = (CaptureCallbackHolder) this.mCaptureCallbackMap.valueAt(index);
                } else {
                    captureCallbackHolder = null;
                }
                if (captureCallbackHolder != null) {
                    long lastRegularFrameNumber = requestLastFrameNumbers.getLastRegularFrameNumber();
                    long lastReprocessFrameNumber = requestLastFrameNumbers.getLastReprocessFrameNumber();
                    if (lastRegularFrameNumber <= completedFrameNumber && lastReprocessFrameNumber <= completedReprocessFrameNumber) {
                        sequenceCompleted = true;
                        this.mCaptureCallbackMap.removeAt(index);
                    }
                }
                if (captureCallbackHolder == null || sequenceCompleted) {
                    iter.remove();
                }
                if (sequenceCompleted) {
                    Runnable anonymousClass10 = new AnonymousClass10(requestId, captureCallbackHolder, requestLastFrameNumbers);
                    captureCallbackHolder.getHandler().post(anonymousClass10);
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
