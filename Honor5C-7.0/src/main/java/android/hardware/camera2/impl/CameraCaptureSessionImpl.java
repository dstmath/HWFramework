package android.hardware.camera2.impl;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCaptureSession.StateCallback;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.dispatch.ArgumentReplacingDispatcher;
import android.hardware.camera2.dispatch.BroadcastDispatcher;
import android.hardware.camera2.dispatch.DuckTypingDispatcher;
import android.hardware.camera2.dispatch.HandlerDispatcher;
import android.hardware.camera2.dispatch.InvokeDispatcher;
import android.hardware.camera2.impl.CallbackProxies.DeviceCaptureCallbackProxy;
import android.hardware.camera2.impl.CallbackProxies.SessionStateCallbackProxy;
import android.hardware.camera2.impl.CameraDeviceImpl.StateCallbackKK;
import android.hardware.camera2.utils.TaskDrainer;
import android.hardware.camera2.utils.TaskDrainer.DrainListener;
import android.hardware.camera2.utils.TaskSingleDrainer;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import com.android.internal.util.Preconditions;
import java.util.List;

public class CameraCaptureSessionImpl extends CameraCaptureSession implements CameraCaptureSessionCore {
    private static final boolean DEBUG = false;
    private static final String TAG = "CameraCaptureSession";
    private final TaskSingleDrainer mAbortDrainer;
    private volatile boolean mAborting;
    private boolean mClosed;
    private final boolean mConfigureSuccess;
    private final Handler mDeviceHandler;
    private final CameraDeviceImpl mDeviceImpl;
    private final int mId;
    private final String mIdString;
    private final TaskSingleDrainer mIdleDrainer;
    private final Surface mInput;
    private final List<Surface> mOutputs;
    private final TaskDrainer<Integer> mSequenceDrainer;
    private boolean mSkipUnconfigure;
    private final StateCallback mStateCallback;
    private final Handler mStateHandler;

    /* renamed from: android.hardware.camera2.impl.CameraCaptureSessionImpl.2 */
    class AnonymousClass2 extends StateCallbackKK {
        private boolean mActive;
        private boolean mBusy;
        final /* synthetic */ CameraCaptureSession val$session;

        AnonymousClass2(CameraCaptureSession val$session) {
            this.val$session = val$session;
            this.mBusy = CameraCaptureSessionImpl.DEBUG;
            this.mActive = CameraCaptureSessionImpl.DEBUG;
        }

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
            CameraCaptureSessionImpl.this.mStateCallback.onActive(this.val$session);
        }

        public void onIdle(CameraDevice camera) {
            synchronized (this.val$session) {
                boolean isAborting = CameraCaptureSessionImpl.this.mAborting;
            }
            if (this.mBusy && isAborting) {
                CameraCaptureSessionImpl.this.mAbortDrainer.taskFinished();
                synchronized (this.val$session) {
                    CameraCaptureSessionImpl.this.mAborting = CameraCaptureSessionImpl.DEBUG;
                }
            }
            if (this.mActive) {
                CameraCaptureSessionImpl.this.mIdleDrainer.taskFinished();
            }
            this.mBusy = CameraCaptureSessionImpl.DEBUG;
            this.mActive = CameraCaptureSessionImpl.DEBUG;
            CameraCaptureSessionImpl.this.mStateCallback.onReady(this.val$session);
        }

        public void onBusy(CameraDevice camera) {
            this.mBusy = true;
        }

        public void onUnconfigured(CameraDevice camera) {
        }

