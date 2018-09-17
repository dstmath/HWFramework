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
import sun.util.calendar.BaseCalendar;

final class SliceOps {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final /* synthetic */ int[] -java-util-stream-StreamShapeSwitchesValues = null;

    final /* synthetic */ class -java_util_function_IntFunction_castingArray__LambdaImpl0 implements IntFunction {
        public Object apply(int arg0) {
            return new Object[arg0];
        }
    }

    /* renamed from: java.util.stream.SliceOps.1 */
    static class AnonymousClass1 extends StatefulOp<T, T> {
        final /* synthetic */ long val$limit;
        final /* synthetic */ long val$skip;

        /* renamed from: java.util.stream.SliceOps.1.1 */
        class AnonymousClass1 extends ChainedReference<T, T> {
            long m;
            long n;
            final /* synthetic */ long val$limit;
            final /* synthetic */ long val$skip;

            AnonymousClass1(Sink $anonymous0, long val$skip, long val$limit) {
                this.val$skip = val$skip;
                this.val$limit = val$limit;
                super($anonymous0);
                this.n = this.val$skip;
                this.m = this.val$limit >= 0 ? this.val$limit : Long.MAX_VALUE;
            }

            public void begin(long size) {
                this.downstream.begin(SliceOps.calcSize(size, this.val$skip, this.m));
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
                return this.m != 0 ? this.downstream.cancellationRequested() : true;
            }
        }

        AnonymousClass1(AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, long val$skip, long val$limit) {
            this.val$skip = val$skip;
            this.val$limit = val$limit;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        Spliterator<T> unorderedSkipLimitSpliterator(Spliterator<T> s, long skip, long limit, long sizeIfKnown) {
            if (skip <= sizeIfKnown) {
                limit = limit >= 0 ? Math.min(limit, sizeIfKnown - skip) : sizeIfKnown - skip;
                skip = 0;
            }
            return new OfRef(s, skip, limit);
        }

        public <P_IN> Spliterator<T> opEvaluateParallelLazy(PipelineHelper<T> helper, Spliterator<P_IN> spliterator) {
            long size = helper.exactOutputSizeIfKnown(spliterator);
            if (size > 0 && spliterator.hasCharacteristics(Record.maxDataSize)) {
                return new OfRef(helper.wrapSpliterator(spliterator), this.val$skip, SliceOps.calcSliceFence(this.val$skip, this.val$limit));
            }
            if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                return unorderedSkipLimitSpliterator(helper.wrapSpliterator(spliterator), this.val$skip, this.val$limit, size);
            }
            return ((Node) new SliceTask(this, helper, spliterator, SliceOps.castingArray(), this.val$skip, this.val$limit).invoke()).spliterator();
        }

        public <P_IN> Node<T> opEvaluateParallel(PipelineHelper<T> helper, Spliterator<P_IN> spliterator, IntFunction<T[]> generator) {
            long size = helper.exactOutputSizeIfKnown(spliterator);
            if (size > 0 && spliterator.hasCharacteristics(Record.maxDataSize)) {
                return Nodes.collect(helper, SliceOps.sliceSpliterator(helper.getSourceShape(), spliterator, this.val$skip, this.val$limit), true, generator);
            } else if (StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                return (Node) new SliceTask(this, helper, spliterator, generator, this.val$skip, this.val$limit).invoke();
            } else {
                return Nodes.collect(this, unorderedSkipLimitSpliterator(helper.wrapSpliterator(spliterator), this.val$skip, this.val$limit, size), true, generator);
            }
        }

        public Sink<T> opWrapSink(int flags, Sink<T> sink) {
            return new AnonymousClass1(sink, this.val$skip, this.val$limit);
        }
    }

    /* renamed from: java.util.stream.SliceOps.2 */
    static class AnonymousClass2 extends IntPipeline.StatefulOp<Integer> {
        final /* synthetic */ long val$limit;
        final /* synthetic */ long val$skip;

        final /* synthetic */ class -java_util_Spliterator_opEvaluateParallelLazy_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl0 implements IntFunction {
            public Object apply(int arg0) {
                return new Integer[arg0];
            }
        }

        /* renamed from: java.util.stream.SliceOps.2.1 */
        class AnonymousClass1 extends ChainedInt<Integer> {
            long m;
            long n;
            final /* synthetic */ long val$limit;
            final /* synthetic */ long val$skip;

