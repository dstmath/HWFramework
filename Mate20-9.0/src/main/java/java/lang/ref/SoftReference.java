package java.lang.ref;

public class SoftReference<T> extends Reference<T> {
    private static long clock;
    private long timestamp = clock;

    public SoftReference(T referent) {
        super(referent);
    }

    public SoftReference(T referent, ReferenceQueue<? super T> q) {
        super(referent, q);
    }

    public T get() {
        T o = super.get();
        if (!(o == null || this.timestamp == clock)) {
            this.timestamp = clock;
        }
        return o;
    }
}
