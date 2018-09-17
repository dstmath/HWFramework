package com.android.internal.os;

import android.util.Slog;

public class HwBootAnimationOeminfo {
    private static final boolean DEBUG_NATIVE = false;
    private static final int OEMINFO_BOOTANIM_RINGDEX = 107;
    private static final int OEMINFO_BOOTANIM_RINGMODE = 106;
    private static final int OEMINFO_BOOTANIM_SHUTFLAG = 105;
    private static final int OEMINFO_BOOTANIM_SWITCH = 104;
    private static final String TAG = "HwBootAnimationNative";
    private static final Object mLock = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.os.HwBootAnimationOeminfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.os.HwBootAnimationOeminfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.HwBootAnimationOeminfo.<clinit>():void");
    }

    private static native int nativeGetBootAnimationParam(int i);

    private static native int nativeSetBootAnimationParam(int i, int i2);

    public static int setBootAnimSoundSwitch(int value) {
        try {
            int nativeSetBootAnimationParam;
            synchronized (mLock) {
                nativeSetBootAnimationParam = nativeSetBootAnimationParam(OEMINFO_BOOTANIM_SWITCH, value);
            }
            return nativeSetBootAnimationParam;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "HwBootAnimationNative error >>>>>" + e);
            return -1;
        }
    }

    public static int getBootAnimSoundSwitch() {
        try {
            int nativeGetBootAnimationParam;
            synchronized (mLock) {
                nativeGetBootAnimationParam = nativeGetBootAnimationParam(OEMINFO_BOOTANIM_SWITCH);
            }
            return nativeGetBootAnimationParam;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "HwBootAnimationNative error >>>>>" + e);
            return -1;
        }
    }

    public static int setBootAnimShutFlag(int value) {
        try {
            int nativeSetBootAnimationParam;
            synchronized (mLock) {
                nativeSetBootAnimationParam = nativeSetBootAnimationParam(OEMINFO_BOOTANIM_SHUTFLAG, value);
            }
            return nativeSetBootAnimationParam;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "HwBootAnimationNative error >>>>>" + e);
            return -1;
        }
    }

    public static int getBootAnimShutFlag() {
        try {
            int nativeGetBootAnimationParam;
            synchronized (mLock) {
                nativeGetBootAnimationParam = nativeGetBootAnimationParam(OEMINFO_BOOTANIM_SHUTFLAG);
            }
            return nativeGetBootAnimationParam;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "HwBootAnimationNative error >>>>>" + e);
            return -1;
        }
    }

    public static int setBootAnimRingMode(int value) {
        try {
            int nativeSetBootAnimationParam;
            synchronized (mLock) {
                nativeSetBootAnimationParam = nativeSetBootAnimationParam(OEMINFO_BOOTANIM_RINGMODE, value);
            }
            return nativeSetBootAnimationParam;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "HwBootAnimationNative error >>>>>" + e);
            return -1;
        }
    }

    public static int setBootAnimRing(int value) {
        try {
            int nativeSetBootAnimationParam;
            synchronized (mLock) {
                nativeSetBootAnimationParam = nativeSetBootAnimationParam(OEMINFO_BOOTANIM_RINGDEX, value);
            }
            return nativeSetBootAnimationParam;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "HwBootAnimationNative error >>>>>" + e);
            return -1;
        }
    }
}
