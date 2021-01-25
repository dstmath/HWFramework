package ohos.aafwk.utils.loader;

import dalvik.system.PathClassLoader;

public class ClassLoaderFactory {
    public static ClassLoader createClassLoader(String str, ClassLoader classLoader) {
        return new PathClassLoader(str, classLoader);
    }
}
