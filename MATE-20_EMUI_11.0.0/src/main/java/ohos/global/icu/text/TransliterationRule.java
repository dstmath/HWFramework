package ohos.global.icu.text;

import ohos.global.icu.impl.Utility;
import ohos.global.icu.text.RuleBasedTransliterator;
import ohos.global.icu.text.Transliterator;

/* access modifiers changed from: package-private */
public class TransliterationRule {
    static final int ANCHOR_END = 2;
    static final int ANCHOR_START = 1;
    private StringMatcher anteContext;
    private int anteContextLength;
    private final RuleBasedTransliterator.Data data;
    byte flags;
    private StringMatcher key;
    private int keyLength;
    private UnicodeReplacer output;
    private String pattern;
    private StringMatcher postContext;
    UnicodeMatcher[] segments;

    public TransliterationRule(String str, int i, int i2, String str2, int i3, int i4, UnicodeMatcher[] unicodeMatcherArr, boolean z, boolean z2, RuleBasedTransliterator.Data data2) {
        this.data = data2;
        if (i < 0) {
            this.anteContextLength = 0;
        } else if (i <= str.length()) {
            this.anteContextLength = i;
        } else {
            throw new IllegalArgumentException("Invalid ante context");
        }
        if (i2 < 0) {
            this.keyLength = str.length() - this.anteContextLength;
        } else if (i2 < this.anteContextLength || i2 > str.length()) {
            throw new IllegalArgumentException("Invalid post context");
        } else {
            this.keyLength = i2 - this.anteContextLength;
        }
        if (i3 < 0) {
            i3 = str2.length();
        } else if (i3 > str2.length()) {
            throw new IllegalArgumentException("Invalid cursor position");
        }
        this.segments = unicodeMatcherArr;
        this.pattern = str;
        this.flags = 0;
        if (z) {
            this.flags = (byte) (this.flags | 1);
        }
        if (z2) {
            this.flags = (byte) (this.flags | 2);
        }
        this.anteContext = null;
        int i5 = this.anteContextLength;
        if (i5 > 0) {
            this.anteContext = new StringMatcher(this.pattern.substring(0, i5), 0, this.data);
        }
        this.key = null;
        int i6 = this.keyLength;
        if (i6 > 0) {
            String str3 = this.pattern;
            int i7 = this.anteContextLength;
            this.key = new StringMatcher(str3.substring(i7, i6 + i7), 0, this.data);
        }
        int length = this.pattern.length();
        int i8 = this.keyLength;
        int i9 = this.anteContextLength;
        int i10 = (length - i8) - i9;
        this.postContext = null;
        if (i10 > 0) {
            this.postContext = new StringMatcher(this.pattern.substring(i9 + i8), 0, this.data);
        }
        this.output = new StringReplacer(str2, i3 + i4, this.data);
    }

    public int getAnteContextLength() {
        int i = this.anteContextLength;
        int i2 = 1;
        if ((this.flags & 1) == 0) {
            i2 = 0;
        }
        return i + i2;
    }

