package java.util;

import java.util.Spliterator.OfDouble;
import java.util.Spliterator.OfInt;
import java.util.Spliterator.OfLong;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public final class Spliterators {
    private static final OfDouble EMPTY_DOUBLE_SPLITERATOR = new OfDouble();
    private static final OfInt EMPTY_INT_SPLITERATOR = new OfInt();
    private static final OfLong EMPTY_LONG_SPLITERATOR = new OfLong();
    private static final Spliterator<Object> EMPTY_SPLITERATOR = new OfRef();

    static class IteratorSpliterator<T> implements Spliterator<T> {
        static final int BATCH_UNIT = 1024;
        static final int MAX_BATCH = 33554432;
        private int batch;
        private final int characteristics;
        private final Collection<? extends T> collection;
        private long est;
        private Iterator<? extends T> it;

        public IteratorSpliterator(Collection<? extends T> collection, int characteristics) {
            this.collection = collection;
            this.it = null;
            if ((characteristics & 4096) == 0) {
                characteristics = (characteristics | 64) | 16384;
            }
            this.characteristics = characteristics;
        }

        public IteratorSpliterator(Iterator<? extends T> iterator, long size, int characteristics) {
            this.collection = null;
            this.it = iterator;
            this.est = size;
            if ((characteristics & 4096) == 0) {
                characteristics = (characteristics | 64) | 16384;
            }
            this.characteristics = characteristics;
        }

        public IteratorSpliterator(Iterator<? extends T> iterator, int characteristics) {
            this.collection = null;
            this.it = iterator;
            this.est = Long.MAX_VALUE;
            this.characteristics = characteristics & -16449;
        }

        public Spliterator<T> trySplit() {
            long s;
            Iterator<? extends T> i = this.it;
            if (i == null) {
                i = this.collection.iterator();
                this.it = i;
                s = (long) this.collection.size();
                this.est = s;
            } else {
                s = this.est;
            }
            if (s <= 1 || !i.hasNext()) {
                return null;
            }
            int n = this.batch + 1024;
            if (((long) n) > s) {
                n = (int) s;
            }
            if (n > MAX_BATCH) {
                n = MAX_BATCH;
            }
            Object[] a = new Object[n];
            int j = 0;
            do {
                a[j] = i.next();
                j++;
                if (j >= n) {
                    break;
                }
            } while (i.hasNext());
            this.batch = j;
            if (this.est != Long.MAX_VALUE) {
                this.est -= (long) j;
            }
            return new ArraySpliterator(a, 0, j, this.characteristics);
        }

        public void forEachRemaining(Consumer<? super T> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Iterator<? extends T> i = this.it;
            if (i == null) {
                i = this.collection.iterator();
                this.it = i;
                this.est = (long) this.collection.size();
            }
            i.forEachRemaining(action);
        }

        public boolean tryAdvance(Consumer<? super T> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (this.it == null) {
                this.it = this.collection.iterator();
                this.est = (long) this.collection.size();
            }
            if (!this.it.hasNext()) {
                return false;
            }
            action.accept(this.it.next());
            return true;
        }

        public long estimateSize() {
            if (this.it != null) {
                return this.est;
            }
            this.it = this.collection.iterator();
            long size = (long) this.collection.size();
            this.est = size;
            return size;
        }

        public int characteristics() {
            return this.characteristics;
        }

        public Comparator<? super T> getComparator() {
            if (hasCharacteristics(4)) {
                return null;
            }
            throw new IllegalStateException();
        }
    }

    public static abstract class AbstractDoubleSpliterator implements OfDouble {
        static final int BATCH_UNIT = 1024;
        static final int MAX_BATCH = 33554432;
        private int batch;
        private final int characteristics;
        private long est;

        static final class HoldingDoubleConsumer implements DoubleConsumer {
            double value;

            HoldingDoubleConsumer() {
            }

            public void accept(double value) {
                this.value = value;
            }
        }

        protected AbstractDoubleSpliterator(long est, int additionalCharacteristics) {
            this.est = est;
            if ((additionalCharacteristics & 64) != 0) {
                additionalCharacteristics |= 16384;
            }
            this.characteristics = additionalCharacteristics;
        }

        public OfDouble trySplit() {
            HoldingDoubleConsumer holder = new HoldingDoubleConsumer();
            long s = this.est;
            if (s <= 1 || !tryAdvance((DoubleConsumer) holder)) {
                return null;
            }
            int n = this.batch + 1024;
            if (((long) n) > s) {
                n = (int) s;
            }
            if (n > MAX_BATCH) {
                n = MAX_BATCH;
            }
            double[] a = new double[n];
            int j = 0;
            do {
                a[j] = holder.value;
                j++;
                if (j >= n) {
                    break;
                }
            } while (tryAdvance((DoubleConsumer) holder));
            this.batch = j;
            if (this.est != Long.MAX_VALUE) {
                this.est -= (long) j;
            }
            return new DoubleArraySpliterator(a, 0, j, characteristics());
        }

        public long estimateSize() {
            return this.est;
        }

        public int characteristics() {
            return this.characteristics;
        }
    }

    public static abstract class AbstractIntSpliterator implements OfInt {
        static final int BATCH_UNIT = 1024;
        static final int MAX_BATCH = 33554432;
        private int batch;
        private final int characteristics;
        private long est;

        static final class HoldingIntConsumer implements IntConsumer {
            int value;

            HoldingIntConsumer() {
            }

            public void accept(int value) {
                this.value = value;
            }
        }

        protected AbstractIntSpliterator(long est, int additionalCharacteristics) {
            this.est = est;
            if ((additionalCharacteristics & 64) != 0) {
                additionalCharacteristics |= 16384;
            }
            this.characteristics = additionalCharacteristics;
        }

        public OfInt trySplit() {
            HoldingIntConsumer holder = new HoldingIntConsumer();
            long s = this.est;
            if (s <= 1 || !tryAdvance((IntConsumer) holder)) {
                return null;
            }
            int n = this.batch + 1024;
            if (((long) n) > s) {
                n = (int) s;
            }
            if (n > MAX_BATCH) {
                n = MAX_BATCH;
            }
            int[] a = new int[n];
            int j = 0;
            do {
                a[j] = holder.value;
                j++;
                if (j >= n) {
                    break;
                }
            } while (tryAdvance((IntConsumer) holder));
            this.batch = j;
            if (this.est != Long.MAX_VALUE) {
                this.est -= (long) j;
            }
            return new IntArraySpliterator(a, 0, j, characteristics());
        }

        public long estimateSize() {
            return this.est;
        }

        public int characteristics() {
            return this.characteristics;
        }
    }

    public static abstract class AbstractLongSpliterator implements OfLong {
        static final int BATCH_UNIT = 1024;
        static final int MAX_BATCH = 33554432;
        private int batch;
        private final int characteristics;
        private long est;

        static final class HoldingLongConsumer implements LongConsumer {
            long value;

            HoldingLongConsumer() {
            }

            public void accept(long value) {
                this.value = value;
            }
        }

        protected AbstractLongSpliterator(long est, int additionalCharacteristics) {
            this.est = est;
            if ((additionalCharacteristics & 64) != 0) {
                additionalCharacteristics |= 16384;
            }
            this.characteristics = additionalCharacteristics;
        }

        public OfLong trySplit() {
            HoldingLongConsumer holder = new HoldingLongConsumer();
            long s = this.est;
            if (s <= 1 || !tryAdvance((LongConsumer) holder)) {
                return null;
            }
            int n = this.batch + 1024;
            if (((long) n) > s) {
                n = (int) s;
            }
            if (n > MAX_BATCH) {
                n = MAX_BATCH;
            }
            long[] a = new long[n];
            int j = 0;
            do {
                a[j] = holder.value;
                j++;
                if (j >= n) {
                    break;
                }
            } while (tryAdvance((LongConsumer) holder));
            this.batch = j;
            if (this.est != Long.MAX_VALUE) {
                this.est -= (long) j;
            }
            return new LongArraySpliterator(a, 0, j, characteristics());
        }

        public long estimateSize() {
            return this.est;
        }

        public int characteristics() {
            return this.characteristics;
        }
    }

    public static abstract class AbstractSpliterator<T> implements Spliterator<T> {
        static final int BATCH_UNIT = 1024;
        static final int MAX_BATCH = 33554432;
        private int batch;
        private final int characteristics;
        private long est;

        static final class HoldingConsumer<T> implements Consumer<T> {
            Object value;

            HoldingConsumer() {
            }

            public void accept(T value) {
                this.value = value;
            }
        }

        protected AbstractSpliterator(long est, int additionalCharacteristics) {
            this.est = est;
            if ((additionalCharacteristics & 64) != 0) {
                additionalCharacteristics |= 16384;
            }
            this.characteristics = additionalCharacteristics;
        }

        public Spliterator<T> trySplit() {
            HoldingConsumer<T> holder = new HoldingConsumer();
            long s = this.est;
            if (s <= 1 || !tryAdvance(holder)) {
                return null;
            }
            int n = this.batch + 1024;
            if (((long) n) > s) {
                n = (int) s;
            }
            if (n > MAX_BATCH) {
                n = MAX_BATCH;
            }
            Object[] a = new Object[n];
            int j = 0;
            do {
                a[j] = holder.value;
                j++;
                if (j >= n) {
                    break;
                }
            } while (tryAdvance(holder));
            this.batch = j;
            if (this.est != Long.MAX_VALUE) {
                this.est -= (long) j;
            }
            return new ArraySpliterator(a, 0, j, characteristics());
        }

        public long estimateSize() {
            return this.est;
        }

        public int characteristics() {
            return this.characteristics;
        }
    }

    static final class ArraySpliterator<T> implements Spliterator<T> {
        private final Object[] array;
        private final int characteristics;
        private final int fence;
        private int index;

        public ArraySpliterator(Object[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        public ArraySpliterator(Object[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = (additionalCharacteristics | 64) | 16384;
        }

        public Spliterator<T> trySplit() {
            int lo = this.index;
            int mid = (this.fence + lo) >>> 1;
            if (lo >= mid) {
                return null;
            }
            Object[] objArr = this.array;
            this.index = mid;
            return new ArraySpliterator(objArr, lo, mid, this.characteristics);
        }

        public void forEachRemaining(Consumer<? super T> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Object[] a = this.array;
            int length = a.length;
            int hi = this.fence;
            if (length >= hi) {
                int i = this.index;
                if (i >= 0) {
                    this.index = hi;
                    if (i < hi) {
                        do {
                            action.accept(a[i]);
                            i++;
                        } while (i < hi);
                    }
                }
            }
        }

        public boolean tryAdvance(Consumer<? super T> action) {
            if (action == null) {
                throw new NullPointerException();
            } else if (this.index < 0 || this.index >= this.fence) {
                return false;
            } else {
                Object[] objArr = this.array;
                int i = this.index;
                this.index = i + 1;
                action.accept(objArr[i]);
                return true;
            }
        }

        public long estimateSize() {
            return (long) (this.fence - this.index);
        }

        public int characteristics() {
            return this.characteristics;
        }

        public Comparator<? super T> getComparator() {
            if (hasCharacteristics(4)) {
                return null;
            }
            throw new IllegalStateException();
        }
    }

    static final class DoubleArraySpliterator implements OfDouble {
        private final double[] array;
        private final int characteristics;
        private final int fence;
        private int index;

        public DoubleArraySpliterator(double[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        public DoubleArraySpliterator(double[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = (additionalCharacteristics | 64) | 16384;
        }

        public OfDouble trySplit() {
            int lo = this.index;
            int mid = (this.fence + lo) >>> 1;
            if (lo >= mid) {
                return null;
            }
            double[] dArr = this.array;
            this.index = mid;
            return new DoubleArraySpliterator(dArr, lo, mid, this.characteristics);
        }

        public void forEachRemaining(DoubleConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            }
            double[] a = this.array;
            int length = a.length;
            int hi = this.fence;
            if (length >= hi) {
                int i = this.index;
                if (i >= 0) {
                    this.index = hi;
                    if (i < hi) {
                        do {
                            action.accept(a[i]);
                            i++;
                        } while (i < hi);
                    }
                }
            }
        }

        public boolean tryAdvance(DoubleConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            } else if (this.index < 0 || this.index >= this.fence) {
                return false;
            } else {
                double[] dArr = this.array;
                int i = this.index;
                this.index = i + 1;
                action.accept(dArr[i]);
                return true;
            }
        }

        public long estimateSize() {
            return (long) (this.fence - this.index);
        }

        public int characteristics() {
            return this.characteristics;
        }

        public Comparator<? super Double> getComparator() {
            if (hasCharacteristics(4)) {
                return null;
            }
            throw new IllegalStateException();
        }
    }

    static final class DoubleIteratorSpliterator implements OfDouble {
        static final int BATCH_UNIT = 1024;
        static final int MAX_BATCH = 33554432;
        private int batch;
        private final int characteristics;
        private long est;
        private PrimitiveIterator.OfDouble it;

        public DoubleIteratorSpliterator(PrimitiveIterator.OfDouble iterator, long size, int characteristics) {
            this.it = iterator;
            this.est = size;
            if ((characteristics & 4096) == 0) {
                characteristics = (characteristics | 64) | 16384;
            }
            this.characteristics = characteristics;
        }

        public DoubleIteratorSpliterator(PrimitiveIterator.OfDouble iterator, int characteristics) {
            this.it = iterator;
            this.est = Long.MAX_VALUE;
            this.characteristics = characteristics & -16449;
        }

        public OfDouble trySplit() {
            PrimitiveIterator.OfDouble i = this.it;
            long s = this.est;
            if (s <= 1 || !i.hasNext()) {
                return null;
            }
            int n = this.batch + 1024;
            if (((long) n) > s) {
                n = (int) s;
            }
            if (n > MAX_BATCH) {
                n = MAX_BATCH;
            }
            double[] a = new double[n];
            int j = 0;
            do {
                a[j] = i.nextDouble();
                j++;
                if (j >= n) {
                    break;
                }
            } while (i.hasNext());
            this.batch = j;
            if (this.est != Long.MAX_VALUE) {
                this.est -= (long) j;
            }
            return new DoubleArraySpliterator(a, 0, j, this.characteristics);
        }

        public void forEachRemaining(DoubleConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            }
            this.it.forEachRemaining(action);
        }

        public boolean tryAdvance(DoubleConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            } else if (!this.it.hasNext()) {
                return false;
            } else {
                action.accept(this.it.nextDouble());
                return true;
            }
        }

        public long estimateSize() {
            return this.est;
        }

        public int characteristics() {
            return this.characteristics;
        }

        public Comparator<? super Double> getComparator() {
            if (hasCharacteristics(4)) {
                return null;
            }
            throw new IllegalStateException();
        }
    }

    private static abstract class EmptySpliterator<T, S extends Spliterator<T>, C> {

        private static final class OfDouble extends EmptySpliterator<Double, java.util.Spliterator.OfDouble, DoubleConsumer> implements java.util.Spliterator.OfDouble {
            OfDouble() {
            }
        }

        private static final class OfInt extends EmptySpliterator<Integer, java.util.Spliterator.OfInt, IntConsumer> implements java.util.Spliterator.OfInt {
            OfInt() {
            }
        }

        private static final class OfLong extends EmptySpliterator<Long, java.util.Spliterator.OfLong, LongConsumer> implements java.util.Spliterator.OfLong {
            OfLong() {
            }
        }

        private static final class OfRef<T> extends EmptySpliterator<T, Spliterator<T>, Consumer<? super T>> implements Spliterator<T> {
            OfRef() {
            }
        }

        EmptySpliterator() {
        }

        public S trySplit() {
            return null;
        }

        public boolean tryAdvance(C consumer) {
            Objects.requireNonNull(consumer);
            return false;
        }

        public void forEachRemaining(C consumer) {
            Objects.requireNonNull(consumer);
        }

        public long estimateSize() {
            return 0;
        }

        public int characteristics() {
            return 16448;
        }
    }

    static final class IntArraySpliterator implements OfInt {
        private final int[] array;
        private final int characteristics;
        private final int fence;
        private int index;

        public IntArraySpliterator(int[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        public IntArraySpliterator(int[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = (additionalCharacteristics | 64) | 16384;
        }

        public OfInt trySplit() {
            int lo = this.index;
            int mid = (this.fence + lo) >>> 1;
            if (lo >= mid) {
                return null;
            }
            int[] iArr = this.array;
            this.index = mid;
            return new IntArraySpliterator(iArr, lo, mid, this.characteristics);
        }

        public void forEachRemaining(IntConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            }
            int[] a = this.array;
            int length = a.length;
            int hi = this.fence;
            if (length >= hi) {
                int i = this.index;
                if (i >= 0) {
                    this.index = hi;
                    if (i < hi) {
                        do {
                            action.accept(a[i]);
                            i++;
                        } while (i < hi);
                    }
                }
            }
        }

        public boolean tryAdvance(IntConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            } else if (this.index < 0 || this.index >= this.fence) {
                return false;
            } else {
                int[] iArr = this.array;
                int i = this.index;
                this.index = i + 1;
                action.accept(iArr[i]);
                return true;
            }
        }

        public long estimateSize() {
            return (long) (this.fence - this.index);
        }

        public int characteristics() {
            return this.characteristics;
        }

        public Comparator<? super Integer> getComparator() {
            if (hasCharacteristics(4)) {
                return null;
            }
            throw new IllegalStateException();
        }
    }

    static final class IntIteratorSpliterator implements OfInt {
        static final int BATCH_UNIT = 1024;
        static final int MAX_BATCH = 33554432;
        private int batch;
        private final int characteristics;
        private long est;
        private PrimitiveIterator.OfInt it;

        public IntIteratorSpliterator(PrimitiveIterator.OfInt iterator, long size, int characteristics) {
            this.it = iterator;
            this.est = size;
            if ((characteristics & 4096) == 0) {
                characteristics = (characteristics | 64) | 16384;
            }
            this.characteristics = characteristics;
        }

        public IntIteratorSpliterator(PrimitiveIterator.OfInt iterator, int characteristics) {
            this.it = iterator;
            this.est = Long.MAX_VALUE;
            this.characteristics = characteristics & -16449;
        }

        public OfInt trySplit() {
            PrimitiveIterator.OfInt i = this.it;
            long s = this.est;
            if (s <= 1 || !i.hasNext()) {
                return null;
            }
            int n = this.batch + 1024;
            if (((long) n) > s) {
                n = (int) s;
            }
            if (n > MAX_BATCH) {
                n = MAX_BATCH;
            }
            int[] a = new int[n];
            int j = 0;
            do {
                a[j] = i.nextInt();
                j++;
                if (j >= n) {
                    break;
                }
            } while (i.hasNext());
            this.batch = j;
            if (this.est != Long.MAX_VALUE) {
                this.est -= (long) j;
            }
            return new IntArraySpliterator(a, 0, j, this.characteristics);
        }

        public void forEachRemaining(IntConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            }
            this.it.forEachRemaining(action);
        }

        public boolean tryAdvance(IntConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            } else if (!this.it.hasNext()) {
                return false;
            } else {
                action.accept(this.it.nextInt());
                return true;
            }
        }

        public long estimateSize() {
            return this.est;
        }

        public int characteristics() {
            return this.characteristics;
        }

        public Comparator<? super Integer> getComparator() {
            if (hasCharacteristics(4)) {
                return null;
            }
            throw new IllegalStateException();
        }
    }

    static final class LongArraySpliterator implements OfLong {
        private final long[] array;
        private final int characteristics;
        private final int fence;
        private int index;

        public LongArraySpliterator(long[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        public LongArraySpliterator(long[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = (additionalCharacteristics | 64) | 16384;
        }

        public OfLong trySplit() {
            int lo = this.index;
            int mid = (this.fence + lo) >>> 1;
            if (lo >= mid) {
                return null;
            }
            long[] jArr = this.array;
            this.index = mid;
            return new LongArraySpliterator(jArr, lo, mid, this.characteristics);
        }

        public void forEachRemaining(LongConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            }
            long[] a = this.array;
            int length = a.length;
            int hi = this.fence;
            if (length >= hi) {
                int i = this.index;
                if (i >= 0) {
                    this.index = hi;
                    if (i < hi) {
                        do {
                            action.accept(a[i]);
                            i++;
                        } while (i < hi);
                    }
                }
            }
        }

        public boolean tryAdvance(LongConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            } else if (this.index < 0 || this.index >= this.fence) {
                return false;
            } else {
                long[] jArr = this.array;
                int i = this.index;
                this.index = i + 1;
                action.accept(jArr[i]);
                return true;
            }
        }

        public long estimateSize() {
            return (long) (this.fence - this.index);
        }

        public int characteristics() {
            return this.characteristics;
        }

        public Comparator<? super Long> getComparator() {
            if (hasCharacteristics(4)) {
                return null;
            }
            throw new IllegalStateException();
        }
    }

    static final class LongIteratorSpliterator implements OfLong {
        static final int BATCH_UNIT = 1024;
        static final int MAX_BATCH = 33554432;
        private int batch;
        private final int characteristics;
        private long est;
        private PrimitiveIterator.OfLong it;

        public LongIteratorSpliterator(PrimitiveIterator.OfLong iterator, long size, int characteristics) {
            this.it = iterator;
            this.est = size;
            if ((characteristics & 4096) == 0) {
                characteristics = (characteristics | 64) | 16384;
            }
            this.characteristics = characteristics;
        }

        public LongIteratorSpliterator(PrimitiveIterator.OfLong iterator, int characteristics) {
            this.it = iterator;
            this.est = Long.MAX_VALUE;
            this.characteristics = characteristics & -16449;
        }

        public OfLong trySplit() {
            PrimitiveIterator.OfLong i = this.it;
            long s = this.est;
            if (s <= 1 || !i.hasNext()) {
                return null;
            }
            int n = this.batch + 1024;
            if (((long) n) > s) {
                n = (int) s;
            }
            if (n > MAX_BATCH) {
                n = MAX_BATCH;
            }
            long[] a = new long[n];
            int j = 0;
            do {
                a[j] = i.nextLong();
                j++;
                if (j >= n) {
                    break;
                }
            } while (i.hasNext());
            this.batch = j;
            if (this.est != Long.MAX_VALUE) {
                this.est -= (long) j;
            }
            return new LongArraySpliterator(a, 0, j, this.characteristics);
        }

        public void forEachRemaining(LongConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            }
            this.it.forEachRemaining(action);
        }

        public boolean tryAdvance(LongConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            } else if (!this.it.hasNext()) {
                return false;
            } else {
                action.accept(this.it.nextLong());
                return true;
            }
        }

        public long estimateSize() {
            return this.est;
        }

        public int characteristics() {
            return this.characteristics;
        }

        public Comparator<? super Long> getComparator() {
            if (hasCharacteristics(4)) {
                return null;
            }
            throw new IllegalStateException();
        }
    }

    private Spliterators() {
    }

    public static <T> Spliterator<T> emptySpliterator() {
        return EMPTY_SPLITERATOR;
    }

    public static OfInt emptyIntSpliterator() {
        return EMPTY_INT_SPLITERATOR;
    }

    public static OfLong emptyLongSpliterator() {
        return EMPTY_LONG_SPLITERATOR;
    }

    public static OfDouble emptyDoubleSpliterator() {
        return EMPTY_DOUBLE_SPLITERATOR;
    }

    public static <T> Spliterator<T> spliterator(Object[] array, int additionalCharacteristics) {
        return new ArraySpliterator((Object[]) Objects.requireNonNull(array), additionalCharacteristics);
    }

    public static <T> Spliterator<T> spliterator(Object[] array, int fromIndex, int toIndex, int additionalCharacteristics) {
        checkFromToBounds(((Object[]) Objects.requireNonNull(array)).length, fromIndex, toIndex);
        return new ArraySpliterator(array, fromIndex, toIndex, additionalCharacteristics);
    }

    public static OfInt spliterator(int[] array, int additionalCharacteristics) {
        return new IntArraySpliterator((int[]) Objects.requireNonNull(array), additionalCharacteristics);
    }

    public static OfInt spliterator(int[] array, int fromIndex, int toIndex, int additionalCharacteristics) {
        checkFromToBounds(((int[]) Objects.requireNonNull(array)).length, fromIndex, toIndex);
        return new IntArraySpliterator(array, fromIndex, toIndex, additionalCharacteristics);
    }

    public static OfLong spliterator(long[] array, int additionalCharacteristics) {
        return new LongArraySpliterator((long[]) Objects.requireNonNull(array), additionalCharacteristics);
    }

    public static OfLong spliterator(long[] array, int fromIndex, int toIndex, int additionalCharacteristics) {
        checkFromToBounds(((long[]) Objects.requireNonNull(array)).length, fromIndex, toIndex);
        return new LongArraySpliterator(array, fromIndex, toIndex, additionalCharacteristics);
    }

    public static OfDouble spliterator(double[] array, int additionalCharacteristics) {
        return new DoubleArraySpliterator((double[]) Objects.requireNonNull(array), additionalCharacteristics);
    }

    public static OfDouble spliterator(double[] array, int fromIndex, int toIndex, int additionalCharacteristics) {
        checkFromToBounds(((double[]) Objects.requireNonNull(array)).length, fromIndex, toIndex);
        return new DoubleArraySpliterator(array, fromIndex, toIndex, additionalCharacteristics);
    }

    private static void checkFromToBounds(int arrayLength, int origin, int fence) {
        if (origin > fence) {
            throw new ArrayIndexOutOfBoundsException("origin(" + origin + ") > fence(" + fence + ")");
        } else if (origin < 0) {
            throw new ArrayIndexOutOfBoundsException(origin);
        } else if (fence > arrayLength) {
            throw new ArrayIndexOutOfBoundsException(fence);
        }
    }

    public static <T> Spliterator<T> spliterator(Collection<? extends T> c, int characteristics) {
        return new IteratorSpliterator((Collection) Objects.requireNonNull(c), characteristics);
    }

    public static <T> Spliterator<T> spliterator(Iterator<? extends T> iterator, long size, int characteristics) {
        return new IteratorSpliterator((Iterator) Objects.requireNonNull(iterator), size, characteristics);
    }

    public static <T> Spliterator<T> spliteratorUnknownSize(Iterator<? extends T> iterator, int characteristics) {
        return new IteratorSpliterator((Iterator) Objects.requireNonNull(iterator), characteristics);
    }

    public static OfInt spliterator(PrimitiveIterator.OfInt iterator, long size, int characteristics) {
        return new IntIteratorSpliterator((PrimitiveIterator.OfInt) Objects.requireNonNull(iterator), size, characteristics);
    }

    public static OfInt spliteratorUnknownSize(PrimitiveIterator.OfInt iterator, int characteristics) {
        return new IntIteratorSpliterator((PrimitiveIterator.OfInt) Objects.requireNonNull(iterator), characteristics);
    }

    public static OfLong spliterator(PrimitiveIterator.OfLong iterator, long size, int characteristics) {
        return new LongIteratorSpliterator((PrimitiveIterator.OfLong) Objects.requireNonNull(iterator), size, characteristics);
    }

    public static OfLong spliteratorUnknownSize(PrimitiveIterator.OfLong iterator, int characteristics) {
        return new LongIteratorSpliterator((PrimitiveIterator.OfLong) Objects.requireNonNull(iterator), characteristics);
    }

    public static OfDouble spliterator(PrimitiveIterator.OfDouble iterator, long size, int characteristics) {
        return new DoubleIteratorSpliterator((PrimitiveIterator.OfDouble) Objects.requireNonNull(iterator), size, characteristics);
    }

    public static OfDouble spliteratorUnknownSize(PrimitiveIterator.OfDouble iterator, int characteristics) {
        return new DoubleIteratorSpliterator((PrimitiveIterator.OfDouble) Objects.requireNonNull(iterator), characteristics);
    }

    public static <T> Iterator<T> iterator(final Spliterator<? extends T> spliterator) {
        Objects.requireNonNull(spliterator);
        return new Object() {
            T nextElement;
            boolean valueReady = false;

            public void accept(T t) {
                this.valueReady = true;
                this.nextElement = t;
            }

            public boolean hasNext() {
                if (!this.valueReady) {
                    spliterator.tryAdvance(this);
                }
                return this.valueReady;
            }

            public T next() {
                if (this.valueReady || (hasNext() ^ 1) == 0) {
                    this.valueReady = false;
                    return this.nextElement;
                }
                throw new NoSuchElementException();
            }
        };
    }

    public static PrimitiveIterator.OfInt iterator(final OfInt spliterator) {
        Objects.requireNonNull(spliterator);
        return new Object() {
            int nextElement;
            boolean valueReady = false;

            public void accept(int t) {
                this.valueReady = true;
                this.nextElement = t;
            }

            public boolean hasNext() {
                if (!this.valueReady) {
                    spliterator.tryAdvance((IntConsumer) this);
                }
                return this.valueReady;
            }

            public int nextInt() {
                if (this.valueReady || (hasNext() ^ 1) == 0) {
                    this.valueReady = false;
                    return this.nextElement;
                }
                throw new NoSuchElementException();
            }
        };
    }

    public static PrimitiveIterator.OfLong iterator(final OfLong spliterator) {
        Objects.requireNonNull(spliterator);
        return new Object() {
            long nextElement;
            boolean valueReady = false;

            public void accept(long t) {
                this.valueReady = true;
                this.nextElement = t;
            }

            public boolean hasNext() {
                if (!this.valueReady) {
                    spliterator.tryAdvance((LongConsumer) this);
                }
                return this.valueReady;
            }

            public long nextLong() {
                if (this.valueReady || (hasNext() ^ 1) == 0) {
                    this.valueReady = false;
                    return this.nextElement;
                }
                throw new NoSuchElementException();
            }
        };
    }

    public static PrimitiveIterator.OfDouble iterator(final OfDouble spliterator) {
        Objects.requireNonNull(spliterator);
        return new Object() {
            double nextElement;
            boolean valueReady = false;

            public void accept(double t) {
                this.valueReady = true;
                this.nextElement = t;
            }

            public boolean hasNext() {
                if (!this.valueReady) {
                    spliterator.tryAdvance((DoubleConsumer) this);
                }
                return this.valueReady;
            }

            public double nextDouble() {
                if (this.valueReady || (hasNext() ^ 1) == 0) {
                    this.valueReady = false;
                    return this.nextElement;
                }
                throw new NoSuchElementException();
            }
        };
    }
}
