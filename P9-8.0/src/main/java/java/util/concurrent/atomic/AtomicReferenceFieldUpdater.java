package java.util.concurrent.atomic;

import dalvik.system.VMStack;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import sun.misc.Unsafe;
import sun.reflect.CallerSensitive;

public abstract class AtomicReferenceFieldUpdater<T, V> {

    private static final class AtomicReferenceFieldUpdaterImpl<T, V> extends AtomicReferenceFieldUpdater<T, V> {
        private static final Unsafe U = Unsafe.getUnsafe();
        private final Class<?> cclass;
        private final long offset;
        private final Class<T> tclass;
        private final Class<V> vclass;

        AtomicReferenceFieldUpdaterImpl(Class<T> tclass, Class<V> vclass, String fieldName, Class<?> caller) {
            try {
                Field field = tclass.getDeclaredField(fieldName);
                int modifiers = field.getModifiers();
                if (vclass != field.getType()) {
                    throw new ClassCastException();
                } else if (vclass.isPrimitive()) {
                    throw new IllegalArgumentException("Must be reference type");
                } else if (Modifier.isVolatile(modifiers)) {
                    if (!Modifier.isProtected(modifiers)) {
                        caller = tclass;
                    }
                    this.cclass = caller;
                    this.tclass = tclass;
                    this.vclass = vclass;
                    this.offset = U.objectFieldOffset(field);
                } else {
                    throw new IllegalArgumentException("Must be volatile type");
                }
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }

        private final void accessCheck(T obj) {
            if (!this.cclass.isInstance(obj)) {
                throwAccessCheckException(obj);
            }
        }

        private final void throwAccessCheckException(T obj) {
            if (this.cclass == this.tclass) {
                throw new ClassCastException();
            }
            throw new RuntimeException(new IllegalAccessException("Class " + this.cclass.getName() + " can not access a protected member of class " + this.tclass.getName() + " using an instance of " + obj.getClass().getName()));
        }

        private final void valueCheck(V v) {
            if (v != null && (this.vclass.isInstance(v) ^ 1) != 0) {
                throwCCE();
            }
        }

        static void throwCCE() {
            throw new ClassCastException();
        }

        public final boolean compareAndSet(T obj, V expect, V update) {
            accessCheck(obj);
            valueCheck(update);
            return U.compareAndSwapObject(obj, this.offset, expect, update);
        }

        public final boolean weakCompareAndSet(T obj, V expect, V update) {
            accessCheck(obj);
            valueCheck(update);
            return U.compareAndSwapObject(obj, this.offset, expect, update);
        }

        public final void set(T obj, V newValue) {
            accessCheck(obj);
            valueCheck(newValue);
            U.putObjectVolatile(obj, this.offset, newValue);
        }

        public final void lazySet(T obj, V newValue) {
            accessCheck(obj);
            valueCheck(newValue);
            U.putOrderedObject(obj, this.offset, newValue);
        }

        public final V get(T obj) {
            accessCheck(obj);
            return U.getObjectVolatile(obj, this.offset);
        }

        public final V getAndSet(T obj, V newValue) {
            accessCheck(obj);
            valueCheck(newValue);
            return U.getAndSetObject(obj, this.offset, newValue);
        }
    }

    public abstract boolean compareAndSet(T t, V v, V v2);

    public abstract V get(T t);

    public abstract void lazySet(T t, V v);

    public abstract void set(T t, V v);

    public abstract boolean weakCompareAndSet(T t, V v, V v2);

    @CallerSensitive
    public static <U, W> AtomicReferenceFieldUpdater<U, W> newUpdater(Class<U> tclass, Class<W> vclass, String fieldName) {
        return new AtomicReferenceFieldUpdaterImpl(tclass, vclass, fieldName, VMStack.getStackClass1());
    }

    protected AtomicReferenceFieldUpdater() {
    }

    public V getAndSet(T obj, V newValue) {
        V prev;
        do {
            prev = get(obj);
        } while (!compareAndSet(obj, prev, newValue));
        return prev;
    }

    public final V getAndUpdate(T obj, UnaryOperator<V> updateFunction) {
        V prev;
        do {
            prev = get(obj);
        } while (!compareAndSet(obj, prev, updateFunction.apply(prev)));
        return prev;
    }

    public final V updateAndGet(T obj, UnaryOperator<V> updateFunction) {
        V next;
        V prev;
        do {
            prev = get(obj);
            next = updateFunction.apply(prev);
        } while (!compareAndSet(obj, prev, next));
        return next;
    }

    public final V getAndAccumulate(T obj, V x, BinaryOperator<V> accumulatorFunction) {
        V prev;
        do {
            prev = get(obj);
        } while (!compareAndSet(obj, prev, accumulatorFunction.apply(prev, x)));
        return prev;
    }

    public final V accumulateAndGet(T obj, V x, BinaryOperator<V> accumulatorFunction) {
        V next;
        V prev;
        do {
            prev = get(obj);
            next = accumulatorFunction.apply(prev, x);
        } while (!compareAndSet(obj, prev, next));
        return next;
    }
}
