package android.test;

@Deprecated
public class PackageInfoSources {
    private static ClassPathPackageInfoSource classPathSource;

    private PackageInfoSources() {
    }

    public static ClassPathPackageInfoSource forClassPath(ClassLoader classLoader) {
        if (classPathSource == null) {
            classPathSource = new ClassPathPackageInfoSource();
            classPathSource.setClassLoader(classLoader);
        }
        return classPathSource;
    }
}
