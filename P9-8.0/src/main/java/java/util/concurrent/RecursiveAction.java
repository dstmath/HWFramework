package java.util.concurrent;

public abstract class RecursiveAction extends ForkJoinTask<Void> {
    private static final long serialVersionUID = 5232453952276485070L;

    protected abstract void compute();

    public final Void getRawResult() {
        return null;
    }

    protected final void setRawResult(Void mustBeNull) {
    }

    protected final boolean exec() {
        compute();
        return true;
    }
}
