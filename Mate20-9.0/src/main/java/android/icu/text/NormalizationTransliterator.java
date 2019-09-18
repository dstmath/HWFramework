package android.icu.text;

import android.icu.impl.Norm2AllModes;
import android.icu.impl.Normalizer2Impl;
import android.icu.text.Transliterator;
import java.util.HashMap;
import java.util.Map;

final class NormalizationTransliterator extends Transliterator {
    static final Map<Normalizer2, SourceTargetUtility> SOURCE_CACHE = new HashMap();
    private final Normalizer2 norm2;

    static class NormalizingTransform implements Transform<String, String> {
        final Normalizer2 norm2;

        public NormalizingTransform(Normalizer2 norm22) {
            this.norm2 = norm22;
        }

        public String transform(String source) {
            return this.norm2.normalize(source);
        }
    }

    static void register() {
        Transliterator.registerFactory("Any-NFC", new Transliterator.Factory() {
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator("NFC", Normalizer2.getNFCInstance());
            }
        });
        Transliterator.registerFactory("Any-NFD", new Transliterator.Factory() {
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator("NFD", Normalizer2.getNFDInstance());
            }
        });
        Transliterator.registerFactory("Any-NFKC", new Transliterator.Factory() {
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator("NFKC", Normalizer2.getNFKCInstance());
            }
        });
        Transliterator.registerFactory("Any-NFKD", new Transliterator.Factory() {
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator("NFKD", Normalizer2.getNFKDInstance());
            }
        });
        Transliterator.registerFactory("Any-FCD", new Transliterator.Factory() {
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator("FCD", Norm2AllModes.getFCDNormalizer2());
            }
        });
        Transliterator.registerFactory("Any-FCC", new Transliterator.Factory() {
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator("FCC", Norm2AllModes.getNFCInstance().fcc);
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

    /* access modifiers changed from: protected */
    public void handleTransliterate(Replaceable text, Transliterator.Position offsets, boolean isIncremental) {
        Normalizer2 normalizer2;
        int char32At;
        int start = offsets.start;
        int limit = offsets.limit;
        if (start < limit) {
            StringBuilder segment = new StringBuilder();
            StringBuilder normalized = new StringBuilder();
            int c = text.char32At(start);
            while (true) {
                int prev = start;
                segment.setLength(0);
                do {
                    segment.appendCodePoint(c);
                    start += Character.charCount(c);
                    if (start >= limit) {
                        break;
                    }
                    normalizer2 = this.norm2;
                    char32At = text.char32At(start);
                    c = char32At;
                } while (!normalizer2.hasBoundaryBefore(char32At));
                if (start == limit && isIncremental && !this.norm2.hasBoundaryAfter(c)) {
                    start = prev;
                    break;
                }
                this.norm2.normalize((CharSequence) segment, normalized);
                if (!Normalizer2Impl.UTF16Plus.equal(segment, normalized)) {
                    text.replace(prev, start, normalized.toString());
                    int delta = normalized.length() - (start - prev);
                    start += delta;
                    limit += delta;
                    continue;
                }
                if (start >= limit) {
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
            cache = SOURCE_CACHE.get(this.norm2);
            if (cache == null) {
                cache = new SourceTargetUtility(new NormalizingTransform(this.norm2), this.norm2);
                SOURCE_CACHE.put(this.norm2, cache);
            }
        }
        cache.addSourceTargetSet(this, inputFilter, sourceSet, targetSet);
    }
}
