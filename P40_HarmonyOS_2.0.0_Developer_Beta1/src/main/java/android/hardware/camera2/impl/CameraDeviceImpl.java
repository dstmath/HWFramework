package android.hardware.camera2.impl;

import android.app.ActivityThread;
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
    private final String TAG;
    private final int mAppTargetSdkVersion;
    private final Runnable mCallOnActive = new Runnable() {
        /* class android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass3 */

        @Override // java.lang.Runnable
        public void run() {
            StateCallbackKK sessionCallback;
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
                } else {
                    return;
                }
            }
            if (sessionCallback != null) {
                sessionCallback.onActive(CameraDeviceImpl.this);
            }
        }
    };
    private final Runnable mCallOnBusy = new Runnable() {
        /* class android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass4 */

        @Override // java.lang.Runnable
        public void run() {
            StateCallbackKK sessionCallback;
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
                } else {
                    return;
                }
            }
            if (sessionCallback != null) {
                sessionCallback.onBusy(CameraDeviceImpl.this);
            }
        }
    };
    private final Runnable mCallOnClosed = new Runnable() {
        /* class android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass5 */
        private boolean mClosedOnce = false;

        @Override // java.lang.Runnable
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
    private final Runnable mCallOnDisconnected = new Runnable() {
        /* class android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass7 */

        @Override // java.lang.Runnable
        public void run() {
            StateCallbackKK sessionCallback;
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
                } else {
                    return;
                }
            }
            if (sessionCallback != null) {
                sessionCallback.onDisconnected(CameraDeviceImpl.this);
            }
            CameraDeviceImpl.this.mDeviceCallback.onDisconnected(CameraDeviceImpl.this);
        }
    };
    private final Runnable mCallOnIdle = new Runnable() {
        /* class android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass6 */

        @Override // java.lang.Runnable
        public void run() {
            StateCallbackKK sessionCallback;
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
                } else {
                    return;
                }
            }
            if (sessionCallback != null) {
                sessionCallback.onIdle(CameraDeviceImpl.this);
            }
        }
    };
    private final Runnable mCallOnOpened = new Runnable() {
        /* class android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            StateCallbackKK sessionCallback;
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
                } else {
                    return;
                }
            }
            if (sessionCallback != null) {
                sessionCallback.onOpened(CameraDeviceImpl.this);
            }
            CameraDeviceImpl.this.mDeviceCallback.onOpened(CameraDeviceImpl.this);
        }
    };
    private final Runnable mCallOnUnconfigured = new Runnable() {
        /* class android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            StateCallbackKK sessionCallback;
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
                } else {
                    return;
                }
            }
            if (sessionCallback != null) {
                sessionCallback.onUnconfigured(CameraDeviceImpl.this);
            }
        }
    };
    private final CameraDeviceCallbacks mCallbacks = new CameraDeviceCallbacks();
    private final String mCameraId;
    private final SparseArray<CaptureCallbackHolder> mCaptureCallbackMap = new SparseArray<>();
    private final CameraCharacteristics mCharacteristics;
    private final AtomicBoolean mClosing = new AtomicBoolean();
    private AbstractMap.SimpleEntry<Integer, InputConfiguration> mConfiguredInput = new AbstractMap.SimpleEntry<>(-1, null);
    private final SparseArray<OutputConfiguration> mConfiguredOutputs = new SparseArray<>();
    private CameraCaptureSessionCore mCurrentSession;
    private final CameraDevice.StateCallback mDeviceCallback;
    private final Executor mDeviceExecutor;
    private final FrameNumberTracker mFrameNumberTracker = new FrameNumberTracker();
    private boolean mIdle = true;
    private boolean mInError = false;
    final Object mInterfaceLock = new Object();
    private int mNextSessionId = 0;
    private ICameraDeviceUserWrapper mRemoteDevice;
    private int mRepeatingRequestId = -1;
    private int[] mRepeatingRequestTypes;
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

    public CameraDeviceImpl(String cameraId, CameraDevice.StateCallback callback, Executor executor, CameraCharacteristics characteristics, int appTargetSdkVersion) {
        if (cameraId == null || callback == null || executor == null || characteristics == null) {
            throw new IllegalArgumentException("Null argument given");
        }
        this.mCameraId = cameraId;
        this.mDeviceCallback = callback;
        this.mDeviceExecutor = executor;
        this.mCharacteristics = characteristics;
        this.mAppTargetSdkVersion = appTargetSdkVersion;
        String tag = String.format("CameraDevice-JV-%s", this.mCameraId);
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
        final int failureCode = 4;
        final boolean failureIsError = true;
        int i = failure.errorCode;
        if (i == 4) {
            failureIsError = false;
        } else if (i == 10) {
            failureCode = 4;
        } else if (i == 6) {
            failureCode = 3;
        } else if (i == 7) {
            failureCode = 1;
        } else if (i != 8) {
            String str = this.TAG;
            Log.e(str, "Unexpected failure in opening camera device: " + failure.errorCode + failure.getMessage());
        } else {
            failureCode = 2;
        }
        synchronized (this.mInterfaceLock) {
            this.mInError = true;
            this.mDeviceExecutor.execute(new Runnable() {
                /* class android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass8 */

                @Override // java.lang.Runnable
                public void run() {
                    if (failureIsError) {
                        CameraDeviceImpl.this.mDeviceCallback.onError(CameraDeviceImpl.this, failureCode);
                    } else {
                        CameraDeviceImpl.this.mDeviceCallback.onDisconnected(CameraDeviceImpl.this);
                    }
                }
            });
        }
    }

    @Override // android.hardware.camera2.CameraDevice
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
            outputs = new ArrayList();
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
                    if (1 == 0 || outputs.size() <= 0) {
                        this.mDeviceExecutor.execute(this.mCallOnUnconfigured);
                    } else {
                        this.mDeviceExecutor.execute(this.mCallOnIdle);
                    }
                } catch (IllegalArgumentException e) {
                    Log.w(this.TAG, "Stream configuration failed due to: " + e.getMessage());
                    if (0 == 0 || outputs.size() <= 0) {
                        this.mDeviceExecutor.execute(this.mCallOnUnconfigured);
                    } else {
                        this.mDeviceExecutor.execute(this.mCallOnIdle);
                    }
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

    @Override // android.hardware.camera2.CameraDevice
    public void createCaptureSession(List<Surface> outputs, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        List<OutputConfiguration> outConfigurations = new ArrayList<>(outputs.size());
        for (Surface surface : outputs) {
            outConfigurations.add(new OutputConfiguration(surface));
        }
        createCaptureSessionInternal(null, outConfigurations, callback, checkAndWrapHandler(handler), 0, null);
    }

    @Override // android.hardware.camera2.CameraDevice
    public void createCaptureSessionByOutputConfigurations(List<OutputConfiguration> outputConfigurations, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        createCaptureSessionInternal(null, new ArrayList<>(outputConfigurations), callback, checkAndWrapHandler(handler), 0, null);
    }

    @Override // android.hardware.camera2.CameraDevice
    public void createReprocessableCaptureSession(InputConfiguration inputConfig, List<Surface> outputs, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        if (inputConfig != null) {
            List<OutputConfiguration> outConfigurations = new ArrayList<>(outputs.size());
            for (Surface surface : outputs) {
                outConfigurations.add(new OutputConfiguration(surface));
            }
            createCaptureSessionInternal(inputConfig, outConfigurations, callback, checkAndWrapHandler(handler), 0, null);
            return;
        }
        throw new IllegalArgumentException("inputConfig cannot be null when creating a reprocessable capture session");
    }

    @Override // android.hardware.camera2.CameraDevice
    public void createReprocessableCaptureSessionByConfigurations(InputConfiguration inputConfig, List<OutputConfiguration> outputs, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        if (inputConfig == null) {
            throw new IllegalArgumentException("inputConfig cannot be null when creating a reprocessable capture session");
        } else if (outputs != null) {
            List<OutputConfiguration> currentOutputs = new ArrayList<>();
            for (OutputConfiguration output : outputs) {
                currentOutputs.add(new OutputConfiguration(output));
            }
            createCaptureSessionInternal(inputConfig, currentOutputs, callback, checkAndWrapHandler(handler), 0, null);
        } else {
            throw new IllegalArgumentException("Output configurations cannot be null when creating a reprocessable capture session");
        }
    }

    @Override // android.hardware.camera2.CameraDevice
    public void createConstrainedHighSpeedCaptureSession(List<Surface> outputs, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        if (outputs == null || outputs.size() == 0 || outputs.size() > 2) {
            throw new IllegalArgumentException("Output surface list must not be null and the size must be no more than 2");
        }
        List<OutputConfiguration> outConfigurations = new ArrayList<>(outputs.size());
        for (Surface surface : outputs) {
            outConfigurations.add(new OutputConfiguration(surface));
        }
        createCaptureSessionInternal(null, outConfigurations, callback, checkAndWrapHandler(handler), 1, null);
    }

    @Override // android.hardware.camera2.CameraDevice
    public void createCustomCaptureSession(InputConfiguration inputConfig, List<OutputConfiguration> outputs, int operatingMode, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        List<OutputConfiguration> currentOutputs = new ArrayList<>();
        for (OutputConfiguration output : outputs) {
            currentOutputs.add(new OutputConfiguration(output));
        }
        createCaptureSessionInternal(inputConfig, currentOutputs, callback, checkAndWrapHandler(handler), operatingMode, null);
    }

    @Override // android.hardware.camera2.CameraDevice
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

    private void createCaptureSessionInternal(InputConfiguration inputConfig, List<OutputConfiguration> outputConfigurations, CameraCaptureSession.StateCallback callback, Executor executor, int operatingMode, CaptureRequest sessionParams) throws CameraAccessException {
        CameraAccessException pendingException;
        boolean configureSuccess;
        Surface input;
        CameraCaptureSessionCore newSession;
        synchronized (this.mInterfaceLock) {
            try {
                checkIfCameraClosedOrInError();
                boolean isConstrainedHighSpeed = operatingMode == 1;
                if (isConstrainedHighSpeed) {
                    if (inputConfig != null) {
                        throw new IllegalArgumentException("Constrained high speed session doesn't support input configuration yet.");
                    }
                }
                if (this.mCurrentSession != null) {
                    this.mCurrentSession.replaceSessionClose();
                }
                Surface input2 = null;
                try {
                    boolean configureSuccess2 = configureStreamsChecked(inputConfig, outputConfigurations, operatingMode, sessionParams);
                    if (configureSuccess2 && inputConfig != null) {
                        input2 = this.mRemoteDevice.getInputSurface();
                    }
                    configureSuccess = configureSuccess2;
                    pendingException = null;
                    input = input2;
                } catch (CameraAccessException e) {
                    configureSuccess = false;
                    pendingException = e;
                    input = null;
                }
                if (isConstrainedHighSpeed) {
                    ArrayList<Surface> surfaces = new ArrayList<>(outputConfigurations.size());
                    for (OutputConfiguration outConfig : outputConfigurations) {
                        surfaces.add(outConfig.getSurface());
                    }
                    SurfaceUtils.checkConstrainedHighSpeedSurfaces(surfaces, null, (StreamConfigurationMap) getCharacteristics().get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP));
                    int i = this.mNextSessionId;
                    this.mNextSessionId = i + 1;
                    newSession = new CameraConstrainedHighSpeedCaptureSessionImpl(i, callback, executor, this, this.mDeviceExecutor, configureSuccess, this.mCharacteristics);
                } else {
                    int i2 = this.mNextSessionId;
                    this.mNextSessionId = i2 + 1;
                    newSession = new CameraCaptureSessionImpl(i2, input, callback, executor, this, this.mDeviceExecutor, configureSuccess);
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

    @Override // android.hardware.camera2.CameraDevice
    public boolean isSessionConfigurationSupported(SessionConfiguration sessionConfig) throws CameraAccessException, UnsupportedOperationException, IllegalArgumentException {
        boolean isSessionConfigurationSupported;
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            isSessionConfigurationSupported = this.mRemoteDevice.isSessionConfigurationSupported(sessionConfig);
        }
        return isSessionConfigurationSupported;
    }

    public void setSessionListener(StateCallbackKK sessionCallback) {
        synchronized (this.mInterfaceLock) {
            this.mSessionStateCallback = sessionCallback;
        }
    }

    private void overrideEnableZsl(CameraMetadataNative request, boolean newValue) {
        if (((Boolean) request.get(CaptureRequest.CONTROL_ENABLE_ZSL)) != null) {
            request.set((CaptureRequest.Key<CaptureRequest.Key<Boolean>>) CaptureRequest.CONTROL_ENABLE_ZSL, (CaptureRequest.Key<Boolean>) Boolean.valueOf(newValue));
        }
    }

    @Override // android.hardware.camera2.CameraDevice
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

    @Override // android.hardware.camera2.CameraDevice
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

    @Override // android.hardware.camera2.CameraDevice
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
    /* access modifiers changed from: public */
    private void checkEarlyTriggerSequenceComplete(final int requestId, long lastFrameNumber, int[] repeatingRequestTypes) {
        if (lastFrameNumber == -1) {
            int index = this.mCaptureCallbackMap.indexOfKey(requestId);
            final CaptureCallbackHolder holder = index >= 0 ? this.mCaptureCallbackMap.valueAt(index) : null;
            if (holder != null) {
                this.mCaptureCallbackMap.removeAt(index);
            }
            if (holder != null) {
                Runnable resultDispatch = new Runnable() {
                    /* class android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass9 */

                    @Override // java.lang.Runnable
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
                Log.w(this.TAG, String.format("did not register callback to request %d", Integer.valueOf(requestId)));
            }
        } else {
            this.mRequestLastFrameNumbersList.add(new RequestLastFrameNumbersHolder(requestId, lastFrameNumber, repeatingRequestTypes));
            checkAndFireSequenceComplete();
        }
    }

    private int[] getRequestTypes(CaptureRequest[] requestArray) {
        int[] requestTypes = new int[requestArray.length];
        for (int i = 0; i < requestArray.length; i++) {
            requestTypes[i] = requestArray[i].getRequestType();
        }
        return requestTypes;
    }

    private int submitCaptureRequest(List<CaptureRequest> requestList, CaptureCallback callback, Executor executor, boolean repeating) throws CameraAccessException {
        int requestId;
        Executor executor2 = checkExecutor(executor, callback);
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
            if (repeating) {
                stopRepeating();
            }
            CaptureRequest[] requestArray = (CaptureRequest[]) requestList.toArray(new CaptureRequest[requestList.size()]);
            for (CaptureRequest request2 : requestArray) {
                request2.convertSurfaceToStreamId(this.mConfiguredOutputs);
            }
            SubmitInfo requestInfo = this.mRemoteDevice.submitRequestList(requestArray, repeating);
            for (CaptureRequest request3 : requestArray) {
                request3.recoverStreamIdToSurface();
            }
            if (callback != null) {
                this.mCaptureCallbackMap.put(requestInfo.getRequestId(), new CaptureCallbackHolder(callback, requestList, executor2, repeating, this.mNextSessionId - 1));
            }
            if (repeating) {
                if (this.mRepeatingRequestId != -1) {
                    checkEarlyTriggerSequenceComplete(this.mRepeatingRequestId, requestInfo.getLastFrameNumber(), this.mRepeatingRequestTypes);
                }
                this.mRepeatingRequestId = requestInfo.getRequestId();
                this.mRepeatingRequestTypes = getRequestTypes(requestArray);
            } else {
                this.mRequestLastFrameNumbersList.add(new RequestLastFrameNumbersHolder(requestList, requestInfo));
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
                int[] requestTypes = this.mRepeatingRequestTypes;
                this.mRepeatingRequestTypes = null;
                try {
                    checkEarlyTriggerSequenceComplete(requestId, this.mRemoteDevice.cancelRequest(requestId), requestTypes);
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
                checkEarlyTriggerSequenceComplete(this.mRepeatingRequestId, lastFrameNumber, this.mRepeatingRequestTypes);
                this.mRepeatingRequestId = -1;
                this.mRepeatingRequestTypes = null;
            }
        }
    }

    @Override // android.hardware.camera2.CameraDevice, java.lang.AutoCloseable
    public void close() {
        synchronized (this.mInterfaceLock) {
            if (!this.mClosing.getAndSet(true)) {
                if (this.mRemoteDevice != null) {
                    String str = this.TAG;
                    Log.i(str, "close camera: " + this.mCameraId + ", package name: " + ActivityThread.currentOpPackageName());
                    HwSystemManager.notifyBackgroundMgr(ActivityThread.currentOpPackageName(), Binder.getCallingPid(), Binder.getCallingUid(), 0, 0);
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
    @Override // java.lang.Object
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
                Size[] inputSizes = configMap.getInputSizes(inputConfig.getFormat());
                for (Size s : inputSizes) {
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

    /* access modifiers changed from: package-private */
    public static class CaptureCallbackHolder {
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
                throw new IllegalArgumentException(String.format("Requested subsequenceId %d is larger than request list size %d.", Integer.valueOf(subsequenceId), Integer.valueOf(this.mRequestList.size())));
            } else if (subsequenceId >= 0) {
                return this.mRequestList.get(subsequenceId);
            } else {
                throw new IllegalArgumentException(String.format("Requested subsequenceId %d is negative", Integer.valueOf(subsequenceId)));
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

    /* access modifiers changed from: package-private */
    public static class RequestLastFrameNumbersHolder {
        private final long mLastRegularFrameNumber;
        private final long mLastReprocessFrameNumber;
        private final long mLastZslStillFrameNumber;
        private final int mRequestId;

        public RequestLastFrameNumbersHolder(List<CaptureRequest> requestList, SubmitInfo requestInfo) {
            long lastRegularFrameNumber = -1;
            long lastReprocessFrameNumber = -1;
            long lastZslStillFrameNumber = -1;
            long frameNumber = requestInfo.getLastFrameNumber();
            int i = 1;
            if (requestInfo.getLastFrameNumber() >= ((long) (requestList.size() - 1))) {
                int i2 = requestList.size() - 1;
                while (true) {
                    if (i2 >= 0) {
                        int requestType = requestList.get(i2).getRequestType();
                        if (requestType == i && lastReprocessFrameNumber == -1) {
                            lastReprocessFrameNumber = frameNumber;
                        } else if (requestType == 2 && lastZslStillFrameNumber == -1) {
                            lastZslStillFrameNumber = frameNumber;
                        } else if (requestType == 0 && lastRegularFrameNumber == -1) {
                            lastRegularFrameNumber = frameNumber;
                        }
                        if (lastReprocessFrameNumber != -1 && lastZslStillFrameNumber != -1 && lastRegularFrameNumber != -1) {
                            break;
                        }
                        frameNumber--;
                        i2--;
                        i = 1;
                    } else {
                        break;
                    }
                }
                this.mLastRegularFrameNumber = lastRegularFrameNumber;
                this.mLastReprocessFrameNumber = lastReprocessFrameNumber;
                this.mLastZslStillFrameNumber = lastZslStillFrameNumber;
                this.mRequestId = requestInfo.getRequestId();
                return;
            }
            throw new IllegalArgumentException("lastFrameNumber: " + requestInfo.getLastFrameNumber() + " should be at least " + (requestList.size() - 1) + " for the number of  requests in the list: " + requestList.size());
        }

        RequestLastFrameNumbersHolder(int requestId, long lastFrameNumber, int[] repeatingRequestTypes) {
            long lastRegularFrameNumber = -1;
            long lastZslStillFrameNumber = -1;
            if (repeatingRequestTypes == null) {
                throw new IllegalArgumentException("repeatingRequest list must not be null");
            } else if (lastFrameNumber >= ((long) (repeatingRequestTypes.length - 1))) {
                long frameNumber = lastFrameNumber;
                int i = repeatingRequestTypes.length;
                while (true) {
                    i--;
                    if (i >= 0) {
                        if (repeatingRequestTypes[i] == 2 && lastZslStillFrameNumber == -1) {
                            lastZslStillFrameNumber = frameNumber;
                        } else if (repeatingRequestTypes[i] == 0 && lastRegularFrameNumber == -1) {
                            lastRegularFrameNumber = frameNumber;
                        }
                        if (lastZslStillFrameNumber != -1 && lastRegularFrameNumber != -1) {
                            break;
                        }
                        frameNumber--;
                    } else {
                        break;
                    }
                }
                this.mLastRegularFrameNumber = lastRegularFrameNumber;
                this.mLastZslStillFrameNumber = lastZslStillFrameNumber;
                this.mLastReprocessFrameNumber = -1;
                this.mRequestId = requestId;
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("lastFrameNumber: ");
                sb.append(lastFrameNumber);
                sb.append(" should be at least ");
                sb.append(repeatingRequestTypes.length - 1);
                sb.append(" for the number of requests in the list: ");
                sb.append(repeatingRequestTypes.length);
                throw new IllegalArgumentException(sb.toString());
            }
        }

        public long getLastRegularFrameNumber() {
            return this.mLastRegularFrameNumber;
        }

        public long getLastReprocessFrameNumber() {
            return this.mLastReprocessFrameNumber;
        }

        public long getLastZslStillFrameNumber() {
            return this.mLastZslStillFrameNumber;
        }

        public long getLastFrameNumber() {
            return Math.max(this.mLastZslStillFrameNumber, Math.max(this.mLastRegularFrameNumber, this.mLastReprocessFrameNumber));
        }

        public int getRequestId() {
            return this.mRequestId;
        }
    }

    public class FrameNumberTracker {
        private long[] mCompletedFrameNumber = new long[3];
        private final TreeMap<Long, Integer> mFutureErrorMap = new TreeMap<>();
        private final HashMap<Long, List<CaptureResult>> mPartialResults = new HashMap<>();
        private final LinkedList<Long>[] mSkippedFrameNumbers = new LinkedList[3];
        private final LinkedList<Long>[] mSkippedOtherFrameNumbers = new LinkedList[3];

        public FrameNumberTracker() {
            for (int i = 0; i < 3; i++) {
                this.mCompletedFrameNumber[i] = -1;
                this.mSkippedOtherFrameNumbers[i] = new LinkedList<>();
                this.mSkippedFrameNumbers[i] = new LinkedList<>();
            }
        }

        private void update() {
            Iterator iter = this.mFutureErrorMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Long, Integer> pair = iter.next();
                Long errorFrameNumber = pair.getKey();
                int requestType = pair.getValue().intValue();
                Boolean removeError = false;
                long longValue = errorFrameNumber.longValue();
                long[] jArr = this.mCompletedFrameNumber;
                if (longValue != jArr[requestType] + 1) {
                    if (this.mSkippedFrameNumbers[requestType].isEmpty()) {
                        int i = 1;
                        while (true) {
                            if (i >= 3) {
                                break;
                            }
                            int otherType = (requestType + i) % 3;
                            if (!this.mSkippedOtherFrameNumbers[otherType].isEmpty() && errorFrameNumber == this.mSkippedOtherFrameNumbers[otherType].element()) {
                                this.mCompletedFrameNumber[requestType] = errorFrameNumber.longValue();
                                this.mSkippedOtherFrameNumbers[otherType].remove();
                                removeError = true;
                                break;
                            }
                            i++;
                        }
                    } else if (errorFrameNumber == this.mSkippedFrameNumbers[requestType].element()) {
                        this.mCompletedFrameNumber[requestType] = errorFrameNumber.longValue();
                        this.mSkippedFrameNumbers[requestType].remove();
                        removeError = true;
                    }
                } else {
                    jArr[requestType] = errorFrameNumber.longValue();
                    removeError = true;
                }
                if (removeError.booleanValue()) {
                    iter.remove();
                }
            }
        }

        public void updateTracker(long frameNumber, boolean isError, int requestType) {
            if (isError) {
                this.mFutureErrorMap.put(Long.valueOf(frameNumber), Integer.valueOf(requestType));
            } else {
                try {
                    updateCompletedFrameNumber(frameNumber, requestType);
                } catch (IllegalArgumentException e) {
                    Log.e(CameraDeviceImpl.this.TAG, e.getMessage());
                }
            }
            update();
        }

        public void updateTracker(long frameNumber, CaptureResult result, boolean partial, int requestType) {
            if (!partial) {
                updateTracker(frameNumber, false, requestType);
            } else if (result != null) {
                List<CaptureResult> partials = this.mPartialResults.get(Long.valueOf(frameNumber));
                if (partials == null) {
                    partials = new ArrayList();
                    this.mPartialResults.put(Long.valueOf(frameNumber), partials);
                }
                partials.add(result);
            }
        }

        public List<CaptureResult> popPartialResults(long frameNumber) {
            return this.mPartialResults.remove(Long.valueOf(frameNumber));
        }

        public long getCompletedFrameNumber() {
            return this.mCompletedFrameNumber[0];
        }

        public long getCompletedReprocessFrameNumber() {
            return this.mCompletedFrameNumber[1];
        }

        public long getCompletedZslStillFrameNumber() {
            return this.mCompletedFrameNumber[2];
        }

        private void updateCompletedFrameNumber(long frameNumber, int requestType) throws IllegalArgumentException {
            int index;
            LinkedList<Long> dstList;
            LinkedList<Long> srcList;
            long[] jArr = this.mCompletedFrameNumber;
            if (frameNumber > jArr[requestType]) {
                int otherType1 = (requestType + 1) % 3;
                int otherType2 = (requestType + 2) % 3;
                long maxOtherFrameNumberSeen = Math.max(jArr[otherType1], jArr[otherType2]);
                if (frameNumber >= maxOtherFrameNumberSeen) {
                    long i = Math.max(maxOtherFrameNumberSeen, this.mCompletedFrameNumber[requestType]);
                    while (true) {
                        i++;
                        if (i >= frameNumber) {
                            break;
                        }
                        this.mSkippedOtherFrameNumbers[requestType].add(Long.valueOf(i));
                    }
                } else if (this.mSkippedFrameNumbers[requestType].isEmpty()) {
                    int index1 = this.mSkippedOtherFrameNumbers[otherType1].indexOf(Long.valueOf(frameNumber));
                    int index2 = this.mSkippedOtherFrameNumbers[otherType2].indexOf(Long.valueOf(frameNumber));
                    boolean inSkippedOther2 = true;
                    boolean inSkippedOther1 = index1 != -1;
                    if (index2 == -1) {
                        inSkippedOther2 = false;
                    }
                    if (inSkippedOther1 ^ inSkippedOther2) {
                        if (inSkippedOther1) {
                            srcList = this.mSkippedOtherFrameNumbers[otherType1];
                            dstList = this.mSkippedFrameNumbers[otherType2];
                            index = index1;
                        } else {
                            srcList = this.mSkippedOtherFrameNumbers[otherType2];
                            dstList = this.mSkippedFrameNumbers[otherType1];
                            index = index2;
                        }
                        for (int i2 = 0; i2 < index; i2++) {
                            dstList.add(srcList.removeFirst());
                        }
                        srcList.remove();
                    } else {
                        throw new IllegalArgumentException("frame number " + frameNumber + " is a repeat or invalid");
                    }
                } else if (frameNumber < this.mSkippedFrameNumbers[requestType].element().longValue()) {
                    throw new IllegalArgumentException("frame number " + frameNumber + " is a repeat");
                } else if (frameNumber <= this.mSkippedFrameNumbers[requestType].element().longValue()) {
                    this.mSkippedFrameNumbers[requestType].remove();
                } else {
                    throw new IllegalArgumentException("frame number " + frameNumber + " comes out of order. Expecting " + this.mSkippedFrameNumbers[requestType].element());
                }
                this.mCompletedFrameNumber[requestType] = frameNumber;
                return;
            }
            throw new IllegalArgumentException("frame number " + frameNumber + " is a repeat");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkAndFireSequenceComplete() {
        Throwable th;
        final CaptureCallbackHolder holder;
        long completedFrameNumber;
        long completedFrameNumber2 = this.mFrameNumberTracker.getCompletedFrameNumber();
        long completedReprocessFrameNumber = this.mFrameNumberTracker.getCompletedReprocessFrameNumber();
        long completedZslStillFrameNumber = this.mFrameNumberTracker.getCompletedZslStillFrameNumber();
        Iterator<RequestLastFrameNumbersHolder> iter = this.mRequestLastFrameNumbersList.iterator();
        while (iter.hasNext()) {
            final RequestLastFrameNumbersHolder requestLastFrameNumbers = iter.next();
            boolean sequenceCompleted = false;
            final int requestId = requestLastFrameNumbers.getRequestId();
            synchronized (this.mInterfaceLock) {
                try {
                    if (this.mRemoteDevice == null) {
                        try {
                            Log.w(this.TAG, "Camera closed while checking sequences");
                            return;
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    } else {
                        int index = this.mCaptureCallbackMap.indexOfKey(requestId);
                        holder = index >= 0 ? this.mCaptureCallbackMap.valueAt(index) : null;
                        if (holder != null) {
                            long lastRegularFrameNumber = requestLastFrameNumbers.getLastRegularFrameNumber();
                            long lastReprocessFrameNumber = requestLastFrameNumbers.getLastReprocessFrameNumber();
                            long lastZslStillFrameNumber = requestLastFrameNumbers.getLastZslStillFrameNumber();
                            if (lastRegularFrameNumber > completedFrameNumber2 || lastReprocessFrameNumber > completedReprocessFrameNumber || lastZslStillFrameNumber > completedZslStillFrameNumber) {
                                completedFrameNumber = completedFrameNumber2;
                            } else {
                                sequenceCompleted = true;
                                completedFrameNumber = completedFrameNumber2;
                                try {
                                    this.mCaptureCallbackMap.removeAt(index);
                                } catch (Throwable th3) {
                                    th = th3;
                                    throw th;
                                }
                            }
                        } else {
                            completedFrameNumber = completedFrameNumber2;
                        }
                    }
                } catch (Throwable th4) {
                    th = th4;
                    throw th;
                }
            }
            if (holder == null || sequenceCompleted) {
                iter.remove();
            }
            if (sequenceCompleted) {
                Runnable resultDispatch = new Runnable() {
                    /* class android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass10 */

                    @Override // java.lang.Runnable
                    public void run() {
                        if (!CameraDeviceImpl.this.isClosed()) {
                            holder.getCallback().onCaptureSequenceCompleted(CameraDeviceImpl.this, requestId, requestLastFrameNumbers.getLastFrameNumber());
                        }
                    }
                };
                long ident = Binder.clearCallingIdentity();
                try {
                    holder.getExecutor().execute(resultDispatch);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
            completedFrameNumber2 = completedFrameNumber;
        }
    }

    public class CameraDeviceCallbacks extends ICameraDeviceCallbacks.Stub {
        public CameraDeviceCallbacks() {
        }

        @Override // android.hardware.camera2.ICameraDeviceCallbacks.Stub, android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.hardware.camera2.ICameraDeviceCallbacks
        public void onDeviceError(int errorCode, CaptureResultExtras resultExtras) {
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    if (errorCode == 0) {
                        long ident = Binder.clearCallingIdentity();
                        try {
                            CameraDeviceImpl.this.mDeviceExecutor.execute(CameraDeviceImpl.this.mCallOnDisconnected);
                        } finally {
                            Binder.restoreCallingIdentity(ident);
                        }
                    } else if (errorCode == 1) {
                        scheduleNotifyError(4);
                    } else if (errorCode == 3 || errorCode == 4 || errorCode == 5) {
                        onCaptureErrorLocked(errorCode, resultExtras);
                    } else if (errorCode != 6) {
                        String str = CameraDeviceImpl.this.TAG;
                        Log.e(str, "Unknown error from camera device: " + errorCode);
                        scheduleNotifyError(5);
                    } else {
                        scheduleNotifyError(3);
                    }
                }
            }
        }

        private void scheduleNotifyError(int code) {
            CameraDeviceImpl.this.mInError = true;
            long ident = Binder.clearCallingIdentity();
            try {
                CameraDeviceImpl.this.mDeviceExecutor.execute(PooledLambda.obtainRunnable($$Lambda$CameraDeviceImpl$CameraDeviceCallbacks$Sm85frAzwGZVMAKNE_gwckYXVQ.INSTANCE, this, Integer.valueOf(code)).recycleOnUse());
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

        @Override // android.hardware.camera2.ICameraDeviceCallbacks
        public void onRepeatingRequestError(long lastFrameNumber, int repeatingRequestId) {
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    if (CameraDeviceImpl.this.mRepeatingRequestId != -1) {
                        CameraDeviceImpl.this.checkEarlyTriggerSequenceComplete(CameraDeviceImpl.this.mRepeatingRequestId, lastFrameNumber, CameraDeviceImpl.this.mRepeatingRequestTypes);
                        if (CameraDeviceImpl.this.mRepeatingRequestId == repeatingRequestId) {
                            CameraDeviceImpl.this.mRepeatingRequestId = -1;
                            CameraDeviceImpl.this.mRepeatingRequestTypes = null;
                        }
                    }
                }
            }
        }

        @Override // android.hardware.camera2.ICameraDeviceCallbacks
        public void onDeviceIdle() {
            Log.i(CameraDeviceImpl.this.TAG, "Camera now idle");
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    if (!CameraDeviceImpl.this.mIdle) {
                        long ident = Binder.clearCallingIdentity();
                        try {
                            CameraDeviceImpl.this.mDeviceExecutor.execute(CameraDeviceImpl.this.mCallOnIdle);
                        } finally {
                            Binder.restoreCallingIdentity(ident);
                        }
                    }
                    CameraDeviceImpl.this.mIdle = true;
                }
            }
        }

        @Override // android.hardware.camera2.ICameraDeviceCallbacks
        public void onCaptureStarted(final CaptureResultExtras resultExtras, final long timestamp) {
            int requestId = resultExtras.getRequestId();
            final long frameNumber = resultExtras.getFrameNumber();
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    final CaptureCallbackHolder holder = (CaptureCallbackHolder) CameraDeviceImpl.this.mCaptureCallbackMap.get(requestId);
                    if (holder != null) {
                        if (!CameraDeviceImpl.this.isClosed()) {
                            long ident = Binder.clearCallingIdentity();
                            try {
                                holder.getExecutor().execute(new Runnable() {
                                    /* class android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.AnonymousClass1 */

                                    @Override // java.lang.Runnable
                                    public void run() {
                                        if (!CameraDeviceImpl.this.isClosed()) {
                                            int subsequenceId = resultExtras.getSubsequenceId();
                                            CaptureRequest request = holder.getRequest(subsequenceId);
                                            if (holder.hasBatchedOutputs()) {
                                                Range<Integer> fpsRange = (Range) request.get(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE);
                                                for (int i = 0; i < holder.getRequestCount(); i++) {
                                                    holder.getCallback().onCaptureStarted(CameraDeviceImpl.this, holder.getRequest(i), timestamp - ((((long) (subsequenceId - i)) * 1000000000) / ((long) fpsRange.getUpper().intValue())), frameNumber - ((long) (subsequenceId - i)));
                                                }
                                                return;
                                            }
                                            holder.getCallback().onCaptureStarted(CameraDeviceImpl.this, holder.getRequest(resultExtras.getSubsequenceId()), timestamp, frameNumber);
                                        }
                                    }
                                });
                            } finally {
                                Binder.restoreCallingIdentity(ident);
                            }
                        }
                    }
                }
            }
        }

        /* JADX INFO: finally extract failed */
        @Override // android.hardware.camera2.ICameraDeviceCallbacks
        public void onResultReceived(CameraMetadataNative result, final CaptureResultExtras resultExtras, PhysicalCaptureResultInfo[] physicalResults) throws RemoteException {
            Object obj;
            Throwable th;
            final CameraMetadataNative resultCopy;
            long frameNumber;
            final CaptureCallbackHolder holder;
            TotalCaptureResult finalResult;
            AnonymousClass2 resultDispatch;
            final TotalCaptureResult resultAsCapture;
            int requestId = resultExtras.getRequestId();
            long frameNumber2 = resultExtras.getFrameNumber();
            Object obj2 = CameraDeviceImpl.this.mInterfaceLock;
            synchronized (obj2) {
                try {
                    if (CameraDeviceImpl.this.mRemoteDevice == null) {
                        try {
                        } catch (Throwable th2) {
                            th = th2;
                            obj = obj2;
                            throw th;
                        }
                    } else {
                        result.set((CameraCharacteristics.Key<CameraCharacteristics.Key<Size>>) CameraCharacteristics.LENS_INFO_SHADING_MAP_SIZE, (CameraCharacteristics.Key<Size>) ((Size) CameraDeviceImpl.this.getCharacteristics().get(CameraCharacteristics.LENS_INFO_SHADING_MAP_SIZE)));
                        final CaptureCallbackHolder holder2 = (CaptureCallbackHolder) CameraDeviceImpl.this.mCaptureCallbackMap.get(requestId);
                        final CaptureRequest request = holder2.getRequest(resultExtras.getSubsequenceId());
                        boolean isPartialResult = resultExtras.getPartialResultCount() < CameraDeviceImpl.this.mTotalPartialCount;
                        int requestType = request.getRequestType();
                        if (CameraDeviceImpl.this.isClosed()) {
                            CameraDeviceImpl.this.mFrameNumberTracker.updateTracker(frameNumber2, null, isPartialResult, requestType);
                            return;
                        }
                        if (holder2.hasBatchedOutputs()) {
                            resultCopy = new CameraMetadataNative(result);
                        } else {
                            resultCopy = null;
                        }
                        if (isPartialResult) {
                            final CaptureResult resultAsCapture2 = new CaptureResult(result, request, resultExtras);
                            resultDispatch = new Runnable() {
                                /* class android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.AnonymousClass2 */

                                @Override // java.lang.Runnable
                                public void run() {
                                    if (CameraDeviceImpl.this.isClosed()) {
                                        return;
                                    }
                                    if (holder2.hasBatchedOutputs()) {
                                        for (int i = 0; i < holder2.getRequestCount(); i++) {
                                            holder2.getCallback().onCaptureProgressed(CameraDeviceImpl.this, holder2.getRequest(i), new CaptureResult(new CameraMetadataNative(resultCopy), holder2.getRequest(i), resultExtras));
                                        }
                                        return;
                                    }
                                    holder2.getCallback().onCaptureProgressed(CameraDeviceImpl.this, request, resultAsCapture2);
                                }
                            };
                            obj = obj2;
                            frameNumber = frameNumber2;
                            finalResult = resultAsCapture2;
                            holder = holder2;
                        } else {
                            final List<CaptureResult> partialResults = CameraDeviceImpl.this.mFrameNumberTracker.popPartialResults(frameNumber2);
                            final long sensorTimestamp = ((Long) result.get(CaptureResult.SENSOR_TIMESTAMP)).longValue();
                            final Range<Integer> fpsRange = (Range) request.get(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE);
                            final int subsequenceId = resultExtras.getSubsequenceId();
                            frameNumber = frameNumber2;
                            try {
                                resultAsCapture = new TotalCaptureResult(result, request, resultExtras, partialResults, holder2.getSessionId(), physicalResults);
                                holder = holder2;
                                obj = obj2;
                            } catch (Throwable th3) {
                                th = th3;
                                obj = obj2;
                                throw th;
                            }
                            try {
                                resultDispatch = new Runnable() {
                                    /* class android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.AnonymousClass3 */

                                    @Override // java.lang.Runnable
                                    public void run() {
                                        if (CameraDeviceImpl.this.isClosed()) {
                                            return;
                                        }
                                        if (holder.hasBatchedOutputs()) {
                                            for (int i = 0; i < holder.getRequestCount(); i++) {
                                                resultCopy.set((CaptureResult.Key<CaptureResult.Key<Long>>) CaptureResult.SENSOR_TIMESTAMP, (CaptureResult.Key<Long>) Long.valueOf(sensorTimestamp - ((((long) (subsequenceId - i)) * 1000000000) / ((long) ((Integer) fpsRange.getUpper()).intValue()))));
                                                holder.getCallback().onCaptureCompleted(CameraDeviceImpl.this, holder.getRequest(i), new TotalCaptureResult(new CameraMetadataNative(resultCopy), holder.getRequest(i), resultExtras, partialResults, holder.getSessionId(), new PhysicalCaptureResultInfo[0]));
                                            }
                                            return;
                                        }
                                        holder.getCallback().onCaptureCompleted(CameraDeviceImpl.this, request, resultAsCapture);
                                    }
                                };
                                finalResult = resultAsCapture;
                            } catch (Throwable th4) {
                                th = th4;
                                throw th;
                            }
                        }
                        long ident = Binder.clearCallingIdentity();
                        try {
                            holder.getExecutor().execute(resultDispatch);
                            Binder.restoreCallingIdentity(ident);
                            CameraDeviceImpl.this.mFrameNumberTracker.updateTracker(frameNumber, finalResult, isPartialResult, requestType);
                            if (!isPartialResult) {
                                CameraDeviceImpl.this.checkAndFireSequenceComplete();
                            }
                        } catch (Throwable th5) {
                            Binder.restoreCallingIdentity(ident);
                            throw th5;
                        }
                    }
                } catch (Throwable th6) {
                    th = th6;
                    obj = obj2;
                    throw th;
                }
            }
        }

        @Override // android.hardware.camera2.ICameraDeviceCallbacks
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

        @Override // android.hardware.camera2.ICameraDeviceCallbacks
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
            String errorPhysicalCameraId = resultExtras.getErrorPhysicalCameraId();
            final CaptureCallbackHolder holder = (CaptureCallbackHolder) CameraDeviceImpl.this.mCaptureCallbackMap.get(requestId);
            final CaptureRequest request = holder.getRequest(subsequenceId);
            if (errorCode == 5) {
                for (final Surface surface : ((OutputConfiguration) CameraDeviceImpl.this.mConfiguredOutputs.get(resultExtras.getErrorStreamId())).getSurfaces()) {
                    if (request.containsTarget(surface)) {
                        Runnable failureDispatch = new Runnable() {
                            /* class android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.AnonymousClass4 */

                            @Override // java.lang.Runnable
                            public void run() {
                                if (!CameraDeviceImpl.this.isClosed()) {
                                    holder.getCallback().onCaptureBufferLost(CameraDeviceImpl.this, request, surface, frameNumber);
                                }
                            }
                        };
                        long ident = Binder.clearCallingIdentity();
                        try {
                            holder.getExecutor().execute(failureDispatch);
                        } finally {
                            Binder.restoreCallingIdentity(ident);
                        }
                    }
                }
                return;
            }
            int reason = 0;
            boolean mayHaveBuffers = errorCode == 4;
            if (CameraDeviceImpl.this.mCurrentSession != null && CameraDeviceImpl.this.mCurrentSession.isAborting()) {
                reason = 1;
            }
            final CaptureFailure failure = new CaptureFailure(request, reason, mayHaveBuffers, requestId, frameNumber, errorPhysicalCameraId);
            Runnable failureDispatch2 = new Runnable() {
                /* class android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.AnonymousClass5 */

                @Override // java.lang.Runnable
                public void run() {
                    if (!CameraDeviceImpl.this.isClosed()) {
                        holder.getCallback().onCaptureFailed(CameraDeviceImpl.this, request, failure);
                    }
                }
            };
            CameraDeviceImpl.this.mFrameNumberTracker.updateTracker(frameNumber, true, request.getRequestType());
            CameraDeviceImpl.this.checkAndFireSequenceComplete();
            long ident2 = Binder.clearCallingIdentity();
            try {
                holder.getExecutor().execute(failureDispatch2);
            } finally {
                Binder.restoreCallingIdentity(ident2);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class CameraHandlerExecutor implements Executor {
        private final Handler mHandler;

        public CameraHandlerExecutor(Handler handler) {
            this.mHandler = (Handler) Preconditions.checkNotNull(handler);
        }

        @Override // java.util.concurrent.Executor
        public void execute(Runnable command) {
            this.mHandler.post(command);
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
    /* access modifiers changed from: public */
    private boolean isClosed() {
        return this.mClosing.get();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CameraCharacteristics getCharacteristics() {
        return this.mCharacteristics;
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        String str = this.TAG;
        Log.w(str, "CameraDevice " + this.mCameraId + " died unexpectedly");
        if (this.mRemoteDevice != null) {
            this.mInError = true;
            Runnable r = new Runnable() {
                /* class android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass11 */

                @Override // java.lang.Runnable
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
