package android.drm;

import android.os.Process;

public class DrmStore {

    public static class Action {
        public static final int DEFAULT = 0;
        public static final int DISPLAY = 7;
        public static final int EXECUTE = 6;
        public static final int OUTPUT = 4;
        public static final int PLAY = 1;
        public static final int PREVIEW = 5;
        public static final int RINGTONE = 2;
        public static final int TRANSFER = 3;

        static boolean isValid(int action) {
            switch (action & Process.PROC_TERM_MASK) {
                case DEFAULT /*0*/:
                case PLAY /*1*/:
                case RINGTONE /*2*/:
                case TRANSFER /*3*/:
                case OUTPUT /*4*/:
                case PREVIEW /*5*/:
                case EXECUTE /*6*/:
                case DISPLAY /*7*/:
                    return true;
                default:
                    return false;
            }
        }
    }

    public interface ConstraintsColumns {
        public static final String EXTENDED_METADATA = "extended_metadata";
        public static final String LICENSE_AVAILABLE_TIME = "license_available_time";
        public static final String LICENSE_EXPIRY_TIME = "license_expiry_time";
        public static final String LICENSE_START_TIME = "license_start_time";
        public static final String MAX_REPEAT_COUNT = "max_repeat_count";
        public static final String REMAINING_REPEAT_COUNT = "remaining_repeat_count";
    }

    public static class DrmObjectType {
        public static final int CONTENT = 1;
        public static final int RIGHTS_OBJECT = 2;
        public static final int TRIGGER_OBJECT = 3;
        public static final int UNKNOWN = 0;
    }

    public static class Playback {
        public static final int PAUSE = 2;
        public static final int RESUME = 3;
        public static final int START = 0;
        public static final int STOP = 1;

        static boolean isValid(int playbackStatus) {
            switch (playbackStatus) {
                case START /*0*/:
                case STOP /*1*/:
                case PAUSE /*2*/:
                case RESUME /*3*/:
                    return true;
                default:
                    return false;
            }
        }
    }

    public static class RightsStatus {
        public static final int RIGHTS_EXPIRED = 2;
        public static final int RIGHTS_INVALID = 1;
        public static final int RIGHTS_NOT_ACQUIRED = 3;
        public static final int RIGHTS_VALID = 0;
    }
}
