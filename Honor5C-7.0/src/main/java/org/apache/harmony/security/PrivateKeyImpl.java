package org.apache.harmony.security;

import java.security.PrivateKey;

public class PrivateKeyImpl implements PrivateKey {
    private static final long serialVersionUID = 7776497482533790279L;
    private String algorithm;
    private byte[] encoding;

    public PrivateKeyImpl(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public String getFormat() {
        return "PKCS#8";
    }

    public byte[] getEncoded() {
        byte[] toReturn = new byte[this.encoding.length];
        System.arraycopy(this.encoding, 0, toReturn, 0, this.encoding.length);
        return toReturn;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void setEncoding(byte[] encoding) {
        this.encoding = new byte[encoding.length];
        System.arraycopy(encoding, 0, this.encoding, 0, encoding.length);
    }
}
