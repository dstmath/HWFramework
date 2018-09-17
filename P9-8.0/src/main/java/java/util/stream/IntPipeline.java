package java.util.stream;

import java.util.IntSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterator.OfInt;
import java.util.Spliterators;
import java.util.function.BiConsumer;
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
import java.util.stream.-$Lambda$QgGTJrv63_zzBbeGjswm_UMqEbo.AnonymousClass13;
import java.util.stream.-$Lambda$QgGTJrv63_zzBbeGjswm_UMqEbo.AnonymousClass14;
import java.util.stream.-$Lambda$QgGTJrv63_zzBbeGjswm_UMqEbo.AnonymousClass15;
import java.util.stream.Node.Builder;
import java.util.stream.Sink.ChainedInt;

public abstract class IntPipeline<E_IN> extends AbstractPipeline<E_IN, Integer, IntStream> implements IntStream {

    public static abstract class StatelessOp<E_IN> extends IntPipeline<E_IN> {
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
        static final /* synthetic */ boolean -assertionsDisabled = (StatefulOp.class.desiredAssertionStatus() ^ 1);

        public abstract <P_IN> Node<Integer> opEvaluateParallel(PipelineHelper<Integer> pipelineHelper, Spliterator<P_IN> spliterator, IntFunction<Integer[]> intFunction);

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
        return new AnonymousClass15(sink);
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

    public final PrimitiveIterator.OfInt iterator() {
        return Spliterators.iterator(spliterator());
    }

    public final OfInt spliterator() {
        return adapt(super.spliterator());
    }

    public final LongStream asLongStream() {
        return new java.util.stream.LongPipeline.StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Integer> opWrapSink(int flags, Sink<Long> sink) {
                return new ChainedInt<Long>(sink) {
                    public void accept(int t) {
                        this.downstream.-java_util_stream_LongPipeline-mthref-0((long) t);
                    }
                };
            }
        };
    }

