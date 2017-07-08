package android.hardware.camera2.legacy;

import android.hardware.camera2.impl.CameraMetadataNative;
import android.os.Handler;
import android.util.Log;

public class CameraDeviceState {
    private static final boolean DEBUG = false;
    public static final int NO_CAPTURE_ERROR = -1;
    private static final int STATE_CAPTURING = 4;
    private static final int STATE_CONFIGURING = 2;
    private static final int STATE_ERROR = 0;
    private static final int STATE_IDLE = 3;
    private static final int STATE_UNCONFIGURED = 1;
    private static final String TAG = "CameraDeviceState";
    private static final String[] sStateNames = null;
    private int mCurrentError;
    private Handler mCurrentHandler;
    private CameraDeviceStateListener mCurrentListener;
    private RequestHolder mCurrentRequest;
    private int mCurrentState;

    /* renamed from: android.hardware.camera2.legacy.CameraDeviceState.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ int val$captureError;
        final /* synthetic */ Object val$captureErrorArg;
        final /* synthetic */ RequestHolder val$request;

        AnonymousClass1(int val$captureError, Object val$captureErrorArg, RequestHolder val$request) {
            this.val$captureError = val$captureError;
            this.val$captureErrorArg = val$captureErrorArg;
            this.val$request = val$request;
        }

