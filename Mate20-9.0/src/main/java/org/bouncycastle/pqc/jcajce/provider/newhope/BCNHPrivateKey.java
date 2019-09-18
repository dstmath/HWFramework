package org.bouncycastle.pqc.jcajce.provider.newhope;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import org.bouncycastle.pqc.crypto.newhope.NHPrivateKeyParameters;
import org.bouncycastle.pqc.jcajce.interfaces.NHPrivateKey;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public class BCNHPrivateKey implements NHPrivateKey {
    private static final long serialVersionUID = 1;
    private final NHPrivateKeyParameters params;

    public BCNHPrivateKey(PrivateKeyInfo privateKeyInfo) throws IOException {
        this.params = new NHPrivateKeyParameters(convert(ASN1OctetString.getInstance(privateKeyInfo.parsePrivateKey()).getOctets()));
    }

    public BCNHPrivateKey(NHPrivateKeyParameters nHPrivateKeyParameters) {
        this.params = nHPrivateKeyParameters;
    }

    private static short[] convert(byte[] bArr) {
        short[] sArr = new short[(bArr.length / 2)];
        for (int i = 0; i != sArr.length; i++) {
            sArr[i] = Pack.littleEndianToShort(bArr, i * 2);
        }
        return sArr;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof BCNHPrivateKey)) {
            return false;
        }
        return Arrays.areEqual(this.params.getSecData(), ((BCNHPrivateKey) obj).params.getSecData());
    }

    public final String getAlgorithm() {
        return "NH";
    }

    public byte[] getEncoded() {
        try {
            AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(PQCObjectIdentifiers.newHope);
            short[] secData = this.params.getSecData();
            byte[] bArr = new byte[(secData.length * 2)];
            for (int i = 0; i != secData.length; i++) {
                Pack.shortToLittleEndian(secData[i], bArr, i * 2);
            }
            return new PrivateKeyInfo(algorithmIdentifier, new DEROctetString(bArr)).getEncoded();
        } catch (IOException e) {
            return null;
        }
    }

    public String getFormat() {
        return "PKCS#8";
    }

    /* access modifiers changed from: package-private */
    public CipherParameters getKeyParams() {
        return this.params;
    }

    public short[] getSecretData() {
        return this.params.getSecData();
    }

    public int hashCode() {
        return Arrays.hashCode(this.params.getSecData());
    }
}
