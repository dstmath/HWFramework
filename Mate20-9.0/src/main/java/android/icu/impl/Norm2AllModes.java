package android.icu.impl;

import android.icu.impl.Normalizer2Impl;
import android.icu.text.Normalizer;
import android.icu.text.Normalizer2;
import android.icu.util.ICUUncheckedIOException;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class Norm2AllModes {
    public static final NoopNormalizer2 NOOP_NORMALIZER2 = new NoopNormalizer2();
    private static CacheBase<String, Norm2AllModes, ByteBuffer> cache = new SoftCache<String, Norm2AllModes, ByteBuffer>() {
        /* access modifiers changed from: protected */
        public Norm2AllModes createInstance(String key, ByteBuffer bytes) {
            Normalizer2Impl impl;
            if (bytes == null) {
                Normalizer2Impl normalizer2Impl = new Normalizer2Impl();
                impl = normalizer2Impl.load(key + ".nrm");
            } else {
                impl = new Normalizer2Impl().load(bytes);
            }
            return new Norm2AllModes(impl);
        }
    };
    public final ComposeNormalizer2 comp;
    public final DecomposeNormalizer2 decomp;
    public final ComposeNormalizer2 fcc;
    public final FCDNormalizer2 fcd;
    public final Normalizer2Impl impl;

    public static final class ComposeNormalizer2 extends Normalizer2WithImpl {
        private final boolean onlyContiguous;

        public ComposeNormalizer2(Normalizer2Impl ni, boolean fcc) {
            super(ni);
            this.onlyContiguous = fcc;
        }

        /* access modifiers changed from: protected */
        public void normalize(CharSequence src, Normalizer2Impl.ReorderingBuffer buffer) {
            this.impl.compose(src, 0, src.length(), this.onlyContiguous, true, buffer);
        }

        /* access modifiers changed from: protected */
        public void normalizeAndAppend(CharSequence src, boolean doNormalize, Normalizer2Impl.ReorderingBuffer buffer) {
            this.impl.composeAndAppend(src, doNormalize, this.onlyContiguous, buffer);
        }

        public boolean isNormalized(CharSequence s) {
            return this.impl.compose(s, 0, s.length(), this.onlyContiguous, false, new Normalizer2Impl.ReorderingBuffer(this.impl, new StringBuilder(), 5));
        }

        public Normalizer.QuickCheckResult quickCheck(CharSequence s) {
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
            return this.impl.hasCompBoundaryAfter(c, this.onlyContiguous);
        }

        public boolean isInert(int c) {
            return this.impl.isCompInert(c, this.onlyContiguous);
        }
    }

    public static final class DecomposeNormalizer2 extends Normalizer2WithImpl {
        public DecomposeNormalizer2(Normalizer2Impl ni) {
            super(ni);
        }

        /* access modifiers changed from: protected */
        public void normalize(CharSequence src, Normalizer2Impl.ReorderingBuffer buffer) {
            this.impl.decompose(src, 0, src.length(), buffer);
        }

        /* access modifiers changed from: protected */
        public void normalizeAndAppend(CharSequence src, boolean doNormalize, Normalizer2Impl.ReorderingBuffer buffer) {
            this.impl.decomposeAndAppend(src, doNormalize, buffer);
        }

        public int spanQuickCheckYes(CharSequence s) {
            return this.impl.decompose(s, 0, s.length(), null);
        }

        public int getQuickCheck(int c) {
            return this.impl.isDecompYes(this.impl.getNorm16(c)) ? 1 : 0;
        }

        public boolean hasBoundaryBefore(int c) {
            return this.impl.hasDecompBoundaryBefore(c);
        }

        public boolean hasBoundaryAfter(int c) {
            return this.impl.hasDecompBoundaryAfter(c);
        }

        public boolean isInert(int c) {
            return this.impl.isDecompInert(c);
        }
    }

    public static final class FCDNormalizer2 extends Normalizer2WithImpl {
        public FCDNormalizer2(Normalizer2Impl ni) {
            super(ni);
        }

        /* access modifiers changed from: protected */
        public void normalize(CharSequence src, Normalizer2Impl.ReorderingBuffer buffer) {
            this.impl.makeFCD(src, 0, src.length(), buffer);
        }

        /* access modifiers changed from: protected */
        public void normalizeAndAppend(CharSequence src, boolean doNormalize, Normalizer2Impl.ReorderingBuffer buffer) {
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
        /* access modifiers changed from: private */
        public static final Norm2AllModesSingleton INSTANCE = new Norm2AllModesSingleton("nfc");

        private NFCSingleton() {
        }
    }

    private static final class NFKCSingleton {
        /* access modifiers changed from: private */
        public static final Norm2AllModesSingleton INSTANCE = new Norm2AllModesSingleton("nfkc");

        private NFKCSingleton() {
        }
    }

    private static final class NFKC_CFSingleton {
        /* access modifiers changed from: private */
        public static final Norm2AllModesSingleton INSTANCE = new Norm2AllModesSingleton("nfkc_cf");

        private NFKC_CFSingleton() {
        }
    }

    public static final class NoopNormalizer2 extends Normalizer2 {
        public StringBuilder normalize(CharSequence src, StringBuilder dest) {
            if (dest != src) {
                dest.setLength(0);
                dest.append(src);
                return dest;
            }
            throw new IllegalArgumentException();
        }

        public Appendable normalize(CharSequence src, Appendable dest) {
            if (dest != src) {
                try {
                    return dest.append(src);
                } catch (IOException e) {
                    throw new ICUUncheckedIOException((Throwable) e);
                }
            } else {
                throw new IllegalArgumentException();
            }
        }

        public StringBuilder normalizeSecondAndAppend(StringBuilder first, CharSequence second) {
            if (first != second) {
                first.append(second);
                return first;
            }
            throw new IllegalArgumentException();
        }

        public StringBuilder append(StringBuilder first, CharSequence second) {
            if (first != second) {
                first.append(second);
                return first;
            }
            throw new IllegalArgumentException();
        }

        public String getDecomposition(int c) {
            return null;
        }

        public boolean isNormalized(CharSequence s) {
            return true;
        }

        public Normalizer.QuickCheckResult quickCheck(CharSequence s) {
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
        /* access modifiers changed from: private */
        public Norm2AllModes allModes;
        /* access modifiers changed from: private */
        public RuntimeException exception;

        private Norm2AllModesSingleton(String name) {
            try {
                Normalizer2Impl normalizer2Impl = new Normalizer2Impl();
                this.allModes = new Norm2AllModes(normalizer2Impl.load(name + ".nrm"));
            } catch (RuntimeException e) {
                this.exception = e;
            }
        }
    }

    public static abstract class Normalizer2WithImpl extends Normalizer2 {
        public final Normalizer2Impl impl;

        public abstract int getQuickCheck(int i);

        /* access modifiers changed from: protected */
        public abstract void normalize(CharSequence charSequence, Normalizer2Impl.ReorderingBuffer reorderingBuffer);

        /* access modifiers changed from: protected */
        public abstract void normalizeAndAppend(CharSequence charSequence, boolean z, Normalizer2Impl.ReorderingBuffer reorderingBuffer);

        public Normalizer2WithImpl(Normalizer2Impl ni) {
            this.impl = ni;
        }

        public StringBuilder normalize(CharSequence src, StringBuilder dest) {
            if (dest != src) {
                dest.setLength(0);
                normalize(src, new Normalizer2Impl.ReorderingBuffer(this.impl, dest, src.length()));
                return dest;
            }
            throw new IllegalArgumentException();
        }

        public Appendable normalize(CharSequence src, Appendable dest) {
            if (dest != src) {
                Normalizer2Impl.ReorderingBuffer buffer = new Normalizer2Impl.ReorderingBuffer(this.impl, dest, src.length());
                normalize(src, buffer);
                buffer.flush();
                return dest;
            }
            throw new IllegalArgumentException();
        }

        public StringBuilder normalizeSecondAndAppend(StringBuilder first, CharSequence second) {
            return normalizeSecondAndAppend(first, second, true);
        }

        public StringBuilder append(StringBuilder first, CharSequence second) {
            return normalizeSecondAndAppend(first, second, false);
        }

        public StringBuilder normalizeSecondAndAppend(StringBuilder first, CharSequence second, boolean doNormalize) {
            if (first != second) {
                normalizeAndAppend(second, doNormalize, new Normalizer2Impl.ReorderingBuffer(this.impl, first, first.length() + second.length()));
                return first;
            }
            throw new IllegalArgumentException();
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

        public Normalizer.QuickCheckResult quickCheck(CharSequence s) {
            return isNormalized(s) ? Normalizer.YES : Normalizer.NO;
        }
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
        Norm2AllModesSingleton singleton;
        if (bytes == null) {
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
        return cache.getInstance(name, bytes);
    }

    public static Normalizer2 getFCDNormalizer2() {
        return getNFCInstance().fcd;
    }
}
