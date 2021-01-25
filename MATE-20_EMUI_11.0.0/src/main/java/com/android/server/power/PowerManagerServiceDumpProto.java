package com.android.server.power;

public final class PowerManagerServiceDumpProto {
    public static final long ACTIVE_WAKE_LOCKS = 1146756268048L;
    public static final long ARE_UIDS_CHANGED = 1133871366188L;
    public static final long ARE_UIDS_CHANGING = 1133871366187L;
    public static final long BATTERY_LEVEL = 1120986464263L;
    public static final long BATTERY_LEVEL_WHEN_DREAM_STARTED = 1120986464264L;
    public static final long BATTERY_SAVER_STATE_MACHINE = 1146756268082L;
    public static final long CONSTANTS = 1146756268033L;
    public static final long DEVICE_IDLE_TEMP_WHITELIST = 2220498092060L;
    public static final long DEVICE_IDLE_WHITELIST = 2220498092059L;
    public static final long DIRTY = 1120986464258L;
    public static final long DOCK_STATE = 1159641169929L;
    public static final long IS_BATTERY_LEVEL_LOW = 1133871366168L;
    public static final long IS_BOOT_COMPLETED = 1133871366156L;
    public static final long IS_DEVICE_IDLE_MODE = 1133871366170L;
    public static final long IS_DISPLAY_READY = 1133871366180L;
    public static final long IS_HAL_AUTO_INTERACTIVE_MODE_ENABLED = 1133871366159L;
    public static final long IS_HAL_AUTO_SUSPEND_MODE_ENABLED = 1133871366158L;
    public static final long IS_HOLDING_DISPLAY_SUSPEND_BLOCKER = 1133871366182L;
    public static final long IS_HOLDING_WAKE_LOCK_SUSPEND_BLOCKER = 1133871366181L;
    public static final long IS_LIGHT_DEVICE_IDLE_MODE = 1133871366169L;
    public static final long IS_POWERED = 1133871366149L;
    public static final long IS_PROXIMITY_POSITIVE = 1133871366155L;
    public static final long IS_REQUEST_WAIT_FOR_NEGATIVE_PROXIMITY = 1133871366165L;
    public static final long IS_SANDMAN_SCHEDULED = 1133871366166L;
    public static final long IS_SANDMAN_SUMMONED = 1133871366167L;
    public static final long IS_SCREEN_BRIGHTNESS_BOOST_IN_PROGRESS = 1133871366179L;
    public static final long IS_STAY_ON = 1133871366154L;
    public static final long IS_SYSTEM_READY = 1133871366157L;
    public static final long IS_WAKEFULNESS_CHANGING = 1133871366148L;
    public static final long LAST_INTERACTIVE_POWER_HINT_TIME_MS = 1112396529697L;
    public static final long LAST_SCREEN_BRIGHTNESS_BOOST_TIME_MS = 1112396529698L;
    public static final long LAST_SLEEP_TIME_MS = 1112396529694L;
    public static final long LAST_USER_ACTIVITY_TIME_MS = 1112396529695L;
    public static final long LAST_USER_ACTIVITY_TIME_NO_CHANGE_LIGHTS_MS = 1112396529696L;
    public static final long LAST_WAKE_TIME_MS = 1112396529693L;
    public static final long LOOPER = 1146756268078L;
    public static final long NOTIFY_LONG_DISPATCHED_MS = 1112396529682L;
    public static final long NOTIFY_LONG_NEXT_CHECK_MS = 1112396529683L;
    public static final long NOTIFY_LONG_SCHEDULED_MS = 1112396529681L;
    public static final long PLUG_TYPE = 1159641169926L;
    public static final long SCREEN_DIM_DURATION_MS = 1120986464298L;
    public static final long SCREEN_OFF_TIMEOUT_MS = 1120986464297L;
    public static final long SETTINGS_AND_CONFIGURATION = 1146756268071L;
    public static final long SLEEP_TIMEOUT_MS = 1172526071848L;
    public static final long SUSPEND_BLOCKERS = 2246267895856L;
    public static final long UID_STATES = 2246267895853L;
    public static final long USER_ACTIVITY = 1146756268052L;
    public static final long WAKEFULNESS = 1159641169923L;
    public static final long WAKE_LOCKS = 2246267895855L;
    public static final long WIRELESS_CHARGER_DETECTOR = 1146756268081L;

    public final class ConstantsProto {
        public static final long IS_NO_CACHED_WAKE_LOCKS = 1133871366145L;

        public ConstantsProto() {
        }
    }

    public final class ActiveWakeLocksProto {
        public static final long IS_BUTTON_BRIGHT = 1133871366148L;
        public static final long IS_CPU = 1133871366145L;
        public static final long IS_DOZE = 1133871366151L;
        public static final long IS_DRAW = 1133871366152L;
        public static final long IS_PROXIMITY_SCREEN_OFF = 1133871366149L;
        public static final long IS_SCREEN_BRIGHT = 1133871366146L;
        public static final long IS_SCREEN_DIM = 1133871366147L;
        public static final long IS_STAY_AWAKE = 1133871366150L;

        public ActiveWakeLocksProto() {
        }
    }

    public final class UserActivityProto {
        public static final long IS_SCREEN_BRIGHT = 1133871366145L;
        public static final long IS_SCREEN_DIM = 1133871366146L;
        public static final long IS_SCREEN_DREAM = 1133871366147L;

        public UserActivityProto() {
        }
    }

    public final class UidStateProto {
        public static final long IS_ACTIVE = 1133871366147L;
        public static final long NUM_WAKE_LOCKS = 1120986464260L;
        public static final long PROCESS_STATE = 1159641169925L;
        public static final long UID = 1120986464257L;
        public static final long UID_STRING = 1138166333442L;

        public UidStateProto() {
        }
    }
}
