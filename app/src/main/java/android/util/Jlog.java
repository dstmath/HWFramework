package android.util;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.huawei.pgmng.log.LogPower;

public final class Jlog {
    private static final String FRAMETEST_FLAG_FILE = "etc/frametest.xml";
    public static final int HW_LOG_ID_JANK = 6;
    public static final long INFLATE_TIME_LIMIT_NS = 16000000;
    public static final int JANK_UPDATE_PROP_DELAY = 900000;
    public static final int JANK_UPDATE_PROP_INTERVAL = 30000;
    public static final long MAX_VIOLATION_DURATION_MS = 8000;
    public static final long MIN_VIOLATION_DURATION_MS = 320;
    public static final long OBTAINVIEW_TIME_LIMIT_NS = 16000000;
    public static final int OPENGL_SKIPPED_FRAME_LIMIT = 30;
    private static final String PERFTEST_FLAG_FILE = "etc/perftest.xml";
    public static final int PERF_CTRL_TYPE_HIGHPERF = 0;
    public static final int PERF_CTRL_TYPE_LOW_VOLTAGE = 3;
    public static final int PERF_CTRL_TYPE_MAX = 4;
    public static final int PERF_CTRL_TYPE_SPEC_SCENE = 1;
    public static final int PERF_CTRL_TYPE_THERMAL_PROTECT = 2;
    public static final int PERF_EVENT_ANIMATION = 7;
    public static final int PERF_EVENT_APP_START = 4;
    public static final int PERF_EVENT_BOOT_COMPLETE = 0;
    public static final int PERF_EVENT_INTERACTION = 1;
    public static final int PERF_EVENT_LIST_FLING = 8;
    public static final int PERF_EVENT_LUCKYMONEY = 12;
    public static final int PERF_EVENT_MAX = 13;
    public static final int PERF_EVENT_OFF = 0;
    public static final int PERF_EVENT_ON = 1;
    public static final int PERF_EVENT_PROBE = 4097;
    public static final int PERF_EVENT_RAW_REQ = 4096;
    public static final int PERF_EVENT_ROTATING = 6;
    public static final int PERF_EVENT_SCREEN_OFF = 10;
    public static final int PERF_EVENT_SCREEN_ON = 9;
    public static final int PERF_EVENT_STATUSBAR = 11;
    public static final int PERF_EVENT_VSYNC_OFF = 3;
    public static final int PERF_EVENT_VSYNC_ON = 2;
    public static final int PERF_EVENT_WINDOW_SWITCH = 5;
    public static final int PERF_HMP_POLICY_STATE_OFF = 0;
    public static final int PERF_HMP_POLICY_STATE_ON = 1;
    public static final int PERF_HMP_PRIORITY_0 = 0;
    public static final int PERF_HMP_PRIORITY_1 = 1;
    public static final int PERF_HMP_PRIORITY_2 = 2;
    public static final int PERF_HMP_PRIORITY_3 = 3;
    public static final int PERF_HMP_PRIORITY_4 = 4;
    public static final int PERF_HMP_PRIORITY_5 = 5;
    public static final int PERF_HMP_PRIORITY_MAX = 6;
    public static final int PERF_TAG_B_CPU_CUR = 6;
    public static final int PERF_TAG_B_CPU_MAX = 5;
    public static final int PERF_TAG_B_CPU_MIN = 4;
    public static final int PERF_TAG_CTRL_TYPE = 0;
    public static final int PERF_TAG_DDR_CUR = 12;
    public static final int PERF_TAG_DDR_MAX = 11;
    public static final int PERF_TAG_DDR_MIN = 10;
    public static final int PERF_TAG_GPU_CUR = 9;
    public static final int PERF_TAG_GPU_MAX = 8;
    public static final int PERF_TAG_GPU_MIN = 7;
    public static final int PERF_TAG_HMP_DN_THRES = 14;
    public static final int PERF_TAG_HMP_POLICY_STATE = 16;
    public static final int PERF_TAG_HMP_PRIORITY = 15;
    public static final int PERF_TAG_HMP_UP_THRES = 13;
    public static final int PERF_TAG_IPA_CONTROL_TEMP = 18;
    public static final int PERF_TAG_IPA_SUSTAINABLE_POWER = 19;
    public static final int PERF_TAG_IPA_SWITCH_TEMP = 17;
    public static final int PERF_TAG_L_CPU_CUR = 3;
    public static final int PERF_TAG_L_CPU_MAX = 2;
    public static final int PERF_TAG_L_CPU_MIN = 1;
    public static final int PERF_TAG_MAX = 20;
    public static final int REQUEST_FAILED = -1;
    public static final int REQUEST_SUCCEEDED = 0;
    public static final long SETUPVIEW_TIME_LIMIT_NS = 16000000;
    public static final int SKIPPED_FRAME_LIMIT = 5;
    private static IBinder mService;
    private static final boolean mbBetaUser = false;
    private static boolean mbEmuiAnimation;
    private static boolean mbFrameTestEnabled;
    private static boolean mbPerfTestEnabled;
    private static final boolean mbUBMEnable = false;
    private static int misPerfhubEnable;
    private static int mnEmuiAnimationSkipFrames;
    private static String msEmuiAnimationName;
    private static StringBuilder msEmuiAnimationSkippedFrames;
    private static String msLaunchingPkgName;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.Jlog.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.Jlog.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.util.Jlog.<clinit>():void");
    }

    public static native int print_janklog_native(int i, int i2, String str);

    private Jlog() {
    }

    public static boolean isPerfTest() {
        return mbPerfTestEnabled;
    }

    public static boolean isFrameTest() {
        return mbFrameTestEnabled;
    }

    public static boolean isBetaUser() {
        return mbBetaUser;
    }

    public static boolean isUBMEnable() {
        return mbUBMEnable ? mbBetaUser : false;
    }

    public static boolean isEnable() {
        return true;
    }

    public static String extractAppName(String msg) {
        int nLen = msg.length();
        if (nLen <= 0 || !msg.endsWith("}")) {
            return msg;
        }
        int nStartPos = msg.indexOf(" u", REQUEST_SUCCEEDED);
        if (nStartPos < 0) {
            return msg;
        }
        nStartPos = msg.indexOf(" ", nStartPos + PERF_TAG_L_CPU_MAX);
        if (nStartPos < 0) {
            return msg;
        }
        return msg.substring(nStartPos + PERF_TAG_L_CPU_MIN, nLen + REQUEST_FAILED);
    }

    public static void animationStart(String msg) {
        mbEmuiAnimation = true;
        mnEmuiAnimationSkipFrames = REQUEST_SUCCEEDED;
        msEmuiAnimationSkippedFrames.delete(REQUEST_SUCCEEDED, msEmuiAnimationSkippedFrames.length());
        msEmuiAnimationName = msg;
    }

    public static void animationEnd() {
        mbEmuiAnimation = false;
        mnEmuiAnimationSkipFrames = REQUEST_SUCCEEDED;
    }

    public static void animationSkipFrames(long num) {
        if (mbEmuiAnimation) {
            if (msEmuiAnimationSkippedFrames.length() == 0) {
                msEmuiAnimationSkippedFrames.append("Skipped ");
            } else {
                msEmuiAnimationSkippedFrames.append("+");
            }
            msEmuiAnimationSkippedFrames.append(num);
            mnEmuiAnimationSkipFrames = (int) (((long) mnEmuiAnimationSkipFrames) + num);
            if (num >= 3 || mnEmuiAnimationSkipFrames >= SKIPPED_FRAME_LIMIT) {
                d(LogPower.BLE_SOCKECT_CLOSED, msEmuiAnimationName, mnEmuiAnimationSkipFrames, msEmuiAnimationSkippedFrames.toString() + " frames");
                animationEnd();
            }
        }
    }

    public static boolean isEmuiAnimation() {
        return mbEmuiAnimation;
    }

    public static void coldLaunchingAppBegin(String pkgname) {
        if (pkgname != null) {
            msLaunchingPkgName = pkgname;
            print_janklog_native(PERF_TAG_L_CPU_CUR, MetricsEvent.DATA_USAGE_LIST, "#ARG1:<" + pkgname + ">");
        }
    }

    public static void warmLaunchingAppBegin(String pkgname) {
        if (pkgname != null) {
            msLaunchingPkgName = pkgname;
            print_janklog_native(PERF_TAG_L_CPU_CUR, MetricsEvent.DISPLAY_SCREEN_ZOOM, "#ARG1:<" + pkgname + ">");
        }
    }

    public static void coldLaunchingAppEnd(String pkgname) {
        if (msLaunchingPkgName != null && pkgname != null && msLaunchingPkgName.length() >= PERF_TAG_L_CPU_MIN) {
            if (pkgname.startsWith(msLaunchingPkgName)) {
                print_janklog_native(PERF_TAG_L_CPU_CUR, MetricsEvent.BILLING_CYCLE, "#ARG1:<" + pkgname + ">");
                msLaunchingPkgName = "";
                return;
            }
            msLaunchingPkgName = "";
        }
    }

    public static void warmLaunchingAppEnd(String pkgname) {
        if (msLaunchingPkgName != null && pkgname != null && msLaunchingPkgName.length() >= PERF_TAG_L_CPU_MIN) {
            if (pkgname.startsWith(msLaunchingPkgName)) {
                print_janklog_native(PERF_TAG_L_CPU_CUR, MetricsEvent.ACCESSIBILITY_FONT_SIZE, "#ARG1:<" + pkgname + ">");
                msLaunchingPkgName = "";
                return;
            }
            msLaunchingPkgName = "";
        }
    }

    public static int d(int tag, String msg) {
        return print_janklog_native(PERF_TAG_L_CPU_CUR, tag, msg);
    }

    public static int d(int tag, String arg1, String msg) {
        return print_janklog_native(PERF_TAG_L_CPU_CUR, tag, "#ARG1:<" + arg1 + ">" + msg);
    }

    public static int d(int tag, int arg2, String msg) {
        return print_janklog_native(PERF_TAG_L_CPU_CUR, tag, "#ARG2:<" + arg2 + ">" + msg);
    }

    public static int d(int tag, String arg1, int arg2, String msg) {
        return print_janklog_native(PERF_TAG_L_CPU_CUR, tag, "#ARG1:<" + arg1 + ">#ARG2:<" + arg2 + ">" + msg);
    }

    public static int v(int tag, String msg) {
        return print_janklog_native(PERF_TAG_L_CPU_MAX, tag, msg);
    }

    public static int i(int tag, String msg) {
        return print_janklog_native(PERF_TAG_B_CPU_MIN, tag, msg);
    }

    public static int w(int tag, String msg) {
        return print_janklog_native(SKIPPED_FRAME_LIMIT, tag, msg);
    }

    public static int e(int tag, String msg) {
        return print_janklog_native(PERF_TAG_B_CPU_CUR, tag, msg);
    }

    public static boolean isHisiChipset() {
        String chipType = SystemProperties.get("ro.board.platform", "");
        if (chipType.startsWith("msm") || chipType.startsWith("qsc") || chipType.startsWith("MSM") || chipType.startsWith("QSC")) {
            return false;
        }
        return true;
    }

    public static int perfEvent(int eventId, String PackageName, int... payload) {
        int i = REQUEST_SUCCEEDED;
        if (REQUEST_FAILED == misPerfhubEnable) {
            if (isHisiChipset()) {
                misPerfhubEnable = PERF_TAG_L_CPU_MIN;
            } else {
                misPerfhubEnable = REQUEST_SUCCEEDED;
            }
        }
        if (misPerfhubEnable == 0) {
            return REQUEST_FAILED;
        }
        int pid = Process.myPid();
        int tid = Process.myTid();
        if (mService == null) {
            mService = ServiceManager.checkService("perfhub");
            if (mService == null) {
                return REQUEST_FAILED;
            }
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        String msg = "pid=" + pid + "|tid=" + tid;
        if (PackageName != null && PackageName.length() > 0) {
            msg = msg + "|" + PackageName;
        }
        data.writeInterfaceToken("android.os.IPerfHub");
        data.writeInt(eventId);
        data.writeString(msg);
        data.writeInt(payload.length);
        int length = payload.length;
        while (i < length) {
            data.writeInt(payload[i]);
            i += PERF_TAG_L_CPU_MIN;
        }
        try {
            mService.transact(PERF_TAG_L_CPU_MIN, data, reply, PERF_TAG_L_CPU_MIN);
        } catch (RemoteException e) {
            mService = null;
        }
        data.recycle();
        reply.recycle();
        return REQUEST_SUCCEEDED;
    }
}
