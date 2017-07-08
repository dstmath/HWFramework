package com.android.internal.os;

import android.net.Credentials;
import android.net.LocalSocket;
import android.os.Process;
import android.os.SystemProperties;
import android.os.Trace;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import android.util.PtmLog;
import com.android.internal.os.ZygoteInit.MethodAndArgsCaller;
import com.huawei.pgmng.log.LogPower;
import dalvik.system.VMRuntime;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL10;
import libcore.io.IoUtils;

class ZygoteConnection {
    private static final int CONNECTION_TIMEOUT_MILLIS = 1000;
    private static final int MAX_ZYGOTE_ARGC = 1024;
    private static final String TAG = "Zygote";
    private static final int[][] intArray2d = null;
    private final String abiList;
    private final LocalSocket mSocket;
    private final DataOutputStream mSocketOutStream;
    private final BufferedReader mSocketReader;
    private final Credentials peer;

    static class Arguments {
        boolean abiListQuery;
        String appDataDir;
        boolean capabilitiesSpecified;
        int debugFlags;
        long effectiveCapabilities;
        int gid;
        boolean gidSpecified;
        int[] gids;
        String instructionSet;
        String invokeWith;
        int mountExternal;
        String niceName;
        long permittedCapabilities;
        String[] remainingArgs;
        ArrayList<int[]> rlimits;
        String seInfo;
        boolean seInfoSpecified;
        int targetSdkVersion;
        boolean targetSdkVersionSpecified;
        int uid;
        boolean uidSpecified;

        Arguments(String[] args) throws IllegalArgumentException {
            this.uid = 0;
            this.gid = 0;
            this.mountExternal = 0;
            parseArgs(args);
        }

