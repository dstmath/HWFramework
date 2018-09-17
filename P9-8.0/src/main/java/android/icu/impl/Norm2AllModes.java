package android.icu.impl;

import android.icu.impl.Normalizer2Impl.ReorderingBuffer;
import android.icu.text.Normalizer;
import android.icu.text.Normalizer.QuickCheckResult;
import android.icu.text.Normalizer2;
import android.icu.util.ICUUncheckedIOException;
import java.nio.ByteBuffer;

public final class Norm2AllModes {
    public static final NoopNormalizer2 NOOP_NORMALIZER2 = new NoopNormalizer2();
    private static CacheBase<String, Norm2AllModes, ByteBuffer> cache = new SoftCache<String, Norm2AllModes, ByteBuffer>() {
        protected Norm2AllModes createInstance(String key, ByteBuffer bytes) {
            Normalizer2Impl impl;
            if (bytes == null) {
                impl = new Normalizer2Impl().load(key + ".nrm");
            } else {
                impl = new Normalizer2Impl().load(bytes);
            }
            return new Norm2AllModes(impl, null);
        }
    };
    public final ComposeNormalizer2 comp;
    public final DecomposeNormalizer2 decomp;
    public final ComposeNormalizer2 fcc;
    public final FCDNormalizer2 fcd;
    public final Normalizer2Impl impl;

    public static abstract class Normalizer2WithImpl extends Normalizer2 {
        public final Normalizer2Impl impl;

        public abstract int getQuickCheck(int i);

        protected abstract void normalize(CharSequence charSequence, ReorderingBuffer reorderingBuffer);

        protected abstract void normalizeAndAppend(CharSequence charSequence, boolean z, ReorderingBuffer reorderingBuffer);

        public Normalizer2WithImpl(Normalizer2Impl ni) {
            this.impl = ni;
        }

        public StringBuilder normalize(CharSequence src, StringBuilder dest) {
            if (dest == src) {
                throw new IllegalArgumentException();
            }
            dest.setLength(0);
            normalize(src, new ReorderingBuffer(this.impl, dest, src.length()));
            return dest;
        }

        public Appendable normalize(CharSequence src, Appendable dest) {
            if (dest == src) {
                throw new IllegalArgumentException();
            }
            ReorderingBuffer buffer = new ReorderingBuffer(this.impl, dest, src.length());
            normalize(src, buffer);
            buffer.flush();
            return dest;
        }

        public StringBuilder normalizeSecondAndAppend(StringBuilder first, CharSequence second) {
            return normalizeSecondAndAppend(first, second, true);
        }

        public StringBuilder append(StringBuilder first, CharSequence second) {
            return normalizeSecondAndAppend(first, second, false);
        }

        public StringBuilder normalizeSecondAndAppend(StringBuilder first, CharSequence second, boolean doNormalize) {
            if (first == second) {
                throw new IllegalArgumentException();
            }
            normalizeAndAppend(second, doNormalize, new ReorderingBuffer(this.impl, first, first.length() + second.length()));
            return first;
        }

        public String getDecomposition(int c) {
            return this.impl.getDecomposition(c);
        }

        public String getRawDecomposition(int c) {
            return this.impl.getRawDecomposition(c);
        }

        public int composePair(int a, int b) {
            return this.impl.composePair(a, b);
        }

        public int getCombiningClass(int c) {
            return this.impl.getCC(this.impl.getNorm16(c));
        }

        public boolean isNormalized(CharSequence s) {
            return s.length() == spanQuickCheckYes(s);
        }

        public QuickCheckResult quickCheck(CharSequence s) {
            return isNormalized(s) ? Normalizer.YES : Normalizer.NO;
        }
    }

    public static final class ComposeNormalizer2 extends Normalizer2WithImpl {
        private final boolean onlyContiguous;

        public ComposeNormalizer2(Normalizer2Impl ni, boolean fcc) {
            super(ni);
            this.onlyContiguous = fcc;
        }

        protected void normalize(CharSequence src, ReorderingBuffer buffer) {
            this.impl.compose(src, 0, src.length(), this.onlyContiguous, true, buffer);
        }

        protected void normalizeAndAppend(CharSequence src, boolean doNormalize, ReorderingBuffer buffer) {
            this.impl.composeAndAppend(src, doNormalize, this.onlyContiguous, buffer);
        }

