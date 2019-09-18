package java.util.stream;

import java.util.Spliterator;
import java.util.concurrent.CountedCompleter;
import java.util.function.IntFunction;
import java.util.stream.DoublePipeline;
import java.util.stream.IntPipeline;
import java.util.stream.LongPipeline;
import java.util.stream.Node;
import java.util.stream.ReferencePipeline;
import java.util.stream.Sink;
import java.util.stream.StreamSpliterators;

final class SliceOps {
    static final /* synthetic */ boolean $assertionsDisabled = false;

    private static final class SliceTask<P_IN, P_OUT> extends AbstractShortCircuitTask<P_IN, P_OUT, Node<P_OUT>, SliceTask<P_IN, P_OUT>> {
        private volatile boolean completed;
        private final IntFunction<P_OUT[]> generator;
        private final AbstractPipeline<P_OUT, P_OUT, ?> op;
        private final long targetOffset;
        private final long targetSize;
        private long thisNodeSize;

        SliceTask(AbstractPipeline<P_OUT, P_OUT, ?> op2, PipelineHelper<P_OUT> helper, Spliterator<P_IN> spliterator, IntFunction<P_OUT[]> generator2, long offset, long size) {
            super(helper, spliterator);
            this.op = op2;
            this.generator = generator2;
            this.targetOffset = offset;
            this.targetSize = size;
        }

        SliceTask(SliceTask<P_IN, P_OUT> parent, Spliterator<P_IN> spliterator) {
            super(parent, spliterator);
            this.op = parent.op;
            this.generator = parent.generator;
            this.targetOffset = parent.targetOffset;
            this.targetSize = parent.targetSize;
        }

        /* access modifiers changed from: protected */
        public SliceTask<P_IN, P_OUT> makeChild(Spliterator<P_IN> spliterator) {
            return new SliceTask<>(this, spliterator);
        }

        /* access modifiers changed from: protected */
        public final Node<P_OUT> getEmptyResult() {
            return Nodes.emptyNode(this.op.getOutputShape());
        }

        /* access modifiers changed from: protected */
        public final Node<P_OUT> doLeaf() {
            long sizeIfKnown = -1;
            if (isRoot()) {
                if (StreamOpFlag.SIZED.isPreserved(this.op.sourceOrOpFlags)) {
                    sizeIfKnown = this.op.exactOutputSizeIfKnown(this.spliterator);
                }
                Node.Builder<P_OUT> nb = this.op.makeNodeBuilder(sizeIfKnown, this.generator);
                this.helper.copyIntoWithCancel(this.helper.wrapSink(this.op.opWrapSink(this.helper.getStreamAndOpFlags(), nb)), this.spliterator);
                return nb.build();
            }
            Node<P_OUT> node = ((Node.Builder) this.helper.wrapAndCopyInto(this.helper.makeNodeBuilder(-1, this.generator), this.spliterator)).build();
            this.thisNodeSize = node.count();
            this.completed = true;
            this.spliterator = null;
            return node;
        }

        public final void onCompletion(CountedCompleter<?> caller) {
            Node<P_OUT> result;
            if (!isLeaf()) {
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
                setLocalResult(isRoot() ? doTruncate(result) : result);
                this.completed = true;
            }
            if (this.targetSize >= 0 && !isRoot() && isLeftCompleted(this.targetOffset + this.targetSize)) {
                cancelLaterNodes();
            }
            super.onCompletion(caller);
        }

        /* access modifiers changed from: protected */
        public void cancel() {
            super.cancel();
            if (this.completed) {
                setLocalResult(getEmptyResult());
            }
        }

        private Node<P_OUT> doTruncate(Node<P_OUT> input) {
            return input.truncate(this.targetOffset, this.targetSize >= 0 ? Math.min(input.count(), this.targetOffset + this.targetSize) : this.thisNodeSize, this.generator);
        }

