package java.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.SecureRandom;
import java.util.Spliterator;
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

    private static final class RandomDoublesSpliterator implements Spliterator.OfDouble {
        final double bound;
        final long fence;
        long index;
        final double origin;
        final SplittableRandom rng;

        RandomDoublesSpliterator(SplittableRandom rng2, long index2, long fence2, double origin2, double bound2) {
            this.rng = rng2;
            this.index = index2;
            this.fence = fence2;
            this.origin = origin2;
            this.bound = bound2;
        }

        public RandomDoublesSpliterator trySplit() {
            long i = this.index;
            long m = (this.fence + i) >>> 1;
            if (m <= i) {
                return null;
            }
            SplittableRandom split = this.rng.split();
            this.index = m;
            RandomDoublesSpliterator randomDoublesSpliterator = new RandomDoublesSpliterator(split, i, m, this.origin, this.bound);
            return randomDoublesSpliterator;
        }

        public long estimateSize() {
            return this.fence - this.index;
        }

        public int characteristics() {
            return 17728;
        }

        public boolean tryAdvance(DoubleConsumer consumer) {
            if (consumer != null) {
                long i = this.index;
                if (i >= this.fence) {
                    return false;
                }
                consumer.accept(this.rng.internalNextDouble(this.origin, this.bound));
                this.index = 1 + i;
                return true;
            }
            throw new NullPointerException();
        }

        public void forEachRemaining(DoubleConsumer consumer) {
            long j;
            if (consumer != null) {
                long i = this.index;
                long f = this.fence;
                if (i < f) {
                    this.index = f;
                    SplittableRandom r = this.rng;
                    double o = this.origin;
                    double b = this.bound;
                    do {
                        consumer.accept(r.internalNextDouble(o, b));
                        j = 1 + i;
                        i = j;
                    } while (j < f);
                    return;
                }
                return;
            }
            throw new NullPointerException();
        }
    }

    private static final class RandomIntsSpliterator implements Spliterator.OfInt {
        final int bound;
        final long fence;
        long index;
        final int origin;
        final SplittableRandom rng;

        RandomIntsSpliterator(SplittableRandom rng2, long index2, long fence2, int origin2, int bound2) {
            this.rng = rng2;
            this.index = index2;
            this.fence = fence2;
            this.origin = origin2;
            this.bound = bound2;
        }

        public RandomIntsSpliterator trySplit() {
            long i = this.index;
            long m = (this.fence + i) >>> 1;
            if (m <= i) {
                return null;
            }
            SplittableRandom split = this.rng.split();
            this.index = m;
            RandomIntsSpliterator randomIntsSpliterator = new RandomIntsSpliterator(split, i, m, this.origin, this.bound);
            return randomIntsSpliterator;
        }

        public long estimateSize() {
            return this.fence - this.index;
        }

        public int characteristics() {
            return 17728;
        }

        public boolean tryAdvance(IntConsumer consumer) {
            if (consumer != null) {
                long i = this.index;
                if (i >= this.fence) {
                    return false;
                }
                consumer.accept(this.rng.internalNextInt(this.origin, this.bound));
                this.index = 1 + i;
                return true;
            }
            throw new NullPointerException();
        }

        public void forEachRemaining(IntConsumer consumer) {
            long j;
            if (consumer != null) {
                long i = this.index;
                long f = this.fence;
                if (i < f) {
                    this.index = f;
                    SplittableRandom r = this.rng;
                    int o = this.origin;
                    int b = this.bound;
                    do {
                        consumer.accept(r.internalNextInt(o, b));
                        j = 1 + i;
                        i = j;
                    } while (j < f);
                    return;
                }
                return;
            }
            throw new NullPointerException();
        }
    }

    private static final class RandomLongsSpliterator implements Spliterator.OfLong {
        final long bound;
        final long fence;
        long index;
        final long origin;
        final SplittableRandom rng;

        RandomLongsSpliterator(SplittableRandom rng2, long index2, long fence2, long origin2, long bound2) {
            this.rng = rng2;
            this.index = index2;
            this.fence = fence2;
            this.origin = origin2;
            this.bound = bound2;
        }

        public RandomLongsSpliterator trySplit() {
            long i = this.index;
            long m = (this.fence + i) >>> 1;
            if (m <= i) {
                return null;
            }
            SplittableRandom split = this.rng.split();
            this.index = m;
            RandomLongsSpliterator randomLongsSpliterator = new RandomLongsSpliterator(split, i, m, this.origin, this.bound);
            return randomLongsSpliterator;
        }

        public long estimateSize() {
            return this.fence - this.index;
        }

        public int characteristics() {
            return 17728;
        }

        public boolean tryAdvance(LongConsumer consumer) {
            if (consumer != null) {
                long i = this.index;
                if (i >= this.fence) {
                    return false;
                }
                consumer.accept(this.rng.internalNextLong(this.origin, this.bound));
                this.index = 1 + i;
                return true;
            }
            throw new NullPointerException();
        }

        public void forEachRemaining(LongConsumer consumer) {
            long j;
            if (consumer != null) {
                long i = this.index;
                long f = this.fence;
                if (i < f) {
                    this.index = f;
                    SplittableRandom r = this.rng;
                    long o = this.origin;
                    long b = this.bound;
                    do {
                        consumer.accept(r.internalNextLong(o, b));
                        j = 1 + i;
                        i = j;
                    } while (j < f);
                    return;
                }
                return;
            }
            throw new NullPointerException();
        }
    }

    private SplittableRandom(long seed2, long gamma2) {
        this.seed = seed2;
        this.gamma = gamma2;
    }

    private static long mix64(long z) {
        long z2 = ((z >>> 30) ^ z) * -4658895280553007687L;
        long z3 = ((z2 >>> 27) ^ z2) * -7723592293110705685L;
        return (z3 >>> 31) ^ z3;
    }

    private static int mix32(long z) {
        long z2 = ((z >>> 33) ^ z) * 7109453100751455733L;
        return (int) ((((z2 >>> 28) ^ z2) * -3808689974395783757L) >>> 32);
    }

    private static long mixGamma(long z) {
        long z2 = ((z >>> 33) ^ z) * -49064778989728563L;
        long z3 = ((z2 >>> 33) ^ z2) * -4265267296055464877L;
        long z4 = ((z3 >>> 33) ^ z3) | 1;
        return Long.bitCount((z4 >>> 1) ^ z4) < 24 ? -6148914691236517206L ^ z4 : z4;
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

    /* access modifiers changed from: package-private */
    public final long internalNextLong(long origin, long bound) {
        long r = mix64(nextSeed());
        if (origin >= bound) {
            return r;
        }
        long n = bound - origin;
        long m = n - 1;
        long j = 0;
        if ((n & m) == 0) {
            return (r & m) + origin;
        }
        if (n > 0) {
            long u = r >>> 1;
            while (true) {
                long j2 = u % n;
                long r2 = j2;
                if ((u + m) - j2 >= j) {
                    return r2 + origin;
                }
                u = mix64(nextSeed()) >>> 1;
                j = 0;
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

    /* access modifiers changed from: package-private */
    public final int internalNextInt(int origin, int bound) {
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
                int i = u % n;
                int r2 = i;
                if ((u + m) - i >= 0) {
                    return r2 + origin;
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

    /* access modifiers changed from: package-private */
    public final double internalNextDouble(double origin, double bound) {
        double r = ((double) (nextLong() >>> 11)) * DOUBLE_UNIT;
        if (origin >= bound) {
            return r;
        }
        double r2 = ((bound - origin) * r) + origin;
        if (r2 >= bound) {
            return Double.longBitsToDouble(Double.doubleToLongBits(bound) - 1);
        }
        return r2;
    }

    public SplittableRandom(long seed2) {
        this(seed2, GOLDEN_GAMMA);
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
        if (bound > 0) {
            int r = mix32(nextSeed());
            int m = bound - 1;
            if ((bound & m) == 0) {
                return r & m;
            }
            int u = r >>> 1;
            while (true) {
                int i = u % bound;
                int r2 = i;
                if ((u + m) - i >= 0) {
                    return r2;
                }
                u = mix32(nextSeed()) >>> 1;
            }
        } else {
            throw new IllegalArgumentException(BAD_BOUND);
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
        if (bound > 0) {
            long r = mix64(nextSeed());
            long m = bound - 1;
            if ((bound & m) == 0) {
                return r & m;
            }
            long u = r >>> 1;
            while (true) {
                long j = u % bound;
                long r2 = j;
                if ((u + m) - j >= 0) {
                    return r2;
                }
                u = mix64(nextSeed()) >>> 1;
            }
        } else {
            throw new IllegalArgumentException(BAD_BOUND);
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
        if (bound > 0.0d) {
            double result = ((double) (mix64(nextSeed()) >>> 11)) * DOUBLE_UNIT * bound;
            if (result < bound) {
                return result;
            }
            return Double.longBitsToDouble(Double.doubleToLongBits(bound) - 1);
        }
        throw new IllegalArgumentException(BAD_BOUND);
    }

    public double nextDouble(double origin, double bound) {
        if (origin < bound) {
            return internalNextDouble(origin, bound);
        }
        throw new IllegalArgumentException(BAD_RANGE);
    }

    public boolean nextBoolean() {
        return mix32(nextSeed()) < 0;
    }

    public IntStream ints(long streamSize) {
        if (streamSize >= 0) {
            RandomIntsSpliterator randomIntsSpliterator = new RandomIntsSpliterator(this, 0, streamSize, Integer.MAX_VALUE, 0);
            return StreamSupport.intStream(randomIntsSpliterator, false);
        }
        throw new IllegalArgumentException(BAD_SIZE);
    }

    public IntStream ints() {
        RandomIntsSpliterator randomIntsSpliterator = new RandomIntsSpliterator(this, 0, Long.MAX_VALUE, Integer.MAX_VALUE, 0);
        return StreamSupport.intStream(randomIntsSpliterator, false);
    }

    public IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound) {
        if (streamSize < 0) {
            throw new IllegalArgumentException(BAD_SIZE);
        } else if (randomNumberOrigin < randomNumberBound) {
            RandomIntsSpliterator randomIntsSpliterator = new RandomIntsSpliterator(this, 0, streamSize, randomNumberOrigin, randomNumberBound);
            return StreamSupport.intStream(randomIntsSpliterator, false);
        } else {
            throw new IllegalArgumentException(BAD_RANGE);
        }
    }

    public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
        if (randomNumberOrigin < randomNumberBound) {
            RandomIntsSpliterator randomIntsSpliterator = new RandomIntsSpliterator(this, 0, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound);
            return StreamSupport.intStream(randomIntsSpliterator, false);
        }
        throw new IllegalArgumentException(BAD_RANGE);
    }

    public LongStream longs(long streamSize) {
        if (streamSize >= 0) {
            RandomLongsSpliterator randomLongsSpliterator = new RandomLongsSpliterator(this, 0, streamSize, Long.MAX_VALUE, 0);
            return StreamSupport.longStream(randomLongsSpliterator, false);
        }
        throw new IllegalArgumentException(BAD_SIZE);
    }

    public LongStream longs() {
        RandomLongsSpliterator randomLongsSpliterator = new RandomLongsSpliterator(this, 0, Long.MAX_VALUE, Long.MAX_VALUE, 0);
        return StreamSupport.longStream(randomLongsSpliterator, false);
    }

    public LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound) {
        if (streamSize < 0) {
            throw new IllegalArgumentException(BAD_SIZE);
        } else if (randomNumberOrigin < randomNumberBound) {
            RandomLongsSpliterator randomLongsSpliterator = r2;
            RandomLongsSpliterator randomLongsSpliterator2 = new RandomLongsSpliterator(this, 0, streamSize, randomNumberOrigin, randomNumberBound);
            return StreamSupport.longStream(randomLongsSpliterator, false);
        } else {
            throw new IllegalArgumentException(BAD_RANGE);
        }
    }

    public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
        if (randomNumberOrigin < randomNumberBound) {
            RandomLongsSpliterator randomLongsSpliterator = new RandomLongsSpliterator(this, 0, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound);
            return StreamSupport.longStream(randomLongsSpliterator, false);
        }
        throw new IllegalArgumentException(BAD_RANGE);
    }

    public DoubleStream doubles(long streamSize) {
        if (streamSize >= 0) {
            RandomDoublesSpliterator randomDoublesSpliterator = new RandomDoublesSpliterator(this, 0, streamSize, Double.MAX_VALUE, 0.0d);
            return StreamSupport.doubleStream(randomDoublesSpliterator, false);
        }
        throw new IllegalArgumentException(BAD_SIZE);
    }

    public DoubleStream doubles() {
        RandomDoublesSpliterator randomDoublesSpliterator = new RandomDoublesSpliterator(this, 0, Long.MAX_VALUE, Double.MAX_VALUE, 0.0d);
        return StreamSupport.doubleStream(randomDoublesSpliterator, false);
    }

    public DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound) {
        if (streamSize < 0) {
            throw new IllegalArgumentException(BAD_SIZE);
        } else if (randomNumberOrigin < randomNumberBound) {
            RandomDoublesSpliterator randomDoublesSpliterator = r2;
            RandomDoublesSpliterator randomDoublesSpliterator2 = new RandomDoublesSpliterator(this, 0, streamSize, randomNumberOrigin, randomNumberBound);
            return StreamSupport.doubleStream(randomDoublesSpliterator, false);
        } else {
            throw new IllegalArgumentException(BAD_RANGE);
        }
    }

    public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
        if (randomNumberOrigin < randomNumberBound) {
            RandomDoublesSpliterator randomDoublesSpliterator = new RandomDoublesSpliterator(this, 0, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound);
            return StreamSupport.doubleStream(randomDoublesSpliterator, false);
        }
        throw new IllegalArgumentException(BAD_RANGE);
    }
}
