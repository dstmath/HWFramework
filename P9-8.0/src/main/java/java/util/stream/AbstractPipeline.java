package java.util.stream;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.-$Lambda$63IP-glqnb7EOeWPY4aVbozUwnc.AnonymousClass1;
import java.util.stream.-$Lambda$63IP-glqnb7EOeWPY4aVbozUwnc.AnonymousClass2;
import java.util.stream.Node.Builder;

public abstract class AbstractPipeline<E_IN, E_OUT, S extends BaseStream<E_OUT, S>> extends PipelineHelper<E_OUT> implements BaseStream<E_OUT, S> {
    static final /* synthetic */ boolean -assertionsDisabled = (AbstractPipeline.class.desiredAssertionStatus() ^ 1);
    private static final String MSG_CONSUMED = "source already consumed or closed";
    private static final String MSG_STREAM_LINKED = "stream has already been operated upon or closed";
    private int combinedFlags;
    private int depth;
    private boolean linkedOrConsumed;
    private AbstractPipeline nextStage;
    private boolean parallel;
    private final AbstractPipeline previousStage;
    private boolean sourceAnyStateful;
    private Runnable sourceCloseAction;
    protected final int sourceOrOpFlags;
    private Spliterator<?> sourceSpliterator;
    private final AbstractPipeline sourceStage;
    private Supplier<? extends Spliterator<?>> sourceSupplier;

    public abstract <P_IN> Node<E_OUT> evaluateToNode(PipelineHelper<E_OUT> pipelineHelper, Spliterator<P_IN> spliterator, boolean z, IntFunction<E_OUT[]> intFunction);

    public abstract void forEachWithCancel(Spliterator<E_OUT> spliterator, Sink<E_OUT> sink);

    public abstract StreamShape getOutputShape();

    public abstract Spliterator<E_OUT> lazySpliterator(Supplier<? extends Spliterator<E_OUT>> supplier);

    public abstract Builder<E_OUT> makeNodeBuilder(long j, IntFunction<E_OUT[]> intFunction);

    public abstract boolean opIsStateful();

    public abstract Sink<E_IN> opWrapSink(int i, Sink<E_OUT> sink);

    public abstract <P_IN> Spliterator<E_OUT> wrap(PipelineHelper<E_OUT> pipelineHelper, Supplier<Spliterator<P_IN>> supplier, boolean z);

    AbstractPipeline(Supplier<? extends Spliterator<?>> source, int sourceFlags, boolean parallel) {
        this.previousStage = null;
        this.sourceSupplier = source;
        this.sourceStage = this;
        this.sourceOrOpFlags = StreamOpFlag.STREAM_MASK & sourceFlags;
        this.combinedFlags = (~(this.sourceOrOpFlags << 1)) & StreamOpFlag.INITIAL_OPS_VALUE;
        this.depth = 0;
        this.parallel = parallel;
    }

    AbstractPipeline(Spliterator<?> source, int sourceFlags, boolean parallel) {
        this.previousStage = null;
        this.sourceSpliterator = source;
        this.sourceStage = this;
        this.sourceOrOpFlags = StreamOpFlag.STREAM_MASK & sourceFlags;
        this.combinedFlags = (~(this.sourceOrOpFlags << 1)) & StreamOpFlag.INITIAL_OPS_VALUE;
        this.depth = 0;
        this.parallel = parallel;
    }

    AbstractPipeline(AbstractPipeline<?, E_IN, ?> previousStage, int opFlags) {
        if (previousStage.linkedOrConsumed) {
            throw new IllegalStateException(MSG_STREAM_LINKED);
        }
        previousStage.linkedOrConsumed = true;
        previousStage.nextStage = this;
        this.previousStage = previousStage;
        this.sourceOrOpFlags = StreamOpFlag.OP_MASK & opFlags;
        this.combinedFlags = StreamOpFlag.combineOpFlags(opFlags, previousStage.combinedFlags);
        this.sourceStage = previousStage.sourceStage;
        if (opIsStateful()) {
            this.sourceStage.sourceAnyStateful = true;
        }
        this.depth = previousStage.depth + 1;
    }

