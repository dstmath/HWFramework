package android.os;

import android.app.backup.FullBackup;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.media.MediaFile;
import android.media.ToneGenerator;
import android.net.NetworkPolicyManager;
import android.net.ProxyInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiEnterpriseConfig;
import android.nfc.tech.Ndef;
import android.provider.ContactsContract.Intents.Insert;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.provider.Settings.System;
import android.renderscript.Mesh.TriangleMeshBuilder;
import android.renderscript.ScriptIntrinsicBLAS;
import android.rms.AppAssociate;
import android.security.keymaster.KeymasterDefs;
import android.service.notification.ZenModeConfig;
import android.speech.tts.Voice;
import android.telephony.SignalStrength;
import android.text.format.DateFormat;
import android.util.ArrayMap;
import android.util.MutableBoolean;
import android.util.Pair;
import android.util.Printer;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatterySipper.DrainType;
import com.android.internal.os.BatteryStatsHelper;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class BatteryStats implements Parcelable {
    private static final /* synthetic */ int[] -com-android-internal-os-BatterySipper$DrainTypeSwitchesValues = null;
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
    private static final long BYTES_PER_GB = 1073741824;
    private static final long BYTES_PER_KB = 1024;
    private static final long BYTES_PER_MB = 1048576;
    private static final String CAMERA_DATA = "cam";
    public static final int CAMERA_TURNED_ON = 17;
    private static final String CHARGE_STEP_DATA = "csd";
    private static final String CHARGE_TIME_REMAIN_DATA = "ctr";
    static final String CHECKIN_VERSION = "18";
    private static final String CPU_DATA = "cpu";
    public static final int DATA_CONNECTION_1xRTT = 7;
    public static final int DATA_CONNECTION_CDMA = 4;
    private static final String DATA_CONNECTION_COUNT_DATA = "dcc";
    public static final int DATA_CONNECTION_EDGE = 2;
    public static final int DATA_CONNECTION_EHRPD = 14;
    public static final int DATA_CONNECTION_EVDO_0 = 5;
    public static final int DATA_CONNECTION_EVDO_A = 6;
    public static final int DATA_CONNECTION_EVDO_B = 12;
    public static final int DATA_CONNECTION_GPRS = 1;
    public static final int DATA_CONNECTION_HSDPA = 8;
    public static final int DATA_CONNECTION_HSPA = 10;
    public static final int DATA_CONNECTION_HSPAP = 15;
    public static final int DATA_CONNECTION_HSUPA = 9;
    public static final int DATA_CONNECTION_IDEN = 11;
    public static final int DATA_CONNECTION_LTE = 13;
    static final String[] DATA_CONNECTION_NAMES = null;
    public static final int DATA_CONNECTION_NONE = 0;
    public static final int DATA_CONNECTION_OTHER = 16;
    private static final String DATA_CONNECTION_TIME_DATA = "dct";
    public static final int DATA_CONNECTION_UMTS = 3;
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
    private static final String FOREGROUND_DATA = "fg";
    public static final int FULL_WIFI_LOCK = 5;
    private static final String GLOBAL_BLUETOOTH_CONTROLLER_DATA = "gble";
    private static final String GLOBAL_MODEM_CONTROLLER_DATA = "gmcd";
    private static final String GLOBAL_NETWORK_DATA = "gn";
    private static final String GLOBAL_WIFI_CONTROLLER_DATA = "gwfcd";
    private static final String GLOBAL_WIFI_DATA = "gwfl";
    private static final String HISTORY_DATA = "h";
    public static final String[] HISTORY_EVENT_CHECKIN_NAMES = null;
    public static final String[] HISTORY_EVENT_NAMES = null;
    public static final BitDescription[] HISTORY_STATE2_DESCRIPTIONS = null;
    public static final BitDescription[] HISTORY_STATE_DESCRIPTIONS = null;
    private static final String HISTORY_STRING_POOL = "hsp";
    public static final int JOB = 14;
    private static final String JOB_DATA = "jb";
    private static final String KERNEL_WAKELOCK_DATA = "kwl";
    private static final boolean LOCAL_LOGV = false;
    private static final String MISC_DATA = "m";
    private static final String MODEM_CONTROLLER_DATA = "mcd";
    public static final int NETWORK_BT_RX_DATA = 4;
    public static final int NETWORK_BT_TX_DATA = 5;
    private static final String NETWORK_DATA = "nt";
    public static final int NETWORK_MOBILE_RX_DATA = 0;
    public static final int NETWORK_MOBILE_TX_DATA = 1;
    public static final int NETWORK_WIFI_RX_DATA = 2;
    public static final int NETWORK_WIFI_TX_DATA = 3;
    public static final int NUM_DATA_CONNECTION_TYPES = 17;
    public static final int NUM_NETWORK_ACTIVITY_TYPES = 6;
    public static final int NUM_SCREEN_BRIGHTNESS_BINS = 5;
    public static final int NUM_WIFI_SIGNAL_STRENGTH_BINS = 5;
    public static final int NUM_WIFI_STATES = 8;
    public static final int NUM_WIFI_SUPPL_STATES = 13;
    private static final String POWER_USE_ITEM_DATA = "pwi";
    private static final String POWER_USE_SUMMARY_DATA = "pws";
    private static final String PROCESS_DATA = "pr";
    public static final int PROCESS_STATE = 12;
    public static final String RESULT_RECEIVER_CONTROLLER_KEY = "controller_activity";
    public static final int SCREEN_BRIGHTNESS_BRIGHT = 4;
    public static final int SCREEN_BRIGHTNESS_DARK = 0;
    private static final String SCREEN_BRIGHTNESS_DATA = "br";
    public static final int SCREEN_BRIGHTNESS_DIM = 1;
    public static final int SCREEN_BRIGHTNESS_LIGHT = 3;
    public static final int SCREEN_BRIGHTNESS_MEDIUM = 2;
    static final String[] SCREEN_BRIGHTNESS_NAMES = null;
    static final String[] SCREEN_BRIGHTNESS_SHORT_NAMES = null;
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
    private static final String[] STAT_NAMES = null;
    public static final long STEP_LEVEL_INITIAL_MODE_MASK = 71776119061217280L;
    public static final int STEP_LEVEL_INITIAL_MODE_SHIFT = 48;
    public static final long STEP_LEVEL_LEVEL_MASK = 280375465082880L;
    public static final int STEP_LEVEL_LEVEL_SHIFT = 40;
    public static final int[] STEP_LEVEL_MODES_OF_INTEREST = null;
    public static final int STEP_LEVEL_MODE_DEVICE_IDLE = 8;
    public static final String[] STEP_LEVEL_MODE_LABELS = null;
    public static final int STEP_LEVEL_MODE_POWER_SAVE = 4;
    public static final int STEP_LEVEL_MODE_SCREEN_STATE = 3;
    public static final int[] STEP_LEVEL_MODE_VALUES = null;
    public static final long STEP_LEVEL_MODIFIED_MODE_MASK = -72057594037927936L;
    public static final int STEP_LEVEL_MODIFIED_MODE_SHIFT = 56;
    public static final long STEP_LEVEL_TIME_MASK = 1099511627775L;
    public static final int SYNC = 13;
    private static final String SYNC_DATA = "sy";
    private static final String UID_DATA = "uid";
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
    public static final int WIFI_BATCHED_SCAN = 11;
    private static final String WIFI_CONTROLLER_DATA = "wfcd";
    private static final String WIFI_DATA = "wfl";
    public static final int WIFI_MULTICAST_ENABLED = 7;
    public static final int WIFI_RUNNING = 4;
    public static final int WIFI_SCAN = 6;
    private static final String WIFI_SIGNAL_STRENGTH_COUNT_DATA = "wsgc";
    private static final String WIFI_SIGNAL_STRENGTH_TIME_DATA = "wsgt";
    private static final String WIFI_STATE_COUNT_DATA = "wsc";
    static final String[] WIFI_STATE_NAMES = null;
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
    static final String[] WIFI_SUPPL_STATE_NAMES = null;
    public static final int WIFI_SUPPL_STATE_SCANNING = 4;
    static final String[] WIFI_SUPPL_STATE_SHORT_NAMES = null;
    private static final String WIFI_SUPPL_STATE_TIME_DATA = "wsst";
    public static final int WIFI_SUPPL_STATE_UNINITIALIZED = 12;
    private final StringBuilder mFormatBuilder;
    private final Formatter mFormatter;

    public static final class BitDescription {
        public final int mask;
        public final String name;
        public final int shift;
        public final String shortName;
        public final String[] shortValues;
        public final String[] values;

        public BitDescription(int mask, String name, String shortName) {
            this.mask = mask;
            this.shift = -1;
            this.name = name;
            this.shortName = shortName;
            this.values = null;
            this.shortValues = null;
        }

        public BitDescription(int mask, int shift, String name, String shortName, String[] values, String[] shortValues) {
            this.mask = mask;
            this.shift = shift;
            this.name = name;
            this.shortName = shortName;
            this.values = values;
            this.shortValues = shortValues;
        }
    }

    public static abstract class ControllerActivityCounter {
        public abstract LongCounter getIdleTimeCounter();

        public abstract LongCounter getPowerCounter();

        public abstract LongCounter getRxTimeCounter();

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
        private final HashMap<String, SparseIntArray>[] mActiveEvents;

        public HistoryEventTracker() {
            this.mActiveEvents = new HashMap[BatteryStats.BLUETOOTH_SCAN_ON];
        }

        public boolean updateState(int code, String name, int uid, int poolIdx) {
            int idx;
            HashMap<String, SparseIntArray> active;
            SparseIntArray uids;
            if ((Document.FLAG_ARCHIVE & code) != 0) {
                idx = code & HistoryItem.EVENT_TYPE_MASK;
                active = this.mActiveEvents[idx];
                if (active == null) {
                    active = new HashMap();
                    this.mActiveEvents[idx] = active;
                }
                uids = (SparseIntArray) active.get(name);
                if (uids == null) {
                    uids = new SparseIntArray();
                    active.put(name, uids);
                }
                if (uids.indexOfKey(uid) >= 0) {
                    return BatteryStats.LOCAL_LOGV;
                }
                uids.put(uid, poolIdx);
            } else if ((code & Process.PROC_OUT_FLOAT) != 0) {
                active = this.mActiveEvents[code & HistoryItem.EVENT_TYPE_MASK];
                if (active == null) {
                    return BatteryStats.LOCAL_LOGV;
                }
                uids = (SparseIntArray) active.get(name);
                if (uids == null) {
                    return BatteryStats.LOCAL_LOGV;
                }
                idx = uids.indexOfKey(uid);
                if (idx < 0) {
                    return BatteryStats.LOCAL_LOGV;
                }
                uids.removeAt(idx);
                if (uids.size() <= 0) {
                    active.remove(name);
                }
            }
            return true;
        }

        public void removeEvents(int code) {
            this.mActiveEvents[code & HistoryItem.EVENT_TYPE_MASK] = null;
        }

        public HashMap<String, SparseIntArray> getStateForEvent(int code) {
            return this.mActiveEvents[code];
        }
    }

    public static final class HistoryItem implements Parcelable {
        public static final byte CMD_CURRENT_TIME = (byte) 5;
        public static final byte CMD_NULL = (byte) -1;
        public static final byte CMD_OVERFLOW = (byte) 6;
        public static final byte CMD_RESET = (byte) 7;
        public static final byte CMD_SHUTDOWN = (byte) 8;
        public static final byte CMD_START = (byte) 4;
        public static final byte CMD_UPDATE = (byte) 0;
        public static final int EVENT_ACTIVE = 10;
        public static final int EVENT_ALARM = 13;
        public static final int EVENT_ALARM_FINISH = 16397;
        public static final int EVENT_ALARM_START = 32781;
        public static final int EVENT_COLLECT_EXTERNAL_STATS = 14;
        public static final int EVENT_CONNECTIVITY_CHANGED = 9;
        public static final int EVENT_COUNT = 19;
        public static final int EVENT_FLAG_FINISH = 16384;
        public static final int EVENT_FLAG_START = 32768;
        public static final int EVENT_FOREGROUND = 2;
        public static final int EVENT_FOREGROUND_FINISH = 16386;
        public static final int EVENT_FOREGROUND_START = 32770;
        public static final int EVENT_JOB = 6;
        public static final int EVENT_JOB_FINISH = 16390;
        public static final int EVENT_JOB_START = 32774;
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
        public static final int EVENT_WAKE_LOCK = 5;
        public static final int EVENT_WAKE_LOCK_FINISH = 16389;
        public static final int EVENT_WAKE_LOCK_START = 32773;
        public static final int MOST_INTERESTING_STATES = 1572864;
        public static final int MOST_INTERESTING_STATES2 = -1749024768;
        public static final int SETTLE_TO_ZERO_STATES = -1638400;
        public static final int SETTLE_TO_ZERO_STATES2 = 1748959232;
        public static final int STATE2_BLUETOOTH_ON_FLAG = 4194304;
        public static final int STATE2_BLUETOOTH_SCAN_FLAG = 1048576;
        public static final int STATE2_CAMERA_FLAG = 2097152;
        public static final int STATE2_CHARGING_FLAG = 16777216;
        public static final int STATE2_DEVICE_IDLE_MASK = 100663296;
        public static final int STATE2_DEVICE_IDLE_SHIFT = 25;
        public static final int STATE2_FLASHLIGHT_FLAG = 134217728;
        public static final int STATE2_PHONE_IN_CALL_FLAG = 8388608;
        public static final int STATE2_POWER_SAVE_FLAG = Integer.MIN_VALUE;
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
        public byte cmd;
        public long currentTime;
        public int eventCode;
        public HistoryTag eventTag;
        public final HistoryTag localEventTag;
        public final HistoryTag localWakeReasonTag;
        public final HistoryTag localWakelockTag;
        public HistoryItem next;
        public int numReadInts;
        public int states;
        public int states2;
        public HistoryStepDetails stepDetails;
        public long time;
        public HistoryTag wakeReasonTag;
        public HistoryTag wakelockTag;

        public boolean isDeltaData() {
            return this.cmd == null ? true : BatteryStats.LOCAL_LOGV;
        }

        public HistoryItem() {
            this.cmd = CMD_NULL;
            this.localWakelockTag = new HistoryTag();
            this.localWakeReasonTag = new HistoryTag();
            this.localEventTag = new HistoryTag();
        }

        public HistoryItem(long time, Parcel src) {
            this.cmd = CMD_NULL;
            this.localWakelockTag = new HistoryTag();
            this.localWakeReasonTag = new HistoryTag();
            this.localEventTag = new HistoryTag();
            this.time = time;
            this.numReadInts = EVENT_FOREGROUND;
            readFromParcel(src);
        }

        public int describeContents() {
            return STATE_BRIGHTNESS_SHIFT;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            int i2 = STATE_BRIGHTNESS_SHIFT;
            dest.writeLong(this.time);
            int i3 = ((this.batteryPlugType << 24) & 251658240) | ((((this.cmd & Process.PROC_TERM_MASK) | ((this.batteryLevel << EVENT_USER_FOREGROUND) & 65280)) | ((this.batteryStatus << EVENT_PACKAGE_ACTIVE) & 983040)) | ((this.batteryHealth << 20) & 15728640));
            if (this.wakelockTag != null) {
                i = STATE_WIFI_FULL_LOCK_FLAG;
            } else {
                i = STATE_BRIGHTNESS_SHIFT;
            }
            i3 |= i;
            if (this.wakeReasonTag != null) {
                i = STATE_GPS_ON_FLAG;
            } else {
                i = STATE_BRIGHTNESS_SHIFT;
            }
            i |= i3;
            if (this.eventCode != 0) {
                i2 = STATE_WAKE_LOCK_FLAG;
            }
            dest.writeInt(i | i2);
            dest.writeInt((this.batteryTemperature & PowerManager.WAKE_LOCK_LEVEL_MASK) | ((this.batteryVoltage << EVENT_PACKAGE_ACTIVE) & Color.RED));
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
            if (this.cmd == EVENT_WAKE_LOCK || this.cmd == STATE_BRIGHTNESS_MASK) {
                dest.writeLong(this.currentTime);
            }
        }

        public void readFromParcel(Parcel src) {
            int start = src.dataPosition();
            int bat = src.readInt();
            this.cmd = (byte) (bat & Process.PROC_TERM_MASK);
            this.batteryLevel = (byte) ((bat >> EVENT_USER_FOREGROUND) & Process.PROC_TERM_MASK);
            this.batteryStatus = (byte) ((bat >> EVENT_PACKAGE_ACTIVE) & STATE2_WIFI_SUPPL_STATE_MASK);
            this.batteryHealth = (byte) ((bat >> 20) & STATE2_WIFI_SUPPL_STATE_MASK);
            this.batteryPlugType = (byte) ((bat >> 24) & STATE2_WIFI_SUPPL_STATE_MASK);
            int bat2 = src.readInt();
            this.batteryTemperature = (short) (bat2 & PowerManager.WAKE_LOCK_LEVEL_MASK);
            this.batteryVoltage = (char) ((bat2 >> EVENT_PACKAGE_ACTIVE) & PowerManager.WAKE_LOCK_LEVEL_MASK);
            this.batteryChargeUAh = src.readInt();
            this.states = src.readInt();
            this.states2 = src.readInt();
            if ((STATE_WIFI_FULL_LOCK_FLAG & bat) != 0) {
                this.wakelockTag = this.localWakelockTag;
                this.wakelockTag.readFromParcel(src);
            } else {
                this.wakelockTag = null;
            }
            if ((STATE_GPS_ON_FLAG & bat) != 0) {
                this.wakeReasonTag = this.localWakeReasonTag;
                this.wakeReasonTag.readFromParcel(src);
            } else {
                this.wakeReasonTag = null;
            }
            if ((STATE_WAKE_LOCK_FLAG & bat) != 0) {
                this.eventCode = src.readInt();
                this.eventTag = this.localEventTag;
                this.eventTag.readFromParcel(src);
            } else {
                this.eventCode = STATE_BRIGHTNESS_SHIFT;
                this.eventTag = null;
            }
            if (this.cmd == EVENT_WAKE_LOCK || this.cmd == STATE_BRIGHTNESS_MASK) {
                this.currentTime = src.readLong();
            } else {
                this.currentTime = 0;
            }
            this.numReadInts += (src.dataPosition() - start) / STATE2_WIFI_SIGNAL_STRENGTH_SHIFT;
        }

        public void clear() {
            this.time = 0;
            this.cmd = CMD_NULL;
            this.batteryLevel = CMD_UPDATE;
            this.batteryStatus = CMD_UPDATE;
            this.batteryHealth = CMD_UPDATE;
            this.batteryPlugType = CMD_UPDATE;
            this.batteryTemperature = (short) 0;
            this.batteryVoltage = '\u0000';
            this.batteryChargeUAh = STATE_BRIGHTNESS_SHIFT;
            this.states = STATE_BRIGHTNESS_SHIFT;
            this.states2 = STATE_BRIGHTNESS_SHIFT;
            this.wakelockTag = null;
            this.wakeReasonTag = null;
            this.eventCode = STATE_BRIGHTNESS_SHIFT;
            this.eventTag = null;
        }

        public void setTo(HistoryItem o) {
            this.time = o.time;
            this.cmd = o.cmd;
            setToCommon(o);
        }

        public void setTo(long time, byte cmd, HistoryItem o) {
            this.time = time;
            this.cmd = cmd;
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
            return (this.batteryLevel == o.batteryLevel && this.batteryStatus == o.batteryStatus && this.batteryHealth == o.batteryHealth && this.batteryPlugType == o.batteryPlugType && this.batteryTemperature == o.batteryTemperature && this.batteryVoltage == o.batteryVoltage && this.batteryChargeUAh == o.batteryChargeUAh && this.states == o.states && this.states2 == o.states2 && this.currentTime == o.currentTime) ? true : BatteryStats.LOCAL_LOGV;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean same(HistoryItem o) {
            if (!sameNonEvent(o) || this.eventCode != o.eventCode) {
                return BatteryStats.LOCAL_LOGV;
            }
            if (this.wakelockTag != o.wakelockTag && (this.wakelockTag == null || o.wakelockTag == null || !this.wakelockTag.equals(o.wakelockTag))) {
                return BatteryStats.LOCAL_LOGV;
            }
            if (this.wakeReasonTag != o.wakeReasonTag && (this.wakeReasonTag == null || o.wakeReasonTag == null || !this.wakeReasonTag.equals(o.wakeReasonTag))) {
                return BatteryStats.LOCAL_LOGV;
            }
            if (this.eventTag == o.eventTag || (this.eventTag != null && o.eventTag != null && this.eventTag.equals(o.eventTag))) {
                return true;
            }
            return BatteryStats.LOCAL_LOGV;
        }
    }

    public static class HistoryPrinter {
        long lastTime;
        int oldChargeMAh;
        int oldHealth;
        int oldLevel;
        int oldPlug;
        int oldState;
        int oldState2;
        int oldStatus;
        int oldTemp;
        int oldVolt;

        public HistoryPrinter() {
            this.oldState = BatteryStats.WIFI_SUPPL_STATE_INVALID;
            this.oldState2 = BatteryStats.WIFI_SUPPL_STATE_INVALID;
            this.oldLevel = -1;
            this.oldStatus = -1;
            this.oldHealth = -1;
            this.oldPlug = -1;
            this.oldTemp = -1;
            this.oldVolt = -1;
            this.oldChargeMAh = -1;
            this.lastTime = -1;
        }

        void reset() {
            this.oldState2 = BatteryStats.WIFI_SUPPL_STATE_INVALID;
            this.oldState = BatteryStats.WIFI_SUPPL_STATE_INVALID;
            this.oldLevel = -1;
            this.oldStatus = -1;
            this.oldHealth = -1;
            this.oldPlug = -1;
            this.oldTemp = -1;
            this.oldVolt = -1;
            this.oldChargeMAh = -1;
        }

        public void printNextItem(PrintWriter pw, HistoryItem rec, long baseTime, boolean checkin, boolean verbose) {
            if (checkin) {
                pw.print(BatteryStats.WIFI_SUPPL_STATE_GROUP_HANDSHAKE);
                pw.print(',');
                pw.print(BatteryStats.HISTORY_DATA);
                pw.print(',');
                if (this.lastTime < 0) {
                    pw.print(rec.time - baseTime);
                } else {
                    pw.print(rec.time - this.lastTime);
                }
                this.lastTime = rec.time;
            } else {
                pw.print("  ");
                TimeUtils.formatDuration(rec.time - baseTime, pw, BatteryStats.BLUETOOTH_SCAN_ON);
                pw.print(" (");
                pw.print(rec.numReadInts);
                pw.print(") ");
            }
            if (rec.cmd == BatteryStats.WIFI_SUPPL_STATE_SCANNING) {
                if (checkin) {
                    pw.print(":");
                }
                pw.println("START");
                reset();
            } else if (rec.cmd == BatteryStats.WIFI_SUPPL_STATE_AUTHENTICATING || rec.cmd == BatteryStats.WIFI_SUPPL_STATE_ASSOCIATED) {
                if (checkin) {
                    pw.print(":");
                }
                if (rec.cmd == BatteryStats.WIFI_SUPPL_STATE_ASSOCIATED) {
                    pw.print("RESET:");
                    reset();
                }
                pw.print("TIME:");
                if (checkin) {
                    pw.println(rec.currentTime);
                    return;
                }
                pw.print(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                pw.println(DateFormat.format("yyyy-MM-dd-HH-mm-ss", rec.currentTime).toString());
            } else if (rec.cmd == BatteryStats.WIFI_SUPPL_STATE_FOUR_WAY_HANDSHAKE) {
                if (checkin) {
                    pw.print(":");
                }
                pw.println("SHUTDOWN");
            } else if (rec.cmd == BatteryStats.WIFI_SUPPL_STATE_ASSOCIATING) {
                if (checkin) {
                    pw.print(":");
                }
                pw.println("*OVERFLOW*");
            } else {
                if (!checkin) {
                    if (rec.batteryLevel < BatteryStats.WIFI_SUPPL_STATE_COMPLETED) {
                        pw.print("00");
                    } else if (rec.batteryLevel < 100) {
                        pw.print(WifiEnterpriseConfig.ENGINE_DISABLE);
                    }
                    pw.print(rec.batteryLevel);
                    if (verbose) {
                        pw.print(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                        if (rec.states >= 0) {
                            if (rec.states < BatteryStats.FLASHLIGHT_TURNED_ON) {
                                pw.print("0000000");
                            } else if (rec.states < TriangleMeshBuilder.TEXTURE_0) {
                                pw.print("000000");
                            } else if (rec.states < StrictMode.DETECT_VM_REGISTRATION_LEAKS) {
                                pw.print("00000");
                            } else if (rec.states < Root.FLAG_EMPTY) {
                                pw.print("0000");
                            } else if (rec.states < Root.FLAG_REMOVABLE_USB) {
                                pw.print("000");
                            } else if (rec.states < StrictMode.PENALTY_DEATH_ON_NETWORK) {
                                pw.print("00");
                            } else if (rec.states < KeymasterDefs.KM_ENUM) {
                                pw.print(WifiEnterpriseConfig.ENGINE_DISABLE);
                            }
                        }
                        pw.print(Integer.toHexString(rec.states));
                    }
                } else if (this.oldLevel != rec.batteryLevel) {
                    this.oldLevel = rec.batteryLevel;
                    pw.print(",Bl=");
                    pw.print(rec.batteryLevel);
                }
                if (this.oldStatus != rec.batteryStatus) {
                    this.oldStatus = rec.batteryStatus;
                    pw.print(checkin ? ",Bs=" : " status=");
                    switch (this.oldStatus) {
                        case BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED /*1*/:
                            pw.print(checkin ? "?" : Environment.MEDIA_UNKNOWN);
                            break;
                        case BatteryStats.WIFI_SUPPL_STATE_INTERFACE_DISABLED /*2*/:
                            pw.print(checkin ? FullBackup.CACHE_TREE_TOKEN : "charging");
                            break;
                        case BatteryStats.WIFI_SUPPL_STATE_INACTIVE /*3*/:
                            pw.print(checkin ? "d" : "discharging");
                            break;
                        case BatteryStats.WIFI_SUPPL_STATE_SCANNING /*4*/:
                            pw.print(checkin ? "n" : "not-charging");
                            break;
                        case BatteryStats.WIFI_SUPPL_STATE_AUTHENTICATING /*5*/:
                            pw.print(checkin ? FullBackup.FILES_TREE_TOKEN : "full");
                            break;
                        default:
                            pw.print(this.oldStatus);
                            break;
                    }
                }
                if (this.oldHealth != rec.batteryHealth) {
                    this.oldHealth = rec.batteryHealth;
                    pw.print(checkin ? ",Bh=" : " health=");
                    switch (this.oldHealth) {
                        case BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED /*1*/:
                            pw.print(checkin ? "?" : Environment.MEDIA_UNKNOWN);
                            break;
                        case BatteryStats.WIFI_SUPPL_STATE_INTERFACE_DISABLED /*2*/:
                            pw.print(checkin ? "g" : "good");
                            break;
                        case BatteryStats.WIFI_SUPPL_STATE_INACTIVE /*3*/:
                            pw.print(checkin ? BatteryStats.HISTORY_DATA : "overheat");
                            break;
                        case BatteryStats.WIFI_SUPPL_STATE_SCANNING /*4*/:
                            pw.print(checkin ? "d" : "dead");
                            break;
                        case BatteryStats.WIFI_SUPPL_STATE_AUTHENTICATING /*5*/:
                            pw.print(checkin ? "v" : "over-voltage");
                            break;
                        case BatteryStats.WIFI_SUPPL_STATE_ASSOCIATING /*6*/:
                            pw.print(checkin ? FullBackup.FILES_TREE_TOKEN : "failure");
                            break;
                        case BatteryStats.WIFI_SUPPL_STATE_ASSOCIATED /*7*/:
                            pw.print(checkin ? FullBackup.CACHE_TREE_TOKEN : "cold");
                            break;
                        default:
                            pw.print(this.oldHealth);
                            break;
                    }
                }
                if (this.oldPlug != rec.batteryPlugType) {
                    this.oldPlug = rec.batteryPlugType;
                    pw.print(checkin ? ",Bp=" : " plug=");
                    switch (this.oldPlug) {
                        case BatteryStats.WIFI_SUPPL_STATE_INVALID /*0*/:
                            pw.print(checkin ? "n" : NetworkPolicyManager.FIREWALL_CHAIN_NAME_NONE);
                            break;
                        case BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED /*1*/:
                            pw.print(checkin ? FullBackup.APK_TREE_TOKEN : "ac");
                            break;
                        case BatteryStats.WIFI_SUPPL_STATE_INTERFACE_DISABLED /*2*/:
                            pw.print(checkin ? "u" : Context.USB_SERVICE);
                            break;
                        case BatteryStats.WIFI_SUPPL_STATE_SCANNING /*4*/:
                            pw.print(checkin ? "w" : "wireless");
                            break;
                        default:
                            pw.print(this.oldPlug);
                            break;
                    }
                }
                if (this.oldTemp != rec.batteryTemperature) {
                    this.oldTemp = rec.batteryTemperature;
                    pw.print(checkin ? ",Bt=" : " temp=");
                    pw.print(this.oldTemp);
                }
                if (this.oldVolt != rec.batteryVoltage) {
                    this.oldVolt = rec.batteryVoltage;
                    pw.print(checkin ? ",Bv=" : " volt=");
                    pw.print(this.oldVolt);
                }
                int chargeMAh = rec.batteryChargeUAh / Process.SYSTEM_UID;
                if (this.oldChargeMAh != chargeMAh) {
                    this.oldChargeMAh = chargeMAh;
                    pw.print(checkin ? ",Bcc=" : " charge=");
                    pw.print(this.oldChargeMAh);
                }
                BatteryStats.printBitDescriptions(pw, this.oldState, rec.states, rec.wakelockTag, BatteryStats.HISTORY_STATE_DESCRIPTIONS, checkin ? BatteryStats.LOCAL_LOGV : true);
                BatteryStats.printBitDescriptions(pw, this.oldState2, rec.states2, null, BatteryStats.HISTORY_STATE2_DESCRIPTIONS, checkin ? BatteryStats.LOCAL_LOGV : true);
                if (rec.wakeReasonTag != null) {
                    if (checkin) {
                        pw.print(",wr=");
                        pw.print(rec.wakeReasonTag.poolIdx);
                    } else {
                        pw.print(" wake_reason=");
                        pw.print(rec.wakeReasonTag.uid);
                        pw.print(":\"");
                        pw.print(rec.wakeReasonTag.string);
                        pw.print("\"");
                    }
                }
                if (rec.eventCode != 0) {
                    String[] eventNames;
                    pw.print(checkin ? "," : WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                    if ((rec.eventCode & Document.FLAG_ARCHIVE) != 0) {
                        pw.print("+");
                    } else if ((rec.eventCode & Process.PROC_OUT_FLOAT) != 0) {
                        pw.print("-");
                    }
                    if (checkin) {
                        eventNames = BatteryStats.HISTORY_EVENT_CHECKIN_NAMES;
                    } else {
                        eventNames = BatteryStats.HISTORY_EVENT_NAMES;
                    }
                    int idx = rec.eventCode & HistoryItem.EVENT_TYPE_MASK;
                    if (idx < 0 || idx >= eventNames.length) {
                        pw.print(checkin ? "Ev" : ZenModeConfig.EVENT_PATH);
                        pw.print(idx);
                    } else {
                        pw.print(eventNames[idx]);
                    }
                    pw.print("=");
                    if (checkin) {
                        pw.print(rec.eventTag.poolIdx);
                    } else {
                        UserHandle.formatUid(pw, rec.eventTag.uid);
                        pw.print(":\"");
                        pw.print(rec.eventTag.string);
                        pw.print("\"");
                    }
                }
                pw.println();
                if (rec.stepDetails != null) {
                    if (checkin) {
                        pw.print(BatteryStats.WIFI_SUPPL_STATE_GROUP_HANDSHAKE);
                        pw.print(',');
                        pw.print(BatteryStats.HISTORY_DATA);
                        pw.print(",0,Dcpu=");
                        pw.print(rec.stepDetails.userTime);
                        pw.print(":");
                        pw.print(rec.stepDetails.systemTime);
                        if (rec.stepDetails.appCpuUid1 >= 0) {
                            printStepCpuUidCheckinDetails(pw, rec.stepDetails.appCpuUid1, rec.stepDetails.appCpuUTime1, rec.stepDetails.appCpuSTime1);
                            if (rec.stepDetails.appCpuUid2 >= 0) {
                                printStepCpuUidCheckinDetails(pw, rec.stepDetails.appCpuUid2, rec.stepDetails.appCpuUTime2, rec.stepDetails.appCpuSTime2);
                            }
                            if (rec.stepDetails.appCpuUid3 >= 0) {
                                printStepCpuUidCheckinDetails(pw, rec.stepDetails.appCpuUid3, rec.stepDetails.appCpuUTime3, rec.stepDetails.appCpuSTime3);
                            }
                        }
                        pw.println();
                        pw.print(BatteryStats.WIFI_SUPPL_STATE_GROUP_HANDSHAKE);
                        pw.print(',');
                        pw.print(BatteryStats.HISTORY_DATA);
                        pw.print(",0,Dpst=");
                        pw.print(rec.stepDetails.statUserTime);
                        pw.print(',');
                        pw.print(rec.stepDetails.statSystemTime);
                        pw.print(',');
                        pw.print(rec.stepDetails.statIOWaitTime);
                        pw.print(',');
                        pw.print(rec.stepDetails.statIrqTime);
                        pw.print(',');
                        pw.print(rec.stepDetails.statSoftIrqTime);
                        pw.print(',');
                        pw.print(rec.stepDetails.statIdlTime);
                        pw.print(',');
                        pw.print(rec.stepDetails.statPlatformIdleState);
                        pw.println();
                    } else {
                        pw.print("                 Details: cpu=");
                        pw.print(rec.stepDetails.userTime);
                        pw.print("u+");
                        pw.print(rec.stepDetails.systemTime);
                        pw.print("s");
                        if (rec.stepDetails.appCpuUid1 >= 0) {
                            pw.print(" (");
                            printStepCpuUidDetails(pw, rec.stepDetails.appCpuUid1, rec.stepDetails.appCpuUTime1, rec.stepDetails.appCpuSTime1);
                            if (rec.stepDetails.appCpuUid2 >= 0) {
                                pw.print(", ");
                                printStepCpuUidDetails(pw, rec.stepDetails.appCpuUid2, rec.stepDetails.appCpuUTime2, rec.stepDetails.appCpuSTime2);
                            }
                            if (rec.stepDetails.appCpuUid3 >= 0) {
                                pw.print(", ");
                                printStepCpuUidDetails(pw, rec.stepDetails.appCpuUid3, rec.stepDetails.appCpuUTime3, rec.stepDetails.appCpuSTime3);
                            }
                            pw.print(')');
                        }
                        pw.println();
                        pw.print("                          /proc/stat=");
                        pw.print(rec.stepDetails.statUserTime);
                        pw.print(" usr, ");
                        pw.print(rec.stepDetails.statSystemTime);
                        pw.print(" sys, ");
                        pw.print(rec.stepDetails.statIOWaitTime);
                        pw.print(" io, ");
                        pw.print(rec.stepDetails.statIrqTime);
                        pw.print(" irq, ");
                        pw.print(rec.stepDetails.statSoftIrqTime);
                        pw.print(" sirq, ");
                        pw.print(rec.stepDetails.statIdlTime);
                        pw.print(" idle");
                        int totalRun = (((rec.stepDetails.statUserTime + rec.stepDetails.statSystemTime) + rec.stepDetails.statIOWaitTime) + rec.stepDetails.statIrqTime) + rec.stepDetails.statSoftIrqTime;
                        int total = totalRun + rec.stepDetails.statIdlTime;
                        if (total > 0) {
                            pw.print(" (");
                            Object[] objArr = new Object[BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED];
                            objArr[BatteryStats.WIFI_SUPPL_STATE_INVALID] = Float.valueOf((((float) totalRun) / ((float) total)) * SensorManager.LIGHT_CLOUDY);
                            pw.print(String.format("%.1f%%", objArr));
                            pw.print(" of ");
                            StringBuilder sb = new StringBuilder(BatteryStats.DUMP_DEVICE_WIFI_ONLY);
                            BatteryStats.formatTimeMsNoSpace(sb, (long) (total * BatteryStats.WIFI_SUPPL_STATE_COMPLETED));
                            pw.print(sb);
                            pw.print(")");
                        }
                        pw.print(", PlatformIdleStat ");
                        pw.print(rec.stepDetails.statPlatformIdleState);
                        pw.println();
                    }
                }
                this.oldState = rec.states;
                this.oldState2 = rec.states2;
            }
        }

        private void printStepCpuUidDetails(PrintWriter pw, int uid, int utime, int stime) {
            UserHandle.formatUid(pw, uid);
            pw.print("=");
            pw.print(utime);
            pw.print("u+");
            pw.print(stime);
            pw.print("s");
        }

        private void printStepCpuUidCheckinDetails(PrintWriter pw, int uid, int utime, int stime) {
            pw.print('/');
            pw.print(uid);
            pw.print(":");
            pw.print(utime);
            pw.print(":");
            pw.print(stime);
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
        public int statSystemTime;
        public int statUserTime;
        public int systemTime;
        public int userTime;

        public HistoryStepDetails() {
            clear();
        }

        public void clear() {
            this.systemTime = BatteryStats.WIFI_SUPPL_STATE_INVALID;
            this.userTime = BatteryStats.WIFI_SUPPL_STATE_INVALID;
            this.appCpuUid3 = -1;
            this.appCpuUid2 = -1;
            this.appCpuUid1 = -1;
            this.appCpuSTime3 = BatteryStats.WIFI_SUPPL_STATE_INVALID;
            this.appCpuUTime3 = BatteryStats.WIFI_SUPPL_STATE_INVALID;
            this.appCpuSTime2 = BatteryStats.WIFI_SUPPL_STATE_INVALID;
            this.appCpuUTime2 = BatteryStats.WIFI_SUPPL_STATE_INVALID;
            this.appCpuSTime1 = BatteryStats.WIFI_SUPPL_STATE_INVALID;
            this.appCpuUTime1 = BatteryStats.WIFI_SUPPL_STATE_INVALID;
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
        }
    }

    public static final class HistoryTag {
        public int poolIdx;
        public String string;
        public int uid;

        public void setTo(HistoryTag o) {
            this.string = o.string;
            this.uid = o.uid;
            this.poolIdx = o.poolIdx;
        }

        public void setTo(String _string, int _uid) {
            this.string = _string;
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
                return BatteryStats.LOCAL_LOGV;
            }
            HistoryTag that = (HistoryTag) o;
            return (this.uid == that.uid && this.string.equals(that.string)) ? true : BatteryStats.LOCAL_LOGV;
        }

        public int hashCode() {
            return (this.string.hashCode() * 31) + this.uid;
        }
    }

    public static final class LevelStepTracker {
        public long mLastStepTime;
        public int mNumStepDurations;
        public final long[] mStepDurations;

        public LevelStepTracker(int maxLevelSteps) {
            this.mLastStepTime = -1;
            this.mStepDurations = new long[maxLevelSteps];
        }

        public LevelStepTracker(int numSteps, long[] steps) {
            this.mLastStepTime = -1;
            this.mNumStepDurations = numSteps;
            this.mStepDurations = new long[numSteps];
            System.arraycopy(steps, BatteryStats.WIFI_SUPPL_STATE_INVALID, this.mStepDurations, BatteryStats.WIFI_SUPPL_STATE_INVALID, numSteps);
        }

        public long getDurationAt(int index) {
            return this.mStepDurations[index] & BatteryStats.STEP_LEVEL_TIME_MASK;
        }

        public int getLevelAt(int index) {
            return (int) ((this.mStepDurations[index] & BatteryStats.STEP_LEVEL_LEVEL_MASK) >> BatteryStats.STEP_LEVEL_LEVEL_SHIFT);
        }

        public int getInitModeAt(int index) {
            return (int) ((this.mStepDurations[index] & BatteryStats.STEP_LEVEL_INITIAL_MODE_MASK) >> BatteryStats.STEP_LEVEL_INITIAL_MODE_SHIFT);
        }

        public int getModModeAt(int index) {
            return (int) ((this.mStepDurations[index] & BatteryStats.STEP_LEVEL_MODIFIED_MODE_MASK) >> BatteryStats.STEP_LEVEL_MODIFIED_MODE_SHIFT);
        }

        private void appendHex(long val, int topOffset, StringBuilder out) {
            boolean hasData = BatteryStats.LOCAL_LOGV;
            while (topOffset >= 0) {
                int digit = (int) ((val >> topOffset) & 15);
                topOffset -= 4;
                if (hasData || digit != 0) {
                    hasData = true;
                    if (digit < 0 || digit > BatteryStats.WIFI_SUPPL_STATE_GROUP_HANDSHAKE) {
                        out.append((char) ((digit + 97) - 10));
                    } else {
                        out.append((char) (digit + BatteryStats.STEP_LEVEL_INITIAL_MODE_SHIFT));
                    }
                }
            }
        }

        public void encodeEntryAt(int index, StringBuilder out) {
            long item = this.mStepDurations[index];
            long duration = item & BatteryStats.STEP_LEVEL_TIME_MASK;
            int level = (int) ((BatteryStats.STEP_LEVEL_LEVEL_MASK & item) >> BatteryStats.STEP_LEVEL_LEVEL_SHIFT);
            int initMode = (int) ((BatteryStats.STEP_LEVEL_INITIAL_MODE_MASK & item) >> BatteryStats.STEP_LEVEL_INITIAL_MODE_SHIFT);
            int modMode = (int) ((BatteryStats.STEP_LEVEL_MODIFIED_MODE_MASK & item) >> BatteryStats.STEP_LEVEL_MODIFIED_MODE_SHIFT);
            switch ((initMode & BatteryStats.WIFI_SUPPL_STATE_INACTIVE) + BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED) {
                case BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED /*1*/:
                    out.append('f');
                    break;
                case BatteryStats.WIFI_SUPPL_STATE_INTERFACE_DISABLED /*2*/:
                    out.append('o');
                    break;
                case BatteryStats.WIFI_SUPPL_STATE_INACTIVE /*3*/:
                    out.append('d');
                    break;
                case BatteryStats.WIFI_SUPPL_STATE_SCANNING /*4*/:
                    out.append('z');
                    break;
            }
            if ((initMode & BatteryStats.WIFI_SUPPL_STATE_SCANNING) != 0) {
                out.append('p');
            }
            if ((initMode & BatteryStats.WIFI_SUPPL_STATE_FOUR_WAY_HANDSHAKE) != 0) {
                out.append('i');
            }
            switch ((modMode & BatteryStats.WIFI_SUPPL_STATE_INACTIVE) + BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED) {
                case BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED /*1*/:
                    out.append('F');
                    break;
                case BatteryStats.WIFI_SUPPL_STATE_INTERFACE_DISABLED /*2*/:
                    out.append('O');
                    break;
                case BatteryStats.WIFI_SUPPL_STATE_INACTIVE /*3*/:
                    out.append('D');
                    break;
                case BatteryStats.WIFI_SUPPL_STATE_SCANNING /*4*/:
                    out.append('Z');
                    break;
            }
            if ((modMode & BatteryStats.WIFI_SUPPL_STATE_SCANNING) != 0) {
                out.append('P');
            }
            if ((modMode & BatteryStats.WIFI_SUPPL_STATE_FOUR_WAY_HANDSHAKE) != 0) {
                out.append('I');
            }
            out.append('-');
            appendHex((long) level, BatteryStats.WIFI_SUPPL_STATE_SCANNING, out);
            out.append('-');
            appendHex(duration, 36, out);
        }

        public void decodeEntryAt(int index, String value) {
            long level;
            long duration;
            int N = value.length();
            int i = BatteryStats.WIFI_SUPPL_STATE_INVALID;
            long out = 0;
            while (i < N) {
                char c = value.charAt(i);
                if (c != '-') {
                    i += BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED;
                    switch (c) {
                        case ToneGenerator.TONE_CDMA_HIGH_S_X4 /*68*/:
                            out |= 144115188075855872L;
                            break;
                        case ToneGenerator.TONE_CDMA_LOW_S_X4 /*70*/:
                            out |= 0;
                            break;
                        case ToneGenerator.TONE_CDMA_LOW_PBX_L /*73*/:
                            out |= 576460752303423488L;
                            break;
                        case ToneGenerator.TONE_CDMA_LOW_PBX_SSL /*79*/:
                            out |= 72057594037927936L;
                            break;
                        case ToneGenerator.TONE_CDMA_HIGH_PBX_SLS /*80*/:
                            out |= 288230376151711744L;
                            break;
                        case ToneGenerator.TONE_CDMA_PRESSHOLDKEY_LITE /*90*/:
                            out |= 216172782113783808L;
                            break;
                        case Voice.QUALITY_VERY_LOW /*100*/:
                            out |= 562949953421312L;
                            break;
                        case Ndef.TYPE_ICODE_SLI /*102*/:
                            out |= 0;
                            break;
                        case MediaFile.FILE_TYPE_MS_EXCEL /*105*/:
                            out |= 2251799813685248L;
                            break;
                        case ScriptIntrinsicBLAS.NO_TRANSPOSE /*111*/:
                            out |= 281474976710656L;
                            break;
                        case ScriptIntrinsicBLAS.TRANSPOSE /*112*/:
                            out |= TrafficStats.PB_IN_BYTES;
                            break;
                        case ScriptIntrinsicBLAS.LOWER /*122*/:
                            out |= 844424930131968L;
                            break;
                        default:
                            break;
                    }
                }
                i += BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED;
                level = 0;
                while (i < N) {
                    c = value.charAt(i);
                    if (c == '-') {
                        i += BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED;
                        level <<= BatteryStats.WIFI_SUPPL_STATE_SCANNING;
                        if (c < '0' && c <= '9') {
                            level += (long) (c - 48);
                        } else if (c < 'a' && c <= 'f') {
                            level += (long) ((c - 97) + BatteryStats.WIFI_SUPPL_STATE_COMPLETED);
                        } else if (c >= 'A' && c <= 'F') {
                            level += (long) ((c - 65) + BatteryStats.WIFI_SUPPL_STATE_COMPLETED);
                        }
                    } else {
                        i += BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED;
                        out |= (level << BatteryStats.STEP_LEVEL_LEVEL_SHIFT) & BatteryStats.STEP_LEVEL_LEVEL_MASK;
                        duration = 0;
                        while (i < N) {
                            c = value.charAt(i);
                            if (c == '-') {
                                i += BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED;
                                duration <<= BatteryStats.WIFI_SUPPL_STATE_SCANNING;
                                if (c < '0' && c <= '9') {
                                    duration += (long) (c - 48);
                                } else if (c < 'a' && c <= 'f') {
                                    duration += (long) ((c - 97) + BatteryStats.WIFI_SUPPL_STATE_COMPLETED);
                                } else if (c >= 'A' && c <= 'F') {
                                    duration += (long) ((c - 65) + BatteryStats.WIFI_SUPPL_STATE_COMPLETED);
                                }
                            } else {
                                this.mStepDurations[index] = (BatteryStats.STEP_LEVEL_TIME_MASK & duration) | out;
                            }
                        }
                        this.mStepDurations[index] = (BatteryStats.STEP_LEVEL_TIME_MASK & duration) | out;
                    }
                }
                i += BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED;
                out |= (level << BatteryStats.STEP_LEVEL_LEVEL_SHIFT) & BatteryStats.STEP_LEVEL_LEVEL_MASK;
                duration = 0;
                while (i < N) {
                    c = value.charAt(i);
                    if (c == '-') {
                        this.mStepDurations[index] = (BatteryStats.STEP_LEVEL_TIME_MASK & duration) | out;
                    }
                    i += BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED;
                    duration <<= BatteryStats.WIFI_SUPPL_STATE_SCANNING;
                    if (c < '0') {
                    }
                    if (c < 'a') {
                    }
                    duration += (long) ((c - 65) + BatteryStats.WIFI_SUPPL_STATE_COMPLETED);
                }
                this.mStepDurations[index] = (BatteryStats.STEP_LEVEL_TIME_MASK & duration) | out;
            }
            i += BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED;
            level = 0;
            while (i < N) {
                c = value.charAt(i);
                if (c == '-') {
                    i += BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED;
                    out |= (level << BatteryStats.STEP_LEVEL_LEVEL_SHIFT) & BatteryStats.STEP_LEVEL_LEVEL_MASK;
                    duration = 0;
                    while (i < N) {
                        c = value.charAt(i);
                        if (c == '-') {
                            i += BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED;
                            duration <<= BatteryStats.WIFI_SUPPL_STATE_SCANNING;
                            if (c < '0') {
                            }
                            if (c < 'a') {
                            }
                            duration += (long) ((c - 65) + BatteryStats.WIFI_SUPPL_STATE_COMPLETED);
                        } else {
                            this.mStepDurations[index] = (BatteryStats.STEP_LEVEL_TIME_MASK & duration) | out;
                        }
                    }
                    this.mStepDurations[index] = (BatteryStats.STEP_LEVEL_TIME_MASK & duration) | out;
                }
                i += BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED;
                level <<= BatteryStats.WIFI_SUPPL_STATE_SCANNING;
                if (c < '0') {
                }
                if (c < 'a') {
                }
                level += (long) ((c - 65) + BatteryStats.WIFI_SUPPL_STATE_COMPLETED);
            }
            i += BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED;
            out |= (level << BatteryStats.STEP_LEVEL_LEVEL_SHIFT) & BatteryStats.STEP_LEVEL_LEVEL_MASK;
            duration = 0;
            while (i < N) {
                c = value.charAt(i);
                if (c == '-') {
                    this.mStepDurations[index] = (BatteryStats.STEP_LEVEL_TIME_MASK & duration) | out;
                }
                i += BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED;
                duration <<= BatteryStats.WIFI_SUPPL_STATE_SCANNING;
                if (c < '0') {
                }
                if (c < 'a') {
                }
                duration += (long) ((c - 65) + BatteryStats.WIFI_SUPPL_STATE_COMPLETED);
            }
            this.mStepDurations[index] = (BatteryStats.STEP_LEVEL_TIME_MASK & duration) | out;
        }

        public void init() {
            this.mLastStepTime = -1;
            this.mNumStepDurations = BatteryStats.WIFI_SUPPL_STATE_INVALID;
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
            for (int i = BatteryStats.WIFI_SUPPL_STATE_INVALID; i < numSteps; i += BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED) {
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
            int numOfInterest = BatteryStats.WIFI_SUPPL_STATE_INVALID;
            for (int i = BatteryStats.WIFI_SUPPL_STATE_INVALID; i < count; i += BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED) {
                long initMode = (steps[i] & BatteryStats.STEP_LEVEL_INITIAL_MODE_MASK) >> BatteryStats.STEP_LEVEL_INITIAL_MODE_SHIFT;
                if ((((steps[i] & BatteryStats.STEP_LEVEL_MODIFIED_MODE_MASK) >> BatteryStats.STEP_LEVEL_MODIFIED_MODE_SHIFT) & modesOfInterest) == 0 && (initMode & modesOfInterest) == modeValues) {
                    numOfInterest += BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED;
                    total += steps[i] & BatteryStats.STEP_LEVEL_TIME_MASK;
                }
            }
            if (numOfInterest <= 0) {
                return -1;
            }
            if (outNumOfInterest != null) {
                outNumOfInterest[BatteryStats.WIFI_SUPPL_STATE_INVALID] = numOfInterest;
            }
            return (total / ((long) numOfInterest)) * 100;
        }

        public void addLevelSteps(int numStepLevels, long modeBits, long elapsedRealtime) {
            int stepCount = this.mNumStepDurations;
            long lastStepTime = this.mLastStepTime;
            if (lastStepTime >= 0 && numStepLevels > 0) {
                long[] steps = this.mStepDurations;
                long duration = elapsedRealtime - lastStepTime;
                for (int i = BatteryStats.WIFI_SUPPL_STATE_INVALID; i < numStepLevels; i += BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED) {
                    System.arraycopy(steps, BatteryStats.WIFI_SUPPL_STATE_INVALID, steps, BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED, steps.length - 1);
                    long thisDuration = duration / ((long) (numStepLevels - i));
                    duration -= thisDuration;
                    if (thisDuration > BatteryStats.STEP_LEVEL_TIME_MASK) {
                        thisDuration = BatteryStats.STEP_LEVEL_TIME_MASK;
                    }
                    steps[BatteryStats.WIFI_SUPPL_STATE_INVALID] = thisDuration | modeBits;
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
            if (N > this.mStepDurations.length) {
                throw new ParcelFormatException("more step durations than available: " + N);
            }
            this.mNumStepDurations = N;
            for (int i = BatteryStats.WIFI_SUPPL_STATE_INVALID; i < N; i += BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED) {
                this.mStepDurations[i] = in.readLong();
            }
        }

        public void writeToParcel(Parcel out) {
            int N = this.mNumStepDurations;
            out.writeInt(N);
            for (int i = BatteryStats.WIFI_SUPPL_STATE_INVALID; i < N; i += BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED) {
                out.writeLong(this.mStepDurations[i]);
            }
        }
    }

    public static abstract class LongCounter {
        public abstract long getCountLocked(int i);

        public abstract void logState(Printer printer, String str);
    }

    public static final class PackageChange {
        public String mPackageName;
        public boolean mUpdate;
        public int mVersionCode;
    }

    public static abstract class Timer {
        public abstract int getCountLocked(int i);

        public abstract long getTimeSinceMarkLocked(long j);

        public abstract long getTotalTimeLocked(long j, int i);

        public abstract void logState(Printer printer, String str);
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
        public static final int NUM_PROCESS_STATE = 6;
        public static final int NUM_USER_ACTIVITY_TYPES = 4;
        public static final int NUM_WIFI_BATCHED_SCAN_BINS = 5;
        public static final int PROCESS_STATE_BACKGROUND = 4;
        public static final int PROCESS_STATE_CACHED = 5;
        public static final int PROCESS_STATE_FOREGROUND = 3;
        public static final int PROCESS_STATE_FOREGROUND_SERVICE = 1;
        static final String[] PROCESS_STATE_NAMES = null;
        public static final int PROCESS_STATE_TOP = 0;
        public static final int PROCESS_STATE_TOP_SLEEPING = 2;
        static final String[] USER_ACTIVITY_TYPES = null;

        public class Pid {
            public int mWakeNesting;
            public long mWakeStartMs;
            public long mWakeSumMs;
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

            public abstract Timer getSensorTime();
        }

        public static abstract class Wakelock {
            public abstract Timer getWakeTime(int i);
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.BatteryStats.Uid.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.BatteryStats.Uid.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.BatteryStats.Uid.<clinit>():void");
        }

        public abstract Timer getAudioTurnedOnTimer();

        public abstract ControllerActivityCounter getBluetoothControllerActivity();

        public abstract Timer getBluetoothScanTimer();

        public abstract Timer getCameraTurnedOnTimer();

        public abstract long getCpuPowerMaUs(int i);

        public abstract Timer getFlashlightTurnedOnTimer();

        public abstract Timer getForegroundActivityTimer();

        public abstract long getFullWifiLockTime(long j, int i);

        public abstract ArrayMap<String, ? extends Timer> getJobStats();

        public abstract int getMobileRadioActiveCount(int i);

        public abstract long getMobileRadioActiveTime(int i);

        public abstract ControllerActivityCounter getModemControllerActivity();

        public abstract long getNetworkActivityBytes(int i, int i2);

        public abstract long getNetworkActivityPackets(int i, int i2);

        public abstract ArrayMap<String, ? extends Pkg> getPackageStats();

        public abstract SparseArray<? extends Pid> getPidStats();

        public abstract long getProcessStateTime(int i, long j, int i2);

        public abstract Timer getProcessStateTimer(int i);

        public abstract ArrayMap<String, ? extends Proc> getProcessStats();

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

        public abstract long getWifiRunningTime(long j, int i);

        public abstract int getWifiScanCount(int i);

        public abstract long getWifiScanTime(long j, int i);

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

    private static /* synthetic */ int[] -getcom-android-internal-os-BatterySipper$DrainTypeSwitchesValues() {
        if (-com-android-internal-os-BatterySipper$DrainTypeSwitchesValues != null) {
            return -com-android-internal-os-BatterySipper$DrainTypeSwitchesValues;
        }
        int[] iArr = new int[DrainType.values().length];
        try {
            iArr[DrainType.APP.ordinal()] = WIFI_SUPPL_STATE_DISCONNECTED;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[DrainType.BLUETOOTH.ordinal()] = WIFI_SUPPL_STATE_INTERFACE_DISABLED;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[DrainType.CAMERA.ordinal()] = WIFI_SUPPL_STATE_INACTIVE;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[DrainType.CELL.ordinal()] = WIFI_SUPPL_STATE_SCANNING;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[DrainType.FLASHLIGHT.ordinal()] = WIFI_SUPPL_STATE_AUTHENTICATING;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[DrainType.IDLE.ordinal()] = WIFI_SUPPL_STATE_ASSOCIATING;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[DrainType.OVERCOUNTED.ordinal()] = WIFI_SUPPL_STATE_ASSOCIATED;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[DrainType.PHONE.ordinal()] = WIFI_SUPPL_STATE_FOUR_WAY_HANDSHAKE;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[DrainType.SCREEN.ordinal()] = WIFI_SUPPL_STATE_GROUP_HANDSHAKE;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[DrainType.UNACCOUNTED.ordinal()] = WIFI_SUPPL_STATE_COMPLETED;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[DrainType.USER.ordinal()] = WIFI_SUPPL_STATE_DORMANT;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[DrainType.WIFI.ordinal()] = WIFI_SUPPL_STATE_UNINITIALIZED;
        } catch (NoSuchFieldError e12) {
        }
        -com-android-internal-os-BatterySipper$DrainTypeSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.BatteryStats.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.BatteryStats.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.BatteryStats.<clinit>():void");
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

    public abstract int getDischargeAmountScreenOff();

    public abstract int getDischargeAmountScreenOffSinceCharge();

    public abstract int getDischargeAmountScreenOn();

    public abstract int getDischargeAmountScreenOnSinceCharge();

    public abstract LongCounter getDischargeCoulombCounter();

    public abstract int getDischargeCurrentLevel();

    public abstract LevelStepTracker getDischargeLevelStepTracker();

    public abstract LongCounter getDischargeScreenOffCoulombCounter();

    public abstract int getDischargeStartLevel();

    public abstract String getEndPlatformVersion();

    public abstract int getEstimatedBatteryCapacity();

    public abstract long getFlashlightOnCount(int i);

    public abstract long getFlashlightOnTime(long j, int i);

    public abstract long getGlobalWifiRunningTime(long j, int i);

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

    public abstract Map<String, ? extends Timer> getKernelWakelockStats();

    public abstract long getLongestDeviceIdleModeTime(int i);

    public abstract int getLowDischargeAmountSinceCharge();

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

    public abstract int getPhoneOnCount(int i);

    public abstract long getPhoneOnTime(long j, int i);

    public abstract long getPhoneSignalScanningTime(long j, int i);

    public abstract int getPhoneSignalStrengthCount(int i, int i2);

    public abstract long getPhoneSignalStrengthTime(int i, long j, int i2);

    public abstract int getPowerSaveModeEnabledCount(int i);

    public abstract long getPowerSaveModeEnabledTime(long j, int i);

    public abstract long getScreenBrightnessTime(int i, long j, int i2);

    public abstract int getScreenOnCount(int i);

    public abstract long getScreenOnTime(long j, int i);

    public abstract long getStartClockTime();

    public abstract int getStartCount();

    public abstract String getStartPlatformVersion();

    public abstract SparseArray<? extends Uid> getUidStats();

    public abstract Map<String, ? extends Timer> getWakeupReasonStats();

    public abstract ControllerActivityCounter getWifiControllerActivity();

    public abstract long getWifiOnTime(long j, int i);

    public abstract int getWifiSignalStrengthCount(int i, int i2);

    public abstract long getWifiSignalStrengthTime(int i, long j, int i2);

    public abstract int getWifiStateCount(int i, int i2);

    public abstract long getWifiStateTime(int i, long j, int i2);

    public abstract int getWifiSupplStateCount(int i, int i2);

    public abstract long getWifiSupplStateTime(int i, long j, int i2);

    public abstract boolean hasBluetoothActivityReporting();

    public abstract boolean hasModemActivityReporting();

    public abstract boolean hasWifiActivityReporting();

    public abstract boolean startIteratingHistoryLocked();

    public abstract boolean startIteratingOldHistoryLocked();

    public abstract void writeToParcelWithoutUids(Parcel parcel, int i);

    public BatteryStats() {
        this.mFormatBuilder = new StringBuilder(DUMP_VERBOSE);
        this.mFormatter = new Formatter(this.mFormatBuilder);
    }

    private static final void formatTimeRaw(StringBuilder out, long seconds) {
        long days = seconds / 86400;
        if (days != 0) {
            out.append(days);
            out.append("d ");
        }
        long used = ((60 * days) * 60) * 24;
        long hours = (seconds - used) / 3600;
        if (!(hours == 0 && used == 0)) {
            out.append(hours);
            out.append("h ");
        }
        used += (60 * hours) * 60;
        long mins = (seconds - used) / 60;
        if (!(mins == 0 && used == 0)) {
            out.append(mins);
            out.append("m ");
        }
        used += 60 * mins;
        if (seconds != 0 || used != 0) {
            out.append(seconds - used);
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
        float perc = (((float) num) / ((float) den)) * SensorManager.LIGHT_CLOUDY;
        this.mFormatBuilder.setLength(WIFI_SUPPL_STATE_INVALID);
        Object[] objArr = new Object[WIFI_SUPPL_STATE_DISCONNECTED];
        objArr[WIFI_SUPPL_STATE_INVALID] = Float.valueOf(perc);
        this.mFormatter.format("%.1f%%", objArr);
        return this.mFormatBuilder.toString();
    }

    final String formatBytesLocked(long bytes) {
        this.mFormatBuilder.setLength(WIFI_SUPPL_STATE_INVALID);
        if (bytes < BYTES_PER_KB) {
            return bytes + "B";
        }
        Object[] objArr;
        if (bytes < BYTES_PER_MB) {
            objArr = new Object[WIFI_SUPPL_STATE_DISCONNECTED];
            objArr[WIFI_SUPPL_STATE_INVALID] = Double.valueOf(((double) bytes) / 1024.0d);
            this.mFormatter.format("%.2fKB", objArr);
            return this.mFormatBuilder.toString();
        } else if (bytes < BYTES_PER_GB) {
            objArr = new Object[WIFI_SUPPL_STATE_DISCONNECTED];
            objArr[WIFI_SUPPL_STATE_INVALID] = Double.valueOf(((double) bytes) / 1048576.0d);
            this.mFormatter.format("%.2fMB", objArr);
            return this.mFormatBuilder.toString();
        } else {
            objArr = new Object[WIFI_SUPPL_STATE_DISCONNECTED];
            objArr[WIFI_SUPPL_STATE_INVALID] = Double.valueOf(((double) bytes) / 1.073741824E9d);
            this.mFormatter.format("%.2fGB", objArr);
            return this.mFormatBuilder.toString();
        }
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
                return ", ";
            }
        }
        return linePrefix;
    }

    private static final boolean printTimer(PrintWriter pw, StringBuilder sb, Timer timer, long rawRealtime, int which, String prefix, String type) {
        if (timer != null) {
            long totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
            int count = timer.getCountLocked(which);
            if (totalTime != 0) {
                sb.setLength(WIFI_SUPPL_STATE_INVALID);
                sb.append(prefix);
                sb.append("    ");
                sb.append(type);
                sb.append(": ");
                formatTimeMs(sb, totalTime);
                sb.append("realtime (");
                sb.append(count);
                sb.append(" times)");
                pw.println(sb.toString());
                return true;
            }
        }
        return LOCAL_LOGV;
    }

    private static final String printWakeLockCheckin(StringBuilder sb, Timer timer, long elapsedRealtimeUs, String name, int which, String linePrefix) {
        long totalTimeMicros = 0;
        int count = WIFI_SUPPL_STATE_INVALID;
        if (timer != null) {
            totalTimeMicros = timer.getTotalTimeLocked(elapsedRealtimeUs, which);
            count = timer.getCountLocked(which);
        }
        sb.append(linePrefix);
        sb.append((500 + totalTimeMicros) / 1000);
        sb.append(',');
        sb.append(name != null ? name + "," : ProxyInfo.LOCAL_EXCL_LIST);
        sb.append(count);
        return ",";
    }

    private static final void dumpLineHeader(PrintWriter pw, int uid, String category, String type) {
        pw.print(WIFI_SUPPL_STATE_GROUP_HANDSHAKE);
        pw.print(',');
        pw.print(uid);
        pw.print(',');
        pw.print(category);
        pw.print(',');
        pw.print(type);
    }

    private static final void dumpLine(PrintWriter pw, int uid, String category, String type, Object... args) {
        dumpLineHeader(pw, uid, category, type);
        int length = args.length;
        for (int i = WIFI_SUPPL_STATE_INVALID; i < length; i += WIFI_SUPPL_STATE_DISCONNECTED) {
            Object arg = args[i];
            pw.print(',');
            pw.print(arg);
        }
        pw.println();
    }

    private static final void dumpTimer(PrintWriter pw, int uid, String category, String type, Timer timer, long rawRealtime, int which) {
        if (timer != null) {
            long totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
            int count = timer.getCountLocked(which);
            if (totalTime != 0) {
                Object[] objArr = new Object[WIFI_SUPPL_STATE_INTERFACE_DISABLED];
                objArr[WIFI_SUPPL_STATE_INVALID] = Long.valueOf(totalTime);
                objArr[WIFI_SUPPL_STATE_DISCONNECTED] = Integer.valueOf(count);
                dumpLine(pw, uid, category, type, objArr);
            }
        }
    }

    private static boolean controllerActivityHasData(ControllerActivityCounter counter, int which) {
        if (counter == null) {
            return LOCAL_LOGV;
        }
        if (counter.getIdleTimeCounter().getCountLocked(which) != 0 || counter.getRxTimeCounter().getCountLocked(which) != 0 || counter.getPowerCounter().getCountLocked(which) != 0) {
            return true;
        }
        LongCounter[] txTimeCounters = counter.getTxTimeCounters();
        int length = txTimeCounters.length;
        for (int i = WIFI_SUPPL_STATE_INVALID; i < length; i += WIFI_SUPPL_STATE_DISCONNECTED) {
            if (txTimeCounters[i].getCountLocked(which) != 0) {
                return true;
            }
        }
        return LOCAL_LOGV;
    }

    private static final void dumpControllerActivityLine(PrintWriter pw, int uid, String category, String type, ControllerActivityCounter counter, int which) {
        if (controllerActivityHasData(counter, which)) {
            dumpLineHeader(pw, uid, category, type);
            pw.print(",");
            pw.print(counter.getIdleTimeCounter().getCountLocked(which));
            pw.print(",");
            pw.print(counter.getRxTimeCounter().getCountLocked(which));
            pw.print(",");
            pw.print(counter.getPowerCounter().getCountLocked(which) / PackageManager.MAXIMUM_VERIFICATION_TIMEOUT);
            LongCounter[] txTimeCounters = counter.getTxTimeCounters();
            int length = txTimeCounters.length;
            for (int i = WIFI_SUPPL_STATE_INVALID; i < length; i += WIFI_SUPPL_STATE_DISCONNECTED) {
                LongCounter c = txTimeCounters[i];
                pw.print(",");
                pw.print(c.getCountLocked(which));
            }
            pw.println();
        }
    }

    private final void printControllerActivityIfInteresting(PrintWriter pw, StringBuilder sb, String prefix, String controllerName, ControllerActivityCounter counter, int which) {
        if (controllerActivityHasData(counter, which)) {
            printControllerActivity(pw, sb, prefix, controllerName, counter, which);
        }
    }

    private final void printControllerActivity(PrintWriter pw, StringBuilder sb, String prefix, String controllerName, ControllerActivityCounter counter, int which) {
        long idleTimeMs = counter.getIdleTimeCounter().getCountLocked(which);
        long rxTimeMs = counter.getRxTimeCounter().getCountLocked(which);
        long powerDrainMaMs = counter.getPowerCounter().getCountLocked(which);
        long totalTxTimeMs = 0;
        LongCounter[] txTimeCounters = counter.getTxTimeCounters();
        int length = txTimeCounters.length;
        for (int i = WIFI_SUPPL_STATE_INVALID; i < length; i += WIFI_SUPPL_STATE_DISCONNECTED) {
            totalTxTimeMs += txTimeCounters[i].getCountLocked(which);
        }
        long totalTimeMs = (idleTimeMs + rxTimeMs) + totalTxTimeMs;
        sb.setLength(WIFI_SUPPL_STATE_INVALID);
        sb.append(prefix);
        sb.append("  ");
        sb.append(controllerName);
        sb.append(" Idle time:   ");
        formatTimeMs(sb, idleTimeMs);
        sb.append("(");
        sb.append(formatRatioLocked(idleTimeMs, totalTimeMs));
        sb.append(")");
        pw.println(sb.toString());
        sb.setLength(WIFI_SUPPL_STATE_INVALID);
        sb.append(prefix);
        sb.append("  ");
        sb.append(controllerName);
        sb.append(" Rx time:     ");
        formatTimeMs(sb, rxTimeMs);
        sb.append("(");
        sb.append(formatRatioLocked(rxTimeMs, totalTimeMs));
        sb.append(")");
        pw.println(sb.toString());
        sb.setLength(WIFI_SUPPL_STATE_INVALID);
        sb.append(prefix);
        sb.append("  ");
        sb.append(controllerName);
        sb.append(" Tx time:     ");
        formatTimeMs(sb, totalTxTimeMs);
        sb.append("(");
        sb.append(formatRatioLocked(totalTxTimeMs, totalTimeMs));
        sb.append(")");
        pw.println(sb.toString());
        int numTxLvls = counter.getTxTimeCounters().length;
        if (numTxLvls > WIFI_SUPPL_STATE_DISCONNECTED) {
            for (int lvl = WIFI_SUPPL_STATE_INVALID; lvl < numTxLvls; lvl += WIFI_SUPPL_STATE_DISCONNECTED) {
                long txLvlTimeMs = counter.getTxTimeCounters()[lvl].getCountLocked(which);
                sb.setLength(WIFI_SUPPL_STATE_INVALID);
                sb.append(prefix);
                sb.append("    [");
                sb.append(lvl);
                sb.append("] ");
                formatTimeMs(sb, txLvlTimeMs);
                sb.append("(");
                sb.append(formatRatioLocked(txLvlTimeMs, totalTxTimeMs));
                sb.append(")");
                pw.println(sb.toString());
            }
        }
        sb.setLength(WIFI_SUPPL_STATE_INVALID);
        sb.append(prefix);
        sb.append("  ");
        sb.append(controllerName);
        sb.append(" Power drain: ").append(BatteryStatsHelper.makemAh(((double) powerDrainMaMs) / 3600000.0d));
        sb.append("mAh");
        pw.println(sb.toString());
    }

    public final void dumpCheckinLocked(Context context, PrintWriter pw, int which, int reqUid) {
        dumpCheckinLocked(context, pw, which, reqUid, BatteryStatsHelper.checkWifiOnly(context));
    }

    public final void dumpCheckinLocked(Context context, PrintWriter pw, int which, int reqUid, boolean wifiOnly) {
        int iu;
        int i;
        int uid;
        long rawUptime = SystemClock.uptimeMillis() * 1000;
        long rawRealtime = SystemClock.elapsedRealtime() * 1000;
        long batteryUptime = getBatteryUptime(rawUptime);
        long whichBatteryUptime = computeBatteryUptime(rawUptime, which);
        long whichBatteryRealtime = computeBatteryRealtime(rawRealtime, which);
        long whichBatteryScreenOffUptime = computeBatteryScreenOffUptime(rawUptime, which);
        long whichBatteryScreenOffRealtime = computeBatteryScreenOffRealtime(rawRealtime, which);
        long totalRealtime = computeRealtime(rawRealtime, which);
        long totalUptime = computeUptime(rawUptime, which);
        long screenOnTime = getScreenOnTime(rawRealtime, which);
        long interactiveTime = getInteractiveTime(rawRealtime, which);
        long powerSaveModeEnabledTime = getPowerSaveModeEnabledTime(rawRealtime, which);
        long deviceIdleModeLightTime = getDeviceIdleModeTime(WIFI_SUPPL_STATE_DISCONNECTED, rawRealtime, which);
        long deviceIdleModeFullTime = getDeviceIdleModeTime(WIFI_SUPPL_STATE_INTERFACE_DISABLED, rawRealtime, which);
        long deviceLightIdlingTime = getDeviceIdlingTime(WIFI_SUPPL_STATE_DISCONNECTED, rawRealtime, which);
        long deviceIdlingTime = getDeviceIdlingTime(WIFI_SUPPL_STATE_INTERFACE_DISABLED, rawRealtime, which);
        int connChanges = getNumConnectivityChange(which);
        long phoneOnTime = getPhoneOnTime(rawRealtime, which);
        long dischargeCount = getDischargeCoulombCounter().getCountLocked(which);
        long dischargeScreenOffCount = getDischargeScreenOffCoulombCounter().getCountLocked(which);
        StringBuilder stringBuilder = new StringBuilder(KeymasterDefs.KM_ALGORITHM_HMAC);
        SparseArray<? extends Uid> uidStats = getUidStats();
        int NU = uidStats.size();
        String category = STAT_NAMES[which];
        String str = BATTERY_DATA;
        Object[] objArr = new Object[WIFI_SUPPL_STATE_GROUP_HANDSHAKE];
        objArr[WIFI_SUPPL_STATE_INVALID] = which == 0 ? Integer.valueOf(getStartCount()) : "N/A";
        objArr[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf(whichBatteryRealtime / 1000);
        objArr[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Long.valueOf(whichBatteryUptime / 1000);
        objArr[WIFI_SUPPL_STATE_INACTIVE] = Long.valueOf(totalRealtime / 1000);
        objArr[WIFI_SUPPL_STATE_SCANNING] = Long.valueOf(totalUptime / 1000);
        objArr[WIFI_SUPPL_STATE_AUTHENTICATING] = Long.valueOf(getStartClockTime());
        objArr[WIFI_SUPPL_STATE_ASSOCIATING] = Long.valueOf(whichBatteryScreenOffRealtime / 1000);
        objArr[WIFI_SUPPL_STATE_ASSOCIATED] = Long.valueOf(whichBatteryScreenOffUptime / 1000);
        objArr[WIFI_SUPPL_STATE_FOUR_WAY_HANDSHAKE] = Integer.valueOf(getEstimatedBatteryCapacity());
        dumpLine(pw, WIFI_SUPPL_STATE_INVALID, category, str, objArr);
        long fullWakeLockTimeTotal = 0;
        long partialWakeLockTimeTotal = 0;
        for (iu = WIFI_SUPPL_STATE_INVALID; iu < NU; iu += WIFI_SUPPL_STATE_DISCONNECTED) {
            int iw;
            ArrayMap<String, ? extends Wakelock> wakelocks = ((Uid) uidStats.valueAt(iu)).getWakelockStats();
            for (iw = wakelocks.size() - 1; iw >= 0; iw--) {
                Wakelock wl = (Wakelock) wakelocks.valueAt(iw);
                Timer fullWakeTimer = wl.getWakeTime(WIFI_SUPPL_STATE_DISCONNECTED);
                if (fullWakeTimer != null) {
                    fullWakeLockTimeTotal += fullWakeTimer.getTotalTimeLocked(rawRealtime, which);
                }
                Timer partialWakeTimer = wl.getWakeTime(WIFI_SUPPL_STATE_INVALID);
                if (partialWakeTimer != null) {
                    partialWakeLockTimeTotal += partialWakeTimer.getTotalTimeLocked(rawRealtime, which);
                }
            }
        }
        long mobileRxTotalBytes = getNetworkActivityBytes(WIFI_SUPPL_STATE_INVALID, which);
        long mobileTxTotalBytes = getNetworkActivityBytes(WIFI_SUPPL_STATE_DISCONNECTED, which);
        long wifiRxTotalBytes = getNetworkActivityBytes(WIFI_SUPPL_STATE_INTERFACE_DISABLED, which);
        long wifiTxTotalBytes = getNetworkActivityBytes(WIFI_SUPPL_STATE_INACTIVE, which);
        long mobileRxTotalPackets = getNetworkActivityPackets(WIFI_SUPPL_STATE_INVALID, which);
        long mobileTxTotalPackets = getNetworkActivityPackets(WIFI_SUPPL_STATE_DISCONNECTED, which);
        long wifiRxTotalPackets = getNetworkActivityPackets(WIFI_SUPPL_STATE_INTERFACE_DISABLED, which);
        long wifiTxTotalPackets = getNetworkActivityPackets(WIFI_SUPPL_STATE_INACTIVE, which);
        long btRxTotalBytes = getNetworkActivityBytes(WIFI_SUPPL_STATE_SCANNING, which);
        long btTxTotalBytes = getNetworkActivityBytes(WIFI_SUPPL_STATE_AUTHENTICATING, which);
        String str2 = GLOBAL_NETWORK_DATA;
        Object[] objArr2 = new Object[WIFI_SUPPL_STATE_COMPLETED];
        objArr2[WIFI_SUPPL_STATE_INVALID] = Long.valueOf(mobileRxTotalBytes);
        objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf(mobileTxTotalBytes);
        objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Long.valueOf(wifiRxTotalBytes);
        objArr2[WIFI_SUPPL_STATE_INACTIVE] = Long.valueOf(wifiTxTotalBytes);
        objArr2[WIFI_SUPPL_STATE_SCANNING] = Long.valueOf(mobileRxTotalPackets);
        objArr2[WIFI_SUPPL_STATE_AUTHENTICATING] = Long.valueOf(mobileTxTotalPackets);
        objArr2[WIFI_SUPPL_STATE_ASSOCIATING] = Long.valueOf(wifiRxTotalPackets);
        objArr2[WIFI_SUPPL_STATE_ASSOCIATED] = Long.valueOf(wifiTxTotalPackets);
        objArr2[WIFI_SUPPL_STATE_FOUR_WAY_HANDSHAKE] = Long.valueOf(btRxTotalBytes);
        objArr2[WIFI_SUPPL_STATE_GROUP_HANDSHAKE] = Long.valueOf(btTxTotalBytes);
        dumpLine(pw, WIFI_SUPPL_STATE_INVALID, category, str2, objArr2);
        dumpControllerActivityLine(pw, WIFI_SUPPL_STATE_INVALID, category, GLOBAL_MODEM_CONTROLLER_DATA, getModemControllerActivity(), which);
        long wifiOnTime = getWifiOnTime(rawRealtime, which);
        long wifiRunningTime = getGlobalWifiRunningTime(rawRealtime, which);
        str2 = GLOBAL_WIFI_DATA;
        objArr2 = new Object[WIFI_SUPPL_STATE_AUTHENTICATING];
        objArr2[WIFI_SUPPL_STATE_INVALID] = Long.valueOf(wifiOnTime / 1000);
        objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf(wifiRunningTime / 1000);
        objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Integer.valueOf(WIFI_SUPPL_STATE_INVALID);
        objArr2[WIFI_SUPPL_STATE_INACTIVE] = Integer.valueOf(WIFI_SUPPL_STATE_INVALID);
        objArr2[WIFI_SUPPL_STATE_SCANNING] = Integer.valueOf(WIFI_SUPPL_STATE_INVALID);
        dumpLine(pw, WIFI_SUPPL_STATE_INVALID, category, str2, objArr2);
        dumpControllerActivityLine(pw, WIFI_SUPPL_STATE_INVALID, category, GLOBAL_WIFI_CONTROLLER_DATA, getWifiControllerActivity(), which);
        dumpControllerActivityLine(pw, WIFI_SUPPL_STATE_INVALID, category, GLOBAL_BLUETOOTH_CONTROLLER_DATA, getBluetoothControllerActivity(), which);
        dumpLine(pw, WIFI_SUPPL_STATE_INVALID, category, MISC_DATA, Long.valueOf(screenOnTime / 1000), Long.valueOf(phoneOnTime / 1000), Long.valueOf(fullWakeLockTimeTotal / 1000), Long.valueOf(partialWakeLockTimeTotal / 1000), Long.valueOf(getMobileRadioActiveTime(rawRealtime, which) / 1000), Long.valueOf(getMobileRadioActiveAdjustedTime(which) / 1000), Long.valueOf(interactiveTime / 1000), Long.valueOf(powerSaveModeEnabledTime / 1000), Integer.valueOf(connChanges), Long.valueOf(deviceIdleModeFullTime / 1000), Integer.valueOf(getDeviceIdleModeCount(WIFI_SUPPL_STATE_INTERFACE_DISABLED, which)), Long.valueOf(deviceIdlingTime / 1000), Integer.valueOf(getDeviceIdlingCount(WIFI_SUPPL_STATE_INTERFACE_DISABLED, which)), Integer.valueOf(getMobileRadioActiveCount(which)), Long.valueOf(getMobileRadioActiveUnknownTime(which) / 1000), Long.valueOf(deviceIdleModeLightTime / 1000), Integer.valueOf(getDeviceIdleModeCount(WIFI_SUPPL_STATE_DISCONNECTED, which)), Long.valueOf(deviceLightIdlingTime / 1000), Integer.valueOf(getDeviceIdlingCount(WIFI_SUPPL_STATE_DISCONNECTED, which)), Long.valueOf(getLongestDeviceIdleModeTime(WIFI_SUPPL_STATE_DISCONNECTED)), Long.valueOf(getLongestDeviceIdleModeTime(WIFI_SUPPL_STATE_INTERFACE_DISABLED)));
        Object[] args = new Object[WIFI_SUPPL_STATE_AUTHENTICATING];
        for (i = WIFI_SUPPL_STATE_INVALID; i < WIFI_SUPPL_STATE_AUTHENTICATING; i += WIFI_SUPPL_STATE_DISCONNECTED) {
            args[i] = Long.valueOf(getScreenBrightnessTime(i, rawRealtime, which) / 1000);
        }
        dumpLine(pw, WIFI_SUPPL_STATE_INVALID, category, SCREEN_BRIGHTNESS_DATA, args);
        args = new Object[WIFI_SUPPL_STATE_AUTHENTICATING];
        for (i = WIFI_SUPPL_STATE_INVALID; i < WIFI_SUPPL_STATE_AUTHENTICATING; i += WIFI_SUPPL_STATE_DISCONNECTED) {
            args[i] = Long.valueOf(getPhoneSignalStrengthTime(i, rawRealtime, which) / 1000);
        }
        dumpLine(pw, WIFI_SUPPL_STATE_INVALID, category, SIGNAL_STRENGTH_TIME_DATA, args);
        str2 = SIGNAL_SCANNING_TIME_DATA;
        objArr2 = new Object[WIFI_SUPPL_STATE_DISCONNECTED];
        objArr2[WIFI_SUPPL_STATE_INVALID] = Long.valueOf(getPhoneSignalScanningTime(rawRealtime, which) / 1000);
        dumpLine(pw, WIFI_SUPPL_STATE_INVALID, category, str2, objArr2);
        for (i = WIFI_SUPPL_STATE_INVALID; i < WIFI_SUPPL_STATE_AUTHENTICATING; i += WIFI_SUPPL_STATE_DISCONNECTED) {
            args[i] = Integer.valueOf(getPhoneSignalStrengthCount(i, which));
        }
        dumpLine(pw, WIFI_SUPPL_STATE_INVALID, category, SIGNAL_STRENGTH_COUNT_DATA, args);
        args = new Object[NUM_DATA_CONNECTION_TYPES];
        for (i = WIFI_SUPPL_STATE_INVALID; i < NUM_DATA_CONNECTION_TYPES; i += WIFI_SUPPL_STATE_DISCONNECTED) {
            args[i] = Long.valueOf(getPhoneDataConnectionTime(i, rawRealtime, which) / 1000);
        }
        dumpLine(pw, WIFI_SUPPL_STATE_INVALID, category, DATA_CONNECTION_TIME_DATA, args);
        for (i = WIFI_SUPPL_STATE_INVALID; i < NUM_DATA_CONNECTION_TYPES; i += WIFI_SUPPL_STATE_DISCONNECTED) {
            args[i] = Integer.valueOf(getPhoneDataConnectionCount(i, which));
        }
        dumpLine(pw, WIFI_SUPPL_STATE_INVALID, category, DATA_CONNECTION_COUNT_DATA, args);
        args = new Object[WIFI_SUPPL_STATE_FOUR_WAY_HANDSHAKE];
        for (i = WIFI_SUPPL_STATE_INVALID; i < WIFI_SUPPL_STATE_FOUR_WAY_HANDSHAKE; i += WIFI_SUPPL_STATE_DISCONNECTED) {
            args[i] = Long.valueOf(getWifiStateTime(i, rawRealtime, which) / 1000);
        }
        dumpLine(pw, WIFI_SUPPL_STATE_INVALID, category, WIFI_STATE_TIME_DATA, args);
        for (i = WIFI_SUPPL_STATE_INVALID; i < WIFI_SUPPL_STATE_FOUR_WAY_HANDSHAKE; i += WIFI_SUPPL_STATE_DISCONNECTED) {
            args[i] = Integer.valueOf(getWifiStateCount(i, which));
        }
        dumpLine(pw, WIFI_SUPPL_STATE_INVALID, category, WIFI_STATE_COUNT_DATA, args);
        args = new Object[SYNC];
        for (i = WIFI_SUPPL_STATE_INVALID; i < SYNC; i += WIFI_SUPPL_STATE_DISCONNECTED) {
            args[i] = Long.valueOf(getWifiSupplStateTime(i, rawRealtime, which) / 1000);
        }
        dumpLine(pw, WIFI_SUPPL_STATE_INVALID, category, WIFI_SUPPL_STATE_TIME_DATA, args);
        for (i = WIFI_SUPPL_STATE_INVALID; i < SYNC; i += WIFI_SUPPL_STATE_DISCONNECTED) {
            args[i] = Integer.valueOf(getWifiSupplStateCount(i, which));
        }
        dumpLine(pw, WIFI_SUPPL_STATE_INVALID, category, WIFI_SUPPL_STATE_COUNT_DATA, args);
        args = new Object[WIFI_SUPPL_STATE_AUTHENTICATING];
        for (i = WIFI_SUPPL_STATE_INVALID; i < WIFI_SUPPL_STATE_AUTHENTICATING; i += WIFI_SUPPL_STATE_DISCONNECTED) {
            args[i] = Long.valueOf(getWifiSignalStrengthTime(i, rawRealtime, which) / 1000);
        }
        dumpLine(pw, WIFI_SUPPL_STATE_INVALID, category, WIFI_SIGNAL_STRENGTH_TIME_DATA, args);
        for (i = WIFI_SUPPL_STATE_INVALID; i < WIFI_SUPPL_STATE_AUTHENTICATING; i += WIFI_SUPPL_STATE_DISCONNECTED) {
            args[i] = Integer.valueOf(getWifiSignalStrengthCount(i, which));
        }
        dumpLine(pw, WIFI_SUPPL_STATE_INVALID, category, WIFI_SIGNAL_STRENGTH_COUNT_DATA, args);
        if (which == WIFI_SUPPL_STATE_INTERFACE_DISABLED) {
            str2 = BATTERY_LEVEL_DATA;
            objArr2 = new Object[WIFI_SUPPL_STATE_INTERFACE_DISABLED];
            objArr2[WIFI_SUPPL_STATE_INVALID] = Integer.valueOf(getDischargeStartLevel());
            objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Integer.valueOf(getDischargeCurrentLevel());
            dumpLine(pw, WIFI_SUPPL_STATE_INVALID, category, str2, objArr2);
        }
        if (which == WIFI_SUPPL_STATE_INTERFACE_DISABLED) {
            str2 = BATTERY_DISCHARGE_DATA;
            objArr2 = new Object[WIFI_SUPPL_STATE_ASSOCIATING];
            objArr2[WIFI_SUPPL_STATE_INVALID] = Integer.valueOf(getDischargeStartLevel() - getDischargeCurrentLevel());
            objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Integer.valueOf(getDischargeStartLevel() - getDischargeCurrentLevel());
            objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Integer.valueOf(getDischargeAmountScreenOn());
            objArr2[WIFI_SUPPL_STATE_INACTIVE] = Integer.valueOf(getDischargeAmountScreenOff());
            objArr2[WIFI_SUPPL_STATE_SCANNING] = Long.valueOf(dischargeCount / 1000);
            objArr2[WIFI_SUPPL_STATE_AUTHENTICATING] = Long.valueOf(dischargeScreenOffCount / 1000);
            dumpLine(pw, WIFI_SUPPL_STATE_INVALID, category, str2, objArr2);
        } else {
            str2 = BATTERY_DISCHARGE_DATA;
            objArr2 = new Object[WIFI_SUPPL_STATE_ASSOCIATING];
            objArr2[WIFI_SUPPL_STATE_INVALID] = Integer.valueOf(getLowDischargeAmountSinceCharge());
            objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Integer.valueOf(getHighDischargeAmountSinceCharge());
            objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Integer.valueOf(getDischargeAmountScreenOnSinceCharge());
            objArr2[WIFI_SUPPL_STATE_INACTIVE] = Integer.valueOf(getDischargeAmountScreenOffSinceCharge());
            objArr2[WIFI_SUPPL_STATE_SCANNING] = Long.valueOf(dischargeCount / 1000);
            objArr2[WIFI_SUPPL_STATE_AUTHENTICATING] = Long.valueOf(dischargeScreenOffCount / 1000);
            dumpLine(pw, WIFI_SUPPL_STATE_INVALID, category, str2, objArr2);
        }
        if (reqUid < 0) {
            Map<String, ? extends Timer> kernelWakelocks = getKernelWakelockStats();
            if (kernelWakelocks.size() > 0) {
                for (Entry<String, ? extends Timer> ent : kernelWakelocks.entrySet()) {
                    stringBuilder.setLength(WIFI_SUPPL_STATE_INVALID);
                    printWakeLockCheckin(stringBuilder, (Timer) ent.getValue(), rawRealtime, null, which, ProxyInfo.LOCAL_EXCL_LIST);
                    str2 = KERNEL_WAKELOCK_DATA;
                    objArr2 = new Object[WIFI_SUPPL_STATE_INTERFACE_DISABLED];
                    objArr2[WIFI_SUPPL_STATE_INVALID] = ent.getKey();
                    objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = stringBuilder.toString();
                    dumpLine(pw, WIFI_SUPPL_STATE_INVALID, category, str2, objArr2);
                }
            }
            Map<String, ? extends Timer> wakeupReasons = getWakeupReasonStats();
            if (wakeupReasons.size() > 0) {
                for (Entry<String, ? extends Timer> ent2 : wakeupReasons.entrySet()) {
                    long totalTimeMicros = ((Timer) ent2.getValue()).getTotalTimeLocked(rawRealtime, which);
                    int count = ((Timer) ent2.getValue()).getCountLocked(which);
                    str = WAKEUP_REASON_DATA;
                    objArr = new Object[WIFI_SUPPL_STATE_INACTIVE];
                    objArr[WIFI_SUPPL_STATE_INVALID] = "\"" + ((String) ent2.getKey()) + "\"";
                    objArr[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf((500 + totalTimeMicros) / 1000);
                    objArr[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Integer.valueOf(count);
                    dumpLine(pw, WIFI_SUPPL_STATE_INVALID, category, str, objArr);
                }
            }
        }
        BatteryStatsHelper batteryStatsHelper = new BatteryStatsHelper(context, LOCAL_LOGV, wifiOnly);
        batteryStatsHelper.create(this);
        batteryStatsHelper.refreshStats(which, -1);
        List<BatterySipper> sippers = batteryStatsHelper.getUsageList();
        if (sippers != null && sippers.size() > 0) {
            str2 = POWER_USE_SUMMARY_DATA;
            objArr2 = new Object[WIFI_SUPPL_STATE_SCANNING];
            objArr2[WIFI_SUPPL_STATE_INVALID] = BatteryStatsHelper.makemAh(batteryStatsHelper.getPowerProfile().getBatteryCapacity());
            objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = BatteryStatsHelper.makemAh(batteryStatsHelper.getComputedPower());
            objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = BatteryStatsHelper.makemAh(batteryStatsHelper.getMinDrainedPower());
            objArr2[WIFI_SUPPL_STATE_INACTIVE] = BatteryStatsHelper.makemAh(batteryStatsHelper.getMaxDrainedPower());
            dumpLine(pw, WIFI_SUPPL_STATE_INVALID, category, str2, objArr2);
            for (i = WIFI_SUPPL_STATE_INVALID; i < sippers.size(); i += WIFI_SUPPL_STATE_DISCONNECTED) {
                String label;
                BatterySipper bs = (BatterySipper) sippers.get(i);
                uid = WIFI_SUPPL_STATE_INVALID;
                switch (-getcom-android-internal-os-BatterySipper$DrainTypeSwitchesValues()[bs.drainType.ordinal()]) {
                    case WIFI_SUPPL_STATE_DISCONNECTED /*1*/:
                        uid = bs.uidObj.getUid();
                        label = UID_DATA;
                        break;
                    case WIFI_SUPPL_STATE_INTERFACE_DISABLED /*2*/:
                        label = "blue";
                        break;
                    case WIFI_SUPPL_STATE_INACTIVE /*3*/:
                        label = Context.CAMERA_SERVICE;
                        break;
                    case WIFI_SUPPL_STATE_SCANNING /*4*/:
                        label = System.RADIO_CELL;
                        break;
                    case WIFI_SUPPL_STATE_AUTHENTICATING /*5*/:
                        label = "flashlight";
                        break;
                    case WIFI_SUPPL_STATE_ASSOCIATING /*6*/:
                        label = "idle";
                        break;
                    case WIFI_SUPPL_STATE_ASSOCIATED /*7*/:
                        label = "over";
                        break;
                    case WIFI_SUPPL_STATE_FOUR_WAY_HANDSHAKE /*8*/:
                        label = Insert.PHONE;
                        break;
                    case WIFI_SUPPL_STATE_GROUP_HANDSHAKE /*9*/:
                        label = "scrn";
                        break;
                    case WIFI_SUPPL_STATE_COMPLETED /*10*/:
                        label = "unacc";
                        break;
                    case WIFI_SUPPL_STATE_DORMANT /*11*/:
                        uid = UserHandle.getUid(bs.userId, WIFI_SUPPL_STATE_INVALID);
                        label = Context.USER_SERVICE;
                        break;
                    case WIFI_SUPPL_STATE_UNINITIALIZED /*12*/:
                        label = System.RADIO_WIFI;
                        break;
                    default:
                        label = "???";
                        break;
                }
                str2 = POWER_USE_ITEM_DATA;
                objArr2 = new Object[WIFI_SUPPL_STATE_INTERFACE_DISABLED];
                objArr2[WIFI_SUPPL_STATE_INVALID] = label;
                objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = BatteryStatsHelper.makemAh(bs.totalPowerMah);
                dumpLine(pw, uid, category, str2, objArr2);
            }
        }
        for (iu = WIFI_SUPPL_STATE_INVALID; iu < NU; iu += WIFI_SUPPL_STATE_DISCONNECTED) {
            uid = uidStats.keyAt(iu);
            if (reqUid < 0 || uid == reqUid) {
                long fullWifiLockOnTime;
                long wifiScanTime;
                int wifiScanCount;
                long uidWifiRunningTime;
                boolean hasData;
                int val;
                String linePrefix;
                String name;
                ArrayMap<String, ? extends Timer> syncs;
                int isy;
                Timer timer;
                long totalTime;
                ArrayMap<String, ? extends Timer> jobs;
                int ij;
                SparseArray<? extends Sensor> sensors;
                int NSE;
                int ise;
                Sensor se;
                int sensorNumber;
                Object[] stateTimes;
                long totalStateTime;
                int ips;
                long time;
                long userCpuTimeUs;
                long systemCpuTimeUs;
                long powerCpuMaUs;
                ArrayMap<String, ? extends Proc> processStats;
                int ipr;
                Proc ps;
                long userMillis;
                long systemMillis;
                long foregroundMillis;
                int starts;
                int numCrashes;
                int numAnrs;
                ArrayMap<String, ? extends Pkg> packageStats;
                int ipkg;
                Pkg ps2;
                int wakeups;
                ArrayMap<String, ? extends Counter> alarms;
                int iwa;
                ArrayMap<String, ? extends Serv> serviceStats;
                int isvc;
                Serv ss;
                long startTime;
                int launches;
                Uid u = (Uid) uidStats.valueAt(iu);
                long mobileBytesRx = u.getNetworkActivityBytes(WIFI_SUPPL_STATE_INVALID, which);
                long mobileBytesTx = u.getNetworkActivityBytes(WIFI_SUPPL_STATE_DISCONNECTED, which);
                long wifiBytesRx = u.getNetworkActivityBytes(WIFI_SUPPL_STATE_INTERFACE_DISABLED, which);
                long wifiBytesTx = u.getNetworkActivityBytes(WIFI_SUPPL_STATE_INACTIVE, which);
                long mobilePacketsRx = u.getNetworkActivityPackets(WIFI_SUPPL_STATE_INVALID, which);
                long mobilePacketsTx = u.getNetworkActivityPackets(WIFI_SUPPL_STATE_DISCONNECTED, which);
                long mobileActiveTime = u.getMobileRadioActiveTime(which);
                int mobileActiveCount = u.getMobileRadioActiveCount(which);
                long wifiPacketsRx = u.getNetworkActivityPackets(WIFI_SUPPL_STATE_INTERFACE_DISABLED, which);
                long wifiPacketsTx = u.getNetworkActivityPackets(WIFI_SUPPL_STATE_INACTIVE, which);
                long btBytesRx = u.getNetworkActivityBytes(WIFI_SUPPL_STATE_SCANNING, which);
                long btBytesTx = u.getNetworkActivityBytes(WIFI_SUPPL_STATE_AUTHENTICATING, which);
                if (mobileBytesRx <= 0 && mobileBytesTx <= 0 && wifiBytesRx <= 0 && wifiBytesTx <= 0 && mobilePacketsRx <= 0 && mobilePacketsTx <= 0 && wifiPacketsRx <= 0 && wifiPacketsTx <= 0 && mobileActiveTime <= 0 && mobileActiveCount <= 0 && btBytesRx <= 0) {
                    if (btBytesTx > 0) {
                    }
                    dumpControllerActivityLine(pw, uid, category, MODEM_CONTROLLER_DATA, u.getModemControllerActivity(), which);
                    fullWifiLockOnTime = u.getFullWifiLockTime(rawRealtime, which);
                    wifiScanTime = u.getWifiScanTime(rawRealtime, which);
                    wifiScanCount = u.getWifiScanCount(which);
                    uidWifiRunningTime = u.getWifiRunningTime(rawRealtime, which);
                    if (fullWifiLockOnTime == 0 && wifiScanTime == 0 && wifiScanCount == 0) {
                        if (uidWifiRunningTime != 0) {
                        }
                        dumpControllerActivityLine(pw, uid, category, WIFI_CONTROLLER_DATA, u.getWifiControllerActivity(), which);
                        dumpTimer(pw, uid, category, BLUETOOTH_MISC_DATA, u.getBluetoothScanTimer(), rawRealtime, which);
                        dumpControllerActivityLine(pw, uid, category, BLUETOOTH_CONTROLLER_DATA, u.getBluetoothControllerActivity(), which);
                        if (u.hasUserActivity()) {
                            args = new Object[WIFI_SUPPL_STATE_SCANNING];
                            hasData = LOCAL_LOGV;
                            for (i = WIFI_SUPPL_STATE_INVALID; i < WIFI_SUPPL_STATE_SCANNING; i += WIFI_SUPPL_STATE_DISCONNECTED) {
                                val = u.getUserActivityCount(i, which);
                                args[i] = Integer.valueOf(val);
                                if (val == 0) {
                                    hasData = true;
                                }
                            }
                            if (hasData) {
                                dumpLine(pw, uid, category, USER_ACTIVITY_DATA, args);
                            }
                        }
                        wakelocks = u.getWakelockStats();
                        for (iw = wakelocks.size() - 1; iw >= 0; iw--) {
                            wl = (Wakelock) wakelocks.valueAt(iw);
                            linePrefix = ProxyInfo.LOCAL_EXCL_LIST;
                            stringBuilder.setLength(WIFI_SUPPL_STATE_INVALID);
                            linePrefix = printWakeLockCheckin(stringBuilder, wl.getWakeTime(WIFI_SUPPL_STATE_INTERFACE_DISABLED), rawRealtime, "w", which, printWakeLockCheckin(stringBuilder, wl.getWakeTime(WIFI_SUPPL_STATE_INVALID), rawRealtime, TtmlUtils.TAG_P, which, printWakeLockCheckin(stringBuilder, wl.getWakeTime(WIFI_SUPPL_STATE_DISCONNECTED), rawRealtime, FullBackup.FILES_TREE_TOKEN, which, linePrefix)));
                            if (stringBuilder.length() <= 0) {
                                name = (String) wakelocks.keyAt(iw);
                                if (name.indexOf(44) >= 0) {
                                    name = name.replace(',', '_');
                                }
                                str2 = WAKELOCK_DATA;
                                objArr2 = new Object[WIFI_SUPPL_STATE_INTERFACE_DISABLED];
                                objArr2[WIFI_SUPPL_STATE_INVALID] = name;
                                objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = stringBuilder.toString();
                                dumpLine(pw, uid, category, str2, objArr2);
                            }
                        }
                        syncs = u.getSyncStats();
                        for (isy = syncs.size() - 1; isy >= 0; isy--) {
                            timer = (Timer) syncs.valueAt(isy);
                            totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                            count = timer.getCountLocked(which);
                            if (totalTime == 0) {
                                str2 = SYNC_DATA;
                                objArr2 = new Object[WIFI_SUPPL_STATE_INACTIVE];
                                objArr2[WIFI_SUPPL_STATE_INVALID] = syncs.keyAt(isy);
                                objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf(totalTime);
                                objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Integer.valueOf(count);
                                dumpLine(pw, uid, category, str2, objArr2);
                            }
                        }
                        jobs = u.getJobStats();
                        for (ij = jobs.size() - 1; ij >= 0; ij--) {
                            timer = (Timer) jobs.valueAt(ij);
                            totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                            count = timer.getCountLocked(which);
                            if (totalTime == 0) {
                                str2 = JOB_DATA;
                                objArr2 = new Object[WIFI_SUPPL_STATE_INACTIVE];
                                objArr2[WIFI_SUPPL_STATE_INVALID] = jobs.keyAt(ij);
                                objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf(totalTime);
                                objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Integer.valueOf(count);
                                dumpLine(pw, uid, category, str2, objArr2);
                            }
                        }
                        dumpTimer(pw, uid, category, FLASHLIGHT_DATA, u.getFlashlightTurnedOnTimer(), rawRealtime, which);
                        dumpTimer(pw, uid, category, CAMERA_DATA, u.getCameraTurnedOnTimer(), rawRealtime, which);
                        dumpTimer(pw, uid, category, VIDEO_DATA, u.getVideoTurnedOnTimer(), rawRealtime, which);
                        dumpTimer(pw, uid, category, AUDIO_DATA, u.getAudioTurnedOnTimer(), rawRealtime, which);
                        sensors = u.getSensorStats();
                        NSE = sensors.size();
                        for (ise = WIFI_SUPPL_STATE_INVALID; ise < NSE; ise += WIFI_SUPPL_STATE_DISCONNECTED) {
                            se = (Sensor) sensors.valueAt(ise);
                            sensorNumber = sensors.keyAt(ise);
                            timer = se.getSensorTime();
                            if (timer == null) {
                                totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                                count = timer.getCountLocked(which);
                                if (totalTime == 0) {
                                    str2 = SENSOR_DATA;
                                    objArr2 = new Object[WIFI_SUPPL_STATE_INACTIVE];
                                    objArr2[WIFI_SUPPL_STATE_INVALID] = Integer.valueOf(sensorNumber);
                                    objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf(totalTime);
                                    objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Integer.valueOf(count);
                                    dumpLine(pw, uid, category, str2, objArr2);
                                }
                            }
                        }
                        dumpTimer(pw, uid, category, VIBRATOR_DATA, u.getVibratorOnTimer(), rawRealtime, which);
                        dumpTimer(pw, uid, category, FOREGROUND_DATA, u.getForegroundActivityTimer(), rawRealtime, which);
                        stateTimes = new Object[WIFI_SUPPL_STATE_ASSOCIATING];
                        totalStateTime = 0;
                        for (ips = WIFI_SUPPL_STATE_INVALID; ips < WIFI_SUPPL_STATE_ASSOCIATING; ips += WIFI_SUPPL_STATE_DISCONNECTED) {
                            time = u.getProcessStateTime(ips, rawRealtime, which);
                            totalStateTime += time;
                            stateTimes[ips] = Long.valueOf((500 + time) / 1000);
                        }
                        if (totalStateTime > 0) {
                            dumpLine(pw, uid, category, STATE_TIME_DATA, stateTimes);
                        }
                        userCpuTimeUs = u.getUserCpuTimeUs(which);
                        systemCpuTimeUs = u.getSystemCpuTimeUs(which);
                        powerCpuMaUs = u.getCpuPowerMaUs(which);
                        if (userCpuTimeUs <= 0 && systemCpuTimeUs <= 0) {
                            if (powerCpuMaUs > 0) {
                            }
                            processStats = u.getProcessStats();
                            for (ipr = processStats.size() - 1; ipr >= 0; ipr--) {
                                ps = (Proc) processStats.valueAt(ipr);
                                userMillis = ps.getUserTime(which);
                                systemMillis = ps.getSystemTime(which);
                                foregroundMillis = ps.getForegroundTime(which);
                                starts = ps.getStarts(which);
                                numCrashes = ps.getNumCrashes(which);
                                numAnrs = ps.getNumAnrs(which);
                                if (userMillis == 0 && systemMillis == 0 && foregroundMillis == 0 && starts == 0 && numAnrs == 0) {
                                    if (numCrashes != 0) {
                                    }
                                }
                                str2 = PROCESS_DATA;
                                objArr2 = new Object[WIFI_SUPPL_STATE_ASSOCIATED];
                                objArr2[WIFI_SUPPL_STATE_INVALID] = processStats.keyAt(ipr);
                                objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf(userMillis);
                                objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Long.valueOf(systemMillis);
                                objArr2[WIFI_SUPPL_STATE_INACTIVE] = Long.valueOf(foregroundMillis);
                                objArr2[WIFI_SUPPL_STATE_SCANNING] = Integer.valueOf(starts);
                                objArr2[WIFI_SUPPL_STATE_AUTHENTICATING] = Integer.valueOf(numAnrs);
                                objArr2[WIFI_SUPPL_STATE_ASSOCIATING] = Integer.valueOf(numCrashes);
                                dumpLine(pw, uid, category, str2, objArr2);
                            }
                            packageStats = u.getPackageStats();
                            for (ipkg = packageStats.size() - 1; ipkg >= 0; ipkg--) {
                                ps2 = (Pkg) packageStats.valueAt(ipkg);
                                wakeups = WIFI_SUPPL_STATE_INVALID;
                                alarms = ps2.getWakeupAlarmStats();
                                for (iwa = alarms.size() - 1; iwa >= 0; iwa--) {
                                    count = ((Counter) alarms.valueAt(iwa)).getCountLocked(which);
                                    wakeups += count;
                                    name = ((String) alarms.keyAt(iwa)).replace(',', '_');
                                    str2 = WAKEUP_ALARM_DATA;
                                    objArr2 = new Object[WIFI_SUPPL_STATE_INTERFACE_DISABLED];
                                    objArr2[WIFI_SUPPL_STATE_INVALID] = name;
                                    objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Integer.valueOf(count);
                                    dumpLine(pw, uid, category, str2, objArr2);
                                }
                                serviceStats = ps2.getServiceStats();
                                for (isvc = serviceStats.size() - 1; isvc >= 0; isvc--) {
                                    ss = (Serv) serviceStats.valueAt(isvc);
                                    startTime = ss.getStartTime(batteryUptime, which);
                                    starts = ss.getStarts(which);
                                    launches = ss.getLaunches(which);
                                    if (startTime == 0 && starts == 0) {
                                        if (launches != 0) {
                                        }
                                    }
                                    str2 = APK_DATA;
                                    objArr2 = new Object[WIFI_SUPPL_STATE_ASSOCIATING];
                                    objArr2[WIFI_SUPPL_STATE_INVALID] = Integer.valueOf(wakeups);
                                    objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = packageStats.keyAt(ipkg);
                                    objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = serviceStats.keyAt(isvc);
                                    objArr2[WIFI_SUPPL_STATE_INACTIVE] = Long.valueOf(startTime / 1000);
                                    objArr2[WIFI_SUPPL_STATE_SCANNING] = Integer.valueOf(starts);
                                    objArr2[WIFI_SUPPL_STATE_AUTHENTICATING] = Integer.valueOf(launches);
                                    dumpLine(pw, uid, category, str2, objArr2);
                                }
                            }
                        }
                        str2 = CPU_DATA;
                        objArr2 = new Object[WIFI_SUPPL_STATE_INACTIVE];
                        objArr2[WIFI_SUPPL_STATE_INVALID] = Long.valueOf(userCpuTimeUs / 1000);
                        objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf(systemCpuTimeUs / 1000);
                        objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Long.valueOf(powerCpuMaUs / 1000);
                        dumpLine(pw, uid, category, str2, objArr2);
                        processStats = u.getProcessStats();
                        while (ipr >= 0) {
                            ps = (Proc) processStats.valueAt(ipr);
                            userMillis = ps.getUserTime(which);
                            systemMillis = ps.getSystemTime(which);
                            foregroundMillis = ps.getForegroundTime(which);
                            starts = ps.getStarts(which);
                            numCrashes = ps.getNumCrashes(which);
                            numAnrs = ps.getNumAnrs(which);
                            if (numCrashes != 0) {
                            } else {
                                str2 = PROCESS_DATA;
                                objArr2 = new Object[WIFI_SUPPL_STATE_ASSOCIATED];
                                objArr2[WIFI_SUPPL_STATE_INVALID] = processStats.keyAt(ipr);
                                objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf(userMillis);
                                objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Long.valueOf(systemMillis);
                                objArr2[WIFI_SUPPL_STATE_INACTIVE] = Long.valueOf(foregroundMillis);
                                objArr2[WIFI_SUPPL_STATE_SCANNING] = Integer.valueOf(starts);
                                objArr2[WIFI_SUPPL_STATE_AUTHENTICATING] = Integer.valueOf(numAnrs);
                                objArr2[WIFI_SUPPL_STATE_ASSOCIATING] = Integer.valueOf(numCrashes);
                                dumpLine(pw, uid, category, str2, objArr2);
                            }
                        }
                        packageStats = u.getPackageStats();
                        for (ipkg = packageStats.size() - 1; ipkg >= 0; ipkg--) {
                            ps2 = (Pkg) packageStats.valueAt(ipkg);
                            wakeups = WIFI_SUPPL_STATE_INVALID;
                            alarms = ps2.getWakeupAlarmStats();
                            for (iwa = alarms.size() - 1; iwa >= 0; iwa--) {
                                count = ((Counter) alarms.valueAt(iwa)).getCountLocked(which);
                                wakeups += count;
                                name = ((String) alarms.keyAt(iwa)).replace(',', '_');
                                str2 = WAKEUP_ALARM_DATA;
                                objArr2 = new Object[WIFI_SUPPL_STATE_INTERFACE_DISABLED];
                                objArr2[WIFI_SUPPL_STATE_INVALID] = name;
                                objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Integer.valueOf(count);
                                dumpLine(pw, uid, category, str2, objArr2);
                            }
                            serviceStats = ps2.getServiceStats();
                            while (isvc >= 0) {
                                ss = (Serv) serviceStats.valueAt(isvc);
                                startTime = ss.getStartTime(batteryUptime, which);
                                starts = ss.getStarts(which);
                                launches = ss.getLaunches(which);
                                if (launches != 0) {
                                } else {
                                    str2 = APK_DATA;
                                    objArr2 = new Object[WIFI_SUPPL_STATE_ASSOCIATING];
                                    objArr2[WIFI_SUPPL_STATE_INVALID] = Integer.valueOf(wakeups);
                                    objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = packageStats.keyAt(ipkg);
                                    objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = serviceStats.keyAt(isvc);
                                    objArr2[WIFI_SUPPL_STATE_INACTIVE] = Long.valueOf(startTime / 1000);
                                    objArr2[WIFI_SUPPL_STATE_SCANNING] = Integer.valueOf(starts);
                                    objArr2[WIFI_SUPPL_STATE_AUTHENTICATING] = Integer.valueOf(launches);
                                    dumpLine(pw, uid, category, str2, objArr2);
                                }
                            }
                        }
                    }
                    str2 = WIFI_DATA;
                    objArr2 = new Object[WIFI_SUPPL_STATE_ASSOCIATED];
                    objArr2[WIFI_SUPPL_STATE_INVALID] = Long.valueOf(fullWifiLockOnTime);
                    objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf(wifiScanTime);
                    objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Long.valueOf(uidWifiRunningTime);
                    objArr2[WIFI_SUPPL_STATE_INACTIVE] = Integer.valueOf(wifiScanCount);
                    objArr2[WIFI_SUPPL_STATE_SCANNING] = Integer.valueOf(WIFI_SUPPL_STATE_INVALID);
                    objArr2[WIFI_SUPPL_STATE_AUTHENTICATING] = Integer.valueOf(WIFI_SUPPL_STATE_INVALID);
                    objArr2[WIFI_SUPPL_STATE_ASSOCIATING] = Integer.valueOf(WIFI_SUPPL_STATE_INVALID);
                    dumpLine(pw, uid, category, str2, objArr2);
                    dumpControllerActivityLine(pw, uid, category, WIFI_CONTROLLER_DATA, u.getWifiControllerActivity(), which);
                    dumpTimer(pw, uid, category, BLUETOOTH_MISC_DATA, u.getBluetoothScanTimer(), rawRealtime, which);
                    dumpControllerActivityLine(pw, uid, category, BLUETOOTH_CONTROLLER_DATA, u.getBluetoothControllerActivity(), which);
                    if (u.hasUserActivity()) {
                        args = new Object[WIFI_SUPPL_STATE_SCANNING];
                        hasData = LOCAL_LOGV;
                        for (i = WIFI_SUPPL_STATE_INVALID; i < WIFI_SUPPL_STATE_SCANNING; i += WIFI_SUPPL_STATE_DISCONNECTED) {
                            val = u.getUserActivityCount(i, which);
                            args[i] = Integer.valueOf(val);
                            if (val == 0) {
                                hasData = true;
                            }
                        }
                        if (hasData) {
                            dumpLine(pw, uid, category, USER_ACTIVITY_DATA, args);
                        }
                    }
                    wakelocks = u.getWakelockStats();
                    for (iw = wakelocks.size() - 1; iw >= 0; iw--) {
                        wl = (Wakelock) wakelocks.valueAt(iw);
                        linePrefix = ProxyInfo.LOCAL_EXCL_LIST;
                        stringBuilder.setLength(WIFI_SUPPL_STATE_INVALID);
                        linePrefix = printWakeLockCheckin(stringBuilder, wl.getWakeTime(WIFI_SUPPL_STATE_INTERFACE_DISABLED), rawRealtime, "w", which, printWakeLockCheckin(stringBuilder, wl.getWakeTime(WIFI_SUPPL_STATE_INVALID), rawRealtime, TtmlUtils.TAG_P, which, printWakeLockCheckin(stringBuilder, wl.getWakeTime(WIFI_SUPPL_STATE_DISCONNECTED), rawRealtime, FullBackup.FILES_TREE_TOKEN, which, linePrefix)));
                        if (stringBuilder.length() <= 0) {
                            name = (String) wakelocks.keyAt(iw);
                            if (name.indexOf(44) >= 0) {
                                name = name.replace(',', '_');
                            }
                            str2 = WAKELOCK_DATA;
                            objArr2 = new Object[WIFI_SUPPL_STATE_INTERFACE_DISABLED];
                            objArr2[WIFI_SUPPL_STATE_INVALID] = name;
                            objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = stringBuilder.toString();
                            dumpLine(pw, uid, category, str2, objArr2);
                        }
                    }
                    syncs = u.getSyncStats();
                    for (isy = syncs.size() - 1; isy >= 0; isy--) {
                        timer = (Timer) syncs.valueAt(isy);
                        totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                        count = timer.getCountLocked(which);
                        if (totalTime == 0) {
                            str2 = SYNC_DATA;
                            objArr2 = new Object[WIFI_SUPPL_STATE_INACTIVE];
                            objArr2[WIFI_SUPPL_STATE_INVALID] = syncs.keyAt(isy);
                            objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf(totalTime);
                            objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Integer.valueOf(count);
                            dumpLine(pw, uid, category, str2, objArr2);
                        }
                    }
                    jobs = u.getJobStats();
                    for (ij = jobs.size() - 1; ij >= 0; ij--) {
                        timer = (Timer) jobs.valueAt(ij);
                        totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                        count = timer.getCountLocked(which);
                        if (totalTime == 0) {
                            str2 = JOB_DATA;
                            objArr2 = new Object[WIFI_SUPPL_STATE_INACTIVE];
                            objArr2[WIFI_SUPPL_STATE_INVALID] = jobs.keyAt(ij);
                            objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf(totalTime);
                            objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Integer.valueOf(count);
                            dumpLine(pw, uid, category, str2, objArr2);
                        }
                    }
                    dumpTimer(pw, uid, category, FLASHLIGHT_DATA, u.getFlashlightTurnedOnTimer(), rawRealtime, which);
                    dumpTimer(pw, uid, category, CAMERA_DATA, u.getCameraTurnedOnTimer(), rawRealtime, which);
                    dumpTimer(pw, uid, category, VIDEO_DATA, u.getVideoTurnedOnTimer(), rawRealtime, which);
                    dumpTimer(pw, uid, category, AUDIO_DATA, u.getAudioTurnedOnTimer(), rawRealtime, which);
                    sensors = u.getSensorStats();
                    NSE = sensors.size();
                    for (ise = WIFI_SUPPL_STATE_INVALID; ise < NSE; ise += WIFI_SUPPL_STATE_DISCONNECTED) {
                        se = (Sensor) sensors.valueAt(ise);
                        sensorNumber = sensors.keyAt(ise);
                        timer = se.getSensorTime();
                        if (timer == null) {
                            totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                            count = timer.getCountLocked(which);
                            if (totalTime == 0) {
                                str2 = SENSOR_DATA;
                                objArr2 = new Object[WIFI_SUPPL_STATE_INACTIVE];
                                objArr2[WIFI_SUPPL_STATE_INVALID] = Integer.valueOf(sensorNumber);
                                objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf(totalTime);
                                objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Integer.valueOf(count);
                                dumpLine(pw, uid, category, str2, objArr2);
                            }
                        }
                    }
                    dumpTimer(pw, uid, category, VIBRATOR_DATA, u.getVibratorOnTimer(), rawRealtime, which);
                    dumpTimer(pw, uid, category, FOREGROUND_DATA, u.getForegroundActivityTimer(), rawRealtime, which);
                    stateTimes = new Object[WIFI_SUPPL_STATE_ASSOCIATING];
                    totalStateTime = 0;
                    for (ips = WIFI_SUPPL_STATE_INVALID; ips < WIFI_SUPPL_STATE_ASSOCIATING; ips += WIFI_SUPPL_STATE_DISCONNECTED) {
                        time = u.getProcessStateTime(ips, rawRealtime, which);
                        totalStateTime += time;
                        stateTimes[ips] = Long.valueOf((500 + time) / 1000);
                    }
                    if (totalStateTime > 0) {
                        dumpLine(pw, uid, category, STATE_TIME_DATA, stateTimes);
                    }
                    userCpuTimeUs = u.getUserCpuTimeUs(which);
                    systemCpuTimeUs = u.getSystemCpuTimeUs(which);
                    powerCpuMaUs = u.getCpuPowerMaUs(which);
                    if (powerCpuMaUs > 0) {
                        str2 = CPU_DATA;
                        objArr2 = new Object[WIFI_SUPPL_STATE_INACTIVE];
                        objArr2[WIFI_SUPPL_STATE_INVALID] = Long.valueOf(userCpuTimeUs / 1000);
                        objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf(systemCpuTimeUs / 1000);
                        objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Long.valueOf(powerCpuMaUs / 1000);
                        dumpLine(pw, uid, category, str2, objArr2);
                    }
                    processStats = u.getProcessStats();
                    while (ipr >= 0) {
                        ps = (Proc) processStats.valueAt(ipr);
                        userMillis = ps.getUserTime(which);
                        systemMillis = ps.getSystemTime(which);
                        foregroundMillis = ps.getForegroundTime(which);
                        starts = ps.getStarts(which);
                        numCrashes = ps.getNumCrashes(which);
                        numAnrs = ps.getNumAnrs(which);
                        if (numCrashes != 0) {
                            str2 = PROCESS_DATA;
                            objArr2 = new Object[WIFI_SUPPL_STATE_ASSOCIATED];
                            objArr2[WIFI_SUPPL_STATE_INVALID] = processStats.keyAt(ipr);
                            objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf(userMillis);
                            objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Long.valueOf(systemMillis);
                            objArr2[WIFI_SUPPL_STATE_INACTIVE] = Long.valueOf(foregroundMillis);
                            objArr2[WIFI_SUPPL_STATE_SCANNING] = Integer.valueOf(starts);
                            objArr2[WIFI_SUPPL_STATE_AUTHENTICATING] = Integer.valueOf(numAnrs);
                            objArr2[WIFI_SUPPL_STATE_ASSOCIATING] = Integer.valueOf(numCrashes);
                            dumpLine(pw, uid, category, str2, objArr2);
                        } else {
                        }
                    }
                    packageStats = u.getPackageStats();
                    for (ipkg = packageStats.size() - 1; ipkg >= 0; ipkg--) {
                        ps2 = (Pkg) packageStats.valueAt(ipkg);
                        wakeups = WIFI_SUPPL_STATE_INVALID;
                        alarms = ps2.getWakeupAlarmStats();
                        for (iwa = alarms.size() - 1; iwa >= 0; iwa--) {
                            count = ((Counter) alarms.valueAt(iwa)).getCountLocked(which);
                            wakeups += count;
                            name = ((String) alarms.keyAt(iwa)).replace(',', '_');
                            str2 = WAKEUP_ALARM_DATA;
                            objArr2 = new Object[WIFI_SUPPL_STATE_INTERFACE_DISABLED];
                            objArr2[WIFI_SUPPL_STATE_INVALID] = name;
                            objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Integer.valueOf(count);
                            dumpLine(pw, uid, category, str2, objArr2);
                        }
                        serviceStats = ps2.getServiceStats();
                        while (isvc >= 0) {
                            ss = (Serv) serviceStats.valueAt(isvc);
                            startTime = ss.getStartTime(batteryUptime, which);
                            starts = ss.getStarts(which);
                            launches = ss.getLaunches(which);
                            if (launches != 0) {
                                str2 = APK_DATA;
                                objArr2 = new Object[WIFI_SUPPL_STATE_ASSOCIATING];
                                objArr2[WIFI_SUPPL_STATE_INVALID] = Integer.valueOf(wakeups);
                                objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = packageStats.keyAt(ipkg);
                                objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = serviceStats.keyAt(isvc);
                                objArr2[WIFI_SUPPL_STATE_INACTIVE] = Long.valueOf(startTime / 1000);
                                objArr2[WIFI_SUPPL_STATE_SCANNING] = Integer.valueOf(starts);
                                objArr2[WIFI_SUPPL_STATE_AUTHENTICATING] = Integer.valueOf(launches);
                                dumpLine(pw, uid, category, str2, objArr2);
                            } else {
                            }
                        }
                    }
                }
                str2 = NETWORK_DATA;
                objArr2 = new Object[WIFI_SUPPL_STATE_UNINITIALIZED];
                objArr2[WIFI_SUPPL_STATE_INVALID] = Long.valueOf(mobileBytesRx);
                objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf(mobileBytesTx);
                objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Long.valueOf(wifiBytesRx);
                objArr2[WIFI_SUPPL_STATE_INACTIVE] = Long.valueOf(wifiBytesTx);
                objArr2[WIFI_SUPPL_STATE_SCANNING] = Long.valueOf(mobilePacketsRx);
                objArr2[WIFI_SUPPL_STATE_AUTHENTICATING] = Long.valueOf(mobilePacketsTx);
                objArr2[WIFI_SUPPL_STATE_ASSOCIATING] = Long.valueOf(wifiPacketsRx);
                objArr2[WIFI_SUPPL_STATE_ASSOCIATED] = Long.valueOf(wifiPacketsTx);
                objArr2[WIFI_SUPPL_STATE_FOUR_WAY_HANDSHAKE] = Long.valueOf(mobileActiveTime);
                objArr2[WIFI_SUPPL_STATE_GROUP_HANDSHAKE] = Integer.valueOf(mobileActiveCount);
                objArr2[WIFI_SUPPL_STATE_COMPLETED] = Long.valueOf(btBytesRx);
                objArr2[WIFI_SUPPL_STATE_DORMANT] = Long.valueOf(btBytesTx);
                dumpLine(pw, uid, category, str2, objArr2);
                dumpControllerActivityLine(pw, uid, category, MODEM_CONTROLLER_DATA, u.getModemControllerActivity(), which);
                fullWifiLockOnTime = u.getFullWifiLockTime(rawRealtime, which);
                wifiScanTime = u.getWifiScanTime(rawRealtime, which);
                wifiScanCount = u.getWifiScanCount(which);
                uidWifiRunningTime = u.getWifiRunningTime(rawRealtime, which);
                if (uidWifiRunningTime != 0) {
                    str2 = WIFI_DATA;
                    objArr2 = new Object[WIFI_SUPPL_STATE_ASSOCIATED];
                    objArr2[WIFI_SUPPL_STATE_INVALID] = Long.valueOf(fullWifiLockOnTime);
                    objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf(wifiScanTime);
                    objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Long.valueOf(uidWifiRunningTime);
                    objArr2[WIFI_SUPPL_STATE_INACTIVE] = Integer.valueOf(wifiScanCount);
                    objArr2[WIFI_SUPPL_STATE_SCANNING] = Integer.valueOf(WIFI_SUPPL_STATE_INVALID);
                    objArr2[WIFI_SUPPL_STATE_AUTHENTICATING] = Integer.valueOf(WIFI_SUPPL_STATE_INVALID);
                    objArr2[WIFI_SUPPL_STATE_ASSOCIATING] = Integer.valueOf(WIFI_SUPPL_STATE_INVALID);
                    dumpLine(pw, uid, category, str2, objArr2);
                }
                dumpControllerActivityLine(pw, uid, category, WIFI_CONTROLLER_DATA, u.getWifiControllerActivity(), which);
                dumpTimer(pw, uid, category, BLUETOOTH_MISC_DATA, u.getBluetoothScanTimer(), rawRealtime, which);
                dumpControllerActivityLine(pw, uid, category, BLUETOOTH_CONTROLLER_DATA, u.getBluetoothControllerActivity(), which);
                if (u.hasUserActivity()) {
                    args = new Object[WIFI_SUPPL_STATE_SCANNING];
                    hasData = LOCAL_LOGV;
                    for (i = WIFI_SUPPL_STATE_INVALID; i < WIFI_SUPPL_STATE_SCANNING; i += WIFI_SUPPL_STATE_DISCONNECTED) {
                        val = u.getUserActivityCount(i, which);
                        args[i] = Integer.valueOf(val);
                        if (val == 0) {
                            hasData = true;
                        }
                    }
                    if (hasData) {
                        dumpLine(pw, uid, category, USER_ACTIVITY_DATA, args);
                    }
                }
                wakelocks = u.getWakelockStats();
                for (iw = wakelocks.size() - 1; iw >= 0; iw--) {
                    wl = (Wakelock) wakelocks.valueAt(iw);
                    linePrefix = ProxyInfo.LOCAL_EXCL_LIST;
                    stringBuilder.setLength(WIFI_SUPPL_STATE_INVALID);
                    linePrefix = printWakeLockCheckin(stringBuilder, wl.getWakeTime(WIFI_SUPPL_STATE_INTERFACE_DISABLED), rawRealtime, "w", which, printWakeLockCheckin(stringBuilder, wl.getWakeTime(WIFI_SUPPL_STATE_INVALID), rawRealtime, TtmlUtils.TAG_P, which, printWakeLockCheckin(stringBuilder, wl.getWakeTime(WIFI_SUPPL_STATE_DISCONNECTED), rawRealtime, FullBackup.FILES_TREE_TOKEN, which, linePrefix)));
                    if (stringBuilder.length() <= 0) {
                        name = (String) wakelocks.keyAt(iw);
                        if (name.indexOf(44) >= 0) {
                            name = name.replace(',', '_');
                        }
                        str2 = WAKELOCK_DATA;
                        objArr2 = new Object[WIFI_SUPPL_STATE_INTERFACE_DISABLED];
                        objArr2[WIFI_SUPPL_STATE_INVALID] = name;
                        objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = stringBuilder.toString();
                        dumpLine(pw, uid, category, str2, objArr2);
                    }
                }
                syncs = u.getSyncStats();
                for (isy = syncs.size() - 1; isy >= 0; isy--) {
                    timer = (Timer) syncs.valueAt(isy);
                    totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                    count = timer.getCountLocked(which);
                    if (totalTime == 0) {
                        str2 = SYNC_DATA;
                        objArr2 = new Object[WIFI_SUPPL_STATE_INACTIVE];
                        objArr2[WIFI_SUPPL_STATE_INVALID] = syncs.keyAt(isy);
                        objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf(totalTime);
                        objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Integer.valueOf(count);
                        dumpLine(pw, uid, category, str2, objArr2);
                    }
                }
                jobs = u.getJobStats();
                for (ij = jobs.size() - 1; ij >= 0; ij--) {
                    timer = (Timer) jobs.valueAt(ij);
                    totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                    count = timer.getCountLocked(which);
                    if (totalTime == 0) {
                        str2 = JOB_DATA;
                        objArr2 = new Object[WIFI_SUPPL_STATE_INACTIVE];
                        objArr2[WIFI_SUPPL_STATE_INVALID] = jobs.keyAt(ij);
                        objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf(totalTime);
                        objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Integer.valueOf(count);
                        dumpLine(pw, uid, category, str2, objArr2);
                    }
                }
                dumpTimer(pw, uid, category, FLASHLIGHT_DATA, u.getFlashlightTurnedOnTimer(), rawRealtime, which);
                dumpTimer(pw, uid, category, CAMERA_DATA, u.getCameraTurnedOnTimer(), rawRealtime, which);
                dumpTimer(pw, uid, category, VIDEO_DATA, u.getVideoTurnedOnTimer(), rawRealtime, which);
                dumpTimer(pw, uid, category, AUDIO_DATA, u.getAudioTurnedOnTimer(), rawRealtime, which);
                sensors = u.getSensorStats();
                NSE = sensors.size();
                for (ise = WIFI_SUPPL_STATE_INVALID; ise < NSE; ise += WIFI_SUPPL_STATE_DISCONNECTED) {
                    se = (Sensor) sensors.valueAt(ise);
                    sensorNumber = sensors.keyAt(ise);
                    timer = se.getSensorTime();
                    if (timer == null) {
                        totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                        count = timer.getCountLocked(which);
                        if (totalTime == 0) {
                            str2 = SENSOR_DATA;
                            objArr2 = new Object[WIFI_SUPPL_STATE_INACTIVE];
                            objArr2[WIFI_SUPPL_STATE_INVALID] = Integer.valueOf(sensorNumber);
                            objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf(totalTime);
                            objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Integer.valueOf(count);
                            dumpLine(pw, uid, category, str2, objArr2);
                        }
                    }
                }
                dumpTimer(pw, uid, category, VIBRATOR_DATA, u.getVibratorOnTimer(), rawRealtime, which);
                dumpTimer(pw, uid, category, FOREGROUND_DATA, u.getForegroundActivityTimer(), rawRealtime, which);
                stateTimes = new Object[WIFI_SUPPL_STATE_ASSOCIATING];
                totalStateTime = 0;
                for (ips = WIFI_SUPPL_STATE_INVALID; ips < WIFI_SUPPL_STATE_ASSOCIATING; ips += WIFI_SUPPL_STATE_DISCONNECTED) {
                    time = u.getProcessStateTime(ips, rawRealtime, which);
                    totalStateTime += time;
                    stateTimes[ips] = Long.valueOf((500 + time) / 1000);
                }
                if (totalStateTime > 0) {
                    dumpLine(pw, uid, category, STATE_TIME_DATA, stateTimes);
                }
                userCpuTimeUs = u.getUserCpuTimeUs(which);
                systemCpuTimeUs = u.getSystemCpuTimeUs(which);
                powerCpuMaUs = u.getCpuPowerMaUs(which);
                if (powerCpuMaUs > 0) {
                    str2 = CPU_DATA;
                    objArr2 = new Object[WIFI_SUPPL_STATE_INACTIVE];
                    objArr2[WIFI_SUPPL_STATE_INVALID] = Long.valueOf(userCpuTimeUs / 1000);
                    objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf(systemCpuTimeUs / 1000);
                    objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Long.valueOf(powerCpuMaUs / 1000);
                    dumpLine(pw, uid, category, str2, objArr2);
                }
                processStats = u.getProcessStats();
                while (ipr >= 0) {
                    ps = (Proc) processStats.valueAt(ipr);
                    userMillis = ps.getUserTime(which);
                    systemMillis = ps.getSystemTime(which);
                    foregroundMillis = ps.getForegroundTime(which);
                    starts = ps.getStarts(which);
                    numCrashes = ps.getNumCrashes(which);
                    numAnrs = ps.getNumAnrs(which);
                    if (numCrashes != 0) {
                    } else {
                        str2 = PROCESS_DATA;
                        objArr2 = new Object[WIFI_SUPPL_STATE_ASSOCIATED];
                        objArr2[WIFI_SUPPL_STATE_INVALID] = processStats.keyAt(ipr);
                        objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Long.valueOf(userMillis);
                        objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = Long.valueOf(systemMillis);
                        objArr2[WIFI_SUPPL_STATE_INACTIVE] = Long.valueOf(foregroundMillis);
                        objArr2[WIFI_SUPPL_STATE_SCANNING] = Integer.valueOf(starts);
                        objArr2[WIFI_SUPPL_STATE_AUTHENTICATING] = Integer.valueOf(numAnrs);
                        objArr2[WIFI_SUPPL_STATE_ASSOCIATING] = Integer.valueOf(numCrashes);
                        dumpLine(pw, uid, category, str2, objArr2);
                    }
                }
                packageStats = u.getPackageStats();
                for (ipkg = packageStats.size() - 1; ipkg >= 0; ipkg--) {
                    ps2 = (Pkg) packageStats.valueAt(ipkg);
                    wakeups = WIFI_SUPPL_STATE_INVALID;
                    alarms = ps2.getWakeupAlarmStats();
                    for (iwa = alarms.size() - 1; iwa >= 0; iwa--) {
                        count = ((Counter) alarms.valueAt(iwa)).getCountLocked(which);
                        wakeups += count;
                        name = ((String) alarms.keyAt(iwa)).replace(',', '_');
                        str2 = WAKEUP_ALARM_DATA;
                        objArr2 = new Object[WIFI_SUPPL_STATE_INTERFACE_DISABLED];
                        objArr2[WIFI_SUPPL_STATE_INVALID] = name;
                        objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = Integer.valueOf(count);
                        dumpLine(pw, uid, category, str2, objArr2);
                    }
                    serviceStats = ps2.getServiceStats();
                    while (isvc >= 0) {
                        ss = (Serv) serviceStats.valueAt(isvc);
                        startTime = ss.getStartTime(batteryUptime, which);
                        starts = ss.getStarts(which);
                        launches = ss.getLaunches(which);
                        if (launches != 0) {
                        } else {
                            str2 = APK_DATA;
                            objArr2 = new Object[WIFI_SUPPL_STATE_ASSOCIATING];
                            objArr2[WIFI_SUPPL_STATE_INVALID] = Integer.valueOf(wakeups);
                            objArr2[WIFI_SUPPL_STATE_DISCONNECTED] = packageStats.keyAt(ipkg);
                            objArr2[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = serviceStats.keyAt(isvc);
                            objArr2[WIFI_SUPPL_STATE_INACTIVE] = Long.valueOf(startTime / 1000);
                            objArr2[WIFI_SUPPL_STATE_SCANNING] = Integer.valueOf(starts);
                            objArr2[WIFI_SUPPL_STATE_AUTHENTICATING] = Integer.valueOf(launches);
                            dumpLine(pw, uid, category, str2, objArr2);
                        }
                    }
                }
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

    public final void dumpLocked(Context context, PrintWriter pw, String prefix, int which, int reqUid, boolean wifiOnly) {
        int i;
        int iu;
        BatterySipper bs;
        long totalTime;
        Timer timer;
        long rawUptime = SystemClock.uptimeMillis() * 1000;
        long rawRealtime = SystemClock.elapsedRealtime() * 1000;
        long batteryUptime = getBatteryUptime(rawUptime);
        long whichBatteryUptime = computeBatteryUptime(rawUptime, which);
        long whichBatteryRealtime = computeBatteryRealtime(rawRealtime, which);
        long totalRealtime = computeRealtime(rawRealtime, which);
        long totalUptime = computeUptime(rawUptime, which);
        long whichBatteryScreenOffUptime = computeBatteryScreenOffUptime(rawUptime, which);
        long whichBatteryScreenOffRealtime = computeBatteryScreenOffRealtime(rawRealtime, which);
        long batteryTimeRemaining = computeBatteryTimeRemaining(rawRealtime);
        long chargeTimeRemaining = computeChargeTimeRemaining(rawRealtime);
        StringBuilder sb = new StringBuilder(KeymasterDefs.KM_ALGORITHM_HMAC);
        SparseArray<? extends Uid> uidStats = getUidStats();
        int NU = uidStats.size();
        int estimatedBatteryCapacity = getEstimatedBatteryCapacity();
        if (estimatedBatteryCapacity > 0) {
            sb.setLength(WIFI_SUPPL_STATE_INVALID);
            sb.append(prefix);
            sb.append("  Estimated battery capacity: ");
            sb.append(BatteryStatsHelper.makemAh((double) estimatedBatteryCapacity));
            sb.append(" mAh");
            pw.println(sb.toString());
        }
        sb.setLength(WIFI_SUPPL_STATE_INVALID);
        sb.append(prefix);
        sb.append("  Time on battery: ");
        formatTimeMs(sb, whichBatteryRealtime / 1000);
        sb.append("(");
        sb.append(formatRatioLocked(whichBatteryRealtime, totalRealtime));
        sb.append(") realtime, ");
        formatTimeMs(sb, whichBatteryUptime / 1000);
        sb.append("(");
        sb.append(formatRatioLocked(whichBatteryUptime, totalRealtime));
        sb.append(") uptime");
        pw.println(sb.toString());
        sb.setLength(WIFI_SUPPL_STATE_INVALID);
        sb.append(prefix);
        sb.append("  Time on battery screen off: ");
        formatTimeMs(sb, whichBatteryScreenOffRealtime / 1000);
        sb.append("(");
        sb.append(formatRatioLocked(whichBatteryScreenOffRealtime, totalRealtime));
        sb.append(") realtime, ");
        formatTimeMs(sb, whichBatteryScreenOffUptime / 1000);
        sb.append("(");
        sb.append(formatRatioLocked(whichBatteryScreenOffUptime, totalRealtime));
        sb.append(") uptime");
        pw.println(sb.toString());
        sb.setLength(WIFI_SUPPL_STATE_INVALID);
        sb.append(prefix);
        sb.append("  Total run time: ");
        formatTimeMs(sb, totalRealtime / 1000);
        sb.append("realtime, ");
        formatTimeMs(sb, totalUptime / 1000);
        sb.append("uptime");
        pw.println(sb.toString());
        if (batteryTimeRemaining >= 0) {
            sb.setLength(WIFI_SUPPL_STATE_INVALID);
            sb.append(prefix);
            sb.append("  Battery time remaining: ");
            formatTimeMs(sb, batteryTimeRemaining / 1000);
            pw.println(sb.toString());
        }
        if (chargeTimeRemaining >= 0) {
            sb.setLength(WIFI_SUPPL_STATE_INVALID);
            sb.append(prefix);
            sb.append("  Charge time remaining: ");
            formatTimeMs(sb, chargeTimeRemaining / 1000);
            pw.println(sb.toString());
        }
        long dischargeCount = getDischargeCoulombCounter().getCountLocked(which);
        if (dischargeCount >= 0) {
            sb.setLength(WIFI_SUPPL_STATE_INVALID);
            sb.append(prefix);
            sb.append("  Discharge: ");
            sb.append(BatteryStatsHelper.makemAh(((double) dischargeCount) / 1000.0d));
            sb.append(" mAh");
            pw.println(sb.toString());
        }
        long dischargeScreenOffCount = getDischargeScreenOffCoulombCounter().getCountLocked(which);
        if (dischargeScreenOffCount >= 0) {
            sb.setLength(WIFI_SUPPL_STATE_INVALID);
            sb.append(prefix);
            sb.append("  Screen off discharge: ");
            sb.append(BatteryStatsHelper.makemAh(((double) dischargeScreenOffCount) / 1000.0d));
            sb.append(" mAh");
            pw.println(sb.toString());
        }
        long dischargeScreenOnCount = dischargeCount - dischargeScreenOffCount;
        if (dischargeScreenOnCount >= 0) {
            sb.setLength(WIFI_SUPPL_STATE_INVALID);
            sb.append(prefix);
            sb.append("  Screen on discharge: ");
            sb.append(BatteryStatsHelper.makemAh(((double) dischargeScreenOnCount) / 1000.0d));
            sb.append(" mAh");
            pw.println(sb.toString());
        }
        pw.print("  Start clock time: ");
        pw.println(DateFormat.format("yyyy-MM-dd-HH-mm-ss", getStartClockTime()).toString());
        long screenOnTime = getScreenOnTime(rawRealtime, which);
        long interactiveTime = getInteractiveTime(rawRealtime, which);
        long powerSaveModeEnabledTime = getPowerSaveModeEnabledTime(rawRealtime, which);
        long deviceIdleModeLightTime = getDeviceIdleModeTime(WIFI_SUPPL_STATE_DISCONNECTED, rawRealtime, which);
        long deviceIdleModeFullTime = getDeviceIdleModeTime(WIFI_SUPPL_STATE_INTERFACE_DISABLED, rawRealtime, which);
        long deviceLightIdlingTime = getDeviceIdlingTime(WIFI_SUPPL_STATE_DISCONNECTED, rawRealtime, which);
        long deviceIdlingTime = getDeviceIdlingTime(WIFI_SUPPL_STATE_INTERFACE_DISABLED, rawRealtime, which);
        long phoneOnTime = getPhoneOnTime(rawRealtime, which);
        long wifiRunningTime = getGlobalWifiRunningTime(rawRealtime, which);
        long wifiOnTime = getWifiOnTime(rawRealtime, which);
        sb.setLength(WIFI_SUPPL_STATE_INVALID);
        sb.append(prefix);
        sb.append("  Screen on: ");
        formatTimeMs(sb, screenOnTime / 1000);
        sb.append("(");
        sb.append(formatRatioLocked(screenOnTime, whichBatteryRealtime));
        sb.append(") ");
        sb.append(getScreenOnCount(which));
        sb.append("x, Interactive: ");
        formatTimeMs(sb, interactiveTime / 1000);
        sb.append("(");
        sb.append(formatRatioLocked(interactiveTime, whichBatteryRealtime));
        sb.append(")");
        pw.println(sb.toString());
        sb.setLength(WIFI_SUPPL_STATE_INVALID);
        sb.append(prefix);
        sb.append("  Screen brightnesses:");
        boolean didOne = LOCAL_LOGV;
        for (i = WIFI_SUPPL_STATE_INVALID; i < WIFI_SUPPL_STATE_AUTHENTICATING; i += WIFI_SUPPL_STATE_DISCONNECTED) {
            long time = getScreenBrightnessTime(i, rawRealtime, which);
            if (time != 0) {
                sb.append("\n    ");
                sb.append(prefix);
                didOne = true;
                sb.append(SCREEN_BRIGHTNESS_NAMES[i]);
                sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                formatTimeMs(sb, time / 1000);
                sb.append("(");
                sb.append(formatRatioLocked(time, screenOnTime));
                sb.append(")");
            }
        }
        if (!didOne) {
            sb.append(" (no activity)");
        }
        pw.println(sb.toString());
        if (powerSaveModeEnabledTime != 0) {
            sb.setLength(WIFI_SUPPL_STATE_INVALID);
            sb.append(prefix);
            sb.append("  Power save mode enabled: ");
            formatTimeMs(sb, powerSaveModeEnabledTime / 1000);
            sb.append("(");
            sb.append(formatRatioLocked(powerSaveModeEnabledTime, whichBatteryRealtime));
            sb.append(")");
            pw.println(sb.toString());
        }
        if (deviceLightIdlingTime != 0) {
            sb.setLength(WIFI_SUPPL_STATE_INVALID);
            sb.append(prefix);
            sb.append("  Device light idling: ");
            formatTimeMs(sb, deviceLightIdlingTime / 1000);
            sb.append("(");
            sb.append(formatRatioLocked(deviceLightIdlingTime, whichBatteryRealtime));
            sb.append(") ");
            sb.append(getDeviceIdlingCount(WIFI_SUPPL_STATE_DISCONNECTED, which));
            sb.append("x");
            pw.println(sb.toString());
        }
        if (deviceIdleModeLightTime != 0) {
            sb.setLength(WIFI_SUPPL_STATE_INVALID);
            sb.append(prefix);
            sb.append("  Idle mode light time: ");
            formatTimeMs(sb, deviceIdleModeLightTime / 1000);
            sb.append("(");
            sb.append(formatRatioLocked(deviceIdleModeLightTime, whichBatteryRealtime));
            sb.append(") ");
            sb.append(getDeviceIdleModeCount(WIFI_SUPPL_STATE_DISCONNECTED, which));
            sb.append("x");
            sb.append(" -- longest ");
            formatTimeMs(sb, getLongestDeviceIdleModeTime(WIFI_SUPPL_STATE_DISCONNECTED));
            pw.println(sb.toString());
        }
        if (deviceIdlingTime != 0) {
            sb.setLength(WIFI_SUPPL_STATE_INVALID);
            sb.append(prefix);
            sb.append("  Device full idling: ");
            formatTimeMs(sb, deviceIdlingTime / 1000);
            sb.append("(");
            sb.append(formatRatioLocked(deviceIdlingTime, whichBatteryRealtime));
            sb.append(") ");
            sb.append(getDeviceIdlingCount(WIFI_SUPPL_STATE_INTERFACE_DISABLED, which));
            sb.append("x");
            pw.println(sb.toString());
        }
        if (deviceIdleModeFullTime != 0) {
            sb.setLength(WIFI_SUPPL_STATE_INVALID);
            sb.append(prefix);
            sb.append("  Idle mode full time: ");
            formatTimeMs(sb, deviceIdleModeFullTime / 1000);
            sb.append("(");
            sb.append(formatRatioLocked(deviceIdleModeFullTime, whichBatteryRealtime));
            sb.append(") ");
            sb.append(getDeviceIdleModeCount(WIFI_SUPPL_STATE_INTERFACE_DISABLED, which));
            sb.append("x");
            sb.append(" -- longest ");
            formatTimeMs(sb, getLongestDeviceIdleModeTime(WIFI_SUPPL_STATE_INTERFACE_DISABLED));
            pw.println(sb.toString());
        }
        if (phoneOnTime != 0) {
            sb.setLength(WIFI_SUPPL_STATE_INVALID);
            sb.append(prefix);
            sb.append("  Active phone call: ");
            formatTimeMs(sb, phoneOnTime / 1000);
            sb.append("(");
            sb.append(formatRatioLocked(phoneOnTime, whichBatteryRealtime));
            sb.append(") ");
            sb.append(getPhoneOnCount(which));
            sb.append("x");
        }
        int connChanges = getNumConnectivityChange(which);
        if (connChanges != 0) {
            pw.print(prefix);
            pw.print("  Connectivity changes: ");
            pw.println(connChanges);
        }
        long fullWakeLockTimeTotalMicros = 0;
        long partialWakeLockTimeTotalMicros = 0;
        ArrayList<TimerEntry> timers = new ArrayList();
        for (iu = WIFI_SUPPL_STATE_INVALID; iu < NU; iu += WIFI_SUPPL_STATE_DISCONNECTED) {
            int iw;
            Uid u = (Uid) uidStats.valueAt(iu);
            ArrayMap<String, ? extends Wakelock> wakelocks = u.getWakelockStats();
            if (wakelocks != null) {
                for (iw = wakelocks.size() - 1; iw >= 0; iw--) {
                    Wakelock wl = (Wakelock) wakelocks.valueAt(iw);
                    Timer fullWakeTimer = wl.getWakeTime(WIFI_SUPPL_STATE_DISCONNECTED);
                    if (fullWakeTimer != null) {
                        fullWakeLockTimeTotalMicros += fullWakeTimer.getTotalTimeLocked(rawRealtime, which);
                    }
                    Timer partialWakeTimer = wl.getWakeTime(WIFI_SUPPL_STATE_INVALID);
                    if (partialWakeTimer != null) {
                        long totalTimeMicros = partialWakeTimer.getTotalTimeLocked(rawRealtime, which);
                        if (totalTimeMicros > 0) {
                            if (reqUid < 0) {
                                timers.add(new TimerEntry((String) wakelocks.keyAt(iw), u.getUid(), partialWakeTimer, totalTimeMicros));
                            }
                            partialWakeLockTimeTotalMicros += totalTimeMicros;
                        }
                    }
                }
            }
        }
        long mobileRxTotalBytes = getNetworkActivityBytes(WIFI_SUPPL_STATE_INVALID, which);
        long mobileTxTotalBytes = getNetworkActivityBytes(WIFI_SUPPL_STATE_DISCONNECTED, which);
        long wifiRxTotalBytes = getNetworkActivityBytes(WIFI_SUPPL_STATE_INTERFACE_DISABLED, which);
        long wifiTxTotalBytes = getNetworkActivityBytes(WIFI_SUPPL_STATE_INACTIVE, which);
        long mobileRxTotalPackets = getNetworkActivityPackets(WIFI_SUPPL_STATE_INVALID, which);
        long mobileTxTotalPackets = getNetworkActivityPackets(WIFI_SUPPL_STATE_DISCONNECTED, which);
        long wifiRxTotalPackets = getNetworkActivityPackets(WIFI_SUPPL_STATE_INTERFACE_DISABLED, which);
        long wifiTxTotalPackets = getNetworkActivityPackets(WIFI_SUPPL_STATE_INACTIVE, which);
        long btRxTotalBytes = getNetworkActivityBytes(WIFI_SUPPL_STATE_SCANNING, which);
        long btTxTotalBytes = getNetworkActivityBytes(WIFI_SUPPL_STATE_AUTHENTICATING, which);
        if (fullWakeLockTimeTotalMicros != 0) {
            sb.setLength(WIFI_SUPPL_STATE_INVALID);
            sb.append(prefix);
            sb.append("  Total full wakelock time: ");
            formatTimeMsNoSpace(sb, (500 + fullWakeLockTimeTotalMicros) / 1000);
            pw.println(sb.toString());
        }
        if (partialWakeLockTimeTotalMicros != 0) {
            sb.setLength(WIFI_SUPPL_STATE_INVALID);
            sb.append(prefix);
            sb.append("  Total partial wakelock time: ");
            formatTimeMsNoSpace(sb, (500 + partialWakeLockTimeTotalMicros) / 1000);
            pw.println(sb.toString());
        }
        pw.print(prefix);
        pw.print("  Mobile total received: ");
        pw.print(formatBytesLocked(mobileRxTotalBytes));
        pw.print(", sent: ");
        pw.print(formatBytesLocked(mobileTxTotalBytes));
        pw.print(" (packets received ");
        pw.print(mobileRxTotalPackets);
        pw.print(", sent ");
        pw.print(mobileTxTotalPackets);
        pw.println(")");
        sb.setLength(WIFI_SUPPL_STATE_INVALID);
        sb.append(prefix);
        sb.append("  Phone signal levels:");
        didOne = LOCAL_LOGV;
        for (i = WIFI_SUPPL_STATE_INVALID; i < WIFI_SUPPL_STATE_AUTHENTICATING; i += WIFI_SUPPL_STATE_DISCONNECTED) {
            time = getPhoneSignalStrengthTime(i, rawRealtime, which);
            if (time != 0) {
                sb.append("\n    ");
                sb.append(prefix);
                didOne = true;
                sb.append(SignalStrength.SIGNAL_STRENGTH_NAMES[i]);
                sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                formatTimeMs(sb, time / 1000);
                sb.append("(");
                sb.append(formatRatioLocked(time, whichBatteryRealtime));
                sb.append(") ");
                sb.append(getPhoneSignalStrengthCount(i, which));
                sb.append("x");
            }
        }
        if (!didOne) {
            sb.append(" (no activity)");
        }
        pw.println(sb.toString());
        sb.setLength(WIFI_SUPPL_STATE_INVALID);
        sb.append(prefix);
        sb.append("  Signal scanning time: ");
        formatTimeMsNoSpace(sb, getPhoneSignalScanningTime(rawRealtime, which) / 1000);
        pw.println(sb.toString());
        sb.setLength(WIFI_SUPPL_STATE_INVALID);
        sb.append(prefix);
        sb.append("  Radio types:");
        didOne = LOCAL_LOGV;
        for (i = WIFI_SUPPL_STATE_INVALID; i < NUM_DATA_CONNECTION_TYPES; i += WIFI_SUPPL_STATE_DISCONNECTED) {
            time = getPhoneDataConnectionTime(i, rawRealtime, which);
            if (time != 0) {
                sb.append("\n    ");
                sb.append(prefix);
                didOne = true;
                sb.append(DATA_CONNECTION_NAMES[i]);
                sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                formatTimeMs(sb, time / 1000);
                sb.append("(");
                sb.append(formatRatioLocked(time, whichBatteryRealtime));
                sb.append(") ");
                sb.append(getPhoneDataConnectionCount(i, which));
                sb.append("x");
            }
        }
        if (!didOne) {
            sb.append(" (no activity)");
        }
        pw.println(sb.toString());
        sb.setLength(WIFI_SUPPL_STATE_INVALID);
        sb.append(prefix);
        sb.append("  Mobile radio active time: ");
        long mobileActiveTime = getMobileRadioActiveTime(rawRealtime, which);
        formatTimeMs(sb, mobileActiveTime / 1000);
        sb.append("(");
        sb.append(formatRatioLocked(mobileActiveTime, whichBatteryRealtime));
        sb.append(") ");
        sb.append(getMobileRadioActiveCount(which));
        sb.append("x");
        pw.println(sb.toString());
        long mobileActiveUnknownTime = getMobileRadioActiveUnknownTime(which);
        if (mobileActiveUnknownTime != 0) {
            sb.setLength(WIFI_SUPPL_STATE_INVALID);
            sb.append(prefix);
            sb.append("  Mobile radio active unknown time: ");
            formatTimeMs(sb, mobileActiveUnknownTime / 1000);
            sb.append("(");
            sb.append(formatRatioLocked(mobileActiveUnknownTime, whichBatteryRealtime));
            sb.append(") ");
            sb.append(getMobileRadioActiveUnknownCount(which));
            sb.append("x");
            pw.println(sb.toString());
        }
        long mobileActiveAdjustedTime = getMobileRadioActiveAdjustedTime(which);
        if (mobileActiveAdjustedTime != 0) {
            sb.setLength(WIFI_SUPPL_STATE_INVALID);
            sb.append(prefix);
            sb.append("  Mobile radio active adjusted time: ");
            formatTimeMs(sb, mobileActiveAdjustedTime / 1000);
            sb.append("(");
            sb.append(formatRatioLocked(mobileActiveAdjustedTime, whichBatteryRealtime));
            sb.append(")");
            pw.println(sb.toString());
        }
        printControllerActivity(pw, sb, prefix, "Radio", getModemControllerActivity(), which);
        pw.print(prefix);
        pw.print("  Wi-Fi total received: ");
        pw.print(formatBytesLocked(wifiRxTotalBytes));
        pw.print(", sent: ");
        pw.print(formatBytesLocked(wifiTxTotalBytes));
        pw.print(" (packets received ");
        pw.print(wifiRxTotalPackets);
        pw.print(", sent ");
        pw.print(wifiTxTotalPackets);
        pw.println(")");
        sb.setLength(WIFI_SUPPL_STATE_INVALID);
        sb.append(prefix);
        sb.append("  Wifi on: ");
        formatTimeMs(sb, wifiOnTime / 1000);
        sb.append("(");
        sb.append(formatRatioLocked(wifiOnTime, whichBatteryRealtime));
        sb.append("), Wifi running: ");
        formatTimeMs(sb, wifiRunningTime / 1000);
        sb.append("(");
        sb.append(formatRatioLocked(wifiRunningTime, whichBatteryRealtime));
        sb.append(")");
        pw.println(sb.toString());
        sb.setLength(WIFI_SUPPL_STATE_INVALID);
        sb.append(prefix);
        sb.append("  Wifi states:");
        didOne = LOCAL_LOGV;
        for (i = WIFI_SUPPL_STATE_INVALID; i < WIFI_SUPPL_STATE_FOUR_WAY_HANDSHAKE; i += WIFI_SUPPL_STATE_DISCONNECTED) {
            time = getWifiStateTime(i, rawRealtime, which);
            if (time != 0) {
                sb.append("\n    ");
                didOne = true;
                sb.append(WIFI_STATE_NAMES[i]);
                sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                formatTimeMs(sb, time / 1000);
                sb.append("(");
                sb.append(formatRatioLocked(time, whichBatteryRealtime));
                sb.append(") ");
                sb.append(getWifiStateCount(i, which));
                sb.append("x");
            }
        }
        if (!didOne) {
            sb.append(" (no activity)");
        }
        pw.println(sb.toString());
        sb.setLength(WIFI_SUPPL_STATE_INVALID);
        sb.append(prefix);
        sb.append("  Wifi supplicant states:");
        didOne = LOCAL_LOGV;
        for (i = WIFI_SUPPL_STATE_INVALID; i < SYNC; i += WIFI_SUPPL_STATE_DISCONNECTED) {
            time = getWifiSupplStateTime(i, rawRealtime, which);
            if (time != 0) {
                sb.append("\n    ");
                didOne = true;
                sb.append(WIFI_SUPPL_STATE_NAMES[i]);
                sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                formatTimeMs(sb, time / 1000);
                sb.append("(");
                sb.append(formatRatioLocked(time, whichBatteryRealtime));
                sb.append(") ");
                sb.append(getWifiSupplStateCount(i, which));
                sb.append("x");
            }
        }
        if (!didOne) {
            sb.append(" (no activity)");
        }
        pw.println(sb.toString());
        sb.setLength(WIFI_SUPPL_STATE_INVALID);
        sb.append(prefix);
        sb.append("  Wifi signal levels:");
        didOne = LOCAL_LOGV;
        for (i = WIFI_SUPPL_STATE_INVALID; i < WIFI_SUPPL_STATE_AUTHENTICATING; i += WIFI_SUPPL_STATE_DISCONNECTED) {
            time = getWifiSignalStrengthTime(i, rawRealtime, which);
            if (time != 0) {
                sb.append("\n    ");
                sb.append(prefix);
                didOne = true;
                sb.append("level(");
                sb.append(i);
                sb.append(") ");
                formatTimeMs(sb, time / 1000);
                sb.append("(");
                sb.append(formatRatioLocked(time, whichBatteryRealtime));
                sb.append(") ");
                sb.append(getWifiSignalStrengthCount(i, which));
                sb.append("x");
            }
        }
        if (!didOne) {
            sb.append(" (no activity)");
        }
        pw.println(sb.toString());
        printControllerActivity(pw, sb, prefix, "WiFi", getWifiControllerActivity(), which);
        pw.print(prefix);
        pw.print("  Bluetooth total received: ");
        pw.print(formatBytesLocked(btRxTotalBytes));
        pw.print(", sent: ");
        pw.println(formatBytesLocked(btTxTotalBytes));
        long bluetoothScanTimeMs = getBluetoothScanTime(rawRealtime, which) / 1000;
        sb.setLength(WIFI_SUPPL_STATE_INVALID);
        sb.append(prefix);
        sb.append("  Bluetooth scan time: ");
        formatTimeMs(sb, bluetoothScanTimeMs);
        pw.println(sb.toString());
        printControllerActivity(pw, sb, prefix, "Bluetooth", getBluetoothControllerActivity(), which);
        pw.println();
        if (which == WIFI_SUPPL_STATE_INTERFACE_DISABLED) {
            if (getIsOnBattery()) {
                pw.print(prefix);
                pw.println("  Device is currently unplugged");
                pw.print(prefix);
                pw.print("    Discharge cycle start level: ");
                pw.println(getDischargeStartLevel());
                pw.print(prefix);
                pw.print("    Discharge cycle current level: ");
                pw.println(getDischargeCurrentLevel());
            } else {
                pw.print(prefix);
                pw.println("  Device is currently plugged into power");
                pw.print(prefix);
                pw.print("    Last discharge cycle start level: ");
                pw.println(getDischargeStartLevel());
                pw.print(prefix);
                pw.print("    Last discharge cycle end level: ");
                pw.println(getDischargeCurrentLevel());
            }
            pw.print(prefix);
            pw.print("    Amount discharged while screen on: ");
            pw.println(getDischargeAmountScreenOn());
            pw.print(prefix);
            pw.print("    Amount discharged while screen off: ");
            pw.println(getDischargeAmountScreenOff());
            pw.println(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        } else {
            pw.print(prefix);
            pw.println("  Device battery use since last full charge");
            pw.print(prefix);
            pw.print("    Amount discharged (lower bound): ");
            pw.println(getLowDischargeAmountSinceCharge());
            pw.print(prefix);
            pw.print("    Amount discharged (upper bound): ");
            pw.println(getHighDischargeAmountSinceCharge());
            pw.print(prefix);
            pw.print("    Amount discharged while screen on: ");
            pw.println(getDischargeAmountScreenOnSinceCharge());
            pw.print(prefix);
            pw.print("    Amount discharged while screen off: ");
            pw.println(getDischargeAmountScreenOffSinceCharge());
            pw.println();
        }
        BatteryStatsHelper batteryStatsHelper = new BatteryStatsHelper(context, LOCAL_LOGV, wifiOnly);
        batteryStatsHelper.create(this);
        batteryStatsHelper.refreshStats(which, -1);
        List<BatterySipper> sippers = batteryStatsHelper.getUsageList();
        if (sippers != null && sippers.size() > 0) {
            pw.print(prefix);
            pw.println("  Estimated power use (mAh):");
            pw.print(prefix);
            pw.print("    Capacity: ");
            printmAh(pw, batteryStatsHelper.getPowerProfile().getBatteryCapacity());
            pw.print(", Computed drain: ");
            printmAh(pw, batteryStatsHelper.getComputedPower());
            pw.print(", actual drain: ");
            printmAh(pw, batteryStatsHelper.getMinDrainedPower());
            if (batteryStatsHelper.getMinDrainedPower() != batteryStatsHelper.getMaxDrainedPower()) {
                pw.print("-");
                printmAh(pw, batteryStatsHelper.getMaxDrainedPower());
            }
            pw.println();
            for (i = WIFI_SUPPL_STATE_INVALID; i < sippers.size(); i += WIFI_SUPPL_STATE_DISCONNECTED) {
                bs = (BatterySipper) sippers.get(i);
                pw.print(prefix);
                switch (-getcom-android-internal-os-BatterySipper$DrainTypeSwitchesValues()[bs.drainType.ordinal()]) {
                    case WIFI_SUPPL_STATE_DISCONNECTED /*1*/:
                        pw.print("    Uid ");
                        UserHandle.formatUid(pw, bs.uidObj.getUid());
                        pw.print(": ");
                        break;
                    case WIFI_SUPPL_STATE_INTERFACE_DISABLED /*2*/:
                        pw.print("    Bluetooth: ");
                        break;
                    case WIFI_SUPPL_STATE_INACTIVE /*3*/:
                        pw.print("    Camera: ");
                        break;
                    case WIFI_SUPPL_STATE_SCANNING /*4*/:
                        pw.print("    Cell standby: ");
                        break;
                    case WIFI_SUPPL_STATE_AUTHENTICATING /*5*/:
                        pw.print("    Flashlight: ");
                        break;
                    case WIFI_SUPPL_STATE_ASSOCIATING /*6*/:
                        pw.print("    Idle: ");
                        break;
                    case WIFI_SUPPL_STATE_ASSOCIATED /*7*/:
                        pw.print("    Over-counted: ");
                        break;
                    case WIFI_SUPPL_STATE_FOUR_WAY_HANDSHAKE /*8*/:
                        pw.print("    Phone calls: ");
                        break;
                    case WIFI_SUPPL_STATE_GROUP_HANDSHAKE /*9*/:
                        pw.print("    Screen: ");
                        break;
                    case WIFI_SUPPL_STATE_COMPLETED /*10*/:
                        pw.print("    Unaccounted: ");
                        break;
                    case WIFI_SUPPL_STATE_DORMANT /*11*/:
                        pw.print("    User ");
                        pw.print(bs.userId);
                        pw.print(": ");
                        break;
                    case WIFI_SUPPL_STATE_UNINITIALIZED /*12*/:
                        pw.print("    Wifi: ");
                        break;
                    default:
                        pw.print("    ???: ");
                        break;
                }
                printmAh(pw, bs.totalPowerMah);
                if (bs.usagePowerMah != bs.totalPowerMah) {
                    pw.print(" (");
                    if (bs.usagePowerMah != 0.0d) {
                        pw.print(" usage=");
                        printmAh(pw, bs.usagePowerMah);
                    }
                    if (bs.cpuPowerMah != 0.0d) {
                        pw.print(" cpu=");
                        printmAh(pw, bs.cpuPowerMah);
                    }
                    if (bs.wakeLockPowerMah != 0.0d) {
                        pw.print(" wake=");
                        printmAh(pw, bs.wakeLockPowerMah);
                    }
                    if (bs.mobileRadioPowerMah != 0.0d) {
                        pw.print(" radio=");
                        printmAh(pw, bs.mobileRadioPowerMah);
                    }
                    if (bs.wifiPowerMah != 0.0d) {
                        pw.print(" wifi=");
                        printmAh(pw, bs.wifiPowerMah);
                    }
                    if (bs.bluetoothPowerMah != 0.0d) {
                        pw.print(" bt=");
                        printmAh(pw, bs.bluetoothPowerMah);
                    }
                    if (bs.gpsPowerMah != 0.0d) {
                        pw.print(" gps=");
                        printmAh(pw, bs.gpsPowerMah);
                    }
                    if (bs.sensorPowerMah != 0.0d) {
                        pw.print(" sensor=");
                        printmAh(pw, bs.sensorPowerMah);
                    }
                    if (bs.cameraPowerMah != 0.0d) {
                        pw.print(" camera=");
                        printmAh(pw, bs.cameraPowerMah);
                    }
                    if (bs.flashlightPowerMah != 0.0d) {
                        pw.print(" flash=");
                        printmAh(pw, bs.flashlightPowerMah);
                    }
                    pw.print(" )");
                }
                pw.println();
            }
            pw.println();
        }
        sippers = batteryStatsHelper.getMobilemsppList();
        if (sippers != null && sippers.size() > 0) {
            pw.print(prefix);
            pw.println("  Per-app mobile ms per packet:");
            totalTime = 0;
            for (i = WIFI_SUPPL_STATE_INVALID; i < sippers.size(); i += WIFI_SUPPL_STATE_DISCONNECTED) {
                bs = (BatterySipper) sippers.get(i);
                sb.setLength(WIFI_SUPPL_STATE_INVALID);
                sb.append(prefix);
                sb.append("    Uid ");
                UserHandle.formatUid(sb, bs.uidObj.getUid());
                sb.append(": ");
                sb.append(BatteryStatsHelper.makemAh(bs.mobilemspp));
                sb.append(" (");
                sb.append(bs.mobileRxPackets + bs.mobileTxPackets);
                sb.append(" packets over ");
                formatTimeMsNoSpace(sb, bs.mobileActive);
                sb.append(") ");
                sb.append(bs.mobileActiveCount);
                sb.append("x");
                pw.println(sb.toString());
                totalTime += bs.mobileActive;
            }
            sb.setLength(WIFI_SUPPL_STATE_INVALID);
            sb.append(prefix);
            sb.append("    TOTAL TIME: ");
            formatTimeMs(sb, totalTime);
            sb.append("(");
            sb.append(formatRatioLocked(totalTime, whichBatteryRealtime));
            sb.append(")");
            pw.println(sb.toString());
            pw.println();
        }
        Comparator<TimerEntry> anonymousClass1 = new Comparator<TimerEntry>() {
            public int compare(TimerEntry lhs, TimerEntry rhs) {
                long lhsTime = lhs.mTime;
                long rhsTime = rhs.mTime;
                if (lhsTime < rhsTime) {
                    return BatteryStats.WIFI_SUPPL_STATE_DISCONNECTED;
                }
                if (lhsTime > rhsTime) {
                    return -1;
                }
                return BatteryStats.WIFI_SUPPL_STATE_INVALID;
            }
        };
        if (reqUid < 0) {
            TimerEntry timer2;
            Map<String, ? extends Timer> kernelWakelocks = getKernelWakelockStats();
            if (kernelWakelocks != null && kernelWakelocks.size() > 0) {
                ArrayList<TimerEntry> ktimers = new ArrayList();
                for (Entry<String, ? extends Timer> ent : kernelWakelocks.entrySet()) {
                    timer = (Timer) ent.getValue();
                    long totalTimeMillis = computeWakeLock(timer, rawRealtime, which);
                    if (totalTimeMillis > 0) {
                        ktimers.add(new TimerEntry((String) ent.getKey(), WIFI_SUPPL_STATE_INVALID, timer, totalTimeMillis));
                    }
                }
                if (ktimers.size() > 0) {
                    Collections.sort(ktimers, anonymousClass1);
                    pw.print(prefix);
                    pw.println("  All kernel wake locks:");
                    for (i = WIFI_SUPPL_STATE_INVALID; i < ktimers.size(); i += WIFI_SUPPL_STATE_DISCONNECTED) {
                        timer2 = (TimerEntry) ktimers.get(i);
                        sb.setLength(WIFI_SUPPL_STATE_INVALID);
                        sb.append(prefix);
                        sb.append("  Kernel Wake lock ");
                        sb.append(timer2.mName);
                        if (!printWakeLock(sb, timer2.mTimer, rawRealtime, null, which, ": ").equals(": ")) {
                            sb.append(" realtime");
                            pw.println(sb.toString());
                        }
                    }
                    pw.println();
                }
            }
            if (timers.size() > 0) {
                Collections.sort(timers, anonymousClass1);
                pw.print(prefix);
                pw.println("  All partial wake locks:");
                for (i = WIFI_SUPPL_STATE_INVALID; i < timers.size(); i += WIFI_SUPPL_STATE_DISCONNECTED) {
                    timer2 = (TimerEntry) timers.get(i);
                    sb.setLength(WIFI_SUPPL_STATE_INVALID);
                    sb.append("  Wake lock ");
                    UserHandle.formatUid(sb, timer2.mId);
                    sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                    sb.append(timer2.mName);
                    printWakeLock(sb, timer2.mTimer, rawRealtime, null, which, ": ");
                    sb.append(" realtime");
                    pw.println(sb.toString());
                }
                timers.clear();
                pw.println();
            }
            Map<String, ? extends Timer> wakeupReasons = getWakeupReasonStats();
            if (wakeupReasons != null && wakeupReasons.size() > 0) {
                pw.print(prefix);
                pw.println("  All wakeup reasons:");
                ArrayList<TimerEntry> reasons = new ArrayList();
                for (Entry<String, ? extends Timer> ent2 : wakeupReasons.entrySet()) {
                    timer = (Timer) ent2.getValue();
                    reasons.add(new TimerEntry((String) ent2.getKey(), WIFI_SUPPL_STATE_INVALID, timer, (long) timer.getCountLocked(which)));
                }
                Collections.sort(reasons, anonymousClass1);
                for (i = WIFI_SUPPL_STATE_INVALID; i < reasons.size(); i += WIFI_SUPPL_STATE_DISCONNECTED) {
                    timer2 = (TimerEntry) reasons.get(i);
                    String str = ": ";
                    sb.setLength(WIFI_SUPPL_STATE_INVALID);
                    sb.append(prefix);
                    sb.append("  Wakeup reason ");
                    sb.append(timer2.mName);
                    printWakeLock(sb, timer2.mTimer, rawRealtime, null, which, ": ");
                    sb.append(" realtime");
                    pw.println(sb.toString());
                }
                pw.println();
            }
        }
        for (iu = WIFI_SUPPL_STATE_INVALID; iu < NU; iu += WIFI_SUPPL_STATE_DISCONNECTED) {
            int uid = uidStats.keyAt(iu);
            if (reqUid < 0 || uid == reqUid || uid == 1000) {
                long packets;
                boolean printTimer;
                boolean hasData;
                int val;
                long totalFullWakelock;
                long totalPartialWakelock;
                long totalWindowWakelock;
                long totalDrawWakelock;
                int countWakelock;
                boolean needComma;
                ArrayMap<String, ? extends Timer> syncs;
                int isy;
                int count;
                ArrayMap<String, ? extends Timer> jobs;
                int ij;
                SparseArray<? extends Sensor> sensors;
                int NSE;
                int ise;
                Sensor se;
                int sensorNumber;
                int handle;
                long totalStateTime;
                int ips;
                long userCpuTimeUs;
                long systemCpuTimeUs;
                long powerCpuMaUs;
                ArrayMap<String, ? extends Proc> processStats;
                int ipr;
                Proc ps;
                long userTime;
                long systemTime;
                long foregroundTime;
                int starts;
                int numCrashes;
                int numAnrs;
                int numExcessive;
                int e;
                ExcessivePower ew;
                boolean hasOne;
                ArrayMap<String, ? extends Pkg> packageStats;
                int ipkg;
                boolean apkActivity;
                Pkg ps2;
                ArrayMap<String, ? extends Counter> alarms;
                int iwa;
                ArrayMap<String, ? extends Serv> serviceStats;
                int isvc;
                Serv ss;
                long startTime;
                int launches;
                u = (Uid) uidStats.valueAt(iu);
                pw.print(prefix);
                pw.print("  ");
                UserHandle.formatUid(pw, uid);
                pw.println(":");
                long mobileRxBytes = u.getNetworkActivityBytes(WIFI_SUPPL_STATE_INVALID, which);
                long mobileTxBytes = u.getNetworkActivityBytes(WIFI_SUPPL_STATE_DISCONNECTED, which);
                long wifiRxBytes = u.getNetworkActivityBytes(WIFI_SUPPL_STATE_INTERFACE_DISABLED, which);
                long wifiTxBytes = u.getNetworkActivityBytes(WIFI_SUPPL_STATE_INACTIVE, which);
                long btRxBytes = u.getNetworkActivityBytes(WIFI_SUPPL_STATE_SCANNING, which);
                long btTxBytes = u.getNetworkActivityBytes(WIFI_SUPPL_STATE_AUTHENTICATING, which);
                long mobileRxPackets = u.getNetworkActivityPackets(WIFI_SUPPL_STATE_INVALID, which);
                long mobileTxPackets = u.getNetworkActivityPackets(WIFI_SUPPL_STATE_DISCONNECTED, which);
                long wifiRxPackets = u.getNetworkActivityPackets(WIFI_SUPPL_STATE_INTERFACE_DISABLED, which);
                long wifiTxPackets = u.getNetworkActivityPackets(WIFI_SUPPL_STATE_INACTIVE, which);
                long uidMobileActiveTime = u.getMobileRadioActiveTime(which);
                int uidMobileActiveCount = u.getMobileRadioActiveCount(which);
                long fullWifiLockOnTime = u.getFullWifiLockTime(rawRealtime, which);
                long wifiScanTime = u.getWifiScanTime(rawRealtime, which);
                int wifiScanCount = u.getWifiScanCount(which);
                long uidWifiRunningTime = u.getWifiRunningTime(rawRealtime, which);
                if (mobileRxBytes <= 0 && mobileTxBytes <= 0 && mobileRxPackets <= 0) {
                    if (mobileTxPackets > 0) {
                    }
                    if (uidMobileActiveTime > 0 || uidMobileActiveCount > 0) {
                        sb.setLength(WIFI_SUPPL_STATE_INVALID);
                        sb.append(prefix);
                        sb.append("    Mobile radio active: ");
                        formatTimeMs(sb, uidMobileActiveTime / 1000);
                        sb.append("(");
                        sb.append(formatRatioLocked(uidMobileActiveTime, mobileActiveTime));
                        sb.append(") ");
                        sb.append(uidMobileActiveCount);
                        sb.append("x");
                        packets = mobileRxPackets + mobileTxPackets;
                        if (packets == 0) {
                            packets = 1;
                        }
                        sb.append(" @ ");
                        sb.append(BatteryStatsHelper.makemAh(((double) (uidMobileActiveTime / 1000)) / ((double) packets)));
                        sb.append(" mspp");
                        pw.println(sb.toString());
                    }
                    printControllerActivityIfInteresting(pw, sb, prefix + "  ", "Modem", u.getModemControllerActivity(), which);
                    if (wifiRxBytes <= 0 && wifiTxBytes <= 0 && wifiRxPackets <= 0) {
                        if (wifiTxPackets > 0) {
                        }
                        if (fullWifiLockOnTime == 0 && wifiScanTime == 0 && wifiScanCount == 0) {
                            if (uidWifiRunningTime != 0) {
                            }
                            printControllerActivityIfInteresting(pw, sb, prefix + "  ", "WiFi", u.getWifiControllerActivity(), which);
                            if (btRxBytes > 0 || btTxBytes > 0) {
                                pw.print(prefix);
                                pw.print("    Bluetooth network: ");
                                pw.print(formatBytesLocked(btRxBytes));
                                pw.print(" received, ");
                                pw.print(formatBytesLocked(btTxBytes));
                                pw.println(" sent");
                            }
                            printTimer = printTimer(pw, sb, u.getBluetoothScanTimer(), rawRealtime, which, prefix, "Bluetooth Scan");
                            if (u.hasUserActivity()) {
                                hasData = LOCAL_LOGV;
                                for (i = WIFI_SUPPL_STATE_INVALID; i < WIFI_SUPPL_STATE_SCANNING; i += WIFI_SUPPL_STATE_DISCONNECTED) {
                                    val = u.getUserActivityCount(i, which);
                                    if (val == 0) {
                                        if (hasData) {
                                            sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                            sb.append("    User activity: ");
                                            hasData = true;
                                        } else {
                                            sb.append(", ");
                                        }
                                        sb.append(val);
                                        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                                        sb.append(Uid.USER_ACTIVITY_TYPES[i]);
                                    }
                                }
                                if (hasData) {
                                    pw.println(sb.toString());
                                }
                            }
                            wakelocks = u.getWakelockStats();
                            totalFullWakelock = 0;
                            totalPartialWakelock = 0;
                            totalWindowWakelock = 0;
                            totalDrawWakelock = 0;
                            countWakelock = WIFI_SUPPL_STATE_INVALID;
                            if (wakelocks != null) {
                                for (iw = wakelocks.size() - 1; iw >= 0; iw--) {
                                    wl = (Wakelock) wakelocks.valueAt(iw);
                                    sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                    sb.append(prefix);
                                    sb.append("    Wake lock ");
                                    sb.append((String) wakelocks.keyAt(iw));
                                    str = printWakeLock(sb, wl.getWakeTime(WAKE_TYPE_DRAW), rawRealtime, "draw", which, printWakeLock(sb, wl.getWakeTime(WIFI_SUPPL_STATE_INTERFACE_DISABLED), rawRealtime, AppAssociate.ASSOC_WINDOW, which, printWakeLock(sb, wl.getWakeTime(WIFI_SUPPL_STATE_INVALID), rawRealtime, "partial", which, printWakeLock(sb, wl.getWakeTime(WIFI_SUPPL_STATE_DISCONNECTED), rawRealtime, "full", which, ": "))));
                                    sb.append(" realtime");
                                    pw.println(sb.toString());
                                    printTimer = true;
                                    countWakelock += WIFI_SUPPL_STATE_DISCONNECTED;
                                    totalFullWakelock += computeWakeLock(wl.getWakeTime(WIFI_SUPPL_STATE_DISCONNECTED), rawRealtime, which);
                                    totalPartialWakelock += computeWakeLock(wl.getWakeTime(WIFI_SUPPL_STATE_INVALID), rawRealtime, which);
                                    totalWindowWakelock += computeWakeLock(wl.getWakeTime(WIFI_SUPPL_STATE_INTERFACE_DISABLED), rawRealtime, which);
                                    totalDrawWakelock += computeWakeLock(wl.getWakeTime(WAKE_TYPE_DRAW), rawRealtime, which);
                                }
                            }
                            if (countWakelock > WIFI_SUPPL_STATE_DISCONNECTED) {
                                if (totalFullWakelock == 0 && totalPartialWakelock == 0) {
                                    if (totalWindowWakelock != 0) {
                                    }
                                }
                                sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                sb.append(prefix);
                                sb.append("    TOTAL wake: ");
                                needComma = LOCAL_LOGV;
                                if (totalFullWakelock != 0) {
                                    needComma = true;
                                    formatTimeMs(sb, totalFullWakelock);
                                    sb.append("full");
                                }
                                if (totalPartialWakelock != 0) {
                                    if (needComma) {
                                        sb.append(", ");
                                    }
                                    needComma = true;
                                    formatTimeMs(sb, totalPartialWakelock);
                                    sb.append("partial");
                                }
                                if (totalWindowWakelock != 0) {
                                    if (needComma) {
                                        sb.append(", ");
                                    }
                                    needComma = true;
                                    formatTimeMs(sb, totalWindowWakelock);
                                    sb.append(AppAssociate.ASSOC_WINDOW);
                                }
                                if (totalDrawWakelock != 0) {
                                    if (needComma) {
                                        sb.append(",");
                                    }
                                    formatTimeMs(sb, totalDrawWakelock);
                                    sb.append("draw");
                                }
                                sb.append(" realtime");
                                pw.println(sb.toString());
                            }
                            syncs = u.getSyncStats();
                            if (syncs != null) {
                                for (isy = syncs.size() - 1; isy >= 0; isy--) {
                                    timer = (Timer) syncs.valueAt(isy);
                                    totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                                    count = timer.getCountLocked(which);
                                    sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                    sb.append(prefix);
                                    sb.append("    Sync ");
                                    sb.append((String) syncs.keyAt(isy));
                                    sb.append(": ");
                                    if (totalTime == 0) {
                                        formatTimeMs(sb, totalTime);
                                        sb.append("realtime (");
                                        sb.append(count);
                                        sb.append(" times)");
                                    } else {
                                        sb.append("(not used)");
                                    }
                                    pw.println(sb.toString());
                                    printTimer = true;
                                }
                            }
                            jobs = u.getJobStats();
                            if (jobs != null) {
                                for (ij = jobs.size() - 1; ij >= 0; ij--) {
                                    timer = (Timer) jobs.valueAt(ij);
                                    totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                                    count = timer.getCountLocked(which);
                                    sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                    sb.append(prefix);
                                    sb.append("    Job ");
                                    sb.append((String) jobs.keyAt(ij));
                                    sb.append(": ");
                                    if (totalTime == 0) {
                                        formatTimeMs(sb, totalTime);
                                        sb.append("realtime (");
                                        sb.append(count);
                                        sb.append(" times)");
                                    } else {
                                        sb.append("(not used)");
                                    }
                                    pw.println(sb.toString());
                                    printTimer = true;
                                }
                            }
                            printTimer = (((printTimer | printTimer(pw, sb, u.getFlashlightTurnedOnTimer(), rawRealtime, which, prefix, "Flashlight")) | printTimer(pw, sb, u.getCameraTurnedOnTimer(), rawRealtime, which, prefix, "Camera")) | printTimer(pw, sb, u.getVideoTurnedOnTimer(), rawRealtime, which, prefix, "Video")) | printTimer(pw, sb, u.getAudioTurnedOnTimer(), rawRealtime, which, prefix, "Audio");
                            sensors = u.getSensorStats();
                            NSE = sensors.size();
                            for (ise = WIFI_SUPPL_STATE_INVALID; ise < NSE; ise += WIFI_SUPPL_STATE_DISCONNECTED) {
                                se = (Sensor) sensors.valueAt(ise);
                                sensorNumber = sensors.keyAt(ise);
                                sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                sb.append(prefix);
                                sb.append("    Sensor ");
                                handle = se.getHandle();
                                if (handle != -10000) {
                                    sb.append("GPS");
                                } else {
                                    sb.append(handle);
                                }
                                sb.append(": ");
                                timer = se.getSensorTime();
                                if (timer == null) {
                                    totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                                    count = timer.getCountLocked(which);
                                    if (totalTime == 0) {
                                        formatTimeMs(sb, totalTime);
                                        sb.append("realtime (");
                                        sb.append(count);
                                        sb.append(" times)");
                                    } else {
                                        sb.append("(not used)");
                                    }
                                } else {
                                    sb.append("(not used)");
                                }
                                pw.println(sb.toString());
                                printTimer = true;
                            }
                            printTimer = (printTimer | printTimer(pw, sb, u.getVibratorOnTimer(), rawRealtime, which, prefix, "Vibrator")) | printTimer(pw, sb, u.getForegroundActivityTimer(), rawRealtime, which, prefix, "Foreground activities");
                            totalStateTime = 0;
                            for (ips = WIFI_SUPPL_STATE_INVALID; ips < WIFI_SUPPL_STATE_ASSOCIATING; ips += WIFI_SUPPL_STATE_DISCONNECTED) {
                                time = u.getProcessStateTime(ips, rawRealtime, which);
                                if (time <= 0) {
                                    totalStateTime += time;
                                    sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                    sb.append(prefix);
                                    sb.append("    ");
                                    sb.append(Uid.PROCESS_STATE_NAMES[ips]);
                                    sb.append(" for: ");
                                    formatTimeMs(sb, (500 + time) / 1000);
                                    pw.println(sb.toString());
                                    printTimer = true;
                                }
                            }
                            if (totalStateTime > 0) {
                                sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                sb.append(prefix);
                                sb.append("    Total running: ");
                                formatTimeMs(sb, (500 + totalStateTime) / 1000);
                                pw.println(sb.toString());
                            }
                            userCpuTimeUs = u.getUserCpuTimeUs(which);
                            systemCpuTimeUs = u.getSystemCpuTimeUs(which);
                            powerCpuMaUs = u.getCpuPowerMaUs(which);
                            if (userCpuTimeUs <= 0 && systemCpuTimeUs <= 0) {
                                if (powerCpuMaUs > 0) {
                                }
                                processStats = u.getProcessStats();
                                if (processStats != null) {
                                    for (ipr = processStats.size() - 1; ipr >= 0; ipr--) {
                                        ps = (Proc) processStats.valueAt(ipr);
                                        userTime = ps.getUserTime(which);
                                        systemTime = ps.getSystemTime(which);
                                        foregroundTime = ps.getForegroundTime(which);
                                        starts = ps.getStarts(which);
                                        numCrashes = ps.getNumCrashes(which);
                                        numAnrs = ps.getNumAnrs(which);
                                        numExcessive = which == 0 ? ps.countExcessivePowers() : WIFI_SUPPL_STATE_INVALID;
                                        if (userTime == 0 || systemTime != 0 || foregroundTime != 0 || starts != 0 || numExcessive != 0 || numCrashes != 0 || numAnrs != 0) {
                                            sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                            sb.append(prefix);
                                            sb.append("    Proc ");
                                            sb.append((String) processStats.keyAt(ipr));
                                            sb.append(":\n");
                                            sb.append(prefix);
                                            sb.append("      CPU: ");
                                            formatTimeMs(sb, userTime);
                                            sb.append("usr + ");
                                            formatTimeMs(sb, systemTime);
                                            sb.append("krn ; ");
                                            formatTimeMs(sb, foregroundTime);
                                            sb.append(FOREGROUND_DATA);
                                            if (starts == 0 && numCrashes == 0) {
                                                if (numAnrs != 0) {
                                                }
                                                pw.println(sb.toString());
                                                for (e = WIFI_SUPPL_STATE_INVALID; e < numExcessive; e += WIFI_SUPPL_STATE_DISCONNECTED) {
                                                    ew = ps.getExcessivePower(e);
                                                    if (ew != null) {
                                                        pw.print(prefix);
                                                        pw.print("      * Killed for ");
                                                        if (ew.type == WIFI_SUPPL_STATE_DISCONNECTED) {
                                                            pw.print("wake lock");
                                                        } else if (ew.type != WIFI_SUPPL_STATE_INTERFACE_DISABLED) {
                                                            pw.print(CPU_DATA);
                                                        } else {
                                                            pw.print(Environment.MEDIA_UNKNOWN);
                                                        }
                                                        pw.print(" use: ");
                                                        TimeUtils.formatDuration(ew.usedTime, pw);
                                                        pw.print(" over ");
                                                        TimeUtils.formatDuration(ew.overTime, pw);
                                                        if (ew.overTime != 0) {
                                                            pw.print(" (");
                                                            pw.print((ew.usedTime * 100) / ew.overTime);
                                                            pw.println("%)");
                                                        }
                                                    }
                                                }
                                                printTimer = true;
                                            }
                                            sb.append("\n");
                                            sb.append(prefix);
                                            sb.append("      ");
                                            hasOne = LOCAL_LOGV;
                                            if (starts != 0) {
                                                hasOne = true;
                                                sb.append(starts);
                                                sb.append(" starts");
                                            }
                                            if (numCrashes != 0) {
                                                if (hasOne) {
                                                    sb.append(", ");
                                                }
                                                hasOne = true;
                                                sb.append(numCrashes);
                                                sb.append(" crashes");
                                            }
                                            if (numAnrs != 0) {
                                                if (hasOne) {
                                                    sb.append(", ");
                                                }
                                                sb.append(numAnrs);
                                                sb.append(" anrs");
                                            }
                                            pw.println(sb.toString());
                                            for (e = WIFI_SUPPL_STATE_INVALID; e < numExcessive; e += WIFI_SUPPL_STATE_DISCONNECTED) {
                                                ew = ps.getExcessivePower(e);
                                                if (ew != null) {
                                                    pw.print(prefix);
                                                    pw.print("      * Killed for ");
                                                    if (ew.type == WIFI_SUPPL_STATE_DISCONNECTED) {
                                                        pw.print("wake lock");
                                                    } else if (ew.type != WIFI_SUPPL_STATE_INTERFACE_DISABLED) {
                                                        pw.print(Environment.MEDIA_UNKNOWN);
                                                    } else {
                                                        pw.print(CPU_DATA);
                                                    }
                                                    pw.print(" use: ");
                                                    TimeUtils.formatDuration(ew.usedTime, pw);
                                                    pw.print(" over ");
                                                    TimeUtils.formatDuration(ew.overTime, pw);
                                                    if (ew.overTime != 0) {
                                                        pw.print(" (");
                                                        pw.print((ew.usedTime * 100) / ew.overTime);
                                                        pw.println("%)");
                                                    }
                                                }
                                            }
                                            printTimer = true;
                                        }
                                    }
                                }
                                packageStats = u.getPackageStats();
                                if (packageStats != null) {
                                    for (ipkg = packageStats.size() - 1; ipkg >= 0; ipkg--) {
                                        pw.print(prefix);
                                        pw.print("    Apk ");
                                        pw.print((String) packageStats.keyAt(ipkg));
                                        pw.println(":");
                                        apkActivity = LOCAL_LOGV;
                                        ps2 = (Pkg) packageStats.valueAt(ipkg);
                                        alarms = ps2.getWakeupAlarmStats();
                                        for (iwa = alarms.size() - 1; iwa >= 0; iwa--) {
                                            pw.print(prefix);
                                            pw.print("      Wakeup alarm ");
                                            pw.print((String) alarms.keyAt(iwa));
                                            pw.print(": ");
                                            pw.print(((Counter) alarms.valueAt(iwa)).getCountLocked(which));
                                            pw.println(" times");
                                            apkActivity = true;
                                        }
                                        serviceStats = ps2.getServiceStats();
                                        for (isvc = serviceStats.size() - 1; isvc >= 0; isvc--) {
                                            ss = (Serv) serviceStats.valueAt(isvc);
                                            startTime = ss.getStartTime(batteryUptime, which);
                                            starts = ss.getStarts(which);
                                            launches = ss.getLaunches(which);
                                            if (startTime == 0 && starts == 0) {
                                                if (launches == 0) {
                                                }
                                            }
                                            sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                            sb.append(prefix);
                                            sb.append("      Service ");
                                            sb.append((String) serviceStats.keyAt(isvc));
                                            sb.append(":\n");
                                            sb.append(prefix);
                                            sb.append("        Created for: ");
                                            formatTimeMs(sb, startTime / 1000);
                                            sb.append("uptime\n");
                                            sb.append(prefix);
                                            sb.append("        Starts: ");
                                            sb.append(starts);
                                            sb.append(", launches: ");
                                            sb.append(launches);
                                            pw.println(sb.toString());
                                            apkActivity = true;
                                        }
                                        if (!apkActivity) {
                                            pw.print(prefix);
                                            pw.println("      (nothing executed)");
                                        }
                                        printTimer = true;
                                    }
                                    if (!printTimer) {
                                        pw.print(prefix);
                                        pw.println("    (nothing executed)");
                                    }
                                }
                            }
                            sb.setLength(WIFI_SUPPL_STATE_INVALID);
                            sb.append(prefix);
                            sb.append("    Total cpu time: u=");
                            formatTimeMs(sb, userCpuTimeUs / 1000);
                            sb.append("s=");
                            formatTimeMs(sb, systemCpuTimeUs / 1000);
                            sb.append("p=");
                            printmAh(sb, ((double) powerCpuMaUs) / 3.6E9d);
                            sb.append("mAh");
                            pw.println(sb.toString());
                            processStats = u.getProcessStats();
                            if (processStats != null) {
                                for (ipr = processStats.size() - 1; ipr >= 0; ipr--) {
                                    ps = (Proc) processStats.valueAt(ipr);
                                    userTime = ps.getUserTime(which);
                                    systemTime = ps.getSystemTime(which);
                                    foregroundTime = ps.getForegroundTime(which);
                                    starts = ps.getStarts(which);
                                    numCrashes = ps.getNumCrashes(which);
                                    numAnrs = ps.getNumAnrs(which);
                                    if (which == 0) {
                                    }
                                    if (userTime == 0) {
                                    }
                                    sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                    sb.append(prefix);
                                    sb.append("    Proc ");
                                    sb.append((String) processStats.keyAt(ipr));
                                    sb.append(":\n");
                                    sb.append(prefix);
                                    sb.append("      CPU: ");
                                    formatTimeMs(sb, userTime);
                                    sb.append("usr + ");
                                    formatTimeMs(sb, systemTime);
                                    sb.append("krn ; ");
                                    formatTimeMs(sb, foregroundTime);
                                    sb.append(FOREGROUND_DATA);
                                    if (numAnrs != 0) {
                                        sb.append("\n");
                                        sb.append(prefix);
                                        sb.append("      ");
                                        hasOne = LOCAL_LOGV;
                                        if (starts != 0) {
                                            hasOne = true;
                                            sb.append(starts);
                                            sb.append(" starts");
                                        }
                                        if (numCrashes != 0) {
                                            if (hasOne) {
                                                sb.append(", ");
                                            }
                                            hasOne = true;
                                            sb.append(numCrashes);
                                            sb.append(" crashes");
                                        }
                                        if (numAnrs != 0) {
                                            if (hasOne) {
                                                sb.append(", ");
                                            }
                                            sb.append(numAnrs);
                                            sb.append(" anrs");
                                        }
                                    }
                                    pw.println(sb.toString());
                                    for (e = WIFI_SUPPL_STATE_INVALID; e < numExcessive; e += WIFI_SUPPL_STATE_DISCONNECTED) {
                                        ew = ps.getExcessivePower(e);
                                        if (ew != null) {
                                            pw.print(prefix);
                                            pw.print("      * Killed for ");
                                            if (ew.type == WIFI_SUPPL_STATE_DISCONNECTED) {
                                                pw.print("wake lock");
                                            } else if (ew.type != WIFI_SUPPL_STATE_INTERFACE_DISABLED) {
                                                pw.print(CPU_DATA);
                                            } else {
                                                pw.print(Environment.MEDIA_UNKNOWN);
                                            }
                                            pw.print(" use: ");
                                            TimeUtils.formatDuration(ew.usedTime, pw);
                                            pw.print(" over ");
                                            TimeUtils.formatDuration(ew.overTime, pw);
                                            if (ew.overTime != 0) {
                                                pw.print(" (");
                                                pw.print((ew.usedTime * 100) / ew.overTime);
                                                pw.println("%)");
                                            }
                                        }
                                    }
                                    printTimer = true;
                                }
                            }
                            packageStats = u.getPackageStats();
                            if (packageStats != null) {
                                for (ipkg = packageStats.size() - 1; ipkg >= 0; ipkg--) {
                                    pw.print(prefix);
                                    pw.print("    Apk ");
                                    pw.print((String) packageStats.keyAt(ipkg));
                                    pw.println(":");
                                    apkActivity = LOCAL_LOGV;
                                    ps2 = (Pkg) packageStats.valueAt(ipkg);
                                    alarms = ps2.getWakeupAlarmStats();
                                    for (iwa = alarms.size() - 1; iwa >= 0; iwa--) {
                                        pw.print(prefix);
                                        pw.print("      Wakeup alarm ");
                                        pw.print((String) alarms.keyAt(iwa));
                                        pw.print(": ");
                                        pw.print(((Counter) alarms.valueAt(iwa)).getCountLocked(which));
                                        pw.println(" times");
                                        apkActivity = true;
                                    }
                                    serviceStats = ps2.getServiceStats();
                                    while (isvc >= 0) {
                                        ss = (Serv) serviceStats.valueAt(isvc);
                                        startTime = ss.getStartTime(batteryUptime, which);
                                        starts = ss.getStarts(which);
                                        launches = ss.getLaunches(which);
                                        if (launches == 0) {
                                        } else {
                                            sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                            sb.append(prefix);
                                            sb.append("      Service ");
                                            sb.append((String) serviceStats.keyAt(isvc));
                                            sb.append(":\n");
                                            sb.append(prefix);
                                            sb.append("        Created for: ");
                                            formatTimeMs(sb, startTime / 1000);
                                            sb.append("uptime\n");
                                            sb.append(prefix);
                                            sb.append("        Starts: ");
                                            sb.append(starts);
                                            sb.append(", launches: ");
                                            sb.append(launches);
                                            pw.println(sb.toString());
                                            apkActivity = true;
                                        }
                                    }
                                    if (!apkActivity) {
                                        pw.print(prefix);
                                        pw.println("      (nothing executed)");
                                    }
                                    printTimer = true;
                                }
                                if (!printTimer) {
                                    pw.print(prefix);
                                    pw.println("    (nothing executed)");
                                }
                            }
                        }
                        sb.setLength(WIFI_SUPPL_STATE_INVALID);
                        sb.append(prefix);
                        sb.append("    Wifi Running: ");
                        formatTimeMs(sb, uidWifiRunningTime / 1000);
                        sb.append("(");
                        sb.append(formatRatioLocked(uidWifiRunningTime, whichBatteryRealtime));
                        sb.append(")\n");
                        sb.append(prefix);
                        sb.append("    Full Wifi Lock: ");
                        formatTimeMs(sb, fullWifiLockOnTime / 1000);
                        sb.append("(");
                        sb.append(formatRatioLocked(fullWifiLockOnTime, whichBatteryRealtime));
                        sb.append(")\n");
                        sb.append(prefix);
                        sb.append("    Wifi Scan: ");
                        formatTimeMs(sb, wifiScanTime / 1000);
                        sb.append("(");
                        sb.append(formatRatioLocked(wifiScanTime, whichBatteryRealtime));
                        sb.append(") ");
                        sb.append(wifiScanCount);
                        sb.append("x");
                        pw.println(sb.toString());
                        printControllerActivityIfInteresting(pw, sb, prefix + "  ", "WiFi", u.getWifiControllerActivity(), which);
                        pw.print(prefix);
                        pw.print("    Bluetooth network: ");
                        pw.print(formatBytesLocked(btRxBytes));
                        pw.print(" received, ");
                        pw.print(formatBytesLocked(btTxBytes));
                        pw.println(" sent");
                        printTimer = printTimer(pw, sb, u.getBluetoothScanTimer(), rawRealtime, which, prefix, "Bluetooth Scan");
                        if (u.hasUserActivity()) {
                            hasData = LOCAL_LOGV;
                            for (i = WIFI_SUPPL_STATE_INVALID; i < WIFI_SUPPL_STATE_SCANNING; i += WIFI_SUPPL_STATE_DISCONNECTED) {
                                val = u.getUserActivityCount(i, which);
                                if (val == 0) {
                                    if (hasData) {
                                        sb.append(", ");
                                    } else {
                                        sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                        sb.append("    User activity: ");
                                        hasData = true;
                                    }
                                    sb.append(val);
                                    sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                                    sb.append(Uid.USER_ACTIVITY_TYPES[i]);
                                }
                            }
                            if (hasData) {
                                pw.println(sb.toString());
                            }
                        }
                        wakelocks = u.getWakelockStats();
                        totalFullWakelock = 0;
                        totalPartialWakelock = 0;
                        totalWindowWakelock = 0;
                        totalDrawWakelock = 0;
                        countWakelock = WIFI_SUPPL_STATE_INVALID;
                        if (wakelocks != null) {
                            for (iw = wakelocks.size() - 1; iw >= 0; iw--) {
                                wl = (Wakelock) wakelocks.valueAt(iw);
                                sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                sb.append(prefix);
                                sb.append("    Wake lock ");
                                sb.append((String) wakelocks.keyAt(iw));
                                str = printWakeLock(sb, wl.getWakeTime(WAKE_TYPE_DRAW), rawRealtime, "draw", which, printWakeLock(sb, wl.getWakeTime(WIFI_SUPPL_STATE_INTERFACE_DISABLED), rawRealtime, AppAssociate.ASSOC_WINDOW, which, printWakeLock(sb, wl.getWakeTime(WIFI_SUPPL_STATE_INVALID), rawRealtime, "partial", which, printWakeLock(sb, wl.getWakeTime(WIFI_SUPPL_STATE_DISCONNECTED), rawRealtime, "full", which, ": "))));
                                sb.append(" realtime");
                                pw.println(sb.toString());
                                printTimer = true;
                                countWakelock += WIFI_SUPPL_STATE_DISCONNECTED;
                                totalFullWakelock += computeWakeLock(wl.getWakeTime(WIFI_SUPPL_STATE_DISCONNECTED), rawRealtime, which);
                                totalPartialWakelock += computeWakeLock(wl.getWakeTime(WIFI_SUPPL_STATE_INVALID), rawRealtime, which);
                                totalWindowWakelock += computeWakeLock(wl.getWakeTime(WIFI_SUPPL_STATE_INTERFACE_DISABLED), rawRealtime, which);
                                totalDrawWakelock += computeWakeLock(wl.getWakeTime(WAKE_TYPE_DRAW), rawRealtime, which);
                            }
                        }
                        if (countWakelock > WIFI_SUPPL_STATE_DISCONNECTED) {
                            if (totalWindowWakelock != 0) {
                                sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                sb.append(prefix);
                                sb.append("    TOTAL wake: ");
                                needComma = LOCAL_LOGV;
                                if (totalFullWakelock != 0) {
                                    needComma = true;
                                    formatTimeMs(sb, totalFullWakelock);
                                    sb.append("full");
                                }
                                if (totalPartialWakelock != 0) {
                                    if (needComma) {
                                        sb.append(", ");
                                    }
                                    needComma = true;
                                    formatTimeMs(sb, totalPartialWakelock);
                                    sb.append("partial");
                                }
                                if (totalWindowWakelock != 0) {
                                    if (needComma) {
                                        sb.append(", ");
                                    }
                                    needComma = true;
                                    formatTimeMs(sb, totalWindowWakelock);
                                    sb.append(AppAssociate.ASSOC_WINDOW);
                                }
                                if (totalDrawWakelock != 0) {
                                    if (needComma) {
                                        sb.append(",");
                                    }
                                    formatTimeMs(sb, totalDrawWakelock);
                                    sb.append("draw");
                                }
                                sb.append(" realtime");
                                pw.println(sb.toString());
                            }
                        }
                        syncs = u.getSyncStats();
                        if (syncs != null) {
                            for (isy = syncs.size() - 1; isy >= 0; isy--) {
                                timer = (Timer) syncs.valueAt(isy);
                                totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                                count = timer.getCountLocked(which);
                                sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                sb.append(prefix);
                                sb.append("    Sync ");
                                sb.append((String) syncs.keyAt(isy));
                                sb.append(": ");
                                if (totalTime == 0) {
                                    sb.append("(not used)");
                                } else {
                                    formatTimeMs(sb, totalTime);
                                    sb.append("realtime (");
                                    sb.append(count);
                                    sb.append(" times)");
                                }
                                pw.println(sb.toString());
                                printTimer = true;
                            }
                        }
                        jobs = u.getJobStats();
                        if (jobs != null) {
                            for (ij = jobs.size() - 1; ij >= 0; ij--) {
                                timer = (Timer) jobs.valueAt(ij);
                                totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                                count = timer.getCountLocked(which);
                                sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                sb.append(prefix);
                                sb.append("    Job ");
                                sb.append((String) jobs.keyAt(ij));
                                sb.append(": ");
                                if (totalTime == 0) {
                                    sb.append("(not used)");
                                } else {
                                    formatTimeMs(sb, totalTime);
                                    sb.append("realtime (");
                                    sb.append(count);
                                    sb.append(" times)");
                                }
                                pw.println(sb.toString());
                                printTimer = true;
                            }
                        }
                        printTimer = (((printTimer | printTimer(pw, sb, u.getFlashlightTurnedOnTimer(), rawRealtime, which, prefix, "Flashlight")) | printTimer(pw, sb, u.getCameraTurnedOnTimer(), rawRealtime, which, prefix, "Camera")) | printTimer(pw, sb, u.getVideoTurnedOnTimer(), rawRealtime, which, prefix, "Video")) | printTimer(pw, sb, u.getAudioTurnedOnTimer(), rawRealtime, which, prefix, "Audio");
                        sensors = u.getSensorStats();
                        NSE = sensors.size();
                        for (ise = WIFI_SUPPL_STATE_INVALID; ise < NSE; ise += WIFI_SUPPL_STATE_DISCONNECTED) {
                            se = (Sensor) sensors.valueAt(ise);
                            sensorNumber = sensors.keyAt(ise);
                            sb.setLength(WIFI_SUPPL_STATE_INVALID);
                            sb.append(prefix);
                            sb.append("    Sensor ");
                            handle = se.getHandle();
                            if (handle != -10000) {
                                sb.append(handle);
                            } else {
                                sb.append("GPS");
                            }
                            sb.append(": ");
                            timer = se.getSensorTime();
                            if (timer == null) {
                                sb.append("(not used)");
                            } else {
                                totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                                count = timer.getCountLocked(which);
                                if (totalTime == 0) {
                                    sb.append("(not used)");
                                } else {
                                    formatTimeMs(sb, totalTime);
                                    sb.append("realtime (");
                                    sb.append(count);
                                    sb.append(" times)");
                                }
                            }
                            pw.println(sb.toString());
                            printTimer = true;
                        }
                        printTimer = (printTimer | printTimer(pw, sb, u.getVibratorOnTimer(), rawRealtime, which, prefix, "Vibrator")) | printTimer(pw, sb, u.getForegroundActivityTimer(), rawRealtime, which, prefix, "Foreground activities");
                        totalStateTime = 0;
                        for (ips = WIFI_SUPPL_STATE_INVALID; ips < WIFI_SUPPL_STATE_ASSOCIATING; ips += WIFI_SUPPL_STATE_DISCONNECTED) {
                            time = u.getProcessStateTime(ips, rawRealtime, which);
                            if (time <= 0) {
                                totalStateTime += time;
                                sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                sb.append(prefix);
                                sb.append("    ");
                                sb.append(Uid.PROCESS_STATE_NAMES[ips]);
                                sb.append(" for: ");
                                formatTimeMs(sb, (500 + time) / 1000);
                                pw.println(sb.toString());
                                printTimer = true;
                            }
                        }
                        if (totalStateTime > 0) {
                            sb.setLength(WIFI_SUPPL_STATE_INVALID);
                            sb.append(prefix);
                            sb.append("    Total running: ");
                            formatTimeMs(sb, (500 + totalStateTime) / 1000);
                            pw.println(sb.toString());
                        }
                        userCpuTimeUs = u.getUserCpuTimeUs(which);
                        systemCpuTimeUs = u.getSystemCpuTimeUs(which);
                        powerCpuMaUs = u.getCpuPowerMaUs(which);
                        if (powerCpuMaUs > 0) {
                            sb.setLength(WIFI_SUPPL_STATE_INVALID);
                            sb.append(prefix);
                            sb.append("    Total cpu time: u=");
                            formatTimeMs(sb, userCpuTimeUs / 1000);
                            sb.append("s=");
                            formatTimeMs(sb, systemCpuTimeUs / 1000);
                            sb.append("p=");
                            printmAh(sb, ((double) powerCpuMaUs) / 3.6E9d);
                            sb.append("mAh");
                            pw.println(sb.toString());
                        }
                        processStats = u.getProcessStats();
                        if (processStats != null) {
                            for (ipr = processStats.size() - 1; ipr >= 0; ipr--) {
                                ps = (Proc) processStats.valueAt(ipr);
                                userTime = ps.getUserTime(which);
                                systemTime = ps.getSystemTime(which);
                                foregroundTime = ps.getForegroundTime(which);
                                starts = ps.getStarts(which);
                                numCrashes = ps.getNumCrashes(which);
                                numAnrs = ps.getNumAnrs(which);
                                if (which == 0) {
                                }
                                if (userTime == 0) {
                                }
                                sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                sb.append(prefix);
                                sb.append("    Proc ");
                                sb.append((String) processStats.keyAt(ipr));
                                sb.append(":\n");
                                sb.append(prefix);
                                sb.append("      CPU: ");
                                formatTimeMs(sb, userTime);
                                sb.append("usr + ");
                                formatTimeMs(sb, systemTime);
                                sb.append("krn ; ");
                                formatTimeMs(sb, foregroundTime);
                                sb.append(FOREGROUND_DATA);
                                if (numAnrs != 0) {
                                    sb.append("\n");
                                    sb.append(prefix);
                                    sb.append("      ");
                                    hasOne = LOCAL_LOGV;
                                    if (starts != 0) {
                                        hasOne = true;
                                        sb.append(starts);
                                        sb.append(" starts");
                                    }
                                    if (numCrashes != 0) {
                                        if (hasOne) {
                                            sb.append(", ");
                                        }
                                        hasOne = true;
                                        sb.append(numCrashes);
                                        sb.append(" crashes");
                                    }
                                    if (numAnrs != 0) {
                                        if (hasOne) {
                                            sb.append(", ");
                                        }
                                        sb.append(numAnrs);
                                        sb.append(" anrs");
                                    }
                                }
                                pw.println(sb.toString());
                                for (e = WIFI_SUPPL_STATE_INVALID; e < numExcessive; e += WIFI_SUPPL_STATE_DISCONNECTED) {
                                    ew = ps.getExcessivePower(e);
                                    if (ew != null) {
                                        pw.print(prefix);
                                        pw.print("      * Killed for ");
                                        if (ew.type == WIFI_SUPPL_STATE_DISCONNECTED) {
                                            pw.print("wake lock");
                                        } else if (ew.type != WIFI_SUPPL_STATE_INTERFACE_DISABLED) {
                                            pw.print(Environment.MEDIA_UNKNOWN);
                                        } else {
                                            pw.print(CPU_DATA);
                                        }
                                        pw.print(" use: ");
                                        TimeUtils.formatDuration(ew.usedTime, pw);
                                        pw.print(" over ");
                                        TimeUtils.formatDuration(ew.overTime, pw);
                                        if (ew.overTime != 0) {
                                            pw.print(" (");
                                            pw.print((ew.usedTime * 100) / ew.overTime);
                                            pw.println("%)");
                                        }
                                    }
                                }
                                printTimer = true;
                            }
                        }
                        packageStats = u.getPackageStats();
                        if (packageStats != null) {
                            for (ipkg = packageStats.size() - 1; ipkg >= 0; ipkg--) {
                                pw.print(prefix);
                                pw.print("    Apk ");
                                pw.print((String) packageStats.keyAt(ipkg));
                                pw.println(":");
                                apkActivity = LOCAL_LOGV;
                                ps2 = (Pkg) packageStats.valueAt(ipkg);
                                alarms = ps2.getWakeupAlarmStats();
                                for (iwa = alarms.size() - 1; iwa >= 0; iwa--) {
                                    pw.print(prefix);
                                    pw.print("      Wakeup alarm ");
                                    pw.print((String) alarms.keyAt(iwa));
                                    pw.print(": ");
                                    pw.print(((Counter) alarms.valueAt(iwa)).getCountLocked(which));
                                    pw.println(" times");
                                    apkActivity = true;
                                }
                                serviceStats = ps2.getServiceStats();
                                while (isvc >= 0) {
                                    ss = (Serv) serviceStats.valueAt(isvc);
                                    startTime = ss.getStartTime(batteryUptime, which);
                                    starts = ss.getStarts(which);
                                    launches = ss.getLaunches(which);
                                    if (launches == 0) {
                                        sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                        sb.append(prefix);
                                        sb.append("      Service ");
                                        sb.append((String) serviceStats.keyAt(isvc));
                                        sb.append(":\n");
                                        sb.append(prefix);
                                        sb.append("        Created for: ");
                                        formatTimeMs(sb, startTime / 1000);
                                        sb.append("uptime\n");
                                        sb.append(prefix);
                                        sb.append("        Starts: ");
                                        sb.append(starts);
                                        sb.append(", launches: ");
                                        sb.append(launches);
                                        pw.println(sb.toString());
                                        apkActivity = true;
                                    } else {
                                    }
                                }
                                if (!apkActivity) {
                                    pw.print(prefix);
                                    pw.println("      (nothing executed)");
                                }
                                printTimer = true;
                            }
                            if (!printTimer) {
                                pw.print(prefix);
                                pw.println("    (nothing executed)");
                            }
                        }
                    }
                    pw.print(prefix);
                    pw.print("    Wi-Fi network: ");
                    pw.print(formatBytesLocked(wifiRxBytes));
                    pw.print(" received, ");
                    pw.print(formatBytesLocked(wifiTxBytes));
                    pw.print(" sent (packets ");
                    pw.print(wifiRxPackets);
                    pw.print(" received, ");
                    pw.print(wifiTxPackets);
                    pw.println(" sent)");
                    if (uidWifiRunningTime != 0) {
                        sb.setLength(WIFI_SUPPL_STATE_INVALID);
                        sb.append(prefix);
                        sb.append("    Wifi Running: ");
                        formatTimeMs(sb, uidWifiRunningTime / 1000);
                        sb.append("(");
                        sb.append(formatRatioLocked(uidWifiRunningTime, whichBatteryRealtime));
                        sb.append(")\n");
                        sb.append(prefix);
                        sb.append("    Full Wifi Lock: ");
                        formatTimeMs(sb, fullWifiLockOnTime / 1000);
                        sb.append("(");
                        sb.append(formatRatioLocked(fullWifiLockOnTime, whichBatteryRealtime));
                        sb.append(")\n");
                        sb.append(prefix);
                        sb.append("    Wifi Scan: ");
                        formatTimeMs(sb, wifiScanTime / 1000);
                        sb.append("(");
                        sb.append(formatRatioLocked(wifiScanTime, whichBatteryRealtime));
                        sb.append(") ");
                        sb.append(wifiScanCount);
                        sb.append("x");
                        pw.println(sb.toString());
                    }
                    printControllerActivityIfInteresting(pw, sb, prefix + "  ", "WiFi", u.getWifiControllerActivity(), which);
                    pw.print(prefix);
                    pw.print("    Bluetooth network: ");
                    pw.print(formatBytesLocked(btRxBytes));
                    pw.print(" received, ");
                    pw.print(formatBytesLocked(btTxBytes));
                    pw.println(" sent");
                    printTimer = printTimer(pw, sb, u.getBluetoothScanTimer(), rawRealtime, which, prefix, "Bluetooth Scan");
                    if (u.hasUserActivity()) {
                        hasData = LOCAL_LOGV;
                        for (i = WIFI_SUPPL_STATE_INVALID; i < WIFI_SUPPL_STATE_SCANNING; i += WIFI_SUPPL_STATE_DISCONNECTED) {
                            val = u.getUserActivityCount(i, which);
                            if (val == 0) {
                                if (hasData) {
                                    sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                    sb.append("    User activity: ");
                                    hasData = true;
                                } else {
                                    sb.append(", ");
                                }
                                sb.append(val);
                                sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                                sb.append(Uid.USER_ACTIVITY_TYPES[i]);
                            }
                        }
                        if (hasData) {
                            pw.println(sb.toString());
                        }
                    }
                    wakelocks = u.getWakelockStats();
                    totalFullWakelock = 0;
                    totalPartialWakelock = 0;
                    totalWindowWakelock = 0;
                    totalDrawWakelock = 0;
                    countWakelock = WIFI_SUPPL_STATE_INVALID;
                    if (wakelocks != null) {
                        for (iw = wakelocks.size() - 1; iw >= 0; iw--) {
                            wl = (Wakelock) wakelocks.valueAt(iw);
                            sb.setLength(WIFI_SUPPL_STATE_INVALID);
                            sb.append(prefix);
                            sb.append("    Wake lock ");
                            sb.append((String) wakelocks.keyAt(iw));
                            str = printWakeLock(sb, wl.getWakeTime(WAKE_TYPE_DRAW), rawRealtime, "draw", which, printWakeLock(sb, wl.getWakeTime(WIFI_SUPPL_STATE_INTERFACE_DISABLED), rawRealtime, AppAssociate.ASSOC_WINDOW, which, printWakeLock(sb, wl.getWakeTime(WIFI_SUPPL_STATE_INVALID), rawRealtime, "partial", which, printWakeLock(sb, wl.getWakeTime(WIFI_SUPPL_STATE_DISCONNECTED), rawRealtime, "full", which, ": "))));
                            sb.append(" realtime");
                            pw.println(sb.toString());
                            printTimer = true;
                            countWakelock += WIFI_SUPPL_STATE_DISCONNECTED;
                            totalFullWakelock += computeWakeLock(wl.getWakeTime(WIFI_SUPPL_STATE_DISCONNECTED), rawRealtime, which);
                            totalPartialWakelock += computeWakeLock(wl.getWakeTime(WIFI_SUPPL_STATE_INVALID), rawRealtime, which);
                            totalWindowWakelock += computeWakeLock(wl.getWakeTime(WIFI_SUPPL_STATE_INTERFACE_DISABLED), rawRealtime, which);
                            totalDrawWakelock += computeWakeLock(wl.getWakeTime(WAKE_TYPE_DRAW), rawRealtime, which);
                        }
                    }
                    if (countWakelock > WIFI_SUPPL_STATE_DISCONNECTED) {
                        if (totalWindowWakelock != 0) {
                            sb.setLength(WIFI_SUPPL_STATE_INVALID);
                            sb.append(prefix);
                            sb.append("    TOTAL wake: ");
                            needComma = LOCAL_LOGV;
                            if (totalFullWakelock != 0) {
                                needComma = true;
                                formatTimeMs(sb, totalFullWakelock);
                                sb.append("full");
                            }
                            if (totalPartialWakelock != 0) {
                                if (needComma) {
                                    sb.append(", ");
                                }
                                needComma = true;
                                formatTimeMs(sb, totalPartialWakelock);
                                sb.append("partial");
                            }
                            if (totalWindowWakelock != 0) {
                                if (needComma) {
                                    sb.append(", ");
                                }
                                needComma = true;
                                formatTimeMs(sb, totalWindowWakelock);
                                sb.append(AppAssociate.ASSOC_WINDOW);
                            }
                            if (totalDrawWakelock != 0) {
                                if (needComma) {
                                    sb.append(",");
                                }
                                formatTimeMs(sb, totalDrawWakelock);
                                sb.append("draw");
                            }
                            sb.append(" realtime");
                            pw.println(sb.toString());
                        }
                    }
                    syncs = u.getSyncStats();
                    if (syncs != null) {
                        for (isy = syncs.size() - 1; isy >= 0; isy--) {
                            timer = (Timer) syncs.valueAt(isy);
                            totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                            count = timer.getCountLocked(which);
                            sb.setLength(WIFI_SUPPL_STATE_INVALID);
                            sb.append(prefix);
                            sb.append("    Sync ");
                            sb.append((String) syncs.keyAt(isy));
                            sb.append(": ");
                            if (totalTime == 0) {
                                formatTimeMs(sb, totalTime);
                                sb.append("realtime (");
                                sb.append(count);
                                sb.append(" times)");
                            } else {
                                sb.append("(not used)");
                            }
                            pw.println(sb.toString());
                            printTimer = true;
                        }
                    }
                    jobs = u.getJobStats();
                    if (jobs != null) {
                        for (ij = jobs.size() - 1; ij >= 0; ij--) {
                            timer = (Timer) jobs.valueAt(ij);
                            totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                            count = timer.getCountLocked(which);
                            sb.setLength(WIFI_SUPPL_STATE_INVALID);
                            sb.append(prefix);
                            sb.append("    Job ");
                            sb.append((String) jobs.keyAt(ij));
                            sb.append(": ");
                            if (totalTime == 0) {
                                formatTimeMs(sb, totalTime);
                                sb.append("realtime (");
                                sb.append(count);
                                sb.append(" times)");
                            } else {
                                sb.append("(not used)");
                            }
                            pw.println(sb.toString());
                            printTimer = true;
                        }
                    }
                    printTimer = (((printTimer | printTimer(pw, sb, u.getFlashlightTurnedOnTimer(), rawRealtime, which, prefix, "Flashlight")) | printTimer(pw, sb, u.getCameraTurnedOnTimer(), rawRealtime, which, prefix, "Camera")) | printTimer(pw, sb, u.getVideoTurnedOnTimer(), rawRealtime, which, prefix, "Video")) | printTimer(pw, sb, u.getAudioTurnedOnTimer(), rawRealtime, which, prefix, "Audio");
                    sensors = u.getSensorStats();
                    NSE = sensors.size();
                    for (ise = WIFI_SUPPL_STATE_INVALID; ise < NSE; ise += WIFI_SUPPL_STATE_DISCONNECTED) {
                        se = (Sensor) sensors.valueAt(ise);
                        sensorNumber = sensors.keyAt(ise);
                        sb.setLength(WIFI_SUPPL_STATE_INVALID);
                        sb.append(prefix);
                        sb.append("    Sensor ");
                        handle = se.getHandle();
                        if (handle != -10000) {
                            sb.append("GPS");
                        } else {
                            sb.append(handle);
                        }
                        sb.append(": ");
                        timer = se.getSensorTime();
                        if (timer == null) {
                            totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                            count = timer.getCountLocked(which);
                            if (totalTime == 0) {
                                formatTimeMs(sb, totalTime);
                                sb.append("realtime (");
                                sb.append(count);
                                sb.append(" times)");
                            } else {
                                sb.append("(not used)");
                            }
                        } else {
                            sb.append("(not used)");
                        }
                        pw.println(sb.toString());
                        printTimer = true;
                    }
                    printTimer = (printTimer | printTimer(pw, sb, u.getVibratorOnTimer(), rawRealtime, which, prefix, "Vibrator")) | printTimer(pw, sb, u.getForegroundActivityTimer(), rawRealtime, which, prefix, "Foreground activities");
                    totalStateTime = 0;
                    for (ips = WIFI_SUPPL_STATE_INVALID; ips < WIFI_SUPPL_STATE_ASSOCIATING; ips += WIFI_SUPPL_STATE_DISCONNECTED) {
                        time = u.getProcessStateTime(ips, rawRealtime, which);
                        if (time <= 0) {
                            totalStateTime += time;
                            sb.setLength(WIFI_SUPPL_STATE_INVALID);
                            sb.append(prefix);
                            sb.append("    ");
                            sb.append(Uid.PROCESS_STATE_NAMES[ips]);
                            sb.append(" for: ");
                            formatTimeMs(sb, (500 + time) / 1000);
                            pw.println(sb.toString());
                            printTimer = true;
                        }
                    }
                    if (totalStateTime > 0) {
                        sb.setLength(WIFI_SUPPL_STATE_INVALID);
                        sb.append(prefix);
                        sb.append("    Total running: ");
                        formatTimeMs(sb, (500 + totalStateTime) / 1000);
                        pw.println(sb.toString());
                    }
                    userCpuTimeUs = u.getUserCpuTimeUs(which);
                    systemCpuTimeUs = u.getSystemCpuTimeUs(which);
                    powerCpuMaUs = u.getCpuPowerMaUs(which);
                    if (powerCpuMaUs > 0) {
                        sb.setLength(WIFI_SUPPL_STATE_INVALID);
                        sb.append(prefix);
                        sb.append("    Total cpu time: u=");
                        formatTimeMs(sb, userCpuTimeUs / 1000);
                        sb.append("s=");
                        formatTimeMs(sb, systemCpuTimeUs / 1000);
                        sb.append("p=");
                        printmAh(sb, ((double) powerCpuMaUs) / 3.6E9d);
                        sb.append("mAh");
                        pw.println(sb.toString());
                    }
                    processStats = u.getProcessStats();
                    if (processStats != null) {
                        for (ipr = processStats.size() - 1; ipr >= 0; ipr--) {
                            ps = (Proc) processStats.valueAt(ipr);
                            userTime = ps.getUserTime(which);
                            systemTime = ps.getSystemTime(which);
                            foregroundTime = ps.getForegroundTime(which);
                            starts = ps.getStarts(which);
                            numCrashes = ps.getNumCrashes(which);
                            numAnrs = ps.getNumAnrs(which);
                            if (which == 0) {
                            }
                            if (userTime == 0) {
                            }
                            sb.setLength(WIFI_SUPPL_STATE_INVALID);
                            sb.append(prefix);
                            sb.append("    Proc ");
                            sb.append((String) processStats.keyAt(ipr));
                            sb.append(":\n");
                            sb.append(prefix);
                            sb.append("      CPU: ");
                            formatTimeMs(sb, userTime);
                            sb.append("usr + ");
                            formatTimeMs(sb, systemTime);
                            sb.append("krn ; ");
                            formatTimeMs(sb, foregroundTime);
                            sb.append(FOREGROUND_DATA);
                            if (numAnrs != 0) {
                                sb.append("\n");
                                sb.append(prefix);
                                sb.append("      ");
                                hasOne = LOCAL_LOGV;
                                if (starts != 0) {
                                    hasOne = true;
                                    sb.append(starts);
                                    sb.append(" starts");
                                }
                                if (numCrashes != 0) {
                                    if (hasOne) {
                                        sb.append(", ");
                                    }
                                    hasOne = true;
                                    sb.append(numCrashes);
                                    sb.append(" crashes");
                                }
                                if (numAnrs != 0) {
                                    if (hasOne) {
                                        sb.append(", ");
                                    }
                                    sb.append(numAnrs);
                                    sb.append(" anrs");
                                }
                            }
                            pw.println(sb.toString());
                            for (e = WIFI_SUPPL_STATE_INVALID; e < numExcessive; e += WIFI_SUPPL_STATE_DISCONNECTED) {
                                ew = ps.getExcessivePower(e);
                                if (ew != null) {
                                    pw.print(prefix);
                                    pw.print("      * Killed for ");
                                    if (ew.type == WIFI_SUPPL_STATE_DISCONNECTED) {
                                        pw.print("wake lock");
                                    } else if (ew.type != WIFI_SUPPL_STATE_INTERFACE_DISABLED) {
                                        pw.print(CPU_DATA);
                                    } else {
                                        pw.print(Environment.MEDIA_UNKNOWN);
                                    }
                                    pw.print(" use: ");
                                    TimeUtils.formatDuration(ew.usedTime, pw);
                                    pw.print(" over ");
                                    TimeUtils.formatDuration(ew.overTime, pw);
                                    if (ew.overTime != 0) {
                                        pw.print(" (");
                                        pw.print((ew.usedTime * 100) / ew.overTime);
                                        pw.println("%)");
                                    }
                                }
                            }
                            printTimer = true;
                        }
                    }
                    packageStats = u.getPackageStats();
                    if (packageStats != null) {
                        for (ipkg = packageStats.size() - 1; ipkg >= 0; ipkg--) {
                            pw.print(prefix);
                            pw.print("    Apk ");
                            pw.print((String) packageStats.keyAt(ipkg));
                            pw.println(":");
                            apkActivity = LOCAL_LOGV;
                            ps2 = (Pkg) packageStats.valueAt(ipkg);
                            alarms = ps2.getWakeupAlarmStats();
                            for (iwa = alarms.size() - 1; iwa >= 0; iwa--) {
                                pw.print(prefix);
                                pw.print("      Wakeup alarm ");
                                pw.print((String) alarms.keyAt(iwa));
                                pw.print(": ");
                                pw.print(((Counter) alarms.valueAt(iwa)).getCountLocked(which));
                                pw.println(" times");
                                apkActivity = true;
                            }
                            serviceStats = ps2.getServiceStats();
                            while (isvc >= 0) {
                                ss = (Serv) serviceStats.valueAt(isvc);
                                startTime = ss.getStartTime(batteryUptime, which);
                                starts = ss.getStarts(which);
                                launches = ss.getLaunches(which);
                                if (launches == 0) {
                                } else {
                                    sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                    sb.append(prefix);
                                    sb.append("      Service ");
                                    sb.append((String) serviceStats.keyAt(isvc));
                                    sb.append(":\n");
                                    sb.append(prefix);
                                    sb.append("        Created for: ");
                                    formatTimeMs(sb, startTime / 1000);
                                    sb.append("uptime\n");
                                    sb.append(prefix);
                                    sb.append("        Starts: ");
                                    sb.append(starts);
                                    sb.append(", launches: ");
                                    sb.append(launches);
                                    pw.println(sb.toString());
                                    apkActivity = true;
                                }
                            }
                            if (!apkActivity) {
                                pw.print(prefix);
                                pw.println("      (nothing executed)");
                            }
                            printTimer = true;
                        }
                        if (!printTimer) {
                            pw.print(prefix);
                            pw.println("    (nothing executed)");
                        }
                    }
                }
                pw.print(prefix);
                pw.print("    Mobile network: ");
                pw.print(formatBytesLocked(mobileRxBytes));
                pw.print(" received, ");
                pw.print(formatBytesLocked(mobileTxBytes));
                pw.print(" sent (packets ");
                pw.print(mobileRxPackets);
                pw.print(" received, ");
                pw.print(mobileTxPackets);
                pw.println(" sent)");
                sb.setLength(WIFI_SUPPL_STATE_INVALID);
                sb.append(prefix);
                sb.append("    Mobile radio active: ");
                formatTimeMs(sb, uidMobileActiveTime / 1000);
                sb.append("(");
                sb.append(formatRatioLocked(uidMobileActiveTime, mobileActiveTime));
                sb.append(") ");
                sb.append(uidMobileActiveCount);
                sb.append("x");
                packets = mobileRxPackets + mobileTxPackets;
                if (packets == 0) {
                    packets = 1;
                }
                sb.append(" @ ");
                sb.append(BatteryStatsHelper.makemAh(((double) (uidMobileActiveTime / 1000)) / ((double) packets)));
                sb.append(" mspp");
                pw.println(sb.toString());
                printControllerActivityIfInteresting(pw, sb, prefix + "  ", "Modem", u.getModemControllerActivity(), which);
                if (wifiTxPackets > 0) {
                    pw.print(prefix);
                    pw.print("    Wi-Fi network: ");
                    pw.print(formatBytesLocked(wifiRxBytes));
                    pw.print(" received, ");
                    pw.print(formatBytesLocked(wifiTxBytes));
                    pw.print(" sent (packets ");
                    pw.print(wifiRxPackets);
                    pw.print(" received, ");
                    pw.print(wifiTxPackets);
                    pw.println(" sent)");
                }
                if (uidWifiRunningTime != 0) {
                    sb.setLength(WIFI_SUPPL_STATE_INVALID);
                    sb.append(prefix);
                    sb.append("    Wifi Running: ");
                    formatTimeMs(sb, uidWifiRunningTime / 1000);
                    sb.append("(");
                    sb.append(formatRatioLocked(uidWifiRunningTime, whichBatteryRealtime));
                    sb.append(")\n");
                    sb.append(prefix);
                    sb.append("    Full Wifi Lock: ");
                    formatTimeMs(sb, fullWifiLockOnTime / 1000);
                    sb.append("(");
                    sb.append(formatRatioLocked(fullWifiLockOnTime, whichBatteryRealtime));
                    sb.append(")\n");
                    sb.append(prefix);
                    sb.append("    Wifi Scan: ");
                    formatTimeMs(sb, wifiScanTime / 1000);
                    sb.append("(");
                    sb.append(formatRatioLocked(wifiScanTime, whichBatteryRealtime));
                    sb.append(") ");
                    sb.append(wifiScanCount);
                    sb.append("x");
                    pw.println(sb.toString());
                }
                printControllerActivityIfInteresting(pw, sb, prefix + "  ", "WiFi", u.getWifiControllerActivity(), which);
                pw.print(prefix);
                pw.print("    Bluetooth network: ");
                pw.print(formatBytesLocked(btRxBytes));
                pw.print(" received, ");
                pw.print(formatBytesLocked(btTxBytes));
                pw.println(" sent");
                printTimer = printTimer(pw, sb, u.getBluetoothScanTimer(), rawRealtime, which, prefix, "Bluetooth Scan");
                if (u.hasUserActivity()) {
                    hasData = LOCAL_LOGV;
                    for (i = WIFI_SUPPL_STATE_INVALID; i < WIFI_SUPPL_STATE_SCANNING; i += WIFI_SUPPL_STATE_DISCONNECTED) {
                        val = u.getUserActivityCount(i, which);
                        if (val == 0) {
                            if (hasData) {
                                sb.append(", ");
                            } else {
                                sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                sb.append("    User activity: ");
                                hasData = true;
                            }
                            sb.append(val);
                            sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                            sb.append(Uid.USER_ACTIVITY_TYPES[i]);
                        }
                    }
                    if (hasData) {
                        pw.println(sb.toString());
                    }
                }
                wakelocks = u.getWakelockStats();
                totalFullWakelock = 0;
                totalPartialWakelock = 0;
                totalWindowWakelock = 0;
                totalDrawWakelock = 0;
                countWakelock = WIFI_SUPPL_STATE_INVALID;
                if (wakelocks != null) {
                    for (iw = wakelocks.size() - 1; iw >= 0; iw--) {
                        wl = (Wakelock) wakelocks.valueAt(iw);
                        sb.setLength(WIFI_SUPPL_STATE_INVALID);
                        sb.append(prefix);
                        sb.append("    Wake lock ");
                        sb.append((String) wakelocks.keyAt(iw));
                        str = printWakeLock(sb, wl.getWakeTime(WAKE_TYPE_DRAW), rawRealtime, "draw", which, printWakeLock(sb, wl.getWakeTime(WIFI_SUPPL_STATE_INTERFACE_DISABLED), rawRealtime, AppAssociate.ASSOC_WINDOW, which, printWakeLock(sb, wl.getWakeTime(WIFI_SUPPL_STATE_INVALID), rawRealtime, "partial", which, printWakeLock(sb, wl.getWakeTime(WIFI_SUPPL_STATE_DISCONNECTED), rawRealtime, "full", which, ": "))));
                        sb.append(" realtime");
                        pw.println(sb.toString());
                        printTimer = true;
                        countWakelock += WIFI_SUPPL_STATE_DISCONNECTED;
                        totalFullWakelock += computeWakeLock(wl.getWakeTime(WIFI_SUPPL_STATE_DISCONNECTED), rawRealtime, which);
                        totalPartialWakelock += computeWakeLock(wl.getWakeTime(WIFI_SUPPL_STATE_INVALID), rawRealtime, which);
                        totalWindowWakelock += computeWakeLock(wl.getWakeTime(WIFI_SUPPL_STATE_INTERFACE_DISABLED), rawRealtime, which);
                        totalDrawWakelock += computeWakeLock(wl.getWakeTime(WAKE_TYPE_DRAW), rawRealtime, which);
                    }
                }
                if (countWakelock > WIFI_SUPPL_STATE_DISCONNECTED) {
                    if (totalWindowWakelock != 0) {
                        sb.setLength(WIFI_SUPPL_STATE_INVALID);
                        sb.append(prefix);
                        sb.append("    TOTAL wake: ");
                        needComma = LOCAL_LOGV;
                        if (totalFullWakelock != 0) {
                            needComma = true;
                            formatTimeMs(sb, totalFullWakelock);
                            sb.append("full");
                        }
                        if (totalPartialWakelock != 0) {
                            if (needComma) {
                                sb.append(", ");
                            }
                            needComma = true;
                            formatTimeMs(sb, totalPartialWakelock);
                            sb.append("partial");
                        }
                        if (totalWindowWakelock != 0) {
                            if (needComma) {
                                sb.append(", ");
                            }
                            needComma = true;
                            formatTimeMs(sb, totalWindowWakelock);
                            sb.append(AppAssociate.ASSOC_WINDOW);
                        }
                        if (totalDrawWakelock != 0) {
                            if (needComma) {
                                sb.append(",");
                            }
                            formatTimeMs(sb, totalDrawWakelock);
                            sb.append("draw");
                        }
                        sb.append(" realtime");
                        pw.println(sb.toString());
                    }
                }
                syncs = u.getSyncStats();
                if (syncs != null) {
                    for (isy = syncs.size() - 1; isy >= 0; isy--) {
                        timer = (Timer) syncs.valueAt(isy);
                        totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                        count = timer.getCountLocked(which);
                        sb.setLength(WIFI_SUPPL_STATE_INVALID);
                        sb.append(prefix);
                        sb.append("    Sync ");
                        sb.append((String) syncs.keyAt(isy));
                        sb.append(": ");
                        if (totalTime == 0) {
                            sb.append("(not used)");
                        } else {
                            formatTimeMs(sb, totalTime);
                            sb.append("realtime (");
                            sb.append(count);
                            sb.append(" times)");
                        }
                        pw.println(sb.toString());
                        printTimer = true;
                    }
                }
                jobs = u.getJobStats();
                if (jobs != null) {
                    for (ij = jobs.size() - 1; ij >= 0; ij--) {
                        timer = (Timer) jobs.valueAt(ij);
                        totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                        count = timer.getCountLocked(which);
                        sb.setLength(WIFI_SUPPL_STATE_INVALID);
                        sb.append(prefix);
                        sb.append("    Job ");
                        sb.append((String) jobs.keyAt(ij));
                        sb.append(": ");
                        if (totalTime == 0) {
                            sb.append("(not used)");
                        } else {
                            formatTimeMs(sb, totalTime);
                            sb.append("realtime (");
                            sb.append(count);
                            sb.append(" times)");
                        }
                        pw.println(sb.toString());
                        printTimer = true;
                    }
                }
                printTimer = (((printTimer | printTimer(pw, sb, u.getFlashlightTurnedOnTimer(), rawRealtime, which, prefix, "Flashlight")) | printTimer(pw, sb, u.getCameraTurnedOnTimer(), rawRealtime, which, prefix, "Camera")) | printTimer(pw, sb, u.getVideoTurnedOnTimer(), rawRealtime, which, prefix, "Video")) | printTimer(pw, sb, u.getAudioTurnedOnTimer(), rawRealtime, which, prefix, "Audio");
                sensors = u.getSensorStats();
                NSE = sensors.size();
                for (ise = WIFI_SUPPL_STATE_INVALID; ise < NSE; ise += WIFI_SUPPL_STATE_DISCONNECTED) {
                    se = (Sensor) sensors.valueAt(ise);
                    sensorNumber = sensors.keyAt(ise);
                    sb.setLength(WIFI_SUPPL_STATE_INVALID);
                    sb.append(prefix);
                    sb.append("    Sensor ");
                    handle = se.getHandle();
                    if (handle != -10000) {
                        sb.append(handle);
                    } else {
                        sb.append("GPS");
                    }
                    sb.append(": ");
                    timer = se.getSensorTime();
                    if (timer == null) {
                        sb.append("(not used)");
                    } else {
                        totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                        count = timer.getCountLocked(which);
                        if (totalTime == 0) {
                            sb.append("(not used)");
                        } else {
                            formatTimeMs(sb, totalTime);
                            sb.append("realtime (");
                            sb.append(count);
                            sb.append(" times)");
                        }
                    }
                    pw.println(sb.toString());
                    printTimer = true;
                }
                printTimer = (printTimer | printTimer(pw, sb, u.getVibratorOnTimer(), rawRealtime, which, prefix, "Vibrator")) | printTimer(pw, sb, u.getForegroundActivityTimer(), rawRealtime, which, prefix, "Foreground activities");
                totalStateTime = 0;
                for (ips = WIFI_SUPPL_STATE_INVALID; ips < WIFI_SUPPL_STATE_ASSOCIATING; ips += WIFI_SUPPL_STATE_DISCONNECTED) {
                    time = u.getProcessStateTime(ips, rawRealtime, which);
                    if (time <= 0) {
                        totalStateTime += time;
                        sb.setLength(WIFI_SUPPL_STATE_INVALID);
                        sb.append(prefix);
                        sb.append("    ");
                        sb.append(Uid.PROCESS_STATE_NAMES[ips]);
                        sb.append(" for: ");
                        formatTimeMs(sb, (500 + time) / 1000);
                        pw.println(sb.toString());
                        printTimer = true;
                    }
                }
                if (totalStateTime > 0) {
                    sb.setLength(WIFI_SUPPL_STATE_INVALID);
                    sb.append(prefix);
                    sb.append("    Total running: ");
                    formatTimeMs(sb, (500 + totalStateTime) / 1000);
                    pw.println(sb.toString());
                }
                userCpuTimeUs = u.getUserCpuTimeUs(which);
                systemCpuTimeUs = u.getSystemCpuTimeUs(which);
                powerCpuMaUs = u.getCpuPowerMaUs(which);
                if (powerCpuMaUs > 0) {
                    sb.setLength(WIFI_SUPPL_STATE_INVALID);
                    sb.append(prefix);
                    sb.append("    Total cpu time: u=");
                    formatTimeMs(sb, userCpuTimeUs / 1000);
                    sb.append("s=");
                    formatTimeMs(sb, systemCpuTimeUs / 1000);
                    sb.append("p=");
                    printmAh(sb, ((double) powerCpuMaUs) / 3.6E9d);
                    sb.append("mAh");
                    pw.println(sb.toString());
                }
                processStats = u.getProcessStats();
                if (processStats != null) {
                    for (ipr = processStats.size() - 1; ipr >= 0; ipr--) {
                        ps = (Proc) processStats.valueAt(ipr);
                        userTime = ps.getUserTime(which);
                        systemTime = ps.getSystemTime(which);
                        foregroundTime = ps.getForegroundTime(which);
                        starts = ps.getStarts(which);
                        numCrashes = ps.getNumCrashes(which);
                        numAnrs = ps.getNumAnrs(which);
                        if (which == 0) {
                        }
                        if (userTime == 0) {
                        }
                        sb.setLength(WIFI_SUPPL_STATE_INVALID);
                        sb.append(prefix);
                        sb.append("    Proc ");
                        sb.append((String) processStats.keyAt(ipr));
                        sb.append(":\n");
                        sb.append(prefix);
                        sb.append("      CPU: ");
                        formatTimeMs(sb, userTime);
                        sb.append("usr + ");
                        formatTimeMs(sb, systemTime);
                        sb.append("krn ; ");
                        formatTimeMs(sb, foregroundTime);
                        sb.append(FOREGROUND_DATA);
                        if (numAnrs != 0) {
                            sb.append("\n");
                            sb.append(prefix);
                            sb.append("      ");
                            hasOne = LOCAL_LOGV;
                            if (starts != 0) {
                                hasOne = true;
                                sb.append(starts);
                                sb.append(" starts");
                            }
                            if (numCrashes != 0) {
                                if (hasOne) {
                                    sb.append(", ");
                                }
                                hasOne = true;
                                sb.append(numCrashes);
                                sb.append(" crashes");
                            }
                            if (numAnrs != 0) {
                                if (hasOne) {
                                    sb.append(", ");
                                }
                                sb.append(numAnrs);
                                sb.append(" anrs");
                            }
                        }
                        pw.println(sb.toString());
                        for (e = WIFI_SUPPL_STATE_INVALID; e < numExcessive; e += WIFI_SUPPL_STATE_DISCONNECTED) {
                            ew = ps.getExcessivePower(e);
                            if (ew != null) {
                                pw.print(prefix);
                                pw.print("      * Killed for ");
                                if (ew.type == WIFI_SUPPL_STATE_DISCONNECTED) {
                                    pw.print("wake lock");
                                } else if (ew.type != WIFI_SUPPL_STATE_INTERFACE_DISABLED) {
                                    pw.print(Environment.MEDIA_UNKNOWN);
                                } else {
                                    pw.print(CPU_DATA);
                                }
                                pw.print(" use: ");
                                TimeUtils.formatDuration(ew.usedTime, pw);
                                pw.print(" over ");
                                TimeUtils.formatDuration(ew.overTime, pw);
                                if (ew.overTime != 0) {
                                    pw.print(" (");
                                    pw.print((ew.usedTime * 100) / ew.overTime);
                                    pw.println("%)");
                                }
                            }
                        }
                        printTimer = true;
                    }
                }
                packageStats = u.getPackageStats();
                if (packageStats != null) {
                    for (ipkg = packageStats.size() - 1; ipkg >= 0; ipkg--) {
                        pw.print(prefix);
                        pw.print("    Apk ");
                        pw.print((String) packageStats.keyAt(ipkg));
                        pw.println(":");
                        apkActivity = LOCAL_LOGV;
                        ps2 = (Pkg) packageStats.valueAt(ipkg);
                        alarms = ps2.getWakeupAlarmStats();
                        for (iwa = alarms.size() - 1; iwa >= 0; iwa--) {
                            pw.print(prefix);
                            pw.print("      Wakeup alarm ");
                            pw.print((String) alarms.keyAt(iwa));
                            pw.print(": ");
                            pw.print(((Counter) alarms.valueAt(iwa)).getCountLocked(which));
                            pw.println(" times");
                            apkActivity = true;
                        }
                        serviceStats = ps2.getServiceStats();
                        while (isvc >= 0) {
                            ss = (Serv) serviceStats.valueAt(isvc);
                            startTime = ss.getStartTime(batteryUptime, which);
                            starts = ss.getStarts(which);
                            launches = ss.getLaunches(which);
                            if (launches == 0) {
                                sb.setLength(WIFI_SUPPL_STATE_INVALID);
                                sb.append(prefix);
                                sb.append("      Service ");
                                sb.append((String) serviceStats.keyAt(isvc));
                                sb.append(":\n");
                                sb.append(prefix);
                                sb.append("        Created for: ");
                                formatTimeMs(sb, startTime / 1000);
                                sb.append("uptime\n");
                                sb.append(prefix);
                                sb.append("        Starts: ");
                                sb.append(starts);
                                sb.append(", launches: ");
                                sb.append(launches);
                                pw.println(sb.toString());
                                apkActivity = true;
                            } else {
                            }
                        }
                        if (!apkActivity) {
                            pw.print(prefix);
                            pw.println("      (nothing executed)");
                        }
                        printTimer = true;
                    }
                    if (!printTimer) {
                        pw.print(prefix);
                        pw.println("    (nothing executed)");
                    }
                }
            }
        }
    }

    static void printBitDescriptions(PrintWriter pw, int oldval, int newval, HistoryTag wakelockTag, BitDescription[] descriptions, boolean longNames) {
        int diff = oldval ^ newval;
        if (diff != 0) {
            boolean didWake = LOCAL_LOGV;
            for (int i = WIFI_SUPPL_STATE_INVALID; i < descriptions.length; i += WIFI_SUPPL_STATE_DISCONNECTED) {
                BitDescription bd = descriptions[i];
                if ((bd.mask & diff) != 0) {
                    pw.print(longNames ? WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER : ",");
                    if (bd.shift < 0) {
                        pw.print((bd.mask & newval) != 0 ? "+" : "-");
                        pw.print(longNames ? bd.name : bd.shortName);
                        if (bd.mask == KeymasterDefs.KM_UINT_REP && wakelockTag != null) {
                            didWake = true;
                            pw.print("=");
                            if (longNames) {
                                UserHandle.formatUid(pw, wakelockTag.uid);
                                pw.print(":\"");
                                pw.print(wakelockTag.string);
                                pw.print("\"");
                            } else {
                                pw.print(wakelockTag.poolIdx);
                            }
                        }
                    } else {
                        pw.print(longNames ? bd.name : bd.shortName);
                        pw.print("=");
                        int val = (bd.mask & newval) >> bd.shift;
                        if (bd.values == null || val < 0 || val >= bd.values.length) {
                            pw.print(val);
                        } else {
                            pw.print(longNames ? bd.values[val] : bd.shortValues[val]);
                        }
                    }
                }
            }
            if (!(didWake || wakelockTag == null)) {
                pw.print(longNames ? " wake_lock=" : ",w=");
                if (longNames) {
                    UserHandle.formatUid(pw, wakelockTag.uid);
                    pw.print(":\"");
                    pw.print(wakelockTag.string);
                    pw.print("\"");
                } else {
                    pw.print(wakelockTag.poolIdx);
                }
            }
        }
    }

    public void prepareForDumpLocked() {
    }

    private void printSizeValue(PrintWriter pw, long size) {
        float result = (float) size;
        String suffix = ProxyInfo.LOCAL_EXCL_LIST;
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
            return LOCAL_LOGV;
        }
        pw.print(label1);
        pw.print(label2);
        pw.print(label3);
        StringBuilder sb = new StringBuilder(DUMP_DEVICE_WIFI_ONLY);
        formatTimeMs(sb, estimatedTime);
        pw.print(sb);
        pw.println();
        return true;
    }

    private static boolean dumpDurationSteps(PrintWriter pw, String prefix, String header, LevelStepTracker steps, boolean checkin) {
        if (steps == null) {
            return LOCAL_LOGV;
        }
        int count = steps.mNumStepDurations;
        if (count <= 0) {
            return LOCAL_LOGV;
        }
        if (!checkin) {
            pw.println(header);
        }
        String[] lineArgs = new String[WIFI_SUPPL_STATE_AUTHENTICATING];
        for (int i = WIFI_SUPPL_STATE_INVALID; i < count; i += WIFI_SUPPL_STATE_DISCONNECTED) {
            long duration = steps.getDurationAt(i);
            int level = steps.getLevelAt(i);
            long initMode = (long) steps.getInitModeAt(i);
            long modMode = (long) steps.getModModeAt(i);
            if (checkin) {
                lineArgs[WIFI_SUPPL_STATE_INVALID] = Long.toString(duration);
                lineArgs[WIFI_SUPPL_STATE_DISCONNECTED] = Integer.toString(level);
                if ((3 & modMode) == 0) {
                    switch (((int) (3 & initMode)) + WIFI_SUPPL_STATE_DISCONNECTED) {
                        case WIFI_SUPPL_STATE_DISCONNECTED /*1*/:
                            lineArgs[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = "s-";
                            break;
                        case WIFI_SUPPL_STATE_INTERFACE_DISABLED /*2*/:
                            lineArgs[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = "s+";
                            break;
                        case WIFI_SUPPL_STATE_INACTIVE /*3*/:
                            lineArgs[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = "sd";
                            break;
                        case WIFI_SUPPL_STATE_SCANNING /*4*/:
                            lineArgs[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = "sds";
                            break;
                        default:
                            lineArgs[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = "?";
                            break;
                    }
                }
                lineArgs[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = ProxyInfo.LOCAL_EXCL_LIST;
                if ((4 & modMode) == 0) {
                    lineArgs[WIFI_SUPPL_STATE_INACTIVE] = (4 & initMode) != 0 ? "p+" : "p-";
                } else {
                    lineArgs[WIFI_SUPPL_STATE_INACTIVE] = ProxyInfo.LOCAL_EXCL_LIST;
                }
                if ((8 & modMode) == 0) {
                    lineArgs[WIFI_SUPPL_STATE_SCANNING] = (8 & initMode) != 0 ? "i+" : "i-";
                } else {
                    lineArgs[WIFI_SUPPL_STATE_SCANNING] = ProxyInfo.LOCAL_EXCL_LIST;
                }
                dumpLine(pw, WIFI_SUPPL_STATE_INVALID, "i", header, lineArgs);
            } else {
                pw.print(prefix);
                pw.print("#");
                pw.print(i);
                pw.print(": ");
                TimeUtils.formatDuration(duration, pw);
                pw.print(" to ");
                pw.print(level);
                boolean haveModes = LOCAL_LOGV;
                if ((3 & modMode) == 0) {
                    pw.print(" (");
                    switch (((int) (3 & initMode)) + WIFI_SUPPL_STATE_DISCONNECTED) {
                        case WIFI_SUPPL_STATE_DISCONNECTED /*1*/:
                            pw.print("screen-off");
                            break;
                        case WIFI_SUPPL_STATE_INTERFACE_DISABLED /*2*/:
                            pw.print("screen-on");
                            break;
                        case WIFI_SUPPL_STATE_INACTIVE /*3*/:
                            pw.print("screen-doze");
                            break;
                        case WIFI_SUPPL_STATE_SCANNING /*4*/:
                            pw.print("screen-doze-suspend");
                            break;
                        default:
                            pw.print("screen-?");
                            break;
                    }
                    haveModes = true;
                }
                if ((4 & modMode) == 0) {
                    pw.print(haveModes ? ", " : " (");
                    pw.print((4 & initMode) != 0 ? "power-save-on" : "power-save-off");
                    haveModes = true;
                }
                if ((8 & modMode) == 0) {
                    pw.print(haveModes ? ", " : " (");
                    pw.print((8 & initMode) != 0 ? "device-idle-on" : "device-idle-off");
                    haveModes = true;
                }
                if (haveModes) {
                    pw.print(")");
                }
                pw.println();
            }
        }
        return true;
    }

    private void dumpHistoryLocked(PrintWriter pw, int flags, long histStart, boolean checkin) {
        HistoryPrinter hprinter = new HistoryPrinter();
        HistoryItem rec = new HistoryItem();
        long lastTime = -1;
        long baseTime = -1;
        boolean printed = LOCAL_LOGV;
        HistoryEventTracker tracker = null;
        while (getNextHistoryLocked(rec)) {
            lastTime = rec.time;
            if (baseTime < 0) {
                baseTime = lastTime;
            }
            if (rec.time >= histStart) {
                if (histStart >= 0 && !printed) {
                    if (rec.cmd == WIFI_SUPPL_STATE_AUTHENTICATING || rec.cmd == WIFI_SUPPL_STATE_ASSOCIATED || rec.cmd == WIFI_SUPPL_STATE_SCANNING || rec.cmd == WIFI_SUPPL_STATE_FOUR_WAY_HANDSHAKE) {
                        printed = true;
                        hprinter.printNextItem(pw, rec, baseTime, checkin, (flags & DUMP_VERBOSE) != 0 ? true : LOCAL_LOGV);
                        rec.cmd = (byte) 0;
                    } else if (rec.currentTime != 0) {
                        printed = true;
                        byte cmd = rec.cmd;
                        rec.cmd = (byte) 5;
                        hprinter.printNextItem(pw, rec, baseTime, checkin, (flags & DUMP_VERBOSE) != 0 ? true : LOCAL_LOGV);
                        rec.cmd = cmd;
                    }
                    if (WIFI_SUPPL_STATE_INVALID != null) {
                        if (rec.cmd != null) {
                            hprinter.printNextItem(pw, rec, baseTime, checkin, (flags & DUMP_VERBOSE) != 0 ? true : LOCAL_LOGV);
                            rec.cmd = (byte) 0;
                        }
                        int oldEventCode = rec.eventCode;
                        HistoryTag oldEventTag = rec.eventTag;
                        rec.eventTag = new HistoryTag();
                        for (int i = WIFI_SUPPL_STATE_INVALID; i < BLUETOOTH_SCAN_ON; i += WIFI_SUPPL_STATE_DISCONNECTED) {
                            HashMap<String, SparseIntArray> active = tracker.getStateForEvent(i);
                            if (active != null) {
                                for (Entry<String, SparseIntArray> ent : active.entrySet()) {
                                    SparseIntArray uids = (SparseIntArray) ent.getValue();
                                    for (int j = WIFI_SUPPL_STATE_INVALID; j < uids.size(); j += WIFI_SUPPL_STATE_DISCONNECTED) {
                                        rec.eventCode = i;
                                        rec.eventTag.string = (String) ent.getKey();
                                        rec.eventTag.uid = uids.keyAt(j);
                                        rec.eventTag.poolIdx = uids.valueAt(j);
                                        hprinter.printNextItem(pw, rec, baseTime, checkin, (flags & DUMP_VERBOSE) != 0 ? true : LOCAL_LOGV);
                                        rec.wakeReasonTag = null;
                                        rec.wakelockTag = null;
                                    }
                                }
                            }
                        }
                        rec.eventCode = oldEventCode;
                        rec.eventTag = oldEventTag;
                        tracker = null;
                    }
                }
                hprinter.printNextItem(pw, rec, baseTime, checkin, (flags & DUMP_VERBOSE) != 0 ? true : LOCAL_LOGV);
            }
        }
        if (histStart >= 0) {
            String str;
            commitCurrentHistoryBatchLocked();
            if (checkin) {
                str = "NEXT: ";
            } else {
                str = "  NEXT: ";
            }
            pw.print(str);
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
                tmpSb.setLength(WIFI_SUPPL_STATE_INVALID);
                formatTimeMs(tmpSb, timeRemaining);
                pw.print(tmpSb);
                pw.print(" (from ");
                pw.print(tmpOutInt[WIFI_SUPPL_STATE_INVALID]);
                pw.println(" steps)");
            }
            for (int i = WIFI_SUPPL_STATE_INVALID; i < STEP_LEVEL_MODES_OF_INTEREST.length; i += WIFI_SUPPL_STATE_DISCONNECTED) {
                long estimatedTime = steps.computeTimeEstimate((long) STEP_LEVEL_MODES_OF_INTEREST[i], (long) STEP_LEVEL_MODE_VALUES[i], tmpOutInt);
                if (estimatedTime > 0) {
                    pw.print(prefix);
                    pw.print(label);
                    pw.print(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                    pw.print(STEP_LEVEL_MODE_LABELS[i]);
                    pw.print(" time: ");
                    tmpSb.setLength(WIFI_SUPPL_STATE_INVALID);
                    formatTimeMs(tmpSb, estimatedTime);
                    pw.print(tmpSb);
                    pw.print(" (from ");
                    pw.print(tmpOutInt[WIFI_SUPPL_STATE_INVALID]);
                    pw.println(" steps)");
                }
            }
        }
    }

    private void dumpDailyPackageChanges(PrintWriter pw, String prefix, ArrayList<PackageChange> changes) {
        if (changes != null) {
            pw.print(prefix);
            pw.println("Package changes:");
            for (int i = WIFI_SUPPL_STATE_INVALID; i < changes.size(); i += WIFI_SUPPL_STATE_DISCONNECTED) {
                PackageChange pc = (PackageChange) changes.get(i);
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
        prepareForDumpLocked();
        boolean filtering = (flags & JOB) != 0 ? true : LOCAL_LOGV;
        if (!((flags & WIFI_SUPPL_STATE_FOUR_WAY_HANDSHAKE) == 0 && filtering)) {
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
                    dumpHistoryLocked(pw, flags, histStart, LOCAL_LOGV);
                    pw.println();
                } finally {
                    finishIteratingHistoryLocked();
                }
            }
            if (startIteratingOldHistoryLocked()) {
                try {
                    HistoryItem rec = new HistoryItem();
                    pw.println("Old battery History:");
                    HistoryPrinter hprinter = new HistoryPrinter();
                    long baseTime = -1;
                    while (getNextOldHistoryLocked(rec)) {
                        if (baseTime < 0) {
                            baseTime = rec.time;
                        }
                        hprinter.printNextItem(pw, rec, baseTime, LOCAL_LOGV, (flags & DUMP_VERBOSE) != 0 ? true : LOCAL_LOGV);
                    }
                    pw.println();
                } finally {
                    finishIteratingOldHistoryLocked();
                }
            }
        }
        if (!filtering || (flags & WIFI_SUPPL_STATE_ASSOCIATING) != 0) {
            int i;
            if (!filtering) {
                SparseArray<? extends Uid> uidStats = getUidStats();
                int NU = uidStats.size();
                boolean didPid = LOCAL_LOGV;
                long nowRealtime = SystemClock.elapsedRealtime();
                for (i = WIFI_SUPPL_STATE_INVALID; i < NU; i += WIFI_SUPPL_STATE_DISCONNECTED) {
                    SparseArray<? extends Pid> pids = ((Uid) uidStats.valueAt(i)).getPidStats();
                    if (pids != null) {
                        for (int j = WIFI_SUPPL_STATE_INVALID; j < pids.size(); j += WIFI_SUPPL_STATE_DISCONNECTED) {
                            Pid pid = (Pid) pids.valueAt(j);
                            if (!didPid) {
                                pw.println("Per-PID Stats:");
                                didPid = true;
                            }
                            long time = pid.mWakeSumMs + (pid.mWakeNesting > 0 ? nowRealtime - pid.mWakeStartMs : 0);
                            pw.print("  PID ");
                            pw.print(pids.keyAt(j));
                            pw.print(" wake time: ");
                            TimeUtils.formatDuration(time, pw);
                            pw.println(ProxyInfo.LOCAL_EXCL_LIST);
                        }
                    }
                }
                if (didPid) {
                    pw.println();
                }
            }
            if (!(filtering && (flags & WIFI_SUPPL_STATE_INTERFACE_DISABLED) == 0)) {
                long timeRemaining;
                if (dumpDurationSteps(pw, "  ", "Discharge step durations:", getDischargeLevelStepTracker(), LOCAL_LOGV)) {
                    timeRemaining = computeBatteryTimeRemaining(SystemClock.elapsedRealtime());
                    if (timeRemaining >= 0) {
                        pw.print("  Estimated discharge time remaining: ");
                        TimeUtils.formatDuration(timeRemaining / 1000, pw);
                        pw.println();
                    }
                    LevelStepTracker steps = getDischargeLevelStepTracker();
                    for (i = WIFI_SUPPL_STATE_INVALID; i < STEP_LEVEL_MODES_OF_INTEREST.length; i += WIFI_SUPPL_STATE_DISCONNECTED) {
                        dumpTimeEstimate(pw, "  Estimated ", STEP_LEVEL_MODE_LABELS[i], " time: ", steps.computeTimeEstimate((long) STEP_LEVEL_MODES_OF_INTEREST[i], (long) STEP_LEVEL_MODE_VALUES[i], null));
                    }
                    pw.println();
                }
                if (dumpDurationSteps(pw, "  ", "Charge step durations:", getChargeLevelStepTracker(), LOCAL_LOGV)) {
                    timeRemaining = computeChargeTimeRemaining(SystemClock.elapsedRealtime());
                    if (timeRemaining >= 0) {
                        pw.print("  Estimated charge time remaining: ");
                        TimeUtils.formatDuration(timeRemaining / 1000, pw);
                        pw.println();
                    }
                    pw.println();
                }
            }
            if (!(filtering && (flags & WIFI_SUPPL_STATE_ASSOCIATING) == 0)) {
                int curIndex;
                DailyItem dit;
                pw.println("Daily stats:");
                pw.print("  Current start time: ");
                pw.println(DateFormat.format("yyyy-MM-dd-HH-mm-ss", getCurrentDailyStartTime()).toString());
                pw.print("  Next min deadline: ");
                pw.println(DateFormat.format("yyyy-MM-dd-HH-mm-ss", getNextMinDailyDeadline()).toString());
                pw.print("  Next max deadline: ");
                pw.println(DateFormat.format("yyyy-MM-dd-HH-mm-ss", getNextMaxDailyDeadline()).toString());
                StringBuilder stringBuilder = new StringBuilder(DUMP_DEVICE_WIFI_ONLY);
                int[] outInt = new int[WIFI_SUPPL_STATE_DISCONNECTED];
                LevelStepTracker dsteps = getDailyDischargeLevelStepTracker();
                LevelStepTracker csteps = getDailyChargeLevelStepTracker();
                ArrayList<PackageChange> pkgc = getDailyPackageChanges();
                if (dsteps.mNumStepDurations <= 0 && csteps.mNumStepDurations <= 0) {
                    if (pkgc != null) {
                    }
                    curIndex = WIFI_SUPPL_STATE_INVALID;
                    while (true) {
                        dit = getDailyItemLocked(curIndex);
                        if (dit == null) {
                            break;
                        }
                        curIndex += WIFI_SUPPL_STATE_DISCONNECTED;
                        if ((flags & WIFI_SUPPL_STATE_SCANNING) != 0) {
                            pw.println();
                        }
                        pw.print("  Daily from ");
                        pw.print(DateFormat.format("yyyy-MM-dd-HH-mm-ss", dit.mStartTime).toString());
                        pw.print(" to ");
                        pw.print(DateFormat.format("yyyy-MM-dd-HH-mm-ss", dit.mEndTime).toString());
                        pw.println(":");
                        if ((flags & WIFI_SUPPL_STATE_SCANNING) == 0 || !filtering) {
                            if (dumpDurationSteps(pw, "      ", "    Discharge step durations:", dit.mDischargeSteps, LOCAL_LOGV)) {
                                dumpDailyLevelStepSummary(pw, "        ", "Discharge", dit.mDischargeSteps, stringBuilder, outInt);
                            }
                            if (dumpDurationSteps(pw, "      ", "    Charge step durations:", dit.mChargeSteps, LOCAL_LOGV)) {
                                dumpDailyLevelStepSummary(pw, "        ", "Charge", dit.mChargeSteps, stringBuilder, outInt);
                            }
                            dumpDailyPackageChanges(pw, "    ", dit.mPackageChanges);
                        } else {
                            dumpDailyLevelStepSummary(pw, "    ", "Discharge", dit.mDischargeSteps, stringBuilder, outInt);
                            dumpDailyLevelStepSummary(pw, "    ", "Charge", dit.mChargeSteps, stringBuilder, outInt);
                        }
                    }
                    pw.println();
                }
                if ((flags & WIFI_SUPPL_STATE_SCANNING) == 0 && filtering) {
                    pw.println("  Current daily steps:");
                    dumpDailyLevelStepSummary(pw, "    ", "Discharge", dsteps, stringBuilder, outInt);
                    dumpDailyLevelStepSummary(pw, "    ", "Charge", csteps, stringBuilder, outInt);
                    curIndex = WIFI_SUPPL_STATE_INVALID;
                    while (true) {
                        dit = getDailyItemLocked(curIndex);
                        if (dit == null) {
                            curIndex += WIFI_SUPPL_STATE_DISCONNECTED;
                            if ((flags & WIFI_SUPPL_STATE_SCANNING) != 0) {
                                pw.println();
                            }
                            pw.print("  Daily from ");
                            pw.print(DateFormat.format("yyyy-MM-dd-HH-mm-ss", dit.mStartTime).toString());
                            pw.print(" to ");
                            pw.print(DateFormat.format("yyyy-MM-dd-HH-mm-ss", dit.mEndTime).toString());
                            pw.println(":");
                            if ((flags & WIFI_SUPPL_STATE_SCANNING) == 0) {
                            }
                            if (dumpDurationSteps(pw, "      ", "    Discharge step durations:", dit.mDischargeSteps, LOCAL_LOGV)) {
                                dumpDailyLevelStepSummary(pw, "        ", "Discharge", dit.mDischargeSteps, stringBuilder, outInt);
                            }
                            if (dumpDurationSteps(pw, "      ", "    Charge step durations:", dit.mChargeSteps, LOCAL_LOGV)) {
                                dumpDailyLevelStepSummary(pw, "        ", "Charge", dit.mChargeSteps, stringBuilder, outInt);
                            }
                            dumpDailyPackageChanges(pw, "    ", dit.mPackageChanges);
                        } else {
                            break;
                            pw.println();
                        }
                    }
                } else {
                    if (dumpDurationSteps(pw, "    ", "  Current daily discharge step durations:", dsteps, LOCAL_LOGV)) {
                        dumpDailyLevelStepSummary(pw, "      ", "Discharge", dsteps, stringBuilder, outInt);
                    }
                    if (dumpDurationSteps(pw, "    ", "  Current daily charge step durations:", csteps, LOCAL_LOGV)) {
                        dumpDailyLevelStepSummary(pw, "      ", "Charge", csteps, stringBuilder, outInt);
                    }
                    dumpDailyPackageChanges(pw, "    ", pkgc);
                    curIndex = WIFI_SUPPL_STATE_INVALID;
                    while (true) {
                        dit = getDailyItemLocked(curIndex);
                        if (dit == null) {
                            break;
                            pw.println();
                        } else {
                            curIndex += WIFI_SUPPL_STATE_DISCONNECTED;
                            if ((flags & WIFI_SUPPL_STATE_SCANNING) != 0) {
                                pw.println();
                            }
                            pw.print("  Daily from ");
                            pw.print(DateFormat.format("yyyy-MM-dd-HH-mm-ss", dit.mStartTime).toString());
                            pw.print(" to ");
                            pw.print(DateFormat.format("yyyy-MM-dd-HH-mm-ss", dit.mEndTime).toString());
                            pw.println(":");
                            if ((flags & WIFI_SUPPL_STATE_SCANNING) == 0) {
                            }
                            if (dumpDurationSteps(pw, "      ", "    Discharge step durations:", dit.mDischargeSteps, LOCAL_LOGV)) {
                                dumpDailyLevelStepSummary(pw, "        ", "Discharge", dit.mDischargeSteps, stringBuilder, outInt);
                            }
                            if (dumpDurationSteps(pw, "      ", "    Charge step durations:", dit.mChargeSteps, LOCAL_LOGV)) {
                                dumpDailyLevelStepSummary(pw, "        ", "Charge", dit.mChargeSteps, stringBuilder, outInt);
                            }
                            dumpDailyPackageChanges(pw, "    ", dit.mPackageChanges);
                        }
                    }
                }
            }
            if (!(filtering && (flags & WIFI_SUPPL_STATE_INTERFACE_DISABLED) == 0)) {
                pw.println("Statistics since last charge:");
                pw.println("  System starts: " + getStartCount() + ", currently on battery: " + getIsOnBattery());
                dumpLocked(context, pw, ProxyInfo.LOCAL_EXCL_LIST, WIFI_SUPPL_STATE_INVALID, reqUid, (flags & DUMP_DEVICE_WIFI_ONLY) != 0 ? true : LOCAL_LOGV);
                pw.println();
            }
        }
    }

    public void dumpCheckinLocked(Context context, PrintWriter pw, List<ApplicationInfo> apps, int flags, long histStart) {
        int i;
        prepareForDumpLocked();
        String str = VERSION_DATA;
        Object[] objArr = new Object[WIFI_SUPPL_STATE_SCANNING];
        objArr[WIFI_SUPPL_STATE_INVALID] = CHECKIN_VERSION;
        objArr[WIFI_SUPPL_STATE_DISCONNECTED] = Integer.valueOf(getParcelVersion());
        objArr[WIFI_SUPPL_STATE_INTERFACE_DISABLED] = getStartPlatformVersion();
        objArr[WIFI_SUPPL_STATE_INACTIVE] = getEndPlatformVersion();
        dumpLine(pw, WIFI_SUPPL_STATE_INVALID, "i", str, objArr);
        long now = getHistoryBaseTime() + SystemClock.elapsedRealtime();
        boolean filtering = (flags & JOB) != 0 ? true : LOCAL_LOGV;
        if (!((flags & FLASHLIGHT_TURNED_ON) == 0 && (flags & WIFI_SUPPL_STATE_FOUR_WAY_HANDSHAKE) == 0) && startIteratingHistoryLocked()) {
            i = WIFI_SUPPL_STATE_INVALID;
            while (i < getHistoryStringPoolSize()) {
                try {
                    pw.print(WIFI_SUPPL_STATE_GROUP_HANDSHAKE);
                    pw.print(',');
                    pw.print(HISTORY_STRING_POOL);
                    pw.print(',');
                    pw.print(i);
                    pw.print(",");
                    pw.print(getHistoryTagPoolUid(i));
                    pw.print(",\"");
                    pw.print(getHistoryTagPoolString(i).replace("\\", "\\\\").replace("\"", "\\\""));
                    pw.print("\"");
                    pw.println();
                    i += WIFI_SUPPL_STATE_DISCONNECTED;
                } finally {
                    finishIteratingHistoryLocked();
                }
            }
            dumpHistoryLocked(pw, flags, histStart, true);
        }
        if (!filtering || (flags & WIFI_SUPPL_STATE_ASSOCIATING) != 0) {
            String[] lineArgs;
            if (apps != null) {
                Pair<ArrayList<String>, MutableBoolean> pkgs;
                SparseArray<Pair<ArrayList<String>, MutableBoolean>> uids = new SparseArray();
                for (i = WIFI_SUPPL_STATE_INVALID; i < apps.size(); i += WIFI_SUPPL_STATE_DISCONNECTED) {
                    ApplicationInfo ai = (ApplicationInfo) apps.get(i);
                    pkgs = (Pair) uids.get(UserHandle.getAppId(ai.uid));
                    if (pkgs == null) {
                        pkgs = new Pair(new ArrayList(), new MutableBoolean(LOCAL_LOGV));
                        uids.put(UserHandle.getAppId(ai.uid), pkgs);
                    }
                    ((ArrayList) pkgs.first).add(ai.packageName);
                }
                SparseArray<? extends Uid> uidStats = getUidStats();
                int NU = uidStats.size();
                lineArgs = new String[WIFI_SUPPL_STATE_INTERFACE_DISABLED];
                for (i = WIFI_SUPPL_STATE_INVALID; i < NU; i += WIFI_SUPPL_STATE_DISCONNECTED) {
                    int uid = UserHandle.getAppId(uidStats.keyAt(i));
                    pkgs = (Pair) uids.get(uid);
                    if (!(pkgs == null || ((MutableBoolean) pkgs.second).value)) {
                        ((MutableBoolean) pkgs.second).value = true;
                        for (int j = WIFI_SUPPL_STATE_INVALID; j < ((ArrayList) pkgs.first).size(); j += WIFI_SUPPL_STATE_DISCONNECTED) {
                            lineArgs[WIFI_SUPPL_STATE_INVALID] = Integer.toString(uid);
                            lineArgs[WIFI_SUPPL_STATE_DISCONNECTED] = (String) ((ArrayList) pkgs.first).get(j);
                            dumpLine(pw, WIFI_SUPPL_STATE_INVALID, "i", UID_DATA, lineArgs);
                        }
                    }
                }
            }
            if (!(filtering && (flags & WIFI_SUPPL_STATE_INTERFACE_DISABLED) == 0)) {
                dumpDurationSteps(pw, ProxyInfo.LOCAL_EXCL_LIST, DISCHARGE_STEP_DATA, getDischargeLevelStepTracker(), true);
                lineArgs = new String[WIFI_SUPPL_STATE_DISCONNECTED];
                long timeRemaining = computeBatteryTimeRemaining(SystemClock.elapsedRealtime());
                if (timeRemaining >= 0) {
                    lineArgs[WIFI_SUPPL_STATE_INVALID] = Long.toString(timeRemaining);
                    dumpLine(pw, WIFI_SUPPL_STATE_INVALID, "i", DISCHARGE_TIME_REMAIN_DATA, lineArgs);
                }
                dumpDurationSteps(pw, ProxyInfo.LOCAL_EXCL_LIST, CHARGE_STEP_DATA, getChargeLevelStepTracker(), true);
                timeRemaining = computeChargeTimeRemaining(SystemClock.elapsedRealtime());
                if (timeRemaining >= 0) {
                    lineArgs[WIFI_SUPPL_STATE_INVALID] = Long.toString(timeRemaining);
                    dumpLine(pw, WIFI_SUPPL_STATE_INVALID, "i", CHARGE_TIME_REMAIN_DATA, lineArgs);
                }
                dumpCheckinLocked(context, pw, (int) WIFI_SUPPL_STATE_INVALID, -1, (flags & DUMP_DEVICE_WIFI_ONLY) != 0 ? true : LOCAL_LOGV);
            }
        }
    }
}
