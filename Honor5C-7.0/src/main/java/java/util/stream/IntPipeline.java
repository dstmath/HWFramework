package java.util.stream;

import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterator.OfInt;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Node.Builder;
import java.util.stream.Sink.ChainedInt;

public abstract class IntPipeline<E_IN> extends AbstractPipeline<E_IN, Integer, IntStream> implements IntStream {

    public static abstract class StatelessOp<E_IN> extends IntPipeline<E_IN> {
        static final /* synthetic */ boolean -assertionsDisabled = false;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.IntPipeline.StatelessOp.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.stream.IntPipeline.StatelessOp.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.IntPipeline.StatelessOp.<clinit>():void");
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

    final /* synthetic */ class -int__toArray__LambdaImpl0 implements IntFunction {
        public /* synthetic */ -int__toArray__LambdaImpl0() {
        }

        public Object apply(int arg0) {
            return new Integer[arg0];
        }
    }

    final /* synthetic */ class -int_sum__LambdaImpl0 implements IntBinaryOperator {
        public /* synthetic */ -int_sum__LambdaImpl0() {
        }

        public int applyAsInt(int arg0, int arg1) {
            return Integer.sum(arg0, arg1);
        }
    }

    final /* synthetic */ class -java_lang_Object_collect_java_util_function_Supplier_supplier_java_util_function_ObjIntConsumer_accumulator_java_util_function_BiConsumer_combiner_LambdaImpl0 implements BinaryOperator {
        private /* synthetic */ BiConsumer val$combiner;

        public /* synthetic */ -java_lang_Object_collect_java_util_function_Supplier_supplier_java_util_function_ObjIntConsumer_accumulator_java_util_function_BiConsumer_combiner_LambdaImpl0(BiConsumer biConsumer) {
            this.val$combiner = biConsumer;
        }

        public Object apply(Object arg0, Object arg1) {
            return this.val$combiner.accept(arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_IntSummaryStatistics_summaryStatistics__LambdaImpl0 implements Supplier {
        public /* synthetic */ -java_util_IntSummaryStatistics_summaryStatistics__LambdaImpl0() {
        }

        public Object get() {
            return new IntSummaryStatistics();
        }
    }

    final /* synthetic */ class -java_util_IntSummaryStatistics_summaryStatistics__LambdaImpl1 implements ObjIntConsumer {
        public /* synthetic */ -java_util_IntSummaryStatistics_summaryStatistics__LambdaImpl1() {
        }

        public void accept(Object arg0, int arg1) {
            ((IntSummaryStatistics) arg0).accept(arg1);
        }
    }

    final /* synthetic */ class -java_util_IntSummaryStatistics_summaryStatistics__LambdaImpl2 implements BiConsumer {
        public /* synthetic */ -java_util_IntSummaryStatistics_summaryStatistics__LambdaImpl2() {
        }

        public void accept(Object arg0, Object arg1) {
            ((IntSummaryStatistics) arg0).combine((IntSummaryStatistics) arg1);
        }
    }

    final /* synthetic */ class -java_util_OptionalDouble_average__LambdaImpl0 implements Supplier {
        public /* synthetic */ -java_util_OptionalDouble_average__LambdaImpl0() {
        }

        public Object get() {
            return new long[2];
        }
    }

    final /* synthetic */ class -java_util_OptionalDouble_average__LambdaImpl1 implements ObjIntConsumer {
        public /* synthetic */ -java_util_OptionalDouble_average__LambdaImpl1() {
        }

        public void accept(Object arg0, int arg1) {
            IntPipeline.-java_util_stream_IntPipeline_lambda$10((long[]) arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_OptionalDouble_average__LambdaImpl2 implements BiConsumer {
        public /* synthetic */ -java_util_OptionalDouble_average__LambdaImpl2() {
        }

        public void accept(Object arg0, Object arg1) {
            IntPipeline.-java_util_stream_IntPipeline_lambda$11((long[]) arg0, (long[]) arg1);
        }
    }

    final /* synthetic */ class -java_util_OptionalInt_max__LambdaImpl0 implements IntBinaryOperator {
        public /* synthetic */ -java_util_OptionalInt_max__LambdaImpl0() {
        }

        public int applyAsInt(int arg0, int arg1) {
            return Math.max(arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_OptionalInt_min__LambdaImpl0 implements IntBinaryOperator {
        public /* synthetic */ -java_util_OptionalInt_min__LambdaImpl0() {
        }

        public int applyAsInt(int arg0, int arg1) {
            return Math.min(arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_function_IntConsumer_adapt_java_util_stream_Sink_sink_LambdaImpl0 implements IntConsumer {
        private /* synthetic */ Sink val$-lambdaCtx;

        public /* synthetic */ -java_util_function_IntConsumer_adapt_java_util_stream_Sink_sink_LambdaImpl0(Sink sink) {
            this.val$-lambdaCtx = sink;
        }

        public void accept(int arg0) {
            this.val$-lambdaCtx.accept(arg0);
        }
    }

    final /* synthetic */ class -java_util_stream_IntStream_distinct__LambdaImpl0 implements ToIntFunction {
        public /* synthetic */ -java_util_stream_IntStream_distinct__LambdaImpl0() {
        }

        public int applyAsInt(Object arg0) {
            return ((Integer) arg0).intValue();
        }
    }

    final /* synthetic */ class -java_util_stream_Stream_boxed__LambdaImpl0 implements IntFunction {
        public /* synthetic */ -java_util_stream_Stream_boxed__LambdaImpl0() {
        }

        public Object apply(int arg0) {
            return Integer.valueOf(arg0);
        }
    }

    final /* synthetic */ class -long_count__LambdaImpl0 implements IntToLongFunction {
        public /* synthetic */ -long_count__LambdaImpl0() {
        }

        public long applyAsLong(int arg0) {
            return 1;
        }
    }

    /* renamed from: java.util.stream.IntPipeline.10 */
    class AnonymousClass10 extends StatelessOp<Integer> {
        final /* synthetic */ IntPipeline this$0;
        final /* synthetic */ IntConsumer val$action;

        /* renamed from: java.util.stream.IntPipeline.10.1 */
        class AnonymousClass1 extends ChainedInt<Integer> {
            final /* synthetic */ AnonymousClass10 this$1;
            final /* synthetic */ IntConsumer val$action;

            AnonymousClass1(AnonymousClass10 this$1, Sink $anonymous0, IntConsumer val$action) {
                this.this$1 = this$1;
                this.val$action = val$action;
                super($anonymous0);
            }

            public void accept(int t) {
                this.val$action.accept(t);
                this.downstream.accept(t);
            }
        }

        AnonymousClass10(IntPipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, IntConsumer val$action) {
            this.this$0 = this$0;
            this.val$action = val$action;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
            return new AnonymousClass1(this, sink, this.val$action);
        }
    }

    /* renamed from: java.util.stream.IntPipeline.1 */
    class AnonymousClass1 extends java.util.stream.LongPipeline.StatelessOp<Integer> {
        final /* synthetic */ IntPipeline this$0;

        /* renamed from: java.util.stream.IntPipeline.1.1 */
        class AnonymousClass1 extends ChainedInt<Long> {
            final /* synthetic */ AnonymousClass1 this$1;

            AnonymousClass1(AnonymousClass1 this$1, Sink $anonymous0) {
                this.this$1 = this$1;
                super($anonymous0);
            }

            public void accept(int t) {
                this.downstream.accept((long) t);
            }
        }

        AnonymousClass1(IntPipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2) {
            this.this$0 = this$0;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Integer> opWrapSink(int flags, Sink<Long> sink) {
            return new AnonymousClass1(this, sink);
        }
    }

    /* renamed from: java.util.stream.IntPipeline.2 */
    class AnonymousClass2 extends java.util.stream.DoublePipeline.StatelessOp<Integer> {
        final /* synthetic */ IntPipeline this$0;

        /* renamed from: java.util.stream.IntPipeline.2.1 */
        class AnonymousClass1 extends ChainedInt<Double> {
            final /* synthetic */ AnonymousClass2 this$1;

            AnonymousClass1(AnonymousClass2 this$1, Sink $anonymous0) {
                this.this$1 = this$1;
                super($anonymous0);
            }

            public void accept(int t) {
                this.downstream.accept((double) t);
            }
        }

        AnonymousClass2(IntPipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2) {
            this.this$0 = this$0;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Integer> opWrapSink(int flags, Sink<Double> sink) {
            return new AnonymousClass1(this, sink);
        }
    }

    /* renamed from: java.util.stream.IntPipeline.3 */
    class AnonymousClass3 extends StatelessOp<Integer> {
        final /* synthetic */ IntPipeline this$0;
        final /* synthetic */ IntUnaryOperator val$mapper;

        /* renamed from: java.util.stream.IntPipeline.3.1 */
        class AnonymousClass1 extends ChainedInt<Integer> {
            final /* synthetic */ AnonymousClass3 this$1;
            final /* synthetic */ IntUnaryOperator val$mapper;

            AnonymousClass1(AnonymousClass3 this$1, Sink $anonymous0, IntUnaryOperator val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
            }

            public void accept(int t) {
                this.downstream.accept(this.val$mapper.applyAsInt(t));
            }
        }

        AnonymousClass3(IntPipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, IntUnaryOperator val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    /* renamed from: java.util.stream.IntPipeline.4 */
    class AnonymousClass4 extends java.util.stream.ReferencePipeline.StatelessOp<Integer, U> {
        final /* synthetic */ IntPipeline this$0;
        final /* synthetic */ IntFunction val$mapper;

        /* renamed from: java.util.stream.IntPipeline.4.1 */
        class AnonymousClass1 extends ChainedInt<U> {
            final /* synthetic */ AnonymousClass4 this$1;
            final /* synthetic */ IntFunction val$mapper;

            AnonymousClass1(AnonymousClass4 this$1, Sink $anonymous0, IntFunction val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
            }

            public void accept(int t) {
                this.downstream.accept(this.val$mapper.apply(t));
            }
        }

        AnonymousClass4(IntPipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, IntFunction val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Integer> opWrapSink(int flags, Sink<U> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    /* renamed from: java.util.stream.IntPipeline.5 */
    class AnonymousClass5 extends java.util.stream.LongPipeline.StatelessOp<Integer> {
        final /* synthetic */ IntPipeline this$0;
        final /* synthetic */ IntToLongFunction val$mapper;

        /* renamed from: java.util.stream.IntPipeline.5.1 */
        class AnonymousClass1 extends ChainedInt<Long> {
            final /* synthetic */ AnonymousClass5 this$1;
            final /* synthetic */ IntToLongFunction val$mapper;

            AnonymousClass1(AnonymousClass5 this$1, Sink $anonymous0, IntToLongFunction val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
            }

            public void accept(int t) {
                this.downstream.accept(this.val$mapper.applyAsLong(t));
            }
        }

        AnonymousClass5(IntPipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, IntToLongFunction val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Integer> opWrapSink(int flags, Sink<Long> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    /* renamed from: java.util.stream.IntPipeline.6 */
    class AnonymousClass6 extends java.util.stream.DoublePipeline.StatelessOp<Integer> {
        final /* synthetic */ IntPipeline this$0;
        final /* synthetic */ IntToDoubleFunction val$mapper;

        /* renamed from: java.util.stream.IntPipeline.6.1 */
        class AnonymousClass1 extends ChainedInt<Double> {
            final /* synthetic */ AnonymousClass6 this$1;
            final /* synthetic */ IntToDoubleFunction val$mapper;

            AnonymousClass1(AnonymousClass6 this$1, Sink $anonymous0, IntToDoubleFunction val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
            }

            public void accept(int t) {
                this.downstream.accept(this.val$mapper.applyAsDouble(t));
            }
        }

        AnonymousClass6(IntPipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, IntToDoubleFunction val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Integer> opWrapSink(int flags, Sink<Double> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    /* renamed from: java.util.stream.IntPipeline.7 */
    class AnonymousClass7 extends StatelessOp<Integer> {
        final /* synthetic */ IntPipeline this$0;
        final /* synthetic */ IntFunction val$mapper;

        /* renamed from: java.util.stream.IntPipeline.7.1 */
        class AnonymousClass1 extends ChainedInt<Integer> {
            final /* synthetic */ AnonymousClass7 this$1;
            final /* synthetic */ IntFunction val$mapper;

            final /* synthetic */ class -void_accept_int_t_LambdaImpl0 implements IntConsumer {
                private /* synthetic */ AnonymousClass1 val$this;

                public /* synthetic */ -void_accept_int_t_LambdaImpl0(AnonymousClass1 anonymousClass1) {
                    this.val$this = anonymousClass1;
                }

                public void accept(int arg0) {
                    this.val$this.-java_util_stream_IntPipeline$7$1_lambda$3(arg0);
                }
            }

            AnonymousClass1(AnonymousClass7 this$1, Sink $anonymous0, IntFunction val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
            }

            public void begin(long size) {
                this.downstream.begin(-1);
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void accept(int t) {
                Throwable th;
                Throwable th2 = null;
                IntStream intStream = null;
                try {
                    intStream = (IntStream) this.val$mapper.apply(t);
                    if (intStream != null) {
                        intStream.sequential().forEach(new -void_accept_int_t_LambdaImpl0());
                    }
                    if (intStream != null) {
                        try {
                            intStream.close();
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

            /* synthetic */ void -java_util_stream_IntPipeline$7$1_lambda$3(int i) {
                this.downstream.accept(i);
            }
        }

        AnonymousClass7(IntPipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, IntFunction val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    /* renamed from: java.util.stream.IntPipeline.8 */
    class AnonymousClass8 extends StatelessOp<Integer> {
        final /* synthetic */ IntPipeline this$0;

        AnonymousClass8(IntPipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2) {
            this.this$0 = this$0;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
            return sink;
        }
    }

    /* renamed from: java.util.stream.IntPipeline.9 */
    class AnonymousClass9 extends StatelessOp<Integer> {
        final /* synthetic */ IntPipeline this$0;
        final /* synthetic */ IntPredicate val$predicate;

        /* renamed from: java.util.stream.IntPipeline.9.1 */
        class AnonymousClass1 extends ChainedInt<Integer> {
            final /* synthetic */ AnonymousClass9 this$1;
            final /* synthetic */ IntPredicate val$predicate;

            AnonymousClass1(AnonymousClass9 this$1, Sink $anonymous0, IntPredicate val$predicate) {
                this.this$1 = this$1;
                this.val$predicate = val$predicate;
                super($anonymous0);
            }

            public void begin(long size) {
                this.downstream.begin(-1);
            }

            public void accept(int t) {
                if (this.val$predicate.test(t)) {
                    this.downstream.accept(t);
                }
            }
        }

        AnonymousClass9(IntPipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, IntPredicate val$predicate) {
            this.this$0 = this$0;
            this.val$predicate = val$predicate;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
            return new AnonymousClass1(this, sink, this.val$predicate);
        }
    }

    public static class Head<E_IN> extends IntPipeline<E_IN> {
        public Head(Supplier<? extends Spliterator<Integer>> source, int sourceFlags, boolean parallel) {
            super((Supplier) source, sourceFlags, parallel);
        }

        public Head(Spliterator<Integer> source, int sourceFlags, boolean parallel) {
            super((Spliterator) source, sourceFlags, parallel);
        }

        public final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        public final Sink<E_IN> opWrapSink(int flags, Sink<Integer> sink) {
            throw new UnsupportedOperationException();
        }

        public void forEach(IntConsumer action) {
            if (isParallel()) {
                super.forEach(action);
            } else {
                IntPipeline.adapt(sourceStageSpliterator()).forEachRemaining(action);
            }
        }

        public void forEachOrdered(IntConsumer action) {
            if (isParallel()) {
                super.forEachOrdered(action);
            } else {
                IntPipeline.adapt(sourceStageSpliterator()).forEachRemaining(action);
            }
        }
    }

    public static abstract class StatefulOp<E_IN> extends IntPipeline<E_IN> {
        static final /* synthetic */ boolean -assertionsDisabled = false;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.IntPipeline.StatefulOp.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.stream.IntPipeline.StatefulOp.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.IntPipeline.StatefulOp.<clinit>():void");
        }

        public abstract <P_IN> Node<Integer> opEvaluateParallel(PipelineHelper<Integer> pipelineHelper, Spliterator<P_IN> spliterator, IntFunction<Integer[]> intFunction);

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

    IntPipeline(Supplier<? extends Spliterator<Integer>> source, int sourceFlags, boolean parallel) {
        super((Supplier) source, sourceFlags, parallel);
    }

    IntPipeline(Spliterator<Integer> source, int sourceFlags, boolean parallel) {
        super((Spliterator) source, sourceFlags, parallel);
    }

    IntPipeline(AbstractPipeline<?, E_IN, ?> upstream, int opFlags) {
        super(upstream, opFlags);
    }

    private static IntConsumer adapt(Sink<Integer> sink) {
        if (sink instanceof IntConsumer) {
            return (IntConsumer) sink;
        }
        if (Tripwire.ENABLED) {
            Tripwire.trip(AbstractPipeline.class, "using IntStream.adapt(Sink<Integer> s)");
        }
        sink.getClass();
        return new -java_util_function_IntConsumer_adapt_java_util_stream_Sink_sink_LambdaImpl0(sink);
    }

    private static OfInt adapt(Spliterator<Integer> s) {
        if (s instanceof OfInt) {
            return (OfInt) s;
        }
        if (Tripwire.ENABLED) {
            Tripwire.trip(AbstractPipeline.class, "using IntStream.adapt(Spliterator<Integer> s)");
        }
        throw new UnsupportedOperationException("IntStream.adapt(Spliterator<Integer> s)");
    }

    public final StreamShape getOutputShape() {
        return StreamShape.INT_VALUE;
    }

    public final <P_IN> Node<Integer> evaluateToNode(PipelineHelper<Integer> helper, Spliterator<P_IN> spliterator, boolean flattenTree, IntFunction<Integer[]> intFunction) {
        return Nodes.collectInt(helper, spliterator, flattenTree);
    }

    public final <P_IN> Spliterator<Integer> wrap(PipelineHelper<Integer> ph, Supplier<Spliterator<P_IN>> supplier, boolean isParallel) {
        return new IntWrappingSpliterator((PipelineHelper) ph, (Supplier) supplier, isParallel);
    }

    public /* bridge */ /* synthetic */ Spliterator m10lazySpliterator(Supplier supplier) {
        return lazySpliterator(supplier);
    }

    public final OfInt lazySpliterator(Supplier<? extends Spliterator<Integer>> supplier) {
        return new OfInt(supplier);
    }

    public final void forEachWithCancel(Spliterator<Integer> spliterator, Sink<Integer> sink) {
        OfInt spl = adapt((Spliterator) spliterator);
        IntConsumer adaptedSink = adapt((Sink) sink);
        while (!sink.cancellationRequested()) {
            if (!spl.tryAdvance(adaptedSink)) {
                return;
            }
        }
    }

    public final Builder<Integer> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Integer[]> intFunction) {
        return Nodes.intBuilder(exactSizeIfKnown);
    }

    public /* bridge */ /* synthetic */ Iterator iterator() {
        return iterator();
    }

    public final PrimitiveIterator.OfInt m9iterator() {
        return Spliterators.iterator(spliterator());
    }

    public /* bridge */ /* synthetic */ Spliterator m11spliterator() {
        return spliterator();
    }

    public final OfInt spliterator() {
        return adapt(super.spliterator());
    }

    public final LongStream asLongStream() {
        return new AnonymousClass1(this, this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT);
    }

    public final DoubleStream asDoubleStream() {
        return new AnonymousClass2(this, this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT);
    }

    public final Stream<Integer> boxed() {
        return mapToObj(new -java_util_stream_Stream_boxed__LambdaImpl0());
    }

    public final IntStream map(IntUnaryOperator mapper) {
        Objects.requireNonNull(mapper);
        return new AnonymousClass3(this, this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT, mapper);
    }

    public final <U> Stream<U> mapToObj(IntFunction<? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return new AnonymousClass4(this, this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT, mapper);
    }

    public final LongStream mapToLong(IntToLongFunction mapper) {
        Objects.requireNonNull(mapper);
        return new AnonymousClass5(this, this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT, mapper);
    }

    public final DoubleStream mapToDouble(IntToDoubleFunction mapper) {
        Objects.requireNonNull(mapper);
        return new AnonymousClass6(this, this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT, mapper);
    }

    public final IntStream flatMap(IntFunction<? extends IntStream> mapper) {
        return new AnonymousClass7(this, this, StreamShape.INT_VALUE, (StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) | StreamOpFlag.NOT_SIZED, mapper);
    }

    public /* bridge */ /* synthetic */ IntStream sequential() {
        return (IntStream) sequential();
    }

    public /* bridge */ /* synthetic */ IntStream parallel() {
        return (IntStream) parallel();
    }

    public /* bridge */ /* synthetic */ BaseStream unordered() {
        return unordered();
    }

    public IntStream m12unordered() {
        if (isOrdered()) {
            return new AnonymousClass8(this, this, StreamShape.INT_VALUE, StreamOpFlag.NOT_ORDERED);
        }
        return this;
    }

    public final IntStream filter(IntPredicate predicate) {
        Objects.requireNonNull(predicate);
        return new AnonymousClass9(this, this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SIZED, predicate);
    }

    public final IntStream peek(IntConsumer action) {
        Objects.requireNonNull(action);
        return new AnonymousClass10(this, this, StreamShape.INT_VALUE, 0, action);
    }

    public final IntStream limit(long maxSize) {
        if (maxSize >= 0) {
            return SliceOps.makeInt(this, 0, maxSize);
        }
        throw new IllegalArgumentException(Long.toString(maxSize));
    }

    public final IntStream skip(long n) {
        if (n < 0) {
            throw new IllegalArgumentException(Long.toString(n));
        } else if (n == 0) {
            return this;
        } else {
            return SliceOps.makeInt(this, n, -1);
        }
    }

    public final IntStream sorted() {
        return SortedOps.makeInt(this);
    }

    public final IntStream distinct() {
        return boxed().distinct().mapToInt(new -java_util_stream_IntStream_distinct__LambdaImpl0());
    }

    public void forEach(IntConsumer action) {
        evaluate(ForEachOps.makeInt(action, false));
    }

    public void forEachOrdered(IntConsumer action) {
        evaluate(ForEachOps.makeInt(action, true));
    }

    public final int sum() {
        return reduce(0, new -int_sum__LambdaImpl0());
    }

    public final OptionalInt min() {
        return reduce(new -java_util_OptionalInt_min__LambdaImpl0());
    }

    public final OptionalInt max() {
        return reduce(new -java_util_OptionalInt_max__LambdaImpl0());
    }

    public final long count() {
        return mapToLong(new -long_count__LambdaImpl0()).sum();
    }

    public final OptionalDouble average() {
        long[] avg = (long[]) collect(new -java_util_OptionalDouble_average__LambdaImpl0(), new -java_util_OptionalDouble_average__LambdaImpl1(), new -java_util_OptionalDouble_average__LambdaImpl2());
        if (avg[0] > 0) {
            return OptionalDouble.of(((double) avg[1]) / ((double) avg[0]));
        }
        return OptionalDouble.empty();
    }

    static /* synthetic */ void -java_util_stream_IntPipeline_lambda$10(long[] ll, int i) {
        ll[0] = ll[0] + 1;
        ll[1] = ll[1] + ((long) i);
    }

    static /* synthetic */ void -java_util_stream_IntPipeline_lambda$11(long[] ll, long[] rr) {
        ll[0] = ll[0] + rr[0];
        ll[1] = ll[1] + rr[1];
    }

    public final IntSummaryStatistics summaryStatistics() {
        return (IntSummaryStatistics) collect(new -java_util_IntSummaryStatistics_summaryStatistics__LambdaImpl0(), new -java_util_IntSummaryStatistics_summaryStatistics__LambdaImpl1(), new -java_util_IntSummaryStatistics_summaryStatistics__LambdaImpl2());
    }

    public final int reduce(int identity, IntBinaryOperator op) {
        return ((Integer) evaluate(ReduceOps.makeInt(identity, op))).intValue();
    }

    public final OptionalInt reduce(IntBinaryOperator op) {
        return (OptionalInt) evaluate(ReduceOps.makeInt(op));
    }

    public final <R> R collect(Supplier<R> supplier, ObjIntConsumer<R> accumulator, BiConsumer<R, R> combiner) {
        return evaluate(ReduceOps.makeInt(supplier, accumulator, new -java_lang_Object_collect_java_util_function_Supplier_supplier_java_util_function_ObjIntConsumer_accumulator_java_util_function_BiConsumer_combiner_LambdaImpl0(combiner)));
    }

    public final boolean anyMatch(IntPredicate predicate) {
        return ((Boolean) evaluate(MatchOps.makeInt(predicate, MatchKind.ANY))).booleanValue();
    }

    public final boolean allMatch(IntPredicate predicate) {
        return ((Boolean) evaluate(MatchOps.makeInt(predicate, MatchKind.ALL))).booleanValue();
    }

    public final boolean noneMatch(IntPredicate predicate) {
        return ((Boolean) evaluate(MatchOps.makeInt(predicate, MatchKind.NONE))).booleanValue();
    }

    public final OptionalInt findFirst() {
        return (OptionalInt) evaluate(FindOps.makeInt(true));
    }

    public final OptionalInt findAny() {
        return (OptionalInt) evaluate(FindOps.makeInt(false));
    }

    public final int[] toArray() {
        return (int[]) Nodes.flattenInt((Node.OfInt) evaluateToArrayNode(new -int__toArray__LambdaImpl0())).asPrimitiveArray();
    }
}
