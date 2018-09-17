package sun.nio.ch;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

class Reflect {

    /* renamed from: sun.nio.ch.Reflect.1 */
    static class AnonymousClass1 implements PrivilegedAction<Void> {
        final /* synthetic */ AccessibleObject val$ao;

        AnonymousClass1(AccessibleObject val$ao) {
            this.val$ao = val$ao;
        }

        public Void run() {
            this.val$ao.setAccessible(true);
            return null;
        }
    }

    private static class ReflectionError extends Error {
        private static final long serialVersionUID = -8659519328078164097L;

        ReflectionError(Throwable x) {
            super(x);
        }
    }

    private Reflect() {
    }

    private static void setAccessible(AccessibleObject ao) {
        AccessController.doPrivileged(new AnonymousClass1(ao));
    }

    static Constructor lookupConstructor(String className, Class[] paramTypes) {
        try {
            Constructor<?> c = Class.forName(className).getDeclaredConstructor(paramTypes);
            setAccessible(c);
            return c;
        } catch (ClassNotFoundException x) {
            throw new ReflectionError(x);
        } catch (NoSuchMethodException x2) {
            throw new ReflectionError(x2);
        }
    }

    static Object invoke(Constructor c, Object[] args) {
        try {
            return c.newInstance(args);
        } catch (InstantiationException x) {
            throw new ReflectionError(x);
        } catch (IllegalAccessException x2) {
            throw new ReflectionError(x2);
        } catch (InvocationTargetException x3) {
            throw new ReflectionError(x3);
        }
    }

    static Method lookupMethod(String className, String methodName, Class... paramTypes) {
        try {
            Method m = Class.forName(className).getDeclaredMethod(methodName, paramTypes);
            setAccessible(m);
            return m;
        } catch (ClassNotFoundException x) {
            throw new ReflectionError(x);
        } catch (NoSuchMethodException x2) {
            throw new ReflectionError(x2);
        }
    }

    static Object invoke(Method m, Object ob, Object[] args) {
        try {
            return m.invoke(ob, args);
        } catch (IllegalAccessException x) {
            throw new ReflectionError(x);
        } catch (InvocationTargetException x2) {
            throw new ReflectionError(x2);
        }
    }

    static Object invokeIO(Method m, Object ob, Object[] args) throws IOException {
        try {
            return m.invoke(ob, args);
        } catch (IllegalAccessException x) {
            throw new ReflectionError(x);
        } catch (InvocationTargetException x2) {
            if (IOException.class.isInstance(x2.getCause())) {
                throw ((IOException) x2.getCause());
            }
            throw new ReflectionError(x2);
        }
    }

    static Field lookupField(String className, String fieldName) {
        try {
            Field f = Class.forName(className).getDeclaredField(fieldName);
            setAccessible(f);
            return f;
        } catch (ClassNotFoundException x) {
            throw new ReflectionError(x);
        } catch (NoSuchFieldException x2) {
            throw new ReflectionError(x2);
        }
    }

    static Object get(Object ob, Field f) {
        try {
            return f.get(ob);
        } catch (IllegalAccessException x) {
            throw new ReflectionError(x);
        }
    }

    static Object get(Field f) {
        return get(null, f);
    }

    static void set(Object ob, Field f, Object val) {
        try {
            f.set(ob, val);
        } catch (IllegalAccessException x) {
            throw new ReflectionError(x);
        }
    }

    static void setInt(Object ob, Field f, int val) {
        try {
            f.setInt(ob, val);
        } catch (IllegalAccessException x) {
            throw new ReflectionError(x);
        }
    }

    static void setBoolean(Object ob, Field f, boolean val) {
        try {
            f.setBoolean(ob, val);
        } catch (IllegalAccessException x) {
            throw new ReflectionError(x);
        }
    }
}
