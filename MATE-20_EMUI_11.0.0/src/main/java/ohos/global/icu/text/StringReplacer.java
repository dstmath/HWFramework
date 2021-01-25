package ohos.global.icu.text;

import ohos.global.icu.impl.Utility;
import ohos.global.icu.text.RuleBasedTransliterator;

class StringReplacer implements UnicodeReplacer {
    private int cursorPos;
    private final RuleBasedTransliterator.Data data;
    private boolean hasCursor;
    private boolean isComplex;
    private String output;

    public StringReplacer(String str, int i, RuleBasedTransliterator.Data data2) {
        this.output = str;
        this.cursorPos = i;
        this.hasCursor = true;
        this.data = data2;
        this.isComplex = true;
    }

    public StringReplacer(String str, RuleBasedTransliterator.Data data2) {
        this.output = str;
        this.cursorPos = 0;
        this.hasCursor = false;
        this.data = data2;
        this.isComplex = true;
    }

    @Override // ohos.global.icu.text.UnicodeReplacer
    public int replace(Replaceable replaceable, int i, int i2, int[] iArr) {
        int i3;
        int i4;
        int i5;
        int i6;
        if (!this.isComplex) {
            replaceable.replace(i, i2, this.output);
            i3 = this.output.length();
            i4 = this.cursorPos;
        } else {
            StringBuffer stringBuffer = new StringBuffer();
            this.isComplex = false;
            int length = replaceable.length();
            if (i > 0) {
                int charCount = UTF16.getCharCount(replaceable.char32At(i - 1));
                replaceable.copy(i - charCount, i, length);
                i6 = charCount + length;
            } else {
                replaceable.replace(length, length, "￿");
                i6 = length + 1;
            }
            int i7 = 0;
            int i8 = 0;
            int i9 = 0;
            int i10 = i6;
            while (i7 < this.output.length()) {
                if (i7 == this.cursorPos) {
                    i8 = (stringBuffer.length() + i10) - i6;
                }
                int charAt = UTF16.charAt(this.output, i7);
                i7 += UTF16.getCharCount(charAt);
                if (i7 == this.output.length()) {
                    i9 = UTF16.getCharCount(replaceable.char32At(i2));
                    replaceable.copy(i2, i2 + i9, i10);
                }
                UnicodeReplacer lookupReplacer = this.data.lookupReplacer(charAt);
                if (lookupReplacer == null) {
                    UTF16.append(stringBuffer, charAt);
                } else {
                    this.isComplex = true;
                    if (stringBuffer.length() > 0) {
                        replaceable.replace(i10, i10, stringBuffer.toString());
                        i10 += stringBuffer.length();
                        stringBuffer.setLength(0);
                    }
                    i10 += lookupReplacer.replace(replaceable, i10, i10, iArr);
                }
            }
            if (stringBuffer.length() > 0) {
                replaceable.replace(i10, i10, stringBuffer.toString());
                i10 += stringBuffer.length();
            }
            i4 = i7 == this.cursorPos ? i10 - i6 : i8;
            int i11 = i10 - i6;
            replaceable.copy(i6, i10, i);
            replaceable.replace(length + i11, i10 + i9 + i11, "");
            replaceable.replace(i + i11, i2 + i11, "");
            i3 = i11;
        }
        if (this.hasCursor) {
            int i12 = this.cursorPos;
            if (i12 < 0) {
                while (i12 < 0 && i > 0) {
                    i -= UTF16.getCharCount(replaceable.char32At(i - 1));
                    i12++;
                }
                i5 = i + i12;
            } else if (i12 > this.output.length()) {
                int i13 = i + i3;
                int length2 = this.cursorPos - this.output.length();
                while (length2 > 0 && i13 < replaceable.length()) {
                    i13 += UTF16.getCharCount(replaceable.char32At(i13));
                    length2--;
                }
                i5 = i13 + length2;
            } else {
                i5 = i + i4;
            }
            iArr[0] = i5;
        }
        return i3;
    }

    @Override // ohos.global.icu.text.UnicodeReplacer
    public String toReplacerPattern(boolean z) {
        int i;
        StringBuffer stringBuffer = new StringBuffer();
        StringBuffer stringBuffer2 = new StringBuffer();
        int i2 = this.cursorPos;
        if (this.hasCursor && i2 < 0) {
            while (true) {
                i = i2 + 1;
                if (i2 >= 0) {
                    break;
                }
                Utility.appendToRule(stringBuffer, 64, true, z, stringBuffer2);
                i2 = i;
            }
            i2 = i;
        }
        for (int i3 = 0; i3 < this.output.length(); i3++) {
            if (this.hasCursor && i3 == i2) {
                Utility.appendToRule(stringBuffer, 124, true, z, stringBuffer2);
            }
            char charAt = this.output.charAt(i3);
            UnicodeReplacer lookupReplacer = this.data.lookupReplacer(charAt);
            if (lookupReplacer == null) {
                Utility.appendToRule(stringBuffer, (int) charAt, false, z, stringBuffer2);
            } else {
                StringBuffer stringBuffer3 = new StringBuffer(" ");
                stringBuffer3.append(lookupReplacer.toReplacerPattern(z));
                stringBuffer3.append(' ');
                Utility.appendToRule(stringBuffer, stringBuffer3.toString(), true, z, stringBuffer2);
            }
        }
        if (this.hasCursor && i2 > this.output.length()) {
            int length = i2 - this.output.length();
            while (true) {
                int i4 = length - 1;
                if (length <= 0) {
                    break;
                }
                Utility.appendToRule(stringBuffer, 64, true, z, stringBuffer2);
                length = i4;
            }
            Utility.appendToRule(stringBuffer, 124, true, z, stringBuffer2);
        }
        Utility.appendToRule(stringBuffer, -1, true, z, stringBuffer2);
        return stringBuffer.toString();
    }

    @Override // ohos.global.icu.text.UnicodeReplacer
    public void addReplacementSetTo(UnicodeSet unicodeSet) {
        int i = 0;
        while (i < this.output.length()) {
            int charAt = UTF16.charAt(this.output, i);
            UnicodeReplacer lookupReplacer = this.data.lookupReplacer(charAt);
            if (lookupReplacer == null) {
                unicodeSet.add(charAt);
            } else {
                lookupReplacer.addReplacementSetTo(unicodeSet);
            }
            i += UTF16.getCharCount(charAt);
        }
    }
}
