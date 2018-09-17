package java.util.stream;

import java.util.Spliterator;
import java.util.Spliterator.OfDouble;
import java.util.Spliterator.OfInt;
import java.util.Spliterator.OfLong;
import java.util.concurrent.CountedCompleter;
import java.util.function.IntFunction;
import java.util.stream.Node.Builder;
import java.util.stream.ReferencePipeline.StatefulOp;
import java.util.stream.Sink.ChainedDouble;
import java.util.stream.Sink.ChainedInt;
import java.util.stream.Sink.ChainedLong;
import java.util.stream.Sink.ChainedReference;

final class SliceOps {
    static final /* synthetic */ boolean -assertionsDisabled = (SliceOps.class.desiredAssertionStatus() ^ 1);
    private static final /* synthetic */ int[] -java-util-stream-StreamShapeSwitchesValues = null;

    private static final class SliceTask<P_IN, P_OUT> extends AbstractShortCircuitTask<P_IN, P_OUT, Node<P_OUT>, SliceTask<P_IN, P_OUT>> {
        private volatile boolean completed;
        private final IntFunction<P_OUT[]> generator;
        private final AbstractPipeline<P_OUT, P_OUT, ?> op;
        private final long targetOffset;
        private final long targetSize;
        private long thisNodeSize;

        SliceTask(AbstractPipeline<P_OUT, P_OUT, ?> op, PipelineHelper<P_OUT> helper, Spliterator<P_IN> spliterator, IntFunction<P_OUT[]> generator, long offset, long size) {
            super((PipelineHelper) helper, (Spliterator) spliterator);
            this.op = op;
            this.generator = generator;
            this.targetOffset = offset;
            this.targetSize = size;
        }

        SliceTask(SliceTask<P_IN, P_OUT> parent, Spliterator<P_IN> spliterator) {
            super((AbstractShortCircuitTask) parent, (Spliterator) spliterator);
            this.op = parent.op;
            this.generator = parent.generator;
            this.targetOffset = parent.targetOffset;
            this.targetSize = parent.targetSize;
        }

        protected SliceTask<P_IN, P_OUT> makeChild(Spliterator<P_IN> spliterator) {
            return new SliceTask(this, spliterator);
        }

        protected final Node<P_OUT> getEmptyResult() {
            return Nodes.emptyNode(this.op.getOutputShape());
        }

        protected final Node<P_OUT> doLeaf() {
            if (isRoot()) {
                long sizeIfKnown;
                if (StreamOpFlag.SIZED.isPreserved(this.op.sourceOrOpFlags)) {
                    sizeIfKnown = this.op.exactOutputSizeIfKnown(this.spliterator);
                } else {
                    sizeIfKnown = -1;
                }
                Builder<P_OUT> nb = this.op.makeNodeBuilder(sizeIfKnown, this.generator);
                this.helper.copyIntoWithCancel(this.helper.wrapSink(this.op.opWrapSink(this.helper.getStreamAndOpFlags(), nb)), this.spliterator);
                return nb.build();
            }
            Node<P_OUT> node = ((Builder) this.helper.wrapAndCopyInto(this.helper.makeNodeBuilder(-1, this.generator), this.spliterator)).build();
            this.thisNodeSize = node.count();
            this.completed = true;
            this.spliterator = null;
            return node;
        }

        public final void onCompletion(CountedCompleter<?> caller) {
            if (!isLeaf()) {
                Node<P_OUT> result;
                this.thisNodeSize = ((SliceTask) this.leftChild).thisNodeSize + ((SliceTask) this.rightChild).thisNodeSize;
                if (this.canceled) {
                    this.thisNodeSize = 0;
                    result = getEmptyResult();
                } else if (this.thisNodeSize == 0) {
                    result = getEmptyResult();
                } else if (((SliceTask) this.leftChild).thisNodeSize == 0) {
                    result = (Node) ((SliceTask) this.rightChild).getLocalResult();
                } else {
                    result = Nodes.conc(this.op.getOutputShape(), (Node) ((SliceTask) this.leftChild).getLocalResult(), (Node) ((SliceTask) this.rightChild).getLocalResult());
                }
                if (isRoot()) {
                    result = doTruncate(result);
                }
                setLocalResult(result);
                this.completed = true;
            }
            if (this.targetSize >= 0 && (isRoot() ^ 1) != 0 && isLeftCompleted(this.targetOffset + this.targetSize)) {
                cancelLaterNodes();
            }
            super.onCompletion(caller);
        }

        protected void cancel() {
            super.cancel();
            if (this.completed) {
                setLocalResult(getEmptyResult());
            }
        }

        private Node<P_OUT> doTruncate(Node<P_OUT> input) {
            return input.truncate(this.targetOffset, this.targetSize >= 0 ? Math.min(input.count(), this.targetOffset + this.targetSize) : this.thisNodeSize, this.generator);
        }

