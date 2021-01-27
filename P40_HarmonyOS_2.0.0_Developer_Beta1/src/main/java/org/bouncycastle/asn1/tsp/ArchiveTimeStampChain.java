package org.bouncycastle.asn1.tsp;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

public class ArchiveTimeStampChain extends ASN1Object {
    private ASN1Sequence archiveTimestamps;

    private ArchiveTimeStampChain(ASN1Sequence aSN1Sequence) {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(aSN1Sequence.size());
        Enumeration objects = aSN1Sequence.getObjects();
        while (objects.hasMoreElements()) {
            aSN1EncodableVector.add(ArchiveTimeStamp.getInstance(objects.nextElement()));
        }
        this.archiveTimestamps = new DERSequence(aSN1EncodableVector);
    }

    public ArchiveTimeStampChain(ArchiveTimeStamp archiveTimeStamp) {
        this.archiveTimestamps = new DERSequence(archiveTimeStamp);
    }

    public ArchiveTimeStampChain(ArchiveTimeStamp[] archiveTimeStampArr) {
        this.archiveTimestamps = new DERSequence(archiveTimeStampArr);
    }

    public static ArchiveTimeStampChain getInstance(Object obj) {
        if (obj instanceof ArchiveTimeStampChain) {
            return (ArchiveTimeStampChain) obj;
        }
        if (obj != null) {
            return new ArchiveTimeStampChain(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public ArchiveTimeStampChain append(ArchiveTimeStamp archiveTimeStamp) {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(this.archiveTimestamps.size() + 1);
        for (int i = 0; i != this.archiveTimestamps.size(); i++) {
            aSN1EncodableVector.add(this.archiveTimestamps.getObjectAt(i));
        }
        aSN1EncodableVector.add(archiveTimeStamp);
        return new ArchiveTimeStampChain(new DERSequence(aSN1EncodableVector));
    }

    public ArchiveTimeStamp[] getArchiveTimestamps() {
        ArchiveTimeStamp[] archiveTimeStampArr = new ArchiveTimeStamp[this.archiveTimestamps.size()];
        for (int i = 0; i != archiveTimeStampArr.length; i++) {
            archiveTimeStampArr[i] = ArchiveTimeStamp.getInstance(this.archiveTimestamps.getObjectAt(i));
        }
        return archiveTimeStampArr;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        return this.archiveTimestamps;
    }
}
