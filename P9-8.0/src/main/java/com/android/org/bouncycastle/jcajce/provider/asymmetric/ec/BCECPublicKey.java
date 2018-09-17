package com.android.org.bouncycastle.jcajce.provider.asymmetric.ec;

import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.DEROctetString;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import com.android.org.bouncycastle.asn1.x9.X962Parameters;
import com.android.org.bouncycastle.asn1.x9.X9ECPoint;
import com.android.org.bouncycastle.asn1.x9.X9IntegerConverter;
import com.android.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import com.android.org.bouncycastle.crypto.params.ECDomainParameters;
import com.android.org.bouncycastle.crypto.params.ECPublicKeyParameters;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.KeyUtil;
import com.android.org.bouncycastle.jcajce.provider.config.ProviderConfiguration;
import com.android.org.bouncycastle.jce.interfaces.ECPointEncoder;
import com.android.org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.util.Strings;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;

public class BCECPublicKey implements ECPublicKey, com.android.org.bouncycastle.jce.interfaces.ECPublicKey, ECPointEncoder {
    static final long serialVersionUID = 2422789860422731812L;
    private String algorithm = "EC";
    private transient ProviderConfiguration configuration;
    private transient ECPublicKeyParameters ecPublicKey;
    private transient ECParameterSpec ecSpec;
    private boolean withCompression;

    public BCECPublicKey(String algorithm, BCECPublicKey key) {
        this.algorithm = algorithm;
        this.ecPublicKey = key.ecPublicKey;
        this.ecSpec = key.ecSpec;
        this.withCompression = key.withCompression;
        this.configuration = key.configuration;
    }

    public BCECPublicKey(String algorithm, ECPublicKeySpec spec, ProviderConfiguration configuration) {
        this.algorithm = algorithm;
        this.ecSpec = spec.getParams();
        this.ecPublicKey = new ECPublicKeyParameters(EC5Util.convertPoint(this.ecSpec, spec.getW(), false), EC5Util.getDomainParameters(configuration, spec.getParams()));
        this.configuration = configuration;
    }

    public BCECPublicKey(String algorithm, com.android.org.bouncycastle.jce.spec.ECPublicKeySpec spec, ProviderConfiguration configuration) {
        this.algorithm = algorithm;
        if (spec.getParams() != null) {
            EllipticCurve ellipticCurve = EC5Util.convertCurve(spec.getParams().getCurve(), spec.getParams().getSeed());
            this.ecPublicKey = new ECPublicKeyParameters(spec.getQ(), ECUtil.getDomainParameters(configuration, spec.getParams()));
            this.ecSpec = EC5Util.convertSpec(ellipticCurve, spec.getParams());
        } else {
            this.ecPublicKey = new ECPublicKeyParameters(configuration.getEcImplicitlyCa().getCurve().createPoint(spec.getQ().getAffineXCoord().toBigInteger(), spec.getQ().getAffineYCoord().toBigInteger()), EC5Util.getDomainParameters(configuration, null));
            this.ecSpec = null;
        }
        this.configuration = configuration;
    }

    public BCECPublicKey(String algorithm, ECPublicKeyParameters params, ECParameterSpec spec, ProviderConfiguration configuration) {
        ECDomainParameters dp = params.getParameters();
        this.algorithm = algorithm;
        this.ecPublicKey = params;
        if (spec == null) {
            this.ecSpec = createSpec(EC5Util.convertCurve(dp.getCurve(), dp.getSeed()), dp);
        } else {
            this.ecSpec = spec;
        }
        this.configuration = configuration;
    }

    public BCECPublicKey(String algorithm, ECPublicKeyParameters params, com.android.org.bouncycastle.jce.spec.ECParameterSpec spec, ProviderConfiguration configuration) {
        ECDomainParameters dp = params.getParameters();
        this.algorithm = algorithm;
        if (spec == null) {
            this.ecSpec = createSpec(EC5Util.convertCurve(dp.getCurve(), dp.getSeed()), dp);
        } else {
            this.ecSpec = EC5Util.convertSpec(EC5Util.convertCurve(spec.getCurve(), spec.getSeed()), spec);
        }
        this.ecPublicKey = params;
        this.configuration = configuration;
    }

    public BCECPublicKey(String algorithm, ECPublicKeyParameters params, ProviderConfiguration configuration) {
        this.algorithm = algorithm;
        this.ecPublicKey = params;
        this.ecSpec = null;
        this.configuration = configuration;
    }

