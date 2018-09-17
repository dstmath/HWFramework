package android.rms.iaware;

public final class Events {
    public static final int EVENT_BASE_APP = 5000;
    public static final int EVENT_BASE_DEVSTATUS = 10000;
    public static final int EVENT_BASE_MEM = 20000;
    public static final int EVENT_BASE_TOUCH = 0;
    public static final int EVENT_FLAG_FINISH = 80000;
    public static final int EVENT_FLAG_START = 10000;
    public static final int EVENT_TYPE_MASK = -81809;

    public interface AppEvents {
        public static final int EVENT_APP_ACTIVITY_BEGIN = 15005;
        public static final int EVENT_APP_ACTIVITY_FINISH = 85005;
        public static final int EVENT_APP_FLING_BEGIN = 15009;
        public static final int EVENT_APP_PROCESS_EXIT_BEGIN = 15003;
        public static final int EVENT_APP_PROCESS_EXIT_FINISH = 85003;
        public static final int EVENT_APP_PROCESS_LAUNCHER_BEGIN = 15001;
        public static final int EVENT_APP_PROCESS_LAUNCHER_FINISH = 85001;
        public static final int EVENT_APP_SCROLL_BEGIN = 15007;
        public static final int EVENT_APP_SCROLL_FINISH = 85007;
    }

    public interface DevStatusEvents {
        public static final int EVENT_AB_UPDATE_BEGIN = 20013;
        public static final int EVENT_AB_UPDATE_FINISH = 90013;
        public static final int EVENT_SCREEN_OFF = 90011;
        public static final int EVENT_SCREEN_ON = 20011;
    }

    public interface InputEvents {
        public static final int EVENT_TOUCH_DOWN = 10001;
        public static final int EVENT_TOUCH_UP = 80001;
    }

    public interface MemEvents {
        public static final int EVENT_POLLING_TIMEOUT = 30002;
        public static final int EVENT_UNKNOWN = 30001;
    }
}
