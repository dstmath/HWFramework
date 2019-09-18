package android.app;

import android.os.Build;
import android.os.GraphicsEnvironment;
import android.os.Trace;
import android.util.ArrayMap;
import com.android.internal.os.ClassLoaderFactory;
import dalvik.system.PathClassLoader;
import java.util.Collection;

public class ApplicationLoaders {
    private static final ApplicationLoaders gApplicationLoaders = new ApplicationLoaders();
    private final ArrayMap<String, ClassLoader> mLoaders = new ArrayMap<>();

    public static ApplicationLoaders getDefault() {
        return gApplicationLoaders;
    }

    /* access modifiers changed from: package-private */
    public ClassLoader getClassLoader(String zip, int targetSdkVersion, boolean isBundled, String librarySearchPath, String libraryPermittedPath, ClassLoader parent, String classLoaderName) {
        return getClassLoader(zip, targetSdkVersion, isBundled, librarySearchPath, libraryPermittedPath, parent, zip, classLoaderName, false);
    }

    /* access modifiers changed from: package-private */
    public ClassLoader getSplitClassLoader(String zip, int targetSdkVersion, boolean isBundled, String librarySearchPath, String libraryPermittedPath, ClassLoader parent, String classLoaderName) {
        return getClassLoader(zip, targetSdkVersion, isBundled, librarySearchPath, libraryPermittedPath, parent, zip, classLoaderName, true);
    }

    private ClassLoader getClassLoader(String zip, int targetSdkVersion, boolean isBundled, String librarySearchPath, String libraryPermittedPath, ClassLoader parent, String cacheKey, String classLoaderName, boolean isSplit) {
        String str = zip;
        String str2 = cacheKey;
        ClassLoader baseParent = ClassLoader.getSystemClassLoader().getParent();
        synchronized (this.mLoaders) {
            ClassLoader parent2 = parent == null ? baseParent : parent;
            if (parent2 == baseParent || isSplit) {
                String str3 = classLoaderName;
                ClassLoader loader = this.mLoaders.get(str2);
                if (loader != null) {
                    return loader;
                }
                Trace.traceBegin(64, str);
                ClassLoader classloader = ClassLoaderFactory.createClassLoader(str, librarySearchPath, libraryPermittedPath, parent2, targetSdkVersion, isBundled, classLoaderName);
                Trace.traceEnd(64);
                Trace.traceBegin(64, "setLayerPaths");
                try {
                    GraphicsEnvironment.getInstance().setLayerPaths(classloader, librarySearchPath, libraryPermittedPath);
                    Trace.traceEnd(64);
                    this.mLoaders.put(str2, classloader);
                    return classloader;
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            } else {
                try {
                    Trace.traceBegin(64, str);
                    ClassLoader loader2 = ClassLoaderFactory.createClassLoader(str, null, parent2, classLoaderName);
                    Trace.traceEnd(64);
                    return loader2;
                } catch (Throwable th2) {
                    th = th2;
                    String str4 = librarySearchPath;
                    String str5 = libraryPermittedPath;
                    throw th;
                }
            }
        }
    }

    public ClassLoader createAndCacheWebViewClassLoader(String packagePath, String libsPath, String cacheKey) {
        return getClassLoader(packagePath, Build.VERSION.SDK_INT, false, libsPath, null, null, cacheKey, null, false);
    }

    /* access modifiers changed from: package-private */
    public void addPath(ClassLoader classLoader, String dexPath) {
        if (classLoader instanceof PathClassLoader) {
            ((PathClassLoader) classLoader).addDexPath(dexPath);
            return;
        }
        throw new IllegalStateException("class loader is not a PathClassLoader");
    }

    /* access modifiers changed from: package-private */
    public void addNative(ClassLoader classLoader, Collection<String> libPaths) {
        if (classLoader instanceof PathClassLoader) {
            ((PathClassLoader) classLoader).addNativePath(libPaths);
            return;
        }
        throw new IllegalStateException("class loader is not a PathClassLoader");
    }
}
