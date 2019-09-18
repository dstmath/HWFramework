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
    private final CameraCaptureSessionImpl mSessionImpl;

    private class WrapperCallback extends CameraCaptureSession.StateCallback {
        private final CameraCaptureSession.StateCallback mCallback;

        public WrapperCallback(CameraCaptureSession.StateCallback callback) {
            this.mCallback = callback;
        }

        public void onConfigured(CameraCaptureSession session) {
            this.mCallback.onConfigured(CameraConstrainedHighSpeedCaptureSessionImpl.this);
        }

        public void onConfigureFailed(CameraCaptureSession session) {
            this.mCallback.onConfigureFailed(CameraConstrainedHighSpeedCaptureSessionImpl.this);
        }

        public void onReady(CameraCaptureSession session) {
            this.mCallback.onReady(CameraConstrainedHighSpeedCaptureSessionImpl.this);
        }

        public void onActive(CameraCaptureSession session) {
            this.mCallback.onActive(CameraConstrainedHighSpeedCaptureSessionImpl.this);
        }

        public void onCaptureQueueEmpty(CameraCaptureSession session) {
            this.mCallback.onCaptureQueueEmpty(CameraConstrainedHighSpeedCaptureSessionImpl.this);
        }

        public void onClosed(CameraCaptureSession session) {
            this.mCallback.onClosed(CameraConstrainedHighSpeedCaptureSessionImpl.this);
        }

        public void onSurfacePrepared(CameraCaptureSession session, Surface surface) {
            this.mCallback.onSurfacePrepared(CameraConstrainedHighSpeedCaptureSessionImpl.this, surface);
        }
    }

    CameraConstrainedHighSpeedCaptureSessionImpl(int id, CameraCaptureSession.StateCallback callback, Executor stateExecutor, CameraDeviceImpl deviceImpl, Executor deviceStateExecutor, boolean configureSuccess, CameraCharacteristics characteristics) {
        this.mCharacteristics = characteristics;
        CameraCaptureSessionImpl cameraCaptureSessionImpl = new CameraCaptureSessionImpl(id, null, new WrapperCallback(callback), stateExecutor, deviceImpl, deviceStateExecutor, configureSuccess);
        this.mSessionImpl = cameraCaptureSessionImpl;
    }

    public List<CaptureRequest> createHighSpeedRequestList(CaptureRequest request) throws CameraAccessException {
        CaptureRequest captureRequest = request;
        if (captureRequest != null) {
            Collection<Surface> outputSurfaces = request.getTargets();
            Range<Integer> fpsRange = (Range) captureRequest.get(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE);
            SurfaceUtils.checkConstrainedHighSpeedSurfaces(outputSurfaces, fpsRange, (StreamConfigurationMap) this.mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP));
            int requestListSize = fpsRange.getUpper().intValue() / 30;
            List<CaptureRequest> requestList = new ArrayList<>();
            CameraMetadataNative requestMetadata = new CameraMetadataNative(request.getNativeCopy());
            CaptureRequest.Builder singleTargetRequestBuilder = new CaptureRequest.Builder(requestMetadata, false, -1, request.getLogicalCameraId(), null);
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
                CaptureRequest.Builder builder = new CaptureRequest.Builder(new CameraMetadataNative(request.getNativeCopy()), false, -1, request.getLogicalCameraId(), null);
                doubleTargetRequestBuilder = builder;
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
                CameraMetadataNative cameraMetadataNative = requestMetadata;
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

    public CameraDevice getDevice() {
        return this.mSessionImpl.getDevice();
    }

    public void prepare(Surface surface) throws CameraAccessException {
        this.mSessionImpl.prepare(surface);
    }

    public void prepare(int maxCount, Surface surface) throws CameraAccessException {
        this.mSessionImpl.prepare(maxCount, surface);
    }

    public void tearDown(Surface surface) throws CameraAccessException {
        this.mSessionImpl.tearDown(surface);
    }

    public int capture(CaptureRequest request, CameraCaptureSession.CaptureCallback listener, Handler handler) throws CameraAccessException {
        throw new UnsupportedOperationException("Constrained high speed session doesn't support this method");
    }

    public int captureSingleRequest(CaptureRequest request, Executor executor, CameraCaptureSession.CaptureCallback listener) throws CameraAccessException {
        throw new UnsupportedOperationException("Constrained high speed session doesn't support this method");
    }

    public int captureBurst(List<CaptureRequest> requests, CameraCaptureSession.CaptureCallback listener, Handler handler) throws CameraAccessException {
        if (isConstrainedHighSpeedRequestList(requests)) {
            return this.mSessionImpl.captureBurst(requests, listener, handler);
        }
        throw new IllegalArgumentException("Only request lists created by createHighSpeedRequestList() can be submitted to a constrained high speed capture session");
    }

    public int captureBurstRequests(List<CaptureRequest> requests, Executor executor, CameraCaptureSession.CaptureCallback listener) throws CameraAccessException {
        if (isConstrainedHighSpeedRequestList(requests)) {
            return this.mSessionImpl.captureBurstRequests(requests, executor, listener);
        }
        throw new IllegalArgumentException("Only request lists created by createHighSpeedRequestList() can be submitted to a constrained high speed capture session");
    }

    public int setRepeatingRequest(CaptureRequest request, CameraCaptureSession.CaptureCallback listener, Handler handler) throws CameraAccessException {
        throw new UnsupportedOperationException("Constrained high speed session doesn't support this method");
    }

    public int setSingleRepeatingRequest(CaptureRequest request, Executor executor, CameraCaptureSession.CaptureCallback listener) throws CameraAccessException {
        throw new UnsupportedOperationException("Constrained high speed session doesn't support this method");
    }

    public int setRepeatingBurst(List<CaptureRequest> requests, CameraCaptureSession.CaptureCallback listener, Handler handler) throws CameraAccessException {
        if (isConstrainedHighSpeedRequestList(requests)) {
            return this.mSessionImpl.setRepeatingBurst(requests, listener, handler);
        }
        throw new IllegalArgumentException("Only request lists created by createHighSpeedRequestList() can be submitted to a constrained high speed capture session");
    }

    public int setRepeatingBurstRequests(List<CaptureRequest> requests, Executor executor, CameraCaptureSession.CaptureCallback listener) throws CameraAccessException {
        if (isConstrainedHighSpeedRequestList(requests)) {
            return this.mSessionImpl.setRepeatingBurstRequests(requests, executor, listener);
        }
        throw new IllegalArgumentException("Only request lists created by createHighSpeedRequestList() can be submitted to a constrained high speed capture session");
    }

    public void stopRepeating() throws CameraAccessException {
        this.mSessionImpl.stopRepeating();
    }

    public void abortCaptures() throws CameraAccessException {
        this.mSessionImpl.abortCaptures();
    }

    public Surface getInputSurface() {
        return null;
    }

    public void updateOutputConfiguration(OutputConfiguration config) throws CameraAccessException {
        throw new UnsupportedOperationException("Constrained high speed session doesn't support this method");
    }

    public void close() {
        this.mSessionImpl.close();
    }

    public boolean isReprocessable() {
        return false;
    }

    public void replaceSessionClose() {
        this.mSessionImpl.replaceSessionClose();
    }

    public CameraDeviceImpl.StateCallbackKK getDeviceStateCallback() {
        return this.mSessionImpl.getDeviceStateCallback();
    }

    public boolean isAborting() {
        return this.mSessionImpl.isAborting();
    }

    public void finalizeOutputConfigurations(List<OutputConfiguration> deferredOutputConfigs) throws CameraAccessException {
        this.mSessionImpl.finalizeOutputConfigurations(deferredOutputConfigs);
    }
}
