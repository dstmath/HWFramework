package android.os.health;

import android.os.health.HealthKeys.Constant;
import android.os.health.HealthKeys.Constants;

public final class UidHealthStats {
    public static final Constants CONSTANTS = null;
    @Constant(type = 1)
    public static final int MEASUREMENT_BLUETOOTH_IDLE_MS = 10020;
    @Constant(type = 1)
    public static final int MEASUREMENT_BLUETOOTH_POWER_MAMS = 10023;
    @Constant(type = 1)
    public static final int MEASUREMENT_BLUETOOTH_RX_BYTES = 10052;
    @Constant(type = 1)
    public static final int MEASUREMENT_BLUETOOTH_RX_MS = 10021;
    @Constant(type = 1)
    public static final int MEASUREMENT_BLUETOOTH_RX_PACKETS = 10058;
    @Constant(type = 1)
    public static final int MEASUREMENT_BLUETOOTH_TX_BYTES = 10053;
    @Constant(type = 1)
    public static final int MEASUREMENT_BLUETOOTH_TX_MS = 10022;
    @Constant(type = 1)
    public static final int MEASUREMENT_BLUETOOTH_TX_PACKETS = 10059;
    @Constant(type = 1)
    public static final int MEASUREMENT_BUTTON_USER_ACTIVITY_COUNT = 10046;
    @Constant(type = 1)
    public static final int MEASUREMENT_CPU_POWER_MAMS = 10064;
    @Constant(type = 1)
    public static final int MEASUREMENT_MOBILE_IDLE_MS = 10024;
    @Constant(type = 1)
    public static final int MEASUREMENT_MOBILE_POWER_MAMS = 10027;
    @Constant(type = 1)
    public static final int MEASUREMENT_MOBILE_RX_BYTES = 10048;
    @Constant(type = 1)
    public static final int MEASUREMENT_MOBILE_RX_MS = 10025;
    @Constant(type = 1)
    public static final int MEASUREMENT_MOBILE_RX_PACKETS = 10054;
    @Constant(type = 1)
    public static final int MEASUREMENT_MOBILE_TX_BYTES = 10049;
    @Constant(type = 1)
    public static final int MEASUREMENT_MOBILE_TX_MS = 10026;
    @Constant(type = 1)
    public static final int MEASUREMENT_MOBILE_TX_PACKETS = 10055;
    @Constant(type = 1)
    public static final int MEASUREMENT_OTHER_USER_ACTIVITY_COUNT = 10045;
    @Constant(type = 1)
    public static final int MEASUREMENT_REALTIME_BATTERY_MS = 10001;
    @Constant(type = 1)
    public static final int MEASUREMENT_REALTIME_SCREEN_OFF_BATTERY_MS = 10003;
    @Constant(type = 1)
    public static final int MEASUREMENT_SYSTEM_CPU_TIME_MS = 10063;
    @Constant(type = 1)
    public static final int MEASUREMENT_TOUCH_USER_ACTIVITY_COUNT = 10047;
    @Constant(type = 1)
    public static final int MEASUREMENT_UPTIME_BATTERY_MS = 10002;
    @Constant(type = 1)
    public static final int MEASUREMENT_UPTIME_SCREEN_OFF_BATTERY_MS = 10004;
    @Constant(type = 1)
    public static final int MEASUREMENT_USER_CPU_TIME_MS = 10062;
    @Constant(type = 1)
    public static final int MEASUREMENT_WIFI_FULL_LOCK_MS = 10029;
    @Constant(type = 1)
    public static final int MEASUREMENT_WIFI_IDLE_MS = 10016;
    @Constant(type = 1)
    public static final int MEASUREMENT_WIFI_MULTICAST_MS = 10031;
    @Constant(type = 1)
    public static final int MEASUREMENT_WIFI_POWER_MAMS = 10019;
    @Constant(type = 1)
    public static final int MEASUREMENT_WIFI_RUNNING_MS = 10028;
    @Constant(type = 1)
    public static final int MEASUREMENT_WIFI_RX_BYTES = 10050;
    @Constant(type = 1)
    public static final int MEASUREMENT_WIFI_RX_MS = 10017;
    @Constant(type = 1)
    public static final int MEASUREMENT_WIFI_RX_PACKETS = 10056;
    @Constant(type = 1)
    public static final int MEASUREMENT_WIFI_TX_BYTES = 10051;
    @Constant(type = 1)
    public static final int MEASUREMENT_WIFI_TX_MS = 10018;
    @Constant(type = 1)
    public static final int MEASUREMENT_WIFI_TX_PACKETS = 10057;
    @Constant(type = 2)
    public static final int STATS_PACKAGES = 10015;
    @Constant(type = 2)
    public static final int STATS_PIDS = 10013;
    @Constant(type = 2)
    public static final int STATS_PROCESSES = 10014;
    @Constant(type = 3)
    public static final int TIMERS_JOBS = 10010;
    @Constant(type = 3)
    public static final int TIMERS_SENSORS = 10012;
    @Constant(type = 3)
    public static final int TIMERS_SYNCS = 10009;
    @Constant(type = 3)
    public static final int TIMERS_WAKELOCKS_DRAW = 10008;
    @Constant(type = 3)
    public static final int TIMERS_WAKELOCKS_FULL = 10005;
    @Constant(type = 3)
    public static final int TIMERS_WAKELOCKS_PARTIAL = 10006;
    @Constant(type = 3)
    public static final int TIMERS_WAKELOCKS_WINDOW = 10007;
    @Constant(type = 0)
    public static final int TIMER_AUDIO = 10032;
    @Constant(type = 0)
    public static final int TIMER_BLUETOOTH_SCAN = 10037;
    @Constant(type = 0)
    public static final int TIMER_CAMERA = 10035;
    @Constant(type = 0)
    public static final int TIMER_FLASHLIGHT = 10034;
    @Constant(type = 0)
    public static final int TIMER_FOREGROUND_ACTIVITY = 10036;
    @Constant(type = 0)
    public static final int TIMER_GPS_SENSOR = 10011;
    @Constant(type = 0)
    public static final int TIMER_MOBILE_RADIO_ACTIVE = 10061;
    @Constant(type = 0)
    public static final int TIMER_PROCESS_STATE_BACKGROUND_MS = 10042;
    @Constant(type = 0)
    public static final int TIMER_PROCESS_STATE_CACHED_MS = 10043;
    @Constant(type = 0)
    public static final int TIMER_PROCESS_STATE_FOREGROUND_MS = 10041;
    @Constant(type = 0)
    public static final int TIMER_PROCESS_STATE_FOREGROUND_SERVICE_MS = 10039;
    @Constant(type = 0)
    public static final int TIMER_PROCESS_STATE_TOP_MS = 10038;
    @Constant(type = 0)
    public static final int TIMER_PROCESS_STATE_TOP_SLEEPING_MS = 10040;
    @Constant(type = 0)
    public static final int TIMER_VIBRATOR = 10044;
    @Constant(type = 0)
    public static final int TIMER_VIDEO = 10033;
    @Constant(type = 0)
    public static final int TIMER_WIFI_SCAN = 10030;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.health.UidHealthStats.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.health.UidHealthStats.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.os.health.UidHealthStats.<clinit>():void");
    }

    private UidHealthStats() {
    }
}
