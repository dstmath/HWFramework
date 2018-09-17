package java.util.stream;

import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc.AnonymousClass10;
import java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc.AnonymousClass11;
import java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc.AnonymousClass12;
import java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc.AnonymousClass13;
import java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc.AnonymousClass14;
import java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc.AnonymousClass15;
import java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc.AnonymousClass16;
import java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc.AnonymousClass17;
import java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc.AnonymousClass5;
import java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc.AnonymousClass6;
import java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc.AnonymousClass7;
import java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc.AnonymousClass8;
import java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc.AnonymousClass9;
import java.util.stream.SpinedBuffer.OfDouble;
import java.util.stream.SpinedBuffer.OfInt;
import java.util.stream.SpinedBuffer.OfLong;

class StreamSpliterators {

    private static abstract class AbstractWrappingSpliterator<P_IN, P_OUT, T_BUFFER extends AbstractSpinedBuffer> implements Spliterator<P_OUT> {
        T_BUFFER buffer;
        Sink<P_IN> bufferSink;
        boolean finished;
        final boolean isParallel;
        long nextToConsume;
        final PipelineHelper<P_OUT> ph;
        BooleanSupplier pusher;
        Spliterator<P_IN> spliterator;
        private Supplier<Spliterator<P_IN>> spliteratorSupplier;

        abstract void initPartialTraversalState();

        abstract AbstractWrappingSpliterator<P_IN, P_OUT, ?> wrap(Spliterator<P_IN> spliterator);

        AbstractWrappingSpliterator(PipelineHelper<P_OUT> ph, Supplier<Spliterator<P_IN>> spliteratorSupplier, boolean parallel) {
            this.ph = ph;
            this.spliteratorSupplier = spliteratorSupplier;
            this.spliterator = null;
            this.isParallel = parallel;
        }

        AbstractWrappingSpliterator(PipelineHelper<P_OUT> ph, Spliterator<P_IN> spliterator, boolean parallel) {
            this.ph = ph;
            this.spliteratorSupplier = null;
            this.spliterator = spliterator;
            this.isParallel = parallel;
        }

        final void init() {
            if (this.spliterator == null) {
                this.spliterator = (Spliterator) this.spliteratorSupplier.lambda$-java_util_stream_Collectors_49198();
                this.spliteratorSupplier = null;
            }
        }

        final boolean doAdvance() {
            if (this.buffer != null) {
                this.nextToConsume++;
                boolean hasNext = this.nextToConsume < this.buffer.count();
                if (!hasNext) {
                    this.nextToConsume = 0;
                    this.buffer.clear();
                    hasNext = fillBuffer();
                }
                return hasNext;
            } else if (this.finished) {
                return false;
            } else {
                init();
                initPartialTraversalState();
                this.nextToConsume = 0;
                this.bufferSink.begin(this.spliterator.getExactSizeIfKnown());
                return fillBuffer();
            }
        }

        public Spliterator<P_OUT> trySplit() {
            Spliterator<P_OUT> spliterator = null;
            if (!this.isParallel || (this.finished ^ 1) == 0) {
                return null;
            }
            init();
            Spliterator<P_IN> split = this.spliterator.trySplit();
            if (split != null) {
                spliterator = wrap(split);
            }
            return spliterator;
        }

        private boolean fillBuffer() {
            while (this.buffer.count() == 0) {
                if (this.bufferSink.cancellationRequested() || (this.pusher.getAsBoolean() ^ 1) != 0) {
                    if (this.finished) {
                        return false;
                    }
                    this.bufferSink.end();
                    this.finished = true;
                }
            }
            return true;
        }

        public final long estimateSize() {
            init();
            return this.spliterator.estimateSize();
        }

        public final long getExactSizeIfKnown() {
            init();
            if (StreamOpFlag.SIZED.isKnown(this.ph.getStreamAndOpFlags())) {
                return this.spliterator.getExactSizeIfKnown();
            }
            return -1;
        }

        public final int characteristics() {
            init();
            int c = StreamOpFlag.toCharacteristics(StreamOpFlag.toStreamFlags(this.ph.getStreamAndOpFlags()));
            if ((c & 64) != 0) {
                return (c & -16449) | (this.spliterator.characteristics() & 16448);
            }
            return c;
        }

        public Comparator<? super P_OUT> getComparator() {
            if (hasCharacteristics(4)) {
                return null;
            }
            throw new IllegalStateException();
        }

        public final String toString() {
            return String.format("%s[%s]", getClass().getName(), this.spliterator);
        }
    }

    static abstract class ArrayBuffer {
        int index;

        static abstract class OfPrimitive<T_CONS> extends ArrayBuffer {
            int index;

            abstract void forEach(T_CONS t_cons, long j);

            OfPrimitive() {
            }

            void reset() {
                this.index = 0;
            }
        }

        static final class OfDouble extends OfPrimitive<DoubleConsumer> implements DoubleConsumer {
            final double[] array;

            OfDouble(int size) {
                this.array = new double[size];
            }

            public void accept(double t) {
                double[] dArr = this.array;
                int i = this.index;
                this.index = i + 1;
                dArr[i] = t;
            }

            void forEach(DoubleConsumer action, long fence) {
                for (int i = 0; ((long) i) < fence; i++) {
                    action.-java_util_stream_StreamSpliterators$DoubleWrappingSpliterator-mthref-1(this.array[i]);
                }
            }
        }

        static final class OfInt extends OfPrimitive<IntConsumer> implements IntConsumer {
            final int[] array;

            OfInt(int size) {
                this.array = new int[size];
            }

