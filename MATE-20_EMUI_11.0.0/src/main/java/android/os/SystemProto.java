package android.os;

public final class SystemProto {
    public static final long BATTERY = 1146756268033L;
    public static final long BATTERY_DISCHARGE = 1146756268034L;
    public static final long CHARGE_STEP = 2246267895813L;
    public static final long CHARGE_TIME_REMAINING_MS = 1112396529667L;
    public static final long CPU_FREQUENCY = 2211908157447L;
    public static final long DATA_CONNECTION = 2246267895816L;
    public static final long DISCHARGE_STEP = 2246267895814L;
    public static final long DISCHARGE_TIME_REMAINING_MS = 1112396529668L;
    public static final long GLOBAL_BLUETOOTH_CONTROLLER = 1146756268041L;
    public static final long GLOBAL_MODEM_CONTROLLER = 1146756268042L;
    public static final long GLOBAL_NETWORK = 1146756268044L;
    public static final long GLOBAL_WIFI = 1146756268045L;
    public static final long GLOBAL_WIFI_CONTROLLER = 1146756268043L;
    public static final long KERNEL_WAKELOCK = 2246267895822L;
    public static final long MISC = 1146756268047L;
    public static final long PHONE_SIGNAL_STRENGTH = 2246267895824L;
    public static final long POWER_USE_ITEM = 2246267895825L;
    public static final long POWER_USE_SUMMARY = 1146756268050L;
    public static final long RESOURCE_POWER_MANAGER = 2246267895827L;
    public static final long SCREEN_BRIGHTNESS = 2246267895828L;
    public static final long SIGNAL_SCANNING = 1146756268053L;
    public static final long WAKEUP_REASON = 2246267895830L;
    public static final long WIFI_MULTICAST_WAKELOCK_TOTAL = 1146756268055L;
    public static final long WIFI_SIGNAL_STRENGTH = 2246267895832L;
    public static final long WIFI_STATE = 2246267895833L;
    public static final long WIFI_SUPPLICANT_STATE = 2246267895834L;

    public final class Battery {
        public static final long BATTERY_REALTIME_MS = 1112396529669L;
        public static final long BATTERY_UPTIME_MS = 1112396529670L;
        public static final long ESTIMATED_BATTERY_CAPACITY_MAH = 1112396529674L;
        public static final long MAX_LEARNED_BATTERY_CAPACITY_UAH = 1112396529676L;
        public static final long MIN_LEARNED_BATTERY_CAPACITY_UAH = 1112396529675L;
        public static final long SCREEN_DOZE_DURATION_MS = 1112396529673L;
        public static final long SCREEN_OFF_REALTIME_MS = 1112396529671L;
        public static final long SCREEN_OFF_UPTIME_MS = 1112396529672L;
        public static final long START_CLOCK_TIME_MS = 1112396529665L;
        public static final long START_COUNT = 1112396529666L;
        public static final long TOTAL_REALTIME_MS = 1112396529667L;
        public static final long TOTAL_UPTIME_MS = 1112396529668L;

        public Battery() {
        }
    }

    public final class BatteryDischarge {
        public static final long LOWER_BOUND_SINCE_CHARGE = 1120986464257L;
        public static final long SCREEN_DOZE_SINCE_CHARGE = 1120986464261L;
        public static final long SCREEN_OFF_SINCE_CHARGE = 1120986464260L;
        public static final long SCREEN_ON_SINCE_CHARGE = 1120986464259L;
        public static final long TOTAL_MAH = 1112396529670L;
        public static final long TOTAL_MAH_DEEP_DOZE = 1112396529674L;
        public static final long TOTAL_MAH_LIGHT_DOZE = 1112396529673L;
        public static final long TOTAL_MAH_SCREEN_DOZE = 1112396529672L;
        public static final long TOTAL_MAH_SCREEN_OFF = 1112396529671L;
        public static final long UPPER_BOUND_SINCE_CHARGE = 1120986464258L;

        public BatteryDischarge() {
        }
    }

