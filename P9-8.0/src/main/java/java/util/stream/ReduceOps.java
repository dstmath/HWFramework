package java.util.stream;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Spliterator;
import java.util.concurrent.CountedCompleter;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;
import java.util.stream.Collector.Characteristics;
import java.util.stream.Sink.OfDouble;
import java.util.stream.Sink.OfInt;
import java.util.stream.Sink.OfLong;

final class ReduceOps {

    private static abstract class ReduceOp<T, R, S extends AccumulatingSink<T, R, S>> implements TerminalOp<T, R> {
        private final StreamShape inputShape;

        public abstract S makeSink();

        ReduceOp(StreamShape shape) {
            this.inputShape = shape;
        }

        public StreamShape inputShape() {
            return this.inputShape;
        }

        public <P_IN> R evaluateSequential(PipelineHelper<T> helper, Spliterator<P_IN> spliterator) {
            return ((AccumulatingSink) helper.wrapAndCopyInto(makeSink(), spliterator)).lambda$-java_util_stream_Collectors_49198();
        }

        public <P_IN> R evaluateParallel(PipelineHelper<T> helper, Spliterator<P_IN> spliterator) {
            return ((AccumulatingSink) new ReduceTask(this, helper, spliterator).invoke()).lambda$-java_util_stream_Collectors_49198();
        }
    }

    private static abstract class Box<U> {
        U state;

        Box() {
        }

        public U get() {
            return this.state;
        }
    }

    private interface AccumulatingSink<T, R, K extends AccumulatingSink<T, R, K>> extends TerminalSink<T, R> {
        void combine(K k);
    }

    /* renamed from: java.util.stream.ReduceOps$10ReducingSink */
    class AnonymousClass10ReducingSink extends Box<R> implements AccumulatingSink<Long, R, AnonymousClass10ReducingSink>, OfLong {
        final /* synthetic */ ObjLongConsumer val$accumulator;
        final /* synthetic */ BinaryOperator val$combiner;
        final /* synthetic */ Supplier val$supplier;

        AnonymousClass10ReducingSink(Supplier supplier, ObjLongConsumer objLongConsumer, BinaryOperator binaryOperator) {
            this.val$supplier = supplier;
            this.val$accumulator = objLongConsumer;
            this.val$combiner = binaryOperator;
        }

        public void begin(long size) {
            this.state = this.val$supplier.lambda$-java_util_stream_Collectors_49198();
        }

        public void accept(long t) {
            this.val$accumulator.accept(this.state, t);
        }

        public void combine(AnonymousClass10ReducingSink other) {
            this.state = this.val$combiner.apply(this.state, other.state);
        }
    }

    /* renamed from: java.util.stream.ReduceOps$11ReducingSink */
    class AnonymousClass11ReducingSink implements AccumulatingSink<Double, Double, AnonymousClass11ReducingSink>, OfDouble {
        private double state;
        final /* synthetic */ double val$identity;
        final /* synthetic */ DoubleBinaryOperator val$operator;

        AnonymousClass11ReducingSink(double d, DoubleBinaryOperator doubleBinaryOperator) {
            this.val$identity = d;
            this.val$operator = doubleBinaryOperator;
        }

        public void begin(long size) {
            this.state = this.val$identity;
        }

        public void accept(double t) {
            this.state = this.val$operator.applyAsDouble(this.state, t);
        }

        public Double get() {
            return Double.valueOf(this.state);
        }

        public void combine(AnonymousClass11ReducingSink other) {
            accept(other.state);
        }
    }

    /* renamed from: java.util.stream.ReduceOps$12ReducingSink */
    class AnonymousClass12ReducingSink implements AccumulatingSink<Double, OptionalDouble, AnonymousClass12ReducingSink>, OfDouble {
        private boolean empty;
        private double state;
        final /* synthetic */ DoubleBinaryOperator val$operator;

        AnonymousClass12ReducingSink(DoubleBinaryOperator doubleBinaryOperator) {
            this.val$operator = doubleBinaryOperator;
        }

        public void begin(long size) {
            this.empty = true;
            this.state = 0.0d;
        }

        public void accept(double t) {
            if (this.empty) {
                this.empty = false;
                this.state = t;
                return;
            }
            this.state = this.val$operator.applyAsDouble(this.state, t);
        }

        public OptionalDouble get() {
            return this.empty ? OptionalDouble.empty() : OptionalDouble.of(this.state);
        }

