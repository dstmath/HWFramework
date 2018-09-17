package java.util.stream;

import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountedCompleter;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.Node.Builder;

final class ForEachOps {

    static abstract class ForEachOp<T> implements TerminalOp<T, Void>, TerminalSink<T, Void> {
        private final boolean ordered;

        static final class OfDouble extends ForEachOp<Double> implements java.util.stream.Sink.OfDouble {
            final DoubleConsumer consumer;

            OfDouble(DoubleConsumer consumer, boolean ordered) {
                super(ordered);
                this.consumer = consumer;
            }

            public StreamShape inputShape() {
                return StreamShape.DOUBLE_VALUE;
            }

            public void accept(double t) {
                this.consumer.-java_util_stream_StreamSpliterators$DoubleWrappingSpliterator-mthref-1(t);
            }
        }

        static final class OfInt extends ForEachOp<Integer> implements java.util.stream.Sink.OfInt {
            final IntConsumer consumer;

            OfInt(IntConsumer consumer, boolean ordered) {
                super(ordered);
                this.consumer = consumer;
            }

            public StreamShape inputShape() {
                return StreamShape.INT_VALUE;
            }

            public void accept(int t) {
                this.consumer.-java_util_stream_StreamSpliterators$IntWrappingSpliterator-mthref-1(t);
            }
        }

        static final class OfLong extends ForEachOp<Long> implements java.util.stream.Sink.OfLong {
            final LongConsumer consumer;

            OfLong(LongConsumer consumer, boolean ordered) {
                super(ordered);
                this.consumer = consumer;
            }

            public StreamShape inputShape() {
                return StreamShape.LONG_VALUE;
            }

            public void accept(long t) {
                this.consumer.-java_util_stream_StreamSpliterators$LongWrappingSpliterator-mthref-1(t);
            }
        }

        static final class OfRef<T> extends ForEachOp<T> {
            final Consumer<? super T> consumer;

            OfRef(Consumer<? super T> consumer, boolean ordered) {
                super(ordered);
                this.consumer = consumer;
            }

            public void accept(T t) {
                this.consumer.-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-1(t);
            }
        }

        protected ForEachOp(boolean ordered) {
            this.ordered = ordered;
        }

        public int getOpFlags() {
            return this.ordered ? 0 : StreamOpFlag.NOT_ORDERED;
        }

        public <S> Void evaluateSequential(PipelineHelper<T> helper, Spliterator<S> spliterator) {
            return ((ForEachOp) helper.wrapAndCopyInto(this, spliterator)).get();
        }

        public <S> Void evaluateParallel(PipelineHelper<T> helper, Spliterator<S> spliterator) {
            if (this.ordered) {
                new ForEachOrderedTask((PipelineHelper) helper, (Spliterator) spliterator, (Sink) this).invoke();
            } else {
                new ForEachTask(helper, spliterator, helper.wrapSink(this)).invoke();
            }
            return null;
        }

        public Void get() {
            return null;
        }
    }

    static final class ForEachOrderedTask<S, T> extends CountedCompleter<Void> {
        private final Sink<T> action;
        private final ConcurrentHashMap<ForEachOrderedTask<S, T>, ForEachOrderedTask<S, T>> completionMap;
        private final PipelineHelper<T> helper;
        private final ForEachOrderedTask<S, T> leftPredecessor;
        private Node<T> node;
        private Spliterator<S> spliterator;
        private final long targetSize;

        protected ForEachOrderedTask(PipelineHelper<T> helper, Spliterator<S> spliterator, Sink<T> action) {
            super(null);
            this.helper = helper;
            this.spliterator = spliterator;
            this.targetSize = AbstractTask.suggestTargetSize(spliterator.estimateSize());
            this.completionMap = new ConcurrentHashMap(Math.max(16, AbstractTask.LEAF_TARGET << 1));
            this.action = action;
            this.leftPredecessor = null;
        }

        ForEachOrderedTask(ForEachOrderedTask<S, T> parent, Spliterator<S> spliterator, ForEachOrderedTask<S, T> leftPredecessor) {
            super(parent);
            this.helper = parent.helper;
            this.spliterator = spliterator;
            this.targetSize = parent.targetSize;
            this.completionMap = parent.completionMap;
            this.action = parent.action;
            this.leftPredecessor = leftPredecessor;
        }

        public final void compute() {
            doCompute(this);
        }

