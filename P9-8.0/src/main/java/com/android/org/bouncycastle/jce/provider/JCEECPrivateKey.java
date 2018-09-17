package com.android.org.bouncycastle.jce.provider;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1Encoding;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.DERBitString;
import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import com.android.org.bouncycastle.asn1.sec.ECPrivateKeyStructure;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import com.android.org.bouncycastle.asn1.x9.X962Parameters;
import com.android.org.bouncycastle.asn1.x9.X9ECParameters;
import com.android.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import com.android.org.bouncycastle.crypto.params.ECDomainParameters;
import com.android.org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.PKCS12BagAttributeCarrierImpl;
import com.android.org.bouncycastle.jce.interfaces.ECPointEncoder;
import com.android.org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import com.android.org.bouncycastle.jce.spec.ECNamedCurveSpec;
import com.android.org.bouncycastle.jce.spec.ECPrivateKeySpec;
import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.util.Strings;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.util.Enumeration;

public class JCEECPrivateKey implements ECPrivateKey, com.android.org.bouncycastle.jce.interfaces.ECPrivateKey, PKCS12BagAttributeCarrier, ECPointEncoder {
    private String algorithm = "EC";
    private PKCS12BagAttributeCarrierImpl attrCarrier = new PKCS12BagAttributeCarrierImpl();
    private BigInteger d;
    private ECParameterSpec ecSpec;
    private DERBitString publicKey;
    private boolean withCompression;

    protected JCEECPrivateKey() {
    }

    public JCEECPrivateKey(ECPrivateKey key) {
        this.d = key.getS();
        this.algorithm = key.getAlgorithm();
        this.ecSpec = key.getParams();
    }

    public JCEECPrivateKey(String algorithm, ECPrivateKeySpec spec) {
        this.algorithm = algorithm;
        this.d = spec.getD();
        if (spec.getParams() != null) {
            this.ecSpec = EC5Util.convertSpec(EC5Util.convertCurve(spec.getParams().getCurve(), spec.getParams().getSeed()), spec.getParams());
        } else {
            this.ecSpec = null;
        }
    }

    public JCEECPrivateKey(String algorithm, java.security.spec.ECPrivateKeySpec spec) {
        this.algorithm = algorithm;
        this.d = spec.getS();
        this.ecSpec = spec.getParams();
    }

    public JCEECPrivateKey(String algorithm, JCEECPrivateKey key) {
        this.algorithm = algorithm;
        this.d = key.d;
        this.ecSpec = key.ecSpec;
        this.withCompression = key.withCompression;
        this.attrCarrier = key.attrCarrier;
        this.publicKey = key.publicKey;
    }

    public JCEECPrivateKey(String algorithm, ECPrivateKeyParameters params, JCEECPublicKey pubKey, ECParameterSpec spec) {
        ECDomainParameters dp = params.getParameters();
        this.algorithm = algorithm;
        this.d = params.getD();
        if (spec == null) {
            this.ecSpec = new ECParameterSpec(EC5Util.convertCurve(dp.getCurve(), dp.getSeed()), new ECPoint(dp.getG().getAffineXCoord().toBigInteger(), dp.getG().getAffineYCoord().toBigInteger()), dp.getN(), dp.getH().intValue());
        } else {
            this.ecSpec = spec;
        }
        this.publicKey = getPublicKeyDetails(pubKey);
    }

    public JCEECPrivateKey(String algorithm, ECPrivateKeyParameters params, JCEECPublicKey pubKey, com.android.org.bouncycastle.jce.spec.ECParameterSpec spec) {
        ECDomainParameters dp = params.getParameters();
        this.algorithm = algorithm;
        this.d = params.getD();
        if (spec == null) {
            this.ecSpec = new ECParameterSpec(EC5Util.convertCurve(dp.getCurve(), dp.getSeed()), new ECPoint(dp.getG().getAffineXCoord().toBigInteger(), dp.getG().getAffineYCoord().toBigInteger()), dp.getN(), dp.getH().intValue());
        } else {
            this.ecSpec = new ECParameterSpec(EC5Util.convertCurve(spec.getCurve(), spec.getSeed()), new ECPoint(spec.getG().getAffineXCoord().toBigInteger(), spec.getG().getAffineYCoord().toBigInteger()), spec.getN(), spec.getH().intValue());
        }
        this.publicKey = getPublicKeyDetails(pubKey);
    }

    public JCEECPrivateKey(String algorithm, ECPrivateKeyParameters params) {
        this.algorithm = algorithm;
        this.d = params.getD();
        this.ecSpec = null;
    }

    JCEECPrivateKey(PrivateKeyInfo info) throws IOException {
        populateFromPrivKeyInfo(info);
    }

