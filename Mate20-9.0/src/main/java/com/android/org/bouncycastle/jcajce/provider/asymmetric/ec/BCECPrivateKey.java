package com.android.org.bouncycastle.jcajce.provider.asymmetric.ec;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1Encoding;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.DERBitString;
import com.android.org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import com.android.org.bouncycastle.asn1.x9.X962Parameters;
import com.android.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import com.android.org.bouncycastle.crypto.params.ECDomainParameters;
import com.android.org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.PKCS12BagAttributeCarrierImpl;
import com.android.org.bouncycastle.jcajce.provider.config.ProviderConfiguration;
import com.android.org.bouncycastle.jce.interfaces.ECPointEncoder;
import com.android.org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import com.android.org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.android.org.bouncycastle.jce.spec.ECPrivateKeySpec;
import com.android.org.bouncycastle.util.Strings;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.util.Enumeration;

public class BCECPrivateKey implements ECPrivateKey, com.android.org.bouncycastle.jce.interfaces.ECPrivateKey, PKCS12BagAttributeCarrier, ECPointEncoder {
    static final long serialVersionUID = 994553197664784084L;
    private String algorithm = "EC";
    private transient PKCS12BagAttributeCarrierImpl attrCarrier = new PKCS12BagAttributeCarrierImpl();
    private transient ProviderConfiguration configuration;
    private transient BigInteger d;
    private transient ECParameterSpec ecSpec;
    private transient DERBitString publicKey;
    private boolean withCompression;

    protected BCECPrivateKey() {
    }

    public BCECPrivateKey(ECPrivateKey key, ProviderConfiguration configuration2) {
        this.d = key.getS();
        this.algorithm = key.getAlgorithm();
        this.ecSpec = key.getParams();
        this.configuration = configuration2;
    }

    public BCECPrivateKey(String algorithm2, ECPrivateKeySpec spec, ProviderConfiguration configuration2) {
        this.algorithm = algorithm2;
        this.d = spec.getD();
        if (spec.getParams() != null) {
            this.ecSpec = EC5Util.convertSpec(EC5Util.convertCurve(spec.getParams().getCurve(), spec.getParams().getSeed()), spec.getParams());
        } else {
            this.ecSpec = null;
        }
        this.configuration = configuration2;
    }

    public BCECPrivateKey(String algorithm2, java.security.spec.ECPrivateKeySpec spec, ProviderConfiguration configuration2) {
        this.algorithm = algorithm2;
        this.d = spec.getS();
        this.ecSpec = spec.getParams();
        this.configuration = configuration2;
    }

    public BCECPrivateKey(String algorithm2, BCECPrivateKey key) {
        this.algorithm = algorithm2;
        this.d = key.d;
        this.ecSpec = key.ecSpec;
        this.withCompression = key.withCompression;
        this.attrCarrier = key.attrCarrier;
        this.publicKey = key.publicKey;
        this.configuration = key.configuration;
    }

    public BCECPrivateKey(String algorithm2, ECPrivateKeyParameters params, BCECPublicKey pubKey, ECParameterSpec spec, ProviderConfiguration configuration2) {
        ECDomainParameters dp = params.getParameters();
        this.algorithm = algorithm2;
        this.d = params.getD();
        this.configuration = configuration2;
        if (spec == null) {
            this.ecSpec = new ECParameterSpec(EC5Util.convertCurve(dp.getCurve(), dp.getSeed()), new ECPoint(dp.getG().getAffineXCoord().toBigInteger(), dp.getG().getAffineYCoord().toBigInteger()), dp.getN(), dp.getH().intValue());
        } else {
            this.ecSpec = spec;
        }
        this.publicKey = getPublicKeyDetails(pubKey);
    }

    public BCECPrivateKey(String algorithm2, ECPrivateKeyParameters params, BCECPublicKey pubKey, com.android.org.bouncycastle.jce.spec.ECParameterSpec spec, ProviderConfiguration configuration2) {
        ECDomainParameters dp = params.getParameters();
        this.algorithm = algorithm2;
        this.d = params.getD();
        this.configuration = configuration2;
        if (spec == null) {
            this.ecSpec = new ECParameterSpec(EC5Util.convertCurve(dp.getCurve(), dp.getSeed()), new ECPoint(dp.getG().getAffineXCoord().toBigInteger(), dp.getG().getAffineYCoord().toBigInteger()), dp.getN(), dp.getH().intValue());
        } else {
            this.ecSpec = EC5Util.convertSpec(EC5Util.convertCurve(spec.getCurve(), spec.getSeed()), spec);
        }
        try {
            this.publicKey = getPublicKeyDetails(pubKey);
        } catch (Exception e) {
            this.publicKey = null;
        }
    }