        private static <S, T> void doCompute(ForEachOrderedTask<S, T> task) {
            ForEachOrderedTask task2;
            Spliterator<S> rightSplit = task2.spliterator;
            long sizeThreshold = task2.targetSize;
            boolean forkRight = false;
            while (rightSplit.estimateSize() > sizeThreshold) {
                Spliterator<S> leftSplit = rightSplit.trySplit();
                if (leftSplit == null) {
                    break;
                }
                ForEachOrderedTask<S, T> taskToFork;
                ForEachOrderedTask<S, T> leftChild = new ForEachOrderedTask(task2, (Spliterator) leftSplit, task2.leftPredecessor);
                ForEachOrderedTask<S, T> rightChild = new ForEachOrderedTask(task2, (Spliterator) rightSplit, (ForEachOrderedTask) leftChild);
                task2.addToPendingCount(1);
                rightChild.addToPendingCount(1);
                task2.completionMap.put(leftChild, rightChild);
                if (task2.leftPredecessor != null) {
                    leftChild.addToPendingCount(1);
                    if (task2.completionMap.replace(task2.leftPredecessor, task2, leftChild)) {
                        task2.addToPendingCount(-1);
                    } else {
                        leftChild.addToPendingCount(-1);
                    }
                }
                if (forkRight) {
                    forkRight = false;
                    rightSplit = leftSplit;
                    task2 = leftChild;
                    taskToFork = rightChild;
                } else {
                    forkRight = true;
                    task2 = rightChild;
                    taskToFork = leftChild;
                }
                taskToFork.fork();
            }
            if (task2.getPendingCount() > 0) {
                task2.node = ((Builder) task2.helper.wrapAndCopyInto(task2.helper.makeNodeBuilder(task2.helper.exactOutputSizeIfKnown(rightSplit), new -$Lambda$uBnxRm2phNSynUZZf0YtswswJRk()), rightSplit)).build();
                task2.spliterator = null;
            }
            task2.tryComplete();
        }

        public void onCompletion(CountedCompleter<?> countedCompleter) {
            if (this.node != null) {
                this.node.forEach(this.action);
                this.node = null;
            } else if (this.spliterator != null) {
                this.helper.wrapAndCopyInto(this.action, this.spliterator);
                this.spliterator = null;
            }
            ForEachOrderedTask<S, T> leftDescendant = (ForEachOrderedTask) this.completionMap.remove(this);
            if (leftDescendant != null) {
                leftDescendant.tryComplete();
            }
        }
    }

    static final class ForEachTask<S, T> extends CountedCompleter<Void> {
        private final PipelineHelper<T> helper;
        private final Sink<S> sink;
        private Spliterator<S> spliterator;
        private long targetSize;

        ForEachTask(PipelineHelper<T> helper, Spliterator<S> spliterator, Sink<S> sink) {
            super(null);
            this.sink = sink;
            this.helper = helper;
            this.spliterator = spliterator;
            this.targetSize = 0;
        }

        ForEachTask(ForEachTask<S, T> parent, Spliterator<S> spliterator) {
            super(parent);
            this.spliterator = spliterator;
            this.sink = parent.sink;
            this.targetSize = parent.targetSize;
            this.helper = parent.helper;
        }

        public void compute() {
            Spliterator<S> rightSplit = this.spliterator;
            long sizeEstimate = rightSplit.estimateSize();
            long sizeThreshold = this.targetSize;
            if (sizeThreshold == 0) {
                sizeThreshold = AbstractTask.suggestTargetSize(sizeEstimate);
                this.targetSize = sizeThreshold;
            }
            boolean isShortCircuit = StreamOpFlag.SHORT_CIRCUIT.isKnown(this.helper.getStreamAndOpFlags());
            boolean forkRight = false;
            Sink<S> taskSink = this.sink;
            ForEachTask<S, T> task = this;
            while (true) {
                if (!isShortCircuit || (taskSink.cancellationRequested() ^ 1) != 0) {
                    if (sizeEstimate <= sizeThreshold) {
                        break;
                    }
                    Spliterator<S> leftSplit = rightSplit.trySplit();
                    if (leftSplit == null) {
                        break;
                    }
                    ForEachTask<S, T> taskToFork;
                    ForEachTask<S, T> leftTask = new ForEachTask(task, leftSplit);
                    task.addToPendingCount(1);
                    if (forkRight) {
                        forkRight = false;
                        rightSplit = leftSplit;
                        taskToFork = task;
                    } else {
                        forkRight = true;
                    }
                    task = leftTask;
                    taskToFork.fork();
                    sizeEstimate = rightSplit.estimateSize();
                } else {
                    break;
                }
            }
            task.helper.copyInto(taskSink, rightSplit);
            task.spliterator = null;
            task.propagateCompletion();
        }
    }

    private ForEachOps() {
    }

    public static <T> TerminalOp<T, Void> makeRef(Consumer<? super T> action, boolean ordered) {
        Objects.requireNonNull(action);
        return new OfRef(action, ordered);
    }

    public static TerminalOp<Integer, Void> makeInt(IntConsumer action, boolean ordered) {
        Objects.requireNonNull(action);
        return new OfInt(action, ordered);
    }

    public static TerminalOp<Long, Void> makeLong(LongConsumer action, boolean ordered) {
        Objects.requireNonNull(action);
        return new OfLong(action, ordered);
    }

    public static TerminalOp<Double, Void> makeDouble(DoubleConsumer action, boolean ordered) {
        Objects.requireNonNull(action);
        return new OfDouble(action, ordered);
    }
}
