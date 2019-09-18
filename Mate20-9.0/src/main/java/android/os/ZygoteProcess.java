package android.os;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.wifi.WifiScanLog;
import android.os.Process;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ZygoteProcess {
    private static final String LOG_TAG = "ZygoteProcess";
    static final int ZYGOTE_RETRY_MILLIS = 500;
    private List<String> mApiBlacklistExemptions;
    private int mHiddenApiAccessLogSampleRate;
    private final Object mLock;
    private final LocalSocketAddress mSecondarySocket;
    private final LocalSocketAddress mSocket;
    private ZygoteState primaryZygoteState;
    private ZygoteState secondaryZygoteState;

    public static class ZygoteState {
        final List<String> abiList;
        final DataInputStream inputStream;
        boolean mClosed;
        final LocalSocket socket;
        final BufferedWriter writer;

        private ZygoteState(LocalSocket socket2, DataInputStream inputStream2, BufferedWriter writer2, List<String> abiList2) {
            this.socket = socket2;
            this.inputStream = inputStream2;
            this.writer = writer2;
            this.abiList = abiList2;
        }

        public static ZygoteState connect(LocalSocketAddress address) throws IOException {
            LocalSocket zygoteSocket = new LocalSocket();
            try {
                zygoteSocket.connect(address);
                DataInputStream zygoteInputStream = new DataInputStream(zygoteSocket.getInputStream());
                BufferedWriter zygoteWriter = new BufferedWriter(new OutputStreamWriter(zygoteSocket.getOutputStream()), 256);
                String abiListString = ZygoteProcess.getAbiList(zygoteWriter, zygoteInputStream);
                Log.i("Zygote", "Process: zygote socket " + address.getNamespace() + "/" + address.getName() + " opened, supported ABIS: " + abiListString);
                return new ZygoteState(zygoteSocket, zygoteInputStream, zygoteWriter, Arrays.asList(abiListString.split(",")));
            } catch (IOException ex) {
                Log.e("Zygote", "I/O exception when connect", ex);
                try {
                    zygoteSocket.close();
                } catch (IOException ignore) {
                    Log.e("Zygote", "I/O exception on routine close when connect fail", ignore);
                }
                throw ex;
            }
        }

        /* access modifiers changed from: package-private */
        public boolean matches(String abi) {
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

        /* access modifiers changed from: package-private */
        public boolean isClosed() {
            return this.mClosed;
        }
    }

    public ZygoteProcess(String primarySocket, String secondarySocket) {
        this(new LocalSocketAddress(primarySocket, LocalSocketAddress.Namespace.RESERVED), new LocalSocketAddress(secondarySocket, LocalSocketAddress.Namespace.RESERVED));
    }

    public ZygoteProcess(LocalSocketAddress primarySocket, LocalSocketAddress secondarySocket) {
        this.mLock = new Object();
        this.mApiBlacklistExemptions = Collections.emptyList();
        this.mSocket = primarySocket;
        this.mSecondarySocket = secondarySocket;
    }

    public LocalSocketAddress getPrimarySocketAddress() {
        return this.mSocket;
    }

    public final Process.ProcessStartResult start(String processClass, String niceName, int uid, int gid, int[] gids, int runtimeFlags, int mountExternal, int targetSdkVersion, String seInfo, String abi, String instructionSet, String appDataDir, String invokeWith, String[] zygoteArgs) {
        try {
            return startViaZygote(processClass, niceName, uid, gid, gids, runtimeFlags, mountExternal, targetSdkVersion, seInfo, abi, instructionSet, appDataDir, invokeWith, false, zygoteArgs);
        } catch (ZygoteStartFailedEx ex) {
            ZygoteStartFailedEx zygoteStartFailedEx = ex;
            Log.e(LOG_TAG, "Starting VM process through Zygote failed");
            throw new RuntimeException("Starting VM process through Zygote failed", ex);
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public static String getAbiList(BufferedWriter writer, DataInputStream inputStream) throws IOException {
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
    private static Process.ProcessStartResult zygoteSendArgsAndGetResult(ZygoteState zygoteState, ArrayList<String> args) throws ZygoteStartFailedEx {
        try {
            int sz = args.size();
            int i = 0;
            while (i < sz) {
                if (args.get(i).indexOf(10) < 0) {
                    i++;
                } else {
                    throw new ZygoteStartFailedEx("embedded newlines not allowed");
                }
            }
            BufferedWriter writer = zygoteState.writer;
            DataInputStream inputStream = zygoteState.inputStream;
            writer.write(Integer.toString(args.size()));
            writer.newLine();
            for (int i2 = 0; i2 < sz; i2++) {
                writer.write(args.get(i2));
                writer.newLine();
            }
            writer.flush();
            Process.ProcessStartResult result = new Process.ProcessStartResult();
            result.pid = inputStream.readInt();
            result.usingWrapper = inputStream.readBoolean();
            if (result.pid >= 0) {
                return result;
            }
            throw new ZygoteStartFailedEx("fork() failed");
        } catch (IOException ex) {
            zygoteState.close();
            throw new ZygoteStartFailedEx((Throwable) ex);
        }
    }

    private Process.ProcessStartResult startViaZygote(String processClass, String niceName, int uid, int gid, int[] gids, int runtimeFlags, int mountExternal, int targetSdkVersion, String seInfo, String abi, String instructionSet, String appDataDir, String invokeWith, boolean startChildZygote, String[] extraArgs) throws ZygoteStartFailedEx {
        Process.ProcessStartResult zygoteSendArgsAndGetResult;
        int sz;
        String str = niceName;
        int[] iArr = gids;
        int i = mountExternal;
        String str2 = seInfo;
        String str3 = instructionSet;
        String str4 = appDataDir;
        String str5 = invokeWith;
        String[] strArr = extraArgs;
        ArrayList arrayList = new ArrayList();
        arrayList.add("--runtime-args");
        arrayList.add("--setuid=" + uid);
        arrayList.add("--setgid=" + gid);
        arrayList.add("--runtime-flags=" + runtimeFlags);
        if (i == 1) {
            arrayList.add("--mount-external-default");
        } else if (i == 2) {
            arrayList.add("--mount-external-read");
        } else if (i == 3) {
            arrayList.add("--mount-external-write");
        }
        arrayList.add("--target-sdk-version=" + targetSdkVersion);
        if (iArr != null && iArr.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("--setgroups=");
            int sz2 = iArr.length;
            int i2 = 0;
            while (true) {
                int i3 = i2;
                if (i3 >= sz2) {
                    break;
                }
                if (i3 != 0) {
                    sz = sz2;
                    sb.append(',');
                } else {
                    sz = sz2;
                }
                sb.append(iArr[i3]);
                i2 = i3 + 1;
                sz2 = sz;
                int i4 = mountExternal;
            }
            arrayList.add(sb.toString());
        }
        if (str != null) {
            arrayList.add("--nice-name=" + str);
        }
        if (str2 != null) {
            arrayList.add("--seinfo=" + str2);
        }
        if (str3 != null) {
            arrayList.add("--instruction-set=" + str3);
        }
        if (str4 != null) {
            arrayList.add("--app-data-dir=" + str4);
        }
        if (str5 != null) {
            arrayList.add("--invoke-with");
            arrayList.add(str5);
        }
        if (startChildZygote) {
            arrayList.add("--start-child-zygote");
        }
        arrayList.add(processClass);
        if (strArr != null) {
            int i5 = 0;
            for (int length = strArr.length; i5 < length; length = length) {
                arrayList.add(strArr[i5]);
                i5++;
            }
        }
        synchronized (this.mLock) {
            zygoteSendArgsAndGetResult = zygoteSendArgsAndGetResult(openZygoteSocketIfNeeded(abi), arrayList);
        }
        return zygoteSendArgsAndGetResult;
    }

    public void close() {
        if (this.primaryZygoteState != null) {
            this.primaryZygoteState.close();
        }
        if (this.secondaryZygoteState != null) {
            this.secondaryZygoteState.close();
        }
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

    public boolean setApiBlacklistExemptions(List<String> exemptions) {
        boolean ok;
        synchronized (this.mLock) {
            this.mApiBlacklistExemptions = exemptions;
            ok = maybeSetApiBlacklistExemptions(this.primaryZygoteState, true);
            if (ok) {
                ok = maybeSetApiBlacklistExemptions(this.secondaryZygoteState, true);
            }
        }
        return ok;
    }

    public void setHiddenApiAccessLogSampleRate(int rate) {
        synchronized (this.mLock) {
            this.mHiddenApiAccessLogSampleRate = rate;
            maybeSetHiddenApiAccessLogSampleRate(this.primaryZygoteState);
            maybeSetHiddenApiAccessLogSampleRate(this.secondaryZygoteState);
        }
    }

    @GuardedBy("mLock")
    private boolean maybeSetApiBlacklistExemptions(ZygoteState state, boolean sendIfEmpty) {
        if (state == null || state.isClosed()) {
            Slog.e(LOG_TAG, "Can't set API blacklist exemptions: no zygote connection");
            return false;
        } else if (!sendIfEmpty && this.mApiBlacklistExemptions.isEmpty()) {
            return true;
        } else {
            try {
                state.writer.write(Integer.toString(this.mApiBlacklistExemptions.size() + 1));
                state.writer.newLine();
                state.writer.write("--set-api-blacklist-exemptions");
                state.writer.newLine();
                for (int i = 0; i < this.mApiBlacklistExemptions.size(); i++) {
                    state.writer.write(this.mApiBlacklistExemptions.get(i));
                    state.writer.newLine();
                }
                state.writer.flush();
                if (state.inputStream.readInt() != 0) {
                    Slog.e(LOG_TAG, "Failed to set API blacklist exemptions; status " + status);
                }
                return true;
            } catch (IOException ioe) {
                Slog.e(LOG_TAG, "Failed to set API blacklist exemptions", ioe);
                this.mApiBlacklistExemptions = Collections.emptyList();
                return false;
            }
        }
    }

    private void maybeSetHiddenApiAccessLogSampleRate(ZygoteState state) {
        if (state != null && !state.isClosed() && this.mHiddenApiAccessLogSampleRate != -1) {
            try {
                state.writer.write(Integer.toString(1));
                state.writer.newLine();
                BufferedWriter bufferedWriter = state.writer;
                bufferedWriter.write("--hidden-api-log-sampling-rate=" + Integer.toString(this.mHiddenApiAccessLogSampleRate));
                state.writer.newLine();
                state.writer.flush();
                int status = state.inputStream.readInt();
                if (status != 0) {
                    Slog.e(LOG_TAG, "Failed to set hidden API log sampling rate; status " + status);
                }
            } catch (IOException ioe) {
                Slog.e(LOG_TAG, "Failed to set hidden API log sampling rate", ioe);
            }
        }
    }

    @GuardedBy("mLock")
    private ZygoteState openZygoteSocketIfNeeded(String abi) throws ZygoteStartFailedEx {
        Preconditions.checkState(Thread.holdsLock(this.mLock), "ZygoteProcess lock not held");
        if (this.primaryZygoteState == null || this.primaryZygoteState.isClosed()) {
            try {
                this.primaryZygoteState = ZygoteState.connect(this.mSocket);
                maybeSetApiBlacklistExemptions(this.primaryZygoteState, false);
                maybeSetHiddenApiAccessLogSampleRate(this.primaryZygoteState);
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
                maybeSetApiBlacklistExemptions(this.secondaryZygoteState, false);
                maybeSetHiddenApiAccessLogSampleRate(this.secondaryZygoteState);
            } catch (IOException ioe2) {
                throw new ZygoteStartFailedEx("Error connecting to secondary zygote", ioe2);
            }
        }
        if (this.secondaryZygoteState.matches(abi)) {
            return this.secondaryZygoteState;
        }
        throw new ZygoteStartFailedEx("Unsupported zygote ABI: " + abi);
    }

    public boolean preloadPackageForAbi(String packagePath, String libsPath, String libFileName, String cacheKey, String abi) throws ZygoteStartFailedEx, IOException {
        boolean z;
        synchronized (this.mLock) {
            ZygoteState state = openZygoteSocketIfNeeded(abi);
            state.writer.write(WifiScanLog.EVENT_KEY5);
            state.writer.newLine();
            state.writer.write("--preload-package");
            state.writer.newLine();
            state.writer.write(packagePath);
            state.writer.newLine();
            state.writer.write(libsPath);
            state.writer.newLine();
            state.writer.write(libFileName);
            state.writer.newLine();
            state.writer.write(cacheKey);
            state.writer.newLine();
            state.writer.flush();
            z = state.inputStream.readInt() == 0;
        }
        return z;
    }

    public boolean preloadDefault(String abi) throws ZygoteStartFailedEx, IOException {
        boolean z;
        synchronized (this.mLock) {
            ZygoteState state = openZygoteSocketIfNeeded(abi);
            state.writer.write("1");
            state.writer.newLine();
            state.writer.write("--preload-default");
            state.writer.newLine();
            state.writer.flush();
            z = state.inputStream.readInt() == 0;
        }
        return z;
    }

    public static void waitForConnectionToZygote(String socketName) {
        waitForConnectionToZygote(new LocalSocketAddress(socketName, LocalSocketAddress.Namespace.RESERVED));
    }

    public static void waitForConnectionToZygote(LocalSocketAddress address) {
        int n = 20;
        while (n >= 0) {
            try {
                ZygoteState.connect(address).close();
                return;
            } catch (IOException ioe) {
                Log.w(LOG_TAG, "Got error connecting to zygote, retrying. msg= " + ioe.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                n--;
            }
        }
        Slog.wtf(LOG_TAG, "Failed to connect to Zygote through socket " + address.getName());
    }

    public ChildZygoteProcess startChildZygote(String processClass, String niceName, int uid, int gid, int[] gids, int runtimeFlags, String seInfo, String abi, String instructionSet) {
        StringBuilder sb = new StringBuilder();
        String str = processClass;
        sb.append(str);
        sb.append("/");
        sb.append(UUID.randomUUID().toString());
        LocalSocketAddress serverAddress = new LocalSocketAddress(sb.toString());
        try {
            return new ChildZygoteProcess(serverAddress, startViaZygote(str, niceName, uid, gid, gids, runtimeFlags, 0, 0, seInfo, abi, instructionSet, null, null, true, new String[]{"--zygote-socket=" + serverAddress.getName()}).pid);
        } catch (ZygoteStartFailedEx ex) {
            ZygoteStartFailedEx zygoteStartFailedEx = ex;
            throw new RuntimeException("Starting child-zygote through Zygote failed", ex);
        }
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
        ArrayList<String> argsForZygote = new ArrayList<>();
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
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }
}
