package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.crypto.CipherParameters;
import java.math.BigInteger;

public class DSAParameters implements CipherParameters {
    private BigInteger g;
    private BigInteger p;
    private BigInteger q;
    private DSAValidationParameters validation;

    public DSAParameters(BigInteger p2, BigInteger q2, BigInteger g2) {
        this.g = g2;
        this.p = p2;
        this.q = q2;
    }

    public DSAParameters(BigInteger p2, BigInteger q2, BigInteger g2, DSAValidationParameters params) {
        this.g = g2;
        this.p = p2;
        this.q = q2;
        this.validation = params;
    }

    public BigInteger getP() {
        return this.p;
    }

    public BigInteger getQ() {
        return this.q;
    }

    public BigInteger getG() {
        return this.g;
    }

    public DSAValidationParameters getValidationParameters() {
        return this.validation;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof DSAParameters)) {
            return false;
        }
        DSAParameters pm = (DSAParameters) obj;
        if (pm.getP().equals(this.p) && pm.getQ().equals(this.q) && pm.getG().equals(this.g)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return (getP().hashCode() ^ getQ().hashCode()) ^ getG().hashCode();
    }
}
