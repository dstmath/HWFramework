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
    static final Object NONE = null;

    /* renamed from: java.util.stream.Streams.1 */
    static class AnonymousClass1 implements Runnable {
        final /* synthetic */ Runnable val$a;
        final /* synthetic */ Runnable val$b;

        AnonymousClass1(Runnable val$a, Runnable val$b) {
            this.val$a = val$a;
            this.val$b = val$b;
        }

        public void run() {
            try {
                this.val$a.run();
                this.val$b.run();
            } catch (Throwable e2) {
                try {
                    e1.addSuppressed(e2);
                } catch (Throwable th) {
                }
            }
        }
    }

    /* renamed from: java.util.stream.Streams.2 */
    static class AnonymousClass2 implements Runnable {
        final /* synthetic */ BaseStream val$a;
        final /* synthetic */ BaseStream val$b;

        AnonymousClass2(BaseStream val$a, BaseStream val$b) {
            this.val$a = val$a;
            this.val$b = val$b;
        }

        public void run() {
            try {
                this.val$a.close();
                this.val$b.close();
            } catch (Throwable e2) {
                try {
                    e1.addSuppressed(e2);
                } catch (Throwable th) {
                }
            }
        }
    }

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
        boolean beforeSplit;
        final boolean unsized;

        private static abstract class OfPrimitive<T, T_CONS, T_SPLITR extends java.util.Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>> extends ConcatSpliterator<T, T_SPLITR> implements java.util.Spliterator.OfPrimitive<T, T_CONS, T_SPLITR> {
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

            public /* bridge */ /* synthetic */ boolean tryAdvance(DoubleConsumer action) {
                return tryAdvance(action);
            }

            public /* bridge */ /* synthetic */ void forEachRemaining(DoubleConsumer action) {
                forEachRemaining(action);
            }

            OfDouble(java.util.Spliterator.OfDouble aSpliterator, java.util.Spliterator.OfDouble bSpliterator) {
                super(bSpliterator, null);
            }
        }

        static class OfInt extends OfPrimitive<Integer, IntConsumer, java.util.Spliterator.OfInt> implements java.util.Spliterator.OfInt {
            public /* bridge */ /* synthetic */ java.util.Spliterator.OfInt trySplit() {
                return (java.util.Spliterator.OfInt) trySplit();
            }

            public /* bridge */ /* synthetic */ boolean tryAdvance(IntConsumer action) {
                return tryAdvance(action);
            }

            public /* bridge */ /* synthetic */ void forEachRemaining(IntConsumer action) {
                forEachRemaining(action);
            }

            OfInt(java.util.Spliterator.OfInt aSpliterator, java.util.Spliterator.OfInt bSpliterator) {
                super(bSpliterator, null);
            }
        }

        static class OfLong extends OfPrimitive<Long, LongConsumer, java.util.Spliterator.OfLong> implements java.util.Spliterator.OfLong {
            public /* bridge */ /* synthetic */ java.util.Spliterator.OfLong trySplit() {
                return (java.util.Spliterator.OfLong) trySplit();
            }

            public /* bridge */ /* synthetic */ boolean tryAdvance(LongConsumer action) {
                return tryAdvance(action);
            }

            public /* bridge */ /* synthetic */ void forEachRemaining(LongConsumer action) {
                forEachRemaining(action);
            }

            OfLong(java.util.Spliterator.OfLong aSpliterator, java.util.Spliterator.OfLong bSpliterator) {
                super(bSpliterator, null);
            }
        }

        static class OfRef<T> extends ConcatSpliterator<T, Spliterator<T>> {
            OfRef(Spliterator<T> aSpliterator, Spliterator<T> bSpliterator) {
                super(aSpliterator, bSpliterator);
            }
        }

        public int characteristics() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.Streams.ConcatSpliterator.characteristics():int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 8 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.Streams.ConcatSpliterator.characteristics():int");
        }

        public ConcatSpliterator(T_SPLITR aSpliterator, T_SPLITR bSpliterator) {
            boolean z = true;
            this.aSpliterator = aSpliterator;
            this.bSpliterator = bSpliterator;
            this.beforeSplit = true;
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
            } else {
                throw new IllegalStateException();
            }
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
            } else {
                throw new IllegalStateException();
            }
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
            } else {
                throw new IllegalStateException();
            }
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
            int i2 = i;
            while (i2 < hUpTo) {
                i = i2 + 1;
                consumer.accept(i2);
                i2 = i;
            }
            if (hLast > 0) {
                consumer.accept(i2);
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
            return (int) (size / ((long) (size < 16777216 ? 2 : RIGHT_BALANCED_SPLIT_RATIO)));
        }
    }

    static final class RangeLongSpliterator implements OfLong {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        private static final long BALANCED_SPLIT_THRESHOLD = 16777216;
        private static final long RIGHT_BALANCED_SPLIT_RATIO = 8;
        private long from;
        private int last;
        private final long upTo;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.Streams.RangeLongSpliterator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.stream.Streams.RangeLongSpliterator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.Streams.RangeLongSpliterator.<clinit>():void");
        }

        RangeLongSpliterator(long from, long upTo, boolean closed) {
            this(from, upTo, closed ? 1 : 0);
        }

        private RangeLongSpliterator(long from, long upTo, int last) {
            if (!-assertionsDisabled) {
                if (((upTo - from) + ((long) last) > 0 ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            this.from = from;
            this.upTo = upTo;
            this.last = last;
        }

        public boolean tryAdvance(LongConsumer consumer) {
            Objects.requireNonNull(consumer);
            long i = this.from;
            if (i < this.upTo) {
                this.from++;
                consumer.accept(i);
                return true;
            } else if (this.last <= 0) {
                return -assertionsDisabled;
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
            long i2 = i;
            while (i2 < hUpTo) {
                i = i2 + 1;
                consumer.accept(i2);
                i2 = i;
            }
            if (hLast > 0) {
                consumer.accept(i2);
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
            } else {
                throw new IllegalStateException();
            }
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.Streams.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.stream.Streams.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.stream.Streams.<clinit>():void");
    }

    private Streams() {
        throw new Error("no instances");
    }

    static Runnable composeWithExceptions(Runnable a, Runnable b) {
        return new AnonymousClass1(a, b);
    }

    static Runnable composedClose(BaseStream<?, ?> a, BaseStream<?, ?> b) {
        return new AnonymousClass2(a, b);
    }
}
