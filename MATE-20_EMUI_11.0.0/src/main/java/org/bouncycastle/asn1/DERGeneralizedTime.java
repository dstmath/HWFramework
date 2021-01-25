package org.bouncycastle.asn1;

import java.io.IOException;
import java.util.Date;
import org.bouncycastle.util.Strings;

public class DERGeneralizedTime extends ASN1GeneralizedTime {
    public DERGeneralizedTime(String str) {
        super(str);
    }

    public DERGeneralizedTime(Date date) {
        super(date);
    }

    public DERGeneralizedTime(byte[] bArr) {
        super(bArr);
    }

    private byte[] getDERTime() {
        byte[] bArr;
        byte[] byteArray;
        int length;
        int i;
        if (this.time[this.time.length - 1] != 90) {
            return this.time;
        }
        if (!hasMinutes()) {
            bArr = new byte[(this.time.length + 4)];
            System.arraycopy(this.time, 0, bArr, 0, this.time.length - 1);
            byteArray = Strings.toByteArray("0000Z");
            length = this.time.length - 1;
            i = 5;
        } else if (!hasSeconds()) {
            bArr = new byte[(this.time.length + 2)];
            System.arraycopy(this.time, 0, bArr, 0, this.time.length - 1);
            byteArray = Strings.toByteArray("00Z");
            length = this.time.length - 1;
            i = 3;
        } else if (!hasFractionalSeconds()) {
            return this.time;
        } else {
            int length2 = this.time.length - 2;
            while (length2 > 0 && this.time[length2] == 48) {
                length2--;
            }
            if (this.time[length2] == 46) {
                byte[] bArr2 = new byte[(length2 + 1)];
                System.arraycopy(this.time, 0, bArr2, 0, length2);
                bArr2[length2] = 90;
                return bArr2;
            }
            byte[] bArr3 = new byte[(length2 + 2)];
            int i2 = length2 + 1;
            System.arraycopy(this.time, 0, bArr3, 0, i2);
            bArr3[i2] = 90;
            return bArr3;
        }
        System.arraycopy(byteArray, 0, bArr, length, i);
        return bArr;
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1GeneralizedTime, org.bouncycastle.asn1.ASN1Primitive
    public void encode(ASN1OutputStream aSN1OutputStream, boolean z) throws IOException {
        aSN1OutputStream.writeEncoded(z, 24, getDERTime());
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1GeneralizedTime, org.bouncycastle.asn1.ASN1Primitive
    public int encodedLength() {
        int length = getDERTime().length;
        return StreamUtil.calculateBodyLength(length) + 1 + length;
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1GeneralizedTime, org.bouncycastle.asn1.ASN1Primitive
    public ASN1Primitive toDERObject() {
        return this;
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1GeneralizedTime, org.bouncycastle.asn1.ASN1Primitive
    public ASN1Primitive toDLObject() {
        return this;
    }
}
