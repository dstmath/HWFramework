package ark.system;

import com.huawei.ark.classloader.ExtendedClassLoaderHelper;

public class HotFixClassLoader {
    private static final String MAPLE_RUNTIME_PROPERTY = "MAPLE_RUNTIME";

    public static boolean applyPatch(ClassLoader classLoader, String str) throws UnsupportedOperationException {
        if (System.getenv(MAPLE_RUNTIME_PROPERTY) != null) {
            return ExtendedClassLoaderHelper.applyPatch(classLoader, str);
        }
        throw new UnsupportedOperationException("Current runtime environment is not ark runtime, can't use HotFixClassLoader");
    }
}
