package java.util.stream;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Spliterator;
import java.util.concurrent.CountedCompleter;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class FindOps {

    final /* synthetic */ class -java_util_stream_TerminalOp_makeDouble_boolean_mustFindFirst_LambdaImpl0 implements Predicate {
        public boolean test(Object arg0) {
            return ((OptionalDouble) arg0).isPresent();
        }
    }

    final /* synthetic */ class -java_util_stream_TerminalOp_makeDouble_boolean_mustFindFirst_LambdaImpl1 implements Supplier {
        public Object get() {
            return new OfDouble();
        }
    }

    final /* synthetic */ class -java_util_stream_TerminalOp_makeInt_boolean_mustFindFirst_LambdaImpl0 implements Predicate {
        public boolean test(Object arg0) {
            return ((OptionalInt) arg0).isPresent();
        }
    }

    final /* synthetic */ class -java_util_stream_TerminalOp_makeInt_boolean_mustFindFirst_LambdaImpl1 implements Supplier {
        public Object get() {
            return new OfInt();
        }
    }

    final /* synthetic */ class -java_util_stream_TerminalOp_makeLong_boolean_mustFindFirst_LambdaImpl0 implements Predicate {
        public boolean test(Object arg0) {
            return ((OptionalLong) arg0).isPresent();
        }
    }

    final /* synthetic */ class -java_util_stream_TerminalOp_makeLong_boolean_mustFindFirst_LambdaImpl1 implements Supplier {
        public Object get() {
            return new OfLong();
        }
    }

    final /* synthetic */ class -java_util_stream_TerminalOp_makeRef_boolean_mustFindFirst_LambdaImpl0 implements Predicate {
        public boolean test(Object arg0) {
            return ((Optional) arg0).isPresent();
        }
    }

    final /* synthetic */ class -java_util_stream_TerminalOp_makeRef_boolean_mustFindFirst_LambdaImpl1 implements Supplier {
        public Object get() {
            return new OfRef();
        }
    }

    private static final class FindOp<T, O> implements TerminalOp<T, O> {
        final O emptyValue;
        final boolean mustFindFirst;
        final Predicate<O> presentPredicate;
        private final StreamShape shape;
        final Supplier<TerminalSink<T, O>> sinkSupplier;

        FindOp(boolean mustFindFirst, StreamShape shape, O emptyValue, Predicate<O> presentPredicate, Supplier<TerminalSink<T, O>> sinkSupplier) {
            this.mustFindFirst = mustFindFirst;
            this.shape = shape;
            this.emptyValue = emptyValue;
            this.presentPredicate = presentPredicate;
            this.sinkSupplier = sinkSupplier;
        }

        public int getOpFlags() {
            return (this.mustFindFirst ? 0 : StreamOpFlag.NOT_ORDERED) | StreamOpFlag.IS_SHORT_CIRCUIT;
        }

        public StreamShape inputShape() {
            return this.shape;
        }

        public <S> O evaluateSequential(PipelineHelper<T> helper, Spliterator<S> spliterator) {
            O result = ((TerminalSink) helper.wrapAndCopyInto((TerminalSink) this.sinkSupplier.get(), spliterator)).get();
            return result != null ? result : this.emptyValue;
        }

        public <P_IN> O evaluateParallel(PipelineHelper<T> helper, Spliterator<P_IN> spliterator) {
            return new FindTask(this, helper, spliterator).invoke();
        }
    }

    private static abstract class FindSink<T, O> implements TerminalSink<T, O> {
        boolean hasValue;
        T value;

        static final class OfDouble extends FindSink<Double, OptionalDouble> implements java.util.stream.Sink.OfDouble {
            OfDouble() {
            }

            public void accept(double value) {
                accept(Double.valueOf(value));
            }

            public OptionalDouble get() {
                return this.hasValue ? OptionalDouble.of(((Double) this.value).doubleValue()) : null;
            }
        }

        static final class OfInt extends FindSink<Integer, OptionalInt> implements java.util.stream.Sink.OfInt {
            OfInt() {
            }

            public void accept(int value) {
                accept(Integer.valueOf(value));
            }

            public OptionalInt get() {
                return this.hasValue ? OptionalInt.of(((Integer) this.value).intValue()) : null;
            }
        }

        static final class OfLong extends FindSink<Long, OptionalLong> implements java.util.stream.Sink.OfLong {
            OfLong() {
            }

            public void accept(long value) {
                accept(Long.valueOf(value));
            }

            public OptionalLong get() {
                return this.hasValue ? OptionalLong.of(((Long) this.value).longValue()) : null;
            }
        }

        static final class OfRef<T> extends FindSink<T, Optional<T>> {
            OfRef() {
            }

            public Optional<T> get() {
                return this.hasValue ? Optional.of(this.value) : null;
            }
        }

        FindSink() {
        }

        public void accept(T value) {
            if (!this.hasValue) {
                this.hasValue = true;
                this.value = value;
            }
        }

        public boolean cancellationRequested() {
            return this.hasValue;
        }
    }

    private static final class FindTask<P_IN, P_OUT, O> extends AbstractShortCircuitTask<P_IN, P_OUT, O, FindTask<P_IN, P_OUT, O>> {
        private final FindOp<P_OUT, O> op;

        FindTask(FindOp<P_OUT, O> op, PipelineHelper<P_OUT> helper, Spliterator<P_IN> spliterator) {
            super((PipelineHelper) helper, (Spliterator) spliterator);
            this.op = op;
        }

        FindTask(FindTask<P_IN, P_OUT, O> parent, Spliterator<P_IN> spliterator) {
            super((AbstractShortCircuitTask) parent, (Spliterator) spliterator);
            this.op = parent.op;
        }

        protected FindTask<P_IN, P_OUT, O> makeChild(Spliterator<P_IN> spliterator) {
            return new FindTask(this, spliterator);
        }

        protected O getEmptyResult() {
            return this.op.emptyValue;
        }

        private void foundResult(O answer) {
            if (isLeftmostNode()) {
                shortCircuit(answer);
            } else {
                cancelLaterNodes();
            }
        }

        protected O doLeaf() {
            O result = ((TerminalSink) this.helper.wrapAndCopyInto((TerminalSink) this.op.sinkSupplier.get(), this.spliterator)).get();
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

        public void onCompletion(CountedCompleter<?> caller) {
            if (this.op.mustFindFirst) {
                FindTask<P_IN, P_OUT, O> child = this.leftChild;
                FindTask<P_IN, P_OUT, O> findTask = null;
                while (child != findTask) {
                    O result = child.getLocalResult();
                    if (result != null && this.op.presentPredicate.test(result)) {
                        setLocalResult(result);
                        foundResult(result);
                        break;
                    }
                    findTask = child;
                    child = this.rightChild;
                }
            }
            super.onCompletion(caller);
        }
    }

    private FindOps() {
    }

    public static <T> TerminalOp<T, Optional<T>> makeRef(boolean mustFindFirst) {
        return new FindOp(mustFindFirst, StreamShape.REFERENCE, Optional.empty(), new -java_util_stream_TerminalOp_makeRef_boolean_mustFindFirst_LambdaImpl0(), new -java_util_stream_TerminalOp_makeRef_boolean_mustFindFirst_LambdaImpl1());
    }

    public static TerminalOp<Integer, OptionalInt> makeInt(boolean mustFindFirst) {
        return new FindOp(mustFindFirst, StreamShape.INT_VALUE, OptionalInt.empty(), new -java_util_stream_TerminalOp_makeInt_boolean_mustFindFirst_LambdaImpl0(), new -java_util_stream_TerminalOp_makeInt_boolean_mustFindFirst_LambdaImpl1());
    }

    public static TerminalOp<Long, OptionalLong> makeLong(boolean mustFindFirst) {
        return new FindOp(mustFindFirst, StreamShape.LONG_VALUE, OptionalLong.empty(), new -java_util_stream_TerminalOp_makeLong_boolean_mustFindFirst_LambdaImpl0(), new -java_util_stream_TerminalOp_makeLong_boolean_mustFindFirst_LambdaImpl1());
    }

    public static TerminalOp<Double, OptionalDouble> makeDouble(boolean mustFindFirst) {
        return new FindOp(mustFindFirst, StreamShape.DOUBLE_VALUE, OptionalDouble.empty(), new -java_util_stream_TerminalOp_makeDouble_boolean_mustFindFirst_LambdaImpl0(), new -java_util_stream_TerminalOp_makeDouble_boolean_mustFindFirst_LambdaImpl1());
    }
}
