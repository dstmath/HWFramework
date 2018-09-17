package com.android.org.bouncycastle.asn1.cms;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.DEROctetString;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.util.Arrays;

public class GCMParameters extends ASN1Object {
    private int icvLen;
    private byte[] nonce;

    public static GCMParameters getInstance(Object obj) {
        if (obj instanceof GCMParameters) {
            return (GCMParameters) obj;
        }
        if (obj != null) {
            return new GCMParameters(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    private GCMParameters(ASN1Sequence seq) {
        this.nonce = ASN1OctetString.getInstance(seq.getObjectAt(0)).getOctets();
        if (seq.size() == 2) {
            this.icvLen = ASN1Integer.getInstance(seq.getObjectAt(1)).getValue().intValue();
        } else {
            this.icvLen = 12;
        }
    }

    public GCMParameters(byte[] nonce, int icvLen) {
        this.nonce = Arrays.clone(nonce);
        this.icvLen = icvLen;
    }

    public byte[] getNonce() {
        return Arrays.clone(this.nonce);
    }

    public int getIcvLen() {
        return this.icvLen;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new DEROctetString(this.nonce));
        if (this.icvLen != 12) {
            v.add(new ASN1Integer((long) this.icvLen));
        }
        return new DERSequence(v);
    }
}
