package ark.system;

import dalvik.system.PathClassLoader;

public class ClassLoaderCreator {
    public static ClassLoader createClassLoader(String str, ClassLoader classLoader) {
        return new PathClassLoader(str, classLoader);
    }

    public static ClassLoader createClassLoader(String str, String str2, ClassLoader classLoader) {
        return new PathClassLoader(str, str2, classLoader);
    }
}
