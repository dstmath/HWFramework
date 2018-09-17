package com.android.org.bouncycastle.asn1.x9;

import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.math.ec.ECFieldElement;
import java.math.BigInteger;

public class X9IntegerConverter {
    public int getByteLength(ECCurve c) {
        return (c.getFieldSize() + 7) / 8;
    }

    public int getByteLength(ECFieldElement fe) {
        return (fe.getFieldSize() + 7) / 8;
    }

    public byte[] integerToBytes(BigInteger s, int qLength) {
        byte[] bytes = s.toByteArray();
        byte[] tmp;
        if (qLength < bytes.length) {
            tmp = new byte[qLength];
            System.arraycopy(bytes, bytes.length - tmp.length, tmp, 0, tmp.length);
            return tmp;
        } else if (qLength <= bytes.length) {
            return bytes;
        } else {
            tmp = new byte[qLength];
            System.arraycopy(bytes, 0, tmp, tmp.length - bytes.length, bytes.length);
            return tmp;
        }
    }
}
