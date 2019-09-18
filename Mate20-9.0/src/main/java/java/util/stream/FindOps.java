package java.util.stream;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Spliterator;
import java.util.concurrent.CountedCompleter;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Sink;

final class FindOps {

    private static final class FindOp<T, O> implements TerminalOp<T, O> {
        final O emptyValue;
        final boolean mustFindFirst;
        final Predicate<O> presentPredicate;
        private final StreamShape shape;
        final Supplier<TerminalSink<T, O>> sinkSupplier;

        FindOp(boolean mustFindFirst2, StreamShape shape2, O emptyValue2, Predicate<O> presentPredicate2, Supplier<TerminalSink<T, O>> sinkSupplier2) {
            this.mustFindFirst = mustFindFirst2;
            this.shape = shape2;
            this.emptyValue = emptyValue2;
            this.presentPredicate = presentPredicate2;
            this.sinkSupplier = sinkSupplier2;
        }

        public int getOpFlags() {
            return StreamOpFlag.IS_SHORT_CIRCUIT | (this.mustFindFirst ? 0 : StreamOpFlag.NOT_ORDERED);
        }

        public StreamShape inputShape() {
            return this.shape;
        }

        public <S> O evaluateSequential(PipelineHelper<T> helper, Spliterator<S> spliterator) {
            O result = ((TerminalSink) helper.wrapAndCopyInto(this.sinkSupplier.get(), spliterator)).get();
            return result != null ? result : this.emptyValue;
        }

        public <P_IN> O evaluateParallel(PipelineHelper<T> helper, Spliterator<P_IN> spliterator) {
            return new FindTask(this, helper, spliterator).invoke();
        }
    }

    private static abstract class FindSink<T, O> implements TerminalSink<T, O> {
        boolean hasValue;
        T value;

        static final class OfDouble extends FindSink<Double, OptionalDouble> implements Sink.OfDouble {
            OfDouble() {
            }

            public /* bridge */ /* synthetic */ void accept(Double d) {
                super.accept(d);
            }

            public void accept(double value) {
                accept(Double.valueOf(value));
            }

            public OptionalDouble get() {
                if (this.hasValue) {
                    return OptionalDouble.of(((Double) this.value).doubleValue());
                }
                return null;
            }
        }

        static final class OfInt extends FindSink<Integer, OptionalInt> implements Sink.OfInt {
            OfInt() {
            }

            public /* bridge */ /* synthetic */ void accept(Integer num) {
                super.accept(num);
            }

            public void accept(int value) {
                accept(Integer.valueOf(value));
            }

            public OptionalInt get() {
                if (this.hasValue) {
                    return OptionalInt.of(((Integer) this.value).intValue());
                }
                return null;
            }
        }

        static final class OfLong extends FindSink<Long, OptionalLong> implements Sink.OfLong {
            OfLong() {
            }

            public /* bridge */ /* synthetic */ void accept(Long l) {
                super.accept(l);
            }

            public void accept(long value) {
                accept(Long.valueOf(value));
            }

            public OptionalLong get() {
                if (this.hasValue) {
                    return OptionalLong.of(((Long) this.value).longValue());
                }
                return null;
            }
        }

        static final class OfRef<T> extends FindSink<T, Optional<T>> {
            OfRef() {
            }

            public Optional<T> get() {
                if (this.hasValue) {
                    return Optional.of(this.value);
                }
                return null;
            }
        }

        FindSink() {
        }

        public void accept(T value2) {
            if (!this.hasValue) {
                this.hasValue = true;
                this.value = value2;
            }
        }

        public boolean cancellationRequested() {
            return this.hasValue;
        }
    }

    private static final class FindTask<P_IN, P_OUT, O> extends AbstractShortCircuitTask<P_IN, P_OUT, O, FindTask<P_IN, P_OUT, O>> {
        private final FindOp<P_OUT, O> op;