            AnonymousClass1(Sink $anonymous0, long val$skip, long val$limit) {
                this.val$skip = val$skip;
                this.val$limit = val$limit;
                super($anonymous0);
                this.n = this.val$skip;
                this.m = this.val$limit >= 0 ? this.val$limit : Long.MAX_VALUE;
            }

            public void begin(long size) {
                this.downstream.begin(SliceOps.calcSize(size, this.val$skip, this.m));
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
                return this.m != 0 ? this.downstream.cancellationRequested() : true;
            }
        }

        AnonymousClass2(AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, long val$skip, long val$limit) {
            this.val$skip = val$skip;
            this.val$limit = val$limit;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        OfInt unorderedSkipLimitSpliterator(OfInt s, long skip, long limit, long sizeIfKnown) {
            if (skip <= sizeIfKnown) {
                limit = limit >= 0 ? Math.min(limit, sizeIfKnown - skip) : sizeIfKnown - skip;
                skip = 0;
            }
            return new OfInt(s, skip, limit);
        }

        public <P_IN> Spliterator<Integer> opEvaluateParallelLazy(PipelineHelper<Integer> helper, Spliterator<P_IN> spliterator) {
            long size = helper.exactOutputSizeIfKnown(spliterator);
            if (size > 0 && spliterator.hasCharacteristics(Record.maxDataSize)) {
                return new OfInt((OfInt) helper.wrapSpliterator(spliterator), this.val$skip, SliceOps.calcSliceFence(this.val$skip, this.val$limit));
            }
            if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                return unorderedSkipLimitSpliterator((OfInt) helper.wrapSpliterator(spliterator), this.val$skip, this.val$limit, size);
            }
            return ((Node) new SliceTask(this, helper, spliterator, new -java_util_Spliterator_opEvaluateParallelLazy_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl0(), this.val$skip, this.val$limit).invoke()).spliterator();
        }

        public <P_IN> Node<Integer> opEvaluateParallel(PipelineHelper<Integer> helper, Spliterator<P_IN> spliterator, IntFunction<Integer[]> generator) {
            long size = helper.exactOutputSizeIfKnown(spliterator);
            if (size > 0 && spliterator.hasCharacteristics(Record.maxDataSize)) {
                return Nodes.collectInt(helper, SliceOps.sliceSpliterator(helper.getSourceShape(), spliterator, this.val$skip, this.val$limit), true);
            } else if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                return Nodes.collectInt(this, unorderedSkipLimitSpliterator((OfInt) helper.wrapSpliterator(spliterator), this.val$skip, this.val$limit, size), true);
            } else {
                return (Node) new SliceTask(this, helper, spliterator, generator, this.val$skip, this.val$limit).invoke();
            }
        }

