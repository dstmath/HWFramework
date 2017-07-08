package android.hardware.camera2;

import android.util.AndroidException;

public class CameraAccessException extends AndroidException {
    public static final int CAMERA_DEPRECATED_HAL = 1000;
    public static final int CAMERA_DISABLED = 1;
    public static final int CAMERA_DISCONNECTED = 2;
    public static final int CAMERA_ERROR = 3;
    public static final int CAMERA_IN_USE = 4;
    public static final int MAX_CAMERAS_IN_USE = 5;
    private static final long serialVersionUID = 5630338637471475675L;
    private final int mReason;

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
        switch (problem) {
            case CAMERA_DISABLED /*1*/:
                return "The camera is disabled due to a device policy, and cannot be opened.";
            case CAMERA_DISCONNECTED /*2*/:
                return "The camera device is removable and has been disconnected from the Android device, or the camera service has shut down the connection due to a higher-priority access request for the camera device.";
            case CAMERA_ERROR /*3*/:
                return "The camera device is currently in the error state; no further calls to it will succeed.";
            case CAMERA_IN_USE /*4*/:
                return "The camera device is in use already";
            case MAX_CAMERAS_IN_USE /*5*/:
                return "The system-wide limit for number of open cameras has been reached, and more camera devices cannot be opened until previous instances are closed.";
            default:
                return null;
        }
    }

    private static String getCombinedMessage(int problem, String message) {
        Object[] objArr = new Object[CAMERA_ERROR];
        objArr[0] = getProblemString(problem);
        objArr[CAMERA_DISABLED] = Integer.valueOf(problem);
        objArr[CAMERA_DISCONNECTED] = message;
        return String.format("%s (%d): %s", objArr);
    }

    private static String getProblemString(int problem) {
        switch (problem) {
            case CAMERA_DISABLED /*1*/:
                return "CAMERA_DISABLED";
            case CAMERA_DISCONNECTED /*2*/:
                return "CAMERA_DISCONNECTED";
            case CAMERA_ERROR /*3*/:
                return "CAMERA_ERROR";
            case CAMERA_IN_USE /*4*/:
                return "CAMERA_IN_USE";
            case MAX_CAMERAS_IN_USE /*5*/:
                return "MAX_CAMERAS_IN_USE";
            case CAMERA_DEPRECATED_HAL /*1000*/:
                return "CAMERA_DEPRECATED_HAL";
            default:
                return "<UNKNOWN ERROR>";
        }
    }
}
