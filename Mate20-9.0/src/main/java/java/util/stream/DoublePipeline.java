package java.util.stream;

import java.util.DoubleSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
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
import java.util.stream.IntPipeline;
import java.util.stream.LongPipeline;
import java.util.stream.MatchOps;
import java.util.stream.Node;
import java.util.stream.ReferencePipeline;
import java.util.stream.Sink;
import java.util.stream.StreamSpliterators;

public abstract class DoublePipeline<E_IN> extends AbstractPipeline<E_IN, Double, DoubleStream> implements DoubleStream {

    public static class Head<E_IN> extends DoublePipeline<E_IN> {
        public /* bridge */ /* synthetic */ DoubleStream parallel() {
            return (DoubleStream) DoublePipeline.super.parallel();
        }

        public /* bridge */ /* synthetic */ DoubleStream sequential() {
            return (DoubleStream) DoublePipeline.super.sequential();
        }

        public Head(Supplier<? extends Spliterator<Double>> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        public Head(Spliterator<Double> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        public final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        public final Sink<E_IN> opWrapSink(int flags, Sink<Double> sink) {
            throw new UnsupportedOperationException();
        }

        public void forEach(DoubleConsumer consumer) {
            if (!isParallel()) {
                DoublePipeline.adapt((Spliterator<Double>) sourceStageSpliterator()).forEachRemaining(consumer);
            } else {
                DoublePipeline.super.forEach(consumer);
            }
        }

        public void forEachOrdered(DoubleConsumer consumer) {
            if (!isParallel()) {
                DoublePipeline.adapt((Spliterator<Double>) sourceStageSpliterator()).forEachRemaining(consumer);
            } else {
                DoublePipeline.super.forEachOrdered(consumer);
            }
        }
    }

    public static abstract class StatefulOp<E_IN> extends DoublePipeline<E_IN> {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        public abstract <P_IN> Node<Double> opEvaluateParallel(PipelineHelper<Double> pipelineHelper, Spliterator<P_IN> spliterator, IntFunction<Double[]> intFunction);

        static {
            Class<DoublePipeline> cls = DoublePipeline.class;
        }

        public /* bridge */ /* synthetic */ DoubleStream parallel() {
            return (DoubleStream) DoublePipeline.super.parallel();
        }

        public /* bridge */ /* synthetic */ DoubleStream sequential() {
            return (DoubleStream) DoublePipeline.super.sequential();
        }

        public StatefulOp(AbstractPipeline<?, E_IN, ?> upstream, StreamShape inputShape, int opFlags) {
            super(upstream, opFlags);
        }

        public final boolean opIsStateful() {
            return true;
        }
    }

    public static abstract class StatelessOp<E_IN> extends DoublePipeline<E_IN> {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        static {
            Class<DoublePipeline> cls = DoublePipeline.class;
        }

        public /* bridge */ /* synthetic */ DoubleStream parallel() {
            return (DoubleStream) DoublePipeline.super.parallel();
        }

        public /* bridge */ /* synthetic */ DoubleStream sequential() {
            return (DoubleStream) DoublePipeline.super.sequential();
        }

        public StatelessOp(AbstractPipeline<?, E_IN, ?> upstream, StreamShape inputShape, int opFlags) {
            super(upstream, opFlags);
        }

        public final boolean opIsStateful() {
            return false;
        }
    }

    public /* bridge */ /* synthetic */ DoubleStream parallel() {
        return (DoubleStream) super.parallel();
    }

    public /* bridge */ /* synthetic */ DoubleStream sequential() {
        return (DoubleStream) super.sequential();
    }

    DoublePipeline(Supplier<? extends Spliterator<Double>> source, int sourceFlags, boolean parallel) {
        super((Supplier<? extends Spliterator<?>>) source, sourceFlags, parallel);
    }

    DoublePipeline(Spliterator<Double> source, int sourceFlags, boolean parallel) {
        super((Spliterator<?>) source, sourceFlags, parallel);
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
        Objects.requireNonNull(sink);
        return new DoubleConsumer() {
            public final void accept(double d) {
                Sink.this.accept(d);
            }
        };
    }

    /* access modifiers changed from: private */
    public static Spliterator.OfDouble adapt(Spliterator<Double> s) {
        if (s instanceof Spliterator.OfDouble) {
            return (Spliterator.OfDouble) s;
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
        return new StreamSpliterators.DoubleWrappingSpliterator(ph, supplier, isParallel);
    }

    public final Spliterator.OfDouble lazySpliterator(Supplier<? extends Spliterator<Double>> supplier) {
        return new StreamSpliterators.DelegatingSpliterator.OfDouble(supplier);
    }

    public final void forEachWithCancel(Spliterator<Double> spliterator, Sink<Double> sink) {
        Spliterator.OfDouble spl = adapt(spliterator);
        DoubleConsumer adaptedSink = adapt(sink);
        while (!sink.cancellationRequested()) {
            if (!spl.tryAdvance(adaptedSink)) {
                return;
            }
        }
    }

    public final Node.Builder<Double> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Double[]> intFunction) {
        return Nodes.doubleBuilder(exactSizeIfKnown);
    }

    public final PrimitiveIterator.OfDouble iterator() {
        return Spliterators.iterator(spliterator());
    }

    public final Spliterator.OfDouble spliterator() {
        return adapt((Spliterator<Double>) super.spliterator());
    }

    public final Stream<Double> boxed() {
        return mapToObj($$Lambda$0HimmAYr5h1pFdNckEhxJ9y9Zqk.INSTANCE);
    }

    public final DoubleStream map(DoubleUnaryOperator mapper) {
        Objects.requireNonNull(mapper);
        final DoubleUnaryOperator doubleUnaryOperator = mapper;
        AnonymousClass1 r0 = new StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedDouble<Double>(sink) {
                    public void accept(double t) {
                        this.downstream.accept(doubleUnaryOperator.applyAsDouble(t));
                    }
                };
            }
        };
        return r0;
    }

