package android.hardware.camera2.impl;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.impl.CallbackProxies;
import android.hardware.camera2.impl.CameraCaptureSessionImpl;
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
    private final TaskSingleDrainer mAbortDrainer;
    private volatile boolean mAborting;
    private boolean mClosed = false;
    private final boolean mConfigureSuccess;
    private final Executor mDeviceExecutor;
    private final CameraDeviceImpl mDeviceImpl;
    private final int mId;
    private final String mIdString;
    private final TaskSingleDrainer mIdleDrainer;
    private final Surface mInput;
    private final TaskDrainer<Integer> mSequenceDrainer;
    private boolean mSkipUnconfigure = false;
    private final CameraCaptureSession.StateCallback mStateCallback;
    private final Executor mStateExecutor;

    CameraCaptureSessionImpl(int id, Surface input, CameraCaptureSession.StateCallback callback, Executor stateExecutor, CameraDeviceImpl deviceImpl, Executor deviceStateExecutor, boolean configureSuccess) {
        if (callback != null) {
            this.mId = id;
            this.mIdString = String.format("Session %d: ", Integer.valueOf(this.mId));
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

    @Override // android.hardware.camera2.CameraCaptureSession
    public CameraDevice getDevice() {
        return this.mDeviceImpl;
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public void prepare(Surface surface) throws CameraAccessException {
        this.mDeviceImpl.prepare(surface);
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public void prepare(int maxCount, Surface surface) throws CameraAccessException {
        this.mDeviceImpl.prepare(maxCount, surface);
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public void tearDown(Surface surface) throws CameraAccessException {
        this.mDeviceImpl.tearDown(surface);
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public void finalizeOutputConfigurations(List<OutputConfiguration> outputConfigs) throws CameraAccessException {
        this.mDeviceImpl.finalizeOutputConfigs(outputConfigs);
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public int capture(CaptureRequest request, CameraCaptureSession.CaptureCallback callback, Handler handler) throws CameraAccessException {
        int addPendingSequence;
        checkCaptureRequest(request);
        synchronized (this.mDeviceImpl.mInterfaceLock) {
            checkNotClosed();
            addPendingSequence = addPendingSequence(this.mDeviceImpl.capture(request, createCaptureCallbackProxy(CameraDeviceImpl.checkHandler(handler, callback), callback), this.mDeviceExecutor));
        }
        return addPendingSequence;
    }

    @Override // android.hardware.camera2.CameraCaptureSession
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

    @Override // android.hardware.camera2.CameraCaptureSession
    public int captureBurst(List<CaptureRequest> requests, CameraCaptureSession.CaptureCallback callback, Handler handler) throws CameraAccessException {
        int addPendingSequence;
        checkCaptureRequests(requests);
        synchronized (this.mDeviceImpl.mInterfaceLock) {
            checkNotClosed();
            addPendingSequence = addPendingSequence(this.mDeviceImpl.captureBurst(requests, createCaptureCallbackProxy(CameraDeviceImpl.checkHandler(handler, callback), callback), this.mDeviceExecutor));
        }
        return addPendingSequence;
    }

    @Override // android.hardware.camera2.CameraCaptureSession
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

    @Override // android.hardware.camera2.CameraCaptureSession
    public int setRepeatingRequest(CaptureRequest request, CameraCaptureSession.CaptureCallback callback, Handler handler) throws CameraAccessException {
        int addPendingSequence;
        checkRepeatingRequest(request);
        synchronized (this.mDeviceImpl.mInterfaceLock) {
            checkNotClosed();
            addPendingSequence = addPendingSequence(this.mDeviceImpl.setRepeatingRequest(request, createCaptureCallbackProxy(CameraDeviceImpl.checkHandler(handler, callback), callback), this.mDeviceExecutor));
        }
        return addPendingSequence;
    }

    @Override // android.hardware.camera2.CameraCaptureSession
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

    @Override // android.hardware.camera2.CameraCaptureSession
    public int setRepeatingBurst(List<CaptureRequest> requests, CameraCaptureSession.CaptureCallback callback, Handler handler) throws CameraAccessException {
        int addPendingSequence;
        checkRepeatingRequests(requests);
        synchronized (this.mDeviceImpl.mInterfaceLock) {
            checkNotClosed();
            addPendingSequence = addPendingSequence(this.mDeviceImpl.setRepeatingBurst(requests, createCaptureCallbackProxy(CameraDeviceImpl.checkHandler(handler, callback), callback), this.mDeviceExecutor));
        }
        return addPendingSequence;
    }

    @Override // android.hardware.camera2.CameraCaptureSession
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

    @Override // android.hardware.camera2.CameraCaptureSession
    public void stopRepeating() throws CameraAccessException {
        synchronized (this.mDeviceImpl.mInterfaceLock) {
            checkNotClosed();
            this.mDeviceImpl.stopRepeating();
        }
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public void abortCaptures() throws CameraAccessException {
        synchronized (this.mDeviceImpl.mInterfaceLock) {
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

    @Override // android.hardware.camera2.CameraCaptureSession
    public void updateOutputConfiguration(OutputConfiguration config) throws CameraAccessException {
        synchronized (this.mDeviceImpl.mInterfaceLock) {
            checkNotClosed();
            this.mDeviceImpl.updateOutputConfiguration(config);
        }
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public boolean isReprocessable() {
        return this.mInput != null;
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public Surface getInputSurface() {
        return this.mInput;
    }

    @Override // android.hardware.camera2.impl.CameraCaptureSessionCore
    public void replaceSessionClose() {
        synchronized (this.mDeviceImpl.mInterfaceLock) {
            this.mSkipUnconfigure = true;
            close();
        }
    }

    @Override // android.hardware.camera2.CameraCaptureSession, java.lang.AutoCloseable
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
            } else {
                return;
            }
        }
        Surface surface = this.mInput;
        if (surface != null) {
            surface.release();
        }
    }

    @Override // android.hardware.camera2.impl.CameraCaptureSessionCore
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
            /* class android.hardware.camera2.impl.CameraCaptureSessionImpl.AnonymousClass1 */

            @Override // android.hardware.camera2.impl.CameraDeviceImpl.CaptureCallback
            public void onCaptureStarted(CameraDevice camera, CaptureRequest request, long timestamp, long frameNumber) {
                if (callback != null && executor != null) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        executor.execute(new Runnable(callback, request, timestamp, frameNumber) {
                            /* class android.hardware.camera2.impl.$$Lambda$CameraCaptureSessionImpl$1$uPVvNnGFdZcxxscdYQ5erNgaRWA */
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

                            @Override // java.lang.Runnable
                            public final void run() {
                                CameraCaptureSessionImpl.AnonymousClass1.this.lambda$onCaptureStarted$0$CameraCaptureSessionImpl$1(this.f$1, this.f$2, this.f$3, this.f$4);
                            }
                        });
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
            }

            public /* synthetic */ void lambda$onCaptureStarted$0$CameraCaptureSessionImpl$1(CameraCaptureSession.CaptureCallback callback, CaptureRequest request, long timestamp, long frameNumber) {
                callback.onCaptureStarted(CameraCaptureSessionImpl.this, request, timestamp, frameNumber);
            }

            @Override // android.hardware.camera2.impl.CameraDeviceImpl.CaptureCallback
            public void onCapturePartial(CameraDevice camera, CaptureRequest request, CaptureResult result) {
                if (callback != null && executor != null) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        executor.execute(new Runnable(callback, request, result) {
                            /* class android.hardware.camera2.impl.$$Lambda$CameraCaptureSessionImpl$1$HRzGZkXU2X5JDcudK0jcqdLZzV8 */
                            private final /* synthetic */ CameraCaptureSession.CaptureCallback f$1;
                            private final /* synthetic */ CaptureRequest f$2;
                            private final /* synthetic */ CaptureResult f$3;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r4;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                CameraCaptureSessionImpl.AnonymousClass1.this.lambda$onCapturePartial$1$CameraCaptureSessionImpl$1(this.f$1, this.f$2, this.f$3);
                            }
                        });
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
            }

            public /* synthetic */ void lambda$onCapturePartial$1$CameraCaptureSessionImpl$1(CameraCaptureSession.CaptureCallback callback, CaptureRequest request, CaptureResult result) {
                callback.onCapturePartial(CameraCaptureSessionImpl.this, request, result);
            }

            @Override // android.hardware.camera2.impl.CameraDeviceImpl.CaptureCallback
            public void onCaptureProgressed(CameraDevice camera, CaptureRequest request, CaptureResult partialResult) {
                if (callback != null && executor != null) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        executor.execute(new Runnable(callback, request, partialResult) {
                            /* class android.hardware.camera2.impl.$$Lambda$CameraCaptureSessionImpl$1$7mSdNTTAoYA0D3ITDxzDJKGykz0 */
                            private final /* synthetic */ CameraCaptureSession.CaptureCallback f$1;
                            private final /* synthetic */ CaptureRequest f$2;
                            private final /* synthetic */ CaptureResult f$3;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r4;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                CameraCaptureSessionImpl.AnonymousClass1.this.lambda$onCaptureProgressed$2$CameraCaptureSessionImpl$1(this.f$1, this.f$2, this.f$3);
                            }
                        });
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
            }

            public /* synthetic */ void lambda$onCaptureProgressed$2$CameraCaptureSessionImpl$1(CameraCaptureSession.CaptureCallback callback, CaptureRequest request, CaptureResult partialResult) {
                callback.onCaptureProgressed(CameraCaptureSessionImpl.this, request, partialResult);
            }

            @Override // android.hardware.camera2.impl.CameraDeviceImpl.CaptureCallback
            public void onCaptureCompleted(CameraDevice camera, CaptureRequest request, TotalCaptureResult result) {
                if (callback != null && executor != null) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        executor.execute(new Runnable(callback, request, result) {
                            /* class android.hardware.camera2.impl.$$Lambda$CameraCaptureSessionImpl$1$OA1Yz_YgzMO8qcV8esRjyt7ykp4 */
                            private final /* synthetic */ CameraCaptureSession.CaptureCallback f$1;
                            private final /* synthetic */ CaptureRequest f$2;
                            private final /* synthetic */ TotalCaptureResult f$3;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r4;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                CameraCaptureSessionImpl.AnonymousClass1.this.lambda$onCaptureCompleted$3$CameraCaptureSessionImpl$1(this.f$1, this.f$2, this.f$3);
                            }
                        });
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
            }

            public /* synthetic */ void lambda$onCaptureCompleted$3$CameraCaptureSessionImpl$1(CameraCaptureSession.CaptureCallback callback, CaptureRequest request, TotalCaptureResult result) {
                callback.onCaptureCompleted(CameraCaptureSessionImpl.this, request, result);
            }

            @Override // android.hardware.camera2.impl.CameraDeviceImpl.CaptureCallback
            public void onCaptureFailed(CameraDevice camera, CaptureRequest request, CaptureFailure failure) {
                if (callback != null && executor != null) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        executor.execute(new Runnable(callback, request, failure) {
                            /* class android.hardware.camera2.impl.$$Lambda$CameraCaptureSessionImpl$1$VsKq1alEqL3XHhLTWXgi7fSF3s */
                            private final /* synthetic */ CameraCaptureSession.CaptureCallback f$1;
                            private final /* synthetic */ CaptureRequest f$2;
                            private final /* synthetic */ CaptureFailure f$3;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r4;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                CameraCaptureSessionImpl.AnonymousClass1.this.lambda$onCaptureFailed$4$CameraCaptureSessionImpl$1(this.f$1, this.f$2, this.f$3);
                            }
                        });
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
            }

            public /* synthetic */ void lambda$onCaptureFailed$4$CameraCaptureSessionImpl$1(CameraCaptureSession.CaptureCallback callback, CaptureRequest request, CaptureFailure failure) {
                callback.onCaptureFailed(CameraCaptureSessionImpl.this, request, failure);
            }

            @Override // android.hardware.camera2.impl.CameraDeviceImpl.CaptureCallback
            public void onCaptureSequenceCompleted(CameraDevice camera, int sequenceId, long frameNumber) {
                if (!(callback == null || executor == null)) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        executor.execute(new Runnable(callback, sequenceId, frameNumber) {
                            /* class android.hardware.camera2.impl.$$Lambda$CameraCaptureSessionImpl$1$KZ4tthx5TnA5BizPVljsPqqdHck */
                            private final /* synthetic */ CameraCaptureSession.CaptureCallback f$1;
                            private final /* synthetic */ int f$2;
                            private final /* synthetic */ long f$3;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r4;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                CameraCaptureSessionImpl.AnonymousClass1.this.lambda$onCaptureSequenceCompleted$5$CameraCaptureSessionImpl$1(this.f$1, this.f$2, this.f$3);
                            }
                        });
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
                CameraCaptureSessionImpl.this.finishPendingSequence(sequenceId);
            }

            public /* synthetic */ void lambda$onCaptureSequenceCompleted$5$CameraCaptureSessionImpl$1(CameraCaptureSession.CaptureCallback callback, int sequenceId, long frameNumber) {
                callback.onCaptureSequenceCompleted(CameraCaptureSessionImpl.this, sequenceId, frameNumber);
            }

            @Override // android.hardware.camera2.impl.CameraDeviceImpl.CaptureCallback
            public void onCaptureSequenceAborted(CameraDevice camera, int sequenceId) {
                if (!(callback == null || executor == null)) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        executor.execute(new Runnable(callback, sequenceId) {
                            /* class android.hardware.camera2.impl.$$Lambda$CameraCaptureSessionImpl$1$TIJELOXvjSbPh6mpBLfBJ5ciNic */
                            private final /* synthetic */ CameraCaptureSession.CaptureCallback f$1;
                            private final /* synthetic */ int f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                CameraCaptureSessionImpl.AnonymousClass1.this.lambda$onCaptureSequenceAborted$6$CameraCaptureSessionImpl$1(this.f$1, this.f$2);
                            }
                        });
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
                CameraCaptureSessionImpl.this.finishPendingSequence(sequenceId);
            }

            public /* synthetic */ void lambda$onCaptureSequenceAborted$6$CameraCaptureSessionImpl$1(CameraCaptureSession.CaptureCallback callback, int sequenceId) {
                callback.onCaptureSequenceAborted(CameraCaptureSessionImpl.this, sequenceId);
            }

            @Override // android.hardware.camera2.impl.CameraDeviceImpl.CaptureCallback
            public void onCaptureBufferLost(CameraDevice camera, CaptureRequest request, Surface target, long frameNumber) {
                if (callback != null && executor != null) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        executor.execute(new Runnable(callback, request, target, frameNumber) {
                            /* class android.hardware.camera2.impl.$$Lambda$CameraCaptureSessionImpl$1$VuYVXvwmJMkbTnKaODhDOjJpE */
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

                            @Override // java.lang.Runnable
                            public final void run() {
                                CameraCaptureSessionImpl.AnonymousClass1.this.lambda$onCaptureBufferLost$7$CameraCaptureSessionImpl$1(this.f$1, this.f$2, this.f$3, this.f$4);
                            }
                        });
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
            }

            public /* synthetic */ void lambda$onCaptureBufferLost$7$CameraCaptureSessionImpl$1(CameraCaptureSession.CaptureCallback callback, CaptureRequest request, Surface target, long frameNumber) {
                callback.onCaptureBufferLost(CameraCaptureSessionImpl.this, request, target, frameNumber);
            }
        };
    }

    @Override // android.hardware.camera2.impl.CameraCaptureSessionCore
    public CameraDeviceImpl.StateCallbackKK getDeviceStateCallback() {
        final Object interfaceLock = this.mDeviceImpl.mInterfaceLock;
        return new CameraDeviceImpl.StateCallbackKK() {
            /* class android.hardware.camera2.impl.CameraCaptureSessionImpl.AnonymousClass2 */
            private boolean mActive = false;
            private boolean mBusy = false;

            @Override // android.hardware.camera2.CameraDevice.StateCallback
            public void onOpened(CameraDevice camera) {
                throw new AssertionError("Camera must already be open before creating a session");
            }

            @Override // android.hardware.camera2.CameraDevice.StateCallback
            public void onDisconnected(CameraDevice camera) {
                CameraCaptureSessionImpl.this.close();
            }

            @Override // android.hardware.camera2.CameraDevice.StateCallback
            public void onError(CameraDevice camera, int error) {
                Log.wtf(CameraCaptureSessionImpl.TAG, CameraCaptureSessionImpl.this.mIdString + "Got device error " + error);
            }

            @Override // android.hardware.camera2.impl.CameraDeviceImpl.StateCallbackKK
            public void onActive(CameraDevice camera) {
                CameraCaptureSessionImpl.this.mIdleDrainer.taskStarted();
                this.mActive = true;
                CameraCaptureSessionImpl.this.mStateCallback.onActive(this);
            }

            @Override // android.hardware.camera2.impl.CameraDeviceImpl.StateCallbackKK
            public void onIdle(CameraDevice camera) {
                boolean isAborting;
                synchronized (interfaceLock) {
                    isAborting = CameraCaptureSessionImpl.this.mAborting;
                }
                if (this.mBusy && isAborting) {
                    CameraCaptureSessionImpl.this.mAbortDrainer.taskFinished();
                    synchronized (interfaceLock) {
                        CameraCaptureSessionImpl.this.mAborting = false;
                    }
                }
                if (this.mActive) {
                    CameraCaptureSessionImpl.this.mIdleDrainer.taskFinished();
                }
                this.mBusy = false;
                this.mActive = false;
                CameraCaptureSessionImpl.this.mStateCallback.onReady(this);
            }

            @Override // android.hardware.camera2.impl.CameraDeviceImpl.StateCallbackKK
            public void onBusy(CameraDevice camera) {
                this.mBusy = true;
            }

            @Override // android.hardware.camera2.impl.CameraDeviceImpl.StateCallbackKK
            public void onUnconfigured(CameraDevice camera) {
            }

            @Override // android.hardware.camera2.impl.CameraDeviceImpl.StateCallbackKK
            public void onRequestQueueEmpty() {
                CameraCaptureSessionImpl.this.mStateCallback.onCaptureQueueEmpty(this);
            }

            @Override // android.hardware.camera2.impl.CameraDeviceImpl.StateCallbackKK
            public void onSurfacePrepared(Surface surface) {
                CameraCaptureSessionImpl.this.mStateCallback.onSurfacePrepared(this, surface);
            }
        };
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
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
    /* access modifiers changed from: public */
    private void finishPendingSequence(int sequenceId) {
        try {
            this.mSequenceDrainer.taskFinished(Integer.valueOf(sequenceId));
        } catch (IllegalStateException e) {
            Log.w(TAG, e.getMessage());
        }
    }

    private class SequenceDrainListener implements TaskDrainer.DrainListener {
        private SequenceDrainListener() {
        }

        @Override // android.hardware.camera2.utils.TaskDrainer.DrainListener
        public void onDrained() {
            CameraCaptureSessionImpl.this.mStateCallback.onClosed(CameraCaptureSessionImpl.this);
            if (!CameraCaptureSessionImpl.this.mSkipUnconfigure) {
                CameraCaptureSessionImpl.this.mAbortDrainer.beginDrain();
            }
        }
    }

    private class AbortDrainListener implements TaskDrainer.DrainListener {
        private AbortDrainListener() {
        }

        @Override // android.hardware.camera2.utils.TaskDrainer.DrainListener
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

        @Override // android.hardware.camera2.utils.TaskDrainer.DrainListener
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
}
