package java.util.stream;

import java.util.LongSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
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
import java.util.stream.DoublePipeline;
import java.util.stream.IntPipeline;
import java.util.stream.MatchOps;
import java.util.stream.Node;
import java.util.stream.ReferencePipeline;
import java.util.stream.Sink;
import java.util.stream.StreamSpliterators;

public abstract class LongPipeline<E_IN> extends AbstractPipeline<E_IN, Long, LongStream> implements LongStream {

    public static class Head<E_IN> extends LongPipeline<E_IN> {
        public /* bridge */ /* synthetic */ LongStream parallel() {
            return (LongStream) LongPipeline.super.parallel();
        }

        public /* bridge */ /* synthetic */ LongStream sequential() {
            return (LongStream) LongPipeline.super.sequential();
        }

        public Head(Supplier<? extends Spliterator<Long>> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        public Head(Spliterator<Long> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        public final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        public final Sink<E_IN> opWrapSink(int flags, Sink<Long> sink) {
            throw new UnsupportedOperationException();
        }

        public void forEach(LongConsumer action) {
            if (!isParallel()) {
                LongPipeline.adapt((Spliterator<Long>) sourceStageSpliterator()).forEachRemaining(action);
            } else {
                LongPipeline.super.forEach(action);
            }
        }

        public void forEachOrdered(LongConsumer action) {
            if (!isParallel()) {
                LongPipeline.adapt((Spliterator<Long>) sourceStageSpliterator()).forEachRemaining(action);
            } else {
                LongPipeline.super.forEachOrdered(action);
            }
        }
    }

    public static abstract class StatefulOp<E_IN> extends LongPipeline<E_IN> {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        public abstract <P_IN> Node<Long> opEvaluateParallel(PipelineHelper<Long> pipelineHelper, Spliterator<P_IN> spliterator, IntFunction<Long[]> intFunction);

        static {
            Class<LongPipeline> cls = LongPipeline.class;
        }

        public /* bridge */ /* synthetic */ LongStream parallel() {
            return (LongStream) LongPipeline.super.parallel();
        }

        public /* bridge */ /* synthetic */ LongStream sequential() {
            return (LongStream) LongPipeline.super.sequential();
        }

        public StatefulOp(AbstractPipeline<?, E_IN, ?> upstream, StreamShape inputShape, int opFlags) {
            super(upstream, opFlags);
        }

        public final boolean opIsStateful() {
            return true;
        }
    }

    public static abstract class StatelessOp<E_IN> extends LongPipeline<E_IN> {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        static {
            Class<LongPipeline> cls = LongPipeline.class;
        }

        public /* bridge */ /* synthetic */ LongStream parallel() {
            return (LongStream) LongPipeline.super.parallel();
        }

        public /* bridge */ /* synthetic */ LongStream sequential() {
            return (LongStream) LongPipeline.super.sequential();
        }

        public StatelessOp(AbstractPipeline<?, E_IN, ?> upstream, StreamShape inputShape, int opFlags) {
            super(upstream, opFlags);
        }

        public final boolean opIsStateful() {
            return false;
        }
    }

    public /* bridge */ /* synthetic */ LongStream parallel() {
        return (LongStream) super.parallel();
    }

    public /* bridge */ /* synthetic */ LongStream sequential() {
        return (LongStream) super.sequential();
    }

    LongPipeline(Supplier<? extends Spliterator<Long>> source, int sourceFlags, boolean parallel) {
        super((Supplier<? extends Spliterator<?>>) source, sourceFlags, parallel);
    }

    LongPipeline(Spliterator<Long> source, int sourceFlags, boolean parallel) {
        super((Spliterator<?>) source, sourceFlags, parallel);
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
        Objects.requireNonNull(sink);
        return new LongConsumer() {
            public final void accept(long j) {
                Sink.this.accept(j);
            }
        };
    }

    /* access modifiers changed from: private */
    public static Spliterator.OfLong adapt(Spliterator<Long> s) {
        if (s instanceof Spliterator.OfLong) {
            return (Spliterator.OfLong) s;
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
        return new StreamSpliterators.LongWrappingSpliterator(ph, supplier, isParallel);
    }

    public final Spliterator.OfLong lazySpliterator(Supplier<? extends Spliterator<Long>> supplier) {
        return new StreamSpliterators.DelegatingSpliterator.OfLong(supplier);
    }

    public final void forEachWithCancel(Spliterator<Long> spliterator, Sink<Long> sink) {
        Spliterator.OfLong spl = adapt(spliterator);
        LongConsumer adaptedSink = adapt(sink);
        while (!sink.cancellationRequested()) {
            if (!spl.tryAdvance(adaptedSink)) {
                return;
            }
        }
    }

    public final Node.Builder<Long> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Long[]> intFunction) {
        return Nodes.longBuilder(exactSizeIfKnown);
    }

    public final PrimitiveIterator.OfLong iterator() {
        return Spliterators.iterator(spliterator());
    }

    public final Spliterator.OfLong spliterator() {
        return adapt((Spliterator<Long>) super.spliterator());
    }

    public final DoubleStream asDoubleStream() {
        return new DoublePipeline.StatelessOp<Long>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Long> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedLong<Double>(sink) {
                    public void accept(long t) {
                        this.downstream.accept((double) t);
                    }
                };
            }
        };
    }

