package android.icu.text;

import android.icu.impl.Norm2AllModes;
import android.icu.impl.Normalizer2Impl.UTF16Plus;
import android.icu.text.Transliterator.Factory;
import android.icu.text.Transliterator.Position;
import java.util.Map;

final class NormalizationTransliterator extends Transliterator {
    static final Map<Normalizer2, SourceTargetUtility> SOURCE_CACHE = null;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.NormalizationTransliterator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.NormalizationTransliterator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.NormalizationTransliterator.<clinit>():void");
    }

    static void register() {
        Transliterator.registerFactory("Any-NFC", new Factory() {
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator(Normalizer2.getNFCInstance(), null);
            }
        });
        Transliterator.registerFactory("Any-NFD", new Factory() {
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator(Normalizer2.getNFDInstance(), null);
            }
        });
        Transliterator.registerFactory("Any-NFKC", new Factory() {
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator(Normalizer2.getNFKCInstance(), null);
            }
        });
        Transliterator.registerFactory("Any-NFKD", new Factory() {
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator(Normalizer2.getNFKDInstance(), null);
            }
        });
        Transliterator.registerFactory("Any-FCD", new Factory() {
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator(Norm2AllModes.getFCDNormalizer2(), null);
            }
        });
        Transliterator.registerFactory("Any-FCC", new Factory() {
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator(Norm2AllModes.getNFCInstance().fcc, null);
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
            do {
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
                } while (!normalizer2.hasBoundaryBefore(c));
                if (start == limit && isIncremental && !this.norm2.hasBoundaryAfter(c)) {
                    start = prev;
                    break;
                }
                this.norm2.normalize(segment, normalized);
                if (!UTF16Plus.equal(segment, normalized)) {
                    text.replace(prev, start, normalized.toString());
                    int delta = normalized.length() - (start - prev);
                    start += delta;
                    limit += delta;
                    continue;
                }
            } while (start < limit);
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
