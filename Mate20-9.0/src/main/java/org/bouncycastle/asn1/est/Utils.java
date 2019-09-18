package org.bouncycastle.asn1.est;

class Utils {
    Utils() {
    }

    static AttrOrOID[] clone(AttrOrOID[] attrOrOIDArr) {
        AttrOrOID[] attrOrOIDArr2 = new AttrOrOID[attrOrOIDArr.length];
        System.arraycopy(attrOrOIDArr, 0, attrOrOIDArr2, 0, attrOrOIDArr.length);
        return attrOrOIDArr2;
    }
}
