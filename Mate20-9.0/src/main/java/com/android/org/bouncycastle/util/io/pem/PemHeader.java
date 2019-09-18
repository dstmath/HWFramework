package com.android.org.bouncycastle.util.io.pem;

public class PemHeader {
    private String name;
    private String value;

    public PemHeader(String name2, String value2) {
        this.name = name2;
        this.value = value2;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public int hashCode() {
        return getHashCode(this.name) + (31 * getHashCode(this.value));
    }

    /* JADX WARNING: type inference failed for: r5v0, types: [java.lang.Object] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public boolean equals(Object r5) {
        boolean z = false;
        if (!(r5 instanceof PemHeader)) {
            return false;
        }
        PemHeader other = r5;
        if (other == this || (isEqual(this.name, other.name) && isEqual(this.value, other.value))) {
            z = true;
        }
        return z;
    }

    private int getHashCode(String s) {
        if (s == null) {
            return 1;
        }
        return s.hashCode();
    }

    private boolean isEqual(String s1, String s2) {
        if (s1 == s2) {
            return true;
        }
        if (s1 == null || s2 == null) {
            return false;
        }
        return s1.equals(s2);
    }
}
