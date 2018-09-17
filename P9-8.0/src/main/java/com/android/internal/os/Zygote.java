package com.android.internal.os;

import android.os.Trace;
import android.system.ErrnoException;
import android.system.Os;
import dalvik.system.ZygoteHooks;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class Zygote {
    public static final int DEBUG_ALWAYS_JIT = 64;
    public static final int DEBUG_ENABLE_ASSERT = 4;
    public static final int DEBUG_ENABLE_CHECKJNI = 2;
    public static final int DEBUG_ENABLE_JDWP = 1;
    public static final int DEBUG_ENABLE_JNI_LOGGING = 16;
    public static final int DEBUG_ENABLE_SAFEMODE = 8;
    public static final int DEBUG_GENERATE_DEBUG_INFO = 32;
    public static final int DEBUG_JAVA_DEBUGGABLE = 256;
    public static final int DEBUG_NATIVE_DEBUGGABLE = 128;
    public static final int MOUNT_EXTERNAL_DEFAULT = 1;
    public static final int MOUNT_EXTERNAL_NONE = 0;
    public static final int MOUNT_EXTERNAL_READ = 2;
    public static final int MOUNT_EXTERNAL_WRITE = 3;
    private static final ZygoteHooks VM_HOOKS = new ZygoteHooks();

    public static class MethodAndArgsCaller extends Exception implements Runnable {
        private final String[] mArgs;
        private final Method mMethod;

        public MethodAndArgsCaller(Method method, String[] args) {
            this.mMethod = method;
            this.mArgs = args;
        }

        public void run() {
            try {
                this.mMethod.invoke(null, new Object[]{this.mArgs});
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch (InvocationTargetException ex2) {
                Throwable cause = ex2.getCause();
                if (cause instanceof RuntimeException) {
                    throw ((RuntimeException) cause);
                } else if (cause instanceof Error) {
                    throw ((Error) cause);
                } else {
                    throw new RuntimeException(ex2);
                }
            }
        }
    }

    public static native int ctrlSockets(int i, int i2);

    protected static native void nativeAllowFileAcrossFork(String str);

    private static native int nativeForkAndSpecialize(int i, int i2, int[] iArr, int i3, int[][] iArr2, int i4, String str, String str2, int[] iArr3, int[] iArr4, String str3, String str4);

    private static native int nativeForkSystemServer(int i, int i2, int[] iArr, int i3, int[][] iArr2, long j, long j2);

    protected static native void nativeUnmountStorageOnInit();

    public static native int procCtrlSockets(int i, int i2, int i3);

    private Zygote() {
    }

    public static int forkAndSpecialize(int uid, int gid, int[] gids, int debugFlags, int[][] rlimits, int mountExternal, String seInfo, String niceName, int[] fdsToClose, int[] fdsToIgnore, String instructionSet, String appDataDir) {
        VM_HOOKS.preFork();
        resetNicePriority();
        int pid = nativeForkAndSpecialize(uid, gid, gids, debugFlags, rlimits, mountExternal, seInfo, niceName, fdsToClose, fdsToIgnore, instructionSet, appDataDir);
        if (pid == 0) {
            Trace.setTracingEnabled(true);
            Trace.traceBegin(64, "PostFork");
        }
        VM_HOOKS.postForkCommon();
        return pid;
    }

    public static int forkSystemServer(int uid, int gid, int[] gids, int debugFlags, int[][] rlimits, long permittedCapabilities, long effectiveCapabilities) {
        VM_HOOKS.preFork();
        resetNicePriority();
        int pid = nativeForkSystemServer(uid, gid, gids, debugFlags, rlimits, permittedCapabilities, effectiveCapabilities);
        if (pid == 0) {
            Trace.setTracingEnabled(true);
        }
        VM_HOOKS.postForkCommon();
        return pid;
    }

    private static void callPostForkChildHooks(int debugFlags, boolean isSystemServer, String instructionSet) {
        VM_HOOKS.postForkChild(debugFlags, isSystemServer, instructionSet);
    }

    static void resetNicePriority() {
        Thread.currentThread().setPriority(5);
    }

    public static void execShell(String command) {
        String[] args = new String[]{"/system/bin/sh", "-c", command};
        try {
            Os.execv(args[0], args);
        } catch (ErrnoException e) {
            throw new RuntimeException(e);
        }
    }

    public static void appendQuotedShellArgs(StringBuilder command, String[] args) {
        for (String arg : args) {
            command.append(" '").append(arg.replace("'", "'\\''")).append("'");
        }
    }
}
