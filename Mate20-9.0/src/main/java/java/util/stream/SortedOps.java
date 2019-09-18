package java.util.stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.DoublePipeline;
import java.util.stream.IntPipeline;
import java.util.stream.LongPipeline;
import java.util.stream.Node;
import java.util.stream.ReferencePipeline;
import java.util.stream.Sink;
import java.util.stream.SpinedBuffer;

final class SortedOps {

    private static abstract class AbstractDoubleSortingSink extends Sink.ChainedDouble<Double> {
        protected boolean cancellationWasRequested;

        AbstractDoubleSortingSink(Sink<? super Double> downstream) {
            super(downstream);
        }

        public final boolean cancellationRequested() {
            this.cancellationWasRequested = true;
            return false;
        }
    }

    private static abstract class AbstractIntSortingSink extends Sink.ChainedInt<Integer> {
        protected boolean cancellationWasRequested;

        AbstractIntSortingSink(Sink<? super Integer> downstream) {
            super(downstream);
        }

        public final boolean cancellationRequested() {
            this.cancellationWasRequested = true;
            return false;
        }
    }

    private static abstract class AbstractLongSortingSink extends Sink.ChainedLong<Long> {
        protected boolean cancellationWasRequested;

        AbstractLongSortingSink(Sink<? super Long> downstream) {
            super(downstream);
        }

        public final boolean cancellationRequested() {
            this.cancellationWasRequested = true;
            return false;
        }
    }

    private static abstract class AbstractRefSortingSink<T> extends Sink.ChainedReference<T, T> {
        protected boolean cancellationWasRequested;
        protected final Comparator<? super T> comparator;

        AbstractRefSortingSink(Sink<? super T> downstream, Comparator<? super T> comparator2) {
            super(downstream);
            this.comparator = comparator2;
        }

        public final boolean cancellationRequested() {
            this.cancellationWasRequested = true;
            return false;
        }
    }

    private static final class DoubleSortingSink extends AbstractDoubleSortingSink {
        private SpinedBuffer.OfDouble b;

        DoubleSortingSink(Sink<? super Double> sink) {
            super(sink);
        }

        public void begin(long size) {
            if (size < 2147483639) {
                this.b = size > 0 ? new SpinedBuffer.OfDouble((int) size) : new SpinedBuffer.OfDouble();
                return;
            }
            throw new IllegalArgumentException("Stream size exceeds max array size");
        }

        public void end() {
            double[] doubles = (double[]) this.b.asPrimitiveArray();
            Arrays.sort(doubles);
            this.downstream.begin((long) doubles.length);
            int i = 0;
            if (!this.cancellationWasRequested) {
                int length = doubles.length;
                while (i < length) {
                    this.downstream.accept(doubles[i]);
                    i++;
                }
            } else {
                int length2 = doubles.length;
                while (i < length2) {
                    double aDouble = doubles[i];
                    if (this.downstream.cancellationRequested()) {
                        break;
                    }
                    this.downstream.accept(aDouble);
                    i++;
                }
            }
            this.downstream.end();
        }

        public void accept(double t) {
            this.b.accept(t);
        }
    }

    private static final class IntSortingSink extends AbstractIntSortingSink {
        private SpinedBuffer.OfInt b;

        IntSortingSink(Sink<? super Integer> sink) {
            super(sink);
        }

        public void begin(long size) {
            if (size < 2147483639) {
                this.b = size > 0 ? new SpinedBuffer.OfInt((int) size) : new SpinedBuffer.OfInt();
                return;
            }
            throw new IllegalArgumentException("Stream size exceeds max array size");
        }

        public void end() {
            int[] ints = (int[]) this.b.asPrimitiveArray();
            Arrays.sort(ints);
            this.downstream.begin((long) ints.length);
            int i = 0;
            if (!this.cancellationWasRequested) {
                int length = ints.length;
                while (i < length) {
                    this.downstream.accept(ints[i]);
                    i++;
                }
            } else {
                int length2 = ints.length;
                while (i < length2) {
                    int anInt = ints[i];
                    if (this.downstream.cancellationRequested()) {
                        break;
                    }
                    this.downstream.accept(anInt);
                    i++;
                }
            }
            this.downstream.end();
        }

        public void accept(int t) {
            this.b.accept(t);
        }
    }

    private static final class LongSortingSink extends AbstractLongSortingSink {
        private SpinedBuffer.OfLong b;

        LongSortingSink(Sink<? super Long> sink) {
            super(sink);
        }

