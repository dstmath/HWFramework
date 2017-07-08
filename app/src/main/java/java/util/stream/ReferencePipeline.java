package java.util.stream;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector.Characteristics;
import java.util.stream.Node.Builder;
import java.util.stream.Sink.ChainedReference;

public abstract class ReferencePipeline<P_IN, P_OUT> extends AbstractPipeline<P_IN, P_OUT, Stream<P_OUT>> implements Stream<P_OUT> {

    public static abstract class StatefulOp<E_IN, E_OUT> extends ReferencePipeline<E_IN, E_OUT> {
        static final /* synthetic */ boolean -assertionsDisabled = false;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.ReferencePipeline.StatefulOp.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.stream.ReferencePipeline.StatefulOp.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.ReferencePipeline.StatefulOp.<clinit>():void");
        }

        public abstract <P_IN> Node<E_OUT> opEvaluateParallel(PipelineHelper<E_OUT> pipelineHelper, Spliterator<P_IN> spliterator, IntFunction<E_OUT[]> intFunction);

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

    public static abstract class StatelessOp<E_IN, E_OUT> extends ReferencePipeline<E_IN, E_OUT> {
        static final /* synthetic */ boolean -assertionsDisabled = false;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.ReferencePipeline.StatelessOp.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.stream.ReferencePipeline.StatelessOp.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.ReferencePipeline.StatelessOp.<clinit>():void");
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

    final /* synthetic */ class -java_lang_Object__toArray__LambdaImpl0 implements IntFunction {
        public /* synthetic */ -java_lang_Object__toArray__LambdaImpl0() {
        }

        public Object apply(int arg0) {
            return new Object[arg0];
        }
    }

    final /* synthetic */ class -java_lang_Object_collect_java_util_stream_Collector_collector_LambdaImpl0 implements Consumer {
        private /* synthetic */ BiConsumer val$accumulator;
        private /* synthetic */ Object val$container;

        public /* synthetic */ -java_lang_Object_collect_java_util_stream_Collector_collector_LambdaImpl0(BiConsumer biConsumer, Object obj) {
            this.val$accumulator = biConsumer;
            this.val$container = obj;
        }

        public void accept(Object arg0) {
            this.val$accumulator.accept(this.val$container, arg0);
        }
    }

    final /* synthetic */ class -long_count__LambdaImpl0 implements ToLongFunction {
        public /* synthetic */ -long_count__LambdaImpl0() {
        }

        public long applyAsLong(Object arg0) {
            return 1;
        }
    }

    /* renamed from: java.util.stream.ReferencePipeline.10 */
    class AnonymousClass10 extends java.util.stream.LongPipeline.StatelessOp<P_OUT> {
        final /* synthetic */ ReferencePipeline this$0;
        final /* synthetic */ Function val$mapper;

        /* renamed from: java.util.stream.ReferencePipeline.10.1 */
        class AnonymousClass1 extends ChainedReference<P_OUT, Long> {
            LongConsumer downstreamAsLong;
            final /* synthetic */ AnonymousClass10 this$1;
            final /* synthetic */ Function val$mapper;

            AnonymousClass1(AnonymousClass10 this$1, Sink $anonymous0, Function val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
                Sink sink = this.downstream;
                sink.getClass();
                this.downstreamAsLong = new ReferencePipeline$10$1$-void__init__java_util_stream_ReferencePipeline$10_this$1_java_util_stream_Sink_$anonymous0_java_util_function_Function_val$mapper_LambdaImpl0(sink);
            }

