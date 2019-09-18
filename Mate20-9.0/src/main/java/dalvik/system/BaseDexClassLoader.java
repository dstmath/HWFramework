package dalvik.system;

import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

public class BaseDexClassLoader extends ClassLoader {
    private static volatile Reporter reporter = null;
    private final DexPathList pathList;

    public interface Reporter {
        void report(List<BaseDexClassLoader> list, List<String> list2);
    }

    public BaseDexClassLoader(String dexPath, File optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        this(dexPath, optimizedDirectory, librarySearchPath, parent, false);
    }

    public BaseDexClassLoader(String dexPath, File optimizedDirectory, String librarySearchPath, ClassLoader parent, boolean isTrusted) {
        super(parent);
        DexPathList dexPathList = new DexPathList(this, dexPath, librarySearchPath, null, isTrusted);
        this.pathList = dexPathList;
        if (reporter != null) {
            reportClassLoaderChain();
        }
    }

    private void reportClassLoaderChain() {
        ArrayList<BaseDexClassLoader> classLoadersChain = new ArrayList<>();
        ArrayList<String> classPaths = new ArrayList<>();
        classLoadersChain.add(this);
        classPaths.add(String.join(File.pathSeparator, this.pathList.getDexPaths()));
        boolean onlySawSupportedClassLoaders = true;
        ClassLoader bootClassLoader = ClassLoader.getSystemClassLoader().getParent();
        ClassLoader current = getParent();
        while (true) {
            if (current != null && current != bootClassLoader) {
                if (!(current instanceof BaseDexClassLoader)) {
                    onlySawSupportedClassLoaders = false;
                    break;
                }
                BaseDexClassLoader bdcCurrent = (BaseDexClassLoader) current;
                classLoadersChain.add(bdcCurrent);
                classPaths.add(String.join(File.pathSeparator, bdcCurrent.pathList.getDexPaths()));
                current = current.getParent();
            } else {
                break;
            }
        }
        if (onlySawSupportedClassLoaders) {
            reporter.report(classLoadersChain, classPaths);
        }
    }

    public BaseDexClassLoader(ByteBuffer[] dexFiles, ClassLoader parent) {
        super(parent);
        this.pathList = new DexPathList(this, dexFiles);
    }

    /* access modifiers changed from: protected */
    public Class<?> findClass(String name) throws ClassNotFoundException {
        List<Throwable> suppressedExceptions = new ArrayList<>();
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
        addDexPath(dexPath, false);
    }

    public void addDexPath(String dexPath, boolean isTrusted) {
        this.pathList.addDexPath(dexPath, null, isTrusted);
    }

    public void addNativePath(Collection<String> libPaths) {
        this.pathList.addNativePath(libPaths);
    }

    /* access modifiers changed from: protected */
    public URL findResource(String name) {
        return this.pathList.findResource(name);
    }

    /* access modifiers changed from: protected */
    public Enumeration<URL> findResources(String name) {
        return this.pathList.findResources(name);
    }

    public String findLibrary(String name) {
        return this.pathList.findLibrary(name);
    }

    /* Debug info: failed to restart local var, previous not found, register: 10 */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0024, code lost:
        return r0;
     */
    public synchronized Package getPackage(String name) {
        if (name != null) {
            if (!name.isEmpty()) {
                Package pack = super.getPackage(name);
                if (pack == null) {
                    pack = definePackage(name, "Unknown", "0.0", "Unknown", "Unknown", "0.0", "Unknown", null);
                }
            }
        }
        return null;
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
