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
import sun.misc.DoubleConsts;
import sun.misc.Unsafe;
import sun.util.logging.PlatformLogger;

public class Random implements Serializable {
    static final String BadBound = "bound must be positive";
    static final String BadRange = "bound must be greater than origin";
    static final String BadSize = "size must be non-negative";
    private static final double DOUBLE_UNIT = 1.1102230246251565E-16d;
    private static final long addend = 11;
    private static final long mask = 281474976710655L;
    private static final long multiplier = 25214903917L;
    private static final long seedOffset = 0;
    private static final AtomicLong seedUniquifier = null;
    private static final ObjectStreamField[] serialPersistentFields = null;
    static final long serialVersionUID = 3905348978240129619L;
    private static final Unsafe unsafe = null;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.Random.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.Random.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.Random.<clinit>():void");
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
            int rnd = nextInt();
            int n = Math.min(len - i, 4);
            int i2 = i;
            while (true) {
                int n2 = n - 1;
                if (n <= 0) {
                    break;
                }
                i = i2 + 1;
                bytes[i2] = (byte) rnd;
                rnd >>= 8;
                n = n2;
                i2 = i;
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

    public int nextInt(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be positive");
        } else if (((-n) & n) == n) {
            return (int) ((((long) n) * ((long) next(31))) >> 31);
        } else {
            int val;
            int bits;
            do {
                bits = next(31);
                val = bits % n;
            } while ((bits - val) + (n - 1) < 0);
            return val;
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
        return ((double) ((((long) next(26)) << 27) + ((long) next(27)))) / 9.007199254740992E15d;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
            return StreamSupport.intStream(new RandomIntsSpliterator(this, seedOffset, streamSize, PlatformLogger.OFF, 0), false);
        }
        throw new IllegalArgumentException(BadSize);
    }

    public IntStream ints() {
        return StreamSupport.intStream(new RandomIntsSpliterator(this, seedOffset, Long.MAX_VALUE, PlatformLogger.OFF, 0), false);
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
            return StreamSupport.doubleStream(new RandomDoublesSpliterator(this, seedOffset, streamSize, DoubleConsts.MAX_VALUE, 0.0d), false);
        }
        throw new IllegalArgumentException(BadSize);
    }

    public DoubleStream doubles() {
        return StreamSupport.doubleStream(new RandomDoublesSpliterator(this, seedOffset, Long.MAX_VALUE, DoubleConsts.MAX_VALUE, 0.0d), false);
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