    public final class BatteryLevelStep {
        public static final long DISPLAY_STATE = 1159641169923L;
        public static final int DS_DOZE = 3;
        public static final int DS_DOZE_SUSPEND = 4;
        public static final int DS_ERROR = 5;
        public static final int DS_MIXED = 0;
        public static final int DS_OFF = 2;
        public static final int DS_ON = 1;
        public static final long DURATION_MS = 1112396529665L;
        public static final long IDLE_MODE = 1159641169925L;
        public static final int IM_MIXED = 0;
        public static final int IM_OFF = 3;
        public static final int IM_ON = 2;
        public static final long LEVEL = 1120986464258L;
        public static final long POWER_SAVE_MODE = 1159641169924L;
        public static final int PSM_MIXED = 0;
        public static final int PSM_OFF = 2;
        public static final int PSM_ON = 1;

        public BatteryLevelStep() {
        }
    }

    public final class DataConnection {
        public static final long IS_NONE = 1133871366146L;
        public static final long NAME = 1159641169921L;
        public static final long TOTAL = 1146756268035L;

        public DataConnection() {
        }
    }

    public final class GlobalNetwork {
        public static final long BT_BYTES_RX = 1112396529673L;
        public static final long BT_BYTES_TX = 1112396529674L;
        public static final long MOBILE_BYTES_RX = 1112396529665L;
        public static final long MOBILE_BYTES_TX = 1112396529666L;
        public static final long MOBILE_PACKETS_RX = 1112396529669L;
        public static final long MOBILE_PACKETS_TX = 1112396529670L;
        public static final long WIFI_BYTES_RX = 1112396529667L;
        public static final long WIFI_BYTES_TX = 1112396529668L;
        public static final long WIFI_PACKETS_RX = 1112396529671L;
        public static final long WIFI_PACKETS_TX = 1112396529672L;

        public GlobalNetwork() {
        }
    }

    public final class GlobalWifi {
        public static final long ON_DURATION_MS = 1112396529665L;
        public static final long RUNNING_DURATION_MS = 1112396529666L;

        public GlobalWifi() {
        }
    }

    public final class KernelWakelock {
        public static final long NAME = 1138166333441L;
        public static final long TOTAL = 1146756268034L;

        public KernelWakelock() {
        }
    }

    public final class Misc {
        public static final long BATTERY_SAVER_MODE_ENABLED_DURATION_MS = 1112396529674L;
        public static final long DEEP_DOZE_COUNT = 1120986464269L;
        public static final long DEEP_DOZE_ENABLED_DURATION_MS = 1112396529676L;
        public static final long DEEP_DOZE_IDLING_COUNT = 1120986464271L;
        public static final long DEEP_DOZE_IDLING_DURATION_MS = 1112396529678L;
        public static final long FULL_WAKELOCK_TOTAL_DURATION_MS = 1112396529667L;
        public static final long INTERACTIVE_DURATION_MS = 1112396529673L;
        public static final long LIGHT_DOZE_COUNT = 1120986464274L;
        public static final long LIGHT_DOZE_ENABLED_DURATION_MS = 1112396529681L;
        public static final long LIGHT_DOZE_IDLING_COUNT = 1120986464276L;
        public static final long LIGHT_DOZE_IDLING_DURATION_MS = 1112396529683L;
        public static final long LONGEST_DEEP_DOZE_DURATION_MS = 1112396529680L;
        public static final long LONGEST_LIGHT_DOZE_DURATION_MS = 1112396529685L;
        public static final long MOBILE_RADIO_ACTIVE_ADJUSTED_TIME_MS = 1112396529670L;
        public static final long MOBILE_RADIO_ACTIVE_COUNT = 1120986464263L;
        public static final long MOBILE_RADIO_ACTIVE_DURATION_MS = 1112396529669L;
        public static final long MOBILE_RADIO_ACTIVE_UNKNOWN_DURATION_MS = 1120986464264L;
        public static final long NUM_CONNECTIVITY_CHANGES = 1120986464267L;
        public static final long PARTIAL_WAKELOCK_TOTAL_DURATION_MS = 1112396529668L;
        public static final long PHONE_ON_DURATION_MS = 1112396529666L;
        public static final long SCREEN_ON_DURATION_MS = 1112396529665L;

        public Misc() {
        }
    }

    public final class PhoneSignalStrength {
        public static final long NAME = 1159641169921L;
        public static final long TOTAL = 1146756268034L;