        public void combine(AnonymousClass12ReducingSink other) {
            if (!other.empty) {
                accept(other.state);
            }
        }
    }

    /* renamed from: java.util.stream.ReduceOps$13ReducingSink */
    class AnonymousClass13ReducingSink extends Box<R> implements AccumulatingSink<Double, R, AnonymousClass13ReducingSink>, OfDouble {
        final /* synthetic */ ObjDoubleConsumer val$accumulator;
        final /* synthetic */ BinaryOperator val$combiner;
        final /* synthetic */ Supplier val$supplier;

        AnonymousClass13ReducingSink(Supplier supplier, ObjDoubleConsumer objDoubleConsumer, BinaryOperator binaryOperator) {
            this.val$supplier = supplier;
            this.val$accumulator = objDoubleConsumer;
            this.val$combiner = binaryOperator;
        }

        public void begin(long size) {
            this.state = this.val$supplier.lambda$-java_util_stream_Collectors_49198();
        }

        public void accept(double t) {
            this.val$accumulator.accept(this.state, t);
        }

        public void combine(AnonymousClass13ReducingSink other) {
            this.state = this.val$combiner.apply(this.state, other.state);
        }
    }

    /* renamed from: java.util.stream.ReduceOps$1ReducingSink */
    class AnonymousClass1ReducingSink extends Box<U> implements AccumulatingSink<T, U, AnonymousClass1ReducingSink> {
        final /* synthetic */ BinaryOperator val$combiner;
        final /* synthetic */ BiFunction val$reducer;
        final /* synthetic */ Object val$seed;

        AnonymousClass1ReducingSink(Object obj, BiFunction biFunction, BinaryOperator binaryOperator) {
            this.val$seed = obj;
            this.val$reducer = biFunction;
            this.val$combiner = binaryOperator;
        }

        public void begin(long size) {
            this.state = this.val$seed;
        }

        public void accept(T t) {
            this.state = this.val$reducer.apply(this.state, t);
        }

        public void combine(AnonymousClass1ReducingSink other) {
            this.state = this.val$combiner.apply(this.state, other.state);
        }
    }

    /* renamed from: java.util.stream.ReduceOps$2ReducingSink */
    class AnonymousClass2ReducingSink implements AccumulatingSink<T, Optional<T>, AnonymousClass2ReducingSink> {
        private boolean empty;
        private T state;
        final /* synthetic */ BinaryOperator val$operator;

        AnonymousClass2ReducingSink(BinaryOperator binaryOperator) {
            this.val$operator = binaryOperator;
        }

        public void begin(long size) {
            this.empty = true;
            this.state = null;
        }

        public void accept(T t) {
            if (this.empty) {
                this.empty = false;
                this.state = t;
                return;
            }
            this.state = this.val$operator.apply(this.state, t);
        }

        public Optional<T> get() {
            return this.empty ? Optional.empty() : Optional.of(this.state);
        }

        public void combine(AnonymousClass2ReducingSink other) {
            if (!other.empty) {
                accept(other.state);
            }
        }
    }

    /* renamed from: java.util.stream.ReduceOps$3ReducingSink */
    class AnonymousClass3ReducingSink extends Box<I> implements AccumulatingSink<T, I, AnonymousClass3ReducingSink> {
        final /* synthetic */ BiConsumer val$accumulator;
        final /* synthetic */ BinaryOperator val$combiner;
        final /* synthetic */ Supplier val$supplier;

        AnonymousClass3ReducingSink(Supplier supplier, BiConsumer biConsumer, BinaryOperator binaryOperator) {
            this.val$supplier = supplier;
            this.val$accumulator = biConsumer;
            this.val$combiner = binaryOperator;
        }

        public void begin(long size) {
            this.state = this.val$supplier.lambda$-java_util_stream_Collectors_49198();
        }

        public void accept(T t) {
            this.val$accumulator.lambda$-java_util_stream_ReferencePipeline_19478(this.state, t);
        }

        public void combine(AnonymousClass3ReducingSink other) {
            this.state = this.val$combiner.apply(this.state, other.state);
        }
    }

    /* renamed from: java.util.stream.ReduceOps$4ReducingSink */
    class AnonymousClass4ReducingSink extends Box<R> implements AccumulatingSink<T, R, AnonymousClass4ReducingSink> {
        final /* synthetic */ BiConsumer val$accumulator;
        final /* synthetic */ BiConsumer val$reducer;
        final /* synthetic */ Supplier val$seedFactory;

