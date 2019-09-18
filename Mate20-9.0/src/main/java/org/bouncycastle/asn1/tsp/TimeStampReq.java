package org.bouncycastle.asn1.tsp;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.Extensions;

public class TimeStampReq extends ASN1Object {
    ASN1Boolean certReq;
    Extensions extensions;
    MessageImprint messageImprint;
    ASN1Integer nonce;
    ASN1ObjectIdentifier tsaPolicy;
    ASN1Integer version;

    private TimeStampReq(ASN1Sequence aSN1Sequence) {
        int size = aSN1Sequence.size();
        this.version = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(0));
        this.messageImprint = MessageImprint.getInstance(aSN1Sequence.getObjectAt(1));
        for (int i = 2; i < size; i++) {
            if (aSN1Sequence.getObjectAt(i) instanceof ASN1ObjectIdentifier) {
                this.tsaPolicy = ASN1ObjectIdentifier.getInstance(aSN1Sequence.getObjectAt(i));
            } else if (aSN1Sequence.getObjectAt(i) instanceof ASN1Integer) {
                this.nonce = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(i));
            } else if (aSN1Sequence.getObjectAt(i) instanceof ASN1Boolean) {
                this.certReq = ASN1Boolean.getInstance((Object) aSN1Sequence.getObjectAt(i));
            } else if (aSN1Sequence.getObjectAt(i) instanceof ASN1TaggedObject) {
                ASN1TaggedObject aSN1TaggedObject = (ASN1TaggedObject) aSN1Sequence.getObjectAt(i);
                if (aSN1TaggedObject.getTagNo() == 0) {
                    this.extensions = Extensions.getInstance(aSN1TaggedObject, false);
                }
            }
        }
    }

    public TimeStampReq(MessageImprint messageImprint2, ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1Integer aSN1Integer, ASN1Boolean aSN1Boolean, Extensions extensions2) {
        this.version = new ASN1Integer(1);
        this.messageImprint = messageImprint2;
        this.tsaPolicy = aSN1ObjectIdentifier;
        this.nonce = aSN1Integer;
        this.certReq = aSN1Boolean;
        this.extensions = extensions2;
    }

    public static TimeStampReq getInstance(Object obj) {
        if (obj instanceof TimeStampReq) {
            return (TimeStampReq) obj;
        }
        if (obj != null) {
            return new TimeStampReq(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public ASN1Boolean getCertReq() {
        return this.certReq;
    }

    public Extensions getExtensions() {
        return this.extensions;
    }

    public MessageImprint getMessageImprint() {
        return this.messageImprint;
    }

    public ASN1Integer getNonce() {
        return this.nonce;
    }

    public ASN1ObjectIdentifier getReqPolicy() {
        return this.tsaPolicy;
    }

    public ASN1Integer getVersion() {
        return this.version;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.version);
        aSN1EncodableVector.add(this.messageImprint);
        if (this.tsaPolicy != null) {
            aSN1EncodableVector.add(this.tsaPolicy);
        }
        if (this.nonce != null) {
            aSN1EncodableVector.add(this.nonce);
        }
        if (this.certReq != null && this.certReq.isTrue()) {
            aSN1EncodableVector.add(this.certReq);
        }
        if (this.extensions != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 0, this.extensions));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
