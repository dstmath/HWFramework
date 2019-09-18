package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1GeneralizedTime;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.ASN1UTCTime;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.DERTaggedObject;
import com.android.org.bouncycastle.asn1.x500.X500Name;
import java.util.Enumeration;
import java.util.NoSuchElementException;

public class TBSCertList extends ASN1Object {
    Extensions crlExtensions;
    X500Name issuer;
    Time nextUpdate;
    ASN1Sequence revokedCertificates;
    AlgorithmIdentifier signature;
    Time thisUpdate;
    ASN1Integer version;

    public static class CRLEntry extends ASN1Object {
        Extensions crlEntryExtensions;
        ASN1Sequence seq;

        private CRLEntry(ASN1Sequence seq2) {
            if (seq2.size() < 2 || seq2.size() > 3) {
                throw new IllegalArgumentException("Bad sequence size: " + seq2.size());
            }
            this.seq = seq2;
        }

        public static CRLEntry getInstance(Object o) {
            if (o instanceof CRLEntry) {
                return (CRLEntry) o;
            }
            if (o != null) {
                return new CRLEntry(ASN1Sequence.getInstance(o));
            }
            return null;
        }

        public ASN1Integer getUserCertificate() {
            return ASN1Integer.getInstance(this.seq.getObjectAt(0));
        }

        public Time getRevocationDate() {
            return Time.getInstance(this.seq.getObjectAt(1));
        }

        public Extensions getExtensions() {
            if (this.crlEntryExtensions == null && this.seq.size() == 3) {
                this.crlEntryExtensions = Extensions.getInstance(this.seq.getObjectAt(2));
            }
            return this.crlEntryExtensions;
        }

        public ASN1Primitive toASN1Primitive() {
            return this.seq;
        }

        public boolean hasExtensions() {
            return this.seq.size() == 3;
        }
    }

    private class EmptyEnumeration implements Enumeration {
        private EmptyEnumeration() {
        }

        public boolean hasMoreElements() {
            return false;
        }

        public Object nextElement() {
            throw new NoSuchElementException("Empty Enumeration");
        }
    }

    private class RevokedCertificatesEnumeration implements Enumeration {
        private final Enumeration en;

        RevokedCertificatesEnumeration(Enumeration en2) {
            this.en = en2;
        }

        public boolean hasMoreElements() {
            return this.en.hasMoreElements();
        }

        public Object nextElement() {
            return CRLEntry.getInstance(this.en.nextElement());
        }
    }

    public static TBSCertList getInstance(ASN1TaggedObject obj, boolean explicit) {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static TBSCertList getInstance(Object obj) {
        if (obj instanceof TBSCertList) {
            return (TBSCertList) obj;
        }
        if (obj != null) {
            return new TBSCertList(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public TBSCertList(ASN1Sequence seq) {
        int seqPos;
        if (seq.size() < 3 || seq.size() > 7) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }
        int seqPos2 = 0;
        if (seq.getObjectAt(0) instanceof ASN1Integer) {
            this.version = ASN1Integer.getInstance(seq.getObjectAt(0));
            seqPos2 = 0 + 1;
        } else {
            this.version = null;
        }
        int seqPos3 = seqPos2 + 1;
        this.signature = AlgorithmIdentifier.getInstance(seq.getObjectAt(seqPos2));
        int seqPos4 = seqPos3 + 1;
        this.issuer = X500Name.getInstance(seq.getObjectAt(seqPos3));
        int seqPos5 = seqPos4 + 1;
        this.thisUpdate = Time.getInstance(seq.getObjectAt(seqPos4));
        if (seqPos5 >= seq.size() || (!(seq.getObjectAt(seqPos5) instanceof ASN1UTCTime) && !(seq.getObjectAt(seqPos5) instanceof ASN1GeneralizedTime) && !(seq.getObjectAt(seqPos5) instanceof Time))) {
            seqPos = seqPos5;
        } else {
            seqPos = seqPos5 + 1;
            this.nextUpdate = Time.getInstance(seq.getObjectAt(seqPos5));
        }
        if (seqPos < seq.size() && !(seq.getObjectAt(seqPos) instanceof ASN1TaggedObject)) {
            this.revokedCertificates = ASN1Sequence.getInstance(seq.getObjectAt(seqPos));
            seqPos++;
        }
        if (seqPos < seq.size() && (seq.getObjectAt(seqPos) instanceof ASN1TaggedObject)) {
            this.crlExtensions = Extensions.getInstance(ASN1Sequence.getInstance((ASN1TaggedObject) seq.getObjectAt(seqPos), true));
        }
    }

    public int getVersionNumber() {
        if (this.version == null) {
            return 1;
        }
        return this.version.getValue().intValue() + 1;
    }

    public ASN1Integer getVersion() {
        return this.version;
    }

    public AlgorithmIdentifier getSignature() {
        return this.signature;
    }

    public X500Name getIssuer() {
        return this.issuer;
    }

    public Time getThisUpdate() {
        return this.thisUpdate;
    }

    public Time getNextUpdate() {
        return this.nextUpdate;
    }

    public CRLEntry[] getRevokedCertificates() {
        if (this.revokedCertificates == null) {
            return new CRLEntry[0];
        }
        CRLEntry[] entries = new CRLEntry[this.revokedCertificates.size()];
        for (int i = 0; i < entries.length; i++) {
            entries[i] = CRLEntry.getInstance(this.revokedCertificates.getObjectAt(i));
        }
        return entries;
    }

    public Enumeration getRevokedCertificateEnumeration() {
        if (this.revokedCertificates == null) {
            return new EmptyEnumeration();
        }
        return new RevokedCertificatesEnumeration(this.revokedCertificates.getObjects());
    }

    public Extensions getExtensions() {
        return this.crlExtensions;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        if (this.version != null) {
            v.add(this.version);
        }
        v.add(this.signature);
        v.add(this.issuer);
        v.add(this.thisUpdate);
        if (this.nextUpdate != null) {
            v.add(this.nextUpdate);
        }
        if (this.revokedCertificates != null) {
            v.add(this.revokedCertificates);
        }
        if (this.crlExtensions != null) {
            v.add(new DERTaggedObject(0, this.crlExtensions));
        }
        return new DERSequence(v);
    }
}
