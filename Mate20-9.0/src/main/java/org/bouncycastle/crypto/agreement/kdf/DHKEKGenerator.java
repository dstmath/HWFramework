package org.bouncycastle.crypto.agreement.kdf;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.DerivationFunction;
import org.bouncycastle.crypto.DerivationParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.util.Pack;

public class DHKEKGenerator implements DerivationFunction {
    private ASN1ObjectIdentifier algorithm;
    private final Digest digest;
    private int keySize;
    private byte[] partyAInfo;
    private byte[] z;

    public DHKEKGenerator(Digest digest2) {
        this.digest = digest2;
    }

    public int generateBytes(byte[] bArr, int i, int i2) throws DataLengthException, IllegalArgumentException {
        boolean z2;
        byte[] bArr2 = bArr;
        int i3 = i2;
        int i4 = i;
        if (bArr2.length - i3 >= i4) {
            long j = (long) i3;
            int digestSize = this.digest.getDigestSize();
            if (j <= 8589934591L) {
                long j2 = (long) digestSize;
                int i5 = (int) (((j + j2) - 1) / j2);
                byte[] bArr3 = new byte[this.digest.getDigestSize()];
                int i6 = 0;
                int i7 = i3;
                int i8 = i4;
                int i9 = 0;
                int i10 = 1;
                while (i9 < i5) {
                    this.digest.update(this.z, i6, this.z.length);
                    ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
                    ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
                    aSN1EncodableVector2.add(this.algorithm);
                    aSN1EncodableVector2.add(new DEROctetString(Pack.intToBigEndian(i10)));
                    aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector2));
                    if (this.partyAInfo != null) {
                        z2 = true;
                        aSN1EncodableVector.add(new DERTaggedObject(true, i6, new DEROctetString(this.partyAInfo)));
                    } else {
                        z2 = true;
                    }
                    aSN1EncodableVector.add(new DERTaggedObject(z2, 2, new DEROctetString(Pack.intToBigEndian(this.keySize))));
                    try {
                        byte[] encoded = new DERSequence(aSN1EncodableVector).getEncoded(ASN1Encoding.DER);
                        this.digest.update(encoded, 0, encoded.length);
                        this.digest.doFinal(bArr3, 0);
                        if (i7 > digestSize) {
                            System.arraycopy(bArr3, 0, bArr2, i8, digestSize);
                            i8 += digestSize;
                            i7 -= digestSize;
                        } else {
                            System.arraycopy(bArr3, 0, bArr2, i8, i7);
                        }
                        i10++;
                        i9++;
                        i6 = 0;
                    } catch (IOException e) {
                        throw new IllegalArgumentException("unable to encode parameter info: " + e.getMessage());
                    }
                }
                this.digest.reset();
                return (int) j;
            }
            throw new IllegalArgumentException("Output length too large");
        }
        throw new OutputLengthException("output buffer too small");
    }

    public Digest getDigest() {
        return this.digest;
    }

    public void init(DerivationParameters derivationParameters) {
        DHKDFParameters dHKDFParameters = (DHKDFParameters) derivationParameters;
        this.algorithm = dHKDFParameters.getAlgorithm();
        this.keySize = dHKDFParameters.getKeySize();
        this.z = dHKDFParameters.getZ();
        this.partyAInfo = dHKDFParameters.getExtraInfo();
    }
}