        AnonymousClass4ReducingSink(Supplier supplier, BiConsumer biConsumer, BiConsumer biConsumer2) {
            this.val$seedFactory = supplier;
            this.val$accumulator = biConsumer;
            this.val$reducer = biConsumer2;
        }

        public void begin(long size) {
            this.state = this.val$seedFactory.lambda$-java_util_stream_Collectors_49198();
        }

        public void accept(T t) {
            this.val$accumulator.lambda$-java_util_stream_ReferencePipeline_19478(this.state, t);
        }

        public void combine(AnonymousClass4ReducingSink other) {
            this.val$reducer.lambda$-java_util_stream_ReferencePipeline_19478(this.state, other.state);
        }
    }

    /* renamed from: java.util.stream.ReduceOps$5ReducingSink */
    class AnonymousClass5ReducingSink implements AccumulatingSink<Integer, Integer, AnonymousClass5ReducingSink>, OfInt {
        private int state;
        final /* synthetic */ int val$identity;
        final /* synthetic */ IntBinaryOperator val$operator;

        AnonymousClass5ReducingSink(int i, IntBinaryOperator intBinaryOperator) {
            this.val$identity = i;
            this.val$operator = intBinaryOperator;
        }

        public void begin(long size) {
            this.state = this.val$identity;
        }

        public void accept(int t) {
            this.state = this.val$operator.applyAsInt(this.state, t);
        }

        public Integer get() {
            return Integer.valueOf(this.state);
        }

        public void combine(AnonymousClass5ReducingSink other) {
            accept(other.state);
        }
    }

    /* renamed from: java.util.stream.ReduceOps$6ReducingSink */
    class AnonymousClass6ReducingSink implements AccumulatingSink<Integer, OptionalInt, AnonymousClass6ReducingSink>, OfInt {
        private boolean empty;
        private int state;
        final /* synthetic */ IntBinaryOperator val$operator;

        AnonymousClass6ReducingSink(IntBinaryOperator intBinaryOperator) {
            this.val$operator = intBinaryOperator;
        }

        public void begin(long size) {
            this.empty = true;
            this.state = 0;
        }

        public void accept(int t) {
            if (this.empty) {
                this.empty = false;
                this.state = t;
                return;
            }
            this.state = this.val$operator.applyAsInt(this.state, t);
        }

        public OptionalInt get() {
            return this.empty ? OptionalInt.empty() : OptionalInt.of(this.state);
        }

        public void combine(AnonymousClass6ReducingSink other) {
            if (!other.empty) {
                accept(other.state);
            }
        }
    }

    /* renamed from: java.util.stream.ReduceOps$7ReducingSink */
    class AnonymousClass7ReducingSink extends Box<R> implements AccumulatingSink<Integer, R, AnonymousClass7ReducingSink>, OfInt {
        final /* synthetic */ ObjIntConsumer val$accumulator;
        final /* synthetic */ BinaryOperator val$combiner;
        final /* synthetic */ Supplier val$supplier;

        AnonymousClass7ReducingSink(Supplier supplier, ObjIntConsumer objIntConsumer, BinaryOperator binaryOperator) {
            this.val$supplier = supplier;
            this.val$accumulator = objIntConsumer;
            this.val$combiner = binaryOperator;
        }

        public void begin(long size) {
            this.state = this.val$supplier.lambda$-java_util_stream_Collectors_49198();
        }

        public void accept(int t) {
            this.val$accumulator.accept(this.state, t);
        }

        public void combine(AnonymousClass7ReducingSink other) {
            this.state = this.val$combiner.apply(this.state, other.state);
        }
    }

    /* renamed from: java.util.stream.ReduceOps$8ReducingSink */
    class AnonymousClass8ReducingSink implements AccumulatingSink<Long, Long, AnonymousClass8ReducingSink>, OfLong {
        private long state;
        final /* synthetic */ long val$identity;
        final /* synthetic */ LongBinaryOperator val$operator;

        AnonymousClass8ReducingSink(long j, LongBinaryOperator longBinaryOperator) {
            this.val$identity = j;
            this.val$operator = longBinaryOperator;
        }

        public void begin(long size) {
            this.state = this.val$identity;
        }

        public void accept(long t) {
            this.state = this.val$operator.applyAsLong(this.state, t);
        }

        public Long get() {
            return Long.valueOf(this.state);
        }

