package com.android.internal.os;

import android.os.Trace;
import android.system.ErrnoException;
import android.system.Os;
import dalvik.system.ZygoteHooks;

public final class Zygote {
    public static final int DEBUG_ALWAYS_JIT = 64;
    public static final int DEBUG_ENABLE_ASSERT = 4;
    public static final int DEBUG_ENABLE_CHECKJNI = 2;
    public static final int DEBUG_ENABLE_DEBUGGER = 1;
    public static final int DEBUG_ENABLE_JNI_LOGGING = 16;
    public static final int DEBUG_ENABLE_SAFEMODE = 8;
    public static final int DEBUG_GENERATE_DEBUG_INFO = 32;
    public static final int DEBUG_NATIVE_DEBUGGABLE = 128;
    public static final int MOUNT_EXTERNAL_DEFAULT = 1;
    public static final int MOUNT_EXTERNAL_NONE = 0;
    public static final int MOUNT_EXTERNAL_READ = 2;
    public static final int MOUNT_EXTERNAL_WRITE = 3;
    private static final ZygoteHooks VM_HOOKS = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.os.Zygote.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.os.Zygote.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.Zygote.<clinit>():void");
    }

    public static native int ctrlSockets(int i, int i2);

    private static native int nativeForkAndSpecialize(int i, int i2, int[] iArr, int i3, int[][] iArr2, int i4, String str, String str2, int[] iArr3, String str3, String str4);

    private static native int nativeForkSystemServer(int i, int i2, int[] iArr, int i3, int[][] iArr2, long j, long j2);

    protected static native void nativeUnmountStorageOnInit();

    public static native int procCtrlSockets(int i, int i2, int i3);

    private Zygote() {
    }

    public static int forkAndSpecialize(int uid, int gid, int[] gids, int debugFlags, int[][] rlimits, int mountExternal, String seInfo, String niceName, int[] fdsToClose, String instructionSet, String appDataDir) {
        VM_HOOKS.preFork();
        int pid = nativeForkAndSpecialize(uid, gid, gids, debugFlags, rlimits, mountExternal, seInfo, niceName, fdsToClose, instructionSet, appDataDir);
        if (pid == 0) {
            Trace.setTracingEnabled(true);
            Trace.traceBegin(64, "PostFork");
        }
        VM_HOOKS.postForkCommon();
        return pid;
    }

    public static int forkSystemServer(int uid, int gid, int[] gids, int debugFlags, int[][] rlimits, long permittedCapabilities, long effectiveCapabilities) {
        VM_HOOKS.preFork();
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

    public static void execShell(String command) {
        String[] args = new String[MOUNT_EXTERNAL_WRITE];
        args[MOUNT_EXTERNAL_NONE] = "/system/bin/sh";
        args[MOUNT_EXTERNAL_DEFAULT] = "-c";
        args[MOUNT_EXTERNAL_READ] = command;
        try {
            Os.execv(args[MOUNT_EXTERNAL_NONE], args);
        } catch (ErrnoException e) {
            throw new RuntimeException(e);
        }
    }

    public static void appendQuotedShellArgs(StringBuilder command, String[] args) {
        int length = args.length;
        for (int i = MOUNT_EXTERNAL_NONE; i < length; i += MOUNT_EXTERNAL_DEFAULT) {
            command.append(" '").append(args[i].replace("'", "'\\''")).append("'");
        }
    }
}