    final <R> R evaluate(TerminalOp<E_OUT, R> terminalOp) {
        if (!-assertionsDisabled && getOutputShape() != terminalOp.inputShape()) {
            throw new AssertionError();
        } else if (this.linkedOrConsumed) {
            throw new IllegalStateException(MSG_STREAM_LINKED);
        } else {
            this.linkedOrConsumed = true;
            if (isParallel()) {
                return terminalOp.evaluateParallel(this, sourceSpliterator(terminalOp.getOpFlags()));
            }
            return terminalOp.evaluateSequential(this, sourceSpliterator(terminalOp.getOpFlags()));
        }
    }

    public final Node<E_OUT> evaluateToArrayNode(IntFunction<E_OUT[]> generator) {
        if (this.linkedOrConsumed) {
            throw new IllegalStateException(MSG_STREAM_LINKED);
        }
        this.linkedOrConsumed = true;
        if (!isParallel() || this.previousStage == null || !opIsStateful()) {
            return evaluate(sourceSpliterator(0), true, generator);
        }
        this.depth = 0;
        return opEvaluateParallel(this.previousStage, this.previousStage.sourceSpliterator(0), generator);
    }

    final Spliterator<E_OUT> sourceStageSpliterator() {
        if (this != this.sourceStage) {
            throw new IllegalStateException();
        } else if (this.linkedOrConsumed) {
            throw new IllegalStateException(MSG_STREAM_LINKED);
        } else {
            this.linkedOrConsumed = true;
            Spliterator<E_OUT> s;
            if (this.sourceStage.sourceSpliterator != null) {
                s = this.sourceStage.sourceSpliterator;
                this.sourceStage.sourceSpliterator = null;
                return s;
            } else if (this.sourceStage.sourceSupplier != null) {
                s = (Spliterator) this.sourceStage.sourceSupplier.get();
                this.sourceStage.sourceSupplier = null;
                return s;
            } else {
                throw new IllegalStateException(MSG_CONSUMED);
            }
        }
    }

    public final S sequential() {
        this.sourceStage.parallel = -assertionsDisabled;
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
        AbstractPipeline abstractPipeline = this.sourceStage;
        if (existingHandler != null) {
            closeHandler = Streams.composeWithExceptions(existingHandler, closeHandler);
        }
        abstractPipeline.sourceCloseAction = closeHandler;
        return this;
    }

    public Spliterator<E_OUT> spliterator() {
        if (this.linkedOrConsumed) {
            throw new IllegalStateException(MSG_STREAM_LINKED);
        }
        this.linkedOrConsumed = true;
        if (this != this.sourceStage) {
            return wrap(this, new AnonymousClass1(this), isParallel());
        }
        if (this.sourceStage.sourceSpliterator != null) {
            Spliterator<E_OUT> s = this.sourceStage.sourceSpliterator;
            this.sourceStage.sourceSpliterator = null;
            return s;
        } else if (this.sourceStage.sourceSupplier != null) {
            Supplier<Spliterator<E_OUT>> s2 = this.sourceStage.sourceSupplier;
            this.sourceStage.sourceSupplier = null;
            return lazySpliterator(s2);
        } else {
            throw new IllegalStateException(MSG_CONSUMED);
        }
    }

    /* synthetic */ Spliterator lambda$-java_util_stream_AbstractPipeline_14339() {
        return sourceSpliterator(0);
    }

    public final boolean isParallel() {
        return this.sourceStage.parallel;
    }

    public final int getStreamFlags() {
        return StreamOpFlag.toStreamFlags(this.combinedFlags);
    }

    private Spliterator<?> sourceSpliterator(int terminalFlags) {
        Spliterator<?> spliterator;
        if (this.sourceStage.sourceSpliterator != null) {
            spliterator = this.sourceStage.sourceSpliterator;
            this.sourceStage.sourceSpliterator = null;
        } else if (this.sourceStage.sourceSupplier != null) {
            spliterator = (Spliterator) this.sourceStage.sourceSupplier.get();
            this.sourceStage.sourceSupplier = null;
        } else {
            throw new IllegalStateException(MSG_CONSUMED);
        }
        if (isParallel() && this.sourceStage.sourceAnyStateful) {
            int depth = 1;
            AbstractPipeline u = this.sourceStage;
            AbstractPipeline p = this.sourceStage.nextStage;
            while (u != this) {
                int thisOpFlags = p.sourceOrOpFlags;
                if (p.opIsStateful()) {
                    depth = 0;
                    if (StreamOpFlag.SHORT_CIRCUIT.isKnown(thisOpFlags)) {
                        thisOpFlags &= ~StreamOpFlag.IS_SHORT_CIRCUIT;
                    }
                    spliterator = p.opEvaluateParallelLazy(u, spliterator);
                    if (spliterator.hasCharacteristics(64)) {
                        thisOpFlags = ((~StreamOpFlag.NOT_SIZED) & thisOpFlags) | StreamOpFlag.IS_SIZED;
                    } else {
                        thisOpFlags = ((~StreamOpFlag.IS_SIZED) & thisOpFlags) | StreamOpFlag.NOT_SIZED;
                    }
                }
                int depth2 = depth + 1;
                p.depth = depth;
                p.combinedFlags = StreamOpFlag.combineOpFlags(thisOpFlags, u.combinedFlags);
                u = p;
                p = p.nextStage;
                depth = depth2;
            }
        }
        if (terminalFlags != 0) {
            this.combinedFlags = StreamOpFlag.combineOpFlags(terminalFlags, this.combinedFlags);
        }
        return spliterator;
    }

