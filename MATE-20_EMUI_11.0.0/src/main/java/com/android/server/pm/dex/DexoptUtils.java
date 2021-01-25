package com.android.server.pm.dex;

import android.content.pm.ApplicationInfo;
import android.content.pm.SharedLibraryInfo;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.os.ClassLoaderFactory;
import java.io.File;
import java.util.List;

public final class DexoptUtils {
    private static final String SHARED_LIBRARY_LOADER_TYPE = ClassLoaderFactory.getPathClassLoaderName();
    private static final String TAG = "DexoptUtils";

    private DexoptUtils() {
    }

    public static String[] getClassLoaderContexts(ApplicationInfo info, List<SharedLibraryInfo> sharedLibraries, boolean[] pathsWithCode) {
        String sharedLibrariesContext = "";
        if (sharedLibraries != null) {
            sharedLibrariesContext = encodeSharedLibraries(sharedLibraries);
        }
        String baseApkContextClassLoader = encodeClassLoader("", info.classLoaderName, sharedLibrariesContext);
        if (info.getSplitCodePaths() == null) {
            return new String[]{baseApkContextClassLoader};
        }
        String[] splitRelativeCodePaths = getSplitRelativeCodePaths(info);
        String baseApkName = new File(info.getBaseCodePath()).getName();
        String[] classLoaderContexts = new String[(splitRelativeCodePaths.length + 1)];
        classLoaderContexts[0] = pathsWithCode[0] ? baseApkContextClassLoader : null;
        if (!info.requestsIsolatedSplitLoading() || info.splitDependencies == null) {
            String classpath = baseApkName;
            for (int i = 1; i < classLoaderContexts.length; i++) {
                if (pathsWithCode[i]) {
                    classLoaderContexts[i] = encodeClassLoader(classpath, info.classLoaderName, sharedLibrariesContext);
                } else {
                    classLoaderContexts[i] = null;
                }
                classpath = encodeClasspath(classpath, splitRelativeCodePaths[i - 1]);
            }
        } else {
            String[] splitClassLoaderEncodingCache = new String[splitRelativeCodePaths.length];
            for (int i2 = 0; i2 < splitRelativeCodePaths.length; i2++) {
                splitClassLoaderEncodingCache[i2] = encodeClassLoader(splitRelativeCodePaths[i2], info.splitClassLoaderNames[i2]);
            }
            String splitDependencyOnBase = encodeClassLoader(baseApkName, info.classLoaderName);
            SparseArray<int[]> splitDependencies = info.splitDependencies;
            for (int i3 = 1; i3 < splitDependencies.size(); i3++) {
                int splitIndex = splitDependencies.keyAt(i3);
                if (pathsWithCode[splitIndex]) {
                    getParentDependencies(splitIndex, splitClassLoaderEncodingCache, splitDependencies, classLoaderContexts, splitDependencyOnBase);
                }
            }
            for (int i4 = 1; i4 < classLoaderContexts.length; i4++) {
                String splitClassLoader = encodeClassLoader("", info.splitClassLoaderNames[i4 - 1]);
                if (pathsWithCode[i4]) {
                    classLoaderContexts[i4] = classLoaderContexts[i4] == null ? splitClassLoader : encodeClassLoaderChain(splitClassLoader, classLoaderContexts[i4]) + sharedLibrariesContext;
                } else {
                    classLoaderContexts[i4] = null;
                }
            }
        }
        return classLoaderContexts;
    }

    public static String getClassLoaderContext(SharedLibraryInfo info) {
        String sharedLibrariesContext = "";
        if (info.getDependencies() != null) {
            sharedLibrariesContext = encodeSharedLibraries(info.getDependencies());
        }
        return encodeClassLoader("", SHARED_LIBRARY_LOADER_TYPE, sharedLibrariesContext);
    }

    private static String getParentDependencies(int index, String[] splitClassLoaderEncodingCache, SparseArray<int[]> splitDependencies, String[] classLoaderContexts, String splitDependencyOnBase) {
        String splitContext;
        if (index == 0) {
            return splitDependencyOnBase;
        }
        if (classLoaderContexts[index] != null) {
            return classLoaderContexts[index];
        }
        int parent = splitDependencies.get(index)[0];
        String parentDependencies = getParentDependencies(parent, splitClassLoaderEncodingCache, splitDependencies, classLoaderContexts, splitDependencyOnBase);
        if (parent == 0) {
            splitContext = parentDependencies;
        } else {
            splitContext = encodeClassLoaderChain(splitClassLoaderEncodingCache[parent - 1], parentDependencies);
        }
        classLoaderContexts[index] = splitContext;
        return splitContext;
    }

    private static String encodeSharedLibrary(SharedLibraryInfo sharedLibrary) {
        List<String> paths = sharedLibrary.getAllCodePaths();
        String classLoaderSpec = encodeClassLoader(encodeClasspath((String[]) paths.toArray(new String[paths.size()])), SHARED_LIBRARY_LOADER_TYPE);
        if (sharedLibrary.getDependencies() == null) {
            return classLoaderSpec;
        }
        return classLoaderSpec + encodeSharedLibraries(sharedLibrary.getDependencies());
    }

