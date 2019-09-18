package org.bouncycastle.jcajce.provider.asymmetric.dh;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.DHParameter;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.DomainParameters;
import org.bouncycastle.asn1.x9.ValidationParams;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.crypto.params.DHPublicKeyParameters;
import org.bouncycastle.crypto.params.DHValidationParameters;
import org.bouncycastle.jcajce.provider.asymmetric.util.KeyUtil;
import org.bouncycastle.jcajce.spec.DHDomainParameterSpec;

public class BCDHPublicKey implements DHPublicKey {
    static final long serialVersionUID = -216691575254424324L;
    private transient DHPublicKeyParameters dhPublicKey;
    private transient DHParameterSpec dhSpec;
    private transient SubjectPublicKeyInfo info;
    private BigInteger y;

    BCDHPublicKey(BigInteger bigInteger, DHParameterSpec dHParameterSpec) {
        this.y = bigInteger;
        this.dhSpec = dHParameterSpec;
        this.dhPublicKey = dHParameterSpec instanceof DHDomainParameterSpec ? new DHPublicKeyParameters(bigInteger, ((DHDomainParameterSpec) dHParameterSpec).getDomainParameters()) : new DHPublicKeyParameters(bigInteger, new DHParameters(dHParameterSpec.getP(), dHParameterSpec.getG()));
    }

    BCDHPublicKey(DHPublicKey dHPublicKey) {
        this.y = dHPublicKey.getY();
        this.dhSpec = dHPublicKey.getParams();
        this.dhPublicKey = new DHPublicKeyParameters(this.y, new DHParameters(this.dhSpec.getP(), this.dhSpec.getG()));
    }

    BCDHPublicKey(DHPublicKeySpec dHPublicKeySpec) {
        this.y = dHPublicKeySpec.getY();
        this.dhSpec = new DHParameterSpec(dHPublicKeySpec.getP(), dHPublicKeySpec.getG());
        this.dhPublicKey = new DHPublicKeyParameters(this.y, new DHParameters(dHPublicKeySpec.getP(), dHPublicKeySpec.getG()));
    }

    public BCDHPublicKey(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        this.info = subjectPublicKeyInfo;
        try {
            this.y = ((ASN1Integer) subjectPublicKeyInfo.parsePublicKey()).getValue();
            ASN1Sequence instance = ASN1Sequence.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters());
            ASN1ObjectIdentifier algorithm = subjectPublicKeyInfo.getAlgorithm().getAlgorithm();
            if (algorithm.equals(PKCSObjectIdentifiers.dhKeyAgreement) || isPKCSParam(instance)) {
                DHParameter instance2 = DHParameter.getInstance(instance);
                this.dhSpec = instance2.getL() != null ? new DHParameterSpec(instance2.getP(), instance2.getG(), instance2.getL().intValue()) : new DHParameterSpec(instance2.getP(), instance2.getG());
                this.dhPublicKey = new DHPublicKeyParameters(this.y, new DHParameters(this.dhSpec.getP(), this.dhSpec.getG()));
            } else if (algorithm.equals(X9ObjectIdentifiers.dhpublicnumber)) {
                DomainParameters instance3 = DomainParameters.getInstance(instance);
                ValidationParams validationParams = instance3.getValidationParams();
                if (validationParams != null) {
                    BigInteger bigInteger = this.y;
                    DHParameters dHParameters = new DHParameters(instance3.getP(), instance3.getG(), instance3.getQ(), instance3.getJ(), new DHValidationParameters(validationParams.getSeed(), validationParams.getPgenCounter().intValue()));
                    this.dhPublicKey = new DHPublicKeyParameters(bigInteger, dHParameters);
                } else {
                    BigInteger bigInteger2 = this.y;
                    DHParameters dHParameters2 = new DHParameters(instance3.getP(), instance3.getG(), instance3.getQ(), instance3.getJ(), (DHValidationParameters) null);
                    this.dhPublicKey = new DHPublicKeyParameters(bigInteger2, dHParameters2);
                }
                this.dhSpec = new DHDomainParameterSpec(this.dhPublicKey.getParameters());
            } else {
                throw new IllegalArgumentException("unknown algorithm type: " + algorithm);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("invalid info structure in DH public key");
        }
    }

