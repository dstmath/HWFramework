package org.bouncycastle.jcajce;

import org.bouncycastle.crypto.PBEParametersGenerator;

public class PKCS12Key implements PBKDFKey {
    private final char[] password;
    private final boolean useWrongZeroLengthConversion;

    public PKCS12Key(char[] cArr) {
        this(cArr, false);
    }

    public PKCS12Key(char[] cArr, boolean z) {
        cArr = cArr == null ? new char[0] : cArr;
        this.password = new char[cArr.length];
        this.useWrongZeroLengthConversion = z;
        System.arraycopy(cArr, 0, this.password, 0, cArr.length);
    }

    public String getAlgorithm() {
        return "PKCS12";
    }

    public byte[] getEncoded() {
        return (!this.useWrongZeroLengthConversion || this.password.length != 0) ? PBEParametersGenerator.PKCS12PasswordToBytes(this.password) : new byte[2];
    }

    public String getFormat() {
        return "PKCS12";
    }

    public char[] getPassword() {
        return this.password;
    }
}
