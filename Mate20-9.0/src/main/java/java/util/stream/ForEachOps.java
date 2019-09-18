package java.util.stream;

import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountedCompleter;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.Node;
import java.util.stream.Sink;

final class ForEachOps {

    static abstract class ForEachOp<T> implements TerminalOp<T, Void>, TerminalSink<T, Void> {
        private final boolean ordered;

        static final class OfDouble extends ForEachOp<Double> implements Sink.OfDouble {
            final DoubleConsumer consumer;

            OfDouble(DoubleConsumer consumer2, boolean ordered) {
                super(ordered);
                this.consumer = consumer2;
            }

            public StreamShape inputShape() {
                return StreamShape.DOUBLE_VALUE;
            }

            public void accept(double t) {
                this.consumer.accept(t);
            }
        }

        static final class OfInt extends ForEachOp<Integer> implements Sink.OfInt {
            final IntConsumer consumer;

            OfInt(IntConsumer consumer2, boolean ordered) {
                super(ordered);
                this.consumer = consumer2;
            }

            public StreamShape inputShape() {
                return StreamShape.INT_VALUE;
            }

            public void accept(int t) {
                this.consumer.accept(t);
            }
        }

        static final class OfLong extends ForEachOp<Long> implements Sink.OfLong {
            final LongConsumer consumer;

            OfLong(LongConsumer consumer2, boolean ordered) {
                super(ordered);
                this.consumer = consumer2;
            }

            public StreamShape inputShape() {
                return StreamShape.LONG_VALUE;
            }

            public void accept(long t) {
                this.consumer.accept(t);
            }
        }

        static final class OfRef<T> extends ForEachOp<T> {
            final Consumer<? super T> consumer;

            OfRef(Consumer<? super T> consumer2, boolean ordered) {
                super(ordered);
                this.consumer = consumer2;
            }

            public void accept(T t) {
                this.consumer.accept(t);
            }
        }

        protected ForEachOp(boolean ordered2) {
            this.ordered = ordered2;
        }

        public int getOpFlags() {
            if (this.ordered) {
                return 0;
            }
            return StreamOpFlag.NOT_ORDERED;
        }

        public <S> Void evaluateSequential(PipelineHelper<T> helper, Spliterator<S> spliterator) {
            return ((ForEachOp) helper.wrapAndCopyInto(this, spliterator)).get();
        }

