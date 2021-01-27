package android.os;

public final class UidProto {
    public static final long AGGREGATED_WAKELOCK = 1146756268056L;
    public static final long AUDIO = 1146756268040L;
    public static final long BLUETOOTH_CONTROLLER = 1146756268035L;
    public static final long BLUETOOTH_MISC = 1146756268038L;
    public static final long CAMERA = 1146756268041L;
    public static final long CPU = 1146756268039L;
    public static final long FLASHLIGHT = 1146756268042L;
    public static final long FOREGROUND_ACTIVITY = 1146756268043L;
    public static final long FOREGROUND_SERVICE = 1146756268044L;
    public static final long JOBS = 2246267895823L;
    public static final long JOB_COMPLETION = 2246267895824L;
    public static final long MODEM_CONTROLLER = 1146756268036L;
    public static final long NETWORK = 1146756268049L;
    public static final long PACKAGES = 2246267895810L;
    public static final long POWER_USE_ITEM = 1146756268050L;
    public static final long PROCESS = 2246267895827L;
    public static final long SENSORS = 2246267895829L;
    public static final long STATES = 2246267895828L;
    public static final long SYNCS = 2246267895830L;
    public static final long UID = 1120986464257L;
    public static final long USER_ACTIVITY = 2246267895831L;
    public static final long VIBRATOR = 1146756268045L;
    public static final long VIDEO = 1146756268046L;
    public static final long WAKELOCKS = 2246267895833L;
    public static final long WAKEUP_ALARM = 2246267895834L;
    public static final long WIFI = 1146756268059L;
    public static final long WIFI_CONTROLLER = 1146756268037L;
    public static final long WIFI_MULTICAST_WAKELOCK = 1146756268060L;

    public final class Package {
        public static final long NAME = 1138166333441L;
        public static final long SERVICES = 2246267895810L;

        public Package() {
        }

        public final class Service {
            public static final long LAUNCH_COUNT = 1120986464260L;
            public static final long NAME = 1138166333441L;
            public static final long START_COUNT = 1120986464259L;
            public static final long START_DURATION_MS = 1112396529666L;

            public Service() {
            }
        }
    }

    public final class BluetoothMisc {
        public static final long APPORTIONED_BLE_SCAN = 1146756268033L;
        public static final long BACKGROUND_BLE_SCAN = 1146756268034L;
        public static final long BACKGROUND_BLE_SCAN_RESULT_COUNT = 1120986464262L;
        public static final long BACKGROUND_UNOPTIMIZED_BLE_SCAN = 1146756268036L;
        public static final long BLE_SCAN_RESULT_COUNT = 1120986464261L;
        public static final long UNOPTIMIZED_BLE_SCAN = 1146756268035L;

        public BluetoothMisc() {
        }
    }

    public final class Cpu {
        public static final int BACKGROUND = 3;
        public static final long BY_FREQUENCY = 2246267895811L;
        public static final long BY_PROCESS_STATE = 2246267895812L;
        public static final int CACHED = 6;
        public static final int FOREGROUND = 2;
        public static final int FOREGROUND_SERVICE = 1;
        public static final int HEAVY_WEIGHT = 5;
        public static final long SYSTEM_DURATION_MS = 1112396529666L;
        public static final int TOP = 0;
        public static final int TOP_SLEEPING = 4;
        public static final long USER_DURATION_MS = 1112396529665L;

        public Cpu() {
        }

        public final class ByFrequency {
            public static final long FREQUENCY_INDEX = 1120986464257L;
            public static final long SCREEN_OFF_DURATION_MS = 1112396529667L;
            public static final long TOTAL_DURATION_MS = 1112396529666L;

            public ByFrequency() {
            }
        }

        public final class ByProcessState {
            public static final long BY_FREQUENCY = 2246267895810L;
            public static final long PROCESS_STATE = 1159641169921L;

            public ByProcessState() {
            }
        }
    }

    public final class Job {
        public static final long BACKGROUND = 1146756268035L;
        public static final long NAME = 1138166333441L;
        public static final long TOTAL = 1146756268034L;

        public Job() {
        }
    }

    public final class JobCompletion {
        public static final long NAME = 1138166333441L;
        public static final long REASON_COUNT = 2246267895810L;

        public JobCompletion() {
        }

        public final class ReasonCount {
            public static final long COUNT = 1120986464258L;
            public static final long NAME = 1159641169921L;

            public ReasonCount() {
            }
        }
    }

