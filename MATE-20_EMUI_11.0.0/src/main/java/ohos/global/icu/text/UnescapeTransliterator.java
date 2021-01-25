package ohos.global.icu.text;

import ohos.global.icu.impl.PatternTokenizer;
import ohos.global.icu.impl.Utility;
import ohos.global.icu.impl.locale.UnicodeLocaleExtension;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.Transliterator;

class UnescapeTransliterator extends Transliterator {
    private static final char END = 65535;
    private char[] spec;

    static void register() {
        Transliterator.registerFactory("Hex-Any/Unicode", new Transliterator.Factory() {
            /* class ohos.global.icu.text.UnescapeTransliterator.AnonymousClass1 */

            @Override // ohos.global.icu.text.Transliterator.Factory
            public Transliterator getInstance(String str) {
                return new UnescapeTransliterator("Hex-Any/Unicode", new char[]{2, 0, 16, 4, 6, 'U', '+', 65535});
            }
        });
        Transliterator.registerFactory("Hex-Any/Java", new Transliterator.Factory() {
            /* class ohos.global.icu.text.UnescapeTransliterator.AnonymousClass2 */

            @Override // ohos.global.icu.text.Transliterator.Factory
            public Transliterator getInstance(String str) {
                return new UnescapeTransliterator("Hex-Any/Java", new char[]{2, 0, 16, 4, 4, PatternTokenizer.BACK_SLASH, UnicodeLocaleExtension.SINGLETON, 65535});
            }
        });
        Transliterator.registerFactory("Hex-Any/C", new Transliterator.Factory() {
            /* class ohos.global.icu.text.UnescapeTransliterator.AnonymousClass3 */

            @Override // ohos.global.icu.text.Transliterator.Factory
            public Transliterator getInstance(String str) {
                return new UnescapeTransliterator("Hex-Any/C", new char[]{2, 0, 16, 4, 4, PatternTokenizer.BACK_SLASH, UnicodeLocaleExtension.SINGLETON, 2, 0, 16, '\b', '\b', PatternTokenizer.BACK_SLASH, 'U', 65535});
            }
        });
        Transliterator.registerFactory("Hex-Any/XML", new Transliterator.Factory() {
            /* class ohos.global.icu.text.UnescapeTransliterator.AnonymousClass4 */

            @Override // ohos.global.icu.text.Transliterator.Factory
            public Transliterator getInstance(String str) {
                return new UnescapeTransliterator("Hex-Any/XML", new char[]{3, 1, 16, 1, 6, '&', '#', 'x', ';', 65535});
            }
        });
        Transliterator.registerFactory("Hex-Any/XML10", new Transliterator.Factory() {
            /* class ohos.global.icu.text.UnescapeTransliterator.AnonymousClass5 */

            @Override // ohos.global.icu.text.Transliterator.Factory
            public Transliterator getInstance(String str) {
                return new UnescapeTransliterator("Hex-Any/XML10", new char[]{2, 1, '\n', 1, 7, '&', '#', ';', 65535});
            }
        });
        Transliterator.registerFactory("Hex-Any/Perl", new Transliterator.Factory() {
            /* class ohos.global.icu.text.UnescapeTransliterator.AnonymousClass6 */

            @Override // ohos.global.icu.text.Transliterator.Factory
            public Transliterator getInstance(String str) {
                return new UnescapeTransliterator("Hex-Any/Perl", new char[]{3, 1, 16, 1, 6, PatternTokenizer.BACK_SLASH, 'x', '{', '}', 65535});
            }
        });
        Transliterator.registerFactory("Hex-Any", new Transliterator.Factory() {
            /* class ohos.global.icu.text.UnescapeTransliterator.AnonymousClass7 */

            @Override // ohos.global.icu.text.Transliterator.Factory
            public Transliterator getInstance(String str) {
                return new UnescapeTransliterator("Hex-Any", new char[]{2, 0, 16, 4, 6, 'U', '+', 2, 0, 16, 4, 4, PatternTokenizer.BACK_SLASH, UnicodeLocaleExtension.SINGLETON, 2, 0, 16, '\b', '\b', PatternTokenizer.BACK_SLASH, 'U', 3, 1, 16, 1, 6, '&', '#', 'x', ';', 2, 1, '\n', 1, 7, '&', '#', ';', 3, 1, 16, 1, 6, PatternTokenizer.BACK_SLASH, 'x', '{', '}', 65535});
            }
        });
    }

