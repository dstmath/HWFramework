package org.bouncycastle.jcajce.spec;

import java.security.spec.AlgorithmParameterSpec;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.cryptopro.ECGOST3410NamedCurves;
import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers;

public class GOST3410ParameterSpec implements AlgorithmParameterSpec {
    private final ASN1ObjectIdentifier digestParamSet;
    private final ASN1ObjectIdentifier encryptionParamSet;
    private final ASN1ObjectIdentifier publicKeyParamSet;

    public GOST3410ParameterSpec(String str) {
        this(getOid(str), getDigestOid(str), null);
    }

    public GOST3410ParameterSpec(ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1ObjectIdentifier aSN1ObjectIdentifier2) {
        this(aSN1ObjectIdentifier, aSN1ObjectIdentifier2, null);
    }

    public GOST3410ParameterSpec(ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1ObjectIdentifier aSN1ObjectIdentifier2, ASN1ObjectIdentifier aSN1ObjectIdentifier3) {
        this.publicKeyParamSet = aSN1ObjectIdentifier;
        this.digestParamSet = aSN1ObjectIdentifier2;
        this.encryptionParamSet = aSN1ObjectIdentifier3;
    }

    private static ASN1ObjectIdentifier getDigestOid(String str) {
        return str.indexOf("12-512") > 0 ? RosstandartObjectIdentifiers.id_tc26_gost_3411_12_512 : str.indexOf("12-256") > 0 ? RosstandartObjectIdentifiers.id_tc26_gost_3411_12_256 : CryptoProObjectIdentifiers.gostR3411_94_CryptoProParamSet;
    }

    private static ASN1ObjectIdentifier getOid(String str) {
        return ECGOST3410NamedCurves.getOID(str);
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

    public String getPublicKeyParamSetName() {
        return ECGOST3410NamedCurves.getName(getPublicKeyParamSet());
    }
}