            public void accept(int t) {
                int[] iArr = this.array;
                int i = this.index;
                this.index = i + 1;
                iArr[i] = t;
            }

            public void forEach(IntConsumer action, long fence) {
                for (int i = 0; ((long) i) < fence; i++) {
                    action.-java_util_stream_StreamSpliterators$IntWrappingSpliterator-mthref-1(this.array[i]);
                }
            }
        }

        static final class OfLong extends OfPrimitive<LongConsumer> implements LongConsumer {
            final long[] array;

            OfLong(int size) {
                this.array = new long[size];
            }

            public void accept(long t) {
                long[] jArr = this.array;
                int i = this.index;
                this.index = i + 1;
                jArr[i] = t;
            }

            public void forEach(LongConsumer action, long fence) {
                for (int i = 0; ((long) i) < fence; i++) {
                    action.-java_util_stream_StreamSpliterators$LongWrappingSpliterator-mthref-1(this.array[i]);
                }
            }
        }

        static final class OfRef<T> extends ArrayBuffer implements Consumer<T> {
            final Object[] array;

            OfRef(int size) {
                this.array = new Object[size];
            }

            public void accept(T t) {
                Object[] objArr = this.array;
                int i = this.index;
                this.index = i + 1;
                objArr[i] = t;
            }

            public void forEach(Consumer<? super T> action, long fence) {
                for (int i = 0; ((long) i) < fence; i++) {
                    action.-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-1(this.array[i]);
                }
            }
        }

        ArrayBuffer() {
        }

        void reset() {
            this.index = 0;
        }
    }

    static class DelegatingSpliterator<T, T_SPLITR extends Spliterator<T>> implements Spliterator<T> {
        private T_SPLITR s;
        private final Supplier<? extends T_SPLITR> supplier;

        static class OfPrimitive<T, T_CONS, T_SPLITR extends java.util.Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>> extends DelegatingSpliterator<T, T_SPLITR> implements java.util.Spliterator.OfPrimitive<T, T_CONS, T_SPLITR> {
            public /* bridge */ /* synthetic */ java.util.Spliterator.OfPrimitive trySplit() {
                return (java.util.Spliterator.OfPrimitive) trySplit();
            }

            OfPrimitive(Supplier<? extends T_SPLITR> supplier) {
                super(supplier);
            }

            public boolean tryAdvance(T_CONS consumer) {
                return ((java.util.Spliterator.OfPrimitive) get()).tryAdvance(consumer);
            }

            public void forEachRemaining(T_CONS consumer) {
                ((java.util.Spliterator.OfPrimitive) get()).forEachRemaining(consumer);
            }
        }

        static final class OfDouble extends OfPrimitive<Double, DoubleConsumer, java.util.Spliterator.OfDouble> implements java.util.Spliterator.OfDouble {
            public /* bridge */ /* synthetic */ java.util.Spliterator.OfDouble trySplit() {
                return (java.util.Spliterator.OfDouble) trySplit();
            }

            OfDouble(Supplier<java.util.Spliterator.OfDouble> supplier) {
                super(supplier);
            }
        }

        static final class OfInt extends OfPrimitive<Integer, IntConsumer, java.util.Spliterator.OfInt> implements java.util.Spliterator.OfInt {
            public /* bridge */ /* synthetic */ java.util.Spliterator.OfInt trySplit() {
                return (java.util.Spliterator.OfInt) trySplit();
            }

            OfInt(Supplier<java.util.Spliterator.OfInt> supplier) {
                super(supplier);
            }
        }

        static final class OfLong extends OfPrimitive<Long, LongConsumer, java.util.Spliterator.OfLong> implements java.util.Spliterator.OfLong {
            public /* bridge */ /* synthetic */ java.util.Spliterator.OfLong trySplit() {
                return (java.util.Spliterator.OfLong) trySplit();
            }

            OfLong(Supplier<java.util.Spliterator.OfLong> supplier) {
                super(supplier);
            }
        }

        DelegatingSpliterator(Supplier<? extends T_SPLITR> supplier) {
            this.supplier = supplier;
        }

        T_SPLITR get() {
            if (this.s == null) {
                this.s = (Spliterator) this.supplier.lambda$-java_util_stream_Collectors_49198();
            }
            return this.s;
        }

        public T_SPLITR trySplit() {
            return get().trySplit();
        }

        public boolean tryAdvance(Consumer<? super T> consumer) {
            return get().tryAdvance(consumer);
        }

        public void forEachRemaining(Consumer<? super T> consumer) {
            get().forEachRemaining(consumer);
        }

        public long estimateSize() {
            return get().estimateSize();
        }

        public int characteristics() {
            return get().characteristics();
        }

        public Comparator<? super T> getComparator() {
            return get().getComparator();
        }

        public long getExactSizeIfKnown() {
            return get().getExactSizeIfKnown();
        }

        public String toString() {
            return getClass().getName() + "[" + get() + "]";
        }
    }

    static final class DistinctSpliterator<T> implements Spliterator<T>, Consumer<T> {
        private static final Object NULL_VALUE = new Object();
        private final Spliterator<T> s;
        private final ConcurrentHashMap<T, Boolean> seen;
        private T tmpSlot;

        DistinctSpliterator(Spliterator<T> s) {
            this(s, new ConcurrentHashMap());
        }

        private DistinctSpliterator(Spliterator<T> s, ConcurrentHashMap<T, Boolean> seen) {
            this.s = s;
            this.seen = seen;
        }

        public void accept(T t) {
            this.tmpSlot = t;
        }

        private T mapNull(T t) {
            return t != null ? t : NULL_VALUE;
        }

