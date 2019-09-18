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
import java.util.stream.Collector;

final class ReduceOps {

    private interface AccumulatingSink<T, R, K extends AccumulatingSink<T, R, K>> extends TerminalSink<T, R> {
        void combine(K k);
    }

    private static abstract class Box<U> {
        U state;

        Box() {
        }

        public U get() {
            return this.state;
        }
    }

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

    private static final class ReduceTask<P_IN, P_OUT, R, S extends AccumulatingSink<P_OUT, R, S>> extends AbstractTask<P_IN, P_OUT, S, ReduceTask<P_IN, P_OUT, R, S>> {
        private final ReduceOp<P_OUT, R, S> op;

        ReduceTask(ReduceOp<P_OUT, R, S> op2, PipelineHelper<P_OUT> helper, Spliterator<P_IN> spliterator) {
            super(helper, spliterator);
            this.op = op2;
        }

        ReduceTask(ReduceTask<P_IN, P_OUT, R, S> parent, Spliterator<P_IN> spliterator) {
            super(parent, spliterator);
            this.op = parent.op;
        }

        /* access modifiers changed from: protected */
        public ReduceTask<P_IN, P_OUT, R, S> makeChild(Spliterator<P_IN> spliterator) {
            return new ReduceTask<>(this, spliterator);
        }

        /* access modifiers changed from: protected */
        public S doLeaf() {
            return (AccumulatingSink) this.helper.wrapAndCopyInto(this.op.makeSink(), this.spliterator);
        }

        public void onCompletion(CountedCompleter<?> caller) {
            if (!isLeaf()) {
                S leftResult = (AccumulatingSink) ((ReduceTask) this.leftChild).getLocalResult();
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
                return new AccumulatingSink<T, U, AnonymousClass1ReducingSink>(reducer, combiner) {
                    final /* synthetic */ BinaryOperator val$combiner;
                    final /* synthetic */ BiFunction val$reducer;

                    {
                        this.val$reducer = r2;
                        this.val$combiner = r3;
                    }

                    public void begin(long size) {
                        this.state = Object.this;
                    }

                    public void accept(T t) {
                        this.state = this.val$reducer.apply(this.state, t);
                    }

                    public void combine(AnonymousClass1ReducingSink other) {
                        this.state = this.val$combiner.apply(this.state, other.state);
                    }
                };
            }
        };
    }