        FindTask(FindOp<P_OUT, O> op2, PipelineHelper<P_OUT> helper, Spliterator<P_IN> spliterator) {
            super(helper, spliterator);
            this.op = op2;
        }

        FindTask(FindTask<P_IN, P_OUT, O> parent, Spliterator<P_IN> spliterator) {
            super(parent, spliterator);
            this.op = parent.op;
        }

        /* access modifiers changed from: protected */
        public FindTask<P_IN, P_OUT, O> makeChild(Spliterator<P_IN> spliterator) {
            return new FindTask<>(this, spliterator);
        }

        /* access modifiers changed from: protected */
        public O getEmptyResult() {
            return this.op.emptyValue;
        }

        private void foundResult(O answer) {
            if (isLeftmostNode()) {
                shortCircuit(answer);
            } else {
                cancelLaterNodes();
            }
        }

        /* access modifiers changed from: protected */
        public O doLeaf() {
            O result = ((TerminalSink) this.helper.wrapAndCopyInto(this.op.sinkSupplier.get(), this.spliterator)).get();
            if (!this.op.mustFindFirst) {
                if (result != null) {
                    shortCircuit(result);
                }
                return null;
            } else if (result == null) {
                return null;
            } else {
                foundResult(result);
                return result;
            }
        }

        /* JADX WARNING: type inference failed for: r2v1, types: [java.util.stream.AbstractTask] */
        /* JADX WARNING: Multi-variable type inference failed */
        public void onCompletion(CountedCompleter<?> caller) {
            if (this.op.mustFindFirst) {
                FindTask<P_IN, P_OUT, O> child = (FindTask) this.leftChild;
                FindTask<P_IN, P_OUT, O> p = null;
                while (true) {
                    if (child == p) {
                        break;
                    }
                    O result = child.getLocalResult();
                    if (result != null && this.op.presentPredicate.test(result)) {
                        setLocalResult(result);
                        foundResult(result);
                        break;
                    }
                    p = child;
                    child = this.rightChild;
                }
            }
            super.onCompletion(caller);
        }
    }

    private FindOps() {
    }

    public static <T> TerminalOp<T, Optional<T>> makeRef(boolean mustFindFirst) {
        FindOp findOp = new FindOp(mustFindFirst, StreamShape.REFERENCE, Optional.empty(), $$Lambda$bjSXRjZ5UYwAzkWXPKwqbJ9BRQ.INSTANCE, $$Lambda$opQ7JxjVCJzqzgTxGU3LVtqC7is.INSTANCE);
        return findOp;
    }

    public static TerminalOp<Integer, OptionalInt> makeInt(boolean mustFindFirst) {
        FindOp findOp = new FindOp(mustFindFirst, StreamShape.INT_VALUE, OptionalInt.empty(), $$Lambda$timJ2_RnT5GwsTSax4Q0EMpi4pc.INSTANCE, $$Lambda$mpgi0fNdNmnu9LkjGowG335UgGc.INSTANCE);
        return findOp;
    }

    public static TerminalOp<Long, OptionalLong> makeLong(boolean mustFindFirst) {
        FindOp findOp = new FindOp(mustFindFirst, StreamShape.LONG_VALUE, OptionalLong.empty(), $$Lambda$XcCQq8gYss3OrVBeBIbyvBZpOz8.INSTANCE, $$Lambda$YpedFjT304pmSbvYSkjP1adjrAo.INSTANCE);
        return findOp;
    }

    public static TerminalOp<Double, OptionalDouble> makeDouble(boolean mustFindFirst) {
        FindOp findOp = new FindOp(mustFindFirst, StreamShape.DOUBLE_VALUE, OptionalDouble.empty(), $$Lambda$yrGzfUbU_IPNM4mz8V8FlMUHCw4.INSTANCE, $$Lambda$l1vHMFuOMPAI8WfDQT6zNBh_B7U.INSTANCE);
        return findOp;
    }
}
