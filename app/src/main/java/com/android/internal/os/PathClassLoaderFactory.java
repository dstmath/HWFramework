package com.android.internal.os;

import android.os.Trace;
import dalvik.system.PathClassLoader;

public class PathClassLoaderFactory {
    private static native String createClassloaderNamespace(ClassLoader classLoader, int i, String str, String str2, boolean z);

    private PathClassLoaderFactory() {
    }

    public static PathClassLoader createClassLoader(String dexPath, String librarySearchPath, String libraryPermittedPath, ClassLoader parent, int targetSdkVersion, boolean isNamespaceShared) {
        PathClassLoader pathClassloader = new PathClassLoader(dexPath, librarySearchPath, parent);
        Trace.traceBegin(64, "createClassloaderNamespace");
        String errorMessage = createClassloaderNamespace(pathClassloader, targetSdkVersion, librarySearchPath, libraryPermittedPath, isNamespaceShared);
        Trace.traceEnd(64);
        if (errorMessage == null) {
            return pathClassloader;
        }
        throw new UnsatisfiedLinkError("Unable to create namespace for the classloader " + pathClassloader + ": " + errorMessage);
    }
}