            public void begin(long size) {
                this.downstream.begin(-1);
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void accept(P_OUT u) {
                Throwable th;
                Throwable th2 = null;
                LongStream longStream = null;
                try {
                    longStream = (LongStream) this.val$mapper.apply(u);
                    if (longStream != null) {
                        longStream.sequential().forEach(this.downstreamAsLong);
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
        }

        AnonymousClass10(ReferencePipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, Function val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<P_OUT> opWrapSink(int flags, Sink<Long> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    /* renamed from: java.util.stream.ReferencePipeline.11 */
    class AnonymousClass11 extends StatelessOp<P_OUT, P_OUT> {
        final /* synthetic */ ReferencePipeline this$0;
        final /* synthetic */ Consumer val$action;

        /* renamed from: java.util.stream.ReferencePipeline.11.1 */
        class AnonymousClass1 extends ChainedReference<P_OUT, P_OUT> {
            final /* synthetic */ AnonymousClass11 this$1;
            final /* synthetic */ Consumer val$action;

            AnonymousClass1(AnonymousClass11 this$1, Sink $anonymous0, Consumer val$action) {
                this.this$1 = this$1;
                this.val$action = val$action;
                super($anonymous0);
            }

            public void accept(P_OUT u) {
                this.val$action.accept(u);
                this.downstream.accept(u);
            }
        }

        AnonymousClass11(ReferencePipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, Consumer val$action) {
            this.this$0 = this$0;
            this.val$action = val$action;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<P_OUT> opWrapSink(int flags, Sink<P_OUT> sink) {
            return new AnonymousClass1(this, sink, this.val$action);
        }
    }

    /* renamed from: java.util.stream.ReferencePipeline.1 */
    class AnonymousClass1 extends StatelessOp<P_OUT, P_OUT> {
        final /* synthetic */ ReferencePipeline this$0;

        AnonymousClass1(ReferencePipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2) {
            this.this$0 = this$0;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<P_OUT> opWrapSink(int flags, Sink<P_OUT> sink) {
            return sink;
        }
    }

    /* renamed from: java.util.stream.ReferencePipeline.2 */
    class AnonymousClass2 extends StatelessOp<P_OUT, P_OUT> {
        final /* synthetic */ ReferencePipeline this$0;
        final /* synthetic */ Predicate val$predicate;

        /* renamed from: java.util.stream.ReferencePipeline.2.1 */
        class AnonymousClass1 extends ChainedReference<P_OUT, P_OUT> {
            final /* synthetic */ AnonymousClass2 this$1;
            final /* synthetic */ Predicate val$predicate;

            AnonymousClass1(AnonymousClass2 this$1, Sink $anonymous0, Predicate val$predicate) {
                this.this$1 = this$1;
                this.val$predicate = val$predicate;
                super($anonymous0);
            }

            public void begin(long size) {
                this.downstream.begin(-1);
            }

            public void accept(P_OUT u) {
                if (this.val$predicate.test(u)) {
                    this.downstream.accept(u);
                }
            }
        }

        AnonymousClass2(ReferencePipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, Predicate val$predicate) {
            this.this$0 = this$0;
            this.val$predicate = val$predicate;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<P_OUT> opWrapSink(int flags, Sink<P_OUT> sink) {
            return new AnonymousClass1(this, sink, this.val$predicate);
        }
    }

    /* renamed from: java.util.stream.ReferencePipeline.3 */
    class AnonymousClass3 extends StatelessOp<P_OUT, R> {
        final /* synthetic */ ReferencePipeline this$0;
        final /* synthetic */ Function val$mapper;

        /* renamed from: java.util.stream.ReferencePipeline.3.1 */
        class AnonymousClass1 extends ChainedReference<P_OUT, R> {
            final /* synthetic */ AnonymousClass3 this$1;
            final /* synthetic */ Function val$mapper;

            AnonymousClass1(AnonymousClass3 this$1, Sink $anonymous0, Function val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
            }

            public void accept(P_OUT u) {
                this.downstream.accept(this.val$mapper.apply(u));
            }
        }

        AnonymousClass3(ReferencePipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, Function val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<P_OUT> opWrapSink(int flags, Sink<R> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    /* renamed from: java.util.stream.ReferencePipeline.4 */
    class AnonymousClass4 extends java.util.stream.IntPipeline.StatelessOp<P_OUT> {
        final /* synthetic */ ReferencePipeline this$0;
        final /* synthetic */ ToIntFunction val$mapper;

        /* renamed from: java.util.stream.ReferencePipeline.4.1 */
        class AnonymousClass1 extends ChainedReference<P_OUT, Integer> {
            final /* synthetic */ AnonymousClass4 this$1;
            final /* synthetic */ ToIntFunction val$mapper;

            AnonymousClass1(AnonymousClass4 this$1, Sink $anonymous0, ToIntFunction val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
            }

            public void accept(P_OUT u) {
                this.downstream.accept(this.val$mapper.applyAsInt(u));
            }
        }

        AnonymousClass4(ReferencePipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, ToIntFunction val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<P_OUT> opWrapSink(int flags, Sink<Integer> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    /* renamed from: java.util.stream.ReferencePipeline.5 */
    class AnonymousClass5 extends java.util.stream.LongPipeline.StatelessOp<P_OUT> {
        final /* synthetic */ ReferencePipeline this$0;
        final /* synthetic */ ToLongFunction val$mapper;

        /* renamed from: java.util.stream.ReferencePipeline.5.1 */
        class AnonymousClass1 extends ChainedReference<P_OUT, Long> {
            final /* synthetic */ AnonymousClass5 this$1;
            final /* synthetic */ ToLongFunction val$mapper;

            AnonymousClass1(AnonymousClass5 this$1, Sink $anonymous0, ToLongFunction val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
            }

            public void accept(P_OUT u) {
                this.downstream.accept(this.val$mapper.applyAsLong(u));
            }
        }

        AnonymousClass5(ReferencePipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, ToLongFunction val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<P_OUT> opWrapSink(int flags, Sink<Long> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    /* renamed from: java.util.stream.ReferencePipeline.6 */
    class AnonymousClass6 extends java.util.stream.DoublePipeline.StatelessOp<P_OUT> {
        final /* synthetic */ ReferencePipeline this$0;
        final /* synthetic */ ToDoubleFunction val$mapper;

        /* renamed from: java.util.stream.ReferencePipeline.6.1 */
        class AnonymousClass1 extends ChainedReference<P_OUT, Double> {
            final /* synthetic */ AnonymousClass6 this$1;
            final /* synthetic */ ToDoubleFunction val$mapper;

            AnonymousClass1(AnonymousClass6 this$1, Sink $anonymous0, ToDoubleFunction val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
            }

            public void accept(P_OUT u) {
                this.downstream.accept(this.val$mapper.applyAsDouble(u));
            }
        }

        AnonymousClass6(ReferencePipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, ToDoubleFunction val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<P_OUT> opWrapSink(int flags, Sink<Double> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    /* renamed from: java.util.stream.ReferencePipeline.7 */
    class AnonymousClass7 extends StatelessOp<P_OUT, R> {
        final /* synthetic */ ReferencePipeline this$0;
        final /* synthetic */ Function val$mapper;

        /* renamed from: java.util.stream.ReferencePipeline.7.1 */
        class AnonymousClass1 extends ChainedReference<P_OUT, R> {
            final /* synthetic */ AnonymousClass7 this$1;
            final /* synthetic */ Function val$mapper;

            AnonymousClass1(AnonymousClass7 this$1, Sink $anonymous0, Function val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
            }

            public void begin(long size) {
                this.downstream.begin(-1);
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void accept(P_OUT u) {
                Throwable th;
                Throwable th2 = null;
                Stream stream = null;
                try {
                    Stream<? extends R> result = (Stream) this.val$mapper.apply(u);
                    if (result != null) {
                        ((Stream) result.sequential()).forEach(this.downstream);
                    }
                    if (result != null) {
                        try {
                            result.close();
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
        }

        AnonymousClass7(ReferencePipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, Function val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<P_OUT> opWrapSink(int flags, Sink<R> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    /* renamed from: java.util.stream.ReferencePipeline.8 */
    class AnonymousClass8 extends java.util.stream.IntPipeline.StatelessOp<P_OUT> {
        final /* synthetic */ ReferencePipeline this$0;
        final /* synthetic */ Function val$mapper;

        /* renamed from: java.util.stream.ReferencePipeline.8.1 */
        class AnonymousClass1 extends ChainedReference<P_OUT, Integer> {
            IntConsumer downstreamAsInt;
            final /* synthetic */ AnonymousClass8 this$1;
            final /* synthetic */ Function val$mapper;

            AnonymousClass1(AnonymousClass8 this$1, Sink $anonymous0, Function val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
                Sink sink = this.downstream;
                sink.getClass();
                this.downstreamAsInt = new ReferencePipeline$8$1$-void__init__java_util_stream_ReferencePipeline$8_this$1_java_util_stream_Sink_$anonymous0_java_util_function_Function_val$mapper_LambdaImpl0(sink);
            }

            public void begin(long size) {
                this.downstream.begin(-1);
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void accept(P_OUT u) {
                Throwable th;
                Throwable th2 = null;
                IntStream intStream = null;
                try {
                    intStream = (IntStream) this.val$mapper.apply(u);
                    if (intStream != null) {
                        intStream.sequential().forEach(this.downstreamAsInt);
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
        }

        AnonymousClass8(ReferencePipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, Function val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<P_OUT> opWrapSink(int flags, Sink<Integer> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    /* renamed from: java.util.stream.ReferencePipeline.9 */
    class AnonymousClass9 extends java.util.stream.DoublePipeline.StatelessOp<P_OUT> {
        final /* synthetic */ ReferencePipeline this$0;
        final /* synthetic */ Function val$mapper;

        /* renamed from: java.util.stream.ReferencePipeline.9.1 */
        class AnonymousClass1 extends ChainedReference<P_OUT, Double> {
            DoubleConsumer downstreamAsDouble;
            final /* synthetic */ AnonymousClass9 this$1;
            final /* synthetic */ Function val$mapper;

            AnonymousClass1(AnonymousClass9 this$1, Sink $anonymous0, Function val$mapper) {
                this.this$1 = this$1;
                this.val$mapper = val$mapper;
                super($anonymous0);
                Sink sink = this.downstream;
                sink.getClass();
                this.downstreamAsDouble = new ReferencePipeline$9$1$-void__init__java_util_stream_ReferencePipeline$9_this$1_java_util_stream_Sink_$anonymous0_java_util_function_Function_val$mapper_LambdaImpl0(sink);
            }

            public void begin(long size) {
                this.downstream.begin(-1);
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void accept(P_OUT u) {
                Throwable th;
                Throwable th2 = null;
                DoubleStream doubleStream = null;
                try {
                    doubleStream = (DoubleStream) this.val$mapper.apply(u);
                    if (doubleStream != null) {
                        doubleStream.sequential().forEach(this.downstreamAsDouble);
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
        }

        AnonymousClass9(ReferencePipeline this$0, AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2, Function val$mapper) {
            this.this$0 = this$0;
            this.val$mapper = val$mapper;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public Sink<P_OUT> opWrapSink(int flags, Sink<Double> sink) {
            return new AnonymousClass1(this, sink, this.val$mapper);
        }
    }

    public static class Head<E_IN, E_OUT> extends ReferencePipeline<E_IN, E_OUT> {
        public Head(Supplier<? extends Spliterator<?>> source, int sourceFlags, boolean parallel) {
            super((Supplier) source, sourceFlags, parallel);
        }

        public Head(Spliterator<?> source, int sourceFlags, boolean parallel) {
            super((Spliterator) source, sourceFlags, parallel);
        }

        public final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        public final Sink<E_IN> opWrapSink(int flags, Sink<E_OUT> sink) {
            throw new UnsupportedOperationException();
        }

        public void forEach(Consumer<? super E_OUT> action) {
            if (isParallel()) {
                super.forEach(action);
            } else {
                sourceStageSpliterator().forEachRemaining(action);
            }
        }

        public void forEachOrdered(Consumer<? super E_OUT> action) {
            if (isParallel()) {
                super.forEachOrdered(action);
            } else {
                sourceStageSpliterator().forEachRemaining(action);
            }
        }
    }

    ReferencePipeline(Supplier<? extends Spliterator<?>> source, int sourceFlags, boolean parallel) {
        super((Supplier) source, sourceFlags, parallel);
    }

    ReferencePipeline(Spliterator<?> source, int sourceFlags, boolean parallel) {
        super((Spliterator) source, sourceFlags, parallel);
    }

    ReferencePipeline(AbstractPipeline<?, P_IN, ?> upstream, int opFlags) {
        super(upstream, opFlags);
    }

    public final StreamShape getOutputShape() {
        return StreamShape.REFERENCE;
    }

    public final <P_IN> Node<P_OUT> evaluateToNode(PipelineHelper<P_OUT> helper, Spliterator<P_IN> spliterator, boolean flattenTree, IntFunction<P_OUT[]> generator) {
        return Nodes.collect(helper, spliterator, flattenTree, generator);
    }

    public final <P_IN> Spliterator<P_OUT> wrap(PipelineHelper<P_OUT> ph, Supplier<Spliterator<P_IN>> supplier, boolean isParallel) {
        return new WrappingSpliterator((PipelineHelper) ph, (Supplier) supplier, isParallel);
    }

    public final Spliterator<P_OUT> lazySpliterator(Supplier<? extends Spliterator<P_OUT>> supplier) {
        return new DelegatingSpliterator(supplier);
    }

    public final void forEachWithCancel(Spliterator<P_OUT> spliterator, Sink<P_OUT> sink) {
        while (!sink.cancellationRequested()) {
            if (!spliterator.tryAdvance(sink)) {
                return;
            }
        }
    }

    public final Builder<P_OUT> makeNodeBuilder(long exactSizeIfKnown, IntFunction<P_OUT[]> generator) {
        return Nodes.builder(exactSizeIfKnown, generator);
    }

    public final Iterator<P_OUT> iterator() {
        return Spliterators.iterator(spliterator());
    }

    public /* bridge */ /* synthetic */ BaseStream unordered() {
        return unordered();
    }

    public Stream<P_OUT> m17unordered() {
        if (isOrdered()) {
            return new AnonymousClass1(this, this, StreamShape.REFERENCE, StreamOpFlag.NOT_ORDERED);
        }
        return this;
    }

    public final Stream<P_OUT> filter(Predicate<? super P_OUT> predicate) {
        Objects.requireNonNull(predicate);
        return new AnonymousClass2(this, this, StreamShape.REFERENCE, StreamOpFlag.NOT_SIZED, predicate);
    }

    public final <R> Stream<R> map(Function<? super P_OUT, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        return new AnonymousClass3(this, this, StreamShape.REFERENCE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT, mapper);
    }

    public final IntStream mapToInt(ToIntFunction<? super P_OUT> mapper) {
        Objects.requireNonNull(mapper);
        return new AnonymousClass4(this, this, StreamShape.REFERENCE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT, mapper);
    }

    public final LongStream mapToLong(ToLongFunction<? super P_OUT> mapper) {
        Objects.requireNonNull(mapper);
        return new AnonymousClass5(this, this, StreamShape.REFERENCE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT, mapper);
    }

    public final DoubleStream mapToDouble(ToDoubleFunction<? super P_OUT> mapper) {
        Objects.requireNonNull(mapper);
        return new AnonymousClass6(this, this, StreamShape.REFERENCE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT, mapper);
    }

    public final <R> Stream<R> flatMap(Function<? super P_OUT, ? extends Stream<? extends R>> mapper) {
        Objects.requireNonNull(mapper);
        return new AnonymousClass7(this, this, StreamShape.REFERENCE, (StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) | StreamOpFlag.NOT_SIZED, mapper);
    }

    public final IntStream flatMapToInt(Function<? super P_OUT, ? extends IntStream> mapper) {
        Objects.requireNonNull(mapper);
        return new AnonymousClass8(this, this, StreamShape.REFERENCE, (StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) | StreamOpFlag.NOT_SIZED, mapper);
    }

    public final DoubleStream flatMapToDouble(Function<? super P_OUT, ? extends DoubleStream> mapper) {
        Objects.requireNonNull(mapper);
        return new AnonymousClass9(this, this, StreamShape.REFERENCE, (StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) | StreamOpFlag.NOT_SIZED, mapper);
    }

    public final LongStream flatMapToLong(Function<? super P_OUT, ? extends LongStream> mapper) {
        Objects.requireNonNull(mapper);
        return new AnonymousClass10(this, this, StreamShape.REFERENCE, (StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) | StreamOpFlag.NOT_SIZED, mapper);
    }

    public final Stream<P_OUT> peek(Consumer<? super P_OUT> action) {
        Objects.requireNonNull(action);
        return new AnonymousClass11(this, this, StreamShape.REFERENCE, 0, action);
    }

    public final Stream<P_OUT> distinct() {
        return DistinctOps.makeRef(this);
    }

    public final Stream<P_OUT> sorted() {
        return SortedOps.makeRef(this);
    }

    public final Stream<P_OUT> sorted(Comparator<? super P_OUT> comparator) {
        return SortedOps.makeRef(this, comparator);
    }

    public final Stream<P_OUT> limit(long maxSize) {
        if (maxSize >= 0) {
            return SliceOps.makeRef(this, 0, maxSize);
        }
        throw new IllegalArgumentException(Long.toString(maxSize));
    }

    public final Stream<P_OUT> skip(long n) {
        if (n < 0) {
            throw new IllegalArgumentException(Long.toString(n));
        } else if (n == 0) {
            return this;
        } else {
            return SliceOps.makeRef(this, n, -1);
        }
    }

    public void forEach(Consumer<? super P_OUT> action) {
        evaluate(ForEachOps.makeRef(action, false));
    }

    public void forEachOrdered(Consumer<? super P_OUT> action) {
        evaluate(ForEachOps.makeRef(action, true));
    }

    public final <A> A[] toArray(IntFunction<A[]> generator) {
        IntFunction<A[]> rawGenerator = generator;
        return Nodes.flatten(evaluateToArrayNode(generator), generator).asArray(generator);
    }

    public final Object[] toArray() {
        return toArray(new -java_lang_Object__toArray__LambdaImpl0());
    }

    public final boolean anyMatch(Predicate<? super P_OUT> predicate) {
        return ((Boolean) evaluate(MatchOps.makeRef(predicate, MatchKind.ANY))).booleanValue();
    }

    public final boolean allMatch(Predicate<? super P_OUT> predicate) {
        return ((Boolean) evaluate(MatchOps.makeRef(predicate, MatchKind.ALL))).booleanValue();
    }

    public final boolean noneMatch(Predicate<? super P_OUT> predicate) {
        return ((Boolean) evaluate(MatchOps.makeRef(predicate, MatchKind.NONE))).booleanValue();
    }

    public final Optional<P_OUT> findFirst() {
        return (Optional) evaluate(FindOps.makeRef(true));
    }

    public final Optional<P_OUT> findAny() {
        return (Optional) evaluate(FindOps.makeRef(false));
    }

    public final P_OUT reduce(P_OUT identity, BinaryOperator<P_OUT> accumulator) {
        return evaluate(ReduceOps.makeRef((Object) identity, (BiFunction) accumulator, (BinaryOperator) accumulator));
    }

    public final Optional<P_OUT> reduce(BinaryOperator<P_OUT> accumulator) {
        return (Optional) evaluate(ReduceOps.makeRef((BinaryOperator) accumulator));
    }

    public final <R> R reduce(R identity, BiFunction<R, ? super P_OUT, R> accumulator, BinaryOperator<R> combiner) {
        return evaluate(ReduceOps.makeRef((Object) identity, (BiFunction) accumulator, (BinaryOperator) combiner));
    }

    public final <R, A> R collect(Collector<? super P_OUT, A, R> collector) {
        A container;
        if (isParallel() && collector.characteristics().contains(Characteristics.CONCURRENT) && (!isOrdered() || collector.characteristics().contains(Characteristics.UNORDERED))) {
            container = collector.supplier().get();
            forEach(new -java_lang_Object_collect_java_util_stream_Collector_collector_LambdaImpl0(collector.accumulator(), container));
        } else {
            container = evaluate(ReduceOps.makeRef((Collector) collector));
        }
        if (collector.characteristics().contains(Characteristics.IDENTITY_FINISH)) {
            return container;
        }
        return collector.finisher().apply(container);
    }

    public final <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super P_OUT> accumulator, BiConsumer<R, R> combiner) {
        return evaluate(ReduceOps.makeRef((Supplier) supplier, (BiConsumer) accumulator, (BiConsumer) combiner));
    }

    public final Optional<P_OUT> max(Comparator<? super P_OUT> comparator) {
        return reduce(BinaryOperator.maxBy(comparator));
    }

    public final Optional<P_OUT> min(Comparator<? super P_OUT> comparator) {
        return reduce(BinaryOperator.minBy(comparator));
    }

    public final long count() {
        return mapToLong(new -long_count__LambdaImpl0()).sum();
    }
}