        public boolean isNormalized(CharSequence s) {
            return this.impl.compose(s, 0, s.length(), this.onlyContiguous, false, new ReorderingBuffer(this.impl, new StringBuilder(), 5));
        }

        public QuickCheckResult quickCheck(CharSequence s) {
            int spanLengthAndMaybe = this.impl.composeQuickCheck(s, 0, s.length(), this.onlyContiguous, false);
            if ((spanLengthAndMaybe & 1) != 0) {
                return Normalizer.MAYBE;
            }
            if ((spanLengthAndMaybe >>> 1) == s.length()) {
                return Normalizer.YES;
            }
            return Normalizer.NO;
        }

        public int spanQuickCheckYes(CharSequence s) {
            return this.impl.composeQuickCheck(s, 0, s.length(), this.onlyContiguous, true) >>> 1;
        }

        public int getQuickCheck(int c) {
            return this.impl.getCompQuickCheck(this.impl.getNorm16(c));
        }

        public boolean hasBoundaryBefore(int c) {
            return this.impl.hasCompBoundaryBefore(c);
        }

        public boolean hasBoundaryAfter(int c) {
            return this.impl.hasCompBoundaryAfter(c, this.onlyContiguous, false);
        }

        public boolean isInert(int c) {
            return this.impl.hasCompBoundaryAfter(c, this.onlyContiguous, true);
        }
    }

    public static final class DecomposeNormalizer2 extends Normalizer2WithImpl {
        public DecomposeNormalizer2(Normalizer2Impl ni) {
            super(ni);
        }

        protected void normalize(CharSequence src, ReorderingBuffer buffer) {
            this.impl.decompose(src, 0, src.length(), buffer);
        }

        protected void normalizeAndAppend(CharSequence src, boolean doNormalize, ReorderingBuffer buffer) {
            this.impl.decomposeAndAppend(src, doNormalize, buffer);
        }

        public int spanQuickCheckYes(CharSequence s) {
            return this.impl.decompose(s, 0, s.length(), null);
        }

        public int getQuickCheck(int c) {
            return this.impl.isDecompYes(this.impl.getNorm16(c)) ? 1 : 0;
        }

        public boolean hasBoundaryBefore(int c) {
            return this.impl.hasDecompBoundary(c, true);
        }

        public boolean hasBoundaryAfter(int c) {
            return this.impl.hasDecompBoundary(c, false);
        }

        public boolean isInert(int c) {
            return this.impl.isDecompInert(c);
        }
    }

    public static final class FCDNormalizer2 extends Normalizer2WithImpl {
        public FCDNormalizer2(Normalizer2Impl ni) {
            super(ni);
        }

        protected void normalize(CharSequence src, ReorderingBuffer buffer) {
            this.impl.makeFCD(src, 0, src.length(), buffer);
        }

        protected void normalizeAndAppend(CharSequence src, boolean doNormalize, ReorderingBuffer buffer) {
            this.impl.makeFCDAndAppend(src, doNormalize, buffer);
        }

        public int spanQuickCheckYes(CharSequence s) {
            return this.impl.makeFCD(s, 0, s.length(), null);
        }

        public int getQuickCheck(int c) {
            return this.impl.isDecompYes(this.impl.getNorm16(c)) ? 1 : 0;
        }

        public boolean hasBoundaryBefore(int c) {
            return this.impl.hasFCDBoundaryBefore(c);
        }

        public boolean hasBoundaryAfter(int c) {
            return this.impl.hasFCDBoundaryAfter(c);
        }

        public boolean isInert(int c) {
            return this.impl.isFCDInert(c);
        }
    }

    private static final class NFCSingleton {
        private static final Norm2AllModesSingleton INSTANCE = new Norm2AllModesSingleton("nfc", null);

        private NFCSingleton() {
        }
    }

    private static final class NFKCSingleton {
        private static final Norm2AllModesSingleton INSTANCE = new Norm2AllModesSingleton("nfkc", null);

        private NFKCSingleton() {
        }
    }

    private static final class NFKC_CFSingleton {
        private static final Norm2AllModesSingleton INSTANCE = new Norm2AllModesSingleton("nfkc_cf", null);

