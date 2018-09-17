package android.os;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.net.wifi.WifiScanLog;
import android.os.Process.ProcessStartResult;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZygoteProcess {
    private static final String LOG_TAG = "ZygoteProcess";
    static final int ZYGOTE_RETRY_MILLIS = 500;
    private final Object mLock = new Object();
    private final String mSecondarySocket;
    private final String mSocket;
    private ZygoteState primaryZygoteState;
    private ZygoteState secondaryZygoteState;

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
                    BufferedWriter zygoteWriter = new BufferedWriter(new OutputStreamWriter(zygoteSocket.getOutputStream()), 256);
                    String abiListString = ZygoteProcess.getAbiList(zygoteWriter, zygoteInputStream);
                    Log.i("Zygote", "Process: zygote socket " + socketAddress + " opened, supported ABIS: " + abiListString);
                    return new ZygoteState(zygoteSocket, zygoteInputStream, zygoteWriter, Arrays.asList(abiListString.split(",")));
                } catch (IOException e) {
                    ex = e;
                    Log.e("Zygote", "I/O exception when connect", ex);
                    try {
                        zygoteSocket.close();
                    } catch (IOException ignore) {
                        Log.e("Zygote", "I/O exception on routine close when connect fail", ignore);
                    }
                    throw ex;
                }
            } catch (IOException e2) {
                ex = e2;
                Log.e("Zygote", "I/O exception when connect", ex);
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
                Log.e(ZygoteProcess.LOG_TAG, "I/O exception on routine close", ex);
            }
            this.mClosed = true;
        }

        boolean isClosed() {
            return this.mClosed;
        }
    }

    public ZygoteProcess(String primarySocket, String secondarySocket) {
        this.mSocket = primarySocket;
        this.mSecondarySocket = secondarySocket;
    }

    public final ProcessStartResult start(String processClass, String niceName, int uid, int gid, int[] gids, int debugFlags, int mountExternal, int targetSdkVersion, String seInfo, String abi, String instructionSet, String appDataDir, String invokeWith, String[] zygoteArgs) {
        try {
            return startViaZygote(processClass, niceName, uid, gid, gids, debugFlags, mountExternal, targetSdkVersion, seInfo, abi, instructionSet, appDataDir, invokeWith, zygoteArgs);
        } catch (ZygoteStartFailedEx ex) {
            Log.e(LOG_TAG, "Starting VM process through Zygote failed");
            throw new RuntimeException("Starting VM process through Zygote failed", ex);
        }
    }

    @GuardedBy("mLock")
    private static String getAbiList(BufferedWriter writer, DataInputStream inputStream) throws IOException {
        writer.write("1");
        writer.newLine();
        writer.write("--query-abi-list");
        writer.newLine();
        writer.flush();
        byte[] bytes = new byte[inputStream.readInt()];
        inputStream.readFully(bytes);
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    @GuardedBy("mLock")
    private static ProcessStartResult zygoteSendArgsAndGetResult(ZygoteState zygoteState, ArrayList<String> args) throws ZygoteStartFailedEx {
        try {
            int i;
            int sz = args.size();
            for (i = 0; i < sz; i++) {
                if (((String) args.get(i)).indexOf(10) >= 0) {
                    throw new ZygoteStartFailedEx("embedded newlines not allowed");
                }
            }
            BufferedWriter writer = zygoteState.writer;
            DataInputStream inputStream = zygoteState.inputStream;
            writer.write(Integer.toString(args.size()));
            writer.newLine();
            for (i = 0; i < sz; i++) {
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

    private ProcessStartResult startViaZygote(String processClass, String niceName, int uid, int gid, int[] gids, int debugFlags, int mountExternal, int targetSdkVersion, String seInfo, String abi, String instructionSet, String appDataDir, String invokeWith, String[] extraArgs) throws ZygoteStartFailedEx {
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
        if ((debugFlags & 1) != 0) {
            argsForZygote.add("--enable-jdwp");
        }
        if ((debugFlags & 2) != 0) {
            argsForZygote.add("--enable-checkjni");
        }
        if ((debugFlags & 32) != 0) {
            argsForZygote.add("--generate-debug-info");
        }
        if ((debugFlags & 64) != 0) {
            argsForZygote.add("--always-jit");
        }
        if ((debugFlags & 128) != 0) {
            argsForZygote.add("--native-debuggable");
        }
        if ((debugFlags & 256) != 0) {
            argsForZygote.add("--java-debuggable");
        }
        if ((debugFlags & 4) != 0) {
            argsForZygote.add("--enable-assert");
        }
        if (mountExternal == 1) {
            argsForZygote.add("--mount-external-default");
        } else if (mountExternal == 2) {
            argsForZygote.add("--mount-external-read");
        } else if (mountExternal == 3) {
            argsForZygote.add("--mount-external-write");
        }
        argsForZygote.add("--target-sdk-version=" + targetSdkVersion);
        if (gids != null && gids.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("--setgroups=");
            int sz = gids.length;
            for (int i = 0; i < sz; i++) {
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
        if (invokeWith != null) {
            argsForZygote.add("--invoke-with");
            argsForZygote.add(invokeWith);
        }
        argsForZygote.add(processClass);
        if (extraArgs != null) {
            for (String arg : extraArgs) {
                argsForZygote.add(arg);
            }
        }
        synchronized (this.mLock) {
        }
        return zygoteSendArgsAndGetResult(openZygoteSocketIfNeeded(abi), argsForZygote);
    }

    public void establishZygoteConnectionForAbi(String abi) {
        try {
            synchronized (this.mLock) {
                openZygoteSocketIfNeeded(abi);
            }
        } catch (ZygoteStartFailedEx ex) {
            throw new RuntimeException("Unable to connect to zygote for abi: " + abi, ex);
        }
    }

    @GuardedBy("mLock")
    private ZygoteState openZygoteSocketIfNeeded(String abi) throws ZygoteStartFailedEx {
        Preconditions.checkState(Thread.holdsLock(this.mLock), "ZygoteProcess lock not held");
        if (this.primaryZygoteState == null || this.primaryZygoteState.isClosed()) {
            try {
                this.primaryZygoteState = ZygoteState.connect(this.mSocket);
            } catch (IOException ioe) {
                throw new ZygoteStartFailedEx("Error connecting to primary zygote", ioe);
            }
        }
        if (this.primaryZygoteState.matches(abi)) {
            return this.primaryZygoteState;
        }
        if (this.secondaryZygoteState == null || this.secondaryZygoteState.isClosed()) {
            try {
                this.secondaryZygoteState = ZygoteState.connect(this.mSecondarySocket);
            } catch (IOException ioe2) {
                throw new ZygoteStartFailedEx("Error connecting to secondary zygote", ioe2);
            }
        }
        if (this.secondaryZygoteState.matches(abi)) {
            return this.secondaryZygoteState;
        }
        throw new ZygoteStartFailedEx("Unsupported zygote ABI: " + abi);
    }

    public void preloadPackageForAbi(String packagePath, String libsPath, String cacheKey, String abi) throws ZygoteStartFailedEx, IOException {
        synchronized (this.mLock) {
            ZygoteState state = openZygoteSocketIfNeeded(abi);
            state.writer.write(WifiScanLog.EVENT_KEY4);
            state.writer.newLine();
            state.writer.write("--preload-package");
            state.writer.newLine();
            state.writer.write(packagePath);
            state.writer.newLine();
            state.writer.write(libsPath);
            state.writer.newLine();
            state.writer.write(cacheKey);
            state.writer.newLine();
            state.writer.flush();
            state.inputStream.readInt();
        }
    }

    public boolean preloadDefault(String abi) throws ZygoteStartFailedEx, IOException {
        boolean z = false;
        synchronized (this.mLock) {
            ZygoteState state = openZygoteSocketIfNeeded(abi);
            state.writer.write("1");
            state.writer.newLine();
            state.writer.write("--preload-default");
            state.writer.newLine();
            state.writer.flush();
            if (state.inputStream.readInt() == 0) {
                z = true;
            }
        }
        return z;
    }

    public final boolean updateHwThemeZipsAndSomeIcons(int currentUserId) {
        boolean ret = true;
        String abi64 = null;
        String abi32 = null;
        if (Build.SUPPORTED_64_BIT_ABIS.length > 0) {
            abi64 = Build.SUPPORTED_64_BIT_ABIS[0];
        }
        if (Build.SUPPORTED_32_BIT_ABIS.length > 0) {
            abi32 = Build.SUPPORTED_32_BIT_ABIS[0];
        }
        ArrayList<String> argsForZygote = new ArrayList();
        argsForZygote.add("updateHwThemeZipsAndIcons");
        argsForZygote.add("setuid=" + currentUserId);
        if (abi64 != null && abi64.equals("arm64-v8a")) {
            ret = zygoteSendArgsForUpdateHwThemes(abi64, argsForZygote);
        }
        Log.i(LOG_TAG, "updateHwThemeZipsAndSomeIcons abi=, " + abi32);
        if (abi32 == null || !abi32.equals("armeabi-v7a")) {
            return ret;
        }
        return zygoteSendArgsForUpdateHwThemes(abi32, argsForZygote);
    }

    private final boolean zygoteSendArgsForUpdateHwThemes(String abi, ArrayList<String> argsForZygote) {
        synchronized (this.mLock) {
            if (abi != null) {
                try {
                    zygoteSendArgsAndGetResult(openZygoteSocketIfNeeded(abi), argsForZygote);
                    return true;
                } catch (ZygoteStartFailedEx e) {
                    Log.e(LOG_TAG, "zygoteSendArgsForUpdateHwThemes  abi " + abi + " fail");
                    return false;
                }
            }
        }
    }
}