        public void combine(AnonymousClass8ReducingSink other) {
            accept(other.state);
        }
    }

    /* renamed from: java.util.stream.ReduceOps$9ReducingSink */
    class AnonymousClass9ReducingSink implements AccumulatingSink<Long, OptionalLong, AnonymousClass9ReducingSink>, OfLong {
        private boolean empty;
        private long state;
        final /* synthetic */ LongBinaryOperator val$operator;

        AnonymousClass9ReducingSink(LongBinaryOperator longBinaryOperator) {
            this.val$operator = longBinaryOperator;
        }

        public void begin(long size) {
            this.empty = true;
            this.state = 0;
        }

        public void accept(long t) {
            if (this.empty) {
                this.empty = false;
                this.state = t;
                return;
            }
            this.state = this.val$operator.applyAsLong(this.state, t);
        }

        public OptionalLong get() {
            return this.empty ? OptionalLong.empty() : OptionalLong.of(this.state);
        }

        public void combine(AnonymousClass9ReducingSink other) {
            if (!other.empty) {
                accept(other.state);
            }
        }
    }

    private static final class ReduceTask<P_IN, P_OUT, R, S extends AccumulatingSink<P_OUT, R, S>> extends AbstractTask<P_IN, P_OUT, S, ReduceTask<P_IN, P_OUT, R, S>> {
        private final ReduceOp<P_OUT, R, S> op;

        ReduceTask(ReduceOp<P_OUT, R, S> op, PipelineHelper<P_OUT> helper, Spliterator<P_IN> spliterator) {
            super((PipelineHelper) helper, (Spliterator) spliterator);
            this.op = op;
        }

        ReduceTask(ReduceTask<P_IN, P_OUT, R, S> parent, Spliterator<P_IN> spliterator) {
            super((AbstractTask) parent, (Spliterator) spliterator);
            this.op = parent.op;
        }

        protected ReduceTask<P_IN, P_OUT, R, S> makeChild(Spliterator<P_IN> spliterator) {
            return new ReduceTask(this, spliterator);
        }

        protected S doLeaf() {
            return (AccumulatingSink) this.helper.wrapAndCopyInto(this.op.makeSink(), this.spliterator);
        }

        public void onCompletion(CountedCompleter<?> caller) {
            if (!isLeaf()) {
                AccumulatingSink leftResult = (AccumulatingSink) ((ReduceTask) this.leftChild).getLocalResult();
                leftResult.combine((AccumulatingSink) ((ReduceTask) this.rightChild).getLocalResult());
                setLocalResult(leftResult);
            }
            super.onCompletion(caller);
        }
    }

    private ReduceOps() {
    }

    public static <T, U> TerminalOp<T, U> makeRef(final U seed, final BiFunction<U, ? super T, U> reducer, final BinaryOperator<U> combiner) {
        Objects.requireNonNull(reducer);
        Objects.requireNonNull(combiner);
        return new ReduceOp<T, U, AnonymousClass1ReducingSink>(StreamShape.REFERENCE) {
            public AnonymousClass1ReducingSink makeSink() {
                return new AnonymousClass1ReducingSink(seed, reducer, combiner);
            }
        };
    }

    public static <T> TerminalOp<T, Optional<T>> makeRef(final BinaryOperator<T> operator) {
        Objects.requireNonNull(operator);
        return new ReduceOp<T, Optional<T>, AnonymousClass2ReducingSink>(StreamShape.REFERENCE) {
            public AnonymousClass2ReducingSink makeSink() {
                return new AnonymousClass2ReducingSink(operator);
            }
        };
    }

    public static <T, I> TerminalOp<T, I> makeRef(Collector<? super T, I, ?> collector) {
        final Supplier<I> supplier = ((Collector) Objects.requireNonNull(collector)).supplier();
        final BiConsumer<I, ? super T> accumulator = collector.accumulator();
        final BinaryOperator<I> combiner = collector.combiner();
        final Collector<? super T, I, ?> collector2 = collector;
        return new ReduceOp<T, I, AnonymousClass3ReducingSink>(StreamShape.REFERENCE) {
            public AnonymousClass3ReducingSink makeSink() {
                return new AnonymousClass3ReducingSink(supplier, accumulator, combiner);
            }

            public int getOpFlags() {
                if (collector2.characteristics().contains(Characteristics.UNORDERED)) {
                    return StreamOpFlag.NOT_ORDERED;
                }
                return 0;
            }
        };
    }

