package com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa;

import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import com.android.org.bouncycastle.crypto.params.RSAKeyParameters;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.KeyUtil;
import com.android.org.bouncycastle.util.Strings;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

public class BCRSAPublicKey implements RSAPublicKey {
    private static final AlgorithmIdentifier DEFAULT_ALGORITHM_IDENTIFIER = new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE);
    static final long serialVersionUID = 2675817738516720772L;
    private transient AlgorithmIdentifier algorithmIdentifier;
    private BigInteger modulus;
    private BigInteger publicExponent;

    BCRSAPublicKey(RSAKeyParameters key) {
        this.algorithmIdentifier = DEFAULT_ALGORITHM_IDENTIFIER;
        this.modulus = key.getModulus();
        this.publicExponent = key.getExponent();
    }

    BCRSAPublicKey(RSAPublicKeySpec spec) {
        this.algorithmIdentifier = DEFAULT_ALGORITHM_IDENTIFIER;
        this.modulus = spec.getModulus();
        this.publicExponent = spec.getPublicExponent();
    }

    BCRSAPublicKey(RSAPublicKey key) {
        this.algorithmIdentifier = DEFAULT_ALGORITHM_IDENTIFIER;
        this.modulus = key.getModulus();
        this.publicExponent = key.getPublicExponent();
    }

    BCRSAPublicKey(SubjectPublicKeyInfo info) {
        populateFromPublicKeyInfo(info);
    }

    private void populateFromPublicKeyInfo(SubjectPublicKeyInfo info) {
        try {
            com.android.org.bouncycastle.asn1.pkcs.RSAPublicKey pubKey = com.android.org.bouncycastle.asn1.pkcs.RSAPublicKey.getInstance(info.parsePublicKey());
            this.algorithmIdentifier = info.getAlgorithm();
            this.modulus = pubKey.getModulus();
            this.publicExponent = pubKey.getPublicExponent();
        } catch (IOException e) {
            throw new IllegalArgumentException("invalid info structure in RSA public key");
        }
    }

    public BigInteger getModulus() {
        return this.modulus;
    }

    public BigInteger getPublicExponent() {
        return this.publicExponent;
    }

    public String getAlgorithm() {
        return "RSA";
    }

    public String getFormat() {
        return "X.509";
    }

    public byte[] getEncoded() {
        return KeyUtil.getEncodedSubjectPublicKeyInfo(this.algorithmIdentifier, new com.android.org.bouncycastle.asn1.pkcs.RSAPublicKey(getModulus(), getPublicExponent()));
    }

    public int hashCode() {
        return getModulus().hashCode() ^ getPublicExponent().hashCode();
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (o == this) {
            return true;
        }
        if (!(o instanceof RSAPublicKey)) {
            return false;
        }
        RSAPublicKey key = (RSAPublicKey) o;
        if (getModulus().equals(key.getModulus())) {
            z = getPublicExponent().equals(key.getPublicExponent());
        }
        return z;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        String nl = Strings.lineSeparator();
        buf.append("RSA Public Key").append(nl);
        buf.append("            modulus: ").append(getModulus().toString(16)).append(nl);
        buf.append("    public exponent: ").append(getPublicExponent().toString(16)).append(nl);
        return buf.toString();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        try {
            this.algorithmIdentifier = AlgorithmIdentifier.getInstance(in.readObject());
        } catch (Exception e) {
            this.algorithmIdentifier = DEFAULT_ALGORITHM_IDENTIFIER;
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        if (!this.algorithmIdentifier.equals(DEFAULT_ALGORITHM_IDENTIFIER)) {
            out.writeObject(this.algorithmIdentifier.getEncoded());
        }
    }
}