        public void begin(long size) {
            if (size < 2147483639) {
                this.b = size > 0 ? new SpinedBuffer.OfLong((int) size) : new SpinedBuffer.OfLong();
                return;
            }
            throw new IllegalArgumentException("Stream size exceeds max array size");
        }

        public void end() {
            long[] longs = (long[]) this.b.asPrimitiveArray();
            Arrays.sort(longs);
            this.downstream.begin((long) longs.length);
            int i = 0;
            if (!this.cancellationWasRequested) {
                int length = longs.length;
                while (i < length) {
                    this.downstream.accept(longs[i]);
                    i++;
                }
            } else {
                int length2 = longs.length;
                while (i < length2) {
                    long aLong = longs[i];
                    if (this.downstream.cancellationRequested()) {
                        break;
                    }
                    this.downstream.accept(aLong);
                    i++;
                }
            }
            this.downstream.end();
        }

        public void accept(long t) {
            this.b.accept(t);
        }
    }

    private static final class OfDouble extends DoublePipeline.StatefulOp<Double> {
        OfDouble(AbstractPipeline<?, Double, ?> upstream) {
            super(upstream, StreamShape.DOUBLE_VALUE, StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
            Objects.requireNonNull(sink);
            if (StreamOpFlag.SORTED.isKnown(flags)) {
                return sink;
            }
            if (StreamOpFlag.SIZED.isKnown(flags)) {
                return new SizedDoubleSortingSink(sink);
            }
            return new DoubleSortingSink(sink);
        }

        /* JADX WARNING: type inference failed for: r5v0, types: [java.util.Spliterator, java.util.Spliterator<P_IN>] */
        /* JADX WARNING: type inference failed for: r6v0, types: [java.util.function.IntFunction<java.lang.Double[]>, java.util.function.IntFunction] */
        /* JADX WARNING: Unknown variable types count: 2 */
        public <P_IN> Node<Double> opEvaluateParallel(PipelineHelper<Double> helper, Spliterator<P_IN> r5, IntFunction<Double[]> r6) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(r5, false, r6);
            }
            double[] content = (double[]) ((Node.OfDouble) helper.evaluate(r5, true, r6)).asPrimitiveArray();
            Arrays.parallelSort(content);
            return Nodes.node(content);
        }
    }

    private static final class OfInt extends IntPipeline.StatefulOp<Integer> {
        OfInt(AbstractPipeline<?, Integer, ?> upstream) {
            super(upstream, StreamShape.INT_VALUE, StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
            Objects.requireNonNull(sink);
            if (StreamOpFlag.SORTED.isKnown(flags)) {
                return sink;
            }
            if (StreamOpFlag.SIZED.isKnown(flags)) {
                return new SizedIntSortingSink(sink);
            }
            return new IntSortingSink(sink);
        }

        /* JADX WARNING: type inference failed for: r5v0, types: [java.util.Spliterator, java.util.Spliterator<P_IN>] */
        /* JADX WARNING: type inference failed for: r6v0, types: [java.util.function.IntFunction<java.lang.Integer[]>, java.util.function.IntFunction] */
        /* JADX WARNING: Unknown variable types count: 2 */
        public <P_IN> Node<Integer> opEvaluateParallel(PipelineHelper<Integer> helper, Spliterator<P_IN> r5, IntFunction<Integer[]> r6) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(r5, false, r6);
            }
            int[] content = (int[]) ((Node.OfInt) helper.evaluate(r5, true, r6)).asPrimitiveArray();
            Arrays.parallelSort(content);
            return Nodes.node(content);
        }
    }

    private static final class OfLong extends LongPipeline.StatefulOp<Long> {
        OfLong(AbstractPipeline<?, Long, ?> upstream) {
            super(upstream, StreamShape.LONG_VALUE, StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
            Objects.requireNonNull(sink);
            if (StreamOpFlag.SORTED.isKnown(flags)) {
                return sink;
            }
            if (StreamOpFlag.SIZED.isKnown(flags)) {
                return new SizedLongSortingSink(sink);
            }
            return new LongSortingSink(sink);
        }

        /* JADX WARNING: type inference failed for: r5v0, types: [java.util.Spliterator, java.util.Spliterator<P_IN>] */
        /* JADX WARNING: type inference failed for: r6v0, types: [java.util.function.IntFunction<java.lang.Long[]>, java.util.function.IntFunction] */
        /* JADX WARNING: Unknown variable types count: 2 */
        public <P_IN> Node<Long> opEvaluateParallel(PipelineHelper<Long> helper, Spliterator<P_IN> r5, IntFunction<Long[]> r6) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(r5, false, r6);
            }
            long[] content = (long[]) ((Node.OfLong) helper.evaluate(r5, true, r6)).asPrimitiveArray();
            Arrays.parallelSort(content);
            return Nodes.node(content);
        }
    }

    private static final class OfRef<T> extends ReferencePipeline.StatefulOp<T, T> {
        private final Comparator<? super T> comparator;
        private final boolean isNaturalSort;

        OfRef(AbstractPipeline<?, T, ?> upstream) {
            super(upstream, StreamShape.REFERENCE, StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
            this.isNaturalSort = true;
            this.comparator = Comparator.naturalOrder();
        }

        OfRef(AbstractPipeline<?, T, ?> upstream, Comparator<? super T> comparator2) {
            super(upstream, StreamShape.REFERENCE, StreamOpFlag.IS_ORDERED | StreamOpFlag.NOT_SORTED);
            this.isNaturalSort = false;
            this.comparator = (Comparator) Objects.requireNonNull(comparator2);
        }

        public Sink<T> opWrapSink(int flags, Sink<T> sink) {
            Objects.requireNonNull(sink);
            if (StreamOpFlag.SORTED.isKnown(flags) && this.isNaturalSort) {
                return sink;
            }
            if (StreamOpFlag.SIZED.isKnown(flags)) {
                return new SizedRefSortingSink(sink, this.comparator);
            }
            return new RefSortingSink(sink, this.comparator);
        }

        /* JADX WARNING: type inference failed for: r4v0, types: [java.util.Spliterator, java.util.Spliterator<P_IN>] */
        /* JADX WARNING: type inference failed for: r5v0, types: [java.util.function.IntFunction<T[]>, java.util.function.IntFunction] */
        /* JADX WARNING: Unknown variable types count: 2 */
        public <P_IN> Node<T> opEvaluateParallel(PipelineHelper<T> helper, Spliterator<P_IN> r4, IntFunction<T[]> r5) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags()) && this.isNaturalSort) {
                return helper.evaluate(r4, false, r5);
            }
            T[] flattenedData = helper.evaluate(r4, true, r5).asArray(r5);
            Arrays.parallelSort(flattenedData, this.comparator);
            return Nodes.node(flattenedData);
        }
    }

    private static final class RefSortingSink<T> extends AbstractRefSortingSink<T> {
        private ArrayList<T> list;

        RefSortingSink(Sink<? super T> sink, Comparator<? super T> comparator) {
            super(sink, comparator);
        }

        public void begin(long size) {
            if (size < 2147483639) {
                this.list = size >= 0 ? new ArrayList<>((int) size) : new ArrayList<>();
                return;
            }
            throw new IllegalArgumentException("Stream size exceeds max array size");
        }

        public void end() {
            this.list.sort(this.comparator);
            this.downstream.begin((long) this.list.size());
            if (!this.cancellationWasRequested) {
                ArrayList<T> arrayList = this.list;
                Sink sink = this.downstream;
                Objects.requireNonNull(sink);
                arrayList.forEach(new Consumer() {
                    public final void accept(Object obj) {
                        Sink.this.accept(obj);
                    }
                });
            } else {
                Iterator<T> it = this.list.iterator();
                while (it.hasNext()) {
                    T t = it.next();
                    if (this.downstream.cancellationRequested()) {
                        break;
                    }
                    this.downstream.accept(t);
                }
            }
            this.downstream.end();
            this.list = null;
        }

        public void accept(T t) {
            this.list.add(t);
        }
    }

    private static final class SizedDoubleSortingSink extends AbstractDoubleSortingSink {
        private double[] array;
        private int offset;

        SizedDoubleSortingSink(Sink<? super Double> downstream) {
            super(downstream);
        }

        public void begin(long size) {
            if (size < 2147483639) {
                this.array = new double[((int) size)];
                return;
            }
            throw new IllegalArgumentException("Stream size exceeds max array size");
        }

        public void end() {
            int i = 0;
            Arrays.sort(this.array, 0, this.offset);
            this.downstream.begin((long) this.offset);
            if (this.cancellationWasRequested) {
                while (true) {
                    int i2 = i;
                    if (i2 >= this.offset || this.downstream.cancellationRequested()) {
                        break;
                    }
                    this.downstream.accept(this.array[i2]);
                    i = i2 + 1;
                }
            } else {
                while (true) {
                    int i3 = i;
                    if (i3 >= this.offset) {
                        break;
                    }
                    this.downstream.accept(this.array[i3]);
                    i = i3 + 1;
                }
            }
            this.downstream.end();
            this.array = null;
        }

        public void accept(double t) {
            double[] dArr = this.array;
            int i = this.offset;
            this.offset = i + 1;
            dArr[i] = t;
        }
    }

    private static final class SizedIntSortingSink extends AbstractIntSortingSink {
        private int[] array;
        private int offset;

        SizedIntSortingSink(Sink<? super Integer> downstream) {
            super(downstream);
        }

        public void begin(long size) {
            if (size < 2147483639) {
                this.array = new int[((int) size)];
                return;
            }
            throw new IllegalArgumentException("Stream size exceeds max array size");
        }

        public void end() {
            int i = 0;
            Arrays.sort(this.array, 0, this.offset);
            this.downstream.begin((long) this.offset);
            if (this.cancellationWasRequested) {
                while (true) {
                    int i2 = i;
                    if (i2 >= this.offset || this.downstream.cancellationRequested()) {
                        break;
                    }
                    this.downstream.accept(this.array[i2]);
                    i = i2 + 1;
                }
            } else {
                while (true) {
                    int i3 = i;
                    if (i3 >= this.offset) {
                        break;
                    }
                    this.downstream.accept(this.array[i3]);
                    i = i3 + 1;
                }
            }
            this.downstream.end();
            this.array = null;
        }

        public void accept(int t) {
            int[] iArr = this.array;
            int i = this.offset;
            this.offset = i + 1;
            iArr[i] = t;
        }
    }

    private static final class SizedLongSortingSink extends AbstractLongSortingSink {
        private long[] array;
        private int offset;

        SizedLongSortingSink(Sink<? super Long> downstream) {
            super(downstream);
        }

        public void begin(long size) {
            if (size < 2147483639) {
                this.array = new long[((int) size)];
                return;
            }
            throw new IllegalArgumentException("Stream size exceeds max array size");
        }

        public void end() {
            int i = 0;
            Arrays.sort(this.array, 0, this.offset);
            this.downstream.begin((long) this.offset);
            if (this.cancellationWasRequested) {
                while (true) {
                    int i2 = i;
                    if (i2 >= this.offset || this.downstream.cancellationRequested()) {
                        break;
                    }
                    this.downstream.accept(this.array[i2]);
                    i = i2 + 1;
                }
            } else {
                while (true) {
                    int i3 = i;
                    if (i3 >= this.offset) {
                        break;
                    }
                    this.downstream.accept(this.array[i3]);
                    i = i3 + 1;
                }
            }
            this.downstream.end();
            this.array = null;
        }

        public void accept(long t) {
            long[] jArr = this.array;
            int i = this.offset;
            this.offset = i + 1;
            jArr[i] = t;
        }
    }

    private static final class SizedRefSortingSink<T> extends AbstractRefSortingSink<T> {
        private T[] array;
        private int offset;

        SizedRefSortingSink(Sink<? super T> sink, Comparator<? super T> comparator) {
            super(sink, comparator);
        }

        public void begin(long size) {
            if (size < 2147483639) {
                this.array = new Object[((int) size)];
                return;
            }
            throw new IllegalArgumentException("Stream size exceeds max array size");
        }

        public void end() {
            int i = 0;
            Arrays.sort(this.array, 0, this.offset, this.comparator);
            this.downstream.begin((long) this.offset);
            if (this.cancellationWasRequested) {
                while (true) {
                    int i2 = i;
                    if (i2 >= this.offset || this.downstream.cancellationRequested()) {
                        break;
                    }
                    this.downstream.accept(this.array[i2]);
                    i = i2 + 1;
                }
            } else {
                while (true) {
                    int i3 = i;
                    if (i3 >= this.offset) {
                        break;
                    }
                    this.downstream.accept(this.array[i3]);
                    i = i3 + 1;
                }
            }
            this.downstream.end();
            this.array = null;
        }

        public void accept(T t) {
            T[] tArr = this.array;
            int i = this.offset;
            this.offset = i + 1;
            tArr[i] = t;
        }
    }

    private SortedOps() {
    }

    static <T> Stream<T> makeRef(AbstractPipeline<?, T, ?> upstream) {
        return new OfRef(upstream);
    }

    static <T> Stream<T> makeRef(AbstractPipeline<?, T, ?> upstream, Comparator<? super T> comparator) {
        return new OfRef(upstream, comparator);
    }

    static <T> IntStream makeInt(AbstractPipeline<?, Integer, ?> upstream) {
        return new OfInt(upstream);
    }

    static <T> LongStream makeLong(AbstractPipeline<?, Long, ?> upstream) {
        return new OfLong(upstream);
    }

    static <T> DoubleStream makeDouble(AbstractPipeline<?, Double, ?> upstream) {
        return new OfDouble(upstream);
    }
}
