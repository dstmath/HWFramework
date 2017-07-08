package android.os;

import android.app.AppGlobals;
import android.content.Context;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcelable.Creator;
import android.provider.DocumentsContract.Root;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.TypedProperties;
import dalvik.bytecode.OpcodeInfo;
import dalvik.system.VMDebug;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.apache.harmony.dalvik.ddmc.Chunk;
import org.apache.harmony.dalvik.ddmc.ChunkHandler;
import org.apache.harmony.dalvik.ddmc.DdmServer;

public final class Debug {
    private static final String DEFAULT_TRACE_BODY = "dmtrace";
    private static final String DEFAULT_TRACE_EXTENSION = ".trace";
    public static final int MEMINFO_BUFFERS = 2;
    public static final int MEMINFO_CACHED = 3;
    public static final int MEMINFO_COUNT = 14;
    public static final int MEMINFO_FREE = 1;
    public static final int MEMINFO_KERNEL_STACK = 12;
    public static final int MEMINFO_MAPPED = 9;
    public static final int MEMINFO_MEM_AVAILABLE = 13;
    public static final int MEMINFO_PAGE_TABLES = 11;
    public static final int MEMINFO_SHMEM = 4;
    public static final int MEMINFO_SLAB = 5;
    public static final int MEMINFO_SWAP_FREE = 7;
    public static final int MEMINFO_SWAP_TOTAL = 6;
    public static final int MEMINFO_TOTAL = 0;
    public static final int MEMINFO_VM_ALLOC_USED = 10;
    public static final int MEMINFO_ZRAM_TOTAL = 8;
    private static final int MIN_DEBUGGER_IDLE = 1300;
    public static final int SHOW_CLASSLOADER = 2;
    public static final int SHOW_FULL_DETAIL = 1;
    public static final int SHOW_INITIALIZED = 4;
    private static final int SPIN_DELAY = 200;
    private static final String SYSFS_QEMU_TRACE_STATE = "/sys/qemu_trace/state";
    private static final String TAG = "Debug";
    @Deprecated
    public static final int TRACE_COUNT_ALLOCS = 1;
    private static final TypedProperties debugProperties = null;
    private static volatile boolean mWaiting;

    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DebugProperty {
    }

    @Deprecated
    public static class InstructionCount {
        private static final int NUM_INSTR = 0;
        private int[] mCounts;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.Debug.InstructionCount.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.Debug.InstructionCount.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.Debug.InstructionCount.<clinit>():void");
        }

        public InstructionCount() {
            this.mCounts = new int[NUM_INSTR];
        }

        public boolean resetAndStart() {
            try {
                VMDebug.startInstructionCounting();
                VMDebug.resetInstructionCount();
                return true;
            } catch (UnsupportedOperationException e) {
                return false;
            }
        }

        public boolean collect() {
            try {
                VMDebug.stopInstructionCounting();
                VMDebug.getInstructionCount(this.mCounts);
                return true;
            } catch (UnsupportedOperationException e) {
                return false;
            }
        }

        public int globalTotal() {
            int count = Debug.MEMINFO_TOTAL;
            for (int i = Debug.MEMINFO_TOTAL; i < NUM_INSTR; i += Debug.TRACE_COUNT_ALLOCS) {
                count += this.mCounts[i];
            }
            return count;
        }

