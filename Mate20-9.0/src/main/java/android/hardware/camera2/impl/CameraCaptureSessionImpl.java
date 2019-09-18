package android.hardware.camera2.impl;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.impl.CallbackProxies;
import android.hardware.camera2.impl.CameraDeviceImpl;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.utils.TaskDrainer;
import android.hardware.camera2.utils.TaskSingleDrainer;
import android.os.Binder;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import com.android.internal.util.Preconditions;
import java.util.List;
import java.util.concurrent.Executor;

public class CameraCaptureSessionImpl extends CameraCaptureSession implements CameraCaptureSessionCore {
    private static final boolean DEBUG = false;
    private static final String TAG = "CameraCaptureSession";
    /* access modifiers changed from: private */
    public final TaskSingleDrainer mAbortDrainer;
    /* access modifiers changed from: private */
    public volatile boolean mAborting;
    private boolean mClosed = false;
    private final boolean mConfigureSuccess;
    private final Executor mDeviceExecutor;
    /* access modifiers changed from: private */
    public final CameraDeviceImpl mDeviceImpl;
    private final int mId;
    /* access modifiers changed from: private */
    public final String mIdString;
    /* access modifiers changed from: private */
    public final TaskSingleDrainer mIdleDrainer;
    private final Surface mInput;
    private final TaskDrainer<Integer> mSequenceDrainer;
    /* access modifiers changed from: private */
    public boolean mSkipUnconfigure = false;
    /* access modifiers changed from: private */
    public final CameraCaptureSession.StateCallback mStateCallback;
    private final Executor mStateExecutor;

    private class AbortDrainListener implements TaskDrainer.DrainListener {
        private AbortDrainListener() {
        }

        public void onDrained() {
            synchronized (CameraCaptureSessionImpl.this.mDeviceImpl.mInterfaceLock) {
                if (!CameraCaptureSessionImpl.this.mSkipUnconfigure) {
                    CameraCaptureSessionImpl.this.mIdleDrainer.beginDrain();
                }
            }
        }
    }

    private class IdleDrainListener implements TaskDrainer.DrainListener {
        private IdleDrainListener() {
        }

        public void onDrained() {
            synchronized (CameraCaptureSessionImpl.this.mDeviceImpl.mInterfaceLock) {
                if (!CameraCaptureSessionImpl.this.mSkipUnconfigure) {
                    try {
                        CameraCaptureSessionImpl.this.mDeviceImpl.configureStreamsChecked(null, null, 0, null);
                    } catch (CameraAccessException e) {
                        Log.e(CameraCaptureSessionImpl.TAG, CameraCaptureSessionImpl.this.mIdString + "Exception while unconfiguring outputs: ", e);
                    } catch (IllegalStateException e2) {
                    }
                }
            }
        }
    }

    private class SequenceDrainListener implements TaskDrainer.DrainListener {
        private SequenceDrainListener() {
        }

        public void onDrained() {
            CameraCaptureSessionImpl.this.mStateCallback.onClosed(CameraCaptureSessionImpl.this);
            if (!CameraCaptureSessionImpl.this.mSkipUnconfigure) {
                CameraCaptureSessionImpl.this.mAbortDrainer.beginDrain();
            }
        }
    }

