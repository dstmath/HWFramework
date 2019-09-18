package java.util.concurrent;

public abstract class RecursiveTask<V> extends ForkJoinTask<V> {
    private static final long serialVersionUID = 5232453952276485270L;
    V result;

    /* access modifiers changed from: protected */
    public abstract V compute();

    public final V getRawResult() {
        return this.result;
    }

    /* access modifiers changed from: protected */
    public final void setRawResult(V value) {
        this.result = value;
    }

    /* access modifiers changed from: protected */
    public final boolean exec() {
        this.result = compute();
        return true;
    }
}
