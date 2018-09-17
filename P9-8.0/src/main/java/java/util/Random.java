package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Spliterator.OfDouble;
import java.util.Spliterator.OfInt;
import java.util.Spliterator.OfLong;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;
import sun.misc.Unsafe;

public class Random implements Serializable {
    static final String BadBound = "bound must be positive";
    static final String BadRange = "bound must be greater than origin";
    static final String BadSize = "size must be non-negative";
    private static final double DOUBLE_UNIT = 1.1102230246251565E-16d;
    private static final long addend = 11;
    private static final long mask = 281474976710655L;
    private static final long multiplier = 25214903917L;
    private static final long seedOffset;
    private static final AtomicLong seedUniquifier = new AtomicLong(8682522807148012L);
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("seed", Long.TYPE), new ObjectStreamField("nextNextGaussian", Double.TYPE), new ObjectStreamField("haveNextNextGaussian", Boolean.TYPE)};
    static final long serialVersionUID = 3905348978240129619L;
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private boolean haveNextNextGaussian;
    private double nextNextGaussian;
    private final AtomicLong seed;

    static final class RandomDoublesSpliterator implements OfDouble {
        final double bound;
        final long fence;
        long index;
        final double origin;
        final Random rng;

        RandomDoublesSpliterator(Random rng, long index, long fence, double origin, double bound) {
            this.rng = rng;
            this.index = index;
            this.fence = fence;
            this.origin = origin;
            this.bound = bound;
        }

        public RandomDoublesSpliterator trySplit() {
            long i = this.index;
            long m = (this.fence + i) >>> 1;
            if (m <= i) {
                return null;
            }
            Random random = this.rng;
            this.index = m;
            return new RandomDoublesSpliterator(random, i, m, this.origin, this.bound);
        }

        public long estimateSize() {
            return this.fence - this.index;
        }

        public int characteristics() {
            return 17728;
        }

        public boolean tryAdvance(DoubleConsumer consumer) {
            if (consumer == null) {
                throw new NullPointerException();
            }
            long i = this.index;
            if (i >= this.fence) {
                return false;
            }
            consumer.accept(this.rng.internalNextDouble(this.origin, this.bound));
            this.index = 1 + i;
            return true;
        }

        public void forEachRemaining(DoubleConsumer consumer) {
            if (consumer == null) {
                throw new NullPointerException();
            }
            long i = this.index;
            long f = this.fence;
            if (i < f) {
                this.index = f;
                Random r = this.rng;
                double o = this.origin;
                double b = this.bound;
                do {
                    consumer.accept(r.internalNextDouble(o, b));
                    i++;
                } while (i < f);
            }
        }
    }

    static final class RandomIntsSpliterator implements OfInt {
        final int bound;
        final long fence;
        long index;
        final int origin;
        final Random rng;

        RandomIntsSpliterator(Random rng, long index, long fence, int origin, int bound) {
            this.rng = rng;
            this.index = index;
            this.fence = fence;
            this.origin = origin;
            this.bound = bound;
        }

        public RandomIntsSpliterator trySplit() {
            long i = this.index;
            long m = (this.fence + i) >>> 1;
            if (m <= i) {
                return null;
            }
            Random random = this.rng;
            this.index = m;
            return new RandomIntsSpliterator(random, i, m, this.origin, this.bound);
        }

        public long estimateSize() {
            return this.fence - this.index;
        }

        public int characteristics() {
            return 17728;
        }

        public boolean tryAdvance(IntConsumer consumer) {
            if (consumer == null) {
                throw new NullPointerException();
            }
            long i = this.index;
            if (i >= this.fence) {
                return false;
            }
            consumer.accept(this.rng.internalNextInt(this.origin, this.bound));
            this.index = 1 + i;
            return true;
        }

        public void forEachRemaining(IntConsumer consumer) {
            if (consumer == null) {
                throw new NullPointerException();
            }
            long i = this.index;
            long f = this.fence;
            if (i < f) {
                this.index = f;
                Random r = this.rng;
                int o = this.origin;
                int b = this.bound;
                do {
                    consumer.accept(r.internalNextInt(o, b));
                    i++;
                } while (i < f);
            }
        }
    }

    static final class RandomLongsSpliterator implements OfLong {
        final long bound;
        final long fence;
        long index;
        final long origin;
        final Random rng;

        RandomLongsSpliterator(Random rng, long index, long fence, long origin, long bound) {
            this.rng = rng;
            this.index = index;
            this.fence = fence;
            this.origin = origin;
            this.bound = bound;
        }

        public RandomLongsSpliterator trySplit() {
            long i = this.index;
            long m = (this.fence + i) >>> 1;
            if (m <= i) {
                return null;
            }
            Random random = this.rng;
            this.index = m;
            return new RandomLongsSpliterator(random, i, m, this.origin, this.bound);
        }

        public long estimateSize() {
            return this.fence - this.index;
        }

        public int characteristics() {
            return 17728;
        }

        public boolean tryAdvance(LongConsumer consumer) {
            if (consumer == null) {
                throw new NullPointerException();
            }
            long i = this.index;
            if (i >= this.fence) {
                return false;
            }
            consumer.accept(this.rng.internalNextLong(this.origin, this.bound));
            this.index = 1 + i;
            return true;
        }

        public void forEachRemaining(LongConsumer consumer) {
            if (consumer == null) {
                throw new NullPointerException();
            }
            long i = this.index;
            long f = this.fence;
            if (i < f) {
                this.index = f;
                Random r = this.rng;
                long o = this.origin;
                long b = this.bound;
                do {
                    consumer.accept(r.internalNextLong(o, b));
                    i++;
                } while (i < f);
            }
        }
    }

    public Random() {
        this(seedUniquifier() ^ System.nanoTime());
    }

    private static long seedUniquifier() {
        long next;
        long current;
        do {
            current = seedUniquifier.get();
            next = current * 181783497276652981L;
        } while (!seedUniquifier.compareAndSet(current, next));
        return next;
    }

    static {
        try {
            seedOffset = unsafe.objectFieldOffset(Random.class.getDeclaredField("seed"));
        } catch (Throwable ex) {
            throw new Error(ex);
        }
    }

    public Random(long seed) {
        this.haveNextNextGaussian = false;
        if (getClass() == Random.class) {
            this.seed = new AtomicLong(initialScramble(seed));
            return;
        }
        this.seed = new AtomicLong();
        setSeed(seed);
    }

    private static long initialScramble(long seed) {
        return (multiplier ^ seed) & mask;
    }

    public synchronized void setSeed(long seed) {
        this.seed.set(initialScramble(seed));
        this.haveNextNextGaussian = false;
    }

    protected int next(int bits) {
        long nextseed;
        AtomicLong seed = this.seed;
        long oldseed;
        do {
            oldseed = seed.get();
            nextseed = ((multiplier * oldseed) + addend) & mask;
        } while (!seed.compareAndSet(oldseed, nextseed));
        return (int) (nextseed >>> (48 - bits));
    }

    public void nextBytes(byte[] bytes) {
        int i = 0;
        int len = bytes.length;
        while (i < len) {
            int i2;
            int rnd = nextInt();
            int min = Math.min(len - i, 4);
            while (true) {
                int n = min;
                i2 = i;
                min = n - 1;
                if (n <= 0) {
                    break;
                }
                i = i2 + 1;
                bytes[i2] = (byte) rnd;
                rnd >>= 8;
            }
            i = i2;
        }
    }

    final long internalNextLong(long origin, long bound) {
        long r = nextLong();
        if (origin >= bound) {
            return r;
        }
        long n = bound - origin;
        long m = n - 1;
        if ((n & m) == seedOffset) {
            return (r & m) + origin;
        }
        if (n > seedOffset) {
            long u = r >>> 1;
            while (true) {
                r = u % n;
                if ((u + m) - r >= seedOffset) {
                    return r + origin;
                }
                u = nextLong() >>> 1;
            }
        } else {
            while (true) {
                if (r >= origin && r < bound) {
                    return r;
                }
                r = nextLong();
            }
        }
    }

    final int internalNextInt(int origin, int bound) {
        if (origin >= bound) {
            return nextInt();
        }
        int n = bound - origin;
        if (n > 0) {
            return nextInt(n) + origin;
        }
        while (true) {
            int r = nextInt();
            if (r >= origin && r < bound) {
                return r;
            }
        }
    }

    final double internalNextDouble(double origin, double bound) {
        double r = nextDouble();
        if (origin >= bound) {
            return r;
        }
        r = ((bound - origin) * r) + origin;
        if (r >= bound) {
            return Double.longBitsToDouble(Double.doubleToLongBits(bound) - 1);
        }
        return r;
    }

    public int nextInt() {
        return next(32);
    }

    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException(BadBound);
        }
        int r = next(31);
        int m = bound - 1;
        if ((bound & m) == 0) {
            return (int) ((((long) bound) * ((long) r)) >> 31);
        }
        int u = r;
        while (true) {
            r = u % bound;
            if ((u - r) + m >= 0) {
                return r;
            }
            u = next(31);
        }
    }

    public long nextLong() {
        return (((long) next(32)) << 32) + ((long) next(32));
    }

    public boolean nextBoolean() {
        return next(1) != 0;
    }

    public float nextFloat() {
        return ((float) next(24)) / 1.6777216E7f;
    }

    public double nextDouble() {
        return ((double) ((((long) next(26)) << 27) + ((long) next(27)))) * DOUBLE_UNIT;
    }

    public synchronized double nextGaussian() {
        if (this.haveNextNextGaussian) {
            this.haveNextNextGaussian = false;
            return this.nextNextGaussian;
        }
        while (true) {
            double v1 = (nextDouble() * 2.0d) - 1.0d;
            double v2 = (nextDouble() * 2.0d) - 1.0d;
            double s = (v1 * v1) + (v2 * v2);
            if (s < 1.0d && s != 0.0d) {
                double multiplier = StrictMath.sqrt((StrictMath.log(s) * -2.0d) / s);
                this.nextNextGaussian = v2 * multiplier;
                this.haveNextNextGaussian = true;
                return v1 * multiplier;
            }
        }
    }

    public IntStream ints(long streamSize) {
        if (streamSize >= seedOffset) {
            return StreamSupport.intStream(new RandomIntsSpliterator(this, seedOffset, streamSize, Integer.MAX_VALUE, 0), false);
        }
        throw new IllegalArgumentException(BadSize);
    }

    public IntStream ints() {
        return StreamSupport.intStream(new RandomIntsSpliterator(this, seedOffset, Long.MAX_VALUE, Integer.MAX_VALUE, 0), false);
    }

    public IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound) {
        if (streamSize < seedOffset) {
            throw new IllegalArgumentException(BadSize);
        } else if (randomNumberOrigin < randomNumberBound) {
            return StreamSupport.intStream(new RandomIntsSpliterator(this, seedOffset, streamSize, randomNumberOrigin, randomNumberBound), false);
        } else {
            throw new IllegalArgumentException(BadRange);
        }
    }

    public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
        if (randomNumberOrigin < randomNumberBound) {
            return StreamSupport.intStream(new RandomIntsSpliterator(this, seedOffset, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound), false);
        }
        throw new IllegalArgumentException(BadRange);
    }

    public LongStream longs(long streamSize) {
        if (streamSize >= seedOffset) {
            return StreamSupport.longStream(new RandomLongsSpliterator(this, seedOffset, streamSize, Long.MAX_VALUE, seedOffset), false);
        }
        throw new IllegalArgumentException(BadSize);
    }

    public LongStream longs() {
        return StreamSupport.longStream(new RandomLongsSpliterator(this, seedOffset, Long.MAX_VALUE, Long.MAX_VALUE, seedOffset), false);
    }

    public LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound) {
        if (streamSize < seedOffset) {
            throw new IllegalArgumentException(BadSize);
        } else if (randomNumberOrigin < randomNumberBound) {
            return StreamSupport.longStream(new RandomLongsSpliterator(this, seedOffset, streamSize, randomNumberOrigin, randomNumberBound), false);
        } else {
            throw new IllegalArgumentException(BadRange);
        }
    }

    public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
        if (randomNumberOrigin < randomNumberBound) {
            return StreamSupport.longStream(new RandomLongsSpliterator(this, seedOffset, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound), false);
        }
        throw new IllegalArgumentException(BadRange);
    }

    public DoubleStream doubles(long streamSize) {
        if (streamSize >= seedOffset) {
            return StreamSupport.doubleStream(new RandomDoublesSpliterator(this, seedOffset, streamSize, Double.MAX_VALUE, 0.0d), false);
        }
        throw new IllegalArgumentException(BadSize);
    }

    public DoubleStream doubles() {
        return StreamSupport.doubleStream(new RandomDoublesSpliterator(this, seedOffset, Long.MAX_VALUE, Double.MAX_VALUE, 0.0d), false);
    }

    public DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound) {
        if (streamSize < seedOffset) {
            throw new IllegalArgumentException(BadSize);
        }
        if ((randomNumberOrigin < randomNumberBound ? 1 : null) != null) {
            return StreamSupport.doubleStream(new RandomDoublesSpliterator(this, seedOffset, streamSize, randomNumberOrigin, randomNumberBound), false);
        }
        throw new IllegalArgumentException(BadRange);
    }

    public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
        boolean z;
        if (randomNumberOrigin < randomNumberBound) {
            z = true;
        } else {
            z = false;
        }
        if (z) {
            return StreamSupport.doubleStream(new RandomDoublesSpliterator(this, seedOffset, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound), false);
        }
        throw new IllegalArgumentException(BadRange);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        GetField fields = s.readFields();
        long seedVal = fields.get("seed", -1);
        if (seedVal < seedOffset) {
            throw new StreamCorruptedException("Random: invalid seed");
        }
        resetSeed(seedVal);
        this.nextNextGaussian = fields.get("nextNextGaussian", 0.0d);
        this.haveNextNextGaussian = fields.get("haveNextNextGaussian", false);
    }

    private synchronized void writeObject(ObjectOutputStream s) throws IOException {
        PutField fields = s.putFields();
        fields.put("seed", this.seed.get());
        fields.put("nextNextGaussian", this.nextNextGaussian);
        fields.put("haveNextNextGaussian", this.haveNextNextGaussian);
        s.writeFields();
    }

    private void resetSeed(long seedVal) {
        unsafe.putObjectVolatile(this, seedOffset, new AtomicLong(seedVal));
    }
}
