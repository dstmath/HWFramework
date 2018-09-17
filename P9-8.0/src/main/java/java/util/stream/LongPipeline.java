package java.util.stream;

import java.util.LongSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterator.OfLong;
import java.util.Spliterators;
import java.util.function.BiConsumer;
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
import java.util.stream.-$Lambda$RYrQKhHyGc-mMxiERR98xxRAWkA.AnonymousClass13;
import java.util.stream.-$Lambda$RYrQKhHyGc-mMxiERR98xxRAWkA.AnonymousClass14;
import java.util.stream.-$Lambda$RYrQKhHyGc-mMxiERR98xxRAWkA.AnonymousClass15;
import java.util.stream.Node.Builder;
import java.util.stream.Sink.ChainedLong;

public abstract class LongPipeline<E_IN> extends AbstractPipeline<E_IN, Long, LongStream> implements LongStream {

    public static abstract class StatelessOp<E_IN> extends LongPipeline<E_IN> {
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
        static final /* synthetic */ boolean -assertionsDisabled = (StatefulOp.class.desiredAssertionStatus() ^ 1);

        public abstract <P_IN> Node<Long> opEvaluateParallel(PipelineHelper<Long> pipelineHelper, Spliterator<P_IN> spliterator, IntFunction<Long[]> intFunction);

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
        return new AnonymousClass15(sink);
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

    public final PrimitiveIterator.OfLong iterator() {
        return Spliterators.iterator(spliterator());
    }

    public final OfLong spliterator() {
        return adapt(super.spliterator());
    }

    public final DoubleStream asDoubleStream() {
        return new java.util.stream.DoublePipeline.StatelessOp<Long>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Long> opWrapSink(int flags, Sink<Double> sink) {
                return new ChainedLong<Double>(sink) {
                    public void accept(long t) {
                        this.downstream.-java_util_stream_ReferencePipeline$9$1-mthref-0((double) t);
                    }
                };
            }
        };
    }

    public final Stream<Long> boxed() {
        return mapToObj(new LongFunction() {
            public final Object apply(long j) {
                return $m$0(j);
            }
        });
    }

