package java.util.stream;

import java.lang.annotation.RCUnownedThisRef;
import java.util.IntSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.DoublePipeline;
import java.util.stream.LongPipeline;
import java.util.stream.MatchOps;
import java.util.stream.Node;
import java.util.stream.ReferencePipeline;
import java.util.stream.Sink;
import java.util.stream.StreamSpliterators;

public abstract class IntPipeline<E_IN> extends AbstractPipeline<E_IN, Integer, IntStream> implements IntStream {

    public static class Head<E_IN> extends IntPipeline<E_IN> {
        public /* bridge */ /* synthetic */ IntStream parallel() {
            return (IntStream) IntPipeline.super.parallel();
        }

        public /* bridge */ /* synthetic */ IntStream sequential() {
            return (IntStream) IntPipeline.super.sequential();
        }

        public Head(Supplier<? extends Spliterator<Integer>> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        public Head(Spliterator<Integer> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        public final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        public final Sink<E_IN> opWrapSink(int flags, Sink<Integer> sink) {
            throw new UnsupportedOperationException();
        }

        public void forEach(IntConsumer action) {
            if (!isParallel()) {
                IntPipeline.adapt((Spliterator<Integer>) sourceStageSpliterator()).forEachRemaining(action);
            } else {
                IntPipeline.super.forEach(action);
            }
        }

        public void forEachOrdered(IntConsumer action) {
            if (!isParallel()) {
                IntPipeline.adapt((Spliterator<Integer>) sourceStageSpliterator()).forEachRemaining(action);
            } else {
                IntPipeline.super.forEachOrdered(action);
            }
        }
    }

    public static abstract class StatefulOp<E_IN> extends IntPipeline<E_IN> {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        public abstract <P_IN> Node<Integer> opEvaluateParallel(PipelineHelper<Integer> pipelineHelper, Spliterator<P_IN> spliterator, IntFunction<Integer[]> intFunction);

        static {
            Class<IntPipeline> cls = IntPipeline.class;
        }

        public /* bridge */ /* synthetic */ IntStream parallel() {
            return (IntStream) IntPipeline.super.parallel();
        }

        public /* bridge */ /* synthetic */ IntStream sequential() {
            return (IntStream) IntPipeline.super.sequential();
        }

        public StatefulOp(AbstractPipeline<?, E_IN, ?> upstream, StreamShape inputShape, int opFlags) {
            super(upstream, opFlags);
        }

        public final boolean opIsStateful() {
            return true;
        }
    }

    public static abstract class StatelessOp<E_IN> extends IntPipeline<E_IN> {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        static {
            Class<IntPipeline> cls = IntPipeline.class;
        }

        public /* bridge */ /* synthetic */ IntStream parallel() {
            return (IntStream) IntPipeline.super.parallel();
        }

        public /* bridge */ /* synthetic */ IntStream sequential() {
            return (IntStream) IntPipeline.super.sequential();
        }

        public StatelessOp(AbstractPipeline<?, E_IN, ?> upstream, StreamShape inputShape, int opFlags) {
            super(upstream, opFlags);
        }

        public final boolean opIsStateful() {
            return false;
        }
    }

    public /* bridge */ /* synthetic */ IntStream parallel() {
        return (IntStream) super.parallel();
    }

    public /* bridge */ /* synthetic */ IntStream sequential() {
        return (IntStream) super.sequential();
    }

    IntPipeline(Supplier<? extends Spliterator<Integer>> source, int sourceFlags, boolean parallel) {
        super((Supplier<? extends Spliterator<?>>) source, sourceFlags, parallel);
    }

    IntPipeline(Spliterator<Integer> source, int sourceFlags, boolean parallel) {
        super((Spliterator<?>) source, sourceFlags, parallel);
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
        Objects.requireNonNull(sink);
        return new IntConsumer() {
            public final void accept(int i) {
                Sink.this.accept(i);
            }
        };
    }

    /* access modifiers changed from: private */
    public static Spliterator.OfInt adapt(Spliterator<Integer> s) {
        if (s instanceof Spliterator.OfInt) {
            return (Spliterator.OfInt) s;
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
        return new StreamSpliterators.IntWrappingSpliterator(ph, supplier, isParallel);
    }

    public final Spliterator.OfInt lazySpliterator(Supplier<? extends Spliterator<Integer>> supplier) {
        return new StreamSpliterators.DelegatingSpliterator.OfInt(supplier);
    }

    public final void forEachWithCancel(Spliterator<Integer> spliterator, Sink<Integer> sink) {
        Spliterator.OfInt spl = adapt(spliterator);
        IntConsumer adaptedSink = adapt(sink);
        while (!sink.cancellationRequested()) {
            if (!spl.tryAdvance(adaptedSink)) {
                return;
            }
        }
    }

    public final Node.Builder<Integer> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Integer[]> intFunction) {
        return Nodes.intBuilder(exactSizeIfKnown);
    }

    public final PrimitiveIterator.OfInt iterator() {
        return Spliterators.iterator(spliterator());
    }

    public final Spliterator.OfInt spliterator() {
        return adapt((Spliterator<Integer>) super.spliterator());
    }

    public final LongStream asLongStream() {
        return new LongPipeline.StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @RCUnownedThisRef
            public Sink<Integer> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedInt<Long>(sink) {
                    public void accept(int t) {
                        this.downstream.accept((long) t);
                    }
                };
            }
        };
    }