    public final Stream<Long> boxed() {
        return mapToObj($$Lambda$w4zz3RuWVbX94KiVllUNB6u_ygA.INSTANCE);
    }

    public final LongStream map(LongUnaryOperator mapper) {
        Objects.requireNonNull(mapper);
        final LongUnaryOperator longUnaryOperator = mapper;
        AnonymousClass2 r0 = new StatelessOp<Long>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedLong<Long>(sink) {
                    public void accept(long t) {
                        this.downstream.accept(longUnaryOperator.applyAsLong(t));
                    }
                };
            }
        };
        return r0;
    }

    public final <U> Stream<U> mapToObj(LongFunction<? extends U> mapper) {
        Objects.requireNonNull(mapper);
        final LongFunction<? extends U> longFunction = mapper;
        AnonymousClass3 r0 = new ReferencePipeline.StatelessOp<Long, U>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Long> opWrapSink(int flags, Sink<U> sink) {
                return new Sink.ChainedLong<U>(sink) {
                    public void accept(long t) {
                        this.downstream.accept(longFunction.apply(t));
                    }
                };
            }
        };
        return r0;
    }

    public final IntStream mapToInt(LongToIntFunction mapper) {
        Objects.requireNonNull(mapper);
        final LongToIntFunction longToIntFunction = mapper;
        AnonymousClass4 r0 = new IntPipeline.StatelessOp<Long>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Long> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedLong<Integer>(sink) {
                    public void accept(long t) {
                        this.downstream.accept(longToIntFunction.applyAsInt(t));
                    }
                };
            }
        };
        return r0;
    }

    public final DoubleStream mapToDouble(LongToDoubleFunction mapper) {
        Objects.requireNonNull(mapper);
        final LongToDoubleFunction longToDoubleFunction = mapper;
        AnonymousClass5 r0 = new DoublePipeline.StatelessOp<Long>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            public Sink<Long> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedLong<Double>(sink) {
                    public void accept(long t) {
                        this.downstream.accept(longToDoubleFunction.applyAsDouble(t));
                    }
                };
            }
        };
        return r0;
    }

    public final LongStream flatMap(LongFunction<? extends LongStream> mapper) {
        final LongFunction<? extends LongStream> longFunction = mapper;
        AnonymousClass6 r0 = new StatelessOp<Long>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED) {
            public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedLong<Long>(sink) {
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
                    public void accept(long t) {
                        LongStream result = (LongStream) longFunction.apply(t);
                        if (result != null) {
                            result.sequential().forEach(
                            /*  JADX ERROR: Method code generation error
                                jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0016: INVOKE  (wrap: java.util.stream.LongStream
                                  0x000d: INVOKE  (r2v1 java.util.stream.LongStream) = (r0v3 'result' java.util.stream.LongStream) java.util.stream.LongStream.sequential():java.util.stream.LongStream type: INTERFACE), (wrap: java.util.stream.-$$Lambda$LongPipeline$6$1$fLvJH_Wq0Kv-MEJSFU3IOaEtvxk
                                  0x0013: CONSTRUCTOR  (r3v1 java.util.stream.-$$Lambda$LongPipeline$6$1$fLvJH_Wq0Kv-MEJSFU3IOaEtvxk) = (r4v0 'this' java.util.stream.LongPipeline$6$1 A[THIS]) java.util.stream.-$$Lambda$LongPipeline$6$1$fLvJH_Wq0Kv-MEJSFU3IOaEtvxk.<init>(java.util.stream.LongPipeline$6$1):void CONSTRUCTOR) java.util.stream.LongStream.forEach(java.util.function.LongConsumer):void type: INTERFACE in method: java.util.stream.LongPipeline.6.1.accept(long):void, dex: boot_classes.dex
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
                                Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0013: CONSTRUCTOR  (r3v1 java.util.stream.-$$Lambda$LongPipeline$6$1$fLvJH_Wq0Kv-MEJSFU3IOaEtvxk) = (r4v0 'this' java.util.stream.LongPipeline$6$1 A[THIS]) java.util.stream.-$$Lambda$LongPipeline$6$1$fLvJH_Wq0Kv-MEJSFU3IOaEtvxk.<init>(java.util.stream.LongPipeline$6$1):void CONSTRUCTOR in method: java.util.stream.LongPipeline.6.1.accept(long):void, dex: boot_classes.dex
                                	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                                	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                                	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                                	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                                	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                                	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                                	... 53 more
                                Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: java.util.stream.-$$Lambda$LongPipeline$6$1$fLvJH_Wq0Kv-MEJSFU3IOaEtvxk, state: NOT_LOADED
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
                                java.util.stream.LongPipeline$6 r0 = java.util.stream.LongPipeline.AnonymousClass6.this
                                java.util.function.LongFunction r0 = r5
                                java.lang.Object r0 = r0.apply(r5)
                                java.util.stream.LongStream r0 = (java.util.stream.LongStream) r0
                                if (r0 == 0) goto L_0x002f
                                r1 = 0
                                java.util.stream.LongStream r2 = r0.sequential()     // Catch:{ Throwable -> 0x001c }
                                java.util.stream.-$$Lambda$LongPipeline$6$1$fLvJH_Wq0Kv-MEJSFU3IOaEtvxk r3 = new java.util.stream.-$$Lambda$LongPipeline$6$1$fLvJH_Wq0Kv-MEJSFU3IOaEtvxk     // Catch:{ Throwable -> 0x001c }
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
                            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.LongPipeline.AnonymousClass6.AnonymousClass1.accept(long):void");
                        }
                    };
                }
            };
            return r0;
        }

        public LongStream unordered() {
            if (!isOrdered()) {
                return this;
            }
            return new StatelessOp<Long>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_ORDERED) {
                public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                    return sink;
                }
            };
        }

        public final LongStream filter(LongPredicate predicate) {
            Objects.requireNonNull(predicate);
            final LongPredicate longPredicate = predicate;
            AnonymousClass8 r0 = new StatelessOp<Long>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SIZED) {
                public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                    return new Sink.ChainedLong<Long>(sink) {
                        public void begin(long size) {
                            this.downstream.begin(-1);
                        }

                        public void accept(long t) {
                            if (longPredicate.test(t)) {
                                this.downstream.accept(t);
                            }
                        }
                    };
                }
            };
            return r0;
        }

        public final LongStream peek(LongConsumer action) {
            Objects.requireNonNull(action);
            final LongConsumer longConsumer = action;
            AnonymousClass9 r0 = new StatelessOp<Long>(this, StreamShape.LONG_VALUE, 0) {
                public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                    return new Sink.ChainedLong<Long>(sink) {
                        public void accept(long t) {
                            longConsumer.accept(t);
                            this.downstream.accept(t);
                        }
                    };
                }
            };
            return r0;
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
            return boxed().distinct().mapToLong($$Lambda$LongPipeline$doop4YO9hzEFGaLnLB3xKA404M4.INSTANCE);
        }

        public void forEach(LongConsumer action) {
            evaluate(ForEachOps.makeLong(action, false));
        }

        public void forEachOrdered(LongConsumer action) {
            evaluate(ForEachOps.makeLong(action, true));
        }

        public final long sum() {
            return reduce(0, $$Lambda$dplkPhACWDPIy18ogwdupEQaN40.INSTANCE);
        }

        public final OptionalLong min() {
            return reduce($$Lambda$OExyAlU04fvFLvnsXWOUeFS6K6Y.INSTANCE);
        }

        public final OptionalLong max() {
            return reduce($$Lambda$6eeAyFpmvaed9kw3uuEs0ErN7sg.INSTANCE);
        }

        static /* synthetic */ long[] lambda$average$1() {
            return new long[2];
        }

        public final OptionalDouble average() {
            long[] avg = (long[]) collect($$Lambda$LongPipeline$C2qxkG7ctBwIL2ufjYSA46AbOM.INSTANCE, $$Lambda$LongPipeline$sfTgyfHS4klE7h4z5MNXsSIFcQ.INSTANCE, $$Lambda$LongPipeline$unkecqyY0oPqnMvfYdq_wAGb9pY.INSTANCE);
            if (avg[0] > 0) {
                return OptionalDouble.of(((double) avg[1]) / ((double) avg[0]));
            }
            return OptionalDouble.empty();
        }

        static /* synthetic */ void lambda$average$2(long[] ll, long i) {
            ll[0] = ll[0] + 1;
            ll[1] = ll[1] + i;
        }

        static /* synthetic */ void lambda$average$3(long[] ll, long[] rr) {
            ll[0] = ll[0] + rr[0];
            ll[1] = ll[1] + rr[1];
        }

        static /* synthetic */ long lambda$count$4(long e) {
            return 1;
        }

        public final long count() {
            return map($$Lambda$LongPipeline$HjmjwoQcQfPYnTF2E4GrQONBjyM.INSTANCE).sum();
        }

        public final LongSummaryStatistics summaryStatistics() {
            return (LongSummaryStatistics) collect($$Lambda$kZuTETptiPwvB1J27Na7j760aLU.INSTANCE, $$Lambda$Y_fORtDI6zkwP_Z_VGSwO2GcnS0.INSTANCE, $$Lambda$JNjUhnscc8mcsjlQNaAi4qIfRDQ.INSTANCE);
        }

        public final long reduce(long identity, LongBinaryOperator op) {
            return ((Long) evaluate(ReduceOps.makeLong(identity, op))).longValue();
        }

        public final OptionalLong reduce(LongBinaryOperator op) {
            return (OptionalLong) evaluate(ReduceOps.makeLong(op));
        }

        public final <R> R collect(Supplier<R> supplier, ObjLongConsumer<R> accumulator, BiConsumer<R, R> combiner) {
            return evaluate(ReduceOps.makeLong(supplier, accumulator, new BinaryOperator() {
                public final Object apply(Object obj, Object obj2) {
                    return BiConsumer.this.accept(obj, obj2);
                }
            }));
        }

        public final boolean anyMatch(LongPredicate predicate) {
            return ((Boolean) evaluate(MatchOps.makeLong(predicate, MatchOps.MatchKind.ANY))).booleanValue();
        }

        public final boolean allMatch(LongPredicate predicate) {
            return ((Boolean) evaluate(MatchOps.makeLong(predicate, MatchOps.MatchKind.ALL))).booleanValue();
        }

        public final boolean noneMatch(LongPredicate predicate) {
            return ((Boolean) evaluate(MatchOps.makeLong(predicate, MatchOps.MatchKind.NONE))).booleanValue();
        }

        public final OptionalLong findFirst() {
            return (OptionalLong) evaluate(FindOps.makeLong(true));
        }

        public final OptionalLong findAny() {
            return (OptionalLong) evaluate(FindOps.makeLong(false));
        }

        static /* synthetic */ Long[] lambda$toArray$6(int x$0) {
            return new Long[x$0];
        }

        public final long[] toArray() {
            return (long[]) Nodes.flattenLong((Node.OfLong) evaluateToArrayNode($$Lambda$LongPipeline$LTFlNC6dzl63DE63FJGCsG7H_c.INSTANCE)).asPrimitiveArray();
        }
    }