        private boolean isLeftCompleted(long target) {
            boolean z = true;
            long size = this.completed ? this.thisNodeSize : completedSize(target);
            if (size >= target) {
                return true;
            }
            AbstractTask node = this;
            for (AbstractTask parent = (SliceTask) getParent(); parent != null; SliceTask parent2 = (SliceTask) parent2.getParent()) {
                if (node == parent2.rightChild) {
                    SliceTask<P_IN, P_OUT> left = parent2.leftChild;
                    if (left != null) {
                        size += left.completedSize(target);
                        if (size >= target) {
                            return true;
                        }
                    } else {
                        continue;
                    }
                }
                node = parent2;
            }
            if (size < target) {
                z = false;
            }
            return z;
        }

        private long completedSize(long target) {
            if (this.completed) {
                return this.thisNodeSize;
            }
            SliceTask<P_IN, P_OUT> left = this.leftChild;
            SliceTask<P_IN, P_OUT> right = this.rightChild;
            if (left == null || right == null) {
                return this.thisNodeSize;
            }
            long leftSize = left.completedSize(target);
            if (leftSize < target) {
                leftSize += right.completedSize(target);
            }
            return leftSize;
        }
    }

    private static /* synthetic */ int[] -getjava-util-stream-StreamShapeSwitchesValues() {
        if (-java-util-stream-StreamShapeSwitchesValues != null) {
            return -java-util-stream-StreamShapeSwitchesValues;
        }
        int[] iArr = new int[StreamShape.values().length];
        try {
            iArr[StreamShape.DOUBLE_VALUE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[StreamShape.INT_VALUE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[StreamShape.LONG_VALUE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[StreamShape.REFERENCE.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -java-util-stream-StreamShapeSwitchesValues = iArr;
        return iArr;
    }

    private SliceOps() {
    }

    private static long calcSize(long size, long skip, long limit) {
        return size >= 0 ? Math.max(-1, Math.min(size - skip, limit)) : -1;
    }

    private static long calcSliceFence(long skip, long limit) {
        long sliceFence = limit >= 0 ? skip + limit : Long.MAX_VALUE;
        return sliceFence >= 0 ? sliceFence : Long.MAX_VALUE;
    }

    private static <P_IN> Spliterator<P_IN> sliceSpliterator(StreamShape shape, Spliterator<P_IN> s, long skip, long limit) {
        if (-assertionsDisabled || s.hasCharacteristics(16384)) {
            long sliceFence = calcSliceFence(skip, limit);
            switch (-getjava-util-stream-StreamShapeSwitchesValues()[shape.ordinal()]) {
                case 1:
                    return new OfDouble((OfDouble) s, skip, sliceFence);
                case 2:
                    return new OfInt((OfInt) s, skip, sliceFence);
                case 3:
                    return new OfLong((OfLong) s, skip, sliceFence);
                case 4:
                    return new OfRef(s, skip, sliceFence);
                default:
                    throw new IllegalStateException("Unknown shape " + shape);
            }
        }
        throw new AssertionError();
    }

    private static <T> IntFunction<T[]> castingArray() {
        return new IntFunction() {
            public final Object apply(int i) {
                return $m$0(i);
            }
        };
    }

    public static <T> Stream<T> makeRef(AbstractPipeline<?, T, ?> upstream, long skip, long limit) {
        if (skip < 0) {
            throw new IllegalArgumentException("Skip must be non-negative: " + skip);
        }
        final long j = skip;
        final long j2 = limit;
        return new StatefulOp<T, T>(upstream, StreamShape.REFERENCE, flags(limit)) {
            Spliterator<T> unorderedSkipLimitSpliterator(Spliterator<T> s, long skip, long limit, long sizeIfKnown) {
                if (skip <= sizeIfKnown) {
                    limit = limit >= 0 ? Math.min(limit, sizeIfKnown - skip) : sizeIfKnown - skip;
                    skip = 0;
                }
                return new OfRef(s, skip, limit);
            }

            public <P_IN> Spliterator<T> opEvaluateParallelLazy(PipelineHelper<T> helper, Spliterator<P_IN> spliterator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(16384)) {
                    return new OfRef(helper.wrapSpliterator(spliterator), j, SliceOps.calcSliceFence(j, j2));
                }
                if (StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    return ((Node) new SliceTask(this, helper, spliterator, SliceOps.castingArray(), j, j2).invoke()).spliterator();
                }
                return unorderedSkipLimitSpliterator(helper.wrapSpliterator(spliterator), j, j2, size);
            }

            public <P_IN> Node<T> opEvaluateParallel(PipelineHelper<T> helper, Spliterator<P_IN> spliterator, IntFunction<T[]> generator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(16384)) {
                    return Nodes.collect(helper, SliceOps.sliceSpliterator(helper.getSourceShape(), spliterator, j, j2), true, generator);
                } else if (StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    return (Node) new SliceTask(this, helper, spliterator, generator, j, j2).invoke();
                } else {
                    return Nodes.collect(this, unorderedSkipLimitSpliterator(helper.wrapSpliterator(spliterator), j, j2, size), true, generator);
                }
            }

            public Sink<T> opWrapSink(int flags, Sink<T> sink) {
                final long j = j;
                final long j2 = j2;
                return new ChainedReference<T, T>(sink) {
                    long m;
                    long n = j;

                    public void begin(long size) {
                        this.downstream.begin(SliceOps.calcSize(size, j, this.m));
                    }

                    public void accept(T t) {
                        if (this.n != 0) {
                            this.n--;
                        } else if (this.m > 0) {
                            this.m--;
                            this.downstream.-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-1(t);
                        }
                    }

                    public boolean cancellationRequested() {
                        return this.m != 0 ? this.downstream.cancellationRequested() : true;
                    }
                };
            }
        };
    }

    public static IntStream makeInt(AbstractPipeline<?, Integer, ?> upstream, long skip, long limit) {
        if (skip < 0) {
            throw new IllegalArgumentException("Skip must be non-negative: " + skip);
        }
        final long j = skip;
        final long j2 = limit;
        return new IntPipeline.StatefulOp<Integer>(upstream, StreamShape.INT_VALUE, flags(limit)) {
            OfInt unorderedSkipLimitSpliterator(OfInt s, long skip, long limit, long sizeIfKnown) {
                if (skip <= sizeIfKnown) {
                    limit = limit >= 0 ? Math.min(limit, sizeIfKnown - skip) : sizeIfKnown - skip;
                    skip = 0;
                }
                return new OfInt(s, skip, limit);
            }

            public <P_IN> Spliterator<Integer> opEvaluateParallelLazy(PipelineHelper<Integer> helper, Spliterator<P_IN> spliterator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(16384)) {
                    return new OfInt((OfInt) helper.wrapSpliterator(spliterator), j, SliceOps.calcSliceFence(j, j2));
                }
                if (StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    return ((Node) new SliceTask(this, helper, spliterator, new -$Lambda$PuGmanPUdjtCbC0vii4r0KGZNys(), j, j2).invoke()).spliterator();
                }
                return unorderedSkipLimitSpliterator((OfInt) helper.wrapSpliterator(spliterator), j, j2, size);
            }

            public <P_IN> Node<Integer> opEvaluateParallel(PipelineHelper<Integer> helper, Spliterator<P_IN> spliterator, IntFunction<Integer[]> generator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(16384)) {
                    return Nodes.collectInt(helper, SliceOps.sliceSpliterator(helper.getSourceShape(), spliterator, j, j2), true);
                } else if (StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    return (Node) new SliceTask(this, helper, spliterator, generator, j, j2).invoke();
                } else {
                    return Nodes.collectInt(this, unorderedSkipLimitSpliterator((OfInt) helper.wrapSpliterator(spliterator), j, j2, size), true);
                }
            }

            public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                final long j = j;
                final long j2 = j2;
                return new ChainedInt<Integer>(sink) {
                    long m;
                    long n = j;

                    public void begin(long size) {
                        this.downstream.begin(SliceOps.calcSize(size, j, this.m));
                    }

                    public void accept(int t) {
                        if (this.n != 0) {
                            this.n--;
                        } else if (this.m > 0) {
                            this.m--;
                            this.downstream.-java_util_stream_IntPipeline-mthref-0(t);
                        }
                    }

                    public boolean cancellationRequested() {
                        return this.m != 0 ? this.downstream.cancellationRequested() : true;
                    }
                };
            }
        };
    }

