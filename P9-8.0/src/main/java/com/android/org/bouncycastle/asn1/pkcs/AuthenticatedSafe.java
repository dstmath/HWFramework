package com.android.org.bouncycastle.asn1.pkcs;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.BERSequence;
import com.android.org.bouncycastle.asn1.DLSequence;

public class AuthenticatedSafe extends ASN1Object {
    private ContentInfo[] info;
    private boolean isBer = true;

    private AuthenticatedSafe(ASN1Sequence seq) {
        this.info = new ContentInfo[seq.size()];
        for (int i = 0; i != this.info.length; i++) {
            this.info[i] = ContentInfo.getInstance(seq.getObjectAt(i));
        }
        this.isBer = seq instanceof BERSequence;
    }

    public static AuthenticatedSafe getInstance(Object o) {
        if (o instanceof AuthenticatedSafe) {
            return (AuthenticatedSafe) o;
        }
        if (o != null) {
            return new AuthenticatedSafe(ASN1Sequence.getInstance(o));
        }
        return null;
    }

    public AuthenticatedSafe(ContentInfo[] info) {
        this.info = info;
    }

    public ContentInfo[] getContentInfo() {
        return this.info;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        for (int i = 0; i != this.info.length; i++) {
            v.add(this.info[i]);
        }
        if (this.isBer) {
            return new BERSequence(v);
        }
        return new DLSequence(v);
    }
}
