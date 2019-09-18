package android.os;

import android.app.job.JobParameters;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiScanLog;
import android.net.wifi.WifiScanner;
import android.os.SystemProto;
import android.os.UidProto;
import android.provider.SettingsStringUtil;
import android.provider.Telephony;
import android.rms.AppAssociate;
import android.rms.HwSysResource;
import android.service.notification.ZenModeConfig;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionPlan;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.LongSparseArray;
import android.util.MutableBoolean;
import android.util.Pair;
import android.util.Printer;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import android.view.Menu;
import android.view.SurfaceControl;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class BatteryStats implements Parcelable {
    private static final String AGGREGATED_WAKELOCK_DATA = "awl";
    public static final int AGGREGATED_WAKE_TYPE_PARTIAL = 20;
    private static final String APK_DATA = "apk";
    private static final String AUDIO_DATA = "aud";
    public static final int AUDIO_TURNED_ON = 15;
    private static final String BATTERY_DATA = "bt";
    private static final String BATTERY_DISCHARGE_DATA = "dc";
    private static final String BATTERY_LEVEL_DATA = "lv";
    private static final int BATTERY_STATS_CHECKIN_VERSION = 9;
    private static final String BLUETOOTH_CONTROLLER_DATA = "ble";
    private static final String BLUETOOTH_MISC_DATA = "blem";
    public static final int BLUETOOTH_SCAN_ON = 19;
    public static final int BLUETOOTH_UNOPTIMIZED_SCAN_ON = 21;
    private static final long BYTES_PER_GB = 1073741824;
    private static final long BYTES_PER_KB = 1024;
    private static final long BYTES_PER_MB = 1048576;
    private static final String CAMERA_DATA = "cam";
    public static final int CAMERA_TURNED_ON = 17;
    private static final String CELLULAR_CONTROLLER_NAME = "Cellular";
    private static final String CHARGE_STEP_DATA = "csd";
    private static final String CHARGE_TIME_REMAIN_DATA = "ctr";
    static final int CHECKIN_VERSION = 32;
    private static final String CPU_DATA = "cpu";
    private static final String CPU_TIMES_AT_FREQ_DATA = "ctf";
    private static final String DATA_CONNECTION_COUNT_DATA = "dcc";
    static final String[] DATA_CONNECTION_NAMES = {"none", "gprs", "edge", "umts", "cdma", "evdo_0", "evdo_A", "1xrtt", "hsdpa", "hsupa", "hspa", "iden", "evdo_b", "lte", "ehrpd", "hspap", "gsm", "td_scdma", "iwlan", "lte_ca", "other"};
    public static final int DATA_CONNECTION_NONE = 0;
    public static final int DATA_CONNECTION_OTHER = 20;
    private static final String DATA_CONNECTION_TIME_DATA = "dct";
    public static final int DEVICE_IDLE_MODE_DEEP = 2;
    public static final int DEVICE_IDLE_MODE_LIGHT = 1;
    public static final int DEVICE_IDLE_MODE_OFF = 0;
    private static final String DISCHARGE_STEP_DATA = "dsd";
    private static final String DISCHARGE_TIME_REMAIN_DATA = "dtr";
    public static final int DUMP_CHARGED_ONLY = 2;
    public static final int DUMP_DAILY_ONLY = 4;
    public static final int DUMP_DEVICE_WIFI_ONLY = 64;
    public static final int DUMP_HISTORY_ONLY = 8;
    public static final int DUMP_INCLUDE_HISTORY = 16;
    public static final int DUMP_VERBOSE = 32;
    private static final String FLASHLIGHT_DATA = "fla";
    public static final int FLASHLIGHT_TURNED_ON = 16;
    public static final int FOREGROUND_ACTIVITY = 10;
    private static final String FOREGROUND_ACTIVITY_DATA = "fg";
    public static final int FOREGROUND_SERVICE = 22;
    private static final String FOREGROUND_SERVICE_DATA = "fgs";
    public static final int FULL_WIFI_LOCK = 5;
    private static final String GLOBAL_BLUETOOTH_CONTROLLER_DATA = "gble";
    private static final String GLOBAL_CPU_FREQ_DATA = "gcf";
    private static final String GLOBAL_MODEM_CONTROLLER_DATA = "gmcd";
    private static final String GLOBAL_NETWORK_DATA = "gn";
    private static final String GLOBAL_WIFI_CONTROLLER_DATA = "gwfcd";
    private static final String GLOBAL_WIFI_DATA = "gwfl";
    private static final String HISTORY_DATA = "h";
    public static final String[] HISTORY_EVENT_CHECKIN_NAMES = {"Enl", "Epr", "Efg", "Etp", "Esy", "Ewl", "Ejb", "Eur", "Euf", "Ecn", "Eac", "Epi", "Epu", "Eal", "Est", "Eai", "Eaa", "Etw", "Esw", "Ewa", "Elw", "Eec"};
    public static final IntToString[] HISTORY_EVENT_INT_FORMATTERS = {sUidToString, sUidToString, sUidToString, sUidToString, sUidToString, sUidToString, sUidToString, sUidToString, sUidToString, sUidToString, sUidToString, sUidToString, sUidToString, sUidToString, sUidToString, sUidToString, sUidToString, sUidToString, sUidToString, sUidToString, sUidToString, sIntToString};
    public static final String[] HISTORY_EVENT_NAMES = {"null", "proc", FOREGROUND_ACTIVITY_DATA, "top", "sync", "wake_lock_in", "job", "user", "userfg", "conn", "active", "pkginst", "pkgunin", ZenModeConfig.IS_ALARM_PATH, "stats", "pkginactive", "pkgactive", "tmpwhitelist", "screenwake", "wakeupap", "longwake", "est_capacity"};
    public static final BitDescription[] HISTORY_STATE2_DESCRIPTIONS;
    public static final BitDescription[] HISTORY_STATE_DESCRIPTIONS;
    private static final String HISTORY_STRING_POOL = "hsp";
    public static final int JOB = 14;
    private static final String JOBS_DEFERRED_DATA = "jbd";
    private static final String JOB_COMPLETION_DATA = "jbc";
    private static final String JOB_DATA = "jb";
    public static final long[] JOB_FRESHNESS_BUCKETS = {DateUtils.HOUR_IN_MILLIS, 7200000, 14400000, 28800000, SubscriptionPlan.BYTES_UNLIMITED};
    private static final String KERNEL_WAKELOCK_DATA = "kwl";
    private static final boolean LOCAL_LOGV = false;
    public static final int MAX_TRACKED_SCREEN_STATE = 4;
    private static final String MISC_DATA = "m";
    private static final String MODEM_CONTROLLER_DATA = "mcd";
    public static final int NETWORK_BT_RX_DATA = 4;
    public static final int NETWORK_BT_TX_DATA = 5;
    private static final String NETWORK_DATA = "nt";
    public static final int NETWORK_MOBILE_BG_RX_DATA = 6;
    public static final int NETWORK_MOBILE_BG_TX_DATA = 7;
    public static final int NETWORK_MOBILE_RX_DATA = 0;
    public static final int NETWORK_MOBILE_TX_DATA = 1;
    public static final int NETWORK_WIFI_BG_RX_DATA = 8;
    public static final int NETWORK_WIFI_BG_TX_DATA = 9;
    public static final int NETWORK_WIFI_RX_DATA = 2;
    public static final int NETWORK_WIFI_TX_DATA = 3;
    public static final int NUM_DATA_CONNECTION_TYPES = 21;
    public static final int NUM_NETWORK_ACTIVITY_TYPES = 10;
    public static final int NUM_SCREEN_BRIGHTNESS_BINS = 5;
    public static final int NUM_WIFI_SIGNAL_STRENGTH_BINS = 5;
    public static final int NUM_WIFI_STATES = 8;
    public static final int NUM_WIFI_SUPPL_STATES = 13;
    private static final String POWER_USE_ITEM_DATA = "pwi";
    private static final String POWER_USE_SUMMARY_DATA = "pws";
    private static final String PROCESS_DATA = "pr";
    public static final int PROCESS_STATE = 12;
    private static final String RESOURCE_POWER_MANAGER_DATA = "rpm";
    public static final String RESULT_RECEIVER_CONTROLLER_KEY = "controller_activity";
    public static final int SCREEN_BRIGHTNESS_BRIGHT = 4;
    public static final int SCREEN_BRIGHTNESS_DARK = 0;
    private static final String SCREEN_BRIGHTNESS_DATA = "br";
    public static final int SCREEN_BRIGHTNESS_DIM = 1;
    public static final int SCREEN_BRIGHTNESS_LIGHT = 3;
    public static final int SCREEN_BRIGHTNESS_MEDIUM = 2;
    static final String[] SCREEN_BRIGHTNESS_NAMES = {"dark", "dim", "medium", "light", "bright"};
    static final String[] SCREEN_BRIGHTNESS_SHORT_NAMES = {WifiEnterpriseConfig.ENGINE_DISABLE, "1", WifiScanLog.EVENT_KEY2, WifiScanLog.EVENT_KEY3, WifiScanLog.EVENT_KEY4};
    protected static final boolean SCREEN_OFF_RPM_STATS_ENABLED = false;
    public static final int SENSOR = 3;
    private static final String SENSOR_DATA = "sr";
    public static final String SERVICE_NAME = "batterystats";
    private static final String SIGNAL_SCANNING_TIME_DATA = "sst";
    private static final String SIGNAL_STRENGTH_COUNT_DATA = "sgc";
    private static final String SIGNAL_STRENGTH_TIME_DATA = "sgt";
    private static final String STATE_TIME_DATA = "st";
    public static final int STATS_CURRENT = 1;
    public static final int STATS_SINCE_CHARGED = 0;
    public static final int STATS_SINCE_UNPLUGGED = 2;
    private static final String[] STAT_NAMES = {"l", "c", "u"};
    public static final long STEP_LEVEL_INITIAL_MODE_MASK = 71776119061217280L;
    public static final int STEP_LEVEL_INITIAL_MODE_SHIFT = 48;
    public static final long STEP_LEVEL_LEVEL_MASK = 280375465082880L;
    public static final int STEP_LEVEL_LEVEL_SHIFT = 40;
    public static final int[] STEP_LEVEL_MODES_OF_INTEREST = {7, 15, 11, 7, 7, 7, 7, 7, 15, 11};
    public static final int STEP_LEVEL_MODE_DEVICE_IDLE = 8;
    public static final String[] STEP_LEVEL_MODE_LABELS = {"screen off", "screen off power save", "screen off device idle", "screen on", "screen on power save", "screen doze", "screen doze power save", "screen doze-suspend", "screen doze-suspend power save", "screen doze-suspend device idle"};
    public static final int STEP_LEVEL_MODE_POWER_SAVE = 4;
    public static final int STEP_LEVEL_MODE_SCREEN_STATE = 3;
    public static final int[] STEP_LEVEL_MODE_VALUES = {0, 4, 8, 1, 5, 2, 6, 3, 7, 11};
    public static final long STEP_LEVEL_MODIFIED_MODE_MASK = -72057594037927936L;
    public static final int STEP_LEVEL_MODIFIED_MODE_SHIFT = 56;
    public static final long STEP_LEVEL_TIME_MASK = 1099511627775L;
    public static final int SYNC = 13;
    private static final String SYNC_DATA = "sy";
    private static final String TAG = "BatteryStats";
    private static final String UID_DATA = "uid";
    @VisibleForTesting
    public static final String UID_TIMES_TYPE_ALL = "A";
    private static final String USER_ACTIVITY_DATA = "ua";
    private static final String VERSION_DATA = "vers";
    private static final String VIBRATOR_DATA = "vib";
    public static final int VIBRATOR_ON = 9;
    private static final String VIDEO_DATA = "vid";
    public static final int VIDEO_TURNED_ON = 8;
    private static final String WAKELOCK_DATA = "wl";
    private static final String WAKEUP_ALARM_DATA = "wua";
    private static final String WAKEUP_REASON_DATA = "wr";
    public static final int WAKE_TYPE_DRAW = 18;
    public static final int WAKE_TYPE_FULL = 1;
    public static final int WAKE_TYPE_PARTIAL = 0;
    public static final int WAKE_TYPE_WINDOW = 2;
    public static final int WIFI_AGGREGATE_MULTICAST_ENABLED = 23;
    public static final int WIFI_BATCHED_SCAN = 11;
    private static final String WIFI_CONTROLLER_DATA = "wfcd";
    private static final String WIFI_CONTROLLER_NAME = "WiFi";
    private static final String WIFI_DATA = "wfl";
    private static final String WIFI_MULTICAST_DATA = "wmc";
    public static final int WIFI_MULTICAST_ENABLED = 7;
    private static final String WIFI_MULTICAST_TOTAL_DATA = "wmct";
    public static final int WIFI_RUNNING = 4;
    public static final int WIFI_SCAN = 6;
    private static final String WIFI_SIGNAL_STRENGTH_COUNT_DATA = "wsgc";
    private static final String WIFI_SIGNAL_STRENGTH_TIME_DATA = "wsgt";
    private static final String WIFI_STATE_COUNT_DATA = "wsc";
    static final String[] WIFI_STATE_NAMES = {"off", "scanning", "no_net", "disconn", "sta", "p2p", "sta_p2p", "soft_ap"};
    public static final int WIFI_STATE_OFF = 0;
    public static final int WIFI_STATE_OFF_SCANNING = 1;
    public static final int WIFI_STATE_ON_CONNECTED_P2P = 5;
    public static final int WIFI_STATE_ON_CONNECTED_STA = 4;
    public static final int WIFI_STATE_ON_CONNECTED_STA_P2P = 6;
    public static final int WIFI_STATE_ON_DISCONNECTED = 3;
    public static final int WIFI_STATE_ON_NO_NETWORKS = 2;
    public static final int WIFI_STATE_SOFT_AP = 7;
    private static final String WIFI_STATE_TIME_DATA = "wst";
    public static final int WIFI_SUPPL_STATE_ASSOCIATED = 7;
    public static final int WIFI_SUPPL_STATE_ASSOCIATING = 6;
    public static final int WIFI_SUPPL_STATE_AUTHENTICATING = 5;
    public static final int WIFI_SUPPL_STATE_COMPLETED = 10;
    private static final String WIFI_SUPPL_STATE_COUNT_DATA = "wssc";
    public static final int WIFI_SUPPL_STATE_DISCONNECTED = 1;
    public static final int WIFI_SUPPL_STATE_DORMANT = 11;
    public static final int WIFI_SUPPL_STATE_FOUR_WAY_HANDSHAKE = 8;
    public static final int WIFI_SUPPL_STATE_GROUP_HANDSHAKE = 9;
    public static final int WIFI_SUPPL_STATE_INACTIVE = 3;
    public static final int WIFI_SUPPL_STATE_INTERFACE_DISABLED = 2;
    public static final int WIFI_SUPPL_STATE_INVALID = 0;
    static final String[] WIFI_SUPPL_STATE_NAMES = {"invalid", "disconn", "disabled", "inactive", "scanning", "authenticating", "associating", "associated", "4-way-handshake", "group-handshake", "completed", "dormant", "uninit"};
    public static final int WIFI_SUPPL_STATE_SCANNING = 4;
    static final String[] WIFI_SUPPL_STATE_SHORT_NAMES = {"inv", "dsc", "dis", "inact", "scan", "auth", "ascing", "asced", "4-way", WifiConfiguration.GroupCipher.varName, "compl", "dorm", "uninit"};
    private static final String WIFI_SUPPL_STATE_TIME_DATA = "wsst";
    public static final int WIFI_SUPPL_STATE_UNINITIALIZED = 12;
    private static final IntToString sIntToString = $$Lambda$BatteryStats$q1UvBdLgHRZVzc68BxdksTmbuCw.INSTANCE;
    private static final IntToString sUidToString = $$Lambda$IyvVQC0mKtsfXbnO0kDL64hrk0.INSTANCE;
    private final StringBuilder mFormatBuilder = new StringBuilder(32);
    private final Formatter mFormatter = new Formatter(this.mFormatBuilder);

    /* renamed from: android.os.BatteryStats$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$os$BatterySipper$DrainType = new int[BatterySipper.DrainType.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.AMBIENT_DISPLAY.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.IDLE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.CELL.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.PHONE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.WIFI.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.BLUETOOTH.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.SCREEN.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.FLASHLIGHT.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.APP.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.USER.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.UNACCOUNTED.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.OVERCOUNTED.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.CAMERA.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.MEMORY.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
        }
    }

    public static final class BitDescription {
        public final int mask;
        public final String name;
        public final int shift;
        public final String shortName;
        public final String[] shortValues;
        public final String[] values;

        public BitDescription(int mask2, String name2, String shortName2) {
            this.mask = mask2;
            this.shift = -1;
            this.name = name2;
            this.shortName = shortName2;
            this.values = null;
            this.shortValues = null;
        }

        public BitDescription(int mask2, int shift2, String name2, String shortName2, String[] values2, String[] shortValues2) {
            this.mask = mask2;
            this.shift = shift2;
            this.name = name2;
            this.shortName = shortName2;
            this.values = values2;
            this.shortValues = shortValues2;
        }
    }

    public static abstract class ControllerActivityCounter {
        public abstract LongCounter getIdleTimeCounter();

        public abstract LongCounter getPowerCounter();

        public abstract LongCounter getRxTimeCounter();

        public abstract LongCounter getScanTimeCounter();

        public abstract LongCounter getSleepTimeCounter();

        public abstract LongCounter[] getTxTimeCounters();
    }

    public static abstract class Counter {
        public abstract int getCountLocked(int i);

        public abstract void logState(Printer printer, String str);
    }

    public static final class DailyItem {
        public LevelStepTracker mChargeSteps;
        public LevelStepTracker mDischargeSteps;
        public long mEndTime;
        public ArrayList<PackageChange> mPackageChanges;
        public long mStartTime;
    }

    public static final class HistoryEventTracker {
        private final HashMap<String, SparseIntArray>[] mActiveEvents = new HashMap[22];

        public boolean updateState(int code, String name, int uid, int poolIdx) {
            if ((32768 & code) != 0) {
                int idx = code & HistoryItem.EVENT_TYPE_MASK;
                HashMap<String, SparseIntArray> active = this.mActiveEvents[idx];
                if (active == null) {
                    active = new HashMap<>();
                    this.mActiveEvents[idx] = active;
                }
                SparseIntArray uids = active.get(name);
                if (uids == null) {
                    uids = new SparseIntArray();
                    active.put(name, uids);
                }
                if (uids.indexOfKey(uid) >= 0) {
                    return false;
                }
                uids.put(uid, poolIdx);
            } else if ((code & 16384) != 0) {
                HashMap<String, SparseIntArray> active2 = this.mActiveEvents[code & HistoryItem.EVENT_TYPE_MASK];
                if (active2 == null) {
                    return false;
                }
                SparseIntArray uids2 = active2.get(name);
                if (uids2 == null) {
                    return false;
                }
                int idx2 = uids2.indexOfKey(uid);
                if (idx2 < 0) {
                    return false;
                }
                uids2.removeAt(idx2);
                if (uids2.size() <= 0) {
                    active2.remove(name);
                }
            }
            return true;
        }

        public void removeEvents(int code) {
            this.mActiveEvents[-49153 & code] = null;
        }

        public HashMap<String, SparseIntArray> getStateForEvent(int code) {
            return this.mActiveEvents[code];
        }
    }

    public static final class HistoryItem implements Parcelable {
        public static final byte CMD_CURRENT_TIME = 5;
        public static final byte CMD_NULL = -1;
        public static final byte CMD_OVERFLOW = 6;
        public static final byte CMD_RESET = 7;
        public static final byte CMD_SHUTDOWN = 8;
        public static final byte CMD_START = 4;
        public static final byte CMD_UPDATE = 0;
        public static final int EVENT_ACTIVE = 10;
        public static final int EVENT_ALARM = 13;
        public static final int EVENT_ALARM_FINISH = 16397;
        public static final int EVENT_ALARM_START = 32781;
        public static final int EVENT_COLLECT_EXTERNAL_STATS = 14;
        public static final int EVENT_CONNECTIVITY_CHANGED = 9;
        public static final int EVENT_COUNT = 22;
        public static final int EVENT_FLAG_FINISH = 16384;
        public static final int EVENT_FLAG_START = 32768;
        public static final int EVENT_FOREGROUND = 2;
        public static final int EVENT_FOREGROUND_FINISH = 16386;
        public static final int EVENT_FOREGROUND_START = 32770;
        public static final int EVENT_JOB = 6;
        public static final int EVENT_JOB_FINISH = 16390;
        public static final int EVENT_JOB_START = 32774;
        public static final int EVENT_LONG_WAKE_LOCK = 20;
        public static final int EVENT_LONG_WAKE_LOCK_FINISH = 16404;
        public static final int EVENT_LONG_WAKE_LOCK_START = 32788;
        public static final int EVENT_NONE = 0;
        public static final int EVENT_PACKAGE_ACTIVE = 16;
        public static final int EVENT_PACKAGE_INACTIVE = 15;
        public static final int EVENT_PACKAGE_INSTALLED = 11;
        public static final int EVENT_PACKAGE_UNINSTALLED = 12;
        public static final int EVENT_PROC = 1;
        public static final int EVENT_PROC_FINISH = 16385;
        public static final int EVENT_PROC_START = 32769;
        public static final int EVENT_SCREEN_WAKE_UP = 18;
        public static final int EVENT_SYNC = 4;
        public static final int EVENT_SYNC_FINISH = 16388;
        public static final int EVENT_SYNC_START = 32772;
        public static final int EVENT_TEMP_WHITELIST = 17;
        public static final int EVENT_TEMP_WHITELIST_FINISH = 16401;
        public static final int EVENT_TEMP_WHITELIST_START = 32785;
        public static final int EVENT_TOP = 3;
        public static final int EVENT_TOP_FINISH = 16387;
        public static final int EVENT_TOP_START = 32771;
        public static final int EVENT_TYPE_MASK = -49153;
        public static final int EVENT_USER_FOREGROUND = 8;
        public static final int EVENT_USER_FOREGROUND_FINISH = 16392;
        public static final int EVENT_USER_FOREGROUND_START = 32776;
        public static final int EVENT_USER_RUNNING = 7;
        public static final int EVENT_USER_RUNNING_FINISH = 16391;
        public static final int EVENT_USER_RUNNING_START = 32775;
        public static final int EVENT_WAKEUP_AP = 19;
        public static final int EVENT_WAKE_LOCK = 5;
        public static final int EVENT_WAKE_LOCK_FINISH = 16389;
        public static final int EVENT_WAKE_LOCK_START = 32773;
        public static final int MOST_INTERESTING_STATES = 1835008;
        public static final int MOST_INTERESTING_STATES2 = -1749024768;
        public static final int SETTLE_TO_ZERO_STATES = -1900544;
        public static final int SETTLE_TO_ZERO_STATES2 = 1748959232;
        public static final int STATE2_BLUETOOTH_ON_FLAG = 4194304;
        public static final int STATE2_BLUETOOTH_SCAN_FLAG = 1048576;
        public static final int STATE2_CAMERA_FLAG = 2097152;
        public static final int STATE2_CELLULAR_HIGH_TX_POWER_FLAG = 524288;
        public static final int STATE2_CHARGING_FLAG = 16777216;
        public static final int STATE2_DEVICE_IDLE_MASK = 100663296;
        public static final int STATE2_DEVICE_IDLE_SHIFT = 25;
        public static final int STATE2_FLASHLIGHT_FLAG = 134217728;
        public static final int STATE2_GPS_SIGNAL_QUALITY_MASK = 128;
        public static final int STATE2_GPS_SIGNAL_QUALITY_SHIFT = 7;
        public static final int STATE2_PHONE_IN_CALL_FLAG = 8388608;
        public static final int STATE2_POWER_SAVE_FLAG = Integer.MIN_VALUE;
        public static final int STATE2_USB_DATA_LINK_FLAG = 262144;
        public static final int STATE2_VIDEO_ON_FLAG = 1073741824;
        public static final int STATE2_WIFI_ON_FLAG = 268435456;
        public static final int STATE2_WIFI_RUNNING_FLAG = 536870912;
        public static final int STATE2_WIFI_SIGNAL_STRENGTH_MASK = 112;
        public static final int STATE2_WIFI_SIGNAL_STRENGTH_SHIFT = 4;
        public static final int STATE2_WIFI_SUPPL_STATE_MASK = 15;
        public static final int STATE2_WIFI_SUPPL_STATE_SHIFT = 0;
        public static final int STATE_AUDIO_ON_FLAG = 4194304;
        public static final int STATE_BATTERY_PLUGGED_FLAG = 524288;
        public static final int STATE_BRIGHTNESS_MASK = 7;
        public static final int STATE_BRIGHTNESS_SHIFT = 0;
        public static final int STATE_CPU_RUNNING_FLAG = Integer.MIN_VALUE;
        public static final int STATE_DATA_CONNECTION_MASK = 15872;
        public static final int STATE_DATA_CONNECTION_SHIFT = 9;
        public static final int STATE_GPS_ON_FLAG = 536870912;
        public static final int STATE_MOBILE_RADIO_ACTIVE_FLAG = 33554432;
        public static final int STATE_PHONE_SCANNING_FLAG = 2097152;
        public static final int STATE_PHONE_SIGNAL_STRENGTH_MASK = 56;
        public static final int STATE_PHONE_SIGNAL_STRENGTH_SHIFT = 3;
        public static final int STATE_PHONE_STATE_MASK = 448;
        public static final int STATE_PHONE_STATE_SHIFT = 6;
        private static final int STATE_RESERVED_0 = 16777216;
        public static final int STATE_SCREEN_DOZE_FLAG = 262144;
        public static final int STATE_SCREEN_ON_FLAG = 1048576;
        public static final int STATE_SENSOR_ON_FLAG = 8388608;
        public static final int STATE_WAKE_LOCK_FLAG = 1073741824;
        public static final int STATE_WIFI_FULL_LOCK_FLAG = 268435456;
        public static final int STATE_WIFI_MULTICAST_ON_FLAG = 65536;
        public static final int STATE_WIFI_RADIO_ACTIVE_FLAG = 67108864;
        public static final int STATE_WIFI_SCAN_FLAG = 134217728;
        public int batteryChargeUAh;
        public byte batteryHealth;
        public byte batteryLevel;
        public byte batteryPlugType;
        public byte batteryStatus;
        public short batteryTemperature;
        public char batteryVoltage;
        public byte cmd = -1;
        public long currentTime;
        public int eventCode;
        public HistoryTag eventTag;
        public final HistoryTag localEventTag = new HistoryTag();
        public final HistoryTag localWakeReasonTag = new HistoryTag();
        public final HistoryTag localWakelockTag = new HistoryTag();
        public HistoryItem next;
        public int numReadInts;
        public int states;
        public int states2;
        public HistoryStepDetails stepDetails;
        public long time;
        public HistoryTag wakeReasonTag;
        public HistoryTag wakelockTag;

        public boolean isDeltaData() {
            return this.cmd == 0;
        }

        public HistoryItem() {
        }

        public HistoryItem(long time2, Parcel src) {
            this.time = time2;
            this.numReadInts = 2;
            readFromParcel(src);
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.time);
            int i = 0;
            int i2 = (this.cmd & 255) | ((this.batteryLevel << 8) & 65280) | ((this.batteryStatus << WifiScanner.PnoSettings.PnoNetwork.FLAG_SAME_NETWORK) & SurfaceControl.FX_SURFACE_MASK) | ((this.batteryHealth << 20) & 15728640) | ((this.batteryPlugType << 24) & 251658240) | (this.wakelockTag != null ? 268435456 : 0) | (this.wakeReasonTag != null ? 536870912 : 0);
            if (this.eventCode != 0) {
                i = 1073741824;
            }
            dest.writeInt(i2 | i);
            dest.writeInt((this.batteryTemperature & 65535) | ((this.batteryVoltage << 16) & Menu.CATEGORY_MASK));
            dest.writeInt(this.batteryChargeUAh);
            dest.writeInt(this.states);
            dest.writeInt(this.states2);
            if (this.wakelockTag != null) {
                this.wakelockTag.writeToParcel(dest, flags);
            }
            if (this.wakeReasonTag != null) {
                this.wakeReasonTag.writeToParcel(dest, flags);
            }
            if (this.eventCode != 0) {
                dest.writeInt(this.eventCode);
                this.eventTag.writeToParcel(dest, flags);
            }
            if (this.cmd == 5 || this.cmd == 7) {
                dest.writeLong(this.currentTime);
            }
        }

        public void readFromParcel(Parcel src) {
            int start = src.dataPosition();
            int bat = src.readInt();
            this.cmd = (byte) (bat & 255);
            this.batteryLevel = (byte) ((bat >> 8) & 255);
            this.batteryStatus = (byte) ((bat >> 16) & 15);
            this.batteryHealth = (byte) ((bat >> 20) & 15);
            this.batteryPlugType = (byte) ((bat >> 24) & 15);
            int bat2 = src.readInt();
            this.batteryTemperature = (short) (bat2 & 65535);
            this.batteryVoltage = (char) (65535 & (bat2 >> 16));
            this.batteryChargeUAh = src.readInt();
            this.states = src.readInt();
            this.states2 = src.readInt();
            if ((268435456 & bat) != 0) {
                this.wakelockTag = this.localWakelockTag;
                this.wakelockTag.readFromParcel(src);
            } else {
                this.wakelockTag = null;
            }
            if ((536870912 & bat) != 0) {
                this.wakeReasonTag = this.localWakeReasonTag;
                this.wakeReasonTag.readFromParcel(src);
            } else {
                this.wakeReasonTag = null;
            }
            if ((1073741824 & bat) != 0) {
                this.eventCode = src.readInt();
                this.eventTag = this.localEventTag;
                this.eventTag.readFromParcel(src);
            } else {
                this.eventCode = 0;
                this.eventTag = null;
            }
            if (this.cmd == 5 || this.cmd == 7) {
                this.currentTime = src.readLong();
            } else {
                this.currentTime = 0;
            }
            this.numReadInts += (src.dataPosition() - start) / 4;
        }

        public void clear() {
            this.time = 0;
            this.cmd = -1;
            this.batteryLevel = 0;
            this.batteryStatus = 0;
            this.batteryHealth = 0;
            this.batteryPlugType = 0;
            this.batteryTemperature = 0;
            this.batteryVoltage = 0;
            this.batteryChargeUAh = 0;
            this.states = 0;
            this.states2 = 0;
            this.wakelockTag = null;
            this.wakeReasonTag = null;
            this.eventCode = 0;
            this.eventTag = null;
        }

        public void setTo(HistoryItem o) {
            this.time = o.time;
            this.cmd = o.cmd;
            setToCommon(o);
        }

        public void setTo(long time2, byte cmd2, HistoryItem o) {
            this.time = time2;
            this.cmd = cmd2;
            setToCommon(o);
        }

        private void setToCommon(HistoryItem o) {
            this.batteryLevel = o.batteryLevel;
            this.batteryStatus = o.batteryStatus;
            this.batteryHealth = o.batteryHealth;
            this.batteryPlugType = o.batteryPlugType;
            this.batteryTemperature = o.batteryTemperature;
            this.batteryVoltage = o.batteryVoltage;
            this.batteryChargeUAh = o.batteryChargeUAh;
            this.states = o.states;
            this.states2 = o.states2;
            if (o.wakelockTag != null) {
                this.wakelockTag = this.localWakelockTag;
                this.wakelockTag.setTo(o.wakelockTag);
            } else {
                this.wakelockTag = null;
            }
            if (o.wakeReasonTag != null) {
                this.wakeReasonTag = this.localWakeReasonTag;
                this.wakeReasonTag.setTo(o.wakeReasonTag);
            } else {
                this.wakeReasonTag = null;
            }
            this.eventCode = o.eventCode;
            if (o.eventTag != null) {
                this.eventTag = this.localEventTag;
                this.eventTag.setTo(o.eventTag);
            } else {
                this.eventTag = null;
            }
            this.currentTime = o.currentTime;
        }

        public boolean sameNonEvent(HistoryItem o) {
            return this.batteryLevel == o.batteryLevel && this.batteryStatus == o.batteryStatus && this.batteryHealth == o.batteryHealth && this.batteryPlugType == o.batteryPlugType && this.batteryTemperature == o.batteryTemperature && this.batteryVoltage == o.batteryVoltage && this.batteryChargeUAh == o.batteryChargeUAh && this.states == o.states && this.states2 == o.states2 && this.currentTime == o.currentTime;
        }

        public boolean same(HistoryItem o) {
            if (!sameNonEvent(o) || this.eventCode != o.eventCode) {
                return false;
            }
            if (this.wakelockTag != o.wakelockTag && (this.wakelockTag == null || o.wakelockTag == null || !this.wakelockTag.equals(o.wakelockTag))) {
                return false;
            }
            if (this.wakeReasonTag != o.wakeReasonTag && (this.wakeReasonTag == null || o.wakeReasonTag == null || !this.wakeReasonTag.equals(o.wakeReasonTag))) {
                return false;
            }
            if (this.eventTag == o.eventTag || (this.eventTag != null && o.eventTag != null && this.eventTag.equals(o.eventTag))) {
                return true;
            }
            return false;
        }
    }

    public static class HistoryPrinter {
        long lastTime = -1;
        int oldChargeMAh = -1;
        int oldHealth = -1;
        int oldLevel = -1;
        int oldPlug = -1;
        int oldState = 0;
        int oldState2 = 0;
        int oldStatus = -1;
        int oldTemp = -1;
        int oldVolt = -1;

        /* access modifiers changed from: package-private */
        public void reset() {
            this.oldState2 = 0;
            this.oldState = 0;
            this.oldLevel = -1;
            this.oldStatus = -1;
            this.oldHealth = -1;
            this.oldPlug = -1;
            this.oldTemp = -1;
            this.oldVolt = -1;
            this.oldChargeMAh = -1;
        }

        public void printNextItem(PrintWriter pw, HistoryItem rec, long baseTime, boolean checkin, boolean verbose) {
            pw.print(printNextItem(rec, baseTime, checkin, verbose));
        }

        public void printNextItem(ProtoOutputStream proto, HistoryItem rec, long baseTime, boolean verbose) {
            for (String line : printNextItem(rec, baseTime, true, verbose).split("\n")) {
                proto.write(2237677961222L, line);
            }
        }

        private String printNextItem(HistoryItem rec, long baseTime, boolean checkin, boolean verbose) {
            String[] eventNames;
            StringBuilder item = new StringBuilder();
            if (!checkin) {
                item.append("  ");
                TimeUtils.formatDuration(rec.time - baseTime, item, 19);
                item.append(" (");
                item.append(rec.numReadInts);
                item.append(") ");
            } else {
                item.append(9);
                item.append(',');
                item.append(BatteryStats.HISTORY_DATA);
                item.append(',');
                if (this.lastTime < 0) {
                    item.append(rec.time - baseTime);
                } else {
                    item.append(rec.time - this.lastTime);
                }
                this.lastTime = rec.time;
            }
            if (rec.cmd == 4) {
                if (checkin) {
                    item.append(SettingsStringUtil.DELIMITER);
                }
                item.append("START\n");
                reset();
            } else if (rec.cmd == 5 || rec.cmd == 7) {
                if (checkin) {
                    item.append(SettingsStringUtil.DELIMITER);
                }
                if (rec.cmd == 7) {
                    item.append("RESET:");
                    reset();
                }
                item.append("TIME:");
                if (checkin) {
                    item.append(rec.currentTime);
                    item.append("\n");
                } else {
                    item.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                    item.append(DateFormat.format((CharSequence) "yyyy-MM-dd-HH-mm-ss", rec.currentTime).toString());
                    item.append("\n");
                }
            } else if (rec.cmd == 8) {
                if (checkin) {
                    item.append(SettingsStringUtil.DELIMITER);
                }
                item.append("SHUTDOWN\n");
            } else if (rec.cmd == 6) {
                if (checkin) {
                    item.append(SettingsStringUtil.DELIMITER);
                }
                item.append("*OVERFLOW*\n");
            } else {
                if (!checkin) {
                    if (rec.batteryLevel < 10) {
                        item.append("00");
                    } else if (rec.batteryLevel < 100) {
                        item.append(WifiEnterpriseConfig.ENGINE_DISABLE);
                    }
                    item.append(rec.batteryLevel);
                    if (verbose) {
                        item.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                        if (rec.states >= 0) {
                            if (rec.states < 16) {
                                item.append("0000000");
                            } else if (rec.states < 256) {
                                item.append("000000");
                            } else if (rec.states < 4096) {
                                item.append("00000");
                            } else if (rec.states < 65536) {
                                item.append("0000");
                            } else if (rec.states < 1048576) {
                                item.append("000");
                            } else if (rec.states < 16777216) {
                                item.append("00");
                            } else if (rec.states < 268435456) {
                                item.append(WifiEnterpriseConfig.ENGINE_DISABLE);
                            }
                        }
                        item.append(Integer.toHexString(rec.states));
                    }
                } else if (this.oldLevel != rec.batteryLevel) {
                    this.oldLevel = rec.batteryLevel;
                    item.append(",Bl=");
                    item.append(rec.batteryLevel);
                }
                if (this.oldStatus != rec.batteryStatus) {
                    this.oldStatus = rec.batteryStatus;
                    item.append(checkin ? ",Bs=" : " status=");
                    switch (this.oldStatus) {
                        case 1:
                            item.append(checkin ? "?" : "unknown");
                            break;
                        case 2:
                            item.append(checkin ? "c" : "charging");
                            break;
                        case 3:
                            item.append(checkin ? "d" : "discharging");
                            break;
                        case 4:
                            item.append(checkin ? "n" : "not-charging");
                            break;
                        case 5:
                            item.append(checkin ? "f" : "full");
                            break;
                        default:
                            item.append(this.oldStatus);
                            break;
                    }
                }
                if (this.oldHealth != rec.batteryHealth) {
                    this.oldHealth = rec.batteryHealth;
                    item.append(checkin ? ",Bh=" : " health=");
                    switch (this.oldHealth) {
                        case 1:
                            item.append(checkin ? "?" : "unknown");
                            break;
                        case 2:
                            item.append(checkin ? "g" : "good");
                            break;
                        case 3:
                            item.append(checkin ? BatteryStats.HISTORY_DATA : "overheat");
                            break;
                        case 4:
                            item.append(checkin ? "d" : "dead");
                            break;
                        case 5:
                            item.append(checkin ? Telephony.BaseMmsColumns.MMS_VERSION : "over-voltage");
                            break;
                        case 6:
                            item.append(checkin ? "f" : "failure");
                            break;
                        case 7:
                            item.append(checkin ? "c" : "cold");
                            break;
                        default:
                            item.append(this.oldHealth);
                            break;
                    }
                }
                if (this.oldPlug != rec.batteryPlugType) {
                    this.oldPlug = rec.batteryPlugType;
                    item.append(checkin ? ",Bp=" : " plug=");
                    int i = this.oldPlug;
                    if (i != 4) {
                        switch (i) {
                            case 0:
                                item.append(checkin ? "n" : "none");
                                break;
                            case 1:
                                item.append(checkin ? "a" : "ac");
                                break;
                            case 2:
                                item.append(checkin ? "u" : "usb");
                                break;
                            default:
                                item.append(this.oldPlug);
                                break;
                        }
                    } else {
                        item.append(checkin ? "w" : "wireless");
                    }
                }
                if (this.oldTemp != rec.batteryTemperature) {
                    this.oldTemp = rec.batteryTemperature;
                    item.append(checkin ? ",Bt=" : " temp=");
                    item.append(this.oldTemp);
                }
                if (this.oldVolt != rec.batteryVoltage) {
                    this.oldVolt = rec.batteryVoltage;
                    item.append(checkin ? ",Bv=" : " volt=");
                    item.append(this.oldVolt);
                }
                int chargeMAh = rec.batteryChargeUAh / 1000;
                if (this.oldChargeMAh != chargeMAh) {
                    this.oldChargeMAh = chargeMAh;
                    item.append(checkin ? ",Bcc=" : " charge=");
                    item.append(this.oldChargeMAh);
                }
                BatteryStats.printBitDescriptions(item, this.oldState, rec.states, rec.wakelockTag, BatteryStats.HISTORY_STATE_DESCRIPTIONS, !checkin);
                BatteryStats.printBitDescriptions(item, this.oldState2, rec.states2, null, BatteryStats.HISTORY_STATE2_DESCRIPTIONS, !checkin);
                if (rec.wakeReasonTag != null) {
                    if (checkin) {
                        item.append(",wr=");
                        item.append(rec.wakeReasonTag.poolIdx);
                    } else {
                        item.append(" wake_reason=");
                        item.append(rec.wakeReasonTag.uid);
                        item.append(":\"");
                        item.append(rec.wakeReasonTag.string);
                        item.append("\"");
                    }
                }
                if (rec.eventCode != 0) {
                    item.append(checkin ? "," : WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                    if ((rec.eventCode & 32768) != 0) {
                        item.append("+");
                    } else if ((rec.eventCode & 16384) != 0) {
                        item.append("-");
                    }
                    if (checkin) {
                        eventNames = BatteryStats.HISTORY_EVENT_CHECKIN_NAMES;
                    } else {
                        eventNames = BatteryStats.HISTORY_EVENT_NAMES;
                    }
                    int idx = rec.eventCode & HistoryItem.EVENT_TYPE_MASK;
                    if (idx < 0 || idx >= eventNames.length) {
                        item.append(checkin ? "Ev" : ZenModeConfig.EVENT_PATH);
                        item.append(idx);
                    } else {
                        item.append(eventNames[idx]);
                    }
                    item.append("=");
                    if (checkin) {
                        item.append(rec.eventTag.poolIdx);
                    } else {
                        item.append(BatteryStats.HISTORY_EVENT_INT_FORMATTERS[idx].applyAsString(rec.eventTag.uid));
                        item.append(":\"");
                        item.append(rec.eventTag.string);
                        item.append("\"");
                    }
                }
                item.append("\n");
                if (rec.stepDetails != null) {
                    if (!checkin) {
                        item.append("                 Details: cpu=");
                        item.append(rec.stepDetails.userTime);
                        item.append("u+");
                        item.append(rec.stepDetails.systemTime);
                        item.append("s");
                        if (rec.stepDetails.appCpuUid1 >= 0) {
                            item.append(" (");
                            printStepCpuUidDetails(item, rec.stepDetails.appCpuUid1, rec.stepDetails.appCpuUTime1, rec.stepDetails.appCpuSTime1);
                            if (rec.stepDetails.appCpuUid2 >= 0) {
                                item.append(", ");
                                printStepCpuUidDetails(item, rec.stepDetails.appCpuUid2, rec.stepDetails.appCpuUTime2, rec.stepDetails.appCpuSTime2);
                            }
                            if (rec.stepDetails.appCpuUid3 >= 0) {
                                item.append(", ");
                                printStepCpuUidDetails(item, rec.stepDetails.appCpuUid3, rec.stepDetails.appCpuUTime3, rec.stepDetails.appCpuSTime3);
                            }
                            item.append(')');
                        }
                        item.append("\n");
                        item.append("                          /proc/stat=");
                        item.append(rec.stepDetails.statUserTime);
                        item.append(" usr, ");
                        item.append(rec.stepDetails.statSystemTime);
                        item.append(" sys, ");
                        item.append(rec.stepDetails.statIOWaitTime);
                        item.append(" io, ");
                        item.append(rec.stepDetails.statIrqTime);
                        item.append(" irq, ");
                        item.append(rec.stepDetails.statSoftIrqTime);
                        item.append(" sirq, ");
                        item.append(rec.stepDetails.statIdlTime);
                        item.append(" idle");
                        int totalRun = rec.stepDetails.statUserTime + rec.stepDetails.statSystemTime + rec.stepDetails.statIOWaitTime + rec.stepDetails.statIrqTime + rec.stepDetails.statSoftIrqTime;
                        int total = rec.stepDetails.statIdlTime + totalRun;
                        if (total > 0) {
                            item.append(" (");
                            item.append(String.format("%.1f%%", new Object[]{Float.valueOf((((float) totalRun) / ((float) total)) * 100.0f)}));
                            item.append(" of ");
                            StringBuilder sb = new StringBuilder(64);
                            BatteryStats.formatTimeMsNoSpace(sb, (long) (total * 10));
                            item.append(sb);
                            item.append(")");
                        }
                        item.append(", PlatformIdleStat ");
                        item.append(rec.stepDetails.statPlatformIdleState);
                        item.append("\n");
                        item.append(", SubsystemPowerState ");
                        item.append(rec.stepDetails.statSubsystemPowerState);
                        item.append("\n");
                    } else {
                        item.append(9);
                        item.append(',');
                        item.append(BatteryStats.HISTORY_DATA);
                        item.append(",0,Dcpu=");
                        item.append(rec.stepDetails.userTime);
                        item.append(SettingsStringUtil.DELIMITER);
                        item.append(rec.stepDetails.systemTime);
                        if (rec.stepDetails.appCpuUid1 >= 0) {
                            printStepCpuUidCheckinDetails(item, rec.stepDetails.appCpuUid1, rec.stepDetails.appCpuUTime1, rec.stepDetails.appCpuSTime1);
                            if (rec.stepDetails.appCpuUid2 >= 0) {
                                printStepCpuUidCheckinDetails(item, rec.stepDetails.appCpuUid2, rec.stepDetails.appCpuUTime2, rec.stepDetails.appCpuSTime2);
                            }
                            if (rec.stepDetails.appCpuUid3 >= 0) {
                                printStepCpuUidCheckinDetails(item, rec.stepDetails.appCpuUid3, rec.stepDetails.appCpuUTime3, rec.stepDetails.appCpuSTime3);
                            }
                        }
                        item.append("\n");
                        item.append(9);
                        item.append(',');
                        item.append(BatteryStats.HISTORY_DATA);
                        item.append(",0,Dpst=");
                        item.append(rec.stepDetails.statUserTime);
                        item.append(',');
                        item.append(rec.stepDetails.statSystemTime);
                        item.append(',');
                        item.append(rec.stepDetails.statIOWaitTime);
                        item.append(',');
                        item.append(rec.stepDetails.statIrqTime);
                        item.append(',');
                        item.append(rec.stepDetails.statSoftIrqTime);
                        item.append(',');
                        item.append(rec.stepDetails.statIdlTime);
                        item.append(',');
                        if (rec.stepDetails.statPlatformIdleState != null) {
                            item.append(rec.stepDetails.statPlatformIdleState);
                            if (rec.stepDetails.statSubsystemPowerState != null) {
                                item.append(',');
                            }
                        }
                        if (rec.stepDetails.statSubsystemPowerState != null) {
                            item.append(rec.stepDetails.statSubsystemPowerState);
                        }
                        item.append("\n");
                    }
                }
                this.oldState = rec.states;
                this.oldState2 = rec.states2;
            }
            return item.toString();
        }

        private void printStepCpuUidDetails(StringBuilder sb, int uid, int utime, int stime) {
            UserHandle.formatUid(sb, uid);
            sb.append("=");
            sb.append(utime);
            sb.append("u+");
            sb.append(stime);
            sb.append("s");
        }

        private void printStepCpuUidCheckinDetails(StringBuilder sb, int uid, int utime, int stime) {
            sb.append('/');
            sb.append(uid);
            sb.append(SettingsStringUtil.DELIMITER);
            sb.append(utime);
            sb.append(SettingsStringUtil.DELIMITER);
            sb.append(stime);
        }
    }

    public static final class HistoryStepDetails {
        public int appCpuSTime1;
        public int appCpuSTime2;
        public int appCpuSTime3;
        public int appCpuUTime1;
        public int appCpuUTime2;
        public int appCpuUTime3;
        public int appCpuUid1;
        public int appCpuUid2;
        public int appCpuUid3;
        public int statIOWaitTime;
        public int statIdlTime;
        public int statIrqTime;
        public String statPlatformIdleState;
        public int statSoftIrqTime;
        public String statSubsystemPowerState;
        public int statSystemTime;
        public int statUserTime;
        public int systemTime;
        public int userTime;

        public HistoryStepDetails() {
            clear();
        }

        public void clear() {
            this.systemTime = 0;
            this.userTime = 0;
            this.appCpuUid3 = -1;
            this.appCpuUid2 = -1;
            this.appCpuUid1 = -1;
            this.appCpuSTime3 = 0;
            this.appCpuUTime3 = 0;
            this.appCpuSTime2 = 0;
            this.appCpuUTime2 = 0;
            this.appCpuSTime1 = 0;
            this.appCpuUTime1 = 0;
        }

        public void writeToParcel(Parcel out) {
            out.writeInt(this.userTime);
            out.writeInt(this.systemTime);
            out.writeInt(this.appCpuUid1);
            out.writeInt(this.appCpuUTime1);
            out.writeInt(this.appCpuSTime1);
            out.writeInt(this.appCpuUid2);
            out.writeInt(this.appCpuUTime2);
            out.writeInt(this.appCpuSTime2);
            out.writeInt(this.appCpuUid3);
            out.writeInt(this.appCpuUTime3);
            out.writeInt(this.appCpuSTime3);
            out.writeInt(this.statUserTime);
            out.writeInt(this.statSystemTime);
            out.writeInt(this.statIOWaitTime);
            out.writeInt(this.statIrqTime);
            out.writeInt(this.statSoftIrqTime);
            out.writeInt(this.statIdlTime);
            out.writeString(this.statPlatformIdleState);
            out.writeString(this.statSubsystemPowerState);
        }

        public void readFromParcel(Parcel in) {
            this.userTime = in.readInt();
            this.systemTime = in.readInt();
            this.appCpuUid1 = in.readInt();
            this.appCpuUTime1 = in.readInt();
            this.appCpuSTime1 = in.readInt();
            this.appCpuUid2 = in.readInt();
            this.appCpuUTime2 = in.readInt();
            this.appCpuSTime2 = in.readInt();
            this.appCpuUid3 = in.readInt();
            this.appCpuUTime3 = in.readInt();
            this.appCpuSTime3 = in.readInt();
            this.statUserTime = in.readInt();
            this.statSystemTime = in.readInt();
            this.statIOWaitTime = in.readInt();
            this.statIrqTime = in.readInt();
            this.statSoftIrqTime = in.readInt();
            this.statIdlTime = in.readInt();
            this.statPlatformIdleState = in.readString();
            this.statSubsystemPowerState = in.readString();
        }
    }

    public static final class HistoryTag {
        public int poolIdx;
        public String string;
        public int uid;

        public void setTo(HistoryTag o) {
            this.string = o.string != null ? o.string : "";
            this.uid = o.uid;
            this.poolIdx = o.poolIdx;
        }

        public void setTo(String _string, int _uid) {
            this.string = _string != null ? _string : "";
            this.uid = _uid;
            this.poolIdx = -1;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.string);
            dest.writeInt(this.uid);
        }

        public void readFromParcel(Parcel src) {
            this.string = src.readString();
            this.uid = src.readInt();
            this.poolIdx = -1;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            HistoryTag that = (HistoryTag) o;
            if (this.uid == that.uid && this.string.equals(that.string)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return (31 * this.string.hashCode()) + this.uid;
        }
    }

    @FunctionalInterface
    public interface IntToString {
        String applyAsString(int i);
    }

    public static final class LevelStepTracker {
        public long mLastStepTime = -1;
        public int mNumStepDurations;
        public final long[] mStepDurations;

        public LevelStepTracker(int maxLevelSteps) {
            this.mStepDurations = new long[maxLevelSteps];
        }

        public LevelStepTracker(int numSteps, long[] steps) {
            this.mNumStepDurations = numSteps;
            this.mStepDurations = new long[numSteps];
            System.arraycopy(steps, 0, this.mStepDurations, 0, numSteps);
        }

        public long getDurationAt(int index) {
            return this.mStepDurations[index] & BatteryStats.STEP_LEVEL_TIME_MASK;
        }

        public int getLevelAt(int index) {
            return (int) ((this.mStepDurations[index] & BatteryStats.STEP_LEVEL_LEVEL_MASK) >> 40);
        }

        public int getInitModeAt(int index) {
            return (int) ((this.mStepDurations[index] & BatteryStats.STEP_LEVEL_INITIAL_MODE_MASK) >> 48);
        }

        public int getModModeAt(int index) {
            return (int) ((this.mStepDurations[index] & BatteryStats.STEP_LEVEL_MODIFIED_MODE_MASK) >> 56);
        }

        private void appendHex(long val, int topOffset, StringBuilder out) {
            boolean hasData = false;
            while (topOffset >= 0) {
                int digit = (int) ((val >> topOffset) & 15);
                topOffset -= 4;
                if (hasData || digit != 0) {
                    hasData = true;
                    if (digit < 0 || digit > 9) {
                        out.append((char) ((97 + digit) - 10));
                    } else {
                        out.append((char) (48 + digit));
                    }
                }
            }
        }

        public void encodeEntryAt(int index, StringBuilder out) {
            long item = this.mStepDurations[index];
            long duration = BatteryStats.STEP_LEVEL_TIME_MASK & item;
            int level = (int) ((BatteryStats.STEP_LEVEL_LEVEL_MASK & item) >> 40);
            int initMode = (int) ((BatteryStats.STEP_LEVEL_INITIAL_MODE_MASK & item) >> 48);
            int modMode = (int) ((BatteryStats.STEP_LEVEL_MODIFIED_MODE_MASK & item) >> 56);
            switch ((initMode & 3) + 1) {
                case 1:
                    out.append('f');
                    break;
                case 2:
                    out.append('o');
                    break;
                case 3:
                    out.append(DateFormat.DATE);
                    break;
                case 4:
                    out.append(DateFormat.TIME_ZONE);
                    break;
            }
            if ((initMode & 4) != 0) {
                out.append('p');
            }
            if ((initMode & 8) != 0) {
                out.append('i');
            }
            switch ((modMode & 3) + 1) {
                case 1:
                    out.append('F');
                    break;
                case 2:
                    out.append('O');
                    break;
                case 3:
                    out.append('D');
                    break;
                case 4:
                    out.append('Z');
                    break;
            }
            if ((modMode & 4) != 0) {
                out.append('P');
            }
            if ((modMode & 8) != 0) {
                out.append('I');
            }
            out.append('-');
            appendHex((long) level, 4, out);
            out.append('-');
            appendHex(duration, 36, out);
        }

        public void decodeEntryAt(int index, String value) {
            char c;
            char c2;
            long level;
            String str = value;
            int N = value.length();
            int i = 0;
            long out = 0;
            while (true) {
                c = '-';
                if (i < N) {
                    char charAt = str.charAt(i);
                    char c3 = charAt;
                    if (charAt != '-') {
                        i++;
                        switch (c3) {
                            case 'D':
                                out |= 144115188075855872L;
                                break;
                            case 'F':
                                out |= 0;
                                break;
                            case 'I':
                                out |= 576460752303423488L;
                                break;
                            case 'O':
                                out |= 72057594037927936L;
                                break;
                            case 'P':
                                out |= 288230376151711744L;
                                break;
                            case 'Z':
                                out |= 216172782113783808L;
                                break;
                            case 'd':
                                out |= 562949953421312L;
                                break;
                            case 'f':
                                out |= 0;
                                break;
                            case 'i':
                                out |= 2251799813685248L;
                                break;
                            case 'o':
                                out |= 281474976710656L;
                                break;
                            case 'p':
                                out |= 1125899906842624L;
                                break;
                            case 'z':
                                out |= 844424930131968L;
                                break;
                        }
                    }
                }
            }
            int i2 = i + 1;
            long level2 = 0;
            while (true) {
                c2 = '0';
                if (i2 < N) {
                    char charAt2 = str.charAt(i2);
                    char c4 = charAt2;
                    if (charAt2 != '-') {
                        i2++;
                        level2 <<= 4;
                        char c5 = c4;
                        if (c5 >= '0' && c5 <= '9') {
                            level2 += (long) (c5 - '0');
                        } else if (c5 >= 'a' && c5 <= 'f') {
                            level2 += (long) ((c5 - 'a') + 10);
                        } else if (c5 >= 'A' && c5 <= 'F') {
                            level2 += (long) ((c5 - 'A') + 10);
                        }
                    }
                }
            }
            int i3 = i2 + 1;
            long out2 = out | ((level2 << 40) & BatteryStats.STEP_LEVEL_LEVEL_MASK);
            long duration = 0;
            while (i3 < N) {
                char charAt3 = str.charAt(i3);
                char c6 = charAt3;
                if (charAt3 != c) {
                    i3++;
                    duration <<= 4;
                    char c7 = c6;
                    if (c7 < c2 || c7 > '9') {
                        level = level2;
                        if (c7 >= 'a' && c7 <= 'f') {
                            duration += (long) ((c7 - 'a') + 10);
                        } else if (c7 >= 'A' && c7 <= 'F') {
                            duration += (long) ((c7 - 'A') + 10);
                        }
                    } else {
                        level = level2;
                        duration += (long) (c7 - '0');
                    }
                    level2 = level;
                    c2 = '0';
                    c = '-';
                } else {
                    this.mStepDurations[index] = (duration & BatteryStats.STEP_LEVEL_TIME_MASK) | out2;
                }
            }
            this.mStepDurations[index] = (duration & BatteryStats.STEP_LEVEL_TIME_MASK) | out2;
        }

        public void init() {
            this.mLastStepTime = -1;
            this.mNumStepDurations = 0;
        }

        public void clearTime() {
            this.mLastStepTime = -1;
        }

        public long computeTimePerLevel() {
            long[] steps = this.mStepDurations;
            int numSteps = this.mNumStepDurations;
            if (numSteps <= 0) {
                return -1;
            }
            long total = 0;
            for (int i = 0; i < numSteps; i++) {
                total += steps[i] & BatteryStats.STEP_LEVEL_TIME_MASK;
            }
            return total / ((long) numSteps);
        }

        public long computeTimeEstimate(long modesOfInterest, long modeValues, int[] outNumOfInterest) {
            long[] steps = this.mStepDurations;
            int count = this.mNumStepDurations;
            if (count <= 0) {
                return -1;
            }
            int numOfInterest = 0;
            long total = 0;
            for (int i = 0; i < count; i++) {
                long initMode = (steps[i] & BatteryStats.STEP_LEVEL_INITIAL_MODE_MASK) >> 48;
                if ((((steps[i] & BatteryStats.STEP_LEVEL_MODIFIED_MODE_MASK) >> 56) & modesOfInterest) == 0 && (initMode & modesOfInterest) == modeValues) {
                    numOfInterest++;
                    total += steps[i] & BatteryStats.STEP_LEVEL_TIME_MASK;
                }
            }
            if (numOfInterest <= 0) {
                return -1;
            }
            if (outNumOfInterest != null) {
                outNumOfInterest[0] = numOfInterest;
            }
            return (total / ((long) numOfInterest)) * 100;
        }

        public void addLevelSteps(int numStepLevels, long modeBits, long elapsedRealtime) {
            int i = numStepLevels;
            long j = elapsedRealtime;
            int stepCount = this.mNumStepDurations;
            long lastStepTime = this.mLastStepTime;
            if (lastStepTime >= 0 && i > 0) {
                long[] steps = this.mStepDurations;
                long duration = j - lastStepTime;
                for (int i2 = 0; i2 < i; i2++) {
                    System.arraycopy(steps, 0, steps, 1, steps.length - 1);
                    long thisDuration = duration / ((long) (i - i2));
                    duration -= thisDuration;
                    if (thisDuration > BatteryStats.STEP_LEVEL_TIME_MASK) {
                        thisDuration = BatteryStats.STEP_LEVEL_TIME_MASK;
                    }
                    steps[0] = thisDuration | modeBits;
                }
                stepCount += i;
                if (stepCount > steps.length) {
                    stepCount = steps.length;
                }
            }
            this.mNumStepDurations = stepCount;
            this.mLastStepTime = j;
        }

        public void readFromParcel(Parcel in) {
            int N = in.readInt();
            if (N <= this.mStepDurations.length) {
                this.mNumStepDurations = N;
                for (int i = 0; i < N; i++) {
                    this.mStepDurations[i] = in.readLong();
                }
                return;
            }
            throw new ParcelFormatException("more step durations than available: " + N);
        }

        public void writeToParcel(Parcel out) {
            int N = this.mNumStepDurations;
            out.writeInt(N);
            for (int i = 0; i < N; i++) {
                out.writeLong(this.mStepDurations[i]);
            }
        }
    }

    public static abstract class LongCounter {
        public abstract long getCountLocked(int i);

        public abstract void logState(Printer printer, String str);
    }

    public static abstract class LongCounterArray {
        public abstract long[] getCountsLocked(int i);

        public abstract void logState(Printer printer, String str);
    }

    public static final class PackageChange {
        public String mPackageName;
        public boolean mUpdate;
        public long mVersionCode;
    }

    public static abstract class Timer {
        public abstract int getCountLocked(int i);

        public abstract long getTimeSinceMarkLocked(long j);

        public abstract long getTotalTimeLocked(long j, int i);

        public abstract void logState(Printer printer, String str);

        public long getMaxDurationMsLocked(long elapsedRealtimeMs) {
            return -1;
        }

        public long getCurrentDurationMsLocked(long elapsedRealtimeMs) {
            return -1;
        }

        public long getTotalDurationMsLocked(long elapsedRealtimeMs) {
            return -1;
        }

        public Timer getSubTimer() {
            return null;
        }

        public boolean isRunningLocked() {
            return false;
        }
    }

    static final class TimerEntry {
        final int mId;
        final String mName;
        final long mTime;
        final Timer mTimer;

        TimerEntry(String name, int id, Timer timer, long time) {
            this.mName = name;
            this.mId = id;
            this.mTimer = timer;
            this.mTime = time;
        }
    }

    public static abstract class Uid {
        public static final int[] CRITICAL_PROC_STATES = {0, 1, 2};
        public static final int NUM_PROCESS_STATE = 7;
        public static final int NUM_USER_ACTIVITY_TYPES = 4;
        public static final int NUM_WIFI_BATCHED_SCAN_BINS = 5;
        public static final int PROCESS_STATE_BACKGROUND = 3;
        public static final int PROCESS_STATE_CACHED = 6;
        public static final int PROCESS_STATE_FOREGROUND = 2;
        public static final int PROCESS_STATE_FOREGROUND_SERVICE = 1;
        public static final int PROCESS_STATE_HEAVY_WEIGHT = 5;
        static final String[] PROCESS_STATE_NAMES = {"Top", "Fg Service", "Foreground", "Background", "Top Sleeping", "Heavy Weight", "Cached"};
        public static final int PROCESS_STATE_TOP = 0;
        public static final int PROCESS_STATE_TOP_SLEEPING = 4;
        @VisibleForTesting
        public static final String[] UID_PROCESS_TYPES = {HwSysResource.BIGDATA_SU_TOL, "FS", "F", "B", "TS", "HW", "C"};
        static final String[] USER_ACTIVITY_TYPES = {"other", "button", "touch", "accessibility"};

        public class Pid {
            public int mWakeNesting;
            public long mWakeStartMs;
            public long mWakeSumMs;

            public Pid() {
            }
        }

        public static abstract class Pkg {

            public static abstract class Serv {
                public abstract int getLaunches(int i);

                public abstract long getStartTime(long j, int i);

                public abstract int getStarts(int i);
            }

            public abstract ArrayMap<String, ? extends Serv> getServiceStats();

            public abstract ArrayMap<String, ? extends Counter> getWakeupAlarmStats();
        }

        public static abstract class Proc {

            public static class ExcessivePower {
                public static final int TYPE_CPU = 2;
                public static final int TYPE_WAKE = 1;
                public long overTime;
                public int type;
                public long usedTime;
            }

            public abstract int countExcessivePowers();

            public abstract ExcessivePower getExcessivePower(int i);

            public abstract long getForegroundTime(int i);

            public abstract int getNumAnrs(int i);

            public abstract int getNumCrashes(int i);

            public abstract int getStarts(int i);

            public abstract long getSystemTime(int i);

            public abstract long getUserTime(int i);

            public abstract boolean isActive();
        }

        public static abstract class Sensor {
            public static final int GPS = -10000;

            public abstract int getHandle();

            public abstract Timer getSensorBackgroundTime();

            public abstract Timer getSensorTime();
        }

        public static abstract class Wakelock {
            public abstract Timer getWakeTime(int i);
        }

        public abstract Timer getAggregatedPartialWakelockTimer();

        public abstract Timer getAudioTurnedOnTimer();

        public abstract ControllerActivityCounter getBluetoothControllerActivity();

        public abstract Timer getBluetoothScanBackgroundTimer();

        public abstract Counter getBluetoothScanResultBgCounter();

        public abstract Counter getBluetoothScanResultCounter();

        public abstract Timer getBluetoothScanTimer();

        public abstract Timer getBluetoothUnoptimizedScanBackgroundTimer();

        public abstract Timer getBluetoothUnoptimizedScanTimer();

        public abstract Timer getCameraTurnedOnTimer();

        public abstract long getCpuActiveTime();

        public abstract long[] getCpuClusterTimes();

        public abstract long[] getCpuFreqTimes(int i);

        public abstract long[] getCpuFreqTimes(int i, int i2);

        public abstract void getDeferredJobsCheckinLineLocked(StringBuilder sb, int i);

        public abstract void getDeferredJobsLineLocked(StringBuilder sb, int i);

        public abstract Timer getFlashlightTurnedOnTimer();

        public abstract Timer getForegroundActivityTimer();

        public abstract Timer getForegroundServiceTimer();

        public abstract long getFullWifiLockTime(long j, int i);

        public abstract ArrayMap<String, SparseIntArray> getJobCompletionStats();

        public abstract ArrayMap<String, ? extends Timer> getJobStats();

        public abstract int getMobileRadioActiveCount(int i);

        public abstract long getMobileRadioActiveTime(int i);

        public abstract long getMobileRadioApWakeupCount(int i);

        public abstract ControllerActivityCounter getModemControllerActivity();

        public abstract Timer getMulticastWakelockStats();

        public abstract long getNetworkActivityBytes(int i, int i2);

        public abstract long getNetworkActivityPackets(int i, int i2);

        public abstract ArrayMap<String, ? extends Pkg> getPackageStats();

        public abstract SparseArray<? extends Pid> getPidStats();

        public abstract long getProcessStateTime(int i, long j, int i2);

        public abstract Timer getProcessStateTimer(int i);

        public abstract ArrayMap<String, ? extends Proc> getProcessStats();

        public abstract long[] getScreenOffCpuFreqTimes(int i);

        public abstract long[] getScreenOffCpuFreqTimes(int i, int i2);

        public abstract SparseArray<? extends Sensor> getSensorStats();

        public abstract ArrayMap<String, ? extends Timer> getSyncStats();

        public abstract long getSystemCpuTimeUs(int i);

        public abstract long getTimeAtCpuSpeed(int i, int i2, int i3);

        public abstract int getUid();

        public abstract int getUserActivityCount(int i, int i2);

        public abstract long getUserCpuTimeUs(int i);

        public abstract Timer getVibratorOnTimer();

        public abstract Timer getVideoTurnedOnTimer();

        public abstract ArrayMap<String, ? extends Wakelock> getWakelockStats();

        public abstract int getWifiBatchedScanCount(int i, int i2);

        public abstract long getWifiBatchedScanTime(int i, long j, int i2);

        public abstract ControllerActivityCounter getWifiControllerActivity();

        public abstract long getWifiMulticastTime(long j, int i);

        public abstract long getWifiRadioApWakeupCount(int i);

        public abstract long getWifiRunningTime(long j, int i);

        public abstract long getWifiScanActualTime(long j);

        public abstract int getWifiScanBackgroundCount(int i);

        public abstract long getWifiScanBackgroundTime(long j);

        public abstract Timer getWifiScanBackgroundTimer();

        public abstract int getWifiScanCount(int i);

        public abstract long getWifiScanTime(long j, int i);

        public abstract Timer getWifiScanTimer();

        public abstract boolean hasNetworkActivity();

        public abstract boolean hasUserActivity();

        public abstract void noteActivityPausedLocked(long j);

        public abstract void noteActivityResumedLocked(long j);

        public abstract void noteFullWifiLockAcquiredLocked(long j);

        public abstract void noteFullWifiLockReleasedLocked(long j);

        public abstract void noteUserActivityLocked(int i);

        public abstract void noteWifiBatchedScanStartedLocked(int i, long j);

        public abstract void noteWifiBatchedScanStoppedLocked(long j);

        public abstract void noteWifiMulticastDisabledLocked(long j);

        public abstract void noteWifiMulticastEnabledLocked(long j);

        public abstract void noteWifiRunningLocked(long j);

        public abstract void noteWifiScanStartedLocked(long j);

        public abstract void noteWifiScanStoppedLocked(long j);

        public abstract void noteWifiStoppedLocked(long j);
    }

    public abstract void commitCurrentHistoryBatchLocked();

    public abstract long computeBatteryRealtime(long j, int i);

    public abstract long computeBatteryScreenOffRealtime(long j, int i);

    public abstract long computeBatteryScreenOffUptime(long j, int i);

    public abstract long computeBatteryTimeRemaining(long j);

    public abstract long computeBatteryUptime(long j, int i);

    public abstract long computeChargeTimeRemaining(long j);

    public abstract long computeRealtime(long j, int i);

    public abstract long computeUptime(long j, int i);

    public abstract void finishIteratingHistoryLocked();

    public abstract void finishIteratingOldHistoryLocked();

    public abstract long getBatteryRealtime(long j);

    public abstract long getBatteryUptime(long j);

    public abstract ControllerActivityCounter getBluetoothControllerActivity();

    public abstract long getBluetoothScanTime(long j, int i);

    public abstract long getCameraOnTime(long j, int i);

    public abstract LevelStepTracker getChargeLevelStepTracker();

    public abstract long[] getCpuFreqs();

    public abstract long getCurrentDailyStartTime();

    public abstract LevelStepTracker getDailyChargeLevelStepTracker();

    public abstract LevelStepTracker getDailyDischargeLevelStepTracker();

    public abstract DailyItem getDailyItemLocked(int i);

    public abstract ArrayList<PackageChange> getDailyPackageChanges();

    public abstract int getDeviceIdleModeCount(int i, int i2);

    public abstract long getDeviceIdleModeTime(int i, long j, int i2);

    public abstract int getDeviceIdlingCount(int i, int i2);

    public abstract long getDeviceIdlingTime(int i, long j, int i2);

    public abstract int getDischargeAmount(int i);

    public abstract int getDischargeAmountScreenDoze();

    public abstract int getDischargeAmountScreenDozeSinceCharge();

    public abstract int getDischargeAmountScreenOff();

    public abstract int getDischargeAmountScreenOffSinceCharge();

    public abstract int getDischargeAmountScreenOn();

    public abstract int getDischargeAmountScreenOnSinceCharge();

    public abstract int getDischargeCurrentLevel();

    public abstract LevelStepTracker getDischargeLevelStepTracker();

    public abstract int getDischargeStartLevel();

    public abstract String getEndPlatformVersion();

    public abstract int getEstimatedBatteryCapacity();

    public abstract long getFlashlightOnCount(int i);

    public abstract long getFlashlightOnTime(long j, int i);

    public abstract long getGlobalWifiRunningTime(long j, int i);

    public abstract long getGpsBatteryDrainMaMs();

    public abstract long getGpsSignalQualityTime(int i, long j, int i2);

    public abstract int getHighDischargeAmountSinceCharge();

    public abstract long getHistoryBaseTime();

    public abstract int getHistoryStringPoolBytes();

    public abstract int getHistoryStringPoolSize();

    public abstract String getHistoryTagPoolString(int i);

    public abstract int getHistoryTagPoolUid(int i);

    public abstract int getHistoryTotalSize();

    public abstract int getHistoryUsedSize();

    public abstract long getInteractiveTime(long j, int i);

    public abstract boolean getIsOnBattery();

    public abstract LongSparseArray<? extends Timer> getKernelMemoryStats();

    public abstract Map<String, ? extends Timer> getKernelWakelockStats();

    public abstract long getLongestDeviceIdleModeTime(int i);

    public abstract int getLowDischargeAmountSinceCharge();

    public abstract int getMaxLearnedBatteryCapacity();

    public abstract int getMinLearnedBatteryCapacity();

    public abstract long getMobileRadioActiveAdjustedTime(int i);

    public abstract int getMobileRadioActiveCount(int i);

    public abstract long getMobileRadioActiveTime(long j, int i);

    public abstract int getMobileRadioActiveUnknownCount(int i);

    public abstract long getMobileRadioActiveUnknownTime(int i);

    public abstract ControllerActivityCounter getModemControllerActivity();

    public abstract long getNetworkActivityBytes(int i, int i2);

    public abstract long getNetworkActivityPackets(int i, int i2);

    public abstract boolean getNextHistoryLocked(HistoryItem historyItem);

    public abstract long getNextMaxDailyDeadline();

    public abstract long getNextMinDailyDeadline();

    public abstract boolean getNextOldHistoryLocked(HistoryItem historyItem);

    public abstract int getNumConnectivityChange(int i);

    public abstract int getParcelVersion();

    public abstract int getPhoneDataConnectionCount(int i, int i2);

    public abstract long getPhoneDataConnectionTime(int i, long j, int i2);

    public abstract Timer getPhoneDataConnectionTimer(int i);

    public abstract int getPhoneOnCount(int i);

    public abstract long getPhoneOnTime(long j, int i);

    public abstract long getPhoneSignalScanningTime(long j, int i);

    public abstract Timer getPhoneSignalScanningTimer();

    public abstract int getPhoneSignalStrengthCount(int i, int i2);

    public abstract long getPhoneSignalStrengthTime(int i, long j, int i2);

    /* access modifiers changed from: protected */
    public abstract Timer getPhoneSignalStrengthTimer(int i);

    public abstract int getPowerSaveModeEnabledCount(int i);

    public abstract long getPowerSaveModeEnabledTime(long j, int i);

    public abstract Map<String, ? extends Timer> getRpmStats();

    public abstract long getScreenBrightnessTime(int i, long j, int i2);

    public abstract Timer getScreenBrightnessTimer(int i);

    public abstract int getScreenDozeCount(int i);

    public abstract long getScreenDozeTime(long j, int i);

    public abstract Map<String, ? extends Timer> getScreenOffRpmStats();

    public abstract int getScreenOnCount(int i);

    public abstract long getScreenOnTime(long j, int i);

    public abstract long getStartClockTime();

    public abstract int getStartCount();

    public abstract String getStartPlatformVersion();

    public abstract long getUahDischarge(int i);

    public abstract long getUahDischargeDeepDoze(int i);

    public abstract long getUahDischargeLightDoze(int i);

    public abstract long getUahDischargeScreenDoze(int i);

    public abstract long getUahDischargeScreenOff(int i);

    public abstract SparseArray<? extends Uid> getUidStats();

    public abstract Map<String, ? extends Timer> getWakeupReasonStats();

    public abstract long getWifiActiveTime(long j, int i);

    public abstract ControllerActivityCounter getWifiControllerActivity();

    public abstract int getWifiMulticastWakelockCount(int i);

    public abstract long getWifiMulticastWakelockTime(long j, int i);

    public abstract long getWifiOnTime(long j, int i);

    public abstract int getWifiSignalStrengthCount(int i, int i2);

    public abstract long getWifiSignalStrengthTime(int i, long j, int i2);

    public abstract Timer getWifiSignalStrengthTimer(int i);

    public abstract int getWifiStateCount(int i, int i2);

    public abstract long getWifiStateTime(int i, long j, int i2);

    public abstract Timer getWifiStateTimer(int i);

    public abstract int getWifiSupplStateCount(int i, int i2);

    public abstract long getWifiSupplStateTime(int i, long j, int i2);

    public abstract Timer getWifiSupplStateTimer(int i);

    public abstract boolean hasBluetoothActivityReporting();

    public abstract boolean hasModemActivityReporting();

    public abstract boolean hasWifiActivityReporting();

    public abstract boolean startIteratingHistoryLocked();

    public abstract boolean startIteratingOldHistoryLocked();

    public abstract void writeToParcelWithoutUids(Parcel parcel, int i);

    static {
        BitDescription bitDescription = new BitDescription(HistoryItem.STATE_DATA_CONNECTION_MASK, 9, "data_conn", "Pcn", DATA_CONNECTION_NAMES, DATA_CONNECTION_NAMES);
        BitDescription bitDescription2 = new BitDescription(448, 6, "phone_state", "Pst", new String[]{"in", "out", "emergency", "off"}, new String[]{"in", "out", "em", "off"});
        BitDescription bitDescription3 = new BitDescription(56, 3, "phone_signal_strength", "Pss", SignalStrength.SIGNAL_STRENGTH_NAMES, new String[]{WifiEnterpriseConfig.ENGINE_DISABLE, "1", WifiScanLog.EVENT_KEY2, WifiScanLog.EVENT_KEY3, WifiScanLog.EVENT_KEY4});
        BitDescription bitDescription4 = new BitDescription(7, 0, "brightness", "Sb", SCREEN_BRIGHTNESS_NAMES, SCREEN_BRIGHTNESS_SHORT_NAMES);
        HISTORY_STATE_DESCRIPTIONS = new BitDescription[]{new BitDescription(Integer.MIN_VALUE, "running", "r"), new BitDescription(1073741824, "wake_lock", "w"), new BitDescription(8388608, "sensor", "s"), new BitDescription(536870912, "gps", "g"), new BitDescription(268435456, "wifi_full_lock", "Wl"), new BitDescription(134217728, "wifi_scan", "Ws"), new BitDescription(65536, "wifi_multicast", "Wm"), new BitDescription(67108864, "wifi_radio", "Wr"), new BitDescription(33554432, "mobile_radio", "Pr"), new BitDescription(2097152, "phone_scanning", "Psc"), new BitDescription(4194304, "audio", "a"), new BitDescription(1048576, "screen", "S"), new BitDescription(524288, BatteryManager.EXTRA_PLUGGED, "BP"), new BitDescription(262144, "screen_doze", "Sd"), bitDescription, bitDescription2, bitDescription3, bitDescription4};
        BitDescription bitDescription5 = new BitDescription(HistoryItem.STATE2_DEVICE_IDLE_MASK, 25, "device_idle", "di", new String[]{"off", "light", "full", "???"}, new String[]{"off", "light", "full", "???"});
        BitDescription bitDescription6 = new BitDescription(112, 4, "wifi_signal_strength", "Wss", new String[]{WifiEnterpriseConfig.ENGINE_DISABLE, "1", WifiScanLog.EVENT_KEY2, WifiScanLog.EVENT_KEY3, WifiScanLog.EVENT_KEY4}, new String[]{WifiEnterpriseConfig.ENGINE_DISABLE, "1", WifiScanLog.EVENT_KEY2, WifiScanLog.EVENT_KEY3, WifiScanLog.EVENT_KEY4});
        BitDescription bitDescription7 = new BitDescription(15, 0, "wifi_suppl", "Wsp", WIFI_SUPPL_STATE_NAMES, WIFI_SUPPL_STATE_SHORT_NAMES);
        BitDescription bitDescription8 = new BitDescription(128, 7, "gps_signal_quality", "Gss", new String[]{"poor", "good"}, new String[]{"poor", "good"});
        HISTORY_STATE2_DESCRIPTIONS = new BitDescription[]{new BitDescription(Integer.MIN_VALUE, "power_save", "ps"), new BitDescription(1073741824, "video", Telephony.BaseMmsColumns.MMS_VERSION), new BitDescription(536870912, "wifi_running", "Ww"), new BitDescription(268435456, "wifi", "W"), new BitDescription(134217728, "flashlight", "fl"), bitDescription5, new BitDescription(16777216, "charging", "ch"), new BitDescription(262144, "usb_data", "Ud"), new BitDescription(8388608, "phone_in_call", "Pcl"), new BitDescription(4194304, "bluetooth", "b"), bitDescription6, bitDescription7, new BitDescription(2097152, "camera", "ca"), new BitDescription(1048576, "ble_scan", "bles"), new BitDescription(524288, "cellular_high_tx_power", "Chtp"), bitDescription8};
    }

    public static int mapToInternalProcessState(int procState) {
        if (procState == 19) {
            return 19;
        }
        if (procState == 2) {
            return 0;
        }
        if (procState == 3) {
            return 1;
        }
        if (procState <= 5) {
            return 2;
        }
        if (procState <= 10) {
            return 3;
        }
        if (procState <= 11) {
            return 4;
        }
        if (procState <= 12) {
            return 5;
        }
        return 6;
    }

    private static final void formatTimeRaw(StringBuilder out, long seconds) {
        long days = seconds / 86400;
        if (days != 0) {
            out.append(days);
            out.append("d ");
        }
        long used = days * 60 * 60 * 24;
        long hours = (seconds - used) / 3600;
        if (!(hours == 0 && used == 0)) {
            out.append(hours);
            out.append("h ");
        }
        long used2 = used + (hours * 60 * 60);
        long mins = (seconds - used2) / 60;
        if (!(mins == 0 && used2 == 0)) {
            out.append(mins);
            out.append("m ");
        }
        long used3 = used2 + (60 * mins);
        if (seconds != 0 || used3 != 0) {
            out.append(seconds - used3);
            out.append("s ");
        }
    }

    public static final void formatTimeMs(StringBuilder sb, long time) {
        long sec = time / 1000;
        formatTimeRaw(sb, sec);
        sb.append(time - (1000 * sec));
        sb.append("ms ");
    }

    public static final void formatTimeMsNoSpace(StringBuilder sb, long time) {
        long sec = time / 1000;
        formatTimeRaw(sb, sec);
        sb.append(time - (1000 * sec));
        sb.append("ms");
    }

    public final String formatRatioLocked(long num, long den) {
        if (den == 0) {
            return "--%";
        }
        this.mFormatBuilder.setLength(0);
        this.mFormatter.format("%.1f%%", new Object[]{Float.valueOf((((float) num) / ((float) den)) * 100.0f)});
        return this.mFormatBuilder.toString();
    }

    /* access modifiers changed from: package-private */
    public final String formatBytesLocked(long bytes) {
        this.mFormatBuilder.setLength(0);
        if (bytes < 1024) {
            return bytes + "B";
        } else if (bytes < 1048576) {
            this.mFormatter.format("%.2fKB", new Object[]{Double.valueOf(((double) bytes) / 1024.0d)});
            return this.mFormatBuilder.toString();
        } else if (bytes < BYTES_PER_GB) {
            this.mFormatter.format("%.2fMB", new Object[]{Double.valueOf(((double) bytes) / 1048576.0d)});
            return this.mFormatBuilder.toString();
        } else {
            this.mFormatter.format("%.2fGB", new Object[]{Double.valueOf(((double) bytes) / 1.073741824E9d)});
            return this.mFormatBuilder.toString();
        }
    }

    private static long roundUsToMs(long timeUs) {
        return (500 + timeUs) / 1000;
    }

    private static long computeWakeLock(Timer timer, long elapsedRealtimeUs, int which) {
        if (timer != null) {
            return (500 + timer.getTotalTimeLocked(elapsedRealtimeUs, which)) / 1000;
        }
        return 0;
    }

    private static final String printWakeLock(StringBuilder sb, Timer timer, long elapsedRealtimeUs, String name, int which, String linePrefix) {
        StringBuilder sb2 = sb;
        Timer timer2 = timer;
        long j = elapsedRealtimeUs;
        String str = name;
        int i = which;
        String str2 = linePrefix;
        if (timer2 != null) {
            long totalTimeMillis = computeWakeLock(timer2, j, i);
            int count = timer2.getCountLocked(i);
            if (totalTimeMillis != 0) {
                sb2.append(str2);
                formatTimeMs(sb2, totalTimeMillis);
                if (str != null) {
                    sb2.append(str);
                    sb2.append(' ');
                }
                sb2.append('(');
                sb2.append(count);
                sb2.append(" times)");
                long maxDurationMs = timer2.getMaxDurationMsLocked(j / 1000);
                if (maxDurationMs >= 0) {
                    sb2.append(" max=");
                    sb2.append(maxDurationMs);
                }
                long totalDurMs = timer2.getTotalDurationMsLocked(j / 1000);
                if (totalDurMs > totalTimeMillis) {
                    sb2.append(" actual=");
                    sb2.append(totalDurMs);
                }
                if (timer.isRunningLocked()) {
                    long currentMs = timer2.getCurrentDurationMsLocked(j / 1000);
                    if (currentMs >= 0) {
                        sb2.append(" (running for ");
                        sb2.append(currentMs);
                        sb2.append("ms)");
                    } else {
                        sb2.append(" (running)");
                    }
                }
                return ", ";
            }
        }
        return str2;
    }

    private static final boolean printTimer(PrintWriter pw, StringBuilder sb, Timer timer, long rawRealtimeUs, int which, String prefix, String type) {
        StringBuilder sb2 = sb;
        Timer timer2 = timer;
        if (timer2 != null) {
            long totalTimeMs = (timer.getTotalTimeLocked(rawRealtimeUs, which) + 500) / 1000;
            int count = timer2.getCountLocked(which);
            if (totalTimeMs != 0) {
                sb2.setLength(0);
                sb2.append(prefix);
                sb2.append("    ");
                sb2.append(type);
                sb2.append(": ");
                formatTimeMs(sb2, totalTimeMs);
                sb2.append("realtime (");
                sb2.append(count);
                sb2.append(" times)");
                long maxDurationMs = timer2.getMaxDurationMsLocked(rawRealtimeUs / 1000);
                if (maxDurationMs >= 0) {
                    sb2.append(" max=");
                    sb2.append(maxDurationMs);
                }
                if (timer.isRunningLocked()) {
                    long currentMs = timer2.getCurrentDurationMsLocked(rawRealtimeUs / 1000);
                    if (currentMs >= 0) {
                        sb2.append(" (running for ");
                        sb2.append(currentMs);
                        sb2.append("ms)");
                    } else {
                        sb2.append(" (running)");
                    }
                }
                pw.println(sb.toString());
                return true;
            }
            PrintWriter printWriter = pw;
        } else {
            PrintWriter printWriter2 = pw;
            int i = which;
        }
        String str = prefix;
        String str2 = type;
        return false;
    }

    private static final String printWakeLockCheckin(StringBuilder sb, Timer timer, long elapsedRealtimeUs, String name, int which, String linePrefix) {
        long totalTimeMicros;
        String str;
        StringBuilder sb2 = sb;
        Timer timer2 = timer;
        long j = elapsedRealtimeUs;
        String str2 = name;
        int i = which;
        int count = 0;
        long max = 0;
        long current = 0;
        long totalDuration = 0;
        if (timer2 != null) {
            long totalTimeMicros2 = timer2.getTotalTimeLocked(j, i);
            count = timer2.getCountLocked(i);
            totalTimeMicros = totalTimeMicros2;
            current = timer2.getCurrentDurationMsLocked(j / 1000);
            max = timer2.getMaxDurationMsLocked(j / 1000);
            totalDuration = timer2.getTotalDurationMsLocked(j / 1000);
        } else {
            totalTimeMicros = 0;
        }
        sb2.append(linePrefix);
        sb2.append((totalTimeMicros + 500) / 1000);
        sb2.append(',');
        if (str2 != null) {
            str = str2 + ",";
        } else {
            str = "";
        }
        sb2.append(str);
        sb2.append(count);
        sb2.append(',');
        sb2.append(current);
        sb2.append(',');
        sb2.append(max);
        if (str2 != null) {
            sb2.append(',');
            sb2.append(totalDuration);
        }
        return ",";
    }

    private static final void dumpLineHeader(PrintWriter pw, int uid, String category, String type) {
        pw.print(9);
        pw.print(',');
        pw.print(uid);
        pw.print(',');
        pw.print(category);
        pw.print(',');
        pw.print(type);
    }

    private static final void dumpLine(PrintWriter pw, int uid, String category, String type, Object... args) {
        dumpLineHeader(pw, uid, category, type);
        for (Object arg : args) {
            pw.print(',');
            pw.print(arg);
        }
        pw.println();
    }

    private static final void dumpTimer(PrintWriter pw, int uid, String category, String type, Timer timer, long rawRealtime, int which) {
        if (timer != null) {
            long totalTime = roundUsToMs(timer.getTotalTimeLocked(rawRealtime, which));
            int count = timer.getCountLocked(which);
            if (totalTime != 0 || count != 0) {
                dumpLine(pw, uid, category, type, Long.valueOf(totalTime), Integer.valueOf(count));
            }
        }
    }

    private static void dumpTimer(ProtoOutputStream proto, long fieldId, Timer timer, long rawRealtimeUs, int which) {
        ProtoOutputStream protoOutputStream = proto;
        Timer timer2 = timer;
        if (timer2 != null) {
            long timeMs = roundUsToMs(timer.getTotalTimeLocked(rawRealtimeUs, which));
            int count = timer2.getCountLocked(which);
            long maxDurationMs = timer2.getMaxDurationMsLocked(rawRealtimeUs / 1000);
            long curDurationMs = timer2.getCurrentDurationMsLocked(rawRealtimeUs / 1000);
            long totalDurationMs = timer2.getTotalDurationMsLocked(rawRealtimeUs / 1000);
            if (!(timeMs == 0 && count == 0 && maxDurationMs == -1 && curDurationMs == -1 && totalDurationMs == -1)) {
                long token = proto.start(fieldId);
                protoOutputStream.write(1112396529665L, timeMs);
                protoOutputStream.write(1112396529666L, count);
                if (maxDurationMs != -1) {
                    protoOutputStream.write(1112396529667L, maxDurationMs);
                }
                if (curDurationMs != -1) {
                    protoOutputStream.write(1112396529668L, curDurationMs);
                }
                if (totalDurationMs != -1) {
                    protoOutputStream.write(1112396529669L, totalDurationMs);
                }
                protoOutputStream.end(token);
            }
        }
    }

    private static boolean controllerActivityHasData(ControllerActivityCounter counter, int which) {
        if (counter == null) {
            return false;
        }
        if (counter.getIdleTimeCounter().getCountLocked(which) != 0 || counter.getRxTimeCounter().getCountLocked(which) != 0 || counter.getPowerCounter().getCountLocked(which) != 0) {
            return true;
        }
        for (LongCounter c : counter.getTxTimeCounters()) {
            if (c.getCountLocked(which) != 0) {
                return true;
            }
        }
        return false;
    }

    private static final void dumpControllerActivityLine(PrintWriter pw, int uid, String category, String type, ControllerActivityCounter counter, int which) {
        if (controllerActivityHasData(counter, which)) {
            dumpLineHeader(pw, uid, category, type);
            pw.print(",");
            pw.print(counter.getIdleTimeCounter().getCountLocked(which));
            pw.print(",");
            pw.print(counter.getRxTimeCounter().getCountLocked(which));
            pw.print(",");
            pw.print(counter.getPowerCounter().getCountLocked(which) / DateUtils.HOUR_IN_MILLIS);
            for (LongCounter c : counter.getTxTimeCounters()) {
                pw.print(",");
                pw.print(c.getCountLocked(which));
            }
            pw.println();
        }
    }

    private static void dumpControllerActivityProto(ProtoOutputStream proto, long fieldId, ControllerActivityCounter counter, int which) {
        if (controllerActivityHasData(counter, which)) {
            long cToken = proto.start(fieldId);
            proto.write(1112396529665L, counter.getIdleTimeCounter().getCountLocked(which));
            proto.write(1112396529666L, counter.getRxTimeCounter().getCountLocked(which));
            proto.write(1112396529667L, counter.getPowerCounter().getCountLocked(which) / DateUtils.HOUR_IN_MILLIS);
            LongCounter[] txCounters = counter.getTxTimeCounters();
            for (int i = 0; i < txCounters.length; i++) {
                LongCounter c = txCounters[i];
                long tToken = proto.start(2246267895812L);
                proto.write(1120986464257L, i);
                proto.write(1112396529666L, c.getCountLocked(which));
                proto.end(tToken);
            }
            proto.end(cToken);
        }
    }

    private final void printControllerActivityIfInteresting(PrintWriter pw, StringBuilder sb, String prefix, String controllerName, ControllerActivityCounter counter, int which) {
        if (controllerActivityHasData(counter, which)) {
            printControllerActivity(pw, sb, prefix, controllerName, counter, which);
        }
    }

    private final void printControllerActivity(PrintWriter pw, StringBuilder sb, String prefix, String controllerName, ControllerActivityCounter counter, int which) {
        String[] powerLevel;
        PrintWriter printWriter = pw;
        StringBuilder sb2 = sb;
        String str = controllerName;
        int i = which;
        long idleTimeMs = counter.getIdleTimeCounter().getCountLocked(i);
        long rxTimeMs = counter.getRxTimeCounter().getCountLocked(i);
        long powerDrainMaMs = counter.getPowerCounter().getCountLocked(i);
        long totalControllerActivityTimeMs = computeBatteryRealtime(SystemClock.elapsedRealtime() * 1000, i) / 1000;
        LongCounter[] txTimeCounters = counter.getTxTimeCounters();
        long totalTxTimeMs = 0;
        int i2 = 0;
        for (int length = txTimeCounters.length; i2 < length; length = length) {
            totalTxTimeMs += txTimeCounters[i2].getCountLocked(i);
            i2++;
        }
        if (str.equals(WIFI_CONTROLLER_NAME)) {
            long scanTimeMs = counter.getScanTimeCounter().getCountLocked(i);
            sb2.setLength(0);
            sb.append(prefix);
            sb2.append("     ");
            sb2.append(str);
            sb2.append(" Scan time:  ");
            formatTimeMs(sb2, scanTimeMs);
            sb2.append("(");
            sb2.append(formatRatioLocked(scanTimeMs, totalControllerActivityTimeMs));
            sb2.append(")");
            printWriter.println(sb.toString());
            long j = scanTimeMs;
            long scanTimeMs2 = totalControllerActivityTimeMs - ((idleTimeMs + rxTimeMs) + totalTxTimeMs);
            sb2.setLength(0);
            sb.append(prefix);
            sb2.append("     ");
            sb2.append(str);
            sb2.append(" Sleep time:  ");
            formatTimeMs(sb2, scanTimeMs2);
            sb2.append("(");
            sb2.append(formatRatioLocked(scanTimeMs2, totalControllerActivityTimeMs));
            sb2.append(")");
            printWriter.println(sb.toString());
        }
        if (str.equals(CELLULAR_CONTROLLER_NAME)) {
            long sleepTimeMs = counter.getSleepTimeCounter().getCountLocked(i);
            sb2.setLength(0);
            sb.append(prefix);
            sb2.append("     ");
            sb2.append(str);
            sb2.append(" Sleep time:  ");
            formatTimeMs(sb2, sleepTimeMs);
            sb2.append("(");
            sb2.append(formatRatioLocked(sleepTimeMs, totalControllerActivityTimeMs));
            sb2.append(")");
            printWriter.println(sb.toString());
        }
        sb2.setLength(0);
        sb.append(prefix);
        sb2.append("     ");
        sb2.append(str);
        sb2.append(" Idle time:   ");
        formatTimeMs(sb2, idleTimeMs);
        sb2.append("(");
        sb2.append(formatRatioLocked(idleTimeMs, totalControllerActivityTimeMs));
        sb2.append(")");
        printWriter.println(sb.toString());
        sb2.setLength(0);
        sb.append(prefix);
        sb2.append("     ");
        sb2.append(str);
        sb2.append(" Rx time:     ");
        formatTimeMs(sb2, rxTimeMs);
        sb2.append("(");
        sb2.append(formatRatioLocked(rxTimeMs, totalControllerActivityTimeMs));
        sb2.append(")");
        printWriter.println(sb.toString());
        sb2.setLength(0);
        sb.append(prefix);
        sb2.append("     ");
        sb2.append(str);
        sb2.append(" Tx time:     ");
        char c = 65535;
        if (controllerName.hashCode() == -851952246 && str.equals(CELLULAR_CONTROLLER_NAME)) {
            c = 0;
        }
        if (c != 0) {
            long j2 = idleTimeMs;
            powerLevel = new String[]{"[0]", "[1]", "[2]", "[3]", "[4]"};
        } else {
            powerLevel = new String[]{"   less than 0dBm: ", "   0dBm to 8dBm: ", "   8dBm to 15dBm: ", "   15dBm to 20dBm: ", "   above 20dBm: "};
        }
        int numTxLvls = Math.min(counter.getTxTimeCounters().length, powerLevel.length);
        if (numTxLvls > 1) {
            printWriter.println(sb.toString());
            int lvl = 0;
            while (lvl < numTxLvls) {
                long txLvlTimeMs = counter.getTxTimeCounters()[lvl].getCountLocked(i);
                sb2.setLength(0);
                sb.append(prefix);
                sb2.append("    ");
                sb2.append(powerLevel[lvl]);
                sb2.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                formatTimeMs(sb2, txLvlTimeMs);
                sb2.append("(");
                sb2.append(formatRatioLocked(txLvlTimeMs, totalControllerActivityTimeMs));
                sb2.append(")");
                printWriter.println(sb.toString());
                lvl++;
                numTxLvls = numTxLvls;
            }
        } else {
            long txLvlTimeMs2 = counter.getTxTimeCounters()[0].getCountLocked(i);
            formatTimeMs(sb2, txLvlTimeMs2);
            sb2.append("(");
            sb2.append(formatRatioLocked(txLvlTimeMs2, totalControllerActivityTimeMs));
            sb2.append(")");
            printWriter.println(sb.toString());
        }
        if (powerDrainMaMs > 0) {
            sb2.setLength(0);
            sb.append(prefix);
            sb2.append("     ");
            sb2.append(str);
            sb2.append(" Battery drain: ");
            sb2.append(BatteryStatsHelper.makemAh(((double) powerDrainMaMs) / 3600000.0d));
            sb2.append("mAh");
            printWriter.println(sb.toString());
        }
    }

    public final void dumpCheckinLocked(Context context, PrintWriter pw, int which, int reqUid) {
        dumpCheckinLocked(context, pw, which, reqUid, BatteryStatsHelper.checkWifiOnly(context));
    }

    /* JADX WARNING: Removed duplicated region for block: B:201:0x0d1e  */
    /* JADX WARNING: Removed duplicated region for block: B:237:0x0e5f  */
    /* JADX WARNING: Removed duplicated region for block: B:240:0x0e8f  */
    /* JADX WARNING: Removed duplicated region for block: B:251:0x0eb9  */
    /* JADX WARNING: Removed duplicated region for block: B:256:0x0eea  */
    /* JADX WARNING: Removed duplicated region for block: B:260:0x0ef9  */
    /* JADX WARNING: Removed duplicated region for block: B:490:0x0fbd A[SYNTHETIC] */
    public final void dumpCheckinLocked(Context context, PrintWriter pw, int which, int reqUid, boolean wifiOnly) {
        long batteryUptime;
        Object obj;
        int multicastWakeLockCountTotal;
        Object[] args;
        int i;
        long rawRealtimeMs;
        long batteryUptime2;
        StringBuilder sb;
        List<BatterySipper> sippers;
        StringBuilder sb2;
        StringBuilder sb3;
        StringBuilder sb4;
        long[] cpuFreqs;
        long rawRealtime;
        long rawRealtimeMs2;
        int iu;
        int NU;
        List<BatterySipper> sippers2;
        SparseArray<? extends Uid> uidStats;
        long batteryUptime3;
        PrintWriter printWriter;
        int i2;
        String category;
        long wifiPacketsBgTx;
        long mobilePacketsBgTx;
        long mobilePacketsBgRx;
        long wifiBytesBgTx;
        long wifiBytesBgRx;
        long mobileBytesBgTx;
        long mobileBytesBgRx;
        long wifiWakeup;
        long mobileWakeup;
        long btBytesTx;
        long btBytesRx;
        long mobilePacketsTx;
        long mobileActiveTime;
        long wifiPacketsTx;
        long wifiPacketsRx;
        long wifiBytesTx;
        char c;
        char c2;
        char c3;
        String category2;
        PrintWriter printWriter2;
        long wifiBytesTx2;
        int uid;
        long fullWifiLockOnTime;
        Uid u;
        long rawRealtime2;
        PrintWriter printWriter3;
        String category3;
        Timer bleTimer;
        Uid u2;
        long uidWifiRunningTime;
        Timer bleTimer2;
        long rawRealtime3;
        long rawRealtimeMs3;
        String category4;
        Uid u3;
        Timer bleTimer3;
        int iw;
        int iw2;
        long rawRealtime4;
        int i3;
        long[] cpuFreqs2;
        StringBuilder sb5;
        int isvc;
        ArrayMap<String, ? extends Counter> alarms;
        Uid.Pkg ps;
        int uid2;
        SparseArray<? extends Uid.Sensor> sensors;
        long rawRealtimeMs4;
        long rawRealtime5;
        SparseArray<? extends Uid.Sensor> sensors2;
        int NSE;
        StringBuilder sb6;
        String category5;
        ArrayMap<String, ? extends Timer> syncs;
        long rawRealtime6;
        long j;
        long rawRealtimeMs5;
        long rawRealtimeMs6;
        StringBuilder sb7;
        ArrayMap<String, ? extends Uid.Wakelock> wakelocks;
        long j2;
        long rawRealtimeMs7;
        long rawRealtimeMs8;
        int resultCountBg;
        long wifiScanActualTimeMsBg;
        String label;
        long rawRealtimeMs9;
        long rawRealtimeMs10;
        PrintWriter printWriter4 = pw;
        int i4 = which;
        int i5 = reqUid;
        long rawUptime = SystemClock.uptimeMillis() * 1000;
        long rawRealtime7 = SystemClock.elapsedRealtime();
        long rawRealtime8 = rawRealtime7 * 1000;
        long batteryUptime4 = getBatteryUptime(rawUptime);
        long whichBatteryUptime = computeBatteryUptime(rawUptime, i4);
        long whichBatteryRealtime = computeBatteryRealtime(rawRealtime8, i4);
        long whichBatteryScreenOffUptime = computeBatteryScreenOffUptime(rawUptime, i4);
        long whichBatteryScreenOffRealtime = computeBatteryScreenOffRealtime(rawRealtime8, i4);
        long totalRealtime = computeRealtime(rawRealtime8, i4);
        long totalUptime = computeUptime(rawUptime, i4);
        long screenOnTime = getScreenOnTime(rawRealtime8, i4);
        long screenDozeTime = getScreenDozeTime(rawRealtime8, i4);
        long interactiveTime = getInteractiveTime(rawRealtime8, i4);
        long powerSaveModeEnabledTime = getPowerSaveModeEnabledTime(rawRealtime8, i4);
        long deviceIdleModeLightTime = getDeviceIdleModeTime(1, rawRealtime8, i4);
        long deviceIdleModeFullTime = getDeviceIdleModeTime(2, rawRealtime8, i4);
        long deviceLightIdlingTime = getDeviceIdlingTime(1, rawRealtime8, i4);
        long deviceIdlingTime = getDeviceIdlingTime(2, rawRealtime8, i4);
        int connChanges = getNumConnectivityChange(i4);
        long phoneOnTime = getPhoneOnTime(rawRealtime8, i4);
        long dischargeCount = getUahDischarge(i4);
        long dischargeScreenOffCount = getUahDischargeScreenOff(i4);
        long dischargeScreenDozeCount = getUahDischargeScreenDoze(i4);
        long dischargeLightDozeCount = getUahDischargeLightDoze(i4);
        long dischargeDeepDozeCount = getUahDischargeDeepDoze(i4);
        StringBuilder sb8 = new StringBuilder(128);
        SparseArray<? extends Uid> uidStats2 = getUidStats();
        int NU2 = uidStats2.size();
        long j3 = rawUptime;
        String category6 = STAT_NAMES[i4];
        int connChanges2 = connChanges;
        Object[] objArr = new Object[12];
        if (i4 == 0) {
            batteryUptime = batteryUptime4;
            obj = Integer.valueOf(getStartCount());
        } else {
            batteryUptime = batteryUptime4;
            obj = "N/A";
        }
        objArr[0] = obj;
        long rawRealtime9 = rawRealtime8;
        objArr[1] = Long.valueOf(whichBatteryRealtime / 1000);
        objArr[2] = Long.valueOf(whichBatteryUptime / 1000);
        objArr[3] = Long.valueOf(totalRealtime / 1000);
        objArr[4] = Long.valueOf(totalUptime / 1000);
        objArr[5] = Long.valueOf(getStartClockTime());
        objArr[6] = Long.valueOf(whichBatteryScreenOffRealtime / 1000);
        objArr[7] = Long.valueOf(whichBatteryScreenOffUptime / 1000);
        objArr[8] = Integer.valueOf(getEstimatedBatteryCapacity());
        objArr[9] = Integer.valueOf(getMinLearnedBatteryCapacity());
        objArr[10] = Integer.valueOf(getMaxLearnedBatteryCapacity());
        objArr[11] = Long.valueOf(screenDozeTime / 1000);
        dumpLine(printWriter4, 0, category6, BATTERY_DATA, objArr);
        long partialWakeLockTimeTotal = 0;
        int iu2 = 0;
        long fullWakeLockTimeTotal = 0;
        while (iu2 < NU2) {
            ArrayMap<String, ? extends Uid.Wakelock> wakelocks2 = ((Uid) uidStats2.valueAt(iu2)).getWakelockStats();
            int i6 = 1;
            int iw3 = wakelocks2.size() - 1;
            while (iw3 >= 0) {
                Uid.Wakelock wl = (Uid.Wakelock) wakelocks2.valueAt(iw3);
                int NU3 = NU2;
                Timer fullWakeTimer = wl.getWakeTime(i6);
                if (fullWakeTimer != null) {
                    rawRealtimeMs9 = rawRealtime7;
                    rawRealtimeMs10 = rawRealtime9;
                    fullWakeLockTimeTotal += fullWakeTimer.getTotalTimeLocked(rawRealtimeMs10, i4);
                } else {
                    rawRealtimeMs9 = rawRealtime7;
                    rawRealtimeMs10 = rawRealtime9;
                }
                Timer timer = fullWakeTimer;
                Timer fullWakeTimer2 = wl.getWakeTime(0);
                if (fullWakeTimer2 != null) {
                    partialWakeLockTimeTotal += fullWakeTimer2.getTotalTimeLocked(rawRealtimeMs10, i4);
                }
                iw3--;
                rawRealtime9 = rawRealtimeMs10;
                NU2 = NU3;
                rawRealtime7 = rawRealtimeMs9;
                i6 = 1;
            }
            long rawRealtimeMs11 = rawRealtime7;
            long rawRealtimeMs12 = rawRealtime9;
            iu2++;
            rawRealtime7 = rawRealtimeMs11;
        }
        int NU4 = NU2;
        long rawRealtimeMs13 = rawRealtime7;
        long mobileRxTotalBytes = getNetworkActivityBytes(0, i4);
        long wifiRxTotalBytes = getNetworkActivityBytes(2, i4);
        long wifiTxTotalBytes = getNetworkActivityBytes(3, i4);
        long mobileRxTotalPackets = getNetworkActivityPackets(0, i4);
        StringBuilder sb9 = sb8;
        SparseArray<? extends Uid> uidStats3 = uidStats2;
        long wifiRxTotalPackets = getNetworkActivityPackets(2, i4);
        long wifiTxTotalPackets = getNetworkActivityPackets(3, i4);
        long btRxTotalBytes = getNetworkActivityBytes(4, i4);
        long btTxTotalBytes = getNetworkActivityBytes(5, i4);
        long mobileRxTotalBytes2 = mobileRxTotalBytes;
        long mobileRxTotalBytes3 = mobileRxTotalBytes2;
        long mobileTxTotalBytes = getNetworkActivityBytes(1, i4);
        long wifiRxTotalBytes2 = wifiRxTotalBytes;
        long wifiRxTotalBytes3 = wifiTxTotalBytes;
        long wifiTxTotalBytes2 = wifiRxTotalBytes3;
        long wifiTxTotalBytes3 = getNetworkActivityPackets(1, i4);
        long mobileTxTotalPackets = wifiTxTotalBytes3;
        long mobileTxTotalPackets2 = wifiTxTotalPackets;
        long wifiTxTotalPackets2 = mobileTxTotalPackets2;
        long btRxTotalBytes2 = btRxTotalBytes;
        String category7 = category6;
        dumpLine(printWriter4, 0, category7, GLOBAL_NETWORK_DATA, Long.valueOf(mobileRxTotalBytes2), Long.valueOf(mobileTxTotalBytes), Long.valueOf(wifiRxTotalBytes), Long.valueOf(wifiRxTotalBytes3), Long.valueOf(mobileRxTotalPackets), Long.valueOf(wifiTxTotalBytes3), Long.valueOf(wifiRxTotalPackets), Long.valueOf(mobileTxTotalPackets2), Long.valueOf(btRxTotalBytes2), Long.valueOf(btTxTotalBytes));
        long wifiRxTotalPackets2 = wifiRxTotalPackets;
        int connChanges3 = connChanges2;
        int NU5 = NU4;
        long j4 = wifiRxTotalBytes2;
        long j5 = wifiTxTotalBytes2;
        long j6 = mobileTxTotalPackets;
        long j7 = wifiTxTotalPackets2;
        long j8 = btRxTotalBytes2;
        long j9 = mobileTxTotalBytes;
        long j10 = mobileRxTotalPackets;
        long j11 = btTxTotalBytes;
        long rawRealtime10 = rawRealtime9;
        long j12 = mobileRxTotalBytes3;
        long rawRealtimeMs14 = batteryUptime;
        dumpControllerActivityLine(printWriter4, 0, category7, GLOBAL_MODEM_CONTROLLER_DATA, getModemControllerActivity(), i4);
        String category8 = category7;
        dumpLine(printWriter4, 0, category8, GLOBAL_WIFI_DATA, Long.valueOf(getWifiOnTime(rawRealtime10, i4) / 1000), Long.valueOf(getGlobalWifiRunningTime(rawRealtime10, i4) / 1000), 0, 0, 0);
        PrintWriter printWriter5 = printWriter4;
        String str = category8;
        String category9 = category8;
        int i7 = i4;
        dumpControllerActivityLine(printWriter5, 0, str, GLOBAL_WIFI_CONTROLLER_DATA, getWifiControllerActivity(), i7);
        dumpControllerActivityLine(printWriter5, 0, category9, GLOBAL_BLUETOOTH_CONTROLLER_DATA, getBluetoothControllerActivity(), i7);
        String category10 = category9;
        dumpLine(printWriter4, 0, category10, MISC_DATA, Long.valueOf(screenOnTime / 1000), Long.valueOf(phoneOnTime / 1000), Long.valueOf(fullWakeLockTimeTotal / 1000), Long.valueOf(partialWakeLockTimeTotal / 1000), Long.valueOf(getMobileRadioActiveTime(rawRealtime10, i4) / 1000), Long.valueOf(getMobileRadioActiveAdjustedTime(i4) / 1000), Long.valueOf(interactiveTime / 1000), Long.valueOf(powerSaveModeEnabledTime / 1000), Integer.valueOf(connChanges3), Long.valueOf(deviceIdleModeFullTime / 1000), Integer.valueOf(getDeviceIdleModeCount(2, i4)), Long.valueOf(deviceIdlingTime / 1000), Integer.valueOf(getDeviceIdlingCount(2, i4)), Integer.valueOf(getMobileRadioActiveCount(i4)), Long.valueOf(getMobileRadioActiveUnknownTime(i4) / 1000), Long.valueOf(deviceIdleModeLightTime / 1000), Integer.valueOf(getDeviceIdleModeCount(1, i4)), Long.valueOf(deviceLightIdlingTime / 1000), Integer.valueOf(getDeviceIdlingCount(1, i4)), Long.valueOf(getLongestDeviceIdleModeTime(1)), Long.valueOf(getLongestDeviceIdleModeTime(2)));
        Object[] args2 = new Object[5];
        int i8 = 0;
        for (int i9 = 5; i8 < i9; i9 = 5) {
            args2[i8] = Long.valueOf(getScreenBrightnessTime(i8, rawRealtime10, i4) / 1000);
            i8++;
        }
        dumpLine(printWriter4, 0, category10, SCREEN_BRIGHTNESS_DATA, args2);
        Object[] args3 = new Object[5];
        int i10 = 0;
        for (int i11 = 5; i10 < i11; i11 = 5) {
            args3[i10] = Long.valueOf(getPhoneSignalStrengthTime(i10, rawRealtime10, i4) / 1000);
            i10++;
        }
        dumpLine(printWriter4, 0, category10, SIGNAL_STRENGTH_TIME_DATA, args3);
        dumpLine(printWriter4, 0, category10, SIGNAL_SCANNING_TIME_DATA, Long.valueOf(getPhoneSignalScanningTime(rawRealtime10, i4) / 1000));
        for (int i12 = 0; i12 < 5; i12++) {
            args3[i12] = Integer.valueOf(getPhoneSignalStrengthCount(i12, i4));
        }
        dumpLine(printWriter4, 0, category10, SIGNAL_STRENGTH_COUNT_DATA, args3);
        Object[] args4 = new Object[21];
        for (int i13 = 0; i13 < 21; i13++) {
            args4[i13] = Long.valueOf(getPhoneDataConnectionTime(i13, rawRealtime10, i4) / 1000);
        }
        dumpLine(printWriter4, 0, category10, DATA_CONNECTION_TIME_DATA, args4);
        for (int i14 = 0; i14 < 21; i14++) {
            args4[i14] = Integer.valueOf(getPhoneDataConnectionCount(i14, i4));
        }
        dumpLine(printWriter4, 0, category10, DATA_CONNECTION_COUNT_DATA, args4);
        Object[] args5 = new Object[8];
        int i15 = 0;
        for (int i16 = 8; i15 < i16; i16 = 8) {
            args5[i15] = Long.valueOf(getWifiStateTime(i15, rawRealtime10, i4) / 1000);
            i15++;
        }
        dumpLine(printWriter4, 0, category10, WIFI_STATE_TIME_DATA, args5);
        for (int i17 = 0; i17 < 8; i17++) {
            args5[i17] = Integer.valueOf(getWifiStateCount(i17, i4));
        }
        dumpLine(printWriter4, 0, category10, WIFI_STATE_COUNT_DATA, args5);
        Object[] args6 = new Object[13];
        for (int i18 = 0; i18 < 13; i18++) {
            args6[i18] = Long.valueOf(getWifiSupplStateTime(i18, rawRealtime10, i4) / 1000);
        }
        dumpLine(printWriter4, 0, category10, WIFI_SUPPL_STATE_TIME_DATA, args6);
        for (int i19 = 0; i19 < 13; i19++) {
            args6[i19] = Integer.valueOf(getWifiSupplStateCount(i19, i4));
        }
        dumpLine(printWriter4, 0, category10, WIFI_SUPPL_STATE_COUNT_DATA, args6);
        Object[] args7 = new Object[5];
        int i20 = 0;
        for (int i21 = 5; i20 < i21; i21 = 5) {
            args7[i20] = Long.valueOf(getWifiSignalStrengthTime(i20, rawRealtime10, i4) / 1000);
            i20++;
        }
        dumpLine(printWriter4, 0, category10, WIFI_SIGNAL_STRENGTH_TIME_DATA, args7);
        for (int i22 = 0; i22 < 5; i22++) {
            args7[i22] = Integer.valueOf(getWifiSignalStrengthCount(i22, i4));
        }
        dumpLine(printWriter4, 0, category10, WIFI_SIGNAL_STRENGTH_COUNT_DATA, args7);
        long multicastWakeLockTimeTotalMicros = getWifiMulticastWakelockTime(rawRealtime10, i4);
        int multicastWakeLockCountTotal2 = getWifiMulticastWakelockCount(i4);
        dumpLine(printWriter4, 0, category10, WIFI_MULTICAST_TOTAL_DATA, Long.valueOf(multicastWakeLockTimeTotalMicros / 1000), Integer.valueOf(multicastWakeLockCountTotal2));
        if (i4 == 2) {
            dumpLine(printWriter4, 0, category10, BATTERY_LEVEL_DATA, Integer.valueOf(getDischargeStartLevel()), Integer.valueOf(getDischargeCurrentLevel()));
        }
        if (i4 == 2) {
            multicastWakeLockCountTotal = multicastWakeLockCountTotal2;
            args = args7;
            dumpLine(printWriter4, 0, category10, BATTERY_DISCHARGE_DATA, Integer.valueOf(getDischargeStartLevel() - getDischargeCurrentLevel()), Integer.valueOf(getDischargeStartLevel() - getDischargeCurrentLevel()), Integer.valueOf(getDischargeAmountScreenOn()), Integer.valueOf(getDischargeAmountScreenOff()), Long.valueOf(dischargeCount / 1000), Long.valueOf(dischargeScreenOffCount / 1000), Integer.valueOf(getDischargeAmountScreenDoze()), Long.valueOf(dischargeScreenDozeCount / 1000), Long.valueOf(dischargeLightDozeCount / 1000), Long.valueOf(dischargeDeepDozeCount / 1000));
            i = 3;
        } else {
            multicastWakeLockCountTotal = multicastWakeLockCountTotal2;
            args = args7;
            i = 3;
            category10 = category10;
            dumpLine(printWriter4, 0, category10, BATTERY_DISCHARGE_DATA, Integer.valueOf(getLowDischargeAmountSinceCharge()), Integer.valueOf(getHighDischargeAmountSinceCharge()), Integer.valueOf(getDischargeAmountScreenOnSinceCharge()), Integer.valueOf(getDischargeAmountScreenOffSinceCharge()), Long.valueOf(dischargeCount / 1000), Long.valueOf(dischargeScreenOffCount / 1000), Integer.valueOf(getDischargeAmountScreenDozeSinceCharge()), Long.valueOf(dischargeScreenDozeCount / 1000), Long.valueOf(dischargeLightDozeCount / 1000), Long.valueOf(dischargeDeepDozeCount / 1000));
        }
        long j13 = wifiRxTotalPackets2;
        int i23 = reqUid;
        if (i23 < 0) {
            Map<String, ? extends Timer> kernelWakelocks = getKernelWakelockStats();
            if (kernelWakelocks.size() > 0) {
                Iterator<Map.Entry<String, ? extends Timer>> it = kernelWakelocks.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, ? extends Timer> ent = it.next();
                    StringBuilder sb10 = sb9;
                    sb10.setLength(0);
                    int i24 = i23;
                    int i25 = multicastWakeLockCountTotal;
                    int connChanges4 = connChanges3;
                    long batteryUptime5 = rawRealtimeMs14;
                    int connChanges5 = i;
                    long batteryUptime6 = rawRealtimeMs13;
                    Object[] objArr2 = args;
                    printWakeLockCheckin(sb10, (Timer) ent.getValue(), rawRealtime10, null, i4, "");
                    StringBuilder sb11 = sb10;
                    dumpLine(printWriter4, 0, category10, KERNEL_WAKELOCK_DATA, "\"" + ent.getKey() + "\"", sb11.toString());
                    sb9 = sb11;
                    kernelWakelocks = kernelWakelocks;
                    it = it;
                    rawRealtimeMs14 = batteryUptime5;
                    i23 = reqUid;
                    i = connChanges5;
                    connChanges3 = connChanges4;
                }
            }
            int i26 = connChanges3;
            batteryUptime2 = rawRealtimeMs14;
            int connChanges6 = i;
            long rawRealtimeMs15 = rawRealtimeMs13;
            sb = sb9;
            Object[] objArr3 = args;
            int i27 = multicastWakeLockCountTotal;
            Map<String, ? extends Timer> wakeupReasons = getWakeupReasonStats();
            if (wakeupReasons.size() > 0) {
                Iterator<Map.Entry<String, ? extends Timer>> it2 = wakeupReasons.entrySet().iterator();
                while (it2.hasNext()) {
                    Map.Entry<String, ? extends Timer> ent2 = it2.next();
                    long totalTimeMicros = ((Timer) ent2.getValue()).getTotalTimeLocked(rawRealtime10, i4);
                    int count = ((Timer) ent2.getValue()).getCountLocked(i4);
                    Iterator<Map.Entry<String, ? extends Timer>> it3 = it2;
                    Object[] objArr4 = new Object[connChanges6];
                    objArr4[0] = "\"" + ent2.getKey() + "\"";
                    objArr4[1] = Long.valueOf((totalTimeMicros + 500) / 1000);
                    objArr4[2] = Integer.valueOf(count);
                    dumpLine(printWriter4, 0, category10, WAKEUP_REASON_DATA, objArr4);
                    wakeupReasons = wakeupReasons;
                    it2 = it3;
                    rawRealtimeMs15 = rawRealtimeMs15;
                    connChanges6 = 3;
                }
            }
            rawRealtimeMs = rawRealtimeMs15;
        } else {
            batteryUptime2 = rawRealtimeMs14;
            rawRealtimeMs = rawRealtimeMs13;
            sb = sb9;
            Object[] objArr5 = args;
            int i28 = multicastWakeLockCountTotal;
        }
        Map<String, ? extends Timer> rpmStats = getRpmStats();
        Map<String, ? extends Timer> screenOffRpmStats = getScreenOffRpmStats();
        if (rpmStats.size() > 0) {
            Iterator<Map.Entry<String, ? extends Timer>> it4 = rpmStats.entrySet().iterator();
            while (it4.hasNext()) {
                Map.Entry<String, ? extends Timer> ent3 = it4.next();
                sb.setLength(0);
                Timer totalTimer = (Timer) ent3.getValue();
                long timeMs = (totalTimer.getTotalTimeLocked(rawRealtime10, i4) + 500) / 1000;
                int count2 = totalTimer.getCountLocked(i4);
                Timer screenOffTimer = (Timer) screenOffRpmStats.get(ent3.getKey());
                if (screenOffTimer != null) {
                    long totalTimeLocked = (screenOffTimer.getTotalTimeLocked(rawRealtime10, i4) + 500) / 1000;
                }
                if (screenOffTimer != null) {
                    int countLocked = screenOffTimer.getCountLocked(i4);
                }
                Timer timer2 = totalTimer;
                dumpLine(printWriter4, 0, category10, RESOURCE_POWER_MANAGER_DATA, "\"" + ent3.getKey() + "\"", Long.valueOf(timeMs), Integer.valueOf(count2));
                it4 = it4;
                rpmStats = rpmStats;
                screenOffRpmStats = screenOffRpmStats;
            }
        }
        Map<String, ? extends Timer> map = screenOffRpmStats;
        BatteryStatsHelper helper = new BatteryStatsHelper(context, false, wifiOnly);
        helper.create(this);
        helper.refreshStats(i4, -1);
        List<BatterySipper> sippers3 = helper.getUsageList();
        if (sippers3 == null || sippers3.size() <= 0) {
            sippers = sippers3;
            sb2 = sb;
            BatteryStatsHelper batteryStatsHelper = helper;
        } else {
            sb2 = sb;
            dumpLine(printWriter4, 0, category10, POWER_USE_SUMMARY_DATA, BatteryStatsHelper.makemAh(helper.getPowerProfile().getBatteryCapacity()), BatteryStatsHelper.makemAh(helper.getComputedPower()), BatteryStatsHelper.makemAh(helper.getMinDrainedPower()), BatteryStatsHelper.makemAh(helper.getMaxDrainedPower()));
            int uid3 = 0;
            int i29 = 0;
            while (i29 < sippers3.size()) {
                BatterySipper bs = sippers3.get(i29);
                switch (AnonymousClass2.$SwitchMap$com$android$internal$os$BatterySipper$DrainType[bs.drainType.ordinal()]) {
                    case 1:
                        label = "ambi";
                        break;
                    case 2:
                        label = "idle";
                        break;
                    case 3:
                        label = "cell";
                        break;
                    case 4:
                        label = "phone";
                        break;
                    case 5:
                        label = "wifi";
                        break;
                    case 6:
                        label = "blue";
                        break;
                    case 7:
                        label = "scrn";
                        break;
                    case 8:
                        label = "flashlight";
                        break;
                    case 9:
                        uid3 = bs.uidObj.getUid();
                        label = "uid";
                        break;
                    case 10:
                        uid3 = UserHandle.getUid(bs.userId, 0);
                        label = "user";
                        break;
                    case 11:
                        label = "unacc";
                        break;
                    case 12:
                        label = "over";
                        break;
                    case 13:
                        label = "camera";
                        break;
                    case 14:
                        label = "memory";
                        break;
                    default:
                        label = "???";
                        break;
                }
                dumpLine(printWriter4, uid3, category10, POWER_USE_ITEM_DATA, label, BatteryStatsHelper.makemAh(bs.totalPowerMah), Integer.valueOf(bs.shouldHide ? 1 : 0), BatteryStatsHelper.makemAh(bs.screenPowerMah), BatteryStatsHelper.makemAh(bs.proportionalSmearMah));
                i29++;
                sippers3 = sippers3;
                helper = helper;
                boolean z = wifiOnly;
            }
            sippers = sippers3;
            BatteryStatsHelper batteryStatsHelper2 = helper;
        }
        long[] cpuFreqs3 = getCpuFreqs();
        if (cpuFreqs3 != null) {
            sb3 = sb2;
            sb3.setLength(0);
            int i30 = 0;
            while (i30 < cpuFreqs3.length) {
                StringBuilder sb12 = new StringBuilder();
                sb12.append(i30 == 0 ? "" : ",");
                sb12.append(cpuFreqs3[i30]);
                sb3.append(sb12.toString());
                i30++;
            }
            dumpLine(printWriter4, 0, category10, GLOBAL_CPU_FREQ_DATA, sb3.toString());
        } else {
            sb3 = sb2;
        }
        int iu3 = 0;
        while (true) {
            int iu4 = iu3;
            int NU6 = NU5;
            if (iu4 < NU6) {
                SparseArray<? extends Uid> uidStats4 = uidStats3;
                int uid4 = uidStats4.keyAt(iu4);
                int i31 = reqUid;
                if (i31 < 0 || uid4 == i31) {
                    Uid u4 = (Uid) uidStats4.valueAt(iu4);
                    long[] cpuFreqs4 = cpuFreqs3;
                    iu = iu4;
                    long mobileBytesRx = u4.getNetworkActivityBytes(0, i4);
                    long rawRealtime11 = rawRealtime10;
                    long mobileBytesTx = u4.getNetworkActivityBytes(1, i4);
                    int NU7 = NU6;
                    StringBuilder sb13 = sb3;
                    long wifiBytesRx = u4.getNetworkActivityBytes(2, i4);
                    int uid5 = uid4;
                    long wifiBytesTx3 = u4.getNetworkActivityBytes(3, i4);
                    String category11 = category10;
                    long mobilePacketsRx = u4.getNetworkActivityPackets(0, i4);
                    long mobilePacketsTx2 = u4.getNetworkActivityPackets(1, i4);
                    long mobileActiveTime2 = u4.getMobileRadioActiveTime(i4);
                    int mobileActiveCount = u4.getMobileRadioActiveCount(i4);
                    long mobileActiveTime3 = mobileActiveTime2;
                    long mobileWakeup2 = u4.getMobileRadioApWakeupCount(i4);
                    SparseArray<? extends Uid> uidStats5 = uidStats4;
                    long wifiPacketsRx2 = u4.getNetworkActivityPackets(2, i4);
                    long wifiPacketsTx2 = u4.getNetworkActivityPackets(3, i4);
                    long wifiWakeup2 = u4.getWifiRadioApWakeupCount(i4);
                    long btBytesRx2 = u4.getNetworkActivityBytes(4, i4);
                    long btBytesTx2 = u4.getNetworkActivityBytes(5, i4);
                    long mobileBytesBgRx2 = u4.getNetworkActivityBytes(6, i4);
                    long mobileBytesBgTx2 = u4.getNetworkActivityBytes(7, i4);
                    long wifiBytesBgRx2 = u4.getNetworkActivityBytes(8, i4);
                    long wifiBytesBgTx2 = u4.getNetworkActivityBytes(9, i4);
                    long mobilePacketsBgRx2 = u4.getNetworkActivityPackets(6, i4);
                    long mobilePacketsBgTx2 = u4.getNetworkActivityPackets(7, i4);
                    long wifiPacketsBgRx = u4.getNetworkActivityPackets(8, i4);
                    long wifiPacketsBgRx2 = u4.getNetworkActivityPackets(9, i4);
                    if (mobileBytesRx > 0 || mobileBytesTx > 0 || wifiBytesRx > 0 || wifiBytesTx3 > 0 || mobilePacketsRx > 0 || mobilePacketsTx2 > 0 || wifiPacketsRx2 > 0 || wifiPacketsTx2 > 0 || mobileActiveTime3 > 0 || mobileActiveCount > 0 || btBytesRx2 > 0 || btBytesTx2 > 0 || mobileWakeup2 > 0 || wifiWakeup2 > 0 || mobileBytesBgRx2 > 0 || mobileBytesBgTx2 > 0 || wifiBytesBgRx2 > 0 || wifiBytesBgTx2 > 0 || mobilePacketsBgRx2 > 0 || mobilePacketsBgTx2 > 0 || wifiPacketsBgRx > 0 || wifiPacketsBgRx2 > 0) {
                        wifiBytesTx = wifiBytesTx3;
                        long mobilePacketsRx2 = mobilePacketsRx;
                        c3 = 4;
                        long mobilePacketsRx3 = mobilePacketsRx2;
                        long wifiPacketsRx3 = wifiPacketsRx2;
                        wifiPacketsRx = wifiPacketsRx3;
                        long wifiPacketsRx4 = wifiPacketsTx2;
                        wifiPacketsTx = wifiPacketsRx4;
                        long wifiPacketsTx3 = mobileActiveTime3;
                        c2 = 8;
                        c = 9;
                        mobileActiveTime = wifiPacketsTx3;
                        long btBytesRx3 = btBytesRx2;
                        mobilePacketsTx = mobilePacketsTx2;
                        btBytesRx = btBytesRx3;
                        long btBytesRx4 = btBytesTx2;
                        btBytesTx = btBytesRx4;
                        long mobileWakeup3 = mobileWakeup2;
                        mobileWakeup = mobileWakeup3;
                        long wifiWakeup3 = wifiWakeup2;
                        wifiWakeup = wifiWakeup3;
                        long wifiWakeup4 = mobileBytesBgRx2;
                        mobileBytesBgRx = wifiWakeup4;
                        long mobileBytesBgTx3 = mobileBytesBgTx2;
                        mobileBytesBgTx = mobileBytesBgTx3;
                        long mobileBytesBgTx4 = wifiBytesBgRx2;
                        wifiBytesBgRx = mobileBytesBgTx4;
                        long wifiBytesBgTx3 = wifiBytesBgTx2;
                        wifiBytesBgTx = wifiBytesBgTx3;
                        long wifiBytesBgTx4 = mobilePacketsBgRx2;
                        mobilePacketsBgRx = wifiBytesBgTx4;
                        long mobilePacketsBgRx3 = mobilePacketsBgTx2;
                        mobilePacketsBgTx = mobilePacketsBgRx3;
                        wifiBytesTx2 = wifiPacketsBgRx;
                        Object[] objArr6 = {Long.valueOf(mobileBytesRx), Long.valueOf(mobileBytesTx), Long.valueOf(wifiBytesRx), Long.valueOf(wifiBytesTx3), Long.valueOf(mobilePacketsRx2), Long.valueOf(mobilePacketsTx2), Long.valueOf(wifiPacketsRx3), Long.valueOf(wifiPacketsRx4), Long.valueOf(wifiPacketsTx3), Integer.valueOf(mobileActiveCount), Long.valueOf(btBytesRx3), Long.valueOf(btBytesRx4), Long.valueOf(mobileWakeup3), Long.valueOf(wifiWakeup3), Long.valueOf(wifiWakeup4), Long.valueOf(mobileBytesBgTx3), Long.valueOf(mobileBytesBgTx4), Long.valueOf(wifiBytesBgTx3), Long.valueOf(wifiBytesBgTx4), Long.valueOf(mobilePacketsBgRx3), Long.valueOf(wifiBytesTx2), Long.valueOf(wifiPacketsBgRx2)};
                        wifiPacketsBgTx = wifiPacketsBgRx2;
                        uid = uid5;
                        category2 = category11;
                        long j14 = mobilePacketsRx3;
                        printWriter2 = pw;
                        dumpLine(printWriter2, uid, category2, NETWORK_DATA, objArr6);
                    } else {
                        wifiPacketsBgTx = wifiPacketsBgRx2;
                        wifiBytesTx = wifiBytesTx3;
                        mobilePacketsTx = mobilePacketsTx2;
                        uid = uid5;
                        category2 = category11;
                        long j15 = mobilePacketsRx;
                        mobileActiveTime = mobileActiveTime3;
                        mobileWakeup = mobileWakeup2;
                        wifiPacketsRx = wifiPacketsRx2;
                        wifiPacketsTx = wifiPacketsTx2;
                        wifiWakeup = wifiWakeup2;
                        btBytesRx = btBytesRx2;
                        btBytesTx = btBytesTx2;
                        mobileBytesBgRx = mobileBytesBgRx2;
                        mobileBytesBgTx = mobileBytesBgTx2;
                        wifiBytesBgRx = wifiBytesBgRx2;
                        wifiBytesBgTx = wifiBytesBgTx2;
                        mobilePacketsBgRx = mobilePacketsBgRx2;
                        mobilePacketsBgTx = mobilePacketsBgTx2;
                        wifiBytesTx2 = wifiPacketsBgRx;
                        printWriter2 = pw;
                        c3 = 4;
                        c2 = 8;
                        c = 9;
                    }
                    long j16 = wifiBytesTx2;
                    long j17 = wifiPacketsRx;
                    long j18 = wifiPacketsTx;
                    long j19 = mobileActiveTime;
                    long j20 = btBytesRx;
                    long j21 = btBytesTx;
                    long j22 = mobileWakeup;
                    long j23 = wifiWakeup;
                    long j24 = mobileBytesBgRx;
                    long j25 = mobileBytesBgTx;
                    long j26 = wifiBytesBgRx;
                    long j27 = wifiBytesBgTx;
                    long j28 = mobilePacketsBgRx;
                    long j29 = mobilePacketsBgTx;
                    long j30 = wifiPacketsBgTx;
                    long j31 = wifiBytesTx;
                    long j32 = mobileBytesTx;
                    char c4 = c3;
                    Uid u5 = u4;
                    char c5 = c2;
                    char c6 = c;
                    sippers2 = sippers;
                    uidStats = uidStats5;
                    long j33 = wifiBytesRx;
                    NU = NU7;
                    int i32 = which;
                    dumpControllerActivityLine(printWriter2, uid, category2, MODEM_CONTROLLER_DATA, u4.getModemControllerActivity(), i32);
                    long rawRealtime12 = rawRealtime11;
                    long fullWifiLockOnTime2 = u5.getFullWifiLockTime(rawRealtime12, i32);
                    long wifiScanTime = u5.getWifiScanTime(rawRealtime12, i32);
                    int wifiScanCount = u5.getWifiScanCount(i32);
                    int wifiScanCountBg = u5.getWifiScanBackgroundCount(i32);
                    int i33 = mobileActiveCount;
                    long j34 = mobileBytesRx;
                    long wifiScanActualTimeMs = (u5.getWifiScanActualTime(rawRealtime12) + 500) / 1000;
                    String category12 = category2;
                    long wifiScanActualTimeMsBg2 = (u5.getWifiScanBackgroundTime(rawRealtime12) + 500) / 1000;
                    long uidWifiRunningTime2 = u5.getWifiRunningTime(rawRealtime12, i32);
                    if (fullWifiLockOnTime2 == 0 && wifiScanTime == 0 && wifiScanCount == 0 && wifiScanCountBg == 0 && wifiScanActualTimeMs == 0) {
                        rawRealtime2 = rawRealtime12;
                        wifiScanActualTimeMsBg = wifiScanActualTimeMsBg2;
                        if (wifiScanActualTimeMsBg == 0 && uidWifiRunningTime2 == 0) {
                            fullWifiLockOnTime = fullWifiLockOnTime2;
                            long j35 = wifiScanActualTimeMsBg;
                            u = u5;
                            category3 = category12;
                            printWriter3 = pw;
                            Uid u6 = u;
                            int i34 = wifiScanCount;
                            long j36 = wifiScanTime;
                            long j37 = fullWifiLockOnTime;
                            String category13 = category3;
                            long rawRealtime13 = rawRealtime2;
                            int i35 = which;
                            dumpControllerActivityLine(printWriter3, uid, category3, WIFI_CONTROLLER_DATA, u6.getWifiControllerActivity(), i35);
                            bleTimer = u6.getBluetoothScanTimer();
                            if (bleTimer == null) {
                                long rawRealtime14 = rawRealtime13;
                                long totalTime = (bleTimer.getTotalTimeLocked(rawRealtime14, i35) + 500) / 1000;
                                if (totalTime != 0) {
                                    int count3 = bleTimer.getCountLocked(i35);
                                    Timer bleTimerBg = u6.getBluetoothScanBackgroundTimer();
                                    int countBg = bleTimerBg != null ? bleTimerBg.getCountLocked(i35) : 0;
                                    rawRealtime3 = rawRealtime14;
                                    int i36 = wifiScanCountBg;
                                    long j38 = wifiScanActualTimeMs;
                                    rawRealtimeMs3 = rawRealtimeMs;
                                    long actualTime = bleTimer.getTotalDurationMsLocked(rawRealtimeMs3);
                                    long actualTimeBg = bleTimerBg != null ? bleTimerBg.getTotalDurationMsLocked(rawRealtimeMs3) : 0;
                                    int resultCount = u6.getBluetoothScanResultCounter() != null ? u6.getBluetoothScanResultCounter().getCountLocked(i35) : 0;
                                    if (u6.getBluetoothScanResultBgCounter() != null) {
                                        bleTimer2 = bleTimer;
                                        resultCountBg = u6.getBluetoothScanResultBgCounter().getCountLocked(i35);
                                    } else {
                                        bleTimer2 = bleTimer;
                                        resultCountBg = 0;
                                    }
                                    uidWifiRunningTime = uidWifiRunningTime2;
                                    Timer unoptimizedScanTimer = u6.getBluetoothUnoptimizedScanTimer();
                                    long unoptimizedScanTotalTime = unoptimizedScanTimer != null ? unoptimizedScanTimer.getTotalDurationMsLocked(rawRealtimeMs3) : 0;
                                    long unoptimizedScanMaxTime = unoptimizedScanTimer != null ? unoptimizedScanTimer.getMaxDurationMsLocked(rawRealtimeMs3) : 0;
                                    Timer unoptimizedScanTimerBg = u6.getBluetoothUnoptimizedScanBackgroundTimer();
                                    long unoptimizedScanTotalTimeBg = unoptimizedScanTimerBg != null ? unoptimizedScanTimerBg.getTotalDurationMsLocked(rawRealtimeMs3) : 0;
                                    long unoptimizedScanMaxTimeBg = unoptimizedScanTimerBg != null ? unoptimizedScanTimerBg.getMaxDurationMsLocked(rawRealtimeMs3) : 0;
                                    Timer timer3 = unoptimizedScanTimer;
                                    Timer timer4 = unoptimizedScanTimerBg;
                                    Object[] objArr7 = new Object[11];
                                    objArr7[0] = Long.valueOf(totalTime);
                                    objArr7[1] = Integer.valueOf(count3);
                                    long j39 = totalTime;
                                    int countBg2 = countBg;
                                    objArr7[2] = Integer.valueOf(countBg2);
                                    objArr7[3] = Long.valueOf(actualTime);
                                    int i37 = countBg2;
                                    long actualTimeBg2 = actualTimeBg;
                                    objArr7[4] = Long.valueOf(actualTimeBg2);
                                    objArr7[5] = Integer.valueOf(resultCount);
                                    objArr7[6] = Integer.valueOf(resultCountBg);
                                    long j40 = actualTimeBg2;
                                    long unoptimizedScanTotalTime2 = unoptimizedScanTotalTime;
                                    Timer timer5 = bleTimerBg;
                                    objArr7[7] = Long.valueOf(unoptimizedScanTotalTime2);
                                    u2 = u6;
                                    long unoptimizedScanTotalTimeBg2 = unoptimizedScanTotalTimeBg;
                                    objArr7[c5] = Long.valueOf(unoptimizedScanTotalTimeBg2);
                                    long j41 = unoptimizedScanTotalTime2;
                                    long unoptimizedScanTotalTime3 = unoptimizedScanMaxTime;
                                    objArr7[c6] = Long.valueOf(unoptimizedScanTotalTime3);
                                    long j42 = unoptimizedScanTotalTime3;
                                    objArr7[10] = Long.valueOf(unoptimizedScanMaxTimeBg);
                                    long j43 = unoptimizedScanTotalTimeBg2;
                                    category4 = category13;
                                    dumpLine(printWriter3, uid, category4, BLUETOOTH_MISC_DATA, objArr7);
                                } else {
                                    rawRealtime3 = rawRealtime14;
                                    bleTimer2 = bleTimer;
                                    uidWifiRunningTime = uidWifiRunningTime2;
                                    u2 = u6;
                                    int i38 = wifiScanCountBg;
                                    long j44 = wifiScanActualTimeMs;
                                    rawRealtimeMs3 = rawRealtimeMs;
                                    category4 = category13;
                                }
                            } else {
                                bleTimer2 = bleTimer;
                                uidWifiRunningTime = uidWifiRunningTime2;
                                u2 = u6;
                                int i39 = wifiScanCountBg;
                                long j45 = wifiScanActualTimeMs;
                                rawRealtimeMs3 = rawRealtimeMs;
                                rawRealtime3 = rawRealtime13;
                                category4 = category13;
                            }
                            u3 = u2;
                            long rawRealtime15 = rawRealtime3;
                            bleTimer3 = bleTimer2;
                            dumpControllerActivityLine(printWriter3, uid, category4, BLUETOOTH_CONTROLLER_DATA, u3.getBluetoothControllerActivity(), i35);
                            if (u3.hasUserActivity()) {
                                Object[] args8 = new Object[4];
                                boolean hasData = false;
                                int i40 = 0;
                                for (int i41 = 4; i40 < i41; i41 = 4) {
                                    int val = u3.getUserActivityCount(i40, i35);
                                    args8[i40] = Integer.valueOf(val);
                                    if (val != 0) {
                                        hasData = true;
                                    }
                                    i40++;
                                }
                                if (hasData) {
                                    dumpLine(printWriter3, uid, category4, USER_ACTIVITY_DATA, args8);
                                }
                                Object[] objArr8 = args8;
                            }
                            if (u3.getAggregatedPartialWakelockTimer() == null) {
                                Timer timer6 = u3.getAggregatedPartialWakelockTimer();
                                long totTimeMs = timer6.getTotalDurationMsLocked(rawRealtimeMs3);
                                Timer bgTimer = timer6.getSubTimer();
                                Timer timer7 = timer6;
                                dumpLine(printWriter3, uid, category4, AGGREGATED_WAKELOCK_DATA, Long.valueOf(totTimeMs), Long.valueOf(bgTimer != null ? bgTimer.getTotalDurationMsLocked(rawRealtimeMs3) : 0));
                            }
                            ArrayMap<String, ? extends Uid.Wakelock> wakelocks3 = u3.getWakelockStats();
                            iw = wakelocks3.size() - 1;
                            while (true) {
                                iw2 = iw;
                                if (iw2 < 0) {
                                    Uid.Wakelock wl2 = (Uid.Wakelock) wakelocks3.valueAt(iw2);
                                    StringBuilder sb14 = sb13;
                                    sb14.setLength(0);
                                    long j46 = rawRealtime15;
                                    Timer bleTimer4 = bleTimer3;
                                    Uid.Wakelock wl3 = wl2;
                                    int i42 = i35;
                                    long rawRealtimeMs16 = rawRealtimeMs3;
                                    StringBuilder sb15 = sb14;
                                    PrintWriter printWriter6 = printWriter3;
                                    String linePrefix = printWakeLockCheckin(sb14, wl2.getWakeTime(1), j46, "f", i42, "");
                                    Timer pTimer = wl3.getWakeTime(0);
                                    Uid u7 = u3;
                                    Timer pTimer2 = pTimer;
                                    String linePrefix2 = printWakeLockCheckin(sb15, pTimer, j46, "p", i42, linePrefix);
                                    long j47 = rawRealtime15;
                                    int i43 = i35;
                                    String printWakeLockCheckin = printWakeLockCheckin(sb15, wl3.getWakeTime(2), j47, "w", i43, printWakeLockCheckin(sb15, pTimer2 != null ? pTimer2.getSubTimer() : null, j47, "bp", i43, linePrefix2));
                                    if (sb15.length() > 0) {
                                        String name = wakelocks3.keyAt(iw2);
                                        if (name.indexOf(44) >= 0) {
                                            name = name.replace(',', '_');
                                        }
                                        if (name.indexOf(10) >= 0) {
                                            name = name.replace(10, '_');
                                        }
                                        if (name.indexOf(13) >= 0) {
                                            name = name.replace(13, '_');
                                        }
                                        dumpLine(printWriter6, uid, category4, WAKELOCK_DATA, name, sb15.toString());
                                    }
                                    iw = iw2 - 1;
                                    printWriter3 = printWriter6;
                                    sb13 = sb15;
                                    bleTimer3 = bleTimer4;
                                    rawRealtimeMs3 = rawRealtimeMs16;
                                    u3 = u7;
                                } else {
                                    Timer timer8 = bleTimer3;
                                    long rawRealtimeMs17 = rawRealtimeMs3;
                                    StringBuilder sb16 = sb13;
                                    printWriter = printWriter3;
                                    Timer mcTimer = u3.getMulticastWakelockStats();
                                    if (mcTimer != null) {
                                        rawRealtime4 = rawRealtime15;
                                        long totalMcWakelockTimeMs = mcTimer.getTotalTimeLocked(rawRealtime4, i35) / 1000;
                                        int countMcWakelock = mcTimer.getCountLocked(i35);
                                        if (totalMcWakelockTimeMs > 0) {
                                            dumpLine(printWriter, uid, category4, WIFI_MULTICAST_DATA, Long.valueOf(totalMcWakelockTimeMs), Integer.valueOf(countMcWakelock));
                                        }
                                    } else {
                                        rawRealtime4 = rawRealtime15;
                                    }
                                    ArrayMap<String, ? extends Timer> syncs2 = u3.getSyncStats();
                                    int isy = syncs2.size() - 1;
                                    while (isy >= 0) {
                                        Timer timer9 = (Timer) syncs2.valueAt(isy);
                                        long totalTime2 = (timer9.getTotalTimeLocked(rawRealtime4, i35) + 500) / 1000;
                                        int count4 = timer9.getCountLocked(i35);
                                        Timer mcTimer2 = mcTimer;
                                        Timer bgTimer2 = timer9.getSubTimer();
                                        if (bgTimer2 != null) {
                                            sb7 = sb16;
                                            wakelocks = wakelocks3;
                                            rawRealtimeMs7 = rawRealtimeMs17;
                                            j2 = bgTimer2.getTotalDurationMsLocked(rawRealtimeMs7);
                                        } else {
                                            sb7 = sb16;
                                            wakelocks = wakelocks3;
                                            rawRealtimeMs7 = rawRealtimeMs17;
                                            j2 = -1;
                                        }
                                        long bgTime = j2;
                                        int bgCount = bgTimer2 != null ? bgTimer2.getCountLocked(i35) : -1;
                                        if (totalTime2 != 0) {
                                            Timer timer10 = timer9;
                                            Timer timer11 = bgTimer2;
                                            rawRealtimeMs8 = rawRealtimeMs7;
                                            long j48 = totalTime2;
                                            dumpLine(printWriter, uid, category4, SYNC_DATA, "\"" + syncs2.keyAt(isy) + "\"", Long.valueOf(totalTime2), Integer.valueOf(count4), Long.valueOf(bgTime), Integer.valueOf(bgCount));
                                        } else {
                                            rawRealtimeMs8 = rawRealtimeMs7;
                                        }
                                        isy--;
                                        mcTimer = mcTimer2;
                                        wakelocks3 = wakelocks;
                                        sb16 = sb7;
                                        rawRealtimeMs17 = rawRealtimeMs8;
                                    }
                                    StringBuilder sb17 = sb16;
                                    ArrayMap<String, ? extends Uid.Wakelock> arrayMap = wakelocks3;
                                    long rawRealtimeMs18 = rawRealtimeMs17;
                                    ArrayMap<String, ? extends Timer> jobs = u3.getJobStats();
                                    int ij = jobs.size() - 1;
                                    while (ij >= 0) {
                                        Timer timer12 = (Timer) jobs.valueAt(ij);
                                        long totalTime3 = (timer12.getTotalTimeLocked(rawRealtime4, i35) + 500) / 1000;
                                        int count5 = timer12.getCountLocked(i35);
                                        Timer bgTimer3 = timer12.getSubTimer();
                                        if (bgTimer3 != null) {
                                            syncs = syncs2;
                                            rawRealtime6 = rawRealtime4;
                                            rawRealtimeMs5 = rawRealtimeMs18;
                                            j = bgTimer3.getTotalDurationMsLocked(rawRealtimeMs5);
                                        } else {
                                            syncs = syncs2;
                                            rawRealtime6 = rawRealtime4;
                                            rawRealtimeMs5 = rawRealtimeMs18;
                                            j = -1;
                                        }
                                        long bgTime2 = j;
                                        int bgCount2 = bgTimer3 != null ? bgTimer3.getCountLocked(i35) : -1;
                                        if (totalTime3 != 0) {
                                            Timer timer13 = timer12;
                                            rawRealtimeMs6 = rawRealtimeMs5;
                                            long j49 = totalTime3;
                                            dumpLine(printWriter, uid, category4, JOB_DATA, "\"" + jobs.keyAt(ij) + "\"", Long.valueOf(totalTime3), Integer.valueOf(count5), Long.valueOf(bgTime2), Integer.valueOf(bgCount2));
                                        } else {
                                            rawRealtimeMs6 = rawRealtimeMs5;
                                        }
                                        ij--;
                                        rawRealtime4 = rawRealtime6;
                                        syncs2 = syncs;
                                        rawRealtimeMs18 = rawRealtimeMs6;
                                    }
                                    ArrayMap<String, ? extends Timer> syncs3 = syncs2;
                                    long rawRealtime16 = rawRealtime4;
                                    long rawRealtimeMs19 = rawRealtimeMs18;
                                    ArrayMap<String, SparseIntArray> completions = u3.getJobCompletionStats();
                                    for (int ic = completions.size() - 1; ic >= 0; ic--) {
                                        SparseIntArray types = completions.valueAt(ic);
                                        if (types != null) {
                                            dumpLine(printWriter, uid, category4, JOB_COMPLETION_DATA, "\"" + completions.keyAt(ic) + "\"", Integer.valueOf(types.get(0, 0)), Integer.valueOf(types.get(1, 0)), Integer.valueOf(types.get(2, 0)), Integer.valueOf(types.get(3, 0)), Integer.valueOf(types.get(4, 0)));
                                        }
                                    }
                                    StringBuilder sb18 = sb17;
                                    u3.getDeferredJobsCheckinLineLocked(sb18, i35);
                                    if (sb18.length() > 0) {
                                        i3 = 0;
                                        dumpLine(printWriter, uid, category4, JOBS_DEFERRED_DATA, sb18.toString());
                                    } else {
                                        i3 = 0;
                                    }
                                    PrintWriter printWriter7 = printWriter;
                                    int i44 = uid;
                                    String str2 = category4;
                                    String category14 = category4;
                                    ArrayMap<String, ? extends Timer> arrayMap2 = jobs;
                                    long rawRealtime17 = rawRealtime16;
                                    ArrayMap<String, ? extends Timer> arrayMap3 = syncs3;
                                    long rawRealtimeMs20 = rawRealtimeMs19;
                                    long j50 = rawRealtime17;
                                    ArrayMap<String, SparseIntArray> arrayMap4 = completions;
                                    long j51 = mobilePacketsTx;
                                    long j52 = uidWifiRunningTime;
                                    String category15 = category14;
                                    int i45 = i3;
                                    int i46 = i35;
                                    dumpTimer(printWriter7, i44, str2, FLASHLIGHT_DATA, u3.getFlashlightTurnedOnTimer(), j50, i46);
                                    String str3 = category15;
                                    dumpTimer(printWriter7, i44, str3, CAMERA_DATA, u3.getCameraTurnedOnTimer(), j50, i46);
                                    dumpTimer(printWriter7, i44, str3, VIDEO_DATA, u3.getVideoTurnedOnTimer(), j50, i46);
                                    dumpTimer(printWriter7, i44, str3, AUDIO_DATA, u3.getAudioTurnedOnTimer(), j50, i46);
                                    SparseArray<? extends Uid.Sensor> sensors3 = u3.getSensorStats();
                                    int NSE2 = sensors3.size();
                                    int ise = i45;
                                    while (ise < NSE2) {
                                        Uid.Sensor se = (Uid.Sensor) sensors3.valueAt(ise);
                                        int sensorNumber = sensors3.keyAt(ise);
                                        Timer timer14 = se.getSensorTime();
                                        if (timer14 != null) {
                                            sb6 = sb18;
                                            long rawRealtime18 = rawRealtime17;
                                            NSE = NSE2;
                                            long totalTime4 = (timer14.getTotalTimeLocked(rawRealtime18, i35) + 500) / 1000;
                                            if (totalTime4 != 0) {
                                                int count6 = timer14.getCountLocked(i35);
                                                sensors2 = sensors3;
                                                Timer bgTimer4 = se.getSensorBackgroundTime();
                                                int bgCount3 = bgTimer4 != null ? bgTimer4.getCountLocked(i35) : 0;
                                                rawRealtime5 = rawRealtime18;
                                                long actualTime2 = timer14.getTotalDurationMsLocked(rawRealtimeMs20);
                                                long bgActualTime = bgTimer4 != null ? bgTimer4.getTotalDurationMsLocked(rawRealtimeMs20) : 0;
                                                Uid.Sensor sensor = se;
                                                Timer timer15 = timer14;
                                                Timer timer16 = bgTimer4;
                                                int bgCount4 = bgCount3;
                                                int i47 = sensorNumber;
                                                int i48 = bgCount4;
                                                rawRealtimeMs4 = rawRealtimeMs20;
                                                category5 = category15;
                                                dumpLine(printWriter, uid, category5, SENSOR_DATA, Integer.valueOf(sensorNumber), Long.valueOf(totalTime4), Integer.valueOf(count6), Integer.valueOf(bgCount4), Long.valueOf(actualTime2), Long.valueOf(bgActualTime));
                                            } else {
                                                sensors2 = sensors3;
                                                rawRealtimeMs4 = rawRealtimeMs20;
                                                rawRealtime5 = rawRealtime18;
                                                category5 = category15;
                                            }
                                        } else {
                                            NSE = NSE2;
                                            sensors2 = sensors3;
                                            rawRealtimeMs4 = rawRealtimeMs20;
                                            sb6 = sb18;
                                            rawRealtime5 = rawRealtime17;
                                            category5 = category15;
                                        }
                                        ise++;
                                        category15 = category5;
                                        sb18 = sb6;
                                        NSE2 = NSE;
                                        sensors3 = sensors2;
                                        rawRealtime17 = rawRealtime5;
                                        rawRealtimeMs20 = rawRealtimeMs4;
                                    }
                                    rawRealtimeMs2 = rawRealtimeMs20;
                                    StringBuilder sb19 = sb18;
                                    long rawRealtime19 = rawRealtime17;
                                    category = category15;
                                    PrintWriter printWriter8 = printWriter;
                                    int i49 = uid;
                                    String str4 = category;
                                    int NSE3 = NSE2;
                                    long j53 = rawRealtime19;
                                    SparseArray<? extends Uid.Sensor> sensors4 = sensors3;
                                    int i50 = i35;
                                    dumpTimer(printWriter8, i49, str4, VIBRATOR_DATA, u3.getVibratorOnTimer(), j53, i50);
                                    dumpTimer(printWriter8, i49, str4, FOREGROUND_ACTIVITY_DATA, u3.getForegroundActivityTimer(), j53, i50);
                                    dumpTimer(printWriter8, i49, str4, FOREGROUND_SERVICE_DATA, u3.getForegroundServiceTimer(), j53, i50);
                                    Object[] stateTimes = new Object[7];
                                    long totalStateTime = 0;
                                    int ips = 0;
                                    for (int i51 = 7; ips < i51; i51 = 7) {
                                        long time = u3.getProcessStateTime(ips, rawRealtime19, i35);
                                        stateTimes[ips] = Long.valueOf((time + 500) / 1000);
                                        ips++;
                                        totalStateTime += time;
                                    }
                                    long rawRealtime20 = rawRealtime19;
                                    if (totalStateTime > 0) {
                                        dumpLine(printWriter, uid, category, "st", stateTimes);
                                    }
                                    long userCpuTimeUs = u3.getUserCpuTimeUs(i35);
                                    long systemCpuTimeUs = u3.getSystemCpuTimeUs(i35);
                                    if (userCpuTimeUs > 0 || systemCpuTimeUs > 0) {
                                        Object[] objArr9 = stateTimes;
                                        long j54 = totalStateTime;
                                        dumpLine(printWriter, uid, category, CPU_DATA, Long.valueOf(userCpuTimeUs / 1000), Long.valueOf(systemCpuTimeUs / 1000), 0);
                                    } else {
                                        Object[] objArr10 = stateTimes;
                                        long j55 = totalStateTime;
                                    }
                                    if (cpuFreqs4 != null) {
                                        long[] cpuFreqTimeMs = u3.getCpuFreqTimes(i35);
                                        if (cpuFreqTimeMs != null) {
                                            cpuFreqs2 = cpuFreqs4;
                                            if (cpuFreqTimeMs.length == cpuFreqs2.length) {
                                                sb5 = sb19;
                                                sb5.setLength(0);
                                                int i52 = 0;
                                                while (i52 < cpuFreqTimeMs.length) {
                                                    StringBuilder sb20 = new StringBuilder();
                                                    sb20.append(i52 == 0 ? "" : ",");
                                                    sb20.append(cpuFreqTimeMs[i52]);
                                                    sb5.append(sb20.toString());
                                                    i52++;
                                                    rawRealtime20 = rawRealtime20;
                                                }
                                                rawRealtime = rawRealtime20;
                                                long[] screenOffCpuFreqTimeMs = u3.getScreenOffCpuFreqTimes(i35);
                                                if (screenOffCpuFreqTimeMs != null) {
                                                    for (int i53 = 0; i53 < screenOffCpuFreqTimeMs.length; i53++) {
                                                        sb5.append("," + screenOffCpuFreqTimeMs[i53]);
                                                    }
                                                } else {
                                                    for (int i54 = 0; i54 < cpuFreqTimeMs.length; i54++) {
                                                        sb5.append(",0");
                                                    }
                                                }
                                                dumpLine(printWriter, uid, category, CPU_TIMES_AT_FREQ_DATA, UID_TIMES_TYPE_ALL, Integer.valueOf(cpuFreqTimeMs.length), sb5.toString());
                                            } else {
                                                rawRealtime = rawRealtime20;
                                                sb5 = sb19;
                                            }
                                        } else {
                                            rawRealtime = rawRealtime20;
                                            cpuFreqs2 = cpuFreqs4;
                                            sb5 = sb19;
                                        }
                                        int procState = 0;
                                        while (procState < 7) {
                                            long[] timesMs = u3.getCpuFreqTimes(i35, procState);
                                            if (timesMs == null || timesMs.length != cpuFreqs2.length) {
                                                sensors = sensors4;
                                            } else {
                                                sb5.setLength(0);
                                                int i55 = 0;
                                                while (i55 < timesMs.length) {
                                                    StringBuilder sb21 = new StringBuilder();
                                                    sb21.append(i55 == 0 ? "" : ",");
                                                    sb21.append(timesMs[i55]);
                                                    sb5.append(sb21.toString());
                                                    i55++;
                                                    sensors4 = sensors4;
                                                }
                                                sensors = sensors4;
                                                long[] screenOffTimesMs = u3.getScreenOffCpuFreqTimes(i35, procState);
                                                if (screenOffTimesMs != null) {
                                                    for (int i56 = 0; i56 < screenOffTimesMs.length; i56++) {
                                                        sb5.append("," + screenOffTimesMs[i56]);
                                                    }
                                                } else {
                                                    for (int i57 = 0; i57 < timesMs.length; i57++) {
                                                        sb5.append(",0");
                                                    }
                                                }
                                                dumpLine(printWriter, uid, category, CPU_TIMES_AT_FREQ_DATA, Uid.UID_PROCESS_TYPES[procState], Integer.valueOf(timesMs.length), sb5.toString());
                                            }
                                            procState++;
                                            sensors4 = sensors;
                                        }
                                    } else {
                                        rawRealtime = rawRealtime20;
                                        SparseArray<? extends Uid.Sensor> sparseArray = sensors4;
                                        cpuFreqs2 = cpuFreqs4;
                                        sb5 = sb19;
                                    }
                                    ArrayMap<String, ? extends Uid.Proc> processStats = u3.getProcessStats();
                                    int ipr = processStats.size() - 1;
                                    while (ipr >= 0) {
                                        Uid.Proc ps2 = (Uid.Proc) processStats.valueAt(ipr);
                                        long userMillis = ps2.getUserTime(i35);
                                        long systemMillis = ps2.getSystemTime(i35);
                                        StringBuilder sb22 = sb5;
                                        long[] cpuFreqs5 = cpuFreqs2;
                                        long foregroundMillis = ps2.getForegroundTime(i35);
                                        int starts = ps2.getStarts(i35);
                                        int NSE4 = NSE3;
                                        int numCrashes = ps2.getNumCrashes(i35);
                                        Uid u8 = u3;
                                        int numAnrs = ps2.getNumAnrs(i35);
                                        if (userMillis == 0 && systemMillis == 0 && foregroundMillis == 0 && starts == 0 && numAnrs == 0 && numCrashes == 0) {
                                            uid2 = uid;
                                        } else {
                                            Uid.Proc proc = ps2;
                                            StringBuilder sb23 = new StringBuilder();
                                            String str5 = PROCESS_DATA;
                                            sb23.append("\"");
                                            sb23.append(processStats.keyAt(ipr));
                                            sb23.append("\"");
                                            uid2 = uid;
                                            dumpLine(printWriter, uid2, category, str5, sb23.toString(), Long.valueOf(userMillis), Long.valueOf(systemMillis), Long.valueOf(foregroundMillis), Integer.valueOf(starts), Integer.valueOf(numAnrs), Integer.valueOf(numCrashes));
                                        }
                                        ipr--;
                                        uid = uid2;
                                        cpuFreqs2 = cpuFreqs5;
                                        sb5 = sb22;
                                        NSE3 = NSE4;
                                        u3 = u8;
                                        i35 = which;
                                    }
                                    int uid6 = uid;
                                    sb4 = sb5;
                                    cpuFreqs = cpuFreqs2;
                                    int i58 = NSE3;
                                    Uid u9 = u3;
                                    ArrayMap<String, ? extends Uid.Pkg> packageStats = u9.getPackageStats();
                                    int ipkg = packageStats.size() - 1;
                                    while (ipkg >= 0) {
                                        Uid.Pkg ps3 = (Uid.Pkg) packageStats.valueAt(ipkg);
                                        int wakeups = 0;
                                        ArrayMap<String, ? extends Counter> alarms2 = ps3.getWakeupAlarmStats();
                                        int iwa = alarms2.size() - 1;
                                        while (iwa >= 0) {
                                            int count7 = ((Counter) alarms2.valueAt(iwa)).getCountLocked(which);
                                            wakeups += count7;
                                            dumpLine(printWriter, uid6, category, WAKEUP_ALARM_DATA, alarms2.keyAt(iwa).replace(',', '_'), Integer.valueOf(count7));
                                            iwa--;
                                            u9 = u9;
                                        }
                                        Uid u10 = u9;
                                        int i59 = which;
                                        ArrayMap<String, ? extends Uid.Pkg.Serv> serviceStats = ps3.getServiceStats();
                                        int isvc2 = serviceStats.size() - 1;
                                        while (isvc2 >= 0) {
                                            Uid.Pkg.Serv ss = (Uid.Pkg.Serv) serviceStats.valueAt(isvc2);
                                            long batteryUptime7 = batteryUptime2;
                                            long startTime = ss.getStartTime(batteryUptime7, i59);
                                            int starts2 = ss.getStarts(i59);
                                            ArrayMap<String, ? extends Uid.Proc> processStats2 = processStats;
                                            int launches = ss.getLaunches(i59);
                                            if (startTime == 0 && starts2 == 0 && launches == 0) {
                                                ps = ps3;
                                                alarms = alarms2;
                                                isvc = isvc2;
                                            } else {
                                                ps = ps3;
                                                alarms = alarms2;
                                                Uid.Pkg.Serv serv = ss;
                                                isvc = isvc2;
                                                dumpLine(printWriter, uid6, category, APK_DATA, Integer.valueOf(wakeups), packageStats.keyAt(ipkg), serviceStats.keyAt(isvc2), Long.valueOf(startTime / 1000), Integer.valueOf(starts2), Integer.valueOf(launches));
                                            }
                                            isvc2 = isvc - 1;
                                            batteryUptime2 = batteryUptime7;
                                            processStats = processStats2;
                                            ps3 = ps;
                                            alarms2 = alarms;
                                        }
                                        ipkg--;
                                        batteryUptime2 = batteryUptime2;
                                        u9 = u10;
                                    }
                                    batteryUptime3 = batteryUptime2;
                                    i2 = which;
                                }
                            }
                        }
                    } else {
                        rawRealtime2 = rawRealtime12;
                        wifiScanActualTimeMsBg = wifiScanActualTimeMsBg2;
                    }
                    u = u5;
                    Object[] objArr11 = new Object[10];
                    fullWifiLockOnTime = fullWifiLockOnTime2;
                    objArr11[0] = Long.valueOf(fullWifiLockOnTime2);
                    objArr11[1] = Long.valueOf(wifiScanTime);
                    objArr11[2] = Long.valueOf(uidWifiRunningTime2);
                    objArr11[3] = Integer.valueOf(wifiScanCount);
                    objArr11[4] = 0;
                    objArr11[5] = 0;
                    objArr11[6] = 0;
                    objArr11[7] = Integer.valueOf(wifiScanCountBg);
                    objArr11[c5] = Long.valueOf(wifiScanActualTimeMs);
                    objArr11[c6] = Long.valueOf(wifiScanActualTimeMsBg);
                    long j56 = wifiScanActualTimeMsBg;
                    category3 = category12;
                    printWriter3 = pw;
                    dumpLine(printWriter3, uid, category3, WIFI_DATA, objArr11);
                    Uid u62 = u;
                    int i342 = wifiScanCount;
                    long j362 = wifiScanTime;
                    long j372 = fullWifiLockOnTime;
                    String category132 = category3;
                    long rawRealtime132 = rawRealtime2;
                    int i352 = which;
                    dumpControllerActivityLine(printWriter3, uid, category3, WIFI_CONTROLLER_DATA, u62.getWifiControllerActivity(), i352);
                    bleTimer = u62.getBluetoothScanTimer();
                    if (bleTimer == null) {
                    }
                    u3 = u2;
                    long rawRealtime152 = rawRealtime3;
                    bleTimer3 = bleTimer2;
                    dumpControllerActivityLine(printWriter3, uid, category4, BLUETOOTH_CONTROLLER_DATA, u3.getBluetoothControllerActivity(), i352);
                    if (u3.hasUserActivity()) {
                    }
                    if (u3.getAggregatedPartialWakelockTimer() == null) {
                    }
                    ArrayMap<String, ? extends Uid.Wakelock> wakelocks32 = u3.getWakelockStats();
                    iw = wakelocks32.size() - 1;
                    while (true) {
                        iw2 = iw;
                        if (iw2 < 0) {
                        }
                        iw = iw2 - 1;
                        printWriter3 = printWriter6;
                        sb13 = sb15;
                        bleTimer3 = bleTimer4;
                        rawRealtimeMs3 = rawRealtimeMs16;
                        u3 = u7;
                    }
                } else {
                    uidStats = uidStats4;
                    NU = NU6;
                    sb4 = sb3;
                    i2 = i4;
                    rawRealtime = rawRealtime10;
                    cpuFreqs = cpuFreqs3;
                    iu = iu4;
                    batteryUptime3 = batteryUptime2;
                    rawRealtimeMs2 = rawRealtimeMs;
                    sippers2 = sippers;
                    category = category10;
                    printWriter = printWriter4;
                }
                iu3 = iu + 1;
                category10 = category;
                i4 = i2;
                printWriter4 = printWriter;
                batteryUptime2 = batteryUptime3;
                uidStats3 = uidStats;
                sippers = sippers2;
                NU5 = NU;
                rawRealtimeMs = rawRealtimeMs2;
                rawRealtime10 = rawRealtime;
                cpuFreqs3 = cpuFreqs;
                sb3 = sb4;
                Context context2 = context;
            } else {
                StringBuilder sb24 = sb3;
                int i60 = i4;
                long j57 = rawRealtime10;
                long[] jArr = cpuFreqs3;
                SparseArray<? extends Uid> sparseArray2 = uidStats3;
                long j58 = batteryUptime2;
                long j59 = rawRealtimeMs;
                List<BatterySipper> list = sippers;
                String str6 = category10;
                PrintWriter printWriter9 = printWriter4;
                return;
            }
        }
    }

    private void printmAh(PrintWriter printer, double power) {
        printer.print(BatteryStatsHelper.makemAh(power));
    }

    private void printmAh(StringBuilder sb, double power) {
        sb.append(BatteryStatsHelper.makemAh(power));
    }

    public final void dumpLocked(Context context, PrintWriter pw, String prefix, int which, int reqUid) {
        dumpLocked(context, pw, prefix, which, reqUid, BatteryStatsHelper.checkWifiOnly(context));
    }

    /* JADX WARNING: Removed duplicated region for block: B:362:0x14e2  */
    /* JADX WARNING: Removed duplicated region for block: B:366:0x1510  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x1529  */
    /* JADX WARNING: Removed duplicated region for block: B:382:0x15d8  */
    /* JADX WARNING: Removed duplicated region for block: B:400:0x1660  */
    /* JADX WARNING: Removed duplicated region for block: B:404:0x1760  */
    /* JADX WARNING: Removed duplicated region for block: B:407:0x17b3  */
    /* JADX WARNING: Removed duplicated region for block: B:410:0x17bd  */
    /* JADX WARNING: Removed duplicated region for block: B:414:0x17e7  */
    /* JADX WARNING: Removed duplicated region for block: B:488:0x19ef  */
    /* JADX WARNING: Removed duplicated region for block: B:491:0x1a0a  */
    /* JADX WARNING: Removed duplicated region for block: B:504:0x1a48  */
    /* JADX WARNING: Removed duplicated region for block: B:507:0x1a5a  */
    /* JADX WARNING: Removed duplicated region for block: B:517:0x1b57  */
    /* JADX WARNING: Removed duplicated region for block: B:520:0x1b74  */
    /* JADX WARNING: Removed duplicated region for block: B:568:0x1c70  */
    /* JADX WARNING: Removed duplicated region for block: B:571:0x1c89  */
    /* JADX WARNING: Removed duplicated region for block: B:575:0x1cc8  */
    /* JADX WARNING: Removed duplicated region for block: B:578:0x1cd5  */
    /* JADX WARNING: Removed duplicated region for block: B:597:0x1d98  */
    /* JADX WARNING: Removed duplicated region for block: B:616:0x1e52  */
    /* JADX WARNING: Removed duplicated region for block: B:626:0x1eab  */
    /* JADX WARNING: Removed duplicated region for block: B:629:0x1f55  */
    /* JADX WARNING: Removed duplicated region for block: B:660:0x20a7  */
    /* JADX WARNING: Removed duplicated region for block: B:667:0x20f7  */
    /* JADX WARNING: Removed duplicated region for block: B:670:0x211d  */
    /* JADX WARNING: Removed duplicated region for block: B:676:0x2152  */
    /* JADX WARNING: Removed duplicated region for block: B:681:0x2188  */
    /* JADX WARNING: Removed duplicated region for block: B:684:0x2191  */
    /* JADX WARNING: Removed duplicated region for block: B:689:0x21c7  */
    /* JADX WARNING: Removed duplicated region for block: B:693:0x21ce  */
    /* JADX WARNING: Removed duplicated region for block: B:711:0x227f  */
    /* JADX WARNING: Removed duplicated region for block: B:764:0x2426  */
    /* JADX WARNING: Removed duplicated region for block: B:767:0x2440  */
    /* JADX WARNING: Removed duplicated region for block: B:788:0x256e  */
    public final void dumpLocked(Context context, PrintWriter pw, String prefix, int which, int reqUid, boolean wifiOnly) {
        long screenDozeTime;
        long whichBatteryScreenOffRealtime;
        int NU;
        long dischargeCount;
        long dischargeScreenOnCount;
        long dischargeLightDozeCount;
        long powerSaveModeEnabledTime;
        long deviceLightIdlingTime;
        long deviceIdleModeLightTime;
        long deviceIdlingTime;
        long deviceIdleModeFullTime;
        long phoneOnTime;
        long phoneOnTime2;
        int NU2;
        long btTxTotalBytes;
        long mobileTxTotalPackets;
        long mobileTxTotalPackets2;
        long wifiActiveTime;
        String[] gpsSignalQualityDescription;
        List<BatterySipper> sippers;
        long rawRealtime;
        int i;
        ArrayList<TimerEntry> timers;
        long rawRealtime2;
        long whichBatteryRealtime;
        LongSparseArray<? extends Timer> mMemoryStats;
        StringBuilder sb;
        int i2;
        StringBuilder sb2;
        PrintWriter printWriter;
        long rawRealtime3;
        long rawRealtimeMs;
        int NU3;
        int iu;
        long[] cpuFreqs;
        SparseArray<? extends Uid> uidStats;
        long whichBatteryRealtime2;
        LongSparseArray<? extends Timer> mMemoryStats2;
        Map<String, ? extends Timer> rpmStats;
        long mobileActiveTime;
        long batteryUptime;
        StringBuilder sb3;
        PrintWriter printWriter2;
        long wifiWakeup;
        long wifiRxPackets;
        long mobileTxBytes;
        long wifiWakeup2;
        PrintWriter printWriter3;
        BatteryStats batteryStats;
        long mobileRxBytes;
        long mobileRxPackets;
        long mobileTxPackets;
        long mobileActiveTime2;
        int wifiScanCountBg;
        long mobileTxBytes2;
        StringBuilder sb4;
        boolean z;
        String str;
        long rawRealtime4;
        long wifiTxBytes;
        long wifiRxPackets2;
        long wifiTxPackets;
        PrintWriter printWriter4;
        long wifiRxBytes;
        long fullWifiLockOnTime;
        long wifiScanActualTime;
        long whichBatteryRealtime3;
        long uidWifiRunningTime;
        Uid u;
        long wifiWakeup3;
        long wifiRxBytes2;
        long wifiTxPackets2;
        long wifiRxPackets3;
        int wifiScanCount;
        long uidWifiRunningTime2;
        StringBuilder sb5;
        int wifiScanCountBg2;
        long wifiRxPackets4;
        PrintWriter printWriter5;
        long wifiRxBytes3;
        long wifiWakeup4;
        Uid u2;
        long btTxBytes;
        long btRxBytes;
        Timer bleTimer;
        Timer bleTimer2;
        long rawRealtimeMs2;
        int wifiScanCountBg3;
        int wifiScanCount2;
        long rawRealtime5;
        long btTxBytes2;
        long btRxBytes2;
        int i3;
        ArrayMap<String, ? extends Uid.Wakelock> wakelocks;
        long rawRealtime6;
        ArrayMap<String, ? extends Uid.Wakelock> wakelocks2;
        long btTxBytes3;
        long btRxBytes3;
        long totalDrawWakelock;
        long totalFullWakelock;
        long totalFullWakelock2;
        long totalWindowWakelock;
        int countWakelock;
        long rawRealtimeMs3;
        Uid u3;
        String str2;
        PrintWriter printWriter6;
        Timer mcTimer;
        long rawRealtime7;
        int i4;
        ArrayMap<String, ? extends Timer> syncs;
        ArrayMap<String, ? extends Timer> jobs;
        int ic;
        int NSE;
        int ise;
        String str3;
        long totalStateTime;
        int ips;
        long userCpuTimeUs;
        long systemCpuTimeUs;
        long[] cpuFreqTimes;
        boolean uidActivity;
        long[] screenOffCpuFreqTimes;
        int procState;
        ArrayMap<String, ? extends Uid.Proc> processStats;
        Uid u4;
        boolean uidActivity2;
        ArrayMap<String, ? extends Uid.Pkg> packageStats;
        ArrayMap<String, ? extends Counter> alarms;
        Uid.Pkg ps;
        ArrayMap<String, ? extends Uid.Proc> processStats2;
        Uid u5;
        int numExcessive;
        long foregroundTime;
        ArrayMap<String, ? extends Uid.Proc> processStats3;
        int numCrashes;
        int numExcessive2;
        int starts;
        long userCpuTimeUs2;
        SparseArray<? extends Uid.Sensor> sensors;
        long rawRealtimeMs4;
        long rawRealtime8;
        int NSE2;
        int uid;
        int NU4;
        int bgCount;
        long totalPartialWakelock;
        long j;
        long rawRealtimeMs5;
        long totalDrawWakelock2;
        long rawRealtimeMs6;
        long actualBgPartialWakelock;
        int wifiScanCountBg4;
        long unoptimizedScanTotalTimeBg;
        long uidWifiRunningTime3;
        long wifiScanActualTime2;
        long packets;
        long mobileTxBytes3;
        int numWifiRxBins;
        long wifiTxTotalPackets;
        long rawRealtime9;
        long mobileTxTotalBytes;
        long whichBatteryRealtime4;
        long screenOnTime;
        BatteryStats batteryStats2 = this;
        PrintWriter printWriter7 = pw;
        String str4 = prefix;
        int i5 = which;
        int i6 = reqUid;
        long rawUptime = SystemClock.uptimeMillis() * 1000;
        long rawRealtime10 = SystemClock.elapsedRealtime() * 1000;
        long batteryUptime2 = batteryStats2.getBatteryUptime(rawUptime);
        long rawRealtimeMs7 = (rawRealtime10 + 500) / 1000;
        long whichBatteryUptime = batteryStats2.computeBatteryUptime(rawUptime, i5);
        long batteryUptime3 = batteryUptime2;
        long whichBatteryRealtime5 = batteryStats2.computeBatteryRealtime(rawRealtime10, i5);
        long totalRealtime = batteryStats2.computeRealtime(rawRealtime10, i5);
        long totalUptime = batteryStats2.computeUptime(rawUptime, i5);
        long whichBatteryUptime2 = whichBatteryUptime;
        long whichBatteryScreenOffUptime = batteryStats2.computeBatteryScreenOffUptime(rawUptime, i5);
        long j2 = rawUptime;
        long rawUptime2 = batteryStats2.computeBatteryScreenOffRealtime(rawRealtime10, i5);
        long batteryTimeRemaining = batteryStats2.computeBatteryTimeRemaining(rawRealtime10);
        long chargeTimeRemaining = batteryStats2.computeChargeTimeRemaining(rawRealtime10);
        long whichBatteryScreenOffUptime2 = whichBatteryScreenOffUptime;
        long screenDozeTime2 = batteryStats2.getScreenDozeTime(rawRealtime10, i5);
        StringBuilder sb6 = new StringBuilder(128);
        SparseArray<? extends Uid> uidStats2 = getUidStats();
        long rawRealtime11 = rawRealtime10;
        int NU5 = uidStats2.size();
        int estimatedBatteryCapacity = getEstimatedBatteryCapacity();
        SparseArray<? extends Uid> uidStats3 = uidStats2;
        if (estimatedBatteryCapacity > 0) {
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("  Estimated battery capacity: ");
            screenDozeTime = screenDozeTime2;
            sb6.append(BatteryStatsHelper.makemAh((double) estimatedBatteryCapacity));
            sb6.append(" mAh");
            printWriter7.println(sb6.toString());
        } else {
            screenDozeTime = screenDozeTime2;
        }
        int minLearnedBatteryCapacity = getMinLearnedBatteryCapacity();
        if (minLearnedBatteryCapacity > 0) {
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("  Min learned battery capacity: ");
            int i7 = minLearnedBatteryCapacity;
            sb6.append(BatteryStatsHelper.makemAh((double) (minLearnedBatteryCapacity / 1000)));
            sb6.append(" mAh");
            printWriter7.println(sb6.toString());
        }
        int maxLearnedBatteryCapacity = getMaxLearnedBatteryCapacity();
        if (maxLearnedBatteryCapacity > 0) {
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("  Max learned battery capacity: ");
            int i8 = maxLearnedBatteryCapacity;
            sb6.append(BatteryStatsHelper.makemAh((double) (maxLearnedBatteryCapacity / 1000)));
            sb6.append(" mAh");
            printWriter7.println(sb6.toString());
        }
        sb6.setLength(0);
        sb6.append(str4);
        sb6.append("  Time on battery: ");
        formatTimeMs(sb6, whichBatteryRealtime5 / 1000);
        sb6.append("(");
        sb6.append(batteryStats2.formatRatioLocked(whichBatteryRealtime5, totalRealtime));
        sb6.append(") realtime, ");
        formatTimeMs(sb6, whichBatteryUptime2 / 1000);
        sb6.append("(");
        long whichBatteryUptime3 = whichBatteryUptime2;
        sb6.append(batteryStats2.formatRatioLocked(whichBatteryUptime3, whichBatteryRealtime5));
        sb6.append(") uptime");
        printWriter7.println(sb6.toString());
        sb6.setLength(0);
        sb6.append(str4);
        sb6.append("  Time on battery screen off: ");
        long whichBatteryUptime4 = whichBatteryUptime3;
        formatTimeMs(sb6, rawUptime2 / 1000);
        sb6.append("(");
        sb6.append(batteryStats2.formatRatioLocked(rawUptime2, whichBatteryRealtime5));
        sb6.append(") realtime, ");
        formatTimeMs(sb6, whichBatteryScreenOffUptime2 / 1000);
        sb6.append("(");
        long whichBatteryScreenOffUptime3 = whichBatteryScreenOffUptime2;
        sb6.append(batteryStats2.formatRatioLocked(whichBatteryScreenOffUptime3, whichBatteryRealtime5));
        sb6.append(") uptime");
        printWriter7.println(sb6.toString());
        sb6.setLength(0);
        sb6.append(str4);
        sb6.append("  Time on battery screen doze: ");
        long whichBatteryScreenOffUptime4 = whichBatteryScreenOffUptime3;
        formatTimeMs(sb6, screenDozeTime / 1000);
        sb6.append("(");
        long screenDozeTime3 = screenDozeTime;
        sb6.append(batteryStats2.formatRatioLocked(screenDozeTime3, whichBatteryRealtime5));
        sb6.append(")");
        printWriter7.println(sb6.toString());
        sb6.setLength(0);
        sb6.append(str4);
        sb6.append("  Total run time: ");
        long screenDozeTime4 = screenDozeTime3;
        formatTimeMs(sb6, totalRealtime / 1000);
        sb6.append("realtime, ");
        formatTimeMs(sb6, totalUptime / 1000);
        sb6.append("uptime");
        printWriter7.println(sb6.toString());
        if (batteryTimeRemaining >= 0) {
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("  Battery time remaining: ");
            formatTimeMs(sb6, batteryTimeRemaining / 1000);
            printWriter7.println(sb6.toString());
        }
        if (chargeTimeRemaining >= 0) {
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("  Charge time remaining: ");
            formatTimeMs(sb6, chargeTimeRemaining / 1000);
            printWriter7.println(sb6.toString());
        }
        long dischargeCount2 = batteryStats2.getUahDischarge(i5);
        if (dischargeCount2 >= 0) {
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("  Discharge: ");
            whichBatteryScreenOffRealtime = rawUptime2;
            sb6.append(BatteryStatsHelper.makemAh(((double) dischargeCount2) / 1000.0d));
            sb6.append(" mAh");
            printWriter7.println(sb6.toString());
        } else {
            whichBatteryScreenOffRealtime = rawUptime2;
        }
        long dischargeScreenOffCount = batteryStats2.getUahDischargeScreenOff(i5);
        if (dischargeScreenOffCount >= 0) {
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("  Screen off discharge: ");
            long j3 = totalRealtime;
            sb6.append(BatteryStatsHelper.makemAh(((double) dischargeScreenOffCount) / 1000.0d));
            sb6.append(" mAh");
            printWriter7.println(sb6.toString());
        }
        long dischargeScreenDozeCount = batteryStats2.getUahDischargeScreenDoze(i5);
        if (dischargeScreenDozeCount >= 0) {
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("  Screen doze discharge: ");
            NU = NU5;
            int i9 = estimatedBatteryCapacity;
            sb6.append(BatteryStatsHelper.makemAh(((double) dischargeScreenDozeCount) / 1000.0d));
            sb6.append(" mAh");
            printWriter7.println(sb6.toString());
        } else {
            NU = NU5;
            int i10 = estimatedBatteryCapacity;
        }
        long dischargeScreenOnCount2 = dischargeCount2 - dischargeScreenOffCount;
        if (dischargeScreenOnCount2 >= 0) {
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("  Screen on discharge: ");
            dischargeCount = dischargeCount2;
            sb6.append(BatteryStatsHelper.makemAh(((double) dischargeScreenOnCount2) / 1000.0d));
            sb6.append(" mAh");
            printWriter7.println(sb6.toString());
        } else {
            dischargeCount = dischargeCount2;
        }
        long dischargeCount3 = batteryStats2.getUahDischargeLightDoze(i5);
        if (dischargeCount3 >= 0) {
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("  Device light doze discharge: ");
            dischargeScreenOnCount = dischargeScreenOnCount2;
            sb6.append(BatteryStatsHelper.makemAh(((double) dischargeCount3) / 1000.0d));
            sb6.append(" mAh");
            printWriter7.println(sb6.toString());
        } else {
            dischargeScreenOnCount = dischargeScreenOnCount2;
        }
        long dischargeScreenOnCount3 = batteryStats2.getUahDischargeDeepDoze(i5);
        if (dischargeScreenOnCount3 >= 0) {
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("  Device deep doze discharge: ");
            dischargeLightDozeCount = dischargeCount3;
            sb6.append(BatteryStatsHelper.makemAh(((double) dischargeScreenOnCount3) / 1000.0d));
            sb6.append(" mAh");
            printWriter7.println(sb6.toString());
        } else {
            dischargeLightDozeCount = dischargeCount3;
        }
        printWriter7.print("  Start clock time: ");
        printWriter7.println(DateFormat.format((CharSequence) "yyyy-MM-dd-HH-mm-ss", getStartClockTime()).toString());
        long screenOnTime2 = dischargeScreenDozeCount;
        long rawRealtime12 = rawRealtime11;
        long screenOnTime3 = batteryStats2.getScreenOnTime(rawRealtime12, i5);
        long whichBatteryScreenOffRealtime2 = dischargeScreenOffCount;
        long interactiveTime = batteryStats2.getInteractiveTime(rawRealtime12, i5);
        long dischargeDeepDozeCount = dischargeScreenOnCount3;
        long powerSaveModeEnabledTime2 = batteryStats2.getPowerSaveModeEnabledTime(rawRealtime12, i5);
        long deviceIdleModeLightTime2 = batteryStats2.getDeviceIdleModeTime(1, rawRealtime12, i5);
        long deviceIdleModeFullTime2 = batteryStats2.getDeviceIdleModeTime(2, rawRealtime12, i5);
        long deviceLightIdlingTime2 = batteryStats2.getDeviceIdlingTime(1, rawRealtime12, i5);
        long deviceIdlingTime2 = batteryStats2.getDeviceIdlingTime(2, rawRealtime12, i5);
        long phoneOnTime3 = batteryStats2.getPhoneOnTime(rawRealtime12, i5);
        long globalWifiRunningTime = batteryStats2.getGlobalWifiRunningTime(rawRealtime12, i5);
        long wifiOnTime = batteryStats2.getWifiOnTime(rawRealtime12, i5);
        sb6.setLength(0);
        sb6.append(str4);
        sb6.append("  Screen on: ");
        long phoneOnTime4 = phoneOnTime3;
        formatTimeMs(sb6, screenOnTime3 / 1000);
        sb6.append("(");
        sb6.append(batteryStats2.formatRatioLocked(screenOnTime3, whichBatteryRealtime5));
        sb6.append(") ");
        sb6.append(batteryStats2.getScreenOnCount(i5));
        sb6.append("x, Interactive: ");
        formatTimeMs(sb6, interactiveTime / 1000);
        sb6.append("(");
        sb6.append(batteryStats2.formatRatioLocked(interactiveTime, whichBatteryRealtime5));
        sb6.append(")");
        printWriter7.println(sb6.toString());
        sb6.setLength(0);
        sb6.append(str4);
        sb6.append("  Screen brightnesses:");
        boolean didOne = false;
        int i11 = 0;
        while (i11 < 5) {
            long interactiveTime2 = interactiveTime;
            long time = batteryStats2.getScreenBrightnessTime(i11, rawRealtime12, i5);
            if (time != 0) {
                sb6.append("\n    ");
                sb6.append(str4);
                sb6.append(SCREEN_BRIGHTNESS_NAMES[i11]);
                sb6.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                formatTimeMs(sb6, time / 1000);
                sb6.append("(");
                sb6.append(batteryStats2.formatRatioLocked(time, screenOnTime3));
                sb6.append(")");
                didOne = true;
            }
            i11++;
            interactiveTime = interactiveTime2;
        }
        long dischargeScreenOffCount2 = interactiveTime;
        if (!didOne) {
            sb6.append(" (no activity)");
        }
        printWriter7.println(sb6.toString());
        if (powerSaveModeEnabledTime2 != 0) {
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("  Power save mode enabled: ");
            formatTimeMs(sb6, powerSaveModeEnabledTime2 / 1000);
            sb6.append("(");
            powerSaveModeEnabledTime = powerSaveModeEnabledTime2;
            sb6.append(batteryStats2.formatRatioLocked(powerSaveModeEnabledTime, whichBatteryRealtime5));
            sb6.append(")");
            printWriter7.println(sb6.toString());
        } else {
            powerSaveModeEnabledTime = powerSaveModeEnabledTime2;
        }
        if (deviceLightIdlingTime2 != 0) {
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("  Device light idling: ");
            formatTimeMs(sb6, deviceLightIdlingTime2 / 1000);
            sb6.append("(");
            boolean z2 = didOne;
            long deviceLightIdlingTime3 = deviceLightIdlingTime2;
            batteryStats2 = this;
            sb6.append(batteryStats2.formatRatioLocked(deviceLightIdlingTime3, whichBatteryRealtime5));
            sb6.append(") ");
            deviceLightIdlingTime = deviceLightIdlingTime3;
            sb6.append(batteryStats2.getDeviceIdlingCount(1, i5));
            sb6.append("x");
            printWriter7.println(sb6.toString());
        } else {
            deviceLightIdlingTime = deviceLightIdlingTime2;
        }
        if (deviceIdleModeLightTime2 != 0) {
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("  Idle mode light time: ");
            formatTimeMs(sb6, deviceIdleModeLightTime2 / 1000);
            sb6.append("(");
            long deviceIdleModeLightTime3 = deviceIdleModeLightTime2;
            sb6.append(batteryStats2.formatRatioLocked(deviceIdleModeLightTime3, whichBatteryRealtime5));
            sb6.append(") ");
            deviceIdleModeLightTime = deviceIdleModeLightTime3;
            sb6.append(batteryStats2.getDeviceIdleModeCount(1, i5));
            sb6.append("x");
            sb6.append(" -- longest ");
            formatTimeMs(sb6, batteryStats2.getLongestDeviceIdleModeTime(1));
            printWriter7.println(sb6.toString());
        } else {
            deviceIdleModeLightTime = deviceIdleModeLightTime2;
        }
        if (deviceIdlingTime2 != 0) {
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("  Device full idling: ");
            formatTimeMs(sb6, deviceIdlingTime2 / 1000);
            sb6.append("(");
            long deviceIdlingTime3 = deviceIdlingTime2;
            sb6.append(batteryStats2.formatRatioLocked(deviceIdlingTime3, whichBatteryRealtime5));
            sb6.append(") ");
            deviceIdlingTime = deviceIdlingTime3;
            sb6.append(batteryStats2.getDeviceIdlingCount(2, i5));
            sb6.append("x");
            printWriter7.println(sb6.toString());
        } else {
            deviceIdlingTime = deviceIdlingTime2;
        }
        if (deviceIdleModeFullTime2 != 0) {
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("  Idle mode full time: ");
            formatTimeMs(sb6, deviceIdleModeFullTime2 / 1000);
            sb6.append("(");
            long deviceIdleModeFullTime3 = deviceIdleModeFullTime2;
            sb6.append(batteryStats2.formatRatioLocked(deviceIdleModeFullTime3, whichBatteryRealtime5));
            sb6.append(") ");
            deviceIdleModeFullTime = deviceIdleModeFullTime3;
            sb6.append(batteryStats2.getDeviceIdleModeCount(2, i5));
            sb6.append("x");
            sb6.append(" -- longest ");
            formatTimeMs(sb6, batteryStats2.getLongestDeviceIdleModeTime(2));
            printWriter7.println(sb6.toString());
        } else {
            deviceIdleModeFullTime = deviceIdleModeFullTime2;
        }
        if (phoneOnTime4 != 0) {
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("  Active phone call: ");
            formatTimeMs(sb6, phoneOnTime4 / 1000);
            sb6.append("(");
            phoneOnTime = phoneOnTime4;
            sb6.append(batteryStats2.formatRatioLocked(phoneOnTime, whichBatteryRealtime5));
            sb6.append(") ");
            sb6.append(batteryStats2.getPhoneOnCount(i5));
            sb6.append("x");
        } else {
            phoneOnTime = phoneOnTime4;
        }
        int connChanges = batteryStats2.getNumConnectivityChange(i5);
        if (connChanges != 0) {
            pw.print(prefix);
            phoneOnTime2 = phoneOnTime;
            printWriter7.print("  Connectivity changes: ");
            printWriter7.println(connChanges);
        } else {
            phoneOnTime2 = phoneOnTime;
        }
        int connChanges2 = connChanges;
        ArrayList<TimerEntry> timers2 = new ArrayList<>();
        long partialWakeLockTimeTotalMicros = 0;
        long fullWakeLockTimeTotalMicros = 0;
        int iu2 = 0;
        while (true) {
            NU2 = NU;
            if (iu2 >= NU2) {
                break;
            }
            int NU6 = NU2;
            SparseArray<? extends Uid> uidStats4 = uidStats3;
            SparseArray<? extends Uid> uidStats5 = uidStats4;
            Uid u6 = (Uid) uidStats4.valueAt(iu2);
            long powerSaveModeEnabledTime3 = powerSaveModeEnabledTime;
            ArrayMap<String, ? extends Uid.Wakelock> wakelocks3 = u6.getWakelockStats();
            if (wakelocks3 != null) {
                screenOnTime = screenOnTime3;
                int i12 = 1;
                int iw = wakelocks3.size() - 1;
                while (iw >= 0) {
                    Uid.Wakelock wl = (Uid.Wakelock) wakelocks3.valueAt(iw);
                    long whichBatteryRealtime6 = whichBatteryRealtime5;
                    Timer fullWakeTimer = wl.getWakeTime(i12);
                    if (fullWakeTimer != null) {
                        fullWakeLockTimeTotalMicros += fullWakeTimer.getTotalTimeLocked(rawRealtime12, i5);
                    }
                    Timer partialWakeTimer = wl.getWakeTime(0);
                    if (partialWakeTimer != null) {
                        long totalTimeMicros = partialWakeTimer.getTotalTimeLocked(rawRealtime12, i5);
                        if (totalTimeMicros > 0) {
                            if (reqUid < 0) {
                                TimerEntry timerEntry = new TimerEntry(wakelocks3.keyAt(iw), u6.getUid(), partialWakeTimer, totalTimeMicros);
                                timers2.add(timerEntry);
                            }
                            partialWakeLockTimeTotalMicros += totalTimeMicros;
                            iw--;
                            whichBatteryRealtime5 = whichBatteryRealtime6;
                            i12 = 1;
                        }
                    }
                    int i13 = reqUid;
                    iw--;
                    whichBatteryRealtime5 = whichBatteryRealtime6;
                    i12 = 1;
                }
                whichBatteryRealtime4 = whichBatteryRealtime5;
            } else {
                whichBatteryRealtime4 = whichBatteryRealtime5;
                screenOnTime = screenOnTime3;
            }
            int i14 = reqUid;
            iu2++;
            NU = NU6;
            uidStats3 = uidStats5;
            powerSaveModeEnabledTime = powerSaveModeEnabledTime3;
            screenOnTime3 = screenOnTime;
            whichBatteryRealtime5 = whichBatteryRealtime4;
        }
        long whichBatteryRealtime7 = whichBatteryRealtime5;
        int NU7 = NU2;
        long powerSaveModeEnabledTime4 = powerSaveModeEnabledTime;
        long mobileRxTotalBytes = screenOnTime3;
        SparseArray<? extends Uid> uidStats6 = uidStats3;
        int i15 = reqUid;
        long mobileRxTotalBytes2 = batteryStats2.getNetworkActivityBytes(0, i5);
        long mobileTxTotalBytes2 = batteryStats2.getNetworkActivityBytes(1, i5);
        long rawRealtime13 = rawRealtime12;
        long wifiRxTotalBytes = batteryStats2.getNetworkActivityBytes(2, i5);
        long wifiTxTotalBytes = batteryStats2.getNetworkActivityBytes(3, i5);
        long mobileRxTotalPackets = batteryStats2.getNetworkActivityPackets(0, i5);
        long wifiTxTotalBytes2 = wifiTxTotalBytes;
        long wifiTxTotalBytes3 = batteryStats2.getNetworkActivityPackets(1, i5);
        ArrayList<TimerEntry> timers3 = timers2;
        long wifiRxTotalPackets = batteryStats2.getNetworkActivityPackets(2, i5);
        long wifiTxTotalPackets2 = batteryStats2.getNetworkActivityPackets(3, i5);
        long btRxTotalBytes = batteryStats2.getNetworkActivityBytes(4, i5);
        long btTxTotalBytes2 = batteryStats2.getNetworkActivityBytes(5, i5);
        if (fullWakeLockTimeTotalMicros != 0) {
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("  Total full wakelock time: ");
            btTxTotalBytes = btTxTotalBytes2;
            formatTimeMsNoSpace(sb6, (fullWakeLockTimeTotalMicros + 500) / 1000);
            printWriter7.println(sb6.toString());
        } else {
            btTxTotalBytes = btTxTotalBytes2;
        }
        if (partialWakeLockTimeTotalMicros != 0) {
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("  Total partial wakelock time: ");
            formatTimeMsNoSpace(sb6, (partialWakeLockTimeTotalMicros + 500) / 1000);
            printWriter7.println(sb6.toString());
        }
        long rawRealtime14 = rawRealtime13;
        long multicastWakeLockTimeTotalMicros = batteryStats2.getWifiMulticastWakelockTime(rawRealtime14, i5);
        int multicastWakeLockCountTotal = batteryStats2.getWifiMulticastWakelockCount(i5);
        if (multicastWakeLockTimeTotalMicros != 0) {
            mobileTxTotalPackets = wifiTxTotalBytes3;
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("  Total WiFi Multicast wakelock Count: ");
            sb6.append(multicastWakeLockCountTotal);
            printWriter7.println(sb6.toString());
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("  Total WiFi Multicast wakelock time: ");
            formatTimeMsNoSpace(sb6, (multicastWakeLockTimeTotalMicros + 500) / 1000);
            printWriter7.println(sb6.toString());
        } else {
            mobileTxTotalPackets = wifiTxTotalBytes3;
        }
        printWriter7.println("");
        pw.print(prefix);
        sb6.setLength(0);
        sb6.append(str4);
        sb6.append("  CONNECTIVITY POWER SUMMARY START");
        printWriter7.println(sb6.toString());
        pw.print(prefix);
        sb6.setLength(0);
        sb6.append(str4);
        sb6.append("  Logging duration for connectivity statistics: ");
        formatTimeMs(sb6, whichBatteryRealtime7 / 1000);
        printWriter7.println(sb6.toString());
        sb6.setLength(0);
        sb6.append(str4);
        sb6.append("  Cellular Statistics:");
        printWriter7.println(sb6.toString());
        pw.print(prefix);
        sb6.setLength(0);
        sb6.append(str4);
        sb6.append("     Cellular kernel active time: ");
        long mobileActiveTime3 = batteryStats2.getMobileRadioActiveTime(rawRealtime14, i5);
        int multicastWakeLockCountTotal2 = multicastWakeLockCountTotal;
        long rawRealtime15 = rawRealtime14;
        formatTimeMs(sb6, mobileActiveTime3 / 1000);
        sb6.append("(");
        long whichBatteryRealtime8 = whichBatteryRealtime7;
        sb6.append(batteryStats2.formatRatioLocked(mobileActiveTime3, whichBatteryRealtime8));
        sb6.append(")");
        printWriter7.println(sb6.toString());
        printWriter7.print("     Cellular data received: ");
        printWriter7.println(batteryStats2.formatBytesLocked(mobileRxTotalBytes2));
        printWriter7.print("     Cellular data sent: ");
        printWriter7.println(batteryStats2.formatBytesLocked(mobileTxTotalBytes2));
        printWriter7.print("     Cellular packets received: ");
        printWriter7.println(mobileRxTotalPackets);
        printWriter7.print("     Cellular packets sent: ");
        long mobileActiveTime4 = mobileActiveTime3;
        long rawRealtime16 = mobileTxTotalPackets;
        printWriter7.println(rawRealtime16);
        sb6.setLength(0);
        sb6.append(str4);
        sb6.append("     Cellular Radio Access Technology:");
        boolean didOne2 = false;
        int i16 = 0;
        while (true) {
            mobileTxTotalPackets2 = rawRealtime16;
            if (i16 >= 21) {
                break;
            }
            long mobileRxTotalPackets2 = mobileRxTotalPackets;
            long rawRealtime17 = rawRealtime15;
            long time2 = batteryStats2.getPhoneDataConnectionTime(i16, rawRealtime17, i5);
            if (time2 == 0) {
                mobileTxTotalBytes = mobileTxTotalBytes2;
            } else {
                mobileTxTotalBytes = mobileTxTotalBytes2;
                sb6.append("\n       ");
                sb6.append(str4);
                sb6.append(DATA_CONNECTION_NAMES[i16]);
                sb6.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                formatTimeMs(sb6, time2 / 1000);
                sb6.append("(");
                sb6.append(batteryStats2.formatRatioLocked(time2, whichBatteryRealtime8));
                sb6.append(") ");
                didOne2 = true;
            }
            i16++;
            rawRealtime15 = rawRealtime17;
            rawRealtime16 = mobileTxTotalPackets2;
            mobileRxTotalPackets = mobileRxTotalPackets2;
            mobileTxTotalBytes2 = mobileTxTotalBytes;
        }
        long mobileRxTotalPackets3 = mobileRxTotalPackets;
        long mobileTxTotalBytes3 = mobileTxTotalBytes2;
        long rawRealtime18 = rawRealtime15;
        if (!didOne2) {
            sb6.append(" (no activity)");
        }
        printWriter7.println(sb6.toString());
        sb6.setLength(0);
        sb6.append(str4);
        sb6.append("     Cellular Rx signal strength (RSRP):");
        String[] cellularRxSignalStrengthDescription = {"very poor (less than -128dBm): ", "poor (-128dBm to -118dBm): ", "moderate (-118dBm to -108dBm): ", "good (-108dBm to -98dBm): ", "great (greater than -98dBm): "};
        int numCellularRxBins = Math.min(5, cellularRxSignalStrengthDescription.length);
        boolean didOne3 = false;
        int i17 = 0;
        while (i17 < numCellularRxBins) {
            long time3 = batteryStats2.getPhoneSignalStrengthTime(i17, rawRealtime18, i5);
            if (time3 == 0) {
                rawRealtime9 = rawRealtime18;
            } else {
                rawRealtime9 = rawRealtime18;
                sb6.append("\n       ");
                sb6.append(str4);
                sb6.append(cellularRxSignalStrengthDescription[i17]);
                sb6.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                formatTimeMs(sb6, time3 / 1000);
                sb6.append("(");
                sb6.append(batteryStats2.formatRatioLocked(time3, whichBatteryRealtime8));
                sb6.append(") ");
                didOne3 = true;
            }
            i17++;
            rawRealtime18 = rawRealtime9;
        }
        long rawRealtime19 = rawRealtime18;
        if (!didOne3) {
            sb6.append(" (no activity)");
        }
        printWriter7.println(sb6.toString());
        int numCellularRxBins2 = numCellularRxBins;
        String[] cellularRxSignalStrengthDescription2 = cellularRxSignalStrengthDescription;
        long mobileRxTotalBytes3 = mobileRxTotalBytes2;
        int i18 = connChanges2;
        long wifiRxTotalPackets2 = wifiRxTotalPackets;
        long btRxTotalBytes2 = btRxTotalBytes;
        long btTxTotalBytes3 = btTxTotalBytes;
        int i19 = multicastWakeLockCountTotal2;
        long batteryUptime4 = batteryUptime3;
        ArrayList<TimerEntry> timers4 = timers3;
        long rawRealtimeMs8 = rawRealtimeMs7;
        long rawRealtime20 = whichBatteryUptime4;
        long whichBatteryUptime5 = whichBatteryScreenOffUptime4;
        long whichBatteryScreenOffUptime5 = screenDozeTime4;
        long screenDozeTime5 = dischargeCount;
        long dischargeCount4 = dischargeLightDozeCount;
        long mobileActiveTime5 = mobileActiveTime4;
        long dischargeLightDozeCount2 = mobileTxTotalPackets2;
        long rawRealtime21 = dischargeScreenOnCount;
        long dischargeScreenOnCount4 = dischargeDeepDozeCount;
        long j4 = deviceLightIdlingTime;
        long j5 = deviceIdleModeLightTime;
        long j6 = deviceIdlingTime;
        long j7 = deviceIdleModeFullTime;
        long j8 = phoneOnTime2;
        int NU8 = NU7;
        long dischargeDeepDozeCount2 = powerSaveModeEnabledTime4;
        long j9 = mobileRxTotalPackets3;
        SparseArray<? extends Uid> uidStats7 = uidStats6;
        batteryStats2.printControllerActivity(printWriter7, sb6, str4, CELLULAR_CONTROLLER_NAME, getModemControllerActivity(), i5);
        pw.print(prefix);
        sb6.setLength(0);
        sb6.append(str4);
        sb6.append("  Wifi Statistics:");
        printWriter7.println(sb6.toString());
        pw.print(prefix);
        sb6.setLength(0);
        sb6.append(str4);
        sb6.append("     Wifi kernel active time: ");
        long rawRealtime22 = rawRealtime19;
        long wifiActiveTime2 = batteryStats2.getWifiActiveTime(rawRealtime22, i5);
        formatTimeMs(sb6, wifiActiveTime2 / 1000);
        sb6.append("(");
        long whichBatteryRealtime9 = whichBatteryRealtime8;
        sb6.append(batteryStats2.formatRatioLocked(wifiActiveTime2, whichBatteryRealtime9));
        sb6.append(")");
        printWriter7.println(sb6.toString());
        printWriter7.print("     Wifi data received: ");
        long wifiRxTotalBytes2 = wifiRxTotalBytes;
        printWriter7.println(batteryStats2.formatBytesLocked(wifiRxTotalBytes2));
        printWriter7.print("     Wifi data sent: ");
        long wifiRxTotalBytes3 = wifiRxTotalBytes2;
        long wifiRxTotalBytes4 = wifiTxTotalBytes2;
        printWriter7.println(batteryStats2.formatBytesLocked(wifiRxTotalBytes4));
        printWriter7.print("     Wifi packets received: ");
        printWriter7.println(wifiRxTotalPackets2);
        printWriter7.print("     Wifi packets sent: ");
        long wifiTxTotalBytes4 = wifiRxTotalBytes4;
        long wifiTxTotalPackets3 = wifiTxTotalPackets2;
        printWriter7.println(wifiTxTotalPackets3);
        sb6.setLength(0);
        sb6.append(str4);
        sb6.append("     Wifi states:");
        boolean didOne4 = false;
        int i20 = 0;
        while (true) {
            wifiActiveTime = wifiActiveTime2;
            if (i20 >= 8) {
                break;
            }
            long time4 = batteryStats2.getWifiStateTime(i20, rawRealtime22, i5);
            if (time4 == 0) {
                wifiTxTotalPackets = wifiTxTotalPackets3;
            } else {
                wifiTxTotalPackets = wifiTxTotalPackets3;
                sb6.append("\n       ");
                sb6.append(WIFI_STATE_NAMES[i20]);
                sb6.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                formatTimeMs(sb6, time4 / 1000);
                sb6.append("(");
                sb6.append(batteryStats2.formatRatioLocked(time4, whichBatteryRealtime9));
                sb6.append(") ");
                didOne4 = true;
            }
            i20++;
            wifiActiveTime2 = wifiActiveTime;
            wifiTxTotalPackets3 = wifiTxTotalPackets;
        }
        long wifiTxTotalPackets4 = wifiTxTotalPackets3;
        if (!didOne4) {
            sb6.append(" (no activity)");
        }
        printWriter7.println(sb6.toString());
        sb6.setLength(0);
        sb6.append(str4);
        sb6.append("     Wifi supplicant states:");
        boolean didOne5 = false;
        for (int i21 = 0; i21 < 13; i21++) {
            long time5 = batteryStats2.getWifiSupplStateTime(i21, rawRealtime22, i5);
            if (time5 != 0) {
                sb6.append("\n       ");
                sb6.append(WIFI_SUPPL_STATE_NAMES[i21]);
                sb6.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                formatTimeMs(sb6, time5 / 1000);
                sb6.append("(");
                sb6.append(batteryStats2.formatRatioLocked(time5, whichBatteryRealtime9));
                sb6.append(") ");
                didOne5 = true;
            }
        }
        if (!didOne5) {
            sb6.append(" (no activity)");
        }
        printWriter7.println(sb6.toString());
        sb6.setLength(0);
        sb6.append(str4);
        sb6.append("     Wifi Rx signal strength (RSSI):");
        boolean z3 = didOne5;
        String[] wifiRxSignalStrengthDescription = {"very poor (less than -88.75dBm): ", "poor (-88.75 to -77.5dBm): ", "moderate (-77.5dBm to -66.25dBm): ", "good (-66.25dBm to -55dBm): ", "great (greater than -55dBm): "};
        int numWifiRxBins2 = Math.min(5, wifiRxSignalStrengthDescription.length);
        boolean didOne6 = false;
        int i22 = 0;
        while (i22 < numWifiRxBins2) {
            String[] wifiRxSignalStrengthDescription2 = wifiRxSignalStrengthDescription;
            long time6 = batteryStats2.getWifiSignalStrengthTime(i22, rawRealtime22, i5);
            if (time6 == 0) {
                numWifiRxBins = numWifiRxBins2;
            } else {
                sb6.append("\n    ");
                sb6.append(str4);
                sb6.append("     ");
                sb6.append(wifiRxSignalStrengthDescription2[i22]);
                numWifiRxBins = numWifiRxBins2;
                formatTimeMs(sb6, time6 / 1000);
                sb6.append("(");
                sb6.append(batteryStats2.formatRatioLocked(time6, whichBatteryRealtime9));
                sb6.append(") ");
                didOne6 = true;
            }
            i22++;
            wifiRxSignalStrengthDescription = wifiRxSignalStrengthDescription2;
            numWifiRxBins2 = numWifiRxBins;
        }
        int numWifiRxBins3 = numWifiRxBins2;
        String[] wifiRxSignalStrengthDescription3 = wifiRxSignalStrengthDescription;
        if (!didOne6) {
            sb6.append(" (no activity)");
        }
        printWriter7.println(sb6.toString());
        long wifiRxTotalPackets3 = wifiRxTotalPackets2;
        long j10 = wifiActiveTime;
        int i23 = numWifiRxBins3;
        long whichBatteryRealtime10 = whichBatteryRealtime9;
        long rawRealtime23 = rawRealtime22;
        batteryStats2.printControllerActivity(printWriter7, sb6, str4, WIFI_CONTROLLER_NAME, getWifiControllerActivity(), i5);
        pw.print(prefix);
        sb6.setLength(0);
        sb6.append(str4);
        sb6.append("  GPS Statistics:");
        printWriter7.println(sb6.toString());
        sb6.setLength(0);
        sb6.append(str4);
        sb6.append("     GPS signal quality (Top 4 Average CN0):");
        String[] gpsSignalQualityDescription2 = {"poor (less than 20 dBHz): ", "good (greater than 20 dBHz): "};
        int numGpsSignalQualityBins = Math.min(2, gpsSignalQualityDescription2.length);
        for (int i24 = 0; i24 < numGpsSignalQualityBins; i24++) {
            long time7 = batteryStats2.getGpsSignalQualityTime(i24, rawRealtime23, i5);
            sb6.append("\n    ");
            sb6.append(str4);
            sb6.append("  ");
            sb6.append(gpsSignalQualityDescription2[i24]);
            formatTimeMs(sb6, time7 / 1000);
            sb6.append("(");
            sb6.append(batteryStats2.formatRatioLocked(time7, whichBatteryRealtime10));
            sb6.append(") ");
        }
        long whichBatteryRealtime11 = whichBatteryRealtime10;
        printWriter7.println(sb6.toString());
        long gpsBatteryDrainMaMs = getGpsBatteryDrainMaMs();
        if (gpsBatteryDrainMaMs > 0) {
            pw.print(prefix);
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("     Battery Drain (mAh): ");
            sb6.append(Double.toString(((double) gpsBatteryDrainMaMs) / 3600000.0d));
            printWriter7.println(sb6.toString());
        }
        pw.print(prefix);
        sb6.setLength(0);
        sb6.append(str4);
        sb6.append("  CONNECTIVITY POWER SUMMARY END");
        printWriter7.println(sb6.toString());
        printWriter7.println("");
        pw.print(prefix);
        printWriter7.print("  Bluetooth total received: ");
        long btRxTotalBytes3 = btRxTotalBytes2;
        printWriter7.print(batteryStats2.formatBytesLocked(btRxTotalBytes3));
        printWriter7.print(", sent: ");
        long btTxTotalBytes4 = btTxTotalBytes3;
        printWriter7.println(batteryStats2.formatBytesLocked(btTxTotalBytes4));
        long rawRealtime24 = rawRealtime23;
        long bluetoothScanTimeMs = batteryStats2.getBluetoothScanTime(rawRealtime23, i5) / 1000;
        sb6.setLength(0);
        sb6.append(str4);
        sb6.append("  Bluetooth scan time: ");
        formatTimeMs(sb6, bluetoothScanTimeMs);
        printWriter7.println(sb6.toString());
        long j11 = btRxTotalBytes3;
        long j12 = gpsBatteryDrainMaMs;
        long bluetoothScanTimeMs2 = bluetoothScanTimeMs;
        long whichBatteryRealtime12 = whichBatteryRealtime11;
        long j13 = btTxTotalBytes4;
        int i25 = numGpsSignalQualityBins;
        batteryStats2.printControllerActivity(printWriter7, sb6, str4, "Bluetooth", getBluetoothControllerActivity(), i5);
        pw.println();
        if (i5 == 2) {
            if (getIsOnBattery()) {
                pw.print(prefix);
                printWriter7.println("  Device is currently unplugged");
                pw.print(prefix);
                printWriter7.print("    Discharge cycle start level: ");
                printWriter7.println(getDischargeStartLevel());
                pw.print(prefix);
                printWriter7.print("    Discharge cycle current level: ");
                printWriter7.println(getDischargeCurrentLevel());
            } else {
                pw.print(prefix);
                printWriter7.println("  Device is currently plugged into power");
                pw.print(prefix);
                printWriter7.print("    Last discharge cycle start level: ");
                printWriter7.println(getDischargeStartLevel());
                pw.print(prefix);
                printWriter7.print("    Last discharge cycle end level: ");
                printWriter7.println(getDischargeCurrentLevel());
            }
            pw.print(prefix);
            printWriter7.print("    Amount discharged while screen on: ");
            printWriter7.println(getDischargeAmountScreenOn());
            pw.print(prefix);
            printWriter7.print("    Amount discharged while screen off: ");
            printWriter7.println(getDischargeAmountScreenOff());
            pw.print(prefix);
            printWriter7.print("    Amount discharged while screen doze: ");
            printWriter7.println(getDischargeAmountScreenDoze());
            printWriter7.println(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        } else {
            pw.print(prefix);
            printWriter7.println("  Device battery use since last full charge");
            pw.print(prefix);
            printWriter7.print("    Amount discharged (lower bound): ");
            printWriter7.println(getLowDischargeAmountSinceCharge());
            pw.print(prefix);
            printWriter7.print("    Amount discharged (upper bound): ");
            printWriter7.println(getHighDischargeAmountSinceCharge());
            pw.print(prefix);
            printWriter7.print("    Amount discharged while screen on: ");
            printWriter7.println(getDischargeAmountScreenOnSinceCharge());
            pw.print(prefix);
            printWriter7.print("    Amount discharged while screen off: ");
            printWriter7.println(getDischargeAmountScreenOffSinceCharge());
            pw.print(prefix);
            printWriter7.print("    Amount discharged while screen doze: ");
            printWriter7.println(getDischargeAmountScreenDozeSinceCharge());
            pw.println();
        }
        BatteryStatsHelper helper = new BatteryStatsHelper(context, false, wifiOnly);
        helper.create(batteryStats2);
        helper.refreshStats(i5, -1);
        List<BatterySipper> sippers2 = helper.getUsageList();
        if (sippers2 == null || sippers2.size() <= 0) {
            gpsSignalQualityDescription = gpsSignalQualityDescription2;
        } else {
            pw.print(prefix);
            printWriter7.println("  Estimated power use (mAh):");
            pw.print(prefix);
            printWriter7.print("    Capacity: ");
            batteryStats2.printmAh(printWriter7, helper.getPowerProfile().getBatteryCapacity());
            printWriter7.print(", Computed drain: ");
            batteryStats2.printmAh(printWriter7, helper.getComputedPower());
            printWriter7.print(", actual drain: ");
            batteryStats2.printmAh(printWriter7, helper.getMinDrainedPower());
            if (helper.getMinDrainedPower() != helper.getMaxDrainedPower()) {
                printWriter7.print("-");
                batteryStats2.printmAh(printWriter7, helper.getMaxDrainedPower());
            }
            pw.println();
            int i26 = 0;
            while (i26 < sippers2.size()) {
                BatterySipper bs = sippers2.get(i26);
                pw.print(prefix);
                switch (AnonymousClass2.$SwitchMap$com$android$internal$os$BatterySipper$DrainType[bs.drainType.ordinal()]) {
                    case 1:
                        printWriter7.print("    Ambient display: ");
                        break;
                    case 2:
                        printWriter7.print("    Idle: ");
                        break;
                    case 3:
                        printWriter7.print("    Cell standby: ");
                        break;
                    case 4:
                        printWriter7.print("    Phone calls: ");
                        break;
                    case 5:
                        printWriter7.print("    Wifi: ");
                        break;
                    case 6:
                        printWriter7.print("    Bluetooth: ");
                        break;
                    case 7:
                        printWriter7.print("    Screen: ");
                        break;
                    case 8:
                        printWriter7.print("    Flashlight: ");
                        break;
                    case 9:
                        printWriter7.print("    Uid ");
                        UserHandle.formatUid(printWriter7, bs.uidObj.getUid());
                        printWriter7.print(": ");
                        break;
                    case 10:
                        printWriter7.print("    User ");
                        printWriter7.print(bs.userId);
                        printWriter7.print(": ");
                        break;
                    case 11:
                        printWriter7.print("    Unaccounted: ");
                        break;
                    case 12:
                        printWriter7.print("    Over-counted: ");
                        break;
                    case 13:
                        printWriter7.print("    Camera: ");
                        break;
                    default:
                        printWriter7.print("    ???: ");
                        break;
                }
                batteryStats2.printmAh(printWriter7, bs.totalPowerMah);
                String[] gpsSignalQualityDescription3 = gpsSignalQualityDescription2;
                if (bs.usagePowerMah != bs.totalPowerMah) {
                    printWriter7.print(" (");
                    if (bs.usagePowerMah != 0.0d) {
                        printWriter7.print(" usage=");
                        batteryStats2.printmAh(printWriter7, bs.usagePowerMah);
                    }
                    if (bs.cpuPowerMah != 0.0d) {
                        printWriter7.print(" cpu=");
                        batteryStats2.printmAh(printWriter7, bs.cpuPowerMah);
                    }
                    if (bs.wakeLockPowerMah != 0.0d) {
                        printWriter7.print(" wake=");
                        batteryStats2.printmAh(printWriter7, bs.wakeLockPowerMah);
                    }
                    if (bs.mobileRadioPowerMah != 0.0d) {
                        printWriter7.print(" radio=");
                        batteryStats2.printmAh(printWriter7, bs.mobileRadioPowerMah);
                    }
                    if (bs.wifiPowerMah != 0.0d) {
                        printWriter7.print(" wifi=");
                        batteryStats2.printmAh(printWriter7, bs.wifiPowerMah);
                    }
                    if (bs.bluetoothPowerMah != 0.0d) {
                        printWriter7.print(" bt=");
                        batteryStats2.printmAh(printWriter7, bs.bluetoothPowerMah);
                    }
                    if (bs.gpsPowerMah != 0.0d) {
                        printWriter7.print(" gps=");
                        batteryStats2.printmAh(printWriter7, bs.gpsPowerMah);
                    }
                    if (bs.sensorPowerMah != 0.0d) {
                        printWriter7.print(" sensor=");
                        batteryStats2.printmAh(printWriter7, bs.sensorPowerMah);
                    }
                    if (bs.cameraPowerMah != 0.0d) {
                        printWriter7.print(" camera=");
                        batteryStats2.printmAh(printWriter7, bs.cameraPowerMah);
                    }
                    if (bs.flashlightPowerMah != 0.0d) {
                        printWriter7.print(" flash=");
                        batteryStats2.printmAh(printWriter7, bs.flashlightPowerMah);
                    }
                    printWriter7.print(" )");
                }
                if (bs.totalSmearedPowerMah != bs.totalPowerMah) {
                    printWriter7.print(" Including smearing: ");
                    batteryStats2.printmAh(printWriter7, bs.totalSmearedPowerMah);
                    printWriter7.print(" (");
                    if (bs.screenPowerMah != 0.0d) {
                        printWriter7.print(" screen=");
                        batteryStats2.printmAh(printWriter7, bs.screenPowerMah);
                    }
                    if (bs.proportionalSmearMah != 0.0d) {
                        printWriter7.print(" proportional=");
                        batteryStats2.printmAh(printWriter7, bs.proportionalSmearMah);
                    }
                    printWriter7.print(" )");
                }
                if (bs.shouldHide) {
                    printWriter7.print(" Excluded from smearing");
                }
                pw.println();
                i26++;
                gpsSignalQualityDescription2 = gpsSignalQualityDescription3;
                boolean z4 = wifiOnly;
                Context context2 = context;
            }
            gpsSignalQualityDescription = gpsSignalQualityDescription2;
            pw.println();
        }
        List<BatterySipper> sippers3 = helper.getMobilemsppList();
        if (sippers3 == null || sippers3.size() <= 0) {
            sippers = sippers3;
        } else {
            pw.print(prefix);
            printWriter7.println("  Per-app mobile ms per packet:");
            long totalTime = 0;
            int i27 = 0;
            while (i27 < sippers3.size()) {
                BatterySipper bs2 = sippers3.get(i27);
                sb6.setLength(0);
                sb6.append(str4);
                sb6.append("    Uid ");
                UserHandle.formatUid(sb6, bs2.uidObj.getUid());
                sb6.append(": ");
                sb6.append(BatteryStatsHelper.makemAh(bs2.mobilemspp));
                sb6.append(" (");
                sb6.append(bs2.mobileRxPackets + bs2.mobileTxPackets);
                sb6.append(" packets over ");
                formatTimeMsNoSpace(sb6, bs2.mobileActive);
                sb6.append(") ");
                sb6.append(bs2.mobileActiveCount);
                sb6.append("x");
                printWriter7.println(sb6.toString());
                totalTime += bs2.mobileActive;
                i27++;
                sippers3 = sippers3;
            }
            sippers = sippers3;
            sb6.setLength(0);
            sb6.append(str4);
            sb6.append("    TOTAL TIME: ");
            formatTimeMs(sb6, totalTime);
            sb6.append("(");
            sb6.append(batteryStats2.formatRatioLocked(totalTime, whichBatteryRealtime12));
            sb6.append(")");
            printWriter7.println(sb6.toString());
            pw.println();
        }
        AnonymousClass1 r11 = new Comparator<TimerEntry>() {
            public int compare(TimerEntry lhs, TimerEntry rhs) {
                long lhsTime = lhs.mTime;
                long rhsTime = rhs.mTime;
                if (lhsTime < rhsTime) {
                    return 1;
                }
                if (lhsTime > rhsTime) {
                    return -1;
                }
                return 0;
            }
        };
        if (i15 < 0) {
            Map<String, ? extends Timer> kernelWakelocks = getKernelWakelockStats();
            if (kernelWakelocks == null || kernelWakelocks.size() <= 0) {
                rawRealtime = rawRealtime24;
                i = -1;
            } else {
                ArrayList<TimerEntry> ktimers = new ArrayList<>();
                for (Map.Entry<String, ? extends Timer> ent : kernelWakelocks.entrySet()) {
                    Timer timer = (Timer) ent.getValue();
                    BatteryStatsHelper helper2 = helper;
                    long rawRealtime25 = rawRealtime24;
                    long totalTimeMillis = computeWakeLock(timer, rawRealtime25, i5);
                    if (totalTimeMillis > 0) {
                        TimerEntry timerEntry2 = new TimerEntry(ent.getKey(), 0, timer, totalTimeMillis);
                        ktimers.add(timerEntry2);
                    }
                    rawRealtime24 = rawRealtime25;
                    helper = helper2;
                }
                BatteryStatsHelper helper3 = helper;
                long rawRealtime26 = rawRealtime24;
                if (ktimers.size() > 0) {
                    Collections.sort(ktimers, r11);
                    pw.print(prefix);
                    printWriter7.println("  All kernel wake locks:");
                    int i28 = 0;
                    while (true) {
                        int i29 = i28;
                        if (i29 < ktimers.size()) {
                            TimerEntry timer2 = ktimers.get(i29);
                            sb6.setLength(0);
                            sb6.append(str4);
                            sb6.append("  Kernel Wake lock ");
                            sb6.append(timer2.mName);
                            int i30 = i29;
                            TimerEntry timerEntry3 = timer2;
                            long rawRealtime27 = rawRealtime26;
                            BatteryStatsHelper helper4 = helper3;
                            ArrayList<TimerEntry> ktimers2 = ktimers;
                            if (!printWakeLock(sb6, timer2.mTimer, rawRealtime26, null, i5, ": ").equals(": ")) {
                                sb6.append(" realtime");
                                printWriter7.println(sb6.toString());
                            }
                            i28 = i30 + 1;
                            ktimers = ktimers2;
                            helper3 = helper4;
                            rawRealtime26 = rawRealtime27;
                        } else {
                            rawRealtime = rawRealtime26;
                            ArrayList<TimerEntry> arrayList = ktimers;
                            BatteryStatsHelper batteryStatsHelper = helper3;
                            i = -1;
                            pw.println();
                        }
                    }
                } else {
                    rawRealtime = rawRealtime26;
                    BatteryStatsHelper batteryStatsHelper2 = helper3;
                    i = -1;
                }
            }
            ArrayList<TimerEntry> timers5 = timers4;
            if (timers5.size() > 0) {
                Collections.sort(timers5, r11);
                pw.print(prefix);
                printWriter7.println("  All partial wake locks:");
                int i31 = 0;
                while (true) {
                    int i32 = i31;
                    if (i32 < timers5.size()) {
                        TimerEntry timer3 = timers5.get(i32);
                        sb6.setLength(0);
                        sb6.append("  Wake lock ");
                        UserHandle.formatUid(sb6, timer3.mId);
                        sb6.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                        sb6.append(timer3.mName);
                        TimerEntry timerEntry4 = timer3;
                        printWakeLock(sb6, timer3.mTimer, rawRealtime, null, i5, ": ");
                        sb6.append(" realtime");
                        printWriter7.println(sb6.toString());
                        i31 = i32 + 1;
                        timers5 = timers5;
                        kernelWakelocks = kernelWakelocks;
                    } else {
                        timers = timers5;
                        timers.clear();
                        pw.println();
                    }
                }
            } else {
                timers = timers5;
            }
            Map<String, ? extends Timer> wakeupReasons = getWakeupReasonStats();
            if (wakeupReasons != null && wakeupReasons.size() > 0) {
                pw.print(prefix);
                printWriter7.println("  All wakeup reasons:");
                ArrayList<TimerEntry> reasons = new ArrayList<>();
                for (Iterator<Map.Entry<String, ? extends Timer>> it = wakeupReasons.entrySet().iterator(); it.hasNext(); it = it) {
                    Map.Entry<String, ? extends Timer> ent2 = it.next();
                    Timer timer4 = (Timer) ent2.getValue();
                    Map.Entry<String, ? extends Timer> entry = ent2;
                    TimerEntry timerEntry5 = new TimerEntry(ent2.getKey(), 0, timer4, (long) timer4.getCountLocked(i5));
                    reasons.add(timerEntry5);
                }
                Collections.sort(reasons, r11);
                int i33 = 0;
                while (true) {
                    int i34 = i33;
                    if (i34 < reasons.size()) {
                        TimerEntry timer5 = reasons.get(i34);
                        sb6.setLength(0);
                        sb6.append(str4);
                        sb6.append("  Wakeup reason ");
                        sb6.append(timer5.mName);
                        TimerEntry timerEntry6 = timer5;
                        printWakeLock(sb6, timer5.mTimer, rawRealtime, null, i5, ": ");
                        sb6.append(" realtime");
                        printWriter7.println(sb6.toString());
                        i33 = i34 + 1;
                        reasons = reasons;
                        wakeupReasons = wakeupReasons;
                    } else {
                        Map<String, ? extends Timer> map = wakeupReasons;
                        pw.println();
                    }
                }
            }
        } else {
            timers = timers4;
            rawRealtime = rawRealtime24;
            i = -1;
        }
        LongSparseArray<? extends Timer> mMemoryStats3 = getKernelMemoryStats();
        if (mMemoryStats3.size() > 0) {
            printWriter7.println("  Memory Stats");
            for (int i35 = 0; i35 < mMemoryStats3.size(); i35++) {
                sb6.setLength(0);
                sb6.append("  Bandwidth ");
                sb6.append(mMemoryStats3.keyAt(i35));
                sb6.append(" Time ");
                sb6.append(((Timer) mMemoryStats3.valueAt(i35)).getTotalTimeLocked(rawRealtime, i5));
                printWriter7.println(sb6.toString());
            }
            rawRealtime2 = rawRealtime;
            pw.println();
        } else {
            rawRealtime2 = rawRealtime;
        }
        Map<String, ? extends Timer> rpmStats2 = getRpmStats();
        if (rpmStats2.size() > 0) {
            pw.print(prefix);
            printWriter7.println("  Resource Power Manager Stats");
            if (rpmStats2.size() > 0) {
                for (Map.Entry<String, ? extends Timer> ent3 : rpmStats2.entrySet()) {
                    StringBuilder sb7 = sb6;
                    long interactiveTime3 = dischargeScreenOffCount2;
                    long j14 = mobileTxTotalBytes3;
                    int i36 = numCellularRxBins2;
                    long j15 = wifiRxTotalBytes3;
                    long j16 = wifiTxTotalBytes4;
                    long j17 = wifiTxTotalPackets4;
                    long j18 = bluetoothScanTimeMs2;
                    long dischargeScreenOffCount3 = whichBatteryScreenOffRealtime2;
                    long dischargeScreenOffCount4 = whichBatteryScreenOffRealtime;
                    String[] strArr = cellularRxSignalStrengthDescription2;
                    String[] strArr2 = wifiRxSignalStrengthDescription3;
                    long dischargeScreenDozeCount2 = screenOnTime2;
                    long dischargeScreenDozeCount3 = mobileRxTotalBytes;
                    long screenOnTime4 = mobileRxTotalBytes3;
                    long j19 = wifiRxTotalPackets3;
                    String[] strArr3 = gpsSignalQualityDescription;
                    List<BatterySipper> list = sippers;
                    printTimer(printWriter7, sb7, (Timer) ent3.getValue(), rawRealtime2, which, prefix, ent3.getKey());
                    printWriter7 = pw;
                    timers = timers;
                    r11 = r11;
                    sb6 = sb7;
                    mMemoryStats3 = mMemoryStats3;
                    whichBatteryRealtime12 = whichBatteryRealtime12;
                    int i37 = reqUid;
                    mobileRxTotalBytes = dischargeScreenDozeCount3;
                    screenOnTime2 = dischargeScreenDozeCount2;
                    whichBatteryScreenOffRealtime = dischargeScreenOffCount4;
                    whichBatteryScreenOffRealtime2 = dischargeScreenOffCount3;
                    dischargeScreenOffCount2 = interactiveTime3;
                }
            }
            mMemoryStats = mMemoryStats3;
            sb = sb6;
            whichBatteryRealtime = whichBatteryRealtime12;
            AnonymousClass1 r121 = r11;
            ArrayList<TimerEntry> arrayList2 = timers;
            long j20 = dischargeScreenOffCount2;
            long j21 = mobileTxTotalBytes3;
            String[] strArr4 = cellularRxSignalStrengthDescription2;
            int i38 = numCellularRxBins2;
            long j22 = wifiRxTotalBytes3;
            long j23 = wifiTxTotalBytes4;
            long j24 = wifiTxTotalPackets4;
            String[] strArr5 = wifiRxSignalStrengthDescription3;
            long j25 = wifiRxTotalPackets3;
            long j26 = bluetoothScanTimeMs2;
            String[] strArr6 = gpsSignalQualityDescription;
            List<BatterySipper> list2 = sippers;
            i2 = 0;
            long interactiveTime4 = whichBatteryScreenOffRealtime2;
            long dischargeScreenOffCount5 = whichBatteryScreenOffRealtime;
            long whichBatteryScreenOffRealtime3 = screenOnTime2;
            long dischargeScreenDozeCount4 = mobileRxTotalBytes;
            long screenOnTime5 = mobileRxTotalBytes3;
            pw.println();
        } else {
            mMemoryStats = mMemoryStats3;
            sb = sb6;
            whichBatteryRealtime = whichBatteryRealtime12;
            AnonymousClass1 r1212 = r11;
            ArrayList<TimerEntry> arrayList3 = timers;
            long j27 = dischargeScreenOffCount2;
            long j28 = mobileTxTotalBytes3;
            String[] strArr7 = cellularRxSignalStrengthDescription2;
            int i39 = numCellularRxBins2;
            long j29 = wifiRxTotalBytes3;
            long j30 = wifiTxTotalBytes4;
            long j31 = wifiTxTotalPackets4;
            String[] strArr8 = wifiRxSignalStrengthDescription3;
            long j32 = wifiRxTotalPackets3;
            long j33 = bluetoothScanTimeMs2;
            String[] strArr9 = gpsSignalQualityDescription;
            List<BatterySipper> list3 = sippers;
            i2 = 0;
            long interactiveTime5 = whichBatteryScreenOffRealtime2;
            long dischargeScreenOffCount6 = whichBatteryScreenOffRealtime;
            long whichBatteryScreenOffRealtime4 = screenOnTime2;
            long dischargeScreenDozeCount5 = mobileRxTotalBytes;
            long screenOnTime6 = mobileRxTotalBytes3;
        }
        long[] cpuFreqs2 = getCpuFreqs();
        if (cpuFreqs2 != null) {
            sb2 = sb;
            sb2.setLength(i2);
            sb2.append("  CPU freqs:");
            for (int i40 = i2; i40 < cpuFreqs2.length; i40++) {
                sb2.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + cpuFreqs2[i40]);
            }
            printWriter = pw;
            printWriter.println(sb2.toString());
            pw.println();
        } else {
            printWriter = pw;
            sb2 = sb;
        }
        int iu3 = i2;
        StringBuilder sb8 = sb2;
        while (true) {
            int iu4 = iu3;
            int NU9 = NU8;
            if (iu4 < NU9) {
                SparseArray<? extends Uid> uidStats8 = uidStats7;
                int uid2 = uidStats8.keyAt(iu4);
                int i41 = reqUid;
                if (i41 < 0 || uid2 == i41 || uid2 == 1000) {
                    Uid u7 = (Uid) uidStats8.valueAt(iu4);
                    pw.print(prefix);
                    printWriter.print("  ");
                    UserHandle.formatUid(printWriter, uid2);
                    printWriter.println(SettingsStringUtil.DELIMITER);
                    boolean uidActivity3 = false;
                    int i42 = which;
                    int iu5 = iu4;
                    int NU10 = NU9;
                    long mobileRxBytes2 = u7.getNetworkActivityBytes(i2, i42);
                    long mobileTxBytes4 = u7.getNetworkActivityBytes(1, i42);
                    int uid3 = uid2;
                    SparseArray<? extends Uid> uidStats9 = uidStats8;
                    long wifiRxBytes4 = u7.getNetworkActivityBytes(2, i42);
                    long wifiTxBytes2 = u7.getNetworkActivityBytes(3, i42);
                    long btRxBytes4 = u7.getNetworkActivityBytes(4, i42);
                    long btTxBytes4 = u7.getNetworkActivityBytes(5, i42);
                    long mobileRxPackets2 = u7.getNetworkActivityPackets(0, i42);
                    StringBuilder sb9 = sb8;
                    long[] cpuFreqs3 = cpuFreqs2;
                    long mobileTxPackets2 = u7.getNetworkActivityPackets(1, i42);
                    long wifiRxPackets5 = u7.getNetworkActivityPackets(2, i42);
                    long wifiTxPackets3 = u7.getNetworkActivityPackets(3, i42);
                    long uidMobileActiveTime = u7.getMobileRadioActiveTime(i42);
                    int uidMobileActiveCount = u7.getMobileRadioActiveCount(i42);
                    long uidMobileActiveTime2 = uidMobileActiveTime;
                    long fullWifiLockOnTime2 = u7.getFullWifiLockTime(rawRealtime2, i42);
                    long wifiScanTime = u7.getWifiScanTime(rawRealtime2, i42);
                    int wifiScanCount3 = u7.getWifiScanCount(i42);
                    int wifiScanCount4 = u7.getWifiScanBackgroundCount(i42);
                    long wifiScanTime2 = wifiScanTime;
                    long wifiScanActualTime3 = u7.getWifiScanActualTime(rawRealtime2);
                    long wifiScanActualTimeBg = u7.getWifiScanBackgroundTime(rawRealtime2);
                    long uidWifiRunningTime4 = u7.getWifiRunningTime(rawRealtime2, i42);
                    long uidWifiRunningTime5 = u7.getMobileRadioApWakeupCount(i42);
                    long rawRealtime28 = rawRealtime2;
                    Map<String, ? extends Timer> rpmStats3 = rpmStats2;
                    long wifiWakeup5 = u7.getWifiRadioApWakeupCount(i42);
                    if (mobileRxBytes2 > 0 || mobileTxBytes4 > 0 || mobileRxPackets2 > 0) {
                        mobileTxBytes3 = mobileTxBytes4;
                        mobileTxBytes = mobileTxPackets2;
                    } else {
                        mobileTxBytes3 = mobileTxBytes4;
                        mobileTxBytes = mobileTxPackets2;
                        if (mobileTxBytes <= 0) {
                            wifiWakeup = wifiWakeup5;
                            wifiRxPackets = wifiRxPackets5;
                            wifiWakeup2 = mobileTxBytes3;
                            batteryStats = this;
                            printWriter3 = pw;
                            long mobileRxBytes3 = mobileRxBytes2;
                            mobileRxBytes = uidMobileActiveTime2;
                            if (mobileRxBytes <= 0 || uidMobileActiveCount > 0) {
                                mobileTxBytes2 = wifiWakeup2;
                                sb4 = sb9;
                                sb4.setLength(0);
                                sb4.append(prefix);
                                sb4.append("    Mobile radio active: ");
                                formatTimeMs(sb4, mobileRxBytes / 1000);
                                sb4.append("(");
                                wifiScanCountBg = wifiScanCount4;
                                long packets2 = mobileActiveTime5;
                                sb4.append(batteryStats.formatRatioLocked(mobileRxBytes, packets2));
                                sb4.append(") ");
                                sb4.append(uidMobileActiveCount);
                                sb4.append("x");
                                packets = mobileRxPackets2 + mobileTxBytes;
                                if (packets == 0) {
                                    packets = 1;
                                }
                                mobileActiveTime2 = packets2;
                                sb4.append(" @ ");
                                mobileTxPackets = mobileTxBytes;
                                mobileRxPackets = mobileRxPackets2;
                                sb4.append(BatteryStatsHelper.makemAh(((double) (mobileRxBytes / 1000)) / ((double) packets)));
                                sb4.append(" mspp");
                                printWriter3.println(sb4.toString());
                            } else {
                                mobileTxBytes2 = wifiWakeup2;
                                mobileTxPackets = mobileTxBytes;
                                mobileRxPackets = mobileRxPackets2;
                                wifiScanCountBg = wifiScanCount4;
                                mobileActiveTime2 = mobileActiveTime5;
                                sb4 = sb9;
                            }
                            if (uidWifiRunningTime5 <= 0) {
                                z = false;
                                sb4.setLength(0);
                                str = prefix;
                                sb4.append(str);
                                sb4.append("    Mobile radio AP wakeups: ");
                                sb4.append(uidWifiRunningTime5);
                                printWriter3.println(sb4.toString());
                            } else {
                                str = prefix;
                                z = false;
                            }
                            BatteryStats batteryStats3 = batteryStats;
                            StringBuilder sb10 = sb4;
                            Uid u8 = u7;
                            long mobileWakeup = uidWifiRunningTime5;
                            int uidMobileActiveCount2 = uidMobileActiveCount;
                            long rawRealtime29 = rawRealtime28;
                            long uidMobileActiveTime3 = mobileRxBytes;
                            long wifiWakeup6 = wifiWakeup;
                            long j34 = mobileTxBytes2;
                            mobileActiveTime = mobileActiveTime2;
                            String str5 = str;
                            rpmStats = rpmStats3;
                            boolean z5 = z;
                            mMemoryStats2 = mMemoryStats;
                            long j35 = mobileTxPackets;
                            batteryStats.printControllerActivityIfInteresting(printWriter3, sb10, str + "  ", CELLULAR_CONTROLLER_NAME, u7.getModemControllerActivity(), which);
                            if (wifiRxBytes4 <= 0 || wifiTxBytes2 > 0) {
                                wifiTxPackets = wifiTxPackets3;
                                wifiRxPackets2 = wifiRxPackets;
                            } else {
                                wifiRxPackets2 = wifiRxPackets;
                                if (wifiRxPackets2 <= 0) {
                                    wifiTxPackets = wifiTxPackets3;
                                    if (wifiTxPackets <= 0) {
                                        printWriter4 = pw;
                                        rawRealtime4 = rawRealtime29;
                                        wifiRxBytes = wifiRxBytes4;
                                        wifiTxBytes = wifiTxBytes2;
                                        long wifiTxBytes3 = wifiTxBytes;
                                        fullWifiLockOnTime = fullWifiLockOnTime2;
                                        if (fullWifiLockOnTime == 0) {
                                            wifiRxPackets3 = wifiRxPackets2;
                                            wifiRxPackets4 = wifiScanTime2;
                                            if (wifiRxPackets4 == 0) {
                                                wifiScanCount = wifiScanCount3;
                                                if (wifiScanCount == 0) {
                                                    wifiScanCountBg2 = wifiScanCountBg;
                                                    if (wifiScanCountBg2 == 0) {
                                                        wifiTxPackets2 = wifiTxPackets;
                                                        wifiScanActualTime2 = wifiScanActualTime3;
                                                        if (wifiScanActualTime2 == 0) {
                                                            wifiRxBytes2 = wifiRxBytes;
                                                            wifiRxBytes3 = wifiScanActualTimeBg;
                                                            if (wifiRxBytes3 == 0) {
                                                                wifiWakeup3 = wifiWakeup6;
                                                                uidWifiRunningTime3 = uidWifiRunningTime4;
                                                                if (uidWifiRunningTime3 == 0) {
                                                                    wifiScanActualTime = wifiScanActualTime2;
                                                                    u = u8;
                                                                    uidWifiRunningTime = uidWifiRunningTime3;
                                                                    whichBatteryRealtime3 = whichBatteryRealtime;
                                                                    sb5 = sb10;
                                                                    uidWifiRunningTime2 = rawRealtime4;
                                                                    printWriter5 = printWriter4;
                                                                    long fullWifiLockOnTime3 = fullWifiLockOnTime;
                                                                    wifiWakeup4 = wifiWakeup3;
                                                                    if (wifiWakeup4 > 0) {
                                                                        sb5.setLength(0);
                                                                        sb5.append(str5);
                                                                        sb5.append("    WiFi AP wakeups: ");
                                                                        sb5.append(wifiWakeup4);
                                                                        printWriter5.println(sb5.toString());
                                                                    }
                                                                    Uid u9 = u;
                                                                    long wifiScanActualTimeBg2 = wifiRxBytes3;
                                                                    long j36 = wifiRxBytes2;
                                                                    whichBatteryRealtime2 = whichBatteryRealtime3;
                                                                    long wifiWakeup7 = wifiWakeup4;
                                                                    u2 = u9;
                                                                    PrintWriter printWriter8 = printWriter5;
                                                                    long j37 = wifiTxPackets2;
                                                                    long wifiScanActualTime4 = wifiScanActualTime;
                                                                    long j38 = wifiRxPackets4;
                                                                    long j39 = wifiRxPackets3;
                                                                    batteryStats3.printControllerActivityIfInteresting(printWriter5, sb5, str5 + "  ", WIFI_CONTROLLER_NAME, u9.getWifiControllerActivity(), which);
                                                                    if (btRxBytes4 > 0) {
                                                                        btTxBytes = btTxBytes4;
                                                                        if (btTxBytes <= 0) {
                                                                            btRxBytes = btRxBytes4;
                                                                            bleTimer = u2.getBluetoothScanTimer();
                                                                            if (bleTimer != null) {
                                                                                int i43 = which;
                                                                                btRxBytes2 = btRxBytes;
                                                                                long btRxBytes5 = (bleTimer.getTotalTimeLocked(uidWifiRunningTime2, i43) + 500) / 1000;
                                                                                if (btRxBytes5 != 0) {
                                                                                    int count = bleTimer.getCountLocked(i43);
                                                                                    btTxBytes2 = btTxBytes;
                                                                                    Timer bleTimerBg = u2.getBluetoothScanBackgroundTimer();
                                                                                    int countBg = bleTimerBg != null ? bleTimerBg.getCountLocked(i43) : 0;
                                                                                    rawRealtime5 = uidWifiRunningTime2;
                                                                                    wifiScanCount2 = wifiScanCount;
                                                                                    long rawRealtimeMs9 = rawRealtimeMs8;
                                                                                    long actualTimeMs = bleTimer.getTotalDurationMsLocked(rawRealtimeMs9);
                                                                                    long actualTimeMsBg = bleTimerBg != null ? bleTimerBg.getTotalDurationMsLocked(rawRealtimeMs9) : 0;
                                                                                    int resultCount = u2.getBluetoothScanResultCounter() != null ? u2.getBluetoothScanResultCounter().getCountLocked(i43) : 0;
                                                                                    if (u2.getBluetoothScanResultBgCounter() != null) {
                                                                                        wifiScanCountBg3 = wifiScanCountBg2;
                                                                                        wifiScanCountBg4 = u2.getBluetoothScanResultBgCounter().getCountLocked(i43);
                                                                                    } else {
                                                                                        wifiScanCountBg3 = wifiScanCountBg2;
                                                                                        wifiScanCountBg4 = 0;
                                                                                    }
                                                                                    Timer unoptimizedScanTimer = u2.getBluetoothUnoptimizedScanTimer();
                                                                                    long unoptimizedScanTotalTime = unoptimizedScanTimer != null ? unoptimizedScanTimer.getTotalDurationMsLocked(rawRealtimeMs9) : 0;
                                                                                    long unoptimizedScanMaxTime = unoptimizedScanTimer != null ? unoptimizedScanTimer.getMaxDurationMsLocked(rawRealtimeMs9) : 0;
                                                                                    Timer unoptimizedScanTimer2 = unoptimizedScanTimer;
                                                                                    Timer unoptimizedScanTimerBg = u2.getBluetoothUnoptimizedScanBackgroundTimer();
                                                                                    long unoptimizedScanTotalTimeBg2 = unoptimizedScanTimerBg != null ? unoptimizedScanTimerBg.getTotalDurationMsLocked(rawRealtimeMs9) : 0;
                                                                                    long unoptimizedScanMaxTimeBg = unoptimizedScanTimerBg != null ? unoptimizedScanTimerBg.getMaxDurationMsLocked(rawRealtimeMs9) : 0;
                                                                                    rawRealtimeMs2 = rawRealtimeMs9;
                                                                                    sb5.setLength(0);
                                                                                    if (actualTimeMs != btRxBytes5) {
                                                                                        sb5.append(str5);
                                                                                        sb5.append("    Bluetooth Scan (total blamed realtime): ");
                                                                                        formatTimeMs(sb5, btRxBytes5);
                                                                                        sb5.append(" (");
                                                                                        sb5.append(count);
                                                                                        sb5.append(" times)");
                                                                                        if (bleTimer.isRunningLocked()) {
                                                                                            sb5.append(" (currently running)");
                                                                                        }
                                                                                        sb5.append("\n");
                                                                                    }
                                                                                    sb5.append(str5);
                                                                                    sb5.append("    Bluetooth Scan (total actual realtime): ");
                                                                                    formatTimeMs(sb5, actualTimeMs);
                                                                                    sb5.append(" (");
                                                                                    sb5.append(count);
                                                                                    sb5.append(" times)");
                                                                                    if (bleTimer.isRunningLocked()) {
                                                                                        sb5.append(" (currently running)");
                                                                                    }
                                                                                    sb5.append("\n");
                                                                                    long actualTimeMsBg2 = actualTimeMsBg;
                                                                                    if (actualTimeMsBg2 > 0 || countBg > 0) {
                                                                                        sb5.append(str5);
                                                                                        int i44 = count;
                                                                                        sb5.append("    Bluetooth Scan (background realtime): ");
                                                                                        formatTimeMs(sb5, actualTimeMsBg2);
                                                                                        sb5.append(" (");
                                                                                        sb5.append(countBg);
                                                                                        sb5.append(" times)");
                                                                                        if (bleTimerBg != null && bleTimerBg.isRunningLocked()) {
                                                                                            sb5.append(" (currently running in background)");
                                                                                        }
                                                                                        sb5.append("\n");
                                                                                    } else {
                                                                                        int i45 = count;
                                                                                    }
                                                                                    sb5.append(str5);
                                                                                    sb5.append("    Bluetooth Scan Results: ");
                                                                                    sb5.append(resultCount);
                                                                                    sb5.append(" (");
                                                                                    sb5.append(wifiScanCountBg4);
                                                                                    sb5.append(" in background)");
                                                                                    long j40 = btRxBytes5;
                                                                                    long unoptimizedScanTotalTime2 = unoptimizedScanTotalTime;
                                                                                    if (unoptimizedScanTotalTime2 <= 0) {
                                                                                        bleTimer2 = bleTimer;
                                                                                        Timer timer6 = bleTimerBg;
                                                                                        unoptimizedScanTotalTimeBg = unoptimizedScanTotalTimeBg2;
                                                                                        if (unoptimizedScanTotalTimeBg <= 0) {
                                                                                            long j41 = unoptimizedScanTotalTime2;
                                                                                            long j42 = unoptimizedScanMaxTime;
                                                                                            Timer timer7 = unoptimizedScanTimer2;
                                                                                            long unoptimizedScanTotalTime3 = unoptimizedScanMaxTimeBg;
                                                                                            long j43 = actualTimeMs;
                                                                                            printWriter8 = pw;
                                                                                            printWriter8.println(sb5.toString());
                                                                                            uidActivity3 = true;
                                                                                        }
                                                                                    } else {
                                                                                        bleTimer2 = bleTimer;
                                                                                        Timer timer8 = bleTimerBg;
                                                                                        unoptimizedScanTotalTimeBg = unoptimizedScanTotalTimeBg2;
                                                                                    }
                                                                                    sb5.append("\n");
                                                                                    sb5.append(str5);
                                                                                    sb5.append("    Unoptimized Bluetooth Scan (realtime): ");
                                                                                    formatTimeMs(sb5, unoptimizedScanTotalTime2);
                                                                                    sb5.append(" (max ");
                                                                                    long j44 = unoptimizedScanTotalTime2;
                                                                                    long unoptimizedScanTotalTime4 = unoptimizedScanMaxTime;
                                                                                    formatTimeMs(sb5, unoptimizedScanTotalTime4);
                                                                                    sb5.append(")");
                                                                                    Timer unoptimizedScanTimer3 = unoptimizedScanTimer2;
                                                                                    if (unoptimizedScanTimer3 == null || !unoptimizedScanTimer3.isRunningLocked()) {
                                                                                    } else {
                                                                                        Timer timer9 = unoptimizedScanTimer3;
                                                                                        sb5.append(" (currently running unoptimized)");
                                                                                    }
                                                                                    if (unoptimizedScanTimerBg == null || unoptimizedScanTotalTimeBg <= 0) {
                                                                                        long unoptimizedScanMaxTime2 = unoptimizedScanTotalTime4;
                                                                                        long unoptimizedScanMaxTime3 = unoptimizedScanMaxTimeBg;
                                                                                        long j432 = actualTimeMs;
                                                                                        printWriter8 = pw;
                                                                                        printWriter8.println(sb5.toString());
                                                                                        uidActivity3 = true;
                                                                                    } else {
                                                                                        sb5.append("\n");
                                                                                        sb5.append(str5);
                                                                                        sb5.append("    Unoptimized Bluetooth Scan (background realtime): ");
                                                                                        formatTimeMs(sb5, unoptimizedScanTotalTimeBg);
                                                                                        sb5.append(" (max ");
                                                                                        long j45 = unoptimizedScanTotalTime4;
                                                                                        formatTimeMs(sb5, unoptimizedScanMaxTimeBg);
                                                                                        sb5.append(")");
                                                                                        if (unoptimizedScanTimerBg.isRunningLocked()) {
                                                                                            sb5.append(" (currently running unoptimized in background)");
                                                                                        }
                                                                                        long j4322 = actualTimeMs;
                                                                                        printWriter8 = pw;
                                                                                        printWriter8.println(sb5.toString());
                                                                                        uidActivity3 = true;
                                                                                    }
                                                                                } else {
                                                                                    bleTimer2 = bleTimer;
                                                                                    btTxBytes2 = btTxBytes;
                                                                                    wifiScanCountBg3 = wifiScanCountBg2;
                                                                                    rawRealtime5 = uidWifiRunningTime2;
                                                                                    wifiScanCount2 = wifiScanCount;
                                                                                    rawRealtimeMs2 = rawRealtimeMs8;
                                                                                }
                                                                            } else {
                                                                                btRxBytes2 = btRxBytes;
                                                                                bleTimer2 = bleTimer;
                                                                                btTxBytes2 = btTxBytes;
                                                                                wifiScanCountBg3 = wifiScanCountBg2;
                                                                                rawRealtime5 = uidWifiRunningTime2;
                                                                                wifiScanCount2 = wifiScanCount;
                                                                                rawRealtimeMs2 = rawRealtimeMs8;
                                                                            }
                                                                            if (u2.hasUserActivity()) {
                                                                                boolean hasData = false;
                                                                                for (int i46 = 0; i46 < 4; i46++) {
                                                                                    int val = u2.getUserActivityCount(i46, which);
                                                                                    if (val != 0) {
                                                                                        if (!hasData) {
                                                                                            sb5.setLength(0);
                                                                                            sb5.append("    User activity: ");
                                                                                            hasData = true;
                                                                                        } else {
                                                                                            sb5.append(", ");
                                                                                        }
                                                                                        sb5.append(val);
                                                                                        sb5.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                                                                                        sb5.append(Uid.USER_ACTIVITY_TYPES[i46]);
                                                                                    }
                                                                                }
                                                                                i3 = which;
                                                                                if (hasData) {
                                                                                    printWriter8.println(sb5.toString());
                                                                                }
                                                                            } else {
                                                                                i3 = which;
                                                                            }
                                                                            wakelocks = u2.getWakelockStats();
                                                                            int countWakelock2 = 0;
                                                                            if (wakelocks != null) {
                                                                                int iw2 = wakelocks.size() - 1;
                                                                                long totalPartialWakelock2 = 0;
                                                                                long totalWindowWakelock2 = 0;
                                                                                long totalDrawWakelock3 = 0;
                                                                                long totalFullWakelock3 = 0;
                                                                                while (true) {
                                                                                    int iw3 = iw2;
                                                                                    if (iw3 >= 0) {
                                                                                        Uid.Wakelock wl2 = (Uid.Wakelock) wakelocks.valueAt(iw3);
                                                                                        sb5.setLength(0);
                                                                                        sb5.append(str5);
                                                                                        sb5.append("    Wake lock ");
                                                                                        sb5.append(wakelocks.keyAt(iw3));
                                                                                        Timer wakeTime = wl2.getWakeTime(1);
                                                                                        long btRxBytes6 = btRxBytes2;
                                                                                        long j46 = rawRealtime5;
                                                                                        Timer bleTimer3 = bleTimer2;
                                                                                        Uid.Wakelock wl3 = wl2;
                                                                                        long btTxBytes5 = btTxBytes2;
                                                                                        int i47 = which;
                                                                                        int iw4 = iw3;
                                                                                        String linePrefix = printWakeLock(sb5, wakeTime, j46, "full", i47, ": ");
                                                                                        Timer pTimer = wl3.getWakeTime(0);
                                                                                        ArrayMap<String, ? extends Uid.Wakelock> wakelocks4 = wakelocks;
                                                                                        Timer pTimer2 = pTimer;
                                                                                        String linePrefix2 = printWakeLock(sb5, pTimer, j46, "partial", i47, linePrefix);
                                                                                        long j47 = rawRealtime5;
                                                                                        int i48 = which;
                                                                                        StringBuilder sb11 = sb5;
                                                                                        Timer timer10 = pTimer2;
                                                                                        String printWakeLock = printWakeLock(sb11, wl3.getWakeTime(18), j47, "draw", i48, printWakeLock(sb11, wl3.getWakeTime(2), j47, AppAssociate.ASSOC_WINDOW, i48, printWakeLock(sb5, pTimer2 != null ? pTimer2.getSubTimer() : null, j47, "background partial", i48, linePrefix2)));
                                                                                        sb5.append(" realtime");
                                                                                        printWriter8.println(sb5.toString());
                                                                                        uidActivity3 = true;
                                                                                        countWakelock2++;
                                                                                        int i49 = which;
                                                                                        long rawRealtime30 = rawRealtime5;
                                                                                        totalFullWakelock3 += computeWakeLock(wl3.getWakeTime(1), rawRealtime30, i49);
                                                                                        totalPartialWakelock2 += computeWakeLock(wl3.getWakeTime(0), rawRealtime30, i49);
                                                                                        totalWindowWakelock2 += computeWakeLock(wl3.getWakeTime(2), rawRealtime30, i49);
                                                                                        totalDrawWakelock3 += computeWakeLock(wl3.getWakeTime(18), rawRealtime30, i49);
                                                                                        iw2 = iw4 - 1;
                                                                                        i3 = i49;
                                                                                        bleTimer2 = bleTimer3;
                                                                                        btRxBytes2 = btRxBytes6;
                                                                                        btTxBytes2 = btTxBytes5;
                                                                                        wakelocks = wakelocks4;
                                                                                    } else {
                                                                                        int i50 = i3;
                                                                                        wakelocks2 = wakelocks;
                                                                                        btRxBytes3 = btRxBytes2;
                                                                                        btTxBytes3 = btTxBytes2;
                                                                                        Timer timer11 = bleTimer2;
                                                                                        countWakelock = countWakelock2;
                                                                                        totalFullWakelock2 = totalFullWakelock3;
                                                                                        totalFullWakelock = totalPartialWakelock2;
                                                                                        totalWindowWakelock = totalWindowWakelock2;
                                                                                        totalDrawWakelock = totalDrawWakelock3;
                                                                                        rawRealtime6 = rawRealtime5;
                                                                                    }
                                                                                }
                                                                            } else {
                                                                                wakelocks2 = wakelocks;
                                                                                btRxBytes3 = btRxBytes2;
                                                                                btTxBytes3 = btTxBytes2;
                                                                                Timer timer12 = bleTimer2;
                                                                                int i51 = i3;
                                                                                totalDrawWakelock = 0;
                                                                                rawRealtime6 = rawRealtime5;
                                                                                totalFullWakelock = 0;
                                                                                countWakelock = 0;
                                                                                totalWindowWakelock = 0;
                                                                                totalFullWakelock2 = 0;
                                                                            }
                                                                            if (countWakelock > 1) {
                                                                                if (u2.getAggregatedPartialWakelockTimer() != null) {
                                                                                    Timer aggTimer = u2.getAggregatedPartialWakelockTimer();
                                                                                    u3 = u2;
                                                                                    totalDrawWakelock2 = totalDrawWakelock;
                                                                                    long rawRealtimeMs10 = rawRealtimeMs2;
                                                                                    long actualTotalPartialWakelock = aggTimer.getTotalDurationMsLocked(rawRealtimeMs10);
                                                                                    Timer bgAggTimer = aggTimer.getSubTimer();
                                                                                    long actualBgPartialWakelock2 = bgAggTimer != null ? bgAggTimer.getTotalDurationMsLocked(rawRealtimeMs10) : 0;
                                                                                    rawRealtimeMs3 = rawRealtimeMs10;
                                                                                    rawRealtimeMs6 = actualTotalPartialWakelock;
                                                                                    actualBgPartialWakelock = actualBgPartialWakelock2;
                                                                                } else {
                                                                                    u3 = u2;
                                                                                    totalDrawWakelock2 = totalDrawWakelock;
                                                                                    rawRealtimeMs6 = 0;
                                                                                    actualBgPartialWakelock = 0;
                                                                                    rawRealtimeMs3 = rawRealtimeMs2;
                                                                                }
                                                                                if (rawRealtimeMs6 == 0 && actualBgPartialWakelock == 0 && totalFullWakelock2 == 0 && totalFullWakelock == 0 && totalWindowWakelock == 0) {
                                                                                    int i52 = countWakelock;
                                                                                    long j48 = totalWindowWakelock;
                                                                                    long j49 = totalDrawWakelock2;
                                                                                    printWriter6 = pw;
                                                                                    str2 = prefix;
                                                                                } else {
                                                                                    sb5.setLength(0);
                                                                                    str2 = prefix;
                                                                                    sb5.append(str2);
                                                                                    sb5.append("    TOTAL wake: ");
                                                                                    boolean needComma = false;
                                                                                    if (totalFullWakelock2 != 0) {
                                                                                        needComma = true;
                                                                                        formatTimeMs(sb5, totalFullWakelock2);
                                                                                        sb5.append("full");
                                                                                    }
                                                                                    if (totalFullWakelock != 0) {
                                                                                        if (needComma) {
                                                                                            sb5.append(", ");
                                                                                        }
                                                                                        needComma = true;
                                                                                        formatTimeMs(sb5, totalFullWakelock);
                                                                                        sb5.append("blamed partial");
                                                                                    }
                                                                                    if (rawRealtimeMs6 != 0) {
                                                                                        if (needComma) {
                                                                                            sb5.append(", ");
                                                                                        }
                                                                                        needComma = true;
                                                                                        formatTimeMs(sb5, rawRealtimeMs6);
                                                                                        sb5.append("actual partial");
                                                                                    }
                                                                                    if (actualBgPartialWakelock != 0) {
                                                                                        if (needComma) {
                                                                                            sb5.append(", ");
                                                                                        }
                                                                                        needComma = true;
                                                                                        formatTimeMs(sb5, actualBgPartialWakelock);
                                                                                        sb5.append("actual background partial");
                                                                                    }
                                                                                    if (totalWindowWakelock != 0) {
                                                                                        if (needComma) {
                                                                                            sb5.append(", ");
                                                                                        }
                                                                                        needComma = true;
                                                                                        formatTimeMs(sb5, totalWindowWakelock);
                                                                                        sb5.append(AppAssociate.ASSOC_WINDOW);
                                                                                    }
                                                                                    int i53 = countWakelock;
                                                                                    long j50 = totalWindowWakelock;
                                                                                    long totalDrawWakelock4 = totalDrawWakelock2;
                                                                                    if (totalDrawWakelock4 != 0) {
                                                                                        if (needComma) {
                                                                                            sb5.append(",");
                                                                                        }
                                                                                        formatTimeMs(sb5, totalDrawWakelock4);
                                                                                        sb5.append("draw");
                                                                                    }
                                                                                    sb5.append(" realtime");
                                                                                    long j51 = totalDrawWakelock4;
                                                                                    printWriter6 = pw;
                                                                                    printWriter6.println(sb5.toString());
                                                                                }
                                                                            } else {
                                                                                long j52 = totalWindowWakelock;
                                                                                str2 = str5;
                                                                                u3 = u2;
                                                                                long j53 = totalDrawWakelock;
                                                                                rawRealtimeMs3 = rawRealtimeMs2;
                                                                                printWriter6 = pw;
                                                                            }
                                                                            Uid u10 = u3;
                                                                            mcTimer = u10.getMulticastWakelockStats();
                                                                            if (mcTimer != null) {
                                                                                rawRealtime7 = rawRealtime6;
                                                                                i4 = which;
                                                                                long multicastWakeLockTimeMicros = mcTimer.getTotalTimeLocked(rawRealtime7, i4);
                                                                                int multicastWakeLockCount = mcTimer.getCountLocked(i4);
                                                                                if (multicastWakeLockTimeMicros > 0) {
                                                                                    sb5.setLength(0);
                                                                                    sb5.append(str2);
                                                                                    sb5.append("    WiFi Multicast Wakelock");
                                                                                    sb5.append(" count = ");
                                                                                    sb5.append(multicastWakeLockCount);
                                                                                    sb5.append(" time = ");
                                                                                    long j54 = totalFullWakelock2;
                                                                                    formatTimeMsNoSpace(sb5, (multicastWakeLockTimeMicros + 500) / 1000);
                                                                                    printWriter6.println(sb5.toString());
                                                                                }
                                                                            } else {
                                                                                rawRealtime7 = rawRealtime6;
                                                                                i4 = which;
                                                                            }
                                                                            syncs = u10.getSyncStats();
                                                                            if (syncs != null) {
                                                                                int isy = syncs.size() - 1;
                                                                                while (isy >= 0) {
                                                                                    Timer timer13 = (Timer) syncs.valueAt(isy);
                                                                                    long totalTime2 = (timer13.getTotalTimeLocked(rawRealtime7, i4) + 500) / 1000;
                                                                                    int count2 = timer13.getCountLocked(i4);
                                                                                    Timer mcTimer2 = mcTimer;
                                                                                    Timer bgTimer = timer13.getSubTimer();
                                                                                    if (bgTimer != null) {
                                                                                        Timer timer14 = timer13;
                                                                                        totalPartialWakelock = totalFullWakelock;
                                                                                        rawRealtimeMs5 = rawRealtimeMs3;
                                                                                        j = bgTimer.getTotalDurationMsLocked(rawRealtimeMs5);
                                                                                    } else {
                                                                                        totalPartialWakelock = totalFullWakelock;
                                                                                        rawRealtimeMs5 = rawRealtimeMs3;
                                                                                        j = -1;
                                                                                    }
                                                                                    long bgTime = j;
                                                                                    int bgCount2 = bgTimer != null ? bgTimer.getCountLocked(i4) : i;
                                                                                    Timer timer15 = bgTimer;
                                                                                    sb5.setLength(0);
                                                                                    sb5.append(str2);
                                                                                    sb5.append("    Sync ");
                                                                                    sb5.append(syncs.keyAt(isy));
                                                                                    sb5.append(": ");
                                                                                    if (totalTime2 != 0) {
                                                                                        formatTimeMs(sb5, totalTime2);
                                                                                        sb5.append("realtime (");
                                                                                        sb5.append(count2);
                                                                                        sb5.append(" times)");
                                                                                        long j55 = totalTime2;
                                                                                        long totalTime3 = bgTime;
                                                                                        if (totalTime3 > 0) {
                                                                                            sb5.append(", ");
                                                                                            formatTimeMs(sb5, totalTime3);
                                                                                            sb5.append("background (");
                                                                                            sb5.append(bgCount2);
                                                                                            sb5.append(" times)");
                                                                                        }
                                                                                    } else {
                                                                                        long totalTime4 = bgTime;
                                                                                        sb5.append("(not used)");
                                                                                    }
                                                                                    printWriter6.println(sb5.toString());
                                                                                    uidActivity3 = true;
                                                                                    isy--;
                                                                                    rawRealtimeMs3 = rawRealtimeMs5;
                                                                                    mcTimer = mcTimer2;
                                                                                    totalFullWakelock = totalPartialWakelock;
                                                                                }
                                                                            }
                                                                            long totalPartialWakelock3 = totalFullWakelock;
                                                                            long rawRealtimeMs11 = rawRealtimeMs3;
                                                                            jobs = u10.getJobStats();
                                                                            if (jobs != null) {
                                                                                int ij = jobs.size() - 1;
                                                                                while (ij >= 0) {
                                                                                    Timer timer16 = (Timer) jobs.valueAt(ij);
                                                                                    long rawRealtime31 = rawRealtime7;
                                                                                    long totalTime5 = (timer16.getTotalTimeLocked(rawRealtime7, i4) + 500) / 1000;
                                                                                    int count3 = timer16.getCountLocked(i4);
                                                                                    Timer bgTimer2 = timer16.getSubTimer();
                                                                                    long bgTime2 = bgTimer2 != null ? bgTimer2.getTotalDurationMsLocked(rawRealtimeMs11) : -1;
                                                                                    int bgCount3 = bgTimer2 != null ? bgTimer2.getCountLocked(i4) : i;
                                                                                    ArrayMap<String, ? extends Timer> syncs2 = syncs;
                                                                                    sb5.setLength(0);
                                                                                    sb5.append(str2);
                                                                                    sb5.append("    Job ");
                                                                                    sb5.append(jobs.keyAt(ij));
                                                                                    sb5.append(": ");
                                                                                    if (totalTime5 != 0) {
                                                                                        formatTimeMs(sb5, totalTime5);
                                                                                        sb5.append("realtime (");
                                                                                        sb5.append(count3);
                                                                                        sb5.append(" times)");
                                                                                        Timer timer17 = timer16;
                                                                                        int i54 = count3;
                                                                                        long bgTime3 = bgTime2;
                                                                                        if (bgTime3 > 0) {
                                                                                            sb5.append(", ");
                                                                                            formatTimeMs(sb5, bgTime3);
                                                                                            sb5.append("background (");
                                                                                            sb5.append(bgCount3);
                                                                                            sb5.append(" times)");
                                                                                        }
                                                                                    } else {
                                                                                        int i55 = count3;
                                                                                        long j56 = bgTime2;
                                                                                        sb5.append("(not used)");
                                                                                    }
                                                                                    printWriter6.println(sb5.toString());
                                                                                    uidActivity3 = true;
                                                                                    ij--;
                                                                                    rawRealtime7 = rawRealtime31;
                                                                                    syncs = syncs2;
                                                                                }
                                                                            }
                                                                            long rawRealtime32 = rawRealtime7;
                                                                            ArrayMap<String, SparseIntArray> completions = u10.getJobCompletionStats();
                                                                            for (ic = completions.size() - 1; ic >= 0; ic--) {
                                                                                SparseIntArray types = completions.valueAt(ic);
                                                                                if (types != null) {
                                                                                    pw.print(prefix);
                                                                                    printWriter6.print("    Job Completions ");
                                                                                    printWriter6.print(completions.keyAt(ic));
                                                                                    printWriter6.print(SettingsStringUtil.DELIMITER);
                                                                                    for (int it2 = 0; it2 < types.size(); it2++) {
                                                                                        printWriter6.print(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                                                                                        printWriter6.print(JobParameters.getReasonName(types.keyAt(it2)));
                                                                                        printWriter6.print("(");
                                                                                        printWriter6.print(types.valueAt(it2));
                                                                                        printWriter6.print("x)");
                                                                                    }
                                                                                    pw.println();
                                                                                }
                                                                            }
                                                                            u10.getDeferredJobsLineLocked(sb5, i4);
                                                                            if (sb5.length() > 0) {
                                                                                printWriter6.print("    Jobs deferred on launch ");
                                                                                printWriter6.println(sb5.toString());
                                                                            }
                                                                            long j57 = btRxBytes3;
                                                                            long btRxBytes7 = btTxBytes3;
                                                                            long btTxBytes6 = mobileRxPackets;
                                                                            long j58 = wifiTxBytes3;
                                                                            PrintWriter printWriter9 = printWriter6;
                                                                            int uid4 = uid3;
                                                                            int i56 = wifiScanCountBg3;
                                                                            uidStats = uidStats9;
                                                                            int i57 = wifiScanCount2;
                                                                            long rawRealtimeMs12 = rawRealtimeMs11;
                                                                            Timer flashlightTurnedOnTimer = u10.getFlashlightTurnedOnTimer();
                                                                            long rawRealtimeMs13 = rawRealtimeMs12;
                                                                            iu = iu5;
                                                                            int NU11 = NU10;
                                                                            long j59 = mobileRxBytes3;
                                                                            long j60 = wifiWakeup7;
                                                                            long j61 = totalPartialWakelock3;
                                                                            long rawRealtime33 = rawRealtime32;
                                                                            long rawRealtimeMs14 = rawRealtime33;
                                                                            ArrayMap<String, ? extends Timer> arrayMap = jobs;
                                                                            ArrayMap<String, SparseIntArray> arrayMap2 = completions;
                                                                            Uid u11 = u10;
                                                                            long j62 = uidWifiRunningTime;
                                                                            long j63 = mobileWakeup;
                                                                            int i58 = 0;
                                                                            long uidWifiRunningTime6 = wifiScanActualTimeBg2;
                                                                            long wifiScanActualTimeBg3 = uidMobileActiveTime3;
                                                                            long uidMobileActiveTime4 = wifiScanActualTime4;
                                                                            long wifiScanActualTime5 = fullWifiLockOnTime3;
                                                                            int i59 = i4;
                                                                            long rawRealtime34 = rawRealtime33;
                                                                            StringBuilder sb12 = sb5;
                                                                            String str6 = str2;
                                                                            cpuFreqs = cpuFreqs3;
                                                                            int i60 = uidMobileActiveCount2;
                                                                            ArrayMap<String, ? extends Uid.Wakelock> arrayMap3 = wakelocks2;
                                                                            StringBuilder sb13 = sb12;
                                                                            long j64 = rawRealtime34;
                                                                            SparseArray<? extends Uid.Sensor> sensors2 = u11.getSensorStats();
                                                                            NSE = sensors2.size();
                                                                            boolean uidActivity4 = uidActivity3 | printTimer(printWriter9, sb5, flashlightTurnedOnTimer, rawRealtimeMs14, i59, str6, "Flashlight") | printTimer(pw, sb13, u11.getCameraTurnedOnTimer(), j64, i59, str6, "Camera") | printTimer(pw, sb13, u11.getVideoTurnedOnTimer(), j64, i59, str6, "Video") | printTimer(pw, sb13, u11.getAudioTurnedOnTimer(), j64, i59, str6, "Audio");
                                                                            ise = 0;
                                                                            while (ise < NSE) {
                                                                                Uid.Sensor se = (Uid.Sensor) sensors2.valueAt(ise);
                                                                                int sensorNumber = sensors2.keyAt(ise);
                                                                                StringBuilder sb14 = sb12;
                                                                                sb14.setLength(i58);
                                                                                sb14.append(str2);
                                                                                sb14.append("    Sensor ");
                                                                                int handle = se.getHandle();
                                                                                if (handle == -10000) {
                                                                                    sb14.append("GPS");
                                                                                } else {
                                                                                    sb14.append(handle);
                                                                                }
                                                                                sb14.append(": ");
                                                                                Timer timer18 = se.getSensorTime();
                                                                                if (timer18 != null) {
                                                                                    NU4 = NU11;
                                                                                    long rawRealtime35 = rawRealtime34;
                                                                                    int i61 = sensorNumber;
                                                                                    int i62 = handle;
                                                                                    long totalTime6 = (timer18.getTotalTimeLocked(rawRealtime35, i4) + 500) / 1000;
                                                                                    uid = uid4;
                                                                                    int count4 = timer18.getCountLocked(i4);
                                                                                    NSE2 = NSE;
                                                                                    Timer bgTimer3 = se.getSensorBackgroundTime();
                                                                                    int bgCount4 = bgTimer3 != null ? bgTimer3.getCountLocked(i4) : 0;
                                                                                    rawRealtime8 = rawRealtime35;
                                                                                    long actualTime = timer18.getTotalDurationMsLocked(rawRealtimeMs13);
                                                                                    long bgActualTime = bgTimer3 != null ? bgTimer3.getTotalDurationMsLocked(rawRealtimeMs13) : 0;
                                                                                    if (totalTime6 != 0) {
                                                                                        if (actualTime != totalTime6) {
                                                                                            formatTimeMs(sb14, totalTime6);
                                                                                            sb14.append("blamed realtime, ");
                                                                                        }
                                                                                        formatTimeMs(sb14, actualTime);
                                                                                        sb14.append("realtime (");
                                                                                        sb14.append(count4);
                                                                                        sb14.append(" times)");
                                                                                        rawRealtimeMs4 = rawRealtimeMs13;
                                                                                        long bgActualTime2 = bgActualTime;
                                                                                        if (bgActualTime2 == 0) {
                                                                                            bgCount = bgCount4;
                                                                                            if (bgCount <= 0) {
                                                                                            }
                                                                                        } else {
                                                                                            bgCount = bgCount4;
                                                                                        }
                                                                                        long j65 = actualTime;
                                                                                        sb14.append(", ");
                                                                                        formatTimeMs(sb14, bgActualTime2);
                                                                                        sb14.append("background (");
                                                                                        sb14.append(bgCount);
                                                                                        sb14.append(" times)");
                                                                                    } else {
                                                                                        rawRealtimeMs4 = rawRealtimeMs13;
                                                                                        long j66 = actualTime;
                                                                                        int i63 = bgCount4;
                                                                                        long rawRealtimeMs15 = bgActualTime;
                                                                                        sb14.append("(not used)");
                                                                                    }
                                                                                } else {
                                                                                    rawRealtimeMs4 = rawRealtimeMs13;
                                                                                    NU4 = NU11;
                                                                                    uid = uid4;
                                                                                    int i64 = sensorNumber;
                                                                                    int i65 = handle;
                                                                                    NSE2 = NSE;
                                                                                    rawRealtime8 = rawRealtime34;
                                                                                    sb14.append("(not used)");
                                                                                }
                                                                                pw.println(sb14.toString());
                                                                                uidActivity4 = true;
                                                                                ise++;
                                                                                sb12 = sb14;
                                                                                NU11 = NU4;
                                                                                uid4 = uid;
                                                                                NSE = NSE2;
                                                                                rawRealtime34 = rawRealtime8;
                                                                                rawRealtimeMs13 = rawRealtimeMs4;
                                                                                str2 = prefix;
                                                                                i58 = 0;
                                                                            }
                                                                            rawRealtimeMs = rawRealtimeMs13;
                                                                            NU3 = NU11;
                                                                            int i66 = uid4;
                                                                            long rawRealtime36 = rawRealtime34;
                                                                            StringBuilder sb15 = sb12;
                                                                            printWriter2 = pw;
                                                                            PrintWriter printWriter10 = printWriter2;
                                                                            StringBuilder sb16 = sb15;
                                                                            long j67 = rawRealtime36;
                                                                            sb3 = sb15;
                                                                            int i67 = i4;
                                                                            int NSE3 = NSE;
                                                                            str3 = prefix;
                                                                            String str7 = str3;
                                                                            SparseArray<? extends Uid.Sensor> sensors3 = sensors2;
                                                                            StringBuilder sb17 = sb3;
                                                                            boolean uidActivity5 = uidActivity4 | printTimer(printWriter10, sb16, u11.getVibratorOnTimer(), j67, i67, str7, "Vibrator") | printTimer(printWriter10, sb17, u11.getForegroundActivityTimer(), j67, i67, str7, "Foreground activities") | printTimer(printWriter2, sb17, u11.getForegroundServiceTimer(), j67, i67, str7, "Foreground services");
                                                                            totalStateTime = 0;
                                                                            boolean uidActivity6 = uidActivity5;
                                                                            ips = 0;
                                                                            while (ips < 7) {
                                                                                long rawRealtime37 = rawRealtime36;
                                                                                long time8 = u11.getProcessStateTime(ips, rawRealtime37, i4);
                                                                                if (time8 > 0) {
                                                                                    sb3.setLength(0);
                                                                                    sb3.append(str3);
                                                                                    sb3.append("    ");
                                                                                    sb3.append(Uid.PROCESS_STATE_NAMES[ips]);
                                                                                    sb3.append(" for: ");
                                                                                    sensors = sensors3;
                                                                                    formatTimeMs(sb3, (time8 + 500) / 1000);
                                                                                    printWriter2.println(sb3.toString());
                                                                                    uidActivity6 = true;
                                                                                    totalStateTime += time8;
                                                                                } else {
                                                                                    sensors = sensors3;
                                                                                }
                                                                                ips++;
                                                                                rawRealtime36 = rawRealtime37;
                                                                                sensors3 = sensors;
                                                                            }
                                                                            long rawRealtime38 = rawRealtime36;
                                                                            if (totalStateTime > 0) {
                                                                                sb3.setLength(0);
                                                                                sb3.append(str3);
                                                                                sb3.append("    Total running: ");
                                                                                formatTimeMs(sb3, (totalStateTime + 500) / 1000);
                                                                                printWriter2.println(sb3.toString());
                                                                            }
                                                                            userCpuTimeUs = u11.getUserCpuTimeUs(i4);
                                                                            systemCpuTimeUs = u11.getSystemCpuTimeUs(i4);
                                                                            if (userCpuTimeUs <= 0 || systemCpuTimeUs > 0) {
                                                                                sb3.setLength(0);
                                                                                sb3.append(str3);
                                                                                sb3.append("    Total cpu time: u=");
                                                                                long j68 = totalStateTime;
                                                                                formatTimeMs(sb3, userCpuTimeUs / 1000);
                                                                                sb3.append("s=");
                                                                                formatTimeMs(sb3, systemCpuTimeUs / 1000);
                                                                                printWriter2.println(sb3.toString());
                                                                            } else {
                                                                                long j69 = totalStateTime;
                                                                            }
                                                                            cpuFreqTimes = u11.getCpuFreqTimes(i4);
                                                                            if (cpuFreqTimes != null) {
                                                                                sb3.setLength(0);
                                                                                sb3.append("    Total cpu time per freq:");
                                                                                int i68 = 0;
                                                                                while (i68 < cpuFreqTimes.length) {
                                                                                    sb3.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + cpuFreqTimes[i68]);
                                                                                    i68++;
                                                                                    uidActivity6 = uidActivity6;
                                                                                }
                                                                                uidActivity = uidActivity6;
                                                                                printWriter2.println(sb3.toString());
                                                                            } else {
                                                                                uidActivity = uidActivity6;
                                                                            }
                                                                            screenOffCpuFreqTimes = u11.getScreenOffCpuFreqTimes(i4);
                                                                            if (screenOffCpuFreqTimes != null) {
                                                                                sb3.setLength(0);
                                                                                sb3.append("    Total screen-off cpu time per freq:");
                                                                                int i69 = 0;
                                                                                while (i69 < screenOffCpuFreqTimes.length) {
                                                                                    sb3.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + screenOffCpuFreqTimes[i69]);
                                                                                    i69++;
                                                                                    rawRealtime38 = rawRealtime38;
                                                                                }
                                                                                rawRealtime3 = rawRealtime38;
                                                                                printWriter2.println(sb3.toString());
                                                                            } else {
                                                                                rawRealtime3 = rawRealtime38;
                                                                            }
                                                                            procState = 0;
                                                                            while (procState < 7) {
                                                                                long[] cpuTimes = u11.getCpuFreqTimes(i4, procState);
                                                                                if (cpuTimes != null) {
                                                                                    sb3.setLength(0);
                                                                                    sb3.append("    Cpu times per freq at state " + Uid.PROCESS_STATE_NAMES[procState] + SettingsStringUtil.DELIMITER);
                                                                                    int i70 = 0;
                                                                                    while (i70 < cpuTimes.length) {
                                                                                        sb3.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + cpuTimes[i70]);
                                                                                        i70++;
                                                                                        userCpuTimeUs = userCpuTimeUs;
                                                                                    }
                                                                                    userCpuTimeUs2 = userCpuTimeUs;
                                                                                    printWriter2.println(sb3.toString());
                                                                                } else {
                                                                                    userCpuTimeUs2 = userCpuTimeUs;
                                                                                }
                                                                                long[] screenOffCpuTimes = u11.getScreenOffCpuFreqTimes(i4, procState);
                                                                                if (screenOffCpuTimes != null) {
                                                                                    sb3.setLength(0);
                                                                                    sb3.append("   Screen-off cpu times per freq at state " + Uid.PROCESS_STATE_NAMES[procState] + SettingsStringUtil.DELIMITER);
                                                                                    for (int i71 = 0; i71 < screenOffCpuTimes.length; i71++) {
                                                                                        sb3.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + screenOffCpuTimes[i71]);
                                                                                    }
                                                                                    printWriter2.println(sb3.toString());
                                                                                }
                                                                                procState++;
                                                                                userCpuTimeUs = userCpuTimeUs2;
                                                                            }
                                                                            processStats = u11.getProcessStats();
                                                                            if (processStats != null) {
                                                                                int ipr = processStats.size() - 1;
                                                                                boolean numAnrs = uidActivity;
                                                                                while (ipr >= 0) {
                                                                                    Uid.Proc ps2 = (Uid.Proc) processStats.valueAt(ipr);
                                                                                    long userTime = ps2.getUserTime(i4);
                                                                                    long systemTime = ps2.getSystemTime(i4);
                                                                                    long[] cpuFreqTimes2 = cpuFreqTimes;
                                                                                    long foregroundTime2 = ps2.getForegroundTime(i4);
                                                                                    int NSE4 = NSE3;
                                                                                    int starts2 = ps2.getStarts(i4);
                                                                                    long[] screenOffCpuFreqTimes2 = screenOffCpuFreqTimes;
                                                                                    int numCrashes2 = ps2.getNumCrashes(i4);
                                                                                    boolean uidActivity7 = numAnrs;
                                                                                    int numAnrs2 = ps2.getNumAnrs(i4);
                                                                                    int numExcessive3 = i4 == 0 ? ps2.countExcessivePowers() : 0;
                                                                                    if (userTime == 0 && systemTime == 0 && foregroundTime2 == 0 && starts2 == 0) {
                                                                                        numExcessive = numExcessive3;
                                                                                        if (numExcessive == 0 && numCrashes2 == 0 && numAnrs2 == 0) {
                                                                                            u5 = u11;
                                                                                            processStats2 = processStats;
                                                                                            numAnrs = uidActivity7;
                                                                                            printWriter2 = pw;
                                                                                            ipr--;
                                                                                            cpuFreqTimes = cpuFreqTimes2;
                                                                                            NSE3 = NSE4;
                                                                                            screenOffCpuFreqTimes = screenOffCpuFreqTimes2;
                                                                                            u11 = u5;
                                                                                            processStats = processStats2;
                                                                                            i4 = which;
                                                                                        }
                                                                                    } else {
                                                                                        numExcessive = numExcessive3;
                                                                                    }
                                                                                    u5 = u11;
                                                                                    sb3.setLength(0);
                                                                                    sb3.append(str3);
                                                                                    sb3.append("    Proc ");
                                                                                    sb3.append(processStats.keyAt(ipr));
                                                                                    sb3.append(":\n");
                                                                                    sb3.append(str3);
                                                                                    sb3.append("      CPU: ");
                                                                                    formatTimeMs(sb3, userTime);
                                                                                    sb3.append("usr + ");
                                                                                    formatTimeMs(sb3, systemTime);
                                                                                    sb3.append("krn ; ");
                                                                                    formatTimeMs(sb3, foregroundTime2);
                                                                                    sb3.append(FOREGROUND_ACTIVITY_DATA);
                                                                                    if (starts2 == 0 && numCrashes2 == 0 && numAnrs2 == 0) {
                                                                                        foregroundTime = foregroundTime2;
                                                                                    } else {
                                                                                        sb3.append("\n");
                                                                                        sb3.append(str3);
                                                                                        sb3.append("      ");
                                                                                        boolean hasOne = false;
                                                                                        if (starts2 != 0) {
                                                                                            hasOne = true;
                                                                                            sb3.append(starts2);
                                                                                            foregroundTime = foregroundTime2;
                                                                                            sb3.append(" starts");
                                                                                        } else {
                                                                                            foregroundTime = foregroundTime2;
                                                                                        }
                                                                                        if (numCrashes2 != 0) {
                                                                                            if (hasOne) {
                                                                                                sb3.append(", ");
                                                                                            }
                                                                                            hasOne = true;
                                                                                            sb3.append(numCrashes2);
                                                                                            sb3.append(" crashes");
                                                                                        }
                                                                                        if (numAnrs2 != 0) {
                                                                                            if (hasOne) {
                                                                                                sb3.append(", ");
                                                                                            }
                                                                                            sb3.append(numAnrs2);
                                                                                            sb3.append(" anrs");
                                                                                        }
                                                                                    }
                                                                                    long j70 = foregroundTime;
                                                                                    printWriter2 = pw;
                                                                                    printWriter2.println(sb3.toString());
                                                                                    int e = 0;
                                                                                    while (e < numExcessive) {
                                                                                        Uid.Proc.ExcessivePower ew = ps2.getExcessivePower(e);
                                                                                        if (ew != null) {
                                                                                            pw.print(prefix);
                                                                                            starts = starts2;
                                                                                            printWriter2.print("      * Killed for ");
                                                                                            numExcessive2 = numExcessive;
                                                                                            if (ew.type == 2) {
                                                                                                printWriter2.print(CPU_DATA);
                                                                                            } else {
                                                                                                printWriter2.print("unknown");
                                                                                            }
                                                                                            printWriter2.print(" use: ");
                                                                                            numCrashes = numCrashes2;
                                                                                            TimeUtils.formatDuration(ew.usedTime, printWriter2);
                                                                                            printWriter2.print(" over ");
                                                                                            TimeUtils.formatDuration(ew.overTime, printWriter2);
                                                                                            if (ew.overTime != 0) {
                                                                                                printWriter2.print(" (");
                                                                                                processStats3 = processStats;
                                                                                                printWriter2.print((ew.usedTime * 100) / ew.overTime);
                                                                                                printWriter2.println("%)");
                                                                                            } else {
                                                                                                processStats3 = processStats;
                                                                                            }
                                                                                        } else {
                                                                                            starts = starts2;
                                                                                            numExcessive2 = numExcessive;
                                                                                            numCrashes = numCrashes2;
                                                                                            processStats3 = processStats;
                                                                                        }
                                                                                        e++;
                                                                                        starts2 = starts;
                                                                                        numExcessive = numExcessive2;
                                                                                        numCrashes2 = numCrashes;
                                                                                        processStats = processStats3;
                                                                                    }
                                                                                    int i72 = numExcessive;
                                                                                    int i73 = numCrashes2;
                                                                                    processStats2 = processStats;
                                                                                    numAnrs = true;
                                                                                    ipr--;
                                                                                    cpuFreqTimes = cpuFreqTimes2;
                                                                                    NSE3 = NSE4;
                                                                                    screenOffCpuFreqTimes = screenOffCpuFreqTimes2;
                                                                                    u11 = u5;
                                                                                    processStats = processStats2;
                                                                                    i4 = which;
                                                                                }
                                                                                u4 = u11;
                                                                                int i74 = NSE3;
                                                                                long[] jArr = screenOffCpuFreqTimes;
                                                                                ArrayMap<String, ? extends Uid.Proc> arrayMap4 = processStats;
                                                                                uidActivity2 = numAnrs;
                                                                            } else {
                                                                                u4 = u11;
                                                                                int i75 = NSE3;
                                                                                long[] jArr2 = screenOffCpuFreqTimes;
                                                                                ArrayMap<String, ? extends Uid.Proc> arrayMap5 = processStats;
                                                                                uidActivity2 = uidActivity;
                                                                            }
                                                                            Uid u12 = u4;
                                                                            packageStats = u12.getPackageStats();
                                                                            if (packageStats != null) {
                                                                                int ipkg = packageStats.size() - 1;
                                                                                boolean uidActivity8 = uidActivity2;
                                                                                while (ipkg >= 0) {
                                                                                    pw.print(prefix);
                                                                                    printWriter2.print("    Apk ");
                                                                                    printWriter2.print(packageStats.keyAt(ipkg));
                                                                                    printWriter2.println(SettingsStringUtil.DELIMITER);
                                                                                    boolean apkActivity = false;
                                                                                    Uid.Pkg ps3 = (Uid.Pkg) packageStats.valueAt(ipkg);
                                                                                    ArrayMap<String, ? extends Counter> alarms2 = ps3.getWakeupAlarmStats();
                                                                                    for (int iwa = alarms2.size() - 1; iwa >= 0; iwa--) {
                                                                                        pw.print(prefix);
                                                                                        printWriter2.print("      Wakeup alarm ");
                                                                                        printWriter2.print(alarms2.keyAt(iwa));
                                                                                        printWriter2.print(": ");
                                                                                        printWriter2.print(((Counter) alarms2.valueAt(iwa)).getCountLocked(which));
                                                                                        printWriter2.println(" times");
                                                                                        apkActivity = true;
                                                                                    }
                                                                                    int i76 = which;
                                                                                    ArrayMap<String, ? extends Uid.Pkg.Serv> serviceStats = ps3.getServiceStats();
                                                                                    int isvc = serviceStats.size() - 1;
                                                                                    while (isvc >= 0) {
                                                                                        Uid.Pkg.Serv ss = (Uid.Pkg.Serv) serviceStats.valueAt(isvc);
                                                                                        long batteryUptime5 = batteryUptime4;
                                                                                        long batteryUptime6 = ss.getStartTime(batteryUptime5, i76);
                                                                                        Uid u13 = u12;
                                                                                        int starts3 = ss.getStarts(i76);
                                                                                        ArrayMap<String, ? extends Uid.Pkg> packageStats2 = packageStats;
                                                                                        int launches = ss.getLaunches(i76);
                                                                                        if (batteryUptime6 == 0 && starts3 == 0 && launches == 0) {
                                                                                            ps = ps3;
                                                                                            alarms = alarms2;
                                                                                        } else {
                                                                                            ps = ps3;
                                                                                            sb3.setLength(0);
                                                                                            sb3.append(str3);
                                                                                            sb3.append("      Service ");
                                                                                            sb3.append(serviceStats.keyAt(isvc));
                                                                                            sb3.append(":\n");
                                                                                            sb3.append(str3);
                                                                                            sb3.append("        Created for: ");
                                                                                            alarms = alarms2;
                                                                                            formatTimeMs(sb3, batteryUptime6 / 1000);
                                                                                            sb3.append("uptime\n");
                                                                                            sb3.append(str3);
                                                                                            sb3.append("        Starts: ");
                                                                                            sb3.append(starts3);
                                                                                            sb3.append(", launches: ");
                                                                                            sb3.append(launches);
                                                                                            printWriter2.println(sb3.toString());
                                                                                            apkActivity = true;
                                                                                        }
                                                                                        isvc--;
                                                                                        batteryUptime4 = batteryUptime5;
                                                                                        u12 = u13;
                                                                                        packageStats = packageStats2;
                                                                                        ps3 = ps;
                                                                                        alarms2 = alarms;
                                                                                    }
                                                                                    Uid u14 = u12;
                                                                                    ArrayMap<String, ? extends Uid.Pkg> packageStats3 = packageStats;
                                                                                    Uid.Pkg pkg = ps3;
                                                                                    ArrayMap<String, ? extends Counter> arrayMap6 = alarms2;
                                                                                    long batteryUptime7 = batteryUptime4;
                                                                                    if (!apkActivity) {
                                                                                        pw.print(prefix);
                                                                                        printWriter2.println("      (nothing executed)");
                                                                                    }
                                                                                    uidActivity8 = true;
                                                                                    ipkg--;
                                                                                    batteryUptime4 = batteryUptime7;
                                                                                    u12 = u14;
                                                                                    packageStats = packageStats3;
                                                                                }
                                                                                ArrayMap<String, ? extends Uid.Pkg> arrayMap7 = packageStats;
                                                                                batteryUptime = batteryUptime4;
                                                                                int i77 = which;
                                                                                if (!uidActivity8) {
                                                                                    pw.print(prefix);
                                                                                    printWriter2.println("    (nothing executed)");
                                                                                }
                                                                            } else {
                                                                                batteryUptime = batteryUptime4;
                                                                                int i78 = which;
                                                                            }
                                                                        }
                                                                    } else {
                                                                        btTxBytes = btTxBytes4;
                                                                    }
                                                                    pw.print(prefix);
                                                                    printWriter8.print("    Bluetooth network: ");
                                                                    btRxBytes = btRxBytes4;
                                                                    printWriter8.print(batteryStats3.formatBytesLocked(btRxBytes));
                                                                    printWriter8.print(" received, ");
                                                                    printWriter8.print(batteryStats3.formatBytesLocked(btTxBytes));
                                                                    printWriter8.println(" sent");
                                                                    bleTimer = u2.getBluetoothScanTimer();
                                                                    if (bleTimer != null) {
                                                                    }
                                                                    if (u2.hasUserActivity()) {
                                                                    }
                                                                    wakelocks = u2.getWakelockStats();
                                                                    int countWakelock22 = 0;
                                                                    if (wakelocks != null) {
                                                                    }
                                                                    if (countWakelock > 1) {
                                                                    }
                                                                    Uid u102 = u3;
                                                                    mcTimer = u102.getMulticastWakelockStats();
                                                                    if (mcTimer != null) {
                                                                    }
                                                                    syncs = u102.getSyncStats();
                                                                    if (syncs != null) {
                                                                    }
                                                                    long totalPartialWakelock32 = totalFullWakelock;
                                                                    long rawRealtimeMs112 = rawRealtimeMs3;
                                                                    jobs = u102.getJobStats();
                                                                    if (jobs != null) {
                                                                    }
                                                                    long rawRealtime322 = rawRealtime7;
                                                                    ArrayMap<String, SparseIntArray> completions2 = u102.getJobCompletionStats();
                                                                    while (ic >= 0) {
                                                                    }
                                                                    u102.getDeferredJobsLineLocked(sb5, i4);
                                                                    if (sb5.length() > 0) {
                                                                    }
                                                                    long j572 = btRxBytes3;
                                                                    long btRxBytes72 = btTxBytes3;
                                                                    long btTxBytes62 = mobileRxPackets;
                                                                    long j582 = wifiTxBytes3;
                                                                    PrintWriter printWriter92 = printWriter6;
                                                                    int uid42 = uid3;
                                                                    int i562 = wifiScanCountBg3;
                                                                    uidStats = uidStats9;
                                                                    int i572 = wifiScanCount2;
                                                                    long rawRealtimeMs122 = rawRealtimeMs112;
                                                                    Timer flashlightTurnedOnTimer2 = u102.getFlashlightTurnedOnTimer();
                                                                    long rawRealtimeMs132 = rawRealtimeMs122;
                                                                    iu = iu5;
                                                                    int NU112 = NU10;
                                                                    long j592 = mobileRxBytes3;
                                                                    long j602 = wifiWakeup7;
                                                                    long j612 = totalPartialWakelock32;
                                                                    long rawRealtime332 = rawRealtime322;
                                                                    long rawRealtimeMs142 = rawRealtime332;
                                                                    ArrayMap<String, ? extends Timer> arrayMap8 = jobs;
                                                                    ArrayMap<String, SparseIntArray> arrayMap22 = completions2;
                                                                    Uid u112 = u102;
                                                                    long j622 = uidWifiRunningTime;
                                                                    long j632 = mobileWakeup;
                                                                    int i582 = 0;
                                                                    long uidWifiRunningTime62 = wifiScanActualTimeBg2;
                                                                    long wifiScanActualTimeBg32 = uidMobileActiveTime3;
                                                                    long uidMobileActiveTime42 = wifiScanActualTime4;
                                                                    long wifiScanActualTime52 = fullWifiLockOnTime3;
                                                                    int i592 = i4;
                                                                    long rawRealtime342 = rawRealtime332;
                                                                    StringBuilder sb122 = sb5;
                                                                    String str62 = str2;
                                                                    cpuFreqs = cpuFreqs3;
                                                                    int i602 = uidMobileActiveCount2;
                                                                    ArrayMap<String, ? extends Uid.Wakelock> arrayMap32 = wakelocks2;
                                                                    StringBuilder sb132 = sb122;
                                                                    long j642 = rawRealtime342;
                                                                    SparseArray<? extends Uid.Sensor> sensors22 = u112.getSensorStats();
                                                                    NSE = sensors22.size();
                                                                    boolean uidActivity42 = uidActivity3 | printTimer(printWriter92, sb5, flashlightTurnedOnTimer2, rawRealtimeMs142, i592, str62, "Flashlight") | printTimer(pw, sb132, u112.getCameraTurnedOnTimer(), j642, i592, str62, "Camera") | printTimer(pw, sb132, u112.getVideoTurnedOnTimer(), j642, i592, str62, "Video") | printTimer(pw, sb132, u112.getAudioTurnedOnTimer(), j642, i592, str62, "Audio");
                                                                    ise = 0;
                                                                    while (ise < NSE) {
                                                                    }
                                                                    rawRealtimeMs = rawRealtimeMs132;
                                                                    NU3 = NU112;
                                                                    int i662 = uid42;
                                                                    long rawRealtime362 = rawRealtime342;
                                                                    StringBuilder sb152 = sb122;
                                                                    printWriter2 = pw;
                                                                    PrintWriter printWriter102 = printWriter2;
                                                                    StringBuilder sb162 = sb152;
                                                                    long j672 = rawRealtime362;
                                                                    sb3 = sb152;
                                                                    int i672 = i4;
                                                                    int NSE32 = NSE;
                                                                    str3 = prefix;
                                                                    String str72 = str3;
                                                                    SparseArray<? extends Uid.Sensor> sensors32 = sensors22;
                                                                    StringBuilder sb172 = sb3;
                                                                    boolean uidActivity52 = uidActivity42 | printTimer(printWriter102, sb162, u112.getVibratorOnTimer(), j672, i672, str72, "Vibrator") | printTimer(printWriter102, sb172, u112.getForegroundActivityTimer(), j672, i672, str72, "Foreground activities") | printTimer(printWriter2, sb172, u112.getForegroundServiceTimer(), j672, i672, str72, "Foreground services");
                                                                    totalStateTime = 0;
                                                                    boolean uidActivity62 = uidActivity52;
                                                                    ips = 0;
                                                                    while (ips < 7) {
                                                                    }
                                                                    long rawRealtime382 = rawRealtime362;
                                                                    if (totalStateTime > 0) {
                                                                    }
                                                                    userCpuTimeUs = u112.getUserCpuTimeUs(i4);
                                                                    systemCpuTimeUs = u112.getSystemCpuTimeUs(i4);
                                                                    if (userCpuTimeUs <= 0) {
                                                                    }
                                                                    sb3.setLength(0);
                                                                    sb3.append(str3);
                                                                    sb3.append("    Total cpu time: u=");
                                                                    long j682 = totalStateTime;
                                                                    formatTimeMs(sb3, userCpuTimeUs / 1000);
                                                                    sb3.append("s=");
                                                                    formatTimeMs(sb3, systemCpuTimeUs / 1000);
                                                                    printWriter2.println(sb3.toString());
                                                                    cpuFreqTimes = u112.getCpuFreqTimes(i4);
                                                                    if (cpuFreqTimes != null) {
                                                                    }
                                                                    screenOffCpuFreqTimes = u112.getScreenOffCpuFreqTimes(i4);
                                                                    if (screenOffCpuFreqTimes != null) {
                                                                    }
                                                                    procState = 0;
                                                                    while (procState < 7) {
                                                                    }
                                                                    processStats = u112.getProcessStats();
                                                                    if (processStats != null) {
                                                                    }
                                                                    Uid u122 = u4;
                                                                    packageStats = u122.getPackageStats();
                                                                    if (packageStats != null) {
                                                                    }
                                                                }
                                                            } else {
                                                                wifiWakeup3 = wifiWakeup6;
                                                                uidWifiRunningTime3 = uidWifiRunningTime4;
                                                            }
                                                        } else {
                                                            wifiRxBytes2 = wifiRxBytes;
                                                            wifiWakeup3 = wifiWakeup6;
                                                            wifiRxBytes3 = wifiScanActualTimeBg;
                                                            uidWifiRunningTime3 = uidWifiRunningTime4;
                                                        }
                                                    } else {
                                                        wifiRxBytes2 = wifiRxBytes;
                                                        wifiTxPackets2 = wifiTxPackets;
                                                        wifiWakeup3 = wifiWakeup6;
                                                        wifiScanActualTime2 = wifiScanActualTime3;
                                                        wifiRxBytes3 = wifiScanActualTimeBg;
                                                        uidWifiRunningTime3 = uidWifiRunningTime4;
                                                    }
                                                } else {
                                                    wifiRxBytes2 = wifiRxBytes;
                                                    wifiTxPackets2 = wifiTxPackets;
                                                    wifiWakeup3 = wifiWakeup6;
                                                    wifiScanActualTime2 = wifiScanActualTime3;
                                                    wifiRxBytes3 = wifiScanActualTimeBg;
                                                    uidWifiRunningTime3 = uidWifiRunningTime4;
                                                    wifiScanCountBg2 = wifiScanCountBg;
                                                }
                                            } else {
                                                wifiRxBytes2 = wifiRxBytes;
                                                wifiTxPackets2 = wifiTxPackets;
                                                wifiWakeup3 = wifiWakeup6;
                                                wifiScanCount = wifiScanCount3;
                                                wifiScanActualTime2 = wifiScanActualTime3;
                                                wifiRxBytes3 = wifiScanActualTimeBg;
                                                uidWifiRunningTime3 = uidWifiRunningTime4;
                                                wifiScanCountBg2 = wifiScanCountBg;
                                            }
                                        } else {
                                            wifiRxBytes2 = wifiRxBytes;
                                            wifiTxPackets2 = wifiTxPackets;
                                            wifiRxPackets3 = wifiRxPackets2;
                                            wifiWakeup3 = wifiWakeup6;
                                            wifiScanCount = wifiScanCount3;
                                            wifiRxPackets4 = wifiScanTime2;
                                            wifiScanActualTime2 = wifiScanActualTime3;
                                            wifiRxBytes3 = wifiScanActualTimeBg;
                                            uidWifiRunningTime3 = uidWifiRunningTime4;
                                            wifiScanCountBg2 = wifiScanCountBg;
                                        }
                                        u = u8;
                                        sb5 = sb10;
                                        sb5.setLength(0);
                                        sb5.append(str5);
                                        sb5.append("    Wifi Running: ");
                                        formatTimeMs(sb5, uidWifiRunningTime3 / 1000);
                                        sb5.append("(");
                                        long whichBatteryRealtime13 = whichBatteryRealtime;
                                        sb5.append(batteryStats3.formatRatioLocked(uidWifiRunningTime3, whichBatteryRealtime13));
                                        sb5.append(")\n");
                                        sb5.append(str5);
                                        sb5.append("    Full Wifi Lock: ");
                                        uidWifiRunningTime = uidWifiRunningTime3;
                                        formatTimeMs(sb5, fullWifiLockOnTime / 1000);
                                        sb5.append("(");
                                        sb5.append(batteryStats3.formatRatioLocked(fullWifiLockOnTime, whichBatteryRealtime13));
                                        sb5.append(")\n");
                                        sb5.append(str5);
                                        sb5.append("    Wifi Scan (blamed): ");
                                        formatTimeMs(sb5, wifiRxPackets4 / 1000);
                                        sb5.append("(");
                                        sb5.append(batteryStats3.formatRatioLocked(wifiRxPackets4, whichBatteryRealtime13));
                                        sb5.append(") ");
                                        sb5.append(wifiScanCount);
                                        sb5.append("x\n");
                                        sb5.append(str5);
                                        sb5.append("    Wifi Scan (actual): ");
                                        formatTimeMs(sb5, wifiScanActualTime2 / 1000);
                                        sb5.append("(");
                                        whichBatteryRealtime3 = whichBatteryRealtime13;
                                        uidWifiRunningTime2 = rawRealtime4;
                                        sb5.append(batteryStats3.formatRatioLocked(wifiScanActualTime2, batteryStats3.computeBatteryRealtime(uidWifiRunningTime2, 0)));
                                        sb5.append(") ");
                                        sb5.append(wifiScanCount);
                                        sb5.append("x\n");
                                        sb5.append(str5);
                                        sb5.append("    Background Wifi Scan: ");
                                        wifiScanActualTime = wifiScanActualTime2;
                                        wifiRxBytes3 = wifiRxBytes3;
                                        formatTimeMs(sb5, wifiRxBytes3 / 1000);
                                        sb5.append("(");
                                        sb5.append(batteryStats3.formatRatioLocked(wifiRxBytes3, batteryStats3.computeBatteryRealtime(uidWifiRunningTime2, 0)));
                                        sb5.append(") ");
                                        sb5.append(wifiScanCountBg2);
                                        sb5.append("x");
                                        printWriter5 = pw;
                                        printWriter5.println(sb5.toString());
                                        long fullWifiLockOnTime32 = fullWifiLockOnTime;
                                        wifiWakeup4 = wifiWakeup3;
                                        if (wifiWakeup4 > 0) {
                                        }
                                        Uid u92 = u;
                                        long wifiScanActualTimeBg22 = wifiRxBytes3;
                                        long j362 = wifiRxBytes2;
                                        whichBatteryRealtime2 = whichBatteryRealtime3;
                                        long wifiWakeup72 = wifiWakeup4;
                                        u2 = u92;
                                        PrintWriter printWriter82 = printWriter5;
                                        long j372 = wifiTxPackets2;
                                        long wifiScanActualTime42 = wifiScanActualTime;
                                        long j382 = wifiRxPackets4;
                                        long j392 = wifiRxPackets3;
                                        batteryStats3.printControllerActivityIfInteresting(printWriter5, sb5, str5 + "  ", WIFI_CONTROLLER_NAME, u92.getWifiControllerActivity(), which);
                                        if (btRxBytes4 > 0) {
                                        }
                                        pw.print(prefix);
                                        printWriter82.print("    Bluetooth network: ");
                                        btRxBytes = btRxBytes4;
                                        printWriter82.print(batteryStats3.formatBytesLocked(btRxBytes));
                                        printWriter82.print(" received, ");
                                        printWriter82.print(batteryStats3.formatBytesLocked(btTxBytes));
                                        printWriter82.println(" sent");
                                        bleTimer = u2.getBluetoothScanTimer();
                                        if (bleTimer != null) {
                                        }
                                        if (u2.hasUserActivity()) {
                                        }
                                        wakelocks = u2.getWakelockStats();
                                        int countWakelock222 = 0;
                                        if (wakelocks != null) {
                                        }
                                        if (countWakelock > 1) {
                                        }
                                        Uid u1022 = u3;
                                        mcTimer = u1022.getMulticastWakelockStats();
                                        if (mcTimer != null) {
                                        }
                                        syncs = u1022.getSyncStats();
                                        if (syncs != null) {
                                        }
                                        long totalPartialWakelock322 = totalFullWakelock;
                                        long rawRealtimeMs1122 = rawRealtimeMs3;
                                        jobs = u1022.getJobStats();
                                        if (jobs != null) {
                                        }
                                        long rawRealtime3222 = rawRealtime7;
                                        ArrayMap<String, SparseIntArray> completions22 = u1022.getJobCompletionStats();
                                        while (ic >= 0) {
                                        }
                                        u1022.getDeferredJobsLineLocked(sb5, i4);
                                        if (sb5.length() > 0) {
                                        }
                                        long j5722 = btRxBytes3;
                                        long btRxBytes722 = btTxBytes3;
                                        long btTxBytes622 = mobileRxPackets;
                                        long j5822 = wifiTxBytes3;
                                        PrintWriter printWriter922 = printWriter6;
                                        int uid422 = uid3;
                                        int i5622 = wifiScanCountBg3;
                                        uidStats = uidStats9;
                                        int i5722 = wifiScanCount2;
                                        long rawRealtimeMs1222 = rawRealtimeMs1122;
                                        Timer flashlightTurnedOnTimer22 = u1022.getFlashlightTurnedOnTimer();
                                        long rawRealtimeMs1322 = rawRealtimeMs1222;
                                        iu = iu5;
                                        int NU1122 = NU10;
                                        long j5922 = mobileRxBytes3;
                                        long j6022 = wifiWakeup72;
                                        long j6122 = totalPartialWakelock322;
                                        long rawRealtime3322 = rawRealtime3222;
                                        long rawRealtimeMs1422 = rawRealtime3322;
                                        ArrayMap<String, ? extends Timer> arrayMap82 = jobs;
                                        ArrayMap<String, SparseIntArray> arrayMap222 = completions22;
                                        Uid u1122 = u1022;
                                        long j6222 = uidWifiRunningTime;
                                        long j6322 = mobileWakeup;
                                        int i5822 = 0;
                                        long uidWifiRunningTime622 = wifiScanActualTimeBg22;
                                        long wifiScanActualTimeBg322 = uidMobileActiveTime3;
                                        long uidMobileActiveTime422 = wifiScanActualTime42;
                                        long wifiScanActualTime522 = fullWifiLockOnTime32;
                                        int i5922 = i4;
                                        long rawRealtime3422 = rawRealtime3322;
                                        StringBuilder sb1222 = sb5;
                                        String str622 = str2;
                                        cpuFreqs = cpuFreqs3;
                                        int i6022 = uidMobileActiveCount2;
                                        ArrayMap<String, ? extends Uid.Wakelock> arrayMap322 = wakelocks2;
                                        StringBuilder sb1322 = sb1222;
                                        long j6422 = rawRealtime3422;
                                        SparseArray<? extends Uid.Sensor> sensors222 = u1122.getSensorStats();
                                        NSE = sensors222.size();
                                        boolean uidActivity422 = uidActivity3 | printTimer(printWriter922, sb5, flashlightTurnedOnTimer22, rawRealtimeMs1422, i5922, str622, "Flashlight") | printTimer(pw, sb1322, u1122.getCameraTurnedOnTimer(), j6422, i5922, str622, "Camera") | printTimer(pw, sb1322, u1122.getVideoTurnedOnTimer(), j6422, i5922, str622, "Video") | printTimer(pw, sb1322, u1122.getAudioTurnedOnTimer(), j6422, i5922, str622, "Audio");
                                        ise = 0;
                                        while (ise < NSE) {
                                        }
                                        rawRealtimeMs = rawRealtimeMs1322;
                                        NU3 = NU1122;
                                        int i6622 = uid422;
                                        long rawRealtime3622 = rawRealtime3422;
                                        StringBuilder sb1522 = sb1222;
                                        printWriter2 = pw;
                                        PrintWriter printWriter1022 = printWriter2;
                                        StringBuilder sb1622 = sb1522;
                                        long j6722 = rawRealtime3622;
                                        sb3 = sb1522;
                                        int i6722 = i4;
                                        int NSE322 = NSE;
                                        str3 = prefix;
                                        String str722 = str3;
                                        SparseArray<? extends Uid.Sensor> sensors322 = sensors222;
                                        StringBuilder sb1722 = sb3;
                                        boolean uidActivity522 = uidActivity422 | printTimer(printWriter1022, sb1622, u1122.getVibratorOnTimer(), j6722, i6722, str722, "Vibrator") | printTimer(printWriter1022, sb1722, u1122.getForegroundActivityTimer(), j6722, i6722, str722, "Foreground activities") | printTimer(printWriter2, sb1722, u1122.getForegroundServiceTimer(), j6722, i6722, str722, "Foreground services");
                                        totalStateTime = 0;
                                        boolean uidActivity622 = uidActivity522;
                                        ips = 0;
                                        while (ips < 7) {
                                        }
                                        long rawRealtime3822 = rawRealtime3622;
                                        if (totalStateTime > 0) {
                                        }
                                        userCpuTimeUs = u1122.getUserCpuTimeUs(i4);
                                        systemCpuTimeUs = u1122.getSystemCpuTimeUs(i4);
                                        if (userCpuTimeUs <= 0) {
                                        }
                                        sb3.setLength(0);
                                        sb3.append(str3);
                                        sb3.append("    Total cpu time: u=");
                                        long j6822 = totalStateTime;
                                        formatTimeMs(sb3, userCpuTimeUs / 1000);
                                        sb3.append("s=");
                                        formatTimeMs(sb3, systemCpuTimeUs / 1000);
                                        printWriter2.println(sb3.toString());
                                        cpuFreqTimes = u1122.getCpuFreqTimes(i4);
                                        if (cpuFreqTimes != null) {
                                        }
                                        screenOffCpuFreqTimes = u1122.getScreenOffCpuFreqTimes(i4);
                                        if (screenOffCpuFreqTimes != null) {
                                        }
                                        procState = 0;
                                        while (procState < 7) {
                                        }
                                        processStats = u1122.getProcessStats();
                                        if (processStats != null) {
                                        }
                                        Uid u1222 = u4;
                                        packageStats = u1222.getPackageStats();
                                        if (packageStats != null) {
                                        }
                                    }
                                } else {
                                    wifiTxPackets = wifiTxPackets3;
                                }
                            }
                            pw.print(prefix);
                            printWriter4 = pw;
                            printWriter4.print("    Wi-Fi network: ");
                            wifiRxBytes = wifiRxBytes4;
                            printWriter4.print(batteryStats3.formatBytesLocked(wifiRxBytes));
                            printWriter4.print(" received, ");
                            rawRealtime4 = rawRealtime29;
                            wifiTxBytes = wifiTxBytes2;
                            printWriter4.print(batteryStats3.formatBytesLocked(wifiTxBytes));
                            printWriter4.print(" sent (packets ");
                            printWriter4.print(wifiRxPackets2);
                            printWriter4.print(" received, ");
                            printWriter4.print(wifiTxPackets);
                            printWriter4.println(" sent)");
                            long wifiTxBytes32 = wifiTxBytes;
                            fullWifiLockOnTime = fullWifiLockOnTime2;
                            if (fullWifiLockOnTime == 0) {
                            }
                            u = u8;
                            sb5 = sb10;
                            sb5.setLength(0);
                            sb5.append(str5);
                            sb5.append("    Wifi Running: ");
                            formatTimeMs(sb5, uidWifiRunningTime3 / 1000);
                            sb5.append("(");
                            long whichBatteryRealtime132 = whichBatteryRealtime;
                            sb5.append(batteryStats3.formatRatioLocked(uidWifiRunningTime3, whichBatteryRealtime132));
                            sb5.append(")\n");
                            sb5.append(str5);
                            sb5.append("    Full Wifi Lock: ");
                            uidWifiRunningTime = uidWifiRunningTime3;
                            formatTimeMs(sb5, fullWifiLockOnTime / 1000);
                            sb5.append("(");
                            sb5.append(batteryStats3.formatRatioLocked(fullWifiLockOnTime, whichBatteryRealtime132));
                            sb5.append(")\n");
                            sb5.append(str5);
                            sb5.append("    Wifi Scan (blamed): ");
                            formatTimeMs(sb5, wifiRxPackets4 / 1000);
                            sb5.append("(");
                            sb5.append(batteryStats3.formatRatioLocked(wifiRxPackets4, whichBatteryRealtime132));
                            sb5.append(") ");
                            sb5.append(wifiScanCount);
                            sb5.append("x\n");
                            sb5.append(str5);
                            sb5.append("    Wifi Scan (actual): ");
                            formatTimeMs(sb5, wifiScanActualTime2 / 1000);
                            sb5.append("(");
                            whichBatteryRealtime3 = whichBatteryRealtime132;
                            uidWifiRunningTime2 = rawRealtime4;
                            sb5.append(batteryStats3.formatRatioLocked(wifiScanActualTime2, batteryStats3.computeBatteryRealtime(uidWifiRunningTime2, 0)));
                            sb5.append(") ");
                            sb5.append(wifiScanCount);
                            sb5.append("x\n");
                            sb5.append(str5);
                            sb5.append("    Background Wifi Scan: ");
                            wifiScanActualTime = wifiScanActualTime2;
                            wifiRxBytes3 = wifiRxBytes3;
                            formatTimeMs(sb5, wifiRxBytes3 / 1000);
                            sb5.append("(");
                            sb5.append(batteryStats3.formatRatioLocked(wifiRxBytes3, batteryStats3.computeBatteryRealtime(uidWifiRunningTime2, 0)));
                            sb5.append(") ");
                            sb5.append(wifiScanCountBg2);
                            sb5.append("x");
                            printWriter5 = pw;
                            printWriter5.println(sb5.toString());
                            long fullWifiLockOnTime322 = fullWifiLockOnTime;
                            wifiWakeup4 = wifiWakeup3;
                            if (wifiWakeup4 > 0) {
                            }
                            Uid u922 = u;
                            long wifiScanActualTimeBg222 = wifiRxBytes3;
                            long j3622 = wifiRxBytes2;
                            whichBatteryRealtime2 = whichBatteryRealtime3;
                            long wifiWakeup722 = wifiWakeup4;
                            u2 = u922;
                            PrintWriter printWriter822 = printWriter5;
                            long j3722 = wifiTxPackets2;
                            long wifiScanActualTime422 = wifiScanActualTime;
                            long j3822 = wifiRxPackets4;
                            long j3922 = wifiRxPackets3;
                            batteryStats3.printControllerActivityIfInteresting(printWriter5, sb5, str5 + "  ", WIFI_CONTROLLER_NAME, u922.getWifiControllerActivity(), which);
                            if (btRxBytes4 > 0) {
                            }
                            pw.print(prefix);
                            printWriter822.print("    Bluetooth network: ");
                            btRxBytes = btRxBytes4;
                            printWriter822.print(batteryStats3.formatBytesLocked(btRxBytes));
                            printWriter822.print(" received, ");
                            printWriter822.print(batteryStats3.formatBytesLocked(btTxBytes));
                            printWriter822.println(" sent");
                            bleTimer = u2.getBluetoothScanTimer();
                            if (bleTimer != null) {
                            }
                            if (u2.hasUserActivity()) {
                            }
                            wakelocks = u2.getWakelockStats();
                            int countWakelock2222 = 0;
                            if (wakelocks != null) {
                            }
                            if (countWakelock > 1) {
                            }
                            Uid u10222 = u3;
                            mcTimer = u10222.getMulticastWakelockStats();
                            if (mcTimer != null) {
                            }
                            syncs = u10222.getSyncStats();
                            if (syncs != null) {
                            }
                            long totalPartialWakelock3222 = totalFullWakelock;
                            long rawRealtimeMs11222 = rawRealtimeMs3;
                            jobs = u10222.getJobStats();
                            if (jobs != null) {
                            }
                            long rawRealtime32222 = rawRealtime7;
                            ArrayMap<String, SparseIntArray> completions222 = u10222.getJobCompletionStats();
                            while (ic >= 0) {
                            }
                            u10222.getDeferredJobsLineLocked(sb5, i4);
                            if (sb5.length() > 0) {
                            }
                            long j57222 = btRxBytes3;
                            long btRxBytes7222 = btTxBytes3;
                            long btTxBytes6222 = mobileRxPackets;
                            long j58222 = wifiTxBytes32;
                            PrintWriter printWriter9222 = printWriter6;
                            int uid4222 = uid3;
                            int i56222 = wifiScanCountBg3;
                            uidStats = uidStats9;
                            int i57222 = wifiScanCount2;
                            long rawRealtimeMs12222 = rawRealtimeMs11222;
                            Timer flashlightTurnedOnTimer222 = u10222.getFlashlightTurnedOnTimer();
                            long rawRealtimeMs13222 = rawRealtimeMs12222;
                            iu = iu5;
                            int NU11222 = NU10;
                            long j59222 = mobileRxBytes3;
                            long j60222 = wifiWakeup722;
                            long j61222 = totalPartialWakelock3222;
                            long rawRealtime33222 = rawRealtime32222;
                            long rawRealtimeMs14222 = rawRealtime33222;
                            ArrayMap<String, ? extends Timer> arrayMap822 = jobs;
                            ArrayMap<String, SparseIntArray> arrayMap2222 = completions222;
                            Uid u11222 = u10222;
                            long j62222 = uidWifiRunningTime;
                            long j63222 = mobileWakeup;
                            int i58222 = 0;
                            long uidWifiRunningTime6222 = wifiScanActualTimeBg222;
                            long wifiScanActualTimeBg3222 = uidMobileActiveTime3;
                            long uidMobileActiveTime4222 = wifiScanActualTime422;
                            long wifiScanActualTime5222 = fullWifiLockOnTime322;
                            int i59222 = i4;
                            long rawRealtime34222 = rawRealtime33222;
                            StringBuilder sb12222 = sb5;
                            String str6222 = str2;
                            cpuFreqs = cpuFreqs3;
                            int i60222 = uidMobileActiveCount2;
                            ArrayMap<String, ? extends Uid.Wakelock> arrayMap3222 = wakelocks2;
                            StringBuilder sb13222 = sb12222;
                            long j64222 = rawRealtime34222;
                            SparseArray<? extends Uid.Sensor> sensors2222 = u11222.getSensorStats();
                            NSE = sensors2222.size();
                            boolean uidActivity4222 = uidActivity3 | printTimer(printWriter9222, sb5, flashlightTurnedOnTimer222, rawRealtimeMs14222, i59222, str6222, "Flashlight") | printTimer(pw, sb13222, u11222.getCameraTurnedOnTimer(), j64222, i59222, str6222, "Camera") | printTimer(pw, sb13222, u11222.getVideoTurnedOnTimer(), j64222, i59222, str6222, "Video") | printTimer(pw, sb13222, u11222.getAudioTurnedOnTimer(), j64222, i59222, str6222, "Audio");
                            ise = 0;
                            while (ise < NSE) {
                            }
                            rawRealtimeMs = rawRealtimeMs13222;
                            NU3 = NU11222;
                            int i66222 = uid4222;
                            long rawRealtime36222 = rawRealtime34222;
                            StringBuilder sb15222 = sb12222;
                            printWriter2 = pw;
                            PrintWriter printWriter10222 = printWriter2;
                            StringBuilder sb16222 = sb15222;
                            long j67222 = rawRealtime36222;
                            sb3 = sb15222;
                            int i67222 = i4;
                            int NSE3222 = NSE;
                            str3 = prefix;
                            String str7222 = str3;
                            SparseArray<? extends Uid.Sensor> sensors3222 = sensors2222;
                            StringBuilder sb17222 = sb3;
                            boolean uidActivity5222 = uidActivity4222 | printTimer(printWriter10222, sb16222, u11222.getVibratorOnTimer(), j67222, i67222, str7222, "Vibrator") | printTimer(printWriter10222, sb17222, u11222.getForegroundActivityTimer(), j67222, i67222, str7222, "Foreground activities") | printTimer(printWriter2, sb17222, u11222.getForegroundServiceTimer(), j67222, i67222, str7222, "Foreground services");
                            totalStateTime = 0;
                            boolean uidActivity6222 = uidActivity5222;
                            ips = 0;
                            while (ips < 7) {
                            }
                            long rawRealtime38222 = rawRealtime36222;
                            if (totalStateTime > 0) {
                            }
                            userCpuTimeUs = u11222.getUserCpuTimeUs(i4);
                            systemCpuTimeUs = u11222.getSystemCpuTimeUs(i4);
                            if (userCpuTimeUs <= 0) {
                            }
                            sb3.setLength(0);
                            sb3.append(str3);
                            sb3.append("    Total cpu time: u=");
                            long j68222 = totalStateTime;
                            formatTimeMs(sb3, userCpuTimeUs / 1000);
                            sb3.append("s=");
                            formatTimeMs(sb3, systemCpuTimeUs / 1000);
                            printWriter2.println(sb3.toString());
                            cpuFreqTimes = u11222.getCpuFreqTimes(i4);
                            if (cpuFreqTimes != null) {
                            }
                            screenOffCpuFreqTimes = u11222.getScreenOffCpuFreqTimes(i4);
                            if (screenOffCpuFreqTimes != null) {
                            }
                            procState = 0;
                            while (procState < 7) {
                            }
                            processStats = u11222.getProcessStats();
                            if (processStats != null) {
                            }
                            Uid u12222 = u4;
                            packageStats = u12222.getPackageStats();
                            if (packageStats != null) {
                            }
                        }
                    }
                    wifiRxPackets = wifiRxPackets5;
                    printWriter3 = pw;
                    pw.print(prefix);
                    printWriter3.print("    Mobile network: ");
                    wifiWakeup = wifiWakeup5;
                    wifiWakeup2 = mobileTxBytes3;
                    batteryStats = this;
                    printWriter3.print(batteryStats.formatBytesLocked(mobileRxBytes2));
                    printWriter3.print(" received, ");
                    printWriter3.print(batteryStats.formatBytesLocked(wifiWakeup2));
                    printWriter3.print(" sent (packets ");
                    printWriter3.print(mobileRxPackets2);
                    printWriter3.print(" received, ");
                    printWriter3.print(mobileTxBytes);
                    printWriter3.println(" sent)");
                    long mobileRxBytes32 = mobileRxBytes2;
                    mobileRxBytes = uidMobileActiveTime2;
                    if (mobileRxBytes <= 0) {
                    }
                    mobileTxBytes2 = wifiWakeup2;
                    sb4 = sb9;
                    sb4.setLength(0);
                    sb4.append(prefix);
                    sb4.append("    Mobile radio active: ");
                    formatTimeMs(sb4, mobileRxBytes / 1000);
                    sb4.append("(");
                    wifiScanCountBg = wifiScanCount4;
                    long packets22 = mobileActiveTime5;
                    sb4.append(batteryStats.formatRatioLocked(mobileRxBytes, packets22));
                    sb4.append(") ");
                    sb4.append(uidMobileActiveCount);
                    sb4.append("x");
                    packets = mobileRxPackets2 + mobileTxBytes;
                    if (packets == 0) {
                    }
                    mobileActiveTime2 = packets22;
                    sb4.append(" @ ");
                    mobileTxPackets = mobileTxBytes;
                    mobileRxPackets = mobileRxPackets2;
                    sb4.append(BatteryStatsHelper.makemAh(((double) (mobileRxBytes / 1000)) / ((double) packets)));
                    sb4.append(" mspp");
                    printWriter3.println(sb4.toString());
                    if (uidWifiRunningTime5 <= 0) {
                    }
                    BatteryStats batteryStats32 = batteryStats;
                    StringBuilder sb102 = sb4;
                    Uid u82 = u7;
                    long mobileWakeup2 = uidWifiRunningTime5;
                    int uidMobileActiveCount22 = uidMobileActiveCount;
                    long rawRealtime292 = rawRealtime28;
                    long uidMobileActiveTime32 = mobileRxBytes;
                    long wifiWakeup62 = wifiWakeup;
                    long j342 = mobileTxBytes2;
                    mobileActiveTime = mobileActiveTime2;
                    String str52 = str;
                    rpmStats = rpmStats3;
                    boolean z52 = z;
                    mMemoryStats2 = mMemoryStats;
                    long j352 = mobileTxPackets;
                    batteryStats.printControllerActivityIfInteresting(printWriter3, sb102, str + "  ", CELLULAR_CONTROLLER_NAME, u7.getModemControllerActivity(), which);
                    if (wifiRxBytes4 <= 0) {
                    }
                    wifiTxPackets = wifiTxPackets3;
                    wifiRxPackets2 = wifiRxPackets;
                    pw.print(prefix);
                    printWriter4 = pw;
                    printWriter4.print("    Wi-Fi network: ");
                    wifiRxBytes = wifiRxBytes4;
                    printWriter4.print(batteryStats32.formatBytesLocked(wifiRxBytes));
                    printWriter4.print(" received, ");
                    rawRealtime4 = rawRealtime292;
                    wifiTxBytes = wifiTxBytes2;
                    printWriter4.print(batteryStats32.formatBytesLocked(wifiTxBytes));
                    printWriter4.print(" sent (packets ");
                    printWriter4.print(wifiRxPackets2);
                    printWriter4.print(" received, ");
                    printWriter4.print(wifiTxPackets);
                    printWriter4.println(" sent)");
                    long wifiTxBytes322 = wifiTxBytes;
                    fullWifiLockOnTime = fullWifiLockOnTime2;
                    if (fullWifiLockOnTime == 0) {
                    }
                    u = u82;
                    sb5 = sb102;
                    sb5.setLength(0);
                    sb5.append(str52);
                    sb5.append("    Wifi Running: ");
                    formatTimeMs(sb5, uidWifiRunningTime3 / 1000);
                    sb5.append("(");
                    long whichBatteryRealtime1322 = whichBatteryRealtime;
                    sb5.append(batteryStats32.formatRatioLocked(uidWifiRunningTime3, whichBatteryRealtime1322));
                    sb5.append(")\n");
                    sb5.append(str52);
                    sb5.append("    Full Wifi Lock: ");
                    uidWifiRunningTime = uidWifiRunningTime3;
                    formatTimeMs(sb5, fullWifiLockOnTime / 1000);
                    sb5.append("(");
                    sb5.append(batteryStats32.formatRatioLocked(fullWifiLockOnTime, whichBatteryRealtime1322));
                    sb5.append(")\n");
                    sb5.append(str52);
                    sb5.append("    Wifi Scan (blamed): ");
                    formatTimeMs(sb5, wifiRxPackets4 / 1000);
                    sb5.append("(");
                    sb5.append(batteryStats32.formatRatioLocked(wifiRxPackets4, whichBatteryRealtime1322));
                    sb5.append(") ");
                    sb5.append(wifiScanCount);
                    sb5.append("x\n");
                    sb5.append(str52);
                    sb5.append("    Wifi Scan (actual): ");
                    formatTimeMs(sb5, wifiScanActualTime2 / 1000);
                    sb5.append("(");
                    whichBatteryRealtime3 = whichBatteryRealtime1322;
                    uidWifiRunningTime2 = rawRealtime4;
                    sb5.append(batteryStats32.formatRatioLocked(wifiScanActualTime2, batteryStats32.computeBatteryRealtime(uidWifiRunningTime2, 0)));
                    sb5.append(") ");
                    sb5.append(wifiScanCount);
                    sb5.append("x\n");
                    sb5.append(str52);
                    sb5.append("    Background Wifi Scan: ");
                    wifiScanActualTime = wifiScanActualTime2;
                    wifiRxBytes3 = wifiRxBytes3;
                    formatTimeMs(sb5, wifiRxBytes3 / 1000);
                    sb5.append("(");
                    sb5.append(batteryStats32.formatRatioLocked(wifiRxBytes3, batteryStats32.computeBatteryRealtime(uidWifiRunningTime2, 0)));
                    sb5.append(") ");
                    sb5.append(wifiScanCountBg2);
                    sb5.append("x");
                    printWriter5 = pw;
                    printWriter5.println(sb5.toString());
                    long fullWifiLockOnTime3222 = fullWifiLockOnTime;
                    wifiWakeup4 = wifiWakeup3;
                    if (wifiWakeup4 > 0) {
                    }
                    Uid u9222 = u;
                    long wifiScanActualTimeBg2222 = wifiRxBytes3;
                    long j36222 = wifiRxBytes2;
                    whichBatteryRealtime2 = whichBatteryRealtime3;
                    long wifiWakeup7222 = wifiWakeup4;
                    u2 = u9222;
                    PrintWriter printWriter8222 = printWriter5;
                    long j37222 = wifiTxPackets2;
                    long wifiScanActualTime4222 = wifiScanActualTime;
                    long j38222 = wifiRxPackets4;
                    long j39222 = wifiRxPackets3;
                    batteryStats32.printControllerActivityIfInteresting(printWriter5, sb5, str52 + "  ", WIFI_CONTROLLER_NAME, u9222.getWifiControllerActivity(), which);
                    if (btRxBytes4 > 0) {
                    }
                    pw.print(prefix);
                    printWriter8222.print("    Bluetooth network: ");
                    btRxBytes = btRxBytes4;
                    printWriter8222.print(batteryStats32.formatBytesLocked(btRxBytes));
                    printWriter8222.print(" received, ");
                    printWriter8222.print(batteryStats32.formatBytesLocked(btTxBytes));
                    printWriter8222.println(" sent");
                    bleTimer = u2.getBluetoothScanTimer();
                    if (bleTimer != null) {
                    }
                    if (u2.hasUserActivity()) {
                    }
                    wakelocks = u2.getWakelockStats();
                    int countWakelock22222 = 0;
                    if (wakelocks != null) {
                    }
                    if (countWakelock > 1) {
                    }
                    Uid u102222 = u3;
                    mcTimer = u102222.getMulticastWakelockStats();
                    if (mcTimer != null) {
                    }
                    syncs = u102222.getSyncStats();
                    if (syncs != null) {
                    }
                    long totalPartialWakelock32222 = totalFullWakelock;
                    long rawRealtimeMs112222 = rawRealtimeMs3;
                    jobs = u102222.getJobStats();
                    if (jobs != null) {
                    }
                    long rawRealtime322222 = rawRealtime7;
                    ArrayMap<String, SparseIntArray> completions2222 = u102222.getJobCompletionStats();
                    while (ic >= 0) {
                    }
                    u102222.getDeferredJobsLineLocked(sb5, i4);
                    if (sb5.length() > 0) {
                    }
                    long j572222 = btRxBytes3;
                    long btRxBytes72222 = btTxBytes3;
                    long btTxBytes62222 = mobileRxPackets;
                    long j582222 = wifiTxBytes322;
                    PrintWriter printWriter92222 = printWriter6;
                    int uid42222 = uid3;
                    int i562222 = wifiScanCountBg3;
                    uidStats = uidStats9;
                    int i572222 = wifiScanCount2;
                    long rawRealtimeMs122222 = rawRealtimeMs112222;
                    Timer flashlightTurnedOnTimer2222 = u102222.getFlashlightTurnedOnTimer();
                    long rawRealtimeMs132222 = rawRealtimeMs122222;
                    iu = iu5;
                    int NU112222 = NU10;
                    long j592222 = mobileRxBytes32;
                    long j602222 = wifiWakeup7222;
                    long j612222 = totalPartialWakelock32222;
                    long rawRealtime332222 = rawRealtime322222;
                    long rawRealtimeMs142222 = rawRealtime332222;
                    ArrayMap<String, ? extends Timer> arrayMap8222 = jobs;
                    ArrayMap<String, SparseIntArray> arrayMap22222 = completions2222;
                    Uid u112222 = u102222;
                    long j622222 = uidWifiRunningTime;
                    long j632222 = mobileWakeup2;
                    int i582222 = 0;
                    long uidWifiRunningTime62222 = wifiScanActualTimeBg2222;
                    long wifiScanActualTimeBg32222 = uidMobileActiveTime32;
                    long uidMobileActiveTime42222 = wifiScanActualTime4222;
                    long wifiScanActualTime52222 = fullWifiLockOnTime3222;
                    int i592222 = i4;
                    long rawRealtime342222 = rawRealtime332222;
                    StringBuilder sb122222 = sb5;
                    String str62222 = str2;
                    cpuFreqs = cpuFreqs3;
                    int i602222 = uidMobileActiveCount22;
                    ArrayMap<String, ? extends Uid.Wakelock> arrayMap32222 = wakelocks2;
                    StringBuilder sb132222 = sb122222;
                    long j642222 = rawRealtime342222;
                    SparseArray<? extends Uid.Sensor> sensors22222 = u112222.getSensorStats();
                    NSE = sensors22222.size();
                    boolean uidActivity42222 = uidActivity3 | printTimer(printWriter92222, sb5, flashlightTurnedOnTimer2222, rawRealtimeMs142222, i592222, str62222, "Flashlight") | printTimer(pw, sb132222, u112222.getCameraTurnedOnTimer(), j642222, i592222, str62222, "Camera") | printTimer(pw, sb132222, u112222.getVideoTurnedOnTimer(), j642222, i592222, str62222, "Video") | printTimer(pw, sb132222, u112222.getAudioTurnedOnTimer(), j642222, i592222, str62222, "Audio");
                    ise = 0;
                    while (ise < NSE) {
                    }
                    rawRealtimeMs = rawRealtimeMs132222;
                    NU3 = NU112222;
                    int i662222 = uid42222;
                    long rawRealtime362222 = rawRealtime342222;
                    StringBuilder sb152222 = sb122222;
                    printWriter2 = pw;
                    PrintWriter printWriter102222 = printWriter2;
                    StringBuilder sb162222 = sb152222;
                    long j672222 = rawRealtime362222;
                    sb3 = sb152222;
                    int i672222 = i4;
                    int NSE32222 = NSE;
                    str3 = prefix;
                    String str72222 = str3;
                    SparseArray<? extends Uid.Sensor> sensors32222 = sensors22222;
                    StringBuilder sb172222 = sb3;
                    boolean uidActivity52222 = uidActivity42222 | printTimer(printWriter102222, sb162222, u112222.getVibratorOnTimer(), j672222, i672222, str72222, "Vibrator") | printTimer(printWriter102222, sb172222, u112222.getForegroundActivityTimer(), j672222, i672222, str72222, "Foreground activities") | printTimer(printWriter2, sb172222, u112222.getForegroundServiceTimer(), j672222, i672222, str72222, "Foreground services");
                    totalStateTime = 0;
                    boolean uidActivity62222 = uidActivity52222;
                    ips = 0;
                    while (ips < 7) {
                    }
                    long rawRealtime382222 = rawRealtime362222;
                    if (totalStateTime > 0) {
                    }
                    userCpuTimeUs = u112222.getUserCpuTimeUs(i4);
                    systemCpuTimeUs = u112222.getSystemCpuTimeUs(i4);
                    if (userCpuTimeUs <= 0) {
                    }
                    sb3.setLength(0);
                    sb3.append(str3);
                    sb3.append("    Total cpu time: u=");
                    long j682222 = totalStateTime;
                    formatTimeMs(sb3, userCpuTimeUs / 1000);
                    sb3.append("s=");
                    formatTimeMs(sb3, systemCpuTimeUs / 1000);
                    printWriter2.println(sb3.toString());
                    cpuFreqTimes = u112222.getCpuFreqTimes(i4);
                    if (cpuFreqTimes != null) {
                    }
                    screenOffCpuFreqTimes = u112222.getScreenOffCpuFreqTimes(i4);
                    if (screenOffCpuFreqTimes != null) {
                    }
                    procState = 0;
                    while (procState < 7) {
                    }
                    processStats = u112222.getProcessStats();
                    if (processStats != null) {
                    }
                    Uid u122222 = u4;
                    packageStats = u122222.getPackageStats();
                    if (packageStats != null) {
                    }
                } else {
                    rawRealtime3 = rawRealtime2;
                    rpmStats = rpmStats2;
                    uidStats = uidStats8;
                    iu = iu4;
                    NU3 = NU9;
                    printWriter2 = printWriter;
                    sb3 = sb8;
                    cpuFreqs = cpuFreqs2;
                    batteryUptime = batteryUptime4;
                    rawRealtimeMs = rawRealtimeMs8;
                    mobileActiveTime = mobileActiveTime5;
                    mMemoryStats2 = mMemoryStats;
                    whichBatteryRealtime2 = whichBatteryRealtime;
                    String str8 = prefix;
                    int NU12 = which;
                }
                iu3 = iu + 1;
                printWriter = printWriter2;
                batteryUptime4 = batteryUptime;
                mobileActiveTime5 = mobileActiveTime;
                rpmStats2 = rpmStats;
                mMemoryStats = mMemoryStats2;
                whichBatteryRealtime = whichBatteryRealtime2;
                uidStats7 = uidStats;
                cpuFreqs2 = cpuFreqs;
                NU8 = NU3;
                rawRealtimeMs8 = rawRealtimeMs;
                i2 = 0;
                sb8 = sb3;
                rawRealtime2 = rawRealtime3;
            } else {
                Map<String, ? extends Timer> map2 = rpmStats2;
                int i79 = NU9;
                PrintWriter printWriter11 = printWriter;
                StringBuilder sb18 = sb8;
                long[] jArr3 = cpuFreqs2;
                long j71 = batteryUptime4;
                long j72 = rawRealtimeMs8;
                long j73 = mobileActiveTime5;
                SparseArray<? extends Uid> sparseArray = uidStats7;
                LongSparseArray<? extends Timer> longSparseArray = mMemoryStats;
                long j74 = whichBatteryRealtime;
                String str9 = prefix;
                int NU13 = which;
                return;
            }
        }
    }

    static void printBitDescriptions(StringBuilder sb, int oldval, int newval, HistoryTag wakelockTag, BitDescription[] descriptions, boolean longNames) {
        int diff = oldval ^ newval;
        if (diff != 0) {
            boolean didWake = false;
            for (BitDescription bd : descriptions) {
                if ((bd.mask & diff) != 0) {
                    sb.append(longNames ? WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER : ",");
                    if (bd.shift < 0) {
                        sb.append((bd.mask & newval) != 0 ? "+" : "-");
                        sb.append(longNames ? bd.name : bd.shortName);
                        if (bd.mask == 1073741824 && wakelockTag != null) {
                            didWake = true;
                            sb.append("=");
                            if (longNames) {
                                UserHandle.formatUid(sb, wakelockTag.uid);
                                sb.append(":\"");
                                sb.append(wakelockTag.string);
                                sb.append("\"");
                            } else {
                                sb.append(wakelockTag.poolIdx);
                            }
                        }
                    } else {
                        sb.append(longNames ? bd.name : bd.shortName);
                        sb.append("=");
                        int val = (bd.mask & newval) >> bd.shift;
                        if (bd.values == null || val < 0 || val >= bd.values.length) {
                            sb.append(val);
                        } else {
                            sb.append(longNames ? bd.values[val] : bd.shortValues[val]);
                        }
                    }
                }
            }
            if (!didWake && wakelockTag != null) {
                sb.append(longNames ? " wake_lock=" : ",w=");
                if (longNames) {
                    UserHandle.formatUid(sb, wakelockTag.uid);
                    sb.append(":\"");
                    sb.append(wakelockTag.string);
                    sb.append("\"");
                } else {
                    sb.append(wakelockTag.poolIdx);
                }
            }
        }
    }

    public void prepareForDumpLocked() {
    }

    private void printSizeValue(PrintWriter pw, long size) {
        float result = (float) size;
        String suffix = "";
        if (result >= 10240.0f) {
            suffix = "KB";
            result /= 1024.0f;
        }
        if (result >= 10240.0f) {
            suffix = "MB";
            result /= 1024.0f;
        }
        if (result >= 10240.0f) {
            suffix = "GB";
            result /= 1024.0f;
        }
        if (result >= 10240.0f) {
            suffix = "TB";
            result /= 1024.0f;
        }
        if (result >= 10240.0f) {
            suffix = "PB";
            result /= 1024.0f;
        }
        pw.print((int) result);
        pw.print(suffix);
    }

    private static boolean dumpTimeEstimate(PrintWriter pw, String label1, String label2, String label3, long estimatedTime) {
        if (estimatedTime < 0) {
            return false;
        }
        pw.print(label1);
        pw.print(label2);
        pw.print(label3);
        StringBuilder sb = new StringBuilder(64);
        formatTimeMs(sb, estimatedTime);
        pw.print(sb);
        pw.println();
        return true;
    }

    private static boolean dumpDurationSteps(PrintWriter pw, String prefix, String header, LevelStepTracker steps, boolean checkin) {
        int count;
        PrintWriter printWriter = pw;
        String str = header;
        LevelStepTracker levelStepTracker = steps;
        char c = 0;
        if (levelStepTracker == null) {
            return false;
        }
        int count2 = levelStepTracker.mNumStepDurations;
        if (count2 <= 0) {
            return false;
        }
        if (!checkin) {
            printWriter.println(str);
        }
        String[] lineArgs = new String[5];
        int i = 0;
        while (i < count2) {
            long duration = levelStepTracker.getDurationAt(i);
            int level = levelStepTracker.getLevelAt(i);
            long initMode = (long) levelStepTracker.getInitModeAt(i);
            long modMode = (long) levelStepTracker.getModModeAt(i);
            if (checkin) {
                lineArgs[c] = Long.toString(duration);
                lineArgs[1] = Integer.toString(level);
                if ((modMode & 3) == 0) {
                    count = count2;
                    switch (((int) (initMode & 3)) + 1) {
                        case 1:
                            lineArgs[2] = "s-";
                            break;
                        case 2:
                            lineArgs[2] = "s+";
                            break;
                        case 3:
                            lineArgs[2] = "sd";
                            break;
                        case 4:
                            lineArgs[2] = "sds";
                            break;
                        default:
                            lineArgs[2] = "?";
                            break;
                    }
                } else {
                    count = count2;
                    lineArgs[2] = "";
                }
                if ((modMode & 4) == 0) {
                    lineArgs[3] = (initMode & 4) != 0 ? "p+" : "p-";
                } else {
                    lineArgs[3] = "";
                }
                if ((modMode & 8) == 0) {
                    lineArgs[4] = (8 & initMode) != 0 ? "i+" : "i-";
                } else {
                    lineArgs[4] = "";
                }
                dumpLine(printWriter, 0, "i", str, (Object[]) lineArgs);
            } else {
                count = count2;
                pw.print(prefix);
                printWriter.print("#");
                printWriter.print(i);
                printWriter.print(": ");
                TimeUtils.formatDuration(duration, printWriter);
                printWriter.print(" to ");
                printWriter.print(level);
                boolean haveModes = false;
                if ((modMode & 3) == 0) {
                    printWriter.print(" (");
                    switch (((int) (initMode & 3)) + 1) {
                        case 1:
                            printWriter.print("screen-off");
                            break;
                        case 2:
                            printWriter.print("screen-on");
                            break;
                        case 3:
                            printWriter.print("screen-doze");
                            break;
                        case 4:
                            printWriter.print("screen-doze-suspend");
                            break;
                        default:
                            printWriter.print("screen-?");
                            break;
                    }
                    haveModes = true;
                }
                if ((modMode & 4) == 0) {
                    printWriter.print(haveModes ? ", " : " (");
                    printWriter.print((initMode & 4) != 0 ? "power-save-on" : "power-save-off");
                    haveModes = true;
                }
                if ((modMode & 8) == 0) {
                    printWriter.print(haveModes ? ", " : " (");
                    printWriter.print((initMode & 8) != 0 ? "device-idle-on" : "device-idle-off");
                    haveModes = true;
                }
                if (haveModes) {
                    printWriter.print(")");
                }
                pw.println();
            }
            i++;
            count2 = count;
            str = header;
            levelStepTracker = steps;
            c = 0;
        }
        return true;
    }

    private static void dumpDurationSteps(ProtoOutputStream proto, long fieldId, LevelStepTracker steps) {
        ProtoOutputStream protoOutputStream = proto;
        LevelStepTracker levelStepTracker = steps;
        if (levelStepTracker != null) {
            int count = levelStepTracker.mNumStepDurations;
            for (int i = 0; i < count; i++) {
                long token = proto.start(fieldId);
                protoOutputStream.write(1112396529665L, levelStepTracker.getDurationAt(i));
                protoOutputStream.write(1120986464258L, levelStepTracker.getLevelAt(i));
                long initMode = (long) levelStepTracker.getInitModeAt(i);
                long modMode = (long) levelStepTracker.getModModeAt(i);
                int ds = 0;
                int i2 = 1;
                if ((modMode & 3) == 0) {
                    switch (((int) (3 & initMode)) + 1) {
                        case 1:
                            ds = 2;
                            break;
                        case 2:
                            ds = 1;
                            break;
                        case 3:
                            ds = 3;
                            break;
                        case 4:
                            ds = 4;
                            break;
                        default:
                            ds = 5;
                            break;
                    }
                }
                protoOutputStream.write(1159641169923L, ds);
                int psm = 0;
                int i3 = 2;
                if ((modMode & 4) == 0) {
                    if ((4 & initMode) == 0) {
                        i2 = 2;
                    }
                    psm = i2;
                }
                protoOutputStream.write(1159641169924L, psm);
                int im = 0;
                if ((modMode & 8) == 0) {
                    if ((8 & initMode) == 0) {
                        i3 = 3;
                    }
                    im = i3;
                }
                protoOutputStream.write(1159641169925L, im);
                protoOutputStream.end(token);
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v2, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r20v7, resolved type: boolean} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x009a  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0183  */
    private void dumpHistoryLocked(PrintWriter pw, int flags, long histStart, boolean checkin) {
        HistoryEventTracker tracker;
        HistoryEventTracker tracker2;
        boolean printed;
        long lastTime;
        HistoryEventTracker tracker3;
        boolean printed2;
        PrintWriter printWriter = pw;
        HistoryPrinter hprinter = new HistoryPrinter();
        HistoryItem rec = new HistoryItem();
        boolean printed3 = false;
        long baseTime = -1;
        long lastTime2 = -1;
        HistoryEventTracker tracker4 = null;
        while (true) {
            tracker = tracker4;
            if (!getNextHistoryLocked(rec)) {
                break;
            }
            lastTime2 = rec.time;
            if (baseTime < 0) {
                baseTime = lastTime2;
            }
            long baseTime2 = baseTime;
            if (rec.time >= histStart) {
                if (histStart < 0 || printed3) {
                    lastTime = lastTime2;
                    tracker2 = tracker;
                    printed = printed3;
                } else {
                    if (rec.cmd == 5 || rec.cmd == 7 || rec.cmd == 4) {
                        lastTime = lastTime2;
                        tracker3 = tracker;
                    } else if (rec.cmd == 8) {
                        lastTime = lastTime2;
                        tracker3 = tracker;
                    } else {
                        tracker3 = tracker;
                        if (rec.currentTime != 0) {
                            byte cmd = rec.cmd;
                            rec.cmd = 5;
                            lastTime = lastTime2;
                            hprinter.printNextItem(printWriter, rec, baseTime2, checkin, (flags & 32) != 0);
                            rec.cmd = cmd;
                            printed = true;
                            printed2 = false;
                        } else {
                            lastTime = lastTime2;
                            printed = printed3;
                            printed2 = false;
                        }
                        if (tracker3 == null) {
                            if (rec.cmd != 0) {
                                hprinter.printNextItem(printWriter, rec, baseTime2, checkin, (flags & 32) != 0 ? true : printed2);
                                rec.cmd = printed2;
                            }
                            int oldEventCode = rec.eventCode;
                            HistoryTag oldEventTag = rec.eventTag;
                            rec.eventTag = new HistoryTag();
                            int i = printed2;
                            while (true) {
                                int i2 = i;
                                if (i2 >= 22) {
                                    break;
                                }
                                HistoryEventTracker tracker5 = tracker3;
                                HashMap<String, SparseIntArray> active = tracker5.getStateForEvent(i2);
                                if (active != null) {
                                    Iterator<Map.Entry<String, SparseIntArray>> it = active.entrySet().iterator();
                                    while (it.hasNext()) {
                                        Map.Entry<String, SparseIntArray> ent = it.next();
                                        SparseIntArray uids = ent.getValue();
                                        int j = printed2;
                                        while (true) {
                                            int j2 = j;
                                            if (j2 >= uids.size()) {
                                                break;
                                            }
                                            rec.eventCode = i2;
                                            Map.Entry<String, SparseIntArray> ent2 = ent;
                                            rec.eventTag.string = ent.getKey();
                                            rec.eventTag.uid = uids.keyAt(j2);
                                            rec.eventTag.poolIdx = uids.valueAt(j2);
                                            SparseIntArray uids2 = uids;
                                            hprinter.printNextItem(printWriter, rec, baseTime2, checkin, (flags & 32) != 0);
                                            rec.wakeReasonTag = null;
                                            rec.wakelockTag = null;
                                            int j3 = j2 + 1;
                                            oldEventTag = oldEventTag;
                                            ent = ent2;
                                            it = it;
                                            tracker5 = tracker5;
                                            active = active;
                                            i2 = i2;
                                            SparseIntArray sparseIntArray = uids2;
                                            j = j3;
                                            uids = sparseIntArray;
                                        }
                                        Iterator<Map.Entry<String, SparseIntArray>> it2 = it;
                                        HistoryEventTracker historyEventTracker = tracker5;
                                        HashMap<String, SparseIntArray> hashMap = active;
                                        int i3 = i2;
                                        HistoryTag historyTag = oldEventTag;
                                        printed2 = false;
                                    }
                                }
                                i = i2 + 1;
                                oldEventTag = oldEventTag;
                                tracker3 = tracker5;
                                printed2 = false;
                            }
                            HistoryEventTracker historyEventTracker2 = tracker3;
                            rec.eventCode = oldEventCode;
                            rec.eventTag = oldEventTag;
                            tracker2 = null;
                        } else {
                            tracker2 = tracker3;
                        }
                    }
                    printed = true;
                    printed2 = false;
                    hprinter.printNextItem(printWriter, rec, baseTime2, checkin, (flags & 32) != 0);
                    rec.cmd = 0;
                    if (tracker3 == null) {
                    }
                }
                baseTime = baseTime2;
                hprinter.printNextItem(printWriter, rec, baseTime, checkin, (flags & 32) != 0);
                lastTime2 = lastTime;
                printed3 = printed;
                tracker4 = tracker2;
            } else {
                baseTime = baseTime2;
                tracker4 = tracker;
            }
        }
        if (histStart >= 0) {
            commitCurrentHistoryBatchLocked();
            printWriter.print(checkin ? "NEXT: " : "  NEXT: ");
            printWriter.println(1 + lastTime2);
        }
    }

    private void dumpDailyLevelStepSummary(PrintWriter pw, String prefix, String label, LevelStepTracker steps, StringBuilder tmpSb, int[] tmpOutInt) {
        PrintWriter printWriter = pw;
        String str = label;
        StringBuilder sb = tmpSb;
        if (steps != null) {
            long timeRemaining = steps.computeTimeEstimate(0, 0, tmpOutInt);
            if (timeRemaining >= 0) {
                pw.print(prefix);
                printWriter.print(str);
                printWriter.print(" total time: ");
                sb.setLength(0);
                formatTimeMs(sb, timeRemaining);
                printWriter.print(sb);
                printWriter.print(" (from ");
                printWriter.print(tmpOutInt[0]);
                printWriter.println(" steps)");
            }
            int i = 0;
            while (true) {
                int i2 = i;
                if (i2 < STEP_LEVEL_MODES_OF_INTEREST.length) {
                    int i3 = i2;
                    long estimatedTime = steps.computeTimeEstimate((long) STEP_LEVEL_MODES_OF_INTEREST[i2], (long) STEP_LEVEL_MODE_VALUES[i2], tmpOutInt);
                    if (estimatedTime > 0) {
                        pw.print(prefix);
                        printWriter.print(str);
                        printWriter.print(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                        printWriter.print(STEP_LEVEL_MODE_LABELS[i3]);
                        printWriter.print(" time: ");
                        sb.setLength(0);
                        formatTimeMs(sb, estimatedTime);
                        printWriter.print(sb);
                        printWriter.print(" (from ");
                        printWriter.print(tmpOutInt[0]);
                        printWriter.println(" steps)");
                    }
                    i = i3 + 1;
                } else {
                    return;
                }
            }
        }
    }

    private void dumpDailyPackageChanges(PrintWriter pw, String prefix, ArrayList<PackageChange> changes) {
        if (changes != null) {
            pw.print(prefix);
            pw.println("Package changes:");
            for (int i = 0; i < changes.size(); i++) {
                PackageChange pc = changes.get(i);
                if (pc.mUpdate) {
                    pw.print(prefix);
                    pw.print("  Update ");
                    pw.print(pc.mPackageName);
                    pw.print(" vers=");
                    pw.println(pc.mVersionCode);
                } else {
                    pw.print(prefix);
                    pw.print("  Uninstall ");
                    pw.println(pc.mPackageName);
                }
            }
        }
    }

    /* JADX WARNING: Multi-variable type inference failed */
    public void dumpLocked(Context context, PrintWriter pw, int flags, int reqUid, long histStart) {
        boolean z;
        boolean z2;
        int[] outInt;
        DailyItem dit;
        DailyItem dit2;
        ArrayList<PackageChange> pkgc;
        LevelStepTracker dsteps;
        LevelStepTracker csteps;
        PrintWriter printWriter = pw;
        prepareForDumpLocked();
        boolean filtering = (flags & 14) != 0;
        if ((flags & 8) != 0 || !filtering) {
            long historyTotalSize = (long) getHistoryTotalSize();
            long historyUsedSize = (long) getHistoryUsedSize();
            if (startIteratingHistoryLocked()) {
                try {
                    printWriter.print("Battery History (");
                    printWriter.print((100 * historyUsedSize) / historyTotalSize);
                    printWriter.print("% used, ");
                    printSizeValue(printWriter, historyUsedSize);
                    printWriter.print(" used of ");
                    printSizeValue(printWriter, historyTotalSize);
                    printWriter.print(", ");
                    printWriter.print(getHistoryStringPoolSize());
                    printWriter.print(" strings using ");
                    printSizeValue(printWriter, (long) getHistoryStringPoolBytes());
                    printWriter.println("):");
                    long j = historyUsedSize;
                    try {
                        dumpHistoryLocked(printWriter, flags, histStart, false);
                        pw.println();
                        finishIteratingHistoryLocked();
                    } catch (Throwable th) {
                        th = th;
                        finishIteratingHistoryLocked();
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    long j2 = historyUsedSize;
                    finishIteratingHistoryLocked();
                    throw th;
                }
            }
            if (startIteratingOldHistoryLocked()) {
                try {
                    HistoryItem rec = new HistoryItem();
                    printWriter.println("Old battery History:");
                    HistoryPrinter hprinter = new HistoryPrinter();
                    long baseTime = -1;
                    while (getNextOldHistoryLocked(rec)) {
                        if (baseTime < 0) {
                            baseTime = rec.time;
                        }
                        long baseTime2 = baseTime;
                        hprinter.printNextItem(printWriter, rec, baseTime2, false, (flags & 32) != 0);
                        baseTime = baseTime2;
                    }
                    pw.println();
                } finally {
                    finishIteratingOldHistoryLocked();
                }
            }
        }
        if (!filtering || (flags & 6) != 0) {
            if (!filtering) {
                SparseArray<? extends Uid> uidStats = getUidStats();
                int NU = uidStats.size();
                long nowRealtime = SystemClock.elapsedRealtime();
                boolean j3 = false;
                for (int i = 0; i < NU; i++) {
                    SparseArray<? extends Uid.Pid> pids = ((Uid) uidStats.valueAt(i)).getPidStats();
                    if (pids != null) {
                        boolean didPid = j3;
                        for (int j4 = 0; j4 < pids.size(); j4++) {
                            Uid.Pid pid = (Uid.Pid) pids.valueAt(j4);
                            if (!didPid) {
                                printWriter.println("Per-PID Stats:");
                                didPid = true;
                            }
                            long j5 = pid.mWakeSumMs;
                            long j6 = pid.mWakeNesting > 0 ? nowRealtime - pid.mWakeStartMs : 0;
                            printWriter.print("  PID ");
                            printWriter.print(pids.keyAt(j4));
                            printWriter.print(" wake time: ");
                            TimeUtils.formatDuration(j5 + j6, printWriter);
                            printWriter.println("");
                        }
                        j3 = didPid;
                    }
                }
                if (j3) {
                    pw.println();
                }
            }
            if (!filtering || (flags & 2) != 0) {
                if (dumpDurationSteps(printWriter, "  ", "Discharge step durations:", getDischargeLevelStepTracker(), false)) {
                    long timeRemaining = computeBatteryTimeRemaining(SystemClock.elapsedRealtime() * 1000);
                    if (timeRemaining >= 0) {
                        printWriter.print("  Estimated discharge time remaining: ");
                        TimeUtils.formatDuration(timeRemaining / 1000, printWriter);
                        pw.println();
                    }
                    LevelStepTracker steps = getDischargeLevelStepTracker();
                    int i2 = 0;
                    while (true) {
                        int i3 = i2;
                        if (i3 >= STEP_LEVEL_MODES_OF_INTEREST.length) {
                            break;
                        }
                        dumpTimeEstimate(printWriter, "  Estimated ", STEP_LEVEL_MODE_LABELS[i3], " time: ", steps.computeTimeEstimate((long) STEP_LEVEL_MODES_OF_INTEREST[i3], (long) STEP_LEVEL_MODE_VALUES[i3], null));
                        i2 = i3 + 1;
                    }
                    pw.println();
                }
                z = false;
                if (dumpDurationSteps(printWriter, "  ", "Charge step durations:", getChargeLevelStepTracker(), false)) {
                    long timeRemaining2 = computeChargeTimeRemaining(SystemClock.elapsedRealtime() * 1000);
                    if (timeRemaining2 >= 0) {
                        printWriter.print("  Estimated charge time remaining: ");
                        TimeUtils.formatDuration(timeRemaining2 / 1000, printWriter);
                        pw.println();
                    }
                    pw.println();
                }
            } else {
                z = false;
            }
            if (!filtering || (flags & 4) != 0) {
                printWriter.println("Daily stats:");
                printWriter.print("  Current start time: ");
                printWriter.println(DateFormat.format((CharSequence) "yyyy-MM-dd-HH-mm-ss", getCurrentDailyStartTime()).toString());
                printWriter.print("  Next min deadline: ");
                printWriter.println(DateFormat.format((CharSequence) "yyyy-MM-dd-HH-mm-ss", getNextMinDailyDeadline()).toString());
                printWriter.print("  Next max deadline: ");
                printWriter.println(DateFormat.format((CharSequence) "yyyy-MM-dd-HH-mm-ss", getNextMaxDailyDeadline()).toString());
                StringBuilder sb = new StringBuilder(64);
                int[] outInt2 = new int[1];
                LevelStepTracker dsteps2 = getDailyDischargeLevelStepTracker();
                LevelStepTracker csteps2 = getDailyChargeLevelStepTracker();
                ArrayList<PackageChange> pkgc2 = getDailyPackageChanges();
                if (dsteps2.mNumStepDurations > 0 || csteps2.mNumStepDurations > 0 || pkgc2 != null) {
                    if ((flags & 4) != 0) {
                        pkgc = pkgc2;
                        csteps = csteps2;
                        dsteps = dsteps2;
                        z2 = z;
                        outInt = outInt2;
                    } else if (!filtering) {
                        pkgc = pkgc2;
                        csteps = csteps2;
                        dsteps = dsteps2;
                        z2 = z;
                        outInt = outInt2;
                    } else {
                        printWriter.println("  Current daily steps:");
                        dumpDailyLevelStepSummary(printWriter, "    ", "Discharge", dsteps2, sb, outInt2);
                        ArrayList<PackageChange> arrayList = pkgc2;
                        LevelStepTracker levelStepTracker = dsteps2;
                        z2 = z;
                        outInt = outInt2;
                        dumpDailyLevelStepSummary(printWriter, "    ", "Charge", csteps2, sb, outInt2);
                    }
                    if (dumpDurationSteps(printWriter, "    ", "  Current daily discharge step durations:", dsteps, z2)) {
                        dumpDailyLevelStepSummary(printWriter, "      ", "Discharge", dsteps, sb, outInt);
                    }
                    if (dumpDurationSteps(printWriter, "    ", "  Current daily charge step durations:", csteps, z2)) {
                        dumpDailyLevelStepSummary(printWriter, "      ", "Charge", csteps, sb, outInt);
                    }
                    dumpDailyPackageChanges(printWriter, "    ", pkgc);
                } else {
                    ArrayList<PackageChange> arrayList2 = pkgc2;
                    LevelStepTracker levelStepTracker2 = csteps2;
                    LevelStepTracker levelStepTracker3 = dsteps2;
                    z2 = z;
                    outInt = outInt2;
                }
                int curIndex = z2;
                while (true) {
                    DailyItem dailyItemLocked = getDailyItemLocked(curIndex);
                    dit = dailyItemLocked;
                    if (dailyItemLocked == null) {
                        break;
                    }
                    int curIndex2 = curIndex + 1;
                    if ((flags & 4) != 0) {
                        pw.println();
                    }
                    printWriter.print("  Daily from ");
                    printWriter.print(DateFormat.format((CharSequence) "yyyy-MM-dd-HH-mm-ss", dit.mStartTime).toString());
                    printWriter.print(" to ");
                    printWriter.print(DateFormat.format((CharSequence) "yyyy-MM-dd-HH-mm-ss", dit.mEndTime).toString());
                    printWriter.println(SettingsStringUtil.DELIMITER);
                    if ((flags & 4) != 0) {
                        dit2 = dit;
                    } else if (!filtering) {
                        dit2 = dit;
                    } else {
                        LevelStepTracker levelStepTracker4 = dit.mDischargeSteps;
                        PrintWriter printWriter2 = printWriter;
                        StringBuilder sb2 = sb;
                        DailyItem dit3 = dit;
                        int[] iArr = outInt;
                        dumpDailyLevelStepSummary(printWriter2, "    ", "Discharge", levelStepTracker4, sb2, iArr);
                        dumpDailyLevelStepSummary(printWriter2, "    ", "Charge", dit3.mChargeSteps, sb2, iArr);
                        curIndex = curIndex2;
                    }
                    if (dumpDurationSteps(printWriter, "      ", "    Discharge step durations:", dit2.mDischargeSteps, z2)) {
                        dumpDailyLevelStepSummary(printWriter, "        ", "Discharge", dit2.mDischargeSteps, sb, outInt);
                    }
                    if (dumpDurationSteps(printWriter, "      ", "    Charge step durations:", dit2.mChargeSteps, z2)) {
                        dumpDailyLevelStepSummary(printWriter, "        ", "Charge", dit2.mChargeSteps, sb, outInt);
                    }
                    dumpDailyPackageChanges(printWriter, "    ", dit2.mPackageChanges);
                    curIndex = curIndex2;
                }
                DailyItem dailyItem = dit;
                pw.println();
            } else {
                z2 = z;
            }
            if (!filtering || (flags & 2) != 0) {
                printWriter.println("Statistics since last charge:");
                printWriter.println("  System starts: " + getStartCount() + ", currently on battery: " + getIsOnBattery());
                dumpLocked(context, printWriter, "", 0, reqUid, (flags & 64) != 0 ? true : z2);
                pw.println();
            }
        }
    }

    public void dumpCheckinLocked(Context context, PrintWriter pw, List<ApplicationInfo> apps, int flags, long histStart) {
        PrintWriter printWriter = pw;
        List<ApplicationInfo> list = apps;
        prepareForDumpLocked();
        boolean z = false;
        boolean z2 = true;
        dumpLine(printWriter, 0, "i", VERSION_DATA, 32, Integer.valueOf(getParcelVersion()), getStartPlatformVersion(), getEndPlatformVersion());
        long historyBaseTime = getHistoryBaseTime() + SystemClock.elapsedRealtime();
        if ((flags & 24) != 0 && startIteratingHistoryLocked()) {
            int i = 0;
            while (i < getHistoryStringPoolSize()) {
                try {
                    printWriter.print(9);
                    printWriter.print(',');
                    printWriter.print(HISTORY_STRING_POOL);
                    printWriter.print(',');
                    printWriter.print(i);
                    printWriter.print(",");
                    printWriter.print(getHistoryTagPoolUid(i));
                    printWriter.print(",\"");
                    printWriter.print(getHistoryTagPoolString(i).replace("\\", "\\\\").replace("\"", "\\\""));
                    printWriter.print("\"");
                    pw.println();
                    i++;
                } finally {
                    finishIteratingHistoryLocked();
                }
            }
            dumpHistoryLocked(printWriter, flags, histStart, true);
        }
        if ((flags & 8) == 0) {
            if (list != null) {
                SparseArray<Pair<ArrayList<String>, MutableBoolean>> uids = new SparseArray<>();
                for (int i2 = 0; i2 < apps.size(); i2++) {
                    ApplicationInfo ai = list.get(i2);
                    Pair<ArrayList<String>, MutableBoolean> pkgs = uids.get(UserHandle.getAppId(ai.uid));
                    if (pkgs == null) {
                        pkgs = new Pair<>(new ArrayList(), new MutableBoolean(false));
                        uids.put(UserHandle.getAppId(ai.uid), pkgs);
                    }
                    ((ArrayList) pkgs.first).add(ai.packageName);
                }
                SparseArray<? extends Uid> uidStats = getUidStats();
                int NU = uidStats.size();
                String[] lineArgs = new String[2];
                int i3 = 0;
                while (i3 < NU) {
                    int uid = UserHandle.getAppId(uidStats.keyAt(i3));
                    Pair<ArrayList<String>, MutableBoolean> pkgs2 = uids.get(uid);
                    if (pkgs2 != null && !((MutableBoolean) pkgs2.second).value) {
                        ((MutableBoolean) pkgs2.second).value = z2;
                        int j = 0;
                        while (j < ((ArrayList) pkgs2.first).size()) {
                            lineArgs[0] = Integer.toString(uid);
                            lineArgs[1] = (String) ((ArrayList) pkgs2.first).get(j);
                            dumpLine(printWriter, 0, "i", "uid", (Object[]) lineArgs);
                            j++;
                            uids = uids;
                            uidStats = uidStats;
                        }
                    }
                    i3++;
                    uids = uids;
                    uidStats = uidStats;
                    z2 = true;
                }
            }
            if ((flags & 4) == 0) {
                dumpDurationSteps(printWriter, "", DISCHARGE_STEP_DATA, getDischargeLevelStepTracker(), true);
                String[] lineArgs2 = new String[1];
                long timeRemaining = computeBatteryTimeRemaining(SystemClock.elapsedRealtime() * 1000);
                if (timeRemaining >= 0) {
                    lineArgs2[0] = Long.toString(timeRemaining);
                    dumpLine(printWriter, 0, "i", DISCHARGE_TIME_REMAIN_DATA, (Object[]) lineArgs2);
                }
                dumpDurationSteps(printWriter, "", CHARGE_STEP_DATA, getChargeLevelStepTracker(), true);
                long timeRemaining2 = computeChargeTimeRemaining(SystemClock.elapsedRealtime() * 1000);
                if (timeRemaining2 >= 0) {
                    lineArgs2[0] = Long.toString(timeRemaining2);
                    dumpLine(printWriter, 0, "i", CHARGE_TIME_REMAIN_DATA, (Object[]) lineArgs2);
                }
                if ((flags & 64) != 0) {
                    z = true;
                }
                long j2 = timeRemaining2;
                dumpCheckinLocked(context, printWriter, 0, -1, z);
            }
        }
    }

    public void dumpProtoLocked(Context context, FileDescriptor fd, List<ApplicationInfo> apps, int flags, long histStart) {
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        prepareForDumpLocked();
        if ((flags & 24) != 0) {
            dumpProtoHistoryLocked(proto, flags, histStart);
            proto.flush();
            return;
        }
        long bToken = proto.start(1146756268033L);
        proto.write(1120986464257L, 32);
        proto.write(1112396529666L, getParcelVersion());
        proto.write(1138166333443L, getStartPlatformVersion());
        proto.write(1138166333444L, getEndPlatformVersion());
        if ((flags & 4) == 0) {
            BatteryStatsHelper helper = new BatteryStatsHelper(context, false, (flags & 64) != 0);
            helper.create(this);
            helper.refreshStats(0, -1);
            dumpProtoAppsLocked(proto, helper, apps);
            dumpProtoSystemLocked(proto, helper);
        }
        proto.end(bToken);
        proto.flush();
    }

    private void dumpProtoAppsLocked(ProtoOutputStream proto, BatteryStatsHelper helper, List<ApplicationInfo> apps) {
        ArrayList<String> pkgs;
        ArrayList<String> pkgs2;
        long rawRealtimeMs;
        long j;
        SparseArray<BatterySipper> uidToSipper;
        ArrayMap<String, ? extends Timer> jobs;
        ArrayMap<String, ? extends Timer> syncs;
        long nToken;
        int uid;
        BatterySipper bs;
        ArrayMap<String, SparseIntArray> completions;
        long[] cpuFreqs;
        long cpuToken;
        int i;
        int i2;
        ArrayList<String> pkgs3;
        SparseArray<BatterySipper> uidToSipper2;
        Uid u;
        long batteryUptimeUs;
        SparseArray<ArrayList<String>> aidToPackages;
        long rawRealtimeMs2;
        long rawRealtimeUs;
        SparseArray<? extends Uid> uidStats;
        long rawUptimeUs;
        ArrayList<String> pkgs4;
        SparseArray<BatterySipper> uidToSipper3;
        Uid u2;
        ArrayList<String> pkgs5;
        ProtoOutputStream protoOutputStream = proto;
        List<ApplicationInfo> list = apps;
        boolean z = false;
        long rawUptimeUs2 = SystemClock.uptimeMillis() * 1000;
        long rawRealtimeMs3 = SystemClock.elapsedRealtime();
        long rawRealtimeUs2 = rawRealtimeMs3 * 1000;
        long batteryUptimeUs2 = getBatteryUptime(rawUptimeUs2);
        SparseArray<ArrayList<String>> aidToPackages2 = new SparseArray<>();
        if (list != null) {
            int i3 = 0;
            while (i3 < apps.size()) {
                ApplicationInfo ai = list.get(i3);
                int aid = UserHandle.getAppId(ai.uid);
                ArrayList<String> pkgs6 = aidToPackages2.get(aid);
                if (pkgs6 == null) {
                    pkgs5 = new ArrayList<>();
                    aidToPackages2.put(aid, pkgs5);
                } else {
                    pkgs5 = pkgs6;
                }
                int i4 = aid;
                pkgs5.add(ai.packageName);
                i3++;
                list = apps;
            }
        }
        SparseArray<BatterySipper> uidToSipper4 = new SparseArray<>();
        List<BatterySipper> sippers = helper.getUsageList();
        if (sippers != null) {
            int i5 = 0;
            while (i5 < sippers.size()) {
                BatterySipper bs2 = sippers.get(i5);
                List<BatterySipper> sippers2 = sippers;
                boolean z2 = z;
                if (bs2.drainType == BatterySipper.DrainType.APP) {
                    uidToSipper4.put(bs2.uidObj.getUid(), bs2);
                }
                i5++;
                sippers = sippers2;
                z = z2;
            }
        }
        List<BatterySipper> sippers3 = sippers;
        boolean z3 = z;
        SparseArray<? extends Uid> uidStats2 = getUidStats();
        int n = uidStats2.size();
        int iu = 0;
        while (true) {
            int iu2 = iu;
            if (iu2 < n) {
                int n2 = n;
                long uTkn = protoOutputStream.start(2246267895813L);
                Uid u3 = (Uid) uidStats2.valueAt(iu2);
                long uTkn2 = uTkn;
                int uid2 = uidStats2.keyAt(iu2);
                int iu3 = iu2;
                protoOutputStream.write(1120986464257L, uid2);
                SparseArray<ArrayList<String>> aidToPackages3 = aidToPackages2;
                ArrayList<String> pkgs7 = aidToPackages3.get(UserHandle.getAppId(uid2));
                if (pkgs7 == null) {
                    pkgs = new ArrayList<>();
                } else {
                    pkgs = pkgs7;
                }
                ArrayMap<String, ? extends Uid.Pkg> packageStats = u3.getPackageStats();
                int uid3 = uid2;
                int ipkg = packageStats.size() - 1;
                while (true) {
                    pkgs2 = pkgs;
                    int ipkg2 = ipkg;
                    if (ipkg2 < 0) {
                        break;
                    }
                    String pkg = packageStats.keyAt(ipkg2);
                    ArrayMap<String, ? extends Uid.Pkg> packageStats2 = packageStats;
                    ArrayMap<String, ? extends Uid.Pkg.Serv> serviceStats = ((Uid.Pkg) packageStats.valueAt(ipkg2)).getServiceStats();
                    if (serviceStats.size() == 0) {
                        aidToPackages = aidToPackages3;
                        batteryUptimeUs = batteryUptimeUs2;
                        u = u3;
                        uidToSipper2 = uidToSipper4;
                        uidStats = uidStats2;
                        rawUptimeUs = rawUptimeUs2;
                        rawRealtimeMs2 = rawRealtimeMs3;
                        rawRealtimeUs = rawRealtimeUs2;
                        pkgs3 = pkgs2;
                    } else {
                        uidStats = uidStats2;
                        rawUptimeUs = rawUptimeUs2;
                        rawRealtimeUs = rawRealtimeUs2;
                        long pToken = protoOutputStream.start(2246267895810L);
                        protoOutputStream.write(1138166333441L, pkg);
                        ArrayList<String> pkgs8 = pkgs2;
                        pkgs8.remove(pkg);
                        int isvc = serviceStats.size() - 1;
                        while (isvc >= 0) {
                            Uid.Pkg.Serv ss = (Uid.Pkg.Serv) serviceStats.valueAt(isvc);
                            String pkg2 = pkg;
                            long rawRealtimeMs4 = rawRealtimeMs3;
                            long startTimeMs = roundUsToMs(ss.getStartTime(batteryUptimeUs2, 0));
                            SparseArray<ArrayList<String>> aidToPackages4 = aidToPackages3;
                            int starts = ss.getStarts(0);
                            long batteryUptimeUs3 = batteryUptimeUs2;
                            int launches = ss.getLaunches(0);
                            if (startTimeMs == 0 && starts == 0 && launches == 0) {
                                u2 = u3;
                                uidToSipper3 = uidToSipper4;
                                pkgs4 = pkgs8;
                            } else {
                                u2 = u3;
                                long sToken = protoOutputStream.start(2246267895810L);
                                uidToSipper3 = uidToSipper4;
                                pkgs4 = pkgs8;
                                protoOutputStream.write(1138166333441L, serviceStats.keyAt(isvc));
                                protoOutputStream.write(1112396529666L, startTimeMs);
                                protoOutputStream.write(1120986464259L, starts);
                                protoOutputStream.write(1120986464260L, launches);
                                protoOutputStream.end(sToken);
                            }
                            isvc--;
                            pkg = pkg2;
                            rawRealtimeMs3 = rawRealtimeMs4;
                            aidToPackages3 = aidToPackages4;
                            batteryUptimeUs2 = batteryUptimeUs3;
                            u3 = u2;
                            uidToSipper4 = uidToSipper3;
                            pkgs8 = pkgs4;
                        }
                        aidToPackages = aidToPackages3;
                        batteryUptimeUs = batteryUptimeUs2;
                        u = u3;
                        uidToSipper2 = uidToSipper4;
                        pkgs3 = pkgs8;
                        rawRealtimeMs2 = rawRealtimeMs3;
                        protoOutputStream.end(pToken);
                    }
                    ipkg = ipkg2 - 1;
                    packageStats = packageStats2;
                    rawUptimeUs2 = rawUptimeUs;
                    uidStats2 = uidStats;
                    rawRealtimeUs2 = rawRealtimeUs;
                    rawRealtimeMs3 = rawRealtimeMs2;
                    aidToPackages3 = aidToPackages;
                    batteryUptimeUs2 = batteryUptimeUs;
                    u3 = u;
                    uidToSipper4 = uidToSipper2;
                    pkgs = pkgs3;
                }
                ArrayMap<String, ? extends Uid.Pkg> packageStats3 = packageStats;
                SparseArray<ArrayList<String>> aidToPackages5 = aidToPackages3;
                long batteryUptimeUs4 = batteryUptimeUs2;
                Uid u4 = u3;
                SparseArray<BatterySipper> uidToSipper5 = uidToSipper4;
                SparseArray<? extends Uid> uidStats3 = uidStats2;
                long rawUptimeUs3 = rawUptimeUs2;
                long rawRealtimeMs5 = rawRealtimeMs3;
                long rawRealtimeUs3 = rawRealtimeUs2;
                ArrayList<String> pkgs9 = pkgs2;
                Iterator<String> it = pkgs9.iterator();
                while (it.hasNext()) {
                    long pToken2 = protoOutputStream.start(2246267895810L);
                    protoOutputStream.write(1138166333441L, it.next());
                    protoOutputStream.end(pToken2);
                }
                Uid u5 = u4;
                if (u5.getAggregatedPartialWakelockTimer() != null) {
                    Timer timer = u5.getAggregatedPartialWakelockTimer();
                    rawRealtimeMs = rawRealtimeMs5;
                    long totTimeMs = timer.getTotalDurationMsLocked(rawRealtimeMs);
                    Timer bgTimer = timer.getSubTimer();
                    long bgTimeMs = bgTimer != null ? bgTimer.getTotalDurationMsLocked(rawRealtimeMs) : 0;
                    long awToken = protoOutputStream.start(1146756268056L);
                    protoOutputStream.write(1112396529665L, totTimeMs);
                    protoOutputStream.write(1112396529666L, bgTimeMs);
                    protoOutputStream.end(awToken);
                } else {
                    rawRealtimeMs = rawRealtimeMs5;
                }
                long uTkn3 = uTkn2;
                ArrayMap<String, ? extends Uid.Pkg> packageStats4 = packageStats3;
                ArrayList<String> arrayList = pkgs9;
                int n3 = n2;
                int iu4 = iu3;
                SparseArray<ArrayList<String>> aidToPackages6 = aidToPackages5;
                int uid4 = uid3;
                long batteryUptimeUs5 = batteryUptimeUs4;
                List<BatterySipper> sippers4 = sippers3;
                Uid u6 = u5;
                dumpTimer(protoOutputStream, 1146756268040L, u5.getAudioTurnedOnTimer(), rawRealtimeUs3, 0);
                dumpControllerActivityProto(protoOutputStream, 1146756268035L, u6.getBluetoothControllerActivity(), 0);
                Timer bleTimer = u6.getBluetoothScanTimer();
                if (bleTimer != null) {
                    ProtoOutputStream protoOutputStream2 = protoOutputStream;
                    long bmToken = protoOutputStream.start(1146756268038L);
                    long bmToken2 = rawRealtimeUs3;
                    dumpTimer(protoOutputStream2, 1146756268033L, bleTimer, bmToken2, 0);
                    dumpTimer(protoOutputStream2, 1146756268034L, u6.getBluetoothScanBackgroundTimer(), bmToken2, 0);
                    dumpTimer(protoOutputStream2, 1146756268035L, u6.getBluetoothUnoptimizedScanTimer(), bmToken2, 0);
                    dumpTimer(protoOutputStream2, 1146756268036L, u6.getBluetoothUnoptimizedScanBackgroundTimer(), bmToken2, 0);
                    if (u6.getBluetoothScanResultCounter() != null) {
                        i = u6.getBluetoothScanResultCounter().getCountLocked(0);
                    } else {
                        i = 0;
                    }
                    protoOutputStream.write(1120986464261L, i);
                    if (u6.getBluetoothScanResultBgCounter() != null) {
                        i2 = u6.getBluetoothScanResultBgCounter().getCountLocked(0);
                    } else {
                        i2 = 0;
                    }
                    protoOutputStream.write(1120986464262L, i2);
                    protoOutputStream.end(bmToken);
                }
                dumpTimer(protoOutputStream, 1146756268041L, u6.getCameraTurnedOnTimer(), rawRealtimeUs3, 0);
                long cpuToken2 = protoOutputStream.start(1146756268039L);
                protoOutputStream.write(1112396529665L, roundUsToMs(u6.getUserCpuTimeUs(0)));
                protoOutputStream.write(1112396529666L, roundUsToMs(u6.getSystemCpuTimeUs(0)));
                long[] cpuFreqs2 = getCpuFreqs();
                if (cpuFreqs2 != null) {
                    long[] cpuFreqTimeMs = u6.getCpuFreqTimes(0);
                    if (cpuFreqTimeMs != null && cpuFreqTimeMs.length == cpuFreqs2.length) {
                        long[] screenOffCpuFreqTimeMs = u6.getScreenOffCpuFreqTimes(0);
                        if (screenOffCpuFreqTimeMs == null) {
                            screenOffCpuFreqTimeMs = new long[cpuFreqTimeMs.length];
                        }
                        int ic = 0;
                        while (ic < cpuFreqTimeMs.length) {
                            long cToken = protoOutputStream.start(2246267895811L);
                            protoOutputStream.write(1120986464257L, ic + 1);
                            protoOutputStream.write(1112396529666L, cpuFreqTimeMs[ic]);
                            protoOutputStream.write(1112396529667L, screenOffCpuFreqTimeMs[ic]);
                            protoOutputStream.end(cToken);
                            ic++;
                            packageStats4 = packageStats4;
                            rawRealtimeMs = rawRealtimeMs;
                            n3 = n3;
                            uTkn3 = uTkn3;
                        }
                    }
                }
                ArrayMap<String, ? extends Uid.Pkg> packageStats5 = packageStats4;
                int n4 = n3;
                long rawRealtimeMs6 = rawRealtimeMs;
                long uTkn4 = uTkn3;
                int procState = 0;
                while (true) {
                    j = 1159641169921L;
                    if (procState >= 7) {
                        break;
                    }
                    long[] timesMs = u6.getCpuFreqTimes(0, procState);
                    if (timesMs == null || timesMs.length != cpuFreqs2.length) {
                        cpuToken = cpuToken2;
                    } else {
                        long[] screenOffTimesMs = u6.getScreenOffCpuFreqTimes(0, procState);
                        if (screenOffTimesMs == null) {
                            screenOffTimesMs = new long[timesMs.length];
                        }
                        long procToken = protoOutputStream.start(2246267895812L);
                        protoOutputStream.write(1159641169921L, procState);
                        int ic2 = 0;
                        while (ic2 < timesMs.length) {
                            long cToken2 = protoOutputStream.start(2246267895810L);
                            protoOutputStream.write(1120986464257L, ic2 + 1);
                            protoOutputStream.write(1112396529666L, timesMs[ic2]);
                            protoOutputStream.write(1112396529667L, screenOffTimesMs[ic2]);
                            protoOutputStream.end(cToken2);
                            ic2++;
                            cpuToken2 = cpuToken2;
                        }
                        cpuToken = cpuToken2;
                        protoOutputStream.end(procToken);
                    }
                    procState++;
                    cpuToken2 = cpuToken;
                }
                long cpuToken3 = cpuToken2;
                protoOutputStream.end(cpuToken3);
                ProtoOutputStream protoOutputStream3 = protoOutputStream;
                long j2 = cpuToken3;
                long j3 = rawRealtimeUs3;
                long[] cpuFreqs3 = cpuFreqs2;
                dumpTimer(protoOutputStream3, 1146756268042L, u6.getFlashlightTurnedOnTimer(), j3, 0);
                dumpTimer(protoOutputStream3, 1146756268043L, u6.getForegroundActivityTimer(), j3, 0);
                dumpTimer(protoOutputStream3, 1146756268044L, u6.getForegroundServiceTimer(), j3, 0);
                ArrayMap<String, SparseIntArray> completions2 = u6.getJobCompletionStats();
                int[] reasons = {0, 1, 2, 3, 4};
                int ic3 = 0;
                while (ic3 < completions2.size()) {
                    SparseIntArray types = completions2.valueAt(ic3);
                    if (types != null) {
                        long jcToken = protoOutputStream.start(2246267895824L);
                        protoOutputStream.write(1138166333441L, completions2.keyAt(ic3));
                        int length = reasons.length;
                        int i6 = 0;
                        while (i6 < length) {
                            int r = reasons[i6];
                            long[] cpuFreqs4 = cpuFreqs3;
                            long rToken = protoOutputStream.start(2246267895810L);
                            protoOutputStream.write(j, r);
                            protoOutputStream.write(1120986464258L, types.get(r, 0));
                            protoOutputStream.end(rToken);
                            i6++;
                            cpuFreqs3 = cpuFreqs4;
                            completions2 = completions2;
                            j = 1159641169921L;
                        }
                        cpuFreqs = cpuFreqs3;
                        completions = completions2;
                        protoOutputStream.end(jcToken);
                    } else {
                        cpuFreqs = cpuFreqs3;
                        completions = completions2;
                    }
                    ic3++;
                    cpuFreqs3 = cpuFreqs;
                    completions2 = completions;
                    j = 1159641169921L;
                }
                ArrayMap<String, SparseIntArray> arrayMap = completions2;
                ArrayMap<String, ? extends Timer> jobs2 = u6.getJobStats();
                int ij = jobs2.size() - 1;
                while (true) {
                    int ij2 = ij;
                    if (ij2 < 0) {
                        break;
                    }
                    Timer timer2 = (Timer) jobs2.valueAt(ij2);
                    Timer bgTimer2 = timer2.getSubTimer();
                    long jToken = protoOutputStream.start(2246267895823L);
                    protoOutputStream.write(1138166333441L, jobs2.keyAt(ij2));
                    ProtoOutputStream protoOutputStream4 = protoOutputStream;
                    long jToken2 = jToken;
                    long jToken3 = rawRealtimeUs3;
                    dumpTimer(protoOutputStream4, 1146756268034L, timer2, jToken3, 0);
                    dumpTimer(protoOutputStream4, 1146756268035L, bgTimer2, jToken3, 0);
                    protoOutputStream.end(jToken2);
                    ij = ij2 - 1;
                }
                dumpControllerActivityProto(protoOutputStream, 1146756268036L, u6.getModemControllerActivity(), 0);
                long nToken2 = protoOutputStream.start(1146756268049L);
                protoOutputStream.write(1112396529665L, u6.getNetworkActivityBytes(0, 0));
                protoOutputStream.write(1112396529666L, u6.getNetworkActivityBytes(1, 0));
                protoOutputStream.write(1112396529667L, u6.getNetworkActivityBytes(2, 0));
                protoOutputStream.write(1112396529668L, u6.getNetworkActivityBytes(3, 0));
                protoOutputStream.write(1112396529669L, u6.getNetworkActivityBytes(4, 0));
                protoOutputStream.write(1112396529670L, u6.getNetworkActivityBytes(5, 0));
                protoOutputStream.write(1112396529671L, u6.getNetworkActivityPackets(0, 0));
                protoOutputStream.write(1112396529672L, u6.getNetworkActivityPackets(1, 0));
                protoOutputStream.write(1112396529673L, u6.getNetworkActivityPackets(2, 0));
                protoOutputStream.write(1112396529674L, u6.getNetworkActivityPackets(3, 0));
                protoOutputStream.write(1112396529675L, roundUsToMs(u6.getMobileRadioActiveTime(0)));
                protoOutputStream.write(1120986464268L, u6.getMobileRadioActiveCount(0));
                protoOutputStream.write(1120986464269L, u6.getMobileRadioApWakeupCount(0));
                protoOutputStream.write((long) UidProto.Network.WIFI_WAKEUP_COUNT, u6.getWifiRadioApWakeupCount(0));
                protoOutputStream.write((long) UidProto.Network.MOBILE_BYTES_BG_RX, u6.getNetworkActivityBytes(6, 0));
                protoOutputStream.write(1112396529680L, u6.getNetworkActivityBytes(7, 0));
                protoOutputStream.write(1112396529681L, u6.getNetworkActivityBytes(8, 0));
                protoOutputStream.write((long) UidProto.Network.WIFI_BYTES_BG_TX, u6.getNetworkActivityBytes(9, 0));
                protoOutputStream.write(1112396529683L, u6.getNetworkActivityPackets(6, 0));
                protoOutputStream.write(1112396529684L, u6.getNetworkActivityPackets(7, 0));
                protoOutputStream.write(1112396529685L, u6.getNetworkActivityPackets(8, 0));
                protoOutputStream.write((long) UidProto.Network.WIFI_PACKETS_BG_TX, u6.getNetworkActivityPackets(9, 0));
                protoOutputStream.end(nToken2);
                SparseArray<BatterySipper> uidToSipper6 = uidToSipper5;
                int uid5 = uid4;
                BatterySipper bs3 = uidToSipper6.get(uid5);
                if (bs3 != null) {
                    long bsToken = protoOutputStream.start(1146756268050L);
                    uidToSipper = uidToSipper6;
                    protoOutputStream.write(1103806595073L, bs3.totalPowerMah);
                    protoOutputStream.write(1133871366146L, bs3.shouldHide);
                    protoOutputStream.write(1103806595075L, bs3.screenPowerMah);
                    protoOutputStream.write(1103806595076L, bs3.proportionalSmearMah);
                    protoOutputStream.end(bsToken);
                } else {
                    uidToSipper = uidToSipper6;
                }
                ArrayMap<String, ? extends Uid.Proc> processStats = u6.getProcessStats();
                int ipr = processStats.size() - 1;
                while (ipr >= 0) {
                    Uid.Proc ps = (Uid.Proc) processStats.valueAt(ipr);
                    long prToken = protoOutputStream.start(2246267895827L);
                    protoOutputStream.write(1138166333441L, processStats.keyAt(ipr));
                    protoOutputStream.write(1112396529666L, ps.getUserTime(0));
                    protoOutputStream.write(1112396529667L, ps.getSystemTime(0));
                    protoOutputStream.write(1112396529668L, ps.getForegroundTime(0));
                    protoOutputStream.write(1120986464261L, ps.getStarts(0));
                    protoOutputStream.write(1120986464262L, ps.getNumAnrs(0));
                    protoOutputStream.write(1120986464263L, ps.getNumCrashes(0));
                    protoOutputStream.end(prToken);
                    ipr--;
                    processStats = processStats;
                    uid5 = uid5;
                }
                int uid6 = uid5;
                ArrayMap<String, ? extends Uid.Proc> arrayMap2 = processStats;
                SparseArray<? extends Uid.Sensor> sensors = u6.getSensorStats();
                int ise = 0;
                while (true) {
                    int ise2 = ise;
                    if (ise2 >= sensors.size()) {
                        break;
                    }
                    Uid.Sensor se = (Uid.Sensor) sensors.valueAt(ise2);
                    Timer timer3 = se.getSensorTime();
                    if (timer3 == null) {
                        bs = bs3;
                        uid = uid6;
                    } else {
                        Timer bgTimer3 = se.getSensorBackgroundTime();
                        int sensorNumber = sensors.keyAt(ise2);
                        protoOutputStream.write(1120986464257L, sensorNumber);
                        ProtoOutputStream protoOutputStream5 = protoOutputStream;
                        bs = bs3;
                        long j4 = rawRealtimeUs3;
                        int i7 = sensorNumber;
                        uid = uid6;
                        dumpTimer(protoOutputStream5, 1146756268034L, timer3, j4, 0);
                        dumpTimer(protoOutputStream5, 1146756268035L, bgTimer3, j4, 0);
                        protoOutputStream.end(protoOutputStream.start(UidProto.SENSORS));
                    }
                    ise = ise2 + 1;
                    bs3 = bs;
                    uid6 = uid;
                }
                int i8 = uid6;
                int ips = 0;
                while (ips < 7) {
                    long rawRealtimeUs4 = rawRealtimeUs3;
                    long durMs = roundUsToMs(u6.getProcessStateTime(ips, rawRealtimeUs4, 0));
                    if (durMs == 0) {
                        nToken = nToken2;
                    } else {
                        long stToken = protoOutputStream.start(2246267895828L);
                        protoOutputStream.write(1159641169921L, ips);
                        nToken = nToken2;
                        protoOutputStream.write(1112396529666L, durMs);
                        protoOutputStream.end(stToken);
                    }
                    ips++;
                    rawRealtimeUs3 = rawRealtimeUs4;
                    nToken2 = nToken;
                }
                long rawRealtimeUs5 = rawRealtimeUs3;
                ArrayMap<String, ? extends Timer> syncs2 = u6.getSyncStats();
                int isy = syncs2.size() - 1;
                while (true) {
                    int isy2 = isy;
                    if (isy2 < 0) {
                        break;
                    }
                    Timer timer4 = (Timer) syncs2.valueAt(isy2);
                    Timer bgTimer4 = timer4.getSubTimer();
                    long syToken = protoOutputStream.start(2246267895830L);
                    protoOutputStream.write(1138166333441L, syncs2.keyAt(isy2));
                    ProtoOutputStream protoOutputStream6 = protoOutputStream;
                    long syToken2 = syToken;
                    long syToken3 = rawRealtimeUs5;
                    Timer timer5 = timer4;
                    dumpTimer(protoOutputStream6, 1146756268034L, timer4, syToken3, 0);
                    dumpTimer(protoOutputStream6, 1146756268035L, bgTimer4, syToken3, 0);
                    protoOutputStream.end(syToken2);
                    isy = isy2 - 1;
                }
                if (u6.hasUserActivity() != 0) {
                    int i9 = 0;
                    while (i9 < 4) {
                        int val = u6.getUserActivityCount(i9, 0);
                        if (val != 0) {
                            long uaToken = protoOutputStream.start(UidProto.USER_ACTIVITY);
                            protoOutputStream.write(1159641169921L, i9);
                            syncs = syncs2;
                            protoOutputStream.write(1120986464258L, val);
                            protoOutputStream.end(uaToken);
                        } else {
                            syncs = syncs2;
                        }
                        i9++;
                        syncs2 = syncs;
                    }
                }
                ProtoOutputStream protoOutputStream7 = protoOutputStream;
                long j5 = rawRealtimeUs5;
                dumpTimer(protoOutputStream7, 1146756268045L, u6.getVibratorOnTimer(), j5, 0);
                dumpTimer(protoOutputStream7, 1146756268046L, u6.getVideoTurnedOnTimer(), j5, 0);
                ArrayMap<String, ? extends Uid.Wakelock> wakelocks = u6.getWakelockStats();
                int iw = wakelocks.size() - 1;
                while (true) {
                    int iw2 = iw;
                    if (iw2 < 0) {
                        break;
                    }
                    Uid.Wakelock wl = (Uid.Wakelock) wakelocks.valueAt(iw2);
                    long wToken = protoOutputStream.start(2246267895833L);
                    protoOutputStream.write(1138166333441L, wakelocks.keyAt(iw2));
                    long wToken2 = wToken;
                    int iw3 = iw2;
                    Uid.Wakelock wl2 = wl;
                    ArrayMap<String, ? extends Uid.Wakelock> wakelocks2 = wakelocks;
                    dumpTimer(protoOutputStream, 1146756268034L, wl.getWakeTime(1), rawRealtimeUs5, 0);
                    Timer pTimer = wl2.getWakeTime(0);
                    if (pTimer != null) {
                        ProtoOutputStream protoOutputStream8 = protoOutputStream;
                        long j6 = rawRealtimeUs5;
                        jobs = jobs2;
                        dumpTimer(protoOutputStream8, 1146756268035L, pTimer, j6, 0);
                        dumpTimer(protoOutputStream8, 1146756268036L, pTimer.getSubTimer(), j6, 0);
                    } else {
                        jobs = jobs2;
                        Timer timer6 = pTimer;
                    }
                    dumpTimer(protoOutputStream, 1146756268037L, wl2.getWakeTime(2), rawRealtimeUs5, 0);
                    protoOutputStream.end(wToken2);
                    iw = iw3 - 1;
                    wakelocks = wakelocks2;
                    jobs2 = jobs;
                }
                ArrayMap<String, ? extends Timer> arrayMap3 = jobs2;
                dumpTimer(protoOutputStream, 1146756268060L, u6.getMulticastWakelockStats(), rawRealtimeUs5, 0);
                ArrayMap<String, ? extends Uid.Pkg> packageStats6 = packageStats5;
                for (int ipkg3 = packageStats6.size() - 1; ipkg3 >= 0; ipkg3--) {
                    ArrayMap<String, ? extends Counter> alarms = ((Uid.Pkg) packageStats6.valueAt(ipkg3)).getWakeupAlarmStats();
                    for (int iwa = alarms.size() - 1; iwa >= 0; iwa--) {
                        long waToken = protoOutputStream.start(2246267895834L);
                        protoOutputStream.write(1138166333441L, alarms.keyAt(iwa));
                        protoOutputStream.write(1120986464258L, ((Counter) alarms.valueAt(iwa)).getCountLocked(0));
                        protoOutputStream.end(waToken);
                    }
                }
                dumpControllerActivityProto(protoOutputStream, 1146756268037L, u6.getWifiControllerActivity(), 0);
                long wToken3 = protoOutputStream.start(1146756268059L);
                protoOutputStream.write(1112396529665L, roundUsToMs(u6.getFullWifiLockTime(rawRealtimeUs5, 0)));
                long wToken4 = wToken3;
                long wToken5 = rawRealtimeUs5;
                dumpTimer(protoOutputStream, 1146756268035L, u6.getWifiScanTimer(), wToken5, 0);
                protoOutputStream.write(1112396529666L, roundUsToMs(u6.getWifiRunningTime(rawRealtimeUs5, 0)));
                dumpTimer(protoOutputStream, 1146756268036L, u6.getWifiScanBackgroundTimer(), wToken5, 0);
                protoOutputStream.end(wToken4);
                protoOutputStream.end(uTkn4);
                iu = iu4 + 1;
                rawRealtimeUs2 = rawRealtimeUs5;
                aidToPackages2 = aidToPackages6;
                batteryUptimeUs2 = batteryUptimeUs5;
                rawUptimeUs2 = rawUptimeUs3;
                uidStats2 = uidStats3;
                sippers3 = sippers4;
                rawRealtimeMs3 = rawRealtimeMs6;
                n = n4;
                uidToSipper4 = uidToSipper;
            } else {
                SparseArray<ArrayList<String>> sparseArray = aidToPackages2;
                long j7 = batteryUptimeUs2;
                SparseArray<BatterySipper> sparseArray2 = uidToSipper4;
                SparseArray<? extends Uid> sparseArray3 = uidStats2;
                long j8 = rawUptimeUs2;
                long j9 = rawRealtimeMs3;
                long j10 = rawRealtimeUs2;
                List<BatterySipper> list2 = sippers3;
                return;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:42:0x00f2 A[Catch:{ all -> 0x0247 }] */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x01e8 A[Catch:{ all -> 0x0247 }] */
    private void dumpProtoHistoryLocked(ProtoOutputStream proto, int flags, long histStart) {
        HistoryEventTracker tracker;
        boolean printed;
        HistoryEventTracker tracker2;
        long lastTime;
        boolean printed2;
        HistoryEventTracker tracker3;
        int i;
        boolean printed3;
        HistoryEventTracker tracker4;
        int i2;
        int oldEventCode;
        HistoryTag oldEventTag;
        HistoryEventTracker tracker5;
        boolean printed4;
        BatteryStats batteryStats = this;
        ProtoOutputStream protoOutputStream = proto;
        if (startIteratingHistoryLocked()) {
            protoOutputStream.write(1120986464257L, 32);
            protoOutputStream.write(1112396529666L, getParcelVersion());
            protoOutputStream.write(1138166333443L, getStartPlatformVersion());
            protoOutputStream.write(1138166333444L, getEndPlatformVersion());
            int i3 = 0;
            while (i3 < getHistoryStringPoolSize()) {
                try {
                    long token = protoOutputStream.start(2246267895813L);
                    protoOutputStream.write(1120986464257L, i3);
                    protoOutputStream.write(1120986464258L, batteryStats.getHistoryTagPoolUid(i3));
                    protoOutputStream.write(1138166333443L, batteryStats.getHistoryTagPoolString(i3));
                    protoOutputStream.end(token);
                    i3++;
                } finally {
                    finishIteratingHistoryLocked();
                }
            }
            HistoryPrinter hprinter = new HistoryPrinter();
            HistoryItem rec = new HistoryItem();
            boolean printed5 = false;
            long baseTime = -1;
            long lastTime2 = -1;
            HistoryEventTracker tracker6 = null;
            while (true) {
                tracker = tracker6;
                if (!batteryStats.getNextHistoryLocked(rec)) {
                    break;
                }
                long lastTime3 = rec.time;
                if (baseTime < 0) {
                    baseTime = lastTime3;
                }
                if (rec.time >= histStart) {
                    if (histStart < 0 || printed5) {
                        lastTime = lastTime3;
                        tracker2 = tracker;
                        printed = printed5;
                    } else {
                        if (!(rec.cmd == 5 || rec.cmd == 7 || rec.cmd == 4)) {
                            if (rec.cmd != 8) {
                                if (rec.currentTime != 0) {
                                    byte cmd = rec.cmd;
                                    rec.cmd = 5;
                                    hprinter.printNextItem(protoOutputStream, rec, baseTime, (flags & 32) != 0);
                                    rec.cmd = cmd;
                                    lastTime = lastTime3;
                                    tracker3 = tracker;
                                    printed2 = true;
                                } else {
                                    lastTime = lastTime3;
                                    printed2 = printed5;
                                    tracker3 = tracker;
                                }
                                if (tracker3 == null) {
                                    if (rec.cmd != 0) {
                                        hprinter.printNextItem(protoOutputStream, rec, baseTime, (flags & 32) != 0);
                                        i = 0;
                                        rec.cmd = 0;
                                    } else {
                                        i = 0;
                                    }
                                    int oldEventCode2 = rec.eventCode;
                                    HistoryTag oldEventTag2 = rec.eventTag;
                                    rec.eventTag = new HistoryTag();
                                    int i4 = i;
                                    while (true) {
                                        int i5 = i4;
                                        if (i5 >= 22) {
                                            break;
                                        }
                                        HashMap<String, SparseIntArray> active = tracker3.getStateForEvent(i5);
                                        if (active == null) {
                                            tracker4 = tracker3;
                                            oldEventTag = oldEventTag2;
                                            i2 = i5;
                                            oldEventCode = oldEventCode2;
                                            printed3 = printed2;
                                        } else {
                                            Iterator<Map.Entry<String, SparseIntArray>> it = active.entrySet().iterator();
                                            while (it.hasNext()) {
                                                Map.Entry<String, SparseIntArray> ent = it.next();
                                                SparseIntArray uids = ent.getValue();
                                                int j = i;
                                                while (true) {
                                                    tracker5 = tracker3;
                                                    SparseIntArray uids2 = uids;
                                                    printed4 = printed2;
                                                    int j2 = j;
                                                    if (j2 >= uids2.size()) {
                                                        break;
                                                    }
                                                    rec.eventCode = i5;
                                                    Map.Entry<String, SparseIntArray> ent2 = ent;
                                                    rec.eventTag.string = ent.getKey();
                                                    rec.eventTag.uid = uids2.keyAt(j2);
                                                    rec.eventTag.poolIdx = uids2.valueAt(j2);
                                                    hprinter.printNextItem(protoOutputStream, rec, baseTime, (flags & 32) != 0);
                                                    rec.wakeReasonTag = null;
                                                    rec.wakelockTag = null;
                                                    int j3 = j2 + 1;
                                                    oldEventTag2 = oldEventTag2;
                                                    oldEventCode2 = oldEventCode2;
                                                    it = it;
                                                    active = active;
                                                    i5 = i5;
                                                    tracker3 = tracker5;
                                                    printed2 = printed4;
                                                    uids = uids2;
                                                    j = j3;
                                                    ent = ent2;
                                                }
                                                Iterator<Map.Entry<String, SparseIntArray>> it2 = it;
                                                HashMap<String, SparseIntArray> hashMap = active;
                                                HistoryTag historyTag = oldEventTag2;
                                                int i6 = i5;
                                                int i7 = oldEventCode2;
                                                tracker3 = tracker5;
                                                printed2 = printed4;
                                                i = 0;
                                            }
                                            tracker4 = tracker3;
                                            oldEventTag = oldEventTag2;
                                            i2 = i5;
                                            oldEventCode = oldEventCode2;
                                            printed3 = printed2;
                                        }
                                        i4 = i2 + 1;
                                        oldEventTag2 = oldEventTag;
                                        oldEventCode2 = oldEventCode;
                                        tracker3 = tracker4;
                                        printed2 = printed3;
                                        i = 0;
                                    }
                                    printed = printed2;
                                    rec.eventCode = oldEventCode2;
                                    rec.eventTag = oldEventTag2;
                                    tracker2 = null;
                                } else {
                                    tracker2 = tracker3;
                                    printed = printed2;
                                }
                            }
                        }
                        printed2 = true;
                        lastTime = lastTime3;
                        tracker3 = tracker;
                        hprinter.printNextItem(protoOutputStream, rec, baseTime, (flags & 32) != 0);
                        rec.cmd = 0;
                        if (tracker3 == null) {
                        }
                    }
                    hprinter.printNextItem(protoOutputStream, rec, baseTime, (flags & 32) != 0);
                    lastTime2 = lastTime;
                    tracker6 = tracker2;
                    printed5 = printed;
                    batteryStats = this;
                } else {
                    lastTime2 = lastTime3;
                    tracker6 = tracker;
                    batteryStats = this;
                }
            }
            if (histStart >= 0) {
                commitCurrentHistoryBatchLocked();
                HistoryPrinter historyPrinter = hprinter;
                protoOutputStream.write(2237677961222L, "NEXT: " + (lastTime2 + 1));
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:52:0x05cf, code lost:
        r3 = r23;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x05de, code lost:
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x060d, code lost:
        r90 = r5;
        r4 = r8.start(android.os.SystemProto.POWER_USE_ITEM);
        r91 = r12;
        r8.write(1159641169921L, r6);
        r8.write(1120986464258L, r3);
        r94 = r6;
        r93 = r7;
        r8.write(1103806595075L, r2.totalPowerMah);
        r8.write(1133871366148L, r2.shouldHide);
        r8.write((long) android.os.SystemProto.PowerUseItem.SCREEN_POWER_MAH, r2.screenPowerMah);
        r8.write((long) android.os.SystemProto.PowerUseItem.PROPORTIONAL_SMEAR_MAH, r2.proportionalSmearMah);
        r8.end(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0659, code lost:
        r1 = r1 + 1;
        r3 = r88;
        r5 = r90;
        r12 = r91;
        r7 = r93;
     */
    private void dumpProtoSystemLocked(ProtoOutputStream proto, BatteryStatsHelper helper) {
        boolean isNone;
        int n;
        ArrayMap<String, ? extends Uid.Wakelock> wakelocks;
        Uid u;
        int i;
        long rawRealtimeUs;
        long pdcToken;
        long timeRemainingUs;
        ProtoOutputStream protoOutputStream = proto;
        BatteryStatsHelper batteryStatsHelper = helper;
        long sToken = protoOutputStream.start(1146756268038L);
        long rawUptimeUs = SystemClock.uptimeMillis() * 1000;
        long rawRealtimeUs2 = SystemClock.elapsedRealtime() * 1000;
        batteryStatsHelper.create(this);
        batteryStatsHelper.refreshStats(0, -1);
        int estimatedBatteryCapacity = (int) helper.getPowerProfile().getBatteryCapacity();
        long bToken = protoOutputStream.start(1146756268033L);
        protoOutputStream.write(1112396529665L, getStartClockTime());
        protoOutputStream.write(1112396529666L, getStartCount());
        protoOutputStream.write(1112396529667L, computeRealtime(rawRealtimeUs2, 0) / 1000);
        protoOutputStream.write(1112396529668L, computeUptime(rawUptimeUs, 0) / 1000);
        protoOutputStream.write(1112396529669L, computeBatteryRealtime(rawRealtimeUs2, 0) / 1000);
        protoOutputStream.write(1112396529670L, computeBatteryUptime(rawUptimeUs, 0) / 1000);
        protoOutputStream.write(1112396529671L, computeBatteryScreenOffRealtime(rawRealtimeUs2, 0) / 1000);
        protoOutputStream.write(1112396529672L, computeBatteryScreenOffUptime(rawUptimeUs, 0) / 1000);
        protoOutputStream.write(1112396529673L, getScreenDozeTime(rawRealtimeUs2, 0) / 1000);
        int estimatedBatteryCapacity2 = estimatedBatteryCapacity;
        protoOutputStream.write(1112396529674L, estimatedBatteryCapacity2);
        protoOutputStream.write(1112396529675L, getMinLearnedBatteryCapacity());
        protoOutputStream.write(1112396529676L, getMaxLearnedBatteryCapacity());
        long bToken2 = bToken;
        protoOutputStream.end(bToken2);
        long bdToken = protoOutputStream.start(1146756268034L);
        long bToken3 = bToken2;
        protoOutputStream.write(1120986464257L, getLowDischargeAmountSinceCharge());
        protoOutputStream.write(1120986464258L, getHighDischargeAmountSinceCharge());
        protoOutputStream.write(1120986464259L, getDischargeAmountScreenOnSinceCharge());
        protoOutputStream.write(1120986464260L, getDischargeAmountScreenOffSinceCharge());
        protoOutputStream.write(1120986464261L, getDischargeAmountScreenDozeSinceCharge());
        int estimatedBatteryCapacity3 = estimatedBatteryCapacity2;
        protoOutputStream.write(1112396529670L, getUahDischarge(0) / 1000);
        protoOutputStream.write(1112396529671L, getUahDischargeScreenOff(0) / 1000);
        protoOutputStream.write(1112396529672L, getUahDischargeScreenDoze(0) / 1000);
        protoOutputStream.write(1112396529673L, getUahDischargeLightDoze(0) / 1000);
        protoOutputStream.write(1112396529674L, getUahDischargeDeepDoze(0) / 1000);
        protoOutputStream.end(bdToken);
        long timeRemainingUs2 = computeChargeTimeRemaining(rawRealtimeUs2);
        if (timeRemainingUs2 >= 0) {
            long j = timeRemainingUs2;
            protoOutputStream.write(1112396529667L, timeRemainingUs2 / 1000);
        } else {
            long timeRemainingUs3 = computeBatteryTimeRemaining(rawRealtimeUs2);
            if (timeRemainingUs3 >= 0) {
                timeRemainingUs = timeRemainingUs3;
                protoOutputStream.write(1112396529668L, timeRemainingUs3 / 1000);
            } else {
                timeRemainingUs = timeRemainingUs3;
                protoOutputStream.write(1112396529668L, -1);
            }
            long j2 = timeRemainingUs;
        }
        dumpDurationSteps(protoOutputStream, 2246267895813L, getChargeLevelStepTracker());
        int i2 = 0;
        while (true) {
            int i3 = i2;
            isNone = true;
            if (i3 >= 21) {
                break;
            }
            if (i3 != 0) {
                isNone = false;
            }
            int telephonyNetworkType = i3;
            if (i3 == 20) {
                telephonyNetworkType = 0;
            }
            int telephonyNetworkType2 = telephonyNetworkType;
            long rawRealtimeUs3 = rawRealtimeUs2;
            long pdcToken2 = protoOutputStream.start(2246267895816L);
            if (isNone) {
                pdcToken = pdcToken2;
                protoOutputStream.write(1133871366146L, isNone);
            } else {
                pdcToken = pdcToken2;
                protoOutputStream.write(1159641169921L, telephonyNetworkType2);
            }
            int i4 = telephonyNetworkType2;
            long j3 = bToken3;
            int i5 = estimatedBatteryCapacity3;
            rawRealtimeUs2 = rawRealtimeUs3;
            boolean z = isNone;
            dumpTimer(protoOutputStream, 1146756268035L, getPhoneDataConnectionTimer(i3), rawRealtimeUs2, 0);
            protoOutputStream.end(pdcToken);
            i2 = i3 + 1;
            bdToken = bdToken;
        }
        long rawRealtimeUs4 = rawRealtimeUs2;
        long j4 = bdToken;
        long j5 = bToken3;
        int i6 = estimatedBatteryCapacity3;
        dumpDurationSteps(protoOutputStream, 2246267895814L, getDischargeLevelStepTracker());
        long[] cpuFreqs = getCpuFreqs();
        if (cpuFreqs != null) {
            for (long i7 : cpuFreqs) {
                protoOutputStream.write((long) SystemProto.CPU_FREQUENCY, i7);
            }
        }
        dumpControllerActivityProto(protoOutputStream, 1146756268041L, getBluetoothControllerActivity(), 0);
        dumpControllerActivityProto(protoOutputStream, 1146756268042L, getModemControllerActivity(), 0);
        long gnToken = protoOutputStream.start(1146756268044L);
        protoOutputStream.write(1112396529665L, getNetworkActivityBytes(0, 0));
        protoOutputStream.write(1112396529666L, getNetworkActivityBytes(1, 0));
        protoOutputStream.write(1112396529669L, getNetworkActivityPackets(0, 0));
        protoOutputStream.write(1112396529670L, getNetworkActivityPackets(1, 0));
        protoOutputStream.write(1112396529667L, getNetworkActivityBytes(2, 0));
        protoOutputStream.write(1112396529668L, getNetworkActivityBytes(3, 0));
        protoOutputStream.write(1112396529671L, getNetworkActivityPackets(2, 0));
        protoOutputStream.write(1112396529672L, getNetworkActivityPackets(3, 0));
        protoOutputStream.write(1112396529673L, getNetworkActivityBytes(4, 0));
        protoOutputStream.write(1112396529674L, getNetworkActivityBytes(5, 0));
        long gnToken2 = gnToken;
        protoOutputStream.end(gnToken2);
        dumpControllerActivityProto(protoOutputStream, 1146756268043L, getWifiControllerActivity(), 0);
        long gnToken3 = gnToken2;
        long rawRealtimeUs5 = rawRealtimeUs4;
        long[] jArr = cpuFreqs;
        protoOutputStream.write(1112396529665L, getWifiOnTime(rawRealtimeUs5, 0) / 1000);
        protoOutputStream.write(1112396529666L, getGlobalWifiRunningTime(rawRealtimeUs5, 0) / 1000);
        long gwToken = protoOutputStream.start(1146756268045L);
        protoOutputStream.end(gwToken);
        Map<String, ? extends Timer> kernelWakelocks = getKernelWakelockStats();
        Iterator<Map.Entry<String, ? extends Timer>> it = kernelWakelocks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ? extends Timer> ent = it.next();
            long rawRealtimeUs6 = rawRealtimeUs5;
            long kwToken = protoOutputStream.start(2246267895822L);
            protoOutputStream = proto;
            protoOutputStream.write(1138166333441L, ent.getKey());
            long j6 = gnToken3;
            long rawRealtimeUs7 = rawRealtimeUs6;
            Map.Entry<String, ? extends Timer> entry = ent;
            dumpTimer(protoOutputStream, 1146756268034L, (Timer) ent.getValue(), rawRealtimeUs7, 0);
            protoOutputStream.end(kwToken);
            isNone = true;
            it = it;
            gwToken = gwToken;
            kernelWakelocks = kernelWakelocks;
            rawRealtimeUs5 = rawRealtimeUs7;
        }
        long rawRealtimeUs8 = rawRealtimeUs5;
        long j7 = gwToken;
        Map<String, ? extends Timer> map = kernelWakelocks;
        boolean z2 = isNone;
        long j8 = gnToken3;
        SparseArray<? extends Uid> uidStats = getUidStats();
        long fullWakeLockTimeTotalUs = 0;
        long partialWakeLockTimeTotalUs = 0;
        int iu = 0;
        while (iu < uidStats.size()) {
            Uid u2 = (Uid) uidStats.valueAt(iu);
            ArrayMap<String, ? extends Uid.Wakelock> wakelocks2 = u2.getWakelockStats();
            int iw = wakelocks2.size() - (z2 ? 1 : 0);
            while (iw >= 0) {
                Uid.Wakelock wl = (Uid.Wakelock) wakelocks2.valueAt(iw);
                Timer fullWakeTimer = wl.getWakeTime(z2 ? 1 : 0);
                if (fullWakeTimer != null) {
                    u = u2;
                    wakelocks = wakelocks2;
                    rawRealtimeUs = rawRealtimeUs8;
                    i = 0;
                    fullWakeLockTimeTotalUs += fullWakeTimer.getTotalTimeLocked(rawRealtimeUs, 0);
                } else {
                    u = u2;
                    wakelocks = wakelocks2;
                    rawRealtimeUs = rawRealtimeUs8;
                    i = 0;
                }
                Timer partialWakeTimer = wl.getWakeTime(i);
                if (partialWakeTimer != null) {
                    partialWakeLockTimeTotalUs += partialWakeTimer.getTotalTimeLocked(rawRealtimeUs, i);
                }
                iw--;
                rawRealtimeUs8 = rawRealtimeUs;
                u2 = u;
                wakelocks2 = wakelocks;
                z2 = true;
            }
            iu++;
            z2 = true;
        }
        long rawRealtimeUs9 = rawRealtimeUs8;
        long mToken = protoOutputStream.start(1146756268047L);
        protoOutputStream.write(1112396529665L, getScreenOnTime(rawRealtimeUs9, 0) / 1000);
        long rawRealtimeUs10 = rawRealtimeUs9;
        SparseArray<? extends Uid> sparseArray = uidStats;
        protoOutputStream.write(1112396529666L, getPhoneOnTime(rawRealtimeUs10, 0) / 1000);
        protoOutputStream.write(1112396529667L, fullWakeLockTimeTotalUs / 1000);
        protoOutputStream.write(1112396529668L, partialWakeLockTimeTotalUs / 1000);
        protoOutputStream.write(1112396529669L, getMobileRadioActiveTime(rawRealtimeUs10, 0) / 1000);
        protoOutputStream.write(1112396529670L, getMobileRadioActiveAdjustedTime(0) / 1000);
        protoOutputStream.write(1120986464263L, getMobileRadioActiveCount(0));
        protoOutputStream.write(1120986464264L, getMobileRadioActiveUnknownTime(0) / 1000);
        protoOutputStream.write(1112396529673L, getInteractiveTime(rawRealtimeUs10, 0) / 1000);
        protoOutputStream.write(1112396529674L, getPowerSaveModeEnabledTime(rawRealtimeUs10, 0) / 1000);
        protoOutputStream.write(1120986464267L, getNumConnectivityChange(0));
        protoOutputStream.write(1112396529676L, getDeviceIdleModeTime(2, rawRealtimeUs10, 0) / 1000);
        protoOutputStream.write(1120986464269L, getDeviceIdleModeCount(2, 0));
        protoOutputStream.write((long) SystemProto.Misc.DEEP_DOZE_IDLING_DURATION_MS, getDeviceIdlingTime(2, rawRealtimeUs10, 0) / 1000);
        protoOutputStream.write((long) SystemProto.Misc.DEEP_DOZE_IDLING_COUNT, getDeviceIdlingCount(2, 0));
        protoOutputStream.write(1112396529680L, getLongestDeviceIdleModeTime(2));
        protoOutputStream.write(1112396529681L, getDeviceIdleModeTime(1, rawRealtimeUs10, 0) / 1000);
        protoOutputStream.write((long) SystemProto.Misc.LIGHT_DOZE_COUNT, getDeviceIdleModeCount(1, 0));
        protoOutputStream.write(1112396529683L, getDeviceIdlingTime(1, rawRealtimeUs10, 0) / 1000);
        protoOutputStream.write((long) SystemProto.Misc.LIGHT_DOZE_IDLING_COUNT, getDeviceIdlingCount(1, 0));
        protoOutputStream.write(1112396529685L, getLongestDeviceIdleModeTime(1));
        protoOutputStream.end(mToken);
        long multicastWakeLockTimeTotalUs = getWifiMulticastWakelockTime(rawRealtimeUs10, 0);
        int multicastWakeLockCountTotal = getWifiMulticastWakelockCount(0);
        long wmctToken = protoOutputStream.start(1146756268055L);
        long rawRealtimeUs11 = rawRealtimeUs10;
        long mToken2 = mToken;
        protoOutputStream.write(1112396529665L, multicastWakeLockTimeTotalUs / 1000);
        protoOutputStream.write(1120986464258L, multicastWakeLockCountTotal);
        protoOutputStream.end(wmctToken);
        List<BatterySipper> sippers = helper.getUsageList();
        if (sippers != null) {
            int i8 = 0;
            while (i8 < sippers.size()) {
                BatterySipper bs = sippers.get(i8);
                int n2 = 0;
                int uid = 0;
                long wmctToken2 = wmctToken;
                switch (AnonymousClass2.$SwitchMap$com$android$internal$os$BatterySipper$DrainType[bs.drainType.ordinal()]) {
                    case 1:
                        n2 = 13;
                        break;
                    case 2:
                        n2 = 1;
                        break;
                    case 3:
                        n2 = 2;
                        break;
                    case 4:
                        n2 = 3;
                        break;
                    case 5:
                        n2 = 4;
                        break;
                    case 6:
                        n2 = 5;
                        break;
                    case 7:
                        n2 = 7;
                        break;
                    case 8:
                        n2 = 6;
                        break;
                    case 9:
                        List<BatterySipper> sippers2 = sippers;
                        int multicastWakeLockCountTotal2 = multicastWakeLockCountTotal;
                        long multicastWakeLockTimeTotalUs2 = multicastWakeLockTimeTotalUs;
                        break;
                    case 10:
                        n2 = 8;
                        uid = UserHandle.getUid(bs.userId, 0);
                        break;
                    case 11:
                        n = 9;
                        break;
                    case 12:
                        n = 10;
                        break;
                    case 13:
                        n = 11;
                        break;
                    case 14:
                        n = 12;
                        break;
                }
            }
        }
        long wmctToken3 = wmctToken;
        List<BatterySipper> sippers3 = sippers;
        int multicastWakeLockCountTotal3 = multicastWakeLockCountTotal;
        long j9 = multicastWakeLockTimeTotalUs;
        long pusToken = protoOutputStream.start(1146756268050L);
        protoOutputStream.write(1103806595073L, helper.getPowerProfile().getBatteryCapacity());
        protoOutputStream.write((long) SystemProto.PowerUseSummary.COMPUTED_POWER_MAH, helper.getComputedPower());
        protoOutputStream.write(1103806595075L, helper.getMinDrainedPower());
        protoOutputStream.write(1103806595076L, helper.getMaxDrainedPower());
        protoOutputStream.end(pusToken);
        Map<String, ? extends Timer> rpmStats = getRpmStats();
        Map<String, ? extends Timer> screenOffRpmStats = getScreenOffRpmStats();
        Iterator<Map.Entry<String, ? extends Timer>> it2 = rpmStats.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry<String, ? extends Timer> ent2 = it2.next();
            protoOutputStream.write(1138166333441L, ent2.getKey());
            long rawRealtimeUs12 = rawRealtimeUs11;
            long j10 = wmctToken3;
            long j11 = mToken2;
            List<BatterySipper> list = sippers3;
            Map<String, ? extends Timer> rpmStats2 = rpmStats;
            int i9 = multicastWakeLockCountTotal3;
            dumpTimer(protoOutputStream, 1146756268034L, (Timer) ent2.getValue(), rawRealtimeUs12, 0);
            Map.Entry<String, ? extends Timer> ent3 = ent2;
            Map<String, ? extends Timer> screenOffRpmStats2 = screenOffRpmStats;
            Map.Entry<String, ? extends Timer> entry2 = ent3;
            dumpTimer(protoOutputStream, 1146756268035L, (Timer) screenOffRpmStats2.get(ent3.getKey()), rawRealtimeUs12, 0);
            protoOutputStream.end(protoOutputStream.start(2246267895827L));
            it2 = it2;
            rpmStats = rpmStats2;
            screenOffRpmStats = screenOffRpmStats2;
        }
        Map<String, ? extends Timer> map2 = rpmStats;
        long rawRealtimeUs13 = rawRealtimeUs11;
        long j12 = mToken2;
        long j13 = wmctToken3;
        List<BatterySipper> list2 = sippers3;
        int i10 = multicastWakeLockCountTotal3;
        int i11 = 0;
        int i12 = 0;
        while (true) {
            int i13 = i12;
            if (i13 < 5) {
                long sbToken = protoOutputStream.start(2246267895828L);
                protoOutputStream.write(1159641169921L, i13);
                dumpTimer(protoOutputStream, 1146756268034L, getScreenBrightnessTimer(i13), rawRealtimeUs13, 0);
                protoOutputStream.end(sbToken);
                i12 = i13 + 1;
            } else {
                dumpTimer(protoOutputStream, 1146756268053L, getPhoneSignalScanningTimer(), rawRealtimeUs13, 0);
                int i14 = 0;
                while (true) {
                    int i15 = i14;
                    if (i15 < 5) {
                        long pssToken = protoOutputStream.start(2246267895824L);
                        protoOutputStream.write(1159641169921L, i15);
                        dumpTimer(protoOutputStream, 1146756268034L, getPhoneSignalStrengthTimer(i15), rawRealtimeUs13, 0);
                        protoOutputStream.end(pssToken);
                        i14 = i15 + 1;
                    } else {
                        Map<String, ? extends Timer> wakeupReasons = getWakeupReasonStats();
                        Iterator<Map.Entry<String, ? extends Timer>> it3 = wakeupReasons.entrySet().iterator();
                        while (it3.hasNext()) {
                            Map.Entry<String, ? extends Timer> ent4 = it3.next();
                            protoOutputStream.write(1138166333441L, ent4.getKey());
                            Map.Entry<String, ? extends Timer> entry3 = ent4;
                            dumpTimer(protoOutputStream, 1146756268034L, (Timer) ent4.getValue(), rawRealtimeUs13, 0);
                            protoOutputStream.end(protoOutputStream.start(2246267895830L));
                            it3 = it3;
                            wakeupReasons = wakeupReasons;
                        }
                        int i16 = 0;
                        while (true) {
                            int i17 = i16;
                            if (i17 < 5) {
                                long wssToken = protoOutputStream.start(SystemProto.WIFI_SIGNAL_STRENGTH);
                                protoOutputStream.write(1159641169921L, i17);
                                dumpTimer(protoOutputStream, 1146756268034L, getWifiSignalStrengthTimer(i17), rawRealtimeUs13, 0);
                                protoOutputStream.end(wssToken);
                                i16 = i17 + 1;
                            } else {
                                int i18 = 0;
                                while (true) {
                                    int i19 = i18;
                                    if (i19 < 8) {
                                        long wsToken = protoOutputStream.start(2246267895833L);
                                        protoOutputStream.write(1159641169921L, i19);
                                        dumpTimer(protoOutputStream, 1146756268034L, getWifiStateTimer(i19), rawRealtimeUs13, 0);
                                        protoOutputStream.end(wsToken);
                                        i18 = i19 + 1;
                                    } else {
                                        while (true) {
                                            int i20 = i11;
                                            if (i20 < 13) {
                                                long wssToken2 = protoOutputStream.start(2246267895834L);
                                                protoOutputStream.write(1159641169921L, i20);
                                                dumpTimer(protoOutputStream, 1146756268034L, getWifiSupplStateTimer(i20), rawRealtimeUs13, 0);
                                                protoOutputStream.end(wssToken2);
                                                i11 = i20 + 1;
                                            } else {
                                                protoOutputStream.end(sToken);
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
