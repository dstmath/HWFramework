package org.bouncycastle.asn1.x9;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.Arrays;

public class X9ECPoint extends ASN1Object {
    private ECCurve c;
    private final ASN1OctetString encoding;
    private ECPoint p;

    public X9ECPoint(ECCurve eCCurve, ASN1OctetString aSN1OctetString) {
        this(eCCurve, aSN1OctetString.getOctets());
    }

    public X9ECPoint(ECCurve eCCurve, byte[] bArr) {
        this.c = eCCurve;
        this.encoding = new DEROctetString(Arrays.clone(bArr));
    }

    public X9ECPoint(ECPoint eCPoint) {
        this(eCPoint, false);
    }

    public X9ECPoint(ECPoint eCPoint, boolean z) {
        this.p = eCPoint.normalize();
        this.encoding = new DEROctetString(eCPoint.getEncoded(z));
    }

    public synchronized ECPoint getPoint() {
        if (this.p == null) {
            this.p = this.c.decodePoint(this.encoding.getOctets()).normalize();
        }
        return this.p;
    }

    public byte[] getPointEncoding() {
        return Arrays.clone(this.encoding.getOctets());
    }

    public boolean isPointCompressed() {
        byte[] octets = this.encoding.getOctets();
        if (octets == null || octets.length <= 0) {
            return false;
        }
        return octets[0] == 2 || octets[0] == 3;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.encoding;
    }
}
