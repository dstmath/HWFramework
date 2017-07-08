package android.util;

public abstract class Singleton<T> {
    private T mInstance;

    protected abstract T create();

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
