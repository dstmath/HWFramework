package com.huawei.odmf.utils;

public abstract class Singleton<T> {
    private volatile T mInstance;

    /* access modifiers changed from: protected */
    public abstract T create();

    public final T get() {
        T t;
        if (this.mInstance != null) {
            return this.mInstance;
        }
        synchronized (this) {
            if (this.mInstance == null) {
                this.mInstance = create();
            }
            t = this.mInstance;
        }
        return t;
    }
}
