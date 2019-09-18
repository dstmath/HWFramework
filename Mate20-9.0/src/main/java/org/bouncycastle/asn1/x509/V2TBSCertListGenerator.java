package org.bouncycastle.asn1.x509;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x500.X500Name;

public class V2TBSCertListGenerator {
    private static final ASN1Sequence[] reasons = new ASN1Sequence[11];
    private ASN1EncodableVector crlentries = new ASN1EncodableVector();
    private Extensions extensions = null;
    private X500Name issuer;
    private Time nextUpdate = null;
    private AlgorithmIdentifier signature;
    private Time thisUpdate;
    private ASN1Integer version = new ASN1Integer(1);

    static {
        reasons[0] = createReasonExtension(0);
        reasons[1] = createReasonExtension(1);
        reasons[2] = createReasonExtension(2);
        reasons[3] = createReasonExtension(3);
        reasons[4] = createReasonExtension(4);
        reasons[5] = createReasonExtension(5);
        reasons[6] = createReasonExtension(6);
        reasons[7] = createReasonExtension(7);
        reasons[8] = createReasonExtension(8);
        reasons[9] = createReasonExtension(9);
        reasons[10] = createReasonExtension(10);
    }

    private static ASN1Sequence createInvalidityDateExtension(ASN1GeneralizedTime aSN1GeneralizedTime) {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        try {
            aSN1EncodableVector.add(Extension.invalidityDate);
            aSN1EncodableVector.add(new DEROctetString(aSN1GeneralizedTime.getEncoded()));
            return new DERSequence(aSN1EncodableVector);
        } catch (IOException e) {
            throw new IllegalArgumentException("error encoding reason: " + e);
        }
    }

    private static ASN1Sequence createReasonExtension(int i) {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        CRLReason lookup = CRLReason.lookup(i);
        try {
            aSN1EncodableVector.add(Extension.reasonCode);
            aSN1EncodableVector.add(new DEROctetString(lookup.getEncoded()));
            return new DERSequence(aSN1EncodableVector);
        } catch (IOException e) {
            throw new IllegalArgumentException("error encoding reason: " + e);
        }
    }

    private void internalAddCRLEntry(ASN1Integer aSN1Integer, Time time, ASN1Sequence aSN1Sequence) {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(aSN1Integer);
        aSN1EncodableVector.add(time);
        if (aSN1Sequence != null) {
            aSN1EncodableVector.add(aSN1Sequence);
        }
        addCRLEntry(new DERSequence(aSN1EncodableVector));
    }

    public void addCRLEntry(ASN1Integer aSN1Integer, ASN1UTCTime aSN1UTCTime, int i) {
        addCRLEntry(aSN1Integer, new Time((ASN1Primitive) aSN1UTCTime), i);
    }

    public void addCRLEntry(ASN1Integer aSN1Integer, Time time, int i) {
        addCRLEntry(aSN1Integer, time, i, null);
    }

    public void addCRLEntry(ASN1Integer aSN1Integer, Time time, int i, ASN1GeneralizedTime aSN1GeneralizedTime) {
        ASN1Sequence aSN1Sequence;
        if (i != 0) {
            ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
            if (i >= reasons.length) {
                aSN1Sequence = createReasonExtension(i);
            } else if (i >= 0) {
                aSN1Sequence = reasons[i];
            } else {
                throw new IllegalArgumentException("invalid reason value: " + i);
            }
            aSN1EncodableVector.add(aSN1Sequence);
            if (aSN1GeneralizedTime != null) {
                aSN1EncodableVector.add(createInvalidityDateExtension(aSN1GeneralizedTime));
            }
            internalAddCRLEntry(aSN1Integer, time, new DERSequence(aSN1EncodableVector));
        } else if (aSN1GeneralizedTime != null) {
            ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
            aSN1EncodableVector2.add(createInvalidityDateExtension(aSN1GeneralizedTime));
            internalAddCRLEntry(aSN1Integer, time, new DERSequence(aSN1EncodableVector2));
        } else {
            addCRLEntry(aSN1Integer, time, (Extensions) null);
        }
    }

    public void addCRLEntry(ASN1Integer aSN1Integer, Time time, Extensions extensions2) {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(aSN1Integer);
        aSN1EncodableVector.add(time);
        if (extensions2 != null) {
            aSN1EncodableVector.add(extensions2);
        }
        addCRLEntry(new DERSequence(aSN1EncodableVector));
    }

    public void addCRLEntry(ASN1Sequence aSN1Sequence) {
        this.crlentries.add(aSN1Sequence);
    }

    public TBSCertList generateTBSCertList() {
        if (this.signature == null || this.issuer == null || this.thisUpdate == null) {
            throw new IllegalStateException("Not all mandatory fields set in V2 TBSCertList generator.");
        }
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.version);
        aSN1EncodableVector.add(this.signature);
        aSN1EncodableVector.add(this.issuer);
        aSN1EncodableVector.add(this.thisUpdate);
        if (this.nextUpdate != null) {
            aSN1EncodableVector.add(this.nextUpdate);
        }
        if (this.crlentries.size() != 0) {
            aSN1EncodableVector.add(new DERSequence(this.crlentries));
        }
        if (this.extensions != null) {
            aSN1EncodableVector.add(new DERTaggedObject(0, this.extensions));
        }
        return new TBSCertList(new DERSequence(aSN1EncodableVector));
    }

    public void setExtensions(Extensions extensions2) {
        this.extensions = extensions2;
    }

    public void setExtensions(X509Extensions x509Extensions) {
        setExtensions(Extensions.getInstance(x509Extensions));
    }

    public void setIssuer(X500Name x500Name) {
        this.issuer = x500Name;
    }

    public void setIssuer(X509Name x509Name) {
        this.issuer = X500Name.getInstance(x509Name.toASN1Primitive());
    }

    public void setNextUpdate(ASN1UTCTime aSN1UTCTime) {
        this.nextUpdate = new Time((ASN1Primitive) aSN1UTCTime);
    }

    public void setNextUpdate(Time time) {
        this.nextUpdate = time;
    }

    public void setSignature(AlgorithmIdentifier algorithmIdentifier) {
        this.signature = algorithmIdentifier;
    }

    public void setThisUpdate(ASN1UTCTime aSN1UTCTime) {
        this.thisUpdate = new Time((ASN1Primitive) aSN1UTCTime);
    }

    public void setThisUpdate(Time time) {
        this.thisUpdate = time;
    }
}
