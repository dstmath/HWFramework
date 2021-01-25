package org.bouncycastle.asn1.tsp;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;

public class TSTInfo extends ASN1Object {
    private Accuracy accuracy;
    private Extensions extensions;
    private ASN1GeneralizedTime genTime;
    private MessageImprint messageImprint;
    private ASN1Integer nonce;
    private ASN1Boolean ordering;
    private ASN1Integer serialNumber;
    private GeneralName tsa;
    private ASN1ObjectIdentifier tsaPolicyId;
    private ASN1Integer version;

    public TSTInfo(ASN1ObjectIdentifier aSN1ObjectIdentifier, MessageImprint messageImprint2, ASN1Integer aSN1Integer, ASN1GeneralizedTime aSN1GeneralizedTime, Accuracy accuracy2, ASN1Boolean aSN1Boolean, ASN1Integer aSN1Integer2, GeneralName generalName, Extensions extensions2) {
        this.version = new ASN1Integer(1);
        this.tsaPolicyId = aSN1ObjectIdentifier;
        this.messageImprint = messageImprint2;
        this.serialNumber = aSN1Integer;
        this.genTime = aSN1GeneralizedTime;
        this.accuracy = accuracy2;
        this.ordering = aSN1Boolean;
        this.nonce = aSN1Integer2;
        this.tsa = generalName;
        this.extensions = extensions2;
    }

    private TSTInfo(ASN1Sequence aSN1Sequence) {
        Enumeration objects = aSN1Sequence.getObjects();
        this.version = ASN1Integer.getInstance(objects.nextElement());
        this.tsaPolicyId = ASN1ObjectIdentifier.getInstance(objects.nextElement());
        this.messageImprint = MessageImprint.getInstance(objects.nextElement());
        this.serialNumber = ASN1Integer.getInstance(objects.nextElement());
        this.genTime = ASN1GeneralizedTime.getInstance(objects.nextElement());
        ASN1Boolean instance = ASN1Boolean.getInstance(false);
        while (true) {
            this.ordering = instance;
            while (objects.hasMoreElements()) {
                ASN1Object aSN1Object = (ASN1Object) objects.nextElement();
                if (aSN1Object instanceof ASN1TaggedObject) {
                    ASN1TaggedObject aSN1TaggedObject = (ASN1TaggedObject) aSN1Object;
                    int tagNo = aSN1TaggedObject.getTagNo();
                    if (tagNo == 0) {
                        this.tsa = GeneralName.getInstance(aSN1TaggedObject, true);
                    } else if (tagNo == 1) {
                        this.extensions = Extensions.getInstance(aSN1TaggedObject, false);
                    } else {
                        throw new IllegalArgumentException("Unknown tag value " + aSN1TaggedObject.getTagNo());
                    }
                } else if ((aSN1Object instanceof ASN1Sequence) || (aSN1Object instanceof Accuracy)) {
                    this.accuracy = Accuracy.getInstance(aSN1Object);
                } else if (aSN1Object instanceof ASN1Boolean) {
                    instance = ASN1Boolean.getInstance(aSN1Object);
                } else if (aSN1Object instanceof ASN1Integer) {
                    this.nonce = ASN1Integer.getInstance(aSN1Object);
                }
            }
            return;
        }
    }

    public static TSTInfo getInstance(Object obj) {
        if (obj instanceof TSTInfo) {
            return (TSTInfo) obj;
        }
        if (obj != null) {
            return new TSTInfo(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public Accuracy getAccuracy() {
        return this.accuracy;
    }

    public Extensions getExtensions() {
        return this.extensions;
    }

    public ASN1GeneralizedTime getGenTime() {
        return this.genTime;
    }

    public MessageImprint getMessageImprint() {
        return this.messageImprint;
    }

    public ASN1Integer getNonce() {
        return this.nonce;
    }

    public ASN1Boolean getOrdering() {
        return this.ordering;
    }

    public ASN1ObjectIdentifier getPolicy() {
        return this.tsaPolicyId;
    }

    public ASN1Integer getSerialNumber() {
        return this.serialNumber;
    }

    public GeneralName getTsa() {
        return this.tsa;
    }

    public ASN1Integer getVersion() {
        return this.version;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(10);
        aSN1EncodableVector.add(this.version);
        aSN1EncodableVector.add(this.tsaPolicyId);
        aSN1EncodableVector.add(this.messageImprint);
        aSN1EncodableVector.add(this.serialNumber);
        aSN1EncodableVector.add(this.genTime);
        Accuracy accuracy2 = this.accuracy;
        if (accuracy2 != null) {
            aSN1EncodableVector.add(accuracy2);
        }
        ASN1Boolean aSN1Boolean = this.ordering;
        if (aSN1Boolean != null && aSN1Boolean.isTrue()) {
            aSN1EncodableVector.add(this.ordering);
        }
        ASN1Integer aSN1Integer = this.nonce;
        if (aSN1Integer != null) {
            aSN1EncodableVector.add(aSN1Integer);
        }
        GeneralName generalName = this.tsa;
        if (generalName != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 0, generalName));
        }
        Extensions extensions2 = this.extensions;
        if (extensions2 != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 1, extensions2));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
