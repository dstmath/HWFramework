package com.google.gson.internal;

import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public abstract class UnsafeAllocator {
    public abstract <T> T newInstance(Class<T> cls) throws Exception;

    public static UnsafeAllocator create() {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field f = unsafeClass.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            final Object unsafe = f.get(null);
            final Method allocateInstance = unsafeClass.getMethod("allocateInstance", new Class[]{Class.class});
            return new UnsafeAllocator() {
                public <T> T newInstance(Class<T> c) throws Exception {
                    assertInstantiable(c);
                    return allocateInstance.invoke(unsafe, new Object[]{c});
                }
            };
        } catch (Exception e) {
            try {
                Method getConstructorId = ObjectStreamClass.class.getDeclaredMethod("getConstructorId", new Class[]{Class.class});
                getConstructorId.setAccessible(true);
                final int constructorId = ((Integer) getConstructorId.invoke(null, new Object[]{Object.class})).intValue();
                final Method newInstance = ObjectStreamClass.class.getDeclaredMethod("newInstance", new Class[]{Class.class, Integer.TYPE});
                newInstance.setAccessible(true);
                return new UnsafeAllocator() {
                    public <T> T newInstance(Class<T> c) throws Exception {
                        assertInstantiable(c);
                        return newInstance.invoke(null, new Object[]{c, Integer.valueOf(constructorId)});
                    }
                };
            } catch (Exception e2) {
                try {
                    final Method newInstance2 = ObjectInputStream.class.getDeclaredMethod("newInstance", new Class[]{Class.class, Class.class});
                    newInstance2.setAccessible(true);
                    return new UnsafeAllocator() {
                        public <T> T newInstance(Class<T> c) throws Exception {
                            assertInstantiable(c);
                            return newInstance2.invoke(null, new Object[]{c, Object.class});
                        }
                    };
                } catch (Exception e3) {
                    return new UnsafeAllocator() {
                        public <T> T newInstance(Class<T> c) {
                            throw new UnsupportedOperationException("Cannot allocate " + c);
                        }
                    };
                }
            }
        }
    }

    static void assertInstantiable(Class<?> c) {
        int modifiers = c.getModifiers();
        if (Modifier.isInterface(modifiers)) {
            throw new UnsupportedOperationException("Interface can't be instantiated! Interface name: " + c.getName());
        } else if (Modifier.isAbstract(modifiers)) {
            throw new UnsupportedOperationException("Abstract class can't be instantiated! Class name: " + c.getName());
        }
    }
}
