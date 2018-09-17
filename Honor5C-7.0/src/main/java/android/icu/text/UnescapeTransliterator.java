package android.icu.text;

import android.icu.impl.PatternTokenizer;
import android.icu.impl.Utility;
import android.icu.lang.UCharacter;
import android.icu.text.Transliterator.Factory;
import android.icu.text.Transliterator.Position;
import android.icu.util.ULocale;

class UnescapeTransliterator extends Transliterator {
    private static final char END = '\uffff';
    private char[] spec;

    static void register() {
        Transliterator.registerFactory("Hex-Any/Unicode", new Factory() {
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/Unicode", new char[]{'\u0002', '\u0000', '\u0010', '\u0004', '\u0006', 'U', '+', UnescapeTransliterator.END});
            }
        });
        Transliterator.registerFactory("Hex-Any/Java", new Factory() {
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/Java", new char[]{'\u0002', '\u0000', '\u0010', '\u0004', '\u0004', PatternTokenizer.BACK_SLASH, ULocale.UNICODE_LOCALE_EXTENSION, UnescapeTransliterator.END});
            }
        });
        Transliterator.registerFactory("Hex-Any/C", new Factory() {
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/C", new char[]{'\u0002', '\u0000', '\u0010', '\u0004', '\u0004', PatternTokenizer.BACK_SLASH, ULocale.UNICODE_LOCALE_EXTENSION, '\u0002', '\u0000', '\u0010', '\b', '\b', PatternTokenizer.BACK_SLASH, 'U', UnescapeTransliterator.END});
            }
        });
        Transliterator.registerFactory("Hex-Any/XML", new Factory() {
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/XML", new char[]{'\u0003', '\u0001', '\u0010', '\u0001', '\u0006', '&', '#', ULocale.PRIVATE_USE_EXTENSION, ';', UnescapeTransliterator.END});
            }
        });
        Transliterator.registerFactory("Hex-Any/XML10", new Factory() {
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/XML10", new char[]{'\u0002', '\u0001', '\n', '\u0001', '\u0007', '&', '#', ';', UnescapeTransliterator.END});
            }
        });
        Transliterator.registerFactory("Hex-Any/Perl", new Factory() {
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/Perl", new char[]{'\u0003', '\u0001', '\u0010', '\u0001', '\u0006', PatternTokenizer.BACK_SLASH, ULocale.PRIVATE_USE_EXTENSION, '{', '}', UnescapeTransliterator.END});
            }
        });
        Transliterator.registerFactory("Hex-Any", new Factory() {
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any", new char[]{'\u0002', '\u0000', '\u0010', '\u0004', '\u0006', 'U', '+', '\u0002', '\u0000', '\u0010', '\u0004', '\u0004', PatternTokenizer.BACK_SLASH, ULocale.UNICODE_LOCALE_EXTENSION, '\u0002', '\u0000', '\u0010', '\b', '\b', PatternTokenizer.BACK_SLASH, 'U', '\u0003', '\u0001', '\u0010', '\u0001', '\u0006', '&', '#', ULocale.PRIVATE_USE_EXTENSION, ';', '\u0002', '\u0001', '\n', '\u0001', '\u0007', '&', '#', ';', '\u0003', '\u0001', '\u0010', '\u0001', '\u0006', PatternTokenizer.BACK_SLASH, ULocale.PRIVATE_USE_EXTENSION, '{', '}', UnescapeTransliterator.END});
            }
        });
    }

    UnescapeTransliterator(String ID, char[] spec) {
        super(ID, null);
        this.spec = spec;
    }

    protected void handleTransliterate(Replaceable text, Position pos, boolean isIncremental) {
        int start = pos.start;
        int limit = pos.limit;
        loop0:
        while (start < limit) {
            int ipat;
            int s;
            int u;
            int ipat2 = 0;
            while (true) {
                if (this.spec[ipat2] == '\uffff') {
                    break;
                }
                ipat = ipat2 + 1;
                int prefixLen = this.spec[ipat2];
                ipat2 = ipat + 1;
                int suffixLen = this.spec[ipat];
                ipat = ipat2 + 1;
                int radix = this.spec[ipat2];
                ipat2 = ipat + 1;
                int minDigits = this.spec[ipat];
                ipat = ipat2 + 1;
                int maxDigits = this.spec[ipat2];
                boolean match = true;
                int i = 0;
                int s2 = start;
                while (i < prefixLen) {
                    if (s2 < limit || i <= 0) {
                        s = s2 + 1;
                        if (text.charAt(s2) != this.spec[ipat + i]) {
                            match = false;
                            break;
                        } else {
                            i++;
                            s2 = s;
                        }
                    } else if (isIncremental) {
                        break loop0;
                    } else {
                        match = false;
                        s = s2;
                    }
                }
                s = s2;
                if (match) {
                    u = 0;
                    int digitCount = 0;
                    while (s < limit) {
                        int ch = text.char32At(s);
                        int digit = UCharacter.digit(ch, radix);
                        if (digit < 0) {
                            break;
                        }
                        s += UTF16.getCharCount(ch);
                        u = (u * radix) + digit;
                        digitCount++;
                        if (digitCount == maxDigits) {
                            break;
                        }
                    }
                    if (s > start && isIncremental) {
                        break loop0;
                    }
                    match = digitCount >= minDigits;
                    if (match) {
                        i = 0;
                        s2 = s;
                        while (i < suffixLen) {
                            if (s2 >= limit) {
                                if (s2 > start && isIncremental) {
                                    break loop0;
                                }
                                match = false;
                                s = s2;
                            } else {
                                s = s2 + 1;
                                if (text.charAt(s2) != this.spec[(ipat + prefixLen) + i]) {
                                    match = false;
                                    break;
                                } else {
                                    i++;
                                    s2 = s;
                                }
                            }
                        }
                        s = s2;
                        if (match) {
                            break;
                        }
                    } else {
                        continue;
                    }
                }
                ipat2 = ipat + (prefixLen + suffixLen);
            }
            String str = UTF16.valueOf(u);
            text.replace(start, s, str);
            limit -= (s - start) - str.length();
            ipat2 = ipat;
            if (start < limit) {
                start += UTF16.getCharCount(text.char32At(start));
            }
        }
        pos.contextLimit += limit - pos.limit;
        pos.limit = limit;
        pos.start = start;
    }

    public void addSourceTargetSet(UnicodeSet inputFilter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        UnicodeSet myFilter = getFilterAsUnicodeSet(inputFilter);
        UnicodeSet items = new UnicodeSet();
        StringBuilder buffer = new StringBuilder();
        int i = 0;
        while (this.spec[i] != END) {
            int j;
            int end = ((this.spec[i] + i) + this.spec[i + 1]) + 5;
            int radix = this.spec[i + 2];
            for (j = 0; j < radix; j++) {
                Utility.appendNumber(buffer, j, radix, 0);
            }
            for (j = i + 5; j < end; j++) {
                items.add(this.spec[j]);
            }
            i = end;
        }
        items.addAll(buffer.toString());
        items.retainAll(myFilter);
        if (items.size() > 0) {
            sourceSet.addAll(items);
            targetSet.addAll(0, UnicodeSet.MAX_VALUE);
        }
    }
}