    public static LongStream makeLong(AbstractPipeline<?, Long, ?> upstream, long skip, long limit) {
        if (skip < 0) {
            throw new IllegalArgumentException("Skip must be non-negative: " + skip);
        }
        final long j = skip;
        final long j2 = limit;
        return new LongPipeline.StatefulOp<Long>(upstream, StreamShape.LONG_VALUE, flags(limit)) {
            OfLong unorderedSkipLimitSpliterator(OfLong s, long skip, long limit, long sizeIfKnown) {
                if (skip <= sizeIfKnown) {
                    limit = limit >= 0 ? Math.min(limit, sizeIfKnown - skip) : sizeIfKnown - skip;
                    skip = 0;
                }
                return new OfLong(s, skip, limit);
            }

            public <P_IN> Spliterator<Long> opEvaluateParallelLazy(PipelineHelper<Long> helper, Spliterator<P_IN> spliterator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(16384)) {
                    return new OfLong((OfLong) helper.wrapSpliterator(spliterator), j, SliceOps.calcSliceFence(j, j2));
                }
                if (StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    return ((Node) new SliceTask(this, helper, spliterator, new IntFunction() {
                        public final Object apply(int i) {
                            return $m$0(i);
                        }
                    }, j, j2).invoke()).spliterator();
                }
                return unorderedSkipLimitSpliterator((OfLong) helper.wrapSpliterator(spliterator), j, j2, size);
            }

            public <P_IN> Node<Long> opEvaluateParallel(PipelineHelper<Long> helper, Spliterator<P_IN> spliterator, IntFunction<Long[]> generator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(16384)) {
                    return Nodes.collectLong(helper, SliceOps.sliceSpliterator(helper.getSourceShape(), spliterator, j, j2), true);
                } else if (StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    return (Node) new SliceTask(this, helper, spliterator, generator, j, j2).invoke();
                } else {
                    return Nodes.collectLong(this, unorderedSkipLimitSpliterator((OfLong) helper.wrapSpliterator(spliterator), j, j2, size), true);
                }
            }

