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
            return ((AccumulatingSink) helper.wrapAndCopyInto(makeSink(), spliterator)).get();
        }

        public <P_IN> R evaluateParallel(PipelineHelper<T> helper, Spliterator<P_IN> spliterator) {
            return ((AccumulatingSink) new ReduceTask(this, helper, spliterator).invoke()).get();
        }
    }

    /* renamed from: java.util.stream.ReduceOps.10 */
    static class AnonymousClass10 extends ReduceOp<Long, R, AnonymousClass10ReducingSink> {
        final /* synthetic */ ObjLongConsumer val$accumulator;
        final /* synthetic */ BinaryOperator val$combiner;
        final /* synthetic */ Supplier val$supplier;

        AnonymousClass10(StreamShape $anonymous0, Supplier val$supplier, ObjLongConsumer val$accumulator, BinaryOperator val$combiner) {
            this.val$supplier = val$supplier;
            this.val$accumulator = val$accumulator;
            this.val$combiner = val$combiner;
            super($anonymous0);
        }

        public AnonymousClass10ReducingSink makeSink() {
            return new AnonymousClass10ReducingSink(this.val$supplier, this.val$accumulator, this.val$combiner);
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

    /* renamed from: java.util.stream.ReduceOps.10ReducingSink */
    class AnonymousClass10ReducingSink extends Box<R> implements AccumulatingSink<Long, R, AnonymousClass10ReducingSink>, OfLong {
        final /* synthetic */ ObjLongConsumer val$accumulator;
        final /* synthetic */ BinaryOperator val$combiner;
        final /* synthetic */ Supplier val$supplier;

        AnonymousClass10ReducingSink(Supplier val$supplier, ObjLongConsumer val$accumulator, BinaryOperator val$combiner) {
            this.val$supplier = val$supplier;
            this.val$accumulator = val$accumulator;
            this.val$combiner = val$combiner;
        }

        public void begin(long size) {
            this.state = this.val$supplier.get();
        }

        public void accept(long t) {
            this.val$accumulator.accept(this.state, t);
        }

        public void combine(AnonymousClass10ReducingSink other) {
            this.state = this.val$combiner.apply(this.state, other.state);
        }
    }

    /* renamed from: java.util.stream.ReduceOps.11 */
    static class AnonymousClass11 extends ReduceOp<Double, Double, AnonymousClass11ReducingSink> {
        final /* synthetic */ double val$identity;
        final /* synthetic */ DoubleBinaryOperator val$operator;

        AnonymousClass11(StreamShape $anonymous0, double val$identity, DoubleBinaryOperator val$operator) {
            this.val$identity = val$identity;
            this.val$operator = val$operator;
            super($anonymous0);
        }

        public AnonymousClass11ReducingSink makeSink() {
            return new AnonymousClass11ReducingSink(this.val$identity, this.val$operator);
        }
    }

    /* renamed from: java.util.stream.ReduceOps.11ReducingSink */
    class AnonymousClass11ReducingSink implements AccumulatingSink<Double, Double, AnonymousClass11ReducingSink>, OfDouble {
        private double state;
        final /* synthetic */ double val$identity;
        final /* synthetic */ DoubleBinaryOperator val$operator;

        AnonymousClass11ReducingSink(double val$identity, DoubleBinaryOperator val$operator) {
            this.val$identity = val$identity;
            this.val$operator = val$operator;
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

    /* renamed from: java.util.stream.ReduceOps.12 */
    static class AnonymousClass12 extends ReduceOp<Double, OptionalDouble, AnonymousClass12ReducingSink> {
        final /* synthetic */ DoubleBinaryOperator val$operator;

        AnonymousClass12(StreamShape $anonymous0, DoubleBinaryOperator val$operator) {
            this.val$operator = val$operator;
            super($anonymous0);
        }

        public AnonymousClass12ReducingSink makeSink() {
            return new AnonymousClass12ReducingSink(this.val$operator);
        }
    }

    /* renamed from: java.util.stream.ReduceOps.12ReducingSink */
    class AnonymousClass12ReducingSink implements AccumulatingSink<Double, OptionalDouble, AnonymousClass12ReducingSink>, OfDouble {
        private boolean empty;
        private double state;
        final /* synthetic */ DoubleBinaryOperator val$operator;

        AnonymousClass12ReducingSink(DoubleBinaryOperator val$operator) {
            this.val$operator = val$operator;
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

    /* renamed from: java.util.stream.ReduceOps.13 */
    static class AnonymousClass13 extends ReduceOp<Double, R, AnonymousClass13ReducingSink> {
        final /* synthetic */ ObjDoubleConsumer val$accumulator;
        final /* synthetic */ BinaryOperator val$combiner;
        final /* synthetic */ Supplier val$supplier;

        AnonymousClass13(StreamShape $anonymous0, Supplier val$supplier, ObjDoubleConsumer val$accumulator, BinaryOperator val$combiner) {
            this.val$supplier = val$supplier;
            this.val$accumulator = val$accumulator;
            this.val$combiner = val$combiner;
            super($anonymous0);
        }

        public AnonymousClass13ReducingSink makeSink() {
            return new AnonymousClass13ReducingSink(this.val$supplier, this.val$accumulator, this.val$combiner);
        }
    }

    /* renamed from: java.util.stream.ReduceOps.13ReducingSink */
    class AnonymousClass13ReducingSink extends Box<R> implements AccumulatingSink<Double, R, AnonymousClass13ReducingSink>, OfDouble {
        final /* synthetic */ ObjDoubleConsumer val$accumulator;
        final /* synthetic */ BinaryOperator val$combiner;
        final /* synthetic */ Supplier val$supplier;

        AnonymousClass13ReducingSink(Supplier val$supplier, ObjDoubleConsumer val$accumulator, BinaryOperator val$combiner) {
            this.val$supplier = val$supplier;
            this.val$accumulator = val$accumulator;
            this.val$combiner = val$combiner;
        }

        public void begin(long size) {
            this.state = this.val$supplier.get();
        }

        public void accept(double t) {
            this.val$accumulator.accept(this.state, t);
        }

        public void combine(AnonymousClass13ReducingSink other) {
            this.state = this.val$combiner.apply(this.state, other.state);
        }
    }

    /* renamed from: java.util.stream.ReduceOps.1 */
    static class AnonymousClass1 extends ReduceOp<T, U, AnonymousClass1ReducingSink> {
        final /* synthetic */ BinaryOperator val$combiner;
        final /* synthetic */ BiFunction val$reducer;
        final /* synthetic */ Object val$seed;

        AnonymousClass1(StreamShape $anonymous0, Object val$seed, BiFunction val$reducer, BinaryOperator val$combiner) {
            this.val$seed = val$seed;
            this.val$reducer = val$reducer;
            this.val$combiner = val$combiner;
            super($anonymous0);
        }

        public AnonymousClass1ReducingSink makeSink() {
            return new AnonymousClass1ReducingSink(this.val$seed, this.val$reducer, this.val$combiner);
        }
    }

    /* renamed from: java.util.stream.ReduceOps.1ReducingSink */
    class AnonymousClass1ReducingSink extends Box<U> implements AccumulatingSink<T, U, AnonymousClass1ReducingSink> {
        final /* synthetic */ BinaryOperator val$combiner;
        final /* synthetic */ BiFunction val$reducer;
        final /* synthetic */ Object val$seed;

        AnonymousClass1ReducingSink(Object val$seed, BiFunction val$reducer, BinaryOperator val$combiner) {
            this.val$seed = val$seed;
            this.val$reducer = val$reducer;
            this.val$combiner = val$combiner;
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

    /* renamed from: java.util.stream.ReduceOps.2 */
    static class AnonymousClass2 extends ReduceOp<T, Optional<T>, AnonymousClass2ReducingSink> {
        final /* synthetic */ BinaryOperator val$operator;

        AnonymousClass2(StreamShape $anonymous0, BinaryOperator val$operator) {
            this.val$operator = val$operator;
            super($anonymous0);
        }

        public AnonymousClass2ReducingSink makeSink() {
            return new AnonymousClass2ReducingSink(this.val$operator);
        }
    }

    /* renamed from: java.util.stream.ReduceOps.2ReducingSink */
    class AnonymousClass2ReducingSink implements AccumulatingSink<T, Optional<T>, AnonymousClass2ReducingSink> {
        private boolean empty;
        private T state;
        final /* synthetic */ BinaryOperator val$operator;

        AnonymousClass2ReducingSink(BinaryOperator val$operator) {
            this.val$operator = val$operator;
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

    /* renamed from: java.util.stream.ReduceOps.3 */
    static class AnonymousClass3 extends ReduceOp<T, I, AnonymousClass3ReducingSink> {
        final /* synthetic */ BiConsumer val$accumulator;
        final /* synthetic */ Collector val$collector;
        final /* synthetic */ BinaryOperator val$combiner;
        final /* synthetic */ Supplier val$supplier;

        AnonymousClass3(StreamShape $anonymous0, Collector val$collector, Supplier val$supplier, BiConsumer val$accumulator, BinaryOperator val$combiner) {
            this.val$collector = val$collector;
            this.val$supplier = val$supplier;
            this.val$accumulator = val$accumulator;
            this.val$combiner = val$combiner;
            super($anonymous0);
        }

        public AnonymousClass3ReducingSink makeSink() {
            return new AnonymousClass3ReducingSink(this.val$supplier, this.val$accumulator, this.val$combiner);
        }

        public int getOpFlags() {
            if (this.val$collector.characteristics().contains(Characteristics.UNORDERED)) {
                return StreamOpFlag.NOT_ORDERED;
            }
            return 0;
        }
    }

    /* renamed from: java.util.stream.ReduceOps.3ReducingSink */
    class AnonymousClass3ReducingSink extends Box<I> implements AccumulatingSink<T, I, AnonymousClass3ReducingSink> {
        final /* synthetic */ BiConsumer val$accumulator;
        final /* synthetic */ BinaryOperator val$combiner;
        final /* synthetic */ Supplier val$supplier;

        AnonymousClass3ReducingSink(Supplier val$supplier, BiConsumer val$accumulator, BinaryOperator val$combiner) {
            this.val$supplier = val$supplier;
            this.val$accumulator = val$accumulator;
            this.val$combiner = val$combiner;
        }

        public void begin(long size) {
            this.state = this.val$supplier.get();
        }

        public void accept(T t) {
            this.val$accumulator.accept(this.state, t);
        }

        public void combine(AnonymousClass3ReducingSink other) {
            this.state = this.val$combiner.apply(this.state, other.state);
        }
    }

    /* renamed from: java.util.stream.ReduceOps.4 */
    static class AnonymousClass4 extends ReduceOp<T, R, AnonymousClass4ReducingSink> {
        final /* synthetic */ BiConsumer val$accumulator;
        final /* synthetic */ BiConsumer val$reducer;
        final /* synthetic */ Supplier val$seedFactory;

        AnonymousClass4(StreamShape $anonymous0, Supplier val$seedFactory, BiConsumer val$accumulator, BiConsumer val$reducer) {
            this.val$seedFactory = val$seedFactory;
            this.val$accumulator = val$accumulator;
            this.val$reducer = val$reducer;
            super($anonymous0);
        }

        public AnonymousClass4ReducingSink makeSink() {
            return new AnonymousClass4ReducingSink(this.val$seedFactory, this.val$accumulator, this.val$reducer);
        }
    }

    /* renamed from: java.util.stream.ReduceOps.4ReducingSink */
    class AnonymousClass4ReducingSink extends Box<R> implements AccumulatingSink<T, R, AnonymousClass4ReducingSink> {
        final /* synthetic */ BiConsumer val$accumulator;
        final /* synthetic */ BiConsumer val$reducer;
        final /* synthetic */ Supplier val$seedFactory;

        AnonymousClass4ReducingSink(Supplier val$seedFactory, BiConsumer val$accumulator, BiConsumer val$reducer) {
            this.val$seedFactory = val$seedFactory;
            this.val$accumulator = val$accumulator;
            this.val$reducer = val$reducer;
        }

        public void begin(long size) {
            this.state = this.val$seedFactory.get();
        }

        public void accept(T t) {
            this.val$accumulator.accept(this.state, t);
        }

        public void combine(AnonymousClass4ReducingSink other) {
            this.val$reducer.accept(this.state, other.state);
        }
    }

    /* renamed from: java.util.stream.ReduceOps.5 */
    static class AnonymousClass5 extends ReduceOp<Integer, Integer, AnonymousClass5ReducingSink> {
        final /* synthetic */ int val$identity;
        final /* synthetic */ IntBinaryOperator val$operator;

        AnonymousClass5(StreamShape $anonymous0, int val$identity, IntBinaryOperator val$operator) {
            this.val$identity = val$identity;
            this.val$operator = val$operator;
            super($anonymous0);
        }

        public AnonymousClass5ReducingSink makeSink() {
            return new AnonymousClass5ReducingSink(this.val$identity, this.val$operator);
        }
    }

    /* renamed from: java.util.stream.ReduceOps.5ReducingSink */
    class AnonymousClass5ReducingSink implements AccumulatingSink<Integer, Integer, AnonymousClass5ReducingSink>, OfInt {
        private int state;
        final /* synthetic */ int val$identity;
        final /* synthetic */ IntBinaryOperator val$operator;

        AnonymousClass5ReducingSink(int val$identity, IntBinaryOperator val$operator) {
            this.val$identity = val$identity;
            this.val$operator = val$operator;
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

    /* renamed from: java.util.stream.ReduceOps.6 */
    static class AnonymousClass6 extends ReduceOp<Integer, OptionalInt, AnonymousClass6ReducingSink> {
        final /* synthetic */ IntBinaryOperator val$operator;

        AnonymousClass6(StreamShape $anonymous0, IntBinaryOperator val$operator) {
            this.val$operator = val$operator;
            super($anonymous0);
        }

        public AnonymousClass6ReducingSink makeSink() {
            return new AnonymousClass6ReducingSink(this.val$operator);
        }
    }

    /* renamed from: java.util.stream.ReduceOps.6ReducingSink */
    class AnonymousClass6ReducingSink implements AccumulatingSink<Integer, OptionalInt, AnonymousClass6ReducingSink>, OfInt {
        private boolean empty;
        private int state;
        final /* synthetic */ IntBinaryOperator val$operator;

        AnonymousClass6ReducingSink(IntBinaryOperator val$operator) {
            this.val$operator = val$operator;
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

    /* renamed from: java.util.stream.ReduceOps.7 */
    static class AnonymousClass7 extends ReduceOp<Integer, R, AnonymousClass7ReducingSink> {
        final /* synthetic */ ObjIntConsumer val$accumulator;
        final /* synthetic */ BinaryOperator val$combiner;
        final /* synthetic */ Supplier val$supplier;

        AnonymousClass7(StreamShape $anonymous0, Supplier val$supplier, ObjIntConsumer val$accumulator, BinaryOperator val$combiner) {
            this.val$supplier = val$supplier;
            this.val$accumulator = val$accumulator;
            this.val$combiner = val$combiner;
            super($anonymous0);
        }

        public AnonymousClass7ReducingSink makeSink() {
            return new AnonymousClass7ReducingSink(this.val$supplier, this.val$accumulator, this.val$combiner);
        }
    }

    /* renamed from: java.util.stream.ReduceOps.7ReducingSink */
    class AnonymousClass7ReducingSink extends Box<R> implements AccumulatingSink<Integer, R, AnonymousClass7ReducingSink>, OfInt {
        final /* synthetic */ ObjIntConsumer val$accumulator;
        final /* synthetic */ BinaryOperator val$combiner;
        final /* synthetic */ Supplier val$supplier;

        AnonymousClass7ReducingSink(Supplier val$supplier, ObjIntConsumer val$accumulator, BinaryOperator val$combiner) {
            this.val$supplier = val$supplier;
            this.val$accumulator = val$accumulator;
            this.val$combiner = val$combiner;
        }

        public void begin(long size) {
            this.state = this.val$supplier.get();
        }

        public void accept(int t) {
            this.val$accumulator.accept(this.state, t);
        }

        public void combine(AnonymousClass7ReducingSink other) {
            this.state = this.val$combiner.apply(this.state, other.state);
        }
    }

    /* renamed from: java.util.stream.ReduceOps.8 */
    static class AnonymousClass8 extends ReduceOp<Long, Long, AnonymousClass8ReducingSink> {
        final /* synthetic */ long val$identity;
        final /* synthetic */ LongBinaryOperator val$operator;

        AnonymousClass8(StreamShape $anonymous0, long val$identity, LongBinaryOperator val$operator) {
            this.val$identity = val$identity;
            this.val$operator = val$operator;
            super($anonymous0);
        }

        public AnonymousClass8ReducingSink makeSink() {
            return new AnonymousClass8ReducingSink(this.val$identity, this.val$operator);
        }
    }

    /* renamed from: java.util.stream.ReduceOps.8ReducingSink */
    class AnonymousClass8ReducingSink implements AccumulatingSink<Long, Long, AnonymousClass8ReducingSink>, OfLong {
        private long state;
        final /* synthetic */ long val$identity;
        final /* synthetic */ LongBinaryOperator val$operator;

        AnonymousClass8ReducingSink(long val$identity, LongBinaryOperator val$operator) {
            this.val$identity = val$identity;
            this.val$operator = val$operator;
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

    /* renamed from: java.util.stream.ReduceOps.9 */
    static class AnonymousClass9 extends ReduceOp<Long, OptionalLong, AnonymousClass9ReducingSink> {
        final /* synthetic */ LongBinaryOperator val$operator;

        AnonymousClass9(StreamShape $anonymous0, LongBinaryOperator val$operator) {
            this.val$operator = val$operator;
            super($anonymous0);
        }

        public AnonymousClass9ReducingSink makeSink() {
            return new AnonymousClass9ReducingSink(this.val$operator);
        }
    }

    /* renamed from: java.util.stream.ReduceOps.9ReducingSink */
    class AnonymousClass9ReducingSink implements AccumulatingSink<Long, OptionalLong, AnonymousClass9ReducingSink>, OfLong {
        private boolean empty;
        private long state;
        final /* synthetic */ LongBinaryOperator val$operator;

        AnonymousClass9ReducingSink(LongBinaryOperator val$operator) {
            this.val$operator = val$operator;
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

    public static <T, U> TerminalOp<T, U> makeRef(U seed, BiFunction<U, ? super T, U> reducer, BinaryOperator<U> combiner) {
        Objects.requireNonNull(reducer);
        Objects.requireNonNull(combiner);
        return new AnonymousClass1(StreamShape.REFERENCE, seed, reducer, combiner);
    }

    public static <T> TerminalOp<T, Optional<T>> makeRef(BinaryOperator<T> operator) {
        Objects.requireNonNull(operator);
        return new AnonymousClass2(StreamShape.REFERENCE, operator);
    }

    public static <T, I> TerminalOp<T, I> makeRef(Collector<? super T, I, ?> collector) {
        return new AnonymousClass3(StreamShape.REFERENCE, collector, ((Collector) Objects.requireNonNull(collector)).supplier(), collector.accumulator(), collector.combiner());
    }

    public static <T, R> TerminalOp<T, R> makeRef(Supplier<R> seedFactory, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> reducer) {
        Objects.requireNonNull(seedFactory);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(reducer);
        return new AnonymousClass4(StreamShape.REFERENCE, seedFactory, accumulator, reducer);
    }

    public static TerminalOp<Integer, Integer> makeInt(int identity, IntBinaryOperator operator) {
        Objects.requireNonNull(operator);
        return new AnonymousClass5(StreamShape.INT_VALUE, identity, operator);
    }

    public static TerminalOp<Integer, OptionalInt> makeInt(IntBinaryOperator operator) {
        Objects.requireNonNull(operator);
        return new AnonymousClass6(StreamShape.INT_VALUE, operator);
    }

    public static <R> TerminalOp<Integer, R> makeInt(Supplier<R> supplier, ObjIntConsumer<R> accumulator, BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        return new AnonymousClass7(StreamShape.INT_VALUE, supplier, accumulator, combiner);
    }

    public static TerminalOp<Long, Long> makeLong(long identity, LongBinaryOperator operator) {
        Objects.requireNonNull(operator);
        return new AnonymousClass8(StreamShape.LONG_VALUE, identity, operator);
    }

    public static TerminalOp<Long, OptionalLong> makeLong(LongBinaryOperator operator) {
        Objects.requireNonNull(operator);
        return new AnonymousClass9(StreamShape.LONG_VALUE, operator);
    }

    public static <R> TerminalOp<Long, R> makeLong(Supplier<R> supplier, ObjLongConsumer<R> accumulator, BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        return new AnonymousClass10(StreamShape.LONG_VALUE, supplier, accumulator, combiner);
    }

    public static TerminalOp<Double, Double> makeDouble(double identity, DoubleBinaryOperator operator) {
        Objects.requireNonNull(operator);
        return new AnonymousClass11(StreamShape.DOUBLE_VALUE, identity, operator);
    }

    public static TerminalOp<Double, OptionalDouble> makeDouble(DoubleBinaryOperator operator) {
        Objects.requireNonNull(operator);
        return new AnonymousClass12(StreamShape.DOUBLE_VALUE, operator);
    }

    public static <R> TerminalOp<Double, R> makeDouble(Supplier<R> supplier, ObjDoubleConsumer<R> accumulator, BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        return new AnonymousClass13(StreamShape.DOUBLE_VALUE, supplier, accumulator, combiner);
    }
}
