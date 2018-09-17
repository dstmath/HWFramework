package java.util.stream;

import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterator.OfDouble;
import java.util.Spliterator.OfInt;
import java.util.Spliterator.OfLong;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.DoubleStream.Builder;

final class Streams {
    static final Object NONE = new Object();

    private static abstract class AbstractStreamBuilderImpl<T, S extends Spliterator<T>> implements Spliterator<T> {
        int count;

        /* synthetic */ AbstractStreamBuilderImpl(AbstractStreamBuilderImpl -this0) {
            this();
        }

        private AbstractStreamBuilderImpl() {
        }

        public S trySplit() {
            return null;
        }

        public long estimateSize() {
            return (long) ((-this.count) - 1);
        }

        public int characteristics() {
            return 17488;
        }
    }

    static abstract class ConcatSpliterator<T, T_SPLITR extends Spliterator<T>> implements Spliterator<T> {
        protected final T_SPLITR aSpliterator;
        protected final T_SPLITR bSpliterator;
        boolean beforeSplit = true;
        final boolean unsized;

        private static abstract class OfPrimitive<T, T_CONS, T_SPLITR extends java.util.Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>> extends ConcatSpliterator<T, T_SPLITR> implements java.util.Spliterator.OfPrimitive<T, T_CONS, T_SPLITR> {
            /* synthetic */ OfPrimitive(java.util.Spliterator.OfPrimitive aSpliterator, java.util.Spliterator.OfPrimitive bSpliterator, OfPrimitive -this2) {
                this(aSpliterator, bSpliterator);
            }

            public /* bridge */ /* synthetic */ java.util.Spliterator.OfPrimitive trySplit() {
                return (java.util.Spliterator.OfPrimitive) trySplit();
            }

            private OfPrimitive(T_SPLITR aSpliterator, T_SPLITR bSpliterator) {
                super(aSpliterator, bSpliterator);
            }

            public boolean tryAdvance(T_CONS action) {
                if (!this.beforeSplit) {
                    return ((java.util.Spliterator.OfPrimitive) this.bSpliterator).tryAdvance(action);
                }
                boolean hasNext = ((java.util.Spliterator.OfPrimitive) this.aSpliterator).tryAdvance(action);
                if (hasNext) {
                    return hasNext;
                }
                this.beforeSplit = false;
                return ((java.util.Spliterator.OfPrimitive) this.bSpliterator).tryAdvance(action);
            }

            public void forEachRemaining(T_CONS action) {
                if (this.beforeSplit) {
                    ((java.util.Spliterator.OfPrimitive) this.aSpliterator).forEachRemaining(action);
                }
                ((java.util.Spliterator.OfPrimitive) this.bSpliterator).forEachRemaining(action);
            }
        }

        static class OfDouble extends OfPrimitive<Double, DoubleConsumer, java.util.Spliterator.OfDouble> implements java.util.Spliterator.OfDouble {
            public /* bridge */ /* synthetic */ java.util.Spliterator.OfDouble trySplit() {
                return (java.util.Spliterator.OfDouble) trySplit();
            }

            OfDouble(java.util.Spliterator.OfDouble aSpliterator, java.util.Spliterator.OfDouble bSpliterator) {
                super(aSpliterator, bSpliterator, null);
            }
        }

        static class OfInt extends OfPrimitive<Integer, IntConsumer, java.util.Spliterator.OfInt> implements java.util.Spliterator.OfInt {
            public /* bridge */ /* synthetic */ java.util.Spliterator.OfInt trySplit() {
                return (java.util.Spliterator.OfInt) trySplit();
            }

            OfInt(java.util.Spliterator.OfInt aSpliterator, java.util.Spliterator.OfInt bSpliterator) {
                super(aSpliterator, bSpliterator, null);
            }
        }

        static class OfLong extends OfPrimitive<Long, LongConsumer, java.util.Spliterator.OfLong> implements java.util.Spliterator.OfLong {
            public /* bridge */ /* synthetic */ java.util.Spliterator.OfLong trySplit() {
                return (java.util.Spliterator.OfLong) trySplit();
            }

            OfLong(java.util.Spliterator.OfLong aSpliterator, java.util.Spliterator.OfLong bSpliterator) {
                super(aSpliterator, bSpliterator, null);
            }
        }

        static class OfRef<T> extends ConcatSpliterator<T, Spliterator<T>> {
            OfRef(Spliterator<T> aSpliterator, Spliterator<T> bSpliterator) {
                super(aSpliterator, bSpliterator);
            }
        }

        public ConcatSpliterator(T_SPLITR aSpliterator, T_SPLITR bSpliterator) {
            boolean z = true;
            this.aSpliterator = aSpliterator;
            this.bSpliterator = bSpliterator;
            if (aSpliterator.estimateSize() + bSpliterator.estimateSize() >= 0) {
                z = false;
            }
            this.unsized = z;
        }

