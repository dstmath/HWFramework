package android.hardware.camera2.impl;

import android.app.ActivityThread;
import android.camera.IHwCameraUtil;
import android.common.HwFrameworkFactory;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.ICameraDeviceCallbacks;
import android.hardware.camera2.ICameraDeviceUser;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.InputConfiguration;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.camera2.utils.SubmitInfo;
import android.hardware.camera2.utils.SurfaceUtils;
import android.hsm.HwSystemManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseArray;
import android.view.Surface;
import com.android.internal.util.Preconditions;
import com.android.internal.util.function.pooled.PooledLambda;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public class CameraDeviceImpl extends CameraDevice implements IBinder.DeathRecipient {
    private static final long NANO_PER_SECOND = 1000000000;
    private static final int REQUEST_ID_NONE = -1;
    private final boolean DEBUG = false;
    /* access modifiers changed from: private */
    public final String TAG;
    private final int mAppTargetSdkVersion;
    private final Runnable mCallOnActive = new Runnable() {
        /* JADX WARNING: Code restructure failed: missing block: B:10:0x001a, code lost:
            r0.onActive(r3.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x001f, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0018, code lost:
            if (r0 == null) goto L_0x001f;
         */
        public void run() {
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    StateCallbackKK sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
                }
            }
        }
    };
    private final Runnable mCallOnBusy = new Runnable() {
        /* JADX WARNING: Code restructure failed: missing block: B:10:0x001a, code lost:
            r0.onBusy(r3.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x001f, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0018, code lost:
            if (r0 == null) goto L_0x001f;
         */
        public void run() {
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    StateCallbackKK sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
                }
            }
        }
    };
    private final Runnable mCallOnClosed = new Runnable() {
        private boolean mClosedOnce = false;

        public void run() {
            StateCallbackKK sessionCallback;
            if (!this.mClosedOnce) {
                synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                    sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
                }
                if (sessionCallback != null) {
                    sessionCallback.onClosed(CameraDeviceImpl.this);
                }
                CameraDeviceImpl.this.mDeviceCallback.onClosed(CameraDeviceImpl.this);
                this.mClosedOnce = true;
                return;
            }
            throw new AssertionError("Don't post #onClosed more than once");
        }
    };
    /* access modifiers changed from: private */
    public final Runnable mCallOnDisconnected = new Runnable() {
        /* JADX WARNING: Code restructure failed: missing block: B:10:0x001a, code lost:
            r0.onDisconnected(r3.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x001f, code lost:
            android.hardware.camera2.impl.CameraDeviceImpl.access$200(r3.this$0).onDisconnected(r3.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x002a, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0018, code lost:
            if (r0 == null) goto L_0x001f;
         */
        public void run() {
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    StateCallbackKK sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public final Runnable mCallOnIdle = new Runnable() {
        /* JADX WARNING: Code restructure failed: missing block: B:10:0x001a, code lost:
            r0.onIdle(r3.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x001f, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0018, code lost:
            if (r0 == null) goto L_0x001f;
         */
        public void run() {
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    StateCallbackKK sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
                }
            }
        }
    };
    private final Runnable mCallOnOpened = new Runnable() {
        /* JADX WARNING: Code restructure failed: missing block: B:10:0x001a, code lost:
            r0.onOpened(r3.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x001f, code lost:
            android.hardware.camera2.impl.CameraDeviceImpl.access$200(r3.this$0).onOpened(r3.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x002a, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0018, code lost:
            if (r0 == null) goto L_0x001f;
         */
        public void run() {
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    StateCallbackKK sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
                }
            }
        }
    };
    private final Runnable mCallOnUnconfigured = new Runnable() {
        /* JADX WARNING: Code restructure failed: missing block: B:10:0x001a, code lost:
            r0.onUnconfigured(r3.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x001f, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0018, code lost:
            if (r0 == null) goto L_0x001f;
         */
        public void run() {
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    StateCallbackKK sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
                }
            }
        }
    };
    private final CameraDeviceCallbacks mCallbacks = new CameraDeviceCallbacks();
    private final String mCameraId;
    /* access modifiers changed from: private */
    public final SparseArray<CaptureCallbackHolder> mCaptureCallbackMap = new SparseArray<>();
    private final CameraCharacteristics mCharacteristics;
    private final AtomicBoolean mClosing = new AtomicBoolean();
    private AbstractMap.SimpleEntry<Integer, InputConfiguration> mConfiguredInput = new AbstractMap.SimpleEntry<>(-1, null);
    /* access modifiers changed from: private */
    public final SparseArray<OutputConfiguration> mConfiguredOutputs = new SparseArray<>();
    /* access modifiers changed from: private */
    public CameraCaptureSessionCore mCurrentSession;
    /* access modifiers changed from: private */
    public final CameraDevice.StateCallback mDeviceCallback;
    /* access modifiers changed from: private */
    public final Executor mDeviceExecutor;
    /* access modifiers changed from: private */
    public final FrameNumberTracker mFrameNumberTracker = new FrameNumberTracker();
    /* access modifiers changed from: private */
    public boolean mIdle = true;
    /* access modifiers changed from: private */
    public boolean mInError = false;
    final Object mInterfaceLock = new Object();
    private int mNextSessionId = 0;
    /* access modifiers changed from: private */
    public ICameraDeviceUserWrapper mRemoteDevice;
    /* access modifiers changed from: private */
    public int mRepeatingRequestId = -1;
    private final List<RequestLastFrameNumbersHolder> mRequestLastFrameNumbersList = new ArrayList();
    /* access modifiers changed from: private */
    public volatile StateCallbackKK mSessionStateCallback;
    /* access modifiers changed from: private */
    public final int mTotalPartialCount;

    public class CameraDeviceCallbacks extends ICameraDeviceCallbacks.Stub {
        public CameraDeviceCallbacks() {
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX INFO: finally extract failed */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x005d, code lost:
            return;
         */
        public void onDeviceError(int errorCode, CaptureResultExtras resultExtras) {
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    switch (errorCode) {
                        case 0:
                            long ident = Binder.clearCallingIdentity();
                            try {
                                CameraDeviceImpl.this.mDeviceExecutor.execute(CameraDeviceImpl.this.mCallOnDisconnected);
                                Binder.restoreCallingIdentity(ident);
                                break;
                            } catch (Throwable th) {
                                Binder.restoreCallingIdentity(ident);
                                throw th;
                            }
                        case 1:
                            scheduleNotifyError(4);
                            break;
                        case 3:
                        case 4:
                        case 5:
                            onCaptureErrorLocked(errorCode, resultExtras);
                            break;
                        case 6:
                            scheduleNotifyError(3);
                            break;
                        default:
                            String access$400 = CameraDeviceImpl.this.TAG;
                            Log.e(access$400, "Unknown error from camera device: " + errorCode);
                            scheduleNotifyError(5);
                            break;
                    }
                }
            }
        }

        private void scheduleNotifyError(int code) {
            boolean unused = CameraDeviceImpl.this.mInError = true;
            long ident = Binder.clearCallingIdentity();
            try {
                CameraDeviceImpl.this.mDeviceExecutor.execute(PooledLambda.obtainRunnable($$Lambda$CameraDeviceImpl$CameraDeviceCallbacks$Sm85frAzwGZVMAKNE_gwckYXVQ.INSTANCE, this, Integer.valueOf(code)));
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        /* access modifiers changed from: private */
        public void notifyError(int code) {
            if (!CameraDeviceImpl.this.isClosed()) {
                CameraDeviceImpl.this.mDeviceCallback.onError(CameraDeviceImpl.this, code);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0030, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0032, code lost:
            return;
         */
        public void onRepeatingRequestError(long lastFrameNumber, int repeatingRequestId) {
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    if (CameraDeviceImpl.this.mRepeatingRequestId != -1) {
                        CameraDeviceImpl.this.checkEarlyTriggerSequenceComplete(CameraDeviceImpl.this.mRepeatingRequestId, lastFrameNumber);
                        if (CameraDeviceImpl.this.mRepeatingRequestId == repeatingRequestId) {
                            int unused = CameraDeviceImpl.this.mRepeatingRequestId = -1;
                        }
                    }
                }
            }
        }

        /* JADX INFO: finally extract failed */
        public void onDeviceIdle() {
            Log.i(CameraDeviceImpl.this.TAG, "Camera now idle");
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    if (!CameraDeviceImpl.this.mIdle) {
                        long ident = Binder.clearCallingIdentity();
                        try {
                            CameraDeviceImpl.this.mDeviceExecutor.execute(CameraDeviceImpl.this.mCallOnIdle);
                            Binder.restoreCallingIdentity(ident);
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(ident);
                            throw th;
                        }
                    }
                    boolean unused = CameraDeviceImpl.this.mIdle = true;
                }
            }
        }

        public void onCaptureStarted(CaptureResultExtras resultExtras, long timestamp) {
            long ident;
            int requestId = resultExtras.getRequestId();
            long frameNumber = resultExtras.getFrameNumber();
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                try {
                    if (CameraDeviceImpl.this.mRemoteDevice == null) {
                        try {
                        } catch (Throwable th) {
                            th = th;
                            int i = requestId;
                            throw th;
                        }
                    } else {
                        CaptureCallbackHolder holder = (CaptureCallbackHolder) CameraDeviceImpl.this.mCaptureCallbackMap.get(requestId);
                        if (holder != null) {
                            if (!CameraDeviceImpl.this.isClosed()) {
                                long ident2 = Binder.clearCallingIdentity();
                                try {
                                    Executor executor = holder.getExecutor();
                                    r1 = r1;
                                    final CaptureResultExtras captureResultExtras = resultExtras;
                                    final CaptureCallbackHolder captureCallbackHolder = holder;
                                    final long j = timestamp;
                                    int i2 = requestId;
                                    ident = ident2;
                                    final long ident3 = frameNumber;
                                    try {
                                        AnonymousClass1 r1 = new Runnable() {
                                            public void run() {
                                                if (!CameraDeviceImpl.this.isClosed()) {
                                                    int subsequenceId = captureResultExtras.getSubsequenceId();
                                                    CaptureRequest request = captureCallbackHolder.getRequest(subsequenceId);
                                                    if (captureCallbackHolder.hasBatchedOutputs()) {
                                                        Range<Integer> fpsRange = (Range) request.get(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE);
                                                        for (int i = 0; i < captureCallbackHolder.getRequestCount(); i++) {
                                                            captureCallbackHolder.getCallback().onCaptureStarted(CameraDeviceImpl.this, captureCallbackHolder.getRequest(i), j - ((((long) (subsequenceId - i)) * CameraDeviceImpl.NANO_PER_SECOND) / ((long) fpsRange.getUpper().intValue())), ident3 - ((long) (subsequenceId - i)));
                                                        }
                                                        return;
                                                    }
                                                    captureCallbackHolder.getCallback().onCaptureStarted(CameraDeviceImpl.this, captureCallbackHolder.getRequest(captureResultExtras.getSubsequenceId()), j, ident3);
                                                }
                                            }
                                        };
                                        executor.execute(r1);
                                        Binder.restoreCallingIdentity(ident);
                                    } catch (Throwable th2) {
                                        th = th2;
                                        throw th;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    int i3 = requestId;
                                    ident = ident2;
                                    Binder.restoreCallingIdentity(ident);
                                    throw th;
                                }
                            }
                        }
                    }
                } catch (Throwable th4) {
                    th = th4;
                    int i4 = requestId;
                    throw th;
                }
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v3, resolved type: android.hardware.camera2.TotalCaptureResult} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v20, resolved type: android.hardware.camera2.impl.CameraDeviceImpl$CameraDeviceCallbacks$2} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v24, resolved type: android.hardware.camera2.impl.CameraDeviceImpl$CameraDeviceCallbacks$3} */
        /* JADX WARNING: Multi-variable type inference failed */
        public void onResultReceived(CameraMetadataNative result, CaptureResultExtras resultExtras, PhysicalCaptureResultInfo[] physicalResults) throws RemoteException {
            Object obj;
            CameraMetadataNative resultCopy;
            long frameNumber;
            CaptureResult finalResult;
            CameraDeviceCallbacks cameraDeviceCallbacks;
            CaptureCallbackHolder holder;
            AnonymousClass3 r1;
            long ident;
            CameraMetadataNative cameraMetadataNative = result;
            int requestId = resultExtras.getRequestId();
            long frameNumber2 = resultExtras.getFrameNumber();
            Object obj2 = CameraDeviceImpl.this.mInterfaceLock;
            synchronized (obj2) {
                try {
                    if (CameraDeviceImpl.this.mRemoteDevice == null) {
                        try {
                        } catch (Throwable th) {
                            th = th;
                            obj = obj2;
                            long j = frameNumber2;
                            int i = requestId;
                            throw th;
                        }
                    } else {
                        cameraMetadataNative.set(CameraCharacteristics.LENS_INFO_SHADING_MAP_SIZE, (Size) CameraDeviceImpl.this.getCharacteristics().get(CameraCharacteristics.LENS_INFO_SHADING_MAP_SIZE));
                        CaptureCallbackHolder holder2 = (CaptureCallbackHolder) CameraDeviceImpl.this.mCaptureCallbackMap.get(requestId);
                        CaptureRequest request = holder2.getRequest(resultExtras.getSubsequenceId());
                        boolean isPartialResult = resultExtras.getPartialResultCount() < CameraDeviceImpl.this.mTotalPartialCount;
                        boolean isReprocess = request.isReprocess();
                        if (holder2 == null) {
                            CameraDeviceImpl.this.mFrameNumberTracker.updateTracker(frameNumber2, null, isPartialResult, isReprocess);
                        } else if (CameraDeviceImpl.this.isClosed()) {
                            CameraDeviceImpl.this.mFrameNumberTracker.updateTracker(frameNumber2, null, isPartialResult, isReprocess);
                        } else {
                            if (holder2.hasBatchedOutputs()) {
                                resultCopy = new CameraMetadataNative(cameraMetadataNative);
                            } else {
                                resultCopy = null;
                            }
                            final CameraMetadataNative resultCopy2 = resultCopy;
                            if (isPartialResult) {
                                CaptureResultExtras captureResultExtras = resultExtras;
                                final CaptureResult resultAsCapture = new CaptureResult(cameraMetadataNative, request, captureResultExtras);
                                final CaptureCallbackHolder captureCallbackHolder = holder2;
                                final CaptureResultExtras captureResultExtras2 = captureResultExtras;
                                final CaptureRequest captureRequest = request;
                                AnonymousClass2 r12 = new Runnable() {
                                    public void run() {
                                        if (CameraDeviceImpl.this.isClosed()) {
                                            return;
                                        }
                                        if (captureCallbackHolder.hasBatchedOutputs()) {
                                            for (int i = 0; i < captureCallbackHolder.getRequestCount(); i++) {
                                                captureCallbackHolder.getCallback().onCaptureProgressed(CameraDeviceImpl.this, captureCallbackHolder.getRequest(i), new CaptureResult(new CameraMetadataNative(resultCopy2), captureCallbackHolder.getRequest(i), captureResultExtras2));
                                            }
                                            return;
                                        }
                                        captureCallbackHolder.getCallback().onCaptureProgressed(CameraDeviceImpl.this, captureRequest, resultAsCapture);
                                    }
                                };
                                CameraMetadataNative cameraMetadataNative2 = resultCopy2;
                                obj = obj2;
                                holder = holder2;
                                CaptureRequest captureRequest2 = request;
                                frameNumber = frameNumber2;
                                int i2 = requestId;
                                cameraDeviceCallbacks = this;
                                finalResult = resultAsCapture;
                                r1 = r12;
                            } else {
                                List<CaptureResult> partialResults = CameraDeviceImpl.this.mFrameNumberTracker.popPartialResults(frameNumber2);
                                holder = holder2;
                                CaptureRequest request2 = request;
                                final long sensorTimestamp = ((Long) cameraMetadataNative.get(CaptureResult.SENSOR_TIMESTAMP)).longValue();
                                frameNumber = frameNumber2;
                                final Range<Integer> fpsRange = (Range) request2.get(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE);
                                try {
                                    final int subsequenceId = resultExtras.getSubsequenceId();
                                    TotalCaptureResult totalCaptureResult = new TotalCaptureResult(cameraMetadataNative, request2, resultExtras, partialResults, holder.getSessionId(), physicalResults);
                                    final TotalCaptureResult resultAsCapture2 = totalCaptureResult;
                                    r1 = r1;
                                    CaptureRequest request3 = request2;
                                    final CaptureCallbackHolder captureCallbackHolder2 = holder;
                                    obj = obj2;
                                    final CameraMetadataNative cameraMetadataNative3 = resultCopy2;
                                    int i3 = requestId;
                                    final CaptureResultExtras captureResultExtras3 = resultExtras;
                                    final List<CaptureResult> list = partialResults;
                                    CameraMetadataNative cameraMetadataNative4 = resultCopy2;
                                    cameraDeviceCallbacks = this;
                                    final CaptureRequest captureRequest3 = request3;
                                    try {
                                        r1 = new Runnable() {
                                            public void run() {
                                                if (CameraDeviceImpl.this.isClosed()) {
                                                    return;
                                                }
                                                if (captureCallbackHolder2.hasBatchedOutputs()) {
                                                    for (int i = 0; i < captureCallbackHolder2.getRequestCount(); i++) {
                                                        cameraMetadataNative3.set(CaptureResult.SENSOR_TIMESTAMP, Long.valueOf(sensorTimestamp - ((((long) (subsequenceId - i)) * CameraDeviceImpl.NANO_PER_SECOND) / ((long) ((Integer) fpsRange.getUpper()).intValue()))));
                                                        TotalCaptureResult totalCaptureResult = new TotalCaptureResult(new CameraMetadataNative(cameraMetadataNative3), captureCallbackHolder2.getRequest(i), captureResultExtras3, list, captureCallbackHolder2.getSessionId(), new PhysicalCaptureResultInfo[0]);
                                                        captureCallbackHolder2.getCallback().onCaptureCompleted(CameraDeviceImpl.this, captureCallbackHolder2.getRequest(i), totalCaptureResult);
                                                    }
                                                    return;
                                                }
                                                captureCallbackHolder2.getCallback().onCaptureCompleted(CameraDeviceImpl.this, captureRequest3, resultAsCapture2);
                                            }
                                        };
                                        Runnable resultDispatch = r1;
                                        finalResult = resultAsCapture2;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        throw th;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    obj = obj2;
                                    int i4 = requestId;
                                    throw th;
                                }
                            }
                            ident = Binder.clearCallingIdentity();
                            holder.getExecutor().execute(r1);
                            Binder.restoreCallingIdentity(ident);
                            CameraDeviceImpl.this.mFrameNumberTracker.updateTracker(frameNumber, finalResult, isPartialResult, isReprocess);
                            if (!isPartialResult) {
                                CameraDeviceImpl.this.checkAndFireSequenceComplete();
                            }
                        }
                    }
                } catch (Throwable th4) {
                    th = th4;
                    obj = obj2;
                    long j2 = frameNumber2;
                    int i5 = requestId;
                    throw th;
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

        /* JADX INFO: finally extract failed */
        private void onCaptureErrorLocked(int errorCode, CaptureResultExtras resultExtras) {
            int i = errorCode;
            int requestId = resultExtras.getRequestId();
            int subsequenceId = resultExtras.getSubsequenceId();
            long frameNumber = resultExtras.getFrameNumber();
            final CaptureCallbackHolder holder = (CaptureCallbackHolder) CameraDeviceImpl.this.mCaptureCallbackMap.get(requestId);
            final CaptureRequest request = holder.getRequest(subsequenceId);
            if (i == 5) {
                List<Surface> surfaces = ((OutputConfiguration) CameraDeviceImpl.this.mConfiguredOutputs.get(resultExtras.getErrorStreamId())).getSurfaces();
                Iterator<Surface> it = surfaces.iterator();
                while (it.hasNext()) {
                    final Surface surface = it.next();
                    if (request.containsTarget(surface)) {
                        final CaptureCallbackHolder captureCallbackHolder = holder;
                        final CaptureRequest captureRequest = request;
                        Surface surface2 = surface;
                        List<Surface> surfaces2 = surfaces;
                        Iterator<Surface> it2 = it;
                        final long j = frameNumber;
                        AnonymousClass4 r1 = new Runnable() {
                            public void run() {
                                if (!CameraDeviceImpl.this.isClosed()) {
                                    captureCallbackHolder.getCallback().onCaptureBufferLost(CameraDeviceImpl.this, captureRequest, surface, j);
                                }
                            }
                        };
                        long ident = Binder.clearCallingIdentity();
                        try {
                            holder.getExecutor().execute(r1);
                            Binder.restoreCallingIdentity(ident);
                            AnonymousClass4 r0 = r1;
                            surfaces = surfaces2;
                            it = it2;
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(ident);
                            throw th;
                        }
                    }
                }
                return;
            }
            final CaptureFailure failure = new CaptureFailure(request, (CameraDeviceImpl.this.mCurrentSession == null || !CameraDeviceImpl.this.mCurrentSession.isAborting()) ? 0 : 1, i == 4, requestId, frameNumber);
            AnonymousClass5 r2 = new Runnable() {
                public void run() {
                    if (!CameraDeviceImpl.this.isClosed()) {
                        holder.getCallback().onCaptureFailed(CameraDeviceImpl.this, request, failure);
                    }
                }
            };
            CameraDeviceImpl.this.mFrameNumberTracker.updateTracker(frameNumber, true, request.isReprocess());
            CameraDeviceImpl.this.checkAndFireSequenceComplete();
            long ident2 = Binder.clearCallingIdentity();
            try {
                holder.getExecutor().execute(r2);
                Binder.restoreCallingIdentity(ident2);
                AnonymousClass5 r02 = r2;
            } catch (Throwable th2) {
                Binder.restoreCallingIdentity(ident2);
                throw th2;
            }
        }
    }

    private static class CameraHandlerExecutor implements Executor {
        private final Handler mHandler;

        public CameraHandlerExecutor(Handler handler) {
            this.mHandler = (Handler) Preconditions.checkNotNull(handler);
        }

        public void execute(Runnable command) {
            this.mHandler.post(command);
        }
    }

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

    static class CaptureCallbackHolder {
        private final CaptureCallback mCallback;
        private final Executor mExecutor;
        private final boolean mHasBatchedOutputs;
        private final boolean mRepeating;
        private final List<CaptureRequest> mRequestList;
        private final int mSessionId;

        CaptureCallbackHolder(CaptureCallback callback, List<CaptureRequest> requestList, Executor executor, boolean repeating, int sessionId) {
            if (callback == null || executor == null) {
                throw new UnsupportedOperationException("Must have a valid handler and a valid callback");
            }
            this.mRepeating = repeating;
            this.mExecutor = executor;
            this.mRequestList = new ArrayList(requestList);
            this.mCallback = callback;
            this.mSessionId = sessionId;
            boolean hasBatchedOutputs = true;
            int i = 0;
            while (true) {
                if (i >= requestList.size()) {
                    break;
                }
                CaptureRequest request = requestList.get(i);
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
                return this.mRequestList.get(subsequenceId);
            } else {
                throw new IllegalArgumentException(String.format("Requested subsequenceId %d is negative", new Object[]{Integer.valueOf(subsequenceId)}));
            }
        }

        public CaptureRequest getRequest() {
            return getRequest(0);
        }

        public Executor getExecutor() {
            return this.mExecutor;
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
        private final TreeMap<Long, Boolean> mFutureErrorMap = new TreeMap<>();
        private final HashMap<Long, List<CaptureResult>> mPartialResults = new HashMap<>();
        private final LinkedList<Long> mSkippedRegularFrameNumbers = new LinkedList<>();
        private final LinkedList<Long> mSkippedReprocessFrameNumbers = new LinkedList<>();

        public FrameNumberTracker() {
        }

        private void update() {
            Iterator iter = this.mFutureErrorMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry pair = iter.next();
                Long errorFrameNumber = (Long) pair.getKey();
                Boolean removeError = true;
                if (((Boolean) pair.getValue()).booleanValue()) {
                    if (errorFrameNumber.longValue() == this.mCompletedReprocessFrameNumber + 1) {
                        this.mCompletedReprocessFrameNumber = errorFrameNumber.longValue();
                    } else if (this.mSkippedReprocessFrameNumbers.isEmpty() || errorFrameNumber != this.mSkippedReprocessFrameNumbers.element()) {
                        removeError = false;
                    } else {
                        this.mCompletedReprocessFrameNumber = errorFrameNumber.longValue();
                        this.mSkippedReprocessFrameNumbers.remove();
                    }
                } else if (errorFrameNumber.longValue() == this.mCompletedFrameNumber + 1) {
                    this.mCompletedFrameNumber = errorFrameNumber.longValue();
                } else if (this.mSkippedRegularFrameNumbers.isEmpty() || errorFrameNumber != this.mSkippedRegularFrameNumbers.element()) {
                    removeError = false;
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
                List<CaptureResult> partials = this.mPartialResults.get(Long.valueOf(frameNumber));
                if (partials == null) {
                    partials = new ArrayList<>();
                    this.mPartialResults.put(Long.valueOf(frameNumber), partials);
                }
                partials.add(result);
            }
        }

        public List<CaptureResult> popPartialResults(long frameNumber) {
            return this.mPartialResults.remove(Long.valueOf(frameNumber));
        }

        public long getCompletedFrameNumber() {
            return this.mCompletedFrameNumber;
        }

        public long getCompletedReprocessFrameNumber() {
            return this.mCompletedReprocessFrameNumber;
        }

        private void updateCompletedFrameNumber(long frameNumber) throws IllegalArgumentException {
            if (frameNumber > this.mCompletedFrameNumber) {
                if (frameNumber > this.mCompletedReprocessFrameNumber) {
                    long i = Math.max(this.mCompletedFrameNumber, this.mCompletedReprocessFrameNumber);
                    while (true) {
                        i++;
                        if (i >= frameNumber) {
                            break;
                        }
                        this.mSkippedReprocessFrameNumbers.add(Long.valueOf(i));
                    }
                } else if (this.mSkippedRegularFrameNumbers.isEmpty() || frameNumber < this.mSkippedRegularFrameNumbers.element().longValue()) {
                    throw new IllegalArgumentException("frame number " + frameNumber + " is a repeat");
                } else if (frameNumber <= this.mSkippedRegularFrameNumbers.element().longValue()) {
                    this.mSkippedRegularFrameNumbers.remove();
                } else {
                    throw new IllegalArgumentException("frame number " + frameNumber + " comes out of order. Expecting " + this.mSkippedRegularFrameNumbers.element());
                }
                this.mCompletedFrameNumber = frameNumber;
                return;
            }
            throw new IllegalArgumentException("frame number " + frameNumber + " is a repeat");
        }

        private void updateCompletedReprocessFrameNumber(long frameNumber) throws IllegalArgumentException {
            if (frameNumber >= this.mCompletedReprocessFrameNumber) {
                if (frameNumber >= this.mCompletedFrameNumber) {
                    long i = Math.max(this.mCompletedFrameNumber, this.mCompletedReprocessFrameNumber);
                    while (true) {
                        i++;
                        if (i >= frameNumber) {
                            break;
                        }
                        this.mSkippedRegularFrameNumbers.add(Long.valueOf(i));
                    }
                } else if (this.mSkippedReprocessFrameNumbers.isEmpty() || frameNumber < this.mSkippedReprocessFrameNumbers.element().longValue()) {
                    throw new IllegalArgumentException("frame number " + frameNumber + " is a repeat");
                } else if (frameNumber <= this.mSkippedReprocessFrameNumbers.element().longValue()) {
                    this.mSkippedReprocessFrameNumbers.remove();
                } else {
                    throw new IllegalArgumentException("frame number " + frameNumber + " comes out of order. Expecting " + this.mSkippedReprocessFrameNumbers.element());
                }
                this.mCompletedReprocessFrameNumber = frameNumber;
                return;
            }
            throw new IllegalArgumentException("frame number " + frameNumber + " is a repeat");
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
            if (requestInfo.getLastFrameNumber() >= ((long) (requestList.size() - 1))) {
                for (int i = requestList.size() - 1; i >= 0; i--) {
                    CaptureRequest request = requestList.get(i);
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
                return;
            }
            throw new IllegalArgumentException("lastFrameNumber: " + requestInfo.getLastFrameNumber() + " should be at least " + (requestList.size() - 1) + " for the number of  requests in the list: " + requestList.size());
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

    public static abstract class StateCallbackKK extends CameraDevice.StateCallback {
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

    public CameraDeviceImpl(String cameraId, CameraDevice.StateCallback callback, Executor executor, CameraCharacteristics characteristics, int appTargetSdkVersion) {
        if (cameraId == null || callback == null || executor == null || characteristics == null) {
            throw new IllegalArgumentException("Null argument given");
        }
        this.mCameraId = cameraId;
        this.mDeviceCallback = callback;
        this.mDeviceExecutor = executor;
        this.mCharacteristics = characteristics;
        this.mAppTargetSdkVersion = appTargetSdkVersion;
        String tag = String.format("CameraDevice-JV-%s", new Object[]{this.mCameraId});
        this.TAG = tag.length() > 23 ? tag.substring(0, 23) : tag;
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
            if (!this.mInError) {
                this.mRemoteDevice = new ICameraDeviceUserWrapper(remoteDevice);
                IBinder remoteDeviceBinder = remoteDevice.asBinder();
                if (remoteDeviceBinder != null) {
                    try {
                        remoteDeviceBinder.linkToDeath(this, 0);
                    } catch (RemoteException e) {
                        this.mDeviceExecutor.execute(this.mCallOnDisconnected);
                        throw new CameraAccessException(2, "The camera device has encountered a serious error");
                    }
                }
                this.mDeviceExecutor.execute(this.mCallOnOpened);
                this.mDeviceExecutor.execute(this.mCallOnUnconfigured);
            }
        }
    }

    public void setRemoteFailure(ServiceSpecificException failure) {
        int failureCode = 4;
        boolean failureIsError = true;
        int i = failure.errorCode;
        if (i == 4) {
            failureIsError = false;
        } else if (i != 10) {
            switch (i) {
                case 6:
                    failureCode = 3;
                    break;
                case 7:
                    failureCode = 1;
                    break;
                case 8:
                    failureCode = 2;
                    break;
                default:
                    String str = this.TAG;
                    Log.e(str, "Unexpected failure in opening camera device: " + failure.errorCode + failure.getMessage());
                    break;
            }
        } else {
            failureCode = 4;
        }
        final int code = failureCode;
        final boolean isError = failureIsError;
        synchronized (this.mInterfaceLock) {
            this.mInError = true;
            this.mDeviceExecutor.execute(new Runnable() {
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
        ArrayList<OutputConfiguration> outputConfigs = new ArrayList<>(outputs.size());
        for (Surface s : outputs) {
            outputConfigs.add(new OutputConfiguration(s));
        }
        configureStreamsChecked(null, outputConfigs, 0, null);
    }

    public boolean configureStreamsChecked(InputConfiguration inputConfig, List<OutputConfiguration> outputs, int operatingMode, CaptureRequest sessionParams) throws CameraAccessException {
        if (outputs == null) {
            outputs = new ArrayList<>();
        }
        if (outputs.size() != 0 || inputConfig == null) {
            checkInputConfiguration(inputConfig);
            synchronized (this.mInterfaceLock) {
                checkIfCameraClosedOrInError();
                HashSet<OutputConfiguration> addSet = new HashSet<>(outputs);
                List<Integer> deleteList = new ArrayList<>();
                for (int i = 0; i < this.mConfiguredOutputs.size(); i++) {
                    int streamId = this.mConfiguredOutputs.keyAt(i);
                    OutputConfiguration outConfig = this.mConfiguredOutputs.valueAt(i);
                    if (outputs.contains(outConfig)) {
                        if (!outConfig.isDeferredConfiguration()) {
                            addSet.remove(outConfig);
                        }
                    }
                    deleteList.add(Integer.valueOf(streamId));
                }
                this.mDeviceExecutor.execute(this.mCallOnBusy);
                stopRepeating();
                try {
                    waitUntilIdle();
                    this.mRemoteDevice.beginConfigure();
                    InputConfiguration currentInputConfig = this.mConfiguredInput.getValue();
                    if (inputConfig != currentInputConfig && (inputConfig == null || !inputConfig.equals(currentInputConfig))) {
                        if (currentInputConfig != null) {
                            this.mRemoteDevice.deleteStream(this.mConfiguredInput.getKey().intValue());
                            this.mConfiguredInput = new AbstractMap.SimpleEntry<>(-1, null);
                        }
                        if (inputConfig != null) {
                            this.mConfiguredInput = new AbstractMap.SimpleEntry<>(Integer.valueOf(this.mRemoteDevice.createInputStream(inputConfig.getWidth(), inputConfig.getHeight(), inputConfig.getFormat())), inputConfig);
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
                    if (sessionParams != null) {
                        this.mRemoteDevice.endConfigure(operatingMode, sessionParams.getNativeCopy());
                    } else {
                        this.mRemoteDevice.endConfigure(operatingMode, null);
                    }
                    if (1 != 0) {
                        if (outputs.size() > 0) {
                            this.mDeviceExecutor.execute(this.mCallOnIdle);
                        }
                    }
                    this.mDeviceExecutor.execute(this.mCallOnUnconfigured);
                } catch (IllegalArgumentException e) {
                    Log.w(this.TAG, "Stream configuration failed due to: " + e.getMessage());
                    if (0 != 0) {
                        if (outputs.size() > 0) {
                            this.mDeviceExecutor.execute(this.mCallOnIdle);
                            return false;
                        }
                    }
                    this.mDeviceExecutor.execute(this.mCallOnUnconfigured);
                    return false;
                } catch (CameraAccessException e2) {
                    if (e2.getReason() == 4) {
                        throw new IllegalStateException("The camera is currently busy. You must wait until the previous operation completes.", e2);
                    }
                    throw e2;
                } catch (Throwable th) {
                    if (0 == 0 || outputs.size() <= 0) {
                        this.mDeviceExecutor.execute(this.mCallOnUnconfigured);
                    } else {
                        this.mDeviceExecutor.execute(this.mCallOnIdle);
                    }
                    throw th;
                }
            }
            return true;
        }
        throw new IllegalArgumentException("cannot configure an input stream without any output streams");
    }

    public void createCaptureSession(List<Surface> outputs, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        ArrayList arrayList = new ArrayList(outputs.size());
        for (Surface surface : outputs) {
            arrayList.add(new OutputConfiguration(surface));
        }
        createCaptureSessionInternal(null, arrayList, callback, checkAndWrapHandler(handler), 0, null);
    }

    public void createCaptureSessionByOutputConfigurations(List<OutputConfiguration> outputConfigurations, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        createCaptureSessionInternal(null, new ArrayList<>(outputConfigurations), callback, checkAndWrapHandler(handler), 0, null);
    }

    public void createReprocessableCaptureSession(InputConfiguration inputConfig, List<Surface> outputs, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        if (inputConfig != null) {
            ArrayList arrayList = new ArrayList(outputs.size());
            for (Surface surface : outputs) {
                arrayList.add(new OutputConfiguration(surface));
            }
            createCaptureSessionInternal(inputConfig, arrayList, callback, checkAndWrapHandler(handler), 0, null);
            return;
        }
        throw new IllegalArgumentException("inputConfig cannot be null when creating a reprocessable capture session");
    }

    public void createReprocessableCaptureSessionByConfigurations(InputConfiguration inputConfig, List<OutputConfiguration> outputs, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        if (inputConfig == null) {
            throw new IllegalArgumentException("inputConfig cannot be null when creating a reprocessable capture session");
        } else if (outputs != null) {
            ArrayList arrayList = new ArrayList();
            for (OutputConfiguration output : outputs) {
                arrayList.add(new OutputConfiguration(output));
            }
            createCaptureSessionInternal(inputConfig, arrayList, callback, checkAndWrapHandler(handler), 0, null);
        } else {
            throw new IllegalArgumentException("Output configurations cannot be null when creating a reprocessable capture session");
        }
    }

    public void createConstrainedHighSpeedCaptureSession(List<Surface> outputs, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        if (outputs == null || outputs.size() == 0 || outputs.size() > 2) {
            throw new IllegalArgumentException("Output surface list must not be null and the size must be no more than 2");
        }
        ArrayList arrayList = new ArrayList(outputs.size());
        for (Surface surface : outputs) {
            arrayList.add(new OutputConfiguration(surface));
        }
        createCaptureSessionInternal(null, arrayList, callback, checkAndWrapHandler(handler), 1, null);
    }

    public void createCustomCaptureSession(InputConfiguration inputConfig, List<OutputConfiguration> outputs, int operatingMode, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        ArrayList arrayList = new ArrayList();
        for (OutputConfiguration output : outputs) {
            arrayList.add(new OutputConfiguration(output));
        }
        createCaptureSessionInternal(inputConfig, arrayList, callback, checkAndWrapHandler(handler), operatingMode, null);
    }

    public void createCaptureSession(SessionConfiguration config) throws CameraAccessException {
        if (config != null) {
            List<OutputConfiguration> outputConfigs = config.getOutputConfigurations();
            if (outputConfigs == null) {
                throw new IllegalArgumentException("Invalid output configurations");
            } else if (config.getExecutor() != null) {
                createCaptureSessionInternal(config.getInputConfiguration(), outputConfigs, config.getStateCallback(), config.getExecutor(), config.getSessionType(), config.getSessionParameters());
            } else {
                throw new IllegalArgumentException("Invalid executor");
            }
        } else {
            throw new IllegalArgumentException("Invalid session configuration");
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: android.hardware.camera2.impl.CameraCaptureSessionImpl} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v6, resolved type: android.hardware.camera2.impl.CameraCaptureSessionImpl} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v13, resolved type: android.hardware.camera2.impl.CameraConstrainedHighSpeedCaptureSessionImpl} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v14, resolved type: android.hardware.camera2.impl.CameraCaptureSessionImpl} */
    /* JADX WARNING: Multi-variable type inference failed */
    private void createCaptureSessionInternal(InputConfiguration inputConfig, List<OutputConfiguration> outputConfigurations, CameraCaptureSession.StateCallback callback, Executor executor, int operatingMode, CaptureRequest sessionParams) throws CameraAccessException {
        CameraAccessException pendingException;
        boolean configureSuccess;
        Surface input;
        CameraCaptureSessionCore newSession;
        InputConfiguration inputConfiguration = inputConfig;
        int i = operatingMode;
        synchronized (this.mInterfaceLock) {
            try {
                checkIfCameraClosedOrInError();
                boolean isConstrainedHighSpeed = i == 1;
                if (isConstrainedHighSpeed) {
                    if (inputConfiguration != null) {
                        throw new IllegalArgumentException("Constrained high speed session doesn't support input configuration yet.");
                    }
                }
                if (this.mCurrentSession != null) {
                    this.mCurrentSession.replaceSessionClose();
                }
                Surface input2 = null;
                try {
                    boolean configureSuccess2 = configureStreamsChecked(inputConfiguration, outputConfigurations, i, sessionParams);
                    if (configureSuccess2 && inputConfiguration != null) {
                        input2 = this.mRemoteDevice.getInputSurface();
                    }
                    configureSuccess = configureSuccess2;
                    pendingException = null;
                    input = input2;
                } catch (CameraAccessException e) {
                    CameraAccessException pendingException2 = e;
                    input = null;
                    configureSuccess = false;
                    pendingException = pendingException2;
                }
                if (isConstrainedHighSpeed) {
                    ArrayList<Surface> surfaces = new ArrayList<>(outputConfigurations.size());
                    for (OutputConfiguration outConfig : outputConfigurations) {
                        surfaces.add(outConfig.getSurface());
                    }
                    StreamConfigurationMap config = (StreamConfigurationMap) getCharacteristics().get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    SurfaceUtils.checkConstrainedHighSpeedSurfaces(surfaces, null, config);
                    int i2 = this.mNextSessionId;
                    this.mNextSessionId = i2 + 1;
                    StreamConfigurationMap streamConfigurationMap = config;
                    ArrayList<Surface> arrayList = surfaces;
                    CameraConstrainedHighSpeedCaptureSessionImpl cameraConstrainedHighSpeedCaptureSessionImpl = new CameraConstrainedHighSpeedCaptureSessionImpl(i2, callback, executor, this, this.mDeviceExecutor, configureSuccess, this.mCharacteristics);
                    newSession = cameraConstrainedHighSpeedCaptureSessionImpl;
                } else {
                    int i3 = this.mNextSessionId;
                    this.mNextSessionId = i3 + 1;
                    CameraCaptureSessionImpl cameraCaptureSessionImpl = new CameraCaptureSessionImpl(i3, input, callback, executor, this, this.mDeviceExecutor, configureSuccess);
                    newSession = cameraCaptureSessionImpl;
                }
                this.mCurrentSession = newSession;
                if (pendingException == null) {
                    this.mSessionStateCallback = this.mCurrentSession.getDeviceStateCallback();
                    return;
                }
                throw pendingException;
            } catch (Throwable th) {
                th = th;
                throw th;
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

    public CaptureRequest.Builder createCaptureRequest(int templateType, Set<String> physicalCameraIdSet) throws CameraAccessException {
        CaptureRequest.Builder builder;
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            for (String physicalId : physicalCameraIdSet) {
                if (physicalId == getId()) {
                    throw new IllegalStateException("Physical id matches the logical id!");
                }
            }
            CameraMetadataNative templatedRequest = this.mRemoteDevice.createDefaultRequest(templateType);
            if (this.mAppTargetSdkVersion < 26 || templateType != 2) {
                overrideEnableZsl(templatedRequest, false);
            }
            builder = new CaptureRequest.Builder(templatedRequest, false, -1, getId(), physicalCameraIdSet);
        }
        return builder;
    }

    public CaptureRequest.Builder createCaptureRequest(int templateType) throws CameraAccessException {
        CaptureRequest.Builder builder;
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            CameraMetadataNative templatedRequest = this.mRemoteDevice.createDefaultRequest(templateType);
            if (this.mAppTargetSdkVersion < 26 || templateType != 2) {
                overrideEnableZsl(templatedRequest, false);
            }
            builder = new CaptureRequest.Builder(templatedRequest, false, -1, getId(), null);
        }
        return builder;
    }

    public CaptureRequest.Builder createReprocessCaptureRequest(TotalCaptureResult inputResult) throws CameraAccessException {
        CaptureRequest.Builder builder;
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            builder = new CaptureRequest.Builder(new CameraMetadataNative(inputResult.getNativeCopy()), true, inputResult.getSessionId(), getId(), null);
        }
        return builder;
    }

    public void prepare(Surface surface) throws CameraAccessException {
        if (surface != null) {
            synchronized (this.mInterfaceLock) {
                int streamId = -1;
                int i = 0;
                while (true) {
                    if (i >= this.mConfiguredOutputs.size()) {
                        break;
                    } else if (this.mConfiguredOutputs.valueAt(i).getSurfaces().contains(surface)) {
                        streamId = this.mConfiguredOutputs.keyAt(i);
                        break;
                    } else {
                        i++;
                    }
                }
                if (streamId != -1) {
                    this.mRemoteDevice.prepare(streamId);
                } else {
                    throw new IllegalArgumentException("Surface is not part of this session");
                }
            }
            return;
        }
        throw new IllegalArgumentException("Surface is null");
    }

    public void prepare(int maxCount, Surface surface) throws CameraAccessException {
        if (surface == null) {
            throw new IllegalArgumentException("Surface is null");
        } else if (maxCount > 0) {
            synchronized (this.mInterfaceLock) {
                int streamId = -1;
                int i = 0;
                while (true) {
                    if (i >= this.mConfiguredOutputs.size()) {
                        break;
                    } else if (surface == this.mConfiguredOutputs.valueAt(i).getSurface()) {
                        streamId = this.mConfiguredOutputs.keyAt(i);
                        break;
                    } else {
                        i++;
                    }
                }
                if (streamId != -1) {
                    this.mRemoteDevice.prepare2(maxCount, streamId);
                } else {
                    throw new IllegalArgumentException("Surface is not part of this session");
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid maxCount given: " + maxCount);
        }
    }

    public void updateOutputConfiguration(OutputConfiguration config) throws CameraAccessException {
        synchronized (this.mInterfaceLock) {
            int streamId = -1;
            int i = 0;
            while (true) {
                if (i >= this.mConfiguredOutputs.size()) {
                    break;
                } else if (config.getSurface() == this.mConfiguredOutputs.valueAt(i).getSurface()) {
                    streamId = this.mConfiguredOutputs.keyAt(i);
                    break;
                } else {
                    i++;
                }
            }
            if (streamId != -1) {
                this.mRemoteDevice.updateOutputConfiguration(streamId, config);
                this.mConfiguredOutputs.put(streamId, config);
            } else {
                throw new IllegalArgumentException("Invalid output configuration");
            }
        }
    }

    public void tearDown(Surface surface) throws CameraAccessException {
        if (surface != null) {
            synchronized (this.mInterfaceLock) {
                int streamId = -1;
                int i = 0;
                while (true) {
                    if (i >= this.mConfiguredOutputs.size()) {
                        break;
                    } else if (surface == this.mConfiguredOutputs.valueAt(i).getSurface()) {
                        streamId = this.mConfiguredOutputs.keyAt(i);
                        break;
                    } else {
                        i++;
                    }
                }
                if (streamId != -1) {
                    this.mRemoteDevice.tearDown(streamId);
                } else {
                    throw new IllegalArgumentException("Surface is not part of this session");
                }
            }
            return;
        }
        throw new IllegalArgumentException("Surface is null");
    }

    public void finalizeOutputConfigs(List<OutputConfiguration> outputConfigs) throws CameraAccessException {
        if (outputConfigs == null || outputConfigs.size() == 0) {
            throw new IllegalArgumentException("deferred config is null or empty");
        }
        synchronized (this.mInterfaceLock) {
            for (OutputConfiguration config : outputConfigs) {
                int streamId = -1;
                int i = 0;
                while (true) {
                    if (i >= this.mConfiguredOutputs.size()) {
                        break;
                    } else if (config.equals(this.mConfiguredOutputs.valueAt(i))) {
                        streamId = this.mConfiguredOutputs.keyAt(i);
                        break;
                    } else {
                        i++;
                    }
                }
                if (streamId == -1) {
                    throw new IllegalArgumentException("Deferred config is not part of this session");
                } else if (config.getSurfaces().size() != 0) {
                    this.mRemoteDevice.finalizeOutputConfigurations(streamId, config);
                    this.mConfiguredOutputs.put(streamId, config);
                } else {
                    throw new IllegalArgumentException("The final config for stream " + streamId + " must have at least 1 surface");
                }
            }
        }
    }

    public int capture(CaptureRequest request, CaptureCallback callback, Executor executor) throws CameraAccessException {
        List<CaptureRequest> requestList = new ArrayList<>();
        requestList.add(request);
        return submitCaptureRequest(requestList, callback, executor, false);
    }

    public int captureBurst(List<CaptureRequest> requests, CaptureCallback callback, Executor executor) throws CameraAccessException {
        if (requests != null && !requests.isEmpty()) {
            return submitCaptureRequest(requests, callback, executor, false);
        }
        throw new IllegalArgumentException("At least one request must be given");
    }

    /* access modifiers changed from: private */
    public void checkEarlyTriggerSequenceComplete(final int requestId, long lastFrameNumber) {
        if (lastFrameNumber == -1) {
            int index = this.mCaptureCallbackMap.indexOfKey(requestId);
            final CaptureCallbackHolder holder = index >= 0 ? this.mCaptureCallbackMap.valueAt(index) : null;
            if (holder != null) {
                this.mCaptureCallbackMap.removeAt(index);
            }
            if (holder != null) {
                Runnable resultDispatch = new Runnable() {
                    public void run() {
                        if (!CameraDeviceImpl.this.isClosed()) {
                            holder.getCallback().onCaptureSequenceAborted(CameraDeviceImpl.this, requestId);
                        }
                    }
                };
                long ident = Binder.clearCallingIdentity();
                try {
                    holder.getExecutor().execute(resultDispatch);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                Log.w(this.TAG, String.format("did not register callback to request %d", new Object[]{Integer.valueOf(requestId)}));
            }
        } else {
            this.mRequestLastFrameNumbersList.add(new RequestLastFrameNumbersHolder(requestId, lastFrameNumber));
            checkAndFireSequenceComplete();
        }
    }

    private int submitCaptureRequest(List<CaptureRequest> requestList, CaptureCallback callback, Executor executor, boolean repeating) throws CameraAccessException {
        int requestId;
        List<CaptureRequest> list = requestList;
        CaptureCallback captureCallback = callback;
        boolean z = repeating;
        Executor executor2 = checkExecutor(executor, captureCallback);
        for (CaptureRequest request : requestList) {
            if (!request.getTargets().isEmpty()) {
                Iterator<Surface> it = request.getTargets().iterator();
                while (true) {
                    if (it.hasNext()) {
                        Surface surface = it.next();
                        if (surface != null) {
                            int i = 0;
                            while (true) {
                                if (i < this.mConfiguredOutputs.size()) {
                                    OutputConfiguration configuration = this.mConfiguredOutputs.valueAt(i);
                                    if (!configuration.isForPhysicalCamera() || !configuration.getSurfaces().contains(surface) || !request.isReprocess()) {
                                        i++;
                                    } else {
                                        throw new IllegalArgumentException("Reprocess request on physical stream is not allowed");
                                    }
                                }
                            }
                        } else {
                            throw new IllegalArgumentException("Null Surface targets are not allowed");
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Each request must have at least one Surface target");
            }
        }
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            if (z) {
                stopRepeating();
            }
            CaptureRequest[] requestArray = (CaptureRequest[]) list.toArray(new CaptureRequest[requestList.size()]);
            for (CaptureRequest request2 : requestArray) {
                request2.convertSurfaceToStreamId(this.mConfiguredOutputs);
            }
            SubmitInfo requestInfo = this.mRemoteDevice.submitRequestList(requestArray, z);
            for (CaptureRequest request3 : requestArray) {
                request3.recoverStreamIdToSurface();
            }
            if (captureCallback != null) {
                CaptureCallbackHolder captureCallbackHolder = r2;
                CaptureRequest[] captureRequestArr = requestArray;
                CaptureCallbackHolder captureCallbackHolder2 = new CaptureCallbackHolder(captureCallback, list, executor2, z, this.mNextSessionId - 1);
                this.mCaptureCallbackMap.put(requestInfo.getRequestId(), captureCallbackHolder);
            }
            if (z) {
                if (this.mRepeatingRequestId != -1) {
                    checkEarlyTriggerSequenceComplete(this.mRepeatingRequestId, requestInfo.getLastFrameNumber());
                }
                this.mRepeatingRequestId = requestInfo.getRequestId();
            } else {
                this.mRequestLastFrameNumbersList.add(new RequestLastFrameNumbersHolder(list, requestInfo));
            }
            if (this.mIdle) {
                this.mDeviceExecutor.execute(this.mCallOnActive);
            }
            this.mIdle = false;
            requestId = requestInfo.getRequestId();
        }
        return requestId;
    }

    public int setRepeatingRequest(CaptureRequest request, CaptureCallback callback, Executor executor) throws CameraAccessException {
        List<CaptureRequest> requestList = new ArrayList<>();
        requestList.add(request);
        return submitCaptureRequest(requestList, callback, executor, true);
    }

    public int setRepeatingBurst(List<CaptureRequest> requests, CaptureCallback callback, Executor executor) throws CameraAccessException {
        if (requests != null && !requests.isEmpty()) {
            return submitCaptureRequest(requests, callback, executor, true);
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
                }
            }
        }
    }

    private void waitUntilIdle() throws CameraAccessException {
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            if (this.mRepeatingRequestId == -1) {
                this.mRemoteDevice.waitUntilIdle();
            } else {
                throw new IllegalStateException("Active repeating request ongoing");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0034, code lost:
        return;
     */
    public void flush() throws CameraAccessException {
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            this.mDeviceExecutor.execute(this.mCallOnBusy);
            if (this.mIdle) {
                Log.i(this.TAG, "camera device is idle now!");
                this.mDeviceExecutor.execute(this.mCallOnIdle);
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
            boolean z = true;
            if (!this.mClosing.getAndSet(true)) {
                if (this.mRemoteDevice != null) {
                    Log.i(this.TAG, "close camera: " + this.mCameraId + ", package name: " + ActivityThread.currentOpPackageName());
                    HwSystemManager.notifyBackgroundMgr(ActivityThread.currentOpPackageName(), Binder.getCallingPid(), Binder.getCallingUid(), 0, 0);
                    IHwCameraUtil hwCameraUtil = HwFrameworkFactory.getHwCameraUtil();
                    if (hwCameraUtil != null) {
                        if (((Integer) this.mCharacteristics.get(CameraCharacteristics.LENS_FACING)).intValue() != 0) {
                            z = false;
                        }
                        hwCameraUtil.notifySurfaceFlingerCameraStatus(z, false);
                    } else {
                        Log.e(this.TAG, "HwFrameworkFactory.getHwCameraUtil is NULL");
                    }
                    this.mRemoteDevice.disconnect();
                    this.mRemoteDevice.unlinkToDeath(this, 0);
                }
                if (this.mRemoteDevice != null || this.mInError) {
                    this.mDeviceExecutor.execute(this.mCallOnClosed);
                }
                this.mRemoteDevice = null;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    private void checkInputConfiguration(InputConfiguration inputConfig) {
        if (inputConfig != null) {
            StreamConfigurationMap configMap = (StreamConfigurationMap) this.mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            boolean validFormat = false;
            for (int format : configMap.getInputFormats()) {
                if (format == inputConfig.getFormat()) {
                    validFormat = true;
                }
            }
            if (validFormat) {
                boolean validSize = false;
                for (Size s : configMap.getInputSizes(inputConfig.getFormat())) {
                    if (inputConfig.getWidth() == s.getWidth() && inputConfig.getHeight() == s.getHeight()) {
                        validSize = true;
                    }
                }
                if (!validSize) {
                    throw new IllegalArgumentException("input size " + inputConfig.getWidth() + "x" + inputConfig.getHeight() + " is not valid");
                }
                return;
            }
            throw new IllegalArgumentException("input format " + inputConfig.getFormat() + " is not valid");
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x006e, code lost:
        r2 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x006f, code lost:
        if (r2 == null) goto L_0x0073;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0071, code lost:
        if (r9 == false) goto L_0x0076;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0073, code lost:
        r7.remove();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0076, code lost:
        if (r9 == false) goto L_0x0092;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0078, code lost:
        r3 = new android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass10(r1);
        r11 = android.os.Binder.clearCallingIdentity();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
        r2.getExecutor().execute(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x008d, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x008e, code lost:
        android.os.Binder.restoreCallingIdentity(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0091, code lost:
        throw r0;
     */
    public void checkAndFireSequenceComplete() {
        long completedFrameNumber;
        long completedFrameNumber2 = this.mFrameNumberTracker.getCompletedFrameNumber();
        long completedReprocessFrameNumber = this.mFrameNumberTracker.getCompletedReprocessFrameNumber();
        Iterator<RequestLastFrameNumbersHolder> iter = this.mRequestLastFrameNumbersList.iterator();
        while (true) {
            Iterator<RequestLastFrameNumbersHolder> iter2 = iter;
            if (iter2.hasNext()) {
                final RequestLastFrameNumbersHolder requestLastFrameNumbers = iter2.next();
                boolean sequenceCompleted = false;
                final int requestId = requestLastFrameNumbers.getRequestId();
                synchronized (this.mInterfaceLock) {
                    try {
                        if (this.mRemoteDevice == null) {
                            try {
                                Log.w(this.TAG, "Camera closed while checking sequences");
                                return;
                            } catch (Throwable th) {
                                th = th;
                                long j = completedFrameNumber2;
                            }
                        } else {
                            int index = this.mCaptureCallbackMap.indexOfKey(requestId);
                            CaptureCallbackHolder holder = index >= 0 ? this.mCaptureCallbackMap.valueAt(index) : null;
                            if (holder != null) {
                                long lastRegularFrameNumber = requestLastFrameNumbers.getLastRegularFrameNumber();
                                long lastReprocessFrameNumber = requestLastFrameNumbers.getLastReprocessFrameNumber();
                                if (lastRegularFrameNumber <= completedFrameNumber2 && lastReprocessFrameNumber <= completedReprocessFrameNumber) {
                                    sequenceCompleted = true;
                                    completedFrameNumber = completedFrameNumber2;
                                    try {
                                        this.mCaptureCallbackMap.removeAt(index);
                                    } catch (Throwable th2) {
                                        th = th2;
                                        throw th;
                                    }
                                }
                            }
                            completedFrameNumber = completedFrameNumber2;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        long j2 = completedFrameNumber2;
                        throw th;
                    }
                }
            } else {
                return;
            }
            iter = iter2;
            completedFrameNumber2 = completedFrameNumber;
        }
    }

    static Executor checkExecutor(Executor executor) {
        return executor == null ? checkAndWrapHandler(null) : executor;
    }

    public static <T> Executor checkExecutor(Executor executor, T callback) {
        return callback != null ? checkExecutor(executor) : executor;
    }

    public static Executor checkAndWrapHandler(Handler handler) {
        return new CameraHandlerExecutor(checkHandler(handler));
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

    /* access modifiers changed from: private */
    public boolean isClosed() {
        return this.mClosing.get();
    }

    /* access modifiers changed from: private */
    public CameraCharacteristics getCharacteristics() {
        return this.mCharacteristics;
    }

    public void binderDied() {
        String str = this.TAG;
        Log.w(str, "CameraDevice " + this.mCameraId + " died unexpectedly");
        if (this.mRemoteDevice != null) {
            this.mInError = true;
            Runnable r = new Runnable() {
                public void run() {
                    if (!CameraDeviceImpl.this.isClosed()) {
                        CameraDeviceImpl.this.mDeviceCallback.onError(CameraDeviceImpl.this, 5);
                    }
                }
            };
            long ident = Binder.clearCallingIdentity();
            try {
                this.mDeviceExecutor.execute(r);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }
}
