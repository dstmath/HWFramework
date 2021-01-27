package com.huawei.facerecognize;

import com.huawei.android.os.SystemPropertiesEx;

public final class FaceRecognizeConstant {
    public static final int DEFAULT_ARRAYLIST_LEN = 64;
    public static final int SUPPORT_FACE_MODE = SystemPropertiesEx.getInt("ro.config.support_face_mode", 0);

    public interface FaceErrorCode {
        public static final int ALGORITHM_NOT_INIT = 5;
        public static final int BUSY = 13;
        public static final int CANCELED = 2;
        public static final int COMPARE_FAIL = 3;
        public static final int FAILED = 1;
        public static final int HAL_INVALIDE = 6;
        public static final int INVALID_PARAMETERS = 9;
        public static final int IN_LOCKOUT_MODE = 8;
        public static final int NO_FACE_DATA = 10;
        public static final int OVER_MAX_FACES = 7;
        public static final int SUCCESS = 0;
        public static final int TIMEOUT = 4;
    }

    public interface HardwareSecurityLevel {
        public static final int FACE_HARDWARE_2D_COMMON_CAMERA = 0;
        public static final int FACE_HARDWARE_2D_RGB_NPU = 7;
        public static final int FACE_HARDWARE_2D_SECURE_CAMERA = 1;
        public static final int FACE_HARDWARE_2D_SWING = 6;
        public static final int FACE_HARDWARE_2D_UNSECURE_CAMERA = 2;
        public static final int FACE_HARDWARE_3D_DUAL_CAMERA = 3;
        public static final int FACE_HARDWARE_3D_STRUCT = 4;
        public static final int FACE_HARDWARE_3D_TOF = 5;
    }
}
