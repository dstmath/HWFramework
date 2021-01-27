package com.android.server.am;

public final class ActivityManagerServiceDumpProcessesProto {
    public static final long ACTIVE_INSTRUMENTATIONS = 2246267895811L;
    public static final long ACTIVE_UIDS = 2246267895812L;
    public static final long ADJ_SEQ = 1120986464305L;
    public static final long ALLOW_LOWER_MEM_LEVEL = 1133871366199L;
    public static final long ALWAYS_FINISH_ACTIVITIES = 1133871366180L;
    public static final long APP_ERRORS = 1146756268045L;
    public static final long BOOTED = 1133871366185L;
    public static final long BOOTING = 1133871366187L;
    public static final long BOOT_ANIMATION_COMPLETE = 1133871366189L;
    public static final long CALL_FINISH_BOOTING = 1133871366188L;
    public static final long CONFIG_WILL_CHANGE = 1133871366165L;
    public static final long CONTROLLER = 1146756268069L;
    public static final long CURRENT_TRACKER = 1146756268063L;
    public static final long DEBUG = 1146756268062L;
    public static final long DEVICE_IDLE_TEMP_WHITELIST = 2220498092057L;
    public static final long DEVICE_IDLE_WHITELIST = 2220498092056L;
    public static final long FACTORY_TEST = 1120986464298L;
    public static final long GC_PROCS = 2246267895820L;
    public static final long GLOBAL_CONFIGURATION = 1146756268051L;
    public static final long GOING_TO_SLEEP = 1146756268079L;
    public static final long HEAVY_WEIGHT_PROC = 1146756268050L;
    public static final long HOME_PROC = 1146756268047L;
    public static final long IMPORTANT_PROCS = 2246267895816L;
    public static final long ISOLATED_PROCS = 2246267895810L;
    public static final long LAST_IDLE_TIME = 1146756268090L;
    public static final long LAST_MEMORY_LEVEL = 1120986464312L;
    public static final long LAST_NUM_PROCESSES = 1120986464313L;
    public static final long LAST_POWER_CHECK_UPTIME_MS = 1112396529710L;
    public static final long LAUNCHING_ACTIVITY = 1146756268080L;
    public static final long LOW_RAM_SINCE_LAST_IDLE_MS = 1112396529723L;
    public static final long LRU_PROCS = 1146756268038L;
    public static final long LRU_SEQ = 1120986464306L;
    public static final long MEM_WATCH_PROCESSES = 1146756268064L;
    public static final long NATIVE_DEBUGGING_APP = 1138166333475L;
    public static final long NEW_NUM_SERVICE_PROCS = 1120986464310L;
    public static final long NUM_CACHED_HIDDEN_PROCS = 1120986464308L;
    public static final long NUM_NON_CACHED_PROCS = 1120986464307L;
    public static final long NUM_SERVICE_PROCS = 1120986464309L;
    public static final long ON_HOLD_PROCS = 2246267895819L;
    public static final long PENDING_TEMP_WHITELIST = 2246267895834L;
    public static final long PERSISTENT_STARTING_PROCS = 2246267895817L;
    public static final long PIDS_SELF_LOCKED = 2246267895815L;
    public static final long PREVIOUS_PROC = 1146756268048L;
    public static final long PREVIOUS_PROC_VISIBLE_TIME_MS = 1112396529681L;
    public static final long PROCESSES_READY = 1133871366183L;
    public static final long PROCS = 2246267895809L;
    public static final long PROFILE = 1146756268066L;
    public static final long REMOVED_PROCS = 2246267895818L;
    public static final long RUNNING_VOICE = 1146756268060L;
    public static final long SCREEN_COMPAT_PACKAGES = 2246267895830L;
    public static final long SLEEP_STATUS = 1146756268059L;
    public static final long SYSTEM_READY = 1133871366184L;
    public static final long TOTAL_PERSISTENT_PROCS = 1120986464294L;
    public static final long TRACK_ALLOCATION_APP = 1138166333473L;
    public static final long UID_OBSERVERS = 2246267895831L;
    public static final long USER_CONTROLLER = 1146756268046L;
    public static final long VALIDATE_UIDS = 2246267895813L;
    public static final long VR_CONTROLLER = 1146756268061L;

    public final class LruProcesses {
        public static final long LIST = 2246267895812L;
        public static final long NON_ACT_AT = 1120986464258L;
        public static final long NON_SVC_AT = 1120986464259L;
        public static final long SIZE = 1120986464257L;

        public LruProcesses() {
        }
    }

    public final class ScreenCompatPackage {
        public static final long MODE = 1120986464258L;
        public static final long PACKAGE = 1138166333441L;

        public ScreenCompatPackage() {
        }
    }

    public final class UidObserverRegistrationProto {
        public static final long CUT_POINT = 1120986464260L;
        public static final long FLAGS = 2259152797699L;
        public static final long LAST_PROC_STATES = 2246267895813L;
        public static final long PACKAGE = 1138166333442L;
        public static final long UID = 1120986464257L;

        public UidObserverRegistrationProto() {
        }

        public final class ProcState {
            public static final long STATE = 1120986464258L;
            public static final long UID = 1120986464257L;

            public ProcState() {
            }
        }
    }

    public final class PendingTempWhitelist {
        public static final long DURATION_MS = 1112396529666L;
        public static final long TAG = 1138166333443L;
        public static final long TARGET_UID = 1120986464257L;

        public PendingTempWhitelist() {
        }
    }

    public final class SleepStatus {
        public static final long SHUTTING_DOWN = 1133871366148L;
        public static final long SLEEPING = 1133871366147L;
        public static final long SLEEP_TOKENS = 2237677961218L;
        public static final long TEST_PSS_MODE = 1133871366149L;
        public static final long WAKEFULNESS = 1159641169921L;

        public SleepStatus() {
        }
    }

    public final class Voice {
        public static final long SESSION = 1138166333441L;
        public static final long WAKELOCK = 1146756268034L;

        public Voice() {
        }
    }

    public final class DebugApp {
        public static final long DEBUG_APP = 1138166333441L;
        public static final long DEBUG_TRANSIENT = 1133871366147L;
        public static final long ORIG_DEBUG_APP = 1138166333442L;
        public static final long ORIG_WAIT_FOR_DEBUGGER = 1133871366148L;

        public DebugApp() {
        }
    }

    public final class MemWatchProcess {
        public static final long DUMP = 1146756268034L;
        public static final long PROCS = 2246267895809L;

        public MemWatchProcess() {
        }

        public final class Process {
            public static final long MEM_STATS = 2246267895810L;
            public static final long NAME = 1138166333441L;

            public Process() {
            }

            public final class MemStats {
                public static final long REPORT_TO = 1138166333443L;
                public static final long SIZE = 1138166333442L;
                public static final long UID = 1120986464257L;

                public MemStats() {
                }
            }
        }

        public final class Dump {
            public static final long FILE = 1138166333442L;
            public static final long IS_USER_INITIATED = 1133871366149L;
            public static final long PID = 1120986464259L;
            public static final long PROC_NAME = 1138166333441L;
            public static final long UID = 1120986464260L;

            public Dump() {
            }
        }
    }

    public final class Profile {
        public static final long APP_NAME = 1138166333441L;
        public static final long INFO = 1146756268035L;
        public static final long PROC = 1146756268034L;
        public static final long TYPE = 1120986464260L;

        public Profile() {
        }
    }

    public final class Controller {
        public static final long CONTROLLER = 1138166333441L;
        public static final long IS_A_MONKEY = 1133871366146L;

        public Controller() {
        }
    }
}
