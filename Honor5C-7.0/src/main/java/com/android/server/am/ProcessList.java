package com.android.server.am;

import android.content.res.Resources;
import android.graphics.Point;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.Build;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.util.MemInfoReader;
import com.android.server.wm.AppTransition;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.WindowManagerService.H;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public final class ProcessList {
    static final int BACKUP_APP_ADJ = 300;
    static final int BSERVICE_APP_THRESHOLD = 0;
    static final int CACHED_APP_MAX_ADJ = 906;
    static final int CACHED_APP_MIN_ADJ = 900;
    static final int EMPTY_APP_PERCENT = 0;
    static final boolean ENABLE_B_SERVICE_PROPAGATION = false;
    static final int FOREGROUND_APP_ADJ = 0;
    static final int HEAVY_WEIGHT_APP_ADJ = 400;
    static final int HOME_APP_ADJ = 600;
    static final int INVALID_ADJ = -10000;
    static final byte LMK_MEMORYCOMPACT = (byte) 53;
    static final byte LMK_MEMORYSHRINK = (byte) 52;
    static final byte LMK_PROCPRIO = (byte) 1;
    static final byte LMK_PROCRECLAIM = (byte) 54;
    static final byte LMK_PROCREMOVE = (byte) 2;
    static final byte LMK_TARGET = (byte) 0;
    static final int MAX_CACHED_APPS = 0;
    private static final int MAX_EMPTY_APPS = 0;
    static final long MAX_EMPTY_TIME = 1800000;
    static final int MIN_BSERVICE_AGING_TIME = 0;
    static final int MIN_CACHED_APPS = 2;
    static final int MIN_CRASH_INTERVAL = 60000;
    static final int NATIVE_ADJ = -1000;
    static final int PAGE_SIZE = 4096;
    static final int PERCEPTIBLE_APP_ADJ = 200;
    static final int PERSISTENT_PROC_ADJ = -800;
    static final int PERSISTENT_SERVICE_ADJ = -700;
    static final int PREVIOUS_APP_ADJ = 700;
    public static final int PROC_MEM_CACHED = 4;
    public static final int PROC_MEM_IMPORTANT = 2;
    public static final int PROC_MEM_PERSISTENT = 0;
    public static final int PROC_MEM_SERVICE = 3;
    public static final int PROC_MEM_TOP = 1;
    public static final int PSS_ALL_INTERVAL = 600000;
    private static final int PSS_FIRST_BACKGROUND_INTERVAL = 20000;
    private static final int PSS_FIRST_CACHED_INTERVAL = 30000;
    private static final int PSS_FIRST_TOP_INTERVAL = 10000;
    public static final int PSS_MAX_INTERVAL = 1800000;
    public static final int PSS_MIN_TIME_FROM_STATE_CHANGE = 15000;
    public static final int PSS_SAFE_TIME_FROM_STATE_CHANGE = 1000;
    private static final int PSS_SAME_CACHED_INTERVAL = 1800000;
    private static final int PSS_SAME_IMPORTANT_INTERVAL = 900000;
    private static final int PSS_SAME_SERVICE_INTERVAL = 1200000;
    private static final int PSS_SHORT_INTERVAL = 120000;
    private static final int PSS_TEST_FIRST_BACKGROUND_INTERVAL = 5000;
    private static final int PSS_TEST_FIRST_TOP_INTERVAL = 3000;
    public static final int PSS_TEST_MIN_TIME_FROM_STATE_CHANGE = 10000;
    private static final int PSS_TEST_SAME_BACKGROUND_INTERVAL = 15000;
    private static final int PSS_TEST_SAME_IMPORTANT_INTERVAL = 10000;
    static final int SCHED_GROUP_BACKGROUND = 0;
    static final int SCHED_GROUP_DEFAULT = 1;
    static final int SCHED_GROUP_TOP_APP = 2;
    static final int SCHED_GROUP_TOP_APP_BOUND = 3;
    static final int SERVICE_ADJ = 500;
    static final int SERVICE_B_ADJ = 800;
    static final int SYSTEM_ADJ = -900;
    private static final String TAG = null;
    static final int TRIM_CACHED_APPS = 0;
    static final int TRIM_CACHE_PERCENT = 0;
    static final int TRIM_CRITICAL_THRESHOLD = 3;
    static final int TRIM_EMPTY_APPS = 0;
    static final int TRIM_EMPTY_PERCENT = 0;
    static final long TRIM_ENABLE_MEMORY = 0;
    static final int TRIM_LOW_THRESHOLD = 5;
    static final int UNKNOWN_ADJ = 1001;
    static final boolean USE_TRIM_SETTINGS = false;
    static final int VISIBLE_APP_ADJ = 100;
    static final int VISIBLE_APP_LAYER_MAX = 99;
    private static final long[] sFirstAwakePssTimes = null;
    private static OutputStream sLmkdOutputStream;
    private static LocalSocket sLmkdSocket;
    private static final int[] sProcStateToProcMem = null;
    private static final long[] sSameAwakePssTimes = null;
    private static final long[] sTestFirstAwakePssTimes = null;
    private static final long[] sTestSameAwakePssTimes = null;
    private long mCachedRestoreLevel;
    private boolean mHaveDisplaySize;
    private final int[] mOomAdj;
    private final int[] mOomMinFree;
    private final int[] mOomMinFreeHigh;
    private final int[] mOomMinFreeLow;
    private final long mTotalMemMb;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.am.ProcessList.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.am.ProcessList.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.ProcessList.<clinit>():void");
    }

    public static boolean allowTrim() {
        return Process.getTotalMemory() < TRIM_ENABLE_MEMORY ? true : USE_TRIM_SETTINGS;
    }

    public static int computeTrimEmptyApps() {
        if (USE_TRIM_SETTINGS && allowTrim()) {
            return (MAX_EMPTY_APPS * TRIM_EMPTY_PERCENT) / VISIBLE_APP_ADJ;
        }
        return MAX_EMPTY_APPS / SCHED_GROUP_TOP_APP;
    }

    public static int computeTrimCachedApps() {
        if (USE_TRIM_SETTINGS && allowTrim()) {
            return (MAX_CACHED_APPS * TRIM_CACHE_PERCENT) / VISIBLE_APP_ADJ;
        }
        return (MAX_CACHED_APPS - MAX_EMPTY_APPS) / TRIM_CRITICAL_THRESHOLD;
    }

    ProcessList() {
        this.mOomAdj = new int[]{TRIM_EMPTY_PERCENT, VISIBLE_APP_ADJ, PERCEPTIBLE_APP_ADJ, BACKUP_APP_ADJ, CACHED_APP_MIN_ADJ, CACHED_APP_MAX_ADJ};
        this.mOomMinFreeLow = new int[]{12288, 18432, 24576, 36864, 43008, 49152};
        this.mOomMinFreeHigh = new int[]{73728, 92160, 110592, 129024, 147456, 184320};
        this.mOomMinFree = new int[this.mOomAdj.length];
        MemInfoReader minfo = new MemInfoReader();
        minfo.readMemInfo();
        this.mTotalMemMb = minfo.getTotalSize() / 1048576;
        updateOomLevels(TRIM_EMPTY_PERCENT, TRIM_EMPTY_PERCENT, USE_TRIM_SETTINGS);
    }

    void applyDisplaySize(WindowManagerService wm) {
        if (!this.mHaveDisplaySize) {
            Point p = new Point();
            wm.getBaseDisplaySize(TRIM_EMPTY_PERCENT, p);
            if (p.x != 0 && p.y != 0) {
                updateOomLevels(p.x, p.y, true);
                this.mHaveDisplaySize = true;
            }
        }
    }

    private void updateOomLevels(int displayWidth, int displayHeight, boolean write) {
        float scale;
        float scaleMem = ((float) (this.mTotalMemMb - 350)) / 350.0f;
        float scaleDisp = (((float) (displayWidth * displayHeight)) - 384000.0f) / ((float) 640000);
        if (scaleMem > scaleDisp) {
            scale = scaleMem;
        } else {
            scale = scaleDisp;
        }
        if (scale < 0.0f) {
            scale = 0.0f;
        } else if (scale > 1.0f) {
            scale = 1.0f;
        }
        int minfree_adj = Resources.getSystem().getInteger(17694729);
        int minfree_abs = Resources.getSystem().getInteger(17694728);
        boolean is64bit = Build.SUPPORTED_64_BIT_ABIS.length > 0 ? true : USE_TRIM_SETTINGS;
        int i = TRIM_EMPTY_PERCENT;
        while (true) {
            int length = this.mOomAdj.length;
            if (i >= r0) {
                break;
            }
            int low = this.mOomMinFreeLow[i];
            int high = this.mOomMinFreeHigh[i];
            if (is64bit) {
                if (i == PROC_MEM_CACHED) {
                    high = (high * TRIM_CRITICAL_THRESHOLD) / SCHED_GROUP_TOP_APP;
                } else if (i == TRIM_LOW_THRESHOLD) {
                    high = (high * 7) / PROC_MEM_CACHED;
                }
            }
            this.mOomMinFree[i] = (int) (((float) low) + (((float) (high - low)) * scale));
            i += SCHED_GROUP_DEFAULT;
        }
        if (minfree_abs >= 0) {
            i = TRIM_EMPTY_PERCENT;
            while (true) {
                length = this.mOomAdj.length;
                if (i >= r0) {
                    break;
                }
                this.mOomMinFree[i] = (int) ((((float) minfree_abs) * ((float) this.mOomMinFree[i])) / ((float) this.mOomMinFree[this.mOomAdj.length - 1]));
                i += SCHED_GROUP_DEFAULT;
            }
        }
        if (minfree_adj != 0) {
            i = TRIM_EMPTY_PERCENT;
            while (true) {
                length = this.mOomAdj.length;
                if (i >= r0) {
                    break;
                }
                int[] iArr = this.mOomMinFree;
                iArr[i] = iArr[i] + ((int) ((((float) minfree_adj) * ((float) this.mOomMinFree[i])) / ((float) this.mOomMinFree[this.mOomAdj.length - 1])));
                if (this.mOomMinFree[i] < 0) {
                    this.mOomMinFree[i] = TRIM_EMPTY_PERCENT;
                }
                i += SCHED_GROUP_DEFAULT;
            }
        }
        this.mCachedRestoreLevel = (getMemLevel(CACHED_APP_MAX_ADJ) / 1024) / 3;
        int reserve = (((displayWidth * displayHeight) * PROC_MEM_CACHED) * TRIM_CRITICAL_THRESHOLD) / DumpState.DUMP_PROVIDERS;
        int reserve_adj = Resources.getSystem().getInteger(17694731);
        int reserve_abs = Resources.getSystem().getInteger(17694730);
        if (reserve_abs >= 0) {
            reserve = reserve_abs;
        }
        if (reserve_adj != 0) {
            reserve += reserve_adj;
            if (reserve < 0) {
                reserve = TRIM_EMPTY_PERCENT;
            }
        }
        if (write) {
            ByteBuffer buf = ByteBuffer.allocate(((this.mOomAdj.length * SCHED_GROUP_TOP_APP) + SCHED_GROUP_DEFAULT) * PROC_MEM_CACHED);
            buf.putInt(TRIM_EMPTY_PERCENT);
            i = TRIM_EMPTY_PERCENT;
            while (true) {
                length = this.mOomAdj.length;
                if (i < r0) {
                    buf.putInt((this.mOomMinFree[i] * DumpState.DUMP_PROVIDERS) / PAGE_SIZE);
                    buf.putInt(this.mOomAdj[i]);
                    i += SCHED_GROUP_DEFAULT;
                } else {
                    writeLmkd(buf);
                    SystemProperties.set("sys.sysctl.extra_free_kbytes", Integer.toString(reserve));
                    return;
                }
            }
        }
    }

    public static int computeEmptyProcessLimit(int totalProcessLimit) {
        if (USE_TRIM_SETTINGS && allowTrim()) {
            return (EMPTY_APP_PERCENT * totalProcessLimit) / VISIBLE_APP_ADJ;
        }
        return totalProcessLimit / SCHED_GROUP_TOP_APP;
    }

    private static String buildOomTag(String prefix, String space, int val, int base) {
        if (val != base) {
            return prefix + "+" + Integer.toString(val - base);
        }
        if (space == null) {
            return prefix;
        }
        return prefix + "  ";
    }

    public static String makeOomAdjString(int setAdj) {
        if (setAdj >= CACHED_APP_MIN_ADJ) {
            return buildOomTag("cch", "  ", setAdj, CACHED_APP_MIN_ADJ);
        }
        if (setAdj >= SERVICE_B_ADJ) {
            return buildOomTag("svcb ", null, setAdj, SERVICE_B_ADJ);
        }
        if (setAdj >= PREVIOUS_APP_ADJ) {
            return buildOomTag("prev ", null, setAdj, PREVIOUS_APP_ADJ);
        }
        if (setAdj >= HOME_APP_ADJ) {
            return buildOomTag("home ", null, setAdj, HOME_APP_ADJ);
        }
        if (setAdj >= SERVICE_ADJ) {
            return buildOomTag("svc  ", null, setAdj, SERVICE_ADJ);
        }
        if (setAdj >= HEAVY_WEIGHT_APP_ADJ) {
            return buildOomTag("hvy  ", null, setAdj, HEAVY_WEIGHT_APP_ADJ);
        }
        if (setAdj >= BACKUP_APP_ADJ) {
            return buildOomTag("bkup ", null, setAdj, BACKUP_APP_ADJ);
        }
        if (setAdj >= PERCEPTIBLE_APP_ADJ) {
            return buildOomTag("prcp ", null, setAdj, PERCEPTIBLE_APP_ADJ);
        }
        if (setAdj >= VISIBLE_APP_ADJ) {
            return buildOomTag("vis  ", null, setAdj, VISIBLE_APP_ADJ);
        }
        if (setAdj >= 0) {
            return buildOomTag("fore ", null, setAdj, TRIM_EMPTY_PERCENT);
        }
        if (setAdj >= PERSISTENT_SERVICE_ADJ) {
            return buildOomTag("psvc ", null, setAdj, PERSISTENT_SERVICE_ADJ);
        }
        if (setAdj >= PERSISTENT_PROC_ADJ) {
            return buildOomTag("pers ", null, setAdj, PERSISTENT_PROC_ADJ);
        }
        if (setAdj >= SYSTEM_ADJ) {
            return buildOomTag("sys  ", null, setAdj, SYSTEM_ADJ);
        }
        if (setAdj >= NATIVE_ADJ) {
            return buildOomTag("ntv  ", null, setAdj, NATIVE_ADJ);
        }
        return Integer.toString(setAdj);
    }

    public static String makeProcStateString(int curProcState) {
        switch (curProcState) {
            case AppTransition.TRANSIT_UNSET /*-1*/:
                return "N ";
            case TRIM_EMPTY_PERCENT /*0*/:
                return "P ";
            case SCHED_GROUP_DEFAULT /*1*/:
                return "PU";
            case SCHED_GROUP_TOP_APP /*2*/:
                return "T ";
            case TRIM_CRITICAL_THRESHOLD /*3*/:
                return "SB";
            case PROC_MEM_CACHED /*4*/:
                return "SF";
            case TRIM_LOW_THRESHOLD /*5*/:
                return "TS";
            case H.REMOVE_STARTING /*6*/:
                return "IF";
            case H.FINISHED_STARTING /*7*/:
                return "IB";
            case H.REPORT_APPLICATION_TOKEN_WINDOWS /*8*/:
                return "BU";
            case H.REPORT_APPLICATION_TOKEN_DRAWN /*9*/:
                return "HW";
            case AppTransition.TRANSIT_TASK_TO_FRONT /*10*/:
                return "S ";
            case H.WINDOW_FREEZE_TIMEOUT /*11*/:
                return "R ";
            case AppTransition.TRANSIT_WALLPAPER_CLOSE /*12*/:
                return "HO";
            case H.APP_TRANSITION_TIMEOUT /*13*/:
                return "LA";
            case H.PERSIST_ANIMATION_SCALE /*14*/:
                return "CA";
            case H.FORCE_GC /*15*/:
                return "Ca";
            case H.ENABLE_SCREEN /*16*/:
                return "CE";
            default:
                return "??";
        }
    }

    public static void appendRamKb(StringBuilder sb, long ramKb) {
        int j = TRIM_EMPTY_PERCENT;
        int fact = 10;
        while (j < 6) {
            if (ramKb < ((long) fact)) {
                sb.append(' ');
            }
            j += SCHED_GROUP_DEFAULT;
            fact *= 10;
        }
        sb.append(ramKb);
    }

    public static boolean procStatesDifferForMem(int procState1, int procState2) {
        return sProcStateToProcMem[procState1] != sProcStateToProcMem[procState2] ? true : USE_TRIM_SETTINGS;
    }

    public static long minTimeFromStateChange(boolean test) {
        return (long) (test ? PSS_TEST_SAME_IMPORTANT_INTERVAL : PSS_TEST_SAME_BACKGROUND_INTERVAL);
    }

    public static long computeNextPssTime(int procState, boolean first, boolean test, boolean sleeping, long now) {
        long[] table;
        if (test) {
            if (first) {
                table = sTestFirstAwakePssTimes;
            } else {
                table = sTestSameAwakePssTimes;
            }
        } else if (first) {
            table = sFirstAwakePssTimes;
        } else {
            table = sSameAwakePssTimes;
        }
        return table[procState] + now;
    }

    public long getMemLevel(int adjustment) {
        for (int i = TRIM_EMPTY_PERCENT; i < this.mOomAdj.length; i += SCHED_GROUP_DEFAULT) {
            if (adjustment <= this.mOomAdj[i]) {
                return (long) (this.mOomMinFree[i] * DumpState.DUMP_PROVIDERS);
            }
        }
        return (long) (this.mOomMinFree[this.mOomAdj.length - 1] * DumpState.DUMP_PROVIDERS);
    }

    long getCachedRestoreThresholdKb() {
        return this.mCachedRestoreLevel;
    }

    public static final void setOomAdj(int pid, int uid, int amt) {
        if (amt != UNKNOWN_ADJ) {
            long start = SystemClock.elapsedRealtime();
            ByteBuffer buf = ByteBuffer.allocate(16);
            buf.putInt(SCHED_GROUP_DEFAULT);
            buf.putInt(pid);
            buf.putInt(uid);
            buf.putInt(amt);
            writeLmkd(buf);
            long now = SystemClock.elapsedRealtime();
            if (now - start > 250) {
                Slog.w("ActivityManager", "SLOW OOM ADJ: " + (now - start) + "ms for pid " + pid + " = " + amt);
            }
        }
    }

    public static final void setOomAdj(int pid, int uid, int amt, String name) {
        if (amt != UNKNOWN_ADJ) {
            int length = name != null ? name.length() + SCHED_GROUP_DEFAULT : TRIM_EMPTY_PERCENT;
            long start = SystemClock.elapsedRealtime();
            ByteBuffer buf = ByteBuffer.allocate(length + 16);
            buf.putInt(SCHED_GROUP_DEFAULT);
            buf.putInt(pid);
            buf.putInt(uid);
            buf.putInt(amt);
            if (name != null) {
                try {
                    buf.put((name + "\n").getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    Slog.w("AwareLog:CPU:ActivityManager", "unsupported encodeing: UTF-8");
                }
            }
            writeLmkd(buf);
            long duration = SystemClock.elapsedRealtime() - start;
            if (duration > 250) {
                Slog.w("AwareLog:CPU:ActivityManager", "SLOW OOM ADJ: " + duration + "ms for pid " + pid + " = " + amt);
            }
        }
    }

    public static final void remove(int pid) {
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putInt(SCHED_GROUP_TOP_APP);
        buf.putInt(pid);
        writeLmkd(buf);
    }

    private static boolean openLmkdSocket() {
        try {
            sLmkdSocket = new LocalSocket(TRIM_CRITICAL_THRESHOLD);
            sLmkdSocket.connect(new LocalSocketAddress("lmkd", Namespace.RESERVED));
            sLmkdOutputStream = sLmkdSocket.getOutputStream();
            return true;
        } catch (IOException e) {
            Slog.w(TAG, "lowmemorykiller daemon socket open failed");
            sLmkdSocket = null;
            return USE_TRIM_SETTINGS;
        }
    }

    private static void writeLmkd(ByteBuffer buf) {
        int i = TRIM_EMPTY_PERCENT;
        while (i < TRIM_CRITICAL_THRESHOLD) {
            if (sLmkdSocket != null || openLmkdSocket()) {
                try {
                    sLmkdOutputStream.write(buf.array(), TRIM_EMPTY_PERCENT, buf.position());
                    return;
                } catch (IOException e) {
                    Slog.w(TAG, "Error writing to lowmemorykiller socket");
                    try {
                        sLmkdSocket.close();
                    } catch (IOException e2) {
                    }
                    sLmkdSocket = null;
                }
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e3) {
                }
                i += SCHED_GROUP_DEFAULT;
            }
        }
    }

    public static final void callMemoryShrinker(int value) {
        long start = SystemClock.elapsedRealtime();
        ByteBuffer buf = ByteBuffer.allocate(16);
        buf.putInt(52);
        buf.putInt(value);
        writeLmkd(buf);
        long now = SystemClock.elapsedRealtime();
        if (now - start > 250) {
            Slog.w("ActivityManager", "SLOW call memory shrinker: " + (now - start));
        }
    }

    public static final void callMemoryCompact(int value) {
        long start = SystemClock.elapsedRealtime();
        ByteBuffer buf = ByteBuffer.allocate(16);
        buf.putInt(53);
        buf.putInt(value);
        writeLmkd(buf);
        long now = SystemClock.elapsedRealtime();
        if (now - start > 250) {
            Slog.w("ActivityManager", "SLOW call memory shrinker: " + (now - start));
        }
    }

    public static final void callProcReclaim(int pid, int mode) {
        long start = SystemClock.elapsedRealtime();
        ByteBuffer buf = ByteBuffer.allocate(16);
        buf.putInt(54);
        buf.putInt(pid);
        buf.putInt(mode);
        writeLmkd(buf);
        long now = SystemClock.elapsedRealtime();
        if (now - start > 250) {
            Slog.w("ActivityManager", "SLOW call process reclaim: " + (now - start));
        }
    }

    public static final void setLmkByIAware(int[] lmkMinfree, int[] lmkAdj) {
        if (lmkMinfree.length == 6 && lmkAdj.length == 6) {
            long start = SystemClock.elapsedRealtime();
            ByteBuffer lmkBuf = ByteBuffer.allocate(((lmkMinfree.length * SCHED_GROUP_TOP_APP) + SCHED_GROUP_DEFAULT) * PROC_MEM_CACHED);
            lmkBuf.putInt(TRIM_EMPTY_PERCENT);
            for (int i = TRIM_EMPTY_PERCENT; i < lmkAdj.length; i += SCHED_GROUP_DEFAULT) {
                lmkBuf.putInt(lmkMinfree[i]);
                lmkBuf.putInt(lmkAdj[i]);
            }
            writeLmkd(lmkBuf);
            long now = SystemClock.elapsedRealtime();
            if (now - start > 250) {
                Slog.w("AwareLog", "SLOW call setLmkByIAware: " + (now - start));
            }
            return;
        }
        Slog.w("AwareLog", "SetLmkByIAware failed!");
    }
}