    public static <T, R> TerminalOp<T, R> makeRef(final Supplier<R> seedFactory, final BiConsumer<R, ? super T> accumulator, final BiConsumer<R, R> reducer) {
        Objects.requireNonNull(seedFactory);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(reducer);
        return new ReduceOp<T, R, AnonymousClass4ReducingSink>(StreamShape.REFERENCE) {
            public AnonymousClass4ReducingSink makeSink() {
                return new AnonymousClass4ReducingSink(seedFactory, accumulator, reducer);
            }
        };
    }

    public static TerminalOp<Integer, Integer> makeInt(final int identity, final IntBinaryOperator operator) {
        Objects.requireNonNull(operator);
        return new ReduceOp<Integer, Integer, AnonymousClass5ReducingSink>(StreamShape.INT_VALUE) {
            public AnonymousClass5ReducingSink makeSink() {
                return new AnonymousClass5ReducingSink(identity, operator);
            }
        };
    }

    public static TerminalOp<Integer, OptionalInt> makeInt(final IntBinaryOperator operator) {
        Objects.requireNonNull(operator);
        return new ReduceOp<Integer, OptionalInt, AnonymousClass6ReducingSink>(StreamShape.INT_VALUE) {
            public AnonymousClass6ReducingSink makeSink() {
                return new AnonymousClass6ReducingSink(operator);
            }
        };
    }

    public static <R> TerminalOp<Integer, R> makeInt(final Supplier<R> supplier, final ObjIntConsumer<R> accumulator, final BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        return new ReduceOp<Integer, R, AnonymousClass7ReducingSink>(StreamShape.INT_VALUE) {
            public AnonymousClass7ReducingSink makeSink() {
                return new AnonymousClass7ReducingSink(supplier, accumulator, combiner);
            }
        };
    }

    public static TerminalOp<Long, Long> makeLong(final long identity, final LongBinaryOperator operator) {
        Objects.requireNonNull(operator);
        return new ReduceOp<Long, Long, AnonymousClass8ReducingSink>(StreamShape.LONG_VALUE) {
            public AnonymousClass8ReducingSink makeSink() {
                return new AnonymousClass8ReducingSink(identity, operator);
            }
        };
    }

    public static TerminalOp<Long, OptionalLong> makeLong(final LongBinaryOperator operator) {
        Objects.requireNonNull(operator);
        return new ReduceOp<Long, OptionalLong, AnonymousClass9ReducingSink>(StreamShape.LONG_VALUE) {
            public AnonymousClass9ReducingSink makeSink() {
                return new AnonymousClass9ReducingSink(operator);
            }
        };
    }

    public static <R> TerminalOp<Long, R> makeLong(final Supplier<R> supplier, final ObjLongConsumer<R> accumulator, final BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        return new ReduceOp<Long, R, AnonymousClass10ReducingSink>(StreamShape.LONG_VALUE) {
            public AnonymousClass10ReducingSink makeSink() {
                return new AnonymousClass10ReducingSink(supplier, accumulator, combiner);
            }
        };
    }

    public static TerminalOp<Double, Double> makeDouble(final double identity, final DoubleBinaryOperator operator) {
        Objects.requireNonNull(operator);
        return new ReduceOp<Double, Double, AnonymousClass11ReducingSink>(StreamShape.DOUBLE_VALUE) {
            public AnonymousClass11ReducingSink makeSink() {
                return new AnonymousClass11ReducingSink(identity, operator);
            }
        };
    }

    public static TerminalOp<Double, OptionalDouble> makeDouble(final DoubleBinaryOperator operator) {
        Objects.requireNonNull(operator);
        return new ReduceOp<Double, OptionalDouble, AnonymousClass12ReducingSink>(StreamShape.DOUBLE_VALUE) {
            public AnonymousClass12ReducingSink makeSink() {
                return new AnonymousClass12ReducingSink(operator);
            }
        };
    }

    public static <R> TerminalOp<Double, R> makeDouble(final Supplier<R> supplier, final ObjDoubleConsumer<R> accumulator, final BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        return new ReduceOp<Double, R, AnonymousClass13ReducingSink>(StreamShape.DOUBLE_VALUE) {
            public AnonymousClass13ReducingSink makeSink() {
                return new AnonymousClass13ReducingSink(supplier, accumulator, combiner);
            }
        };
    }
}
