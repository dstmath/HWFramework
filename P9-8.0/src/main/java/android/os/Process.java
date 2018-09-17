package android.os;

import android.os.StrictMode.ThreadPolicy;
import android.system.Os;
import android.system.OsConstants;
import android.webkit.WebViewZygote;
import dalvik.system.VMRuntime;

public class Process {
    public static final int AUDIOSERVER_UID = 1041;
    public static final int BLUETOOTH_UID = 1002;
    public static final int CAMERASERVER_UID = 1047;
    public static final int DRM_UID = 1019;
    public static final int FIRST_APPLICATION_CACHE_GID = 20000;
    public static final int FIRST_APPLICATION_UID = 10000;
    public static final int FIRST_ISOLATED_UID = 99000;
    public static final int FIRST_SHARED_APPLICATION_GID = 50000;
    public static final int KEYSTORE_UID = 1017;
    public static final int LAST_APPLICATION_CACHE_GID = 29999;
    public static final int LAST_APPLICATION_UID = 19999;
    public static final int LAST_ISOLATED_UID = 99999;
    public static final int LAST_SHARED_APPLICATION_GID = 59999;
    private static final String LOG_TAG = "Process";
    public static final int LOG_UID = 1007;
    public static final int MEDIA_RW_GID = 1023;
    public static final int MEDIA_UID = 1013;
    public static final int NFC_UID = 1027;
    public static final int OTA_UPDATE_UID = 1061;
    public static final int PACKAGE_INFO_GID = 1032;
    public static final int PHONE_UID = 1001;
    public static final int PROC_CHAR = 2048;
    public static final int PROC_COMBINE = 256;
    public static final int PROC_OUT_FLOAT = 16384;
    public static final int PROC_OUT_LONG = 8192;
    public static final int PROC_OUT_STRING = 4096;
    public static final int PROC_PARENS = 512;
    public static final int PROC_QUOTES = 1024;
    public static final int PROC_SPACE_TERM = 32;
    public static final int PROC_TAB_TERM = 9;
    public static final int PROC_TERM_MASK = 255;
    public static final int PROC_ZERO_TERM = 0;
    public static final int ROOT_UID = 0;
    public static final int SCHED_BATCH = 3;
    public static final int SCHED_FIFO = 1;
    public static final int SCHED_IDLE = 5;
    public static final int SCHED_OTHER = 0;
    public static final int SCHED_RESET_ON_FORK = 1073741824;
    public static final int SCHED_RR = 2;
    public static final String SECONDARY_ZYGOTE_SOCKET = "zygote_secondary";
    public static final int SHARED_RELRO_UID = 1037;
    public static final int SHARED_USER_GID = 9997;
    public static final int SHELL_UID = 2000;
    public static final int SIGNAL_KILL = 9;
    public static final int SIGNAL_QUIT = 3;
    public static final int SIGNAL_USR1 = 10;
    public static final int SMARTCARD_UID = 1091;
    public static final int SPI_UID = 1054;
    public static final int SYSTEM_UID = 1000;
    public static final int THREAD_GROUP_AUDIO_APP = 3;
    public static final int THREAD_GROUP_AUDIO_SYS = 4;
    public static final int THREAD_GROUP_BG_NONINTERACTIVE = 0;
    public static final int THREAD_GROUP_DEFAULT = -1;
    private static final int THREAD_GROUP_FOREGROUND = 1;
    public static final int THREAD_GROUP_KEY_BACKGROUND = 6;
    public static final int THREAD_GROUP_PERCEPTIBLE = 10;
    public static final int THREAD_GROUP_SYSTEM = 2;
    public static final int THREAD_GROUP_TOP_APP = 5;
    public static final int THREAD_PRIORITY_AUDIO = -16;
    public static final int THREAD_PRIORITY_BACKGROUND = 10;
    public static final int THREAD_PRIORITY_DEFAULT = 0;
    public static final int THREAD_PRIORITY_DISPLAY = -4;
    public static final int THREAD_PRIORITY_FOREGROUND = -2;
    public static final int THREAD_PRIORITY_LESS_FAVORABLE = 1;
    public static final int THREAD_PRIORITY_LOWEST = 19;
    public static final int THREAD_PRIORITY_MORE_FAVORABLE = -1;
    public static final int THREAD_PRIORITY_URGENT_AUDIO = -19;
    public static final int THREAD_PRIORITY_URGENT_DISPLAY = -8;
    public static final int VPN_UID = 1016;
    public static final int WEBVIEW_ZYGOTE_UID = 1051;
    public static final int WIFI_UID = 1010;
    public static final String ZYGOTE_SOCKET = "zygote";
    private static long sStartElapsedRealtime;
    private static long sStartUptimeMillis;
    public static final ZygoteProcess zygoteProcess = new ZygoteProcess(ZYGOTE_SOCKET, SECONDARY_ZYGOTE_SOCKET);

    public static final class ProcessStartResult {
        public int pid;
        public boolean usingWrapper;
    }

    public static final native long getElapsedCpuTime();

    public static final native int[] getExclusiveCores();

    public static final native long getFreeMemory();

    public static final native int getGidForName(String str);

    public static final native int[] getPids(String str, int[] iArr);

    public static final native int[] getPidsForCommands(String[] strArr);

    public static final native int getProcessGroup(int i) throws IllegalArgumentException, SecurityException;

    public static final native long getPss(int i);

    public static final native int getThreadPriority(int i) throws IllegalArgumentException;

    public static final native int getThreadScheduler(int i) throws IllegalArgumentException;

