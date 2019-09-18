package com.android.org.bouncycastle.asn1;

public class OIDTokenizer {
    private int index = 0;
    private String oid;

    public OIDTokenizer(String oid2) {
        this.oid = oid2;
    }

    public boolean hasMoreTokens() {
        return this.index != -1;
    }

    public String nextToken() {
        if (this.index == -1) {
            return null;
        }
        int end = this.oid.indexOf(46, this.index);
        if (end == -1) {
            String token = this.oid.substring(this.index);
            this.index = -1;
            return token;
        }
        String token2 = this.oid.substring(this.index, end);
        this.index = end + 1;
        return token2;
    }
}
