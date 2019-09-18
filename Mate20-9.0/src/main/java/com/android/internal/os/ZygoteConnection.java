package com.android.internal.os;

import android.net.Credentials;
import android.net.LocalSocket;
import android.os.FactoryTest;
import android.os.Process;
import android.os.SystemProperties;
import android.os.Trace;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructPollfd;
import android.util.Log;
import dalvik.system.VMRuntime;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import libcore.io.IoUtils;

class ZygoteConnection {
    private static final String TAG = "Zygote";
    private static final int[][] intArray2d = ((int[][]) Array.newInstance(int.class, new int[]{0, 0}));
    private final String abiList;
    private boolean isEof;
    private final LocalSocket mSocket;
    private final DataOutputStream mSocketOutStream;
    private final BufferedReader mSocketReader;
    private final Credentials peer;

    static class Arguments {
        boolean abiListQuery;
        String[] apiBlacklistExemptions;
        String appDataDir;
        boolean capabilitiesSpecified;
        long effectiveCapabilities;
        int gid = 0;
        boolean gidSpecified;
        int[] gids;
        int hiddenApiAccessLogSampleRate = -1;
        String instructionSet;
        String invokeWith;
        int mountExternal = 0;
        String niceName;
        long permittedCapabilities;
        boolean preloadDefault;
        String preloadPackage;
        String preloadPackageCacheKey;
        String preloadPackageLibFileName;
        String preloadPackageLibs;
        String[] remainingArgs;
        ArrayList<int[]> rlimits;
        int runtimeFlags;
        String seInfo;
        boolean seInfoSpecified;
        boolean startChildZygote;
        int targetSdkVersion;
        boolean targetSdkVersionSpecified;
        int uid = 0;
        boolean uidSpecified;

        Arguments(String[] args) throws IllegalArgumentException {
            parseArgs(args);
        }

