package java.util.stream;

import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.SpinedBuffer;
import java.util.stream.Stream;

final class Streams {
    static final Object NONE = new Object();

    private static abstract class AbstractStreamBuilderImpl<T, S extends Spliterator<T>> implements Spliterator<T> {
        int count;

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

        static class OfDouble extends OfPrimitive<Double, DoubleConsumer, Spliterator.OfDouble> implements Spliterator.OfDouble {
            public /* bridge */ /* synthetic */ void forEachRemaining(DoubleConsumer doubleConsumer) {
                super.forEachRemaining(doubleConsumer);
            }

            public /* bridge */ /* synthetic */ boolean tryAdvance(DoubleConsumer doubleConsumer) {
                return super.tryAdvance(doubleConsumer);
            }

            public /* bridge */ /* synthetic */ Spliterator.OfDouble trySplit() {
                return (Spliterator.OfDouble) super.trySplit();
            }

            OfDouble(Spliterator.OfDouble aSpliterator, Spliterator.OfDouble bSpliterator) {
                super(aSpliterator, bSpliterator);
            }
        }

        static class OfInt extends OfPrimitive<Integer, IntConsumer, Spliterator.OfInt> implements Spliterator.OfInt {
            public /* bridge */ /* synthetic */ void forEachRemaining(IntConsumer intConsumer) {
                super.forEachRemaining(intConsumer);
            }

            public /* bridge */ /* synthetic */ boolean tryAdvance(IntConsumer intConsumer) {
                return super.tryAdvance(intConsumer);
            }

            public /* bridge */ /* synthetic */ Spliterator.OfInt trySplit() {
                return (Spliterator.OfInt) super.trySplit();
            }

            OfInt(Spliterator.OfInt aSpliterator, Spliterator.OfInt bSpliterator) {
                super(aSpliterator, bSpliterator);
            }
        }

        static class OfLong extends OfPrimitive<Long, LongConsumer, Spliterator.OfLong> implements Spliterator.OfLong {
            public /* bridge */ /* synthetic */ void forEachRemaining(LongConsumer longConsumer) {
                super.forEachRemaining(longConsumer);
            }

            public /* bridge */ /* synthetic */ boolean tryAdvance(LongConsumer longConsumer) {
                return super.tryAdvance(longConsumer);
            }

            public /* bridge */ /* synthetic */ Spliterator.OfLong trySplit() {
                return (Spliterator.OfLong) super.trySplit();
            }

            OfLong(Spliterator.OfLong aSpliterator, Spliterator.OfLong bSpliterator) {
                super(aSpliterator, bSpliterator);
            }
        }

        private static abstract class OfPrimitive<T, T_CONS, T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>> extends ConcatSpliterator<T, T_SPLITR> implements Spliterator.OfPrimitive<T, T_CONS, T_SPLITR> {
            public /* bridge */ /* synthetic */ Spliterator.OfPrimitive trySplit() {
                return (Spliterator.OfPrimitive) super.trySplit();
            }

            private OfPrimitive(T_SPLITR aSpliterator, T_SPLITR bSpliterator) {
                super(aSpliterator, bSpliterator);
            }

            public boolean tryAdvance(T_CONS action) {
                if (!this.beforeSplit) {
                    return ((Spliterator.OfPrimitive) this.bSpliterator).tryAdvance(action);
                }
                boolean hasNext = ((Spliterator.OfPrimitive) this.aSpliterator).tryAdvance(action);
                if (hasNext) {
                    return hasNext;
                }
                this.beforeSplit = false;
                return ((Spliterator.OfPrimitive) this.bSpliterator).tryAdvance(action);
            }

            public void forEachRemaining(T_CONS action) {
                if (this.beforeSplit) {
                    ((Spliterator.OfPrimitive) this.aSpliterator).forEachRemaining(action);
                }
                ((Spliterator.OfPrimitive) this.bSpliterator).forEachRemaining(action);
            }
        }

        static class OfRef<T> extends ConcatSpliterator<T, Spliterator<T>> {
            OfRef(Spliterator<T> aSpliterator, Spliterator<T> bSpliterator) {
                super(aSpliterator, bSpliterator);
            }
        }