    CameraCaptureSessionImpl(int id, Surface input, CameraCaptureSession.StateCallback callback, Executor stateExecutor, CameraDeviceImpl deviceImpl, Executor deviceStateExecutor, boolean configureSuccess) {
        if (callback != null) {
            this.mId = id;
            this.mIdString = String.format("Session %d: ", new Object[]{Integer.valueOf(this.mId)});
            this.mInput = input;
            this.mStateExecutor = (Executor) Preconditions.checkNotNull(stateExecutor, "stateExecutor must not be null");
            this.mStateCallback = createUserStateCallbackProxy(this.mStateExecutor, callback);
            this.mDeviceExecutor = (Executor) Preconditions.checkNotNull(deviceStateExecutor, "deviceStateExecutor must not be null");
            this.mDeviceImpl = (CameraDeviceImpl) Preconditions.checkNotNull(deviceImpl, "deviceImpl must not be null");
            this.mSequenceDrainer = new TaskDrainer<>(this.mDeviceExecutor, new SequenceDrainListener(), "seq");
            this.mIdleDrainer = new TaskSingleDrainer(this.mDeviceExecutor, new IdleDrainListener(), "idle");
            this.mAbortDrainer = new TaskSingleDrainer(this.mDeviceExecutor, new AbortDrainListener(), "abort");
            if (configureSuccess) {
                this.mStateCallback.onConfigured(this);
                this.mConfigureSuccess = true;
                return;
            }
            this.mStateCallback.onConfigureFailed(this);
            this.mClosed = true;
            Log.e(TAG, this.mIdString + "Failed to create capture session; configuration failed");
            this.mConfigureSuccess = false;
            return;
        }
        throw new IllegalArgumentException("callback must not be null");
    }

    public CameraDevice getDevice() {
        return this.mDeviceImpl;
    }

    public void prepare(Surface surface) throws CameraAccessException {
        this.mDeviceImpl.prepare(surface);
    }

    public void prepare(int maxCount, Surface surface) throws CameraAccessException {
        this.mDeviceImpl.prepare(maxCount, surface);
    }

    public void tearDown(Surface surface) throws CameraAccessException {
        this.mDeviceImpl.tearDown(surface);
    }

    public void finalizeOutputConfigurations(List<OutputConfiguration> outputConfigs) throws CameraAccessException {
        this.mDeviceImpl.finalizeOutputConfigs(outputConfigs);
    }

    public int capture(CaptureRequest request, CameraCaptureSession.CaptureCallback callback, Handler handler) throws CameraAccessException {
        int addPendingSequence;
        checkCaptureRequest(request);
        synchronized (this.mDeviceImpl.mInterfaceLock) {
            checkNotClosed();
            addPendingSequence = addPendingSequence(this.mDeviceImpl.capture(request, createCaptureCallbackProxy(CameraDeviceImpl.checkHandler(handler, callback), callback), this.mDeviceExecutor));
        }
        return addPendingSequence;
    }

    public int captureSingleRequest(CaptureRequest request, Executor executor, CameraCaptureSession.CaptureCallback callback) throws CameraAccessException {
        int addPendingSequence;
        if (executor == null) {
            throw new IllegalArgumentException("executor must not be null");
        } else if (callback != null) {
            checkCaptureRequest(request);
            synchronized (this.mDeviceImpl.mInterfaceLock) {
                checkNotClosed();
                addPendingSequence = addPendingSequence(this.mDeviceImpl.capture(request, createCaptureCallbackProxyWithExecutor(CameraDeviceImpl.checkExecutor(executor, callback), callback), this.mDeviceExecutor));
            }
            return addPendingSequence;
        } else {
            throw new IllegalArgumentException("callback must not be null");
        }
    }