        /* JADX WARNING: type inference failed for: r1v1, types: [java.util.stream.AbstractTask] */
        /* JADX WARNING: Multi-variable type inference failed */
        private boolean isLeftCompleted(long target) {
            long size = this.completed ? this.thisNodeSize : completedSize(target);
            boolean z = true;
            if (size >= target) {
                return true;
            }
            long size2 = size;
            SliceTask<P_IN, P_OUT> node = this;
            for (SliceTask<P_IN, P_OUT> parent = (SliceTask) getParent(); parent != null; parent = parent.getParent()) {
                if (node == parent.rightChild) {
                    SliceTask<P_IN, P_OUT> left = (SliceTask) parent.leftChild;
                    if (left != null) {
                        size2 += left.completedSize(target);
                        if (size2 >= target) {
                            return true;
                        }
                    } else {
                        continue;
                    }
                }
                node = parent;
            }
            if (size2 < target) {
                z = false;
            }
            return z;
        }

        private long completedSize(long target) {
            if (this.completed) {
                return this.thisNodeSize;
            }
            SliceTask<P_IN, P_OUT> left = (SliceTask) this.leftChild;
            SliceTask<P_IN, P_OUT> right = (SliceTask) this.rightChild;
            if (left == null || right == null) {
                return this.thisNodeSize;
            }
            long leftSize = left.completedSize(target);
            return leftSize >= target ? leftSize : right.completedSize(target) + leftSize;
        }
    }

    private SliceOps() {
    }

