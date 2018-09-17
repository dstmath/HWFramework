package java.util.stream;

import java.util.DoubleSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterator.OfDouble;
import java.util.Spliterators;
import java.util.function.BiConsumer;
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
import java.util.stream.-$Lambda$15cJOyg3Zha5J4OQxNOU52CozsU.AnonymousClass15;
import java.util.stream.-$Lambda$15cJOyg3Zha5J4OQxNOU52CozsU.AnonymousClass16;
import java.util.stream.-$Lambda$15cJOyg3Zha5J4OQxNOU52CozsU.AnonymousClass17;
import java.util.stream.Node.Builder;
import java.util.stream.Sink.ChainedDouble;

public abstract class DoublePipeline<E_IN> extends AbstractPipeline<E_IN, Double, DoubleStream> implements DoubleStream {

    public static abstract class StatelessOp<E_IN> extends DoublePipeline<E_IN> {
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
        static final /* synthetic */ boolean -assertionsDisabled = (StatefulOp.class.desiredAssertionStatus() ^ 1);

        public abstract <P_IN> Node<Double> opEvaluateParallel(PipelineHelper<Double> pipelineHelper, Spliterator<P_IN> spliterator, IntFunction<Double[]> intFunction);

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
        return new AnonymousClass17(sink);
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

    public final PrimitiveIterator.OfDouble iterator() {
        return Spliterators.iterator(spliterator());
    }

    public final OfDouble spliterator() {
        return adapt(super.spliterator());
    }

    public final Stream<Double> boxed() {
        return mapToObj(new DoubleFunction() {
            public final Object apply(double d) {
                return $m$0(d);
            }
        });
    }