    private static String encodeSharedLibraries(List<SharedLibraryInfo> sharedLibraries) {
        String sharedLibrariesContext = "{";
        boolean first = true;
        for (SharedLibraryInfo info : sharedLibraries) {
            if (!first) {
                sharedLibrariesContext = sharedLibrariesContext + "#";
            }
            first = false;
            sharedLibrariesContext = sharedLibrariesContext + encodeSharedLibrary(info);
        }
        return sharedLibrariesContext + "}";
    }

    private static String encodeClasspath(String[] classpathElements) {
        if (classpathElements == null || classpathElements.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String element : classpathElements) {
            if (sb.length() != 0) {
                sb.append(":");
            }
            sb.append(element);
        }
        return sb.toString();
    }

    private static String encodeClasspath(String classpath, String newElement) {
        if (classpath.isEmpty()) {
            return newElement;
        }
        return classpath + ":" + newElement;
    }

    static String encodeClassLoader(String classpath, String classLoaderName) {
        classpath.getClass();
        String classLoaderDexoptEncoding = classLoaderName;
        if (ClassLoaderFactory.isPathClassLoaderName(classLoaderName)) {
            classLoaderDexoptEncoding = "PCL";
        } else if (ClassLoaderFactory.isDelegateLastClassLoaderName(classLoaderName)) {
            classLoaderDexoptEncoding = "DLC";
        } else {
            Slog.wtf(TAG, "Unsupported classLoaderName: " + classLoaderName);
        }
        return classLoaderDexoptEncoding + "[" + classpath + "]";
    }

    private static String encodeClassLoader(String classpath, String classLoaderName, String sharedLibraries) {
        return encodeClassLoader(classpath, classLoaderName) + sharedLibraries;
    }

    static String encodeClassLoaderChain(String cl1, String cl2) {
        if (cl1.isEmpty()) {
            return cl2;
        }
        if (cl2.isEmpty()) {
            return cl1;
        }
        return cl1 + ";" + cl2;
    }

    static String[] processContextForDexLoad(List<String> classLoadersNames, List<String> classPaths) {
        if (classLoadersNames.size() != classPaths.size()) {
            throw new IllegalArgumentException("The size of the class loader names and the dex paths do not match.");
        } else if (!classLoadersNames.isEmpty()) {
            String parentContext = "";
            for (int i = 1; i < classLoadersNames.size(); i++) {
                if (!ClassLoaderFactory.isValidClassLoaderName(classLoadersNames.get(i)) || classPaths.get(i) == null) {
                    return null;
                }
                String sharedLibraryContext = null;
                if (classPaths.get(i).contains("||")) {
                    String[] pair = classPaths.get(i).split("\\|\\|");
                    classPaths.set(i, pair[0]);
                    if (pair.length == 2) {
                        sharedLibraryContext = pair[1];
                    }
                }
                String classpath = encodeClasspath(classPaths.get(i).split(File.pathSeparator));
                if (sharedLibraryContext == null) {
                    parentContext = encodeClassLoaderChain(parentContext, encodeClassLoader(classpath, classLoadersNames.get(i)));
                } else {
                    parentContext = encodeClassLoaderChain(parentContext, encodeClassLoader(classpath, classLoadersNames.get(i)) + sharedLibraryContext);
                }
            }
            String loadingClassLoader = classLoadersNames.get(0);
            if (!ClassLoaderFactory.isValidClassLoaderName(loadingClassLoader)) {
                return null;
            }
            String[] loadedDexPaths = classPaths.get(0).split(File.pathSeparator);
            String[] loadedDexPathsContext = new String[loadedDexPaths.length];
            String currentLoadedDexPathClasspath = "";
            for (int i2 = 0; i2 < loadedDexPaths.length; i2++) {
                String dexPath = loadedDexPaths[i2];
                loadedDexPathsContext[i2] = encodeClassLoaderChain(encodeClassLoader(currentLoadedDexPathClasspath, loadingClassLoader), parentContext);
                currentLoadedDexPathClasspath = encodeClasspath(currentLoadedDexPathClasspath, dexPath);
            }
            return loadedDexPathsContext;
        } else {
            throw new IllegalArgumentException("Empty classLoadersNames");
        }
    }

    private static String[] getSplitRelativeCodePaths(ApplicationInfo info) {
        String baseCodePath = new File(info.getBaseCodePath()).getParent();
        String[] splitCodePaths = info.getSplitCodePaths();
        String[] splitRelativeCodePaths = new String[splitCodePaths.length];
        for (int i = 0; i < splitCodePaths.length; i++) {
            File pathFile = new File(splitCodePaths[i]);
            splitRelativeCodePaths[i] = pathFile.getName();
            String basePath = pathFile.getParent();
            if (!basePath.equals(baseCodePath)) {
                Slog.wtf(TAG, "Split paths have different base paths: " + basePath + " and " + baseCodePath);
            }
        }
        return splitRelativeCodePaths;
    }
}
