package com.android.org.bouncycastle.asn1.pkcs;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.BERSequence;

public class Pfx extends ASN1Object implements PKCSObjectIdentifiers {
    private ContentInfo contentInfo;
    private MacData macData = null;

    private Pfx(ASN1Sequence seq) {
        if (((ASN1Integer) seq.getObjectAt(0)).getValue().intValue() == 3) {
            this.contentInfo = ContentInfo.getInstance(seq.getObjectAt(1));
            if (seq.size() == 3) {
                this.macData = MacData.getInstance(seq.getObjectAt(2));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("wrong version for PFX PDU");
    }

    public static Pfx getInstance(Object obj) {
        if (obj instanceof Pfx) {
            return (Pfx) obj;
        }
        if (obj != null) {
            return new Pfx(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public Pfx(ContentInfo contentInfo2, MacData macData2) {
        this.contentInfo = contentInfo2;
        this.macData = macData2;
    }

    public ContentInfo getAuthSafe() {
        return this.contentInfo;
    }

    public MacData getMacData() {
        return this.macData;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new ASN1Integer(3));
        v.add(this.contentInfo);
        if (this.macData != null) {
            v.add(this.macData);
        }
        return new BERSequence(v);
    }
}
