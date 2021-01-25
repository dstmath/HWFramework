package android.app;

import android.annotation.UnsupportedAppUsage;
import android.content.pm.SharedLibraryInfo;
import android.os.Build;
import android.os.GraphicsEnvironment;
import android.os.Trace;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.os.ClassLoaderFactory;
import com.android.internal.os.ZygoteInit;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationLoaders {
    private static final String TAG = "ApplicationLoaders";
    private static final ApplicationLoaders gApplicationLoaders = new ApplicationLoaders();
    @UnsupportedAppUsage
    private final ArrayMap<String, ClassLoader> mLoaders = new ArrayMap<>();
    private Map<String, CachedClassLoader> mSystemLibsCacheMap = null;

    @UnsupportedAppUsage
    public static ApplicationLoaders getDefault() {
        return gApplicationLoaders;
    }

    /* access modifiers changed from: package-private */
    public ClassLoader getClassLoader(String zip, int targetSdkVersion, boolean isBundled, String librarySearchPath, String libraryPermittedPath, ClassLoader parent, String classLoaderName) {
        return getClassLoaderWithSharedLibraries(zip, targetSdkVersion, isBundled, librarySearchPath, libraryPermittedPath, parent, classLoaderName, null);
    }

    /* access modifiers changed from: package-private */
    public ClassLoader getSplitClassLoader(String zip, int targetSdkVersion, boolean isBundled, String librarySearchPath, String libraryPermittedPath, ClassLoader parent, String classLoaderName) {
        return getClassLoader(zip, targetSdkVersion, isBundled, librarySearchPath, libraryPermittedPath, parent, zip, classLoaderName, null, true);
    }

    public void releaseSplitClassLoader(String cacheKey) {
        if (!TextUtils.isEmpty(cacheKey)) {
            Log.i(TAG, "releaseSplitClassLoader:" + cacheKey);
            synchronized (this.mLoaders) {
                this.mLoaders.remove(cacheKey);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ClassLoader getClassLoaderWithSharedLibraries(String zip, int targetSdkVersion, boolean isBundled, String librarySearchPath, String libraryPermittedPath, ClassLoader parent, String classLoaderName, List<ClassLoader> sharedLibraries) {
        return getClassLoader(zip, targetSdkVersion, isBundled, librarySearchPath, libraryPermittedPath, parent, zip, classLoaderName, sharedLibraries, false);
    }

    /* access modifiers changed from: package-private */
    public ClassLoader getSharedLibraryClassLoaderWithSharedLibraries(String zip, int targetSdkVersion, boolean isBundled, String librarySearchPath, String libraryPermittedPath, ClassLoader parent, String classLoaderName, List<ClassLoader> sharedLibraries) {
        ClassLoader loader = getCachedNonBootclasspathSystemLib(zip, parent, classLoaderName, sharedLibraries);
        if (loader != null) {
            return loader;
        }
        return getClassLoaderWithSharedLibraries(zip, targetSdkVersion, isBundled, librarySearchPath, libraryPermittedPath, parent, classLoaderName, sharedLibraries);
    }

    private ClassLoader getClassLoader(String zip, int targetSdkVersion, boolean isBundled, String librarySearchPath, String libraryPermittedPath, ClassLoader parent, String cacheKey, String classLoaderName, List<ClassLoader> sharedLibraries, boolean isSplit) {
        ClassLoader parent2;
        ClassLoader baseParent = ClassLoader.getSystemClassLoader().getParent();
        synchronized (this.mLoaders) {
            if (parent == null) {
                parent2 = baseParent;
            } else {
                parent2 = parent;
            }
            if (parent2 != baseParent) {
                if (!isSplit) {
                    try {
                        Trace.traceBegin(64, zip);
                        ClassLoader loader = ClassLoaderFactory.createClassLoader(zip, null, parent2, classLoaderName, sharedLibraries);
                        Trace.traceEnd(64);
                        return loader;
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                }
            }
            ClassLoader loader2 = this.mLoaders.get(cacheKey);
            if (loader2 != null) {
                return loader2;
            }
            Trace.traceBegin(64, zip);
            ClassLoader classloader = ClassLoaderFactory.createClassLoader(zip, librarySearchPath, libraryPermittedPath, parent2, targetSdkVersion, isBundled, classLoaderName, sharedLibraries);
            Trace.traceEnd(64);
            Trace.traceBegin(64, "setLayerPaths");
            try {
                GraphicsEnvironment.getInstance().setLayerPaths(classloader, librarySearchPath, libraryPermittedPath);
                Trace.traceEnd(64);
                if (cacheKey != null) {
                    this.mLoaders.put(cacheKey, classloader);
                }
                return classloader;
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    public void createAndCacheNonBootclasspathSystemClassLoaders(SharedLibraryInfo[] libs) {
        String classPathString;
        if (ZygoteInit.sIsMygote) {
            for (SharedLibraryInfo info : libs) {
                List<String> paths = info.getAllCodePaths();
                if (paths.size() == 1) {
                    classPathString = paths.get(0);
                } else {
                    classPathString = TextUtils.join(File.pathSeparator, paths);
                }
                getDefault().addPath(ClassLoader.getSystemClassLoader(), classPathString);
            }
        } else if (this.mSystemLibsCacheMap == null) {
            this.mSystemLibsCacheMap = new HashMap();
            for (SharedLibraryInfo lib : libs) {
                createAndCacheNonBootclasspathSystemClassLoader(lib);
            }
        } else {
            throw new IllegalStateException("Already cached.");
        }
    }

    public void addAndCacheNonBootclasspathSystemClassLoaders(SharedLibraryInfo lib) {
        if (this.mSystemLibsCacheMap != null) {
            createAndCacheNonBootclasspathSystemClassLoader(lib);
        }
    }

    private void createAndCacheNonBootclasspathSystemClassLoader(SharedLibraryInfo lib) {
        ArrayList<ClassLoader> sharedLibraries;
        String path = lib.getPath();
        List<SharedLibraryInfo> dependencies = lib.getDependencies();
        if (dependencies != null) {
            ArrayList<ClassLoader> sharedLibraries2 = new ArrayList<>(dependencies.size());
            for (SharedLibraryInfo dependency : dependencies) {
                String dependencyPath = dependency.getPath();
                CachedClassLoader cached = this.mSystemLibsCacheMap.get(dependencyPath);
                if (cached != null) {
                    sharedLibraries2.add(cached.loader);
                } else {
                    throw new IllegalStateException("Failed to find dependency " + dependencyPath + " of cachedlibrary " + path);
                }
            }
            sharedLibraries = sharedLibraries2;
        } else {
            sharedLibraries = null;
        }
        ClassLoader classLoader = getClassLoader(path, Build.VERSION.SDK_INT, true, null, null, null, null, null, sharedLibraries, false);
        if (classLoader != null) {
            CachedClassLoader cached2 = new CachedClassLoader();
            cached2.loader = classLoader;
            cached2.sharedLibraries = sharedLibraries;
            Log.d(TAG, "Created zygote-cached class loader: " + path);
            this.mSystemLibsCacheMap.put(path, cached2);
            return;
        }
        throw new IllegalStateException("Failed to cache " + path);
    }

    private static boolean sharedLibrariesEquals(List<ClassLoader> lhs, List<ClassLoader> rhs) {
        if (lhs == null) {
            return rhs == null;
        }
        return lhs.equals(rhs);
    }

    public ClassLoader getCachedNonBootclasspathSystemLib(String zip, ClassLoader parent, String classLoaderName, List<ClassLoader> sharedLibraries) {
        CachedClassLoader cached;
        Map<String, CachedClassLoader> map = this.mSystemLibsCacheMap;
        if (map == null || parent != null || classLoaderName != null || (cached = map.get(zip)) == null) {
            return null;
        }
        if (!sharedLibrariesEquals(sharedLibraries, cached.sharedLibraries)) {
            Log.w(TAG, "Unexpected environment for cached library: (" + sharedLibraries + "|" + cached.sharedLibraries + ")");
            return null;
        }
        Log.d(TAG, "Returning zygote-cached class loader: " + zip);
        return cached.loader;
    }

    public ClassLoader createAndCacheWebViewClassLoader(String packagePath, String libsPath, String cacheKey) {
        return getClassLoader(packagePath, Build.VERSION.SDK_INT, false, libsPath, null, null, cacheKey, null, null, false);
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

    /* access modifiers changed from: private */
    public static class CachedClassLoader {
        ClassLoader loader;
        List<ClassLoader> sharedLibraries;

        private CachedClassLoader() {
        }
    }
}
