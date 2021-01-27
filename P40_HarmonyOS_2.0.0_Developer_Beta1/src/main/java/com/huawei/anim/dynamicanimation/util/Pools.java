package com.huawei.anim.dynamicanimation.util;

public class Pools {

    public interface Pool<T> {
        T acquire();

        boolean release(T t);
    }

    private Pools() {
    }

    public static class SimplePool<T> implements Pool<T> {
        private final Object[] a;
        private int b;

        public SimplePool(int i) {
            if (i > 0) {
                this.a = new Object[i];
                return;
            }
            throw new IllegalArgumentException("The max pool size must be > 0");
        }

        @Override // com.huawei.anim.dynamicanimation.util.Pools.Pool
        public T acquire() {
            int i = this.b;
            if (i <= 0) {
                return null;
            }
            int i2 = i - 1;
            Object[] objArr = this.a;
            T t = (T) objArr[i2];
            objArr[i2] = null;
            this.b = i - 1;
            return t;
        }

        @Override // com.huawei.anim.dynamicanimation.util.Pools.Pool
        public boolean release(T t) {
            if (!a(t)) {
                int i = this.b;
                Object[] objArr = this.a;
                if (i >= objArr.length) {
                    return false;
                }
                objArr[i] = t;
                this.b = i + 1;
                return true;
            }
            throw new IllegalStateException("The instance is already in the pool!");
        }

        private boolean a(T t) {
            for (int i = 0; i < this.b; i++) {
                if (this.a[i] == t) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class SynchronizedPool<T> extends SimplePool<T> {
        private final Object a = new Object();

        public SynchronizedPool(int i) {
            super(i);
        }

        @Override // com.huawei.anim.dynamicanimation.util.Pools.SimplePool, com.huawei.anim.dynamicanimation.util.Pools.Pool
        public T acquire() {
            T t;
            synchronized (this.a) {
                t = (T) super.acquire();
            }
            return t;
        }

        @Override // com.huawei.anim.dynamicanimation.util.Pools.SimplePool, com.huawei.anim.dynamicanimation.util.Pools.Pool
        public boolean release(T t) {
            boolean release;
            synchronized (this.a) {
                release = super.release(t);
            }
            return release;
        }
    }
}
