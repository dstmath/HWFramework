package dalvik.system;

import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class BaseDexClassLoader extends ClassLoader {
    private static volatile Reporter reporter = null;
    private final DexPathList pathList;

    public interface Reporter {
        void report(List<String> list);
    }

    public BaseDexClassLoader(String dexPath, File optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(parent);
        this.pathList = new DexPathList(this, dexPath, librarySearchPath, null);
        if (reporter != null) {
            reporter.report(this.pathList.getDexPaths());
        }
    }

    public BaseDexClassLoader(ByteBuffer[] dexFiles, ClassLoader parent) {
        super(parent);
        this.pathList = new DexPathList(this, dexFiles);
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        List<Throwable> suppressedExceptions = new ArrayList();
        Class c = this.pathList.findClass(name, suppressedExceptions);
        if (c != null) {
            return c;
        }
        ClassNotFoundException cnfe = new ClassNotFoundException("Didn't find class \"" + name + "\" on path: " + this.pathList);
        for (Throwable t : suppressedExceptions) {
            cnfe.addSuppressed(t);
        }
        throw cnfe;
    }

    public void addDexPath(String dexPath) {
        this.pathList.addDexPath(dexPath, null);
    }

    protected URL findResource(String name) {
        return this.pathList.findResource(name);
    }

    protected Enumeration<URL> findResources(String name) {
        return this.pathList.findResources(name);
    }

    public String findLibrary(String name) {
        return this.pathList.findLibrary(name);
    }

    /* JADX WARNING: Missing block: B:10:0x002c, code:
            return r9;
     */
    /* JADX WARNING: Missing block: B:12:0x002e, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected synchronized Package getPackage(String name) {
        if (name != null) {
            if ((name.isEmpty() ^ 1) != 0) {
                Package pack = super.getPackage(name);
                if (pack == null) {
                    pack = definePackage(name, "Unknown", "0.0", "Unknown", "Unknown", "0.0", "Unknown", null);
                }
            }
        }
    }

    public String getLdLibraryPath() {
        StringBuilder result = new StringBuilder();
        for (File directory : this.pathList.getNativeLibraryDirectories()) {
            if (result.length() > 0) {
                result.append(':');
            }
            result.append(directory);
        }
        return result.toString();
    }

    public String toString() {
        return getClass().getName() + "[" + this.pathList + "]";
    }

    public static void setReporter(Reporter newReporter) {
        reporter = newReporter;
    }

    public static Reporter getReporter() {
        return reporter;
    }
}