    public BCECPublicKey(ECPublicKey key, ProviderConfiguration configuration) {
        this.algorithm = key.getAlgorithm();
        this.ecSpec = key.getParams();
        this.ecPublicKey = new ECPublicKeyParameters(EC5Util.convertPoint(this.ecSpec, key.getW(), false), EC5Util.getDomainParameters(configuration, key.getParams()));
    }

    BCECPublicKey(String algorithm, SubjectPublicKeyInfo info, ProviderConfiguration configuration) {
        this.algorithm = algorithm;
        this.configuration = configuration;
        populateFromPubKeyInfo(info);
    }

    private ECParameterSpec createSpec(EllipticCurve ellipticCurve, ECDomainParameters dp) {
        return new ECParameterSpec(ellipticCurve, new ECPoint(dp.getG().getAffineXCoord().toBigInteger(), dp.getG().getAffineYCoord().toBigInteger()), dp.getN(), dp.getH().intValue());
    }

    private void populateFromPubKeyInfo(SubjectPublicKeyInfo info) {
        X962Parameters params = X962Parameters.getInstance(info.getAlgorithm().getParameters());
        ECCurve curve = EC5Util.getCurve(this.configuration, params);
        this.ecSpec = EC5Util.convertToSpec(params, curve);
        byte[] data = info.getPublicKeyData().getBytes();
        ASN1OctetString key = new DEROctetString(data);
        if (data[0] == (byte) 4 && data[1] == data.length - 2 && ((data[2] == (byte) 2 || data[2] == (byte) 3) && new X9IntegerConverter().getByteLength(curve) >= data.length - 3)) {
            try {
                key = (ASN1OctetString) ASN1Primitive.fromByteArray(data);
            } catch (IOException e) {
                throw new IllegalArgumentException("error recovering public key");
            }
        }
        this.ecPublicKey = new ECPublicKeyParameters(new X9ECPoint(curve, key).getPoint(), ECUtil.getDomainParameters(this.configuration, params));
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public String getFormat() {
        return "X.509";
    }

    public byte[] getEncoded() {
        return KeyUtil.getEncodedSubjectPublicKeyInfo(new SubjectPublicKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, ECUtils.getDomainParametersFromName(this.ecSpec, this.withCompression)), ASN1OctetString.getInstance(new X9ECPoint(this.ecPublicKey.getQ(), this.withCompression).toASN1Primitive()).getOctets()));
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

    public ECPoint getW() {
        com.android.org.bouncycastle.math.ec.ECPoint q = this.ecPublicKey.getQ();
        return new ECPoint(q.getAffineXCoord().toBigInteger(), q.getAffineYCoord().toBigInteger());
    }

    public com.android.org.bouncycastle.math.ec.ECPoint getQ() {
        com.android.org.bouncycastle.math.ec.ECPoint q = this.ecPublicKey.getQ();
        if (this.ecSpec == null) {
            return q.getDetachedPoint();
        }
        return q;
    }

    ECPublicKeyParameters engineGetKeyParameters() {
        return this.ecPublicKey;
    }

    com.android.org.bouncycastle.jce.spec.ECParameterSpec engineGetSpec() {
        if (this.ecSpec != null) {
            return EC5Util.convertSpec(this.ecSpec, this.withCompression);
        }
        return this.configuration.getEcImplicitlyCa();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        String nl = Strings.lineSeparator();
        com.android.org.bouncycastle.math.ec.ECPoint q = this.ecPublicKey.getQ();
        buf.append("EC Public Key").append(nl);
        buf.append("            X: ").append(q.getAffineXCoord().toBigInteger().toString(16)).append(nl);
        buf.append("            Y: ").append(q.getAffineYCoord().toBigInteger().toString(16)).append(nl);
        return buf.toString();
    }

    public void setPointFormat(String style) {
        this.withCompression = "UNCOMPRESSED".equalsIgnoreCase(style) ^ 1;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof BCECPublicKey)) {
            return false;
        }
        BCECPublicKey other = (BCECPublicKey) o;
        if (this.ecPublicKey.getQ().equals(other.ecPublicKey.getQ())) {
            z = engineGetSpec().equals(other.engineGetSpec());
        }
        return z;
    }

    public int hashCode() {
        return this.ecPublicKey.getQ().hashCode() ^ engineGetSpec().hashCode();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        byte[] enc = (byte[]) in.readObject();
        this.configuration = BouncyCastleProvider.CONFIGURATION;
        populateFromPubKeyInfo(SubjectPublicKeyInfo.getInstance(ASN1Primitive.fromByteArray(enc)));
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(getEncoded());
    }
}
