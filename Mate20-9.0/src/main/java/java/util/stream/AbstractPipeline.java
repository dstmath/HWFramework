package java.util.stream;

import java.lang.annotation.RCUnownedRef;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.Node;

public abstract class AbstractPipeline<E_IN, E_OUT, S extends BaseStream<E_OUT, S>> extends PipelineHelper<E_OUT> implements BaseStream<E_OUT, S> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final String MSG_CONSUMED = "source already consumed or closed";
    private static final String MSG_STREAM_LINKED = "stream has already been operated upon or closed";
    private int combinedFlags;
    private int depth;
    private boolean linkedOrConsumed;
    @RCUnownedRef
    private AbstractPipeline nextStage;
    private boolean parallel;
    private final AbstractPipeline previousStage;
    private boolean sourceAnyStateful;
    private Runnable sourceCloseAction;
    protected final int sourceOrOpFlags;
    private Spliterator<?> sourceSpliterator;
    @RCUnownedRef
    private final AbstractPipeline sourceStage;
    private Supplier<? extends Spliterator<?>> sourceSupplier;

    public abstract <P_IN> Node<E_OUT> evaluateToNode(PipelineHelper<E_OUT> pipelineHelper, Spliterator<P_IN> spliterator, boolean z, IntFunction<E_OUT[]> intFunction);

    public abstract void forEachWithCancel(Spliterator<E_OUT> spliterator, Sink<E_OUT> sink);

    public abstract StreamShape getOutputShape();

    public abstract Spliterator<E_OUT> lazySpliterator(Supplier<? extends Spliterator<E_OUT>> supplier);

    public abstract Node.Builder<E_OUT> makeNodeBuilder(long j, IntFunction<E_OUT[]> intFunction);

    public abstract boolean opIsStateful();

    public abstract Sink<E_IN> opWrapSink(int i, Sink<E_OUT> sink);

    public abstract <P_IN> Spliterator<E_OUT> wrap(PipelineHelper<E_OUT> pipelineHelper, Supplier<Spliterator<P_IN>> supplier, boolean z);

    AbstractPipeline(Supplier<? extends Spliterator<?>> source, int sourceFlags, boolean parallel2) {
        this.previousStage = null;
        this.sourceSupplier = source;
        this.sourceStage = this;
        this.sourceOrOpFlags = StreamOpFlag.STREAM_MASK & sourceFlags;
        this.combinedFlags = (~(this.sourceOrOpFlags << 1)) & StreamOpFlag.INITIAL_OPS_VALUE;
        this.depth = 0;
        this.parallel = parallel2;
    }

    AbstractPipeline(Spliterator<?> source, int sourceFlags, boolean parallel2) {
        this.previousStage = null;
        this.sourceSpliterator = source;
        this.sourceStage = this;
        this.sourceOrOpFlags = StreamOpFlag.STREAM_MASK & sourceFlags;
        this.combinedFlags = (~(this.sourceOrOpFlags << 1)) & StreamOpFlag.INITIAL_OPS_VALUE;
        this.depth = 0;
        this.parallel = parallel2;
    }

    AbstractPipeline(AbstractPipeline<?, E_IN, ?> previousStage2, int opFlags) {
        if (!previousStage2.linkedOrConsumed) {
            previousStage2.linkedOrConsumed = true;
            previousStage2.nextStage = this;
            this.previousStage = previousStage2;
            this.sourceOrOpFlags = StreamOpFlag.OP_MASK & opFlags;
            this.combinedFlags = StreamOpFlag.combineOpFlags(opFlags, previousStage2.combinedFlags);
            this.sourceStage = previousStage2.sourceStage;
            if (opIsStateful()) {
                this.sourceStage.sourceAnyStateful = true;
            }
            this.depth = previousStage2.depth + 1;
            return;
        }
        throw new IllegalStateException(MSG_STREAM_LINKED);
    }

    /* access modifiers changed from: package-private */
    public final <R> R evaluate(TerminalOp<E_OUT, R> terminalOp) {
        if (!this.linkedOrConsumed) {
            this.linkedOrConsumed = true;
            if (isParallel()) {
                return terminalOp.evaluateParallel(this, sourceSpliterator(terminalOp.getOpFlags()));
            }
            return terminalOp.evaluateSequential(this, sourceSpliterator(terminalOp.getOpFlags()));
        }
        throw new IllegalStateException(MSG_STREAM_LINKED);
    }

    public final Node<E_OUT> evaluateToArrayNode(IntFunction<E_OUT[]> generator) {
        if (!this.linkedOrConsumed) {
            this.linkedOrConsumed = true;
            if (!isParallel() || this.previousStage == null || !opIsStateful()) {
                return evaluate(sourceSpliterator(0), true, generator);
            }
            this.depth = 0;
            return opEvaluateParallel(this.previousStage, this.previousStage.sourceSpliterator(0), generator);
        }
        throw new IllegalStateException(MSG_STREAM_LINKED);
    }

    /* access modifiers changed from: package-private */
    public final Spliterator<E_OUT> sourceStageSpliterator() {
        if (this != this.sourceStage) {
            throw new IllegalStateException();
        } else if (!this.linkedOrConsumed) {
            this.linkedOrConsumed = true;
            if (this.sourceStage.sourceSpliterator != null) {
                Spliterator<?> spliterator = this.sourceStage.sourceSpliterator;
                this.sourceStage.sourceSpliterator = null;
                return spliterator;
            } else if (this.sourceStage.sourceSupplier != null) {
                Spliterator<E_OUT> s = (Spliterator) this.sourceStage.sourceSupplier.get();
                this.sourceStage.sourceSupplier = null;
                return s;
            } else {
                throw new IllegalStateException(MSG_CONSUMED);
            }
        } else {
            throw new IllegalStateException(MSG_STREAM_LINKED);
        }
    }

    public final S sequential() {
        this.sourceStage.parallel = $assertionsDisabled;
        return this;
    }

    public final S parallel() {
        this.sourceStage.parallel = true;
        return this;
    }

    public void close() {
        this.linkedOrConsumed = true;
        this.sourceSupplier = null;
        this.sourceSpliterator = null;
        if (this.sourceStage.sourceCloseAction != null) {
            Runnable closeAction = this.sourceStage.sourceCloseAction;
            this.sourceStage.sourceCloseAction = null;
            closeAction.run();
        }
    }

    public S onClose(Runnable closeHandler) {
        Runnable existingHandler = this.sourceStage.sourceCloseAction;
        this.sourceStage.sourceCloseAction = existingHandler == null ? closeHandler : Streams.composeWithExceptions(existingHandler, closeHandler);
        return this;
    }

    public Spliterator<E_OUT> spliterator() {
        if (!this.linkedOrConsumed) {
            this.linkedOrConsumed = true;
            if (this != this.sourceStage) {
                return wrap(this, new Supplier() {
                    public final Object get() {
                        return AbstractPipeline.this.sourceSpliterator(0);
                    }
                }, isParallel());
            }
            if (this.sourceStage.sourceSpliterator != null) {
                Spliterator<?> spliterator = this.sourceStage.sourceSpliterator;
                this.sourceStage.sourceSpliterator = null;
                return spliterator;
            } else if (this.sourceStage.sourceSupplier != null) {
                Supplier<? extends Spliterator<?>> supplier = this.sourceStage.sourceSupplier;
                this.sourceStage.sourceSupplier = null;
                return lazySpliterator(supplier);
            } else {
                throw new IllegalStateException(MSG_CONSUMED);
            }
        } else {
            throw new IllegalStateException(MSG_STREAM_LINKED);
        }
    }

    public final boolean isParallel() {
        return this.sourceStage.parallel;
    }

    public final int getStreamFlags() {
        return StreamOpFlag.toStreamFlags(this.combinedFlags);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v3, resolved type: java.util.Spliterator<E_OUT>} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v19, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v5, resolved type: java.util.Spliterator<?>} */
    /* access modifiers changed from: private */
    /* JADX WARNING: Multi-variable type inference failed */
    public Spliterator<?> sourceSpliterator(int terminalFlags) {
        Spliterator<?> spliterator;
        int i;
        if (this.sourceStage.sourceSpliterator != null) {
            spliterator = this.sourceStage.sourceSpliterator;
            this.sourceStage.sourceSpliterator = null;
        } else if (this.sourceStage.sourceSupplier != null) {
            spliterator = this.sourceStage.sourceSupplier.get();
            this.sourceStage.sourceSupplier = null;
        } else {
            throw new IllegalStateException(MSG_CONSUMED);
        }
        if (isParallel() && this.sourceStage.sourceAnyStateful) {
            int depth2 = 1;
            AbstractPipeline u = this.sourceStage;
            AbstractPipeline p = this.sourceStage.nextStage;
            Spliterator<?> spliterator2 = spliterator;
            while (u != this) {
                int thisOpFlags = p.sourceOrOpFlags;
                if (p.opIsStateful()) {
                    depth2 = 0;
                    if (StreamOpFlag.SHORT_CIRCUIT.isKnown(thisOpFlags)) {
                        thisOpFlags &= ~StreamOpFlag.IS_SHORT_CIRCUIT;
                    }
                    spliterator2 = p.opEvaluateParallelLazy(u, spliterator2);
                    if (spliterator2.hasCharacteristics(64)) {
                        i = ((~StreamOpFlag.NOT_SIZED) & thisOpFlags) | StreamOpFlag.IS_SIZED;
                    } else {
                        i = ((~StreamOpFlag.IS_SIZED) & thisOpFlags) | StreamOpFlag.NOT_SIZED;
                    }
                    thisOpFlags = i;
                }
                p.depth = depth2;
                p.combinedFlags = StreamOpFlag.combineOpFlags(thisOpFlags, u.combinedFlags);
                u = p;
                p = p.nextStage;
                depth2++;
            }
            spliterator = spliterator2;
        }
        if (terminalFlags != 0) {
            this.combinedFlags = StreamOpFlag.combineOpFlags(terminalFlags, this.combinedFlags);
        }
        return spliterator;
    }

    /* access modifiers changed from: package-private */
    public final StreamShape getSourceShape() {
        AbstractPipeline p = this;
        while (p.depth > 0) {
            p = p.previousStage;
        }
        return p.getOutputShape();
    }

    /* access modifiers changed from: package-private */
    public final <P_IN> long exactOutputSizeIfKnown(Spliterator<P_IN> spliterator) {
        if (StreamOpFlag.SIZED.isKnown(getStreamAndOpFlags())) {
            return spliterator.getExactSizeIfKnown();
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public final <P_IN, S extends Sink<E_OUT>> S wrapAndCopyInto(S sink, Spliterator<P_IN> spliterator) {
        copyInto(wrapSink((Sink) Objects.requireNonNull(sink)), spliterator);
        return sink;
    }

    /* access modifiers changed from: package-private */
    public final <P_IN> void copyInto(Sink<P_IN> wrappedSink, Spliterator<P_IN> spliterator) {
        Objects.requireNonNull(wrappedSink);
        if (!StreamOpFlag.SHORT_CIRCUIT.isKnown(getStreamAndOpFlags())) {
            wrappedSink.begin(spliterator.getExactSizeIfKnown());
            spliterator.forEachRemaining(wrappedSink);
            wrappedSink.end();
            return;
        }
        copyIntoWithCancel(wrappedSink, spliterator);
    }

    /* access modifiers changed from: package-private */
    public final <P_IN> void copyIntoWithCancel(Sink<P_IN> wrappedSink, Spliterator<P_IN> spliterator) {
        AbstractPipeline p = this;
        while (p.depth > 0) {
            p = p.previousStage;
        }
        wrappedSink.begin(spliterator.getExactSizeIfKnown());
        p.forEachWithCancel(spliterator, wrappedSink);
        wrappedSink.end();
    }

    public final int getStreamAndOpFlags() {
        return this.combinedFlags;
    }

    /* access modifiers changed from: package-private */
    public final boolean isOrdered() {
        return StreamOpFlag.ORDERED.isKnown(this.combinedFlags);
    }

    public final <P_IN> Sink<P_IN> wrapSink(Sink<E_OUT> sink) {
        Objects.requireNonNull(sink);
        Sink<E_OUT> sink2 = sink;
        for (AbstractPipeline p = this; p.depth > 0; p = p.previousStage) {
            sink2 = p.opWrapSink(p.previousStage.combinedFlags, sink2);
        }
        return sink2;
    }

    /* access modifiers changed from: package-private */
    public final <P_IN> Spliterator<E_OUT> wrapSpliterator(Spliterator<P_IN> sourceSpliterator2) {
        if (this.depth == 0) {
            return sourceSpliterator2;
        }
        return wrap(this, new Supplier() {
            public final Object get() {
                return AbstractPipeline.lambda$wrapSpliterator$1(Spliterator.this);
            }
        }, isParallel());
    }

    static /* synthetic */ Spliterator lambda$wrapSpliterator$1(Spliterator sourceSpliterator2) {
        return sourceSpliterator2;
    }

    public final <P_IN> Node<E_OUT> evaluate(Spliterator<P_IN> spliterator, boolean flatten, IntFunction<E_OUT[]> generator) {
        if (isParallel()) {
            return evaluateToNode(this, spliterator, flatten, generator);
        }
        return ((Node.Builder) wrapAndCopyInto(makeNodeBuilder(exactOutputSizeIfKnown(spliterator), generator), spliterator)).build();
    }

    public <P_IN> Node<E_OUT> opEvaluateParallel(PipelineHelper<E_OUT> pipelineHelper, Spliterator<P_IN> spliterator, IntFunction<E_OUT[]> intFunction) {
        throw new UnsupportedOperationException("Parallel evaluation is not supported");
    }

    static /* synthetic */ Object[] lambda$opEvaluateParallelLazy$2(int i) {
        return new Object[i];
    }

    public <P_IN> Spliterator<E_OUT> opEvaluateParallelLazy(PipelineHelper<E_OUT> helper, Spliterator<P_IN> spliterator) {
        return opEvaluateParallel(helper, spliterator, $$Lambda$AbstractPipeline$wEsmW74nQaCA9FYTjN7e9qkJaXE.INSTANCE).spliterator();
    }
}
