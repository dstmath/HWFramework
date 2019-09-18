package java.util.stream;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.MatchOps;
import java.util.stream.Sink;

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

    enum MatchKind {
        ANY(true, true),
        ALL(false, false),
        NONE(true, false);
        
        /* access modifiers changed from: private */
        public final boolean shortCircuitResult;
        /* access modifiers changed from: private */
        public final boolean stopOnPredicateMatches;

        private MatchKind(boolean stopOnPredicateMatches2, boolean shortCircuitResult2) {
            this.stopOnPredicateMatches = stopOnPredicateMatches2;
            this.shortCircuitResult = shortCircuitResult2;
        }
    }

    private static final class MatchOp<T> implements TerminalOp<T, Boolean> {
        private final StreamShape inputShape;
        final MatchKind matchKind;
        final Supplier<BooleanTerminalSink<T>> sinkSupplier;

        MatchOp(StreamShape shape, MatchKind matchKind2, Supplier<BooleanTerminalSink<T>> sinkSupplier2) {
            this.inputShape = shape;
            this.matchKind = matchKind2;
            this.sinkSupplier = sinkSupplier2;
        }

        public int getOpFlags() {
            return StreamOpFlag.IS_SHORT_CIRCUIT | StreamOpFlag.NOT_ORDERED;
        }

        public StreamShape inputShape() {
            return this.inputShape;
        }

        public <S> Boolean evaluateSequential(PipelineHelper<T> helper, Spliterator<S> spliterator) {
            return Boolean.valueOf(((BooleanTerminalSink) helper.wrapAndCopyInto(this.sinkSupplier.get(), spliterator)).getAndClearState());
        }

        public <S> Boolean evaluateParallel(PipelineHelper<T> helper, Spliterator<S> spliterator) {
            return (Boolean) new MatchTask(this, helper, spliterator).invoke();
        }
    }

    private static final class MatchTask<P_IN, P_OUT> extends AbstractShortCircuitTask<P_IN, P_OUT, Boolean, MatchTask<P_IN, P_OUT>> {
        private final MatchOp<P_OUT> op;

        MatchTask(MatchOp<P_OUT> op2, PipelineHelper<P_OUT> helper, Spliterator<P_IN> spliterator) {
            super(helper, spliterator);
            this.op = op2;
        }

        MatchTask(MatchTask<P_IN, P_OUT> parent, Spliterator<P_IN> spliterator) {
            super(parent, spliterator);
            this.op = parent.op;
        }

        /* access modifiers changed from: protected */
        public MatchTask<P_IN, P_OUT> makeChild(Spliterator<P_IN> spliterator) {
            return new MatchTask<>(this, spliterator);
        }

        /* access modifiers changed from: protected */
        public Boolean doLeaf() {
            boolean b = ((BooleanTerminalSink) this.helper.wrapAndCopyInto(this.op.sinkSupplier.get(), this.spliterator)).getAndClearState();
            if (b == this.op.matchKind.shortCircuitResult) {
                shortCircuit(Boolean.valueOf(b));
            }
            return null;
        }

        /* access modifiers changed from: protected */
        public Boolean getEmptyResult() {
            return Boolean.valueOf(!this.op.matchKind.shortCircuitResult);
        }
    }

    private MatchOps() {
    }

    public static <T> TerminalOp<T, Boolean> makeRef(Predicate<? super T> predicate, MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        return new MatchOp(StreamShape.REFERENCE, matchKind, new Supplier(predicate) {
            private final /* synthetic */ Predicate f$1;

            {
                this.f$1 = r2;
            }

            public final Object get() {
                return MatchOps.lambda$makeRef$0(MatchOps.MatchKind.this, this.f$1);
            }
        });
    }

    static /* synthetic */ BooleanTerminalSink lambda$makeRef$0(MatchKind matchKind, Predicate predicate) {
        return new BooleanTerminalSink<T>(predicate) {
            final /* synthetic */ Predicate val$predicate;

            {
                this.val$predicate = r2;
            }

            public void accept(T t) {
                if (!this.stop && this.val$predicate.test(t) == MatchKind.this.stopOnPredicateMatches) {
                    this.stop = true;
                    this.value = MatchKind.this.shortCircuitResult;
                }
            }
        };
    }

    public static TerminalOp<Integer, Boolean> makeInt(IntPredicate predicate, MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        return new MatchOp(StreamShape.INT_VALUE, matchKind, new Supplier(predicate) {
            private final /* synthetic */ IntPredicate f$1;

            {
                this.f$1 = r2;
            }

            public final Object get() {
                return MatchOps.lambda$makeInt$1(MatchOps.MatchKind.this, this.f$1);
            }
        });
    }

    static /* synthetic */ BooleanTerminalSink lambda$makeInt$1(MatchKind matchKind, IntPredicate predicate) {
        return new Sink.OfInt(predicate) {
            final /* synthetic */ IntPredicate val$predicate;

            {
                this.val$predicate = r2;
            }

            public void accept(int t) {
                if (!this.stop && this.val$predicate.test(t) == MatchKind.this.stopOnPredicateMatches) {
                    this.stop = true;
                    this.value = MatchKind.this.shortCircuitResult;
                }
            }
        };
    }

    public static TerminalOp<Long, Boolean> makeLong(LongPredicate predicate, MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        return new MatchOp(StreamShape.LONG_VALUE, matchKind, new Supplier(predicate) {
            private final /* synthetic */ LongPredicate f$1;

            {
                this.f$1 = r2;
            }

            public final Object get() {
                return MatchOps.lambda$makeLong$2(MatchOps.MatchKind.this, this.f$1);
            }
        });
    }

    static /* synthetic */ BooleanTerminalSink lambda$makeLong$2(MatchKind matchKind, LongPredicate predicate) {
        return new Sink.OfLong(predicate) {
            final /* synthetic */ LongPredicate val$predicate;

            {
                this.val$predicate = r2;
            }

            public void accept(long t) {
                if (!this.stop && this.val$predicate.test(t) == MatchKind.this.stopOnPredicateMatches) {
                    this.stop = true;
                    this.value = MatchKind.this.shortCircuitResult;
                }
            }
        };
    }

    public static TerminalOp<Double, Boolean> makeDouble(DoublePredicate predicate, MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        return new MatchOp(StreamShape.DOUBLE_VALUE, matchKind, new Supplier(predicate) {
            private final /* synthetic */ DoublePredicate f$1;

            {
                this.f$1 = r2;
            }

            public final Object get() {
                return MatchOps.lambda$makeDouble$3(MatchOps.MatchKind.this, this.f$1);
            }
        });
    }

    static /* synthetic */ BooleanTerminalSink lambda$makeDouble$3(MatchKind matchKind, DoublePredicate predicate) {
        return new Sink.OfDouble(predicate) {
            final /* synthetic */ DoublePredicate val$predicate;

            {
                this.val$predicate = r2;
            }

            public void accept(double t) {
                if (!this.stop && this.val$predicate.test(t) == MatchKind.this.stopOnPredicateMatches) {
                    this.stop = true;
                    this.value = MatchKind.this.shortCircuitResult;
                }
            }
        };
    }
}
