package java.util.stream;

import java.util.Spliterator;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;

abstract class AbstractTask<P_IN, P_OUT, R, K extends AbstractTask<P_IN, P_OUT, R, K>> extends CountedCompleter<R> {
    static final int LEAF_TARGET = (ForkJoinPool.getCommonPoolParallelism() << 2);
    protected final PipelineHelper<P_OUT> helper;
    protected K leftChild;
    private R localResult;
    protected K rightChild;
    protected Spliterator<P_IN> spliterator;
    protected long targetSize;

    protected abstract R doLeaf();

    protected abstract K makeChild(Spliterator<P_IN> spliterator);

    protected AbstractTask(PipelineHelper<P_OUT> helper, Spliterator<P_IN> spliterator) {
        super(null);
        this.helper = helper;
        this.spliterator = spliterator;
        this.targetSize = 0;
    }

    protected AbstractTask(K parent, Spliterator<P_IN> spliterator) {
        super(parent);
        this.spliterator = spliterator;
        this.helper = parent.helper;
        this.targetSize = parent.targetSize;
    }

    public static long suggestTargetSize(long sizeEstimate) {
        long est = sizeEstimate / ((long) LEAF_TARGET);
        return est > 0 ? est : 1;
    }

    protected final long getTargetSize(long sizeEstimate) {
        long s = this.targetSize;
        if (s != 0) {
            return s;
        }
        s = suggestTargetSize(sizeEstimate);
        this.targetSize = s;
        return s;
    }

    public R getRawResult() {
        return this.localResult;
    }

    protected void setRawResult(R result) {
        if (result != null) {
            throw new IllegalStateException();
        }
    }

    protected R getLocalResult() {
        return this.localResult;
    }

    protected void setLocalResult(R localResult) {
        this.localResult = localResult;
    }

    protected boolean isLeaf() {
        return this.leftChild == null;
    }

    protected boolean isRoot() {
        return getParent() == null;
    }

    protected K getParent() {
        return (AbstractTask) getCompleter();
    }

    public void compute() {
        Spliterator<P_IN> rs = this.spliterator;
        long sizeEstimate = rs.estimateSize();
        long sizeThreshold = getTargetSize(sizeEstimate);
        boolean forkRight = false;
        K task = this;
        while (sizeEstimate > sizeThreshold) {
            Spliterator<P_IN> ls = rs.trySplit();
            if (ls == null) {
                break;
            }
            K taskToFork;
            K leftChild = task.makeChild(ls);
            task.leftChild = leftChild;
            K rightChild = task.makeChild(rs);
            task.rightChild = rightChild;
            task.setPendingCount(1);
            if (forkRight) {
                forkRight = false;
                rs = ls;
                task = leftChild;
                taskToFork = rightChild;
            } else {
                forkRight = true;
                task = rightChild;
                taskToFork = leftChild;
            }
            taskToFork.fork();
            sizeEstimate = rs.estimateSize();
        }
        task.setLocalResult(task.doLeaf());
        task.tryComplete();
    }

    public void onCompletion(CountedCompleter<?> countedCompleter) {
        this.spliterator = null;
        this.rightChild = null;
        this.leftChild = null;
    }

    protected boolean isLeftmostNode() {
        K node = this;
        while (node != null) {
            K parent = node.getParent();
            if (parent != null && parent.leftChild != node) {
                return false;
            }
            node = parent;
        }
        return true;
    }
}