        public ConcatSpliterator(T_SPLITR aSpliterator2, T_SPLITR bSpliterator2) {
            this.aSpliterator = aSpliterator2;
            this.bSpliterator = bSpliterator2;
            boolean z = true;
            this.unsized = aSpliterator2.estimateSize() + bSpliterator2.estimateSize() >= 0 ? false : z;
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
            return size >= 0 ? size : Long.MAX_VALUE;
        }

        public int characteristics() {
            if (!this.beforeSplit) {
                return this.bSpliterator.characteristics();
            }
            return this.aSpliterator.characteristics() & this.bSpliterator.characteristics() & (~(5 | (this.unsized ? 16448 : 0)));
        }

        public Comparator<? super T> getComparator() {
            if (!this.beforeSplit) {
                return this.bSpliterator.getComparator();
            }
            throw new IllegalStateException();
        }
    }

    static final class DoubleStreamBuilderImpl extends AbstractStreamBuilderImpl<Double, Spliterator.OfDouble> implements DoubleStream.Builder, Spliterator.OfDouble {
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
                    this.buffer.accept(this.first);
                    this.count++;
                }
                this.buffer.accept(t);
            } else {
                throw new IllegalStateException();
            }
        }

        public DoubleStream build() {
            int c = this.count;
            if (c >= 0) {
                this.count = (-this.count) - 1;
                return c < 2 ? StreamSupport.doubleStream(this, false) : StreamSupport.doubleStream(this.buffer.spliterator(), false);
            }
            throw new IllegalStateException();
        }

        public boolean tryAdvance(DoubleConsumer action) {
            Objects.requireNonNull(action);
            if (this.count != -2) {
                return false;
            }
            action.accept(this.first);
            this.count = -1;
            return true;
        }

        public void forEachRemaining(DoubleConsumer action) {
            Objects.requireNonNull(action);
            if (this.count == -2) {
                action.accept(this.first);
                this.count = -1;
            }
        }
    }

    static final class IntStreamBuilderImpl extends AbstractStreamBuilderImpl<Integer, Spliterator.OfInt> implements IntStream.Builder, Spliterator.OfInt {
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
                    this.buffer.accept(this.first);
                    this.count++;
                }
                this.buffer.accept(t);
            } else {
                throw new IllegalStateException();
            }
        }

        public IntStream build() {
            int c = this.count;
            if (c >= 0) {
                this.count = (-this.count) - 1;
                return c < 2 ? StreamSupport.intStream(this, false) : StreamSupport.intStream(this.buffer.spliterator(), false);
            }
            throw new IllegalStateException();
        }

        public boolean tryAdvance(IntConsumer action) {
            Objects.requireNonNull(action);
            if (this.count != -2) {
                return false;
            }
            action.accept(this.first);
            this.count = -1;
            return true;
        }

        public void forEachRemaining(IntConsumer action) {
            Objects.requireNonNull(action);
            if (this.count == -2) {
                action.accept(this.first);
                this.count = -1;
            }
        }
    }

    static final class LongStreamBuilderImpl extends AbstractStreamBuilderImpl<Long, Spliterator.OfLong> implements LongStream.Builder, Spliterator.OfLong {
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
                    this.buffer.accept(this.first);
                    this.count++;
                }
                this.buffer.accept(t);
            } else {
                throw new IllegalStateException();
            }
        }

        public LongStream build() {
            int c = this.count;
            if (c >= 0) {
                this.count = (-this.count) - 1;
                return c < 2 ? StreamSupport.longStream(this, false) : StreamSupport.longStream(this.buffer.spliterator(), false);
            }
            throw new IllegalStateException();
        }

        public boolean tryAdvance(LongConsumer action) {
            Objects.requireNonNull(action);
            if (this.count != -2) {
                return false;
            }
            action.accept(this.first);
            this.count = -1;
            return true;
        }

        public void forEachRemaining(LongConsumer action) {
            Objects.requireNonNull(action);
            if (this.count == -2) {
                action.accept(this.first);
                this.count = -1;
            }
        }
    }

    static final class RangeIntSpliterator implements Spliterator.OfInt {
        private static final int BALANCED_SPLIT_THRESHOLD = 16777216;
        private static final int RIGHT_BALANCED_SPLIT_RATIO = 8;
        private int from;
        private int last;
        private final int upTo;

        RangeIntSpliterator(int from2, int upTo2, boolean closed) {
            this(from2, upTo2, (int) closed);
        }

        private RangeIntSpliterator(int from2, int upTo2, int last2) {
            this.from = from2;
            this.upTo = upTo2;
            this.last = last2;
        }

        public boolean tryAdvance(IntConsumer consumer) {
            Objects.requireNonNull(consumer);
            int i = this.from;
            if (i < this.upTo) {
                this.from++;
                consumer.accept(i);
                return true;
            } else if (this.last <= 0) {
                return false;
            } else {
                this.last = 0;
                consumer.accept(i);
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
            while (i < hUpTo) {
                consumer.accept(i);
                i++;
            }
            if (hLast > 0) {
                consumer.accept(i);
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

        public Spliterator.OfInt trySplit() {
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

    static final class RangeLongSpliterator implements Spliterator.OfLong {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private static final long BALANCED_SPLIT_THRESHOLD = 16777216;
        private static final long RIGHT_BALANCED_SPLIT_RATIO = 8;
        private long from;
        private int last;
        private final long upTo;

        static {
            Class<Streams> cls = Streams.class;
        }

        RangeLongSpliterator(long from2, long upTo2, boolean closed) {
            this(from2, upTo2, (int) closed);
        }

        private RangeLongSpliterator(long from2, long upTo2, int last2) {
            this.from = from2;
            this.upTo = upTo2;
            this.last = last2;
        }

        public boolean tryAdvance(LongConsumer consumer) {
            Objects.requireNonNull(consumer);
            long i = this.from;
            if (i < this.upTo) {
                this.from++;
                consumer.accept(i);
                return true;
            } else if (this.last <= 0) {
                return $assertionsDisabled;
            } else {
                this.last = 0;
                consumer.accept(i);
                return true;
            }
        }

        public void forEachRemaining(LongConsumer consumer) {
            Objects.requireNonNull(consumer);
            long i = this.from;
            long hUpTo = this.upTo;
            int hLast = this.last;
            this.from = this.upTo;
            this.last = 0;
            while (i < hUpTo) {
                consumer.accept(i);
                i = 1 + i;
            }
            if (hLast > 0) {
                consumer.accept(i);
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

        public Spliterator.OfLong trySplit() {
            long size = estimateSize();
            if (size <= 1) {
                return null;
            }
            long j = this.from;
            long splitPoint = this.from + splitPoint(size);
            this.from = splitPoint;
            RangeLongSpliterator rangeLongSpliterator = new RangeLongSpliterator(j, splitPoint, 0);
            return rangeLongSpliterator;
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
                    this.buffer = new SpinedBuffer<>();
                    this.buffer.accept(this.first);
                    this.count++;
                }
                this.buffer.accept(t);
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
            }
            throw new IllegalStateException();
        }

        public boolean tryAdvance(Consumer<? super T> action) {
            Objects.requireNonNull(action);
            if (this.count != -2) {
                return false;
            }
            action.accept(this.first);
            this.count = -1;
            return true;
        }

        public void forEachRemaining(Consumer<? super T> action) {
            Objects.requireNonNull(action);
            if (this.count == -2) {
                action.accept(this.first);
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
                    Runnable.this.run();
                    b.run();
                    return;
                } catch (Throwable e2) {
                    try {
                        e1.addSuppressed(e2);
                    } catch (Throwable th) {
                    }
                }
                throw e1;
            }
        };
    }

    static Runnable composedClose(final BaseStream<?, ?> a, final BaseStream<?, ?> b) {
        return new Runnable() {
            public void run() {
                try {
                    BaseStream.this.close();
                    b.close();
                    return;
                } catch (Throwable e2) {
                    try {
                        e1.addSuppressed(e2);
                    } catch (Throwable th) {
                    }
                }
                throw e1;
            }
        };
    }
}
