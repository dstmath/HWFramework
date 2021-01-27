package com.huawei.gson.internal;

import java.lang.reflect.Type;

public final class Primitives {
    private Primitives() {
    }

    public static boolean isPrimitive(Type type) {
        return (type instanceof Class) && ((Class) type).isPrimitive();
    }

    public static boolean isWrapperType(Type type) {
        return type == Integer.class || type == Float.class || type == Byte.class || type == Double.class || type == Long.class || type == Character.class || type == Boolean.class || type == Short.class || type == Void.class;
    }

    public static <T> Class<T> wrap(Class<T> type) {
        if (type == Integer.TYPE) {
            return Integer.class;
        }
        if (type == Float.TYPE) {
            return Float.class;
        }
        if (type == Byte.TYPE) {
            return Byte.class;
        }
        if (type == Double.TYPE) {
            return Double.class;
        }
        if (type == Long.TYPE) {
            return Long.class;
        }
        if (type == Character.TYPE) {
            return Character.class;
        }
        if (type == Boolean.TYPE) {
            return Boolean.class;
        }
        if (type == Short.TYPE) {
            return Short.class;
        }
        if (type == Void.TYPE) {
            return Void.class;
        }
        return type;
    }

    public static <T> Class<T> unwrap(Class<T> type) {
        if (type == Integer.class) {
            return Integer.TYPE;
        }
        if (type == Float.class) {
            return Float.TYPE;
        }
        if (type == Byte.class) {
            return Byte.TYPE;
        }
        if (type == Double.class) {
            return Double.TYPE;
        }
        if (type == Long.class) {
            return Long.TYPE;
        }
        if (type == Character.class) {
            return Character.TYPE;
        }
        if (type == Boolean.class) {
            return Boolean.TYPE;
        }
        if (type == Short.class) {
            return Short.TYPE;
        }
        if (type == Void.class) {
            return Void.TYPE;
        }
        return type;
    }
}