    /* access modifiers changed from: private */
    public static long calcSize(long size, long skip, long limit) {
        if (size >= 0) {
            return Math.max(-1, Math.min(size - skip, limit));
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public static long calcSliceFence(long skip, long limit) {
        long sliceFence = limit >= 0 ? skip + limit : Long.MAX_VALUE;
        if (sliceFence >= 0) {
            return sliceFence;
        }
        return Long.MAX_VALUE;
    }

    /* access modifiers changed from: private */
    public static <P_IN> Spliterator<P_IN> sliceSpliterator(StreamShape shape, Spliterator<P_IN> s, long skip, long limit) {
        long sliceFence = calcSliceFence(skip, limit);
        switch (shape) {
            case REFERENCE:
                StreamSpliterators.SliceSpliterator.OfRef ofRef = new StreamSpliterators.SliceSpliterator.OfRef(s, skip, sliceFence);
                return ofRef;
            case INT_VALUE:
                StreamSpliterators.SliceSpliterator.OfInt ofInt = new StreamSpliterators.SliceSpliterator.OfInt((Spliterator.OfInt) s, skip, sliceFence);
                return ofInt;
            case LONG_VALUE:
                StreamSpliterators.SliceSpliterator.OfLong ofLong = new StreamSpliterators.SliceSpliterator.OfLong((Spliterator.OfLong) s, skip, sliceFence);
                return ofLong;
            case DOUBLE_VALUE:
                StreamSpliterators.SliceSpliterator.OfDouble ofDouble = new StreamSpliterators.SliceSpliterator.OfDouble((Spliterator.OfDouble) s, skip, sliceFence);
                return ofDouble;
            default:
                throw new IllegalStateException("Unknown shape " + shape);
        }
    }

    /* access modifiers changed from: private */
    public static <T> IntFunction<T[]> castingArray() {
        return $$Lambda$SliceOps$T0eS2B9nWeCpmA7G2QlMnW3G2UA.INSTANCE;
    }

    static /* synthetic */ Object[] lambda$castingArray$0(int size) {
        return new Object[size];
    }

    public static <T> Stream<T> makeRef(AbstractPipeline<?, T, ?> upstream, long skip, long limit) {
        if (skip >= 0) {
            final long j = skip;
            final long j2 = limit;
            AnonymousClass1 r1 = new ReferencePipeline.StatefulOp<T, T>(upstream, StreamShape.REFERENCE, flags(limit)) {
                /* access modifiers changed from: package-private */
                public Spliterator<T> unorderedSkipLimitSpliterator(Spliterator<T> s, long skip, long limit, long sizeIfKnown) {
                    if (skip <= sizeIfKnown) {
                        limit = limit >= 0 ? Math.min(limit, sizeIfKnown - skip) : sizeIfKnown - skip;
                        skip = 0;
                    }
                    StreamSpliterators.UnorderedSliceSpliterator.OfRef ofRef = new StreamSpliterators.UnorderedSliceSpliterator.OfRef(s, skip, limit);
                    return ofRef;
                }

                /* JADX WARNING: type inference failed for: r16v0, types: [java.util.Spliterator, java.util.Spliterator<P_IN>] */
                /* JADX WARNING: Unknown variable types count: 1 */
                public <P_IN> Spliterator<T> opEvaluateParallelLazy(PipelineHelper<T> helper, Spliterator<P_IN> r16) {
                    Spliterator spliterator;
                    long size = helper.exactOutputSizeIfKnown(r16);
                    if (size > 0) {
                        spliterator = r16;
                        if (spliterator.hasCharacteristics(16384)) {
                            StreamSpliterators.SliceSpliterator.OfRef ofRef = new StreamSpliterators.SliceSpliterator.OfRef(helper.wrapSpliterator(r16), j, SliceOps.calcSliceFence(j, j2));
                            return ofRef;
                        }
                    } else {
                        spliterator = r16;
                    }
                    if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                        return unorderedSkipLimitSpliterator(helper.wrapSpliterator(r16), j, j2, size);
                    }
                    SliceTask sliceTask = new SliceTask(this, helper, spliterator, SliceOps.castingArray(), j, j2);
                    return ((Node) sliceTask.invoke()).spliterator();
                }

                /* JADX WARNING: type inference failed for: r18v0, types: [java.util.Spliterator, java.util.Spliterator<P_IN>] */
                /* JADX WARNING: Unknown variable types count: 1 */
                public <P_IN> Node<T> opEvaluateParallel(PipelineHelper<T> helper, Spliterator<P_IN> r18, IntFunction<T[]> generator) {
                    PipelineHelper<T> pipelineHelper;
                    Spliterator spliterator;
                    IntFunction<T[]> intFunction = generator;
                    long size = helper.exactOutputSizeIfKnown(r18);
                    if (size > 0) {
                        spliterator = r18;
                        if (spliterator.hasCharacteristics(16384)) {
                            return Nodes.collect(helper, SliceOps.sliceSpliterator(helper.getSourceShape(), spliterator, j, j2), true, intFunction);
                        }
                        pipelineHelper = helper;
                    } else {
                        pipelineHelper = helper;
                        spliterator = r18;
                    }
                    if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                        return Nodes.collect(this, unorderedSkipLimitSpliterator(helper.wrapSpliterator(r18), j, j2, size), true, intFunction);
                    }
                    SliceTask sliceTask = new SliceTask(this, pipelineHelper, spliterator, intFunction, j, j2);
                    return (Node) sliceTask.invoke();
                }

                public Sink<T> opWrapSink(int flags, Sink<T> sink) {
                    return new Sink.ChainedReference<T, T>(sink) {
                        long m;
                        long n = j;

                        {
                            this.m = j2 >= 0 ? j2 : Long.MAX_VALUE;
                        }

                        public void begin(long size) {
                            this.downstream.begin(SliceOps.calcSize(size, j, this.m));
                        }

                        public void accept(T t) {
                            if (this.n != 0) {
                                this.n--;
                            } else if (this.m > 0) {
                                this.m--;
                                this.downstream.accept(t);
                            }
                        }

                        public boolean cancellationRequested() {
                            return this.m == 0 || this.downstream.cancellationRequested();
                        }
                    };
                }
            };
            return r1;
        }
        throw new IllegalArgumentException("Skip must be non-negative: " + skip);
    }

