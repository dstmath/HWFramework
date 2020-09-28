package android.os;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.app.backup.FullBackup;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.hardware.contexthub.V1_0.HostEndPoint;
import android.location.LocationManager;
import android.net.TrafficStats;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiScanLog;
import android.net.wifi.WifiScanner;
import android.provider.SettingsStringUtil;
import android.provider.Telephony;
import android.rms.HwSysResource;
import android.telephony.SignalStrength;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.util.ArrayMap;
import android.util.LongSparseArray;
import android.util.MutableBoolean;
import android.util.Pair;
import android.util.Printer;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import android.view.SurfaceControl;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.cdma.HwCustPlusAndIddNddConvertUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
    static final int CHECKIN_VERSION = 34;
    private static final String CPU_DATA = "cpu";
    private static final String CPU_TIMES_AT_FREQ_DATA = "ctf";
    private static final String DATA_CONNECTION_COUNT_DATA = "dcc";
    static final String[] DATA_CONNECTION_NAMES = {"none", "gprs", "edge", "umts", "cdma", "evdo_0", "evdo_A", "1xrtt", "hsdpa", "hsupa", "hspa", "iden", "evdo_b", "lte", "ehrpd", "hspap", "gsm", "td_scdma", "iwlan", "lte_ca", "nr", "other"};
    public static final int DATA_CONNECTION_NONE = 0;
    public static final int DATA_CONNECTION_OTHER = 21;
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
    public static final IntToString[] HISTORY_EVENT_INT_FORMATTERS;
    public static final String[] HISTORY_EVENT_NAMES = {"null", "proc", FOREGROUND_ACTIVITY_DATA, "top", "sync", "wake_lock_in", "job", "user", "userfg", "conn", "active", "pkginst", "pkgunin", "alarm", Context.STATS_MANAGER, "pkginactive", "pkgactive", "tmpwhitelist", "screenwake", "wakeupap", "longwake", "est_capacity"};
    public static final BitDescription[] HISTORY_STATE2_DESCRIPTIONS = {new BitDescription(Integer.MIN_VALUE, "power_save", "ps"), new BitDescription(1073741824, "video", Telephony.BaseMmsColumns.MMS_VERSION), new BitDescription(536870912, "wifi_running", "Ww"), new BitDescription(268435456, "wifi", "W"), new BitDescription(134217728, "flashlight", "fl"), new BitDescription(HistoryItem.STATE2_DEVICE_IDLE_MASK, 25, "device_idle", "di", new String[]{"off", "light", "full", "???"}, new String[]{"off", "light", "full", "???"}), new BitDescription(16777216, "charging", "ch"), new BitDescription(262144, "usb_data", "Ud"), new BitDescription(8388608, "phone_in_call", "Pcl"), new BitDescription(4194304, "bluetooth", "b"), new BitDescription(112, 4, "wifi_signal_strength", "Wss", new String[]{WifiEnterpriseConfig.ENGINE_DISABLE, "1", WifiScanLog.EVENT_KEY2, WifiScanLog.EVENT_KEY3, WifiScanLog.EVENT_KEY4}, new String[]{WifiEnterpriseConfig.ENGINE_DISABLE, "1", WifiScanLog.EVENT_KEY2, WifiScanLog.EVENT_KEY3, WifiScanLog.EVENT_KEY4}), new BitDescription(15, 0, "wifi_suppl", "Wsp", WIFI_SUPPL_STATE_NAMES, WIFI_SUPPL_STATE_SHORT_NAMES), new BitDescription(2097152, Context.CAMERA_SERVICE, "ca"), new BitDescription(1048576, "ble_scan", "bles"), new BitDescription(524288, "cellular_high_tx_power", "Chtp"), new BitDescription(128, 7, "gps_signal_quality", "Gss", new String[]{"poor", "good"}, new String[]{"poor", "good"})};
    public static final BitDescription[] HISTORY_STATE_DESCRIPTIONS;
    private static final String HISTORY_STRING_POOL = "hsp";
    public static final int JOB = 14;
    private static final String JOBS_DEFERRED_DATA = "jbd";
    private static final String JOB_COMPLETION_DATA = "jbc";
    private static final String JOB_DATA = "jb";
    public static final long[] JOB_FRESHNESS_BUCKETS = {3600000, 7200000, 14400000, 28800000, Long.MAX_VALUE};
    private static final String KERNEL_WAKELOCK_DATA = "kwl";
    private static final boolean LOCAL_LOGV = false;
    public static final int MAX_TRACKED_SCREEN_STATE = 4;
    public static final double MILLISECONDS_IN_HOUR = 3600000.0d;
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
    @UnsupportedAppUsage
    public static final int NUM_DATA_CONNECTION_TYPES = 22;
    public static final int NUM_NETWORK_ACTIVITY_TYPES = 10;
    @UnsupportedAppUsage
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
    @UnsupportedAppUsage
    @Deprecated
    public static final int STATS_CURRENT = 1;
    public static final int STATS_SINCE_CHARGED = 0;
    @Deprecated
    public static final int STATS_SINCE_UNPLUGGED = 2;
    private static final String[] STAT_NAMES = {"l", FullBackup.CACHE_TREE_TOKEN, "u"};
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
    @UnsupportedAppUsage
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
    private static final IntToString sIntToString = $$Lambda$q1UvBdLgHRZVzc68BxdksTmbuCw.INSTANCE;
    private static final IntToString sUidToString = $$Lambda$IyvVQC0mKtsfXbnO0kDL64hrk0.INSTANCE;
    private final StringBuilder mFormatBuilder = new StringBuilder(32);
    private final Formatter mFormatter = new Formatter(this.mFormatBuilder);

    public static abstract class ControllerActivityCounter {
        public abstract LongCounter getIdleTimeCounter();

        public abstract LongCounter getMonitoredRailChargeConsumedMaMs();

        public abstract LongCounter getPowerCounter();

        public abstract LongCounter getRxTimeCounter();

        public abstract LongCounter getScanTimeCounter();

        public abstract LongCounter getSleepTimeCounter();

        public abstract LongCounter[] getTxTimeCounters();
    }

    public static abstract class Counter {
        @UnsupportedAppUsage
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

    @FunctionalInterface
    public interface IntToString {
        String applyAsString(int i);
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

    public abstract void commitCurrentHistoryBatchLocked();

    @UnsupportedAppUsage
    public abstract long computeBatteryRealtime(long j, int i);

    public abstract long computeBatteryScreenOffRealtime(long j, int i);

    public abstract long computeBatteryScreenOffUptime(long j, int i);

    @UnsupportedAppUsage
    public abstract long computeBatteryTimeRemaining(long j);

    @UnsupportedAppUsage
    public abstract long computeBatteryUptime(long j, int i);

    @UnsupportedAppUsage
    public abstract long computeChargeTimeRemaining(long j);

    public abstract long computeRealtime(long j, int i);

    public abstract long computeUptime(long j, int i);

    public abstract void finishIteratingHistoryLocked();

    public abstract void finishIteratingOldHistoryLocked();

    public abstract long getBatteryRealtime(long j);

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public abstract long getPhoneOnTime(long j, int i);

    public abstract long getPhoneSignalScanningTime(long j, int i);

    public abstract Timer getPhoneSignalScanningTimer();

    public abstract int getPhoneSignalStrengthCount(int i, int i2);

    @UnsupportedAppUsage
    public abstract long getPhoneSignalStrengthTime(int i, long j, int i2);

    /* access modifiers changed from: protected */
    public abstract Timer getPhoneSignalStrengthTimer(int i);

    public abstract int getPowerSaveModeEnabledCount(int i);

    public abstract long getPowerSaveModeEnabledTime(long j, int i);

    public abstract Map<String, ? extends Timer> getRpmStats();

    @UnsupportedAppUsage
    public abstract long getScreenBrightnessTime(int i, long j, int i2);

    public abstract Timer getScreenBrightnessTimer(int i);

    public abstract int getScreenDozeCount(int i);

    public abstract long getScreenDozeTime(long j, int i);

    public abstract Map<String, ? extends Timer> getScreenOffRpmStats();

    public abstract int getScreenOnCount(int i);

    @UnsupportedAppUsage
    public abstract long getScreenOnTime(long j, int i);

    public abstract long getStartClockTime();

    public abstract int getStartCount();

    public abstract String getStartPlatformVersion();

    public abstract long getUahDischarge(int i);

    public abstract long getUahDischargeDeepDoze(int i);

    public abstract long getUahDischargeLightDoze(int i);

    public abstract long getUahDischargeScreenDoze(int i);

    public abstract long getUahDischargeScreenOff(int i);

    @UnsupportedAppUsage
    public abstract SparseArray<? extends Uid> getUidStats();

    public abstract Map<String, ? extends Timer> getWakeupReasonStats();

    public abstract long getWifiActiveTime(long j, int i);

    public abstract ControllerActivityCounter getWifiControllerActivity();

    public abstract int getWifiMulticastWakelockCount(int i);

    public abstract long getWifiMulticastWakelockTime(long j, int i);

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public abstract boolean startIteratingHistoryLocked();

    public abstract boolean startIteratingOldHistoryLocked();

    public abstract void writeToParcelWithoutUids(Parcel parcel, int i);

    static {
        String[] strArr = DATA_CONNECTION_NAMES;
        HISTORY_STATE_DESCRIPTIONS = new BitDescription[]{new BitDescription(Integer.MIN_VALUE, "running", "r"), new BitDescription(1073741824, "wake_lock", "w"), new BitDescription(8388608, Context.SENSOR_SERVICE, "s"), new BitDescription(536870912, LocationManager.GPS_PROVIDER, "g"), new BitDescription(268435456, "wifi_full_lock", "Wl"), new BitDescription(134217728, "wifi_scan", "Ws"), new BitDescription(65536, "wifi_multicast", "Wm"), new BitDescription(67108864, "wifi_radio", "Wr"), new BitDescription(33554432, "mobile_radio", "Pr"), new BitDescription(2097152, "phone_scanning", "Psc"), new BitDescription(4194304, "audio", FullBackup.APK_TREE_TOKEN), new BitDescription(1048576, "screen", "S"), new BitDescription(524288, BatteryManager.EXTRA_PLUGGED, "BP"), new BitDescription(262144, "screen_doze", "Sd"), new BitDescription(HistoryItem.STATE_DATA_CONNECTION_MASK, 9, "data_conn", "Pcn", strArr, strArr), new BitDescription(448, 6, "phone_state", "Pst", new String[]{"in", "out", PhoneConstants.APN_TYPE_EMERGENCY, "off"}, new String[]{"in", "out", "em", "off"}), new BitDescription(56, 3, "phone_signal_strength", "Pss", SignalStrength.SIGNAL_STRENGTH_NAMES, new String[]{WifiEnterpriseConfig.ENGINE_DISABLE, "1", WifiScanLog.EVENT_KEY2, WifiScanLog.EVENT_KEY3, WifiScanLog.EVENT_KEY4}), new BitDescription(7, 0, "brightness", "Sb", SCREEN_BRIGHTNESS_NAMES, SCREEN_BRIGHTNESS_SHORT_NAMES)};
        IntToString intToString = sUidToString;
        IntToString intToString2 = sIntToString;
        HISTORY_EVENT_INT_FORMATTERS = new IntToString[]{intToString, intToString, intToString, intToString, intToString, intToString, intToString, intToString, intToString, intToString, intToString, intToString2, intToString, intToString, intToString, intToString, intToString, intToString, intToString, intToString, intToString, intToString2};
    }

    public static abstract class Timer {
        @UnsupportedAppUsage
        public abstract int getCountLocked(int i);

        public abstract long getTimeSinceMarkLocked(long j);

        @UnsupportedAppUsage
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

    public static int mapToInternalProcessState(int procState) {
        if (procState == 21) {
            return 21;
        }
        if (procState == 2) {
            return 0;
        }
        if (ActivityManager.isForegroundService(procState)) {
            return 1;
        }
        if (procState <= 7) {
            return 2;
        }
        if (procState <= 12) {
            return 3;
        }
        if (procState <= 13) {
            return 4;
        }
        if (procState <= 14) {
            return 5;
        }
        return 6;
    }

    public static abstract class Uid {
        public static final int[] CRITICAL_PROC_STATES = {0, 3, 4, 1, 2};
        public static final int NUM_PROCESS_STATE = 7;
        public static final int NUM_USER_ACTIVITY_TYPES = USER_ACTIVITY_TYPES.length;
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
        static final String[] USER_ACTIVITY_TYPES = {"other", "button", "touch", Context.ACCESSIBILITY_SERVICE, Context.ATTENTION_SERVICE};

        public static abstract class Pkg {

            public static abstract class Serv {
                @UnsupportedAppUsage
                public abstract int getLaunches(int i);

                @UnsupportedAppUsage
                public abstract long getStartTime(long j, int i);

                @UnsupportedAppUsage
                public abstract int getStarts(int i);
            }

            @UnsupportedAppUsage
            public abstract ArrayMap<String, ? extends Serv> getServiceStats();

            @UnsupportedAppUsage
            public abstract ArrayMap<String, ? extends Counter> getWakeupAlarmStats();
        }

        public static abstract class Proc {

            public static class ExcessivePower {
                public static final int TYPE_CPU = 2;
                public static final int TYPE_WAKE = 1;
                @UnsupportedAppUsage
                public long overTime;
                @UnsupportedAppUsage
                public int type;
                @UnsupportedAppUsage
                public long usedTime;
            }

            @UnsupportedAppUsage
            public abstract int countExcessivePowers();

            @UnsupportedAppUsage
            public abstract ExcessivePower getExcessivePower(int i);

            @UnsupportedAppUsage
            public abstract long getForegroundTime(int i);

            public abstract int getNumAnrs(int i);

            public abstract int getNumCrashes(int i);

            @UnsupportedAppUsage
            public abstract int getStarts(int i);

            @UnsupportedAppUsage
            public abstract long getSystemTime(int i);

            @UnsupportedAppUsage
            public abstract long getUserTime(int i);

            public abstract boolean isActive();
        }

        public static abstract class Sensor {
            @UnsupportedAppUsage
            public static final int GPS = -10000;

            @UnsupportedAppUsage
            public abstract int getHandle();

            public abstract Timer getSensorBackgroundTime();

            @UnsupportedAppUsage
            public abstract Timer getSensorTime();
        }

        public static abstract class Wakelock {
            @UnsupportedAppUsage
            public abstract Timer getWakeTime(int i);
        }

        public abstract Timer getAggregatedPartialWakelockTimer();

        @UnsupportedAppUsage
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

        @UnsupportedAppUsage
        public abstract long getFullWifiLockTime(long j, int i);

        public abstract ArrayMap<String, SparseIntArray> getJobCompletionStats();

        public abstract ArrayMap<String, ? extends Timer> getJobStats();

        public abstract int getMobileRadioActiveCount(int i);

        @UnsupportedAppUsage
        public abstract long getMobileRadioActiveTime(int i);

        public abstract long getMobileRadioApWakeupCount(int i);

        public abstract ControllerActivityCounter getModemControllerActivity();

        public abstract Timer getMulticastWakelockStats();

        @UnsupportedAppUsage
        public abstract long getNetworkActivityBytes(int i, int i2);

        public abstract long getNetworkActivityPackets(int i, int i2);

        @UnsupportedAppUsage
        public abstract ArrayMap<String, ? extends Pkg> getPackageStats();

        public abstract SparseArray<? extends Pid> getPidStats();

        public abstract long getProcessStateTime(int i, long j, int i2);

        public abstract Timer getProcessStateTimer(int i);

        @UnsupportedAppUsage
        public abstract ArrayMap<String, ? extends Proc> getProcessStats();

        public abstract long[] getScreenOffCpuFreqTimes(int i);

        public abstract long[] getScreenOffCpuFreqTimes(int i, int i2);

        @UnsupportedAppUsage
        public abstract SparseArray<? extends Sensor> getSensorStats();

        public abstract ArrayMap<String, ? extends Timer> getSyncStats();

        public abstract long getSystemCpuTimeUs(int i);

        public abstract long getTimeAtCpuSpeed(int i, int i2, int i3);

        @UnsupportedAppUsage
        public abstract int getUid();

        public abstract int getUserActivityCount(int i, int i2);

        public abstract long getUserCpuTimeUs(int i);

        public abstract Timer getVibratorOnTimer();

        @UnsupportedAppUsage
        public abstract Timer getVideoTurnedOnTimer();

        @UnsupportedAppUsage
        public abstract ArrayMap<String, ? extends Wakelock> getWakelockStats();

        public abstract int getWifiBatchedScanCount(int i, int i2);

        @UnsupportedAppUsage
        public abstract long getWifiBatchedScanTime(int i, long j, int i2);

        public abstract ControllerActivityCounter getWifiControllerActivity();

        @UnsupportedAppUsage
        public abstract long getWifiMulticastTime(long j, int i);

        public abstract long getWifiRadioApWakeupCount(int i);

        @UnsupportedAppUsage
        public abstract long getWifiRunningTime(long j, int i);

        public abstract long getWifiScanActualTime(long j);

        public abstract int getWifiScanBackgroundCount(int i);

        public abstract long getWifiScanBackgroundTime(long j);

        public abstract Timer getWifiScanBackgroundTimer();

        public abstract int getWifiScanCount(int i);

        @UnsupportedAppUsage
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

        public class Pid {
            public int mWakeNesting;
            public long mWakeStartMs;
            public long mWakeSumMs;

            public Pid() {
            }
        }
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
                        out.append((char) ((digit + 97) - 10));
                    } else {
                        out.append((char) (digit + 48));
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
            int i = (initMode & 3) + 1;
            if (i == 1) {
                out.append('f');
            } else if (i == 2) {
                out.append('o');
            } else if (i == 3) {
                out.append(DateFormat.DATE);
            } else if (i == 4) {
                out.append(DateFormat.TIME_ZONE);
            }
            if ((initMode & 4) != 0) {
                out.append('p');
            }
            if ((initMode & 8) != 0) {
                out.append('i');
            }
            int i2 = (modMode & 3) + 1;
            if (i2 == 1) {
                out.append('F');
            } else if (i2 == 2) {
                out.append('O');
            } else if (i2 == 3) {
                out.append('D');
            } else if (i2 == 4) {
                out.append('Z');
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
            char c3;
            char c4;
            char c5;
            int N = value.length();
            int i = 0;
            long out = 0;
            while (true) {
                c = '-';
                if (i >= N || (c5 = value.charAt(i)) == '-') {
                    int i2 = i + 1;
                    long level = 0;
                } else {
                    i++;
                    if (c5 == 'D') {
                        out |= 144115188075855872L;
                    } else if (c5 == 'F') {
                        out |= 0;
                    } else if (c5 == 'I') {
                        out |= 576460752303423488L;
                    } else if (c5 == 'Z') {
                        out |= 216172782113783808L;
                    } else if (c5 == 'd') {
                        out |= 562949953421312L;
                    } else if (c5 == 'f') {
                        out |= 0;
                    } else if (c5 == 'i') {
                        out |= 2251799813685248L;
                    } else if (c5 == 'z') {
                        out |= 844424930131968L;
                    } else if (c5 == 'O') {
                        out |= 72057594037927936L;
                    } else if (c5 == 'P') {
                        out |= 288230376151711744L;
                    } else if (c5 == 'o') {
                        out |= 281474976710656L;
                    } else if (c5 == 'p') {
                        out |= TrafficStats.PB_IN_BYTES;
                    }
                }
            }
            int i22 = i + 1;
            long level2 = 0;
            while (true) {
                c2 = '9';
                c3 = 4;
                if (i22 < N) {
                    char c6 = value.charAt(i22);
                    if (c6 == '-') {
                        break;
                    }
                    i22++;
                    level2 <<= 4;
                    if (c6 >= '0' && c6 <= '9') {
                        level2 += (long) (c6 - '0');
                    } else if (c6 >= 'a' && c6 <= 'f') {
                        level2 += (long) ((c6 - 'a') + 10);
                    } else if (c6 >= 'A' && c6 <= 'F') {
                        level2 += (long) ((c6 - 'A') + 10);
                    }
                } else {
                    break;
                }
            }
            int i3 = i22 + 1;
            long out2 = out | ((level2 << 40) & BatteryStats.STEP_LEVEL_LEVEL_MASK);
            long duration = 0;
            while (true) {
                if (i3 < N) {
                    char c7 = value.charAt(i3);
                    if (c7 == c) {
                        break;
                    }
                    i3++;
                    duration <<= c3;
                    if (c7 >= '0' && c7 <= c2) {
                        duration += (long) (c7 - '0');
                        c = '-';
                        c2 = '9';
                        c3 = 4;
                    } else if (c7 < 'a' || c7 > 'f') {
                        if (c7 >= 'A') {
                            c4 = 'F';
                            if (c7 <= 'F') {
                                duration += (long) ((c7 - 'A') + 10);
                                c = '-';
                                c2 = '9';
                                c3 = 4;
                            }
                        } else {
                            c4 = 'F';
                        }
                        c = '-';
                        c2 = '9';
                        c3 = 4;
                    } else {
                        duration += (long) ((c7 - 'a') + 10);
                        c = '-';
                        c2 = '9';
                        c3 = 4;
                    }
                } else {
                    break;
                }
            }
            this.mStepDurations[index] = (BatteryStats.STEP_LEVEL_TIME_MASK & duration) | out2;
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
            long total = 0;
            int numOfInterest = 0;
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
            int stepCount = this.mNumStepDurations;
            long lastStepTime = this.mLastStepTime;
            if (lastStepTime >= 0 && numStepLevels > 0) {
                long[] steps = this.mStepDurations;
                long duration = elapsedRealtime - lastStepTime;
                for (int i = 0; i < numStepLevels; i++) {
                    System.arraycopy(steps, 0, steps, 1, steps.length - 1);
                    long thisDuration = duration / ((long) (numStepLevels - i));
                    duration -= thisDuration;
                    if (thisDuration > BatteryStats.STEP_LEVEL_TIME_MASK) {
                        thisDuration = BatteryStats.STEP_LEVEL_TIME_MASK;
                    }
                    steps[0] = thisDuration | modeBits;
                }
                stepCount += numStepLevels;
                if (stepCount > steps.length) {
                    stepCount = steps.length;
                }
            }
            this.mNumStepDurations = stepCount;
            this.mLastStepTime = elapsedRealtime;
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

    public static final class HistoryTag {
        public int poolIdx;
        public String string;
        public int uid;

        public void setTo(HistoryTag o) {
            String str = o.string;
            if (str == null) {
                str = "";
            }
            this.string = str;
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
            return (this.string.hashCode() * 31) + this.uid;
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

    public static final class HistoryItem implements Parcelable {
        public static final byte CMD_CURRENT_TIME = 5;
        public static final byte CMD_NULL = -1;
        public static final byte CMD_OVERFLOW = 6;
        public static final byte CMD_RESET = 7;
        public static final byte CMD_SHUTDOWN = 8;
        public static final byte CMD_START = 4;
        @UnsupportedAppUsage
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
        @UnsupportedAppUsage
        public byte batteryHealth;
        @UnsupportedAppUsage
        public byte batteryLevel;
        @UnsupportedAppUsage
        public byte batteryPlugType;
        @UnsupportedAppUsage
        public byte batteryStatus;
        public short batteryTemperature;
        @UnsupportedAppUsage
        public char batteryVoltage;
        @UnsupportedAppUsage
        public byte cmd = -1;
        public long currentTime;
        public int eventCode;
        public HistoryTag eventTag;
        public final HistoryTag localEventTag = new HistoryTag();
        public final HistoryTag localWakeReasonTag = new HistoryTag();
        public final HistoryTag localWakelockTag = new HistoryTag();
        public double modemRailChargeMah;
        public HistoryItem next;
        public int numReadInts;
        @UnsupportedAppUsage
        public int states;
        @UnsupportedAppUsage
        public int states2;
        public HistoryStepDetails stepDetails;
        @UnsupportedAppUsage
        public long time;
        public HistoryTag wakeReasonTag;
        public HistoryTag wakelockTag;
        public double wifiRailChargeMah;

        public boolean isDeltaData() {
            return this.cmd == 0;
        }

        @UnsupportedAppUsage
        public HistoryItem() {
        }

        public HistoryItem(long time2, Parcel src) {
            this.time = time2;
            this.numReadInts = 2;
            readFromParcel(src);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.time);
            int i = 0;
            int i2 = (this.cmd & 255) | ((this.batteryLevel << 8) & 65280) | ((this.batteryStatus << WifiScanner.PnoSettings.PnoNetwork.FLAG_SAME_NETWORK) & SurfaceControl.FX_SURFACE_MASK) | ((this.batteryHealth << 20) & 15728640) | ((this.batteryPlugType << 24) & 251658240) | (this.wakelockTag != null ? 268435456 : 0) | (this.wakeReasonTag != null ? 536870912 : 0);
            if (this.eventCode != 0) {
                i = 1073741824;
            }
            dest.writeInt(i2 | i);
            dest.writeInt((this.batteryTemperature & HostEndPoint.BROADCAST) | ((this.batteryVoltage << 16) & -65536));
            dest.writeInt(this.batteryChargeUAh);
            dest.writeDouble(this.modemRailChargeMah);
            dest.writeDouble(this.wifiRailChargeMah);
            dest.writeInt(this.states);
            dest.writeInt(this.states2);
            HistoryTag historyTag = this.wakelockTag;
            if (historyTag != null) {
                historyTag.writeToParcel(dest, flags);
            }
            HistoryTag historyTag2 = this.wakeReasonTag;
            if (historyTag2 != null) {
                historyTag2.writeToParcel(dest, flags);
            }
            int i3 = this.eventCode;
            if (i3 != 0) {
                dest.writeInt(i3);
                this.eventTag.writeToParcel(dest, flags);
            }
            byte b = this.cmd;
            if (b == 5 || b == 7) {
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
            this.modemRailChargeMah = src.readDouble();
            this.wifiRailChargeMah = src.readDouble();
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
            byte b = this.cmd;
            if (b == 5 || b == 7) {
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
            this.modemRailChargeMah = 0.0d;
            this.wifiRailChargeMah = 0.0d;
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
            this.modemRailChargeMah = o.modemRailChargeMah;
            this.wifiRailChargeMah = o.wifiRailChargeMah;
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
            return this.batteryLevel == o.batteryLevel && this.batteryStatus == o.batteryStatus && this.batteryHealth == o.batteryHealth && this.batteryPlugType == o.batteryPlugType && this.batteryTemperature == o.batteryTemperature && this.batteryVoltage == o.batteryVoltage && this.batteryChargeUAh == o.batteryChargeUAh && this.modemRailChargeMah == o.modemRailChargeMah && this.wifiRailChargeMah == o.wifiRailChargeMah && this.states == o.states && this.states2 == o.states2 && this.currentTime == o.currentTime;
        }

        public boolean same(HistoryItem o) {
            if (!sameNonEvent(o) || this.eventCode != o.eventCode) {
                return false;
            }
            HistoryTag historyTag = this.wakelockTag;
            HistoryTag historyTag2 = o.wakelockTag;
            if (historyTag != historyTag2 && (historyTag == null || historyTag2 == null || !historyTag.equals(historyTag2))) {
                return false;
            }
            HistoryTag historyTag3 = this.wakeReasonTag;
            HistoryTag historyTag4 = o.wakeReasonTag;
            if (historyTag3 != historyTag4 && (historyTag3 == null || historyTag4 == null || !historyTag3.equals(historyTag4))) {
                return false;
            }
            HistoryTag historyTag5 = this.eventTag;
            HistoryTag historyTag6 = o.eventTag;
            if (historyTag5 == historyTag6) {
                return true;
            }
            if (historyTag5 == null || historyTag6 == null || !historyTag5.equals(historyTag6)) {
                return false;
            }
            return true;
        }
    }

    public static final class HistoryEventTracker {
        private final HashMap<String, SparseIntArray>[] mActiveEvents = new HashMap[22];

        public boolean updateState(int code, String name, int uid, int poolIdx) {
            SparseIntArray uids;
            int idx;
            if ((32768 & code) != 0) {
                int idx2 = code & HistoryItem.EVENT_TYPE_MASK;
                HashMap<String, SparseIntArray> active = this.mActiveEvents[idx2];
                if (active == null) {
                    active = new HashMap<>();
                    this.mActiveEvents[idx2] = active;
                }
                SparseIntArray uids2 = active.get(name);
                if (uids2 == null) {
                    uids2 = new SparseIntArray();
                    active.put(name, uids2);
                }
                if (uids2.indexOfKey(uid) >= 0) {
                    return false;
                }
                uids2.put(uid, poolIdx);
                return true;
            } else if ((code & 16384) == 0) {
                return true;
            } else {
                HashMap<String, SparseIntArray> active2 = this.mActiveEvents[code & HistoryItem.EVENT_TYPE_MASK];
                if (active2 == null || (uids = active2.get(name)) == null || (idx = uids.indexOfKey(uid)) < 0) {
                    return false;
                }
                uids.removeAt(idx);
                if (uids.size() > 0) {
                    return true;
                }
                active2.remove(name);
                return true;
            }
        }

        public void removeEvents(int code) {
            this.mActiveEvents[-49153 & code] = null;
        }

        public HashMap<String, SparseIntArray> getStateForEvent(int code) {
            return this.mActiveEvents[code];
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
        this.mFormatter.format("%.1f%%", Float.valueOf((((float) num) / ((float) den)) * 100.0f));
        return this.mFormatBuilder.toString();
    }

    /* access modifiers changed from: package-private */
    public final String formatBytesLocked(long bytes) {
        this.mFormatBuilder.setLength(0);
        if (bytes < 1024) {
            return bytes + "B";
        } else if (bytes < 1048576) {
            this.mFormatter.format("%.2fKB", Double.valueOf(((double) bytes) / 1024.0d));
            return this.mFormatBuilder.toString();
        } else if (bytes < 1073741824) {
            this.mFormatter.format("%.2fMB", Double.valueOf(((double) bytes) / 1048576.0d));
            return this.mFormatBuilder.toString();
        } else {
            this.mFormatter.format("%.2fGB", Double.valueOf(((double) bytes) / 1.073741824E9d));
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
        if (timer != null) {
            long totalTimeMillis = computeWakeLock(timer, elapsedRealtimeUs, which);
            int count = timer.getCountLocked(which);
            if (totalTimeMillis != 0) {
                sb.append(linePrefix);
                formatTimeMs(sb, totalTimeMillis);
                if (name != null) {
                    sb.append(name);
                    sb.append(' ');
                }
                sb.append('(');
                sb.append(count);
                sb.append(" times)");
                long maxDurationMs = timer.getMaxDurationMsLocked(elapsedRealtimeUs / 1000);
                if (maxDurationMs >= 0) {
                    sb.append(" max=");
                    sb.append(maxDurationMs);
                }
                long totalDurMs = timer.getTotalDurationMsLocked(elapsedRealtimeUs / 1000);
                if (totalDurMs > totalTimeMillis) {
                    sb.append(" actual=");
                    sb.append(totalDurMs);
                }
                if (!timer.isRunningLocked()) {
                    return ", ";
                }
                long currentMs = timer.getCurrentDurationMsLocked(elapsedRealtimeUs / 1000);
                if (currentMs >= 0) {
                    sb.append(" (running for ");
                    sb.append(currentMs);
                    sb.append("ms)");
                    return ", ";
                }
                sb.append(" (running)");
                return ", ";
            }
        }
        return linePrefix;
    }

    private static final boolean printTimer(PrintWriter pw, StringBuilder sb, Timer timer, long rawRealtimeUs, int which, String prefix, String type) {
        if (timer != null) {
            long totalTimeMs = (timer.getTotalTimeLocked(rawRealtimeUs, which) + 500) / 1000;
            int count = timer.getCountLocked(which);
            if (totalTimeMs != 0) {
                sb.setLength(0);
                sb.append(prefix);
                sb.append("    ");
                sb.append(type);
                sb.append(": ");
                formatTimeMs(sb, totalTimeMs);
                sb.append("realtime (");
                sb.append(count);
                sb.append(" times)");
                long maxDurationMs = timer.getMaxDurationMsLocked(rawRealtimeUs / 1000);
                if (maxDurationMs >= 0) {
                    sb.append(" max=");
                    sb.append(maxDurationMs);
                }
                if (timer.isRunningLocked()) {
                    long currentMs = timer.getCurrentDurationMsLocked(rawRealtimeUs / 1000);
                    if (currentMs >= 0) {
                        sb.append(" (running for ");
                        sb.append(currentMs);
                        sb.append("ms)");
                    } else {
                        sb.append(" (running)");
                    }
                }
                pw.println(sb.toString());
                return true;
            }
        }
        return false;
    }

    private static final String printWakeLockCheckin(StringBuilder sb, Timer timer, long elapsedRealtimeUs, String name, int which, String linePrefix) {
        long totalTimeMicros;
        String str;
        int count = 0;
        long max = 0;
        long current = 0;
        long totalDuration = 0;
        if (timer != null) {
            long totalTimeMicros2 = timer.getTotalTimeLocked(elapsedRealtimeUs, which);
            count = timer.getCountLocked(which);
            totalTimeMicros = totalTimeMicros2;
            current = timer.getCurrentDurationMsLocked(elapsedRealtimeUs / 1000);
            max = timer.getMaxDurationMsLocked(elapsedRealtimeUs / 1000);
            totalDuration = timer.getTotalDurationMsLocked(elapsedRealtimeUs / 1000);
        } else {
            totalTimeMicros = 0;
        }
        sb.append(linePrefix);
        sb.append((totalTimeMicros + 500) / 1000);
        sb.append(',');
        if (name != null) {
            str = name + SmsManager.REGEX_PREFIX_DELIMITER;
        } else {
            str = "";
        }
        sb.append(str);
        sb.append(count);
        sb.append(',');
        sb.append(current);
        sb.append(',');
        sb.append(max);
        if (name != null) {
            sb.append(',');
            sb.append(totalDuration);
        }
        return SmsManager.REGEX_PREFIX_DELIMITER;
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

    @UnsupportedAppUsage
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
        if (timer != null) {
            long timeMs = roundUsToMs(timer.getTotalTimeLocked(rawRealtimeUs, which));
            int count = timer.getCountLocked(which);
            long maxDurationMs = timer.getMaxDurationMsLocked(rawRealtimeUs / 1000);
            long curDurationMs = timer.getCurrentDurationMsLocked(rawRealtimeUs / 1000);
            long totalDurationMs = timer.getTotalDurationMsLocked(rawRealtimeUs / 1000);
            if (timeMs != 0 || count != 0 || maxDurationMs != -1 || curDurationMs != -1 || totalDurationMs != -1) {
                long token = proto.start(fieldId);
                proto.write(1112396529665L, timeMs);
                proto.write(1112396529666L, count);
                if (maxDurationMs != -1) {
                    proto.write(1112396529667L, maxDurationMs);
                }
                if (curDurationMs != -1) {
                    proto.write(1112396529668L, curDurationMs);
                }
                if (totalDurationMs != -1) {
                    proto.write(1112396529669L, totalDurationMs);
                }
                proto.end(token);
            }
        }
    }

    private static boolean controllerActivityHasData(ControllerActivityCounter counter, int which) {
        if (counter == null) {
            return false;
        }
        if (!(counter.getIdleTimeCounter().getCountLocked(which) == 0 && counter.getRxTimeCounter().getCountLocked(which) == 0 && counter.getPowerCounter().getCountLocked(which) == 0 && counter.getMonitoredRailChargeConsumedMaMs().getCountLocked(which) == 0)) {
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
            pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
            pw.print(counter.getIdleTimeCounter().getCountLocked(which));
            pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
            pw.print(counter.getRxTimeCounter().getCountLocked(which));
            pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
            pw.print(((double) counter.getPowerCounter().getCountLocked(which)) / 3600000.0d);
            pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
            pw.print(((double) counter.getMonitoredRailChargeConsumedMaMs().getCountLocked(which)) / 3600000.0d);
            LongCounter[] txTimeCounters = counter.getTxTimeCounters();
            for (LongCounter c : txTimeCounters) {
                pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
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
            proto.write(1112396529667L, ((double) counter.getPowerCounter().getCountLocked(which)) / 3600000.0d);
            proto.write(1103806595077L, ((double) counter.getMonitoredRailChargeConsumedMaMs().getCountLocked(which)) / 3600000.0d);
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

    /* JADX INFO: Multiple debug info for r11v11 long: [D('sleepTimeMs' long), D('scanTimeMs' long)] */
    private final void printControllerActivity(PrintWriter pw, StringBuilder sb, String prefix, String controllerName, ControllerActivityCounter counter, int which) {
        long rxTimeMs;
        String str;
        Object obj;
        int i;
        String[] powerLevel;
        long powerDrainMaMs;
        long idleTimeMs = counter.getIdleTimeCounter().getCountLocked(which);
        long rxTimeMs2 = counter.getRxTimeCounter().getCountLocked(which);
        long powerDrainMaMs2 = counter.getPowerCounter().getCountLocked(which);
        long monitoredRailChargeConsumedMaMs = counter.getMonitoredRailChargeConsumedMaMs().getCountLocked(which);
        long totalControllerActivityTimeMs = computeBatteryRealtime(SystemClock.elapsedRealtime() * 1000, which) / 1000;
        long totalTxTimeMs = 0;
        LongCounter[] txTimeCounters = counter.getTxTimeCounters();
        int i2 = 0;
        for (int length = txTimeCounters.length; i2 < length; length = length) {
            totalTxTimeMs += txTimeCounters[i2].getCountLocked(which);
            i2++;
        }
        if (controllerName.equals(WIFI_CONTROLLER_NAME)) {
            long scanTimeMs = counter.getScanTimeCounter().getCountLocked(which);
            sb.setLength(0);
            sb.append(prefix);
            sb.append("     ");
            sb.append(controllerName);
            sb.append(" Scan time:  ");
            formatTimeMs(sb, scanTimeMs);
            sb.append("(");
            sb.append(formatRatioLocked(scanTimeMs, totalControllerActivityTimeMs));
            sb.append(")");
            pw.println(sb.toString());
            long scanTimeMs2 = totalControllerActivityTimeMs - ((idleTimeMs + rxTimeMs2) + totalTxTimeMs);
            sb.setLength(0);
            sb.append(prefix);
            sb.append("     ");
            sb.append(controllerName);
            str = " Sleep time:  ";
            sb.append(str);
            formatTimeMs(sb, scanTimeMs2);
            sb.append("(");
            rxTimeMs = rxTimeMs2;
            sb.append(formatRatioLocked(scanTimeMs2, totalControllerActivityTimeMs));
            sb.append(")");
            pw.println(sb.toString());
        } else {
            rxTimeMs = rxTimeMs2;
            str = " Sleep time:  ";
        }
        if (controllerName.equals(CELLULAR_CONTROLLER_NAME)) {
            i = which;
            long sleepTimeMs = counter.getSleepTimeCounter().getCountLocked(i);
            obj = CELLULAR_CONTROLLER_NAME;
            sb.setLength(0);
            sb.append(prefix);
            sb.append("     ");
            sb.append(controllerName);
            sb.append(str);
            formatTimeMs(sb, sleepTimeMs);
            sb.append("(");
            sb.append(formatRatioLocked(sleepTimeMs, totalControllerActivityTimeMs));
            sb.append(")");
            pw.println(sb.toString());
        } else {
            i = which;
            obj = CELLULAR_CONTROLLER_NAME;
        }
        sb.setLength(0);
        sb.append(prefix);
        sb.append("     ");
        sb.append(controllerName);
        sb.append(" Idle time:   ");
        formatTimeMs(sb, idleTimeMs);
        sb.append("(");
        sb.append(formatRatioLocked(idleTimeMs, totalControllerActivityTimeMs));
        sb.append(")");
        pw.println(sb.toString());
        sb.setLength(0);
        sb.append(prefix);
        sb.append("     ");
        sb.append(controllerName);
        sb.append(" Rx time:     ");
        formatTimeMs(sb, rxTimeMs);
        sb.append("(");
        sb.append(formatRatioLocked(rxTimeMs, totalControllerActivityTimeMs));
        sb.append(")");
        pw.println(sb.toString());
        sb.setLength(0);
        sb.append(prefix);
        sb.append("     ");
        sb.append(controllerName);
        sb.append(" Tx time:     ");
        if (((controllerName.hashCode() == -851952246 && controllerName.equals(obj)) ? (char) 0 : 65535) != 0) {
            powerLevel = new String[]{"[0]", "[1]", "[2]", "[3]", "[4]"};
        } else {
            powerLevel = new String[]{"   less than 0dBm: ", "   0dBm to 8dBm: ", "   8dBm to 15dBm: ", "   15dBm to 20dBm: ", "   above 20dBm: "};
        }
        int numTxLvls = Math.min(counter.getTxTimeCounters().length, powerLevel.length);
        if (numTxLvls > 1) {
            pw.println(sb.toString());
            for (int lvl = 0; lvl < numTxLvls; lvl++) {
                long txLvlTimeMs = counter.getTxTimeCounters()[lvl].getCountLocked(i);
                sb.setLength(0);
                sb.append(prefix);
                sb.append("    ");
                sb.append(powerLevel[lvl]);
                sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                formatTimeMs(sb, txLvlTimeMs);
                sb.append("(");
                sb.append(formatRatioLocked(txLvlTimeMs, totalControllerActivityTimeMs));
                sb.append(")");
                pw.println(sb.toString());
            }
        } else {
            long txLvlTimeMs2 = counter.getTxTimeCounters()[0].getCountLocked(i);
            formatTimeMs(sb, txLvlTimeMs2);
            sb.append("(");
            sb.append(formatRatioLocked(txLvlTimeMs2, totalControllerActivityTimeMs));
            sb.append(")");
            pw.println(sb.toString());
        }
        if (powerDrainMaMs2 > 0) {
            sb.setLength(0);
            sb.append(prefix);
            sb.append("     ");
            sb.append(controllerName);
            sb.append(" Battery drain: ");
            powerDrainMaMs = powerDrainMaMs2;
            sb.append(BatteryStatsHelper.makemAh(((double) powerDrainMaMs) / 3600000.0d));
            sb.append("mAh");
            pw.println(sb.toString());
        } else {
            powerDrainMaMs = powerDrainMaMs2;
        }
        if (monitoredRailChargeConsumedMaMs > 0) {
            sb.setLength(0);
            sb.append(prefix);
            sb.append("     ");
            sb.append(controllerName);
            sb.append(" Monitored rail energy drain: ");
            sb.append(new DecimalFormat("#.##").format(((double) monitoredRailChargeConsumedMaMs) / 3600000.0d));
            sb.append(" mAh");
            pw.println(sb.toString());
        }
    }

    public final void dumpCheckinLocked(Context context, PrintWriter pw, int which, int reqUid) {
        dumpCheckinLocked(context, pw, which, reqUid, BatteryStatsHelper.checkWifiOnly(context));
    }

    /*  JADX ERROR: IndexOutOfBoundsException in pass: SSATransform
        java.lang.IndexOutOfBoundsException: bitIndex < 0: -89
        	at java.util.BitSet.get(BitSet.java:623)
        	at jadx.core.dex.visitors.ssa.LiveVarAnalysis.fillBasicBlockInfo(LiveVarAnalysis.java:65)
        	at jadx.core.dex.visitors.ssa.LiveVarAnalysis.runAnalysis(LiveVarAnalysis.java:36)
        	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:50)
        	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:41)
        */
    /*  JADX ERROR: NullPointerException in pass: ConstInlineVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ConstInlineVisitor.checkForFinallyBlock(ConstInlineVisitor.java:111)
        	at jadx.core.dex.visitors.ConstInlineVisitor.checkInsn(ConstInlineVisitor.java:102)
        	at jadx.core.dex.visitors.ConstInlineVisitor.process(ConstInlineVisitor.java:52)
        	at jadx.core.dex.visitors.ConstInlineVisitor.visit(ConstInlineVisitor.java:44)
        */
    /*  JADX ERROR: NullPointerException in pass: ConstructorVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.instructions.mods.ConstructorInsn.<init>(ConstructorInsn.java:47)
        	at jadx.core.dex.visitors.ConstructorVisitor.processInvoke(ConstructorVisitor.java:64)
        	at jadx.core.dex.visitors.ConstructorVisitor.replaceInvoke(ConstructorVisitor.java:48)
        	at jadx.core.dex.visitors.ConstructorVisitor.visit(ConstructorVisitor.java:37)
        */
    /*  JADX ERROR: NullPointerException in pass: InitCodeVariables
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.InitCodeVariables.initCodeVar(InitCodeVariables.java:56)
        	at jadx.core.dex.visitors.InitCodeVariables.initCodeVars(InitCodeVariables.java:45)
        	at jadx.core.dex.visitors.InitCodeVariables.visit(InitCodeVariables.java:32)
        */
    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getFirstUseSkipMove(ModVisitor.java:533)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:124)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:93)
        */
    /*  JADX ERROR: NullPointerException in pass: MoveInlineVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.MoveInlineVisitor.processMove(MoveInlineVisitor.java:54)
        	at jadx.core.dex.visitors.MoveInlineVisitor.moveInline(MoveInlineVisitor.java:39)
        	at jadx.core.dex.visitors.MoveInlineVisitor.visit(MoveInlineVisitor.java:30)
        */
    /*  JADX ERROR: NullPointerException in pass: PrepareForCodeGen
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.PrepareForCodeGen.removeInstructions(PrepareForCodeGen.java:98)
        	at jadx.core.dex.visitors.PrepareForCodeGen.visit(PrepareForCodeGen.java:68)
        */
    /*  JADX ERROR: NullPointerException in pass: RegionMakerVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.regions.IfMakerHelper.getNextIfNodeInfo(IfMakerHelper.java:401)
        	at jadx.core.dex.visitors.regions.IfMakerHelper.getNextIf(IfMakerHelper.java:359)
        	at jadx.core.dex.visitors.regions.IfMakerHelper.mergeNestedIfNodes(IfMakerHelper.java:163)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:664)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:125)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:88)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:50)
        */
    /*  JADX ERROR: NullPointerException in pass: SimplifyVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.SimplifyVisitor.collectUseChain(SimplifyVisitor.java:312)
        	at jadx.core.dex.visitors.SimplifyVisitor.convertInvoke(SimplifyVisitor.java:303)
        	at jadx.core.dex.visitors.SimplifyVisitor.simplifyInsn(SimplifyVisitor.java:141)
        	at jadx.core.dex.visitors.SimplifyVisitor.simplifyBlock(SimplifyVisitor.java:83)
        	at jadx.core.dex.visitors.SimplifyVisitor.visit(SimplifyVisitor.java:68)
        */
    public final void dumpCheckinLocked(android.content.Context r202, java.io.PrintWriter r203, int r204, int r205, boolean r206) {
        /*
            r201 = this;
            r0 = r201
            r9 = r203
            r10 = r204
            r11 = r205
            r12 = 1
            r13 = 0
            java.lang.Integer r14 = java.lang.Integer.valueOf(r13)
            if (r10 == 0) goto L_0x0034
            java.lang.String[] r1 = android.os.BatteryStats.STAT_NAMES
            r1 = r1[r10]
            java.lang.Object[] r2 = new java.lang.Object[r12]
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "ERROR: BatteryStats.dumpCheckin called for which type "
            r3.append(r4)
            r3.append(r10)
            java.lang.String r4 = " but only STATS_SINCE_CHARGED is supported."
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            r2[r13] = r3
            java.lang.String r3 = "err"
            dumpLine(r9, r13, r1, r3, r2)
            return
        L_0x0034:
            long r1 = android.os.SystemClock.uptimeMillis()
            r15 = 1000(0x3e8, double:4.94E-321)
            long r7 = r1 * r15
            long r5 = android.os.SystemClock.elapsedRealtime()
            long r3 = r5 * r15
            long r1 = r0.getBatteryUptime(r7)
            long r17 = r0.computeBatteryUptime(r7, r10)
            long r19 = r0.computeBatteryRealtime(r3, r10)
            long r21 = r0.computeBatteryScreenOffUptime(r7, r10)
            long r23 = r0.computeBatteryScreenOffRealtime(r3, r10)
            long r25 = r0.computeRealtime(r3, r10)
            long r27 = r0.computeUptime(r7, r10)
            long r29 = r0.getScreenOnTime(r3, r10)
            long r31 = r0.getScreenDozeTime(r3, r10)
            long r33 = r0.getInteractiveTime(r3, r10)
            long r35 = r0.getPowerSaveModeEnabledTime(r3, r10)
            long r37 = r0.getDeviceIdleModeTime(r12, r3, r10)
            r39 = r1
            r2 = 2
            long r41 = r0.getDeviceIdleModeTime(r2, r3, r10)
            long r43 = r0.getDeviceIdlingTime(r12, r3, r10)
            long r45 = r0.getDeviceIdlingTime(r2, r3, r10)
            int r47 = r0.getNumConnectivityChange(r10)
            long r48 = r0.getPhoneOnTime(r3, r10)
            long r50 = r0.getUahDischarge(r10)
            long r52 = r0.getUahDischargeScreenOff(r10)
            long r54 = r0.getUahDischargeScreenDoze(r10)
            long r56 = r0.getUahDischargeLightDoze(r10)
            long r58 = r0.getUahDischargeDeepDoze(r10)
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r2 = 128(0x80, float:1.794E-43)
            r1.<init>(r2)
            r2 = r1
            android.util.SparseArray r1 = r201.getUidStats()
            r61 = r2
            int r2 = r1.size()
            java.lang.String[] r62 = android.os.BatteryStats.STAT_NAMES
            r12 = r62[r10]
            r15 = 12
            java.lang.Object[] r15 = new java.lang.Object[r15]
            if (r10 != 0) goto L_0x00c2
            int r16 = r201.getStartCount()
            java.lang.Integer r16 = java.lang.Integer.valueOf(r16)
            goto L_0x00c4
        L_0x00c2:
            java.lang.String r16 = "N/A"
        L_0x00c4:
            r15[r13] = r16
            r64 = 1000(0x3e8, double:4.94E-321)
            long r66 = r19 / r64
            java.lang.Long r16 = java.lang.Long.valueOf(r66)
            r62 = 1
            r15[r62] = r16
            long r66 = r17 / r64
            java.lang.Long r16 = java.lang.Long.valueOf(r66)
            r60 = 2
            r15[r60] = r16
            long r66 = r25 / r64
            java.lang.Long r16 = java.lang.Long.valueOf(r66)
            r13 = 3
            r15[r13] = r16
            long r66 = r27 / r64
            java.lang.Long r16 = java.lang.Long.valueOf(r66)
            r13 = 4
            r15[r13] = r16
            long r67 = r201.getStartClockTime()
            java.lang.Long r16 = java.lang.Long.valueOf(r67)
            r13 = 5
            r15[r13] = r16
            long r68 = r23 / r64
            java.lang.Long r16 = java.lang.Long.valueOf(r68)
            r13 = 6
            r15[r13] = r16
            long r69 = r21 / r64
            java.lang.Long r16 = java.lang.Long.valueOf(r69)
            r13 = 7
            r15[r13] = r16
            int r16 = r201.getEstimatedBatteryCapacity()
            java.lang.Integer r16 = java.lang.Integer.valueOf(r16)
            r13 = 8
            r15[r13] = r16
            int r16 = r201.getMinLearnedBatteryCapacity()
            java.lang.Integer r16 = java.lang.Integer.valueOf(r16)
            r13 = 9
            r15[r13] = r16
            r16 = 10
            int r72 = r201.getMaxLearnedBatteryCapacity()
            java.lang.Integer r72 = java.lang.Integer.valueOf(r72)
            r15[r16] = r72
            r16 = 11
            r64 = 1000(0x3e8, double:4.94E-321)
            long r72 = r31 / r64
            java.lang.Long r72 = java.lang.Long.valueOf(r72)
            r15[r16] = r72
            java.lang.String r13 = "bt"
            r72 = r5
            r5 = 0
            dumpLine(r9, r5, r12, r13, r15)
            r5 = 0
            r74 = 0
            r13 = 0
            r76 = r74
            r74 = r5
        L_0x014c:
            if (r13 >= r2) goto L_0x019b
            java.lang.Object r5 = r1.valueAt(r13)
            android.os.BatteryStats$Uid r5 = (android.os.BatteryStats.Uid) r5
            android.util.ArrayMap r6 = r5.getWakelockStats()
            int r15 = r6.size()
            r78 = r1
            r1 = 1
            int r15 = r15 - r1
        L_0x0161:
            if (r15 < 0) goto L_0x0192
            java.lang.Object r63 = r6.valueAt(r15)
            r79 = r2
            r2 = r63
            android.os.BatteryStats$Uid$Wakelock r2 = (android.os.BatteryStats.Uid.Wakelock) r2
            r80 = r5
            android.os.BatteryStats$Timer r5 = r2.getWakeTime(r1)
            if (r5 == 0) goto L_0x017b
            long r81 = r5.getTotalTimeLocked(r3, r10)
            long r74 = r74 + r81
        L_0x017b:
            r81 = r5
            r1 = 0
            android.os.BatteryStats$Timer r5 = r2.getWakeTime(r1)
            if (r5 == 0) goto L_0x018a
            long r82 = r5.getTotalTimeLocked(r3, r10)
            long r76 = r76 + r82
        L_0x018a:
            int r15 = r15 + -1
            r2 = r79
            r5 = r80
            r1 = 1
            goto L_0x0161
        L_0x0192:
            r79 = r2
            r80 = r5
            int r13 = r13 + 1
            r1 = r78
            goto L_0x014c
        L_0x019b:
            r78 = r1
            r79 = r2
            r1 = 0
            long r80 = r0.getNetworkActivityBytes(r1, r10)
            r2 = 1
            long r82 = r0.getNetworkActivityBytes(r2, r10)
            r5 = 2
            long r84 = r0.getNetworkActivityBytes(r5, r10)
            r6 = 3
            long r86 = r0.getNetworkActivityBytes(r6, r10)
            long r88 = r0.getNetworkActivityPackets(r1, r10)
            long r90 = r0.getNetworkActivityPackets(r2, r10)
            long r92 = r0.getNetworkActivityPackets(r5, r10)
            long r94 = r0.getNetworkActivityPackets(r6, r10)
            r1 = 4
            long r96 = r0.getNetworkActivityBytes(r1, r10)
            r1 = 5
            long r98 = r0.getNetworkActivityBytes(r1, r10)
            r1 = 10
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.Long r2 = java.lang.Long.valueOf(r80)
            r5 = 0
            r1[r5] = r2
            java.lang.Long r2 = java.lang.Long.valueOf(r82)
            r5 = 1
            r1[r5] = r2
            java.lang.Long r2 = java.lang.Long.valueOf(r84)
            r5 = 2
            r1[r5] = r2
            java.lang.Long r2 = java.lang.Long.valueOf(r86)
            r6 = 3
            r1[r6] = r2
            java.lang.Long r2 = java.lang.Long.valueOf(r88)
            r6 = 4
            r1[r6] = r2
            java.lang.Long r2 = java.lang.Long.valueOf(r90)
            r6 = 5
            r1[r6] = r2
            java.lang.Long r2 = java.lang.Long.valueOf(r92)
            r6 = 6
            r1[r6] = r2
            java.lang.Long r2 = java.lang.Long.valueOf(r94)
            r6 = 7
            r1[r6] = r2
            java.lang.Long r2 = java.lang.Long.valueOf(r96)
            r6 = 8
            r1[r6] = r2
            java.lang.Long r2 = java.lang.Long.valueOf(r98)
            r6 = 9
            r1[r6] = r2
            java.lang.String r2 = "gn"
            r6 = 0
            dumpLine(r9, r6, r12, r2, r1)
            r2 = 0
            android.os.BatteryStats$ControllerActivityCounter r6 = r201.getModemControllerActivity()
            java.lang.String r13 = "gmcd"
            r100 = r39
            r15 = r78
            r1 = r203
            r39 = r7
            r8 = r79
            r7 = r5
            r5 = r61
            r7 = r3
            r3 = r12
            r4 = r13
            r13 = r5
            r102 = r72
            r5 = r6
            r6 = r204
            dumpControllerActivityLine(r1, r2, r3, r4, r5, r6)
            long r72 = r0.getWifiOnTime(r7, r10)
            long r104 = r0.getGlobalWifiRunningTime(r7, r10)
            r1 = 5
            java.lang.Object[] r2 = new java.lang.Object[r1]
            r3 = 1000(0x3e8, double:4.94E-321)
            long r5 = r72 / r3
            java.lang.Long r1 = java.lang.Long.valueOf(r5)
            r5 = 0
            r2[r5] = r1
            long r61 = r104 / r3
            java.lang.Long r1 = java.lang.Long.valueOf(r61)
            r3 = 1
            r2[r3] = r1
            r1 = 2
            r2[r1] = r14
            r1 = 3
            r2[r1] = r14
            r1 = 4
            r2[r1] = r14
            java.lang.String r1 = "gwfl"
            dumpLine(r9, r5, r12, r1, r2)
            r2 = 0
            android.os.BatteryStats$ControllerActivityCounter r5 = r201.getWifiControllerActivity()
            java.lang.String r4 = "gwfcd"
            r1 = r203
            r3 = r12
            r6 = r204
            dumpControllerActivityLine(r1, r2, r3, r4, r5, r6)
            android.os.BatteryStats$ControllerActivityCounter r5 = r201.getBluetoothControllerActivity()
            java.lang.String r4 = "gble"
            dumpControllerActivityLine(r1, r2, r3, r4, r5, r6)
            r1 = 21
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r2 = 1000(0x3e8, double:4.94E-321)
            long r4 = r29 / r2
            java.lang.Long r4 = java.lang.Long.valueOf(r4)
            r5 = 0
            r1[r5] = r4
            long r4 = r48 / r2
            java.lang.Long r4 = java.lang.Long.valueOf(r4)
            r5 = 1
            r1[r5] = r4
            long r4 = r74 / r2
            java.lang.Long r4 = java.lang.Long.valueOf(r4)
            r5 = 2
            r1[r5] = r4
            long r4 = r76 / r2
            java.lang.Long r4 = java.lang.Long.valueOf(r4)
            r5 = 3
            r1[r5] = r4
            long r4 = r0.getMobileRadioActiveTime(r7, r10)
            long r4 = r4 / r2
            java.lang.Long r4 = java.lang.Long.valueOf(r4)
            r5 = 4
            r1[r5] = r4
            long r4 = r0.getMobileRadioActiveAdjustedTime(r10)
            long r4 = r4 / r2
            java.lang.Long r4 = java.lang.Long.valueOf(r4)
            r5 = 5
            r1[r5] = r4
            long r4 = r33 / r2
            java.lang.Long r4 = java.lang.Long.valueOf(r4)
            r5 = 6
            r1[r5] = r4
            long r4 = r35 / r2
            java.lang.Long r4 = java.lang.Long.valueOf(r4)
            r5 = 7
            r1[r5] = r4
            java.lang.Integer r4 = java.lang.Integer.valueOf(r47)
            r5 = 8
            r1[r5] = r4
            long r4 = r41 / r2
            java.lang.Long r4 = java.lang.Long.valueOf(r4)
            r5 = 9
            r1[r5] = r4
            r4 = 10
            r5 = 2
            int r6 = r0.getDeviceIdleModeCount(r5, r10)
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)
            r1[r4] = r6
            r4 = 11
            long r60 = r45 / r2
            java.lang.Long r2 = java.lang.Long.valueOf(r60)
            r1[r4] = r2
            r2 = 12
            int r3 = r0.getDeviceIdlingCount(r5, r10)
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            r1[r2] = r3
            r2 = 13
            int r3 = r0.getMobileRadioActiveCount(r10)
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            r1[r2] = r3
            r2 = 14
            long r3 = r0.getMobileRadioActiveUnknownTime(r10)
            r5 = 1000(0x3e8, double:4.94E-321)
            long r3 = r3 / r5
            java.lang.Long r3 = java.lang.Long.valueOf(r3)
            r1[r2] = r3
            r2 = 15
            long r3 = r37 / r5
            java.lang.Long r3 = java.lang.Long.valueOf(r3)
            r1[r2] = r3
            r2 = 16
            r3 = 1
            int r4 = r0.getDeviceIdleModeCount(r3, r10)
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            r1[r2] = r4
            r2 = 17
            long r106 = r43 / r5
            java.lang.Long r4 = java.lang.Long.valueOf(r106)
            r1[r2] = r4
            r2 = 18
            int r4 = r0.getDeviceIdlingCount(r3, r10)
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            r1[r2] = r4
            r2 = 19
            long r4 = r0.getLongestDeviceIdleModeTime(r3)
            java.lang.Long r3 = java.lang.Long.valueOf(r4)
            r1[r2] = r3
            r2 = 20
            r3 = 2
            long r4 = r0.getLongestDeviceIdleModeTime(r3)
            java.lang.Long r3 = java.lang.Long.valueOf(r4)
            r1[r2] = r3
            java.lang.String r2 = "m"
            r3 = 0
            dumpLine(r9, r3, r12, r2, r1)
            r1 = 5
            java.lang.Object[] r2 = new java.lang.Object[r1]
            r3 = 0
        L_0x037e:
            if (r3 >= r1) goto L_0x0392
            long r4 = r0.getScreenBrightnessTime(r3, r7, r10)
            r64 = 1000(0x3e8, double:4.94E-321)
            long r4 = r4 / r64
            java.lang.Long r1 = java.lang.Long.valueOf(r4)
            r2[r3] = r1
            int r3 = r3 + 1
            r1 = 5
            goto L_0x037e
        L_0x0392:
            java.lang.String r1 = "br"
            r3 = 0
            dumpLine(r9, r3, r12, r1, r2)
            r1 = 6
            java.lang.Object[] r2 = new java.lang.Object[r1]
            r3 = 0
        L_0x039c:
            if (r3 >= r1) goto L_0x03b0
            long r4 = r0.getPhoneSignalStrengthTime(r3, r7, r10)
            r64 = 1000(0x3e8, double:4.94E-321)
            long r4 = r4 / r64
            java.lang.Long r1 = java.lang.Long.valueOf(r4)
            r2[r3] = r1
            int r3 = r3 + 1
            r1 = 6
            goto L_0x039c
        L_0x03b0:
            java.lang.String r1 = "sgt"
            r3 = 0
            dumpLine(r9, r3, r12, r1, r2)
            r1 = 1
            java.lang.Object[] r4 = new java.lang.Object[r1]
            long r5 = r0.getPhoneSignalScanningTime(r7, r10)
            r61 = 1000(0x3e8, double:4.94E-321)
            long r5 = r5 / r61
            java.lang.Long r1 = java.lang.Long.valueOf(r5)
            r4[r3] = r1
            java.lang.String r1 = "sst"
            dumpLine(r9, r3, r12, r1, r4)
            r1 = 0
        L_0x03cf:
            r3 = 6
            if (r1 >= r3) goto L_0x03df
            int r3 = r0.getPhoneSignalStrengthCount(r1, r10)
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            r2[r1] = r3
            int r1 = r1 + 1
            goto L_0x03cf
        L_0x03df:
            java.lang.String r1 = "sgc"
            r3 = 0
            dumpLine(r9, r3, r12, r1, r2)
            r1 = 22
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r2 = 0
        L_0x03eb:
            r3 = 22
            if (r2 >= r3) goto L_0x03ff
            long r3 = r0.getPhoneDataConnectionTime(r2, r7, r10)
            r5 = 1000(0x3e8, double:4.94E-321)
            long r3 = r3 / r5
            java.lang.Long r3 = java.lang.Long.valueOf(r3)
            r1[r2] = r3
            int r2 = r2 + 1
            goto L_0x03eb
        L_0x03ff:
            java.lang.String r2 = "dct"
            r3 = 0
            dumpLine(r9, r3, r12, r2, r1)
            r2 = 0
        L_0x0406:
            r3 = 22
            if (r2 >= r3) goto L_0x0417
            int r3 = r0.getPhoneDataConnectionCount(r2, r10)
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            r1[r2] = r3
            int r2 = r2 + 1
            goto L_0x0406
        L_0x0417:
            java.lang.String r2 = "dcc"
            r3 = 0
            dumpLine(r9, r3, r12, r2, r1)
            r2 = 8
            java.lang.Object[] r1 = new java.lang.Object[r2]
            r3 = 0
        L_0x0422:
            if (r3 >= r2) goto L_0x0437
            long r4 = r0.getWifiStateTime(r3, r7, r10)
            r64 = 1000(0x3e8, double:4.94E-321)
            long r4 = r4 / r64
            java.lang.Long r2 = java.lang.Long.valueOf(r4)
            r1[r3] = r2
            int r3 = r3 + 1
            r2 = 8
            goto L_0x0422
        L_0x0437:
            java.lang.String r2 = "wst"
            r3 = 0
            dumpLine(r9, r3, r12, r2, r1)
            r2 = 0
        L_0x043f:
            r3 = 8
            if (r2 >= r3) goto L_0x0450
            int r3 = r0.getWifiStateCount(r2, r10)
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            r1[r2] = r3
            int r2 = r2 + 1
            goto L_0x043f
        L_0x0450:
            java.lang.String r2 = "wsc"
            r3 = 0
            dumpLine(r9, r3, r12, r2, r1)
            r2 = 13
            java.lang.Object[] r1 = new java.lang.Object[r2]
            r2 = 0
        L_0x045c:
            r3 = 13
            if (r2 >= r3) goto L_0x0470
            long r3 = r0.getWifiSupplStateTime(r2, r7, r10)
            r5 = 1000(0x3e8, double:4.94E-321)
            long r3 = r3 / r5
            java.lang.Long r3 = java.lang.Long.valueOf(r3)
            r1[r2] = r3
            int r2 = r2 + 1
            goto L_0x045c
        L_0x0470:
            java.lang.String r2 = "wsst"
            r3 = 0
            dumpLine(r9, r3, r12, r2, r1)
            r2 = 0
        L_0x0478:
            r3 = 13
            if (r2 >= r3) goto L_0x0489
            int r3 = r0.getWifiSupplStateCount(r2, r10)
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            r1[r2] = r3
            int r2 = r2 + 1
            goto L_0x0478
        L_0x0489:
            java.lang.String r2 = "wssc"
            r3 = 0
            dumpLine(r9, r3, r12, r2, r1)
            r2 = 5
            java.lang.Object[] r6 = new java.lang.Object[r2]
            r1 = 0
        L_0x0494:
            if (r1 >= r2) goto L_0x04a7
            long r2 = r0.getWifiSignalStrengthTime(r1, r7, r10)
            r4 = 1000(0x3e8, double:4.94E-321)
            long r2 = r2 / r4
            java.lang.Long r2 = java.lang.Long.valueOf(r2)
            r6[r1] = r2
            int r1 = r1 + 1
            r2 = 5
            goto L_0x0494
        L_0x04a7:
            java.lang.String r1 = "wsgt"
            r2 = 0
            dumpLine(r9, r2, r12, r1, r6)
            r1 = 0
        L_0x04af:
            r2 = 5
            if (r1 >= r2) goto L_0x04bf
            int r2 = r0.getWifiSignalStrengthCount(r1, r10)
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            r6[r1] = r2
            int r1 = r1 + 1
            goto L_0x04af
        L_0x04bf:
            java.lang.String r1 = "wsgc"
            r2 = 0
            dumpLine(r9, r2, r12, r1, r6)
            long r106 = r0.getWifiMulticastWakelockTime(r7, r10)
            int r61 = r0.getWifiMulticastWakelockCount(r10)
            r1 = 2
            java.lang.Object[] r3 = new java.lang.Object[r1]
            r4 = 1000(0x3e8, double:4.94E-321)
            long r108 = r106 / r4
            java.lang.Long r1 = java.lang.Long.valueOf(r108)
            r3[r2] = r1
            java.lang.Integer r1 = java.lang.Integer.valueOf(r61)
            r4 = 1
            r3[r4] = r1
            java.lang.String r1 = "wmct"
            dumpLine(r9, r2, r12, r1, r3)
            r1 = 10
            java.lang.Object[] r1 = new java.lang.Object[r1]
            int r3 = r201.getLowDischargeAmountSinceCharge()
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            r1[r2] = r3
            int r2 = r201.getHighDischargeAmountSinceCharge()
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            r3 = 1
            r1[r3] = r2
            int r2 = r201.getDischargeAmountScreenOnSinceCharge()
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            r60 = 2
            r1[r60] = r2
            int r2 = r201.getDischargeAmountScreenOffSinceCharge()
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            r3 = 3
            r1[r3] = r2
            r2 = 1000(0x3e8, double:4.94E-321)
            long r4 = r50 / r2
            java.lang.Long r4 = java.lang.Long.valueOf(r4)
            r5 = 4
            r1[r5] = r4
            long r4 = r52 / r2
            java.lang.Long r4 = java.lang.Long.valueOf(r4)
            r5 = 5
            r1[r5] = r4
            int r4 = r201.getDischargeAmountScreenDozeSinceCharge()
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            r5 = 6
            r1[r5] = r4
            long r4 = r54 / r2
            java.lang.Long r4 = java.lang.Long.valueOf(r4)
            r5 = 7
            r1[r5] = r4
            long r4 = r56 / r2
            java.lang.Long r4 = java.lang.Long.valueOf(r4)
            r5 = 8
            r1[r5] = r4
            long r4 = r58 / r2
            java.lang.Long r2 = java.lang.Long.valueOf(r4)
            r3 = 9
            r1[r3] = r2
            java.lang.String r2 = "dc"
            r3 = 0
            dumpLine(r9, r3, r12, r2, r1)
            r108 = 500(0x1f4, double:2.47E-321)
            java.lang.String r5 = "\""
            if (r11 >= 0) goto L_0x0670
            java.util.Map r78 = r201.getKernelWakelockStats()
            int r1 = r78.size()
            if (r1 <= 0) goto L_0x05ec
            java.util.Set r1 = r78.entrySet()
            java.util.Iterator r110 = r1.iterator()
        L_0x0573:
            boolean r1 = r110.hasNext()
            if (r1 == 0) goto L_0x05e1
            java.lang.Object r1 = r110.next()
            r111 = r1
            java.util.Map$Entry r111 = (java.util.Map.Entry) r111
            r1 = 0
            r13.setLength(r1)
            java.lang.Object r1 = r111.getValue()
            r2 = r1
            android.os.BatteryStats$Timer r2 = (android.os.BatteryStats.Timer) r2
            r112 = 0
            java.lang.String r113 = ""
            r1 = r13
            r3 = r7
            r114 = r7
            r8 = r5
            r5 = r112
            r112 = r6
            r6 = r204
            r11 = r60
            r60 = r14
            r199 = r114
            r114 = r15
            r14 = r199
            r7 = r113
            printWakeLockCheckin(r1, r2, r3, r5, r6, r7)
            java.lang.Object[] r1 = new java.lang.Object[r11]
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r8)
            java.lang.Object r3 = r111.getKey()
            java.lang.String r3 = (java.lang.String) r3
            r2.append(r3)
            r2.append(r8)
            java.lang.String r2 = r2.toString()
            r3 = 0
            r1[r3] = r2
            java.lang.String r2 = r13.toString()
            r4 = 1
            r1[r4] = r2
            java.lang.String r2 = "kwl"
            dumpLine(r9, r3, r12, r2, r1)
            r5 = r8
            r7 = r14
            r14 = r60
            r6 = r112
            r15 = r114
            r60 = r11
            r11 = r205
            goto L_0x0573
        L_0x05e1:
            r112 = r6
            r114 = r15
            r11 = r60
            r60 = r14
            r14 = r7
            r8 = r5
            goto L_0x05f6
        L_0x05ec:
            r112 = r6
            r114 = r15
            r11 = r60
            r60 = r14
            r14 = r7
            r8 = r5
        L_0x05f6:
            java.util.Map r1 = r201.getWakeupReasonStats()
            int r2 = r1.size()
            if (r2 <= 0) goto L_0x066d
            java.util.Set r2 = r1.entrySet()
            java.util.Iterator r2 = r2.iterator()
        L_0x0608:
            boolean r3 = r2.hasNext()
            if (r3 == 0) goto L_0x066a
            java.lang.Object r3 = r2.next()
            java.util.Map$Entry r3 = (java.util.Map.Entry) r3
            java.lang.Object r4 = r3.getValue()
            android.os.BatteryStats$Timer r4 = (android.os.BatteryStats.Timer) r4
            long r4 = r4.getTotalTimeLocked(r14, r10)
            java.lang.Object r6 = r3.getValue()
            android.os.BatteryStats$Timer r6 = (android.os.BatteryStats.Timer) r6
            int r6 = r6.getCountLocked(r10)
            r7 = 3
            java.lang.Object[] r11 = new java.lang.Object[r7]
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            r7.append(r8)
            java.lang.Object r111 = r3.getKey()
            r113 = r1
            r1 = r111
            java.lang.String r1 = (java.lang.String) r1
            r7.append(r1)
            r7.append(r8)
            java.lang.String r1 = r7.toString()
            r7 = 0
            r11[r7] = r1
            long r115 = r4 + r108
            r64 = 1000(0x3e8, double:4.94E-321)
            long r115 = r115 / r64
            java.lang.Long r1 = java.lang.Long.valueOf(r115)
            r62 = 1
            r11[r62] = r1
            java.lang.Integer r1 = java.lang.Integer.valueOf(r6)
            r62 = 2
            r11[r62] = r1
            java.lang.String r1 = "wr"
            dumpLine(r9, r7, r12, r1, r11)
            r1 = r113
            r11 = 2
            goto L_0x0608
        L_0x066a:
            r113 = r1
            goto L_0x0678
        L_0x066d:
            r113 = r1
            goto L_0x0678
        L_0x0670:
            r112 = r6
            r60 = r14
            r114 = r15
            r14 = r7
            r8 = r5
        L_0x0678:
            java.util.Map r11 = r201.getRpmStats()
            java.util.Map r7 = r201.getScreenOffRpmStats()
            int r1 = r11.size()
            r115 = 0
            if (r1 <= 0) goto L_0x071a
            java.util.Set r1 = r11.entrySet()
            java.util.Iterator r1 = r1.iterator()
        L_0x0690:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x0718
            java.lang.Object r2 = r1.next()
            java.util.Map$Entry r2 = (java.util.Map.Entry) r2
            r3 = 0
            r13.setLength(r3)
            java.lang.Object r3 = r2.getValue()
            android.os.BatteryStats$Timer r3 = (android.os.BatteryStats.Timer) r3
            long r4 = r3.getTotalTimeLocked(r14, r10)
            long r4 = r4 + r108
            r64 = 1000(0x3e8, double:4.94E-321)
            long r4 = r4 / r64
            int r6 = r3.getCountLocked(r10)
            r78 = r1
            java.lang.Object r1 = r2.getKey()
            java.lang.Object r1 = r7.get(r1)
            android.os.BatteryStats$Timer r1 = (android.os.BatteryStats.Timer) r1
            if (r1 == 0) goto L_0x06cd
            long r117 = r1.getTotalTimeLocked(r14, r10)
            long r117 = r117 + r108
            r64 = 1000(0x3e8, double:4.94E-321)
            long r117 = r117 / r64
            goto L_0x06cf
        L_0x06cd:
            r117 = r115
        L_0x06cf:
            if (r1 == 0) goto L_0x06d6
            int r111 = r1.getCountLocked(r10)
            goto L_0x06d8
        L_0x06d6:
            r111 = 0
        L_0x06d8:
            r113 = r1
            r119 = r3
            r1 = 3
            java.lang.Object[] r3 = new java.lang.Object[r1]
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r8)
            java.lang.Object r120 = r2.getKey()
            r121 = r2
            r2 = r120
            java.lang.String r2 = (java.lang.String) r2
            r1.append(r2)
            r1.append(r8)
            java.lang.String r1 = r1.toString()
            r2 = 0
            r3[r2] = r1
            java.lang.Long r1 = java.lang.Long.valueOf(r4)
            r62 = 1
            r3[r62] = r1
            java.lang.Integer r1 = java.lang.Integer.valueOf(r6)
            r62 = 2
            r3[r62] = r1
            java.lang.String r1 = "rpm"
            dumpLine(r9, r2, r12, r1, r3)
            r1 = r78
            goto L_0x0690
        L_0x0718:
            r2 = 0
            goto L_0x071b
        L_0x071a:
            r2 = 0
        L_0x071b:
            com.android.internal.os.BatteryStatsHelper r1 = new com.android.internal.os.BatteryStatsHelper
            r6 = r202
            r5 = r206
            r1.<init>(r6, r2, r5)
            r4 = r1
            r4.create(r0)
            r3 = -1
            r4.refreshStats(r10, r3)
            java.util.List r2 = r4.getUsageList()
            if (r2 == 0) goto L_0x086a
            int r1 = r2.size()
            if (r1 <= 0) goto L_0x086a
            r1 = 4
            java.lang.Object[] r3 = new java.lang.Object[r1]
            com.android.internal.os.PowerProfile r1 = r4.getPowerProfile()
            double r117 = r1.getBatteryCapacity()
            java.lang.String r1 = com.android.internal.os.BatteryStatsHelper.makemAh(r117)
            r62 = 0
            r3[r62] = r1
            double r117 = r4.getComputedPower()
            java.lang.String r1 = com.android.internal.os.BatteryStatsHelper.makemAh(r117)
            r63 = 1
            r3[r63] = r1
            double r117 = r4.getMinDrainedPower()
            java.lang.String r1 = com.android.internal.os.BatteryStatsHelper.makemAh(r117)
            r110 = 2
            r3[r110] = r1
            double r117 = r4.getMaxDrainedPower()
            java.lang.String r1 = com.android.internal.os.BatteryStatsHelper.makemAh(r117)
            r66 = 3
            r3[r66] = r1
            java.lang.String r1 = "pws"
            r0 = 0
            dumpLine(r9, r0, r12, r1, r3)
            r0 = 0
            r1 = 0
        L_0x0778:
            int r3 = r2.size()
            if (r1 >= r3) goto L_0x0861
            java.lang.Object r3 = r2.get(r1)
            com.android.internal.os.BatterySipper r3 = (com.android.internal.os.BatterySipper) r3
            int[] r111 = android.os.BatteryStats.AnonymousClass2.$SwitchMap$com$android$internal$os$BatterySipper$DrainType
            r113 = r0
            com.android.internal.os.BatterySipper$DrainType r0 = r3.drainType
            int r0 = r0.ordinal()
            r0 = r111[r0]
            switch(r0) {
                case 1: goto L_0x0818;
                case 2: goto L_0x080f;
                case 3: goto L_0x0807;
                case 4: goto L_0x07fe;
                case 5: goto L_0x07f5;
                case 6: goto L_0x07ed;
                case 7: goto L_0x07e4;
                case 8: goto L_0x07dc;
                case 9: goto L_0x07d0;
                case 10: goto L_0x07c3;
                case 11: goto L_0x07b9;
                case 12: goto L_0x07af;
                case 13: goto L_0x07a6;
                case 14: goto L_0x079c;
                default: goto L_0x0793;
            }
        L_0x0793:
            r111 = r2
            java.lang.String r0 = "???"
            r2 = r0
            r0 = r113
            goto L_0x081f
        L_0x079c:
            java.lang.String r0 = "memory"
            r111 = r2
            r2 = r0
            r0 = r113
            goto L_0x081f
        L_0x07a6:
            java.lang.String r0 = "camera"
            r111 = r2
            r2 = r0
            r0 = r113
            goto L_0x081f
        L_0x07af:
            java.lang.String r0 = "over"
            r111 = r2
            r2 = r0
            r0 = r113
            goto L_0x081f
        L_0x07b9:
            java.lang.String r0 = "unacc"
            r111 = r2
            r2 = r0
            r0 = r113
            goto L_0x081f
        L_0x07c3:
            int r0 = r3.userId
            r111 = r2
            r2 = 0
            int r0 = android.os.UserHandle.getUid(r0, r2)
            java.lang.String r2 = "user"
            goto L_0x081f
        L_0x07d0:
            r111 = r2
            android.os.BatteryStats$Uid r0 = r3.uidObj
            int r0 = r0.getUid()
            java.lang.String r2 = "uid"
            goto L_0x081f
        L_0x07dc:
            r111 = r2
            java.lang.String r0 = "flashlight"
            r2 = r0
            r0 = r113
            goto L_0x081f
        L_0x07e4:
            r111 = r2
            java.lang.String r0 = "scrn"
            r2 = r0
            r0 = r113
            goto L_0x081f
        L_0x07ed:
            r111 = r2
            java.lang.String r0 = "blue"
            r2 = r0
            r0 = r113
            goto L_0x081f
        L_0x07f5:
            r111 = r2
            java.lang.String r0 = "wifi"
            r2 = r0
            r0 = r113
            goto L_0x081f
        L_0x07fe:
            r111 = r2
            java.lang.String r0 = "phone"
            r2 = r0
            r0 = r113
            goto L_0x081f
        L_0x0807:
            r111 = r2
            java.lang.String r0 = "cell"
            r2 = r0
            r0 = r113
            goto L_0x081f
        L_0x080f:
            r111 = r2
            java.lang.String r0 = "idle"
            r2 = r0
            r0 = r113
            goto L_0x081f
        L_0x0818:
            r111 = r2
            java.lang.String r0 = "ambi"
            r2 = r0
            r0 = r113
        L_0x081f:
            r117 = r4
            r4 = 5
            java.lang.Object[] r5 = new java.lang.Object[r4]
            r4 = 0
            r5[r4] = r2
            r118 = r7
            double r6 = r3.totalPowerMah
            java.lang.String r4 = com.android.internal.os.BatteryStatsHelper.makemAh(r6)
            r6 = 1
            r5[r6] = r4
            boolean r4 = r3.shouldHide
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            r6 = 2
            r5[r6] = r4
            double r6 = r3.screenPowerMah
            java.lang.String r4 = com.android.internal.os.BatteryStatsHelper.makemAh(r6)
            r6 = 3
            r5[r6] = r4
            double r6 = r3.proportionalSmearMah
            java.lang.String r4 = com.android.internal.os.BatteryStatsHelper.makemAh(r6)
            r6 = 4
            r5[r6] = r4
            java.lang.String r4 = "pwi"
            dumpLine(r9, r0, r12, r4, r5)
            int r1 = r1 + 1
            r6 = r202
            r5 = r206
            r2 = r111
            r4 = r117
            r7 = r118
            goto L_0x0778
        L_0x0861:
            r113 = r0
            r111 = r2
            r117 = r4
            r118 = r7
            goto L_0x0870
        L_0x086a:
            r111 = r2
            r117 = r4
            r118 = r7
        L_0x0870:
            long[] r0 = r201.getCpuFreqs()
            if (r0 == 0) goto L_0x08ab
            r1 = 0
            r13.setLength(r1)
            r1 = 0
        L_0x087b:
            int r2 = r0.length
            if (r1 >= r2) goto L_0x089c
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            if (r1 != 0) goto L_0x0888
            java.lang.String r3 = ""
            goto L_0x088a
        L_0x0888:
            java.lang.String r3 = ","
        L_0x088a:
            r2.append(r3)
            r3 = r0[r1]
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            r13.append(r2)
            int r1 = r1 + 1
            goto L_0x087b
        L_0x089c:
            r1 = 1
            java.lang.Object[] r2 = new java.lang.Object[r1]
            java.lang.String r1 = r13.toString()
            r3 = 0
            r2[r3] = r1
            java.lang.String r1 = "gcf"
            dumpLine(r9, r3, r12, r1, r2)
        L_0x08ab:
            r1 = 0
            r7 = r1
        L_0x08ad:
            r6 = r79
            if (r7 >= r6) goto L_0x1409
            r5 = r114
            int r4 = r5.keyAt(r7)
            r3 = r205
            r2 = 2
            if (r3 < 0) goto L_0x08ec
            if (r4 == r3) goto L_0x08ec
            r183 = r0
            r78 = r2
            r160 = r5
            r159 = r6
            r196 = r8
            r70 = r13
            r189 = r14
            r13 = r100
            r197 = r102
            r110 = r111
            r103 = r118
            r16 = 9
            r62 = 0
            r63 = 1
            r64 = 1000(0x3e8, double:4.94E-321)
            r66 = 3
            r67 = 4
            r68 = 5
            r71 = 8
            r161 = -1
            r118 = r7
            r111 = r11
            goto L_0x13ef
        L_0x08ec:
            java.lang.Object r1 = r5.valueAt(r7)
            android.os.BatteryStats$Uid r1 = (android.os.BatteryStats.Uid) r1
            r2 = 0
            long r113 = r1.getNetworkActivityBytes(r2, r10)
            r2 = 1
            long r119 = r1.getNetworkActivityBytes(r2, r10)
            r2 = 2
            long r121 = r1.getNetworkActivityBytes(r2, r10)
            r2 = 3
            long r123 = r1.getNetworkActivityBytes(r2, r10)
            r2 = 0
            long r125 = r1.getNetworkActivityPackets(r2, r10)
            r2 = 1
            long r127 = r1.getNetworkActivityPackets(r2, r10)
            long r129 = r1.getMobileRadioActiveTime(r10)
            int r79 = r1.getMobileRadioActiveCount(r10)
            long r131 = r1.getMobileRadioApWakeupCount(r10)
            r2 = 2
            long r133 = r1.getNetworkActivityPackets(r2, r10)
            r2 = 3
            long r135 = r1.getNetworkActivityPackets(r2, r10)
            long r137 = r1.getWifiRadioApWakeupCount(r10)
            r2 = 4
            long r139 = r1.getNetworkActivityBytes(r2, r10)
            r2 = 5
            long r141 = r1.getNetworkActivityBytes(r2, r10)
            r2 = 6
            long r143 = r1.getNetworkActivityBytes(r2, r10)
            r2 = 7
            long r145 = r1.getNetworkActivityBytes(r2, r10)
            r2 = 8
            long r147 = r1.getNetworkActivityBytes(r2, r10)
            r2 = 9
            long r149 = r1.getNetworkActivityBytes(r2, r10)
            r2 = 6
            long r151 = r1.getNetworkActivityPackets(r2, r10)
            r2 = 7
            long r153 = r1.getNetworkActivityPackets(r2, r10)
            r2 = 8
            long r155 = r1.getNetworkActivityPackets(r2, r10)
            r2 = 9
            long r157 = r1.getNetworkActivityPackets(r2, r10)
            int r2 = (r113 > r115 ? 1 : (r113 == r115 ? 0 : -1))
            if (r2 > 0) goto L_0x09bb
            int r2 = (r119 > r115 ? 1 : (r119 == r115 ? 0 : -1))
            if (r2 > 0) goto L_0x09bb
            int r2 = (r121 > r115 ? 1 : (r121 == r115 ? 0 : -1))
            if (r2 > 0) goto L_0x09bb
            int r2 = (r123 > r115 ? 1 : (r123 == r115 ? 0 : -1))
            if (r2 > 0) goto L_0x09bb
            int r2 = (r125 > r115 ? 1 : (r125 == r115 ? 0 : -1))
            if (r2 > 0) goto L_0x09bb
            int r2 = (r127 > r115 ? 1 : (r127 == r115 ? 0 : -1))
            if (r2 > 0) goto L_0x09bb
            int r2 = (r133 > r115 ? 1 : (r133 == r115 ? 0 : -1))
            if (r2 > 0) goto L_0x09bb
            int r2 = (r135 > r115 ? 1 : (r135 == r115 ? 0 : -1))
            if (r2 > 0) goto L_0x09bb
            int r2 = (r129 > r115 ? 1 : (r129 == r115 ? 0 : -1))
            if (r2 > 0) goto L_0x09bb
            if (r79 > 0) goto L_0x09bb
            int r2 = (r139 > r115 ? 1 : (r139 == r115 ? 0 : -1))
            if (r2 > 0) goto L_0x09bb
            int r2 = (r141 > r115 ? 1 : (r141 == r115 ? 0 : -1))
            if (r2 > 0) goto L_0x09bb
            int r2 = (r131 > r115 ? 1 : (r131 == r115 ? 0 : -1))
            if (r2 > 0) goto L_0x09bb
            int r2 = (r137 > r115 ? 1 : (r137 == r115 ? 0 : -1))
            if (r2 > 0) goto L_0x09bb
            int r2 = (r143 > r115 ? 1 : (r143 == r115 ? 0 : -1))
            if (r2 > 0) goto L_0x09bb
            int r2 = (r145 > r115 ? 1 : (r145 == r115 ? 0 : -1))
            if (r2 > 0) goto L_0x09bb
            int r2 = (r147 > r115 ? 1 : (r147 == r115 ? 0 : -1))
            if (r2 > 0) goto L_0x09bb
            int r2 = (r149 > r115 ? 1 : (r149 == r115 ? 0 : -1))
            if (r2 > 0) goto L_0x09bb
            int r2 = (r151 > r115 ? 1 : (r151 == r115 ? 0 : -1))
            if (r2 > 0) goto L_0x09bb
            int r2 = (r153 > r115 ? 1 : (r153 == r115 ? 0 : -1))
            if (r2 > 0) goto L_0x09bb
            int r2 = (r155 > r115 ? 1 : (r155 == r115 ? 0 : -1))
            if (r2 > 0) goto L_0x09bb
            int r2 = (r157 > r115 ? 1 : (r157 == r115 ? 0 : -1))
            if (r2 <= 0) goto L_0x09b7
            goto L_0x09bb
        L_0x09b7:
            r110 = 2
            goto L_0x0a75
        L_0x09bb:
            r2 = 22
            java.lang.Object[] r2 = new java.lang.Object[r2]
            java.lang.Long r159 = java.lang.Long.valueOf(r113)
            r62 = 0
            r2[r62] = r159
            java.lang.Long r159 = java.lang.Long.valueOf(r119)
            r63 = 1
            r2[r63] = r159
            java.lang.Long r159 = java.lang.Long.valueOf(r121)
            r110 = 2
            r2[r110] = r159
            java.lang.Long r159 = java.lang.Long.valueOf(r123)
            r66 = 3
            r2[r66] = r159
            java.lang.Long r159 = java.lang.Long.valueOf(r125)
            r67 = 4
            r2[r67] = r159
            java.lang.Long r159 = java.lang.Long.valueOf(r127)
            r68 = 5
            r2[r68] = r159
            java.lang.Long r159 = java.lang.Long.valueOf(r133)
            r69 = 6
            r2[r69] = r159
            java.lang.Long r159 = java.lang.Long.valueOf(r135)
            r70 = 7
            r2[r70] = r159
            java.lang.Long r159 = java.lang.Long.valueOf(r129)
            r71 = 8
            r2[r71] = r159
            java.lang.Integer r159 = java.lang.Integer.valueOf(r79)
            r16 = 9
            r2[r16] = r159
            r159 = 10
            java.lang.Long r160 = java.lang.Long.valueOf(r139)
            r2[r159] = r160
            r159 = 11
            java.lang.Long r160 = java.lang.Long.valueOf(r141)
            r2[r159] = r160
            r159 = 12
            java.lang.Long r160 = java.lang.Long.valueOf(r131)
            r2[r159] = r160
            r159 = 13
            java.lang.Long r160 = java.lang.Long.valueOf(r137)
            r2[r159] = r160
            r159 = 14
            java.lang.Long r160 = java.lang.Long.valueOf(r143)
            r2[r159] = r160
            r159 = 15
            java.lang.Long r160 = java.lang.Long.valueOf(r145)
            r2[r159] = r160
            r159 = 16
            java.lang.Long r160 = java.lang.Long.valueOf(r147)
            r2[r159] = r160
            r159 = 17
            java.lang.Long r160 = java.lang.Long.valueOf(r149)
            r2[r159] = r160
            r159 = 18
            java.lang.Long r160 = java.lang.Long.valueOf(r151)
            r2[r159] = r160
            r159 = 19
            java.lang.Long r160 = java.lang.Long.valueOf(r153)
            r2[r159] = r160
            r159 = 20
            java.lang.Long r160 = java.lang.Long.valueOf(r155)
            r2[r159] = r160
            r159 = 21
            java.lang.Long r160 = java.lang.Long.valueOf(r157)
            r2[r159] = r160
            java.lang.String r3 = "nt"
            dumpLine(r9, r4, r12, r3, r2)
        L_0x0a75:
            android.os.BatteryStats$ControllerActivityCounter r159 = r1.getModemControllerActivity()
            java.lang.String r160 = "mcd"
            r3 = r1
            r1 = r203
            r199 = r111
            r111 = r11
            r11 = r110
            r110 = r199
            r2 = r4
            r11 = r3
            r161 = -1
            r3 = r12
            r162 = r0
            r0 = r4
            r4 = r160
            r160 = r5
            r5 = r159
            r159 = r6
            r6 = r204
            dumpControllerActivityLine(r1, r2, r3, r4, r5, r6)
            long r163 = r11.getFullWifiLockTime(r14, r10)
            long r165 = r11.getWifiScanTime(r14, r10)
            int r167 = r11.getWifiScanCount(r10)
            int r168 = r11.getWifiScanBackgroundCount(r10)
            long r1 = r11.getWifiScanActualTime(r14)
            long r1 = r1 + r108
            r3 = 1000(0x3e8, double:4.94E-321)
            long r169 = r1 / r3
            long r1 = r11.getWifiScanBackgroundTime(r14)
            long r1 = r1 + r108
            long r171 = r1 / r3
            long r173 = r11.getWifiRunningTime(r14, r10)
            int r1 = (r163 > r115 ? 1 : (r163 == r115 ? 0 : -1))
            if (r1 != 0) goto L_0x0adb
            int r1 = (r165 > r115 ? 1 : (r165 == r115 ? 0 : -1))
            if (r1 != 0) goto L_0x0adb
            if (r-89 != 0) goto L_0x0adb
            if (r-88 != 0) goto L_0x0adb
            int r1 = (r169 > r115 ? 1 : (r169 == r115 ? 0 : -1))
            if (r1 != 0) goto L_0x0adb
            int r1 = (r171 > r115 ? 1 : (r171 == r115 ? 0 : -1))
            if (r1 != 0) goto L_0x0adb
            int r1 = (r173 > r115 ? 1 : (r173 == r115 ? 0 : -1))
            if (r1 == 0) goto L_0x0b21
        L_0x0adb:
            r1 = 10
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.Long r2 = java.lang.Long.valueOf(r163)
            r3 = 0
            r1[r3] = r2
            java.lang.Long r2 = java.lang.Long.valueOf(r165)
            r3 = 1
            r1[r3] = r2
            java.lang.Long r2 = java.lang.Long.valueOf(r173)
            r3 = 2
            r1[r3] = r2
            java.lang.Integer r2 = java.lang.Integer.valueOf(r167)
            r3 = 3
            r1[r3] = r2
            r2 = 4
            r1[r2] = r60
            r2 = 5
            r1[r2] = r60
            r2 = 6
            r1[r2] = r60
            java.lang.Integer r2 = java.lang.Integer.valueOf(r168)
            r3 = 7
            r1[r3] = r2
            java.lang.Long r2 = java.lang.Long.valueOf(r169)
            r3 = 8
            r1[r3] = r2
            java.lang.Long r2 = java.lang.Long.valueOf(r171)
            r3 = 9
            r1[r3] = r2
            java.lang.String r2 = "wfl"
            dumpLine(r9, r0, r12, r2, r1)
        L_0x0b21:
            android.os.BatteryStats$ControllerActivityCounter r5 = r11.getWifiControllerActivity()
            java.lang.String r4 = "wfcd"
            r1 = r203
            r2 = r0
            r3 = r12
            r6 = r204
            dumpControllerActivityLine(r1, r2, r3, r4, r5, r6)
            android.os.BatteryStats$Timer r6 = r11.getBluetoothScanTimer()
            if (r6 == 0) goto L_0x0c36
            long r1 = r6.getTotalTimeLocked(r14, r10)
            long r1 = r1 + r108
            r3 = 1000(0x3e8, double:4.94E-321)
            long r1 = r1 / r3
            int r3 = (r1 > r115 ? 1 : (r1 == r115 ? 0 : -1))
            if (r3 == 0) goto L_0x0c25
            int r3 = r6.getCountLocked(r10)
            android.os.BatteryStats$Timer r4 = r11.getBluetoothScanBackgroundTimer()
            if (r4 == 0) goto L_0x0b54
            int r5 = r4.getCountLocked(r10)
            goto L_0x0b55
        L_0x0b54:
            r5 = 0
        L_0x0b55:
            r176 = r7
            r175 = r8
            r7 = r102
            long r102 = r6.getTotalDurationMsLocked(r7)
            if (r4 == 0) goto L_0x0b66
            long r177 = r4.getTotalDurationMsLocked(r7)
            goto L_0x0b68
        L_0x0b66:
            r177 = r115
        L_0x0b68:
            android.os.BatteryStats$Counter r179 = r11.getBluetoothScanResultCounter()
            if (r-77 == 0) goto L_0x0b79
            r179 = r4
            android.os.BatteryStats$Counter r4 = r11.getBluetoothScanResultCounter()
            int r4 = r4.getCountLocked(r10)
            goto L_0x0b7c
        L_0x0b79:
            r179 = r4
            r4 = 0
        L_0x0b7c:
            android.os.BatteryStats$Counter r180 = r11.getBluetoothScanResultBgCounter()
            if (r-76 == 0) goto L_0x0b8d
            r180 = r6
            android.os.BatteryStats$Counter r6 = r11.getBluetoothScanResultBgCounter()
            int r6 = r6.getCountLocked(r10)
            goto L_0x0b90
        L_0x0b8d:
            r180 = r6
            r6 = 0
        L_0x0b90:
            r181 = r14
            android.os.BatteryStats$Timer r14 = r11.getBluetoothUnoptimizedScanTimer()
            if (r14 == 0) goto L_0x0b9d
            long r183 = r14.getTotalDurationMsLocked(r7)
            goto L_0x0b9f
        L_0x0b9d:
            r183 = r115
        L_0x0b9f:
            if (r14 == 0) goto L_0x0ba6
            long r185 = r14.getMaxDurationMsLocked(r7)
            goto L_0x0ba8
        L_0x0ba6:
            r185 = r115
        L_0x0ba8:
            android.os.BatteryStats$Timer r15 = r11.getBluetoothUnoptimizedScanBackgroundTimer()
            if (r15 == 0) goto L_0x0bb4
            long r187 = r15.getTotalDurationMsLocked(r7)
            goto L_0x0bb6
        L_0x0bb4:
            r187 = r115
        L_0x0bb6:
            if (r15 == 0) goto L_0x0bbd
            long r189 = r15.getMaxDurationMsLocked(r7)
            goto L_0x0bbf
        L_0x0bbd:
            r189 = r115
        L_0x0bbf:
            r191 = r14
            r14 = 11
            java.lang.Object[] r14 = new java.lang.Object[r14]
            java.lang.Long r192 = java.lang.Long.valueOf(r1)
            r62 = 0
            r14[r62] = r192
            java.lang.Integer r192 = java.lang.Integer.valueOf(r3)
            r63 = 1
            r14[r63] = r192
            java.lang.Integer r192 = java.lang.Integer.valueOf(r5)
            r78 = 2
            r14[r78] = r192
            java.lang.Long r192 = java.lang.Long.valueOf(r102)
            r66 = 3
            r14[r66] = r192
            java.lang.Long r192 = java.lang.Long.valueOf(r177)
            r67 = 4
            r14[r67] = r192
            java.lang.Integer r192 = java.lang.Integer.valueOf(r4)
            r68 = 5
            r14[r68] = r192
            java.lang.Integer r192 = java.lang.Integer.valueOf(r6)
            r69 = 6
            r14[r69] = r192
            java.lang.Long r192 = java.lang.Long.valueOf(r183)
            r70 = 7
            r14[r70] = r192
            java.lang.Long r192 = java.lang.Long.valueOf(r187)
            r71 = 8
            r14[r71] = r192
            java.lang.Long r192 = java.lang.Long.valueOf(r185)
            r16 = 9
            r14[r16] = r192
            r192 = 10
            java.lang.Long r193 = java.lang.Long.valueOf(r189)
            r14[r192] = r193
            r192 = r1
            java.lang.String r1 = "blem"
            dumpLine(r9, r0, r12, r1, r14)
            goto L_0x0c44
        L_0x0c25:
            r192 = r1
            r180 = r6
            r176 = r7
            r175 = r8
            r181 = r14
            r7 = r102
            r16 = 9
            r71 = 8
            goto L_0x0c44
        L_0x0c36:
            r180 = r6
            r176 = r7
            r175 = r8
            r181 = r14
            r7 = r102
            r16 = 9
            r71 = 8
        L_0x0c44:
            android.os.BatteryStats$ControllerActivityCounter r5 = r11.getBluetoothControllerActivity()
            java.lang.String r4 = "ble"
            r1 = r203
            r2 = r0
            r3 = r12
            r14 = r180
            r6 = r204
            dumpControllerActivityLine(r1, r2, r3, r4, r5, r6)
            boolean r1 = r11.hasUserActivity()
            if (r1 == 0) goto L_0x0c80
            int r1 = android.os.BatteryStats.Uid.NUM_USER_ACTIVITY_TYPES
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r2 = 0
            r3 = 0
        L_0x0c62:
            int r4 = android.os.BatteryStats.Uid.NUM_USER_ACTIVITY_TYPES
            if (r3 >= r4) goto L_0x0c76
            int r4 = r11.getUserActivityCount(r3, r10)
            java.lang.Integer r5 = java.lang.Integer.valueOf(r4)
            r1[r3] = r5
            if (r4 == 0) goto L_0x0c73
            r2 = 1
        L_0x0c73:
            int r3 = r3 + 1
            goto L_0x0c62
        L_0x0c76:
            if (r2 == 0) goto L_0x0c7e
            java.lang.String r3 = "ua"
            dumpLine(r9, r0, r12, r3, r1)
        L_0x0c7e:
            r112 = r1
        L_0x0c80:
            android.os.BatteryStats$Timer r1 = r11.getAggregatedPartialWakelockTimer()
            if (r1 == 0) goto L_0x0cb6
            android.os.BatteryStats$Timer r1 = r11.getAggregatedPartialWakelockTimer()
            long r2 = r1.getTotalDurationMsLocked(r7)
            android.os.BatteryStats$Timer r4 = r1.getSubTimer()
            if (r4 == 0) goto L_0x0c99
            long r5 = r4.getTotalDurationMsLocked(r7)
            goto L_0x0c9b
        L_0x0c99:
            r5 = r115
        L_0x0c9b:
            r102 = r1
            r15 = 2
            java.lang.Object[] r1 = new java.lang.Object[r15]
            java.lang.Long r15 = java.lang.Long.valueOf(r2)
            r62 = 0
            r1[r62] = r15
            java.lang.Long r15 = java.lang.Long.valueOf(r5)
            r63 = 1
            r1[r63] = r15
            java.lang.String r15 = "awl"
            dumpLine(r9, r0, r12, r15, r1)
            goto L_0x0cb8
        L_0x0cb6:
            r63 = 1
        L_0x0cb8:
            android.util.ArrayMap r15 = r11.getWakelockStats()
            int r1 = r15.size()
            int r1 = r1 + -1
            r6 = r1
        L_0x0cc3:
            if (r6 < 0) goto L_0x0d84
            java.lang.Object r1 = r15.valueAt(r6)
            r5 = r1
            android.os.BatteryStats$Uid$Wakelock r5 = (android.os.BatteryStats.Uid.Wakelock) r5
            java.lang.String r102 = ""
            r1 = 0
            r13.setLength(r1)
            r1 = 1
            android.os.BatteryStats$Timer r2 = r5.getWakeTime(r1)
            java.lang.String r103 = "f"
            r1 = r13
            r3 = r181
            r177 = r7
            r8 = r5
            r5 = r103
            r7 = r6
            r6 = r204
            r180 = r14
            r103 = r118
            r118 = r176
            r194 = r177
            r14 = r7
            r7 = r102
            java.lang.String r102 = printWakeLockCheckin(r1, r2, r3, r5, r6, r7)
            r1 = 0
            android.os.BatteryStats$Timer r176 = r8.getWakeTime(r1)
            java.lang.String r5 = "p"
            r1 = r13
            r2 = r176
            r7 = r102
            java.lang.String r102 = printWakeLockCheckin(r1, r2, r3, r5, r6, r7)
            if (r-80 == 0) goto L_0x0d0b
            android.os.BatteryStats$Timer r1 = r176.getSubTimer()
            goto L_0x0d0c
        L_0x0d0b:
            r1 = 0
        L_0x0d0c:
            r2 = r1
            java.lang.String r5 = "bp"
            r1 = r13
            r3 = r181
            r6 = r204
            r7 = r102
            java.lang.String r102 = printWakeLockCheckin(r1, r2, r3, r5, r6, r7)
            r1 = 2
            android.os.BatteryStats$Timer r2 = r8.getWakeTime(r1)
            java.lang.String r5 = "w"
            r1 = r13
            r7 = r102
            java.lang.String r1 = printWakeLockCheckin(r1, r2, r3, r5, r6, r7)
            int r2 = r13.length()
            if (r2 <= 0) goto L_0x0d78
            java.lang.Object r2 = r15.keyAt(r14)
            java.lang.String r2 = (java.lang.String) r2
            r3 = 44
            int r3 = r2.indexOf(r3)
            if (r3 < 0) goto L_0x0d45
            r3 = 44
            r4 = 95
            java.lang.String r2 = r2.replace(r3, r4)
        L_0x0d45:
            r3 = 10
            int r3 = r2.indexOf(r3)
            if (r3 < 0) goto L_0x0d55
            r3 = 10
            r4 = 95
            java.lang.String r2 = r2.replace(r3, r4)
        L_0x0d55:
            r3 = 13
            int r3 = r2.indexOf(r3)
            if (r3 < 0) goto L_0x0d65
            r3 = 13
            r4 = 95
            java.lang.String r2 = r2.replace(r3, r4)
        L_0x0d65:
            r3 = 2
            java.lang.Object[] r4 = new java.lang.Object[r3]
            r3 = 0
            r4[r3] = r2
            java.lang.String r3 = r13.toString()
            r5 = 1
            r4[r5] = r3
            java.lang.String r3 = "wl"
            dumpLine(r9, r0, r12, r3, r4)
        L_0x0d78:
            int r6 = r14 + -1
            r176 = r118
            r14 = r180
            r7 = r194
            r118 = r103
            goto L_0x0cc3
        L_0x0d84:
            r194 = r7
            r180 = r14
            r103 = r118
            r118 = r176
            r14 = r6
            android.os.BatteryStats$Timer r14 = r11.getMulticastWakelockStats()
            if (r14 == 0) goto L_0x0dbd
            r6 = r181
            long r1 = r14.getTotalTimeLocked(r6, r10)
            r3 = 1000(0x3e8, double:4.94E-321)
            long r1 = r1 / r3
            int r3 = r14.getCountLocked(r10)
            int r4 = (r1 > r115 ? 1 : (r1 == r115 ? 0 : -1))
            if (r4 <= 0) goto L_0x0dbf
            r4 = 2
            java.lang.Object[] r5 = new java.lang.Object[r4]
            java.lang.Long r4 = java.lang.Long.valueOf(r1)
            r8 = 0
            r5[r8] = r4
            java.lang.Integer r4 = java.lang.Integer.valueOf(r3)
            r8 = 1
            r5[r8] = r4
            java.lang.String r4 = "wmc"
            dumpLine(r9, r0, r12, r4, r5)
            goto L_0x0dbf
        L_0x0dbd:
            r6 = r181
        L_0x0dbf:
            android.util.ArrayMap r8 = r11.getSyncStats()
            int r1 = r8.size()
            r2 = 1
            int r1 = r1 - r2
        L_0x0dc9:
            if (r1 < 0) goto L_0x0e6a
            java.lang.Object r2 = r8.valueAt(r1)
            android.os.BatteryStats$Timer r2 = (android.os.BatteryStats.Timer) r2
            long r3 = r2.getTotalTimeLocked(r6, r10)
            long r3 = r3 + r108
            r64 = 1000(0x3e8, double:4.94E-321)
            long r3 = r3 / r64
            int r5 = r2.getCountLocked(r10)
            r102 = r14
            android.os.BatteryStats$Timer r14 = r2.getSubTimer()
            if (r14 == 0) goto L_0x0df0
            r181 = r6
            r6 = r194
            long r176 = r14.getTotalDurationMsLocked(r6)
            goto L_0x0df6
        L_0x0df0:
            r181 = r6
            r6 = r194
            r176 = -1
        L_0x0df6:
            if (r14 == 0) goto L_0x0dfd
            int r178 = r14.getCountLocked(r10)
            goto L_0x0dff
        L_0x0dfd:
            r178 = r161
        L_0x0dff:
            int r179 = (r3 > r115 ? 1 : (r3 == r115 ? 0 : -1))
            if (r-77 == 0) goto L_0x0e50
            r179 = r2
            r183 = r14
            r2 = 5
            java.lang.Object[] r14 = new java.lang.Object[r2]
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r184 = r15
            r15 = r175
            r2.append(r15)
            java.lang.Object r175 = r8.keyAt(r1)
            r185 = r8
            r8 = r175
            java.lang.String r8 = (java.lang.String) r8
            r2.append(r8)
            r2.append(r15)
            java.lang.String r2 = r2.toString()
            r8 = 0
            r14[r8] = r2
            java.lang.Long r2 = java.lang.Long.valueOf(r3)
            r8 = 1
            r14[r8] = r2
            java.lang.Integer r2 = java.lang.Integer.valueOf(r5)
            r8 = 2
            r14[r8] = r2
            java.lang.Long r2 = java.lang.Long.valueOf(r176)
            r8 = 3
            r14[r8] = r2
            java.lang.Integer r2 = java.lang.Integer.valueOf(r178)
            r8 = 4
            r14[r8] = r2
            java.lang.String r2 = "sy"
            dumpLine(r9, r0, r12, r2, r14)
            goto L_0x0e5a
        L_0x0e50:
            r179 = r2
            r185 = r8
            r183 = r14
            r184 = r15
            r15 = r175
        L_0x0e5a:
            int r1 = r1 + -1
            r194 = r6
            r175 = r15
            r14 = r102
            r6 = r181
            r15 = r184
            r8 = r185
            goto L_0x0dc9
        L_0x0e6a:
            r181 = r6
            r185 = r8
            r102 = r14
            r184 = r15
            r15 = r175
            r6 = r194
            android.util.ArrayMap r14 = r11.getJobStats()
            int r1 = r14.size()
            r2 = 1
            int r1 = r1 - r2
        L_0x0e80:
            if (r1 < 0) goto L_0x0f00
            java.lang.Object r2 = r14.valueAt(r1)
            android.os.BatteryStats$Timer r2 = (android.os.BatteryStats.Timer) r2
            r4 = r181
            long r175 = r2.getTotalTimeLocked(r4, r10)
            long r175 = r175 + r108
            r64 = 1000(0x3e8, double:4.94E-321)
            long r175 = r175 / r64
            int r3 = r2.getCountLocked(r10)
            android.os.BatteryStats$Timer r8 = r2.getSubTimer()
            if (r8 == 0) goto L_0x0ea3
            long r177 = r8.getTotalDurationMsLocked(r6)
            goto L_0x0ea5
        L_0x0ea3:
            r177 = -1
        L_0x0ea5:
            if (r8 == 0) goto L_0x0eac
            int r179 = r8.getCountLocked(r10)
            goto L_0x0eae
        L_0x0eac:
            r179 = r161
        L_0x0eae:
            int r181 = (r175 > r115 ? 1 : (r175 == r115 ? 0 : -1))
            if (r-75 == 0) goto L_0x0ef7
            r181 = r2
            r182 = r4
            r2 = 5
            java.lang.Object[] r4 = new java.lang.Object[r2]
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r15)
            java.lang.Object r5 = r14.keyAt(r1)
            java.lang.String r5 = (java.lang.String) r5
            r2.append(r5)
            r2.append(r15)
            java.lang.String r2 = r2.toString()
            r5 = 0
            r4[r5] = r2
            java.lang.Long r2 = java.lang.Long.valueOf(r175)
            r5 = 1
            r4[r5] = r2
            java.lang.Integer r2 = java.lang.Integer.valueOf(r3)
            r5 = 2
            r4[r5] = r2
            java.lang.Long r2 = java.lang.Long.valueOf(r177)
            r5 = 3
            r4[r5] = r2
            java.lang.Integer r2 = java.lang.Integer.valueOf(r179)
            r5 = 4
            r4[r5] = r2
            java.lang.String r2 = "jb"
            dumpLine(r9, r0, r12, r2, r4)
            goto L_0x0efb
        L_0x0ef7:
            r181 = r2
            r182 = r4
        L_0x0efb:
            int r1 = r1 + -1
            r181 = r182
            goto L_0x0e80
        L_0x0f00:
            r182 = r181
            android.util.ArrayMap r8 = r11.getJobCompletionStats()
            int r1 = r8.size()
            r2 = 1
            int r1 = r1 - r2
        L_0x0f0c:
            if (r1 < 0) goto L_0x0f78
            java.lang.Object r2 = r8.valueAt(r1)
            android.util.SparseIntArray r2 = (android.util.SparseIntArray) r2
            if (r2 == 0) goto L_0x0f75
            r3 = 6
            java.lang.Object[] r4 = new java.lang.Object[r3]
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r15)
            java.lang.Object r5 = r8.keyAt(r1)
            java.lang.String r5 = (java.lang.String) r5
            r3.append(r5)
            r3.append(r15)
            java.lang.String r3 = r3.toString()
            r5 = 0
            r4[r5] = r3
            int r3 = r2.get(r5, r5)
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            r5 = 1
            r4[r5] = r3
            r3 = 0
            int r62 = r2.get(r5, r3)
            java.lang.Integer r5 = java.lang.Integer.valueOf(r62)
            r3 = 2
            r4[r3] = r5
            r5 = 0
            int r62 = r2.get(r3, r5)
            java.lang.Integer r3 = java.lang.Integer.valueOf(r62)
            r5 = 3
            r4[r5] = r3
            r3 = 0
            int r62 = r2.get(r5, r3)
            java.lang.Integer r5 = java.lang.Integer.valueOf(r62)
            r3 = 4
            r4[r3] = r5
            r5 = 0
            int r175 = r2.get(r3, r5)
            java.lang.Integer r3 = java.lang.Integer.valueOf(r175)
            r5 = 5
            r4[r5] = r3
            java.lang.String r3 = "jbc"
            dumpLine(r9, r0, r12, r3, r4)
        L_0x0f75:
            int r1 = r1 + -1
            goto L_0x0f0c
        L_0x0f78:
            r11.getDeferredJobsCheckinLineLocked(r13, r10)
            int r1 = r13.length()
            if (r1 <= 0) goto L_0x0f91
            r1 = 1
            java.lang.Object[] r2 = new java.lang.Object[r1]
            java.lang.String r1 = r13.toString()
            r3 = 0
            r2[r3] = r1
            java.lang.String r1 = "jbd"
            dumpLine(r9, r0, r12, r1, r2)
        L_0x0f91:
            android.os.BatteryStats$Timer r5 = r11.getFlashlightTurnedOnTimer()
            java.lang.String r4 = "fla"
            r1 = r203
            r2 = r0
            r3 = r12
            r175 = r182
            r178 = r14
            r177 = r15
            r181 = r175
            r14 = r6
            r6 = r181
            r176 = r8
            r196 = r177
            r175 = r185
            r8 = r204
            dumpTimer(r1, r2, r3, r4, r5, r6, r8)
            android.os.BatteryStats$Timer r5 = r11.getCameraTurnedOnTimer()
            java.lang.String r4 = "cam"
            dumpTimer(r1, r2, r3, r4, r5, r6, r8)
            android.os.BatteryStats$Timer r5 = r11.getVideoTurnedOnTimer()
            java.lang.String r4 = "vid"
            dumpTimer(r1, r2, r3, r4, r5, r6, r8)
            android.os.BatteryStats$Timer r5 = r11.getAudioTurnedOnTimer()
            java.lang.String r4 = "aud"
            dumpTimer(r1, r2, r3, r4, r5, r6, r8)
            android.util.SparseArray r8 = r11.getSensorStats()
            int r6 = r8.size()
            r1 = 0
        L_0x0fd6:
            if (r1 >= r6) goto L_0x106e
            java.lang.Object r2 = r8.valueAt(r1)
            android.os.BatteryStats$Uid$Sensor r2 = (android.os.BatteryStats.Uid.Sensor) r2
            int r3 = r8.keyAt(r1)
            android.os.BatteryStats$Timer r4 = r2.getSensorTime()
            if (r4 == 0) goto L_0x105e
            r177 = r6
            r6 = r181
            long r181 = r4.getTotalTimeLocked(r6, r10)
            long r181 = r181 + r108
            r64 = 1000(0x3e8, double:4.94E-321)
            long r181 = r181 / r64
            int r5 = (r181 > r115 ? 1 : (r181 == r115 ? 0 : -1))
            if (r5 == 0) goto L_0x1057
            int r5 = r4.getCountLocked(r10)
            r185 = r6
            android.os.BatteryStats$Timer r6 = r2.getSensorBackgroundTime()
            if (r6 == 0) goto L_0x100b
            int r7 = r6.getCountLocked(r10)
            goto L_0x100c
        L_0x100b:
            r7 = 0
        L_0x100c:
            long r187 = r4.getTotalDurationMsLocked(r14)
            if (r6 == 0) goto L_0x1017
            long r189 = r6.getTotalDurationMsLocked(r14)
            goto L_0x1019
        L_0x1017:
            r189 = r115
        L_0x1019:
            r179 = r2
            r183 = r4
            r2 = 6
            java.lang.Object[] r4 = new java.lang.Object[r2]
            java.lang.Integer r2 = java.lang.Integer.valueOf(r3)
            r62 = 0
            r4[r62] = r2
            java.lang.Long r2 = java.lang.Long.valueOf(r181)
            r63 = 1
            r4[r63] = r2
            java.lang.Integer r2 = java.lang.Integer.valueOf(r5)
            r78 = 2
            r4[r78] = r2
            java.lang.Integer r2 = java.lang.Integer.valueOf(r7)
            r66 = 3
            r4[r66] = r2
            java.lang.Long r2 = java.lang.Long.valueOf(r187)
            r67 = 4
            r4[r67] = r2
            java.lang.Long r2 = java.lang.Long.valueOf(r189)
            r68 = 5
            r4[r68] = r2
            java.lang.String r2 = "sr"
            dumpLine(r9, r0, r12, r2, r4)
            goto L_0x1066
        L_0x1057:
            r179 = r2
            r183 = r4
            r185 = r6
            goto L_0x1066
        L_0x105e:
            r179 = r2
            r183 = r4
            r177 = r6
            r185 = r181
        L_0x1066:
            int r1 = r1 + 1
            r6 = r177
            r181 = r185
            goto L_0x0fd6
        L_0x106e:
            r177 = r6
            r185 = r181
            android.os.BatteryStats$Timer r5 = r11.getVibratorOnTimer()
            java.lang.String r4 = "vib"
            r1 = r203
            r2 = r0
            r3 = r12
            r6 = r181
            r179 = r8
            r8 = r204
            dumpTimer(r1, r2, r3, r4, r5, r6, r8)
            android.os.BatteryStats$Timer r5 = r11.getForegroundActivityTimer()
            java.lang.String r4 = "fg"
            dumpTimer(r1, r2, r3, r4, r5, r6, r8)
            android.os.BatteryStats$Timer r5 = r11.getForegroundServiceTimer()
            java.lang.String r4 = "fgs"
            dumpTimer(r1, r2, r3, r4, r5, r6, r8)
            r1 = 7
            java.lang.Object[] r2 = new java.lang.Object[r1]
            r3 = 0
            r5 = 0
        L_0x109e:
            if (r5 >= r1) goto L_0x10ba
            r6 = r181
            long r181 = r11.getProcessStateTime(r5, r6, r10)
            long r3 = r3 + r181
            long r185 = r181 + r108
            r64 = 1000(0x3e8, double:4.94E-321)
            long r185 = r185 / r64
            java.lang.Long r1 = java.lang.Long.valueOf(r185)
            r2[r5] = r1
            int r5 = r5 + 1
            r181 = r6
            r1 = 7
            goto L_0x109e
        L_0x10ba:
            r6 = r181
            int r1 = (r3 > r115 ? 1 : (r3 == r115 ? 0 : -1))
            if (r1 <= 0) goto L_0x10c6
            java.lang.String r1 = "st"
            dumpLine(r9, r0, r12, r1, r2)
        L_0x10c6:
            long r181 = r11.getUserCpuTimeUs(r10)
            long r185 = r11.getSystemCpuTimeUs(r10)
            int r1 = (r181 > r115 ? 1 : (r181 == r115 ? 0 : -1))
            if (r1 > 0) goto L_0x10d6
            int r1 = (r185 > r115 ? 1 : (r185 == r115 ? 0 : -1))
            if (r1 <= 0) goto L_0x10f5
        L_0x10d6:
            r1 = 3
            java.lang.Object[] r5 = new java.lang.Object[r1]
            r64 = 1000(0x3e8, double:4.94E-321)
            long r187 = r181 / r64
            java.lang.Long r1 = java.lang.Long.valueOf(r187)
            r8 = 0
            r5[r8] = r1
            long r187 = r185 / r64
            java.lang.Long r1 = java.lang.Long.valueOf(r187)
            r8 = 1
            r5[r8] = r1
            r1 = 2
            r5[r1] = r60
            java.lang.String r1 = "cpu"
            dumpLine(r9, r0, r12, r1, r5)
        L_0x10f5:
            if (r-94 == 0) goto L_0x122b
            long[] r1 = r11.getCpuFreqTimes(r10)
            if (r1 == 0) goto L_0x118d
            int r5 = r1.length
            r8 = r162
            r162 = r2
            int r2 = r8.length
            if (r5 != r2) goto L_0x1188
            r2 = 0
            r13.setLength(r2)
            r2 = 0
        L_0x110a:
            int r5 = r1.length
            if (r2 >= r5) goto L_0x1131
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            if (r2 != 0) goto L_0x1117
            java.lang.String r183 = ""
            goto L_0x1119
        L_0x1117:
            java.lang.String r183 = ","
        L_0x1119:
            r187 = r3
            r3 = r183
            r5.append(r3)
            r3 = r1[r2]
            r5.append(r3)
            java.lang.String r3 = r5.toString()
            r13.append(r3)
            int r2 = r2 + 1
            r3 = r187
            goto L_0x110a
        L_0x1131:
            r187 = r3
            long[] r2 = r11.getScreenOffCpuFreqTimes(r10)
            if (r2 == 0) goto L_0x115d
            r3 = 0
        L_0x113a:
            int r4 = r2.length
            if (r3 >= r4) goto L_0x115a
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = ","
            r4.append(r5)
            r189 = r6
            r5 = r2[r3]
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            r13.append(r4)
            int r3 = r3 + 1
            r6 = r189
            goto L_0x113a
        L_0x115a:
            r189 = r6
            goto L_0x116b
        L_0x115d:
            r189 = r6
            r3 = 0
        L_0x1160:
            int r4 = r1.length
            if (r3 >= r4) goto L_0x116b
            java.lang.String r4 = ",0"
            r13.append(r4)
            int r3 = r3 + 1
            goto L_0x1160
        L_0x116b:
            r3 = 3
            java.lang.Object[] r4 = new java.lang.Object[r3]
            java.lang.String r3 = "A"
            r5 = 0
            r4[r5] = r3
            int r3 = r1.length
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            r5 = 1
            r4[r5] = r3
            java.lang.String r3 = r13.toString()
            r5 = 2
            r4[r5] = r3
            java.lang.String r3 = "ctf"
            dumpLine(r9, r0, r12, r3, r4)
            goto L_0x1195
        L_0x1188:
            r187 = r3
            r189 = r6
            goto L_0x1195
        L_0x118d:
            r187 = r3
            r189 = r6
            r8 = r162
            r162 = r2
        L_0x1195:
            r2 = 0
        L_0x1196:
            r3 = 7
            if (r2 >= r3) goto L_0x1228
            long[] r3 = r11.getCpuFreqTimes(r10, r2)
            if (r3 == 0) goto L_0x1220
            int r4 = r3.length
            int r5 = r8.length
            if (r4 != r5) goto L_0x1220
            r4 = 0
            r13.setLength(r4)
            r4 = 0
        L_0x11a8:
            int r5 = r3.length
            if (r4 >= r5) goto L_0x11c9
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            if (r4 != 0) goto L_0x11b5
            java.lang.String r6 = ""
            goto L_0x11b7
        L_0x11b5:
            java.lang.String r6 = ","
        L_0x11b7:
            r5.append(r6)
            r6 = r3[r4]
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            r13.append(r5)
            int r4 = r4 + 1
            goto L_0x11a8
        L_0x11c9:
            long[] r4 = r11.getScreenOffCpuFreqTimes(r10, r2)
            if (r4 == 0) goto L_0x11f3
            r5 = 0
        L_0x11d0:
            int r6 = r4.length
            if (r5 >= r6) goto L_0x11f0
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = ","
            r6.append(r7)
            r183 = r8
            r7 = r4[r5]
            r6.append(r7)
            java.lang.String r6 = r6.toString()
            r13.append(r6)
            int r5 = r5 + 1
            r8 = r183
            goto L_0x11d0
        L_0x11f0:
            r183 = r8
            goto L_0x1201
        L_0x11f3:
            r183 = r8
            r5 = 0
        L_0x11f6:
            int r6 = r3.length
            if (r5 >= r6) goto L_0x1201
            java.lang.String r6 = ",0"
            r13.append(r6)
            int r5 = r5 + 1
            goto L_0x11f6
        L_0x1201:
            r5 = 3
            java.lang.Object[] r6 = new java.lang.Object[r5]
            java.lang.String[] r5 = android.os.BatteryStats.Uid.UID_PROCESS_TYPES
            r5 = r5[r2]
            r7 = 0
            r6[r7] = r5
            int r5 = r3.length
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)
            r7 = 1
            r6[r7] = r5
            java.lang.String r5 = r13.toString()
            r7 = 2
            r6[r7] = r5
            java.lang.String r5 = "ctf"
            dumpLine(r9, r0, r12, r5, r6)
            goto L_0x1222
        L_0x1220:
            r183 = r8
        L_0x1222:
            int r2 = r2 + 1
            r8 = r183
            goto L_0x1196
        L_0x1228:
            r183 = r8
            goto L_0x1233
        L_0x122b:
            r187 = r3
            r189 = r6
            r183 = r162
            r162 = r2
        L_0x1233:
            android.util.ArrayMap r1 = r11.getProcessStats()
            int r2 = r1.size()
            r3 = 1
            int r2 = r2 - r3
        L_0x123e:
            if (r2 < 0) goto L_0x12d8
            java.lang.Object r3 = r1.valueAt(r2)
            android.os.BatteryStats$Uid$Proc r3 = (android.os.BatteryStats.Uid.Proc) r3
            long r4 = r3.getUserTime(r10)
            long r6 = r3.getSystemTime(r10)
            long r191 = r3.getForegroundTime(r10)
            int r8 = r3.getStarts(r10)
            int r193 = r3.getNumCrashes(r10)
            int r194 = r3.getNumAnrs(r10)
            int r195 = (r4 > r115 ? 1 : (r4 == r115 ? 0 : -1))
            if (r-61 != 0) goto L_0x1278
            int r195 = (r6 > r115 ? 1 : (r6 == r115 ? 0 : -1))
            if (r-61 != 0) goto L_0x1278
            int r195 = (r191 > r115 ? 1 : (r191 == r115 ? 0 : -1))
            if (r-61 != 0) goto L_0x1278
            if (r8 != 0) goto L_0x1278
            if (r-62 != 0) goto L_0x1278
            if (r-63 == 0) goto L_0x1271
            goto L_0x1278
        L_0x1271:
            r70 = r13
            r197 = r14
            r14 = r196
            goto L_0x12ce
        L_0x1278:
            r195 = r3
            r70 = r13
            r3 = 7
            java.lang.Object[] r13 = new java.lang.Object[r3]
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r197 = r14
            r14 = r196
            r3.append(r14)
            java.lang.Object r15 = r1.keyAt(r2)
            java.lang.String r15 = (java.lang.String) r15
            r3.append(r15)
            r3.append(r14)
            java.lang.String r3 = r3.toString()
            r15 = 0
            r13[r15] = r3
            java.lang.Long r3 = java.lang.Long.valueOf(r4)
            r15 = 1
            r13[r15] = r3
            java.lang.Long r3 = java.lang.Long.valueOf(r6)
            r15 = 2
            r13[r15] = r3
            java.lang.Long r3 = java.lang.Long.valueOf(r191)
            r15 = 3
            r13[r15] = r3
            java.lang.Integer r3 = java.lang.Integer.valueOf(r8)
            r15 = 4
            r13[r15] = r3
            java.lang.Integer r3 = java.lang.Integer.valueOf(r194)
            r15 = 5
            r13[r15] = r3
            java.lang.Integer r3 = java.lang.Integer.valueOf(r193)
            r15 = 6
            r13[r15] = r3
            java.lang.String r3 = "pr"
            dumpLine(r9, r0, r12, r3, r13)
        L_0x12ce:
            int r2 = r2 + -1
            r196 = r14
            r13 = r70
            r14 = r197
            goto L_0x123e
        L_0x12d8:
            r70 = r13
            r197 = r14
            r14 = r196
            android.util.ArrayMap r2 = r11.getPackageStats()
            int r3 = r2.size()
            r4 = 1
            int r3 = r3 - r4
        L_0x12e9:
            if (r3 < 0) goto L_0x13db
            java.lang.Object r4 = r2.valueAt(r3)
            android.os.BatteryStats$Uid$Pkg r4 = (android.os.BatteryStats.Uid.Pkg) r4
            r5 = 0
            android.util.ArrayMap r6 = r4.getWakeupAlarmStats()
            int r7 = r6.size()
            r8 = 1
            int r7 = r7 - r8
        L_0x12fc:
            if (r7 < 0) goto L_0x1332
            java.lang.Object r8 = r6.valueAt(r7)
            android.os.BatteryStats$Counter r8 = (android.os.BatteryStats.Counter) r8
            int r8 = r8.getCountLocked(r10)
            int r5 = r5 + r8
            java.lang.Object r13 = r6.keyAt(r7)
            java.lang.String r13 = (java.lang.String) r13
            r15 = 44
            r191 = r1
            r1 = 95
            java.lang.String r1 = r13.replace(r15, r1)
            r13 = 2
            java.lang.Object[] r15 = new java.lang.Object[r13]
            r13 = 0
            r15[r13] = r1
            java.lang.Integer r13 = java.lang.Integer.valueOf(r8)
            r63 = 1
            r15[r63] = r13
            java.lang.String r13 = "wua"
            dumpLine(r9, r0, r12, r13, r15)
            int r7 = r7 + -1
            r1 = r191
            goto L_0x12fc
        L_0x1332:
            r191 = r1
            android.util.ArrayMap r1 = r4.getServiceStats()
            int r7 = r1.size()
            r8 = 1
            int r7 = r7 - r8
        L_0x133e:
            if (r7 < 0) goto L_0x13bd
            java.lang.Object r8 = r1.valueAt(r7)
            android.os.BatteryStats$Uid$Pkg$Serv r8 = (android.os.BatteryStats.Uid.Pkg.Serv) r8
            r196 = r14
            r13 = r100
            long r100 = r8.getStartTime(r13, r10)
            int r15 = r8.getStarts(r10)
            int r192 = r8.getLaunches(r10)
            int r193 = (r100 > r115 ? 1 : (r100 == r115 ? 0 : -1))
            if (r-63 != 0) goto L_0x1372
            if (r15 != 0) goto L_0x1372
            if (r-64 == 0) goto L_0x135f
            goto L_0x1372
        L_0x135f:
            r193 = r4
            r69 = r6
            r62 = 0
            r63 = 1
            r64 = 1000(0x3e8, double:4.94E-321)
            r66 = 3
            r67 = 4
            r68 = 5
            r78 = 2
            goto L_0x13b2
        L_0x1372:
            r193 = r4
            r69 = r6
            r4 = 6
            java.lang.Object[] r6 = new java.lang.Object[r4]
            java.lang.Integer r194 = java.lang.Integer.valueOf(r5)
            r62 = 0
            r6[r62] = r194
            java.lang.Object r194 = r2.keyAt(r3)
            r63 = 1
            r6[r63] = r194
            java.lang.Object r194 = r1.keyAt(r7)
            r78 = 2
            r6[r78] = r194
            r64 = 1000(0x3e8, double:4.94E-321)
            long r194 = r100 / r64
            java.lang.Long r194 = java.lang.Long.valueOf(r194)
            r66 = 3
            r6[r66] = r194
            java.lang.Integer r194 = java.lang.Integer.valueOf(r15)
            r67 = 4
            r6[r67] = r194
            java.lang.Integer r194 = java.lang.Integer.valueOf(r192)
            r68 = 5
            r6[r68] = r194
            java.lang.String r4 = "apk"
            dumpLine(r9, r0, r12, r4, r6)
        L_0x13b2:
            int r7 = r7 + -1
            r100 = r13
            r6 = r69
            r4 = r193
            r14 = r196
            goto L_0x133e
        L_0x13bd:
            r193 = r4
            r69 = r6
            r196 = r14
            r13 = r100
            r62 = 0
            r63 = 1
            r64 = 1000(0x3e8, double:4.94E-321)
            r66 = 3
            r67 = 4
            r68 = 5
            r78 = 2
            int r3 = r3 + -1
            r1 = r191
            r14 = r196
            goto L_0x12e9
        L_0x13db:
            r191 = r1
            r196 = r14
            r13 = r100
            r62 = 0
            r63 = 1
            r64 = 1000(0x3e8, double:4.94E-321)
            r66 = 3
            r67 = 4
            r68 = 5
            r78 = 2
        L_0x13ef:
            int r7 = r118 + 1
            r100 = r13
            r13 = r70
            r118 = r103
            r11 = r111
            r79 = r159
            r114 = r160
            r0 = r183
            r14 = r189
            r8 = r196
            r102 = r197
            r111 = r110
            goto L_0x08ad
        L_0x1409:
            return
            switch-data {1->0x0818, 2->0x080f, 3->0x0807, 4->0x07fe, 5->0x07f5, 6->0x07ed, 7->0x07e4, 8->0x07dc, 9->0x07d0, 10->0x07c3, 11->0x07b9, 12->0x07af, 13->0x07a6, 14->0x079c, }
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.BatteryStats.dumpCheckinLocked(android.content.Context, java.io.PrintWriter, int, int, boolean):void");
    }

    /* access modifiers changed from: package-private */
    public static final class TimerEntry {
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

    private void printmAh(PrintWriter printer, double power) {
        printer.print(BatteryStatsHelper.makemAh(power));
    }

    private void printmAh(StringBuilder sb, double power) {
        sb.append(BatteryStatsHelper.makemAh(power));
    }

    public final void dumpLocked(Context context, PrintWriter pw, String prefix, int which, int reqUid) {
        dumpLocked(context, pw, prefix, which, reqUid, BatteryStatsHelper.checkWifiOnly(context));
    }

    /*  JADX ERROR: IndexOutOfBoundsException in pass: SSATransform
        java.lang.IndexOutOfBoundsException: bitIndex < 0: -25
        	at java.util.BitSet.get(BitSet.java:623)
        	at jadx.core.dex.visitors.ssa.LiveVarAnalysis.fillBasicBlockInfo(LiveVarAnalysis.java:65)
        	at jadx.core.dex.visitors.ssa.LiveVarAnalysis.runAnalysis(LiveVarAnalysis.java:36)
        	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:50)
        	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:41)
        */
    /*  JADX ERROR: NullPointerException in pass: ConstInlineVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ConstInlineVisitor.checkInsn(ConstInlineVisitor.java:80)
        	at jadx.core.dex.visitors.ConstInlineVisitor.process(ConstInlineVisitor.java:52)
        	at jadx.core.dex.visitors.ConstInlineVisitor.visit(ConstInlineVisitor.java:44)
        */
    /*  JADX ERROR: NullPointerException in pass: ConstructorVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.instructions.mods.ConstructorInsn.<init>(ConstructorInsn.java:47)
        	at jadx.core.dex.visitors.ConstructorVisitor.processInvoke(ConstructorVisitor.java:64)
        	at jadx.core.dex.visitors.ConstructorVisitor.replaceInvoke(ConstructorVisitor.java:48)
        	at jadx.core.dex.visitors.ConstructorVisitor.visit(ConstructorVisitor.java:37)
        */
    /*  JADX ERROR: NullPointerException in pass: InitCodeVariables
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.InitCodeVariables.initCodeVar(InitCodeVariables.java:56)
        	at jadx.core.dex.visitors.InitCodeVariables.initCodeVars(InitCodeVariables.java:45)
        	at jadx.core.dex.visitors.InitCodeVariables.visit(InitCodeVariables.java:32)
        */
    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.nodes.InsnNode.rebindArgs(InsnNode.java:420)
        	at jadx.core.utils.BlockUtils.replaceInsn(BlockUtils.java:657)
        	at jadx.core.dex.visitors.ModVisitor.replaceConst(ModVisitor.java:323)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:114)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:93)
        */
    /*  JADX ERROR: NullPointerException in pass: MoveInlineVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.MoveInlineVisitor.processMove(MoveInlineVisitor.java:54)
        	at jadx.core.dex.visitors.MoveInlineVisitor.moveInline(MoveInlineVisitor.java:39)
        	at jadx.core.dex.visitors.MoveInlineVisitor.visit(MoveInlineVisitor.java:30)
        */
    /*  JADX ERROR: NullPointerException in pass: PrepareForCodeGen
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.PrepareForCodeGen.removeInstructions(PrepareForCodeGen.java:98)
        	at jadx.core.dex.visitors.PrepareForCodeGen.visit(PrepareForCodeGen.java:68)
        */
    /*  JADX ERROR: NullPointerException in pass: RegionMakerVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.regions.IfMakerHelper.getNextIfNodeInfo(IfMakerHelper.java:401)
        	at jadx.core.dex.visitors.regions.IfMakerHelper.getNextIf(IfMakerHelper.java:359)
        	at jadx.core.dex.visitors.regions.IfMakerHelper.mergeNestedIfNodes(IfMakerHelper.java:163)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:664)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:125)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:88)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:50)
        */
    /*  JADX ERROR: NullPointerException in pass: SimplifyVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.SimplifyVisitor.collectUseChain(SimplifyVisitor.java:312)
        	at jadx.core.dex.visitors.SimplifyVisitor.convertInvoke(SimplifyVisitor.java:303)
        	at jadx.core.dex.visitors.SimplifyVisitor.simplifyInsn(SimplifyVisitor.java:141)
        	at jadx.core.dex.visitors.SimplifyVisitor.simplifyBlock(SimplifyVisitor.java:83)
        	at jadx.core.dex.visitors.SimplifyVisitor.visit(SimplifyVisitor.java:68)
        */
    public final void dumpLocked(android.content.Context r227, java.io.PrintWriter r228, java.lang.String r229, int r230, int r231, boolean r232) {
        /*
            r226 = this;
            r7 = r226
            r15 = r228
            r14 = r229
            r13 = r230
            r11 = r231
            if (r13 == 0) goto L_0x0026
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "ERROR: BatteryStats.dump called for which type "
            r0.append(r1)
            r0.append(r13)
            java.lang.String r1 = " but only STATS_SINCE_CHARGED is supported"
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            r15.println(r0)
            return
        L_0x0026:
            long r0 = android.os.SystemClock.uptimeMillis()
            r16 = 1000(0x3e8, double:4.94E-321)
            long r9 = r0 * r16
            long r0 = android.os.SystemClock.elapsedRealtime()
            long r5 = r0 * r16
            r18 = 500(0x1f4, double:2.47E-321)
            long r0 = r5 + r18
            long r3 = r0 / r16
            long r1 = r7.getBatteryUptime(r9)
            r20 = r3
            long r3 = r7.computeBatteryUptime(r9, r13)
            r22 = r1
            long r1 = r7.computeBatteryRealtime(r5, r13)
            long r11 = r7.computeRealtime(r5, r13)
            long r24 = r7.computeUptime(r9, r13)
            r26 = r3
            long r3 = r7.computeBatteryScreenOffUptime(r9, r13)
            r28 = r9
            long r9 = r7.computeBatteryScreenOffRealtime(r5, r13)
            long r30 = r7.computeBatteryTimeRemaining(r5)
            long r32 = r7.computeChargeTimeRemaining(r5)
            r34 = r3
            long r3 = r7.getScreenDozeTime(r5, r13)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r8 = 128(0x80, float:1.794E-43)
            r0.<init>(r8)
            r8 = r0
            android.util.SparseArray r0 = r226.getUidStats()
            r36 = r5
            int r5 = r0.size()
            int r6 = r226.getEstimatedBatteryCapacity()
            r38 = r0
            java.lang.String r0 = " mAh"
            r39 = r5
            r5 = 0
            if (r6 <= 0) goto L_0x00ab
            r8.setLength(r5)
            r8.append(r14)
            java.lang.String r5 = "  Estimated battery capacity: "
            r8.append(r5)
            r41 = r3
            double r3 = (double) r6
            java.lang.String r3 = com.android.internal.os.BatteryStatsHelper.makemAh(r3)
            r8.append(r3)
            r8.append(r0)
            java.lang.String r3 = r8.toString()
            r15.println(r3)
            goto L_0x00ad
        L_0x00ab:
            r41 = r3
        L_0x00ad:
            int r5 = r226.getMinLearnedBatteryCapacity()
            if (r5 <= 0) goto L_0x00d3
            r3 = 0
            r8.setLength(r3)
            r8.append(r14)
            java.lang.String r3 = "  Min learned battery capacity: "
            r8.append(r3)
            int r3 = r5 / 1000
            double r3 = (double) r3
            java.lang.String r3 = com.android.internal.os.BatteryStatsHelper.makemAh(r3)
            r8.append(r3)
            r8.append(r0)
            java.lang.String r3 = r8.toString()
            r15.println(r3)
        L_0x00d3:
            int r4 = r226.getMaxLearnedBatteryCapacity()
            if (r4 <= 0) goto L_0x00fc
            r3 = 0
            r8.setLength(r3)
            r8.append(r14)
            java.lang.String r3 = "  Max learned battery capacity: "
            r8.append(r3)
            int r3 = r4 / 1000
            r43 = r4
            double r3 = (double) r3
            java.lang.String r3 = com.android.internal.os.BatteryStatsHelper.makemAh(r3)
            r8.append(r3)
            r8.append(r0)
            java.lang.String r3 = r8.toString()
            r15.println(r3)
            goto L_0x00fe
        L_0x00fc:
            r43 = r4
        L_0x00fe:
            r3 = 0
            r8.setLength(r3)
            r8.append(r14)
            java.lang.String r3 = "  Time on battery: "
            r8.append(r3)
            long r3 = r1 / r16
            formatTimeMs(r8, r3)
            java.lang.String r4 = "("
            r8.append(r4)
            java.lang.String r3 = r7.formatRatioLocked(r1, r11)
            r8.append(r3)
            java.lang.String r3 = ") realtime, "
            r8.append(r3)
            r45 = r5
            r44 = r6
            long r5 = r26 / r16
            formatTimeMs(r8, r5)
            r8.append(r4)
            r5 = r26
            java.lang.String r3 = r7.formatRatioLocked(r5, r1)
            r8.append(r3)
            java.lang.String r3 = ") uptime"
            r8.append(r3)
            java.lang.String r3 = r8.toString()
            r15.println(r3)
            r3 = 0
            r8.setLength(r3)
            r8.append(r14)
            java.lang.String r3 = "  Time on battery screen off: "
            r8.append(r3)
            long r5 = r9 / r16
            formatTimeMs(r8, r5)
            r8.append(r4)
            java.lang.String r3 = r7.formatRatioLocked(r9, r1)
            r8.append(r3)
            java.lang.String r3 = ") realtime, "
            r8.append(r3)
            long r5 = r34 / r16
            formatTimeMs(r8, r5)
            r8.append(r4)
            r5 = r34
            java.lang.String r3 = r7.formatRatioLocked(r5, r1)
            r8.append(r3)
            java.lang.String r3 = ") uptime"
            r8.append(r3)
            java.lang.String r3 = r8.toString()
            r15.println(r3)
            r3 = 0
            r8.setLength(r3)
            r8.append(r14)
            java.lang.String r3 = "  Time on battery screen doze: "
            r8.append(r3)
            long r5 = r41 / r16
            formatTimeMs(r8, r5)
            r8.append(r4)
            r5 = r41
            java.lang.String r3 = r7.formatRatioLocked(r5, r1)
            r8.append(r3)
            java.lang.String r3 = ")"
            r8.append(r3)
            java.lang.String r5 = r8.toString()
            r15.println(r5)
            r5 = 0
            r8.setLength(r5)
            r8.append(r14)
            java.lang.String r5 = "  Total run time: "
            r8.append(r5)
            long r5 = r11 / r16
            formatTimeMs(r8, r5)
            java.lang.String r5 = "realtime, "
            r8.append(r5)
            long r5 = r24 / r16
            formatTimeMs(r8, r5)
            java.lang.String r5 = "uptime"
            r8.append(r5)
            java.lang.String r5 = r8.toString()
            r15.println(r5)
            r46 = 0
            int r5 = (r30 > r46 ? 1 : (r30 == r46 ? 0 : -1))
            if (r5 < 0) goto L_0x01f1
            r5 = 0
            r8.setLength(r5)
            r8.append(r14)
            java.lang.String r5 = "  Battery time remaining: "
            r8.append(r5)
            long r5 = r30 / r16
            formatTimeMs(r8, r5)
            java.lang.String r5 = r8.toString()
            r15.println(r5)
        L_0x01f1:
            int r5 = (r32 > r46 ? 1 : (r32 == r46 ? 0 : -1))
            if (r5 < 0) goto L_0x020d
            r5 = 0
            r8.setLength(r5)
            r8.append(r14)
            java.lang.String r5 = "  Charge time remaining: "
            r8.append(r5)
            long r5 = r32 / r16
            formatTimeMs(r8, r5)
            java.lang.String r5 = r8.toString()
            r15.println(r5)
        L_0x020d:
            long r5 = r7.getUahDischarge(r13)
            int r48 = (r5 > r46 ? 1 : (r5 == r46 ? 0 : -1))
            if (r48 < 0) goto L_0x023d
            r48 = r9
            r9 = 0
            r8.setLength(r9)
            r8.append(r14)
            java.lang.String r9 = "  Discharge: "
            r8.append(r9)
            double r9 = (double) r5
            r50 = 4652007308841189376(0x408f400000000000, double:1000.0)
            double r9 = r9 / r50
            java.lang.String r9 = com.android.internal.os.BatteryStatsHelper.makemAh(r9)
            r8.append(r9)
            r8.append(r0)
            java.lang.String r9 = r8.toString()
            r15.println(r9)
            goto L_0x023f
        L_0x023d:
            r48 = r9
        L_0x023f:
            long r9 = r7.getUahDischargeScreenOff(r13)
            int r50 = (r9 > r46 ? 1 : (r9 == r46 ? 0 : -1))
            if (r50 < 0) goto L_0x026f
            r50 = r11
            r11 = 0
            r8.setLength(r11)
            r8.append(r14)
            java.lang.String r11 = "  Screen off discharge: "
            r8.append(r11)
            double r11 = (double) r9
            r52 = 4652007308841189376(0x408f400000000000, double:1000.0)
            double r11 = r11 / r52
            java.lang.String r11 = com.android.internal.os.BatteryStatsHelper.makemAh(r11)
            r8.append(r11)
            r8.append(r0)
            java.lang.String r11 = r8.toString()
            r15.println(r11)
            goto L_0x0271
        L_0x026f:
            r50 = r11
        L_0x0271:
            long r11 = r7.getUahDischargeScreenDoze(r13)
            int r52 = (r11 > r46 ? 1 : (r11 == r46 ? 0 : -1))
            if (r52 < 0) goto L_0x02a3
            r52 = r3
            r3 = 0
            r8.setLength(r3)
            r8.append(r14)
            java.lang.String r3 = "  Screen doze discharge: "
            r8.append(r3)
            r53 = r1
            double r1 = (double) r11
            r55 = 4652007308841189376(0x408f400000000000, double:1000.0)
            double r1 = r1 / r55
            java.lang.String r1 = com.android.internal.os.BatteryStatsHelper.makemAh(r1)
            r8.append(r1)
            r8.append(r0)
            java.lang.String r1 = r8.toString()
            r15.println(r1)
            goto L_0x02a7
        L_0x02a3:
            r53 = r1
            r52 = r3
        L_0x02a7:
            long r2 = r5 - r9
            int r1 = (r2 > r46 ? 1 : (r2 == r46 ? 0 : -1))
            if (r1 < 0) goto L_0x02d5
            r1 = 0
            r8.setLength(r1)
            r8.append(r14)
            java.lang.String r1 = "  Screen on discharge: "
            r8.append(r1)
            r55 = r5
            double r5 = (double) r2
            r57 = 4652007308841189376(0x408f400000000000, double:1000.0)
            double r5 = r5 / r57
            java.lang.String r1 = com.android.internal.os.BatteryStatsHelper.makemAh(r5)
            r8.append(r1)
            r8.append(r0)
            java.lang.String r1 = r8.toString()
            r15.println(r1)
            goto L_0x02d7
        L_0x02d5:
            r55 = r5
        L_0x02d7:
            long r5 = r7.getUahDischargeLightDoze(r13)
            int r1 = (r5 > r46 ? 1 : (r5 == r46 ? 0 : -1))
            if (r1 < 0) goto L_0x0307
            r1 = 0
            r8.setLength(r1)
            r8.append(r14)
            java.lang.String r1 = "  Device light doze discharge: "
            r8.append(r1)
            r57 = r2
            double r1 = (double) r5
            r59 = 4652007308841189376(0x408f400000000000, double:1000.0)
            double r1 = r1 / r59
            java.lang.String r1 = com.android.internal.os.BatteryStatsHelper.makemAh(r1)
            r8.append(r1)
            r8.append(r0)
            java.lang.String r1 = r8.toString()
            r15.println(r1)
            goto L_0x0309
        L_0x0307:
            r57 = r2
        L_0x0309:
            long r2 = r7.getUahDischargeDeepDoze(r13)
            int r1 = (r2 > r46 ? 1 : (r2 == r46 ? 0 : -1))
            if (r1 < 0) goto L_0x0339
            r1 = 0
            r8.setLength(r1)
            r8.append(r14)
            java.lang.String r1 = "  Device deep doze discharge: "
            r8.append(r1)
            r59 = r5
            double r5 = (double) r2
            r61 = 4652007308841189376(0x408f400000000000, double:1000.0)
            double r5 = r5 / r61
            java.lang.String r1 = com.android.internal.os.BatteryStatsHelper.makemAh(r5)
            r8.append(r1)
            r8.append(r0)
            java.lang.String r0 = r8.toString()
            r15.println(r0)
            goto L_0x033b
        L_0x0339:
            r59 = r5
        L_0x033b:
            java.lang.String r0 = "  Start clock time: "
            r15.print(r0)
            long r0 = r226.getStartClockTime()
            java.lang.String r5 = "yyyy-MM-dd-HH-mm-ss"
            java.lang.CharSequence r0 = android.text.format.DateFormat.format(r5, r0)
            java.lang.String r0 = r0.toString()
            r15.println(r0)
            r5 = r36
            long r0 = r7.getScreenOnTime(r5, r13)
            r36 = r11
            long r11 = r7.getInteractiveTime(r5, r13)
            r61 = r9
            long r9 = r7.getPowerSaveModeEnabledTime(r5, r13)
            r63 = r2
            r3 = 1
            r65 = r9
            long r9 = r7.getDeviceIdleModeTime(r3, r5, r13)
            r2 = 2
            r67 = r9
            long r9 = r7.getDeviceIdleModeTime(r2, r5, r13)
            r69 = r9
            long r9 = r7.getDeviceIdlingTime(r3, r5, r13)
            r71 = r4
            long r3 = r7.getDeviceIdlingTime(r2, r5, r13)
            r74 = r3
            long r2 = r7.getPhoneOnTime(r5, r13)
            long r76 = r7.getGlobalWifiRunningTime(r5, r13)
            long r78 = r7.getWifiOnTime(r5, r13)
            r4 = 0
            r8.setLength(r4)
            r8.append(r14)
            java.lang.String r4 = "  Screen on: "
            r8.append(r4)
            r80 = r2
            long r2 = r0 / r16
            formatTimeMs(r8, r2)
            r4 = r71
            r8.append(r4)
            r2 = r53
            r53 = r9
            java.lang.String r9 = r7.formatRatioLocked(r0, r2)
            r8.append(r9)
            java.lang.String r10 = ") "
            r8.append(r10)
            int r9 = r7.getScreenOnCount(r13)
            r8.append(r9)
            java.lang.String r9 = "x, Interactive: "
            r8.append(r9)
            r71 = r10
            long r9 = r11 / r16
            formatTimeMs(r8, r9)
            r8.append(r4)
            java.lang.String r9 = r7.formatRatioLocked(r11, r2)
            r8.append(r9)
            r9 = r52
            r8.append(r9)
            java.lang.String r10 = r8.toString()
            r15.println(r10)
            r10 = 0
            r8.setLength(r10)
            r8.append(r14)
            java.lang.String r10 = "  Screen brightnesses:"
            r8.append(r10)
            r10 = 0
            r52 = 0
            r218 = r52
            r52 = r10
            r10 = r218
        L_0x03f5:
            r82 = r11
            r11 = 5
            java.lang.String r12 = " "
            if (r10 >= r11) goto L_0x0437
            r84 = r2
            long r2 = r7.getScreenBrightnessTime(r10, r5, r13)
            int r11 = (r2 > r46 ? 1 : (r2 == r46 ? 0 : -1))
            if (r11 != 0) goto L_0x0407
            goto L_0x0430
        L_0x0407:
            java.lang.String r11 = "\n    "
            r8.append(r11)
            r8.append(r14)
            r11 = 1
            java.lang.String[] r52 = android.os.BatteryStats.SCREEN_BRIGHTNESS_NAMES
            r86 = r11
            r11 = r52[r10]
            r8.append(r11)
            r8.append(r12)
            long r11 = r2 / r16
            formatTimeMs(r8, r11)
            r8.append(r4)
            java.lang.String r11 = r7.formatRatioLocked(r2, r0)
            r8.append(r11)
            r8.append(r9)
            r52 = r86
        L_0x0430:
            int r10 = r10 + 1
            r11 = r82
            r2 = r84
            goto L_0x03f5
        L_0x0437:
            r84 = r2
            if (r52 != 0) goto L_0x0440
            java.lang.String r2 = " (no activity)"
            r8.append(r2)
        L_0x0440:
            java.lang.String r2 = r8.toString()
            r15.println(r2)
            int r2 = (r65 > r46 ? 1 : (r65 == r46 ? 0 : -1))
            if (r2 == 0) goto L_0x0477
            r2 = 0
            r8.setLength(r2)
            r8.append(r14)
            java.lang.String r2 = "  Power save mode enabled: "
            r8.append(r2)
            long r2 = r65 / r16
            formatTimeMs(r8, r2)
            r8.append(r4)
            r10 = r65
            r2 = r84
            r65 = r0
            java.lang.String r0 = r7.formatRatioLocked(r10, r2)
            r8.append(r0)
            r8.append(r9)
            java.lang.String r0 = r8.toString()
            r15.println(r0)
            goto L_0x047d
        L_0x0477:
            r10 = r65
            r2 = r84
            r65 = r0
        L_0x047d:
            int r0 = (r53 > r46 ? 1 : (r53 == r46 ? 0 : -1))
            if (r0 == 0) goto L_0x04bd
            r0 = 0
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r0 = "  Device light idling: "
            r8.append(r0)
            long r0 = r53 / r16
            formatTimeMs(r8, r0)
            r8.append(r4)
            r0 = r53
            r53 = r10
            java.lang.String r10 = r7.formatRatioLocked(r0, r2)
            r8.append(r10)
            r10 = r71
            r8.append(r10)
            r84 = r0
            r11 = 1
            int r0 = r7.getDeviceIdlingCount(r11, r13)
            r8.append(r0)
            java.lang.String r0 = "x"
            r8.append(r0)
            java.lang.String r0 = r8.toString()
            r15.println(r0)
            goto L_0x04c3
        L_0x04bd:
            r84 = r53
            r53 = r10
            r10 = r71
        L_0x04c3:
            int r0 = (r67 > r46 ? 1 : (r67 == r46 ? 0 : -1))
            if (r0 == 0) goto L_0x0508
            r0 = 0
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r0 = "  Idle mode light time: "
            r8.append(r0)
            long r0 = r67 / r16
            formatTimeMs(r8, r0)
            r8.append(r4)
            r0 = r67
            java.lang.String r11 = r7.formatRatioLocked(r0, r2)
            r8.append(r11)
            r8.append(r10)
            r11 = 1
            int r0 = r7.getDeviceIdleModeCount(r11, r13)
            r8.append(r0)
            java.lang.String r0 = "x"
            r8.append(r0)
            java.lang.String r0 = " -- longest "
            r8.append(r0)
            long r0 = r7.getLongestDeviceIdleModeTime(r11)
            formatTimeMs(r8, r0)
            java.lang.String r0 = r8.toString()
            r15.println(r0)
        L_0x0508:
            int r0 = (r74 > r46 ? 1 : (r74 == r46 ? 0 : -1))
            if (r0 == 0) goto L_0x0541
            r0 = 0
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r0 = "  Device full idling: "
            r8.append(r0)
            long r0 = r74 / r16
            formatTimeMs(r8, r0)
            r8.append(r4)
            r0 = r74
            java.lang.String r11 = r7.formatRatioLocked(r0, r2)
            r8.append(r11)
            r8.append(r10)
            r11 = 2
            int r0 = r7.getDeviceIdlingCount(r11, r13)
            r8.append(r0)
            java.lang.String r0 = "x"
            r8.append(r0)
            java.lang.String r0 = r8.toString()
            r15.println(r0)
        L_0x0541:
            int r0 = (r69 > r46 ? 1 : (r69 == r46 ? 0 : -1))
            if (r0 == 0) goto L_0x0586
            r0 = 0
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r0 = "  Idle mode full time: "
            r8.append(r0)
            long r0 = r69 / r16
            formatTimeMs(r8, r0)
            r8.append(r4)
            r0 = r69
            java.lang.String r11 = r7.formatRatioLocked(r0, r2)
            r8.append(r11)
            r8.append(r10)
            r11 = 2
            int r0 = r7.getDeviceIdleModeCount(r11, r13)
            r8.append(r0)
            java.lang.String r0 = "x"
            r8.append(r0)
            java.lang.String r0 = " -- longest "
            r8.append(r0)
            long r0 = r7.getLongestDeviceIdleModeTime(r11)
            formatTimeMs(r8, r0)
            java.lang.String r0 = r8.toString()
            r15.println(r0)
        L_0x0586:
            int r0 = (r80 > r46 ? 1 : (r80 == r46 ? 0 : -1))
            if (r0 == 0) goto L_0x05b8
            r0 = 0
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r0 = "  Active phone call: "
            r8.append(r0)
            long r0 = r80 / r16
            formatTimeMs(r8, r0)
            r8.append(r4)
            r0 = r80
            java.lang.String r11 = r7.formatRatioLocked(r0, r2)
            r8.append(r11)
            r8.append(r10)
            int r11 = r7.getPhoneOnCount(r13)
            r8.append(r11)
            java.lang.String r11 = "x"
            r8.append(r11)
            goto L_0x05ba
        L_0x05b8:
            r0 = r80
        L_0x05ba:
            int r11 = r7.getNumConnectivityChange(r13)
            if (r11 == 0) goto L_0x05ce
            r228.print(r229)
            r80 = r0
            java.lang.String r0 = "  Connectivity changes: "
            r15.print(r0)
            r15.println(r11)
            goto L_0x05d0
        L_0x05ce:
            r80 = r0
        L_0x05d0:
            r0 = 0
            r86 = 0
            java.util.ArrayList r71 = new java.util.ArrayList
            r71.<init>()
            r88 = r71
            r71 = 0
            r89 = r86
            r86 = r0
            r0 = r71
        L_0x05e3:
            r1 = r39
            if (r0 >= r1) goto L_0x0692
            r39 = r1
            r1 = r38
            java.lang.Object r38 = r1.valueAt(r0)
            android.os.BatteryStats$Uid r38 = (android.os.BatteryStats.Uid) r38
            r71 = r1
            android.util.ArrayMap r1 = r38.getWakelockStats()
            if (r1 == 0) goto L_0x0678
            int r91 = r1.size()
            r92 = r11
            r11 = 1
            int r91 = r91 + -1
            r11 = r91
        L_0x0605:
            if (r11 < 0) goto L_0x066f
            java.lang.Object r91 = r1.valueAt(r11)
            r93 = r10
            r10 = r91
            android.os.BatteryStats$Uid$Wakelock r10 = (android.os.BatteryStats.Uid.Wakelock) r10
            r94 = r9
            r91 = r12
            r12 = 1
            android.os.BatteryStats$Timer r9 = r10.getWakeTime(r12)
            if (r9 == 0) goto L_0x0622
            long r95 = r9.getTotalTimeLocked(r5, r13)
            long r86 = r86 + r95
        L_0x0622:
            r101 = r9
            r12 = 0
            android.os.BatteryStats$Timer r9 = r10.getWakeTime(r12)
            if (r9 == 0) goto L_0x0660
            long r102 = r9.getTotalTimeLocked(r5, r13)
            int r12 = (r102 > r46 ? 1 : (r102 == r46 ? 0 : -1))
            if (r12 <= 0) goto L_0x065b
            if (r-25 >= 0) goto L_0x0654
            android.os.BatteryStats$TimerEntry r12 = new android.os.BatteryStats$TimerEntry
            java.lang.Object r95 = r1.keyAt(r11)
            r96 = r95
            java.lang.String r96 = (java.lang.String) r96
            int r97 = r38.getUid()
            r95 = r12
            r98 = r9
            r99 = r102
            r95.<init>(r96, r97, r98, r99)
            r95 = r10
            r10 = r88
            r10.add(r12)
            goto L_0x0658
        L_0x0654:
            r95 = r10
            r10 = r88
        L_0x0658:
            long r89 = r89 + r102
            goto L_0x0664
        L_0x065b:
            r95 = r10
            r10 = r88
            goto L_0x0664
        L_0x0660:
            r95 = r10
            r10 = r88
        L_0x0664:
            int r11 = r11 + -1
            r88 = r10
            r12 = r91
            r10 = r93
            r9 = r94
            goto L_0x0605
        L_0x066f:
            r94 = r9
            r93 = r10
            r91 = r12
            r10 = r88
            goto L_0x0682
        L_0x0678:
            r94 = r9
            r93 = r10
            r92 = r11
            r91 = r12
            r10 = r88
        L_0x0682:
            int r0 = r0 + 1
            r88 = r10
            r38 = r71
            r12 = r91
            r11 = r92
            r10 = r93
            r9 = r94
            goto L_0x05e3
        L_0x0692:
            r39 = r1
            r94 = r9
            r93 = r10
            r92 = r11
            r91 = r12
            r71 = r38
            r10 = r88
            r0 = 0
            long r11 = r7.getNetworkActivityBytes(r0, r13)
            r9 = 1
            long r0 = r7.getNetworkActivityBytes(r9, r13)
            r38 = r10
            r95 = r11
            r9 = 2
            long r10 = r7.getNetworkActivityBytes(r9, r13)
            r12 = 3
            r97 = r10
            long r9 = r7.getNetworkActivityBytes(r12, r13)
            r99 = r9
            r11 = 0
            long r9 = r7.getNetworkActivityPackets(r11, r13)
            r101 = r9
            r11 = 1
            long r9 = r7.getNetworkActivityPackets(r11, r13)
            r72 = r9
            r12 = 2
            long r9 = r7.getNetworkActivityPackets(r12, r13)
            r11 = 3
            long r11 = r7.getNetworkActivityPackets(r11, r13)
            r104 = r0
            r0 = 4
            long r0 = r7.getNetworkActivityBytes(r0, r13)
            r106 = r0
            r0 = 5
            long r0 = r7.getNetworkActivityBytes(r0, r13)
            int r108 = (r86 > r46 ? 1 : (r86 == r46 ? 0 : -1))
            if (r108 == 0) goto L_0x0703
            r108 = r0
            r0 = 0
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r0 = "  Total full wakelock time: "
            r8.append(r0)
            long r0 = r86 + r18
            long r0 = r0 / r16
            formatTimeMsNoSpace(r8, r0)
            java.lang.String r0 = r8.toString()
            r15.println(r0)
            goto L_0x0705
        L_0x0703:
            r108 = r0
        L_0x0705:
            int r0 = (r89 > r46 ? 1 : (r89 == r46 ? 0 : -1))
            if (r0 == 0) goto L_0x0723
            r0 = 0
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r0 = "  Total partial wakelock time: "
            r8.append(r0)
            long r0 = r89 + r18
            long r0 = r0 / r16
            formatTimeMsNoSpace(r8, r0)
            java.lang.String r0 = r8.toString()
            r15.println(r0)
        L_0x0723:
            long r110 = r7.getWifiMulticastWakelockTime(r5, r13)
            int r1 = r7.getWifiMulticastWakelockCount(r13)
            int r0 = (r110 > r46 ? 1 : (r110 == r46 ? 0 : -1))
            if (r0 == 0) goto L_0x0763
            r0 = 0
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r0 = "  Total WiFi Multicast wakelock Count: "
            r8.append(r0)
            r8.append(r1)
            java.lang.String r0 = r8.toString()
            r15.println(r0)
            r0 = 0
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r0 = "  Total WiFi Multicast wakelock time: "
            r8.append(r0)
            long r112 = r110 + r18
            r114 = r1
            long r0 = r112 / r16
            formatTimeMsNoSpace(r8, r0)
            java.lang.String r0 = r8.toString()
            r15.println(r0)
            goto L_0x0765
        L_0x0763:
            r114 = r1
        L_0x0765:
            java.lang.String r0 = ""
            r15.println(r0)
            r228.print(r229)
            r0 = 0
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r0 = "  CONNECTIVITY POWER SUMMARY START"
            r8.append(r0)
            java.lang.String r0 = r8.toString()
            r15.println(r0)
            r228.print(r229)
            r0 = 0
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r0 = "  Logging duration for connectivity statistics: "
            r8.append(r0)
            long r0 = r2 / r16
            formatTimeMs(r8, r0)
            java.lang.String r0 = r8.toString()
            r15.println(r0)
            r0 = 0
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r0 = "  Cellular Statistics:"
            r8.append(r0)
            java.lang.String r0 = r8.toString()
            r15.println(r0)
            r228.print(r229)
            r1 = 0
            r8.setLength(r1)
            r8.append(r14)
            java.lang.String r0 = "     Cellular kernel active time: "
            r8.append(r0)
            r112 = r11
            long r11 = r7.getMobileRadioActiveTime(r5, r13)
            r115 = r2
            long r1 = r11 / r16
            formatTimeMs(r8, r1)
            r8.append(r4)
            r1 = r115
            java.lang.String r0 = r7.formatRatioLocked(r11, r1)
            r8.append(r0)
            r3 = r94
            r8.append(r3)
            java.lang.String r0 = r8.toString()
            r15.println(r0)
            android.os.BatteryStats$ControllerActivityCounter r94 = r226.getModemControllerActivity()
            java.lang.String r115 = "Cellular"
            r117 = r71
            r118 = r106
            r120 = r108
            r218 = r11
            r11 = r104
            r104 = r218
            r220 = r74
            r74 = r84
            r84 = r80
            r80 = r220
            r0 = r226
            r122 = r22
            r40 = r114
            r71 = 0
            r22 = r9
            r9 = r1
            r1 = r228
            r2 = r8
            r127 = r3
            r125 = r20
            r20 = r26
            r26 = r34
            r34 = r41
            r41 = r80
            r3 = r229
            r80 = r9
            r9 = r4
            r4 = r115
            r106 = r5
            r88 = r9
            r6 = r39
            r39 = r45
            r9 = r71
            r5 = r94
            r10 = r6
            r6 = r230
            r0.printControllerActivity(r1, r2, r3, r4, r5, r6)
            java.lang.String r0 = "     Cellular data received: "
            r15.print(r0)
            r5 = r95
            java.lang.String r0 = r7.formatBytesLocked(r5)
            r15.println(r0)
            java.lang.String r0 = "     Cellular data sent: "
            r15.print(r0)
            java.lang.String r0 = r7.formatBytesLocked(r11)
            r15.println(r0)
            java.lang.String r0 = "     Cellular packets received: "
            r15.print(r0)
            r3 = r101
            r15.println(r3)
            java.lang.String r0 = "     Cellular packets sent: "
            r15.print(r0)
            r1 = r72
            r15.println(r1)
            r8.setLength(r9)
            r8.append(r14)
            java.lang.String r0 = "     Cellular Radio Access Technology:"
            r8.append(r0)
            r0 = 0
            r45 = 0
            r218 = r45
            r45 = r0
            r0 = r218
        L_0x0872:
            r9 = 22
            if (r0 >= r9) goto L_0x08e6
            r72 = r1
            r52 = r10
            r9 = r106
            long r1 = r7.getPhoneDataConnectionTime(r0, r9, r13)
            int r94 = (r1 > r46 ? 1 : (r1 == r46 ? 0 : -1))
            if (r94 != 0) goto L_0x0893
            r101 = r3
            r95 = r5
            r106 = r11
            r11 = r80
            r6 = r88
            r4 = r91
            r5 = r93
            goto L_0x08ce
        L_0x0893:
            r101 = r3
            java.lang.String r3 = "\n       "
            r8.append(r3)
            r8.append(r14)
            r3 = 1
            java.lang.String[] r4 = android.os.BatteryStats.DATA_CONNECTION_NAMES
            r45 = r3
            int r3 = r4.length
            if (r0 >= r3) goto L_0x08a8
            r3 = r4[r0]
            goto L_0x08aa
        L_0x08a8:
            java.lang.String r3 = "ERROR"
        L_0x08aa:
            r8.append(r3)
            r4 = r91
            r8.append(r4)
            r95 = r5
            long r5 = r1 / r16
            formatTimeMs(r8, r5)
            r6 = r88
            r8.append(r6)
            r106 = r11
            r11 = r80
            java.lang.String r3 = r7.formatRatioLocked(r1, r11)
            r8.append(r3)
            r5 = r93
            r8.append(r5)
        L_0x08ce:
            int r0 = r0 + 1
            r91 = r4
            r93 = r5
            r88 = r6
            r80 = r11
            r1 = r72
            r5 = r95
            r3 = r101
            r11 = r106
            r106 = r9
            r10 = r52
            r9 = 0
            goto L_0x0872
        L_0x08e6:
            r72 = r1
            r101 = r3
            r95 = r5
            r52 = r10
            r6 = r88
            r4 = r91
            r5 = r93
            r9 = r106
            r106 = r11
            r11 = r80
            if (r45 != 0) goto L_0x0901
            java.lang.String r0 = " (no activity)"
            r8.append(r0)
        L_0x0901:
            java.lang.String r0 = r8.toString()
            r15.println(r0)
            r0 = 0
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r0 = "     Cellular Rx signal strength (RSRP):"
            r8.append(r0)
            java.lang.String r0 = "very poor (less than -128dBm): "
            java.lang.String r1 = "poor (-128dBm to -118dBm): "
            java.lang.String r2 = "moderate (-118dBm to -108dBm): "
            java.lang.String r3 = "good (-108dBm to -98dBm): "
            java.lang.String r15 = "great (greater than -98dBm): "
            java.lang.String[] r0 = new java.lang.String[]{r0, r1, r2, r3, r15}
            r15 = r0
            r0 = 0
            r1 = 6
            int r2 = r15.length
            int r3 = java.lang.Math.min(r1, r2)
            r1 = 0
            r45 = r0
        L_0x0932:
            if (r1 >= r3) goto L_0x096f
            r80 = r3
            long r2 = r7.getPhoneSignalStrengthTime(r1, r9, r13)
            int r0 = (r2 > r46 ? 1 : (r2 == r46 ? 0 : -1))
            if (r0 != 0) goto L_0x0941
            r93 = r9
            goto L_0x0968
        L_0x0941:
            java.lang.String r0 = "\n       "
            r8.append(r0)
            r8.append(r14)
            r0 = 1
            r45 = r0
            r0 = r15[r1]
            r8.append(r0)
            r8.append(r4)
            r93 = r9
            long r9 = r2 / r16
            formatTimeMs(r8, r9)
            r8.append(r6)
            java.lang.String r0 = r7.formatRatioLocked(r2, r11)
            r8.append(r0)
            r8.append(r5)
        L_0x0968:
            int r1 = r1 + 1
            r3 = r80
            r9 = r93
            goto L_0x0932
        L_0x096f:
            r80 = r3
            r93 = r9
            if (r45 != 0) goto L_0x097a
            java.lang.String r0 = " (no activity)"
            r8.append(r0)
        L_0x097a:
            java.lang.String r0 = r8.toString()
            r10 = r228
            r10.println(r0)
            r228.print(r229)
            r0 = 0
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r0 = "  Wifi Statistics:"
            r8.append(r0)
            java.lang.String r0 = r8.toString()
            r10.println(r0)
            r228.print(r229)
            r0 = 0
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r0 = "     Wifi kernel active time: "
            r8.append(r0)
            r2 = r93
            long r0 = r7.getWifiActiveTime(r2, r13)
            long r2 = r0 / r16
            formatTimeMs(r8, r2)
            r8.append(r6)
            java.lang.String r2 = r7.formatRatioLocked(r0, r11)
            r8.append(r2)
            r9 = r127
            r8.append(r9)
            java.lang.String r2 = r8.toString()
            r10.println(r2)
            android.os.BatteryStats$ControllerActivityCounter r81 = r226.getWifiControllerActivity()
            java.lang.String r88 = "WiFi"
            r108 = r0
            r0 = r226
            r1 = r228
            r115 = r11
            r11 = r93
            r2 = r8
            r93 = r101
            r3 = r229
            r91 = r15
            r15 = r4
            r4 = r88
            r9 = r5
            r5 = r81
            r81 = r9
            r9 = r6
            r6 = r230
            r0.printControllerActivity(r1, r2, r3, r4, r5, r6)
            java.lang.String r0 = "     Wifi data received: "
            r10.print(r0)
            r5 = r97
            java.lang.String r0 = r7.formatBytesLocked(r5)
            r10.println(r0)
            java.lang.String r0 = "     Wifi data sent: "
            r10.print(r0)
            r3 = r99
            java.lang.String r0 = r7.formatBytesLocked(r3)
            r10.println(r0)
            java.lang.String r0 = "     Wifi packets received: "
            r10.print(r0)
            r1 = r22
            r10.println(r1)
            java.lang.String r0 = "     Wifi packets sent: "
            r10.print(r0)
            r5 = r112
            r10.println(r5)
            r0 = 0
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r0 = "     Wifi states:"
            r8.append(r0)
            r0 = 0
            r22 = 0
            r218 = r22
            r22 = r0
            r0 = r218
        L_0x0a35:
            r99 = r1
            r1 = 8
            if (r0 >= r1) goto L_0x0a85
            long r1 = r7.getWifiStateTime(r0, r11, r13)
            int r23 = (r1 > r46 ? 1 : (r1 == r46 ? 0 : -1))
            if (r23 != 0) goto L_0x0a4c
            r101 = r3
            r112 = r5
            r6 = r81
            r3 = r115
            goto L_0x0a78
        L_0x0a4c:
            r101 = r3
            java.lang.String r3 = "\n       "
            r8.append(r3)
            r3 = 1
            java.lang.String[] r4 = android.os.BatteryStats.WIFI_STATE_NAMES
            r4 = r4[r0]
            r8.append(r4)
            r8.append(r15)
            r22 = r3
            long r3 = r1 / r16
            formatTimeMs(r8, r3)
            r8.append(r9)
            r112 = r5
            r3 = r115
            java.lang.String r5 = r7.formatRatioLocked(r1, r3)
            r8.append(r5)
            r6 = r81
            r8.append(r6)
        L_0x0a78:
            int r0 = r0 + 1
            r115 = r3
            r81 = r6
            r1 = r99
            r3 = r101
            r5 = r112
            goto L_0x0a35
        L_0x0a85:
            r101 = r3
            r112 = r5
            r6 = r81
            r3 = r115
            if (r22 != 0) goto L_0x0a94
            java.lang.String r0 = " (no activity)"
            r8.append(r0)
        L_0x0a94:
            java.lang.String r0 = r8.toString()
            r10.println(r0)
            r0 = 0
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r0 = "     Wifi supplicant states:"
            r8.append(r0)
            r0 = 0
            r1 = 0
        L_0x0aa9:
            r2 = 13
            if (r1 >= r2) goto L_0x0ae8
            r81 = r6
            long r5 = r7.getWifiSupplStateTime(r1, r11, r13)
            int r2 = (r5 > r46 ? 1 : (r5 == r46 ? 0 : -1))
            if (r2 != 0) goto L_0x0abc
            r22 = r11
            r11 = r81
            goto L_0x0ae2
        L_0x0abc:
            java.lang.String r2 = "\n       "
            r8.append(r2)
            r0 = 1
            java.lang.String[] r2 = android.os.BatteryStats.WIFI_SUPPL_STATE_NAMES
            r2 = r2[r1]
            r8.append(r2)
            r8.append(r15)
            r22 = r11
            long r11 = r5 / r16
            formatTimeMs(r8, r11)
            r8.append(r9)
            java.lang.String r2 = r7.formatRatioLocked(r5, r3)
            r8.append(r2)
            r11 = r81
            r8.append(r11)
        L_0x0ae2:
            int r1 = r1 + 1
            r6 = r11
            r11 = r22
            goto L_0x0aa9
        L_0x0ae8:
            r22 = r11
            r11 = r6
            if (r0 != 0) goto L_0x0af2
            java.lang.String r1 = " (no activity)"
            r8.append(r1)
        L_0x0af2:
            java.lang.String r1 = r8.toString()
            r10.println(r1)
            r1 = 0
            r8.setLength(r1)
            r8.append(r14)
            java.lang.String r1 = "     Wifi Rx signal strength (RSSI):"
            r8.append(r1)
            java.lang.String r1 = "very poor (less than -88.75dBm): "
            java.lang.String r2 = "poor (-88.75 to -77.5dBm): "
            java.lang.String r5 = "moderate (-77.5dBm to -66.25dBm): "
            java.lang.String r6 = "good (-66.25dBm to -55dBm): "
            java.lang.String r12 = "great (greater than -55dBm): "
            java.lang.String[] r1 = new java.lang.String[]{r1, r2, r5, r6, r12}
            r12 = r1
            r0 = 0
            r1 = 5
            int r2 = r12.length
            int r6 = java.lang.Math.min(r1, r2)
            r1 = 0
            r45 = r0
        L_0x0b23:
            if (r1 >= r6) goto L_0x0b6d
            r81 = r6
            r5 = r22
            r22 = r11
            long r10 = r7.getWifiSignalStrengthTime(r1, r5, r13)
            int r0 = (r10 > r46 ? 1 : (r10 == r46 ? 0 : -1))
            if (r0 != 0) goto L_0x0b38
            r114 = r5
            r6 = r22
            goto L_0x0b63
        L_0x0b38:
            java.lang.String r0 = "\n    "
            r8.append(r0)
            r8.append(r14)
            r0 = 1
            java.lang.String r2 = "     "
            r8.append(r2)
            r2 = r12[r1]
            r8.append(r2)
            r114 = r5
            long r5 = r10 / r16
            formatTimeMs(r8, r5)
            r8.append(r9)
            java.lang.String r2 = r7.formatRatioLocked(r10, r3)
            r8.append(r2)
            r6 = r22
            r8.append(r6)
            r45 = r0
        L_0x0b63:
            int r1 = r1 + 1
            r10 = r228
            r11 = r6
            r6 = r81
            r22 = r114
            goto L_0x0b23
        L_0x0b6d:
            r81 = r6
            r6 = r11
            r114 = r22
            if (r45 != 0) goto L_0x0b79
            java.lang.String r0 = " (no activity)"
            r8.append(r0)
        L_0x0b79:
            java.lang.String r0 = r8.toString()
            r10 = r228
            r10.println(r0)
            r228.print(r229)
            r0 = 0
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r1 = "  GPS Statistics:"
            r8.append(r1)
            java.lang.String r1 = r8.toString()
            r10.println(r1)
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r0 = "     GPS signal quality (Top 4 Average CN0):"
            r8.append(r0)
            java.lang.String r0 = "poor (less than 20 dBHz): "
            java.lang.String r1 = "good (greater than 20 dBHz): "
            java.lang.String[] r0 = new java.lang.String[]{r0, r1}
            r11 = r0
            int r0 = r11.length
            r5 = 2
            int r2 = java.lang.Math.min(r5, r0)
            r0 = 0
        L_0x0bb5:
            if (r0 >= r2) goto L_0x0bf6
            r23 = r2
            r22 = r6
            r5 = r114
            long r1 = r7.getGpsSignalQualityTime(r0, r5, r13)
            r88 = r12
            java.lang.String r12 = "\n    "
            r8.append(r12)
            r8.append(r14)
            java.lang.String r12 = "  "
            r8.append(r12)
            r12 = r11[r0]
            r8.append(r12)
            r103 = r11
            long r11 = r1 / r16
            formatTimeMs(r8, r11)
            r8.append(r9)
            java.lang.String r11 = r7.formatRatioLocked(r1, r3)
            r8.append(r11)
            r11 = r22
            r8.append(r11)
            int r0 = r0 + 1
            r6 = r11
            r2 = r23
            r12 = r88
            r11 = r103
            r5 = 2
            goto L_0x0bb5
        L_0x0bf6:
            r23 = r2
            r103 = r11
            r88 = r12
            r11 = r6
            r5 = r114
            java.lang.String r0 = r8.toString()
            r10.println(r0)
            long r1 = r226.getGpsBatteryDrainMaMs()
            int r0 = (r1 > r46 ? 1 : (r1 == r46 ? 0 : -1))
            if (r0 <= 0) goto L_0x0c43
            r228.print(r229)
            r0 = 0
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r0 = "     GPS Battery Drain: "
            r8.append(r0)
            java.text.DecimalFormat r0 = new java.text.DecimalFormat
            java.lang.String r12 = "#.##"
            r0.<init>(r12)
            r115 = r3
            double r3 = (double) r1
            r129 = 4704985352480227328(0x414b774000000000, double:3600000.0)
            double r3 = r3 / r129
            java.lang.String r0 = r0.format(r3)
            r8.append(r0)
            java.lang.String r0 = "mAh"
            r8.append(r0)
            java.lang.String r0 = r8.toString()
            r10.println(r0)
            goto L_0x0c45
        L_0x0c43:
            r115 = r3
        L_0x0c45:
            r228.print(r229)
            r0 = 0
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r0 = "  CONNECTIVITY POWER SUMMARY END"
            r8.append(r0)
            java.lang.String r0 = r8.toString()
            r10.println(r0)
            java.lang.String r0 = ""
            r10.println(r0)
            r228.print(r229)
            java.lang.String r0 = "  Bluetooth total received: "
            r10.print(r0)
            r3 = r118
            java.lang.String r0 = r7.formatBytesLocked(r3)
            r10.print(r0)
            java.lang.String r0 = ", sent: "
            r10.print(r0)
            r22 = r11
            r11 = r120
            java.lang.String r0 = r7.formatBytesLocked(r11)
            r10.println(r0)
            long r118 = r7.getBluetoothScanTime(r5, r13)
            long r11 = r118 / r16
            r0 = 0
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r0 = "  Bluetooth scan time: "
            r8.append(r0)
            formatTimeMs(r8, r11)
            java.lang.String r0 = r8.toString()
            r10.println(r0)
            android.os.BatteryStats$ControllerActivityCounter r114 = r226.getBluetoothControllerActivity()
            java.lang.String r118 = "Bluetooth"
            r0 = r226
            r129 = r1
            r1 = r228
            r2 = r8
            r131 = r11
            r11 = r115
            r115 = r3
            r3 = r229
            r4 = r118
            r133 = r5
            r6 = 2
            r5 = r114
            r218 = r81
            r81 = r15
            r15 = r22
            r22 = r218
            r6 = r230
            r0.printControllerActivity(r1, r2, r3, r4, r5, r6)
            r228.println()
            r228.print(r229)
            java.lang.String r0 = "  Device battery use since last full charge"
            r10.println(r0)
            r228.print(r229)
            java.lang.String r0 = "    Amount discharged (lower bound): "
            r10.print(r0)
            int r0 = r226.getLowDischargeAmountSinceCharge()
            r10.println(r0)
            r228.print(r229)
            java.lang.String r0 = "    Amount discharged (upper bound): "
            r10.print(r0)
            int r0 = r226.getHighDischargeAmountSinceCharge()
            r10.println(r0)
            r228.print(r229)
            java.lang.String r0 = "    Amount discharged while screen on: "
            r10.print(r0)
            int r0 = r226.getDischargeAmountScreenOnSinceCharge()
            r10.println(r0)
            r228.print(r229)
            java.lang.String r0 = "    Amount discharged while screen off: "
            r10.print(r0)
            int r0 = r226.getDischargeAmountScreenOffSinceCharge()
            r10.println(r0)
            r228.print(r229)
            java.lang.String r0 = "    Amount discharged while screen doze: "
            r10.print(r0)
            int r0 = r226.getDischargeAmountScreenDozeSinceCharge()
            r10.println(r0)
            r228.println()
            com.android.internal.os.BatteryStatsHelper r0 = new com.android.internal.os.BatteryStatsHelper
            r6 = r227
            r5 = r232
            r1 = 0
            r0.<init>(r6, r1, r5)
            r4 = r0
            r4.create(r7)
            r2 = -1
            r4.refreshStats(r13, r2)
            java.util.List r0 = r4.getUsageList()
            if (r0 == 0) goto L_0x0f32
            int r1 = r0.size()
            if (r1 <= 0) goto L_0x0f32
            r228.print(r229)
            java.lang.String r1 = "  Estimated power use (mAh):"
            r10.println(r1)
            r228.print(r229)
            java.lang.String r1 = "    Capacity: "
            r10.print(r1)
            com.android.internal.os.PowerProfile r1 = r4.getPowerProfile()
            double r2 = r1.getBatteryCapacity()
            r7.printmAh(r10, r2)
            java.lang.String r1 = ", Computed drain: "
            r10.print(r1)
            double r1 = r4.getComputedPower()
            r7.printmAh(r10, r1)
            java.lang.String r1 = ", actual drain: "
            r10.print(r1)
            double r1 = r4.getMinDrainedPower()
            r7.printmAh(r10, r1)
            double r1 = r4.getMinDrainedPower()
            double r118 = r4.getMaxDrainedPower()
            int r1 = (r1 > r118 ? 1 : (r1 == r118 ? 0 : -1))
            if (r1 == 0) goto L_0x0d88
            java.lang.String r1 = "-"
            r10.print(r1)
            double r1 = r4.getMaxDrainedPower()
            r7.printmAh(r10, r1)
        L_0x0d88:
            r228.println()
            r1 = 0
        L_0x0d8c:
            int r2 = r0.size()
            if (r1 >= r2) goto L_0x0f2a
            java.lang.Object r2 = r0.get(r1)
            com.android.internal.os.BatterySipper r2 = (com.android.internal.os.BatterySipper) r2
            r228.print(r229)
            int[] r3 = android.os.BatteryStats.AnonymousClass2.$SwitchMap$com$android$internal$os$BatterySipper$DrainType
            r118 = r0
            com.android.internal.os.BatterySipper$DrainType r0 = r2.drainType
            int r0 = r0.ordinal()
            r0 = r3[r0]
            switch(r0) {
                case 1: goto L_0x0e11;
                case 2: goto L_0x0e0b;
                case 3: goto L_0x0e05;
                case 4: goto L_0x0dff;
                case 5: goto L_0x0df9;
                case 6: goto L_0x0df3;
                case 7: goto L_0x0ded;
                case 8: goto L_0x0de7;
                case 9: goto L_0x0dd3;
                case 10: goto L_0x0dc3;
                case 11: goto L_0x0dbd;
                case 12: goto L_0x0db7;
                case 13: goto L_0x0db1;
                default: goto L_0x0daa;
            }
        L_0x0daa:
            java.lang.String r0 = "    ???: "
            r10.print(r0)
            goto L_0x0e17
        L_0x0db1:
            java.lang.String r0 = "    Camera: "
            r10.print(r0)
            goto L_0x0e17
        L_0x0db7:
            java.lang.String r0 = "    Over-counted: "
            r10.print(r0)
            goto L_0x0e17
        L_0x0dbd:
            java.lang.String r0 = "    Unaccounted: "
            r10.print(r0)
            goto L_0x0e17
        L_0x0dc3:
            java.lang.String r0 = "    User "
            r10.print(r0)
            int r0 = r2.userId
            r10.print(r0)
            java.lang.String r0 = ": "
            r10.print(r0)
            goto L_0x0e17
        L_0x0dd3:
            java.lang.String r0 = "    Uid "
            r10.print(r0)
            android.os.BatteryStats$Uid r0 = r2.uidObj
            int r0 = r0.getUid()
            android.os.UserHandle.formatUid(r10, r0)
            java.lang.String r0 = ": "
            r10.print(r0)
            goto L_0x0e17
        L_0x0de7:
            java.lang.String r0 = "    Flashlight: "
            r10.print(r0)
            goto L_0x0e17
        L_0x0ded:
            java.lang.String r0 = "    Screen: "
            r10.print(r0)
            goto L_0x0e17
        L_0x0df3:
            java.lang.String r0 = "    Bluetooth: "
            r10.print(r0)
            goto L_0x0e17
        L_0x0df9:
            java.lang.String r0 = "    Wifi: "
            r10.print(r0)
            goto L_0x0e17
        L_0x0dff:
            java.lang.String r0 = "    Phone calls: "
            r10.print(r0)
            goto L_0x0e17
        L_0x0e05:
            java.lang.String r0 = "    Cell standby: "
            r10.print(r0)
            goto L_0x0e17
        L_0x0e0b:
            java.lang.String r0 = "    Idle: "
            r10.print(r0)
            goto L_0x0e17
        L_0x0e11:
            java.lang.String r0 = "    Ambient display: "
            r10.print(r0)
        L_0x0e17:
            double r5 = r2.totalPowerMah
            r7.printmAh(r10, r5)
            double r5 = r2.usagePowerMah
            r136 = r11
            double r11 = r2.totalPowerMah
            int r0 = (r5 > r11 ? 1 : (r5 == r11 ? 0 : -1))
            r5 = 0
            if (r0 == 0) goto L_0x0ed2
            java.lang.String r0 = " ("
            r10.print(r0)
            double r11 = r2.usagePowerMah
            int r0 = (r11 > r5 ? 1 : (r11 == r5 ? 0 : -1))
            if (r0 == 0) goto L_0x0e3d
            java.lang.String r0 = " usage="
            r10.print(r0)
            double r11 = r2.usagePowerMah
            r7.printmAh(r10, r11)
        L_0x0e3d:
            double r11 = r2.cpuPowerMah
            int r0 = (r11 > r5 ? 1 : (r11 == r5 ? 0 : -1))
            if (r0 == 0) goto L_0x0e4d
            java.lang.String r0 = " cpu="
            r10.print(r0)
            double r11 = r2.cpuPowerMah
            r7.printmAh(r10, r11)
        L_0x0e4d:
            double r11 = r2.wakeLockPowerMah
            int r0 = (r11 > r5 ? 1 : (r11 == r5 ? 0 : -1))
            if (r0 == 0) goto L_0x0e5d
            java.lang.String r0 = " wake="
            r10.print(r0)
            double r11 = r2.wakeLockPowerMah
            r7.printmAh(r10, r11)
        L_0x0e5d:
            double r11 = r2.mobileRadioPowerMah
            int r0 = (r11 > r5 ? 1 : (r11 == r5 ? 0 : -1))
            if (r0 == 0) goto L_0x0e6d
            java.lang.String r0 = " radio="
            r10.print(r0)
            double r11 = r2.mobileRadioPowerMah
            r7.printmAh(r10, r11)
        L_0x0e6d:
            double r11 = r2.wifiPowerMah
            int r0 = (r11 > r5 ? 1 : (r11 == r5 ? 0 : -1))
            if (r0 == 0) goto L_0x0e7d
            java.lang.String r0 = " wifi="
            r10.print(r0)
            double r11 = r2.wifiPowerMah
            r7.printmAh(r10, r11)
        L_0x0e7d:
            double r11 = r2.bluetoothPowerMah
            int r0 = (r11 > r5 ? 1 : (r11 == r5 ? 0 : -1))
            if (r0 == 0) goto L_0x0e8d
            java.lang.String r0 = " bt="
            r10.print(r0)
            double r11 = r2.bluetoothPowerMah
            r7.printmAh(r10, r11)
        L_0x0e8d:
            double r11 = r2.gpsPowerMah
            int r0 = (r11 > r5 ? 1 : (r11 == r5 ? 0 : -1))
            if (r0 == 0) goto L_0x0e9d
            java.lang.String r0 = " gps="
            r10.print(r0)
            double r11 = r2.gpsPowerMah
            r7.printmAh(r10, r11)
        L_0x0e9d:
            double r11 = r2.sensorPowerMah
            int r0 = (r11 > r5 ? 1 : (r11 == r5 ? 0 : -1))
            if (r0 == 0) goto L_0x0ead
            java.lang.String r0 = " sensor="
            r10.print(r0)
            double r11 = r2.sensorPowerMah
            r7.printmAh(r10, r11)
        L_0x0ead:
            double r11 = r2.cameraPowerMah
            int r0 = (r11 > r5 ? 1 : (r11 == r5 ? 0 : -1))
            if (r0 == 0) goto L_0x0ebd
            java.lang.String r0 = " camera="
            r10.print(r0)
            double r11 = r2.cameraPowerMah
            r7.printmAh(r10, r11)
        L_0x0ebd:
            double r11 = r2.flashlightPowerMah
            int r0 = (r11 > r5 ? 1 : (r11 == r5 ? 0 : -1))
            if (r0 == 0) goto L_0x0ecd
            java.lang.String r0 = " flash="
            r10.print(r0)
            double r11 = r2.flashlightPowerMah
            r7.printmAh(r10, r11)
        L_0x0ecd:
            java.lang.String r0 = " )"
            r10.print(r0)
        L_0x0ed2:
            double r11 = r2.totalSmearedPowerMah
            double r5 = r2.totalPowerMah
            int r0 = (r11 > r5 ? 1 : (r11 == r5 ? 0 : -1))
            if (r0 == 0) goto L_0x0f12
            java.lang.String r0 = " Including smearing: "
            r10.print(r0)
            double r5 = r2.totalSmearedPowerMah
            r7.printmAh(r10, r5)
            java.lang.String r0 = " ("
            r10.print(r0)
            double r5 = r2.screenPowerMah
            r11 = 0
            int r0 = (r5 > r11 ? 1 : (r5 == r11 ? 0 : -1))
            if (r0 == 0) goto L_0x0efb
            java.lang.String r0 = " screen="
            r10.print(r0)
            double r5 = r2.screenPowerMah
            r7.printmAh(r10, r5)
        L_0x0efb:
            double r5 = r2.proportionalSmearMah
            r11 = 0
            int r0 = (r5 > r11 ? 1 : (r5 == r11 ? 0 : -1))
            if (r0 == 0) goto L_0x0f0d
            java.lang.String r0 = " proportional="
            r10.print(r0)
            double r5 = r2.proportionalSmearMah
            r7.printmAh(r10, r5)
        L_0x0f0d:
            java.lang.String r0 = " )"
            r10.print(r0)
        L_0x0f12:
            boolean r0 = r2.shouldHide
            if (r0 == 0) goto L_0x0f1b
            java.lang.String r0 = " Excluded from smearing"
            r10.print(r0)
        L_0x0f1b:
            r228.println()
            int r1 = r1 + 1
            r6 = r227
            r5 = r232
            r0 = r118
            r11 = r136
            goto L_0x0d8c
        L_0x0f2a:
            r118 = r0
            r136 = r11
            r228.println()
            goto L_0x0f36
        L_0x0f32:
            r118 = r0
            r136 = r11
        L_0x0f36:
            java.util.List r11 = r4.getMobilemsppList()
            if (r11 == 0) goto L_0x0fdf
            int r0 = r11.size()
            if (r0 <= 0) goto L_0x0fdf
            r228.print(r229)
            java.lang.String r0 = "  Per-app mobile ms per packet:"
            r10.println(r0)
            r0 = 0
            r2 = 0
        L_0x0f4d:
            int r3 = r11.size()
            if (r2 >= r3) goto L_0x0fb2
            java.lang.Object r3 = r11.get(r2)
            com.android.internal.os.BatterySipper r3 = (com.android.internal.os.BatterySipper) r3
            r5 = 0
            r8.setLength(r5)
            r8.append(r14)
            java.lang.String r5 = "    Uid "
            r8.append(r5)
            android.os.BatteryStats$Uid r5 = r3.uidObj
            int r5 = r5.getUid()
            android.os.UserHandle.formatUid(r8, r5)
            java.lang.String r5 = ": "
            r8.append(r5)
            double r5 = r3.mobilemspp
            java.lang.String r5 = com.android.internal.os.BatteryStatsHelper.makemAh(r5)
            r8.append(r5)
            java.lang.String r5 = " ("
            r8.append(r5)
            long r5 = r3.mobileRxPackets
            r118 = r11
            long r11 = r3.mobileTxPackets
            long r5 = r5 + r11
            r8.append(r5)
            java.lang.String r5 = " packets over "
            r8.append(r5)
            long r5 = r3.mobileActive
            formatTimeMsNoSpace(r8, r5)
            r8.append(r15)
            int r5 = r3.mobileActiveCount
            r8.append(r5)
            java.lang.String r5 = "x"
            r8.append(r5)
            java.lang.String r5 = r8.toString()
            r10.println(r5)
            long r5 = r3.mobileActive
            long r0 = r0 + r5
            int r2 = r2 + 1
            r11 = r118
            goto L_0x0f4d
        L_0x0fb2:
            r118 = r11
            r2 = 0
            r8.setLength(r2)
            r8.append(r14)
            java.lang.String r2 = "    TOTAL TIME: "
            r8.append(r2)
            formatTimeMs(r8, r0)
            r8.append(r9)
            r11 = r136
            java.lang.String r2 = r7.formatRatioLocked(r0, r11)
            r8.append(r2)
            r6 = r127
            r8.append(r6)
            java.lang.String r2 = r8.toString()
            r10.println(r2)
            r228.println()
            goto L_0x0fe5
        L_0x0fdf:
            r118 = r11
            r6 = r127
            r11 = r136
        L_0x0fe5:
            android.os.BatteryStats$1 r0 = new android.os.BatteryStats$1
            r0.<init>()
            r5 = r0
            if (r-25 >= 0) goto L_0x121f
            java.util.Map r119 = r226.getKernelWakelockStats()
            if (r119 == 0) goto L_0x10e2
            int r0 = r119.size()
            if (r0 <= 0) goto L_0x10e2
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r2 = r0
            java.util.Set r0 = r119.entrySet()
            java.util.Iterator r0 = r0.iterator()
        L_0x1008:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x104e
            java.lang.Object r1 = r0.next()
            java.util.Map$Entry r1 = (java.util.Map.Entry) r1
            java.lang.Object r3 = r1.getValue()
            android.os.BatteryStats$Timer r3 = (android.os.BatteryStats.Timer) r3
            r124 = r4
            r127 = r5
            r4 = r133
            long r133 = computeWakeLock(r3, r4, r13)
            int r136 = (r133 > r46 ? 1 : (r133 == r46 ? 0 : -1))
            if (r-120 <= 0) goto L_0x1043
            r142 = r0
            android.os.BatteryStats$TimerEntry r0 = new android.os.BatteryStats$TimerEntry
            java.lang.Object r136 = r1.getKey()
            r137 = r136
            java.lang.String r137 = (java.lang.String) r137
            r138 = 0
            r136 = r0
            r139 = r3
            r140 = r133
            r136.<init>(r137, r138, r139, r140)
            r2.add(r0)
            goto L_0x1045
        L_0x1043:
            r142 = r0
        L_0x1045:
            r133 = r4
            r4 = r124
            r5 = r127
            r0 = r142
            goto L_0x1008
        L_0x104e:
            r124 = r4
            r127 = r5
            r4 = r133
            int r0 = r2.size()
            if (r0 <= 0) goto L_0x10d7
            r3 = r127
            java.util.Collections.sort(r2, r3)
            r228.print(r229)
            java.lang.String r0 = "  All kernel wake locks:"
            r10.println(r0)
            r0 = 0
            r1 = r0
        L_0x1069:
            int r0 = r2.size()
            if (r1 >= r0) goto L_0x10c7
            java.lang.Object r0 = r2.get(r1)
            android.os.BatteryStats$TimerEntry r0 = (android.os.BatteryStats.TimerEntry) r0
            java.lang.String r127 = ": "
            r133 = r1
            r1 = 0
            r8.setLength(r1)
            r8.append(r14)
            java.lang.String r1 = "  Kernel Wake lock "
            r8.append(r1)
            java.lang.String r1 = r0.mName
            r8.append(r1)
            android.os.BatteryStats$Timer r1 = r0.mTimer
            r134 = 0
            r136 = r0
            r0 = r8
            r137 = r2
            r114 = r3
            r138 = -1
            r2 = r4
            r139 = r4
            r4 = r134
            r143 = r114
            r5 = r230
            r114 = r6
            r6 = r127
            java.lang.String r0 = printWakeLock(r0, r1, r2, r4, r5, r6)
            java.lang.String r1 = ": "
            boolean r1 = r0.equals(r1)
            if (r1 != 0) goto L_0x10bc
            java.lang.String r1 = " realtime"
            r8.append(r1)
            java.lang.String r1 = r8.toString()
            r10.println(r1)
        L_0x10bc:
            int r1 = r133 + 1
            r6 = r114
            r2 = r137
            r4 = r139
            r3 = r143
            goto L_0x1069
        L_0x10c7:
            r133 = r1
            r137 = r2
            r143 = r3
            r139 = r4
            r114 = r6
            r138 = -1
            r228.println()
            goto L_0x10ec
        L_0x10d7:
            r137 = r2
            r139 = r4
            r114 = r6
            r143 = r127
            r138 = -1
            goto L_0x10ec
        L_0x10e2:
            r124 = r4
            r143 = r5
            r114 = r6
            r139 = r133
            r138 = -1
        L_0x10ec:
            int r0 = r38.size()
            if (r0 <= 0) goto L_0x1168
            r6 = r38
            r5 = r143
            java.util.Collections.sort(r6, r5)
            r228.print(r229)
            java.lang.String r0 = "  All partial wake locks:"
            r10.println(r0)
            r0 = 0
            r4 = r0
        L_0x1103:
            int r0 = r6.size()
            if (r4 >= r0) goto L_0x1159
            java.lang.Object r0 = r6.get(r4)
            r2 = r0
            android.os.BatteryStats$TimerEntry r2 = (android.os.BatteryStats.TimerEntry) r2
            r0 = 0
            r8.setLength(r0)
            java.lang.String r0 = "  Wake lock "
            r8.append(r0)
            int r0 = r2.mId
            android.os.UserHandle.formatUid(r8, r0)
            r3 = r81
            r8.append(r3)
            java.lang.String r0 = r2.mName
            r8.append(r0)
            android.os.BatteryStats$Timer r1 = r2.mTimer
            r38 = 0
            java.lang.String r81 = ": "
            r0 = r8
            r127 = r2
            r133 = r3
            r2 = r139
            r134 = r4
            r4 = r38
            r144 = r5
            r5 = r230
            r38 = r6
            r6 = r81
            printWakeLock(r0, r1, r2, r4, r5, r6)
            java.lang.String r0 = " realtime"
            r8.append(r0)
            java.lang.String r0 = r8.toString()
            r10.println(r0)
            int r4 = r134 + 1
            r6 = r38
            r81 = r133
            r5 = r144
            goto L_0x1103
        L_0x1159:
            r134 = r4
            r144 = r5
            r38 = r6
            r133 = r81
            r38.clear()
            r228.println()
            goto L_0x116c
        L_0x1168:
            r133 = r81
            r144 = r143
        L_0x116c:
            java.util.Map r81 = r226.getWakeupReasonStats()
            if (r81 == 0) goto L_0x121c
            int r0 = r81.size()
            if (r0 <= 0) goto L_0x121c
            r228.print(r229)
            java.lang.String r0 = "  All wakeup reasons:"
            r10.println(r0)
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r6 = r0
            java.util.Set r0 = r81.entrySet()
            java.util.Iterator r0 = r0.iterator()
        L_0x118e:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x11be
            java.lang.Object r1 = r0.next()
            java.util.Map$Entry r1 = (java.util.Map.Entry) r1
            java.lang.Object r2 = r1.getValue()
            android.os.BatteryStats$Timer r2 = (android.os.BatteryStats.Timer) r2
            android.os.BatteryStats$TimerEntry r3 = new android.os.BatteryStats$TimerEntry
            java.lang.Object r4 = r1.getKey()
            r146 = r4
            java.lang.String r146 = (java.lang.String) r146
            r147 = 0
            int r4 = r2.getCountLocked(r13)
            long r4 = (long) r4
            r145 = r3
            r148 = r2
            r149 = r4
            r145.<init>(r146, r147, r148, r149)
            r6.add(r3)
            goto L_0x118e
        L_0x11be:
            r5 = r144
            java.util.Collections.sort(r6, r5)
            r0 = 0
            r4 = r0
        L_0x11c5:
            int r0 = r6.size()
            if (r4 >= r0) goto L_0x1212
            java.lang.Object r0 = r6.get(r4)
            r2 = r0
            android.os.BatteryStats$TimerEntry r2 = (android.os.BatteryStats.TimerEntry) r2
            java.lang.String r127 = ": "
            r0 = 0
            r8.setLength(r0)
            r8.append(r14)
            java.lang.String r0 = "  Wakeup reason "
            r8.append(r0)
            java.lang.String r0 = r2.mName
            r8.append(r0)
            android.os.BatteryStats$Timer r1 = r2.mTimer
            r134 = 0
            java.lang.String r136 = ": "
            r0 = r8
            r137 = r2
            r2 = r139
            r141 = r4
            r4 = r134
            r134 = r5
            r5 = r230
            r142 = r6
            r6 = r136
            printWakeLock(r0, r1, r2, r4, r5, r6)
            java.lang.String r0 = " realtime"
            r8.append(r0)
            java.lang.String r0 = r8.toString()
            r10.println(r0)
            int r4 = r141 + 1
            r5 = r134
            r6 = r142
            goto L_0x11c5
        L_0x1212:
            r141 = r4
            r134 = r5
            r142 = r6
            r228.println()
            goto L_0x122b
        L_0x121c:
            r134 = r144
            goto L_0x122b
        L_0x121f:
            r124 = r4
            r114 = r6
            r139 = r133
            r138 = -1
            r134 = r5
            r133 = r81
        L_0x122b:
            android.util.LongSparseArray r6 = r226.getKernelMemoryStats()
            int r0 = r6.size()
            if (r0 <= 0) goto L_0x1275
            java.lang.String r0 = "  Memory Stats"
            r10.println(r0)
            r0 = 0
        L_0x123b:
            int r1 = r6.size()
            if (r0 >= r1) goto L_0x126f
            r1 = 0
            r8.setLength(r1)
            java.lang.String r2 = "  Bandwidth "
            r8.append(r2)
            long r2 = r6.keyAt(r0)
            r8.append(r2)
            java.lang.String r2 = " Time "
            r8.append(r2)
            java.lang.Object r2 = r6.valueAt(r0)
            android.os.BatteryStats$Timer r2 = (android.os.BatteryStats.Timer) r2
            r3 = r139
            long r1 = r2.getTotalTimeLocked(r3, r13)
            r8.append(r1)
            java.lang.String r1 = r8.toString()
            r10.println(r1)
            int r0 = r0 + 1
            goto L_0x123b
        L_0x126f:
            r3 = r139
            r228.println()
            goto L_0x1277
        L_0x1275:
            r3 = r139
        L_0x1277:
            java.util.Map r71 = r226.getRpmStats()
            int r0 = r71.size()
            if (r0 <= 0) goto L_0x13a4
            r228.print(r229)
            java.lang.String r0 = "  Resource Power Manager Stats"
            r10.println(r0)
            int r0 = r71.size()
            if (r0 <= 0) goto L_0x136b
            java.util.Set r0 = r71.entrySet()
            java.util.Iterator r0 = r0.iterator()
        L_0x1297:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x1335
            java.lang.Object r1 = r0.next()
            java.util.Map$Entry r1 = (java.util.Map.Entry) r1
            java.lang.Object r2 = r1.getKey()
            java.lang.String r2 = (java.lang.String) r2
            java.lang.Object r81 = r1.getValue()
            android.os.BatteryStats$Timer r81 = (android.os.BatteryStats.Timer) r81
            r119 = r8
            r8 = r228
            r4 = r3
            r127 = r114
            r3 = 0
            r114 = r6
            r6 = r9
            r218 = r74
            r74 = r101
            r101 = r99
            r99 = r72
            r72 = r218
            r220 = r53
            r53 = r61
            r61 = r220
            r9 = r119
            r151 = r15
            r15 = r52
            r10 = r81
            r152 = r11
            r52 = r92
            r154 = r133
            r92 = r1
            r218 = r88
            r88 = r0
            r0 = r104
            r219 = r106
            r107 = r218
            r105 = r112
            r112 = r120
            r120 = r131
            r131 = r118
            r118 = r103
            r103 = r219
            r11 = r4
            r13 = r230
            r14 = r229
            r155 = r15
            r15 = r2
            printTimer(r8, r9, r10, r11, r13, r14, r15)
            r10 = r228
            r3 = r4
            r9 = r6
            r92 = r52
            r6 = r114
            r8 = r119
            r114 = r127
            r15 = r151
            r11 = r152
            r52 = r155
            r218 = r0
            r0 = r88
            r88 = r107
            r220 = r103
            r103 = r118
            r118 = r131
            r131 = r120
            r120 = r112
            r112 = r105
            r104 = r218
            r106 = r220
            r222 = r99
            r99 = r101
            r101 = r74
            r74 = r72
            r72 = r222
            r224 = r53
            r53 = r61
            r61 = r224
            goto L_0x1297
        L_0x1335:
            r4 = r3
            r119 = r8
            r152 = r11
            r151 = r15
            r155 = r52
            r52 = r92
            r0 = r104
            r127 = r114
            r154 = r133
            r3 = 0
            r114 = r6
            r6 = r9
            r218 = r106
            r107 = r88
            r105 = r112
            r112 = r120
            r120 = r131
            r131 = r118
            r118 = r103
            r103 = r218
            r220 = r74
            r74 = r101
            r101 = r99
            r99 = r72
            r72 = r220
            r222 = r53
            r53 = r61
            r61 = r222
            goto L_0x13a0
        L_0x136b:
            r4 = r3
            r119 = r8
            r152 = r11
            r151 = r15
            r155 = r52
            r52 = r92
            r0 = r104
            r127 = r114
            r154 = r133
            r3 = 0
            r114 = r6
            r6 = r9
            r218 = r106
            r107 = r88
            r105 = r112
            r112 = r120
            r120 = r131
            r131 = r118
            r118 = r103
            r103 = r218
            r220 = r74
            r74 = r101
            r101 = r99
            r99 = r72
            r72 = r220
            r222 = r53
            r53 = r61
            r61 = r222
        L_0x13a0:
            r228.println()
            goto L_0x13d9
        L_0x13a4:
            r4 = r3
            r119 = r8
            r152 = r11
            r151 = r15
            r155 = r52
            r52 = r92
            r0 = r104
            r127 = r114
            r154 = r133
            r3 = 0
            r114 = r6
            r6 = r9
            r218 = r106
            r107 = r88
            r105 = r112
            r112 = r120
            r120 = r131
            r131 = r118
            r118 = r103
            r103 = r218
            r220 = r74
            r74 = r101
            r101 = r99
            r99 = r72
            r72 = r220
            r222 = r53
            r53 = r61
            r61 = r222
        L_0x13d9:
            long[] r15 = r226.getCpuFreqs()
            if (r15 == 0) goto L_0x1415
            r14 = r119
            r14.setLength(r3)
            java.lang.String r2 = "  CPU freqs:"
            r14.append(r2)
            r2 = 0
        L_0x13ea:
            int r8 = r15.length
            if (r2 >= r8) goto L_0x1406
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r13 = r154
            r8.append(r13)
            r9 = r15[r2]
            r8.append(r9)
            java.lang.String r8 = r8.toString()
            r14.append(r8)
            int r2 = r2 + 1
            goto L_0x13ea
        L_0x1406:
            r13 = r154
            java.lang.String r2 = r14.toString()
            r11 = r228
            r11.println(r2)
            r228.println()
            goto L_0x141b
        L_0x1415:
            r11 = r228
            r14 = r119
            r13 = r154
        L_0x141b:
            r2 = 0
            r12 = r2
        L_0x141d:
            r10 = r155
            if (r12 >= r10) goto L_0x25c2
            r9 = r117
            int r8 = r9.keyAt(r12)
            r2 = r231
            if (r2 < 0) goto L_0x1453
            if (r8 == r2) goto L_0x1453
            r3 = 1000(0x3e8, float:1.401E-42)
            if (r8 == r3) goto L_0x1453
            r2 = r229
            r136 = r0
            r199 = r4
            r183 = r6
            r117 = r9
            r135 = r10
            r5 = r11
            r119 = r12
            r210 = r13
            r1 = r14
            r145 = r15
            r13 = r122
            r207 = r125
            r182 = r127
            r189 = r151
            r92 = 1
            r11 = r230
            goto L_0x25a5
        L_0x1453:
            java.lang.Object r3 = r9.valueAt(r12)
            android.os.BatteryStats$Uid r3 = (android.os.BatteryStats.Uid) r3
            r228.print(r229)
            java.lang.String r2 = "  "
            r11.print(r2)
            android.os.UserHandle.formatUid(r11, r8)
            java.lang.String r2 = ":"
            r11.println(r2)
            r88 = 0
            r2 = r230
            r92 = r8
            r117 = r9
            r155 = r10
            r8 = 0
            long r9 = r3.getNetworkActivityBytes(r8, r2)
            r119 = r12
            r154 = r13
            r8 = 1
            long r12 = r3.getNetworkActivityBytes(r8, r2)
            r132 = r0
            r8 = 2
            long r0 = r3.getNetworkActivityBytes(r8, r2)
            r8 = 3
            r136 = r0
            long r0 = r3.getNetworkActivityBytes(r8, r2)
            r8 = 4
            r139 = r0
            long r0 = r3.getNetworkActivityBytes(r8, r2)
            r8 = 5
            r141 = r0
            long r0 = r3.getNetworkActivityBytes(r8, r2)
            r143 = r0
            r8 = 0
            long r0 = r3.getNetworkActivityPackets(r8, r2)
            r146 = r14
            r145 = r15
            r8 = 1
            long r14 = r3.getNetworkActivityPackets(r8, r2)
            r147 = r6
            r8 = 2
            long r6 = r3.getNetworkActivityPackets(r8, r2)
            r8 = 3
            r148 = r6
            long r7 = r3.getNetworkActivityPackets(r8, r2)
            r156 = r7
            long r7 = r3.getMobileRadioActiveTime(r2)
            int r6 = r3.getMobileRadioActiveCount(r2)
            r158 = r7
            r8 = r6
            long r6 = r3.getFullWifiLockTime(r4, r2)
            r160 = r6
            long r6 = r3.getWifiScanTime(r4, r2)
            r162 = r6
            int r7 = r3.getWifiScanCount(r2)
            int r6 = r3.getWifiScanBackgroundCount(r2)
            r164 = r6
            r150 = r7
            long r6 = r3.getWifiScanActualTime(r4)
            r165 = r6
            long r6 = r3.getWifiScanBackgroundTime(r4)
            r167 = r6
            long r6 = r3.getWifiRunningTime(r4, r2)
            r169 = r6
            long r6 = r3.getMobileRadioApWakeupCount(r2)
            r171 = r4
            long r4 = r3.getWifiRadioApWakeupCount(r2)
            int r173 = (r9 > r46 ? 1 : (r9 == r46 ? 0 : -1))
            if (r-83 > 0) goto L_0x1514
            int r173 = (r12 > r46 ? 1 : (r12 == r46 ? 0 : -1))
            if (r-83 > 0) goto L_0x1514
            int r173 = (r0 > r46 ? 1 : (r0 == r46 ? 0 : -1))
            if (r-83 > 0) goto L_0x1514
            int r173 = (r14 > r46 ? 1 : (r14 == r46 ? 0 : -1))
            if (r-83 <= 0) goto L_0x150d
            goto L_0x1514
        L_0x150d:
            r2 = r226
            r174 = r148
            r148 = r4
            goto L_0x154a
        L_0x1514:
            r228.print(r229)
            java.lang.String r2 = "    Mobile network: "
            r11.print(r2)
            r2 = r226
            r174 = r148
            r148 = r4
            java.lang.String r4 = r2.formatBytesLocked(r9)
            r11.print(r4)
            java.lang.String r4 = " received, "
            r11.print(r4)
            java.lang.String r4 = r2.formatBytesLocked(r12)
            r11.print(r4)
            java.lang.String r4 = " sent (packets "
            r11.print(r4)
            r11.print(r0)
            java.lang.String r4 = " received, "
            r11.print(r4)
            r11.print(r14)
            java.lang.String r4 = " sent)"
            r11.println(r4)
        L_0x154a:
            int r4 = (r158 > r46 ? 1 : (r158 == r46 ? 0 : -1))
            if (r4 > 0) goto L_0x1565
            if (r8 <= 0) goto L_0x1551
            goto L_0x1565
        L_0x1551:
            r4 = r229
            r178 = r0
            r176 = r9
            r180 = r132
            r5 = r146
            r146 = r3
            r132 = r12
            r12 = r158
            r158 = r8
            goto L_0x15d2
        L_0x1565:
            r5 = r146
            r4 = 0
            r5.setLength(r4)
            r4 = r229
            r5.append(r4)
            r176 = r9
            java.lang.String r9 = "    Mobile radio active: "
            r5.append(r9)
            long r9 = r158 / r16
            formatTimeMs(r5, r9)
            r9 = r147
            r5.append(r9)
            r146 = r3
            r9 = r132
            r132 = r12
            r12 = r158
            java.lang.String r3 = r2.formatRatioLocked(r12, r9)
            r5.append(r3)
            r3 = r151
            r5.append(r3)
            r5.append(r8)
            java.lang.String r2 = "x"
            r5.append(r2)
            long r158 = r0 + r14
            int r2 = (r158 > r46 ? 1 : (r158 == r46 ? 0 : -1))
            if (r2 != 0) goto L_0x15ab
            r158 = 1
            r178 = r0
            r0 = r158
            goto L_0x15af
        L_0x15ab:
            r178 = r0
            r0 = r158
        L_0x15af:
            java.lang.String r2 = " @ "
            r5.append(r2)
            r151 = r3
            long r2 = r12 / r16
            double r2 = (double) r2
            r158 = r8
            r180 = r9
            double r8 = (double) r0
            double r2 = r2 / r8
            java.lang.String r2 = com.android.internal.os.BatteryStatsHelper.makemAh(r2)
            r5.append(r2)
            java.lang.String r2 = " mspp"
            r5.append(r2)
            java.lang.String r2 = r5.toString()
            r11.println(r2)
        L_0x15d2:
            int r0 = (r6 > r46 ? 1 : (r6 == r46 ? 0 : -1))
            if (r0 <= 0) goto L_0x15ed
            r3 = 0
            r5.setLength(r3)
            r5.append(r4)
            java.lang.String r0 = "    Mobile radio AP wakeups: "
            r5.append(r0)
            r5.append(r6)
            java.lang.String r0 = r5.toString()
            r11.println(r0)
            goto L_0x15ee
        L_0x15ed:
            r3 = 0
        L_0x15ee:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r0.append(r4)
            java.lang.String r1 = "  "
            r0.append(r1)
            java.lang.String r8 = r0.toString()
            android.os.BatteryStats$ControllerActivityCounter r9 = r146.getModemControllerActivity()
            java.lang.String r10 = "Cellular"
            r1 = r136
            r182 = r143
            r143 = r178
            r136 = r180
            r218 = r12
            r12 = r141
            r141 = r218
            r220 = r14
            r14 = r139
            r139 = r220
            r0 = r226
            r178 = r12
            r12 = r1
            r1 = r228
            r180 = r6
            r7 = r226
            r6 = r230
            r2 = r5
            r184 = r127
            r81 = r146
            r185 = r151
            r3 = r8
            r8 = r4
            r189 = r148
            r187 = r171
            r4 = r10
            r10 = r5
            r5 = r9
            r146 = r10
            r199 = r147
            r127 = r158
            r8 = r160
            r191 = r162
            r10 = r164
            r193 = r165
            r195 = r167
            r197 = r169
            r147 = r180
            r0.printControllerActivityIfInteresting(r1, r2, r3, r4, r5, r6)
            int r0 = (r12 > r46 ? 1 : (r12 == r46 ? 0 : -1))
            if (r0 > 0) goto L_0x1663
            int r0 = (r14 > r46 ? 1 : (r14 == r46 ? 0 : -1))
            if (r0 > 0) goto L_0x1663
            r5 = r174
            int r0 = (r5 > r46 ? 1 : (r5 == r46 ? 0 : -1))
            if (r0 > 0) goto L_0x1665
            int r0 = (r156 > r46 ? 1 : (r156 == r46 ? 0 : -1))
            if (r0 <= 0) goto L_0x1660
            goto L_0x1665
        L_0x1660:
            r3 = r156
            goto L_0x1697
        L_0x1663:
            r5 = r174
        L_0x1665:
            r228.print(r229)
            java.lang.String r0 = "    Wi-Fi network: "
            r11.print(r0)
            java.lang.String r0 = r7.formatBytesLocked(r12)
            r11.print(r0)
            java.lang.String r0 = " received, "
            r11.print(r0)
            java.lang.String r0 = r7.formatBytesLocked(r14)
            r11.print(r0)
            java.lang.String r0 = " sent (packets "
            r11.print(r0)
            r11.print(r5)
            java.lang.String r0 = " received, "
            r11.print(r0)
            r3 = r156
            r11.print(r3)
            java.lang.String r0 = " sent)"
            r11.println(r0)
        L_0x1697:
            int r0 = (r8 > r46 ? 1 : (r8 == r46 ? 0 : -1))
            if (r0 != 0) goto L_0x16f3
            r1 = r191
            int r0 = (r1 > r46 ? 1 : (r1 == r46 ? 0 : -1))
            if (r0 != 0) goto L_0x16e6
            if (r-106 != 0) goto L_0x16e6
            if (r10 != 0) goto L_0x16e6
            r156 = r14
            r14 = r193
            int r0 = (r14 > r46 ? 1 : (r14 == r46 ? 0 : -1))
            if (r0 != 0) goto L_0x16dd
            r158 = r12
            r12 = r195
            int r0 = (r12 > r46 ? 1 : (r12 == r46 ? 0 : -1))
            if (r0 != 0) goto L_0x16d8
            r160 = r8
            r8 = r197
            int r0 = (r8 > r46 ? 1 : (r8 == r46 ? 0 : -1))
            if (r0 == 0) goto L_0x16be
            goto L_0x1701
        L_0x16be:
            r191 = r1
            r174 = r5
            r197 = r8
            r9 = r10
            r8 = r11
            r0 = r146
            r10 = r150
            r200 = r160
            r11 = r185
            r1 = r187
            r6 = r199
            r5 = r229
            r160 = r3
            goto L_0x17d7
        L_0x16d8:
            r160 = r8
            r8 = r197
            goto L_0x1701
        L_0x16dd:
            r160 = r8
            r158 = r12
            r12 = r195
            r8 = r197
            goto L_0x1701
        L_0x16e6:
            r160 = r8
            r158 = r12
            r156 = r14
            r14 = r193
            r12 = r195
            r8 = r197
            goto L_0x1701
        L_0x16f3:
            r160 = r8
            r158 = r12
            r156 = r14
            r1 = r191
            r14 = r193
            r12 = r195
            r8 = r197
        L_0x1701:
            r174 = r5
            r0 = r146
            r6 = 0
            r0.setLength(r6)
            r5 = r229
            r200 = r160
            r0.append(r5)
            java.lang.String r6 = "    Wifi Running: "
            r0.append(r6)
            r160 = r3
            long r3 = r8 / r16
            formatTimeMs(r0, r3)
            r6 = r199
            r0.append(r6)
            r3 = r152
            java.lang.String r11 = r7.formatRatioLocked(r8, r3)
            r0.append(r11)
            java.lang.String r11 = ")\n"
            r0.append(r11)
            r0.append(r5)
            java.lang.String r11 = "    Full Wifi Lock: "
            r0.append(r11)
            r197 = r8
            r164 = r10
            r8 = r200
            long r10 = r8 / r16
            formatTimeMs(r0, r10)
            r0.append(r6)
            java.lang.String r10 = r7.formatRatioLocked(r8, r3)
            r0.append(r10)
            java.lang.String r10 = ")\n"
            r0.append(r10)
            r0.append(r5)
            java.lang.String r10 = "    Wifi Scan (blamed): "
            r0.append(r10)
            long r10 = r1 / r16
            formatTimeMs(r0, r10)
            r0.append(r6)
            java.lang.String r10 = r7.formatRatioLocked(r1, r3)
            r0.append(r10)
            r11 = r185
            r0.append(r11)
            r10 = r150
            r0.append(r10)
            r191 = r1
            java.lang.String r1 = "x\n"
            r0.append(r1)
            r0.append(r5)
            java.lang.String r1 = "    Wifi Scan (actual): "
            r0.append(r1)
            long r1 = r14 / r16
            formatTimeMs(r0, r1)
            r0.append(r6)
            r1 = r187
            r3 = 0
            long r8 = r7.computeBatteryRealtime(r1, r3)
            java.lang.String r3 = r7.formatRatioLocked(r14, r8)
            r0.append(r3)
            r0.append(r11)
            r0.append(r10)
            java.lang.String r3 = "x\n"
            r0.append(r3)
            r0.append(r5)
            java.lang.String r3 = "    Background Wifi Scan: "
            r0.append(r3)
            long r3 = r12 / r16
            formatTimeMs(r0, r3)
            r0.append(r6)
            r3 = 0
            long r8 = r7.computeBatteryRealtime(r1, r3)
            java.lang.String r3 = r7.formatRatioLocked(r12, r8)
            r0.append(r3)
            r0.append(r11)
            r9 = r164
            r0.append(r9)
            java.lang.String r3 = "x"
            r0.append(r3)
            java.lang.String r3 = r0.toString()
            r8 = r228
            r8.println(r3)
        L_0x17d7:
            r3 = r189
            int r146 = (r3 > r46 ? 1 : (r3 == r46 ? 0 : -1))
            if (r-110 <= 0) goto L_0x17f6
            r199 = r6
            r6 = 0
            r0.setLength(r6)
            r0.append(r5)
            java.lang.String r6 = "    WiFi AP wakeups: "
            r0.append(r6)
            r0.append(r3)
            java.lang.String r6 = r0.toString()
            r8.println(r6)
            goto L_0x17f8
        L_0x17f6:
            r199 = r6
        L_0x17f8:
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            r6.append(r5)
            r146 = r0
            java.lang.String r0 = "  "
            r6.append(r0)
            java.lang.String r6 = r6.toString()
            android.os.BatteryStats$ControllerActivityCounter r149 = r81.getWifiControllerActivity()
            java.lang.String r150 = "WiFi"
            r151 = r146
            r0 = r226
            r193 = r14
            r162 = r191
            r14 = r1
            r1 = r228
            r2 = r151
            r164 = r3
            r3 = r6
            r4 = r150
            r6 = r5
            r166 = r174
            r5 = r149
            r146 = r9
            r195 = r12
            r13 = r199
            r12 = 0
            r9 = r6
            r6 = r230
            r0.printControllerActivityIfInteresting(r1, r2, r3, r4, r5, r6)
            int r0 = (r178 > r46 ? 1 : (r178 == r46 ? 0 : -1))
            if (r0 > 0) goto L_0x1843
            r5 = r182
            int r0 = (r5 > r46 ? 1 : (r5 == r46 ? 0 : -1))
            if (r0 <= 0) goto L_0x1840
            goto L_0x1845
        L_0x1840:
            r2 = r178
            goto L_0x1867
        L_0x1843:
            r5 = r182
        L_0x1845:
            r228.print(r229)
            java.lang.String r0 = "    Bluetooth network: "
            r8.print(r0)
            r2 = r178
            java.lang.String r0 = r7.formatBytesLocked(r2)
            r8.print(r0)
            java.lang.String r0 = " received, "
            r8.print(r0)
            java.lang.String r0 = r7.formatBytesLocked(r5)
            r8.print(r0)
            java.lang.String r0 = " sent"
            r8.println(r0)
        L_0x1867:
            android.os.BatteryStats$Timer r4 = r81.getBluetoothScanTimer()
            java.lang.String r1 = " times)"
            if (r4 == 0) goto L_0x1a60
            r0 = r230
            long r149 = r4.getTotalTimeLocked(r14, r0)
            long r149 = r149 + r18
            r199 = r13
            long r12 = r149 / r16
            int r149 = (r12 > r46 ? 1 : (r12 == r46 ? 0 : -1))
            if (r-107 == 0) goto L_0x1a4e
            r178 = r2
            int r2 = r4.getCountLocked(r0)
            android.os.BatteryStats$Timer r3 = r81.getBluetoothScanBackgroundTimer()
            if (r3 == 0) goto L_0x1890
            int r149 = r3.getCountLocked(r0)
            goto L_0x1892
        L_0x1890:
            r149 = 0
        L_0x1892:
            r150 = r149
            r182 = r5
            r149 = r10
            r185 = r11
            r10 = r125
            long r5 = r4.getTotalDurationMsLocked(r10)
            if (r3 == 0) goto L_0x18a7
            long r125 = r3.getTotalDurationMsLocked(r10)
            goto L_0x18a9
        L_0x18a7:
            r125 = r46
        L_0x18a9:
            r168 = r125
            android.os.BatteryStats$Counter r125 = r81.getBluetoothScanResultCounter()
            if (r125 == 0) goto L_0x18ba
            android.os.BatteryStats$Counter r7 = r81.getBluetoothScanResultCounter()
            int r7 = r7.getCountLocked(r0)
            goto L_0x18bb
        L_0x18ba:
            r7 = 0
        L_0x18bb:
            android.os.BatteryStats$Counter r125 = r81.getBluetoothScanResultBgCounter()
            if (r125 == 0) goto L_0x18cc
            r187 = r14
            android.os.BatteryStats$Counter r14 = r81.getBluetoothScanResultBgCounter()
            int r14 = r14.getCountLocked(r0)
            goto L_0x18cf
        L_0x18cc:
            r187 = r14
            r14 = 0
        L_0x18cf:
            android.os.BatteryStats$Timer r15 = r81.getBluetoothUnoptimizedScanTimer()
            if (r15 == 0) goto L_0x18da
            long r125 = r15.getTotalDurationMsLocked(r10)
            goto L_0x18dc
        L_0x18da:
            r125 = r46
        L_0x18dc:
            r170 = r125
            if (r15 == 0) goto L_0x18e5
            long r125 = r15.getMaxDurationMsLocked(r10)
            goto L_0x18e7
        L_0x18e5:
            r125 = r46
        L_0x18e7:
            r172 = r125
            android.os.BatteryStats$Timer r0 = r81.getBluetoothUnoptimizedScanBackgroundTimer()
            if (r0 == 0) goto L_0x18f5
            long r125 = r0.getTotalDurationMsLocked(r10)
            goto L_0x18f7
        L_0x18f5:
            r125 = r46
        L_0x18f7:
            r174 = r125
            if (r0 == 0) goto L_0x1900
            long r125 = r0.getMaxDurationMsLocked(r10)
            goto L_0x1902
        L_0x1900:
            r125 = r46
        L_0x1902:
            r180 = r125
            r125 = r10
            r10 = r151
            r11 = 0
            r10.setLength(r11)
            int r11 = (r5 > r12 ? 1 : (r5 == r12 ? 0 : -1))
            if (r11 == 0) goto L_0x1936
            r10.append(r9)
            java.lang.String r11 = "    Bluetooth Scan (total blamed realtime): "
            r10.append(r11)
            formatTimeMs(r10, r12)
            java.lang.String r11 = " ("
            r10.append(r11)
            r10.append(r2)
            r10.append(r1)
            boolean r11 = r4.isRunningLocked()
            if (r11 == 0) goto L_0x1931
            java.lang.String r11 = " (currently running)"
            r10.append(r11)
        L_0x1931:
            java.lang.String r11 = "\n"
            r10.append(r11)
        L_0x1936:
            r10.append(r9)
            java.lang.String r11 = "    Bluetooth Scan (total actual realtime): "
            r10.append(r11)
            formatTimeMs(r10, r5)
            java.lang.String r11 = " ("
            r10.append(r11)
            r10.append(r2)
            r10.append(r1)
            boolean r11 = r4.isRunningLocked()
            if (r11 == 0) goto L_0x1957
            java.lang.String r11 = " (currently running)"
            r10.append(r11)
        L_0x1957:
            java.lang.String r11 = "\n"
            r10.append(r11)
            r11 = r4
            r189 = r5
            r4 = r168
            int r6 = (r4 > r46 ? 1 : (r4 == r46 ? 0 : -1))
            if (r6 > 0) goto L_0x196d
            r6 = r150
            if (r6 <= 0) goto L_0x196a
            goto L_0x196f
        L_0x196a:
            r150 = r2
            goto L_0x1999
        L_0x196d:
            r6 = r150
        L_0x196f:
            r10.append(r9)
            r150 = r2
            java.lang.String r2 = "    Bluetooth Scan (background realtime): "
            r10.append(r2)
            formatTimeMs(r10, r4)
            java.lang.String r2 = " ("
            r10.append(r2)
            r10.append(r6)
            r10.append(r1)
            if (r3 == 0) goto L_0x1994
            boolean r2 = r3.isRunningLocked()
            if (r2 == 0) goto L_0x1994
            java.lang.String r2 = " (currently running in background)"
            r10.append(r2)
        L_0x1994:
            java.lang.String r2 = "\n"
            r10.append(r2)
        L_0x1999:
            r10.append(r9)
            java.lang.String r2 = "    Bluetooth Scan Results: "
            r10.append(r2)
            r10.append(r7)
            java.lang.String r2 = " ("
            r10.append(r2)
            r10.append(r14)
            java.lang.String r2 = " in background)"
            r10.append(r2)
            r151 = r1
            r1 = r170
            int r168 = (r1 > r46 ? 1 : (r1 == r46 ? 0 : -1))
            if (r-88 > 0) goto L_0x19d0
            r168 = r3
            r169 = r4
            r3 = r174
            int r5 = (r3 > r46 ? 1 : (r3 == r46 ? 0 : -1))
            if (r5 <= 0) goto L_0x19c4
            goto L_0x19d6
        L_0x19c4:
            r174 = r1
            r171 = r172
            r1 = r180
            r5 = r184
            r173 = r0
            goto L_0x1a44
        L_0x19d0:
            r168 = r3
            r169 = r4
            r3 = r174
        L_0x19d6:
            java.lang.String r5 = "\n"
            r10.append(r5)
            r10.append(r9)
            java.lang.String r5 = "    Unoptimized Bluetooth Scan (realtime): "
            r10.append(r5)
            formatTimeMs(r10, r1)
            java.lang.String r5 = " (max "
            r10.append(r5)
            r174 = r1
            r1 = r172
            formatTimeMs(r10, r1)
            r5 = r184
            r10.append(r5)
            if (r15 == 0) goto L_0x1a0a
            boolean r171 = r15.isRunningLocked()
            if (r-85 == 0) goto L_0x1a07
            r171 = r1
            java.lang.String r1 = " (currently running unoptimized)"
            r10.append(r1)
            goto L_0x1a0c
        L_0x1a07:
            r171 = r1
            goto L_0x1a0c
        L_0x1a0a:
            r171 = r1
        L_0x1a0c:
            if (r0 == 0) goto L_0x1a40
            int r1 = (r3 > r46 ? 1 : (r3 == r46 ? 0 : -1))
            if (r1 <= 0) goto L_0x1a40
            java.lang.String r1 = "\n"
            r10.append(r1)
            r10.append(r9)
            java.lang.String r1 = "    Unoptimized Bluetooth Scan (background realtime): "
            r10.append(r1)
            formatTimeMs(r10, r3)
            java.lang.String r1 = " (max "
            r10.append(r1)
            r1 = r180
            formatTimeMs(r10, r1)
            r10.append(r5)
            boolean r173 = r0.isRunningLocked()
            if (r-83 == 0) goto L_0x1a3d
            r173 = r0
            java.lang.String r0 = " (currently running unoptimized in background)"
            r10.append(r0)
            goto L_0x1a44
        L_0x1a3d:
            r173 = r0
            goto L_0x1a44
        L_0x1a40:
            r173 = r0
            r1 = r180
        L_0x1a44:
            java.lang.String r0 = r10.toString()
            r8.println(r0)
            r88 = 1
            goto L_0x1a73
        L_0x1a4e:
            r178 = r2
            r182 = r5
            r149 = r10
            r185 = r11
            r187 = r14
            r10 = r151
            r5 = r184
            r151 = r1
            r11 = r4
            goto L_0x1a73
        L_0x1a60:
            r178 = r2
            r182 = r5
            r149 = r10
            r185 = r11
            r199 = r13
            r187 = r14
            r10 = r151
            r5 = r184
            r151 = r1
            r11 = r4
        L_0x1a73:
            boolean r0 = r81.hasUserActivity()
            java.lang.String r7 = ", "
            if (r0 == 0) goto L_0x1ac4
            r0 = 0
            r1 = 0
        L_0x1a7d:
            int r2 = android.os.BatteryStats.Uid.NUM_USER_ACTIVITY_TYPES
            if (r1 >= r2) goto L_0x1ab4
            r2 = r230
            r15 = r81
            int r3 = r15.getUserActivityCount(r1, r2)
            if (r3 == 0) goto L_0x1aab
            if (r0 != 0) goto L_0x1a98
            r4 = 0
            r10.setLength(r4)
            java.lang.String r4 = "    User activity: "
            r10.append(r4)
            r0 = 1
            goto L_0x1a9b
        L_0x1a98:
            r10.append(r7)
        L_0x1a9b:
            r10.append(r3)
            r13 = r154
            r10.append(r13)
            java.lang.String[] r4 = android.os.BatteryStats.Uid.USER_ACTIVITY_TYPES
            r4 = r4[r1]
            r10.append(r4)
            goto L_0x1aad
        L_0x1aab:
            r13 = r154
        L_0x1aad:
            int r1 = r1 + 1
            r154 = r13
            r81 = r15
            goto L_0x1a7d
        L_0x1ab4:
            r2 = r230
            r15 = r81
            r13 = r154
            if (r0 == 0) goto L_0x1aca
            java.lang.String r1 = r10.toString()
            r8.println(r1)
            goto L_0x1aca
        L_0x1ac4:
            r2 = r230
            r15 = r81
            r13 = r154
        L_0x1aca:
            android.util.ArrayMap r14 = r15.getWakelockStats()
            r0 = 0
            r3 = 0
            r168 = 0
            r170 = 0
            r6 = 0
            if (r14 == 0) goto L_0x1bde
            int r12 = r14.size()
            r81 = 1
            int r12 = r12 + -1
            r81 = r6
            r172 = r168
            r174 = r170
            r168 = r0
            r170 = r3
        L_0x1aec:
            if (r12 < 0) goto L_0x1bc2
            java.lang.Object r0 = r14.valueAt(r12)
            r6 = r0
            android.os.BatteryStats$Uid$Wakelock r6 = (android.os.BatteryStats.Uid.Wakelock) r6
            java.lang.String r150 = ": "
            r0 = 0
            r10.setLength(r0)
            r10.append(r9)
            java.lang.String r0 = "    Wake lock "
            r10.append(r0)
            java.lang.Object r0 = r14.keyAt(r12)
            java.lang.String r0 = (java.lang.String) r0
            r10.append(r0)
            r0 = 1
            android.os.BatteryStats$Timer r1 = r6.getWakeTime(r0)
            java.lang.String r4 = "full"
            r0 = r10
            r3 = r151
            r151 = r11
            r154 = r14
            r11 = r2
            r14 = r3
            r2 = r187
            r180 = r182
            r182 = r5
            r5 = r230
            r183 = r13
            r13 = r6
            r6 = r150
            java.lang.String r150 = printWakeLock(r0, r1, r2, r4, r5, r6)
            r0 = 0
            android.os.BatteryStats$Timer r184 = r13.getWakeTime(r0)
            java.lang.String r4 = "partial"
            r0 = r10
            r1 = r184
            r6 = r150
            java.lang.String r150 = printWakeLock(r0, r1, r2, r4, r5, r6)
            if (r-72 == 0) goto L_0x1b45
            android.os.BatteryStats$Timer r0 = r184.getSubTimer()
            goto L_0x1b46
        L_0x1b45:
            r0 = 0
        L_0x1b46:
            r1 = r0
            java.lang.String r4 = "background partial"
            r0 = r10
            r2 = r187
            r5 = r230
            r6 = r150
            java.lang.String r150 = printWakeLock(r0, r1, r2, r4, r5, r6)
            r0 = 2
            android.os.BatteryStats$Timer r1 = r13.getWakeTime(r0)
            java.lang.String r4 = "window"
            r0 = r10
            r6 = r150
            java.lang.String r150 = printWakeLock(r0, r1, r2, r4, r5, r6)
            r0 = 18
            android.os.BatteryStats$Timer r1 = r13.getWakeTime(r0)
            java.lang.String r4 = "draw"
            r0 = r10
            r6 = r150
            java.lang.String r0 = printWakeLock(r0, r1, r2, r4, r5, r6)
            java.lang.String r1 = " realtime"
            r10.append(r1)
            java.lang.String r1 = r10.toString()
            r8.println(r1)
            r88 = 1
            int r81 = r81 + 1
            r1 = 1
            android.os.BatteryStats$Timer r2 = r13.getWakeTime(r1)
            r3 = r187
            long r1 = computeWakeLock(r2, r3, r11)
            long r168 = r168 + r1
            r1 = 0
            android.os.BatteryStats$Timer r2 = r13.getWakeTime(r1)
            long r1 = computeWakeLock(r2, r3, r11)
            long r170 = r170 + r1
            r2 = 2
            android.os.BatteryStats$Timer r1 = r13.getWakeTime(r2)
            long r5 = computeWakeLock(r1, r3, r11)
            long r172 = r172 + r5
            r1 = 18
            android.os.BatteryStats$Timer r1 = r13.getWakeTime(r1)
            long r5 = computeWakeLock(r1, r3, r11)
            long r174 = r174 + r5
            int r12 = r12 + -1
            r2 = r11
            r11 = r151
            r5 = r182
            r13 = r183
            r151 = r14
            r14 = r154
            r182 = r180
            goto L_0x1aec
        L_0x1bc2:
            r154 = r14
            r14 = r151
            r180 = r182
            r3 = r187
            r182 = r5
            r151 = r11
            r183 = r13
            r11 = r2
            r2 = 2
            r12 = r3
            r6 = r81
            r0 = r168
            r3 = r170
            r202 = r172
            r204 = r174
            goto L_0x1bf2
        L_0x1bde:
            r154 = r14
            r14 = r151
            r180 = r182
            r182 = r5
            r151 = r11
            r183 = r13
            r12 = r187
            r11 = r2
            r2 = 2
            r202 = r168
            r204 = r170
        L_0x1bf2:
            r5 = 1
            if (r6 <= r5) goto L_0x1ce3
            r168 = 0
            r170 = 0
            android.os.BatteryStats$Timer r5 = r15.getAggregatedPartialWakelockTimer()
            if (r5 == 0) goto L_0x1c22
            android.os.BatteryStats$Timer r5 = r15.getAggregatedPartialWakelockTimer()
            r172 = r3
            r2 = r125
            long r168 = r5.getTotalDurationMsLocked(r2)
            android.os.BatteryStats$Timer r4 = r5.getSubTimer()
            if (r4 == 0) goto L_0x1c17
            long r125 = r4.getTotalDurationMsLocked(r2)
            goto L_0x1c19
        L_0x1c17:
            r125 = r46
        L_0x1c19:
            r170 = r125
            r125 = r2
            r4 = r168
            r2 = r170
            goto L_0x1c2a
        L_0x1c22:
            r172 = r3
            r2 = r125
            r4 = r168
            r2 = r170
        L_0x1c2a:
            int r81 = (r4 > r46 ? 1 : (r4 == r46 ? 0 : -1))
            if (r81 != 0) goto L_0x1c49
            int r81 = (r2 > r46 ? 1 : (r2 == r46 ? 0 : -1))
            if (r81 != 0) goto L_0x1c49
            int r81 = (r0 > r46 ? 1 : (r0 == r46 ? 0 : -1))
            if (r81 != 0) goto L_0x1c49
            int r81 = (r172 > r46 ? 1 : (r172 == r46 ? 0 : -1))
            if (r81 != 0) goto L_0x1c49
            r187 = r12
            r11 = r202
            int r13 = (r11 > r46 ? 1 : (r11 == r46 ? 0 : -1))
            if (r13 == 0) goto L_0x1c43
            goto L_0x1c4d
        L_0x1c43:
            r168 = r0
            r0 = r204
            goto L_0x1ced
        L_0x1c49:
            r187 = r12
            r11 = r202
        L_0x1c4d:
            r13 = 0
            r10.setLength(r13)
            r10.append(r9)
            java.lang.String r13 = "    TOTAL wake: "
            r10.append(r13)
            r13 = 0
            int r81 = (r0 > r46 ? 1 : (r0 == r46 ? 0 : -1))
            if (r81 == 0) goto L_0x1c6a
            r13 = 1
            formatTimeMs(r10, r0)
            r168 = r0
            java.lang.String r0 = "full"
            r10.append(r0)
            goto L_0x1c6c
        L_0x1c6a:
            r168 = r0
        L_0x1c6c:
            int r0 = (r172 > r46 ? 1 : (r172 == r46 ? 0 : -1))
            if (r0 == 0) goto L_0x1c81
            if (r13 == 0) goto L_0x1c75
            r10.append(r7)
        L_0x1c75:
            r13 = 1
            r0 = r172
            formatTimeMs(r10, r0)
            java.lang.String r0 = "blamed partial"
            r10.append(r0)
        L_0x1c81:
            int r0 = (r4 > r46 ? 1 : (r4 == r46 ? 0 : -1))
            if (r0 == 0) goto L_0x1c93
            if (r13 == 0) goto L_0x1c8a
            r10.append(r7)
        L_0x1c8a:
            r13 = 1
            formatTimeMs(r10, r4)
            java.lang.String r0 = "actual partial"
            r10.append(r0)
        L_0x1c93:
            int r0 = (r2 > r46 ? 1 : (r2 == r46 ? 0 : -1))
            if (r0 == 0) goto L_0x1ca5
            if (r13 == 0) goto L_0x1c9c
            r10.append(r7)
        L_0x1c9c:
            r13 = 1
            formatTimeMs(r10, r2)
            java.lang.String r0 = "actual background partial"
            r10.append(r0)
        L_0x1ca5:
            int r0 = (r11 > r46 ? 1 : (r11 == r46 ? 0 : -1))
            if (r0 == 0) goto L_0x1cb8
            if (r13 == 0) goto L_0x1cae
            r10.append(r7)
        L_0x1cae:
            r13 = 1
            formatTimeMs(r10, r11)
            java.lang.String r0 = "window"
            r10.append(r0)
        L_0x1cb8:
            r0 = r204
            int r81 = (r0 > r46 ? 1 : (r0 == r46 ? 0 : -1))
            if (r81 == 0) goto L_0x1cd4
            if (r13 == 0) goto L_0x1cc8
            r170 = r2
            java.lang.String r2 = ","
            r10.append(r2)
            goto L_0x1cca
        L_0x1cc8:
            r170 = r2
        L_0x1cca:
            r13 = 1
            formatTimeMs(r10, r0)
            java.lang.String r2 = "draw"
            r10.append(r2)
            goto L_0x1cd6
        L_0x1cd4:
            r170 = r2
        L_0x1cd6:
            java.lang.String r2 = " realtime"
            r10.append(r2)
            java.lang.String r2 = r10.toString()
            r8.println(r2)
            goto L_0x1ced
        L_0x1ce3:
            r168 = r0
            r172 = r3
            r187 = r12
            r11 = r202
            r0 = r204
        L_0x1ced:
            android.os.BatteryStats$Timer r2 = r15.getMulticastWakelockStats()
            if (r2 == 0) goto L_0x1d32
            r3 = r230
            r170 = r11
            r4 = r187
            long r11 = r2.getTotalTimeLocked(r4, r3)
            int r13 = r2.getCountLocked(r3)
            int r81 = (r11 > r46 ? 1 : (r11 == r46 ? 0 : -1))
            if (r81 <= 0) goto L_0x1d2f
            r174 = r0
            r0 = 0
            r10.setLength(r0)
            r10.append(r9)
            java.lang.String r0 = "    WiFi Multicast Wakelock"
            r10.append(r0)
            java.lang.String r0 = " count = "
            r10.append(r0)
            r10.append(r13)
            java.lang.String r0 = " time = "
            r10.append(r0)
            long r0 = r11 + r18
            long r0 = r0 / r16
            formatTimeMsNoSpace(r10, r0)
            java.lang.String r0 = r10.toString()
            r8.println(r0)
            goto L_0x1d3a
        L_0x1d2f:
            r174 = r0
            goto L_0x1d3a
        L_0x1d32:
            r3 = r230
            r174 = r0
            r170 = r11
            r4 = r187
        L_0x1d3a:
            android.util.ArrayMap r0 = r15.getSyncStats()
            if (r0 == 0) goto L_0x1e03
            int r1 = r0.size()
            r11 = 1
            int r1 = r1 - r11
        L_0x1d46:
            if (r1 < 0) goto L_0x1df8
            java.lang.Object r11 = r0.valueAt(r1)
            android.os.BatteryStats$Timer r11 = (android.os.BatteryStats.Timer) r11
            long r12 = r11.getTotalTimeLocked(r4, r3)
            long r12 = r12 + r18
            long r12 = r12 / r16
            r81 = r2
            int r2 = r11.getCountLocked(r3)
            r150 = r6
            android.os.BatteryStats$Timer r6 = r11.getSubTimer()
            if (r6 == 0) goto L_0x1d6d
            r187 = r4
            r4 = r125
            long r125 = r6.getTotalDurationMsLocked(r4)
            goto L_0x1d73
        L_0x1d6d:
            r187 = r4
            r4 = r125
            r125 = -1
        L_0x1d73:
            r189 = r125
            if (r6 == 0) goto L_0x1d7c
            int r125 = r6.getCountLocked(r3)
            goto L_0x1d7e
        L_0x1d7c:
            r125 = r138
        L_0x1d7e:
            r126 = r125
            r125 = r6
            r6 = 0
            r10.setLength(r6)
            r10.append(r9)
            java.lang.String r6 = "    Sync "
            r10.append(r6)
            java.lang.Object r6 = r0.keyAt(r1)
            java.lang.String r6 = (java.lang.String) r6
            r10.append(r6)
            java.lang.String r6 = ": "
            r10.append(r6)
            int r6 = (r12 > r46 ? 1 : (r12 == r46 ? 0 : -1))
            if (r6 == 0) goto L_0x1dd3
            formatTimeMs(r10, r12)
            java.lang.String r6 = "realtime ("
            r10.append(r6)
            r10.append(r2)
            r10.append(r14)
            r6 = r11
            r191 = r12
            r11 = r189
            int r13 = (r11 > r46 ? 1 : (r11 == r46 ? 0 : -1))
            if (r13 <= 0) goto L_0x1dce
            r10.append(r7)
            formatTimeMs(r10, r11)
            java.lang.String r13 = "background ("
            r10.append(r13)
            r13 = r126
            r10.append(r13)
            r10.append(r14)
            r126 = r0
            goto L_0x1de1
        L_0x1dce:
            r13 = r126
            r126 = r0
            goto L_0x1de1
        L_0x1dd3:
            r6 = r11
            r191 = r12
            r13 = r126
            r11 = r189
            r126 = r0
            java.lang.String r0 = "(not used)"
            r10.append(r0)
        L_0x1de1:
            java.lang.String r0 = r10.toString()
            r8.println(r0)
            r88 = 1
            int r1 = r1 + -1
            r2 = r81
            r0 = r126
            r6 = r150
            r125 = r4
            r4 = r187
            goto L_0x1d46
        L_0x1df8:
            r81 = r2
            r187 = r4
            r150 = r6
            r4 = r125
            r126 = r0
            goto L_0x1e0d
        L_0x1e03:
            r81 = r2
            r187 = r4
            r150 = r6
            r4 = r125
            r126 = r0
        L_0x1e0d:
            android.util.ArrayMap r0 = r15.getJobStats()
            if (r0 == 0) goto L_0x1ec4
            int r1 = r0.size()
            r2 = 1
            int r1 = r1 - r2
        L_0x1e19:
            if (r1 < 0) goto L_0x1ebd
            java.lang.Object r2 = r0.valueAt(r1)
            android.os.BatteryStats$Timer r2 = (android.os.BatteryStats.Timer) r2
            r11 = r187
            long r187 = r2.getTotalTimeLocked(r11, r3)
            long r187 = r187 + r18
            r189 = r11
            long r11 = r187 / r16
            int r6 = r2.getCountLocked(r3)
            android.os.BatteryStats$Timer r13 = r2.getSubTimer()
            if (r13 == 0) goto L_0x1e3c
            long r187 = r13.getTotalDurationMsLocked(r4)
            goto L_0x1e3e
        L_0x1e3c:
            r187 = -1
        L_0x1e3e:
            r191 = r187
            if (r13 == 0) goto L_0x1e47
            int r125 = r13.getCountLocked(r3)
            goto L_0x1e49
        L_0x1e47:
            r125 = r138
        L_0x1e49:
            r184 = r125
            r125 = r2
            r2 = 0
            r10.setLength(r2)
            r10.append(r9)
            java.lang.String r2 = "    Job "
            r10.append(r2)
            java.lang.Object r2 = r0.keyAt(r1)
            java.lang.String r2 = (java.lang.String) r2
            r10.append(r2)
            java.lang.String r2 = ": "
            r10.append(r2)
            int r2 = (r11 > r46 ? 1 : (r11 == r46 ? 0 : -1))
            if (r2 == 0) goto L_0x1e9d
            formatTimeMs(r10, r11)
            java.lang.String r2 = "realtime ("
            r10.append(r2)
            r10.append(r6)
            r10.append(r14)
            r187 = r4
            r4 = r191
            int r2 = (r4 > r46 ? 1 : (r4 == r46 ? 0 : -1))
            if (r2 <= 0) goto L_0x1e98
            r10.append(r7)
            formatTimeMs(r10, r4)
            java.lang.String r2 = "background ("
            r10.append(r2)
            r2 = r184
            r10.append(r2)
            r10.append(r14)
            r184 = r0
            goto L_0x1eaa
        L_0x1e98:
            r2 = r184
            r184 = r0
            goto L_0x1eaa
        L_0x1e9d:
            r187 = r4
            r2 = r184
            r4 = r191
            r184 = r0
            java.lang.String r0 = "(not used)"
            r10.append(r0)
        L_0x1eaa:
            java.lang.String r0 = r10.toString()
            r8.println(r0)
            r88 = 1
            int r1 = r1 + -1
            r0 = r184
            r4 = r187
            r187 = r189
            goto L_0x1e19
        L_0x1ebd:
            r184 = r0
            r189 = r187
            r187 = r4
            goto L_0x1eca
        L_0x1ec4:
            r184 = r0
            r189 = r187
            r187 = r4
        L_0x1eca:
            android.util.ArrayMap r0 = r15.getJobCompletionStats()
            int r1 = r0.size()
            r2 = 1
            int r1 = r1 - r2
        L_0x1ed4:
            if (r1 < 0) goto L_0x1f33
            java.lang.Object r4 = r0.valueAt(r1)
            android.util.SparseIntArray r4 = (android.util.SparseIntArray) r4
            if (r4 == 0) goto L_0x1f28
            r228.print(r229)
            java.lang.String r5 = "    Job Completions "
            r8.print(r5)
            java.lang.Object r5 = r0.keyAt(r1)
            java.lang.String r5 = (java.lang.String) r5
            r8.print(r5)
            java.lang.String r5 = ":"
            r8.print(r5)
            r5 = 0
        L_0x1ef5:
            int r6 = r4.size()
            if (r5 >= r6) goto L_0x1f20
            r6 = r183
            r8.print(r6)
            int r11 = r4.keyAt(r5)
            java.lang.String r11 = android.app.job.JobParameters.getReasonName(r11)
            r8.print(r11)
            r13 = r199
            r8.print(r13)
            int r11 = r4.valueAt(r5)
            r8.print(r11)
            java.lang.String r11 = "x)"
            r8.print(r11)
            int r5 = r5 + 1
            goto L_0x1ef5
        L_0x1f20:
            r6 = r183
            r13 = r199
            r228.println()
            goto L_0x1f2c
        L_0x1f28:
            r6 = r183
            r13 = r199
        L_0x1f2c:
            int r1 = r1 + -1
            r183 = r6
            r199 = r13
            goto L_0x1ed4
        L_0x1f33:
            r6 = r183
            r13 = r199
            r15.getDeferredJobsLineLocked(r10, r3)
            int r1 = r10.length()
            if (r1 <= 0) goto L_0x1f4c
            java.lang.String r1 = "    Jobs deferred on launch "
            r8.print(r1)
            java.lang.String r1 = r10.toString()
            r8.println(r1)
        L_0x1f4c:
            android.os.BatteryStats$Timer r1 = r15.getFlashlightTurnedOnTimer()
            java.lang.String r4 = "Flashlight"
            r5 = r92
            r191 = r200
            r11 = 2
            r92 = r2
            r2 = r9
            r218 = r141
            r141 = r160
            r160 = r218
            r8 = r228
            r12 = r146
            r9 = r10
            r125 = r5
            r183 = r6
            r128 = r10
            r146 = r149
            r135 = r155
            r5 = r187
            r149 = r12
            r10 = r1
            r1 = r228
            r155 = r0
            r187 = r195
            r0 = 0
            r218 = r189
            r189 = r185
            r185 = r178
            r178 = r158
            r158 = r132
            r132 = r218
            r11 = r132
            r206 = r183
            r183 = r13
            r13 = r230
            r207 = r14
            r195 = r132
            r132 = r156
            r156 = r193
            r14 = r229
            r193 = r15
            r15 = r4
            boolean r4 = printTimer(r8, r9, r10, r11, r13, r14, r15)
            r4 = r88 | r4
            android.os.BatteryStats$Timer r10 = r193.getCameraTurnedOnTimer()
            java.lang.String r15 = "Camera"
            r9 = r128
            r11 = r195
            boolean r8 = printTimer(r8, r9, r10, r11, r13, r14, r15)
            r4 = r4 | r8
            android.os.BatteryStats$Timer r10 = r193.getVideoTurnedOnTimer()
            java.lang.String r15 = "Video"
            r8 = r228
            boolean r8 = printTimer(r8, r9, r10, r11, r13, r14, r15)
            r4 = r4 | r8
            android.os.BatteryStats$Timer r10 = r193.getAudioTurnedOnTimer()
            java.lang.String r15 = "Audio"
            r8 = r228
            boolean r8 = printTimer(r8, r9, r10, r11, r13, r14, r15)
            r4 = r4 | r8
            android.util.SparseArray r15 = r193.getSensorStats()
            int r14 = r15.size()
            r8 = 0
        L_0x1fd4:
            if (r8 >= r14) goto L_0x20c9
            java.lang.Object r9 = r15.valueAt(r8)
            android.os.BatteryStats$Uid$Sensor r9 = (android.os.BatteryStats.Uid.Sensor) r9
            int r10 = r15.keyAt(r8)
            r13 = r128
            r13.setLength(r0)
            r13.append(r2)
            java.lang.String r11 = "    Sensor "
            r13.append(r11)
            int r11 = r9.getHandle()
            r12 = -10000(0xffffffffffffd8f0, float:NaN)
            if (r11 != r12) goto L_0x1ffb
            java.lang.String r12 = "GPS"
            r13.append(r12)
            goto L_0x1ffe
        L_0x1ffb:
            r13.append(r11)
        L_0x1ffe:
            java.lang.String r12 = ": "
            r13.append(r12)
            android.os.BatteryStats$Timer r12 = r9.getSensorTime()
            if (r12 == 0) goto L_0x2096
            r0 = r195
            long r194 = r12.getTotalTimeLocked(r0, r3)
            long r194 = r194 + r18
            r199 = r0
            long r0 = r194 / r16
            r128 = r10
            int r10 = r12.getCountLocked(r3)
            r194 = r11
            android.os.BatteryStats$Timer r11 = r9.getSensorBackgroundTime()
            if (r11 == 0) goto L_0x2028
            int r195 = r11.getCountLocked(r3)
            goto L_0x202a
        L_0x2028:
            r195 = 0
        L_0x202a:
            r196 = r195
            r201 = r14
            r195 = r15
            long r14 = r12.getTotalDurationMsLocked(r5)
            if (r11 == 0) goto L_0x203b
            long r202 = r11.getTotalDurationMsLocked(r5)
            goto L_0x203d
        L_0x203b:
            r202 = r46
        L_0x203d:
            r204 = r202
            int r202 = (r0 > r46 ? 1 : (r0 == r46 ? 0 : -1))
            if (r-54 == 0) goto L_0x2088
            int r202 = (r14 > r0 ? 1 : (r14 == r0 ? 0 : -1))
            if (r-54 == 0) goto L_0x2052
            formatTimeMs(r13, r0)
            r202 = r0
            java.lang.String r0 = "blamed realtime, "
            r13.append(r0)
            goto L_0x2054
        L_0x2052:
            r202 = r0
        L_0x2054:
            formatTimeMs(r13, r14)
            java.lang.String r0 = "realtime ("
            r13.append(r0)
            r13.append(r10)
            r0 = r207
            r13.append(r0)
            r207 = r5
            r5 = r204
            int r1 = (r5 > r46 ? 1 : (r5 == r46 ? 0 : -1))
            if (r1 != 0) goto L_0x2072
            r1 = r196
            if (r1 <= 0) goto L_0x2095
            goto L_0x2074
        L_0x2072:
            r1 = r196
        L_0x2074:
            r13.append(r7)
            formatTimeMs(r13, r5)
            r204 = r5
            java.lang.String r5 = "background ("
            r13.append(r5)
            r13.append(r1)
            r13.append(r0)
            goto L_0x2095
        L_0x2088:
            r202 = r0
            r1 = r196
            r0 = r207
            r207 = r5
            java.lang.String r5 = "(not used)"
            r13.append(r5)
        L_0x2095:
            goto L_0x20a9
        L_0x2096:
            r128 = r10
            r194 = r11
            r201 = r14
            r199 = r195
            r0 = r207
            r207 = r5
            r195 = r15
            java.lang.String r1 = "(not used)"
            r13.append(r1)
        L_0x20a9:
            java.lang.String r1 = r13.toString()
            r5 = r228
            r14 = r199
            r5.println(r1)
            r4 = 1
            int r8 = r8 + 1
            r1 = r5
            r128 = r13
            r5 = r207
            r207 = r0
            r0 = 0
            r218 = r14
            r15 = r195
            r195 = r218
            r14 = r201
            goto L_0x1fd4
        L_0x20c9:
            r207 = r5
            r201 = r14
            r13 = r128
            r5 = r1
            r218 = r195
            r195 = r15
            r14 = r218
            android.os.BatteryStats$Timer r10 = r193.getVibratorOnTimer()
            java.lang.String r0 = "Vibrator"
            r8 = r228
            r9 = r13
            r11 = r14
            r1 = r13
            r13 = r230
            r199 = r14
            r6 = r201
            r14 = r229
            r128 = r195
            r15 = r0
            boolean r0 = printTimer(r8, r9, r10, r11, r13, r14, r15)
            r0 = r0 | r4
            android.os.BatteryStats$Timer r10 = r193.getForegroundActivityTimer()
            java.lang.String r15 = "Foreground activities"
            r9 = r1
            r11 = r199
            boolean r4 = printTimer(r8, r9, r10, r11, r13, r14, r15)
            r0 = r0 | r4
            android.os.BatteryStats$Timer r10 = r193.getForegroundServiceTimer()
            java.lang.String r15 = "Foreground services"
            boolean r4 = printTimer(r8, r9, r10, r11, r13, r14, r15)
            r0 = r0 | r4
            r8 = 0
            r4 = 0
        L_0x210d:
            r10 = 7
            if (r4 >= r10) goto L_0x214f
            r12 = r193
            r10 = r199
            long r13 = r12.getProcessStateTime(r4, r10, r3)
            int r15 = (r13 > r46 ? 1 : (r13 == r46 ? 0 : -1))
            if (r15 <= 0) goto L_0x2148
            long r8 = r8 + r13
            r15 = 0
            r1.setLength(r15)
            r1.append(r2)
            java.lang.String r15 = "    "
            r1.append(r15)
            java.lang.String[] r15 = android.os.BatteryStats.Uid.PROCESS_STATE_NAMES
            r15 = r15[r4]
            r1.append(r15)
            java.lang.String r15 = " for: "
            r1.append(r15)
            long r193 = r13 + r18
            r195 = r8
            long r8 = r193 / r16
            formatTimeMs(r1, r8)
            java.lang.String r8 = r1.toString()
            r5.println(r8)
            r0 = 1
            r8 = r195
        L_0x2148:
            int r4 = r4 + 1
            r199 = r10
            r193 = r12
            goto L_0x210d
        L_0x214f:
            r12 = r193
            r10 = r199
            int r4 = (r8 > r46 ? 1 : (r8 == r46 ? 0 : -1))
            if (r4 <= 0) goto L_0x2171
            r4 = 0
            r1.setLength(r4)
            r1.append(r2)
            java.lang.String r4 = "    Total running: "
            r1.append(r4)
            long r13 = r8 + r18
            long r13 = r13 / r16
            formatTimeMs(r1, r13)
            java.lang.String r4 = r1.toString()
            r5.println(r4)
        L_0x2171:
            long r13 = r12.getUserCpuTimeUs(r3)
            long r193 = r12.getSystemCpuTimeUs(r3)
            int r4 = (r13 > r46 ? 1 : (r13 == r46 ? 0 : -1))
            if (r4 > 0) goto L_0x2185
            int r4 = (r193 > r46 ? 1 : (r193 == r46 ? 0 : -1))
            if (r4 <= 0) goto L_0x2182
            goto L_0x2185
        L_0x2182:
            r195 = r8
            goto L_0x21aa
        L_0x2185:
            r4 = 0
            r1.setLength(r4)
            r1.append(r2)
            java.lang.String r4 = "    Total cpu time: u="
            r1.append(r4)
            r195 = r8
            long r8 = r13 / r16
            formatTimeMs(r1, r8)
            java.lang.String r4 = "s="
            r1.append(r4)
            long r8 = r193 / r16
            formatTimeMs(r1, r8)
            java.lang.String r4 = r1.toString()
            r5.println(r4)
        L_0x21aa:
            long[] r4 = r12.getCpuFreqTimes(r3)
            if (r4 == 0) goto L_0x21e6
            r8 = 0
            r1.setLength(r8)
            java.lang.String r8 = "    Total cpu time per freq:"
            r1.append(r8)
            r8 = 0
        L_0x21ba:
            int r9 = r4.length
            if (r8 >= r9) goto L_0x21da
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            r15 = r206
            r9.append(r15)
            r199 = r10
            r10 = r4[r8]
            r9.append(r10)
            java.lang.String r9 = r9.toString()
            r1.append(r9)
            int r8 = r8 + 1
            r10 = r199
            goto L_0x21ba
        L_0x21da:
            r199 = r10
            r15 = r206
            java.lang.String r8 = r1.toString()
            r5.println(r8)
            goto L_0x21ea
        L_0x21e6:
            r199 = r10
            r15 = r206
        L_0x21ea:
            long[] r8 = r12.getScreenOffCpuFreqTimes(r3)
            if (r8 == 0) goto L_0x2222
            r9 = 0
            r1.setLength(r9)
            java.lang.String r9 = "    Total screen-off cpu time per freq:"
            r1.append(r9)
            r9 = 0
        L_0x21fa:
            int r10 = r8.length
            if (r9 >= r10) goto L_0x2218
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            r10.append(r15)
            r201 = r13
            r13 = r8[r9]
            r10.append(r13)
            java.lang.String r10 = r10.toString()
            r1.append(r10)
            int r9 = r9 + 1
            r13 = r201
            goto L_0x21fa
        L_0x2218:
            r201 = r13
            java.lang.String r9 = r1.toString()
            r5.println(r9)
            goto L_0x2224
        L_0x2222:
            r201 = r13
        L_0x2224:
            r9 = 0
        L_0x2225:
            r10 = 7
            if (r9 >= r10) goto L_0x22d8
            long[] r10 = r12.getCpuFreqTimes(r3, r9)
            if (r10 == 0) goto L_0x227b
            r11 = 0
            r1.setLength(r11)
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            java.lang.String r13 = "    Cpu times per freq at state "
            r11.append(r13)
            java.lang.String[] r13 = android.os.BatteryStats.Uid.PROCESS_STATE_NAMES
            r13 = r13[r9]
            r11.append(r13)
            java.lang.String r13 = ":"
            r11.append(r13)
            java.lang.String r11 = r11.toString()
            r1.append(r11)
            r11 = 0
        L_0x2250:
            int r13 = r10.length
            if (r11 >= r13) goto L_0x2270
            java.lang.StringBuilder r13 = new java.lang.StringBuilder
            r13.<init>()
            r13.append(r15)
            r203 = r6
            r14 = r7
            r6 = r10[r11]
            r13.append(r6)
            java.lang.String r6 = r13.toString()
            r1.append(r6)
            int r11 = r11 + 1
            r7 = r14
            r6 = r203
            goto L_0x2250
        L_0x2270:
            r203 = r6
            r14 = r7
            java.lang.String r6 = r1.toString()
            r5.println(r6)
            goto L_0x227e
        L_0x227b:
            r203 = r6
            r14 = r7
        L_0x227e:
            long[] r6 = r12.getScreenOffCpuFreqTimes(r3, r9)
            if (r6 == 0) goto L_0x22ce
            r7 = 0
            r1.setLength(r7)
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r11 = "   Screen-off cpu times per freq at state "
            r7.append(r11)
            java.lang.String[] r11 = android.os.BatteryStats.Uid.PROCESS_STATE_NAMES
            r11 = r11[r9]
            r7.append(r11)
            java.lang.String r11 = ":"
            r7.append(r11)
            java.lang.String r7 = r7.toString()
            r1.append(r7)
            r7 = 0
        L_0x22a6:
            int r11 = r6.length
            if (r7 >= r11) goto L_0x22c4
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            r11.append(r15)
            r204 = r14
            r13 = r6[r7]
            r11.append(r13)
            java.lang.String r11 = r11.toString()
            r1.append(r11)
            int r7 = r7 + 1
            r14 = r204
            goto L_0x22a6
        L_0x22c4:
            r204 = r14
            java.lang.String r7 = r1.toString()
            r5.println(r7)
            goto L_0x22d0
        L_0x22ce:
            r204 = r14
        L_0x22d0:
            int r9 = r9 + 1
            r6 = r203
            r7 = r204
            goto L_0x2225
        L_0x22d8:
            r203 = r6
            r204 = r7
            android.util.ArrayMap r6 = r12.getProcessStats()
            if (r6 == 0) goto L_0x2473
            int r7 = r6.size()
            int r7 = r7 + -1
        L_0x22e9:
            if (r7 < 0) goto L_0x2468
            java.lang.Object r9 = r6.valueAt(r7)
            android.os.BatteryStats$Uid$Proc r9 = (android.os.BatteryStats.Uid.Proc) r9
            long r10 = r9.getUserTime(r3)
            long r13 = r9.getSystemTime(r3)
            r205 = r4
            long r4 = r9.getForegroundTime(r3)
            r206 = r0
            int r0 = r9.getStarts(r3)
            r209 = r8
            int r8 = r9.getNumCrashes(r3)
            r210 = r15
            int r15 = r9.getNumAnrs(r3)
            if (r3 != 0) goto L_0x2318
            int r211 = r9.countExcessivePowers()
            goto L_0x231a
        L_0x2318:
            r211 = 0
        L_0x231a:
            r212 = r211
            int r211 = (r10 > r46 ? 1 : (r10 == r46 ? 0 : -1))
            if (r-45 != 0) goto L_0x233b
            int r211 = (r13 > r46 ? 1 : (r13 == r46 ? 0 : -1))
            if (r-45 != 0) goto L_0x233b
            int r211 = (r4 > r46 ? 1 : (r4 == r46 ? 0 : -1))
            if (r-45 != 0) goto L_0x233b
            if (r0 != 0) goto L_0x233b
            r3 = r212
            if (r3 != 0) goto L_0x233d
            if (r8 != 0) goto L_0x233d
            if (r15 == 0) goto L_0x2333
            goto L_0x233d
        L_0x2333:
            r5 = r228
            r211 = r12
            r0 = r206
            goto L_0x245a
        L_0x233b:
            r3 = r212
        L_0x233d:
            r211 = r12
            r12 = 0
            r1.setLength(r12)
            r1.append(r2)
            java.lang.String r12 = "    Proc "
            r1.append(r12)
            java.lang.Object r12 = r6.keyAt(r7)
            java.lang.String r12 = (java.lang.String) r12
            r1.append(r12)
            java.lang.String r12 = ":\n"
            r1.append(r12)
            r1.append(r2)
            java.lang.String r12 = "      CPU: "
            r1.append(r12)
            formatTimeMs(r1, r10)
            java.lang.String r12 = "usr + "
            r1.append(r12)
            formatTimeMs(r1, r13)
            java.lang.String r12 = "krn ; "
            r1.append(r12)
            formatTimeMs(r1, r4)
            java.lang.String r12 = "fg"
            r1.append(r12)
            if (r0 != 0) goto L_0x2389
            if (r8 != 0) goto L_0x2389
            if (r15 == 0) goto L_0x2382
            goto L_0x2389
        L_0x2382:
            r212 = r0
            r213 = r4
            r0 = r204
            goto L_0x23d2
        L_0x2389:
            java.lang.String r12 = "\n"
            r1.append(r12)
            r1.append(r2)
            java.lang.String r12 = "      "
            r1.append(r12)
            r12 = 0
            if (r0 == 0) goto L_0x23a5
            r12 = 1
            r1.append(r0)
            r212 = r0
            java.lang.String r0 = " starts"
            r1.append(r0)
            goto L_0x23a7
        L_0x23a5:
            r212 = r0
        L_0x23a7:
            if (r8 == 0) goto L_0x23bf
            if (r12 == 0) goto L_0x23b1
            r0 = r204
            r1.append(r0)
            goto L_0x23b3
        L_0x23b1:
            r0 = r204
        L_0x23b3:
            r12 = 1
            r1.append(r8)
            r213 = r4
            java.lang.String r4 = " crashes"
            r1.append(r4)
            goto L_0x23c3
        L_0x23bf:
            r213 = r4
            r0 = r204
        L_0x23c3:
            if (r15 == 0) goto L_0x23d2
            if (r12 == 0) goto L_0x23ca
            r1.append(r0)
        L_0x23ca:
            r1.append(r15)
            java.lang.String r4 = " anrs"
            r1.append(r4)
        L_0x23d2:
            java.lang.String r4 = r1.toString()
            r5 = r228
            r5.println(r4)
            r4 = 0
        L_0x23dc:
            if (r4 >= r3) goto L_0x2450
            android.os.BatteryStats$Uid$Proc$ExcessivePower r12 = r9.getExcessivePower(r4)
            if (r12 == 0) goto L_0x243c
            r228.print(r229)
            r204 = r0
            java.lang.String r0 = "      * Killed for "
            r5.print(r0)
            int r0 = r12.type
            r215 = r3
            r3 = 2
            if (r0 != r3) goto L_0x23fb
            java.lang.String r0 = "cpu"
            r5.print(r0)
            goto L_0x2401
        L_0x23fb:
            java.lang.String r0 = "unknown"
            r5.print(r0)
        L_0x2401:
            java.lang.String r0 = " use: "
            r5.print(r0)
            r0 = r4
            long r3 = r12.usedTime
            android.util.TimeUtils.formatDuration(r3, r5)
            java.lang.String r3 = " over "
            r5.print(r3)
            long r3 = r12.overTime
            android.util.TimeUtils.formatDuration(r3, r5)
            long r3 = r12.overTime
            int r3 = (r3 > r46 ? 1 : (r3 == r46 ? 0 : -1))
            if (r3 == 0) goto L_0x2437
            java.lang.String r3 = " ("
            r5.print(r3)
            long r3 = r12.usedTime
            r216 = 100
            long r3 = r3 * r216
            r217 = r8
            r216 = r9
            long r8 = r12.overTime
            long r3 = r3 / r8
            r5.print(r3)
            java.lang.String r3 = "%)"
            r5.println(r3)
            goto L_0x2445
        L_0x2437:
            r217 = r8
            r216 = r9
            goto L_0x2445
        L_0x243c:
            r204 = r0
            r215 = r3
            r0 = r4
            r217 = r8
            r216 = r9
        L_0x2445:
            int r4 = r0 + 1
            r0 = r204
            r3 = r215
            r9 = r216
            r8 = r217
            goto L_0x23dc
        L_0x2450:
            r204 = r0
            r215 = r3
            r0 = r4
            r217 = r8
            r216 = r9
            r0 = 1
        L_0x245a:
            int r7 = r7 + -1
            r3 = r230
            r4 = r205
            r8 = r209
            r15 = r210
            r12 = r211
            goto L_0x22e9
        L_0x2468:
            r206 = r0
            r205 = r4
            r209 = r8
            r211 = r12
            r210 = r15
            goto L_0x247d
        L_0x2473:
            r205 = r4
            r209 = r8
            r211 = r12
            r210 = r15
            r206 = r0
        L_0x247d:
            android.util.ArrayMap r0 = r211.getPackageStats()
            if (r0 == 0) goto L_0x259d
            int r3 = r0.size()
            int r3 = r3 + -1
        L_0x248a:
            if (r3 < 0) goto L_0x258a
            r228.print(r229)
            java.lang.String r4 = "    Apk "
            r5.print(r4)
            java.lang.Object r4 = r0.keyAt(r3)
            java.lang.String r4 = (java.lang.String) r4
            r5.print(r4)
            java.lang.String r4 = ":"
            r5.println(r4)
            r4 = 0
            java.lang.Object r7 = r0.valueAt(r3)
            android.os.BatteryStats$Uid$Pkg r7 = (android.os.BatteryStats.Uid.Pkg) r7
            android.util.ArrayMap r8 = r7.getWakeupAlarmStats()
            int r9 = r8.size()
            int r9 = r9 + -1
        L_0x24b3:
            if (r9 < 0) goto L_0x24e3
            r228.print(r229)
            java.lang.String r10 = "      Wakeup alarm "
            r5.print(r10)
            java.lang.Object r10 = r8.keyAt(r9)
            java.lang.String r10 = (java.lang.String) r10
            r5.print(r10)
            java.lang.String r10 = ": "
            r5.print(r10)
            java.lang.Object r10 = r8.valueAt(r9)
            android.os.BatteryStats$Counter r10 = (android.os.BatteryStats.Counter) r10
            r11 = r230
            int r10 = r10.getCountLocked(r11)
            r5.print(r10)
            java.lang.String r10 = " times"
            r5.println(r10)
            r4 = 1
            int r9 = r9 + -1
            goto L_0x24b3
        L_0x24e3:
            r11 = r230
            android.util.ArrayMap r9 = r7.getServiceStats()
            int r10 = r9.size()
            int r10 = r10 + -1
        L_0x24ef:
            if (r10 < 0) goto L_0x256c
            java.lang.Object r12 = r9.valueAt(r10)
            android.os.BatteryStats$Uid$Pkg$Serv r12 = (android.os.BatteryStats.Uid.Pkg.Serv) r12
            r13 = r122
            long r122 = r12.getStartTime(r13, r11)
            int r15 = r12.getStarts(r11)
            r204 = r0
            int r0 = r12.getLaunches(r11)
            int r212 = (r122 > r46 ? 1 : (r122 == r46 ? 0 : -1))
            if (r-44 != 0) goto L_0x2515
            if (r15 != 0) goto L_0x2515
            if (r0 == 0) goto L_0x2510
            goto L_0x2515
        L_0x2510:
            r212 = r6
            r213 = r7
            goto L_0x2561
        L_0x2515:
            r212 = r6
            r6 = 0
            r1.setLength(r6)
            r1.append(r2)
            java.lang.String r6 = "      Service "
            r1.append(r6)
            java.lang.Object r6 = r9.keyAt(r10)
            java.lang.String r6 = (java.lang.String) r6
            r1.append(r6)
            java.lang.String r6 = ":\n"
            r1.append(r6)
            r1.append(r2)
            java.lang.String r6 = "        Created for: "
            r1.append(r6)
            r213 = r7
            long r6 = r122 / r16
            formatTimeMs(r1, r6)
            java.lang.String r6 = "uptime\n"
            r1.append(r6)
            r1.append(r2)
            java.lang.String r6 = "        Starts: "
            r1.append(r6)
            r1.append(r15)
            java.lang.String r6 = ", launches: "
            r1.append(r6)
            r1.append(r0)
            java.lang.String r6 = r1.toString()
            r5.println(r6)
            r4 = 1
        L_0x2561:
            int r10 = r10 + -1
            r122 = r13
            r0 = r204
            r6 = r212
            r7 = r213
            goto L_0x24ef
        L_0x256c:
            r204 = r0
            r212 = r6
            r213 = r7
            r13 = r122
            if (r4 != 0) goto L_0x257e
            r228.print(r229)
            java.lang.String r0 = "      (nothing executed)"
            r5.println(r0)
        L_0x257e:
            r206 = 1
            int r3 = r3 + -1
            r122 = r13
            r0 = r204
            r6 = r212
            goto L_0x248a
        L_0x258a:
            r11 = r230
            r204 = r0
            r212 = r6
            r13 = r122
            if (r-50 != 0) goto L_0x25a5
            r228.print(r229)
            java.lang.String r0 = "    (nothing executed)"
            r5.println(r0)
            goto L_0x25a5
        L_0x259d:
            r11 = r230
            r204 = r0
            r212 = r6
            r13 = r122
        L_0x25a5:
            int r12 = r119 + 1
            r7 = r226
            r11 = r5
            r122 = r13
            r155 = r135
            r15 = r145
            r127 = r182
            r6 = r183
            r151 = r189
            r4 = r199
            r125 = r207
            r13 = r210
            r3 = 0
            r14 = r1
            r0 = r136
            goto L_0x141d
        L_0x25c2:
            return
            switch-data {1->0x0e11, 2->0x0e0b, 3->0x0e05, 4->0x0dff, 5->0x0df9, 6->0x0df3, 7->0x0ded, 8->0x0de7, 9->0x0dd3, 10->0x0dc3, 11->0x0dbd, 12->0x0db7, 13->0x0db1, }
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.BatteryStats.dumpLocked(android.content.Context, java.io.PrintWriter, java.lang.String, int, int, boolean):void");
    }

    static void printBitDescriptions(StringBuilder sb, int oldval, int newval, HistoryTag wakelockTag, BitDescription[] descriptions, boolean longNames) {
        int diff = oldval ^ newval;
        if (diff != 0) {
            boolean didWake = false;
            for (BitDescription bd : descriptions) {
                if ((bd.mask & diff) != 0) {
                    sb.append(longNames ? WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER : SmsManager.REGEX_PREFIX_DELIMITER);
                    if (bd.shift < 0) {
                        sb.append((bd.mask & newval) != 0 ? HwCustPlusAndIddNddConvertUtils.PLUS_PREFIX : NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
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
            if (!(didWake || wakelockTag == null)) {
                sb.append(longNames ? " wake_lock=" : ",w=");
                if (longNames) {
                    UserHandle.formatUid(sb, wakelockTag.uid);
                    sb.append(":\"");
                    sb.append(wakelockTag.string);
                    sb.append("\"");
                    return;
                }
                sb.append(wakelockTag.poolIdx);
            }
        }
    }

    public void prepareForDumpLocked() {
    }

    public static class HistoryPrinter {
        long lastTime = -1;
        int oldChargeMAh = -1;
        int oldHealth = -1;
        int oldLevel = -1;
        double oldModemRailChargeMah = -1.0d;
        int oldPlug = -1;
        int oldState = 0;
        int oldState2 = 0;
        int oldStatus = -1;
        int oldTemp = -1;
        int oldVolt = -1;
        double oldWifiRailChargeMah = -1.0d;

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
            this.oldModemRailChargeMah = -1.0d;
            this.oldWifiRailChargeMah = -1.0d;
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
            } else {
                byte b = rec.cmd;
                String str = WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
                if (b == 5 || rec.cmd == 7) {
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
                        item.append(str);
                        item.append(DateFormat.format("yyyy-MM-dd-HH-mm-ss", rec.currentTime).toString());
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
                        item.append((int) rec.batteryLevel);
                        if (verbose) {
                            item.append(str);
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
                        item.append((int) rec.batteryLevel);
                    }
                    if (this.oldStatus != rec.batteryStatus) {
                        this.oldStatus = rec.batteryStatus;
                        item.append(checkin ? ",Bs=" : " status=");
                        int i = this.oldStatus;
                        if (i == 1) {
                            item.append(checkin ? "?" : "unknown");
                        } else if (i == 2) {
                            item.append(checkin ? FullBackup.CACHE_TREE_TOKEN : "charging");
                        } else if (i == 3) {
                            item.append(checkin ? "d" : "discharging");
                        } else if (i == 4) {
                            item.append(checkin ? "n" : "not-charging");
                        } else if (i != 5) {
                            item.append(i);
                        } else {
                            item.append(checkin ? FullBackup.FILES_TREE_TOKEN : "full");
                        }
                    }
                    if (this.oldHealth != rec.batteryHealth) {
                        this.oldHealth = rec.batteryHealth;
                        item.append(checkin ? ",Bh=" : " health=");
                        int i2 = this.oldHealth;
                        switch (i2) {
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
                                item.append(checkin ? FullBackup.FILES_TREE_TOKEN : "failure");
                                break;
                            case 7:
                                item.append(checkin ? FullBackup.CACHE_TREE_TOKEN : "cold");
                                break;
                            default:
                                item.append(i2);
                                break;
                        }
                    }
                    if (this.oldPlug != rec.batteryPlugType) {
                        this.oldPlug = rec.batteryPlugType;
                        item.append(checkin ? ",Bp=" : " plug=");
                        int i3 = this.oldPlug;
                        if (i3 == 0) {
                            item.append(checkin ? "n" : "none");
                        } else if (i3 == 1) {
                            item.append(checkin ? FullBackup.APK_TREE_TOKEN : "ac");
                        } else if (i3 == 2) {
                            item.append(checkin ? "u" : Context.USB_SERVICE);
                        } else if (i3 != 4) {
                            item.append(i3);
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
                    if (this.oldModemRailChargeMah != rec.modemRailChargeMah) {
                        this.oldModemRailChargeMah = rec.modemRailChargeMah;
                        item.append(checkin ? ",Mrc=" : " modemRailChargemAh=");
                        item.append(new DecimalFormat("#.##").format(this.oldModemRailChargeMah));
                    }
                    if (this.oldWifiRailChargeMah != rec.wifiRailChargeMah) {
                        this.oldWifiRailChargeMah = rec.wifiRailChargeMah;
                        item.append(checkin ? ",Wrc=" : " wifiRailChargemAh=");
                        item.append(new DecimalFormat("#.##").format(this.oldWifiRailChargeMah));
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
                        if (checkin) {
                            str = SmsManager.REGEX_PREFIX_DELIMITER;
                        }
                        item.append(str);
                        if ((rec.eventCode & 32768) != 0) {
                            item.append(HwCustPlusAndIddNddConvertUtils.PLUS_PREFIX);
                        } else if ((rec.eventCode & 16384) != 0) {
                            item.append(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                        }
                        if (checkin) {
                            eventNames = BatteryStats.HISTORY_EVENT_CHECKIN_NAMES;
                        } else {
                            eventNames = BatteryStats.HISTORY_EVENT_NAMES;
                        }
                        int idx = rec.eventCode & HistoryItem.EVENT_TYPE_MASK;
                        if (idx < 0 || idx >= eventNames.length) {
                            item.append(checkin ? "Ev" : "event");
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
                                item.append(String.format("%.1f%%", Float.valueOf((((float) totalRun) / ((float) total)) * 100.0f)));
                                item.append(" of ");
                                StringBuilder sb = new StringBuilder(64);
                                BatteryStats.formatTimeMsNoSpace(sb, (long) (total * 10));
                                item.append((CharSequence) sb);
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
        int count2;
        String str;
        String str2 = header;
        LevelStepTracker levelStepTracker = steps;
        char c = 0;
        if (levelStepTracker == null || (count = levelStepTracker.mNumStepDurations) <= 0) {
            return false;
        }
        if (!checkin) {
            pw.println(str2);
        }
        String[] lineArgs = new String[5];
        int i = 0;
        while (i < count) {
            long duration = levelStepTracker.getDurationAt(i);
            int level = levelStepTracker.getLevelAt(i);
            long initMode = (long) levelStepTracker.getInitModeAt(i);
            long modMode = (long) levelStepTracker.getModModeAt(i);
            if (checkin) {
                lineArgs[c] = Long.toString(duration);
                lineArgs[1] = Integer.toString(level);
                if ((modMode & 3) == 0) {
                    count2 = count;
                    int i2 = ((int) (initMode & 3)) + 1;
                    if (i2 == 1) {
                        lineArgs[2] = "s-";
                    } else if (i2 == 2) {
                        lineArgs[2] = "s+";
                    } else if (i2 == 3) {
                        lineArgs[2] = "sd";
                    } else if (i2 != 4) {
                        lineArgs[2] = "?";
                    } else {
                        lineArgs[2] = "sds";
                    }
                } else {
                    count2 = count;
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
                dumpLine(pw, 0, "i", str2, lineArgs);
            } else {
                count2 = count;
                pw.print(prefix);
                pw.print("#");
                pw.print(i);
                pw.print(": ");
                TimeUtils.formatDuration(duration, pw);
                pw.print(" to ");
                pw.print(level);
                boolean haveModes = false;
                if ((modMode & 3) == 0) {
                    pw.print(" (");
                    int i3 = ((int) (initMode & 3)) + 1;
                    if (i3 == 1) {
                        pw.print("screen-off");
                    } else if (i3 == 2) {
                        pw.print("screen-on");
                    } else if (i3 == 3) {
                        pw.print("screen-doze");
                    } else if (i3 != 4) {
                        pw.print("screen-?");
                    } else {
                        pw.print("screen-doze-suspend");
                    }
                    haveModes = true;
                }
                String str3 = ", ";
                if ((modMode & 4) == 0) {
                    if (haveModes) {
                        str = str3;
                    } else {
                        str = " (";
                    }
                    pw.print(str);
                    pw.print((initMode & 4) != 0 ? "power-save-on" : "power-save-off");
                    haveModes = true;
                }
                if ((modMode & 8) == 0) {
                    if (!haveModes) {
                        str3 = " (";
                    }
                    pw.print(str3);
                    pw.print((8 & initMode) != 0 ? "device-idle-on" : "device-idle-off");
                    haveModes = true;
                }
                if (haveModes) {
                    pw.print(")");
                }
                pw.println();
            }
            i++;
            str2 = header;
            levelStepTracker = steps;
            count = count2;
            c = 0;
        }
        return true;
    }

    private static void dumpDurationSteps(ProtoOutputStream proto, long fieldId, LevelStepTracker steps) {
        if (steps != null) {
            int count = steps.mNumStepDurations;
            for (int i = 0; i < count; i++) {
                long token = proto.start(fieldId);
                proto.write(1112396529665L, steps.getDurationAt(i));
                proto.write(1120986464258L, steps.getLevelAt(i));
                long initMode = (long) steps.getInitModeAt(i);
                long modMode = (long) steps.getModModeAt(i);
                int ds = 0;
                int i2 = 2;
                int i3 = 1;
                if ((modMode & 3) == 0) {
                    int i4 = ((int) (3 & initMode)) + 1;
                    if (i4 == 1) {
                        ds = 2;
                    } else if (i4 == 2) {
                        ds = 1;
                    } else if (i4 == 3) {
                        ds = 3;
                    } else if (i4 != 4) {
                        ds = 5;
                    } else {
                        ds = 4;
                    }
                }
                proto.write(1159641169923L, ds);
                int psm = 0;
                if ((modMode & 4) == 0) {
                    if ((4 & initMode) == 0) {
                        i3 = 2;
                    }
                    psm = i3;
                }
                proto.write(1159641169924L, psm);
                int im = 0;
                if ((modMode & 8) == 0) {
                    if ((8 & initMode) == 0) {
                        i2 = 3;
                    }
                    im = i2;
                }
                proto.write(1159641169925L, im);
                proto.end(token);
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r11v1, types: [byte] */
    /* JADX WARN: Type inference failed for: r11v12 */
    /* JADX WARN: Type inference failed for: r11v13 */
    /* JADX WARN: Type inference failed for: r11v14 */
    /* JADX WARNING: Unknown variable types count: 1 */
    private void dumpHistoryLocked(PrintWriter pw, int flags, long histStart, boolean checkin) {
        long baseTime;
        boolean printed;
        ?? r11;
        HistoryPrinter hprinter = new HistoryPrinter();
        HistoryItem rec = new HistoryItem();
        long lastTime = -1;
        long baseTime2 = -1;
        boolean printed2 = false;
        HistoryEventTracker tracker = null;
        while (getNextHistoryLocked(rec)) {
            long lastTime2 = rec.time;
            if (baseTime2 < 0) {
                baseTime = lastTime2;
            } else {
                baseTime = baseTime2;
            }
            if (rec.time >= histStart) {
                if (histStart < 0 || printed2) {
                    printed = printed2;
                } else {
                    if (rec.cmd == 5 || rec.cmd == 7 || rec.cmd == 4 || rec.cmd == 8) {
                        printed = true;
                        r11 = 0;
                        hprinter.printNextItem(pw, rec, baseTime, checkin, (flags & 32) != 0);
                        rec.cmd = 0;
                    } else if (rec.currentTime != 0) {
                        printed = true;
                        byte cmd = rec.cmd;
                        rec.cmd = 5;
                        hprinter.printNextItem(pw, rec, baseTime, checkin, (flags & 32) != 0);
                        rec.cmd = cmd;
                        r11 = 0;
                    } else {
                        printed = printed2;
                        r11 = 0;
                    }
                    if (tracker != null) {
                        if (rec.cmd != 0) {
                            hprinter.printNextItem(pw, rec, baseTime, checkin, (flags & 32) != 0 ? true : r11);
                            rec.cmd = r11;
                        }
                        int oldEventCode = rec.eventCode;
                        HistoryTag oldEventTag = rec.eventTag;
                        rec.eventTag = new HistoryTag();
                        int i = 0;
                        boolean z = r11;
                        while (i < 22) {
                            HashMap<String, SparseIntArray> active = tracker.getStateForEvent(i);
                            if (active != null) {
                                boolean z2 = z;
                                for (Map.Entry<String, SparseIntArray> ent : active.entrySet()) {
                                    SparseIntArray uids = ent.getValue();
                                    int j = 0;
                                    boolean z3 = z2;
                                    while (j < uids.size()) {
                                        rec.eventCode = i;
                                        rec.eventTag.string = ent.getKey();
                                        rec.eventTag.uid = uids.keyAt(j);
                                        rec.eventTag.poolIdx = uids.valueAt(j);
                                        hprinter.printNextItem(pw, rec, baseTime, checkin, (flags & 32) != 0 ? true : z3);
                                        rec.wakeReasonTag = null;
                                        rec.wakelockTag = null;
                                        j++;
                                        oldEventTag = oldEventTag;
                                        uids = uids;
                                        i = i;
                                        z3 = false;
                                    }
                                    z2 = false;
                                }
                            }
                            i++;
                            oldEventTag = oldEventTag;
                            z = false;
                        }
                        rec.eventCode = oldEventCode;
                        rec.eventTag = oldEventTag;
                        tracker = null;
                    }
                }
                hprinter.printNextItem(pw, rec, baseTime, checkin, (flags & 32) != 0);
                printed2 = printed;
                lastTime = lastTime2;
                baseTime2 = baseTime;
            } else {
                lastTime = lastTime2;
                baseTime2 = baseTime;
            }
        }
        if (histStart >= 0) {
            commitCurrentHistoryBatchLocked();
            pw.print(checkin ? "NEXT: " : "  NEXT: ");
            pw.println(1 + lastTime);
        }
    }

    private void dumpDailyLevelStepSummary(PrintWriter pw, String prefix, String label, LevelStepTracker steps, StringBuilder tmpSb, int[] tmpOutInt) {
        if (steps != null) {
            long timeRemaining = steps.computeTimeEstimate(0, 0, tmpOutInt);
            if (timeRemaining >= 0) {
                pw.print(prefix);
                pw.print(label);
                pw.print(" total time: ");
                tmpSb.setLength(0);
                formatTimeMs(tmpSb, timeRemaining);
                pw.print(tmpSb);
                pw.print(" (from ");
                pw.print(tmpOutInt[0]);
                pw.println(" steps)");
            }
            int i = 0;
            while (true) {
                int[] iArr = STEP_LEVEL_MODES_OF_INTEREST;
                if (i < iArr.length) {
                    long estimatedTime = steps.computeTimeEstimate((long) iArr[i], (long) STEP_LEVEL_MODE_VALUES[i], tmpOutInt);
                    if (estimatedTime > 0) {
                        pw.print(prefix);
                        pw.print(label);
                        pw.print(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                        pw.print(STEP_LEVEL_MODE_LABELS[i]);
                        pw.print(" time: ");
                        tmpSb.setLength(0);
                        formatTimeMs(tmpSb, estimatedTime);
                        pw.print(tmpSb);
                        pw.print(" (from ");
                        pw.print(tmpOutInt[0]);
                        pw.println(" steps)");
                    }
                    i++;
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

    public void dumpLocked(Context context, PrintWriter pw, int flags, int reqUid, long histStart) {
        boolean z;
        boolean z2;
        boolean z3;
        CharSequence charSequence;
        boolean z4;
        int[] outInt;
        LevelStepTracker dsteps;
        String str;
        LevelStepTracker dsteps2;
        CharSequence charSequence2;
        boolean z5;
        DailyItem dit;
        String str2;
        LevelStepTracker csteps;
        ArrayList<PackageChange> pkgc;
        long baseTime;
        prepareForDumpLocked();
        boolean filtering = (flags & 14) != 0;
        if ((flags & 8) != 0 || !filtering) {
            long historyTotalSize = (long) getHistoryTotalSize();
            long historyUsedSize = (long) getHistoryUsedSize();
            if (startIteratingHistoryLocked()) {
                try {
                    pw.print("Battery History (");
                    pw.print((100 * historyUsedSize) / historyTotalSize);
                    pw.print("% used, ");
                    printSizeValue(pw, historyUsedSize);
                    pw.print(" used of ");
                    printSizeValue(pw, historyTotalSize);
                    pw.print(", ");
                    pw.print(getHistoryStringPoolSize());
                    pw.print(" strings using ");
                    printSizeValue(pw, (long) getHistoryStringPoolBytes());
                    pw.println("):");
                    try {
                        dumpHistoryLocked(pw, flags, histStart, false);
                        pw.println();
                        finishIteratingHistoryLocked();
                    } catch (Throwable th) {
                        th = th;
                        finishIteratingHistoryLocked();
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    finishIteratingHistoryLocked();
                    throw th;
                }
            }
            if (startIteratingOldHistoryLocked()) {
                try {
                    HistoryItem rec = new HistoryItem();
                    pw.println("Old battery History:");
                    HistoryPrinter hprinter = new HistoryPrinter();
                    long baseTime2 = -1;
                    while (getNextOldHistoryLocked(rec)) {
                        if (baseTime2 < 0) {
                            baseTime = rec.time;
                        } else {
                            baseTime = baseTime2;
                        }
                        hprinter.printNextItem(pw, rec, baseTime, false, (flags & 32) != 0);
                        baseTime2 = baseTime;
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
                boolean didPid = false;
                long nowRealtime = SystemClock.elapsedRealtime();
                for (int i = 0; i < NU; i++) {
                    SparseArray<? extends Uid.Pid> pids = ((Uid) uidStats.valueAt(i)).getPidStats();
                    if (pids != null) {
                        for (int j = 0; j < pids.size(); j++) {
                            Uid.Pid pid = (Uid.Pid) pids.valueAt(j);
                            if (!didPid) {
                                pw.println("Per-PID Stats:");
                                didPid = true;
                            }
                            long j2 = pid.mWakeSumMs;
                            long j3 = pid.mWakeNesting > 0 ? nowRealtime - pid.mWakeStartMs : 0;
                            pw.print("  PID ");
                            pw.print(pids.keyAt(j));
                            pw.print(" wake time: ");
                            TimeUtils.formatDuration(j2 + j3, pw);
                            pw.println("");
                        }
                    }
                }
                if (didPid) {
                    pw.println();
                }
            }
            if (!filtering || (flags & 2) != 0) {
                if (dumpDurationSteps(pw, "  ", "Discharge step durations:", getDischargeLevelStepTracker(), false)) {
                    long timeRemaining = computeBatteryTimeRemaining(SystemClock.elapsedRealtime() * 1000);
                    if (timeRemaining >= 0) {
                        pw.print("  Estimated discharge time remaining: ");
                        TimeUtils.formatDuration(timeRemaining / 1000, pw);
                        pw.println();
                    }
                    LevelStepTracker steps = getDischargeLevelStepTracker();
                    int i2 = 0;
                    while (true) {
                        int[] iArr = STEP_LEVEL_MODES_OF_INTEREST;
                        if (i2 >= iArr.length) {
                            break;
                        }
                        dumpTimeEstimate(pw, "  Estimated ", STEP_LEVEL_MODE_LABELS[i2], " time: ", steps.computeTimeEstimate((long) iArr[i2], (long) STEP_LEVEL_MODE_VALUES[i2], null));
                        i2++;
                    }
                    pw.println();
                }
                z = false;
                if (dumpDurationSteps(pw, "  ", "Charge step durations:", getChargeLevelStepTracker(), false)) {
                    long timeRemaining2 = computeChargeTimeRemaining(SystemClock.elapsedRealtime() * 1000);
                    if (timeRemaining2 >= 0) {
                        pw.print("  Estimated charge time remaining: ");
                        TimeUtils.formatDuration(timeRemaining2 / 1000, pw);
                        pw.println();
                    }
                    pw.println();
                }
            } else {
                z = false;
            }
            if (!filtering || (flags & 4) != 0) {
                pw.println("Daily stats:");
                pw.print("  Current start time: ");
                pw.println(DateFormat.format("yyyy-MM-dd-HH-mm-ss", getCurrentDailyStartTime()).toString());
                pw.print("  Next min deadline: ");
                pw.println(DateFormat.format("yyyy-MM-dd-HH-mm-ss", getNextMinDailyDeadline()).toString());
                pw.print("  Next max deadline: ");
                pw.println(DateFormat.format("yyyy-MM-dd-HH-mm-ss", getNextMaxDailyDeadline()).toString());
                StringBuilder sb = new StringBuilder(64);
                int[] outInt2 = new int[1];
                LevelStepTracker dsteps3 = getDailyDischargeLevelStepTracker();
                LevelStepTracker csteps2 = getDailyChargeLevelStepTracker();
                ArrayList<PackageChange> pkgc2 = getDailyPackageChanges();
                if (dsteps3.mNumStepDurations > 0 || csteps2.mNumStepDurations > 0 || pkgc2 != null) {
                    if ((flags & 4) != 0) {
                        z2 = true;
                        str = "    ";
                        pkgc = pkgc2;
                        csteps = csteps2;
                        dsteps = dsteps3;
                        outInt = outInt2;
                        z4 = z;
                        charSequence = "yyyy-MM-dd-HH-mm-ss";
                    } else if (!filtering) {
                        z2 = true;
                        str = "    ";
                        pkgc = pkgc2;
                        csteps = csteps2;
                        dsteps = dsteps3;
                        outInt = outInt2;
                        z4 = z;
                        charSequence = "yyyy-MM-dd-HH-mm-ss";
                    } else {
                        pw.println("  Current daily steps:");
                        str = "    ";
                        dumpDailyLevelStepSummary(pw, "    ", "Discharge", dsteps3, sb, outInt2);
                        dsteps = dsteps3;
                        outInt = outInt2;
                        z4 = z;
                        charSequence = "yyyy-MM-dd-HH-mm-ss";
                        z2 = true;
                        dumpDailyLevelStepSummary(pw, "    ", "Charge", csteps2, sb, outInt);
                    }
                    if (dumpDurationSteps(pw, str, "  Current daily discharge step durations:", dsteps, z4)) {
                        dumpDailyLevelStepSummary(pw, "      ", "Discharge", dsteps, sb, outInt);
                    }
                    if (dumpDurationSteps(pw, str, "  Current daily charge step durations:", csteps, z4)) {
                        dumpDailyLevelStepSummary(pw, "      ", "Charge", csteps, sb, outInt);
                    }
                    dumpDailyPackageChanges(pw, str, pkgc);
                } else {
                    z2 = true;
                    str = "    ";
                    dsteps = dsteps3;
                    outInt = outInt2;
                    z4 = z;
                    charSequence = "yyyy-MM-dd-HH-mm-ss";
                }
                int curIndex = 0;
                while (true) {
                    DailyItem dit2 = getDailyItemLocked(curIndex);
                    if (dit2 == null) {
                        break;
                    }
                    int curIndex2 = curIndex + 1;
                    if ((flags & 4) != 0) {
                        pw.println();
                    }
                    pw.print("  Daily from ");
                    pw.print(DateFormat.format(charSequence, dit2.mStartTime).toString());
                    pw.print(" to ");
                    pw.print(DateFormat.format(charSequence, dit2.mEndTime).toString());
                    pw.println(SettingsStringUtil.DELIMITER);
                    if ((flags & 4) != 0) {
                        charSequence2 = charSequence;
                        dit = dit2;
                    } else if (!filtering) {
                        charSequence2 = charSequence;
                        dit = dit2;
                    } else {
                        charSequence2 = charSequence;
                        dumpDailyLevelStepSummary(pw, "    ", "Discharge", dit2.mDischargeSteps, sb, outInt);
                        dumpDailyLevelStepSummary(pw, "    ", "Charge", dit2.mChargeSteps, sb, outInt);
                        dsteps2 = dsteps;
                        z5 = false;
                        z4 = z5;
                        curIndex = curIndex2;
                        charSequence = charSequence2;
                        dsteps = dsteps2;
                    }
                    if (dumpDurationSteps(pw, "      ", "    Discharge step durations:", dit.mDischargeSteps, false)) {
                        dsteps2 = dsteps;
                        str2 = "      ";
                        dumpDailyLevelStepSummary(pw, "        ", "Discharge", dit.mDischargeSteps, sb, outInt);
                    } else {
                        dsteps2 = dsteps;
                        str2 = "      ";
                    }
                    if (dumpDurationSteps(pw, str2, "    Charge step durations:", dit.mChargeSteps, false)) {
                        z5 = false;
                        dumpDailyLevelStepSummary(pw, "        ", "Charge", dit.mChargeSteps, sb, outInt);
                    } else {
                        z5 = false;
                    }
                    dumpDailyPackageChanges(pw, str, dit.mPackageChanges);
                    z4 = z5;
                    curIndex = curIndex2;
                    charSequence = charSequence2;
                    dsteps = dsteps2;
                }
                z3 = z4;
                pw.println();
            } else {
                z3 = z;
                z2 = true;
            }
            if (!filtering || (flags & 2) != 0) {
                pw.println("Statistics since last charge:");
                pw.println("  System starts: " + getStartCount() + ", currently on battery: " + getIsOnBattery());
                dumpLocked(context, pw, "", 0, reqUid, (flags & 64) != 0 ? z2 : z3);
                pw.println();
            }
        }
    }

    public void dumpCheckinLocked(Context context, PrintWriter pw, List<ApplicationInfo> apps, int flags, long histStart) {
        prepareForDumpLocked();
        boolean z = true;
        dumpLine(pw, 0, "i", VERSION_DATA, 34, Integer.valueOf(getParcelVersion()), getStartPlatformVersion(), getEndPlatformVersion());
        long historyBaseTime = getHistoryBaseTime() + SystemClock.elapsedRealtime();
        if ((flags & 24) != 0 && startIteratingHistoryLocked()) {
            for (int i = 0; i < getHistoryStringPoolSize(); i++) {
                try {
                    pw.print(9);
                    pw.print(',');
                    pw.print(HISTORY_STRING_POOL);
                    pw.print(',');
                    pw.print(i);
                    pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
                    pw.print(getHistoryTagPoolUid(i));
                    pw.print(",\"");
                    pw.print(getHistoryTagPoolString(i).replace("\\", "\\\\").replace("\"", "\\\""));
                    pw.print("\"");
                    pw.println();
                } finally {
                    finishIteratingHistoryLocked();
                }
            }
            dumpHistoryLocked(pw, flags, histStart, true);
        }
        if ((flags & 8) == 0) {
            if (apps != null) {
                SparseArray<Pair<ArrayList<String>, MutableBoolean>> uids = new SparseArray<>();
                for (int i2 = 0; i2 < apps.size(); i2++) {
                    ApplicationInfo ai = apps.get(i2);
                    Pair<ArrayList<String>, MutableBoolean> pkgs = uids.get(UserHandle.getAppId(ai.uid));
                    if (pkgs == null) {
                        pkgs = new Pair<>(new ArrayList(), new MutableBoolean(false));
                        uids.put(UserHandle.getAppId(ai.uid), pkgs);
                    }
                    pkgs.first.add(ai.packageName);
                }
                SparseArray<? extends Uid> uidStats = getUidStats();
                int NU = uidStats.size();
                String[] lineArgs = new String[2];
                int i3 = 0;
                while (i3 < NU) {
                    int uid = UserHandle.getAppId(uidStats.keyAt(i3));
                    Pair<ArrayList<String>, MutableBoolean> pkgs2 = uids.get(uid);
                    if (pkgs2 != null && !pkgs2.second.value) {
                        pkgs2.second.value = z;
                        int j = 0;
                        while (j < pkgs2.first.size()) {
                            lineArgs[0] = Integer.toString(uid);
                            lineArgs[1] = (String) pkgs2.first.get(j);
                            dumpLine(pw, 0, "i", "uid", lineArgs);
                            j++;
                            uids = uids;
                        }
                    }
                    i3++;
                    uids = uids;
                    z = true;
                }
            }
            if ((flags & 4) == 0) {
                dumpDurationSteps(pw, "", DISCHARGE_STEP_DATA, getDischargeLevelStepTracker(), true);
                String[] lineArgs2 = new String[1];
                long timeRemaining = computeBatteryTimeRemaining(SystemClock.elapsedRealtime() * 1000);
                if (timeRemaining >= 0) {
                    lineArgs2[0] = Long.toString(timeRemaining);
                    dumpLine(pw, 0, "i", DISCHARGE_TIME_REMAIN_DATA, lineArgs2);
                }
                dumpDurationSteps(pw, "", CHARGE_STEP_DATA, getChargeLevelStepTracker(), true);
                long timeRemaining2 = computeChargeTimeRemaining(1000 * SystemClock.elapsedRealtime());
                if (timeRemaining2 >= 0) {
                    lineArgs2[0] = Long.toString(timeRemaining2);
                    dumpLine(pw, 0, "i", CHARGE_TIME_REMAIN_DATA, lineArgs2);
                }
                dumpCheckinLocked(context, pw, 0, -1, (flags & 64) != 0);
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
        proto.write(1120986464257L, 34);
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
        List<BatterySipper> sippers;
        ArrayList<String> pkgs;
        long rawRealtimeMs;
        long uTkn;
        ArrayMap<String, ? extends Uid.Pkg> packageStats;
        Timer bleTimer;
        long j;
        SparseArray<BatterySipper> uidToSipper;
        long j2;
        long nToken;
        Uid.Wakelock wl;
        int uid;
        BatterySipper bs;
        long cpuToken;
        Uid u;
        SparseArray<BatterySipper> uidToSipper2;
        Timer bleTimer2;
        long[] cpuFreqs;
        int ipkg;
        long rawRealtimeMs2;
        long batteryUptimeUs;
        SparseArray<ArrayList<String>> aidToPackages;
        long rawRealtimeUs;
        long rawUptimeUs;
        ArrayMap<String, ? extends Uid.Pkg> packageStats2;
        int ipkg2;
        long rawRealtimeMs3;
        ArrayList<String> pkgs2;
        int which = 0;
        long rawUptimeUs2 = SystemClock.uptimeMillis() * 1000;
        long rawRealtimeMs4 = SystemClock.elapsedRealtime();
        long rawRealtimeUs2 = rawRealtimeMs4 * 1000;
        long batteryUptimeUs2 = getBatteryUptime(rawUptimeUs2);
        SparseArray<ArrayList<String>> aidToPackages2 = new SparseArray<>();
        if (apps != null) {
            for (int i = 0; i < apps.size(); i++) {
                ApplicationInfo ai = apps.get(i);
                int aid = UserHandle.getAppId(ai.uid);
                ArrayList<String> pkgs3 = aidToPackages2.get(aid);
                if (pkgs3 == null) {
                    pkgs2 = new ArrayList<>();
                    aidToPackages2.put(aid, pkgs2);
                } else {
                    pkgs2 = pkgs3;
                }
                pkgs2.add(ai.packageName);
            }
        }
        SparseArray<BatterySipper> uidToSipper3 = new SparseArray<>();
        List<BatterySipper> sippers2 = helper.getUsageList();
        if (sippers2 != null) {
            int i2 = 0;
            while (i2 < sippers2.size()) {
                BatterySipper bs2 = sippers2.get(i2);
                if (bs2.drainType == BatterySipper.DrainType.APP) {
                    uidToSipper3.put(bs2.uidObj.getUid(), bs2);
                }
                i2++;
                sippers2 = sippers2;
            }
            sippers = sippers2;
        } else {
            sippers = sippers2;
        }
        SparseArray<? extends Uid> uidStats = getUidStats();
        int n = uidStats.size();
        int iu = 0;
        while (iu < n) {
            long uTkn2 = proto.start(2246267895813L);
            Uid u2 = (Uid) uidStats.valueAt(iu);
            int uid2 = uidStats.keyAt(iu);
            proto.write(1120986464257L, uid2);
            SparseArray<ArrayList<String>> aidToPackages3 = aidToPackages2;
            ArrayList<String> pkgs4 = aidToPackages3.get(UserHandle.getAppId(uid2));
            if (pkgs4 == null) {
                pkgs = new ArrayList<>();
            } else {
                pkgs = pkgs4;
            }
            ArrayMap<String, ? extends Uid.Pkg> packageStats3 = u2.getPackageStats();
            int ipkg3 = packageStats3.size() - 1;
            while (ipkg3 >= 0) {
                String pkg = packageStats3.keyAt(ipkg3);
                ArrayMap<String, ? extends Uid.Pkg.Serv> serviceStats = ((Uid.Pkg) packageStats3.valueAt(ipkg3)).getServiceStats();
                if (serviceStats.size() == 0) {
                    packageStats2 = packageStats3;
                    ipkg = ipkg3;
                    aidToPackages = aidToPackages3;
                    batteryUptimeUs = batteryUptimeUs2;
                    rawUptimeUs = rawUptimeUs2;
                    rawRealtimeMs2 = rawRealtimeMs4;
                    rawRealtimeUs = rawRealtimeUs2;
                } else {
                    rawUptimeUs = rawUptimeUs2;
                    rawRealtimeUs = rawRealtimeUs2;
                    long pToken = proto.start(2246267895810L);
                    proto.write(1138166333441L, pkg);
                    pkgs.remove(pkg);
                    int isvc = serviceStats.size() - 1;
                    while (isvc >= 0) {
                        Uid.Pkg.Serv ss = (Uid.Pkg.Serv) serviceStats.valueAt(isvc);
                        long startTimeMs = roundUsToMs(ss.getStartTime(batteryUptimeUs2, 0));
                        int starts = ss.getStarts(0);
                        int launches = ss.getLaunches(0);
                        if (startTimeMs == 0 && starts == 0 && launches == 0) {
                            ipkg2 = ipkg3;
                            rawRealtimeMs3 = rawRealtimeMs4;
                        } else {
                            rawRealtimeMs3 = rawRealtimeMs4;
                            long sToken = proto.start(2246267895810L);
                            ipkg2 = ipkg3;
                            proto.write(1138166333441L, serviceStats.keyAt(isvc));
                            proto.write(1112396529666L, startTimeMs);
                            proto.write(1120986464259L, starts);
                            proto.write(1120986464260L, launches);
                            proto.end(sToken);
                        }
                        isvc--;
                        packageStats3 = packageStats3;
                        aidToPackages3 = aidToPackages3;
                        pkg = pkg;
                        batteryUptimeUs2 = batteryUptimeUs2;
                        rawRealtimeMs4 = rawRealtimeMs3;
                        ipkg3 = ipkg2;
                    }
                    packageStats2 = packageStats3;
                    ipkg = ipkg3;
                    aidToPackages = aidToPackages3;
                    batteryUptimeUs = batteryUptimeUs2;
                    rawRealtimeMs2 = rawRealtimeMs4;
                    proto.end(pToken);
                }
                ipkg3 = ipkg - 1;
                which = which;
                packageStats3 = packageStats2;
                uidStats = uidStats;
                rawUptimeUs2 = rawUptimeUs;
                rawRealtimeUs2 = rawRealtimeUs;
                aidToPackages3 = aidToPackages;
                batteryUptimeUs2 = batteryUptimeUs;
                rawRealtimeMs4 = rawRealtimeMs2;
            }
            long rawRealtimeUs3 = rawRealtimeUs2;
            Iterator<String> it = pkgs.iterator();
            while (it.hasNext()) {
                long pToken2 = proto.start(2246267895810L);
                proto.write(1138166333441L, it.next());
                proto.end(pToken2);
            }
            if (u2.getAggregatedPartialWakelockTimer() != null) {
                Timer timer = u2.getAggregatedPartialWakelockTimer();
                rawRealtimeMs = rawRealtimeMs4;
                long totTimeMs = timer.getTotalDurationMsLocked(rawRealtimeMs);
                Timer bgTimer = timer.getSubTimer();
                long bgTimeMs = bgTimer != null ? bgTimer.getTotalDurationMsLocked(rawRealtimeMs) : 0;
                long awToken = proto.start(1146756268056L);
                proto.write(1112396529665L, totTimeMs);
                proto.write(1112396529666L, bgTimeMs);
                proto.end(awToken);
            } else {
                rawRealtimeMs = rawRealtimeMs4;
            }
            long uTkn3 = uTkn2;
            ArrayMap<String, ? extends Uid.Pkg> packageStats4 = packageStats3;
            SparseArray<BatterySipper> uidToSipper4 = uidToSipper3;
            Uid u3 = u2;
            dumpTimer(proto, 1146756268040L, u2.getAudioTurnedOnTimer(), rawRealtimeUs3, 0);
            dumpControllerActivityProto(proto, 1146756268035L, u3.getBluetoothControllerActivity(), 0);
            Timer bleTimer3 = u3.getBluetoothScanTimer();
            if (bleTimer3 != null) {
                long bmToken = proto.start(1146756268038L);
                dumpTimer(proto, 1146756268033L, bleTimer3, rawRealtimeUs3, 0);
                dumpTimer(proto, 1146756268034L, u3.getBluetoothScanBackgroundTimer(), rawRealtimeUs3, 0);
                dumpTimer(proto, 1146756268035L, u3.getBluetoothUnoptimizedScanTimer(), rawRealtimeUs3, 0);
                dumpTimer(proto, 1146756268036L, u3.getBluetoothUnoptimizedScanBackgroundTimer(), rawRealtimeUs3, 0);
                proto.write(1120986464261L, u3.getBluetoothScanResultCounter() != null ? u3.getBluetoothScanResultCounter().getCountLocked(0) : 0);
                proto.write(1120986464262L, u3.getBluetoothScanResultBgCounter() != null ? u3.getBluetoothScanResultBgCounter().getCountLocked(0) : 0);
                proto.end(bmToken);
            }
            dumpTimer(proto, 1146756268041L, u3.getCameraTurnedOnTimer(), rawRealtimeUs3, 0);
            long cpuToken2 = proto.start(1146756268039L);
            proto.write(1112396529665L, roundUsToMs(u3.getUserCpuTimeUs(0)));
            proto.write(1112396529666L, roundUsToMs(u3.getSystemCpuTimeUs(0)));
            long[] cpuFreqs2 = getCpuFreqs();
            if (cpuFreqs2 != null) {
                long[] cpuFreqTimeMs = u3.getCpuFreqTimes(0);
                if (cpuFreqTimeMs == null || cpuFreqTimeMs.length != cpuFreqs2.length) {
                    bleTimer = bleTimer3;
                    packageStats = packageStats4;
                    uTkn = uTkn3;
                } else {
                    long[] screenOffCpuFreqTimeMs = u3.getScreenOffCpuFreqTimes(0);
                    if (screenOffCpuFreqTimeMs == null) {
                        screenOffCpuFreqTimeMs = new long[cpuFreqTimeMs.length];
                    }
                    int ic = 0;
                    while (ic < cpuFreqTimeMs.length) {
                        long cToken = proto.start(2246267895811L);
                        proto.write(1120986464257L, ic + 1);
                        proto.write(1112396529666L, cpuFreqTimeMs[ic]);
                        proto.write(1112396529667L, screenOffCpuFreqTimeMs[ic]);
                        proto.end(cToken);
                        ic++;
                        bleTimer3 = bleTimer3;
                        packageStats4 = packageStats4;
                        uTkn3 = uTkn3;
                    }
                    bleTimer = bleTimer3;
                    packageStats = packageStats4;
                    uTkn = uTkn3;
                }
            } else {
                bleTimer = bleTimer3;
                packageStats = packageStats4;
                uTkn = uTkn3;
            }
            int procState = 0;
            while (true) {
                j = 1159641169921L;
                if (procState >= 7) {
                    break;
                }
                long[] timesMs = u3.getCpuFreqTimes(0, procState);
                if (timesMs == null || timesMs.length != cpuFreqs2.length) {
                    uidToSipper2 = uidToSipper4;
                    u = u3;
                    cpuFreqs = cpuFreqs2;
                    bleTimer2 = bleTimer;
                } else {
                    long[] screenOffTimesMs = u3.getScreenOffCpuFreqTimes(0, procState);
                    if (screenOffTimesMs == null) {
                        screenOffTimesMs = new long[timesMs.length];
                    }
                    long procToken = proto.start(2246267895812L);
                    proto.write(1159641169921L, procState);
                    int ic2 = 0;
                    while (ic2 < timesMs.length) {
                        long cToken2 = proto.start(2246267895810L);
                        proto.write(1120986464257L, ic2 + 1);
                        proto.write(1112396529666L, timesMs[ic2]);
                        proto.write(1112396529667L, screenOffTimesMs[ic2]);
                        proto.end(cToken2);
                        ic2++;
                        cpuFreqs2 = cpuFreqs2;
                        bleTimer = bleTimer;
                        uidToSipper4 = uidToSipper4;
                        u3 = u3;
                    }
                    uidToSipper2 = uidToSipper4;
                    u = u3;
                    cpuFreqs = cpuFreqs2;
                    bleTimer2 = bleTimer;
                    proto.end(procToken);
                }
                procState++;
                cpuFreqs2 = cpuFreqs;
                bleTimer = bleTimer2;
                uidToSipper4 = uidToSipper2;
                u3 = u;
            }
            proto.end(cpuToken2);
            long cpuToken3 = cpuToken2;
            dumpTimer(proto, 1146756268042L, u3.getFlashlightTurnedOnTimer(), rawRealtimeUs3, 0);
            dumpTimer(proto, 1146756268043L, u3.getForegroundActivityTimer(), rawRealtimeUs3, 0);
            dumpTimer(proto, 1146756268044L, u3.getForegroundServiceTimer(), rawRealtimeUs3, 0);
            ArrayMap<String, SparseIntArray> completions = u3.getJobCompletionStats();
            int[] reasons = {0, 1, 2, 3, 4};
            int ic3 = 0;
            while (ic3 < completions.size()) {
                SparseIntArray types = completions.valueAt(ic3);
                if (types != null) {
                    long jcToken = proto.start(2246267895824L);
                    proto.write(1138166333441L, completions.keyAt(ic3));
                    int length = reasons.length;
                    int i3 = 0;
                    while (i3 < length) {
                        int r = reasons[i3];
                        long rToken = proto.start(2246267895810L);
                        proto.write(j, r);
                        proto.write(1120986464258L, types.get(r, 0));
                        proto.end(rToken);
                        i3++;
                        cpuToken3 = cpuToken3;
                        j = 1159641169921L;
                    }
                    cpuToken = cpuToken3;
                    proto.end(jcToken);
                } else {
                    cpuToken = cpuToken3;
                }
                ic3++;
                cpuToken3 = cpuToken;
                j = 1159641169921L;
            }
            ArrayMap<String, ? extends Timer> jobs = u3.getJobStats();
            int ij = jobs.size() - 1;
            while (ij >= 0) {
                Timer timer2 = (Timer) jobs.valueAt(ij);
                Timer bgTimer2 = timer2.getSubTimer();
                long jToken = proto.start(2246267895823L);
                proto.write(1138166333441L, jobs.keyAt(ij));
                dumpTimer(proto, 1146756268034L, timer2, rawRealtimeUs3, 0);
                dumpTimer(proto, 1146756268035L, bgTimer2, rawRealtimeUs3, 0);
                proto.end(jToken);
                ij--;
                reasons = reasons;
            }
            dumpControllerActivityProto(proto, 1146756268036L, u3.getModemControllerActivity(), 0);
            long nToken2 = proto.start(1146756268049L);
            Uid u4 = u3;
            proto.write(1112396529665L, u4.getNetworkActivityBytes(0, 0));
            proto.write(1112396529666L, u4.getNetworkActivityBytes(1, 0));
            proto.write(1112396529667L, u4.getNetworkActivityBytes(2, 0));
            proto.write(1112396529668L, u4.getNetworkActivityBytes(3, 0));
            proto.write(1112396529669L, u4.getNetworkActivityBytes(4, 0));
            proto.write(1112396529670L, u4.getNetworkActivityBytes(5, 0));
            proto.write(1112396529671L, u4.getNetworkActivityPackets(0, 0));
            proto.write(1112396529672L, u4.getNetworkActivityPackets(1, 0));
            proto.write(1112396529673L, u4.getNetworkActivityPackets(2, 0));
            proto.write(1112396529674L, u4.getNetworkActivityPackets(3, 0));
            proto.write(1112396529675L, roundUsToMs(u4.getMobileRadioActiveTime(0)));
            proto.write(1120986464268L, u4.getMobileRadioActiveCount(0));
            proto.write(1120986464269L, u4.getMobileRadioApWakeupCount(0));
            proto.write(1120986464270L, u4.getWifiRadioApWakeupCount(0));
            proto.write(1112396529679L, u4.getNetworkActivityBytes(6, 0));
            proto.write(1112396529680L, u4.getNetworkActivityBytes(7, 0));
            proto.write(1112396529681L, u4.getNetworkActivityBytes(8, 0));
            proto.write(1112396529682L, u4.getNetworkActivityBytes(9, 0));
            proto.write(1112396529683L, u4.getNetworkActivityPackets(6, 0));
            proto.write(1112396529684L, u4.getNetworkActivityPackets(7, 0));
            proto.write(1112396529685L, u4.getNetworkActivityPackets(8, 0));
            proto.write(1112396529686L, u4.getNetworkActivityPackets(9, 0));
            proto.end(nToken2);
            int uid3 = uid2;
            BatterySipper bs3 = uidToSipper4.get(uid3);
            if (bs3 != null) {
                long bsToken = proto.start(1146756268050L);
                uidToSipper = uidToSipper4;
                proto.write(1103806595073L, bs3.totalPowerMah);
                proto.write(1133871366146L, bs3.shouldHide);
                proto.write(1103806595075L, bs3.screenPowerMah);
                proto.write(1103806595076L, bs3.proportionalSmearMah);
                proto.end(bsToken);
            } else {
                uidToSipper = uidToSipper4;
            }
            ArrayMap<String, ? extends Uid.Proc> processStats = u4.getProcessStats();
            int ipr = processStats.size() - 1;
            while (ipr >= 0) {
                Uid.Proc ps = (Uid.Proc) processStats.valueAt(ipr);
                long prToken = proto.start(2246267895827L);
                proto.write(1138166333441L, processStats.keyAt(ipr));
                proto.write(1112396529666L, ps.getUserTime(0));
                proto.write(1112396529667L, ps.getSystemTime(0));
                proto.write(1112396529668L, ps.getForegroundTime(0));
                proto.write(1120986464261L, ps.getStarts(0));
                proto.write(1120986464262L, ps.getNumAnrs(0));
                proto.write(1120986464263L, ps.getNumCrashes(0));
                proto.end(prToken);
                ipr--;
                completions = completions;
                u4 = u4;
            }
            Uid u5 = u4;
            SparseArray<? extends Uid.Sensor> sensors = u5.getSensorStats();
            int ise = 0;
            while (ise < sensors.size()) {
                Uid.Sensor se = (Uid.Sensor) sensors.valueAt(ise);
                Timer timer3 = se.getSensorTime();
                if (timer3 == null) {
                    bs = bs3;
                    uid = uid3;
                } else {
                    Timer bgTimer3 = se.getSensorBackgroundTime();
                    int sensorNumber = sensors.keyAt(ise);
                    long seToken = proto.start(2246267895829L);
                    proto.write(1120986464257L, sensorNumber);
                    bs = bs3;
                    uid = uid3;
                    dumpTimer(proto, 1146756268034L, timer3, rawRealtimeUs3, 0);
                    dumpTimer(proto, 1146756268035L, bgTimer3, rawRealtimeUs3, 0);
                    proto.end(seToken);
                }
                ise++;
                bs3 = bs;
                uid3 = uid;
            }
            int ips = 0;
            while (ips < 7) {
                long durMs = roundUsToMs(u5.getProcessStateTime(ips, rawRealtimeUs3, 0));
                if (durMs == 0) {
                    rawRealtimeUs3 = rawRealtimeUs3;
                } else {
                    long stToken = proto.start(2246267895828L);
                    proto.write(1159641169921L, ips);
                    rawRealtimeUs3 = rawRealtimeUs3;
                    proto.write(1112396529666L, durMs);
                    proto.end(stToken);
                }
                ips++;
                u5 = u5;
            }
            ArrayMap<String, ? extends Timer> syncs = u5.getSyncStats();
            int isy = syncs.size() - 1;
            while (isy >= 0) {
                Timer timer4 = (Timer) syncs.valueAt(isy);
                Timer bgTimer4 = timer4.getSubTimer();
                long syToken = proto.start(2246267895830L);
                proto.write(1138166333441L, syncs.keyAt(isy));
                dumpTimer(proto, 1146756268034L, timer4, rawRealtimeUs3, 0);
                dumpTimer(proto, 1146756268035L, bgTimer4, rawRealtimeUs3, 0);
                proto.end(syToken);
                isy--;
                syncs = syncs;
                rawRealtimeUs3 = rawRealtimeUs3;
            }
            if (u5.hasUserActivity()) {
                for (int i4 = 0; i4 < Uid.NUM_USER_ACTIVITY_TYPES; i4++) {
                    int val = u5.getUserActivityCount(i4, 0);
                    if (val != 0) {
                        long uaToken = proto.start(2246267895831L);
                        proto.write(1159641169921L, i4);
                        proto.write(1120986464258L, val);
                        proto.end(uaToken);
                    }
                }
                j2 = 1120986464258L;
            } else {
                j2 = 1120986464258L;
            }
            dumpTimer(proto, 1146756268045L, u5.getVibratorOnTimer(), rawRealtimeUs3, 0);
            dumpTimer(proto, 1146756268046L, u5.getVideoTurnedOnTimer(), rawRealtimeUs3, 0);
            ArrayMap<String, ? extends Uid.Wakelock> wakelocks = u5.getWakelockStats();
            int iw = wakelocks.size() - 1;
            while (iw >= 0) {
                Uid.Wakelock wl2 = (Uid.Wakelock) wakelocks.valueAt(iw);
                long wToken = proto.start(2246267895833L);
                proto.write(1138166333441L, wakelocks.keyAt(iw));
                dumpTimer(proto, 1146756268034L, wl2.getWakeTime(1), rawRealtimeUs3, 0);
                Timer pTimer = wl2.getWakeTime(0);
                if (pTimer != null) {
                    nToken = nToken2;
                    wl = wl2;
                    dumpTimer(proto, 1146756268035L, pTimer, rawRealtimeUs3, 0);
                    dumpTimer(proto, 1146756268036L, pTimer.getSubTimer(), rawRealtimeUs3, 0);
                } else {
                    nToken = nToken2;
                    wl = wl2;
                }
                dumpTimer(proto, 1146756268037L, wl.getWakeTime(2), rawRealtimeUs3, 0);
                proto.end(wToken);
                iw--;
                wakelocks = wakelocks;
                nToken2 = nToken;
            }
            dumpTimer(proto, 1146756268060L, u5.getMulticastWakelockStats(), rawRealtimeUs3, 0);
            int i5 = 1;
            int ipkg4 = packageStats.size() - 1;
            while (ipkg4 >= 0) {
                ArrayMap<String, ? extends Counter> alarms = ((Uid.Pkg) packageStats.valueAt(ipkg4)).getWakeupAlarmStats();
                for (int iwa = alarms.size() - i5; iwa >= 0; iwa--) {
                    long waToken = proto.start(2246267895834L);
                    proto.write(1138166333441L, alarms.keyAt(iwa));
                    proto.write(1120986464258L, ((Counter) alarms.valueAt(iwa)).getCountLocked(0));
                    proto.end(waToken);
                }
                ipkg4--;
                packageStats = packageStats;
                i5 = 1;
            }
            dumpControllerActivityProto(proto, 1146756268037L, u5.getWifiControllerActivity(), 0);
            long wToken2 = proto.start(1146756268059L);
            proto.write(1112396529665L, roundUsToMs(u5.getFullWifiLockTime(rawRealtimeUs3, 0)));
            dumpTimer(proto, 1146756268035L, u5.getWifiScanTimer(), rawRealtimeUs3, 0);
            proto.write(1112396529666L, roundUsToMs(u5.getWifiRunningTime(rawRealtimeUs3, 0)));
            dumpTimer(proto, 1146756268036L, u5.getWifiScanBackgroundTimer(), rawRealtimeUs3, 0);
            proto.end(wToken2);
            proto.end(uTkn);
            iu++;
            which = which;
            sippers = sippers;
            aidToPackages2 = aidToPackages3;
            batteryUptimeUs2 = batteryUptimeUs2;
            rawRealtimeMs4 = rawRealtimeMs;
            n = n;
            uidStats = uidStats;
            rawRealtimeUs2 = rawRealtimeUs3;
            rawUptimeUs2 = rawUptimeUs2;
            uidToSipper3 = uidToSipper;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v5, resolved type: byte */
    /* JADX DEBUG: Multi-variable search result rejected for r0v7, resolved type: byte */
    /* JADX DEBUG: Multi-variable search result rejected for r0v11, resolved type: byte */
    /* JADX DEBUG: Multi-variable search result rejected for r0v13, resolved type: byte */
    /* JADX DEBUG: Multi-variable search result rejected for r0v15, resolved type: byte */
    /* JADX DEBUG: Multi-variable search result rejected for r0v18, resolved type: byte */
    /* JADX DEBUG: Multi-variable search result rejected for r0v20, resolved type: byte */
    /* JADX DEBUG: Multi-variable search result rejected for r0v21, resolved type: byte */
    /* JADX DEBUG: Multi-variable search result rejected for r0v24, resolved type: byte */
    /* JADX DEBUG: Multi-variable search result rejected for r8v13, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r8v14, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r8v15, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r8v16, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r8v17, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r8v18, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r7v9, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r7v10, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r7v11, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00ed A[Catch:{ all -> 0x0219 }] */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x01c0 A[Catch:{ all -> 0x0219 }] */
    private void dumpProtoHistoryLocked(ProtoOutputStream proto, int flags, long histStart) {
        long baseTime;
        HistoryEventTracker tracker;
        byte cmd;
        boolean z;
        int i;
        HistoryEventTracker tracker2;
        HistoryTag oldEventTag;
        if (startIteratingHistoryLocked()) {
            proto.write(1120986464257L, 34);
            proto.write(1112396529666L, getParcelVersion());
            proto.write(1138166333443L, getStartPlatformVersion());
            proto.write(1138166333444L, getEndPlatformVersion());
            byte b = 0;
            for (int i2 = 0; i2 < getHistoryStringPoolSize(); i2++) {
                try {
                    long token = proto.start(2246267895813L);
                    proto.write(1120986464257L, i2);
                    proto.write(1120986464258L, getHistoryTagPoolUid(i2));
                    proto.write(1138166333443L, getHistoryTagPoolString(i2));
                    proto.end(token);
                } finally {
                    finishIteratingHistoryLocked();
                }
            }
            HistoryPrinter hprinter = new HistoryPrinter();
            HistoryItem rec = new HistoryItem();
            long lastTime = -1;
            long baseTime2 = -1;
            byte printed = 0;
            HistoryEventTracker tracker3 = null;
            while (getNextHistoryLocked(rec)) {
                HistoryEventTracker tracker4 = tracker3;
                long lastTime2 = rec.time;
                if (baseTime2 < 0) {
                    baseTime = lastTime2;
                } else {
                    baseTime = baseTime2;
                }
                if (rec.time >= histStart) {
                    if (histStart < 0 || printed != 0) {
                        tracker = tracker4;
                        z = false;
                        cmd = printed;
                    } else {
                        if (!(rec.cmd == 5 || rec.cmd == 7 || rec.cmd == 4)) {
                            if (rec.cmd != 8) {
                                if (rec.currentTime != 0) {
                                    byte cmd2 = rec.cmd;
                                    rec.cmd = 5;
                                    hprinter.printNextItem(proto, rec, baseTime, (flags & 32) != 0 ? 1 : b);
                                    rec.cmd = cmd2;
                                    cmd = 1;
                                } else {
                                    cmd = printed;
                                }
                                if (tracker4 == null) {
                                    if (rec.cmd != 0) {
                                        hprinter.printNextItem(proto, rec, baseTime, (flags & 32) != 0 ? 1 : b);
                                        rec.cmd = b;
                                    }
                                    int oldEventCode = rec.eventCode;
                                    HistoryTag oldEventTag2 = rec.eventTag;
                                    rec.eventTag = new HistoryTag();
                                    int i3 = 0;
                                    while (i3 < 22) {
                                        HistoryEventTracker tracker5 = tracker4;
                                        HashMap<String, SparseIntArray> active = tracker5.getStateForEvent(i3);
                                        if (active == null) {
                                            i = i3;
                                            tracker2 = tracker5;
                                            oldEventTag = oldEventTag2;
                                        } else {
                                            for (Map.Entry<String, SparseIntArray> ent : active.entrySet()) {
                                                SparseIntArray uids = ent.getValue();
                                                int j = b;
                                                while (j < uids.size()) {
                                                    rec.eventCode = i3;
                                                    rec.eventTag.string = ent.getKey();
                                                    rec.eventTag.uid = uids.keyAt(j);
                                                    rec.eventTag.poolIdx = uids.valueAt(j);
                                                    hprinter.printNextItem(proto, rec, baseTime, (flags & 32) != 0);
                                                    rec.wakeReasonTag = null;
                                                    rec.wakelockTag = null;
                                                    j++;
                                                    oldEventTag2 = oldEventTag2;
                                                    uids = uids;
                                                    tracker5 = tracker5;
                                                    i3 = i3;
                                                }
                                                b = 0;
                                            }
                                            i = i3;
                                            tracker2 = tracker5;
                                            oldEventTag = oldEventTag2;
                                        }
                                        i3 = i + 1;
                                        oldEventTag2 = oldEventTag;
                                        tracker4 = tracker2;
                                        b = 0;
                                    }
                                    z = false;
                                    rec.eventCode = oldEventCode;
                                    rec.eventTag = oldEventTag2;
                                    tracker = null;
                                } else {
                                    tracker = tracker4;
                                    z = false;
                                }
                            }
                        }
                        cmd = 1;
                        hprinter.printNextItem(proto, rec, baseTime, (flags & 32) != 0 ? 1 : b);
                        rec.cmd = b;
                        if (tracker4 == null) {
                        }
                    }
                    hprinter.printNextItem(proto, rec, baseTime, (flags & 32) != 0);
                    lastTime = lastTime2;
                    printed = cmd;
                    baseTime2 = baseTime;
                    tracker3 = tracker;
                    b = 0;
                } else {
                    lastTime = lastTime2;
                    baseTime2 = baseTime;
                    tracker3 = tracker4;
                    b = 0;
                }
            }
            if (histStart >= 0) {
                commitCurrentHistoryBatchLocked();
                proto.write(2237677961222L, "NEXT: " + (1 + lastTime));
            }
        }
    }

    private void dumpProtoSystemLocked(ProtoOutputStream proto, BatteryStatsHelper helper) {
        int i;
        long sToken;
        long multicastWakeLockTimeTotalUs;
        int i2;
        long pdcToken;
        ProtoOutputStream protoOutputStream = proto;
        long sToken2 = protoOutputStream.start(1146756268038L);
        long rawUptimeUs = SystemClock.uptimeMillis() * 1000;
        long rawRealtimeUs = SystemClock.elapsedRealtime() * 1000;
        helper.create(this);
        helper.refreshStats(0, -1);
        int estimatedBatteryCapacity = (int) helper.getPowerProfile().getBatteryCapacity();
        long bToken = protoOutputStream.start(1146756268033L);
        protoOutputStream.write(1112396529665L, getStartClockTime());
        protoOutputStream.write(1112396529666L, getStartCount());
        protoOutputStream.write(1112396529667L, computeRealtime(rawRealtimeUs, 0) / 1000);
        protoOutputStream.write(1112396529668L, computeUptime(rawUptimeUs, 0) / 1000);
        protoOutputStream.write(1112396529669L, computeBatteryRealtime(rawRealtimeUs, 0) / 1000);
        protoOutputStream.write(1112396529670L, computeBatteryUptime(rawUptimeUs, 0) / 1000);
        protoOutputStream.write(1112396529671L, computeBatteryScreenOffRealtime(rawRealtimeUs, 0) / 1000);
        protoOutputStream.write(1112396529672L, computeBatteryScreenOffUptime(rawUptimeUs, 0) / 1000);
        protoOutputStream.write(1112396529673L, getScreenDozeTime(rawRealtimeUs, 0) / 1000);
        protoOutputStream.write(1112396529674L, estimatedBatteryCapacity);
        protoOutputStream.write(1112396529675L, getMinLearnedBatteryCapacity());
        protoOutputStream.write(1112396529676L, getMaxLearnedBatteryCapacity());
        protoOutputStream.end(bToken);
        long bdToken = protoOutputStream.start(1146756268034L);
        protoOutputStream.write(1120986464257L, getLowDischargeAmountSinceCharge());
        protoOutputStream.write(1120986464258L, getHighDischargeAmountSinceCharge());
        protoOutputStream.write(1120986464259L, getDischargeAmountScreenOnSinceCharge());
        protoOutputStream.write(1120986464260L, getDischargeAmountScreenOffSinceCharge());
        protoOutputStream.write(1120986464261L, getDischargeAmountScreenDozeSinceCharge());
        protoOutputStream.write(1112396529670L, getUahDischarge(0) / 1000);
        protoOutputStream.write(1112396529671L, getUahDischargeScreenOff(0) / 1000);
        protoOutputStream.write(1112396529672L, getUahDischargeScreenDoze(0) / 1000);
        protoOutputStream.write(1112396529673L, getUahDischargeLightDoze(0) / 1000);
        protoOutputStream.write(1112396529674L, getUahDischargeDeepDoze(0) / 1000);
        protoOutputStream.end(bdToken);
        long timeRemainingUs = computeChargeTimeRemaining(rawRealtimeUs);
        if (timeRemainingUs >= 0) {
            protoOutputStream.write(1112396529667L, timeRemainingUs / 1000);
        } else {
            long timeRemainingUs2 = computeBatteryTimeRemaining(rawRealtimeUs);
            if (timeRemainingUs2 >= 0) {
                protoOutputStream.write(1112396529668L, timeRemainingUs2 / 1000);
            } else {
                protoOutputStream.write(1112396529668L, -1);
            }
        }
        dumpDurationSteps(protoOutputStream, 2246267895813L, getChargeLevelStepTracker());
        int i3 = 0;
        while (true) {
            i = 1;
            boolean isNone = true;
            if (i3 >= 22) {
                break;
            }
            if (i3 != 0) {
                isNone = false;
            }
            int telephonyNetworkType = i3 == 21 ? 0 : i3;
            long pdcToken2 = protoOutputStream.start(2246267895816L);
            if (isNone) {
                pdcToken = pdcToken2;
                protoOutputStream.write(1133871366146L, isNone);
            } else {
                pdcToken = pdcToken2;
                protoOutputStream.write(1159641169921L, telephonyNetworkType);
            }
            rawRealtimeUs = rawRealtimeUs;
            dumpTimer(proto, 1146756268035L, getPhoneDataConnectionTimer(i3), rawRealtimeUs, 0);
            protoOutputStream.end(pdcToken);
            i3++;
        }
        dumpDurationSteps(protoOutputStream, 2246267895814L, getDischargeLevelStepTracker());
        long[] cpuFreqs = getCpuFreqs();
        if (cpuFreqs != null) {
            int length = cpuFreqs.length;
            int i4 = 0;
            while (i4 < length) {
                protoOutputStream.write(SystemProto.CPU_FREQUENCY, cpuFreqs[i4]);
                i4++;
                sToken2 = sToken2;
            }
            sToken = sToken2;
        } else {
            sToken = sToken2;
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
        protoOutputStream.end(gnToken);
        dumpControllerActivityProto(protoOutputStream, 1146756268043L, getWifiControllerActivity(), 0);
        long gwToken = protoOutputStream.start(1146756268045L);
        long[] cpuFreqs2 = cpuFreqs;
        protoOutputStream.write(1112396529665L, getWifiOnTime(rawRealtimeUs, 0) / 1000);
        protoOutputStream.write(1112396529666L, getGlobalWifiRunningTime(rawRealtimeUs, 0) / 1000);
        protoOutputStream.end(gwToken);
        for (Map.Entry<String, ? extends Timer> ent : getKernelWakelockStats().entrySet()) {
            long kwToken = protoOutputStream.start(2246267895822L);
            protoOutputStream = proto;
            protoOutputStream.write(1138166333441L, ent.getKey());
            dumpTimer(proto, 1146756268034L, (Timer) ent.getValue(), rawRealtimeUs, 0);
            protoOutputStream.end(kwToken);
            i = 1;
            gwToken = gwToken;
            gnToken = gnToken;
            cpuFreqs2 = cpuFreqs2;
        }
        SparseArray<? extends Uid> uidStats = getUidStats();
        long fullWakeLockTimeTotalUs = 0;
        long partialWakeLockTimeTotalUs = 0;
        for (int iu = 0; iu < uidStats.size(); iu++) {
            ArrayMap<String, ? extends Uid.Wakelock> wakelocks = ((Uid) uidStats.valueAt(iu)).getWakelockStats();
            for (int iw = wakelocks.size() - i; iw >= 0; iw--) {
                Uid.Wakelock wl = (Uid.Wakelock) wakelocks.valueAt(iw);
                Timer fullWakeTimer = wl.getWakeTime(i);
                if (fullWakeTimer != null) {
                    i2 = 0;
                    fullWakeLockTimeTotalUs += fullWakeTimer.getTotalTimeLocked(rawRealtimeUs, 0);
                } else {
                    i2 = 0;
                }
                Timer partialWakeTimer = wl.getWakeTime(i2);
                if (partialWakeTimer != null) {
                    partialWakeLockTimeTotalUs += partialWakeTimer.getTotalTimeLocked(rawRealtimeUs, i2);
                }
            }
        }
        long mToken = protoOutputStream.start(1146756268047L);
        protoOutputStream.write(1112396529665L, getScreenOnTime(rawRealtimeUs, 0) / 1000);
        protoOutputStream.write(1112396529666L, getPhoneOnTime(rawRealtimeUs, 0) / 1000);
        protoOutputStream.write(1112396529667L, fullWakeLockTimeTotalUs / 1000);
        protoOutputStream.write(1112396529668L, partialWakeLockTimeTotalUs / 1000);
        protoOutputStream.write(1112396529669L, getMobileRadioActiveTime(rawRealtimeUs, 0) / 1000);
        protoOutputStream.write(1112396529670L, getMobileRadioActiveAdjustedTime(0) / 1000);
        protoOutputStream.write(1120986464263L, getMobileRadioActiveCount(0));
        protoOutputStream.write(1120986464264L, getMobileRadioActiveUnknownTime(0) / 1000);
        protoOutputStream.write(1112396529673L, getInteractiveTime(rawRealtimeUs, 0) / 1000);
        protoOutputStream.write(1112396529674L, getPowerSaveModeEnabledTime(rawRealtimeUs, 0) / 1000);
        protoOutputStream.write(1120986464267L, getNumConnectivityChange(0));
        protoOutputStream.write(1112396529676L, getDeviceIdleModeTime(2, rawRealtimeUs, 0) / 1000);
        protoOutputStream.write(1120986464269L, getDeviceIdleModeCount(2, 0));
        protoOutputStream.write(1112396529678L, getDeviceIdlingTime(2, rawRealtimeUs, 0) / 1000);
        protoOutputStream.write(1120986464271L, getDeviceIdlingCount(2, 0));
        protoOutputStream.write(1112396529680L, getLongestDeviceIdleModeTime(2));
        protoOutputStream.write(1112396529681L, getDeviceIdleModeTime(i, rawRealtimeUs, 0) / 1000);
        protoOutputStream.write(1120986464274L, getDeviceIdleModeCount(i, 0));
        protoOutputStream.write(1112396529683L, getDeviceIdlingTime(i, rawRealtimeUs, 0) / 1000);
        protoOutputStream.write(1120986464276L, getDeviceIdlingCount(i, 0));
        protoOutputStream.write(1112396529685L, getLongestDeviceIdleModeTime(i));
        protoOutputStream.end(mToken);
        long multicastWakeLockTimeTotalUs2 = getWifiMulticastWakelockTime(rawRealtimeUs, 0);
        int multicastWakeLockCountTotal = getWifiMulticastWakelockCount(0);
        long wmctToken = protoOutputStream.start(1146756268055L);
        protoOutputStream.write(1112396529665L, multicastWakeLockTimeTotalUs2 / 1000);
        protoOutputStream.write(1120986464258L, multicastWakeLockCountTotal);
        protoOutputStream.end(wmctToken);
        List<BatterySipper> sippers = helper.getUsageList();
        if (sippers != null) {
            int i5 = 0;
            while (i5 < sippers.size()) {
                BatterySipper bs = sippers.get(i5);
                int n = 0;
                int uid = 0;
                uid = 0;
                uid = 0;
                uid = 0;
                uid = 0;
                uid = 0;
                uid = 0;
                uid = 0;
                uid = 0;
                uid = 0;
                uid = 0;
                uid = 0;
                uid = 0;
                switch (bs.drainType) {
                    case AMBIENT_DISPLAY:
                        n = 13;
                        break;
                    case IDLE:
                        n = 1;
                        break;
                    case CELL:
                        n = 2;
                        break;
                    case PHONE:
                        n = 3;
                        break;
                    case WIFI:
                        n = 4;
                        break;
                    case BLUETOOTH:
                        n = 5;
                        break;
                    case SCREEN:
                        n = 7;
                        break;
                    case FLASHLIGHT:
                        n = 6;
                        break;
                    case APP:
                        multicastWakeLockTimeTotalUs = multicastWakeLockTimeTotalUs2;
                        continue;
                        i5++;
                        multicastWakeLockTimeTotalUs2 = multicastWakeLockTimeTotalUs;
                        wmctToken = wmctToken;
                    case USER:
                        n = 8;
                        uid = UserHandle.getUid(bs.userId, 0);
                        break;
                    case UNACCOUNTED:
                        n = 9;
                        break;
                    case OVERCOUNTED:
                        n = 10;
                        break;
                    case CAMERA:
                        n = 11;
                        break;
                    case MEMORY:
                        n = 12;
                        break;
                }
                long puiToken = protoOutputStream.start(2246267895825L);
                multicastWakeLockTimeTotalUs = multicastWakeLockTimeTotalUs2;
                protoOutputStream.write(1159641169921L, n);
                protoOutputStream.write(1120986464258L, uid);
                protoOutputStream.write(1103806595075L, bs.totalPowerMah);
                protoOutputStream.write(1133871366148L, bs.shouldHide);
                protoOutputStream.write(1103806595077L, bs.screenPowerMah);
                protoOutputStream.write(1103806595078L, bs.proportionalSmearMah);
                protoOutputStream.end(puiToken);
                i5++;
                multicastWakeLockTimeTotalUs2 = multicastWakeLockTimeTotalUs;
                wmctToken = wmctToken;
            }
        }
        long pusToken = protoOutputStream.start(1146756268050L);
        protoOutputStream.write(1103806595073L, helper.getPowerProfile().getBatteryCapacity());
        protoOutputStream.write(1103806595074L, helper.getComputedPower());
        protoOutputStream.write(1103806595075L, helper.getMinDrainedPower());
        protoOutputStream.write(1103806595076L, helper.getMaxDrainedPower());
        protoOutputStream.end(pusToken);
        Map<String, ? extends Timer> rpmStats = getRpmStats();
        Map<String, ? extends Timer> screenOffRpmStats = getScreenOffRpmStats();
        for (Map.Entry<String, ? extends Timer> ent2 : rpmStats.entrySet()) {
            long rpmToken = protoOutputStream.start(2246267895827L);
            protoOutputStream.write(1138166333441L, ent2.getKey());
            dumpTimer(proto, 1146756268034L, (Timer) ent2.getValue(), rawRealtimeUs, 0);
            dumpTimer(proto, 1146756268035L, (Timer) screenOffRpmStats.get(ent2.getKey()), rawRealtimeUs, 0);
            protoOutputStream.end(rpmToken);
            sippers = sippers;
            screenOffRpmStats = screenOffRpmStats;
        }
        for (int i6 = 0; i6 < 5; i6++) {
            long sbToken = protoOutputStream.start(2246267895828L);
            protoOutputStream.write(1159641169921L, i6);
            dumpTimer(proto, 1146756268034L, getScreenBrightnessTimer(i6), rawRealtimeUs, 0);
            protoOutputStream.end(sbToken);
        }
        dumpTimer(proto, 1146756268053L, getPhoneSignalScanningTimer(), rawRealtimeUs, 0);
        for (int i7 = 0; i7 < 6; i7++) {
            long pssToken = protoOutputStream.start(2246267895824L);
            protoOutputStream.write(1159641169921L, i7);
            dumpTimer(proto, 1146756268034L, getPhoneSignalStrengthTimer(i7), rawRealtimeUs, 0);
            protoOutputStream.end(pssToken);
        }
        for (Map.Entry<String, ? extends Timer> ent3 : getWakeupReasonStats().entrySet()) {
            long wrToken = protoOutputStream.start(2246267895830L);
            protoOutputStream.write(1138166333441L, ent3.getKey());
            dumpTimer(proto, 1146756268034L, (Timer) ent3.getValue(), rawRealtimeUs, 0);
            protoOutputStream.end(wrToken);
        }
        for (int i8 = 0; i8 < 5; i8++) {
            long wssToken = protoOutputStream.start(2246267895832L);
            protoOutputStream.write(1159641169921L, i8);
            dumpTimer(proto, 1146756268034L, getWifiSignalStrengthTimer(i8), rawRealtimeUs, 0);
            protoOutputStream.end(wssToken);
        }
        for (int i9 = 0; i9 < 8; i9++) {
            long wsToken = protoOutputStream.start(2246267895833L);
            protoOutputStream.write(1159641169921L, i9);
            dumpTimer(proto, 1146756268034L, getWifiStateTimer(i9), rawRealtimeUs, 0);
            protoOutputStream.end(wsToken);
        }
        for (int i10 = 0; i10 < 13; i10++) {
            long wssToken2 = protoOutputStream.start(2246267895834L);
            protoOutputStream.write(1159641169921L, i10);
            dumpTimer(proto, 1146756268034L, getWifiSupplStateTimer(i10), rawRealtimeUs, 0);
            protoOutputStream.end(wssToken2);
        }
        protoOutputStream.end(sToken);
    }
}
