package org.bouncycastle.asn1;

import java.io.IOException;
import java.math.BigInteger;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Properties;

public class ASN1Integer extends ASN1Primitive {
    static final int SIGN_EXT_SIGNED = -1;
    static final int SIGN_EXT_UNSIGNED = 255;
    private final byte[] bytes;
    private final int start;

    public ASN1Integer(long j) {
        this.bytes = BigInteger.valueOf(j).toByteArray();
        this.start = 0;
    }

    public ASN1Integer(BigInteger bigInteger) {
        this.bytes = bigInteger.toByteArray();
        this.start = 0;
    }

    public ASN1Integer(byte[] bArr) {
        this(bArr, true);
    }

    ASN1Integer(byte[] bArr, boolean z) {
        if (!isMalformed(bArr)) {
            this.bytes = z ? Arrays.clone(bArr) : bArr;
            this.start = signBytesToSkip(bArr);
            return;
        }
        throw new IllegalArgumentException("malformed integer");
    }

    public static ASN1Integer getInstance(Object obj) {
        if (obj == null || (obj instanceof ASN1Integer)) {
            return (ASN1Integer) obj;
        }
        if (obj instanceof byte[]) {
            try {
                return (ASN1Integer) fromByteArray((byte[]) obj);
            } catch (Exception e) {
                throw new IllegalArgumentException("encoding error in getInstance: " + e.toString());
            }
        } else {
            throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
        }
    }

    public static ASN1Integer getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        ASN1Primitive object = aSN1TaggedObject.getObject();
        return (z || (object instanceof ASN1Integer)) ? getInstance(object) : new ASN1Integer(ASN1OctetString.getInstance(object).getOctets());
    }

    static int intValue(byte[] bArr, int i, int i2) {
        int length = bArr.length;
        int max = Math.max(i, length - 4);
        int i3 = i2 & bArr[max];
        while (true) {
            max++;
            if (max >= length) {
                return i3;
            }
            i3 = (i3 << 8) | (bArr[max] & 255);
        }
    }

    static boolean isMalformed(byte[] bArr) {
        int length = bArr.length;
        if (length == 0) {
            return true;
        }
        if (length != 1) {
            return bArr[0] == (bArr[1] >> 7) && !Properties.isOverrideSet("org.bouncycastle.asn1.allow_unsafe_integer");
        }
        return false;
    }

    static long longValue(byte[] bArr, int i, int i2) {
        int length = bArr.length;
        int max = Math.max(i, length - 8);
        long j = (long) (i2 & bArr[max]);
        while (true) {
            max++;
            if (max >= length) {
                return j;
            }
            j = (j << 8) | ((long) (bArr[max] & 255));
        }
    }

    static int signBytesToSkip(byte[] bArr) {
        int length = bArr.length - 1;
        int i = 0;
        while (i < length) {
            int i2 = i + 1;
            if (bArr[i] != (bArr[i2] >> 7)) {
                break;
            }
            i = i2;
        }
        return i;
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public boolean asn1Equals(ASN1Primitive aSN1Primitive) {
        if (!(aSN1Primitive instanceof ASN1Integer)) {
            return false;
        }
        return Arrays.areEqual(this.bytes, ((ASN1Integer) aSN1Primitive).bytes);
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public void encode(ASN1OutputStream aSN1OutputStream, boolean z) throws IOException {
        aSN1OutputStream.writeEncoded(z, 2, this.bytes);
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public int encodedLength() {
        return StreamUtil.calculateBodyLength(this.bytes.length) + 1 + this.bytes.length;
    }

    public BigInteger getPositiveValue() {
        return new BigInteger(1, this.bytes);
    }

    public BigInteger getValue() {
        return new BigInteger(this.bytes);
    }

    public boolean hasValue(BigInteger bigInteger) {
        return bigInteger != null && intValue(this.bytes, this.start, -1) == bigInteger.intValue() && getValue().equals(bigInteger);
    }

    @Override // org.bouncycastle.asn1.ASN1Primitive, org.bouncycastle.asn1.ASN1Object
    public int hashCode() {
        return Arrays.hashCode(this.bytes);
    }

    public int intPositiveValueExact() {
        byte[] bArr = this.bytes;
        int length = bArr.length;
        int i = this.start;
        int i2 = length - i;
        if (i2 <= 4 && (i2 != 4 || (bArr[i] & 128) == 0)) {
            return intValue(this.bytes, this.start, 255);
        }
        throw new ArithmeticException("ASN.1 Integer out of positive int range");
    }

    public int intValueExact() {
        byte[] bArr = this.bytes;
        int length = bArr.length;
        int i = this.start;
        if (length - i <= 4) {
            return intValue(bArr, i, -1);
        }
        throw new ArithmeticException("ASN.1 Integer out of int range");
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public boolean isConstructed() {
        return false;
    }

    public long longValueExact() {
        byte[] bArr = this.bytes;
        int length = bArr.length;
        int i = this.start;
        if (length - i <= 8) {
            return longValue(bArr, i, -1);
        }
        throw new ArithmeticException("ASN.1 Integer out of long range");
    }

    public String toString() {
        return getValue().toString();
    }
}
