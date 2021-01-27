package ark.system;

import dalvik.system.DelegateLastClassLoader;
import dalvik.system.PathClassLoader;

public class ClassLoaderCreator {
    public static ClassLoader createClassLoader(String str, ClassLoader classLoader) {
        return createClassLoader(str, null, classLoader, false);
    }

    public static ClassLoader createClassLoader(String str, String str2, ClassLoader classLoader) {
        return createClassLoader(str, str2, classLoader, false);
    }

    public static ClassLoader createClassLoader(String str, String str2, ClassLoader classLoader, boolean z) {
        if (!z) {
            return new PathClassLoader(str, str2, classLoader);
        }
        return new DelegateLastClassLoader(str, str2, classLoader);
    }
}
