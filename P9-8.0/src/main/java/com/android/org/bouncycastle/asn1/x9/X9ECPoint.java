package com.android.org.bouncycastle.asn1.x9;

import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.DEROctetString;
import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.math.ec.ECPoint;
import com.android.org.bouncycastle.util.Arrays;

public class X9ECPoint extends ASN1Object {
    private ECCurve c;
    private final ASN1OctetString encoding;
    private ECPoint p;

    public X9ECPoint(ECPoint p) {
        this(p, false);
    }

    public X9ECPoint(ECPoint p, boolean compressed) {
        this.p = p.normalize();
        this.encoding = new DEROctetString(p.getEncoded(compressed));
    }

    public X9ECPoint(ECCurve c, byte[] encoding) {
        this.c = c;
        this.encoding = new DEROctetString(Arrays.clone(encoding));
    }

    public X9ECPoint(ECCurve c, ASN1OctetString s) {
        this(c, s.getOctets());
    }

    public byte[] getPointEncoding() {
        return Arrays.clone(this.encoding.getOctets());
    }

    public synchronized ECPoint getPoint() {
        if (this.p == null) {
            this.p = this.c.decodePoint(this.encoding.getOctets()).normalize();
        }
        return this.p;
    }

    public boolean isPointCompressed() {
        byte[] octets = this.encoding.getOctets();
        if (octets == null || octets.length <= 0 || (octets[0] != (byte) 2 && octets[0] != (byte) 3)) {
            return false;
        }
        return true;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.encoding;
    }
}