    public final class Network {
        public static final long BT_BYTES_RX = 1112396529669L;
        public static final long BT_BYTES_TX = 1112396529670L;
        public static final long MOBILE_ACTIVE_COUNT = 1120986464268L;
        public static final long MOBILE_ACTIVE_DURATION_MS = 1112396529675L;
        public static final long MOBILE_BYTES_BG_RX = 1112396529679L;
        public static final long MOBILE_BYTES_BG_TX = 1112396529680L;
        public static final long MOBILE_BYTES_RX = 1112396529665L;
        public static final long MOBILE_BYTES_TX = 1112396529666L;
        public static final long MOBILE_PACKETS_BG_RX = 1112396529683L;
        public static final long MOBILE_PACKETS_BG_TX = 1112396529684L;
        public static final long MOBILE_PACKETS_RX = 1112396529671L;
        public static final long MOBILE_PACKETS_TX = 1112396529672L;
        public static final long MOBILE_WAKEUP_COUNT = 1120986464269L;
        public static final long WIFI_BYTES_BG_RX = 1112396529681L;
        public static final long WIFI_BYTES_BG_TX = 1112396529682L;
        public static final long WIFI_BYTES_RX = 1112396529667L;
        public static final long WIFI_BYTES_TX = 1112396529668L;
        public static final long WIFI_PACKETS_BG_RX = 1112396529685L;
        public static final long WIFI_PACKETS_BG_TX = 1112396529686L;
        public static final long WIFI_PACKETS_RX = 1112396529673L;
        public static final long WIFI_PACKETS_TX = 1112396529674L;
        public static final long WIFI_WAKEUP_COUNT = 1120986464270L;

        public Network() {
        }
    }

    public final class PowerUseItem {
        public static final long COMPUTED_POWER_MAH = 1103806595073L;
        public static final long PROPORTIONAL_SMEAR_MAH = 1103806595076L;
        public static final long SCREEN_POWER_MAH = 1103806595075L;
        public static final long SHOULD_HIDE = 1133871366146L;

        public PowerUseItem() {
        }
    }

    public final class Process {
        public static final long ANR_COUNT = 1120986464262L;
        public static final long CRASH_COUNT = 1120986464263L;
        public static final long FOREGROUND_DURATION_MS = 1112396529668L;
        public static final long NAME = 1138166333441L;
        public static final long START_COUNT = 1120986464261L;
        public static final long SYSTEM_DURATION_MS = 1112396529667L;
        public static final long USER_DURATION_MS = 1112396529666L;

        public Process() {
        }
    }

    public final class StateTime {
        public static final long DURATION_MS = 1112396529666L;
        public static final int PROCESS_STATE_BACKGROUND = 3;
        public static final int PROCESS_STATE_CACHED = 6;
        public static final int PROCESS_STATE_FOREGROUND = 2;
        public static final int PROCESS_STATE_FOREGROUND_SERVICE = 1;
        public static final int PROCESS_STATE_HEAVY_WEIGHT = 5;
        public static final int PROCESS_STATE_TOP = 0;
        public static final int PROCESS_STATE_TOP_SLEEPING = 4;
        public static final long STATE = 1159641169921L;

        public StateTime() {
        }
    }

    public final class Sensor {
        public static final long APPORTIONED = 1146756268034L;
        public static final long BACKGROUND = 1146756268035L;
        public static final long ID = 1120986464257L;

        public Sensor() {
        }
    }

    public final class Sync {
        public static final long BACKGROUND = 1146756268035L;
        public static final long NAME = 1138166333441L;
        public static final long TOTAL = 1146756268034L;

        public Sync() {
        }
    }

    public final class UserActivity {
        public static final long COUNT = 1120986464258L;
        public static final long NAME = 1159641169921L;

        public UserActivity() {
        }
    }

    public final class AggregatedWakelock {
        public static final long BACKGROUND_PARTIAL_DURATION_MS = 1112396529666L;
        public static final long PARTIAL_DURATION_MS = 1112396529665L;

        public AggregatedWakelock() {
        }
    }

    public final class Wakelock {
        public static final long BACKGROUND_PARTIAL = 1146756268036L;
        public static final long FULL = 1146756268034L;
        public static final long NAME = 1138166333441L;
        public static final long PARTIAL = 1146756268035L;
        public static final long WINDOW = 1146756268037L;

        public Wakelock() {
        }
    }

    public final class WakeupAlarm {
        public static final long COUNT = 1120986464258L;
        public static final long NAME = 1138166333441L;

        public WakeupAlarm() {
        }
    }

    public final class Wifi {
        public static final long APPORTIONED_SCAN = 1146756268035L;
        public static final long BACKGROUND_SCAN = 1146756268036L;
        public static final long FULL_WIFI_LOCK_DURATION_MS = 1112396529665L;
        public static final long RUNNING_DURATION_MS = 1112396529666L;

        public Wifi() {
        }
    }
}
