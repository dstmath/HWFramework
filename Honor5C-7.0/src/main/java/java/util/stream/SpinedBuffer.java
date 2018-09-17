package java.util.stream;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;

public class SpinedBuffer<E> extends AbstractSpinedBuffer implements Consumer<E>, Iterable<E> {
    private static final int SPLITERATOR_CHARACTERISTICS = 16464;
    protected E[] curChunk;
    protected E[][] spine;

    public static abstract class OfPrimitive<E, T_ARR, T_CONS> extends AbstractSpinedBuffer implements Iterable<E> {
        T_ARR curChunk;
        T_ARR[] spine;

        abstract class BaseSpliterator<T_SPLITR extends java.util.Spliterator.OfPrimitive<E, T_CONS, T_SPLITR>> implements java.util.Spliterator.OfPrimitive<E, T_CONS, T_SPLITR> {
            static final /* synthetic */ boolean -assertionsDisabled = false;
            final /* synthetic */ boolean $assertionsDisabled;
            final int lastSpineElementFence;
            final int lastSpineIndex;
            T_ARR splChunk;
            int splElementIndex;
            int splSpineIndex;
            final /* synthetic */ OfPrimitive this$1;

            static {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.SpinedBuffer.OfPrimitive.BaseSpliterator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.stream.SpinedBuffer.OfPrimitive.BaseSpliterator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.stream.SpinedBuffer.OfPrimitive.BaseSpliterator.<clinit>():void");
            }

            abstract void arrayForOne(T_ARR t_arr, int i, T_CONS t_cons);

            abstract T_SPLITR arraySpliterator(T_ARR t_arr, int i, int i2);

            abstract T_SPLITR newSpliterator(int i, int i2, int i3, int i4);

