package ohos.com.sun.org.apache.regexp.internal;

public final class CharacterArrayCharacterIterator implements CharacterIterator {
    private final int len;
    private final int off;
    private final char[] src;

    public CharacterArrayCharacterIterator(char[] cArr, int i, int i2) {
        this.src = cArr;
        this.off = i;
        this.len = i2;
    }

    @Override // ohos.com.sun.org.apache.regexp.internal.CharacterIterator
    public String substring(int i, int i2) {
        if (i2 > this.len) {
            throw new IndexOutOfBoundsException("endIndex=" + i2 + "; sequence size=" + this.len);
        } else if (i >= 0 && i <= i2) {
            return new String(this.src, this.off + i, i2 - i);
        } else {
            throw new IndexOutOfBoundsException("beginIndex=" + i + "; endIndex=" + i2);
        }
    }

    @Override // ohos.com.sun.org.apache.regexp.internal.CharacterIterator
    public String substring(int i) {
        return substring(i, this.len);
    }

    @Override // ohos.com.sun.org.apache.regexp.internal.CharacterIterator
    public char charAt(int i) {
        return this.src[this.off + i];
    }

    @Override // ohos.com.sun.org.apache.regexp.internal.CharacterIterator
    public boolean isEnd(int i) {
        return i >= this.len;
    }
}
