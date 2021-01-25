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
                    break;
                }
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
            int i2 = i + 1;
            long level = 0;
            while (true) {
                c2 = '9';
                c3 = 4;
                if (i2 < N) {
                    char c6 = value.charAt(i2);
                    if (c6 == '-') {
                        break;
                    }
                    i2++;
                    level <<= 4;
                    if (c6 >= '0' && c6 <= '9') {
                        level += (long) (c6 - '0');
                    } else if (c6 >= 'a' && c6 <= 'f') {
                        level += (long) ((c6 - 'a') + 10);
                    } else if (c6 >= 'A' && c6 <= 'F') {
                        level += (long) ((c6 - 'A') + 10);
                    }
                } else {
                    break;
                }
            }
            int i3 = i2 + 1;
            long out2 = out | ((level << 40) & BatteryStats.STEP_LEVEL_LEVEL_MASK);
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
    public final void dumpCheckinLocked(android.content.Context r202, java.io.PrintWriter r203, int r204, int r205, boolean r206) {
        /*
        // Method dump skipped, instructions count: 5158
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
    public final void dumpLocked(android.content.Context r227, java.io.PrintWriter r228, java.lang.String r229, int r230, int r231, boolean r232) {
        /*
        // Method dump skipped, instructions count: 9692
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
                        item.append(rec.batteryLevel);
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
                        item.append(rec.batteryLevel);
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

    private void dumpHistoryLocked(PrintWriter pw, int flags, long histStart, boolean checkin) {
        long baseTime;
        boolean printed;
        HistoryTag historyTag;
        boolean z;
        boolean z2;
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
                        historyTag = null;
                        hprinter.printNextItem(pw, rec, baseTime, checkin, (flags & 32) != 0);
                        rec.cmd = 0;
                    } else if (rec.currentTime != 0) {
                        printed = true;
                        byte cmd = rec.cmd;
                        rec.cmd = 5;
                        hprinter.printNextItem(pw, rec, baseTime, checkin, (flags & 32) != 0);
                        rec.cmd = cmd;
                        historyTag = null;
                    } else {
                        printed = printed2;
                        historyTag = null;
                    }
                    if (tracker != null) {
                        if (rec.cmd != 0) {
                            if ((flags & 32) != 0) {
                                z2 = true;
                            } else {
                                boolean z3 = historyTag == 1 ? 1 : 0;
                                boolean z4 = historyTag == 1 ? 1 : 0;
                                boolean z5 = historyTag == 1 ? 1 : 0;
                                boolean z6 = historyTag == 1 ? 1 : 0;
                                boolean z7 = historyTag == 1 ? 1 : 0;
                                z2 = z3;
                            }
                            hprinter.printNextItem(pw, rec, baseTime, checkin, z2);
                            byte b = historyTag == 1 ? (byte) 1 : 0;
                            byte b2 = historyTag == 1 ? (byte) 1 : 0;
                            byte b3 = historyTag == 1 ? (byte) 1 : 0;
                            byte b4 = historyTag == 1 ? (byte) 1 : 0;
                            byte b5 = historyTag == 1 ? (byte) 1 : 0;
                            rec.cmd = b;
                        }
                        int oldEventCode = rec.eventCode;
                        HistoryTag oldEventTag = rec.eventTag;
                        rec.eventTag = new HistoryTag();
                        int i = 0;
                        HistoryTag oldEventTag2 = historyTag;
                        while (i < 22) {
                            HashMap<String, SparseIntArray> active = tracker.getStateForEvent(i);
                            if (active != null) {
                                HistoryTag oldEventTag3 = oldEventTag2;
                                for (Map.Entry<String, SparseIntArray> ent : active.entrySet()) {
                                    SparseIntArray uids = ent.getValue();
                                    int j = 0;
                                    HistoryTag oldEventTag4 = oldEventTag3;
                                    while (j < uids.size()) {
                                        rec.eventCode = i;
                                        rec.eventTag.string = ent.getKey();
                                        rec.eventTag.uid = uids.keyAt(j);
                                        rec.eventTag.poolIdx = uids.valueAt(j);
                                        if ((flags & 32) != 0) {
                                            z = true;
                                        } else {
                                            boolean z8 = oldEventTag4 == 1 ? 1 : 0;
                                            boolean z9 = oldEventTag4 == 1 ? 1 : 0;
                                            boolean z10 = oldEventTag4 == 1 ? 1 : 0;
                                            boolean z11 = oldEventTag4 == 1 ? 1 : 0;
                                            z = z8;
                                        }
                                        hprinter.printNextItem(pw, rec, baseTime, checkin, z);
                                        rec.wakeReasonTag = null;
                                        rec.wakelockTag = null;
                                        j++;
                                        oldEventTag = oldEventTag;
                                        uids = uids;
                                        i = i;
                                        oldEventTag4 = null;
                                    }
                                    oldEventTag3 = null;
                                }
                            }
                            i++;
                            oldEventTag = oldEventTag;
                            oldEventTag2 = null;
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
        Throwable th;
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
                    } catch (Throwable th2) {
                        th = th2;
                        finishIteratingHistoryLocked();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
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

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0134: APUT  
      (r3v4 'lineArgs' java.lang.String[] A[D('lineArgs' java.lang.String[])])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.String : 0x0130: INVOKE  (r11v9 java.lang.String) = (r5v2 'uid' int A[D('uid' int)]) type: STATIC call: java.lang.Integer.toString(int):java.lang.String)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x01b1: APUT  
      (r0v4 'lineArgs' java.lang.String[] A[D('lineArgs' java.lang.String[])])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.String : 0x01ad: INVOKE  (r1v9 java.lang.String) = (r18v1 'timeRemaining' long A[D('timeRemaining' long)]) type: STATIC call: java.lang.Long.toString(long):java.lang.String)
     */
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
