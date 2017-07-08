package java.util.stream;

import java.util.DoubleSummaryStatistics;
import java.util.Iterator;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterator.OfDouble;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntFunction;
import java.util.function.ObjDoubleConsumer;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.Node.Builder;
import java.util.stream.Sink.ChainedDouble;

public abstract class DoublePipeline<E_IN> extends AbstractPipeline<E_IN, Double, DoubleStream> implements DoubleStream {

    final /* synthetic */ class -double__toArray__LambdaImpl0 implements IntFunction {
        public Object apply(int arg0) {
            return new Double[arg0];
        }
    }

    final /* synthetic */ class -double_sum__LambdaImpl0 implements Supplier {
        public Object get() {
            return new double[3];
        }
    }

    final /* synthetic */ class -double_sum__LambdaImpl1 implements ObjDoubleConsumer {
        public void accept(Object arg0, double arg1) {
            DoublePipeline.-java_util_stream_DoublePipeline_lambda$6((double[]) arg0, arg1);
        }
    }

    final /* synthetic */ class -double_sum__LambdaImpl2 implements BiConsumer {
        public void accept(Object arg0, Object arg1) {
            DoublePipeline.-java_util_stream_DoublePipeline_lambda$7((double[]) arg0, (double[]) arg1);
        }
    }

