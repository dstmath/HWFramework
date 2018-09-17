package android.icu.text;

import android.icu.impl.Norm2AllModes;
import android.icu.impl.Normalizer2Impl.UTF16Plus;
import android.icu.text.Transliterator.Factory;
import android.icu.text.Transliterator.Position;
import java.util.HashMap;
import java.util.Map;

final class NormalizationTransliterator extends Transliterator {
    static final Map<Normalizer2, SourceTargetUtility> SOURCE_CACHE = new HashMap();
    private final Normalizer2 norm2;

    static class NormalizingTransform implements Transform<String, String> {
        final Normalizer2 norm2;

        public NormalizingTransform(Normalizer2 norm2) {
            this.norm2 = norm2;
        }

        public String transform(String source) {
            return this.norm2.normalize(source);
        }
    }

    /* synthetic */ NormalizationTransliterator(String id, Normalizer2 n2, NormalizationTransliterator -this2) {
        this(id, n2);
    }

    static void register() {
        Transliterator.registerFactory("Any-NFC", new Factory() {
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator("NFC", Normalizer2.getNFCInstance(), null);
            }
        });
        Transliterator.registerFactory("Any-NFD", new Factory() {
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator("NFD", Normalizer2.getNFDInstance(), null);
            }
        });
        Transliterator.registerFactory("Any-NFKC", new Factory() {
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator("NFKC", Normalizer2.getNFKCInstance(), null);
            }
        });
        Transliterator.registerFactory("Any-NFKD", new Factory() {
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator("NFKD", Normalizer2.getNFKDInstance(), null);
            }
        });
        Transliterator.registerFactory("Any-FCD", new Factory() {
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator("FCD", Norm2AllModes.getFCDNormalizer2(), null);
            }
        });
        Transliterator.registerFactory("Any-FCC", new Factory() {
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator("FCC", Norm2AllModes.getNFCInstance().fcc, null);
            }
        });
        Transliterator.registerSpecialInverse("NFC", "NFD", true);
        Transliterator.registerSpecialInverse("NFKC", "NFKD", true);
        Transliterator.registerSpecialInverse("FCC", "NFD", false);
        Transliterator.registerSpecialInverse("FCD", "FCD", false);
    }

    private NormalizationTransliterator(String id, Normalizer2 n2) {
        super(id, null);
        this.norm2 = n2;
    }

    protected void handleTransliterate(Replaceable text, Position offsets, boolean isIncremental) {
        int start = offsets.start;
        int limit = offsets.limit;
        if (start < limit) {
            CharSequence segment = new StringBuilder();
            StringBuilder normalized = new StringBuilder();
            int c = text.char32At(start);
            while (true) {
                int prev = start;
                segment.setLength(0);
                Normalizer2 normalizer2;
                do {
                    segment.appendCodePoint(c);
                    start += Character.charCount(c);
                    if (start >= limit) {
                        break;
                    }
                    normalizer2 = this.norm2;
                    c = text.char32At(start);
                } while ((normalizer2.hasBoundaryBefore(c) ^ 1) != 0);
                if (start != limit || !isIncremental || (this.norm2.hasBoundaryAfter(c) ^ 1) == 0) {
                    this.norm2.normalize(segment, normalized);
                    if (!UTF16Plus.equal(segment, normalized)) {
                        text.replace(prev, start, normalized.toString());
                        int delta = normalized.length() - (start - prev);
                        start += delta;
                        limit += delta;
                    }
                    if (start >= limit) {
                        break;
                    }
                } else {
                    start = prev;
                    break;
                }
            }
            offsets.start = start;
            offsets.contextLimit += limit - offsets.limit;
            offsets.limit = limit;
        }
    }

    public void addSourceTargetSet(UnicodeSet inputFilter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        SourceTargetUtility cache;
        synchronized (SOURCE_CACHE) {
            cache = (SourceTargetUtility) SOURCE_CACHE.get(this.norm2);
            if (cache == null) {
                cache = new SourceTargetUtility(new NormalizingTransform(this.norm2), this.norm2);
                SOURCE_CACHE.put(this.norm2, cache);
            }
        }
        cache.addSourceTargetSet(this, inputFilter, sourceSet, targetSet);
    }
}
