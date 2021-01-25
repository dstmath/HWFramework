package org.bouncycastle.jcajce.spec;

import java.security.spec.ECParameterSpec;
import org.bouncycastle.asn1.ua.DSTU4145Params;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.util.Arrays;

public class DSTU4145ParameterSpec extends ECParameterSpec {
    private final byte[] dke;
    private final ECDomainParameters parameters;

    public DSTU4145ParameterSpec(ECDomainParameters eCDomainParameters) {
        this(eCDomainParameters, EC5Util.convertToSpec(eCDomainParameters), DSTU4145Params.getDefaultDKE());
    }

    private DSTU4145ParameterSpec(ECDomainParameters eCDomainParameters, ECParameterSpec eCParameterSpec, byte[] bArr) {
        super(eCParameterSpec.getCurve(), eCParameterSpec.getGenerator(), eCParameterSpec.getOrder(), eCParameterSpec.getCofactor());
        this.parameters = eCDomainParameters;
        this.dke = Arrays.clone(bArr);
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj instanceof DSTU4145ParameterSpec) {
            return this.parameters.equals(((DSTU4145ParameterSpec) obj).parameters);
        }
        return false;
    }

    public byte[] getDKE() {
        return Arrays.clone(this.dke);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return this.parameters.hashCode();
    }
}
