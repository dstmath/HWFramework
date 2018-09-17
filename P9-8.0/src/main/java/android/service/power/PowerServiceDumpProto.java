package android.service.power;

public final class PowerServiceDumpProto {
    public static final long ACTIVE_WAKE_LOCKS = 1172526071824L;
    public static final long ARE_UIDS_CHANGED = 1155346202669L;
    public static final long ARE_UIDS_CHANGING = 1155346202668L;
    public static final long BATTERY_LEVEL = 1112396529671L;
    public static final long BATTERY_LEVEL_WHEN_DREAM_STARTED = 1112396529672L;
    public static final long CONSTANTS = 1172526071809L;
    public static final long DEVICE_IDLE_TEMP_WHITELIST = 2211908157469L;
    public static final long DEVICE_IDLE_WHITELIST = 2211908157468L;
    public static final long DIRTY = 1112396529666L;
    public static final long DOCK_STATE = 1168231104521L;
    public static final int DOCK_STATE_CAR = 2;
    public static final int DOCK_STATE_DESK = 1;
    public static final int DOCK_STATE_HE_DESK = 4;
    public static final int DOCK_STATE_LE_DESK = 3;
    public static final int DOCK_STATE_UNDOCKED = 0;
    public static final long IS_BATTERY_LEVEL_LOW = 1155346202649L;
    public static final long IS_BOOT_COMPLETED = 1155346202636L;
    public static final long IS_DEVICE_IDLE_MODE = 1155346202651L;
    public static final long IS_DISPLAY_READY = 1155346202661L;
    public static final long IS_HAL_AUTO_INTERACTIVE_MODE_ENABLED = 1155346202639L;
    public static final long IS_HAL_AUTO_SUSPEND_MODE_ENABLED = 1155346202638L;
    public static final long IS_HOLDING_DISPLAY_SUSPEND_BLOCKER = 1155346202663L;
    public static final long IS_HOLDING_WAKE_LOCK_SUSPEND_BLOCKER = 1155346202662L;
    public static final long IS_LIGHT_DEVICE_IDLE_MODE = 1155346202650L;
    public static final long IS_LOW_POWER_MODE_ENABLED = 1155346202648L;
    public static final long IS_POWERED = 1155346202629L;
    public static final long IS_PROXIMITY_POSITIVE = 1155346202635L;
    public static final long IS_REQUEST_WAIT_FOR_NEGATIVE_PROXIMITY = 1155346202645L;
    public static final long IS_SANDMAN_SCHEDULED = 1155346202646L;
    public static final long IS_SANDMAN_SUMMONED = 1155346202647L;
    public static final long IS_SCREEN_BRIGHTNESS_BOOST_IN_PROGRESS = 1155346202660L;
    public static final long IS_STAY_ON = 1155346202634L;
    public static final long IS_SYSTEM_READY = 1155346202637L;
    public static final long IS_WAKEFULNESS_CHANGING = 1155346202628L;
    public static final long LAST_INTERACTIVE_POWER_HINT_TIME_MS = 1116691496994L;
    public static final long LAST_SCREEN_BRIGHTNESS_BOOST_TIME_MS = 1116691496995L;
    public static final long LAST_SLEEP_TIME_MS = 1116691496991L;
    public static final long LAST_USER_ACTIVITY_TIME_MS = 1116691496992L;
    public static final long LAST_USER_ACTIVITY_TIME_NO_CHANGE_LIGHTS_MS = 1116691496993L;
    public static final long LAST_WAKE_TIME_MS = 1116691496990L;
    public static final long LOOPER = 1172526071855L;
    public static final long NOTIFY_LONG_DISPATCHED_MS = 1116691496978L;
    public static final long NOTIFY_LONG_NEXT_CHECK_MS = 1116691496979L;
    public static final long NOTIFY_LONG_SCHEDULED_MS = 1116691496977L;
    public static final long PLUG_TYPE = 1168231104518L;
    public static final int PLUG_TYPE_NONE = 0;
    public static final int PLUG_TYPE_PLUGGED_AC = 1;
    public static final int PLUG_TYPE_PLUGGED_USB = 2;
    public static final int PLUG_TYPE_PLUGGED_WIRELESS = 4;
    public static final long SCREEN_DIM_DURATION_MS = 1112396529707L;
    public static final long SCREEN_OFF_TIMEOUT_MS = 1112396529706L;
    public static final long SETTINGS_AND_CONFIGURATION = 1172526071848L;
    public static final long SLEEP_TIMEOUT_MS = 1129576398889L;
    public static final long SUSPEND_BLOCKERS = 2272037699633L;
    public static final long UIDS = 2272037699630L;
    public static final long USER_ACTIVITY = 1172526071828L;
    public static final long WAKEFULNESS = 1168231104515L;
    public static final int WAKEFULNESS_ASLEEP = 0;
    public static final int WAKEFULNESS_AWAKE = 1;
    public static final int WAKEFULNESS_DOZING = 3;
    public static final int WAKEFULNESS_DREAMING = 2;
    public static final int WAKEFULNESS_UNKNOWN = 4;
    public static final long WAKE_LOCKS = 2272037699632L;
    public static final long WIRELESS_CHARGER_DETECTOR = 1172526071858L;