            BaseSpliterator(OfPrimitive this$1, int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                Object obj = 1;
                this.this$1 = this$1;
                this.splSpineIndex = firstSpineIndex;
                this.lastSpineIndex = lastSpineIndex;
                this.splElementIndex = firstSpineElementIndex;
                this.lastSpineElementFence = lastSpineElementFence;
                if (!-assertionsDisabled) {
                    if (this$1.spine == null && !(firstSpineIndex == 0 && lastSpineIndex == 0)) {
                        obj = null;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                if (this$1.spine == null) {
                    obj = this$1.curChunk;
                } else {
                    obj = this$1.spine[firstSpineIndex];
                }
                this.splChunk = obj;
            }

            public long estimateSize() {
                if (this.splSpineIndex == this.lastSpineIndex) {
                    return ((long) this.lastSpineElementFence) - ((long) this.splElementIndex);
                }
                return ((this.this$1.priorElementCount[this.lastSpineIndex] + ((long) this.lastSpineElementFence)) - this.this$1.priorElementCount[this.splSpineIndex]) - ((long) this.splElementIndex);
            }

            public int characteristics() {
                return SpinedBuffer.SPLITERATOR_CHARACTERISTICS;
            }

            public boolean tryAdvance(T_CONS consumer) {
                Objects.requireNonNull(consumer);
                if (this.splSpineIndex >= this.lastSpineIndex && (this.splSpineIndex != this.lastSpineIndex || this.splElementIndex >= this.lastSpineElementFence)) {
                    return false;
                }
                Object obj = this.splChunk;
                int i = this.splElementIndex;
                this.splElementIndex = i + 1;
                arrayForOne(obj, i, consumer);
                if (this.splElementIndex == this.this$1.arrayLength(this.splChunk)) {
                    this.splElementIndex = 0;
                    this.splSpineIndex++;
                    if (this.this$1.spine != null && this.splSpineIndex <= this.lastSpineIndex) {
                        this.splChunk = this.this$1.spine[this.splSpineIndex];
                    }
                }
                return true;
            }

            public void forEachRemaining(T_CONS consumer) {
                Objects.requireNonNull(consumer);
                if (this.splSpineIndex < this.lastSpineIndex || (this.splSpineIndex == this.lastSpineIndex && this.splElementIndex < this.lastSpineElementFence)) {
                    int i = this.splElementIndex;
                    for (int sp = this.splSpineIndex; sp < this.lastSpineIndex; sp++) {
                        T_ARR chunk = this.this$1.spine[sp];
                        this.this$1.arrayForEach(chunk, i, this.this$1.arrayLength(chunk), consumer);
                        i = 0;
                    }
                    this.this$1.arrayForEach(this.splSpineIndex == this.lastSpineIndex ? this.splChunk : this.this$1.spine[this.lastSpineIndex], i, this.lastSpineElementFence, consumer);
                    this.splSpineIndex = this.lastSpineIndex;
                    this.splElementIndex = this.lastSpineElementFence;
                }
            }

            public T_SPLITR trySplit() {
                T_SPLITR ret;
                if (this.splSpineIndex < this.lastSpineIndex) {
                    ret = newSpliterator(this.splSpineIndex, this.lastSpineIndex - 1, this.splElementIndex, this.this$1.arrayLength(this.this$1.spine[this.lastSpineIndex - 1]));
                    this.splSpineIndex = this.lastSpineIndex;
                    this.splElementIndex = 0;
                    this.splChunk = this.this$1.spine[this.splSpineIndex];
                    return ret;
                } else if (this.splSpineIndex != this.lastSpineIndex) {
                    return null;
                } else {
                    int t = (this.lastSpineElementFence - this.splElementIndex) / 2;
                    if (t == 0) {
                        return null;
                    }
                    ret = arraySpliterator(this.splChunk, this.splElementIndex, t);
                    this.splElementIndex += t;
                    return ret;
                }
            }
        }

        protected abstract void arrayForEach(T_ARR t_arr, int i, int i2, T_CONS t_cons);

        protected abstract int arrayLength(T_ARR t_arr);

        public /* bridge */ /* synthetic */ long count() {
            return super.count();
        }

        public abstract void forEach(Consumer<? super E> consumer);

        public /* bridge */ /* synthetic */ boolean isEmpty() {
            return super.isEmpty();
        }

        public abstract Iterator<E> iterator();

        public abstract T_ARR newArray(int i);

        protected abstract T_ARR[] newArrayArray(int i);

        OfPrimitive(int initialCapacity) {
            super(initialCapacity);
            this.curChunk = newArray(1 << this.initialChunkPower);
        }

        OfPrimitive() {
            this.curChunk = newArray(1 << this.initialChunkPower);
        }

        protected long capacity() {
            if (this.spineIndex == 0) {
                return (long) arrayLength(this.curChunk);
            }
            return this.priorElementCount[this.spineIndex] + ((long) arrayLength(this.spine[this.spineIndex]));
        }

        private void inflateSpine() {
            if (this.spine == null) {
                this.spine = newArrayArray(8);
                this.priorElementCount = new long[8];
                this.spine[0] = this.curChunk;
            }
        }

        protected final void ensureCapacity(long targetSize) {
            long capacity = capacity();
            if (targetSize > capacity) {
                inflateSpine();
                int i = this.spineIndex + 1;
                while (targetSize > capacity) {
                    if (i >= this.spine.length) {
                        int newSpineSize = this.spine.length * 2;
                        this.spine = Arrays.copyOf(this.spine, newSpineSize);
                        this.priorElementCount = Arrays.copyOf(this.priorElementCount, newSpineSize);
                    }
                    int nextChunkSize = chunkSize(i);
                    this.spine[i] = newArray(nextChunkSize);
                    this.priorElementCount[i] = this.priorElementCount[i - 1] + ((long) arrayLength(this.spine[i - 1]));
                    capacity += (long) nextChunkSize;
                    i++;
                }
            }
        }

        protected void increaseCapacity() {
            ensureCapacity(capacity() + 1);
        }

        protected int chunkFor(long index) {
            if (this.spineIndex == 0) {
                if (index < ((long) this.elementIndex)) {
                    return 0;
                }
                throw new IndexOutOfBoundsException(Long.toString(index));
            } else if (index >= count()) {
                throw new IndexOutOfBoundsException(Long.toString(index));
            } else {
                for (int j = 0; j <= this.spineIndex; j++) {
                    if (index < this.priorElementCount[j] + ((long) arrayLength(this.spine[j]))) {
                        return j;
                    }
                }
                throw new IndexOutOfBoundsException(Long.toString(index));
            }
        }

        public void copyInto(T_ARR array, int offset) {
            long finalOffset = ((long) offset) + count();
            if (finalOffset > ((long) arrayLength(array)) || finalOffset < ((long) offset)) {
                throw new IndexOutOfBoundsException("does not fit");
            } else if (this.spineIndex == 0) {
                System.arraycopy(this.curChunk, 0, (Object) array, offset, this.elementIndex);
            } else {
                for (int i = 0; i < this.spineIndex; i++) {
                    System.arraycopy(this.spine[i], 0, (Object) array, offset, arrayLength(this.spine[i]));
                    offset += arrayLength(this.spine[i]);
                }
                if (this.elementIndex > 0) {
                    System.arraycopy(this.curChunk, 0, (Object) array, offset, this.elementIndex);
                }
            }
        }

        public T_ARR asPrimitiveArray() {
            long size = count();
            if (size >= 2147483639) {
                throw new IllegalArgumentException("Stream size exceeds max array size");
            }
            T_ARR result = newArray((int) size);
            copyInto(result, 0);
            return result;
        }

        protected void preAccept() {
            if (this.elementIndex == arrayLength(this.curChunk)) {
                inflateSpine();
                if (this.spineIndex + 1 >= this.spine.length || this.spine[this.spineIndex + 1] == null) {
                    increaseCapacity();
                }
                this.elementIndex = 0;
                this.spineIndex++;
                this.curChunk = this.spine[this.spineIndex];
            }
        }

        public void clear() {
            if (this.spine != null) {
                this.curChunk = this.spine[0];
                this.spine = null;
                this.priorElementCount = null;
            }
            this.elementIndex = 0;
            this.spineIndex = 0;
        }

        public void forEach(T_CONS consumer) {
            for (int j = 0; j < this.spineIndex; j++) {
                arrayForEach(this.spine[j], 0, arrayLength(this.spine[j]), consumer);
            }
            arrayForEach(this.curChunk, 0, this.elementIndex, consumer);
        }
    }

