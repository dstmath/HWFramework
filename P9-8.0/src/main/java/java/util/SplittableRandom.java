package java.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.SecureRandom;
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

public final class SplittableRandom {
    static final String BAD_BOUND = "bound must be positive";
    static final String BAD_RANGE = "bound must be greater than origin";
    static final String BAD_SIZE = "size must be non-negative";
    private static final double DOUBLE_UNIT = 1.1102230246251565E-16d;
    private static final long GOLDEN_GAMMA = -7046029254386353131L;
    private static final AtomicLong defaultGen = new AtomicLong(mix64(System.currentTimeMillis()) ^ mix64(System.nanoTime()));
    private final long gamma;
    private long seed;

    private static final class RandomDoublesSpliterator implements OfDouble {
        final double bound;
        final long fence;
        long index;
        final double origin;
        final SplittableRandom rng;

        RandomDoublesSpliterator(SplittableRandom rng, long index, long fence, double origin, double bound) {
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
            SplittableRandom split = this.rng.split();
            this.index = m;
            return new RandomDoublesSpliterator(split, i, m, this.origin, this.bound);
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
                SplittableRandom r = this.rng;
                double o = this.origin;
                double b = this.bound;
                do {
                    consumer.accept(r.internalNextDouble(o, b));
                    i++;
                } while (i < f);
            }
        }
    }

    private static final class RandomIntsSpliterator implements OfInt {
        final int bound;
        final long fence;
        long index;
        final int origin;
        final SplittableRandom rng;

        RandomIntsSpliterator(SplittableRandom rng, long index, long fence, int origin, int bound) {
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
            SplittableRandom split = this.rng.split();
            this.index = m;
            return new RandomIntsSpliterator(split, i, m, this.origin, this.bound);
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
                SplittableRandom r = this.rng;
                int o = this.origin;
                int b = this.bound;
                do {
                    consumer.accept(r.internalNextInt(o, b));
                    i++;
                } while (i < f);
            }
        }
    }

    private static final class RandomLongsSpliterator implements OfLong {
        final long bound;
        final long fence;
        long index;
        final long origin;
        final SplittableRandom rng;

        RandomLongsSpliterator(SplittableRandom rng, long index, long fence, long origin, long bound) {
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
            SplittableRandom split = this.rng.split();
            this.index = m;
            return new RandomLongsSpliterator(split, i, m, this.origin, this.bound);
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
                SplittableRandom r = this.rng;
                long o = this.origin;
                long b = this.bound;
                do {
                    consumer.accept(r.internalNextLong(o, b));
                    i++;
                } while (i < f);
            }
        }
    }

    private SplittableRandom(long seed, long gamma) {
        this.seed = seed;
        this.gamma = gamma;
    }

    private static long mix64(long z) {
        z = ((z >>> 30) ^ z) * -4658895280553007687L;
        z = ((z >>> 27) ^ z) * -7723592293110705685L;
        return (z >>> 31) ^ z;
    }

    private static int mix32(long z) {
        z = ((z >>> 33) ^ z) * 7109453100751455733L;
        return (int) ((((z >>> 28) ^ z) * -3808689974395783757L) >>> 32);
    }

    private static long mixGamma(long z) {
        z = ((z >>> 33) ^ z) * -49064778989728563L;
        z = ((z >>> 33) ^ z) * -4265267296055464877L;
        z = ((z >>> 33) ^ z) | 1;
        return Long.bitCount((z >>> 1) ^ z) < 24 ? z ^ -6148914691236517206L : z;
    }

    private long nextSeed() {
        long j = this.seed + this.gamma;
        this.seed = j;
        return j;
    }

    static {
        if (((Boolean) AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            public Boolean run() {
                return Boolean.valueOf(Boolean.getBoolean("java.util.secureRandomSeed"));
            }
        })).booleanValue()) {
            byte[] seedBytes = SecureRandom.getSeed(8);
            long s = ((long) seedBytes[0]) & 255;
            for (int i = 1; i < 8; i++) {
                s = (s << 8) | (((long) seedBytes[i]) & 255);
            }
            defaultGen.set(s);
        }
    }

    final long internalNextLong(long origin, long bound) {
        long r = mix64(nextSeed());
        if (origin >= bound) {
            return r;
        }
        long n = bound - origin;
        long m = n - 1;
        if ((n & m) == 0) {
            return (r & m) + origin;
        }
        if (n > 0) {
            long u = r >>> 1;
            while (true) {
                r = u % n;
                if ((u + m) - r >= 0) {
                    return r + origin;
                }
                u = mix64(nextSeed()) >>> 1;
            }
        } else {
            while (true) {
                if (r >= origin && r < bound) {
                    return r;
                }
                r = mix64(nextSeed());
            }
        }
    }

    final int internalNextInt(int origin, int bound) {
        int r = mix32(nextSeed());
        if (origin >= bound) {
            return r;
        }
        int n = bound - origin;
        int m = n - 1;
        if ((n & m) == 0) {
            return (r & m) + origin;
        }
        if (n > 0) {
            int u = r >>> 1;
            while (true) {
                r = u % n;
                if ((u + m) - r >= 0) {
                    return r + origin;
                }
                u = mix32(nextSeed()) >>> 1;
            }
        } else {
            while (true) {
                if (r >= origin && r < bound) {
                    return r;
                }
                r = mix32(nextSeed());
            }
        }
    }

    final double internalNextDouble(double origin, double bound) {
        double r = ((double) (nextLong() >>> 11)) * DOUBLE_UNIT;
        if (origin >= bound) {
            return r;
        }
        r = ((bound - origin) * r) + origin;
        if (r >= bound) {
            return Double.longBitsToDouble(Double.doubleToLongBits(bound) - 1);
        }
        return r;
    }

    public SplittableRandom(long seed) {
        this(seed, GOLDEN_GAMMA);
    }

    public SplittableRandom() {
        long s = defaultGen.getAndAdd(4354685564936845354L);
        this.seed = mix64(s);
        this.gamma = mixGamma(GOLDEN_GAMMA + s);
    }

    public SplittableRandom split() {
        return new SplittableRandom(nextLong(), mixGamma(nextSeed()));
    }

    public int nextInt() {
        return mix32(nextSeed());
    }

    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException(BAD_BOUND);
        }
        int r = mix32(nextSeed());
        int m = bound - 1;
        if ((bound & m) == 0) {
            return r & m;
        }
        int u = r >>> 1;
        while (true) {
            r = u % bound;
            if ((u + m) - r >= 0) {
                return r;
            }
            u = mix32(nextSeed()) >>> 1;
        }
    }

    public int nextInt(int origin, int bound) {
        if (origin < bound) {
            return internalNextInt(origin, bound);
        }
        throw new IllegalArgumentException(BAD_RANGE);
    }

    public long nextLong() {
        return mix64(nextSeed());
    }

    public long nextLong(long bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException(BAD_BOUND);
        }
        long r = mix64(nextSeed());
        long m = bound - 1;
        if ((bound & m) == 0) {
            return r & m;
        }
        long u = r >>> 1;
        while (true) {
            r = u % bound;
            if ((u + m) - r >= 0) {
                return r;
            }
            u = mix64(nextSeed()) >>> 1;
        }
    }

    public long nextLong(long origin, long bound) {
        if (origin < bound) {
            return internalNextLong(origin, bound);
        }
        throw new IllegalArgumentException(BAD_RANGE);
    }

    public double nextDouble() {
        return ((double) (mix64(nextSeed()) >>> 11)) * DOUBLE_UNIT;
    }

    public double nextDouble(double bound) {
        if ((bound > 0.0d ? 1 : null) == null) {
            throw new IllegalArgumentException(BAD_BOUND);
        }
        double result = (((double) (mix64(nextSeed()) >>> 11)) * DOUBLE_UNIT) * bound;
        if (result < bound) {
            return result;
        }
        return Double.longBitsToDouble(Double.doubleToLongBits(bound) - 1);
    }

    public double nextDouble(double origin, double bound) {
        if ((origin < bound ? 1 : null) != null) {
            return internalNextDouble(origin, bound);
        }
        throw new IllegalArgumentException(BAD_RANGE);
    }

    public boolean nextBoolean() {
        return mix32(nextSeed()) < 0;
    }

    public IntStream ints(long streamSize) {
        if (streamSize >= 0) {
            return StreamSupport.intStream(new RandomIntsSpliterator(this, 0, streamSize, Integer.MAX_VALUE, 0), false);
        }
        throw new IllegalArgumentException(BAD_SIZE);
    }

    public IntStream ints() {
        return StreamSupport.intStream(new RandomIntsSpliterator(this, 0, Long.MAX_VALUE, Integer.MAX_VALUE, 0), false);
    }

    public IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound) {
        if (streamSize < 0) {
            throw new IllegalArgumentException(BAD_SIZE);
        } else if (randomNumberOrigin < randomNumberBound) {
            return StreamSupport.intStream(new RandomIntsSpliterator(this, 0, streamSize, randomNumberOrigin, randomNumberBound), false);
        } else {
            throw new IllegalArgumentException(BAD_RANGE);
        }
    }

    public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
        if (randomNumberOrigin < randomNumberBound) {
            return StreamSupport.intStream(new RandomIntsSpliterator(this, 0, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound), false);
        }
        throw new IllegalArgumentException(BAD_RANGE);
    }

    public LongStream longs(long streamSize) {
        if (streamSize >= 0) {
            return StreamSupport.longStream(new RandomLongsSpliterator(this, 0, streamSize, Long.MAX_VALUE, 0), false);
        }
        throw new IllegalArgumentException(BAD_SIZE);
    }

    public LongStream longs() {
        return StreamSupport.longStream(new RandomLongsSpliterator(this, 0, Long.MAX_VALUE, Long.MAX_VALUE, 0), false);
    }

    public LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound) {
        if (streamSize < 0) {
            throw new IllegalArgumentException(BAD_SIZE);
        } else if (randomNumberOrigin < randomNumberBound) {
            return StreamSupport.longStream(new RandomLongsSpliterator(this, 0, streamSize, randomNumberOrigin, randomNumberBound), false);
        } else {
            throw new IllegalArgumentException(BAD_RANGE);
        }
    }

    public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
        if (randomNumberOrigin < randomNumberBound) {
            return StreamSupport.longStream(new RandomLongsSpliterator(this, 0, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound), false);
        }
        throw new IllegalArgumentException(BAD_RANGE);
    }

    public DoubleStream doubles(long streamSize) {
        if (streamSize >= 0) {
            return StreamSupport.doubleStream(new RandomDoublesSpliterator(this, 0, streamSize, Double.MAX_VALUE, 0.0d), false);
        }
        throw new IllegalArgumentException(BAD_SIZE);
    }

    public DoubleStream doubles() {
        return StreamSupport.doubleStream(new RandomDoublesSpliterator(this, 0, Long.MAX_VALUE, Double.MAX_VALUE, 0.0d), false);
    }

    public DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound) {
        if (streamSize < 0) {
            throw new IllegalArgumentException(BAD_SIZE);
        }
        if ((randomNumberOrigin < randomNumberBound ? 1 : null) != null) {
            return StreamSupport.doubleStream(new RandomDoublesSpliterator(this, 0, streamSize, randomNumberOrigin, randomNumberBound), false);
        }
        throw new IllegalArgumentException(BAD_RANGE);
    }

    public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
        boolean z;
        if (randomNumberOrigin < randomNumberBound) {
            z = true;
        } else {
            z = false;
        }
        if (z) {
            return StreamSupport.doubleStream(new RandomDoublesSpliterator(this, 0, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound), false);
        }
        throw new IllegalArgumentException(BAD_RANGE);
    }
}