        private void parseArgs(String[] args) throws IllegalArgumentException {
            int i;
            boolean seenRuntimeArgs = false;
            int curArg = 0;
            boolean expectRuntimeArgs = true;
            while (true) {
                i = 0;
                if (curArg >= args.length) {
                    break;
                }
                String arg = args[curArg];
                if (arg.equals("--")) {
                    curArg++;
                    break;
                }
                if (!arg.startsWith("--setuid=")) {
                    if (!arg.startsWith("--setgid=")) {
                        if (!arg.startsWith("--target-sdk-version=")) {
                            if (!arg.equals("--runtime-args")) {
                                if (!arg.startsWith("--runtime-flags=")) {
                                    if (!arg.startsWith("--seinfo=")) {
                                        if (!arg.startsWith("--capabilities=")) {
                                            if (!arg.startsWith("--rlimit=")) {
                                                if (!arg.startsWith("--setgroups=")) {
                                                    if (!arg.equals("--invoke-with")) {
                                                        if (!arg.startsWith("--nice-name=")) {
                                                            if (!arg.equals("--mount-external-default")) {
                                                                if (!arg.equals("--mount-external-read")) {
                                                                    if (!arg.equals("--mount-external-write")) {
                                                                        if (!arg.equals("--query-abi-list")) {
                                                                            if (!arg.startsWith("--instruction-set=")) {
                                                                                if (!arg.startsWith("--app-data-dir=")) {
                                                                                    if (!arg.equals("--preload-package")) {
                                                                                        if (!arg.equals("--preload-default")) {
                                                                                            if (!arg.equals("--start-child-zygote")) {
                                                                                                if (!arg.equals("--set-api-blacklist-exemptions")) {
                                                                                                    if (!arg.startsWith("--hidden-api-log-sampling-rate=")) {
                                                                                                        break;
                                                                                                    }
                                                                                                    try {
                                                                                                        this.hiddenApiAccessLogSampleRate = Integer.parseInt(arg.substring(arg.indexOf(61) + 1));
                                                                                                        expectRuntimeArgs = false;
                                                                                                    } catch (NumberFormatException nfe) {
                                                                                                        throw new IllegalArgumentException("Invalid log sampling rate: " + rateStr, nfe);
                                                                                                    }
                                                                                                } else {
                                                                                                    this.apiBlacklistExemptions = (String[]) Arrays.copyOfRange(args, curArg + 1, args.length);
                                                                                                    curArg = args.length;
                                                                                                    expectRuntimeArgs = false;
                                                                                                }
                                                                                            } else {
                                                                                                this.startChildZygote = true;
                                                                                            }
                                                                                        } else {
                                                                                            this.preloadDefault = true;
                                                                                            expectRuntimeArgs = false;
                                                                                        }
                                                                                    } else {
                                                                                        int curArg2 = curArg + 1;
                                                                                        this.preloadPackage = args[curArg2];
                                                                                        int curArg3 = curArg2 + 1;
                                                                                        this.preloadPackageLibs = args[curArg3];
                                                                                        int curArg4 = curArg3 + 1;
                                                                                        this.preloadPackageLibFileName = args[curArg4];
                                                                                        curArg = curArg4 + 1;
                                                                                        this.preloadPackageCacheKey = args[curArg];
                                                                                    }
                                                                                } else {
                                                                                    this.appDataDir = arg.substring(arg.indexOf(61) + 1);
                                                                                }
                                                                            } else {
                                                                                this.instructionSet = arg.substring(arg.indexOf(61) + 1);
                                                                            }
                                                                        } else {
                                                                            this.abiListQuery = true;
                                                                        }
                                                                    } else {
                                                                        this.mountExternal = 3;
                                                                    }
                                                                } else {
                                                                    this.mountExternal = 2;
                                                                }
                                                            } else {
                                                                this.mountExternal = 1;
                                                            }
                                                        } else if (this.niceName == null) {
                                                            this.niceName = arg.substring(arg.indexOf(61) + 1);
                                                        } else {
                                                            throw new IllegalArgumentException("Duplicate arg specified");
                                                        }
                                                    } else if (this.invokeWith == null) {
                                                        curArg++;
                                                        try {
                                                            this.invokeWith = args[curArg];
                                                        } catch (IndexOutOfBoundsException e) {
                                                            throw new IllegalArgumentException("--invoke-with requires argument");
                                                        }
                                                    } else {
                                                        throw new IllegalArgumentException("Duplicate arg specified");
                                                    }
                                                } else if (this.gids == null) {
                                                    String[] params = arg.substring(arg.indexOf(61) + 1).split(",");
                                                    this.gids = new int[params.length];
                                                    for (int i2 = params.length - 1; i2 >= 0; i2--) {
                                                        this.gids[i2] = Integer.parseInt(params[i2]);
                                                    }
                                                } else {
                                                    throw new IllegalArgumentException("Duplicate arg specified");
                                                }
                                            } else {
                                                String[] limitStrings = arg.substring(arg.indexOf(61) + 1).split(",");
                                                if (limitStrings.length == 3) {
                                                    int[] rlimitTuple = new int[limitStrings.length];
                                                    while (i < limitStrings.length) {
                                                        rlimitTuple[i] = Integer.parseInt(limitStrings[i]);
                                                        i++;
                                                    }
                                                    if (this.rlimits == null) {
                                                        this.rlimits = new ArrayList<>();
                                                    }
                                                    this.rlimits.add(rlimitTuple);
                                                } else {
                                                    throw new IllegalArgumentException("--rlimit= should have 3 comma-delimited ints");
                                                }
                                            }
                                        } else if (!this.capabilitiesSpecified) {
                                            this.capabilitiesSpecified = true;
                                            String[] capStrings = arg.substring(arg.indexOf(61) + 1).split(",", 2);
                                            if (capStrings.length == 1) {
                                                this.effectiveCapabilities = Long.decode(capStrings[0]).longValue();
                                                this.permittedCapabilities = this.effectiveCapabilities;
                                            } else {
                                                this.permittedCapabilities = Long.decode(capStrings[0]).longValue();
                                                this.effectiveCapabilities = Long.decode(capStrings[1]).longValue();
                                            }
                                        } else {
                                            throw new IllegalArgumentException("Duplicate arg specified");
                                        }
                                    } else if (!this.seInfoSpecified) {
                                        this.seInfoSpecified = true;
                                        this.seInfo = arg.substring(arg.indexOf(61) + 1);
                                    } else {
                                        throw new IllegalArgumentException("Duplicate arg specified");
                                    }
                                } else {
                                    this.runtimeFlags = Integer.parseInt(arg.substring(arg.indexOf(61) + 1));
                                }
                            } else {
                                seenRuntimeArgs = true;
                            }
                        } else if (!this.targetSdkVersionSpecified) {
                            this.targetSdkVersionSpecified = true;
                            this.targetSdkVersion = Integer.parseInt(arg.substring(arg.indexOf(61) + 1));
                        } else {
                            throw new IllegalArgumentException("Duplicate target-sdk-version specified");
                        }
                    } else if (!this.gidSpecified) {
                        this.gidSpecified = true;
                        this.gid = Integer.parseInt(arg.substring(arg.indexOf(61) + 1));
                    } else {
                        throw new IllegalArgumentException("Duplicate arg specified");
                    }
                } else if (!this.uidSpecified) {
                    this.uidSpecified = true;
                    this.uid = Integer.parseInt(arg.substring(arg.indexOf(61) + 1));
                } else {
                    throw new IllegalArgumentException("Duplicate arg specified");
                }
                curArg++;
            }
            if (this.abiListQuery) {
                if (args.length - curArg > 0) {
                    throw new IllegalArgumentException("Unexpected arguments after --query-abi-list.");
                }
            } else if (this.preloadPackage != null) {
                if (args.length - curArg > 0) {
                    throw new IllegalArgumentException("Unexpected arguments after --preload-package.");
                }
            } else if (expectRuntimeArgs) {
                if (!seenRuntimeArgs) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Unexpected argument : ");
                    sb.append(args.length > curArg ? args[curArg] : "args.length <= curArg");
                    throw new IllegalArgumentException(sb.toString());
                }
                this.remainingArgs = new String[(args.length - curArg)];
                System.arraycopy(args, curArg, this.remainingArgs, 0, this.remainingArgs.length);
            }
            if (this.startChildZygote) {
                boolean seenChildSocketArg = false;
                String[] strArr = this.remainingArgs;
                int length = strArr.length;
                while (true) {
                    if (i >= length) {
                        break;
                    } else if (strArr[i].startsWith(Zygote.CHILD_ZYGOTE_SOCKET_NAME_ARG)) {
                        seenChildSocketArg = true;
                        break;
                    } else {
                        i++;
                    }
                }
                if (!seenChildSocketArg) {
                    throw new IllegalArgumentException("--start-child-zygote specified without --zygote-socket=");
                }
            }
        }
    }

    ZygoteConnection(LocalSocket socket, String abiList2) throws IOException {
        this.mSocket = socket;
        this.abiList = abiList2;
        this.mSocketOutStream = new DataOutputStream(socket.getOutputStream());
        this.mSocketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()), 256);
        this.mSocket.setSoTimeout(1000);
        try {
            this.peer = this.mSocket.getPeerCredentials();
            this.isEof = false;
        } catch (IOException ex) {
            Log.e(TAG, "Cannot read peer credentials", ex);
            throw ex;
        }
    }

    public DataOutputStream getDataOutputStream() {
        return this.mSocketOutStream;
    }

    /* access modifiers changed from: package-private */
    public FileDescriptor getFileDesciptor() {
        return this.mSocket.getFileDescriptor();
    }

    /* JADX WARNING: type inference failed for: r12v8, types: [java.lang.Object[]] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Multi-variable type inference failed */
    public Runnable processOneCommand(ZygoteServer zygoteServer) {
        try {
            String[] args = readArgumentList();
            FileDescriptor[] descriptors = this.mSocket.getAncillaryFileDescriptors();
            if (args == null) {
                this.isEof = true;
                return null;
            } else if (args == null || args.length != 2 || args[0] == null || !args[0].equals("updateHwThemeZipsAndIcons")) {
                FileDescriptor childPipeFd = null;
                FileDescriptor serverPipeFd = null;
                Arguments parsedArgs = new Arguments(args);
                if (parsedArgs.abiListQuery) {
                    handleAbiListQuery();
                    return null;
                } else if (parsedArgs.preloadDefault) {
                    handlePreload();
                    return null;
                } else if (parsedArgs.preloadPackage != null) {
                    handlePreloadPackage(parsedArgs.preloadPackage, parsedArgs.preloadPackageLibs, parsedArgs.preloadPackageLibFileName, parsedArgs.preloadPackageCacheKey);
                    return null;
                } else if (parsedArgs.apiBlacklistExemptions != null) {
                    handleApiBlacklistExemptions(parsedArgs.apiBlacklistExemptions);
                    return null;
                } else if (parsedArgs.hiddenApiAccessLogSampleRate != -1) {
                    handleHiddenApiAccessLogSampleRate(parsedArgs.hiddenApiAccessLogSampleRate);
                    return null;
                } else if (parsedArgs.permittedCapabilities == 0 && parsedArgs.effectiveCapabilities == 0) {
                    applyUidSecurityPolicy(parsedArgs, this.peer);
                    applyInvokeWithSecurityPolicy(parsedArgs, this.peer);
                    applyDebuggerSystemProperty(parsedArgs);
                    applyInvokeWithSystemProperty(parsedArgs);
                    int[][] rlimits = null;
                    if (parsedArgs.rlimits != null) {
                        rlimits = parsedArgs.rlimits.toArray(intArray2d);
                    }
                    int[] fdsToIgnore = null;
                    if (parsedArgs.invokeWith != null) {
                        try {
                            FileDescriptor[] pipeFds = Os.pipe2(OsConstants.O_CLOEXEC);
                            childPipeFd = pipeFds[1];
                            serverPipeFd = pipeFds[0];
                            Os.fcntlInt(childPipeFd, OsConstants.F_SETFD, 0);
                            fdsToIgnore = new int[]{childPipeFd.getInt$(), serverPipeFd.getInt$()};
                        } catch (ErrnoException errnoEx) {
                            throw new IllegalStateException("Unable to set up pipe for invoke-with", errnoEx);
                        }
                    }
                    FileDescriptor serverPipeFd2 = serverPipeFd;
                    FileDescriptor childPipeFd2 = childPipeFd;
                    int[] fdsToIgnore2 = fdsToIgnore;
                    int[] fdsToClose = {-1, -1};
                    FileDescriptor fd = this.mSocket.getFileDescriptor();
                    if (fd != null) {
                        fdsToClose[0] = fd.getInt$();
                    }
                    FileDescriptor fd2 = zygoteServer.getServerSocketFileDescriptor();
                    if (fd2 != null) {
                        fdsToClose[1] = fd2.getInt$();
                    }
                    String[] strArr = args;
                    FileDescriptor[] descriptors2 = descriptors;
                    FileDescriptor serverPipeFd3 = serverPipeFd2;
                    int pid = Zygote.forkAndSpecialize(parsedArgs.uid, parsedArgs.gid, parsedArgs.gids, parsedArgs.runtimeFlags, rlimits, parsedArgs.mountExternal, parsedArgs.seInfo, parsedArgs.niceName, fdsToClose, fdsToIgnore2, parsedArgs.startChildZygote, parsedArgs.instructionSet, parsedArgs.appDataDir);
                    if (pid == 0) {
                        try {
                            zygoteServer.setForkChild();
                            zygoteServer.closeServerSocket();
                            IoUtils.closeQuietly(serverPipeFd3);
                        } catch (Throwable th) {
                            th = th;
                            FileDescriptor[] fileDescriptorArr = descriptors2;
                            IoUtils.closeQuietly(childPipeFd2);
                            IoUtils.closeQuietly(serverPipeFd3);
                            throw th;
                        }
                        try {
                            try {
                                Runnable handleChildProc = handleChildProc(parsedArgs, descriptors2, childPipeFd2, parsedArgs.startChildZygote);
                                IoUtils.closeQuietly(childPipeFd2);
                                IoUtils.closeQuietly(null);
                                return handleChildProc;
                            } catch (Throwable th2) {
                                th = th2;
                                serverPipeFd3 = null;
                                IoUtils.closeQuietly(childPipeFd2);
                                IoUtils.closeQuietly(serverPipeFd3);
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            FileDescriptor[] fileDescriptorArr2 = descriptors2;
                            serverPipeFd3 = null;
                            IoUtils.closeQuietly(childPipeFd2);
                            IoUtils.closeQuietly(serverPipeFd3);
                            throw th;
                        }
                    } else {
                        FileDescriptor[] descriptors3 = descriptors2;
                        try {
                            IoUtils.closeQuietly(childPipeFd2);
                            childPipeFd2 = null;
                            handleParentProc(pid, descriptors3, serverPipeFd3);
                            IoUtils.closeQuietly(null);
                            IoUtils.closeQuietly(serverPipeFd3);
                            return null;
                        } catch (Throwable th4) {
                            th = th4;
                            IoUtils.closeQuietly(childPipeFd2);
                            IoUtils.closeQuietly(serverPipeFd3);
                            throw th;
                        }
                    }
                } else {
                    String[] strArr2 = args;
                    FileDescriptor[] fileDescriptorArr3 = descriptors;
                    throw new ZygoteSecurityException("Client may not specify capabilities: permitted=0x" + Long.toHexString(parsedArgs.permittedCapabilities) + ", effective=0x" + Long.toHexString(parsedArgs.effectiveCapabilities));
                }
            } else {
                int currentUserId = 0;
                try {
                    if (args[1] != null && args[1].startsWith("setuid=")) {
                        currentUserId = Integer.parseInt(args[1].substring(args[1].indexOf(61) + 1));
                    }
                    ZygoteInit.clearHwThemeZipsAndSomeIcons();
                    ZygoteInit.preloadHwThemeZipsAndSomeIcons(currentUserId);
                    try {
                        this.mSocketOutStream.writeInt(1);
                        this.mSocketOutStream.writeBoolean(false);
                    } catch (IOException ex) {
                        Log.e(TAG, "Error writing to command socket", ex);
                    }
                } catch (Exception ex2) {
                    Log.e(TAG, "updateHwThemeZipsAndSomeIcons", ex2);
                    this.mSocketOutStream.writeInt(1);
                    this.mSocketOutStream.writeBoolean(false);
                } catch (Throwable th5) {
                    Throwable th6 = th5;
                    try {
                        this.mSocketOutStream.writeInt(1);
                        this.mSocketOutStream.writeBoolean(false);
                    } catch (IOException ex3) {
                        Log.e(TAG, "Error writing to command socket", ex3);
                    }
                    throw th6;
                }
                return null;
            }
        } catch (IOException ex4) {
            throw new IllegalStateException("IOException on command socket", ex4);
        }
    }

    private void handleAbiListQuery() {
        try {
            byte[] abiListBytes = this.abiList.getBytes(StandardCharsets.US_ASCII);
            this.mSocketOutStream.writeInt(abiListBytes.length);
            this.mSocketOutStream.write(abiListBytes);
        } catch (IOException ioe) {
            throw new IllegalStateException("Error writing to command socket", ioe);
        }
    }

    private void handlePreload() {
        try {
            if (isPreloadComplete()) {
                this.mSocketOutStream.writeInt(1);
                return;
            }
            preload();
            this.mSocketOutStream.writeInt(0);
        } catch (IOException ioe) {
            throw new IllegalStateException("Error writing to command socket", ioe);
        }
    }

    private void handleApiBlacklistExemptions(String[] exemptions) {
        try {
            ZygoteInit.setApiBlacklistExemptions(exemptions);
            this.mSocketOutStream.writeInt(0);
        } catch (IOException ioe) {
            throw new IllegalStateException("Error writing to command socket", ioe);
        }
    }

    private void handleHiddenApiAccessLogSampleRate(int percent) {
        try {
            ZygoteInit.setHiddenApiAccessLogSampleRate(percent);
            this.mSocketOutStream.writeInt(0);
        } catch (IOException ioe) {
            throw new IllegalStateException("Error writing to command socket", ioe);
        }
    }

    /* access modifiers changed from: protected */
    public void preload() {
        ZygoteInit.lazyPreload();
    }

    /* access modifiers changed from: protected */
    public boolean isPreloadComplete() {
        return ZygoteInit.isPreloadComplete();
    }

    /* access modifiers changed from: protected */
    public DataOutputStream getSocketOutputStream() {
        return this.mSocketOutStream;
    }

    /* access modifiers changed from: protected */
    public void handlePreloadPackage(String packagePath, String libsPath, String libFileName, String cacheKey) {
        throw new RuntimeException("Zyogte does not support package preloading");
    }

    /* access modifiers changed from: package-private */
    public void closeSocket() {
        try {
            this.mSocket.close();
        } catch (IOException ex) {
            Log.e(TAG, "Exception while closing command socket in parent", ex);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isClosedByPeer() {
        return this.isEof;
    }

    private String[] readArgumentList() throws IOException {
        try {
            String s = this.mSocketReader.readLine();
            if (s == null) {
                return null;
            }
            int argc = Integer.parseInt(s);
            if (argc <= 1024) {
                String[] result = new String[argc];
                int i = 0;
                while (i < argc) {
                    result[i] = this.mSocketReader.readLine();
                    if (result[i] != null) {
                        i++;
                    } else {
                        throw new IOException("truncated request");
                    }
                }
                return result;
            }
            throw new IOException("max arg count exceeded");
        } catch (NumberFormatException e) {
            Log.e(TAG, "invalid Zygote wire format: non-int at argc");
            throw new IOException("invalid wire format");
        }
    }

    private static void applyUidSecurityPolicy(Arguments args, Credentials peer2) throws ZygoteSecurityException {
        if (peer2.getUid() == 1000) {
            if ((FactoryTest.getMode() == 0) && args.uidSpecified && args.uid < 1000) {
                throw new ZygoteSecurityException("System UID may not launch process with UID < 1000");
            }
        }
        if (!args.uidSpecified) {
            args.uid = peer2.getUid();
            args.uidSpecified = true;
        }
        if (!args.gidSpecified) {
            args.gid = peer2.getGid();
            args.gidSpecified = true;
        }
    }

    public static void applyDebuggerSystemProperty(Arguments args) {
        if (RoSystemProperties.DEBUGGABLE) {
            args.runtimeFlags |= 1;
        }
    }

    private static void applyInvokeWithSecurityPolicy(Arguments args, Credentials peer2) throws ZygoteSecurityException {
        int peerUid = peer2.getUid();
        if (args.invokeWith != null && peerUid != 0 && (args.runtimeFlags & 1) == 0) {
            throw new ZygoteSecurityException("Peer is permitted to specify anexplicit invoke-with wrapper command only for debuggableapplications.");
        }
    }

    public static void applyInvokeWithSystemProperty(Arguments args) {
        if (args.invokeWith == null && args.niceName != null) {
            args.invokeWith = SystemProperties.get("wrap." + args.niceName);
            if (args.invokeWith != null && args.invokeWith.length() == 0) {
                args.invokeWith = null;
            }
        }
    }

    private Runnable handleChildProc(Arguments parsedArgs, FileDescriptor[] descriptors, FileDescriptor pipeFd, boolean isZygote) {
        closeSocket();
        if (descriptors != null) {
            try {
                Os.dup2(descriptors[0], OsConstants.STDIN_FILENO);
                Os.dup2(descriptors[1], OsConstants.STDOUT_FILENO);
                Os.dup2(descriptors[2], OsConstants.STDERR_FILENO);
                for (FileDescriptor fd : descriptors) {
                    IoUtils.closeQuietly(fd);
                }
            } catch (ErrnoException ex) {
                Log.e(TAG, "Error reopening stdio", ex);
            }
        }
        if (parsedArgs.niceName != null) {
            Process.setArgV0(parsedArgs.niceName);
        }
        Trace.traceEnd(64);
        if (parsedArgs.invokeWith != null) {
            WrapperInit.execApplication(parsedArgs.invokeWith, parsedArgs.niceName, parsedArgs.targetSdkVersion, VMRuntime.getCurrentInstructionSet(), pipeFd, parsedArgs.remainingArgs);
            throw new IllegalStateException("WrapperInit.execApplication unexpectedly returned");
        } else if (!isZygote) {
            return ZygoteInit.zygoteInit(parsedArgs.targetSdkVersion, parsedArgs.remainingArgs, null);
        } else {
            return ZygoteInit.childZygoteInit(parsedArgs.targetSdkVersion, parsedArgs.remainingArgs, null);
        }
    }

    private void handleParentProc(int pid, FileDescriptor[] descriptors, FileDescriptor pipeFd) {
        int pid2;
        int i = pid;
        FileDescriptor[] fileDescriptorArr = descriptors;
        FileDescriptor fileDescriptor = pipeFd;
        if (i > 0) {
            setChildPgid(pid);
        }
        short s = 0;
        if (fileDescriptorArr != null) {
            for (FileDescriptor fd : fileDescriptorArr) {
                IoUtils.closeQuietly(fd);
            }
        }
        boolean usingWrapper = false;
        if (fileDescriptor != null && i > 0) {
            int innerPid = -1;
            try {
                StructPollfd[] fds = {new StructPollfd()};
                byte[] data = new byte[4];
                int remainingSleepTime = 30000;
                int dataIndex = 0;
                long startTime = System.nanoTime();
                while (dataIndex < data.length && remainingSleepTime > 0) {
                    fds[s].fd = fileDescriptor;
                    fds[s].events = (short) OsConstants.POLLIN;
                    fds[s].revents = s;
                    fds[s].userData = null;
                    int res = Os.poll(fds, remainingSleepTime);
                    remainingSleepTime = 30000 - ((int) ((System.nanoTime() - startTime) / 1000000));
                    if (res > 0) {
                        if ((fds[0].revents & OsConstants.POLLIN) == 0) {
                            break;
                        }
                        int readBytes = Os.read(fileDescriptor, data, dataIndex, 1);
                        if (readBytes >= 0) {
                            dataIndex += readBytes;
                        } else {
                            throw new RuntimeException("Some error");
                        }
                    } else if (res == 0) {
                        Log.w(TAG, "Timed out waiting for child.");
                    }
                    s = 0;
                }
                if (dataIndex == data.length) {
                    innerPid = new DataInputStream(new ByteArrayInputStream(data)).readInt();
                }
                if (innerPid == -1) {
                    Log.w(TAG, "Error reading pid from wrapped process, child may have died");
                }
            } catch (Exception ex) {
                Log.w(TAG, "Error reading pid from wrapped process, child may have died", ex);
            }
            if (innerPid > 0) {
                int parentPid = innerPid;
                while (parentPid > 0 && parentPid != i) {
                    parentPid = Process.getParentPid(parentPid);
                }
                if (parentPid > 0) {
                    Log.i(TAG, "Wrapped process has pid " + innerPid);
                    pid2 = innerPid;
                    usingWrapper = true;
                    this.mSocketOutStream.writeInt(pid2);
                    this.mSocketOutStream.writeBoolean(usingWrapper);
                }
                Log.w(TAG, "Wrapped process reported a pid that is not a child of the process that we forked: childPid=" + i + " innerPid=" + innerPid);
            }
        }
        pid2 = i;
        try {
            this.mSocketOutStream.writeInt(pid2);
            this.mSocketOutStream.writeBoolean(usingWrapper);
        } catch (IOException ex2) {
            throw new IllegalStateException("Error writing to command socket", ex2);
        }
    }

    private void setChildPgid(int pid) {
        try {
            Os.setpgid(pid, Os.getpgid(this.peer.getPid()));
        } catch (ErrnoException e) {
            Log.i(TAG, "Zygote: setpgid failed. This is normal if peer is not in our session");
        }
    }
}