    public static class OfDouble extends OfPrimitive<Double, double[], DoubleConsumer> implements DoubleConsumer {

        /* renamed from: java.util.stream.SpinedBuffer.OfDouble.1Splitr */
        class AnonymousClass1Splitr extends BaseSpliterator<java.util.Spliterator.OfDouble> implements java.util.Spliterator.OfDouble {
            final /* synthetic */ OfDouble this$1;

            public /* bridge */ /* synthetic */ boolean tryAdvance(DoubleConsumer consumer) {
                return tryAdvance(consumer);
            }

            public /* bridge */ /* synthetic */ void forEachRemaining(DoubleConsumer consumer) {
                forEachRemaining(consumer);
            }

            public /* bridge */ /* synthetic */ java.util.Spliterator.OfDouble trySplit() {
                return (java.util.Spliterator.OfDouble) trySplit();
            }

            AnonymousClass1Splitr(OfDouble this$1, int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                this.this$1 = this$1;
                super(this$1, firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
            }

            /* bridge */ /* synthetic */ java.util.Spliterator.OfPrimitive newSpliterator(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                return newSpliterator(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
            }

            AnonymousClass1Splitr m90newSpliterator(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                return new AnonymousClass1Splitr(this.this$1, firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
            }

            /* bridge */ /* synthetic */ void arrayForOne(Object array, int index, Object consumer) {
                arrayForOne((double[]) array, index, (DoubleConsumer) consumer);
            }

            void arrayForOne(double[] array, int index, DoubleConsumer consumer) {
                consumer.accept(array[index]);
            }

            /* bridge */ /* synthetic */ java.util.Spliterator.OfPrimitive arraySpliterator(Object array, int offset, int len) {
                return arraySpliterator((double[]) array, offset, len);
            }

            java.util.Spliterator.OfDouble arraySpliterator(double[] array, int offset, int len) {
                return Arrays.spliterator(array, offset, offset + len);
            }
        }

        public OfDouble() {
        }

        public OfDouble(int initialCapacity) {
            super(initialCapacity);
        }

        public void forEach(Consumer<? super Double> consumer) {
            if (consumer instanceof DoubleConsumer) {
                forEach((Object) (DoubleConsumer) consumer);
                return;
            }
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling SpinedBuffer.OfDouble.forEach(Consumer)");
            }
            spliterator().forEachRemaining((Consumer) consumer);
        }

        protected /* bridge */ /* synthetic */ Object[] newArrayArray(int size) {
            return newArrayArray(size);
        }

        protected double[][] m88newArrayArray(int size) {
            return new double[size][];
        }

        public /* bridge */ /* synthetic */ Object newArray(int size) {
            return newArray(size);
        }

        public double[] m87newArray(int size) {
            return new double[size];
        }

        protected /* bridge */ /* synthetic */ int arrayLength(Object array) {
            return arrayLength((double[]) array);
        }

        protected int arrayLength(double[] array) {
            return array.length;
        }

        protected /* bridge */ /* synthetic */ void arrayForEach(Object array, int from, int to, Object consumer) {
            arrayForEach((double[]) array, from, to, (DoubleConsumer) consumer);
        }

        protected void arrayForEach(double[] array, int from, int to, DoubleConsumer consumer) {
            for (int i = from; i < to; i++) {
                consumer.accept(array[i]);
            }
        }

        public void accept(double i) {
            preAccept();
            double[] dArr = (double[]) this.curChunk;
            int i2 = this.elementIndex;
            this.elementIndex = i2 + 1;
            dArr[i2] = i;
        }

        public double get(long index) {
            int ch = chunkFor(index);
            if (this.spineIndex == 0 && ch == 0) {
                return ((double[]) this.curChunk)[(int) index];
            }
            return ((double[][]) this.spine)[ch][(int) (index - this.priorElementCount[ch])];
        }

        public /* bridge */ /* synthetic */ Iterator iterator() {
            return iterator();
        }

        public java.util.PrimitiveIterator.OfDouble m86iterator() {
            return Spliterators.iterator(spliterator());
        }

        public /* bridge */ /* synthetic */ Spliterator m89spliterator() {
            return spliterator();
        }

        public java.util.Spliterator.OfDouble spliterator() {
            return new AnonymousClass1Splitr(this, 0, this.spineIndex, 0, this.elementIndex);
        }

        public String toString() {
            double[] array = (double[]) asPrimitiveArray();
            if (array.length < HttpURLConnection.HTTP_OK) {
                return String.format("%s[length=%d, chunks=%d]%s", getClass().getSimpleName(), Integer.valueOf(array.length), Integer.valueOf(this.spineIndex), Arrays.toString(array));
            }
            double[] array2 = Arrays.copyOf(array, (int) HttpURLConnection.HTTP_OK);
            return String.format("%s[length=%d, chunks=%d]%s...", getClass().getSimpleName(), Integer.valueOf(array.length), Integer.valueOf(this.spineIndex), Arrays.toString(array2));
        }
    }

