package android.hardware.camera2.impl;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCaptureSession.StateCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.impl.CameraDeviceImpl.StateCallbackKK;
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

public class CameraConstrainedHighSpeedCaptureSessionImpl extends CameraConstrainedHighSpeedCaptureSession implements CameraCaptureSessionCore {
    private final CameraCharacteristics mCharacteristics;
    private final CameraCaptureSessionImpl mSessionImpl;

    private class WrapperCallback extends StateCallback {
        private final StateCallback mCallback;

        public WrapperCallback(StateCallback callback) {
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

    CameraConstrainedHighSpeedCaptureSessionImpl(int id, StateCallback callback, Handler stateHandler, CameraDeviceImpl deviceImpl, Handler deviceStateHandler, boolean configureSuccess, CameraCharacteristics characteristics) {
        this.mCharacteristics = characteristics;
        this.mSessionImpl = new CameraCaptureSessionImpl(id, null, new WrapperCallback(callback), stateHandler, deviceImpl, deviceStateHandler, configureSuccess);
    }

    public List<CaptureRequest> createHighSpeedRequestList(CaptureRequest request) throws CameraAccessException {
        if (request == null) {
            throw new IllegalArgumentException("Input capture request must not be null");
        }
        Collection<Surface> outputSurfaces = request.getTargets();
        Range<Integer> fpsRange = (Range) request.get(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE);
        SurfaceUtils.checkConstrainedHighSpeedSurfaces(outputSurfaces, fpsRange, (StreamConfigurationMap) this.mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP));
        int requestListSize = ((Integer) fpsRange.getUpper()).intValue() / 30;
        List<CaptureRequest> requestList = new ArrayList();
        Builder singleTargetRequestBuilder = new Builder(new CameraMetadataNative(request.getNativeCopy()), false, -1);
        Iterator<Surface> iterator = outputSurfaces.iterator();
        Surface firstSurface = (Surface) iterator.next();
        if (outputSurfaces.size() == 1 && SurfaceUtils.isSurfaceForHwVideoEncoder(firstSurface)) {
            singleTargetRequestBuilder.set(CaptureRequest.CONTROL_CAPTURE_INTENT, Integer.valueOf(1));
        } else {
            singleTargetRequestBuilder.set(CaptureRequest.CONTROL_CAPTURE_INTENT, Integer.valueOf(3));
        }
        singleTargetRequestBuilder.setPartOfCHSRequestList(true);
        Builder doubleTargetRequestBuilder = null;
        if (outputSurfaces.size() == 2) {
            doubleTargetRequestBuilder = new Builder(new CameraMetadataNative(request.getNativeCopy()), false, -1);
            doubleTargetRequestBuilder.set(CaptureRequest.CONTROL_CAPTURE_INTENT, Integer.valueOf(3));
            doubleTargetRequestBuilder.addTarget(firstSurface);
            Surface secondSurface = (Surface) iterator.next();
            doubleTargetRequestBuilder.addTarget(secondSurface);
            doubleTargetRequestBuilder.setPartOfCHSRequestList(true);
            Surface recordingSurface = firstSurface;
            if (!SurfaceUtils.isSurfaceForHwVideoEncoder(firstSurface)) {
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

    public int capture(CaptureRequest request, CaptureCallback listener, Handler handler) throws CameraAccessException {
        throw new UnsupportedOperationException("Constrained high speed session doesn't support this method");
    }

    public int captureBurst(List<CaptureRequest> requests, CaptureCallback listener, Handler handler) throws CameraAccessException {
        if (isConstrainedHighSpeedRequestList(requests)) {
            return this.mSessionImpl.captureBurst(requests, listener, handler);
        }
        throw new IllegalArgumentException("Only request lists created by createHighSpeedRequestList() can be submitted to a constrained high speed capture session");
    }

    public int setRepeatingRequest(CaptureRequest request, CaptureCallback listener, Handler handler) throws CameraAccessException {
        throw new UnsupportedOperationException("Constrained high speed session doesn't support this method");
    }

    public int setRepeatingBurst(List<CaptureRequest> requests, CaptureCallback listener, Handler handler) throws CameraAccessException {
        if (isConstrainedHighSpeedRequestList(requests)) {
            return this.mSessionImpl.setRepeatingBurst(requests, listener, handler);
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

    public void close() {
        this.mSessionImpl.close();
    }

    public boolean isReprocessable() {
        return false;
    }

    public void replaceSessionClose() {
        this.mSessionImpl.replaceSessionClose();
    }

    public StateCallbackKK getDeviceStateCallback() {
        return this.mSessionImpl.getDeviceStateCallback();
    }

    public boolean isAborting() {
        return this.mSessionImpl.isAborting();
    }

    public void finalizeOutputConfigurations(List<OutputConfiguration> deferredOutputConfigs) throws CameraAccessException {
        this.mSessionImpl.finalizeOutputConfigurations(deferredOutputConfigs);
    }
}
