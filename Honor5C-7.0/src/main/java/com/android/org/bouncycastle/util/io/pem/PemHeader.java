package com.android.org.bouncycastle.util.io.pem;

public class PemHeader {
    private String name;
    private String value;

    public PemHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public int hashCode() {
        return getHashCode(this.name) + (getHashCode(this.value) * 31);
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof PemHeader)) {
            return false;
        }
        PemHeader other = (PemHeader) o;
        if (other == this) {
            z = true;
        } else if (isEqual(this.name, other.name)) {
            z = isEqual(this.value, other.value);
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
