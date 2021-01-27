package org.bouncycastle.asn1.pkcs;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.BERSequence;
import org.bouncycastle.asn1.DLSequence;

public class AuthenticatedSafe extends ASN1Object {
    private ContentInfo[] info;
    private boolean isBer = true;

    private AuthenticatedSafe(ASN1Sequence aSN1Sequence) {
        this.info = new ContentInfo[aSN1Sequence.size()];
        int i = 0;
        while (true) {
            ContentInfo[] contentInfoArr = this.info;
            if (i != contentInfoArr.length) {
                contentInfoArr[i] = ContentInfo.getInstance(aSN1Sequence.getObjectAt(i));
                i++;
            } else {
                this.isBer = aSN1Sequence instanceof BERSequence;
                return;
            }
        }
    }

    public AuthenticatedSafe(ContentInfo[] contentInfoArr) {
        this.info = copy(contentInfoArr);
    }

    private ContentInfo[] copy(ContentInfo[] contentInfoArr) {
        ContentInfo[] contentInfoArr2 = new ContentInfo[contentInfoArr.length];
        System.arraycopy(contentInfoArr, 0, contentInfoArr2, 0, contentInfoArr2.length);
        return contentInfoArr2;
    }

    public static AuthenticatedSafe getInstance(Object obj) {
        if (obj instanceof AuthenticatedSafe) {
            return (AuthenticatedSafe) obj;
        }
        if (obj != null) {
            return new AuthenticatedSafe(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public ContentInfo[] getContentInfo() {
        return copy(this.info);
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        return this.isBer ? new BERSequence(this.info) : new DLSequence(this.info);
    }
}