    /* access modifiers changed from: package-private */
    public final int getIndexValue() {
        if (this.anteContextLength == this.pattern.length()) {
            return -1;
        }
        int charAt = UTF16.charAt(this.pattern, this.anteContextLength);
        if (this.data.lookupMatcher(charAt) == null) {
            return charAt & 255;
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public final boolean matchesIndexValue(int i) {
        StringMatcher stringMatcher = this.key;
        if (stringMatcher == null) {
            stringMatcher = this.postContext;
        }
        if (stringMatcher != null) {
            return stringMatcher.matchesIndexValue(i);
        }
        return true;
    }

    public boolean masks(TransliterationRule transliterationRule) {
        int length = this.pattern.length();
        int i = this.anteContextLength;
        int i2 = transliterationRule.anteContextLength;
        int length2 = this.pattern.length() - i;
        int length3 = transliterationRule.pattern.length() - i2;
        if (i != i2 || length2 != length3 || this.keyLength > transliterationRule.keyLength || !transliterationRule.pattern.regionMatches(0, this.pattern, 0, length)) {
            return i <= i2 && (length2 < length3 || (length2 == length3 && this.keyLength <= transliterationRule.keyLength)) && transliterationRule.pattern.regionMatches(i2 - i, this.pattern, 0, length);
        }
        byte b = this.flags;
        if (b == transliterationRule.flags) {
            return true;
        }
        if ((b & 1) == 0 && (b & 2) == 0) {
            return true;
        }
        byte b2 = transliterationRule.flags;
        return ((b2 & 1) == 0 || (b2 & 2) == 0) ? false : true;
    }

    static final int posBefore(Replaceable replaceable, int i) {
        return i > 0 ? i - UTF16.getCharCount(replaceable.char32At(i - 1)) : i - 1;
    }

    static final int posAfter(Replaceable replaceable, int i) {
        return (i < 0 || i >= replaceable.length()) ? i + 1 : i + UTF16.getCharCount(replaceable.char32At(i));
    }

    public int matchAndReplace(Replaceable replaceable, Transliterator.Position position, boolean z) {
        int matches;
        if (this.segments != null) {
            int i = 0;
            while (true) {
                UnicodeMatcher[] unicodeMatcherArr = this.segments;
                if (i >= unicodeMatcherArr.length) {
                    break;
                }
                ((StringMatcher) unicodeMatcherArr[i]).resetMatch();
                i++;
            }
        }
        int posBefore = posBefore(replaceable, position.contextStart);
        int[] iArr = {posBefore(replaceable, position.start)};
        StringMatcher stringMatcher = this.anteContext;
        if (stringMatcher != null && stringMatcher.matches(replaceable, iArr, posBefore, false) != 2) {
            return 0;
        }
        int i2 = iArr[0];
        int posAfter = posAfter(replaceable, i2);
        if ((this.flags & 1) != 0 && i2 != posBefore) {
            return 0;
        }
        iArr[0] = position.start;
        StringMatcher stringMatcher2 = this.key;
        if (stringMatcher2 != null && (matches = stringMatcher2.matches(replaceable, iArr, position.limit, z)) != 2) {
            return matches;
        }
        int i3 = iArr[0];
        if (this.postContext != null) {
            if (z && i3 == position.limit) {
                return 1;
            }
            int matches2 = this.postContext.matches(replaceable, iArr, position.contextLimit, z);
            if (matches2 != 2) {
                return matches2;
            }
        }
        int i4 = iArr[0];
        if ((this.flags & 2) != 0) {
            if (i4 != position.contextLimit) {
                return 0;
            }
            if (z) {
                return 1;
            }
        }
        int replace = this.output.replace(replaceable, position.start, i3, iArr) - (i3 - position.start);
        int i5 = iArr[0];
        position.limit += replace;
        position.contextLimit += replace;
        position.start = Math.max(posAfter, Math.min(Math.min(i4 + replace, position.limit), i5));
        return 2;
    }

    public String toRule(boolean z) {
        StringBuffer stringBuffer = new StringBuffer();
        StringBuffer stringBuffer2 = new StringBuffer();
        boolean z2 = (this.anteContext == null && this.postContext == null) ? false : true;
        if ((this.flags & 1) != 0) {
            stringBuffer.append('^');
        }
        Utility.appendToRule(stringBuffer, this.anteContext, z, stringBuffer2);
        if (z2) {
            Utility.appendToRule(stringBuffer, 123, true, z, stringBuffer2);
        }
        Utility.appendToRule(stringBuffer, this.key, z, stringBuffer2);
        if (z2) {
            Utility.appendToRule(stringBuffer, 125, true, z, stringBuffer2);
        }
        Utility.appendToRule(stringBuffer, this.postContext, z, stringBuffer2);
        if ((this.flags & 2) != 0) {
            stringBuffer.append(SymbolTable.SYMBOL_REF);
        }
        Utility.appendToRule(stringBuffer, " > ", true, z, stringBuffer2);
        Utility.appendToRule(stringBuffer, this.output.toReplacerPattern(z), true, z, stringBuffer2);
        Utility.appendToRule(stringBuffer, 59, true, z, stringBuffer2);
        return stringBuffer.toString();
    }

    public String toString() {
        return '{' + toRule(true) + '}';
    }

    /* access modifiers changed from: package-private */
    public void addSourceTargetSet(UnicodeSet unicodeSet, UnicodeSet unicodeSet2, UnicodeSet unicodeSet3, UnicodeSet unicodeSet4) {
        int i = this.anteContextLength + this.keyLength;
        UnicodeSet unicodeSet5 = new UnicodeSet();
        UnicodeSet unicodeSet6 = new UnicodeSet();
        int i2 = this.anteContextLength;
        while (i2 < i) {
            int charAt = UTF16.charAt(this.pattern, i2);
            i2 += UTF16.getCharCount(charAt);
            UnicodeMatcher lookupMatcher = this.data.lookupMatcher(charAt);
            if (lookupMatcher != null) {
                try {
                    if (unicodeSet.containsSome((UnicodeSet) lookupMatcher)) {
                        lookupMatcher.addMatchSetTo(unicodeSet5);
                    } else {
                        return;
                    }
                } catch (ClassCastException unused) {
                    unicodeSet6.clear();
                    lookupMatcher.addMatchSetTo(unicodeSet6);
                    if (unicodeSet.containsSome(unicodeSet6)) {
                        unicodeSet5.addAll(unicodeSet6);
                    } else {
                        return;
                    }
                }
            } else if (unicodeSet.contains(charAt)) {
                unicodeSet5.add(charAt);
            } else {
                return;
            }
        }
        unicodeSet2.addAll(unicodeSet5);
        this.output.addReplacementSetTo(unicodeSet3);
    }
}