    final StreamShape getSourceShape() {
        AbstractPipeline p = this;
        while (p.depth > 0) {
            p = p.previousStage;
        }
        return p.getOutputShape();
    }

    final <P_IN> long exactOutputSizeIfKnown(Spliterator<P_IN> spliterator) {
        return StreamOpFlag.SIZED.isKnown(getStreamAndOpFlags()) ? spliterator.getExactSizeIfKnown() : -1;
    }

    final <P_IN, S extends Sink<E_OUT>> S wrapAndCopyInto(S sink, Spliterator<P_IN> spliterator) {
        copyInto(wrapSink((Sink) Objects.requireNonNull(sink)), spliterator);
        return sink;
    }

    final <P_IN> void copyInto(Sink<P_IN> wrappedSink, Spliterator<P_IN> spliterator) {
        Objects.requireNonNull(wrappedSink);
        if (StreamOpFlag.SHORT_CIRCUIT.isKnown(getStreamAndOpFlags())) {
            copyIntoWithCancel(wrappedSink, spliterator);
            return;
        }
        wrappedSink.begin(spliterator.getExactSizeIfKnown());
        spliterator.forEachRemaining(wrappedSink);
        wrappedSink.end();
    }

    final <P_IN> void copyIntoWithCancel(Sink<P_IN> wrappedSink, Spliterator<P_IN> spliterator) {
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

    final boolean isOrdered() {
        return StreamOpFlag.ORDERED.isKnown(this.combinedFlags);
    }

    public final <P_IN> Sink<P_IN> wrapSink(Sink<E_OUT> sink) {
        Objects.requireNonNull(sink);
        for (AbstractPipeline p = this; p.depth > 0; p = p.previousStage) {
            sink = p.opWrapSink(p.previousStage.combinedFlags, sink);
        }
        return sink;
    }

    final <P_IN> Spliterator<E_OUT> wrapSpliterator(Spliterator<P_IN> sourceSpliterator) {
        if (this.depth == 0) {
            return sourceSpliterator;
        }
        return wrap(this, new AnonymousClass2(sourceSpliterator), isParallel());
    }

    static /* synthetic */ Spliterator lambda$-java_util_stream_AbstractPipeline_20439(Spliterator sourceSpliterator) {
        return sourceSpliterator;
    }

    public final <P_IN> Node<E_OUT> evaluate(Spliterator<P_IN> spliterator, boolean flatten, IntFunction<E_OUT[]> generator) {
        if (isParallel()) {
            return evaluateToNode(this, spliterator, flatten, generator);
        }
        return ((Builder) wrapAndCopyInto(makeNodeBuilder(exactOutputSizeIfKnown(spliterator), generator), spliterator)).build();
    }

    public <P_IN> Node<E_OUT> opEvaluateParallel(PipelineHelper<E_OUT> pipelineHelper, Spliterator<P_IN> spliterator, IntFunction<E_OUT[]> intFunction) {
        throw new UnsupportedOperationException("Parallel evaluation is not supported");
    }

    public <P_IN> Spliterator<E_OUT> opEvaluateParallelLazy(PipelineHelper<E_OUT> helper, Spliterator<P_IN> spliterator) {
        return opEvaluateParallel(helper, spliterator, new -$Lambda$63IP-glqnb7EOeWPY4aVbozUwnc()).spliterator();
    }
}