        public void onSurfacePrepared(Surface surface) {
            CameraCaptureSessionImpl.this.mStateCallback.onSurfacePrepared(this.val$session, surface);
        }
    }

    private class AbortDrainListener implements DrainListener {
        private AbortDrainListener() {
        }

        public void onDrained() {
            synchronized (CameraCaptureSessionImpl.this) {
                if (CameraCaptureSessionImpl.this.mSkipUnconfigure) {
                    return;
                }
                CameraCaptureSessionImpl.this.mIdleDrainer.beginDrain();
            }
        }
    }

    private class IdleDrainListener implements DrainListener {
        private IdleDrainListener() {
        }

        public void onDrained() {
            synchronized (CameraCaptureSessionImpl.this.mDeviceImpl.mInterfaceLock) {
                synchronized (CameraCaptureSessionImpl.this) {
                    if (CameraCaptureSessionImpl.this.mSkipUnconfigure) {
                        return;
                    }
                    try {
                        CameraCaptureSessionImpl.this.mDeviceImpl.configureStreamsChecked(null, null, CameraCaptureSessionImpl.DEBUG);
                    } catch (CameraAccessException e) {
                        Log.e(CameraCaptureSessionImpl.TAG, CameraCaptureSessionImpl.this.mIdString + "Exception while unconfiguring outputs: ", e);
                    } catch (IllegalStateException e2) {
                    }
                    return;
                }
            }
        }
    }

    private class SequenceDrainListener implements DrainListener {
        private SequenceDrainListener() {
        }

        public void onDrained() {
            CameraCaptureSessionImpl.this.mStateCallback.onClosed(CameraCaptureSessionImpl.this);
            if (!CameraCaptureSessionImpl.this.mSkipUnconfigure) {
                CameraCaptureSessionImpl.this.mAbortDrainer.beginDrain();
            }
        }
    }

    CameraCaptureSessionImpl(int id, Surface input, List<Surface> outputs, StateCallback callback, Handler stateHandler, CameraDeviceImpl deviceImpl, Handler deviceStateHandler, boolean configureSuccess) {
        this.mClosed = DEBUG;
        this.mSkipUnconfigure = DEBUG;
        if (outputs == null || outputs.isEmpty()) {
            throw new IllegalArgumentException("outputs must be a non-null, non-empty list");
        } else if (callback == null) {
            throw new IllegalArgumentException("callback must not be null");
        } else {
            this.mId = id;
            this.mIdString = String.format("Session %d: ", new Object[]{Integer.valueOf(this.mId)});
            this.mOutputs = outputs;
            this.mInput = input;
            this.mStateHandler = CameraDeviceImpl.checkHandler(stateHandler);
            this.mStateCallback = createUserStateCallbackProxy(this.mStateHandler, callback);
            this.mDeviceHandler = (Handler) Preconditions.checkNotNull(deviceStateHandler, "deviceStateHandler must not be null");
            this.mDeviceImpl = (CameraDeviceImpl) Preconditions.checkNotNull(deviceImpl, "deviceImpl must not be null");
            this.mSequenceDrainer = new TaskDrainer(this.mDeviceHandler, new SequenceDrainListener(), "seq");
            this.mIdleDrainer = new TaskSingleDrainer(this.mDeviceHandler, new IdleDrainListener(), "idle");
            this.mAbortDrainer = new TaskSingleDrainer(this.mDeviceHandler, new AbortDrainListener(), "abort");
            if (configureSuccess) {
                this.mStateCallback.onConfigured(this);
                this.mConfigureSuccess = true;
                return;
            }
            this.mStateCallback.onConfigureFailed(this);
            this.mClosed = true;
            Log.e(TAG, this.mIdString + "Failed to create capture session; configuration failed");
            this.mConfigureSuccess = DEBUG;
        }
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

    public synchronized int capture(CaptureRequest request, CaptureCallback callback, Handler handler) throws CameraAccessException {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        } else if (request.isReprocess() && !isReprocessable()) {
            throw new IllegalArgumentException("this capture session cannot handle reprocess requests");
        } else if (!request.isReprocess() || request.getReprocessableSessionId() == this.mId) {
            checkNotClosed();
        } else {
            throw new IllegalArgumentException("capture request was created for another session");
        }
        return addPendingSequence(this.mDeviceImpl.capture(request, createCaptureCallbackProxy(CameraDeviceImpl.checkHandler(handler, callback), callback), this.mDeviceHandler));
    }

    public synchronized int captureBurst(List<CaptureRequest> requests, CaptureCallback callback, Handler handler) throws CameraAccessException {
        if (requests == null) {
            throw new IllegalArgumentException("Requests must not be null");
        } else if (requests.isEmpty()) {
            throw new IllegalArgumentException("Requests must have at least one element");
        } else {
            for (CaptureRequest request : requests) {
                if (request.isReprocess()) {
                    if (!isReprocessable()) {
                        throw new IllegalArgumentException("This capture session cannot handle reprocess requests");
                    } else if (request.getReprocessableSessionId() != this.mId) {
                        throw new IllegalArgumentException("Capture request was created for another session");
                    }
                }
            }
            checkNotClosed();
        }
        return addPendingSequence(this.mDeviceImpl.captureBurst(requests, createCaptureCallbackProxy(CameraDeviceImpl.checkHandler(handler, callback), callback), this.mDeviceHandler));
    }

    public synchronized int setRepeatingRequest(CaptureRequest request, CaptureCallback callback, Handler handler) throws CameraAccessException {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        } else if (request.isReprocess()) {
            throw new IllegalArgumentException("repeating reprocess requests are not supported");
        } else {
            checkNotClosed();
        }
        return addPendingSequence(this.mDeviceImpl.setRepeatingRequest(request, createCaptureCallbackProxy(CameraDeviceImpl.checkHandler(handler, callback), callback), this.mDeviceHandler));
    }

    public synchronized int setRepeatingBurst(List<CaptureRequest> requests, CaptureCallback callback, Handler handler) throws CameraAccessException {
        if (requests == null) {
            throw new IllegalArgumentException("requests must not be null");
        } else if (requests.isEmpty()) {
            throw new IllegalArgumentException("requests must have at least one element");
        } else {
            for (CaptureRequest r : requests) {
                if (r.isReprocess()) {
                    throw new IllegalArgumentException("repeating reprocess burst requests are not supported");
                }
            }
            checkNotClosed();
        }
        return addPendingSequence(this.mDeviceImpl.setRepeatingBurst(requests, createCaptureCallbackProxy(CameraDeviceImpl.checkHandler(handler, callback), callback), this.mDeviceHandler));
    }

    public synchronized void stopRepeating() throws CameraAccessException {
        checkNotClosed();
        this.mDeviceImpl.stopRepeating();
    }

    public synchronized void abortCaptures() throws CameraAccessException {
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

    public boolean isReprocessable() {
        return this.mInput != null ? true : DEBUG;
    }

    public Surface getInputSurface() {
        return this.mInput;
    }

    public synchronized void replaceSessionClose() {
        this.mSkipUnconfigure = true;
        close();
    }

    public synchronized void close() {
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

    public boolean isAborting() {
        return this.mAborting;
    }

    private StateCallback createUserStateCallbackProxy(Handler handler, StateCallback callback) {
        return new SessionStateCallbackProxy(new HandlerDispatcher(new InvokeDispatcher(callback), handler));
    }

    private CameraDeviceImpl.CaptureCallback createCaptureCallbackProxy(Handler handler, CaptureCallback callback) {
        CameraDeviceImpl.CaptureCallback localCallback = new CameraDeviceImpl.CaptureCallback() {
            public void onCaptureSequenceCompleted(CameraDevice camera, int sequenceId, long frameNumber) {
                CameraCaptureSessionImpl.this.finishPendingSequence(sequenceId);
            }

            public void onCaptureSequenceAborted(CameraDevice camera, int sequenceId) {
                CameraCaptureSessionImpl.this.finishPendingSequence(sequenceId);
            }
        };
        if (callback == null) {
            return localCallback;
        }
        InvokeDispatcher<CameraDeviceImpl.CaptureCallback> localSink = new InvokeDispatcher(localCallback);
        return new DeviceCaptureCallbackProxy(new BroadcastDispatcher(new ArgumentReplacingDispatcher(new DuckTypingDispatcher(new HandlerDispatcher(new InvokeDispatcher(callback), handler), CaptureCallback.class), 0, this), localSink));
    }

    public StateCallbackKK getDeviceStateCallback() {
        CameraCaptureSession session = this;
        return new AnonymousClass2(this);
    }

    protected void finalize() throws Throwable {
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

    private void finishPendingSequence(int sequenceId) {
        try {
            this.mSequenceDrainer.taskFinished(Integer.valueOf(sequenceId));
        } catch (IllegalStateException e) {
            Log.w(TAG, e.getMessage());
        }
    }
}
