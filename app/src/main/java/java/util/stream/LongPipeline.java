package java.util.stream;

import java.util.Iterator;
import java.util.LongSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterator.OfLong;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.IntFunction;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;
import java.util.stream.Node.Builder;
import java.util.stream.Sink.ChainedLong;

public abstract class LongPipeline<E_IN> extends AbstractPipeline<E_IN, Long, LongStream> implements LongStream {

    public static abstract class StatelessOp<E_IN> extends LongPipeline<E_IN> {
        static final /* synthetic */ boolean -assertionsDisabled = false;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.LongPipeline.StatelessOp.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.stream.LongPipeline.StatelessOp.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.LongPipeline.StatelessOp.<clinit>():void");
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

    final /* synthetic */ class -java_lang_Object_collect_java_util_function_Supplier_supplier_java_util_function_ObjLongConsumer_accumulator_java_util_function_BiConsumer_combiner_LambdaImpl0 implements BinaryOperator {
        private /* synthetic */ BiConsumer val$combiner;

        public /* synthetic */ -java_lang_Object_collect_java_util_function_Supplier_supplier_java_util_function_ObjLongConsumer_accumulator_java_util_function_BiConsumer_combiner_LambdaImpl0(BiConsumer biConsumer) {
            this.val$combiner = biConsumer;
        }

        public Object apply(Object arg0, Object arg1) {
            return this.val$combiner.accept(arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_LongSummaryStatistics_summaryStatistics__LambdaImpl0 implements Supplier {
        public /* synthetic */ -java_util_LongSummaryStatistics_summaryStatistics__LambdaImpl0() {
        }

        public Object get() {
            return new LongSummaryStatistics();
        }
    }

    final /* synthetic */ class -java_util_LongSummaryStatistics_summaryStatistics__LambdaImpl1 implements ObjLongConsumer {
        public /* synthetic */ -java_util_LongSummaryStatistics_summaryStatistics__LambdaImpl1() {
        }

        public void accept(Object arg0, long arg1) {
            ((LongSummaryStatistics) arg0).accept(arg1);
        }
    }

    final /* synthetic */ class -java_util_LongSummaryStatistics_summaryStatistics__LambdaImpl2 implements BiConsumer {
        public /* synthetic */ -java_util_LongSummaryStatistics_summaryStatistics__LambdaImpl2() {
        }

        public void accept(Object arg0, Object arg1) {
            ((LongSummaryStatistics) arg0).combine((LongSummaryStatistics) arg1);
        }
    }

    final /* synthetic */ class -java_util_OptionalDouble_average__LambdaImpl0 implements Supplier {
        public /* synthetic */ -java_util_OptionalDouble_average__LambdaImpl0() {
        }

        public Object get() {
            return new long[2];
        }
    }

    final /* synthetic */ class -java_util_OptionalDouble_average__LambdaImpl1 implements ObjLongConsumer {
        public /* synthetic */ -java_util_OptionalDouble_average__LambdaImpl1() {
        }

        public void accept(Object arg0, long arg1) {
            LongPipeline.-java_util_stream_LongPipeline_lambda$9((long[]) arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_OptionalDouble_average__LambdaImpl2 implements BiConsumer {
        public /* synthetic */ -java_util_OptionalDouble_average__LambdaImpl2() {
        }

        public void accept(Object arg0, Object arg1) {
            LongPipeline.-java_util_stream_LongPipeline_lambda$10((long[]) arg0, (long[]) arg1);
        }
    }

    final /* synthetic */ class -java_util_OptionalLong_max__LambdaImpl0 implements LongBinaryOperator {
        public /* synthetic */ -java_util_OptionalLong_max__LambdaImpl0() {
        }

        public long applyAsLong(long arg0, long arg1) {
            return Math.max(arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_OptionalLong_min__LambdaImpl0 implements LongBinaryOperator {
        public /* synthetic */ -java_util_OptionalLong_min__LambdaImpl0() {
        }

        public long applyAsLong(long arg0, long arg1) {
            return Math.min(arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_function_LongConsumer_adapt_java_util_stream_Sink_sink_LambdaImpl0 implements LongConsumer {
        private /* synthetic */ Sink val$-lambdaCtx;

        public /* synthetic */ -java_util_function_LongConsumer_adapt_java_util_stream_Sink_sink_LambdaImpl0(Sink sink) {
            this.val$-lambdaCtx = sink;
        }

        public void accept(long arg0) {
            this.val$-lambdaCtx.accept(arg0);
        }
    }

    final /* synthetic */ class -java_util_stream_LongStream_distinct__LambdaImpl0 implements ToLongFunction {
        public /* synthetic */ -java_util_stream_LongStream_distinct__LambdaImpl0() {
        }

        public long applyAsLong(Object arg0) {
            return ((Long) arg0).longValue();
        }
    }

    final /* synthetic */ class -java_util_stream_Stream_boxed__LambdaImpl0 implements LongFunction {
        public /* synthetic */ -java_util_stream_Stream_boxed__LambdaImpl0() {
        }

        public Object apply(long arg0) {
            return Long.valueOf(arg0);
        }
    }

    final /* synthetic */ class -long__toArray__LambdaImpl0 implements IntFunction {
        public /* synthetic */ -long__toArray__LambdaImpl0() {
        }

        public Object apply(int arg0) {
            return new Long[arg0];
        }
    }

    final /* synthetic */ class -long_count__LambdaImpl0 implements LongUnaryOperator {
        public /* synthetic */ -long_count__LambdaImpl0() {
        }

        public long applyAsLong(long arg0) {
            return 1;
        }
    }

    final /* synthetic */ class -long_sum__LambdaImpl0 implements LongBinaryOperator {
        public /* synthetic */ -long_sum__LambdaImpl0() {
        }

        public long applyAsLong(long arg0, long arg1) {
            return Long.sum(arg0, arg1);
        }
    }

    /* renamed from: java.util.stream.LongPipeline.1 */
    class AnonymousClass1 extends java.util.stream.DoublePipeline.StatelessOp<Long> {
        final /* synthetic */ LongPipeline this$0;

        /* renamed from: java.util.stream.LongPipeline.1.1 */
        class AnonymousClass1 extends ChainedLong<Double> {
            final /* synthetic */ AnonymousClass1 this$1;

            AnonymousClass1(AnonymousClass1 this$1, Sink $anonymous0) {
                this.this$1 = this$1;
                super($anonymous0);
            }

            public void accept(long t) {
                this.downstream.accept((double) t);
            }
        }

        AnonymousClass1(LongPipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2) {
            this.this$0 = this$0;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Long> opWrapSink(int flags, Sink<Double> sink) {
            return new AnonymousClass1(this, sink);
        }
    }

    /* renamed from: java.util.stream.LongPipeline.2 */
    class AnonymousClass2 extends StatelessOp<Long> {
        final /* synthetic */ LongPipeline this$0;
        final /* synthetic */ LongUnaryOperator val$mapper;

        /* renamed from: java.util.stream.LongPipeline.2.1 */
        class AnonymousClass1 extends ChainedLong<Long> {
            final /* synthetic */ AnonymousClass2 this$1;
            final /* synthetic */ LongUnaryOperator val$mapper;

            AnonymousClass1(AnonymousClass2 this$1, Sink $anonymous0, LongUnaryOperator val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
            }

            public void accept(long t) {
                this.downstream.accept(this.val$mapper.applyAsLong(t));
            }
        }

        AnonymousClass2(LongPipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, LongUnaryOperator val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    /* renamed from: java.util.stream.LongPipeline.3 */
    class AnonymousClass3 extends java.util.stream.ReferencePipeline.StatelessOp<Long, U> {
        final /* synthetic */ LongPipeline this$0;
        final /* synthetic */ LongFunction val$mapper;

        /* renamed from: java.util.stream.LongPipeline.3.1 */
        class AnonymousClass1 extends ChainedLong<U> {
            final /* synthetic */ AnonymousClass3 this$1;
            final /* synthetic */ LongFunction val$mapper;

            AnonymousClass1(AnonymousClass3 this$1, Sink $anonymous0, LongFunction val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
            }

            public void accept(long t) {
                this.downstream.accept(this.val$mapper.apply(t));
            }
        }

        AnonymousClass3(LongPipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, LongFunction val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Long> opWrapSink(int flags, Sink<U> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    /* renamed from: java.util.stream.LongPipeline.4 */
    class AnonymousClass4 extends java.util.stream.IntPipeline.StatelessOp<Long> {
        final /* synthetic */ LongPipeline this$0;
        final /* synthetic */ LongToIntFunction val$mapper;

        /* renamed from: java.util.stream.LongPipeline.4.1 */
        class AnonymousClass1 extends ChainedLong<Integer> {
            final /* synthetic */ AnonymousClass4 this$1;
            final /* synthetic */ LongToIntFunction val$mapper;

            AnonymousClass1(AnonymousClass4 this$1, Sink $anonymous0, LongToIntFunction val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
            }

            public void accept(long t) {
                this.downstream.accept(this.val$mapper.applyAsInt(t));
            }
        }

        AnonymousClass4(LongPipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, LongToIntFunction val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Long> opWrapSink(int flags, Sink<Integer> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    /* renamed from: java.util.stream.LongPipeline.5 */
    class AnonymousClass5 extends java.util.stream.DoublePipeline.StatelessOp<Long> {
        final /* synthetic */ LongPipeline this$0;
        final /* synthetic */ LongToDoubleFunction val$mapper;

        /* renamed from: java.util.stream.LongPipeline.5.1 */
        class AnonymousClass1 extends ChainedLong<Double> {
            final /* synthetic */ AnonymousClass5 this$1;
            final /* synthetic */ LongToDoubleFunction val$mapper;

            AnonymousClass1(AnonymousClass5 this$1, Sink $anonymous0, LongToDoubleFunction val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
            }

            public void accept(long t) {
                this.downstream.accept(this.val$mapper.applyAsDouble(t));
            }
        }

        AnonymousClass5(LongPipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, LongToDoubleFunction val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Long> opWrapSink(int flags, Sink<Double> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    /* renamed from: java.util.stream.LongPipeline.6 */
    class AnonymousClass6 extends StatelessOp<Long> {
        final /* synthetic */ LongPipeline this$0;
        final /* synthetic */ LongFunction val$mapper;

        /* renamed from: java.util.stream.LongPipeline.6.1 */
        class AnonymousClass1 extends ChainedLong<Long> {
            final /* synthetic */ AnonymousClass6 this$1;
            final /* synthetic */ LongFunction val$mapper;

            final /* synthetic */ class -void_accept_long_t_LambdaImpl0 implements LongConsumer {
                private /* synthetic */ AnonymousClass1 val$this;

                public /* synthetic */ -void_accept_long_t_LambdaImpl0(AnonymousClass1 anonymousClass1) {
                    this.val$this = anonymousClass1;
                }

                public void accept(long arg0) {
                    this.val$this.-java_util_stream_LongPipeline$6$1_lambda$3(arg0);
                }
            }

            AnonymousClass1(AnonymousClass6 this$1, Sink $anonymous0, LongFunction val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
            }

            public void begin(long size) {
                this.downstream.begin(-1);
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void accept(long t) {
                Throwable th;
                Throwable th2 = null;
                LongStream longStream = null;
                try {
                    longStream = (LongStream) this.val$mapper.apply(t);
                    if (longStream != null) {
                        longStream.sequential().forEach(new -void_accept_long_t_LambdaImpl0());
                    }
                    if (longStream != null) {
                        try {
                            longStream.close();
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

            /* synthetic */ void -java_util_stream_LongPipeline$6$1_lambda$3(long i) {
                this.downstream.accept(i);
            }
        }

        AnonymousClass6(LongPipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, LongFunction val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    /* renamed from: java.util.stream.LongPipeline.7 */
    class AnonymousClass7 extends StatelessOp<Long> {
        final /* synthetic */ LongPipeline this$0;

        AnonymousClass7(LongPipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2) {
            this.this$0 = this$0;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
            return sink;
        }
    }

    /* renamed from: java.util.stream.LongPipeline.8 */
    class AnonymousClass8 extends StatelessOp<Long> {
        final /* synthetic */ LongPipeline this$0;
        final /* synthetic */ LongPredicate val$predicate;

        /* renamed from: java.util.stream.LongPipeline.8.1 */
        class AnonymousClass1 extends ChainedLong<Long> {
            final /* synthetic */ AnonymousClass8 this$1;
            final /* synthetic */ LongPredicate val$predicate;

            AnonymousClass1(AnonymousClass8 this$1, Sink $anonymous0, LongPredicate val$predicate) {
                this.this$1 = this$1;
                this.val$predicate = val$predicate;
                super($anonymous0);
            }

            public void begin(long size) {
                this.downstream.begin(-1);
            }

            public void accept(long t) {
                if (this.val$predicate.test(t)) {
                    this.downstream.accept(t);
                }
            }
        }

        AnonymousClass8(LongPipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, LongPredicate val$predicate) {
            this.this$0 = this$0;
            this.val$predicate = val$predicate;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
            return new AnonymousClass1(this, sink, this.val$predicate);
        }
    }

    /* renamed from: java.util.stream.LongPipeline.9 */
    class AnonymousClass9 extends StatelessOp<Long> {
        final /* synthetic */ LongPipeline this$0;
        final /* synthetic */ LongConsumer val$action;

        /* renamed from: java.util.stream.LongPipeline.9.1 */
        class AnonymousClass1 extends ChainedLong<Long> {
            final /* synthetic */ AnonymousClass9 this$1;
            final /* synthetic */ LongConsumer val$action;

            AnonymousClass1(AnonymousClass9 this$1, Sink $anonymous0, LongConsumer val$action) {
                this.this$1 = this$1;
                this.val$action = val$action;
                super($anonymous0);
            }

            public void accept(long t) {
                this.val$action.accept(t);
                this.downstream.accept(t);
            }
        }

        AnonymousClass9(LongPipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, LongConsumer val$action) {
            this.this$0 = this$0;
            this.val$action = val$action;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
            return new AnonymousClass1(this, sink, this.val$action);
        }
    }

    public static class Head<E_IN> extends LongPipeline<E_IN> {
        public Head(Supplier<? extends Spliterator<Long>> source, int sourceFlags, boolean parallel) {
            super((Supplier) source, sourceFlags, parallel);
        }

        public Head(Spliterator<Long> source, int sourceFlags, boolean parallel) {
            super((Spliterator) source, sourceFlags, parallel);
        }

        public final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        public final Sink<E_IN> opWrapSink(int flags, Sink<Long> sink) {
            throw new UnsupportedOperationException();
        }

        public void forEach(LongConsumer action) {
            if (isParallel()) {
                super.forEach(action);
            } else {
                LongPipeline.adapt(sourceStageSpliterator()).forEachRemaining(action);
            }
        }

        public void forEachOrdered(LongConsumer action) {
            if (isParallel()) {
                super.forEachOrdered(action);
            } else {
                LongPipeline.adapt(sourceStageSpliterator()).forEachRemaining(action);
            }
        }
    }

    public static abstract class StatefulOp<E_IN> extends LongPipeline<E_IN> {
        static final /* synthetic */ boolean -assertionsDisabled = false;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.LongPipeline.StatefulOp.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.stream.LongPipeline.StatefulOp.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.LongPipeline.StatefulOp.<clinit>():void");
        }

        public abstract <P_IN> Node<Long> opEvaluateParallel(PipelineHelper<Long> pipelineHelper, Spliterator<P_IN> spliterator, IntFunction<Long[]> intFunction);

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

    LongPipeline(Supplier<? extends Spliterator<Long>> source, int sourceFlags, boolean parallel) {
        super((Supplier) source, sourceFlags, parallel);
    }

    LongPipeline(Spliterator<Long> source, int sourceFlags, boolean parallel) {
        super((Spliterator) source, sourceFlags, parallel);
    }

    LongPipeline(AbstractPipeline<?, E_IN, ?> upstream, int opFlags) {
        super(upstream, opFlags);
    }

    private static LongConsumer adapt(Sink<Long> sink) {
        if (sink instanceof LongConsumer) {
            return (LongConsumer) sink;
        }
        if (Tripwire.ENABLED) {
            Tripwire.trip(AbstractPipeline.class, "using LongStream.adapt(Sink<Long> s)");
        }
        sink.getClass();
        return new -java_util_function_LongConsumer_adapt_java_util_stream_Sink_sink_LambdaImpl0(sink);
    }

    private static OfLong adapt(Spliterator<Long> s) {
        if (s instanceof OfLong) {
            return (OfLong) s;
        }
        if (Tripwire.ENABLED) {
            Tripwire.trip(AbstractPipeline.class, "using LongStream.adapt(Spliterator<Long> s)");
        }
        throw new UnsupportedOperationException("LongStream.adapt(Spliterator<Long> s)");
    }

    public final StreamShape getOutputShape() {
        return StreamShape.LONG_VALUE;
    }

    public final <P_IN> Node<Long> evaluateToNode(PipelineHelper<Long> helper, Spliterator<P_IN> spliterator, boolean flattenTree, IntFunction<Long[]> intFunction) {
        return Nodes.collectLong(helper, spliterator, flattenTree);
    }

    public final <P_IN> Spliterator<Long> wrap(PipelineHelper<Long> ph, Supplier<Spliterator<P_IN>> supplier, boolean isParallel) {
        return new LongWrappingSpliterator((PipelineHelper) ph, (Supplier) supplier, isParallel);
    }

    public /* bridge */ /* synthetic */ Spliterator m19lazySpliterator(Supplier supplier) {
        return lazySpliterator(supplier);
    }

    public final OfLong lazySpliterator(Supplier<? extends Spliterator<Long>> supplier) {
        return new OfLong(supplier);
    }

    public final void forEachWithCancel(Spliterator<Long> spliterator, Sink<Long> sink) {
        OfLong spl = adapt((Spliterator) spliterator);
        LongConsumer adaptedSink = adapt((Sink) sink);
        while (!sink.cancellationRequested()) {
            if (!spl.tryAdvance(adaptedSink)) {
                return;
            }
        }
    }

    public final Builder<Long> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Long[]> intFunction) {
        return Nodes.longBuilder(exactSizeIfKnown);
    }

    public /* bridge */ /* synthetic */ Iterator iterator() {
        return iterator();
    }

    public final PrimitiveIterator.OfLong m18iterator() {
        return Spliterators.iterator(spliterator());
    }

    public /* bridge */ /* synthetic */ Spliterator m20spliterator() {
        return spliterator();
    }

    public final OfLong spliterator() {
        return adapt(super.spliterator());
    }

    public final DoubleStream asDoubleStream() {
        return new AnonymousClass1(this, this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT);
    }

    public final Stream<Long> boxed() {
        return mapToObj(new -java_util_stream_Stream_boxed__LambdaImpl0());
    }

    public final LongStream map(LongUnaryOperator mapper) {
        Objects.requireNonNull(mapper);
        return new AnonymousClass2(this, this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT, mapper);
    }

    public final <U> Stream<U> mapToObj(LongFunction<? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return new AnonymousClass3(this, this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT, mapper);
    }

    public final IntStream mapToInt(LongToIntFunction mapper) {
        Objects.requireNonNull(mapper);
        return new AnonymousClass4(this, this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT, mapper);
    }

    public final DoubleStream mapToDouble(LongToDoubleFunction mapper) {
        Objects.requireNonNull(mapper);
        return new AnonymousClass5(this, this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT, mapper);
    }

    public final LongStream flatMap(LongFunction<? extends LongStream> mapper) {
        return new AnonymousClass6(this, this, StreamShape.LONG_VALUE, (StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) | StreamOpFlag.NOT_SIZED, mapper);
    }

    public /* bridge */ /* synthetic */ BaseStream unordered() {
        return unordered();
    }

    public LongStream m21unordered() {
        if (isOrdered()) {
            return new AnonymousClass7(this, this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_ORDERED);
        }
        return this;
    }

    public /* bridge */ /* synthetic */ LongStream sequential() {
        return (LongStream) sequential();
    }

    public /* bridge */ /* synthetic */ LongStream parallel() {
        return (LongStream) parallel();
    }

    public final LongStream filter(LongPredicate predicate) {
        Objects.requireNonNull(predicate);
        return new AnonymousClass8(this, this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SIZED, predicate);
    }

    public final LongStream peek(LongConsumer action) {
        Objects.requireNonNull(action);
        return new AnonymousClass9(this, this, StreamShape.LONG_VALUE, 0, action);
    }

    public final LongStream limit(long maxSize) {
        if (maxSize >= 0) {
            return SliceOps.makeLong(this, 0, maxSize);
        }
        throw new IllegalArgumentException(Long.toString(maxSize));
    }

    public final LongStream skip(long n) {
        if (n < 0) {
            throw new IllegalArgumentException(Long.toString(n));
        } else if (n == 0) {
            return this;
        } else {
            return SliceOps.makeLong(this, n, -1);
        }
    }

    public final LongStream sorted() {
        return SortedOps.makeLong(this);
    }

    public final LongStream distinct() {
        return boxed().distinct().mapToLong(new -java_util_stream_LongStream_distinct__LambdaImpl0());
    }

    public void forEach(LongConsumer action) {
        evaluate(ForEachOps.makeLong(action, false));
    }

    public void forEachOrdered(LongConsumer action) {
        evaluate(ForEachOps.makeLong(action, true));
    }

    public final long sum() {
        return reduce(0, new -long_sum__LambdaImpl0());
    }

    public final OptionalLong min() {
        return reduce(new -java_util_OptionalLong_min__LambdaImpl0());
    }

    public final OptionalLong max() {
        return reduce(new -java_util_OptionalLong_max__LambdaImpl0());
    }

    public final OptionalDouble average() {
        long[] avg = (long[]) collect(new -java_util_OptionalDouble_average__LambdaImpl0(), new -java_util_OptionalDouble_average__LambdaImpl1(), new -java_util_OptionalDouble_average__LambdaImpl2());
        if (avg[0] > 0) {
            return OptionalDouble.of(((double) avg[1]) / ((double) avg[0]));
        }
        return OptionalDouble.empty();
    }

    static /* synthetic */ void -java_util_stream_LongPipeline_lambda$9(long[] ll, long i) {
        ll[0] = ll[0] + 1;
        ll[1] = ll[1] + i;
    }

    static /* synthetic */ void -java_util_stream_LongPipeline_lambda$10(long[] ll, long[] rr) {
        ll[0] = ll[0] + rr[0];
        ll[1] = ll[1] + rr[1];
    }

    public final long count() {
        return map(new -long_count__LambdaImpl0()).sum();
    }

    public final LongSummaryStatistics summaryStatistics() {
        return (LongSummaryStatistics) collect(new -java_util_LongSummaryStatistics_summaryStatistics__LambdaImpl0(), new -java_util_LongSummaryStatistics_summaryStatistics__LambdaImpl1(), new -java_util_LongSummaryStatistics_summaryStatistics__LambdaImpl2());
    }

    public final long reduce(long identity, LongBinaryOperator op) {
        return ((Long) evaluate(ReduceOps.makeLong(identity, op))).longValue();
    }

    public final OptionalLong reduce(LongBinaryOperator op) {
        return (OptionalLong) evaluate(ReduceOps.makeLong(op));
    }

    public final <R> R collect(Supplier<R> supplier, ObjLongConsumer<R> accumulator, BiConsumer<R, R> combiner) {
        return evaluate(ReduceOps.makeLong(supplier, accumulator, new -java_lang_Object_collect_java_util_function_Supplier_supplier_java_util_function_ObjLongConsumer_accumulator_java_util_function_BiConsumer_combiner_LambdaImpl0(combiner)));
    }

    public final boolean anyMatch(LongPredicate predicate) {
        return ((Boolean) evaluate(MatchOps.makeLong(predicate, MatchKind.ANY))).booleanValue();
    }

    public final boolean allMatch(LongPredicate predicate) {
        return ((Boolean) evaluate(MatchOps.makeLong(predicate, MatchKind.ALL))).booleanValue();
    }

    public final boolean noneMatch(LongPredicate predicate) {
        return ((Boolean) evaluate(MatchOps.makeLong(predicate, MatchKind.NONE))).booleanValue();
    }

    public final OptionalLong findFirst() {
        return (OptionalLong) evaluate(FindOps.makeLong(true));
    }

    public final OptionalLong findAny() {
        return (OptionalLong) evaluate(FindOps.makeLong(false));
    }

    public final long[] toArray() {
        return (long[]) Nodes.flattenLong((Node.OfLong) evaluateToArrayNode(new -long__toArray__LambdaImpl0())).asPrimitiveArray();
    }
}