    public static class OfInt extends OfPrimitive<Integer, int[], IntConsumer> implements IntConsumer {

        /* renamed from: java.util.stream.SpinedBuffer.OfInt.1Splitr */
        class AnonymousClass1Splitr extends BaseSpliterator<java.util.Spliterator.OfInt> implements java.util.Spliterator.OfInt {
            final /* synthetic */ OfInt this$1;

            public /* bridge */ /* synthetic */ boolean tryAdvance(IntConsumer consumer) {
                return tryAdvance(consumer);
            }

            public /* bridge */ /* synthetic */ void forEachRemaining(IntConsumer consumer) {
                forEachRemaining(consumer);
            }

            public /* bridge */ /* synthetic */ java.util.Spliterator.OfInt trySplit() {
                return (java.util.Spliterator.OfInt) trySplit();
            }

            AnonymousClass1Splitr(OfInt this$1, int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                this.this$1 = this$1;
                super(this$1, firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
            }

            /* bridge */ /* synthetic */ java.util.Spliterator.OfPrimitive newSpliterator(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                return newSpliterator(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
            }

            AnonymousClass1Splitr m95newSpliterator(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                return new AnonymousClass1Splitr(this.this$1, firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
            }

            /* bridge */ /* synthetic */ void arrayForOne(Object array, int index, Object consumer) {
                arrayForOne((int[]) array, index, (IntConsumer) consumer);
            }

            void arrayForOne(int[] array, int index, IntConsumer consumer) {
                consumer.accept(array[index]);
            }

            /* bridge */ /* synthetic */ java.util.Spliterator.OfPrimitive arraySpliterator(Object array, int offset, int len) {
                return arraySpliterator((int[]) array, offset, len);
            }

            java.util.Spliterator.OfInt arraySpliterator(int[] array, int offset, int len) {
                return Arrays.spliterator(array, offset, offset + len);
            }
        }

        public OfInt() {
        }

        public OfInt(int initialCapacity) {
            super(initialCapacity);
        }

        public void forEach(Consumer<? super Integer> consumer) {
            if (consumer instanceof IntConsumer) {
                forEach((Object) (IntConsumer) consumer);
                return;
            }
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling SpinedBuffer.OfInt.forEach(Consumer)");
            }
            spliterator().forEachRemaining((Consumer) consumer);
        }

        protected /* bridge */ /* synthetic */ Object[] newArrayArray(int size) {
            return newArrayArray(size);
        }

        protected int[][] m93newArrayArray(int size) {
            return new int[size][];
        }

        public /* bridge */ /* synthetic */ Object newArray(int size) {
            return newArray(size);
        }

        public int[] m92newArray(int size) {
            return new int[size];
        }

        protected /* bridge */ /* synthetic */ int arrayLength(Object array) {
            return arrayLength((int[]) array);
        }

        protected int arrayLength(int[] array) {
            return array.length;
        }

        protected /* bridge */ /* synthetic */ void arrayForEach(Object array, int from, int to, Object consumer) {
            arrayForEach((int[]) array, from, to, (IntConsumer) consumer);
        }

        protected void arrayForEach(int[] array, int from, int to, IntConsumer consumer) {
            for (int i = from; i < to; i++) {
                consumer.accept(array[i]);
            }
        }

        public void accept(int i) {
            preAccept();
            int[] iArr = (int[]) this.curChunk;
            int i2 = this.elementIndex;
            this.elementIndex = i2 + 1;
            iArr[i2] = i;
        }

        public int get(long index) {
            int ch = chunkFor(index);
            if (this.spineIndex == 0 && ch == 0) {
                return ((int[]) this.curChunk)[(int) index];
            }
            return ((int[][]) this.spine)[ch][(int) (index - this.priorElementCount[ch])];
        }

        public /* bridge */ /* synthetic */ Iterator iterator() {
            return iterator();
        }

        public java.util.PrimitiveIterator.OfInt m91iterator() {
            return Spliterators.iterator(spliterator());
        }

        public /* bridge */ /* synthetic */ Spliterator m94spliterator() {
            return spliterator();
        }

        public java.util.Spliterator.OfInt spliterator() {
            return new AnonymousClass1Splitr(this, 0, this.spineIndex, 0, this.elementIndex);
        }

        public String toString() {
            int[] array = (int[]) asPrimitiveArray();
            if (array.length < HttpURLConnection.HTTP_OK) {
                return String.format("%s[length=%d, chunks=%d]%s", getClass().getSimpleName(), Integer.valueOf(array.length), Integer.valueOf(this.spineIndex), Arrays.toString(array));
            }
            int[] array2 = Arrays.copyOf(array, (int) HttpURLConnection.HTTP_OK);
            return String.format("%s[length=%d, chunks=%d]%s...", getClass().getSimpleName(), Integer.valueOf(array.length), Integer.valueOf(this.spineIndex), Arrays.toString(array2));
        }
    }

