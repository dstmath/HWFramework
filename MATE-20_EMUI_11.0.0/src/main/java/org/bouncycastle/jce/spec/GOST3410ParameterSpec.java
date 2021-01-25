package org.bouncycastle.jce.spec;

import java.security.spec.AlgorithmParameterSpec;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.cryptopro.GOST3410NamedParameters;
import org.bouncycastle.asn1.cryptopro.GOST3410ParamSetParameters;
import org.bouncycastle.asn1.cryptopro.GOST3410PublicKeyAlgParameters;
import org.bouncycastle.jce.interfaces.GOST3410Params;

public class GOST3410ParameterSpec implements AlgorithmParameterSpec, GOST3410Params {
    private String digestParamSetOID;
    private String encryptionParamSetOID;
    private String keyParamSetOID;
    private GOST3410PublicKeyParameterSetSpec keyParameters;

    public GOST3410ParameterSpec(String str) {
        this(str, CryptoProObjectIdentifiers.gostR3411_94_CryptoProParamSet.getId(), null);
    }

    public GOST3410ParameterSpec(String str, String str2) {
        this(str, str2, null);
    }

    public GOST3410ParameterSpec(String str, String str2, String str3) {
        GOST3410ParamSetParameters gOST3410ParamSetParameters;
        try {
            gOST3410ParamSetParameters = GOST3410NamedParameters.getByOID(new ASN1ObjectIdentifier(str));
        } catch (IllegalArgumentException e) {
            ASN1ObjectIdentifier oid = GOST3410NamedParameters.getOID(str);
            if (oid != null) {
                str = oid.getId();
                gOST3410ParamSetParameters = GOST3410NamedParameters.getByOID(oid);
            } else {
                gOST3410ParamSetParameters = null;
            }
        }
        if (gOST3410ParamSetParameters != null) {
            this.keyParameters = new GOST3410PublicKeyParameterSetSpec(gOST3410ParamSetParameters.getP(), gOST3410ParamSetParameters.getQ(), gOST3410ParamSetParameters.getA());
            this.keyParamSetOID = str;
            this.digestParamSetOID = str2;
            this.encryptionParamSetOID = str3;
            return;
        }
        throw new IllegalArgumentException("no key parameter set for passed in name/OID.");
    }

    public GOST3410ParameterSpec(GOST3410PublicKeyParameterSetSpec gOST3410PublicKeyParameterSetSpec) {
        this.keyParameters = gOST3410PublicKeyParameterSetSpec;
        this.digestParamSetOID = CryptoProObjectIdentifiers.gostR3411_94_CryptoProParamSet.getId();
        this.encryptionParamSetOID = null;
    }

    public static GOST3410ParameterSpec fromPublicKeyAlg(GOST3410PublicKeyAlgParameters gOST3410PublicKeyAlgParameters) {
        return gOST3410PublicKeyAlgParameters.getEncryptionParamSet() != null ? new GOST3410ParameterSpec(gOST3410PublicKeyAlgParameters.getPublicKeyParamSet().getId(), gOST3410PublicKeyAlgParameters.getDigestParamSet().getId(), gOST3410PublicKeyAlgParameters.getEncryptionParamSet().getId()) : new GOST3410ParameterSpec(gOST3410PublicKeyAlgParameters.getPublicKeyParamSet().getId(), gOST3410PublicKeyAlgParameters.getDigestParamSet().getId());
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (!(obj instanceof GOST3410ParameterSpec)) {
            return false;
        }
        GOST3410ParameterSpec gOST3410ParameterSpec = (GOST3410ParameterSpec) obj;
        if (!this.keyParameters.equals(gOST3410ParameterSpec.keyParameters) || !this.digestParamSetOID.equals(gOST3410ParameterSpec.digestParamSetOID)) {
            return false;
        }
        String str = this.encryptionParamSetOID;
        String str2 = gOST3410ParameterSpec.encryptionParamSetOID;
        return str == str2 || (str != null && str.equals(str2));
    }

    @Override // org.bouncycastle.jce.interfaces.GOST3410Params
    public String getDigestParamSetOID() {
        return this.digestParamSetOID;
    }

    @Override // org.bouncycastle.jce.interfaces.GOST3410Params
    public String getEncryptionParamSetOID() {
        return this.encryptionParamSetOID;
    }

    @Override // org.bouncycastle.jce.interfaces.GOST3410Params
    public String getPublicKeyParamSetOID() {
        return this.keyParamSetOID;
    }

    @Override // org.bouncycastle.jce.interfaces.GOST3410Params
    public GOST3410PublicKeyParameterSetSpec getPublicKeyParameters() {
        return this.keyParameters;
    }

    @Override // java.lang.Object
    public int hashCode() {
        int hashCode = this.keyParameters.hashCode() ^ this.digestParamSetOID.hashCode();
        String str = this.encryptionParamSetOID;
        return hashCode ^ (str != null ? str.hashCode() : 0);
    }
}