        public boolean tryAdvance(Consumer<? super T> action) {
            while (this.s.tryAdvance(this)) {
                if (this.seen.putIfAbsent(mapNull(this.tmpSlot), Boolean.TRUE) == null) {
                    action.-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-1(this.tmpSlot);
                    this.tmpSlot = null;
                    return true;
                }
            }
            return false;
        }

        public void forEachRemaining(Consumer<? super T> action) {
            this.s.forEachRemaining(new AnonymousClass17(this, action));
        }

        /* synthetic */ void lambda$-java_util_stream_StreamSpliterators$DistinctSpliterator_46149(Consumer action, Object t) {
            if (this.seen.putIfAbsent(mapNull(t), Boolean.TRUE) == null) {
                action.-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-1(t);
            }
        }

        public Spliterator<T> trySplit() {
            Spliterator<T> split = this.s.trySplit();
            if (split != null) {
                return new DistinctSpliterator(split, this.seen);
            }
            return null;
        }

        public long estimateSize() {
            return this.s.estimateSize();
        }

        public int characteristics() {
            return (this.s.characteristics() & -16469) | 1;
        }

        public Comparator<? super T> getComparator() {
            return this.s.getComparator();
        }
    }

    static final class DoubleWrappingSpliterator<P_IN> extends AbstractWrappingSpliterator<P_IN, Double, OfDouble> implements Spliterator.OfDouble {
        DoubleWrappingSpliterator(PipelineHelper<Double> ph, Supplier<Spliterator<P_IN>> supplier, boolean parallel) {
            super((PipelineHelper) ph, (Supplier) supplier, parallel);
        }

        DoubleWrappingSpliterator(PipelineHelper<Double> ph, Spliterator<P_IN> spliterator, boolean parallel) {
            super((PipelineHelper) ph, (Spliterator) spliterator, parallel);
        }

        AbstractWrappingSpliterator<P_IN, Double, ?> wrap(Spliterator<P_IN> s) {
            return new DoubleWrappingSpliterator(this.ph, (Spliterator) s, this.isParallel);
        }

        void initPartialTraversalState() {
            OfDouble b = new OfDouble();
            this.buffer = b;
            PipelineHelper pipelineHelper = this.ph;
            b.getClass();
            this.bufferSink = pipelineHelper.wrapSink(new AnonymousClass10(b));
            this.pusher = new AnonymousClass5(this);
        }

        /* synthetic */ boolean lambda$-java_util_stream_StreamSpliterators$DoubleWrappingSpliterator_16351() {
            return this.spliterator.tryAdvance(this.bufferSink);
        }

        public Spliterator.OfDouble trySplit() {
            return (Spliterator.OfDouble) super.trySplit();
        }

        public boolean tryAdvance(DoubleConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext) {
                consumer.-java_util_stream_StreamSpliterators$DoubleWrappingSpliterator-mthref-1(((OfDouble) this.buffer).get(this.nextToConsume));
            }
            return hasNext;
        }

