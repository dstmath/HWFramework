package com.android.org.bouncycastle.jcajce.provider.asymmetric.dsa;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.DSAParameter;
import com.android.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import com.android.org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.KeyUtil;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.PKCS12BagAttributeCarrierImpl;
import com.android.org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.spec.DSAParameterSpec;
import java.security.spec.DSAPrivateKeySpec;
import java.util.Enumeration;

public class BCDSAPrivateKey implements DSAPrivateKey, PKCS12BagAttributeCarrier {
    private static final long serialVersionUID = -4677259546958385734L;
    private transient PKCS12BagAttributeCarrierImpl attrCarrier = new PKCS12BagAttributeCarrierImpl();
    private transient DSAParams dsaSpec;
    private BigInteger x;

    protected BCDSAPrivateKey() {
    }

    BCDSAPrivateKey(DSAPrivateKey key) {
        this.x = key.getX();
        this.dsaSpec = key.getParams();
    }

    BCDSAPrivateKey(DSAPrivateKeySpec spec) {
        this.x = spec.getX();
        this.dsaSpec = new DSAParameterSpec(spec.getP(), spec.getQ(), spec.getG());
    }

    public BCDSAPrivateKey(PrivateKeyInfo info) throws IOException {
        DSAParameter params = DSAParameter.getInstance(info.getPrivateKeyAlgorithm().getParameters());
        this.x = ((ASN1Integer) info.parsePrivateKey()).getValue();
        this.dsaSpec = new DSAParameterSpec(params.getP(), params.getQ(), params.getG());
    }

    BCDSAPrivateKey(DSAPrivateKeyParameters params) {
        this.x = params.getX();
        this.dsaSpec = new DSAParameterSpec(params.getParameters().getP(), params.getParameters().getQ(), params.getParameters().getG());
    }

    public String getAlgorithm() {
        return "DSA";
    }

    public String getFormat() {
        return "PKCS#8";
    }

    public byte[] getEncoded() {
        return KeyUtil.getEncodedPrivateKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa, new DSAParameter(this.dsaSpec.getP(), this.dsaSpec.getQ(), this.dsaSpec.getG()).toASN1Primitive()), new ASN1Integer(getX()));
    }

    public DSAParams getParams() {
        return this.dsaSpec;
    }

    public BigInteger getX() {
        return this.x;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof DSAPrivateKey)) {
            return false;
        }
        DSAPrivateKey other = (DSAPrivateKey) o;
        if (getX().equals(other.getX()) && getParams().getG().equals(other.getParams().getG()) && getParams().getP().equals(other.getParams().getP())) {
            z = getParams().getQ().equals(other.getParams().getQ());
        }
        return z;
    }

    public int hashCode() {
        return ((getX().hashCode() ^ getParams().getG().hashCode()) ^ getParams().getP().hashCode()) ^ getParams().getQ().hashCode();
    }

    public void setBagAttribute(ASN1ObjectIdentifier oid, ASN1Encodable attribute) {
        this.attrCarrier.setBagAttribute(oid, attribute);
    }

    public ASN1Encodable getBagAttribute(ASN1ObjectIdentifier oid) {
        return this.attrCarrier.getBagAttribute(oid);
    }

    public Enumeration getBagAttributeKeys() {
        return this.attrCarrier.getBagAttributeKeys();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.dsaSpec = new DSAParameterSpec((BigInteger) in.readObject(), (BigInteger) in.readObject(), (BigInteger) in.readObject());
        this.attrCarrier = new PKCS12BagAttributeCarrierImpl();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(this.dsaSpec.getP());
        out.writeObject(this.dsaSpec.getQ());
        out.writeObject(this.dsaSpec.getG());
    }
}
