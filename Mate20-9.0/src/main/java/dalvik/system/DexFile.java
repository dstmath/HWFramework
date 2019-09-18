package dalvik.system;

import android.system.ErrnoException;
import dalvik.system.DexPathList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import libcore.io.Libcore;

@Deprecated
public final class DexFile {
    public static final int DEX2OAT_FOR_BOOT_IMAGE = 2;
    public static final int DEX2OAT_FOR_FILTER = 3;
    public static final int DEX2OAT_FOR_RELOCATION = 4;
    public static final int DEX2OAT_FROM_SCRATCH = 1;
    public static final int NO_DEXOPT_NEEDED = 0;
    /* access modifiers changed from: private */
    public Object mCookie;
    private final String mFileName;
    private Object mInternalCookie;

    private static class DFEnum implements Enumeration<String> {
        private int mIndex = 0;
        private String[] mNameList;

        DFEnum(DexFile df) {
            this.mNameList = DexFile.getClassNameList(df.mCookie);
        }

        public boolean hasMoreElements() {
            return this.mIndex < this.mNameList.length;
        }

        public String nextElement() {
            String[] strArr = this.mNameList;
            int i = this.mIndex;
            this.mIndex = i + 1;
            return strArr[i];
        }
    }

    public static final class OptimizationInfo {
        private final String reason;
        private final String status;

        private OptimizationInfo(String status2, String reason2) {
            this.status = status2;
            this.reason = reason2;
        }

        public String getStatus() {
            return this.status;
        }

        public String getReason() {
            return this.reason;
        }
    }

    private static native boolean closeDexFile(Object obj);

    private static native Object createCookieWithArray(byte[] bArr, int i, int i2);

    private static native Object createCookieWithDirectBuffer(ByteBuffer byteBuffer, int i, int i2);

    private static native Class defineClassNative(String str, ClassLoader classLoader, Object obj, DexFile dexFile) throws ClassNotFoundException, NoClassDefFoundError;

    /* access modifiers changed from: private */
    public static native String[] getClassNameList(Object obj);

    private static native String[] getDexFileOptimizationStatus(String str, String str2) throws FileNotFoundException;

    public static native String[] getDexFileOutputPaths(String str, String str2) throws FileNotFoundException;

    public static native String getDexFileStatus(String str, String str2) throws FileNotFoundException;

    public static native int getDexOptNeeded(String str, String str2, String str3, String str4, boolean z, boolean z2) throws FileNotFoundException, IOException;

    public static native String getNonProfileGuidedCompilerFilter(String str);

    public static native String getSafeModeCompilerFilter(String str);

    private static native long getStaticSizeOfDexFile(Object obj);

    private static native boolean isBackedByOatFile(Object obj);

    public static native boolean isDexOptNeeded(String str) throws FileNotFoundException, IOException;

    public static native boolean isProfileGuidedCompilerFilter(String str);

    public static native boolean isValidCompilerFilter(String str);

    private static native Object openDexFileNative(String str, String str2, int i, ClassLoader classLoader, DexPathList.Element[] elementArr);

    private static native void setTrusted(Object obj);

    @Deprecated
    public DexFile(File file) throws IOException {
        this(file.getPath());
    }

    DexFile(File file, ClassLoader loader, DexPathList.Element[] elements) throws IOException {
        this(file.getPath(), loader, elements);
    }

    @Deprecated
    public DexFile(String fileName) throws IOException {
        this(fileName, (ClassLoader) null, (DexPathList.Element[]) null);
    }

    DexFile(String fileName, ClassLoader loader, DexPathList.Element[] elements) throws IOException {
        this.mCookie = openDexFile(fileName, null, 0, loader, elements);
        this.mInternalCookie = this.mCookie;
        this.mFileName = fileName;
    }

    DexFile(ByteBuffer buf) throws IOException {
        this.mCookie = openInMemoryDexFile(buf);
        this.mInternalCookie = this.mCookie;
        this.mFileName = null;
    }

