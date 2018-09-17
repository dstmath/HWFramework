package android.app;

import android.common.HwFrameworkFactory;
import android.os.Build.VERSION;
import android.os.Trace;
import android.util.ArrayMap;
import com.android.internal.os.PathClassLoaderFactory;
import dalvik.system.PathClassLoader;

public class ApplicationLoaders {
    private static final ApplicationLoaders gApplicationLoaders = new ApplicationLoaders();
    private final ArrayMap<String, ClassLoader> mLoaders = new ArrayMap();

    private static native void setupVulkanLayerPath(ClassLoader classLoader, String str);

    public static ApplicationLoaders getDefault() {
        return gApplicationLoaders;
    }

    ClassLoader getClassLoader(String zip, int targetSdkVersion, boolean isBundled, String librarySearchPath, String libraryPermittedPath, ClassLoader parent) {
        return getClassLoader(zip, targetSdkVersion, isBundled, librarySearchPath, libraryPermittedPath, parent, zip);
    }

    private ClassLoader getClassLoader(String zip, int targetSdkVersion, boolean isBundled, String librarySearchPath, String libraryPermittedPath, ClassLoader parent, String cacheKey) {
        ClassLoader baseParent = ClassLoader.getSystemClassLoader().getParent();
        synchronized (this.mLoaders) {
            if (parent == null) {
                ClassLoader featureParent = HwFrameworkFactory.getHwFLClassLoaderParent(zip);
                if (featureParent != null) {
                    baseParent = featureParent;
                }
                parent = baseParent;
            }
            PathClassLoader pathClassloader;
            if (parent == baseParent) {
                ClassLoader loader = (ClassLoader) this.mLoaders.get(cacheKey);
                if (loader != null) {
                    return loader;
                }
                Trace.traceBegin(64, zip);
                pathClassloader = PathClassLoaderFactory.createClassLoader(zip, librarySearchPath, libraryPermittedPath, parent, targetSdkVersion, isBundled);
                Trace.traceEnd(64);
                Trace.traceBegin(64, "setupVulkanLayerPath");
                setupVulkanLayerPath(pathClassloader, librarySearchPath);
                Trace.traceEnd(64);
                this.mLoaders.put(cacheKey, pathClassloader);
                return pathClassloader;
            }
            Trace.traceBegin(64, zip);
            pathClassloader = new PathClassLoader(zip, parent);
            Trace.traceEnd(64);
            return pathClassloader;
        }
    }

    public ClassLoader createAndCacheWebViewClassLoader(String packagePath, String libsPath, String cacheKey) {
        return getClassLoader(packagePath, VERSION.SDK_INT, false, libsPath, null, null, cacheKey);
    }

    void addPath(ClassLoader classLoader, String dexPath) {
        if (classLoader instanceof PathClassLoader) {
            ((PathClassLoader) classLoader).addDexPath(dexPath);
            return;
        }
        throw new IllegalStateException("class loader is not a PathClassLoader");
    }
}
