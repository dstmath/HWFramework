package org.bouncycastle.asn1.smime;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;

public class SMIMECapabilityVector {
    private ASN1EncodableVector capabilities = new ASN1EncodableVector();

    public void addCapability(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        this.capabilities.add(new DERSequence(aSN1ObjectIdentifier));
    }

    public void addCapability(ASN1ObjectIdentifier aSN1ObjectIdentifier, int i) {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(2);
        aSN1EncodableVector.add(aSN1ObjectIdentifier);
        aSN1EncodableVector.add(new ASN1Integer((long) i));
        this.capabilities.add(new DERSequence(aSN1EncodableVector));
    }

    public void addCapability(ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1Encodable aSN1Encodable) {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(2);
        aSN1EncodableVector.add(aSN1ObjectIdentifier);
        aSN1EncodableVector.add(aSN1Encodable);
        this.capabilities.add(new DERSequence(aSN1EncodableVector));
    }

    public ASN1EncodableVector toASN1EncodableVector() {
        return this.capabilities;
    }
}
