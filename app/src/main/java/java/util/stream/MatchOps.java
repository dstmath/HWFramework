package java.util.stream;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Sink.OfDouble;
import java.util.stream.Sink.OfInt;
import java.util.stream.Sink.OfLong;

final class MatchOps {

    private static abstract class BooleanTerminalSink<T> implements Sink<T> {
        boolean stop;
        boolean value;

        BooleanTerminalSink(MatchKind matchKind) {
            this.value = !matchKind.shortCircuitResult;
        }

        public boolean getAndClearState() {
            return this.value;
        }

        public boolean cancellationRequested() {
            return this.stop;
        }
    }

    /* renamed from: java.util.stream.MatchOps.1MatchSink */
    class AnonymousClass1MatchSink extends BooleanTerminalSink<T> {
        final /* synthetic */ MatchKind val$matchKind;
        final /* synthetic */ Predicate val$predicate;

        AnonymousClass1MatchSink(MatchKind val$matchKind, Predicate val$predicate) {
            this.val$matchKind = val$matchKind;
            this.val$predicate = val$predicate;
            super(val$matchKind);
        }

        public void accept(T t) {
            if (!this.stop && this.val$predicate.test(t) == this.val$matchKind.stopOnPredicateMatches) {
                this.stop = true;
                this.value = this.val$matchKind.shortCircuitResult;
            }
        }
    }

    /* renamed from: java.util.stream.MatchOps.2MatchSink */
    class AnonymousClass2MatchSink extends BooleanTerminalSink<Integer> implements OfInt {
        final /* synthetic */ MatchKind val$matchKind;
        final /* synthetic */ IntPredicate val$predicate;

        AnonymousClass2MatchSink(MatchKind val$matchKind, IntPredicate val$predicate) {
            this.val$matchKind = val$matchKind;
            this.val$predicate = val$predicate;
            super(val$matchKind);
        }

        public void accept(int t) {
            if (!this.stop && this.val$predicate.test(t) == this.val$matchKind.stopOnPredicateMatches) {
                this.stop = true;
                this.value = this.val$matchKind.shortCircuitResult;
            }
        }
    }

    /* renamed from: java.util.stream.MatchOps.3MatchSink */
    class AnonymousClass3MatchSink extends BooleanTerminalSink<Long> implements OfLong {
        final /* synthetic */ MatchKind val$matchKind;
        final /* synthetic */ LongPredicate val$predicate;

        AnonymousClass3MatchSink(MatchKind val$matchKind, LongPredicate val$predicate) {
            this.val$matchKind = val$matchKind;
            this.val$predicate = val$predicate;
            super(val$matchKind);
        }

        public void accept(long t) {
            if (!this.stop && this.val$predicate.test(t) == this.val$matchKind.stopOnPredicateMatches) {
                this.stop = true;
                this.value = this.val$matchKind.shortCircuitResult;
            }
        }
    }

    /* renamed from: java.util.stream.MatchOps.4MatchSink */
    class AnonymousClass4MatchSink extends BooleanTerminalSink<Double> implements OfDouble {
        final /* synthetic */ MatchKind val$matchKind;
        final /* synthetic */ DoublePredicate val$predicate;

        AnonymousClass4MatchSink(MatchKind val$matchKind, DoublePredicate val$predicate) {
            this.val$matchKind = val$matchKind;
            this.val$predicate = val$predicate;
            super(val$matchKind);
        }

        public void accept(double t) {
            if (!this.stop && this.val$predicate.test(t) == this.val$matchKind.stopOnPredicateMatches) {
                this.stop = true;
                this.value = this.val$matchKind.shortCircuitResult;
            }
        }
    }

    enum MatchKind {
        ;
        
        private final boolean shortCircuitResult;
        private final boolean stopOnPredicateMatches;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.MatchOps.MatchKind.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.stream.MatchOps.MatchKind.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.MatchOps.MatchKind.<clinit>():void");
        }

