package ohos.biometrics.enrollment;

import java.util.List;
import ohos.agp.graphics.Surface;
import ohos.biometrics.enrollment.BiometricEnroll;

public interface IFaceEnroll {

    public interface AcquireInfo {
        public static final int FACEID_ENROLL_INFO_BEGIN = 2000;
        public static final int FACEID_FACE_ANGLE_BASE = 1000;
        public static final int FACEID_FACE_DETECTED = 38;
        public static final int FACEID_FACE_MOVED = 33;
        public static final int FACEID_HAS_REGISTERED = 37;
        public static final int FACEID_NOT_GAZE = 36;
        public static final int FACEID_NOT_THE_SAME_FACE = 35;
        public static final int FACEID_OUT_OF_BOUNDS = 34;
        public static final int FACE_UNLOCK_FACE_BAD_QUALITY = 4;
        public static final int FACE_UNLOCK_FACE_BLUR = 28;
        public static final int FACE_UNLOCK_FACE_DARKLIGHT = 30;
        public static final int FACE_UNLOCK_FACE_DARKPIC = 39;
        public static final int FACE_UNLOCK_FACE_DOWN = 18;
        public static final int FACE_UNLOCK_FACE_EYE_CLOSE = 22;
        public static final int FACE_UNLOCK_FACE_EYE_OCCLUSION = 21;
        public static final int FACE_UNLOCK_FACE_HALF_SHADOW = 32;
        public static final int FACE_UNLOCK_FACE_HIGHTLIGHT = 31;
        public static final int FACE_UNLOCK_FACE_KEEP = 19;
        public static final int FACE_UNLOCK_FACE_MOUTH_OCCLUSION = 23;
        public static final int FACE_UNLOCK_FACE_MULTI = 27;
        public static final int FACE_UNLOCK_FACE_NOT_COMPLETE = 29;
        public static final int FACE_UNLOCK_FACE_NOT_FOUND = 5;
        public static final int FACE_UNLOCK_FACE_OFFSET_BOTTOM = 11;
        public static final int FACE_UNLOCK_FACE_OFFSET_LEFT = 8;
        public static final int FACE_UNLOCK_FACE_OFFSET_RIGHT = 10;
        public static final int FACE_UNLOCK_FACE_OFFSET_TOP = 9;
        public static final int FACE_UNLOCK_FACE_RISE = 16;
        public static final int FACE_UNLOCK_FACE_ROTATED_LEFT = 15;
        public static final int FACE_UNLOCK_FACE_ROTATED_RIGHT = 17;
        public static final int FACE_UNLOCK_FACE_ROTATE_BOTTOM_LEFT = 43;
        public static final int FACE_UNLOCK_FACE_ROTATE_BOTTOM_RIGHT = 42;
        public static final int FACE_UNLOCK_FACE_ROTATE_TOP_LEFT = 41;
        public static final int FACE_UNLOCK_FACE_ROTATE_TOP_RIGHT = 40;
        public static final int FACE_UNLOCK_FACE_SCALE_TOO_LARGE = 7;
        public static final int FACE_UNLOCK_FACE_SCALE_TOO_SMALL = 6;
        public static final int FACE_UNLOCK_IMAGE_BLUR = 20;
        public static final int FACE_UNLOCK_LIVENESS_FAILURE = 14;
        public static final int FACE_UNLOCK_LIVENESS_WARNING = 13;
        public static final int MG_UNLOCK_COMPARE_FAILURE = 12;
    }

    public interface FaceErrorCode {
        public static final int ALGORITHM_NOT_INIT = 5;
        public static final int BUSY = 13;
        public static final int CAMERA_FAIL = 12;
        public static final int CANCELED = 2;
        public static final int COMPARE_FAIL = 3;
        public static final int FAILED = 1;
        public static final int HAL_INVALIDE = 6;
        public static final int INVALID_PARAMETERS = 9;
        public static final int IN_LOCKOUT_MODE = 8;
        public static final int LOW_TEMP_CAP = 11;
        public static final int NO_FACE_DATA = 10;
        public static final int OVER_MAX_FACES = 7;
        public static final int SUCCESS = 0;
        public static final int TIMEOUT = 4;
        public static final int UNKNOWN = 100;
    }

    int cancelEnroll();

    int execEnroll(byte[] bArr, long j, int i, Surface surface);

    int finishEnroll();

    int getEnrolledFaceId();

    List<BiometricEnroll.EnrollInfo> getEnrolledInfo();

    BiometricEnroll.EnrolledTips getEnrolledTips();

    BiometricEnroll.EnrollInitResult initEnroll();

    int removeEnrolledInfo(int i);

    int renameEnrolledInfo(int i, String str);
}
