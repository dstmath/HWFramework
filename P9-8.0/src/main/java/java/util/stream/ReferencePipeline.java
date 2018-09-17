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
        static final /* synthetic */ boolean -assertionsDisabled = (StatefulOp.class.desiredAssertionStatus() ^ 1);

        public abstract <P_IN> Node<E_OUT> opEvaluateParallel(PipelineHelper<E_OUT> pipelineHelper, Spliterator<P_IN> spliterator, IntFunction<E_OUT[]> intFunction);

        public StatefulOp(AbstractPipeline<?, E_IN, ?> upstream, StreamShape inputShape, int opFlags) {
            super(upstream, opFlags);
            if (!-assertionsDisabled && upstream.getOutputShape() != inputShape) {
                throw new AssertionError();
            }
        }

        public final boolean opIsStateful() {
            return true;
        }
    }

    public static abstract class StatelessOp<E_IN, E_OUT> extends ReferencePipeline<E_IN, E_OUT> {
        static final /* synthetic */ boolean -assertionsDisabled = (StatelessOp.class.desiredAssertionStatus() ^ 1);

        public StatelessOp(AbstractPipeline<?, E_IN, ?> upstream, StreamShape inputShape, int opFlags) {
            super(upstream, opFlags);
            if (!-assertionsDisabled && upstream.getOutputShape() != inputShape) {
                throw new AssertionError();
            }
        }

        public final boolean opIsStateful() {
            return false;
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

    public Stream<P_OUT> unordered() {
        if (isOrdered()) {
            return new StatelessOp<P_OUT, P_OUT>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_ORDERED) {
                public Sink<P_OUT> opWrapSink(int flags, Sink<P_OUT> sink) {
                    return sink;
                }
            };
        }
        return this;
    }

    public final Stream<P_OUT> filter(Predicate<? super P_OUT> predicate) {
        Objects.requireNonNull(predicate);
        final Predicate<? super P_OUT> predicate2 = predicate;
        return new StatelessOp<P_OUT, P_OUT>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_SIZED) {
            public Sink<P_OUT> opWrapSink(int flags, Sink<P_OUT> sink) {
                final Predicate predicate = predicate2;
                return new ChainedReference<P_OUT, P_OUT>(sink) {
                    public void begin(long size) {
                        this.downstream.begin(-1);
                    }

                    public void accept(P_OUT u) {
                        if (predicate.test(u)) {
                            this.downstream.-java_util_stream_SortedOps$RefSortingSink-mthref-0(u);
                        }
                    }
                };
            }
        };
    }

    public final <R> Stream<R> map(Function<? super P_OUT, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        final Function<? super P_OUT, ? extends R> function = mapper;
        return new StatelessOp<P_OUT, R>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<P_OUT> opWrapSink(int flags, Sink<R> sink) {
                final Function function = function;
                return new ChainedReference<P_OUT, R>(sink) {
                    public void accept(P_OUT u) {
                        this.downstream.-java_util_stream_SortedOps$RefSortingSink-mthref-0(function.lambda$-java_util_stream_Collectors_49854(u));
                    }
                };
            }
        };
    }

    public final IntStream mapToInt(ToIntFunction<? super P_OUT> mapper) {
        Objects.requireNonNull(mapper);
        final ToIntFunction<? super P_OUT> toIntFunction = mapper;
        return new java.util.stream.IntPipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<P_OUT> opWrapSink(int flags, Sink<Integer> sink) {
                final ToIntFunction toIntFunction = toIntFunction;
                return new ChainedReference<P_OUT, Integer>(sink) {
                    public void accept(P_OUT u) {
                        this.downstream.-java_util_stream_IntPipeline-mthref-0(toIntFunction.applyAsInt(u));
                    }
                };
            }
        };
    }

    public final LongStream mapToLong(ToLongFunction<? super P_OUT> mapper) {
        Objects.requireNonNull(mapper);
        final ToLongFunction<? super P_OUT> toLongFunction = mapper;
        return new java.util.stream.LongPipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<P_OUT> opWrapSink(int flags, Sink<Long> sink) {
                final ToLongFunction toLongFunction = toLongFunction;
                return new ChainedReference<P_OUT, Long>(sink) {
                    public void accept(P_OUT u) {
                        this.downstream.-java_util_stream_LongPipeline-mthref-0(toLongFunction.applyAsLong(u));
                    }
                };
            }
        };
    }

    public final DoubleStream mapToDouble(ToDoubleFunction<? super P_OUT> mapper) {
        Objects.requireNonNull(mapper);
        final ToDoubleFunction<? super P_OUT> toDoubleFunction = mapper;
        return new java.util.stream.DoublePipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<P_OUT> opWrapSink(int flags, Sink<Double> sink) {
                final ToDoubleFunction toDoubleFunction = toDoubleFunction;
                return new ChainedReference<P_OUT, Double>(sink) {
                    public void accept(P_OUT u) {
                        this.downstream.-java_util_stream_ReferencePipeline$9$1-mthref-0(toDoubleFunction.applyAsDouble(u));
                    }
                };
            }
        };
    }

    public final <R> Stream<R> flatMap(Function<? super P_OUT, ? extends Stream<? extends R>> mapper) {
        Objects.requireNonNull(mapper);
        final Function<? super P_OUT, ? extends Stream<? extends R>> function = mapper;
        return new StatelessOp<P_OUT, R>(this, StreamShape.REFERENCE, (StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) | StreamOpFlag.NOT_SIZED) {
            public Sink<P_OUT> opWrapSink(int flags, Sink<R> sink) {
                final Function function = function;
                return new ChainedReference<P_OUT, R>(sink) {
                    public void begin(long size) {
                        this.downstream.begin(-1);
                    }

                    public void accept(P_OUT u) {
                        Throwable th;
                        Throwable th2 = null;
                        Stream stream = null;
                        try {
                            Stream<? extends R> result = (Stream) function.lambda$-java_util_stream_Collectors_49854(u);
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
                            return;
                        } catch (Throwable th22) {
                            Throwable th4 = th22;
                            th22 = th;
                            th = th4;
                        }
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (Throwable th5) {
                                if (th22 == null) {
                                    th22 = th5;
                                } else if (th22 != th5) {
                                    th22.addSuppressed(th5);
                                }
                            }
                        }
                        if (th22 != null) {
                            throw th22;
                        }
                        throw th;
                    }
                };
            }
        };
    }

    public final IntStream flatMapToInt(Function<? super P_OUT, ? extends IntStream> mapper) {
        Objects.requireNonNull(mapper);
        final Function<? super P_OUT, ? extends IntStream> function = mapper;
        return new java.util.stream.IntPipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE, (StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) | StreamOpFlag.NOT_SIZED) {
            public Sink<P_OUT> opWrapSink(int flags, Sink<Integer> sink) {
                final Function function = function;
                return new ChainedReference<P_OUT, Integer>(sink) {
                    IntConsumer downstreamAsInt;

                    public void begin(long size) {
                        this.downstream.begin(-1);
                    }

                    public void accept(P_OUT u) {
                        Throwable th;
                        Throwable th2 = null;
                        IntStream intStream = null;
                        try {
                            intStream = (IntStream) function.lambda$-java_util_stream_Collectors_49854(u);
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
                            return;
                        } catch (Throwable th22) {
                            Throwable th4 = th22;
                            th22 = th;
                            th = th4;
                        }
                        if (intStream != null) {
                            try {
                                intStream.close();
                            } catch (Throwable th5) {
                                if (th22 == null) {
                                    th22 = th5;
                                } else if (th22 != th5) {
                                    th22.addSuppressed(th5);
                                }
                            }
                        }
                        if (th22 != null) {
                            throw th22;
                        }
                        throw th;
                    }
                };
            }
        };
    }

    public final DoubleStream flatMapToDouble(Function<? super P_OUT, ? extends DoubleStream> mapper) {
        Objects.requireNonNull(mapper);
        final Function<? super P_OUT, ? extends DoubleStream> function = mapper;
        return new java.util.stream.DoublePipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE, (StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) | StreamOpFlag.NOT_SIZED) {
            public Sink<P_OUT> opWrapSink(int flags, Sink<Double> sink) {
                final Function function = function;
                return new ChainedReference<P_OUT, Double>(sink) {
                    DoubleConsumer downstreamAsDouble;

                    public void begin(long size) {
                        this.downstream.begin(-1);
                    }

                    public void accept(P_OUT u) {
                        Throwable th;
                        Throwable th2 = null;
                        DoubleStream doubleStream = null;
                        try {
                            doubleStream = (DoubleStream) function.lambda$-java_util_stream_Collectors_49854(u);
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
                            return;
                        } catch (Throwable th22) {
                            Throwable th4 = th22;
                            th22 = th;
                            th = th4;
                        }
                        if (doubleStream != null) {
                            try {
                                doubleStream.close();
                            } catch (Throwable th5) {
                                if (th22 == null) {
                                    th22 = th5;
                                } else if (th22 != th5) {
                                    th22.addSuppressed(th5);
                                }
                            }
                        }
                        if (th22 != null) {
                            throw th22;
                        }
                        throw th;
                    }
                };
            }
        };
    }

    public final LongStream flatMapToLong(Function<? super P_OUT, ? extends LongStream> mapper) {
        Objects.requireNonNull(mapper);
        final Function<? super P_OUT, ? extends LongStream> function = mapper;
        return new java.util.stream.LongPipeline.StatelessOp<P_OUT>(this, StreamShape.REFERENCE, (StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) | StreamOpFlag.NOT_SIZED) {
            public Sink<P_OUT> opWrapSink(int flags, Sink<Long> sink) {
                final Function function = function;
                return new ChainedReference<P_OUT, Long>(sink) {
                    LongConsumer downstreamAsLong;

                    public void begin(long size) {
                        this.downstream.begin(-1);
                    }

                    public void accept(P_OUT u) {
                        Throwable th;
                        Throwable th2 = null;
                        LongStream longStream = null;
                        try {
                            longStream = (LongStream) function.lambda$-java_util_stream_Collectors_49854(u);
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
                            return;
                        } catch (Throwable th22) {
                            Throwable th4 = th22;
                            th22 = th;
                            th = th4;
                        }
                        if (longStream != null) {
                            try {
                                longStream.close();
                            } catch (Throwable th5) {
                                if (th22 == null) {
                                    th22 = th5;
                                } else if (th22 != th5) {
                                    th22.addSuppressed(th5);
                                }
                            }
                        }
                        if (th22 != null) {
                            throw th22;
                        }
                        throw th;
                    }
                };
            }
        };
    }

    public final Stream<P_OUT> peek(Consumer<? super P_OUT> action) {
        Objects.requireNonNull(action);
        final Consumer<? super P_OUT> consumer = action;
        return new StatelessOp<P_OUT, P_OUT>(this, StreamShape.REFERENCE, 0) {
            public Sink<P_OUT> opWrapSink(int flags, Sink<P_OUT> sink) {
                final Consumer consumer = consumer;
                return new ChainedReference<P_OUT, P_OUT>(sink) {
                    public void accept(P_OUT u) {
                        consumer.-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-1(u);
                        this.downstream.-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-1(u);
                    }
                };
            }
        };
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
        return toArray(new -$Lambda$DJvCeprCIGMk0JvfSkTmQUmEYKA());
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
            container = collector.supplier().lambda$-java_util_stream_Collectors_49198();
            forEach(new java.util.stream.-$Lambda$DJvCeprCIGMk0JvfSkTmQUmEYKA.AnonymousClass5(collector.accumulator(), container));
        } else {
            container = evaluate(ReduceOps.makeRef((Collector) collector));
        }
        if (collector.characteristics().contains(Characteristics.IDENTITY_FINISH)) {
            return container;
        }
        return collector.finisher().lambda$-java_util_stream_Collectors_49854(container);
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
        return mapToLong(new ToLongFunction() {
            public final long applyAsLong(Object obj) {
                return $m$0(obj);
            }
        }).sum();
    }
}
