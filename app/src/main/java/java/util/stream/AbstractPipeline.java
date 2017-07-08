package java.util.stream;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Node.Builder;

public abstract class AbstractPipeline<E_IN, E_OUT, S extends BaseStream<E_OUT, S>> extends PipelineHelper<E_OUT> implements BaseStream<E_OUT, S> {
    static final /* synthetic */ boolean -assertionsDisabled = false;
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

    final /* synthetic */ class -java_util_Spliterator_opEvaluateParallelLazy_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl0 implements IntFunction {
        public Object apply(int arg0) {
            return new Object[arg0];
        }
    }

    final /* synthetic */ class -java_util_Spliterator_spliterator__LambdaImpl0 implements Supplier {
        private /* synthetic */ AbstractPipeline val$this;

        public /* synthetic */ -java_util_Spliterator_spliterator__LambdaImpl0(AbstractPipeline abstractPipeline) {
            this.val$this = abstractPipeline;
        }

        public Object get() {
            return this.val$this.-java_util_stream_AbstractPipeline_lambda$1();
        }
    }

    final /* synthetic */ class -java_util_Spliterator_wrapSpliterator_java_util_Spliterator_sourceSpliterator_LambdaImpl0 implements Supplier {
        private /* synthetic */ Spliterator val$sourceSpliterator;

        public /* synthetic */ -java_util_Spliterator_wrapSpliterator_java_util_Spliterator_sourceSpliterator_LambdaImpl0(Spliterator spliterator) {
            this.val$sourceSpliterator = spliterator;
        }

        public Object get() {
            return AbstractPipeline.-java_util_stream_AbstractPipeline_lambda$2(this.val$sourceSpliterator);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.AbstractPipeline.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.stream.AbstractPipeline.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.stream.AbstractPipeline.<clinit>():void");
    }

    AbstractPipeline(java.util.Spliterator<?> r1, int r2, boolean r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.AbstractPipeline.<init>(java.util.Spliterator, int, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.stream.AbstractPipeline.<init>(java.util.Spliterator, int, boolean):void");
    }

    AbstractPipeline(java.util.function.Supplier<? extends java.util.Spliterator<?>> r1, int r2, boolean r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.AbstractPipeline.<init>(java.util.function.Supplier, int, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.stream.AbstractPipeline.<init>(java.util.function.Supplier, int, boolean):void");
    }

    private java.util.Spliterator<?> sourceSpliterator(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.AbstractPipeline.sourceSpliterator(int):java.util.Spliterator<?>
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.stream.AbstractPipeline.sourceSpliterator(int):java.util.Spliterator<?>");
    }

    public abstract <P_IN> Node<E_OUT> evaluateToNode(PipelineHelper<E_OUT> pipelineHelper, Spliterator<P_IN> spliterator, boolean z, IntFunction<E_OUT[]> intFunction);

    public abstract void forEachWithCancel(Spliterator<E_OUT> spliterator, Sink<E_OUT> sink);

    public abstract StreamShape getOutputShape();

    public abstract Spliterator<E_OUT> lazySpliterator(Supplier<? extends Spliterator<E_OUT>> supplier);

    public abstract Builder<E_OUT> makeNodeBuilder(long j, IntFunction<E_OUT[]> intFunction);

    public abstract boolean opIsStateful();

    public abstract Sink<E_IN> opWrapSink(int i, Sink<E_OUT> sink);

    public abstract <P_IN> Spliterator<E_OUT> wrap(PipelineHelper<E_OUT> pipelineHelper, Supplier<Spliterator<P_IN>> supplier, boolean z);

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
        if (!-assertionsDisabled) {
            if (!(getOutputShape() == terminalOp.inputShape() ? true : -assertionsDisabled)) {
                throw new AssertionError();
            }
        }
        if (this.linkedOrConsumed) {
            throw new IllegalStateException(MSG_STREAM_LINKED);
        }
        this.linkedOrConsumed = true;
        if (isParallel()) {
            return terminalOp.evaluateParallel(this, sourceSpliterator(terminalOp.getOpFlags()));
        }
        return terminalOp.evaluateSequential(this, sourceSpliterator(terminalOp.getOpFlags()));
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
            return wrap(this, new -java_util_Spliterator_spliterator__LambdaImpl0(), isParallel());
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

    /* synthetic */ Spliterator -java_util_stream_AbstractPipeline_lambda$1() {
        return sourceSpliterator(0);
    }

    public final boolean isParallel() {
        return this.sourceStage.parallel;
    }

    public final int getStreamFlags() {
        return StreamOpFlag.toStreamFlags(this.combinedFlags);
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
        return wrap(this, new -java_util_Spliterator_wrapSpliterator_java_util_Spliterator_sourceSpliterator_LambdaImpl0(sourceSpliterator), isParallel());
    }

    static /* synthetic */ Spliterator -java_util_stream_AbstractPipeline_lambda$2(Spliterator sourceSpliterator) {
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
        return opEvaluateParallel(helper, spliterator, new -java_util_Spliterator_opEvaluateParallelLazy_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl0()).spliterator();
    }
}
