package ohos.global.icu.text;

public class ReplaceableString implements Replaceable {
    private StringBuffer buf;

    @Override // ohos.global.icu.text.Replaceable
    public boolean hasMetaData() {
        return false;
    }

    public ReplaceableString(String str) {
        this.buf = new StringBuffer(str);
    }

    public ReplaceableString(StringBuffer stringBuffer) {
        this.buf = stringBuffer;
    }

    public ReplaceableString() {
        this.buf = new StringBuffer();
    }

    public String toString() {
        return this.buf.toString();
    }

    public String substring(int i, int i2) {
        return this.buf.substring(i, i2);
    }

    @Override // ohos.global.icu.text.Replaceable
    public int length() {
        return this.buf.length();
    }

    @Override // ohos.global.icu.text.Replaceable
    public char charAt(int i) {
        return this.buf.charAt(i);
    }

    @Override // ohos.global.icu.text.Replaceable
    public int char32At(int i) {
        return UTF16.charAt(this.buf, i);
    }

    @Override // ohos.global.icu.text.Replaceable
    public void getChars(int i, int i2, char[] cArr, int i3) {
        if (i != i2) {
            this.buf.getChars(i, i2, cArr, i3);
        }
    }

    @Override // ohos.global.icu.text.Replaceable
    public void replace(int i, int i2, String str) {
        this.buf.replace(i, i2, str);
    }

    @Override // ohos.global.icu.text.Replaceable
    public void replace(int i, int i2, char[] cArr, int i3, int i4) {
        this.buf.delete(i, i2);
        this.buf.insert(i, cArr, i3, i4);
    }

    @Override // ohos.global.icu.text.Replaceable
    public void copy(int i, int i2, int i3) {
        if (i != i2 || i < 0 || i > this.buf.length()) {
            int i4 = i2 - i;
            char[] cArr = new char[i4];
            getChars(i, i2, cArr, 0);
            replace(i3, i3, cArr, 0, i4);
        }
    }
}