        public int globalMethodInvocations() {
            int count = Debug.MEMINFO_TOTAL;
            for (int i = Debug.MEMINFO_TOTAL; i < NUM_INSTR; i += Debug.TRACE_COUNT_ALLOCS) {
                if (OpcodeInfo.isInvoke(i)) {
                    count += this.mCounts[i];
                }
            }
            return count;
        }
    }

    public static class MemoryInfo implements Parcelable {
        public static final Creator<MemoryInfo> CREATOR = null;
        public static final int HEAP_DALVIK = 1;
        public static final int HEAP_NATIVE = 2;
        public static final int HEAP_UNKNOWN = 0;
        public static final int NUM_CATEGORIES = 8;
        public static final int NUM_DVK_STATS = 8;
        public static final int NUM_OTHER_STATS = 17;
        public static final int OTHER_APK = 8;
        public static final int OTHER_ART = 12;
        public static final int OTHER_ASHMEM = 3;
        public static final int OTHER_CURSOR = 2;
        public static final int OTHER_DALVIK_ACCOUNTING = 20;
        public static final int OTHER_DALVIK_CODE_CACHE = 21;
        public static final int OTHER_DALVIK_INDIRECT_REFERENCE_TABLE = 24;
        public static final int OTHER_DALVIK_LARGE = 18;
        public static final int OTHER_DALVIK_LINEARALLOC = 19;
        public static final int OTHER_DALVIK_NON_MOVING = 23;
        public static final int OTHER_DALVIK_NORMAL = 17;
        public static final int OTHER_DALVIK_OTHER = 0;
        public static final int OTHER_DALVIK_ZYGOTE = 22;
        public static final int OTHER_DEX = 10;
        public static final int OTHER_GL = 15;
        public static final int OTHER_GL_DEV = 4;
        public static final int OTHER_GRAPHICS = 14;
        public static final int OTHER_JAR = 7;
        public static final int OTHER_OAT = 11;
        public static final int OTHER_OTHER_MEMTRACK = 16;
        public static final int OTHER_SO = 6;
        public static final int OTHER_STACK = 1;
        public static final int OTHER_TTF = 9;
        public static final int OTHER_UNKNOWN_DEV = 5;
        public static final int OTHER_UNKNOWN_MAP = 13;
        public static final int offsetPrivateClean = 4;
        public static final int offsetPrivateDirty = 2;
        public static final int offsetPss = 0;
        public static final int offsetSharedClean = 5;
        public static final int offsetSharedDirty = 3;
        public static final int offsetSwappablePss = 1;
        public static final int offsetSwappedOut = 6;
        public static final int offsetSwappedOutPss = 7;
        public int dalvikPrivateClean;
        public int dalvikPrivateDirty;
        public int dalvikPss;
        public int dalvikSharedClean;
        public int dalvikSharedDirty;
        public int dalvikSwappablePss;
        public int dalvikSwappedOut;
        public int dalvikSwappedOutPss;
        public boolean hasSwappedOutPss;
        public int nativePrivateClean;
        public int nativePrivateDirty;
        public int nativePss;
        public int nativeSharedClean;
        public int nativeSharedDirty;
        public int nativeSwappablePss;
        public int nativeSwappedOut;
        public int nativeSwappedOutPss;
        public int otherPrivateClean;
        public int otherPrivateDirty;
        public int otherPss;
        public int otherSharedClean;
        public int otherSharedDirty;
        private int[] otherStats;
        public int otherSwappablePss;
        public int otherSwappedOut;
        public int otherSwappedOutPss;

        /* renamed from: android.os.Debug.MemoryInfo.1 */
        static class AnonymousClass1 implements Creator<MemoryInfo> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m28createFromParcel(Parcel source) {
                return createFromParcel(source);
            }

            public MemoryInfo createFromParcel(Parcel source) {
                return new MemoryInfo(source, null);
            }

            public /* bridge */ /* synthetic */ Object[] m29newArray(int size) {
                return newArray(size);
            }

            public MemoryInfo[] newArray(int size) {
                return new MemoryInfo[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.Debug.MemoryInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.Debug.MemoryInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.Debug.MemoryInfo.<clinit>():void");
        }

        /* synthetic */ MemoryInfo(Parcel source, MemoryInfo memoryInfo) {
            this(source);
        }

        public MemoryInfo() {
            this.otherStats = new int[Debug.SPIN_DELAY];
        }

        public int getTotalPss() {
            return ((this.dalvikPss + this.nativePss) + this.otherPss) + getTotalSwappedOutPss();
        }

        public int getTotalUss() {
            return ((((this.dalvikPrivateClean + this.dalvikPrivateDirty) + this.nativePrivateClean) + this.nativePrivateDirty) + this.otherPrivateClean) + this.otherPrivateDirty;
        }

        public int getTotalSwappablePss() {
            return (this.dalvikSwappablePss + this.nativeSwappablePss) + this.otherSwappablePss;
        }

        public int getTotalPrivateDirty() {
            return (this.dalvikPrivateDirty + this.nativePrivateDirty) + this.otherPrivateDirty;
        }

        public int getTotalSharedDirty() {
            return (this.dalvikSharedDirty + this.nativeSharedDirty) + this.otherSharedDirty;
        }

        public int getTotalPrivateClean() {
            return (this.dalvikPrivateClean + this.nativePrivateClean) + this.otherPrivateClean;
        }

        public int getTotalSharedClean() {
            return (this.dalvikSharedClean + this.nativeSharedClean) + this.otherSharedClean;
        }

        public int getTotalSwappedOut() {
            return (this.dalvikSwappedOut + this.nativeSwappedOut) + this.otherSwappedOut;
        }

        public int getTotalSwappedOutPss() {
            return (this.dalvikSwappedOutPss + this.nativeSwappedOutPss) + this.otherSwappedOutPss;
        }

        public int getOtherPss(int which) {
            return this.otherStats[(which * OTHER_APK) + offsetPss];
        }

        public int getOtherSwappablePss(int which) {
            return this.otherStats[(which * OTHER_APK) + offsetSwappablePss];
        }

        public int getOtherPrivateDirty(int which) {
            return this.otherStats[(which * OTHER_APK) + offsetPrivateDirty];
        }

        public int getOtherSharedDirty(int which) {
            return this.otherStats[(which * OTHER_APK) + offsetSharedDirty];
        }

        public int getOtherPrivateClean(int which) {
            return this.otherStats[(which * OTHER_APK) + offsetPrivateClean];
        }

        public int getOtherPrivate(int which) {
            return getOtherPrivateClean(which) + getOtherPrivateDirty(which);
        }

        public int getOtherSharedClean(int which) {
            return this.otherStats[(which * OTHER_APK) + offsetSharedClean];
        }

        public int getOtherSwappedOut(int which) {
            return this.otherStats[(which * OTHER_APK) + offsetSwappedOut];
        }

        public int getOtherSwappedOutPss(int which) {
            return this.otherStats[(which * OTHER_APK) + offsetSwappedOutPss];
        }

        public static String getOtherLabel(int which) {
            switch (which) {
                case offsetPss /*0*/:
                    return "Dalvik Other";
                case offsetSwappablePss /*1*/:
                    return "Stack";
                case offsetPrivateDirty /*2*/:
                    return "Cursor";
                case offsetSharedDirty /*3*/:
                    return "Ashmem";
                case offsetPrivateClean /*4*/:
                    return "Gfx dev";
                case offsetSharedClean /*5*/:
                    return "Other dev";
                case offsetSwappedOut /*6*/:
                    return ".so mmap";
                case offsetSwappedOutPss /*7*/:
                    return ".jar mmap";
                case OTHER_APK /*8*/:
                    return ".apk mmap";
                case OTHER_TTF /*9*/:
                    return ".ttf mmap";
                case OTHER_DEX /*10*/:
                    return ".dex mmap";
                case OTHER_OAT /*11*/:
                    return ".oat mmap";
                case OTHER_ART /*12*/:
                    return ".art mmap";
                case OTHER_UNKNOWN_MAP /*13*/:
                    return "Other mmap";
                case OTHER_GRAPHICS /*14*/:
                    return "EGL mtrack";
                case OTHER_GL /*15*/:
                    return "GL mtrack";
                case OTHER_OTHER_MEMTRACK /*16*/:
                    return "Other mtrack";
                case OTHER_DALVIK_NORMAL /*17*/:
                    return ".Heap";
                case OTHER_DALVIK_LARGE /*18*/:
                    return ".LOS";
                case OTHER_DALVIK_LINEARALLOC /*19*/:
                    return ".LinearAlloc";
                case OTHER_DALVIK_ACCOUNTING /*20*/:
                    return ".GC";
                case OTHER_DALVIK_CODE_CACHE /*21*/:
                    return ".JITCache";
                case OTHER_DALVIK_ZYGOTE /*22*/:
                    return ".Zygote";
                case OTHER_DALVIK_NON_MOVING /*23*/:
                    return ".NonMoving";
                case OTHER_DALVIK_INDIRECT_REFERENCE_TABLE /*24*/:
                    return ".IndirectRef";
                default:
                    return "????";
            }
        }

        public String getMemoryStat(String statName) {
            if (statName.equals("summary.java-heap")) {
                return Integer.toString(getSummaryJavaHeap());
            }
            if (statName.equals("summary.native-heap")) {
                return Integer.toString(getSummaryNativeHeap());
            }
            if (statName.equals("summary.code")) {
                return Integer.toString(getSummaryCode());
            }
            if (statName.equals("summary.stack")) {
                return Integer.toString(getSummaryStack());
            }
            if (statName.equals("summary.graphics")) {
                return Integer.toString(getSummaryGraphics());
            }
            if (statName.equals("summary.private-other")) {
                return Integer.toString(getSummaryPrivateOther());
            }
            if (statName.equals("summary.system")) {
                return Integer.toString(getSummarySystem());
            }
            if (statName.equals("summary.total-pss")) {
                return Integer.toString(getSummaryTotalPss());
            }
            if (statName.equals("summary.total-swap")) {
                return Integer.toString(getSummaryTotalSwap());
            }
            return null;
        }

        public Map<String, String> getMemoryStats() {
            Map<String, String> stats = new HashMap();
            stats.put("summary.java-heap", Integer.toString(getSummaryJavaHeap()));
            stats.put("summary.native-heap", Integer.toString(getSummaryNativeHeap()));
            stats.put("summary.code", Integer.toString(getSummaryCode()));
            stats.put("summary.stack", Integer.toString(getSummaryStack()));
            stats.put("summary.graphics", Integer.toString(getSummaryGraphics()));
            stats.put("summary.private-other", Integer.toString(getSummaryPrivateOther()));
            stats.put("summary.system", Integer.toString(getSummarySystem()));
            stats.put("summary.total-pss", Integer.toString(getSummaryTotalPss()));
            stats.put("summary.total-swap", Integer.toString(getSummaryTotalSwap()));
            return stats;
        }

        public int getSummaryJavaHeap() {
            return this.dalvikPrivateDirty + getOtherPrivate(OTHER_ART);
        }

        public int getSummaryNativeHeap() {
            return this.nativePrivateDirty;
        }

        public int getSummaryCode() {
            return ((((getOtherPrivate(offsetSwappedOut) + getOtherPrivate(offsetSwappedOutPss)) + getOtherPrivate(OTHER_APK)) + getOtherPrivate(OTHER_TTF)) + getOtherPrivate(OTHER_DEX)) + getOtherPrivate(OTHER_OAT);
        }

        public int getSummaryStack() {
            return getOtherPrivateDirty(offsetSwappablePss);
        }

        public int getSummaryGraphics() {
            return (getOtherPrivate(offsetPrivateClean) + getOtherPrivate(OTHER_GRAPHICS)) + getOtherPrivate(OTHER_GL);
        }

        public int getSummaryPrivateOther() {
            return (((((getTotalPrivateClean() + getTotalPrivateDirty()) - getSummaryJavaHeap()) - getSummaryNativeHeap()) - getSummaryCode()) - getSummaryStack()) - getSummaryGraphics();
        }

        public int getSummarySystem() {
            return (getTotalPss() - getTotalPrivateClean()) - getTotalPrivateDirty();
        }

        public int getSummaryTotalPss() {
            return getTotalPss();
        }

        public int getSummaryTotalSwap() {
            return getTotalSwappedOut();
        }

        public int getSummaryTotalSwapPss() {
            return getTotalSwappedOutPss();
        }

        public boolean hasSwappedOutPss() {
            return this.hasSwappedOutPss;
        }

        public int describeContents() {
            return offsetPss;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.dalvikPss);
            dest.writeInt(this.dalvikSwappablePss);
            dest.writeInt(this.dalvikPrivateDirty);
            dest.writeInt(this.dalvikSharedDirty);
            dest.writeInt(this.dalvikPrivateClean);
            dest.writeInt(this.dalvikSharedClean);
            dest.writeInt(this.dalvikSwappedOut);
            dest.writeInt(this.nativePss);
            dest.writeInt(this.nativeSwappablePss);
            dest.writeInt(this.nativePrivateDirty);
            dest.writeInt(this.nativeSharedDirty);
            dest.writeInt(this.nativePrivateClean);
            dest.writeInt(this.nativeSharedClean);
            dest.writeInt(this.nativeSwappedOut);
            dest.writeInt(this.otherPss);
            dest.writeInt(this.otherSwappablePss);
            dest.writeInt(this.otherPrivateDirty);
            dest.writeInt(this.otherSharedDirty);
            dest.writeInt(this.otherPrivateClean);
            dest.writeInt(this.otherSharedClean);
            dest.writeInt(this.otherSwappedOut);
            dest.writeInt(this.hasSwappedOutPss ? offsetSwappablePss : offsetPss);
            dest.writeInt(this.otherSwappedOutPss);
            dest.writeIntArray(this.otherStats);
        }

        public void readFromParcel(Parcel source) {
            boolean z = false;
            this.dalvikPss = source.readInt();
            this.dalvikSwappablePss = source.readInt();
            this.dalvikPrivateDirty = source.readInt();
            this.dalvikSharedDirty = source.readInt();
            this.dalvikPrivateClean = source.readInt();
            this.dalvikSharedClean = source.readInt();
            this.dalvikSwappedOut = source.readInt();
            this.nativePss = source.readInt();
            this.nativeSwappablePss = source.readInt();
            this.nativePrivateDirty = source.readInt();
            this.nativeSharedDirty = source.readInt();
            this.nativePrivateClean = source.readInt();
            this.nativeSharedClean = source.readInt();
            this.nativeSwappedOut = source.readInt();
            this.otherPss = source.readInt();
            this.otherSwappablePss = source.readInt();
            this.otherPrivateDirty = source.readInt();
            this.otherSharedDirty = source.readInt();
            this.otherPrivateClean = source.readInt();
            this.otherSharedClean = source.readInt();
            this.otherSwappedOut = source.readInt();
            if (source.readInt() != 0) {
                z = true;
            }
            this.hasSwappedOutPss = z;
            this.otherSwappedOutPss = source.readInt();
            this.otherStats = source.createIntArray();
        }

        private MemoryInfo(Parcel source) {
            this.otherStats = new int[Debug.SPIN_DELAY];
            readFromParcel(source);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.Debug.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.Debug.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.os.Debug.<clinit>():void");
    }

    public static native void dumpNativeBacktraceToFile(int i, String str);

    public static native void dumpNativeHeap(FileDescriptor fileDescriptor);

    public static final native int getBinderDeathObjectCount();

    public static final native int getBinderLocalObjectCount();

    public static final native int getBinderProxyObjectCount();

    public static native int getBinderReceivedTransactions();

    public static native int getBinderSentTransactions();

    public static native void getMemInfo(long[] jArr);

    public static native void getMemoryInfo(int i, MemoryInfo memoryInfo);

    public static native void getMemoryInfo(MemoryInfo memoryInfo);

    public static native long getNativeHeapAllocatedSize();

    public static native long getNativeHeapFreeSize();

    public static native long getNativeHeapSize();

    public static native long getPss();

    public static native long getPss(int i, long[] jArr, long[] jArr2);

    public static final native float getSurfaceFlingerFrameRate();

    public static native String getUnreachableMemory(int i, boolean z);

    private Debug() {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void waitForDebugger() {
        if (VMDebug.isDebuggingEnabled() && !isDebuggerConnected()) {
            long delta;
            System.out.println("Sending WAIT chunk");
            byte[] data = new byte[TRACE_COUNT_ALLOCS];
            data[MEMINFO_TOTAL] = (byte) 0;
            DdmServer.sendChunk(new Chunk(ChunkHandler.type("WAIT"), data, MEMINFO_TOTAL, TRACE_COUNT_ALLOCS));
            mWaiting = true;
            while (!isDebuggerConnected()) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                }
            }
            mWaiting = false;
            System.out.println("Debugger has connected");
            while (true) {
                delta = VMDebug.lastDebuggerActivity();
                if (delta >= 0) {
                    if (delta >= 1300) {
                        break;
                    }
                    System.out.println("waiting for debugger to settle...");
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e2) {
                    }
                } else {
                    break;
                }
            }
            System.out.println("debugger has settled (" + delta + ")");
        }
    }

    public static boolean waitingForDebugger() {
        return mWaiting;
    }

    public static boolean isDebuggerConnected() {
        return VMDebug.isDebuggerConnected();
    }

    public static String[] getVmFeatureList() {
        return VMDebug.getVmFeatureList();
    }

    @Deprecated
    public static void changeDebugPort(int port) {
    }

    public static void startNativeTracing() {
        Throwable th;
        PrintWriter printWriter = null;
        try {
            PrintWriter outStream = new FastPrintWriter(new FileOutputStream(SYSFS_QEMU_TRACE_STATE));
            try {
                outStream.println(WifiEnterpriseConfig.ENGINE_ENABLE);
                if (outStream != null) {
                    outStream.close();
                }
                printWriter = outStream;
            } catch (Exception e) {
                printWriter = outStream;
                if (printWriter != null) {
                    printWriter.close();
                }
                VMDebug.startEmulatorTracing();
            } catch (Throwable th2) {
                th = th2;
                printWriter = outStream;
                if (printWriter != null) {
                    printWriter.close();
                }
                throw th;
            }
        } catch (Exception e2) {
            if (printWriter != null) {
                printWriter.close();
            }
            VMDebug.startEmulatorTracing();
        } catch (Throwable th3) {
            th = th3;
            if (printWriter != null) {
                printWriter.close();
            }
            throw th;
        }
        VMDebug.startEmulatorTracing();
    }

    public static void stopNativeTracing() {
        Throwable th;
        VMDebug.stopEmulatorTracing();
        PrintWriter printWriter = null;
        try {
            PrintWriter outStream = new FastPrintWriter(new FileOutputStream(SYSFS_QEMU_TRACE_STATE));
            try {
                outStream.println(WifiEnterpriseConfig.ENGINE_DISABLE);
                if (outStream != null) {
                    outStream.close();
                }
                printWriter = outStream;
            } catch (Exception e) {
                printWriter = outStream;
                if (printWriter != null) {
                    printWriter.close();
                }
            } catch (Throwable th2) {
                th = th2;
                printWriter = outStream;
                if (printWriter != null) {
                    printWriter.close();
                }
                throw th;
            }
        } catch (Exception e2) {
            if (printWriter != null) {
                printWriter.close();
            }
        } catch (Throwable th3) {
            th = th3;
            if (printWriter != null) {
                printWriter.close();
            }
            throw th;
        }
    }

    public static void enableEmulatorTraceOutput() {
        VMDebug.startEmulatorTracing();
    }

    public static void startMethodTracing() {
        VMDebug.startMethodTracing(fixTracePath(null), MEMINFO_TOTAL, MEMINFO_TOTAL, false, MEMINFO_TOTAL);
    }

    public static void startMethodTracing(String tracePath) {
        startMethodTracing(tracePath, MEMINFO_TOTAL, MEMINFO_TOTAL);
    }

    public static void startMethodTracing(String tracePath, int bufferSize) {
        startMethodTracing(tracePath, bufferSize, MEMINFO_TOTAL);
    }

    public static void startMethodTracing(String tracePath, int bufferSize, int flags) {
        VMDebug.startMethodTracing(fixTracePath(tracePath), bufferSize, flags, false, MEMINFO_TOTAL);
    }

    public static void startMethodTracingSampling(String tracePath, int bufferSize, int intervalUs) {
        VMDebug.startMethodTracing(fixTracePath(tracePath), bufferSize, MEMINFO_TOTAL, true, intervalUs);
    }

    private static String fixTracePath(String tracePath) {
        if (tracePath == null || tracePath.charAt(MEMINFO_TOTAL) != '/') {
            File dir;
            Context context = AppGlobals.getInitialApplication();
            if (context != null) {
                dir = context.getExternalFilesDir(null);
            } else {
                dir = Environment.getExternalStorageDirectory();
            }
            if (tracePath == null) {
                tracePath = new File(dir, DEFAULT_TRACE_BODY).getAbsolutePath();
            } else {
                tracePath = new File(dir, tracePath).getAbsolutePath();
            }
        }
        if (tracePath.endsWith(DEFAULT_TRACE_EXTENSION)) {
            return tracePath;
        }
        return tracePath + DEFAULT_TRACE_EXTENSION;
    }

    public static void startMethodTracing(String traceName, FileDescriptor fd, int bufferSize, int flags) {
        VMDebug.startMethodTracing(traceName, fd, bufferSize, flags, false, MEMINFO_TOTAL);
    }

    public static void startMethodTracingDdms(int bufferSize, int flags, boolean samplingEnabled, int intervalUs) {
        VMDebug.startMethodTracingDdms(bufferSize, flags, samplingEnabled, intervalUs);
    }

    public static int getMethodTracingMode() {
        return VMDebug.getMethodTracingMode();
    }

    public static void stopMethodTracing() {
        VMDebug.stopMethodTracing();
    }

    public static long threadCpuTimeNanos() {
        return VMDebug.threadCpuTimeNanos();
    }

    @Deprecated
    public static void startAllocCounting() {
        VMDebug.startAllocCounting();
    }

    @Deprecated
    public static void stopAllocCounting() {
        VMDebug.stopAllocCounting();
    }

    @Deprecated
    public static int getGlobalAllocCount() {
        return VMDebug.getAllocCount(TRACE_COUNT_ALLOCS);
    }

    @Deprecated
    public static void resetGlobalAllocCount() {
        VMDebug.resetAllocCount(TRACE_COUNT_ALLOCS);
    }

    @Deprecated
    public static int getGlobalAllocSize() {
        return VMDebug.getAllocCount(SHOW_CLASSLOADER);
    }

    @Deprecated
    public static void resetGlobalAllocSize() {
        VMDebug.resetAllocCount(SHOW_CLASSLOADER);
    }

    @Deprecated
    public static int getGlobalFreedCount() {
        return VMDebug.getAllocCount(SHOW_INITIALIZED);
    }

    @Deprecated
    public static void resetGlobalFreedCount() {
        VMDebug.resetAllocCount(SHOW_INITIALIZED);
    }

    @Deprecated
    public static int getGlobalFreedSize() {
        return VMDebug.getAllocCount(MEMINFO_ZRAM_TOTAL);
    }

    @Deprecated
    public static void resetGlobalFreedSize() {
        VMDebug.resetAllocCount(MEMINFO_ZRAM_TOTAL);
    }

    @Deprecated
    public static int getGlobalGcInvocationCount() {
        return VMDebug.getAllocCount(16);
    }

    @Deprecated
    public static void resetGlobalGcInvocationCount() {
        VMDebug.resetAllocCount(16);
    }

    @Deprecated
    public static int getGlobalClassInitCount() {
        return VMDebug.getAllocCount(32);
    }

    @Deprecated
    public static void resetGlobalClassInitCount() {
        VMDebug.resetAllocCount(32);
    }

    @Deprecated
    public static int getGlobalClassInitTime() {
        return VMDebug.getAllocCount(64);
    }

    @Deprecated
    public static void resetGlobalClassInitTime() {
        VMDebug.resetAllocCount(64);
    }

    @Deprecated
    public static int getGlobalExternalAllocCount() {
        return MEMINFO_TOTAL;
    }

    @Deprecated
    public static void resetGlobalExternalAllocSize() {
    }

    @Deprecated
    public static void resetGlobalExternalAllocCount() {
    }

    @Deprecated
    public static int getGlobalExternalAllocSize() {
        return MEMINFO_TOTAL;
    }

    @Deprecated
    public static int getGlobalExternalFreedCount() {
        return MEMINFO_TOTAL;
    }

    @Deprecated
    public static void resetGlobalExternalFreedCount() {
    }

    @Deprecated
    public static int getGlobalExternalFreedSize() {
        return MEMINFO_TOTAL;
    }

    @Deprecated
    public static void resetGlobalExternalFreedSize() {
    }

    @Deprecated
    public static int getThreadAllocCount() {
        return VMDebug.getAllocCount(Root.FLAG_EMPTY);
    }

    @Deprecated
    public static void resetThreadAllocCount() {
        VMDebug.resetAllocCount(Root.FLAG_EMPTY);
    }

    @Deprecated
    public static int getThreadAllocSize() {
        return VMDebug.getAllocCount(Root.FLAG_ADVANCED);
    }

    @Deprecated
    public static void resetThreadAllocSize() {
        VMDebug.resetAllocCount(Root.FLAG_ADVANCED);
    }

    @Deprecated
    public static int getThreadExternalAllocCount() {
        return MEMINFO_TOTAL;
    }

    @Deprecated
    public static void resetThreadExternalAllocCount() {
    }

    @Deprecated
    public static int getThreadExternalAllocSize() {
        return MEMINFO_TOTAL;
    }

    @Deprecated
    public static void resetThreadExternalAllocSize() {
    }

    @Deprecated
    public static int getThreadGcInvocationCount() {
        return VMDebug.getAllocCount(Root.FLAG_REMOVABLE_USB);
    }

    @Deprecated
    public static void resetThreadGcInvocationCount() {
        VMDebug.resetAllocCount(Root.FLAG_REMOVABLE_USB);
    }

    @Deprecated
    public static void resetAllCounts() {
        VMDebug.resetAllocCount(-1);
    }

    public static String getRuntimeStat(String statName) {
        return VMDebug.getRuntimeStat(statName);
    }

    public static Map<String, String> getRuntimeStats() {
        return VMDebug.getRuntimeStats();
    }

    @Deprecated
    public static int setAllocationLimit(int limit) {
        return -1;
    }

    @Deprecated
    public static int setGlobalAllocationLimit(int limit) {
        return -1;
    }

    public static void printLoadedClasses(int flags) {
        VMDebug.printLoadedClasses(flags);
    }

    public static int getLoadedClassCount() {
        return VMDebug.getLoadedClassCount();
    }

    public static void dumpHprofData(String fileName) throws IOException {
        VMDebug.dumpHprofData(fileName);
    }

    public static void dumpHprofData(String fileName, FileDescriptor fd) throws IOException {
        VMDebug.dumpHprofData(fileName, fd);
    }

    public static void dumpHprofDataDdms() {
        VMDebug.dumpHprofDataDdms();
    }

    public static long countInstancesOfClass(Class cls) {
        return VMDebug.countInstancesOfClass(cls, true);
    }

    public static final boolean cacheRegisterMap(String classAndMethodDesc) {
        return VMDebug.cacheRegisterMap(classAndMethodDesc);
    }

    public static final void dumpReferenceTables() {
        VMDebug.dumpReferenceTables();
    }

    private static boolean fieldTypeMatches(Field field, Class<?> cl) {
        Class<?> fieldClass = field.getType();
        if (fieldClass == cl) {
            return true;
        }
        try {
            try {
                boolean z;
                if (fieldClass == ((Class) cl.getField("TYPE").get(null))) {
                    z = true;
                } else {
                    z = false;
                }
                return z;
            } catch (IllegalAccessException e) {
                return false;
            }
        } catch (NoSuchFieldException e2) {
            return false;
        }
    }

    private static void modifyFieldIfSet(Field field, TypedProperties properties, String propertyName) {
        if (field.getType() == String.class) {
            int stringInfo = properties.getStringInfo(propertyName);
            switch (stringInfo) {
                case TextToSpeech.STOPPED /*-2*/:
                    throw new IllegalArgumentException("Type of " + propertyName + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + " does not match field type (" + field.getType() + ")");
                case TextToSpeech.LANG_MISSING_DATA /*-1*/:
                    return;
                case MEMINFO_TOTAL /*0*/:
                    try {
                        field.set(null, null);
                        return;
                    } catch (IllegalAccessException ex) {
                        throw new IllegalArgumentException("Cannot set field for " + propertyName, ex);
                    }
                case TRACE_COUNT_ALLOCS /*1*/:
                    break;
                default:
                    throw new IllegalStateException("Unexpected getStringInfo(" + propertyName + ") return value " + stringInfo);
            }
        }
        Object value = properties.get(propertyName);
        if (value != null) {
            if (fieldTypeMatches(field, value.getClass())) {
                try {
                    field.set(null, value);
                } catch (IllegalAccessException ex2) {
                    throw new IllegalArgumentException("Cannot set field for " + propertyName, ex2);
                }
            }
            throw new IllegalArgumentException("Type of " + propertyName + " (" + value.getClass() + ") " + " does not match field type (" + field.getType() + ")");
        }
    }

    public static void setFieldsOn(Class<?> cl) {
        setFieldsOn(cl, false);
    }

    public static void setFieldsOn(Class<?> cl, boolean partial) {
        Log.wtf(TAG, "setFieldsOn(" + (cl == null ? "null" : cl.getName()) + ") called in non-DEBUG build");
    }

    public static boolean dumpService(String name, FileDescriptor fd, String[] args) {
        IBinder service = ServiceManager.getService(name);
        if (service == null) {
            Log.e(TAG, "Can't find service to dump: " + name);
            return false;
        }
        try {
            service.dump(fd, args);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "Can't dump service: " + name, e);
            return false;
        }
    }

    private static String getCaller(StackTraceElement[] callStack, int depth) {
        if (depth + SHOW_INITIALIZED >= callStack.length) {
            return "<bottom of call stack>";
        }
        StackTraceElement caller = callStack[depth + SHOW_INITIALIZED];
        return caller.getClassName() + "." + caller.getMethodName() + ":" + caller.getLineNumber();
    }

    public static String getCallers(int depth) {
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        StringBuffer sb = new StringBuffer();
        for (int i = MEMINFO_TOTAL; i < depth; i += TRACE_COUNT_ALLOCS) {
            sb.append(getCaller(callStack, i)).append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        }
        return sb.toString();
    }

    public static String getCallers(int start, int depth) {
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        StringBuffer sb = new StringBuffer();
        depth += start;
        for (int i = start; i < depth; i += TRACE_COUNT_ALLOCS) {
            sb.append(getCaller(callStack, i)).append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        }
        return sb.toString();
    }

    public static String getCallers(int depth, String linePrefix) {
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        StringBuffer sb = new StringBuffer();
        for (int i = MEMINFO_TOTAL; i < depth; i += TRACE_COUNT_ALLOCS) {
            sb.append(linePrefix).append(getCaller(callStack, i)).append("\n");
        }
        return sb.toString();
    }

    public static String getCaller() {
        return getCaller(Thread.currentThread().getStackTrace(), MEMINFO_TOTAL);
    }
}
