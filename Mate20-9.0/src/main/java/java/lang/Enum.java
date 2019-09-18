package java.lang;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.Enum;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import libcore.util.BasicLruCache;
import libcore.util.EmptyArray;

public abstract class Enum<E extends Enum<E>> implements Comparable<E>, Serializable {
    private static final BasicLruCache<Class<? extends Enum>, Object[]> sharedConstantsCache = new BasicLruCache<Class<? extends Enum>, Object[]>(64) {
        /* access modifiers changed from: protected */
        public Object[] create(Class<? extends Enum> enumType) {
            if (!enumType.isEnum()) {
                return null;
            }
            try {
                Method method = enumType.getDeclaredMethod("values", EmptyArray.CLASS);
                method.setAccessible(true);
                return (Object[]) method.invoke(null, new Object[0]);
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

    protected Enum(String name2, int ordinal2) {
        this.name = name2;
        this.ordinal = ordinal2;
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

    /* access modifiers changed from: protected */
    public final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public final int compareTo(E o) {
        Enum<?> other = o;
        if (getClass() == other.getClass() || getDeclaringClass() == other.getDeclaringClass()) {
            return this.ordinal - other.ordinal;
        }
        throw new ClassCastException();
    }

    public final Class<E> getDeclaringClass() {
        Class<?> clazz = getClass();
        Class<? super Object> superclass = clazz.getSuperclass();
        return superclass == Enum.class ? clazz : superclass;
    }

    public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name2) {
        if (enumType == null) {
            throw new NullPointerException("enumType == null");
        } else if (name2 != null) {
            T[] values = getSharedConstants(enumType);
            if (values != null) {
                for (int i = values.length - 1; i >= 0; i--) {
                    T value = values[i];
                    if (name2.equals(value.name())) {
                        return value;
                    }
                }
                throw new IllegalArgumentException("No enum constant " + enumType.getCanonicalName() + "." + name2);
            }
            throw new IllegalArgumentException(enumType.toString() + " is not an enum type.");
        } else {
            throw new NullPointerException("name == null");
        }
    }

    public static <T extends Enum<T>> T[] getSharedConstants(Class<T> enumType) {
        return (Enum[]) sharedConstantsCache.get(enumType);
    }

    /* access modifiers changed from: protected */
    public final void finalize() {
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException("can't deserialize enum");
    }

    private void readObjectNoData() throws ObjectStreamException {
        throw new InvalidObjectException("can't deserialize enum");
    }
}
