package java.lang;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import libcore.util.BasicLruCache;
import libcore.util.EmptyArray;

public abstract class Enum<E extends Enum<E>> implements Comparable<E>, Serializable {
    private static final BasicLruCache<Class<? extends Enum>, Object[]> sharedConstantsCache = new BasicLruCache<Class<? extends Enum>, Object[]>(64) {
        protected Object[] create(Class<? extends Enum> enumType) {
            if (!enumType.isEnum()) {
                return null;
            }
            try {
                Method method = enumType.getDeclaredMethod("values", EmptyArray.CLASS);
                method.setAccessible(true);
                return (Object[]) method.invoke((Object[]) null, new Object[0]);
            } catch (NoSuchMethodException impossible) {
                throw new AssertionError("impossible", impossible);
            } catch (IllegalAccessException impossible2) {
                throw new AssertionError("impossible", impossible2);
            } catch (InvocationTargetException impossible3) {
                throw new AssertionError("impossible", impossible3);
            }
        }
    };
    private final String name;
    private final int ordinal;

    public final String name() {
        return this.name;
    }

    public final int ordinal() {
        return this.ordinal;
    }

    protected Enum(String name, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
    }

    public String toString() {
        return this.name;
    }

    public final boolean equals(Object other) {
        return this == other;
    }

    public final int hashCode() {
        return super.hashCode();
    }

    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public final int compareTo(E o) {
        E other = o;
        if (getClass() == o.getClass() || getDeclaringClass() == o.getDeclaringClass()) {
            return this.ordinal - o.ordinal;
        }
        throw new ClassCastException();
    }

    public final Class<E> getDeclaringClass() {
        Class<?> clazz = getClass();
        Class<?> zuper = clazz.getSuperclass();
        return zuper == Enum.class ? clazz : zuper;
    }

    public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
        if (enumType == null) {
            throw new NullPointerException("enumType == null");
        } else if (name == null) {
            throw new NullPointerException("name == null");
        } else {
            T[] values = getSharedConstants(enumType);
            if (values == null) {
                throw new IllegalArgumentException(enumType.toString() + " is not an enum type.");
            }
            for (int i = values.length - 1; i >= 0; i--) {
                T value = values[i];
                if (name.equals(value.name())) {
                    return value;
                }
            }
            throw new IllegalArgumentException("No enum constant " + enumType.getCanonicalName() + "." + name);
        }
    }

    public static <T extends Enum<T>> T[] getSharedConstants(Class<T> enumType) {
        return (Enum[]) sharedConstantsCache.get(enumType);
    }

    protected final void finalize() {
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException("can't deserialize enum");
    }

    private void readObjectNoData() throws ObjectStreamException {
        throw new InvalidObjectException("can't deserialize enum");
    }
}
