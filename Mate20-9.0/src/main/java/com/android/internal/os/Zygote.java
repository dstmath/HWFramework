package com.android.internal.os;

import android.os.Trace;
import android.system.ErrnoException;
import android.system.Os;
import dalvik.system.ZygoteHooks;

public final class Zygote {
    public static final int API_ENFORCEMENT_POLICY_MASK = 12288;
    public static final int API_ENFORCEMENT_POLICY_SHIFT = Integer.numberOfTrailingZeros(12288);
    public static final String CHILD_ZYGOTE_SOCKET_NAME_ARG = "--zygote-socket=";
    public static final int DEBUG_ALWAYS_JIT = 64;
    public static final int DEBUG_ENABLE_ASSERT = 4;
    public static final int DEBUG_ENABLE_CHECKJNI = 2;
    public static final int DEBUG_ENABLE_JDWP = 1;
    public static final int DEBUG_ENABLE_JNI_LOGGING = 16;
    public static final int DEBUG_ENABLE_SAFEMODE = 8;
    public static final int DEBUG_GENERATE_DEBUG_INFO = 32;
    public static final int DEBUG_GENERATE_MINI_DEBUG_INFO = 2048;
    public static final int DEBUG_JAVA_DEBUGGABLE = 256;
    public static final int DEBUG_NATIVE_DEBUGGABLE = 128;
    public static final int DISABLE_VERIFIER = 512;
    public static final int MOUNT_EXTERNAL_DEFAULT = 1;
    public static final int MOUNT_EXTERNAL_NONE = 0;
    public static final int MOUNT_EXTERNAL_READ = 2;
    public static final int MOUNT_EXTERNAL_WRITE = 3;
    public static final int ONLY_USE_SYSTEM_OAT_FILES = 1024;
    public static final int PROFILE_SYSTEM_SERVER = 16384;
    private static final ZygoteHooks VM_HOOKS = new ZygoteHooks();

    public static native int ctrlSockets(int i, int i2);

    protected static native void nativeAllowFileAcrossFork(String str);

    private static native int nativeForkAndSpecialize(int i, int i2, int[] iArr, int i3, int[][] iArr2, int i4, String str, String str2, int[] iArr3, int[] iArr4, boolean z, String str3, String str4);

    private static native int nativeForkSystemServer(int i, int i2, int[] iArr, int i3, int[][] iArr2, long j, long j2);

    static native void nativePreApplicationInit();

    static native void nativeSecurityInit();

    protected static native void nativeUnmountStorageOnInit();

    public static native int procCtrlSockets(int i, int i2, int i3);

    private Zygote() {
    }

    public static int forkAndSpecialize(int uid, int gid, int[] gids, int runtimeFlags, int[][] rlimits, int mountExternal, String seInfo, String niceName, int[] fdsToClose, int[] fdsToIgnore, boolean startChildZygote, String instructionSet, String appDataDir) {
        VM_HOOKS.preFork();
        resetNicePriority();
        int pid = nativeForkAndSpecialize(uid, gid, gids, runtimeFlags, rlimits, mountExternal, seInfo, niceName, fdsToClose, fdsToIgnore, startChildZygote, instructionSet, appDataDir);
        if (pid == 0) {
            Trace.setTracingEnabled(true, runtimeFlags);
            Trace.traceBegin(64, "PostFork");
        } else {
            int i = runtimeFlags;
        }
        VM_HOOKS.postForkCommon();
        return pid;
    }

    public static int forkSystemServer(int uid, int gid, int[] gids, int runtimeFlags, int[][] rlimits, long permittedCapabilities, long effectiveCapabilities) {
        VM_HOOKS.preFork();
        resetNicePriority();
        int pid = nativeForkSystemServer(uid, gid, gids, runtimeFlags, rlimits, permittedCapabilities, effectiveCapabilities);
        if (pid == 0) {
            Trace.setTracingEnabled(true, runtimeFlags);
        }
        VM_HOOKS.postForkCommon();
        return pid;
    }

    private static void callPostForkChildHooks(int runtimeFlags, boolean isSystemServer, boolean isZygote, String instructionSet) {
        VM_HOOKS.postForkChild(runtimeFlags, isSystemServer, isZygote, instructionSet);
    }

    static void resetNicePriority() {
        Thread.currentThread().setPriority(5);
    }

    public static void execShell(String command) {
        String[] args = {"/system/bin/sh", "-c", command};
        try {
            Os.execv(args[0], args);
        } catch (ErrnoException e) {
            throw new RuntimeException(e);
        }
    }

    public static void appendQuotedShellArgs(StringBuilder command, String[] args) {
        for (String arg : args) {
            command.append(" '");
            command.append(arg.replace("'", "'\\''"));
            command.append("'");
        }
    }
}
