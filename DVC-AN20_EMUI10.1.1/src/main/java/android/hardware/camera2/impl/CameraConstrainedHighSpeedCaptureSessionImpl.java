package android.hardware.camera2.impl;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.impl.CameraDeviceImpl;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.camera2.utils.SurfaceUtils;
import android.os.ConditionVariable;
import android.os.Handler;
import android.util.Range;
import android.view.Surface;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

public class CameraConstrainedHighSpeedCaptureSessionImpl extends CameraConstrainedHighSpeedCaptureSession implements CameraCaptureSessionCore {
    private final CameraCharacteristics mCharacteristics;
    private final ConditionVariable mInitialized = new ConditionVariable();
    private final CameraCaptureSessionImpl mSessionImpl;

    CameraConstrainedHighSpeedCaptureSessionImpl(int id, CameraCaptureSession.StateCallback callback, Executor stateExecutor, CameraDeviceImpl deviceImpl, Executor deviceStateExecutor, boolean configureSuccess, CameraCharacteristics characteristics) {
        this.mCharacteristics = characteristics;
        this.mSessionImpl = new CameraCaptureSessionImpl(id, null, new WrapperCallback(callback), stateExecutor, deviceImpl, deviceStateExecutor, configureSuccess);
        this.mInitialized.open();
    }