    public final LongStream map(LongUnaryOperator mapper) {
        Objects.requireNonNull(mapper);
        final LongUnaryOperator longUnaryOperator = mapper;
        return new StatelessOp<Long>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                final LongUnaryOperator longUnaryOperator = longUnaryOperator;
                return new ChainedLong<Long>(sink) {
                    public void accept(long t) {
                        this.downstream.-java_util_stream_LongPipeline-mthref-0(longUnaryOperator.applyAsLong(t));
                    }
                };
            }
        };
    }

    public final <U> Stream<U> mapToObj(LongFunction<? extends U> mapper) {
        Objects.requireNonNull(mapper);
        final LongFunction<? extends U> longFunction = mapper;
        return new java.util.stream.ReferencePipeline.StatelessOp<Long, U>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Long> opWrapSink(int flags, Sink<U> sink) {
                final LongFunction longFunction = longFunction;
                return new ChainedLong<U>(sink) {
                    public void accept(long t) {
                        this.downstream.-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-1(longFunction.apply(t));
                    }
                };
            }
        };
    }

    public final IntStream mapToInt(LongToIntFunction mapper) {
        Objects.requireNonNull(mapper);
        final LongToIntFunction longToIntFunction = mapper;
        return new java.util.stream.IntPipeline.StatelessOp<Long>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Long> opWrapSink(int flags, Sink<Integer> sink) {
                final LongToIntFunction longToIntFunction = longToIntFunction;
                return new ChainedLong<Integer>(sink) {
                    public void accept(long t) {
                        this.downstream.-java_util_stream_IntPipeline-mthref-0(longToIntFunction.applyAsInt(t));
                    }
                };
            }
        };
    }

    public final DoubleStream mapToDouble(LongToDoubleFunction mapper) {
        Objects.requireNonNull(mapper);
        final LongToDoubleFunction longToDoubleFunction = mapper;
        return new java.util.stream.DoublePipeline.StatelessOp<Long>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Long> opWrapSink(int flags, Sink<Double> sink) {
                final LongToDoubleFunction longToDoubleFunction = longToDoubleFunction;
                return new ChainedLong<Double>(sink) {
                    public void accept(long t) {
                        this.downstream.-java_util_stream_ReferencePipeline$9$1-mthref-0(longToDoubleFunction.applyAsDouble(t));
                    }
                };
            }
        };
    }

    public final LongStream flatMap(LongFunction<? extends LongStream> mapper) {
        final LongFunction<? extends LongStream> longFunction = mapper;
        return new StatelessOp<Long>(this, StreamShape.LONG_VALUE, (StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) | StreamOpFlag.NOT_SIZED) {
            public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                final LongFunction longFunction = longFunction;
                return new ChainedLong<Long>(sink) {
                    public void begin(long size) {
                        this.downstream.begin(-1);
                    }

                    public void accept(long t) {
                        Throwable th;
                        Throwable th2 = null;
                        LongStream longStream = null;
                        try {
                            longStream = (LongStream) longFunction.apply(t);
                            if (longStream != null) {
                                longStream.sequential().forEach(new AnonymousClass14(this));
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

                    /* synthetic */ void lambda$-java_util_stream_LongPipeline$6$1_11125(long i) {
                        this.downstream.-java_util_stream_LongPipeline-mthref-0(i);
                    }
                };
            }
        };
    }

    public /* bridge */ /* synthetic */ LongStream sequential() {
        return (LongStream) sequential();
    }

    public LongStream unordered() {
        if (isOrdered()) {
            return new StatelessOp<Long>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_ORDERED) {
                public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                    return sink;
                }
            };
        }
        return this;
    }

    public /* bridge */ /* synthetic */ LongStream parallel() {
        return (LongStream) parallel();
    }

    public final LongStream filter(LongPredicate predicate) {
        Objects.requireNonNull(predicate);
        final LongPredicate longPredicate = predicate;
        return new StatelessOp<Long>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SIZED) {
            public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                final LongPredicate longPredicate = longPredicate;
                return new ChainedLong<Long>(sink) {
                    public void begin(long size) {
                        this.downstream.begin(-1);
                    }

                    public void accept(long t) {
                        if (longPredicate.test(t)) {
                            this.downstream.-java_util_stream_LongPipeline-mthref-0(t);
                        }
                    }
                };
            }
        };
    }

    public final LongStream peek(LongConsumer action) {
        Objects.requireNonNull(action);
        final LongConsumer longConsumer = action;
        return new StatelessOp<Long>(this, StreamShape.LONG_VALUE, 0) {
            public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                final LongConsumer longConsumer = longConsumer;
                return new ChainedLong<Long>(sink) {
                    public void accept(long t) {
                        longConsumer.-java_util_stream_StreamSpliterators$LongWrappingSpliterator-mthref-1(t);
                        this.downstream.-java_util_stream_LongPipeline-mthref-0(t);
                    }
                };
            }
        };
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
        return boxed().distinct().mapToLong(new ToLongFunction() {
            public final long applyAsLong(Object obj) {
                return $m$0(obj);
            }
        });
    }

    public void forEach(LongConsumer action) {
        evaluate(ForEachOps.makeLong(action, false));
    }

    public void forEachOrdered(LongConsumer action) {
        evaluate(ForEachOps.makeLong(action, true));
    }

    public final long sum() {
        return reduce(0, new LongBinaryOperator() {
            public final long applyAsLong(long j, long j2) {
                return $m$0(j, j2);
            }
        });
    }

    public final OptionalLong min() {
        return reduce(new LongBinaryOperator() {
            public final long applyAsLong(long j, long j2) {
                return $m$0(j, j2);
            }
        });
    }

    public final OptionalLong max() {
        return reduce(new LongBinaryOperator() {
            public final long applyAsLong(long j, long j2) {
                return $m$0(j, j2);
            }
        });
    }

    public final OptionalDouble average() {
        long[] avg = (long[]) collect(new Supplier() {
            public final Object get() {
                return $m$0();
            }
        }, new ObjLongConsumer() {
            public final void accept(Object obj, long j) {
                $m$0(obj, j);
            }
        }, new -$Lambda$RYrQKhHyGc-mMxiERR98xxRAWkA());
        if (avg[0] > 0) {
            return OptionalDouble.of(((double) avg[1]) / ((double) avg[0]));
        }
        return OptionalDouble.empty();
    }

    static /* synthetic */ void lambda$-java_util_stream_LongPipeline_14701(long[] ll, long i) {
        ll[0] = ll[0] + 1;
        ll[1] = ll[1] + i;
    }

    static /* synthetic */ void lambda$-java_util_stream_LongPipeline_14862(long[] ll, long[] rr) {
        ll[0] = ll[0] + rr[0];
        ll[1] = ll[1] + rr[1];
    }

    public final long count() {
        return map(new LongUnaryOperator() {
            public final long applyAsLong(long j) {
                return $m$0(j);
            }
        }).sum();
    }

    public final LongSummaryStatistics summaryStatistics() {
        return (LongSummaryStatistics) collect(new Supplier() {
            public final Object get() {
                return $m$0();
            }
        }, new ObjLongConsumer() {
            public final void accept(Object obj, long j) {
                $m$0(obj, j);
            }
        }, new BiConsumer() {
            public final void accept(Object obj, Object obj2) {
                $m$0(obj, obj2);
            }
        });
    }

    public final long reduce(long identity, LongBinaryOperator op) {
        return ((Long) evaluate(ReduceOps.makeLong(identity, op))).lambda$-java_util_stream_LongPipeline_13909();
    }

    public final OptionalLong reduce(LongBinaryOperator op) {
        return (OptionalLong) evaluate(ReduceOps.makeLong(op));
    }

    public final <R> R collect(Supplier<R> supplier, ObjLongConsumer<R> accumulator, BiConsumer<R, R> combiner) {
        return evaluate(ReduceOps.makeLong(supplier, accumulator, new AnonymousClass13(combiner)));
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
        return (long[]) Nodes.flattenLong((Node.OfLong) evaluateToArrayNode(new IntFunction() {
            public final Object apply(int i) {
                return $m$0(i);
            }
        })).asPrimitiveArray();
    }
}