        private NFKC_CFSingleton() {
        }
    }

    public static final class NoopNormalizer2 extends Normalizer2 {
        public StringBuilder normalize(CharSequence src, StringBuilder dest) {
            if (dest != src) {
                dest.setLength(0);
                return dest.append(src);
            }
            throw new IllegalArgumentException();
        }

        public Appendable normalize(CharSequence src, Appendable dest) {
            if (dest != src) {
                try {
                    return dest.append(src);
                } catch (Throwable e) {
                    throw new ICUUncheckedIOException(e);
                }
            }
            throw new IllegalArgumentException();
        }

        public StringBuilder normalizeSecondAndAppend(StringBuilder first, CharSequence second) {
            if (first != second) {
                return first.append(second);
            }
            throw new IllegalArgumentException();
        }

        public StringBuilder append(StringBuilder first, CharSequence second) {
            if (first != second) {
                return first.append(second);
            }
            throw new IllegalArgumentException();
        }

        public String getDecomposition(int c) {
            return null;
        }

        public boolean isNormalized(CharSequence s) {
            return true;
        }

        public QuickCheckResult quickCheck(CharSequence s) {
            return Normalizer.YES;
        }

        public int spanQuickCheckYes(CharSequence s) {
            return s.length();
        }

        public boolean hasBoundaryBefore(int c) {
            return true;
        }

        public boolean hasBoundaryAfter(int c) {
            return true;
        }

        public boolean isInert(int c) {
            return true;
        }
    }

    private static final class Norm2AllModesSingleton {
        private Norm2AllModes allModes;
        private RuntimeException exception;

        /* synthetic */ Norm2AllModesSingleton(String name, Norm2AllModesSingleton -this1) {
            this(name);
        }

        private Norm2AllModesSingleton(String name) {
            try {
                this.allModes = new Norm2AllModes(new Normalizer2Impl().load(name + ".nrm"), null);
            } catch (RuntimeException e) {
                this.exception = e;
            }
        }
    }

    /* synthetic */ Norm2AllModes(Normalizer2Impl ni, Norm2AllModes -this1) {
        this(ni);
    }

    private Norm2AllModes(Normalizer2Impl ni) {
        this.impl = ni;
        this.comp = new ComposeNormalizer2(ni, false);
        this.decomp = new DecomposeNormalizer2(ni);
        this.fcd = new FCDNormalizer2(ni);
        this.fcc = new ComposeNormalizer2(ni, true);
    }

    private static Norm2AllModes getInstanceFromSingleton(Norm2AllModesSingleton singleton) {
        if (singleton.exception == null) {
            return singleton.allModes;
        }
        throw singleton.exception;
    }

    public static Norm2AllModes getNFCInstance() {
        return getInstanceFromSingleton(NFCSingleton.INSTANCE);
    }

    public static Norm2AllModes getNFKCInstance() {
        return getInstanceFromSingleton(NFKCSingleton.INSTANCE);
    }

    public static Norm2AllModes getNFKC_CFInstance() {
        return getInstanceFromSingleton(NFKC_CFSingleton.INSTANCE);
    }

    public static Normalizer2WithImpl getN2WithImpl(int index) {
        switch (index) {
            case 0:
                return getNFCInstance().decomp;
            case 1:
                return getNFKCInstance().decomp;
            case 2:
                return getNFCInstance().comp;
            case 3:
                return getNFKCInstance().comp;
            default:
                return null;
        }
    }

    public static Norm2AllModes getInstance(ByteBuffer bytes, String name) {
        if (bytes == null) {
            Norm2AllModesSingleton singleton;
            if (name.equals("nfc")) {
                singleton = NFCSingleton.INSTANCE;
            } else if (name.equals("nfkc")) {
                singleton = NFKCSingleton.INSTANCE;
            } else if (name.equals("nfkc_cf")) {
                singleton = NFKC_CFSingleton.INSTANCE;
            } else {
                singleton = null;
            }
            if (singleton != null) {
                if (singleton.exception == null) {
                    return singleton.allModes;
                }
                throw singleton.exception;
            }
        }
        return (Norm2AllModes) cache.getInstance(name, bytes);
    }

    public static Normalizer2 getFCDNormalizer2() {
        return getNFCInstance().fcd;
    }
}
