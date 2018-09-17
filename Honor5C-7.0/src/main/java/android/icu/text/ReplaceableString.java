package android.icu.text;

public class ReplaceableString implements Replaceable {
    private StringBuffer buf;

    public ReplaceableString(String str) {
        this.buf = new StringBuffer(str);
    }

    public ReplaceableString(StringBuffer buf) {
        this.buf = buf;
    }

    public ReplaceableString() {
        this.buf = new StringBuffer();
    }

    public String toString() {
        return this.buf.toString();
    }

    public String substring(int start, int limit) {
        return this.buf.substring(start, limit);
    }

    public int length() {
        return this.buf.length();
    }

    public char charAt(int offset) {
        return this.buf.charAt(offset);
    }

    public int char32At(int offset) {
        return UTF16.charAt(this.buf, offset);
    }

    public void getChars(int srcStart, int srcLimit, char[] dst, int dstStart) {
        if (srcStart != srcLimit) {
            this.buf.getChars(srcStart, srcLimit, dst, dstStart);
        }
    }

    public void replace(int start, int limit, String text) {
        this.buf.replace(start, limit, text);
    }

    public void replace(int start, int limit, char[] chars, int charsStart, int charsLen) {
        this.buf.delete(start, limit);
        this.buf.insert(start, chars, charsStart, charsLen);
    }

    public void copy(int start, int limit, int dest) {
        if (start != limit || start < 0 || start > this.buf.length()) {
            char[] text = new char[(limit - start)];
            getChars(start, limit, text, 0);
            replace(dest, dest, text, 0, limit - start);
        }
    }

    public boolean hasMetaData() {
        return false;
    }
}
