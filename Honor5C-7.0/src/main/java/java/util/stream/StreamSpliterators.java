package java.util.stream;

import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterator.OfPrimitive;
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
                this.spliterator = (Spliterator) this.spliteratorSupplier.get();
                this.spliteratorSupplier = null;
            }
        }

        final boolean doAdvance() {
            boolean hasNext = false;
            if (this.buffer != null) {
                this.nextToConsume++;
                if (this.nextToConsume < this.buffer.count()) {
                    hasNext = true;
                }
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
            if (!this.isParallel || this.finished) {
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
                if (this.bufferSink.cancellationRequested() || !this.pusher.getAsBoolean()) {
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
                    action.accept(this.array[i]);
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
                    action.accept(this.array[i]);
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
                    action.accept(this.array[i]);
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
                    action.accept(this.array[i]);
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

            public /* bridge */ /* synthetic */ boolean tryAdvance(DoubleConsumer consumer) {
                return tryAdvance(consumer);
            }

            public /* bridge */ /* synthetic */ void forEachRemaining(DoubleConsumer consumer) {
                forEachRemaining(consumer);
            }

            OfDouble(Supplier<java.util.Spliterator.OfDouble> supplier) {
                super(supplier);
            }
        }

        static final class OfInt extends OfPrimitive<Integer, IntConsumer, java.util.Spliterator.OfInt> implements java.util.Spliterator.OfInt {
            public /* bridge */ /* synthetic */ java.util.Spliterator.OfInt trySplit() {
                return (java.util.Spliterator.OfInt) trySplit();
            }

            public /* bridge */ /* synthetic */ boolean tryAdvance(IntConsumer consumer) {
                return tryAdvance(consumer);
            }

            public /* bridge */ /* synthetic */ void forEachRemaining(IntConsumer consumer) {
                forEachRemaining(consumer);
            }

            OfInt(Supplier<java.util.Spliterator.OfInt> supplier) {
                super(supplier);
            }
        }

        static final class OfLong extends OfPrimitive<Long, LongConsumer, java.util.Spliterator.OfLong> implements java.util.Spliterator.OfLong {
            public /* bridge */ /* synthetic */ java.util.Spliterator.OfLong trySplit() {
                return (java.util.Spliterator.OfLong) trySplit();
            }

            public /* bridge */ /* synthetic */ boolean tryAdvance(LongConsumer consumer) {
                return tryAdvance(consumer);
            }

            public /* bridge */ /* synthetic */ void forEachRemaining(LongConsumer consumer) {
                forEachRemaining(consumer);
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
                this.s = (Spliterator) this.supplier.get();
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
        private static final Object NULL_VALUE = null;
        private final Spliterator<T> s;
        private final ConcurrentHashMap<T, Boolean> seen;
        private T tmpSlot;

        final /* synthetic */ class -void_forEachRemaining_java_util_function_Consumer_action_LambdaImpl0 implements Consumer {
            private /* synthetic */ Consumer val$action;
            private /* synthetic */ DistinctSpliterator val$this;

            public /* synthetic */ -void_forEachRemaining_java_util_function_Consumer_action_LambdaImpl0(DistinctSpliterator distinctSpliterator, Consumer consumer) {
                this.val$this = distinctSpliterator;
                this.val$action = consumer;
            }

            public void accept(Object arg0) {
                this.val$this.-java_util_stream_StreamSpliterators$DistinctSpliterator_lambda$18(this.val$action, arg0);
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.StreamSpliterators.DistinctSpliterator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.stream.StreamSpliterators.DistinctSpliterator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.StreamSpliterators.DistinctSpliterator.<clinit>():void");
        }

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
                    action.accept(this.tmpSlot);
                    this.tmpSlot = null;
                    return true;
                }
            }
            return false;
        }

        public void forEachRemaining(Consumer<? super T> action) {
            this.s.forEachRemaining(new -void_forEachRemaining_java_util_function_Consumer_action_LambdaImpl0(this, action));
        }

        /* synthetic */ void -java_util_stream_StreamSpliterators$DistinctSpliterator_lambda$18(Consumer action, Object t) {
            if (this.seen.putIfAbsent(mapNull(t), Boolean.TRUE) == null) {
                action.accept(t);
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

        final /* synthetic */ class -void_forEachRemaining_java_util_function_DoubleConsumer_consumer_LambdaImpl0 implements Sink.OfDouble {
            private /* synthetic */ DoubleConsumer val$-lambdaCtx;

            public /* synthetic */ -void_forEachRemaining_java_util_function_DoubleConsumer_consumer_LambdaImpl0(DoubleConsumer doubleConsumer) {
                this.val$-lambdaCtx = doubleConsumer;
            }

            public void accept(double arg0) {
                this.val$-lambdaCtx.accept(arg0);
            }
        }

        final /* synthetic */ class -void_initPartialTraversalState__LambdaImpl0 implements Sink.OfDouble {
            private /* synthetic */ OfDouble val$-lambdaCtx;

            public /* synthetic */ -void_initPartialTraversalState__LambdaImpl0(OfDouble ofDouble) {
                this.val$-lambdaCtx = ofDouble;
            }

            public void accept(double arg0) {
                this.val$-lambdaCtx.accept(arg0);
            }
        }

        final /* synthetic */ class -void_initPartialTraversalState__LambdaImpl1 implements BooleanSupplier {
            private /* synthetic */ DoubleWrappingSpliterator val$this;

            public /* synthetic */ -void_initPartialTraversalState__LambdaImpl1(DoubleWrappingSpliterator doubleWrappingSpliterator) {
                this.val$this = doubleWrappingSpliterator;
            }

            public boolean getAsBoolean() {
                return this.val$this.-java_util_stream_StreamSpliterators$DoubleWrappingSpliterator_lambda$11();
            }
        }

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
            this.bufferSink = pipelineHelper.wrapSink(new -void_initPartialTraversalState__LambdaImpl0(b));
            this.pusher = new -void_initPartialTraversalState__LambdaImpl1();
        }

        /* synthetic */ boolean -java_util_stream_StreamSpliterators$DoubleWrappingSpliterator_lambda$11() {
            return this.spliterator.tryAdvance(this.bufferSink);
        }

        public /* bridge */ /* synthetic */ OfPrimitive m67trySplit() {
            return trySplit();
        }

        public /* bridge */ /* synthetic */ Spliterator m68trySplit() {
            return trySplit();
        }

        public Spliterator.OfDouble trySplit() {
            return (Spliterator.OfDouble) super.trySplit();
        }

        public /* bridge */ /* synthetic */ boolean tryAdvance(Object consumer) {
            return tryAdvance((DoubleConsumer) consumer);
        }

        public boolean tryAdvance(DoubleConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext) {
                consumer.accept(((OfDouble) this.buffer).get(this.nextToConsume));
            }
            return hasNext;
        }

        public /* bridge */ /* synthetic */ void forEachRemaining(Object consumer) {
            forEachRemaining((DoubleConsumer) consumer);
        }

        public void forEachRemaining(DoubleConsumer consumer) {
            if (this.buffer != null || this.finished) {
                do {
                } while (tryAdvance(consumer));
                return;
            }
            Objects.requireNonNull(consumer);
            init();
            PipelineHelper pipelineHelper = this.ph;
            consumer.getClass();
            pipelineHelper.wrapAndCopyInto(new -void_forEachRemaining_java_util_function_DoubleConsumer_consumer_LambdaImpl0(consumer), this.spliterator);
            this.finished = true;
        }
    }

    static abstract class InfiniteSupplyingSpliterator<T> implements Spliterator<T> {
        long estimate;

        static final class OfDouble extends InfiniteSupplyingSpliterator<Double> implements java.util.Spliterator.OfDouble {
            final DoubleSupplier s;

            public /* bridge */ /* synthetic */ void forEachRemaining(Object action) {
                forEachRemaining((DoubleConsumer) action);
            }

            OfDouble(long size, DoubleSupplier s) {
                super(size);
                this.s = s;
            }

            public /* bridge */ /* synthetic */ boolean tryAdvance(Object action) {
                return tryAdvance((DoubleConsumer) action);
            }

            public boolean tryAdvance(DoubleConsumer action) {
                Objects.requireNonNull(action);
                action.accept(this.s.getAsDouble());
                return true;
            }

            public /* bridge */ /* synthetic */ OfPrimitive m69trySplit() {
                return trySplit();
            }

            public /* bridge */ /* synthetic */ Spliterator m70trySplit() {
                return trySplit();
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

            public /* bridge */ /* synthetic */ void forEachRemaining(Object action) {
                forEachRemaining((IntConsumer) action);
            }

            OfInt(long size, IntSupplier s) {
                super(size);
                this.s = s;
            }

            public /* bridge */ /* synthetic */ boolean tryAdvance(Object action) {
                return tryAdvance((IntConsumer) action);
            }

            public boolean tryAdvance(IntConsumer action) {
                Objects.requireNonNull(action);
                action.accept(this.s.getAsInt());
                return true;
            }

            public /* bridge */ /* synthetic */ OfPrimitive m71trySplit() {
                return trySplit();
            }

            public /* bridge */ /* synthetic */ Spliterator m72trySplit() {
                return trySplit();
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

            public /* bridge */ /* synthetic */ void forEachRemaining(Object action) {
                forEachRemaining((LongConsumer) action);
            }

            OfLong(long size, LongSupplier s) {
                super(size);
                this.s = s;
            }

            public /* bridge */ /* synthetic */ boolean tryAdvance(Object action) {
                return tryAdvance((LongConsumer) action);
            }

            public boolean tryAdvance(LongConsumer action) {
                Objects.requireNonNull(action);
                action.accept(this.s.getAsLong());
                return true;
            }

            public /* bridge */ /* synthetic */ OfPrimitive m73trySplit() {
                return trySplit();
            }

            public /* bridge */ /* synthetic */ Spliterator m74trySplit() {
                return trySplit();
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
                action.accept(this.s.get());
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
            return Record.maxExpansion;
        }
    }

    static final class IntWrappingSpliterator<P_IN> extends AbstractWrappingSpliterator<P_IN, Integer, OfInt> implements Spliterator.OfInt {

        final /* synthetic */ class -void_forEachRemaining_java_util_function_IntConsumer_consumer_LambdaImpl0 implements Sink.OfInt {
            private /* synthetic */ IntConsumer val$-lambdaCtx;

            public /* synthetic */ -void_forEachRemaining_java_util_function_IntConsumer_consumer_LambdaImpl0(IntConsumer intConsumer) {
                this.val$-lambdaCtx = intConsumer;
            }

            public void accept(int arg0) {
                this.val$-lambdaCtx.accept(arg0);
            }
        }

        final /* synthetic */ class -void_initPartialTraversalState__LambdaImpl0 implements Sink.OfInt {
            private /* synthetic */ OfInt val$-lambdaCtx;

            public /* synthetic */ -void_initPartialTraversalState__LambdaImpl0(OfInt ofInt) {
                this.val$-lambdaCtx = ofInt;
            }

            public void accept(int arg0) {
                this.val$-lambdaCtx.accept(arg0);
            }
        }

        final /* synthetic */ class -void_initPartialTraversalState__LambdaImpl1 implements BooleanSupplier {
            private /* synthetic */ IntWrappingSpliterator val$this;

            public /* synthetic */ -void_initPartialTraversalState__LambdaImpl1(IntWrappingSpliterator intWrappingSpliterator) {
                this.val$this = intWrappingSpliterator;
            }

            public boolean getAsBoolean() {
                return this.val$this.-java_util_stream_StreamSpliterators$IntWrappingSpliterator_lambda$5();
            }
        }

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
            this.bufferSink = pipelineHelper.wrapSink(new -void_initPartialTraversalState__LambdaImpl0(b));
            this.pusher = new -void_initPartialTraversalState__LambdaImpl1();
        }

        /* synthetic */ boolean -java_util_stream_StreamSpliterators$IntWrappingSpliterator_lambda$5() {
            return this.spliterator.tryAdvance(this.bufferSink);
        }

        public /* bridge */ /* synthetic */ OfPrimitive m75trySplit() {
            return trySplit();
        }

        public /* bridge */ /* synthetic */ Spliterator m76trySplit() {
            return trySplit();
        }

        public Spliterator.OfInt trySplit() {
            return (Spliterator.OfInt) super.trySplit();
        }

        public /* bridge */ /* synthetic */ boolean tryAdvance(Object consumer) {
            return tryAdvance((IntConsumer) consumer);
        }

        public boolean tryAdvance(IntConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext) {
                consumer.accept(((OfInt) this.buffer).get(this.nextToConsume));
            }
            return hasNext;
        }

        public /* bridge */ /* synthetic */ void forEachRemaining(Object consumer) {
            forEachRemaining((IntConsumer) consumer);
        }

        public void forEachRemaining(IntConsumer consumer) {
            if (this.buffer != null || this.finished) {
                do {
                } while (tryAdvance(consumer));
                return;
            }
            Objects.requireNonNull(consumer);
            init();
            PipelineHelper pipelineHelper = this.ph;
            consumer.getClass();
            pipelineHelper.wrapAndCopyInto(new -void_forEachRemaining_java_util_function_IntConsumer_consumer_LambdaImpl0(consumer), this.spliterator);
            this.finished = true;
        }
    }

    static final class LongWrappingSpliterator<P_IN> extends AbstractWrappingSpliterator<P_IN, Long, OfLong> implements Spliterator.OfLong {

        final /* synthetic */ class -void_forEachRemaining_java_util_function_LongConsumer_consumer_LambdaImpl0 implements Sink.OfLong {
            private /* synthetic */ LongConsumer val$-lambdaCtx;

            public /* synthetic */ -void_forEachRemaining_java_util_function_LongConsumer_consumer_LambdaImpl0(LongConsumer longConsumer) {
                this.val$-lambdaCtx = longConsumer;
            }

            public void accept(long arg0) {
                this.val$-lambdaCtx.accept(arg0);
            }
        }

        final /* synthetic */ class -void_initPartialTraversalState__LambdaImpl0 implements Sink.OfLong {
            private /* synthetic */ OfLong val$-lambdaCtx;

            public /* synthetic */ -void_initPartialTraversalState__LambdaImpl0(OfLong ofLong) {
                this.val$-lambdaCtx = ofLong;
            }

            public void accept(long arg0) {
                this.val$-lambdaCtx.accept(arg0);
            }
        }

        final /* synthetic */ class -void_initPartialTraversalState__LambdaImpl1 implements BooleanSupplier {
            private /* synthetic */ LongWrappingSpliterator val$this;

            public /* synthetic */ -void_initPartialTraversalState__LambdaImpl1(LongWrappingSpliterator longWrappingSpliterator) {
                this.val$this = longWrappingSpliterator;
            }

            public boolean getAsBoolean() {
                return this.val$this.-java_util_stream_StreamSpliterators$LongWrappingSpliterator_lambda$8();
            }
        }

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
            this.bufferSink = pipelineHelper.wrapSink(new -void_initPartialTraversalState__LambdaImpl0(b));
            this.pusher = new -void_initPartialTraversalState__LambdaImpl1();
        }

        /* synthetic */ boolean -java_util_stream_StreamSpliterators$LongWrappingSpliterator_lambda$8() {
            return this.spliterator.tryAdvance(this.bufferSink);
        }

        public /* bridge */ /* synthetic */ OfPrimitive m77trySplit() {
            return trySplit();
        }

        public /* bridge */ /* synthetic */ Spliterator m78trySplit() {
            return trySplit();
        }

        public Spliterator.OfLong trySplit() {
            return (Spliterator.OfLong) super.trySplit();
        }

        public /* bridge */ /* synthetic */ boolean tryAdvance(Object consumer) {
            return tryAdvance((LongConsumer) consumer);
        }

        public boolean tryAdvance(LongConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext) {
                consumer.accept(((OfLong) this.buffer).get(this.nextToConsume));
            }
            return hasNext;
        }

        public /* bridge */ /* synthetic */ void forEachRemaining(Object consumer) {
            forEachRemaining((LongConsumer) consumer);
        }

        public void forEachRemaining(LongConsumer consumer) {
            if (this.buffer != null || this.finished) {
                do {
                } while (tryAdvance(consumer));
                return;
            }
            Objects.requireNonNull(consumer);
            init();
            PipelineHelper pipelineHelper = this.ph;
            consumer.getClass();
            pipelineHelper.wrapAndCopyInto(new -void_forEachRemaining_java_util_function_LongConsumer_consumer_LambdaImpl0(consumer), this.spliterator);
            this.finished = true;
        }
    }

    static abstract class SliceSpliterator<T, T_SPLITR extends Spliterator<T>> {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        long fence;
        long index;
        T_SPLITR s;
        final long sliceFence;
        final long sliceOrigin;

        static abstract class OfPrimitive<T, T_SPLITR extends java.util.Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>, T_CONS> extends SliceSpliterator<T, T_SPLITR> implements java.util.Spliterator.OfPrimitive<T, T_CONS, T_SPLITR> {
            /* synthetic */ OfPrimitive(java.util.Spliterator.OfPrimitive s, long sliceOrigin, long sliceFence, long origin, long fence, OfPrimitive ofPrimitive) {
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

            final /* synthetic */ class -java_util_function_DoubleConsumer_emptyConsumer__LambdaImpl0 implements DoubleConsumer {
                public /* synthetic */ -java_util_function_DoubleConsumer_emptyConsumer__LambdaImpl0() {
                }

                public void accept(double arg0) {
                    OfDouble.-java_util_stream_StreamSpliterators$SliceSpliterator$OfDouble_lambda$17(arg0);
                }
            }

            static /* synthetic */ void -java_util_stream_StreamSpliterators$SliceSpliterator$OfDouble_lambda$17(double e) {
            }

            public /* bridge */ /* synthetic */ java.util.Spliterator.OfDouble trySplit() {
                return (java.util.Spliterator.OfDouble) trySplit();
            }

            public /* bridge */ /* synthetic */ boolean tryAdvance(DoubleConsumer action) {
                return tryAdvance(action);
            }

            public /* bridge */ /* synthetic */ void forEachRemaining(DoubleConsumer action) {
                forEachRemaining(action);
            }

            OfDouble(java.util.Spliterator.OfDouble s, long sliceOrigin, long sliceFence) {
                super(s, sliceOrigin, sliceFence);
            }

            OfDouble(java.util.Spliterator.OfDouble s, long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence, null);
            }

            protected /* bridge */ /* synthetic */ Spliterator makeSpliterator(Spliterator s, long sliceOrigin, long sliceFence, long origin, long fence) {
                return makeSpliterator((java.util.Spliterator.OfDouble) s, sliceOrigin, sliceFence, origin, fence);
            }

            protected java.util.Spliterator.OfDouble makeSpliterator(java.util.Spliterator.OfDouble s, long sliceOrigin, long sliceFence, long origin, long fence) {
                return new OfDouble(s, sliceOrigin, sliceFence, origin, fence);
            }

            protected /* bridge */ /* synthetic */ Object emptyConsumer() {
                return emptyConsumer();
            }

            protected DoubleConsumer m79emptyConsumer() {
                return new -java_util_function_DoubleConsumer_emptyConsumer__LambdaImpl0();
            }
        }

        static final class OfInt extends OfPrimitive<Integer, java.util.Spliterator.OfInt, IntConsumer> implements java.util.Spliterator.OfInt {

            final /* synthetic */ class -java_util_function_IntConsumer_emptyConsumer__LambdaImpl0 implements IntConsumer {
                public /* synthetic */ -java_util_function_IntConsumer_emptyConsumer__LambdaImpl0() {
                }

                public void accept(int arg0) {
                    OfInt.-java_util_stream_StreamSpliterators$SliceSpliterator$OfInt_lambda$15(arg0);
                }
            }

            static /* synthetic */ void -java_util_stream_StreamSpliterators$SliceSpliterator$OfInt_lambda$15(int e) {
            }

            public /* bridge */ /* synthetic */ java.util.Spliterator.OfInt trySplit() {
                return (java.util.Spliterator.OfInt) trySplit();
            }

            public /* bridge */ /* synthetic */ boolean tryAdvance(IntConsumer action) {
                return tryAdvance(action);
            }

            public /* bridge */ /* synthetic */ void forEachRemaining(IntConsumer action) {
                forEachRemaining(action);
            }

            OfInt(java.util.Spliterator.OfInt s, long sliceOrigin, long sliceFence) {
                super(s, sliceOrigin, sliceFence);
            }

            OfInt(java.util.Spliterator.OfInt s, long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence, null);
            }

            protected /* bridge */ /* synthetic */ Spliterator makeSpliterator(Spliterator s, long sliceOrigin, long sliceFence, long origin, long fence) {
                return makeSpliterator((java.util.Spliterator.OfInt) s, sliceOrigin, sliceFence, origin, fence);
            }

            protected java.util.Spliterator.OfInt makeSpliterator(java.util.Spliterator.OfInt s, long sliceOrigin, long sliceFence, long origin, long fence) {
                return new OfInt(s, sliceOrigin, sliceFence, origin, fence);
            }

            protected /* bridge */ /* synthetic */ Object emptyConsumer() {
                return emptyConsumer();
            }

            protected IntConsumer m80emptyConsumer() {
                return new -java_util_function_IntConsumer_emptyConsumer__LambdaImpl0();
            }
        }

        static final class OfLong extends OfPrimitive<Long, java.util.Spliterator.OfLong, LongConsumer> implements java.util.Spliterator.OfLong {

            final /* synthetic */ class -java_util_function_LongConsumer_emptyConsumer__LambdaImpl0 implements LongConsumer {
                public /* synthetic */ -java_util_function_LongConsumer_emptyConsumer__LambdaImpl0() {
                }

                public void accept(long arg0) {
                    OfLong.-java_util_stream_StreamSpliterators$SliceSpliterator$OfLong_lambda$16(arg0);
                }
            }

            static /* synthetic */ void -java_util_stream_StreamSpliterators$SliceSpliterator$OfLong_lambda$16(long e) {
            }

            public /* bridge */ /* synthetic */ java.util.Spliterator.OfLong trySplit() {
                return (java.util.Spliterator.OfLong) trySplit();
            }

            public /* bridge */ /* synthetic */ boolean tryAdvance(LongConsumer action) {
                return tryAdvance(action);
            }

            public /* bridge */ /* synthetic */ void forEachRemaining(LongConsumer action) {
                forEachRemaining(action);
            }

            OfLong(java.util.Spliterator.OfLong s, long sliceOrigin, long sliceFence) {
                super(s, sliceOrigin, sliceFence);
            }

            OfLong(java.util.Spliterator.OfLong s, long sliceOrigin, long sliceFence, long origin, long fence) {
                super(s, sliceOrigin, sliceFence, origin, fence, null);
            }

            protected /* bridge */ /* synthetic */ Spliterator makeSpliterator(Spliterator s, long sliceOrigin, long sliceFence, long origin, long fence) {
                return makeSpliterator((java.util.Spliterator.OfLong) s, sliceOrigin, sliceFence, origin, fence);
            }

            protected java.util.Spliterator.OfLong makeSpliterator(java.util.Spliterator.OfLong s, long sliceOrigin, long sliceFence, long origin, long fence) {
                return new OfLong(s, sliceOrigin, sliceFence, origin, fence);
            }

            protected /* bridge */ /* synthetic */ Object emptyConsumer() {
                return emptyConsumer();
            }

            protected LongConsumer m81emptyConsumer() {
                return new -java_util_function_LongConsumer_emptyConsumer__LambdaImpl0();
            }
        }

        static final class OfRef<T> extends SliceSpliterator<T, Spliterator<T>> implements Spliterator<T> {

            final /* synthetic */ class -boolean_tryAdvance_java_util_function_Consumer_action_LambdaImpl0 implements Consumer {
                public /* synthetic */ -boolean_tryAdvance_java_util_function_Consumer_action_LambdaImpl0() {
                }

                public void accept(Object arg0) {
                    OfRef.-java_util_stream_StreamSpliterators$SliceSpliterator$OfRef_lambda$13(arg0);
                }
            }

            final /* synthetic */ class -void_forEachRemaining_java_util_function_Consumer_action_LambdaImpl0 implements Consumer {
                public /* synthetic */ -void_forEachRemaining_java_util_function_Consumer_action_LambdaImpl0() {
                }

                public void accept(Object arg0) {
                    OfRef.-java_util_stream_StreamSpliterators$SliceSpliterator$OfRef_lambda$14(arg0);
                }
            }

            static /* synthetic */ void -java_util_stream_StreamSpliterators$SliceSpliterator$OfRef_lambda$13(Object e) {
            }

            static /* synthetic */ void -java_util_stream_StreamSpliterators$SliceSpliterator$OfRef_lambda$14(Object e) {
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
                    this.s.tryAdvance(new -boolean_tryAdvance_java_util_function_Consumer_action_LambdaImpl0());
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
                            this.s.tryAdvance(new -void_forEachRemaining_java_util_function_Consumer_action_LambdaImpl0());
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

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.StreamSpliterators.SliceSpliterator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.stream.StreamSpliterators.SliceSpliterator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.StreamSpliterators.SliceSpliterator.<clinit>():void");
        }

        protected abstract T_SPLITR makeSpliterator(T_SPLITR t_splitr, long j, long j2, long j3, long j4);

        SliceSpliterator(T_SPLITR s, long sliceOrigin, long sliceFence, long origin, long fence) {
            if (-assertionsDisabled || s.hasCharacteristics(Record.maxDataSize)) {
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
                long leftSplitFence;
                T_SPLITR leftSplit = this.s.trySplit();
                if (leftSplit != null) {
                    long leftSplitFenceUnbounded = this.index + leftSplit.estimateSize();
                    leftSplitFence = Math.min(leftSplitFenceUnbounded, this.sliceFence);
                    if (this.sliceOrigin < leftSplitFence) {
                        if (leftSplitFence < this.sliceFence) {
                            break;
                        }
                        this.s = leftSplit;
                        this.fence = leftSplitFence;
                    } else {
                        this.index = leftSplitFence;
                    }
                } else {
                    return null;
                }
            }
            if (this.index < this.sliceOrigin || leftSplitFenceUnbounded > this.sliceFence) {
                long j = this.sliceOrigin;
                long j2 = this.sliceFence;
                long j3 = this.index;
                this.index = leftSplitFence;
                return makeSpliterator(leftSplit, j, j2, j3, leftSplitFence);
            }
            this.index = leftSplitFence;
            return leftSplit;
        }

        public long estimateSize() {
            return this.sliceOrigin < this.fence ? this.fence - Math.max(this.sliceOrigin, this.index) : 0;
        }

        public int characteristics() {
            return this.s.characteristics();
        }
    }

    static abstract class UnorderedSliceSpliterator<T, T_SPLITR extends Spliterator<T>> {
        static final /* synthetic */ boolean -assertionsDisabled = false;
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
                T_CONS consumer = this;
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
                            ofPrimitive = bufferCreate(UnorderedSliceSpliterator.CHUNK_SIZE);
                        } else {
                            ofPrimitive.reset();
                        }
                        T_CONS sbc = r1;
                        long permitsRequested = 0;
                        while (((java.util.Spliterator.OfPrimitive) this.s).tryAdvance(sbc)) {
                            permitsRequested++;
                            if (permitsRequested >= 128) {
                                break;
                            }
                        }
                        if (permitsRequested != 0) {
                            r1.forEach(action, acquirePermits(permitsRequested));
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

            public /* bridge */ /* synthetic */ boolean tryAdvance(DoubleConsumer action) {
                return tryAdvance(action);
            }

            public /* bridge */ /* synthetic */ void forEachRemaining(DoubleConsumer action) {
                forEachRemaining(action);
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

            protected /* bridge */ /* synthetic */ void acceptConsumed(Object action) {
                acceptConsumed((DoubleConsumer) action);
            }

            protected void acceptConsumed(DoubleConsumer action) {
                action.accept(this.tmpValue);
            }

            protected /* bridge */ /* synthetic */ OfPrimitive m82bufferCreate(int initialCapacity) {
                return bufferCreate(initialCapacity);
            }

            protected OfDouble bufferCreate(int initialCapacity) {
                return new OfDouble(initialCapacity);
            }

            protected /* bridge */ /* synthetic */ Spliterator makeSpliterator(Spliterator s) {
                return makeSpliterator((java.util.Spliterator.OfDouble) s);
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

            public /* bridge */ /* synthetic */ boolean tryAdvance(IntConsumer action) {
                return tryAdvance(action);
            }

            public /* bridge */ /* synthetic */ void forEachRemaining(IntConsumer action) {
                forEachRemaining(action);
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

            protected /* bridge */ /* synthetic */ void acceptConsumed(Object action) {
                acceptConsumed((IntConsumer) action);
            }

            protected void acceptConsumed(IntConsumer action) {
                action.accept(this.tmpValue);
            }

            protected /* bridge */ /* synthetic */ OfPrimitive m83bufferCreate(int initialCapacity) {
                return bufferCreate(initialCapacity);
            }

            protected OfInt bufferCreate(int initialCapacity) {
                return new OfInt(initialCapacity);
            }

            protected /* bridge */ /* synthetic */ Spliterator makeSpliterator(Spliterator s) {
                return makeSpliterator((java.util.Spliterator.OfInt) s);
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

            public /* bridge */ /* synthetic */ boolean tryAdvance(LongConsumer action) {
                return tryAdvance(action);
            }

            public /* bridge */ /* synthetic */ void forEachRemaining(LongConsumer action) {
                forEachRemaining(action);
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

            protected /* bridge */ /* synthetic */ void acceptConsumed(Object action) {
                acceptConsumed((LongConsumer) action);
            }

            protected void acceptConsumed(LongConsumer action) {
                action.accept(this.tmpValue);
            }

            protected /* bridge */ /* synthetic */ OfPrimitive m84bufferCreate(int initialCapacity) {
                return bufferCreate(initialCapacity);
            }

            protected OfLong bufferCreate(int initialCapacity) {
                return new OfLong(initialCapacity);
            }

            protected /* bridge */ /* synthetic */ Spliterator makeSpliterator(Spliterator s) {
                return makeSpliterator((java.util.Spliterator.OfLong) s);
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
                        action.accept(this.tmpSlot);
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
                            ofRef = new OfRef(UnorderedSliceSpliterator.CHUNK_SIZE);
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
            ;

            static {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.StreamSpliterators.UnorderedSliceSpliterator.PermitStatus.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.stream.StreamSpliterators.UnorderedSliceSpliterator.PermitStatus.<clinit>():void
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
                throw new UnsupportedOperationException("Method not decompiled: java.util.stream.StreamSpliterators.UnorderedSliceSpliterator.PermitStatus.<clinit>():void");
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.StreamSpliterators.UnorderedSliceSpliterator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.stream.StreamSpliterators.UnorderedSliceSpliterator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.StreamSpliterators.UnorderedSliceSpliterator.<clinit>():void");
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
            long grabbing;
            if (!-assertionsDisabled) {
                if ((numElements > 0 ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            long remainingPermits;
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
            } while (!this.permits.compareAndSet(remainingPermits, remainingPermits - grabbing));
            if (this.unlimited) {
                return Math.max(numElements - grabbing, 0);
            }
            if (remainingPermits > this.skipThreshold) {
                return Math.max(grabbing - (remainingPermits - this.skipThreshold), 0);
            }
            return grabbing;
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

        final /* synthetic */ class -void_forEachRemaining_java_util_function_Consumer_consumer_LambdaImpl0 implements Sink {
            private /* synthetic */ Consumer val$-lambdaCtx;

            public /* synthetic */ -void_forEachRemaining_java_util_function_Consumer_consumer_LambdaImpl0(Consumer consumer) {
                this.val$-lambdaCtx = consumer;
            }

            public void accept(Object arg0) {
                this.val$-lambdaCtx.accept(arg0);
            }
        }

        final /* synthetic */ class -void_initPartialTraversalState__LambdaImpl0 implements Sink {
            private /* synthetic */ SpinedBuffer val$-lambdaCtx;

            public /* synthetic */ -void_initPartialTraversalState__LambdaImpl0(SpinedBuffer spinedBuffer) {
                this.val$-lambdaCtx = spinedBuffer;
            }

            public void accept(Object arg0) {
                this.val$-lambdaCtx.accept(arg0);
            }
        }

        final /* synthetic */ class -void_initPartialTraversalState__LambdaImpl1 implements BooleanSupplier {
            private /* synthetic */ WrappingSpliterator val$this;

            public /* synthetic */ -void_initPartialTraversalState__LambdaImpl1(WrappingSpliterator wrappingSpliterator) {
                this.val$this = wrappingSpliterator;
            }

            public boolean getAsBoolean() {
                return this.val$this.-java_util_stream_StreamSpliterators$WrappingSpliterator_lambda$2();
            }
        }

        WrappingSpliterator(PipelineHelper<P_OUT> ph, Supplier<Spliterator<P_IN>> supplier, boolean parallel) {
            super((PipelineHelper) ph, (Supplier) supplier, parallel);
        }

        WrappingSpliterator(PipelineHelper<P_OUT> ph, Spliterator<P_IN> spliterator, boolean parallel) {
            super((PipelineHelper) ph, (Spliterator) spliterator, parallel);
        }

        /* bridge */ /* synthetic */ AbstractWrappingSpliterator wrap(Spliterator s) {
            return wrap(s);
        }

        WrappingSpliterator<P_IN, P_OUT> m85wrap(Spliterator<P_IN> s) {
            return new WrappingSpliterator(this.ph, (Spliterator) s, this.isParallel);
        }

        void initPartialTraversalState() {
            SpinedBuffer<P_OUT> b = new SpinedBuffer();
            this.buffer = b;
            PipelineHelper pipelineHelper = this.ph;
            b.getClass();
            this.bufferSink = pipelineHelper.wrapSink(new -void_initPartialTraversalState__LambdaImpl0(b));
            this.pusher = new -void_initPartialTraversalState__LambdaImpl1();
        }

        /* synthetic */ boolean -java_util_stream_StreamSpliterators$WrappingSpliterator_lambda$2() {
            return this.spliterator.tryAdvance(this.bufferSink);
        }

        public boolean tryAdvance(Consumer<? super P_OUT> consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext) {
                consumer.accept(((SpinedBuffer) this.buffer).get(this.nextToConsume));
            }
            return hasNext;
        }

        public void forEachRemaining(Consumer<? super P_OUT> consumer) {
            if (this.buffer != null || this.finished) {
                do {
                } while (tryAdvance(consumer));
                return;
            }
            Objects.requireNonNull(consumer);
            init();
            PipelineHelper pipelineHelper = this.ph;
            consumer.getClass();
            pipelineHelper.wrapAndCopyInto(new -void_forEachRemaining_java_util_function_Consumer_consumer_LambdaImpl0(consumer), this.spliterator);
            this.finished = true;
        }
    }

    StreamSpliterators() {
    }
}