        private void parseArgs(String[] args) throws IllegalArgumentException {
            int curArg = 0;
            boolean seenRuntimeArgs = false;
            while (curArg < args.length) {
                String arg = args[curArg];
                if (arg.equals("--")) {
                    curArg++;
                    break;
                }
                if (!arg.startsWith("--setuid=")) {
                    if (!arg.startsWith("--setgid=")) {
                        if (!arg.startsWith("--target-sdk-version=")) {
                            if (!arg.equals("--enable-debugger")) {
                                if (!arg.equals("--enable-safemode")) {
                                    if (!arg.equals("--enable-checkjni")) {
                                        if (!arg.equals("--generate-debug-info")) {
                                            if (!arg.equals("--always-jit")) {
                                                if (!arg.equals("--native-debuggable")) {
                                                    if (!arg.equals("--enable-jni-logging")) {
                                                        if (!arg.equals("--enable-assert")) {
                                                            if (!arg.equals("--runtime-args")) {
                                                                if (!arg.startsWith("--seinfo=")) {
                                                                    if (!arg.startsWith("--capabilities=")) {
                                                                        int i;
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
                                                                                                                break;
                                                                                                            }
                                                                                                            this.appDataDir = arg.substring(arg.indexOf(61) + 1);
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
                                                                                    } else if (this.niceName != null) {
                                                                                        throw new IllegalArgumentException("Duplicate arg specified");
                                                                                    } else {
                                                                                        this.niceName = arg.substring(arg.indexOf(61) + 1);
                                                                                    }
                                                                                } else if (this.invokeWith != null) {
                                                                                    throw new IllegalArgumentException("Duplicate arg specified");
                                                                                } else {
                                                                                    curArg++;
                                                                                    try {
                                                                                        this.invokeWith = args[curArg];
                                                                                    } catch (IndexOutOfBoundsException e) {
                                                                                        throw new IllegalArgumentException("--invoke-with requires argument");
                                                                                    }
                                                                                }
                                                                            } else if (this.gids != null) {
                                                                                throw new IllegalArgumentException("Duplicate arg specified");
                                                                            } else {
                                                                                String[] params = arg.substring(arg.indexOf(61) + 1).split(PtmLog.PAIRE_DELIMETER);
                                                                                this.gids = new int[params.length];
                                                                                for (i = params.length - 1; i >= 0; i--) {
                                                                                    this.gids[i] = Integer.parseInt(params[i]);
                                                                                }
                                                                            }
                                                                        } else {
                                                                            String[] limitStrings = arg.substring(arg.indexOf(61) + 1).split(PtmLog.PAIRE_DELIMETER);
                                                                            if (limitStrings.length != 3) {
                                                                                throw new IllegalArgumentException("--rlimit= should have 3 comma-delimited ints");
                                                                            }
                                                                            int[] rlimitTuple = new int[limitStrings.length];
                                                                            for (i = 0; i < limitStrings.length; i++) {
                                                                                rlimitTuple[i] = Integer.parseInt(limitStrings[i]);
                                                                            }
                                                                            if (this.rlimits == null) {
                                                                                this.rlimits = new ArrayList();
                                                                            }
                                                                            this.rlimits.add(rlimitTuple);
                                                                        }
                                                                    } else if (this.capabilitiesSpecified) {
                                                                        throw new IllegalArgumentException("Duplicate arg specified");
                                                                    } else {
                                                                        this.capabilitiesSpecified = true;
                                                                        String[] capStrings = arg.substring(arg.indexOf(61) + 1).split(PtmLog.PAIRE_DELIMETER, 2);
                                                                        if (capStrings.length == 1) {
                                                                            this.effectiveCapabilities = Long.decode(capStrings[0]).longValue();
                                                                            this.permittedCapabilities = this.effectiveCapabilities;
                                                                        } else {
                                                                            this.permittedCapabilities = Long.decode(capStrings[0]).longValue();
                                                                            this.effectiveCapabilities = Long.decode(capStrings[1]).longValue();
                                                                        }
                                                                    }
                                                                } else if (this.seInfoSpecified) {
                                                                    throw new IllegalArgumentException("Duplicate arg specified");
                                                                } else {
                                                                    this.seInfoSpecified = true;
                                                                    this.seInfo = arg.substring(arg.indexOf(61) + 1);
                                                                }
                                                            } else {
                                                                seenRuntimeArgs = true;
                                                            }
                                                        } else {
                                                            this.debugFlags |= 4;
                                                        }
                                                    } else {
                                                        this.debugFlags |= 16;
                                                    }
                                                } else {
                                                    this.debugFlags |= LogPower.START_CHG_ROTATION;
                                                }
                                            } else {
                                                this.debugFlags |= 64;
                                            }
                                        } else {
                                            this.debugFlags |= 32;
                                        }
                                    } else {
                                        this.debugFlags |= 2;
                                    }
                                } else {
                                    this.debugFlags |= 8;
                                }
                            } else {
                                this.debugFlags |= 1;
                            }
                        } else if (this.targetSdkVersionSpecified) {
                            throw new IllegalArgumentException("Duplicate target-sdk-version specified");
                        } else {
                            this.targetSdkVersionSpecified = true;
                            this.targetSdkVersion = Integer.parseInt(arg.substring(arg.indexOf(61) + 1));
                        }
                    } else if (this.gidSpecified) {
                        throw new IllegalArgumentException("Duplicate arg specified");
                    } else {
                        this.gidSpecified = true;
                        this.gid = Integer.parseInt(arg.substring(arg.indexOf(61) + 1));
                    }
                } else if (this.uidSpecified) {
                    throw new IllegalArgumentException("Duplicate arg specified");
                } else {
                    this.uidSpecified = true;
                    this.uid = Integer.parseInt(arg.substring(arg.indexOf(61) + 1));
                }
                curArg++;
            }
            if (this.abiListQuery) {
                if (args.length - curArg > 0) {
                    throw new IllegalArgumentException("Unexpected arguments after --query-abi-list.");
                }
            } else if (seenRuntimeArgs) {
                this.remainingArgs = new String[(args.length - curArg)];
                System.arraycopy(args, curArg, this.remainingArgs, 0, this.remainingArgs.length);
            } else {
                throw new IllegalArgumentException("Unexpected argument : " + args[curArg]);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.os.ZygoteConnection.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.os.ZygoteConnection.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.ZygoteConnection.<clinit>():void");
    }

    ZygoteConnection(LocalSocket socket, String abiList) throws IOException {
        this.mSocket = socket;
        this.abiList = abiList;
        this.mSocketOutStream = new DataOutputStream(socket.getOutputStream());
        this.mSocketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()), GL10.GL_DEPTH_BUFFER_BIT);
        this.mSocket.setSoTimeout(CONNECTION_TIMEOUT_MILLIS);
        try {
            this.peer = this.mSocket.getPeerCredentials();
        } catch (IOException ex) {
            Log.e(TAG, "Cannot read peer credentials", ex);
            throw ex;
        }
    }

    FileDescriptor getFileDesciptor() {
        return this.mSocket.getFileDescriptor();
    }

    boolean runOnce() throws MethodAndArgsCaller {
        boolean handleParentProc;
        Throwable ex;
        Throwable ex2;
        Throwable ex3;
        Arguments arguments = null;
        try {
            String[] args = readArgumentList();
            FileDescriptor[] descriptors = this.mSocket.getAncillaryFileDescriptors();
            if (args == null) {
                closeSocket();
                return true;
            } else if (args == null || args.length != 2 || args[0] == null || !args[0].equals("updateHwThemeZipsAndIcons")) {
                PrintStream printStream = null;
                if (descriptors != null && descriptors.length >= 3) {
                    PrintStream printStream2 = new PrintStream(new FileOutputStream(descriptors[2]));
                }
                int pid = -1;
                FileDescriptor fileDescriptor = null;
                FileDescriptor fileDescriptor2 = null;
                try {
                    Arguments arguments2 = new Arguments(args);
                    try {
                        if (arguments2.abiListQuery) {
                            return handleAbiListQuery();
                        }
                        if (arguments2.permittedCapabilities == 0 && arguments2.effectiveCapabilities == 0) {
                            applyUidSecurityPolicy(arguments2, this.peer);
                            applyInvokeWithSecurityPolicy(arguments2, this.peer);
                            applyDebuggerSystemProperty(arguments2);
                            applyInvokeWithSystemProperty(arguments2);
                            int[][] iArr = null;
                            if (arguments2.rlimits != null) {
                                iArr = (int[][]) arguments2.rlimits.toArray(intArray2d);
                            }
                            if (arguments2.invokeWith != null) {
                                FileDescriptor[] pipeFds = Os.pipe2(OsConstants.O_CLOEXEC);
                                fileDescriptor = pipeFds[1];
                                fileDescriptor2 = pipeFds[0];
                                Os.fcntlInt(fileDescriptor, OsConstants.F_SETFD, 0);
                            }
                            int[] fdsToClose = new int[]{-1, -1};
                            FileDescriptor fd = this.mSocket.getFileDescriptor();
                            if (fd != null) {
                                fdsToClose[0] = fd.getInt$();
                            }
                            fd = ZygoteInit.getServerSocketFileDescriptor();
                            if (fd != null) {
                                fdsToClose[1] = fd.getInt$();
                            }
                            pid = Zygote.forkAndSpecialize(arguments2.uid, arguments2.gid, arguments2.gids, arguments2.debugFlags, iArr, arguments2.mountExternal, arguments2.seInfo, arguments2.niceName, fdsToClose, arguments2.instructionSet, arguments2.appDataDir);
                            arguments = arguments2;
                            if (pid == 0) {
                                try {
                                    IoUtils.closeQuietly(fileDescriptor2);
                                    fileDescriptor2 = null;
                                    handleChildProc(arguments, descriptors, fileDescriptor, printStream);
                                } finally {
                                    IoUtils.closeQuietly(fileDescriptor);
                                    IoUtils.closeQuietly(fileDescriptor2);
                                }
                            } else {
                                IoUtils.closeQuietly(fileDescriptor);
                                fileDescriptor = null;
                                handleParentProc = handleParentProc(pid, descriptors, fileDescriptor2, arguments);
                                IoUtils.closeQuietly(null);
                                IoUtils.closeQuietly(fileDescriptor2);
                                return handleParentProc;
                            }
                            return true;
                        }
                        throw new ZygoteSecurityException("Client may not specify capabilities: permitted=0x" + Long.toHexString(arguments2.permittedCapabilities) + ", effective=0x" + Long.toHexString(arguments2.effectiveCapabilities));
                    } catch (ErrnoException e) {
                        ex = e;
                        arguments = arguments2;
                        logAndPrintError(printStream, "Exception creating pipe", ex);
                        if (pid == 0) {
                            IoUtils.closeQuietly(fileDescriptor);
                            fileDescriptor = null;
                            handleParentProc = handleParentProc(pid, descriptors, fileDescriptor2, arguments);
                            IoUtils.closeQuietly(null);
                            IoUtils.closeQuietly(fileDescriptor2);
                            return handleParentProc;
                        }
                        IoUtils.closeQuietly(fileDescriptor2);
                        fileDescriptor2 = null;
                        handleChildProc(arguments, descriptors, fileDescriptor, printStream);
                        return true;
                    } catch (IllegalArgumentException e2) {
                        ex2 = e2;
                        arguments = arguments2;
                        logAndPrintError(printStream, "Invalid zygote arguments", ex2);
                        if (pid == 0) {
                            IoUtils.closeQuietly(fileDescriptor2);
                            fileDescriptor2 = null;
                            handleChildProc(arguments, descriptors, fileDescriptor, printStream);
                        } else {
                            IoUtils.closeQuietly(fileDescriptor);
                            fileDescriptor = null;
                            handleParentProc = handleParentProc(pid, descriptors, fileDescriptor2, arguments);
                            IoUtils.closeQuietly(null);
                            IoUtils.closeQuietly(fileDescriptor2);
                            return handleParentProc;
                        }
                        return true;
                    } catch (ZygoteSecurityException e3) {
                        ex3 = e3;
                        arguments = arguments2;
                        logAndPrintError(printStream, "Zygote security policy prevents request: ", ex3);
                        if (pid == 0) {
                            IoUtils.closeQuietly(fileDescriptor);
                            fileDescriptor = null;
                            handleParentProc = handleParentProc(pid, descriptors, fileDescriptor2, arguments);
                            IoUtils.closeQuietly(null);
                            IoUtils.closeQuietly(fileDescriptor2);
                            return handleParentProc;
                        }
                        IoUtils.closeQuietly(fileDescriptor2);
                        fileDescriptor2 = null;
                        handleChildProc(arguments, descriptors, fileDescriptor, printStream);
                        return true;
                    }
                } catch (ErrnoException e4) {
                    ex = e4;
                    logAndPrintError(printStream, "Exception creating pipe", ex);
                    if (pid == 0) {
                        IoUtils.closeQuietly(fileDescriptor2);
                        fileDescriptor2 = null;
                        handleChildProc(arguments, descriptors, fileDescriptor, printStream);
                    } else {
                        IoUtils.closeQuietly(fileDescriptor);
                        fileDescriptor = null;
                        handleParentProc = handleParentProc(pid, descriptors, fileDescriptor2, arguments);
                        IoUtils.closeQuietly(null);
                        IoUtils.closeQuietly(fileDescriptor2);
                        return handleParentProc;
                    }
                    return true;
                } catch (IllegalArgumentException e5) {
                    ex2 = e5;
                    logAndPrintError(printStream, "Invalid zygote arguments", ex2);
                    if (pid == 0) {
                        IoUtils.closeQuietly(fileDescriptor);
                        fileDescriptor = null;
                        handleParentProc = handleParentProc(pid, descriptors, fileDescriptor2, arguments);
                        IoUtils.closeQuietly(null);
                        IoUtils.closeQuietly(fileDescriptor2);
                        return handleParentProc;
                    }
                    IoUtils.closeQuietly(fileDescriptor2);
                    fileDescriptor2 = null;
                    handleChildProc(arguments, descriptors, fileDescriptor, printStream);
                    return true;
                } catch (ZygoteSecurityException e6) {
                    ex3 = e6;
                    logAndPrintError(printStream, "Zygote security policy prevents request: ", ex3);
                    if (pid == 0) {
                        IoUtils.closeQuietly(fileDescriptor2);
                        fileDescriptor2 = null;
                        handleChildProc(arguments, descriptors, fileDescriptor, printStream);
                    } else {
                        IoUtils.closeQuietly(fileDescriptor);
                        fileDescriptor = null;
                        handleParentProc = handleParentProc(pid, descriptors, fileDescriptor2, arguments);
                        IoUtils.closeQuietly(null);
                        IoUtils.closeQuietly(fileDescriptor2);
                        return handleParentProc;
                    }
                    return true;
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
                    } catch (Throwable ex4) {
                        Log.e(TAG, "Error writing to command socket", ex4);
                    }
                } catch (Throwable ex5) {
                    Log.e(TAG, "updateHwThemeZipsAndSomeIcons", ex5);
                } catch (Throwable th) {
                    try {
                        this.mSocketOutStream.writeInt(1);
                        this.mSocketOutStream.writeBoolean(false);
                    } catch (Throwable ex42) {
                        Log.e(TAG, "Error writing to command socket", ex42);
                    }
                }
                return false;
            }
        } catch (IOException ex6) {
            Log.w(TAG, "IOException on command socket " + ex6.getMessage());
            closeSocket();
            return true;
        }
    }

    private boolean handleAbiListQuery() {
        try {
            byte[] abiListBytes = this.abiList.getBytes(StandardCharsets.US_ASCII);
            this.mSocketOutStream.writeInt(abiListBytes.length);
            this.mSocketOutStream.write(abiListBytes);
            return false;
        } catch (IOException ioe) {
            Log.e(TAG, "Error writing to command socket", ioe);
            return true;
        }
    }

    void closeSocket() {
        try {
            this.mSocket.close();
        } catch (IOException ex) {
            Log.e(TAG, "Exception while closing command socket in parent", ex);
        }
    }

    private String[] readArgumentList() throws IOException {
        try {
            String s = this.mSocketReader.readLine();
            if (s == null) {
                return null;
            }
            int argc = Integer.parseInt(s);
            if (argc > MAX_ZYGOTE_ARGC) {
                throw new IOException("max arg count exceeded");
            }
            String[] result = new String[argc];
            for (int i = 0; i < argc; i++) {
                result[i] = this.mSocketReader.readLine();
                if (result[i] == null) {
                    throw new IOException("truncated request");
                }
            }
            return result;
        } catch (NumberFormatException e) {
            Log.e(TAG, "invalid Zygote wire format: non-int at argc");
            throw new IOException("invalid wire format");
        }
    }

    private static void applyUidSecurityPolicy(Arguments args, Credentials peer) throws ZygoteSecurityException {
        if (peer.getUid() == CONNECTION_TIMEOUT_MILLIS) {
            boolean uidRestricted;
            String factoryTest = SystemProperties.get("ro.factorytest");
            if (factoryTest.equals("1") || factoryTest.equals("2")) {
                uidRestricted = false;
            } else {
                uidRestricted = true;
            }
            if (uidRestricted && args.uidSpecified && args.uid < CONNECTION_TIMEOUT_MILLIS) {
                throw new ZygoteSecurityException("System UID may not launch process with UID < 1000");
            }
        }
        if (!args.uidSpecified) {
            args.uid = peer.getUid();
            args.uidSpecified = true;
        }
        if (!args.gidSpecified) {
            args.gid = peer.getGid();
            args.gidSpecified = true;
        }
    }

    public static void applyDebuggerSystemProperty(Arguments args) {
        if ("1".equals(SystemProperties.get("ro.debuggable"))) {
            args.debugFlags |= 1;
        }
    }

    private static void applyInvokeWithSecurityPolicy(Arguments args, Credentials peer) throws ZygoteSecurityException {
        int peerUid = peer.getUid();
        if (args.invokeWith != null && peerUid != 0) {
            throw new ZygoteSecurityException("Peer is not permitted to specify an explicit invoke-with wrapper command");
        }
    }

    public static void applyInvokeWithSystemProperty(Arguments args) {
        if (args.invokeWith == null && args.niceName != null) {
            String property = "wrap." + args.niceName;
            if (property.length() > 31) {
                if (property.charAt(30) != '.') {
                    property = property.substring(0, 31);
                } else {
                    property = property.substring(0, 30);
                }
            }
            args.invokeWith = SystemProperties.get(property);
            if (args.invokeWith != null && args.invokeWith.length() == 0) {
                args.invokeWith = null;
            }
        }
    }

    private void handleChildProc(Arguments parsedArgs, FileDescriptor[] descriptors, FileDescriptor pipeFd, PrintStream newStderr) throws MethodAndArgsCaller {
        closeSocket();
        ZygoteInit.closeServerSocket();
        if (descriptors != null) {
            try {
                Os.dup2(descriptors[0], OsConstants.STDIN_FILENO);
                Os.dup2(descriptors[1], OsConstants.STDOUT_FILENO);
                Os.dup2(descriptors[2], OsConstants.STDERR_FILENO);
                for (FileDescriptor fd : descriptors) {
                    IoUtils.closeQuietly(fd);
                }
                newStderr = System.err;
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
            return;
        }
        RuntimeInit.zygoteInit(parsedArgs.targetSdkVersion, parsedArgs.remainingArgs, null);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean handleParentProc(int pid, FileDescriptor[] descriptors, FileDescriptor pipeFd, Arguments parsedArgs) {
        if (pid > 0) {
            setChildPgid(pid);
        }
        if (descriptors != null) {
            for (FileDescriptor fd : descriptors) {
                IoUtils.closeQuietly(fd);
            }
        }
        boolean usingWrapper = false;
        if (pipeFd != null && pid > 0) {
            DataInputStream is = new DataInputStream(new FileInputStream(pipeFd));
            int innerPid = -1;
            try {
                innerPid = is.readInt();
                try {
                    is.close();
                } catch (IOException e) {
                }
            } catch (IOException ex) {
                Log.w(TAG, "Error reading pid from wrapped process, child may have died", ex);
            } catch (Throwable th) {
                try {
                    is.close();
                } catch (IOException e2) {
                }
            }
            if (innerPid > 0) {
                int parentPid = innerPid;
                while (parentPid > 0 && parentPid != pid) {
                    parentPid = Process.getParentPid(parentPid);
                }
                if (parentPid > 0) {
                    Log.i(TAG, "Wrapped process has pid " + innerPid);
                    pid = innerPid;
                    usingWrapper = true;
                } else {
                    Log.w(TAG, "Wrapped process reported a pid that is not a child of the process that we forked: childPid=" + pid + " innerPid=" + innerPid);
                }
            }
        }
        try {
            this.mSocketOutStream.writeInt(pid);
            this.mSocketOutStream.writeBoolean(usingWrapper);
            return false;
        } catch (IOException ex2) {
            Log.e(TAG, "Error writing to command socket", ex2);
            return true;
        }
    }

    private void setChildPgid(int pid) {
        try {
            Os.setpgid(pid, Os.getpgid(this.peer.getPid()));
        } catch (ErrnoException e) {
            Log.i(TAG, "Zygote: setpgid failed. This is normal if peer is not in our session");
        }
    }

    private static void logAndPrintError(PrintStream newStderr, String message, Throwable ex) {
        Log.e(TAG, message, ex);
        if (newStderr != null) {
            StringBuilder append = new StringBuilder().append(message);
            if (ex == null) {
                ex = "";
            }
            newStderr.println(append.append(ex).toString());
        }
    }
}
