package dalvik.system;

import android.system.ErrnoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import libcore.io.Libcore;

public final class DexFile {
    public static final int DEX2OAT_NEEDED = 1;
    public static final int NO_DEXOPT_NEEDED = 0;
    public static final int PATCHOAT_NEEDED = 2;
    public static final int SELF_PATCHOAT_NEEDED = 3;
    private final CloseGuard guard;
    private Object mCookie;
    private final String mFileName;
    private Object mInternalCookie;

    private class DFEnum implements Enumeration<String> {
        private int mIndex;
        private String[] mNameList;

        DFEnum(DexFile df) {
            this.mIndex = DexFile.NO_DEXOPT_NEEDED;
            this.mNameList = DexFile.getClassNameList(DexFile.this.mCookie);
        }

        public boolean hasMoreElements() {
            return this.mIndex < this.mNameList.length;
        }

        public String nextElement() {
            String[] strArr = this.mNameList;
            int i = this.mIndex;
            this.mIndex = i + DexFile.DEX2OAT_NEEDED;
            return strArr[i];
        }
    }

    private static native boolean closeDexFile(Object obj);

    private static native Class defineClassNative(String str, ClassLoader classLoader, Object obj, DexFile dexFile) throws ClassNotFoundException, NoClassDefFoundError;

    private static native String[] getClassNameList(Object obj);

    public static native String getDexFileStatus(String str, String str2) throws FileNotFoundException;

    public static native int getDexOptNeeded(String str, String str2, String str3, boolean z) throws FileNotFoundException, IOException;

    public static native String getNonProfileGuidedCompilerFilter(String str);

    private static native boolean isBackedByOatFile(Object obj);

    public static native boolean isDexOptNeeded(String str) throws FileNotFoundException, IOException;

    public static native boolean isProfileGuidedCompilerFilter(String str);

    public static native boolean isValidCompilerFilter(String str);

    private static native Object openDexFileNative(String str, String str2, int i, ClassLoader classLoader, Element[] elementArr);

    public DexFile(File file) throws IOException {
        this(file.getPath());
    }

    DexFile(File file, ClassLoader loader, Element[] elements) throws IOException {
        this(file.getPath(), loader, elements);
    }

    public DexFile(String fileName) throws IOException {
        this(fileName, null, null);
    }

    DexFile(String fileName, ClassLoader loader, Element[] elements) throws IOException {
        this.guard = CloseGuard.get();
        this.mCookie = openDexFile(fileName, null, NO_DEXOPT_NEEDED, loader, elements);
        this.mInternalCookie = this.mCookie;
        this.mFileName = fileName;
        this.guard.open("close");
    }

    private DexFile(String sourceName, String outputName, int flags, ClassLoader loader, Element[] elements) throws IOException {
        this.guard = CloseGuard.get();
        if (outputName != null) {
            try {
                String parent = new File(outputName).getParent();
                if (Libcore.os.getuid() != Libcore.os.stat(parent).st_uid) {
                    throw new IllegalArgumentException("Optimized data directory " + parent + " is not owned by the current user. Shared storage cannot protect" + " your application from code injection attacks.");
                }
            } catch (ErrnoException e) {
            }
        }
        this.mCookie = openDexFile(sourceName, outputName, flags, loader, elements);
        this.mFileName = sourceName;
    }

    public static DexFile loadDex(String sourcePathName, String outputPathName, int flags) throws IOException {
        return loadDex(sourcePathName, outputPathName, flags, null, null);
    }

    static DexFile loadDex(String sourcePathName, String outputPathName, int flags, ClassLoader loader, Element[] elements) throws IOException {
        return new DexFile(sourcePathName, outputPathName, flags, loader, elements);
    }

    public String getName() {
        return this.mFileName;
    }

    public String toString() {
        return getName();
    }

    public void close() throws IOException {
        if (this.mInternalCookie != null) {
            if (closeDexFile(this.mInternalCookie)) {
                this.mInternalCookie = null;
            }
            this.guard.close();
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
        Class result = null;
        try {
            result = defineClassNative(name, loader, cookie, dexFile);
        } catch (NoClassDefFoundError e) {
            if (suppressed != null) {
                suppressed.add(e);
            }
        } catch (ClassNotFoundException e2) {
            if (suppressed != null) {
                suppressed.add(e2);
            }
        }
        return result;
    }

    public Enumeration<String> entries() {
        return new DFEnum(this);
    }

    protected void finalize() throws Throwable {
        try {
            if (this.guard != null) {
                this.guard.warnIfOpen();
            }
            if (this.mInternalCookie == null || closeDexFile(this.mInternalCookie)) {
                this.mInternalCookie = null;
                this.mCookie = null;
                return;
            }
            throw new AssertionError("Failed to close dex file in finalizer.");
        } finally {
            super.finalize();
        }
    }

    private static Object openDexFile(String sourceName, String outputName, int flags, ClassLoader loader, Element[] elements) throws IOException {
        String str = null;
        String absolutePath = new File(sourceName).getAbsolutePath();
        if (outputName != null) {
            str = new File(outputName).getAbsolutePath();
        }
        return openDexFileNative(absolutePath, str, flags, loader, elements);
    }

    boolean isBackedByOatFile() {
        return isBackedByOatFile(this.mCookie);
    }
}
