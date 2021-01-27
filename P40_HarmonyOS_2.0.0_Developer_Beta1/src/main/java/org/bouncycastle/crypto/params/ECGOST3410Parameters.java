package org.bouncycastle.crypto.params;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;

public class ECGOST3410Parameters extends ECNamedDomainParameters {
    private final ASN1ObjectIdentifier digestParamSet;
    private final ASN1ObjectIdentifier encryptionParamSet;
    private final ASN1ObjectIdentifier publicKeyParamSet;

    public ECGOST3410Parameters(ECDomainParameters eCDomainParameters, ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1ObjectIdentifier aSN1ObjectIdentifier2) {
        this(eCDomainParameters, aSN1ObjectIdentifier, aSN1ObjectIdentifier2, null);
    }

    public ECGOST3410Parameters(ECDomainParameters eCDomainParameters, ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1ObjectIdentifier aSN1ObjectIdentifier2, ASN1ObjectIdentifier aSN1ObjectIdentifier3) {
        super(aSN1ObjectIdentifier, eCDomainParameters.getCurve(), eCDomainParameters.getG(), eCDomainParameters.getN(), eCDomainParameters.getH(), eCDomainParameters.getSeed());
        if (!(eCDomainParameters instanceof ECNamedDomainParameters) || aSN1ObjectIdentifier.equals((ASN1Primitive) ((ECNamedDomainParameters) eCDomainParameters).getName())) {
            this.publicKeyParamSet = aSN1ObjectIdentifier;
            this.digestParamSet = aSN1ObjectIdentifier2;
            this.encryptionParamSet = aSN1ObjectIdentifier3;
            return;
        }
        throw new IllegalArgumentException("named parameters do not match publicKeyParamSet value");
    }

    public ASN1ObjectIdentifier getDigestParamSet() {
        return this.digestParamSet;
    }

    public ASN1ObjectIdentifier getEncryptionParamSet() {
        return this.encryptionParamSet;
    }

    public ASN1ObjectIdentifier getPublicKeyParamSet() {
        return this.publicKeyParamSet;
    }
}
