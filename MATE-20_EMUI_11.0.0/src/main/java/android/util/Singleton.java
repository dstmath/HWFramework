package android.util;

import android.annotation.UnsupportedAppUsage;

public abstract class Singleton<T> {
    @UnsupportedAppUsage
    private T mInstance;

    /* access modifiers changed from: protected */
    public abstract T create();

    @UnsupportedAppUsage
    public final T get() {
        T t;
        synchronized (this) {
            if (this.mInstance == null) {
                this.mInstance = create();
            }
            t = this.mInstance;
        }
        return t;
    }
}
