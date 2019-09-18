package huawei.android.security.facerecognition;

public class FaceRecognizeEvent {
    public static final int EV_CAMERA_INTERRUPT = 6;
    public static final int EV_CANCEL_AUTH = 1;
    public static final int EV_CANCEL_ENROLL = 2;
    public static final int EV_CREATE_SESSION_RET = 4;
    public static final int EV_FR_AUTH_ACQUIRED = 11;
    public static final int EV_FR_AUTH_CANCEL = 12;
    public static final int EV_FR_AUTH_RESULT = 10;
    public static final int EV_FR_ENROLL_ACQUIRED = 8;
    public static final int EV_FR_ENROLL_CANCEL = 9;
    public static final int EV_FR_ENROLL_RESULT = 7;
    public static final int EV_FR_REMOVED = 13;
    public static final int EV_OPEN_CAMERA_RET = 3;
    public static final int EV_REPEAT_REQUEST_RET = 5;
    private long[] mArgs;
    private int mType;

    public FaceRecognizeEvent(int eventType, long... args) {
        this.mType = eventType;
        this.mArgs = args;
    }

    public int getType() {
        return this.mType;
    }

    public long[] getArgs() {
        return (long[]) this.mArgs.clone();
    }

    public String toString() {
        switch (this.mType) {
            case 1:
                return "cancel auth";
            case 2:
                return "cancel enroll";
            case 3:
                return "open camera";
            case 4:
                return "create session";
            case 5:
                return "repeat request";
            case 6:
                return "camera interrupt";
            case 7:
                return "enroll result";
            case 8:
                return "enroll acquired";
            case 9:
                return "enroll cancelled";
            case 10:
                return "auth result";
            case 11:
                return "auth acquired";
            case 12:
                return "auth cancelled";
            case 13:
                return "face removed";
            default:
                return "unknown";
        }
    }
}
