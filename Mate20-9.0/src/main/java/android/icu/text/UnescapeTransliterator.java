package android.icu.text;

import android.icu.impl.PatternTokenizer;
import android.icu.impl.Utility;
import android.icu.text.Transliterator;
import android.icu.util.ULocale;

class UnescapeTransliterator extends Transliterator {
    private static final char END = '￿';
    private char[] spec;

    static void register() {
        Transliterator.registerFactory("Hex-Any/Unicode", new Transliterator.Factory() {
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/Unicode", new char[]{2, 0, 16, 4, 6, 'U', '+', 65535});
            }
        });
        Transliterator.registerFactory("Hex-Any/Java", new Transliterator.Factory() {
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/Java", new char[]{2, 0, 16, 4, 4, PatternTokenizer.BACK_SLASH, 'u', 65535});
            }
        });
        Transliterator.registerFactory("Hex-Any/C", new Transliterator.Factory() {
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/C", new char[]{2, 0, 16, 4, 4, PatternTokenizer.BACK_SLASH, 'u', 2, 0, 16, 8, 8, PatternTokenizer.BACK_SLASH, 'U', 65535});
            }
        });
        Transliterator.registerFactory("Hex-Any/XML", new Transliterator.Factory() {
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/XML", new char[]{3, 1, 16, 1, 6, '&', '#', ULocale.PRIVATE_USE_EXTENSION, ';', 65535});
            }
        });
        Transliterator.registerFactory("Hex-Any/XML10", new Transliterator.Factory() {
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/XML10", new char[]{2, 1, 10, 1, 7, '&', '#', ';', 65535});
            }
        });
        Transliterator.registerFactory("Hex-Any/Perl", new Transliterator.Factory() {
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/Perl", new char[]{3, 1, 16, 1, 6, PatternTokenizer.BACK_SLASH, ULocale.PRIVATE_USE_EXTENSION, '{', '}', 65535});
            }
        });
        Transliterator.registerFactory("Hex-Any", new Transliterator.Factory() {
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any", new char[]{2, 0, 16, 4, 6, 'U', '+', 2, 0, 16, 4, 4, PatternTokenizer.BACK_SLASH, 'u', 2, 0, 16, 8, 8, PatternTokenizer.BACK_SLASH, 'U', 3, 1, 16, 1, 6, '&', '#', ULocale.PRIVATE_USE_EXTENSION, ';', 2, 1, 10, 1, 7, '&', '#', ';', 3, 1, 16, 1, 6, PatternTokenizer.BACK_SLASH, ULocale.PRIVATE_USE_EXTENSION, '{', '}', 65535});
            }
        });
    }

    UnescapeTransliterator(String ID, char[] spec2) {
        super(ID, null);
        this.spec = spec2;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x005c, code lost:
        if (r14 == false) goto L_0x00e2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x005e, code lost:
        r16 = 0;
        r6 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0062, code lost:
        if (r15 < r5) goto L_0x006d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0064, code lost:
        if (r15 <= r4) goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0066, code lost:
        if (r26 == false) goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x006a, code lost:
        r19 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x006d, code lost:
        r19 = r12;
        r12 = r1.char32At(r15);
        r18 = android.icu.lang.UCharacter.digit(r12, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0077, code lost:
        if (r18 >= 0) goto L_0x007d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x007a, code lost:
        r12 = r16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x007d, code lost:
        r15 = r15 + android.icu.text.UTF16.getCharCount(r12);
        r16 = (r16 * r9) + r18;
        r6 = r6 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0089, code lost:
        if (r6 != r11) goto L_0x00de;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x008b, code lost:
        r12 = r16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x008e, code lost:
        if (r6 < r10) goto L_0x0093;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0090, code lost:
        r16 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0093, code lost:
        r16 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0095, code lost:
        r14 = r16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0097, code lost:
        if (r14 == false) goto L_0x00e4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0099, code lost:
        r21 = r6;
        r6 = r15;
        r15 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a0, code lost:
        if (r15 >= r8) goto L_0x00c6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00a2, code lost:
        if (r6 < r5) goto L_0x00ab;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00a4, code lost:
        if (r6 <= r4) goto L_0x00a9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00a6, code lost:
        if (r26 == false) goto L_0x00a9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00a9, code lost:
        r14 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00ab, code lost:
        r16 = r6 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00b9, code lost:
        if (r1.charAt(r6) == r0.spec[(r13 + r7) + r15]) goto L_0x00c1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00bb, code lost:
        r14 = false;
        r6 = r16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00c1, code lost:
        r15 = r15 + 1;
        r6 = r16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00c6, code lost:
        if (r14 == false) goto L_0x00da;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00c8, code lost:
        r3 = android.icu.text.UTF16.valueOf(r12);
        r1.replace(r4, r6, r3);
        r5 = r5 - ((r6 - r4) - r3.length());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00da, code lost:
        r19 = r15;
        r15 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00de, code lost:
        r12 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00e2, code lost:
        r19 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00eb, code lost:
        if (r4 >= r5) goto L_0x000a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00ed, code lost:
        r4 = r4 + android.icu.text.UTF16.getCharCount(r1.char32At(r4));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00e4, code lost:
        continue;
     */
    public void handleTransliterate(Replaceable text, Transliterator.Position pos, boolean isIncremental) {
        char prefixLen;
        char suffixLen;
        int ipat;
        Replaceable replaceable = text;
        Transliterator.Position position = pos;
        int start = position.start;
        int limit = position.limit;
        loop0:
        while (start < limit) {
            int prefixLen2 = 0;
            while (true) {
                if (this.spec[prefixLen2] == 65535) {
                    int i = prefixLen2;
                    break;
                }
                int ipat2 = prefixLen2 + 1;
                prefixLen = this.spec[prefixLen2];
                int ipat3 = ipat2 + 1;
                suffixLen = this.spec[ipat2];
                int ipat4 = ipat3 + 1;
                char radix = this.spec[ipat3];
                int ipat5 = ipat4 + 1;
                char minDigits = this.spec[ipat4];
                ipat = ipat5 + 1;
                char maxDigits = this.spec[ipat5];
                boolean match = true;
                int s = start;
                int i2 = 0;
                while (true) {
                    if (i2 >= prefixLen) {
                        break;
                    } else if (s < limit || i2 <= 0) {
                        int s2 = s + 1;
                        if (replaceable.charAt(s) != this.spec[ipat + i2]) {
                            match = false;
                            s = s2;
                            break;
                        }
                        i2++;
                        s = s2;
                    } else if (isIncremental) {
                        break loop0;
                    } else {
                        match = false;
                    }
                }
                prefixLen2 = ipat + prefixLen + suffixLen;
            }
        }
        position.contextLimit += limit - position.limit;
        position.limit = limit;
        position.start = start;
    }

    public void addSourceTargetSet(UnicodeSet inputFilter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        UnicodeSet myFilter = getFilterAsUnicodeSet(inputFilter);
        UnicodeSet items = new UnicodeSet();
        StringBuilder buffer = new StringBuilder();
        int i = 0;
        while (this.spec[i] != 65535) {
            int end = this.spec[i] + i + this.spec[i + 1] + 5;
            char radix = this.spec[i + 2];
            for (int j = 0; j < radix; j++) {
                Utility.appendNumber(buffer, j, radix, 0);
            }
            for (int j2 = i + 5; j2 < end; j2++) {
                items.add((int) this.spec[j2]);
            }
            i = end;
        }
        items.addAll((CharSequence) buffer.toString());
        items.retainAll(myFilter);
        if (items.size() > 0) {
            sourceSet.addAll(items);
            targetSet.addAll(0, 1114111);
        }
    }
}