    public BCECPrivateKey(String algorithm2, ECPrivateKeyParameters params, ProviderConfiguration configuration2) {
        this.algorithm = algorithm2;
        this.d = params.getD();
        this.ecSpec = null;
        this.configuration = configuration2;
    }

    BCECPrivateKey(String algorithm2, PrivateKeyInfo info, ProviderConfiguration configuration2) throws IOException {
        this.algorithm = algorithm2;
        this.configuration = configuration2;
        populateFromPrivKeyInfo(info);
    }

    private void populateFromPrivKeyInfo(PrivateKeyInfo info) throws IOException {
        X962Parameters params = X962Parameters.getInstance(info.getPrivateKeyAlgorithm().getParameters());
        this.ecSpec = EC5Util.convertToSpec(params, EC5Util.getCurve(this.configuration, params));
        ASN1Encodable privKey = info.parsePrivateKey();
        if (privKey instanceof ASN1Integer) {
            this.d = ASN1Integer.getInstance(privKey).getValue();
            return;
        }
        com.android.org.bouncycastle.asn1.sec.ECPrivateKey ec = com.android.org.bouncycastle.asn1.sec.ECPrivateKey.getInstance(privKey);
        this.d = ec.getKey();
        this.publicKey = ec.getPublicKey();
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public String getFormat() {
        return "PKCS#8";
    }

    public byte[] getEncoded() {
        int orderBitLength;
        com.android.org.bouncycastle.asn1.sec.ECPrivateKey keyStructure;
        X962Parameters params = ECUtils.getDomainParametersFromName(this.ecSpec, this.withCompression);
        if (this.ecSpec == null) {
            orderBitLength = ECUtil.getOrderBitLength(this.configuration, null, getS());
        } else {
            orderBitLength = ECUtil.getOrderBitLength(this.configuration, this.ecSpec.getOrder(), getS());
        }
        if (this.publicKey != null) {
            keyStructure = new com.android.org.bouncycastle.asn1.sec.ECPrivateKey(orderBitLength, getS(), this.publicKey, params);
        } else {
            keyStructure = new com.android.org.bouncycastle.asn1.sec.ECPrivateKey(orderBitLength, getS(), (ASN1Encodable) params);
        }
        try {
            return new PrivateKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, params), keyStructure).getEncoded(ASN1Encoding.DER);
        } catch (IOException e) {
            return null;
        }
    }

    public ECParameterSpec getParams() {
        return this.ecSpec;
    }

    public com.android.org.bouncycastle.jce.spec.ECParameterSpec getParameters() {
        if (this.ecSpec == null) {
            return null;
        }
        return EC5Util.convertSpec(this.ecSpec, this.withCompression);
    }

    /* access modifiers changed from: package-private */
    public com.android.org.bouncycastle.jce.spec.ECParameterSpec engineGetSpec() {
        if (this.ecSpec != null) {
            return EC5Util.convertSpec(this.ecSpec, this.withCompression);
        }
        return this.configuration.getEcImplicitlyCa();
    }

    public BigInteger getS() {
        return this.d;
    }

    public BigInteger getD() {
        return this.d;
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

    public void setPointFormat(String style) {
        this.withCompression = !"UNCOMPRESSED".equalsIgnoreCase(style);
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof BCECPrivateKey)) {
            return false;
        }
        BCECPrivateKey other = (BCECPrivateKey) o;
        if (getD().equals(other.getD()) && engineGetSpec().equals(other.engineGetSpec())) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return getD().hashCode() ^ engineGetSpec().hashCode();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        String nl = Strings.lineSeparator();
        buf.append("EC Private Key");
        buf.append(nl);
        buf.append("             S: ");
        buf.append(this.d.toString(16));
        buf.append(nl);
        return buf.toString();
    }

    private DERBitString getPublicKeyDetails(BCECPublicKey pub) {
        try {
            return SubjectPublicKeyInfo.getInstance(ASN1Primitive.fromByteArray(pub.getEncoded())).getPublicKeyData();
        } catch (IOException e) {
            return null;
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.configuration = BouncyCastleProvider.CONFIGURATION;
        populateFromPrivKeyInfo(PrivateKeyInfo.getInstance(ASN1Primitive.fromByteArray((byte[]) in.readObject())));
        this.attrCarrier = new PKCS12BagAttributeCarrierImpl();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(getEncoded());
    }
}
