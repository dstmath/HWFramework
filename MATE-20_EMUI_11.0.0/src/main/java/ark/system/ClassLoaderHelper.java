package ark.system;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.PathClassLoader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;

public class ClassLoaderHelper {
    public static Enumeration<URL> findResources(ClassLoader classLoader, String str) throws IllegalArgumentException {
        try {
            Method declaredMethod = BaseDexClassLoader.class.getDeclaredMethod("findResources", String.class);
            declaredMethod.setAccessible(true);
            if (classLoader instanceof PathClassLoader) {
                Object invoke = declaredMethod.invoke(classLoader, str);
                if (invoke instanceof Enumeration) {
                    return (Enumeration) invoke;
                }
            }
            throw new IllegalArgumentException("Find resources failed, input classLoader must be an instance created by ark.system.ClassLoaderCreator");
        } catch (IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException unused) {
            throw new IllegalArgumentException("Find resources failed, input name must be an instance of String");
        }
    }

    public static String findNativeLibrary(ClassLoader classLoader, String str) throws IllegalArgumentException {
        if (classLoader instanceof PathClassLoader) {
            return ((PathClassLoader) classLoader).findLibrary(str);
        }
        throw new IllegalArgumentException("Find library failed, input classLoader must be an instance created by ark.system.ClassLoaderCreator");
    }

    public static String toString(ClassLoader classLoader) {
        return classLoader.toString();
    }
}