    @Override // android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession
    public List<CaptureRequest> createHighSpeedRequestList(CaptureRequest request) throws CameraAccessException {
        if (request != null) {
            Collection<Surface> outputSurfaces = request.getTargets();
            Range<Integer> fpsRange = (Range) request.get(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE);
            SurfaceUtils.checkConstrainedHighSpeedSurfaces(outputSurfaces, fpsRange, (StreamConfigurationMap) this.mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP));
            int requestListSize = fpsRange.getUpper().intValue() / 30;
            List<CaptureRequest> requestList = new ArrayList<>();
            CaptureRequest.Builder singleTargetRequestBuilder = new CaptureRequest.Builder(new CameraMetadataNative(request.getNativeCopy()), false, -1, request.getLogicalCameraId(), null);
            singleTargetRequestBuilder.setTag(request.getTag());
            Iterator<Surface> iterator = outputSurfaces.iterator();
            Surface firstSurface = iterator.next();
            if (outputSurfaces.size() != 1 || !SurfaceUtils.isSurfaceForHwVideoEncoder(firstSurface)) {
                singleTargetRequestBuilder.set(CaptureRequest.CONTROL_CAPTURE_INTENT, 3);
            } else {
                singleTargetRequestBuilder.set(CaptureRequest.CONTROL_CAPTURE_INTENT, 1);
            }
            singleTargetRequestBuilder.setPartOfCHSRequestList(true);
            CaptureRequest.Builder doubleTargetRequestBuilder = null;
            if (outputSurfaces.size() == 2) {
                doubleTargetRequestBuilder = new CaptureRequest.Builder(new CameraMetadataNative(request.getNativeCopy()), false, -1, request.getLogicalCameraId(), null);
                doubleTargetRequestBuilder.setTag(request.getTag());
                doubleTargetRequestBuilder.set(CaptureRequest.CONTROL_CAPTURE_INTENT, 3);
                doubleTargetRequestBuilder.addTarget(firstSurface);
                Surface secondSurface = iterator.next();
                doubleTargetRequestBuilder.addTarget(secondSurface);
                doubleTargetRequestBuilder.setPartOfCHSRequestList(true);
                Surface recordingSurface = firstSurface;
                if (!SurfaceUtils.isSurfaceForHwVideoEncoder(recordingSurface)) {
                    recordingSurface = secondSurface;
                }
                singleTargetRequestBuilder.addTarget(recordingSurface);
            } else {
                singleTargetRequestBuilder.addTarget(firstSurface);
            }
            for (int i = 0; i < requestListSize; i++) {
                if (i != 0 || doubleTargetRequestBuilder == null) {
                    requestList.add(singleTargetRequestBuilder.build());
                } else {
                    requestList.add(doubleTargetRequestBuilder.build());
                }
            }
            return Collections.unmodifiableList(requestList);
        }
        throw new IllegalArgumentException("Input capture request must not be null");
    }

    private boolean isConstrainedHighSpeedRequestList(List<CaptureRequest> requestList) {
        Preconditions.checkCollectionNotEmpty(requestList, "High speed request list");
        for (CaptureRequest request : requestList) {
            if (!request.isPartOfCRequestList()) {
                return false;
            }
        }
        return true;
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public CameraDevice getDevice() {
        return this.mSessionImpl.getDevice();
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public void prepare(Surface surface) throws CameraAccessException {
        this.mSessionImpl.prepare(surface);
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public void prepare(int maxCount, Surface surface) throws CameraAccessException {
        this.mSessionImpl.prepare(maxCount, surface);
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public void tearDown(Surface surface) throws CameraAccessException {
        this.mSessionImpl.tearDown(surface);
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public int capture(CaptureRequest request, CameraCaptureSession.CaptureCallback listener, Handler handler) throws CameraAccessException {
        throw new UnsupportedOperationException("Constrained high speed session doesn't support this method");
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public int captureSingleRequest(CaptureRequest request, Executor executor, CameraCaptureSession.CaptureCallback listener) throws CameraAccessException {
        throw new UnsupportedOperationException("Constrained high speed session doesn't support this method");
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public int captureBurst(List<CaptureRequest> requests, CameraCaptureSession.CaptureCallback listener, Handler handler) throws CameraAccessException {
        if (isConstrainedHighSpeedRequestList(requests)) {
            return this.mSessionImpl.captureBurst(requests, listener, handler);
        }
        throw new IllegalArgumentException("Only request lists created by createHighSpeedRequestList() can be submitted to a constrained high speed capture session");
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public int captureBurstRequests(List<CaptureRequest> requests, Executor executor, CameraCaptureSession.CaptureCallback listener) throws CameraAccessException {
        if (isConstrainedHighSpeedRequestList(requests)) {
            return this.mSessionImpl.captureBurstRequests(requests, executor, listener);
        }
        throw new IllegalArgumentException("Only request lists created by createHighSpeedRequestList() can be submitted to a constrained high speed capture session");
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public int setRepeatingRequest(CaptureRequest request, CameraCaptureSession.CaptureCallback listener, Handler handler) throws CameraAccessException {
        throw new UnsupportedOperationException("Constrained high speed session doesn't support this method");
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public int setSingleRepeatingRequest(CaptureRequest request, Executor executor, CameraCaptureSession.CaptureCallback listener) throws CameraAccessException {
        throw new UnsupportedOperationException("Constrained high speed session doesn't support this method");
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public int setRepeatingBurst(List<CaptureRequest> requests, CameraCaptureSession.CaptureCallback listener, Handler handler) throws CameraAccessException {
        if (isConstrainedHighSpeedRequestList(requests)) {
            return this.mSessionImpl.setRepeatingBurst(requests, listener, handler);
        }
        throw new IllegalArgumentException("Only request lists created by createHighSpeedRequestList() can be submitted to a constrained high speed capture session");
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public int setRepeatingBurstRequests(List<CaptureRequest> requests, Executor executor, CameraCaptureSession.CaptureCallback listener) throws CameraAccessException {
        if (isConstrainedHighSpeedRequestList(requests)) {
            return this.mSessionImpl.setRepeatingBurstRequests(requests, executor, listener);
        }
        throw new IllegalArgumentException("Only request lists created by createHighSpeedRequestList() can be submitted to a constrained high speed capture session");
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public void stopRepeating() throws CameraAccessException {
        this.mSessionImpl.stopRepeating();
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public void abortCaptures() throws CameraAccessException {
        this.mSessionImpl.abortCaptures();
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public Surface getInputSurface() {
        return null;
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public void updateOutputConfiguration(OutputConfiguration config) throws CameraAccessException {
        throw new UnsupportedOperationException("Constrained high speed session doesn't support this method");
    }

    @Override // android.hardware.camera2.CameraCaptureSession, java.lang.AutoCloseable
    public void close() {
        this.mSessionImpl.close();
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public boolean isReprocessable() {
        return false;
    }

    @Override // android.hardware.camera2.impl.CameraCaptureSessionCore
    public void replaceSessionClose() {
        this.mSessionImpl.replaceSessionClose();
    }

    @Override // android.hardware.camera2.impl.CameraCaptureSessionCore
    public CameraDeviceImpl.StateCallbackKK getDeviceStateCallback() {
        return this.mSessionImpl.getDeviceStateCallback();
    }

    @Override // android.hardware.camera2.impl.CameraCaptureSessionCore
    public boolean isAborting() {
        return this.mSessionImpl.isAborting();
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public void finalizeOutputConfigurations(List<OutputConfiguration> deferredOutputConfigs) throws CameraAccessException {
        this.mSessionImpl.finalizeOutputConfigurations(deferredOutputConfigs);
    }

    private class WrapperCallback extends CameraCaptureSession.StateCallback {
        private final CameraCaptureSession.StateCallback mCallback;

        public WrapperCallback(CameraCaptureSession.StateCallback callback) {
            this.mCallback = callback;
        }

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onConfigured(CameraCaptureSession session) {
            CameraConstrainedHighSpeedCaptureSessionImpl.this.mInitialized.block();
            this.mCallback.onConfigured(CameraConstrainedHighSpeedCaptureSessionImpl.this);
        }

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onConfigureFailed(CameraCaptureSession session) {
            CameraConstrainedHighSpeedCaptureSessionImpl.this.mInitialized.block();
            this.mCallback.onConfigureFailed(CameraConstrainedHighSpeedCaptureSessionImpl.this);
        }

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onReady(CameraCaptureSession session) {
            this.mCallback.onReady(CameraConstrainedHighSpeedCaptureSessionImpl.this);
        }

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onActive(CameraCaptureSession session) {
            this.mCallback.onActive(CameraConstrainedHighSpeedCaptureSessionImpl.this);
        }

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onCaptureQueueEmpty(CameraCaptureSession session) {
            this.mCallback.onCaptureQueueEmpty(CameraConstrainedHighSpeedCaptureSessionImpl.this);
        }

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onClosed(CameraCaptureSession session) {
            this.mCallback.onClosed(CameraConstrainedHighSpeedCaptureSessionImpl.this);
        }

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onSurfacePrepared(CameraCaptureSession session, Surface surface) {
            this.mCallback.onSurfacePrepared(CameraConstrainedHighSpeedCaptureSessionImpl.this, surface);
        }
    }
}
