package android.test;

import com.google.android.collect.Maps;
import com.google.android.collect.Sets;
import dalvik.system.DexFile;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Deprecated
public class ClassPathPackageInfoSource {
    private static final String CLASS_EXTENSION = ".class";
    private static final ClassLoader CLASS_LOADER = ClassPathPackageInfoSource.class.getClassLoader();
    private static String[] apkPaths;
    private final SimpleCache<String, ClassPathPackageInfo> cache = new SimpleCache<String, ClassPathPackageInfo>() {
        protected ClassPathPackageInfo load(String pkgName) {
            return ClassPathPackageInfoSource.this.createPackageInfo(pkgName);
        }
    };
    private ClassLoader classLoader;
    private final String[] classPath = getClassPath();
    private final Map<File, Set<String>> jarFiles = Maps.newHashMap();

    ClassPathPackageInfoSource() {
    }

    public static void setApkPaths(String[] apkPaths) {
        apkPaths = apkPaths;
    }

    public ClassPathPackageInfo getPackageInfo(String pkgName) {
        return (ClassPathPackageInfo) this.cache.get(pkgName);
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0042 A:{ExcHandler: java.lang.ClassNotFoundException (r3_0 'e' java.lang.Throwable), Splitter: B:7:0x0033} */
    /* JADX WARNING: Missing block: B:13:0x0042, code:
            r3 = move-exception;
     */
    /* JADX WARNING: Missing block: B:14:0x0043, code:
            android.util.Log.w("ClassPathPackageInfoSource", "Cannot load class. Make sure it is in your apk. Class name: '" + r0 + "'. Message: " + r3.getMessage(), r3);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ClassPathPackageInfo createPackageInfo(String packageName) {
        Set<String> subpackageNames = new TreeSet();
        Set<String> classNames = new TreeSet();
        Set<Class<?>> topLevelClasses = Sets.newHashSet();
        findClasses(packageName, classNames, subpackageNames);
        for (String className : classNames) {
            if (!(className.endsWith(".R") || className.endsWith(".Manifest"))) {
                try {
                    topLevelClasses.add(Class.forName(className, false, this.classLoader != null ? this.classLoader : CLASS_LOADER));
                } catch (Throwable e) {
                }
            }
        }
        return new ClassPathPackageInfo(this, packageName, subpackageNames, topLevelClasses);
    }

    private void findClasses(String packageName, Set<String> classNames, Set<String> subpackageNames) {
        String pathPrefix = (packageName + '.').replace('.', '/');
        for (String entryName : this.classPath) {
            if (new File(entryName).exists()) {
                try {
                    if (entryName.endsWith(".apk")) {
                        findClassesInApk(entryName, packageName, classNames, subpackageNames);
                    } else {
                        for (String apkPath : apkPaths) {
                            scanForApkFiles(new File(apkPath), packageName, classNames, subpackageNames);
                        }
                    }
                } catch (IOException e) {
                    throw new AssertionError("Can't read classpath entry " + entryName + ": " + e.getMessage());
                }
            }
        }
    }

    private void scanForApkFiles(File source, String packageName, Set<String> classNames, Set<String> subpackageNames) throws IOException {
        if (source.getPath().endsWith(".apk")) {
            findClassesInApk(source.getPath(), packageName, classNames, subpackageNames);
            return;
        }
        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                scanForApkFiles(file, packageName, classNames, subpackageNames);
            }
        }
    }

    private void findClassesInDirectory(File classDir, String packagePrefix, String pathPrefix, Set<String> classNames, Set<String> subpackageNames) throws IOException {
        File directory = new File(classDir, pathPrefix);
        if (directory.exists()) {
            for (File f : directory.listFiles()) {
                String name = f.getName();
                if (name.endsWith(CLASS_EXTENSION) && isToplevelClass(name)) {
                    classNames.add(packagePrefix + getClassName(name));
                } else if (f.isDirectory()) {
                    subpackageNames.add(packagePrefix + name);
                }
            }
        }
    }

    private void findClassesInJar(File jarFile, String pathPrefix, Set<String> classNames, Set<String> subpackageNames) throws IOException {
        Set<String> entryNames = getJarEntries(jarFile);
        if (entryNames.contains(pathPrefix)) {
            int prefixLength = pathPrefix.length();
            for (String entryName : entryNames) {
                if (entryName.startsWith(pathPrefix) && entryName.endsWith(CLASS_EXTENSION)) {
                    int index = entryName.indexOf(47, prefixLength);
                    if (index >= 0) {
                        subpackageNames.add(entryName.substring(0, index).replace('/', '.'));
                    } else if (isToplevelClass(entryName)) {
                        classNames.add(getClassName(entryName).replace('/', '.'));
                    }
                }
            }
        }
    }

    private void findClassesInApk(String apkPath, String packageName, Set<String> classNames, Set<String> subpackageNames) throws IOException {
        DexFile dexFile;
        Throwable th;
        try {
            DexFile dexFile2 = new DexFile(apkPath);
            try {
                Enumeration<String> apkClassNames = dexFile2.entries();
                while (apkClassNames.hasMoreElements()) {
                    String className = (String) apkClassNames.nextElement();
                    if (className.startsWith(packageName)) {
                        String subPackageName = packageName;
                        int lastPackageSeparator = className.lastIndexOf(46);
                        if (lastPackageSeparator > 0) {
                            subPackageName = className.substring(0, lastPackageSeparator);
                        }
                        if (subPackageName.length() > packageName.length()) {
                            subpackageNames.add(subPackageName);
                        } else if (isToplevelClass(className)) {
                            classNames.add(className);
                        }
                    }
                }
            } catch (IOException e) {
                dexFile = dexFile2;
            } catch (Throwable th2) {
                th = th2;
                dexFile = dexFile2;
                throw th;
            }
        } catch (IOException e2) {
        } catch (Throwable th3) {
            th = th3;
            throw th;
        }
    }

    private Set<String> getJarEntries(File jarFile) throws IOException {
        Set<String> entryNames = (Set) this.jarFiles.get(jarFile);
        if (entryNames == null) {
            entryNames = Sets.newHashSet();
            Enumeration<? extends ZipEntry> entries = new ZipFile(jarFile).entries();
            while (entries.hasMoreElements()) {
                String entryName = ((ZipEntry) entries.nextElement()).getName();
                if (entryName.endsWith(CLASS_EXTENSION)) {
                    entryNames.add(entryName);
                    int lastIndex = entryName.lastIndexOf(47);
                    while (true) {
                        entryNames.add(entryName.substring(0, lastIndex + 1));
                        lastIndex = entryName.lastIndexOf(47, lastIndex - 1);
                        if (lastIndex <= 0) {
                            break;
                        }
                    }
                }
            }
            this.jarFiles.put(jarFile, entryNames);
        }
        return entryNames;
    }

    private static boolean isToplevelClass(String fileName) {
        return fileName.indexOf(36) < 0;
    }

    private static String getClassName(String className) {
        return className.substring(0, className.length() - CLASS_EXTENSION.length());
    }

    private static String[] getClassPath() {
        return System.getProperty("java.class.path").split(Pattern.quote(System.getProperty("path.separator", ":")));
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
