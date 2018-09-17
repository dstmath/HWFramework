package com.android.org.bouncycastle.asn1.pkcs;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.BERSequence;
import com.android.org.bouncycastle.asn1.BERTaggedObject;
import com.android.org.bouncycastle.asn1.DLSequence;
import java.util.Enumeration;

public class ContentInfo extends ASN1Object implements PKCSObjectIdentifiers {
    private ASN1Encodable content;
    private ASN1ObjectIdentifier contentType;
    private boolean isBer = true;

    public static ContentInfo getInstance(Object obj) {
        if (obj instanceof ContentInfo) {
            return (ContentInfo) obj;
        }
        if (obj != null) {
            return new ContentInfo(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    private ContentInfo(ASN1Sequence seq) {
        Enumeration e = seq.getObjects();
        this.contentType = (ASN1ObjectIdentifier) e.nextElement();
        if (e.hasMoreElements()) {
            this.content = ((ASN1TaggedObject) e.nextElement()).getObject();
        }
        this.isBer = seq instanceof BERSequence;
    }

    public ContentInfo(ASN1ObjectIdentifier contentType, ASN1Encodable content) {
        this.contentType = contentType;
        this.content = content;
    }

    public ASN1ObjectIdentifier getContentType() {
        return this.contentType;
    }

    public ASN1Encodable getContent() {
        return this.content;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.contentType);
        if (this.content != null) {
            v.add(new BERTaggedObject(true, 0, this.content));
        }
        if (this.isBer) {
            return new BERSequence(v);
        }
        return new DLSequence(v);
    }
}
