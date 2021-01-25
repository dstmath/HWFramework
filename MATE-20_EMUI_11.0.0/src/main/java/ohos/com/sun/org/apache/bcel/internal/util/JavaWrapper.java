package ohos.com.sun.org.apache.bcel.internal.util;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class JavaWrapper {
    private ClassLoader loader;

    private static ClassLoader getClassLoader() {
        String systemProperty = SecuritySupport.getSystemProperty("bcel.classloader");
        if (systemProperty == null || "".equals(systemProperty)) {
            systemProperty = "ohos.com.sun.org.apache.bcel.internal.util.ClassLoader";
        }
        try {
            return (ClassLoader) Class.forName(systemProperty).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
    }

    public JavaWrapper(ClassLoader classLoader) {
        this.loader = classLoader;
    }

    public JavaWrapper() {
        this(getClassLoader());
    }

    public void runMain(String str, String[] strArr) throws ClassNotFoundException {
        try {
            Method method = this.loader.loadClass(str).getMethod("_main", strArr.getClass());
            int modifiers = method.getModifiers();
            Class<?> returnType = method.getReturnType();
            if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers) || Modifier.isAbstract(modifiers) || returnType != Void.TYPE) {
                throw new NoSuchMethodException();
            }
            try {
                method.invoke(null, strArr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (NoSuchMethodException unused) {
            PrintStream printStream = System.out;
            printStream.println("In class " + str + ": public static void _main(String[] argv) is not defined");
        }
    }

    public static void _main(String[] strArr) throws Exception {
        if (strArr.length == 0) {
            System.out.println("Missing class name.");
            return;
        }
        String str = strArr[0];
        String[] strArr2 = new String[(strArr.length - 1)];
        System.arraycopy(strArr, 1, strArr2, 0, strArr2.length);
        new JavaWrapper().runMain(str, strArr2);
    }
}