        public <S> Void evaluateParallel(PipelineHelper<T> helper, Spliterator<S> spliterator) {
            if (this.ordered) {
                new ForEachOrderedTask(helper, spliterator, this).invoke();
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

        protected ForEachOrderedTask(PipelineHelper<T> helper2, Spliterator<S> spliterator2, Sink<T> action2) {
            super(null);
            this.helper = helper2;
            this.spliterator = spliterator2;
            this.targetSize = AbstractTask.suggestTargetSize(spliterator2.estimateSize());
            this.completionMap = new ConcurrentHashMap<>(Math.max(16, AbstractTask.LEAF_TARGET << 1));
            this.action = action2;
            this.leftPredecessor = null;
        }

        ForEachOrderedTask(ForEachOrderedTask<S, T> parent, Spliterator<S> spliterator2, ForEachOrderedTask<S, T> leftPredecessor2) {
            super(parent);
            this.helper = parent.helper;
            this.spliterator = spliterator2;
            this.targetSize = parent.targetSize;
            this.completionMap = parent.completionMap;
            this.action = parent.action;
            this.leftPredecessor = leftPredecessor2;
        }

        public final void compute() {
            doCompute(this);
        }

        private static <S, T> void doCompute(ForEachOrderedTask<S, T> task) {
            ForEachOrderedTask<S, T> taskToFork;
            Spliterator<S> rightSplit = task.spliterator;
            long sizeThreshold = task.targetSize;
            boolean forkRight = false;
            while (rightSplit.estimateSize() > sizeThreshold) {
                Spliterator<S> trySplit = rightSplit.trySplit();
                Spliterator<S> leftSplit = trySplit;
                if (trySplit == null) {
                    break;
                }
                ForEachOrderedTask<S, T> leftChild = new ForEachOrderedTask<>(task, leftSplit, task.leftPredecessor);
                ForEachOrderedTask<S, T> rightChild = new ForEachOrderedTask<>(task, rightSplit, leftChild);
                task.addToPendingCount(1);
                rightChild.addToPendingCount(1);
                task.completionMap.put(leftChild, rightChild);
                if (task.leftPredecessor != null) {
                    leftChild.addToPendingCount(1);
                    if (task.completionMap.replace(task.leftPredecessor, task, leftChild)) {
                        task.addToPendingCount(-1);
                    } else {
                        leftChild.addToPendingCount(-1);
                    }
                }
                if (forkRight) {
                    forkRight = false;
                    rightSplit = leftSplit;
                    task = leftChild;
                    taskToFork = rightChild;
                } else {
                    forkRight = true;
                    task = rightChild;
                    taskToFork = leftChild;
                }
                taskToFork.fork();
            }
            if (task.getPendingCount() > 0) {
                task.node = ((Node.Builder) task.helper.wrapAndCopyInto(task.helper.makeNodeBuilder(task.helper.exactOutputSizeIfKnown(rightSplit), $$Lambda$ForEachOps$ForEachOrderedTask$XLqga2XPr4V7tlS8H12fizIno.INSTANCE), rightSplit)).build();
                task.spliterator = null;
            }
            task.tryComplete();
        }

        static /* synthetic */ Object[] lambda$doCompute$0(int size) {
            return new Object[size];
        }

        public void onCompletion(CountedCompleter<?> countedCompleter) {
            if (this.node != null) {
                this.node.forEach(this.action);
                this.node = null;
            } else if (this.spliterator != null) {
                this.helper.wrapAndCopyInto(this.action, this.spliterator);
                this.spliterator = null;
            }
            ForEachOrderedTask<S, T> leftDescendant = this.completionMap.remove(this);
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

        ForEachTask(PipelineHelper<T> helper2, Spliterator<S> spliterator2, Sink<S> sink2) {
            super(null);
            this.sink = sink2;
            this.helper = helper2;
            this.spliterator = spliterator2;
            this.targetSize = 0;
        }

        ForEachTask(ForEachTask<S, T> parent, Spliterator<S> spliterator2) {
            super(parent);
            this.spliterator = spliterator2;
            this.sink = parent.sink;
            this.targetSize = parent.targetSize;
            this.helper = parent.helper;
        }

        public void compute() {
            ForEachTask<S, T> taskToFork;
            Spliterator<S> rightSplit = this.spliterator;
            long sizeEstimate = rightSplit.estimateSize();
            long j = this.targetSize;
            long sizeThreshold = j;
            if (j == 0) {
                long suggestTargetSize = AbstractTask.suggestTargetSize(sizeEstimate);
                sizeThreshold = suggestTargetSize;
                this.targetSize = suggestTargetSize;
            }
            boolean isShortCircuit = StreamOpFlag.SHORT_CIRCUIT.isKnown(this.helper.getStreamAndOpFlags());
            Sink<S> taskSink = this.sink;
            boolean forkRight = false;
            Spliterator<S> rightSplit2 = rightSplit;
            ForEachTask<S, T> task = this;
            while (true) {
                if (!isShortCircuit || !taskSink.cancellationRequested()) {
                    if (sizeEstimate <= sizeThreshold) {
                        break;
                    }
                    Spliterator<S> trySplit = rightSplit2.trySplit();
                    Spliterator<S> leftSplit = trySplit;
                    if (trySplit == null) {
                        break;
                    }
                    ForEachTask<S, T> leftTask = new ForEachTask<>(task, leftSplit);
                    task.addToPendingCount(1);
                    if (forkRight) {
                        forkRight = false;
                        rightSplit2 = leftSplit;
                        taskToFork = task;
                        task = leftTask;
                    } else {
                        forkRight = true;
                        taskToFork = leftTask;
                    }
                    taskToFork.fork();
                    sizeEstimate = rightSplit2.estimateSize();
                } else {
                    break;
                }
            }
            task.helper.copyInto(taskSink, rightSplit2);
            task.spliterator = null;
            task.propagateCompletion();
        }
    }

    private ForEachOps() {
    }

    public static <T> TerminalOp<T, Void> makeRef(Consumer<? super T> action, boolean ordered) {
        Objects.requireNonNull(action);
        return new ForEachOp.OfRef(action, ordered);
    }

    public static TerminalOp<Integer, Void> makeInt(IntConsumer action, boolean ordered) {
        Objects.requireNonNull(action);
        return new ForEachOp.OfInt(action, ordered);
    }

    public static TerminalOp<Long, Void> makeLong(LongConsumer action, boolean ordered) {
        Objects.requireNonNull(action);
        return new ForEachOp.OfLong(action, ordered);
    }

    public static TerminalOp<Double, Void> makeDouble(DoubleConsumer action, boolean ordered) {
        Objects.requireNonNull(action);
        return new ForEachOp.OfDouble(action, ordered);
    }
}
