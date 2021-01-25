package org.bouncycastle.asn1.cms;

import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.tsp.EvidenceRecord;

public class Evidence extends ASN1Object implements ASN1Choice {
    private EvidenceRecord ersEvidence;
    private ASN1Sequence otherEvidence;
    private TimeStampTokenEvidence tstEvidence;

    private Evidence(ASN1TaggedObject aSN1TaggedObject) {
        if (aSN1TaggedObject.getTagNo() == 0) {
            this.tstEvidence = TimeStampTokenEvidence.getInstance(aSN1TaggedObject, false);
        } else if (aSN1TaggedObject.getTagNo() == 1) {
            this.ersEvidence = EvidenceRecord.getInstance(aSN1TaggedObject, false);
        } else if (aSN1TaggedObject.getTagNo() == 2) {
            this.otherEvidence = ASN1Sequence.getInstance(aSN1TaggedObject, false);
        } else {
            throw new IllegalArgumentException("unknown tag in Evidence");
        }
    }

    public Evidence(TimeStampTokenEvidence timeStampTokenEvidence) {
        this.tstEvidence = timeStampTokenEvidence;
    }

    public Evidence(EvidenceRecord evidenceRecord) {
        this.ersEvidence = evidenceRecord;
    }

    public static Evidence getInstance(Object obj) {
        if (obj == null || (obj instanceof Evidence)) {
            return (Evidence) obj;
        }
        if (obj instanceof ASN1TaggedObject) {
            return new Evidence(ASN1TaggedObject.getInstance(obj));
        }
        throw new IllegalArgumentException("unknown object in getInstance");
    }

    public static Evidence getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(aSN1TaggedObject.getObject());
    }

    public EvidenceRecord getErsEvidence() {
        return this.ersEvidence;
    }

    public TimeStampTokenEvidence getTstEvidence() {
        return this.tstEvidence;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        TimeStampTokenEvidence timeStampTokenEvidence = this.tstEvidence;
        if (timeStampTokenEvidence != null) {
            return new DERTaggedObject(false, 0, timeStampTokenEvidence);
        }
        EvidenceRecord evidenceRecord = this.ersEvidence;
        return evidenceRecord != null ? new DERTaggedObject(false, 1, evidenceRecord) : new DERTaggedObject(false, 2, this.otherEvidence);
    }
}
