package org.bouncycastle.asn1.cmp;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class PBMParameter extends ASN1Object {
    private ASN1Integer iterationCount;
    private AlgorithmIdentifier mac;
    private AlgorithmIdentifier owf;
    private ASN1OctetString salt;

    public PBMParameter(ASN1OctetString aSN1OctetString, AlgorithmIdentifier algorithmIdentifier, ASN1Integer aSN1Integer, AlgorithmIdentifier algorithmIdentifier2) {
        this.salt = aSN1OctetString;
        this.owf = algorithmIdentifier;
        this.iterationCount = aSN1Integer;
        this.mac = algorithmIdentifier2;
    }

    private PBMParameter(ASN1Sequence aSN1Sequence) {
        this.salt = ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(0));
        this.owf = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(1));
        this.iterationCount = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(2));
        this.mac = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(3));
    }

    public PBMParameter(byte[] bArr, AlgorithmIdentifier algorithmIdentifier, int i, AlgorithmIdentifier algorithmIdentifier2) {
        this((ASN1OctetString) new DEROctetString(bArr), algorithmIdentifier, new ASN1Integer((long) i), algorithmIdentifier2);
    }

    public static PBMParameter getInstance(Object obj) {
        if (obj instanceof PBMParameter) {
            return (PBMParameter) obj;
        }
        if (obj != null) {
            return new PBMParameter(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public ASN1Integer getIterationCount() {
        return this.iterationCount;
    }

    public AlgorithmIdentifier getMac() {
        return this.mac;
    }

    public AlgorithmIdentifier getOwf() {
        return this.owf;
    }

    public ASN1OctetString getSalt() {
        return this.salt;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.salt);
        aSN1EncodableVector.add(this.owf);
        aSN1EncodableVector.add(this.iterationCount);
        aSN1EncodableVector.add(this.mac);
        return new DERSequence(aSN1EncodableVector);
    }
}
