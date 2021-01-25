package org.bouncycastle.crypto.signers;

import java.io.IOException;
import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.util.Arrays;

public class StandardDSAEncoding implements DSAEncoding {
    public static final StandardDSAEncoding INSTANCE = new StandardDSAEncoding();

    /* access modifiers changed from: protected */
    public BigInteger checkValue(BigInteger bigInteger, BigInteger bigInteger2) {
        if (bigInteger2.signum() >= 0 && (bigInteger == null || bigInteger2.compareTo(bigInteger) < 0)) {
            return bigInteger2;
        }
        throw new IllegalArgumentException("Value out of range");
    }

    @Override // org.bouncycastle.crypto.signers.DSAEncoding
    public BigInteger[] decode(BigInteger bigInteger, byte[] bArr) throws IOException {
        ASN1Sequence aSN1Sequence = (ASN1Sequence) ASN1Primitive.fromByteArray(bArr);
        if (aSN1Sequence.size() == 2) {
            BigInteger decodeValue = decodeValue(bigInteger, aSN1Sequence, 0);
            BigInteger decodeValue2 = decodeValue(bigInteger, aSN1Sequence, 1);
            if (Arrays.areEqual(encode(bigInteger, decodeValue, decodeValue2), bArr)) {
                return new BigInteger[]{decodeValue, decodeValue2};
            }
        }
        throw new IllegalArgumentException("Malformed signature");
    }

    /* access modifiers changed from: protected */
    public BigInteger decodeValue(BigInteger bigInteger, ASN1Sequence aSN1Sequence, int i) {
        return checkValue(bigInteger, ((ASN1Integer) aSN1Sequence.getObjectAt(i)).getValue());
    }

    @Override // org.bouncycastle.crypto.signers.DSAEncoding
    public byte[] encode(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3) throws IOException {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        encodeValue(bigInteger, aSN1EncodableVector, bigInteger2);
        encodeValue(bigInteger, aSN1EncodableVector, bigInteger3);
        return new DERSequence(aSN1EncodableVector).getEncoded(ASN1Encoding.DER);
    }

    /* access modifiers changed from: protected */
    public void encodeValue(BigInteger bigInteger, ASN1EncodableVector aSN1EncodableVector, BigInteger bigInteger2) {
        aSN1EncodableVector.add(new ASN1Integer(checkValue(bigInteger, bigInteger2)));
    }
}