    public static class OfLong extends OfPrimitive<Long, long[], LongConsumer> implements LongConsumer {

        /* renamed from: java.util.stream.SpinedBuffer.OfLong.1Splitr */
        class AnonymousClass1Splitr extends BaseSpliterator<java.util.Spliterator.OfLong> implements java.util.Spliterator.OfLong {
            final /* synthetic */ OfLong this$1;

            public /* bridge */ /* synthetic */ boolean tryAdvance(LongConsumer consumer) {
                return tryAdvance(consumer);
            }

            public /* bridge */ /* synthetic */ void forEachRemaining(LongConsumer consumer) {
                forEachRemaining(consumer);
            }

            public /* bridge */ /* synthetic */ java.util.Spliterator.OfLong trySplit() {
                return (java.util.Spliterator.OfLong) trySplit();
            }

            AnonymousClass1Splitr(OfLong this$1, int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                this.this$1 = this$1;
                super(this$1, firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
            }

            /* bridge */ /* synthetic */ java.util.Spliterator.OfPrimitive newSpliterator(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                return newSpliterator(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
            }

            AnonymousClass1Splitr m100newSpliterator(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                return new AnonymousClass1Splitr(this.this$1, firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
            }

            /* bridge */ /* synthetic */ void arrayForOne(Object array, int index, Object consumer) {
                arrayForOne((long[]) array, index, (LongConsumer) consumer);
            }

            void arrayForOne(long[] array, int index, LongConsumer consumer) {
                consumer.accept(array[index]);
            }

            /* bridge */ /* synthetic */ java.util.Spliterator.OfPrimitive arraySpliterator(Object array, int offset, int len) {
                return arraySpliterator((long[]) array, offset, len);
            }

            java.util.Spliterator.OfLong arraySpliterator(long[] array, int offset, int len) {
                return Arrays.spliterator(array, offset, offset + len);
            }
        }

        public OfLong() {
        }

        public OfLong(int initialCapacity) {
            super(initialCapacity);
        }

        public void forEach(Consumer<? super Long> consumer) {
            if (consumer instanceof LongConsumer) {
                forEach((Object) (LongConsumer) consumer);
                return;
            }
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling SpinedBuffer.OfLong.forEach(Consumer)");
            }
            spliterator().forEachRemaining((Consumer) consumer);
        }

        protected /* bridge */ /* synthetic */ Object[] newArrayArray(int size) {
            return newArrayArray(size);
        }

        protected long[][] m98newArrayArray(int size) {
            return new long[size][];
        }

        public /* bridge */ /* synthetic */ Object newArray(int size) {
            return newArray(size);
        }

        public long[] m97newArray(int size) {
            return new long[size];
        }

        protected /* bridge */ /* synthetic */ int arrayLength(Object array) {
            return arrayLength((long[]) array);
        }

        protected int arrayLength(long[] array) {
            return array.length;
        }

        protected /* bridge */ /* synthetic */ void arrayForEach(Object array, int from, int to, Object consumer) {
            arrayForEach((long[]) array, from, to, (LongConsumer) consumer);
        }

        protected void arrayForEach(long[] array, int from, int to, LongConsumer consumer) {
            for (int i = from; i < to; i++) {
                consumer.accept(array[i]);
            }
        }

        public void accept(long i) {
            preAccept();
            long[] jArr = (long[]) this.curChunk;
            int i2 = this.elementIndex;
            this.elementIndex = i2 + 1;
            jArr[i2] = i;
        }

        public long get(long index) {
            int ch = chunkFor(index);
            if (this.spineIndex == 0 && ch == 0) {
                return ((long[]) this.curChunk)[(int) index];
            }
            return ((long[][]) this.spine)[ch][(int) (index - this.priorElementCount[ch])];
        }

        public /* bridge */ /* synthetic */ Iterator iterator() {
            return iterator();
        }

        public java.util.PrimitiveIterator.OfLong m96iterator() {
            return Spliterators.iterator(spliterator());
        }

        public /* bridge */ /* synthetic */ Spliterator m99spliterator() {
            return spliterator();
        }

        public java.util.Spliterator.OfLong spliterator() {
            return new AnonymousClass1Splitr(this, 0, this.spineIndex, 0, this.elementIndex);
        }

        public String toString() {
            long[] array = (long[]) asPrimitiveArray();
            if (array.length < HttpURLConnection.HTTP_OK) {
                return String.format("%s[length=%d, chunks=%d]%s", getClass().getSimpleName(), Integer.valueOf(array.length), Integer.valueOf(this.spineIndex), Arrays.toString(array));
            }
            long[] array2 = Arrays.copyOf(array, (int) HttpURLConnection.HTTP_OK);
            return String.format("%s[length=%d, chunks=%d]%s...", getClass().getSimpleName(), Integer.valueOf(array.length), Integer.valueOf(this.spineIndex), Arrays.toString(array2));
        }
    }