    public final class ActiveWakeLocksProto {
        public static final long IS_BUTTON_BRIGHT = 1155346202628L;
        public static final long IS_CPU = 1155346202625L;
        public static final long IS_DOZE = 1155346202631L;
        public static final long IS_DRAW = 1155346202632L;
        public static final long IS_PROXIMITY_SCREEN_OFF = 1155346202629L;
        public static final long IS_SCREEN_BRIGHT = 1155346202626L;
        public static final long IS_SCREEN_DIM = 1155346202627L;
        public static final long IS_STAY_AWAKE = 1155346202630L;
    }

    public final class ConstantsProto {
        public static final long IS_NO_CACHED_WAKE_LOCKS = 1155346202625L;
    }

    public final class UidProto {
        public static final long IS_ACTIVE = 1155346202627L;
        public static final long IS_PROCESS_STATE_UNKNOWN = 1155346202629L;
        public static final long NUM_WAKE_LOCKS = 1112396529668L;
        public static final long PROCESS_STATE = 1168231104518L;
        public static final int PROCESS_STATE_BACKUP = 8;
        public static final int PROCESS_STATE_BOUND_FOREGROUND_SERVICE = 3;
        public static final int PROCESS_STATE_CACHED_ACTIVITY = 14;
        public static final int PROCESS_STATE_CACHED_ACTIVITY_CLIENT = 15;
        public static final int PROCESS_STATE_CACHED_EMPTY = 16;
        public static final int PROCESS_STATE_FOREGROUND_SERVICE = 4;
        public static final int PROCESS_STATE_HEAVY_WEIGHT = 9;
        public static final int PROCESS_STATE_HOME = 12;
        public static final int PROCESS_STATE_IMPORTANT_BACKGROUND = 7;
        public static final int PROCESS_STATE_IMPORTANT_FOREGROUND = 6;
        public static final int PROCESS_STATE_LAST_ACTIVITY = 13;
        public static final int PROCESS_STATE_NONEXISTENT = 17;
        public static final int PROCESS_STATE_PERSISTENT = 0;
        public static final int PROCESS_STATE_PERSISTENT_UI = 1;
        public static final int PROCESS_STATE_RECEIVER = 11;
        public static final int PROCESS_STATE_SERVICE = 10;
        public static final int PROCESS_STATE_TOP = 2;
        public static final int PROCESS_STATE_TOP_SLEEPING = 5;
        public static final long UID = 1112396529665L;
        public static final long UID_STRING = 1159641169922L;
    }

    public final class UserActivityProto {
        public static final long IS_SCREEN_BRIGHT = 1155346202625L;
        public static final long IS_SCREEN_DIM = 1155346202626L;
        public static final long IS_SCREEN_DREAM = 1155346202627L;
    }
}
