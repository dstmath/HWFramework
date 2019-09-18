package org.bouncycastle.asn1.cmc;

import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.util.Arrays;

public class RevokeRequest extends ASN1Object {
    private DERUTF8String comment;
    private ASN1GeneralizedTime invalidityDate;
    private final X500Name name;
    private ASN1OctetString passphrase;
    private final CRLReason reason;
    private final ASN1Integer serialNumber;

    private RevokeRequest(ASN1Sequence aSN1Sequence) {
        int i;
        if (aSN1Sequence.size() < 3 || aSN1Sequence.size() > 6) {
            throw new IllegalArgumentException("incorrect sequence size");
        }
        this.name = X500Name.getInstance(aSN1Sequence.getObjectAt(0));
        this.serialNumber = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(1));
        this.reason = CRLReason.getInstance(aSN1Sequence.getObjectAt(2));
        if (aSN1Sequence.size() <= 3 || !(aSN1Sequence.getObjectAt(3).toASN1Primitive() instanceof ASN1GeneralizedTime)) {
            i = 3;
        } else {
            i = 4;
            this.invalidityDate = ASN1GeneralizedTime.getInstance(aSN1Sequence.getObjectAt(3));
        }
        if (aSN1Sequence.size() > i && (aSN1Sequence.getObjectAt(i).toASN1Primitive() instanceof ASN1OctetString)) {
            this.passphrase = ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(i));
            i++;
        }
        if (aSN1Sequence.size() > i && (aSN1Sequence.getObjectAt(i).toASN1Primitive() instanceof DERUTF8String)) {
            this.comment = DERUTF8String.getInstance(aSN1Sequence.getObjectAt(i));
        }
    }

    public RevokeRequest(X500Name x500Name, ASN1Integer aSN1Integer, CRLReason cRLReason, ASN1GeneralizedTime aSN1GeneralizedTime, ASN1OctetString aSN1OctetString, DERUTF8String dERUTF8String) {
        this.name = x500Name;
        this.serialNumber = aSN1Integer;
        this.reason = cRLReason;
        this.invalidityDate = aSN1GeneralizedTime;
        this.passphrase = aSN1OctetString;
        this.comment = dERUTF8String;
    }

    public static RevokeRequest getInstance(Object obj) {
        if (obj instanceof RevokeRequest) {
            return (RevokeRequest) obj;
        }
        if (obj != null) {
            return new RevokeRequest(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public DERUTF8String getComment() {
        return this.comment;
    }

    public ASN1GeneralizedTime getInvalidityDate() {
        return this.invalidityDate;
    }

    public X500Name getName() {
        return this.name;
    }

    public byte[] getPassPhrase() {
        if (this.passphrase != null) {
            return Arrays.clone(this.passphrase.getOctets());
        }
        return null;
    }

    public ASN1OctetString getPassphrase() {
        return this.passphrase;
    }

    public CRLReason getReason() {
        return this.reason;
    }

    public BigInteger getSerialNumber() {
        return this.serialNumber.getValue();
    }

    public void setComment(DERUTF8String dERUTF8String) {
        this.comment = dERUTF8String;
    }

    public void setInvalidityDate(ASN1GeneralizedTime aSN1GeneralizedTime) {
        this.invalidityDate = aSN1GeneralizedTime;
    }

    public void setPassphrase(ASN1OctetString aSN1OctetString) {
        this.passphrase = aSN1OctetString;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.name);
        aSN1EncodableVector.add(this.serialNumber);
        aSN1EncodableVector.add(this.reason);
        if (this.invalidityDate != null) {
            aSN1EncodableVector.add(this.invalidityDate);
        }
        if (this.passphrase != null) {
            aSN1EncodableVector.add(this.passphrase);
        }
        if (this.comment != null) {
            aSN1EncodableVector.add(this.comment);
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
