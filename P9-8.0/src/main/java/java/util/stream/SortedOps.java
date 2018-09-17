package java.util.stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.IntFunction;
import java.util.stream.DoublePipeline.StatefulOp;
import java.util.stream.Sink.ChainedDouble;
import java.util.stream.Sink.ChainedInt;
import java.util.stream.Sink.ChainedLong;
import java.util.stream.Sink.ChainedReference;

final class SortedOps {

    private static abstract class AbstractDoubleSortingSink extends ChainedDouble<Double> {
        protected boolean cancellationWasRequested;

        AbstractDoubleSortingSink(Sink<? super Double> downstream) {
            super(downstream);
        }

        public final boolean cancellationRequested() {
            this.cancellationWasRequested = true;
            return false;
        }
    }

    private static abstract class AbstractIntSortingSink extends ChainedInt<Integer> {
        protected boolean cancellationWasRequested;

        AbstractIntSortingSink(Sink<? super Integer> downstream) {
            super(downstream);
        }

        public final boolean cancellationRequested() {
            this.cancellationWasRequested = true;
            return false;
        }
    }

    private static abstract class AbstractLongSortingSink extends ChainedLong<Long> {
        protected boolean cancellationWasRequested;

        AbstractLongSortingSink(Sink<? super Long> downstream) {
            super(downstream);
        }

        public final boolean cancellationRequested() {
            this.cancellationWasRequested = true;
            return false;
        }
    }

    private static abstract class AbstractRefSortingSink<T> extends ChainedReference<T, T> {
        protected boolean cancellationWasRequested;
        protected final Comparator<? super T> comparator;

        AbstractRefSortingSink(Sink<? super T> downstream, Comparator<? super T> comparator) {
            super(downstream);
            this.comparator = comparator;
        }

        public final boolean cancellationRequested() {
            this.cancellationWasRequested = true;
            return false;
        }
    }

    private static final class DoubleSortingSink extends AbstractDoubleSortingSink {
        private java.util.stream.SpinedBuffer.OfDouble b;

        DoubleSortingSink(Sink<? super Double> sink) {
            super(sink);
        }

        public void begin(long size) {
            if (size >= 2147483639) {
                throw new IllegalArgumentException("Stream size exceeds max array size");
            }
            this.b = size > 0 ? new java.util.stream.SpinedBuffer.OfDouble((int) size) : new java.util.stream.SpinedBuffer.OfDouble();
        }

        public void end() {
            int i = 0;
            double[] doubles = (double[]) this.b.asPrimitiveArray();
            Arrays.sort(doubles);
            this.downstream.begin((long) doubles.length);
            int length;
            if (this.cancellationWasRequested) {
                length = doubles.length;
                while (i < length) {
                    double aDouble = doubles[i];
                    if (this.downstream.cancellationRequested()) {
                        break;
                    }
                    this.downstream.-java_util_stream_ReferencePipeline$9$1-mthref-0(aDouble);
                    i++;
                }
            } else {
                length = doubles.length;
                while (i < length) {
                    this.downstream.-java_util_stream_ReferencePipeline$9$1-mthref-0(doubles[i]);
                    i++;
                }
            }
            this.downstream.end();
        }

        public void accept(double t) {
            this.b.-java_util_stream_StreamSpliterators$DoubleWrappingSpliterator-mthref-0(t);
        }
    }

    private static final class IntSortingSink extends AbstractIntSortingSink {
        private java.util.stream.SpinedBuffer.OfInt b;

        IntSortingSink(Sink<? super Integer> sink) {
            super(sink);
        }

        public void begin(long size) {
            if (size >= 2147483639) {
                throw new IllegalArgumentException("Stream size exceeds max array size");
            }
            this.b = size > 0 ? new java.util.stream.SpinedBuffer.OfInt((int) size) : new java.util.stream.SpinedBuffer.OfInt();
        }

        public void end() {
            int i = 0;
            int[] ints = (int[]) this.b.asPrimitiveArray();
            Arrays.sort(ints);
            this.downstream.begin((long) ints.length);
            int length;
            if (this.cancellationWasRequested) {
                length = ints.length;
                while (i < length) {
                    int anInt = ints[i];
                    if (this.downstream.cancellationRequested()) {
                        break;
                    }
                    this.downstream.-java_util_stream_IntPipeline-mthref-0(anInt);
                    i++;
                }
            } else {
                length = ints.length;
                while (i < length) {
                    this.downstream.-java_util_stream_IntPipeline-mthref-0(ints[i]);
                    i++;
                }
            }
            this.downstream.end();
        }

        public void accept(int t) {
            this.b.-java_util_stream_StreamSpliterators$IntWrappingSpliterator-mthref-0(t);
        }
    }

    private static final class LongSortingSink extends AbstractLongSortingSink {
        private java.util.stream.SpinedBuffer.OfLong b;

        LongSortingSink(Sink<? super Long> sink) {
            super(sink);
        }

        public void begin(long size) {
            if (size >= 2147483639) {
                throw new IllegalArgumentException("Stream size exceeds max array size");
            }
            this.b = size > 0 ? new java.util.stream.SpinedBuffer.OfLong((int) size) : new java.util.stream.SpinedBuffer.OfLong();
        }

        public void end() {
            int i = 0;
            long[] longs = (long[]) this.b.asPrimitiveArray();
            Arrays.sort(longs);
            this.downstream.begin((long) longs.length);
            int length;
            if (this.cancellationWasRequested) {
                length = longs.length;
                while (i < length) {
                    long aLong = longs[i];
                    if (this.downstream.cancellationRequested()) {
                        break;
                    }
                    this.downstream.-java_util_stream_LongPipeline-mthref-0(aLong);
                    i++;
                }
            } else {
                length = longs.length;
                while (i < length) {
                    this.downstream.-java_util_stream_LongPipeline-mthref-0(longs[i]);
                    i++;
                }
            }
            this.downstream.end();
        }