        public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
            return new AnonymousClass1(sink, this.val$skip, this.val$limit);
        }
    }

    /* renamed from: java.util.stream.SliceOps.3 */
    static class AnonymousClass3 extends LongPipeline.StatefulOp<Long> {
        final /* synthetic */ long val$limit;
        final /* synthetic */ long val$skip;

        final /* synthetic */ class -java_util_Spliterator_opEvaluateParallelLazy_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl0 implements IntFunction {
            public Object apply(int arg0) {
                return new Long[arg0];
            }
        }

        /* renamed from: java.util.stream.SliceOps.3.1 */
        class AnonymousClass1 extends ChainedLong<Long> {
            long m;
            long n;
            final /* synthetic */ long val$limit;
            final /* synthetic */ long val$skip;

            AnonymousClass1(Sink $anonymous0, long val$skip, long val$limit) {
                this.val$skip = val$skip;
                this.val$limit = val$limit;
                super($anonymous0);
                this.n = this.val$skip;
                this.m = this.val$limit >= 0 ? this.val$limit : Long.MAX_VALUE;
            }

            public void begin(long size) {
                this.downstream.begin(SliceOps.calcSize(size, this.val$skip, this.m));
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
                return this.m != 0 ? this.downstream.cancellationRequested() : true;
            }
        }

        AnonymousClass3(AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, long val$skip, long val$limit) {
            this.val$skip = val$skip;
            this.val$limit = val$limit;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        OfLong unorderedSkipLimitSpliterator(OfLong s, long skip, long limit, long sizeIfKnown) {
            if (skip <= sizeIfKnown) {
                limit = limit >= 0 ? Math.min(limit, sizeIfKnown - skip) : sizeIfKnown - skip;
                skip = 0;
            }
            return new OfLong(s, skip, limit);
        }

        public <P_IN> Spliterator<Long> opEvaluateParallelLazy(PipelineHelper<Long> helper, Spliterator<P_IN> spliterator) {
            long size = helper.exactOutputSizeIfKnown(spliterator);
            if (size > 0 && spliterator.hasCharacteristics(Record.maxDataSize)) {
                return new OfLong((OfLong) helper.wrapSpliterator(spliterator), this.val$skip, SliceOps.calcSliceFence(this.val$skip, this.val$limit));
            }
            if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                return unorderedSkipLimitSpliterator((OfLong) helper.wrapSpliterator(spliterator), this.val$skip, this.val$limit, size);
            }
            return ((Node) new SliceTask(this, helper, spliterator, new -java_util_Spliterator_opEvaluateParallelLazy_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl0(), this.val$skip, this.val$limit).invoke()).spliterator();
        }

        public <P_IN> Node<Long> opEvaluateParallel(PipelineHelper<Long> helper, Spliterator<P_IN> spliterator, IntFunction<Long[]> generator) {
            long size = helper.exactOutputSizeIfKnown(spliterator);
            if (size > 0 && spliterator.hasCharacteristics(Record.maxDataSize)) {
                return Nodes.collectLong(helper, SliceOps.sliceSpliterator(helper.getSourceShape(), spliterator, this.val$skip, this.val$limit), true);
            } else if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                return Nodes.collectLong(this, unorderedSkipLimitSpliterator((OfLong) helper.wrapSpliterator(spliterator), this.val$skip, this.val$limit, size), true);
            } else {
                return (Node) new SliceTask(this, helper, spliterator, generator, this.val$skip, this.val$limit).invoke();
            }
        }

        public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
            return new AnonymousClass1(sink, this.val$skip, this.val$limit);
        }
    }

    /* renamed from: java.util.stream.SliceOps.4 */
    static class AnonymousClass4 extends DoublePipeline.StatefulOp<Double> {
        final /* synthetic */ long val$limit;
        final /* synthetic */ long val$skip;

        final /* synthetic */ class -java_util_Spliterator_opEvaluateParallelLazy_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl0 implements IntFunction {
            public Object apply(int arg0) {
                return new Double[arg0];
            }
        }

        /* renamed from: java.util.stream.SliceOps.4.1 */
        class AnonymousClass1 extends ChainedDouble<Double> {
            long m;
            long n;
            final /* synthetic */ long val$limit;
            final /* synthetic */ long val$skip;

            AnonymousClass1(Sink $anonymous0, long val$skip, long val$limit) {
                this.val$skip = val$skip;
                this.val$limit = val$limit;
                super($anonymous0);
                this.n = this.val$skip;
                this.m = this.val$limit >= 0 ? this.val$limit : Long.MAX_VALUE;
            }

            public void begin(long size) {
                this.downstream.begin(SliceOps.calcSize(size, this.val$skip, this.m));
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
                return this.m != 0 ? this.downstream.cancellationRequested() : true;
            }
        }

        AnonymousClass4(AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, long val$skip, long val$limit) {
            this.val$skip = val$skip;
            this.val$limit = val$limit;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        OfDouble unorderedSkipLimitSpliterator(OfDouble s, long skip, long limit, long sizeIfKnown) {
            if (skip <= sizeIfKnown) {
                limit = limit >= 0 ? Math.min(limit, sizeIfKnown - skip) : sizeIfKnown - skip;
                skip = 0;
            }
            return new OfDouble(s, skip, limit);
        }

        public <P_IN> Spliterator<Double> opEvaluateParallelLazy(PipelineHelper<Double> helper, Spliterator<P_IN> spliterator) {
            long size = helper.exactOutputSizeIfKnown(spliterator);
            if (size > 0 && spliterator.hasCharacteristics(Record.maxDataSize)) {
                return new OfDouble((OfDouble) helper.wrapSpliterator(spliterator), this.val$skip, SliceOps.calcSliceFence(this.val$skip, this.val$limit));
            }
            if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                return unorderedSkipLimitSpliterator((OfDouble) helper.wrapSpliterator(spliterator), this.val$skip, this.val$limit, size);
            }
            return ((Node) new SliceTask(this, helper, spliterator, new -java_util_Spliterator_opEvaluateParallelLazy_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl0(), this.val$skip, this.val$limit).invoke()).spliterator();
        }

        public <P_IN> Node<Double> opEvaluateParallel(PipelineHelper<Double> helper, Spliterator<P_IN> spliterator, IntFunction<Double[]> generator) {
            long size = helper.exactOutputSizeIfKnown(spliterator);
            if (size > 0 && spliterator.hasCharacteristics(Record.maxDataSize)) {
                return Nodes.collectDouble(helper, SliceOps.sliceSpliterator(helper.getSourceShape(), spliterator, this.val$skip, this.val$limit), true);
            } else if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                return Nodes.collectDouble(this, unorderedSkipLimitSpliterator((OfDouble) helper.wrapSpliterator(spliterator), this.val$skip, this.val$limit, size), true);
            } else {
                return (Node) new SliceTask(this, helper, spliterator, generator, this.val$skip, this.val$limit).invoke();
            }
        }

        public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
            return new AnonymousClass1(sink, this.val$skip, this.val$limit);
        }
    }

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
            if (this.targetSize >= 0 && !isRoot() && isLeftCompleted(this.targetOffset + this.targetSize)) {
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
            for (AbstractTask parent = (SliceTask) getParent(); parent != null; SliceTask parent2 = (SliceTask) parent.getParent()) {
                if (node == parent.rightChild) {
                    SliceTask<P_IN, P_OUT> left = parent.leftChild;
                    if (left != null) {
                        size += left.completedSize(target);
                        if (size >= target) {
                            return true;
                        }
                    } else {
                        continue;
                    }
                }
                node = parent;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.SliceOps.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.stream.SliceOps.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.stream.SliceOps.<clinit>():void");
    }

    private SliceOps() {
    }

    private static long calcSize(long size, long skip, long limit) {
        return size >= 0 ? Math.max(-1, Math.min(size - skip, limit)) : -1;
    }

    private static long calcSliceFence(long skip, long limit) {
        long sliceFence;
        if (limit >= 0) {
            sliceFence = skip + limit;
        } else {
            sliceFence = Long.MAX_VALUE;
        }
        if (sliceFence >= 0) {
            return sliceFence;
        }
        return Long.MAX_VALUE;
    }

    private static <P_IN> Spliterator<P_IN> sliceSpliterator(StreamShape shape, Spliterator<P_IN> s, long skip, long limit) {
        if (-assertionsDisabled || s.hasCharacteristics(Record.maxDataSize)) {
            long sliceFence = calcSliceFence(skip, limit);
            switch (-getjava-util-stream-StreamShapeSwitchesValues()[shape.ordinal()]) {
                case BaseCalendar.SUNDAY /*1*/:
                    return new OfDouble((OfDouble) s, skip, sliceFence);
                case BaseCalendar.MONDAY /*2*/:
                    return new OfInt((OfInt) s, skip, sliceFence);
                case BaseCalendar.TUESDAY /*3*/:
                    return new OfLong((OfLong) s, skip, sliceFence);
                case BaseCalendar.WEDNESDAY /*4*/:
                    return new OfRef(s, skip, sliceFence);
                default:
                    throw new IllegalStateException("Unknown shape " + shape);
            }
        }
        throw new AssertionError();
    }

    private static <T> IntFunction<T[]> castingArray() {
        return new -java_util_function_IntFunction_castingArray__LambdaImpl0();
    }

    public static <T> Stream<T> makeRef(AbstractPipeline<?, T, ?> upstream, long skip, long limit) {
        if (skip < 0) {
            throw new IllegalArgumentException("Skip must be non-negative: " + skip);
        }
        return new AnonymousClass1(upstream, StreamShape.REFERENCE, flags(limit), skip, limit);
    }

    public static IntStream makeInt(AbstractPipeline<?, Integer, ?> upstream, long skip, long limit) {
        if (skip < 0) {
            throw new IllegalArgumentException("Skip must be non-negative: " + skip);
        }
        return new AnonymousClass2(upstream, StreamShape.INT_VALUE, flags(limit), skip, limit);
    }

    public static LongStream makeLong(AbstractPipeline<?, Long, ?> upstream, long skip, long limit) {
        if (skip < 0) {
            throw new IllegalArgumentException("Skip must be non-negative: " + skip);
        }
        return new AnonymousClass3(upstream, StreamShape.LONG_VALUE, flags(limit), skip, limit);
    }

    public static DoubleStream makeDouble(AbstractPipeline<?, Double, ?> upstream, long skip, long limit) {
        if (skip < 0) {
            throw new IllegalArgumentException("Skip must be non-negative: " + skip);
        }
        return new AnonymousClass4(upstream, StreamShape.DOUBLE_VALUE, flags(limit), skip, limit);
    }

    private static int flags(long limit) {
        return (limit != -1 ? StreamOpFlag.IS_SHORT_CIRCUIT : 0) | StreamOpFlag.NOT_SIZED;
    }
}
