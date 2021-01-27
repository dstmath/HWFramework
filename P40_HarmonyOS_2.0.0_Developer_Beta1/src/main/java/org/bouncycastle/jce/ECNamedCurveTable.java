package org.bouncycastle.jce;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;

public class ECNamedCurveTable {
    public static Enumeration getNames() {
        return org.bouncycastle.asn1.x9.ECNamedCurveTable.getNames();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0013, code lost:
        r0 = org.bouncycastle.asn1.x9.ECNamedCurveTable.getByName(r8);
     */
    public static ECNamedCurveParameterSpec getParameterSpec(String str) {
        X9ECParameters byName = CustomNamedCurves.getByName(str);
        if (byName == null) {
            try {
                byName = CustomNamedCurves.getByOID(new ASN1ObjectIdentifier(str));
            } catch (IllegalArgumentException e) {
            }
            if (byName == null && byName == null) {
                try {
                    byName = org.bouncycastle.asn1.x9.ECNamedCurveTable.getByOID(new ASN1ObjectIdentifier(str));
                } catch (IllegalArgumentException e2) {
                }
            }
        }
        if (byName == null) {
            return null;
        }
        return new ECNamedCurveParameterSpec(str, byName.getCurve(), byName.getG(), byName.getN(), byName.getH(), byName.getSeed());
    }
}