    private DexFile(String sourceName, String outputName, int flags, ClassLoader loader, DexPathList.Element[] elements) throws IOException {
        if (outputName != null) {
            try {
                String parent = new File(outputName).getParent();
                if (Libcore.os.getuid() != Libcore.os.stat(parent).st_uid) {
                    throw new IllegalArgumentException("Optimized data directory " + parent + " is not owned by the current user. Shared storage cannot protect your application from code injection attacks.");
                }
            } catch (ErrnoException e) {
            }
        }
        this.mCookie = openDexFile(sourceName, outputName, flags, loader, elements);
        this.mInternalCookie = this.mCookie;
        this.mFileName = sourceName;
    }

    @Deprecated
    public static DexFile loadDex(String sourcePathName, String outputPathName, int flags) throws IOException {
        return loadDex(sourcePathName, outputPathName, flags, null, null);
    }

    static DexFile loadDex(String sourcePathName, String outputPathName, int flags, ClassLoader loader, DexPathList.Element[] elements) throws IOException {
        DexFile dexFile = new DexFile(sourcePathName, outputPathName, flags, loader, elements);
        return dexFile;
    }

    public String getName() {
        return this.mFileName;
    }

    public String toString() {
        if (this.mFileName != null) {
            return getName();
        }
        return "InMemoryDexFile[cookie=" + Arrays.toString((long[]) this.mCookie) + "]";
    }

    public void close() throws IOException {
        if (this.mInternalCookie != null) {
            if (closeDexFile(this.mInternalCookie)) {
                this.mInternalCookie = null;
            }
            this.mCookie = null;
        }
    }

    public Class loadClass(String name, ClassLoader loader) {
        return loadClassBinaryName(name.replace('.', '/'), loader, null);
    }

    public Class loadClassBinaryName(String name, ClassLoader loader, List<Throwable> suppressed) {
        return defineClass(name, loader, this.mCookie, this, suppressed);
    }

    private static Class defineClass(String name, ClassLoader loader, Object cookie, DexFile dexFile, List<Throwable> suppressed) {
        try {
            return defineClassNative(name, loader, cookie, dexFile);
        } catch (NoClassDefFoundError e) {
            if (suppressed == null) {
                return null;
            }
            suppressed.add(e);
            return null;
        } catch (ClassNotFoundException e2) {
            if (suppressed == null) {
                return null;
            }
            suppressed.add(e2);
            return null;
        }
    }

    public Enumeration<String> entries() {
        return new DFEnum(this);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mInternalCookie != null) {
                if (!closeDexFile(this.mInternalCookie)) {
                    throw new AssertionError("Failed to close dex file in finalizer.");
                }
            }
            this.mInternalCookie = null;
            this.mCookie = null;
        } finally {
            super.finalize();
        }
    }

    private static Object openDexFile(String sourceName, String outputName, int flags, ClassLoader loader, DexPathList.Element[] elements) throws IOException {
        String str;
        String absolutePath = new File(sourceName).getAbsolutePath();
        if (outputName == null) {
            str = null;
        } else {
            str = new File(outputName).getAbsolutePath();
        }
        return openDexFileNative(absolutePath, str, flags, loader, elements);
    }

    private static Object openInMemoryDexFile(ByteBuffer buf) throws IOException {
        if (buf.isDirect()) {
            return createCookieWithDirectBuffer(buf, buf.position(), buf.limit());
        }
        return createCookieWithArray(buf.array(), buf.position(), buf.limit());
    }

    /* access modifiers changed from: package-private */
    public boolean isBackedByOatFile() {
        return isBackedByOatFile(this.mCookie);
    }

    /* access modifiers changed from: package-private */
    public void setTrusted() {
        setTrusted(this.mCookie);
    }

    public static int getDexOptNeeded(String fileName, String instructionSet, String compilerFilter, boolean newProfile, boolean downgrade) throws FileNotFoundException, IOException {
        return getDexOptNeeded(fileName, instructionSet, compilerFilter, null, newProfile, downgrade);
    }

    public static OptimizationInfo getDexFileOptimizationInfo(String fileName, String instructionSet) throws FileNotFoundException {
        String[] status = getDexFileOptimizationStatus(fileName, instructionSet);
        return new OptimizationInfo(status[0], status[1]);
    }

    public long getStaticSizeOfDexFile() {
        return getStaticSizeOfDexFile(this.mCookie);
    }
}
