package org.bouncycastle.util.io.pem;

public class PemHeader {
    private String name;
    private String value;

    public PemHeader(String str, String str2) {
        this.name = str;
        this.value = str2;
    }

    private int getHashCode(String str) {
        if (str == null) {
            return 1;
        }
        return str.hashCode();
    }

    private boolean isEqual(String str, String str2) {
        if (str == str2) {
            return true;
        }
        if (str == null || str2 == null) {
            return false;
        }
        return str.equals(str2);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof PemHeader)) {
            return false;
        }
        PemHeader pemHeader = (PemHeader) obj;
        if (pemHeader == this || (isEqual(this.name, pemHeader.name) && isEqual(this.value, pemHeader.value))) {
            z = true;
        }
        return z;
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
}
