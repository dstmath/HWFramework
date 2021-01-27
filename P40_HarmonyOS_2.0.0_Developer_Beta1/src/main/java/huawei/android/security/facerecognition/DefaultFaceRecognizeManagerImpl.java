package huawei.android.security.facerecognition;

import android.content.Context;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Surface;
import com.huawei.hwpartsecurity.BuildConfig;
import java.util.HashMap;
import java.util.Locale;

public class DefaultFaceRecognizeManagerImpl {
    private static final HashMap<Integer, String> ACQUIRED_INFO_MAP = new HashMap<Integer, String>() {
        /* class huawei.android.security.facerecognition.DefaultFaceRecognizeManagerImpl.AnonymousClass1 */

        {
            put(4, "bad quality");
            put(5, "no face detected");
            put(6, "face too small");
            put(7, "face too large");
            put(8, "offset left");
            put(9, "offset top");
            put(10, "offset right");
            put(11, "offset bottom");
            put(13, "liveness warning");
            put(14, "liveness failure");
            put(15, "rotated left");
            put(16, "face rise to high");
            put(17, "rotated right");
            put(18, "face too low");
            put(19, "keep still");
            put(21, "eyes occlusion");
            put(22, "eyes close");
            put(23, "mouth occlusion");
            put(27, "multi faces");
            put(28, "face blur");
            put(29, "face not complete");
            put(30, "too dark");
            put(31, "too light");
            put(32, "half shadow");
            put(38, "face detected");
            put(39, "picture too dark");
            put(40, "rotate top right");
            put(41, "rotate top left");
            put(42, "rotate bottom right");
            put(43, "rotate bottom left");
            put(44, "without mask");
            put(45, "covered with mask");
            put(46, "open camera failed");
        }
    };
    public static final int CODE_CALLBACK_ACQUIRE = 3;
    public static final int CODE_CALLBACK_BUSY = 4;
    public static final int CODE_CALLBACK_CANCEL = 2;
    public static final int CODE_CALLBACK_FACEID = 6;
    public static final int CODE_CALLBACK_OUT_OF_MEM = 5;
    public static final int CODE_CALLBACK_RESULT = 1;
    public static final int FACE_MODE_SUPPORT_MASK = -1;
    public static final int FACE_MODE_SUPPORT_POS = 5;
    public static final int FACE_RECONITION_SUPPORT_MASK = 1;
    public static final int FACE_RECONITION_SUPPORT_POS = 4;
    public static final int FACE_SECURE_LEVEL_MASK = 15;
    public static final int FACE_SECURE_LEVEL_POS = 0;
    public static final int FLAG_SHEATH = 1;
    private static final String FORMATE_STR = "****%04x";
    private static final int MASK_CODE = 65535;
    public static final int REQUEST_OK = 0;
    public static final int TYPE_CALLBACK_AUTH = 2;
    public static final int TYPE_CALLBACK_ENROLL = 1;
    public static final int TYPE_CALLBACK_REMOVE = 3;

    public interface AcquireInfo {
        public static final int FACEID_ENROLL_INFO_BEGIN = 2000;
        public static final int FACEID_FACE_ANGLE_BASE = 1000;
        public static final int FACEID_FACE_DETECTED = 38;
        public static final int FACEID_FACE_MOVED = 33;
        public static final int FACEID_HAS_REGISTERED = 37;
        public static final int FACEID_NOT_GAZE = 36;
        public static final int FACEID_NOT_THE_SAME_FACE = 35;
        public static final int FACEID_OUT_OF_BOUNDS = 34;
        public static final int FACE_UNLOCK_COVERED_WITH_MASK = 45;
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
        public static final int FACE_UNLOCK_FAILURE = 3;
        public static final int FACE_UNLOCK_IMAGE_BLUR = 20;
        public static final int FACE_UNLOCK_INVALID_ARGUMENT = 1;
        public static final int FACE_UNLOCK_INVALID_HANDLE = 2;
        public static final int FACE_UNLOCK_LIVENESS_FAILURE = 14;
        public static final int FACE_UNLOCK_LIVENESS_WARNING = 13;
        public static final int FACE_UNLOCK_OK = 0;
        public static final int FACE_UNLOCK_OPEN_CAMERA_FAILED = 46;
        public static final int FACE_UNLOCK_WITHOUT_MASK = 44;
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

    public static final class FaceInfo {
        public int faceId;
        public boolean hasAlternateAppearance;
        public String name;
    }

