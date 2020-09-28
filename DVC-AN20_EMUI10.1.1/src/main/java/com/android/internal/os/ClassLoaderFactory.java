package com.android.internal.os;

import android.annotation.UnsupportedAppUsage;
import android.os.Trace;
import dalvik.system.DelegateLastClassLoader;
import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;
import java.util.List;

public class ClassLoaderFactory {
    private static final String DELEGATE_LAST_CLASS_LOADER_NAME = DelegateLastClassLoader.class.getName();
    private static final String DEX_CLASS_LOADER_NAME = DexClassLoader.class.getName();
    private static final String PATH_CLASS_LOADER_NAME = PathClassLoader.class.getName();

    @UnsupportedAppUsage
    private static native String createClassloaderNamespace(ClassLoader classLoader, int i, String str, String str2, boolean z, String str3);

    private ClassLoaderFactory() {
    }

    public static String getPathClassLoaderName() {
        return PATH_CLASS_LOADER_NAME;
    }

    public static boolean isValidClassLoaderName(String name) {
        return name != null && (isPathClassLoaderName(name) || isDelegateLastClassLoaderName(name));
    }

    public static boolean isPathClassLoaderName(String name) {
        return name == null || PATH_CLASS_LOADER_NAME.equals(name) || DEX_CLASS_LOADER_NAME.equals(name);
    }

    public static boolean isDelegateLastClassLoaderName(String name) {
        return DELEGATE_LAST_CLASS_LOADER_NAME.equals(name);
    }

    public static ClassLoader createClassLoader(String dexPath, String librarySearchPath, ClassLoader parent, String classloaderName, List<ClassLoader> sharedLibraries) {
        ClassLoader[] arrayOfSharedLibraries;
        if (sharedLibraries == null) {
            arrayOfSharedLibraries = null;
        } else {
            arrayOfSharedLibraries = (ClassLoader[]) sharedLibraries.toArray(new ClassLoader[sharedLibraries.size()]);
        }
        if (isPathClassLoaderName(classloaderName)) {
            return new PathClassLoader(dexPath, librarySearchPath, parent, arrayOfSharedLibraries);
        }
        if (isDelegateLastClassLoaderName(classloaderName)) {
            return new DelegateLastClassLoader(dexPath, librarySearchPath, parent, arrayOfSharedLibraries);
        }
        throw new AssertionError("Invalid classLoaderName: " + classloaderName);
    }

    public static ClassLoader createClassLoader(String dexPath, String librarySearchPath, String libraryPermittedPath, ClassLoader parent, int targetSdkVersion, boolean isNamespaceShared, String classLoaderName) {
        return createClassLoader(dexPath, librarySearchPath, libraryPermittedPath, parent, targetSdkVersion, isNamespaceShared, classLoaderName, null);
    }

    public static ClassLoader createClassLoader(String dexPath, String librarySearchPath, String libraryPermittedPath, ClassLoader parent, int targetSdkVersion, boolean isNamespaceShared, String classLoaderName, List<ClassLoader> sharedLibraries) {
        ClassLoader classLoader = createClassLoader(dexPath, librarySearchPath, parent, classLoaderName, sharedLibraries);
        Trace.traceBegin(64, "createClassloaderNamespace");
        String errorMessage = createClassloaderNamespace(classLoader, targetSdkVersion, librarySearchPath, libraryPermittedPath, isNamespaceShared, dexPath);
        Trace.traceEnd(64);
        if (errorMessage == null) {
            return classLoader;
        }
        throw new UnsatisfiedLinkError("Unable to create namespace for the classloader " + classLoader + ": " + errorMessage);
    }
}