        public T_SPLITR trySplit() {
            T_SPLITR ret = this.beforeSplit ? this.aSpliterator : this.bSpliterator.trySplit();
            this.beforeSplit = false;
            return ret;
        }

        public boolean tryAdvance(Consumer<? super T> consumer) {
            if (!this.beforeSplit) {
                return this.bSpliterator.tryAdvance(consumer);
            }
            boolean hasNext = this.aSpliterator.tryAdvance(consumer);
            if (hasNext) {
                return hasNext;
            }
            this.beforeSplit = false;
            return this.bSpliterator.tryAdvance(consumer);
        }

        public void forEachRemaining(Consumer<? super T> consumer) {
            if (this.beforeSplit) {
                this.aSpliterator.forEachRemaining(consumer);
            }
            this.bSpliterator.forEachRemaining(consumer);
        }

        public long estimateSize() {
            if (!this.beforeSplit) {
                return this.bSpliterator.estimateSize();
            }
            long size = this.aSpliterator.estimateSize() + this.bSpliterator.estimateSize();
            if (size < 0) {
                size = Long.MAX_VALUE;
            }
            return size;
        }

        public int characteristics() {
            if (!this.beforeSplit) {
                return this.bSpliterator.characteristics();
            }
            return (~((this.unsized ? 16448 : 0) | 5)) & (this.bSpliterator.characteristics() & this.aSpliterator.characteristics());
        }

        public Comparator<? super T> getComparator() {
            if (!this.beforeSplit) {
                return this.bSpliterator.getComparator();
            }
            throw new IllegalStateException();
        }
    }

    static final class DoubleStreamBuilderImpl extends AbstractStreamBuilderImpl<Double, OfDouble> implements Builder, OfDouble {
        SpinedBuffer.OfDouble buffer;
        double first;

        DoubleStreamBuilderImpl() {
            super();
        }

        DoubleStreamBuilderImpl(double t) {
            super();
            this.first = t;
            this.count = -2;
        }

        public void accept(double t) {
            if (this.count == 0) {
                this.first = t;
                this.count++;
            } else if (this.count > 0) {
                if (this.buffer == null) {
                    this.buffer = new SpinedBuffer.OfDouble();
                    this.buffer.-java_util_stream_StreamSpliterators$DoubleWrappingSpliterator-mthref-0(this.first);
                    this.count++;
                }
                this.buffer.-java_util_stream_StreamSpliterators$DoubleWrappingSpliterator-mthref-0(t);
            } else {
                throw new IllegalStateException();
            }
        }

        public DoubleStream build() {
            int c = this.count;
            if (c >= 0) {
                this.count = (-this.count) - 1;
                return c < 2 ? StreamSupport.doubleStream(this, false) : StreamSupport.doubleStream(this.buffer.spliterator(), false);
            } else {
                throw new IllegalStateException();
            }
        }

        public boolean tryAdvance(DoubleConsumer action) {
            Objects.requireNonNull(action);
            if (this.count != -2) {
                return false;
            }
            action.-java_util_stream_StreamSpliterators$DoubleWrappingSpliterator-mthref-1(this.first);
            this.count = -1;
            return true;
        }

        public void forEachRemaining(DoubleConsumer action) {
            Objects.requireNonNull(action);
            if (this.count == -2) {
                action.-java_util_stream_StreamSpliterators$DoubleWrappingSpliterator-mthref-1(this.first);
                this.count = -1;
            }
        }
    }

    static final class IntStreamBuilderImpl extends AbstractStreamBuilderImpl<Integer, OfInt> implements IntStream.Builder, OfInt {
        SpinedBuffer.OfInt buffer;
        int first;

        IntStreamBuilderImpl() {
            super();
        }

        IntStreamBuilderImpl(int t) {
            super();
            this.first = t;
            this.count = -2;
        }

        public void accept(int t) {
            if (this.count == 0) {
                this.first = t;
                this.count++;
            } else if (this.count > 0) {
                if (this.buffer == null) {
                    this.buffer = new SpinedBuffer.OfInt();
                    this.buffer.-java_util_stream_StreamSpliterators$IntWrappingSpliterator-mthref-0(this.first);
                    this.count++;
                }
                this.buffer.-java_util_stream_StreamSpliterators$IntWrappingSpliterator-mthref-0(t);
            } else {
                throw new IllegalStateException();
            }
        }

        public IntStream build() {
            int c = this.count;
            if (c >= 0) {
                this.count = (-this.count) - 1;
                return c < 2 ? StreamSupport.intStream(this, false) : StreamSupport.intStream(this.buffer.spliterator(), false);
            } else {
                throw new IllegalStateException();
            }
        }

