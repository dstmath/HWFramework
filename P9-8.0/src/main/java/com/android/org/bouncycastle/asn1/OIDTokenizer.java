package com.android.org.bouncycastle.asn1;

public class OIDTokenizer {
    private int index = 0;
    private String oid;

    public OIDTokenizer(String oid) {
        this.oid = oid;
    }

    public boolean hasMoreTokens() {
        return this.index != -1;
    }

    public String nextToken() {
        if (this.index == -1) {
            return null;
        }
        int end = this.oid.indexOf(46, this.index);
        String token;
        if (end == -1) {
            token = this.oid.substring(this.index);
            this.index = -1;
            return token;
        }
        token = this.oid.substring(this.index, end);
        this.index = end + 1;
        return token;
    }
}
