package java.util.concurrent.atomic;

import dalvik.system.VMStack;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import sun.misc.Unsafe;
import sun.reflect.CallerSensitive;

public abstract class AtomicIntegerFieldUpdater<T> {

    private static final class AtomicIntegerFieldUpdaterImpl<T> extends AtomicIntegerFieldUpdater<T> {
        private static final Unsafe U = Unsafe.getUnsafe();
        private final Class<?> cclass;
        private final long offset;
        private final Class<T> tclass;

        AtomicIntegerFieldUpdaterImpl(final Class<T> tclass, final String fieldName, Class<?> caller) {
            try {
                Field field = (Field) AccessController.doPrivileged(new PrivilegedExceptionAction<Field>() {
                    public Field run() throws NoSuchFieldException {
                        return tclass.getDeclaredField(fieldName);
                    }
                });
                int modifiers = field.getModifiers();
                if (field.getType() != Integer.TYPE) {
                    throw new IllegalArgumentException("Must be integer type");
                } else if (Modifier.isVolatile(modifiers)) {
                    if (!Modifier.isProtected(modifiers)) {
                        caller = tclass;
                    }
                    this.cclass = caller;
                    this.tclass = tclass;
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

        public final boolean compareAndSet(T obj, int expect, int update) {
            accessCheck(obj);
            return U.compareAndSwapInt(obj, this.offset, expect, update);
        }

        public final boolean weakCompareAndSet(T obj, int expect, int update) {
            accessCheck(obj);
            return U.compareAndSwapInt(obj, this.offset, expect, update);
        }

        public final void set(T obj, int newValue) {
            accessCheck(obj);
            U.putIntVolatile(obj, this.offset, newValue);
        }

        public final void lazySet(T obj, int newValue) {
            accessCheck(obj);
            U.putOrderedInt(obj, this.offset, newValue);
        }

        public final int get(T obj) {
            accessCheck(obj);
            return U.getIntVolatile(obj, this.offset);
        }

        public final int getAndSet(T obj, int newValue) {
            accessCheck(obj);
            return U.getAndSetInt(obj, this.offset, newValue);
        }

        public final int getAndAdd(T obj, int delta) {
            accessCheck(obj);
            return U.getAndAddInt(obj, this.offset, delta);
        }

        public final int getAndIncrement(T obj) {
            return getAndAdd(obj, 1);
        }

        public final int getAndDecrement(T obj) {
            return getAndAdd(obj, -1);
        }

        public final int incrementAndGet(T obj) {
            return getAndAdd(obj, 1) + 1;
        }

        public final int decrementAndGet(T obj) {
            return getAndAdd(obj, -1) - 1;
        }

        public final int addAndGet(T obj, int delta) {
            return getAndAdd(obj, delta) + delta;
        }
    }

    public abstract boolean compareAndSet(T t, int i, int i2);

    public abstract int get(T t);

    public abstract void lazySet(T t, int i);

    public abstract void set(T t, int i);

    public abstract boolean weakCompareAndSet(T t, int i, int i2);

    @CallerSensitive
    public static <U> AtomicIntegerFieldUpdater<U> newUpdater(Class<U> tclass, String fieldName) {
        return new AtomicIntegerFieldUpdaterImpl(tclass, fieldName, VMStack.getStackClass1());
    }

    protected AtomicIntegerFieldUpdater() {
    }

    public int getAndSet(T obj, int newValue) {
        int prev;
        do {
            prev = get(obj);
        } while (!compareAndSet(obj, prev, newValue));
        return prev;
    }

    public int getAndIncrement(T obj) {
        int prev;
        do {
            prev = get(obj);
        } while (!compareAndSet(obj, prev, prev + 1));
        return prev;
    }

    public int getAndDecrement(T obj) {
        int prev;
        do {
            prev = get(obj);
        } while (!compareAndSet(obj, prev, prev - 1));
        return prev;
    }

    public int getAndAdd(T obj, int delta) {
        int prev;
        do {
            prev = get(obj);
        } while (!compareAndSet(obj, prev, prev + delta));
        return prev;
    }

    public int incrementAndGet(T obj) {
        int next;
        int prev;
        do {
            prev = get(obj);
            next = prev + 1;
        } while (!compareAndSet(obj, prev, next));
        return next;
    }

    public int decrementAndGet(T obj) {
        int next;
        int prev;
        do {
            prev = get(obj);
            next = prev - 1;
        } while (!compareAndSet(obj, prev, next));
        return next;
    }

    public int addAndGet(T obj, int delta) {
        int next;
        int prev;
        do {
            prev = get(obj);
            next = prev + delta;
        } while (!compareAndSet(obj, prev, next));
        return next;
    }

    public final int getAndUpdate(T obj, IntUnaryOperator updateFunction) {
        int prev;
        do {
            prev = get(obj);
        } while (!compareAndSet(obj, prev, updateFunction.applyAsInt(prev)));
        return prev;
    }

    public final int updateAndGet(T obj, IntUnaryOperator updateFunction) {
        int next;
        int prev;
        do {
            prev = get(obj);
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSet(obj, prev, next));
        return next;
    }

    public final int getAndAccumulate(T obj, int x, IntBinaryOperator accumulatorFunction) {
        int prev;
        do {
            prev = get(obj);
        } while (!compareAndSet(obj, prev, accumulatorFunction.applyAsInt(prev, x)));
        return prev;
    }

    public final int accumulateAndGet(T obj, int x, IntBinaryOperator accumulatorFunction) {
        int next;
        int prev;
        do {
            prev = get(obj);
            next = accumulatorFunction.applyAsInt(prev, x);
        } while (!compareAndSet(obj, prev, next));
        return next;
    }
}
