package java.util;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.Enum;

public abstract class EnumSet<E extends Enum<E>> extends AbstractSet<E> implements Cloneable, Serializable {
    /* access modifiers changed from: private */
    public static Enum<?>[] ZERO_LENGTH_ENUM_ARRAY = new Enum[0];
    final Class<E> elementType;
    final Enum<?>[] universe;

    private static class SerializationProxy<E extends Enum<E>> implements Serializable {
        private static final long serialVersionUID = 362491234563181265L;
        private final Class<E> elementType;
        private final Enum<?>[] elements;

        SerializationProxy(EnumSet<E> set) {
            this.elementType = set.elementType;
            this.elements = (Enum[]) set.toArray(EnumSet.ZERO_LENGTH_ENUM_ARRAY);
        }

        private Object readResolve() {
            EnumSet<E> result = EnumSet.noneOf(this.elementType);
            for (Enum<?> e : this.elements) {
                result.add(e);
            }
            return result;
        }
    }

    /* access modifiers changed from: package-private */
    public abstract void addAll();

    /* access modifiers changed from: package-private */
    public abstract void addRange(E e, E e2);

    /* access modifiers changed from: package-private */
    public abstract void complement();

    EnumSet(Class<E> elementType2, Enum<?>[] universe2) {
        this.elementType = elementType2;
        this.universe = universe2;
    }

    public static <E extends Enum<E>> EnumSet<E> noneOf(Class<E> elementType2) {
        Enum<?>[] universe2 = getUniverse(elementType2);
        if (universe2 == null) {
            throw new ClassCastException(elementType2 + " not an enum");
        } else if (universe2.length <= 64) {
            return new RegularEnumSet(elementType2, universe2);
        } else {
            return new JumboEnumSet(elementType2, universe2);
        }
    }

    public static <E extends Enum<E>> EnumSet<E> allOf(Class<E> elementType2) {
        EnumSet<E> result = noneOf(elementType2);
        result.addAll();
        return result;
    }

    public static <E extends Enum<E>> EnumSet<E> copyOf(EnumSet<E> s) {
        return s.clone();
    }

    public static <E extends Enum<E>> EnumSet<E> copyOf(Collection<E> c) {
        if (c instanceof EnumSet) {
            return ((EnumSet) c).clone();
        }
        if (!c.isEmpty()) {
            Iterator<E> i = c.iterator();
            EnumSet<E> result = of((Enum) i.next());
            while (i.hasNext()) {
                result.add((Enum) i.next());
            }
            return result;
        }
        throw new IllegalArgumentException("Collection is empty");
    }

    public static <E extends Enum<E>> EnumSet<E> complementOf(EnumSet<E> s) {
        EnumSet<E> result = copyOf(s);
        result.complement();
        return result;
    }

    public static <E extends Enum<E>> EnumSet<E> of(E e) {
        EnumSet<E> result = noneOf(e.getDeclaringClass());
        result.add(e);
        return result;
    }

    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2) {
        EnumSet<E> result = noneOf(e1.getDeclaringClass());
        result.add(e1);
        result.add(e2);
        return result;
    }

    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3) {
        EnumSet<E> result = noneOf(e1.getDeclaringClass());
        result.add(e1);
        result.add(e2);
        result.add(e3);
        return result;
    }

    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3, E e4) {
        EnumSet<E> result = noneOf(e1.getDeclaringClass());
        result.add(e1);
        result.add(e2);
        result.add(e3);
        result.add(e4);
        return result;
    }

    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3, E e4, E e5) {
        EnumSet<E> result = noneOf(e1.getDeclaringClass());
        result.add(e1);
        result.add(e2);
        result.add(e3);
        result.add(e4);
        result.add(e5);
        return result;
    }

    @SafeVarargs
    public static <E extends Enum<E>> EnumSet<E> of(E first, E... rest) {
        EnumSet<E> result = noneOf(first.getDeclaringClass());
        result.add(first);
        for (E e : rest) {
            result.add(e);
        }
        return result;
    }

    public static <E extends Enum<E>> EnumSet<E> range(E from, E to) {
        if (from.compareTo(to) <= 0) {
            EnumSet<E> result = noneOf(from.getDeclaringClass());
            result.addRange(from, to);
            return result;
        }
        throw new IllegalArgumentException(from + " > " + to);
    }

    public EnumSet<E> clone() {
        try {
            return (EnumSet) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError((Object) e);
        }
    }

    /* access modifiers changed from: package-private */
    public final void typeCheck(E e) {
        Class<?> eClass = e.getClass();
        if (eClass != this.elementType && eClass.getSuperclass() != this.elementType) {
            throw new ClassCastException(eClass + " != " + this.elementType);
        }
    }

    private static <E extends Enum<E>> E[] getUniverse(Class<E> elementType2) {
        return (Enum[]) elementType2.getEnumConstantsShared();
    }

    /* access modifiers changed from: package-private */
    public Object writeReplace() {
        return new SerializationProxy(this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
}
