package java.util.stream;

import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.AbstractShortCircuitTask;

abstract class AbstractShortCircuitTask<P_IN, P_OUT, R, K extends AbstractShortCircuitTask<P_IN, P_OUT, R, K>> extends AbstractTask<P_IN, P_OUT, R, K> {
    protected volatile boolean canceled;
    protected final AtomicReference<R> sharedResult;

    /* access modifiers changed from: protected */
    public abstract R getEmptyResult();

    protected AbstractShortCircuitTask(PipelineHelper<P_OUT> helper, Spliterator<P_IN> spliterator) {
        super(helper, spliterator);
        this.sharedResult = new AtomicReference<>(null);
    }

    protected AbstractShortCircuitTask(K parent, Spliterator<P_IN> spliterator) {
        super(parent, spliterator);
        this.sharedResult = parent.sharedResult;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0055, code lost:
        r9 = r6.doLeaf();
     */
    public void compute() {
        R result;
        K taskToFork;
        Spliterator<P_IN> rs = this.spliterator;
        long sizeEstimate = rs.estimateSize();
        long sizeThreshold = getTargetSize(sizeEstimate);
        boolean forkRight = false;
        K task = this;
        AtomicReference<R> sr = this.sharedResult;
        while (true) {
            R r = sr.get();
            result = r;
            if (r == null) {
                if (!task.taskCanceled()) {
                    if (sizeEstimate <= sizeThreshold) {
                        break;
                    }
                    Spliterator<P_IN> trySplit = rs.trySplit();
                    Spliterator<P_IN> ls = trySplit;
                    if (trySplit == null) {
                        break;
                    }
                    K k = (AbstractShortCircuitTask) task.makeChild(ls);
                    K leftChild = k;
                    task.leftChild = k;
                    K k2 = (AbstractShortCircuitTask) task.makeChild(rs);
                    K rightChild = k2;
                    task.rightChild = k2;
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
                } else {
                    result = task.getEmptyResult();
                    break;
                }
            } else {
                break;
            }
        }
        task.setLocalResult(result);
        task.tryComplete();
    }

    /* access modifiers changed from: protected */
    public void shortCircuit(R result) {
        if (result != null) {
            this.sharedResult.compareAndSet(null, result);
        }
    }

    /* access modifiers changed from: protected */
    public void setLocalResult(R localResult) {
        if (!isRoot()) {
            super.setLocalResult(localResult);
        } else if (localResult != null) {
            this.sharedResult.compareAndSet(null, localResult);
        }
    }

    public R getRawResult() {
        return getLocalResult();
    }

    public R getLocalResult() {
        if (!isRoot()) {
            return super.getLocalResult();
        }
        R answer = this.sharedResult.get();
        return answer == null ? getEmptyResult() : answer;
    }

    /* access modifiers changed from: protected */
    public void cancel() {
        this.canceled = true;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    public boolean taskCanceled() {
        boolean cancel = this.canceled;
        if (!cancel) {
            K parent = (AbstractShortCircuitTask) getParent();
            while (!cancel && parent != null) {
                cancel = parent.canceled;
                parent = (AbstractShortCircuitTask) parent.getParent();
            }
        }
        return cancel;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    public void cancelLaterNodes() {
        K node = this;
        for (K parent = (AbstractShortCircuitTask) getParent(); parent != null; parent = (AbstractShortCircuitTask) parent.getParent()) {
            if (parent.leftChild == node) {
                K rightSibling = (AbstractShortCircuitTask) parent.rightChild;
                if (!rightSibling.canceled) {
                    rightSibling.cancel();
                }
            }
            node = parent;
        }
    }
}
