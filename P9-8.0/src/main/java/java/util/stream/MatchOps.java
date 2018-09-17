package java.util.stream;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.-$Lambda$6Ha_z5NODYn_Qs3Vg00tRqQ1oBM.AnonymousClass1;
import java.util.stream.-$Lambda$6Ha_z5NODYn_Qs3Vg00tRqQ1oBM.AnonymousClass2;
import java.util.stream.-$Lambda$6Ha_z5NODYn_Qs3Vg00tRqQ1oBM.AnonymousClass3;
import java.util.stream.Sink.OfDouble;
import java.util.stream.Sink.OfInt;
import java.util.stream.Sink.OfLong;

final class MatchOps {

    private static abstract class BooleanTerminalSink<T> implements Sink<T> {
        boolean stop;
        boolean value;

        BooleanTerminalSink(MatchKind matchKind) {
            this.value = matchKind.shortCircuitResult ^ 1;
        }

        public boolean getAndClearState() {
            return this.value;
        }

        public boolean cancellationRequested() {
            return this.stop;
        }
    }

    /* renamed from: java.util.stream.MatchOps$1MatchSink */
    class AnonymousClass1MatchSink extends BooleanTerminalSink<T> {
        final /* synthetic */ MatchKind val$matchKind;
        final /* synthetic */ Predicate val$predicate;

        AnonymousClass1MatchSink(MatchKind matchKind, Predicate predicate) {
            this.val$matchKind = matchKind;
            this.val$predicate = predicate;
            super(matchKind);
        }

        public void accept(T t) {
            if (!this.stop && this.val$predicate.test(t) == this.val$matchKind.stopOnPredicateMatches) {
                this.stop = true;
                this.value = this.val$matchKind.shortCircuitResult;
            }
        }
    }

    /* renamed from: java.util.stream.MatchOps$2MatchSink */
    class AnonymousClass2MatchSink extends BooleanTerminalSink<Integer> implements OfInt {
        final /* synthetic */ MatchKind val$matchKind;
        final /* synthetic */ IntPredicate val$predicate;

        AnonymousClass2MatchSink(MatchKind matchKind, IntPredicate intPredicate) {
            this.val$matchKind = matchKind;
            this.val$predicate = intPredicate;
            super(matchKind);
        }

        public void accept(int t) {
            if (!this.stop && this.val$predicate.test(t) == this.val$matchKind.stopOnPredicateMatches) {
                this.stop = true;
                this.value = this.val$matchKind.shortCircuitResult;
            }
        }
    }

    /* renamed from: java.util.stream.MatchOps$3MatchSink */
    class AnonymousClass3MatchSink extends BooleanTerminalSink<Long> implements OfLong {
        final /* synthetic */ MatchKind val$matchKind;
        final /* synthetic */ LongPredicate val$predicate;

        AnonymousClass3MatchSink(MatchKind matchKind, LongPredicate longPredicate) {
            this.val$matchKind = matchKind;
            this.val$predicate = longPredicate;
            super(matchKind);
        }

        public void accept(long t) {
            if (!this.stop && this.val$predicate.test(t) == this.val$matchKind.stopOnPredicateMatches) {
                this.stop = true;
                this.value = this.val$matchKind.shortCircuitResult;
            }
        }
    }

    /* renamed from: java.util.stream.MatchOps$4MatchSink */
    class AnonymousClass4MatchSink extends BooleanTerminalSink<Double> implements OfDouble {
        final /* synthetic */ MatchKind val$matchKind;
        final /* synthetic */ DoublePredicate val$predicate;

        AnonymousClass4MatchSink(MatchKind matchKind, DoublePredicate doublePredicate) {
            this.val$matchKind = matchKind;
            this.val$predicate = doublePredicate;
            super(matchKind);
        }

        public void accept(double t) {
            if (!this.stop && this.val$predicate.test(t) == this.val$matchKind.stopOnPredicateMatches) {
                this.stop = true;
                this.value = this.val$matchKind.shortCircuitResult;
            }
        }
    }

    enum MatchKind {
        ANY(true, true),
        ALL(false, false),
        NONE(true, false);
        
        private final boolean shortCircuitResult;
        private final boolean stopOnPredicateMatches;

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

        public <S> Boolean evaluateSequential(PipelineHelper<T> helper, Spliterator<S> spliterator) {
            return Boolean.valueOf(((BooleanTerminalSink) helper.wrapAndCopyInto((BooleanTerminalSink) this.sinkSupplier.lambda$-java_util_stream_Collectors_49198(), spliterator)).getAndClearState());
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

        protected MatchTask<P_IN, P_OUT> makeChild(Spliterator<P_IN> spliterator) {
            return new MatchTask(this, spliterator);
        }

        protected Boolean doLeaf() {
            boolean b = ((BooleanTerminalSink) this.helper.wrapAndCopyInto((BooleanTerminalSink) this.op.sinkSupplier.lambda$-java_util_stream_Collectors_49198(), this.spliterator)).getAndClearState();
            if (b == this.op.matchKind.shortCircuitResult) {
                shortCircuit(Boolean.valueOf(b));
            }
            return null;
        }

        protected Boolean getEmptyResult() {
            return Boolean.valueOf(this.op.matchKind.shortCircuitResult ^ 1);
        }
    }

    private MatchOps() {
    }

    public static <T> TerminalOp<T, Boolean> makeRef(Predicate<? super T> predicate, MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        return new MatchOp(StreamShape.REFERENCE, matchKind, new AnonymousClass3(matchKind, predicate));
    }

    public static TerminalOp<Integer, Boolean> makeInt(IntPredicate predicate, MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        return new MatchOp(StreamShape.INT_VALUE, matchKind, new AnonymousClass1(matchKind, predicate));
    }

    public static TerminalOp<Long, Boolean> makeLong(LongPredicate predicate, MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        return new MatchOp(StreamShape.LONG_VALUE, matchKind, new AnonymousClass2(matchKind, predicate));
    }

    public static TerminalOp<Double, Boolean> makeDouble(DoublePredicate predicate, MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        return new MatchOp(StreamShape.DOUBLE_VALUE, matchKind, new -$Lambda$6Ha_z5NODYn_Qs3Vg00tRqQ1oBM(matchKind, predicate));
    }
}
