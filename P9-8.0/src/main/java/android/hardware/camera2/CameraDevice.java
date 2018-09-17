package android.hardware.camera2;

import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.params.InputConfiguration;
import android.hardware.camera2.params.OutputConfiguration;
import android.os.Handler;
import android.view.Surface;
import java.util.List;

public abstract class CameraDevice implements AutoCloseable {
    public static final int SESSION_OPERATION_MODE_CONSTRAINED_HIGH_SPEED = 1;
    public static final int SESSION_OPERATION_MODE_NORMAL = 0;
    public static final int SESSION_OPERATION_MODE_VENDOR_START = 32768;
    public static final int TEMPLATE_MANUAL = 6;
    public static final int TEMPLATE_PREVIEW = 1;
    public static final int TEMPLATE_RECORD = 3;
    public static final int TEMPLATE_STILL_CAPTURE = 2;
    public static final int TEMPLATE_VIDEO_SNAPSHOT = 4;
    public static final int TEMPLATE_ZERO_SHUTTER_LAG = 5;

    public static abstract class StateCallback {
        public static final int ERROR_CAMERA_DEVICE = 4;
        public static final int ERROR_CAMERA_DISABLED = 3;
        public static final int ERROR_CAMERA_IN_USE = 1;
        public static final int ERROR_CAMERA_SERVICE = 5;
        public static final int ERROR_MAX_CAMERAS_IN_USE = 2;

        public abstract void onDisconnected(CameraDevice cameraDevice);

        public abstract void onError(CameraDevice cameraDevice, int i);

        public abstract void onOpened(CameraDevice cameraDevice);

        public void onClosed(CameraDevice camera) {
        }
    }

    public abstract void close();

    public abstract Builder createCaptureRequest(int i) throws CameraAccessException;

    public abstract void createCaptureSession(List<Surface> list, android.hardware.camera2.CameraCaptureSession.StateCallback stateCallback, Handler handler) throws CameraAccessException;

    public abstract void createCaptureSessionByOutputConfigurations(List<OutputConfiguration> list, android.hardware.camera2.CameraCaptureSession.StateCallback stateCallback, Handler handler) throws CameraAccessException;

    public abstract void createConstrainedHighSpeedCaptureSession(List<Surface> list, android.hardware.camera2.CameraCaptureSession.StateCallback stateCallback, Handler handler) throws CameraAccessException;

    public abstract void createCustomCaptureSession(InputConfiguration inputConfiguration, List<OutputConfiguration> list, int i, android.hardware.camera2.CameraCaptureSession.StateCallback stateCallback, Handler handler) throws CameraAccessException;

    public abstract Builder createReprocessCaptureRequest(TotalCaptureResult totalCaptureResult) throws CameraAccessException;

    public abstract void createReprocessableCaptureSession(InputConfiguration inputConfiguration, List<Surface> list, android.hardware.camera2.CameraCaptureSession.StateCallback stateCallback, Handler handler) throws CameraAccessException;

    public abstract void createReprocessableCaptureSessionByConfigurations(InputConfiguration inputConfiguration, List<OutputConfiguration> list, android.hardware.camera2.CameraCaptureSession.StateCallback stateCallback, Handler handler) throws CameraAccessException;

    public abstract String getId();
}