    public static <T> TerminalOp<T, Optional<T>> makeRef(final BinaryOperator<T> operator) {
        Objects.requireNonNull(operator);
        return new ReduceOp<T, Optional<T>, AnonymousClass2ReducingSink>(StreamShape.REFERENCE) {
            public AnonymousClass2ReducingSink makeSink() {
                return new AccumulatingSink<T, Optional<T>, AnonymousClass2ReducingSink>() {
                    private boolean empty;
                    private T state;

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
                        this.state = BinaryOperator.this.apply(this.state, t);
                    }

                    public Optional<T> get() {
                        return this.empty ? Optional.empty() : Optional.of(this.state);
                    }

                    public void combine(AnonymousClass2ReducingSink other) {
                        if (!other.empty) {
                            accept(other.state);
                        }
                    }
                };
            }
        };
    }

    public static <T, I> TerminalOp<T, I> makeRef(Collector<? super T, I, ?> collector) {
        Supplier<I> supplier = ((Collector) Objects.requireNonNull(collector)).supplier();
        BiConsumer<I, ? super T> accumulator = collector.accumulator();
        final BinaryOperator<I> combiner = collector.combiner();
        final BiConsumer<I, ? super T> biConsumer = accumulator;
        final Supplier<I> supplier2 = supplier;
        final Collector<? super T, I, ?> collector2 = collector;
        AnonymousClass3 r1 = new ReduceOp<T, I, AnonymousClass3ReducingSink>(StreamShape.REFERENCE) {
            public AnonymousClass3ReducingSink makeSink() {
                return new AccumulatingSink<T, I, AnonymousClass3ReducingSink>(biConsumer, combiner) {
                    final /* synthetic */ BiConsumer val$accumulator;
                    final /* synthetic */ BinaryOperator val$combiner;

                    {
                        this.val$accumulator = r2;
                        this.val$combiner = r3;
                    }

                    public void begin(long size) {
                        this.state = Supplier.this.get();
                    }

                    public void accept(T t) {
                        this.val$accumulator.accept(this.state, t);
                    }

                    public void combine(AnonymousClass3ReducingSink other) {
                        this.state = this.val$combiner.apply(this.state, other.state);
                    }
                };
            }

            public int getOpFlags() {
                if (collector2.characteristics().contains(Collector.Characteristics.UNORDERED)) {
                    return StreamOpFlag.NOT_ORDERED;
                }
                return 0;
            }
        };
        return r1;
    }

    public static <T, R> TerminalOp<T, R> makeRef(final Supplier<R> seedFactory, final BiConsumer<R, ? super T> accumulator, final BiConsumer<R, R> reducer) {
        Objects.requireNonNull(seedFactory);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(reducer);
        return new ReduceOp<T, R, AnonymousClass4ReducingSink>(StreamShape.REFERENCE) {
            public AnonymousClass4ReducingSink makeSink() {
                return new AccumulatingSink<T, R, AnonymousClass4ReducingSink>(accumulator, reducer) {
                    final /* synthetic */ BiConsumer val$accumulator;
                    final /* synthetic */ BiConsumer val$reducer;

                    {
                        this.val$accumulator = r2;
                        this.val$reducer = r3;
                    }

                    public void begin(long size) {
                        this.state = Supplier.this.get();
                    }

                    public void accept(T t) {
                        this.val$accumulator.accept(this.state, t);
                    }

                    public void combine(AnonymousClass4ReducingSink other) {
                        this.val$reducer.accept(this.state, other.state);
                    }
                };
            }
        };
    }

    public static TerminalOp<Integer, Integer> makeInt(final int identity, final IntBinaryOperator operator) {
        Objects.requireNonNull(operator);
        return new ReduceOp<Integer, Integer, AnonymousClass5ReducingSink>(StreamShape.INT_VALUE) {
            public AnonymousClass5ReducingSink makeSink() {
                return new Object(identity, operator) {
                    private int state;
                    final /* synthetic */ int val$identity;
                    final /* synthetic */ IntBinaryOperator val$operator;

                    {
                        this.val$identity = r1;
                        this.val$operator = r2;
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
                };
            }
        };
    }

    public static TerminalOp<Integer, OptionalInt> makeInt(final IntBinaryOperator operator) {
        Objects.requireNonNull(operator);
        return new ReduceOp<Integer, OptionalInt, AnonymousClass6ReducingSink>(StreamShape.INT_VALUE) {
            public AnonymousClass6ReducingSink makeSink() {
                return new Object() {
                    private boolean empty;
                    private int state;

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
                        this.state = IntBinaryOperator.this.applyAsInt(this.state, t);
                    }

                    public OptionalInt get() {
                        return this.empty ? OptionalInt.empty() : OptionalInt.of(this.state);
                    }

                    public void combine(AnonymousClass6ReducingSink other) {
                        if (!other.empty) {
                            accept(other.state);
                        }
                    }
                };
            }
        };
    }

    public static <R> TerminalOp<Integer, R> makeInt(final Supplier<R> supplier, final ObjIntConsumer<R> accumulator, final BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        return new ReduceOp<Integer, R, AnonymousClass7ReducingSink>(StreamShape.INT_VALUE) {
            public AnonymousClass7ReducingSink makeSink() {
                return new Box<R>(accumulator, combiner) {
                    final /* synthetic */ ObjIntConsumer val$accumulator;
                    final /* synthetic */ BinaryOperator val$combiner;

                    {
                        this.val$accumulator = r2;
                        this.val$combiner = r3;
                    }

                    public void begin(long size) {
                        this.state = Supplier.this.get();
                    }

                    public void accept(int t) {
                        this.val$accumulator.accept(this.state, t);
                    }

                    public void combine(AnonymousClass7ReducingSink other) {
                        this.state = this.val$combiner.apply(this.state, other.state);
                    }
                };
            }
        };
    }

    public static TerminalOp<Long, Long> makeLong(final long identity, final LongBinaryOperator operator) {
        Objects.requireNonNull(operator);
        return new ReduceOp<Long, Long, AnonymousClass8ReducingSink>(StreamShape.LONG_VALUE) {
            public AnonymousClass8ReducingSink makeSink() {
                return new Object(identity, operator) {
                    private long state;
                    final /* synthetic */ long val$identity;
                    final /* synthetic */ LongBinaryOperator val$operator;

                    {
                        this.val$identity = r1;
                        this.val$operator = r3;
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
                };
            }
        };
    }

    public static TerminalOp<Long, OptionalLong> makeLong(final LongBinaryOperator operator) {
        Objects.requireNonNull(operator);
        return new ReduceOp<Long, OptionalLong, AnonymousClass9ReducingSink>(StreamShape.LONG_VALUE) {
            public AnonymousClass9ReducingSink makeSink() {
                return new Object() {
                    private boolean empty;
                    private long state;

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
                        this.state = LongBinaryOperator.this.applyAsLong(this.state, t);
                    }

                    public OptionalLong get() {
                        return this.empty ? OptionalLong.empty() : OptionalLong.of(this.state);
                    }

                    public void combine(AnonymousClass9ReducingSink other) {
                        if (!other.empty) {
                            accept(other.state);
                        }
                    }
                };
            }
        };
    }

    public static <R> TerminalOp<Long, R> makeLong(final Supplier<R> supplier, final ObjLongConsumer<R> accumulator, final BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        return new ReduceOp<Long, R, AnonymousClass10ReducingSink>(StreamShape.LONG_VALUE) {
            public AnonymousClass10ReducingSink makeSink() {
                return new Box<R>(accumulator, combiner) {
                    final /* synthetic */ ObjLongConsumer val$accumulator;
                    final /* synthetic */ BinaryOperator val$combiner;

                    {
                        this.val$accumulator = r2;
                        this.val$combiner = r3;
                    }

                    public void begin(long size) {
                        this.state = Supplier.this.get();
                    }

                    public void accept(long t) {
                        this.val$accumulator.accept(this.state, t);
                    }

                    public void combine(AnonymousClass10ReducingSink other) {
                        this.state = this.val$combiner.apply(this.state, other.state);
                    }
                };
            }
        };
    }

    public static TerminalOp<Double, Double> makeDouble(final double identity, final DoubleBinaryOperator operator) {
        Objects.requireNonNull(operator);
        return new ReduceOp<Double, Double, AnonymousClass11ReducingSink>(StreamShape.DOUBLE_VALUE) {
            public AnonymousClass11ReducingSink makeSink() {
                return new Object(identity, operator) {
                    private double state;
                    final /* synthetic */ double val$identity;
                    final /* synthetic */ DoubleBinaryOperator val$operator;

                    {
                        this.val$identity = r1;
                        this.val$operator = r3;
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
                };
            }
        };
    }

    public static TerminalOp<Double, OptionalDouble> makeDouble(final DoubleBinaryOperator operator) {
        Objects.requireNonNull(operator);
        return new ReduceOp<Double, OptionalDouble, AnonymousClass12ReducingSink>(StreamShape.DOUBLE_VALUE) {
            public AnonymousClass12ReducingSink makeSink() {
                return new Object() {
                    private boolean empty;
                    private double state;

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
                        this.state = DoubleBinaryOperator.this.applyAsDouble(this.state, t);
                    }

                    public OptionalDouble get() {
                        return this.empty ? OptionalDouble.empty() : OptionalDouble.of(this.state);
                    }

                    public void combine(AnonymousClass12ReducingSink other) {
                        if (!other.empty) {
                            accept(other.state);
                        }
                    }
                };
            }
        };
    }

    public static <R> TerminalOp<Double, R> makeDouble(final Supplier<R> supplier, final ObjDoubleConsumer<R> accumulator, final BinaryOperator<R> combiner) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        return new ReduceOp<Double, R, AnonymousClass13ReducingSink>(StreamShape.DOUBLE_VALUE) {
            public AnonymousClass13ReducingSink makeSink() {
                return new Box<R>(accumulator, combiner) {
                    final /* synthetic */ ObjDoubleConsumer val$accumulator;
                    final /* synthetic */ BinaryOperator val$combiner;

                    {
                        this.val$accumulator = r2;
                        this.val$combiner = r3;
                    }

                    public void begin(long size) {
                        this.state = Supplier.this.get();
                    }

                    public void accept(double t) {
                        this.val$accumulator.accept(this.state, t);
                    }

                    public void combine(AnonymousClass13ReducingSink other) {
                        this.state = this.val$combiner.apply(this.state, other.state);
                    }
                };
            }
        };
    }
}