    final /* synthetic */ class -java_lang_Object_collect_java_util_function_Supplier_supplier_java_util_function_ObjDoubleConsumer_accumulator_java_util_function_BiConsumer_combiner_LambdaImpl0 implements BinaryOperator {
        private /* synthetic */ BiConsumer val$combiner;

        public /* synthetic */ -java_lang_Object_collect_java_util_function_Supplier_supplier_java_util_function_ObjDoubleConsumer_accumulator_java_util_function_BiConsumer_combiner_LambdaImpl0(BiConsumer biConsumer) {
            this.val$combiner = biConsumer;
        }

        public Object apply(Object arg0, Object arg1) {
            return this.val$combiner.accept(arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_DoubleSummaryStatistics_summaryStatistics__LambdaImpl0 implements Supplier {
        public Object get() {
            return new DoubleSummaryStatistics();
        }
    }

    final /* synthetic */ class -java_util_DoubleSummaryStatistics_summaryStatistics__LambdaImpl1 implements ObjDoubleConsumer {
        public void accept(Object arg0, double arg1) {
            ((DoubleSummaryStatistics) arg0).accept(arg1);
        }
    }

    final /* synthetic */ class -java_util_DoubleSummaryStatistics_summaryStatistics__LambdaImpl2 implements BiConsumer {
        public void accept(Object arg0, Object arg1) {
            ((DoubleSummaryStatistics) arg0).combine((DoubleSummaryStatistics) arg1);
        }
    }

    final /* synthetic */ class -java_util_OptionalDouble_average__LambdaImpl0 implements Supplier {
        public Object get() {
            return new double[4];
        }
    }

    final /* synthetic */ class -java_util_OptionalDouble_average__LambdaImpl1 implements ObjDoubleConsumer {
        public void accept(Object arg0, double arg1) {
            DoublePipeline.-java_util_stream_DoublePipeline_lambda$11((double[]) arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_OptionalDouble_average__LambdaImpl2 implements BiConsumer {
        public void accept(Object arg0, Object arg1) {
            DoublePipeline.-java_util_stream_DoublePipeline_lambda$12((double[]) arg0, (double[]) arg1);
        }
    }

    final /* synthetic */ class -java_util_OptionalDouble_max__LambdaImpl0 implements DoubleBinaryOperator {
        public double applyAsDouble(double arg0, double arg1) {
            return Math.max(arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_OptionalDouble_min__LambdaImpl0 implements DoubleBinaryOperator {
        public double applyAsDouble(double arg0, double arg1) {
            return Math.min(arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_function_DoubleConsumer_adapt_java_util_stream_Sink_sink_LambdaImpl0 implements DoubleConsumer {
        private /* synthetic */ Sink val$-lambdaCtx;

        public /* synthetic */ -java_util_function_DoubleConsumer_adapt_java_util_stream_Sink_sink_LambdaImpl0(Sink sink) {
            this.val$-lambdaCtx = sink;
        }

        public void accept(double arg0) {
            this.val$-lambdaCtx.accept(arg0);
        }
    }

    final /* synthetic */ class -java_util_stream_DoubleStream_distinct__LambdaImpl0 implements ToDoubleFunction {
        public double applyAsDouble(Object arg0) {
            return ((Double) arg0).doubleValue();
        }
    }

    final /* synthetic */ class -java_util_stream_Stream_boxed__LambdaImpl0 implements DoubleFunction {
        public Object apply(double arg0) {
            return Double.valueOf(arg0);
        }
    }

    final /* synthetic */ class -long_count__LambdaImpl0 implements DoubleToLongFunction {
        public long applyAsLong(double arg0) {
            return 1;
        }
    }

    public static abstract class StatelessOp<E_IN> extends DoublePipeline<E_IN> {
        static final /* synthetic */ boolean -assertionsDisabled = false;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.DoublePipeline.StatelessOp.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.stream.DoublePipeline.StatelessOp.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DoublePipeline.StatelessOp.<clinit>():void");
        }

        public StatelessOp(AbstractPipeline<?, E_IN, ?> upstream, StreamShape inputShape, int opFlags) {
            super(upstream, opFlags);
            if (!-assertionsDisabled) {
                if ((upstream.getOutputShape() == inputShape ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
        }

        public final boolean opIsStateful() {
            return false;
        }
    }

    /* renamed from: java.util.stream.DoublePipeline.1 */
    class AnonymousClass1 extends StatelessOp<Double> {
        final /* synthetic */ DoublePipeline this$0;
        final /* synthetic */ DoubleUnaryOperator val$mapper;

        /* renamed from: java.util.stream.DoublePipeline.1.1 */
        class AnonymousClass1 extends ChainedDouble<Double> {
            final /* synthetic */ AnonymousClass1 this$1;
            final /* synthetic */ DoubleUnaryOperator val$mapper;

            AnonymousClass1(AnonymousClass1 this$1, Sink $anonymous0, DoubleUnaryOperator val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
            }

            public void accept(double t) {
                this.downstream.accept(this.val$mapper.applyAsDouble(t));
            }
        }

        AnonymousClass1(DoublePipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, DoubleUnaryOperator val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    /* renamed from: java.util.stream.DoublePipeline.2 */
    class AnonymousClass2 extends java.util.stream.ReferencePipeline.StatelessOp<Double, U> {
        final /* synthetic */ DoublePipeline this$0;
        final /* synthetic */ DoubleFunction val$mapper;

        /* renamed from: java.util.stream.DoublePipeline.2.1 */
        class AnonymousClass1 extends ChainedDouble<U> {
            final /* synthetic */ AnonymousClass2 this$1;
            final /* synthetic */ DoubleFunction val$mapper;

            AnonymousClass1(AnonymousClass2 this$1, Sink $anonymous0, DoubleFunction val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
            }

            public void accept(double t) {
                this.downstream.accept(this.val$mapper.apply(t));
            }
        }

        AnonymousClass2(DoublePipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, DoubleFunction val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Double> opWrapSink(int flags, Sink<U> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    /* renamed from: java.util.stream.DoublePipeline.3 */
    class AnonymousClass3 extends java.util.stream.IntPipeline.StatelessOp<Double> {
        final /* synthetic */ DoublePipeline this$0;
        final /* synthetic */ DoubleToIntFunction val$mapper;

        /* renamed from: java.util.stream.DoublePipeline.3.1 */
        class AnonymousClass1 extends ChainedDouble<Integer> {
            final /* synthetic */ AnonymousClass3 this$1;
            final /* synthetic */ DoubleToIntFunction val$mapper;

            AnonymousClass1(AnonymousClass3 this$1, Sink $anonymous0, DoubleToIntFunction val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
            }

            public void accept(double t) {
                this.downstream.accept(this.val$mapper.applyAsInt(t));
            }
        }

        AnonymousClass3(DoublePipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, DoubleToIntFunction val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Double> opWrapSink(int flags, Sink<Integer> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    /* renamed from: java.util.stream.DoublePipeline.4 */
    class AnonymousClass4 extends java.util.stream.LongPipeline.StatelessOp<Double> {
        final /* synthetic */ DoublePipeline this$0;
        final /* synthetic */ DoubleToLongFunction val$mapper;

        /* renamed from: java.util.stream.DoublePipeline.4.1 */
        class AnonymousClass1 extends ChainedDouble<Long> {
            final /* synthetic */ AnonymousClass4 this$1;
            final /* synthetic */ DoubleToLongFunction val$mapper;

            AnonymousClass1(AnonymousClass4 this$1, Sink $anonymous0, DoubleToLongFunction val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
            }

            public void accept(double t) {
                this.downstream.accept(this.val$mapper.applyAsLong(t));
            }
        }

        AnonymousClass4(DoublePipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, DoubleToLongFunction val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Double> opWrapSink(int flags, Sink<Long> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    /* renamed from: java.util.stream.DoublePipeline.5 */
    class AnonymousClass5 extends StatelessOp<Double> {
        final /* synthetic */ DoublePipeline this$0;
        final /* synthetic */ DoubleFunction val$mapper;

        /* renamed from: java.util.stream.DoublePipeline.5.1 */
        class AnonymousClass1 extends ChainedDouble<Double> {
            final /* synthetic */ AnonymousClass5 this$1;
            final /* synthetic */ DoubleFunction val$mapper;

            final /* synthetic */ class -void_accept_double_t_LambdaImpl0 implements DoubleConsumer {
                private /* synthetic */ AnonymousClass1 val$this;

                public /* synthetic */ -void_accept_double_t_LambdaImpl0(AnonymousClass1 anonymousClass1) {
                    this.val$this = anonymousClass1;
                }

                public void accept(double arg0) {
                    this.val$this.-java_util_stream_DoublePipeline$5$1_lambda$3(arg0);
                }
            }

            AnonymousClass1(AnonymousClass5 this$1, Sink $anonymous0, DoubleFunction val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
            }

            public void begin(long size) {
                this.downstream.begin(-1);
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void accept(double t) {
                Throwable th;
                Throwable th2 = null;
                DoubleStream doubleStream = null;
                try {
                    doubleStream = (DoubleStream) this.val$mapper.apply(t);
                    if (doubleStream != null) {
                        doubleStream.sequential().forEach(new -void_accept_double_t_LambdaImpl0());
                    }
                    if (doubleStream != null) {
                        try {
                            doubleStream.close();
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                    }
                    if (th2 != null) {
                        throw th2;
                    }
                } catch (Throwable th22) {
                    Throwable th4 = th22;
                    th22 = th;
                    th = th4;
                }
            }

            /* synthetic */ void -java_util_stream_DoublePipeline$5$1_lambda$3(double i) {
                this.downstream.accept(i);
            }
        }

        AnonymousClass5(DoublePipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, DoubleFunction val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    /* renamed from: java.util.stream.DoublePipeline.6 */
    class AnonymousClass6 extends StatelessOp<Double> {
        final /* synthetic */ DoublePipeline this$0;

        AnonymousClass6(DoublePipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2) {
            this.this$0 = this$0;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
            return sink;
        }
    }

    /* renamed from: java.util.stream.DoublePipeline.7 */
    class AnonymousClass7 extends StatelessOp<Double> {
        final /* synthetic */ DoublePipeline this$0;
        final /* synthetic */ DoublePredicate val$predicate;

        /* renamed from: java.util.stream.DoublePipeline.7.1 */
        class AnonymousClass1 extends ChainedDouble<Double> {
            final /* synthetic */ AnonymousClass7 this$1;
            final /* synthetic */ DoublePredicate val$predicate;

            AnonymousClass1(AnonymousClass7 this$1, Sink $anonymous0, DoublePredicate val$predicate) {
                this.this$1 = this$1;
                this.val$predicate = val$predicate;
                super($anonymous0);
            }

            public void begin(long size) {
                this.downstream.begin(-1);
            }

            public void accept(double t) {
                if (this.val$predicate.test(t)) {
                    this.downstream.accept(t);
                }
            }
        }

        AnonymousClass7(DoublePipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, DoublePredicate val$predicate) {
            this.this$0 = this$0;
            this.val$predicate = val$predicate;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
            return new AnonymousClass1(this, sink, this.val$predicate);
        }
    }

    /* renamed from: java.util.stream.DoublePipeline.8 */
    class AnonymousClass8 extends StatelessOp<Double> {
        final /* synthetic */ DoublePipeline this$0;
        final /* synthetic */ DoubleConsumer val$action;

        /* renamed from: java.util.stream.DoublePipeline.8.1 */
        class AnonymousClass1 extends ChainedDouble<Double> {
            final /* synthetic */ AnonymousClass8 this$1;
            final /* synthetic */ DoubleConsumer val$action;

            AnonymousClass1(AnonymousClass8 this$1, Sink $anonymous0, DoubleConsumer val$action) {
                this.this$1 = this$1;
                this.val$action = val$action;
                super($anonymous0);
            }

            public void accept(double t) {
                this.val$action.accept(t);
                this.downstream.accept(t);
            }
        }

        AnonymousClass8(DoublePipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, DoubleConsumer val$action) {
            this.this$0 = this$0;
            this.val$action = val$action;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
            return new AnonymousClass1(this, sink, this.val$action);
        }
    }

    public static class Head<E_IN> extends DoublePipeline<E_IN> {
        public Head(Supplier<? extends Spliterator<Double>> source, int sourceFlags, boolean parallel) {
            super((Supplier) source, sourceFlags, parallel);
        }

        public Head(Spliterator<Double> source, int sourceFlags, boolean parallel) {
            super((Spliterator) source, sourceFlags, parallel);
        }

        public final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        public final Sink<E_IN> opWrapSink(int flags, Sink<Double> sink) {
            throw new UnsupportedOperationException();
        }

        public void forEach(DoubleConsumer consumer) {
            if (isParallel()) {
                super.forEach(consumer);
            } else {
                DoublePipeline.adapt(sourceStageSpliterator()).forEachRemaining(consumer);
            }
        }

        public void forEachOrdered(DoubleConsumer consumer) {
            if (isParallel()) {
                super.forEachOrdered(consumer);
            } else {
                DoublePipeline.adapt(sourceStageSpliterator()).forEachRemaining(consumer);
            }
        }
    }

    public static abstract class StatefulOp<E_IN> extends DoublePipeline<E_IN> {
        static final /* synthetic */ boolean -assertionsDisabled = false;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.DoublePipeline.StatefulOp.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.stream.DoublePipeline.StatefulOp.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DoublePipeline.StatefulOp.<clinit>():void");
        }

        public abstract <P_IN> Node<Double> opEvaluateParallel(PipelineHelper<Double> pipelineHelper, Spliterator<P_IN> spliterator, IntFunction<Double[]> intFunction);

        public StatefulOp(AbstractPipeline<?, E_IN, ?> upstream, StreamShape inputShape, int opFlags) {
            super(upstream, opFlags);
            if (!-assertionsDisabled) {
                if ((upstream.getOutputShape() == inputShape ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
        }

        public final boolean opIsStateful() {
            return true;
        }
    }

    DoublePipeline(Supplier<? extends Spliterator<Double>> source, int sourceFlags, boolean parallel) {
        super((Supplier) source, sourceFlags, parallel);
    }

    DoublePipeline(Spliterator<Double> source, int sourceFlags, boolean parallel) {
        super((Spliterator) source, sourceFlags, parallel);
    }

    DoublePipeline(AbstractPipeline<?, E_IN, ?> upstream, int opFlags) {
        super(upstream, opFlags);
    }

    private static DoubleConsumer adapt(Sink<Double> sink) {
        if (sink instanceof DoubleConsumer) {
            return (DoubleConsumer) sink;
        }
        if (Tripwire.ENABLED) {
            Tripwire.trip(AbstractPipeline.class, "using DoubleStream.adapt(Sink<Double> s)");
        }
        sink.getClass();
        return new -java_util_function_DoubleConsumer_adapt_java_util_stream_Sink_sink_LambdaImpl0(sink);
    }

    private static OfDouble adapt(Spliterator<Double> s) {
        if (s instanceof OfDouble) {
            return (OfDouble) s;
        }
        if (Tripwire.ENABLED) {
            Tripwire.trip(AbstractPipeline.class, "using DoubleStream.adapt(Spliterator<Double> s)");
        }
        throw new UnsupportedOperationException("DoubleStream.adapt(Spliterator<Double> s)");
    }

    public final StreamShape getOutputShape() {
        return StreamShape.DOUBLE_VALUE;
    }

    public final <P_IN> Node<Double> evaluateToNode(PipelineHelper<Double> helper, Spliterator<P_IN> spliterator, boolean flattenTree, IntFunction<Double[]> intFunction) {
        return Nodes.collectDouble(helper, spliterator, flattenTree);
    }

    public final <P_IN> Spliterator<Double> wrap(PipelineHelper<Double> ph, Supplier<Spliterator<P_IN>> supplier, boolean isParallel) {
        return new DoubleWrappingSpliterator((PipelineHelper) ph, (Supplier) supplier, isParallel);
    }

    public /* bridge */ /* synthetic */ Spliterator m14lazySpliterator(Supplier supplier) {
        return lazySpliterator(supplier);
    }

    public final OfDouble lazySpliterator(Supplier<? extends Spliterator<Double>> supplier) {
        return new OfDouble(supplier);
    }

    public final void forEachWithCancel(Spliterator<Double> spliterator, Sink<Double> sink) {
        OfDouble spl = adapt((Spliterator) spliterator);
        DoubleConsumer adaptedSink = adapt((Sink) sink);
        while (!sink.cancellationRequested()) {
            if (!spl.tryAdvance(adaptedSink)) {
                return;
            }
        }
    }

    public final Builder<Double> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Double[]> intFunction) {
        return Nodes.doubleBuilder(exactSizeIfKnown);
    }

    public /* bridge */ /* synthetic */ Iterator iterator() {
        return iterator();
    }

    public final PrimitiveIterator.OfDouble m13iterator() {
        return Spliterators.iterator(spliterator());
    }

    public /* bridge */ /* synthetic */ Spliterator m15spliterator() {
        return spliterator();
    }

    public final OfDouble spliterator() {
        return adapt(super.spliterator());
    }

    public final Stream<Double> boxed() {
        return mapToObj(new -java_util_stream_Stream_boxed__LambdaImpl0());
    }

    public final DoubleStream map(DoubleUnaryOperator mapper) {
        Objects.requireNonNull(mapper);
        return new AnonymousClass1(this, this, StreamShape.DOUBLE_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT, mapper);
    }

    public final <U> Stream<U> mapToObj(DoubleFunction<? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return new AnonymousClass2(this, this, StreamShape.DOUBLE_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT, mapper);
    }

    public final IntStream mapToInt(DoubleToIntFunction mapper) {
        Objects.requireNonNull(mapper);
        return new AnonymousClass3(this, this, StreamShape.DOUBLE_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT, mapper);
    }

    public final LongStream mapToLong(DoubleToLongFunction mapper) {
        Objects.requireNonNull(mapper);
        return new AnonymousClass4(this, this, StreamShape.DOUBLE_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT, mapper);
    }

    public final DoubleStream flatMap(DoubleFunction<? extends DoubleStream> mapper) {
        return new AnonymousClass5(this, this, StreamShape.DOUBLE_VALUE, (StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) | StreamOpFlag.NOT_SIZED, mapper);
    }

    public /* bridge */ /* synthetic */ BaseStream unordered() {
        return unordered();
    }

    public DoubleStream m16unordered() {
        if (isOrdered()) {
            return new AnonymousClass6(this, this, StreamShape.DOUBLE_VALUE, StreamOpFlag.NOT_ORDERED);
        }
        return this;
    }

    public final DoubleStream filter(DoublePredicate predicate) {
        Objects.requireNonNull(predicate);
        return new AnonymousClass7(this, this, StreamShape.DOUBLE_VALUE, StreamOpFlag.NOT_SIZED, predicate);
    }

    public /* bridge */ /* synthetic */ DoubleStream sequential() {
        return (DoubleStream) sequential();
    }

    public /* bridge */ /* synthetic */ DoubleStream parallel() {
        return (DoubleStream) parallel();
    }

    public final DoubleStream peek(DoubleConsumer action) {
        Objects.requireNonNull(action);
        return new AnonymousClass8(this, this, StreamShape.DOUBLE_VALUE, 0, action);
    }

    public final DoubleStream limit(long maxSize) {
        if (maxSize >= 0) {
            return SliceOps.makeDouble(this, 0, maxSize);
        }
        throw new IllegalArgumentException(Long.toString(maxSize));
    }

    public final DoubleStream skip(long n) {
        if (n < 0) {
            throw new IllegalArgumentException(Long.toString(n));
        } else if (n == 0) {
            return this;
        } else {
            return SliceOps.makeDouble(this, n, -1);
        }
    }

    public final DoubleStream sorted() {
        return SortedOps.makeDouble(this);
    }

    public final DoubleStream distinct() {
        return boxed().distinct().mapToDouble(new -java_util_stream_DoubleStream_distinct__LambdaImpl0());
    }

    public void forEach(DoubleConsumer consumer) {
        evaluate(ForEachOps.makeDouble(consumer, false));
    }

    public void forEachOrdered(DoubleConsumer consumer) {
        evaluate(ForEachOps.makeDouble(consumer, true));
    }

    public final double sum() {
        return Collectors.computeFinalSum((double[]) collect(new -double_sum__LambdaImpl0(), new -double_sum__LambdaImpl1(), new -double_sum__LambdaImpl2()));
    }

    static /* synthetic */ void -java_util_stream_DoublePipeline_lambda$6(double[] ll, double d) {
        Collectors.sumWithCompensation(ll, d);
        ll[2] = ll[2] + d;
    }

    static /* synthetic */ void -java_util_stream_DoublePipeline_lambda$7(double[] ll, double[] rr) {
        Collectors.sumWithCompensation(ll, rr[0]);
        Collectors.sumWithCompensation(ll, rr[1]);
        ll[2] = ll[2] + rr[2];
    }

    public final OptionalDouble min() {
        return reduce(new -java_util_OptionalDouble_min__LambdaImpl0());
    }

    public final OptionalDouble max() {
        return reduce(new -java_util_OptionalDouble_max__LambdaImpl0());
    }

    public final OptionalDouble average() {
        double[] avg = (double[]) collect(new -java_util_OptionalDouble_average__LambdaImpl0(), new -java_util_OptionalDouble_average__LambdaImpl1(), new -java_util_OptionalDouble_average__LambdaImpl2());
        if (avg[2] > 0.0d) {
            return OptionalDouble.of(Collectors.computeFinalSum(avg) / avg[2]);
        }
        return OptionalDouble.empty();
    }

    static /* synthetic */ void -java_util_stream_DoublePipeline_lambda$11(double[] ll, double d) {
        ll[2] = ll[2] + 1.0d;
        Collectors.sumWithCompensation(ll, d);
        ll[3] = ll[3] + d;
    }

    static /* synthetic */ void -java_util_stream_DoublePipeline_lambda$12(double[] ll, double[] rr) {
        Collectors.sumWithCompensation(ll, rr[0]);
        Collectors.sumWithCompensation(ll, rr[1]);
        ll[2] = ll[2] + rr[2];
        ll[3] = ll[3] + rr[3];
    }

    public final long count() {
        return mapToLong(new -long_count__LambdaImpl0()).sum();
    }

    public final DoubleSummaryStatistics summaryStatistics() {
        return (DoubleSummaryStatistics) collect(new -java_util_DoubleSummaryStatistics_summaryStatistics__LambdaImpl0(), new -java_util_DoubleSummaryStatistics_summaryStatistics__LambdaImpl1(), new -java_util_DoubleSummaryStatistics_summaryStatistics__LambdaImpl2());
    }

    public final double reduce(double identity, DoubleBinaryOperator op) {
        return ((Double) evaluate(ReduceOps.makeDouble(identity, op))).doubleValue();
    }

    public final OptionalDouble reduce(DoubleBinaryOperator op) {
        return (OptionalDouble) evaluate(ReduceOps.makeDouble(op));
    }

    public final <R> R collect(Supplier<R> supplier, ObjDoubleConsumer<R> accumulator, BiConsumer<R, R> combiner) {
        return evaluate(ReduceOps.makeDouble(supplier, accumulator, new -java_lang_Object_collect_java_util_function_Supplier_supplier_java_util_function_ObjDoubleConsumer_accumulator_java_util_function_BiConsumer_combiner_LambdaImpl0(combiner)));
    }

    public final boolean anyMatch(DoublePredicate predicate) {
        return ((Boolean) evaluate(MatchOps.makeDouble(predicate, MatchKind.ANY))).booleanValue();
    }

    public final boolean allMatch(DoublePredicate predicate) {
        return ((Boolean) evaluate(MatchOps.makeDouble(predicate, MatchKind.ALL))).booleanValue();
    }

    public final boolean noneMatch(DoublePredicate predicate) {
        return ((Boolean) evaluate(MatchOps.makeDouble(predicate, MatchKind.NONE))).booleanValue();
    }

    public final OptionalDouble findFirst() {
        return (OptionalDouble) evaluate(FindOps.makeDouble(true));
    }

    public final OptionalDouble findAny() {
        return (OptionalDouble) evaluate(FindOps.makeDouble(false));
    }

    public final double[] toArray() {
        return (double[]) Nodes.flattenDouble((Node.OfDouble) evaluateToArrayNode(new -double__toArray__LambdaImpl0())).asPrimitiveArray();
    }
}
