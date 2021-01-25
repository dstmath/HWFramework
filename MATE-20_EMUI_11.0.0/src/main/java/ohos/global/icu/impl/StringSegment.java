package ohos.global.icu.impl;

import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.UnicodeSet;

public class StringSegment implements CharSequence {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private int end;
    private boolean foldCase;
    private int start = 0;
    private final String str;

    public StringSegment(String str2, boolean z) {
        this.str = str2;
        this.end = str2.length();
        this.foldCase = z;
    }

    public int getOffset() {
        return this.start;
    }

    public void setOffset(int i) {
        this.start = i;
    }

    public void adjustOffset(int i) {
        this.start += i;
    }

    public void adjustOffsetByCodePoint() {
        this.start += Character.charCount(getCodePoint());
    }

    public void setLength(int i) {
        this.end = this.start + i;
    }

    public void resetLength() {
        this.end = this.str.length();
    }

    @Override // java.lang.CharSequence
    public int length() {
        return this.end - this.start;
    }

    @Override // java.lang.CharSequence
    public char charAt(int i) {
        return this.str.charAt(i + this.start);
    }

    @Override // java.lang.CharSequence
    public CharSequence subSequence(int i, int i2) {
        String str2 = this.str;
        int i3 = this.start;
        return str2.subSequence(i + i3, i2 + i3);
    }

    public int getCodePoint() {
        char charAt = this.str.charAt(this.start);
        if (Character.isHighSurrogate(charAt)) {
            int i = this.start;
            if (i + 1 < this.end) {
                char charAt2 = this.str.charAt(i + 1);
                if (Character.isLowSurrogate(charAt2)) {
                    return Character.toCodePoint(charAt, charAt2);
                }
            }
        }
        return charAt;
    }

    public int codePointAt(int i) {
        return this.str.codePointAt(this.start + i);
    }

    public boolean startsWith(int i) {
        return codePointsEqual(getCodePoint(), i, this.foldCase);
    }

    public boolean startsWith(UnicodeSet unicodeSet) {
        int codePoint = getCodePoint();
        if (codePoint == -1) {
            return false;
        }
        return unicodeSet.contains(codePoint);
    }

    public boolean startsWith(CharSequence charSequence) {
        if (charSequence == null || charSequence.length() == 0 || length() == 0) {
            return false;
        }
        return codePointsEqual(Character.codePointAt(this, 0), Character.codePointAt(charSequence, 0), this.foldCase);
    }

    public int getCommonPrefixLength(CharSequence charSequence) {
        return getPrefixLengthInternal(charSequence, this.foldCase);
    }

    public int getCaseSensitivePrefixLength(CharSequence charSequence) {
        return getPrefixLengthInternal(charSequence, false);
    }

    private int getPrefixLengthInternal(CharSequence charSequence, boolean z) {
        int i = 0;
        while (i < Math.min(length(), charSequence.length())) {
            int codePointAt = Character.codePointAt(this, i);
            if (!codePointsEqual(codePointAt, Character.codePointAt(charSequence, i), z)) {
                break;
            }
            i += Character.charCount(codePointAt);
        }
        return i;
    }

    private static final boolean codePointsEqual(int i, int i2, boolean z) {
        if (i == i2) {
            return true;
        }
        if (!z) {
            return false;
        }
        return UCharacter.foldCase(i, true) == UCharacter.foldCase(i2, true);
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (!(obj instanceof CharSequence)) {
            return false;
        }
        return Utility.charSequenceEquals(this, (CharSequence) obj);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Utility.charSequenceHashCode(this);
    }

    @Override // java.lang.CharSequence, java.lang.Object
    public String toString() {
        return this.str.substring(0, this.start) + "[" + this.str.substring(this.start, this.end) + "]" + this.str.substring(this.end);
    }
}
