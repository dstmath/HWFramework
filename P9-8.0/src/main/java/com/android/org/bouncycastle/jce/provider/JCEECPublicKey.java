package com.android.org.bouncycastle.jce.provider;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.DEROctetString;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import com.android.org.bouncycastle.asn1.x9.X962Parameters;
import com.android.org.bouncycastle.asn1.x9.X9ECParameters;
import com.android.org.bouncycastle.asn1.x9.X9ECPoint;
import com.android.org.bouncycastle.asn1.x9.X9IntegerConverter;
import com.android.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import com.android.org.bouncycastle.crypto.params.ECDomainParameters;
import com.android.org.bouncycastle.crypto.params.ECPublicKeyParameters;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.KeyUtil;
import com.android.org.bouncycastle.jce.interfaces.ECPointEncoder;
import com.android.org.bouncycastle.jce.spec.ECNamedCurveSpec;
import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.math.ec.ECPoint;
import com.android.org.bouncycastle.util.Strings;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;

public class JCEECPublicKey implements ECPublicKey, com.android.org.bouncycastle.jce.interfaces.ECPublicKey, ECPointEncoder {
    private String algorithm = "EC";
    private ECParameterSpec ecSpec;
    private ECPoint q;
    private boolean withCompression;

    public JCEECPublicKey(String algorithm, JCEECPublicKey key) {
        this.algorithm = algorithm;
        this.q = key.q;
        this.ecSpec = key.ecSpec;
        this.withCompression = key.withCompression;
    }

    public JCEECPublicKey(String algorithm, ECPublicKeySpec spec) {
        this.algorithm = algorithm;
        this.ecSpec = spec.getParams();
        this.q = EC5Util.convertPoint(this.ecSpec, spec.getW(), false);
    }

    public JCEECPublicKey(String algorithm, com.android.org.bouncycastle.jce.spec.ECPublicKeySpec spec) {
        this.algorithm = algorithm;
        this.q = spec.getQ();
        if (spec.getParams() != null) {
            this.ecSpec = EC5Util.convertSpec(EC5Util.convertCurve(spec.getParams().getCurve(), spec.getParams().getSeed()), spec.getParams());
            return;
        }
        if (this.q.getCurve() == null) {
            this.q = BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa().getCurve().createPoint(this.q.getAffineXCoord().toBigInteger(), this.q.getAffineYCoord().toBigInteger(), false);
        }
        this.ecSpec = null;
    }

    public JCEECPublicKey(String algorithm, ECPublicKeyParameters params, ECParameterSpec spec) {
        ECDomainParameters dp = params.getParameters();
        this.algorithm = algorithm;
        this.q = params.getQ();
        if (spec == null) {
            this.ecSpec = createSpec(EC5Util.convertCurve(dp.getCurve(), dp.getSeed()), dp);
        } else {
            this.ecSpec = spec;
        }
    }

    public JCEECPublicKey(String algorithm, ECPublicKeyParameters params, com.android.org.bouncycastle.jce.spec.ECParameterSpec spec) {
        ECDomainParameters dp = params.getParameters();
        this.algorithm = algorithm;
        this.q = params.getQ();
        if (spec == null) {
            this.ecSpec = createSpec(EC5Util.convertCurve(dp.getCurve(), dp.getSeed()), dp);
        } else {
            this.ecSpec = EC5Util.convertSpec(EC5Util.convertCurve(spec.getCurve(), spec.getSeed()), spec);
        }
    }

    public JCEECPublicKey(String algorithm, ECPublicKeyParameters params) {
        this.algorithm = algorithm;
        this.q = params.getQ();
        this.ecSpec = null;
    }

    private ECParameterSpec createSpec(EllipticCurve ellipticCurve, ECDomainParameters dp) {
        return new ECParameterSpec(ellipticCurve, new java.security.spec.ECPoint(dp.getG().getAffineXCoord().toBigInteger(), dp.getG().getAffineYCoord().toBigInteger()), dp.getN(), dp.getH().intValue());
    }

    public JCEECPublicKey(ECPublicKey key) {
        this.algorithm = key.getAlgorithm();
        this.ecSpec = key.getParams();
        this.q = EC5Util.convertPoint(this.ecSpec, key.getW(), false);
    }

    JCEECPublicKey(SubjectPublicKeyInfo info) {
        populateFromPubKeyInfo(info);
    }