        public boolean tryAdvance(IntConsumer action) {
            Objects.requireNonNull(action);
            if (this.count != -2) {
                return false;
            }
            action.-java_util_stream_StreamSpliterators$IntWrappingSpliterator-mthref-1(this.first);
            this.count = -1;
            return true;
        }

        public void forEachRemaining(IntConsumer action) {
            Objects.requireNonNull(action);
            if (this.count == -2) {
                action.-java_util_stream_StreamSpliterators$IntWrappingSpliterator-mthref-1(this.first);
                this.count = -1;
            }
        }
    }

    static final class LongStreamBuilderImpl extends AbstractStreamBuilderImpl<Long, OfLong> implements LongStream.Builder, OfLong {
        SpinedBuffer.OfLong buffer;
        long first;

        LongStreamBuilderImpl() {
            super();
        }

        LongStreamBuilderImpl(long t) {
            super();
            this.first = t;
            this.count = -2;
        }

        public void accept(long t) {
            if (this.count == 0) {
                this.first = t;
                this.count++;
            } else if (this.count > 0) {
                if (this.buffer == null) {
                    this.buffer = new SpinedBuffer.OfLong();
                    this.buffer.-java_util_stream_StreamSpliterators$LongWrappingSpliterator-mthref-0(this.first);
                    this.count++;
                }
                this.buffer.-java_util_stream_StreamSpliterators$LongWrappingSpliterator-mthref-0(t);
            } else {
                throw new IllegalStateException();
            }
        }

        public LongStream build() {
            int c = this.count;
            if (c >= 0) {
                this.count = (-this.count) - 1;
                return c < 2 ? StreamSupport.longStream(this, false) : StreamSupport.longStream(this.buffer.spliterator(), false);
            } else {
                throw new IllegalStateException();
            }
        }

        public boolean tryAdvance(LongConsumer action) {
            Objects.requireNonNull(action);
            if (this.count != -2) {
                return false;
            }
            action.-java_util_stream_StreamSpliterators$LongWrappingSpliterator-mthref-1(this.first);
            this.count = -1;
            return true;
        }

        public void forEachRemaining(LongConsumer action) {
            Objects.requireNonNull(action);
            if (this.count == -2) {
                action.-java_util_stream_StreamSpliterators$LongWrappingSpliterator-mthref-1(this.first);
                this.count = -1;
            }
        }
    }

    static final class RangeIntSpliterator implements OfInt {
        private static final int BALANCED_SPLIT_THRESHOLD = 16777216;
        private static final int RIGHT_BALANCED_SPLIT_RATIO = 8;
        private int from;
        private int last;
        private final int upTo;

        RangeIntSpliterator(int from, int upTo, boolean closed) {
            this(from, upTo, closed ? 1 : 0);
        }

        private RangeIntSpliterator(int from, int upTo, int last) {
            this.from = from;
            this.upTo = upTo;
            this.last = last;
        }

        public boolean tryAdvance(IntConsumer consumer) {
            Objects.requireNonNull(consumer);
            int i = this.from;
            if (i < this.upTo) {
                this.from++;
                consumer.-java_util_stream_StreamSpliterators$IntWrappingSpliterator-mthref-1(i);
                return true;
            } else if (this.last <= 0) {
                return false;
            } else {
                this.last = 0;
                consumer.-java_util_stream_StreamSpliterators$IntWrappingSpliterator-mthref-1(i);
                return true;
            }
        }

        public void forEachRemaining(IntConsumer consumer) {
            Objects.requireNonNull(consumer);
            int i = this.from;
            int hUpTo = this.upTo;
            int hLast = this.last;
            this.from = this.upTo;
            this.last = 0;
            int i2 = i;
            while (i2 < hUpTo) {
                i = i2 + 1;
                consumer.-java_util_stream_StreamSpliterators$IntWrappingSpliterator-mthref-1(i2);
                i2 = i;
            }
            if (hLast > 0) {
                consumer.-java_util_stream_StreamSpliterators$IntWrappingSpliterator-mthref-1(i2);
            }
        }

        public long estimateSize() {
            return (((long) this.upTo) - ((long) this.from)) + ((long) this.last);
        }

        public int characteristics() {
            return 17749;
        }

        public Comparator<? super Integer> getComparator() {
            return null;
        }

        public OfInt trySplit() {
            long size = estimateSize();
            if (size <= 1) {
                return null;
            }
            int i = this.from;
            int splitPoint = this.from + splitPoint(size);
            this.from = splitPoint;
            return new RangeIntSpliterator(i, splitPoint, 0);
        }

        private int splitPoint(long size) {
            return (int) (size / ((long) (size < 16777216 ? 2 : 8)));
        }
    }

