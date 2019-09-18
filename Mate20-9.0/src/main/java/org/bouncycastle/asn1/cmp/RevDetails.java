package org.bouncycastle.asn1.cmp;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.crmf.CertTemplate;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.X509Extensions;

public class RevDetails extends ASN1Object {
    private CertTemplate certDetails;
    private Extensions crlEntryDetails;

    private RevDetails(ASN1Sequence aSN1Sequence) {
        this.certDetails = CertTemplate.getInstance(aSN1Sequence.getObjectAt(0));
        if (aSN1Sequence.size() > 1) {
            this.crlEntryDetails = Extensions.getInstance(aSN1Sequence.getObjectAt(1));
        }
    }

    public RevDetails(CertTemplate certTemplate) {
        this.certDetails = certTemplate;
    }

    public RevDetails(CertTemplate certTemplate, Extensions extensions) {
        this.certDetails = certTemplate;
        this.crlEntryDetails = extensions;
    }

    public RevDetails(CertTemplate certTemplate, X509Extensions x509Extensions) {
        this.certDetails = certTemplate;
        this.crlEntryDetails = Extensions.getInstance(x509Extensions.toASN1Primitive());
    }

    public static RevDetails getInstance(Object obj) {
        if (obj instanceof RevDetails) {
            return (RevDetails) obj;
        }
        if (obj != null) {
            return new RevDetails(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public CertTemplate getCertDetails() {
        return this.certDetails;
    }

    public Extensions getCrlEntryDetails() {
        return this.crlEntryDetails;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.certDetails);
        if (this.crlEntryDetails != null) {
            aSN1EncodableVector.add(this.crlEntryDetails);
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
