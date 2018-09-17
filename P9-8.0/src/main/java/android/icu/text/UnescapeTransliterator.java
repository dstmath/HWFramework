package android.icu.text;

import android.icu.impl.PatternTokenizer;
import android.icu.impl.Utility;
import android.icu.lang.UCharacter;
import android.icu.text.Transliterator.Factory;
import android.icu.text.Transliterator.Position;
import android.icu.util.ULocale;

class UnescapeTransliterator extends Transliterator {
    private static final char END = '￿';
    private char[] spec;

    static void register() {
        Transliterator.registerFactory("Hex-Any/Unicode", new Factory() {
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/Unicode", new char[]{2, 0, 16, 4, 6, 'U', '+', 65535});
            }
        });
        Transliterator.registerFactory("Hex-Any/Java", new Factory() {
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/Java", new char[]{2, 0, 16, 4, 4, PatternTokenizer.BACK_SLASH, 'u', 65535});
            }
        });
        Transliterator.registerFactory("Hex-Any/C", new Factory() {
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/C", new char[]{2, 0, 16, 4, 4, PatternTokenizer.BACK_SLASH, 'u', 2, 0, 16, 8, 8, PatternTokenizer.BACK_SLASH, 'U', 65535});
            }
        });
        Transliterator.registerFactory("Hex-Any/XML", new Factory() {
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/XML", new char[]{3, 1, 16, 1, 6, '&', '#', ULocale.PRIVATE_USE_EXTENSION, ';', 65535});
            }
        });
        Transliterator.registerFactory("Hex-Any/XML10", new Factory() {
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/XML10", new char[]{2, 1, 10, 1, 7, '&', '#', ';', 65535});
            }
        });
        Transliterator.registerFactory("Hex-Any/Perl", new Factory() {
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any/Perl", new char[]{3, 1, 16, 1, 6, PatternTokenizer.BACK_SLASH, ULocale.PRIVATE_USE_EXTENSION, '{', '}', 65535});
            }
        });
        Transliterator.registerFactory("Hex-Any", new Factory() {
            public Transliterator getInstance(String ID) {
                return new UnescapeTransliterator("Hex-Any", new char[]{2, 0, 16, 4, 6, 'U', '+', 2, 0, 16, 4, 4, PatternTokenizer.BACK_SLASH, 'u', 2, 0, 16, 8, 8, PatternTokenizer.BACK_SLASH, 'U', 3, 1, 16, 1, 6, '&', '#', ULocale.PRIVATE_USE_EXTENSION, ';', 2, 1, 10, 1, 7, '&', '#', ';', 3, 1, 16, 1, 6, PatternTokenizer.BACK_SLASH, ULocale.PRIVATE_USE_EXTENSION, '{', '}', 65535});
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
            int ipat = 0;
            while (this.spec[ipat] != 65535) {
                int s;
                int ipat2 = ipat + 1;
                int prefixLen = this.spec[ipat];
                ipat = ipat2 + 1;
                int suffixLen = this.spec[ipat2];
                ipat2 = ipat + 1;
                int radix = this.spec[ipat];
                ipat = ipat2 + 1;
                int minDigits = this.spec[ipat2];
                ipat2 = ipat + 1;
                int maxDigits = this.spec[ipat];
                boolean match = true;
                int i = 0;
                int s2 = start;
                while (i < prefixLen) {
                    if (s2 < limit || i <= 0) {
                        s = s2 + 1;
                        if (text.charAt(s2) != this.spec[ipat2 + i]) {
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
                    int u = 0;
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
                        while (true) {
                            s2 = s;
                            if (i >= suffixLen) {
                                s = s2;
                                break;
                            } else if (s2 >= limit) {
                                if (s2 > start && isIncremental) {
                                    break loop0;
                                }
                                match = false;
                                s = s2;
                            } else {
                                s = s2 + 1;
                                if (text.charAt(s2) != this.spec[(ipat2 + prefixLen) + i]) {
                                    match = false;
                                    break;
                                }
                                i++;
                            }
                        }
                        if (match) {
                            String str = UTF16.valueOf(u);
                            text.replace(start, s, str);
                            limit -= (s - start) - str.length();
                            ipat = ipat2;
                            break;
                        }
                    } else {
                        continue;
                    }
                }
                ipat = ipat2 + (prefixLen + suffixLen);
            }
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
        while (this.spec[i] != 65535) {
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
            targetSet.addAll(0, 1114111);
        }
    }
}
