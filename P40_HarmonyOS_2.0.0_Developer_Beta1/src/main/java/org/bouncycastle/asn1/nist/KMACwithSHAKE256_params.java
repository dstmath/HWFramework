package org.bouncycastle.asn1.nist;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.util.Arrays;

public class KMACwithSHAKE256_params extends ASN1Object {
    private static final int DEF_LENGTH = 512;
    private static final byte[] EMPTY_STRING = new byte[0];
    private final byte[] customizationString;
    private final int outputLength;

    public KMACwithSHAKE256_params(int i) {
        this.outputLength = i;
        this.customizationString = EMPTY_STRING;
    }

    public KMACwithSHAKE256_params(int i, byte[] bArr) {
        this.outputLength = i;
        this.customizationString = Arrays.clone(bArr);
    }

    private KMACwithSHAKE256_params(ASN1Sequence aSN1Sequence) {
        byte[] bArr;
        ASN1Encodable objectAt;
        if (aSN1Sequence.size() <= 2) {
            if (aSN1Sequence.size() == 2) {
                this.outputLength = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(0)).intValueExact();
                objectAt = aSN1Sequence.getObjectAt(1);
            } else {
                if (aSN1Sequence.size() != 1) {
                    this.outputLength = 512;
                } else if (aSN1Sequence.getObjectAt(0) instanceof ASN1Integer) {
                    this.outputLength = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(0)).intValueExact();
                } else {
                    this.outputLength = 512;
                    objectAt = aSN1Sequence.getObjectAt(0);
                }
                bArr = EMPTY_STRING;
                this.customizationString = bArr;
                return;
            }
            bArr = Arrays.clone(ASN1OctetString.getInstance(objectAt).getOctets());
            this.customizationString = bArr;
            return;
        }
        throw new IllegalArgumentException("sequence size greater than 2");
    }

    public static KMACwithSHAKE256_params getInstance(Object obj) {
        if (obj instanceof KMACwithSHAKE256_params) {
            return (KMACwithSHAKE256_params) obj;
        }
        if (obj != null) {
            return new KMACwithSHAKE256_params(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public byte[] getCustomizationString() {
        return Arrays.clone(this.customizationString);
    }

    public int getOutputLength() {
        return this.outputLength;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        int i = this.outputLength;
        if (i != 512) {
            aSN1EncodableVector.add(new ASN1Integer((long) i));
        }
        if (this.customizationString.length != 0) {
            aSN1EncodableVector.add(new DEROctetString(getCustomizationString()));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
