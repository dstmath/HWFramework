package ohos.global.icu.impl;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import ohos.global.icu.util.ICUException;

public abstract class CacheValue<V> {
    private static final CacheValue NULL_VALUE = new NullValue();
    private static volatile Strength strength = Strength.SOFT;

    public enum Strength {
        STRONG,
        SOFT
    }

    public abstract V get();

    public boolean isNull() {
        return false;
    }

    public abstract V resetIfCleared(V v);

    public static void setStrength(Strength strength2) {
        strength = strength2;
    }

    public static boolean futureInstancesWillBeStrong() {
        return strength == Strength.STRONG;
    }

    public static <V> CacheValue<V> getInstance(V v) {
        if (v == null) {
            return NULL_VALUE;
        }
        return strength == Strength.STRONG ? new StrongValue(v) : new SoftValue(v);
    }

    private static final class NullValue<V> extends CacheValue<V> {
        @Override // ohos.global.icu.impl.CacheValue
        public V get() {
            return null;
        }

        @Override // ohos.global.icu.impl.CacheValue
        public boolean isNull() {
            return true;
        }

        private NullValue() {
        }

        @Override // ohos.global.icu.impl.CacheValue
        public V resetIfCleared(V v) {
            if (v == null) {
                return null;
            }
            throw new ICUException("resetting a null value to a non-null value");
        }
    }

    private static final class StrongValue<V> extends CacheValue<V> {
        private V value;

        StrongValue(V v) {
            this.value = v;
        }

        @Override // ohos.global.icu.impl.CacheValue
        public V get() {
            return this.value;
        }

        @Override // ohos.global.icu.impl.CacheValue
        public V resetIfCleared(V v) {
            return this.value;
        }
    }

    private static final class SoftValue<V> extends CacheValue<V> {
        private volatile Reference<V> ref;

        SoftValue(V v) {
            this.ref = new SoftReference(v);
        }

        @Override // ohos.global.icu.impl.CacheValue
        public V get() {
            return this.ref.get();
        }

        @Override // ohos.global.icu.impl.CacheValue
        public synchronized V resetIfCleared(V v) {
            V v2 = this.ref.get();
            if (v2 != null) {
                return v2;
            }
            this.ref = new SoftReference(v);
            return v;
        }
    }
}
