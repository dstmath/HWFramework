package com.android.org.bouncycastle.asn1.x500.style;

public class X500NameTokenizer {
    private StringBuffer buf;
    private int index;
    private char separator;
    private String value;

    public X500NameTokenizer(String oid) {
        this(oid, ',');
    }

    public X500NameTokenizer(String oid, char separator) {
        this.buf = new StringBuffer();
        this.value = oid;
        this.index = -1;
        this.separator = separator;
    }

    public boolean hasMoreTokens() {
        return this.index != this.value.length();
    }

    public String nextToken() {
        if (this.index == this.value.length()) {
            return null;
        }
        int end = this.index + 1;
        int quoted = 0;
        boolean escaped = false;
        this.buf.setLength(0);
        while (end != this.value.length()) {
            char c = this.value.charAt(end);
            if (c == '\"') {
                if (!escaped) {
                    quoted ^= 1;
                }
                this.buf.append(c);
                escaped = false;
            } else if (escaped || quoted != 0) {
                this.buf.append(c);
                escaped = false;
            } else if (c == '\\') {
                this.buf.append(c);
                escaped = true;
            } else if (c == this.separator) {
                break;
            } else {
                this.buf.append(c);
            }
            end++;
        }
        this.index = end;
        return this.buf.toString();
    }
}
