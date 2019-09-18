package java.util.stream;

import java.lang.annotation.RCUnownedThisRef;
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
import java.util.stream.Collector;
import java.util.stream.DoublePipeline;
import java.util.stream.IntPipeline;
import java.util.stream.LongPipeline;
import java.util.stream.MatchOps;
import java.util.stream.Node;
import java.util.stream.Sink;
import java.util.stream.StreamSpliterators;

public abstract class ReferencePipeline<P_IN, P_OUT> extends AbstractPipeline<P_IN, P_OUT, Stream<P_OUT>> implements Stream<P_OUT> {

    public static class Head<E_IN, E_OUT> extends ReferencePipeline<E_IN, E_OUT> {
        public Head(Supplier<? extends Spliterator<?>> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        public Head(Spliterator<?> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        public final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        public final Sink<E_IN> opWrapSink(int flags, Sink<E_OUT> sink) {
            throw new UnsupportedOperationException();
        }

        public void forEach(Consumer<? super E_OUT> action) {
            if (!isParallel()) {
                sourceStageSpliterator().forEachRemaining(action);
            } else {
                ReferencePipeline.super.forEach(action);
            }
        }

        public void forEachOrdered(Consumer<? super E_OUT> action) {
            if (!isParallel()) {
                sourceStageSpliterator().forEachRemaining(action);
            } else {
                ReferencePipeline.super.forEachOrdered(action);
            }
        }
    }

    public static abstract class StatefulOp<E_IN, E_OUT> extends ReferencePipeline<E_IN, E_OUT> {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        public abstract <P_IN> Node<E_OUT> opEvaluateParallel(PipelineHelper<E_OUT> pipelineHelper, Spliterator<P_IN> spliterator, IntFunction<E_OUT[]> intFunction);

        static {
            Class<ReferencePipeline> cls = ReferencePipeline.class;
        }

        public StatefulOp(AbstractPipeline<?, E_IN, ?> upstream, StreamShape inputShape, int opFlags) {
            super(upstream, opFlags);
        }

        public final boolean opIsStateful() {
            return true;
        }
    }

    public static abstract class StatelessOp<E_IN, E_OUT> extends ReferencePipeline<E_IN, E_OUT> {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        static {
            Class<ReferencePipeline> cls = ReferencePipeline.class;
        }

        public StatelessOp(AbstractPipeline<?, E_IN, ?> upstream, StreamShape inputShape, int opFlags) {
            super(upstream, opFlags);
        }

        public final boolean opIsStateful() {
            return false;
        }
    }

    ReferencePipeline(Supplier<? extends Spliterator<?>> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    ReferencePipeline(Spliterator<?> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
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
        return new StreamSpliterators.WrappingSpliterator(ph, supplier, isParallel);
    }

    public final Spliterator<P_OUT> lazySpliterator(Supplier<? extends Spliterator<P_OUT>> supplier) {
        return new StreamSpliterators.DelegatingSpliterator(supplier);
    }

    public final void forEachWithCancel(Spliterator<P_OUT> spliterator, Sink<P_OUT> sink) {
        while (!sink.cancellationRequested()) {
            if (!spliterator.tryAdvance(sink)) {
                return;
            }
        }
    }

    public final Node.Builder<P_OUT> makeNodeBuilder(long exactSizeIfKnown, IntFunction<P_OUT[]> generator) {
        return Nodes.builder(exactSizeIfKnown, generator);
    }

    public final Iterator<P_OUT> iterator() {
        return Spliterators.iterator(spliterator());
    }

    public Stream<P_OUT> unordered() {
        if (!isOrdered()) {
            return this;
        }
        return new StatelessOp<P_OUT, P_OUT>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_ORDERED) {
            @RCUnownedThisRef
            public Sink<P_OUT> opWrapSink(int flags, Sink<P_OUT> sink) {
                return sink;
            }
        };
    }

    public final Stream<P_OUT> filter(Predicate<? super P_OUT> predicate) {
        Objects.requireNonNull(predicate);
        final Predicate<? super P_OUT> predicate2 = predicate;
        AnonymousClass2 r0 = new StatelessOp<P_OUT, P_OUT>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_SIZED) {
            @RCUnownedThisRef
            public Sink<P_OUT> opWrapSink(int flags, Sink<P_OUT> sink) {
                return new Sink.ChainedReference<P_OUT, P_OUT>(sink) {
                    public void begin(long size) {
                        this.downstream.begin(-1);
                    }

                    public void accept(P_OUT u) {
                        if (predicate2.test(u)) {
                            this.downstream.accept(u);
                        }
                    }
                };
            }
        };
        return r0;
    }

