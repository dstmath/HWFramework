package android.hardware.camera2;

import android.hardware.camera2.params.OutputConfiguration;
import android.os.Handler;
import android.view.Surface;
import java.util.List;

public abstract class CameraCaptureSession implements AutoCloseable {
    public static final int SESSION_ID_NONE = -1;

    public static abstract class CaptureCallback {
        public static final int NO_FRAMES_CAPTURED = -1;

        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
        }

        public void onCapturePartial(CameraCaptureSession session, CaptureRequest request, CaptureResult result) {
        }

        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
        }

        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
        }

        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
        }

        public void onCaptureSequenceCompleted(CameraCaptureSession session, int sequenceId, long frameNumber) {
        }

        public void onCaptureSequenceAborted(CameraCaptureSession session, int sequenceId) {
        }

        public void onCaptureBufferLost(CameraCaptureSession session, CaptureRequest request, Surface target, long frameNumber) {
        }
    }

    public static abstract class StateCallback {
        public abstract void onConfigureFailed(CameraCaptureSession cameraCaptureSession);

        public abstract void onConfigured(CameraCaptureSession cameraCaptureSession);

        public void onReady(CameraCaptureSession session) {
        }

        public void onActive(CameraCaptureSession session) {
        }

        public void onCaptureQueueEmpty(CameraCaptureSession session) {
        }

        public void onClosed(CameraCaptureSession session) {
        }

        public void onSurfacePrepared(CameraCaptureSession session, Surface surface) {
        }
    }

    public abstract void abortCaptures() throws CameraAccessException;

    public abstract int capture(CaptureRequest captureRequest, CaptureCallback captureCallback, Handler handler) throws CameraAccessException;

    public abstract int captureBurst(List<CaptureRequest> list, CaptureCallback captureCallback, Handler handler) throws CameraAccessException;

    public abstract void close();

    public abstract void finalizeOutputConfigurations(List<OutputConfiguration> list) throws CameraAccessException;

    public abstract CameraDevice getDevice();

    public abstract Surface getInputSurface();

    public abstract boolean isReprocessable();

    public abstract void prepare(int i, Surface surface) throws CameraAccessException;

    public abstract void prepare(Surface surface) throws CameraAccessException;

    public abstract int setRepeatingBurst(List<CaptureRequest> list, CaptureCallback captureCallback, Handler handler) throws CameraAccessException;

    public abstract int setRepeatingRequest(CaptureRequest captureRequest, CaptureCallback captureCallback, Handler handler) throws CameraAccessException;

    public abstract void stopRepeating() throws CameraAccessException;

    public abstract void tearDown(Surface surface) throws CameraAccessException;
}
