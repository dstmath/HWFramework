package ohos.global.icu.text;

import ohos.global.icu.impl.Utility;
import ohos.global.icu.text.Transliterator;

class EscapeTransliterator extends Transliterator {
    private boolean grokSupplementals;
    private int minDigits;
    private String prefix;
    private int radix;
    private String suffix;
    private EscapeTransliterator supplementalHandler;

    static void register() {
        Transliterator.registerFactory("Any-Hex/Unicode", new Transliterator.Factory() {
            /* class ohos.global.icu.text.EscapeTransliterator.AnonymousClass1 */

            @Override // ohos.global.icu.text.Transliterator.Factory
            public Transliterator getInstance(String str) {
                return new EscapeTransliterator("Any-Hex/Unicode", "U+", "", 16, 4, true, null);
            }
        });
        Transliterator.registerFactory("Any-Hex/Java", new Transliterator.Factory() {
            /* class ohos.global.icu.text.EscapeTransliterator.AnonymousClass2 */

            @Override // ohos.global.icu.text.Transliterator.Factory
            public Transliterator getInstance(String str) {
                return new EscapeTransliterator("Any-Hex/Java", "\\u", "", 16, 4, false, null);
            }
        });
        Transliterator.registerFactory("Any-Hex/C", new Transliterator.Factory() {
            /* class ohos.global.icu.text.EscapeTransliterator.AnonymousClass3 */

            @Override // ohos.global.icu.text.Transliterator.Factory
            public Transliterator getInstance(String str) {
                return new EscapeTransliterator("Any-Hex/C", "\\u", "", 16, 4, true, new EscapeTransliterator("", "\\U", "", 16, 8, true, null));
            }
        });
        Transliterator.registerFactory("Any-Hex/XML", new Transliterator.Factory() {
            /* class ohos.global.icu.text.EscapeTransliterator.AnonymousClass4 */

            @Override // ohos.global.icu.text.Transliterator.Factory
            public Transliterator getInstance(String str) {
                return new EscapeTransliterator("Any-Hex/XML", "&#x", ";", 16, 1, true, null);
            }
        });
        Transliterator.registerFactory("Any-Hex/XML10", new Transliterator.Factory() {
            /* class ohos.global.icu.text.EscapeTransliterator.AnonymousClass5 */

            @Override // ohos.global.icu.text.Transliterator.Factory
            public Transliterator getInstance(String str) {
                return new EscapeTransliterator("Any-Hex/XML10", "&#", ";", 10, 1, true, null);
            }
        });
        Transliterator.registerFactory("Any-Hex/Perl", new Transliterator.Factory() {
            /* class ohos.global.icu.text.EscapeTransliterator.AnonymousClass6 */

            @Override // ohos.global.icu.text.Transliterator.Factory
            public Transliterator getInstance(String str) {
                return new EscapeTransliterator("Any-Hex/Perl", "\\x{", "}", 16, 1, true, null);
            }
        });
        Transliterator.registerFactory("Any-Hex/Plain", new Transliterator.Factory() {
            /* class ohos.global.icu.text.EscapeTransliterator.AnonymousClass7 */

            @Override // ohos.global.icu.text.Transliterator.Factory
            public Transliterator getInstance(String str) {
                return new EscapeTransliterator("Any-Hex/Plain", "", "", 16, 4, true, null);
            }
        });
        Transliterator.registerFactory("Any-Hex", new Transliterator.Factory() {
            /* class ohos.global.icu.text.EscapeTransliterator.AnonymousClass8 */

            @Override // ohos.global.icu.text.Transliterator.Factory
            public Transliterator getInstance(String str) {
                return new EscapeTransliterator("Any-Hex", "\\u", "", 16, 4, false, null);
            }
        });
    }

    EscapeTransliterator(String str, String str2, String str3, int i, int i2, boolean z, EscapeTransliterator escapeTransliterator) {
        super(str, null);
        this.prefix = str2;
        this.suffix = str3;
        this.radix = i;
        this.minDigits = i2;
        this.grokSupplementals = z;
        this.supplementalHandler = escapeTransliterator;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.text.Transliterator
    public void handleTransliterate(Replaceable replaceable, Transliterator.Position position, boolean z) {
        int i = position.start;
        int i2 = position.limit;
        StringBuilder sb = new StringBuilder(this.prefix);
        int length = this.prefix.length();
        boolean z2 = false;
        while (i < i2) {
            int char32At = this.grokSupplementals ? replaceable.char32At(i) : replaceable.charAt(i);
            int charCount = this.grokSupplementals ? UTF16.getCharCount(char32At) : 1;
            if ((-65536 & char32At) == 0 || this.supplementalHandler == null) {
                if (z2) {
                    sb.setLength(0);
                    sb.append(this.prefix);
                    z2 = false;
                } else {
                    sb.setLength(length);
                }
                Utility.appendNumber(sb, char32At, this.radix, this.minDigits);
                sb.append(this.suffix);
            } else {
                sb.setLength(0);
                sb.append(this.supplementalHandler.prefix);
                EscapeTransliterator escapeTransliterator = this.supplementalHandler;
                Utility.appendNumber(sb, char32At, escapeTransliterator.radix, escapeTransliterator.minDigits);
                sb.append(this.supplementalHandler.suffix);
                z2 = true;
            }
            replaceable.replace(i, i + charCount, sb.toString());
            i += sb.length();
            i2 += sb.length() - charCount;
        }
        position.contextLimit += i2 - position.limit;
        position.limit = i2;
        position.start = i;
    }

    @Override // ohos.global.icu.text.Transliterator
    public void addSourceTargetSet(UnicodeSet unicodeSet, UnicodeSet unicodeSet2, UnicodeSet unicodeSet3) {
        unicodeSet2.addAll(getFilterAsUnicodeSet(unicodeSet));
        while (this != null) {
            if (unicodeSet.size() != 0) {
                unicodeSet3.addAll(this.prefix);
                unicodeSet3.addAll(this.suffix);
                StringBuilder sb = new StringBuilder();
                int i = 0;
                while (true) {
                    int i2 = this.radix;
                    if (i >= i2) {
                        break;
                    }
                    Utility.appendNumber(sb, i, i2, this.minDigits);
                    i++;
                }
                unicodeSet3.addAll(sb.toString());
            }
            this = this.supplementalHandler;
        }
    }
}
