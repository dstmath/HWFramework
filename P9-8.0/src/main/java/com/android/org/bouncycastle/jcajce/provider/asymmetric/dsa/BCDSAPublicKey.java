package com.android.org.bouncycastle.jcajce.provider.asymmetric.dsa;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.DSAParameter;
import com.android.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import com.android.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import com.android.org.bouncycastle.crypto.params.DSAPublicKeyParameters;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.KeyUtil;
import com.android.org.bouncycastle.util.Strings;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAParameterSpec;
import java.security.spec.DSAPublicKeySpec;

public class BCDSAPublicKey implements DSAPublicKey {
    private static BigInteger ZERO = BigInteger.valueOf(0);
    private static final long serialVersionUID = 1752452449903495175L;
    private transient DSAParams dsaSpec;
    private transient DSAPublicKeyParameters lwKeyParams;
    private BigInteger y;

    BCDSAPublicKey(DSAPublicKeySpec spec) {
        this.y = spec.getY();
        this.dsaSpec = new DSAParameterSpec(spec.getP(), spec.getQ(), spec.getG());
        this.lwKeyParams = new DSAPublicKeyParameters(this.y, DSAUtil.toDSAParameters(this.dsaSpec));
    }

    BCDSAPublicKey(DSAPublicKey key) {
        this.y = key.getY();
        this.dsaSpec = key.getParams();
        this.lwKeyParams = new DSAPublicKeyParameters(this.y, DSAUtil.toDSAParameters(this.dsaSpec));
    }

    BCDSAPublicKey(DSAPublicKeyParameters params) {
        this.y = params.getY();
        if (params != null) {
            this.dsaSpec = new DSAParameterSpec(params.getParameters().getP(), params.getParameters().getQ(), params.getParameters().getG());
        } else {
            this.dsaSpec = null;
        }
        this.lwKeyParams = params;
    }

    public BCDSAPublicKey(SubjectPublicKeyInfo info) {
        try {
            this.y = ((ASN1Integer) info.parsePublicKey()).getValue();
            if (isNotNull(info.getAlgorithm().getParameters())) {
                DSAParameter params = DSAParameter.getInstance(info.getAlgorithm().getParameters());
                this.dsaSpec = new DSAParameterSpec(params.getP(), params.getQ(), params.getG());
            } else {
                this.dsaSpec = null;
            }
            this.lwKeyParams = new DSAPublicKeyParameters(this.y, DSAUtil.toDSAParameters(this.dsaSpec));
        } catch (IOException e) {
            throw new IllegalArgumentException("invalid info structure in DSA public key");
        }
    }

    private boolean isNotNull(ASN1Encodable parameters) {
        return parameters != null ? DERNull.INSTANCE.equals(parameters.toASN1Primitive()) ^ 1 : false;
    }

    public String getAlgorithm() {
        return "DSA";
    }

    public String getFormat() {
        return "X.509";
    }

    DSAPublicKeyParameters engineGetKeyParameters() {
        return this.lwKeyParams;
    }

    public byte[] getEncoded() {
        if (this.dsaSpec == null) {
            return KeyUtil.getEncodedSubjectPublicKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa), new ASN1Integer(this.y));
        }
        return KeyUtil.getEncodedSubjectPublicKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa, new DSAParameter(this.dsaSpec.getP(), this.dsaSpec.getQ(), this.dsaSpec.getG()).toASN1Primitive()), new ASN1Integer(this.y));
    }

    public DSAParams getParams() {
        return this.dsaSpec;
    }

    public BigInteger getY() {
        return this.y;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        String nl = Strings.lineSeparator();
        buf.append("DSA Public Key").append(nl);
        buf.append("            y: ").append(getY().toString(16)).append(nl);
        return buf.toString();
    }

    public int hashCode() {
        if (this.dsaSpec != null) {
            return ((getY().hashCode() ^ getParams().getG().hashCode()) ^ getParams().getP().hashCode()) ^ getParams().getQ().hashCode();
        }
        return getY().hashCode();
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof DSAPublicKey)) {
            return false;
        }
        DSAPublicKey other = (DSAPublicKey) o;
        if (this.dsaSpec != null) {
            if (getY().equals(other.getY()) && other.getParams() != null && getParams().getG().equals(other.getParams().getG()) && getParams().getP().equals(other.getParams().getP())) {
                z = getParams().getQ().equals(other.getParams().getQ());
            }
            return z;
        }
        if (getY().equals(other.getY()) && other.getParams() == null) {
            z = true;
        }
        return z;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        BigInteger p = (BigInteger) in.readObject();
        if (p.equals(ZERO)) {
            this.dsaSpec = null;
        } else {
            this.dsaSpec = new DSAParameterSpec(p, (BigInteger) in.readObject(), (BigInteger) in.readObject());
        }
        this.lwKeyParams = new DSAPublicKeyParameters(this.y, DSAUtil.toDSAParameters(this.dsaSpec));
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        if (this.dsaSpec == null) {
            out.writeObject(ZERO);
            return;
        }
        out.writeObject(this.dsaSpec.getP());
        out.writeObject(this.dsaSpec.getQ());
        out.writeObject(this.dsaSpec.getG());
    }
}