    private void checkCaptureRequest(CaptureRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        } else if (request.isReprocess() && !isReprocessable()) {
            throw new IllegalArgumentException("this capture session cannot handle reprocess requests");
        } else if (request.isReprocess() && request.getReprocessableSessionId() != this.mId) {
            throw new IllegalArgumentException("capture request was created for another session");
        }
    }

    public int captureBurst(List<CaptureRequest> requests, CameraCaptureSession.CaptureCallback callback, Handler handler) throws CameraAccessException {
        int addPendingSequence;
        checkCaptureRequests(requests);
        synchronized (this.mDeviceImpl.mInterfaceLock) {
            checkNotClosed();
            addPendingSequence = addPendingSequence(this.mDeviceImpl.captureBurst(requests, createCaptureCallbackProxy(CameraDeviceImpl.checkHandler(handler, callback), callback), this.mDeviceExecutor));
        }
        return addPendingSequence;
    }

    public int captureBurstRequests(List<CaptureRequest> requests, Executor executor, CameraCaptureSession.CaptureCallback callback) throws CameraAccessException {
        int addPendingSequence;
        if (executor == null) {
            throw new IllegalArgumentException("executor must not be null");
        } else if (callback != null) {
            checkCaptureRequests(requests);
            synchronized (this.mDeviceImpl.mInterfaceLock) {
                checkNotClosed();
                addPendingSequence = addPendingSequence(this.mDeviceImpl.captureBurst(requests, createCaptureCallbackProxyWithExecutor(CameraDeviceImpl.checkExecutor(executor, callback), callback), this.mDeviceExecutor));
            }
            return addPendingSequence;
        } else {
            throw new IllegalArgumentException("callback must not be null");
        }
    }

    private void checkCaptureRequests(List<CaptureRequest> requests) {
        if (requests == null) {
            throw new IllegalArgumentException("Requests must not be null");
        } else if (!requests.isEmpty()) {
            for (CaptureRequest request : requests) {
                if (request.isReprocess()) {
                    if (!isReprocessable()) {
                        throw new IllegalArgumentException("This capture session cannot handle reprocess requests");
                    } else if (request.getReprocessableSessionId() != this.mId) {
                        throw new IllegalArgumentException("Capture request was created for another session");
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Requests must have at least one element");
        }
    }

    public int setRepeatingRequest(CaptureRequest request, CameraCaptureSession.CaptureCallback callback, Handler handler) throws CameraAccessException {
        int addPendingSequence;
        checkRepeatingRequest(request);
        synchronized (this.mDeviceImpl.mInterfaceLock) {
            checkNotClosed();
            addPendingSequence = addPendingSequence(this.mDeviceImpl.setRepeatingRequest(request, createCaptureCallbackProxy(CameraDeviceImpl.checkHandler(handler, callback), callback), this.mDeviceExecutor));
        }
        return addPendingSequence;
    }

    public int setSingleRepeatingRequest(CaptureRequest request, Executor executor, CameraCaptureSession.CaptureCallback callback) throws CameraAccessException {
        int addPendingSequence;
        if (executor == null) {
            throw new IllegalArgumentException("executor must not be null");
        } else if (callback != null) {
            checkRepeatingRequest(request);
            synchronized (this.mDeviceImpl.mInterfaceLock) {
                checkNotClosed();
                addPendingSequence = addPendingSequence(this.mDeviceImpl.setRepeatingRequest(request, createCaptureCallbackProxyWithExecutor(CameraDeviceImpl.checkExecutor(executor, callback), callback), this.mDeviceExecutor));
            }
            return addPendingSequence;
        } else {
            throw new IllegalArgumentException("callback must not be null");
        }
    }

    private void checkRepeatingRequest(CaptureRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        } else if (request.isReprocess()) {
            throw new IllegalArgumentException("repeating reprocess requests are not supported");
        }
    }

    public int setRepeatingBurst(List<CaptureRequest> requests, CameraCaptureSession.CaptureCallback callback, Handler handler) throws CameraAccessException {
        int addPendingSequence;
        checkRepeatingRequests(requests);
        synchronized (this.mDeviceImpl.mInterfaceLock) {
            checkNotClosed();
            addPendingSequence = addPendingSequence(this.mDeviceImpl.setRepeatingBurst(requests, createCaptureCallbackProxy(CameraDeviceImpl.checkHandler(handler, callback), callback), this.mDeviceExecutor));
        }
        return addPendingSequence;
    }

    public int setRepeatingBurstRequests(List<CaptureRequest> requests, Executor executor, CameraCaptureSession.CaptureCallback callback) throws CameraAccessException {
        int addPendingSequence;
        if (executor == null) {
            throw new IllegalArgumentException("executor must not be null");
        } else if (callback != null) {
            checkRepeatingRequests(requests);
            synchronized (this.mDeviceImpl.mInterfaceLock) {
                checkNotClosed();
                addPendingSequence = addPendingSequence(this.mDeviceImpl.setRepeatingBurst(requests, createCaptureCallbackProxyWithExecutor(CameraDeviceImpl.checkExecutor(executor, callback), callback), this.mDeviceExecutor));
            }
            return addPendingSequence;
        } else {
            throw new IllegalArgumentException("callback must not be null");
        }
    }

    private void checkRepeatingRequests(List<CaptureRequest> requests) {
        if (requests == null) {
            throw new IllegalArgumentException("requests must not be null");
        } else if (!requests.isEmpty()) {
            for (CaptureRequest r : requests) {
                if (r.isReprocess()) {
                    throw new IllegalArgumentException("repeating reprocess burst requests are not supported");
                }
            }
        } else {
            throw new IllegalArgumentException("requests must have at least one element");
        }
    }

    public void stopRepeating() throws CameraAccessException {
        synchronized (this.mDeviceImpl.mInterfaceLock) {
            checkNotClosed();
            this.mDeviceImpl.stopRepeating();
        }
    }

    public void abortCaptures() throws CameraAccessException {
        synchronized (this.mDeviceImpl.mInterfaceLock) {
            Log.i(TAG, this.mIdString + "abortCaptures");
            checkNotClosed();
            if (this.mAborting) {
                Log.w(TAG, this.mIdString + "abortCaptures - Session is already aborting; doing nothing");
                return;
            }
            this.mAborting = true;
            this.mAbortDrainer.taskStarted();
            this.mDeviceImpl.flush();
        }
    }

    public void updateOutputConfiguration(OutputConfiguration config) throws CameraAccessException {
        synchronized (this.mDeviceImpl.mInterfaceLock) {
            checkNotClosed();
            this.mDeviceImpl.updateOutputConfiguration(config);
        }
    }

    public boolean isReprocessable() {
        return this.mInput != null;
    }

    public Surface getInputSurface() {
        return this.mInput;
    }

    public void replaceSessionClose() {
        synchronized (this.mDeviceImpl.mInterfaceLock) {
            this.mSkipUnconfigure = true;
            close();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0035, code lost:
        if (r5.mInput == null) goto L_0x003c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0037, code lost:
        r5.mInput.release();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003c, code lost:
        return;
     */
    public void close() {
        synchronized (this.mDeviceImpl.mInterfaceLock) {
            if (!this.mClosed) {
                this.mClosed = true;
                try {
                    this.mDeviceImpl.stopRepeating();
                } catch (IllegalStateException e) {
                    this.mStateCallback.onClosed(this);
                    return;
                } catch (CameraAccessException e2) {
                    Log.e(TAG, this.mIdString + "Exception while stopping repeating: ", e2);
                }
                this.mSequenceDrainer.beginDrain();
            }
        }
    }

    public boolean isAborting() {
        return this.mAborting;
    }

    private CameraCaptureSession.StateCallback createUserStateCallbackProxy(Executor executor, CameraCaptureSession.StateCallback callback) {
        return new CallbackProxies.SessionStateCallbackProxy(executor, callback);
    }

    private CameraDeviceImpl.CaptureCallback createCaptureCallbackProxy(Handler handler, CameraCaptureSession.CaptureCallback callback) {
        Executor executor;
        if (callback != null) {
            executor = CameraDeviceImpl.checkAndWrapHandler(handler);
        } else {
            executor = null;
        }
        return createCaptureCallbackProxyWithExecutor(executor, callback);
    }

    private CameraDeviceImpl.CaptureCallback createCaptureCallbackProxyWithExecutor(final Executor executor, final CameraCaptureSession.CaptureCallback callback) {
        return new CameraDeviceImpl.CaptureCallback() {
            public void onCaptureStarted(CameraDevice camera, CaptureRequest request, long timestamp, long frameNumber) {
                if (callback != null && executor != null) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        Executor executor = executor;
                        $$Lambda$CameraCaptureSessionImpl$1$uPVvNnGFdZcxxscdYQ5erNgaRWA r1 = new Runnable(callback, request, timestamp, frameNumber) {
                            private final /* synthetic */ CameraCaptureSession.CaptureCallback f$1;
                            private final /* synthetic */ CaptureRequest f$2;
                            private final /* synthetic */ long f$3;
                            private final /* synthetic */ long f$4;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r4;
                                this.f$4 = r6;
                            }

                            public final void run() {
                                this.f$1.onCaptureStarted(CameraCaptureSessionImpl.this, this.f$2, this.f$3, this.f$4);
                            }
                        };
                        executor.execute(r1);
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
            }

            public void onCapturePartial(CameraDevice camera, CaptureRequest request, CaptureResult result) {
                if (callback != null && executor != null) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        executor.execute(new Runnable(callback, request, result) {
                            private final /* synthetic */ CameraCaptureSession.CaptureCallback f$1;
                            private final /* synthetic */ CaptureRequest f$2;
                            private final /* synthetic */ CaptureResult f$3;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r4;
                            }

                            public final void run() {
                                this.f$1.onCapturePartial(CameraCaptureSessionImpl.this, this.f$2, this.f$3);
                            }
                        });
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
            }

            public void onCaptureProgressed(CameraDevice camera, CaptureRequest request, CaptureResult partialResult) {
                if (callback != null && executor != null) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        executor.execute(new Runnable(callback, request, partialResult) {
                            private final /* synthetic */ CameraCaptureSession.CaptureCallback f$1;
                            private final /* synthetic */ CaptureRequest f$2;
                            private final /* synthetic */ CaptureResult f$3;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r4;
                            }

                            public final void run() {
                                this.f$1.onCaptureProgressed(CameraCaptureSessionImpl.this, this.f$2, this.f$3);
                            }
                        });
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
            }

            public void onCaptureCompleted(CameraDevice camera, CaptureRequest request, TotalCaptureResult result) {
                if (callback != null && executor != null) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        executor.execute(new Runnable(callback, request, result) {
                            private final /* synthetic */ CameraCaptureSession.CaptureCallback f$1;
                            private final /* synthetic */ CaptureRequest f$2;
                            private final /* synthetic */ TotalCaptureResult f$3;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r4;
                            }

                            public final void run() {
                                this.f$1.onCaptureCompleted(CameraCaptureSessionImpl.this, this.f$2, this.f$3);
                            }
                        });
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
            }

            public void onCaptureFailed(CameraDevice camera, CaptureRequest request, CaptureFailure failure) {
                if (callback != null && executor != null) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        executor.execute(new Runnable(callback, request, failure) {
                            private final /* synthetic */ CameraCaptureSession.CaptureCallback f$1;
                            private final /* synthetic */ CaptureRequest f$2;
                            private final /* synthetic */ CaptureFailure f$3;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r4;
                            }

                            public final void run() {
                                this.f$1.onCaptureFailed(CameraCaptureSessionImpl.this, this.f$2, this.f$3);
                            }
                        });
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
            }

            public void onCaptureSequenceCompleted(CameraDevice camera, int sequenceId, long frameNumber) {
                if (!(callback == null || executor == null)) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        Executor executor = executor;
                        $$Lambda$CameraCaptureSessionImpl$1$KZ4tthx5TnA5BizPVljsPqqdHck r3 = new Runnable(callback, sequenceId, frameNumber) {
                            private final /* synthetic */ CameraCaptureSession.CaptureCallback f$1;
                            private final /* synthetic */ int f$2;
                            private final /* synthetic */ long f$3;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r4;
                            }

                            public final void run() {
                                this.f$1.onCaptureSequenceCompleted(CameraCaptureSessionImpl.this, this.f$2, this.f$3);
                            }
                        };
                        executor.execute(r3);
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
                CameraCaptureSessionImpl.this.finishPendingSequence(sequenceId);
            }

            public void onCaptureSequenceAborted(CameraDevice camera, int sequenceId) {
                if (!(callback == null || executor == null)) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        executor.execute(new Runnable(callback, sequenceId) {
                            private final /* synthetic */ CameraCaptureSession.CaptureCallback f$1;
                            private final /* synthetic */ int f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            public final void run() {
                                this.f$1.onCaptureSequenceAborted(CameraCaptureSessionImpl.this, this.f$2);
                            }
                        });
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
                CameraCaptureSessionImpl.this.finishPendingSequence(sequenceId);
            }

            public void onCaptureBufferLost(CameraDevice camera, CaptureRequest request, Surface target, long frameNumber) {
                if (callback != null && executor != null) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        Executor executor = executor;
                        $$Lambda$CameraCaptureSessionImpl$1$VuYVXvwmJMkbTnKaODhDOjJpE r1 = new Runnable(callback, request, target, frameNumber) {
                            private final /* synthetic */ CameraCaptureSession.CaptureCallback f$1;
                            private final /* synthetic */ CaptureRequest f$2;
                            private final /* synthetic */ Surface f$3;
                            private final /* synthetic */ long f$4;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r4;
                                this.f$4 = r5;
                            }

                            public final void run() {
                                this.f$1.onCaptureBufferLost(CameraCaptureSessionImpl.this, this.f$2, this.f$3, this.f$4);
                            }
                        };
                        executor.execute(r1);
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
            }
        };
    }

    public CameraDeviceImpl.StateCallbackKK getDeviceStateCallback() {
        final Object interfaceLock = this.mDeviceImpl.mInterfaceLock;
        return new CameraDeviceImpl.StateCallbackKK() {
            private boolean mActive = false;
            private boolean mBusy = false;

            public void onOpened(CameraDevice camera) {
                throw new AssertionError("Camera must already be open before creating a session");
            }

            public void onDisconnected(CameraDevice camera) {
                CameraCaptureSessionImpl.this.close();
            }

            public void onError(CameraDevice camera, int error) {
                Log.wtf(CameraCaptureSessionImpl.TAG, CameraCaptureSessionImpl.this.mIdString + "Got device error " + error);
            }

            public void onActive(CameraDevice camera) {
                CameraCaptureSessionImpl.this.mIdleDrainer.taskStarted();
                this.mActive = true;
                CameraCaptureSessionImpl.this.mStateCallback.onActive(this);
            }

            public void onIdle(CameraDevice camera) {
                boolean isAborting;
                synchronized (interfaceLock) {
                    isAborting = CameraCaptureSessionImpl.this.mAborting;
                }
                if (this.mBusy && isAborting) {
                    CameraCaptureSessionImpl.this.mAbortDrainer.taskFinished();
                    synchronized (interfaceLock) {
                        boolean unused = CameraCaptureSessionImpl.this.mAborting = false;
                    }
                }
                if (this.mActive) {
                    CameraCaptureSessionImpl.this.mIdleDrainer.taskFinished();
                }
                this.mBusy = false;
                this.mActive = false;
                CameraCaptureSessionImpl.this.mStateCallback.onReady(this);
            }

            public void onBusy(CameraDevice camera) {
                this.mBusy = true;
            }

            public void onUnconfigured(CameraDevice camera) {
            }

            public void onRequestQueueEmpty() {
                CameraCaptureSessionImpl.this.mStateCallback.onCaptureQueueEmpty(this);
            }

            public void onSurfacePrepared(Surface surface) {
                CameraCaptureSessionImpl.this.mStateCallback.onSurfacePrepared(this, surface);
            }
        };
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    private void checkNotClosed() {
        if (this.mClosed) {
            throw new IllegalStateException("Session has been closed; further changes are illegal.");
        }
    }

    private int addPendingSequence(int sequenceId) {
        this.mSequenceDrainer.taskStarted(Integer.valueOf(sequenceId));
        return sequenceId;
    }

    /* access modifiers changed from: private */
    public void finishPendingSequence(int sequenceId) {
        try {
            this.mSequenceDrainer.taskFinished(Integer.valueOf(sequenceId));
        } catch (IllegalStateException e) {
            Log.w(TAG, e.getMessage());
        }
    }
}
