package android.rms.iaware;

public final class Events {
    public static final int EVENT_BASE_APP = 5000;
    public static final int EVENT_BASE_CPU = 30000;
    public static final int EVENT_BASE_DEVSTATUS = 10000;
    public static final int EVENT_BASE_MEM = 20000;
    public static final int EVENT_BASE_TOUCH = 0;
    public static final int EVENT_FLAG_FINISH = 80000;
    public static final int EVENT_FLAG_START = 10000;
    public static final int EVENT_TYPE_MASK = -81809;

    public interface AppEvents {
        public static final int EVENT_APP_ACTION = 15018;
        public static final int EVENT_APP_ACTIVITY_BEGIN = 15005;
        public static final int EVENT_APP_ACTIVITY_DISPLAYED_BEGIN = 15013;
        public static final int EVENT_APP_ACTIVITY_DISPLAYED_FINISH = 85013;
        public static final int EVENT_APP_ACTIVITY_FINISH = 85005;
        public static final int EVENT_APP_ACTIVITY_IN = 15019;
        public static final int EVENT_APP_ACTIVITY_OUT = 85019;
        public static final int EVENT_APP_DEXOPT_FORK_NOTIFY = 15015;
        public static final int EVENT_APP_FLING_BEGIN = 15009;
        public static final int EVENT_APP_GALLERY_SCALE = 15014;
        public static final int EVENT_APP_INSTALLER_MANAGER = 15016;
        public static final int EVENT_APP_PROCESS_EXIT_BEGIN = 15003;
        public static final int EVENT_APP_PROCESS_EXIT_FINISH = 85003;
        public static final int EVENT_APP_PROCESS_LAUNCHER_BEGIN = 15001;
        public static final int EVENT_APP_PROCESS_LAUNCHER_FINISH = 85001;
        public static final int EVENT_APP_REQUEST_MEM = 15010;
        public static final int EVENT_APP_SCROLL_BEGIN = 15007;
        public static final int EVENT_APP_SCROLL_FINISH = 85007;
        public static final int EVETN_APP_CAMERA_SHOT = 15011;
        public static final int EVETN_APP_SKIPPED_FRAME = 15012;
    }

    public interface CpuEvents {
        public static final int EVENT_FULLSCREEN_CHANGE = 30000;
    }

    public interface DevStatusEvents {
        public static final int EVENT_MEDIA_BUTTON_CLICK = 20017;
        public static final int EVENT_MULTI_WINDOW = 20019;
        public static final int EVENT_SCREEN_OFF = 90011;
        public static final int EVENT_SCREEN_ON = 20011;
        public static final int EVENT_STATUS_BAR_HIDDEN = 90015;
        public static final int EVENT_STATUS_BAR_REVEALED = 20015;
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