    public static IntStream makeInt(AbstractPipeline<?, Integer, ?> upstream, long skip, long limit) {
        if (skip >= 0) {
            final long j = skip;
            final long j2 = limit;
            AnonymousClass2 r1 = new IntPipeline.StatefulOp<Integer>(upstream, StreamShape.INT_VALUE, flags(limit)) {
                /* access modifiers changed from: package-private */
                public Spliterator.OfInt unorderedSkipLimitSpliterator(Spliterator.OfInt s, long skip, long limit, long sizeIfKnown) {
                    if (skip <= sizeIfKnown) {
                        limit = limit >= 0 ? Math.min(limit, sizeIfKnown - skip) : sizeIfKnown - skip;
                        skip = 0;
                    }
                    StreamSpliterators.UnorderedSliceSpliterator.OfInt ofInt = new StreamSpliterators.UnorderedSliceSpliterator.OfInt(s, skip, limit);
                    return ofInt;
                }

                /* JADX WARNING: type inference failed for: r16v0, types: [java.util.Spliterator, java.util.Spliterator<P_IN>] */
                /* JADX WARNING: Unknown variable types count: 1 */
                public <P_IN> Spliterator<Integer> opEvaluateParallelLazy(PipelineHelper<Integer> helper, Spliterator<P_IN> r16) {
                    Spliterator spliterator;
                    long size = helper.exactOutputSizeIfKnown(r16);
                    if (size > 0) {
                        spliterator = r16;
                        if (spliterator.hasCharacteristics(16384)) {
                            StreamSpliterators.SliceSpliterator.OfInt ofInt = new StreamSpliterators.SliceSpliterator.OfInt((Spliterator.OfInt) helper.wrapSpliterator(r16), j, SliceOps.calcSliceFence(j, j2));
                            return ofInt;
                        }
                    } else {
                        spliterator = r16;
                    }
                    if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                        return unorderedSkipLimitSpliterator((Spliterator.OfInt) helper.wrapSpliterator(r16), j, j2, size);
                    }
                    SliceTask sliceTask = new SliceTask(this, helper, spliterator, $$Lambda$SliceOps$2$pJKvYyBs7HGPiOPTm_fxpciSsG8.INSTANCE, j, j2);
                    return ((Node) sliceTask.invoke()).spliterator();
                }

                static /* synthetic */ Integer[] lambda$opEvaluateParallelLazy$0(int x$0) {
                    return new Integer[x$0];
                }

                /* JADX WARNING: type inference failed for: r17v0, types: [java.util.Spliterator, java.util.Spliterator<P_IN>] */
                /* JADX WARNING: Unknown variable types count: 1 */
                public <P_IN> Node<Integer> opEvaluateParallel(PipelineHelper<Integer> helper, Spliterator<P_IN> r17, IntFunction<Integer[]> generator) {
                    PipelineHelper<Integer> pipelineHelper;
                    Spliterator spliterator;
                    long size = helper.exactOutputSizeIfKnown(r17);
                    if (size > 0) {
                        spliterator = r17;
                        if (spliterator.hasCharacteristics(16384)) {
                            return Nodes.collectInt(helper, SliceOps.sliceSpliterator(helper.getSourceShape(), spliterator, j, j2), true);
                        }
                        pipelineHelper = helper;
                    } else {
                        pipelineHelper = helper;
                        spliterator = r17;
                    }
                    if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                        return Nodes.collectInt(this, unorderedSkipLimitSpliterator((Spliterator.OfInt) helper.wrapSpliterator(r17), j, j2, size), true);
                    }
                    SliceTask sliceTask = new SliceTask(this, pipelineHelper, spliterator, generator, j, j2);
                    return (Node) sliceTask.invoke();
                }

                public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                    return new Sink.ChainedInt<Integer>(sink) {
                        long m;
                        long n = j;

                        {
                            this.m = j2 >= 0 ? j2 : Long.MAX_VALUE;
                        }

                        public void begin(long size) {
                            this.downstream.begin(SliceOps.calcSize(size, j, this.m));
                        }

                        public void accept(int t) {
                            if (this.n != 0) {
                                this.n--;
                            } else if (this.m > 0) {
                                this.m--;
                                this.downstream.accept(t);
                            }
                        }

                        public boolean cancellationRequested() {
                            return this.m == 0 || this.downstream.cancellationRequested();
                        }
                    };
                }
            };
            return r1;
        }
        throw new IllegalArgumentException("Skip must be non-negative: " + skip);
    }

    public static LongStream makeLong(AbstractPipeline<?, Long, ?> upstream, long skip, long limit) {
        if (skip >= 0) {
            final long j = skip;
            final long j2 = limit;
            AnonymousClass3 r1 = new LongPipeline.StatefulOp<Long>(upstream, StreamShape.LONG_VALUE, flags(limit)) {
                /* access modifiers changed from: package-private */
                public Spliterator.OfLong unorderedSkipLimitSpliterator(Spliterator.OfLong s, long skip, long limit, long sizeIfKnown) {
                    if (skip <= sizeIfKnown) {
                        limit = limit >= 0 ? Math.min(limit, sizeIfKnown - skip) : sizeIfKnown - skip;
                        skip = 0;
                    }
                    StreamSpliterators.UnorderedSliceSpliterator.OfLong ofLong = new StreamSpliterators.UnorderedSliceSpliterator.OfLong(s, skip, limit);
                    return ofLong;
                }

                /* JADX WARNING: type inference failed for: r16v0, types: [java.util.Spliterator, java.util.Spliterator<P_IN>] */
                /* JADX WARNING: Unknown variable types count: 1 */
                public <P_IN> Spliterator<Long> opEvaluateParallelLazy(PipelineHelper<Long> helper, Spliterator<P_IN> r16) {
                    Spliterator spliterator;
                    long size = helper.exactOutputSizeIfKnown(r16);
                    if (size > 0) {
                        spliterator = r16;
                        if (spliterator.hasCharacteristics(16384)) {
                            StreamSpliterators.SliceSpliterator.OfLong ofLong = new StreamSpliterators.SliceSpliterator.OfLong((Spliterator.OfLong) helper.wrapSpliterator(r16), j, SliceOps.calcSliceFence(j, j2));
                            return ofLong;
                        }
                    } else {
                        spliterator = r16;
                    }
                    if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                        return unorderedSkipLimitSpliterator((Spliterator.OfLong) helper.wrapSpliterator(r16), j, j2, size);
                    }
                    SliceTask sliceTask = new SliceTask(this, helper, spliterator, $$Lambda$SliceOps$3$iKJ8R9VMhJpW3rzcr1q11o2TH4.INSTANCE, j, j2);
                    return ((Node) sliceTask.invoke()).spliterator();
                }

                static /* synthetic */ Long[] lambda$opEvaluateParallelLazy$0(int x$0) {
                    return new Long[x$0];
                }

                /* JADX WARNING: type inference failed for: r17v0, types: [java.util.Spliterator, java.util.Spliterator<P_IN>] */
                /* JADX WARNING: Unknown variable types count: 1 */
                public <P_IN> Node<Long> opEvaluateParallel(PipelineHelper<Long> helper, Spliterator<P_IN> r17, IntFunction<Long[]> generator) {
                    PipelineHelper<Long> pipelineHelper;
                    Spliterator spliterator;
                    long size = helper.exactOutputSizeIfKnown(r17);
                    if (size > 0) {
                        spliterator = r17;
                        if (spliterator.hasCharacteristics(16384)) {
                            return Nodes.collectLong(helper, SliceOps.sliceSpliterator(helper.getSourceShape(), spliterator, j, j2), true);
                        }
                        pipelineHelper = helper;
                    } else {
                        pipelineHelper = helper;
                        spliterator = r17;
                    }
                    if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                        return Nodes.collectLong(this, unorderedSkipLimitSpliterator((Spliterator.OfLong) helper.wrapSpliterator(r17), j, j2, size), true);
                    }
                    SliceTask sliceTask = new SliceTask(this, pipelineHelper, spliterator, generator, j, j2);
                    return (Node) sliceTask.invoke();
                }

                public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                    return new Sink.ChainedLong<Long>(sink) {
                        long m;
                        long n = j;

                        {
                            this.m = j2 >= 0 ? j2 : Long.MAX_VALUE;
                        }

                        public void begin(long size) {
                            this.downstream.begin(SliceOps.calcSize(size, j, this.m));
                        }

                        public void accept(long t) {
                            if (this.n != 0) {
                                this.n--;
                            } else if (this.m > 0) {
                                this.m--;
                                this.downstream.accept(t);
                            }
                        }

                        public boolean cancellationRequested() {
                            return this.m == 0 || this.downstream.cancellationRequested();
                        }
                    };
                }
            };
            return r1;
        }
        throw new IllegalArgumentException("Skip must be non-negative: " + skip);
    }

    public static DoubleStream makeDouble(AbstractPipeline<?, Double, ?> upstream, long skip, long limit) {
        if (skip >= 0) {
            final long j = skip;
            final long j2 = limit;
            AnonymousClass4 r1 = new DoublePipeline.StatefulOp<Double>(upstream, StreamShape.DOUBLE_VALUE, flags(limit)) {
                /* access modifiers changed from: package-private */
                public Spliterator.OfDouble unorderedSkipLimitSpliterator(Spliterator.OfDouble s, long skip, long limit, long sizeIfKnown) {
                    if (skip <= sizeIfKnown) {
                        limit = limit >= 0 ? Math.min(limit, sizeIfKnown - skip) : sizeIfKnown - skip;
                        skip = 0;
                    }
                    StreamSpliterators.UnorderedSliceSpliterator.OfDouble ofDouble = new StreamSpliterators.UnorderedSliceSpliterator.OfDouble(s, skip, limit);
                    return ofDouble;
                }

                /* JADX WARNING: type inference failed for: r16v0, types: [java.util.Spliterator, java.util.Spliterator<P_IN>] */
                /* JADX WARNING: Unknown variable types count: 1 */
                public <P_IN> Spliterator<Double> opEvaluateParallelLazy(PipelineHelper<Double> helper, Spliterator<P_IN> r16) {
                    Spliterator spliterator;
                    long size = helper.exactOutputSizeIfKnown(r16);
                    if (size > 0) {
                        spliterator = r16;
                        if (spliterator.hasCharacteristics(16384)) {
                            StreamSpliterators.SliceSpliterator.OfDouble ofDouble = new StreamSpliterators.SliceSpliterator.OfDouble((Spliterator.OfDouble) helper.wrapSpliterator(r16), j, SliceOps.calcSliceFence(j, j2));
                            return ofDouble;
                        }
                    } else {
                        spliterator = r16;
                    }
                    if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                        return unorderedSkipLimitSpliterator((Spliterator.OfDouble) helper.wrapSpliterator(r16), j, j2, size);
                    }
                    SliceTask sliceTask = new SliceTask(this, helper, spliterator, $$Lambda$SliceOps$4$JdMLhF4N5dBS3gGxMct4lK2SQ04.INSTANCE, j, j2);
                    return ((Node) sliceTask.invoke()).spliterator();
                }

                static /* synthetic */ Double[] lambda$opEvaluateParallelLazy$0(int x$0) {
                    return new Double[x$0];
                }

                /* JADX WARNING: type inference failed for: r17v0, types: [java.util.Spliterator, java.util.Spliterator<P_IN>] */
                /* JADX WARNING: Unknown variable types count: 1 */
                public <P_IN> Node<Double> opEvaluateParallel(PipelineHelper<Double> helper, Spliterator<P_IN> r17, IntFunction<Double[]> generator) {
                    PipelineHelper<Double> pipelineHelper;
                    Spliterator spliterator;
                    long size = helper.exactOutputSizeIfKnown(r17);
                    if (size > 0) {
                        spliterator = r17;
                        if (spliterator.hasCharacteristics(16384)) {
                            return Nodes.collectDouble(helper, SliceOps.sliceSpliterator(helper.getSourceShape(), spliterator, j, j2), true);
                        }
                        pipelineHelper = helper;
                    } else {
                        pipelineHelper = helper;
                        spliterator = r17;
                    }
                    if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                        return Nodes.collectDouble(this, unorderedSkipLimitSpliterator((Spliterator.OfDouble) helper.wrapSpliterator(r17), j, j2, size), true);
                    }
                    SliceTask sliceTask = new SliceTask(this, pipelineHelper, spliterator, generator, j, j2);
                    return (Node) sliceTask.invoke();
                }

                public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
                    return new Sink.ChainedDouble<Double>(sink) {
                        long m;
                        long n = j;

                        {
                            this.m = j2 >= 0 ? j2 : Long.MAX_VALUE;
                        }

                        public void begin(long size) {
                            this.downstream.begin(SliceOps.calcSize(size, j, this.m));
                        }

                        public void accept(double t) {
                            if (this.n != 0) {
                                this.n--;
                            } else if (this.m > 0) {
                                this.m--;
                                this.downstream.accept(t);
                            }
                        }

                        public boolean cancellationRequested() {
                            return this.m == 0 || this.downstream.cancellationRequested();
                        }
                    };
                }
            };
            return r1;
        }
        throw new IllegalArgumentException("Skip must be non-negative: " + skip);
    }

    private static int flags(long limit) {
        return StreamOpFlag.NOT_SIZED | (limit != -1 ? StreamOpFlag.IS_SHORT_CIRCUIT : 0);
    }
}
