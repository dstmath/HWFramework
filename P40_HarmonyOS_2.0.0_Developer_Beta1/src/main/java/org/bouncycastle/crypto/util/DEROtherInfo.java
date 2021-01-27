package org.bouncycastle.crypto.util;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class DEROtherInfo {
    private final DERSequence sequence;

    public static final class Builder {
        private final AlgorithmIdentifier algorithmID;
        private final ASN1OctetString partyUVInfo;
        private final ASN1OctetString partyVInfo;
        private ASN1TaggedObject suppPrivInfo;
        private ASN1TaggedObject suppPubInfo;

        public Builder(AlgorithmIdentifier algorithmIdentifier, byte[] bArr, byte[] bArr2) {
            this.algorithmID = algorithmIdentifier;
            this.partyUVInfo = DerUtil.getOctetString(bArr);
            this.partyVInfo = DerUtil.getOctetString(bArr2);
        }

        public DEROtherInfo build() {
            ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
            aSN1EncodableVector.add(this.algorithmID);
            aSN1EncodableVector.add(this.partyUVInfo);
            aSN1EncodableVector.add(this.partyVInfo);
            ASN1TaggedObject aSN1TaggedObject = this.suppPubInfo;
            if (aSN1TaggedObject != null) {
                aSN1EncodableVector.add(aSN1TaggedObject);
            }
            ASN1TaggedObject aSN1TaggedObject2 = this.suppPrivInfo;
            if (aSN1TaggedObject2 != null) {
                aSN1EncodableVector.add(aSN1TaggedObject2);
            }
            return new DEROtherInfo(new DERSequence(aSN1EncodableVector));
        }

        public Builder withSuppPrivInfo(byte[] bArr) {
            this.suppPrivInfo = new DERTaggedObject(false, 1, DerUtil.getOctetString(bArr));
            return this;
        }

        public Builder withSuppPubInfo(byte[] bArr) {
            this.suppPubInfo = new DERTaggedObject(false, 0, DerUtil.getOctetString(bArr));
            return this;
        }
    }

    private DEROtherInfo(DERSequence dERSequence) {
        this.sequence = dERSequence;
    }

    public byte[] getEncoded() throws IOException {
        return this.sequence.getEncoded();
    }
}