    UnescapeTransliterator(String str, char[] cArr) {
        super(str, null);
        this.spec = cArr;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0046, code lost:
        r5 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00aa, code lost:
        if (r3 >= r4) goto L_0x000a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00ac, code lost:
        r3 = r3 + ohos.global.icu.text.UTF16.getCharCount(r18.char32At(r3));
     */
    @Override // ohos.global.icu.text.Transliterator
    public void handleTransliterate(Replaceable replaceable, Transliterator.Position position, boolean z) {
        boolean z2;
        boolean z3;
        int i = position.start;
        int i2 = position.limit;
        loop0:
        while (i < i2) {
            int i3 = 0;
            while (true) {
                char[] cArr = this.spec;
                if (cArr[i3] == 65535) {
                    break;
                }
                int i4 = i3 + 1;
                char c = cArr[i3];
                int i5 = i4 + 1;
                char c2 = cArr[i4];
                int i6 = i5 + 1;
                char c3 = cArr[i5];
                int i7 = i6 + 1;
                char c4 = cArr[i6];
                int i8 = i7 + 1;
                char c5 = cArr[i7];
                int i9 = i;
                int i10 = 0;
                while (true) {
                    z2 = true;
                    if (i10 >= c) {
                        z3 = true;
                        break;
                    } else if (i9 < i2 || i10 <= 0) {
                        int i11 = i9 + 1;
                        if (replaceable.charAt(i9) != this.spec[i8 + i10]) {
                            i9 = i11;
                            break;
                        } else {
                            i10++;
                            i9 = i11;
                        }
                    } else if (z) {
                        break loop0;
                    }
                }
                if (z3) {
                    int i12 = 0;
                    int i13 = 0;
                    while (true) {
                        if (i9 < i2) {
                            int char32At = replaceable.char32At(i9);
                            int digit = UCharacter.digit(char32At, c3);
                            if (digit >= 0) {
                                i9 += UTF16.getCharCount(char32At);
                                i12 = (i12 * c3) + digit;
                                i13++;
                                if (i13 == c5) {
                                    break;
                                }
                            } else {
                                break;
                            }
                        } else if (i9 > i && z) {
                            break loop0;
                        }
                    }
                    if (i13 < c4) {
                        z2 = false;
                    }
                    if (z2) {
                        int i14 = 0;
                        while (true) {
                            if (i14 >= c2) {
                                break;
                            } else if (i9 < i2) {
                                int i15 = i9 + 1;
                                if (replaceable.charAt(i9) != this.spec[i8 + c + i14]) {
                                    i9 = i15;
                                    break;
                                } else {
                                    i14++;
                                    i9 = i15;
                                }
                            } else if (i9 > i && z) {
                                break loop0;
                            }
                        }
                        z2 = false;
                        if (z2) {
                            String valueOf = UTF16.valueOf(i12);
                            replaceable.replace(i, i9, valueOf);
                            i2 -= (i9 - i) - valueOf.length();
                            break;
                        }
                    } else {
                        continue;
                    }
                }
                i3 = c + c2 + i8;
            }
        }
        position.contextLimit += i2 - position.limit;
        position.limit = i2;
        position.start = i;
    }

    @Override // ohos.global.icu.text.Transliterator
    public void addSourceTargetSet(UnicodeSet unicodeSet, UnicodeSet unicodeSet2, UnicodeSet unicodeSet3) {
        UnicodeSet filterAsUnicodeSet = getFilterAsUnicodeSet(unicodeSet);
        UnicodeSet unicodeSet4 = new UnicodeSet();
        StringBuilder sb = new StringBuilder();
        char c = 0;
        while (true) {
            char[] cArr = this.spec;
            if (cArr[c] == 65535) {
                break;
            }
            int i = cArr[c] + c + cArr[c + 1] + 5;
            char c2 = cArr[c + 2];
            for (int i2 = 0; i2 < c2; i2++) {
                Utility.appendNumber(sb, i2, c2, 0);
            }
            for (int i3 = c + 5; i3 < i; i3++) {
                unicodeSet4.add(this.spec[i3]);
            }
            c = i;
        }
        unicodeSet4.addAll(sb.toString());
        unicodeSet4.retainAll(filterAsUnicodeSet);
        if (unicodeSet4.size() > 0) {
            unicodeSet2.addAll(unicodeSet4);
            unicodeSet3.addAll(0, 1114111);
        }
    }
}