    private void populateFromPubKeyInfo(SubjectPublicKeyInfo info) {
        ECCurve curve;
        X962Parameters params = new X962Parameters((ASN1Primitive) info.getAlgorithmId().getParameters());
        X9ECParameters ecP;
        if (params.isNamedCurve()) {
            ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) params.getParameters();
            ecP = ECUtil.getNamedCurveByOid(oid);
            curve = ecP.getCurve();
            this.ecSpec = new ECNamedCurveSpec(ECUtil.getCurveName(oid), EC5Util.convertCurve(curve, ecP.getSeed()), new java.security.spec.ECPoint(ecP.getG().getAffineXCoord().toBigInteger(), ecP.getG().getAffineYCoord().toBigInteger()), ecP.getN(), ecP.getH());
        } else if (params.isImplicitlyCA()) {
            this.ecSpec = null;
            curve = BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa().getCurve();
        } else {
            ecP = X9ECParameters.getInstance(params.getParameters());
            curve = ecP.getCurve();
            this.ecSpec = new ECParameterSpec(EC5Util.convertCurve(curve, ecP.getSeed()), new java.security.spec.ECPoint(ecP.getG().getAffineXCoord().toBigInteger(), ecP.getG().getAffineYCoord().toBigInteger()), ecP.getN(), ecP.getH().intValue());
        }
        byte[] data = info.getPublicKeyData().getBytes();
        ASN1OctetString key = new DEROctetString(data);
        if (data[0] == (byte) 4 && data[1] == data.length - 2 && ((data[2] == (byte) 2 || data[2] == (byte) 3) && new X9IntegerConverter().getByteLength(curve) >= data.length - 3)) {
            try {
                key = (ASN1OctetString) ASN1Primitive.fromByteArray(data);
            } catch (IOException e) {
                throw new IllegalArgumentException("error recovering public key");
            }
        }
        this.q = new X9ECPoint(curve, key).getPoint();
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public String getFormat() {
        return "X.509";
    }

    public byte[] getEncoded() {
        ASN1Encodable params;
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
        return KeyUtil.getEncodedSubjectPublicKeyInfo(new SubjectPublicKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, params), ((ASN1OctetString) new X9ECPoint(engineGetQ().getCurve().createPoint(getQ().getAffineXCoord().toBigInteger(), getQ().getAffineYCoord().toBigInteger(), this.withCompression)).toASN1Primitive()).getOctets()));
    }

    private void extractBytes(byte[] encKey, int offSet, BigInteger bI) {
        byte[] val = bI.toByteArray();
        if (val.length < 32) {
            byte[] tmp = new byte[32];
            System.arraycopy(val, 0, tmp, tmp.length - val.length, val.length);
            val = tmp;
        }
        for (int i = 0; i != 32; i++) {
            encKey[offSet + i] = val[(val.length - 1) - i];
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

    public java.security.spec.ECPoint getW() {
        return new java.security.spec.ECPoint(this.q.getAffineXCoord().toBigInteger(), this.q.getAffineYCoord().toBigInteger());
    }

    public ECPoint getQ() {
        if (this.ecSpec == null) {
            return this.q.getDetachedPoint();
        }
        return this.q;
    }

    public ECPoint engineGetQ() {
        return this.q;
    }

    com.android.org.bouncycastle.jce.spec.ECParameterSpec engineGetSpec() {
        if (this.ecSpec != null) {
            return EC5Util.convertSpec(this.ecSpec, this.withCompression);
        }
        return BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        String nl = Strings.lineSeparator();
        buf.append("EC Public Key").append(nl);
        buf.append("            X: ").append(this.q.getAffineXCoord().toBigInteger().toString(16)).append(nl);
        buf.append("            Y: ").append(this.q.getAffineYCoord().toBigInteger().toString(16)).append(nl);
        return buf.toString();
    }

    public void setPointFormat(String style) {
        this.withCompression = "UNCOMPRESSED".equalsIgnoreCase(style) ^ 1;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof JCEECPublicKey)) {
            return false;
        }
        JCEECPublicKey other = (JCEECPublicKey) o;
        if (engineGetQ().equals(other.engineGetQ())) {
            z = engineGetSpec().equals(other.engineGetSpec());
        }
        return z;
    }

    public int hashCode() {
        return engineGetQ().hashCode() ^ engineGetSpec().hashCode();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        populateFromPubKeyInfo(SubjectPublicKeyInfo.getInstance(ASN1Primitive.fromByteArray((byte[]) in.readObject())));
        this.algorithm = (String) in.readObject();
        this.withCompression = in.readBoolean();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(getEncoded());
        out.writeObject(this.algorithm);
        out.writeBoolean(this.withCompression);
    }
}
