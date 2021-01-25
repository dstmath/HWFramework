package ohos.biometrics.authentication;

public interface IFaceAuthentication {

    public static class FaceAuthResultCode {
        public static final int FACE_AUTH_NOT_SUPPORT = -1;
        public static final int FACE_AUTH_RESULT_BUSY = 5;
        public static final int FACE_AUTH_RESULT_CAMERA_FAIL = 4;
        public static final int FACE_AUTH_RESULT_CANCELED = 2;
        public static final int FACE_AUTH_RESULT_COMPARE_FAILURE = 1;
        public static final int FACE_AUTH_RESULT_GENERAL_ERROR = 100;
        public static final int FACE_AUTH_RESULT_INVALID_PARAMETERS = 6;
        public static final int FACE_AUTH_RESULT_LOCKED = 7;
        public static final int FACE_AUTH_RESULT_NOT_ENROLLED = 8;
        public static final int FACE_AUTH_RESULT_SUCESS = 0;
        public static final int FACE_AUTH_RESULT_TIMEOUT = 3;
    }

    public static class FaceAuthTipsCode {
        public static final int FACE_AUTH_TIP_NOT_DETECTED = 11;
        public static final int FACE_AUTH_TIP_POOR_GAZE = 10;
        public static final int FACE_AUTH_TIP_TOO_BRIGHT = 1;
        public static final int FACE_AUTH_TIP_TOO_CLOSE = 3;
        public static final int FACE_AUTH_TIP_TOO_DARK = 2;
        public static final int FACE_AUTH_TIP_TOO_FAR = 4;
        public static final int FACE_AUTH_TIP_TOO_HIGH = 5;
        public static final int FACE_AUTH_TIP_TOO_LEFT = 8;
        public static final int FACE_AUTH_TIP_TOO_LOW = 6;
        public static final int FACE_AUTH_TIP_TOO_MUCH_MOTION = 9;
        public static final int FACE_AUTH_TIP_TOO_RIGHT = 7;
    }
}