        public void forEachRemaining(DoubleConsumer consumer) {
            if (this.buffer != null || (this.finished ^ 1) == 0) {
                while (tryAdvance(consumer)) {
                }
                return;
            }
            Objects.requireNonNull(consumer);
            init();
            PipelineHelper pipelineHelper = this.ph;
            consumer.getClass();
            pipelineHelper.wrapAndCopyInto(new AnonymousClass9(consumer), this.spliterator);
            this.finished = true;
        }
    }

    static abstract class InfiniteSupplyingSpliterator<T> implements Spliterator<T> {
        long estimate;

        static final class OfDouble extends InfiniteSupplyingSpliterator<Double> implements java.util.Spliterator.OfDouble {
            final DoubleSupplier s;

            OfDouble(long size, DoubleSupplier s) {
                super(size);
                this.s = s;
            }

            public boolean tryAdvance(DoubleConsumer action) {
                Objects.requireNonNull(action);
                action.-java_util_stream_StreamSpliterators$DoubleWrappingSpliterator-mthref-1(this.s.getAsDouble());
                return true;
            }

            public java.util.Spliterator.OfDouble trySplit() {
                if (this.estimate == 0) {
                    return null;
                }
                long j = this.estimate >>> 1;
                this.estimate = j;
                return new OfDouble(j, this.s);
            }
        }

        static final class OfInt extends InfiniteSupplyingSpliterator<Integer> implements java.util.Spliterator.OfInt {
            final IntSupplier s;

            OfInt(long size, IntSupplier s) {
                super(size);
                this.s = s;
            }

            public boolean tryAdvance(IntConsumer action) {
                Objects.requireNonNull(action);
                action.-java_util_stream_StreamSpliterators$IntWrappingSpliterator-mthref-1(this.s.getAsInt());
                return true;
            }

            public java.util.Spliterator.OfInt trySplit() {
                if (this.estimate == 0) {
                    return null;
                }
                long j = this.estimate >>> 1;
                this.estimate = j;
                return new OfInt(j, this.s);
            }
        }

        static final class OfLong extends InfiniteSupplyingSpliterator<Long> implements java.util.Spliterator.OfLong {
            final LongSupplier s;

            OfLong(long size, LongSupplier s) {
                super(size);
                this.s = s;
            }

            public boolean tryAdvance(LongConsumer action) {
                Objects.requireNonNull(action);
                action.-java_util_stream_StreamSpliterators$LongWrappingSpliterator-mthref-1(this.s.getAsLong());
                return true;
            }

            public java.util.Spliterator.OfLong trySplit() {
                if (this.estimate == 0) {
                    return null;
                }
                long j = this.estimate >>> 1;
                this.estimate = j;
                return new OfLong(j, this.s);
            }
        }

        static final class OfRef<T> extends InfiniteSupplyingSpliterator<T> {
            final Supplier<T> s;

            OfRef(long size, Supplier<T> s) {
                super(size);
                this.s = s;
            }

            public boolean tryAdvance(Consumer<? super T> action) {
                Objects.requireNonNull(action);
                action.-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-1(this.s.lambda$-java_util_stream_Collectors_49198());
                return true;
            }

            public Spliterator<T> trySplit() {
                if (this.estimate == 0) {
                    return null;
                }
                long j = this.estimate >>> 1;
                this.estimate = j;
                return new OfRef(j, this.s);
            }
        }

        protected InfiniteSupplyingSpliterator(long estimate) {
            this.estimate = estimate;
        }

        public long estimateSize() {
            return this.estimate;
        }

        public int characteristics() {
            return 1024;
        }
    }

    static final class IntWrappingSpliterator<P_IN> extends AbstractWrappingSpliterator<P_IN, Integer, OfInt> implements Spliterator.OfInt {
        IntWrappingSpliterator(PipelineHelper<Integer> ph, Supplier<Spliterator<P_IN>> supplier, boolean parallel) {
            super((PipelineHelper) ph, (Supplier) supplier, parallel);
        }

        IntWrappingSpliterator(PipelineHelper<Integer> ph, Spliterator<P_IN> spliterator, boolean parallel) {
            super((PipelineHelper) ph, (Spliterator) spliterator, parallel);
        }

        AbstractWrappingSpliterator<P_IN, Integer, ?> wrap(Spliterator<P_IN> s) {
            return new IntWrappingSpliterator(this.ph, (Spliterator) s, this.isParallel);
        }

        void initPartialTraversalState() {
            OfInt b = new OfInt();
            this.buffer = b;
            PipelineHelper pipelineHelper = this.ph;
            b.getClass();
            this.bufferSink = pipelineHelper.wrapSink(new AnonymousClass12(b));
            this.pusher = new AnonymousClass6(this);
        }

        /* synthetic */ boolean lambda$-java_util_stream_StreamSpliterators$IntWrappingSpliterator_12402() {
            return this.spliterator.tryAdvance(this.bufferSink);
        }

        public Spliterator.OfInt trySplit() {
            return (Spliterator.OfInt) super.trySplit();
        }

        public boolean tryAdvance(IntConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext) {
                consumer.-java_util_stream_StreamSpliterators$IntWrappingSpliterator-mthref-1(((OfInt) this.buffer).get(this.nextToConsume));
            }
            return hasNext;
        }

        public void forEachRemaining(IntConsumer consumer) {
            if (this.buffer != null || (this.finished ^ 1) == 0) {
                while (tryAdvance(consumer)) {
                }
                return;
            }
            Objects.requireNonNull(consumer);
            init();
            PipelineHelper pipelineHelper = this.ph;
            consumer.getClass();
            pipelineHelper.wrapAndCopyInto(new AnonymousClass11(consumer), this.spliterator);
            this.finished = true;
        }
    }

    static final class LongWrappingSpliterator<P_IN> extends AbstractWrappingSpliterator<P_IN, Long, OfLong> implements Spliterator.OfLong {
        LongWrappingSpliterator(PipelineHelper<Long> ph, Supplier<Spliterator<P_IN>> supplier, boolean parallel) {
            super((PipelineHelper) ph, (Supplier) supplier, parallel);
        }

        LongWrappingSpliterator(PipelineHelper<Long> ph, Spliterator<P_IN> spliterator, boolean parallel) {
            super((PipelineHelper) ph, (Spliterator) spliterator, parallel);
        }

        AbstractWrappingSpliterator<P_IN, Long, ?> wrap(Spliterator<P_IN> s) {
            return new LongWrappingSpliterator(this.ph, (Spliterator) s, this.isParallel);
        }

        void initPartialTraversalState() {
            OfLong b = new OfLong();
            this.buffer = b;
            PipelineHelper pipelineHelper = this.ph;
            b.getClass();
            this.bufferSink = pipelineHelper.wrapSink(new AnonymousClass14(b));
            this.pusher = new AnonymousClass7(this);
        }

        /* synthetic */ boolean lambda$-java_util_stream_StreamSpliterators$LongWrappingSpliterator_14357() {
            return this.spliterator.tryAdvance(this.bufferSink);
        }

        public Spliterator.OfLong trySplit() {
            return (Spliterator.OfLong) super.trySplit();
        }

        public boolean tryAdvance(LongConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext) {
                consumer.-java_util_stream_StreamSpliterators$LongWrappingSpliterator-mthref-1(((OfLong) this.buffer).get(this.nextToConsume));
            }
            return hasNext;
        }

        public void forEachRemaining(LongConsumer consumer) {
            if (this.buffer != null || (this.finished ^ 1) == 0) {
                while (tryAdvance(consumer)) {
                }
                return;
            }
            Objects.requireNonNull(consumer);
            init();
            PipelineHelper pipelineHelper = this.ph;
            consumer.getClass();
            pipelineHelper.wrapAndCopyInto(new AnonymousClass13(consumer), this.spliterator);
            this.finished = true;
        }
    }

    static abstract class SliceSpliterator<T, T_SPLITR extends Spliterator<T>> {
        static final /* synthetic */ boolean -assertionsDisabled = (SliceSpliterator.class.desiredAssertionStatus() ^ 1);
        long fence;
        long index;
        T_SPLITR s;
        final long sliceFence;
        final long sliceOrigin;

        static abstract class OfPrimitive<T, T_SPLITR extends java.util.Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>, T_CONS> extends SliceSpliterator<T, T_SPLITR> implements java.util.Spliterator.OfPrimitive<T, T_CONS, T_SPLITR> {
            /* synthetic */ OfPrimitive(java.util.Spliterator.OfPrimitive s, long sliceOrigin, long sliceFence, long origin, long fence, OfPrimitive -this5) {
                this(s, sliceOrigin, sliceFence, origin, fence);
            }

            protected abstract T_CONS emptyConsumer();

            public /* bridge */ /* synthetic */ java.util.Spliterator.OfPrimitive trySplit() {
                return (java.util.Spliterator.OfPrimitive) trySplit();
            }

            OfPrimitive(T_SPLITR s, long sliceOrigin, long sliceFence) {
                this(s, sliceOrigin, sliceFence, 0, Math.min(s.estimateSize(), sliceFence));
            }

            private OfPrimitive(T_SPLITR s, long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence);
            }

            public boolean tryAdvance(T_CONS action) {
                Objects.requireNonNull(action);
                if (this.sliceOrigin >= this.fence) {
                    return false;
                }
                while (this.sliceOrigin > this.index) {
                    ((java.util.Spliterator.OfPrimitive) this.s).tryAdvance(emptyConsumer());
                    this.index++;
                }
                if (this.index >= this.fence) {
                    return false;
                }
                this.index++;
                return ((java.util.Spliterator.OfPrimitive) this.s).tryAdvance(action);
            }

            public void forEachRemaining(T_CONS action) {
                Objects.requireNonNull(action);
                if (this.sliceOrigin < this.fence && this.index < this.fence) {
                    if (this.index >= this.sliceOrigin) {
                        if (((java.util.Spliterator.OfPrimitive) this.s).estimateSize() + this.index <= this.sliceFence) {
                            ((java.util.Spliterator.OfPrimitive) this.s).forEachRemaining(action);
                            this.index = this.fence;
                        }
                    }
                    while (this.sliceOrigin > this.index) {
                        ((java.util.Spliterator.OfPrimitive) this.s).tryAdvance(emptyConsumer());
                        this.index++;
                    }
                    while (this.index < this.fence) {
                        ((java.util.Spliterator.OfPrimitive) this.s).tryAdvance(action);
                        this.index++;
                    }
                }
            }
        }

        static final class OfDouble extends OfPrimitive<Double, java.util.Spliterator.OfDouble, DoubleConsumer> implements java.util.Spliterator.OfDouble {
            static /* synthetic */ void lambda$-java_util_stream_StreamSpliterators$SliceSpliterator$OfDouble_31710(double e) {
            }

            public /* bridge */ /* synthetic */ java.util.Spliterator.OfDouble trySplit() {
                return (java.util.Spliterator.OfDouble) trySplit();
            }

            OfDouble(java.util.Spliterator.OfDouble s, long sliceOrigin, long sliceFence) {
                super(s, sliceOrigin, sliceFence);
            }

            OfDouble(java.util.Spliterator.OfDouble s, long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence, null);
            }

            protected java.util.Spliterator.OfDouble makeSpliterator(java.util.Spliterator.OfDouble s, long sliceOrigin, long sliceFence, long origin, long fence) {
                return new OfDouble(s, sliceOrigin, sliceFence, origin, fence);
            }

            protected DoubleConsumer emptyConsumer() {
                return new DoubleConsumer() {
                    public final void accept(double d) {
                        $m$0(d);
                    }
                };
            }
        }

        static final class OfInt extends OfPrimitive<Integer, java.util.Spliterator.OfInt, IntConsumer> implements java.util.Spliterator.OfInt {
            static /* synthetic */ void lambda$-java_util_stream_StreamSpliterators$SliceSpliterator$OfInt_29662(int e) {
            }

            public /* bridge */ /* synthetic */ java.util.Spliterator.OfInt trySplit() {
                return (java.util.Spliterator.OfInt) trySplit();
            }

            OfInt(java.util.Spliterator.OfInt s, long sliceOrigin, long sliceFence) {
                super(s, sliceOrigin, sliceFence);
            }

            OfInt(java.util.Spliterator.OfInt s, long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence, null);
            }

            protected java.util.Spliterator.OfInt makeSpliterator(java.util.Spliterator.OfInt s, long sliceOrigin, long sliceFence, long origin, long fence) {
                return new OfInt(s, sliceOrigin, sliceFence, origin, fence);
            }

            protected IntConsumer emptyConsumer() {
                return new IntConsumer() {
                    public final void accept(int i) {
                        $m$0(i);
                    }
                };
            }
        }

        static final class OfLong extends OfPrimitive<Long, java.util.Spliterator.OfLong, LongConsumer> implements java.util.Spliterator.OfLong {
            static /* synthetic */ void lambda$-java_util_stream_StreamSpliterators$SliceSpliterator$OfLong_30670(long e) {
            }

            public /* bridge */ /* synthetic */ java.util.Spliterator.OfLong trySplit() {
                return (java.util.Spliterator.OfLong) trySplit();
            }

            OfLong(java.util.Spliterator.OfLong s, long sliceOrigin, long sliceFence) {
                super(s, sliceOrigin, sliceFence);
            }

            OfLong(java.util.Spliterator.OfLong s, long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence, null);
            }

            protected java.util.Spliterator.OfLong makeSpliterator(java.util.Spliterator.OfLong s, long sliceOrigin, long sliceFence, long origin, long fence) {
                return new OfLong(s, sliceOrigin, sliceFence, origin, fence);
            }

            protected LongConsumer emptyConsumer() {
                return new LongConsumer() {
                    public final void accept(long j) {
                        $m$0(j);
                    }
                };
            }
        }

        static final class OfRef<T> extends SliceSpliterator<T, Spliterator<T>> implements Spliterator<T> {
            static /* synthetic */ void lambda$-java_util_stream_StreamSpliterators$SliceSpliterator$OfRef_25294(Object obj) {
            }

            static /* synthetic */ void lambda$-java_util_stream_StreamSpliterators$SliceSpliterator$OfRef_26203(Object obj) {
            }

            OfRef(Spliterator<T> s, long sliceOrigin, long sliceFence) {
                this(s, sliceOrigin, sliceFence, 0, Math.min(s.estimateSize(), sliceFence));
            }

            private OfRef(Spliterator<T> s, long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence);
            }

            protected Spliterator<T> makeSpliterator(Spliterator<T> s, long sliceOrigin, long sliceFence, long origin, long fence) {
                return new OfRef(s, sliceOrigin, sliceFence, origin, fence);
            }

            public boolean tryAdvance(Consumer<? super T> action) {
                Objects.requireNonNull(action);
                if (this.sliceOrigin >= this.fence) {
                    return false;
                }
                while (this.sliceOrigin > this.index) {
                    this.s.tryAdvance(new Consumer() {
                        public final void accept(Object obj) {
                            $m$0(obj);
                        }
                    });
                    this.index++;
                }
                if (this.index >= this.fence) {
                    return false;
                }
                this.index++;
                return this.s.tryAdvance(action);
            }

            public void forEachRemaining(Consumer<? super T> action) {
                Objects.requireNonNull(action);
                if (this.sliceOrigin < this.fence && this.index < this.fence) {
                    if (this.index < this.sliceOrigin || this.index + this.s.estimateSize() > this.sliceFence) {
                        while (this.sliceOrigin > this.index) {
                            this.s.tryAdvance(new -$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc());
                            this.index++;
                        }
                        while (this.index < this.fence) {
                            this.s.tryAdvance(action);
                            this.index++;
                        }
                    } else {
                        this.s.forEachRemaining(action);
                        this.index = this.fence;
                    }
                }
            }
        }

        protected abstract T_SPLITR makeSpliterator(T_SPLITR t_splitr, long j, long j2, long j3, long j4);

        SliceSpliterator(T_SPLITR s, long sliceOrigin, long sliceFence, long origin, long fence) {
            if (-assertionsDisabled || s.hasCharacteristics(16384)) {
                this.s = s;
                this.sliceOrigin = sliceOrigin;
                this.sliceFence = sliceFence;
                this.index = origin;
                this.fence = fence;
                return;
            }
            throw new AssertionError();
        }

        public T_SPLITR trySplit() {
            if (this.sliceOrigin >= this.fence || this.index >= this.fence) {
                return null;
            }
            while (true) {
                T_SPLITR leftSplit = this.s.trySplit();
                if (leftSplit == null) {
                    return null;
                }
                long leftSplitFenceUnbounded = this.index + leftSplit.estimateSize();
                long leftSplitFence = Math.min(leftSplitFenceUnbounded, this.sliceFence);
                if (this.sliceOrigin >= leftSplitFence) {
                    this.index = leftSplitFence;
                } else if (leftSplitFence >= this.sliceFence) {
                    this.s = leftSplit;
                    this.fence = leftSplitFence;
                } else if (this.index < this.sliceOrigin || leftSplitFenceUnbounded > this.sliceFence) {
                    long j = this.sliceOrigin;
                    long j2 = this.sliceFence;
                    long j3 = this.index;
                    this.index = leftSplitFence;
                    return makeSpliterator(leftSplit, j, j2, j3, leftSplitFence);
                } else {
                    this.index = leftSplitFence;
                    return leftSplit;
                }
            }
        }

        public long estimateSize() {
            return this.sliceOrigin < this.fence ? this.fence - Math.max(this.sliceOrigin, this.index) : 0;
        }

        public int characteristics() {
            return this.s.characteristics();
        }
    }

    static abstract class UnorderedSliceSpliterator<T, T_SPLITR extends Spliterator<T>> {
        static final /* synthetic */ boolean -assertionsDisabled = (UnorderedSliceSpliterator.class.desiredAssertionStatus() ^ 1);
        static final int CHUNK_SIZE = 128;
        private final AtomicLong permits;
        protected final T_SPLITR s;
        private final long skipThreshold;
        protected final boolean unlimited;

        static abstract class OfPrimitive<T, T_CONS, T_BUFF extends OfPrimitive<T_CONS>, T_SPLITR extends java.util.Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>> extends UnorderedSliceSpliterator<T, T_SPLITR> implements java.util.Spliterator.OfPrimitive<T, T_CONS, T_SPLITR> {
            protected abstract void acceptConsumed(T_CONS t_cons);

            protected abstract T_BUFF bufferCreate(int i);

            public /* bridge */ /* synthetic */ java.util.Spliterator.OfPrimitive trySplit() {
                return (java.util.Spliterator.OfPrimitive) trySplit();
            }

            OfPrimitive(T_SPLITR s, long skip, long limit) {
                super(s, skip, limit);
            }

            OfPrimitive(T_SPLITR s, OfPrimitive<T, T_CONS, T_BUFF, T_SPLITR> parent) {
                super(s, parent);
            }

            public boolean tryAdvance(T_CONS action) {
                Objects.requireNonNull(action);
                while (permitStatus() != PermitStatus.NO_MORE && ((java.util.Spliterator.OfPrimitive) this.s).tryAdvance(this)) {
                    if (acquirePermits(1) == 1) {
                        acceptConsumed(action);
                        return true;
                    }
                }
                return UnorderedSliceSpliterator.-assertionsDisabled;
            }

            public void forEachRemaining(T_CONS action) {
                Objects.requireNonNull(action);
                OfPrimitive ofPrimitive = null;
                while (true) {
                    PermitStatus permitStatus = permitStatus();
                    if (permitStatus == PermitStatus.NO_MORE) {
                        return;
                    }
                    if (permitStatus == PermitStatus.MAYBE_MORE) {
                        if (ofPrimitive == null) {
                            ofPrimitive = bufferCreate(128);
                        } else {
                            ofPrimitive.reset();
                        }
                        T_CONS sbc = ofPrimitive;
                        long permitsRequested = 0;
                        while (((java.util.Spliterator.OfPrimitive) this.s).tryAdvance(sbc)) {
                            permitsRequested++;
                            if (permitsRequested >= 128) {
                                break;
                            }
                        }
                        if (permitsRequested != 0) {
                            ofPrimitive.forEach(action, acquirePermits(permitsRequested));
                        } else {
                            return;
                        }
                    }
                    ((java.util.Spliterator.OfPrimitive) this.s).forEachRemaining(action);
                    return;
                }
            }
        }

        static final class OfDouble extends OfPrimitive<Double, DoubleConsumer, OfDouble, java.util.Spliterator.OfDouble> implements java.util.Spliterator.OfDouble, DoubleConsumer {
            double tmpValue;

            public /* bridge */ /* synthetic */ java.util.Spliterator.OfDouble trySplit() {
                return (java.util.Spliterator.OfDouble) trySplit();
            }

            OfDouble(java.util.Spliterator.OfDouble s, long skip, long limit) {
                super(s, skip, limit);
            }

            OfDouble(java.util.Spliterator.OfDouble s, OfDouble parent) {
                super(s, parent);
            }

            public void accept(double value) {
                this.tmpValue = value;
            }

            protected void acceptConsumed(DoubleConsumer action) {
                action.-java_util_stream_StreamSpliterators$DoubleWrappingSpliterator-mthref-1(this.tmpValue);
            }

            protected OfDouble bufferCreate(int initialCapacity) {
                return new OfDouble(initialCapacity);
            }

            protected java.util.Spliterator.OfDouble makeSpliterator(java.util.Spliterator.OfDouble s) {
                return new OfDouble(s, this);
            }
        }

        static final class OfInt extends OfPrimitive<Integer, IntConsumer, OfInt, java.util.Spliterator.OfInt> implements java.util.Spliterator.OfInt, IntConsumer {
            int tmpValue;

            public /* bridge */ /* synthetic */ java.util.Spliterator.OfInt trySplit() {
                return (java.util.Spliterator.OfInt) trySplit();
            }

            OfInt(java.util.Spliterator.OfInt s, long skip, long limit) {
                super(s, skip, limit);
            }

            OfInt(java.util.Spliterator.OfInt s, OfInt parent) {
                super(s, parent);
            }

            public void accept(int value) {
                this.tmpValue = value;
            }

            protected void acceptConsumed(IntConsumer action) {
                action.-java_util_stream_StreamSpliterators$IntWrappingSpliterator-mthref-1(this.tmpValue);
            }

            protected OfInt bufferCreate(int initialCapacity) {
                return new OfInt(initialCapacity);
            }

            protected java.util.Spliterator.OfInt makeSpliterator(java.util.Spliterator.OfInt s) {
                return new OfInt(s, this);
            }
        }

        static final class OfLong extends OfPrimitive<Long, LongConsumer, OfLong, java.util.Spliterator.OfLong> implements java.util.Spliterator.OfLong, LongConsumer {
            long tmpValue;

            public /* bridge */ /* synthetic */ java.util.Spliterator.OfLong trySplit() {
                return (java.util.Spliterator.OfLong) trySplit();
            }

            OfLong(java.util.Spliterator.OfLong s, long skip, long limit) {
                super(s, skip, limit);
            }

            OfLong(java.util.Spliterator.OfLong s, OfLong parent) {
                super(s, parent);
            }

            public void accept(long value) {
                this.tmpValue = value;
            }

            protected void acceptConsumed(LongConsumer action) {
                action.-java_util_stream_StreamSpliterators$LongWrappingSpliterator-mthref-1(this.tmpValue);
            }

            protected OfLong bufferCreate(int initialCapacity) {
                return new OfLong(initialCapacity);
            }

            protected java.util.Spliterator.OfLong makeSpliterator(java.util.Spliterator.OfLong s) {
                return new OfLong(s, this);
            }
        }

        static final class OfRef<T> extends UnorderedSliceSpliterator<T, Spliterator<T>> implements Spliterator<T>, Consumer<T> {
            T tmpSlot;

            OfRef(Spliterator<T> s, long skip, long limit) {
                super(s, skip, limit);
            }

            OfRef(Spliterator<T> s, OfRef<T> parent) {
                super(s, parent);
            }

            public final void accept(T t) {
                this.tmpSlot = t;
            }

            public boolean tryAdvance(Consumer<? super T> action) {
                Objects.requireNonNull(action);
                while (permitStatus() != PermitStatus.NO_MORE && this.s.tryAdvance(this)) {
                    if (acquirePermits(1) == 1) {
                        action.-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-1(this.tmpSlot);
                        this.tmpSlot = null;
                        return true;
                    }
                }
                return UnorderedSliceSpliterator.-assertionsDisabled;
            }

            public void forEachRemaining(Consumer<? super T> action) {
                Objects.requireNonNull(action);
                OfRef ofRef = null;
                while (true) {
                    PermitStatus permitStatus = permitStatus();
                    if (permitStatus == PermitStatus.NO_MORE) {
                        return;
                    }
                    if (permitStatus == PermitStatus.MAYBE_MORE) {
                        if (ofRef == null) {
                            ofRef = new OfRef(128);
                        } else {
                            ofRef.reset();
                        }
                        long permitsRequested = 0;
                        while (this.s.tryAdvance(ofRef)) {
                            permitsRequested++;
                            if (permitsRequested >= 128) {
                                break;
                            }
                        }
                        if (permitsRequested != 0) {
                            ofRef.forEach(action, acquirePermits(permitsRequested));
                        } else {
                            return;
                        }
                    }
                    this.s.forEachRemaining(action);
                    return;
                }
            }

            protected Spliterator<T> makeSpliterator(Spliterator<T> s) {
                return new OfRef(s, this);
            }
        }

        enum PermitStatus {
            NO_MORE,
            MAYBE_MORE,
            UNLIMITED
        }

        protected abstract T_SPLITR makeSpliterator(T_SPLITR t_splitr);

        UnorderedSliceSpliterator(T_SPLITR s, long skip, long limit) {
            long j;
            this.s = s;
            this.unlimited = limit < 0 ? true : -assertionsDisabled;
            if (limit >= 0) {
                j = limit;
            } else {
                j = 0;
            }
            this.skipThreshold = j;
            if (limit >= 0) {
                skip += limit;
            }
            this.permits = new AtomicLong(skip);
        }

        UnorderedSliceSpliterator(T_SPLITR s, UnorderedSliceSpliterator<T, T_SPLITR> parent) {
            this.s = s;
            this.unlimited = parent.unlimited;
            this.permits = parent.permits;
            this.skipThreshold = parent.skipThreshold;
        }

        protected final long acquirePermits(long numElements) {
            if (-assertionsDisabled || numElements > 0) {
                long remainingPermits;
                long grabbing;
                do {
                    remainingPermits = this.permits.get();
                    if (remainingPermits != 0) {
                        grabbing = Math.min(remainingPermits, numElements);
                        if (grabbing <= 0) {
                            break;
                        }
                    } else {
                        if (!this.unlimited) {
                            numElements = 0;
                        }
                        return numElements;
                    }
                } while ((this.permits.compareAndSet(remainingPermits, remainingPermits - grabbing) ^ 1) != 0);
                if (this.unlimited) {
                    return Math.max(numElements - grabbing, 0);
                }
                if (remainingPermits > this.skipThreshold) {
                    return Math.max(grabbing - (remainingPermits - this.skipThreshold), 0);
                }
                return grabbing;
            }
            throw new AssertionError();
        }

        protected final PermitStatus permitStatus() {
            if (this.permits.get() > 0) {
                return PermitStatus.MAYBE_MORE;
            }
            return this.unlimited ? PermitStatus.UNLIMITED : PermitStatus.NO_MORE;
        }

        public final T_SPLITR trySplit() {
            T_SPLITR t_splitr = null;
            if (this.permits.get() == 0) {
                return null;
            }
            T_SPLITR split = this.s.trySplit();
            if (split != null) {
                t_splitr = makeSpliterator(split);
            }
            return t_splitr;
        }

        public final long estimateSize() {
            return this.s.estimateSize();
        }

        public final int characteristics() {
            return this.s.characteristics() & -16465;
        }
    }

    static final class WrappingSpliterator<P_IN, P_OUT> extends AbstractWrappingSpliterator<P_IN, P_OUT, SpinedBuffer<P_OUT>> {
        WrappingSpliterator(PipelineHelper<P_OUT> ph, Supplier<Spliterator<P_IN>> supplier, boolean parallel) {
            super((PipelineHelper) ph, (Supplier) supplier, parallel);
        }

        WrappingSpliterator(PipelineHelper<P_OUT> ph, Spliterator<P_IN> spliterator, boolean parallel) {
            super((PipelineHelper) ph, (Spliterator) spliterator, parallel);
        }

        WrappingSpliterator<P_IN, P_OUT> wrap(Spliterator<P_IN> s) {
            return new WrappingSpliterator(this.ph, (Spliterator) s, this.isParallel);
        }

        void initPartialTraversalState() {
            SpinedBuffer<P_OUT> b = new SpinedBuffer();
            this.buffer = b;
            PipelineHelper pipelineHelper = this.ph;
            b.getClass();
            this.bufferSink = pipelineHelper.wrapSink(new AnonymousClass16(b));
            this.pusher = new AnonymousClass8(this);
        }

        /* synthetic */ boolean lambda$-java_util_stream_StreamSpliterators$WrappingSpliterator_10555() {
            return this.spliterator.tryAdvance(this.bufferSink);
        }

        public boolean tryAdvance(Consumer<? super P_OUT> consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext) {
                consumer.-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-1(((SpinedBuffer) this.buffer).get(this.nextToConsume));
            }
            return hasNext;
        }

        public void forEachRemaining(Consumer<? super P_OUT> consumer) {
            if (this.buffer != null || (this.finished ^ 1) == 0) {
                while (tryAdvance(consumer)) {
                }
                return;
            }
            Objects.requireNonNull(consumer);
            init();
            PipelineHelper pipelineHelper = this.ph;
            consumer.getClass();
            pipelineHelper.wrapAndCopyInto(new AnonymousClass15(consumer), this.spliterator);
            this.finished = true;
        }
    }

    StreamSpliterators() {
    }
}
