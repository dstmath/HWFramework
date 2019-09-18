package org.bouncycastle.asn1.pkcs;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.BERSequence;
import org.bouncycastle.asn1.BERTaggedObject;
import org.bouncycastle.asn1.DLSequence;

public class ContentInfo extends ASN1Object implements PKCSObjectIdentifiers {
    private ASN1Encodable content;
    private ASN1ObjectIdentifier contentType;
    private boolean isBer = true;

    public ContentInfo(ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1Encodable aSN1Encodable) {
        this.contentType = aSN1ObjectIdentifier;
        this.content = aSN1Encodable;
    }

    private ContentInfo(ASN1Sequence aSN1Sequence) {
        Enumeration objects = aSN1Sequence.getObjects();
        this.contentType = (ASN1ObjectIdentifier) objects.nextElement();
        if (objects.hasMoreElements()) {
            this.content = ((ASN1TaggedObject) objects.nextElement()).getObject();
        }
        this.isBer = aSN1Sequence instanceof BERSequence;
    }

    public static ContentInfo getInstance(Object obj) {
        if (obj instanceof ContentInfo) {
            return (ContentInfo) obj;
        }
        if (obj != null) {
            return new ContentInfo(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public ASN1Encodable getContent() {
        return this.content;
    }

    public ASN1ObjectIdentifier getContentType() {
        return this.contentType;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.contentType);
        if (this.content != null) {
            aSN1EncodableVector.add(new BERTaggedObject(true, 0, this.content));
        }
        return this.isBer ? new BERSequence(aSN1EncodableVector) : new DLSequence(aSN1EncodableVector);
    }
}
