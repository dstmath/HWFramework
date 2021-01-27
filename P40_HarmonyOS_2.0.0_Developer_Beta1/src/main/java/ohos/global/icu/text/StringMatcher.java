package ohos.global.icu.text;

import ohos.global.icu.impl.Utility;
import ohos.global.icu.text.RuleBasedTransliterator;

/* access modifiers changed from: package-private */
public class StringMatcher implements UnicodeMatcher, UnicodeReplacer {
    private final RuleBasedTransliterator.Data data;
    private int matchLimit;
    private int matchStart;
    private String pattern;
    private int segmentNumber;

    @Override // ohos.global.icu.text.UnicodeReplacer
    public void addReplacementSetTo(UnicodeSet unicodeSet) {
    }

    public StringMatcher(String str, int i, RuleBasedTransliterator.Data data2) {
        this.data = data2;
        this.pattern = str;
        this.matchLimit = -1;
        this.matchStart = -1;
        this.segmentNumber = i;
    }

    public StringMatcher(String str, int i, int i2, int i3, RuleBasedTransliterator.Data data2) {
        this(str.substring(i, i2), i3, data2);
    }

    @Override // ohos.global.icu.text.UnicodeMatcher
    public int matches(Replaceable replaceable, int[] iArr, int i, boolean z) {
        int[] iArr2 = {iArr[0]};
        if (i < iArr2[0]) {
            for (int length = this.pattern.length() - 1; length >= 0; length--) {
                char charAt = this.pattern.charAt(length);
                UnicodeMatcher lookupMatcher = this.data.lookupMatcher(charAt);
                if (lookupMatcher != null) {
                    int matches = lookupMatcher.matches(replaceable, iArr2, i, z);
                    if (matches != 2) {
                        return matches;
                    }
                } else if (iArr2[0] <= i || charAt != replaceable.charAt(iArr2[0])) {
                    return 0;
                } else {
                    iArr2[0] = iArr2[0] - 1;
                }
            }
            if (this.matchStart < 0) {
                this.matchStart = iArr2[0] + 1;
                this.matchLimit = iArr[0] + 1;
            }
        } else {
            for (int i2 = 0; i2 < this.pattern.length(); i2++) {
                if (z && iArr2[0] == i) {
                    return 1;
                }
                char charAt2 = this.pattern.charAt(i2);
                UnicodeMatcher lookupMatcher2 = this.data.lookupMatcher(charAt2);
                if (lookupMatcher2 != null) {
                    int matches2 = lookupMatcher2.matches(replaceable, iArr2, i, z);
                    if (matches2 != 2) {
                        return matches2;
                    }
                } else if (iArr2[0] >= i || charAt2 != replaceable.charAt(iArr2[0])) {
                    return 0;
                } else {
                    iArr2[0] = iArr2[0] + 1;
                }
            }
            this.matchStart = iArr[0];
            this.matchLimit = iArr2[0];
        }
        iArr[0] = iArr2[0];
        return 2;
    }

    @Override // ohos.global.icu.text.UnicodeMatcher
    public String toPattern(boolean z) {
        StringBuffer stringBuffer = new StringBuffer();
        StringBuffer stringBuffer2 = new StringBuffer();
        if (this.segmentNumber > 0) {
            stringBuffer.append('(');
        }
        for (int i = 0; i < this.pattern.length(); i++) {
            char charAt = this.pattern.charAt(i);
            UnicodeMatcher lookupMatcher = this.data.lookupMatcher(charAt);
            if (lookupMatcher == null) {
                Utility.appendToRule(stringBuffer, charAt, false, z, stringBuffer2);
            } else {
                Utility.appendToRule(stringBuffer, lookupMatcher.toPattern(z), true, z, stringBuffer2);
            }
        }
        if (this.segmentNumber > 0) {
            stringBuffer.append(')');
        }
        Utility.appendToRule(stringBuffer, -1, true, z, stringBuffer2);
        return stringBuffer.toString();
    }

    @Override // ohos.global.icu.text.UnicodeMatcher
    public boolean matchesIndexValue(int i) {
        if (this.pattern.length() == 0) {
            return true;
        }
        int charAt = UTF16.charAt(this.pattern, 0);
        UnicodeMatcher lookupMatcher = this.data.lookupMatcher(charAt);
        if (lookupMatcher != null) {
            return lookupMatcher.matchesIndexValue(i);
        }
        if ((charAt & 255) == i) {
            return true;
        }
        return false;
    }

    @Override // ohos.global.icu.text.UnicodeMatcher
    public void addMatchSetTo(UnicodeSet unicodeSet) {
        int i = 0;
        while (i < this.pattern.length()) {
            int charAt = UTF16.charAt(this.pattern, i);
            UnicodeMatcher lookupMatcher = this.data.lookupMatcher(charAt);
            if (lookupMatcher == null) {
                unicodeSet.add(charAt);
            } else {
                lookupMatcher.addMatchSetTo(unicodeSet);
            }
            i += UTF16.getCharCount(charAt);
        }
    }

    @Override // ohos.global.icu.text.UnicodeReplacer
    public int replace(Replaceable replaceable, int i, int i2, int[] iArr) {
        int i3;
        int i4;
        int i5 = this.matchStart;
        if (i5 < 0 || i5 == (i4 = this.matchLimit)) {
            i3 = 0;
        } else {
            replaceable.copy(i5, i4, i2);
            i3 = this.matchLimit - this.matchStart;
        }
        replaceable.replace(i, i2, "");
        return i3;
    }

    @Override // ohos.global.icu.text.UnicodeReplacer
    public String toReplacerPattern(boolean z) {
        StringBuffer stringBuffer = new StringBuffer("$");
        Utility.appendNumber(stringBuffer, this.segmentNumber, 10, 1);
        return stringBuffer.toString();
    }

    public void resetMatch() {
        this.matchLimit = -1;
        this.matchStart = -1;
    }
}
