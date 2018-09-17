package com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa;

import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import com.android.org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.crypto.params.RSAKeyParameters;
import com.android.org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.KeyUtil;
import com.android.org.bouncycastle.util.Strings;
import java.io.IOException;
import java.math.BigInteger;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.RSAPrivateCrtKeySpec;

public class BCRSAPrivateCrtKey extends BCRSAPrivateKey implements RSAPrivateCrtKey {
    static final long serialVersionUID = 7834723820638524718L;
    private BigInteger crtCoefficient;
    private BigInteger primeExponentP;
    private BigInteger primeExponentQ;
    private BigInteger primeP;
    private BigInteger primeQ;
    private BigInteger publicExponent;

    BCRSAPrivateCrtKey(RSAPrivateCrtKeyParameters key) {
        super((RSAKeyParameters) key);
        this.publicExponent = key.getPublicExponent();
        this.primeP = key.getP();
        this.primeQ = key.getQ();
        this.primeExponentP = key.getDP();
        this.primeExponentQ = key.getDQ();
        this.crtCoefficient = key.getQInv();
    }

    BCRSAPrivateCrtKey(RSAPrivateCrtKeySpec spec) {
        this.modulus = spec.getModulus();
        this.publicExponent = spec.getPublicExponent();
        this.privateExponent = spec.getPrivateExponent();
        this.primeP = spec.getPrimeP();
        this.primeQ = spec.getPrimeQ();
        this.primeExponentP = spec.getPrimeExponentP();
        this.primeExponentQ = spec.getPrimeExponentQ();
        this.crtCoefficient = spec.getCrtCoefficient();
    }

    BCRSAPrivateCrtKey(RSAPrivateCrtKey key) {
        this.modulus = key.getModulus();
        this.publicExponent = key.getPublicExponent();
        this.privateExponent = key.getPrivateExponent();
        this.primeP = key.getPrimeP();
        this.primeQ = key.getPrimeQ();
        this.primeExponentP = key.getPrimeExponentP();
        this.primeExponentQ = key.getPrimeExponentQ();
        this.crtCoefficient = key.getCrtCoefficient();
    }

    BCRSAPrivateCrtKey(PrivateKeyInfo info) throws IOException {
        this(RSAPrivateKey.getInstance(info.parsePrivateKey()));
    }

    BCRSAPrivateCrtKey(RSAPrivateKey key) {
        this.modulus = key.getModulus();
        this.publicExponent = key.getPublicExponent();
        this.privateExponent = key.getPrivateExponent();
        this.primeP = key.getPrime1();
        this.primeQ = key.getPrime2();
        this.primeExponentP = key.getExponent1();
        this.primeExponentQ = key.getExponent2();
        this.crtCoefficient = key.getCoefficient();
    }

    public String getFormat() {
        return "PKCS#8";
    }

    public byte[] getEncoded() {
        return KeyUtil.getEncodedPrivateKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE), new RSAPrivateKey(getModulus(), getPublicExponent(), getPrivateExponent(), getPrimeP(), getPrimeQ(), getPrimeExponentP(), getPrimeExponentQ(), getCrtCoefficient()));
    }

    public BigInteger getPublicExponent() {
        return this.publicExponent;
    }

    public BigInteger getPrimeP() {
        return this.primeP;
    }

    public BigInteger getPrimeQ() {
        return this.primeQ;
    }

    public BigInteger getPrimeExponentP() {
        return this.primeExponentP;
    }

    public BigInteger getPrimeExponentQ() {
        return this.primeExponentQ;
    }

    public BigInteger getCrtCoefficient() {
        return this.crtCoefficient;
    }

    public int hashCode() {
        return (getModulus().hashCode() ^ getPublicExponent().hashCode()) ^ getPrivateExponent().hashCode();
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (o == this) {
            return true;
        }
        if (!(o instanceof RSAPrivateCrtKey)) {
            return false;
        }
        RSAPrivateCrtKey key = (RSAPrivateCrtKey) o;
        if (getModulus().equals(key.getModulus()) && getPublicExponent().equals(key.getPublicExponent()) && getPrivateExponent().equals(key.getPrivateExponent()) && getPrimeP().equals(key.getPrimeP()) && getPrimeQ().equals(key.getPrimeQ()) && getPrimeExponentP().equals(key.getPrimeExponentP()) && getPrimeExponentQ().equals(key.getPrimeExponentQ())) {
            z = getCrtCoefficient().equals(key.getCrtCoefficient());
        }
        return z;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        String nl = Strings.lineSeparator();
        buf.append("RSA Private CRT Key").append(nl);
        buf.append("            modulus: ").append(getModulus().toString(16)).append(nl);
        buf.append("    public exponent: ").append(getPublicExponent().toString(16)).append(nl);
        buf.append("   private exponent: ").append(getPrivateExponent().toString(16)).append(nl);
        buf.append("             primeP: ").append(getPrimeP().toString(16)).append(nl);
        buf.append("             primeQ: ").append(getPrimeQ().toString(16)).append(nl);
        buf.append("     primeExponentP: ").append(getPrimeExponentP().toString(16)).append(nl);
        buf.append("     primeExponentQ: ").append(getPrimeExponentQ().toString(16)).append(nl);
        buf.append("     crtCoefficient: ").append(getCrtCoefficient().toString(16)).append(nl);
        return buf.toString();
    }
}