        public void run() {
            CameraDeviceState.this.mCurrentListener.onError(this.val$captureError, this.val$captureErrorArg, this.val$request);
        }
    }

    /* renamed from: android.hardware.camera2.legacy.CameraDeviceState.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ RequestHolder val$request;
        final /* synthetic */ CameraMetadataNative val$result;

        AnonymousClass2(CameraMetadataNative val$result, RequestHolder val$request) {
            this.val$result = val$result;
            this.val$request = val$request;
        }

        public void run() {
            CameraDeviceState.this.mCurrentListener.onCaptureResult(this.val$result, this.val$request);
        }
    }

    /* renamed from: android.hardware.camera2.legacy.CameraDeviceState.3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ long val$lastFrameNumber;

        AnonymousClass3(long val$lastFrameNumber) {
            this.val$lastFrameNumber = val$lastFrameNumber;
        }

        public void run() {
            CameraDeviceState.this.mCurrentListener.onRepeatingRequestError(this.val$lastFrameNumber);
        }
    }

    /* renamed from: android.hardware.camera2.legacy.CameraDeviceState.8 */
    class AnonymousClass8 implements Runnable {
        final /* synthetic */ int val$error;

        AnonymousClass8(int val$error) {
            this.val$error = val$error;
        }

        public void run() {
            CameraDeviceState.this.mCurrentListener.onError(this.val$error, null, CameraDeviceState.this.mCurrentRequest);
        }
    }

    /* renamed from: android.hardware.camera2.legacy.CameraDeviceState.9 */
    class AnonymousClass9 implements Runnable {
        final /* synthetic */ long val$timestamp;

        AnonymousClass9(long val$timestamp) {
            this.val$timestamp = val$timestamp;
        }

        public void run() {
            CameraDeviceState.this.mCurrentListener.onCaptureStarted(CameraDeviceState.this.mCurrentRequest, this.val$timestamp);
        }
    }

    public interface CameraDeviceStateListener {
        void onBusy();

        void onCaptureResult(CameraMetadataNative cameraMetadataNative, RequestHolder requestHolder);

        void onCaptureStarted(RequestHolder requestHolder, long j);

        void onConfiguring();

        void onError(int i, Object obj, RequestHolder requestHolder);

        void onIdle();

        void onRepeatingRequestError(long j);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.camera2.legacy.CameraDeviceState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.camera2.legacy.CameraDeviceState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.CameraDeviceState.<clinit>():void");
    }

    public CameraDeviceState() {
        this.mCurrentState = STATE_UNCONFIGURED;
        this.mCurrentError = NO_CAPTURE_ERROR;
        this.mCurrentRequest = null;
        this.mCurrentHandler = null;
        this.mCurrentListener = null;
    }

    public synchronized void setError(int error) {
        this.mCurrentError = error;
        doStateTransition(STATE_ERROR);
    }

    public synchronized boolean setConfiguring() {
        doStateTransition(STATE_CONFIGURING);
        return this.mCurrentError == NO_CAPTURE_ERROR ? true : DEBUG;
    }

    public synchronized boolean setIdle() {
        doStateTransition(STATE_IDLE);
        return this.mCurrentError == NO_CAPTURE_ERROR ? true : DEBUG;
    }

    public synchronized boolean setCaptureStart(RequestHolder request, long timestamp, int captureError) {
        this.mCurrentRequest = request;
        doStateTransition(STATE_CAPTURING, timestamp, captureError);
        return this.mCurrentError == NO_CAPTURE_ERROR ? true : DEBUG;
    }

    public synchronized boolean setCaptureResult(RequestHolder request, CameraMetadataNative result, int captureError, Object captureErrorArg) {
        boolean z = true;
        synchronized (this) {
            if (this.mCurrentState != STATE_CAPTURING) {
                Log.e(TAG, "Cannot receive result while in state: " + this.mCurrentState);
                this.mCurrentError = STATE_UNCONFIGURED;
                doStateTransition(STATE_ERROR);
                if (this.mCurrentError != NO_CAPTURE_ERROR) {
                    z = DEBUG;
                }
                return z;
            }
            if (!(this.mCurrentHandler == null || this.mCurrentListener == null)) {
                if (captureError != NO_CAPTURE_ERROR) {
                    this.mCurrentHandler.post(new AnonymousClass1(captureError, captureErrorArg, request));
                } else {
                    this.mCurrentHandler.post(new AnonymousClass2(result, request));
                }
            }
            if (this.mCurrentError != NO_CAPTURE_ERROR) {
                z = DEBUG;
            }
            return z;
        }
    }

    public synchronized boolean setCaptureResult(RequestHolder request, CameraMetadataNative result) {
        return setCaptureResult(request, result, NO_CAPTURE_ERROR, null);
    }

    public synchronized void setRepeatingRequestError(long lastFrameNumber) {
        this.mCurrentHandler.post(new AnonymousClass3(lastFrameNumber));
    }

    public synchronized void setCameraDeviceCallbacks(Handler handler, CameraDeviceStateListener listener) {
        this.mCurrentHandler = handler;
        this.mCurrentListener = listener;
    }

    private void doStateTransition(int newState) {
        doStateTransition(newState, 0, NO_CAPTURE_ERROR);
    }

    private void doStateTransition(int newState, long timestamp, int error) {
        if (newState != this.mCurrentState) {
            String stateName = "UNKNOWN";
            if (newState >= 0 && newState < sStateNames.length) {
                stateName = sStateNames[newState];
            }
            Log.i(TAG, "Legacy camera service transitioning to state " + stateName);
        }
        if (!(newState == 0 || newState == STATE_IDLE || this.mCurrentState == newState || this.mCurrentHandler == null || this.mCurrentListener == null)) {
            this.mCurrentHandler.post(new Runnable() {
                public void run() {
                    CameraDeviceState.this.mCurrentListener.onBusy();
                }
            });
        }
        switch (newState) {
            case STATE_ERROR /*0*/:
                if (!(this.mCurrentState == 0 || this.mCurrentHandler == null || this.mCurrentListener == null)) {
                    this.mCurrentHandler.post(new Runnable() {
                        public void run() {
                            CameraDeviceState.this.mCurrentListener.onError(CameraDeviceState.this.mCurrentError, null, CameraDeviceState.this.mCurrentRequest);
                        }
                    });
                }
                this.mCurrentState = STATE_ERROR;
            case STATE_CONFIGURING /*2*/:
                if (this.mCurrentState == STATE_UNCONFIGURED || this.mCurrentState == STATE_IDLE) {
                    if (!(this.mCurrentState == STATE_CONFIGURING || this.mCurrentHandler == null || this.mCurrentListener == null)) {
                        this.mCurrentHandler.post(new Runnable() {
                            public void run() {
                                CameraDeviceState.this.mCurrentListener.onConfiguring();
                            }
                        });
                    }
                    this.mCurrentState = STATE_CONFIGURING;
                    return;
                }
                Log.e(TAG, "Cannot call configure while in state: " + this.mCurrentState);
                this.mCurrentError = STATE_UNCONFIGURED;
                doStateTransition(STATE_ERROR);
            case STATE_IDLE /*3*/:
                if (this.mCurrentState == STATE_IDLE) {
                    return;
                }
                if (this.mCurrentState == STATE_CONFIGURING || this.mCurrentState == STATE_CAPTURING) {
                    if (!(this.mCurrentState == STATE_IDLE || this.mCurrentHandler == null || this.mCurrentListener == null)) {
                        this.mCurrentHandler.post(new Runnable() {
                            public void run() {
                                CameraDeviceState.this.mCurrentListener.onIdle();
                            }
                        });
                    }
                    this.mCurrentState = STATE_IDLE;
                    return;
                }
                Log.e(TAG, "Cannot call idle while in state: " + this.mCurrentState);
                this.mCurrentError = STATE_UNCONFIGURED;
                doStateTransition(STATE_ERROR);
            case STATE_CAPTURING /*4*/:
                if (this.mCurrentState == STATE_IDLE || this.mCurrentState == STATE_CAPTURING) {
                    if (!(this.mCurrentHandler == null || this.mCurrentListener == null)) {
                        if (error != NO_CAPTURE_ERROR) {
                            this.mCurrentHandler.post(new AnonymousClass8(error));
                        } else {
                            this.mCurrentHandler.post(new AnonymousClass9(timestamp));
                        }
                    }
                    this.mCurrentState = STATE_CAPTURING;
                    return;
                }
                Log.e(TAG, "Cannot call capture while in state: " + this.mCurrentState);
                this.mCurrentError = STATE_UNCONFIGURED;
                doStateTransition(STATE_ERROR);
            default:
                throw new IllegalStateException("Transition to unknown state: " + newState);
        }
    }
}
