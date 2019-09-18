package org.bouncycastle.cms.jcajce;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.cms.ecc.ECCCMSSharedInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.Pack;

class RFC5753KeyMaterialGenerator implements KeyMaterialGenerator {
    RFC5753KeyMaterialGenerator() {
    }

    public byte[] generateKDFMaterial(AlgorithmIdentifier algorithmIdentifier, int i, byte[] bArr) {
        try {
            return new ECCCMSSharedInfo(algorithmIdentifier, bArr, Pack.intToBigEndian(i)).getEncoded(ASN1Encoding.DER);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create KDF material: " + e);
        }
    }
}
