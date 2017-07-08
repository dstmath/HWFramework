package dalvik.system;

import java.lang.ref.FinalizerReference;
import java.util.Map;

public final class VMRuntime {
    private static final Map<String, String> ABI_TO_INSTRUCTION_SET_MAP = null;
    private static final VMRuntime THE_ONE = null;
    private int targetSdkVersion;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: dalvik.system.VMRuntime.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: dalvik.system.VMRuntime.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.VMRuntime.<clinit>():void");
    }

    public static native boolean didPruneDalvikCache();

    public static native String getCurrentInstructionSet();

    public static native boolean isBootClassPathOnDisk(String str);

    private native void nativeSetTargetHeapUtilization(float f);

    public static native void registerAppInfo(String str, String str2, String[] strArr, String str3);

    public static native void registerSensitiveThread();

    private native void setTargetSdkVersionNative(int i);

    public native long addressOf(Object obj);

    public native String bootClassPath();

    public native void clampGrowthLimit();

    public native String classPath();

    public native void clearGrowthLimit();

    public native void concurrentGC();

    public native void disableJitCompilation();

    public native float getTargetHeapUtilization();

    public native boolean is64Bit();

    public native boolean isCheckJniEnabled();

    public native boolean isDebuggerActive();

    public native boolean isNativeDebuggable();

    public native Object newNonMovableArray(Class<?> cls, int i);

    public native Object newUnpaddedArray(Class<?> cls, int i);

    public native void preloadDexCaches();

    public native String[] properties();

    public native void registerNativeAllocation(int i);

    public native void registerNativeFree(int i);

    public native void requestConcurrentGC();

    public native void requestHeapTrim();

    public native void runHeapTasks();

    public native void startHeapTaskProcessor();

    public native void startJitCompilation();

    public native void stopHeapTaskProcessor();

    public native void trimHeap();

    public native void updateProcessState(int i);

    public native String vmInstructionSet();

    public native String vmLibrary();

    public native String vmVersion();

    private VMRuntime() {
    }

    public static VMRuntime getRuntime() {
        return THE_ONE;
    }

    public float setTargetHeapUtilization(float newTarget) {
        if (newTarget <= 0.0f || newTarget >= 1.0f) {
            throw new IllegalArgumentException(newTarget + " out of range (0,1)");
        }
        float oldTarget;
        synchronized (this) {
            oldTarget = getTargetHeapUtilization();
            nativeSetTargetHeapUtilization(newTarget);
        }
        return oldTarget;
    }

    public synchronized void setTargetSdkVersion(int targetSdkVersion) {
        this.targetSdkVersion = targetSdkVersion;
        setTargetSdkVersionNative(this.targetSdkVersion);
    }

    public synchronized int getTargetSdkVersion() {
        return this.targetSdkVersion;
    }

    @Deprecated
    public long getMinimumHeapSize() {
        return 0;
    }

    @Deprecated
    public long setMinimumHeapSize(long size) {
        return 0;
    }

    @Deprecated
    public void gcSoftReferences() {
    }

    @Deprecated
    public void runFinalizationSync() {
        System.runFinalization();
    }

    @Deprecated
    public boolean trackExternalAllocation(long size) {
        return true;
    }

    @Deprecated
    public void trackExternalFree(long size) {
    }

    @Deprecated
    public long getExternalBytesAllocated() {
        return 0;
    }

    public static void runFinalization(long timeout) {
        try {
            FinalizerReference.finalizeAllEnqueued(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static String getInstructionSet(String abi) {
        String instructionSet = (String) ABI_TO_INSTRUCTION_SET_MAP.get(abi);
        if (instructionSet != null) {
            return instructionSet;
        }
        throw new IllegalArgumentException("Unsupported ABI: " + abi);
    }

    public static boolean is64BitInstructionSet(String instructionSet) {
        if ("arm64".equals(instructionSet) || "x86_64".equals(instructionSet)) {
            return true;
        }
        return "mips64".equals(instructionSet);
    }

    public static boolean is64BitAbi(String abi) {
        return is64BitInstructionSet(getInstructionSet(abi));
    }
}
