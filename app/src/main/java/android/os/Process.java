package android.os;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.net.wifi.WifiEnterpriseConfig;
import android.security.keymaster.KeymasterDefs;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import dalvik.system.VMRuntime;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Process {
    public static final int AUDIOSERVER_UID = 1041;
    public static final int BLUETOOTH_UID = 1002;
    public static final int CAMERASERVER_UID = 1047;
    public static final int DRM_UID = 1019;
    public static final int FIRST_APPLICATION_UID = 10000;
    public static final int FIRST_ISOLATED_UID = 99000;
    public static final int FIRST_SHARED_APPLICATION_GID = 50000;
    public static final int LAST_APPLICATION_UID = 19999;
    public static final int LAST_ISOLATED_UID = 99999;
    public static final int LAST_SHARED_APPLICATION_GID = 59999;
    private static final String LOG_TAG = "Process";
    public static final int LOG_UID = 1007;
    public static final int MEDIA_RW_GID = 1023;
    public static final int MEDIA_UID = 1013;
    public static final int NFC_UID = 1027;
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
    public static final int SMARTCARD_UID = 1050;
    public static final int SPI_UID = 1054;
    public static final int SYSTEM_UID = 1000;
    public static final int THREAD_GROUP_AUDIO_APP = 3;
    public static final int THREAD_GROUP_AUDIO_SYS = 4;
    public static final int THREAD_GROUP_BG_NONINTERACTIVE = 0;
    public static final int THREAD_GROUP_DEFAULT = -1;
    private static final int THREAD_GROUP_FOREGROUND = 1;
    public static final int THREAD_GROUP_PERCEPTIBLE = 6;
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
    public static final int WIFI_UID = 1010;
    static final int ZYGOTE_RETRY_MILLIS = 500;
    public static final String ZYGOTE_SOCKET = "zygote";
    static ZygoteState primaryZygoteState;
    private static long sStartElapsedRealtime;
    private static long sStartUptimeMillis;
    static ZygoteState secondaryZygoteState;

    public static final class ProcessStartResult {
        public int pid;
        public boolean usingWrapper;
    }

    public static class ZygoteState {
        final List<String> abiList;
        final DataInputStream inputStream;
        boolean mClosed;
        final LocalSocket socket;
        final BufferedWriter writer;

        private ZygoteState(LocalSocket socket, DataInputStream inputStream, BufferedWriter writer, List<String> abiList) {
            this.socket = socket;
            this.inputStream = inputStream;
            this.writer = writer;
            this.abiList = abiList;
        }

        public static ZygoteState connect(String socketAddress) throws IOException {
            IOException ex;
            LocalSocket zygoteSocket = new LocalSocket();
            try {
                zygoteSocket.connect(new LocalSocketAddress(socketAddress, Namespace.RESERVED));
                DataInputStream zygoteInputStream = new DataInputStream(zygoteSocket.getInputStream());
                try {
                    BufferedWriter zygoteWriter = new BufferedWriter(new OutputStreamWriter(zygoteSocket.getOutputStream()), Process.PROC_COMBINE);
                    String abiListString = Process.getAbiList(zygoteWriter, zygoteInputStream);
                    Log.i("Zygote", "Process: zygote socket opened, supported ABIS: " + abiListString);
                    return new ZygoteState(zygoteSocket, zygoteInputStream, zygoteWriter, Arrays.asList(abiListString.split(",")));
                } catch (IOException e) {
                    ex = e;
                    try {
                        zygoteSocket.close();
                    } catch (IOException e2) {
                    }
                    throw ex;
                }
            } catch (IOException e3) {
                ex = e3;
                zygoteSocket.close();
                throw ex;
            }
        }

        boolean matches(String abi) {
            return this.abiList.contains(abi);
        }

        public void close() {
            try {
                this.socket.close();
            } catch (IOException ex) {
                Log.e(Process.LOG_TAG, "I/O exception on routine close", ex);
            }
            this.mClosed = true;
        }

        boolean isClosed() {
            return this.mClosed;
        }
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

    public static final native void setThreadPriority(int i) throws IllegalArgumentException, SecurityException;

    public static final native void setThreadPriority(int i, int i2) throws IllegalArgumentException, SecurityException;

    public static final native void setThreadScheduler(int i, int i2, int i3) throws IllegalArgumentException;

    public static final native int setUid(int i);

    public static final ProcessStartResult start(String processClass, String niceName, int uid, int gid, int[] gids, int debugFlags, int mountExternal, int targetSdkVersion, String seInfo, String abi, String instructionSet, String appDataDir, String[] zygoteArgs) {
        try {
            return startViaZygote(processClass, niceName, uid, gid, gids, debugFlags, mountExternal, targetSdkVersion, seInfo, abi, instructionSet, appDataDir, zygoteArgs);
        } catch (ZygoteStartFailedEx ex) {
            Log.e(LOG_TAG, "Starting VM process through Zygote failed");
            throw new RuntimeException("Starting VM process through Zygote failed", ex);
        }
    }

    private static String getAbiList(BufferedWriter writer, DataInputStream inputStream) throws IOException {
        writer.write(WifiEnterpriseConfig.ENGINE_ENABLE);
        writer.newLine();
        writer.write("--query-abi-list");
        writer.newLine();
        writer.flush();
        byte[] bytes = new byte[inputStream.readInt()];
        inputStream.readFully(bytes);
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    private static ProcessStartResult zygoteSendArgsAndGetResult(ZygoteState zygoteState, ArrayList<String> args) throws ZygoteStartFailedEx {
        try {
            int i;
            int sz = args.size();
            for (i = THREAD_PRIORITY_DEFAULT; i < sz; i += THREAD_PRIORITY_LESS_FAVORABLE) {
                if (((String) args.get(i)).indexOf(THREAD_PRIORITY_BACKGROUND) >= 0) {
                    throw new ZygoteStartFailedEx("embedded newlines not allowed");
                }
            }
            BufferedWriter writer = zygoteState.writer;
            DataInputStream inputStream = zygoteState.inputStream;
            writer.write(Integer.toString(args.size()));
            writer.newLine();
            for (i = THREAD_PRIORITY_DEFAULT; i < sz; i += THREAD_PRIORITY_LESS_FAVORABLE) {
                writer.write((String) args.get(i));
                writer.newLine();
            }
            writer.flush();
            ProcessStartResult result = new ProcessStartResult();
            result.pid = inputStream.readInt();
            result.usingWrapper = inputStream.readBoolean();
            if (result.pid >= 0) {
                return result;
            }
            throw new ZygoteStartFailedEx("fork() failed");
        } catch (Throwable ex) {
            zygoteState.close();
            throw new ZygoteStartFailedEx(ex);
        }
    }

    private static ProcessStartResult startViaZygote(String processClass, String niceName, int uid, int gid, int[] gids, int debugFlags, int mountExternal, int targetSdkVersion, String seInfo, String abi, String instructionSet, String appDataDir, String[] extraArgs) throws ZygoteStartFailedEx {
        ProcessStartResult zygoteSendArgsAndGetResult;
        synchronized (Process.class) {
            ArrayList<String> argsForZygote = new ArrayList();
            argsForZygote.add("--runtime-args");
            argsForZygote.add("--setuid=" + uid);
            argsForZygote.add("--setgid=" + gid);
            if ((debugFlags & 16) != 0) {
                argsForZygote.add("--enable-jni-logging");
            }
            if ((debugFlags & 8) != 0) {
                argsForZygote.add("--enable-safemode");
            }
            if ((debugFlags & THREAD_PRIORITY_LESS_FAVORABLE) != 0) {
                argsForZygote.add("--enable-debugger");
            }
            if ((debugFlags & THREAD_GROUP_SYSTEM) != 0) {
                argsForZygote.add("--enable-checkjni");
            }
            if ((debugFlags & PROC_SPACE_TERM) != 0) {
                argsForZygote.add("--generate-debug-info");
            }
            if ((debugFlags & 64) != 0) {
                argsForZygote.add("--always-jit");
            }
            if ((debugFlags & KeymasterDefs.KM_ALGORITHM_HMAC) != 0) {
                argsForZygote.add("--native-debuggable");
            }
            if ((debugFlags & THREAD_GROUP_AUDIO_SYS) != 0) {
                argsForZygote.add("--enable-assert");
            }
            if (mountExternal == THREAD_PRIORITY_LESS_FAVORABLE) {
                argsForZygote.add("--mount-external-default");
            } else if (mountExternal == THREAD_GROUP_SYSTEM) {
                argsForZygote.add("--mount-external-read");
            } else if (mountExternal == THREAD_GROUP_AUDIO_APP) {
                argsForZygote.add("--mount-external-write");
            }
            argsForZygote.add("--target-sdk-version=" + targetSdkVersion);
            if (gids != null && gids.length > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("--setgroups=");
                int sz = gids.length;
                for (int i = THREAD_PRIORITY_DEFAULT; i < sz; i += THREAD_PRIORITY_LESS_FAVORABLE) {
                    if (i != 0) {
                        sb.append(',');
                    }
                    sb.append(gids[i]);
                }
                argsForZygote.add(sb.toString());
            }
            if (niceName != null) {
                argsForZygote.add("--nice-name=" + niceName);
            }
            if (seInfo != null) {
                argsForZygote.add("--seinfo=" + seInfo);
            }
            if (instructionSet != null) {
                argsForZygote.add("--instruction-set=" + instructionSet);
            }
            if (appDataDir != null) {
                argsForZygote.add("--app-data-dir=" + appDataDir);
            }
            argsForZygote.add(processClass);
            if (extraArgs != null) {
                int length = extraArgs.length;
                for (int i2 = THREAD_PRIORITY_DEFAULT; i2 < length; i2 += THREAD_PRIORITY_LESS_FAVORABLE) {
                    argsForZygote.add(extraArgs[i2]);
                }
            }
            zygoteSendArgsAndGetResult = zygoteSendArgsAndGetResult(openZygoteSocketIfNeeded(abi), argsForZygote);
        }
        return zygoteSendArgsAndGetResult;
    }

    public static void establishZygoteConnectionForAbi(String abi) {
        try {
            openZygoteSocketIfNeeded(abi);
        } catch (ZygoteStartFailedEx ex) {
            throw new RuntimeException("Unable to connect to zygote for abi: " + abi, ex);
        }
    }

    private static ZygoteState openZygoteSocketIfNeeded(String abi) throws ZygoteStartFailedEx {
        if (primaryZygoteState == null || primaryZygoteState.isClosed()) {
            try {
                primaryZygoteState = ZygoteState.connect(ZYGOTE_SOCKET);
            } catch (IOException ioe) {
                throw new ZygoteStartFailedEx("Error connecting to primary zygote", ioe);
            }
        }
        if (primaryZygoteState.matches(abi)) {
            return primaryZygoteState;
        }
        if (secondaryZygoteState == null || secondaryZygoteState.isClosed()) {
            try {
                secondaryZygoteState = ZygoteState.connect(SECONDARY_ZYGOTE_SOCKET);
            } catch (IOException ioe2) {
                throw new ZygoteStartFailedEx("Error connecting to secondary zygote", ioe2);
            }
        }
        if (secondaryZygoteState.matches(abi)) {
            return secondaryZygoteState;
        }
        throw new ZygoteStartFailedEx("Unsupported zygote ABI: " + abi);
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
        String[] procStatusLabels = new String[THREAD_PRIORITY_LESS_FAVORABLE];
        procStatusLabels[THREAD_PRIORITY_DEFAULT] = "Uid:";
        long[] procStatusValues = new long[THREAD_PRIORITY_LESS_FAVORABLE];
        procStatusValues[THREAD_PRIORITY_DEFAULT] = -1;
        readProcLines("/proc/" + pid + "/status", procStatusLabels, procStatusValues);
        return (int) procStatusValues[THREAD_PRIORITY_DEFAULT];
    }

    public static final String getCmdlineForPid(int pid) {
        String[] outStrings = new String[THREAD_PRIORITY_LESS_FAVORABLE];
        String str = "/proc/" + pid + "/cmdline";
        int[] iArr = new int[THREAD_PRIORITY_LESS_FAVORABLE];
        iArr[THREAD_PRIORITY_DEFAULT] = 4128;
        readProcFile(str, iArr, outStrings, null, null);
        return outStrings[THREAD_PRIORITY_DEFAULT];
    }

    public static final int getParentPid(int pid) {
        String[] procStatusLabels = new String[THREAD_PRIORITY_LESS_FAVORABLE];
        procStatusLabels[THREAD_PRIORITY_DEFAULT] = "PPid:";
        long[] procStatusValues = new long[THREAD_PRIORITY_LESS_FAVORABLE];
        procStatusValues[THREAD_PRIORITY_DEFAULT] = -1;
        readProcLines("/proc/" + pid + "/status", procStatusLabels, procStatusValues);
        return (int) procStatusValues[THREAD_PRIORITY_DEFAULT];
    }

    public static final int getThreadGroupLeader(int tid) {
        String[] procStatusLabels = new String[THREAD_PRIORITY_LESS_FAVORABLE];
        procStatusLabels[THREAD_PRIORITY_DEFAULT] = "Tgid:";
        long[] procStatusValues = new long[THREAD_PRIORITY_LESS_FAVORABLE];
        procStatusValues[THREAD_PRIORITY_DEFAULT] = -1;
        readProcLines("/proc/" + tid + "/status", procStatusLabels, procStatusValues);
        return (int) procStatusValues[THREAD_PRIORITY_DEFAULT];
    }

    @Deprecated
    public static final boolean supportsProcesses() {
        return true;
    }

    public static final void killProcess(int pid) {
        sendSignal(pid, SIGNAL_KILL);
    }

    public static final void killProcessQuiet(int pid) {
        sendSignalQuiet(pid, SIGNAL_KILL);
    }

    public static final boolean isThreadInProcess(int tid, int pid) {
        try {
            if (Os.access("/proc/" + tid + "/task/" + pid, OsConstants.F_OK)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static final boolean updateHwThemeZipsAndSomeIcons(int currentUserId) {
        boolean ret = true;
        ArrayList<String> argsForZygote = new ArrayList();
        argsForZygote.add("updateHwThemeZipsAndIcons");
        argsForZygote.add("setuid=" + currentUserId);
        synchronized (Process.class) {
            String abi = Build.SUPPORTED_64_BIT_ABIS[THREAD_PRIORITY_DEFAULT];
            if (abi != null) {
                try {
                    if (abi.equals("arm64-v8a")) {
                        zygoteSendArgsAndGetResult(openZygoteSocketIfNeeded(abi), argsForZygote);
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "updateHwThemeZipsAndSomeIcons  arm64 failed");
                    ret = false;
                }
            }
            ret = false;
        }
        return ret;
    }
}