    public final DoubleStream asDoubleStream() {
        return new java.util.stream.DoublePipeline.StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Integer> opWrapSink(int flags, Sink<Double> sink) {
                return new ChainedInt<Double>(sink) {
                    public void accept(int t) {
                        this.downstream.-java_util_stream_ReferencePipeline$9$1-mthref-0((double) t);
                    }
                };
            }
        };
    }

    public final Stream<Integer> boxed() {
        return mapToObj(new IntFunction() {
            public final Object apply(int i) {
                return $m$0(i);
            }
        });
    }

    public final IntStream map(IntUnaryOperator mapper) {
        Objects.requireNonNull(mapper);
        final IntUnaryOperator intUnaryOperator = mapper;
        return new StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                final IntUnaryOperator intUnaryOperator = intUnaryOperator;
                return new ChainedInt<Integer>(sink) {
                    public void accept(int t) {
                        this.downstream.-java_util_stream_IntPipeline-mthref-0(intUnaryOperator.applyAsInt(t));
                    }
                };
            }
        };
    }

    public final <U> Stream<U> mapToObj(IntFunction<? extends U> mapper) {
        Objects.requireNonNull(mapper);
        final IntFunction<? extends U> intFunction = mapper;
        return new java.util.stream.ReferencePipeline.StatelessOp<Integer, U>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Integer> opWrapSink(int flags, Sink<U> sink) {
                final IntFunction intFunction = intFunction;
                return new ChainedInt<U>(sink) {
                    public void accept(int t) {
                        this.downstream.-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-1(intFunction.apply(t));
                    }
                };
            }
        };
    }

    public final LongStream mapToLong(IntToLongFunction mapper) {
        Objects.requireNonNull(mapper);
        final IntToLongFunction intToLongFunction = mapper;
        return new java.util.stream.LongPipeline.StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Integer> opWrapSink(int flags, Sink<Long> sink) {
                final IntToLongFunction intToLongFunction = intToLongFunction;
                return new ChainedInt<Long>(sink) {
                    public void accept(int t) {
                        this.downstream.-java_util_stream_LongPipeline-mthref-0(intToLongFunction.applyAsLong(t));
                    }
                };
            }
        };
    }

    public final DoubleStream mapToDouble(IntToDoubleFunction mapper) {
        Objects.requireNonNull(mapper);
        final IntToDoubleFunction intToDoubleFunction = mapper;
        return new java.util.stream.DoublePipeline.StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Integer> opWrapSink(int flags, Sink<Double> sink) {
                final IntToDoubleFunction intToDoubleFunction = intToDoubleFunction;
                return new ChainedInt<Double>(sink) {
                    public void accept(int t) {
                        this.downstream.-java_util_stream_ReferencePipeline$9$1-mthref-0(intToDoubleFunction.applyAsDouble(t));
                    }
                };
            }
        };
    }

    public final IntStream flatMap(IntFunction<? extends IntStream> mapper) {
        final IntFunction<? extends IntStream> intFunction = mapper;
        return new StatelessOp<Integer>(this, StreamShape.INT_VALUE, (StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) | StreamOpFlag.NOT_SIZED) {
            public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                final IntFunction intFunction = intFunction;
                return new ChainedInt<Integer>(sink) {
                    public void begin(long size) {
                        this.downstream.begin(-1);
                    }

                    public void accept(int t) {
                        Throwable th;
                        Throwable th2 = null;
                        IntStream intStream = null;
                        try {
                            intStream = (IntStream) intFunction.apply(t);
                            if (intStream != null) {
                                intStream.sequential().forEach(new AnonymousClass14(this));
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

                    /* synthetic */ void lambda$-java_util_stream_IntPipeline$7$1_11907(int i) {
                        this.downstream.-java_util_stream_IntPipeline-mthref-0(i);
                    }
                };
            }
        };
    }

    public /* bridge */ /* synthetic */ IntStream sequential() {
        return (IntStream) sequential();
    }

    public /* bridge */ /* synthetic */ IntStream parallel() {
        return (IntStream) parallel();
    }

    public IntStream unordered() {
        if (isOrdered()) {
            return new StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_ORDERED) {
                public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                    return sink;
                }
            };
        }
        return this;
    }

    public final IntStream filter(IntPredicate predicate) {
        Objects.requireNonNull(predicate);
        final IntPredicate intPredicate = predicate;
        return new StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SIZED) {
            public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                final IntPredicate intPredicate = intPredicate;
                return new ChainedInt<Integer>(sink) {
                    public void begin(long size) {
                        this.downstream.begin(-1);
                    }

                    public void accept(int t) {
                        if (intPredicate.test(t)) {
                            this.downstream.-java_util_stream_IntPipeline-mthref-0(t);
                        }
                    }
                };
            }
        };
    }

    public final IntStream peek(IntConsumer action) {
        Objects.requireNonNull(action);
        final IntConsumer intConsumer = action;
        return new StatelessOp<Integer>(this, StreamShape.INT_VALUE, 0) {
            public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                final IntConsumer intConsumer = intConsumer;
                return new ChainedInt<Integer>(sink) {
                    public void accept(int t) {
                        intConsumer.-java_util_stream_StreamSpliterators$IntWrappingSpliterator-mthref-1(t);
                        this.downstream.-java_util_stream_IntPipeline-mthref-0(t);
                    }
                };
            }
        };
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
        return boxed().distinct().mapToInt(new ToIntFunction() {
            public final int applyAsInt(Object obj) {
                return $m$0(obj);
            }
        });
    }

    public void forEach(IntConsumer action) {
        evaluate(ForEachOps.makeInt(action, false));
    }

    public void forEachOrdered(IntConsumer action) {
        evaluate(ForEachOps.makeInt(action, true));
    }

    public final int sum() {
        return reduce(0, new IntBinaryOperator() {
            public final int applyAsInt(int i, int i2) {
                return $m$0(i, i2);
            }
        });
    }

    public final OptionalInt min() {
        return reduce(new IntBinaryOperator() {
            public final int applyAsInt(int i, int i2) {
                return $m$0(i, i2);
            }
        });
    }

    public final OptionalInt max() {
        return reduce(new IntBinaryOperator() {
            public final int applyAsInt(int i, int i2) {
                return $m$0(i, i2);
            }
        });
    }

    public final long count() {
        return mapToLong(new IntToLongFunction() {
            public final long applyAsLong(int i) {
                return $m$0(i);
            }
        }).sum();
    }

    public final OptionalDouble average() {
        long[] avg = (long[]) collect(new Supplier() {
            public final Object get() {
                return $m$0();
            }
        }, new ObjIntConsumer() {
            public final void accept(Object obj, int i) {
                $m$0(obj, i);
            }
        }, new -$Lambda$QgGTJrv63_zzBbeGjswm_UMqEbo());
        if (avg[0] > 0) {
            return OptionalDouble.of(((double) avg[1]) / ((double) avg[0]));
        }
        return OptionalDouble.empty();
    }

    static /* synthetic */ void lambda$-java_util_stream_IntPipeline_15510(long[] ll, int i) {
        ll[0] = ll[0] + 1;
        ll[1] = ll[1] + ((long) i);
    }

    static /* synthetic */ void lambda$-java_util_stream_IntPipeline_15671(long[] ll, long[] rr) {
        ll[0] = ll[0] + rr[0];
        ll[1] = ll[1] + rr[1];
    }

    public final IntSummaryStatistics summaryStatistics() {
        return (IntSummaryStatistics) collect(new Supplier() {
            public final Object get() {
                return $m$0();
            }
        }, new ObjIntConsumer() {
            public final void accept(Object obj, int i) {
                $m$0(obj, i);
            }
        }, new BiConsumer() {
            public final void accept(Object obj, Object obj2) {
                $m$0(obj, obj2);
            }
        });
    }

    public final int reduce(int identity, IntBinaryOperator op) {
        return ((Integer) evaluate(ReduceOps.makeInt(identity, op))).lambda$-java_util_stream_IntPipeline_14709();
    }

    public final OptionalInt reduce(IntBinaryOperator op) {
        return (OptionalInt) evaluate(ReduceOps.makeInt(op));
    }

    public final <R> R collect(Supplier<R> supplier, ObjIntConsumer<R> accumulator, BiConsumer<R, R> combiner) {
        return evaluate(ReduceOps.makeInt(supplier, accumulator, new AnonymousClass13(combiner)));
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
        return (int[]) Nodes.flattenInt((Node.OfInt) evaluateToArrayNode(new IntFunction() {
            public final Object apply(int i) {
                return $m$0(i);
            }
        })).asPrimitiveArray();
    }
}