    final /* synthetic */ class -java_lang_String_toString__LambdaImpl0 implements Consumer {
        private /* synthetic */ List val$-lambdaCtx;

        public /* synthetic */ -java_lang_String_toString__LambdaImpl0(List list) {
            this.val$-lambdaCtx = list;
        }

        public void accept(Object arg0) {
            this.val$-lambdaCtx.add(arg0);
        }
    }

    /* renamed from: java.util.stream.SpinedBuffer.1Splitr */
    class AnonymousClass1Splitr implements Spliterator<E> {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        final int lastSpineElementFence;
        final int lastSpineIndex;
        E[] splChunk;
        int splElementIndex;
        int splSpineIndex;
        final /* synthetic */ SpinedBuffer this$0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.SpinedBuffer.1Splitr.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.stream.SpinedBuffer.1Splitr.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.SpinedBuffer.1Splitr.<clinit>():void");
        }

        AnonymousClass1Splitr(SpinedBuffer this$0, int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
            Object[] objArr;
            Object obj = 1;
            this.this$0 = this$0;
            this.splSpineIndex = firstSpineIndex;
            this.lastSpineIndex = lastSpineIndex;
            this.splElementIndex = firstSpineElementIndex;
            this.lastSpineElementFence = lastSpineElementFence;
            if (!-assertionsDisabled) {
                if (this$0.spine == null && !(firstSpineIndex == 0 && lastSpineIndex == 0)) {
                    obj = null;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            if (this$0.spine == null) {
                objArr = this$0.curChunk;
            } else {
                objArr = this$0.spine[firstSpineIndex];
            }
            this.splChunk = objArr;
        }

        public long estimateSize() {
            if (this.splSpineIndex == this.lastSpineIndex) {
                return ((long) this.lastSpineElementFence) - ((long) this.splElementIndex);
            }
            return ((this.this$0.priorElementCount[this.lastSpineIndex] + ((long) this.lastSpineElementFence)) - this.this$0.priorElementCount[this.splSpineIndex]) - ((long) this.splElementIndex);
        }

        public int characteristics() {
            return SpinedBuffer.SPLITERATOR_CHARACTERISTICS;
        }

        public boolean tryAdvance(Consumer<? super E> consumer) {
            Objects.requireNonNull(consumer);
            if (this.splSpineIndex >= this.lastSpineIndex && (this.splSpineIndex != this.lastSpineIndex || this.splElementIndex >= this.lastSpineElementFence)) {
                return false;
            }
            Object[] objArr = this.splChunk;
            int i = this.splElementIndex;
            this.splElementIndex = i + 1;
            consumer.accept(objArr[i]);
            if (this.splElementIndex == this.splChunk.length) {
                this.splElementIndex = 0;
                this.splSpineIndex++;
                if (this.this$0.spine != null && this.splSpineIndex <= this.lastSpineIndex) {
                    this.splChunk = this.this$0.spine[this.splSpineIndex];
                }
            }
            return true;
        }

        public void forEachRemaining(Consumer<? super E> consumer) {
            Objects.requireNonNull(consumer);
            if (this.splSpineIndex < this.lastSpineIndex || (this.splSpineIndex == this.lastSpineIndex && this.splElementIndex < this.lastSpineElementFence)) {
                E[] chunk;
                int i = this.splElementIndex;
                for (int sp = this.splSpineIndex; sp < this.lastSpineIndex; sp++) {
                    chunk = this.this$0.spine[sp];
                    while (i < chunk.length) {
                        consumer.accept(chunk[i]);
                        i++;
                    }
                    i = 0;
                }
                chunk = this.splSpineIndex == this.lastSpineIndex ? this.splChunk : this.this$0.spine[this.lastSpineIndex];
                int hElementIndex = this.lastSpineElementFence;
                while (i < hElementIndex) {
                    consumer.accept(chunk[i]);
                    i++;
                }
                this.splSpineIndex = this.lastSpineIndex;
                this.splElementIndex = this.lastSpineElementFence;
            }
        }

        public Spliterator<E> trySplit() {
            Spliterator<E> ret;
            if (this.splSpineIndex < this.lastSpineIndex) {
                ret = new AnonymousClass1Splitr(this.this$0, this.splSpineIndex, this.lastSpineIndex - 1, this.splElementIndex, this.this$0.spine[this.lastSpineIndex - 1].length);
                this.splSpineIndex = this.lastSpineIndex;
                this.splElementIndex = 0;
                this.splChunk = this.this$0.spine[this.splSpineIndex];
                return ret;
            } else if (this.splSpineIndex != this.lastSpineIndex) {
                return null;
            } else {
                int t = (this.lastSpineElementFence - this.splElementIndex) / 2;
                if (t == 0) {
                    return null;
                }
                ret = Arrays.spliterator(this.splChunk, this.splElementIndex, this.splElementIndex + t);
                this.splElementIndex += t;
                return ret;
            }
        }
    }

    public /* bridge */ /* synthetic */ long count() {
        return super.count();
    }

    public /* bridge */ /* synthetic */ boolean isEmpty() {
        return super.isEmpty();
    }

    public SpinedBuffer(int initialCapacity) {
        super(initialCapacity);
        this.curChunk = new Object[(1 << this.initialChunkPower)];
    }

    public SpinedBuffer() {
        this.curChunk = new Object[(1 << this.initialChunkPower)];
    }

    protected long capacity() {
        if (this.spineIndex == 0) {
            return (long) this.curChunk.length;
        }
        return this.priorElementCount[this.spineIndex] + ((long) this.spine[this.spineIndex].length);
    }

    private void inflateSpine() {
        if (this.spine == null) {
            this.spine = new Object[8][];
            this.priorElementCount = new long[8];
            this.spine[0] = this.curChunk;
        }
    }

    protected final void ensureCapacity(long targetSize) {
        long capacity = capacity();
        if (targetSize > capacity) {
            inflateSpine();
            int i = this.spineIndex + 1;
            while (targetSize > capacity) {
                if (i >= this.spine.length) {
                    int newSpineSize = this.spine.length * 2;
                    this.spine = (Object[][]) Arrays.copyOf(this.spine, newSpineSize);
                    this.priorElementCount = Arrays.copyOf(this.priorElementCount, newSpineSize);
                }
                int nextChunkSize = chunkSize(i);
                this.spine[i] = new Object[nextChunkSize];
                this.priorElementCount[i] = this.priorElementCount[i - 1] + ((long) this.spine[i - 1].length);
                capacity += (long) nextChunkSize;
                i++;
            }
        }
    }

    protected void increaseCapacity() {
        ensureCapacity(capacity() + 1);
    }

    public E get(long index) {
        if (this.spineIndex == 0) {
            if (index < ((long) this.elementIndex)) {
                return this.curChunk[(int) index];
            }
            throw new IndexOutOfBoundsException(Long.toString(index));
        } else if (index >= count()) {
            throw new IndexOutOfBoundsException(Long.toString(index));
        } else {
            for (int j = 0; j <= this.spineIndex; j++) {
                if (index < this.priorElementCount[j] + ((long) this.spine[j].length)) {
                    return this.spine[j][(int) (index - this.priorElementCount[j])];
                }
            }
            throw new IndexOutOfBoundsException(Long.toString(index));
        }
    }

    public void copyInto(E[] array, int offset) {
        long finalOffset = ((long) offset) + count();
        if (finalOffset > ((long) array.length) || finalOffset < ((long) offset)) {
            throw new IndexOutOfBoundsException("does not fit");
        } else if (this.spineIndex == 0) {
            System.arraycopy(this.curChunk, 0, (Object) array, offset, this.elementIndex);
        } else {
            for (int i = 0; i < this.spineIndex; i++) {
                System.arraycopy(this.spine[i], 0, (Object) array, offset, this.spine[i].length);
                offset += this.spine[i].length;
            }
            if (this.elementIndex > 0) {
                System.arraycopy(this.curChunk, 0, (Object) array, offset, this.elementIndex);
            }
        }
    }

    public E[] asArray(IntFunction<E[]> arrayFactory) {
        long size = count();
        if (size >= 2147483639) {
            throw new IllegalArgumentException("Stream size exceeds max array size");
        }
        Object[] result = (Object[]) arrayFactory.apply((int) size);
        copyInto(result, 0);
        return result;
    }

    public void clear() {
        int i;
        if (this.spine != null) {
            this.curChunk = this.spine[0];
            for (i = 0; i < this.curChunk.length; i++) {
                this.curChunk[i] = null;
            }
            this.spine = null;
            this.priorElementCount = null;
        } else {
            for (i = 0; i < this.elementIndex; i++) {
                this.curChunk[i] = null;
            }
        }
        this.elementIndex = 0;
        this.spineIndex = 0;
    }

    public Iterator<E> iterator() {
        return Spliterators.iterator(spliterator());
    }

    public void forEach(Consumer<? super E> consumer) {
        for (int j = 0; j < this.spineIndex; j++) {
            for (E t : this.spine[j]) {
                consumer.accept(t);
            }
        }
        for (int i = 0; i < this.elementIndex; i++) {
            consumer.accept(this.curChunk[i]);
        }
    }

    public void accept(E e) {
        if (this.elementIndex == this.curChunk.length) {
            inflateSpine();
            if (this.spineIndex + 1 >= this.spine.length || this.spine[this.spineIndex + 1] == null) {
                increaseCapacity();
            }
            this.elementIndex = 0;
            this.spineIndex++;
            this.curChunk = this.spine[this.spineIndex];
        }
        Object[] objArr = this.curChunk;
        int i = this.elementIndex;
        this.elementIndex = i + 1;
        objArr[i] = e;
    }

    public String toString() {
        List<E> list = new ArrayList();
        list.getClass();
        forEach(new -java_lang_String_toString__LambdaImpl0(list));
        return "SpinedBuffer:" + list.toString();
    }

    public Spliterator<E> spliterator() {
        return new AnonymousClass1Splitr(this, 0, this.spineIndex, 0, this.elementIndex);
    }
}