        public void accept(long t) {
            this.b.-java_util_stream_StreamSpliterators$LongWrappingSpliterator-mthref-0(t);
        }
    }

    private static final class OfDouble extends StatefulOp<Double> {
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

        public <P_IN> Node<Double> opEvaluateParallel(PipelineHelper<Double> helper, Spliterator<P_IN> spliterator, IntFunction<Double[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            double[] content = (double[]) ((java.util.stream.Node.OfDouble) helper.evaluate(spliterator, true, generator)).asPrimitiveArray();
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

        public <P_IN> Node<Integer> opEvaluateParallel(PipelineHelper<Integer> helper, Spliterator<P_IN> spliterator, IntFunction<Integer[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            int[] content = (int[]) ((java.util.stream.Node.OfInt) helper.evaluate(spliterator, true, generator)).asPrimitiveArray();
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

        public <P_IN> Node<Long> opEvaluateParallel(PipelineHelper<Long> helper, Spliterator<P_IN> spliterator, IntFunction<Long[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            long[] content = (long[]) ((java.util.stream.Node.OfLong) helper.evaluate(spliterator, true, generator)).asPrimitiveArray();
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

        OfRef(AbstractPipeline<?, T, ?> upstream, Comparator<? super T> comparator) {
            super(upstream, StreamShape.REFERENCE, StreamOpFlag.IS_ORDERED | StreamOpFlag.NOT_SORTED);
            this.isNaturalSort = false;
            this.comparator = (Comparator) Objects.requireNonNull(comparator);
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

        public <P_IN> Node<T> opEvaluateParallel(PipelineHelper<T> helper, Spliterator<P_IN> spliterator, IntFunction<T[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags()) && this.isNaturalSort) {
                return helper.evaluate(spliterator, false, generator);
            }
            Object[] flattenedData = helper.evaluate(spliterator, true, generator).asArray(generator);
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
            if (size >= 2147483639) {
                throw new IllegalArgumentException("Stream size exceeds max array size");
            }
            this.list = size >= 0 ? new ArrayList((int) size) : new ArrayList();
        }

        public void end() {
            this.list.sort(this.comparator);
            this.downstream.begin((long) this.list.size());
            if (this.cancellationWasRequested) {
                for (T t : this.list) {
                    if (this.downstream.cancellationRequested()) {
                        break;
                    }
                    this.downstream.-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-1(t);
                }
            } else {
                ArrayList arrayList = this.list;
                Sink sink = this.downstream;
                sink.getClass();
                arrayList.forEach(new -$Lambda$FhdcrNpyVDGf61Kgpb2-fJGXzpo(sink));
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
            if (size >= 2147483639) {
                throw new IllegalArgumentException("Stream size exceeds max array size");
            }
            this.array = new double[((int) size)];
        }

        public void end() {
            Arrays.sort(this.array, 0, this.offset);
            this.downstream.begin((long) this.offset);
            int i;
            if (this.cancellationWasRequested) {
                for (i = 0; i < this.offset && (this.downstream.cancellationRequested() ^ 1) != 0; i++) {
                    this.downstream.-java_util_stream_ReferencePipeline$9$1-mthref-0(this.array[i]);
                }
            } else {
                for (i = 0; i < this.offset; i++) {
                    this.downstream.-java_util_stream_ReferencePipeline$9$1-mthref-0(this.array[i]);
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
            if (size >= 2147483639) {
                throw new IllegalArgumentException("Stream size exceeds max array size");
            }
            this.array = new int[((int) size)];
        }

        public void end() {
            Arrays.sort(this.array, 0, this.offset);
            this.downstream.begin((long) this.offset);
            int i;
            if (this.cancellationWasRequested) {
                for (i = 0; i < this.offset && (this.downstream.cancellationRequested() ^ 1) != 0; i++) {
                    this.downstream.-java_util_stream_IntPipeline-mthref-0(this.array[i]);
                }
            } else {
                for (i = 0; i < this.offset; i++) {
                    this.downstream.-java_util_stream_IntPipeline-mthref-0(this.array[i]);
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
            if (size >= 2147483639) {
                throw new IllegalArgumentException("Stream size exceeds max array size");
            }
            this.array = new long[((int) size)];
        }

        public void end() {
            Arrays.sort(this.array, 0, this.offset);
            this.downstream.begin((long) this.offset);
            int i;
            if (this.cancellationWasRequested) {
                for (i = 0; i < this.offset && (this.downstream.cancellationRequested() ^ 1) != 0; i++) {
                    this.downstream.-java_util_stream_LongPipeline-mthref-0(this.array[i]);
                }
            } else {
                for (i = 0; i < this.offset; i++) {
                    this.downstream.-java_util_stream_LongPipeline-mthref-0(this.array[i]);
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
            if (size >= 2147483639) {
                throw new IllegalArgumentException("Stream size exceeds max array size");
            }
            this.array = new Object[((int) size)];
        }

        public void end() {
            Arrays.sort(this.array, 0, this.offset, this.comparator);
            this.downstream.begin((long) this.offset);
            int i;
            if (this.cancellationWasRequested) {
                for (i = 0; i < this.offset && (this.downstream.cancellationRequested() ^ 1) != 0; i++) {
                    this.downstream.-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-1(this.array[i]);
                }
            } else {
                for (i = 0; i < this.offset; i++) {
                    this.downstream.-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-1(this.array[i]);
                }
            }
            this.downstream.end();
            this.array = null;
        }

        public void accept(T t) {
            Object[] objArr = this.array;
            int i = this.offset;
            this.offset = i + 1;
            objArr[i] = t;
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