        private MatchKind(boolean stopOnPredicateMatches, boolean shortCircuitResult) {
            this.stopOnPredicateMatches = stopOnPredicateMatches;
            this.shortCircuitResult = shortCircuitResult;
        }
    }

    private static final class MatchOp<T> implements TerminalOp<T, Boolean> {
        private final StreamShape inputShape;
        final MatchKind matchKind;
        final Supplier<BooleanTerminalSink<T>> sinkSupplier;

        MatchOp(StreamShape shape, MatchKind matchKind, Supplier<BooleanTerminalSink<T>> sinkSupplier) {
            this.inputShape = shape;
            this.matchKind = matchKind;
            this.sinkSupplier = sinkSupplier;
        }

        public int getOpFlags() {
            return StreamOpFlag.IS_SHORT_CIRCUIT | StreamOpFlag.NOT_ORDERED;
        }

        public StreamShape inputShape() {
            return this.inputShape;
        }

        public /* bridge */ /* synthetic */ Object m63evaluateSequential(PipelineHelper helper, Spliterator spliterator) {
            return evaluateSequential(helper, spliterator);
        }

        public <S> Boolean evaluateSequential(PipelineHelper<T> helper, Spliterator<S> spliterator) {
            return Boolean.valueOf(((BooleanTerminalSink) helper.wrapAndCopyInto((BooleanTerminalSink) this.sinkSupplier.get(), spliterator)).getAndClearState());
        }

        public /* bridge */ /* synthetic */ Object m62evaluateParallel(PipelineHelper helper, Spliterator spliterator) {
            return evaluateParallel(helper, spliterator);
        }

        public <S> Boolean evaluateParallel(PipelineHelper<T> helper, Spliterator<S> spliterator) {
            return (Boolean) new MatchTask(this, helper, spliterator).invoke();
        }
    }

    private static final class MatchTask<P_IN, P_OUT> extends AbstractShortCircuitTask<P_IN, P_OUT, Boolean, MatchTask<P_IN, P_OUT>> {
        private final MatchOp<P_OUT> op;

        MatchTask(MatchOp<P_OUT> op, PipelineHelper<P_OUT> helper, Spliterator<P_IN> spliterator) {
            super((PipelineHelper) helper, (Spliterator) spliterator);
            this.op = op;
        }

        MatchTask(MatchTask<P_IN, P_OUT> parent, Spliterator<P_IN> spliterator) {
            super((AbstractShortCircuitTask) parent, (Spliterator) spliterator);
            this.op = parent.op;
        }

        protected /* bridge */ /* synthetic */ AbstractTask makeChild(Spliterator spliterator) {
            return makeChild(spliterator);
        }

        protected MatchTask<P_IN, P_OUT> m66makeChild(Spliterator<P_IN> spliterator) {
            return new MatchTask(this, spliterator);
        }

        protected /* bridge */ /* synthetic */ Object m64doLeaf() {
            return doLeaf();
        }

        protected Boolean doLeaf() {
            boolean b = ((BooleanTerminalSink) this.helper.wrapAndCopyInto((BooleanTerminalSink) this.op.sinkSupplier.get(), this.spliterator)).getAndClearState();
            if (b == this.op.matchKind.shortCircuitResult) {
                shortCircuit(Boolean.valueOf(b));
            }
            return null;
        }

        protected /* bridge */ /* synthetic */ Object m65getEmptyResult() {
            return getEmptyResult();
        }

        protected Boolean getEmptyResult() {
            return Boolean.valueOf(!this.op.matchKind.shortCircuitResult);
        }
    }

    private MatchOps() {
    }

    public static <T> TerminalOp<T, Boolean> makeRef(Predicate<? super T> predicate, MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        return new MatchOp(StreamShape.REFERENCE, matchKind, new MatchOps$-java_util_stream_TerminalOp_makeRef_java_util_function_Predicate_predicate_java_util_stream_MatchOps$MatchKind_matchKind_LambdaImpl0(matchKind, predicate));
    }

    public static TerminalOp<Integer, Boolean> makeInt(IntPredicate predicate, MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        return new MatchOp(StreamShape.INT_VALUE, matchKind, new MatchOps$-java_util_stream_TerminalOp_makeInt_java_util_function_IntPredicate_predicate_java_util_stream_MatchOps$MatchKind_matchKind_LambdaImpl0(matchKind, predicate));
    }

    public static TerminalOp<Long, Boolean> makeLong(LongPredicate predicate, MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        return new MatchOp(StreamShape.LONG_VALUE, matchKind, new MatchOps$-java_util_stream_TerminalOp_makeLong_java_util_function_LongPredicate_predicate_java_util_stream_MatchOps$MatchKind_matchKind_LambdaImpl0(matchKind, predicate));
    }

    public static TerminalOp<Double, Boolean> makeDouble(DoublePredicate predicate, MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        return new MatchOp(StreamShape.DOUBLE_VALUE, matchKind, new MatchOps$-java_util_stream_TerminalOp_makeDouble_java_util_function_DoublePredicate_predicate_java_util_stream_MatchOps$MatchKind_matchKind_LambdaImpl0(matchKind, predicate));
    }
}
