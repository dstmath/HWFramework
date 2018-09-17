package java.util.stream;

import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicReference;

abstract class AbstractShortCircuitTask<P_IN, P_OUT, R, K extends AbstractShortCircuitTask<P_IN, P_OUT, R, K>> extends AbstractTask<P_IN, P_OUT, R, K> {
    protected volatile boolean canceled;
    protected final AtomicReference<R> sharedResult;

    protected abstract R getEmptyResult();

    protected AbstractShortCircuitTask(PipelineHelper<P_OUT> helper, Spliterator<P_IN> spliterator) {
        super((PipelineHelper) helper, (Spliterator) spliterator);
        this.sharedResult = new AtomicReference(null);
    }

    protected AbstractShortCircuitTask(K parent, Spliterator<P_IN> spliterator) {
        super((AbstractTask) parent, (Spliterator) spliterator);
        this.sharedResult = parent.sharedResult;
    }

    /* JADX WARNING: Missing block: B:12:0x002f, code:
            r3 = r11.doLeaf();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void compute() {
        R result;
        Spliterator<P_IN> rs = this.spliterator;
        long sizeEstimate = rs.estimateSize();
        long sizeThreshold = getTargetSize(sizeEstimate);
        boolean forkRight = false;
        K task = this;
        AtomicReference<R> sr = this.sharedResult;
        while (true) {
            result = sr.get();
            if (result == null) {
                if (!task.taskCanceled()) {
                    if (sizeEstimate <= sizeThreshold) {
                        break;
                    }
                    Spliterator<P_IN> ls = rs.trySplit();
                    if (ls == null) {
                        break;
                    }
                    K taskToFork;
                    K leftChild = (AbstractShortCircuitTask) task.makeChild(ls);
                    task.leftChild = leftChild;
                    K rightChild = (AbstractShortCircuitTask) task.makeChild(rs);
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
                } else {
                    result = task.getEmptyResult();
                    break;
                }
            }
            break;
        }
        task.setLocalResult(result);
        task.tryComplete();
    }

    protected void shortCircuit(R result) {
        if (result != null) {
            this.sharedResult.compareAndSet(null, result);
        }
    }

    protected void setLocalResult(R localResult) {
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
        if (answer == null) {
            answer = getEmptyResult();
        }
        return answer;
    }

    protected void cancel() {
        this.canceled = true;
    }

    protected boolean taskCanceled() {
        boolean cancel = this.canceled;
        if (!cancel) {
            K parent = (AbstractShortCircuitTask) getParent();
            while (!cancel && parent != null) {
                cancel = parent.canceled;
                AbstractShortCircuitTask parent2 = (AbstractShortCircuitTask) parent2.getParent();
            }
        }
        return cancel;
    }

    protected void cancelLaterNodes() {
        K node = this;
        for (K parent = (AbstractShortCircuitTask) getParent(); parent != null; AbstractShortCircuitTask parent2 = (AbstractShortCircuitTask) parent2.getParent()) {
            if (parent2.leftChild == node) {
                AbstractShortCircuitTask rightSibling = (AbstractShortCircuitTask) parent2.rightChild;
                if (!rightSibling.canceled) {
                    rightSibling.cancel();
                }
            }
            node = parent2;
        }
    }
}
