package com.android.org.bouncycastle.jcajce;

import com.android.org.bouncycastle.util.Arrays;
import javax.crypto.interfaces.PBEKey;

public class PKCS12KeyWithParameters extends PKCS12Key implements PBEKey {
    private final int iterationCount;
    private final byte[] salt;

    public PKCS12KeyWithParameters(char[] password, byte[] salt, int iterationCount) {
        super(password);
        this.salt = Arrays.clone(salt);
        this.iterationCount = iterationCount;
    }

    public PKCS12KeyWithParameters(char[] password, boolean useWrongZeroLengthConversion, byte[] salt, int iterationCount) {
        super(password, useWrongZeroLengthConversion);
        this.salt = Arrays.clone(salt);
        this.iterationCount = iterationCount;
    }

    public byte[] getSalt() {
        return this.salt;
    }

    public int getIterationCount() {
        return this.iterationCount;
    }
}