    public final DoubleStream map(DoubleUnaryOperator mapper) {
        Objects.requireNonNull(mapper);
        final DoubleUnaryOperator doubleUnaryOperator = mapper;
        return new StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
                final DoubleUnaryOperator doubleUnaryOperator = doubleUnaryOperator;
                return new ChainedDouble<Double>(sink) {
                    public void accept(double t) {
                        this.downstream.-java_util_stream_ReferencePipeline$9$1-mthref-0(doubleUnaryOperator.applyAsDouble(t));
                    }
                };
            }
        };
    }

    public final <U> Stream<U> mapToObj(DoubleFunction<? extends U> mapper) {
        Objects.requireNonNull(mapper);
        final DoubleFunction<? extends U> doubleFunction = mapper;
        return new java.util.stream.ReferencePipeline.StatelessOp<Double, U>(this, StreamShape.DOUBLE_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Double> opWrapSink(int flags, Sink<U> sink) {
                final DoubleFunction doubleFunction = doubleFunction;
                return new ChainedDouble<U>(sink) {
                    public void accept(double t) {
                        this.downstream.-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-1(doubleFunction.apply(t));
                    }
                };
            }
        };
    }

    public final IntStream mapToInt(DoubleToIntFunction mapper) {
        Objects.requireNonNull(mapper);
        final DoubleToIntFunction doubleToIntFunction = mapper;
        return new java.util.stream.IntPipeline.StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Double> opWrapSink(int flags, Sink<Integer> sink) {
                final DoubleToIntFunction doubleToIntFunction = doubleToIntFunction;
                return new ChainedDouble<Integer>(sink) {
                    public void accept(double t) {
                        this.downstream.-java_util_stream_IntPipeline-mthref-0(doubleToIntFunction.applyAsInt(t));
                    }
                };
            }
        };
    }

    public final LongStream mapToLong(DoubleToLongFunction mapper) {
        Objects.requireNonNull(mapper);
        final DoubleToLongFunction doubleToLongFunction = mapper;
        return new java.util.stream.LongPipeline.StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Double> opWrapSink(int flags, Sink<Long> sink) {
                final DoubleToLongFunction doubleToLongFunction = doubleToLongFunction;
                return new ChainedDouble<Long>(sink) {
                    public void accept(double t) {
                        this.downstream.-java_util_stream_LongPipeline-mthref-0(doubleToLongFunction.applyAsLong(t));
                    }
                };
            }
        };
    }

    public final DoubleStream flatMap(DoubleFunction<? extends DoubleStream> mapper) {
        final DoubleFunction<? extends DoubleStream> doubleFunction = mapper;
        return new StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, (StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) | StreamOpFlag.NOT_SIZED) {
            public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
                final DoubleFunction doubleFunction = doubleFunction;
                return new ChainedDouble<Double>(sink) {
                    public void begin(long size) {
                        this.downstream.begin(-1);
                    }

                    public void accept(double t) {
                        Throwable th;
                        Throwable th2 = null;
                        DoubleStream doubleStream = null;
                        try {
                            doubleStream = (DoubleStream) doubleFunction.apply(t);
                            if (doubleStream != null) {
                                doubleStream.sequential().forEach(new AnonymousClass16(this));
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

                    /* synthetic */ void lambda$-java_util_stream_DoublePipeline$5$1_10563(double i) {
                        this.downstream.-java_util_stream_ReferencePipeline$9$1-mthref-0(i);
                    }
                };
            }
        };
    }

    public DoubleStream unordered() {
        if (isOrdered()) {
            return new StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, StreamOpFlag.NOT_ORDERED) {
                public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
                    return sink;
                }
            };
        }
        return this;
    }

    public final DoubleStream filter(DoublePredicate predicate) {
        Objects.requireNonNull(predicate);
        final DoublePredicate doublePredicate = predicate;
        return new StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, StreamOpFlag.NOT_SIZED) {
            public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
                final DoublePredicate doublePredicate = doublePredicate;
                return new ChainedDouble<Double>(sink) {
                    public void begin(long size) {
                        this.downstream.begin(-1);
                    }

                    public void accept(double t) {
                        if (doublePredicate.test(t)) {
                            this.downstream.-java_util_stream_ReferencePipeline$9$1-mthref-0(t);
                        }
                    }
                };
            }
        };
    }

    public /* bridge */ /* synthetic */ DoubleStream sequential() {
        return (DoubleStream) sequential();
    }

    public /* bridge */ /* synthetic */ DoubleStream parallel() {
        return (DoubleStream) parallel();
    }

    public final DoubleStream peek(DoubleConsumer action) {
        Objects.requireNonNull(action);
        final DoubleConsumer doubleConsumer = action;
        return new StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, 0) {
            public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
                final DoubleConsumer doubleConsumer = doubleConsumer;
                return new ChainedDouble<Double>(sink) {
                    public void accept(double t) {
                        doubleConsumer.-java_util_stream_StreamSpliterators$DoubleWrappingSpliterator-mthref-1(t);
                        this.downstream.-java_util_stream_ReferencePipeline$9$1-mthref-0(t);
                    }
                };
            }
        };
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
        return boxed().distinct().mapToDouble(new ToDoubleFunction() {
            public final double applyAsDouble(Object obj) {
                return $m$0(obj);
            }
        });
    }

    public void forEach(DoubleConsumer consumer) {
        evaluate(ForEachOps.makeDouble(consumer, false));
    }

    public void forEachOrdered(DoubleConsumer consumer) {
        evaluate(ForEachOps.makeDouble(consumer, true));
    }

    public final double sum() {
        return Collectors.computeFinalSum((double[]) collect(new Supplier() {
            public final Object get() {
                return $m$0();
            }
        }, new ObjDoubleConsumer() {
            public final void accept(Object obj, double d) {
                $m$0(obj, d);
            }
        }, new BiConsumer() {
            public final void accept(Object obj, Object obj2) {
                $m$0(obj, obj2);
            }
        }));
    }

    static /* synthetic */ void lambda$-java_util_stream_DoublePipeline_14331(double[] ll, double d) {
        Collectors.sumWithCompensation(ll, d);
        ll[2] = ll[2] + d;
    }

    static /* synthetic */ void lambda$-java_util_stream_DoublePipeline_14530(double[] ll, double[] rr) {
        Collectors.sumWithCompensation(ll, rr[0]);
        Collectors.sumWithCompensation(ll, rr[1]);
        ll[2] = ll[2] + rr[2];
    }

    public final OptionalDouble min() {
        return reduce(new DoubleBinaryOperator() {
            public final double applyAsDouble(double d, double d2) {
                return $m$0(d, d2);
            }
        });
    }

    public final OptionalDouble max() {
        return reduce(new DoubleBinaryOperator() {
            public final double applyAsDouble(double d, double d2) {
                return $m$0(d, d2);
            }
        });
    }

    public final OptionalDouble average() {
        double[] avg = (double[]) collect(new Supplier() {
            public final Object get() {
                return $m$0();
            }
        }, new ObjDoubleConsumer() {
            public final void accept(Object obj, double d) {
                $m$0(obj, d);
            }
        }, new -$Lambda$15cJOyg3Zha5J4OQxNOU52CozsU());
        if (avg[2] > 0.0d) {
            return OptionalDouble.of(Collectors.computeFinalSum(avg) / avg[2]);
        }
        return OptionalDouble.empty();
    }

    static /* synthetic */ void lambda$-java_util_stream_DoublePipeline_15880(double[] ll, double d) {
        ll[2] = ll[2] + 1.0d;
        Collectors.sumWithCompensation(ll, d);
        ll[3] = ll[3] + d;
    }

    static /* synthetic */ void lambda$-java_util_stream_DoublePipeline_16123(double[] ll, double[] rr) {
        Collectors.sumWithCompensation(ll, rr[0]);
        Collectors.sumWithCompensation(ll, rr[1]);
        ll[2] = ll[2] + rr[2];
        ll[3] = ll[3] + rr[3];
    }

    public final long count() {
        return mapToLong(new DoubleToLongFunction() {
            public final long applyAsLong(double d) {
                return $m$0(d);
            }
        }).sum();
    }

    public final DoubleSummaryStatistics summaryStatistics() {
        return (DoubleSummaryStatistics) collect(new Supplier() {
            public final Object get() {
                return $m$0();
            }
        }, new ObjDoubleConsumer() {
            public final void accept(Object obj, double d) {
                $m$0(obj, d);
            }
        }, new BiConsumer() {
            public final void accept(Object obj, Object obj2) {
                $m$0(obj, obj2);
            }
        });
    }

    public final double reduce(double identity, DoubleBinaryOperator op) {
        return ((Double) evaluate(ReduceOps.makeDouble(identity, op))).lambda$-java_util_stream_DoublePipeline_13468();
    }

    public final OptionalDouble reduce(DoubleBinaryOperator op) {
        return (OptionalDouble) evaluate(ReduceOps.makeDouble(op));
    }

    public final <R> R collect(Supplier<R> supplier, ObjDoubleConsumer<R> accumulator, BiConsumer<R, R> combiner) {
        return evaluate(ReduceOps.makeDouble(supplier, accumulator, new AnonymousClass15(combiner)));
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
        return (double[]) Nodes.flattenDouble((Node.OfDouble) evaluateToArrayNode(new IntFunction() {
            public final Object apply(int i) {
                return $m$0(i);
            }
        })).asPrimitiveArray();
    }
}