            public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                final long j = j;
                final long j2 = j2;
                return new ChainedLong<Long>(sink) {
                    long m;
                    long n = j;

                    public void begin(long size) {
                        this.downstream.begin(SliceOps.calcSize(size, j, this.m));
                    }

                    public void accept(long t) {
                        if (this.n != 0) {
                            this.n--;
                        } else if (this.m > 0) {
                            this.m--;
                            this.downstream.-java_util_stream_LongPipeline-mthref-0(t);
                        }
                    }

                    public boolean cancellationRequested() {
                        return this.m != 0 ? this.downstream.cancellationRequested() : true;
                    }
                };
            }
        };
    }

    public static DoubleStream makeDouble(AbstractPipeline<?, Double, ?> upstream, long skip, long limit) {
        if (skip < 0) {
            throw new IllegalArgumentException("Skip must be non-negative: " + skip);
        }
        final long j = skip;
        final long j2 = limit;
        return new DoublePipeline.StatefulOp<Double>(upstream, StreamShape.DOUBLE_VALUE, flags(limit)) {
            OfDouble unorderedSkipLimitSpliterator(OfDouble s, long skip, long limit, long sizeIfKnown) {
                if (skip <= sizeIfKnown) {
                    limit = limit >= 0 ? Math.min(limit, sizeIfKnown - skip) : sizeIfKnown - skip;
                    skip = 0;
                }
                return new OfDouble(s, skip, limit);
            }

            public <P_IN> Spliterator<Double> opEvaluateParallelLazy(PipelineHelper<Double> helper, Spliterator<P_IN> spliterator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(16384)) {
                    return new OfDouble((OfDouble) helper.wrapSpliterator(spliterator), j, SliceOps.calcSliceFence(j, j2));
                }
                if (StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    return ((Node) new SliceTask(this, helper, spliterator, new IntFunction() {
                        public final Object apply(int i) {
                            return $m$0(i);
                        }
                    }, j, j2).invoke()).spliterator();
                }
                return unorderedSkipLimitSpliterator((OfDouble) helper.wrapSpliterator(spliterator), j, j2, size);
            }

            public <P_IN> Node<Double> opEvaluateParallel(PipelineHelper<Double> helper, Spliterator<P_IN> spliterator, IntFunction<Double[]> generator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(16384)) {
                    return Nodes.collectDouble(helper, SliceOps.sliceSpliterator(helper.getSourceShape(), spliterator, j, j2), true);
                } else if (StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    return (Node) new SliceTask(this, helper, spliterator, generator, j, j2).invoke();
                } else {
                    return Nodes.collectDouble(this, unorderedSkipLimitSpliterator((OfDouble) helper.wrapSpliterator(spliterator), j, j2, size), true);
                }
            }

            public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
                final long j = j;
                final long j2 = j2;
                return new ChainedDouble<Double>(sink) {
                    long m;
                    long n = j;

                    public void begin(long size) {
                        this.downstream.begin(SliceOps.calcSize(size, j, this.m));
                    }

                    public void accept(double t) {
                        if (this.n != 0) {
                            this.n--;
                        } else if (this.m > 0) {
                            this.m--;
                            this.downstream.-java_util_stream_ReferencePipeline$9$1-mthref-0(t);
                        }
                    }

                    public boolean cancellationRequested() {
                        return this.m != 0 ? this.downstream.cancellationRequested() : true;
                    }
                };
            }
        };
    }

    private static int flags(long limit) {
        return (limit != -1 ? StreamOpFlag.IS_SHORT_CIRCUIT : 0) | StreamOpFlag.NOT_SIZED;
    }
}
