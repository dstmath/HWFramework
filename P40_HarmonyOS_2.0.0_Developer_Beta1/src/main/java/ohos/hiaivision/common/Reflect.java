package ohos.hiaivision.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class Reflect {
    private Class<?> className;
    private Constructor<?> constructor;
    private Method methodName;
    private Object objectName;

    private Reflect(String str, ClassLoader classLoader) throws ReflectiveOperationException {
        this.className = classLoader.loadClass(str);
    }

    public Reflect create(Object... objArr) throws ReflectiveOperationException {
        if (this.objectName == null) {
            if (objArr.length == 0) {
                this.constructor = this.className.getDeclaredConstructor(new Class[0]);
                this.constructor.setAccessible(true);
                this.objectName = this.constructor.newInstance(new Object[0]);
            } else {
                this.objectName = this.constructor.newInstance(objArr);
            }
        }
        return this;
    }

    public Reflect call(String str, Class<?>... clsArr) throws ReflectiveOperationException {
        this.methodName = this.className.getDeclaredMethod(str, clsArr);
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            /* class ohos.hiaivision.common.Reflect.AnonymousClass1 */

            @Override // java.security.PrivilegedAction
            public Object run() {
                Reflect.this.methodName.setAccessible(true);
                return null;
            }
        });
        return this;
    }

    public Object invoke(Object... objArr) throws ReflectiveOperationException {
        return this.methodName.invoke(this.objectName, objArr);
    }

    public static class Builder {
        public static Reflect on(String str, ClassLoader classLoader) throws ReflectiveOperationException {
            return new Reflect(str, classLoader);
        }
    }
}
