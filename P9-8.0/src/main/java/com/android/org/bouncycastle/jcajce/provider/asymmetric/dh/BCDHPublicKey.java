package com.android.org.bouncycastle.jcajce.provider.asymmetric.dh;

import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.pkcs.DHParameter;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import com.android.org.bouncycastle.asn1.x9.DomainParameters;
import com.android.org.bouncycastle.asn1.x9.ValidationParams;
import com.android.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import com.android.org.bouncycastle.crypto.params.DHParameters;
import com.android.org.bouncycastle.crypto.params.DHPublicKeyParameters;
import com.android.org.bouncycastle.crypto.params.DHValidationParameters;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.KeyUtil;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;

public class BCDHPublicKey implements DHPublicKey {
    static final long serialVersionUID = -216691575254424324L;
    private transient DHPublicKeyParameters dhPublicKey;
    private transient DHParameterSpec dhSpec;
    private transient SubjectPublicKeyInfo info;
    private BigInteger y;

    BCDHPublicKey(DHPublicKeySpec spec) {
        this.y = spec.getY();
        this.dhSpec = new DHParameterSpec(spec.getP(), spec.getG());
        this.dhPublicKey = new DHPublicKeyParameters(this.y, new DHParameters(spec.getP(), spec.getG()));
    }

    BCDHPublicKey(DHPublicKey key) {
        this.y = key.getY();
        this.dhSpec = key.getParams();
        this.dhPublicKey = new DHPublicKeyParameters(this.y, new DHParameters(this.dhSpec.getP(), this.dhSpec.getG()));
    }

    BCDHPublicKey(DHPublicKeyParameters params) {
        this.y = params.getY();
        this.dhSpec = new DHParameterSpec(params.getParameters().getP(), params.getParameters().getG(), params.getParameters().getL());
        this.dhPublicKey = params;
    }

    BCDHPublicKey(BigInteger y, DHParameterSpec dhSpec) {
        this.y = y;
        this.dhSpec = dhSpec;
        this.dhPublicKey = new DHPublicKeyParameters(y, new DHParameters(dhSpec.getP(), dhSpec.getG()));
    }

    public BCDHPublicKey(SubjectPublicKeyInfo info) {
        this.info = info;
        try {
            this.y = ((ASN1Integer) info.parsePublicKey()).getValue();
            ASN1Sequence seq = ASN1Sequence.getInstance(info.getAlgorithm().getParameters());
            ASN1ObjectIdentifier id = info.getAlgorithm().getAlgorithm();
            if (id.equals(PKCSObjectIdentifiers.dhKeyAgreement) || isPKCSParam(seq)) {
                DHParameter params = DHParameter.getInstance(seq);
                if (params.getL() != null) {
                    this.dhSpec = new DHParameterSpec(params.getP(), params.getG(), params.getL().intValue());
                } else {
                    this.dhSpec = new DHParameterSpec(params.getP(), params.getG());
                }
                this.dhPublicKey = new DHPublicKeyParameters(this.y, new DHParameters(this.dhSpec.getP(), this.dhSpec.getG()));
            } else if (id.equals(X9ObjectIdentifiers.dhpublicnumber)) {
                DomainParameters params2 = DomainParameters.getInstance(seq);
                this.dhSpec = new DHParameterSpec(params2.getP(), params2.getG());
                ValidationParams validationParams = params2.getValidationParams();
                if (validationParams != null) {
                    BigInteger bigInteger = this.y;
                    BigInteger bigInteger2 = bigInteger;
                    this.dhPublicKey = new DHPublicKeyParameters(bigInteger2, new DHParameters(params2.getP(), params2.getG(), params2.getQ(), params2.getJ(), new DHValidationParameters(validationParams.getSeed(), validationParams.getPgenCounter().intValue())));
                    return;
                }
                this.dhPublicKey = new DHPublicKeyParameters(this.y, new DHParameters(params2.getP(), params2.getG(), params2.getQ(), params2.getJ(), null));
            } else {
                throw new IllegalArgumentException("unknown algorithm type: " + id);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("invalid info structure in DH public key");
        }
    }

    public String getAlgorithm() {
        return "DH";
    }

    public String getFormat() {
        return "X.509";
    }

    public byte[] getEncoded() {
        if (this.info != null) {
            return KeyUtil.getEncodedSubjectPublicKeyInfo(this.info);
        }
        return KeyUtil.getEncodedSubjectPublicKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.dhKeyAgreement, new DHParameter(this.dhSpec.getP(), this.dhSpec.getG(), this.dhSpec.getL()).toASN1Primitive()), new ASN1Integer(this.y));
    }

    public DHParameterSpec getParams() {
        return this.dhSpec;
    }

    public BigInteger getY() {
        return this.y;
    }

    public DHPublicKeyParameters engineGetKeyParameters() {
        return this.dhPublicKey;
    }

    private boolean isPKCSParam(ASN1Sequence seq) {
        if (seq.size() == 2) {
            return true;
        }
        if (seq.size() > 3) {
            return false;
        }
        return ASN1Integer.getInstance(seq.getObjectAt(2)).getValue().compareTo(BigInteger.valueOf((long) ASN1Integer.getInstance(seq.getObjectAt(0)).getValue().bitLength())) <= 0;
    }

    public int hashCode() {
        return ((getY().hashCode() ^ getParams().getG().hashCode()) ^ getParams().getP().hashCode()) ^ getParams().getL();
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof DHPublicKey)) {
            return false;
        }
        DHPublicKey other = (DHPublicKey) o;
        if (getY().equals(other.getY()) && getParams().getG().equals(other.getParams().getG()) && getParams().getP().equals(other.getParams().getP()) && getParams().getL() == other.getParams().getL()) {
            z = true;
        }
        return z;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.dhSpec = new DHParameterSpec((BigInteger) in.readObject(), (BigInteger) in.readObject(), in.readInt());
        this.info = null;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(this.dhSpec.getP());
        out.writeObject(this.dhSpec.getG());
        out.writeInt(this.dhSpec.getL());
    }
}
