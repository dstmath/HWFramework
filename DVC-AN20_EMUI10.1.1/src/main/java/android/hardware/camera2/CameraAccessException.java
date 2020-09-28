package android.hardware.camera2;

import android.util.AndroidException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CameraAccessException extends AndroidException {
    public static final int CAMERA_DEPRECATED_HAL = 1000;
    public static final int CAMERA_DISABLED = 1;
    public static final int CAMERA_DISCONNECTED = 2;
    public static final int CAMERA_ERROR = 3;
    public static final int CAMERA_IN_USE = 4;
    public static final int MAX_CAMERAS_IN_USE = 5;
    private static final long serialVersionUID = 5630338637471475675L;
    private final int mReason;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AccessError {
    }

    public final int getReason() {
        return this.mReason;
    }

    public CameraAccessException(int problem) {
        super(getDefaultMessage(problem));
        this.mReason = problem;
    }

    public CameraAccessException(int problem, String message) {
        super(getCombinedMessage(problem, message));
        this.mReason = problem;
    }

    public CameraAccessException(int problem, String message, Throwable cause) {
        super(getCombinedMessage(problem, message), cause);
        this.mReason = problem;
    }

    public CameraAccessException(int problem, Throwable cause) {
        super(getDefaultMessage(problem), cause);
        this.mReason = problem;
    }

    public static String getDefaultMessage(int problem) {
        if (problem == 1) {
            return "The camera is disabled due to a device policy, and cannot be opened.";
        }
        if (problem == 2) {
            return "The camera device is removable and has been disconnected from the Android device, or the camera service has shut down the connection due to a higher-priority access request for the camera device.";
        }
        if (problem == 3) {
            return "The camera device is currently in the error state; no further calls to it will succeed.";
        }
        if (problem == 4) {
            return "The camera device is in use already";
        }
        if (problem != 5) {
            return null;
        }
        return "The system-wide limit for number of open cameras has been reached, and more camera devices cannot be opened until previous instances are closed.";
    }

    private static String getCombinedMessage(int problem, String message) {
        return String.format("%s (%d): %s", getProblemString(problem), Integer.valueOf(problem), message);
    }

    private static String getProblemString(int problem) {
        if (problem == 1) {
            return "CAMERA_DISABLED";
        }
        if (problem == 2) {
            return "CAMERA_DISCONNECTED";
        }
        if (problem == 3) {
            return "CAMERA_ERROR";
        }
        if (problem == 4) {
            return "CAMERA_IN_USE";
        }
        if (problem == 5) {
            return "MAX_CAMERAS_IN_USE";
        }
        if (problem != 1000) {
            return "<UNKNOWN ERROR>";
        }
        return "CAMERA_DEPRECATED_HAL";
    }
}