    public static final native long getTotalMemory();

    public static final native int getUidForName(String str);

    public static final native int killProcessGroup(int i, int i2);

    public static final native boolean parseProcLine(byte[] bArr, int i, int i2, int[] iArr, String[] strArr, long[] jArr, float[] fArr);

    public static final native boolean readProcFile(String str, int[] iArr, String[] strArr, long[] jArr, float[] fArr);

    public static final native void readProcLines(String str, String[] strArr, long[] jArr);

    public static final native void removeAllProcessGroups();

    public static final native void sendSignal(int i, int i2);

    public static final native void sendSignalQuiet(int i, int i2);

    public static final native void setArgV0(String str);

    public static final native void setCanSelfBackground(boolean z);

    public static final native int setGid(int i);

    public static final native int setProcessAffinity(int i, int i2);

    public static final native void setProcessGroup(int i, int i2) throws IllegalArgumentException, SecurityException;

    public static final native boolean setSwappiness(int i, boolean z);

    public static final native void setThreadBoostInfo(String[] strArr);

    public static final native void setThreadGroup(int i, int i2) throws IllegalArgumentException, SecurityException;

    public static final native void setThreadGroupAndCpuset(int i, int i2) throws IllegalArgumentException, SecurityException;

    public static final native void setThreadPriority(int i) throws IllegalArgumentException, SecurityException;

    public static final native void setThreadPriority(int i, int i2) throws IllegalArgumentException, SecurityException;

    public static final native void setThreadScheduler(int i, int i2, int i3) throws IllegalArgumentException;

    public static final native int setUid(int i);

    public static final ProcessStartResult start(String processClass, String niceName, int uid, int gid, int[] gids, int debugFlags, int mountExternal, int targetSdkVersion, String seInfo, String abi, String instructionSet, String appDataDir, String invokeWith, String[] zygoteArgs) {
        return zygoteProcess.start(processClass, niceName, uid, gid, gids, debugFlags, mountExternal, targetSdkVersion, seInfo, abi, instructionSet, appDataDir, invokeWith, zygoteArgs);
    }

    public static final ProcessStartResult startWebView(String processClass, String niceName, int uid, int gid, int[] gids, int debugFlags, int mountExternal, int targetSdkVersion, String seInfo, String abi, String instructionSet, String appDataDir, String invokeWith, String[] zygoteArgs) {
        return WebViewZygote.getProcess().start(processClass, niceName, uid, gid, gids, debugFlags, mountExternal, targetSdkVersion, seInfo, abi, instructionSet, appDataDir, invokeWith, zygoteArgs);
    }

    public static final long getStartElapsedRealtime() {
        return sStartElapsedRealtime;
    }

    public static final long getStartUptimeMillis() {
        return sStartUptimeMillis;
    }

    public static final void setStartTimes(long elapsedRealtime, long uptimeMillis) {
        sStartElapsedRealtime = elapsedRealtime;
        sStartUptimeMillis = uptimeMillis;
    }

    public static final boolean is64Bit() {
        return VMRuntime.getRuntime().is64Bit();
    }

    public static final int myPid() {
        return Os.getpid();
    }

    public static final int myPpid() {
        return Os.getppid();
    }

    public static final int myTid() {
        return Os.gettid();
    }

    public static final int myUid() {
        return Os.getuid();
    }

    public static UserHandle myUserHandle() {
        return UserHandle.of(UserHandle.getUserId(myUid()));
    }

    public static boolean isApplicationUid(int uid) {
        return UserHandle.isApp(uid);
    }

    public static final boolean isIsolated() {
        return isIsolated(myUid());
    }

    public static final boolean isIsolated(int uid) {
        uid = UserHandle.getAppId(uid);
        if (uid < FIRST_ISOLATED_UID || uid > LAST_ISOLATED_UID) {
            return false;
        }
        return true;
    }

    public static final int getUidForPid(int pid) {
        long[] procStatusValues = new long[]{-1};
        readProcLines("/proc/" + pid + "/status", new String[]{"Uid:"}, procStatusValues);
        return (int) procStatusValues[0];
    }

    public static final String getCmdlineForPid(int pid) {
        String[] outStrings = new String[1];
        readProcFile("/proc/" + pid + "/cmdline", new int[]{4128}, outStrings, null, null);
        return outStrings[0];
    }

    public static final int getParentPid(int pid) {
        long[] procStatusValues = new long[]{-1};
        readProcLines("/proc/" + pid + "/status", new String[]{"PPid:"}, procStatusValues);
        return (int) procStatusValues[0];
    }

    public static final int getThreadGroupLeader(int tid) {
        long[] procStatusValues = new long[]{-1};
        readProcLines("/proc/" + tid + "/status", new String[]{"Tgid:"}, procStatusValues);
        return (int) procStatusValues[0];
    }

    @Deprecated
    public static final boolean supportsProcesses() {
        return true;
    }

    public static final void killProcess(int pid) {
        sendSignal(pid, 9);
    }

    public static final void killProcessQuiet(int pid) {
        sendSignalQuiet(pid, 9);
    }

    public static final boolean isThreadInProcess(int tid, int pid) {
        ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
        try {
            if (Os.access("/proc/" + tid + "/task/" + pid, OsConstants.F_OK)) {
                boolean z = true;
                return z;
            }
            StrictMode.setThreadPolicy(oldPolicy);
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    public static final boolean updateHwThemeZipsAndSomeIcons(int currentUserId) {
        return zygoteProcess.updateHwThemeZipsAndSomeIcons(currentUserId);
    }
}