    public final DoubleStream asDoubleStream() {
        return new DoublePipeline.StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @RCUnownedThisRef
            public Sink<Integer> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedInt<Double>(sink) {
                    public void accept(int t) {
                        this.downstream.accept((double) t);
                    }
                };
            }
        };
    }

    public final Stream<Integer> boxed() {
        return mapToObj($$Lambda$wFoizRiPqYBPe0X4aSzbj2iL3g.INSTANCE);
    }

    public final IntStream map(IntUnaryOperator mapper) {
        Objects.requireNonNull(mapper);
        final IntUnaryOperator intUnaryOperator = mapper;
        AnonymousClass3 r0 = new StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @RCUnownedThisRef
            public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedInt<Integer>(sink) {
                    public void accept(int t) {
                        this.downstream.accept(intUnaryOperator.applyAsInt(t));
                    }
                };
            }
        };
        return r0;
    }

    public final <U> Stream<U> mapToObj(IntFunction<? extends U> mapper) {
        Objects.requireNonNull(mapper);
        final IntFunction<? extends U> intFunction = mapper;
        AnonymousClass4 r0 = new ReferencePipeline.StatelessOp<Integer, U>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @RCUnownedThisRef
            public Sink<Integer> opWrapSink(int flags, Sink<U> sink) {
                return new Sink.ChainedInt<U>(sink) {
                    public void accept(int t) {
                        this.downstream.accept(intFunction.apply(t));
                    }
                };
            }
        };
        return r0;
    }

    public final LongStream mapToLong(IntToLongFunction mapper) {
        Objects.requireNonNull(mapper);
        final IntToLongFunction intToLongFunction = mapper;
        AnonymousClass5 r0 = new LongPipeline.StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @RCUnownedThisRef
            public Sink<Integer> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedInt<Long>(sink) {
                    public void accept(int t) {
                        this.downstream.accept(intToLongFunction.applyAsLong(t));
                    }
                };
            }
        };
        return r0;
    }

    public final DoubleStream mapToDouble(IntToDoubleFunction mapper) {
        Objects.requireNonNull(mapper);
        final IntToDoubleFunction intToDoubleFunction = mapper;
        AnonymousClass6 r0 = new DoublePipeline.StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @RCUnownedThisRef
            public Sink<Integer> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedInt<Double>(sink) {
                    public void accept(int t) {
                        this.downstream.accept(intToDoubleFunction.applyAsDouble(t));
                    }
                };
            }
        };
        return r0;
    }

    public final IntStream flatMap(IntFunction<? extends IntStream> mapper) {
        final IntFunction<? extends IntStream> intFunction = mapper;
        AnonymousClass7 r0 = new StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED) {
            @RCUnownedThisRef
            public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedInt<Integer>(sink) {
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
                    public void accept(int t) {
                        IntStream result = (IntStream) intFunction.apply(t);
                        if (result != null) {
                            result.sequential().forEach(
                            /*  JADX ERROR: Method code generation error
                                jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0016: INVOKE  (wrap: java.util.stream.IntStream
                                  0x000d: INVOKE  (r2v1 java.util.stream.IntStream) = (r0v3 'result' java.util.stream.IntStream) java.util.stream.IntStream.sequential():java.util.stream.IntStream type: INTERFACE), (wrap: java.util.stream.-$$Lambda$IntPipeline$7$1$E2wwNE1UnVxs0E9-n47lRWmnJGM
                                  0x0013: CONSTRUCTOR  (r3v1 java.util.stream.-$$Lambda$IntPipeline$7$1$E2wwNE1UnVxs0E9-n47lRWmnJGM) = (r4v0 'this' java.util.stream.IntPipeline$7$1 A[THIS]) java.util.stream.-$$Lambda$IntPipeline$7$1$E2wwNE1UnVxs0E9-n47lRWmnJGM.<init>(java.util.stream.IntPipeline$7$1):void CONSTRUCTOR) java.util.stream.IntStream.forEach(java.util.function.IntConsumer):void type: INTERFACE in method: java.util.stream.IntPipeline.7.1.accept(int):void, dex: boot_classes.dex
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
                                Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0013: CONSTRUCTOR  (r3v1 java.util.stream.-$$Lambda$IntPipeline$7$1$E2wwNE1UnVxs0E9-n47lRWmnJGM) = (r4v0 'this' java.util.stream.IntPipeline$7$1 A[THIS]) java.util.stream.-$$Lambda$IntPipeline$7$1$E2wwNE1UnVxs0E9-n47lRWmnJGM.<init>(java.util.stream.IntPipeline$7$1):void CONSTRUCTOR in method: java.util.stream.IntPipeline.7.1.accept(int):void, dex: boot_classes.dex
                                	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                                	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                	... 53 more
                                Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: java.util.stream.-$$Lambda$IntPipeline$7$1$E2wwNE1UnVxs0E9-n47lRWmnJGM, state: NOT_LOADED
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
                                java.util.stream.IntPipeline$7 r0 = java.util.stream.IntPipeline.AnonymousClass7.this
                                java.util.function.IntFunction r0 = r5
                                java.lang.Object r0 = r0.apply(r5)
                                java.util.stream.IntStream r0 = (java.util.stream.IntStream) r0
                                if (r0 == 0) goto L_0x002f
                                r1 = 0
                                java.util.stream.IntStream r2 = r0.sequential()     // Catch:{ Throwable -> 0x001c }
                                java.util.stream.-$$Lambda$IntPipeline$7$1$E2wwNE1UnVxs0E9-n47lRWmnJGM r3 = new java.util.stream.-$$Lambda$IntPipeline$7$1$E2wwNE1UnVxs0E9-n47lRWmnJGM     // Catch:{ Throwable -> 0x001c }
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
                            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.IntPipeline.AnonymousClass7.AnonymousClass1.accept(int):void");
                        }
                    };
                }
            };
            return r0;
        }

        public IntStream unordered() {
            if (!isOrdered()) {
                return this;
            }
            return new StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_ORDERED) {
                @RCUnownedThisRef
                public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                    return sink;
                }
            };
        }

        public final IntStream filter(IntPredicate predicate) {
            Objects.requireNonNull(predicate);
            final IntPredicate intPredicate = predicate;
            AnonymousClass9 r0 = new StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SIZED) {
                @RCUnownedThisRef
                public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                    return new Sink.ChainedInt<Integer>(sink) {
                        public void begin(long size) {
                            this.downstream.begin(-1);
                        }

                        public void accept(int t) {
                            if (intPredicate.test(t)) {
                                this.downstream.accept(t);
                            }
                        }
                    };
                }
            };
            return r0;
        }

        public final IntStream peek(IntConsumer action) {
            Objects.requireNonNull(action);
            final IntConsumer intConsumer = action;
            AnonymousClass10 r0 = new StatelessOp<Integer>(this, StreamShape.INT_VALUE, 0) {
                @RCUnownedThisRef
                public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                    return new Sink.ChainedInt<Integer>(sink) {
                        public void accept(int t) {
                            intConsumer.accept(t);
                            this.downstream.accept(t);
                        }
                    };
                }
            };
            return r0;
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
            return boxed().distinct().mapToInt($$Lambda$IntPipeline$RE7oGjPWog3HR9X8MdhU1ZGRE.INSTANCE);
        }

        public void forEach(IntConsumer action) {
            evaluate(ForEachOps.makeInt(action, false));
        }

        public void forEachOrdered(IntConsumer action) {
            evaluate(ForEachOps.makeInt(action, true));
        }

        public final int sum() {
            return reduce(0, $$Lambda$ono9Bp0lMrKbIRfAAYdycY0_qag.INSTANCE);
        }

        public final OptionalInt min() {
            return reduce($$Lambda$FZ2W1z3RReutoY2tFnI_NsF0lTk.INSTANCE);
        }

        public final OptionalInt max() {
            return reduce($$Lambda$HJTpjoyUrBGPZyR69XwKllqU1YY.INSTANCE);
        }

        static /* synthetic */ long lambda$count$1(int e) {
            return 1;
        }

        public final long count() {
            return mapToLong($$Lambda$IntPipeline$Q_Wb7uDnZZMCasMbsGNAwSlprMo.INSTANCE).sum();
        }

        static /* synthetic */ long[] lambda$average$2() {
            return new long[2];
        }

        public final OptionalDouble average() {
            long[] avg = (long[]) collect($$Lambda$IntPipeline$MrivqBp4YhHB_ix11jxmkPQ1lbE.INSTANCE, $$Lambda$IntPipeline$0s_rkIyKzlnj_MbqfCTpum_W2c.INSTANCE, $$Lambda$IntPipeline$hMFCZ84F0UujzJhdWtPfESTkN2A.INSTANCE);
            if (avg[0] > 0) {
                return OptionalDouble.of(((double) avg[1]) / ((double) avg[0]));
            }
            return OptionalDouble.empty();
        }

        static /* synthetic */ void lambda$average$3(long[] ll, int i) {
            ll[0] = ll[0] + 1;
            ll[1] = ll[1] + ((long) i);
        }

        static /* synthetic */ void lambda$average$4(long[] ll, long[] rr) {
            ll[0] = ll[0] + rr[0];
            ll[1] = ll[1] + rr[1];
        }

        public final IntSummaryStatistics summaryStatistics() {
            return (IntSummaryStatistics) collect($$Lambda$_Ea_sNpqZAwihIOCRBaP7hHgWWI.INSTANCE, $$Lambda$UowTf7vzuMsu4sv1eMs5iEeNh0.INSTANCE, $$Lambda$YcgMAuDDScc4HC6CSMDq1R0qa40.INSTANCE);
        }

        public final int reduce(int identity, IntBinaryOperator op) {
            return ((Integer) evaluate(ReduceOps.makeInt(identity, op))).intValue();
        }

        public final OptionalInt reduce(IntBinaryOperator op) {
            return (OptionalInt) evaluate(ReduceOps.makeInt(op));
        }

        public final <R> R collect(Supplier<R> supplier, ObjIntConsumer<R> accumulator, BiConsumer<R, R> combiner) {
            return evaluate(ReduceOps.makeInt(supplier, accumulator, new BinaryOperator() {
                public final Object apply(Object obj, Object obj2) {
                    return BiConsumer.this.accept(obj, obj2);
                }
            }));
        }

        public final boolean anyMatch(IntPredicate predicate) {
            return ((Boolean) evaluate(MatchOps.makeInt(predicate, MatchOps.MatchKind.ANY))).booleanValue();
        }

        public final boolean allMatch(IntPredicate predicate) {
            return ((Boolean) evaluate(MatchOps.makeInt(predicate, MatchOps.MatchKind.ALL))).booleanValue();
        }

        public final boolean noneMatch(IntPredicate predicate) {
            return ((Boolean) evaluate(MatchOps.makeInt(predicate, MatchOps.MatchKind.NONE))).booleanValue();
        }

        public final OptionalInt findFirst() {
            return (OptionalInt) evaluate(FindOps.makeInt(true));
        }

        public final OptionalInt findAny() {
            return (OptionalInt) evaluate(FindOps.makeInt(false));
        }

        static /* synthetic */ Integer[] lambda$toArray$6(int x$0) {
            return new Integer[x$0];
        }

        public final int[] toArray() {
            return (int[]) Nodes.flattenInt((Node.OfInt) evaluateToArrayNode($$Lambda$IntPipeline$ozedusDMANE_B8aDthWCd1Lna4.INSTANCE)).asPrimitiveArray();
        }
    }