    private void populateFromPrivKeyInfo(PrivateKeyInfo info) throws IOException {
        X962Parameters params = new X962Parameters((ASN1Primitive) info.getPrivateKeyAlgorithm().getParameters());
        X9ECParameters ecP;
        if (params.isNamedCurve()) {
            ASN1ObjectIdentifier oid = ASN1ObjectIdentifier.getInstance(params.getParameters());
            ecP = ECUtil.getNamedCurveByOid(oid);
            this.ecSpec = new ECNamedCurveSpec(ECUtil.getCurveName(oid), EC5Util.convertCurve(ecP.getCurve(), ecP.getSeed()), new ECPoint(ecP.getG().getAffineXCoord().toBigInteger(), ecP.getG().getAffineYCoord().toBigInteger()), ecP.getN(), ecP.getH());
        } else if (params.isImplicitlyCA()) {
            this.ecSpec = null;
        } else {
            ecP = X9ECParameters.getInstance(params.getParameters());
            this.ecSpec = new ECParameterSpec(EC5Util.convertCurve(ecP.getCurve(), ecP.getSeed()), new ECPoint(ecP.getG().getAffineXCoord().toBigInteger(), ecP.getG().getAffineYCoord().toBigInteger()), ecP.getN(), ecP.getH().intValue());
        }
        ASN1Encodable privKey = info.parsePrivateKey();
        if (privKey instanceof ASN1Integer) {
            this.d = ASN1Integer.getInstance(privKey).getValue();
            return;
        }
        ECPrivateKeyStructure ec = new ECPrivateKeyStructure((ASN1Sequence) privKey);
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
        X962Parameters params;
        ECPrivateKeyStructure keyStructure;
        if (this.ecSpec instanceof ECNamedCurveSpec) {
            ASN1ObjectIdentifier curveOid = ECUtil.getNamedCurveOid(((ECNamedCurveSpec) this.ecSpec).getName());
            if (curveOid == null) {
                curveOid = new ASN1ObjectIdentifier(((ECNamedCurveSpec) this.ecSpec).getName());
            }
            params = new X962Parameters(curveOid);
        } else if (this.ecSpec == null) {
            params = new X962Parameters(DERNull.INSTANCE);
        } else {
            ECCurve curve = EC5Util.convertCurve(this.ecSpec.getCurve());
            params = new X962Parameters(new X9ECParameters(curve, EC5Util.convertPoint(curve, this.ecSpec.getGenerator(), this.withCompression), this.ecSpec.getOrder(), BigInteger.valueOf((long) this.ecSpec.getCofactor()), this.ecSpec.getCurve().getSeed()));
        }
        if (this.publicKey != null) {
            keyStructure = new ECPrivateKeyStructure(getS(), this.publicKey, params);
        } else {
            keyStructure = new ECPrivateKeyStructure(getS(), params);
        }
        try {
            return new PrivateKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, params.toASN1Primitive()), keyStructure.toASN1Primitive()).getEncoded(ASN1Encoding.DER);
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

    com.android.org.bouncycastle.jce.spec.ECParameterSpec engineGetSpec() {
        if (this.ecSpec != null) {
            return EC5Util.convertSpec(this.ecSpec, this.withCompression);
        }
        return BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa();
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
        this.withCompression = "UNCOMPRESSED".equalsIgnoreCase(style) ^ 1;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof JCEECPrivateKey)) {
            return false;
        }
        JCEECPrivateKey other = (JCEECPrivateKey) o;
        if (getD().equals(other.getD())) {
            z = engineGetSpec().equals(other.engineGetSpec());
        }
        return z;
    }

    public int hashCode() {
        return getD().hashCode() ^ engineGetSpec().hashCode();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        String nl = Strings.lineSeparator();
        buf.append("EC Private Key").append(nl);
        buf.append("             S: ").append(this.d.toString(16)).append(nl);
        return buf.toString();
    }

    private DERBitString getPublicKeyDetails(JCEECPublicKey pub) {
        try {
            return SubjectPublicKeyInfo.getInstance(ASN1Primitive.fromByteArray(pub.getEncoded())).getPublicKeyData();
        } catch (IOException e) {
            return null;
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        populateFromPrivKeyInfo(PrivateKeyInfo.getInstance(ASN1Primitive.fromByteArray((byte[]) in.readObject())));
        this.algorithm = (String) in.readObject();
        this.withCompression = in.readBoolean();
        this.attrCarrier = new PKCS12BagAttributeCarrierImpl();
        this.attrCarrier.readObject(in);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(getEncoded());
        out.writeObject(this.algorithm);
        out.writeBoolean(this.withCompression);
        this.attrCarrier.writeObject(out);
    }
}