        public PhoneSignalStrength() {
        }
    }

    public final class PowerUseItem {
        public static final int AMBIENT_DISPLAY = 13;
        public static final int BLUETOOTH = 5;
        public static final int CAMERA = 11;
        public static final int CELL = 2;
        public static final long COMPUTED_POWER_MAH = 1103806595075L;
        public static final int FLASHLIGHT = 6;
        public static final int IDLE = 1;
        public static final int MEMORY = 12;
        public static final long NAME = 1159641169921L;
        public static final int OVERCOUNTED = 10;
        public static final int PHONE = 3;
        public static final long PROPORTIONAL_SMEAR_MAH = 1103806595078L;
        public static final int SCREEN = 7;
        public static final long SCREEN_POWER_MAH = 1103806595077L;
        public static final long SHOULD_HIDE = 1133871366148L;
        public static final long UID = 1120986464258L;
        public static final int UNACCOUNTED = 9;
        public static final int UNKNOWN_SIPPER = 0;
        public static final int USER = 8;
        public static final int WIFI = 4;

        public PowerUseItem() {
        }
    }

    public final class PowerUseSummary {
        public static final long BATTERY_CAPACITY_MAH = 1103806595073L;
        public static final long COMPUTED_POWER_MAH = 1103806595074L;
        public static final long MAX_DRAINED_POWER_MAH = 1103806595076L;
        public static final long MIN_DRAINED_POWER_MAH = 1103806595075L;

        public PowerUseSummary() {
        }
    }

    public final class ResourcePowerManager {
        public static final long NAME = 1138166333441L;
        public static final long SCREEN_OFF = 1146756268035L;
        public static final long TOTAL = 1146756268034L;

        public ResourcePowerManager() {
        }
    }

    public final class ScreenBrightness {
        public static final int BRIGHT = 4;
        public static final int DARK = 0;
        public static final int DIM = 1;
        public static final int LIGHT = 3;
        public static final int MEDIUM = 2;
        public static final long NAME = 1159641169921L;
        public static final long TOTAL = 1146756268034L;

        public ScreenBrightness() {
        }
    }

    public final class WakeupReason {
        public static final long NAME = 1138166333441L;
        public static final long TOTAL = 1146756268034L;

        public WakeupReason() {
        }
    }

    public final class WifiMulticastWakelockTotal {
        public static final long COUNT = 1120986464258L;
        public static final long DURATION_MS = 1112396529665L;

        public WifiMulticastWakelockTotal() {
        }
    }

    public final class WifiSignalStrength {
        public static final int GOOD = 3;
        public static final int GREAT = 4;
        public static final int MODERATE = 2;
        public static final long NAME = 1159641169921L;
        public static final int NONE = 0;
        public static final int POOR = 1;
        public static final long TOTAL = 1146756268034L;

        public WifiSignalStrength() {
        }
    }

    public final class WifiState {
        public static final long NAME = 1159641169921L;
        public static final int OFF = 0;
        public static final int OFF_SCANNING = 1;
        public static final int ON_CONNECTED_P2P = 5;
        public static final int ON_CONNECTED_STA = 4;
        public static final int ON_CONNECTED_STA_P2P = 6;
        public static final int ON_DISCONNECTED = 3;
        public static final int ON_NO_NETWORKS = 2;
        public static final int SOFT_AP = 7;
        public static final long TOTAL = 1146756268034L;

        public WifiState() {
        }
    }

    public final class WifiSupplicantState {
        public static final int ASSOCIATED = 7;
        public static final int ASSOCIATING = 6;
        public static final int AUTHENTICATING = 5;
        public static final int COMPLETED = 10;
        public static final int DISCONNECTED = 1;
        public static final int DORMANT = 11;
        public static final int FOUR_WAY_HANDSHAKE = 8;
        public static final int GROUP_HANDSHAKE = 9;
        public static final int INACTIVE = 3;
        public static final int INTERFACE_DISABLED = 2;
        public static final int INVALID = 0;
        public static final long NAME = 1159641169921L;
        public static final int SCANNING = 4;
        public static final long TOTAL = 1146756268034L;
        public static final int UNINITIALIZED = 12;

        public WifiSupplicantState() {
        }
    }
}