    static final class RangeLongSpliterator implements OfLong {
        static final /* synthetic */ boolean -assertionsDisabled = (RangeLongSpliterator.class.desiredAssertionStatus() ^ 1);
        private static final long BALANCED_SPLIT_THRESHOLD = 16777216;
        private static final long RIGHT_BALANCED_SPLIT_RATIO = 8;
        private long from;
        private int last;
        private final long upTo;

        RangeLongSpliterator(long from, long upTo, boolean closed) {
            this(from, upTo, closed ? 1 : 0);
        }

        private RangeLongSpliterator(long from, long upTo, int last) {
            if (-assertionsDisabled || (upTo - from) + ((long) last) > 0) {
                this.from = from;
                this.upTo = upTo;
                this.last = last;
                return;
            }
            throw new AssertionError();
        }

        public boolean tryAdvance(LongConsumer consumer) {
            Objects.requireNonNull(consumer);
            long i = this.from;
            if (i < this.upTo) {
                this.from++;
                consumer.-java_util_stream_StreamSpliterators$LongWrappingSpliterator-mthref-1(i);
                return true;
            } else if (this.last <= 0) {
                return -assertionsDisabled;
            } else {
                this.last = 0;
                consumer.-java_util_stream_StreamSpliterators$LongWrappingSpliterator-mthref-1(i);
                return true;
            }
        }

        public void forEachRemaining(LongConsumer consumer) {
            long i;
            Objects.requireNonNull(consumer);
            long i2 = this.from;
            long hUpTo = this.upTo;
            int hLast = this.last;
            this.from = this.upTo;
            this.last = 0;
            while (true) {
                i = i2;
                if (i >= hUpTo) {
                    break;
                }
                i2 = i + 1;
                consumer.-java_util_stream_StreamSpliterators$LongWrappingSpliterator-mthref-1(i);
            }
            if (hLast > 0) {
                consumer.-java_util_stream_StreamSpliterators$LongWrappingSpliterator-mthref-1(i);
            }
        }

        public long estimateSize() {
            return (this.upTo - this.from) + ((long) this.last);
        }

        public int characteristics() {
            return 17749;
        }

        public Comparator<? super Long> getComparator() {
            return null;
        }

        public OfLong trySplit() {
            long size = estimateSize();
            if (size <= 1) {
                return null;
            }
            long j = this.from;
            long splitPoint = this.from + splitPoint(size);
            this.from = splitPoint;
            return new RangeLongSpliterator(j, splitPoint, 0);
        }

        private long splitPoint(long size) {
            return size / (size < BALANCED_SPLIT_THRESHOLD ? 2 : RIGHT_BALANCED_SPLIT_RATIO);
        }
    }

    static final class StreamBuilderImpl<T> extends AbstractStreamBuilderImpl<T, Spliterator<T>> implements Stream.Builder<T> {
        SpinedBuffer<T> buffer;
        T first;

        StreamBuilderImpl() {
            super();
        }

        StreamBuilderImpl(T t) {
            super();
            this.first = t;
            this.count = -2;
        }

        public void accept(T t) {
            if (this.count == 0) {
                this.first = t;
                this.count++;
            } else if (this.count > 0) {
                if (this.buffer == null) {
                    this.buffer = new SpinedBuffer();
                    this.buffer.-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-0(this.first);
                    this.count++;
                }
                this.buffer.-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-0(t);
            } else {
                throw new IllegalStateException();
            }
        }

        public Stream.Builder<T> add(T t) {
            accept(t);
            return this;
        }

        public Stream<T> build() {
            int c = this.count;
            if (c >= 0) {
                this.count = (-this.count) - 1;
                return c < 2 ? StreamSupport.stream(this, false) : StreamSupport.stream(this.buffer.spliterator(), false);
            } else {
                throw new IllegalStateException();
            }
        }

        public boolean tryAdvance(Consumer<? super T> action) {
            Objects.requireNonNull(action);
            if (this.count != -2) {
                return false;
            }
            action.-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-1(this.first);
            this.count = -1;
            return true;
        }

        public void forEachRemaining(Consumer<? super T> action) {
            Objects.requireNonNull(action);
            if (this.count == -2) {
                action.-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-1(this.first);
                this.count = -1;
            }
        }
    }

    private Streams() {
        throw new Error("no instances");
    }

    static Runnable composeWithExceptions(final Runnable a, final Runnable b) {
        return new Runnable() {
            public void run() {
                try {
                    a.run();
                    b.run();
                } catch (Throwable e2) {
                    try {
                        e1.addSuppressed(e2);
                    } catch (Throwable th) {
                    }
                }
            }
        };
    }

    static Runnable composedClose(final BaseStream<?, ?> a, final BaseStream<?, ?> b) {
        return new Runnable() {
            public void run() {
                try {
                    a.close();
                    b.close();
                } catch (Throwable e2) {
                    try {
                        e1.addSuppressed(e2);
                    } catch (Throwable th) {
                    }
                }
            }
        };
    }
}