    public static final class FaceRecognitionAbility {
        public int faceMode;
        public boolean isFaceRecognitionSupport;
        public int reserve;
        public int secureLevel;
    }

    public interface FaceRecognizeCallback {
        void onCallbackEvent(int i, int i2, int i3, int i4);
    }

    public DefaultFaceRecognizeManagerImpl(@NonNull Context context, @Nullable FaceRecognizeCallback callback) {
    }

    public int authenticate(long opId, int flags, @Nullable Surface preview) {
        return 0;
    }

    public int cancelAuthenticate(long opId) {
        return 0;
    }

    public int preparePayInfo(byte[] aaid, byte[] nonce, byte[] extra) {
        return 0;
    }

    public int getPayResult(int[] faceId, byte[] token, int[] tokenLen, byte[] reserve) {
        return 0;
    }

    public int enroll(int reqId, int flags, byte[] token, @Nullable Surface preview) {
        return 0;
    }

    public int cancelEnroll(int reqId) {
        return 0;
    }

    public long preEnroll() {
        return 0;
    }

    public int setEnrollInfo(int[] enrollInfo) {
        return 0;
    }

    public long getAuthenticatorId() {
        return 0;
    }

    public int registerSecureRegistryCallback(IBinder callback) {
        return 0;
    }

    public void postEnroll() {
    }

    public int remove(int reqId, int faceId) {
        return 0;
    }

    public int init() {
        return 0;
    }

    public int release() {
        return 0;
    }

    public int[] getEnrolledFaceIDs() {
        return new int[0];
    }

    public FaceInfo getFaceInfo(int faceId) {
        return null;
    }

    public int rename(int faceId, String name) {
        return 0;
    }

    public FaceRecognitionAbility getFaceRecognitionAbility() {
        return null;
    }

    public int getHardwareSupportType() {
        return 0;
    }

    public int getAngleDim() {
        return 0;
    }

    public void setCallback(FaceRecognizeCallback callback) {
    }

    public void resetTimeout() {
    }

    public int getRemainingNum() {
        return 0;
    }

    public long getRemainingTime() {
        return 0;
    }

    public int getTotalAuthFailedTimes() {
        return 0;
    }

    public static String getTypeString(int type) {
        if (type == 1) {
            return "ENROLL";
        }
        if (type == 2) {
            return "AUTH";
        }
        if (type == 3) {
            return "REMOVE";
        }
        return BuildConfig.FLAVOR + type;
    }

    public static String getCodeString(int code) {
        if (code == 1) {
            return "result";
        }
        if (code == 2) {
            return "cancel";
        }
        if (code == 3) {
            return "acquire";
        }
        if (code == 4) {
            return "request busy";
        }
        if (code == 6) {
            return "faceId";
        }
        return BuildConfig.FLAVOR + code;
    }

    public static String acquireInfoString(int aquireInfo) {
        if (ACQUIRED_INFO_MAP.containsKey(Integer.valueOf(aquireInfo))) {
            return ACQUIRED_INFO_MAP.get(Integer.valueOf(aquireInfo));
        }
        return BuildConfig.FLAVOR + aquireInfo;
    }

    public static String resultInfoString(int resultCode) {
        switch (resultCode) {
            case 0:
                return "success";
            case 1:
                return "failed";
            case 2:
                return "cancelled";
            case 3:
                return "compare fail";
            case 4:
                return "time out";
            case 5:
                return "invoke init first";
            case 6:
                return "hal invalid";
            case 7:
                return "over max faces";
            case 8:
                return "in lockout mode";
            case 9:
                return "invalid parameters";
            case 10:
                return "no face data";
            case 11:
                return "low temp & cap";
            case 12:
                return "camera fail";
            case 13:
                return "busy";
            default:
                return BuildConfig.FLAVOR + resultCode;
        }
    }

    private static String printFaceId(int faceId) {
        return String.format(Locale.ROOT, FORMATE_STR, Integer.valueOf(65535 & faceId));
    }

    public static String getErrorCodeString(int code, int errorCode) {
        if (code == 1) {
            return resultInfoString(errorCode);
        }
        if (code == 3) {
            return acquireInfoString(errorCode);
        }
        if (code == 6) {
            return printFaceId(errorCode);
        }
        return BuildConfig.FLAVOR + errorCode;
    }

    public int checkNeedUpgradeFeature(long reqId) {
        return 0;
    }

    public int upgradeFeature(long reqId, @NonNull byte[] authToken) {
        return 0;
    }
}