    public final <R> Stream<R> map(Function<? super P_OUT, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        final Function<? super P_OUT, ? extends R> function = mapper;
        AnonymousClass3 r0 = new StatelessOp<P_OUT, R>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @RCUnownedThisRef
            public Sink<P_OUT> opWrapSink(int flags, Sink<R> sink) {
                return new Sink.ChainedReference<P_OUT, R>(sink) {
                    public void accept(P_OUT u) {
                        this.downstream.accept(function.apply(u));
                    }
                };
            }
        };
        return r0;
    }

    public final IntStream mapToInt(ToIntFunction<? super P_OUT> mapper) {
        Objects.requireNonNull(mapper);
        final ToIntFunction<? super P_OUT> toIntFunction = mapper;
        AnonymousClass4 r0 = new IntPipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @RCUnownedThisRef
            public Sink<P_OUT> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedReference<P_OUT, Integer>(sink) {
                    public void accept(P_OUT u) {
                        this.downstream.accept(toIntFunction.applyAsInt(u));
                    }
                };
            }
        };
        return r0;
    }

    public final LongStream mapToLong(ToLongFunction<? super P_OUT> mapper) {
        Objects.requireNonNull(mapper);
        final ToLongFunction<? super P_OUT> toLongFunction = mapper;
        AnonymousClass5 r0 = new LongPipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @RCUnownedThisRef
            public Sink<P_OUT> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedReference<P_OUT, Long>(sink) {
                    public void accept(P_OUT u) {
                        this.downstream.accept(toLongFunction.applyAsLong(u));
                    }
                };
            }
        };
        return r0;
    }

    public final DoubleStream mapToDouble(ToDoubleFunction<? super P_OUT> mapper) {
        Objects.requireNonNull(mapper);
        final ToDoubleFunction<? super P_OUT> toDoubleFunction = mapper;
        AnonymousClass6 r0 = new DoublePipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @RCUnownedThisRef
            public Sink<P_OUT> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedReference<P_OUT, Double>(sink) {
                    public void accept(P_OUT u) {
                        this.downstream.accept(toDoubleFunction.applyAsDouble(u));
                    }
                };
            }
        };
        return r0;
    }

    public final <R> Stream<R> flatMap(Function<? super P_OUT, ? extends Stream<? extends R>> mapper) {
        Objects.requireNonNull(mapper);
        final Function<? super P_OUT, ? extends Stream<? extends R>> function = mapper;
        AnonymousClass7 r0 = new StatelessOp<P_OUT, R>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED) {
            @RCUnownedThisRef
            public Sink<P_OUT> opWrapSink(int flags, Sink<R> sink) {
                return new Sink.ChainedReference<P_OUT, R>(sink) {
                    public void begin(long size) {
                        this.downstream.begin(-1);
                    }

                    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001f, code lost:
                        if (r1 != null) goto L_0x0021;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
                        r0.close();
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0025, code lost:
                        r3 = move-exception;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0026, code lost:
                        r1.addSuppressed(r3);
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002a, code lost:
                        r0.close();
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0019, code lost:
                        r2 = move-exception;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001d, code lost:
                        if (r0 != null) goto L_0x001f;
                     */
                    public void accept(P_OUT u) {
                        Stream<? extends R> result = (Stream) function.apply(u);
                        if (result != null) {
                            ((Stream) result.sequential()).forEach(this.downstream);
                        }
                        if (result != null) {
                            result.close();
                            return;
                        }
                        return;
                        throw th;
                    }
                };
            }
        };
        return r0;
    }

    public final IntStream flatMapToInt(Function<? super P_OUT, ? extends IntStream> mapper) {
        Objects.requireNonNull(mapper);
        final Function<? super P_OUT, ? extends IntStream> function = mapper;
        AnonymousClass8 r0 = new IntPipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED) {
            @RCUnownedThisRef
            public Sink<P_OUT> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedReference<P_OUT, Integer>(sink) {
                    IntConsumer downstreamAsInt;

                    {
                        Sink sink = this.downstream;
                        Objects.requireNonNull(sink);
                        this.downstreamAsInt = new IntConsumer() {
                            public final void accept(int i) {
                                Sink.this.accept(i);
                            }
                        };
                    }

                    public void begin(long size) {
                        this.downstream.begin(-1);
                    }

                    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001d, code lost:
                        if (r1 != null) goto L_0x001f;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
                        r0.close();
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0023, code lost:
                        r3 = move-exception;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0024, code lost:
                        r1.addSuppressed(r3);
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
                        r0.close();
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0017, code lost:
                        r2 = move-exception;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001b, code lost:
                        if (r0 != null) goto L_0x001d;
                     */
                    public void accept(P_OUT u) {
                        IntStream result = (IntStream) function.apply(u);
                        if (result != null) {
                            result.sequential().forEach(this.downstreamAsInt);
                        }
                        if (result != null) {
                            result.close();
                            return;
                        }
                        return;
                        throw th;
                    }
                };
            }
        };
        return r0;
    }

    public final DoubleStream flatMapToDouble(Function<? super P_OUT, ? extends DoubleStream> mapper) {
        Objects.requireNonNull(mapper);
        final Function<? super P_OUT, ? extends DoubleStream> function = mapper;
        AnonymousClass9 r0 = new DoublePipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED) {
            @RCUnownedThisRef
            public Sink<P_OUT> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedReference<P_OUT, Double>(sink) {
                    DoubleConsumer downstreamAsDouble;

                    {
                        Sink sink = this.downstream;
                        Objects.requireNonNull(sink);
                        this.downstreamAsDouble = new DoubleConsumer() {
                            public final void accept(double d) {
                                Sink.this.accept(d);
                            }
                        };
                    }

                    public void begin(long size) {
                        this.downstream.begin(-1);
                    }

                    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001d, code lost:
                        if (r1 != null) goto L_0x001f;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
                        r0.close();
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0023, code lost:
                        r3 = move-exception;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0024, code lost:
                        r1.addSuppressed(r3);
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
                        r0.close();
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0017, code lost:
                        r2 = move-exception;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001b, code lost:
                        if (r0 != null) goto L_0x001d;
                     */
                    public void accept(P_OUT u) {
                        DoubleStream result = (DoubleStream) function.apply(u);
                        if (result != null) {
                            result.sequential().forEach(this.downstreamAsDouble);
                        }
                        if (result != null) {
                            result.close();
                            return;
                        }
                        return;
                        throw th;
                    }
                };
            }
        };
        return r0;
    }

    public final LongStream flatMapToLong(Function<? super P_OUT, ? extends LongStream> mapper) {
        Objects.requireNonNull(mapper);
        final Function<? super P_OUT, ? extends LongStream> function = mapper;
        AnonymousClass10 r0 = new LongPipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED) {
            @RCUnownedThisRef
            public Sink<P_OUT> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedReference<P_OUT, Long>(sink) {
                    LongConsumer downstreamAsLong;

                    {
                        Sink sink = this.downstream;
                        Objects.requireNonNull(sink);
                        this.downstreamAsLong = new LongConsumer() {
                            public final void accept(long j) {
                                Sink.this.accept(j);
                            }
                        };
                    }

                    public void begin(long size) {
                        this.downstream.begin(-1);
                    }

                    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001d, code lost:
                        if (r1 != null) goto L_0x001f;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
                        r0.close();
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0023, code lost:
                        r3 = move-exception;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0024, code lost:
                        r1.addSuppressed(r3);
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
                        r0.close();
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0017, code lost:
                        r2 = move-exception;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001b, code lost:
                        if (r0 != null) goto L_0x001d;
                     */
                    public void accept(P_OUT u) {
                        LongStream result = (LongStream) function.apply(u);
                        if (result != null) {
                            result.sequential().forEach(this.downstreamAsLong);
                        }
                        if (result != null) {
                            result.close();
                            return;
                        }
                        return;
                        throw th;
                    }
                };
            }
        };
        return r0;
    }

    public final Stream<P_OUT> peek(Consumer<? super P_OUT> action) {
        Objects.requireNonNull(action);
        final Consumer<? super P_OUT> consumer = action;
        AnonymousClass11 r0 = new StatelessOp<P_OUT, P_OUT>(this, StreamShape.REFERENCE, 0) {
            @RCUnownedThisRef
            public Sink<P_OUT> opWrapSink(int flags, Sink<P_OUT> sink) {
                return new Sink.ChainedReference<P_OUT, P_OUT>(sink) {
                    public void accept(P_OUT u) {
                        consumer.accept(u);
                        this.downstream.accept(u);
                    }
                };
            }
        };
        return r0;
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
        IntFunction rawGenerator = generator;
        return Nodes.flatten(evaluateToArrayNode(rawGenerator), rawGenerator).asArray(rawGenerator);
    }

    static /* synthetic */ Object[] lambda$toArray$0(int x$0) {
        return new Object[x$0];
    }

    public final Object[] toArray() {
        return toArray($$Lambda$ReferencePipeline$n3O_UMTjTSOeDSKD1yhh_2N2rRU.INSTANCE);
    }

    /* JADX WARNING: type inference failed for: r2v0, types: [java.util.function.Predicate<? super P_OUT>, java.util.function.Predicate] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public final boolean anyMatch(Predicate<? super P_OUT> r2) {
        return ((Boolean) evaluate(MatchOps.makeRef(r2, MatchOps.MatchKind.ANY))).booleanValue();
    }

    /* JADX WARNING: type inference failed for: r2v0, types: [java.util.function.Predicate<? super P_OUT>, java.util.function.Predicate] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public final boolean allMatch(Predicate<? super P_OUT> r2) {
        return ((Boolean) evaluate(MatchOps.makeRef(r2, MatchOps.MatchKind.ALL))).booleanValue();
    }

    /* JADX WARNING: type inference failed for: r2v0, types: [java.util.function.Predicate<? super P_OUT>, java.util.function.Predicate] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public final boolean noneMatch(Predicate<? super P_OUT> r2) {
        return ((Boolean) evaluate(MatchOps.makeRef(r2, MatchOps.MatchKind.NONE))).booleanValue();
    }

    public final Optional<P_OUT> findFirst() {
        return (Optional) evaluate(FindOps.makeRef(true));
    }

    public final Optional<P_OUT> findAny() {
        return (Optional) evaluate(FindOps.makeRef(false));
    }

    /* JADX WARNING: type inference failed for: r3v0, types: [java.util.function.BinaryOperator<P_OUT>, java.util.function.BiFunction, java.util.function.BinaryOperator] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public final P_OUT reduce(P_OUT identity, BinaryOperator<P_OUT> r3) {
        return evaluate(ReduceOps.makeRef(identity, r3, r3));
    }

    /* JADX WARNING: type inference failed for: r2v0, types: [java.util.function.BinaryOperator<P_OUT>, java.util.function.BinaryOperator] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public final Optional<P_OUT> reduce(BinaryOperator<P_OUT> r2) {
        return (Optional) evaluate(ReduceOps.makeRef(r2));
    }

    /* JADX WARNING: type inference failed for: r4v0, types: [java.util.function.BinaryOperator<R>, java.util.function.BinaryOperator] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public final <R> R reduce(R identity, BiFunction<R, ? super P_OUT, R> accumulator, BinaryOperator<R> r4) {
        return evaluate(ReduceOps.makeRef(identity, accumulator, r4));
    }

    public final <R, A> R collect(Collector<? super P_OUT, A, R> collector) {
        A container;
        if (!isParallel() || !collector.characteristics().contains(Collector.Characteristics.CONCURRENT) || (isOrdered() && !collector.characteristics().contains(Collector.Characteristics.UNORDERED))) {
            container = evaluate(ReduceOps.makeRef(collector));
        } else {
            container = collector.supplier().get();
            forEach(new Consumer(container) {
                private final /* synthetic */ Object f$1;

                {
                    this.f$1 = r2;
                }

                public final void accept(Object obj) {
                    BiConsumer.this.accept(this.f$1, obj);
                }
            });
        }
        if (collector.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)) {
            return container;
        }
        return collector.finisher().apply(container);
    }

    public final <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super P_OUT> accumulator, BiConsumer<R, R> combiner) {
        return evaluate(ReduceOps.makeRef(supplier, accumulator, combiner));
    }

    public final Optional<P_OUT> max(Comparator<? super P_OUT> comparator) {
        return reduce(BinaryOperator.maxBy(comparator));
    }

    public final Optional<P_OUT> min(Comparator<? super P_OUT> comparator) {
        return reduce(BinaryOperator.minBy(comparator));
    }

    static /* synthetic */ long lambda$count$2(Object e) {
        return 1;
    }

    public final long count() {
        return mapToLong($$Lambda$ReferencePipeline$mk6xSsLZAKvG89IyN8pzBoM6otw.INSTANCE).sum();
    }
}