    BCDHPublicKey(DHPublicKeyParameters dHPublicKeyParameters) {
        this.y = dHPublicKeyParameters.getY();
        this.dhSpec = new DHDomainParameterSpec(dHPublicKeyParameters.getParameters());
        this.dhPublicKey = dHPublicKeyParameters;
    }

    private boolean isPKCSParam(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() == 2) {
            return true;
        }
        if (aSN1Sequence.size() > 3) {
            return false;
        }
        return ASN1Integer.getInstance(aSN1Sequence.getObjectAt(2)).getValue().compareTo(BigInteger.valueOf((long) ASN1Integer.getInstance(aSN1Sequence.getObjectAt(0)).getValue().bitLength())) <= 0;
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        this.dhSpec = new DHParameterSpec((BigInteger) objectInputStream.readObject(), (BigInteger) objectInputStream.readObject(), objectInputStream.readInt());
        this.info = null;
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeObject(this.dhSpec.getP());
        objectOutputStream.writeObject(this.dhSpec.getG());
        objectOutputStream.writeInt(this.dhSpec.getL());
    }

    public DHPublicKeyParameters engineGetKeyParameters() {
        return this.dhPublicKey;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof DHPublicKey)) {
            return false;
        }
        DHPublicKey dHPublicKey = (DHPublicKey) obj;
        if (getY().equals(dHPublicKey.getY()) && getParams().getG().equals(dHPublicKey.getParams().getG()) && getParams().getP().equals(dHPublicKey.getParams().getP()) && getParams().getL() == dHPublicKey.getParams().getL()) {
            z = true;
        }
        return z;
    }

    public String getAlgorithm() {
        return "DH";
    }

    public byte[] getEncoded() {
        if (this.info != null) {
            return KeyUtil.getEncodedSubjectPublicKeyInfo(this.info);
        }
        if (!(this.dhSpec instanceof DHDomainParameterSpec) || ((DHDomainParameterSpec) this.dhSpec).getQ() == null) {
            return KeyUtil.getEncodedSubjectPublicKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.dhKeyAgreement, new DHParameter(this.dhSpec.getP(), this.dhSpec.getG(), this.dhSpec.getL()).toASN1Primitive()), (ASN1Encodable) new ASN1Integer(this.y));
        }
        DHParameters domainParameters = ((DHDomainParameterSpec) this.dhSpec).getDomainParameters();
        DHValidationParameters validationParameters = domainParameters.getValidationParameters();
        ValidationParams validationParams = null;
        if (validationParameters == null) {
            validationParams = new ValidationParams(validationParameters.getSeed(), validationParameters.getCounter());
        }
        ValidationParams validationParams2 = validationParams;
        ASN1ObjectIdentifier aSN1ObjectIdentifier = X9ObjectIdentifiers.dhpublicnumber;
        DomainParameters domainParameters2 = new DomainParameters(domainParameters.getP(), domainParameters.getG(), domainParameters.getQ(), domainParameters.getJ(), validationParams2);
        return KeyUtil.getEncodedSubjectPublicKeyInfo(new AlgorithmIdentifier(aSN1ObjectIdentifier, domainParameters2.toASN1Primitive()), (ASN1Encodable) new ASN1Integer(this.y));
    }

    public String getFormat() {
        return "X.509";
    }

    public DHParameterSpec getParams() {
        return this.dhSpec;
    }

    public BigInteger getY() {
        return this.y;
    }

    public int hashCode() {
        return ((getY().hashCode() ^ getParams().getG().hashCode()) ^ getParams().getP().hashCode()) ^ getParams().getL();
    }
}