    public final <U> Stream<U> mapToObj(DoubleFunction<? extends U> mapper) {
        Objects.requireNonNull(mapper);
        final DoubleFunction<? extends U> doubleFunction = mapper;
        AnonymousClass2 r0 = new ReferencePipeline.StatelessOp<Double, U>(this, StreamShape.DOUBLE_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Double> opWrapSink(int flags, Sink<U> sink) {
                return new Sink.ChainedDouble<U>(sink) {
                    public void accept(double t) {
                        this.downstream.accept(doubleFunction.apply(t));
                    }
                };
            }
        };
        return r0;
    }

    public final IntStream mapToInt(DoubleToIntFunction mapper) {
        Objects.requireNonNull(mapper);
        final DoubleToIntFunction doubleToIntFunction = mapper;
        AnonymousClass3 r0 = new IntPipeline.StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Double> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedDouble<Integer>(sink) {
                    public void accept(double t) {
                        this.downstream.accept(doubleToIntFunction.applyAsInt(t));
                    }
                };
            }
        };
        return r0;
    }

    public final LongStream mapToLong(DoubleToLongFunction mapper) {
        Objects.requireNonNull(mapper);
        final DoubleToLongFunction doubleToLongFunction = mapper;
        AnonymousClass4 r0 = new LongPipeline.StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Double> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedDouble<Long>(sink) {
                    public void accept(double t) {
                        this.downstream.accept(doubleToLongFunction.applyAsLong(t));
                    }
                };
            }
        };
        return r0;
    }

    public final DoubleStream flatMap(DoubleFunction<? extends DoubleStream> mapper) {
        final DoubleFunction<? extends DoubleStream> doubleFunction = mapper;
        AnonymousClass5 r0 = new StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED) {
            public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedDouble<Double>(sink) {
                    public void begin(long size) {
                        this.downstream.begin(-1);
                    }

                    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0020, code lost:
                        if (r1 != null) goto L_0x0022;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
                        r0.close();
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0026, code lost:
                        r3 = move-exception;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0027, code lost:
                        r1.addSuppressed(r3);
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002b, code lost:
                        r0.close();
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:5:0x001a, code lost:
                        r2 = move-exception;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001e, code lost:
                        if (r0 != null) goto L_0x0020;
                     */
                    public void accept(double t) {
                        DoubleStream result = (DoubleStream) doubleFunction.apply(t);
                        if (result != null) {
                            result.sequential().forEach(
                            /*  JADX ERROR: Method code generation error
                                jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0016: INVOKE  (wrap: java.util.stream.DoubleStream
                                  0x000d: INVOKE  (r2v1 java.util.stream.DoubleStream) = (r0v3 'result' java.util.stream.DoubleStream) java.util.stream.DoubleStream.sequential():java.util.stream.DoubleStream type: INTERFACE), (wrap: java.util.stream.-$$Lambda$DoublePipeline$5$1$kqJiVK7sQB3kKvPk9DB9gInHJq4
                                  0x0013: CONSTRUCTOR  (r3v1 java.util.stream.-$$Lambda$DoublePipeline$5$1$kqJiVK7sQB3kKvPk9DB9gInHJq4) = (r4v0 'this' java.util.stream.DoublePipeline$5$1 A[THIS]) java.util.stream.-$$Lambda$DoublePipeline$5$1$kqJiVK7sQB3kKvPk9DB9gInHJq4.<init>(java.util.stream.DoublePipeline$5$1):void CONSTRUCTOR) java.util.stream.DoubleStream.forEach(java.util.function.DoubleConsumer):void type: INTERFACE in method: java.util.stream.DoublePipeline.5.1.accept(double):void, dex: boot_classes.dex
                                	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                                	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                                	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:142)
                                	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
                                	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                                	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                                	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
                                	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
                                	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                                	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:665)
                                	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:596)
                                	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:303)
                                	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                                	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                                	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                                	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
                                	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
                                	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                                	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:665)
                                	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:596)
                                	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                                	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                                	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                                	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
                                	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
                                	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                                	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                                	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                                	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                                	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                                	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                                	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                                	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                                Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0013: CONSTRUCTOR  (r3v1 java.util.stream.-$$Lambda$DoublePipeline$5$1$kqJiVK7sQB3kKvPk9DB9gInHJq4) = (r4v0 'this' java.util.stream.DoublePipeline$5$1 A[THIS]) java.util.stream.-$$Lambda$DoublePipeline$5$1$kqJiVK7sQB3kKvPk9DB9gInHJq4.<init>(java.util.stream.DoublePipeline$5$1):void CONSTRUCTOR in method: java.util.stream.DoublePipeline.5.1.accept(double):void, dex: boot_classes.dex
                                	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                                	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                	... 53 more
                                Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: java.util.stream.-$$Lambda$DoublePipeline$5$1$kqJiVK7sQB3kKvPk9DB9gInHJq4, state: NOT_LOADED
                                	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                                	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                                	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                                	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                                	... 58 more
                                */
                            /* JADX WARNING: Code restructure failed: missing block: B:10:0x0020, code lost:
                                if (r1 != null) goto L_0x0022;
                             */
                            /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
                                r0.close();
                             */
                            /* JADX WARNING: Code restructure failed: missing block: B:13:0x0026, code lost:
                                r3 = move-exception;
                             */
                            /* JADX WARNING: Code restructure failed: missing block: B:14:0x0027, code lost:
                                r1.addSuppressed(r3);
                             */
                            /* JADX WARNING: Code restructure failed: missing block: B:15:0x002b, code lost:
                                r0.close();
                             */
                            /* JADX WARNING: Code restructure failed: missing block: B:5:0x001a, code lost:
                                r2 = move-exception;
                             */
                            /* JADX WARNING: Code restructure failed: missing block: B:9:0x001e, code lost:
                                if (r0 != null) goto L_0x0020;
                             */
                            /*
                                this = this;
                                java.util.stream.DoublePipeline$5 r0 = java.util.stream.DoublePipeline.AnonymousClass5.this
                                java.util.function.DoubleFunction r0 = r5
                                java.lang.Object r0 = r0.apply(r5)
                                java.util.stream.DoubleStream r0 = (java.util.stream.DoubleStream) r0
                                if (r0 == 0) goto L_0x002f
                                r1 = 0
                                java.util.stream.DoubleStream r2 = r0.sequential()     // Catch:{ Throwable -> 0x001c }
                                java.util.stream.-$$Lambda$DoublePipeline$5$1$kqJiVK7sQB3kKvPk9DB9gInHJq4 r3 = new java.util.stream.-$$Lambda$DoublePipeline$5$1$kqJiVK7sQB3kKvPk9DB9gInHJq4     // Catch:{ Throwable -> 0x001c }
                                r3.<init>(r4)     // Catch:{ Throwable -> 0x001c }
                                r2.forEach(r3)     // Catch:{ Throwable -> 0x001c }
                                goto L_0x002f
                            L_0x001a:
                                r2 = move-exception
                                goto L_0x001e
                            L_0x001c:
                                r1 = move-exception
                                throw r1     // Catch:{ all -> 0x001a }
                            L_0x001e:
                                if (r0 == 0) goto L_0x002e
                                if (r1 == 0) goto L_0x002b
                                r0.close()     // Catch:{ Throwable -> 0x0026 }
                                goto L_0x002e
                            L_0x0026:
                                r3 = move-exception
                                r1.addSuppressed(r3)
                                goto L_0x002e
                            L_0x002b:
                                r0.close()
                            L_0x002e:
                                throw r2
                            L_0x002f:
                                if (r0 == 0) goto L_0x0034
                                r0.close()
                            L_0x0034:
                                return
                            */
                            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DoublePipeline.AnonymousClass5.AnonymousClass1.accept(double):void");
                        }
                    };
                }
            };
            return r0;
        }

        public DoubleStream unordered() {
            if (!isOrdered()) {
                return this;
            }
            return new StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, StreamOpFlag.NOT_ORDERED) {
                public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
                    return sink;
                }
            };
        }

        public final DoubleStream filter(DoublePredicate predicate) {
            Objects.requireNonNull(predicate);
            final DoublePredicate doublePredicate = predicate;
            AnonymousClass7 r0 = new StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, StreamOpFlag.NOT_SIZED) {
                public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
                    return new Sink.ChainedDouble<Double>(sink) {
                        public void begin(long size) {
                            this.downstream.begin(-1);
                        }

                        public void accept(double t) {
                            if (doublePredicate.test(t)) {
                                this.downstream.accept(t);
                            }
                        }
                    };
                }
            };
            return r0;
        }

        public final DoubleStream peek(DoubleConsumer action) {
            Objects.requireNonNull(action);
            final DoubleConsumer doubleConsumer = action;
            AnonymousClass8 r0 = new StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, 0) {
                public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
                    return new Sink.ChainedDouble<Double>(sink) {
                        public void accept(double t) {
                            doubleConsumer.accept(t);
                            this.downstream.accept(t);
                        }
                    };
                }
            };
            return r0;
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
            return boxed().distinct().mapToDouble($$Lambda$DoublePipeline$gq0fD9NZ938fl5Zgm1Lwm9G2tpI.INSTANCE);
        }

        public void forEach(DoubleConsumer consumer) {
            evaluate(ForEachOps.makeDouble(consumer, false));
        }

        public void forEachOrdered(DoubleConsumer consumer) {
            evaluate(ForEachOps.makeDouble(consumer, true));
        }

        static /* synthetic */ double[] lambda$sum$1() {
            return new double[3];
        }

        public final double sum() {
            return Collectors.computeFinalSum((double[]) collect($$Lambda$DoublePipeline$jsM76ecD5K_oP4TaArM1RdmdjOw.INSTANCE, $$Lambda$DoublePipeline$btJQIF5a5bk658mbj9AIl0UV19Q.INSTANCE, $$Lambda$DoublePipeline$KYIKJiRuFnKlAv02sN6Y0G5US7E.INSTANCE));
        }

        static /* synthetic */ void lambda$sum$2(double[] ll, double d) {
            Collectors.sumWithCompensation(ll, d);
            ll[2] = ll[2] + d;
        }

        static /* synthetic */ void lambda$sum$3(double[] ll, double[] rr) {
            Collectors.sumWithCompensation(ll, rr[0]);
            Collectors.sumWithCompensation(ll, rr[1]);
            ll[2] = ll[2] + rr[2];
        }

        public final OptionalDouble min() {
            return reduce($$Lambda$Xsl4nKeYydTETtdRjTtEXmjJItE.INSTANCE);
        }

        public final OptionalDouble max() {
            return reduce($$Lambda$xi7ZBZfKmkbt5CSsaL8qlNeHupc.INSTANCE);
        }

        static /* synthetic */ double[] lambda$average$4() {
            return new double[4];
        }

        public final OptionalDouble average() {
            double[] avg = (double[]) collect($$Lambda$DoublePipeline$O7F4ENrC3oYj9E0vblCKW9Dec60.INSTANCE, $$Lambda$DoublePipeline$lWQTyY6EPN0Xvhyjp5Lr5ZKBDCA.INSTANCE, $$Lambda$DoublePipeline$8lpXAdS4oGMq6Yo_dNhNdoPgg0.INSTANCE);
            if (avg[2] > 0.0d) {
                return OptionalDouble.of(Collectors.computeFinalSum(avg) / avg[2]);
            }
            return OptionalDouble.empty();
        }

        static /* synthetic */ void lambda$average$5(double[] ll, double d) {
            ll[2] = ll[2] + 1.0d;
            Collectors.sumWithCompensation(ll, d);
            ll[3] = ll[3] + d;
        }

        static /* synthetic */ void lambda$average$6(double[] ll, double[] rr) {
            Collectors.sumWithCompensation(ll, rr[0]);
            Collectors.sumWithCompensation(ll, rr[1]);
            ll[2] = ll[2] + rr[2];
            ll[3] = ll[3] + rr[3];
        }

        static /* synthetic */ long lambda$count$7(double e) {
            return 1;
        }

        public final long count() {
            return mapToLong($$Lambda$DoublePipeline$V2mM4_kocaa0EZ7g04Qc6_Yd13E.INSTANCE).sum();
        }

        public final DoubleSummaryStatistics summaryStatistics() {
            return (DoubleSummaryStatistics) collect($$Lambda$745FUy7cYwYu7KrMQTYh2DNqh1I.INSTANCE, $$Lambda$9clh6DyAY2rGfAxuH1sO9aEBuU.INSTANCE, $$Lambda$BZcmU4lh1MU8ke57orLk6ELdvT4.INSTANCE);
        }

        public final double reduce(double identity, DoubleBinaryOperator op) {
            return ((Double) evaluate(ReduceOps.makeDouble(identity, op))).doubleValue();
        }

        public final OptionalDouble reduce(DoubleBinaryOperator op) {
            return (OptionalDouble) evaluate(ReduceOps.makeDouble(op));
        }

        public final <R> R collect(Supplier<R> supplier, ObjDoubleConsumer<R> accumulator, BiConsumer<R, R> combiner) {
            return evaluate(ReduceOps.makeDouble(supplier, accumulator, new BinaryOperator() {
                public final Object apply(Object obj, Object obj2) {
                    return BiConsumer.this.accept(obj, obj2);
                }
            }));
        }

        public final boolean anyMatch(DoublePredicate predicate) {
            return ((Boolean) evaluate(MatchOps.makeDouble(predicate, MatchOps.MatchKind.ANY))).booleanValue();
        }

        public final boolean allMatch(DoublePredicate predicate) {
            return ((Boolean) evaluate(MatchOps.makeDouble(predicate, MatchOps.MatchKind.ALL))).booleanValue();
        }

        public final boolean noneMatch(DoublePredicate predicate) {
            return ((Boolean) evaluate(MatchOps.makeDouble(predicate, MatchOps.MatchKind.NONE))).booleanValue();
        }

        public final OptionalDouble findFirst() {
            return (OptionalDouble) evaluate(FindOps.makeDouble(true));
        }

        public final OptionalDouble findAny() {
            return (OptionalDouble) evaluate(FindOps.makeDouble(false));
        }

        static /* synthetic */ Double[] lambda$toArray$9(int x$0) {
            return new Double[x$0];
        }

        public final double[] toArray() {
            return (double[]) Nodes.flattenDouble((Node.OfDouble) evaluateToArrayNode($$Lambda$DoublePipeline$VwL6T93St4bY9lzEXgl24N_DcA4.INSTANCE)).asPrimitiveArray();
        }
    }
